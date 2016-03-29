package in.tamchow.fractal.fractals;
import java.io.Serializable;
/**
 * Interface which indicates that an implementor can generate a fractal
 */
public interface FractalGenerator extends Serializable {
    void generate();
}