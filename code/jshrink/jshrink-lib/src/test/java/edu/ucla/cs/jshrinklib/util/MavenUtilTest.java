package edu.ucla.cs.jshrinklib.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

public class MavenUtilTest {
	@Test
	public void test1() {
		String root_path = "src/test/resources/square_okhttp";
		File root_dir = new File(root_path);
		HashMap<String, File> modules = new HashMap<String, File>();
		MavenUtils.getModules(root_dir, modules);
		assertEquals(19, modules.size());
	}
}
