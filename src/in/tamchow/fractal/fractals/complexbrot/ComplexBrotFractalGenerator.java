package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MersenneTwister;
import in.tamchow.fractal.helpers.stack.Stack;
import in.tamchow.fractal.helpers.stack.impls.FixedStack;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.symbolics.Function;
import in.tamchow.fractal.math.symbolics.Polynomial;

import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
import static in.tamchow.fractal.math.complex.ComplexOperations.*;
/**
 * ComplexBrot fractal generator
 * NOTE: Only supports the *BROT (excepting MANDELBROT, of course) fractal modes.
 * Other modes will throw an {@link UnsupportedOperationException}
 *
 * @see ComplexFractalGenerator
 * @see ComplexBrotFractalParams
 */
public class ComplexBrotFractalGenerator extends PixelFractalGenerator {
    static Complex[][] plane_map;
    private static Complex[] points;
    private static long sumIterations;
    ComplexBrotFractalParams params;
    ComplexFractalGenerator.Mode mode;
    Publisher progressPublisher;
    PixelContainer plane;
    int center_x, center_y, lastConstantIdx;
    double zoom;
    double zoom_factor;
    double base_precision;
    double scale;
    double tolerance;
    double escape_radius;
    int depth;
    int switch_rate;
    Complex centre_offset, lastConstant;
    String[][] constants;
    String function;
    int[][][] bases;
    int[] iterations;
    boolean silencer, anti, mandelbrotToJulia, juliaToMandelbrot;
    String variableCode, oldVariableCode;
    private long maxiter;
    public ComplexBrotFractalGenerator(@NotNull ComplexBrotFractalParams params, Publisher progressPublisher) {
        this.params = params;
        initFractal(params);
        doZooms(params.zoomConfig);
        setProgressPublisher(progressPublisher);
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
        @NotNull ComplexBrotFractalParams modified = new ComplexBrotFractalParams(params);
        modified.setHeight(height);
        initFractal(modified);
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
        @NotNull ComplexBrotFractalParams modified = new ComplexBrotFractalParams(params);
        modified.setWidth(width);
        initFractal(modified);
    }
    public void setIterations(@NotNull int[] iterations) {
        this.iterations = new int[iterations.length];
        System.arraycopy(iterations, 0, this.iterations, 0, iterations.length);
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    public void setOldVariableCode(String oldVariableCode) {
        this.oldVariableCode = oldVariableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    private void initFractal(@NotNull ComplexBrotFractalParams params) {
        plane = new LinearizedPixelContainer(params.getWidth(), params.getHeight());
        setFunction(params.getFunction());
        setConstants(params.getConstants());
        setZoom(params.getZoom());
        setZoom_factor(params.getZoom_level());
        setBase_precision(params.getBase_precision());
        setMode(params.getMode());
        setDepth(params.getNum_points());
        setIterations(params.getIterations());
        setEscape_radius(params.getEscape_radius());
        silencer = params.useThreadedGenerator();
        anti = params.isAnti();
        resetCentre();
        setScale(this.base_precision * Math.pow(zoom, zoom_factor));
        setVariableCode(params.getVariableCode());
        setOldVariableCode(params.getOldVariableCode());
        setTolerance(params.getTolerance());
        sumIterations = sumIterations();
        lastConstant = new Complex(-1, 0);
        mandelbrotToJulia = false;
        juliaToMandelbrot = false;
        switch_rate = params.getSwitch_rate();
        if (!(switch_rate == 0 || switch_rate == -1 || switch_rate == 1)) {
            if (switch_rate < 0) {
                juliaToMandelbrot = true;
                this.switch_rate = -switch_rate;
            } else {
                mandelbrotToJulia = true;
            }
        }
        if (plane_map == null) {
            plane_map = new Complex[plane.getHeight()][plane.getWidth()];
            populateMap();
        }
        if (points == null) {
            createPoints();
        }
        bases = new int[iterations.length][getImageHeight()][getImageWidth()];
    }
    public int[][][] getBases() {
        return bases;
    }
    private Complex[] createPoints() {
        points = new Complex[depth];
        for (int i = 0; i < points.length; ++i) {
            Complex random_point;
            do {
                random_point = getRandomPoint();
            } while (containsPoint(random_point));
            points[i] = random_point;
        }
        return points;
    }
    @NotNull
    protected int[] start_end_coordinates(int idx, int maxIdx) {
        int distance = depth / maxIdx;
        //{startIdx,endIdx}
        if (idx == maxIdx - 1) {
            return new int[]{(idx * distance), depth};
        } else {
            return new int[]{(idx * distance), ((idx + 1) * distance)};
        }
    }
    private Complex getRandomPoint() {
        int random_x = boundsProtected(Math.round((float) new MersenneTwister().nextDouble() * plane.getWidth()), plane.getWidth()),
                random_y = boundsProtected(Math.round((float) new MersenneTwister().nextDouble() * plane.getHeight()), plane.getHeight());
        return plane_map[random_y][random_x];
    }
    private boolean containsPoint(@Nullable Complex point) {
        for (@Nullable Complex aPoint : points) {
            if (point != null && aPoint != null && point.equals(aPoint)) {
                return true;
            }
        }
        return false;
    }
    public void createImage() {
        @NotNull PixelContainer[] levels = new PixelContainer[bases.length];
        int[] maxima = new int[levels.length];
        for (int i = 0; i < maxima.length; ++i) {
            maxima[i] = getMaximum(bases[i]);
        }
        for (int i = 0; i < bases.length; ++i) {
            levels[i] = new PixelContainer(getImageWidth(), getImageHeight());
            for (int j = 0; j < bases[i].length; ++j) {
                for (int k = 0; k < bases[i][j].length; ++k) {
                    levels[i].setPixel(j, k, getColor(j, k, i, maxima[i]));
                }
            }
        }
        plane = plane.falseColor(levels);
    }
    private int getColor(int i, int j, int level, int maximum) {
        //int channel = boundsProtected(Math.round((float) bases[level][i][j] / maximum) * 255, 256);
        int channel = boundsProtected(Math.round((float) (Math.sqrt(bases[level][i][j]) / Math.sqrt(maximum))) * 255, 256);
        return Colorizer.toGray(channel);
    }
    private int getMaximum(@NotNull int[][] base) {
        int max = 0;
        for (@NotNull int[] row : base) {
            for (int val : row) {
                if (val >= max) max = val;
            }
        }
        return max;
    }
    public Publisher getProgressPublisher() {
        return progressPublisher;
    }
    public void setProgressPublisher(Publisher progressPublisher) {
        this.progressPublisher = progressPublisher;
    }
    public ComplexBrotFractalParams getParams() {
        return params;
    }
    public void setParams(ComplexBrotFractalParams params) {
        this.params = params;
    }
    public PixelContainer getPlane() {
        return plane;
    }
    @NotNull
    public int[] toCoordinates(Complex point) {
        point = subtract(point, centre_offset);
        if (Math.abs(params.skew) >= params.tolerance) {
            /*Matrix rotor = Matrix.rotationMatrix2D(params.skew).inverse();
            point = matrixToComplex(MatrixOperations.multiply(rotor, complexToMatrix(point)));*/
            point = matrixToComplex(doRotate(complexToMatrix(point), -params.skew));
        }
        return new int[]{boundsProtected(Math.round((float) (point.real() * scale) + center_x), getImageWidth()),
                boundsProtected(Math.round(center_y - (float) (point.imaginary() * scale)), getImageHeight())};
    }
    public void zoom(@NotNull ZoomParams zoom) {
        if (zoom.centre == null) {
            zoom(zoom.centre_x, zoom.centre_y, zoom.level);
        } else {
            zoom(zoom.centre, zoom.level);
        }
    }
    public void mandelbrotToJulia(@NotNull Matrix constant, double level) {
        zoom(constant, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void resetCentre() {
        setCenter_x(plane.getWidth() / 2);
        setCenter_y(plane.getHeight() / 2);
        resetCentre_Offset();
    }
    public void resetCentre_Offset() {
        centre_offset = Complex.ZERO;
    }
    private void changeMode(@NotNull Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == ComplexFractalGenerator.Mode.BUDDHABROT) ? ComplexFractalGenerator.Mode.JULIABROT : ComplexFractalGenerator.Mode.BUDDHABROT);
    }
    private void setMode(ComplexFractalGenerator.Mode mode) {
        this.mode = mode;
    }
    public void zoom(@NotNull Matrix centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(centre_offset, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(centre_offset, level));
        }
        zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level);
    }
    public void zoom(@NotNull Complex centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(complexToMatrix(centre_offset), level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(complexToMatrix(centre_offset), level));
        }
        setCentre_offset(centre_offset);
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
        //setCenter_x(toCoordinates(centre_offset)[0]);setCenter_y(toCoordinates(centre_offset)[1]);
        populateMap();
    }
    public void populateMap() {
        for (int i = 0; i < plane.getHeight(); i++) {
            for (int j = 0; j < plane.getWidth(); j++) {
                plane_map[i][j] = fromCoordinates(j, i);
            }
        }
    }
    @NotNull
    public Complex fromCoordinates(int x, int y) {
        @NotNull Complex point = new Complex(((boundsProtected(x, getImageWidth()) - center_x) / scale),
                ((center_y - boundsProtected(y, getImageWidth())) / scale));
        if (Math.abs(params.skew) > params.tolerance) {
            /*Matrix rotor = Matrix.rotationMatrix2D(params.skew);
            point = matrixToComplex(MatrixOperations.multiply(rotor, complexToMatrix(point)));*/
            point = matrixToComplex(doRotate(complexToMatrix(point), params.skew));
        }
        return add(centre_offset, point);
    }
    public void mandelbrotToJulia(int cx, int cy, double level) {
        zoom(cx, cy, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void zoom(int cx, int cy, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(cx, cy, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(cx, cy, level));
        }
        cx = boundsProtected(cx, plane.getWidth());
        cy = boundsProtected(cy, plane.getHeight());
        //setCenter_x(cx);setCenter_y(cy);
        setCentre_offset(fromCoordinates(cx, cy));
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
        populateMap();
    }
    public void setConstants(@NotNull String[][] constants) {
        this.constants = new String[constants.length][constants[0].length];
        for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, constants[i].length);
        }
    }
    public void mandelbrotToJulia(@NotNull Complex constant, double level) {
        zoom(constant, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void mandelbrotToJulia(@NotNull ZoomParams zoom) {
        zoom(zoom);
        changeMode(centre_offset);
        resetCentre();
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
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getZoom_factor() {
        return zoom_factor;
    }
    public void setZoom_factor(double zoom_factor) {
        this.zoom_factor = zoom_factor;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {
            this.base_precision = base_precision;
        }
    }
    private void publishProgress(long ctr, int start, int end, int current) {
        if (!silencer) {
            float completion = Math.round(((float) current) / (end - start));
            progressPublisher.publish(ctr + " iterations of " + maxiter + ",completion = " + (completion * 100.0f) + "%", completion, current);
        }
    }
    public double calculateBasePrecision() {
        return ((plane.getHeight() >= plane.getWidth()) ? plane.getWidth() / 2 : plane.getHeight() / 2);
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = clamp(depth, 0, plane.getHeight() * plane.getWidth());
    }
    public Complex getCentre_offset() {
        return centre_offset;
    }
    public void setCentre_offset(Complex centre_offset) {
        this.centre_offset = centre_offset;
    }
    public Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(constants[0][1]);
            } else {
                lastConstant = new Complex(constants[getLastConstantIndex()][1]);
            }
        }
        return lastConstant;
    }
    public void setLastConstant(@NotNull Complex value) {
        constants[getLastConstantIndex()][1] = value.toString();
        lastConstant = new Complex(value);
    }
    public int getLastConstantIndex() {
        @NotNull String[] parts = split(function, " ");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIdx(getConstantIndex(parts[i]));
                return lastConstantIdx;
            }
        }
        return -1;
    }
    public int getConstantIndex(String constant) {
        for (int i = 0; i < constants.length; i++) {
            if (constants[i][0].equals(constant)) {
                return i;
            }
        }
        return -1;
    }
    public void setLastConstantIdx(int lastConstantIdx) {
        this.lastConstantIdx = lastConstantIdx;
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
    public void generate() {
        generate(0, depth);
    }
    private long sumIterations() {
        long sum = 0;
        for (long iteration : iterations) {
            sum += iteration;
        }
        return sum;
    }
    public void generate(int start, int end) {
        maxiter = (end - start) * sumIterations;
        switch (mode) {
            case RUDYBROT:
            case BUDDHABROT:
                mandelbrotGenerate(start, end);
                break;
            case JULIABROT:
                juliaGenerate(start, end);
                break;
            case NEWTONBROT:
            case JULIA_NOVABROT:
            case MANDELBROT_NOVABROT:
                newtonGenerate(start, end);
                break;
            case SECANTBROT:
                secantGenerate(start, end);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported fractal render mode");
        }
        if (!silencer) {
            //Don't create the image if in multithreaded mode, the threaded generator handles it
            createImage();
        }
    }
    private void secantGenerate(int start, int end) {
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        for (int level = 0; level < iterations.length; ++level) {
            int iteration = iterations[level];
            @NotNull Stack<Complex> last = new FixedStack<>(iteration + 2);
            @NotNull int[][] tmp = new int[getImageHeight()][getImageWidth()];
            int c = 0;
            outer:
            for (int j = start; j < end; ++j) {
                Complex z = points[j], ztmp2 = Complex.ZERO, zold = Complex.ZERO;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                while (c <= iteration) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    Complex ztmp;
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    Complex a = fe.evaluate(function, false);
                    Complex b = fe.evaluate(function, zold);
                    ztmp = subtract(z,
                            divide(
                                    multiply(a,
                                            subtract(z, zold)),
                                    subtract(a, b)));
                    fe.setZ_value(ztmp.toString());
                    if (fe.evaluate(function, ztmp).cabs() <= tolerance) {
                        c = iteration;
                        break;
                    }
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iteration;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    fe.setOldvalue(ztmp2.toString());
                    publishProgress(ctr, start, end, j);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
            }
            last.clear();
            updateBases(c, iteration, level, tmp);
        }
    }
    private void newtonGenerate(int start, int end) {
        @Nullable Complex constant = params.getNewton_constant();
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex degree;
        @NotNull String functionderiv = "";
        if (Function.isSpecialFunction(function)) {
            @NotNull Function func = Function.fromString(function, variableCode, oldVariableCode);
            func.setConsts(constants);
            function = func.toString();
            degree = func.getDegree();
            functionderiv = func.derivative(1);
        } else {
            @NotNull Polynomial polynomial = Polynomial.fromString(function);
            polynomial.setConstdec(constants);
            polynomial.setVariableCode(variableCode);
            polynomial.setOldvariablecode(oldVariableCode);
            function = polynomial.toString();
            degree = polynomial.getDegree();
            functionderiv = polynomial.derivative().toString();
        }
        if (constant != null && constant.equals(Complex.ZERO)) {
            constant = divide(Complex.ONE, degree);
        }
        Complex toadd = Complex.ZERO;
        @NotNull Complex lastConstantBackup = new Complex(getLastConstant());
        if (mode == ComplexFractalGenerator.Mode.JULIA_NOVABROT) {
            toadd = new Complex(getLastConstant());
        }
        for (int level = 0; level < iterations.length; ++level) {
            int iteration = iterations[level];
            @NotNull Stack<Complex> last = new FixedStack<>(iteration + 2);
            @NotNull int[][] tmp = new int[getImageHeight()][getImageWidth()];
            int c = 0;
            outer:
            for (int j = start; j < end; ++j) {
                boolean useJulia = false, useMandelbrot = false;
                Complex z = points[j], ztmp2 = Complex.ZERO;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (mode == ComplexFractalGenerator.Mode.MANDELBROT_NOVABROT) {
                    toadd = points[j];
                    z = Complex.ZERO;
                }
                last.push(z);
                while (c <= iteration) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    if (mode == ComplexFractalGenerator.Mode.MANDELBROT_NOVABROT) {
                        if (mandelbrotToJulia) {
                            if (c % switch_rate == 0) {
                                useJulia = (!useJulia);
                            }
                            if (useJulia) {
                                toadd = lastConstantBackup;
                            } else {
                                toadd = points[j];
                            }
                        }
                    }
                    if (mode == ComplexFractalGenerator.Mode.JULIA_NOVABROT) {
                        if (juliaToMandelbrot) {
                            if (c % switch_rate == 0) {
                                useMandelbrot = (!useMandelbrot);
                            }
                            if (useMandelbrot) {
                                toadd = points[j];
                            } else {
                                toadd = lastConstantBackup;
                            }
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    last.pop();
                    Complex ztmp;
                    if (constant != null) {
                        ztmp = add(subtract(z, multiply(constant, divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false)))), toadd);
                    } else {
                        ztmp = add(subtract(z, divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false))), toadd);
                    }
                    fe.setZ_value(ztmp.toString());
                    if (fe.evaluate(function, ztmp).cabs() <= tolerance) {
                        c = iteration;
                        break;
                    }
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iteration;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    publishProgress(ctr, start, end, j);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
            }
            last.clear();
            updateBases(c, iteration, level, tmp);
        }
    }
    private void mandelbrotGenerate(int start, int end) {
        double bailout = escape_radius * escape_radius + tolerance;
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        for (int level = 0; level < iterations.length; ++level) {
            int iteration = iterations[level];
            @NotNull Stack<Complex> last = new FixedStack<>(iteration + 2);
            @NotNull int[][] tmp = new int[getImageHeight()][getImageWidth()];
            outer:
            for (int j = start; j < end; ++j) {
                @NotNull Complex z = (mode == ComplexFractalGenerator.Mode.RUDYBROT) ? new Complex(points[j]) : Complex.ZERO, ztmp2 = Complex.ZERO;
                setLastConstant(points[j]);
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                fe.setConstdec(this.constants);
                fe.setZ_value(z.toString());
                last.push(z);
                int c = 0;
                boolean useJulia = false;
                while (c <= iteration && z.cabs() <= bailout) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    if (mandelbrotToJulia) {
                        if (c % switch_rate == 0) {
                            useJulia = (!useJulia);
                        }
                        if (useJulia) {
                            setLastConstant(lastConstantBackup);
                            fe.setConstdec(constants);
                        } else {
                            setLastConstant(points[j]);
                            fe.setConstdec(constants);
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    last.pop();
                    Complex ztmp = fe.evaluate(function, false);
                    last.push(ztmp);
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iteration;
                        break;
                    }
                    z = new Complex(ztmp);
                    @NotNull int[] coords = toCoordinates(z);
                    ++tmp[coords[1]][coords[0]];
                    fe.setZ_value(z.toString());
                    publishProgress(ctr, start, end, j);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                last.clear();
                updateBases(c, iteration, level, tmp);
            }
        }
    }
    private void updateBases(int c, int iteration, int level, int[][] tmp) {
        if (anti) {
            if (c == iteration) {
                bases[level] = intDDAAdd(bases[level], tmp);
            }
        } else {
            if (c < iteration) {
                bases[level] = intDDAAdd(bases[level], tmp);
            }
        }
    }
    private void juliaGenerate(int start, int end) {
        double bailout = escape_radius * escape_radius + tolerance;
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        for (int level = 0; level < iterations.length; ++level) {
            int iteration = iterations[level];
            @NotNull Stack<Complex> last = new FixedStack<>(iteration + 2);
            @NotNull int[][] tmp = new int[getImageHeight()][getImageWidth()];
            int c = 0;
            outer:
            for (int j = start; j < end; ++j) {
                Complex z = points[j], ztmp2 = Complex.ZERO;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                last.push(z);
                boolean useMandelBrot = false;
                while (c <= iteration && z.cabs() <= bailout) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    if (juliaToMandelbrot) {
                        if (c % switch_rate == 0) {
                            useMandelBrot = (!useMandelBrot);
                        }
                        if (useMandelBrot) {
                            setLastConstant(points[j]);
                            fe.setConstdec(constants);
                        } else {
                            setLastConstant(lastConstantBackup);
                            fe.setConstdec(constants);
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    last.pop();
                    Complex ztmp = fe.evaluate(function, false);
                    last.push(ztmp);
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iteration;
                        break;
                    }
                    z = new Complex(ztmp);
                    @NotNull int[] coords = toCoordinates(z);
                    ++tmp[coords[1]][coords[0]];
                    fe.setZ_value(z.toString());
                    publishProgress(ctr, start, end, j);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
            }
            last.clear();
            updateBases(c, iteration, level, tmp);
        }
    }
    public void setEscape_radius(double escape_radius) {
        this.escape_radius = escape_radius;
    }
}