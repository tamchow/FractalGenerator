package in.tamchow.fractal.fractals;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.graphicsutilities.containers.Pannable;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.Zoomable;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Indicates that the fractal generator (the implementor) generates the fractal on a pixel-per-pixel basis
 */
public abstract class PixelFractalGenerator extends FractalGenerator implements Zoomable, Pannable {
    protected PixelFractalGenerator() {
    }
    public abstract double calculateBasePrecision();
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