package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.Pannable;

import java.io.Serializable;
/**
 * Complex brot fractal generator
 * TODO: Implement
 */
public class ComplexBrotFractalGenerator implements Serializable, Pannable {
    ComplexBrotFractalParams params;
    Publisher publisher;
    ImageData plane;
    int[][] escape_data;
    double[][] normalized_escapes;
    public ComplexBrotFractalGenerator(ComplexBrotFractalParams params, Publisher publisher) {
        this.params = params; this.publisher = publisher;
    }
    public int[][] getEscape_data() {return escape_data;}
    public double[][] getNormalized_escapes() {return normalized_escapes;}
    public ComplexBrotFractalParams getParams() {
        return params;
    }
    public void setParams(ComplexBrotFractalParams params) {
        this.params = params;
    }
    public ImageData getPlane() {
        return plane;
    }
    public void setPlane(ImageData plane) {
        this.plane = plane;
    }
    @Override
    public void pan(int distance, double angle) {
    }
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
    }
    @Override
    public void pan(int x_dist, int y_dist) {
    }
    public void generate() {}
}