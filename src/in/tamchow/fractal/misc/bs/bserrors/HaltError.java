package in.tamchow.fractal.misc.bs.bserrors;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * BS interpreter's error on "HALT" signal by code ('\' in BS code)
 */
public class HaltError extends Error {
    private static final String DEFAULT_MESSAGE = "Error: Halt Signalled";
    public HaltError() {
        this(DEFAULT_MESSAGE);
    }
    public HaltError(String message) {
        this(null, message);
    }
    public HaltError(@Nullable Throwable cause) {
        this(cause, DEFAULT_MESSAGE);
    }
    public HaltError(@Nullable Throwable cause, String message) {
        super(message, cause);
    }
    @NotNull
    @Override
    public String getMessage() {
        return super.getMessage() + ((getCause() == null) ? "" : getCause().getMessage());
    }
    @NotNull
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + ((getCause() == null) ? "" : getCause().getLocalizedMessage());
    }
}