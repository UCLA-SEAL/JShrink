package edu.ucla.cs.jshrinklib;

import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapser;
import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapserAnalysis;
import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapserData;
import edu.ucla.cs.jshrinklib.fieldwiper.FieldWiper;
import edu.ucla.cs.jshrinklib.methodinliner.InlineData;
import edu.ucla.cs.jshrinklib.methodinliner.MethodInliner;
import edu.ucla.cs.jshrinklib.methodwiper.MethodWiper;
import edu.ucla.cs.jshrinklib.reachability.*;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.PathResolutionUtil;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import org.apache.commons.io.FileUtils;
import soot.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JShrink {
	private File projectDir;
	private EntryPointProcessor entryPointProcessor;
	private Optional<File> tamiflex;
	private Optional<File> jmtrace;
	private boolean useSpark;
	private boolean verbose;
	private boolean useCache;
	private boolean ignoreLibs;
	private Optional<IProjectAnalyser> projectAnalyser = Optional.empty();
	private boolean projectAnalyserRun = false;
	private Set<SootClass> classesToModify = new HashSet<SootClass>();
	private Set<SootClass> classesToRemove = new HashSet<SootClass>();
	private ClassReferenceGraph classDependencyGraph = null;
	private long libSizeCompressed = -1;
	private long libSizeDecompressed = -1;
	private long appSizeCompressed = -1;
	private long appSizeDecompressed = -1;

	// a global boolean variable indicating whether we should allow
	public static boolean enable_type_dependency = false;
	public static boolean enable_member_visibility = false;
	public static boolean enable_super_class_recursion_check = false;
	public static boolean enable_annotation_updates = false;

	//Map<Class,Exception thrown by Soot>
	private Map<String,String> unmodifiableClasses = new HashMap<String, String>();
	private boolean runTests;
	private Optional<Map<MethodData, Set<MethodData>>> callGraphs = Optional.empty();

	/*
	Regardless of whether "reset()" is run, the project, if compiled, remains so. We do not want to recompile, thus
	we keep note of the compilation status of the project.
	 */
	private boolean alreadyCompiled = false;

	private static Optional<JShrink> instance = Optional.empty();

	/*
	At present this only work on Maven Directories. I should expand this to be more general (some code exists to
	support this but TamiFlex is a big hurdle for now --- we need to run test cases).
	*/

	//TODO: Expand for projects that are not just Maven (see above).

	//TODO: Yet to implement the functionality to ignore certain classes for modification.

	/*
	TODO: We do not assume interaction between remove methods/method inlining/call-graph collapsing.
	E.g., it could be the case that a method is inlined, and we can therefore remove that method to save space. I would
	assume we need to rebuild the callgraph to do so.

	Perhaps we need to implement an "updateCallGraph" feature. At present we need to simply run "updateClassFiles()"
	after inlining/call-graph collapsing to observe these affects. Is there a way to update the call graph in a more
	 efficient way? We don't need to reload the classes to SootClasses, for example.
	 */



	public static JShrink createInstance(File projectDir,
	                                  EntryPointProcessor entryPointProcessor,
	                                  Optional<File> tamiflex, Optional<File> jmtrace,
	                                  boolean useSpark, boolean verbose, boolean executeTests, boolean useCache,
										 boolean ignoreLibs) throws IOException{
		/*
		Due to Soot using a singleton pattern, I use a singleton pattern here to ensure safety.
		E.g., only one project can be worked on at once.
		 */
		if(instance.isPresent()){
			throw new IOException("Instance of JShrink already exists. Please use \"getInstance\".");
		}
		instance = Optional.of(new JShrink(projectDir, entryPointProcessor, tamiflex, jmtrace, useSpark,
				verbose, executeTests, useCache, ignoreLibs));
		return instance.get();
	}

	public static boolean instanceExists(){
		return instance.isPresent();
	}

	public static JShrink getInstance() throws IOException{
		if(instance.isPresent()){
			return instance.get();
		}
		throw new IOException("Instance of JShrink does not exist. Please used \"createInstance\".");
	}

	public static JShrink resetInstance(File projectDir,
	                                    EntryPointProcessor entryPointProcessor,
	                                    Optional<File> tamiflex, Optional<File> jmtrace,
	                                    boolean useSpark, boolean verbose, boolean executeTests,
										boolean useCache, boolean ignoreLibs) throws IOException{
		if(instance.isPresent()){
			instance.get().reset();
			instance.get().projectDir = projectDir;
			instance.get().entryPointProcessor = entryPointProcessor;
			instance.get().tamiflex = tamiflex;
			instance.get().jmtrace = jmtrace;
			instance.get().useSpark = useSpark;
			instance.get().verbose = verbose;
			instance.get().useCache = useCache;
			instance.get().runTests = executeTests;
			instance.get().ignoreLibs = ignoreLibs;
			instance.get().alreadyCompiled = false;
			instance.get().libSizeCompressed = -1;
			instance.get().libSizeDecompressed = -1;
			instance.get().appSizeCompressed = -1;
			instance.get().appSizeDecompressed = -1;
			return instance.get();
		}
		throw new IOException("Instance of JShrink does not exist. Please use \"createInstance\".");
	}

	private JShrink(File projectDir, EntryPointProcessor entryPointProcessor, Optional<File> tamiflex, Optional<File> jmtrace,
	                boolean useSpark, boolean verbose, boolean executeTests, boolean useCache, boolean ignoreLibs){
			this.projectDir = projectDir;
			this.entryPointProcessor = entryPointProcessor;
			this.tamiflex = tamiflex;
			this.jmtrace = jmtrace;
			this.useSpark = useSpark;
			this.verbose = verbose;
			this.runTests = executeTests;
			this.useCache = useCache;
			this.ignoreLibs = ignoreLibs;
			classDependencyGraph = new ClassReferenceGraph();
	}

	public Set<MethodData> getAllEntryPoints() {
		return this.getProjectAnalyserRun().getEntryPoints();
	}

	private IProjectAnalyser getProjectAnalyser(){
		//Will return setup, not guaranteed to have been run. Use "getProjectAnalyserRun" for this.
		if(this.projectAnalyser.isPresent()){
			return this.projectAnalyser.get();
		}

		//Just supporting MavenSingleProjectAnalysis for now
		this.projectAnalyser = Optional.of(
			new MavenSingleProjectAnalyzer(this.projectDir.getAbsolutePath(),
				this.entryPointProcessor, this.tamiflex, this.jmtrace,
				this.useSpark, this.verbose, this.runTests, this.useCache, this.ignoreLibs));

		((MavenSingleProjectAnalyzer) this.projectAnalyser.get()).setCompileProject(!alreadyCompiled);

		this.projectAnalyser.get().setup();
		this.alreadyCompiled = true;
		((MavenSingleProjectAnalyzer) this.projectAnalyser.get()).setCompileProject(!alreadyCompiled);
		updateSizes();

		return this.projectAnalyser.get();
	}

	private void loadClasses(){
		G.reset();
		SootUtils.setup_trimming(this.getProjectAnalyser().getLibClasspaths(),
			this.getProjectAnalyser().getAppClasspaths(), this.getProjectAnalyser().getTestClasspaths());
		Scene.v().loadNecessaryClasses();
	}

	private IProjectAnalyser getProjectAnalyserRun(){
		if(!this.projectAnalyserRun){
			this.getProjectAnalyser().run();
			loadClasses();
			this.projectAnalyserRun = true;
		}
		return this.getProjectAnalyser();
	}

	public Set<MethodData> getAllAppMethods(){
		return this.getProjectAnalyserRun().getAppMethods();
	}

	public Set<MethodData> getAllLibMethods(){
		return this.getProjectAnalyserRun().getLibMethodsCompileOnly();
	}

	public Set<FieldData> getAllAppFields() {
		return this.getProjectAnalyserRun().getAppFields();
	}

	public Set<FieldData> getAllLibFields() {
		return this.getProjectAnalyserRun().getLibFieldsCompileOnly();
	}

	public Set<MethodData> getUsedAppMethods(){
		return this.getProjectAnalyserRun().getUsedAppMethods().keySet();
	}

	public Set<MethodData> getUsedLibMethods(){
		return this.getProjectAnalyserRun().getUsedLibMethodsCompileOnly().keySet();
	}

	public Set<FieldData> getUsedAppFields() {
		return this.getProjectAnalyserRun().getUsedAppFields();
	}

	public Set<FieldData> getUsedLibFields() {
		return this.getProjectAnalyserRun().getUsedLibFieldsCompileOnly();
	}

	public Set<MethodData> getAllTestMethods(){
		return this.getProjectAnalyserRun().getTestMethods();
	}

	public Set<MethodData> getUsedTestMethods(){
		return this.getProjectAnalyserRun().getUsedTestMethods().keySet();
	}

	public Set<String> getAllAppClasses(){
		return this.getProjectAnalyserRun().getAppClasses();
	}

	public Set<String> getAllLibClasses(){
		return this.getProjectAnalyserRun().getLibClassesCompileOnly();
	}

	public Set<String> getUsedAppClasses(){
		return this.getProjectAnalyserRun().getUsedAppClasses();
	}

	public Set<String> getUsedLibClasses(){
		return this.getProjectAnalyserRun().getUsedLibClassesCompileOnly();
	}

	public Set<String> getTestClasses(){
		return this.getProjectAnalyserRun().getTestClasses();
	}

	public Set<String> getUsedTestClasses(){
		return this.getProjectAnalyserRun().getUsedTestClasses();
	}

	public Map<String, String> getUnmodifiableClasses() {
		return this.unmodifiableClasses;
	}

	public ClassCollapserData collapseClasses(boolean collapseAppClasses, boolean collapseLibClasses, boolean removeClasses){
		Set<String> allClasses = new HashSet<String>();
		Set<String> usedClasses = new HashSet<String>();
		Set<MethodData> usedMethods = new HashSet<MethodData>();

		if(collapseAppClasses) {
			allClasses.addAll(this.getAllAppClasses());
			usedClasses.addAll(this.getUsedAppClasses());
			usedMethods.addAll(this.getUsedAppMethods());
		}
		if(collapseLibClasses) {
			allClasses.addAll(this.getAllLibClasses());
			usedClasses.addAll(this.getUsedLibClasses());
			usedMethods.addAll(this.getUsedLibMethods());
		}

		ClassCollapserAnalysis classCollapserAnalysis =
			new ClassCollapserAnalysis(allClasses, usedClasses, usedMethods, this.getSimplifiedCallGraph(),
				this.getAllEntryPoints(), unmodifiableClasses.keySet());
		classCollapserAnalysis.run();
		ClassCollapser classCollapser = new ClassCollapser();
		classCollapser.run(classCollapserAnalysis, this.getTestClasses());

		ClassCollapserData classCollapserData = classCollapser.getClassCollapserData();
		for(String classToRewrite : classCollapserData.getClassesToRewrite()){
			SootClass sootClass = Scene.v().loadClassAndSupport(classToRewrite);
			this.classesToModify.add(sootClass);
		}
		for(String classToRemove : classCollapserData.getClassesToRemove()){
			SootClass sootClass = Scene.v().loadClassAndSupport(classToRemove);
			this.classesToModify.remove(sootClass);
			this.classesToRemove.add(sootClass);
		}

		if(removeClasses) {
			// The class collapsing procedure only removed unused subclasses
			// we can further remove all unused classes
			Set<String> unusedClasses = new HashSet<String>(allClasses);
			unusedClasses.removeAll(usedClasses);
			for(String classToRemove : unusedClasses) {
				// as mentioned in the Jax paper, Jax removes a class if it is unused and if it does not have a derived class
				if(!classCollapserAnalysis.childrenMap.containsKey(classToRemove) ||
						(classCollapserAnalysis.childrenMap.get(classToRemove).isEmpty())) {
					SootClass sootClass = Scene.v().loadClassAndSupport(classToRemove);
					this.classesToModify.remove(sootClass);
					this.classesToRemove.add(sootClass);
				}
			}
		}

		return classCollapserData;
	}

	public Map<MethodData, Set<MethodData>> getSimplifiedCallGraph(){
		if(callGraphs.isPresent()) {
			return callGraphs.get();
		}

		callGraphs = Optional.of(new HashMap<MethodData, Set<MethodData>>());
		callGraphs.get().putAll(this.getProjectAnalyserRun().getUsedAppMethods());
		callGraphs.get().putAll(this.getProjectAnalyserRun().getUsedLibMethodsCompileOnly());
		callGraphs.get().putAll(this.getProjectAnalyserRun().getUsedTestMethods());
		return callGraphs.get();
	}

	public InlineData inlineMethods(boolean inlineAppClassMethods, boolean inlineLibClassMethods){
		Set<String> classesInScope = new HashSet<String>();
		if(inlineAppClassMethods) {
			classesInScope.addAll(this.getAllAppClasses());
		}
		if(inlineLibClassMethods) {
			classesInScope.addAll(this.getAllLibClasses());
		}
		classesInScope.addAll(this.getUsedTestClasses());
		Map<MethodData, Set<MethodData>> simplifiedCallGraph = new HashMap<MethodData, Set<MethodData>>();
		Map<MethodData, Set<MethodData>> originalCallGraph = this.getSimplifiedCallGraph();
		for(MethodData md : originalCallGraph.keySet()) {
			if(classesInScope.contains(md.getClassName())) {
				simplifiedCallGraph.put(md,  originalCallGraph.get(md));
			}
		}

		Map<SootMethod, Set<SootMethod>> callgraph = new HashMap<SootMethod, Set<SootMethod>>();
		for(Map.Entry<SootMethod, Set<SootMethod>> entry :
			SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(simplifiedCallGraph).entrySet()){
			callgraph.put(entry.getKey(), entry.getValue());
		}

		Set<File> classPaths = new HashSet<File>();

		classPaths.addAll(this.getProjectAnalyser().getAppClasspaths());
		classPaths.addAll(this.getProjectAnalyser().getLibClasspaths());

		InlineData output = MethodInliner.inlineMethods(callgraph, classPaths, unmodifiableClasses.keySet());
		this.classesToModify.addAll(output.getClassesModified());

		return output;
	}

	public Set<MethodData> removeMethods(Set<MethodData> toRemove, boolean removeUnusedClasses){
		Set<MethodData> removedMethods = new HashSet<MethodData>();

		if(removeUnusedClasses) {
			Set<SootClass> sootClassesAffected = new HashSet<SootClass>();
			for (MethodData methodData : toRemove) {
				sootClassesAffected.add(Scene.v().loadClassAndSupport(methodData.getClassName()));
			}

			for (SootClass sootClass : sootClassesAffected) {
				boolean removeClass = true;
				for (SootField sootField : sootClass.getFields()) {
					if (sootField.isStatic() && !sootField.isPrivate()) {
						removeClass = false;
						break;
					}
				}
				if (!removeClass) {
					continue;
				}

				for (SootMethod sootMethod : sootClass.getMethods()) {
					MethodData methodData = SootUtils.sootMethodToMethodData(sootMethod);
					if (!toRemove.contains(methodData)) {
						removeClass = false;
						break;
					}
				}

				if (removeClass) {
					this.classesToRemove.add(sootClass);
					for (SootMethod sootMethod : sootClass.getMethods()) {
						MethodData methodData = SootUtils.sootMethodToMethodData(sootMethod);
						removedMethods.add(methodData);
					}
				}
			}
		}

		//Remove the classes and not the classes affected.
		for(MethodData methodData : toRemove){
			if(!removedMethods.contains(methodData)) {
				if(unmodifiableClasses.containsKey(methodData.getClassName())) {
					// this class cannot be modified by Soot
					continue;
				}
				SootClass sootClass = Scene.v().loadClassAndSupport(methodData.getClassName());
				if (!sootClass.isEnum() && sootClass.declaresMethod(methodData.getSubSignature())) {
					SootMethod sootMethod = sootClass.getMethod(methodData.getSubSignature());
					if (MethodWiper.removeMethod(sootMethod)) {
						removedMethods.add(methodData);
						this.classesToModify.add(sootClass);
					}
				}
			}
		}

		return removedMethods;
	}

	public Set<MethodData> wipeMethods(Set<MethodData> toRemove){
		Set<MethodData> removedMethods = new HashSet<MethodData>();
		for(MethodData methodData : toRemove){
			SootClass sootClass = Scene.v().loadClassAndSupport(methodData.getClassName());
			if(!sootClass.isEnum() && sootClass.declaresMethod(methodData.getSubSignature())) {
				SootMethod sootMethod = sootClass.getMethod(methodData.getSubSignature());
				if(MethodWiper.wipeMethodBody(sootMethod)) {
					removedMethods.add(methodData);
					this.classesToModify.add(sootClass);
				}
			}
		}
		return removedMethods;
	}

	public Set<MethodData> wipeMethodAndAddException(Set<MethodData> toRemove, Optional<String> exceptionMethod){
		Set<MethodData> removedMethods = new HashSet<MethodData>();
		for(MethodData methodData : toRemove){
			SootClass sootClass = Scene.v().loadClassAndSupport(methodData.getClassName());
			if(!sootClass.isEnum() && sootClass.declaresMethod(methodData.getSubSignature())) {
				SootMethod sootMethod = sootClass.getMethod(methodData.getSubSignature());
				boolean success = false;
				if (exceptionMethod.isPresent()) {
					success = MethodWiper.wipeMethodBodyAndInsertRuntimeException(sootMethod, exceptionMethod.get());
				} else {
					success = MethodWiper.wipeMethodBodyAndInsertRuntimeException(sootMethod);
				}
				if (success) {
					removedMethods.add(methodData);
					this.classesToModify.add(sootClass);
				}
			}
		}
		return removedMethods;
	}


	public void removeClasses(Set<String> classes){
		loadClasses();
		for(String className : classes){
			SootClass sootClass = Scene.v().loadClassAndSupport(className);
			this.classesToRemove.add(sootClass);
			this.classesToModify.remove(sootClass);
		}
	}

	public Set<File> getClassPaths(){
		Set<File> classPaths = new HashSet<File>();

		classPaths.addAll(this.getProjectAnalyser().getAppClasspaths());
		classPaths.addAll(this.getProjectAnalyser().getLibClasspaths());
		classPaths.addAll(this.getProjectAnalyser().getTestClasspaths());

		return classPaths;
	}
	public void updateClassFilesAtPath(Set<File> classPaths){
		try {
			Set<File> decompressedJars =
					new HashSet<File>(ClassFileUtils.extractJars(new ArrayList<File>(classPaths)));
			modifyClasses(this.classesToModify, classPaths);
			this.removeClasses(this.classesToRemove, classPaths);
			/*
			File.delete() does not delete a file immediately. I was therefore running into a problem where the jars
			were being recompressed with the files that were supposed to be deleted. I found adding a small delay
			solved this problem. However, it would be good to find a better solution to this problem.
			TODO: Fix the above.
			 */
			TimeUnit.SECONDS.sleep(1);
			ClassFileUtils.compressJars(decompressedJars);
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	public void updateClassFiles(){
		try {
			Set<File> classPaths = this.getClassPaths();
			Set<File> decompressedJars =
				new HashSet<File>(ClassFileUtils.extractJars(new ArrayList<File>(classPaths)));

			modifyClasses(this.classesToModify, classPaths);
			this.classesToModify.clear();

			this.removeClasses(this.classesToRemove, classPaths);
			/*
			File.delete() does not delete a file immediately. I was therefore running into a problem where the jars
			were being recompressed with the files that were supposed to be deleted. I found adding a small delay
			solved this problem. However, it would be good to find a better solution to this problem.
			TODO: Fix the above.
			 */
			TimeUnit.SECONDS.sleep(1);
			this.classesToRemove.clear();
            //modifyClasses(this.classesToModify, classPaths);
			ClassFileUtils.compressJars(decompressedJars);

			//this.classesToModify.clear();
			updateSizes();
			this.reset();
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void reset(){
		this.projectAnalyser = Optional.empty();
		this.projectAnalyserRun = false;
		this.classesToModify.clear();
		this.classesToRemove.clear();
		this.callGraphs = Optional.empty();
		G.reset();
	}

	private long getSize(boolean withJarsDecompressed, List<File> classPaths){
		Set<File> decompressedJars = new HashSet<File>();
		long toReturn = 0;
		try {
			if(withJarsDecompressed){
				decompressedJars =
					new HashSet<File>(ClassFileUtils.extractJars(new ArrayList<File>(classPaths)));
			}

			for(File file : classPaths){
				toReturn+=ClassFileUtils.getSize(file);
			}

			ClassFileUtils.compressJars(decompressedJars);
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		return toReturn;
	}

	private void updateSizes(){
		this.libSizeCompressed = getSize(false, this.getProjectAnalyser().getLibClasspaths());
		this.libSizeDecompressed = getSize(true, this.getProjectAnalyser().getLibClasspaths());
		this.appSizeCompressed = getSize(false, this.getProjectAnalyser().getAppClasspaths());
		this.appSizeDecompressed = getSize(true, this.getProjectAnalyser().getAppClasspaths());
	}

	private void checkSizes(){
//		if(this.libSizeCompressed < 0 || this.libSizeDecompressed < 0
//			|| this.appSizeCompressed < 0 || this.appSizeDecompressed < 0){
//			updateSizes();
//		}
		updateSizes();
	}

	public long getLibSize(boolean withJarsDecompressed){
		checkSizes();
		if(withJarsDecompressed){
			return this.libSizeDecompressed;
		} else {
			return this.libSizeCompressed;
		}
	}

	public long getAppSize(boolean withJarsDecompressed){
		checkSizes();
		if(withJarsDecompressed){
			return this.appSizeDecompressed;
		} else {
			return this.appSizeCompressed;
		}
	}

	public TestOutput getTestOutput(){
		assert(this.runTests);
		return this.getProjectAnalyser().getTestOutput();
	}

	/*
	This method does a Soot pass. Soot can make classes smaller even without any transformations. Thus, this allows
	us to do a pass at the beginning of the run to optimise all the code with soot before doing so with transformations
	(to obtain a good ground truth).

	This must be run before any transformations as it may cause problems later.
	 */
	public void makeSootPass(){
		Set<File> classPaths = getClassPaths();
		Set<SootClass> classesToRewrite = new HashSet<SootClass>();
		for(String className : this.getProjectAnalyserRun().getAppClasses()){
			SootClass sootClass = Scene.v().loadClassAndSupport(className);
			if(!SootUtils.modifiableSootClass(sootClass)){
				Optional<String> exceptionMessage = SootUtils.getUnmodifiableClassException(sootClass);
				assert(exceptionMessage.isPresent());
				unmodifiableClasses.put(className, exceptionMessage.get());
				continue;
			}
			classesToRewrite.add(sootClass);
		}
		for(String className : this.getProjectAnalyserRun().getLibClassesCompileOnly()){
			SootClass sootClass = Scene.v().loadClassAndSupport(className);
			if(!SootUtils.modifiableSootClass(sootClass)){
				Optional<String> exceptionMessage = SootUtils.getUnmodifiableClassException(sootClass);
				assert(exceptionMessage.isPresent());
				unmodifiableClasses.put(className, exceptionMessage.get());
				continue;
			}
			classesToRewrite.add(sootClass);
		}

		/*for(String className: this.getProjectAnalyserRun().getLibClasses()){
			SootClass sootClass = Scene.v().loadClassAndSupport(className);
			if(!SootUtils.modifiableSootClass(sootClass)){
				Optional<String> exceptionMessage = SootUtils.getUnmodifiableClassException(sootClass);
				assert(exceptionMessage.isPresent());
				unmodifiableClasses.put(className, exceptionMessage.get());
				continue;
			}
			this.classDependencyGraph.addClass(sootClass);
		}*/
		// We need to update class name references in test classes in class collapsing
		// So we need to make sure they are modifiable.
		// I saw a case in the disunity project where a test class has lambda expressions which
		// crashes the write-out process
		for(String className : this.getProjectAnalyserRun().getTestClasses()) {
			SootClass sootClass = Scene.v().loadClassAndSupport(className);
			if(!SootUtils.modifiableSootClass(sootClass)){
				Optional<String> exceptionMessage = SootUtils.getUnmodifiableClassException(sootClass);
				assert(exceptionMessage.isPresent());
				unmodifiableClasses.put(className, exceptionMessage.get());
			}
			// no need to rewrite since we do not measure the size of test code
//			classesToRewrite.add(sootClass);
		}
		try {
			Set<File> decompressedJars =
				new HashSet<File>(ClassFileUtils.extractJars(new ArrayList<File>(classPaths)));
			modifyClasses(classesToRewrite, classPaths);
			ClassFileUtils.compressJars(decompressedJars);
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		updateSizes();

		long appSizeBefore = this.getAppSize(true);
		long libSizeBefore = this.getLibSize(true);

		//Run setup again to return the tests (They may have been corrupted by the Soot class).
		this.getProjectAnalyser().setup();

		long appSizeAfter = this.getAppSize(true);
		long libSizeAfter = this.getLibSize(true);

		if(appSizeAfter != appSizeBefore){
			System.out.println("WARNING: App Size Differs before and after running 'setup'");
		}

		if(libSizeAfter != libSizeBefore){
			System.out.println("WARNING: Lib Size Differs before and after running 'setup'");
		}
	}

	/*
	I basically just use these (the following two methods) for debugging purposes. They keep track of classes that will
	be modified and deleted upon execution of "updateClassFiles()".
	 */
	public Set<String> classesToModified(){
		Set<String> toReturn = new HashSet<String>();
		for(SootClass sootClass : this.classesToModify){
			toReturn.add(sootClass.getName());
		}
		return Collections.unmodifiableSet(toReturn);
	}

	public Set<String> classesToRemove(){
		Set<String> toReturn = new HashSet<String>();
		for(SootClass sootClass : this.classesToRemove){
			toReturn.add(sootClass.getName());
		}
		return Collections.unmodifiableSet(toReturn);
	}

	private void modifyClasses(Set<SootClass> classesToRewrite, Set<File> classPaths){
		for (SootClass sootClass : classesToRewrite) {
			try {
				if(unmodifiableClasses.containsKey(sootClass.getName())) {
					if(verbose) {
						// we will not update the class since it will cause exceptions when writing out to bytecode based on
						// the first soot pass. But this may cause a problem when loading or running the unmodified class.
						System.out.println("Attempting to update an unmodifiable class " + sootClass.getName());
					}
				} else {
					ClassFileUtils.writeClass(sootClass, classPaths);
				}
			} catch (IOException e) {
				System.err.println("An exception was thrown when attempting to rewrite a class:");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public Set<String> filterUnmodifiableClass() {
		HashSet<SootClass> unmodifiableClasses = new HashSet<SootClass>();
		HashSet<String> classNameOnly = new HashSet<String>();
		for(SootClass sootClass : this.classesToModify) {
			if(!SootUtils.modifiableSootClass(sootClass)) {
				unmodifiableClasses.add(sootClass);
				classNameOnly.add(sootClass.getName());
			}
		}

		this.classesToModify.removeAll(unmodifiableClasses);

		return classNameOnly;
	}

	private void removeClasses(Set<SootClass> classesToRemove, Set<File> classPaths){
		if(classesToRemove.size() == 0)
			return;

		Set<String> classesToBeRemoved = new HashSet<String>();
		if(JShrink.enable_type_dependency) {
			Instant start = Instant.now();
			PathResolutionUtil.buildMap(classPaths);
			classesToBeRemoved = classesToRemove.stream().map(x->x.getName()).collect(Collectors.toSet());
			for(String className : this.getProjectAnalyser().getAppClasses()){
				SootClass sootClass = Scene.v().getSootClass(className);
				this.classDependencyGraph.addClass(sootClass.getName(), PathResolutionUtil.getClassPath(sootClass.getName()));
			}
			for(String className : this.getProjectAnalyser().getLibClassesCompileOnly()){
				SootClass sootClass = Scene.v().getSootClass(className);
				this.classDependencyGraph.addClass(sootClass.getName(), PathResolutionUtil.getClassPath(sootClass.getName()));
			}
			for(String className : this.getProjectAnalyser().getTestClasses()){
				SootClass sootClass = Scene.v().getSootClass(className);
				this.classDependencyGraph.addClass(sootClass.getName(), PathResolutionUtil.getClassPath(sootClass.getName()));
			}
			if(this.verbose)
				System.out.println("Resolved dependencies in "+Duration.between(Instant.now(),start).getSeconds());
		}

		for(SootClass sootClass : classesToRemove){
			Set<String> referencedBy = this.classDependencyGraph.getReferencedBy(sootClass.getName());
			//not including classes marked for deletion
			referencedBy.removeAll(classesToBeRemoved);
			try{
				if(referencedBy.size()>0)
				{
					if(unmodifiableClasses.containsKey(sootClass.getName()) || sootClass.isAbstract()) {
						// do not remove things in an unmodifiable class since the class cannot be updated anyway
						continue;
					}

					Set<SootField> fieldsToRemove = new HashSet<SootField>(sootClass.getFields());
					for(SootField toRemove : fieldsToRemove){
						toRemove.setDeclared(true);
						toRemove.setDeclaringClass(sootClass);
						sootClass.removeField(toRemove);
					}

					Set<SootMethod> methodsToRemove = new HashSet<SootMethod>(sootClass.getMethods());
					for(SootMethod toRemove : methodsToRemove){
						sootClass.removeMethod(toRemove);
					}

					methodsToRemove.clear();
					fieldsToRemove.clear();
					
					ClassFileUtils.writeClass(sootClass, classPaths);
				}else{
					ClassFileUtils.removeClass(sootClass, classPaths);
				}
			} catch (IOException e){
				System.err.println("An exception was thrown when attempting to delete a class:");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public Set<FieldData> removeFields(Set<FieldData> toRemove) {
		Set<FieldData> removedFields = new HashSet<FieldData>();

		// cluster fields based on their owner classes, so we only need to load each class once in Soot
		Map<String, Set<FieldData>>  toRemoveByClassName = new HashMap<String, Set<FieldData>>();
		for(FieldData field : toRemove) {
			String className = field.getClassName();
			Set<FieldData> set;
			if(toRemoveByClassName.containsKey(className)) {
				set = toRemoveByClassName.get(className);
			} else {
				set = new HashSet<FieldData>();
			}
			set.add(field);
			toRemoveByClassName.put(className, set);
		}

		// modify each Soot class
		for(String className : toRemoveByClassName.keySet()) {
			if(unmodifiableClasses.containsKey(className)) {
				// do not remove a field in an unmodifiable class since the class cannot be updated anyway
				continue;
			}
			SootClass sootClass = Scene.v().getSootClass(className);
			Set<FieldData> unusedFields = toRemoveByClassName.get(className);
			for(FieldData unusedField : unusedFields) {
				SootField sootField = null;
				for(SootField field : sootClass.getFields()) {
					// comment out the check on serialVersionUID since Tamiflex can detect it
//					if(field.getName().equals("serialVersionUID")) {
//						// keep this field since JVM needs this field for serialization and validating serialized objects
//						continue;
//					}
					if(field.getName().equals(unusedField.getName()) && field.getType().toString()
						.equals(unusedField.getType())) {
						sootField = field;
						break;
					}
				}

				if(sootField != null && FieldWiper.removeField(sootField, verbose)) {
					removedFields.add(unusedField);
					this.classesToModify.add(sootClass);
				}
			}
		}

		return removedFields;
	}
	public String getLog(){
		return ((MavenSingleProjectAnalyzer)getProjectAnalyser()).getLog();
	}
}
