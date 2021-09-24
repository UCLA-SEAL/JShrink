public class Main3{	
	public static void main(String[] args) {
		A b = new B("a", "b");
		delegate(b);
	}

	public static void delegate(A a) {
		a.foo();
	}
}
