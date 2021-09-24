package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.reachability.ASMClassVisitor;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class MethodDataTest {

	@Test
	public void methodDataBasicTest(){
		String methodName = "method_1";
		String className = "com.example.Class1";
		String methodReturnType = "void";
		String[] args = new String[3];
		args[0] = "java.util.String";
		args[1] = "com.example.Class2";
		args[2] = "int";
		boolean isPublic = true;
		boolean isStatic = true;
		MethodData methodData = new MethodData(methodName, className, methodReturnType, args, isPublic, isStatic);

		assertTrue(methodData.getAnnotation().isEmpty());
		assertEquals(methodName, methodData.getName());
		assertEquals(className, methodData.getClassName());
		assertEquals(methodReturnType, methodData.getReturnType());
		assertEquals(args.length, methodData.getArgs().length);
		for(int i=0; i< args.length; i++){
			assertEquals(args[i], methodData.getArgs()[i]);
		}
		assertEquals(isPublic, methodData.isPublic());
		assertEquals(isStatic, methodData.isStatic());
	}

	@Test
	public void methodDataToStringTest(){
		String methodName = "method_1";
		String className = "com.example.Class1";
		String methodReturnType = "void";
		String[] args = new String[3];
		args[0] = "java.util.String";
		args[1] = "com.example.Class2";
		args[2] = "int";
		boolean isPublic = true;
		boolean isStatic = true;
		MethodData methodData = new MethodData(methodName, className, methodReturnType, args, isPublic, isStatic);

		String expected = "<com.example.Class1: public static void method_1(java.util.String,com.example.Class2,int)>";
		assertEquals(expected, methodData.toString());
	}

	@Test
	public void methodDataAnnotationTest(){
		String methodName = "method_1";
		String className = "com.example.Class1";
		String methodReturnType = "void";
		String[] args = new String[3];
		args[0] = "java.util.String";
		args[1] = "com.example.Class2";
		args[2] = "int";
		boolean isPublic = true;
		boolean isStatic = true;
		MethodData methodData = new MethodData(methodName, className, methodReturnType, args, isPublic, isStatic);
		String annotation = "org.junit.Test";
		methodData.setAnnotation(annotation);

		assertFalse(methodData.getAnnotation().isEmpty());
		assertEquals(annotation, methodData.getAnnotation());
	}



	@Test
	public void testJUnit3Test() throws FileNotFoundException, IOException {
		String testClassPath = "src/test/resources/RepeatedTestTest.class";
		ClassReader cr = new ClassReader(new FileInputStream(testClassPath));
		Set<String> classes = new HashSet<String>();
		Set<MethodData> methods = new HashSet<MethodData>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
		cr.accept(new ASMClassVisitor(Opcodes.ASM5, classes, methods, null, null, virtualCalls), ClassReader.SKIP_DEBUG);
		for(MethodData md : methods) {
			if(md.getName().startsWith("test")) {
				assertTrue(md.isJUnit3Test());
			}
		}
	}

	@Test
	public void equalsTest(){
		MethodData methodData1 = new MethodData("introspect",
			"org.apache.log4j.config.PropertySetter", "void",
			new String[0], false, false);
		MethodData methodData2 = new MethodData("introspect",
			"org.apache.log4j.config.PropertySetter", "void",
			new String[0], false, false);

		assertEquals(methodData1.getSignature(), methodData2.getSignature());
		assertEquals(methodData1.getSubSignature(), methodData2.getSubSignature());
		assertEquals(methodData1, methodData2);
	}

	@Test
	public void equalsTest2(){
		String[] args = {"parameter"};
		MethodData methodData1 = new MethodData("introspect",
			"org.apache.log4j.config.PropertySetter", "void",
			args, false, false);
		MethodData methodData2 = new MethodData("introspect",
			"org.apache.log4j.config.PropertySetter", "void",
			args, false, false);

		assertEquals(methodData1.getSignature(), methodData2.getSignature());
		assertEquals(methodData1.getSubSignature(), methodData2.getSubSignature());
		assertEquals(methodData1, methodData2);
	}
}
