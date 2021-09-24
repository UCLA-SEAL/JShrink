package edu.ucla.cs.jshrinklib.reachability;

import org.junit.Before;
import org.junit.Test;

public class ClassReferenceGraphTest {

    private ClassReferenceGraph dependencyGraph;

    @Before
    void setUp(){
        dependencyGraph = new ClassReferenceGraph();
    }

    @Test
    public void testSingleClassAddition(){
        String class_name = "";
        String class_path = "";
        dependencyGraph.addClass(class_name, class_path);
        dependencyGraph.getNodes();
        dependencyGraph.getReferences(class_name);
        dependencyGraph.getReferencedBy(class_name);
    }

    @Test
    public void testSingleClassAddition2(){

    }

    @Test
    public void testSingleClassNullError(){

    }

    @Test
    public void testSingleClassPathError(){

    }

    @Test
    public void testMultiClassAddition(){

    }

    @Test
    public void testMultiClassAddition2(){

    }

    @Test
    public void testMultiClassAdditionWithErrors(){

    }

}
