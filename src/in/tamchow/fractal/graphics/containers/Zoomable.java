package in.tamchow.fractal.graphics.containers;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
/**
 * Interface that indicates that a system can be zoomed into losslessly
 */
public interface Zoomable {
    void zoom(int cx, int cy, double level, boolean write, boolean additive);
    void zoom(ZoomParams zoom);
    void doZooms(ZoomConfig zoomConfig);
}