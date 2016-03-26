package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomParams;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.helpers.MathUtils;
import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.LinearizedImageData;
import in.tamchow.fractal.imgutils.Pannable;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.ComplexOperations;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.math.matrix.MatrixOperations;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * Complex brot fractal generator
 * NOTE: Length of the iterations[] can be only 1 or 3
 * TODO: Implement
 */
public class ComplexBrotFractalGenerator implements Serializable, Pannable {
    ComplexBrotFractalParams params;
    ComplexFractalGenerator.Mode mode;
    Publisher progressPublisher;
    ImageData plane;
    ArrayList<Complex> roots;
    int center_x, center_y, lastConstantIdx;
    double zoom, zoom_factor, base_precision, scale;
    long depth;
    Complex centre_offset, lastConstant;
    Complex[][] plane_map;
    String[][] constants;
    String function;
    int[][][] bases;
    public ComplexBrotFractalGenerator(ComplexBrotFractalParams params, Publisher publisher) {
        this.params = params; setProgressPublisher(publisher);
    }
    public int[][][] getBases() {return bases;}
    public void createImage() {
        if (bases.length == 1) {
            for (int i = 0; i < plane.getHeight(); i++) {
                for (int j = 0; j < plane.getWidth(); j++) {
                    plane.setPixel(i, j, getColor(i, j, 0));
                }
            }
        } else {
            ImageData level1 = new LinearizedImageData(plane.getHeight(), plane.getWidth());
            for (int i = 0; i < level1.getHeight(); i++) {
                for (int j = 0; j < level1.getWidth(); j++) {
                    level1.setPixel(i, j, getColor(i, j, 0));
                }
            } ImageData level2 = new LinearizedImageData(plane.getHeight(), plane.getWidth());
            for (int i = 0; i < level2.getHeight(); i++) {
                for (int j = 0; j < level2.getWidth(); j++) {
                    level2.setPixel(i, j, getColor(i, j, 1));
                }
            } ImageData level3 = new LinearizedImageData(plane.getHeight(), plane.getWidth());
            for (int i = 0; i < level3.getHeight(); i++) {
                for (int j = 0; j < level3.getWidth(); j++) {
                    level3.setPixel(i, j, getColor(i, j, 2));
                }
            } plane = plane.falseColor(level1, level2, level3);
        }
    }
    private int getColor(int i, int j, int level) {
        return MathUtils.boundsProtected(Math.round((float) bases[level][i][j] / getMaximum(bases[level])) * 255, 256);
    }
    private int getMaximum(int[][] base) {
        int max = -1; for (int[] row : base) {
            for (int val : row) {
                if (val > max) max = val;
            }
        } return max;
    }
    public Publisher getProgressPublisher() {return progressPublisher;}
    public void setProgressPublisher(Publisher progressPublisher) {this.progressPublisher = progressPublisher;}
    public ComplexBrotFractalParams getParams() {return params;}
    public void setParams(ComplexBrotFractalParams params) {this.params = params;}
    public ImageData getPlane() {return plane;}
    public int[] toCooordinates(Complex point) {
        point = ComplexOperations.subtract(point, centre_offset); if (Math.abs(params.skew) >= params.tolerance) {
            Matrix rotor = Matrix.rotationMatrix2D(params.skew).inverse();
            point = MathUtils.matrixToComplex(MatrixOperations.multiply(rotor, MathUtils.complexToMatrix(point)));
        } int x = (int) ((point.real() * scale) + center_x), y = (int) (center_y - (point.imaginary() * scale));
        x = MathUtils.boundsProtected(x, plane.getWidth()); y = MathUtils.boundsProtected(y, plane.getHeight());
        return new int[]{x, y};
    }
    public void zoom(ZoomParams zoom) {
        if (zoom.centre == null) {zoom(zoom.centre_x, zoom.centre_y, zoom.level);} else {zoom(zoom.centre, zoom.level);}
    }
    public void mandelbrotToJulia(Matrix constant, double level) {zoom(constant, level); changeMode(centre_offset); resetCentre();}
    public void resetCentre() {
        setCenter_x(plane.getWidth() / 2); setCenter_y(plane.getHeight() / 2); resetCentre_Offset();
    }
    public void resetCentre_Offset() {centre_offset = new Complex(0);}
    private void changeMode(Complex lastConstant) {
        setLastConstant(lastConstant);
        setMode((mode == ComplexFractalGenerator.Mode.BUDDHABROT) ? ComplexFractalGenerator.Mode.JULIABROT : ComplexFractalGenerator.Mode.BUDDHABROT);
    }
    private void setMode(ComplexFractalGenerator.Mode mode) {
        this.mode = mode;
    }
    public void zoom(Matrix centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(centre_offset, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(centre_offset, level));
        } zoom(new Complex(centre_offset.get(0, 0), centre_offset.get(1, 0)), level);
    }
    public void zoom(Complex centre_offset, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(MathUtils.complexToMatrix(centre_offset), level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(MathUtils.complexToMatrix(centre_offset), level));
        } setCentre_offset(centre_offset); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor));
        //setCenter_x(toCooordinates(centre_offset)[0]);setCenter_y(toCooordinates(centre_offset)[1]);
        populateMap();
    }
    public void populateMap() {
        for (int i = 0; i < plane.getHeight(); i++) {
            for (int j = 0; j < plane.getWidth(); j++) {plane_map[i][j] = fromCooordinates(j, i);}
        }
    }
    public Complex fromCooordinates(int x, int y) {
        x = MathUtils.boundsProtected(x, plane.getWidth()); y = MathUtils.boundsProtected(y, plane.getHeight());
        Complex point = new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale));
        if (Math.abs(params.skew) > params.tolerance) {
            Matrix rotor = Matrix.rotationMatrix2D(params.skew);
            point = MathUtils.matrixToComplex(MatrixOperations.multiply(rotor, MathUtils.complexToMatrix(point)));
        } return ComplexOperations.add(centre_offset, point);
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    private int indexOfRoot(Complex z) {
        for (int i = 0; i < roots.size(); i++) {
            if (ComplexOperations.distance_squared(roots.get(i), z) < params.tolerance) {return i;}
        } return -1;
    }
    private int closestRootIndex(Complex z) {
        int leastDistanceIdx = 0; double leastDistance = ComplexOperations.distance_squared(z, roots.get(0));
        for (int i = 1; i < roots.size(); i++) {
            double distance = ComplexOperations.distance_squared(z, roots.get(i));
            if (distance < leastDistance) {leastDistance = distance; leastDistanceIdx = i;}
        } return leastDistanceIdx;
    }
    public void mandelbrotToJulia(int cx, int cy, double level) {zoom(cx, cy, level); changeMode(centre_offset); resetCentre();}
    public void zoom(int cx, int cy, double level) {
        if (params.zoomConfig.zooms == null) {
            params.zoomConfig.setZooms(new ZoomParams[]{new ZoomParams(cx, cy, level)});
        } else {
            params.zoomConfig.addZoom(new ZoomParams(cx, cy, level));
        } cx = MathUtils.boundsProtected(cx, plane.getWidth()); cy = MathUtils.boundsProtected(cy, plane.getHeight());
        //setCenter_x(cx);setCenter_y(cy);
        setCentre_offset(fromCooordinates(cx, cy)); setZoom_factor(level);
        setScale(base_precision * Math.pow(zoom, zoom_factor)); populateMap();
    }
    public void setconstants(String[][] constants) {
        this.constants = new String[constants.length][constants[0].length]; for (int i = 0; i < constants.length; i++) {
            System.arraycopy(constants[i], 0, this.constants[i], 0, constants[i].length);
        }
    }
    public void mandelbrotToJulia(Complex constant, double level) {zoom(constant, level); changeMode(centre_offset); resetCentre();}
    public void mandelbrotToJulia(ZoomParams zoom) {zoom(zoom); changeMode(centre_offset); resetCentre();}
    public int getCenter_x() {
        return center_x;
    }
    public void setCenter_x(int center_x) {
        this.center_x = center_x;
    }
    public int getCenter_y() {
        return center_y;
    }
    public void setCenter_y(int center_y) {
        this.center_y = center_y;
    }
    public double getZoom() {
        return zoom;
    }
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getZoom_factor() {
        return zoom_factor;
    }
    public void setZoom_factor(double zoom_factor) {
        this.zoom_factor = zoom_factor;
    }
    public double getBase_precision() {
        return base_precision;
    }
    public void setBase_precision(double base_precision) {
        if (base_precision <= 0) {
            this.base_precision = calculateBasePrecision();
        } else {this.base_precision = base_precision;}
    }
    public double calculateBasePrecision() {
        return ((plane.getHeight() >= plane.getWidth()) ? plane.getWidth() / 2 : plane.getHeight() / 2);
    }
    public long getDepth() {
        return depth;
    }
    public void setDepth(long depth) {
        this.depth = depth;
    }
    public Complex getCentre_offset() {
        return centre_offset;
    }
    public void setCentre_offset(Complex centre_offset) {
        this.centre_offset = centre_offset;
    }
    public Complex getLastConstant() {
        if (lastConstant.equals(new Complex(-1, 0))) {
            if (getLastConstantIndex() == -1) {
                lastConstant = new Complex(constants[0][1]);
            } else {lastConstant = new Complex(constants[getLastConstantIndex()][1]);}
        } return lastConstant;
    }
    public void setLastConstant(Complex value) {
        constants[getLastConstantIndex()][1] = value.toString(); lastConstant = new Complex(value);
    }
    public int getLastConstantIndex() {
        String[] parts = StringManipulator.split(function, " ");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (getConstantIndex(parts[i]) != -1) {
                setLastConstantIdx(getConstantIndex(parts[i])); return lastConstantIdx;
            }
        } return -1;
    }
    public int getConstantIndex(String constant) {
        for (int i = 0; i < constants.length; i++) {if (constants[i][0].equals(constant)) {return i;}} return -1;
    }
    public void setLastConstantIdx(int lastConstantIdx) {
        this.lastConstantIdx = lastConstantIdx;
    }
    @Override
    public void pan(int distance, double angle) {pan(distance, angle, false);}
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        zoom(center_x + x_dist, center_y + y_dist, zoom_factor);
    }
    public void generate() {}
}