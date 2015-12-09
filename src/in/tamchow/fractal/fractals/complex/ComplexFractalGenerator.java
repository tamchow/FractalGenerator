package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.ComplexOperations;
import in.tamchow.fractal.math.complex.FunctionEvaluator;
import in.tamchow.fractal.math.symbolics.Polynomial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
/**
 * The actual fractal plotter for Julia, Newton and Mandelbrot Sets using an iterative algorithm.
 * The Buddhabrot technique (naive algorithm) is also implemented (of sorts) for all modes.
 * Various (10) Coloring modes (2 have been commented out as they produce output similar to an enabled option)
 */
public class ComplexFractalGenerator implements Serializable {
    public static final int MODE_MANDELBROT = 0, MODE_JULIA = 1, MODE_NEWTON = 2, MODE_BUDDHABROT = 3, MODE_NEWTONBROT = 4, MODE_JULIABROT = 5;
    ColorConfig color;
    ArrayList<Complex> roots;
    double zoom, zoom_factor, base_precision, scale;
    int center_x, center_y, mode;
    double degree, tolerance;
    long maxiter;
    ImageData argand;
    String function;
    String[][] consts;
    int[][] escapedata;
    Complex[][] argand_map;
    Complex centre_offset;
    boolean advancedDegree;
    private String variableCode;
    public ComplexFractalGenerator(ComplexFractalParams params) {
        initFractal(params.initParams.width, params.initParams.height, params.initParams.zoom, params.initParams.zoom_factor, params.initParams.base_precision, params.initParams.fractal_mode, params.initParams.function, params.initParams.consts, params.initParams.variableCode, params.initParams.tolerance, params.initParams.color);
    }
    private void initFractal(int width, int height, double zoom, double zoom_factor, double base_precision, int mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color) {
        setZoom(zoom);
        setZoom_factor(zoom_factor);
        setFunction(function);
        setBase_precision(base_precision);
        setConsts(consts);
        setScale((int) (base_precision * Math.pow(zoom, zoom_factor)));
        argand = new ImageData(width, height);
        setMode(mode);
        if (mode == MODE_MANDELBROT) {
            consts[0][0] = "c";
            consts[0][1] = "0,+0i";
        } resetCentre();
        setMaxiter(argand.getHeight() * argand.getWidth());
        argand_map = new Complex[argand.getHeight()][argand.getWidth()];
        populateMap();
        escapedata = new int[argand.getHeight()][argand.getWidth()];
        setVariableCode(variableCode);
        setTolerance(tolerance);
        roots = new ArrayList<>();
        setColor(color);
        setDegree(-1);
        setAdvancedDegree(true);
    }
    private void populateMap() {
        for (int i = 0; i < argand.getHeight(); i++) {
            for (int j = 0; j < argand.getWidth(); j++) {
                argand_map[i][j] = fromCooordinates(j, i);
            }
        }
    }
    public Complex fromCooordinates(int x, int y) {
        return ComplexOperations.add(centre_offset, new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale)));
    }
    public void resetCentre() {
        setCenter_x(argand.getWidth() / 2); setCenter_y(argand.getHeight() / 2);
        centre_offset = new Complex(Complex.ZERO);
    }
    public ComplexFractalGenerator(int width, int height, double zoom, double zoom_factor, double base_precision, int mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color) {
        initFractal(width, height, zoom, zoom_factor, base_precision, mode, function, consts, variableCode, tolerance, color);
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
    public double getDegree() {
        return degree;
    }
    public synchronized void setDegree(double degree) {
        this.degree = degree;
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
            for (int j = 0; j < argand_map[0].length; j++) {
                this.argand_map[i][j] = new Complex(argand_map[i][j]);
            }
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
    /**
     * @param nx:No.   of threads horizontally
     * @param ix:Index of thread horizontally
     * @param ny:No.   of threads vertically
     * @param iy:Index of thread vertically
     * @return the start and end coordinates for a particular thread's rendering region
     */
    public int[] start_end_coordinates(int nx, int ix, int ny, int iy) {//for multithreading purposes
        int start_x, end_x, start_y, end_y;
        int x_dist = argand.getWidth() / nx, y_dist = argand.getHeight() / ny;
        if (ix == (nx - 1)) {
            start_x = (nx - 1) * x_dist;
            end_x = argand.getWidth();
        } else {
            start_x = ix * x_dist;
            end_x = (ix + 1) * x_dist;
        }
        if (iy == (ny - 1)) {
            start_y = (ny - 1) * y_dist;
            end_y = argand.getHeight();
        } else {
            start_y = iy * y_dist;
            end_y = (iy + 1) * y_dist;
        }
        return new int[]{start_x, end_x, start_y, end_y};
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
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
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
            generate(params.runParams.start_x, params.runParams.end_x, params.runParams.start_y, params.runParams.end_y, params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        } else {
            generate(params.runParams.iterations, params.runParams.escape_radius, params.runParams.constant);
        }
    }
    public void generate(int iterations, double escape_radius, Complex constant) {
        generate(0, argand.getWidth(), 0, argand.getHeight(), iterations, escape_radius, constant);
    }
    public void generate(int iterations, double escape_radius) {
        generate(0, argand.getWidth(), 0, argand.getHeight(), iterations, escape_radius, null);
    }
    public void generate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, Complex constant) {
        setMaxiter((end_x - start_x) * (end_y - start_y) * iterations);
        if (mode != MODE_NEWTON && degree == -1) {
            degree = new FunctionEvaluator(variableCode, consts, advancedDegree).getDegree(function);
        }
        switch (mode) {
            case MODE_MANDELBROT:
            case MODE_BUDDHABROT:
                mandelbrotGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius);
                break;
            case MODE_JULIA:
            case MODE_JULIABROT:
                juliaGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius);
                break;
            case MODE_NEWTON:
            case MODE_NEWTONBROT:
                if (constant == null) {
                    newtonGenerate(start_x, end_x, start_y, end_y, iterations);
                } else {
                    newtonGenerate(start_x, end_x, start_y, end_y, iterations, constant);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown fractal render mode");
        }
    }
    private boolean isInBounds(Complex val) {
        if (val.imaginary() <= argand_map[0][center_x].imaginary() && val.imaginary() >= argand_map[argand_map.length - 1][center_x].imaginary()) {
            if (val.real() <= argand_map[center_y][argand_map[0].length - 1].real() && val.real() >= argand_map[center_y][0].real()) {
                return true;
            }
        }
        return false;
    }
    public void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        Stack<Complex> last = new Stack<>();
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        Polynomial poly; String functionderiv = "";
        if (Colors.CALCULATIONS.DISTANCE_ESTIMATION == color.mode) {
            poly = Polynomial.fromString(function);
            function = poly.toString();
            functionderiv = poly.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = new Complex(Complex.ZERO);
                Complex zd = new Complex(Complex.ZERO);
                setFirstConstant(this.consts[0][0], argand_map[i][j].toString());
                fe.setZ_value(z.toString());
                fe.setConstdec(this.consts);
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    fed.setZ_value(zd.toString());
                    fed.setConstdec(this.consts);
                }
                int c = 0x0;
                last.push(z);
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function);
                    Complex ztmpd = null;
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        ztmpd = fed.evaluate(functionderiv);
                    }
                    last.push(ztmp);
                    if (ztmp.equals(z)) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                    }
                    System.out.println(ctr + " iterations of " + maxiter);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    pass[1] = new Complex(zd);
                }
                escapedata[i][j] = c;
                if (mode == MODE_BUDDHABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, escape_radius, iterations));
                } else {
                    argand.setPixel(i, j, getColor(c, pass, escape_radius, iterations));
                }
                last.clear();
            }
        }
    }
    public synchronized void setFirstConstant(String name, String value) {
        this.consts[0][0] = name;
        this.consts[0][1] = value;
    }
    public void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, Complex constant) {
        Polynomial polynomial = Polynomial.fromString(function);
        function = polynomial.toString(); Stack<Complex> last = new Stack<>();
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        degree = polynomial.getDegree();
        String functionderiv = "";
        if (constant.equals(Complex.ZERO)) {
            constant = new Complex("" + (1 / degree));
        }
        if (Colors.CALCULATIONS.DISTANCE_ESTIMATION == color.mode) {
            functionderiv = polynomial.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j];
                Complex zd = argand_map[i][j]; int c = 0x0;
                fe.setZ_value(z.toString());
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    fed.setZ_value(zd.toString());
                }
                last.push(z);
                while (c <= iterations) {
                    Complex ztmp = ComplexOperations.subtract(z, ComplexOperations.multiply(constant, ComplexOperations.divide(fe.evaluate(function), fe.evaluate(polynomial.derivative().toString()))));
                    Complex ztmpd = null;
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        ztmpd = ComplexOperations.subtract(zd, ComplexOperations.multiply(constant, ComplexOperations.divide(fed.evaluate(functionderiv), fed.evaluate(polynomial.derivative().derivative().toString()))));
                    }
                    if (z.equals(Complex.ZERO)) {
                        c = iterations;
                        break;
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) < tolerance) {
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                    }
                    System.out.println(ctr + " iterations of " + maxiter);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                if (!containsRoot(z)) {
                    roots.add(z);
                }
                double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                pass[0] = new Complex(z);
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    pass[1] = new Complex(zd);
                }
                escapedata[i][j] = c;
                if (mode == MODE_NEWTONBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, root_reached, iterations));
                } else {
                    argand.setPixel(i, j, getColor(c, pass, root_reached, iterations));
                }
                last.clear();
            }
        }
    }
    public void newtonGenerate(int start_x, int end_x, int start_y, int end_y, int iterations) {
        Polynomial polynomial = Polynomial.fromString(function);
        function = polynomial.toString(); Stack<Complex> last = new Stack<>();
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        degree = polynomial.getDegree();
        String functionderiv = "";
        if (Colors.CALCULATIONS.DISTANCE_ESTIMATION == color.mode) {
            functionderiv = polynomial.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j];
                Complex zd = argand_map[i][j]; int c = 0x0;
                fe.setZ_value(z.toString());
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    fed.setZ_value(zd.toString());
                }
                last.push(z);
                while (c <= iterations) {
                    Complex ztmp = ComplexOperations.subtract(z, ComplexOperations.divide(fe.evaluate(function), fe.evaluate(polynomial.derivative().toString())));
                    Complex ztmpd = null;
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        ztmpd = ComplexOperations.subtract(zd, ComplexOperations.divide(fed.evaluate(functionderiv), fed.evaluate(polynomial.derivative().derivative().toString())));
                    }
                    if (z.equals(Complex.ZERO)) {
                        c = iterations;
                        break;
                    }
                    if (ComplexOperations.distance_squared(z, ztmp) < tolerance) {
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(ztmpd.toString());
                    }
                    System.out.println(ctr + " iterations of " + maxiter);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                if (!containsRoot(z)) {
                    roots.add(z);
                }
                double root_reached = ComplexOperations.divide(ComplexOperations.principallog(argand_map[i][j]), ComplexOperations.principallog(z)).modulus();
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                pass[0] = new Complex(z);
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    pass[1] = new Complex(zd);
                }
                escapedata[i][j] = c;
                if (mode == MODE_NEWTONBROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, root_reached, iterations));
                } else {
                    argand.setPixel(i, j, getColor(c, pass, root_reached, iterations));
                }
                last.clear();
            }
        }
    }
    private synchronized boolean containsRoot(Complex z) {
        for (Complex c : roots) {
            if (ComplexOperations.distance_squared(c, z) < tolerance) {
                return true;
            }
        }
        return false;
    }
    private synchronized int indexOfRoot(Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (ComplexOperations.distance_squared(roots.get(i), z) < tolerance) {
                return i;
            }
        }
        return -1;
    }
    public void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius) {
        Stack<Complex> last = new Stack<>();
        FunctionEvaluator fe = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        Polynomial poly; String functionderiv = "";
        if (Colors.CALCULATIONS.DISTANCE_ESTIMATION == color.mode) {
            poly = Polynomial.fromString(function);
            function = poly.toString();
            functionderiv = poly.derivative().toString();
        }
        FunctionEvaluator fed = new FunctionEvaluator(Complex.ZERO.toString(), variableCode, consts, advancedDegree);
        long ctr = 0;
        outer:
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j];
                Complex zd = argand_map[i][j]; int c = 0x0;
                fe.setZ_value(z.toString());
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    fed.setZ_value(zd.toString());
                }
                last.push(z);
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function);
                    Complex ztmpd = null;
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        ztmpd = fed.evaluate(functionderiv);
                    }
                    last.push(ztmp);
                    if (ztmp.equals(z)) {
                        c = iterations;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                        zd = new Complex(ztmpd);
                        fed.setZ_value(zd.toString());
                    }
                    System.out.println(ctr + " iterations of " + maxiter);
                    c++;
                    if (ctr > maxiter) {
                        break outer;
                    }
                    ctr++;
                }
                Complex[] pass = new Complex[3];
                for (int k = 0; k < last.size() && k < pass.length; k++) {
                    pass[k] = last.pop();
                }
                if (last.size() < 3) {
                    for (int m = last.size(); m < pass.length; m++) {
                        pass[m] = new Complex(Complex.ZERO);
                    }
                }
                if (color.mode == Colors.CALCULATIONS.DISTANCE_ESTIMATION) {
                    pass[1] = new Complex(zd);
                }
                escapedata[i][j] = c;
                if (mode == MODE_JULIABROT) {
                    argand.setPixel(toCooordinates(z)[1], toCooordinates(z)[0], argand.getPixel(toCooordinates(z)[1], toCooordinates(z)[0]) + getColor(c, pass, escape_radius, iterations));
                } else {
                    argand.setPixel(i, j, getColor(c, pass, escape_radius, iterations));
                }
                last.clear();
            }
        }
    }
    public ColorConfig getColor() {
        return color;
    }
    public void setColor(ColorConfig color) {
        this.color = new ColorConfig(color);
    }
    public synchronized int getColor(int val, Complex[] last, double escape_radius, int iterations) {
        int colortmp, color1, color2;
        double renormalized = ((val + 1) - (Math.log(Math.log(last[0].modulus() / Math.log(escape_radius)) / Math.log(degree))));
        double lbnd, ubnd, calc;
        switch (color.getMode()) {
            case Colors.CALCULATIONS.SIMPLE: colortmp = color.getColor((val * (iterations * color.color_density)) % color.num_colors); break;
            case Colors.CALCULATIONS.SIMPLE_SMOOTH: color1 = color.getColor((val * (iterations * color.color_density)) % color.num_colors); color2 = color.getColor(((val + 1) * (iterations * color.color_density)) % color.num_colors);
                //colortmp=ColorConfig.linearInterpolated(color1,color2, val, iterations);
                colortmp = ColorConfig.linearInterpolated(color1, color2, renormalized - ((int) renormalized)); break;
            case Colors.CALCULATIONS.COLOR_DIVIDE:
                color1 = (int) (0xffffff / renormalized);
                color2 = (int) (0xffffff / (renormalized + 1));
                colortmp = ColorConfig.linearInterpolated(color1, color2, renormalized - ((int) renormalized));
                break;
            case Colors.CALCULATIONS.COLOR_HIGH_CONTRAST:
                colortmp = (iterations * val) << 16 | (iterations * val) << 8 | (iterations * val);
                break;
            case Colors.CALCULATIONS.COLOR_MULTIPLY:
                color1 = (int) renormalized << 16 | (int) renormalized << 8 | (int) renormalized;
                color2 = (int) (renormalized + 1) << 16 | (int) (renormalized + 1) << 8 | (int) (renormalized + 1);
                colortmp = ColorConfig.linearInterpolated(color1, color2, renormalized - ((int) renormalized));
                break;
            case Colors.CALCULATIONS.COLOR_GRAYSCALE:
                colortmp = val << 16 | val << 8 | val;
                break;
            case Colors.CALCULATIONS.DISTANCE_ESTIMATION:
                double distance = last[0].modulus() * Math.log(last[0].modulus()) / Math.log(last[1].modulus());
                colortmp = (distance > escape_radius) ? 0xffffff : 0x000000;
                break;
            /*case ColorMode.COLOR_MULTIPLY_3:
                color1=((int)renormalized<<16)<<16|((int)renormalized<<8)<<8|(int)renormalized;
                color2=((int)(renormalized+1)<<16)<<16|((int)(renormalized+1)<<8)<<8|(int)(renormalized+1);
                color=ColorConfig.linearInterpolated(color1,color2,renormalized-((int)renormalized));
                break;
            case ColorMode.COLOR_MULTIPLY_4:
                color1=((int)renormalized)<<16|((int)renormalized<<8)<<8|((int)renormalized<<16);
                color2=((int)(renormalized+1))<<16|((int)(renormalized+1)<<8)<<8|((int)(renormalized+1)<<16);
                color=ColorConfig.linearInterpolated(color1,color2,renormalized-((int)renormalized));
                break;
                */
            case Colors.CALCULATIONS.COLOR_NEWTON_1:
                /*if(indexOfRoot(last[0])>0) {*/
                color1 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) val / iterations)); color2 = color.getTint(color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors), ((double) (val + 1) / iterations)); colortmp = ColorConfig.linearInterpolated(color1, color2, val, iterations);
                /*}else {
                color = ColorConfig.linearInterpolated(0xffffff, color.getColor((indexOfRoot(last[0]) * color.color_density) % color.num_colors),((double) val / iterations));
                }*/
                break;
            case Colors.CALCULATIONS.COLOR_NEWTON_2:
                colortmp = ColorConfig.linearInterpolated(0xffffff, color.getColor((int) (escape_radius * color.color_density) % color.num_colors), renormalized - ((int) renormalized));
                break;
            case Colors.CALCULATIONS.CURVATURE_AVERAGE:
                lbnd = -Math.PI;
                ubnd = Math.PI;
                if (last[1].equals(Complex.ZERO) && last[2] == Complex.ZERO) {
                    calc = Math.PI / 2;
                } else {
                    calc = Math.abs(ComplexOperations.divide(ComplexOperations.subtract(last[0], last[1]), ComplexOperations.subtract(last[1], last[2])).arg());
                }
                colortmp = color.splineInterpolated(color.createIndex(calc, lbnd, ubnd), renormalized - (int) renormalized);
                break;
            case Colors.CALCULATIONS.STRIPE_AVERAGE:
                lbnd = 0.0;//min value of 0.5*sin(x)+0.5, min value of sin(x)=-1
                ubnd = 1.0;//max value of 0.5*sin(x)+0.5, max value of sin(x)=1
                calc = 0.5 * Math.sin(color.color_density * last[0].arg()) + 0.5;
                colortmp = color.splineInterpolated(color.createIndex(calc, lbnd, ubnd), renormalized - (int) renormalized);
                break;
            case Colors.CALCULATIONS.TRIANGLE_AREA_INEQUALITY:
                lbnd = Math.abs(ComplexOperations.power(last[1], new Complex(degree)).modulus() - new Complex(consts[0][1]).modulus());
                ubnd = ComplexOperations.power(last[1], new Complex(degree)).modulus() + new Complex(consts[0][1]).modulus();
                calc = (last[0].modulus() - lbnd) / (ubnd - lbnd);
                colortmp = color.splineInterpolated(color.createIndex(calc, lbnd, ubnd), renormalized - (int) renormalized);
                break;
            default:
                throw new IllegalArgumentException("invalid argument");
        }
        return colortmp;
    }
    public int[] toCooordinates(Complex point) {
        int x = (int) ((point.real() * scale) + center_x), y = (int) (center_y - (point.imaginary() * scale));
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x >= argand.getWidth()) {
            x = argand.getWidth() - 1;
        }
        if (y >= argand.getHeight()) {
            y = argand.getHeight() - 1;
        }
        return new int[]{x, y};
    }
    public void zoom(int cx, int cy, double level) {
        if (cx < 0) {
            cx = 0;
        }
        if (cy < 0) {
            cy = 0;
        }
        if (cx >= argand.getWidth()) {
            cx = argand.getWidth() - 1;
        }
        if (cy >= argand.getHeight()) {
            cy = argand.getHeight() - 1;
        }
        setCentre_offset(fromCooordinates(cx, cy));
        setZoom_factor(level); setScale(base_precision * Math.pow(zoom, zoom_factor));
        populateMap();
    }
    public void resetBasePrecision() {
        setBase_precision((argand.getHeight() >= argand.getWidth()) ? argand.getWidth() / 2 : argand.getHeight() / 2);
    }
}