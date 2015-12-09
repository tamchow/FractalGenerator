package in.tamchow.fractal.config.fractalconfig.IFS;
import in.tamchow.fractal.math.matrix.Matrix;
/**
 * Holds Parameters for an IFS fractal
 */
public class IFSFractalParams {
    Matrix[] transforms, translators;
    double[] weights;
    int[] colors;
    long depth;
    int zoom, zoomlevel, base_precision, width, height;
    public IFSFractalParams(IFSFractalParams config) {
        if (!(config.getColors().length == config.getWeights().length && config.getTransforms().length == config.getTranslators().length)) {
            throw new IllegalArgumentException("Configuration object is not properly defined");
        } setColors(config.getColors()); setWeights(config.getWeights()); setTransforms(config.getTransforms());
        setTranslators(config.getTranslators()); setDepth(config.getDepth());
    }
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
    public int getZoom() {
        return zoom;
    }
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
    public int getZoomlevel() {
        return zoomlevel;
    }
    public void setZoomlevel(int zoomlevel) {
        this.zoomlevel = zoomlevel;
    }
    public int getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(int base_precision) {
        this.base_precision = base_precision;
    }
    public void fromString(String[] input) {
        setWidth(Integer.valueOf(input[0])); setHeight(Integer.valueOf(input[1]));
        setBase_precision(Integer.valueOf(input[2])); setZoom(Integer.valueOf(input[3]));
        setZoomlevel(Integer.valueOf(input[4])); setDepth(Integer.valueOf(input[5]));
        transforms = new Matrix[input.length - 6]; translators = new Matrix[input.length - 6];
        colors = new int[input.length - 6]; weights = new double[input.length - 6];
        for (int i = 6; i < input.length; i++) {
            String[] parts = input[i].split(" "); transforms[i] = Matrix.fromString(parts[0]);
            translators[i] = Matrix.fromString(parts[1]); weights[i] = Double.valueOf(parts[2]);
            colors[i] = Integer.valueOf(parts[3], 16);
        }
    }
}
