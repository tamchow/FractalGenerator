package in.tamchow.fractal.graphics.containers;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.color.InterpolationType;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.math.matrix.Matrix;

import java.io.Serializable;
/**
 * Encapsulates an image or animation frame, for platform independence, takes int32 packed ARGB in hex values as pixels.
 */
public class PixelContainer implements Serializable, Pannable, Comparable<PixelContainer> {
    private static final int DEFAULT_WIDTH = 640, DFAULT_HEIGHT = 480;
    private String path;
    @Nullable
    private int[][] pixdata;
    public PixelContainer() {
        this(DEFAULT_WIDTH, DFAULT_HEIGHT);
    }
    public PixelContainer(int w, int h) {
        this("");
        pixdata = new int[h][w];
    }
    public PixelContainer(@NotNull int[][] pixdata) {
        this("");
        setPixdata(pixdata);
    }
    public PixelContainer(@NotNull PixelContainer img) {
        this(img.getPath());
        setPixdata(img.getPixdata());
    }
    public PixelContainer(String path) {
        this.path = path;
        pixdata = null;
    }
    @NotNull
    public static PixelContainer fromHSL(@NotNull HSL[][] input) {
        @NotNull PixelContainer img = new PixelContainer(input[0].length, input.length);
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                img.setPixel(i, j, input[i][j].toRGB());
            }
        }
        return img;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    @Nullable
    public int[][] getPixdata() {
        return pixdata;
    }
    public void setPixdata(@NotNull int[][] pixdata) {
        this.pixdata = new int[pixdata.length][pixdata[0].length];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }
    @NotNull
    protected int[] imageBounds(int y, int x) {
        return MathUtils.imageBounds(y, x, getWidth(), getHeight());
    }
    protected int normalized(int y, int x) {
        return MathUtils.normalized(y, x, getWidth(), getHeight());
    }
    /*public void setPixel(int y, int x, int val) {
        @NotNull int[] yx = imageBounds(y, x);
        //pixdata[yx[0]][yx[1]] = val;
        setPixel(yx[0] * getWidth() + yx[1], val);
    }
    */
    public void setPixel(int y, int x, int val) {
        setPixel(normalized(y, x), val);
    }
    @NotNull
    public HSL[][] toHSL() {
        @NotNull HSL[][] output = new HSL[pixdata.length][pixdata[0].length];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[i].length; j++) {
                output[i][j] = HSL.fromRGB(getPixel(i, j));
            }
        }
        return output;
    }
    /*public int getPixel(int y, int x) {
        @NotNull int[] yx = imageBounds(y, x);
        //return pixdata[yx[0]][yx[1]];
        return getPixel(yx[0] * getWidth() + yx[1]);
    }*/
    public int getPixel(int y, int x) {
        return getPixel(normalized(y, x));
    }
    public int getHeight() {
        if (pixdata == null) {
            return -1;
        }
        return pixdata.length;
    }
    public int getWidth() {
        if (pixdata == null) {
            return -1;
        }
        return pixdata[0].length;
    }
    @Nullable
    public int[] getRow(int row) {
        row = MathUtils.boundsProtected(row, getHeight());
        return pixdata[row];
    }
    @NotNull
    public PixelContainer getPostProcessed(@NotNull PostProcessMode mode, double[][] biases, int byParts, InterpolationType interpolationType, boolean gammaCorrection) {
        @NotNull PixelContainer processed = new PixelContainer(this);
        if (mode == PostProcessMode.NONE) {
            return processed;
        }
        for (int i = 1; i < processed.getHeight() - 1; i++) {
            for (int j = 1; j < processed.getWidth() - 1; j++) {
                int left = getPixel(i, j - 1), right = getPixel(i, j + 1), top = getPixel(i - 1, j), bottom = getPixel(i + 1, j);
                int top_left = getPixel(i - 1, j - 1), top_right = getPixel(i - 1, j + 1), bottom_left = getPixel(i + 1, j - 1), bottom_right = getPixel(i + 1, j + 1);
                double average = (top_left + top + top_right + left + right + bottom_left + bottom + bottom_right) / 8;
                int median = MathUtils.median(new int[]{top_left, top, top_right, left, right, bottom_left, bottom, bottom_right});
                switch (mode) {
                    case MEAN:
                        processed.setPixel(i, j, Math.round((float) average));
                        break;
                    case WEIGHTED_MEAN:
                        processed.setPixel(i, j, Math.round((float) ((average + getPixel(i, j)) / 2)));
                        break;
                    case INTERPOLATED_MEAN:
                        processed.setPixel(i, j, Colorizer.interpolated(Math.round((float) average), getPixel(i, j), biases[i][j] - (long) biases[i][j], byParts, interpolationType, gammaCorrection));
                        break;
                    case MEDIAN:
                        processed.setPixel(i, j, median);
                        break;
                    case WEIGHTED_MEDIAN:
                        processed.setPixel(i, j, Math.round((float) ((median + getPixel(i, j)) / 2)));
                        break;
                    case INTERPOLATED_MEDIAN:
                        processed.setPixel(i, j, Colorizer.interpolated(median, getPixel(i, j), biases[i][j] - (long) biases[i][j], byParts, interpolationType, gammaCorrection));
                        break;
                    case INTERPOLATED:
                        processed.setPixel(i, j, Colorizer.interpolated(getPixel(i, j - 1), getPixel(i, j), biases[i][j] - (long) biases[i][j], byParts, interpolationType, gammaCorrection));
                        break;
                    case NEGATIVE:
                        processed.setPixel(i, j, Colorizer.packARGB(
                                Colorizer.separateARGB(getPixel(i, j), Colors.RGBCOMPONENTS.ALPHA),
                                0xff - Colorizer.separateARGB(getPixel(i, j), Colors.RGBCOMPONENTS.RED),
                                0xff - Colorizer.separateARGB(getPixel(i, j), Colors.RGBCOMPONENTS.GREEN),
                                0xff - Colorizer.separateARGB(getPixel(i, j), Colors.RGBCOMPONENTS.BLUE)));
                        break;
                    case NONE:
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported Post Processing type");
                }
            }
        }
        return processed;
    }
    public PixelContainer toHeightMap() {
        return toHeightMap(255);
    }
    public PixelContainer toHeightMap(double scale) {
        return toHeightMap(getWidth(), getHeight(), scale);
    }
    public PixelContainer toHeightMap(int numXTiles, int numYTiles) {
        return toHeightMap(numXTiles, numYTiles, 255);
    }
    public PixelContainer toHeightMap(int numXTiles, int numYTiles, double scale) {
        int[][] tileHeights = new int[numYTiles + 1][numXTiles + 1];
        for (int y = 0; y < tileHeights.length; ++y) {
            for (int x = 0; x < tileHeights[y].length; ++x) {
                int pixelXPos = (int) Math.ceil(x * ((double) getWidth() - 1) / numXTiles);
                int pixelYPos = (int) Math.ceil(y * ((double) getHeight() - 1) / numYTiles);
                // Get the channels for the given pixel
                int red = Colorizer.separateARGB(getPixel(pixelYPos, pixelXPos), Colors.RGBCOMPONENTS.RED);
                int green = Colorizer.separateARGB(getPixel(pixelYPos, pixelXPos), Colors.RGBCOMPONENTS.GREEN);
                int blue = Colorizer.separateARGB(getPixel(pixelYPos, pixelXPos), Colors.RGBCOMPONENTS.BLUE);
                double alpha = Colorizer.separateARGB(getPixel(pixelYPos, pixelXPos), Colors.RGBCOMPONENTS.ALPHA);
                // Note that we divide by `alpha` to get the height
                // If we always use fully opaque images, alpha will be the maximum possible value
                // This allows us to handle 8bit, 16bit, or Nbit images
                // Values are based on ratios. We don't care about the images bit depth
                int heightAtThisPosition = (byte) (scale * (red + green + blue) / 3.0 / alpha);
                tileHeights[y][x] = Colorizer.toGray(heightAtThisPosition);
            }
        }
        return new PixelContainer(tileHeights);
    }
    public HeightFieldLoc[] generateHeightField() {
        HeightFieldLoc[] heightFieldLocs = new HeightFieldLoc[getHeight() * getWidth()];
        for (int i = 0; i < getHeight(); ++i) {
            for (int j = 0; j < getWidth(); ++j) {
                heightFieldLocs[i * getWidth() + j] = new HeightFieldLoc(i, j,
                        Colorizer.separateARGB(getPixel(i, j), Colors.RGBCOMPONENTS.RED) / 255.0);
            }
        }
        return heightFieldLocs;
    }
    public void setSize(int height, int width) {
        @NotNull int[][] tmp = new int[pixdata.length][pixdata[0].length];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, tmp[i], 0, tmp[i].length);
        }
        this.pixdata = new int[height][width];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(tmp[i], 0, this.pixdata[i], 0, Math.min(this.pixdata[i].length, tmp[i].length));
        }
    }
    public void setPixdata(@NotNull int[] pixdata, int scan) {
        this.pixdata = new int[pixdata.length / scan][scan];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata, i * scan, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }
    public int[] getPixels() {
        @NotNull int[] pixels = new int[pixdata.length * pixdata[0].length];
        for (int i = 0; i < pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, pixels, i * pixdata[i].length, pixdata[i].length);
        }
        return pixels;
    }
    @NotNull
    public Matrix fromCooordinates(int x, int y) {
        double scale = ((getHeight() >= getWidth()) ? getWidth() / 2 : getHeight() / 2);
        int center_x = getWidth() / 2, center_y = getHeight() / 2;
        x = MathUtils.boundsProtected(x, getWidth());
        y = MathUtils.boundsProtected(y, getHeight());
        @NotNull double[][] matrixData = new double[2][1];
        matrixData[0][0] = ((((double) x) - center_x) / scale);
        matrixData[1][0] = ((center_y - ((double) y)) / scale);
        return new Matrix(matrixData);
    }
    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof PixelContainer && dataEquals((PixelContainer) o));
    }
    private boolean dataEquals(@NotNull PixelContainer o) {
        for (int i = 0; i < o.getWidth() * o.getHeight() && i < this.getHeight() * this.getWidth(); ++i) {
            if (getPixel(i) != o.getPixel(i)) return false;
        }
        return true;
    }
    @NotNull
    public PixelContainer falseColor(@NotNull PixelContainer[] channels) {
        if (channels.length == 0) {
            return this;//don't throw an exception
        } else if (channels.length == 1) {
            return falseColor(channels[0], channels[0], channels[0]);//grayscale
        } else if (channels.length == 2) {
            return falseColor(channels[0], channels[1], channels[1]);
        } else if (channels.length == 3) {
            return falseColor(channels[0], channels[1], channels[2]);
        } else {
            return falseColor(channels[0], channels[1], channels[2], channels[3]);
        }
    }
    public void add(@NotNull PixelContainer toAdd) {
        add(toAdd, false, null);
    }
    public void add(@NotNull PixelContainer toAdd, boolean blend, @Nullable double[][] biases) {
        if (toAdd.getHeight() != getHeight() || toAdd.getWidth() != getWidth()) {
            throw new IllegalArgumentException("Cannot add mismatched dimension containers");
        }
        if (blend) {
            if (biases == null) {
                for (int i = 0; i < Math.min(getHeight(), toAdd.getHeight()); i++) {
                    for (int j = 0; j < Math.min(getWidth(), toAdd.getWidth()); j++) {
                        //median color between 2 extremes
                        setPixel(i, j, Colorizer.interpolated(getPixel(i, j), toAdd.getPixel(i, j), 0.5, 0,
                                InterpolationType.LINEAR, false));
                    }
                }
            } else {
                if (biases.length != getHeight() || biases[0].length != getWidth()) {
                    throw new IllegalArgumentException("Cannot blend containers for mismatched dimensions of pixel biases");
                }
                for (int i = 0; i < Math.min(getHeight(), toAdd.getHeight()); i++) {
                    for (int j = 0; j < Math.min(getWidth(), toAdd.getWidth()); j++) {
                        //median color between 2 extremes
                        setPixel(i, j, Colorizer.interpolated(getPixel(i, j), toAdd.getPixel(i, j), biases[i][j], 0,
                                InterpolationType.LINEAR, false));
                    }
                }
            }
        } else {
            for (int i = 0; i < Math.min(getHeight(), toAdd.getHeight()); i++) {
                for (int j = 0; j < Math.min(getWidth(), toAdd.getWidth()); j++) {
                    //simple addition
                    setPixel(i, j, getPixel(i, j) + toAdd.getPixel(i, j));
                }
            }
        }
    }
    @NotNull
    public PixelContainer falseColor(@NotNull PixelContainer r, @NotNull PixelContainer g, @NotNull PixelContainer b) {
        int nwidth = MathUtils.min(r.getWidth(), g.getWidth(), b.getWidth()),
                nheight = MathUtils.min(r.getHeight(), g.getHeight(), b.getHeight());
        @NotNull PixelContainer falseColored = new PixelContainer(nwidth, nheight);
        for (int i = 0; i < falseColored.getHeight(); i++) {
            for (int j = 0; j < falseColored.getWidth(); j++) {
                falseColored.setPixel(i, j, Colorizer.toRGB(Colorizer.separateARGB(r.getPixel(i, j), Colors.RGBCOMPONENTS.RED), Colorizer.separateARGB(g.getPixel(i, j), Colors.RGBCOMPONENTS.GREEN), Colorizer.separateARGB(b.getPixel(i, j), Colors.RGBCOMPONENTS.BLUE)));
            }
        }
        return falseColored;
    }
    @NotNull
    public PixelContainer falseColor(PixelContainer a, @NotNull PixelContainer r, @NotNull PixelContainer g, @NotNull PixelContainer b) {
        int nwidth = MathUtils.min(a.getWidth(), r.getWidth(), g.getWidth(), b.getWidth()),
                nheight = MathUtils.min(a.getHeight(), r.getHeight(), g.getHeight(), b.getHeight());
        @NotNull PixelContainer falseColored = new PixelContainer(nwidth, nheight);
        for (int i = 0; i < falseColored.getHeight(); i++) {
            for (int j = 0; j < falseColored.getWidth(); j++) {
                falseColored.setPixel(i, j, Colorizer.packARGB(Colorizer.separateARGB(r.getPixel(i, j), Colors.RGBCOMPONENTS.ALPHA), Colorizer.separateARGB(r.getPixel(i, j), Colors.RGBCOMPONENTS.RED), Colorizer.separateARGB(g.getPixel(i, j), Colors.RGBCOMPONENTS.GREEN), Colorizer.separateARGB(b.getPixel(i, j), Colors.RGBCOMPONENTS.BLUE)));
            }
        }
        return falseColored;
    }
    @NotNull
    public int[] toCooordinates(@NotNull Matrix point) {
        double scale = ((getHeight() >= getWidth()) ? getWidth() / 2 : getHeight() / 2);
        int center_x = getWidth() / 2, center_y = getHeight() / 2;
        int x = (int) ((point.get(0, 0) * scale) + center_x), y = (int) (center_y - (point.get(1, 0) * scale));
        x = MathUtils.boundsProtected(x, getWidth());
        y = MathUtils.boundsProtected(y, getHeight());
        return new int[]{x, y};
    }
    @NotNull
    public PixelContainer getRotatedImage(double angle) {
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = getWidth(), h = getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        @NotNull PixelContainer rotated = new PixelContainer(neww, newh);
        //Matrix rotor = Matrix.rotationMatrix2D(angle);
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                @NotNull Matrix coords = fromCooordinates(j, i);
                //coords = MatrixOperations.multiply(rotor, coords);
                coords = MathUtils.doRotate(coords, angle);
                @NotNull int[] rcoords = toCooordinates(coords);
                rotated.setPixel(rcoords[1], rcoords[0], getPixel(i, j));
            }
        }
        return rotated;
    }
    public int getNum_pixels() {
        return getWidth() * getHeight();
    }
    public int getPixel(int i) {
        i = MathUtils.boundsProtected(i, getNum_pixels());
        //return getPixel(i / getWidth(), i % getWidth());
        return pixdata[i / getWidth()][i % getWidth()];
    }
    public void setPixel(int i, int val) {
        i = MathUtils.boundsProtected(i, getNum_pixels());
        pixdata[i / getWidth()][i % getWidth()] = val;
    }
    @Override
    public void pan(int distance, double angle) {
        pan(distance, angle, false);
    }
    @Override
    public void pan(int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    @Override
    public void pan(int x_dist, int y_dist) {
        pan(getWidth(), getHeight(), x_dist, y_dist);
    }
    public void pan(int x_res, int y_res, int x_dist, int y_dist) {
        if (x_res + x_dist >= getWidth() || y_res + y_dist >= getHeight() || x_res + x_dist < 0 || y_res + y_dist < 0) {
            throw new UnsupportedOperationException("Panning out of range");
        } else {
            @NotNull PixelContainer tmp = new PixelContainer(x_res, y_res);
            int start_x = ((getWidth() - x_res) / 2) + x_dist, start_y = ((getHeight() - y_res) / 2) + y_dist;
            int end_x = (getWidth() - (start_x - x_dist)) + x_dist, end_y = (getHeight() - (start_y - y_dist)) + y_dist;
            for (int i = start_y, k = 0; i < end_y && k < tmp.getHeight(); i++, k++) {
                for (int j = start_x, l = 0; j < end_x && l < tmp.getWidth(); j++, l++) {
                    tmp.setPixel(k, l, getPixel(i, j));
                }
            }
            setPixdata(tmp.getPixdata());
        }
    }
    public void pan(int x_res, int y_res, int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        pan(x_res, y_res, (int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    public void pan(int x_res, int y_res, int distance, double angle) {
        pan(x_res, y_res, distance, angle, false);
    }
    @NotNull
    public PixelContainer subImage(int x_res, int y_res) {
        @NotNull PixelContainer subImage = new PixelContainer(this);
        subImage.pan(x_res, y_res, 0, 0);
        return subImage;
    }
    @Override
    public int compareTo(@NotNull PixelContainer o) {
        int difference = 0;
        for (int i = 0; i < getHeight() && i < o.getHeight(); ++i) {
            for (int j = 0; j < getWidth() && j < o.getWidth(); ++j) {
                difference += getPixel(j - i) - o.getPixel(j, i);
            }
        }
        return difference ^ getPath().compareTo(o.getPath());
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String pixels = getPath() + "," + getHeight() + "," + getWidth() + "\n";
        for (int i = 0; i < getHeight(); ++i) {
            for (int j = 0; j < getWidth(); ++j) {
                pixels += " " + getPixel(i, j);
            }
            pixels += "\n";
        }
        return pixels.trim();
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    public enum PostProcessMode {MEAN, MEDIAN, WEIGHTED_MEAN, WEIGHTED_MEDIAN, INTERPOLATED_MEAN, INTERPOLATED_MEDIAN, INTERPOLATED, NEGATIVE, NONE, TEXT_TO_IMAGE}
    public final class HeightFieldLoc {
        public final int x, y;
        public final double height;
        public HeightFieldLoc(int x, int y, double height) {
            this.x = x;
            this.y = y;
            this.height = height;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeightFieldLoc that = (HeightFieldLoc) o;
            return x == that.x && y == that.y && Double.compare(that.height, height) == 0;
        }
        @Override
        public int hashCode() {
            int result;
            long temp;
            result = x;
            result = 31 * result + y;
            temp = Double.doubleToLongBits(height);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
        @Override
        public String toString() {
            return String.format("%d,%d,%f", x, y, height);
        }
    }
}