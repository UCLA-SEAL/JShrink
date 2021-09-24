public class B extends A{
	String f2;

	public B(String s1, String s2) {
		super(s1);
		f2 = s2;
	}

	@Override
	public void foo(){
		print();
		System.out.println(f1 + " " + f2);
	}

	@Override
	public boolean bar() {
		return false;
	}

	public void print() {
		System.out.println(f1);
		System.out.println(f2);
	}
}
