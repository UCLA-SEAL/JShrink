package edu.ucla.cs.jshrink.test.inliner.package1;

import edu.ucla.cs.jshrink.test.inliner.package2.A;

public class Main {
	/*package*/ static int results = 0;
	public static void main(String[] args){
		A a = new A();
		results = a.toInline();
	}
}
