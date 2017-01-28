package in.tamchow.fractal.color;
import in.tamchow.fractal.helpers.math.MathUtils;
/**
 * A few constant values for coloring modes
 */
public final class Colors {
    private static final double a = 0.055, opa = 1 + a, rgbLimit = 0.04045, linearLimit = 0.0031308, factor = 12.92, powFactor = 2.4, iPowFactor = 1 / powFactor;
    private Colors() {
    }
    public static int rgbToLinear(int in, boolean correct) {
        if (correct) {
            in = MathUtils.boundsProtected(in, 256);
            double s = in / 255.0;
            if (s <= rgbLimit) {
                return (int) ((s / factor) * 255);
            } else {
                return (int) (Math.pow(((s + a) / opa), powFactor) * 255);
            }
        }
        return in;
    }
    public static int linearToRgb(int in, boolean correct) {
        if (correct) {
            in = MathUtils.boundsProtected(in, 256);
            double s = in / 255.0;
            if (s <= linearLimit) {
                return (int) ((s * factor) * 255);
            } else {
                return (int) ((Math.pow(opa * s, iPowFactor) - a) * 255);
            }
        }
        return in;
    }
    public enum MODE {
        SIMPLE, SIMPLE_SMOOTH,
        DIVIDE, DIVIDE_NORMALIZED, MULTIPLY, MULTIPLY_NORMALIZED,
        GRAYSCALE_HIGH_CONTRAST, GRAYSCALE_LOW_CONTRAST,
        NEWTON_CLASSIC, NEWTON_NORMALIZED_MODULUS,
        CURVATURE_AVERAGE_NOABS, CURVATURE_AVERAGE_ABS,
        STRIPE_AVERAGE,
        TRIANGLE_AREA_INEQUALITY,
        CUMULATIVE_DISTANCE, CUMULATIVE_ANGLE,
        HISTOGRAM,
        SIMPLE_DISTANCE_ESTIMATION, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE, DISTANCE_ESTIMATION_2C_OR_BW,
        ORBIT_TRAP_MIN, ORBIT_TRAP_MAX, ORBIT_TRAP_AVG,
        LINE_TRAP_MIN, LINE_TRAP_MAX, LINE_TRAP_AVG,
        GAUSSIAN_INT_DISTANCE,
        EPSILON_CROSS,
        DOMAIN_COLORING, DOMAIN_COLORING_FAUX,
        RANK_ORDER,
        ASCII_ART_CHARACTER, ASCII_ART_NUMERIC
    }
    public enum PALETTE {RANDOM_PALETTE, CUSTOM_PALETTE, GRADIENT_PALETTE, SMOOTH_PALETTE, SHADE_PALETTE}
    public static final class RGBCOMPONENTS {//not an enum because the constants are used for calculating stuff
        public static final int ALPHA = 3, RED = 2, GREEN = 1, BLUE = 0;
        private RGBCOMPONENTS() {
        }
    }
    public static final class BASE_COLORS {
        public static final int BLACK = 0xff000000, WHITE = 0xffffffff, RED = 0xffff0000, GREEN = 0xff00ff00, BLUE = 0xff0000ff, CYAN = 0xff00ffff, MAGENTA = 0xffff00ff, YELLOW = 0xffffff00;//opaque
        private BASE_COLORS() {
        }
    }
}