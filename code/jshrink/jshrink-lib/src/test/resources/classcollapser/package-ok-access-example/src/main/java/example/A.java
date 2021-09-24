package example;

public class A {
	private final String classType;

	public A(String classType){
		this.classType = classType;
	}

	public String getClassType(){
		return this.classType;
	}

	//This class will be overridden in this case.
	public String saySomething(){
		return "I am Class A";
	}

	public final String uniqueToA(){
		return "I am unique to Class A";
	}
}
