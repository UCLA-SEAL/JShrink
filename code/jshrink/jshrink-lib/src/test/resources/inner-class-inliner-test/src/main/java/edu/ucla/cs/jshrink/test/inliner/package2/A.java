package edu.ucla.cs.jshrink.test.inliner.package2;

public class A {
	public A(){}

	public int toInline(){
		int toReturn = 0;
		toReturn += Inner.packagePrivateMethod();
		toReturn += Inner.packagePrivateMethod();
		return toReturn;
	}

	public static class Inner {
		public static int packagePrivateMethod() {
			return 1;
		}
	}
}
