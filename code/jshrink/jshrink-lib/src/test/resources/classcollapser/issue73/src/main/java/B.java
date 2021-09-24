public class B extends A {

	public static B BInstance = new B();

	public B(){
		super("B");
	}

	@Override
	public String saySomething(){
		return "I am class B";
	}

	public final String uniqueToB(){
		return "I am unique to class B";
	}
}
