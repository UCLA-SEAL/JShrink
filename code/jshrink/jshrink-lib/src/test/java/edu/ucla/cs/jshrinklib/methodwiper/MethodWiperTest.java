package edu.ucla.cs.jshrinklib.methodwiper;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import edu.ucla.cs.jshrinklib.TestUtils;
import edu.ucla.cs.jshrinklib.reachability.CallGraphAnalysisSimpleTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Ignore;
import soot.*;

import java.io.*;

import org.junit.Test;
import soot.options.Options;

public class MethodWiperTest {
	private static SootClass getSootClassFromResources(String className){
		File classFile = new File(MethodWiperTest.class
			.getClassLoader().getResource("methodwiper" + File.separator + className + ".class").getFile());

		final String workingClasspath=classFile.getParentFile().getAbsolutePath();
		return TestUtils.getSootClass(workingClasspath, className);
	}

	@After
	public void before(){
		G.reset();
	}

	@Test
	public void sanityCheck() {
		SootClass sootClass = getSootClassFromResources("Test");
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_Test1_staticVoidMethodNoParams(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticVoidMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticIntMethodNoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticIntMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticStringMethodNoParam() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticStringMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticDoubleMethodNoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticDoubleMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticVoidMethodTwoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticVoidMethodTwoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticIntMethodTwoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticIntMethodTwoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_methodNoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("methodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_intMethodNoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("intMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_intMethodTwoParams() {
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("intMethodTwoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticBooleanMethodNoParams(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticBooleanMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticCharMethodNoParams(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticCharMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticByteMethodNoParams(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticByteMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticShortMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}

	@Test
	public void wipeMethodBodyTest_staticShortMethodNoParams(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBody(sootClass.getMethodByName("staticShortMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();

		assertEquals(expected, output);
	}


	@Test
	public void wipeMethodBodyAndInsertRuntimeException_TestWithMessage(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.wipeMethodBodyAndInsertRuntimeException(
			sootClass.getMethodByName("intMethodTwoParams"), "TEST"));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "Exception in thread \"main\" java.lang.RuntimeException: TEST" + System.lineSeparator() +
			"\tat Test.intMethodTwoParams(Test.java)" + System.lineSeparator() +
			"\tat Test.main(Test.java)" + System.lineSeparator();

		assertEquals(expected, output);
	}

	//Interfaces don't ahve bodies to delete, therefore these cases should be handled gracefully

	@Test
	public void wipeInterfaceMethodBody_noReturnTypeNoParameter(){
		SootClass sootClass = getSootClassFromResources("InterfaceTest");
		assertFalse(MethodWiper.wipeMethodBody(sootClass.getMethodByName("interface1")));
	}

	@Test
	public void wipeInterfaceMethodBody_stringReturnTypeNoParameter(){
		SootClass sootClass = getSootClassFromResources("InterfaceTest");
		assertFalse(MethodWiper.wipeMethodBodyAndInsertRuntimeException(sootClass.getMethodByName("interface2"), "TEST"));
	}

	@Test
	public void wipeInterfaceMethodBody_noReturnTypeOneParameter(){
		SootClass sootClass = getSootClassFromResources("InterfaceTest");
		assertFalse(MethodWiper.wipeMethodBodyAndInsertRuntimeException(sootClass.getMethodByName("interface3")));
	}

	@Test
	public void removeDefaultInterfaceMethod(){
		SootClass sootClass = getSootClassFromResources("InterfaceTest");
		assertTrue(MethodWiper.removeMethod(sootClass.getMethodByName("defaultMethod")));
	}

	@Test
	public void wipeTinyMethodBody(){
		SootClass sootClass = getSootClassFromResources("TinyMethodTest");
		assertFalse(MethodWiper.wipeMethodBodyAndInsertRuntimeException(sootClass.getMethodByName("getNum")
			,"THIS IS A PURPOSELY LONG EXCEPTION SO THAT THE EXCEPTION CODE IS BIGGER THAN WHAT IT REPLACES"));
	}

	@Test
	public void wipeNativeMethodBody(){
		SootClass sootClass = getSootClassFromResources("CornerCases");
		assertFalse(MethodWiper.wipeMethodBody(sootClass.getMethodByName("readByte")));
	}

	@Test
	public void removeEntireMethod(){
		SootClass sootClass = getSootClassFromResources("Test");
		assertTrue(MethodWiper.removeMethod(sootClass.getMethodByName("staticShortMethodNoParams")));
		String output = TestUtils.runClass(sootClass);

		String expected = "staticVoidMethodNoParams touched" + System.lineSeparator();
		expected += "staticIntMethodNoParams touched" + System.lineSeparator();
		expected += "staticStringMethodNoParams touched" + System.lineSeparator();
		expected += "staticDoubleMethodNoParams touched" + System.lineSeparator();
		expected += "staticVoidMethodTwoParams touched" + System.lineSeparator();
		expected += "staticIntMethodTwoParams touched" + System.lineSeparator();
		expected += "methodNoParams touched" + System.lineSeparator();
		expected += "intMethodNoParams touched" + System.lineSeparator();
		expected += "intMethodTwoParams touched" + System.lineSeparator();
		expected += "staticBooleanMethodNoParams touched" + System.lineSeparator();
		expected += "staticCharMethodNoParams touched" + System.lineSeparator();
		expected += "staticByteMethodNoParams touched" + System.lineSeparator();
//		expected += "Exception in thread \"main\" java.lang.NoSuchMethodError: Test.staticShortMethodNoParams()Ljava/lang/Short;\n" +
//				"\tat Test.main(Test.java)" + System.lineSeparator();
		// Note that after removing this method, Soot will automatically add a dummy method back if that removed method is called somewhere else
		// The dummy method will throw an exception inside
		expected += "Exception in thread \"main\" java.lang.Error: Unresolved compilation error: Method <Test: java.lang.Short staticShortMethodNoParams()> does not exist!\n" +
				"\tat Test.staticShortMethodNoParams(Test.java)\n" +
				"\tat Test.main(Test.java)\n";

		assertEquals(expected, output);
	}

	@Test @Ignore
	public void removeReferencedMethod() throws IOException{
		ClassLoader classLoader = CallGraphAnalysisSimpleTest.class.getClassLoader();
		File project = new File(classLoader.getResource("method-reference-project").getFile());
		File projectCopy = File.createTempFile("method-reference-project", "");
		projectCopy.delete();
		FileUtils.copyDirectory(project, projectCopy);

		File classFile = new File(projectCopy.getAbsolutePath() + File.separator + "target/classes/References.class");

		final String workingClasspath=classFile.getParentFile().getAbsolutePath();
		SootClass referenceClass = TestUtils.getSootClass(workingClasspath, "References");
		assertTrue(MethodWiper.removeMethod(referenceClass.getMethodByName("unusedMethodReference")));

		FileUtils.copyFile(TestUtils.createClass(referenceClass), classFile);
		String output = TestUtils.runClass(workingClasspath, "Main");

		assertEquals("usedMethodReference has been called by main" + System.lineSeparator(), output);
	}
}