package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.util.SootUtils;
import jdk.nashorn.internal.codegen.CompilerConstants;
import soot.Scene;

import java.io.*;
import java.util.*;

public class CallGraphAnalysisCacheWrapper implements IProjectAnalyser {

	private CallGraphAnalysis cga = null;
	private final File projectDirectory;
	private final EntryPointProcessor entryPointProcessor;
	private final boolean useSparkInstance;
	private final String projectModule;
	private final boolean useCache;
	private final boolean verbose;
	private final List<File> libJarPath;
	private final List<File> appClassPath;
	private final List<File> appTestPath;
	private boolean hasSetup;
	private boolean hasRun;

	public CallGraphAnalysisCacheWrapper(File projectDir, String module, List<File> libJarPath,
	                                     List<File> appClassPath,
	                                     List<File> appTestPath,
	                                     EntryPointProcessor entryPointProc,
	                                     boolean useSpark, boolean useCache, boolean verbose){
		this.projectDirectory = projectDir;
		this.entryPointProcessor = entryPointProc;
		this.useSparkInstance = useSpark;
		this.projectModule = module;
		this.useCache = useCache;
		this.libJarPath = new ArrayList<File>(libJarPath);
		this.appClassPath = new ArrayList<File>(appClassPath);
		this.appTestPath = new ArrayList<File>(appTestPath);
		this.verbose = verbose;

		Optional<File> cache = this.getCache();

		if(cache.isPresent() && this.useCache){
			if(this.verbose){
				System.out.println("Call graph cache exists (\"" + cache.get().getAbsolutePath() + "\"). Loading...");
			}

			try{
				ObjectInputStream in=new ObjectInputStream(new FileInputStream(getCacheFileLocation()));
				this.cga=(CallGraphAnalysis) in.readObject();
				in.close();
			}catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}

			if(this.verbose){
				System.out.println("Done loading call graph cache.");
			}

			this.cga.setAppClassPath(this.appClassPath);
			this.cga.setAppTestPath(this.appTestPath);
			this.cga.setLibJarPath(this.libJarPath);

			hasSetup = true;
			hasRun = true;
		} else{
			this.cga = new CallGraphAnalysis(libJarPath, appClassPath, appTestPath,entryPointProc, useSpark);
			hasSetup = false;
			hasRun = false;
		}

		assert(this.cga != null);
	}


	/*package*/ Optional<File> getCache(){

		File cacheFile = this.getCacheFileLocation();

		if(cacheFile.exists()){
			return Optional.of(cacheFile);
		}

		return Optional.empty();
	}

	private File getCacheFileLocation(){

		int customEntryPointHashcode = 0;
		for(MethodData methodData : this.entryPointProcessor.getCustomEntry()){
			customEntryPointHashcode += methodData.hashCode();
		}

		int classPathHashcode = 0;
		Set<File> classPaths = new HashSet<File>();
		classPaths.addAll(this.appClassPath);
		classPaths.addAll(this.appTestPath);
		classPaths.addAll(this.libJarPath);
		for(File classPath :  classPaths){
			classPathHashcode += classPath.getName().hashCode();
		}

		File cacheFile = new File(getCacheDirectory(this.projectDirectory).getAbsolutePath()
			+ File.separator + "cga_" + this.projectModule + (this.entryPointProcessor.isMainEntry() ? "_main" : "")
			+ (this.entryPointProcessor.isPublicEntry() ? "_public" : "")
			+ (this.entryPointProcessor.isTestEntry() ? "_test" : "")
			+ (this.useSparkInstance ? "_spark" : "") + "_customEntry" + Integer.toHexString(customEntryPointHashcode)
			+ "_classPath" + Integer.toHexString(classPathHashcode) +".cache");

		return cacheFile;
	}

	public static File getCacheDirectory(File projectDirectory){
		return new File( projectDirectory.getAbsolutePath() + File.separator + "jshrink_caches");
	}

	private void createCache(){
		try{

			File cacheFile = getCacheFileLocation();
			if(cacheFile.exists()){
				cacheFile.delete();
			}

			if(!getCacheDirectory(this.projectDirectory).exists()){
				getCacheDirectory(this.projectDirectory).mkdirs();
			}
			cacheFile.createNewFile();

			FileOutputStream fout = new FileOutputStream(getCacheFileLocation());
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(this.cga);
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void setup() {
		if(!hasSetup){
			cga.setup();
			hasSetup = true;
		}
	}

	@Override
	public void run() {
		if(!hasRun){
			cga.run();
			if(this.useCache) {
				if(this.verbose){
					System.out.println("Storing call graph as a cache...");
				}
				this.createCache();
				if(this.verbose){
					System.out.println("Done storing call graph as cache (\""
							+ this.getCacheFileLocation().getAbsolutePath() + "\")");
				}
			}
			hasRun = true;
		} else {
			SootUtils.setup_trimming(this.libJarPath, this.appClassPath, this.appTestPath);
			Scene.v().loadNecessaryClasses();
		}
	}

	@Override
	public Set<String> getLibClasses() {
		return cga.getLibClasses();
	}

	@Override
	public Set<String> getLibClassesCompileOnly() {
		return cga.getLibClassesCompileOnly();
	}

	@Override
	public Set<MethodData> getLibMethods() {
		return cga.getLibMethods();
	}

	@Override
	public Set<MethodData> getLibMethodsCompileOnly() {
		return cga.getLibMethodsCompileOnly();
	}

	@Override
	public Set<String> getAppClasses() {
		return cga.getAppClasses();
	}

	@Override
	public Set<MethodData> getAppMethods() {
		return cga.getAppMethods();
	}

	@Override
	public Set<String> getUsedLibClasses() {
		return cga.getUsedLibClasses();
	}

	@Override
	public Set<String> getUsedLibClassesCompileOnly() {
		return cga.getUsedLibClassesCompileOnly();
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedLibMethods() {
		return cga.getUsedLibMethods();
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedLibMethodsCompileOnly() {
		return cga.getUsedLibMethodsCompileOnly();
	}

	@Override
	public Set<MethodData> getTestMethods() {
		return cga.getTestMethods();
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedTestMethods() {
		return cga.getUsedTestMethods();
	}

	@Override
	public Set<String> getTestClasses() {
		return cga.getTestClasses();
	}

	@Override
	public Set<String> getUsedTestClasses() {
		return cga.getUsedTestClasses();
	}

	@Override
	public Set<String> getUsedAppClasses() {
		return cga.getUsedAppClasses();
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedAppMethods() {
		return cga.getUsedAppMethods();
	}

	@Override
	public List<File> getAppClasspaths() {
		return cga.getAppClasspaths();
	}

	@Override
	public List<File> getLibClasspaths() {
		return cga.getLibClasspaths();
	}

	@Override
	public List<File> getTestClasspaths() {
		return cga.getTestClasspaths();
	}

	@Override
	public Set<MethodData> getEntryPoints() {
		return cga.getEntryPoints();
	}

	@Override
	public TestOutput getTestOutput() {
		return cga.getTestOutput();
	}

	@Override
	public Set<FieldData> getLibFields() {
		return cga.getLibFields();
	}

	@Override
	public Set<FieldData> getLibFieldsCompileOnly() {
		return cga.getLibFieldsCompileOnly();
	}

	@Override
	public Set<FieldData> getAppFields() {
		return cga.getAppFields();
	}

	@Override
	public Set<FieldData> getUsedLibFields() {
		return cga.getUsedLibFields();
	}

	@Override
	public Set<FieldData> getUsedLibFieldsCompileOnly() {
		return cga.getUsedLibFieldsCompileOnly();
	}

	@Override
	public Set<FieldData> getUsedAppFields() {
		return cga.getUsedAppFields();
	}

	public String getLibPathOfClass(String libClass) {
		return this.cga.getLibPathOfClass(libClass);
	}

	public String getLibPathOfMethod(MethodData methodData) {
		return this.cga.getLibPathOfMethod(methodData);
	}

	public String getLibPathOfField(FieldData fieldData) {
		return cga.getLibPathOfField(fieldData);
	}
}
