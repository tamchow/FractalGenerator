package in.tamchow.fractal.misc.bs.bserrors;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * BS unmatched literal exception
 */
public class UnmatchedLiteralException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Error: Unmatched Literal";
    public UnmatchedLiteralException() {
        this(DEFAULT_MESSAGE);
    }
    public UnmatchedLiteralException(String message) {
        this(null, message);
    }
    public UnmatchedLiteralException(@Nullable Throwable cause) {
        this(cause, DEFAULT_MESSAGE);
    }
    public UnmatchedLiteralException(@Nullable Throwable cause, String message) {
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