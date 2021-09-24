import org.junit.ComparisonFailure;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AssertTest2{
    @Test
    public void testAssertNullNotEqualsString() {
        try {
            Assert1.assertEquals(null, "foo");
            Assert1.fail();
        } catch (ComparisonFailure1 e) {
            e.getMessage();
        }
        catch(Exception e){

        }
    }
    @Test(expected = ComparisonFailure.class)
    public void stringsNotEqual() {
        assertEquals("abc", "def");
    }

    @Test
    public void testAssertStringNotEqualsNull() {
        try {
            Assert1.assertEquals("foo", null);
            Assert1.fail();
        }
        catch(RuntimeException e){
            e.printStackTrace();
        }
        catch (ComparisonFailure1 e) {
            e.getMessage(); // why no assertion?
        }

    }
}