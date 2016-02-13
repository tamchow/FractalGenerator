package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.helpers.MathUtils;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.LinearizedImageData;
import in.tamchow.fractal.imgutils.Pannable;
import in.tamchow.fractal.math.FixedStack;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.ComplexOperations;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;
import in.tamchow.fractal.math.symbolics.Function;
import in.tamchow.fractal.math.symbolics.Polynomial;

import java.io.Serializable;
import java.util.ArrayList;
/** The actual fractal plotter for Julia, Newton, Nova (both Mandelbrot and Julia variants),Secant and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes.
 * Various (21) Coloring modes*/
public final class ComplexFractalGenerator implements Serializable, Pannable {
    ColorConfig color;
    ArrayList<Complex> roots;
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
    Complex centre_offset, degree, lastConstant, trap_point;
    boolean advancedDegree, mandelbrotToJulia, juliaToMandelbrot, useLineTrap;
    int[] histogram;
    double[][] normalized_escapes;
    double a, b, c;
    Publisher progressPublisher;
    ComplexFractalParams params;
    private String variableCode, oldvariablecode;
    public ComplexFractalGenerator(ComplexFractalParams params, Publisher progressPublisher) {
        this.params = params;
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.zoom_factor, params.initParams.base_precision, params.initParams.fractal_mode, params.initParams.function, params.initParams.consts, params.initParams.variableCode, params.initParams.oldvariablecode, params.initParams.tolerance, params.initParams.degree, params.initParams.color, params.initParams.switch_rate, params.initParams.trap_point, params.initParams.linetrap);
        if (params.zoomConfig != null) {for (ZoomParams zoom : params.zoomConfig.zooms) {zoom(zoom);}}
        this.progressPublisher = progressPublisher;
    }
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, ColorConfig color, Publisher progressPublisher) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, oldvariablecode, tolerance, new Complex(-1, 0), color, 0, Complex.ZERO, null);
        this.progressPublisher = progressPublisher;
    }
    private void initFractal(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, Complex degree, ColorConfig color, int switch_rate, Complex trap_point, String linetrap) {
        argand = new LinearizedImageData(width, height); setMode(mode);
        setMaxiter(argand.getHeight() * argand.getWidth());
        argand_map = new Complex[argand.getHeight()][argand.getWidth()];

        escapedata = new int[argand.getHeight()][argand.getWidth()];
        normalized_escapes = new double[argand.getHeight()][argand.getWidth()]; setVariableCode(variableCode);
        setZoom(zoom); setZoom_factor(zoom_factor); setFunction(function); setBase_precision(base_precision);
        setConsts(consts); setScale((int) (this.base_precision * Math.pow(zoom, zoom_factor))); resetCentre();
        setOldvariablecode(oldvariablecode); setTolerance(tolerance); roots = new ArrayList<>(); setColor(color);
        setDegree(degree); if (degree.equals(new Complex(-1, 0))) {
            setAdvancedDegree(true);
        } lastConstant = new Complex(-1, 0);
        if (this.color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE || color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR) {
            setStripe_density(this.color.getColor_density()); this.color.setColor_density(-1);
        } else {setStripe_density(-1);} mandelbrotToJulia = false; juliaToMandelbrot = false;
        if (!(switch_rate == 0 || switch_rate == -1 || switch_rate == 1)) {
            if (switch_rate < 0) {juliaToMandelbrot = true; this.switch_rate = -switch_rate;} else {
                mandelbrotToJulia = true; this.switch_rate = switch_rate;
            }
        } setTrap_point(trap_point); useLineTrap = false; if (linetrap != null) {
            a = Double.valueOf(linetrap.split(",")[0]); b = Double.valueOf(linetrap.split(",")[1]);
            c = Double.valueOf(linetrap.split(",")[2]); useLineTrap = true;
        } populateMap();
    }
    public void populateMap() {
        for (int i = 0; i < argand.getHeight(); i++) {
            for (int j = 0; j < argand.getWidth(); j++) {argand_map[i][j] = fromCooordinates(j, i);}
        } if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
            boundary_elements = new Complex[2 * (argand.getHeight() + argand.getWidth() - 2)]; int j = 0;
            for (int i = 0; i < argand.getHeight(); i++, ++j) {
                boundary_elements[j] = argand_map[i][0];
                boundary_elements[++j] = argand_map[i][argand_map[i].length - 1];
            } for (int i = 1; i < argand.getWidth() - 1; i++, ++j) {
                boundary_elements[j] = argand_map[0][i]; boundary_elements[++j] = argand_map[argand_map.length - 1][i];
            }
        }
    }
    public Complex fromCooordinates(int x, int y) {
        x = MathUtils.boundsProtected(x, argand.getWidth()); y = MathUtils.boundsProtected(y, argand.getHeight());
        if (params.initParams.skew == 0) {
            return ComplexOperations.add(centre_offset, new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale)));
        } else {
            double[][] matrix = new double[2][1]; matrix[0][0] = ((((double) x) - center_x) / scale);
            matrix[1][0] = ((center_y - ((double) y)) / scale); Matrix point = new Matrix(matrix);
            Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew);
            point = MatrixOperations.multiply(rotor, point);
            return ComplexOperations.add(centre_offset, new Complex(point.get(0, 0), point.get(1, 0)));
        }
    }
    public void resetCentre() {
        setCenter_x(argand.getWidth() / 2); setCenter_y(argand.getHeight() / 2); resetCentre_Offset();
    }
    public void resetCentre_Offset() {centre_offset = new Complex(Complex.ZERO);}
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color, Publisher progressPublisher, int switch_rate, Complex trap_point) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1, 0), color, switch_rate, trap_point, null);
        this.progressPublisher = progressPublisher;
    }
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color, Publisher progressPublisher, int switch_rate, String linetrap) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1, 0), color, switch_rate, Complex.ZERO, linetrap);
        this.progressPublisher = progressPublisher;
    }
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, Mode mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color, Publisher progressPublisher, int switch_rate, Complex trap_point, String linetrap) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1, 0), color, switch_rate, trap_point, linetrap);
        this.progressPublisher = progressPublisher;
    }
    public ColorConfig getColor() {return color;}
    public void setColor(ColorConfig color) {this.color = new ColorConfig(color);}
    public double modulusForPhase(double phase) {
        for (Complex num : boundary_elements) {
            if (Math.abs(phase - num.arg()) <= tolerance) {
                return num.modulus();
            }
        } return Double.NaN;
    }
    public String getOldvariablecode() {return oldvariablecode;}
    public void setOldvariablecode(String oldvariablecode) {this.oldvariablecode = oldvariablecode;}
    public Complex getTrap_point() {return trap_point;}
    public void setTrap_point(Complex trap_point) {this.trap_point = new Complex(trap_point);}
    public Publisher getProgressPublisher() {return progressPublisher;}
    public void setProgressPublisher(Publisher progressPublisher) {this.progressPublisher = progressPublisher;}
    public ComplexFractalParams getParams() {return params;}
    public void setParams(ComplexFractalParams params) {this.params = params;}
    public int getStripe_density() {return stripe_density;}
    public void setStripe_density(int stripe_density) {this.stripe_density = stripe_density;}
    public boolean isAdvancedDegree() {
        return advancedDegree;
    }
    public void setAdvancedDegree(boolean advancedDegree) {
        this.advancedDegree = advancedDegree;
    }
    public ArrayList<Complex> getRoots() {
        return roots;
    }
    public long getMaxiter() {
        return maxiter;
    }
    public void setMaxiter(long maxiter) {
        this.maxiter = maxiter;
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public int[][] getEscapedata() {
        return escapedata;
    }
    public Complex getDegree() {
        return degree;
    }
    public void setDegree(Complex degree) {
        this.degree = new Complex(degree);
    }
    public double getTolerance() {
        return tolerance;
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
    public Complex[][] getArgand_map() {
        return argand_map;
    }
    public void setArgand_map(Complex[][] argand_map) {
        this.argand_map = new Complex[argand_map.length][argand_map[0].length];
        for (int i = 0; i < argand_map.length; i++) {
            for (int j = 0; j < argand_map[0].length; j++) {this.argand_map[i][j] = new Complex(argand_map[i][j]);}
        }
    }
    public Complex getCentre_offset() {
        return centre_offset;
    }
    public void setCentre_offset(Complex centre_offset) {
        this.centre_offset = new Complex(centre_offset);
    }
    public Mode getMode() {return mode;}
    public void setMode(Mode mode) {this.mode = mode;}
    /**@param nx:No.   of threads horizontally
     * @param ix:Index of thread horizontally
     * @param ny:No.   of threads vertically
     * @param iy:Index of thread vertically
     * @return the start and end coordinates for a particular thread's rendering region*/
    public int[] start_end_coordinates(int nx, int ix, int ny, int iy) {
        return start_end_coordinates(0, argand.getWidth(), 0, argand.getHeight(), nx, ix, ny, iy);
    }
    public int[] start_end_coordinates(int startx, int endx, int starty, int endy, int nx, int ix, int ny, int iy) {
        //for multithreading purposes
        int start_x = startx, end_x, start_y = starty, end_y;
        int x_dist = (endx - startx) / nx, y_dist = (endy - starty) / ny;
        if (ix == (nx - 1)) {
            start_x += (nx - 1) * x_dist; end_x = endx;
        } else {start_x += ix * x_dist; end_x = (ix + 1) * x_dist;} if (iy == (ny - 1)) {
            start_y += (ny - 1) * y_dist; end_y = endy;
        } else {start_y += iy * y_dist; end_y = (iy + 1) * y_dist;} return new int[]{start_x, end_x, start_y, end_y};
    }
    public double getScale() {
        return scale;
    }
    public void setScale(double scale) {
        this.scale = scale;
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
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {this.base_precision = base_precision;}
    }
    public double calculateBasePrecision() {
        return ((argand.getHeight() >= argand.getWidth()) ? argand.getWidth() / 2 : argand.getHeight() / 2);
    }
    public ImageData getArgand() {
        return argand;
    }
    public void setArgand(ImageData argand) {this.argand = new LinearizedImageData(argand);}
    public String[][] getConsts() {
        return consts;
    }
    public void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length]; for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
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
    public void generate(ComplexFractalParams params) {
        if (params.runParams.fully_configured) {
            generate(params.runParams.start_x, params.runParams.end_x, params.runParams.start_y, params.runParams.end_y, (int) params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        } else {generate(params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);}
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
        if (mode != Mode.NEWTON && mode != Mode.NEWTONBROT && mode != Mode.JULIA_NOVA && mode != Mode.JULIA_NOVABROT && mode != Mode.MANDELBROT_NOVA && mode != Mode.MANDELBROT_NOVABROT && (color.getMode() != Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() != Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) && degree.equals(new Complex(-1, 0)) && (!color.isExponentialSmoothing())) {
            degree = new FunctionEvaluator(variableCode, consts, oldvariablecode, advancedDegree).getDegree(function);
        }
        if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
            histogram = new int[iterations + 2];
        } switch (mode) {
            case MANDELBROT: case RUDY: case RUDYBROT:
            case BUDDHABROT: mandelbrotGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius); break;
            case JULIA: case JULIABROT: juliaGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius); break;
            case NEWTON: case NEWTONBROT: case JULIA_NOVA: case JULIA_NOVABROT: case MANDELBROT_NOVA:
            case MANDELBROT_NOVABROT: newtonGenerate(start_x, end_x, start_y, end_y, iterations, constant); break;
            case SECANT: case SECANTBROT: secantGenerate(start_x, end_x, start_y, end_y, iterations); break;
            default: throw new IllegalArgumentException("Unknown fractal render mode");
        }
        if (!params.useThreadedGenerator() && (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR)) {
            double scaling = Math.pow(zoom, zoom_factor);
            int total = 0; for (int i = 0; i < iterations; i += 1) {total += histogram[i];}
            for (int i = start_y; i < end_y; i++) {
                for (int j = start_x; j < end_x; j++) {
                    double hue = 0.0, hue2 = 0.0, hue3 = 0.0;
                    for (int k = 0; k < escapedata[i][j]; k += 1) {hue += ((double) histogram[k]) / total;}
                    double normalized_count = normalized_escapes[i][j]; int colortmp;
                    if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                        for (int k = 0; k < escapedata[i][j] + 1; k += 1) {hue2 += ((double) histogram[k]) / total;}
                        for (int k = 0; k < escapedata[i][j] - 1; k += 1) {hue3 += ((double) histogram[k]) / total;}
                        int colortmp1 = ColorConfig.linearInterpolated(color.createIndex(hue, 0, 1, scaling), color.createIndex(hue2, 0, 1, scaling), normalized_count - (int) normalized_count, color.isByParts());
                        int colortmp2 = ColorConfig.linearInterpolated(color.createIndex(hue3, 0, 1, scaling), color.createIndex(hue, 0, 1, scaling), normalized_count - (int) normalized_count, color.isByParts());
                        colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, normalized_count - (int) normalized_count, color.isByParts());
                    } else {
                        colortmp = color.splineInterpolated(color.createIndex(hue, 0, 1, scaling), normalized_count - (int) normalized_count);
                    } argand.setPixel(i, j, colortmp);
                }
            }
        }
    }
    public void secantGenerate(int start_x, int end_x, int start_y, int end_y, int iterations) {
        FixedStack last = new FixedStack(iterations + 2); FixedStack lastd = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode); func.setConsts(consts);
                function = func.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = func.getDegree();}
                functionderiv = func.derivative(1);
            } else {
                Polynomial poly = Polynomial.fromString(function); poly.setConstdec(consts);
                poly.setVariableCode(variableCode); poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = poly.getDegree();}
                functionderiv = poly.derivative().toString();
            }
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode, advancedDegree);
        long ctr = 0; outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j], zd = new Complex(Complex.ONE), ztmp2 = new Complex(Complex.ZERO), ztmpd2 = new Complex(Complex.ZERO), z2 = new Complex(Complex.ZERO);
                int c = 0; fe.setZ_value(z.toString()); fe.setOldvalue(ztmp2 + "");
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString()); fed.setOldvalue(ztmpd2 + "");
                } last.push(z); lastd.push(zd);
                double s = 0, maxModulus = 0, mindist = 1E10, maxdist = mindist, lbnd = 0, ubnd = 0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE) {
                    mindist = 0; maxdist = mindist;
                }
                while (c <= iterations) {
                    Complex ztmp = new Complex(z), ztmpd = new Complex(zd); last.pop();
                    ztmp2 = (last.size() > 0) ? last.peek() : ztmp2; last.push(z); fe.setOldvalue(ztmp2 + "");
                    last.pop(); if (last.size() > 1) {last.pop(); z2 = last.peek(); last.push(ztmp2);} last.push(z);
                    Complex a = fe.evaluate(function, false); fe.setZ_value(ztmp2.toString());
                    Complex b = fe.evaluate(function, false);
                    ztmp = ComplexOperations.subtract(ztmp, ComplexOperations.divide(ComplexOperations.multiply(a, ComplexOperations.subtract(ztmp, ztmp2)), ComplexOperations.subtract(a, b)));
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        Complex e = fed.evaluate(functionderiv, false); fed.setZ_value(ztmpd2.toString());
                        Complex d = fed.evaluate(functionderiv, false);
                        ztmpd = ComplexOperations.subtract(ztmpd, ComplexOperations.divide(ComplexOperations.multiply(e, ComplexOperations.subtract(ztmpd, ztmpd2)), ComplexOperations.subtract(e, d)));
                    } fe.setZ_value(ztmp + "");
                    s += Math.exp(-ComplexOperations.divide(Complex.ONE, ComplexOperations.subtract(z, ztmp)).modulus());
                    double distance = 0; if (useLineTrap) {
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
                    } maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, false).modulus() <= tolerance/*&&roots.size()<(int)degree.modulus()*/) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {roots.add(ztmp);}
                        } break;
                    } if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {roots.add(ztmp);}
                        } break;
                    } z = new Complex(ztmp); fe.setZ_value(z.toString()); fe.setOldvalue(ztmp2 + "");
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = new Complex(ztmpd); fed.setZ_value(ztmpd.toString()); lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2; lastd.push(zd);
                        fed.setOldvalue(ztmpd2 + "");
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++; maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;
                } if (roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {pass[k] = last.pop();} if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(Complex.ZERO) : pass[m - 1];
                    }
                } pass[0] = new Complex(z);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = new Complex(centre_offset);
                } escapedata[i][j] = c;
                Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = ComplexOperations.distance_squared(pass[2], root);
                double d1 = ComplexOperations.distance_squared(root, pass[0]);
                if (color.isExponentialSmoothing()) {normalized_escapes[i][j] = s;} else {
                    normalized_escapes[i][j] = c + Math.abs((Math.log(tolerance) - Math.log(d0)) / (Math.log(d1) - Math.log(d0)));
                } int colortmp = 0x0; switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case ORBIT_TRAP_MAX:
                    case LINE_TRAP_MAX: colortmp = getColor(i, j, c, pass, maxdist, iterations); break;
                    case ORBIT_TRAP_AVG:
                    case LINE_TRAP_AVG: colortmp = getColor(i, j, c, pass, (mindist + maxdist) / 2, iterations); break;
                    case EPSILON_CROSS_LINEAR: case EPSILON_CROSS_SPLINE: case GAUSSIAN_INT_DISTANCE_LINEAR:
                    case GAUSSIAN_INT_DISTANCE_SPLINE: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case CURVATURE_AVERAGE_LINEAR: case CURVATURE_AVERAGE_SPLINE: case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE: case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE: colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations); break;
                    default: colortmp = getColor(i, j, c, pass, maxModulus, iterations);
                } if (mode == Mode.SECANTBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {argand.setPixel(i, j, colortmp);} last.clear(); lastd.clear();
            }
        }
    }
    /**
     * NOTE:Call after generating the fractal, as this uses data from @code escapdedata
     */
    public Complex[] getBoundaryPoints(int depth) {
        ArrayList<Complex> points = new ArrayList<>(2 * argand.getWidth());
        for (int j = 0; j < argand_map[0].length; j++) {
            int imin = -1, imax = -1; for (int i = 0; i < argand_map.length; i++) {
                int itmp = -1; if (escapedata[i][j] == depth) {itmp = i;} if (imin == -1) {imin = itmp; imax = imin;}
                if (itmp > imax) {imax = itmp;}
            } points.add(argand_map[imin][j]); points.add(argand_map[imax][j]);
        } Complex[] boundaryPoints = new Complex[points.size()]; points.toArray(boundaryPoints); return boundaryPoints;
    }
    private boolean isInBounds(Complex val) {
        if (val.imaginary() <= argand_map[0][center_x].imaginary() && val.imaginary() >= argand_map[argand_map.length - 1][center_x].imaginary()) {
            if (val.real() <= argand_map[center_y][argand_map[0].length - 1].real() && val.real() >= argand_map[center_y][0].real()) {
                return true;
            }
        } return false;
    }
    public Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(consts[0][1]);
            } else {lastConstant = new Complex(consts[getLastConstantIndex()][1]);}
        } return lastConstant;
    }
    public void setLastConstant(Complex value) {
        consts[getLastConstantIndex()][1] = value.toString(); lastConstant = new Complex(value);
    }
    public int getLastConstantIndex() {
        String[] parts = function.split(" "); for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIdx(getConstantIndex(parts[i])); return lastConstantIdx;
            }
        } return -1;
    }
    public int getConstantIndex(String constant) {
        for (int i = 0; i < consts.length; i++) {if (consts[i][0].equals(constant)) {return i;}} return -1;
    }
    public void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack last = new FixedStack(iterations + 2); FixedStack lastd = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode); func.setConsts(consts);
                function = func.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = func.getDegree();}
                functionderiv = func.derivative(1);
            } else {
                Polynomial poly = Polynomial.fromString(function); poly.setConstdec(consts);
                poly.setVariableCode(variableCode); poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = poly.getDegree();}
                functionderiv = poly.derivative().toString();
            }
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode, advancedDegree);
        long ctr = 0; Complex lastConstantBackup = getLastConstant();
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
                    mindist = 0; maxdist = mindist;
                }
                Complex z = (mode == Mode.RUDY || mode == Mode.RUDYBROT) ? new Complex(argand_map[i][j]) : new Complex(Complex.ZERO);
                Complex zd = new Complex(Complex.ONE), ztmp2 = new Complex(Complex.ZERO), ztmpd2 = new Complex(Complex.ZERO), z2 = new Complex(Complex.ZERO);
                setLastConstant(argand_map[i][j]); fe.setZ_value(z.toString()); fe.setOldvalue(ztmp2 + "");
                fe.setConstdec(this.consts);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString()); fed.setOldvalue(ztmpd2 + "");
                    fed.setConstdec(this.consts);
                } int c = 0; last.push(z); lastd.push(zd); boolean useJulia = false;
                while (c <= iterations && z.modulus() < escape_radius) {
                    if (mandelbrotToJulia) {
                        if (c % switch_rate == 0) {useJulia = (!useJulia);} if (useJulia) {
                            setLastConstant(lastConstantBackup); fe.setConstdec(this.consts);
                            fed.setConstdec(this.consts);
                        } else if (!useJulia) {
                            setLastConstant(argand_map[i][j]); fe.setConstdec(this.consts);
                            fed.setConstdec(this.consts);
                        }
                    } last.pop(); ztmp2 = (last.size() > 0) ? last.peek() : ztmp2; last.push(z);
                    fe.setOldvalue(ztmp2 + ""); last.pop(); if (last.size() > 1) {
                        last.pop(); z2 = last.peek(); last.push(ztmp2);
                    } last.push(z); Complex ztmp = fe.evaluate(function, false);
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = fed.evaluate(functionderiv, false);
                    } last.push(ztmp); s += Math.exp(-ztmp.modulus()); double distance = 0; if (useLineTrap) {
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
                    } maxdist = (Math.max(distance, maxdist)); if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {c = iterations; break;}
                    z = new Complex(ztmp); fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        fed.setZ_value(zd.toString()); lastd.pop(); ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd); fed.setOldvalue(ztmpd2 + "");
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;}
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(Complex.ZERO) : pass[m - 1];
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = argand_map[i][j];
                } escapedata[i][j] = c; if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass[0], escape_radius);
                } int colortmp = 0x0; switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case ORBIT_TRAP_MAX: case LINE_TRAP_MAX:
                    case DOMAIN_COLORING_FAUX: colortmp = getColor(i, j, c, pass, maxdist, iterations); break;
                    case ORBIT_TRAP_AVG:
                    case LINE_TRAP_AVG: colortmp = getColor(i, j, c, pass, (mindist + maxdist) / 2, iterations); break;
                    case EPSILON_CROSS_LINEAR: case EPSILON_CROSS_SPLINE: case GAUSSIAN_INT_DISTANCE_LINEAR:
                    case GAUSSIAN_INT_DISTANCE_SPLINE: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case CURVATURE_AVERAGE_LINEAR: case CURVATURE_AVERAGE_SPLINE: case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE: case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE: colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations); break;
                    default: colortmp = getColor(i, j, c, pass, escape_radius, iterations);
                } if (mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {argand.setPixel(i, j, colortmp);} last.clear(); lastd.clear();
            }
        }
    }
    public void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, Complex constant) {
        String functionderiv = "", functionderiv2 = ""; if (Function.isSpecialFunction(function)) {
            Function func = Function.fromString(function, variableCode, oldvariablecode); func.setConsts(consts);
            function = func.toString();
            if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = func.getDegree();}
            functionderiv = func.derivative(1);
            if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                functionderiv2 = func.derivative(2);
            }
        } else {
        Polynomial polynomial = Polynomial.fromString(function); polynomial.setConstdec(consts);
            polynomial.setVariableCode(variableCode); polynomial.setOldvariablecode(oldvariablecode);
            function = polynomial.toString();
            if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {
                degree = polynomial.getDegree();
            }
            functionderiv = polynomial.derivative() + "";
            if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                functionderiv2 = polynomial.derivative().derivative() + "";
            }
        } FixedStack last = new FixedStack(iterations + 2); FixedStack lastd = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode, advancedDegree);
        if (constant != null && constant.equals(Complex.ZERO)) {
            constant = ComplexOperations.divide(Complex.ONE, degree);
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode, advancedDegree);
        long ctr = 0; Complex toadd = new Complex(Complex.ZERO);
        Complex lastConstantBackup = new Complex(getLastConstant());
        if (mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT) {toadd = new Complex(getLastConstant());}
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
                    mindist = 0; maxdist = mindist;
                }
                boolean useJulia = false, useMandelbrot = false;
                Complex z = argand_map[i][j], zd = new Complex(Complex.ONE), ztmp2 = new Complex(Complex.ZERO), ztmpd2 = new Complex(Complex.ZERO), z2 = new Complex(Complex.ZERO);
                int c = 0; fe.setZ_value(z.toString()); fe.setOldvalue(ztmp2 + "");
                if (mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
                    toadd = argand_map[i][j]; z = new Complex(Complex.ZERO);
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString()); fed.setOldvalue(ztmpd2 + "");
                } last.push(z); lastd.push(zd);
                while (c <= iterations) {
                    if (mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
                        if (mandelbrotToJulia) {
                            if (c % switch_rate == 0) {useJulia = (!useJulia);}
                            if (useJulia) {toadd = lastConstantBackup;} else if (!useJulia) {toadd = argand_map[i][j];}
                        }
                    } if (mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT) {
                        if (juliaToMandelbrot) {
                            if (c % switch_rate == 0) {useMandelbrot = (!useMandelbrot);}
                            if (!useMandelbrot) {toadd = lastConstantBackup;} else if (useMandelbrot) {
                                toadd = argand_map[i][j];
                            }
                        }
                    } last.pop(); ztmp2 = (last.size() > 0) ? last.peek() : ztmp2; last.push(z); last.pop();
                    if (last.size() > 1) {last.pop(); z2 = last.peek(); last.push(ztmp2);} last.push(z);
                    Complex ztmp, ztmpd; fe.setOldvalue(ztmp2 + ""); if (constant != null) {
                        ztmp = ComplexOperations.add(ComplexOperations.subtract(z, ComplexOperations.multiply(constant, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false)))), toadd);
                        ztmpd = null;
                        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                            ztmpd = ComplexOperations.add(ComplexOperations.subtract(zd, ComplexOperations.multiply(constant, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false)))), toadd);
                        }} else {
                        ztmp = ComplexOperations.add(ComplexOperations.subtract(z, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(functionderiv, false))), toadd);
                        ztmpd = null;
                        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                            ztmpd = ComplexOperations.add(ComplexOperations.subtract(zd, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(functionderiv2, false))), toadd);
                        }
                    } fe.setZ_value(ztmp + "");
                    s += Math.exp(-ComplexOperations.divide(Complex.ONE, ComplexOperations.subtract(z, ztmp)).modulus());
                    double distance = 0; if (useLineTrap) {
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
                    } maxdist = (Math.max(distance, maxdist));
                    if (fe.evaluate(function, false).modulus() <= tolerance/*&&roots.size()<(int)degree.modulus()*/) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {roots.add(ztmp);}
                        } break;
                    } if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {
                        if (color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_STRIPES || color.getMode() == Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED) {
                            if (indexOfRoot(ztmp) == -1) {roots.add(ztmp);}
                        } break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = new Complex(ztmpd); fed.setZ_value(zd.toString()); lastd.pop();
                        ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2; lastd.push(zd);
                        fed.setOldvalue(ztmpd2 + "");
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++; maxModulus = z.modulus() > maxModulus ? z.modulus() : maxModulus;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;
                } if (roots.size() == 0) {
                    throw new UnsupportedOperationException("Could not find a root in given iteration limit. Try a higher iteration limit.");
                }
                double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {pass[k] = last.pop();} if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(Complex.ZERO) : pass[m - 1];
                    }
                } pass[0] = new Complex(z);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = new Complex(centre_offset);
                } escapedata[i][j] = c;
                Complex root = (roots.size() == 0) ? pass[1] : roots.get(closestRootIndex(pass[0]));
                double d0 = ComplexOperations.distance_squared(pass[2], root);
                double d1 = ComplexOperations.distance_squared(root, pass[0]);
                if (color.isExponentialSmoothing()) {normalized_escapes[i][j] = s;} else {
                    normalized_escapes[i][j] = c + Math.abs((Math.log(tolerance) - Math.log(d0)) / (Math.log(d1) - Math.log(d0)));
                } int colortmp = 0x0; switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case ORBIT_TRAP_MAX:
                    case LINE_TRAP_MAX: colortmp = getColor(i, j, c, pass, maxdist, iterations); break;
                    case ORBIT_TRAP_AVG:
                    case LINE_TRAP_AVG: colortmp = getColor(i, j, c, pass, (mindist + maxdist) / 2, iterations); break;
                    case EPSILON_CROSS_LINEAR: case EPSILON_CROSS_SPLINE: case GAUSSIAN_INT_DISTANCE_LINEAR:
                    case GAUSSIAN_INT_DISTANCE_SPLINE: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case CURVATURE_AVERAGE_LINEAR: case CURVATURE_AVERAGE_SPLINE: case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE: case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE: colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations); break;
                    default: colortmp = getColor(i, j, c, pass, maxModulus, iterations);
                } if (mode == Mode.NEWTONBROT || mode == Mode.JULIA_NOVABROT || mode == Mode.MANDELBROT_NOVABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {argand.setPixel(i, j, colortmp);} last.clear(); lastd.clear();
            }
        }
    }
    public void publishProgress(long ctr, int i, int startx, int endx, int j, int starty, int endy) {
        if (params != null && (!params.useThreadedGenerator())) {
        float completion = ((float) ((i - starty) * (endx - startx) + (j - startx)) / ((endx - startx) * (endy - starty))) * 100.0f;
            progressPublisher.publish(ctr + " iterations of " + maxiter + ",completion = " + completion + "%", completion / 100.0);
        }
    }
    private int indexOfRoot(Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (ComplexOperations.distance_squared(roots.get(i), z) < tolerance) {return i;}}return -1;}
    private int closestRootIndex(Complex z) {
        int leastDistanceIdx = 0; double leastDistance = ComplexOperations.distance_squared(z, roots.get(0));
        for (int i = 0; i < roots.size(); i++) {
            double distance = ComplexOperations.distance_squared(z, roots.get(i));
            if (distance < leastDistance) {leastDistance = distance; leastDistanceIdx = i;}
        } return leastDistanceIdx;
    }
    public void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack last = new FixedStack(iterations + 2); FixedStack lastd = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            if (Function.isSpecialFunction(function)) {
                Function func = Function.fromString(function, variableCode, oldvariablecode); func.setConsts(consts);
                function = func.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = func.getDegree();}
                functionderiv = func.derivative(1);
            } else {
                Polynomial poly = Polynomial.fromString(function); poly.setConstdec(consts);
                poly.setVariableCode(variableCode); poly.setOldvariablecode(oldvariablecode);
                function = poly.toString();
                if (degree.equals(new Complex("-1")) && (!color.isExponentialSmoothing())) {degree = poly.getDegree();}
                functionderiv = poly.derivative().toString();
            }
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, oldvariablecode, advancedDegree);
        long ctr = 0; Complex lastConstantBackup = getLastConstant(); outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j], zd = new Complex(Complex.ONE), ztmp2 = new Complex(Complex.ZERO), ztmpd2 = new Complex(Complex.ZERO), z2 = new Complex(Complex.ZERO);
                double s = 0, mindist = escape_radius, maxdist = mindist, lbnd = 0, ubnd = 0; int c = 0x0;
                if (color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR ||
                        color.mode == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR ||
                        color.mode == Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE ||
                        color.mode == Colors.CALCULATIONS.DOMAIN_COLORING_FAUX) {
                    mindist = 0; maxdist = mindist;
                }
                fe.setZ_value(z.toString());
                fe.setOldvalue(ztmp2 + "");
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString()); fed.setOldvalue(ztmpd2 + "");
                } last.push(z); lastd.push(zd); boolean useMandelBrot = false;
                while (c <= iterations && z.modulus() < escape_radius) {
                    if (juliaToMandelbrot) {
                        if (c % switch_rate == 0) {useMandelBrot = (!useMandelBrot);} if (!useMandelBrot) {
                            setLastConstant(lastConstantBackup); fe.setConstdec(this.consts);
                            fed.setConstdec(this.consts);
                        } else if (useMandelBrot) {
                            setLastConstant(argand_map[i][j]); fe.setConstdec(this.consts);
                            fed.setConstdec(this.consts);
                        }
                    } last.pop(); ztmp2 = (last.size() > 0) ? last.peek() : ztmp2; last.push(z);
                    fe.setOldvalue(ztmp2 + ""); last.pop();
                    if (last.size() > 1) {last.pop(); z2 = last.peek(); last.push(ztmp2);} last.push(z);
                    Complex ztmp = fe.evaluate(function, false);
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = fed.evaluate(functionderiv, false);
                    } last.push(ztmp); s += Math.exp(-ztmp.modulus()); double distance = 0; if (useLineTrap) {
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
                    } maxdist = (Math.max(distance, maxdist)); if (color.mode == Colors.CALCULATIONS.DOMAIN_COLORING) {
                        maxdist = Math.max(ztmp.modulus(), maxdist);
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) <= tolerance) {c = iterations; break;}
                    z = new Complex(ztmp); fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        fed.setZ_value(zd.toString()); lastd.pop(); ztmpd2 = (lastd.size() > 0) ? lastd.peek() : ztmpd2;
                        lastd.push(zd); fed.setOldvalue(ztmpd2 + "");
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;}
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {pass[k] = last.pop();} if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = m == 0 ? new Complex(Complex.ZERO) : pass[m - 1];
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = new Complex(centre_offset);
                } escapedata[i][j] = c; if (color.isExponentialSmoothing()) {
                    normalized_escapes[i][j] = s;
                } else {
                    normalized_escapes[i][j] = getNormalized(c, iterations, pass[0], escape_radius);
                } int colortmp = 0x0; switch (color.getMode()) {
                    case ORBIT_TRAP_MIN:
                    case LINE_TRAP_MIN: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case ORBIT_TRAP_MAX: case LINE_TRAP_MAX:
                    case DOMAIN_COLORING_FAUX: colortmp = getColor(i, j, c, pass, maxdist, iterations); break;
                    case ORBIT_TRAP_AVG:
                    case LINE_TRAP_AVG: colortmp = getColor(i, j, c, pass, (mindist + maxdist) / 2, iterations); break;
                    case EPSILON_CROSS_LINEAR: case EPSILON_CROSS_SPLINE: case GAUSSIAN_INT_DISTANCE_LINEAR:
                    case GAUSSIAN_INT_DISTANCE_SPLINE: colortmp = getColor(i, j, c, pass, mindist, iterations); break;
                    case CURVATURE_AVERAGE_LINEAR: case CURVATURE_AVERAGE_SPLINE: case STRIPE_AVERAGE_LINEAR:
                    case STRIPE_AVERAGE_SPLINE: case TRIANGLE_AREA_INEQUALITY_LINEAR:
                    case TRIANGLE_AREA_INEQUALITY_SPLINE: colortmp = getColor(i, j, c, pass, c == 0 ? mindist : mindist / c, iterations); break;
                    default: colortmp = getColor(i, j, c, pass, escape_radius, iterations);
                } if (mode == Mode.JULIABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + colortmp);
                } else {argand.setPixel(i, j, colortmp);} last.clear(); lastd.clear();
            }
        }
    }
    public ColorConfig getInterpolated() {return color;}
    public double getNormalized(int val, int iterations, Complex z, double escape) {
        double renormalized; if (!color.isLogIndex()) {
            if (degree.equals(Complex.ZERO) || degree.equals(Complex.ONE)) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = (val + 1) + ComplexOperations.divide(new Complex(Math.log(Math.log(z.modulus()) / Math.log(escape))), ComplexOperations.principallog(degree)).modulus();
                if (renormalized - (long) renormalized == 0) {renormalized += ((double) val / iterations);}
            }
        } else {
            if (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT || mode == Mode.RUDY || mode == Mode.RUDYBROT) {
            if (degree.equals(Complex.ZERO) || degree.equals(Complex.ONE)) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = (val + 1) + ComplexOperations.divide(new Complex(Math.log(Math.log(z.modulus()) / Math.log(escape))), ComplexOperations.principallog(degree)).modulus();
                if (renormalized - (long) renormalized == 0) {renormalized += ((double) val / iterations);}
            }
            } else {renormalized = val + (0.5 + 0.5 * (Math.sin(z.arg()) * color.color_density));}
        } return renormalized;
    }
    public int[] getHistogram() {return histogram;}
    public double[][] getNormalized_escapes() {return normalized_escapes;}
    public int getLastConstantIdx() {return lastConstantIdx;}
    public void setLastConstantIdx(int lastConstantIdx) {this.lastConstantIdx = lastConstantIdx;}
    public int getColor(int i, int j, int val, Complex[] last, double escape_radius, int iterations) {
        int colortmp, colortmp1, colortmp2, color1, color2, color3, index;
        double renormalized, lbnd = 0, ubnd = 1, calc, scaling = Math.pow(zoom, zoom_factor), smoothcount;
        if (color.isExponentialSmoothing() || mode == Mode.NEWTON || mode == Mode.NEWTONBROT || mode == Mode.JULIA_NOVA || mode == Mode.JULIA_NOVABROT || mode == Mode.MANDELBROT_NOVA || mode == Mode.MANDELBROT_NOVABROT) {
            smoothcount = normalized_escapes[i][j]; renormalized = smoothcount;
        } else {
            renormalized = getNormalized(val, iterations, last[0], escape_radius); if (color.isLogIndex()) {
                smoothcount = (renormalized > 0) ? Math.abs(Math.log(renormalized)) : ComplexOperations.principallog(new Complex(renormalized, 0)).modulus();
            } else {smoothcount = renormalized;}
        }
        switch (color.getMode()) {
            case SIMPLE: colortmp = color.getColor(color.createIndex(val, 0, iterations, scaling)); break;
            case SIMPLE_SMOOTH_LINEAR: colortmp = getInterpolated(color.createIndex(val, 0, iterations, scaling), smoothcount); break;
            case SIMPLE_SMOOTH_SPLINE: colortmp = color.splineInterpolated(color.createIndex(val, 0, iterations, scaling), smoothcount); break;
            case COLOR_DIVIDE_DIRECT: val = (val == 0) ? iterations + 1 : (val - 1 == 0) ? iterations + 2 : val; color1 = (0xffffff / val); color2 = (0xffffff / (val + 1)); color3 = (0xffffff / (val - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts()); break;
            case COLOR_DIVIDE_NORMALIZED: color1 = (int) (0xffffff / renormalized); color2 = (int) (0xffffff / (renormalized + 1)); color3 = (int) (0xffffff / (renormalized - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts()); break;
            case COLOR_GRAYSCALE_HIGH_CONTRAST: colortmp = ColorConfig.toGray(val * iterations); break;
            case SIMPLE_DISTANCE_ESTIMATION: calc = Math.abs((double) val / iterations); if (calc > 1) {
                calc = calc - 1;
            } colortmp1 = (int) (calc * 255); if (calc > 0.5) {
                colortmp = ColorConfig.toRGB(colortmp1, 255, colortmp1);
            } else {
                colortmp = ColorConfig.toRGB(0, colortmp1, 0);
            } break;
            case COLOR_MULTIPLY_DIRECT: color1 = ColorConfig.toGray(val); color2 = ColorConfig.toGray(val + 1); color3 = ColorConfig.toGray(Math.abs((val - 1))); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts()); break;
            case COLOR_MULTIPLY_NORMALIZED: color1 = ColorConfig.toGray((int) Math.abs(renormalized)); color2 = ColorConfig.toGray((int) Math.abs(renormalized + 1)); color3 = ColorConfig.toGray((int) Math.abs(renormalized - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts()); break;
            case COLOR_GRAYSCALE_LOW_CONTRAST: colortmp = ColorConfig.toGray(val); break;
            case DISTANCE_ESTIMATION_GRAYSCALE:
            case DISTANCE_ESTIMATION_COLOR: double distance; if ((mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT)) {
                distance = Math.abs(Math.sqrt(Math.pow(last[0].modulus(), 2) / Math.pow(last[1].modulus(), 2)) * 0.5 * Math.log(Math.pow(last[0].modulus(), 2)));
            } else {
                distance = Math.abs(last[0].modulus() * Math.log(last[0].modulus())) / Math.log(last[1].modulus());
            } if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE) {
                color1 = (int) Math.abs((distance - (long) distance) * 255); if (color1 > 255) color1 %= 255;
                colortmp = ColorConfig.toRGB(color1, color1, color1);
            } else {
                index = color.createIndex((distance - (long) distance), lbnd, ubnd, scaling);
                colortmp = color.splineInterpolated(index, distance - (long) distance);
                /*if (mode == Mode.BUDDHABROT || mode == Mode.MANDELBROT) {
                    if (!color.isByParts()) {colortmp = (distance > escape_radius) ? 0xffffff : 0x000000;} else {
                        colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(last[0], last[2]))) ? 0xffffff : 0x000000;
                    }} else {
                    colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(centre_offset, last[2]))) ? 0xffffff : 0x000000;}*/
            } break; case COLOR_HISTOGRAM:
            case COLOR_HISTOGRAM_LINEAR: colortmp = 0x000000; break;//Don't need to deal with this here, it's post-calculated
            case COLOR_NEWTON_STRIPES: color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius)) % color.num_colors, ((double) (val + 1) / iterations)); color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) Math.abs(val - 1) / iterations)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts());
                break;
            case COLOR_NEWTON_NORMALIZED: color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) (val + 1) / iterations)); color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * (int) escape_radius) % color.num_colors), ((double) Math.abs(val - 1) / iterations)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, val, iterations, color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, val, iterations, color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, val, iterations, color.isByParts()); break;
            case COLOR_NEWTON_CLASSIC: color1 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) (val + 1) / iterations)); color3 = color.getTint(color.getColor((closestRootIndex(last[0]) * color.color_density) % color.num_colors), ((double) Math.abs(val - 1) / iterations)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, val, iterations, color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, val, iterations, color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, val, iterations, color.isByParts()); break;
            case CURVATURE_AVERAGE_LINEAR:
            case CURVATURE_AVERAGE_SPLINE: lbnd = -Math.PI; ubnd = Math.PI; index = color.createIndex(escape_radius, lbnd, ubnd, scaling); if (color.getMode() == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR) {
                colortmp = getInterpolated(index, smoothcount);
            } else {colortmp = color.splineInterpolated(index, smoothcount - ((long) smoothcount));} break;
            case STRIPE_AVERAGE_LINEAR: case STRIPE_AVERAGE_SPLINE:
                //min value of 0.5*sin(x)+0.5, min value of sin(x)=-1,max value of 0.5*sin(x)+0.5, max value of sin(x)=1
                index = color.createIndex(escape_radius, lbnd, ubnd, scaling); if (color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR) {
                    colortmp = getInterpolated(index, smoothcount);
                } else {colortmp = color.splineInterpolated(index, smoothcount - ((long) smoothcount));} break;
            case TRIANGLE_AREA_INEQUALITY_LINEAR: colortmp = getInterpolated(color.createIndex(escape_radius, lbnd, ubnd, scaling), smoothcount); break;
            case TRIANGLE_AREA_INEQUALITY_SPLINE: colortmp = color.splineInterpolated(color.createIndex(escape_radius, lbnd, ubnd, scaling), smoothcount - ((long) smoothcount)); break;
            case EPSILON_CROSS_LINEAR: case EPSILON_CROSS_SPLINE:
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
                if (color.getMode() == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR) {
                    colortmp = getInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount);
                } else {
                    colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount);
                }
                } else {
                    smoothcount = Math.log(1 + escape_radius);
                if (color.getMode() == Colors.CALCULATIONS.EPSILON_CROSS_LINEAR) {
                    colortmp = getInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount);
                } else {
                    colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount);
                }
                } break;
            case GAUSSIAN_INT_DISTANCE_LINEAR: colortmp = getInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount); break;
            case GAUSSIAN_INT_DISTANCE_SPLINE: colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount); break;
            case ORBIT_TRAP_AVG: case ORBIT_TRAP_MAX: case ORBIT_TRAP_MIN: case LINE_TRAP_MIN: case LINE_TRAP_MAX:
            case LINE_TRAP_AVG: colortmp = color.splineInterpolated(color.createIndex(escape_radius - (long) escape_radius, lbnd, ubnd, scaling), smoothcount - (long) smoothcount); break;
            case DOMAIN_COLORING: colortmp = new HSL(HSL.hueFromAngle(last[0].arg() + Math.PI), last[0].modulus() / (2 * modulusForPhase(last[0].arg())), Math.min(Math.abs(last[0].imaginary()), Math.abs(last[0].real())) / Math.max(Math.abs(last[0].imaginary()), Math.abs(last[0].real()))).toRGB(); break;
            case DOMAIN_COLORING_FAUX: colortmp = new HSL(HSL.hueFromAngle(last[0].arg() + Math.PI), last[0].modulus() / (2 * escape_radius), Math.min(Math.abs(last[0].imaginary()), Math.abs(last[0].real())) / Math.max(Math.abs(last[0].imaginary()), Math.abs(last[0].real()))).toRGB(); break;
            default: throw new IllegalArgumentException("invalid argument");
        } return colortmp;
    }
    private int getInterpolated(int index, double smoothcount) {
        int color1, color2, color3, colortmp1, colortmp2, colortmp,
                index2 = MathUtils.boundsProtected(index + 1, color.getNum_colors()); color1 = color.getColor(index);
        color2 = color.getColor(index2); color3 = MathUtils.boundsProtected(index - 1, color.getNum_colors());
        colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((long) smoothcount), color.isByParts());
        colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((long) smoothcount), color.isByParts());
        colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((long) smoothcount), color.isByParts());
        return colortmp;
    }
    public int[] toCooordinates(Complex point) {
        point = ComplexOperations.subtract(point, centre_offset); if (params.initParams.skew != 0) {
            Matrix coords = new Matrix(new double[][]{{point.real()}, {point.imaginary()}});
            Matrix rotor = Matrix.rotationMatrix2D(params.initParams.skew).inverse();
            coords = MatrixOperations.multiply(rotor, coords); point = new Complex(coords.get(0, 0), coords.get(1, 0));
        } int x = (int) ((point.real() * scale) + center_x), y = (int) (center_y - (point.imaginary() * scale));
        x = MathUtils.boundsProtected(x, argand.getWidth()); y = MathUtils.boundsProtected(y, argand.getHeight());
        return new int[]{x, y};
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {zoom(zoom.centre_x, zoom.centre_y, zoom.level);} else {zoom(zoom.centre, zoom.level);}
    }
    public void mandelbrotToJulia(Matrix constant, double level) {zoom(constant, level); changeMode(centre_offset); resetCentre();}
    private void changeMode(Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == Mode.BUDDHABROT || mode == Mode.RUDYBROT) ? Mode.JULIABROT : ((mode == Mode.MANDELBROT || mode == Mode.RUDY) ? Mode.JULIA : mode));
    }
    public void zoom(Matrix centre_offset, double level) {
        zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level);
    }
    public void zoom(Complex centre_offset, double level) {
        setCentre_offset(centre_offset); setZoom_factor(level); setScale(base_precision * Math.pow(zoom, zoom_factor));
        populateMap();
    }
    public void mandelbrotToJulia(int cx, int cy, double level) {zoom(cx, cy, level); changeMode(centre_offset); resetCentre();}
    public void zoom(int cx, int cy, double level) {
        cx = MathUtils.boundsProtected(cx, argand.getWidth()); cy = MathUtils.boundsProtected(cy, argand.getHeight());
        setCentre_offset(fromCooordinates(cx, cy)); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor)); populateMap();
    }
    public void mandelbrotToJulia(Complex constant, double level) {zoom(constant, level); changeMode(centre_offset); resetCentre();}
    public void mandelbrotToJulia(ZoomParams zoom) {zoom(zoom); changeMode(centre_offset); resetCentre();}
    @Override
    public void pan(int distance, double angle) {pan(distance, angle, false);}
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(center_x + x_dist, center_y + y_dist, zoom_factor); int histogram_length = histogram.length;
        histogram = new int[histogram_length]; int[][] tmp_escapes = new int[escapedata.length][escapedata[0].length];
        double[][] tmp_normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        ImageData tmp_argand = new LinearizedImageData(argand);
        for (int i = 0; i < tmp_escapes.length && i < tmp_normalized_escapes.length; i++) {
            System.arraycopy(escapedata[i], 0, tmp_escapes[i], 0, tmp_escapes[i].length);
            System.arraycopy(normalized_escapes[i], 0, tmp_normalized_escapes[i], 0, tmp_normalized_escapes[i].length);
        } argand = new ImageData(tmp_argand.getWidth(), tmp_argand.getHeight());
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