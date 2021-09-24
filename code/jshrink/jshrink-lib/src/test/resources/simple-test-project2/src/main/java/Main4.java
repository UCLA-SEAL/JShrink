public class Main4{	
	public static void main(String[] args) {
		A b = new B("a", "b");
		A c = new C("a", 1);
		delegate(b);
	}

	public static void delegate(A a) {
		a.foo();
	}
}
