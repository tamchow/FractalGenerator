package in.tamchow.fractal.config.fractalconfig;

import in.tamchow.fractal.FractalGenerator;
import in.tamchow.fractal.config.color.ColorMode;

import java.io.Serializable;

/**
 * Parameters for configuring the initialization of a fractal
 */
public class FractalInitParams implements Serializable {
    public String function, variableCode;
    public String[][] consts;
    public int width, height, zoom, zoom_factor, base_precision, color_mode, num_colors, color_density, fractal_mode;
    public double boundary_condition;

    public FractalInitParams(FractalInitParams initParams) {
        initParams(initParams.width, initParams.height, initParams.zoom, initParams.zoom_factor, initParams.base_precision, initParams.color_mode, initParams.num_colors, initParams.color_density, initParams.fractal_mode, initParams.boundary_condition, initParams.function, initParams.consts, initParams.variableCode);
    }

    public FractalInitParams(int width, int height, int zoom, int zoom_factor, int base_precision, int color_mode, int num_colors, int color_density, int fractal_mode, double boundary_condition, String function, String[][] consts, String variableCode) {
        initParams(width, height, zoom, zoom_factor, base_precision, color_mode, num_colors, color_density, fractal_mode, boundary_condition, function, consts, variableCode);
    }

    public FractalInitParams() {
        String func = "z ^ 2 + c";
        String[][] consts = {{"c", "-0.8,+0.156i"}};
        initParams(1921, 1081, 10, 0, 540, ColorMode.COLOR_DIVIDE, 32, 256, FractalGenerator.MODE_JULIA, 2, func, consts, "z");
    }

    private void initParams(int width, int height, int zoom, int zoom_factor, int base_precision, int color_mode, int num_colors, int color_density, int fractal_mode, double boundary_condition, String function, String[][] consts, String variableCode) {
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.zoom_factor = zoom_factor;
        this.base_precision = base_precision;
        this.color_mode = color_mode;
        this.num_colors = num_colors;
        this.color_density = color_density;
        this.fractal_mode = fractal_mode;
        this.boundary_condition = boundary_condition;
        this.function = function;
        setConsts(consts);
        this.variableCode = variableCode;
    }

    private void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }

    public void paramsFromString(String[] params) {
        String[][] consts = new String[params.length - 11][2];
        for (int i = 12; i < params.length; i++) {
            consts[i - 12][0] = params[i].substring(0, params[i].indexOf(' '));
            consts[i - 12][1] = params[i].substring(params[i].indexOf(' ') + 1, params[i].length());
        }
        initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Integer.valueOf(params[8]), Double.valueOf(params[9]), params[10], consts, params[11]);
    }
}