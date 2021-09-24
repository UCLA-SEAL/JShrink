import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardStuffTest {

	@Test
	public void getStringTest(){
		StandardStuff s = new StandardStuff();
		assertEquals("Hello world", s.getString());
	}

	@Test
	public void publicAndTestedButUntouchedTest(){
		StandardStuff s = new StandardStuff();
		s.publicAndTestedButUntouched();
	}
}
