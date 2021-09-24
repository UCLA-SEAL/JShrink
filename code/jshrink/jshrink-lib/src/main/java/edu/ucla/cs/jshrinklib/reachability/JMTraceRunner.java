package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.util.MavenUtils;
import edu.ucla.cs.jshrinklib.util.POMUtils;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class JMTraceRunner extends TamiFlexRunner{

	private String projectPath;
	private String mtraceJARPath, mtraceLibPath, agentPathInjection;

	private boolean resolvePath(){
		File mtrace_jar = new File(mtraceJARPath);
		File mtrace_lib = new File(mtraceLibPath);
		if(mtrace_jar.exists() && mtrace_lib.exists()) {
			// update the jar path with the absolute path
			// because 'mvn test' is run in the root directory of the given project
			// a relative path will not work
			this.mtraceJARPath = mtrace_jar.getAbsolutePath();
			this.mtraceLibPath = mtrace_lib.getAbsolutePath();
			this.agentPathInjection = "-Xbootclasspath/a:"+this.mtraceJARPath+" -agentpath:"+this.mtraceLibPath;
		} else {
			return false;
		}
		return true;
	}

	public void injectJMTrace(File pom_file) {
		POMUtils.addAgentToPOM(this.agentPathInjection, pom_file);
	}

	public JMTraceRunner(String JMTraceHomeDir, String mavenProjectPath) {
		super("", mavenProjectPath, false);
		projectPath = mavenProjectPath;
		mtraceJARPath = JMTraceHomeDir+File.separator+"jmtrace.jar";
		mtraceLibPath = JMTraceHomeDir+File.separator+"libjmtrace.so";
		this.resolvePath();
	}
	public void run(TamiFlexRunner tamiflex) throws IOException{
		super.accessed_classes=tamiflex.accessed_classes;
		super.used_methods=tamiflex.used_methods;
		super.accessed_fields=tamiflex.accessed_fields;
		super.used_methods_callers=tamiflex.used_methods_callers;
		this.run();
	}
	@Override
	public void run() throws IOException{
		if(!this.resolvePath()){
			System.err.println("[JMTraceRunner] Error: the JMTrace lib or jar does not exist in " + mtraceLibPath);
			return;
		}

		HashMap<String, File> modules = new HashMap<String, File>();
		MavenUtils.getModules(new File(projectPath), modules);

		//inject mtrace into all module pom files
		HashMap<String, File> POMMap = MavenUtils.backupModulePOMS(modules);
		for(Map.Entry<String, File> entry: POMMap.entrySet()) {
			// inject the java agent in the POM file
			injectJMTrace(entry.getValue());
		}
		try{
			//run the test to obtain traces
			boolean testResult = super.runMavenTest();
		}
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			// restore the modified pom files
			MavenUtils.restoreModulePOMS(POMMap);
		}

		//parse the results
		for(String artifact_id : modules.keySet()) {
			File dir = modules.get(artifact_id);
			analyze(artifact_id,dir.getAbsolutePath() + File.separator + "jmtrace.log");
		}
	}
	@Override
	public void analyze(String module, String log_path) {
		File log = new File(log_path);
		if(log.exists()){
			// classes that are referenced or instantiated via Java reflection
			//public HashMap<String, HashSet<String>> accessed_classes;

			// methods that are referenced or invoked via Java reflection
			//public HashMap<String, Map<String, Set<String>>> used_methods;
			HashSet class_set;
			Map<String, Set<String>> method_map;
			Set<String> used_callers;
			if(super.accessed_classes.containsKey(module)) {
				class_set = super.accessed_classes.get(module);
			} else {
				class_set = new HashSet<String>();
			}
			if(used_methods.containsKey(module)) {
				method_map = super.used_methods.get(module);
			} else {
				method_map = new HashMap<String, Set<String>>();
			}
			if(super.used_methods_callers.containsKey(module)) {
				used_callers = super.used_methods_callers.get(module);
			} else {
				used_callers = new HashSet<String>();
			}
			try {
				List<String> lines = FileUtils.readLines(log, Charset.defaultCharset());
				//skip lines till stats begin
				int start = lines.indexOf("Begin Class Stats");
				String currClass="";
				for(int i=start;i<lines.size();i++){
					String[] tokens = lines.get(i).split(",");
					if(tokens[0].equals("Class")){
						currClass = tokens[1].replaceAll("/",".");
						if(Long.parseLong(tokens[2])>=0){
							class_set.add(currClass);
						}
					}
					else if(tokens[0].equals("Method") && Long.parseLong(tokens[3])>0){
						String returnType = Type.getReturnType(tokens[2]).getClassName();
						Type[] ts = Type.getArgumentTypes(tokens[2]);
						StringJoiner args = new StringJoiner(",");
						for(Type t:ts){
							String c_name = t.getClassName();
							args.add(c_name);
							class_set.add(c_name);
						}
						String method_name = currClass+": "+returnType+" "+tokens[1]+"("+args+")";
						HashSet<String> callers = new HashSet<String>();
						if(tokens.length>7 && tokens[7].length()>1 && !tokens[7].equals("(null)")){
							callers.addAll(Arrays.asList(tokens[7].split(";")).stream().map(x->{
								return x.replaceAll(": ","\\.");
							}).collect(Collectors.toList()));
						}
						method_map.put(method_name, callers);
						used_callers.addAll(callers);
					}
				}
				super.accessed_classes.put(module, class_set);
				super.used_methods.put(module,method_map);
				super.used_methods_callers.put(module, used_callers);
				//creating empty placeholders for the other 2 arrays
				if(!super.accessed_fields.containsKey(module)) {
					super.accessed_fields.put(module, new HashSet<>());
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		else {
			System.err.println("[JMTraceRunner] Error: There is no jmtrace log file - " + log_path);
		}

		//super.analyze(module, log);
	}
}
