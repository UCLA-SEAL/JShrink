package edu.ucla.cs.jshrinkapp;

import edu.ucla.cs.jshrinklib.reachability.MethodData;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationCommandLineParser {

	private final static String APPLICATION_NAME = "jdebloat.jar";

	private final List<File> libClassPath;
	private final List<File> appClassPath;
	private final List<File> testClassPath;
	private final boolean pruneApp;
	private final boolean mainEntryPoint;
	private final boolean publicEntryPoints;
	private final boolean testEntryPoints;
	private final Set<MethodData> customEntryPoints;
	private final boolean doRemoveMethods;
	private final Optional<File> mavenDirectory;
	private final Set<String> classesToIgnore;
	private final Optional<String> exceptionMessage;
	private final boolean exception;
	private final Optional<File> tamiflex;
	private final Optional<File> jmtrace;
	private final boolean removeClasses;
	private final boolean spark;
	private final boolean inlineMethods;
	private final boolean classCollapse;
	private final boolean verbose;
	private final boolean runTests;
	private final boolean skipMethodRemoval;
	private final boolean removeFields;
	private final File logDirectory;
	private final String backupPath;
	private final boolean cache;
	private final boolean ignoreLibs;
	private final boolean baseline;


	private static void printHelp(CommandLine commandLine){
		HelpFormatter helpFormatter = new HelpFormatter();
		String header = "An application to get the call-graph analysis of an application and to wipe unused methods";
		String footer = "";

		helpFormatter.printHelp(APPLICATION_NAME, header, getOptions(),footer, true);
		System.out.println();
	}

	public ApplicationCommandLineParser(String[] args) throws FileNotFoundException, ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;
		try{
			commandLine = parser.parse(ApplicationCommandLineParser.getOptions(), args);
		} catch (ParseException e){
			printHelp(commandLine);
			throw new ParseException("Could not parse arguments" + System.lineSeparator()
			+ "Exception information:" + System.lineSeparator() + e.getLocalizedMessage());
		}

		if(commandLine.hasOption('h')){
			printHelp(commandLine);
			System.exit(1);
		}

		assert(commandLine != null);

		this.pruneApp = commandLine.hasOption("p");

		if(commandLine.hasOption("a")){
			appClassPath = pathToFiles(commandLine.getOptionValue("a"));
		} else {
			appClassPath = new ArrayList<File>();
		}

		if(commandLine.hasOption('l')){
			libClassPath = pathToFiles(commandLine.getOptionValue("l"));
		} else {
			libClassPath = new ArrayList<File>();
		}

		if(commandLine.hasOption('t')){
			testClassPath = pathToFiles(commandLine.getOptionValue("t"));
		} else {
			testClassPath = new ArrayList<File>();
		}

		if(commandLine.hasOption('n')){

			File potentialMavenDirectory = new File(commandLine.getOptionValue('n'));
			if(!potentialMavenDirectory.exists() || !potentialMavenDirectory.isDirectory()){
				throw new FileNotFoundException("Specified Maven directory '" + potentialMavenDirectory.getAbsolutePath()
					+ "' is not a directory.");
			}

			File pomFile = new File(potentialMavenDirectory.getAbsolutePath() + File.separator + "pom.xml");

			if(!pomFile.exists() || pomFile.isDirectory()){
				throw new FileNotFoundException("File '" + pomFile.getAbsolutePath() + "' does not exist in " +
						"specified Maven directory.");
			}

			mavenDirectory = Optional.of(potentialMavenDirectory);

			if(!mvnCommandExists()){
				throw new ParseException("Maven Directory specified, yet the 'mvn' command was not found on your " +
					"system. Please install it.");
			}

		} else {
			mavenDirectory = Optional.empty();
		}


		mainEntryPoint = commandLine.hasOption('m');
		publicEntryPoints = commandLine.hasOption('u');
		testEntryPoints = commandLine.hasOption('s');

		customEntryPoints = new HashSet<MethodData>();
		if(commandLine.hasOption('c')){
			customEntryPoints.addAll(getMethodData(commandLine.getOptionValues('c'), commandLine));
		}

		this.doRemoveMethods = commandLine.hasOption('r');

		if(!mainEntryPoint && !publicEntryPoints && ! testEntryPoints && customEntryPoints.isEmpty()){
			printHelp(commandLine);
			throw new ParseException("No entry points were specified");
		}

		this.classesToIgnore = new HashSet<String>();
		if(commandLine.hasOption("i")){
			for(String className :  commandLine.getOptionValues("i")){
				this.classesToIgnore.add(className);
			}
		}

		this.exception = commandLine.hasOption("e");

		if(commandLine.hasOption("e") && commandLine.getOptionValue("e") != null){
			this.exceptionMessage = Optional.of(commandLine.getOptionValue("e"));
		} else {
			this.exceptionMessage = Optional.empty();
		}

		if (commandLine.hasOption("f")){
			File tamiFlexJar = new File(commandLine.getOptionValue("f"));
			if(!tamiFlexJar.exists()){
				throw new FileNotFoundException("Specified TamiFlex jar (\"" + tamiFlexJar.getAbsolutePath() + "\") " +
						"does not exist.");
			}
			if(tamiFlexJar.isDirectory()){
				throw new FileNotFoundException("Specified TamiFlex jar (\"" + tamiFlexJar.getAbsolutePath() + "\") " +
						"is a directory. Jar file expected.");
			}
			this.tamiflex = Optional.of(tamiFlexJar);
		}
		else{
			this.tamiflex = Optional.empty();
		}
		if(commandLine.hasOption("jm")){
			File jmTraceHomePath=new File(commandLine.getOptionValue("jm"));
			if(!jmTraceHomePath.exists()){
				throw new FileNotFoundException("Specified path (\"" + jmTraceHomePath.getAbsolutePath() + "\") " +
						"does not exist.");
			}
			this.jmtrace = Optional.of(jmTraceHomePath);
		}
		else {
			this.jmtrace = Optional.empty();
		}

		this.removeClasses = commandLine.hasOption("o");
		this.spark = commandLine.hasOption("k");
		this.inlineMethods = commandLine.hasOption("I");
		this.classCollapse = commandLine.hasOption("C");
		this.verbose = commandLine.hasOption("v");
		this.runTests = commandLine.hasOption("T");
		this.skipMethodRemoval = commandLine.hasOption("S");
		this.removeFields = commandLine.hasOption("F");
		this.cache = commandLine.hasOption("A");
		this.ignoreLibs = commandLine.hasOption("b");
		this.baseline = commandLine.hasOption("S");

		if(this.removeFields && this.skipMethodRemoval){
			throw new ParseException("Cannot Remove fields while skipping method removal.");
		}

		if(this.classCollapse && this.skipMethodRemoval){
			throw new ParseException("Cannot Collapse classes while skipping method removal.");
		}

		if(commandLine.hasOption("L")){
			File directory = new File(commandLine.getOptionValue("L"));
			if(!directory.exists()){
				if(!directory.mkdirs()){
					throw new ParseException("Specified log directory '"
						+ directory.getAbsolutePath() + " cannot be created.");
				}
			} else if(!directory.isDirectory()){
				throw new ParseException("Specified log directory '"
					+ directory.getAbsolutePath() + "' is not a directory.");
			} else if(!directory.canWrite()){
				throw new ParseException("Specified log directory '"
					+ directory.getAbsolutePath() + "' is not writable.");
			}
			this.logDirectory = directory;
		} else {
			File directory = new File(System.getProperty("user.home") + File.separator + "jshrink_output");
			if(directory.exists()){
				directory.delete();
			}
			directory.mkdirs();
			this.logDirectory = directory;
		}
		if(commandLine.hasOption("ch")){
			File backupPath = new File(commandLine.getOptionValue("ch"));
			if(!backupPath.exists()){
				if(!backupPath.mkdirs()){
					throw new ParseException("Specified backup path '"
							+ backupPath.getAbsolutePath() + " cannot be created.");
				}
			} else if(!backupPath.isDirectory()){
				throw new ParseException("Specified backup path '"
						+ backupPath.getAbsolutePath() + "' is not a directory.");
			} else if(!backupPath.canWrite()){
				throw new ParseException("Specified backup path '"
						+ backupPath.getAbsolutePath() + "' is not writable.");
			}
			this.backupPath = backupPath.getAbsolutePath();
		}
		else{
			this.backupPath = null;
		}
	}

	private static List<MethodData> getMethodData(String[] values, CommandLine commandLine) throws ParseException{
		List<MethodData> toReturn = new ArrayList<MethodData>();
		StringBuilder toAdd = new StringBuilder();
		for(String val : values) {
				/*
				Due to the weird way the Apache Commons CLI library works, i need to stitch
				together the strings as they may contain spaces
				 */
			if (val.endsWith(">")) {
				toAdd.append(val);
				try {
					toReturn.add(getMethodDataFromSignature(toAdd.toString()));
				} catch (IOException e) {
					printHelp(commandLine);
					throw new ParseException("Could not create method from input string " +
						"'" + toAdd.toString() + "' Exception thrown:"
						+ System.lineSeparator() + e.getLocalizedMessage());
				}
				toAdd = new StringBuilder();
			} else {
				toAdd.append(val + " ");
			}
		}
		return toReturn;
	}

	private static MethodData getMethodDataFromSignature(String signature) throws IOException{
		signature = signature.trim();
		if(!signature.startsWith("<") || !signature.endsWith(">")){
			throw new IOException("Signature must start with with '<' and end with '>'");
		}

		signature = signature.substring(1,signature.length()-1);
		String[] signatureSplit = signature.split(":");

		if(signatureSplit.length != 2){
			throw new IOException("Method signature must be in format of " +
				"'<[classname]:[public?] [static?] [returnType] [methodName]([args...?])>'");
		}

		String clName = signatureSplit[0];
		String methodString = signatureSplit[1];

		boolean publicMethod;
		if (methodString.toLowerCase().contains("public")) publicMethod = true;
		else publicMethod = false;
		boolean staticMethod = methodString.toLowerCase().contains("static");

		Pattern pattern = Pattern.compile("<?([a-zA-Z][a-zA-Z0-9_]*>?)(\\(.*\\))");
		Matcher matcher = pattern.matcher(methodString);

		if(!matcher.find()){
			throw new IOException("Could not find a method matching our regex pattern ('" + pattern.toString() + "')");
		}

		String method = matcher.group();
		String methodName = method.substring(0,method.indexOf('('));
		String[] methodArgs = method.substring(method.indexOf('(')+1, method.lastIndexOf(')'))
			.split(",");

		for(int i=0; i<methodArgs.length; i++){
			methodArgs[i] = methodArgs[i].trim();
		}

		if(methodArgs.length == 1 && methodArgs[0].isEmpty()){ //For case "... method();
			methodArgs = new String[0];
		}

		String[] temp = methodString.substring(0, methodString.indexOf(methodName)).trim().split("\\s+");
		String methodReturnType = temp[temp.length-1];

		return new MethodData(methodName,clName,methodReturnType, methodArgs, publicMethod, staticMethod);
	}

	private static List<File> pathToFiles(String path) throws FileNotFoundException {
		List<File> toReturn = new ArrayList<File>();

		String[] filePaths = path.split(File.pathSeparator);

		for(String f : filePaths){
			File toAdd = new File(f);
			if(!toAdd.exists()){
				throw new FileNotFoundException("Input path entry '" + f + "' does not exist");
			}
			toReturn.add(toAdd);
		}

		return toReturn;
	}

	private static Options getOptions(){

		Option pruneAppOption = Option.builder("p")
			.desc("Prune the application classes as well")
			.longOpt("prune-app")
			.hasArg(false)
			.required(false)
			.build();

		Option libClassPathOption = Option.builder("l")
			.desc("Specify the classpath for libraries")
			.longOpt("lib-classpath")
			.hasArg(true)
			.required(false)
			.optionalArg(false)
			.build();

		Option appClassPathOption = Option.builder("a")
			.desc("Specify the application classpath")
			.longOpt("app-classpath")
			.hasArg(true)
			.required(false)
			.optionalArg(false)
			.build();

		Option testClassPathOption = Option.builder("t")
			.desc("Specify the test classpath")
			.longOpt("test-classpath")
			.hasArg(true)
			.required(false)
			.optionalArg(false)
			.build();

		Option mavenTargetOption = Option.builder("n")
				.desc("Instead of targeting using lib/app/test classpaths, a Maven project directory may be specified")
				.longOpt("maven-project")
				.hasArgs()
				.required(false)
				.build();

		Option mainEntryPointOption = Option.builder("m")
			.desc("Include the main method as an entry point")
			.longOpt("main-entry")
			.hasArg(false)
			.required(false)
			.build();

		Option publicEntryPointOption = Option.builder("u")
			.desc("Include public methods as entry points")
			.longOpt("public-entry")
			.hasArg(false)
			.required(false)
			.build();

		Option  testEntryPointOption = Option.builder("s")
			.desc("Include the test methods as entry points")
			.longOpt("test-entry")
			.hasArg(false)
			.required(false)
			.build();

		Option customEntryPointOption = Option.builder("c")
			.desc("Specify custom entry points in syntax of " +
				"'<[classname]:[public?] [static?] [returnType] [methodName]([args...?])>'")
			.longOpt("custom-entry")
			.hasArgs()
			.valueSeparator()
			.required(false)
			.build();

		Option ignoreClassesOption = Option.builder("i")
				.desc("Specify classes that should not be delete or modified")
				.longOpt("ignore-classes")
				.hasArgs()
				.valueSeparator()
				.required(false)
				.build();

		Option specifyExceptionOption = Option.builder("e")
				.desc("Specify if an exception message should be included in a wiped method (Optional argument: the message)")
				.longOpt("include-exception")
				.hasArg(true)
				.optionalArg(true)
				.argName("Exception Message")
				.required(false)
				.build();

		Option removeClassesOption = Option.builder("o")
				.desc("Remove unused classes (only worked with \"remove-methods\" flag)")
				.longOpt("remove-classes")
				.hasArg(false)
				.required(false)
				.build();

		Option tamiFlexOption = Option.builder("f")
				.desc("Enable TamiFlex")
				.longOpt("tamiflex")
				.hasArg(true)
				.argName("TamiFlex Jar")
				.required(false)
				.build();

		Option jmTraceOption = Option.builder("jm")
				.desc("Enable JMTrace")
				.longOpt("jmtrace")
				.hasArg(true)
				.argName("JMTrace Home Dir")
				.required(false)
				.build();

		Option removeMethodsOption = Option.builder("r")
			.desc("Remove methods header and body (by default, the bodies are wiped)")
			.longOpt("remove-methods")
			.hasArg(false)
			.required(false)
			.build();


		Option helpOption = Option.builder("h")
			.desc("Help")
			.longOpt("help")
			.hasArg(false)
			.required(false)
			.build();

		Option sparkOption = Option.builder("k")
				.desc("Use Spark call graph analysis (Uses CHA by default)")
				.longOpt("use-spark")
				.hasArg(false)
				.required(false)
				.build();

		Option inlineMethodsOption = Option.builder("I")
				.desc("Inline methods that are only called from one location")
				.longOpt("inline")
				.hasArg(false)
				.required(false)
				.build();

		Option classCollapserOption = Option.builder("C")
				.desc("Collapse classes where appropriate")
				.longOpt("class-collapser")
				.hasArg(false)
				.required(false)
				.build();

		Option verboseOption = Option.builder("v")
			.desc("Verbose output")
			.longOpt("verbose")
			.hasArg(false)
			.required(false)
			.build();

		Option testOutputOption = Option.builder("T")
			.desc("Run the project tests.")
			.longOpt("run-tests")
			.hasArg(false)
			.required(false)
			.build();

		Option skipMethodWiping = Option.builder("S")
			.desc("Skip the method removal of unused methods")
			.longOpt("skip-method-removal")
			.hasArg(false)
			.required(false)
			.build();

		Option removeFieldsOption = Option.builder("F")
				.desc("Remove unused field members of a class.")
				.longOpt("remove-fields")
				.hasArg(false)
				.required(false)
				.build();

		Option logDirectoryOption = Option.builder("L")
			.desc("The directory to store logging information.")
			.longOpt("log-directory")
			.hasArg(true)
			.required(false)
			.build();

		Option cacheOption = Option.builder("A")
			.desc("Use/create caches (warning: can be dangerous, use carefully)")
			.longOpt("use-cache")
			.hasArg(false)
			.required(false)
			.build();

		Option ignoreLibsOptions = Option.builder("b")
				.desc("Only prune the app at the level of the application.")
				.longOpt("ignore-libs")
				.hasArg(false)
				.required(false)
				.build();

		Option baselineOption = Option.builder("S")
				.desc("Use the baseline version of JShrink.")
				.longOpt("baseline")
				.hasArg(false)
				.required(false)
				.build();
		Option checkpointOption = Option.builder("ch")
				.desc("Create checkpoints and rollback on test failure.")
				.longOpt("checkpoint")
				.hasArg(true)
				.required(false)
				.build();

		Options toReturn = new Options();
		toReturn.addOption(libClassPathOption);
		toReturn.addOption(appClassPathOption);
		toReturn.addOption(testClassPathOption);
		toReturn.addOption(mavenTargetOption);
		toReturn.addOption(mainEntryPointOption);
		toReturn.addOption(publicEntryPointOption);
		toReturn.addOption(testEntryPointOption);
		toReturn.addOption(pruneAppOption);
		toReturn.addOption(tamiFlexOption);
		toReturn.addOption(customEntryPointOption);
		toReturn.addOption(ignoreClassesOption);
		toReturn.addOption(specifyExceptionOption);
		toReturn.addOption(removeClassesOption);
		toReturn.addOption(sparkOption);
		toReturn.addOption(removeMethodsOption);
		toReturn.addOption(helpOption);
		toReturn.addOption(inlineMethodsOption);
		toReturn.addOption(classCollapserOption);
		toReturn.addOption(verboseOption);
		toReturn.addOption(testOutputOption);
		toReturn.addOption(skipMethodWiping);
		toReturn.addOption(removeFieldsOption);
		toReturn.addOption(logDirectoryOption);
		toReturn.addOption(jmTraceOption);
		toReturn.addOption(cacheOption);
		toReturn.addOption(ignoreLibsOptions);
		toReturn.addOption(baselineOption);
		toReturn.addOption(checkpointOption);

		return toReturn;
	}

	private boolean mvnCommandExists(){
		try {
			Process p = null;
			ProcessBuilder pb = new ProcessBuilder("mvn","--version");
			p = pb.start();

			p.waitFor();

			return p.exitValue() == 0;
		}catch(IOException|InterruptedException e){
			return false;
		}
	}

	public List<File> getAppClassPath() {
		return appClassPath;
	}

	public List<File> getLibClassPath() {
		return libClassPath;
	}

	public List<File> getTestClassPath() {
		return testClassPath;
	}

	public boolean isPruneAppInstance() {
		return pruneApp;
	}

	public boolean includeMainEntryPoint(){
		return mainEntryPoint;
	}

	public boolean includePublicEntryPoints(){
		return publicEntryPoints;
	}

	public boolean includeTestEntryPoints(){
		return testEntryPoints;
	}

	public Set<MethodData> getCustomEntryPoints(){
		return Collections.unmodifiableSet(customEntryPoints);
	}

	public boolean removeMethods(){
		return doRemoveMethods;
	}

	public Optional<File> getMavenDirectory(){
		return mavenDirectory;
	}

	public Set<String> getClassesToIgnore(){
		return Collections.unmodifiableSet(classesToIgnore);
	}

	public boolean includeException(){
		return exception;
	}

	public Optional<String> getExceptionMessage(){
		return exceptionMessage;
	}

	public Optional<File> getTamiflex(){
		return this.tamiflex;
	}

	public Optional<File> getJmtrace(){
		return this.jmtrace;
	}

	public boolean removeClasses(){
		return this.removeClasses;
	}

	public boolean useSpark(){return this.spark;}

	public boolean inlineMethods(){
		return this.inlineMethods;
	}

	public boolean collapseClasses(){
		return this.classCollapse;
	}

	public boolean isVerbose(){
		return this.verbose;
	}

	public boolean isRunTests(){
		return this.runTests;
	}

	public boolean isSkipMethodRemoval(){
		return this.skipMethodRemoval;
	}

	public boolean removedFields() {
		return this.removeFields;
	}

	public File getLogDirectory(){
		return this.logDirectory;
	}

	public boolean useCache(){
		return this.cache;
	}

	public boolean isIgnoreLibs(){
		return this.ignoreLibs;
	}

	public boolean useBaseline(){
		return this.baseline;
	}

	public String getBackupPath(){ return this.backupPath;}
}
