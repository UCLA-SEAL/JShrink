package edu.ucla.cs.jshrinklib.util;

import org.junit.After;
import org.junit.Test;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MethodBodyUtilsTest {
    private static SootClass getSootClassFromResources(String pathName, String className){
        File classFile = new File(
            MethodBodyUtils.class.getClassLoader().getResource(pathName + className + ".class").getFile());

        final String workingClasspath=classFile.getParentFile().getAbsolutePath();
        Options.v().set_soot_classpath(SootUtils.getJREJars() + File.pathSeparator + workingClasspath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        List<String> processDirs = new ArrayList<String>();
        processDirs.add(workingClasspath);
        Options.v().set_process_dir(processDirs);

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        Scene.v().loadNecessaryClasses();


        return sClass;
    }


    @After
    public void before(){
        G.reset();
    }

    @Test
    public void isEmptyConstructorTest_noConstructor() {
        SootClass A = getSootClassFromResources("methodbodyutils"
            + File.separator + "no_constructor" + File.separator, "A");
        SootClass B = getSootClassFromResources("methodbodyutils"
            + File.separator + "no_constructor" + File.separator, "B");

        assertTrue(MethodBodyUtils.isEmptyConstructor(A.getMethodByName("<init>")));
        assertTrue(MethodBodyUtils.isEmptyConstructor(B.getMethodByName("<init>")));
    }

    @Test
    public void isEmptyConstructorTest_emptyConstructor() {
        SootClass A = getSootClassFromResources("methodbodyutils"
            + File.separator + "empty_constructor" + File.separator, "A");
        SootClass B = getSootClassFromResources("methodbodyutils"
            + File.separator + "empty_constructor" + File.separator, "B");

        assertTrue(MethodBodyUtils.isEmptyConstructor(A.getMethodByName("<init>")));
        assertTrue(MethodBodyUtils.isEmptyConstructor(B.getMethodByName("<init>")));
    }

    @Test
    public void isEmptyConstructorTest_constructorWithBody() {
        SootClass A = getSootClassFromResources("methodbodyutils"
            + File.separator + "constructor_with_body" + File.separator, "A");

        assertFalse(MethodBodyUtils.isEmptyConstructor(A.getMethodByName("<init>")));

    }

    @Test
    public void isEmptyConstructorTest_parameterMismatch() {
        SootClass A = getSootClassFromResources("methodbodyutils"
            + File.separator + "constructor_with_parameter" + File.separator, "A");
        SootClass B = getSootClassFromResources("methodbodyutils"
            + File.separator + "constructor_with_parameter" + File.separator, "B");

        assertFalse(MethodBodyUtils.isEmptyConstructor(B.getMethodByName("<init>")));
    }
}
