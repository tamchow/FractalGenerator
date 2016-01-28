package in.tamchow.fractal.imgutils;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.HSL;
import in.tamchow.fractal.helpers.MathUtils;
/**
 * Encapsulates an image or animation frame, for platform independence, takes int32 packed ARGB in hex values as pixels.
 */
public class ImageData {
    private String path;
    private int[][] pixdata;
    public ImageData() {
        path = ""; pixdata = new int[801][801]; for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {pixdata[i][j] = 0x000000;}
        }
    }
    public ImageData(int w, int h) {
        path = ""; pixdata = new int[h][w]; for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {pixdata[i][j] = 0x000000;}
        }
    }
    public ImageData(int[][] pixdata) {path = ""; setPixdata(pixdata);}
    public ImageData(ImageData img) {setPixdata(img.getPixdata()); path = img.getPath();}
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int[][] getPixdata() {
        return pixdata;
    }
    public void setPixdata(int[][] pixdata) {
        this.pixdata = new int[pixdata.length][pixdata[0].length];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }
    public ImageData(String path) {this.path = path; pixdata = null;}
    public static ImageData fromHSL(HSL[][] input) {
        ImageData img = new ImageData(input[0].length, input.length); for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {img.setPixel(i, j, input[i][j].toRGB());}
        } return img;
    }
    public void fill(int color) {
        for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {pixdata[i][j] = color;}
        }
    }
    public void drawRect(int startx, int starty, int endx, int endy, int thickness, int color) {
        int oldcolor = pixdata[(endy - starty) / 2][(endx - startx) / 2]; fillRect(startx, starty, endx, endy, color);
        fillRect(startx + thickness, starty + thickness, endx - thickness, endy - thickness, oldcolor);
    }
    public void fillRect(int startx, int starty, int endx, int endy, int color) {
        for (int i = starty; i < endy; i++) {
            for (int j = startx; j < endx; j++) {pixdata[i][j] = color;}
        }
    }
    public synchronized void setPixel(int y, int x, int val) {
        if (y < 0) {y += getHeight(); setPixel(y, x, val);}
        if (y >= getHeight()) {y -= getHeight(); setPixel(y, x, val);}
        if (x < 0) {x += getWidth(); setPixel(y, x, val);} if (x >= getWidth()) {x -= getWidth(); setPixel(y, x, val);}
        pixdata[y][x] = val;
    }
    public HSL[][] toHSL() {
        HSL[][] output = new HSL[pixdata.length][pixdata[0].length]; for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[i].length; j++) {output[i][j] = HSL.fromRGB(getPixel(i, j));}
        } return output;
    }
    public synchronized int getPixel(int y, int x) {
        if (x < 0) {y -= x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        if (x >= getWidth()) {y += x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        y = MathUtils.boundsProtected(y, getHeight()); return pixdata[y][x];
    }
    public int getHeight() {if (pixdata == null) {return -1;} return pixdata.length;}
    public int getWidth() {if (pixdata == null) {return -1;} return pixdata[0].length;}
    public int[] getRow(int row) {row = MathUtils.boundsProtected(row, getHeight()); return pixdata[row];}
    public ImageData getPostProcessed(PostProcessMode mode, double[][] biases, boolean byParts) {
        ImageData processed = new ImageData(this); if (mode == PostProcessMode.NONE) {return processed;}
        for (int i = 1; i < processed.getPixdata().length - 1; i++) {
            for (int j = 1; j < processed.getPixdata()[i].length - 1; j++) {
                int left = pixdata[i][j - 1], right = pixdata[i][j + 1], top = pixdata[i - 1][j], bottom = pixdata[i + 1][j];
                int top_left = pixdata[i - 1][j - 1], top_right = pixdata[i - 1][j + 1], bottom_left = pixdata[i + 1][j - 1], bottom_right = pixdata[i + 1][j + 1];
                double average = (top_left + top + top_right + left + right + bottom_left + bottom + bottom_right) / 8;
                switch (mode) {
                    case AVERAGE: processed.setPixel(i, j, (int) average); break;
                    case WEIGHTED_AVERAGE: processed.setPixel(i, j, (int) ((average + pixdata[i][j]) / 2)); break;
                    case INTERPOLATED_AVERAGE: processed.setPixel(i, j, ColorConfig.linearInterpolated((int) average, pixdata[i][j], biases[i][j] - (long) biases[i][j], byParts)); break;
                    case INTERPOLATED: processed.setPixel(i, j, ColorConfig.linearInterpolated(getPixel(i, j - 1), getPixel(i, j), biases[i][j] - (long) biases[i][j], byParts)); break;
                    case NONE: break;
                    default: throw new IllegalArgumentException("Unsupported Post Processing type");
                }
            }
        } return processed;
    }
    public synchronized void setSize(int height, int width) {
        int[][] tmp = new int[pixdata.length][pixdata[0].length]; for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, tmp[i], 0, this.pixdata[i].length);} this.pixdata = new int[height][width];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(tmp[i], 0, this.pixdata[i], 0, this.pixdata[i].length);}}
    public synchronized void setPixdata(int[] pixdata, int scan) {
        this.pixdata = new int[pixdata.length / scan][scan];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata, i * scan, this.pixdata[i], 0, this.pixdata[i].length);}}
    public synchronized int[] getPixels() {
        int[] pixels = new int[pixdata.length * pixdata[0].length]; for (int i = 0; i < pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, pixels, i * pixdata[i].length, pixdata[i].length);
        } return pixels;
    }
    public synchronized int getPixel(int i) {return getPixel(i / pixdata[0].length, i % pixdata[0].length);}
    public synchronized void setPixel(int i, int val) {setPixel(i / pixdata[0].length, i % pixdata[0].length, val);}
    public ImageData pan(int x_res, int y_res, int x_dist, int y_dist) {
        if (x_res + x_dist >= getWidth() || y_res + y_dist >= getHeight() || x_res + x_dist < 0 || y_res + y_dist < 0) {
            throw new UnsupportedOperationException("Panning out of range");
        } else {
            ImageData tmp = new ImageData(x_res, y_res);
            int start_x = ((getWidth() - x_res) / 2) + x_dist, start_y = ((getHeight() - y_res) / 2) + y_dist;
            int end_x = (getWidth() - ((getWidth() - x_res) / 2)) + x_dist, end_y = (getHeight() - ((getHeight() - y_res) / 2)) + y_dist;
            for (int i = start_y, k = 0; i < end_y && k < tmp.getHeight(); i++, k++) {
                for (int j = start_x, l = 0; j < end_x && l < tmp.getWidth(); j++, l++) {
                    tmp.setPixel(k, l, getPixel(i, j));
                }
            } return tmp;
        }
    }
    public ImageData pan(int x_res, int y_res, int distance, double angle, boolean flip_axes) {
        angle = (flip_axes) ? (Math.PI / 2) - angle : angle;
        return pan(x_res, y_res, (int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)));
    }
    public ImageData pan(int x_res, int y_res, int distance, double angle) {
        return pan(x_res, y_res, distance, angle, false);
    }
    public enum PostProcessMode {AVERAGE, WEIGHTED_AVERAGE, INTERPOLATED_AVERAGE, INTERPOLATED, NONE}
}