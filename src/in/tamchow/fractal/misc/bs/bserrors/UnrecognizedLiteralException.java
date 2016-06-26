package in.tamchow.fractal.misc.bs.bserrors;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * BS Unrecognized Literal Exception
 */
public class UnrecognizedLiteralException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Error: Unrecognized Literal";
    public UnrecognizedLiteralException() {
        this(DEFAULT_MESSAGE);
    }
    public UnrecognizedLiteralException(String message) {
        this(null, message);
    }
    public UnrecognizedLiteralException(@Nullable Throwable cause) {
        this(cause, DEFAULT_MESSAGE);
    }
    public UnrecognizedLiteralException(@Nullable Throwable cause, String message) {
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