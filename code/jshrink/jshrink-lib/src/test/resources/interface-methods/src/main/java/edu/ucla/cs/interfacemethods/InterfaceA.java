package edu.ucla.cs.interfacemethods;

public interface InterfaceA {
	default void helloWorld(){
		System.out.println("Hello world.");
	}

	default void goodbyeWorld(){
		System.out.println("Goodbye world.");
	}

	void interfaceAMethod();
}
