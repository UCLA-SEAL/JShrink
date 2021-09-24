package edu.ucla.cs.jshrinklib.fieldwiper;

import edu.ucla.cs.jshrinklib.util.SootUtils;
import soot.SootClass;
import soot.SootField;

public class FieldWiper {
    public static boolean removeField(SootField field, boolean verbose) {
        SootClass owningClass = field.getDeclaringClass();
//        if(SootUtils.modifiableSootClass(owningClass)) {
            owningClass.removeField(field);
//            if(!SootUtils.modifiableSootClass(owningClass)){
//                if(verbose) {
//                    System.out.println("Soot modification error occurs after removing fields in " + owningClass + ". This should not occur");
//                }
//                owningClass.addField(field);
//                return false;
//            }

            return true;
//        } else {
//            if(verbose) {
//                System.out.println("Soot modification error occurs before removing fields in " + owningClass + ". This should not occur.");
//            }
//        }
//        return false;
    }
}
