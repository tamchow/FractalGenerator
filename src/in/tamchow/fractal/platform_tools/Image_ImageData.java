package in.tamchow.fractal.platform_tools;

import in.tamchow.fractal.imgutils.ImageData;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Handles platform conversions
 */
public class Image_ImageData {
    public static ImageData toImageData(Image img) {
        BufferedImage buf = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        buf.getGraphics().drawImage(img, 0, 0, null);
        ImageData imageData = new ImageData(buf.getWidth(), buf.getHeight());
        for (int i = 0; i < imageData.getHeight(); i++) {
            for (int j = 0; j < imageData.getWidth(); j++) {
                imageData.setPixel(i, j, buf.getRGB(j, i));
            }
        }
        return imageData;
    }

    public static BufferedImage toImage(ImageData img) {
        BufferedImage buf = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                buf.setRGB(j, i, img.getPixel(i, j));
            }
        }
        return buf;
    }
}
