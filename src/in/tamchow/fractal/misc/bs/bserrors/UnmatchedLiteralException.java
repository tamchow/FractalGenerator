package in.tamchow.fractal.misc.bs.bserrors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * BS unmatched literal exception
 */
public class UnmatchedLiteralException extends RuntimeException {
    String message;
    @Nullable
    Throwable cause;
    public UnmatchedLiteralException(String message) {
        this.message = message;
        cause = null;
    }
    public UnmatchedLiteralException(String message, Throwable cause) {
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