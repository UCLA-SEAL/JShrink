package edu.ucla.cs.jshrink.test.inliner;

public class Main {
	/*package*/ static int results = 0;
	public static void main(String[] args){
		A a = new A();
		results = a.toInlineOk();
		results += a.toInlineIllegal();
	}
}
