package in.tamchow.fractal.color;
/**
 * A few constant values for coloring modes
 */
public final class Colors {
    private Colors() {
    }
    public enum MODE {
        SIMPLE, SIMPLE_SMOOTH_LINEAR, SIMPLE_SMOOTH_SPLINE,
        DIVIDE, MULTIPLY, DIVIDE_NORMALIZED, MULTIPLY_NORMALIZED,
        GRAYSCALE_HIGH_CONTRAST, GRAYSCALE_LOW_CONTRAST,
        NEWTON_CLASSIC, NEWTON_NORMALIZED_1, NEWTON_NORMALIZED_2,
        CURVATURE_AVERAGE_NOABS_SPLINE, CURVATURE_AVERAGE_ABS_SPLINE,
        CURVATURE_AVERAGE_NOABS_LINEAR, CURVATURE_AVERAGE_ABS_LINEAR,
        STRIPE_AVERAGE_SPLINE, STRIPE_AVERAGE_LINEAR,
        TRIANGLE_AREA_INEQUALITY_SPLINE, TRIANGLE_AREA_INEQUALITY_LINEAR,
        HISTOGRAM_SPLINE, HISTOGRAM_LINEAR,
        SIMPLE_DISTANCE_ESTIMATION, DISTANCE_ESTIMATION_COLOR, DISTANCE_ESTIMATION_GRAYSCALE,
        ORBIT_TRAP_MIN, ORBIT_TRAP_MAX, ORBIT_TRAP_AVG,
        LINE_TRAP_MIN, LINE_TRAP_MAX, LINE_TRAP_AVG,
        GAUSSIAN_INT_DISTANCE_LINEAR, GAUSSIAN_INT_DISTANCE_SPLINE,
        EPSILON_CROSS_SPLINE, EPSILON_CROSS_LINEAR,
        DOMAIN_COLORING, DOMAIN_COLORING_FAUX,
        RANK_ORDER_LINEAR, RANK_ORDER_SPLINE,
        ASCII_ART
    }
    public enum PALETTE {RANDOM_PALETTE, CUSTOM_PALETTE, GRADIENT_PALETTE, SMOOTH_PALETTE_LINEAR, SMOOTH_PALETTE_SPLINE, SHADE_PALETTE}
    public static final class RGBCOMPONENTS {//not an enum because the constants are used for calculating stuff
        public static final int ALPHA = 3, RED = 2, GREEN = 1, BLUE = 0;
        private RGBCOMPONENTS() {
        }
    }
    public static final class BASE_COLORS {
        public static final int BLACK = 0xff000000, WHITE = 0xffffffff, RED = 0xffff0000, GREEN = 0xff00ff00, BLUE = 0xff0000ff, CYAN = 0xff000ff0, MAGENTA = 0xfff0000f, YELLOW = 0xff0ff000;//opaque
        private BASE_COLORS() {
        }
    }
}