package in.tamchow.fractal.config.fractalconfig.complexbrot;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Holds parameters for a complex Brot fractal
 * TODO: Implement
 */
public class ComplexBrotFractalParams implements Serializable, DataFromString {
    public ZoomConfig zoomConfig;
    public int width, height, num_threads, switch_rate, byParts;
    public long num_points;
    public Complex newton_constant;
    public long[] iterations;
    public double zoom, zoom_level, base_precision, skew, tolerance;
    public String function, variableCode, oldVariableCode, path;
    public ComplexFractalGenerator.Mode mode;
    public String[][] constants;
    public boolean anti;
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, long num_points, long[] iterations, double zoom, double zoom_level, double base_precision, double tolerance, double skew, String function, String variableCode, ComplexFractalGenerator.Mode mode, String[][] constants, boolean anti) {
        this(width, height, num_threads, switch_rate, num_points, iterations, zoom, zoom_level, base_precision, tolerance, skew, function, variableCode, variableCode + "_p", mode, constants, anti);
    }
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, long num_points, long[] iterations, double zoom, double zoom_level, double base_precision, double tolerance, double skew, String function, String variableCode, String oldVariableCode, ComplexFractalGenerator.Mode mode, String[][] constants, boolean anti) {
        setWidth(width); setHeight(height); setNum_threads(num_threads); setSwitch_rate(switch_rate);
        setNum_points(num_points); setIterations(iterations); setZoom(zoom); setZoom_level(zoom_level);
        setBase_precision(base_precision); setTolerance(tolerance); setSkew(skew); setFunction(function);
        setVariableCode(variableCode); setOldVariableCode(oldVariableCode); setMode(mode); setConstants(constants);
        setAnti(anti);
    }
    public ComplexBrotFractalParams() {}
    public ComplexBrotFractalParams(ComplexBrotFractalParams old) {
        this(old.getWidth(), old.getHeight(), old.getNum_threads(), old.getSwitch_rate(), old.getNum_points(), old.getIterations(), old.getZoom(), old.getZoom_level(), old.getBase_precision(), old.getTolerance(), old.getSkew(), old.getFunction(), old.getVariableCode(), old.getOldVariableCode(), old.getMode(), old.getConstants(), old.isAnti());
        setPath(old.getPath());
        setNewton_constant(old.getNewton_constant());
    }
    public Complex getNewton_constant() {return newton_constant;}
    public void setNewton_constant(Complex newton_constant) {this.newton_constant = newton_constant;}
    public int getSwitch_rate() {return switch_rate;}
    public void setSwitch_rate(int switch_rate) {this.switch_rate = switch_rate;}
    public double getTolerance() {return tolerance;}
    public void setTolerance(double tolerance) {this.tolerance = tolerance;}
    public double getSkew() {return skew;}
    public void setSkew(double skew) {this.skew = skew;}
    public String getFunction() {return function;}
    public void setFunction(String function) {this.function = function;}
    public String getVariableCode() {return variableCode;}
    public void setVariableCode(String variableCode) {this.variableCode = variableCode;}
    public String getOldVariableCode() {return oldVariableCode;}
    public void setOldVariableCode(String oldVariableCode) {this.oldVariableCode = oldVariableCode;}
    public ComplexFractalGenerator.Mode getMode() {return mode;}
    public void setMode(ComplexFractalGenerator.Mode mode) {this.mode = mode;}
    public boolean isAnti() {return anti;}
    public void setAnti(boolean anti) {this.anti = anti;}
    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}
    public String[][] getConstants() {return constants;}
    public void setConstants(String[][] constants) {
        this.constants = new String[constants.length][2]; for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, this.constants[i].length);
        }
    }
    public int getWidth() {return width;}
    public void setWidth(int width) {this.width = width;}
    public int getHeight() {return height;}
    public void setHeight(int height) {this.height = height;}
    public int getNum_threads() {return num_threads;}
    public void setNum_threads(int num_threads) {this.num_threads = num_threads;}
    public long getNum_points() {return num_points;}
    public void setNum_points(long num_points) {this.num_points = num_points;}
    public long[] getIterations() {return iterations;}
    public void setIterations(long[] iterations) {
        this.iterations = new long[iterations.length];
        System.arraycopy(iterations, 0, this.iterations, 0, this.iterations.length);
    }
    public double getZoom() {return zoom;}
    public void setZoom(double zoom) {this.zoom = zoom;}
    public double getZoom_level() {return zoom_level;}
    public void setZoom_level(double zoom_level) {this.zoom_level = zoom_level;}
    public double getBase_precision() {return base_precision;}
    public void setBase_precision(double base_precision) {this.base_precision = base_precision;}
    public int getByParts() {return byParts;}
    public void setByParts(int byParts) {this.byParts = byParts;}
    public boolean useThreadedGenerator() {return num_threads > 1;}
    public ZoomConfig getZoomConfig() {return zoomConfig;}
    public void setZoomConfig(ZoomConfig zoomConfig) {this.zoomConfig = zoomConfig;}
    @Override
    public void fromString(String[] data) {
    }
    @Override
    public String toString() {
        return null;
    }
}