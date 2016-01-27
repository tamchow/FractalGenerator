package in.tamchow.fractal.config.fractalconfig.IFS;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.math.matrix.Matrix;
/**
 * Holds Parameters for an IFS fractal
 */
public class IFSFractalParams {
    public ZoomConfig zoomConfig;
    Matrix[] transforms, translators;
    double[] weights;
    int[] colors;
    long depth;
    int width, height, fps, frameskip;
    double zoom, zoomlevel, base_precision;
    private IFSFractalParams() {setFrameskip(-1);}
    public IFSFractalParams(IFSFractalParams config) {
        if (!(config.getColors().length == config.getWeights().length && config.getTransforms().length == config.getTranslators().length)) {
            throw new IllegalArgumentException("Configuration object is not properly defined");
        } setColors(config.getColors()); setWeights(config.getWeights()); setTransforms(config.getTransforms());
        setTranslators(config.getTranslators()); setDepth(config.getDepth()); setFrameskip(config.getFrameskip());
        setFps(config.getFps());
    }
    public int getFrameskip() {return frameskip;}
    public void setFrameskip(int frameskip) {this.frameskip = frameskip;}
    public long getDepth() {
        return depth;
    }
    public void setDepth(long depth) {
        this.depth = depth;
    }
    public Matrix[] getTransforms() {
        return transforms;
    }
    public void setTransforms(Matrix[] transforms) {
        this.transforms = new Matrix[transforms.length]; for (int i = 0; i < transforms.length; i++) {
            this.transforms[i] = new Matrix(transforms[i]);
        }
    }
    public Matrix[] getTranslators() {
        return translators;
    }
    public void setTranslators(Matrix[] translators) {
        this.translators = new Matrix[translators.length]; for (int i = 0; i < translators.length; i++) {
            this.translators[i] = new Matrix(translators[i]);
        }
    }
    public double[] getWeights() {
        return weights;
    }
    public void setWeights(double[] weights) {
        this.weights = new double[weights.length]; System.arraycopy(weights, 0, this.weights, 0, this.weights.length);
    }
    public int[] getColors() {
        return colors;
    }
    public void setColors(int[] colors) {
        this.colors = new int[colors.length]; System.arraycopy(colors, 0, this.colors, 0, this.colors.length);
    }
    public int getFps() {return fps;}
    public void setFps(int fps) {this.fps = fps;}
    public static IFSFractalParams fromString(String[] input) {
        IFSFractalParams params = new IFSFractalParams(); params.setWidth(Integer.valueOf(input[0]));
        params.setHeight(Integer.valueOf(input[1])); params.setBase_precision(Double.valueOf(input[2]));
        params.setZoom(Double.valueOf(input[3])); params.setZoomlevel(Double.valueOf(input[4]));
        params.setDepth(Long.valueOf(input[5])); params.setFps(Integer.valueOf(input[6]));
        params.transforms = new Matrix[input.length - 7]; params.translators = new Matrix[input.length - 7];
        params.colors = new int[input.length - 7]; params.weights = new double[input.length - 7];
        for (int i = 7; i < input.length; i++) {
            String[] parts = input[i].split(" "); params.transforms[i] = Matrix.fromString(parts[0]);
            params.translators[i] = Matrix.fromString(parts[1]); params.weights[i] = Double.valueOf(parts[2]);
            params.colors[i] = Integer.valueOf(parts[3], 16);
        } return params;
    }
    public void setZoomConfig(ZoomConfig config) {zoomConfig = new ZoomConfig(config);}
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getZoomlevel() {
        return zoomlevel;
    }
    public void setZoomlevel(double zoomlevel) {
        this.zoomlevel = zoomlevel;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        this.base_precision = base_precision;
    }
}