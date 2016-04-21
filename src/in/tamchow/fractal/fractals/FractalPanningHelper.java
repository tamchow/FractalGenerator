package in.tamchow.fractal.fractals;
import in.tamchow.fractal.fractals.IFS.IFSGenerator;
import in.tamchow.fractal.fractals.IFS.ThreadedIFSGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ComplexBrotFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ThreadedComplexBrotFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Pannable;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Helper class for on-demand panning of various {@link Pannable} types.
 * Not used by CLI (batch-zooming can be used for this purpose by maintaining same zoom level), but useful for GUI (not implemented)
 *
 * @see Pannable
 * @see PixelFractalGenerator
 * @see ComplexFractalGenerator
 * @see ComplexBrotFractalGenerator
 * @see IFSGenerator
 * @see ThreadedGenerator
 * @see ThreadedComplexFractalGenerator
 * @see ThreadedComplexBrotFractalGenerator
 * @see ThreadedIFSGenerator
 */
public class FractalPanningHelper {
    @NotNull
    public static PixelContainer pan(Pannable toPan, int distance, double angle) {
        return pan(toPan, distance, angle, false);
    }
    @NotNull
    public static PixelContainer pan(Pannable toPan, int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        return pan(toPan, (int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @SuppressWarnings("unchecked")
    @NotNull
    public static PixelContainer pan(Pannable toPanThis, int x_dist, int y_dist) {
        if (toPanThis instanceof PixelFractalGenerator) {
            @NotNull PixelFractalGenerator toPan = (PixelFractalGenerator) toPanThis;
            try {
                @NotNull PixelContainer plane = new PixelContainer(toPan.getPlane());
                plane.pan(toPan.getConfiguredWidth(), toPan.getConfiguredHeight(), x_dist, y_dist);
                return plane;
            } catch (UnsupportedOperationException rangeViolation) {
                //buffer exhausted, so we re-render completely
                if (toPanThis instanceof ComplexFractalGenerator) {
                    toPan.pan(x_dist, y_dist);
                    int start_x, end_x, start_y, end_y;
                    if (x_dist < 0) {
                        start_x = toPan.getConfiguredWidth() + x_dist;
                        end_x = toPan.getConfiguredWidth();
                    } else {
                        start_x = 0;
                        end_x = x_dist;
                    }
                    start_y = 0;
                    end_y = toPan.getConfiguredHeight();
                    @NotNull ThreadedComplexFractalGenerator panner = new ThreadedComplexFractalGenerator((ComplexFractalGenerator) toPan);
                    panner.generate(start_x, end_x, start_y, end_y);
                    if (y_dist < 0) {
                        start_y = 0;
                        end_y = (-y_dist);
                    } else {
                        start_y = toPan.getConfiguredHeight() - y_dist;
                        end_y = toPan.getConfiguredHeight();
                    }
                    start_x = 0;
                    end_x = toPan.getConfiguredWidth();
                    panner.generate(start_x, end_x, start_y, end_y);
                } else if (toPanThis instanceof ComplexBrotFractalGenerator &&
                        ((ComplexBrotFractalGenerator) toPanThis).isSequential()) {
                    toPan = (ComplexBrotFractalGenerator) toPanThis;
                    toPan.pan(x_dist, y_dist);
                    int start_x, end_x, start_y, end_y;
                    if (x_dist < 0) {
                        start_x = toPan.getConfiguredWidth() + x_dist;
                        end_x = toPan.getConfiguredWidth();
                    } else {
                        start_x = 0;
                        end_x = x_dist;
                    }
                    start_y = 0;
                    end_y = toPan.getConfiguredHeight();
                    @NotNull ThreadedComplexBrotFractalGenerator panner = new ThreadedComplexBrotFractalGenerator((ComplexBrotFractalGenerator) toPan);
                    panner.generate(start_x, end_x, start_y, end_y);
                    if (y_dist < 0) {
                        start_y = 0;
                        end_y = (-y_dist);
                    } else {
                        start_y = toPan.getConfiguredHeight() - y_dist;
                        end_y = toPan.getConfiguredHeight();
                    }
                    start_x = 0;
                    end_x = toPan.getConfiguredWidth();
                    panner.generate(start_x, end_x, start_y, end_y);
                } else {
                    toPan.pan(x_dist, y_dist);
                    ThreadedGenerator panner;
                    if (toPan instanceof ComplexBrotFractalGenerator &&
                            (!((ComplexBrotFractalGenerator) toPan).isSequential())) {
                        panner = new ThreadedComplexBrotFractalGenerator((ComplexBrotFractalGenerator) toPan);
                    } else {
                        panner = new ThreadedIFSGenerator((IFSGenerator) toPan);
                    }
                    panner.generate();
                }
                return toPan.getPlane().subImage(toPan.getWidth(), toPan.getHeight());
            }
        } else if (toPanThis instanceof PixelContainer) {
            toPanThis.pan(x_dist, y_dist);
            return (PixelContainer) toPanThis;
        } else {
            throw new UnsupportedOperationException("Object of type " + toPanThis.getClass() + "is not of type " + Pannable.class);
        }
    }
}