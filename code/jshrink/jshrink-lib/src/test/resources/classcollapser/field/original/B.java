public class B extends A{
    public int b;

    public B(int aa, int bb) {
        a = aa;
        b = bb;
    }

    public void foo() {
        System.out.printf("class B, a = %d, b = %d\n", a, b);
    }
}
