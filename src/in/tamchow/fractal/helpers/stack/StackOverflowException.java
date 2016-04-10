package in.tamchow.fractal.helpers.stack;
/**
 * Custom Stack Overflow Exception class
 */
public class StackOverflowException extends IndexOutOfBoundsException {
    /**
     * Constructs the exception with a default message
     */
    public StackOverflowException() {
        this("Stack Overflow");
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
