package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Parameters for configuring the initialization of a fractal
 */
public class ComplexFractalInitParams implements Serializable, DataFromString {
    public String function;
    public String variableCode;
    public String linetrap;
    public String[][] consts;
    public int width, height, fractal_mode, switch_rate;
    public double tolerance, zoom, zoom_factor, base_precision;
    public Complex degree, trap_point;
    public ColorConfig color;
    public boolean flip;
    public ComplexFractalInitParams(ComplexFractalInitParams initParams) {
        initParams(initParams.width, initParams.height, initParams.zoom, initParams.zoom_factor, initParams.base_precision, initParams.fractal_mode, initParams.function, initParams.consts, initParams.variableCode, initParams.tolerance, initParams.degree, initParams.getColor(), initParams.switch_rate, initParams.trap_point, initParams.linetrap, initParams.isFlip());
    }
    public boolean isFlip() {return flip;}
    public void setFlip(boolean flip) {this.flip = flip;}
    public ColorConfig getColor() {
        return new ColorConfig(color);
    }
    public void setColor(ColorConfig color) {
        this.color = new ColorConfig(color);
    }
    private void initParams(int width, int height, double zoom, double zoom_factor, double base_precision, int fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, ColorConfig colors, int switch_rate, Complex trap_point, String linetrap, boolean flip) {
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.zoom_factor = zoom_factor;
        this.base_precision = base_precision;
        this.fractal_mode = fractal_mode;
        this.function = function; this.switch_rate = switch_rate;
        setConsts(consts);
        this.variableCode = variableCode; this.tolerance = tolerance; this.degree = new Complex(degree);
        setColor(colors); setTrap_point(trap_point); this.flip = flip; this.linetrap = linetrap;
    }
    private void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double zoom_factor, double base_precision, int fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, ColorConfig color, int switch_rate, Complex trap_point) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, tolerance, degree, color, switch_rate, trap_point, null, false);
    }
    public ComplexFractalInitParams() {
        String func = "z ^ 2 + c"; String[][] consts = {{"c", "-0.8,+0.156i"}};
        ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.CURVATURE_AVERAGE_SPLINE, 19, 16, true, true);
        cfg.setPalette(new int[]{ColorConfig.toRGB(66, 30, 15), ColorConfig.toRGB(25, 7, 26), ColorConfig.toRGB(9, 1, 47), ColorConfig.toRGB(4, 4, 73), ColorConfig.toRGB(0, 7, 100), ColorConfig.toRGB(12, 44, 138), ColorConfig.toRGB(24, 82, 177), ColorConfig.toRGB(57, 125, 209), ColorConfig.toRGB(134, 181, 229), ColorConfig.toRGB(211, 236, 248), ColorConfig.toRGB(241, 233, 191), ColorConfig.toRGB(248, 201, 95), ColorConfig.toRGB(255, 170, 0), ColorConfig.toRGB(204, 128, 0), ColorConfig.toRGB(153, 87, 0), ColorConfig.toRGB(106, 52, 3)}, false);
        initParams(1921, 1081, 10, 0, 540, ComplexFractalGenerator.MODE_JULIA, func, consts, "z", 1e-5, new Complex("-1"), cfg, 0, Complex.ZERO, null, false);
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
        } String[] colorcfg = params[10].split(",");
        ColorConfig colorConfig = new ColorConfig(); colorConfig.fromString(colorcfg); if (params.length == 12) {
            initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Double.valueOf(params[2]), Double.valueOf(params[3]), Double.valueOf(params[4]), Integer.valueOf(params[5]), params[6], consts, params[7], Double.valueOf(params[8]), new Complex(params[11]), colorConfig, 0);
        } else {
            initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Double.valueOf(params[2]), Double.valueOf(params[3]), Double.valueOf(params[4]), Integer.valueOf(params[5]), params[6], consts, params[7], Double.valueOf(params[8]), new Complex("-1"), colorConfig, 0);
        }
    }
    private void initParams(int width, int height, double zoom, double zoom_factor, double base_precision, int fractal_mode, String function, String[][] consts, String variableCode, double tolerance, Complex degree, ColorConfig colors, int switch_rate) {
        this.width = width; this.height = height; this.zoom = zoom; this.zoom_factor = zoom_factor;
        this.base_precision = base_precision; this.fractal_mode = fractal_mode; this.function = function;
        this.switch_rate = switch_rate; setConsts(consts); this.variableCode = variableCode; this.tolerance = tolerance;
        this.degree = new Complex(degree); setColor(colors); setTrap_point(Complex.ZERO); this.flip = false;
        this.linetrap = null;
    }
}