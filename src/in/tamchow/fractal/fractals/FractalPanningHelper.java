package in.tamchow.fractal.fractals;
import in.tamchow.fractal.fractals.IFS.IFSGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ComplexBrotFractalGenerator;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.Pannable;
/**
 * Helps to pan a fractal image on-demand, hence does not implement Pannable
 */
public class FractalPanningHelper {
    public static synchronized ImageData pan(Pannable toPan, int distance, double angle) {
        return pan(toPan, distance, angle, false);
    }
    public static synchronized ImageData pan(Pannable toPan, int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        return pan(toPan, (int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    public static synchronized ImageData pan(Pannable toPanthis, int x_dist, int y_dist) {
        if (toPanthis instanceof ComplexFractalGenerator) {
            ComplexFractalGenerator toPan = (ComplexFractalGenerator) toPanthis;
            toPan.pan(x_dist, y_dist);
            int start_x, end_x, start_y, end_y;
            if (x_dist < 0) {
                start_x = toPan.getArgand().getWidth() + x_dist;
                end_x = toPan.getArgand().getWidth();
            } else {
                start_x = 0;
                end_x = x_dist;
            }
            start_y = 0;
            end_y = toPan.getArgand().getHeight();
            ThreadedComplexFractalGenerator panner = new ThreadedComplexFractalGenerator(toPan);
            panner.generate(start_x, end_x, start_y, end_y);
            if (y_dist < 0) {
                start_y = 0;
                end_y = (-y_dist);
            } else {
                start_y = toPan.getArgand().getHeight() - y_dist;
                end_y = toPan.getArgand().getHeight();
            }
            start_x = 0;
            end_x = toPan.getArgand().getWidth();
            panner.generate(start_x, end_x, start_y, end_y);
            return toPan.getArgand();
        } else if (toPanthis instanceof IFSGenerator || toPanthis instanceof ComplexBrotFractalGenerator) {
            IFSGenerator toPan = (IFSGenerator) toPanthis;
            try {
                ImageData plane = new ImageData(toPan.getPlane());
                plane.pan(toPan.getParams().getWidth(), toPan.getParams().getHeight(), x_dist, y_dist);
                return plane;
            } catch (UnsupportedOperationException rangeViolation) {
                //buffer exhausted, so we re-render completely
                toPan.pan(x_dist, y_dist);
                toPan.generate();
                return toPan.getPlane().subImage(toPan.getParams().getWidth(), toPan.getParams().getHeight());
            }
        } else if (toPanthis instanceof ComplexBrotFractalGenerator) {
            ComplexBrotFractalGenerator toPan = (ComplexBrotFractalGenerator) toPanthis;
            try {
                ImageData plane = new ImageData(toPan.getPlane());
                plane.pan(toPan.getParams().getWidth(), toPan.getParams().getHeight(), x_dist, y_dist);
                return plane;
            } catch (UnsupportedOperationException rangeViolation) {
                //buffer exhausted, so we re-render completely
                toPan.pan(x_dist, y_dist);
                toPan.generate();
                return toPan.getPlane().subImage(toPan.getParams().getWidth(), toPan.getParams().getHeight());
            }
        } else if (toPanthis instanceof ImageData) {
            toPanthis.pan(x_dist, y_dist);
            return (ImageData) toPanthis;
        } else {
            throw new UnsupportedOperationException("Object not of Pannable type");
        }
    }
}