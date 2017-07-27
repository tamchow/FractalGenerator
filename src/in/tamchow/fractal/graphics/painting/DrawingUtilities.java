package in.tamchow.fractal.graphics.painting;
import in.tamchow.fractal.color.ColorData;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.InterpolationType;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Line Drawing Utilities for {@link in.tamchow.fractal.graphics.containers.PixelContainer}
 *
 * @see in.tamchow.fractal.graphics.containers.PixelContainer
 * @see ColorData#interpolated(int, int, double, int, InterpolationType, boolean)
 * @see Colors.BaseColors
 */
public final class DrawingUtilities {
    /**
     * Uses Xiaolin Wu's Anti-Aliased line drawing algorithm
     *
     * @param canvas the {@link PixelContainer} to draw this line on
     * @param from_x starting ordinate
     * @param from_y starting abscissa
     * @param to_x ending ordinate
     * @param to_y ending abscissa
     * @param color line pixel color
     */
    public static void drawLine(@NotNull PixelContainer canvas, double from_x, double from_y,
                                double to_x, double to_y, int color) {
        boolean steep = Math.abs(to_y - from_y) > Math.abs(to_x - from_x);
        if (steep) {
            double t = from_x;
            from_x = from_y;
            from_y = t;
            t = to_x;
            to_x = to_y;
            to_y = t;
        }
        if (from_x > to_x) {
            double t = from_x;
            from_x = to_x;
            to_x = t;
            t = from_y;
            from_y = to_y;
            to_y = t;
        }
        double dx = to_x - from_x;
        double dy = to_y - from_y;
        double gradient = dy / dx;
        // handle first endpoint
        double xend = from_x;
        double yend = from_y + gradient * (xend - from_x);
        double xgap = rfpart(from_x + 0.5);
        double xpxl1 = xend; // this will be used in the main loop
        double ypxl1 = (int) (yend);
        if (steep) {
            plot(canvas, color, ypxl1, xpxl1, rfpart(yend) * xgap);
            plot(canvas, color, ypxl1 + 1, xpxl1, fpart(yend) * xgap);
        } else {
            plot(canvas, color, xpxl1, ypxl1, rfpart(yend) * xgap);
            plot(canvas, color, xpxl1, ypxl1 + 1, fpart(yend) * xgap);
        }
        double intery = yend + gradient; // first y-intersection for the main loop
        // handle second endpoint
        xend = Math.round(to_x);
        yend = to_y + gradient * (xend - to_x);
        xgap = fpart(to_x + 0.5);
        double xpxl2 = xend; //this will be used in the main loop
        double ypxl2 = (int) (yend);
        if (steep) {
            plot(canvas, color, ypxl2, xpxl2, rfpart(yend) * xgap);
            plot(canvas, color, ypxl2 + 1, xpxl2, fpart(yend) * xgap);
        } else {
            plot(canvas, color, xpxl2, ypxl2, rfpart(yend) * xgap);
            plot(canvas, color, xpxl2, ypxl2 + 1, fpart(yend) * xgap);
        }
        // main loop
        for (double x = xpxl1 + 1; x <= xpxl2 - 1; ++x) {
            if (steep) {
                plot(canvas, color, (int) (intery), x, rfpart(intery));
                plot(canvas, color, (int) (intery) + 1, x, fpart(intery));
            } else {
                plot(canvas, color, x, (int) (intery), rfpart(intery));
                plot(canvas, color, x, (int) (intery) + 1, fpart(intery));
            }
            intery = intery + gradient;
        }
    }
    private static void plot(@NotNull PixelContainer canvas, int color, double x, double y, double brightness) {
        canvas.setPixel(Math.round((float) y), Math.round((float) x),
                ColorData.interpolated(color, Colors.BaseColors.WHITE, brightness, 0, InterpolationType.LINEAR,
                        false));
    }
    private static double fpart(double x) {
        return x - Math.floor(x);
    }
    private static double rfpart(double x) {
        return 1.0 - fpart(x);
    }
    /**
     * Uses Bresenham's Line Drawing Algorithm
     *
     * @param canvas the {@link PixelContainer} to draw this line on
     * @param from_x starting ordinate
     * @param from_y starting abscissa
     * @param to_x ending ordinate
     * @param to_y ending abscissa
     * @param color line pixel color
     *
     * Deprecated.
     * Use the {@link DrawingUtilities#drawLine(PixelContainer, double, double, double, double, int)}
     */
    @Deprecated
    public static void drawLine(@NotNull PixelContainer canvas, int from_x, int from_y, int to_x, int to_y, int color) {
        int deltax = Math.abs(to_x - from_x), deltay = Math.abs(to_y - from_y),
                numpixels, d, dinc1, dinc2, x, xinc1, xinc2, y, yinc1, yinc2;
        if (deltax >= deltay) {
            numpixels = deltax + 1;
            d = (2 * deltay) - deltax;
            dinc1 = deltay << 1;
            dinc2 = (deltay - deltax) << 1;
            xinc1 = 1;
            xinc2 = 1;
            yinc1 = 0;
            yinc2 = 1;
        } else {
            numpixels = deltay + 1;
            d = (2 * deltax) - deltay;
            dinc1 = deltax << 1;
            dinc2 = (deltax - deltay) << 1;
            xinc1 = 0;
            xinc2 = 1;
            yinc1 = 1;
            yinc2 = 1;
        }
        if (from_x > to_x) {
            xinc1 = -xinc1;
            xinc2 = -xinc2;
        }
        if (from_y > to_y) {
            yinc1 = -yinc1;
            yinc2 = -yinc2;
        }
        x = from_x;
        y = from_y;
        for (int i = 1; i <= numpixels; ++i) {
            canvas.setPixel(y, x, color);
            if (d < 0) {
                d += dinc1;
                x += xinc1;
                y += yinc1;
            } else {
                d += dinc2;
                x += xinc2;
                y += yinc2;
            }
        }
    }
    /**
     * Color fill
     *
     * @param canvas the {@link PixelContainer} to fill color
     * @param color fill color
     */
    public static void fill(@NotNull PixelContainer canvas, int color) {
        for (int i = 0; i < canvas.getHeight(); i++) {
            for (int j = 0; j < canvas.getWidth(); j++) {
                canvas.setPixel(i, j, color);
            }
        }
    }
    /**
     * Rectangle outline of specific thickness
     *
     * @param canvas the {@link PixelContainer} to draw this rectangle outline on
     * @param startx starting ordinate
     * @param starty starting abscissa
     * @param endx ending ordinate
     * @param endy ending abscissa
     * @param thickness the thickness of the border
     * @param color line pixel color
     */
    public static void drawRect(@NotNull PixelContainer canvas, int startx, int starty,
                                int endx, int endy, int thickness, int color) {
        int oldcolor = canvas.getPixel((endy - starty) / 2, (endx - startx) / 2);
        fillRect(canvas, startx, starty, endx, endy, color);
        fillRect(canvas, startx + thickness, starty + thickness,
                endx - thickness, endy - thickness, oldcolor);
    }
    /**
     * Color-filled rectangle
     *
     * @param canvas the {@link PixelContainer} to draw this rectangle on
     * @param startx starting ordinate
     * @param starty starting abscissa
     * @param endx ending ordinate
     * @param endy ending abscissa
     * @param color line pixel color
     */
    public static void fillRect(@NotNull PixelContainer canvas, int startx, int starty, int endx, int endy, int color) {
        for (int i = starty; i < endy; i++) {
            for (int j = startx; j < endx; j++) {
                canvas.setPixel(i, j, color);
            }
        }
    }
}