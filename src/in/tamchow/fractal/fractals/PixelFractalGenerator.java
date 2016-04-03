package in.tamchow.fractal.fractals;
import in.tamchow.fractal.graphicsutilities.containers.Pannable;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.Zoomable;
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
    PixelContainer getPlane();
}