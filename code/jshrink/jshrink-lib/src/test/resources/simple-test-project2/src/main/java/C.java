public class C extends A{
	int f2;

	public C(String s1, int i2) {
		super(s1);
		f2 = i2;
	}

	@Override
	public void foo(){
		System.out.println(f1 + " " + f2);
	}
}
