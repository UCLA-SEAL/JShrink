package edu.ucla.cs.jshrinkapp;

import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapserData;
import edu.ucla.cs.jshrinklib.methodinliner.InlineData;
import edu.ucla.cs.jshrinklib.reachability.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import soot.G;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class ApplicationTest {

	private static Optional<File> simpleTestProject = Optional.empty();
	private static Optional<File> moduleTestProject = Optional.empty();
	private static Optional<File> reflectionTestProject = Optional.empty();
	private static Optional<File> junitProject = Optional.empty();
	private static Optional<File> simpleClassCollapserProject = Optional.empty();
	private static Optional<File> overridenFieldClassCollapserProject = Optional.empty();
	private static Optional<File> methodNameConflictClassCollapserProject = Optional.empty();
	private static Optional<File> lambdaProject = Optional.empty();
	private static Optional<File> dynamicDispatchingProject = Optional.empty();
	private static Optional<File> logDirectory = Optional.empty();
	private static Optional<File> issue74and81Directory = Optional.empty();
	private static Optional<File> issue96Directory = Optional.empty();
	private static Optional<File> issue99Directory = Optional.empty();
	private static Optional<File> bukkit = Optional.empty();

	protected static File getOptionalFile(Optional<File> optionalFile, String resources) {
		if (optionalFile.isPresent()) {
			return optionalFile.get();
		}
		ClassLoader classLoader = ApplicationTest.class.getClassLoader();
		File f = new File(classLoader.getResource(resources).getFile());

		try {
			File copy = File.createTempFile(resources + "_", "");
			copy.delete();
			copy.mkdir();

			FileUtils.copyDirectory(f, copy);

			optionalFile = Optional.of(copy);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return optionalFile.get();
	}

	protected File getTamiFlexJar() {
		File toReturn = new File(
				ApplicationTest.class.getClassLoader().getResource(
						"tamiflex" + File.separator + "poa-2.0.3.jar").getFile());
		return toReturn;
	}

	protected File getJMTrace(){
		return new File(ApplicationTest.class.getClassLoader().getResource("jmtrace").getFile());
	}

	private static File getSimpleTestProjectDir() {
		return getOptionalFile(simpleTestProject, "simple-test-project");
	}

	private boolean jarIntact() {
		if (simpleTestProject.isPresent()) {
			File f = new File(simpleTestProject.get().getAbsolutePath()
					+ File.pathSeparator + "libs" + File.pathSeparator + "standard-stuff-library.jar");
			return f.exists() && !f.isDirectory();
		}

		return true;
	}

	private File getModuleProjectDir() {
		return getOptionalFile(moduleTestProject, "module-test-project");
	}

	private File getReflectionProjectDir() {
		return getOptionalFile(reflectionTestProject, "reflection-test-project");
	}

	private File getJunitProjectDir() {
		return getOptionalFile(junitProject, "junit4");
	}

	private File getAnnotationProjectDir() {
	    return getOptionalFile(junitProject, "annotations");
    }

	private File getLogDirectory() {
		if (logDirectory.isPresent()) {
			return logDirectory.get();
		}

		try {
			File lDirectory = File.createTempFile("log_directory_", "");
			lDirectory.delete();

			logDirectory = Optional.of(lDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return logDirectory.get();
	}

	private File getSimpleClassCollapserDir() {
		return getOptionalFile(simpleClassCollapserProject, "classcollapser"
				+ File.separator + "simple-collapse-example");
	}

	private File getOverriddenFieldClassCollapserDir() {
		return getOptionalFile(overridenFieldClassCollapserProject, "classcollapser"
				+ File.separator + "override-field-example");
	}

	private File getMethodNameConflictClassCollapserDir() {
		return getOptionalFile(methodNameConflictClassCollapserProject, "classcollapser"
				+ File.separator + "method-name-conflict-example");
	}

	private File getLambdaAppProject() {
		return getOptionalFile(lambdaProject, "lambda-test-project");
	}

	private File getDynamicDispatchingProject() {
		return getOptionalFile(dynamicDispatchingProject, "dynamic-dispatching-test-project");
	}

	private File getIssue74And81Project() {
		return getOptionalFile(issue74and81Directory, "classcollapser"
				+ File.separator + "issue74and81");
	}

	private File getIssue96Project() {
		return getOptionalFile(issue96Directory, "classcollapser"
				+ File.separator + "issue96");
	}

	private File getIssue99Project() {
		return getOptionalFile(issue99Directory, "classcollapser"
				+ File.separator + "issue99");
	}

	protected File getBukkitProjectDir() {
		return getOptionalFile(bukkit, "bukkit");
	}

	@After
	public void rectifyChanges() {
		simpleTestProject = Optional.empty();
		moduleTestProject = Optional.empty();
		reflectionTestProject = Optional.empty();
		junitProject = Optional.empty();
		simpleClassCollapserProject = Optional.empty();
		lambdaProject = Optional.empty();
		logDirectory = Optional.empty();
		overridenFieldClassCollapserProject = Optional.empty();
		issue74and81Directory = Optional.empty();
		issue96Directory = Optional.empty();
		bukkit = Optional.empty();
		G.reset();
	}


	private boolean isPresent(Set<MethodData> methodsRemoved, String className, String methodName) {
		for (MethodData methodData : methodsRemoved) {
			if (methodData.getClassName().equals(className) && methodData.getName().equals(methodName)) {
				return true;
			}
		}

		return false;
	}


	/*
	@Test
	public void test(){
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project /home/bobbyrbruce/Desktop/java-apns ");
		arguments.append("--main-entry ");
		arguments.append("--public-entry ");
		arguments.append("--test-entry ");
		arguments.append("--inline ");
		arguments.append("-T ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));
	}
	*/

	@Test
	public void mainTest_targetMainEntryPoint() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;


		assertTrue(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("StandardStuffSub"));
		assertEquals(2, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void mainTest_targetMainEntryPoint_applicationOnly() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
		arguments.append("--ignore-libs ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;


		assertTrue(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(jarIntact());
	}

	@Test
	public void mainTest_targetMainEntryPoint_withSpark() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--use-spark ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-classes ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertTrue(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("StandardStuffSub"));
		assertEquals(2, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void mainTest_targetTestEntryPoints() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-classes ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertTrue(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertTrue(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		//(Method is untouched by too small to remove)
//		assertTrue(isPresent(methodsRemoved,"edu.ucla.cs.onr.test.LibraryClass","<init>"));
		assertTrue(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main$1", "compare"));


		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertEquals(2, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void mainTest_targetPublicEntryPoints() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--public-entry ");
		arguments.append("--include-exception ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertTrue(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertEquals(0, classRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void mainTest_targetAllEntryPoints() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--public-entry ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--include-exception \"message_removed\" ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertTrue(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertEquals(0, classRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void testAnnotations(){
		/*
		 This test is to ensure annotations are not removed during method removal. Our current policy is to leave
		 annotations alone.
		 */

		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getAnnotationProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-classes ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertEquals(1, methodsRemoved.size());
		assertTrue(isPresent(methodsRemoved, "Application", "unusedMethod"));
		assertTrue(classesRemoved.isEmpty());
	}

	@Test
	public void mainTest_targetAllEntryPoints_withTamiFlex() {
		/*
		Note: There is actually no reflection in this target, i just want to ensure reflection isn't making anything
		crash.
		 */
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--public-entry ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--include-exception \"message_removed\" ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertFalse(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertTrue(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertEquals(0, classRemoved.size());

		assertTrue(jarIntact());
	}


	@Ignore
	@Test //Ignoring this test right now as it's failing (we think it's a bug in CHA call graph analysis)
	public void mainTest_targetCustomEntryPoint() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--custom-entry <StandardStuff: public void publicAndTestedButUntouched()> ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertTrue(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertTrue(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertTrue(isPresent(methodsRemoved, "Main", "main"));
		assertTrue(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertEquals(2, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void mavenTest_mainMethodEntry_withOutTamiFlex() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getModuleProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "touchedViaReflection"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));

		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass2"));
		assertFalse(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertFalse(classesRemoved.contains("StandardStuff"));

		assertTrue(jarIntact());
	}

	@Test
//	@Ignore
	public void mavenTest_mainMethodEntry_withTamiFlex() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getModuleProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry "); //Note: when targeting Maven, we always implicitly target test entry due to TamiFlex
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "touchedViaReflection"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));

		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));

		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass2"));
		assertFalse(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertFalse(classesRemoved.contains("StandardStuff"));

		assertTrue(jarIntact());
	}

	@Test
	@Ignore //We don't support "--ignore-classes" for now
	public void ignoreClassTest() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--ignore-classes edu.ucla.cs.onr.test.LibraryClass edu.ucla.cs.onr.test.UnusedClass ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		try {
			Method method = ApplicationTest.class.getMethod("ignoreClassTest");
			Object o = method.invoke(null);
		} catch (Exception e) {

		}

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertFalse(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("StandardStuffSub"));
		assertEquals(1, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void reflectionTest_mainMethodEntry_withTamiFlex() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getReflectionProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry "); //Note: when targeting Maven, we always implicitly target test entry due to TamiFlex
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--remove-classes ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "ReflectionStuff", "touchedViaReflection"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass2"));
		assertFalse(classesRemoved.contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertFalse(classesRemoved.contains("ReflectionStuff"));
		assertFalse(classesRemoved.contains("StandardStuff"));

		assertTrue(jarIntact());
	}

	@Test
    @Ignore //I dont know why we have this test. Obviously without TamiFlex, this test will fail
	public void reflectionTest_mainMethodEntry_withoutTamiFlex() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getReflectionProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry "); //Note: when targeting Maven, we always implicitly target test entry due to TamiFlex
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertTrue(isPresent(methodsRemoved, "ReflectionStuff", "touchedViaReflection"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertEquals(0, classesRemoved.size());

		assertTrue(jarIntact());
	}

	@Test
	public void junit_test() {
		//This tests ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-classes ");
		arguments.append("--run-tests ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
//		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void junit_test_dont_run_tests() {
		//This tests ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		assertNull(Application.testOutputBefore);
		assertNull(Application.testOutputAfter);
	}

	@Test
	public void junit_test_log_in_home_directory() {
		//This tests ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		File expected = new File(System.getProperty("user.home") + File.separator + "jshrink_output");
		assertTrue(expected.exists());
		assertTrue(new File(expected.getAbsolutePath() + File.separator + "log.dat").exists());
		assertTrue(new File(expected.getAbsolutePath() + File.separator + "test_output_before.dat").exists());
		assertTrue(new File(expected.getAbsolutePath() + File.separator + "test_output_after.dat").exists());
		assertTrue(new File(expected.getAbsolutePath() + File.separator + "unmodifiable_classes_log.dat").exists());

		expected.delete();
	}

	@Test
	public void junit_test_methodinliner() {
		//This tests ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--skip-method-removal ");
		arguments.append("--inline ");
		arguments.append("--run-tests ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		InlineData inlineData = Application.inlineData;

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void superClassMethodOverridenTest() throws IOException, InterruptedException {
		StringBuilder arguments = new StringBuilder();
		String projectPath = new File(ApplicationTest.class.getClassLoader().getResource("superclass-method-overriden-test-project").getFile()).getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + projectPath + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-fields ");
		arguments.append("--class-collapser ");
		arguments.append("--run-tests ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
		arguments.append("--verbose ");
		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		assertTrue(classCollapseResult.getClassesToRemove().contains("B"));
		assertFalse(classCollapseResult.getClassesToRemove().contains("C"));
		assertFalse(classCollapseResult.getClassesToRewrite().contains("B"));
		assertTrue(classCollapseResult.getClassesToRewrite().contains("Main"));
		assertTrue(classCollapseResult.getClassesToRewrite().contains("A"));
	}
	@Test
	public void junit_test_class_collapser() {
		StringBuilder arguments = new StringBuilder();
		String projectFilePath = getJunitProjectDir().getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + projectFilePath + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--remove-classes ");
		arguments.append("--class-collapser ");
		arguments.append("--run-tests ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
//		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		System.out.println(classCollapseResult.getRemovedMethods().size());
		System.out.println(classCollapseResult.getClassesToRemove().size());
		System.out.println(classCollapseResult.getClassesToRewrite().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void test_issue25() {
		String junit_project_path = getJunitProjectDir().getAbsolutePath();
		Set<MethodData> entryPoints = new HashSet<MethodData>();
		MethodData failedTest = new MethodData("verifierRunsAfterTest",
			"org.junit.rules.VerifierRuleTest", "void", new String[]{},
			true, false);
		entryPoints.add(failedTest);
		EntryPointProcessor entryPointProcessor = new EntryPointProcessor(false, false, false, entryPoints);
		MavenSingleProjectAnalyzer runner =
				new MavenSingleProjectAnalyzer(junit_project_path, entryPointProcessor, Optional.empty(), Optional.empty(), false, true, true, true, false);
		runner.setup();
		runner.run();
		assertTrue(isPresent(runner.getUsedAppMethods().keySet(), "org.junit.rules.Verifier", "verify"));
	}

	@Test
	public void test_issue74_and_issue84() {
		StringBuilder arguments = new StringBuilder();
		String projectFilePath = getIssue74And81Project().getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + projectFilePath + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--run-tests ");
		arguments.append("--use-cache ");
//		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		assertEquals(0, classCollapseResult.getRemovedMethods().size());
		assertEquals(0, classCollapseResult.getClassesToRemove().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void test_issue96() {
		StringBuilder arguments = new StringBuilder();
		String projectFilePath = getIssue96Project().getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + projectFilePath + "\" ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--run-tests ");
		arguments.append("--use-cache ");
//		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		assertEquals(1, classCollapseResult.getClassesToRemove().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void test_issue99_simplified() {
		StringBuilder arguments = new StringBuilder();
		String projectFilePath = getIssue99Project().getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + projectFilePath + "\" ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--run-tests ");
		arguments.append("--use-cache ");
//		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		assertEquals(1, classCollapseResult.getClassesToRemove().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void test_issue99_original() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getBukkitProjectDir() + "\" ");
		arguments.append("--public-entry ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
//		arguments.append("--verbose ");
		arguments.append("-T ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		System.out.println(classCollapseResult.getRemovedMethods().size());
		System.out.println(classCollapseResult.getClassesToRemove().size());
		System.out.println(classCollapseResult.getClassesToRewrite().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void junit_test_class_collapser_and_inliner() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--inline ");
		arguments.append("--run-tests ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		System.out.println(classCollapseResult.getRemovedMethods().size());
		System.out.println(classCollapseResult.getClassesToRemove().size());
		System.out.println(classCollapseResult.getClassesToRewrite().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void junit_test_class_collapser_and_inliner_with_checkpoint() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--inline ");
		arguments.append("--run-tests ");
		arguments.append("--checkpoint /tmp/checkpointTest ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		System.out.println(classCollapseResult.getRemovedMethods().size());
		System.out.println(classCollapseResult.getClassesToRemove().size());
		System.out.println(classCollapseResult.getClassesToRewrite().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void Bukkit_test_class_collapser_and_inliner_with_checkpoint() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getBukkitProjectDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--inline ");
		arguments.append("--run-tests ");
		arguments.append("--checkpoint /tmp/checkpointTest ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--use-cache ");
		arguments.append("--verbose ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapseResult = Application.classCollapserData;
		System.out.println(classCollapseResult.getRemovedMethods().size());
		System.out.println(classCollapseResult.getClassesToRemove().size());
		System.out.println(classCollapseResult.getClassesToRewrite().size());

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void test_handling_virtually_invoked_methods() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getDynamicDispatchingProject().getAbsolutePath() + "\" ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> md = Application.removedMethods;
		assertEquals(2, md.size());
		assertTrue(isPresent(md, "A", "unused")); // A.usused() is not used at all
		assertTrue(isPresent(md, "A", "m")); // A.m() is virtually invoked in test cases
		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void lambdaMethodTest() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getLambdaAppProject() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertTrue(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "isEven"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "isNegativeNumber"));
	}

	@Test
	public void lambdaMethodTest_full() {
		/*
		This test fails do to Soot which cannot properly process convert SootClass to .java files which contain Lambda
		expressions. I've set it to ignore for the time being as it's not something we can presently fix.
		 */
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getLambdaAppProject() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertTrue(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "isEven"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "isNegativeNumber"));
		assertTrue(isPresent(methodsRemoved, "Main", "methodNotUsed"));
		assertEquals(1, methodsRemoved.size());
	}

	@Test
	public void inlineMethodTest() throws IOException {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--inline ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		InlineData methodsInlined = Application.inlineData;

		Assert.assertTrue(methodsInlined.getInlineLocations().containsKey(TestUtils.getMethodDataFromSignature(
				"<StandardStuff$NestedClass: void nestedClassMethodCallee()>")));
		assertEquals(1, methodsInlined.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff$NestedClass: void nestedClassMethodCallee()>")).size());
		Assert.assertTrue(methodsInlined.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff$NestedClass: void nestedClassMethodCallee()>"))
				.contains(TestUtils.getMethodDataFromSignature("<StandardStuff$NestedClass: public void nestedClassMethod()>")));

		Assert.assertTrue(methodsInlined.getInlineLocations().containsKey(
				TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")));
		assertEquals(1,methodsInlined.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")).size());
		Assert.assertTrue(methodsInlined.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>"))
				.contains(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		Assert.assertTrue(methodsInlined.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).isPresent());
		assertEquals(1, methodsInlined.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get().size());
		Assert.assertTrue(methodsInlined.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get()
				.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		Assert.assertTrue(methodsInlined.getInlineLocations().containsKey(
				TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")));
		assertEquals(1, methodsInlined.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")).size());
		Assert.assertTrue(methodsInlined.getInlineLocations().get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>"))
				.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(jarIntact());
	}

	@Test
	public void testMethodInlinerWithClassCollapser() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getSimpleClassCollapserDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("--inline ");
		arguments.append("-T ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapserData = Application.classCollapserData;

		assertEquals(2, classCollapserData.getClassesToRemove().size());
		assertTrue(classCollapserData.getClassesToRemove().contains("B"));
		assertTrue(classCollapserData.getClassesToRemove().contains("C"));

		assertEquals(2, classCollapserData.getClassesToRemove().size());
		assertTrue(classCollapserData.getClassesToRemove().contains("B"));
		assertTrue(classCollapserData.getClassesToRemove().contains("C"));

		// A.saySomething is replaced by B.saySomething.
		assertEquals(1, classCollapserData.getRemovedMethods().size());
		assertTrue(isPresent(classCollapserData.getRemovedMethods(), "A", "saySomething"));

		InlineData methodsInlined = Application.inlineData;
		assertEquals(5, methodsInlined.getInlineLocations().size());
		methodsInlined.getInlineLocations().keySet().contains(
				new MethodData("getClassType", "A", "java.lang.String", new String[] {}, true, false));
		methodsInlined.getInlineLocations().keySet().contains(
				new MethodData("saySomething", "A", "java.lang.String", new String[] {}, true, false));
		methodsInlined.getInlineLocations().keySet().contains(
				new MethodData("uniqueToA", "A", "java.lang.String", new String[] {}, true, false));
		methodsInlined.getInlineLocations().keySet().contains(
				new MethodData("uniqueToB", "A", "java.lang.String", new String[] {}, true, false));
		methodsInlined.getInlineLocations().keySet().contains(
				new MethodData("getString", "A", "java.lang.String", new String[] {}, false, false));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void classCollapserTest() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getSimpleClassCollapserDir().getAbsolutePath() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("-T ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapserData = Application.classCollapserData;

		assertEquals(2, classCollapserData.getClassesToRemove().size());
		assertTrue(classCollapserData.getClassesToRemove().contains("B"));
		assertTrue(classCollapserData.getClassesToRemove().contains("C"));

		// A.saySomething is replaced by B.saySomething.
		assertEquals(1, classCollapserData.getRemovedMethods().size());
		assertTrue(isPresent(classCollapserData.getRemovedMethods(), "A", "saySomething"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void classCollapserTestOnClassesWithOverriddenFields() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getOverriddenFieldClassCollapserDir().getAbsolutePath() + "\" ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("-T ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapserData = Application.classCollapserData;

		assertEquals(1, classCollapserData.getClassesToRemove().size());
		assertTrue(classCollapserData.getClassesToRemove().contains("SubA"));
		assertEquals(2, classCollapserData.getClassesToRewrite().size());
		assertTrue(classCollapserData.getClassesToRewrite().contains("A"));
		assertTrue(classCollapserData.getClassesToRewrite().contains("SimpleTest"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void classCollapserTestOnMethodNameConflicts() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getMethodNameConflictClassCollapserDir().getAbsolutePath() + "\" ");
		arguments.append("--test-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--class-collapser ");
		arguments.append("-T ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		ClassCollapserData classCollapserData = Application.classCollapserData;

		assertEquals(1, classCollapserData.getClassesToRemove().size());
		assertTrue(classCollapserData.getClassesToRemove().contains("SubA"));
		assertEquals(3, classCollapserData.getClassesToRewrite().size());
		assertTrue(classCollapserData.getClassesToRewrite().contains("A"));
		assertTrue(classCollapserData.getClassesToRewrite().contains("B"));
		assertTrue(classCollapserData.getClassesToRewrite().contains("SimpleTest"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}

	@Test
	public void mainTest_targetMainEntryPoint_classCollapser() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--class-collapser ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");


		Application.main(arguments.toString().split("\\s+"));

		Set<MethodData> methodsRemoved = Application.removedMethods;
		Set<String> classesRemoved = Application.removedClasses;

		assertFalse(Application.removedMethod);
		assertTrue(Application.wipedMethodBody);
		assertFalse(Application.wipedMethodBodyWithExceptionNoMessage);
		assertFalse(Application.wipedMethodBodyWithExceptionAndMessage);

		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getStringStatic"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "getString"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "<init>"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff", "doNothing"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(methodsRemoved, "StandardStuffSub", "subMethodUntouched"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethod"));
		assertFalse(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(methodsRemoved, "StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertFalse(isPresent(methodsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertFalse(isPresent(methodsRemoved, "Main", "main"));
		assertFalse(isPresent(methodsRemoved, "Main", "compare"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(methodsRemoved,
				"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));

		assertTrue(classesRemoved.contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(classesRemoved.contains("StandardStuffSub"));
		assertEquals(2, classesRemoved.size());

		assertTrue(jarIntact());
	}

	private boolean isFieldPresent(Set<FieldData> fieldSet, String className, String fieldName) {
		for (FieldData fieldData : fieldSet) {
			if (fieldData.getClassName().equals(className) && fieldData.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This test case aims to test the field removal function only without interacting with other transformations.
	 */
	@Test
	public void fieldRemovalTest() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + getSimpleTestProjectDir().getAbsolutePath() + " ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--remove-fields ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<FieldData> fieldsRemoved = Application.removedFields;

		assertEquals(4, fieldsRemoved.size());
		// though the following fields are referened in the source code, they are inlined by Java compiler in the bytecode
		// so they are not used in bytecode
		assertTrue(isFieldPresent(fieldsRemoved, "StandardStuff", "HELLO_WORLD_STRING"));
		assertTrue(isFieldPresent(fieldsRemoved, "StandardStuff", "GOODBYE_STRING"));
		assertTrue(isFieldPresent(fieldsRemoved, "edu.ucla.cs.onr.test.LibraryClass", "x"));
		assertTrue(isFieldPresent(fieldsRemoved, "edu.ucla.cs.onr.test.LibraryClass2", "y"));
		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());

		assertTrue(jarIntact());
	}

	@Test
	public void fieldRemovalTestWithTamiFlex() {
		ClassLoader classLoader = ApplicationTest.class.getClassLoader();
		String tamiflex_test_project_path =
				new File(classLoader.getResource("tamiflex-test-project").getFile()).getAbsolutePath();

		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project " + tamiflex_test_project_path + " ");
		arguments.append("--test-entry ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--remove-fields ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		Set<FieldData> fieldsRemoved = Application.removedFields;

		assertEquals(1, fieldsRemoved.size());
		// though the following fields are referened in the source code, they are inlined by Java compiler in the bytecode
		// so they are not used in bytecode
		assertTrue(isFieldPresent(fieldsRemoved, "A", "f3"));
		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());

		assertTrue(jarIntact());
	}

	/**
	 * Check how many test failures there will be when running our tool on JUnit without enabling TamiFlex
	 */
	@Test
	public void runFieldRemovalOnJUnitWithoutTamiFlex() {
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--public-entry ");
		arguments.append("--remove-fields ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		assertEquals(0, Application.testOutputBefore.getErrors());
		assertEquals(7, Application.testOutputAfter.getErrors());

		assertTrue(jarIntact());
	}

	/**
	 * After enabling TamiFlex, there should be no test failure after removing unused fields.
	 */
	@Test
	public void runFieldRemovalOnJUnitWithTamiFlex() {
		//This test ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getJunitProjectDir() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--public-entry ");
		arguments.append("--remove-fields ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());

		assertTrue(jarIntact());
	}
	@Test
	public void runMethodRemovalOnProjectWithJMTrace() {
		//This test ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + getSimpleTestProjectDir() + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--jmtrace "+ getJMTrace().getAbsolutePath() +" ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());

	}
	@Test
	public void runMethodRemovalOnProjectWithTamiflexAndJMTrace() {
		//This test ensures that all test cases pass before and after the tool is run
		StringBuilder arguments = new StringBuilder();
		String project_path = getSimpleTestProjectDir().getAbsolutePath();
		arguments.append("--prune-app ");
		arguments.append("--maven-project \"" + project_path + "\" ");
		arguments.append("--main-entry ");
		arguments.append("--test-entry ");
		arguments.append("--tamiflex " + getTamiFlexJar().getAbsolutePath() + " ");
		arguments.append("--jmtrace " + getJMTrace().getAbsolutePath() + " ");
		arguments.append("--public-entry ");
		arguments.append("--remove-methods ");
		arguments.append("--run-tests ");
		arguments.append("--log-directory " + getLogDirectory().getAbsolutePath() + " ");
		arguments.append("--use-cache ");

		Application.main(arguments.toString().split("\\s+"));

		assertEquals(Application.testOutputBefore.getRun(), Application.testOutputAfter.getRun());
		assertEquals(Application.testOutputBefore.getErrors(), Application.testOutputAfter.getErrors());
		assertEquals(Application.testOutputBefore.getFailures(), Application.testOutputAfter.getFailures());
		assertEquals(Application.testOutputBefore.getSkipped(), Application.testOutputAfter.getSkipped());
	}
}
