package edu.ucla.cs.jshrinklib.reachability;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CallGraphAnalysisCacheWrapperTest {

	private File project;

	@Before
	public void before(){
		File original = new File(CallGraphAnalysisCacheWrapper.class.getClassLoader()
			.getResource("simple-test-project2").getFile());

		try {
			this.project = File.createTempFile("simple-test-project2_", "");
			this.project.delete();
			FileUtils.copyDirectory(original, this.project);
			File cacheDir = new File(this.project.getAbsolutePath() + File.separator + "jshrink_caches");
			if(cacheDir.exists()) {
				FileUtils.deleteDirectory(cacheDir);
			}
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Test
	public void testCache(){
		ClassLoader classLoader = CallGraphAnalysisCacheWrapper.class.getClassLoader();
		List<File> libJarPath = new ArrayList<File>();
		List<File> appClassPath = new ArrayList<File>();
		appClassPath.add(new File(
			this.project.getAbsolutePath() + File.separator + "target"
				+ File.separator + "classes"));
		List<File> appTestPath = new ArrayList<File>();
		appTestPath.add(new File(
			this.project.getAbsolutePath() + File.separator + "target"
				+ File.separator + "test-classes"));

		EntryPointProcessor entryPointProcessor =
			new EntryPointProcessor(true, false, false, new HashSet<MethodData>());

		CallGraphAnalysisCacheWrapper callGraphAnalysisCacheWrapper =
			new CallGraphAnalysisCacheWrapper(this.project, "module", libJarPath, appClassPath,
				appTestPath,entryPointProcessor, false, true, false);

		assertFalse(callGraphAnalysisCacheWrapper.getCache().isPresent());

		callGraphAnalysisCacheWrapper.setup();
		callGraphAnalysisCacheWrapper.run();

		assertTrue(callGraphAnalysisCacheWrapper.getCache().isPresent());

		CallGraphAnalysisCacheWrapper callGraphAnalysisCacheWrapper2 =
			new CallGraphAnalysisCacheWrapper(this.project, "module", libJarPath, appClassPath,
				appTestPath,entryPointProcessor, false, true, false);

		assertTrue(callGraphAnalysisCacheWrapper2.getCache().isPresent());

		callGraphAnalysisCacheWrapper2.setup();
		callGraphAnalysisCacheWrapper2.run();

		assertTrue(callGraphAnalysisCacheWrapper2.getCache().isPresent());
	}
}
