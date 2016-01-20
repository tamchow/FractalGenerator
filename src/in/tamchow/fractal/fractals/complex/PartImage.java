package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.imgutils.ImageData;
/**
 * Holds a part of a fractal image for threaded generation, along with the render coordinates
 */
public class PartImage {
    ImageData imageData;
    int[][] escapedata;
    double[][] normalized_escapes;
    int[] histogram;
    int startx, endx, starty, endy;
    public PartImage(ImageData imageData, int startx, int endx, int starty, int endy) {
        this.imageData = new ImageData(imageData); this.startx = startx; this.endx = endx; this.starty = starty;
        this.endy = endy;
    }
    public PartImage(int[][] escapedata, double[][] normalized_escapes, int[] histogram, int startx, int endx, int starty, int endy) {
        this.startx = startx; this.endx = endx; this.starty = starty; this.endy = endy;
        this.escapedata = new int[escapedata.length][escapedata[0].length];
        for (int i = 0; i < escapedata.length; i++) {
            System.arraycopy(escapedata[i], 0, this.escapedata[i], 0, this.escapedata[i].length);
        } this.normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        for (int i = 0; i < normalized_escapes.length; i++) {
            System.arraycopy(normalized_escapes[i], 0, this.normalized_escapes[i], 0, this.normalized_escapes[i].length);
        } this.histogram = new int[histogram.length];
        System.arraycopy(histogram, 0, this.histogram, 0, this.histogram.length);
    }
}