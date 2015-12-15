package in.tamchow.fractal.config.fractalconfig.fractal_zooms;

import java.io.Serializable;
/**
 * Holds data for a fractal zoom
 */
public class ZoomParams implements Serializable {
    public int centre_x, centre_y;
    public double level;
    public ZoomParams(ZoomParams old) {
        centre_x = old.centre_x; centre_y = old.centre_y; level = old.level;
    }
    public ZoomParams(int centre_x, int centre_y, double level) {
        this.centre_x = centre_x; this.centre_y = centre_y; this.level = level;
    }
    public static ZoomParams fromString(String params) {
        String[] parts = params.split(",");
        return new ZoomParams(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Double.valueOf(parts[2]));
    }
}
