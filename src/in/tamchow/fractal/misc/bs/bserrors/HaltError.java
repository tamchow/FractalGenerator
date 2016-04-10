package in.tamchow.fractal.misc.bs.bserrors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * BS interpreter's error on "HALT" signal by code ('\' in BS code)
 */
public class HaltError extends Error {
    String message;
    @Nullable
    Throwable cause;
    public HaltError(String message) {
        this.message = message;
        cause = null;
    }
    public HaltError(String message, Throwable cause) {
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