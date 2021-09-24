public class A {
    B b;

    public A(B b) {
        this.b = b;
    }

    public void print() {
        System.out.print("A : " + b.name);
    }
}