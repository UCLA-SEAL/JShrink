package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.util.EntryPointUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

//TODO: I think this could be merged with "EntryPointUtil"

public class EntryPointProcessor implements Serializable {
    private final boolean mainEntry;
    private final boolean publicEntry;
    private final boolean testEntry;

    private Set<MethodData> customEntry;


    public EntryPointProcessor(boolean setMainEntry, boolean setPublicEntry,
                               boolean setTestEntry, Set<MethodData> setCustomEntry){
        this.mainEntry = setMainEntry;
        this.publicEntry = setPublicEntry;
        this.testEntry = setTestEntry;
        this.customEntry = setCustomEntry;
    }

    public boolean isMainEntry(){
        return this.mainEntry;
    }

    public boolean isPublicEntry(){
        return this.publicEntry;
    }

    public boolean isTestEntry(){
        return this.testEntry;
    }

    public Set<MethodData> getCustomEntry(){
        return this.customEntry;
    }

    public Set<MethodData> getEntryPoints(Set<MethodData> appMethods, Set<MethodData> testMethods){
        Set<MethodData> toReturn = new HashSet<MethodData>();

        if(this.mainEntry){
            toReturn.addAll(EntryPointUtil.getMainMethodsAsEntryPoints(appMethods));
        }

        if(this.publicEntry){
            toReturn.addAll(EntryPointUtil.getPublicMethodsAsEntryPoints(appMethods));
        }

        if(this.testEntry){
            toReturn.addAll(EntryPointUtil.getTestMethodsAsEntryPoints(testMethods));
        }

        toReturn.addAll(customEntry);

        return toReturn;
    }
}
