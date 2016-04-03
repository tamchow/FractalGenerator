package in.tamchow.fractal.graphicsutilities.containers;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
/**
 * Holds an image as an SDA, extends PixelContainer
 */
public class LinearizedPixelContainer extends PixelContainer implements Serializable {
    private int[] pixdata;
    private int width;
    public LinearizedPixelContainer(PixelContainer source) {
        if (source instanceof LinearizedPixelContainer) {
            this.width = source.getWidth();
            this.pixdata = new int[source.getPixels().length];
            System.arraycopy(source.getPixels(), 0, pixdata, 0, pixdata.length);
        } else {
            pixdata = new int[source.getHeight() * source.getWidth()];
            width = source.getWidth();
            for (int i = 0; i < source.getHeight(); i++) {
                System.arraycopy(source.getPixdata()[i], 0, pixdata, i * width, width);
            }
        }
    }
    public LinearizedPixelContainer(int[] pixdata, int width) {
        this.width = width;
        this.pixdata = new int[pixdata.length];
        System.arraycopy(pixdata, 0, this.pixdata, 0, this.pixdata.length);
    }
    public LinearizedPixelContainer(int[][] pixels) {
        pixdata = new int[pixels.length * pixels[0].length];
        width = pixels[0].length;
        for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(pixels[i], 0, pixdata, i * width, width);
        }
    }
    public LinearizedPixelContainer(int width, int height) {
        this.width = width;
        pixdata = new int[height * width];
    }
    @Override
    public int[] getPixels() {
        return pixdata;
    }
    @Override
    public int[][] getPixdata() {
        int[][] pixels = new int[pixdata.length / width][width];
        for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(pixdata, i * width, pixels[i], 0, pixels[i].length);
        }
        return pixels;
    }
    public PixelContainer toImageData() {
        return new PixelContainer(getPixdata());
    }
    @Override
    public int getWidth() {
        return width;
    }
    @Override
    public int getHeight() {
        return pixdata.length / width;
    }
    @Override
    public int getPixel(int y, int x) {
        if (x < 0) {
            y -= x / getWidth();
            x = MathUtils.boundsProtected(x, getWidth());
        }
        if (x >= getWidth()) {
            y += x / getWidth();
            x = MathUtils.boundsProtected(x, getWidth());
        }
        y = MathUtils.boundsProtected(y, getHeight());
        return pixdata[y * width + x];
    }
    @Override
    public void setPixel(int y, int x, int val) {
        if (x < 0) {
            y -= x / getWidth();
            x = MathUtils.boundsProtected(x, getWidth());
        }
        if (x >= getWidth()) {
            y += x / getWidth();
            x = MathUtils.boundsProtected(x, getWidth());
        }
        y = MathUtils.boundsProtected(y, getHeight());
        pixdata[y * width + x] = val;
    }
    @Override
    public int getPixel(int i) {
        return pixdata[i];
    }
    @Override
    public void setPixel(int i, int val) {
        pixdata[i] = val;
    }
    @Override
    public LinearizedPixelContainer getPostProcessed(PostProcessMode mode, double[][] biases, int byParts) {
        return new LinearizedPixelContainer(toImageData().getPostProcessed(mode, biases, byParts));
    }
}