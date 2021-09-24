abstract class A {
    protected final SomeClass field;

    public A(SomeClass sc) {
        field = sc;
        field.someInterface.m();
    }
}