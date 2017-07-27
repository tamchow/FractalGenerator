package in.tamchow.fractal.config.fractalconfig.IFS;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Strings;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.matrix.Matrix;

import static in.tamchow.fractal.config.Strings.DECLARATIONS.*;
/**
 * Holds Parameters for an IFS fractal
 */
public class IFSFractalParams extends Config {
    private static final String VARIABLE_CODES = "x:y:r:t:p";
    public ZoomConfig zoomConfig;
    public PixelContainer.PostProcessMode postProcessMode;
    private Matrix[] transforms, translators;
    private boolean ifsMode;
    private String x_code, r_code, y_code, t_code, p_code;
    private String[] yfunctions, xfunctions;
    private double[] weights;
    private int[] colors;
    private int depth, frameskip, threads;
    private double zoom, base_precision, skew;
    {
        setName(Strings.BLOCKS.IFS);
    }
    public IFSFractalParams() {
        super();
        setDepth(1);
        setThreads(1);
        @NotNull String[] variableCodes = StringManipulator.split(VARIABLE_CODES, ":");
        setFrameskip(-1);
        setPath("");
        setX_code(variableCodes[0]);
        setY_code(variableCodes[1]);
        setR_code(variableCodes[2]);
        setT_code(variableCodes[3]);
        setP_code(variableCodes[4]);
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
    }
    public IFSFractalParams(@NotNull IFSFractalParams config) {
        super(config.height, config.width, config.path);
        if (!(config.getColors().length == config.getWeights().length && config.getTransforms().length == config.getTranslators().length)) {
            throw new IllegalArgumentException("Configuration object is not properly defined");
        }
        setColors(config.getColors());
        setWeights(config.getWeights());
        setTransforms(config.getTransforms());
        setTranslators(config.getTranslators());
        setDepth(config.getDepth());
        setFrameskip(config.getFrameskip());
        setFps(config.getFps());
        setPath(config.getPath());
        setSkew(config.getSkew());
        setPostProcessMode(config.getPostProcessMode());
        setThreads(config.getThreads());
        setX_code(config.getX_code());
        setY_code(config.getY_code());
        setR_code(config.getR_code());
        setT_code(config.getT_code());
        setP_code(config.getP_code());
        if (config.zoomConfig.hasZooms()) {
            this.zoomConfig = new ZoomConfig(config.zoomConfig);
        }
        setPath(config.getPath());
        setPostProcessMode(config.getPostProcessMode());
    }
    @Override
    public void fromString(@NotNull String[] input) {
        @NotNull String[] ifsData = StringManipulator.split(input[0], ":");
        if (ifsData.length == 1) {
            setIfsMode(Boolean.parseBoolean(ifsData[0]));
        } else {
            setIfsMode(Boolean.parseBoolean(ifsData[0]));
            setX_code(ifsData[1]);
            setY_code(ifsData[2]);
            setR_code(ifsData[3]);
            setT_code(ifsData[4]);
            setP_code(ifsData[5]);
        }
        setWidth(Integer.parseInt(input[1]));
        setHeight(Integer.parseInt(input[2]));
        setBase_precision(Double.parseDouble(input[3]));
        setZoom(Double.parseDouble(input[4]));
        setDepth(Integer.parseInt(input[5]));
        setFps(Integer.parseInt(input[6]));
        setSkew(Double.parseDouble(input[7]));
        if (isIfsMode()) {
            xfunctions = new String[input.length - 8];
            yfunctions = new String[input.length - 8];
            colors = new int[input.length - 8];
            weights = new double[input.length - 8];
            for (int i = 8; i < input.length; i++) {
                @NotNull String[] parts = StringManipulator.split(input[i], " ");
                xfunctions[i] = parts[0];
                yfunctions[i] = parts[1];
                weights[i] = Double.parseDouble(parts[2]);
                colors[i] = Integer.parseInt(parts[3], 16);
            }
        } else {
            transforms = new Matrix[input.length - 8];
            translators = new Matrix[input.length - 8];
            colors = new int[input.length - 8];
            weights = new double[input.length - 8];
            for (int i = 8; i < input.length; i++) {
                @NotNull String[] parts = StringManipulator.split(input[i], " ");
                transforms[i] = new Matrix(parts[0]);
                translators[i] = new Matrix(parts[1]);
                weights[i] = Double.parseDouble(parts[2]);
                colors[i] = Integer.parseInt(parts[3], 16);
            }
        }
    }
    public String getX_code() {
        return x_code;
    }
    public void setX_code(String x_code) {
        this.x_code = x_code;
    }
    public String getY_code() {
        return y_code;
    }
    public void setY_code(String y_code) {
        this.y_code = y_code;
    }
    public String getR_code() {
        return r_code;
    }
    public void setR_code(String r_code) {
        this.r_code = r_code;
    }
    public String getT_code() {
        return t_code;
    }
    public void setT_code(String t_code) {
        this.t_code = t_code;
    }
    public String getP_code() {
        return p_code;
    }
    public void setP_code(String p_code) {
        this.p_code = p_code;
    }
    public double getSkew() {
        return skew;
    }
    public void setSkew(double skew) {
        this.skew = skew;
    }
    public int getFrameskip() {
        return frameskip;
    }
    public void setFrameskip(int frameskip) {
        this.frameskip = frameskip;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = Math.abs((depth == 0) ? 1 : depth);
    }
    public Matrix[] getTransforms() {
        return transforms;
    }
    public void setTransforms(@NotNull Matrix[] transforms) {
        this.transforms = new Matrix[transforms.length];
        for (int i = 0; i < transforms.length; i++) {
            this.transforms[i] = new Matrix(transforms[i]);
        }
    }
    public Matrix[] getTranslators() {
        return translators;
    }
    public void setTranslators(@NotNull Matrix[] translators) {
        this.translators = new Matrix[translators.length];
        for (int i = 0; i < translators.length; i++) {
            this.translators[i] = new Matrix(translators[i]);
        }
    }
    public double[] getWeights() {
        return weights;
    }
    public void setWeights(@NotNull double[] weights) {
        this.weights = new double[weights.length];
        System.arraycopy(weights, 0, this.weights, 0, this.weights.length);
    }
    public int[] getColors() {
        return colors;
    }
    public void setColors(@NotNull int[] colors) {
        this.colors = new int[colors.length];
        System.arraycopy(colors, 0, this.colors, 0, this.colors.length);
    }
    public int getThreads() {
        return threads;
    }
    public void setThreads(int threads) {
        this.threads = MathUtils.clamp(threads, 1, depth);
    }
    public boolean isAnimated() {
        return frameskip >= 0;
    }
    public boolean isIfsMode() {
        return ifsMode;
    }
    public void setIfsMode(boolean ifsMode) {
        this.ifsMode = ifsMode;
    }
    public boolean useThreadedGenerator() {
        return (threads > 1) && (frameskip < 0);
    }
    public String[] getYfunctions() {
        return yfunctions;
    }
    public void setYfunctions(String[] yfunctions) {
        this.yfunctions = yfunctions;
    }
    public String[] getXfunctions() {
        return xfunctions;
    }
    public void setXfunctions(String[] xfunctions) {
        this.xfunctions = xfunctions;
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String representation = THREADS + threads + "\n" +
                ((frameskip >= 0) ? FRAMESKIP + frameskip + "\n" : "");
        representation += (postProcessMode != null) ? POSTPROCESSING + postProcessMode + "\n" : "";
        representation += ifsMode + ((ifsMode) ? ":" + createCodeString() : "") + "\n" + width + "\n" + height + "\n" + base_precision + "\n" + zoom + "\n" + depth + "\n" + fps + "\n" + skew;
        if (ifsMode) {
            for (int i = 0; i < weights.length; i++) {
                representation += "\n" + xfunctions[i] + " " + yfunctions[i] + " " + weights[i] + " " + colors[i];
            }
        } else {
            for (int i = 0; i < weights.length; i++) {
                representation += "\n" + transforms[i] + " " + translators[i] + " " + weights[i] + " " + colors[i];
            }
        }
        if (zoomConfig != null) {
            representation += "\n" + zoomConfig;
        }
        return representation.trim();
    }
    private String createCodeString() {
        return String.format("%s:%s:%s:%s:%s", getX_code(), getY_code(), getR_code(), getT_code(), getP_code());
    }
    public void setZoomConfig(@NotNull ZoomConfig config) {
        zoomConfig = new ZoomConfig(config);
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
    public PixelContainer.PostProcessMode getPostProcessMode() {
        return postProcessMode;
    }
    public void setPostProcessMode(PixelContainer.PostProcessMode postProcessMode) {
        this.postProcessMode = postProcessMode;
    }
}