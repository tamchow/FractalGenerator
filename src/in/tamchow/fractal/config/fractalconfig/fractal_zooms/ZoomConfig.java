package in.tamchow.fractal.config.fractalconfig.fractal_zooms;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.io.Serializable;
/**
 * Holds a set of fractal zooms
 */
public class ZoomConfig implements Serializable {
    public ZoomParams[] zooms;
    public ZoomConfig() {
    }
    public ZoomConfig(@NotNull ZoomConfig old) {
        setZooms(old.zooms);
    }
    @NotNull
    public static ZoomConfig fromString(@NotNull String[] params) {
        @NotNull ZoomConfig zoom = new ZoomConfig();
        zoom.zooms = new ZoomParams[params.length];
        for (int i = 0; i < zoom.zooms.length; i++) {
            zoom.zooms[i] = ZoomParams.fromString(params[i]);
        }
        return zoom;
    }
    public void setZooms(@NotNull ZoomParams[] zooms) {
        this.zooms = new ZoomParams[zooms.length];
        for (int i = 0; i < zooms.length; i++) {
            this.zooms[i] = new ZoomParams(zooms[i]);
        }
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String representation = "[Zooms]";
        for (ZoomParams zoom : zooms) {
            representation += "\n" + zoom;
        }
        representation += "\n[EndZooms]";
        return representation;
    }
    public void addZoom(ZoomParams zoom) {
        @NotNull ZoomParams[] tmp = new ZoomParams[this.zooms.length];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = new ZoomParams(this.zooms[i]);
        }
        this.zooms = new ZoomParams[tmp.length + 1];
        for (int i = 0; i < tmp.length; i++) {
            this.zooms[i] = new ZoomParams(tmp[i]);
        }
        this.zooms[tmp.length] = zoom;
    }
}