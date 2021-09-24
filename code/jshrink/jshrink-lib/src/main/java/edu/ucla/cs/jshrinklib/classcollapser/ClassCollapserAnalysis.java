package edu.ucla.cs.jshrinklib.classcollapser;

import edu.ucla.cs.jshrinklib.JShrink;
import edu.ucla.cs.jshrinklib.reachability.FieldData;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.FilePathProcessor;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import fj.P;
import soot.*;
import soot.JastAddJ.Modifiers;

import edu.ucla.cs.jshrinklib.reachability.MethodData;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.ondemand.pautil.SootUtil;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.util.EmptyChain;


import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassCollapserAnalysis {
    public Set<String> appClasses;
    private Set<String> usedAppClasses;
    public Map<String, Set<String>> usedAppMethods;
    private Set<MethodData> usedAppMethodData;
    private Set<String> processableLeaves;
    private LinkedList<ArrayList<String>> collapseList;
    private Set<String> removeList;
    private Map<String, String> nameChangeList;

    private Map<String, String> parentsMap; // class -> superclass
    public Map<String, Set<String>> childrenMap; // class -> subclasses
    private Map<String, Set<String>> parentsVirtualMap; // class -> interfaces
    private Map<String, Set<String>> childrenVirtualMap; // interface -> subclasses
    private Map<String, SootClass> appClassMap;
    private Set<MethodData> entryPoints;
    private Set<String> classesToIgnore;

    private Map<MethodData, Set<MethodData>> callGraph;

    public ClassCollapserAnalysis(Set<String> appCls,
                                  Set<String> usedAppCls,
                                  Set<MethodData> usedAppMethodData,
                                  Map<MethodData, Set<MethodData>> callGraph,
                                  Set<MethodData> entryPoints,
                                  Set<String> classesToIgnore) {

        appClasses = appCls;
        parentsMap = new HashMap<String, String>();
        childrenMap = new HashMap<String, Set<String>>();
        parentsVirtualMap  = new HashMap<String, Set<String>>();
        childrenVirtualMap = new HashMap<String, Set<String>>();
        processableLeaves = new HashSet<String>();
        collapseList = new LinkedList<ArrayList<String>>();
        nameChangeList = new HashMap<String, String>();
        removeList = new HashSet<String>();
        appClassMap = new HashMap<String, SootClass> ();
        this.entryPoints = entryPoints;
        usedAppClasses = new HashSet<String>(usedAppCls);  //We're modifying used appClasses in this analysis, therefore copy here
        this.usedAppMethodData = usedAppMethodData;

        usedAppMethods = new HashMap<String, Set<String>>();
        for (MethodData m: usedAppMethodData) {
            String className = m.getClassName();
            if (!usedAppMethods.containsKey(className)) {
                usedAppMethods.put(className, new HashSet<String>());
            }
            usedAppMethods.get(className).add(m.getSubSignature());
        }

        this.callGraph = callGraph;
        this.classesToIgnore = new HashSet<String>(classesToIgnore);
    }

    public void run() {
        setup();
        // bug fix for issue#99, enforce transitive class usage
        enforceTransitiveClassUsage();
        LinkedList<String> queue = new LinkedList<String>();
        HashSet<String> visited = new HashSet<String>();
        for (String leaf: processableLeaves) {
            queue.addLast(leaf);
        }
        while (!queue.isEmpty()) {
            String child = queue.removeFirst();
            visited.add(child);
            Set<String> parents = new HashSet<String>();
            if (!parentsMap.get(child).isEmpty()) {
                parents.add(parentsMap.get(child));
            }
            for (String p: parentsVirtualMap.get(child)) {
                parents.add(p);
            }

            if (usedAppClasses.contains(child) && parents.size() == 1) {
                String singleParent = parents.iterator().next();
                boolean cont = false;
                for (String c: childrenMap.get(singleParent)) {
                    if (!visited.contains(c) && !queue.contains(c)) {
                        queue.addLast(child);
                    }

                    if(!c.equals(child) && usedAppClasses.contains(c)) {
                        // quick check whether there is a sibling class that is also used
                        cont = true;
                        break;
                    }
                }
                for (String c : childrenVirtualMap.get(singleParent)) {
                    if (!visited.contains(c) && !queue.contains(c)) {
                        queue.addLast(child);
                    }

                    if(!c.equals(child) && usedAppClasses.contains(c)) {
                        // quick check whether there is a sibling class that is also used
                        cont = true;
                        break;
                    }
                }

                if(!cont) {
                    // none of the sibling is used, can further check
                    SootClass fromClass = Scene.v().loadClassAndSupport(child);
                    SootClass toClass = Scene.v().loadClassAndSupport(singleParent);
                    if (collapsable(child, singleParent, fromClass, toClass) && checkMethodOverridesFromHierarchy(child, singleParent)) {
                        ArrayList<String> collapse = new ArrayList<String>();
                        collapse.add(child);
                        collapse.add(singleParent);
                        collapseList.addLast(collapse);
                        nameChangeList.put(child, singleParent);
                        usedAppClasses.add(singleParent);

                        // all the subclasses or interfaces of this parent class will be removed
                        if(childrenMap.containsKey(singleParent)) {
                            for (String child2 : childrenMap.get(singleParent)) {
                                removeList.add(child2);
                            }
                        }
                        if(childrenVirtualMap.containsKey(singleParent)) {
                            for(String child2 : childrenVirtualMap.get(singleParent)) {
                                removeList.add(child2);
                            }
                        }
                    }
                }
            }
            for (String parent: parents) {
                Set<String> children = new HashSet<String>();
                children.addAll(childrenMap.get(parent));
                children.addAll(childrenVirtualMap.get(parent));
                if(visited.containsAll(children) && !visited.contains(parent) && !queue.contains(parent)) {
                    // all children has been processed
                    // move on to their parent
                    queue.addLast(parent);
                }
            }
        }
        postprocess();
    }

    private void setup() {
        initClassHierarchy();
        initLeaves();
        handleVirtualCallsAndSuperConstructors();
    }

    private void handleVirtualCallsAndSuperConstructors() {
        // handle virtual method calls and superclass constructor calls
        Set<String> affectedClasses = new HashSet<String>();
        for(MethodData m : usedAppMethodData) {
            Set<MethodData> callers = callGraph.get(m);
            if(callers.isEmpty() && !entryPoints.contains(m)) {
                // this is a virtual call that is never invoked at runtime
                // exclude it from used methods
                String subSignature = m.getSubSignature();
                String className = m.getClassName();
                usedAppMethods.get(className).remove(subSignature);
                affectedClasses.add(className);
            } else if (m.getName().equals("<init>")) {
                // check if this constructor is only called in the constructors of its subclasses
                String className = m.getClassName();
                if(!childrenMap.containsKey(className)) {
                    // this class has no subclasses, so no need to check
                    continue;
                }

                Set<String> subClasses = childrenMap.get(className);
                boolean onlyCalledInSubClassConstructor = true;
                for(MethodData caller : callers) {
                    String callerClass = caller.getClassName();
                    if(!subClasses.contains(callerClass)) {
                        // this constructor is not only called in its subclasses
                        onlyCalledInSubClassConstructor = false;
                        break;
                    }
                }

                if(onlyCalledInSubClassConstructor) {
                    String subSignature = m.getSubSignature();
                    usedAppMethods.get(className).remove(subSignature);
                    affectedClasses.add(className);
                }
            }
        }

        // after removing unused virtual calls and superclass constructor calls
        // we need to update the used classes to filter out those used classes that are included due to these calls
        for(String className : affectedClasses) {
            if(usedAppMethods.get(className).isEmpty()) {
                usedAppMethods.remove(className);
                usedAppClasses.remove(className);
            }
        }
    }

    private void postprocess() {
        List<String> keys = new ArrayList<String>(nameChangeList.keySet());
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            boolean changed = nameChangeList.containsKey(key);
            while (changed) {
                if (nameChangeList.containsKey(nameChangeList.get(key))) {
                    nameChangeList.put(key, nameChangeList.get(nameChangeList.get(key)));
                } else {
                    changed = false;
                }
            }
        }
    }

    private boolean collapsable(String from, String to, SootClass fromClass, SootClass toClass) {
        if(classesToIgnore.contains(from) || classesToIgnore.contains(to)) {
            return false;
        }

        if (isAnnoymousInner(from) || isAnnoymousInner(to)) {
            return false;
        }

        if(isInnerClass(from) && isInnerClass(to)) {
            // A temporary fix for merging two inner classes
            // Merging two inner classes is very tricky because inner classes have a field called this$0 to reference
            // to the outer classes. In our current implementation, the this$0 field in the super inner class is removed
            // but it is actually used in the <init> method of the super inner class, causing a " resolved field is null"
            // exception in FieldRefValidator.java in Soot when writing out the merged class to bytecode.
            return false;
        }


        if (fromClass.isEnum() || toClass.isEnum()) {
            return false;
        }

        if(toClass.isInterface() && !fromClass.isInterface()) {
            // do not merge a class to an interface
            return false;
        }

//        if ((toClass.isInterface() && !fromClass.isInterface() && (fromClass.getFields() instanceof EmptyChain))) {
//            return false;
//        }

        if (!(fromClass.getFields() instanceof EmptyChain) && (toClass.getFields() instanceof EmptyChain)) {
            return false;
        }

        if (fromClass.isStatic() || toClass.isStatic()) {
            return false;
        }


        if(JShrink.enable_member_visibility) {
            if(!isSafeAccessAfterMerge(toClass, fromClass)){
                return false;
            }
        }

        int numUsedChildren = 0;
        for (String child: childrenMap.get(to)) {
            if (usedAppClasses.contains(child)) {
                numUsedChildren += 1;
            }
        }
        for (String child: childrenVirtualMap.get(to)) {
            if (usedAppClasses.contains(child)) {
                numUsedChildren += 1;
            }
        }
        if (numUsedChildren <= 1) {
            for (SootMethod m: fromClass.getMethods()) {
                if (usedAppMethods.containsKey(to)) {
                    String signature = m.getSubSignature();
                    Set<String> usedMethodsInSuperClass = usedAppMethods.get(to);
                    if(usedMethodsInSuperClass.contains(signature)) {
                        // if there is a method with the same signature in the super class is used,
                        // then we cannot merge the sub class into the super class
                        return false;
                    } else if(m.getReturnType().toString().equals(from)) {
                        // covariant return type is allowed in an overriding method
                        // if a overriding method with a covariant return type and the overridden method in
                        // the super class are both used. We cannot merge them.
                        String signature2 = to + " " + signature.substring(signature.indexOf(' ') + 1);
                        if(usedMethodsInSuperClass.contains(signature2)) {
                            return false;
                        }
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean isSafeAccessAfterMerge(SootClass to, SootClass from){
        //If the classes to be merged are in the same package, then we have no problem.
        if(to.getPackageName().equals(from.getPackageName())){
            return true;
        }

        //Is the class to be merged package-private?
        if(SootUtils.isPackagePrivate(from)){
            return false;
        }

        //Are any of the fields package-private or are types that are package-private?
        for(SootField sootField: from.getFields()){
            if(SootUtils.isPackagePrivate(sootField)){
                return false;
            }

            SootClass type = Scene.v().getSootClass(sootField.getType().toString());
            if(SootUtils.isPackagePrivate(type)){
                return false;
            }
        }

        //Are any of the methods package-private?
        for(SootMethod sootMethod : from.getMethods()){

            if(SootUtils.isPackagePrivate(sootMethod)){
                return false;
            }

			if(sootMethod.isAbstract() || sootMethod.isNative()){
				continue;
			}

            //Does any method contain reference to a package-private class, method, or field?
            Body b = sootMethod.retrieveActiveBody();
            for(Unit unit : b.getUnits()) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
					InvokeExpr callExpr = stmt.getInvokeExpr();
					SootMethodRef smf = callExpr.getMethodRef();
					SootClass sootClass = smf.getDeclaringClass();
					SootMethod invokedMethod = smf.tryResolve();
					if(invokedMethod == null){
						//At this level we cannot always resolve virtual methods unfortunately.
						//I don't know as of yet how this may affect this check.
						continue;
					}
					if (SootUtils.isPackagePrivate(invokedMethod)) {
						return false;
					}
					if (SootUtils.isPackagePrivate(sootClass)) {
						return false;
					}
                } else if(stmt.containsFieldRef()){
                    FieldRef fieldRef = stmt.getFieldRef();
                    SootField sootField = fieldRef.getField();
                    if(SootUtils.isPackagePrivate(sootField)){
                        return false;
                    }
                }
            }

        }

        return true;
    }

  //  private boolean isPackagePrivate(int modifiers){
  //      return !Modifier.isPrivate(modifiers) && !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers);
  //  }

    private boolean isAnnoymousInner(String name) {
        int realNameIndex = name.length() - 1;
        while(realNameIndex >= 0 && name.charAt(realNameIndex) != '$') {
            realNameIndex -= 1;
        }
        if (realNameIndex < 0) {
            return false;
        }
        String realName = name.substring(realNameIndex + 1);
        for (int i = 0; i < realName.length(); ++ i) {
            if (!Character.isDigit(realName.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isInnerClass(String name) {
        return name.contains("$");
    }

    private void initLeaves() {
        for (String parent: childrenMap.keySet()) {
            if (childrenMap.get(parent).size() == 0 && childrenVirtualMap.get(parent).size() == 0) {
                processableLeaves.add(parent);
            }
        }
    }

    private void initClassHierarchy() {
        Set<String> visited = new HashSet<String>();

        for (String c: appClasses) {
            parentsMap.put(c, "");
            childrenMap.put(c, new HashSet<String>());
            parentsVirtualMap.put(c, new HashSet<String>());
            childrenVirtualMap.put(c, new HashSet<String>());
        }

        for (String c: appClasses) {
            initOneClass(c, visited);
        }
    }

    private void initOneClass(String thisClass, Set<String> visited) {
        if (visited.contains(thisClass)) {
            return;
        }
        visited.add(thisClass);

        SootClass sootClass = Scene.v().loadClassAndSupport(thisClass);
        Scene.v().loadNecessaryClasses();
        appClassMap.put(thisClass, sootClass);
        if (sootClass.hasSuperclass() && childrenMap.containsKey(sootClass.getSuperclass().getName())) {
            String superClass = sootClass.getSuperclass().getName();
            parentsMap.put(thisClass, superClass);
            childrenMap.get(superClass).add(thisClass);
        }
        for (SootClass c : sootClass.getInterfaces()) {
            String superInterface = c.getName();
            if (childrenVirtualMap.containsKey(superInterface)) {
                parentsVirtualMap.get(thisClass).add(superInterface);
                childrenVirtualMap.get(superInterface).add(thisClass);
            }
            if(usedAppClasses.contains(thisClass) && !usedAppClasses.contains(superInterface)) {
                usedAppClasses.add(superInterface);
            }
        }
     }

     // Class is used if its subclass is used
     private void enforceTransitiveClassUsage() {
         HashSet<String> tmp = new HashSet<String>(usedAppClasses);
         for(String className : tmp) {
             enforceTransitiveClassUsage(className);
         }
     }

     private void enforceTransitiveClassUsage(String className) {
         if(parentsMap.containsKey(className)) {
             String superClass = parentsMap.get(className);
             if(!superClass.isEmpty() && !usedAppClasses.contains(superClass)) {
                 usedAppClasses.add(superClass);
                 enforceTransitiveClassUsage(superClass);
             }
         }

         if(parentsVirtualMap.containsKey(className)) {
             Set<String> superInterfaces = parentsVirtualMap.get(className);
             for(String superInterface : superInterfaces) {
                 if(!usedAppClasses.contains(superInterface)) {
                     usedAppClasses.add(superInterface);
                     enforceTransitiveClassUsage(superInterface);
                 }
             }
         }
     }

     private boolean checkMethodOverridesFromHierarchy(String from, String to){
        SootClass fromClass = Scene.v().loadClassAndSupport(from);
        for(SootMethod m:fromClass.getMethods()){
            if(m.getName().contains("init"))
                continue;
            String parentClass = to;
            while(parentsMap.get(parentClass).length()>0){
                parentClass = parentsMap.get(parentClass);
                Set<String> usedMethodsInSuperClass = usedAppMethods.get(parentClass);
                if(usedMethodsInSuperClass!=null && usedMethodsInSuperClass.contains(m.getSubSignature())) {
                    // if there is a method with the same signature in the super class is used,
                    // then we cannot merge the sub class into the super class
                    return false;
                }
            }
        }
        return true;
     }


    /*package*/ Queue<ArrayList<String>> getCollapseList() {
        return collapseList;
    }

    /*package*/ Map<String, String> getNameChangeList() {
        return nameChangeList;
    }

    /*package*/ Set<String> getRemoveList() {
        return removeList;
    }

    /*package*/ Map<String, Set<String>> getProcessedUsedMethods() {
        return usedAppMethods;
    }

    public Map<MethodData, Set<MethodData>> getCallGraph() {
        return callGraph;
    }
}
