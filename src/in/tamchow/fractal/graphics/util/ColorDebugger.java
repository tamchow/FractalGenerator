package in.tamchow.fractal.graphics.util;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.math.MathUtils;
/**
 * Creates a {@link in.tamchow.fractal.graphics.containers.PixelContainer}
 * having all the colors represented by an array of packed ints.
 */
public class ColorDebugger {
    public static PixelContainer createDebugImage(int[] colors) {
        int width = Math.round((float) Math.sqrt(colors.length)), height = colors.length / width;
        int[][] pixels = new int[height][width];
        for (int i = 0; i < pixels.length; ++i) {
            for (int j = 0; j < pixels[i].length; ++j) {
                pixels[i][j] = colors[MathUtils.normalized(i, j, width, height)];
            }
        }
        return new PixelContainer(pixels);
    }
}
