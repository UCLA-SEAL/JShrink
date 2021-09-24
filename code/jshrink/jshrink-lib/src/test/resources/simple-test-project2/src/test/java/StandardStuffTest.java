import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardStuffTest {

	@Test
	public void testA(){
		A a = new A("hello");
		assertEquals(true, a.bar());
	}

	@Test
	public void testB(){
		B b = new B("hello", "world");
		assertEquals(false, b.bar());
	}

	@Test
	public void testC(){
		C c = new C("hello", 2019);
		assertEquals(true, c.bar());
	}
}
