import example.A;

public class B extends A {
	public B(){
		super("B");
	}

	@Override
	public String saySomething(){
		return "I am class B";
	}

	public final String uniqueToB(){
		Main.packagePrivateMethod();
		return "I am unique to class B";
	}
}
