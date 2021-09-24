package edu.ucla.cs.jshrinklib.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class DependencyLogUtils {
	
	public static Set<String> resolveDependencies(File logFile, String name) throws IOException {
		HashSet<String> dependencies = new HashSet<String>();
		String log2 = FileUtils.readFileToString(logFile,
				Charset.defaultCharset());
		
		if(!log2.contains("BUILD SUCCESS")) {
			System.err.println("Warning: " + name + " is not resolved correctly.");
		}
		// count the number of dependencies
		String[] log2_lines = log2.split(System.lineSeparator());
		boolean start = false;
		for (String line : log2_lines) {
			if (line.startsWith("[INFO] The following files have been resolved:")) {
				start = true;
				continue;
			}

			if (start) {
				// truncate the INFO header
				String sub = line.substring(6).trim();
				if (sub.isEmpty()) {
					// the end of the dependency list
					start = false;
				} else {
					if(sub.equals("none")) {
						start = false;
						continue;
					}
					String groupId = sub.substring(0, sub.indexOf(':'));
					String sub2 = sub.substring(sub.indexOf(':') + 1);
					String artifactId = sub2.substring(0,
							sub2.indexOf(':'));
					String dep = groupId + ":" + artifactId;
					dependencies.add(dep);
				}
			}
		}
		
		return dependencies;
	}
	
	public static Set<String> detectUnusedDependencies(File logFile, String name) throws IOException {
		HashSet<String> unused = new HashSet<String>();
		String log = FileUtils.readFileToString(logFile,
				Charset.defaultCharset());
		
		if(!log.contains("BUILD SUCCESS")) {
			System.err.println("Warning: " + name + " is not resolved correctly.");
		}
		// count the number of dependencies
		String[] log2_lines = log.split(System.lineSeparator());
		boolean start = false;
		for (String line : log2_lines) {
			if (line.startsWith("[WARNING] Unused declared dependencies found:")) {
				start = true;
				continue;
			}

			if (start) {
				if(!line.startsWith("[WARNING]") || !line.contains(":")) {
					start = false;
					continue;
				}
				
				// truncate the WARNING header
				String sub = line.substring(9);
				String groupId = sub.substring(0, sub.indexOf(':')).trim();
				String sub2 = sub.substring(sub.indexOf(':') + 1);
				String artifactId = sub2.substring(0,
						sub2.indexOf(':')).trim();
				String dep = groupId + ":" + artifactId;
				unused.add(dep);
			}
		}
		
		return unused;
	}
}
