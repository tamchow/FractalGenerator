package in.tamchow.fractal.fractals;
import in.tamchow.fractal.imgutils.containers.Pannable;
import in.tamchow.fractal.imgutils.containers.Zoomable;
/**
 * Indicates that the fractal generator (the implementor) generates the fractal on a pixel-per-pixel basis
 */
public interface PixelFractalGenerator extends FractalGenerator, Zoomable, Pannable {
    double calculateBasePrecision();
    void resetCentre();
}