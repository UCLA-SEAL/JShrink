import edu.ucla.cs.jshrinklib.TestUtils;
import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import org.junit.Test;
import soot.*;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SootTest {

    @Test
    public void testSootRewriteClassLiteralOnce() throws IOException {
        ClassLoader classLoader = SootTest.class.getClassLoader();
        String classPath = new File(classLoader.getResource("soot-error" + File.separator + "A.class").getFile()).getParentFile().getAbsolutePath();
        SootClass sootClass = TestUtils.getSootClass(classPath, "A");

        // first soot rewrite
        File outputFile = new File(classPath + File.separator + "A.class");
        File tmpFile = new File(classPath + File.separator + "A.temp");
        outputFile.renameTo(tmpFile);
        ClassFileUtils.writeClass(sootClass, outputFile);

        String[] cmd = new String[] {"java", "-cp", classPath, "A"};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        Process process = processBuilder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = "";
        String line;
        while((line = br.readLine()) != null) {
            output += line + System.lineSeparator();
        }
        assertEquals("class A\n", output);

        BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String error = "";
        String line2;
        while ((line2 = br2.readLine()) != null) {
            error += line2 + System.lineSeparator();
        }
        assertTrue(error.isEmpty());
    }

    @Test
    public void testSootRewriteClassLiteralTwice() throws IOException {
        // soot seems not handling X.class correctly when converting a class file to Jimple and convert it back
        ClassLoader classLoader = SootTest.class.getClassLoader();
        String classPath = new File(classLoader.getResource("soot-error" + File.separator + "A.class").getFile()).getParentFile().getAbsolutePath();
        SootClass sootClass = TestUtils.getSootClass(classPath, "A");

        for(SootMethod method : sootClass.getMethods()) {
            if(method.isConcrete()) method.retrieveActiveBody();
        }

        // first soot rewrite
        File outputFile = new File(classPath + File.separator + "A.class");
        File tmpFile = new File(classPath + File.separator + "A.temp");
        outputFile.renameTo(tmpFile);
        ClassFileUtils.writeClass(sootClass, outputFile);

        // must reset Soot before reloading the same class, otherwise we will get the cached Soot class
        G.reset();

        // reload the same class and do second soot rewrite
        sootClass = TestUtils.getSootClass(classPath, "A");
        for(SootMethod method : sootClass.getMethods()) {
            if(method.isConcrete()) method.retrieveActiveBody();
        }
        ClassFileUtils.writeClass(sootClass, outputFile);

        // run the class and we will get a bytecode validation error
        String[] cmd = new String[] {"java", "-cp", classPath, "A"};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        Process process = processBuilder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = "";
        String line;
        while((line = br.readLine()) != null) {
            output += line + System.lineSeparator();
        }
        assertEquals("class A\n", output);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String error = "";
        String line2;
        while ((line2 = br2.readLine()) != null) {
            error += line2 + System.lineSeparator();
        }
        assertTrue(error.isEmpty());
//        assertTrue(error.contains("ClassNotFoundException"));

        // restore the original class file
        outputFile.delete();
        tmpFile.renameTo(outputFile);
    }

    @Test
    public void testSimpleSootClass() {
        ClassLoader classLoader = SootTest.class.getClassLoader();
        String classPath = new File(classLoader.getResource("soot-error" + File.separator + "A.class").getFile()).getParentFile().getAbsolutePath();
        SootClass sootClass = TestUtils.getSootClass(classPath, "A");
        for(SootMethod method : sootClass.getMethods()) {
            Body b = method.retrieveActiveBody();
            for(Unit u : b.getUnits()) {
                if (u instanceof InvokeStmt) {
                    InvokeExpr expr = ((InvokeStmt) u).getInvokeExpr();
                    List<Value> args = expr.getArgs();
                    for(Value arg : args) {
                        if(arg instanceof ClassConstant) {
                            System.out.println(((ClassConstant) arg).getValue());
                        }
                    }
                }
                System.out.println(u);
            }
        }
    }
}
