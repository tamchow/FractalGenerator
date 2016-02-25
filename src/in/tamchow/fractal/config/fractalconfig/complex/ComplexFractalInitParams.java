package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.color.Color_Utils_Config;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Parameters for configuring the initialization of a fractal
 */
public class ComplexFractalInitParams implements Serializable, DataFromString {
    public String function, variableCode, oldvariablecode, linetrap;
    public String[][] consts;
    public int width, height, switch_rate;
    public ComplexFractalGenerator.Mode fractal_mode;
    public double tolerance, zoom, zoom_factor, base_precision, skew;
    public Complex degree, trap_point;
    public Color_Utils_Config color;
    public ComplexFractalInitParams(ComplexFractalInitParams initParams) {
        initParams(initParams.width, initParams.height, initParams.zoom, initParams.zoom_factor, initParams.base_precision, initParams.fractal_mode, initParams.function, initParams.consts, initParams.variableCode, initParams.oldvariablecode, initParams.tolerance, initParams.degree, initParams.getColor(), initParams.switch_rate, initParams.trap_point, initParams.linetrap, initParams.skew);
    }
    public Color_Utils_Config getColor() {
        return new Color_Utils_Config(color);
    }
    public void setColor(Color_Utils_Config color) {
        this.color = new Color_Utils_Config(color);
    }
    private void initParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, Complex degree, Color_Utils_Config colors, int switch_rate, Complex trap_point, String linetrap, double skew) {
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.zoom_factor = zoom_factor;
        this.base_precision = base_precision; this.skew = skew;
        this.fractal_mode = fractal_mode; this.function = function; this.switch_rate = switch_rate; setConsts(consts);
        setOldvariablecode(oldvariablecode);
        this.variableCode = variableCode; this.tolerance = tolerance; this.degree = new Complex(degree);
        setColor(colors); setTrap_point(trap_point); this.linetrap = linetrap;
    }
    private void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, Color_Utils_Config color, int switch_rate, Complex trap_point) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, variableCode + "_p", tolerance, degree, color, switch_rate, trap_point, null, 0);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, int switch_rate, Complex trap_point) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1), color, switch_rate, trap_point, null, 0);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, int switch_rate, Complex trap_point, String linetrap) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1), color, switch_rate, trap_point, linetrap, 0);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Color_Utils_Config color, int switch_rate, Complex trap_point, String linetrap, double skew) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, variableCode + "_p", tolerance, new Complex(-1), color, switch_rate, trap_point, linetrap, skew);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, String oldvariablecode, double tolerance, Color_Utils_Config color, int switch_rate, Complex trap_point, String linetrap, double skew) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, oldvariablecode, tolerance, new Complex(-1), color, switch_rate, trap_point, linetrap, skew);
    }
    public ComplexFractalInitParams() {
        String func = "z ^ 2 + c"; String[][] consts = {{"c", "-0.8,+0.156i"}};
        Color_Utils_Config cfg = new Color_Utils_Config(Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE, 19, 16, 0, true, false);
        cfg.setPalette(new int[]{Color_Utils_Config.toRGB(66, 30, 15), Color_Utils_Config.toRGB(25, 7, 26), Color_Utils_Config.toRGB(9, 1, 47), Color_Utils_Config.toRGB(4, 4, 73), Color_Utils_Config.toRGB(0, 7, 100), Color_Utils_Config.toRGB(12, 44, 138), Color_Utils_Config.toRGB(24, 82, 177), Color_Utils_Config.toRGB(57, 125, 209), Color_Utils_Config.toRGB(134, 181, 229), Color_Utils_Config.toRGB(211, 236, 248), Color_Utils_Config.toRGB(241, 233, 191), Color_Utils_Config.toRGB(248, 201, 95), Color_Utils_Config.toRGB(255, 170, 0), Color_Utils_Config.toRGB(204, 128, 0), Color_Utils_Config.toRGB(153, 87, 0), Color_Utils_Config.toRGB(106, 52, 3)}, false);
        initParams(1921, 1081, 10, 0, 540, ComplexFractalGenerator.Mode.JULIA, func, consts, "z", "z_p", 1e-5, new Complex("-1"), cfg, 0, Complex.ZERO, null, 0);
    }
    public String getOldvariablecode() {return oldvariablecode;}
    public void setOldvariablecode(String oldvariablecode) {this.oldvariablecode = oldvariablecode;}
    @Override
    public String toString() {
        String representation = "[Initconfig]\n" + width + "\n" + height + "\n" + zoom + "\n" + zoom_factor + "\n";
        representation += base_precision + "\n" + fractal_mode + "\n" + function + "\n" + constantsToString() + "\n" + variableCode + "\n";
        representation += "Old_variable_code:" + oldvariablecode + "\n" + tolerance + "\n" + degree + "\n" + color + "\n" + "Switch_Mode_Rate:" + switch_rate + "\n" + "Trap_point:" + trap_point + "\n";
        if (linetrap != null) {representation += "Trap_line:" + linetrap + "\n";} representation += skew + "\n";
        return representation;
    }
    private String constantsToString() {
        String representation = ""; for (String[] constant : consts) {
            for (String s : constant) {
                representation += s + ":";
            } representation = representation.substring(0, representation.length() - 1) + ";";
        } representation = representation.substring(0, representation.length() - 1); return representation;
    }
    public String getLinetrap() {return linetrap;}
    public void setLinetrap(String linetrap) {this.linetrap = linetrap;}
    public Complex getTrap_point() {return trap_point;}
    public void setTrap_point(Complex trap_point) {this.trap_point = new Complex(trap_point);}
    public int getSwitch_rate() {return switch_rate;}
    public void setSwitch_rate(int switch_rate) {this.switch_rate = switch_rate;}
    public void fromString(String[] params) {
        String[] con = params[9].split(";");
        String[][] consts = new String[con.length][2];
        for (int i = 0; i < consts.length; i++) {
            consts[i] = con[i].split(":");
        } String[] colorcfg = params[10].split(","); Color_Utils_Config colorConfig = new Color_Utils_Config();
        colorConfig.fromString(colorcfg); if (params.length == 13) {
            initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Double.valueOf(params[2]), Double.valueOf(params[3]), Double.valueOf(params[4]), ComplexFractalGenerator.Mode.valueOf(params[5]), params[6], consts, params[7], Double.valueOf(params[8]), new Complex(params[11]), colorConfig, 0, Double.valueOf(params[12]));
        } else {
            initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Double.valueOf(params[2]), Double.valueOf(params[3]), Double.valueOf(params[4]), ComplexFractalGenerator.Mode.valueOf(params[5]), params[6], consts, params[7], Double.valueOf(params[8]), new Complex("-1"), colorConfig, 0, Double.valueOf(params[12]));
        }
    }
    private void initParams(int width, int height, double zoom, double zoom_factor, double base_precision, ComplexFractalGenerator.Mode fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, Color_Utils_Config colors, int switch_rate, double skew) {
        this.width = width; this.height = height; this.zoom = zoom; this.zoom_factor = zoom_factor;
        this.base_precision = base_precision; this.fractal_mode = fractal_mode; this.function = function;
        this.switch_rate = switch_rate; setConsts(consts); this.variableCode = variableCode; this.tolerance = tolerance;
        this.degree = new Complex(degree); setColor(colors); setTrap_point(Complex.ZERO); this.linetrap = null;
        setOldvariablecode(variableCode + "_p"); this.skew = skew;
    }
}