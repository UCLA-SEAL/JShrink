public class Main {
	public static void main(String[] args){
		Main.dynamicDispatch1(new Parent());
		Main.dynamicDispatch1(new Child());
		ChildsChild childsChild = new ChildsChild();
		Main.dynamicDispatch2(childsChild);
	}

	public static void dynamicDispatch1(Parent p){
		System.out.println(p.bla());
	}

	public static void dynamicDispatch2(Parent p){
		System.out.println(p.bla());
	}
}
