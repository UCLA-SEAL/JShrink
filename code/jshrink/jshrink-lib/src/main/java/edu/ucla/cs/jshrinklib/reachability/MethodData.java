package edu.ucla.cs.jshrinklib.reachability;

import soot.Scene;

import java.io.Serializable;

public class MethodData implements Serializable {
	private String name;
	private String className;
	private String[] args;
	private boolean isPublicMethod;
	private String returnType;
	private boolean isStaticMethod;
	private String annotation;

	/* Handle test cases written in JUnit 3*/
	private boolean isJUnit3Test = false;
	
	public boolean isJUnit3Test() {
		return isJUnit3Test;
	}
	
	public void setAsJUnit3Test() {
		isJUnit3Test = true;
	}

	/*
	Note: We only record whether a method is public or not. We do not record other access information.
	Therefore, the data stored here cannot constitute a full signature of the method. It is, however
	sufficient for our requirements.
	 */

	public MethodData(String methodName, String methodClassName, String methodReturnType,
	                  String[] methodArgs, boolean isPublic, boolean isStatic){
		this.setData(methodName, methodClassName, methodReturnType, methodArgs, isPublic, isStatic);
	}

	private void setData(String methodName, String methodClassName, String methodReturnType,
	                     String[] methodArgs, boolean isPublic, boolean isStatic){

		this.name=methodName;
		this.className = methodClassName;
		this.args = methodArgs;
		this.isPublicMethod = isPublic;
		this.returnType = methodReturnType;
		this.isStaticMethod = isStatic;
		this.annotation = "";
	}

	//I don't like this, but I can't construct MethodData with knowledge of whether it's annotated or not
	/*package*/ void setAnnotation(String annotation){
		this.annotation = annotation;
	}

	public String getName(){
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName(){
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String[] getArgs(){
		return this.args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public String getReturnType(){
		return this.returnType;
	}

	public void setReturnType(String type) {
		this.returnType = type;
	}

	public boolean isPublic(){
		return this.isPublicMethod;
	}

	public boolean isStatic(){
		return this.isStaticMethod;
	}

	public String getAnnotation(){
		return this.annotation;
	}

	public String getSubSignature(){
		StringBuilder stringBuilder = new StringBuilder();
		//stringBuilder.append((this.returnType.equals("void") ? "void" : Scene.v().quotedNameOf(this.returnType))
			//+ " " + Scene.v().quotedNameOf(this.name) + "(");
		String returnTypeProcess = this.returnType.contains(".") ? Scene.v().quotedNameOf(this.returnType) : this.returnType;
		stringBuilder.append(returnTypeProcess + " " + Scene.v().quotedNameOf(this.name) + "(");
		for(int i=0; i< this.args.length; i++){
			String argProcess = this.args[i].contains(".") ? Scene.v().quotedNameOf(this.args[i]) : this.args[i];
			stringBuilder.append(argProcess);
			if(i < this.args.length -1){
				stringBuilder.append(",");
			}
		}
		stringBuilder.append(")");

		return stringBuilder.toString();
	}

	public String getSignature(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<" + Scene.v().quotedNameOf(this.className) + ": ");

		if(this.isPublicMethod){
			stringBuilder.append("public ");
		}

		if(this.isStaticMethod){
			stringBuilder.append("static ");
		}

		stringBuilder.append(getSubSignature() + ">");

		return stringBuilder.toString();
	}

	@Override
	public String toString(){
		return getSignature();
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof MethodData){
			MethodData toCompare = (MethodData)o;
			if(this.name.equals(toCompare.name) && this.className.equals(toCompare.className)
				&& this.args.length == toCompare.args.length && this.returnType.equals(toCompare.returnType)){
				for(int i=0; i<this.args.length; i++){
					if(!this.args[i].equals(toCompare.args[i])){
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode(){
		int toReturn = this.name.length() * 1 + this.className.length() * 2
			 + this.returnType.length() * 16;

		for(int i=0; i<this.args.length; i++){
			toReturn += this.args[i].length() * Math.pow(2.0, (i+5));
		}

		return toReturn;
	}
}
