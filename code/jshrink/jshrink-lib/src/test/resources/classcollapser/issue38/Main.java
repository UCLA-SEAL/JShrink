public class Main{
    public static void main(String[] args) {
        SomeInterfaceImplementation sii = new SomeInterfaceImplementation();
        SomeClass sc = new SomeClass(sii);
        B b = new B(sc);
    }
}