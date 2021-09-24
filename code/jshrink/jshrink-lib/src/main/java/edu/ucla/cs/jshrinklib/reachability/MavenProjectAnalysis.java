package edu.ucla.cs.jshrinklib.reachability;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import soot.G;
import edu.ucla.cs.jshrinklib.util.DependencyLogUtils;

public class MavenProjectAnalysis {
	final static String maven_project_path = "/media/troy/Disk2/ONR/BigQuery/sample-projects";
	final static String csv_file_path = "analysis_result(cha).tsv";
	
	public static void main(String[] args) throws IOException {
		File output = new File(csv_file_path);
		
		// check for analyzed projects	
		Set<String> analyzed_project = new HashSet<String>();
		if(output.exists()) {
			List<String> lines = FileUtils.readLines(output, Charset.defaultCharset());
			for(String line : lines) {
				String[] ss = line.split("\t");
				if(ss.length > 1) {
					analyzed_project.add(ss[0]);
				}
			}
		}
		
		File rootDir = new File(maven_project_path);
		for (File proj : rootDir.listFiles()) {
			String proj_name = proj.getName();
			if (!proj_name.contains("_") || analyzed_project.contains(proj_name))
				continue;
			String usr_name = proj_name.substring(0, proj_name.indexOf('_'));
			String repo_name = proj_name.substring(proj_name.indexOf('_') + 1);
			String repo_link = "https://github.com/" + usr_name + "/"
					+ repo_name;

			File build_log = new File(proj.getAbsolutePath() + File.separator
					+ "onr_build.log");
			File test_log = new File(proj.getAbsolutePath() + File.separator
					+ "onr_test.log");
			if(build_log.exists() && test_log.exists()) {
				// build success
				String proj_path = proj.getAbsolutePath();
				
				if (proj_name.equals("thymeleaf_thymeleaf")) {
					// the folder of compiled test classes is thymeleaf-tests/target/test-classes
					// need to handle this somehow
				}
				
				// count the total number of dependencies
				int dependency_count = -1;
				File dependency_log = new File(proj_path + File.separator + "onr_dependency.log");
				if(dependency_log.exists()) {
					Set<String> dependencies = DependencyLogUtils.resolveDependencies(dependency_log, usr_name + "/" + repo_name);
					dependency_count = dependencies.size();
				}
				
				if(dependency_count > 40) {
					// start from small projects first
					continue;
				}

				MavenSingleProjectAnalyzer runner = new MavenSingleProjectAnalyzer(proj_path,
						new EntryPointProcessor(true, false, true,
							new HashSet<MethodData>()), Optional.empty(), Optional.empty(), false,
					false, true, true, false);
				runner.run();
				
				String record = proj_name + "\t"
						+ repo_link + "\t"
						+ dependency_count + "\t"
						+ runner.getLibClasses().size() + "\t"
						+ runner.getUsedLibClasses().size() + "\t"
						+ runner.getLibMethods().size() + "\t"
						+ runner.getUsedLibMethods().size() + "\t"
						+ runner.getAppClasses().size() + "\t"
						+ runner.getUsedAppClasses().size() + "\t"
						+ runner.getAppMethods().size() + "\t"
						+ runner.getUsedAppMethods().size() + "\t"
						+ System.lineSeparator();
				
				try {
					FileUtils.writeStringToFile(output, record, Charset.defaultCharset(), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
		
				G.reset();
			}
		}
	}
}
