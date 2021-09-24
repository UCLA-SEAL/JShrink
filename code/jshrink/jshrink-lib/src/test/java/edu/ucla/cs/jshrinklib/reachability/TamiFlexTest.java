package edu.ucla.cs.jshrinklib.reachability;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import edu.ucla.cs.jshrinklib.GitGetter;
import fj.Hash;
import fj.data.vector.V;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

public class TamiFlexTest {
	/**
	 * Test the java agent injection on a pom file that (1) explicitly declares surefire plugin,
	 * and (2) has the configuration node but no the argLine node
	 * 
	 * @throws IOException
	 */

	private static GitGetter gitGetter;

	@BeforeClass
	public static void setup(){
		gitGetter = new GitGetter();
	}

	@AfterClass
	public static void cleanup(){
		gitGetter.removeGitDir();
	}

	@Test
	public void testJavaAgentInjection1() throws IOException {
		String pom_file = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "junit_pom.xml").getFile()).getAbsolutePath();
		
		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		
		// inject tamiflex as a java agent in the surefire test plugin
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, null, false);
		tamiflex.injectTamiFlex(pom_file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		assertTrue(content.contains("<argLine>-javaagent:" + tamiflex_jar_path + "</argLine>"));
		
		// make sure we do not inject the java agent repetitively
		tamiflex.injectTamiFlex(pom_file);
		content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		int count = StringUtils.countMatches(content, "<argLine>-javaagent:" + tamiflex_jar_path + "</argLine>");

		// restore the pom file in case of test failures
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertEquals(1, count);
	}
	
	/**
	 * Test the java agent injection on a pom file that (1) explicitly declares surefire plugin,
	 * (2) has no configuration node and of course also no the argLine node
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJavaAgentInjection2() throws IOException {
		String pom_file = "src/test/resources/tamiflex/apache_lang_pom.xml";
		
		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		
		// inject tamiflex as a java agent in the surefire test plugin
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, null, false);
		tamiflex.injectTamiFlex(pom_file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
				
		// restore the pom file first in case of test failure
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertTrue(content.contains("<configuration>"
				+ "<argLine>-javaagent:" + tamiflex_jar_path + "</argLine>"
				+ "</configuration>"));
	}
	
	/**
	 * 
	 * Test the java agent injection on a pom file that (1) explicitly declares surefire plugin,
	 * (2) has the configuration node with an argLine node
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJavaAgentInjection3() throws IOException {
		String pom_file = "src/test/resources/tamiflex/hankcs_HanLP_pom.xml";
		
		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		
		// inject tamiflex as a java agent in the surefire test plugin
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, null, false);
		tamiflex.injectTamiFlex(pom_file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		assertTrue(content.contains("<argLine>-Dfile.encoding=UTF-8 -javaagent:" + tamiflex_jar_path + "</argLine>"));
		
		// restore the pom file
		FileUtils.copyFile(copy, file);
		copy.delete();
	}
	
	/**
	 * 
	 * Test the java agent injection on a pom file that (1) does not declare surefire plugin,
	 * (2) and therefore also has no configuration node or argLine node
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJavaAgentInjection4() throws IOException {
		String pom_file = "src/test/resources/tamiflex/amaembo_streamex_pom.xml";
		
		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		
		// inject tamiflex as a java agent in the surefire test plugin
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, null, false);
		tamiflex.injectTamiFlex(pom_file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		assertTrue(content.contains("<plugin>"
				+ "<groupId>org.apache.maven.plugins</groupId>"
				+ "<artifactId>maven-surefire-plugin</artifactId>"
				+ "<version>2.20.1</version>"
				+ "<configuration><argLine>-javaagent:" + tamiflex_jar_path + "</argLine></configuration>"
				+ "</plugin>"));
		
		// restore the pom file
		FileUtils.copyFile(copy, file);
		copy.delete();
	}
	
	/**
	 * 
	 * Test the java agent injection on a pom file that does not even have a build node
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJavaAgentInjection5() throws IOException {
		String pom_file = "src/test/resources/tamiflex/pf4j_plugin1_pom.xml";
		
		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		
		// inject tamiflex as a java agent in the surefire test plugin
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, null, false);
		tamiflex.injectTamiFlex(pom_file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());

		// restore the pom file first in case of test failure
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertTrue(content.contains("<build><plugins><plugin>"
				+ "<groupId>org.apache.maven.plugins</groupId>"
				+ "<artifactId>maven-surefire-plugin</artifactId>"
				+ "<version>2.20.1</version>"
				+ "<configuration><argLine>-javaagent:" + tamiflex_jar_path + "</argLine></configuration>"
				+ "</plugin></plugins></build>"));
	}
	
	@Test
	public void testRunMavenTest() throws IOException, InterruptedException {
		String project_path = "src/test/resources/simple-test-project";
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, false);
		boolean result = tamiflex.runMavenTest();
		assertFalse(result);
	}
	
	@Test @Ignore //This just takes too long...
	public void testRunMavenTest2() throws IOException, InterruptedException {
		String project_path = "src/test/resources/square_okhttp";
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, false);
		boolean result = tamiflex.runMavenTest();
		// if square_okhttp does not pass all its test cases, the assertion will fail
		assertTrue(result);
	}
	
	@Test
	public void testLogAnalysis() {
		TamiFlexRunner tamiflex = new TamiFlexRunner(null, null, false);
		String log = "src/test/resources/tamiflex/junit_refl.log";
		tamiflex.analyze("junit", log);
		assertEquals(1040, tamiflex.accessed_classes.get("junit").size());
		assertEquals(626, tamiflex.accessed_fields.get("junit").size());
		assertEquals(2975, tamiflex.used_methods.get("junit").size());

		assertTrue(tamiflex.accessed_classes.get("junit").contains("org.junit.runner.notification.RunListener$ThreadSafe"));
	}
	
	@Test
	public void testLogAnalysis2() {
		TamiFlexRunner tamiflex = new TamiFlexRunner(null, null, false);
		String log = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator
				+ "apache_commons_lang_refl.log").getFile()).getAbsolutePath();
		tamiflex.analyze("commons-lang3", log);
		assertEquals(896, tamiflex.accessed_classes.get("commons-lang3").size());
		assertEquals(867, tamiflex.accessed_fields.get("commons-lang3").size());
		assertEquals(4705, tamiflex.used_methods.get("commons-lang3").size());
	}
	
	@Test
	public void testTamiFlexRunner() {
		String project_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("apache_commons-lang").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, false);
		try {
			tamiflex.run();
			assertEquals(893, tamiflex.accessed_classes.get("commons-lang3").size());
			assertEquals(849, tamiflex.accessed_fields.get("commons-lang3").size());
			assertEquals(4696, tamiflex.used_methods.get("commons-lang3").size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTamiFlexRerun() {
		String project_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("apache_commons-lang").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
			assertEquals(893, tamiflex.accessed_classes.get("commons-lang3").size());
			assertEquals(849, tamiflex.accessed_fields.get("commons-lang3").size());
			assertEquals(4696, tamiflex.used_methods.get("commons-lang3").size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testTamiFlexOnSimpleMavenProject() {
		String project_path = new File(TamiFlexTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "tamiflex-test-project").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
			assertTrue(tamiflex.accessed_classes.get("tamiflex-test-project").contains("A"));
			assertFalse(tamiflex.accessed_classes.get("tamiflex-test-project").contains("Main"));
			String field1 = "A: java.lang.String f4";
			String field2 = "A: java.lang.String f5";
			// Though A.f4 is used in a dynamically invoked method, it is not dynamically accessed so it is not
			// logged by TamiFlex
			assertFalse(tamiflex.accessed_fields.get("tamiflex-test-project").contains(field1));
			assertTrue(tamiflex.accessed_fields.get("tamiflex-test-project").contains(field2));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTamiFlexOnMavenProjectWithOneSubmodule() {
		// the gson project has many submodules but only one submodule is actually built
		String project_path = this.gitGetter.addGitHubProject("google","gson",
				"aa236ec38d39f434c1641aeaef9241aec18affde").getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
			// flaky
			assertEquals(435, tamiflex.accessed_classes.get("gson").size());
			assertEquals(521, tamiflex.accessed_fields.get("gson").size());
			assertEquals(1668, tamiflex.used_methods.get("gson").size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTamiFlexOnMavenProjectWithMultiSubmodules() {
		// the essentials project has multiple modules compiled but only one module has 
		// real Java class files, the other two only have resources
		String project_path = this.gitGetter.addGitHubProject("greenrobot","essentials",
			"31eaaeb410174004196c9ef9c9469e0d02afd94b").getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
			assertEquals(1, tamiflex.accessed_classes.size());
			assertEquals(1, tamiflex.accessed_fields.size());
			assertEquals(1, tamiflex.used_methods.size());
			assertEquals(72, tamiflex.accessed_classes.get("essentials").size());
			assertEquals(72, tamiflex.accessed_fields.get("essentials").size());
			// some tests are not deterministic, so the assertion below may fail
			assertEquals(246, tamiflex.used_methods.get("essentials").size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Note that injecting TamiFlex causes test failures in this project. 
	 */
	@Test
	public void testTamiFlexOnMavenProjectWithMultiSubmodules2() {
		// the cglib project has five modules
		// four of them have java class files and only two of them 
		// have test classes
		String project_path = this.gitGetter.addGitHubProject("cglib","cglib",
				"5942bcd657f35a699f05fadfdf720a5c6a3af2b5").getAbsolutePath();
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
			// only one module is tested successfully
			assertEquals(1, tamiflex.accessed_classes.size());
			assertEquals(1, tamiflex.accessed_fields.size());
			assertEquals(1, tamiflex.used_methods.size());
			HashSet<String> all_accessed_classes = new HashSet<String>();
			for(String module : tamiflex.accessed_classes.keySet()) {
				all_accessed_classes.addAll(tamiflex.accessed_classes.get(module));
			}
			assertEquals(62, all_accessed_classes.size());
			HashSet<String> all_accessed_fields = new HashSet<String>();
			for(String module : tamiflex.accessed_fields.keySet()) {
				all_accessed_fields.addAll(tamiflex.accessed_fields.get(module));
			}
			// flaky
			assertEquals(36, all_accessed_fields.size());
			HashSet<String> all_used_methods = new HashSet<String>();
			for(String module : tamiflex.used_methods.keySet()) {
				all_used_methods.addAll(tamiflex.used_methods.get(module).keySet());
			}
			// flaky
			assertEquals(391, all_used_methods.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTamiFlexConfig() {
		// create a dummy TamiFlexRunner object
		TamiFlexRunner tamiflex = new TamiFlexRunner("", "", false);
		try {
			tamiflex.checkTamiFlexConfig();
			
			File propFile = new File(
					System.getProperty("user.home") + File.separator
						+ ".tamiflex" + File.separator + "poa.properties");
			assertTrue(propFile.exists());
			String content = FileUtils.readFileToString(propFile, Charset.defaultCharset());
			assertTrue(content.contains("dontDumpClasses = true"));
			assertTrue(content.contains("dontNormalize = true"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void compareTamiflexWithJMTrace(){
		String project_path = "/home/jay/call-graph-analysis/experiment_resources/sample-projects/sockeqwe_fragmentargs";
		String jmTraceHome = "/home/jay/openjdktest/test/jshrink-mtrace";
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();

		JMTraceRunner jmt = new JMTraceRunner(jmTraceHome, project_path);
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);

		class Helper{
			public Set<?> disjunction(Set<?> s1, Set<?> s2){
				return  s1.stream().filter(x-> !s2.contains(x)).collect(Collectors.toSet());
			}
		}
		try {
			jmt.run();
			tamiflex.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Helper h = new Helper();
		for(String module : tamiflex.accessed_classes.keySet()){
			System.out.flush();
			System.out.println("For module "+module);
			System.out.println("Classes only in JMTrace");
			Set<?> jmtrace_only = h.disjunction(jmt.accessed_classes.get(module), tamiflex.accessed_classes.get(module));
			System.out.println(Arrays.toString(jmtrace_only.toArray()));

			System.out.println("Classes only in Tamiflex");
			Set<?> tamiflex_only = h.disjunction(tamiflex.accessed_classes.get(module), jmt.accessed_classes.get(module));
			System.out.println(Arrays.toString(tamiflex_only.toArray()));

			System.out.println("Methods only in JMTrace");
			jmtrace_only = h.disjunction(jmt.used_methods.get(module).keySet(), tamiflex.used_methods.get(module).keySet());
			System.out.println(Arrays.toString(jmtrace_only.toArray()));

			System.out.println("Methods only in Tamiflex");
			tamiflex_only = h.disjunction(tamiflex.used_methods.get(module).keySet(), jmt.used_methods.get(module).keySet());
			System.out.println(Arrays.toString(tamiflex_only.toArray()));
		}
	}

	@Test
	public void testDynamicCallLogAnalysis() throws URISyntaxException, IOException {
		String project_path = "/home/jay/openjdktest/Li_Sui-benchmark";
		String tamiflex_jar_path = new File(TamiFlexTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "poa-2.0.3.jar").getFile()).getAbsolutePath();
		String module = "benchmark";
		String expected_path = TamiFlexTest.class.getClassLoader().getResource("LiSuiBenchmark").toURI().getPath()+File.separator+"BenchmarkOracle(ExceptedCallEdges).csv";
		String unexpected_path = TamiFlexTest.class.getClassLoader().getResource("LiSuiBenchmark").toURI().getPath()+File.separator+"BenchmarkOracle(UnexceptedCallEdges).csv";

		Set<String> expectedAppMethods = new HashSet<String>();
		Set<String> unexpectedAppMethods = new HashSet<String>();
		Set<String> accessedClassNames = new HashSet<String>();
		Files.lines(new File(expected_path).toPath()).map(l -> l.split("->"))
				.forEach(e -> {
					if(e.length == 2){
						//expectedAppMethods.add(e[0]);
						expectedAppMethods.add(e[1]);
						//accessedClassNames.add(e[0].split(": ")[0]);
						accessedClassNames.add(e[1].split(": ")[0]);
					}
				});
		Files.lines(new File(unexpected_path).toPath()).map(l -> l.split("->"))
				.forEach(e -> {
					if(e.length == 2){
						unexpectedAppMethods.add(e[1]);
						accessedClassNames.add(e[1].split(": ")[0]);
					}
				});
		TamiFlexRunner tamiflex = new TamiFlexRunner(tamiflex_jar_path, project_path, true);
		try {
			tamiflex.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashSet<String> methods = new HashSet<String>(tamiflex.used_methods.get(module).keySet());
		Set<String> appMethods = methods.stream().filter(x->accessedClassNames.contains(x.split(": ")[0])).collect(Collectors.toSet());
		assertTrue(appMethods.size()>0);
		//assertTrue(h.disjunction(unexpectedAppMethods, appMethods).size() == unexpectedAppMethods.size());
		//assertTrue(h.disjunction(expectedAppMethods, appMethods).size() == 0);

		//unexpectedAppMethods.retainAll(appMethods);
		/*for(Set<String> callers: tamiflex.used_methods.get(module).values()){
			methods.addAll(callers);
		}
		appMethods = methods.stream().filter(x->accessedClassNames.contains(x.split(": ")[0])).collect(Collectors.toSet());*/
		Set truePositives = new HashSet<String>(appMethods);
		Set falsePositives = new HashSet<String>(appMethods);
		truePositives.retainAll(expectedAppMethods);
		falsePositives.retainAll(unexpectedAppMethods);
		int total_expected = expectedAppMethods.size();
		int total_unexpected = unexpectedAppMethods.size();

		System.out.println("True Positives - "+truePositives.size()+"/"+total_expected);
		System.out.println("\nFalse Positives - "+falsePositives.size()+"/"+total_unexpected);

		unexpectedAppMethods.retainAll(appMethods);
		expectedAppMethods.removeAll(appMethods);

		unexpectedAppMethods.stream().sorted().forEach(x->System.out.println(x));

		System.out.println("\nFalse Negatives - "+expectedAppMethods.size()+"/"+total_expected);
		expectedAppMethods.stream().sorted().forEach(x->System.out.println(x));
	}
}
