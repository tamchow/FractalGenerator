package in.tamchow.fractal.config.fractalconfig;

import in.tamchow.fractal.FractalGenerator;
import in.tamchow.fractal.config.color.ColorConfig;
import in.tamchow.fractal.config.color.Colors;

import java.io.Serializable;

/**
 * Parameters for configuring the initialization of a fractal
 */
public class FractalInitParams implements Serializable {
    public String function, variableCode;
    public String[][] consts;
    public int width, height, zoom, zoom_factor, base_precision, fractal_mode;
    public double tolerance;
    public ColorConfig color;

    public FractalInitParams(FractalInitParams initParams) {
        initParams(initParams.width, initParams.height, initParams.zoom, initParams.zoom_factor, initParams.base_precision, initParams.fractal_mode, initParams.function, initParams.consts, initParams.variableCode, initParams.tolerance, initParams.getColor());
    }

    public FractalInitParams(int width, int height, int zoom, int zoom_factor, int base_precision, int fractal_mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig color) {
        initParams(width, height, zoom, zoom_factor, base_precision, fractal_mode, function, consts, variableCode, tolerance, color);
    }

    public FractalInitParams() {
        String func = "z ^ 2 + c";
        String[][] consts = {{"c", "-0.8,+0.156i"}};
        ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.STRIPE_AVERAGE, 32, 256, 0);
        initParams(1921, 1081, 10, 0, 540, FractalGenerator.MODE_JULIA, func, consts, "z", 1e-5, cfg);
    }

    public ColorConfig getColor() {
        return new ColorConfig(color);
    }

    public void setColor(ColorConfig color) {
        this.color = new ColorConfig(color);
    }

    private void initParams(int width, int height, int zoom, int zoom_factor, int base_precision, int fractal_mode, String function, String[][] consts, String variableCode, double tolerance, ColorConfig colors) {
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.zoom_factor = zoom_factor;
        this.base_precision = base_precision;
        this.fractal_mode = fractal_mode;
        this.function = function;
        setConsts(consts);
        this.variableCode = variableCode;
        this.tolerance = tolerance;
    }

    private void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }

    public void paramsFromString(String[] params) {
        String[] con = params[9].split(";");
        String[][] consts = new String[con.length][2];
        for (int i = 0; i < consts.length; i++) {
            consts[i][0] = con[i].substring(0, con[i].indexOf(':'));
            consts[i][1] = con[i].substring(con[i].indexOf(':') + 1, con[i].length());
        }
        String[] colorcfg = new String[params.length - 10];
        for (int i = 0; i < colorcfg.length && (i + 10) < params.length; i++) {
            colorcfg[i] = params[i + 10];
        }
        ColorConfig colorConfig = new ColorConfig();
        colorConfig.colorsFromString(colorcfg);
        initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), Integer.valueOf(params[5]), params[6], consts, params[7], Double.valueOf(params[8]), colorConfig);
    }
}