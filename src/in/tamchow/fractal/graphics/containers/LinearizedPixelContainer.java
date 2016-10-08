package in.tamchow.fractal.graphics.containers;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
/**
 * Holds an image as an SDA, extends PixelContainer
 */
public final class LinearizedPixelContainer extends PixelContainer implements Serializable {
    private int[] pixdata;
    private int width;
    public LinearizedPixelContainer(PixelContainer source) {
        /*if (source instanceof LinearizedPixelContainer) {
            this.width = source.getWidth();
            this.pixdata = new int[source.getPixels().length];
            System.arraycopy(source.getPixels(), 0, pixdata, 0, pixdata.length);
        } else {
            pixdata = new int[source.getHeight() * source.getWidth()];
            width = source.getWidth();
            for (int i = 0; i < source.getHeight(); i++) {
                System.arraycopy(source.getPixdata()[i], 0, pixdata, i * width, width);
            }
        }*/
        setPixdata(source.getPixdata());
    }
    public LinearizedPixelContainer(@NotNull int[] pixdata, int width) {
        setPixdata(pixdata, width);
    }
    public LinearizedPixelContainer(@NotNull int[][] pixels) {
        setPixdata(pixels);
    }
    public LinearizedPixelContainer(int width, int height) {
        this.width = width;
        pixdata = new int[height * width];
    }
    @Override
    public int[] getRow(int idx) {
        idx = MathUtils.boundsProtected(idx, getHeight());
        int[] row = new int[width];
        System.arraycopy(pixdata, idx * width, row, 0, width);
        return row;
    }
    @Override
    public void setSize(int width, int height) {
        this.width = width;
        @NotNull int[] tmpData = new int[pixdata.length];
        System.arraycopy(pixdata, 0, tmpData, 0, tmpData.length);
        pixdata = new int[width * height];
        System.arraycopy(tmpData, 0, pixdata, 0, Math.min(pixdata.length, tmpData.length));
    }
    @Override
    public int[] getPixels() {
        return pixdata;
    }
    @NotNull
    @Override
    public int[][] getPixdata() {
        @NotNull int[][] pixels = new int[pixdata.length / width][width];
        for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(pixdata, i * width, pixels[i], 0, pixels[i].length);
        }
        return pixels;
    }
    @Override
    public void setPixdata(@NotNull int[][] pixels) {
        pixdata = new int[pixels.length * pixels[0].length];
        width = pixels[0].length;
        for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(pixels[i], 0, pixdata, i * width, width);
        }
    }
    @NotNull
    public PixelContainer toPixelContainer() {
        return new PixelContainer(getPixdata());
    }
    @Override
    public void setPixdata(@NotNull int[] pixdata, int scan) {
        this.width = scan;
        this.pixdata = new int[pixdata.length];
        System.arraycopy(pixdata, 0, this.pixdata, 0, pixdata.length);
    }
    @Override
    public int getWidth() {
        return width;
    }
    @Override
    public int getHeight() {
        return pixdata.length / width;
    }
    /*@Override
    public int getPixel(int y, int x) {
        @NotNull int[] yx = imageBounds(y, x);
        return pixdata[yx[0] * getWidth() + yx[1]];
    }
    @Override
    public int getNum_pixels() {
        return pixdata.length;
    }
    @Override
    public void setPixel(int y, int x, int val) {
        @NotNull int[] yx = imageBounds(y, x);
        pixdata[yx[0] * getWidth() + yx[1]] = val;
    }*/
    @Override
    public int getPixel(int i) {
        return pixdata[MathUtils.boundsProtected(i, pixdata.length)];
    }
    @Override
    public void setPixel(int i, int val) {
        pixdata[MathUtils.boundsProtected(i, pixdata.length)] = val;
    }
    @NotNull
    @Override
    public LinearizedPixelContainer getPostProcessed(@NotNull PostProcessMode mode, double[][] biases, int byParts, boolean gammaCorrection) {
        return new LinearizedPixelContainer(toPixelContainer().getPostProcessed(mode, biases, byParts, gammaCorrection));
    }
}