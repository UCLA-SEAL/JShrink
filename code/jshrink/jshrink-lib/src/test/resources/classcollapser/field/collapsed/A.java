public class A {
    public int a;
    public int b;

    public A() {
        a = 0;
    }

    public A(int aa, int bb) {
        a = aa;
        b = bb;
    }

    public void foo() {
        System.out.printf("class B, a = %d, b = %d\n", a, b);
    }
}
