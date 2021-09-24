package edu.ucla.cs.jshrinklib.classcollapser;

import edu.ucla.cs.jshrinklib.reachability.MethodData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassCollapserData {
	private final Set<MethodData> removedMethods;
	private final Set<String> classesToRemove;
	private final Set<String> classesToRewrite;

	public ClassCollapserData(Set<MethodData> removedMethods,
	                          Set<String> classesToRemove, Set<String> classesToRewrite){
		this.removedMethods = new HashSet<MethodData>(removedMethods);
		this.classesToRemove = new HashSet<String>(classesToRemove);
		this.classesToRewrite = new HashSet<String>(classesToRewrite);
	}

	public Set<MethodData> getRemovedMethods(){
		return Collections.unmodifiableSet(this.removedMethods);
	}

	public Set<String> getClassesToRemove(){
		return Collections.unmodifiableSet(this.classesToRemove);
	}

	public Set<String> getClassesToRewrite(){
		return Collections.unmodifiableSet(this.classesToRewrite);
	}
}
