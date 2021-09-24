package edu.ucla.cs.jshrinklib.reachability;

import java.io.File;

import edu.ucla.cs.jshrinklib.util.ASMUtils;
import edu.ucla.cs.jshrinklib.util.EntryPointUtil;
import edu.ucla.cs.jshrinklib.util.MavenUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import soot.G;

import java.util.*;

public class CallGraphAnalysisStressTest {
	private static String root_path = "/media/troy/Disk2/ONR/BigQuery/sample-projects";
	
	private List<File> app_class_paths;
	private List<File> app_test_paths;
	List<File> lib_class_paths;
	Set<MethodData> entryPoints;
	
	private void setup(String project_folder) {
		System.out.println("Processing " + project_folder + " ...");
		app_class_paths = new ArrayList<File>();
		File app_class_path;
		if(project_folder.equals("thinkgem_jeesite")) {
			// a custom target directory
			app_class_path = new File(
					"/media/troy/Disk2/ONR/BigQuery/sample-projects/thinkgem_jeesite/src/main/webapp/WEB-INF/classes");
		} else {
			app_class_path = new File(root_path + 
					File.separator	+ project_folder + File.separator + "/target/classes");
		}
		 
		app_class_paths.add(app_class_path);

		app_test_paths = new ArrayList<File>();
		File app_test_path = new File(root_path + 
				File.separator + project_folder + File.separator + "/target/test-classes");
		app_test_paths.add(app_test_path);

		// get paths to dependent libraries from the log file
        String cp_log = root_path + 
        		File.separator + project_folder + File.separator + "onr_classpath_new.log";       
		lib_class_paths = new ArrayList<File>();
		// We assume the maven project does not have any submodules
		String firstClassPaths = MavenUtils.getClasspathsFromFile(new File(cp_log)).values().iterator().next();
        String[] paths = firstClassPaths.split(File.pathSeparator);
		for(String path: paths){
			lib_class_paths.add(new File(path));
		}
		
		entryPoints = new HashSet<MethodData>();
		// get main methods
		HashSet<String> appClasses = new HashSet<String>();
		Set<MethodData> appMethods = new HashSet<MethodData>();
		Set<FieldData> appFields = new HashSet<FieldData>();
		Map<MethodData, Set<FieldData>> appFieldRefs = new HashMap<MethodData, Set<FieldData>>();
		Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
		ASMUtils.readClass(app_class_path, appClasses, appMethods, appFields, appFieldRefs, virtualCalls);
		Set<MethodData> mainMethods = EntryPointUtil.getMainMethodsAsEntryPoints(appMethods);
		entryPoints.addAll(mainMethods);
		
		// get test methods
        File test_log_path = new File(root_path + File.separator +
                                      project_folder + File.separator + "onr_test.log");
        Set<MethodData> executedTests = 
        		EntryPointUtil.getTestMethodsAsEntryPoints(test_log_path, app_test_path);
//        for(MethodData test : executedTests) {
//        	System.out.println(test);
//        }
        entryPoints.addAll(executedTests);
	}
	
	@Test @Ignore
	public void testJUnit4() {
        String project_folder = "junit-team_junit4";
		setup(project_folder);
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test @Ignore
	public void testApacheCommonsLang() {
		String project_folder = "apache_commons-lang";
		setup(project_folder);
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testSquareJavapoet() {
		String project_folder = "square_javapoet";
		setup(project_folder);
		
		//19 library dependencies
		CallGraphAnalysis runner =
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testJJWT() {
		String project_folder = "jwtk_jjwt";
		setup(project_folder);
		
		//30 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testDensityConverter() {
		String project_folder = "patrickfav_density-converter";
		setup(project_folder);
		
		//40 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testAmazonEcho() {
		String project_folder = "armzilla_amazon-echo-ha-bridge";
		setup(project_folder);
		
		//68 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testElasticSearch() {
		String project_folder = "NLPchina_elasticsearch";
		setup(project_folder);
		
		//71 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testSpringRestServiceOauth() {
		String project_folder = "royclarkson_spring-rest-service-oauth";
		setup(project_folder);
		
		//75 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testSolo() {
		String project_folder = "b3log_solo";
		setup(project_folder);
		
		//78 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testLittleProxy() {
		String project_folder = "adamfisk_LittleProxy";
		setup(project_folder);
		
		//116 library dependencies
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),true);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testJeesite() {
		String project_folder = "thinkgem_jeesite";
		setup(project_folder);
		
		//136 library dependencies
		// disable spark since it will be out of memory
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),false);
		runner.run();
	}
	
	@Test
	@Ignore
	public void testDddsampleCore() {
		String project_folder = "citerus_dddsample-core";
		setup(project_folder);
		
		//152 library dependencies
		// disable spark since it will be out of memory
		CallGraphAnalysis runner = 
				new CallGraphAnalysis(lib_class_paths, app_class_paths, app_test_paths, new EntryPointProcessor(false, false, false, entryPoints),false);
		runner.run();
	}
	
	@After
	public void cleanup() {
		System.out.println("Resetting Soot...");
		G.reset();
	}
}
