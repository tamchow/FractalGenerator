package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
/**
 * Holds a part of a fractal's data for threaded generation, along with the render coordinates
 */
public final class PartComplexFractalData implements Serializable {
    PixelContainer pixelContainer;
    int[][] escapedata;
    double[][] normalized_escapes;
    int[] histogram;
    int startx, endx, starty, endy;
    public PartComplexFractalData(@NotNull int[][] escapedata, @NotNull double[][] normalized_escapes, PixelContainer pixelContainer, int startx, int endx, int starty, int endy) {
        this.startx = startx;
        this.endx = endx;
        this.starty = starty;
        this.endy = endy;
        initData(escapedata, normalized_escapes);
        this.pixelContainer = new PixelContainer(pixelContainer);
    }
    public PartComplexFractalData(@NotNull int[][] escapedata, @NotNull double[][] normalized_escapes, @NotNull int[] histogram, int startx, int endx, int starty, int endy) {
        this.startx = startx;
        this.endx = endx;
        this.starty = starty;
        this.endy = endy;
        initData(escapedata, normalized_escapes);
        this.histogram = new int[histogram.length];
        System.arraycopy(histogram, 0, this.histogram, 0, this.histogram.length);
    }
    private void initData(@NotNull int[][] escapedata, @NotNull double[][] normalized_escapes) {
        this.escapedata = new int[escapedata.length][escapedata[0].length];
        for (int i = 0; i < escapedata.length; i++) {
            System.arraycopy(escapedata[i], 0, this.escapedata[i], 0, this.escapedata[i].length);
        }
        this.normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        for (int i = 0; i < normalized_escapes.length; i++) {
            System.arraycopy(normalized_escapes[i], 0, this.normalized_escapes[i], 0, this.normalized_escapes[i].length);
        }
    }
}