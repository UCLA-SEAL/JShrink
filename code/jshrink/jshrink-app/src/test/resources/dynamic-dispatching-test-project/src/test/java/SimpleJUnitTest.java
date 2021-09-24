import org.junit.Test;

public class SimpleJUnitTest {

    /***
     * This is a simple version of the NoSuchMethodError found in VerifierRuleTest when testing our approach on JUnit
     * @throws Exception
     */
	@Test
	public void testAnonymousClass() throws Exception {
		A a = new A() {
		    @Override
            protected void m() {
                System.out.println("override a concrete method in the super class.");
            }
        };
        a.m();
	}

	@Test
    public void testConcreteSubClass() {
	    A a = new B();
	    a.m();
    }
}
