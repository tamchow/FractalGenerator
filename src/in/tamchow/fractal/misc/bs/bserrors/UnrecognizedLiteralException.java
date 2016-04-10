package in.tamchow.fractal.misc.bs.bserrors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * BS Unrecognized Literal Exception
 */
public class UnrecognizedLiteralException extends RuntimeException {
    String message;
    @Nullable
    Throwable cause;
    public UnrecognizedLiteralException(String message) {
        this.message = message;
        cause = null;
    }
    public UnrecognizedLiteralException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }
    @NotNull
    @Override
    public String toString() {
        return getMessage();
    }
    @NotNull
    @Override
    public String getMessage() {
        return message + ((cause == null) ? "" : cause.getMessage());
    }
    @NotNull
    @Override
    public String getLocalizedMessage() {
        return message + ((cause == null) ? "" : cause.getLocalizedMessage());
    }
}