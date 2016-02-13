package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.helpers.MathUtils;
import in.tamchow.fractal.imgutils.Animation;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.LinearizedImageData;
import in.tamchow.fractal.imgutils.Pannable;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;

import java.io.Serializable;
import java.util.Random;
/**
 * Generates IFS fractals
 */
public class IFSGenerator implements Serializable, Pannable {
    ImageData plane;
    IFSFractalParams params;
    Matrix centre_offset, initial;
    int center_x, center_y;
    double zoom, zoom_factor, base_precision, scale;
    long depth;
    boolean completion;
    Publisher progressPublisher;
    public IFSGenerator(IFSFractalParams params, Publisher progressPublisher) {
        setParams(params); initIFS(params); this.progressPublisher = progressPublisher;
    }
    private void initIFS(IFSFractalParams params) {
        plane = new LinearizedImageData(params.getWidth(), params.getHeight()); resetCentre();
        setDepth(params.getDepth()); setZoom(params.getZoom()); setZoom_factor(params.getZoomlevel());
        setBase_precision(params.getBase_precision()); initial = null; completion = false;
        if (params.zoomConfig != null) {for (ZoomParams zoom : params.zoomConfig.zooms) {zoom(zoom);}}
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {zoom(zoom.centre_x, zoom.centre_y, zoom.level);} else {zoom(zoom.centre, zoom.level);}
    }
    public void zoom(Matrix centre_offset, double level) {
        setCentre_offset(centre_offset); setZoom_factor(level); setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    public void zoom(int cx, int cy, double level) {
        cx = MathUtils.boundsProtected(cx, plane.getWidth()); cy = MathUtils.boundsProtected(cy, plane.getHeight());
        setCentre_offset(fromCooordinates(cx, cy)); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));}
    public Matrix fromCooordinates(int x, int y) {
        x = MathUtils.boundsProtected(x, plane.getWidth()); y = MathUtils.boundsProtected(y, plane.getHeight());
        double[][] matrixData = new double[2][1]; matrixData[0][0] = ((((double) x) - center_x) / scale);
        matrixData[1][0] = ((center_y - ((double) y)) / scale); if (params.getSkew() != 0) {
            return MatrixOperations.add(MatrixOperations.multiply(Matrix.rotationMatrix2D(params.getSkew()), new Matrix(matrixData)), centre_offset);
        } else {
            return MatrixOperations.add(new Matrix(matrixData), centre_offset);
        }
    }
    public void resetCentre() {
        setCenter_x(plane.getWidth() / 2); setCenter_y(plane.getHeight() / 2); double[][] matrixData = new double[2][1];
        matrixData[0][0] = 0; matrixData[1][0] = 0; setCentre_offset(new Matrix(matrixData));}
    public void setWidth(int width) {
        IFSFractalParams modified = new IFSFractalParams(params); modified.setWidth(width); initIFS(modified);
    }
    public void setHeight(int height) {
        IFSFractalParams modified = new IFSFractalParams(params); modified.setHeight(height); initIFS(modified);
    }
    public IFSFractalParams getParams() {return params;}
    public void setParams(IFSFractalParams params) {this.params = new IFSFractalParams(params);}
    public long getDepth() {return depth;}
    public void setDepth(long depth) {this.depth = depth;}
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {this.base_precision = base_precision;}
    }
    public double calculateBasePrecision() {
        return ((plane.getHeight() >= plane.getWidth()) ? plane.getWidth() / 2 : plane.getHeight() / 2);
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
    public void generate() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCooordinates(random.nextInt(plane.getWidth()), random.nextInt(plane.getHeight()));
        } Matrix point = new Matrix(initial); for (long i = 0; i <= depth; i++) {
            int index = MathUtils.weightedRandom(params.getWeights()); int[] coord = toCooordinates(point);
            plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
            modifyPoint(point, index);
            if (point.equals(initial) || i == depth || isOutOfBounds(point)) {completion = true; break;}
            publishProgress(i);
        }
    }
    public boolean isOutOfBounds(Matrix point) {
        int x = (int) ((point.get(0, 0) * scale) + center_x), y = (int) (center_y - (point.get(1, 0) * scale));
        return x < 0 || y < 0 || x >= plane.getWidth() || y >= plane.getHeight();
    }
    public int[] toCooordinates(Matrix point) {
        point = MatrixOperations.subtract(point, centre_offset); if (params.getSkew() != 0) {
            point = MatrixOperations.multiply(Matrix.rotationMatrix2D(params.getSkew()).inverse(), point);
        }
        int x = (int) ((point.get(0, 0) * scale) + center_x), y = (int) (center_y - (point.get(1, 0) * scale));
        x = MathUtils.boundsProtected(x, plane.getWidth()); y = MathUtils.boundsProtected(y, plane.getHeight());
        return new int[]{x, y};
    }
    public synchronized void publishProgress(long val) {
        float completion = (((float) val) / depth) * 100.0f;
        progressPublisher.publish("% completion= " + completion + "%", completion);
    }
    private void modifyPoint(Matrix point, int index) {
        if (params.isIfsMode()) {
            double x = point.get(0, 0), y = point.get(1, 0);
            FunctionEvaluator fe = FunctionEvaluator.prepareIFS("x", x, y);
            point.set(0, 0, fe.evaluateForIFS(params.getXfunctions()[index])); fe.setVariableCode("y");
            fe.setZ_value(y + ""); point.set(1, 0, fe.evaluateForIFS(params.getYfunctions()[index]));
        } else {
            point = MatrixOperations.add(MatrixOperations.multiply(params.getTransforms()[index], point), params.getTranslators()[index]);
        }
    }
    public boolean isComplete() {return completion;}
    public Animation generateAnimation() {
        Animation animation = new Animation(params.getFps()); for (long i = 0; i <= depth && (!completion); i++) {
            generateStep(); publishProgress(i);
            if (params.getFrameskip() > 0 && i % params.getFrameskip() == 0) {animation.addFrame(plane);}
        } return animation;
    }
    public void generateStep() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCooordinates(random.nextInt(plane.getWidth()), random.nextInt(plane.getHeight()));
        } Matrix point = new Matrix(initial); int index = MathUtils.weightedRandom(params.getWeights());
        int[] coord = toCooordinates(point);
        plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
        modifyPoint(point, index);
        if (point.equals(initial) || isOutOfBounds(point)) completion = true;
    }
    @Override
    public void pan(int distance, double angle) {pan(distance, angle, false);}
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {zoom(center_x + x_dist, center_y + y_dist, zoom_factor);}
}