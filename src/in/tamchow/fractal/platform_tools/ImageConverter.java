package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.graphics.containers.Animation;
import in.tamchow.fractal.graphics.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.awt.*;
import java.awt.image.BufferedImage;
/**
 * Handles platform conversions of images
 */
public class ImageConverter {
    private static final int AUTO_DIMENSIONS = Integer.MAX_VALUE >>> 7;//2^24
    @NotNull
    public static BufferedImage[] animationFrames(@NotNull Animation animation) {
        @NotNull BufferedImage[] frames = new BufferedImage[animation.getNumFrames()];
        for (int i = 0; i < frames.length; ++i) {
            frames[i] = toImage(animation.getFrame(i));
        }
        return frames;
    }
    @NotNull
    public static BufferedImage toImage(@NotNull PixelContainer img) {
        return toImage(img, 0, 0, img.getWidth(), img.getHeight());
    }
    @NotNull
    public static BufferedImage toImage(@NotNull PixelContainer img, int startx, int starty, int endx, int endy) {
        @NotNull BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_ARGB);
        buf.setRGB(startx, starty, buf.getWidth(), buf.getHeight(), img.getPixels(), 0, buf.getWidth());
        return buf;
    }
    @NotNull
    public static Animation framesAsAnimation(@NotNull BufferedImage[] frames, int fps) {
        @NotNull Animation animation = new Animation(fps);
        for (@NotNull BufferedImage frame : frames) {
            animation.addFrame(toPixelContainer(frame));
        }
        return animation;
    }
    @NotNull
    public static PixelContainer toPixelContainer(@NotNull Image img) {
        return toPixelContainer(img, 0, 0, img.getWidth(null), img.getHeight(null));
    }
    @NotNull
    public static PixelContainer toPixelContainer(Image img, int startx, int starty, int endx, int endy) {
        @NotNull BufferedImage buf = new BufferedImage(endx - startx, endy - starty, BufferedImage.TYPE_INT_ARGB);
        buf.getGraphics().drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), startx, starty, endx, endy, null);
        @NotNull PixelContainer pixelContainer = new LinearizedPixelContainer(buf.getWidth(), buf.getHeight());
        pixelContainer.setPixdata(buf.getRGB(0, 0, buf.getWidth(), buf.getHeight(), null, 0, buf.getWidth()), buf.getWidth());
        return pixelContainer;
    }
    @NotNull
    public static PixelContainer drawTextToPixelContainer(String text, String fontName, int fontStyle, int penColor, int backColor, int fontSize, int x, int y) {
        return drawTextToPixelContainer(text, fontName, fontStyle, penColor, backColor, fontSize, x, y, AUTO_DIMENSIONS, AUTO_DIMENSIONS, true);
    }
    @NotNull
    public static PixelContainer drawTextToPixelContainer(String text, String fontName, int fontStyle, int penColor, int backColor, int fontSize, int x, int y, int width, int height, boolean autoDimensions) {
        return toPixelContainer(drawTextToImage(text, fontName, fontStyle, penColor, backColor, fontSize, x, y, width, height, autoDimensions));
    }
    @NotNull
    public static BufferedImage drawTextToImage(String text, String fontName, int fontStyle, int penColor, int backColor, int fontSize, int x, int y) {
        return drawTextToImage(text, fontName, fontStyle, penColor, backColor, fontSize, x, y, AUTO_DIMENSIONS, AUTO_DIMENSIONS, true);
    }
    @NotNull
    public static BufferedImage drawTextToImage(String text, String fontName, int fontStyle, int penColor, int backColor, int fontSize, int x, int y, int width, int height, boolean autoDimensions) {
        @NotNull BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        @NotNull Graphics2D graphics = buf.createGraphics();
        graphics.setBackground(new Color(backColor));
        graphics.setColor(new Color(penColor));
        //painting.setStroke(new BasicStroke(fontSize));
        graphics.setFont(new Font(fontName, fontStyle, fontSize));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        @NotNull String[] lines = StringManipulator.split(text, "\n");
        int spacing = fontMetrics.getHeight();
        String longestLine = "";
        for (String line : lines) {
            longestLine = line.length() > longestLine.length() ? line : longestLine;
        }
        if (autoDimensions) {
            buf = new BufferedImage(fontMetrics.stringWidth(longestLine),
                    fontMetrics.getHeight() * lines.length,
                    BufferedImage.TYPE_INT_ARGB);
            graphics = buf.createGraphics();
            graphics.setBackground(new Color(backColor));
            graphics.setColor(new Color(penColor));
            //painting.setStroke(new BasicStroke(fontSize));
            graphics.setFont(new Font(fontName, fontStyle, fontSize));
        }
        //painting.fillRect(0, 0, buf.getWidth(), buf.getHeight());
        graphics.clearRect(0, 0, buf.getWidth(), buf.getHeight());
        drawString(graphics, lines, x, y, spacing);
        graphics.dispose();
        return buf;
    }
    private static void drawString(Graphics g, String[] text, int x, int y, int spacing) {
        for (String line : text) {
            g.drawString(line, x, y += spacing);
        }
    }
}