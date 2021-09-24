package edu.ucla.cs.jshrinklib.reachability;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import org.apache.commons.io.FileUtils;

import soot.G;
import edu.ucla.cs.jshrinklib.util.MavenUtils;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * 
 * This class runs reachability analysis on a single maven project and 
 * handles submodules if any
 * 
 * It is only designed for the experiment purpose. Make sure to do two
 * things before running it
 * 
 * 	1. The maven project is successfully built
 * 
 *  2. The classpath(s) of the maven project are successfully resolved
 *     and logged into a 'onr_classpath_new.log' file in the root dir.
 *     If you haven't done this, run resolve_classpath.sh in the script 
 *     folder first
 * 
 * @author Tianyi Zhang
 *
 */
public class MavenSingleProjectAnalyzer implements IProjectAnalyser {
	
	private String project_path;
	
	private final Set<String> libClasses;
	private final Set<String> libClassesCompileOnly;
	private final Set<MethodData> libMethods;
	private final Set<MethodData> libMethodsCompileOnly;
	private final Set<FieldData> libFields;
	private final Set<FieldData> libFieldsCompileOnly;
	private final Set<String> appClasses;
	private final Set<MethodData> appMethods;
	private final Set<FieldData> appFields;
	private final Set<String> usedLibClasses;
	private final Set<String> usedLibClassesCompileOnly;
	private final Map<MethodData, Set<MethodData>> usedLibMethods;
	private final Map<MethodData, Set<MethodData>> usedLibMethodsCompileOnly;
	private final Set<FieldData> usedLibFields;
	private final Set<FieldData> usedLibFieldsCompileOnly;
	private final Set<String> usedAppClasses;
	private final Map<MethodData, Set<MethodData>> usedAppMethods;
	private final Set<FieldData> usedAppFields;
	private final Set<MethodData> testMethods;
	private final Map<MethodData, Set<MethodData>> usedTestMethods;
	private final Set<String> testClasses;
	private final Set<String> usedTestClasses;
	private final Map<String,List<File>> app_class_paths;
	private final Map<String,List<File>> app_test_paths;
	private final Map<String,List<File>> lib_class_paths;
	private final HashMap<String, String> classpaths;
	private final HashMap<String, String> classpaths_compile_only;
	private final EntryPointProcessor entryPointProcessor;
	private final Set<MethodData> entryPoints;
	private final Optional<File> tamiFlexJar;
	private final Optional<File> jmtrace;
	private final boolean useSpark;
	private final boolean verbose;
	private TestOutput testOutput;
	private boolean compileProject = true;
	private final boolean runTests;
	private final boolean useCache;
	private final StringBuilder log;
	private final boolean ignoreLibs;

	public MavenSingleProjectAnalyzer(String pathToMavenProject, EntryPointProcessor entryPointProc,
									  Optional<File> tamiFlex, Optional<File> jmtrace,
	                                  boolean useSpark, boolean verbose, boolean executeTests, boolean useCache,
									  boolean ignoreLibs) {
		project_path = pathToMavenProject;

		log = new StringBuilder();
		libClasses = new HashSet<String>();
		libClassesCompileOnly = new HashSet<String>();
		libMethods = new HashSet<MethodData>();
		libMethodsCompileOnly = new HashSet<MethodData>();
		libFields = new HashSet<FieldData>();
		libFieldsCompileOnly = new HashSet<FieldData>();
		appClasses = new HashSet<String>();
		appMethods = new HashSet<MethodData>();
		appFields = new HashSet<FieldData>();
		usedLibClasses = new HashSet<String>();
		usedLibClassesCompileOnly = new HashSet<String>();
		usedLibMethods = new HashMap<MethodData, Set<MethodData>>();
		usedLibMethodsCompileOnly = new HashMap<MethodData,Set<MethodData>>();
		usedLibFields = new HashSet<FieldData>();
		usedLibFieldsCompileOnly = new HashSet<FieldData>();
		usedAppClasses = new HashSet<String>();
		usedAppMethods = new HashMap<MethodData, Set<MethodData>>();
		usedAppFields = new HashSet<FieldData>();
		app_class_paths = new HashMap<String, List<File>>();
		app_test_paths = new HashMap<String, List<File>>();
		lib_class_paths = new HashMap<String, List<File>>();
		testMethods = new HashSet<MethodData>();
		usedTestMethods = new HashMap<MethodData, Set<MethodData>>();
		testClasses = new HashSet<String>();
		usedTestClasses = new HashSet<String>();
		classpaths = new HashMap<String, String>();
		classpaths_compile_only = new HashMap<String, String>();
		entryPointProcessor = entryPointProc;
		entryPoints = new HashSet<MethodData>();
		tamiFlexJar = tamiFlex;
		this.jmtrace = jmtrace;
		this.useSpark = useSpark;
		this.verbose = verbose;
		this.runTests = executeTests;
		this.useCache = useCache;
		this.ignoreLibs = ignoreLibs;

		// initialize a dummy test output object instead of assigning a null value
		// if the test is never run due to a compilation error in a build process, the test output object will remain dummy
		//... also if this.run-tests == false
		testOutput = new TestOutput(-1, -1, -1, -1, "", true);
	}

	public void setCompileProject(boolean compileProject) {
		this.compileProject = compileProject;
	}

	public void cleanup(){
		//This is just used for cleaning up after testing. It just runs "mvn clean"
		File pomFile = new File(project_path + File.separator + "pom.xml");
		File libsDir = new File(project_path + File.separator + "libs");

		try{
			Process process = Runtime.getRuntime().exec("mvn -f " + pomFile.getAbsolutePath() + " " +
					"clean");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String maven_log = "";
			String line;
			while((line=reader.readLine()) != null) {
				maven_log += line + System.lineSeparator();
			}
			reader.close();
			
			if(maven_log.contains("BUILD FAILURE")) {
				System.err.println("'mvn clean' fails.");
			}

			try {
				FileUtils.forceDelete(libsDir);
			}catch(FileNotFoundException e){
				//Do nothing
			}

		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void setup(){
		File root_dir = new File(project_path);

		// find all submodules if any
		HashMap<String, File> modules = new HashMap<String, File>();
		MavenUtils.getModules(root_dir, modules);

		// get all classpaths for submodules if any
		String classpathInfo = "";
		String classpathInfo_compile_only = "";
		try {
			 File pomFile = new File(root_dir + File.separator + "pom.xml");
			 File libsDir = new File(root_dir + File.separator + "libs");

			 String[] cmd;
			 ProcessBuilder processBuilder;
			 Process process;
			 InputStream stdout;
			 InputStreamReader isr;
			 BufferedReader br;
			 String line;
			 int exitValue;
			 if(this.compileProject) {
			 	if(this.verbose){
			 		System.out.println("Compiling project...");
			    }
				 // Ensure the project is compiled.
				 // Prepare the command and its arguments in a String array in case there is a space or special
				 // character in the pom file path or lib dir path.
				 cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "install",
					 "-Dmaven.repo.local=" + libsDir.getAbsolutePath(),
					 "--quiet",
					 "--batch-mode",
					 "-DskipTests=true"};
				 processBuilder = new ProcessBuilder(cmd);
				 processBuilder.redirectErrorStream(true);
				 process = processBuilder.start();
				 stdout = process.getInputStream();
				 isr = new InputStreamReader(stdout);
				 br = new BufferedReader(isr);

				 String output = "";
				 while ((line = br.readLine()) != null) {
				 	output += line + System.lineSeparator();
				 }
				 br.close();

				 exitValue = process.waitFor();

				 if (exitValue != 0) {
					 throw new IOException("Build failed! Output the following " + System.lineSeparator() + output);
				 }
				 if(this.verbose){
					 System.out.println("Done compiling project!");
				 }
			 }

			 if(this.runTests) {
				 if (this.verbose) {
					 System.out.println("Running project tests...");
				 }

				 cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "surefire:test",
					 "-Dmaven.repo.local=" + libsDir.getAbsolutePath(), "--batch-mode", "-fn"};
				 processBuilder = new ProcessBuilder(cmd);
				 processBuilder.redirectErrorStream(true);
				 process = processBuilder.start();
				 stdout = process.getInputStream();
				 isr = new InputStreamReader(stdout);
				 br = new BufferedReader(isr);

				 String maven_log = "";
				 while ((line = br.readLine()) != null) {
					 maven_log += line + System.lineSeparator();
				 }
				 br.close();

				 exitValue = process.waitFor();

				 // still get test output even in case of test failure
				 this.testOutput = MavenUtils.testOutputFromString(maven_log);

				 if (this.verbose) {
					 System.out.println("Done running project tests!");
				 }
			 }

			if(this.verbose){
				System.out.println("Getting dependency information...");
			}
			// first get the full classpath (compile scope + test scope) so that we will get a more complete
			// call graph in the static analysis later
			cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "dependency:build-classpath",
				"-Dmaven.repo.local=" + libsDir.getAbsolutePath(),  "--batch-mode"};
			processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			process = processBuilder.start();
			stdout = process.getInputStream();
			isr = new InputStreamReader(stdout);
			br = new BufferedReader(isr);

			while((line=br.readLine()) != null) {
				classpathInfo += line + System.lineSeparator();
			}
			br.close();

			exitValue = process.waitFor();
			
			if(exitValue != 0) {
				throw new IOException("Cannot get dependency information! The following was output:"
					+ System.lineSeparator() + classpathInfo);
			}

			if(this.verbose){
				System.out.println("Done getting dependency information!");
			}

			if(this.verbose){
				System.out.println("Getting compile scope dependency information...");
			}
			
			// then get the classpath of the compile scope only for the future method removal
			cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "dependency:build-classpath",
				"-Dmaven.repo.local=" + libsDir.getAbsolutePath(), "-DincludeScope=compile", "--batch-mode"};
			processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			process = processBuilder.start();
			stdout = process.getInputStream();
			isr = new InputStreamReader(stdout);
			br = new BufferedReader(isr);

			while((line = br.readLine()) != null) {
				classpathInfo_compile_only += line + System.lineSeparator();
			}
			br.close();

			exitValue = process.waitFor();

			if(exitValue != 0) {
				throw new IOException("Cannot get dependency information for compile scope! The following was output:"
					+ System.lineSeparator() + classpathInfo_compile_only);
			}

			if(this.verbose){
				System.out.println("Done getting compile scope dependency information!");
			}

		}catch(IOException | InterruptedException e){
			e.printStackTrace();
			//TODO: is it really good to handle this here? Should we maybe throw the error further up?
			System.exit(1);
		}


		classpaths.putAll(MavenUtils.getClasspaths(classpathInfo));
		classpaths_compile_only.putAll(MavenUtils.getClasspaths(classpathInfo_compile_only));

		for(String artifact_id : modules.keySet()) {
			// Note that not all submodules are built
			if(classpaths.containsKey(artifact_id)) {

				if(this.verbose){
					System.out.println("Getting classpath information for module \"" + artifact_id +"\"...");
				}

				String cp = classpaths.get(artifact_id);
				File dir = modules.get(artifact_id);

				// make sure there are src and target directories in this submodule
				File srcDir =
						new File(dir.getAbsolutePath() + File.separator + "src");
				File targetDir =
						new File(dir.getAbsolutePath() + File.separator + "target");
				if(!srcDir.exists() || !targetDir.exists()) {
					//This is sometimes the case, I don't think it's anything to worry about.
					continue;
				}

				// initialize the arguments for CallGraphAnalaysis
				File app_class_path =
						new File(dir.getAbsolutePath() + File.separator + "target/classes");
				if(app_class_path.exists()) {
					app_class_paths.put(artifact_id, Arrays.asList(app_class_path));
				}

				File app_test_path =
						new File(dir.getAbsolutePath() + File.separator + "target/test-classes");
				if(app_test_path.exists()) {
					app_test_paths.put(artifact_id, Arrays.asList(app_test_path));
				}

				if(!this.ignoreLibs) {
					String[] cps = cp.split(File.pathSeparator);
					lib_class_paths.put(artifact_id, new ArrayList<File>());
					for (String path : cps) {
						File pathFile = new File(path);
						if (!path.isEmpty() && pathFile.exists() && ClassFileUtils.directoryContains(root_dir, pathFile)
								//I only consider .class and .jar files as valid libraries
								&& (pathFile.getAbsolutePath().endsWith(".class")
								|| pathFile.getAbsolutePath().endsWith(".jar"))) {
							lib_class_paths.get(artifact_id).add(new File(path));
						}
					}
				}
				if(this.verbose){
					System.out.println("Done getting classpath information for module \"" + artifact_id +"\"!");
				}

			}
		}
	}

	private static void addToMap(Map<MethodData, Set<MethodData>> map, MethodData key, Collection<MethodData> elements){
		if(!map.containsKey(key)){
			map.put(key, new HashSet<MethodData>());
		}
		for(MethodData element : elements){
			map.get(key).add(element);
		}
	}

	private static void addToMap(Map<MethodData,Set<MethodData>> map, Map<MethodData, Set<MethodData>> toAdd){
		for(Map.Entry<MethodData, Set<MethodData>> entry : toAdd.entrySet()){
			addToMap(map, entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void run() {
		File root_dir = new File(project_path);
		
		// find all submodules if any
		HashMap<String, File> modules = new HashMap<String, File>();
		MavenUtils.getModules(root_dir, modules);

		int count = 0;
		long callgraphTimeSeconds = 0;
		for(String artifact_id : modules.keySet()) {
			// Note that not all submodules are built
			if(classpaths.containsKey(artifact_id)) {

				if(this.verbose){
					System.out.println("Running callgraph analysis for module \"" + artifact_id +"\"...");
				}
				
				// increment the count of analyzed modules
				count++;

				List<File> localLibClassPaths =
					(lib_class_paths.containsKey(artifact_id) ? lib_class_paths.get(artifact_id) : new ArrayList<File>());

				List<File> localAppClassPaths =
					(app_class_paths.containsKey(artifact_id) ? app_class_paths.get(artifact_id) : new ArrayList<File>());

				List<File> localTestClassPaths =
					(app_test_paths.containsKey(artifact_id) ? app_test_paths.get(artifact_id) : new ArrayList<File>());

				Instant callgraphStart = Instant.now();

				CallGraphAnalysisCacheWrapper runner =
						new CallGraphAnalysisCacheWrapper(new File(project_path), artifact_id, localLibClassPaths,
							localAppClassPaths, localTestClassPaths, entryPointProcessor,
								this.useSpark, this.useCache, this.verbose);
				runner.setup();
				runner.run();

				callgraphTimeSeconds += Duration.between(callgraphStart, Instant.now()).getSeconds();

				//this.callgraphs.addAll(runner.getCallGraphs());
				
				String cp_compile_only = classpaths_compile_only.get(artifact_id);
				HashSet<String> compile_lib_paths = 
						new HashSet<String>(Arrays.asList(cp_compile_only.split(File.pathSeparator)));
				// aggregate the analysis result of the submodule
				for(String libClass : runner.getLibClasses()) {
					this.libClasses.add(libClass);
					String lib_path = runner.getLibPathOfClass(libClass);
					if(compile_lib_paths.contains(lib_path)) {
						this.libClassesCompileOnly.add(libClass);
					}
				}
				for(MethodData libMethod : runner.getLibMethods()) {
					this.libMethods.add(libMethod);
					String lib_path = runner.getLibPathOfMethod(libMethod);
					if(compile_lib_paths.contains(lib_path)) {
						this.libMethodsCompileOnly.add(libMethod);
					}
				}
				for(FieldData libField : runner.getLibFields()) {
					this.libFields.add(libField);
					String lib_path = runner.getLibPathOfField(libField);
					if(compile_lib_paths.contains(lib_path)) {
						this.libFieldsCompileOnly.add(libField);
					}
				}
				for(String usedLibClass : runner.getUsedLibClasses()) {
					this.usedLibClasses.add(usedLibClass);
					String lib_path = runner.getLibPathOfClass(usedLibClass);
					if(compile_lib_paths.contains(lib_path)) {
						this.usedLibClassesCompileOnly.add(usedLibClass);
					}
				}
				for(Map.Entry<MethodData,Set<MethodData>> libMethod : runner.getUsedLibMethods().entrySet()) {
					addToMap(this.usedLibMethods, libMethod.getKey(),libMethod.getValue());
					String lib_path = runner.getLibPathOfMethod(libMethod.getKey());
					if(compile_lib_paths.contains(lib_path)) {
						addToMap(this.usedLibMethodsCompileOnly, libMethod.getKey(), libMethod.getValue());
					}
				}
				for(FieldData usedLibField : runner.getUsedLibFields()) {
					this.usedLibFields.add(usedLibField);
					String lib_path = runner.getLibPathOfField(usedLibField);
					if(compile_lib_paths.contains(lib_path)) {
						this.usedLibFieldsCompileOnly.add(usedLibField);
					}
				}

				this.appClasses.addAll(runner.getAppClasses());
				this.appMethods.addAll(runner.getAppMethods());
				this.appFields.addAll(runner.getAppFields());
				this.usedAppClasses.addAll(runner.getUsedAppClasses());
				addToMap(this.usedAppMethods, runner.getUsedAppMethods());
				this.usedAppFields.addAll(runner.getUsedAppFields());

				this.testClasses.addAll(runner.getTestClasses());
				this.testMethods.addAll(runner.getTestMethods());
				this.usedTestClasses.addAll(runner.getUsedTestClasses());
				addToMap(this.usedTestMethods, runner.getUsedTestMethods());

				this.entryPoints.addAll(runner.getEntryPoints());
				
				// make sure to reset Soot after running reachability analysis
				G.reset();

				if(this.verbose){
					System.out.println("Done running callgraph analysis for module \"" + artifact_id +"\"!");
				}
			}
		}

		log.append("static_analysis_time," + Long.toString(callgraphTimeSeconds) + System.lineSeparator());

		if(this.verbose){
			System.out.println("Running Dynamic analysis...");
		}
		// (optional) use tamiflex to dynamically identify reflection calls
		long dynamicTimeTotal = 0;
		if(tamiFlexJar.isPresent() || jmtrace.isPresent()) {
			//TamiFlexRunner
			Instant start = Instant.now();
			TamiFlexRunner tamiflex;

			if(tamiFlexJar.isPresent()){
				tamiflex = new TamiFlexRunner(tamiFlexJar.get().getAbsolutePath(),	project_path, false);
			}
			else{
				tamiflex = new JMTraceRunner(jmtrace.get().getAbsolutePath(), project_path);
			}

			try {
				tamiflex.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//if both options were present
			if(jmtrace.isPresent() && tamiFlexJar.isPresent()){
				JMTraceRunner jmtrunner = new JMTraceRunner(jmtrace.get().getAbsolutePath(), project_path);
				try {

					jmtrunner.run(tamiflex);
					//resetting results to merged results
					tamiflex = (TamiFlexRunner) jmtrunner;

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			dynamicTimeTotal += Duration.between(start, Instant.now()).getSeconds();

			// add all reached classes in each module to the corresponding set of used classes
			for(String module : tamiflex.accessed_classes.keySet()) {
				Set<String> usedClassesInModule = tamiflex.accessed_classes.get(module);
				for(String usedClass : usedClassesInModule) {
					if(appClasses.contains(usedClass)) {
						usedAppClasses.add(usedClass);
					} else if (libClasses.contains(usedClass)) {
						usedLibClasses.add(usedClass);
						if(libClassesCompileOnly.contains(usedClass)) {
							usedLibClassesCompileOnly.add(usedClass);
						}
					}
				}
			}

			// aggregate all used methods from each submodule (if any) into one set
			HashMap<String, HashSet<MethodData>> new_entry_points = new HashMap<String, HashSet<MethodData>>();

			//aggregate all methods to be used to search caller methods
			Set<MethodData> allMethods = new HashSet<MethodData>();
			allMethods.addAll(testMethods);
			allMethods.addAll(appMethods);
			allMethods.addAll(libMethods);


			for(String module : tamiflex.used_methods.keySet()) {
				HashSet<MethodData> set = new HashSet<MethodData>();

				//resolve all the caller methods to method data objects and add them to used_methods to be processed by the next loop
				HashMap<String, ArrayList<MethodData>> processed_callers = new HashMap<String, ArrayList<MethodData>>();

				for (String calledFrom : tamiflex.used_methods_callers.get(module)) {
					if(calledFrom==null) continue;

					String[] calledFromSplit = calledFrom.split("\\.");
					String calledFromClass = "";
					for(int i=0; i<calledFromSplit.length -1 ; i++){
						calledFromClass += calledFromSplit[i];
						if(i != (calledFromSplit.length -2)){
							calledFromClass += ".";
						}
					}
					String calledFromMethodName = calledFromSplit[calledFromSplit.length-1];

					//search for caller method in all methods
					for(MethodData md: allMethods){
						if(calledFromClass.equals(md.getClassName()) && calledFromMethodName.equals(md.getName())){
							ArrayList methodarr;
							if(processed_callers.containsKey(calledFrom)){
								//if exists, get the resolved methods array
								methodarr = processed_callers.get(calledFrom);
							}
							else{
								//create new array
								methodarr = new ArrayList<MethodData>();
							}
							methodarr.add(md);
							processed_callers.put(calledFrom, methodarr);

							//insert into used_methods array to be processed by the next loop
							//tamiflex.used_methods.get(module).put( md.getClassName()+": "+md.getSubSignature(),new HashSet<String>());
						}
					}
				}

				//process used_methods
				for(Map.Entry<String, Set<String>> entry : tamiflex.used_methods.get(module).entrySet()) {
					String record = entry.getKey();
					String[] ss = record.split(": ");
					String class_name1 = ss[0];
					String method_signature1 = ss[1];
					Set<MethodData> calledFromMethodData = new HashSet<MethodData>();
					for(String calledFrom : entry.getValue()){
						//TEMP FIX: add all entry from the resolved methods, might need to be changed later
						if(processed_callers.containsKey(calledFrom)){
							processed_callers.get(calledFrom).forEach((caller_md)->calledFromMethodData.add(caller_md));
						}
					}
					
					boolean foundInApp = false;
					for(MethodData md : appMethods) {
						String class_name2 = md.getClassName();
						// The signature generated below only contains return type, method name, 
						// and arguments. There are no access modifiers.
						String method_signature2 = md.getSubSignature(); 
						if(class_name1.equals(class_name2) && method_signature1.equals(method_signature2)) {
							// this is an application method
							if(!usedAppMethods.containsKey(md)) {
								// this method is already identified as a used method by static analysis
								set.add(md);
								usedAppMethods.put(md, new HashSet<MethodData>());
								usedAppClasses.add(md.getClassName());
							}

							usedAppMethods.get(md).addAll(calledFromMethodData);

							foundInApp = true;
							break;
						}
					}

					
					if(!foundInApp) {
						for(MethodData md : libMethods) {
							String class_name2 = md.getClassName();
							// The signature generated below only contains return type, method name, 
							// and arguments. There are no access modifiers.
							String method_signature2 = md.getSubSignature(); 
							if(class_name1.equals(class_name2) && method_signature1.equals(method_signature2)) {
								// this is a library method
								if(!usedLibMethods.containsKey(md)) {
									// this method is already identified as a used method by static analysis
									set.add(md);
									usedLibMethods.put(md, new HashSet<MethodData>());
									usedLibClasses.add(md.getClassName());
									
									// also need to update usedLibMethodsCompileOnly and usedLibClassesCompile only
									if(libMethodsCompileOnly.contains(md)) {
										usedLibMethodsCompileOnly.put(md, new HashSet<MethodData>());
										usedLibClasses.add(md.getClassName());
									}
								}

								usedLibMethods.get(md).addAll(calledFromMethodData);
								if(usedLibMethodsCompileOnly.containsKey(md)){
									usedLibMethodsCompileOnly.get(md).addAll(calledFromMethodData);
								}

								foundInApp = true;
								break;

							}
						}
					}

					if(!foundInApp){
						for(MethodData md: testMethods){
							if(class_name1.equals(md.getClassName()) && method_signature1.equals(md.getSubSignature())){
								if(!usedTestMethods.containsKey(md)){
									set.add(md);
									usedTestMethods.put(md, new HashSet<MethodData>());
									usedTestClasses.add(md.getClassName());
								}

								usedTestMethods.get(md).addAll(calledFromMethodData);
								foundInApp = true;
								break;
							}
						}
					}
					
					new_entry_points.put(module, set);
				}
			}


			// aggregate all accessed fields from each submodule (if any) into one set
			for(String module : tamiflex.accessed_fields.keySet()) {
				for(String record : tamiflex.accessed_fields.get(module)) {
					if(record == null) continue;

					String[] ss = record.split(": ");
					String ownerClassName = ss[0];
					String fieldSignature = ss[1];
					String[] ss2 = fieldSignature.split(" ");
					String fieldType = ss2[0];
					String fieldName = ss2[1];

					// check the application fields first
					if(appClasses.contains(ownerClassName)) {
						// this accessed field is from application classes
						for(FieldData field : appFields) {
							if(field.getName().equals(fieldName) && field.getClassName().equals(ownerClassName)
									&& field.getType().equals(fieldType)) {
								usedAppFields.add(field);
								usedAppClasses.add(ownerClassName);
							}
						}
					} else if (libClasses.contains(ownerClassName)) {
						// this accessed field is from external libraries
						for(FieldData field : libFields) {
							if(field.getName().equals(fieldName) && field.getClassName().equals(ownerClassName)
									&& field.getType().equals(fieldType)) {
								usedLibFields.add(field);
								usedLibClasses.add(ownerClassName);

								if(libFieldsCompileOnly.contains(field)) {
									usedLibFieldsCompileOnly.add(field);
									usedLibClassesCompileOnly.add(ownerClassName);
								}
							}
						}
					}
				}
			}
			if(this.verbose){
				System.out.println("Done running dynamic analysis!");
			}

			log.append("dynamic_analysis_time," + Long.toString(dynamicTimeTotal) + System.lineSeparator());

			long callGraphDynamicTime = 0;
			// set those methods that are invoked via reflection as entry points and redo the
			// static analysis
			for(String module : new_entry_points.keySet()) {
				if(this.verbose){
					System.out.println("Running Tamiflex callgraph analysis for module \"" + module +"\"...");
				}
				HashSet<MethodData> entry_methods = new_entry_points.get(module);
				
				List<File> localLibClassPaths =
						(lib_class_paths.containsKey(module) ? lib_class_paths.get(module) : new ArrayList<File>());

				List<File> localAppClassPaths =
						(app_class_paths.containsKey(module) ? app_class_paths.get(module) : new ArrayList<File>());

				List<File> localTestClassPaths =
						(app_test_paths.containsKey(module) ? app_test_paths.get(module) : new ArrayList<File>());

				EntryPointProcessor customEntryPointProcessor =
					new EntryPointProcessor(false, false, false,entry_methods);

				Instant callgraphTime = Instant.now();
				CallGraphAnalysisCacheWrapper runner =
						new CallGraphAnalysisCacheWrapper(new File(project_path), module, localLibClassPaths,
							localAppClassPaths, localTestClassPaths, customEntryPointProcessor,
								useSpark, this.useCache, this.verbose);
				runner.setup();
				runner.run();

				callGraphDynamicTime += Duration.between(callgraphTime, Instant.now()).getSeconds();

				//this.callgraphs.addAll(runner.getCallGraphs());
				
				// aggregate the analysis result of the submodule
				for(String class_name : runner.getUsedLibClasses()) {
					this.usedLibClasses.add(class_name);
					if(this.libClassesCompileOnly.contains(class_name)) {
						this.usedLibClassesCompileOnly.add(class_name);
					}
				}
				for(Map.Entry<MethodData, Set<MethodData>> entry: runner.getUsedLibMethods().entrySet()){
					addToMap(this.usedLibMethods, entry.getKey(), entry.getValue());

					if(this.libMethodsCompileOnly.contains(entry.getKey())) {
						addToMap(this.usedLibMethodsCompileOnly, entry.getKey(), entry.getValue());
					}
				}
				for(FieldData field : runner.getUsedLibFields()) {
					this.usedLibFields.add(field);
					if(this.libFieldsCompileOnly.contains(field)) {
						this.usedLibFieldsCompileOnly.add(field);
					}
				}

				this.usedAppClasses.addAll(runner.getUsedAppClasses());
				addToMap(this.usedAppMethods, runner.getUsedAppMethods());
				this.entryPoints.addAll(runner.getEntryPoints());
				this.usedAppFields.addAll(runner.getUsedAppFields());

				this.usedTestClasses.addAll(runner.getUsedTestClasses());
				addToMap(this.usedTestMethods, runner.getUsedTestMethods());
				
				// make sure to reset Soot after running reachability analysis
				G.reset();

				if(this.verbose){
					System.out.println("Done running Tamiflex callgraph analysis for module \"" + module +"\"!");
				}
			}
			log.append("dynamic_callgraph_extension_time," + Long.toString(callGraphDynamicTime) + System.lineSeparator());
		}


		
		if(count > 1) {
			adjustClassesAndMethodsAndFieldsFromSubmodules();
		}
	}
	
	/**
	 * When analyzing different submodules, the application classes in one module may
	 * be treated as library classes in another module. We need to adjust this so that 
	 * application classes/methods/fields are always stored in the application related fields,
	 * though they are used as library classes/methods/fields in other modules
	 */
	private void adjustClassesAndMethodsAndFieldsFromSubmodules() {
		Set<String> lib_classes_copy = new HashSet<String>(this.libClasses);
		// only keep the app classes from other modules
		lib_classes_copy.retainAll(this.appClasses);
		// after removing the app classes from other modules, only classes from external libs remain 
		this.libClasses.removeAll(lib_classes_copy);
		// do the same to get classes from external libs in the compile scope
		Set<String> lib_classes_compile_only_copy = new HashSet<String>(this.libClassesCompileOnly);
		lib_classes_compile_only_copy.retainAll(this.appClasses);
		this.libClassesCompileOnly.removeAll(lib_classes_compile_only_copy);
		
		Set<MethodData> lib_methods_copy = new HashSet<MethodData>(this.libMethods);
		// only keep the app methods from other modules
		lib_methods_copy.retainAll(this.appMethods);
		// after removing the app methods from other modules, only methods from external libs remain
		this.libMethods.removeAll(lib_methods_copy);
		// do the same to get methods from external libs in the compile scope
		Set<MethodData> lib_methods_compile_only_copy = new HashSet<MethodData>(this.libMethodsCompileOnly);
		lib_methods_compile_only_copy.retainAll(this.appMethods);
		this.libMethodsCompileOnly.removeAll(lib_methods_compile_only_copy);

		Set<FieldData> lib_fields_copy = new HashSet<FieldData>(this.libFields);
		// only keep the app fields from other modules
		lib_fields_copy.retainAll(this.appFields);
		// after removing the app fields from other modules, only fields from external libs remain
		this.libFields.removeAll(lib_fields_copy);
		// do the same to get methods from external libs in the compile scope
		Set<FieldData> lib_fields_compile_only_copy = new HashSet<FieldData>(this.libFieldsCompileOnly);
		lib_fields_compile_only_copy.retainAll(this.appFields);
		this.libFieldsCompileOnly.removeAll(lib_fields_compile_only_copy);
		
		Set<String> used_lib_classes_copy = new HashSet<String>(this.usedLibClasses);
		// only keep used app classes from other modules
		used_lib_classes_copy.retainAll(this.appClasses);
		// after removing used app classes from other modules, only used classes from external libs remain 
		this.usedLibClasses.removeAll(used_lib_classes_copy);
		// do the same to get used classes from external libs in the compile scope 
		Set<String> used_lib_classes_compile_only_copy = new HashSet<String>(this.usedLibClassesCompileOnly);
		used_lib_classes_compile_only_copy.retainAll(this.appClasses); 
		this.usedLibClassesCompileOnly.removeAll(used_lib_classes_compile_only_copy);
		
		// also need to add the used app classes from other modules back to the set of used app classes
		// just in case that those classes are not used in their own modules
		this.usedAppClasses.addAll(used_lib_classes_copy);
		
		Map<MethodData, Set<MethodData>> used_lib_methods_copy
				= new HashMap<MethodData, Set<MethodData>>();
		// only keep used app methods from other modules
		for(Map.Entry<MethodData,Set<MethodData>> methodData : this.usedLibMethods.entrySet()){
			if(this.appMethods.contains(methodData.getKey())) {
				addToMap(used_lib_methods_copy, methodData.getKey(), methodData.getValue());
			}
		}
		// after removing used app methods from other modules, only used methods from external libs remain
		for(Map.Entry<MethodData, Set<MethodData>> methodData : used_lib_methods_copy.entrySet()){
			this.usedLibMethods.remove(methodData.getKey());
		}
		// do the same to get used methods from external libs in the compile scope
		Map<MethodData,Set<MethodData>> used_lib_methods_compile_only_copy = new HashMap<MethodData,Set<MethodData>>();
		for(Map.Entry<MethodData,Set<MethodData>> entry : this.usedLibMethodsCompileOnly.entrySet()){
			if(this.appMethods.contains(entry.getKey())){
				addToMap(used_lib_methods_compile_only_copy, entry.getKey(), entry.getValue());
			}
		}

		for(Map.Entry<MethodData, Set<MethodData>> entry : used_lib_methods_compile_only_copy.entrySet()){
			this.usedLibMethodsCompileOnly.remove(entry.getKey());
		}

		// also need to add the used app methods from other modules back to the set of used app methods
		// just in case that those methods are not called in their own modules
		addToMap(this.usedAppMethods,used_lib_methods_copy);

		Set<FieldData> used_lib_fields_copy = new HashSet<FieldData>(this.usedLibFields);
		// only keep used app fields from other modules
		used_lib_fields_copy.retainAll(this.appFields);
		// after removing used app fields from other modules, only used fields from external libs remain
		this.usedLibFields.removeAll(used_lib_fields_copy);
		// do the same to get used fields from external libs in the compile scope
		Set<FieldData> used_lib_fields_compile_only_copy = new HashSet<FieldData>(this.usedLibFieldsCompileOnly);
		used_lib_fields_compile_only_copy.retainAll(this.appFields);
		this.usedLibFieldsCompileOnly.removeAll(used_lib_fields_compile_only_copy);

		// also need to add the used app fields from other modules back to the set of used app fields
		// just in case that those fields are not used in their own modules
		this.usedAppFields.addAll(used_lib_fields_copy);
	}

	/*
	I don't like that I've done it this way. We do not wish to concern ourselves with annotations. We wish to simply
	leave annotations alone.

	The reason I've done this here, and not in the CallGraphAnalysis is I've already generated a large number of caches
	of the callgraph information generated by the CallGraphAnalysis. Altering it would been rewriting the caches. Ergo,
	the fix has been made here

	TODO: This filtering of annotation classes should be moved to the callgraph (the caches callgraphs need re-generated).
	 */

	private static boolean isAnnotation(String className){
		SootClass sootClass = Scene.v().getSootClass(className);
		return Modifier.isAnnotation(sootClass.getModifiers());
	}

	private static Set<String> stripAnnotationClasses(Set<String> classes){
		Set<String> toReturn = new HashSet<String>();
		for(String className : classes){
			if(!isAnnotation(className)){
				toReturn.add(className);
			}
		}
		return toReturn;
	}

	private static Set<MethodData> stripAnnotationMethods(Set<MethodData> methodData){
		Set<MethodData> toReturn = new HashSet<MethodData>();
		for(MethodData toCheck : methodData){
			if(!isAnnotation(toCheck.getClassName())){
				toReturn.add(toCheck);
			}
		}
		return toReturn;
	}

	private static Map<MethodData, Set<MethodData>> stripAnnotationMethods(Map<MethodData, Set<MethodData>> methodData){
		Map<MethodData, Set<MethodData>> toReturn = new HashMap<MethodData, Set<MethodData>>();
		for(Map.Entry<MethodData, Set<MethodData>> entry : methodData.entrySet()){
			if(!isAnnotation(entry.getKey().getClassName())){
				Set<MethodData> newValues = new HashSet<MethodData>();
				for(MethodData value : entry.getValue()){
					if(!isAnnotation(value.getClassName())){
						newValues.add(value);
					}
				}
				toReturn.put(entry.getKey(), newValues);
			}
		}

		return toReturn;
	}

	private static Set<FieldData> stripAnnotationFields(Set<FieldData> fieldData){
		Set<FieldData> toReturn = new HashSet<FieldData>();
		for(FieldData toCheck : fieldData){
			if(!isAnnotation(toCheck.getClassName())){
				toReturn.add(toCheck);
			}
		}
		return toReturn;
	}

	@Override
	public Set<String> getLibClasses() {
		return Collections.unmodifiableSet(this.libClasses);
	}

	@Override
	public Set<MethodData> getLibMethods() {
		return stripAnnotationMethods(Collections.unmodifiableSet(this.libMethods));
	}

	@Override
	public Set<FieldData> getLibFields() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.libFields));
	}
	
	@Override
	public Set<String> getLibClassesCompileOnly() {
		return stripAnnotationClasses(Collections.unmodifiableSet(this.libClassesCompileOnly));
	}
	
	@Override
	public Set<MethodData> getLibMethodsCompileOnly() {
		return stripAnnotationMethods(Collections.unmodifiableSet(this.libMethodsCompileOnly));
	}

	@Override
	public Set<FieldData> getLibFieldsCompileOnly() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.libFieldsCompileOnly));
	}

	@Override
	public Set<String> getAppClasses() {
		return stripAnnotationClasses(Collections.unmodifiableSet(this.appClasses));
	}

	@Override
	public Set<MethodData> getAppMethods() {
		return stripAnnotationMethods(Collections.unmodifiableSet(this.appMethods));
	}

	@Override
	public Set<FieldData> getAppFields() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.appFields));
	}

	@Override
	public Set<String> getUsedLibClasses() {
		return stripAnnotationClasses(Collections.unmodifiableSet(this.usedLibClasses));
	}
	
	@Override
	public Set<String> getUsedLibClassesCompileOnly() {
		return stripAnnotationClasses(Collections.unmodifiableSet(this.usedLibClassesCompileOnly));
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedLibMethods() {
		return stripAnnotationMethods(Collections.unmodifiableMap(this.usedLibMethods));
	}
	
	@Override
	public Map<MethodData, Set<MethodData>> getUsedLibMethodsCompileOnly() {
		return stripAnnotationMethods(Collections.unmodifiableMap(this.usedLibMethodsCompileOnly));
	}

	@Override
	public Set<FieldData> getUsedLibFields() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.usedLibFields));
	}

	@Override
	public Set<FieldData> getUsedLibFieldsCompileOnly() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.usedLibFieldsCompileOnly));
	}

	@Override
	public Set<String> getUsedAppClasses() {
		return stripAnnotationClasses(Collections.unmodifiableSet(this.usedAppClasses));
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedAppMethods() {
		return stripAnnotationMethods(Collections.unmodifiableMap(this.usedAppMethods));
	}

	@Override
	public Set<FieldData> getUsedAppFields() {
		return stripAnnotationFields(Collections.unmodifiableSet(this.usedAppFields));
	}

	@Override
	public List<File> getAppClasspaths() {
		return Collections.unmodifiableList(getClassPath(this.app_class_paths));
	}

	@Override
	public List<File> getLibClasspaths() {
		/*
		TODO: This is a bit of a mess --- really don't like this classpath/classpath_compile_only thing. It's a mess.
		Can we clean this up at some point? It's confusing and it'll just introduce more bugs going forward.
		 */
		List<File> toReturn = new ArrayList<File>();
		for(String key : classpaths_compile_only.keySet()){
			String cp_compile_only = classpaths_compile_only.get(key);
			HashSet<String> compile_lib_paths =
					new HashSet<String>(Arrays.asList(cp_compile_only.split(File.pathSeparator)));
			for(String classPath : compile_lib_paths){
				File toAdd = new File(classPath);
				if(ClassFileUtils.directoryContains(new File(this.project_path), toAdd)) {
					toReturn.add(new File(classPath));
				}
			}
		}
		return Collections.unmodifiableList(toReturn);
	}

	@Override
	public List<File> getTestClasspaths() {
		return Collections.unmodifiableList(getClassPath(this.app_test_paths));
	}

	private List<File> getClassPath(Map<String, List<File>> paths){
		List<File> toReturn = new ArrayList<File>();
		for(Map.Entry<String, List<File>> mapEntry : paths.entrySet()){
			for(File file : mapEntry.getValue()){
				if(!toReturn.contains(file)){
					toReturn.add(file);
				}
			}
		}

		return toReturn;
	}

	@Override
	public Set<MethodData> getEntryPoints(){
		return this.entryPoints;
	}

	@Override
	public TestOutput getTestOutput(){
		return this.testOutput;
	}

	@Override
	public Set<MethodData> getTestMethods(){
		return stripAnnotationMethods(this.testMethods);
	}

	@Override
	public Map<MethodData, Set<MethodData>> getUsedTestMethods(){
		return stripAnnotationMethods(this.usedTestMethods);
	}

	@Override
	public Set<String> getTestClasses(){
		return stripAnnotationClasses(this.testClasses);
	}

	@Override
	public Set<String> getUsedTestClasses(){
		return stripAnnotationClasses(this.usedTestClasses);
	}

	public String getLog(){
		return this.log.toString();
	}
}
