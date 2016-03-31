package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.Color_Utils_Config;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.helpers.MathUtils;
import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.imgutils.containers.ImageData;
import in.tamchow.fractal.imgutils.containers.LinearizedImageData;
import in.tamchow.fractal.math.FixedStack;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.ComplexOperations;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;
import in.tamchow.fractal.math.symbolics.Function;
import in.tamchow.fractal.math.symbolics.Polynomial;

import java.util.ArrayList;
/**
 * The actual fractal plotter for Julia, Newton, Nova (both Mandelbrot and Julia variants),Secant and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes.
 * Various (21) Coloring modes
 */
public final class ComplexFractalGenerator implements PixelFractalGenerator {
    private static ArrayList<Complex> roots;
    protected Color_Utils_Config color;
    Complex[] boundary_elements;
    double zoom, zoom_factor, base_precision, scale;
    int center_x, center_y, lastConstantIdx, stripe_density, switch_rate;
    Mode mode;
    double tolerance;
    long maxiter;
    ImageData argand;
    String function;
    String[][] consts;
    int[][] escapedata;
    Complex[][] argand_map;
    Complex centre_offset, lastConstant, trap_point;
    boolean mandelbrotToJulia, juliaToMandelbrot, useLineTrap, silencer, simpleSmoothing;
    int[] histogram;
    double[][] normalized_escapes;
    double a, b, c;
    Publisher progressPublisher;
    ComplexFractalParams params;
    private String variableCode, oldvariablecode;
    public ComplexFractalGenerator(ComplexFractalParams params, Publisher progressPublisher) {
        this.params = params;
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.zoom_factor, params.initParams.base_precision, params.initParams.fractal_mode, params.initParams.function, params.initParams.consts, params.initParams.variableCode, params.initParams.oldvariablecode, params.initParams.tolerance, params.initParams.color, params.initParams.switch_rate, params.initParams.trap_point, params.initParams.linetrap);
        if (params.zoomConfig.zooms != null) {
            for (ZoomParams zoom : params.zoomConfig.zooms) {
                zoom(zoom);
            }
        }
        this.progressPublisher = progressPublisher;
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, Color_Utils_Config color, Publisher progressPublisher) {
        //initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, oldvariablecode, tolerance, new Complex(-1, 0), color, 0, Complex.ZERO, null);
        //this.progressPublisher = progressPublisher;
        //ComplexFractalParams params=new ComplexFractalParams();
        //params.initParams=new ComplexFractalInitParams(width,height,zoom,zoom_factor,base_precision,mode,function,consts,variableCode,oldvariablecode,tolerance,color,0,Complex.ZERO,null,0);
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, oldvariablecode, tolerance, color, 0, Complex.ZERO, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, Publisher progressPublisher, int switch_rate, Complex trap_point) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, trap_point, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, Publisher progressPublisher, int switch_rate, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, Complex.ZERO, linetrap, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, Publisher progressPublisher, int switch_rate, Complex trap_point, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, color, switch_rate, trap_point, linetrap, 0), null), progressPublisher);
    }
    public ImageData getPlane() {
        return getArgand();
    }
    private void initFractal(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, Color_Utils_Config color, int switch_rate, Complex trap_point, String linetrap) {
        silencer = params.useThreadedGenerator();
        argand = new LinearizedImageData(width, height);
        setMode(mode);
        setMaxiter(argand.getHeight() * argand.getWidth());
        argand_map = new Complex[argand.getHeight()][argand.getWidth()];
        escapedata = new int[argand.getHeight()][argand.getWidth()];
        normalized_escapes = new double[argand.getHeight()][argand.getWidth()];
        setVariableCode(variableCode);
        setZoom(zoom);
        setZoom_factor(zoom_factor);
        setFunction(function);
        setBase_precision(base_precision);
        setConsts(consts);
        setScale(this.base_precision * Math.pow(zoom, zoom_factor));
        resetCentre();
        setOldvariablecode(oldvariablecode);
        setTolerance(tolerance);
        if (roots == null) {
            roots = new ArrayList<>();
        }
        setColor(color);
        lastConstant = new Complex(-1, 0);
        if ((this.color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE || this.color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR) || ((!this.color.isExponentialSmoothing()) && (this.color.isLogIndex() && (!(mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT))))) {
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
            a = Double.valueOf(StringManipulator.split(linetrap, ",")[0]);
            b = Double.valueOf(StringManipulator.split(linetrap, ",")[1]);
            c = Double.valueOf(StringManipulator.split(linetrap, ",")[2]);
            useLineTrap = true;
        }
        if (color.getSmoothing_base().equals(Complex.E)) {
            simpleSmoothing = true;
        }
        populateMap();
    }
    public Color_Utils_Config getColor() {
        return color;
    }
    public void setColor(Color_Utils_Config color) {
        this.color = new Color_Utils_Config(color);
    }
    public double modulusForPhase(double phase) {
        for (Complex num : boundary_elements) {
            if (Math.abs(phase - num.arg()) <= tolerance) {
                return num.modulus();
            }
        }
        return Double.NaN;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
    }
    public void setTrap_point(Complex trap_point) {
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
    public void setStripe_density(int stripe_density) {
        this.stripe_density = stripe_density;
    }
    public void setMaxiter(long maxiter) {
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
     * @param nx:No.   of threads horizontally
     * @param ix:Index of thread horizontally
     * @param ny:No.   of threads vertically
     * @param iy:Index of thread vertically
     * @return the start and end coordinates for a particular thread's rendering region
     */
    protected int[] start_end_coordinates(int nx, int ix, int ny, int iy) {
        return start_end_coordinates(0, argand.getWidth(), 0, argand.getHeight(), nx, ix, ny, iy);
    }
    protected int[] start_end_coordinates(int startx, int endx, int starty, int endy, int nx, int ix, int ny, int iy) {
        //for multithreading purposes
        int start_x = startx, end_x, start_y = starty, end_y;
        int x_dist = Math.round((float) (endx - startx) / nx), y_dist = Math.round((float) (endy - starty) / ny);
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
    public double calculateBasePrecision() {
        return ((argand.getHeight() >= argand.getWidth()) ? argand.getWidth() / 2 : argand.getHeight() / 2);
    }
    public ImageData getArgand() {
        return argand;
    }
    public void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }
    public void generate() {
        if (params.runParams.fully_configured) {
            generate(params.runParams.start_x, params.runParams.end_x, params.runParams.start_y, params.runParams.end_y, (int) params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        } else {
            generate(params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        }
    }
    public void generate(long iterations, double escape_radius, Complex constant) {
        generate(0, argand.getWidth(), 0, argand.getHeight(), (int) iterations, escape_radius, constant);
    }
    public void generate(long iterations, double escape_radius) {
        generate(0, argand.getWidth(), 0, argand.getHeight(), (int) iterations, escape_radius, null);
    }
    public void generate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, Complex constant) {
        setMaxiter((end_x - start_x) * (end_y - start_y) * iterations);
        if (this.color.getMode() == Colors.CALCULATIONS.SIMPLE_SMOOTH_LINEAR || this.color.getMode() == Colors.CALCULATIONS.SIMPLE_SMOOTH_SPLINE) {
            this.color.setColor_density(this.color.getColor_density() * iterations);
        }
        if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
            histogram = new int[iterations + 2];
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
        if (!params.useThreadedGenerator() && (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE)) {
            double scaling = Math.pow(zoom, zoom_factor);
            int total = 0;
            for (int i = 0; i < iterations; i += 1) {
                total += histogram[i];
            }
            if (color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                System.arraycopy(MathUtils.rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
            }
            for (int i = start_y; i < end_y; i++) {
                for (int j = start_x; j < end_x; j++) {
                    int colortmp = 0;
                    double normalized_count = normalized_escapes[i][j];
                    int pi = i, pj = j - 1, ni = i, nj = j + 1;
                    if (pj < 0) {
                        pi = (i == 0) ? i : i - 1;
                        pj = escapedata[pi].length - 1;
                    }
                    if (nj >= escapedata[i].length) {
                        ni = (i == escapedata.length - 1) ? i : i + 1;
                        nj = 0;
                    }
                    int ep = escapedata[pi][pj], en = escapedata[ni][nj], e = escapedata[i][j];
                    if (color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                        if (color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR) {
                            int color1 = color.getColor(color.createIndex(((double) MathUtils.indexOf(histogram, ep)) / iterations, 0, 1, scaling)), color2 = color.getColor(color.createIndex(((double) MathUtils.indexOf(histogram, e)) / iterations, 0, 1, scaling)), color3 = color.getColor(color.createIndex(((double) MathUtils.indexOf(histogram, en)) / iterations, 0, 1, scaling));
                            int colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, normalized_count - (long) normalized_count, color.getByParts());
                            int colortmp2 = Color_Utils_Config.linearInterpolated(color2, color3, normalized_count - (long) normalized_count, color.getByParts());
                            if (color.isLogIndex()) {
                                colortmp = Color_Utils_Config.linearInterpolated(colortmp1, colortmp2, normalized_count - (long) normalized_count, color.getByParts());
                            } else {
                                colortmp = color2;
                            }
                        } else {
                            int idxp = color.createIndex(((double) MathUtils.indexOf(histogram, ep)) / iterations, 0, 1, scaling),
                                    idxn = color.createIndex(((double) MathUtils.indexOf(histogram, en)) / iterations, 0, 1, scaling), idxt = Math.min(idxp, idxn);
                            colortmp = color.splineInterpolated(color.createIndex(((double) MathUtils.indexOf(histogram, e)) / iterations, 0, 1, scaling), idxt, normalized_count - (long) normalized_count);
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
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                            int colortmp1 = Color_Utils_Config.linearInterpolated(color.getColor(color.createIndex(hue, 0, 1, scaling)), color.getColor(color.createIndex(hue2, 0, 1, scaling)), normalized_count - (long) normalized_count, color.getByParts());
                            int colortmp2 = Color_Utils_Config.linearInterpolated(color.getColor(color.createIndex(hue3, 0, 1, scaling)), color.getColor(color.createIndex(hue, 0, 1, scaling)), normalized_count - (long) normalized_count, color.getByParts());
                            colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, normalized_count - (long) normalized_count, color.getByParts());
                        } else {
                            int idxp = color.createIndex(hue3, 0, 1, scaling),
                                    idxn = color.createIndex(hue2, 0, 1, scaling), idxt = Math.min(idxp, idxn);
                            colortmp = color.splineInterpolated(color.createIndex(hue, 0, 1, scaling), idxt, normalized_count - (long) normalized_count);
                        }
                    }
                    argand.setPixel(i, j, colortmp);
                }
            }
        }
    }
    public void secantGenerate(int start_x, int end_x, int start_y, int end_y, int iterations) {
        FixedStack<Complex> last = new FixedStack<>(iterations + 2);
        FixedStack<Complex> lastd = new FixedStack<>(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        String functionderiv = "";
        Complex degree = null;
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode);
                func.setConsts(consts);
                function = func.toString();
                functionderiv = func.derivative(1);
                degree = func.getDegree();
            } else {
                Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(consts);
                poly.setVariableCode(variableCode);
                poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
                degree = poly.getDegree();
            }
        }
        degree = (degree == null) ? fe.getDegree(function) : degree;
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j], zd = new Complex(1), ztmp2 = new Complex(0), ztmpd2 = new Complex(0), z2 = new Complex(0);
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd = 0, ubnd = 0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                    mindist = 0;
                    maxdist = mindist;
                }
                while (c <= iterations) {
                    Complex ztmp, ztmpd = new Complex(zd);
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
                    fe.setZ_value(ztmp2.toString());
                    Complex b = fe.evaluate(function, false);
                    ztmp = ComplexOperations.subtract(z,
                            ComplexOperations.divide(
                                    ComplexOperations.multiply(a,
                                            ComplexOperations.subtract(z, ztmp2)),
                                    ComplexOperations.subtract(a, b)));
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        Complex e = fed.evaluate(functionderiv, false);
                        fed.setZ_value(ztmpd2.toString());
                        Complex d = fed.evaluate(functionderiv, false);
                        ztmpd = ComplexOperations.subtract(ztmpd, ComplexOperations.divide(ComplexOperations.multiply(e, ComplexOperations.subtract(ztmpd, ztmpd2)), ComplexOperations.subtract(e, d)));
                    }
                    fe.setZ_value(ztmp.toString());
                    if (simpleSmoothing) {
                        s += Math.exp(-(ztmp.modulus() + 0.5 / (ComplexOperations.subtract(z, ztmp).modulus())));
                    } else {
                        s += ComplexOperations.power(color.getSmoothing_base(),
                                ComplexOperations.add(ztmp,
                                        ComplexOperations.divide(
                                                new Complex(0.5),
                                                (ComplexOperations.subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = Math.abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= Math.sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR || color.mode == Colors.CALCULATIONS.EPSILON_CROSS_SPLINE) {
                        distance = Math.min(Math.abs(ztmp.real()), Math.abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_LINEAR || color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_SPLINE) {
                        long gx = Math.round(ztmp.real() * trap_point.modulus());
                        long gy = Math.round(ztmp.imaginary() * trap_point.modulus());
                        distance = Math.sqrt(Math.pow(gx - ztmp.real(), 2) + Math.pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * Math.sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR || color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        //Complex degree = ComplexOperations.divide(ztmp, z);
                        lbnd = Math.abs(ComplexOperations.power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = ComplexOperations.power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                    } else if (color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += Math.PI / 2;
                        } else {
                            mindist += ComplexOperations.divide(ComplexOperations.subtract(ztmp, ztmp2), ComplexOperations.subtract(ztmp2, z2)).arg();
                        }
                    } else {
                        distance = Math.sqrt(ComplexOperations.distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, false).modulus() <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {
                                roots.add(ztmp);
                            }
                        }
                        c = iterations;
                        break;
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {
                                roots.add(ztmp);
                            }
                        }
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    fe.setOldvalue(ztmp2.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
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
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;
                }
                if ((color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(0) : pass[m - 1];
                    }
                }
                pass[0] = new Complex(z);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = ComplexOperations.distance_squared(pass[2], pass[1]);
                double d1 = ComplexOperations.distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = c + (Math.log(tolerance) - Math.log(d0)) / (Math.log(d1) - Math.log(d0));
                }
                int colortmp = 0x0;
                switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN:
                        colortmp = getColor(i, j, c, pass, mindist, iterations);
                        break;
                    case ORBIT_TRAP_MAX:
                    case LINE_TRAP_MAX:
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
                    case CURVATURE_AVERAGE_LINEAR:
                    case CURVATURE_AVERAGE_SPLINE:
                    case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE:
                    case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE:
                        colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations);
                        break;
                    default:
                        colortmp = getColor(i, j, c, pass, maxModulus, iterations);
                }
                if (mode == Mode.SECANTBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    /**
     * NOTE:Call after generating the fractal, as this uses data from @code escapdedata
     */
    @Deprecated
    public Complex[] getBoundaryPoints(int depth) {
        ArrayList<Complex> points = new ArrayList<>(2 * argand.getWidth());
        for (int j = 0; j < argand_map[0].length; j++) {
            int imin = -1, imax = -1;
            for (int i = 0; i < argand_map.length; i++) {
                int itmp = -1;
                if (escapedata[i][j] == depth) {
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
        Complex[] boundaryPoints = new Complex[points.size()];
        points.toArray(boundaryPoints);
        return boundaryPoints;
    }
    private boolean isInBounds(Complex val) {
        if (val.imaginary() <= argand_map[0][center_x].imaginary() && val.imaginary() >= argand_map[argand_map.length - 1][center_x].imaginary()) {
            if (val.real() <= argand_map[center_y][argand_map[0].length - 1].real() && val.real() >= argand_map[center_y][0].real()) {
                return true;
            }
        }
        return false;
    }
    public Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(consts[0][1]);
            } else {
                lastConstant = new Complex(consts[getLastConstantIndex()][1]);
            }
        }
        return lastConstant;
    }
    public void setLastConstant(Complex value) {
        consts[getLastConstantIndex()][1] = value.toString();
        lastConstant = new Complex(value);
    }
    public int getLastConstantIndex() {
        String[] parts = StringManipulator.split(function, " ");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIdx(getConstantIndex(parts[i]));
                return lastConstantIdx;
            }
        }
        return -1;
    }
    public int getConstantIndex(String constant) {
        for (int i = 0; i < consts.length; i++) {
            if (consts[i][0].equals(constant)) {
                return i;
            }
        }
        return -1;
    }
    public void setLastConstantIdx(int lastConstantIdx) {
        this.lastConstantIdx = lastConstantIdx;
    }
    public void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack<Complex> last = new FixedStack<>(iterations + 2);
        FixedStack<Complex> lastd = new FixedStack<>(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode);
                func.setConsts(consts);
                function = func.toString();
                functionderiv = func.derivative(1);
            } else {
                Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(consts);
                poly.setVariableCode(variableCode);
                poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
            }
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd = 0, ubnd = 0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                Complex z = (mode == Mode.RUDY || mode == Mode.RUDYBROT) ? new Complex(argand_map[i][j]) : new Complex(0);
                Complex zd = new Complex(1), ztmp2 = new Complex(0), ztmpd2 = new Complex(0), z2 = new Complex(0);
                setLastConstant(argand_map[i][j]);
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                fe.setConstdec(consts);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                    fed.setConstdec(consts);
                }
                int c = 0;
                last.push(z);
                lastd.push(zd);
                boolean useJulia = false;
                while (c <= iterations && z.modulus() < escape_radius) {
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
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += Math.exp(-(ztmp.modulus() + 0.5 / (ComplexOperations.subtract(z, ztmp).modulus())));
                    } else {
                        s += ComplexOperations.power(color.getSmoothing_base(),
                                ComplexOperations.add(ztmp,
                                        ComplexOperations.divide(
                                                new Complex(0.5),
                                                (ComplexOperations.subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = Math.abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= Math.sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR || color.mode == Colors.CALCULATIONS.EPSILON_CROSS_SPLINE) {
                        distance = Math.min(Math.abs(ztmp.real()), Math.abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_LINEAR || color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_SPLINE) {
                        long gx = Math.round(ztmp.real() * trap_point.modulus());
                        long gy = Math.round(ztmp.imaginary() * trap_point.modulus());
                        distance = Math.sqrt(Math.pow(gx - ztmp.real(), 2) + Math.pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * Math.sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR || color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        Complex degree = ComplexOperations.divide(ztmp, z);
                        lbnd = Math.abs(ComplexOperations.power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = ComplexOperations.power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                    } else if (color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += Math.PI / 2;
                        } else {
                            mindist += ComplexOperations.divide(ComplexOperations.subtract(ztmp, ztmp2), ComplexOperations.subtract(ztmp2, z2)).arg();
                        }
                    } else {
                        distance = Math.sqrt(ComplexOperations.distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
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
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(0) : pass[m - 1];
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd);
                    pass[2] = argand_map[i][j];
                }
                escapedata[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
                }
                int colortmp = 0x0;
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
                    case CURVATURE_AVERAGE_LINEAR:
                    case CURVATURE_AVERAGE_SPLINE:
                    case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE:
                    case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE:
                        colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations);
                        break;
                    default:
                        colortmp = getColor(i, j, c, pass, escape_radius, iterations);
                }
                if (mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    public void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, Complex constant) {
        String functionderiv = "", functionderiv2 = "";
        Complex degree;
        if (Function.isSpecialFunction(function)) {
            Function func = Function.fromString(function, variableCode, oldvariablecode);
            func.setConsts(consts);
            function = func.toString();
            degree = func.getDegree();
            functionderiv = func.derivative(1);
            if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                functionderiv2 = func.derivative(2);
            }
        } else {
            Polynomial polynomial = Polynomial.fromString(function);
            polynomial.setConstdec(consts);
            polynomial.setVariableCode(variableCode);
            polynomial.setOldvariablecode(oldvariablecode);
            function = polynomial.toString();
            degree = polynomial.getDegree();
            functionderiv = polynomial.derivative().toString();
            if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                functionderiv2 = polynomial.derivative().derivative().toString();
            }
        }
        FixedStack<Complex> last = new FixedStack<>(iterations + 2);
        FixedStack<Complex> lastd = new FixedStack<>(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        if (constant != null && constant.equals(Complex.ZERO)) {
            constant = ComplexOperations.divide(Complex.ONE, degree);
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex toadd = Complex.ZERO;
        Complex lastConstantBackup = new Complex(getLastConstant());
        if (mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT) {
            toadd = new Complex(getLastConstant());
        }
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd = 0, ubnd = 0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                    mindist = 0;
                    maxdist = mindist;
                }
                boolean useJulia = false, useMandelbrot = false;
                Complex z = argand_map[i][j], zd = new Complex(1), ztmp2 = new Complex(0), ztmpd2 = new Complex(0), z2 = new Complex(0);
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
                    toadd = argand_map[i][j];
                    z = new Complex(0);
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                while (c <= iterations) {
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
                    Complex ztmp, ztmpd;
                    fe.setOldvalue(ztmp2.toString());
                    if (constant != null) {
                        ztmp = ComplexOperations.add(ComplexOperations.subtract(z, ComplexOperations.multiply(constant, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false)))), toadd);
                        ztmpd = null;
                        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                            ztmpd = ComplexOperations.add(ComplexOperations.subtract(zd, ComplexOperations.multiply(constant, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false)))), toadd);
                        }
                    } else {
                        ztmp = ComplexOperations.add(ComplexOperations.subtract(z, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false))), toadd);
                        ztmpd = null;
                        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                            ztmpd = ComplexOperations.add(ComplexOperations.subtract(zd, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false))), toadd);
                        }
                    }
                    fe.setZ_value(ztmp.toString());
                    if (simpleSmoothing) {
                        s += Math.exp(-(ztmp.modulus() + 0.5 / (ComplexOperations.subtract(z, ztmp).modulus())));
                    } else {
                        s += ComplexOperations.power(color.getSmoothing_base(),
                                ComplexOperations.add(ztmp,
                                        ComplexOperations.divide(
                                                new Complex(0.5),
                                                (ComplexOperations.subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = Math.abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= Math.sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR || color.mode == Colors.CALCULATIONS.EPSILON_CROSS_SPLINE) {
                        distance = Math.min(Math.abs(ztmp.real()), Math.abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_LINEAR || color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_SPLINE) {
                        long gx = Math.round(ztmp.real() * trap_point.modulus());
                        long gy = Math.round(ztmp.imaginary() * trap_point.modulus());
                        distance = Math.sqrt(Math.pow(gx - ztmp.real(), 2) + Math.pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * Math.sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR || color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        lbnd = Math.abs(ComplexOperations.power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = ComplexOperations.power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                    } else if (color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += Math.PI / 2;
                        } else {
                            mindist += ComplexOperations.divide(ComplexOperations.subtract(ztmp, ztmp2), ComplexOperations.subtract(ztmp2, z2)).arg();
                        }
                    } else {
                        distance = Math.sqrt(ComplexOperations.distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, false).modulus() <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {
                                roots.add(ztmp);
                            }
                        }
                        c = iterations;
                        break;
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {
                                roots.add(ztmp);
                            }
                        }
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
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
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                if ((color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(0) : pass[m - 1];
                    }
                }
                pass[0] = new Complex(z);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = ComplexOperations.distance_squared(pass[2], pass[1]);
                double d1 = ComplexOperations.distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = c + Math.abs((Math.log(tolerance) - Math.log(d0)) / (Math.log(d1) - Math.log(d0)));
                }
                int colortmp = 0x0;
                switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN:
                        colortmp = getColor(i, j, c, pass, mindist, iterations);
                        break;
                    case ORBIT_TRAP_MAX:
                    case LINE_TRAP_MAX:
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
                    case CURVATURE_AVERAGE_LINEAR:
                    case CURVATURE_AVERAGE_SPLINE:
                    case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE:
                    case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE:
                        colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations);
                        break;
                    default:
                        colortmp = getColor(i, j, c, pass, maxModulus, iterations);
                }
                if (mode == Mode.NEWTONBROT || mode == Mode.JULIA_NOVABROT || mode == Mode.MANDELBROT_NOVABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    public void publishProgress(long ctr, int i, int startx, int endx, int j, int starty, int endy) {
        if (params != null && (!params.useThreadedGenerator())) {
            float completion = ((float) ((i - starty) * (endx - startx) + (j - startx)) / ((endx - startx) * (endy - starty)));
            progressPublisher.publish(ctr + " iterations of " + maxiter + ",completion = " + (completion * 100.0f) + "%", completion);
        }
    }
    private int indexOfRoot(Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (ComplexOperations.distance_squared(roots.get(i), z) < tolerance) {
                return i;
            }
        }
        return -1;
    }
    private int closestRootIndex(Complex z) {
        int leastDistanceIdx = 0;
        double leastDistance = ComplexOperations.distance_squared(z, roots.get(0));
        for (int i = 1; i < roots.size(); i++) {
            double distance = ComplexOperations.distance_squared(z, roots.get(i));
            if (distance < leastDistance) {
                leastDistance = distance;
                leastDistanceIdx = i;
            }
        }
        return leastDistanceIdx;
    }
    public void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack<Complex> last = new FixedStack<>(iterations + 2);
        FixedStack<Complex> lastd = new FixedStack<>(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode);
                func.setConsts(consts);
                function = func.toString();
                functionderiv = func.derivative(1);
            } else {
                Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(consts);
                poly.setVariableCode(variableCode);
                poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
            }
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j], zd = new Complex(1), ztmp2 = new Complex(0), ztmpd2 = new Complex(0), z2 = new Complex(0);
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd = 0, ubnd = 0;
                int c = 0x0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.DOMAIN_COLORING_FAUX) {
                    mindist = 0;
                    maxdist = mindist;
                }
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                boolean useMandelBrot = false;
                while (c <= iterations && z.modulus() < escape_radius) {
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
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += Math.exp(-(ztmp.modulus() + 0.5 / (ComplexOperations.subtract(z, ztmp).modulus())));
                    } else {
                        s += ComplexOperations.power(color.getSmoothing_base(),
                                ComplexOperations.add(ztmp,
                                        ComplexOperations.divide(
                                                new Complex(0.5),
                                                (ComplexOperations.subtract(z, ztmp)))).negated()).modulus();
                    }
                    double distance = 0;
                    if (useLineTrap) {
                        distance = Math.abs(this.a * ztmp.real() + this.b * ztmp.imaginary() + this.c);
                        distance /= Math.sqrt(this.a * this.a + this.b * this.b);
                        mindist = (Math.min(distance, mindist));
                    } else if (color.mode == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR || color.mode == Colors.CALCULATIONS.EPSILON_CROSS_SPLINE) {
                        distance = Math.min(Math.abs(ztmp.real()), Math.abs(ztmp.imaginary()));
                        mindist = Math.min(distance, mindist);
                    } else if (color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_LINEAR || color.mode == Colors.CALCULATIONS.GAUSSIAN_INT_DISTANCE_SPLINE) {
                        long gx = Math.round(ztmp.real() * trap_point.modulus());
                        long gy = Math.round(ztmp.imaginary() * trap_point.modulus());
                        distance = Math.sqrt(Math.pow(gx - ztmp.real(), 2) + Math.pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE) {
                        mindist += 0.5 * Math.sin(ztmp.arg() * stripe_density) + 0.5;
                    } else if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR || color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE) {
                        Complex degree = ComplexOperations.divide(ztmp, z);
                        lbnd = Math.abs(ComplexOperations.power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = ComplexOperations.power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);
                    } else if (color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR || color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += Math.PI / 2;
                        } else {
                            mindist += ComplexOperations.divide(ComplexOperations.subtract(ztmp, ztmp2), ComplexOperations.subtract(ztmp2, z2)).arg();
                        }
                    } else {
                        distance = Math.sqrt(ComplexOperations.distance_squared(ztmp, trap_point));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
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
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                    histogram[c]++;
                }
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(0) : pass[m - 1];
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
                }
                int colortmp = 0x0;
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
                    case CURVATURE_AVERAGE_LINEAR:
                    case CURVATURE_AVERAGE_SPLINE:
                    case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE:
                    case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE:
                        colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations);
                        break;
                    default:
                        colortmp = getColor(i, j, c, pass, escape_radius, iterations);
                }
                if (mode == Mode.JULIABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {
                    argand.setPixel(i, j, colortmp);
                }
                last.clear();
                lastd.clear();
            }
        }
    }
    public double getNormalized(int val, int iterations, Complex[] z_values, double escape) {
        Complex z = z_values[0]; /*double degree = this.degree.modulus(); if (escape < zoom) {
            degree = Math.log(z.modulus() * z.modulus()) / Math.log(z_values[1].modulus() * z_values[1].modulus());
        }*/
        double renormalized, degree = Math.log(z.modulus() * z.modulus()) / Math.log(z_values[1].modulus() * z_values[1].modulus());
        if (!color.isLogIndex() || (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT)) {
            if (degree == 0 || degree == 1) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = val - (Math.log(Math.log(z.modulus() / Math.log(escape))) / Math.log(degree));
                if (renormalized - (long) renormalized == 0) {
                    renormalized += ((double) val / iterations);
                }
            }
        } else {
            renormalized = val + (0.5 + 0.5 * (Math.sin(z.arg()) * stripe_density));
        }
        return renormalized;
    }
    public int[] getHistogram() {
        return histogram;
    }
    public double[][] getNormalized_escapes() {
        return normalized_escapes;
    }
    public int getColor(int i, int j, int val, Complex[] last, double escape_radius, int iterations) {
        int colortmp, colortmp1, colortmp2, color1, color2, color3, index;
        double renormalized, lbnd = 0, ubnd = 1, calc, scaling = Math.pow(zoom, zoom_factor), smoothcount;
        renormalized = normalized_escapes[i][j];
        smoothcount = renormalized;
        if ((!(color.isExponentialSmoothing() || mode == Mode.NEWTON || mode == Mode.NEWTONBROT || mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT || mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT)) && color.isLogIndex()) {
            smoothcount = (renormalized > 0) ? Math.abs(Math.log(renormalized)) : ComplexOperations.principallog(new Complex(renormalized, 0)).modulus();
        }
        switch (color.getMode()) {
            case SIMPLE:
                colortmp = color.getColor(color.createIndex(val, 0, iterations, scaling));
                break;
            case SIMPLE_SMOOTH_LINEAR:
                colortmp = getInterpolated(color.createIndex(val, 0, iterations, scaling), smoothcount);
                break;
            case SIMPLE_SMOOTH_SPLINE:
                colortmp = color.splineInterpolated(color.createIndex(val, 0, iterations, scaling), smoothcount);
                break;
            case COLOR_DIVIDE_DIRECT:
                val = (val == 0) ? iterations + 1 : (val - 1 == 0) ? iterations + 2 : val;
                color1 = (0xffffff / val);
                color2 = (0xffffff / (val + 1));
                color3 = (0xffffff / (val - 1));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
                break;
            case COLOR_DIVIDE_NORMALIZED:
                color1 = (int) (0xffffff / renormalized);
                color2 = (int) (0xffffff / (renormalized + 1));
                color3 = (int) (0xffffff / (renormalized - 1));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
                break;
            case COLOR_GRAYSCALE_HIGH_CONTRAST:
                colortmp = Color_Utils_Config.toGray(val * iterations);
                break;
            case SIMPLE_DISTANCE_ESTIMATION:
                calc = Math.abs((double) val / iterations);
                if (calc > 1) {
                    calc = calc - 1;
                }
                colortmp1 = (int) (calc * 255);
                if (calc > 0.5) {
                    colortmp = Color_Utils_Config.toRGB(colortmp1, 255, colortmp1);
                } else {
                    colortmp = Color_Utils_Config.toRGB(0, colortmp1, 0);
                }
                break;
            case COLOR_MULTIPLY_DIRECT:
                color1 = Color_Utils_Config.toGray(val);
                color2 = Color_Utils_Config.toGray(val + 1);
                color3 = Color_Utils_Config.toGray(Math.abs((val - 1)));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
                break;
            case COLOR_MULTIPLY_NORMALIZED:
                color1 = Color_Utils_Config.toGray((int) Math.abs(renormalized));
                color2 = Color_Utils_Config.toGray((int) Math.abs(renormalized + 1));
                color3 = Color_Utils_Config.toGray((int) Math.abs(renormalized - 1));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
                break;
            case COLOR_GRAYSCALE_LOW_CONTRAST:
                colortmp = Color_Utils_Config.toGray(val);
                break;
            case DISTANCE_ESTIMATION_GRAYSCALE:
            case DISTANCE_ESTIMATION_COLOR:
                double distance;
                if ((mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT)) {
                    distance = Math.abs(Math.sqrt(Math.pow(last[0].modulus(), 2) / Math.pow(last[1].modulus(), 2)) * 0.5 * Math.log(Math.pow(last[0].modulus(), 2)));
                } else {
                    distance = Math.abs(last[0].modulus() * Math.log(last[0].modulus())) / Math.log(last[1].modulus());
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE) {
                    color1 = (int) Math.abs((distance - (long) distance) * 255);
                    if (color1 > 255) color1 %= 255;
                    colortmp = Color_Utils_Config.toRGB(color1, color1, color1);
                } else {
                    index = color.createIndex((distance - (long) distance), lbnd, ubnd, scaling);
                    colortmp = color.splineInterpolated(index, distance - (long) distance);
                /*if (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT) {
                    if (!color.getByParts()) {colortmp = (distance > escape_radius) ? 0xffffff : 0x000000;} else {
                        colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(last[0], last[2]))) ? 0xffffff : 0x000000;
                    }} else {
                    colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(centre_offset, last[2]))) ? 0xffffff : 0x000000;}*/
                }
                break;
            case COLOR_HISTOGRAM:
            case COLOR_HISTOGRAM_LINEAR:
            case RANK_ORDER_LINEAR:
            case RANK_ORDER_SPLINE:
                colortmp = 0x000000;
                break;//Don't need to deal with this here, it's post-calculated
            case COLOR_NEWTON_STRIPES:
                color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) val / iterations));
                color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.num_colors, ((double) (val + 1) / iterations));
                color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) Math.abs(val - 1) / iterations));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
                break;
            case COLOR_NEWTON_NORMALIZED:
                color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) val / iterations));
                color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) (val + 1) / iterations));
                color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) Math.abs(val - 1) / iterations));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, val, iterations, color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, val, iterations, color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, val, iterations, color.getByParts());
                break;
            case COLOR_NEWTON_CLASSIC:
                color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) val / iterations));
                color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) (val + 1) / iterations));
                color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) Math.abs(val - 1) / iterations));
                colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, val, iterations, color.getByParts());
                colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, val, iterations, color.getByParts());
                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, val, iterations, color.getByParts());
                break;
            case CURVATURE_AVERAGE_LINEAR:
            case CURVATURE_AVERAGE_SPLINE:
                lbnd = -Math.PI;
                ubnd = Math.PI;
                index = color.createIndex(escape_radius, lbnd, ubnd, scaling);
                if (color.getMode() == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR) {
                    colortmp = getInterpolated(index, smoothcount);
                } else {
                    colortmp = color.splineInterpolated(index, smoothcount - ((long) smoothcount));
                }
                break;
            case STRIPE_AVERAGE_LINEAR:
            case STRIPE_AVERAGE_SPLINE:
                //min value of 0.5*sin(x)+0.5, min value of sin(x)=-1,max value of 0.5*sin(x)+0.5, max value of sin(x)=1
                index = color.createIndex(escape_radius, lbnd, ubnd, scaling);
                if (color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR) {
                    colortmp = getInterpolated(index, smoothcount);
                } else {
                    colortmp = color.splineInterpolated(index, smoothcount - ((long) smoothcount));
                }
                break;
            case TRIANGLE_AREA_INEQUALITY_LINEAR:
                colortmp = getInterpolated(color.createIndex(escape_radius, lbnd, ubnd, scaling), smoothcount);
                break;
            case TRIANGLE_AREA_INEQUALITY_SPLINE:
                colortmp = color.splineInterpolated(color.createIndex(escape_radius, lbnd, ubnd, scaling), smoothcount - ((long) smoothcount));
                break;
            case EPSILON_CROSS_LINEAR:
            case EPSILON_CROSS_SPLINE:
                /*if(Double.isNaN(escape_radius)){calc=-1;}
                else if(escape_radius<0){
                    calc=Math.abs(a+c*(-escape_radius)/Math.abs(trap_point.imaginary()));}else{
                    calc=Math.abs(a+c*(-escape_radius)/Math.abs(trap_point.real()));}
                if(calc==-1){colortmp=color.getColor(color.createIndex(val,0,iterations,scaling));}else {
                    calc = (calc > 1) ? calc - (long) calc : calc;
                    index = color.createIndex(calc, lbnd, ubnd, scaling);
                    if(color.getMode()== Colors.CALCULATIONS.EPSILON_CROSS_LINEAR){
                        colortmp=getColor(index,smoothcount);}else{
                        colortmp=color.splineInterpolated(index,smoothcount-(long)smoothcount);}}*/
                if (escape_radius < trap_point.modulus() * base_precision && val > 0) {
                    smoothcount = Math.abs(val - escape_radius);
                } else {
                    smoothcount = Math.log(1 + escape_radius);
                }
                if (color.getMode() == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR) {
                    colortmp = getInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount);
                } else {
                    colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount);
                }
                break;
            case GAUSSIAN_INT_DISTANCE_LINEAR:
                colortmp = getInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount);
                break;
            case GAUSSIAN_INT_DISTANCE_SPLINE:
                colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount);
                break;
            case ORBIT_TRAP_AVG:
            case ORBIT_TRAP_MAX:
            case ORBIT_TRAP_MIN:
            case LINE_TRAP_MIN:
            case LINE_TRAP_MAX:
            case LINE_TRAP_AVG:
                colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount);
                break;
            case DOMAIN_COLORING:
                colortmp = new HSL(HSL.hueFromAngle(last[0].arg() + Math.PI), last[0].modulus() / (2 * modulusForPhase(last[0].arg())), Math.min(Math.abs(last[0].imaginary()), Math.abs(last[0].real())) / Math.max(Math.abs(last[0].imaginary()), Math.abs(last[0].real()))).toRGB();
                break;
            case DOMAIN_COLORING_FAUX:
                colortmp = new HSL(HSL.hueFromAngle(last[0].arg() + Math.PI), last[0].modulus() / (2 * escape_radius), Math.min(Math.abs(last[0].imaginary()), Math.abs(last[0].real())) / Math.max(Math.abs(last[0].imaginary()), Math.abs(last[0].real()))).toRGB();
                break;
            default:
                throw new IllegalArgumentException("invalid argument");
        }
        return colortmp;
    }
    private int getInterpolated(int index, double smoothcount) {
        int color1, color2, color3, colortmp1, colortmp2, colortmp,
                index2 = MathUtils.boundsProtected(index + 1, color.getNum_colors());
        color1 = color.getColor(index);
        color2 = color.getColor(index2);
        color3 = MathUtils.boundsProtected(index - 1, color.getNum_colors());
        colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.getByParts());
        colortmp2 = Color_Utils_Config.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.getByParts());
        colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.getByParts());
        return colortmp;
    }
    public int[] toCooordinates(Complex point) {
        point = ComplexOperations.subtract(point, centre_offset);
        if (Math.abs(params.initParams.skew) >= tolerance) {
            Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew).inverse();
            point = MathUtils.matrixToComplex(MatrixOperations.multiply(rotor, MathUtils.complexToMatrix(point)));
        }
        int x = (int) ((point.real() * scale) + center_x), y = (int) (center_y - (point.imaginary() * scale));
        x = MathUtils.boundsProtected(x, argand.getWidth());
        y = MathUtils.boundsProtected(y, argand.getHeight());
        return new int[]{x, y};
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {
            zoom(zoom.centre_x, zoom.centre_y, zoom.level);
        } else {
            zoom(zoom.centre, zoom.level);
        }
    }
    public void mandelbrotToJulia(Matrix constant, double level) {
        zoom(constant, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void resetCentre() {
        setCenter_x(argand.getWidth() / 2);
        setCenter_y(argand.getHeight() / 2);
        resetCentre_Offset();
    }
    @Override
    public int getWidth() {
        return getArgand().getWidth();
    }
    @Override
    public int getHeight() {
        return getArgand().getHeight();
    }
    public void resetCentre_Offset() {
        centre_offset = new Complex(0);
    }
    public void setCenter_x(int center_x) {
        this.center_x = center_x;
    }
    public void setCenter_y(int center_y) {
        this.center_y = center_y;
    }
    private void changeMode(Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) ? Mode.JULIABROT : ((mode == Mode.MANDELBROT || mode == Mode.RUDY) ? Mode.JULIA : mode));
    }
    public void zoom(Matrix centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(centre_offset, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(centre_offset, level));
        }
        zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level);
    }
    public void zoom(Complex centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(MathUtils.complexToMatrix(centre_offset), level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(MathUtils.complexToMatrix(centre_offset), level));
        }
        setCentre_offset(centre_offset);
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
        //setCenter_x(toCooordinates(centre_offset)[0]);setCenter_y(toCooordinates(centre_offset)[1]);
        populateMap();
    }
    public void populateMap() {
        for (int i = 0; i < argand.getHeight(); i++) {
            for (int j = 0; j < argand.getWidth(); j++) {
                argand_map[i][j] = fromCooordinates(j, i);
            }
        }
        if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
            boundary_elements = new Complex[2 * (argand.getHeight() + argand.getWidth() - 2)];
            int j = 0;
            for (int i = 0; i < argand.getHeight(); i++, ++j) {
                boundary_elements[j] = argand_map[i][0];
                boundary_elements[++j] = argand_map[i][argand_map[i].length - 1];
            }
            for (int i = 1; i < argand.getWidth() - 1; i++, ++j) {
                boundary_elements[j] = argand_map[0][i];
                boundary_elements[++j] = argand_map[argand_map.length - 1][i];
            }
        }
    }
    public Complex fromCooordinates(int x, int y) {
        x = MathUtils.boundsProtected(x, argand.getWidth());
        y = MathUtils.boundsProtected(y, argand.getHeight());
        Complex point = new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale));
        if (Math.abs(params.initParams.skew) > tolerance) {
            Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew);
            point = MathUtils.matrixToComplex(MatrixOperations.multiply(rotor, MathUtils.complexToMatrix(point)));
        }
        return ComplexOperations.add(centre_offset, point);
    }
    public void setCentre_offset(Complex centre_offset) {
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
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(cx, cy, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(cx, cy, level));
        }
        cx = MathUtils.boundsProtected(cx, argand.getWidth());
        cy = MathUtils.boundsProtected(cy, argand.getHeight());
        //setCenter_x(cx);setCenter_y(cy);
        setCentre_offset(fromCooordinates(cx, cy));
        setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
        populateMap();
    }
    public void mandelbrotToJulia(Complex constant, double level) {
        zoom(constant, level);
        changeMode(centre_offset);
        resetCentre();
    }
    public void mandelbrotToJulia(ZoomParams zoom) {
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
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(center_x + x_dist, center_y + y_dist, zoom_factor);
        int histogram_length = histogram.length;
        histogram = new int[histogram_length];
        int[][] tmp_escapes = new int[escapedata.length][escapedata[0].length];
        double[][] tmp_normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        ImageData tmp_argand = new LinearizedImageData(argand);
        for (int i = 0; i < tmp_escapes.length && i < tmp_normalized_escapes.length; i++) {
            System.arraycopy(escapedata[i], 0, tmp_escapes[i], 0, tmp_escapes[i].length);
            System.arraycopy(normalized_escapes[i], 0, tmp_normalized_escapes[i], 0, tmp_normalized_escapes[i].length);
        }
        argand = new ImageData(tmp_argand.getWidth(), tmp_argand.getHeight());
        escapedata = new int[tmp_escapes.length][tmp_escapes[0].length];
        normalized_escapes = new double[tmp_normalized_escapes.length][tmp_normalized_escapes[0].length];
        if (y_dist < 0) {
            for (int i = 0, j = y_dist; i < argand.getHeight() - y_dist && j < argand.getHeight(); i++, j++) {
                rangedCopyHelper(i, j, x_dist, tmp_escapes, tmp_normalized_escapes, tmp_argand);
            }
        } else {
            for (int i = (-y_dist), j = 0; i < argand.getHeight() && j < argand.getHeight() + y_dist; i++, j++) {
                rangedCopyHelper(i, j, x_dist, tmp_escapes, tmp_normalized_escapes, tmp_argand);
            }
        }
    }
    private void rangedCopyHelper(int i, int j, int x_dist, int[][] tmp_escapes, double[][] tmp_normalized_escapes, ImageData tmp_argand) {
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