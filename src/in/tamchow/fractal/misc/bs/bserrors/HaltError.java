package in.tamchow.fractal.misc.bs.bserrors;
/**
 * BS interpreter's error on "HALT" signal by code ('\' in BS code)
 */
public class HaltError extends Error {
    String message;
    Throwable cause;
    public HaltError(String message) {this.message = message; cause = null;}
    public HaltError(String message, Throwable cause) {this.message = message; this.cause = cause;}
    @Override
    public String toString() {return getMessage();}
    @Override
    public String getMessage() {return message + ((cause == null) ? "" : cause.getMessage());}
    @Override
    public String getLocalizedMessage() {return message + ((cause == null) ? "" : cause.getLocalizedMessage());}
}