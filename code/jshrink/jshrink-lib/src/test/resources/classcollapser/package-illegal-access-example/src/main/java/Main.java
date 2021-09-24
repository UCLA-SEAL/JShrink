import example.A;

public class Main {
	public static void main(String[] args){
		A example = new B();

		System.out.println("ClassType: " + example.getClassType());
		System.out.println("Something to say: " + example.saySomething());
		System.out.println("Something unique to A: " + example.uniqueToA());
		System.out.println("Something unique to B: " + ((B) example).uniqueToB());
	}

	/*package*/ static void packagePrivateMethod(){
		System.out.println("Hello world!");
	}
}
