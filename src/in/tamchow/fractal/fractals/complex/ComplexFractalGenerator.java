package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.Printable;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.FixedStack;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.ComplexOperations;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.symbolics.Polynomial;

import java.io.Serializable;
import java.util.ArrayList;
/** The actual fractal plotter for Julia, Newton and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes.
 * Various (21) Coloring modes*/
public class ComplexFractalGenerator implements Serializable {
    public static final int MODE_MANDELBROT = 0, MODE_JULIA = 1, MODE_NEWTON = 2, MODE_BUDDHABROT = 3, MODE_NEWTONBROT = 4, MODE_JULIABROT = 5;
    ColorConfig color;
    ArrayList<Complex> roots;
    double zoom, zoom_factor, base_precision, scale;
    int center_x, center_y, mode, lastConstantIdx;
    double tolerance;
    long maxiter;
    ImageData argand;
    String function;
    String[][] consts;
    int[][] escapedata;
    Complex[][] argand_map;
    Complex centre_offset, degree, lastConstant;
    boolean advancedDegree;
    int[] histogram;
    double[][] normalized_escapes;
    Printable progressPublisher;
    private String variableCode;
    public ComplexFractalGenerator(ComplexFractalParams params, Printable progressPublisher) {
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.zoom_factor, params.initParams.base_precision, params.initParams.fractal_mode, params.initParams.function, params.initParams.consts, params.initParams.variableCode, params.initParams.tolerance, params.initParams.degree, params.initParams.color);
        if (params.zoomConfig != null) {for (ZoomParams zoom : params.zoomConfig.zooms) {zoom(zoom);}}
        this.progressPublisher = progressPublisher;
    }
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, int mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color, Printable progressPublisher) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, tolerance, new Complex(-1, 0), color);
        this.progressPublisher = progressPublisher;
    }
    private void initFractal(int width, int height, double zoom, double zoom_factor, double base_precision, int mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, ColorConfig color) {
        setZoom(zoom); setZoom_factor(zoom_factor); setFunction(function); setBase_precision(base_precision);
        setConsts(consts); setScale((int) (base_precision * Math.pow(zoom, zoom_factor)));
        argand = new ImageData(width, height); setMode(mode); resetCentre();
        setMaxiter(argand.getHeight() * argand.getWidth());
        argand_map = new Complex[argand.getHeight()][argand.getWidth()]; populateMap();
        escapedata = new int[argand.getHeight()][argand.getWidth()];
        normalized_escapes = new double[argand.getHeight()][argand.getWidth()]; setVariableCode(variableCode);
        setTolerance(tolerance);
        roots = new ArrayList<>(); setColor(color); setDegree(degree); if (degree.equals(new Complex("-1"))) {
            setAdvancedDegree(true);
        } lastConstant = new Complex(-1, 0);
    }
    public synchronized void populateMap() {
        for (int i = 0; i < argand.getHeight(); i++) {
            for (int j = 0; j < argand.getWidth(); j++) {argand_map[i][j] = fromCooordinates(j, i);}
        }
    }
    public Complex fromCooordinates(int x, int y) {
        return ComplexOperations.add(centre_offset, new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale)));
    }
    public void resetCentre() {
        setCenter_x(argand.getWidth() / 2); setCenter_y(argand.getHeight() / 2);
        centre_offset = new Complex(Complex.ZERO);
    }
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
    public synchronized void setMaxiter(long maxiter) {
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
    public synchronized void setDegree(Complex degree) {
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
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    /**@param nx:No.   of threads horizontally
     * @param ix:Index of thread horizontally
     * @param ny:No.   of threads vertically
     * @param iy:Index of thread vertically
     * @return the start and end coordinates for a particular thread's rendering region*/
    public int[] start_end_coordinates(int nx, int ix, int ny, int iy) {//for multithreading purposes
        int start_x, end_x, start_y, end_y; int x_dist = argand.getWidth() / nx, y_dist = argand.getHeight() / ny;
        if (ix == (nx - 1)) {
            start_x = (nx - 1) * x_dist; end_x = argand.getWidth();
        } else {start_x = ix * x_dist; end_x = (ix + 1) * x_dist;} if (iy == (ny - 1)) {
            start_y = (ny - 1) * y_dist; end_y = argand.getHeight();
        } else {start_y = iy * y_dist; end_y = (iy + 1) * y_dist;} return new int[]{start_x, end_x, start_y, end_y};
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
        this.base_precision = base_precision;
    }
    public ImageData getArgand() {
        return argand;
    }
    public void setArgand(ImageData argand) {
        this.argand = new ImageData(argand);
    }
    public String[][] getConsts() {
        return consts;
    }
    public synchronized void setConsts(String[][] consts) {
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
        if (mode != MODE_NEWTON && (color.getMode() != Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() != Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) && degree.equals(new Complex(-1, 0))) {
            degree = new FunctionEvaluator(variableCode, consts, advancedDegree).getDegree(function);
        }
        if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
            histogram = new int[iterations + 2];
        } switch (mode) {
            case MODE_MANDELBROT:
            case MODE_BUDDHABROT: mandelbrotGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius); break;
            case MODE_JULIA:
            case MODE_JULIABROT: juliaGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius); break;
            case MODE_NEWTON:
            case MODE_NEWTONBROT: newtonGenerate(start_x, end_x, start_y, end_y, iterations, constant); break;
            default: throw new IllegalArgumentException("Unknown fractal render mode");
        }
        if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
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
    private synchronized boolean isInBounds(Complex val) {
        if (val.imaginary() <= argand_map[0][center_x].imaginary() && val.imaginary() >= argand_map[argand_map.length - 1][center_x].imaginary()) {
            if (val.real() <= argand_map[center_y][argand_map[0].length - 1].real() && val.real() >= argand_map[center_y][0].real()) {
                return true;
            }
        } return false;
    }
    private synchronized Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {lastConstant = new Complex(consts[getLastConstantIndex()][1]);}
        return lastConstant;
    }
    private synchronized void setLastConstant(Complex value) {consts[lastConstantIdx][1] = value.toString();}
    private synchronized int getConstantIndex(String constant) {
        for (int i = 0; i < consts.length; i++) {if (consts[i][0].equals(constant)) {return i;}} return -1;}
    private synchronized int getLastConstantIndex() {
        String[] parts = function.split(" "); for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                lastConstantIdx = getConstantIndex(parts[i]); return getConstantIndex(parts[i]);}} return -1;}
    public void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack last = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        Polynomial poly; String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            poly = Polynomial.fromString(function); poly.setConstdec(consts); poly.setVariableCode(variableCode);
            function = poly.toString(); if (degree.equals(new Complex("-1"))) {degree = poly.getDegree();}
            functionderiv = poly.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0; getLastConstant(); setLastConstant(Complex.ZERO);
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = new Complex(Complex.ZERO); Complex zd = new Complex(Complex.ONE);
                setLastConstant(argand_map[i][j]);
                fe.setZ_value(z.toString());
                fe.setConstdec(this.consts);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                    fed.setConstdec(this.consts);
                } int c = 0;
                last.push(z);
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function, false);
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = fed.evaluate(functionderiv, false);
                    }
                    last.push(ztmp);
                    if (ztmp.equals(z)) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        fed.setZ_value(zd.toString());
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
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
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = argand_map[i][j];
                } escapedata[i][j] = c; normalized_escapes[i][j] = getNormalized(c, iterations, pass[0], escape_radius);
                if (mode == MODE_BUDDHABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, escape_radius, iterations));
                } else {
                    argand.setPixel(i, j, getColor(c, pass, escape_radius, iterations));
                }
                last.clear();
            }
        }
    }
    public void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, Complex constant) {
        Polynomial polynomial = Polynomial.fromString(function); polynomial.setConstdec(consts);
        polynomial.setVariableCode(variableCode); function = polynomial.toString();
        FixedStack last = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        if (degree.equals(new Complex("-1"))) {degree = polynomial.getDegree();}
        String functionderiv = ""; if (constant != null && constant.equals(Complex.ZERO)) {
            constant = ComplexOperations.divide(Complex.ONE, degree);
        }
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            functionderiv = polynomial.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j]; Complex zd = new Complex(Complex.ONE);//argand_map[i][j]
                int c = 0; fe.setZ_value(z.toString());
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                }
                last.push(z);
                while (c <= iterations) {
                    Complex ztmp, ztmpd; if (constant != null) {
                        ztmp = ComplexOperations.subtract(z, ComplexOperations.multiply(constant, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(polynomial.derivative().toString(), false))));
                        ztmpd = null; if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE) {
                            ztmpd = ComplexOperations.subtract(zd, ComplexOperations.multiply(constant, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(polynomial.derivative().derivative().toString(), false))));
                        }} else {
                        ztmp = ComplexOperations.subtract(z, ComplexOperations.divide(fe.evaluate(function, false), fe.evaluate(polynomial.derivative().toString(), false)));
                        ztmpd = null; if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE) {
                            ztmpd = ComplexOperations.subtract(zd, ComplexOperations.divide(fed.evaluate(functionderiv, false), fed.evaluate(polynomial.derivative().derivative().toString(), false)));
                        }
                    } if (z.equals(Complex.ZERO)) {c = iterations; break;}
                    if (ComplexOperations.distance_squared(z, ztmp) < tolerance) {break;}
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;
                } if (indexOfRoot(z) == -1) {roots.add(z);}
                double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {pass[k] = last.pop();} if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                } pass[0] = new Complex(z);
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = new Complex(centre_offset);
                } escapedata[i][j] = c; normalized_escapes[i][j] = getNormalized(c, iterations, pass[0],root_reached);
                if (mode == MODE_NEWTONBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, root_reached, iterations));
                } else {argand.setPixel(i, j, getColor(c, pass, root_reached, iterations));} last.clear();}}}
    public synchronized void publishProgress(long ctr, int i, int startx, int endx, int j, int starty, int endy) {
        float completion = ((float) (i * (endx - startx) + j) / ((endx - startx) * (endy - starty))) * 100.0f;
        progressPublisher.println(ctr + " iterations of " + maxiter + ",completion = " + completion + "%");
    }
    private synchronized int indexOfRoot(Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (ComplexOperations.distance_squared(roots.get(i), z) < tolerance) {return i;}}return -1;}
    public void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        FixedStack last = new FixedStack(iterations + 2);
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        Polynomial poly; String functionderiv = "";
        if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
            poly = Polynomial.fromString(function); poly.setConstdec(consts); poly.setVariableCode(variableCode);
            function = poly.toString(); if (degree.equals(new Complex("-1"))) {degree = poly.getDegree();}
            functionderiv = poly.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0; outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j]; Complex zd = new Complex(Complex.ONE);//argand_map[i][j];
                int c = 0x0; fe.setZ_value(z.toString());
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    fed.setZ_value(zd.toString());
                }
                last.push(z);
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function, false);
                    Complex ztmpd = null;
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        ztmpd = fed.evaluate(functionderiv, false);
                    } last.push(ztmp); if (ztmp.equals(z)) {c = iterations; break;} z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                        zd = new Complex(ztmpd); fed.setZ_value(zd.toString());
                    } publishProgress(ctr, i, start_x, end_x, j, start_y, end_y); c++; if (ctr > maxiter) {break outer;}
                    ctr++;
                }
                if (color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    histogram[c]++;}
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {pass[k] = last.pop();} if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE || color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR) {
                    pass[1] = new Complex(zd); pass[2] = new Complex(centre_offset);
                }
                escapedata[i][j] = c; normalized_escapes[i][j] = getNormalized(c, iterations, pass[0], escape_radius);
                if (mode == MODE_JULIABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, escape_radius, iterations));
                } else {argand.setPixel(i, j, getColor(c, pass, escape_radius, iterations));} last.clear();
            }
        }
    }
    public ColorConfig getColor() {return color;}
    public void setColor(ColorConfig color) {this.color = new ColorConfig(color);}
    public synchronized double getNormalized(int val, int iterations, Complex z, double escape) {
        double renormalized; if (!color.isLogIndex()) {
            if (degree.equals(Complex.ZERO) || degree.equals(Complex.ONE)) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = (val + 1) - (Math.log(Math.log(z.modulus()) / Math.log(escape)) / ComplexOperations.principallog(degree).modulus());
                if (renormalized - (int) renormalized == 0) {renormalized += ((double) val / iterations);}
            }
        } else {
            if (mode == MODE_BUDDHABROT || mode == MODE_MANDELBROT) {
            if (degree.equals(Complex.ZERO) || degree.equals(Complex.ONE)) {
                renormalized = val + ((double) val / iterations);
            } else {
                renormalized = (val + 1) - (Math.log(Math.log(z.modulus()) / Math.log(escape)) / ComplexOperations.principallog(degree).modulus());
                if (renormalized - (int) renormalized == 0) {renormalized += ((double) val / iterations);}
            }
            } else {renormalized = val + (0.5 + 0.5 * (Math.sin(z.arg()) * color.color_density));}
        } return renormalized;
    }
    public int[] getHistogram() {return histogram;}
    public double[][] getNormalized_escapes() {return normalized_escapes;}
    public int getLastConstantIdx() {return lastConstantIdx;}
    public synchronized int getColor(int val, Complex[] last, double escape_radius, int iterations) {
        int colortmp, colortmp1, colortmp2, color1, color2, color3, index, index2; double renormalized;
        double lbnd, ubnd, calc, scaling = Math.pow(zoom, zoom_factor);
        renormalized = getNormalized(val, iterations, last[0], escape_radius);
        double smoothcount = (renormalized > 0) ? Math.abs(Math.log(renormalized)) : ComplexOperations.principallog(new Complex(renormalized, 0)).modulus();
        switch (color.getMode()) {
            case Colors.CALCULATIONS.SIMPLE: colortmp = color.getColor((val * color.color_density) % color.num_colors); break;
            case Colors.CALCULATIONS.SIMPLE_SMOOTH: color1 = color.getColor((val * (iterations * color.color_density)) % color.num_colors); color2 = color.getColor(((val + 1) * (iterations * color.color_density)) % color.num_colors); color3 = color.getColor(((val - 1) * (iterations * color.color_density)) % color.num_colors); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts());
                break;
            case Colors.CALCULATIONS.COLOR_DIVIDE_DIRECT: color1 = (0xffffff / val); color2 = (0xffffff / (val + 1)); color3 = (0xffffff / (val - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts()); break;
            case Colors.CALCULATIONS.COLOR_DIVIDE_NORMALIZED: color1 = (int) (0xffffff / renormalized); color2 = (int) (0xffffff / (renormalized + 1)); color3 = (int) (0xffffff / (renormalized - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts()); break;
            case Colors.CALCULATIONS.COLOR_GRAYSCALE_HIGH_CONTRAST: colortmp = (iterations * val) << 16 | (iterations * val) << 8 | (iterations * val); break;
            case Colors.CALCULATIONS.SIMPLE_DISTANCE_ESTIMATION: calc = Math.abs((double) val / iterations); if (calc > 1) {
                calc = calc - 1;
            } colortmp1 = (int) (calc * 255); if (calc > 0.5) {
                colortmp = ColorConfig.toRGB(colortmp1, 255, colortmp1);
            } else {
                colortmp = ColorConfig.toRGB(0, colortmp1, 0);
            } break;
            case Colors.CALCULATIONS.COLOR_MULTIPLY_DIRECT: color1 = val << 16 | val << 8 | val; color2 = (val + 1) << 16 | (val + 1) << 8 | (val + 1); color3 = Math.abs((val - 1) << 16 | (val - 1) << 8 | (val - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts()); break;
            case Colors.CALCULATIONS.COLOR_MULTIPLY_NORMALIZED: color1 = (int) renormalized << 16 | (int) renormalized << 8 | (int) renormalized; color2 = (int) (renormalized + 1) << 16 | (int) (renormalized + 1) << 8 | (int) (renormalized + 1); color3 = Math.abs((int) (renormalized - 1) << 16 | (int) (renormalized - 1) << 8 | (int) (renormalized - 1)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts()); break;
            case Colors.CALCULATIONS.COLOR_GRAYSCALE_LOW_CONTRAST:
                colortmp = val << 16 | val << 8 | val;
                break; case Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE:
            case Colors.CALCULATIONS.DISTANCE_ESTIMATION_COLOR: double distance; if ((mode == MODE_BUDDHABROT || mode == MODE_MANDELBROT)) {
                distance = Math.abs(Math.sqrt(Math.pow(last[0].modulus(), 2) / Math.pow(last[1].modulus(), 2)) * 0.5 * Math.log(Math.pow(last[0].modulus(), 2)));
            } else {
                distance = Math.abs(last[0].modulus() * Math.log(last[0].modulus())) / Math.log(last[1].modulus());
            } if (color.getMode() == Colors.CALCULATIONS.DISTANCE_ESTIMATION_GRAYSCALE) {
                color1 = (int) ((distance - (int) distance) * 255);
                colortmp = ColorConfig.toRGB(color1, color1, color1);
            } else {
                index = color.createIndex((distance - (int) distance), 0, 1, scaling);
                colortmp = color.splineInterpolated(index, renormalized - (int) renormalized);
                /*
                if (mode == MODE_BUDDHABROT || mode == MODE_MANDELBROT) {
                    if (!color.isByParts()) {colortmp = (distance > escape_radius) ? 0xffffff : 0x000000;} else {
                        colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(last[0], last[2]))) ? 0xffffff : 0x000000;
                    }
                } else {
                    colortmp = (distance > Math.sqrt(ComplexOperations.distance_squared(centre_offset, last[2]))) ? 0xffffff : 0x000000;
                }*/
            }
                break;
            case Colors.CALCULATIONS.COLOR_HISTOGRAM:
            case Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR: colortmp = 0x000000; break;//Don't need to deal with this here, it's post-calculated
            case Colors.CALCULATIONS.COLOR_NEWTON_STRIPES:
                /*if(indexOfRoot(last[0])>0) {*/
                color1 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) (val + 1) / iterations)); color3 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) Math.abs(val - 1) / iterations)); colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts()); colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts()); colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts());/*}else {
                color = ColorConfig.linearInterpolated(0xffffff, color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors),((double) val / iterations),color.isByParts());
                }*/
                break;
            case Colors.CALCULATIONS.COLOR_NEWTON_NORMALIZED: colortmp = ColorConfig.linearInterpolated(0xffffff, color.getColor((int) (escape_radius * color.color_density) % color.num_colors), smoothcount - ((int) smoothcount), color.isByParts()); break;
            case Colors.CALCULATIONS.COLOR_NEWTON_CLASSIC: color1 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) (val + 1) / iterations)); colortmp = ColorConfig.linearInterpolated(color1, color2, val, iterations, color.isByParts()); break;
            case Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR:
            case Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE: lbnd = -Math.PI; ubnd = Math.PI;
                if (last[1].equals(Complex.ZERO) && last[2] == Complex.ZERO) {
                    calc = Math.PI / 2;
                } else {
                    calc = Math.abs(ComplexOperations.divide(ComplexOperations.subtract(last[0], last[1]), ComplexOperations.subtract(last[1], last[2])).arg());
                } index = color.createIndex(calc, lbnd, ubnd, scaling); if (color.getMode() == Colors.CALCULATIONS.CURVATURE_AVERAGE_LINEAR) {
                colortmp = getColor(index, smoothcount);
            } else {colortmp = color.splineInterpolated(index, smoothcount - ((int) smoothcount));}
                break; case Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR: case Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE:
                lbnd = 0.0;//min value of 0.5*sin(x)+0.5, min value of sin(x)=-1
                ubnd = 1.0;//max value of 0.5*sin(x)+0.5, max value of sin(x)=1
                calc = 0.5 * Math.sin(color.color_density * last[0].arg()) + 0.5; index = color.createIndex(calc, lbnd, ubnd, scaling); if (color.getMode() == Colors.CALCULATIONS.STRIPE_AVERAGE_LINEAR) {
                    colortmp = getColor(index, smoothcount);
                } else {colortmp = color.splineInterpolated(index, smoothcount - ((int) smoothcount));}
                break; case Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR:
            case Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_SPLINE: lbnd = Math.abs(ComplexOperations.power(last[1], new Complex(degree)).modulus() - getLastConstant().modulus()); ubnd = ComplexOperations.power(last[1], new Complex(degree)).modulus() + getLastConstant().modulus(); calc = (last[0].modulus() - lbnd) / (ubnd - lbnd); index = color.createIndex(calc, lbnd, ubnd, scaling); if (color.getMode() == Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY_LINEAR) {
                colortmp = getColor(index, smoothcount);
            } else {
                colortmp = color.splineInterpolated(index, smoothcount - ((int) smoothcount));
            } break; default: throw new IllegalArgumentException("invalid argument");
        } return colortmp;
    }
    private int getColor(int index, double smoothcount) {
        int color1, color2, color3, colortmp1, colortmp2, colortmp,
                index2 = (index + 1) >= color.num_colors ? index : index + 1; color1 = color.getColor(index);
        color2 = color.getColor(index2); color3 = color.getColor(Math.abs(index - 1));
        colortmp1 = ColorConfig.linearInterpolated(color1, color2, smoothcount - ((int) smoothcount), color.isByParts());
        colortmp2 = ColorConfig.linearInterpolated(color3, color1, smoothcount - ((int) smoothcount), color.isByParts());
        colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, smoothcount - ((int) smoothcount), color.isByParts());
        return colortmp;
    }
    public int[] toCooordinates(Complex point) {
        int x = (int) ((point.real() * scale) + center_x), y = (int) (center_y - (point.imaginary() * scale));
        if (x < 0) {x = 0;} if (y < 0) {y = 0;} if (x >= argand.getWidth()) {x = argand.getWidth() - 1;}
        if (y >= argand.getHeight()) {y = argand.getHeight() - 1;} return new int[]{x, y};
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {zoom(zoom.centre_x, zoom.centre_y, zoom.level);} else {zoom(zoom.centre, zoom.level);}
    }
    public void zoom(Matrix centre_offset, double level) {
        setCentre_offset(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0))); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor)); populateMap();
    }
    public void zoom(int cx, int cy, double level) {
        if (cx < 0) {cx = 0;} if (cy < 0) {cy = 0;} if (cx >= argand.getWidth()) {cx = argand.getWidth() - 1;}
        if (cy >= argand.getHeight()) {cy = argand.getHeight() - 1;} setCentre_offset(fromCooordinates(cx, cy));
        setZoom_factor(level); setScale(base_precision * Math.pow(zoom, zoom_factor)); populateMap();
    }
    public void resetBasePrecision() {
        setBase_precision((argand.getHeight() >= argand.getWidth()) ? argand.getWidth() / 2 : argand.getHeight() / 2);
    }
}