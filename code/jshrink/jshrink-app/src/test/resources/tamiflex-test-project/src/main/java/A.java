public class A{
	private static String f1 = "static";
	private String f2; // used in class constructors
	private String f3; // never used
	private String f4; // used in a dynamically invoked method, m3
	private String f5; // dynamically accessed

	public A(String s) {
		f2 = s;
	}

	public A(String s1, String s2) {
		f2 = s1;
		f3 = s2;
	}

	public void m1(){
		System.out.println("This is method 1");
	}

	public void m2() {
		System.out.println("This method is never used.");
	}

	public void m3() {
		f4 = "f4";
	}
}
