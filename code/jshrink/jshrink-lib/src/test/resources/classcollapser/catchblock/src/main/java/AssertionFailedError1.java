
/**
 * Thrown when an assertion failed.
 */
public class AssertionFailedError1 extends AssertionError {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new AssertionFailedError without a detail message.
     */
    public AssertionFailedError1() {
    }

    /**
     * Constructs a new AssertionFailedError with the specified detail message.
     * A null message is replaced by an empty String.
     * @param message the detail message. The detail message is saved for later 
     * retrieval by the {@code Throwable.getMessage()} method.
     */
    public AssertionFailedError1(String message) {
        super(defaultString(message));
    }

    private static String defaultString(String message) {
        return message == null ? "" : message;
    }
    public String getMessage(){ return "";}
}