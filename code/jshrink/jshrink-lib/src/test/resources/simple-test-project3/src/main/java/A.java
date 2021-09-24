public class A{
	static String f1;
	static String f2 = Constants.B; // the initialization of a static field will be compiled to an anonymous static method in Java bytecode
	String f3 = Constants.C; // the field initialization will be compiled to constructors in Java bytecode

	static {
		f1 = Constants.A; // all code in a static field will be compiled to an anonymous static method in Java bytecode
	}

	public A(String s) {
		f1 = s;
	}

	public A(String s1, String s2) {
		f1 = s1;
		f2 = s2;
	}

	public A(String s1, String s2, String s3) {
		f1 = s1;
		f2 = s2;
		f3 = s3;
	}

	public void foo(){
		System.out.println(f1);
	}

	public boolean bar() {
		return true;
	}
}
