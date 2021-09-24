public class Main {
    public static void main(String[] args) {
        A a = new B(1, 2);
        B b = new B(3, 4);

        a.foo();
        b.foo();
    }
}
