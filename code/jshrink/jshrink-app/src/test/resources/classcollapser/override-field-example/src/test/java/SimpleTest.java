import org.junit.Test;

public class SimpleTest {
    @Test
    public void testSubA() {
        SubB subB = new SubB();
        SubA subA = new SubA(subB);
        subA.print();
    }

    @Test
    public void testB() {
        B b = new B();
    }

    @Test
    public void testBClass() {
        System.out.println(B.class);
    }
}