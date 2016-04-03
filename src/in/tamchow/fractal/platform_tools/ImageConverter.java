package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;

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
    public static BufferedImage toImage(PixelContainer img) {
        return toImage(img, 0, 0, img.getWidth(), img.getHeight());
    }
    public static BufferedImage toImage(PixelContainer img, int startx, int starty, int endx, int endy) {
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
    public static PixelContainer toImageData(Image img) {
        return toImageData(img, 0, 0, img.getWidth(null), img.getHeight(null));
    }
    public static PixelContainer toImageData(Image img, int startx, int starty, int endx, int endy) {
        BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_ARGB);
        buf.getGraphics().drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), startx, starty, endx, endy, null);
        PixelContainer pixelContainer = new PixelContainer(buf.getWidth(), buf.getHeight());
        for (int i = 0; i < pixelContainer.getHeight(); i++) {
            for (int j = 0; j < pixelContainer.getWidth(); j++) {
                pixelContainer.setPixel(i, j, buf.getRGB(j, i));
            }
        }
        return pixelContainer;
    }
}