package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.stack.Stack;
import in.tamchow.fractal.helpers.stack.impls.FixedStack;
import in.tamchow.fractal.helpers.strings.CharBuffer;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.symbolics.Function;

import java.util.ArrayList;

import static in.tamchow.fractal.color.Colors.MODE.*;
import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
import static in.tamchow.fractal.math.complex.ComplexOperations.*;
import static java.lang.Double.*;
import static java.lang.Math.*;
import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.sin;
/**
 * The actual fractal plotter for Julia, Newton, Nova (both Mandelbrot and Julia variants),Secant and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes.
 * Various (21) Coloring modes
 */
public final class ComplexFractalGenerator extends PixelFractalGenerator {
    private static final String asciiArtBase =
            "~`+-*/#@!%^&(){}[];'|:?><.,_=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static Complex[][] argand_map;
    private static ArrayList<Complex> roots;
    private static int valCount = 0;
    private static double vals = 0;
    private static Complex[] boundary_elements;
    protected Colorizer color;
    protected int[] histogram;
    protected Publisher progressPublisher;
    protected ComplexFractalParams params;
    protected double zoom, zoom_factor, base_precision, scale;
    int[][] escapedata;
    double[][] normalized_escapes;
    private PixelContainer argand;
    private int center_x, center_y, lastConstantIdx, stripe_density, switch_rate;
    private Mode mode;
    private double tolerance;
    private long maxiter;
    private String function;
    private String[][] consts;
    private Complex distance_estimate_multiplier;
    private Complex centre_offset, lastConstant, trap_point;
    private boolean mandelbrotToJulia, juliaToMandelbrot, useLineTrap, silencer, simpleSmoothing;
    private double a, b, c;
    private String variableCode, oldvariablecode;
    private int colorIfMore = Colors.BASE_COLORS.WHITE, colorIfLess = Colors.BASE_COLORS.BLACK;
    public ComplexFractalGenerator(@NotNull ComplexFractalParams params, Publisher progressPublisher) {
        this.params = params;
        initFractal(params);
        doZooms(params.zoomConfig);
        setProgressPublisher(progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, String oldvariablecode, double tolerance, @NotNull Colorizer color, Publisher progressPublisher) {
        //initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, oldvariablecode, tolerance, new Complex(-1, 0), color, 0, Complex.ZERO, null);
        //this.progressPublisher = progressPublisher;
        //ComplexFractalParams params=new ComplexFractalParams();
        //params.initParams=new ComplexFractalInitParams(width,height,zoom,zoom_factor,base_precision,mode,function,consts,variableCode,oldvariablecode,tolerance,color,0,Complex.ZERO,null,0);
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, oldvariablecode, tolerance, color, 0, Complex.ZERO, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, double tolerance, @NotNull Colorizer color, Publisher progressPublisher, int switch_rate, @NotNull Complex trap_point) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, trap_point, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, double tolerance, @NotNull Colorizer color, Publisher progressPublisher, int switch_rate, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, Complex.ZERO, linetrap, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, double tolerance, @NotNull Colorizer color, Publisher progressPublisher, int switch_rate, @NotNull Complex trap_point, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, trap_point, linetrap, 0), null), progressPublisher);
    }
    public static String getAsciiArtBase() {
        return asciiArtBase;
    }
    @NotNull
    public static int[] start_end_coordinates(int startx, int endx, int starty, int endy, int nx, int ix, int ny, int iy) {
        //for multithreading purposes
        int start_x = startx, end_x, start_y = starty, end_y;
        int x_dist = round((float) (endx - startx) / nx), y_dist = round((float) (endy - starty) / ny);
        if (ix == (nx - 1)) {
            start_x += (nx - 1) * x_dist;
            end_x = endx;
        } else {
            start_x += ix * x_dist;
            end_x = (ix + 1) * x_dist;
        }
        if (iy == (ny - 1)) {
            start_y += (ny - 1) * y_dist;
            end_y = endy;
        } else {
            start_y += iy * y_dist;
            end_y = (iy + 1) * y_dist;
        }
        return new int[]{start_x, end_x, start_y, end_y};
    }
    public ArrayList<Complex> getRoots() {
        return roots;
    }
    public PixelContainer getPlane() {
        return getArgand();
    }
    private void initFractal(@NotNull ComplexFractalParams params) {
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.zoom_factor, params.initParams.base_precision, params.initParams.fractal_mode, params.initParams.function, params.initParams.consts, params.initParams.variableCode, params.initParams.oldvariablecode, params.initParams.tolerance, params.initParams.color, params.initParams.switch_rate, params.initParams.trap_point, params.initParams.linetrap);
    }
    private void initFractal(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, String oldvariablecode, double tolerance, @NotNull Colorizer color, int switch_rate, @NotNull Complex trap_point, @Nullable String linetrap) {
        silencer = params.useThreadedGenerator();
        argand = new LinearizedPixelContainer(width, height);
        setMode(mode);
        setMaxiter(getImageHeight() * argand.getWidth());
        escapedata = new int[getImageHeight()][getImageWidth()];
        normalized_escapes = new double[getImageHeight()][getImageWidth()];
        setVariableCode(variableCode);
        setZoom(zoom);
        setZoom_factor(zoom_factor);
        setFunction(function);
        setBase_precision(base_precision);
        setConsts(consts);
        setScale(this.base_precision * pow(zoom, zoom_factor));
        resetCentre();
        setOldvariablecode(oldvariablecode);
        setTolerance(tolerance);
        if (roots == null) {
            roots = new ArrayList<>();
        }
        setColor(color);
        lastConstant = new Complex(-1, 0);
        if ((this.color.getMode() == STRIPE_AVERAGE_SPLINE || this.color.getMode() == STRIPE_AVERAGE_LINEAR)
                || ((!this.color.isExponentialSmoothing()) &&
                (this.color.isLogIndex() &&
                        (!(mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT))))) {
            setStripe_density(this.color.getColor_density());
            this.color.setColor_density(-1);
        } else {
            setStripe_density(-1);
        }
        mandelbrotToJulia = false;
        juliaToMandelbrot = false;
        if (!(switch_rate == 0 || switch_rate == -1 || switch_rate == 1)) {
            if (switch_rate < 0) {
                juliaToMandelbrot = true;
                this.switch_rate = -switch_rate;
            } else {
                mandelbrotToJulia = true;
                this.switch_rate = switch_rate;
            }
        }
        setTrap_point(trap_point);
        useLineTrap = false;
        if (linetrap != null) {
            @NotNull String[] parts = split(linetrap, ",");
            a = Double.valueOf(parts[0]);
            b = Double.valueOf(parts[1]);
            c = Double.valueOf(parts[2]);
            useLineTrap = true;
        }
        if (color.getSmoothing_base().equals(Complex.E)) {
            simpleSmoothing = true;
        }
        if (color.getPalette().length == 2) {
            colorIfMore = color.getPalette()[0];
            colorIfLess = color.getPalette()[1];
        }
        if (argand_map == null) {
            argand_map = new Complex[getImageHeight()][getImageWidth()];
            populateMap();
        }
    }
    public Colorizer getColor() {
        return color;
    }
    public void setColor(@NotNull Colorizer color) {
        this.color = new Colorizer(color);
    }
    private synchronized double modulusForPhase(double phase) {
        for (@NotNull Complex num : boundary_elements) {
            if (abs(phase - num.arg()) <= tolerance) {
                return num.modulus();
            }
        }
        return NaN;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
    }
    public void setTrap_point(@NotNull Complex trap_point) {
        this.trap_point = new Complex(trap_point);
    }
    public Publisher getProgressPublisher() {
        return progressPublisher;
    }
    public void setProgressPublisher(Publisher progressPublisher) {
        this.progressPublisher = progressPublisher;
    }
    public ComplexFractalParams getParams() {
        return params;
    }
    public void setParams(ComplexFractalParams params) {
        this.params = params;
    }
    private void setStripe_density(int stripe_density) {
        this.stripe_density = stripe_density;
    }
    private void setMaxiter(long maxiter) {
        this.maxiter = maxiter;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public int[][] getEscapedata() {
        return escapedata;
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
    public Mode getMode() {
        return mode;
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    /**
     * @param nx:No. of threads horizontally
     * @param ix:Index of thread horizontally
     * @param ny:No. of threads vertically
     * @param iy:Index of thread vertically
     * @return the start and end coordinates for a particular thread's rendering region
     */
    @NotNull
    protected int[] start_end_coordinates(int nx, int ix, int ny, int iy) {
        return start_end_coordinates(0, getImageWidth(), 0, getImageHeight(), nx, ix, ny, iy);
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {
            this.base_precision = base_precision;
        }
    }
    @Override
    public double calculateBasePrecision() {
        return ((getImageHeight() >= argand.getWidth()) ? getImageWidth() / 2 : getImageHeight() / 2);
    }
    public PixelContainer getArgand() {
        return argand;
    }
    public void setConsts(@NotNull String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }
    public void generate() {
        if (params.runParams.fully_configured) {
            generate(params.runParams.start_x, params.runParams.end_x, params.runParams.start_y, params.runParams.end_y, params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        } else {
            generate(params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        }
    }
    public void generate(int iterations, double escape_radius, Complex constant) {
        generate(0, getImageWidth(), 0, getImageHeight(), iterations, escape_radius, constant);
    }
    public void generate(int iterations, double escape_radius) {
        generate(0, getImageWidth(), 0, getImageHeight(), iterations, escape_radius, null);
    }
    public void generate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, Complex constant) {
        setMaxiter((end_x - start_x) * (end_y - start_y) * iterations);
        if (this.color.getMode() == SIMPLE_SMOOTH_LINEAR || this.color.getMode() == SIMPLE_SMOOTH_SPLINE) {
            this.color.setColor_density(this.color.getColor_density() * iterations);
        }
        if (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR || color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
            histogram = new int[iterations + 1];
        }
        switch (mode) {
            case MANDELBROT:
            case RUDY:
            case RUDYBROT:
            case BUDDHABROT:
                mandelbrotGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius);
                break;
            case JULIA:
            case JULIABROT:
                juliaGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius);
                break;
            case NEWTON:
            case NEWTONBROT:
            case JULIA_NOVA:
            case JULIA_NOVABROT:
            case MANDELBROT_NOVA:
            case MANDELBROT_NOVABROT:
                newtonGenerate(start_x, end_x, start_y, end_y, iterations, constant);
                break;
            case SECANT:
            case SECANTBROT:
                secantGenerate(start_x, end_x, start_y, end_y, iterations);
                break;
            default:
                throw new IllegalArgumentException("Unknown fractal render mode");
        }
        if (!params.useThreadedGenerator() && (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR || color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE)) {
            double scaling = base_precision * pow(zoom, zoom_factor);
            int total = 0;
            for (int i = 0; i < iterations; i += 1) {
                total += histogram[i];
            }
            if (color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
                System.arraycopy(rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
            }
            for (int i = start_y; i < end_y; i++) {
                for (int j = start_x; j < end_x; j++) {
                    int colortmp, pi = i, pj = j - 1, ni = i, nj = j + 1;
                    double normalized_count = normalized_escapes[i][j];
                    if (pj < 0) {
                        pi = (i == 0) ? i : i - 1;
                        pj = escapedata[pi].length - 1;
                    }
                    if (nj >= escapedata[i].length) {
                        ni = (i == escapedata.length - 1) ? i : i + 1;
                        nj = 0;
                    }
                    int ep = escapedata[pi][pj], en = escapedata[ni][nj], e = escapedata[i][j];
                    if (color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
                        if (color.getMode() == RANK_ORDER_LINEAR) {
                            int color1 = color.getColor(color.createIndex(((double) indexOf(histogram, ep)) / iterations, 0, 1)), color2 = color.getColor(color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1)), color3 = color.getColor(color.createIndex(((double) indexOf(histogram, en)) / iterations, 0, 1));
                            int colortmp1 = Colorizer.linearInterpolated(color1, color2, normalized_count - (long) normalized_count, color.getByParts());
                            int colortmp2 = Colorizer.linearInterpolated(color2, color3, normalized_count - (long) normalized_count, color.getByParts());
                            if (color.isLogIndex()) {
                                colortmp = Colorizer.linearInterpolated(colortmp1, colortmp2, normalized_count - (long) normalized_count, color.getByParts());
                            } else {
                                colortmp = color2;
                            }
                        } else {
                            int idxp = color.createIndex(((double) indexOf(histogram, ep)) / iterations, 0, 1),
                                    idxn = color.createIndex(((double) indexOf(histogram, en)) / iterations, 0, 1), idxMin = (idxp < idxn) ? idxp : idxn, idxMax = (idxp > idxn) ? idxp : idxn;
                            if (color.isModifierEnabled()) {
                                colortmp = color.splineInterpolated(color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1), normalized_count - (long) normalized_count);
                            } else {
                                if (color.isLogIndex()) {
                                    colortmp = color.splineInterpolated(idxMin, color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1), normalized_count - (long) normalized_count);
                                } else {
                                    colortmp = color.splineInterpolated(color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1), idxMax, normalized_count - (long) normalized_count);
                                }
                            }
                        }
                    } else {
                        double hue = 0.0, hue2 = 0.0, hue3 = 0.0;
                        for (int k = 0; k < e; k += 1) {
                            hue += ((double) histogram[k]) / total;
                        }
                        for (int k = 0; k < en; k += 1) {
                            hue2 += ((double) histogram[k]) / total;
                        }
                        for (int k = 0; k < ep; k += 1) {
                            hue3 += ((double) histogram[k]) / total;
                        }
                        if (color.getMode() == HISTOGRAM_LINEAR) {
                            int colortmp1 = Colorizer.linearInterpolated(color.getColor(color.createIndex(hue, 0, 1)), color.getColor(color.createIndex(hue2, 0, 1)), normalized_count - (long) normalized_count, color.getByParts());
                            int colortmp2 = Colorizer.linearInterpolated(color.getColor(color.createIndex(hue3, 0, 1)), color.getColor(color.createIndex(hue, 0, 1)), normalized_count - (long) normalized_count, color.getByParts());
                            colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, normalized_count - (long) normalized_count, color.getByParts());
                        } else {
                            int idxp = color.createIndex(hue3, 0, 1),
                                    idxn = color.createIndex(hue2, 0, 1), idxt = Math.min(idxp, idxn);
                            colortmp = color.splineInterpolated(color.createIndex(hue, 0, 1), idxt, normalized_count - (long) normalized_count);
                        }
                    }
                    argand.setPixel(i, j, colortmp);
                }
            }
        }
    }
    private void secantGenerate(int start_x, int end_x, int start_y, int end_y, int iterations) {
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        @NotNull String functionderiv = "";
        @Nullable Complex degree = null;
        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
            @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
            function = func.toString();
            functionderiv = func.firstDerivative();
            degree = func.getDegree();
            /*if (Function.isSpecialFunction(function)) {
                @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
                function = func.toString();
                functionderiv = func.derivative(1);
                degree = func.getDegree();
            } else {
                @NotNull Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(consts);
                poly.setVariableCode(variableCode);
                poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
                degree = poly.getDegree();
            }*/
        }
        degree = (degree == null) ? fe.getDegree(function) : degree;
        distance_estimate_multiplier = degree;
        roots.ensureCapacity(round((float) degree.modulus()));
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                @NotNull Complex z = argand_map[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO, zold = Complex.ZERO;
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd, ubnd;
                if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.getMode() == STRIPE_AVERAGE_LINEAR ||
                        color.getMode() == STRIPE_AVERAGE_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE ||
                        color.getMode() == DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                while (c < iterations) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    @NotNull Complex ztmp, ztmpd = new Complex(zd);
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    last.pop();
                    if (last.size() > 1) {
                        last.pop();
                        z2 = last.peek();
                        last.push(ztmp2);
                    }
                    last.push(z);
                    Complex a = fe.evaluate(function, false);
                    Complex b = fe.evaluate(function, zold);
                    ztmp = subtract(z,
                            divide(
                                    multiply(a,
                                            subtract(z, zold)),
                                    subtract(a, b)));
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        Complex e = fed.evaluate(functionderiv, false);
                        fed.setZ_value(ztmpd2.toString());
                        Complex d = fed.evaluate(functionderiv, false);
                        ztmpd = subtract(ztmpd, divide(multiply(e, subtract(ztmpd, ztmpd2)), subtract(e, d)));
                    }
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothing_base(),
                                add(ztmp,
                                        divide(
                                                new Complex(0.5),
                                                (subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == EPSILON_CROSS_LINEAR || color.getMode() == EPSILON_CROSS_SPLINE) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE_LINEAR || color.getMode() == GAUSSIAN_INT_DISTANCE_SPLINE) {
                        /*long gx = round(ztmp.real() * trap_point.modulus());
                        long gy = round(ztmp.imaginary() * trap_point.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trap_point.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE_LINEAR || color.getMode() == STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR || color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        /*//Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argand_map[i][j];
                        }else{
                            adjust=getLastConstant();
                        }*/
                        /*double znAbs = subtract(ztmp, adjust).modulus();
                        znAbs = (znAbs <= tolerance) ? ztmp.modulus() : znAbs;
                        lbnd = abs(znAbs - adjust.modulus());
                        ubnd = znAbs + adjust.modulus();
                        double value = (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                        mindist += (isNaN(value) || isInfinite(value)) ? 0 : value;*/
                        double znAbs = subtract(ztmp, adjust).modulus(),
                                zMod = ztmp.modulus(), aMod = adjust.modulus();
                        znAbs = (znAbs <= tolerance) ? zMod : znAbs;
                        lbnd = abs(znAbs - aMod);
                        ubnd = znAbs + aMod;
                        double value = (zMod - lbnd) / (ubnd - lbnd);
                        //mindist+= (isNaN(value)||isInfinite(value))?zMod-(long)zMod:value;
                        mindist += (isNaN(value)) ? 0 : ((isInfinite(value)) ? 1 : value);
                    } else if (color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                            color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, ztmp).modulus() <= tolerance || distance(z, ztmp) <= tolerance) {
                        if (color.getMode() == NEWTON_CLASSIC || color.getMode() == NEWTON_NORMALIZED_MODULUS || color.getMode() == NEWTON_NORMALIZED_ITERATIONS) {
                            synchronized (roots) {
                                if (indexOfRoot(ztmp) == -1) {
                                    roots.add(ztmp);
                                }
                            }
                        }
                        //c = iterations;
                        break;
                    }
                    zold = z;
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    fe.setOldvalue(zold.toString());
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                    maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR) {
                    histogram[c]++;
                }
                if ((color.getMode() == NEWTON_CLASSIC || color.getMode() == NEWTON_NORMALIZED_MODULUS || color.getMode() == NEWTON_NORMALIZED_ITERATIONS) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = divide(principallog(argand_map[i][j]), principallog(z)).modulus();
                @NotNull Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? Complex.ZERO : pass[m - 1];
                    }
                }
                pass[0] = new Complex(z);
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = distance_squared(pass[2], pass[1]);
                double d1 = distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = c + (log(tolerance) - log(d0)) / (log(d1) - log(d0));
                }
                int colortmp = getColorTmp(iterations, i, j, maxModulus, mindist, maxdist, c, pass);
                if (mode == Mode.SECANTBROT) {
                    argand.setPixel(toCoordinates(z)[1], toCoordinates(z)[0], argand.getPixel(toCoordinates(z)[1], toCoordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    /**
     * NOTE:Call after generating the fractal, as this uses data from {@link #escapedata}
     *
     * @param depth the iteration count to be considered as a boundary
     * @return The boundary points
     * @deprecated No replacement
     */
    @NotNull
    @Deprecated
    public Complex[] getBoundaryPoints(int depth) {
        @NotNull ArrayList<Complex> points = new ArrayList<>(2 * getImageWidth());
        for (int j = 0; j < getImageWidth(); j++) {
            int imin = -1, imax = -1;
            for (int i = 0; i < getImageHeight(); i++) {
                int itmp = -1;
                if (getEscapedata()[i][j] == depth) {
                    itmp = i;
                }
                if (imin == -1) {
                    imin = itmp;
                    imax = imin;
                }
                if (itmp > imax) {
                    imax = itmp;
                }
            }
            points.add(argand_map[imin][j]);
            points.add(argand_map[imax][j]);
        }
        @NotNull Complex[] boundaryPoints = new Complex[points.size()];
        points.toArray(boundaryPoints);
        return boundaryPoints;
    }
    private boolean isInBounds(@NotNull Complex val) {
        if (val.imaginary() <= argand_map[0][center_x].imaginary() && val.imaginary() >= argand_map[getImageHeight() - 1][center_x].imaginary()) {
            if (val.real() <= argand_map[center_y][getImageWidth() - 1].real() && val.real() >= argand_map[center_y][0].real()) {
                return true;
            }
        }
        return false;
    }
    private Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(consts[0][1]);
            } else {
                lastConstant = new Complex(consts[getLastConstantIndex()][1]);
            }
        }
        return lastConstant;
    }
    private void setLastConstant(@NotNull Complex value) {
        consts[getLastConstantIndex()][1] = value.toString();
        lastConstant = new Complex(value);
    }
    private int getLastConstantIndex() {
        @NotNull String[] parts = split(function, " ");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIdx(getConstantIndex(parts[i]));
                return lastConstantIdx;
            }
        }
        return -1;
    }
    private int getConstantIndex(String constant) {
        for (int i = 0; i < consts.length; i++) {
            if (consts[i][0].equals(constant)) {
                return i;
            }
        }
        return -1;
    }
    private String checkDE() {
        String functionderiv = "";
        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
            @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
            function = func.toString();
            functionderiv = func.firstDerivative();
            distance_estimate_multiplier = func.getDegree();
            /*if (Function.isSpecialFunction(function)) {
                @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
                function = func.toString();
                functionderiv = func.derivative(1);
                distance_estimate_multiplier = func.getDegree();
            } else {
                @NotNull Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(consts);
                poly.setVariableCode(variableCode);
                poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
                distance_estimate_multiplier = poly.getDegree();
            }*/
        }
        return functionderiv;
    }
    private void setLastConstantIdx(int lastConstantIdx) {
        this.lastConstantIdx = lastConstantIdx;
    }
    private void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        double bailout = escape_radius * escape_radius + tolerance;
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        @NotNull String functionderiv = checkDE();
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd, ubnd;
                if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.getMode() == STRIPE_AVERAGE_LINEAR ||
                        color.getMode() == STRIPE_AVERAGE_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE ||
                        color.getMode() == DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                @NotNull Complex z = (mode == Mode.RUDY || mode == Mode.RUDYBROT) ? new Complex(argand_map[i][j]) : Complex.ZERO;
                Complex zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                setLastConstant(argand_map[i][j]);
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                fe.setConstdec(consts);
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                    fed.setConstdec(consts);
                }
                int c = 0;
                last.push(z);
                lastd.push(zd);
                boolean useJulia = false;
                while (c < iterations && z.cabs() <= bailout) {
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
                            fe.setConstdec(consts);
                            fed.setConstdec(consts);
                        } else {
                            setLastConstant(argand_map[i][j]);
                            fe.setConstdec(consts);
                            fed.setConstdec(consts);
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    last.pop();
                    if (last.size() > 1) {
                        last.pop();
                        z2 = last.peek();
                        last.push(ztmp2);
                    }
                    last.push(z);
                    Complex ztmp = fe.evaluate(function, false);
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothing_base(),
                                add(ztmp,
                                        divide(
                                                new Complex(0.5),
                                                (subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == EPSILON_CROSS_LINEAR || color.getMode() == EPSILON_CROSS_SPLINE) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE_LINEAR || color.getMode() == GAUSSIAN_INT_DISTANCE_SPLINE) {
                        /*long gx = round(ztmp.real() * trap_point.modulus());
                        long gy = round(ztmp.imaginary() * trap_point.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trap_point.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE_LINEAR || color.getMode() == STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR || color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argand_map[i][j];
                        }else{
                            adjust=getLastConstant();
                        }*/
                        /*double znAbs = subtract(ztmp, adjust).modulus();
                        znAbs = (znAbs <= tolerance) ? ztmp.modulus() : znAbs;
                        lbnd = abs(znAbs - adjust.modulus());
                        ubnd = znAbs + adjust.modulus();
                        double value = (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                        mindist += (isNaN(value) || isInfinite(value)) ? 0 : value;*/
                        double znAbs = subtract(ztmp, adjust).modulus(),
                                zMod = ztmp.modulus(), aMod = adjust.modulus();
                        znAbs = (znAbs <= tolerance) ? zMod : znAbs;
                        lbnd = abs(znAbs - aMod);
                        ubnd = znAbs + aMod;
                        double value = (zMod - lbnd) / (ubnd - lbnd);
                        //mindist+= (isNaN(value)||isInfinite(value))?zMod-(long)zMod:value;
                        mindist += (isNaN(value)) ? 0 : ((isInfinite(value)) ? 1 : value);
                    } else if (color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                            color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (color.getMode() == DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                if (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR ||
                        color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                @NotNull Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? Complex.ZERO : pass[m - 1];
                    }
                }
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    pass[1] = new Complex(zd);
                    pass[2] = argand_map[i][j];
                }
                escapedata[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
                }
                int colortmp = getColorTmp(iterations, i, j, escape_radius, mindist, maxdist, c, pass);
                if (mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) {
                    argand.setPixel(toCoordinates(z)[1], toCoordinates(z)[0], argand.getPixel(toCoordinates(z)[1], toCoordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    private void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, @Nullable Complex constant) {
        @NotNull String functionderiv, functionderiv2 = "";
        Complex degree;
        @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
        function = func.toString();
        degree = func.getDegree();
        functionderiv = func.firstDerivative();
        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
            functionderiv2 = func.secondDerivative();
        }
        /*if (Function.isSpecialFunction(function)) {
            @NotNull Function func = Function.fromString(function, variableCode, oldvariablecode, consts);
            function = func.toString();
            degree = func.getDegree();
            functionderiv = func.derivative(1);
            if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                functionderiv2 = func.derivative(2);
            }
        } else {
            @NotNull Polynomial polynomial = Polynomial.fromString(function);
            polynomial.setConstdec(consts);
            polynomial.setVariableCode(variableCode);
            polynomial.setOldvariablecode(oldvariablecode);
            function = polynomial.toString();
            degree = polynomial.getDegree();
            functionderiv = polynomial.derivative().toString();
            if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                functionderiv2 = polynomial.derivative().derivative().toString();
            }
        }*/
        distance_estimate_multiplier = degree;
        roots.ensureCapacity(round((float) degree.modulus()));
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        if (constant != null && constant.equals(Complex.ZERO)) {
            constant = divide(Complex.ONE, degree);
        }
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex toadd = Complex.ZERO;
        @NotNull Complex lastConstantBackup = new Complex(getLastConstant());
        if (mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT) {
            toadd = new Complex(getLastConstant());
        }
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd, ubnd;
                if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.getMode() == STRIPE_AVERAGE_LINEAR ||
                        color.getMode() == STRIPE_AVERAGE_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE ||
                        color.getMode() == DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                boolean useJulia = false, useMandelbrot = false;
                @NotNull Complex z = argand_map[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
                    toadd = argand_map[i][j];
                    z = Complex.ZERO;
                }
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                while (c < iterations) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    if (mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
                        if (mandelbrotToJulia) {
                            if (c % switch_rate == 0) {
                                useJulia = (!useJulia);
                            }
                            if (useJulia) {
                                toadd = lastConstantBackup;
                            } else {
                                toadd = argand_map[i][j];
                            }
                        }
                    }
                    if (mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT) {
                        if (juliaToMandelbrot) {
                            if (c % switch_rate == 0) {
                                useMandelbrot = (!useMandelbrot);
                            }
                            if (useMandelbrot) {
                                toadd = argand_map[i][j];
                            } else {
                                toadd = lastConstantBackup;
                            }
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    last.pop();
                    if (last.size() > 1) {
                        last.pop();
                        z2 = last.peek();
                        last.push(ztmp2);
                    }
                    last.push(z);
                    @Nullable Complex ztmp, ztmpd;
                    fe.setOldvalue(ztmp2.toString());
                    if (constant != null) {
                        ztmp = add(subtract(z, multiply(constant, divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false)))), toadd);
                        ztmpd = null;
                        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                            ztmpd = add(subtract(zd, multiply(constant, divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false)))), toadd);
                        }
                    } else {
                        ztmp = add(subtract(z, divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false))), toadd);
                        ztmpd = null;
                        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                            ztmpd = add(subtract(zd, divide(fed.evaluate(functionderiv, false),
                                    fed.evaluate(functionderiv2, false))), toadd);
                        }
                    }
                    fe.setZ_value(ztmp.toString());
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothing_base(),
                                add(ztmp,
                                        divide(
                                                new Complex(0.5),
                                                (subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == EPSILON_CROSS_LINEAR || color.getMode() == EPSILON_CROSS_SPLINE) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE_LINEAR || color.getMode() == GAUSSIAN_INT_DISTANCE_SPLINE) {
                        /*long gx = round(ztmp.real() * trap_point.modulus());
                        long gy = round(ztmp.imaginary() * trap_point.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trap_point.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE_LINEAR || color.getMode() == STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR || color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        /*lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argand_map[i][j];
                        }else{
                            adjust=getLastConstant();
                        }*/
                        /*double znAbs = subtract(ztmp, adjust).modulus();
                        znAbs = (znAbs <= tolerance) ? ztmp.modulus() : znAbs;
                        lbnd = abs(znAbs - adjust.modulus());
                        ubnd = znAbs + adjust.modulus();
                        double value = (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                        mindist += (isNaN(value) || isInfinite(value)) ? 0 : value;*/
                        double znAbs = subtract(ztmp, adjust).modulus(),
                                zMod = ztmp.modulus(), aMod = adjust.modulus();
                        znAbs = (znAbs <= tolerance) ? zMod : znAbs;
                        lbnd = abs(znAbs - aMod);
                        ubnd = znAbs + aMod;
                        double value = (zMod - lbnd) / (ubnd - lbnd);
                        //mindist+= (isNaN(value)||isInfinite(value))?zMod-(long)zMod:value;
                        mindist += (isNaN(value)) ? 0 : ((isInfinite(value)) ? 1 : value);
                    } else if (color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                            color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, ztmp).modulus() <= tolerance || distance(z, ztmp) <= tolerance) {
                        if (color.getMode() == NEWTON_CLASSIC || color.getMode() == NEWTON_NORMALIZED_MODULUS || color.getMode() == NEWTON_NORMALIZED_ITERATIONS) {
                            synchronized (roots) {
                                if (indexOfRoot(ztmp) == -1) {
                                    roots.add(ztmp);
                                }
                            }
                        }
                        //c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                    maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR ||
                        color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                if ((color.getMode() == NEWTON_CLASSIC || color.getMode() == NEWTON_NORMALIZED_MODULUS || color.getMode() == NEWTON_NORMALIZED_ITERATIONS) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = divide(principallog(argand_map[i][j]), principallog(z)).modulus();
                @NotNull Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? Complex.ZERO : pass[m - 1];
                    }
                }
                pass[0] = new Complex(z);
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = distance_squared(pass[2], pass[1]);
                double d1 = distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = c + abs((log(tolerance) - log(d0)) / (log(d1) - log(d0)));
                }
                int colortmp = getColorTmp(iterations, i, j, maxModulus, mindist, maxdist, c, pass);
                if (mode == Mode.NEWTONBROT || mode == Mode.JULIA_NOVABROT || mode == Mode.MANDELBROT_NOVABROT) {
                    argand.setPixel(toCoordinates(z)[1], toCoordinates(z)[0], argand.getPixel(toCoordinates(z)[1], toCoordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    private int getColorTmp(int iterations, int i, int j, double escape_radius, double mindist, double maxdist, int c, Complex[] pass) {
        int colortmp;
        switch (color.getMode()) {
            case ORBIT_TRAP_MIN:
            case LINE_TRAP_MIN:
                colortmp = getColor(i, j, c, pass, mindist, iterations);
                break;
            case ORBIT_TRAP_MAX:
            case LINE_TRAP_MAX:
            case DOMAIN_COLORING_FAUX:
                colortmp = getColor(i, j, c, pass, maxdist, iterations);
                break;
            case ORBIT_TRAP_AVG:
            case LINE_TRAP_AVG:
                colortmp = getColor(i, j, c, pass, (mindist + maxdist) / 2, iterations);
                break;
            case EPSILON_CROSS_LINEAR:
            case EPSILON_CROSS_SPLINE:
            case GAUSSIAN_INT_DISTANCE_LINEAR:
            case GAUSSIAN_INT_DISTANCE_SPLINE:
                colortmp = getColor(i, j, c, pass, mindist, iterations);
                break;
            case CURVATURE_AVERAGE_NOABS_LINEAR:
            case CURVATURE_AVERAGE_NOABS_SPLINE:
            case CURVATURE_AVERAGE_ABS_LINEAR:
            case CURVATURE_AVERAGE_ABS_SPLINE:
            case STRIPE_AVERAGE_LINEAR:
            case STRIPE_AVERAGE_SPLINE:
            case TRIANGLE_AREA_INEQUALITY_LINEAR:
            case TRIANGLE_AREA_INEQUALITY_SPLINE:
                colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations);
                break;
            default:
                colortmp = getColor(i, j, c, pass, escape_radius, iterations);
        }
        return colortmp;
    }
    private void publishProgress(long ctr, int i, int startx, int endx, int j, int starty, int endy) {
        if (!silencer) {
            float completion = ((float) ((i - starty) * (endx - startx) + (j - startx)) / ((endx - startx) * (endy - starty)));
            progressPublisher.publish(ctr + " iterations of " + maxiter + ",completion = " + (completion * 100.0f) + "%", completion,
                    (i * (endx - startx) + j));
        }
    }
    private int indexOfRoot(@NotNull Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (distance_squared(roots.get(i), z) < tolerance) {
                return i;
            }
        }
        return -1;
    }
    private int closestRootIndex(@NotNull Complex z) {
        int leastDistanceIdx = 0;
        double leastDistance = distance_squared(z, roots.get(0));
        for (int i = 1; i < roots.size(); i++) {
            double distance = distance_squared(z, roots.get(i));
            if (distance < leastDistance) {
                leastDistance = distance;
                leastDistanceIdx = i;
            }
        }
        return leastDistanceIdx;
    }
    private void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        double bailout = escape_radius * escape_radius + tolerance;
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        @NotNull String functionderiv = checkDE();
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd, ubnd;
                int c = 0x0;
                if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.getMode() == STRIPE_AVERAGE_LINEAR ||
                        color.getMode() == STRIPE_AVERAGE_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR ||
                        color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE ||
                        color.getMode() == DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                boolean useMandelBrot = false;
                while (c < iterations && z.cabs() <= bailout) {
                    if (stop) {
                        return;
                    }
                    checkAndDoPause();
                    if (juliaToMandelbrot) {
                        if (c % switch_rate == 0) {
                            useMandelBrot = (!useMandelBrot);
                        }
                        if (useMandelBrot) {
                            setLastConstant(argand_map[i][j]);
                            fe.setConstdec(consts);
                            fed.setConstdec(consts);
                        } else {
                            setLastConstant(lastConstantBackup);
                            fe.setConstdec(consts);
                            fed.setConstdec(consts);
                        }
                    }
                    last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2;
                    last.push(z);
                    fe.setOldvalue(ztmp2.toString());
                    last.pop();
                    if (last.size() > 1) {
                        last.pop();
                        z2 = last.peek();
                        last.push(ztmp2);
                    }
                    last.push(z);
                    Complex ztmp = fe.evaluate(function, false);
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothing_base(),
                                add(ztmp,
                                        divide(
                                                new Complex(0.5),
                                                (subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == EPSILON_CROSS_LINEAR || color.getMode() == EPSILON_CROSS_SPLINE) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = Math.min(distance, mindist);
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE_LINEAR || color.getMode() == GAUSSIAN_INT_DISTANCE_SPLINE) {
                        /*long gx = round(ztmp.real() * trap_point.modulus());
                        long gy = round(ztmp.imaginary() * trap_point.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trap_point.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE_LINEAR || color.getMode() == STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY_LINEAR || color.getMode() == TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argand_map[i][j];
                        }else{
                            adjust=getLastConstant();
                        }*/
                        /*double znAbs = subtract(ztmp, adjust).modulus();
                        znAbs = (znAbs <= tolerance) ? ztmp.modulus() : znAbs;
                        lbnd = abs(znAbs - adjust.modulus());
                        ubnd = znAbs + adjust.modulus();
                        double value = (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                        mindist += (isNaN(value) || isInfinite(value)) ? 0 : value;*/
                        double znAbs = subtract(ztmp, adjust).modulus(),
                                zMod = ztmp.modulus(), aMod = adjust.modulus();
                        znAbs = (znAbs <= tolerance) ? zMod : znAbs;
                        lbnd = abs(znAbs - aMod);
                        ubnd = znAbs + aMod;
                        double value = (zMod - lbnd) / (ubnd - lbnd);
                        //mindist+= (isNaN(value)||isInfinite(value))?zMod-(long)zMod:value;
                        mindist += (isNaN(value)) ? 0 : ((isInfinite(value)) ? 1 : value);
                    } else if (color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_NOABS_SPLINE ||
                            color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (color.getMode() == DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (distance_squared(z, ztmp) <= tolerance) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                if (color.getMode() == HISTOGRAM_SPLINE || color.getMode() == HISTOGRAM_LINEAR ||
                        color.getMode() == RANK_ORDER_LINEAR || color.getMode() == RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                @NotNull Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? Complex.ZERO : pass[m - 1];
                    }
                }
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
                }
                int colortmp = getColorTmp(iterations, i, j, escape_radius, mindist, maxdist, c, pass);
                if (mode == Mode.JULIABROT) {
                    argand.setPixel(toCoordinates(z)[1], toCoordinates(z)[0], argand.getPixel(toCoordinates(z)[1], toCoordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    private double getNormalized(int val, int iterations, Complex[] z_values, double escape) {
        Complex z = z_values[0]; /*double degree = this.degree.modulus(); if (escape < zoom) {
            degree = log(z.modulus() * z.modulus()) / log(z_values[1].modulus() * z_values[1].modulus());
        }*/
        double renormalized, degree = log(z.modulus() * z.modulus()) / log(z_values[1].modulus() * z_values[1].modulus());
        if (!color.isLogIndex() || (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT)) {
            if (degree == 0 || degree == 1) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = val - (log(log(z.modulus() / log(escape))) / log(degree));
                if (renormalized - (long) renormalized == 0) {
                    renormalized += ((double) val / iterations);
                }
            }
        } else {
            renormalized = val + (0.5 + 0.5 * (sin(z.arg()) * stripe_density));
        }
        return (isNaN(renormalized) ? 0 : (isInfinite(renormalized) ? 1 : renormalized));
    }
    public int[] getHistogram() {
        return histogram;
    }
    public double[][] getNormalized_escapes() {
        return normalized_escapes;
    }
    public double averageIterations() {
        return vals / valCount;
    }
    public int getColor(int i, int j, int val, Complex[] last, double escape_radius, int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Illegal maximum iteration count : " + iterations);
        }
        int colortmp, colortmp1, colortmp2, color1, color2, color3, index;
        double renormalized, lbnd = 0.0, ubnd = 1.0, calc, scaling = base_precision * pow(zoom, zoom_factor), smoothcount;
        renormalized = normalized_escapes[i][j];
        smoothcount = renormalized;
        if ((!(color.isExponentialSmoothing() ||
                mode == Mode.NEWTON || mode == Mode.NEWTONBROT ||
                mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT ||
                mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT)) &&
                color.isLogIndex()) {
            smoothcount = (renormalized > 0) ? abs(log(renormalized)) : principallog(new Complex(renormalized, 0)).modulus();
            smoothcount = (isNaN(smoothcount) ? 0 : (isInfinite(smoothcount) ? 1 : smoothcount));
            normalized_escapes[i][j] = (isNaN(renormalized) ? 0 : (isInfinite(renormalized) ? 1 : renormalized));
        } else {
            normalized_escapes[i][j] = (isNaN(smoothcount) ? 0 : (isInfinite(smoothcount) ? 1 : smoothcount));
        }
        escape_radius = (isNaN(escape_radius) ? 0 : (isInfinite(escape_radius) ? 1 : escape_radius));
        for (int c = 0; c < last.length; ++c) {
            last[c] = (isNaN(last[c].cabs()) ? Complex.ZERO : (isInfinite(last[c].cabs()) ? Complex.ONE : last[c]));
        }
        int nextVal = (val == iterations) ? iterations : val, previousVal = (val == 0) ? 0 : val - 1, backup = 0;
        double interpolation = smoothcount - (long) smoothcount;
        vals += val;
        valCount++;
        switch (color.getMode()) {
            case SIMPLE:
                colortmp = color.getColor(color.createIndex(val, 0, iterations));
                break;
            case SIMPLE_SMOOTH_LINEAR:
                colortmp = getInterpolated(color.createIndex(val, 0, iterations), interpolation);
                break;
            case SIMPLE_SMOOTH_SPLINE:
                colortmp = color.splineInterpolated(color.createIndex(val, 0, iterations), interpolation);
                break;
            case DIVIDE:
                val = (val == 0) ? iterations + 1 : (val - 1 == 0) ? iterations + 1 : val;
                color1 = (0xffffff / val);
                color2 = (0xffffff / (val + 1));
                color3 = (0xffffff / (val - 1));
                colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                break;
            case DIVIDE_NORMALIZED:
                color1 = (int) (0xffffff / renormalized);
                color2 = (int) (0xffffff / (renormalized + 1));
                color3 = (int) (0xffffff / (renormalized - 1));
                colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                break;
            case GRAYSCALE_HIGH_CONTRAST:
                colortmp = Colorizer.toGray(val * iterations);
                break;
            case SIMPLE_DISTANCE_ESTIMATION:
                calc = abs((double) val / iterations);
                if (calc > 1) {
                    calc = calc - 1;
                }
                colortmp1 = (int) (calc * 255);
                if (calc > 0.5) {
                    colortmp = Colorizer.toRGB(colortmp1, 255, colortmp1);
                } else {
                    colortmp = Colorizer.toRGB(0, colortmp1, 0);
                }
                break;
            case MULTIPLY:
                color1 = Colorizer.toGray(val);
                color2 = Colorizer.toGray(val + 1);
                color3 = Colorizer.toGray(abs((val - 1)));
                colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                break;
            case MULTIPLY_NORMALIZED:
                color1 = Colorizer.toGray((int) abs(renormalized));
                color2 = Colorizer.toGray((int) abs(renormalized + 1));
                color3 = Colorizer.toGray((int) abs(renormalized - 1));
                colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                break;
            case GRAYSCALE_LOW_CONTRAST:
                colortmp = Colorizer.toGray(val);
                break;
            case DISTANCE_ESTIMATION_GRAYSCALE:
            case DISTANCE_ESTIMATION_COLOR:
            case DISTANCE_ESTIMATION_2C_OR_BW:
                double distance;
                if ((mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT)) {
                    distance = abs(distance_estimate_multiplier.modulus() *
                            (sqrt(last[0].cabs() / last[1].cabs()) * 0.5 * log(last[0].cabs())));
                } else {
                    distance = abs(distance_estimate_multiplier.modulus() *
                            (last[0].modulus() * log(last[0].modulus())) / log(last[1].modulus()));
                }
                distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE) {
                    colortmp = Colorizer.toGray(boundsProtected((int) abs((distance - (long) distance) * 255), 256));
                } else if (color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    if ((!color.isModifierEnabled()) && (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.JULIA || mode == Mode.JULIABROT)) {
                        /*if (!color.isModifierEnabled()) {*/
                        colortmp = (distance > escape_radius) ? colorIfMore : colorIfLess;
                        /*} else {
                            colortmp = (distance > sqrt(distance_squared(last[0], last[2]))) ?
                                    colorIfMore : colorIfLess;
                        }*/
                    } else {
                        colortmp = (distance > sqrt(distance_squared(centre_offset, last[0]))) ? colorIfMore : colorIfLess;
                    }
                } else {
                    index = color.createIndex((distance - (long) distance), lbnd, ubnd);
                    //colortmp = color.splineInterpolated(index, distance - (long) distance);
                    if (color.isModifierEnabled()) {
                        colortmp = getInterpolated(index, interpolation);
                    } else {
                        colortmp = color.splineInterpolated(index, interpolation);
                    }
                }
                break;
            case HISTOGRAM_SPLINE:
            case HISTOGRAM_LINEAR:
            case RANK_ORDER_LINEAR:
            case RANK_ORDER_SPLINE:
            case ASCII_ART_NUMERIC:
            case ASCII_ART_CHARACTER:
                colortmp = 0xff000000;//Don't need to deal with this here, it's post-calculated
                break;
            case NEWTON_NORMALIZED_MODULUS:
            case NEWTON_NORMALIZED_ITERATIONS:
            case NEWTON_CLASSIC:
                if (color.isModifierEnabled()) {
                    int color_density_backup = color.getColor_density();
                    if (color.getMode() == NEWTON_NORMALIZED_ITERATIONS) {
                        color.changeColorDensity(Math.round(((float) val / iterations) * color_density_backup));
                    } else if (color.getMode() == NEWTON_NORMALIZED_MODULUS) {
                        color.changeColorDensity(Math.round((float) escape_radius));
                    }
                    index = color.createIndexSimple(closestRootIndex(last[0]), 0, roots.size());
                    int preTintColor = color.getColor(index);
                    color1 = color.getTint(preTintColor, ((double) val / iterations));
                    color2 = color.getTint(preTintColor, ((double) nextVal / iterations));
                    color3 = color.getTint(preTintColor, ((double) previousVal / iterations));
                    colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                    colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                    colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                    color.setColor_density(color_density_backup);
                } else {
                    if (color.getMode() == Colors.MODE.NEWTON_NORMALIZED_MODULUS) {
                        if (color.getByParts() > 0) {
                            color1 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.getNum_colors(), ((double) nextVal / iterations));
                            color3 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) previousVal / iterations));
                            backup = color.getByParts();
                            color.setByParts(0);
                        } else {
                            color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.getNum_colors(), ((double) nextVal / iterations));
                            color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) previousVal / iterations));
                        }
                        colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                        colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                        colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                        color.setByParts(backup);
                    } else if (color.getMode() == Colors.MODE.NEWTON_NORMALIZED_ITERATIONS) {
                        if (color.getByParts() > 0) {
                            color1 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.getNum_colors(), ((double) nextVal / iterations));
                            color3 = color.getShade(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) previousVal / iterations));
                            backup = color.getByParts();
                            color.setByParts(0);
                        } else {
                            color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.getNum_colors(), ((double) nextVal / iterations));
                            color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.getNum_colors()), ((double) previousVal / iterations));
                        }
                        colortmp1 = Colorizer.linearInterpolated(color1, color2, val, iterations, color.getByParts());
                        colortmp2 = Colorizer.linearInterpolated(color3, color1, val, iterations, color.getByParts());
                        colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, val, iterations, color.getByParts());
                        color.setByParts(backup);
                    } else if (color.getMode() == Colors.MODE.NEWTON_CLASSIC) {
                        if (color.getByParts() > 0) {
                            color1 = color.getShade(color.getColor((closestRootIndex(last[0]) * color.getColor_density()) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getShade(color.getColor((closestRootIndex(last[0]) * color.getColor_density()) % color.getNum_colors()), ((double) nextVal / iterations));
                            color3 = color.getShade(color.getColor((closestRootIndex(last[0]) * color.getColor_density()) % color.getNum_colors()), ((double) previousVal / iterations));
                            backup = color.getByParts();
                            color.setByParts(0);
                        } else {
                            color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.getColor_density()) % color.getNum_colors()), ((double) val / iterations));
                            color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.getColor_density())) % color.getNum_colors(), ((double) nextVal / iterations));
                            color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.getColor_density()) % color.getNum_colors()), ((double) previousVal / iterations));
                        }
                        colortmp1 = Colorizer.linearInterpolated(color1, color2, interpolation, color.getByParts());
                        colortmp2 = Colorizer.linearInterpolated(color3, color1, interpolation, color.getByParts());
                        colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, interpolation, color.getByParts());
                        color.setByParts(backup);
                    } else {
                        colortmp = 0xff000000;
                    }
                }
                break;
            case CURVATURE_AVERAGE_NOABS_LINEAR:
            case CURVATURE_AVERAGE_NOABS_SPLINE:
            case CURVATURE_AVERAGE_ABS_LINEAR:
            case CURVATURE_AVERAGE_ABS_SPLINE:
                if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_ABS_SPLINE) {
                    lbnd = 0;
                } else {
                    lbnd = -PI;
                }
                ubnd = PI;
                index = color.createIndex(escape_radius, lbnd, ubnd);
                if (color.getMode() == CURVATURE_AVERAGE_ABS_LINEAR || color.getMode() == CURVATURE_AVERAGE_NOABS_LINEAR) {
                    colortmp = getInterpolated(index, interpolation);
                } else {
                    colortmp = color.splineInterpolated(index, interpolation);
                }
                break;
            case STRIPE_AVERAGE_LINEAR:
            case STRIPE_AVERAGE_SPLINE:
                //min value of 0.5*sin(x)+0.5=0, min value of sin(x)=-1,max value of 0.5*sin(x)+0.5=1, max value of sin(x)=1
                index = color.createIndex(escape_radius, lbnd, ubnd);
                if (color.getMode() == STRIPE_AVERAGE_LINEAR) {
                    colortmp = getInterpolated(index, interpolation);
                } else {
                    colortmp = color.splineInterpolated(index, interpolation);
                }
                break;
            case TRIANGLE_AREA_INEQUALITY_LINEAR:
                colortmp = getInterpolated(color.createIndex(escape_radius, lbnd, ubnd), interpolation);
                break;
            case TRIANGLE_AREA_INEQUALITY_SPLINE:
                colortmp = color.splineInterpolated(color.createIndex(escape_radius, lbnd, ubnd), interpolation);
                break;
            case EPSILON_CROSS_LINEAR:
            case EPSILON_CROSS_SPLINE:
                lbnd = 0;
                ubnd = abs(argand_map[0][0].modulus() - trap_point.modulus());
                /*if(isNaN(escape_radius)){calc=-1;}
                else if(escape_radius<0){
                    calc=abs(a+c*(-escape_radius)/abs(trap_point.imaginary()));}else{
                    calc=abs(a+c*(-escape_radius)/abs(trap_point.real()));}
                if(calc==-1){colortmp=color.getColor(color.createIndex(val,0,iterations,scaling));}else {
                    calc = (calc > 1) ? calc - (long) calc : calc;
                    index = color.createIndex(calc, lbnd, ubnd, scaling);
                    if(color.getMode()== EPSILON_CROSS_LINEAR){
                        colortmp=getInterpolated(index,smoothcount);}else{
                        colortmp=color.splineInterpolated(index,smoothcount-(long)smoothcount);}}*/
                if (scaling <= color.getNum_colors()) {
                    if (escape_radius < trap_point.modulus() * base_precision) {
                        smoothcount = abs(val - escape_radius);
                    } else {
                        smoothcount = log(1 + escape_radius);
                    }
                    escape_radius = escape_radius - (long) escape_radius;
                } else {
                    smoothcount = principallog(new Complex(escape_radius / ubnd)).modulus();
                    smoothcount = isNaN(smoothcount) ? 0.0 : (isInfinite(smoothcount) ? 1.0 : smoothcount);
                }
                interpolation = smoothcount - (long) smoothcount;
                if (color.getMode() == EPSILON_CROSS_LINEAR) {
                    colortmp = getInterpolated(color.createIndex(escape_radius, lbnd, ubnd), interpolation);
                } else {
                    colortmp = color.splineInterpolated(color.createIndex(escape_radius, lbnd, ubnd), interpolation);
                }
                break;
            case GAUSSIAN_INT_DISTANCE_LINEAR:
                lbnd = 0;
                ubnd = argand_map[0][0].modulus() - centre_offset.modulus();
                colortmp = getInterpolated(color.createIndex(abs(escape_radius), lbnd, ubnd), interpolation);
                break;
            case GAUSSIAN_INT_DISTANCE_SPLINE:
                lbnd = 0;
                ubnd = argand_map[0][0].modulus() - centre_offset.modulus();
                colortmp = color.splineInterpolated(color.createIndex(abs(escape_radius), lbnd, ubnd), interpolation);
                break;
            case ORBIT_TRAP_AVG:
            case ORBIT_TRAP_MAX:
            case ORBIT_TRAP_MIN:
            case LINE_TRAP_MIN:
            case LINE_TRAP_MAX:
            case LINE_TRAP_AVG:
                lbnd = 0;
                double origin_modulus = (trap_point == null) ? centre_offset.modulus() : trap_point.modulus();
                ubnd = abs(argand_map[0][0].modulus() - origin_modulus);
                index = color.createIndex(abs(escape_radius), lbnd, ubnd);
                if (color.isModifierEnabled()) {
                    colortmp = getInterpolated(index, interpolation);
                } else {
                    colortmp = color.splineInterpolated(index, interpolation);
                }
                break;
            case DOMAIN_COLORING:
                colortmp = new HSL(
                        HSL.hueFromAngle(last[0].arg() + PI),
                        last[0].modulus() / (2 * modulusForPhase(last[0].arg())),
                        (((double) val) / iterations)
                        /*Math.min(abs(last[0].imaginary()),
                                abs(last[0].real())) / Math.max(abs(last[0].imaginary()),
                                abs(last[0].real()))*/).toRGB();
                break;
            case DOMAIN_COLORING_FAUX:
                colortmp = new HSL(
                        HSL.hueFromAngle(last[0].arg() + PI),
                        last[0].modulus() / (2 * escape_radius),
                        (((double) val) / iterations)
                        /*Math.min(abs(last[0].imaginary()),
                                abs(last[0].real())) / Math.max(abs(last[0].imaginary()),
                                abs(last[0].real()))*/).toRGB();
                break;
            default:
                throw new IllegalArgumentException("invalid argument");
        }
        return colortmp;
    }
    private int getInterpolated(int index, double smoothcount) {
        int color1, color2, color3, colortmp1, colortmp2, colortmp,
                index2 = boundsProtected(index + 1, color.getNum_colors()),
                index3 = boundsProtected(index - 1, color.getNum_colors());
        color1 = color.getColor(index);
        color2 = color.getColor(index2);
        color3 = color.getColor(index3);
        colortmp1 = Colorizer.linearInterpolated(color1, color2, smoothcount, color.getByParts());
        colortmp2 = Colorizer.linearInterpolated(color3, color1, smoothcount, color.getByParts());
        colortmp = Colorizer.linearInterpolated(colortmp2, colortmp1, smoothcount, color.getByParts());
        return colortmp;
    }
    @NotNull
    public String createASCIIArt() {
        return createASCIIArt(params.runParams.iterations);
    }
    @NotNull
    public String createASCIIArt(int iterations) {
        CharBuffer buffer = new ResizableCharBuffer((getImageWidth() + 1) * getImageHeight());
        if (color.getMode() == ASCII_ART_CHARACTER) {
            char[] lookup = new char[iterations];
            for (int i = 1; i < lookup.length; ++i) {
                lookup[i] = asciiArtBase.charAt(boundsProtected(i - 1, asciiArtBase.length()));
            }
            lookup[lookup.length - 1] = lookup[0] = ' ';
            for (int[] anEscapedata : escapedata) {
                for (int anAnEscapedata : anEscapedata) {
                    buffer.append(lookup[boundsProtected(anAnEscapedata, lookup.length)]);
                }
                buffer.append('\n');
            }
        } else {
            for (int[] anEscapedata : escapedata) {
                for (int anAnEscapedata : anEscapedata) {
                    buffer.append(String.valueOf(anAnEscapedata)).append(" ");
                }
                buffer.append("\n");
            }
        }
        return buffer.toString().trim();
    }
    @NotNull
    public int[] toCoordinates(Complex point) {
        if (abs(params.initParams.skew) >= tolerance) {
            /*Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew).inverse();
            point = matrixToComplex(MatrixOperations.multiply(rotor, complexToMatrix(point)));*/
            point = matrixToComplex(doRotate(complexToMatrix(point), complexToMatrix(centre_offset), -params.initParams.skew));
        }
        point = subtract(point, centre_offset);
        return new int[]{boundsProtected(round((float) (point.real() * scale) + center_x), getImageWidth()),
                boundsProtected(round(center_y - (float) (point.imaginary() * scale)), getImageHeight())};
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
        setCenter_x(getImageWidth() / 2);
        setCenter_y(getImageHeight() / 2);
        resetCentre_Offset();
    }
    @Override
    public int getConfiguredWidth() {
        return params.initParams.getWidth();
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
        @NotNull ComplexFractalParams modified = new ComplexFractalParams(params);
        modified.initParams.setWidth(width);
        initFractal(modified);
    }
    @Override
    public int getConfiguredHeight() {
        return params.initParams.getHeight();
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
        @NotNull ComplexFractalParams modified = new ComplexFractalParams(params);
        modified.initParams.setHeight(height);
        initFractal(modified);
    }
    private void resetCentre_Offset() {
        centre_offset = Complex.ZERO;
    }
    private void setCenter_x(int center_x) {
        this.center_x = center_x;
    }
    private void setCenter_y(int center_y) {
        this.center_y = center_y;
    }
    private void changeMode(@NotNull Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) ? Mode.JULIABROT : ((mode == Mode.MANDELBROT || mode == Mode.RUDY) ? Mode.JULIA : mode));
    }
    public void zoom(@NotNull Matrix centre_offset, double level) {
        params.zoomConfig.addZoom(new ZoomParams(centre_offset, level));
        zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level);
    }
    public void zoom(@NotNull Complex centre_offset, double level) {
        params.zoomConfig.addZoom(new ZoomParams(complexToMatrix(centre_offset), level));
        setCentre_offset(centre_offset);
        setZoom_factor(level);
        setScale(base_precision * pow(zoom, zoom_factor));
        //setCenter_x(toCoordinates(centre_offset)[0]);setCenter_y(toCoordinates(centre_offset)[1]);
        populateMap();
    }
    private void populateMap() {
        for (int i = 0; i < getImageHeight(); i++) {
            for (int j = 0; j < getImageWidth(); j++) {
                argand_map[i][j] = fromCoordinates(j, i);
            }
        }
        if (color.getMode() == DOMAIN_COLORING && boundary_elements == null) {
            boundary_elements = new Complex[2 * (getImageHeight() + getImageWidth() - 2)];
            int j = 0;
            for (int i = 0; i < getImageHeight(); i++, ++j) {
                boundary_elements[j] = argand_map[i][0];
                boundary_elements[++j] = argand_map[i][getImageWidth() - 1];
            }
            for (int i = 1; i < getImageWidth() - 1; i++, ++j) {
                boundary_elements[j] = argand_map[0][i];
                boundary_elements[++j] = argand_map[getImageHeight() - 1][i];
            }
        }
    }
    @NotNull
    public Complex fromCoordinates(int x, int y) {
        @NotNull Complex point = add(new Complex(((boundsProtected(x, getImageWidth()) - center_x) / scale),
                ((center_y - boundsProtected(y, getImageWidth())) / scale)), centre_offset);
        if (abs(params.initParams.skew) > tolerance) {
            /*Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew);
            point = matrixToComplex(MatrixOperations.multiply(rotor, complexToMatrix(point)));*/
            point = matrixToComplex(doRotate(complexToMatrix(point), complexToMatrix(centre_offset), params.initParams.skew));
        }
        return point;
    }
    public void setCentre_offset(@NotNull Complex centre_offset) {
        this.centre_offset = new Complex(centre_offset);
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    public void setZoom_factor(double zoom_factor) {
        this.zoom_factor = zoom_factor;
    }
    public void mandelbrotToJulia(int cx, int cy, double level) {
        zoom(cx, cy, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void zoom(int cx, int cy, double level) {
        params.zoomConfig.addZoom(new ZoomParams(cx, cy, level));
        cx = boundsProtected(cx, argand.getWidth());
        cy = boundsProtected(cy, argand.getHeight());
        //setCenter_x(cx);setCenter_y(cy);
        setCentre_offset(fromCoordinates(cx, cy));
        setZoom_factor(level);
        setScale(base_precision * pow(zoom, zoom_factor));
        populateMap();
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
    @Override
    public void pan(int distance, double angle) {
        pan(distance, angle, false);
    }
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (PI / 2) - angle : angle;
        pan((int) (distance * cos(angle)), (int) (distance * sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(center_x + x_dist, center_y + y_dist, zoom_factor);
        @NotNull int[][] tmp_escapes = new int[escapedata.length][escapedata[0].length];
        @NotNull double[][] tmp_normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        @NotNull PixelContainer tmp_argand = new LinearizedPixelContainer(argand);
        for (int i = 0; i < tmp_escapes.length && i < tmp_normalized_escapes.length; i++) {
            System.arraycopy(escapedata[i], 0, tmp_escapes[i], 0, tmp_escapes[i].length);
            System.arraycopy(normalized_escapes[i], 0, tmp_normalized_escapes[i], 0, tmp_normalized_escapes[i].length);
        }
        argand = new PixelContainer(tmp_argand.getWidth(), tmp_argand.getHeight());
        escapedata = new int[tmp_escapes.length][tmp_escapes[0].length];
        normalized_escapes = new double[tmp_normalized_escapes.length][tmp_normalized_escapes[0].length];
        if (y_dist > 0) {
            for (int i = 0, j = y_dist; i < getImageHeight() - y_dist && j < getImageHeight(); i++, j++) {
                rangedCopyHelper(i, j, x_dist, tmp_escapes, tmp_normalized_escapes, tmp_argand);
            }
        } else {
            for (int i = (-y_dist), j = 0; i < getImageHeight() && j < getImageHeight() + y_dist; i++, j++) {
                rangedCopyHelper(i, j, x_dist, tmp_escapes, tmp_normalized_escapes, tmp_argand);
            }
        }
    }
    private void rangedCopyHelper(int i, int j, int x_dist, int[][] tmp_escapes, double[][] tmp_normalized_escapes, @NotNull PixelContainer tmp_argand) {
        if (x_dist < 0) {
            System.arraycopy(tmp_escapes[i], (-x_dist), escapedata[j], 0, escapedata[j].length + x_dist);
            System.arraycopy(tmp_normalized_escapes[i], (-x_dist), normalized_escapes[j], 0, normalized_escapes[j].length + x_dist);
            for (int k = (-x_dist), l = 0; k < tmp_argand.getWidth() && l < tmp_argand.getWidth() + x_dist; k++, l++) {
                argand.setPixel(j, l, tmp_argand.getPixel(i, k));
            }
        } else {
            System.arraycopy(tmp_escapes[i], 0, escapedata[j], x_dist, escapedata[j].length - x_dist);
            System.arraycopy(tmp_normalized_escapes[i], 0, normalized_escapes[j], x_dist, normalized_escapes[j].length - x_dist);
            for (int k = 0, l = x_dist; k < tmp_argand.getWidth() - x_dist && l < tmp_argand.getWidth(); k++, l++) {
                argand.setPixel(j, l, tmp_argand.getPixel(i, k));
            }
        }
    }
    public enum Mode {MANDELBROT, JULIA, NEWTON, BUDDHABROT, NEWTONBROT, JULIABROT, MANDELBROT_NOVA, JULIA_NOVA, MANDELBROT_NOVABROT, JULIA_NOVABROT, SECANT, SECANTBROT, RUDY, RUDYBROT}
}