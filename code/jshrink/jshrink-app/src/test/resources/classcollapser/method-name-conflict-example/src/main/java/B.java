public class B {
    public String f;

    public B (A a) {
        f = a.m();
    }

    public B (A a, int i) {
        f = a.m() + i;
    }

    public B (SubA a) {
        f = a.m();
    }


    public String call(A a) {
        return a.m();
    }

    public String call(SubA a) {
        return a.m();
    }

    public String call_sub(A a) {
        return a.m();
    }
}