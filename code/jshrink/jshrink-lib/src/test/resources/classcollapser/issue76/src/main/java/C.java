public class C extends A {
	public C(){
		super("C");
	}

	@Override
	public String saySomething(){
		return "I am class C";
	}

	public final String uniqueToC(){
		return "I am unique to class C";
	}
}
