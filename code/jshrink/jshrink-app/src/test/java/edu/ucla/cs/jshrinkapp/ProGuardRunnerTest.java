package edu.ucla.cs.jshrinkapp;

import edu.ucla.cs.proguard.ProGuardRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProGuardRunnerTest {
    @Test
    public void testProGuardRunner() throws IOException, InterruptedException {
        String jarFilePath = ProGuardRunner.class.getClassLoader().getResource("proguard" + File.separator + "junit-4.13.jar").getPath();
        String dependenciesPath = ProGuardRunner.class.getClassLoader().getResource("proguard" + File.separator + "hamcrest-core-1.3.jar").getPath();
        ProGuardRunner runner = new ProGuardRunner();
        runner.run(jarFilePath, dependenciesPath);
    }

    @Test
    public void testBundleClassFiles() throws IOException, InterruptedException {
        String classFilePath = ProGuardRunnerTest.class.getClassLoader().getResource("lambda-test-project" + File.separator + "target" + File.separator + "classes").getPath();
        ArrayList<String> classFiles = new ArrayList<String>();
        classFiles.add("Main.class");
        classFiles.add("NumericTest.class");
        classFiles.add("StandardStuff.class");
        ProGuardRunner runner = new ProGuardRunner();
        File f = new File("test.jar");
        assertFalse(f.exists());
        boolean exitCode = runner.bundleClassFiles(classFilePath, classFiles, f.getAbsolutePath(), false);
        assertTrue(f.exists());
        f.deleteOnExit();
        assertTrue(exitCode);
    }

    @Test
    public void testBundleFolder() throws IOException, InterruptedException {
        String classFilePath = ProGuardRunnerTest.class.getClassLoader().getResource("lambda-test-project" + File.separator + "target").getPath();
        ArrayList<String> classFiles = new ArrayList<String>();
        classFiles.add("classes");
        ProGuardRunner runner = new ProGuardRunner();
        File f = new File("test.jar");
        assertFalse(f.exists());
        boolean exitCode = runner.bundleClassFiles(classFilePath, classFiles, f.getAbsolutePath(), false);
        assertTrue(f.exists());
        f.deleteOnExit();
        assertTrue(exitCode);
    }

    @Test
    public void testBundleSingleModuleMavenProject() throws IOException, InterruptedException {
        String projectDir = ProGuardRunnerTest.class.getClassLoader().getResource("lambda-test-project").getPath();
        ProGuardRunner runner = new ProGuardRunner();
        File f = new File("test.jar");
        assertFalse(f.exists());
        boolean exitCode = runner.bundleMavenProject(projectDir, f.getAbsolutePath());
        assertTrue(f.exists());
        f.deleteOnExit();
        assertTrue(exitCode);
    }

    @Test
    public void testBundleMultiModuleMavenProject() throws IOException, InterruptedException {
        String projectDir = ProGuardRunnerTest.class.getClassLoader().getResource("module-test-project").getPath();
        ProGuardRunner runner = new ProGuardRunner();
        File f = new File("test.jar");
        assertFalse(f.exists());
        boolean exitCode = runner.bundleMavenProject(projectDir, f.getAbsolutePath());
        assertTrue(f.exists());
        f.deleteOnExit();
        assertTrue(exitCode);
    }
}
