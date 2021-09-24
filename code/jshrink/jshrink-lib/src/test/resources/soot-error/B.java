public class B {
    private void foo(int a) {
        System.out.println("yes");
    }

    public void bar() {
        B b = new B();
        b.foo(1);
    }
}