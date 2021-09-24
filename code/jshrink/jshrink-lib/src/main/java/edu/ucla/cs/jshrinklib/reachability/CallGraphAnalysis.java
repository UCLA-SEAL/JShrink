package edu.ucla.cs.jshrinklib.reachability;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import edu.ucla.cs.jshrinklib.util.ASMUtils;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.EntryPointUtil;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

public class CallGraphAnalysis implements IProjectAnalyser, Serializable {

	private List<File> libJarPath;
	private List<File> appClassPath;
	private List<File> appTestPath;
	private final Set<MethodData> entryMethods;
	private final Set<String> libClasses;
	private final Set<MethodData> libMethods;
	private final Set<FieldData> libFields;
	private final Map<MethodData, Set<FieldData>> libFieldReferences;
	private final Set<String> appClasses;
	private final Set<MethodData> appMethods;
	private final Set<FieldData> appFields;
	private final Map<MethodData, Set<FieldData>> appFieldReferences;
	private final Set<String> usedLibClasses;
	private final Map<MethodData,Set<MethodData>> usedLibMethods; // callee -> a set of callers
	private final Set<FieldData> usedLibFields;
	private final Set<String> usedAppClasses;
	private final Map<MethodData,Set<MethodData>> usedAppMethods; // callee -> a set of callers
	private final Set<FieldData> usedAppFields;
	private final Set<MethodData> testMethods;
	private final Set<String> testClasses;
	private final Map<MethodData, Set<MethodData>> usedTestMethods;
	private final Set<String> usedTestClasses;
	private final EntryPointProcessor entryPointProcessor;
	private final boolean useSpark;
	private final Map<MethodData, Set<MethodData>> virtualMethodCalls;

	public CallGraphAnalysis(List<File> libJarPath,
	                              List<File> appClassPath, 
	                              List<File> appTestPath, 
	                              EntryPointProcessor entryPointProc,
	                         boolean useSpark) {
		this.libJarPath = libJarPath;
		this.appClassPath = appClassPath;
		this.appTestPath = appTestPath;
		this.entryMethods = new HashSet<MethodData>();

		libClasses = new HashSet<String>();
		libMethods = new HashSet<MethodData>();
		libFields = new HashSet<FieldData>();
		libFieldReferences = new HashMap<MethodData, Set<FieldData>>();
		appClasses = new HashSet<String>();
		appMethods = new HashSet<MethodData>();
		appFields = new HashSet<FieldData>();
		appFieldReferences = new HashMap<MethodData, Set<FieldData>>();
		usedLibClasses = new HashSet<String>();
		usedLibMethods = new HashMap<MethodData, Set<MethodData>>();
		usedLibFields = new HashSet<FieldData>();
		usedAppClasses = new HashSet<String>();
		usedAppMethods = new HashMap<MethodData, Set<MethodData>>();
		usedAppFields = new HashSet<FieldData>();
		testClasses = new HashSet<String>();
		testMethods = new HashSet<MethodData>();
		usedTestMethods = new HashMap<MethodData, Set<MethodData>>();
		usedTestClasses = new HashSet<String>();
		entryPointProcessor = entryPointProc;
		this.useSpark = useSpark;
		virtualMethodCalls = new HashMap<MethodData, Set<MethodData>>();
	}

	@Override
	public void setup() {
		/* Setup is used to get the app/test/lib classpath information. In ClassGraphAnalysis, this is given via the
		constructor and, therefore, does not need generated as in MavenProjectAnalysis
		 */
	}

	@Override
	public void run() {
        // 1. use ASM to find all classes and methods
        this.findAllClassesAndMethodsAndFields();
        // 2. get entry points
        this.entryMethods.addAll(this.entryPointProcessor.getEntryPoints(appMethods,testMethods));
		// 3. construct the call graph and compute the reachable classes and methods
		this.runCallGraphAnalysis();
	}

	/*package*/ void setLibJarPath(List<File> libJarPath){
		this.libJarPath = libJarPath;
	}

	/*package*/ void setAppClassPath(List<File> appClassPath){
		this.appClassPath = appClassPath;
	}

	/*package*/ void setAppTestPath(List<File> appTestPath){
		this.appTestPath = appTestPath;
	}
	
	
	/**
	 * 
	 * This method is used to run Spark/CHA given a set of entry methods found by TamiFlex
	 *  
	 * @param entryPoints
	 */
	public void run(Set<MethodData> entryPoints) {
		// 1. use ASM to find all classes and methods
        this.findAllClassesAndMethodsAndFields();
        
		// clear just in case this method is misused---should not call this method twice or call this
		// after calling the overriden run method
		if(!this.entryMethods.isEmpty()) {
			this.entryMethods.clear();
		}
		
		// 2. add the given entry points
		entryMethods.addAll(entryPoints);
		
		
		// 3. run call graph analysis
		this.runCallGraphAnalysis();
	}

	private void findAllClassesAndMethodsAndFields() {
		for (File lib : this.libJarPath) {
			HashSet<String> classes_in_this_lib = new HashSet<String>();
			HashSet<MethodData> methods_in_this_lib = new HashSet<MethodData>();
			HashSet<FieldData> fields_in_this_lib = new HashSet<FieldData>();
			ASMUtils.readClass(lib, classes_in_this_lib, methods_in_this_lib, fields_in_this_lib, libFieldReferences, virtualMethodCalls);
			this.libClasses.addAll(classes_in_this_lib);
			this.libMethods.addAll(methods_in_this_lib);
			this.libFields.addAll(fields_in_this_lib);
		}

		for (File appPath : appClassPath) {
			ASMUtils.readClass(appPath, appClasses, appMethods, appFields, appFieldReferences, virtualMethodCalls);
		}

		for (File testPath : this.appTestPath){
			// no need to collect field data for test cases
			ASMUtils.readClass(testPath, testClasses, testMethods,null, null, virtualMethodCalls);
		}
	}

	private void runCallGraphAnalysis() {
		// must call this first, and we only need to call it once
		SootUtils.setup_trimming(this.libJarPath, this.appClassPath, this.appTestPath);
		Scene.v().loadNecessaryClasses();

		List<SootMethod> entryPoints = EntryPointUtil.convertToSootMethod(entryMethods);
		Scene.v().setEntryPoints(entryPoints);

		if(this.useSpark) {
			Map<String, String> opt = SootUtils.getSparkOpt();
			SparkTransformer.v().transform("", opt);
		} else {
			CHATransformer.v().transform();
		}

		CallGraph cg = Scene.v().getCallGraph();

		Map<MethodData,Set<MethodData>> usedMethods = new HashMap<MethodData,Set<MethodData>>();
		Set<String> usedClasses = new HashSet<String>();

		for (SootMethod entryMethod : entryPoints) {
			MethodData entryMethodData = SootUtils.sootMethodToMethodData(entryMethod);
			if(!usedMethods.containsKey(entryMethodData)) {
				usedMethods.put(entryMethodData, new HashSet<MethodData>());
			}
			usedClasses.add(entryMethodData.getClassName());
			SootUtils.visitMethodNonRecur(entryMethod, cg, usedClasses, usedMethods, this.appClasses, this.libClasses);
		}

		// check for used library classes and methods
		this.usedLibClasses.addAll(this.libClasses);
		this.usedLibClasses.retainAll(usedClasses);
		for(Map.Entry<MethodData, Set<MethodData>> e : usedMethods.entrySet()){
			if(this.libMethods.contains(e.getKey())){
				this.usedLibMethods.put(e.getKey(), e.getValue());
			}
		}

		// check for used application classes and methods
		this.usedAppClasses.addAll(this.appClasses);
		this.usedAppClasses.retainAll(usedClasses);
		for(Map.Entry<MethodData, Set<MethodData>> e : usedMethods.entrySet()){
			if(this.appMethods.contains(e.getKey())){
				this.usedAppMethods.put(e.getKey(),e.getValue());
			}
		}

		// check for used test classes and methods
		this.usedTestClasses.addAll(this.testClasses);
		this.usedTestClasses.retainAll(usedClasses);
		for(Map.Entry<MethodData, Set<MethodData>> e : usedMethods.entrySet()){
			if(this.testMethods.contains(e.getKey())){
				this.usedTestMethods.put(e.getKey(), e.getValue());
			}
		}

		// check for the referenced but not actually invoked methods
		// we still want to keep those methods since JVM needs to find them at runtime for dynamic dispatching
		for(MethodData method : this.virtualMethodCalls.keySet()) {
			if (this.usedLibMethods.containsKey(method) || this.usedAppMethods.containsKey(method)
					|| this.usedTestMethods.containsKey(method) || this.entryMethods.contains(method)) {
				// this method is used, check whether all virtual calls in this method is also in the used method set
				Set<MethodData> virtualCalls = this.virtualMethodCalls.get(method);
				for (MethodData virtualCall : virtualCalls) {
					String className = virtualCall.getClassName();
					if (appClasses.contains(className)) {
						// a virtual call to an application method
						MethodData md = findMethodCall(virtualCall, appMethods);
						if (md != null) {
							if (!usedAppMethods.containsKey(md)) {
								usedAppMethods.put(md, new HashSet<MethodData>());
								if (!usedAppClasses.contains(className)) {
									usedAppClasses.add(className);
								}
							}
						}
					} else if (libClasses.contains(className)) {
						// a virtual call to a library method
						MethodData md = findMethodCall(virtualCall, libMethods);
						if (md != null) {
							if (!usedLibMethods.containsKey(md)) {
								usedLibMethods.put(md, new HashSet<MethodData>());
								if (!usedLibClasses.contains(className)) {
									usedLibClasses.add(className);
								}
							}
						}
					}
				}
			}
		}

		// check for used fields
		for(MethodData method : usedMethods.keySet()) {
			Set<FieldData> fieldRefs;
			if(this.libFieldReferences.containsKey(method)) {
				fieldRefs = this.libFieldReferences.get(method);
			} else if (this.appFieldReferences.containsKey(method)) {
				fieldRefs = this.appFieldReferences.get(method);
			} else {
				continue;
			}

			Map<FieldData, String> fieldToUpdate = new HashMap<FieldData, String>();
			for(FieldData field : fieldRefs) {
				if(libFields.contains(field)) {
					usedLibFields.add(field);
				} else if (appFields.contains(field)) {
					usedAppFields.add(field);
				} else {
					// two cases: 1. this field is from JDK class 2. this field is inherited from super class
					// we need to handle the second case
					String ownerClass = field.getClassName();
					if(this.libClasses.contains(ownerClass) || this.appClasses.contains(ownerClass)) {
						// this field is inherited
						boolean notFound = true;
						String currentClassName = ownerClass;
						while(notFound) {
							SootClass sootClass = Scene.v().getSootClass(currentClassName);
							if(!sootClass.hasSuperclass()) {
								// the field is inherited from a JDK class
								break;
							}

							SootClass superClass = sootClass.getSuperclass();
							for(SootField superField : superClass.getFields()) {
								if(superField.getName().equals(field.getName())) {
									notFound = false;
									// If we directly modify the field data, the hash set
									// will not update the pre-computed hash value of this field
									// data in its hashtable
									fieldToUpdate.put(field, superClass.getName());

									break;
								}
							}
							currentClassName = superClass.getName();
						}
					}
				}
			}

			for(FieldData fd : fieldToUpdate.keySet()) {
				String superClassName = fieldToUpdate.get(fd);
				fieldRefs.remove(fd);
				fd.setClassName(superClassName);
				fieldRefs.add(fd);

				// also add this updated field data to the corresponding used field set
				if(this.libClasses.contains(superClassName)) {
					this.usedLibFields.add(fd);
				} else if (this.appClasses.contains(superClassName)) {
					this.usedAppFields.add(fd);
				}
			}
		}
	}

	/**
	 * This method is to find the original MethodData object of a method call. The isPublic and isStatic fields in a
	 * method call object are always set to true and false since there is no method modifier information in the callsite.
	 * So we use this method to find the original MethodData object of a method call. Return null if we cannot find one.
	 *
	 * @param call
	 * @param set
	 * @return
	 */
	private MethodData findMethodCall(MethodData call, Set<MethodData> set) {
		for(MethodData method : set) {
			if(method.getClassName().equals(call.getClassName())
				&& method.getName().equals(call.getName())
				&& Arrays.equals(method.getArgs(), call.getArgs())) {
				return method;
			}
		}

		return null;
	}

	@Override
	public Set<String> getLibClasses() {
		return Collections.unmodifiableSet(this.libClasses);
	}

	@Override
	public Set<MethodData> getLibMethods() {
		return Collections.unmodifiableSet(this.libMethods);
	}

	@Override
	public Set<FieldData> getLibFields() {
		return Collections.unmodifiableSet(this.libFields);
	}

	//TODO: I don't like having these three mehtods here. Not really relevant to the call graph.
	public String getLibPathOfMethod(MethodData methodData) {
		return ClassFileUtils.classInPath(methodData.getClassName(),
			this.libJarPath).get(0).getAbsolutePath();
	}

	public String getLibPathOfField(FieldData fieldData) {
		return ClassFileUtils.classInPath(fieldData.getClassName(),
			this.libJarPath).get(0).getAbsolutePath();
	}

	public String getLibPathOfClass(String libClass) {
		List<File> files = ClassFileUtils.classInPath(libClass, this.libJarPath);
		if(files.size() == 0) {
			return null;
		}
		return ClassFileUtils.classInPath(libClass, this.libJarPath).get(0).getAbsolutePath();
	}

	@Override
	public Set<String> getAppClasses() {
		return Collections.unmodifiableSet(this.appClasses);
	}

	@Override
	public Set<MethodData> getAppMethods() {
		return Collections.unmodifiableSet(this.appMethods);
	}

	@Override
	public Set<FieldData> getAppFields() {
		return Collections.unmodifiableSet(this.appFields);
	}

	public Map<MethodData, Set<FieldData>> getAppFieldReferences() {
		return Collections.unmodifiableMap(this.appFieldReferences);
	}

	@Override
	public Set<String> getUsedLibClasses() {
		return Collections.unmodifiableSet(this.usedLibClasses);
	}

	@Override
	public Map<MethodData,Set<MethodData>> getUsedLibMethods() {
		return Collections.unmodifiableMap(this.usedLibMethods);
	}

	@Override
	public Set<FieldData> getUsedLibFields() {
		return Collections.unmodifiableSet(this.usedLibFields);
	}

	@Override
	public Set<String> getUsedAppClasses() {
		return Collections.unmodifiableSet(this.usedAppClasses);
	}

	@Override
	public Map<MethodData,Set<MethodData>>getUsedAppMethods() {
		return Collections.unmodifiableMap(this.usedAppMethods);
	}

	@Override
	public Set<FieldData> getUsedAppFields() {
		return Collections.unmodifiableSet(this.usedAppFields);
	}

	@Override
	public List<File> getAppClasspaths() {
		return Collections.unmodifiableList(this.appClassPath);
	}

	@Override
	public List<File> getLibClasspaths() {
		return Collections.unmodifiableList(this.libJarPath);
	}

	@Override
	public List<File> getTestClasspaths() {
		return Collections.unmodifiableList(this.appTestPath);
	}

	@Override
	public Set<MethodData> getEntryPoints() {
		return Collections.unmodifiableSet(this.entryMethods);
	}

	@Override
	public Set<String> getUsedLibClassesCompileOnly() {
		return this.getUsedLibClasses();
	}

	@Override
	public Map<MethodData,Set<MethodData>> getUsedLibMethodsCompileOnly() {
		return this.getUsedLibMethods();
	}

	@Override
	public Set<FieldData> getUsedLibFieldsCompileOnly() {
		return this.getUsedLibFieldsCompileOnly();
	}

	@Override
	public Set<String> getLibClassesCompileOnly() {
		return this.getLibClasses();
	}

	@Override
	public Set<MethodData> getLibMethodsCompileOnly() {
		return this.getLibMethods();
	}

	@Override
	public Set<FieldData> getLibFieldsCompileOnly() {
		return this.getLibFields();
	}

	@Override
	public TestOutput getTestOutput(){
		assert(false); //This has not been implemented yet.
		return null;
	}

	@Override
	public Set<MethodData> getTestMethods(){
		return this.testMethods;
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedTestMethods(){
		return this.usedTestMethods;
	}

	@Override
	public Set<String> getTestClasses(){
		return this.testClasses;
	}

	@Override
	public Set<String> getUsedTestClasses(){
		return this.usedTestClasses;
	}
}
