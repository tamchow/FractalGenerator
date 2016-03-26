package in.tamchow.fractal.misc.bs.bserrors;

/**
 * BS Unrecognized Literal Exception
 */
public class UnrecognizedLiteralException extends RuntimeException {
    String message;
    Throwable cause;

    public UnrecognizedLiteralException(String message) {
        this.message = message;
        cause = null;
    }

    public UnrecognizedLiteralException(String message, Throwable cause) {
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