package in.tamchow.fractal.helpers.stack;
/**
 * Custom Stack Overflow Exception class
 */
public class StackOverflowException extends IndexOutOfBoundsException {
    private static final String DEFAULT_MESSAGE = "Stack Overflow";
    /**
     * Constructs the exception with a default message
     */
    public StackOverflowException() {
        this(DEFAULT_MESSAGE);
    }
    /**
     * Constructs the exception with a custom message
     *
     * @param message The custom message
     */
    public StackOverflowException(String message) {
        super(message);
    }
}
