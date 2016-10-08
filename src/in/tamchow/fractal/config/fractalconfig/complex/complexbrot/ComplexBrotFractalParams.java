package in.tamchow.fractal.config.fractalconfig.complex.complexbrot;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Strings;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;

import static in.tamchow.fractal.config.Strings.DECLARATIONS.*;
/**
 * Holds parameters for a ComplexBrot fractal
 */
public class ComplexBrotFractalParams extends Config {
    public ZoomConfig zoomConfig;
    @Nullable
    public Complex newton_constant;
    public int[] iterations;
    public ComplexFractalGenerator.Mode mode;
    public String[][] constants;
    private PixelContainer.PostProcessMode postProcessMode;
    private int num_threads, switch_rate, num_points, xThreads, yThreads, xPointsPerPixel, yPointsPerPixel, maxHitThreshold;
    private double zoom, base_precision, skew, tolerance, escape_radius;
    private String function, variableCode, oldVariableCode;
    private boolean anti, sequential, clamped, skidColoring, logScaling;
    private double[] percentiles;
    {
        setName(Strings.BLOCKS.COMPLEXBROT);
    }
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, int num_points, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        this(width, height, num_threads, switch_rate, num_points, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, variableCode + "_p", constants, mode, anti, clamped, percentiles);
    }
    public ComplexBrotFractalParams(int width, int height, int num_threads, int switch_rate, int num_points, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        this();
        init(width, height, num_threads, switch_rate, num_points, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, oldVariableCode, constants, mode, anti, clamped, percentiles);
    }
    public ComplexBrotFractalParams(int width, int height, int xThreads, int yThreads, int switch_rate, int xPointsPerPixel, int yPointsPerPixel, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        this(width, height, xThreads, yThreads, switch_rate, xPointsPerPixel, yPointsPerPixel, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, variableCode + "_p", constants, mode, anti, clamped, percentiles);
    }
    public ComplexBrotFractalParams(int width, int height, int xThreads, int yThreads, int switch_rate, int xPointsPerPixel, int yPointsPerPixel, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        this();
        init(width, height, xThreads, yThreads, switch_rate, xPointsPerPixel, yPointsPerPixel, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, oldVariableCode, constants, mode, anti, clamped, percentiles);
    }
    public ComplexBrotFractalParams() {
        super();
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
        zoomConfig = new ZoomConfig();
        setNewton_constant(null);
        setNum_points(1);
        setNum_threads(1);
        setLogScaling(true);
    }
    public ComplexBrotFractalParams(@NotNull ComplexBrotFractalParams old) {
        if (old.isSequential()) {
            init(old.getWidth(), old.getHeight(), old.getNum_threads(), old.getSwitch_rate(), old.getNum_points(), old.getMaxHitThreshold(), old.getIterations(), old.getZoom(), old.getBase_precision(), old.getEscape_radius(), old.getTolerance(), old.getSkew(), old.getFunction(), old.getVariableCode(), old.getOldVariableCode(), old.getConstants(), old.getMode(), old.isAnti(), old.isClamped(), old.getPercentiles());
        } else {
            init(old.getWidth(), old.getHeight(), old.getxThreads(), old.getyThreads(), old.getSwitch_rate(), old.getxPointsPerPixel(), old.getyPointsPerPixel(), old.getMaxHitThreshold(), old.getIterations(), old.getZoom(), old.getBase_precision(), old.getEscape_radius(), old.getTolerance(), old.getSkew(), old.getFunction(), old.getVariableCode(), old.getOldVariableCode(), old.getConstants(), old.getMode(), old.isAnti(), old.isClamped(), old.getPercentiles());
        }
        setPath(old.getPath());
        setLogScaling(old.isLogScaling());
        setPostProcessMode(old.getPostProcessMode());
        setNewton_constant(old.getNewton_constant());
        if (old.zoomConfig.hasZooms()) {
            this.zoomConfig = new ZoomConfig(old.getZoomConfig());
        }
    }
    public boolean isLogScaling() {
        return logScaling;
    }
    public void setLogScaling(boolean logScaling) {
        this.logScaling = logScaling;
    }
    public int getxThreads() {
        return xThreads;
    }
    public void setxThreads(int xThreads) {
        this.xThreads = xThreads;
    }
    public int getyThreads() {
        return yThreads;
    }
    public void setyThreads(int yThreads) {
        this.yThreads = yThreads;
    }
    public int getxPointsPerPixel() {
        return xPointsPerPixel;
    }
    public void setxPointsPerPixel(int xPointsPerPixel) {
        this.xPointsPerPixel = xPointsPerPixel;
    }
    public int getyPointsPerPixel() {
        return yPointsPerPixel;
    }
    public void setyPointsPerPixel(int yPointsPerPixel) {
        this.yPointsPerPixel = yPointsPerPixel;
    }
    public boolean isSequential() {
        return sequential;
    }
    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }
    private void init(int width, int height, int num_threads, int switch_rate, int num_points, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        setNum_points(num_points);
        setNum_threads(num_threads);
        commonInit(width, height, switch_rate, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, oldVariableCode, constants, mode, anti, clamped, percentiles);
        setSequential(false);
    }
    private void init(int width, int height, int xThreads, int yThreads, int switch_rate, int xPointsPerPixel, int yPointsPerPixel, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        setxPointsPerPixel(xPointsPerPixel);
        setyPointsPerPixel(yPointsPerPixel);
        setxThreads(xThreads);
        setyThreads(yThreads);
        commonInit(width, height, switch_rate, maxHitThreshold, iterations, zoom, base_precision, escape_radius, tolerance, skew, function, variableCode, oldVariableCode, constants, mode, anti, clamped, percentiles);
        setSequential(true);
    }
    private void commonInit(int width, int height, int switch_rate, int maxHitThreshold, @NotNull int[] iterations, double zoom, double base_precision, double escape_radius, double tolerance, double skew, String function, String variableCode, String oldVariableCode, @NotNull String[][] constants, ComplexFractalGenerator.Mode mode, boolean anti, boolean clamped, @Nullable double[] percentiles) {
        setWidth(width);
        setHeight(height);
        setSwitch_rate(switch_rate);
        setIterations(iterations);
        setZoom(zoom);
        setBase_precision(base_precision);
        setEscape_radius(escape_radius);
        setTolerance(tolerance);
        setSkew(skew);
        setFunction(function);
        setVariableCode(variableCode);
        setOldVariableCode(oldVariableCode);
        setMode(mode);
        setMaxHitThreshold(maxHitThreshold);
        setConstants(constants);
        setAnti(anti);
        setClamped(clamped);
        setPercentiles(percentiles);
    }
    public double getEscape_radius() {
        return escape_radius;
    }
    public void setEscape_radius(double escape_radius) {
        this.escape_radius = escape_radius;
    }
    public PixelContainer.PostProcessMode getPostProcessMode() {
        return postProcessMode;
    }
    public void setPostProcessMode(PixelContainer.PostProcessMode postProcessMode) {
        this.postProcessMode = postProcessMode;
    }
    @Nullable
    public Complex getNewton_constant() {
        return newton_constant;
    }
    public void setNewton_constant(@Nullable Complex newton_constant) {
        this.newton_constant = (newton_constant != null) ? new Complex(newton_constant) : null;
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
    public String[][] getConstants() {
        return constants;
    }
    public void setConstants(@NotNull String[][] constants) {
        this.constants = new String[constants.length][2];
        for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, this.constants[i].length);
        }
    }
    public int getNum_threads() {
        return num_threads;
    }
    public void setNum_threads(int num_threads) {
        this.num_threads = MathUtils.clamp(num_threads, 1, getNum_points());
    }
    public int getNum_points() {
        return num_points;
    }
    public void setNum_points(int num_points) {
        this.num_points = MathUtils.clamp(num_points, 1, getHeight() * getWidth());
    }
    public int[] getIterations() {
        return iterations;
    }
    public void setIterations(@NotNull int[] iterations) {
        this.iterations = new int[iterations.length];
        //System.arraycopy(iterations, 0, this.iterations, 0, this.iterations.length);
        for (int i = 0; i < iterations.length; ++i) {
            this.iterations[i] = MathUtils.clamp(iterations[i], 0, Integer.MAX_VALUE - 2);
        }
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        this.base_precision = base_precision;
    }
    public boolean useThreadedGenerator() {
        if (isSequential()) {
            return xThreads * yThreads > 1;
        }
        return num_threads > 1;
    }
    public ZoomConfig getZoomConfig() {
        return zoomConfig;
    }
    public void setZoomConfig(ZoomConfig zoomConfig) {
        this.zoomConfig = zoomConfig;
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
    @Override
    public void fromString(String[] data) {
        setNum_threads(1);
        setSequential(Boolean.valueOf(data[0]));
        setWidth(Integer.valueOf(data[1]));
        setHeight(Integer.valueOf(data[2]));
        setIterations(integersFromStrings(StringManipulator.split(data[3], ",")));
        setBase_precision(Double.valueOf(data[4]));
        setZoom(Double.valueOf(data[5]));
        setEscape_radius(Double.valueOf(data[6]));
        setTolerance(Double.valueOf(data[7]));
        setSkew(Double.valueOf(data[8]));
        setMaxHitThreshold(Integer.valueOf(data[9]));
        setFunction(data[10]);
        setVariableCode(data[11]);
        setMode(ComplexFractalGenerator.Mode.valueOf(data[12]));
        @NotNull String[] con = StringManipulator.split(data[13], ";");
        @NotNull String[][] consts = new String[con.length][2];
        for (int i = 0; i < consts.length; i++) {
            consts[i] = StringManipulator.split(con[i], ":");
        }
        setConstants(consts);
        setAnti(Boolean.valueOf(data[14]));
        setClamped(Boolean.valueOf(data[15]));
        setSkidColoring(Boolean.valueOf(data[16]));
        setPercentiles(doublesFromStrings(StringManipulator.split(data[17], ",")));
        if (isSequential()) {
            setxPointsPerPixel(Integer.valueOf(data[18]));
            setyPointsPerPixel(Integer.valueOf(data[19]));
            setxThreads(Integer.valueOf(data[20]));
            setyThreads(Integer.valueOf(data[21]));
        } else {
            setNum_points(Integer.valueOf(data[18]));
        }
    }
    private double[] doublesFromStrings(@NotNull String[] strings) {
        @NotNull double[] doubles = new double[strings.length];
        for (int i = 0; i < doubles.length; ++i) {
            doubles[i] = Double.valueOf(strings[i]);
        }
        return doubles;
    }
    private String arrayToString(@NotNull int[] ints) {
        @NotNull String string = "";
        for (int anInt : ints) {
            string += anInt + ",";
        }
        return string.substring(0, string.length() - 1);//trim trailing ','
    }
    private String arrayToString(@NotNull double[] doubles) {
        @NotNull String string = "";
        for (double aDouble : doubles) {
            string += aDouble + ",";
        }
        return string.substring(0, string.length() - 1);//trim trailing ','
    }
    @NotNull
    private int[] integersFromStrings(@NotNull String[] strings) {
        @NotNull int[] ints = new int[strings.length];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = Integer.valueOf(strings[i]);
        }
        return ints;
    }
    @Nullable
    @Override
    public String toString() {
        @Nullable String representation = POSTPROCESSING + postProcessMode + ((isSequential()) ? "\n" + THREADS + num_threads : "") + ((newton_constant != null) ? "\n" + NEWTON_CONSTANT + newton_constant : "") + "\n" + isSequential() + "\n" + SWITCH_RATE + switch_rate + "\n" + OLD_VARIABLE_CODE + oldVariableCode;
        representation += width + "\n" + height + "\n" + arrayToString(iterations) + "\n" + base_precision + "\n" + zoom + "\n" + escape_radius + "\n" + tolerance + "\n" + skew + "\n" + maxHitThreshold + "\n" + function + "\n" + variableCode + "\n" + mode + "\n" + constantsToString() + "\n" + isAnti() + "\n" + isClamped() + "\n" + isSkidColoring() + "\n" + arrayToString(percentiles) + "\n";
        if (isSequential()) {
            representation += xPointsPerPixel + "\n" + yPointsPerPixel + "\n" + xThreads + "\n" + yThreads;
        } else {
            representation += num_points;
        }
        if (zoomConfig != null) {
            representation += "\n" + zoomConfig;
        }
        return representation;
    }
    public int getMaxHitThreshold() {
        return maxHitThreshold;
    }
    public void setMaxHitThreshold(int maxHitThreshold) {
        this.maxHitThreshold = maxHitThreshold;
    }
    public boolean isClamped() {
        return clamped;
    }
    public void setClamped(boolean clamped) {
        this.clamped = clamped;
    }
    public boolean isSkidColoring() {
        return skidColoring;
    }
    public void setSkidColoring(boolean skidColoring) {
        this.skidColoring = skidColoring;
    }
    public double[] getPercentiles() {
        return percentiles;
    }
    public void setPercentiles(@Nullable double[] percentiles) {
        this.percentiles = new double[getIterations().length];
        if (percentiles == null) {
            for (int i = 0; i < this.percentiles.length; ++i) {
                this.percentiles[i] = 1.0;
            }
        } else {
            int i = 0;
            for (; i < this.percentiles.length && i < percentiles.length; ++i) {
                this.percentiles[i] = percentiles[i];
            }
            for (; i < this.percentiles.length; ++i) {
                this.percentiles[i] = 1.0;
            }
        }
    }
}