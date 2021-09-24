package edu.ucla.cs.jshrinkapp;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import edu.ucla.cs.jshrinklib.JShrink;
import edu.ucla.cs.jshrinklib.backup.BackupService;
import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapser;
import edu.ucla.cs.jshrinklib.classcollapser.ClassCollapserData;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.methodinliner.InlineData;
import edu.ucla.cs.jshrinklib.reachability.*;

import org.apache.log4j.PropertyConfigurator;

public class Application {
	//I use this for testing to see if the correct methods have been removed.
	/*package*/ static final Set<MethodData> removedMethods = new HashSet<MethodData>();

	//I use this for testing to see if the correct classes have been removed.
	/*package*/ static final Set<String> removedClasses = new HashSet<String>();

	static final Set<FieldData> removedFields = new HashSet<FieldData>();

	//I use this for testing to see if the correct methods have been inlined.
	/*package*/ static InlineData inlineData = null;

	//I use this for testing to see if the correct methods, etc, have been collapsed.
	/*package*/ static ClassCollapserData classCollapserData = null;

	//I use the following for testing to ensure the right kind of method wipe has been used.
	/*package*/ static boolean removedMethod = false;
	/*package*/ static boolean wipedMethodBody = false;
	/*package*/ static boolean wipedMethodBodyWithExceptionNoMessage = false;
	/*package*/ static boolean wipedMethodBodyWithExceptionAndMessage = false;

	/*package*/ static TestOutput testOutputBefore = null;
	/*package*/ static TestOutput testOutputAfter = null;

	/*package*/ static Map<String, String> unmodifiableClass = null;
	static BackupService backupService = null;

	public static void main(String[] args) {

		long startTime = System.nanoTime();
		//Re-initialise this each time Application is run (for testing).
		removedMethods.clear();
		removedClasses.clear();
		removedFields.clear();
		inlineData = null;
		classCollapserData = null;
		removedMethod = false;
		wipedMethodBody = false;
		wipedMethodBodyWithExceptionNoMessage = false;
		wipedMethodBodyWithExceptionAndMessage = false;
		testOutputBefore = null;
		testOutputAfter = null;
		unmodifiableClass = new HashMap<String, String>();

		StringBuilder toLog = new StringBuilder();

		//I just put this in to stop an error.
		PropertyConfigurator.configure(
				Application.class.getClassLoader().getResourceAsStream("log4j.properties"));

		//Load the command line arguments.
		ApplicationCommandLineParser commandLineParser = null;

		try {
			commandLineParser = new ApplicationCommandLineParser(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		assert (commandLineParser != null);

		//Setup which features we are going to use in JShrink. For now, baseline has them all turned off, otherwise all true
		JShrink.enable_type_dependency = !commandLineParser.useBaseline();
		JShrink.enable_member_visibility = !commandLineParser.useBaseline();
		JShrink.enable_super_class_recursion_check = !commandLineParser.useBaseline();
		JShrink.enable_annotation_updates = !commandLineParser.useBaseline();

		if(commandLineParser.collapseClasses() && commandLineParser.removeClasses()){
			//TODO: This inconsistency should be solved.
			System.err.println("WARNING: When the \"--class-collaper\" and \"--remove-classes\" flags are set, the" +
					"\"--remove-classes\" functionality changes to that of what is used in the Jax paper");
		}

		//TODO: Classes in which all methods are removed, and have no fields that are accessed, should be removed.

		EntryPointProcessor entryPointProcessor = new EntryPointProcessor(commandLineParser.includeMainEntryPoint(),
				commandLineParser.includePublicEntryPoints(),
				commandLineParser.includeTestEntryPoints(),
				commandLineParser.getCustomEntryPoints());

		// These can all be seen as TODOs for now.
		if (!commandLineParser.getMavenDirectory().isPresent()) {
			System.err.println("Sorry, we can only process Maven directories for now!");
			System.exit(1);
		}

		if(!commandLineParser.getClassesToIgnore().isEmpty()){
			System.err.println("Sorry, we do not support the \"classes to ignore\" functionality for now!");
			System.exit(1);
		}


		if(commandLineParser.isVerbose()){
			System.out.println("Creating jShrink instance...");
		}

		//Initialize the jShrink instance.
		JShrink jShrink = null;
		try {
			if(JShrink.instanceExists()){
				jShrink = JShrink.resetInstance(commandLineParser.getMavenDirectory().get(), entryPointProcessor,
						commandLineParser.getTamiflex(), commandLineParser.getJmtrace(), commandLineParser.useSpark(), commandLineParser.isVerbose(),
						commandLineParser.isRunTests(), commandLineParser.useCache(), commandLineParser.isIgnoreLibs());
			} else {
				jShrink = JShrink.createInstance(commandLineParser.getMavenDirectory().get(), entryPointProcessor,
						commandLineParser.getTamiflex(), commandLineParser.getJmtrace(), commandLineParser.useSpark(), commandLineParser.isVerbose(),
						commandLineParser.isRunTests(), commandLineParser.useCache(), commandLineParser.isIgnoreLibs());

			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		assert (jShrink != null);

		if(commandLineParser.isVerbose()){
			System.out.println("Done creating jShrink instance!");
			System.out.println("Making \"soot pass\"...");
		}
		long analysisStartTime = System.nanoTime();
		jShrink.makeSootPass();
		long analysisEndTime = System.nanoTime();

		unmodifiableClass.putAll(jShrink.getUnmodifiableClasses());

		if(commandLineParser.isVerbose()){
			System.out.println("Done making \"soot pass\"!");
		}

		toLog.append("app_size_before," + jShrink.getAppSize(true) + System.lineSeparator());
		toLog.append("libs_size_before," + jShrink.getLibSize(true) + System.lineSeparator());


		if(commandLineParser.isRunTests()) {
			testOutputBefore = jShrink.getTestOutput();
			if (!testOutputBefore.isTestBuildSuccess()) {
				System.err.println("Cannot build tests for the target application (after \"soot pass\").");
				System.exit(1);
			}

			toLog.append("tests_run_before," + testOutputBefore.getRun() + System.lineSeparator());
			toLog.append("tests_errors_before," + testOutputBefore.getErrors() + System.lineSeparator());
			toLog.append("tests_failed_before," + testOutputBefore.getFailures() + System.lineSeparator());
			toLog.append("tests_skipped_before," + testOutputBefore.getSkipped() + System.lineSeparator());
		}

		//Note the number of library and application methods and fields before and transformations.
		Set<MethodData> allAppMethodsBefore = jShrink.getAllAppMethods();
		Set<MethodData> allLibMethodsBefore = jShrink.getAllLibMethods();
		Set<FieldData> allAppFieldsBefore = jShrink.getAllAppFields();
		Set<FieldData> allLibFieldsBefore = jShrink.getAllLibFields();

		toLog.append("app_num_methods_before," + allAppMethodsBefore.size() + System.lineSeparator());
		toLog.append("libs_num_methods_before," + allLibMethodsBefore.size() + System.lineSeparator());
		toLog.append("app_num_fields_before," + allAppFieldsBefore.size() + System.lineSeparator());
		toLog.append("libs_num_fields_before," + allLibFieldsBefore.size() + System.lineSeparator());

		//These two sets will be used to keep track of the application and library methods and fields removed.
		Set<MethodData> appMethodsRemoved = new HashSet<MethodData>();
		Set<MethodData> libMethodsRemoved = new HashSet<MethodData>();
		Set<FieldData> appFieldsRemoved = new HashSet<FieldData>();
		Set<FieldData> libFieldsRemoved = new HashSet<FieldData>();
		Set<String> appClassesRemoved = new HashSet<String>();
		Set<String> libClassesRemoved = new HashSet<String>();
		Set<String> libClassesBeforeRemoved = libClassesRemoved;
		Set<MethodData> appMethodsBeforeRemoved = appMethodsRemoved;
		Set<MethodData> libMethodsBeforeRemoved = libMethodsRemoved;
		Set<String> appClassesBeforeRemoved = appClassesRemoved;
		Set<FieldData> libFieldsBeforeRemoved = libFieldsRemoved;
		Set<FieldData> appFieldsBeforeRemoved = appFieldsRemoved;

		//Keeping a note of these to add to the verbose log
		Set<MethodData> appMethodsUsed = new HashSet<MethodData>();
		Set<MethodData> libMethodsUsed = new HashSet<MethodData>();
		Set<MethodData> appMethodsUnused = new HashSet<MethodData>();
		Set<MethodData> libMethodsUnused = new HashSet<MethodData>();
		Set<String> appClassesUsed = new HashSet<String>();
		Set<String> libClassesUsed = new HashSet<String>();
		Set<String> appClassesUnused = new HashSet<String>();
		Set<String> libClassesUnused = new HashSet<String>();
		Set<FieldData> appFieldsUsed = new HashSet<FieldData>();
		Set<FieldData> libFieldsUsed = new HashSet<FieldData>();
		Set<FieldData> appFieldsUnused = new HashSet<FieldData>();
		Set<FieldData> libFieldsUnused = new HashSet<FieldData>();

		appMethodsUsed.addAll(jShrink.getUsedAppMethods());
		libMethodsUsed.addAll(jShrink.getUsedLibMethods());
		appMethodsUnused.addAll(jShrink.getAllAppMethods());
		appMethodsUnused.removeAll(appMethodsUsed);
		libMethodsUnused.addAll(jShrink.getAllLibMethods());
		libMethodsUnused.removeAll(libMethodsUsed);

		appClassesUsed.addAll(jShrink.getUsedAppClasses());
		libClassesUsed.addAll(jShrink.getUsedLibClasses());
		appClassesUnused.addAll(jShrink.getAllAppClasses());
		appClassesUnused.removeAll(appClassesUsed);
		libClassesUnused.addAll(jShrink.getAllLibClasses());
		libClassesUnused.removeAll(libClassesUsed);

		appFieldsUsed.addAll(jShrink.getUsedAppFields());
		libFieldsUsed.addAll(jShrink.getUsedLibFields());
		appFieldsUnused.addAll(jShrink.getAllAppFields());
		appFieldsUnused.removeAll(appFieldsUsed);
		libFieldsUnused.addAll(jShrink.getAllLibFields());
		libFieldsUnused.removeAll(libFieldsUsed);

		Set<String> allAppClasses = jShrink.getAllAppClasses();
		Set<String> allLibClasses = jShrink.getAllLibClasses();

		toLog.append("app_num_classes_before," + allAppClasses.size() + System.lineSeparator());
		toLog.append("lib_num_classes_before," + allLibClasses.size() + System.lineSeparator());

		if(commandLineParser.getBackupPath()!=null) {
			//commandLineParser.useCheckpoints();
			backupService = new BackupService(commandLineParser.getMavenDirectory().get(), commandLineParser.getBackupPath(),commandLineParser.isVerbose());
		}
		//create checkpoint
		if(backupService!=null){
			backupService.addCheckpoint("init");
		}

		//Run the method removal.
		if(!commandLineParser.isSkipMethodRemoval()) {
			if(commandLineParser.isVerbose()){
				System.out.println("Removing unused methods...");
			}
			Set<MethodData> appMethodsToRemove = new HashSet<MethodData>();
			Set<MethodData> libMethodsToRemove = new HashSet<MethodData>();
			libMethodsToRemove.addAll(jShrink.getAllLibMethods());
			libMethodsToRemove.removeAll(jShrink.getUsedLibMethods());
			if (commandLineParser.isPruneAppInstance()) {
				appMethodsToRemove.addAll(jShrink.getAllAppMethods());
				appMethodsToRemove.removeAll(jShrink.getUsedAppMethods());
			}

			// find all virtually invoked methods
			Set<MethodData> appVirtualMethodsToWipe = new HashSet<MethodData>();
			Set<MethodData> libVirtualMethodsToWipe = new HashSet<MethodData>();
			Set<MethodData> entryPoints = jShrink.getAllEntryPoints();
			Map<MethodData, Set<MethodData>> callGraph = jShrink.getSimplifiedCallGraph();
			for(MethodData m : jShrink.getUsedLibMethods()) {
				Set<MethodData> callers = callGraph.get(m);
				if(callers.isEmpty() && !entryPoints.contains(m)) {
					// this method is only invoked virtually
					libVirtualMethodsToWipe.add(m);
				}
			}
			if(commandLineParser.isPruneAppInstance()) {
				for(MethodData m : jShrink.getUsedAppMethods()) {
					Set<MethodData> callers = callGraph.get(m);
					if(callers.isEmpty() && !entryPoints.contains(m)) {
						// this method is only invoked virtually
						appVirtualMethodsToWipe.add(m);
					}
				}
			}

			if (commandLineParser.removeMethods()) {
				// commandLineParser.removeClasses() && !commandLineParser.collapseClasses() ensures that we only perform the kind of class removal
				// implemented in JShrink.removeMethods when running JRed
				// the kind of class removel implemented in JShrink.removeMethods removes a class if it is unused and it does not contain a static
				// field that is accessible from other classes, which is exactly the same as described in JRed
				appMethodsRemoved.addAll(jShrink.removeMethods(appMethodsToRemove, commandLineParser.removeClasses() && !commandLineParser.collapseClasses()));
				libMethodsRemoved.addAll(jShrink.removeMethods(libMethodsToRemove, commandLineParser.removeClasses() && !commandLineParser.collapseClasses()));

				// wipe the body of virtually invoked methods, keep their method headers
				appMethodsRemoved.addAll(jShrink.wipeMethods(appVirtualMethodsToWipe));
				libMethodsRemoved.addAll(jShrink.wipeMethods(libVirtualMethodsToWipe));

				removedMethod = true;
			}
			else if (commandLineParser.includeException()) {
				appMethodsRemoved.addAll(jShrink.wipeMethodAndAddException(appMethodsToRemove,
						commandLineParser.getExceptionMessage()));
				libMethodsRemoved.addAll(jShrink.wipeMethodAndAddException(libMethodsToRemove,
						commandLineParser.getExceptionMessage()));

				appMethodsRemoved.addAll(jShrink.wipeMethodAndAddException(appVirtualMethodsToWipe,
						commandLineParser.getExceptionMessage()));
				libMethodsRemoved.addAll(jShrink.wipeMethodAndAddException(libVirtualMethodsToWipe,
						commandLineParser.getExceptionMessage()));

				if (commandLineParser.getExceptionMessage().isPresent()) {
					wipedMethodBodyWithExceptionAndMessage = true;
				} else {
					wipedMethodBodyWithExceptionNoMessage = true;
				}
			}
			else {
				appMethodsRemoved.addAll(jShrink.wipeMethods(appMethodsToRemove));
				libMethodsRemoved.addAll(jShrink.wipeMethods(libMethodsToRemove));

				appMethodsRemoved.addAll(jShrink.wipeMethods(appVirtualMethodsToWipe));
				libMethodsRemoved.addAll(jShrink.wipeMethods(libVirtualMethodsToWipe));

				wipedMethodBody = true;
			}

			removedClasses.addAll(jShrink.classesToRemove());

			if(commandLineParser.isVerbose()){
				System.out.println("Done removing unused methods!");
			}

			//add new checkpoint and update
			if(!Application.applyAndValidateTransform(jShrink, "method-removal", toLog, appMethodsBeforeRemoved,
					allAppMethodsBefore,libMethodsBeforeRemoved,allLibMethodsBefore,appFieldsBeforeRemoved,allAppFieldsBefore,
					libFieldsBeforeRemoved,allLibFieldsBefore,appClassesBeforeRemoved,allAppClasses,libClassesBeforeRemoved,allLibClasses,
					commandLineParser,startTime
			))
			{
				return;
			}



			//Run the field removal
			if(commandLineParser.removedFields()) {
				if(commandLineParser.isVerbose()){
					System.out.println("Removing unused fields...");
					//store information before the transition
					libClassesBeforeRemoved = libClassesRemoved;
					appMethodsBeforeRemoved = appMethodsRemoved;
					libMethodsBeforeRemoved = libMethodsRemoved;
					appClassesBeforeRemoved = appClassesRemoved;
					libFieldsBeforeRemoved = libFieldsRemoved;
					appFieldsBeforeRemoved = appFieldsRemoved;
				}
				Set<FieldData> libFieldsToRemove = new HashSet<FieldData>();
				libFieldsToRemove.addAll(jShrink.getAllLibFields());
				libFieldsToRemove.removeAll(jShrink.getUsedLibFields());
				libFieldsRemoved.addAll(jShrink.removeFields(libFieldsToRemove));
				removedFields.addAll(libFieldsRemoved);

				if(commandLineParser.isPruneAppInstance()) {
					Set<FieldData> appFieldsToRemove = new HashSet<FieldData>();
					appFieldsToRemove.addAll(jShrink.getAllAppFields());
					appFieldsToRemove.removeAll(jShrink.getUsedAppFields());
					appFieldsRemoved.addAll(jShrink.removeFields(appFieldsToRemove));
					removedFields.addAll(appFieldsRemoved);
				}

				if(commandLineParser.isVerbose()){
					System.out.println("Done removing unused fields!");
				}

				if(!Application.applyAndValidateTransform(jShrink, "field-removal", toLog, appMethodsBeforeRemoved,
						allAppMethodsBefore,libMethodsBeforeRemoved,allLibMethodsBefore,appFieldsBeforeRemoved,allAppFieldsBefore,
						libFieldsBeforeRemoved,allLibFieldsBefore,appClassesBeforeRemoved,allAppClasses,libClassesBeforeRemoved,allLibClasses,
						commandLineParser,startTime
				)) return;
			}


			//Run the class collapser.
			if (commandLineParser.collapseClasses()) {
				if(commandLineParser.isVerbose()){
					System.out.println("Collapsing collapsable classes...");
					libClassesBeforeRemoved = libClassesRemoved;
					appMethodsBeforeRemoved = appMethodsRemoved;
					libMethodsBeforeRemoved = libMethodsRemoved;
					appClassesBeforeRemoved = appClassesRemoved;
					libFieldsBeforeRemoved = libFieldsRemoved;
					appFieldsBeforeRemoved = appFieldsRemoved;
				}
				classCollapserData = jShrink.collapseClasses(commandLineParser.isPruneAppInstance(), true, commandLineParser.removeClasses());

				//Update our sets to note what has been removed.

				appMethodsRemoved.addAll(classCollapserData.getRemovedMethods());
				libMethodsRemoved.addAll(classCollapserData.getRemovedMethods());
				appMethodsRemoved.retainAll(allAppMethodsBefore);
				libMethodsRemoved.retainAll(allLibMethodsBefore);

				removedClasses.addAll(jShrink.classesToRemove());
				if(commandLineParser.isVerbose()){
					System.out.println("Done collapsing collapsable classes!");
				}

				if(!Application.applyAndValidateTransform(jShrink, "class-collapser", toLog, appMethodsBeforeRemoved,
						allAppMethodsBefore,libMethodsBeforeRemoved,allLibMethodsBefore,appFieldsBeforeRemoved,allAppFieldsBefore,
						libFieldsBeforeRemoved,allLibFieldsBefore,appClassesBeforeRemoved,allAppClasses,libClassesBeforeRemoved,allLibClasses,
						commandLineParser,startTime
				))
				{
					return;
				}
			}

			// filter out unmodifiable classes after debloating
			filterUnmodifiableClassesAfterDebloating(jShrink, appMethodsRemoved,
					libMethodsRemoved, appFieldsRemoved, libFieldsRemoved);

//			jShrink.updateClassFiles();
		}


		//Run the method inliner.
		if (commandLineParser.inlineMethods()) {
			if(commandLineParser.isVerbose()){
				System.out.println("Inlining inlinable methods...");
				libClassesBeforeRemoved = libClassesRemoved;
				appMethodsBeforeRemoved = appMethodsRemoved;
				libMethodsBeforeRemoved = libMethodsRemoved;
				appClassesBeforeRemoved = appClassesRemoved;
				libFieldsBeforeRemoved = libFieldsRemoved;
				appFieldsBeforeRemoved = appFieldsRemoved;
			}
			inlineData = jShrink.inlineMethods(commandLineParser.isPruneAppInstance(), true);

			//Remove all the methods that have been inlined
			for(MethodData methodInlined : inlineData.getInlineLocations().keySet()){
				if (!jShrink.removeMethods(new HashSet<MethodData>(Arrays.asList(methodInlined))
						,commandLineParser.removeClasses()).isEmpty()) {
					if (allAppClasses.contains(methodInlined.getClassName())) {
						appMethodsRemoved.add(methodInlined);
					} else if (allLibClasses.contains(methodInlined.getClassName())) {
						libMethodsRemoved.add(methodInlined);
					}
				}
			}


			removedClasses.addAll(jShrink.classesToRemove());
			if(commandLineParser.isVerbose()){
				System.out.println("Done inlining inlinable methods!");
			}

			filterUnmodifiableClassesAfterDebloating(jShrink, appMethodsRemoved,
					libMethodsRemoved, appFieldsRemoved, libFieldsRemoved);

			if(!Application.applyAndValidateTransform(jShrink, "method-inline", toLog, appMethodsBeforeRemoved,
					allAppMethodsBefore,libMethodsBeforeRemoved,allLibMethodsBefore,appFieldsBeforeRemoved,allAppFieldsBefore,
					libFieldsBeforeRemoved,allLibFieldsBefore,appClassesBeforeRemoved,allAppClasses,libClassesBeforeRemoved,allLibClasses,
					commandLineParser,startTime
			))return;
		}

		toLog.append(jShrink.getLog());
		// update class files at the end of all transformations
		jShrink.updateClassFiles();

		appClassesRemoved.addAll(removedClasses);
		appClassesRemoved.retainAll(allAppClasses);
		libClassesRemoved.addAll(removedClasses);
		libClassesRemoved.retainAll(allLibClasses);

		toLog.append("app_num_methods_after," +
				(allAppMethodsBefore.size() - appMethodsRemoved.size()) + System.lineSeparator());
		toLog.append("libs_num_methods_after," +
				(allLibMethodsBefore.size() - libMethodsRemoved.size()) + System.lineSeparator());
		toLog.append("app_num_fields_after," +
				(allAppFieldsBefore.size() - appFieldsRemoved.size()) + System.lineSeparator());
		toLog.append("libs_num_fields_after," +
				(allLibFieldsBefore.size() - libFieldsRemoved.size()) + System.lineSeparator());
		toLog.append("app_num_classes_after," + (allAppClasses.size() - appClassesRemoved.size()) + System.lineSeparator());
		toLog.append("lib_num_classes_after," + (allLibClasses.size() - libClassesRemoved.size()) + System.lineSeparator());
		toLog.append("app_size_after," + jShrink.getAppSize(true) + System.lineSeparator());
		toLog.append("libs_size_after," + jShrink.getLibSize(true) + System.lineSeparator());

		if(commandLineParser.isRunTests()) {
			testOutputAfter = jShrink.getTestOutput();

			toLog.append("tests_run_after," + testOutputAfter.getRun() + System.lineSeparator());
			toLog.append("tests_errors_after," + testOutputAfter.getErrors() + System.lineSeparator());
			toLog.append("tests_failed_after," + testOutputAfter.getFailures() + System.lineSeparator());
			toLog.append("tests_skipped_after," + testOutputAfter.getSkipped() + System.lineSeparator());
		}

		removedMethods.addAll(appMethodsRemoved);
		removedMethods.addAll(libMethodsRemoved);


		//Populate the verboseLog
		StringBuilder toLogVerbose = new StringBuilder();
		for(MethodData methodData : appMethodsUsed){
			toLogVerbose.append("APP_METHOD_USED," + methodData.getSignature() + System.lineSeparator());
		}

		for(MethodData methodData : libMethodsUsed){
			toLogVerbose.append("Lib_METHOD_USED," + methodData.getSignature() + System.lineSeparator());
		}

		for(MethodData methodData : appMethodsUnused){
			toLogVerbose.append("APP_METHOD_UNUSED," + methodData.getSignature() + System.lineSeparator());
		}

		for(MethodData methodData : libMethodsUnused){
			toLogVerbose.append("LIB_METHOD_UNUSED," + methodData.getSignature() + System.lineSeparator());
		}

		for(String cls: appClassesUsed){
			toLogVerbose.append("APP_CLASS_USED," + cls + System.lineSeparator());
		}

		for(String cls: libClassesUsed){
			toLogVerbose.append("LIB_CLASS_USED," + cls + System.lineSeparator());
		}

		for(String cls: appClassesUnused){
			toLogVerbose.append("APP_CLASS_UNUSED," + cls + System.lineSeparator());
		}

		for(String cls: libClassesUnused){
			toLogVerbose.append("LIB_CLASS_UNUSED," + cls + System.lineSeparator());
		}

		for(FieldData fieldData : appFieldsUsed){
			toLogVerbose.append("APP_FIELD_USED," + fieldData.toString() + System.lineSeparator());
		}

		for(FieldData fieldData : libFieldsUsed){
			toLogVerbose.append("LIB_FIELD_USED," + fieldData.toString() + System.lineSeparator());
		}

		for(FieldData fieldData : appFieldsUnused){
			toLogVerbose.append("APP_FIELD_UNUSED," + fieldData.toString() + System.lineSeparator());
		}

		for(FieldData fieldData : libFieldsUnused){
			toLogVerbose.append("LIB_FIELD_UNUSED," + fieldData.toString() + System.lineSeparator());
		}

		for(String className : appClassesRemoved) {
			toLogVerbose.append("APP_CLASS_REMOVED," + className + System.lineSeparator());
		}

		for(String className : libClassesRemoved) {
			toLogVerbose.append("LIB_CLASS_REMOVED," + className + System.lineSeparator());
		}

		for(MethodData methodData : appMethodsRemoved){
			toLogVerbose.append("APP_METHOD_REMOVED," + methodData.getSignature() + System.lineSeparator());
		}

		for(MethodData methodData : libMethodsRemoved){
			toLogVerbose.append("LIB_METHOD_REMOVED," + methodData.getSignature() + System.lineSeparator());
		}

		for(FieldData fieldData : appFieldsRemoved){
			toLogVerbose.append("APP_FIELD_REMOVED," + fieldData.toString() + System.lineSeparator());
		}

		for(FieldData fieldData : appFieldsRemoved){
			toLogVerbose.append("LIB_FIELD_REMOVED," + fieldData.toString() + System.lineSeparator());
		}

		if(commandLineParser.collapseClasses() && classCollapserData!=null){
			for(String classString : classCollapserData.getClassesToRemove()){
				toLogVerbose.append("CLASS_REMOVED_VIA_CLASS_COLLAPSE," + classString + System.lineSeparator());
			}

			for(String classesToRewrite : classCollapserData.getClassesToRewrite()){
				toLogVerbose.append("CLASSES_TO_REWRITE_VIA_CLASS_COLLAPSE," + classesToRewrite + System.lineSeparator());
			}

			for(MethodData methodData : classCollapserData.getRemovedMethods()){
				toLogVerbose.append("METHODS_REMOVED_VIA_CLASS_COLLAPSE," + methodData +  System.lineSeparator());
			}

			toLogVerbose.append(ClassCollapser.log);
		}

		if(commandLineParser.inlineMethods() && inlineData != null){
			for(Map.Entry<MethodData, Set<MethodData>> inlineLocation : inlineData.getInlineLocations().entrySet()){
				for(MethodData inlinedTo : inlineLocation.getValue()) {
					toLogVerbose.append("INLINED_METHOD," + inlineLocation.getKey().getSubSignature() + " to "
							+ inlinedTo.getSignature() + System.lineSeparator());
					Optional<Set<MethodData>> ultimateLocationSet
							= inlineData.getUltimateInlineLocations(inlineLocation.getKey());
					if(ultimateLocationSet.isPresent()) {
						for (MethodData ultimateLocation : ultimateLocationSet.get()) {
							toLogVerbose.append("INLINED_METHOD_ULTIMATE_LOCATION,"
									+ inlineLocation.getKey().getSubSignature()
									+ " to " + ultimateLocation.getSignature() + System.lineSeparator());
						}
					}
				}
			}
		}


		long endTime = System.nanoTime();
		toLog.append("time_elapsed," + TimeUnit.NANOSECONDS.toSeconds((endTime - startTime)) + System.lineSeparator());
		outputToLogDirectory(commandLineParser.getLogDirectory(), toLog.toString(), toLogVerbose.toString(),
				commandLineParser.isRunTests() ? Optional.of(testOutputBefore.getTestOutputText()) : Optional.empty(),
				commandLineParser.isRunTests() ? Optional.of(testOutputAfter.getTestOutputText()) : Optional.empty(),
				unmodifiableClass);

		if(commandLineParser.isVerbose()){
			System.out.println("Output logging info to \"" + commandLineParser.getLogDirectory() + "\".");
		}
	}

	private static void outputToLogDirectory(File directory, String log, String verboseLog,
											 Optional<String> testOutputBefore, Optional<String> testOutputAfter,
											 Map<String, String> unmodifiableClasses){

		try {
			FileWriter fileWriter =
					new FileWriter(directory.getAbsolutePath() + File.separator + "log.dat");
			fileWriter.write(log);
			fileWriter.close();

			fileWriter =
					new FileWriter(directory.getAbsolutePath() + File.separator + "verbose_log.dat");
			fileWriter.write(verboseLog);
			fileWriter.close();

			if(testOutputBefore.isPresent()) {
				fileWriter =
						new FileWriter(directory.getAbsolutePath() + File.separator + "test_output_before.dat");
				fileWriter.write(testOutputBefore.get());
				fileWriter.close();
			}

			if(testOutputAfter.isPresent()) {
				fileWriter =
						new FileWriter(directory.getAbsolutePath() + File.separator + "test_output_after.dat");
				fileWriter.write(testOutputAfter.get());
				fileWriter.close();
			}

			StringBuilder unmodifiableClassesString = new StringBuilder();
			for(Map.Entry<String, String> entry : unmodifiableClasses.entrySet()){
				unmodifiableClassesString.append(entry.getKey() + System.lineSeparator() + entry.getValue()
						+ System.lineSeparator() + System.lineSeparator());
			}

			fileWriter =
					new FileWriter(directory.getAbsolutePath() + File.separator + "unmodifiable_classes_log.dat");
			fileWriter.write(unmodifiableClassesString.toString());
			fileWriter.close();

		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	private static boolean applyAndValidateTransform(JShrink jShrink, String transform, StringBuilder toLog,
													 Set<MethodData> appMethodsRemoved, Set<MethodData> allAppMethodsBefore,
													 Set<MethodData> libMethodsRemoved, Set<MethodData> allLibMethodsBefore, Set<FieldData> appFieldsRemoved,
													 Set<FieldData> allAppFieldsBefore, Set<FieldData> libFieldsRemoved,
													 Set<FieldData> allLibFieldsBefore, Set<String> appClassesRemoved,
													 Set<String> allAppClasses, Set<String> libClassesRemoved,
													 Set<String> allLibClasses, ApplicationCommandLineParser commandLineParser,
													 long startTime){
		if(backupService!=null){
			//create new copy
			backupService.addCheckpoint(transform);

			//update files
			jShrink.updateClassFilesAtPath(backupService.resolveFiles(jShrink.getClassPaths()));

			//conduct tests
			if(!backupService.validateLastCheckpoint()){
				//if not safe
				backupService.removeCheckpoint();
				backupService.revertToLast();

				//clean up all checkpoints
				while(backupService.removeCheckpoint()){}
				System.err.println("Exiting after checkpoint failure - "+transform);
				toLog.append(jShrink.getLog());
				// update class files at the end of all transformations
				// write the result after rollback
				//jShrink.updateClassFiles();


				toLog.append("app_num_methods_after," +
						(allAppMethodsBefore.size() - appMethodsRemoved.size()) + System.lineSeparator());
				toLog.append("libs_num_methods_after," +
						(allLibMethodsBefore.size() - libMethodsRemoved.size()) + System.lineSeparator());
				toLog.append("app_num_fields_after," +
						(allAppFieldsBefore.size() - appFieldsRemoved.size()) + System.lineSeparator());
				toLog.append("libs_num_fields_after," +
						(allLibFieldsBefore.size() - libFieldsRemoved.size()) + System.lineSeparator());
				toLog.append("app_num_classes_after," + (allAppClasses.size() - appClassesRemoved.size()) + System.lineSeparator());
				toLog.append("lib_num_classes_after," + (allLibClasses.size() - libClassesRemoved.size()) + System.lineSeparator());
				toLog.append("app_size_after," + jShrink.getAppSize(true) + System.lineSeparator());
				toLog.append("libs_size_after," + jShrink.getLibSize(true) + System.lineSeparator());

				if(commandLineParser.isRunTests()) {
					testOutputAfter = jShrink.getTestOutput();

					toLog.append("tests_run_after," + testOutputAfter.getRun() + System.lineSeparator());
					toLog.append("tests_errors_after," + testOutputAfter.getErrors() + System.lineSeparator());
					toLog.append("tests_failed_after," + testOutputAfter.getFailures() + System.lineSeparator());
					toLog.append("tests_skipped_after," + testOutputAfter.getSkipped() + System.lineSeparator());
				}

				removedMethods.addAll(appMethodsRemoved);
				removedMethods.addAll(libMethodsRemoved);


				//Populate the verboseLog
				StringBuilder toLogVerbose = new StringBuilder();


				for(String className : appClassesRemoved) {
					toLogVerbose.append("APP_CLASS_REMOVED," + className + System.lineSeparator());
				}

				for(String className : libClassesRemoved) {
					toLogVerbose.append("LIB_CLASS_REMOVED," + className + System.lineSeparator());
				}

				for(MethodData methodData : appMethodsRemoved){
					toLogVerbose.append("APP_METHOD_REMOVED," + methodData.getSignature() + System.lineSeparator());
				}

				for(MethodData methodData : libMethodsRemoved){
					toLogVerbose.append("LIB_METHOD_REMOVED," + methodData.getSignature() + System.lineSeparator());
				}

				for(FieldData fieldData : appFieldsRemoved){
					toLogVerbose.append("APP_FIELD_REMOVED," + fieldData.toString() + System.lineSeparator());
				}

				for(FieldData fieldData : appFieldsRemoved){
					toLogVerbose.append("LIB_FIELD_REMOVED," + fieldData.toString() + System.lineSeparator());
				}

				if(commandLineParser.collapseClasses() && classCollapserData!=null){
					for(String classString : classCollapserData.getClassesToRemove()){
						toLogVerbose.append("CLASS_REMOVED_VIA_CLASS_COLLAPSE," + classString + System.lineSeparator());
					}

					for(String classesToRewrite : classCollapserData.getClassesToRewrite()){
						toLogVerbose.append("CLASSES_TO_REWRITE_VIA_CLASS_COLLAPSE," + classesToRewrite + System.lineSeparator());
					}

					for(MethodData methodData : classCollapserData.getRemovedMethods()){
						toLogVerbose.append("METHODS_REMOVED_VIA_CLASS_COLLAPSE," + methodData +  System.lineSeparator());
					}

					toLogVerbose.append(ClassCollapser.log);
				}

				if(commandLineParser.inlineMethods() && inlineData != null){
					for(Map.Entry<MethodData, Set<MethodData>> inlineLocation : inlineData.getInlineLocations().entrySet()){
						for(MethodData inlinedTo : inlineLocation.getValue()) {
							toLogVerbose.append("INLINED_METHOD," + inlineLocation.getKey().getSubSignature() + " to "
									+ inlinedTo.getSignature() + System.lineSeparator());
							Optional<Set<MethodData>> ultimateLocationSet
									= inlineData.getUltimateInlineLocations(inlineLocation.getKey());
							if(ultimateLocationSet.isPresent()) {
								for (MethodData ultimateLocation : ultimateLocationSet.get()) {
									toLogVerbose.append("INLINED_METHOD_ULTIMATE_LOCATION,"
											+ inlineLocation.getKey().getSubSignature()
											+ " to " + ultimateLocation.getSignature() + System.lineSeparator());
								}
							}
						}
					}
				}


				long endTime = System.nanoTime();
				toLog.append("time_elapsed," + TimeUnit.NANOSECONDS.toSeconds((endTime - startTime)) + System.lineSeparator());
				outputToLogDirectory(commandLineParser.getLogDirectory(), toLog.toString(), toLogVerbose.toString(),
						commandLineParser.isRunTests() ? Optional.of(testOutputBefore.getTestOutputText()) : Optional.empty(),
						commandLineParser.isRunTests() ? Optional.of(testOutputAfter.getTestOutputText()) : Optional.empty(),
						unmodifiableClass);

				if(commandLineParser.isVerbose()){
					System.out.println("Output logging info to \"" + commandLineParser.getLogDirectory() + "\".");
				}
				return false;
			}
		}
		return true;
	}
	private static void filterUnmodifiableClassesAfterDebloating(JShrink jShrink, Set<MethodData> appMethodsRemoved,
																 Set<MethodData> libMethodsRemoved,
																 Set<FieldData> appFieldsRemoved,
																 Set<FieldData> libFieldsRemoved) {
		Set<String> classes = jShrink.filterUnmodifiableClass();
		HashSet<MethodData> methodsNotRemoved = new HashSet<MethodData>();
		for(MethodData removedMethod : appMethodsRemoved) {
			if(classes.contains(removedMethod.getClassName())) {
				methodsNotRemoved.add(removedMethod);
			}
		}
		appMethodsRemoved.removeAll(methodsNotRemoved);
		methodsNotRemoved.clear();
		for(MethodData removedMethod : libMethodsRemoved) {
			if(classes.contains(removedMethod.getClassName())) {
				methodsNotRemoved.add(removedMethod);
			}
		}
		libMethodsRemoved.removeAll(methodsNotRemoved);
		methodsNotRemoved.clear();
		HashSet<FieldData> fieldsNotRemoved = new HashSet<FieldData>();
		for(FieldData removedField : appFieldsRemoved) {
			if(classes.contains(removedField.getClassName())) {
				fieldsNotRemoved.add(removedField);
			}
		}
		appFieldsRemoved.removeAll(fieldsNotRemoved);
		fieldsNotRemoved.clear();
		for(FieldData removedField : libFieldsRemoved) {
			if(classes.contains(removedField.getClassName())) {
				fieldsNotRemoved.add(removedField);
			}
		}
		libFieldsRemoved.removeAll(fieldsNotRemoved);
		fieldsNotRemoved.clear();
	}
}
