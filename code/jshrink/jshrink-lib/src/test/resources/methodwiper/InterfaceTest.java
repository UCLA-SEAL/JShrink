public interface InterfaceTest {
	public void interface1();
	public String interface2();
	public void interface3(int x);
	default void defaultMethod(){
		System.out.println("Hello world");
	}
}
