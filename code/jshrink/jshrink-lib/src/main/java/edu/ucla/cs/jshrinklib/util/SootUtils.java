package edu.ucla.cs.jshrinklib.util;

import java.io.*;
import java.util.*;

import edu.ucla.cs.jshrinklib.reachability.FieldData;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import soot.*;
import soot.jimple.JasminClass;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

public class SootUtils {

	public static String getJREJars() { 
		String defaultClasspath = Scene.v().defaultClassPath();
		String cp;
		if(defaultClasspath.contains(":")) {
			// The JRE in Mac returns multiple JRE jars including rt.jar and jce.jar
			cp = defaultClasspath;
		} else {
			// The JRE in Linux returns rt.jar only
			String jrePath = defaultClasspath.substring(0, defaultClasspath.lastIndexOf(File.separator));
			String jcePath = jrePath + File.separator + "jce.jar";
			// add rt.jar and jce.jar to the classpath, as required by Soot
			cp = defaultClasspath + File.pathSeparator + jcePath;
		}
		
		return cp;
	}

	private static String listToPathString(List<File> paths){
		StringBuilder sb = new StringBuilder();
		for(File path : paths){
			if(path.exists()) {
				sb.append(File.pathSeparator + path.getAbsolutePath());
			} else {
				System.err.println(path.getAbsolutePath() + " does not exist.");
			}
		}
		return sb.toString();
	}

	public static MethodData sootMethodToMethodData(SootMethod sootMethod){
		String methodName = sootMethod.getName();
		String methodClassName = sootMethod.getDeclaringClass().getName();
		String methodReturnType = sootMethod.getReturnType().toString();

		//For some reason, Soot sets Public to true in some cases where the method is protected.
		boolean isPublic = !sootMethod.isProtected() && sootMethod.isPublic();
		boolean isStatic = sootMethod.isStatic();

		String[] methodArgs = new String[sootMethod.getParameterCount()];
		for(int i=0; i<sootMethod.getParameterTypes().size(); i++){
			Type type = sootMethod.getParameterTypes().get(i);
			methodArgs[i] = type.toString();
		}

		return new MethodData(methodName,methodClassName,methodReturnType,methodArgs,isPublic, isStatic);
	}

	public static FieldData sootFieldToFieldData(SootField sootField){
		String fieldName = sootField.getName();
		String className = sootField.getDeclaringClass().getName();
		// Soot handles the generic type the same way as ASM due to type erasure in Java
		String fieldType = sootField.getType().toString();
		boolean isStatic = sootField.isStatic();

		return new FieldData(fieldName, className, isStatic, fieldType);
	}

	public static Map<SootMethod, Set<SootMethod>> convertMethodDataCallGraphToSootMethodCallGraph(
			Map<MethodData, Set<MethodData>> map){
		Map<SootMethod, Set<SootMethod>> toReturn = new HashMap<SootMethod, Set<SootMethod>>();
		for(Map.Entry<MethodData, Set<MethodData>> entry : map.entrySet()){
			SootClass keySootClass = Scene.v().loadClassAndSupport(entry.getKey().getClassName());

			/*
			For some reason, I occasionally got a "java.lang.RuntimeException: not declared <method> in class <class>"
			error when runnin the "ApplicationTest:junit_test()" JUnit Test. I therefore added these "declaresMethod"
			checks... doesn't get to the root of the problem but I had to move forward.
			TODO: Investigate this bug.
			 */
			if(keySootClass.declaresMethod(entry.getKey().getSubSignature())) {
				SootMethod keySootMethod = keySootClass.getMethod(entry.getKey().getSubSignature());
				Set<SootMethod> value = new HashSet<SootMethod>();
				for (MethodData methodData : entry.getValue()) {
					SootClass valueSootClass = Scene.v().getSootClass(methodData.getClassName());
					if (valueSootClass.declaresMethod(methodData.getSubSignature())) {
						SootMethod valueSootMethod = valueSootClass.getMethod(methodData.getSubSignature());
						value.add(valueSootMethod);
					}
				}
				toReturn.put(keySootMethod, value);
			}
		}
		return toReturn;
	}

	public static Map<MethodData, Set<MethodData>> convertSootMethodCallGraphToMethodDataCallGraph(
			Map<SootMethod, Set<SootMethod>> map){
		Map<MethodData, Set<MethodData>> toReturn = new HashMap<MethodData, Set<MethodData>>();
		for(Map.Entry<SootMethod, Set<SootMethod>> entry : map.entrySet()){
			Set<MethodData> value = new HashSet<MethodData>();
			for(SootMethod sootMethod : entry.getValue()){
				value.add(sootMethodToMethodData(sootMethod));
			}
			toReturn.put(sootMethodToMethodData(entry.getKey()),value);
		}
		return toReturn;
	}

	public static Map<SootMethod, Set<SootMethod>> mergeCallGraphMaps(
			Map<SootMethod, Set<SootMethod>> map1, Map<SootMethod, Set<SootMethod>> map2){
		Map<SootMethod, Set<SootMethod>> toReturn = new HashMap<SootMethod, Set<SootMethod>>(map1);
		for(Map.Entry<SootMethod, Set<SootMethod>> entry : map2.entrySet()){
			if(!toReturn.containsKey(entry.getKey())){
				toReturn.put(entry.getKey(), new HashSet<SootMethod>());
			}
			toReturn.get(entry.getKey()).addAll(entry.getValue());
		}
		return toReturn;
	}

	public static void setup_trimming(List<File> libJarPath, List<File> appClassPath, List<File> appTestPath){
		String cp = SootUtils.getJREJars();
		cp += listToPathString(libJarPath);
		cp += listToPathString(appClassPath);
		cp += listToPathString(appTestPath);
		Options.v().set_soot_classpath(cp);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		// try the following two options to ignore the static field error in the Jython lib 
		// the first one does not work but the second one works (why?)
		// check the following links for reference:
		// https://github.com/petablox-project/petablox/issues/6
		// https://github.com/Sable/soot/issues/410
		// https://github.com/Sable/soot/issues/717 
//		Options.v().setPhaseOption("jb.tr", "ignore-wrong-staticness:true");
		Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

		// set the application directories
		List<String> dirs = new ArrayList<String>();

		for(File path : appClassPath) {
			// double check whether the file path exists
			if(path.exists()) {
				dirs.add(path.getAbsolutePath());
			} else {
				System.err.println(path.getAbsolutePath() + " does not exist.");
			}
			
		}

		for(File path : appTestPath) {
			if(path.exists()) {
				dirs.add(path.getAbsolutePath());
			} else {
				System.err.println(path.getAbsolutePath() + " does not exist.");
			}
		}

		for(File path : libJarPath){
			dirs.add(path.getAbsolutePath());
		}

		Options.v().set_process_dir(dirs);
	}

	public static HashMap<String, String> getSparkOpt() {
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("enabled","true");
		opt.put("verbose","true");
		opt.put("ignore-types","false");          
		opt.put("force-gc","true");            
		opt.put("pre-jimplify","false");          
		opt.put("vta","false");                   
		opt.put("rta","false");                   
		opt.put("field-based","false");           
		opt.put("types-for-sites","false");        
		opt.put("merge-stringbuffer","true");   
		opt.put("string-constants","false");     
		opt.put("simulate-natives","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            
		opt.put("simplify-offline","false");    
		opt.put("simplify-sccs","true");        
		opt.put("ignore-types-for-sccs","false");
		opt.put("propagator","worklist");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid");
		opt.put("dump-html","false");           
		opt.put("dump-pag","false");             
		opt.put("dump-solution","false");        
		opt.put("topo-sort","false");           
		opt.put("dump-types","true");             
		opt.put("class-method-var","true");     
		opt.put("dump-answer","false");          
		opt.put("add-tags","false");             
		opt.put("set-mass","false"); 
		return opt;
	}

	public static void visitMethodClassCollapser(SootMethod m, CallGraph cg, Set<String> usedClass, Set<MethodData> usedMethods) {
		//a queue of Pair objects of SootMethods, the second object is the callsite of the first method
		Set<String> visited = new HashSet<String>();
		Stack<Pair<SootMethod, SootMethod>> methods_to_visit = new Stack<Pair<SootMethod, SootMethod>>();
		methods_to_visit.add(new Pair<SootMethod, SootMethod>(m, null));

		while(!methods_to_visit.isEmpty()) {
			Pair<SootMethod, SootMethod> first = methods_to_visit.pop();
			SootMethod firstMethod = first.getO1();
			MethodData firstMethodData = sootMethodToMethodData(firstMethod);
			SootMethod callSite = first.getO2();
			MethodData callSiteData = (callSite == null) ? null : sootMethodToMethodData(callSite);

			String className = firstMethodData.getClassName();
			if(!visited.contains(firstMethod.getSignature())) {
				// avoid recursion
				SootClass superClass = (callSite == null || !callSite.getDeclaringClass().hasSuperclass()) ? null : callSite.getDeclaringClass().getSuperclass();
//				if (superClass != null) {
//					superClasses.add(superClass.getName());
//				}
//				for (SootClass inter: firstMethod.getDeclaringClass().getInterfaces()) {
//					superClasses.add(inter.getName());
//				}
				if (callSiteData == null || !(callSiteData.getName().equals("<init>") && superClass != null && superClass.getName().equals(className))) {
					usedClass.add(className);
					usedMethods.add(firstMethodData);
				}
				visited.add(firstMethod.getSignature());

				// add callees to the stack
				Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(firstMethod));
				while (targets.hasNext()){
					SootMethod method = (SootMethod)targets.next();
					methods_to_visit.push(new Pair<SootMethod, SootMethod>(method, firstMethod));
				}
			}
		}
	}

	public static void visitMethodNonRecur(SootMethod parent, CallGraph cg, Set<String> usedClass, Map<MethodData,
			Set<MethodData>> visited, Set<String> appClasses, Set<String> libClasses){
		Queue<SootMethod> stack = new LinkedList<SootMethod>();
		stack.add(parent);

		Map<SootMethod, Set<SootMethod>> visitedSootMethod = new HashMap<SootMethod, Set<SootMethod>>();

		while(!stack.isEmpty()) {
			SootMethod par = stack.poll();
			//MethodData parentMethodData = sootMethodToMethodData(par);
			usedClass.add(par.getDeclaringClass().getName());

			Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(par));
			while (targets.hasNext()) {
				SootMethod method = (SootMethod) targets.next();
				//MethodData methodMethodData = sootMethodToMethodData(method);

				if (!visitedSootMethod.containsKey(method)) {
					visitedSootMethod.put(method, new HashSet<SootMethod>());
				} else if (visitedSootMethod.get(method).contains(par)) {
					continue;
				}

				visitedSootMethod.get(method).add(par);
				stack.add(method);

			}
		}

		for(Map.Entry<SootMethod, Set<SootMethod>> entry: visitedSootMethod.entrySet()){
			MethodData methodDataKey = sootMethodToMethodData(entry.getKey());
			if(!visited.containsKey(methodDataKey)){
				visited.put(sootMethodToMethodData(entry.getKey()), new HashSet<MethodData>());
			}

			for(SootMethod sootMethod: entry.getValue()){
				visited.get(methodDataKey).add(sootMethodToMethodData(sootMethod));
			}
		}

	}

	public static boolean modifiableSootClass(SootClass sootClass){
		/*
		This is a cheap trick. Soot doesn't support every single bytecode structure in Java bytecode, and will throw an
		error if trying to convert SootClass it does not understand to a JasminClass. Lambda expressions are a common
		example of this, though I have observed some more obscure cases. I therefore convert the SootClass to the
		JasminClass here. If there is an error thrown, I return false (i.e., it should not be modified, we can't change
		this class).
		 */
		try {
			for(SootMethod m: sootClass.getMethods()){
				if(m.isConcrete()) {
					m.retrieveActiveBody();
				}
			}

			StringWriter stringWriter = new StringWriter();
			PrintWriter writerOut = new PrintWriter(stringWriter);
			JasminClass jasminClass = new JasminClass(sootClass);

		}catch (Exception e){
			return false;
		}

		return true;
	}

	public static Optional<String> getUnmodifiableClassException(SootClass sootClass){
		try {
			for(SootMethod m: sootClass.getMethods()){
				if(m.isConcrete()) {
					m.retrieveActiveBody();
				}
			}

			StringWriter stringWriter = new StringWriter();
			PrintWriter writerOut = new PrintWriter(stringWriter);
			JasminClass jasminClass = new JasminClass(sootClass);

		}catch (Exception e){
			return Optional.of(e.getLocalizedMessage());
		}

		return Optional.empty();
	}

	public static boolean isPackagePrivate(ClassMember classMember){
		return !classMember.isPrivate() && !classMember.isProtected() && !classMember.isPublic();
	}

	public static boolean isPackagePrivate(SootClass sootClass){
		return !sootClass.isPrivate() && !sootClass.isProtected() && !sootClass.isPublic();
	}
}
