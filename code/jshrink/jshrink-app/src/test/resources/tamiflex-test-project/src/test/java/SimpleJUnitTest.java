import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class SimpleJUnitTest {

	@Test
	public void testAccessPrivateFields() throws Exception {
		Class<?> aClass = Class.forName("A");
		Field field = aClass.getDeclaredField("f5");
		field.setAccessible(true);
		A a = new A("hello");
		field.set(a, "java");
	}

	@Test
	public void testInvokeMethod() throws Exception {
		Class<?> aClass = Class.forName("A");
		Method method = aClass.getMethod("m3", null);
		A a = new A("hello");
		method.invoke(a, null);
	}

	@Test
	public void testB() {
		assertFalse(A.class.equals(B.class));
	}
}
