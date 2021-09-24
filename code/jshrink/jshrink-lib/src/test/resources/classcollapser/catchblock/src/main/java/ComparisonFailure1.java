
public class ComparisonFailure1 extends AssertionFailedError1 {
    private static final int MAX_CONTEXT_LENGTH = 20;
    private static final long serialVersionUID = 1L;

    private String fExpected;
    private String fActual;

    public ComparisonFailure1(String message, String expected, String actual) {
        super(message);
        fExpected = expected;
        fActual = actual;
    }


    @Override
    public String getMessage() {
        return "";
    }


    public String getActual() {
        return fActual;
    }


    public String getExpected() {
        return fExpected;
    }
}