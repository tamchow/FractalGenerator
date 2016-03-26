package in.tamchow.fractal.misc.bs.bserrors;
/**
 * BS unmatched literal exception
 */
public class UnmatchedLiteralException extends RuntimeException {
    String message;
    Throwable cause;
    public UnmatchedLiteralException(String message) {
        this.message = message;
        cause = null;
    }
    public UnmatchedLiteralException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }
    @Override
    public String toString() {
        return getMessage();
    }
    @Override
    public String getMessage() {
        return message + ((cause == null) ? "" : cause.getMessage());
    }
    @Override
    public String getLocalizedMessage() {
        return message + ((cause == null) ? "" : cause.getLocalizedMessage());
    }
}