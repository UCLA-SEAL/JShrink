public class C extends A {
    public int c;

    public C(int aa, int cc) {
        a = aa;
        c = cc;
    }

    public void foo() {
        System.out.printf("class B, a = %d, c = %d\n", a, c);
    }
}
