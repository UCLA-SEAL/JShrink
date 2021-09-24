package edu.ucla.cs.jshrinklib.util;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

import java.util.Iterator;

public class MethodBodyUtils {
    public static boolean isEmptyConstructor(SootMethod method) {
        Body body = method.retrieveActiveBody();
        Iterator<Unit> iterator = body.getUnits().iterator();
        if (!iterator.hasNext()) {
            return false;
        }
        Unit u0 = iterator.next();
        if (!(u0 instanceof IdentityStmt)) {
            return false;
        }
        if (!iterator.hasNext()) {
            return false;
        }
        Unit u1 = iterator.next();
        if (!(u1 instanceof InvokeStmt)) {
            return false;
        }
        InvokeExpr expr = ((InvokeStmt)u1).getInvokeExpr();
        SootMethod m = expr.getMethod();
        if (m.getDeclaringClass() != method.getDeclaringClass().getSuperclass()) {
            return false;
        }
        if (!m.getName().equals("<init>")) {
            return false;
        }
        if (!m.getParameterTypes().equals(method.getParameterTypes())) {
            System.out.println("mismatch");
            return false;
        }
        if (!iterator.hasNext()) {
            return false;
        }
        Unit u2 = iterator.next();
        if(!(u2 instanceof ReturnVoidStmt)) {
            return false;
        }
        if (iterator.hasNext()) {
            return false;
        }
        return true;
    }
}
