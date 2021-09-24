package edu.ucla.cs.jshrinklib;

import edu.ucla.cs.jshrinklib.reachability.MethodData;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {
    /*
     * Calling this method will cause the classpath of Soot to be reset
     * Therefore, do not use this method if you are also using Soot for other analysis
     */
    public static SootClass getSootClass(String classPath, String className){
        soot_setup(classPath);

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        Scene.v().loadNecessaryClasses();

        return sClass;
    }

    public static void soot_setup(String classPath) {
        Options.v().set_soot_classpath(SootUtils.getJREJars() + File.pathSeparator + classPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        List<String> processDirs = new ArrayList<String>();
        processDirs.add(classPath);
        Options.v().set_process_dir(processDirs);
    }

    public static File createClass(SootClass sootClass){

        // I receive 'Exception thrown: method <init> has no active body' if methods are not retrieved
        for(SootMethod sootMethod : sootClass.getMethods()){
            sootMethod.retrieveActiveBody();
        }

        File fileToReturn = null;
        try {
            String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_class);
            fileToReturn = new File(fileName);
            OutputStream streamOut = new JasminOutputStream(new FileOutputStream(fileToReturn));
            PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));

            JasminClass jasminClass = new JasminClass(sootClass);
            jasminClass.print(writerOut);
            writerOut.flush();
            streamOut.close();

        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        assert(fileToReturn != null);

        return fileToReturn;
    }

    public static String runClass(SootClass sootClass){
        File classFile = createClass(sootClass);
        String classPath = classFile.getParentFile().getAbsolutePath();
        String className = classFile.getName().replaceAll(".class","");
        return runClass(classPath, className);
    }

    public static String runClass(String classPath, String className) {
        String cmd = "java -cp "+ classPath + " " + className;

        Process p =null;
        StringBuilder output = new StringBuilder();
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            BufferedReader brInputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line;
            while((line = brInputStream.readLine())!=null){
                output.append(line + System.lineSeparator());
            }
            brInputStream.close();

            while((line = brErrorStream.readLine()) != null){
                output.append(line + System.lineSeparator());
            }
            brErrorStream.close();

            //} catch(IOException e InterruptedException ie){
        } catch(Exception e){
            System.err.println("Exception thrown when trying to run the following script:");
            StringBuilder sb = new StringBuilder();
            System.err.println(cmd);
            System.err.println("The following error was thrown: ");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return output.toString();
    }

    public static MethodData getMethodDataFromSignature(String signature) throws IOException{
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
}
