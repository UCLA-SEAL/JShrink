package edu.ucla.cs.jshrinklib.util;

import java.io.File;
import java.util.*;

public class FilePathProcessor {
    public static void process(Map<String, String> allClasses, String basePath, String prefix, String prefixFile) {
        File folder = new File(basePath);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; ++ i) {
            String absPath = listOfFiles[i].getAbsolutePath();
            if (listOfFiles[i].isFile() && absPath.substring(absPath.length() - 5).equals("class")) {
            	String name = listOfFiles[i].getName();
            	String className = name.substring(0, name.length() - 6);
            	if (prefix.equals("")) {
            		allClasses.put(className, name);
            	} else {
            		allClasses.put(prefix + "." + className, prefixFile + File.separator + name);
            	}
            } else if (listOfFiles[i].isDirectory()){
            	String newPrefix;
            	if (prefix.equals("")) {
            		newPrefix = listOfFiles[i].getName();
            	} else {
            		newPrefix = prefix + "." + listOfFiles[i].getName();
            	}
                FilePathProcessor.process(allClasses, absPath, newPrefix, prefixFile + File.separator + listOfFiles[i].getName());
            }
        }
    }
}
