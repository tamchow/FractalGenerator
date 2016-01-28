package in.tamchow.fractal.imgutils;
import in.tamchow.fractal.helpers.MathUtils;

import java.io.Serializable;
/**
 * Holds an image as an SDA, extends ImageData
 */
public class LinearizedImageData extends ImageData implements Serializable {
    private int[] pixdata;
    private int width;
    public LinearizedImageData(ImageData source) {
        if (source instanceof LinearizedImageData) {
            this.width = source.getWidth(); this.pixdata = new int[source.getPixels().length];
            System.arraycopy(source.getPixels(), 0, pixdata, 0, pixdata.length);
        } else {
            pixdata = new int[source.getHeight() * source.getWidth()]; width = source.getWidth();
            for (int i = 0; i < source.getHeight(); i++) {
                System.arraycopy(source.getPixdata()[i], 0, pixdata, i * width, width);
            }
        }
    }
    public LinearizedImageData(int[] pixdata, int width) {
        this.width = width; this.pixdata = new int[pixdata.length];
        System.arraycopy(pixdata, 0, this.pixdata, 0, this.pixdata.length);
    }
    public LinearizedImageData(int[][] pixels) {
        pixdata = new int[pixels.length * pixels[0].length]; width = pixels[0].length;
        for (int i = 0; i < pixels.length; i++) {System.arraycopy(pixels[i], 0, pixdata, i * width, width);}
    }
    public int[] getPixels() {return pixdata;}
    public LinearizedImageData(int width, int height) {this.width = width; pixdata = new int[height * width];}

    public int[][] getPixdata() {
        int[][] pixels = new int[pixdata.length / width][width]; for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(pixdata, i * width, pixels[i], 0, pixels[i].length);
        } return pixels;
    }
    public ImageData toImageData() {return new ImageData(getPixdata());}
    public int getWidth() {return width;}
    public int getHeight() {return pixdata.length / width;}
    public int getPixel(int y, int x) {
        if (x < 0) {y -= x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        if (x >= getWidth()) {y += x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        y = MathUtils.boundsProtected(y, getHeight()); return pixdata[y * width + x];
    }
    public void setPixel(int y, int x, int val) {
        if (x < 0) {y -= x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        if (x >= getWidth()) {y += x / getWidth(); x = MathUtils.boundsProtected(x, getWidth());}
        y = MathUtils.boundsProtected(y, getHeight()); pixdata[y * width + x] = val;
    }
    public int getPixel(int i) {return pixdata[i];}
    public void setPixel(int i, int val) {pixdata[i] = val;}
    public LinearizedImageData getPostProcessed(PostProcessMode mode, double[][] biases, boolean byParts) {
        return new LinearizedImageData(toImageData().getPostProcessed(mode, biases, byParts));
    }
}