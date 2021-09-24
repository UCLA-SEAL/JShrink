package edu.ucla.cs.jshrink.test.inliner;

public class A {
	public A(){}

	public int toInlineOk(){
		int toReturn = 0;
		toReturn += this.packagePrivateMethod();
		toReturn += this.packagePrivateMethod();
		return toReturn;
	}

	public int toInlineIllegal(){
		int toReturn = 0;
		toReturn += this.packagePrivateMethod();
		toReturn += this.packagePrivateMethod();
		toReturn += this.privateMethod();
		return toReturn;
	}

	public int packagePrivateMethod() {
			return privateMethod();
		}

	private int privateMethod(){
		return 2;
	}
}
