package in.tamchow.fractal.config.fractalconfig.complexbrot;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Holds parameters for a ComplexBrot fractal
 */
public class ComplexBrotFractalParams implements Serializable, DataFromString {
    public ZoomConfig zoomConfig;
    public PixelContainer.PostProcessMode postprocessMode;
    public int width, height, num_threads, switch_rate, num_points;
    public Complex newton_constant;
    public int[] iterations;
    public double zoom, zoom_level, base_precision, skew, tolerance, escape_radius;
    public String function, variableCode, oldVariableCode, path;
    public ComplexFractalGenerator.Mode mode;
    public String[][] constants;
    public boolean anti;
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, int num_points, int[] iterations, double zoom, double zoom_level, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, ComplexFractalGenerator.Mode mode, String[][] constants, boolean anti) {
        this(width, height, num_threads, switch_rate, num_points, iterations, zoom, zoom_level, base_precision, escape_radius, tolerance, skew, function, variableCode, variableCode + "_p", mode, constants, anti);
    }
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, int num_points, int[] iterations, double zoom, double zoom_level, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, ComplexFractalGenerator.Mode mode, String[][] constants, boolean anti) {
        setWidth(width);
        setHeight(height);
        setNum_threads(num_threads);
        setSwitch_rate(switch_rate);
        setNum_points(num_points);
        setIterations(iterations);
        setZoom(zoom);
        setZoom_level(zoom_level);
        setBase_precision(base_precision);
        setEscape_radius(escape_radius);
        setTolerance(tolerance);
        setSkew(skew);
        setFunction(function);
        setVariableCode(variableCode);
        setOldVariableCode(oldVariableCode);
        setMode(mode);
        setConstants(constants);
        setAnti(anti);
    }
    public ComplexBrotFractalParams() {
        setPath("");
        setPostprocessMode(PixelContainer.PostProcessMode.NONE);
    }
    public ComplexBrotFractalParams(ComplexBrotFractalParams old) {
        this(old.getWidth(), old.getHeight(), old.getNum_threads(), old.getSwitch_rate(), old.getNum_points(), old.getIterations(), old.getZoom(), old.getZoom_level(), old.getBase_precision(), old.getEscape_radius(), old.getTolerance(), old.getSkew(), old.getFunction(), old.getVariableCode(), old.getOldVariableCode(), old.getMode(), old.getConstants(), old.isAnti());
        setPath(old.getPath());
        setPostprocessMode(old.getPostprocessMode());
        setNewton_constant(old.getNewton_constant());
    }
    public double getEscape_radius() {
        return escape_radius;
    }
    public void setEscape_radius(double escape_radius) {
        this.escape_radius = escape_radius;
    }
    public PixelContainer.PostProcessMode getPostprocessMode() {
        return postprocessMode;
    }
    public void setPostprocessMode(PixelContainer.PostProcessMode postprocessMode) {
        this.postprocessMode = postprocessMode;
    }
    public Complex getNewton_constant() {
        return newton_constant;
    }
    public void setNewton_constant(Complex newton_constant) {
        this.newton_constant = newton_constant;
    }
    public int getSwitch_rate() {
        return switch_rate;
    }
    public void setSwitch_rate(int switch_rate) {
        this.switch_rate = switch_rate;
    }
    public double getTolerance() {
        return tolerance;
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
    public double getSkew() {
        return skew;
    }
    public void setSkew(double skew) {
        this.skew = skew;
    }
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public String getOldVariableCode() {
        return oldVariableCode;
    }
    public void setOldVariableCode(String oldVariableCode) {
        this.oldVariableCode = oldVariableCode;
    }
    public ComplexFractalGenerator.Mode getMode() {
        return mode;
    }
    public void setMode(ComplexFractalGenerator.Mode mode) {
        this.mode = mode;
    }
    public boolean isAnti() {
        return anti;
    }
    public void setAnti(boolean anti) {
        this.anti = anti;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String[][] getConstants() {
        return constants;
    }
    public void setConstants(String[][] constants) {
        this.constants = new String[constants.length][2];
        for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, this.constants[i].length);
        }
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getNum_threads() {
        return num_threads;
    }
    public void setNum_threads(int num_threads) {
        this.num_threads = num_threads;
    }
    public int getNum_points() {
        return num_points;
    }
    public void setNum_points(int num_points) {
        this.num_points = num_points;
    }
    public int[] getIterations() {
        return iterations;
    }
    public void setIterations(int[] iterations) {
        this.iterations = new int[iterations.length];
        System.arraycopy(iterations, 0, this.iterations, 0, this.iterations.length);
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getZoom_level() {
        return zoom_level;
    }
    public void setZoom_level(double zoom_level) {
        this.zoom_level = zoom_level;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        this.base_precision = base_precision;
    }
    public boolean useThreadedGenerator() {
        return num_threads > 1;
    }
    public ZoomConfig getZoomConfig() {
        return zoomConfig;
    }
    public void setZoomConfig(ZoomConfig zoomConfig) {
        this.zoomConfig = zoomConfig;
    }
    private String constantsToString() {
        String representation = "";
        for (String[] constant : constants) {
            for (String s : constant) {
                representation += s + ":";
            }
            representation = representation.substring(0, representation.length() - 1) + ";";
        }
        representation = representation.substring(0, representation.length() - 1);
        return representation;
    }
    @Override
    public void fromString(String[] data) {
        setWidth(Integer.valueOf(data[0]));
        setHeight(Integer.valueOf(data[1]));
        setIterations(integersFromStrings(StringManipulator.split(data[2], ",")));
        setBase_precision(Double.valueOf(data[3]));
        setZoom(Double.valueOf(data[4]));
        setZoom_level(Double.valueOf(data[5]));
        setEscape_radius(Double.valueOf(data[6]));
        setTolerance(Double.valueOf(data[7]));
        setSkew(Double.valueOf(data[8]));
        setNum_points(Integer.valueOf(data[9]));
        setFunction(data[10]);
        setVariableCode(data[11]);
        setMode(ComplexFractalGenerator.Mode.valueOf(data[12]));
        String[] con = StringManipulator.split(data[13], ";");
        String[][] consts = new String[con.length][2];
        for (int i = 0; i < consts.length; i++) {
            consts[i] = StringManipulator.split(con[i], ":");
        }
        setConstants(consts);
        setAnti(Boolean.valueOf(data[14]));
    }
    private String integersToString(int[] ints) {
        String string = "";
        for (int anInt : ints) {
            string += anInt + ",";
        }
        return string.substring(0, string.length() - 1);//trim trailing ','
    }
    private int[] integersFromStrings(String[] strings) {
        int[] ints = new int[strings.length];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = Integer.valueOf(strings[i]);
        }
        return ints;
    }
    @Override
    public String toString() {
        String representation = "Postprocessing:" + postprocessMode + "\nThreads:" + num_threads + ((newton_constant != null) ? "\nNewton_constant:" + newton_constant : "");
        representation += "%n%d%n%d%n%s%n%f%n%f%n%f%n%f%n%f%n%f%nSwitch_Mode_Rate:%d%n%d%n%s%n%s%nOld_variable_code:%s%n%s%n%s%n%s";
        representation = String.format(representation, width, height, integersToString(iterations), base_precision, zoom, zoom_level, escape_radius, tolerance, skew, switch_rate, num_points, function, variableCode, oldVariableCode, constantsToString(), anti);
        if (zoomConfig != null) {
            representation += "\n" + zoomConfig;
        }
        return representation;
    }
}