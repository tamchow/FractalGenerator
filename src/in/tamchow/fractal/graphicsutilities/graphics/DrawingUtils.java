package in.tamchow.fractal.graphicsutilities.graphics;
import in.tamchow.fractal.color.Color_Utils_Config;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
/**
 * Line Drawing Utilities for {@link in.tamchow.fractal.graphicsutilities.containers.PixelContainer}
 *
 * @see in.tamchow.fractal.graphicsutilities.containers.PixelContainer
 * @see Color_Utils_Config#linearInterpolated(int, int, double, int)
 * @see in.tamchow.fractal.color.Colors.BASE_COLORS
 */
public class DrawingUtils {
    public static void drawLine(PixelContainer canvas, double from_x, double from_y, double to_x, double to_y, int color) {
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
    private static void plot(PixelContainer canvas, int color, double x, double y, double brightness) {
        canvas.setPixel(Math.round((float) y), Math.round((float) x),
                Color_Utils_Config.linearInterpolated(color, Colors.BASE_COLORS.WHITE, brightness, 0));
    }
    private static double fpart(double x) {
        return x - Math.floor(x);
    }
    private static double rfpart(double x) {
        return 1.0 - fpart(x);
    }
}