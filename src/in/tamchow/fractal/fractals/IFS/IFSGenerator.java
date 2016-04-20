package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;

import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.math.matrix.MatrixOperations.*;
/**
 * Generates IFS fractals
 *
 * Hack to support multithreading:
 *
 * Actually makes 2 passes:
 * 1 in single threaded initialization phase without rendering to get starting points for threads (fast)
 * &amp; 1 in multithreaded mode for rendering/animation (slow)
 */
public class IFSGenerator extends PixelFractalGenerator {
    private static final double TOLERANCE = 1E-15;
    static Matrix[] points;
    static Matrix initial;
    PixelContainer plane;
    IFSFractalParams params;
    Matrix centre_offset, point;
    int center_x, center_y;
    double zoom, zoom_factor, base_precision, scale;
    int depth;
    boolean completion, silencer;
    Publisher progressPublisher;
    private Animation animation;
    private double[][] weightDistribution;
    public IFSGenerator(@NotNull IFSFractalParams params, Publisher progressPublisher) {
        setParams(params);
        initIFS(params);
        doZooms(params.zoomConfig);
        setProgressPublisher(progressPublisher);
    }
    private void populatePoints() {
        points = new Matrix[params.getThreads()/*Clamped between 1 and depth*/];
        //clamping guarantees that points.length >= 1, so this is okay.
        points[0] = initial;
        //indexes start from 1 so we can avoid doing a pass unnecessarily
        int gap = depth / params.getThreads(), pidx = 1;
        for (long i = 1; pidx < points.length && i <= depth; ++i) {
            generateStep(false);
            if (completion) {
                break;
            }
            if (i % gap == 0) {
                points[pidx++] = point;
            }
        }
        if (pidx < params.getThreads()) {
            params.setThreads(pidx);
        }
    }
    @Override
    public void doZooms(@NotNull ZoomConfig zoomConfig) {
        if (zoomConfig.zooms != null) {
            for (@NotNull ZoomParams zoom : zoomConfig.zooms) {
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
        return getPlane().getHeight();
    }
    @Override
    public int getHeight() {
        return getConfiguredHeight();
    }
    @Override
    public void setHeight(int height) {
        height = clamp(height, getPlane().getHeight());
        @NotNull IFSFractalParams modified = new IFSFractalParams(params);
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
        width = clamp(width, getPlane().getWidth());
        @NotNull IFSFractalParams modified = new IFSFractalParams(params);
        modified.setWidth(width);
        initIFS(modified);
    }
    private void initIFS(@NotNull IFSFractalParams params) {
        plane = new LinearizedPixelContainer(params.getWidth(), params.getHeight());
        resetCentre();
        setDepth(params.getDepth());
        setZoom(params.getZoom());
        setZoom_factor(params.getZoomlevel());
        setBase_precision(params.getBase_precision());
        if (initial == null) {
            initial = fromCoordinates(Math.round((float) Math.random() * getWidth()), Math.round((float) Math.random() * getHeight()));
        }
        completion = false;
        point = new Matrix(initial);
        if (points == null) {
            populatePoints();
        }
        animation = new Animation(params.getFps());
        if (params.zoomConfig.zooms != null) {
            for (@NotNull ZoomParams zoom : params.zoomConfig.zooms) {
                zoom(zoom);
            }
        }
        silencer = params.useThreadedGenerator();
    }
    public void zoom(@NotNull ZoomParams zoom) {
        if (zoom.centre == null) {
            zoom(zoom.centre_x, zoom.centre_y, zoom.level);
        } else {
            zoom(zoom.centre, zoom.level);
        }
    }
    public void zoom(@NotNull Matrix centre_offset, double level) {
        setCentre_offset(centre_offset);
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    public void zoom(int cx, int cy, double level) {
        cx = boundsProtected(cx, plane.getWidth());
        cy = boundsProtected(cy, plane.getHeight());
        setCentre_offset(fromCoordinates(cx, cy));
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
    }
    @NotNull
    public Matrix fromCoordinates(int x, int y) {
        @NotNull Matrix point = new Matrix(new double[][]{{(boundsProtected(x, getImageWidth()) - center_x) / scale},
                {(center_y - boundsProtected(y, getImageHeight())) / scale}});
        if (Math.abs(params.getSkew()) > TOLERANCE) {
            //return add(multiply(Matrix.rotationMatrix2D(params.getSkew()),point), centre_offset);
            return add(doRotate(point, params.getSkew()), centre_offset);
        }
        return add(point, centre_offset);
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
    public void setParams(@NotNull IFSFractalParams params) {
        this.params = new IFSFractalParams(params);
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = clamp(depth, 0, plane.getHeight() * plane.getWidth());
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
    public void setCentre_offset(@NotNull Matrix centre_offset) {
        this.centre_offset = new Matrix(centre_offset);
    }
    public PixelContainer getPlane() {
        return plane;
    }
    public void generate() {
        generate(0);
    }
    public void generate(int index) {
        /*if (initial == null) {
            Random random = new Random();
            initial = fromCoordinates(random.nextInt(plane.getWidth()), random.nextInt(plane.getHeight()));
        }
        Matrix point = new Matrix(initial);
        for (long i = 0; i <= depth; i++) {
            int index = weightedRandom(params.getWeights());
            int[] coord = toCoordinates(point);
            plane.setPixel(coord[1], coord[0], plane.getPixel(coord[1], coord[0]) + params.getColors()[index]);
            point = modifyPoint(point, index);
            if (point.equals(initial) || i == depth || isOutOfBounds(point)) {
                completion = true;
                break;
            }
            publishProgress(i);
        }*/
        initial = points[index];
        int frameskip = Math.abs(params.getFrameskip()) + 1;//to skip 1 frame, it must divide by 2, etc.
        for (long i = 0; i <= depth && (!completion); i++) {
            if (stop) {
                return;
            }
            checkAndDoPause();
            generateStep();
            publishProgress(i);
            if (params.isAnimated() && i % frameskip == 0) {
                animation.addFrame(plane);
            }
        }
    }
    public Animation getAnimation() {
        return animation;
    }
    private Matrix normalizePoint(Matrix point) {
        point = subtract(point, centre_offset);
        if (Math.abs(params.getSkew()) > TOLERANCE) {
            //point = multiply(Matrix.rotationMatrix2D(params.getSkew()).inverse(),point);
            point = doRotate(point, -params.getSkew());
        }
        return point;
    }
    public boolean isOutOfBounds(Matrix point) {
        point = normalizePoint(point);
        int x = Math.round((float) (point.get(0, 0) * scale) + center_x),
                y = Math.round(center_y - (float) (point.get(1, 0) * scale));
        return x < 0 || y < 0 || x >= plane.getWidth() || y >= plane.getHeight();
    }
    @NotNull
    public int[] toCoordinates(Matrix point) {
        point = normalizePoint(point);
        return new int[]{boundsProtected(Math.round((float) (point.get(0, 0) * scale) + center_x), getImageWidth()),
                boundsProtected(Math.round(center_y - (float) (point.get(1, 0) * scale)), getImageHeight())};
    }
    public synchronized void publishProgress(long val) {
        if (!silencer) {
            float completion = (((float) val) / depth);
            progressPublisher.publish("% completion= " + (completion * 100.0f) + "%", completion, (int) val);
        }
    }
    @NotNull
    private Matrix modifyPoint(@NotNull Matrix point, int index) {
        if (params.isIfsMode()) {
            double x = point.get(0, 0), y = point.get(1, 0);
            @NotNull FunctionEvaluator fe = FunctionEvaluator.prepareIFS(params.getX_code(), params.getR_code(), params.getT_code(), params.getP_code(), x, y);
            point.set(0, 0, fe.evaluateForIFS(params.getXfunctions()[index]));
            fe.setVariableCode(params.getY_code());
            fe.setZ_value(String.valueOf(y));
            point.set(1, 0, fe.evaluateForIFS(params.getYfunctions()[index]));
        } else {
            point = add(multiply(params.getTransforms()[index], point), params.getTranslators()[index]);
        }
        return point;
    }
    public boolean isComplete() {
        return completion;
    }
    public void generateStep() {
        generateStep(true);
    }
    public void generateStep(boolean render) {
        int index = weightedRandom(params.getWeights());
        @NotNull int[] coord = toCoordinates(point);
        if (render) {
            plane.setPixel(coord[1], coord[0],
                    Colorizer.linearInterpolated(
                            plane.getPixel(coord[1], coord[0]), params.getColors()[index],
                            /*Use default (proper) linear interpolation*/
                            params.getWeights()[index], 0));
            weightDistribution[coord[1]][coord[0]] = params.getWeights()[index];
        }
        @NotNull Matrix point = modifyPoint(this.point, index);
        if (point.equals(initial) || isOutOfBounds(point)) {
            completion = true;
        } else {
            this.point = point;
        }
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
    public double[][] getWeightDistribution() {
        return weightDistribution;
    }
}