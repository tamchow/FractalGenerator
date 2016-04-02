package in.tamchow.fractal.fractals;
import in.tamchow.fractal.imgutils.containers.ImageData;
import in.tamchow.fractal.imgutils.containers.Pannable;
import in.tamchow.fractal.imgutils.containers.Zoomable;
/**
 * Indicates that the fractal generator (the implementor) generates the fractal on a pixel-per-pixel basis
 */
public interface PixelFractalGenerator extends FractalGenerator, Zoomable, Pannable {
    double calculateBasePrecision();
    void resetCentre();
    int getConfiguredHeight();
    int getImageHeight();
    int getHeight();
    void setHeight(int height);
    int getConfiguredWidth();
    int getImageWidth();
    int getWidth();
    void setWidth(int width);
    ImageData getPlane();
}