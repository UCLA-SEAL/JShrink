import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StandardStuffTest {

	@Test
	public void testA(){
		A a = new A("hello");
		assertEquals(true, a.bar());
	}

	@Test
	public void testSameLastName() {
		B b = new B("Zhang");
		assertTrue(b.hasSameLastName("Zhang"));
	}

	@Test
	public void testSameName() {
		B b = new B("Zhang", "Tianyi");
		assertTrue(b.hasSameName("Zhang", "Tianyi"));
	}
}
