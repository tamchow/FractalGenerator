package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.imgutils.ImageData;
/**
 * Holds a part of a fractal image for threaded generation, along with the render coordinates
 */
public class PartImage {
    ImageData imageData;
    int startx, endx, starty, endy;
    public PartImage(ImageData imageData, int startx, int endx, int starty, int endy) {
        this.imageData = imageData; this.startx = startx; this.endx = endx; this.starty = starty; this.endy = endy;
    }
}