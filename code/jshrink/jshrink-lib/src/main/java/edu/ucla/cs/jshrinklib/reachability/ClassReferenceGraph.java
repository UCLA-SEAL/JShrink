package edu.ucla.cs.jshrinklib.reachability;

import soot.*;
import soot.javaToJimple.InitialResolver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassReferenceGraph implements Serializable {
    private HashMap<String, Set<String>> graph=null;
    public ClassReferenceGraph(){
        graph = new HashMap<>();
    }
    private Set<String> getConstantPoolReferences(String classPath) throws Exception{
        return ConstantPoolScanner.getClassReferences(classPath);
    }
    private Set<String> getSootReferences(SootClass sc){
        Set<String> toReturn = new HashSet<>();
        String name;
        for(soot.Type st: SootResolver.v().getReferenceSignatures(sc)){
            if(st instanceof RefType){
                name = ((RefType) st).getClassName();
                if(name.startsWith("java") || name.startsWith("sun"))
                    continue;
                toReturn.add(name);
            }
        }
        return toReturn;
    }
    public void addClass(String className, Set<String> references){
        Set<String> refererrs;
        className = className.replaceAll("/",".");
        references.remove(className);
        for(String ref: references){
            refererrs = graph.getOrDefault(ref, new HashSet<String>());
            refererrs.add(className);
            graph.put(ref, refererrs);
        }
    }

    public void addClass(String className, String classPath){
        try{
            addClass(className, this.getConstantPoolReferences(classPath));
        }
        catch (Exception e) {
            System.err.println("An an exception was thrown while getting references for Class "+className+" at "+classPath);
            e.printStackTrace();
            //System.exit(1);
        }
    }

    public void addAll(HashMap<String, String> classNamePathMap) {
        Set<String> references, refererrs;
        for(String clazz: classNamePathMap.keySet()){
            this.addClass(clazz, classNamePathMap.get(clazz));
        }
    }

    public void addClass(SootClass sc){
        try{
            addClass(sc.getName(), this.getSootReferences(sc));
        }
        catch (Exception e) {
            System.err.println("An an exception was thrown while getting references for Class "+sc.getName());
            e.printStackTrace();
            //System.exit(1);
        }
    }



    public Set<String> getReferences(String className){
        Set<String> references = new HashSet<String>();
        for(Map.Entry<String, Set<String>> e:graph.entrySet()){
            if(e.getValue().contains(className))
                references.add(e.getKey());
        }
        return references;
    }

    public Set<String> getReferencedBy(String className){
        return graph.getOrDefault(className, new HashSet<String>());
    }

    public Set<String> getNodes(){
        return graph.keySet();
    }
}
