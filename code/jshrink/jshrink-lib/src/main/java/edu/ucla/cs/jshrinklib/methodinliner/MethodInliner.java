package edu.ucla.cs.jshrinklib.methodinliner;

import edu.ucla.cs.jshrinklib.JShrink;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.invoke.SiteInliner;

import java.io.File;
import java.util.*;

public class MethodInliner {
	private final static boolean debug = false;

	/**
	 * This method will inline any methods called from a single location (the callee method is subsequently removed)
	 * @param callgraph The call graph, represented as a callgraph (Map<Callee, Set<Caller>>). Modified as the actual
	 * 	callgraph is
	 * @param classpaths The classpaths of the code that are deemed modifiable.
	 * @return A InlineData object --- this documents what methods have been inlined, where, and what methods have been\
	 * 	modified
	 */
	public static InlineData inlineMethods(Map<SootMethod, Set<SootMethod>> callgraph, Set<File> classpaths, Set<String> unmodifiableClasses){

		InlineData toReturn = new InlineData();
		Set<SootMethod> methodsRemoved = new HashSet<SootMethod>();

		boolean callgraphChanged = false;

		Comparator<SootMethod> comparator = new Comparator<SootMethod>() {
			@Override
			public int compare(SootMethod o1, SootMethod o2) {
				return o1.getSignature().compareTo(o2.getSignature());
			}
		};

		do{
			SortedMap<SootMethod, Set<SootMethod>> sortedCallgraph = new TreeMap<SootMethod, Set<SootMethod>>(comparator);
			sortedCallgraph.putAll(callgraph);
			callgraphChanged = false;
			for (Map.Entry<SootMethod, Set<SootMethod>> entry : sortedCallgraph.entrySet()) {

				//We are only interested inlining methods if there is only one line site
				if (entry.getValue().size() != 1) {
					if(debug){
						System.out.println();
						System.out.println("More than one call location for " + entry.getKey().getSignature());
					}
					continue;
				}

				SootMethod caller = entry.getValue().iterator().next();
				SootMethod callee = entry.getKey();

				if(debug){
					System.out.println();
					System.out.println("Attempting to inline " + callee.getSignature()
						+ " at " + caller.getSignature() + ".");
				}

				//Both the caller and callee classes must be within the current classpaths.
				if (ClassFileUtils.classInPath(callee.getDeclaringClass().getName(), classpaths).isEmpty()
					|| ClassFileUtils.classInPath(caller.getDeclaringClass().getName(), classpaths).isEmpty()) {
					if(debug){
						System.out.println("FAILED: Caller or Callee not within the current classpath");
					}
					continue;
				}

				if(callee.getDeclaringClass().isEnum() || caller.getDeclaringClass().isEnum()){
					if(debug){
						System.out.println("FAILED: Caller or Callee is an ENUM.");
					}
					continue;
				}

				/*
				We do not consider inner-classes at this time. They are complex corner-cases that are not handled well
				by Soot's inliner.
				 */
				if(!callee.getDeclaringClass().getName().equals(caller.getDeclaringClass().getName())
					&& (caller.getDeclaringClass().getName().contains("$")
					|| callee.getDeclaringClass().getName().contains("$"))){
					if(debug){
						System.out.println("FAILED: Caller or Callee class is an inner class.");
					}
					continue;
				}


				try {
					callee.retrieveActiveBody();
					caller.retrieveActiveBody();
				} catch (Exception e) {
					//This is a catch all --- if the methods can't be retrieved, we can't inline them.
					if(debug){
						System.out.println("FAILED: Cannot retrieve active body for caller or callee.");
					}
					continue;
				}

				/*
				This checks that if the callee comes from a different class, that it contains no references to inner
				classes.
				 */
				if(!caller.getDeclaringClass().equals(callee.getDeclaringClass())) {
					Set<String> classRefs = classesReferenced(callee.retrieveActiveBody());
					boolean incompatibleRef = false;
					for (String classRef : classRefs) {
						if (classRef.startsWith(callee.getDeclaringClass().getName() + "$")) {
							if (debug) {
								System.out.println("FAILED: Callee contains reference to inner class field/method.");
							}
							incompatibleRef = true;
							break;
						}
					}
					if (incompatibleRef) {
						continue;
					}
				}


				//The caller and the callee must be contained in a SootClasses that are ultimately modifiable.
//				if (!SootUtils.modifiableSootClass(caller.getDeclaringClass())
//					|| !SootUtils.modifiableSootClass(callee.getDeclaringClass())) {
				if(unmodifiableClasses.contains(caller.getDeclaringClass().getName()) ||
					unmodifiableClasses.contains(callee.getDeclaringClass().getName())) {
					if(debug){
						System.out.println("FAILED: Caller or Callee not within modifiable SootClass.");
					}
					continue;
				}

				/*
				We do not inline constructors (unless within a constructor in the same class). Doing so can cause
				problems with the MethodWiper component.
				 */
				if (callee.isConstructor()) {
					if (!(caller.getDeclaringClass().equals(callee.getDeclaringClass()) && caller.isConstructor())) {
						if(debug){
							System.out.println("FAILED: Callee is a constructor.");
						}
						continue;
					}
				}

				//We ignore access methods (created by the compiler for inner classes).
				if (callee.getName().startsWith("access$") || caller.getName().startsWith("access$")) {
					if(debug) {
						System.out.println("Caller or Callee is access$ methood");
					}
					continue;
				}


				if(JShrink.enable_member_visibility) {
					/*
					 * Check that inlining the method does not break any access controls (references to
					 * private/package-private methods, etc.). If this check is ignored, IllegalAccessExceptions can be
					 * thrown at runtime.
					 */
					if(!accessControlsOk(callee, caller.getDeclaringClass())){
						if(debug){
							System.out.println("Inlining the callee would violate Java access controls.");
						}
						continue;
					}
				}

				Body b = caller.retrieveActiveBody();
				List<Map.Entry<Stmt, Boolean>> toInline = toInline(b, callee);

				// There must be exactly 1 inline site in the caller method.
				if (toInline.size() != 1) {
					if(debug){
						System.out.println("FAILED: More than 1 inline site.");
					}
					continue;
				}

				Stmt site = toInline.get(0).getKey();

				//If it's an instance of Dynamic Invocation, this is a special case that must be handled carefully.
				//If they are multiple potential targets, then we must skip this potential inlining.
				if(toInline.get(0).getValue()){
					Set<SootMethod> calledFromCaller = new HashSet<SootMethod>();
					for( Map.Entry<SootMethod, Set<SootMethod>> entry2 : sortedCallgraph.entrySet()){
						if(entry2.getValue().contains(caller)){
							calledFromCaller.add(entry2.getKey());
						}
					}

					boolean cont = false;
					for(SootMethod sootMethod: calledFromCaller){
						if(!sootMethod.equals(callee) && sootMethod.getSubSignature().equals(callee.getSubSignature())){
							cont = true;
							break;
						}
					}

					if(cont){
						if(debug){
							System.out.println("Failed: Dynamic Dispatch with more than one candidate.");
						}
						continue;
					}
				}

				/*
				I'm not sure exactly what this does, but I think it's good to use Soot's own "Inlinability" check here.
				ModifierOptions: "safe", "unsafe", or "nochanges". Though, at the time of writing, "unsafe" is the only
				option that's been implemented. "unsafe" means that the inline may be unsafe but is possible.
				*/
				boolean isInlineSafe;
				try {
					isInlineSafe = InlinerSafetyManager.ensureInlinability(callee, site, caller, "unsafe");
				} catch (Exception e) {
					// suppress the exception and just do not inline this
					if(debug) {
						System.out.println("FAILED: exception occurs when checking inline safety.");
						System.out.println(e.getMessage());
						System.out.println(e.getStackTrace());
					}

					continue;
				}
				if (!isInlineSafe) {
					if(debug){
						System.out.println("FAILED: InlineSafetyManager.ensureInlinability returned false.");
					}
					continue;
				}

				boolean isInlineEfficient;
				try {
					isInlineEfficient = inlineIsEfficient(callee, site, caller);
				} catch (Exception e) {
					// suppress the exception and just do not inline this
					if(debug) {
						System.out.println("FAILED: exception occurs when checking inline efficiency.");
						System.out.println(e.getMessage());
						System.out.println(e.getStackTrace());
					}

					continue;
				}
				if(!isInlineEfficient){
					if(debug){
						System.out.println("FAILED: This inline operation would increase the size of the app.");
					}
					continue;
				}


				//I don't know why I have to do this again, but I get errors otherwise.
				try {
					b = caller.retrieveActiveBody();
					toInline = toInline(b, callee);
					site = toInline.get(0).getKey();
				}catch(IndexOutOfBoundsException e){
					/*
					Sometimes due to Soot parsing bytecode incorrectly, the "toInline" function returns an empty
					ArrayList. In these rare cases I think it best just to skip the method inlining for those methods.
					It's a cheap fix but shouldn't have a big impact.
					*/
					continue;

				}

				//Inline the method
				SiteInliner.inlineSite(callee, site, caller);

				//Record the inlined method.
				toReturn.addInlinedMethods(SootUtils.sootMethodToMethodData(callee),
					SootUtils.sootMethodToMethodData(caller));
				toReturn.addClassModified(caller.getDeclaringClass());

				//Remove update our call graph information (I admit this is a bit inefficient but it's simple).
				for (Map.Entry<SootMethod, Set<SootMethod>> entry2 : callgraph.entrySet()) {
					if (entry2.getValue().contains(callee)) {
						entry2.getValue().remove(callee);
						entry2.getValue().add(caller);
						callgraphChanged=true;
					}
				}

				if(callgraph.containsKey(callee)) {
					callgraph.remove(callee);
					callgraphChanged=true;
				}

				if(debug){
					System.out.println("SUCCESS!");
				}
			}
		}while(callgraphChanged);

		//Update the call graph with the removed methods.
		for(SootMethod sootMethod : methodsRemoved){
			callgraph.remove(sootMethod);
		}

		return toReturn;
	}

	//Returns the statements, and whether they are an dynamic invocation or not.
	private static List<Map.Entry<Stmt, Boolean>> toInline(Body b, SootMethod callee){
		List<Map.Entry<Stmt, Boolean>> toReturn = new ArrayList<Map.Entry<Stmt, Boolean>>();
		for (Unit u : b.getUnits()) {
			InvokeExpr invokeExpr = null;
			if(u instanceof InvokeStmt){
				invokeExpr = ((InvokeStmt)u).getInvokeExpr();
			} else if(u instanceof JAssignStmt && ((JAssignStmt)u).containsInvokeExpr()){
				invokeExpr = ((JAssignStmt) u).getInvokeExpr();
			}



			if(invokeExpr != null){
				SootMethod sootMethod = invokeExpr.getMethod();
				if(invokeExpr instanceof  DynamicInvokeExpr || invokeExpr instanceof VirtualInvokeExpr){
					if(sootMethod.getSubSignature().equals(callee.getSubSignature())){
						toReturn.add(new HashMap.SimpleEntry<Stmt, Boolean>((Stmt) u, true));
					}
				} else {
					if(sootMethod.equals(callee)){
						toReturn.add(new HashMap.SimpleEntry<Stmt, Boolean>((Stmt) u, false));
					}
				}
			}
		}
		return toReturn;
	}

	private static Set<String> classesReferenced(Body b){
		Set<String> toReturn = new HashSet<String>();
		for (Unit u : b.getUnits()) {
			InvokeExpr invokeExpr = null;
			if(u instanceof InvokeStmt){
				invokeExpr = ((InvokeStmt)u).getInvokeExpr();
			} else if(u instanceof JAssignStmt && ((JAssignStmt)u).containsInvokeExpr()){
				invokeExpr = ((JAssignStmt) u).getInvokeExpr();
			}

			if(invokeExpr != null){
				SootMethod sootMethod = invokeExpr.getMethod();
				toReturn.add(sootMethod.getDeclaringClass().getName());
			}

			Stmt s = (Stmt) u;
			if(s.containsFieldRef()) {
				FieldRef fr = s.getFieldRef();
				if(fr.getField() == null) {
					continue;
				}

				if(fr.getField().getDeclaringClass() == null) {
					continue;
				}

				toReturn.add(fr.getField().getDeclaringClass().getName());
			}
		}
		return toReturn;
	}

	private static boolean inlineIsEfficient(SootMethod inline, Stmt toInline, SootMethod container){
		SootClass inlineClass = inline.getDeclaringClass();
		SootClass containerClass = container.getDeclaringClass();

		//Obtain the sizes before inlining.
		long inlineClassSizeBefore = ClassFileUtils.getSize(inlineClass);
		long containerClassSizeBefore = ClassFileUtils.getSize(containerClass);

		//Inline and remove the inlined location.

		Body body = container.retrieveActiveBody();
		Body bodyClone = (Body)body.clone();//SerializationUtils.clone(body);
		assert(body.toString().equals(bodyClone.toString()));
		SiteInliner.inlineSite(inline, toInline, container);

		int inlineMethodIndex = inlineClass.getMethods().indexOf(inline);
		inlineClass.getMethods().remove(inline);

		//Obtain the sizes after inlining.
		long inlineClassSizeAfter = ClassFileUtils.getSize(inlineClass);
		long containerClassSizeAfter = ClassFileUtils.getSize(containerClass);

		//Revert the inlining and re-add the removed method.
		inlineClass.getMethods().add(inlineMethodIndex, inline);
		container.setActiveBody(bodyClone);
		assert(bodyClone.toString().equals(container.retrieveActiveBody().toString()));

		//Return true if inlining reduces size, otherwise return false.
		return (inlineClassSizeAfter + containerClassSizeAfter)
			-(inlineClassSizeBefore + containerClassSizeBefore) < 0;

	}

	private static boolean accessControlsOk(SootMethod sootMethod, SootClass sootClass){

		//No access control problems if the methods exist in the same class.
		if(sootMethod.getDeclaringClass().equals(sootClass)){
			return true;
		}

		boolean inSamePackage = sootMethod.getDeclaringClass().getPackageName().equals(sootClass.getPackageName());

		Body b = sootMethod.retrieveActiveBody();
		for(Unit unit : b.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt.containsInvokeExpr()) {
				InvokeExpr callExpr = stmt.getInvokeExpr();
				SootMethodRef smf = callExpr.getMethodRef();
				SootMethod invokedMethod = smf.tryResolve();
				if (invokedMethod == null) {
					//At this level we cannot always resolve virtual methods unfortunately.
					//I don't know as of yet how this may affect this check.
					continue;
				}

				if(!inSamePackage && SootUtils.isPackagePrivate(invokedMethod)){
					return false;
				} else if(invokedMethod.isPrivate()){
					return false;
				} else if(invokedMethod.isProtected()){
					return false;
				}
			} else if (stmt.containsFieldRef()) {
				FieldRef fieldRef = stmt.getFieldRef();
				SootField sootField = fieldRef.getField();
				if(!inSamePackage && SootUtils.isPackagePrivate(sootField)){
					return false;
				} else if(sootField.isPrivate()){
					return false;
				}
			}
		}

		return true;
	}
}