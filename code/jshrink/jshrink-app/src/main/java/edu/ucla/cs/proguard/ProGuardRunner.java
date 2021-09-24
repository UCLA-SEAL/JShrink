package edu.ucla.cs.proguard;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProGuardRunner {
    private final String proguardJar;

    public ProGuardRunner() {
        URL res = getClass().getClassLoader().getResource("proguard.jar");
        File file = null;
        try {
            file = Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(file != null) {
            this.proguardJar = file.getAbsolutePath();
        } else {
            this.proguardJar = "";
        }
    }

    public void run(String jarFilePath, String dependenciesPath) throws IOException, InterruptedException {
        // write out the ProGuard config file
        String configFilePath = config(jarFilePath, dependenciesPath);

        // run ProGuard from command line
        String[] cmd = {"java", "-jar", proguardJar, "@" + configFilePath};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        InputStream stdout = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdout);
        BufferedReader br = new BufferedReader(isr);

        String output = null;
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }
        process.waitFor();
    }

    /* Note: ProcessBuilder does not work with wildcards. So do not give a classfile path like target/classes/*.class */
    public boolean bundleClassFiles(String baseDir, List<String> classFiles, String outputJarPath, boolean updateJar) throws IOException, InterruptedException {
        File f = new File(outputJarPath);

        String[] cmd = new String[3 + classFiles.size()];
        cmd[0] = "jar";
        if(updateJar) {
            cmd[1] = "uf";
        } else {
            if(f.exists()) {
                f.delete();
            }
            cmd[1] = "cf";
        }

        cmd[2] = outputJarPath;
        for(int i = 3; i < classFiles.size() + 3; i++) {
            cmd[i] = classFiles.get(i-3);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.directory(new File(baseDir));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        InputStream stdout = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdout);
        BufferedReader br = new BufferedReader(isr);

        String output = null;
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    public boolean bundleMavenProject(String projectPath, String outputJarPath) throws IOException, InterruptedException {
        File outputJar = new File(outputJarPath);
        if (outputJar.exists()) {
            outputJar.delete();
        }

        boolean updateJar = false;
        File dir = new File(projectPath);
        // find all target classes and test classes folders
        ArrayList<String> classFolders =  new ArrayList<String>();
        ArrayList<String> testFolders =  new ArrayList<String>();
        traverseFolders(dir, classFolders, testFolders);
        for(String classFolder : classFolders) {
            File f = new File(classFolder);
            ArrayList<String> files = new ArrayList<String>();
            for(File f2 : f.listFiles()) {
                files.add(f2.getName());
            }

            boolean exitCode = bundleClassFiles(classFolder, files, outputJarPath, updateJar);

            if(!exitCode) {
                return false;
            }
            updateJar = true;
        }

        return true;
    }

    private void traverseFolders(File dir, List<String> classFolders, List<String> testFolders) {
        for(File f : dir.listFiles()) {
            if(dir.getName().equals("target")) {
                if (f.isDirectory() && f.getName().equals("classes")) {
                    classFolders.add(f.getAbsolutePath());
                } else if (f.isDirectory() && f.getName().equals("test-classes")) {
                    testFolders.add(f.getAbsolutePath());
                }
            } else if (f.isDirectory()) {
                traverseFolders(f, classFolders, testFolders);
            }
        }
    }

    /* set dependenciesPath to an empty string if the input jar file contains all its dependencies */
    private String config(String jarFilePath, String dependenciesPath) {
        String lineBreak = System.lineSeparator();
        String jarFilePathNoExtension = jarFilePath.substring(0, jarFilePath.lastIndexOf('.'));

        String s = "-injars " + jarFilePath +lineBreak;
        s += "-outjars " + jarFilePathNoExtension + "_out.jar" + lineBreak;
        s += "-libraryjars  <java.home>/lib/rt.jar" + lineBreak;
        s += "-libraryjars  <java.home>/lib/jce.jar" + lineBreak;
        String[] dependencies = dependenciesPath.split(";");
        for(String dependency : dependencies) {
            s += "-libraryjars " + dependency + lineBreak;
        }
        s += "-dontobfuscate" + lineBreak;
        s += lineBreak;
        s += "-keep public class * {" + lineBreak;
        s += "    public *;" + lineBreak;
        s += "}";

        File configFile = new File(jarFilePathNoExtension + ".pro");
        try {
            FileUtils.write(configFile, s, Charset.defaultCharset(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configFile.getAbsolutePath();
    }
}
