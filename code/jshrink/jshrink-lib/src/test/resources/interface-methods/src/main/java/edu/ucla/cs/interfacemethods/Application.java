package edu.ucla.cs.interfacemethods;

public class Application {
	public static void main(String[] args){
		Implementor implementor = new Implementor();
		implementor.helloWorld();
		implementor.foo();
	}
}
