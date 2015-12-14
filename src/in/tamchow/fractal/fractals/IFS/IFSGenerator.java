package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.imgutils.Animation;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.MathUtils;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;

import java.util.Random;
/**
 * Generates IFS fractals
 */
public class IFSGenerator {
    ImageData plane;
    IFSFractalParams params;
    Matrix centre_offset, initial;
    int center_x, center_y;
    double zoom, zoom_factor, base_precision, scale;
    long depth;
    boolean completion;
    public IFSGenerator(IFSFractalParams params) {
        setParams(params); plane = new ImageData(params.getWidth(), params.getHeight()); resetCentre();
        setDepth(params.getDepth()); setZoom(params.getZoom()); setZoom_factor(params.getZoomlevel());
        setBase_precision(params.getBase_precision()); initial = null; completion = false;
        if (params.zoomConfig != null) {for (ZoomParams zoom : params.zoomConfig.zooms) {zoom(zoom);}}
    }
    public void zoom(ZoomParams zoom) {
        zoom(zoom.centre_x, zoom.centre_y, zoom.level);
    }
    public void zoom(int cx, int cy, double level) {
        if (cx < 0) {
            cx = 0;
        } if (cy < 0) {
            cy = 0;
        } if (cx >= plane.getWidth()) {
            cx = plane.getWidth() - 1;
        } if (cy >= plane.getHeight()) {
            cy = plane.getHeight() - 1;
        } setCentre_offset(fromCooordinates(cx, cy)); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    public Matrix fromCooordinates(int x, int y) {
        double[][] matrixData = new double[2][1]; matrixData[0][0] = ((((double) x) - center_x) / scale);
        matrixData[1][0] = ((center_y - ((double) y)) / scale);
        return MatrixOperations.add(centre_offset, new Matrix(matrixData));
    }
    public void resetCentre() {
        setCenter_x(plane.getWidth() / 2); setCenter_y(plane.getHeight() / 2); double[][] matrixData = new double[2][1];
        matrixData[0][0] = 0; matrixData[1][0] = 0;
    }
    public IFSFractalParams getParams() {return params;}
    public void setParams(IFSFractalParams params) {this.params = new IFSFractalParams(params);}
    public long getDepth() {return depth;}
    public void setDepth(long depth) {this.depth = depth;}
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        this.base_precision = base_precision;
    }
    public double getZoom_factor() {
        return zoom_factor;
    }
    public void setZoom_factor(double zoom_factor) {
        this.zoom_factor = zoom_factor;
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public int getCenter_x() {return center_x;}
    public void setCenter_x(int center_x) {this.center_x = center_x;}
    public int getCenter_y() {return center_y;}
    public void setCenter_y(int center_y) {this.center_y = center_y;}
    public double getScale() {return scale;}
    public void setScale(double scale) {this.scale = scale;}
    public Matrix getCentre_offset() {return centre_offset;}
    public void setCentre_offset(Matrix centre_offset) {this.centre_offset = new Matrix(centre_offset);}
    public ImageData getPlane() {return plane;}
    public void resetBasePrecision() {
        setBase_precision((plane.getHeight() >= plane.getWidth()) ? plane.getWidth() / 2 : plane.getHeight() / 2);
    }
    public void generate() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCooordinates(random.nextInt(plane.getWidth() - 1), random.nextInt(plane.getHeight() - 1));
        } Matrix point = new Matrix(initial); for (long i = 0; i <= depth; i++) {
            int index = MathUtils.weightedRandom(params.getWeights()); int[] coord = toCooordinates(point);
            plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
            point = MatrixOperations.add(MatrixOperations.multiply(params.getTransforms()[index], point), params.getTranslators()[index]);
            if (point.equals(initial) || i == depth) {
                completion = true; break;
            }
        }
    }
    public int[] toCooordinates(Matrix point) {
        int x = (int) ((point.get(0, 0) * scale) + center_x), y = (int) (center_y - (point.get(1, 0) * scale));
        if (x < 0) {
            x = 0;
        } if (y < 0) {
            y = 0;
        } if (x >= plane.getWidth()) {
            x = plane.getWidth() - 1;
        } if (y >= plane.getHeight()) {
            y = plane.getHeight() - 1;
        } return new int[]{x, y};
    }
    public boolean isComplete() {return completion;}
    public Animation generateAnimation() {
        Animation animation = new Animation(); for (long i = 0; i <= depth && (!completion); i++) {
            generateStep(); animation.addFrame(plane);
        } return animation;
    }
    public void generateStep() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCooordinates(random.nextInt(plane.getWidth() - 1), random.nextInt(plane.getHeight() - 1));
        } Matrix point = new Matrix(initial); int index = MathUtils.weightedRandom(params.getWeights());
        int[] coord = toCooordinates(point);
        plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
        point = MatrixOperations.add(MatrixOperations.multiply(params.getTransforms()[index], point), params.getTranslators()[index]);
        if (point.equals(initial)) completion = true;
    }
}