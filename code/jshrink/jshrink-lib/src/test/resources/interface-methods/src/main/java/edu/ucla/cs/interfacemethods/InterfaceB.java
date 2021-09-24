package edu.ucla.cs.interfacemethods;

public interface InterfaceB {
	default void foo(){
		System.out.println("foo");
	}

	default void bar(){
		System.out.println("bar");
	}

	void interfaceBMethod();
}
