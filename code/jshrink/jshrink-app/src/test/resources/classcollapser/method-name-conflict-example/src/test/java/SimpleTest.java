import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SimpleTest {
    @Test
    public void testSubA() {
        SubA subA = new SubA();
        assertEquals("SubA: m", subA.m());
    }

    @Test
    public void testB() {
        SubA subA = new SubA();
        B b = new B(subA);
        assertEquals("SubA: m", b.f);
    }

    @Test
    public void testB4() {
        SubA subA = new SubA();
        B b = new B(subA);
        assertEquals("SubA: m", b.call(subA));
    }
}