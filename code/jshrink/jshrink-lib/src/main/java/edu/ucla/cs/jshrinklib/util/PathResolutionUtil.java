package edu.ucla.cs.jshrinklib.util;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class PathResolutionUtil {
	private static HashMap<String, String> classPathMap = new HashMap<String, String>();
	private static HashSet<String> classPaths = new HashSet<>();
	public static void buildMap(Set<File> class_paths){
		for(File cp:class_paths) {
			classPaths.add(cp.getAbsolutePath());
			PathResolutionUtil.readClass(cp);
		}
	}
	public static String getClassPath(String className){
		if(PathResolutionUtil.classPathMap.containsKey(className))
			return PathResolutionUtil.classPathMap.get(className);
		else
		{
			String fp = className.replaceAll("\\.",File.separator)+".class";
			String path = "";
			for(String cp: classPaths){
				path = cp+File.separator+fp;
				if(new File(path).exists())
					return path;
			}
		}
		return null;
	}
	public static void readClass(File dir){
		try {
			if (dir.isDirectory()) {
				readClassFromDirectory(dir, "");
			}
			else {
				throw new IOException("Cannot read classes from '" + dir.getAbsolutePath() + "'. It is not a directory.");
			}
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void readClassFromDirectory(File dirPath, String prefix) {
		if(prefix.startsWith("/"))
			prefix = prefix.substring(1);
		if(!dirPath.exists()) {
			// fix NPE due to non-existent file
			System.err.println(dirPath.getAbsolutePath() + " does not exist.");
			return;
		}

		for(File f : dirPath.listFiles()) {
			if(f.isDirectory()) {
				readClassFromDirectory(f, prefix+"/"+dirPath.getName());
			} else {
				String fName = f.getName();
				if(fName.endsWith(".class")) {
					String class_package = "";
					int path_start = prefix.indexOf("/");
					if(path_start>-1){
						class_package = prefix.substring(prefix.indexOf("/")+1).replaceAll("/",".")+".";
					}
					//prefix shouldn't start from classes. or test-classes
					if(prefix.length()==0 && (dirPath.getName().equals("classes") || dirPath.getName().equals("test-classes"))){
						class_package = "";
					}
					else
						class_package += dirPath.getName()+".";
					PathResolutionUtil.classPathMap.put(class_package+fName.substring(0,fName.length()-6),f.getAbsolutePath());
				}
			}
		}
	}
}
