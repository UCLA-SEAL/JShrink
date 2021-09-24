public class Main {
    public static void main(String[] args) {
        A a = new C();
        a.foo();
        B b = new C();
        b.foo();
        C c = new C();
        c.foo();
        c.boo();
    }
}
