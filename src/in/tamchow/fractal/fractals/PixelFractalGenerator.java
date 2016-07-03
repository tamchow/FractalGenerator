package in.tamchow.fractal.fractals;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.graphicsutilities.containers.Pannable;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.Zoomable;
import in.tamchow.fractal.helpers.annotations.NotNull;

import static java.lang.Math.abs;
import static java.lang.Math.min;
/**
 * Indicates that the fractal generator (the implementor) generates the fractal on a pixel-per-pixel basis
 */
public abstract class PixelFractalGenerator extends FractalGenerator implements Zoomable, Pannable {
    protected PixelFractalGenerator() {
    }
    public double calculateBasePrecision() {
        return ((getImageHeight() >= getImageWidth()) ? getImageWidth() / 2 : getImageHeight() / 2);
    }
    public double calculateBasePrecision(double xstart, double xend, double ystart, double yend) {
        return min(getImageWidth() / (abs(abs(xend) + abs(xstart))), getImageHeight() / (abs(abs(yend) + abs(ystart))));
    }
    public abstract void resetCentre();
    public abstract int getConfiguredHeight();
    public abstract int getImageHeight();
    public abstract int getHeight();
    public abstract void setHeight(int height);
    public abstract int getConfiguredWidth();
    public abstract int getImageWidth();
    public abstract int getWidth();
    public abstract void setWidth(int width);
    public abstract PixelContainer getPlane();
    @Override
    public void doZooms(@NotNull ZoomConfig zoomConfig) {
        if (zoomConfig.hasZooms()) {
            for (@NotNull ZoomParams zoom : zoomConfig) {
                zoom(zoom);
            }
        }
    }
    @Override
    public String toString() {
        return super.toString() + ":width=" + getWidth() + ",height=" + getHeight();
    }
}