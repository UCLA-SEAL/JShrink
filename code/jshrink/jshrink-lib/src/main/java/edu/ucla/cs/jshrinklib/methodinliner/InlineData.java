package edu.ucla.cs.jshrinklib.methodinliner;

import edu.ucla.cs.jshrinklib.reachability.MethodData;
import soot.SootClass;

import java.util.*;

public class InlineData {
	//A container class
	private Set<SootClass> classesModified = new HashSet<SootClass>();
	/*
	Note: Need to store methods as strings : as they are removed, they no loner exist. We store their former
	signatures.
	 */
	private Map<MethodData, Set<MethodData>> inlineLocations = new HashMap<MethodData, Set<MethodData>>();

	public InlineData(){}

	/*package*/ void addInlinedMethods(MethodData calleeMethod, MethodData callerMethod){
		if(!this.inlineLocations.containsKey(calleeMethod)){
			this.inlineLocations.put(calleeMethod, new HashSet<MethodData>());
		}
		this.inlineLocations.get(calleeMethod).add(callerMethod);
	}

	/*package*/ void addClassModified(SootClass modifiedClass){
		this.classesModified.add(modifiedClass);
	}

	public Set<SootClass> getClassesModified(){
		return Collections.unmodifiableSet(this.classesModified);
	}

	public Map<MethodData, Set<MethodData>> getInlineLocations(){
		return Collections.unmodifiableMap(this.inlineLocations);
	}

	public Optional<Set<MethodData>> getUltimateInlineLocations(MethodData methodData){
			/*
			As inlining may happen in a chain, it's useful to know where after all inlining is done, where the inlined
			method ultimately exists. Returns empty optional if the signature is not a valid inlined method
			*/
		Optional<Set<MethodData>> toReturn = this.inlineLocations.containsKey(methodData)
				? Optional.of(this.inlineLocations.get(methodData)) : Optional.empty();
		if(toReturn.isPresent()) {
			Set<MethodData> temp = new HashSet<MethodData>(toReturn.get());
			for (MethodData loc : temp) {
				Optional<Set<MethodData>> locLocs = getUltimateInlineLocations(loc);
				if(locLocs.isPresent()){
					toReturn.get().remove(loc);
					toReturn.get().addAll(locLocs.get());
				}
			}
		}

		assert((toReturn.isPresent() && !toReturn.get().isEmpty()) || !toReturn.isPresent());
		return toReturn;
	}
}
