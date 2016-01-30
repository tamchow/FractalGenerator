package in.tamchow.fractal.config.fractalconfig.fractal_zooms;
import java.io.Serializable;
/**
 * Holds a set of fractal zooms
 */
public class ZoomConfig implements Serializable {
    public ZoomParams[] zooms;
    public ZoomConfig() {}
    public ZoomConfig(ZoomConfig old) {
        setZooms(old.zooms);
    }
    public void setZooms(ZoomParams[] zooms) {
        this.zooms = new ZoomParams[zooms.length];
        for (int i = 0; i < zooms.length; i++) {this.zooms[i] = new ZoomParams(zooms[i]);}
    }
    public static ZoomConfig fromString(String[] params) {
        ZoomConfig zoom = new ZoomConfig(); zoom.zooms = new ZoomParams[params.length];
        for (int i = 0; i < zoom.zooms.length; i++) {zoom.zooms[i] = ZoomParams.fromString(params[i]);} return zoom;
    }
    @Override
    public String toString() {
        String representation = "[Zooms]"; for (ZoomParams zoom : zooms) {
            representation += "\n" + zoom;
        } representation += "\n[EndZooms]"; return representation;
    }
}
