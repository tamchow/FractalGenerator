package in.tamchow.fractal.config.fractalconfig.fractal_zooms;

import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.math.matrix.Matrix;

import java.io.Serializable;

/**
 * Holds data for a fractal zoom
 */
public class ZoomParams implements Serializable {
    public int centre_x, centre_y;
    public Matrix centre;
    public double level;

    public ZoomParams(ZoomParams old) {
        centre_x = old.centre_x;
        centre_y = old.centre_y;
        level = old.level;
        centre = (old.centre == null) ? null : new Matrix(old.centre);
    }

    public ZoomParams(int centre_x, int centre_y, double level) {
        this.centre_x = centre_x;
        this.centre_y = centre_y;
        this.level = level;
        this.centre = null;
    }

    public ZoomParams(Matrix centre, double level) {
        this.centre = new Matrix(centre);
        this.level = level;
    }

    public static ZoomParams fromString(String params) {
        String[] parts = StringManipulator.split(params, " ");
        if (parts.length == 2) {
            return new ZoomParams(Matrix.fromString(parts[0]), Double.valueOf(parts[1]));
        }
        return new ZoomParams(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Double.valueOf(parts[2]));
    }

    @Override
    public String toString() {
        if (centre == null) {
            return centre_x + " " + centre_y + " " + level;
        }
        return centre + " " + level;
    }
}