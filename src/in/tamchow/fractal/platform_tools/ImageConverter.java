package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.imgutils.containers.Animation;
import in.tamchow.fractal.imgutils.containers.ImageData;

import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * Handles platform conversions of images
 */
public class ImageConverter {
    public static BufferedImage[] animationFrames(Animation animation) {
        BufferedImage[] frames = new BufferedImage[animation.getNumFrames()];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = toImage(animation.getFrame(i));
        }
        return frames;
    }
    public static BufferedImage toImage(ImageData img) {
        return toImage(img, 0, 0, img.getWidth(), img.getHeight());
    }
    public static BufferedImage toImage(ImageData img, int startx, int starty, int endx, int endy) {
        BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_ARGB);
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
    public static Animation framesAsAnimation(BufferedImage[] frames, int fps) {
        Animation animation = new Animation(fps);
        for (BufferedImage frame : frames) {
            animation.addFrame(toImageData(frame));
        }
        return animation;
    }
    public static ImageData toImageData(Image img) {
        return toImageData(img, 0, 0, img.getWidth(null), img.getHeight(null));
    }
    public static ImageData toImageData(Image img, int startx, int starty, int endx, int endy) {
        BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_ARGB);
        buf.getGraphics().drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), startx, starty, endx, endy, null);
        ImageData imageData = new ImageData(buf.getWidth(), buf.getHeight());
        for (int i = 0; i < imageData.getHeight(); i++) {
            for (int j = 0; j < imageData.getWidth(); j++) {
                imageData.setPixel(i, j, buf.getRGB(j, i));
            }
        }
        return imageData;
    }
}