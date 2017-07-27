package in.tamchow.fractal.config.fractalconfig.fractal_zooms;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.matrix.Matrix;

import java.io.Serializable;
/**
 * Holds data for a fractal zoom
 */
public class ZoomParams implements Serializable {
    public int centre_x, centre_y;
    @Nullable
    public Matrix centre, bounds;
    public double level;
    public ZoomParams(@NotNull ZoomParams old) {
        centre_x = old.centre_x;
        centre_y = old.centre_y;
        level = old.level;
        bounds = (old.bounds == null) ? null : new Matrix(old.bounds);
        centre = (old.centre == null) ? null : new Matrix(old.centre);
    }
    public ZoomParams(int centre_x, int centre_y, double level) {
        this.centre_x = centre_x;
        this.centre_y = centre_y;
        this.level = level;
        this.centre = null;
        this.bounds = null;
    }
    public ZoomParams(@NotNull Matrix points, double level) {
        centre = new Matrix(points);
        bounds = null;
        this.level = level;
    }
    public ZoomParams(@NotNull Matrix points) {
        bounds = new Matrix(points);
        centre = null;
    }
    @NotNull
    public static ZoomParams fromString(@NotNull String params) {
        @NotNull String[] parts = StringManipulator.split(params, " ");
        if (parts.length == 1) {
            return new ZoomParams(new Matrix(parts[0]));
        } else if (parts.length == 2) {
            return new ZoomParams(new Matrix(parts[0]), Double.parseDouble(parts[1]));
        }
        return new ZoomParams(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
    }
    @NotNull
    @Override
    public String toString() {
        if (centre == null && bounds != null) {
            return bounds.toString();
        } else if (centre != null && bounds == null) {
            return centre + " " + level;
        }
        return centre_x + " " + centre_y + " " + level;
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof ZoomParams && other.toString().equals(toString());
    }
}