package in.tamchow.fractal.misc.mathstuff;
import static java.lang.Math.*;
public class DoughNut {
    private static final double MAX_ANGLE = 2 * PI, SQRT_2 = sqrt(2);
    private static final int EMPTY_CELL = -1;
    private int screen_width = 30, screen_height = 30, luminance_multiplier = 256, frames = 1024, sleep = 100;
    private double theta_spacing = 0.07, phi_spacing = 0.02, R1 = 1, R2 = 2, K2 = 5, scale = 0.375,
            A_spacing = 0.07, B_spacing = 0.03, K1 = 0;
    private char[] ASCII_luminance_indices = ".,-~:;=!*#$@".toCharArray();
    public DoughNut() {
        initK1();
    }
    public DoughNut(int screen_width, int screen_height, int frames, int sleep) {
        this.screen_width = screen_width;
        this.screen_height = screen_height;
        this.frames = frames;
        this.sleep = sleep;
        initK1();
    }
    public DoughNut(int screen_width, int screen_height, int frames, int sleep, int luminance_multiplier) {
        this.screen_width = screen_width;
        this.screen_height = screen_height;
        this.luminance_multiplier = luminance_multiplier;
        this.frames = frames;
        this.sleep = sleep;
        initK1();
    }
    public DoughNut(int screen_width, int screen_height, int frames, int sleep, int luminance_multiplier,
                    double theta_spacing, double phi_spacing, double R1, double R2, double K2, double scale,
                    double A_spacing, double B_spacing, String ascii) {
        this.screen_width = screen_width;
        this.screen_height = screen_height;
        this.luminance_multiplier = luminance_multiplier;
        this.theta_spacing = theta_spacing;
        this.phi_spacing = phi_spacing;
        this.R1 = R1;
        this.R2 = R2;
        this.K2 = K2;
        this.scale = scale;
        this.A_spacing = A_spacing;
        this.B_spacing = B_spacing;
        this.frames = frames;
        this.sleep = sleep;
        this.ASCII_luminance_indices = ascii.toCharArray();
        initK1();
    }
    public DoughNut(String info) {
        String[] parts = info.split("\\s+");
        this.screen_width = Integer.valueOf(parts[0]);
        this.screen_height = Integer.valueOf(parts[1]);
        this.luminance_multiplier = Integer.valueOf(parts[2]);
        this.theta_spacing = Double.valueOf(parts[3]);
        this.phi_spacing = Double.valueOf(parts[4]);
        this.R1 = Double.valueOf(parts[5]);
        this.R2 = Double.valueOf(parts[6]);
        this.K2 = Double.valueOf(parts[7]);
        this.scale = Double.valueOf(parts[8]);
        this.A_spacing = Double.valueOf(parts[9]);
        this.B_spacing = Double.valueOf(parts[10]);
        this.frames = Integer.valueOf(parts[11]);
        this.sleep = Integer.valueOf(parts[12]);
        this.ASCII_luminance_indices = parts[13].toCharArray();
        initK1();
    }
    public static void main(String[] args) {
        DoughNut dn = new DoughNut();
        dn.show_ascii_animation(dn.render_ascii_animation(dn.render_animation()));
    }
    private void initK1() {
        K1 = scale * ((screen_width * K2) / (R1 + R2));
    }
    public int[][] render_frame(double A, double B) {
        // precomputed sines and cosines of A and B
        double cosA = cos(A), sinA = sin(A), cosB = cos(B), sinB = sin(B);
        int[][] output = new int[screen_width][screen_height];
        for (int i = 0; i < output.length; ++i) {
            for (int j = 0; j < output[i].length; ++j) {
                output[i][j] = EMPTY_CELL;
            }
        }
        double zbuffer[][] = new double[screen_width][screen_height];
        // theta goes around the cross-sectional circle of a torus
        for (double theta = 0; theta < MAX_ANGLE; theta += theta_spacing) {
            // precomputed sines and cosines of theta
            double costheta = cos(theta), sintheta = sin(theta);
            // phi goes around the center of revolution of a torus
            for (double phi = 0; phi < MAX_ANGLE; phi += phi_spacing) {
                // precomputed sines and cosines of phi
                double cosphi = cos(phi), sinphi = sin(phi);
                // the x,y coordinate of the circle, before revolving
                double circlex = R2 + R1 * costheta;
                double circley = R1 * sintheta;
                // final 3D (x,y,z) coordinate after rotations
                double x = circlex * (cosB * cosphi + sinA * sinB * sinphi)
                        - circley * cosA * sinB;
                double y = circlex * (sinB * cosphi - sinA * cosB * sinphi)
                        + circley * cosA * cosB;
                double z = K2 + cosA * circlex * sinphi + circley * sinA;
                double ooz = 1 / z;  // "one over z"
                // x and y projection.  note that y is negated here, because y
                // goes up in 3D space but down on 2D displays.
                int xp = (int) (screen_width / 2 + K1 * ooz * x);
                int yp = (int) (screen_height / 2 - K1 * ooz * y);
                xp = min(clamped(xp, 0, zbuffer.length - 1), clamped(xp, 0, output.length - 1));
                yp = min(clamped(yp, 0, zbuffer[0].length - 1), clamped(yp, 0, output[0].length - 1));
                // calculate luminance.  ugly, but correct.
                double L = cosphi * costheta * sinB - cosA * costheta * sinphi -
                        sinA * sintheta + cosB * (cosA * sintheta - costheta * sinA * sinphi);
                // L ranges from -SQRT_2 to +SQRT_2. If it's < 0, the surface
                // is pointing away from us, so we won't bother trying to plot it.
                if (L >= 0) {
                    // Test against the z-buffer. Larger 1/z means the pixel is
                    // closer to the viewer than what's already plotted.
                    if (ooz > zbuffer[xp][yp]) {
                        zbuffer[xp][yp] = ooz;
                        output[xp][yp] = bounded((int) ((L / SQRT_2) * luminance_multiplier), luminance_multiplier);
                    }
                }
            }
        }
        return output;
    }
    private int clamped(int val, int min, int max) {
        if (min > max) {
            int t = max;
            max = min;
            min = t;
        }
        return (val < min) ? min : ((val > max) ? max : val);
    }
    private int bounded(int val, int max) {
        return val % max;
    }
    public int[][][] render_animation() {
        int[][][] animation = new int[frames][screen_width][screen_height];
        int frameIdx = 0;
        for (double A = 0; frameIdx < frames; A += A_spacing) {
            for (double B = 0; frameIdx < frames; B += B_spacing) {
                animation[frameIdx] = render_frame(A, B);
                ++frameIdx;
            }
        }
        return animation;
    }
    public char[][] render_ascii_frame(int[][] output) {
        char[][] ascii = new char[output[0].length][output.length];
        for (int j = 0; j < ascii.length; ++j) {
            for (int i = 0; i < ascii[j].length; ++i) {
                int idx = bounded((int) (((double) output[i][j] / luminance_multiplier) * ASCII_luminance_indices.length), ASCII_luminance_indices.length);
                ascii[j][i] = (output[i][j] < 0 || idx < 0) ? ' ' : ASCII_luminance_indices[idx];
            }
        }
        return ascii;
    }
    public char[][][] render_ascii_animation(int[][][] animation) {
        char[][][] ascii = new char[animation.length][animation[0][0].length][animation[0].length];
        for (int i = 0; i < ascii.length; ++i) {
            ascii[i] = render_ascii_frame(animation[i]);
        }
        return ascii;
    }
    public void show_ascii_animation(char[][][] animation) {
        for (int i = 0; i < animation.length; ++i) {
            System.out.print('\f');
            //System.out.print("\\x1b[H");
            show_ascii_frame(animation[i]);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
        }
    }
    public void show_ascii_frame(char[][] animation) {
        for (int j = 0; j < animation.length; ++j) {
            for (int i = 0; i < animation[j].length; ++i) {
                System.out.print(animation[j][i]);
            }
            System.out.println();
        }
    }
    @Override
    public String toString() {
        return screen_width + " " + screen_height + " " + frames + " " + sleep + " " + luminance_multiplier + " " + theta_spacing + " " + phi_spacing + " " + R1 + " " + R2
                + " " + K2 + " " + scale + " " + A_spacing + " " + B_spacing + " " + new String(ASCII_luminance_indices);
    }
}