package edu.ucla.cs.interfacemethods;

public class Implementor implements InterfaceA, InterfaceB {
	@Override
	public void interfaceAMethod() {
		System.out.println("interfaceAMethod executed.");
	}

	@Override
	public void interfaceBMethod() {
		System.out.println("interfaceBMethod executed.");
	}

	@Override
	public void helloWorld(){
		System.out.println("hello world overwritten.");
	}

	@Override
	public void bar(){
		System.out.println("bar overwritten.");
	}
}
