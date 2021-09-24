package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.GitGetter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class JMTraceTest {
	/**
	 * Test the java agent injection on a pom file that (1) explicitly declares surefire plugin,
	 * and (2) has the configuration node but no the argLine node
	 *
	 * @throws IOException
	 */

	private static GitGetter gitGetter;
	private static JMTraceRunner jmtrace;
	private static String JMTracehome, injection, injectedNode;

	@BeforeClass
	public static void setup() {
		gitGetter = new GitGetter();
		JMTracehome = new File(JMTraceTest.class.getClassLoader().getResource("jmtrace").getFile()).getAbsolutePath();
		injection = "-Xbootclasspath/a:" + JMTracehome + File.separator+"jmtrace.jar" + " -agentpath:" + JMTracehome + File.separator+"libjmtrace.so";
		injectedNode = "<argLine>"+injection+"</argLine>";
		jmtrace = new JMTraceRunner(JMTracehome, null);
	}

	@AfterClass
	public static void cleanup() {
		gitGetter.removeGitDir();
	}

	@Test
	public void testJavaAgentInjection1() throws IOException {
		String pom_file = new File(JMTraceTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "junit_pom.xml").getFile()).getAbsolutePath();

		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);

		// inject
		jmtrace.injectJMTrace(file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());

		//check injection

		assertTrue(content.contains(injectedNode));

		// make sure we do not inject the java agent repetitively
		jmtrace.injectTamiFlex(pom_file);
		content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		int count = StringUtils.countMatches(content, injection);

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

		// inject
		jmtrace.injectJMTrace(file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());

		// restore the pom file first in case of test failure
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertTrue(content.contains("<configuration>"
				+ injectedNode
				+ "</configuration>"));
	}

	/**
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

		jmtrace.injectJMTrace(file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		String test ="<argLine>-Dfile.encoding=UTF-8 " + injection + "</argLine>";
		assertTrue(content.contains("<argLine>-Dfile.encoding=UTF-8 " + injection + "</argLine>"));

		// restore the pom file
		FileUtils.copyFile(copy, file);
		copy.delete();
	}

	/**
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
		jmtrace.injectJMTrace(file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());
		assertTrue(content.contains("<plugin>"
				+ "<groupId>org.apache.maven.plugins</groupId>"
				+ "<artifactId>maven-surefire-plugin</artifactId>"
				+ "<version>2.20.1</version>"
				+ "<configuration>" + injectedNode + "</configuration>"
				+ "</plugin>"));

		// restore the pom file
		FileUtils.copyFile(copy, file);
		copy.delete();
	}

	/**
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
		jmtrace.injectJMTrace(file);
		String content = FileUtils.readFileToString(new File(pom_file), Charset.defaultCharset());

		// restore the pom file first in case of test failure
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertTrue(content.contains("<build><plugins><plugin>"
				+ "<groupId>org.apache.maven.plugins</groupId>"
				+ "<artifactId>maven-surefire-plugin</artifactId>"
				+ "<version>2.20.1</version>"
				+ "<configuration>" + injectedNode + "</configuration>"
				+ "</plugin></plugins></build>"));
	}

	
	@Test
	public void testRunMavenTest() throws IOException, InterruptedException {
		String project_path = "src/test/resources/tamiflex/tamiflex-test-project";
		JMTraceRunner jmtrace_local = new JMTraceRunner(JMTracehome, project_path);
		File file = new File(project_path+"/pom.xml");
		File copy = new File(file.getAbsolutePath() + ".tmp");
		FileUtils.copyFile(file, copy);
		jmtrace_local.injectJMTrace(file);

		boolean result = jmtrace_local.runMavenTest();
		FileUtils.copyFile(copy, file);
		copy.delete();

		assertTrue(result);
	}

	@Test
	public void testJMTraceRun() throws IOException{
		String project_path = new File(JMTraceTest.class.getClassLoader().getResource("simple-test-project").getFile()).getAbsolutePath();
		TamiFlexRunner jmtracelocal = new JMTraceRunner(JMTracehome, project_path);
		jmtracelocal.run();
		assertTrue(new File(project_path+File.separator+"jmtrace.log").exists());
	}


	@Test
	public void testDynamicCallLogAnalysis() throws URISyntaxException, IOException {
		String log = "/home/jay/openjdktest/Li_Sui-benchmark/jmtrace.log";
		String module = "dpbbench";
        String expected_path = JMTraceTest.class.getClassLoader().getResource("LiSuiBenchmark").toURI().getPath()+File.separator+"BenchmarkOracle(ExceptedCallEdges).csv";
        String unexpected_path = JMTraceTest.class.getClassLoader().getResource("LiSuiBenchmark").toURI().getPath()+File.separator+"BenchmarkOracle(UnexceptedCallEdges).csv";

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

        jmtrace.analyze(module, log);
        Set<String> methods = jmtrace.used_methods.get(module).keySet();
        Set<String> appMethods = methods.stream().filter(x->accessedClassNames.contains(x.split(": ")[0])).collect(Collectors.toSet());
        assertTrue(appMethods.size()>0);
        //assertTrue(h.disjunction(unexpectedAppMethods, appMethods).size() == unexpectedAppMethods.size());
        //assertTrue(h.disjunction(expectedAppMethods, appMethods).size() == 0);
		Set truePositives = new HashSet<String>(appMethods);
		Set falsePositives = new HashSet<String>(appMethods);
		truePositives.retainAll(expectedAppMethods);
		falsePositives.retainAll(unexpectedAppMethods);
		int total_expected = expectedAppMethods.size();

        System.out.println("True Positives - "+truePositives.size()+"/"+total_expected);
		System.out.println("\nFalse Positives - "+falsePositives.size()+"/"+unexpectedAppMethods.size());

		unexpectedAppMethods.retainAll(appMethods);
		expectedAppMethods.removeAll(appMethods);

		unexpectedAppMethods.stream().sorted().forEach(x->System.out.println(x));

		System.out.println("\nFalse Negatives - "+expectedAppMethods.size()+"/"+total_expected);
		expectedAppMethods.stream().sorted().forEach(x->System.out.println(x));
	}
/*
	@Test
	public void testLogAnalysis2() {
		TamiFlexRunner tamiflex = new TamiFlexRunner(null, null, false);
		String log = new File(JMTraceTest.class.getClassLoader()
			.getResource("tamiflex" + File.separator
				+ "apache_commons_lang_refl.log").getFile()).getAbsolutePath();
		tamiflex.analyze("commons-lang3", log);
		assertEquals(896, tamiflex.accessed_classes.get("commons-lang3").size());
		assertEquals(867, tamiflex.accessed_fields.get("commons-lang3").size());
		assertEquals(4705, tamiflex.used_methods.get("commons-lang3").size());
	}
	
	@Test
	public void testTamiFlexRunner() {
		String project_path = new File(JMTraceTest.class.getClassLoader()
			.getResource("apache_commons-lang").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(JMTraceTest.class.getClassLoader()
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
		String project_path = new File(JMTraceTest.class.getClassLoader()
			.getResource("apache_commons-lang").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(JMTraceTest.class.getClassLoader()
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
		String project_path = new File(JMTraceTest.class.getClassLoader()
				.getResource("tamiflex" + File.separator + "tamiflex-test-project").getFile()).getAbsolutePath();
		String tamiflex_jar_path = new File(JMTraceTest.class.getClassLoader()
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
		String tamiflex_jar_path = new File(JMTraceTest.class.getClassLoader()
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
	/*

 */
	@Test
	public void testJMTraceOnMavenProjectWithMultiSubmodules() {
		// the essentials project has multiple modules compiled but only one module has 
		// real Java class files, the other two only have resources
		String project_path = this.gitGetter.addGitHubProject("greenrobot","essentials",
			"31eaaeb410174004196c9ef9c9469e0d02afd94b").getAbsolutePath();
		JMTraceRunner jmt = new JMTraceRunner(JMTracehome, project_path);
		try {
			jmt.run();
			//assertEquals(1, tamiflex.accessed_classes.size());
			//assertEquals(1, tamiflex.accessed_fields.size());
			/*assertEquals(1, jmt.used_methods.size());
			assertEquals(229, jmt.accessed_classes.get("essentials").size());
			//assertEquals(72, tamiflex.accessed_fields.get("essentials").size());
			// some tests are not deterministic, so the assertion below may fail
			assertEquals(1225, jmt.used_methods.get("essentials").size());*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

