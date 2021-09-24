package edu.ucla.cs.jshrinklib.reachability;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConstantPoolScanner {
    static class ConstantPoolReference{
        String ref_id, type, refs, comment;
        @Override
        public String toString(){
            return (this.ref_id+":"+this.type+":"+this.refs+":"+this.comment).replaceAll("/",".");
        }
    }
    private static ArrayList<ConstantPoolReference> javapRunner(File clazz) throws IOException, InterruptedException {
        ArrayList<ConstantPoolReference> outputStream = new ArrayList<ConstantPoolReference>();

        String[] cmd;
        ProcessBuilder processBuilder;
        Process process;
        InputStream stdout;
        InputStreamReader isr;
        BufferedReader br;
        String line;
        int exitValue;
        cmd = new String[]{"javap", "-v", clazz.getAbsolutePath()};
        processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        process = processBuilder.start();
        stdout = process.getInputStream();
        isr = new InputStreamReader(stdout);
        br = new BufferedReader(isr);

        while((line=br.readLine()) != null && !line.equals("Constant pool:")) {}
        while((line=br.readLine()) != null && !line.equals("{")) {
            try{
                outputStream.add(javapParse(line));
            }
            catch(Exception e){
                System.err.println("Could not parse line "+line+ " for class "+clazz.getAbsolutePath());
                e.printStackTrace();
            }
        }

        br.close();

        exitValue = process.waitFor();

        if(exitValue != 0) {
            throw new IOException("JAVAP cannot get type dependency information! The following was output:"
                    + System.lineSeparator() + isr.read());
        }
        return outputStream;
    }
    private static ConstantPoolReference javapParse(String line){
        ConstantPoolReference cpr = new ConstantPoolReference();
        int type_start = line.indexOf("= ") +2;
        cpr.ref_id = line.substring(0,type_start -3).trim();
        cpr.type = line.substring(type_start, line.length());
        int type_end = cpr.type.indexOf(" ");
        if(type_end <= 0){
            return cpr;
        }
        cpr.type = cpr.type.substring(0, type_end);

        int comment_start = line.indexOf("//");
        if (comment_start <= 0)
            comment_start = line.length();

        cpr.refs = line.substring(type_start+type_end, comment_start).trim();
        if(comment_start!=line.length()) {
            cpr.comment = line.substring(comment_start+2, line.length()).trim().replaceAll("\"","");
        }
        else{
            cpr.comment = "";
        }
        return cpr;//(ref_id, type, refs, comment);

    }
    public static ArrayList<ConstantPoolReference> getConstantPool(File classFile) throws IOException, InterruptedException{
        //TODO: May replace with better method to obtain constant pool
        return javapRunner(classFile);
    }
    private static Set<String> getClassReferencesFromPool(ArrayList<ConstantPoolReference> constantPool){
        Set<String> references = new HashSet<String>();
        for(ConstantPoolReference cpr: constantPool){
            if(cpr.type.equals("Class")){
                int s = 0, e=cpr.comment.length();
                while(cpr.comment.charAt(s)=='[' && s<e){
                    s++;
                }
                if(cpr.comment.charAt(s)=='L' && cpr.comment.charAt(e-1)==';') {s++; e--;}
                String className = cpr.comment.substring(s,e);
                if(className.length()==1 || className.startsWith("java") || className.startsWith("sun")){
                    continue;
                }
                references.add(className.replaceAll("/","."));
            }

        }
        return references;
    }

    /* This method obtains the constant pool through the javap -v command and returns all references of the class type
     * */

    public static Set<String> getClassReferences(String class_path) throws IOException, InterruptedException{
        return getClassReferencesFromPool(getConstantPool(new File(class_path)));
    }

    public static Set<String> getClassReferences(File clazz) throws IOException, InterruptedException{
        return getClassReferencesFromPool(getConstantPool(clazz));
    }
}
