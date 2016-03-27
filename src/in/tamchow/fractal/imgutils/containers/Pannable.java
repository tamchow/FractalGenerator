package in.tamchow.fractal.imgutils.containers;
/**
 * A data type that can be panned.
 */
public interface Pannable {
    void pan(int distance, double angle);
    void pan(int distance, double angle, boolean flip_axes);
    void pan(int x_dist, int y_dist);
}