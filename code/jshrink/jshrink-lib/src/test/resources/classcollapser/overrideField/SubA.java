public class SubA extends A {
    SubB b;

    public SubA(SubB b) {
        super(b);
        this.b = b;
    }

    @Override
    public void print() {
        System.out.println("SubA : " + b.name);
    }
}