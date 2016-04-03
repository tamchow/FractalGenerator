package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;

import java.util.Random;
/**
 * Generates IFS fractals
 */
public class IFSGenerator implements PixelFractalGenerator {
    private static final double TOLERANCE = 1E-15;
    PixelContainer plane;
    IFSFractalParams params;
    Matrix centre_offset, initial, point;
    int center_x, center_y;
    double zoom, zoom_factor, base_precision, scale;
    int depth;
    boolean completion, silencer;
    Publisher progressPublisher;
    public IFSGenerator(IFSFractalParams params, Publisher progressPublisher) {
        setParams(params);
        initIFS(params);
        doZooms(params.zoomConfig);
        setProgressPublisher(progressPublisher);
    }
    @Override
    public void doZooms(ZoomConfig zoomConfig) {
        if (zoomConfig.zooms != null) {
            for (ZoomParams zoom : zoomConfig.zooms) {
                zoom(zoom);
            }
        }
    }
    @Override
    public int getConfiguredHeight() {
        return params.getHeight();
    }
    @Override
    public int getImageHeight() {
        return getPlane().getWidth();
    }
    @Override
    public int getHeight() {
        return getConfiguredHeight();
    }
    @Override
    public void setHeight(int height) {
        height = MathUtils.clamp(height, getPlane().getHeight());
        IFSFractalParams modified = new IFSFractalParams(params);
        modified.setHeight(height);
        initIFS(modified);
    }
    @Override
    public int getConfiguredWidth() {
        return params.getWidth();
    }
    @Override
    public int getImageWidth() {
        return getPlane().getWidth();
    }
    @Override
    public int getWidth() {
        return getConfiguredWidth();
    }
    @Override
    public void setWidth(int width) {
        width = MathUtils.clamp(width, getPlane().getWidth());
        IFSFractalParams modified = new IFSFractalParams(params);
        modified.setWidth(width);
        initIFS(modified);
    }
    private void initIFS(IFSFractalParams params) {
        plane = new LinearizedPixelContainer(params.getWidth(), params.getHeight());
        resetCentre();
        setDepth(params.getDepth());
        setZoom(params.getZoom());
        setZoom_factor(params.getZoomlevel());
        setBase_precision(params.getBase_precision());
        initial = null;
        completion = false;
        point = null;
        if (params.zoomConfig.zooms != null) {
            for (ZoomParams zoom : params.zoomConfig.zooms) {
                zoom(zoom);
            }
        }
        silencer = params.useThreadedGenerator();
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {
            zoom(zoom.centre_x, zoom.centre_y, zoom.level);
        } else {
            zoom(zoom.centre, zoom.level);
        }
    }
    public void zoom(Matrix centre_offset, double level) {
        setCentre_offset(centre_offset);
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    public void zoom(int cx, int cy, double level) {
        cx = MathUtils.boundsProtected(cx, plane.getWidth());
        cy = MathUtils.boundsProtected(cy, plane.getHeight());
        setCentre_offset(fromCoordinates(cx, cy));
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    public Matrix fromCoordinates(int x, int y) {
        Matrix point = new Matrix(new double[][]{{(MathUtils.boundsProtected(x, getImageWidth()) - center_x) / scale},
                {(center_y - MathUtils.boundsProtected(y, getImageHeight())) / scale}});
        if (Math.abs(params.getSkew()) > TOLERANCE) {
            //return MatrixOperations.add(MatrixOperations.multiply(Matrix.rotationMatrix2D(params.getSkew()),point), centre_offset);
            return MatrixOperations.add(MathUtils.doRotate(point, params.getSkew()), centre_offset);
        }
        return MatrixOperations.add(point, centre_offset);
    }
    public void resetCentre() {
        setCenter_x(plane.getWidth() / 2);
        setCenter_y(plane.getHeight() / 2);
        setCentre_offset(new Matrix(new double[][]{{0}, {0}}));
    }
    public Publisher getProgressPublisher() {
        return progressPublisher;
    }
    public void setProgressPublisher(Publisher progressPublisher) {
        this.progressPublisher = progressPublisher;
    }
    public IFSFractalParams getParams() {
        return params;
    }
    public void setParams(IFSFractalParams params) {
        this.params = new IFSFractalParams(params);
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = MathUtils.clamp(depth, 0, plane.getHeight() * plane.getWidth());
    }
    double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {
            this.base_precision = base_precision;
        }
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
    public int getCenter_x() {
        return center_x;
    }
    public void setCenter_x(int center_x) {
        this.center_x = center_x;
    }
    public int getCenter_y() {
        return center_y;
    }
    public void setCenter_y(int center_y) {
        this.center_y = center_y;
    }
    public double getScale() {
        return scale;
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    public Matrix getCentre_offset() {
        return centre_offset;
    }
    public void setCentre_offset(Matrix centre_offset) {
        this.centre_offset = new Matrix(centre_offset);
    }
    public PixelContainer getPlane() {
        return plane;
    }
    public void generate() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCoordinates(random.nextInt(plane.getWidth()), random.nextInt(plane.getHeight()));
        }
        Matrix point = new Matrix(initial);
        for (long i = 0; i <= depth; i++) {
            int index = MathUtils.weightedRandom(params.getWeights());
            int[] coord = toCoordinates(point);
            plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
            point = modifyPoint(point, index);
            if (point.equals(initial) || i == depth || isOutOfBounds(point)) {
                completion = true;
                break;
            }
            publishProgress(i);
        }
    }
    public boolean isOutOfBounds(Matrix point) {
        int x = (int) ((point.get(0, 0) * scale) + center_x), y = (int) (center_y - (point.get(1, 0) * scale));
        return x < 0 || y < 0 || x >= plane.getWidth() || y >= plane.getHeight();
    }
    public int[] toCoordinates(Matrix point) {
        point = MatrixOperations.subtract(point, centre_offset);
        if (Math.abs(params.getSkew()) > TOLERANCE) {
            //point = MatrixOperations.multiply(Matrix.rotationMatrix2D(params.getSkew()).inverse(),point);
            point = MathUtils.doRotate(point, -params.getSkew());
        }
        return new int[]{MathUtils.boundsProtected(Math.round((float) (point.get(0, 0) * scale) + center_x), getImageWidth()),
                MathUtils.boundsProtected(Math.round(center_y - (float) (point.get(1, 0) * scale)), getImageHeight())};
    }
    public synchronized void publishProgress(long val) {
        if (!silencer) {
            float completion = (((float) val) / depth);
            progressPublisher.publish("% completion= " + (completion * 100.0f) + "%", completion);
        }
    }
    private Matrix modifyPoint(Matrix point, int index) {
        if (params.isIfsMode()) {
            double x = point.get(0, 0), y = point.get(1, 0);
            FunctionEvaluator fe = FunctionEvaluator.prepareIFS(params.getX_code(), params.getR_code(), params.getT_code(), params.getP_code(), x, y);
            point.set(0, 0, fe.evaluateForIFS(params.getXfunctions()[index]));
            fe.setVariableCode(params.getY_code());
            fe.setZ_value(y + "");
            point.set(1, 0, fe.evaluateForIFS(params.getYfunctions()[index]));
        } else {
            point = MatrixOperations.add(MatrixOperations.multiply(params.getTransforms()[index], point), params.getTranslators()[index]);
        }
        return point;
    }
    public boolean isComplete() {
        return completion;
    }
    public Animation generateAnimation() {
        int frameskip = Math.abs(params.getFrameskip()) + 1;//to skip 1 frame, it must divide by 2, etc.
        Animation animation = new Animation(params.getFps());
        for (long i = 0; i <= depth && (!completion); i++) {
            generateStep();
            publishProgress(i);
            if (i % frameskip == 0) {
                animation.addFrame(plane);
            }
        }
        return animation;
    }
    public void generateStep() {
        if (initial == null) {
            Random random = new Random();
            initial = fromCoordinates(random.nextInt(plane.getWidth()), random.nextInt(plane.getHeight()));
        }
        if (point == null) {
            point = new Matrix(initial);
        }
        int index = MathUtils.weightedRandom(params.getWeights());
        int[] coord = toCoordinates(point);
        plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
        point = modifyPoint(point, index);
        if (point.equals(initial) || isOutOfBounds(point)) completion = true;
    }
    @Override
    public void pan(int distance, double angle) {
        pan(distance, angle, false);
    }
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(center_x + x_dist, center_y + y_dist, zoom_factor);
    }
}