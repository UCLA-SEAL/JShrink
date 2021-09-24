import edu.ucla.cs.jshrinklib.JShrink;
import edu.ucla.cs.jshrinklib.reachability.FieldData;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.reachability.EntryPointProcessor;
import edu.ucla.cs.jshrinklib.reachability.TestOutput;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class JShrinkTest {
	private static File simpleTestProjectDir;
	private static EntryPointProcessor entryPointProcessor;
	private static Optional<File> tamiflex;
	private static Optional<File> jmtrace;
	private static boolean useSpark;
	private static JShrink jShrink;

	private static void resetSimpleTestProjectDir(){
		try {
			if(simpleTestProjectDir != null){
				simpleTestProjectDir.delete();
			}
			File original = new File(JShrinkTest.class.getClassLoader()
				.getResource("simple-test-project").getFile());
			File copy = File.createTempFile("sample-test-project", "");
			copy.delete();
			copy.mkdir();
			FileUtils.copyDirectory(original, copy);
			simpleTestProjectDir = copy;
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

	}

	@BeforeClass
	public static void beforeClass(){
		resetSimpleTestProjectDir();
		entryPointProcessor =
			new EntryPointProcessor(true, false,
				false,new HashSet<MethodData>());
		tamiflex = Optional.empty();
		jmtrace = Optional.empty();
		useSpark = true;
		try {
			jShrink = JShrink.createInstance(simpleTestProjectDir, entryPointProcessor, tamiflex,jmtrace,
				useSpark, false, true, false, false);
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void reboot(){
		try {
			resetSimpleTestProjectDir();
			this.jShrink = JShrink.resetInstance(this.simpleTestProjectDir,
				this.entryPointProcessor, this.tamiflex, this.jmtrace, this.useSpark, false, true, false, false);
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Test
	public void getInstanceTest(){
		try {
			assertEquals(this.jShrink, JShrink.getInstance());
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean isPresent(Set<MethodData> methodSet, String className, String methodName){
		for(MethodData methodData : methodSet){
			if(methodData.getClassName().equals(className) && methodData.getName().equals(methodName)){
				return true;
			}
		}
		return false;
	}


	@Test
	public void getAllAppMethodsTest(){
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
				"StandardStuff", "<clinit>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "doNothing"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "touchedViaReflection"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "getString"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "getStringStatic"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "publicAndTestedButUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "publicAndTestedButUntouchedCallee"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "publicNotTestedButUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "publicNotTestedButUntouchedCallee"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "privateAndUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff", "protectedAndUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$1", "compare"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$1", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$NestedClass", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$NestedClass", "nestedClassMethod"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuff$NestedClass", "nestedClassNeverTouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuffSub", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuffSub", "protectedAndUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"StandardStuffSub", "subMethodUntouched"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main", "access$000"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main", "main"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main", "compare"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main$1", "compare"));
		assertTrue(isPresent(this.jShrink.getAllAppMethods(),
			"Main$1", "<init>"));
		assertEquals(30,this.jShrink.getAllAppMethods().size());
	}

	@Test
	public void getAllLibMethodsTest(){
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.UnusedClass", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.UnusedClass", "unusedMethod"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "untouchedGetNumber"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "privateUntouchedGetNumber"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass2", "<init>"));
		assertTrue(isPresent(this.jShrink.getAllLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass2", "methodInAnotherClass"));
		assertEquals(8,this.jShrink.getAllLibMethods().size());
	}

	@Test
	public void getAllAppClassesTest(){
		assertTrue(this.jShrink.getAllAppClasses().contains("StandardStuff"));
		assertTrue(this.jShrink.getAllAppClasses().contains("StandardStuff$NestedClass"));
		assertTrue(this.jShrink.getAllAppClasses().contains("StandardStuff$1"));
		assertTrue(this.jShrink.getAllAppClasses().contains("StandardStuffSub"));
		assertTrue(this.jShrink.getAllAppClasses().contains("Main$1"));
		assertTrue(this.jShrink.getAllAppClasses().contains("Main"));
		assertEquals(6, this.jShrink.getAllAppClasses().size());
	}

	@Test
	public void getAllLibClassesTest(){
		assertTrue(this.jShrink.getAllLibClasses().contains("edu.ucla.cs.onr.test.UnusedClass"));
		assertTrue(this.jShrink.getAllLibClasses().contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertTrue(this.jShrink.getAllLibClasses().contains("edu.ucla.cs.onr.test.LibraryClass2"));
		assertEquals(3, this.jShrink.getAllLibClasses().size());
	}

	@Test
	public void getUsedAppMethodsTest(){
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff", "<init>"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff", "getString"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff", "getStringStatic"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff", "doNothing"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff$1", "<init>"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff$1", "compare"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff$NestedClass", "<init>"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff$NestedClass", "nestedClassMethod"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"StandardStuff$NestedClass", "nestedClassMethodCallee"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"Main", "main"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"Main$1", "compare"));
		assertTrue(isPresent(this.jShrink.getUsedAppMethods(),
			"Main", "compare"));
		assertEquals(18,this.jShrink.getUsedAppMethods().size());
	}

	@Test
	public void getUsedLibMethodsTest(){
		assertTrue(isPresent(this.jShrink.getUsedLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "<init>"));
		assertTrue(isPresent(this.jShrink.getUsedLibMethods(),
			"edu.ucla.cs.onr.test.LibraryClass", "getNumber"));
		assertEquals(2,this.jShrink.getUsedLibMethods().size());
	}

	@Test
	public void getUsedAppClassesTest(){
		assertTrue(this.jShrink.getUsedAppClasses().contains("StandardStuff"));
		assertTrue(this.jShrink.getUsedAppClasses().contains("StandardStuff$NestedClass"));
		assertTrue(this.jShrink.getUsedAppClasses().contains("Main"));
		assertTrue(this.jShrink.getUsedAppClasses().contains("Main$1"));
		assertTrue(this.jShrink.getAllAppClasses().contains("StandardStuff$1"));
		assertEquals(5, this.jShrink.getUsedAppClasses().size());
	}

	@Test
	public void getSizesTest(){
		assertEquals(10723, this.jShrink.getAppSize(true));
		assertEquals(8020, this.jShrink.getLibSize(true));
	}

	@Test
	public void getTestDataTest(){
		assertEquals(4, this.jShrink.getTestOutput().getRun());
		assertEquals(0, this.jShrink.getTestOutput().getErrors());
		assertEquals(1, this.jShrink.getTestOutput().getFailures());
		assertEquals(0, this.jShrink.getTestOutput().getSkipped());
	}

	@Test
	public void getUsedLibClassesTest(){
		assertTrue(this.jShrink.getUsedLibClasses().contains("edu.ucla.cs.onr.test.LibraryClass"));
		assertEquals(1, this.jShrink.getUsedLibClasses().size());
	}

	@Test
	public void getSimplifiedCallGraphTest(){
		//TODO: Complete this.
	}

	@Test
	public void makeSootPassTest(){
		this.jShrink.makeSootPass(); //Simply ensuring this doesn't crash for now.
		reboot();
	}

	@Test
	public void removeMethodsTest(){
		Set<MethodData> toRemove = new HashSet<MethodData>();
		toRemove.addAll(this.jShrink.getAllAppMethods());
		toRemove.removeAll(this.jShrink.getUsedAppMethods());
		Set<MethodData> methodsRemoved = this.jShrink.removeMethods(toRemove,true);
		this.jShrink.updateClassFiles();

		assertFalse(methodsRemoved.isEmpty());
		for(MethodData removed : methodsRemoved){
			assertFalse(this.jShrink.getAllAppMethods().contains(removed));
		}
		reboot(); //Reset things back to normal
	}

	@Test
	public void removeClassesTest(){
		Set<String> toRemove = new HashSet<String>();
		toRemove.add("edu.ucla.cs.onr.test.LibraryClass");
		toRemove.add("edu.ucla.cs.onr.test.LibraryClass2");
		this.jShrink.removeClasses(toRemove);
		this.jShrink.updateClassFiles();

		for(String removed: toRemove){
			assertFalse(this.jShrink.getAllLibClasses().contains(removed));
		}
		reboot(); //Reset things back to normal
	}

	private boolean isFieldPresent(Set<FieldData> fieldSet, String className, String fieldName){
		for(FieldData fieldData : fieldSet){
			if(fieldData.getClassName().equals(className) && fieldData.getName().equals(fieldName)){
				return true;
			}
		}
		return false;
	}

	@Test
	public void getAllAppFieldsTest() {
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff", "HELLO_WORLD_STRING"));
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff", "GOODBYE_STRING"));
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff", "integer"));
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff", "f1"));
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff", "f2"));
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuffSub", "f1"));
		// an anonymous class has an implicit field to its container class
		assertTrue(isFieldPresent(this.jShrink.getAllAppFields(),
				"StandardStuff$1", "this$0"));
		assertEquals(7, this.jShrink.getAllAppFields().size());
	}

	@Test
	public void getAllLibFieldsTest() {
		assertTrue(isFieldPresent(this.jShrink.getAllLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass", "x"));
		assertTrue(isFieldPresent(this.jShrink.getAllLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass", "f1"));
		assertTrue(isFieldPresent(this.jShrink.getAllLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass2", "y"));
		assertEquals(3, this.jShrink.getAllLibFields().size());
	}

	@Test
	public void getUsedAppFieldsTest() {
		// Final fields are inlined during java compilation
		// so the following two fields are not referenced at all
		assertFalse(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff", "HELLO_WORLD_STRING"));
		assertFalse(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff", "GOODBYE_STRING"));
		assertTrue(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff", "integer"));

		// Since f1 and f2 are initialized, they are always used as long as the class is used
		assertTrue(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff", "f1"));
		assertTrue(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff", "f2"));

		// Though StandardStuffSub.f1 is also initialized, its class is not used when setting the main method as an entry point
		// So this field is not used
		assertFalse(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuffSub", "f1"));

		assertTrue(isFieldPresent(this.jShrink.getUsedAppFields(),
				"StandardStuff$1", "this$0"));

		assertEquals(4, this.jShrink.getUsedAppFields().size());
	}

	@Test
	public void getUsedLibFieldsTest() {
		// x and y are inlined by Java compiler
		assertFalse(isFieldPresent(this.jShrink.getUsedLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass", "x"));
		assertFalse(isFieldPresent(this.jShrink.getUsedLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass2", "y"));
		// f1 is not used
		assertFalse(isFieldPresent(this.jShrink.getUsedLibFields(),
				"edu.ucla.cs.onr.test.LibraryClass", "f1"));
		assertEquals(0, this.jShrink.getUsedLibFields().size());
	}

	@Test
	public void removeFieldsTest() {
		Set<FieldData> toRemove = new HashSet<FieldData>();
		toRemove.addAll(this.jShrink.getAllAppFields());
		toRemove.removeAll(this.jShrink.getUsedAppFields());
		TestOutput before = this.jShrink.getTestOutput();
		Set<FieldData> fieldsRemoved = this.jShrink.removeFields(toRemove);
		this.jShrink.updateClassFiles();

		assertEquals(3, fieldsRemoved.size());
		for(FieldData removed : fieldsRemoved){
			assertFalse(this.jShrink.getAllAppFields().contains(removed));
		}
		TestOutput after = this.jShrink.getTestOutput();

		// make sure the test result is still the same after field removal
		assertEquals(before.getRun(), after.getRun());
		assertEquals(before.getErrors(), after.getErrors());
		assertEquals(before.getFailures(), after.getFailures());
		assertEquals(before.getSkipped(), after.getSkipped());

		// continue to remove unused lib fields
		toRemove.clear();
		toRemove.addAll(this.jShrink.getAllLibFields());
		toRemove.removeAll(this.jShrink.getUsedLibFields());
		fieldsRemoved.clear();
		fieldsRemoved = this.jShrink.removeFields(toRemove);
		this.jShrink.updateClassFiles();

		assertEquals(3, fieldsRemoved.size());
		for(FieldData removed : fieldsRemoved){
			assertFalse(this.jShrink.getAllLibFields().contains(removed));
		}

		after =  this.jShrink.getTestOutput();

		// check the test result again
		assertEquals(before.getRun(), after.getRun());
		assertEquals(before.getErrors(), after.getErrors());
		assertEquals(before.getFailures(), after.getFailures());
		assertEquals(before.getSkipped(), after.getSkipped());

		reboot(); //Reset things back to normal
	}
}
