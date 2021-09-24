package edu.ucla.cs.jshrinklib.methodwiper;

import edu.ucla.cs.jshrinklib.util.ClassFileUtils;
import edu.ucla.cs.jshrinklib.util.SootUtils;
import soot.*;
import soot.jimple.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class was made to support the complete wiping of methods (while remaining compilable). I have tried to keep this
 * as simple as possible. The API either allows the wiping of methods an exception throw inserted,
 * `wipeMethodBody(SootMethod sootMethod)`, the wiping of a method, and including a RuntimeException
 * throw, `wipeMethodBodyAndInsertRuntimeException(SootMethod sootMethod)`, with a RuntimeException throw inc. message,
 * `wipeMethodBodyAndInsertRuntimeException(SootMethod sootMethod, String message)`, or with a custom thrown exception
 * (warning --- this is a bit of an advanced feature),
 * `wipeMethodAndInsertThrow(SootMethod scootMethod, Value toThrow)`.
 */
public class MethodWiper {

	private static final String RUNTIME_EXCEPTION_REF = "java.lang.RuntimeException";
	private static final String RUNTIME_EXCEPTION_INIT = "<java.lang.RuntimeException: void <init>()>";
	private static final String RUNTIME_EXCEPTION_INIT_WITH_MESSAGE =
		"<java.lang.RuntimeException: void <init>(java.lang.String)>";

	private static Body getBody(SootMethod sootMethod) {

		//Retrieve the active body
		sootMethod.retrieveActiveBody();

		Body toReturn = new JimpleBody();

		//Need to add 'this', if a non-static method
		Local thisLocal = null;
		if(!sootMethod.isStatic()) {

			SootClass declClass = sootMethod.getDeclaringClass();
			Type classType = declClass.getType();
			thisLocal = Jimple.v().newLocal("r0", classType);
			toReturn.getLocals().add(thisLocal);

			Unit thisIdentityStatement = Jimple.v().newIdentityStmt(thisLocal,
				Jimple.v().newThisRef(RefType.v(declClass)));
			toReturn.getUnits().add(thisIdentityStatement);

		}

		//Handle the parameters
		List<Type> parameterTypes = sootMethod.getParameterTypes();
		for (int i = 0; i < parameterTypes.size(); i++) {
			Type type = parameterTypes.get(i);
			Local arg = Jimple.v().newLocal("i" + Integer.toString(i), type);
			toReturn.getLocals().add(arg);

			Unit paramIdentifyStatement = Jimple.v().newIdentityStmt(arg, Jimple.v().newParameterRef(type, i));
			toReturn.getUnits().add(paramIdentifyStatement);
		}

		/* If the method is a contructor, I add a 'this()' call. This stops "java.lang.Verify.Error ... Constructor
	       must call 'super()' or 'this()'" errors
	    */
		if(sootMethod.isConstructor()){
			assert(thisLocal != null);
			SootClass superClass =  sootMethod.getDeclaringClass().getSuperclass();

			SootMethod superClassConstructor = null;
			for(SootMethod method : superClass.getMethods()){
				if(method.isConstructor()){
					if(superClassConstructor == null
						|| superClassConstructor.getParameterCount() > method.getParameterCount() ){
						superClassConstructor = method;
					}
				}
			}

			assert(superClassConstructor!=null);
			List<Value> parameters =new ArrayList<Value>();
			for(Type type : superClassConstructor.getParameterTypes()){
				parameters.add(getDefaultUnitFromType(type));
			}


			InvokeStmt superInvoke = Jimple.v().newInvokeStmt(
				Jimple.v().newSpecialInvokeExpr(thisLocal,superClassConstructor.makeRef(),parameters));
			toReturn.getUnits().add(superInvoke);
		}


		return toReturn;
	}

	private static Value getDefaultUnitFromType(Type type){

		Value toReturn = null;
		 if (type == LongType.v()) {
			toReturn = LongConstant.v(0);
		} else if (type == FloatType.v()) {
			toReturn = FloatConstant.v(0.0f);
		} else if (type == DoubleType.v()){
			toReturn = DoubleConstant.v(0.0);
		} else if (type == BooleanType.v() || type == IntType.v()
			|| type == ByteType.v() || type == ShortType.v() || type == CharType.v()) {
			return IntConstant.v(0);
		} else { //If not a primitive, must be an object (can therefore be null)
			return NullConstant.v();
		}

		return toReturn;
	}

	private static void addReturnUnit(Body body, SootMethod sootMethod) {
		Type returnType  = sootMethod.getReturnType();
		if(returnType == VoidType.v()){
			body.getUnits().add(Jimple.v().newReturnVoidStmt());
		} else {
			body.getUnits().add(Jimple.v().newReturnStmt(getDefaultUnitFromType(returnType)));
		}
	}

	private static void addThrowRuntimeException(Body body, Optional<String> message) {

		//Declare the locals
		RefType exceptionRef = RefType.v(RUNTIME_EXCEPTION_REF);
		Local localRuntimeException = Jimple.v().newLocal("r0", exceptionRef);
		body.getLocals().add(localRuntimeException);

		//$r0 = new java.lang.RuntimeException;
		AssignStmt assignStmt = Jimple.v().newAssignStmt(localRuntimeException, Jimple.v().newNewExpr(exceptionRef));
		body.getUnits().add(assignStmt);

		SpecialInvokeExpr sie;

		if (message.isPresent()) {
			//specialinvoke $r0.<java.lang.RuntimeException: void <init>(java.lang.String)>("ERROR");
			SootMethod runTimeExceptionMethod = Scene.v().getMethod(RUNTIME_EXCEPTION_INIT_WITH_MESSAGE);
			sie = Jimple.v().newSpecialInvokeExpr(localRuntimeException,
				runTimeExceptionMethod.makeRef(), StringConstant.v(message.get()));
		} else {
			//specialinvoke $r0.<java.lang.RuntimeException: void <init>(java.lang.String)>();
			SootMethod runTimeExceptionMethod = Scene.v().getMethod(RUNTIME_EXCEPTION_INIT);
			sie = Jimple.v().newSpecialInvokeExpr(localRuntimeException,
				runTimeExceptionMethod.makeRef());
		}

		InvokeStmt initStmt = Jimple.v().newInvokeStmt(sie);
		body.getUnits().add(initStmt);

		//throw $r0
		body.getUnits().add(Jimple.v().newThrowStmt(localRuntimeException));
	}

	private static boolean wipeMethodBody(SootMethod sootMethod, Optional<Optional<String>> exception){

		if(sootMethod.isAbstract() || sootMethod.isNative()){
			return false;
		}

		SootClass sootClass = sootMethod.getDeclaringClass();

		if(!SootUtils.modifiableSootClass(sootClass)){
			return false;
		}

		long originalSize = ClassFileUtils.getSize(sootClass);


		Body body = getBody(sootMethod);
		if(exception.isPresent()){
			addThrowRuntimeException(body, exception.get());
		} else {
			addReturnUnit(body, sootMethod);
		}

		Body oldBody = sootMethod.getActiveBody();
		body.setMethod(sootMethod);
		sootMethod.setActiveBody(body);

		long newSize = ClassFileUtils.getSize(sootClass);

		if(newSize >= originalSize){
			sootMethod.setActiveBody(oldBody);
			return false;
		}

		return true;
	}

	/**
	 * Wipes the method contents of a method's body and adds the bare minimum to ensure it remains compilable.
	 *
	 * @param sootMethod The method to be wiped
	 * @return a boolean specifying whether the method was wiped or not
	 */
	public static boolean wipeMethodBody(SootMethod sootMethod) {
		return wipeMethodBody(sootMethod, Optional.empty());
	}


	/**
	 * Deletes the method from the class
	 *
	 * Warning: May result in unpredictable behaviour
	 *
	 * @param sootMethod The method to be removed
	 * @return a boolean specifying whether the method was deleted or not
	 */
	public static boolean removeMethod(SootMethod sootMethod){

		if(sootMethod.isAbstract() || sootMethod.isNative()){
			return false;
		}

		SootClass sootClass = sootMethod.getDeclaringClass();
		//int index =sootClass.getMethods().indexOf(sootMethod);
		sootClass.removeMethod(sootMethod);
		if(!SootUtils.modifiableSootClass(sootClass)){
			//If not valid, re-add the method
			//sootClass.addMethod(sootMethod);
			sootClass.getOrAddMethod(sootMethod);
			//sootClass.getMethods().add(index, sootMethod);
			return false;
		}

		return true;
	}

	/**
	 * Wipes the contents of a methods body and inserts a throw statement; throws a RuntimeException
	 *
	 * @param sootMethod The method to be wiped
	 * @param message    The message to be thrown
	 * @return a boolean specifying whether the method was wiped or not
	 */
	public static boolean wipeMethodBodyAndInsertRuntimeException(SootMethod sootMethod, String message) {
		return wipeMethodBody(sootMethod,Optional.of(Optional.of(message)));
	}

	/**
	 * Wipes the contents of a methods body and inserts a throw statement; throws a RuntimeException
	 *
	 * @param sootMethod The method to be wiped
	 * @return a boolean specifying whether the method was wiped or not
	 */
	public static boolean wipeMethodBodyAndInsertRuntimeException(SootMethod sootMethod) {
		return wipeMethodBody(sootMethod, Optional.of(Optional.empty()));
	}

}

