package edu.ucla.cs.jshrinklib.util;

import static org.junit.Assert.*;

import edu.ucla.cs.jshrinklib.reachability.JMTraceRunner;
import edu.ucla.cs.jshrinklib.reachability.JMTraceTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PomUtilTest {
	@Test
	public void test1() {
		String path = new File(PomUtilTest.class.getClassLoader()
			.getResource("activiti-bpmn-converter-pom.xml").getFile()).getAbsolutePath();
		String id = POMUtils.getArtifactId(path);
		assertEquals("activiti-json-converter", id);
	}

	@Test
	public void testNameExclusionTest(){
		String pom_file = "src/test/resources/tamiflex/hankcs_HanLP_pom.xml";

		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		try {
			FileUtils.copyFile(file, copy);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<String> testNames = new ArrayList<>();
		testNames.add("a.b.c");
		testNames.add("x.y.z");
		POMUtils.addTestExclusionsToPOM(testNames, file);


		// restore the pom file
		try {
			FileUtils.copyFile(copy, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		copy.delete();
	}

	@Test
	public void testNameExclusionWithJMtraceTest(){
		String pom_file = "src/test/resources/tamiflex/hankcs_HanLP_pom.xml";

		// save a copy of the pom file
		File file = new File(pom_file);
		File copy = new File(file.getAbsolutePath() + ".tmp");
		try {
			FileUtils.copyFile(file, copy);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// inject tamiflex as a java agent in the surefire test plugin
		JMTraceRunner jmtrace = new JMTraceRunner(new File(JMTraceTest.class.getClassLoader().getResource("jmtrace").getFile()).getAbsolutePath(), null);
		jmtrace.injectJMTrace(file);


		ArrayList<String> testNames = new ArrayList<>();
		testNames.add("a.b.c");
		testNames.add("x.y.z");
		POMUtils.addTestExclusionsToPOM(testNames, file);
		// restore the pom file
		try {
			FileUtils.copyFile(copy, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		copy.delete();
	}
}
