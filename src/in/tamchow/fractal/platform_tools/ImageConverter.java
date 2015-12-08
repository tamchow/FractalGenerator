package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.imgutils.ImageData;

import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * Handles platform conversions
 */
public class ImageConverter {
    public static ImageData toImageData(Image img) {
        return toImageData(img, 0, 0, img.getWidth(null), img.getHeight(null));
    }
    public static ImageData toImageData(Image img, int startx, int starty, int endx, int endy) {
        BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_RGB);
        buf.getGraphics().drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), startx, starty, endx, endy, null);
        ImageData imageData = new ImageData(buf.getWidth(), buf.getHeight());
        for (int i = 0; i < imageData.getHeight(); i++) {
            for (int j = 0; j < imageData.getWidth(); j++) {
                imageData.setPixel(i, j, buf.getRGB(j, i));
            }
        }
        return imageData;
    }
    public static BufferedImage toImage(ImageData img) {
        return toImage(img, 0, 0, img.getWidth(), img.getHeight());
    }
    public static BufferedImage toImage(ImageData img, int startx, int starty, int endx, int endy) {
        BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < buf.getHeight(); i++) {
            for (int j = 0; j < buf.getWidth(); j++) {
                if ((j + startx) > endx || (i + starty) > endy) {
                    break;
                }
                buf.setRGB(j, i, img.getPixel(starty + i, startx + j));
            }
        }
        return buf;
    }
}
