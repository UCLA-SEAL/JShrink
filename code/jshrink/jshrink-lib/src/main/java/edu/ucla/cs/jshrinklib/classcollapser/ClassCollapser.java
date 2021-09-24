package edu.ucla.cs.jshrinklib.classcollapser;

import edu.ucla.cs.jshrinklib.JShrink;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import fj.P;
import soot.*;
import soot.JastAddJ.Annotation;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.spark.ondemand.pautil.SootUtil;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.tagkit.*;

import java.util.*;
import java.util.regex.Pattern;

public class ClassCollapser {

    //This is a hacky way to get logging information. TODO: Fix this at some point
    public static StringBuilder log = new StringBuilder();

    private final Set<String> classesToRewrite;
    private final Set<String> classesToRemove;
    private final Set<MethodData> removedMethods;

    private HashMap<String, SootMethod> renamedMethods;

    public ClassCollapser() {
        this.classesToRemove = new HashSet<String>();
        this.classesToRewrite = new HashSet<String>();
        this.removedMethods = new HashSet<MethodData>();
        this.renamedMethods = new HashMap<String, SootMethod>();
    }

    public void run(ClassCollapserAnalysis classCollapserAnalysis, Set<String> testClasses) {
        Map<MethodData, Set<MethodData>> callGraph = classCollapserAnalysis.getCallGraph();

        HashMap<String, SootClass> nameToSootClass = new HashMap<String, SootClass>();

        for (ArrayList<String> collapse: classCollapserAnalysis.getCollapseList()) {
            String fromName = collapse.get(0);
            String toName = collapse.get(1);
            if (!nameToSootClass.containsKey(fromName)) {
                nameToSootClass.put(fromName, Scene.v().loadClassAndSupport(fromName));
            }
            if (!nameToSootClass.containsKey(toName)) {
                nameToSootClass.put(toName, Scene.v().loadClassAndSupport(toName));
            }
            SootClass from = nameToSootClass.get(fromName);
            SootClass to = nameToSootClass.get(toName);

            Set<MethodData> removedMethods =
                    ClassCollapser.mergeTwoClasses(from, to, callGraph);

            this.removedMethods.addAll(removedMethods);

            this.classesToRewrite.add(to.getName());
//            this.classesToRemove.add(from.getName());
//            callGraph.remove(fromName);
        }

        // add all classes to remove, including those merged subclasses and their unused siblings
        this.classesToRemove.addAll(classCollapserAnalysis.getRemoveList());
        Set<MethodData> methodsToRemove = new HashSet<MethodData>();
        for(String className : classCollapserAnalysis.getRemoveList()) {
            for(MethodData md : callGraph.keySet()) {
                if(md.getName().equals(className)) {
                    methodsToRemove.add(md);
                }
            }
        }

        for(MethodData md : methodsToRemove) {
            callGraph.remove(md);
        }

        Set<String> allClasses = new HashSet<String>();
        allClasses.addAll(classCollapserAnalysis.appClasses);
        allClasses.addAll(testClasses);

        // update any references to collapsed classes
        Map<String, String> nameChangeList = classCollapserAnalysis.getNameChangeList();

        for(String fromName: nameChangeList.keySet()) {
            String toName = nameChangeList.get(fromName);
            if (!nameToSootClass.containsKey(fromName)) {
                nameToSootClass.put(fromName, Scene.v().loadClassAndSupport(fromName));
            }
            if (!nameToSootClass.containsKey(toName)) {
                nameToSootClass.put(toName, Scene.v().loadClassAndSupport(toName));
            }
            SootClass from = nameToSootClass.get(fromName);
            SootClass to = nameToSootClass.get(toName);
            for (String className : allClasses) {
                if(className.equals(fromName)) {
                    // no need to handle the collapsed class, since this class will be removed at the end
                    continue;
                }

                if (!nameToSootClass.containsKey(className)) {
                    nameToSootClass.put(className, Scene.v().loadClassAndSupport(className));
                }
                SootClass sootClass = nameToSootClass.get(className);
                if (changeClassNamesInClass(sootClass, from, to)) {
                    classesToRewrite.add(sootClass.getName());
                }
            }
        }

        // update the renamed methods in the call graph
        List<MethodData> usedMethods = new ArrayList<MethodData>(callGraph.keySet());
        // must iterate the hashmap keyset this way since we need to update keys in the hasmap
        for(int i = 0; i < usedMethods.size(); i++) {
            MethodData md = usedMethods.get(i);
            String subSignature = md.getClassName() + " : " + md.getSubSignature();
            Set<MethodData> callers = callGraph.get(md);
            if(renamedMethods.containsKey(subSignature)) {
                SootMethod sootMethod = renamedMethods.get(subSignature);
                MethodData renamedMethodData = SootUtils.sootMethodToMethodData(sootMethod);
                callGraph.remove(md);
                callGraph.put(renamedMethodData, callers);
            }

            for(MethodData caller : callers) {
                String callerSignature = caller.getClassName() + " : " + caller.getSubSignature();
                if(renamedMethods.containsKey(callerSignature)) {
                    SootMethod sootMethod = renamedMethods.get(callerSignature);
                    String retType = sootMethod.getReturnType().toString();
                    caller.setReturnType(retType);

                    List<Type> paramTypes = sootMethod.getParameterTypes();
                    String[] types = new String[paramTypes.size()];
                    for(int j = 0; j < types.length; j++) {
                        types[j] = paramTypes.get(j).toString();
                    }
                    caller.setArgs(types);

                    caller.setName(sootMethod.getName());
                }
            }
        }

        // update method call targets whose return types, parameter types, or even names have been changed
        for(String className : allClasses) {
            if(nameChangeList.containsKey(className)) {
                // no need to update any merged classes since they will be deleted anyway
                continue;
            }

            if(!nameToSootClass.containsKey(className)) {
                nameToSootClass.put(className, Scene.v().loadClassAndSupport(className));
            }
            SootClass sootClass = nameToSootClass.get(className);
            ArrayList<SootMethod> sootMethods = new ArrayList<SootMethod>(sootClass.getMethods());
            for(int i = 0; i < sootMethods.size(); i++) {
                SootMethod m = sootMethods.get(i);
                if(m.isNative() || m.isAbstract()) continue;

                Body b = m.retrieveActiveBody();
                for(Unit unit : b.getUnits()) {
                    Stmt stmt = (Stmt) unit;
                    if(stmt.containsInvokeExpr()) {
                        InvokeExpr callExpr = stmt.getInvokeExpr();
                        SootMethodRef smf = callExpr.getMethodRef();
                        boolean updated = false;
                        String signature = smf.getDeclaringClass().getName() + " : "
                                    + SootMethod.getSubSignature(smf.getName(), smf.getParameterTypes(), smf.getReturnType());
                        if(renamedMethods.containsKey(signature)) {
                            SootMethod renamed_method = renamedMethods.get(signature);
                            if (renamed_method.getName().equals("<init>")) {
                                // update the callsite by adding a dummy argument
                                if(stmt instanceof InvokeStmt && callExpr instanceof SpecialInvokeExpr) {
                                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) callExpr;
                                    InvokeStmt invokeStmt = (InvokeStmt) stmt;
                                    invokeStmt.setInvokeExpr(callExpr);
                                    Local base = (Local) specialInvokeExpr.getBase();
                                    List<Type> paramTypes = new ArrayList<Type>(smf.getParameterTypes());
                                    int numOfDummyVariales = renamed_method.getParameterTypes().size() - smf.getParameterTypes().size();
                                    for(int j = 0; j < numOfDummyVariales; j++) {
                                        paramTypes.add(Scene.v().getType("int"));
                                    }
                                    SootMethodRef new_smf = Scene.v().makeMethodRef(smf.getDeclaringClass(),
                                            smf.getName(),
                                            paramTypes,
                                            smf.getReturnType(),
                                            smf.isStatic());
                                    List<Value> args = callExpr.getArgs();
                                    for(int j = 0; j < numOfDummyVariales; j++) {
                                        args.add(IntConstant.v(1));
                                    }
                                    SpecialInvokeExpr new_specialInvokeExpr = Jimple.v().newSpecialInvokeExpr(base, new_smf, args);
                                    invokeStmt.setInvokeExpr(new_specialInvokeExpr);
                                    callExpr = new_specialInvokeExpr;
                                } else {
                                    // constructor call should always be in InvokeStatement
                                    throw new RuntimeException("Unsupported method invocation type: " + callExpr);
                                }
                            } else {
                                // update the method target name in the callsite
                                SootMethodRef new_smf = Scene.v().makeMethodRef(renamed_method.getDeclaringClass(),
                                        renamed_method.getName(),
                                        renamed_method.getParameterTypes(),
                                        renamed_method.getReturnType(),
                                        renamed_method.isStatic());
                                callExpr.setMethodRef(new_smf);
                            }

                            updated = true;
                        }

                        // check each merged class
                        for(String fromClassName : nameChangeList.keySet()) {
                            String toClassName = nameChangeList.get(fromClassName);

                            // get the smf again in case it is updated in the previous step
                            smf = callExpr.getMethodRef();
                            if(smf.getReturnType() == Scene.v().getType(fromClassName)) {
                                SootMethodRef new_smf = Scene.v().makeMethodRef(smf.getDeclaringClass(),
                                        smf.getName(),
                                        smf.getParameterTypes(),
                                        Scene.v().getType(toClassName),
                                        smf.isStatic());
                                callExpr.setMethodRef(new_smf);
                                updated = true;
                                smf = new_smf;
                            }

                            boolean paramUpdated = false;
                            List<Type> paramTypes = new ArrayList<Type>(smf.getParameterTypes());
                            for(int j = 0; j < paramTypes.size(); j++) {
                                Type paramT = paramTypes.get(j);
                                if(paramT == Scene.v().getType(fromClassName)) {
                                    paramTypes.remove(j);
                                    paramTypes.add(j, Scene.v().getType(toClassName));
                                    paramUpdated = true;
                                }
                            }

                            if(paramUpdated) {
                                SootMethodRef new_smf = Scene.v().makeMethodRef(smf.getDeclaringClass(),
                                        smf.getName(),
                                        paramTypes,
                                        smf.getReturnType(),
                                        smf.isStatic());
                                callExpr.setMethodRef(new_smf);
                                updated = true;
                            }
                        }

                        // double check whether this method call target is updated correctly
                        if(updated) {
                            try {
                                SootMethod sootMethod = callExpr.getMethod();
                                classesToRewrite.add(className);
                            } catch (SootMethodRefImpl.ClassResolutionFailedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        HashSet<String> innerClassesToUpdateTogether = new HashSet<String>();
        for(String className : classesToRewrite) {
            // Make sure when we update an outer class, we also rewrite the bytecode of all its inner classes using Soot
            // Otherwise we will introduce bytecode inconsistencies
            // See GitHub issue#77 https://github.com/tianyi-zhang/call-graph-analysis/issues/77
            for(String className2 : testClasses) {
                if (className2.startsWith(className + "$")) {
                    if(!classesToRemove.contains(className2) && !classesToRewrite.contains(className2)) {
                        log.append("This inner class, " + className2 + " should also be updated by Soot together with its outerclass.\n");
                        innerClassesToUpdateTogether.add(className2);
                    }
                }
            }
        }
        classesToRewrite.addAll(innerClassesToUpdateTogether);
    }

    /**
     * Merges one soot class into another
     * @param from The class that will be merged from, and discarded (the super class)
     * @param to The class that will be merged into, and kept (the sub class)
     * @return The set of methods that have been removed
     */
    /*package*/ static Set<MethodData> mergeTwoClasses(SootClass from, SootClass to, Map<MethodData, Set<MethodData>> callGraph) {
        Set<MethodData> toReturn = new HashSet<MethodData>();
        HashMap<String, SootField> originalFields = new HashMap<String, SootField>();
        for (SootField field : to.getFields()) {
            originalFields.put(field.getName(), field);
        }
        // reset modifiers
        to.setModifiers(from.getModifiers());

        // find fields that are used in super class constructors
        HashSet<String> fieldsUsedInConstructor = new HashSet<String>();
        for(SootMethod method : to.getMethods()) {
            if(method.getName().equals("<init>")) {
                Body b = method.retrieveActiveBody();
                for(Unit u : b.getUnits()) {
                    Stmt s = (Stmt) u;
                    if(!s.containsFieldRef()) {
                        continue;
                    }
                    FieldRef fr = s.getFieldRef();
                    fieldsUsedInConstructor.add(fr.getField().getName());
                }
            }
        }

        HashSet<String> renamedFields = new HashSet<String>();
        for (SootField field : from.getFields()) {
            String fieldName = field.getName();
            if (originalFields.containsKey(fieldName)) {
                // overridden field
                if(fieldsUsedInConstructor.contains(fieldName)) {
                    // must keep this field and rename
                    renamedFields.add(fieldName);
                    originalFields.get(fieldName).setName("super" + fieldName);
                } else {
                    // safely remove
                    to.getFields().remove(originalFields.get(fieldName));
                }
            }
            // reset the declaring class
            field.setDeclaringClass(to);
            to.getFields().addLast(field);
        }

        // update references to all renamed fields if any
        for(SootMethod method : to.getMethods()) {
            if(method.isAbstract() || method.isNative()) continue;
            Body b = method.retrieveActiveBody();
            for(Unit u : b.getUnits()) {
                Stmt s = (Stmt) u;
                if(!s.containsFieldRef()) {
                    continue;
                }

                FieldRef fr = s.getFieldRef();
                SootFieldRef sfr = fr.getFieldRef();
                if(renamedFields.contains(sfr.name())) {
                    // the original field has been renamed
                    AbstractSootFieldRef new_sfr = new AbstractSootFieldRef(sfr.declaringClass(),
                            "super" + sfr.name(), sfr.type(), sfr.isStatic());
                    fr.setFieldRef(new_sfr);
                }
            }
        }

        HashMap<String, SootMethod> originalMethods = new HashMap<String, SootMethod>();
        for (SootMethod method : to.getMethods()) {
            originalMethods.put(method.getSubSignature(), method);
        }
        HashSet<SootMethod> methodsToMove = new HashSet<SootMethod>();
        HashSet<SootMethod> methodsToRemoveInSuperClass = new HashSet<SootMethod>();
        List<SootMethod> fromMethods = from.getMethods();
        for (int i = 0; i < fromMethods.size(); i++) {
            SootMethod method = fromMethods.get(i);
            MethodData md = SootUtils.sootMethodToMethodData(method);

            if(!callGraph.containsKey(md) && !(method.isAbstract() || method.isNative())) {
                // this method is not used so no need to move it
                // this check is only true when method removal is not enabled
                // but we have to keep interface methods, abstract methods, and native methods
                // since they are never removed
                continue;
            }

            // find the super constructor calls in a constructor of a subclass
            Stmt toInLine = null;
            SootMethod inlinee = null;
            if (method.getName().equals("<init>")) {
                Body b = method.retrieveActiveBody();
                for (Unit u : b.getUnits()) {
                    if (u instanceof InvokeStmt) {
                        InvokeExpr expr = ((InvokeStmt)u).getInvokeExpr();
                        SootMethod m = expr.getMethod();
                        if (m.getName().equals(method.getName()) && m.getDeclaringClass().getName().equals(to.getName())) {
                            toInLine = (InvokeStmt) u;
                            inlinee = m;
                        }
                    }
                }
            } else if(method.getName().equals("<clinit>")){
				Body b = method.retrieveActiveBody();
				for (Unit u : b.getUnits()) {
					if (u instanceof JAssignStmt) {
						JAssignStmt stmt = (JAssignStmt) u;
						if (stmt.containsFieldRef()) {
							FieldRef fr = stmt.getFieldRef();
							SootFieldRef oldFieldRef = fr.getFieldRef();
							AbstractSootFieldRef newFieldRef =
								new AbstractSootFieldRef(to, oldFieldRef.name(),
									oldFieldRef.type(), oldFieldRef.isStatic());
							fr.setFieldRef(newFieldRef);
						}
					}
				}
			}
            if (inlinee != null && toInLine != null) {
                Body b = inlinee.retrieveActiveBody();
                // inline the constructor
                SiteInliner.inlineSite(inlinee, toInLine, method);
                if (originalMethods.containsKey(method.getSubSignature())) {
                    methodsToRemoveInSuperClass.add(originalMethods.get(method.getSubSignature()));
                }
                // add this method to the methodsToMove list
                methodsToMove.add(method);
            } else {
//                if (usedMethods.containsKey(from.getName()) && usedMethods.get(from.getName()).contains(method.getSubSignature())) {
                    if (!originalMethods.containsKey(method.getSubSignature())) {
                        // add this method to the methodsToMove list
                        methodsToMove.add(method);
                    } else {
                        // add this method to the methodsToMove list
                        methodsToRemoveInSuperClass.add(originalMethods.get(method.getSubSignature()));
                        methodsToMove.add(method);
                    }
//                }
            }

            if(JShrink.enable_super_class_recursion_check) {
                if(method.isNative() || method.isAbstract()) {
                    continue;
                }
                // check if the method calls an overridden method in the super class
                Body b = method.retrieveActiveBody();
                for(Unit u : b.getUnits()) {
                    if (u instanceof Stmt) {
                        Stmt stmt = (Stmt) u;
                        if(stmt.containsInvokeExpr()) {
                            InvokeExpr invokeExpr = stmt.getInvokeExpr();
                            SootMethodRef smf = invokeExpr.getMethodRef();
                            if(smf.getDeclaringClass().getName().equals(to.getName()) && smf.getName().equals(method.getName()) && smf.getParameterTypes().equals(method.getParameterTypes())) {
                                // replace the call target from the super class to the super class of the super class
                                // to avoid recursion
                                invokeExpr.setMethodRef(Scene.v().makeMethodRef(to.getSuperclass(), smf.getName(),
                                        smf.getParameterTypes(),
                                        smf.getReturnType(),
                                        smf.isStatic()));
                            }
                        }
                    }
                }
            }
        }

        for(SootMethod method : methodsToRemoveInSuperClass) {
            // conflicted but unused methods (very likely to be virtually invoked) in a super class will be removed and
            // replaced with a concrete class
            MethodData md = SootUtils.sootMethodToMethodData(method);
            toReturn.add(md);
            to.removeMethod(method);

            // update the call graph
            callGraph.remove(md);
        }

        // move methods from the subclass to the superclass
        for(SootMethod m : methodsToMove) {
            MethodData orig_md = SootUtils.sootMethodToMethodData(m);
            from.removeMethod(m);
            to.addMethod(m);

            if(callGraph.containsKey(orig_md)) {
                Set<MethodData> callers = callGraph.get(orig_md);
                MethodData md = SootUtils.sootMethodToMethodData(m);
                // update the references to this method if it is the caller of other methods
                for(MethodData m2 : callGraph.keySet()) {
                    Set<MethodData> callers2 = callGraph.get(m2);
                    for(MethodData caller : callers2) {
                        if(caller.equals(orig_md)) {
                            caller.setClassName(to.getName());
                        }
                    }
                    callGraph.put(m2, callers2);
                }
                callGraph.put(md, callers);
                callGraph.remove(orig_md);
            }
        }

        //The final modifier can be problematic, best just to remove them to be save
        if(to.isFinal()){
            to.setModifiers(to.getModifiers() - Modifier.FINAL);
        }

        for(SootMethod sootMethod : to.getMethods()){
            if(sootMethod.isFinal()) {
                sootMethod.setModifiers(sootMethod.getModifiers() - Modifier.FINAL);
            }
        }

        for(SootField sootField : to.getFields()){
            if(sootField.isFinal()){
                sootField.setModifiers(sootField.getModifiers() - Modifier.FINAL);
            }
        }

        return toReturn;
    }

    /**
     * Changes class names in the body of all methods of a class (Legacy soot approach)
     * @param c The class in which we are modifying the bodies
     * @param changeFrom The original name of the class to be changed
     * @param changeTo The new name of the class to be changed
    **/
    boolean changeClassNamesInClass(SootClass c, SootClass changeFrom, SootClass changeTo) {
        assert c != changeFrom;

        boolean changed = false;
        if (c.hasSuperclass() && c.getSuperclass().getName().equals(changeFrom.getName())) {
            log.append("CHANGE_SUPER_CLASS_IN_CLASS_COLLAPSER," + c.getName() + " super class changed from "
                + c.getSuperclass().getName() + " to " + changeTo + System.lineSeparator());
            c.setSuperclass(changeTo);
            changed = true;
        }
        if (c.getInterfaces().contains(changeFrom)) {
            log.append("REMOVED_INTERFACE_IN_CLASS_COLLAPSER," + changeFrom + " in " + c.getName()
                + System.lineSeparator());
            c.removeInterface(changeFrom);
            if(!c.getInterfaces().contains(changeTo)) {
                log.append("ADDED_INTERFACE_IN_CLASS_COLLAPSER," + changeTo + " in " + c.getName()
                    + System.lineSeparator());
                c.addInterface(changeTo);
            }
            changed = true;
        }
        for (SootField f: c.getFields()) {
            if (f.getType() == Scene.v().getType(changeFrom.getName())) {
                log.append("CHANGED_TYPE_IN_CLASS_COLLAPSER," + f.getType() + "type for variable " + f.getName()
                    + "in class " + c.getName() + " to " + Scene.v().getType(changeTo.getName()) + System.lineSeparator());
                f.setType(Scene.v().getType(changeTo.getName()));
                changed = true;
            }
        }

        if(JShrink.enable_annotation_updates) {
            // handle class annotations
            List<Tag> tags  = c.getTags();
            for(int i = 0; i < tags.size(); i++) {
                Tag tag = tags.get(i);
                if(tag instanceof VisibilityAnnotationTag) {
                    ArrayList<AnnotationTag> annotations = ((VisibilityAnnotationTag) tag).getAnnotations();
                    for(AnnotationTag annotation : annotations) {
                        Collection<AnnotationElem> values = annotation.getElems();
                        List<AnnotationElem> newValues = new ArrayList<AnnotationElem>();
                        for(AnnotationElem annotationElem: values) {
                            if(annotationElem instanceof AnnotationClassElem) {
                                String desc = ((AnnotationClassElem) annotationElem).getDesc();
                                if(desc.startsWith("L") && desc.endsWith(";")) {
                                    String typeName = desc.substring(1, desc.length() - 1);
                                    typeName = typeName.replaceAll(Pattern.quote("/"), ".");
                                    if(typeName.equals(changeFrom.getName())) {
                                        AnnotationClassElem classElem = new AnnotationClassElem(
                                                "L" + changeTo.getName().replaceAll(Pattern.quote("."), "/") + ";",
                                                annotationElem.getKind(), annotationElem.getName());
                                        newValues.add(classElem);
                                        changed = true;
                                        continue;
                                    }
                                }
                                newValues.add(annotationElem);
                            } else if (annotationElem instanceof AnnotationArrayElem) {
                                AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) annotationElem;
                                ArrayList<AnnotationElem> newValues2 = new ArrayList<AnnotationElem>();
                                for(AnnotationElem annotationElem2 : annotationArrayElem.getValues()) {
                                    if(annotationElem2 instanceof AnnotationClassElem) {
                                        String desc2 = ((AnnotationClassElem) annotationElem2).getDesc();
                                        if(desc2.startsWith("L") && desc2.endsWith(";")) {
                                            String typeName2 = desc2.substring(1, desc2.length() - 1);
                                            typeName2 = typeName2.replaceAll(Pattern.quote("/"), ".");
                                            if(typeName2.equals(changeFrom.getName())) {
                                                AnnotationClassElem classElem2 = new AnnotationClassElem(
                                                        "L" + changeTo.getName().replaceAll(Pattern.quote("."), "/") + ";",
                                                        annotationElem2.getKind(), annotationElem2.getName());
                                                newValues2.add(classElem2);
                                                changed = true;
                                                continue;
                                            }
                                        }
                                    }
                                    newValues2.add(annotationElem2);
                                }
                                AnnotationArrayElem newArrayElem = new AnnotationArrayElem(newValues2, annotationArrayElem.getKind(), annotationArrayElem.getName());
                                newValues.add(newArrayElem);
                            } else {
                                newValues.add(annotationElem);
                            }
                        }
                        annotation.setElems(newValues);
                        for(AnnotationElem annotationElem : newValues){
                            log.append("ADDED_ANNOTATION_ELEMENT_IN_CLASS_COLLAPSER," + annotationElem.toString()
                                    + " from class " + c.getName() + System.lineSeparator());
                        }
                    }
                }
            }
        }

        List<SootMethod> sootMethods = c.getMethods();
        // In the Gecco project, the order of the methods is changed in the middle of the loop. It is very strange. adw
        List<SootMethod> sootMethodsCopy = new ArrayList<SootMethod>(sootMethods);
        for (int i = 0; i < sootMethodsCopy.size(); i++) {
            SootMethod m = sootMethodsCopy.get(i);
            // I saw a case in android.jar where one method in a class has the return type of changeFrom and the other method
            // in the same class has the return type of changeTo. In Java, two methods in the same class can never have return
            // types with inheritance relationship. But Android allows this on purpose to generate stub methods which will be
            // interpreted by Android emulator. So resetting the return of the first method from changeFrom to changeTo will
            // cause a naming conflict. Nevertheless, keep one of these methods to avoid errors.
            String signature = m.getSubSignature();
            boolean name_conflict = false;
            String original_method = null;
            if(signature.contains(changeFrom.getName())) {
                // the signature of this method is very likely to be updated

                // generate the new method signature after update type references in the signature
                Type retType = m.getReturnType();
                if(retType == changeFrom.getType()) {
                    retType = changeTo.getType();
                }
                List<Type> paramTypes = new ArrayList<Type>(m.getParameterTypes());
                for(int j = 0; j < paramTypes.size(); j++) {
                    Type paramT = paramTypes.get(j);
                    if(paramT == changeFrom.getType()) {
                        paramTypes.remove(j);
                        paramTypes.add(j, changeTo.getType());
                    }
                }
                String methodName = m.getName();
                String renamed_signature = SootMethod.getSubSignature(methodName, paramTypes, retType);

                // double check if this method signature is indeed updated
                if(!renamed_signature.equals(signature)) {

                    if(hasNameConflict(sootMethodsCopy, renamed_signature)) {
                        // there is a name conflict
                        name_conflict = true;

                        // record the original method signature before renaming the method
                        original_method = m.getSubSignature();

                        do {
                            // rename it
                            if(!methodName.equals("<init>")) {
                                methodName += "_sub";
                            } else {
                                // cannot rename a class constructor
                                // add a dummy parameter instead
                                Type intT = Scene.v().getType("int");
                                paramTypes.add(intT);

                                // add a new local variable for the newly
                                Body b = m.retrieveActiveBody();
                                // initialize the newly added parameter
                                Local arg = Jimple.v().newLocal("i" + (paramTypes.size()-1), intT);
                                b.getLocals().addLast(arg);
                                Unit paramIdentifyStatement = Jimple.v().newIdentityStmt(arg, Jimple.v().newParameterRef(intT, paramTypes.size()-1));
                                // We cannot insert at the end since the last statement is often a return statement
                                // find the last identify statement
                                Unit lastIdentityStmt = null;
                                for(Unit unit : b.getUnits()) {
                                    if(unit instanceof IdentityStmt) {
                                        lastIdentityStmt = unit;
                                    } else {
                                        break;
                                    }
                                }
                                if(lastIdentityStmt == null) {
                                    // no local variable?
                                    Unit first = b.getUnits().getFirst();
                                    b.getUnits().insertBefore(paramIdentifyStatement, first);
                                } else {
                                    b.getUnits().insertAfter(paramIdentifyStatement, lastIdentityStmt);
                                }
                            }

                            renamed_signature = SootMethod.getSubSignature(methodName, paramTypes, retType);
                        } while (hasNameConflict(sootMethodsCopy, renamed_signature));


                        // remove the original method before updating its signature
                        c.removeMethod(m);

                        m.setName(methodName);
                        m.setParameterTypes(paramTypes);
                        m.setReturnType(retType);

                        // add it back to update the internal method signature map in the Soot class
                        c.addMethod(m);

                        changed = true;
                    }
                }
            }

            boolean changed2 = changeClassNamesInMethod(m, changeFrom, changeTo);

            if(name_conflict) {
                assert original_method != null;
                renamedMethods.put(c.getName() + " : " + original_method, m);
            }
            // do not inline change2 since Java do short circuit evaluation
            // we still want to make sure type references in each method body is updated correctly
            changed = changed || changed2;
        }

        return changed;
    }

    private boolean hasNameConflict(List<SootMethod> methods, String renamed_signature) {
        boolean hasConflict = false;

        for(int j = 0; j < methods.size(); j++) {
            SootMethod m2 = methods.get(j);
            String signature = m2.getSubSignature();
            if(signature.equals(renamed_signature)) {
                // this is not allowed in Java but saw this case in android.jar
                // remove m from class and continue
                hasConflict = true;
                break;
            }
        }

        return hasConflict;
    }

    //Supporting method for changeClassNameInClass
    private static boolean changeClassNamesInMethod(SootMethod m, SootClass changeFrom, SootClass changeTo) {
        boolean changed = false;
        if (m.getReturnType() == Scene.v().getType(changeFrom.getName())) {
            // the following method call changes the order of methods in methodList
            m.setReturnType(Scene.v().getType(changeTo.getName()));
            changed = true;
        }
        List<Type> types = m.getParameterTypes();
        ArrayList<Type> newTypes = new ArrayList<Type>();
        boolean changeTypes = false;
        for (int i = 0; i < m.getParameterCount(); ++i) {
            if (types.get(i) ==  Scene.v().getType(changeFrom.getName())) {
                newTypes.add(Scene.v().getType(changeTo.getName()));
                changeTypes = true;
            } else {
                newTypes.add(types.get(i));
            }
        }
        if (changeTypes) {
            // the following method call also changes the order of methods in methodList
            m.setParameterTypes(newTypes);
            changed = true;
        }

        boolean changeExceptions = false;
        ArrayList<SootClass> newExceptions = new ArrayList<SootClass>();
        for (SootClass e: m.getExceptions()) {
            if (e.getName().equals(changeFrom.getName())) {
                newExceptions.add(changeTo);
                changeExceptions = true;
            } else {
                newExceptions.add(e);
            }
        }
        if (changeExceptions) {
            // same here, the following method call also changes the order of methods in methodList
            m.setExceptions(newExceptions);
            changed = true;
        }

        if (!m.isAbstract() && !m.isNative()) {
            Body b = m.retrieveActiveBody();
            for (Local l : b.getLocals()) {
                if (l.getType() == Scene.v().getType(changeFrom.getName())) {
                    l.setType(Scene.v().getType(changeTo.getName()));
                    changed = true;
                }
            }
            for (Unit u : m.retrieveActiveBody().getUnits()) {
                if (u instanceof InvokeStmt) {
                    InvokeExpr expr = ((InvokeStmt) u).getInvokeExpr();
                    SootMethodRef originalMethodRef = expr.getMethodRef();
                    // check whether the method target is declared in the changeFrom class
                    if (originalMethodRef.getDeclaringClass().getName().equals(changeFrom.getName())) {
                        expr.setMethodRef(Scene.v().makeMethodRef(changeTo, originalMethodRef.getName(),
                                originalMethodRef.getParameterTypes(),
                                originalMethodRef.getReturnType(),
                                originalMethodRef.isStatic()));
                        ((InvokeStmt) u).setInvokeExpr(expr);
                        changed = true;
                    }
                } else if (u instanceof DefinitionStmt) {
                    Value rightOp = ((DefinitionStmt) u).getRightOp();

                    if (rightOp instanceof NewExpr && rightOp.getType() == Scene.v().getType(changeFrom.getName())) {
                        ((NewExpr) rightOp).setBaseType((RefType) Scene.v().getType(changeTo.getName()));
                        changed = true;
                    } else if (rightOp instanceof JCastExpr) {
                        JCastExpr expr = (JCastExpr) rightOp;
                        if (expr.getType() == Scene.v().getType(changeFrom.getName())) {
                            expr.setCastType(Scene.v().getType(changeTo.getName()));
                            changed = true;
                        }
                    } else if (rightOp instanceof InvokeExpr) {
                        InvokeExpr expr = (InvokeExpr) rightOp;
                        SootMethodRef originalMethodRef = expr.getMethodRef();
                        if (originalMethodRef.getDeclaringClass().getName().equals(changeFrom.getName())) {
                            expr.setMethodRef(Scene.v().makeMethodRef(changeTo, originalMethodRef.getName(),
                                    originalMethodRef.getParameterTypes(),
                                    originalMethodRef.getReturnType(),
                                    originalMethodRef.isStatic()));
                            changed = true;
                        }
                        //if return type of method is of type which has been merged
                        /*else if((originalMethodRef.getReturnType() instanceof RefType) &&
                                ((RefType) originalMethodRef.getReturnType()).getClassName().equals(changeFrom.getName()))
                        {
                            expr.setMethodRef(Scene.v().makeMethodRef(originalMethodRef.getDeclaringClass(), originalMethodRef.getName(),
                                    originalMethodRef.getParameterTypes(),
                                    changeTo.getType(),
                                    originalMethodRef.isStatic()));
                            changed = true;
                        }*/
                    } else if (rightOp instanceof JInstanceOfExpr) {
                        JInstanceOfExpr expr = (JInstanceOfExpr) rightOp;
                        if(expr.getCheckType() == Scene.v().getType(changeFrom.getName())) {
                            expr.setCheckType(Scene.v().getType(changeTo.getName()));
                            changed = true;
                        }
                    }

                    // handle field references
                    if(u instanceof JIdentityStmt) {
                        JIdentityStmt stmt = (JIdentityStmt) u;
                        if(rightOp instanceof ParameterRef && rightOp.getType() == Scene.v().getType(changeFrom.getName())) {
                            ParameterRef oldRef = (ParameterRef) rightOp;
                            ParameterRef newRef = new ParameterRef(Scene.v().getType(changeTo.getName()), oldRef.getIndex());
                            stmt.setRightOp(newRef);
                            changed = true;
                            continue;
                        } else if (rightOp instanceof ThisRef && rightOp.getType() == Scene.v().getType(changeFrom.getName())) {
                            ThisRef newRef = new ThisRef(RefType.v(changeTo.getName()));
                            stmt.setRightOp(newRef);
                            changed = true;
                            continue;
                        }
                    }  else if (u instanceof JAssignStmt) {
                        JAssignStmt stmt = (JAssignStmt) u;

                        if (stmt.containsFieldRef()) {
                            FieldRef fr = stmt.getFieldRef();
                            if (fr instanceof InstanceFieldRef || fr instanceof StaticFieldRef) {
                                if (fr.getType().toString().equals(changeFrom.getName())) {
                                    // the referenced field is in the type of a removed class/interface
                                    SootFieldRef oldFieldRef = fr.getFieldRef();
                                    AbstractSootFieldRef newFieldRef =
                                            new AbstractSootFieldRef(oldFieldRef.declaringClass(), oldFieldRef.name(),
                                                    Scene.v().getType(changeTo.getName()), oldFieldRef.isStatic());
                                    fr.setFieldRef(newFieldRef);
                                    changed = true;
                                    continue;
                                } else if (fr.getFieldRef().declaringClass().getName().equals(changeFrom.getName())) {
                                    // use a field from the collapsed class
                                    // reset the declaring class of this field reference to the collapse-to class
                                    SootFieldRef oldFieldRef = fr.getFieldRef();
                                    AbstractSootFieldRef newFieldRef =
                                            new AbstractSootFieldRef(changeTo, oldFieldRef.name(),
                                                    oldFieldRef.type(), oldFieldRef.isStatic());
                                    fr.setFieldRef(newFieldRef);
                                    changed = true;
                                    continue;
                                }
                            }
                        }
                        else{
                            for(ValueBox v: u.getUseAndDefBoxes()){
                                if(v.getValue() instanceof ClassConstant && (ClassConstant.fromType(changeFrom.getType()).equals(v.getValue())))
                                {
                                    v.setValue(ClassConstant.fromType(changeTo.getType()));
                                    changed = true;
                                }
                                else if(v.getValue() instanceof RefType && v.getValue().getType().equals(changeFrom.getType())){
                                    v.setValue((Value) changeTo.getType());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
            for (Trap t: m.retrieveActiveBody().getTraps()){
                if(t.getException() != null && t.getException().getName().equals(changeFrom.getName())){
                    t.setException(changeTo);
                }
            }

            if(JShrink.enable_annotation_updates) {
                // handle method annotations
                List<Tag> tags = m.getTags();
                for (int i = 0; i < tags.size(); i++) {
                    Tag tag = tags.get(i);
                    if (tag instanceof VisibilityAnnotationTag) {
                        ArrayList<AnnotationTag> annotations = ((VisibilityAnnotationTag) tag).getAnnotations();
                        for (AnnotationTag annotation : annotations) {
                            Collection<AnnotationElem> values = annotation.getElems();
                            List<AnnotationElem> newValues = new ArrayList<AnnotationElem>();
                            for (AnnotationElem annotationElem : values) {
                                if (annotationElem instanceof AnnotationClassElem) {
                                    String desc = ((AnnotationClassElem) annotationElem).getDesc();
                                    if (desc.startsWith("L") && desc.endsWith(";")) {
                                        String typeName = desc.substring(1, desc.length() - 1);
                                        typeName = typeName.replaceAll(Pattern.quote("/"), ".");
                                        if (typeName.equals(changeFrom.getName())) {
                                            AnnotationClassElem classElem = new AnnotationClassElem(
                                                    "L" + changeTo.getName().replaceAll(Pattern.quote("."), "/") + ";",
                                                    annotationElem.getKind(), annotationElem.getName());
                                            newValues.add(classElem);
                                            changed = true;
                                            continue;
                                        }
                                    }
                                    newValues.add(annotationElem);
                                } else if (annotationElem instanceof AnnotationArrayElem) {
                                    AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) annotationElem;
                                    ArrayList<AnnotationElem> newValues2 = new ArrayList<AnnotationElem>();
                                    for (AnnotationElem annotationElem2 : annotationArrayElem.getValues()) {
                                        if (annotationElem2 instanceof AnnotationClassElem) {
                                            String desc2 = ((AnnotationClassElem) annotationElem2).getDesc();
                                            if (desc2.startsWith("L") && desc2.endsWith(";")) {
                                                String typeName2 = desc2.substring(1, desc2.length() - 1);
                                                typeName2 = typeName2.replaceAll(Pattern.quote("/"), ".");
                                                if (typeName2.equals(changeFrom.getName())) {
                                                    AnnotationClassElem classElem2 = new AnnotationClassElem(
                                                            "L" + changeTo.getName().replaceAll(Pattern.quote("."), "/") + ";",
                                                            annotationElem2.getKind(), annotationElem2.getName());
                                                    newValues2.add(classElem2);
                                                    changed = true;
                                                    continue;
                                                }
                                            }
                                        }
                                        newValues2.add(annotationElem2);
                                    }
                                    AnnotationArrayElem newArrayElem = new AnnotationArrayElem(newValues2, annotationArrayElem.getKind(), annotationArrayElem.getName());
                                    newValues.add(newArrayElem);
                                }
                            }
                            annotation.setElems(newValues);
                        }
                    }
                }
            }
        }
        return changed;
    }

    public ClassCollapserData getClassCollapserData(){
        return new ClassCollapserData(this.removedMethods, this.classesToRemove, this.classesToRewrite);
    }
}
