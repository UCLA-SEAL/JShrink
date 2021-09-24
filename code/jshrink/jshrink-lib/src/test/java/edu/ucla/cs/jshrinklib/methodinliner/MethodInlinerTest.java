package edu.ucla.cs.jshrinklib.methodinliner;

import edu.ucla.cs.jshrinklib.TestUtils;
import edu.ucla.cs.jshrinklib.reachability.CallGraphAnalysis;
import edu.ucla.cs.jshrinklib.reachability.EntryPointProcessor;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import soot.G;
import soot.Scene;
import soot.SootMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodInlinerTest {

	private Map<SootMethod, Set<SootMethod>> callgraph;
	List<File> appClassPath = new ArrayList<File>();
	List<File> appTestPath = new ArrayList<File>();
	List<File> libJarPath = new ArrayList<File>();
	private File original;
	private File backup;

	@After
	public void after(){
		appClassPath.clear();
		appTestPath.clear();
		libJarPath.clear();
		callgraph = null;
		original = null;
		backup.delete();
		backup = null;
		G.reset();
	}

	public Set<File> getClasspaths(){
		Set<File> toReturn = new HashSet<File>();
		toReturn.addAll(appTestPath);
		toReturn.addAll(appClassPath);
		toReturn.addAll(libJarPath);
		return toReturn;
	}

	public void setup_simpleTestProject(){
		ClassLoader classLoader = MethodInlinerTest.class.getClassLoader();
		original = new File(classLoader.getResource("simple-test-project").getFile());

		try{
			backup = File.createTempFile("backup", "");
			backup.delete();
			FileUtils.copyDirectory(original,backup);
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		appClassPath.add(new File(backup.getAbsolutePath()
				+ File.separator + "target" + File.separator + "classes"));
		libJarPath.add(new File(backup.getAbsolutePath()
				+ File.separator + "libs" + File.separator + "standard-stuff-library.jar"));

		EntryPointProcessor epp = new EntryPointProcessor(true, false, false,
				new HashSet<MethodData>() );

		CallGraphAnalysis callGraphAnalysis =
				new CallGraphAnalysis(libJarPath, appClassPath, appTestPath, epp,false);
		callGraphAnalysis.setup();
		callGraphAnalysis.run();

		SootUtils.setup_trimming(libJarPath,appClassPath,appTestPath);
		this.callgraph = SootUtils.mergeCallGraphMaps(
				SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedAppMethods()),
				SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedLibMethods()));
	}

	public void setup_dynamicDispatchTest(){
		ClassLoader classLoader = MethodInlinerTest.class.getClassLoader();
		original = new File(classLoader.getResource("dynamic-dispatch-project").getFile());

		try{
			backup = File.createTempFile("backup", "");
			backup.delete();
			FileUtils.copyDirectory(original,backup);
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		appClassPath.add(new File(backup.getAbsolutePath()
			+ File.separator + "target" + File.separator + "classes"));

		EntryPointProcessor epp = new EntryPointProcessor(true, false, false,
			new HashSet<MethodData>() );

		CallGraphAnalysis callGraphAnalysis =
			new CallGraphAnalysis(libJarPath, appClassPath, appTestPath, epp,true);
		callGraphAnalysis.setup();
		callGraphAnalysis.run();

		SootUtils.setup_trimming(libJarPath,appClassPath,appTestPath);
		this.callgraph = SootUtils.mergeCallGraphMaps(
			SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedAppMethods()),
			SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedLibMethods()));
	}

	public void setup_innerClassInlinerTest(){
		ClassLoader classLoader = MethodInlinerTest.class.getClassLoader();
		original = new File(classLoader.getResource("inner-class-inliner-test").getFile());

		try{
			backup = File.createTempFile("backup", "");
			backup.delete();
			FileUtils.copyDirectory(original,backup);
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		appClassPath.add(new File(backup.getAbsolutePath()
			+ File.separator + "target" + File.separator + "classes"));

		EntryPointProcessor epp = new EntryPointProcessor(true, false, false,
			new HashSet<MethodData>() );

		CallGraphAnalysis callGraphAnalysis =
			new CallGraphAnalysis(libJarPath, appClassPath, appTestPath, epp,false);
		callGraphAnalysis.setup();
		callGraphAnalysis.run();

		SootUtils.setup_trimming(libJarPath,appClassPath,appTestPath);
		this.callgraph = SootUtils.mergeCallGraphMaps(
			SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedAppMethods()),
			SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedLibMethods()));
	}

	public void setup_illegalAccessProject(){
		ClassLoader classLoader = MethodInlinerTest.class.getClassLoader();
		original = new File(classLoader.getResource("illegal-access-project").getFile());

		try{
			backup = File.createTempFile("backup", "");
			backup.delete();
			FileUtils.copyDirectory(original,backup);
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		appClassPath.add(new File(backup.getAbsolutePath()
				+ File.separator + "target" + File.separator + "classes"));

		EntryPointProcessor epp = new EntryPointProcessor(true, false, false,
				new HashSet<MethodData>() );

		CallGraphAnalysis callGraphAnalysis =
				new CallGraphAnalysis(libJarPath, appClassPath, appTestPath, epp,false);
		callGraphAnalysis.setup();
		callGraphAnalysis.run();

		SootUtils.setup_trimming(libJarPath,appClassPath,appTestPath);
		this.callgraph = SootUtils.mergeCallGraphMaps(
				SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedAppMethods()),
				SootUtils.convertMethodDataCallGraphToSootMethodCallGraph(callGraphAnalysis.getUsedLibMethods()));
	}

	@Test
	public void inlineMethodsTest() throws IOException{
		setup_simpleTestProject();
		Set<File> decompressedJars = ClassFileUtils.extractJars(new ArrayList<File>(getClasspaths()));
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(),  new HashSet<String>());

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
				TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")));
		assertEquals(1,inlineData.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")).size());
		assertTrue(inlineData.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>"))
				.contains(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertTrue(inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).isPresent());
		assertEquals(1, inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get().size());
		assertTrue(inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get()
				.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		ClassFileUtils.compressJars(decompressedJars);
	}

	@Test
	public void inlineMethodsTest_withoutDecompressedJars() throws IOException{
		setup_simpleTestProject();
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(), new HashSet<String>());

		assertEquals(6, inlineData.getInlineLocations().size());

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
				TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")));
		assertEquals(1,inlineData.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")).size());
		assertTrue(inlineData.getInlineLocations()
				.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>"))
				.contains(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertTrue(inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).isPresent());
		assertEquals(1, inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get().size());
		assertTrue(inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get()
				.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));
	}

	@Test
	public void inlineMethodsTest_withClassRewrite() throws IOException{
		setup_simpleTestProject();
		Set<File> decompressedJars = ClassFileUtils.extractJars(new ArrayList<File>(getClasspaths()));
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(), new HashSet<String>());


		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<StandardStuff: public static java.lang.String getStringStatic(int)>"))
			.contains(TestUtils.getMethodDataFromSignature("<StandardStuff: public java.lang.String getString()>")));
		assertTrue(inlineData.getUltimateInlineLocations(
						TestUtils.getMethodDataFromSignature(
								"<StandardStuff: public static java.lang.String getStringStatic(int)>")).isPresent());
		assertEquals(1, inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get().size());
		assertTrue(inlineData.getUltimateInlineLocations(
				TestUtils.getMethodDataFromSignature(
						"<StandardStuff: public static java.lang.String getStringStatic(int)>")).get()
				.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations().containsKey(
			TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")));
		assertEquals(1,inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>")).size());
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<edu.ucla.cs.onr.test.LibraryClass: public int getNumber()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		ClassFileUtils.writeClass(
			Scene.v().loadClassAndSupport("StandardStuff"), getClasspaths());
		ClassFileUtils.writeClass(
			Scene.v().loadClassAndSupport("edu.ucla.cs.onr.test.LibraryClass"), getClasspaths());
		ClassFileUtils.compressJars(decompressedJars);
	}

	@Test
	public void innerClassInlinerTest() throws IOException{
		setup_innerClassInlinerTest();
		Set<File> decompressedJars = ClassFileUtils.extractJars(new ArrayList<File>(getClasspaths()));
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(), new HashSet<String>());
		assertEquals(0, inlineData.getInlineLocations().size());
		ClassFileUtils.compressJars(decompressedJars);
	}

	@Test
	public void packageIllegalAccessProject() throws IOException{
		setup_illegalAccessProject();
		Set<File> decompressedJars = ClassFileUtils.extractJars(new ArrayList<File>(getClasspaths()));
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(), new HashSet<String>());
		assertEquals(1, inlineData.getInlineLocations().size());
		ClassFileUtils.compressJars(decompressedJars);
	}

	@Test
	public void dynamicDispatchTest() throws IOException{
		setup_dynamicDispatchTest();
		//Reminder: I'm using Spark instead of CHA here.
		InlineData inlineData = MethodInliner.inlineMethods(this.callgraph, getClasspaths(), new HashSet<String>());

		assertEquals(2, inlineData.getInlineLocations().size());

		assertTrue(inlineData.getInlineLocations()
			.containsKey(TestUtils.getMethodDataFromSignature("<Main: public static void dynamicDispatch2(Parent)>")));
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<Main: public static void dynamicDispatch2(Parent)>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void main(java.lang.String[])>")));

		assertTrue(inlineData.getInlineLocations()
			.containsKey(TestUtils.getMethodDataFromSignature("<ChildsChild: public java.lang.String bla()>")));
		assertTrue(inlineData.getInlineLocations()
			.get(TestUtils.getMethodDataFromSignature("<ChildsChild: public java.lang.String bla()>"))
			.contains(TestUtils.getMethodDataFromSignature("<Main: public static void dynamicDispatch2(Parent)>")));
	}
}
