public class B extends A {
	public B(){
		super("B");
	}

	@Override
	public String saySomething(){
		return getString();
	}

	private String getString() {
		// random computation
		int i = 0;
		i++;
		i--;
		return "I am class B";
	}

	public final String uniqueToB(){
		return "I am unique to class B";
	}
}
