package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.color.ColorData;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;

import static in.tamchow.fractal.config.Strings.BLOCKS.ENDINIT;
import static in.tamchow.fractal.config.Strings.BLOCKS.INIT;
import static in.tamchow.fractal.config.Strings.DECLARATIONS.*;
/**
 * Parameters for configuring the initialization of a fractal
 */
public class ComplexFractalInitParams implements Serializable, DataFromString {
    @Nullable
    public String function, variableCode, oldVariableCode, lineTrap;
    public String[][] constants;
    public int width, height, switchRate;
    public ComplexFractalGenerator.Mode fractalMode;
    public double tolerance, zoom, basePrecision, skew;
    public Complex trapPoint;
    public ColorData color;
    public ComplexFractalInitParams(@NotNull ComplexFractalInitParams initParams) {
        initParams(initParams.width, initParams.height, initParams.zoom, initParams.basePrecision, initParams.fractalMode, initParams.function, initParams.constants, initParams.variableCode, initParams.oldVariableCode, initParams.tolerance, initParams.getColor(), initParams.switchRate, initParams.trapPoint, initParams.lineTrap, initParams.skew);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, int switchRate, @NotNull Complex trapPoint) {
        initParams(width, height, zoom, basePrecision, fractalMode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, trapPoint, null, 0);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, int switchRate, @NotNull Complex trapPoint, String lineTrap) {
        initParams(width, height, zoom, basePrecision, fractalMode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, trapPoint, lineTrap, 0);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData color, int switchRate, @NotNull Complex trapPoint, String lineTrap, double skew) {
        initParams(width, height, zoom, basePrecision, fractalMode, function, constants, variableCode, variableCode + "_p", tolerance, color, switchRate, trapPoint, lineTrap, skew);
    }
    public ComplexFractalInitParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, String oldVariableCode, double tolerance, @NotNull ColorData color, int switchRate, @NotNull Complex trapPoint, String lineTrap, double skew) {
        initParams(width, height, zoom, basePrecision, fractalMode, function, constants, variableCode, oldVariableCode, tolerance, color, switchRate, trapPoint, lineTrap, skew);
    }
    /**
     * Sets up an unusable {@link ComplexFractalInitParams}.
     * <b>Do not</b> use as-is.
     */
    public ComplexFractalInitParams() {
        @NotNull String[][] consts = {{null, null}};
        @NotNull ColorData cfg = new ColorData();
        initParams(0, 0, 0, 0, null, null, consts, null, null, 0, cfg, 0, null, null, 0);
    }
    @NotNull
    public ColorData getColor() {
        return new ColorData(color);
    }
    public void setColor(@NotNull ColorData color) {
        this.color = new ColorData(color);
    }
    private void setConstants(@NotNull String[][] constants) {
        this.constants = new String[constants.length][constants[0].length];
        for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, constants[i].length);
        }
    }
    @Nullable
    public String getOldVariableCode() {
        return oldVariableCode;
    }
    public void setOldVariableCode(String oldVariableCode) {
        this.oldVariableCode = oldVariableCode;
    }
    @Override
    public String toString() {
        String representation = INIT + "%n%d%n%d%n%f%n%f%n%s%n%s%n%s%n%s%n" + OLD_VARIABLE_CODE + "%s$n%f%n%s%n" + SWITCH_RATE + "%d%n" + TRAP_POINT + "%s%n";
        representation = String.format(representation, width, height, zoom,
                basePrecision, fractalMode, function, constantsToString(), variableCode,
                oldVariableCode, tolerance, color, switchRate, trapPoint);
        if (lineTrap != null) {
            representation += TRAP_LINE + lineTrap + "\n";
        }
        representation += skew + "\n" + ENDINIT;
        return representation;
    }
    @NotNull
    private String constantsToString() {
        @NotNull String representation = "";
        for (@NotNull String[] constant : constants) {
            for (String s : constant) {
                representation += s + ":";
            }
            representation = representation.substring(0, representation.length() - 1) + ";";
        }
        representation = representation.substring(0, representation.length() - 1);
        return representation;
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
    @Nullable
    public String getLineTrap() {
        return lineTrap;
    }
    public void setLineTrap(String lineTrap) {
        this.lineTrap = lineTrap;
    }
    public Complex getTrapPoint() {
        return trapPoint;
    }
    public void setTrapPoint(@NotNull Complex trapPoint) {
        this.trapPoint = new Complex(trapPoint);
    }
    public int getSwitchRate() {
        return switchRate;
    }
    public void setSwitchRate(int switchRate) {
        this.switchRate = switchRate;
    }
    public void fromString(@NotNull String[] params) {
        @NotNull String[] constantsRaw = StringManipulator.split(params[8], ";");
        @NotNull String[][] constants = new String[constantsRaw.length][2];
        for (int i = 0; i < constants.length; i++) {
            constants[i] = StringManipulator.split(constantsRaw[i], ":");
        }
        @NotNull String[] colorConfigRaw = StringManipulator.split(params[9], ",");
        @NotNull ColorData colorConfig = new ColorData();
        colorConfig.fromString(colorConfigRaw);
        if (params.length == 12) {
            initParams(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Double.parseDouble(params[2]), Double.parseDouble(params[3]), ComplexFractalGenerator.Mode.valueOf(params[4]), params[5], constants, params[6], Double.parseDouble(params[7]), colorConfig, 0, Double.parseDouble(params[11]));
        }
    }
    private void initParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, String oldVariableCode, double tolerance, @NotNull ColorData colors, int switchRate, @NotNull Complex trapPoint, String lineTrap, double skew) {
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.basePrecision = basePrecision;
        this.skew = skew;
        this.fractalMode = fractalMode;
        this.function = function;
        this.switchRate = switchRate;
        setConstants(constants);
        setOldVariableCode(oldVariableCode);
        this.variableCode = variableCode;
        this.tolerance = tolerance;
        setColor(colors);
        setTrapPoint(trapPoint);
        this.lineTrap = lineTrap;
    }
    private void initParams(int width, int height, double zoom, double basePrecision, ComplexFractalGenerator.Mode fractalMode, String function, @NotNull String[][] constants, String variableCode, double tolerance, @NotNull ColorData colors, int switchRate, double skew) {
        initParams(width, height, zoom, basePrecision, fractalMode, function, constants, variableCode, variableCode+"_p", tolerance, colors, switchRate, Complex.ZERO, null, skew);
    }
}