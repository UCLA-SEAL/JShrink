package edu.ucla.cs.jshrinklib.reachability;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ASMClassVisitorTest {
    @Test
    public void testVisitClassMethodsAndFields() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("ASMClassVisitor.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCalls);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(4, methods.size());
        assertEquals(5, fields.size());
        assertEquals(4, virtualCalls.size());
        Set<MethodData> allVirtualCalls = new HashSet<MethodData>();
        for(MethodData md : virtualCalls.keySet()) {
            allVirtualCalls.addAll(virtualCalls.get(md));
        }
        assertEquals(10, allVirtualCalls.size());

        FieldData fieldWithGenericType = new FieldData("classes", "edu.ucla.cs.jshrinklib.reachability.ASMClassVisitor", false, "java.util.Set");
        assertTrue(fields.contains(fieldWithGenericType));
    }

    @Test
    public void testVisitVirtualCalls() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("simple-test-project2" + File.separator + "target"
                + File.separator + "classes" + File.separator + "Main.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCallMap = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCallMap);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Main.class has two methods <init> and main(String[] args)
        assertEquals(2, virtualCallMap.size());
        MethodData mainMethod = new MethodData("main", "Main", "void", new String[] {"java.lang.String[]"}, true, true);
        Set<MethodData> virtualCalls = virtualCallMap.get(mainMethod);
        // Main.main has one virtual call to A.foo()
        assertEquals(1, virtualCalls.size());
    }

    @Test
    public void testVisitFieldReferences() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("simple-test-project"
                + File.separator + "target" + File.separator + "classes" + File.separator + "Main.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCalls);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            MethodData method1 = new MethodData("main", "Main", "void", new String[] {"java.lang.String[]"}, true, true);
            Set<FieldData> fieldReferences1 = fieldRefs.get(method1);
            assertNotNull(fieldReferences1);
            assertEquals(2, fieldReferences1.size());
            FieldData f1 = null;
            FieldData f2 = null;
            for(FieldData field : fieldReferences1) {
                if(field.getName().equals("f1")) {
                    f1 = field;
                } else if (field.getName().equals("f2")) {
                    f2 = field;
                }
            }
            assertNotNull(f1);
            assertFalse(f1.isStatic());
            assertNotNull(f2);
            assertTrue(f2.isStatic());

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVisitFieldReferences2() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("simple-test-project"
                + File.separator + "target" + File.separator + "classes" + File.separator + "StandardStuff.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCalls);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            MethodData method1 = new MethodData("getStringStatic", "StandardStuff", "java.lang.String", new String[] {"int"}, false, true);
            Set<FieldData> fieldReferences1 = fieldRefs.get(method1);
            assertNotNull(fieldReferences1);
            assertEquals(1, fieldReferences1.size());
            FieldData f1 = null;
            FieldData f2 = null;
            FieldData f3 = null;
            for(FieldData field : fieldReferences1) {
                if(field.getName().equals("HELLO_WORLD_STRING")) {
                    f1 = field;
                } else if (field.getName().equals("GOODBYE_STRING")) {
                    f2 = field;
                } else if (field.getName().equals("out")) {
                    f3 = field;
                }
            }
            assertNull(f1); // this private static final field is inlined by the Java compiler
            assertNull(f2);
            assertNotNull(f3);
            assertTrue(f3.isStatic());

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFieldReferenceCornerCases() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("simple-test-project3"
                + File.separator + "target" + File.separator + "classes" + File.separator + "A.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCalls);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            // check for the initialization of static fields. All of them will be compiled to an anonymous static method called <clinit>.
            MethodData method1 = new MethodData("<clinit>", "A", "void", new String[] {}, false, true);
            Set<FieldData> fieldReferences1 = fieldRefs.get(method1);
            assertNotNull(fieldReferences1);
            assertEquals(4, fieldReferences1.size());
            FieldData f1 = new FieldData("f1", "A", true,  "java.lang.String");
            FieldData f2 = new FieldData("f2", "A", true,  "java.lang.String");
            FieldData constantA = new FieldData("A", "Constants", true,  "java.lang.String");
            FieldData constantB = new FieldData("B", "Constants", true,  "java.lang.String");
            assertTrue(fieldReferences1.contains(f1));
            assertTrue(fieldReferences1.contains(f2));
            assertTrue(fieldReferences1.contains(constantA));
            assertTrue(fieldReferences1.contains(constantB));

            // check for the intialization of a non-static field. It will be compiled to all class constructors.
            // one constructor
            MethodData method2 = new MethodData("<init>", "A", "void", new String[] {"java.lang.String"}, true, false);
            Set<FieldData> fieldReferences2 = fieldRefs.get(method2);
            assertNotNull(fieldReferences2);
            assertEquals(3, fieldReferences2.size());
            FieldData f3 = new FieldData("f3", "A", false,  "java.lang.String");
            FieldData constantC = new FieldData("C", "Constants", true,  "java.lang.String");
            assertTrue(fieldReferences2.contains(f1));
            assertTrue(fieldReferences2.contains(f3));
            assertTrue(fieldReferences2.contains(constantC));

            // another constructor
            MethodData method3 = new MethodData("<init>", "A", "void",
                    new String[] {"java.lang.String", "java.lang.String"}, true, false);
            Set<FieldData> fieldReferences3 = fieldRefs.get(method3);
            assertNotNull(fieldReferences3);
            assertEquals(4, fieldReferences3.size());
            assertTrue(fieldReferences3.contains(f1));
            assertTrue(fieldReferences3.contains(f2));
            assertTrue(fieldReferences3.contains(f3));
            assertTrue(fieldReferences3.contains(constantC));

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVisitStaticFieldReference() {
        ClassLoader classLoader = ASMClassVisitorTest.class.getClassLoader();
        String pathToClassFile = classLoader.getResource("BeanConverterConfig.class").getFile();
        Set<String> classes = new HashSet<String>();
        Set<MethodData> methods = new HashSet<MethodData>();
        Set<FieldData> fields = new HashSet<FieldData>();
        Map<MethodData, Set<FieldData>> fieldRefs = new HashMap<MethodData, Set<FieldData>>();
        Map<MethodData, Set<MethodData>> virtualCalls = new HashMap<MethodData, Set<MethodData>>();
        try {
            FileInputStream fis = new FileInputStream(pathToClassFile);
            ClassReader cr = new ClassReader(fis);
            ASMClassVisitor cv = new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldRefs, virtualCalls);
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(8, methods.size());
        assertEquals(2, fields.size());

        FieldData staticField = new FieldData("BEANS_BY_CLASSLOADER", "com.blade.jdbc.model.BeanConverterConfig", true, "com.blade.jdbc.model.ContextClassLoaderLocal");
        assertTrue(fields.contains(staticField));
        for(MethodData method : fieldRefs.keySet()) {
            Set<FieldData> fieldRefsInMethod = fieldRefs.get(method);
            if(method.getName().equals("setInstance")) {
                assertTrue(fieldRefsInMethod.contains(staticField));
            }
        }
    }
}
