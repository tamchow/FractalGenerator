package in.tamchow.fractal.fractals.l_system;
/**
 * Indicates a grammar error during generation of an L-System Fractal
 */
public class LSGrammarException extends RuntimeException {
    public LSGrammarException(String message, Throwable cause) {
        super(message, cause);
    }
    public LSGrammarException(String message) {
        super(message);
    }
}