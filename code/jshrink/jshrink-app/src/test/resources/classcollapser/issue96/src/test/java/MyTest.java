import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MyTest{

    @Test
    public void test1() {
        A a = new A();
        assertEquals("a", a.foo());
    }

    @Test
    public void test2() {
        C c = new C();
        assertEquals("", c.foo());
    }
}
