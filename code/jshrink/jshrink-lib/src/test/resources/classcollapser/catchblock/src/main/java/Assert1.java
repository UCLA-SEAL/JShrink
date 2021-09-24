public class Assert1 {

    protected Assert1() {
    }

    static public void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }


    static public void assertTrue(boolean condition) {
        assertTrue(null, condition);
    }

    static public void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    static public void assertFalse(boolean condition) {
        assertFalse(null, condition);
    }

    static public void fail(String message) {
        if (message == null) {
            throw new AssertionFailedError1();
        }
        throw new AssertionFailedError1(message);
    }

    static public void fail() {
        fail(null);
    }

    static public void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        failNotEquals(message, expected, actual);
    }


    static public void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }


    static public void assertEquals(String message, String expected, String actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        String cleanMessage = message == null ? "" : message;
        throw new ComparisonFailure1(cleanMessage, expected, actual);
    }


    static public void assertEquals(String expected, String actual) {
        assertEquals(null, expected, actual);
    }

    static public void assertEquals(String message, double expected, double actual, double delta) {
        if (Double.compare(expected, actual) == 0) {
            return;
        }
        if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, new Double(expected), new Double(actual));
        }
    }

    static public void assertEquals(double expected, double actual, double delta) {
        assertEquals(null, expected, actual, delta);
    }

    static public void assertEquals(String message, float expected, float actual, float delta) {
        if (Float.compare(expected, actual) == 0) {
            return;
        }
        if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, new Float(expected), new Float(actual));
        }
    }


    static public void assertEquals(float expected, float actual, float delta) {
        assertEquals(null, expected, actual, delta);
    }


    static public void assertEquals(String message, long expected, long actual) {
        assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
    }


    static public void assertEquals(long expected, long actual) {
        assertEquals(null, expected, actual);
    }


    static public void assertEquals(String message, boolean expected, boolean actual) {
        assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    /**
     * Asserts that two booleans are equal.
     */
    static public void assertEquals(boolean expected, boolean actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two bytes are equal. If they are not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertEquals(String message, byte expected, byte actual) {
        assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    /**
     * Asserts that two bytes are equal.
     */
    static public void assertEquals(byte expected, byte actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two chars are equal. If they are not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertEquals(String message, char expected, char actual) {
        assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

    /**
     * Asserts that two chars are equal.
     */
    static public void assertEquals(char expected, char actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two shorts are equal. If they are not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertEquals(String message, short expected, short actual) {
        assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
    }

    /**
     * Asserts that two shorts are equal.
     */
    static public void assertEquals(short expected, short actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two ints are equal. If they are not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertEquals(String message, int expected, int actual) {
        assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    /**
     * Asserts that two ints are equal.
     */
    static public void assertEquals(int expected, int actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that an object isn't null.
     */
    static public void assertNotNull(Object object) {
        assertNotNull(null, object);
    }

    /**
     * Asserts that an object isn't null. If it is
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertNotNull(String message, Object object) {
        assertTrue(message, object != null);
    }

    /**
     * Asserts that an object is null. If it isn't an {@link AssertionError} is
     * thrown.
     * Message contains: Expected: <null> but was: object
     *
     * @param object Object to check or <code>null</code>
     */
    static public void assertNull(Object object) {
        if (object != null) {
            assertNull("Expected: <null> but was: " + object.toString(), object);
        }
    }

    /**
     * Asserts that an object is null.  If it is not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertNull(String message, Object object) {
        assertTrue(message, object == null);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * an AssertionFailedError1 is thrown with the given message.
     */
    static public void assertSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        failNotSame(message, expected, actual);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * the same an AssertionFailedError1 is thrown.
     */
    static public void assertSame(Object expected, Object actual) {
        assertSame(null, expected, actual);
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError1 is thrown with the
     * given message.
     */
    static public void assertNotSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            failSame(message);
        }
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError1 is thrown.
     */
    static public void assertNotSame(Object expected, Object actual) {
        assertNotSame(null, expected, actual);
    }

    static public void failSame(String message) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected not same");
    }

    static public void failNotSame(String message, Object expected, Object actual) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
    }

    static public void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    public static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && message.length() > 0) {
            formatted = message + " ";
        }
        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }
}
