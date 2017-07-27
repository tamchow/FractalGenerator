package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.ColorData;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.PixelFractalGenerator;
import in.tamchow.fractal.graphics.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphics.containers.PixelContainer;
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

import static in.tamchow.fractal.color.Colors.BaseColors.*;
import static in.tamchow.fractal.color.Colors.MODE.*;
import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.helpers.strings.StringManipulator.correctPadding;
import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
import static in.tamchow.fractal.math.complex.ComplexOperations.*;
import static java.lang.Double.*;
import static java.lang.Math.*;
import static java.lang.Math.log;
import static java.lang.Math.sin;
/**
 * The actual fractal plotter for Julia, Newton, Nova (both Mandelbrot and Julia variants),Secant and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes in {@link in.tamchow.fractal.fractals.complex.complexbrot.ComplexBrotFractalGenerator}
 * Various (21+) Coloring modes
 */
public final class ComplexFractalGenerator extends PixelFractalGenerator {
    private static final String asciiArtBase =
            "~`+-*/#@!%^&(){}[];'|:?><.,_=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static final ArrayList<Complex> roots = new ArrayList<>();
    private static Complex[][] argandMap;
    private static int valCount = 0;
    private static double values = 0;
    private static Complex[] boundaryElements;
    protected ColorData color;
    protected int[] histogram;
    protected Publisher progressPublisher;
    protected ComplexFractalParams params;
    protected double zoom, base_precision, scale, tolerance, a, b, c;
    int[][] orbitEscapeData;
    double[][] normalizedEscapes, miscellaneous;
    private PixelContainer argand;
    private int centerX, centerY, lastConstantIndex, stripeDensity, switchRate;
    private Mode mode;
    private long maxIterations;
    private String[][] constants;
    private Complex distanceEstimateMultiplier, centreOffset, lastConstant, trapPoint;
    private boolean mandelbrotToJulia, juliaToMandelbrot, useLineTrap, silencer, simpleSmoothing, newtonTinting, nonPercentileBasedRankOrder;
    private String variableCode, oldVariableCode, function;
    private int colorIfMore = WHITE, colorIfLess = BLACK;
    public ComplexFractalGenerator(@NotNull ComplexFractalParams params, Publisher progressPublisher) {
        this.params = params;
        initFractal(params);
        doZooms(params.zoomConfig);
        setProgressPublisher(progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double base_precision, Mode mode, String function, @NotNull String[][] constants, String variableCode, String oldVariableCode, double tolerance, @NotNull ColorData color, Publisher progressPublisher) {
        //initFractal(width, height, zoom, zoom_factor, basePrecision, mode, function, constants, variableCode, oldVariableCode, tolerance, new Complex(-1, 0), color, 0, Complex._0, null);
        //this.progressPublisher = progressPublisher;
        //ComplexFractalParams params=new ComplexFractalParams();
        //params.initParams=new ComplexFractalInitParams(width,height,zoom,zoom_factor,basePrecision,mode,function,constants,variableCode,oldVariableCode,tolerance,color,0,Complex._0,null,0);
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, base_precision, mode, function, constants, variableCode, oldVariableCode, tolerance, color, 0, Complex.ZERO, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double base_precision, Mode mode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, Publisher progressPublisher, int switchRate, @NotNull Complex trapPoint) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, base_precision, mode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, trapPoint, null, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double base_precision, Mode mode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, Publisher progressPublisher, int switchRate, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, base_precision, mode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, Complex.ZERO, linetrap, 0), null), progressPublisher);
    }
    @Deprecated
    public ComplexFractalGenerator(int width, int height, double zoom, double base_precision, Mode mode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, Publisher progressPublisher, int switchRate, @NotNull Complex trapPoint, String linetrap) {
        this(new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, base_precision, mode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, trapPoint, linetrap, 0), null), progressPublisher);
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
    public static Complex[] getBoundaryElements() {
        return boundaryElements;
    }
    public boolean isNonPercentileBasedRankOrder() {
        return nonPercentileBasedRankOrder;
    }
    public ArrayList<Complex> getRoots() {
        return roots;
    }
    public PixelContainer getPlane() {
        return getArgand();
    }
    private void initFractal(@NotNull ComplexFractalParams params) {
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.basePrecision, params.initParams.fractalMode, params.initParams.function, params.initParams.constants, params.initParams.variableCode, params.initParams.oldVariableCode, params.initParams.tolerance, params.initParams.color, params.initParams.switchRate, params.initParams.trapPoint, params.initParams.lineTrap);
    }
    private void initFractal(int width, int height, double zoom, double base_precision, Mode mode, String function, @NotNull String[][] consts, String variableCode, String oldvariablecode, double tolerance, @NotNull ColorData color, int switch_rate, @NotNull Complex trap_point, @Nullable String linetrap) {
        silencer = params.useThreadedGenerator();
        argand = new LinearizedPixelContainer(width, height);
        setMode(mode);
        setMaxIterations(getImageHeight() * argand.getWidth());
        orbitEscapeData = new int[getImageHeight()][getImageWidth()];
        normalizedEscapes = new double[getImageHeight()][getImageWidth()];
        miscellaneous = new double[getImageHeight()][getImageWidth()];
        setVariableCode(variableCode);
        setZoom(zoom);
        setFunction(function);
        setBase_precision(base_precision);
        setConstants(consts);
        setScale(this.base_precision * this.zoom);
        resetCentre();
        setOldVariableCode(oldvariablecode);
        setTolerance(tolerance);
        setColor(color);
        lastConstant = new Complex(-1, 0);
        if ((this.color.getMode() == STRIPE_AVERAGE) || ((!this.color.isExponentialSmoothing()) &&
                (this.color.isLogIndex() && (!isAnyOf(mode, Mode.BUDDHABROT, Mode.MANDELBROT, Mode.RUDY, Mode.RUDYBROT))))) {
            setStripeDensity(this.color.getColorDensity());
            this.color.setColorDensity(-1);
        } else {
            setStripeDensity(-1);
        }
        mandelbrotToJulia = false;
        juliaToMandelbrot = false;
        if (!(switch_rate == 0 || switch_rate == -1 || switch_rate == 1)) {
            if (switch_rate < 0) {
                juliaToMandelbrot = true;
                this.switchRate = -switch_rate;
            } else {
                mandelbrotToJulia = true;
                this.switchRate = switch_rate;
            }
        }
        setTrapPoint(trap_point);
        useLineTrap = false;
        if (linetrap != null) {
            @NotNull String[] parts = split(linetrap, ",");
            a = Double.parseDouble(parts[0]);
            b = Double.parseDouble(parts[1]);
            c = Double.parseDouble(parts[2]);
            useLineTrap = true;
        }
        if (color.getSmoothingBase().equals(Complex.E)) {
            simpleSmoothing = true;
        }
        if (color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW && color.getPalette().length == 2) {
            colorIfMore = color.getPalette()[0];
            colorIfLess = color.getPalette()[1];
        }
        if (isAnyOf(color.getMode(), NEWTON_CLASSIC, NEWTON_NORMALIZED_MODULUS) && (color.getByParts() < 0)) {
            newtonTinting = true;
            color.setByParts(0);
        }
        if (color.getMode() == RANK_ORDER && (color.getByParts() < 0)) {
            nonPercentileBasedRankOrder = true;
            color.setByParts(0);
        }
        if (argandMap == null) {
            argandMap = new Complex[getImageHeight()][getImageWidth()];
            populateMap();
        }
    }
    public ColorData getColor() {
        return color;
    }
    public void setColor(@NotNull ColorData color) {
        this.color = new ColorData(color);
    }
    private synchronized double modulusForPhase(double phase) {
        for (@NotNull Complex num : boundaryElements) {
            if (abs(phase - num.arg()) <= tolerance) {
                return num.modulus();
            }
        }
        return NaN;
    }
    public void setOldVariableCode(String oldVariableCode) {
        this.oldVariableCode = oldVariableCode;
    }
    public void setTrapPoint(@NotNull Complex trapPoint) {
        this.trapPoint = new Complex(trapPoint);
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
    private void setStripeDensity(int stripeDensity) {
        this.stripeDensity = stripeDensity;
    }
    private void setMaxIterations(long maxIterations) {
        this.maxIterations = maxIterations;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public int[][] getOrbitEscapeData() {
        return orbitEscapeData;
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
    public PixelContainer getArgand() {
        return argand;
    }
    public void setConstants(@NotNull String[][] constants) {
        this.constants = new String[constants.length][constants[0].length];
        for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, constants[i].length);
        }
    }
    public void generate() {
        if (params.runParams.fullyConfigured) {
            generate(params.runParams.startX, params.runParams.endX, params.runParams.startY, params.runParams.endY, params.runParams.iterations, params.runParams.escapeRadius, params.runParams.constant);
        } else {
            generate(params.runParams.iterations, params.runParams.escapeRadius, params.runParams.constant);
        }
    }
    public void generate(int iterations, double escapeRadius, Complex constant) {
        generate(0, getImageWidth(), 0, getImageHeight(), iterations, escapeRadius, constant);
    }
    public void generate(int iterations, double escapeRadius) {
        generate(iterations, escapeRadius, null);
    }
    public void generate(int startX, int endX, int startY, int endY, int iterations, double escapeRadius, Complex constant) {
        setMaxIterations((endX - startX) * (endY - startY) * iterations);
        if (this.color.getMode() == SIMPLE_SMOOTH) {
            this.color.setColorDensity(this.color.getColorDensity() * iterations);
        }
        if (color.getMode() == HISTOGRAM || color.getMode() == RANK_ORDER) {
            histogram = new int[iterations + 1];
        }
        switch (mode) {
            case MANDELBROT:
            case RUDY:
            case RUDYBROT:
            case BUDDHABROT:
                mandelbrotGenerate(startX, endX, startY, endY, iterations, escapeRadius);
                break;
            case JULIA:
            case JULIABROT:
                juliaGenerate(startX, endX, startY, endY, iterations, escapeRadius);
                break;
            case NEWTON:
            case NEWTONBROT:
            case JULIA_NOVA:
            case JULIA_NOVABROT:
            case MANDELBROT_NOVA:
            case MANDELBROT_NOVABROT:
                newtonGenerate(startX, endX, startY, endY, iterations, constant);
                break;
            case SECANT:
            case SECANTBROT:
                secantGenerate(startX, endX, startY, endY, iterations);
                break;
            default:
                throw new IllegalArgumentException("Unknown fractal render mode");
        }
        if (!params.useThreadedGenerator() && (color.getMode() == HISTOGRAM || color.getMode() == RANK_ORDER)) {
            //double scaling = basePrecision * pow(zoom, zoom_factor);
            int total = 0;
            for (int i = 0; i < iterations; i += 1) {
                total += histogram[i];
            }
            if (color.getMode() == RANK_ORDER) {
                System.arraycopy(rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
            }
            for (int i = startY; i < endY; i++) {
                for (int j = startX; j < endX; j++) {
                    int colorTmp, pi = i, pj = j - 1, ni = i, nj = j + 1;
                    double normalized_count = normalizedEscapes[i][j];
                    if (pj < 0) {
                        pi = (i == 0) ? i : i - 1;
                        pj = orbitEscapeData[pi].length - 1;
                    }
                    if (nj >= orbitEscapeData[i].length) {
                        ni = (i == orbitEscapeData.length - 1) ? i : i + 1;
                        nj = 0;
                    }
                    int ep = orbitEscapeData[pi][pj], en = orbitEscapeData[ni][nj], e = orbitEscapeData[i][j];
                    if (color.getMode() == RANK_ORDER) {
                        int idxp = color.createIndex(percentileOf(ep, histogram), 0, 1);
                        int idx = color.createIndex(percentileOf(e, histogram), 0, 1);
                        int idxn = color.createIndex(percentileOf(en, histogram), 0, 1);
                        if (isNonPercentileBasedRankOrder()) {
                            idxp = color.createIndex(((double) indexOf(histogram, ep)) / iterations, 0, 1);
                            idx = color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1);
                            idxn = color.createIndex(((double) indexOf(histogram, en)) / iterations, 0, 1);
                        }
                        int idxMin = (idxp < idxn) ? idxp : idxn, idxMax = (idxp > idxn) ? idxp : idxn;
                        switch (color.getInterpolationType()) {
                            case LINEAR:
                                colorTmp = color.interpolated(idxp, idx, idxn, normalized_count - (long) normalized_count);
                                break;
                            case CATMULL_ROM_SPLINE: {
                                if (color.isModifierEnabled()) {
                                    colorTmp = color.interpolated(idx, idxMin, idxMax, normalized_count - (long) normalized_count);
                                } else {
                                    if (color.isLogIndex()) {
                                        colorTmp = color.interpolated(idxMin, idx, normalized_count - (long) normalized_count);
                                    } else {
                                        colorTmp = color.interpolated(idx, idxMax, normalized_count - (long) normalized_count);
                                    }
                                }
                            }
                            break;
                            default:
                                //TODO: Implement properly
                                colorTmp = Integer.MIN_VALUE;
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
                        switch (color.getInterpolationType()) {
                            case LINEAR:
                                colorTmp = color.interpolated(color.createIndex(hue2, 0, 1), color.createIndex(hue, 0, 1), color.createIndex(hue3, 0, 1), normalized_count - (long) normalized_count);
                                break;
                            case CATMULL_ROM_SPLINE:
                                int idxp = color.createIndex(hue3, 0, 1), idxn = color.createIndex(hue2, 0, 1);
                                if (color.isModifierEnabled()) {
                                    colorTmp = color.interpolated(color.createIndex(hue, 0, 1), Math.max(idxp, idxn), normalized_count - (long) normalized_count);
                                } else {
                                    colorTmp = color.interpolated(Math.min(idxp, idxn), color.createIndex(hue, 0, 1), normalized_count - (long) normalized_count);
                                }
                                break;
                            default:
                                //TODO: Implement properly
                                colorTmp = Integer.MIN_VALUE;
                        }
                    }
                    argand.setPixel(i, j, colorTmp);
                }
            }
        }
        if (isAnyOf(color.getMode(), CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
            colorizeWRTDistanceOrAngle();
        }
    }
    public double[][] getMiscellaneous() {
        return miscellaneous;
    }
    void colorizeWRTDistanceOrAngle() {
        double maxValue = 0;
        for (int i = 0; i < getImageHeight(); ++i) {
            for (int j = 0; j < getImageWidth(); ++j) {
                maxValue = miscellaneous[i][j] > maxValue ? miscellaneous[i][j] : maxValue;
            }
        }
        for (int i = 0; i < getImageHeight(); ++i) {
            for (int j = 0; j < getImageWidth(); ++j) {
                argand.setPixel(i, j, color.interpolated(color.getColor(
                        color.createIndex(abs(miscellaneous[i][j] / maxValue), 0, 1)), normalizedEscapes[i][j]));
            }
        }
    }
    private void addRoot(Complex zTmp) {
        if (isAnyOf(color.getMode(), NEWTON_CLASSIC, NEWTON_NORMALIZED_MODULUS)) {
            synchronized (roots) {
                if (indexOfRoot(zTmp) == -1) {
                    roots.add(zTmp);
                }
            }
        }
    }
    private void secantGenerate(int start_x, int end_x, int start_y, int end_y, int iterations) {
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        @NotNull String functionderiv = "";
        @Nullable Complex degree = null;
        if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
            @NotNull Function func = new Function(null, variableCode, oldVariableCode, constants).fromString(function);
            function = func.toString();
            functionderiv = func.firstDerivative();
            degree = func.getDegree();
            /*if (Function.isSpecialFunction(function)) {
                @NotNull Function func = Function.fromString(function, variableCode, oldVariableCode, constants);
                function = func.toString();
                functionderiv = func.derivative(1);
                degree = func.getDegree();
            } else {
                @NotNull Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(constants);
                poly.setVariableCode(variableCode);
                poly.setOldVariableCode(oldVariableCode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
                degree = poly.getDegree();
            }*/
        }
        degree = (degree == null) ? fe.getDegree(function) : degree;
        distanceEstimateMultiplier = degree;
        roots.ensureCapacity(round((float) degree.modulus()));
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        function = correctPadding(function, FunctionEvaluator.OPERATIONS);
        functionderiv = correctPadding(functionderiv, FunctionEvaluator.OPERATIONS);
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                @NotNull Complex z = argandMap[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO, zold = Complex.ZERO;
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                }
                last.push(z);
                lastd.push(zd);
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd, ubnd;
                if (isAnyOf(color.getMode(), TRIANGLE_AREA_INEQUALITY, STRIPE_AVERAGE, CURVATURE_AVERAGE_NOABS,
                        CURVATURE_AVERAGE_ABS, DOMAIN_COLORING_FAUX, CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
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
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        Complex e = fed.evaluate(functionderiv, false);
                        fed.setZ_value(ztmpd2.toString());
                        Complex d = fed.evaluate(functionderiv, false);
                        ztmpd = subtract(ztmpd, divide(multiply(e, subtract(ztmpd, ztmpd2)), subtract(e, d)));
                    }
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothingBase(),
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
                    } else if (color.getMode() == EPSILON_CROSS) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == CUMULATIVE_ANGLE) {
                        mindist += (ztmp.arg() - z.arg());
                    } else if (color.getMode() == CUMULATIVE_DISTANCE) {
                        mindist += (ztmp.modulus() - z.modulus());
                    } else if (color.getMode() == CUMULATIVE_ANGLE) {
                        mindist += (ztmp.arg() - z.arg());
                    } else if (color.getMode() == CUMULATIVE_DISTANCE) {
                        mindist += (ztmp.modulus() - z.modulus());
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE) {
                        /*long gx = round(ztmp.real() * trapPoint.modulus());
                        long gy = round(ztmp.imaginary() * trapPoint.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trapPoint.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripeDensity) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argandMap[i][j];
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
                    } else if (isAnyOf(color.getMode(), CURVATURE_AVERAGE_ABS, CURVATURE_AVERAGE_NOABS)) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trapPoint));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, ztmp).modulus() <= tolerance || distance(z, ztmp) <= tolerance) {
                        addRoot(ztmp);
                        //c = iterations;
                        break;
                    }
                    zold = z;
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    fe.setOldvalue(zold.toString());
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxIterations) {
                        break outer;
                    }
                    ctr++;
                    maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (isAnyOf(color.getMode(), HISTOGRAM, RANK_ORDER)) {
                    histogram[c]++;
                }
                if (isAnyOf(color.getMode(), NEWTON_CLASSIC, NEWTON_NORMALIZED_MODULUS) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = divide(principallog(argandMap[i][j]), principallog(z)).modulus();
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
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centreOffset);
                }
                orbitEscapeData[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = distance_squared(pass[2], pass[1]);
                double d1 = distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalizedEscapes[i][j] = s;
                } else {
                    normalizedEscapes[i][j] = c + (log(tolerance) - log(d0)) / (log(d1) - log(d0));
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
     * NOTE:Call after generating the fractal, as this uses data from {@link #orbitEscapeData}
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
                if (getOrbitEscapeData()[i][j] == depth) {
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
            points.add(argandMap[imin][j]);
            points.add(argandMap[imax][j]);
        }
        @NotNull Complex[] boundaryPoints = new Complex[points.size()];
        points.toArray(boundaryPoints);
        return boundaryPoints;
    }
    private boolean isInBounds(@NotNull Complex val) {
        if (val.imaginary() <= argandMap[0][centerX].imaginary() && val.imaginary() >= argandMap[getImageHeight() - 1][centerX].imaginary()) {
            if (val.real() <= argandMap[centerY][getImageWidth() - 1].real() && val.real() >= argandMap[centerY][0].real()) {
                return true;
            }
        }
        return false;
    }
    private Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(constants[0][1]);
            } else {
                lastConstant = new Complex(constants[getLastConstantIndex()][1]);
            }
        }
        return lastConstant;
    }
    private void setLastConstant(@NotNull Complex value) {
        int constantIndex = getLastConstantIndex();
        if(constantIndex >= 0) {
            constants[constantIndex][1] = value.toString();
        }
        lastConstant = new Complex(value);
    }
    private int getLastConstantIndex() {
        @NotNull String[] parts = split(function, " ");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIndex(getConstantIndex(parts[i]));
                return lastConstantIndex;
            }
        }
        return -1;
    }
    private int getConstantIndex(String constant) {
        for (int i = 0; i < constants.length; i++) {
            if (constants[i][0].equals(constant)) {
                return i;
            }
        }
        return -1;
    }
    private String checkDE() {
        String functionderiv = "";
        if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
            @NotNull Function func = new Function(null, variableCode, oldVariableCode, constants).fromString(function);
            function = func.toString();
            functionderiv = func.firstDerivative();
            distanceEstimateMultiplier = func.getDegree();
            /*if (Function.isSpecialFunction(function)) {
                @NotNull Function func = Function.fromString(function, variableCode, oldVariableCode, constants);
                function = func.toString();
                functionderiv = func.derivative(1);
                distanceEstimateMultiplier = func.getDegree();
            } else {
                @NotNull Polynomial poly = Polynomial.fromString(function);
                poly.setConstdec(constants);
                poly.setVariableCode(variableCode);
                poly.setOldVariableCode(oldVariableCode);
                function = poly.toString();
                functionderiv = poly.derivative().toString();
                distanceEstimateMultiplier = poly.getDegree();
            }*/
        }
        return functionderiv;
    }
    private void setLastConstantIndex(int lastConstantIndex) {
        this.lastConstantIndex = lastConstantIndex;
    }
    private void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        double bailout = escape_radius * escape_radius + tolerance;
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        @NotNull String functionderiv = checkDE();
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        function = correctPadding(function, FunctionEvaluator.OPERATIONS);
        functionderiv = correctPadding(functionderiv, FunctionEvaluator.OPERATIONS);
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd, ubnd;
                if (isAnyOf(color.getMode(), TRIANGLE_AREA_INEQUALITY, STRIPE_AVERAGE, CURVATURE_AVERAGE_NOABS,
                        CURVATURE_AVERAGE_ABS, DOMAIN_COLORING_FAUX, CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
                    mindist = 0;
                    maxdist = mindist;
                }
                @NotNull Complex z = (mode == Mode.RUDY || mode == Mode.RUDYBROT) ? new Complex(argandMap[i][j]) : Complex.ZERO;
                Complex zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                setLastConstant(argandMap[i][j]);
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                fe.setConstdec(constants);
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    fed.setZ_value(zd.toString());
                    fed.setOldvalue(ztmpd2.toString());
                    fed.setConstdec(constants);
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
                        if (c % switchRate == 0) {
                            useJulia = (!useJulia);
                        }
                        if (useJulia) {
                            setLastConstant(lastConstantBackup);
                            fe.setConstdec(constants);
                            fed.setConstdec(constants);
                        } else {
                            setLastConstant(argandMap[i][j]);
                            fe.setConstdec(constants);
                            fed.setConstdec(constants);
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
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothingBase(),
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
                    } else if (color.getMode() == EPSILON_CROSS) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == CUMULATIVE_ANGLE) {
                        mindist += (ztmp.arg() - z.arg());
                    } else if (color.getMode() == CUMULATIVE_DISTANCE) {
                        mindist += (ztmp.modulus() - z.modulus());
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE) {
                        /*long gx = round(ztmp.real() * trapPoint.modulus());
                        long gy = round(ztmp.imaginary() * trapPoint.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trapPoint.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripeDensity) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argandMap[i][j];
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
                    } else if (isAnyOf(color.getMode(), CURVATURE_AVERAGE_ABS, CURVATURE_AVERAGE_NOABS)) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trapPoint));
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
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxIterations) {
                        break outer;
                    }
                    ctr++;
                }
                if (isAnyOf(color.getMode(), HISTOGRAM, RANK_ORDER)) {
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
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    pass[1] = new Complex(zd);
                    pass[2] = argandMap[i][j];
                }
                orbitEscapeData[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalizedEscapes[i][j] = s;
                } else {
                    normalizedEscapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
                }
                int colortmp = getColorTmp(iterations, i, j, escape_radius, mindist, maxdist, c, pass);
                if (isAnyOf(mode, Mode.BUDDHABROT, Mode.RUDYBROT)) {
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
        @NotNull Function func = new Function(null, variableCode, oldVariableCode, constants).fromString(function);
        function = func.toString();
        degree = func.getDegree();
        functionderiv = func.firstDerivative();
        if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
            functionderiv2 = func.secondDerivative();
        }
        /*if (Function.isSpecialFunction(function)) {
            @NotNull Function func = Function.fromString(function, variableCode, oldVariableCode, constants);
            function = func.toString();
            degree = func.getDegree();
            functionderiv = func.derivative(1);
            if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                functionderiv2 = func.derivative(2);
            }
        } else {
            @NotNull Polynomial polynomial = Polynomial.fromString(function);
            polynomial.setConstdec(constants);
            polynomial.setVariableCode(variableCode);
            polynomial.setOldVariableCode(oldVariableCode);
            function = polynomial.toString();
            degree = polynomial.getDegree();
            functionderiv = polynomial.derivative().toString();
            if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == DISTANCE_ESTIMATION_COLOR || color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                functionderiv2 = polynomial.derivative().derivative().toString();
            }
        }*/
        distanceEstimateMultiplier = degree;
        roots.ensureCapacity(round((float) degree.modulus()));
        @NotNull Stack<Complex> last = new FixedStack<>(iterations + 1);
        @NotNull Stack<Complex> lastd = new FixedStack<>(iterations + 1);
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        if (constant != null && constant.equals(Complex.ZERO)) {
            constant = divide(Complex.ONE, degree);
        }
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex toadd = Complex.ZERO;
        @NotNull Complex lastConstantBackup = new Complex(getLastConstant());
        if (isAnyOf(mode, Mode.JULIA_NOVA, Mode.JULIA_NOVABROT)) {
            toadd = new Complex(getLastConstant());
        }
        function = correctPadding(function, FunctionEvaluator.OPERATIONS);
        functionderiv = correctPadding(functionderiv, FunctionEvaluator.OPERATIONS);
        functionderiv2 = correctPadding(functionderiv2, FunctionEvaluator.OPERATIONS);
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd, ubnd;
                if (isAnyOf(color.getMode(), TRIANGLE_AREA_INEQUALITY, STRIPE_AVERAGE, CURVATURE_AVERAGE_NOABS,
                        CURVATURE_AVERAGE_ABS, DOMAIN_COLORING_FAUX, CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
                    mindist = 0;
                    maxdist = mindist;
                }
                boolean useJulia = false, useMandelbrot = false;
                @NotNull Complex z = argandMap[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                int c = 0;
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (isAnyOf(mode, Mode.MANDELBROT_NOVA, Mode.MANDELBROT_NOVABROT)) {
                    toadd = argandMap[i][j];
                    z = Complex.ZERO;
                }
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
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
                    if (isAnyOf(mode, Mode.MANDELBROT_NOVA, Mode.MANDELBROT_NOVABROT)) {
                        if (mandelbrotToJulia) {
                            if (c % switchRate == 0) {
                                useJulia = (!useJulia);
                            }
                            if (useJulia) {
                                toadd = lastConstantBackup;
                            } else {
                                toadd = argandMap[i][j];
                            }
                        }
                    }
                    if (isAnyOf(mode, Mode.JULIA_NOVA, Mode.JULIA_NOVABROT)) {
                        if (juliaToMandelbrot) {
                            if (c % switchRate == 0) {
                                useMandelbrot = (!useMandelbrot);
                            }
                            if (useMandelbrot) {
                                toadd = argandMap[i][j];
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
                        if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                            ztmpd = add(subtract(zd, multiply(constant, divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false)))), toadd);
                        }
                    } else {
                        ztmp = add(subtract(z, divide(fe.evaluate(function, false),
                                fe.evaluate(functionderiv, false))), toadd);
                        ztmpd = null;
                        if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                            ztmpd = add(subtract(zd, divide(fed.evaluate(functionderiv, false),
                                    fed.evaluate(functionderiv2, false))), toadd);
                        }
                    }
                    fe.setZ_value(ztmp.toString());
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothingBase(),
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
                    } else if (color.getMode() == EPSILON_CROSS) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == CUMULATIVE_ANGLE) {
                        mindist += (ztmp.arg() - z.arg());
                    } else if (color.getMode() == CUMULATIVE_DISTANCE) {
                        mindist += (ztmp.modulus() - z.modulus());
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE) {
                        /*long gx = round(ztmp.real() * trapPoint.modulus());
                        long gy = round(ztmp.imaginary() * trapPoint.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trapPoint.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripeDensity) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argandMap[i][j];
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
                    } else if (color.getMode() == CURVATURE_AVERAGE_NOABS || color.getMode() == CURVATURE_AVERAGE_ABS) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trapPoint));
                        mindist = (Math.min(distance, mindist));
                    }
                    maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, ztmp).modulus() <= tolerance || distance(z, ztmp) <= tolerance) {
                        addRoot(ztmp);
                        //c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxIterations) {
                        break outer;
                    }
                    ctr++;
                    maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (isAnyOf(color.getMode(), HISTOGRAM, RANK_ORDER)) {
                    histogram[c]++;
                }
                if (isAnyOf(color.getMode(), NEWTON_CLASSIC, NEWTON_NORMALIZED_MODULUS) && roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                //double root_reached = divide(principallog(argandMap[i][j]), principallog(z)).modulus();
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
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centreOffset);
                }
                orbitEscapeData[i][j] = c;
                //Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = distance_squared(pass[2], pass[1]);
                double d1 = distance_squared(pass[1], pass[0]);
                if (color.isExponentialSmoothing()) {
                    normalizedEscapes[i][j] = s;
                } else {
                    normalizedEscapes[i][j] = c + abs((log(tolerance) - log(d0)) / (log(d1) - log(d0)));
                }
                int colortmp = getColorTmp(iterations, i, j, maxModulus, mindist, maxdist, c, pass);
                if (isAnyOf(mode, Mode.NEWTONBROT, Mode.MANDELBROT_NOVABROT, Mode.JULIA_NOVABROT)) {
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
            case CUMULATIVE_ANGLE:
            case CUMULATIVE_DISTANCE:
                miscellaneous[i][j] = mindist;
            case EPSILON_CROSS:
            case GAUSSIAN_INT_DISTANCE:
                colortmp = getColor(i, j, c, pass, mindist, iterations);
                break;
            case CURVATURE_AVERAGE_NOABS:
            case CURVATURE_AVERAGE_ABS:
            case STRIPE_AVERAGE:
            case TRIANGLE_AREA_INEQUALITY:
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
            progressPublisher.publish(ctr + " iterations of " + maxIterations + ",completion = " + (completion * 100.0f) + "%", completion,
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
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        @NotNull String functionderiv = checkDE();
        @NotNull FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, constants, oldVariableCode);
        long ctr = 0;
        Complex lastConstantBackup = getLastConstant();
        function = correctPadding(function, FunctionEvaluator.OPERATIONS);
        functionderiv = correctPadding(functionderiv, FunctionEvaluator.OPERATIONS);
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argandMap[i][j], zd = Complex.ONE, ztmp2 = Complex.ZERO, ztmpd2 = Complex.ZERO, z2 = Complex.ZERO;
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd, ubnd;
                int c = 0x0;
                if (isAnyOf(color.getMode(), TRIANGLE_AREA_INEQUALITY, STRIPE_AVERAGE, CURVATURE_AVERAGE_NOABS,
                        CURVATURE_AVERAGE_ABS, DOMAIN_COLORING_FAUX, CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
                    mindist = 0;
                    maxdist = mindist;
                }
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2.toString());
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
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
                        if (c % switchRate == 0) {
                            useMandelBrot = (!useMandelBrot);
                        }
                        if (useMandelBrot) {
                            setLastConstant(argandMap[i][j]);
                            fe.setConstdec(constants);
                            fed.setConstdec(constants);
                        } else {
                            setLastConstant(lastConstantBackup);
                            fe.setConstdec(constants);
                            fed.setConstdec(constants);
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
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (simpleSmoothing) {
                        s += exp(-(ztmp.modulus() + 0.5 / (subtract(z, ztmp).modulus())));
                    } else {
                        s += power(color.getSmoothingBase(),
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
                    } else if (color.getMode() == EPSILON_CROSS) {
                        distance = Math.min(abs(ztmp.real()), abs(ztmp.imaginary()));
                        mindist = (Math.min(distance, mindist));
                    } else if (color.getMode() == CUMULATIVE_ANGLE) {
                        mindist += (ztmp.arg() - z.arg());
                    } else if (color.getMode() == CUMULATIVE_DISTANCE) {
                        mindist += (ztmp.modulus() - z.modulus());
                    } else if (color.getMode() == GAUSSIAN_INT_DISTANCE) {
                        /*long gx = round(ztmp.real() * trapPoint.modulus());
                        long gy = round(ztmp.imaginary() * trapPoint.modulus());
                        distance = sqrt(pow(gx - ztmp.real(), 2) + pow(gy - ztmp.imaginary(), 2));
                        mindist = (Math.min(distance, mindist));*/
                        long trap_factor = round(trapPoint.modulus());
                        double gint_x = round(ztmp.real() * trap_factor) / trap_factor,
                                gint_y = round(ztmp.imaginary() * trap_factor) / trap_factor,
                                x = ztmp.real(), y = ztmp.imaginary(), x_n = x - gint_x, y_n = y - gint_y;
                        distance = sqrt(x_n * x_n + y_n * y_n);
                        distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                        mindist = (Math.min(distance, mindist));
                    }
                    if (color.getMode() == STRIPE_AVERAGE) {
                        mindist += 0.5 * sin(ztmp.arg() * stripeDensity) + 0.5;
                    } else if (color.getMode() == TRIANGLE_AREA_INEQUALITY) {
                        /*@NotNull Complex degree = divide(ztmp, z);
                        lbnd = abs(power(z, degree).modulus() - getLastConstant().modulus());
                        ubnd = power(z, degree).modulus() + getLastConstant().modulus();
                        mindist += (ztmp.modulus() - lbnd) / (ubnd - lbnd);*/
                        Complex adjust = getLastConstant();
                        /*if(mode==Mode.MANDELBROT||mode==Mode.BUDDHABROT||
                                mode==Mode.MANDELBROT_NOVA||mode==Mode.MANDELBROT_NOVABROT){
                            adjust=argandMap[i][j];
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
                    } else if (isAnyOf(color.getMode(), CURVATURE_AVERAGE_ABS, CURVATURE_AVERAGE_NOABS)) {
                        if (ztmp2.equals(Complex.ZERO) && z2.equals(Complex.ZERO)) {
                            mindist += PI / 2;
                        } else {
                            mindist += divide(subtract(ztmp, ztmp2), subtract(ztmp2, z2)).arg();
                            if (color.getMode() == CURVATURE_AVERAGE_ABS) {
                                mindist = abs(mindist);
                            }
                        }
                    } else {
                        distance = sqrt(distance_squared(ztmp, trapPoint));
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
                    if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                        fed.setZ_value(zd.toString());
                        lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd);
                        fed.setOldvalue(ztmpd2.toString());
                    }
                    publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxIterations) {
                        break outer;
                    }
                    ctr++;
                }
                if (isAnyOf(color.getMode(), HISTOGRAM, RANK_ORDER)) {
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
                if (isAnyOf(color.getMode(), DISTANCE_ESTIMATION_2C_OR_BW, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE)) {
                    pass[1] = new Complex(zd);
                    pass[2] = new Complex(centreOffset);
                }
                orbitEscapeData[i][j] = c;
                if (color.isExponentialSmoothing()) {
                    normalizedEscapes[i][j] = s;
                } else {
                    normalizedEscapes[i][j] = getNormalized(c, iterations, pass, escape_radius);
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
        Complex z = z_values[0];
        /*double degree = this.degree.modulus(); if (escape < zoom) {
            degree = log(z.modulus() * z.modulus()) / log(z_values[1].modulus() * z_values[1].modulus());
        }*/
        double renormalized, degree = abs(log(z.modulus() * z.modulus()) / log(z_values[1].modulus() * z_values[1].modulus()));
        if (!color.isLogIndex() || (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT)) {
            if (degree == 0 || degree == 1) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = val + 1 + (log(log(abs(escape)) / log(z.modulus())) / log(degree));
                // renormalized = val - (log(log(z.modulus() / log(escape))) / log(degree));
                /*if (renormalized - (long) renormalized == 0) {
                    renormalized += ((double) val / iterations);
                }*/
            }
        } else {
            renormalized = val + (0.5 + 0.5 * (sin(z.arg()) * stripeDensity));
        }
        return color.getFractionalCount(val, (isNaN(renormalized) ? 0 : (isInfinite(renormalized) ? 1 : renormalized)) / iterations);
    }
    public int[] getHistogram() {
        return histogram;
    }
    public double[][] getNormalizedEscapes() {
        return normalizedEscapes;
    }
    public double averageIterations() {
        return values / valCount;
    }
    public int getColor(int i, int j, int val, Complex[] last, double realData, int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Illegal maximum iteration count : " + iterations);
        }
        int colorTmp, colorTmp1, color1, color2, color3, index = 0, density = color.getColorDensity();
        double reNormalized, lowerBound = 0.0, upperBound = 1.0, calc, scaling = base_precision * zoom, smoothcount;
        reNormalized = normalizedEscapes[i][j];
        smoothcount = reNormalized;
        if ((!(color.isExponentialSmoothing() ||
                mode == Mode.NEWTON || mode == Mode.NEWTONBROT ||
                mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT ||
                mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT)) &&
                color.isLogIndex()) {
            smoothcount = (reNormalized > 0) ? abs(log(reNormalized)) : principallog(new Complex(reNormalized, 0)).modulus();
            smoothcount = (isNaN(smoothcount) ? 0 : (isInfinite(smoothcount) ? 1 : smoothcount));
            normalizedEscapes[i][j] = (isNaN(reNormalized) ? 0 : (isInfinite(reNormalized) ? 1 : reNormalized));
        } else {
            normalizedEscapes[i][j] = (isNaN(smoothcount) ? 0 : (isInfinite(smoothcount) ? 1 : smoothcount));
        }
        realData = (isNaN(realData) ? 0 : (isInfinite(realData) ? 1 : realData));
        for (int c = 0; c < last.length; ++c) {
            last[c] = (isNaN(last[c].cabs()) ? Complex.ZERO : (isInfinite(last[c].cabs()) ? Complex.ONE : last[c]));
        }
        int nextVal = (val >= iterations) ? iterations : val, previousVal = (val == 0) ? 0 : val - 1;
        double interpolation = smoothcount - (long) smoothcount;
        values += val;
        valCount++;
        switch (color.getMode()) {
            case SIMPLE:
                colorTmp = color.getColor(color.createIndex(val, 0, iterations));
                break;
            case SIMPLE_SMOOTH:
                colorTmp = color.interpolated(color.createIndex(val, 0, iterations), interpolation);
                break;
            case DIVIDE:
                val = (val == 0) ? iterations + 1 : (val - 1 == 0) ? iterations + 1 : val;
                color1 = (0xffffff / val);
                color2 = (0xffffff / (val + 1));
                color3 = (0xffffff / (val - 1));
                colorTmp = color.interpolated(color1, color2, color3, interpolation);
                break;
            case DIVIDE_NORMALIZED:
                color1 = (int) (0xffffff / reNormalized);
                color2 = (int) (0xffffff / (reNormalized + 1));
                color3 = (int) (0xffffff / (reNormalized - 1));
                colorTmp = color.interpolated(color1, color2, color3, interpolation);
                break;
            case GRAYSCALE_HIGH_CONTRAST:
                colorTmp = ColorData.toGray(val * iterations);
                break;
            case SIMPLE_DISTANCE_ESTIMATION:
                calc = abs((double) val / iterations);
                if (calc > 1) {
                    calc = calc - 1;
                }
                colorTmp1 = (int) (calc * 255);
                if (calc > 0.5) {
                    colorTmp = ColorData.toRGB(colorTmp1, 255, colorTmp1);
                } else {
                    colorTmp = ColorData.toRGB(0, colorTmp1, 0);
                }
                break;
            case MULTIPLY:
                color1 = ColorData.toGray(val);
                color2 = ColorData.toGray(val + 1);
                color3 = ColorData.toGray(abs((val - 1)));
                colorTmp = color.interpolated(color1, color2, color3, interpolation);
                break;
            case MULTIPLY_NORMALIZED:
                color1 = ColorData.toGray((int) abs(reNormalized));
                color2 = ColorData.toGray((int) abs(reNormalized + 1));
                color3 = ColorData.toGray((int) abs(reNormalized - 1));
                colorTmp = color.interpolated(color1, color2, color3, interpolation);
                break;
            case GRAYSCALE_LOW_CONTRAST:
                colorTmp = ColorData.toGray(val);
                break;
            case DISTANCE_ESTIMATION_GRAYSCALE:
            case DISTANCE_ESTIMATION_COLOR:
            case DISTANCE_ESTIMATION_2C_OR_BW:
                double distance;
                if ((mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT)) {
                    distance = abs(distanceEstimateMultiplier.modulus() *
                            (sqrt(last[0].cabs() / last[1].cabs()) * 0.5 * log(last[0].cabs())));
                } else {
                    distance = abs(distanceEstimateMultiplier.modulus() *
                            (last[0].modulus() * log(last[0].modulus())) / log(last[1].modulus()));
                }
                distance = isNaN(distance) ? 0 : (isInfinite(distance) ? 1 : distance);
                if (color.getMode() == DISTANCE_ESTIMATION_GRAYSCALE) {
                    colorTmp = ColorData.toGray(boundsProtected((int) abs((distance - (long) distance) * 255), 256));
                } else if (color.getMode() == DISTANCE_ESTIMATION_2C_OR_BW) {
                    if ((!color.isModifierEnabled()) && (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.JULIA || mode == Mode.JULIABROT)) {
                        /*if (!color.isModifierEnabled()) {*/
                        colorTmp = (distance > realData) ? colorIfMore : colorIfLess;
                        /*} else {
                            colorTmp = (distance > sqrt(distance_squared(last[0], last[2]))) ?
                                    colorIfMore : colorIfLess;
                        }*/
                    } else {
                        colorTmp = (distance > sqrt(distance_squared(centreOffset, last[0]))) ? colorIfMore : colorIfLess;
                    }
                } else {
                    index = color.createIndex((distance - (long) distance), lowerBound, upperBound);
                    colorTmp = color.interpolated(index, interpolation);
                }
                break;
            case HISTOGRAM:
            case RANK_ORDER:
            case CUMULATIVE_ANGLE:
            case CUMULATIVE_DISTANCE:
            case ASCII_ART_NUMERIC:
            case ASCII_ART_CHARACTER:
                colorTmp = 0xff000000;//Don't need to deal with this here, it's post-calculated
                break;
            case NEWTON_NORMALIZED_MODULUS:
                if (color.isLogIndex()) {
                    density = Math.round((float) realData);
                } else {
                    density *= Math.round((float) val / iterations);
                }
            case NEWTON_CLASSIC:
                index = color.createIndexSimple(closestRootIndex(last[0]), 0.0, roots.size(), density);
                int preTintColor = color.getColor(index);
                if (newtonTinting) {
                    color1 = color.getTint(preTintColor, ((double) val / iterations));
                    color2 = color.getTint(preTintColor, ((double) nextVal / iterations));
                    color3 = color.getTint(preTintColor, ((double) previousVal / iterations));
                } else {
                    color1 = color.getShade(preTintColor, ((double) val / iterations));
                    color2 = color.getShade(preTintColor, ((double) nextVal / iterations));
                    color3 = color.getShade(preTintColor, ((double) previousVal / iterations));
                }
                colorTmp = color.interpolatedColor(color1, color2, color3, interpolation);
                break;
            case CURVATURE_AVERAGE_NOABS:
            case CURVATURE_AVERAGE_ABS:
                if (color.getMode() == CURVATURE_AVERAGE_ABS) {
                    lowerBound = 0;
                } else {
                    lowerBound = -PI;
                }
                upperBound = PI;
                index = color.createIndex(realData, lowerBound, upperBound);
                colorTmp = color.interpolated(index, interpolation);
                break;
            case STRIPE_AVERAGE:
                //min value of 0.5*sin(x)+0.5=0, min value of sin(x)=-1,max value of 0.5*sin(x)+0.5=1, max value of sin(x)=1
            case TRIANGLE_AREA_INEQUALITY:
                colorTmp = color.interpolated(color.createIndex(realData, lowerBound, upperBound), interpolation);
                break;
            case EPSILON_CROSS:
                realData = abs(realData);
                lowerBound = 0;
                upperBound = abs(argandMap[0][0].modulus() - trapPoint.modulus());
                /*if (isNaN(realData)) {
                    calc = -1;
                } else if (realData < 0) {
                    calc = abs(a + c * (-realData) / abs(trapPoint.imaginary()));
                } else {
                    calc = abs(a + c * (-realData) / abs(trapPoint.real()));
                }
                if (calc == -1) {
                    colorTmp = color.getColor(color.createIndex(val, 0, iterations));
                } else {
                    calc = (calc > 1) ? calc - (long) calc : calc;
                    index = color.createIndex(calc, lowerBound, upperBound);
                    colorTmp = color.interpolated(index, smoothcount - (long) smoothcount);
                }*/
                if (scaling <= color.getNumColors()) {
                    if (realData < trapPoint.modulus() * base_precision) {
                        smoothcount = abs(val - realData);
                    } else {
                        smoothcount = log(1 + realData);
                    }
                    realData = realData - (long) realData;
                } else {
                    smoothcount = principallog(new Complex(realData / upperBound)).modulus();
                    smoothcount = isNaN(smoothcount) ? 0.0 : (isInfinite(smoothcount) ? 1.0 : smoothcount);
                }
                interpolation = smoothcount - (long) smoothcount;
                colorTmp = color.interpolated(color.createIndex(realData, lowerBound, upperBound), interpolation);
                break;
            case GAUSSIAN_INT_DISTANCE:
                lowerBound = 0;
                upperBound = argandMap[0][0].modulus() - centreOffset.modulus();
                colorTmp = color.interpolated(color.createIndex(abs(realData), lowerBound, upperBound), interpolation);
                break;
            case ORBIT_TRAP_AVG:
            case ORBIT_TRAP_MAX:
            case ORBIT_TRAP_MIN:
            case LINE_TRAP_MIN:
            case LINE_TRAP_MAX:
            case LINE_TRAP_AVG:
                lowerBound = 0;
                double origin_modulus = (trapPoint == null) ? centreOffset.modulus() : trapPoint.modulus();
                upperBound = abs(argandMap[0][0].modulus() - origin_modulus);
                index = color.createIndex(abs(realData), lowerBound, upperBound);
                colorTmp = color.interpolated(index, interpolation);
                break;
            case DOMAIN_COLORING:
                colorTmp = new HSL(
                        HSL.hueFromAngle(last[0].arg() + PI),
                        last[0].modulus() / (2 * modulusForPhase(last[0].arg())),
                        (((double) val) / iterations)
                        /*Math.min(abs(last[0].imaginary()),
                                abs(last[0].real())) / Math.max(abs(last[0].imaginary()),
                                abs(last[0].real()))*/).toRGB();
                break;
            case DOMAIN_COLORING_FAUX:
                colorTmp = new HSL(
                        HSL.hueFromAngle(last[0].arg() + PI),
                        last[0].modulus() / (2 * realData),
                        (((double) val) / iterations)
                        /*Math.min(abs(last[0].imaginary()),
                                abs(last[0].real())) / Math.max(abs(last[0].imaginary()),
                                abs(last[0].real()))*/).toRGB();
                break;
            default:
                throw new IllegalArgumentException("invalid argument");
        }
        return colorTmp;
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
            for (int[] anEscapedata : orbitEscapeData) {
                for (int anAnEscapedata : anEscapedata) {
                    buffer.append(lookup[boundsProtected(anAnEscapedata, lookup.length)]);
                }
                buffer.append('\n');
            }
        } else {
            for (int[] anEscapedata : orbitEscapeData) {
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
            point = matrixToComplex(doRotate(complexToMatrix(point), complexToMatrix(centreOffset), -params.initParams.skew));
        }
        point = subtract(point, centreOffset);
        return new int[]{boundsProtected(round((float) (point.real() * scale) + centerX), getImageWidth()),
                boundsProtected(round(centerY - (float) (point.imaginary() * scale)), getImageHeight())};
    }
    @Override
    public void zoom(@NotNull ZoomParams zoom) {
        if (zoom.centre == null && zoom.bounds == null) {
            zoom(zoom.centre_x, zoom.centre_y, zoom.level, false, true);
        } else if (zoom.bounds == null) {
            zoom(zoom.centre, zoom.level, false, true);
        } else {
            zoom(zoom.bounds, false, true);
        }
    }
    public void zoom(Matrix bounds) {
        zoom(bounds, true, true);
    }
    public void zoom(Matrix bounds, boolean write, boolean additive) {
        if (write) {
            params.zoomConfig.addZoom(new ZoomParams(bounds));
        }
        double xs = bounds.get(0, 0), ys = bounds.get(0, 1), xe = bounds.get(1, 0), ye = bounds.get(1, 1);
        double xr = Math.abs(xe - xs), yr = Math.abs(ye - ys);
        @NotNull Matrix topLeftCurrent = complexToMatrix(fromCoordinates(0, 0)),
                bottomRightCurrent = complexToMatrix(fromCoordinates(getImageWidth() - 1, getImageHeight() - 1));
        double xsc = topLeftCurrent.get(0, 0), ysc = topLeftCurrent.get(1, 0),
                xec = bottomRightCurrent.get(0, 0), yec = bottomRightCurrent.get(1, 0);
        double xrc = Math.abs(xec - xsc), yrc = Math.abs(yec - ysc);
        double xscale = xrc / xr, yscale = yrc / yr;
        setScale(scale * Math.min(xscale, yscale));
        setCentreOffset(new Complex(xs + (xr / 2), ys + (yr / 2)));
        double newzoom = scale / base_precision;
        setZoom((additive) ? newzoom : newzoom / zoom);
        populateMap();
    }
    public void mandelbrotToJulia(@NotNull Matrix constant, double level) {
        zoom(constant, level);
        changeMode(centreOffset);
        resetCentre();
    }
    public void resetCentre() {
        setCenterX(getImageWidth() / 2);
        setCenterY(getImageHeight() / 2);
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
        centreOffset = Complex.ZERO;
    }
    private void setCenterX(int centerX) {
        this.centerX = centerX;
    }
    private void setCenterY(int centerY) {
        this.centerY = centerY;
    }
    private void changeMode(@NotNull Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) ? Mode.JULIABROT : ((mode == Mode.MANDELBROT || mode == Mode.RUDY) ? Mode.JULIA : mode));
    }
    public void zoom(@NotNull Matrix centre_offset, double level, boolean write, boolean additive) {
        //params.zoomConfig.addZoom(new ZoomParams(centreOffset, level));
        zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level, write, additive);
    }
    public void zoom(@NotNull Complex centre_offset, double level, boolean write, boolean additive) {
        if (write) {
            params.zoomConfig.addZoom(new ZoomParams(complexToMatrix(centre_offset), level));
        }
        setCentreOffset(centre_offset);
        setZoom((additive) ? zoom * level : level);
        setScale(base_precision * zoom);
        //setCenterX(toCoordinates(centreOffset)[0]);setCenterY(toCoordinates(centreOffset)[1]);
        populateMap();
    }
    private void populateMap() {
        for (int i = 0; i < getImageHeight(); i++) {
            for (int j = 0; j < getImageWidth(); j++) {
                argandMap[i][j] = fromCoordinates(j, i);
            }
        }
        if (color.getMode() == DOMAIN_COLORING && boundaryElements == null) {
            boundaryElements = new Complex[2 * (getImageHeight() + getImageWidth() - 2)];
            int j = 0;
            for (int i = 0; i < getImageHeight(); i++, ++j) {
                boundaryElements[j] = argandMap[i][0];
                boundaryElements[++j] = argandMap[i][getImageWidth() - 1];
            }
            for (int i = 1; i < getImageWidth() - 1; i++, ++j) {
                boundaryElements[j] = argandMap[0][i];
                boundaryElements[++j] = argandMap[getImageHeight() - 1][i];
            }
        }
    }
    @NotNull
    public Complex fromCoordinates(int x, int y) {
        @NotNull Complex point = add(new Complex(((boundsProtected(x, getImageWidth()) - centerX) / scale),
                ((centerY - boundsProtected(y, getImageHeight())) / scale)), centreOffset);
        if (abs(params.initParams.skew) > tolerance) {
            /*Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew);
            point = matrixToComplex(MatrixOperations.multiply(rotor, complexToMatrix(point)));*/
            point = matrixToComplex(doRotate(complexToMatrix(point), complexToMatrix(centreOffset), params.initParams.skew));
        }
        return point;
    }
    public void setCentreOffset(@NotNull Complex centreOffset) {
        this.centreOffset = new Complex(centreOffset);
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    public void mandelbrotToJulia(int cx, int cy, double level) {
        zoom(cx, cy, level);
        changeMode(centreOffset);
        resetCentre();
    }
    public void zoom(int cx, int cy, double level) {
        zoom(cx, cy, level, true, true);
    }
    public void zoom(int cx, int cy, double level, boolean write, boolean additive) {
        if (write) {
            params.zoomConfig.addZoom(new ZoomParams(cx, cy, level));
        }
        cx = boundsProtected(cx, argand.getWidth());
        cy = boundsProtected(cy, argand.getHeight());
        //setCenterX(cx);setCenterY(cy);
        setCentreOffset(fromCoordinates(cx, cy));
        setZoom((additive) ? zoom * level : level);
        setScale(base_precision * zoom);
        populateMap();
    }
    public void mandelbrotToJulia(@NotNull Complex constant, double level) {
        zoom(constant, level);
        changeMode(centreOffset);
        resetCentre();
    }
    public void zoom(@NotNull Complex constant, double level) {
        zoom(constant, level, true, true);
    }
    public void zoom(@NotNull Matrix constant, double level) {
        zoom(constant, level, true, true);
    }
    public void mandelbrotToJulia(@NotNull ZoomParams zoom) {
        zoom(zoom);
        changeMode(centreOffset);
        resetCentre();
    }
    @Override
    public void pan(int distance, double angle) {
        pan(distance, angle, false);
    }
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (PI / 2) - angle : angle;
        pan(Math.round(distance * (float) Math.cos(angle)), Math.round(distance * (float) Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(centerX + x_dist, centerY + y_dist, zoom, false, false);
        @NotNull int[][] tmp_escapes = new int[orbitEscapeData.length][orbitEscapeData[0].length];
        @NotNull double[][] tmp_normalized_escapes = new double[normalizedEscapes.length][normalizedEscapes[0].length];
        @NotNull PixelContainer tmp_argand = new LinearizedPixelContainer(argand);
        for (int i = 0; i < tmp_escapes.length && i < tmp_normalized_escapes.length; i++) {
            System.arraycopy(orbitEscapeData[i], 0, tmp_escapes[i], 0, tmp_escapes[i].length);
            System.arraycopy(normalizedEscapes[i], 0, tmp_normalized_escapes[i], 0, tmp_normalized_escapes[i].length);
        }
        argand = new PixelContainer(tmp_argand.getWidth(), tmp_argand.getHeight());
        orbitEscapeData = new int[tmp_escapes.length][tmp_escapes[0].length];
        normalizedEscapes = new double[tmp_normalized_escapes.length][tmp_normalized_escapes[0].length];
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
            System.arraycopy(tmp_escapes[i], (-x_dist), orbitEscapeData[j], 0, orbitEscapeData[j].length + x_dist);
            System.arraycopy(tmp_normalized_escapes[i], (-x_dist), normalizedEscapes[j], 0, normalizedEscapes[j].length + x_dist);
            for (int k = (-x_dist), l = 0; k < tmp_argand.getWidth() && l < tmp_argand.getWidth() + x_dist; k++, l++) {
                argand.setPixel(j, l, tmp_argand.getPixel(i, k));
            }
        } else {
            System.arraycopy(tmp_escapes[i], 0, orbitEscapeData[j], x_dist, orbitEscapeData[j].length - x_dist);
            System.arraycopy(tmp_normalized_escapes[i], 0, normalizedEscapes[j], x_dist, normalizedEscapes[j].length - x_dist);
            for (int k = 0, l = x_dist; k < tmp_argand.getWidth() - x_dist && l < tmp_argand.getWidth(); k++, l++) {
                argand.setPixel(j, l, tmp_argand.getPixel(i, k));
            }
        }
    }
    public enum Mode {MANDELBROT, JULIA, NEWTON, BUDDHABROT, NEWTONBROT, JULIABROT, MANDELBROT_NOVA, JULIA_NOVA, MANDELBROT_NOVABROT, JULIA_NOVABROT, SECANT, SECANTBROT, RUDY, RUDYBROT}
}