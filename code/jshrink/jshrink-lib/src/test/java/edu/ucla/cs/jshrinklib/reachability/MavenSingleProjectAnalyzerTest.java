package edu.ucla.cs.jshrinklib.reachability;

import static org.junit.Assert.*;

import edu.ucla.cs.jshrinklib.GitGetter;
import org.junit.*;

import soot.G;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MavenSingleProjectAnalyzerTest {

	private static GitGetter gitGetter;

	@BeforeClass
	public static void setup(){
		gitGetter = new GitGetter();
	}

	@AfterClass
	public static void cleanup(){
		gitGetter.removeGitDir();
	}

	private File getTamiFlexJar(){
		File toReturn = new File(
				MavenSingleProjectAnalyzer.class.getClassLoader()
					.getResource("tamiflex/poa-2.0.3.jar").getFile());
		return toReturn;
	}

	/*
	I'm ignoring most of these these tests as they are just too intense for a quick test.
	 */

	@Test @Ignore
	public void testMavenProjectWithNoSubmodulesSparkOnly() {

		String junit_project = new File(MavenSingleProjectAnalyzer.class.getClassLoader()
			.getResource("junit4").getFile()).getAbsolutePath();

		MavenSingleProjectAnalyzer runner =
				new MavenSingleProjectAnalyzer(junit_project,
						new EntryPointProcessor(true, false, true,
							 new HashSet<MethodData>()),
						Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(72, runner.getUsedLibClasses().size());
		assertEquals(354, runner.getUsedLibMethods().size());
		assertEquals(37, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(197, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(254, runner.getUsedAppClasses().size());
		assertEquals(1474, runner.getUsedAppMethods().size());
		assertEquals(87, runner.getUsedLibFields().size());
		assertEquals(37, runner.getUsedLibFieldsCompileOnly().size());
		assertEquals(366, runner.getUsedAppFields().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithNoSubmodulesBothSparkAndTamiFlex() {

		String junit_project = new File(MavenSingleProjectAnalyzer.class.getClassLoader()
			.getResource("junit4").getFile()).getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(junit_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.of(getTamiFlexJar()), Optional.empty(),  true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(73, runner.getUsedLibClasses().size());
		assertEquals(359, runner.getUsedLibMethods().size());
		assertEquals(38, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(200, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(282, runner.getUsedAppClasses().size());
		assertEquals(1567, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithOneSubmoduleSparkOnly() {
		// the gson project has many submodules but only one submodule is actually built
		String gson_project = gitGetter.addGitHubProject("google","gson",
				"aa236ec38d39f434c1641aeaef9241aec18affde").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(gson_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(72, runner.getUsedLibClasses().size());
		assertEquals(219, runner.getUsedLibMethods().size());
		// This project has no library dependencies in the compile scope 
		assertEquals(0, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(0, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(163, runner.getUsedAppClasses().size());
		assertEquals(930, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithOneSubmoduleBothSparkAndTamiFlex() {

		// the gson project has many submodules but only one submodule is actually built
		String gson_project = gitGetter.addGitHubProject("google","gson",
				"aa236ec38d39f434c1641aeaef9241aec18affde").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(gson_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(89, runner.getUsedLibClasses().size());
		assertEquals(306, runner.getUsedLibMethods().size());
		assertEquals(0, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(0, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(168, runner.getUsedAppClasses().size());
		assertEquals(940, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodulesSparkOnly() {
		// the essentials project has multiple modules compiled but only one module has 
		// real Java class files, the other two only have resources
		String essentials_project = gitGetter.addGitHubProject("greenrobot","essentials",
				"31eaaeb410174004196c9ef9c9469e0d02afd94b").getAbsolutePath();;
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(essentials_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(1168, runner.getUsedLibClasses().size());
		assertEquals(5684, runner.getUsedLibMethods().size());
		// this project also has no dependencies in the compile scope
		assertEquals(0, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(0, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(26, runner.getUsedAppClasses().size());
		assertEquals(195, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodulesBothSparkAndTamiFlex() {
		// the essentials project has multiple modules compiled but only one module has 
		// real Java class files, the other two only have resources
		String essentials_project = gitGetter.addGitHubProject("greenrobot","essentials",
				"31eaaeb410174004196c9ef9c9469e0d02afd94b").getAbsolutePath();;
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(essentials_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.of(getTamiFlexJar()),Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(1184, runner.getUsedLibClasses().size());
		assertEquals(5735, runner.getUsedLibMethods().size());
		assertEquals(0, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(0, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(26, runner.getUsedAppClasses().size());
		assertEquals(195, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodules2SparkOnly() {
		// the cglib project has five modules
		// four of them have java class files and only two of them 
		// have test classes
		String cglib_project = gitGetter.addGitHubProject("cglib","cglib",
				"5942bcd657f35a699f05fadfdf720a5c6a3af2b5").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(cglib_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(968, runner.getUsedLibClasses().size());
		assertEquals(4429, runner.getUsedLibMethods().size());
		assertEquals(899, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(4257, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(157, runner.getUsedAppClasses().size());
		assertEquals(902, runner.getUsedAppMethods().size());
	}
	
	/**
	 * Note that injecting TamiFlex causes many test failures in this project.
	 */
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodules2SparkAndTamiFlex() {
		// the cglib project has five modules
		// four of them have java class files and only two of them 
		// have test classes
		String cglib_project = gitGetter.addGitHubProject("cglib","cglib",
				"5942bcd657f35a699f05fadfdf720a5c6a3af2b5").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(cglib_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.of(getTamiFlexJar()),Optional.empty(),true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(970, runner.getUsedLibClasses().size());
		assertEquals(4523, runner.getUsedLibMethods().size());
		assertEquals(899, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(4257, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(158, runner.getUsedAppClasses().size());
		assertEquals(903, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodules3SparkOnly() {
		// the pf4j project has two submodules and one of them has two subsubmodules
		String pf4j_project = gitGetter.addGitHubProject("decebals","pf4j",
				"b0073dade44d9085052043f0b1e1952f0515cde7").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(pf4j_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(),Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(1159, runner.getUsedLibClasses().size());
		assertEquals(3609, runner.getUsedLibMethods().size());
		assertEquals(268, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(1133, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(64, runner.getUsedAppClasses().size());
		assertEquals(323, runner.getUsedAppMethods().size());
	}
	
	@Test @Ignore
	public void testMavenProjectWithMultiSubmodules3BothSparkAndTamiFlex() {
		
		// the pf4j project has two submodules and one of them has two subsubmodules
		String pf4j_project = gitGetter.addGitHubProject("decebals","pf4j",
			"b0073dade44d9085052043f0b1e1952f0515cde7").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(pf4j_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.of(getTamiFlexJar()), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		assertEquals(1196, runner.getUsedLibClasses().size());
		assertEquals(3766, runner.getUsedLibMethods().size());
		assertEquals(269, runner.getUsedLibClassesCompileOnly().size());
		assertEquals(1146, runner.getUsedLibMethodsCompileOnly().size());
		assertEquals(69, runner.getUsedAppClasses().size());
		assertEquals(384, runner.getUsedAppMethods().size());
	}

	@Test @Ignore
	public void testCallGraphInfo(){
		ClassLoader classLoader = MavenSingleProjectAnalyzerTest.class.getClassLoader();
		File mavenProject = new File(classLoader.getResource("module-test-project").getFile());
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(mavenProject.getAbsolutePath(),
				new EntryPointProcessor(true, false, false,
						new HashSet<MethodData>()), Optional.of(getTamiFlexJar()), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
		Map<MethodData, Set<MethodData>> usedAppMethods = runner.getUsedAppMethods();
		Map<MethodData, Set<MethodData>> usedLibMethods = runner.getUsedLibMethods();

		Optional<Set<MethodData>> anotherMain =
				get(usedAppMethods, "edu.ucla.cs.onr.test.another.Main", "main");
		assertTrue(anotherMain.isPresent());
		assertEquals(0, anotherMain.get().size());

		Optional<Set<MethodData>> main = get(usedAppMethods, "Main", "main");
		assertTrue(main.isPresent());
		assertEquals(0, main.get().size());

		Optional<Set<MethodData>> standardStuffInit =
				get(usedAppMethods, "StandardStuff", "<init>");
		assertTrue(standardStuffInit.isPresent());
		assertEquals(1, standardStuffInit.get().size());
		assertTrue(contains(standardStuffInit.get(), "Main", "main"));

		Optional<Set<MethodData>> standardStuffGetString =
				get(usedAppMethods, "StandardStuff", "getString");
		assertTrue(standardStuffGetString.isPresent());
		assertEquals(1, standardStuffGetString.get().size());
		assertTrue(contains(standardStuffGetString.get(), "Main", "main"));

		Optional<Set<MethodData>> libraryClassInit =
				get(usedLibMethods, "edu.ucla.cs.onr.test.LibraryClass", "<init>");
		assertTrue(libraryClassInit.isPresent());
		assertEquals(1, libraryClassInit.get().size());
		assertTrue(contains(libraryClassInit.get(), "Main", "main"));

		Optional<Set<MethodData>> libraryClassgetNumber =
				get(usedLibMethods, "edu.ucla.cs.onr.test.LibraryClass", "getNumber");
		assertTrue(libraryClassgetNumber.isPresent());
		assertEquals(1, libraryClassgetNumber.get().size());
		assertTrue(contains(libraryClassgetNumber.get(), "Main", "main"));

		/*
		Technically this is called by "StandardStuff:<init>()" but this is called via reflection and is therefore
		treated as an entry point.
		 */
		Optional<Set<MethodData>> standardStuffTouchedViaReflect =
				get(usedAppMethods, "StandardStuff", "touchedViaReflection");
		assertTrue(standardStuffTouchedViaReflect.isPresent());
		assertEquals(0, standardStuffTouchedViaReflect.get().size());

		Optional<Set<MethodData>> standardStuffGetStaticString =
				get(usedAppMethods, "StandardStuff", "getStringStatic");
		assertTrue(standardStuffGetStaticString.isPresent());
		assertEquals(1, standardStuffGetStaticString.get().size());
		assertTrue(contains(standardStuffGetStaticString.get(), "StandardStuff", "getString"));
	}

	private static Optional<Set<MethodData>> get(Map<MethodData,Set<MethodData>> map,
	                                             String className, String methodName){
		MethodData methodData = null;
		for(Map.Entry<MethodData, Set<MethodData>> entry : map.entrySet()){
			if(entry.getKey().getClassName().equals(className) && entry.getKey().getName().equals(methodName)
					&& (methodData == null || methodData.getArgs().length > entry.getKey().getArgs().length)){
				methodData = entry.getKey();
			}
		}
		if(methodData != null){
			return Optional.of(map.get(methodData));
		}
		return Optional.empty();
	}

	private static boolean contains(Set<MethodData> set, String className, String methodName){
		for(MethodData methodData : set){
			if(methodData.getClassName().equals(className) && methodData.getName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Must manually remove JCTools_JCTools/jctools-experimental/target/classes/
	 * org/jctools/queues/blocking/TemplateBlocking.java to resolve this NPE
	 * Tried to catch it outside of Soot but couldn't
	 */
	@Test @Ignore
	public void testNPEInJCTools() {
		String jctools_project = gitGetter.addGitHubProject("JCTools","JCTools",
				"aeeffff7815c97fd5d57dfdaaea1a6eaba2e15dd").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(jctools_project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()), Optional.empty(), Optional.empty(), true,
			false, true, false, false);
		runner.setup();
		runner.run();
	}

	@Test @Ignore
	public void testClassResolution() {
		String project = gitGetter.addGitHubProject("davidmoten","rxjava-extras",
				"a91d2ba7d454843250e0b0fce36084f9fb02a551").getAbsolutePath();
		MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(project,
				new EntryPointProcessor(true, false, true,
					new HashSet<MethodData>()),
				Optional.empty(), Optional.empty(), true, false, true, false, false);
		runner.setup();
		runner.run();
	}
	
	@After
	public void cleanUp() {
		G.reset();
	}
}
