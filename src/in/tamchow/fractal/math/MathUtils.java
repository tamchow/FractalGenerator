package in.tamchow.fractal.math;

import in.tamchow.fractal.math.complex.Complex;

import java.util.Random;

/**
 * Weighted Random Number generator and approximations
 */
public class MathUtils {

    public static boolean approxEquals(Complex a, Complex b, double tolerance) {
        return Math.abs(a.real() - b.real()) <= tolerance && Math.abs(a.imaginary() - b.imaginary()) <= tolerance;
    }

    public static double weightedRandom(double[] values, double[] weights) {
        int factor = 0, pidx = 0;
        double sum = 0.0;
        for (int i = 0; i < weights.length; i++) {
            int afterpoint = (weights[i] + "".substring((weights[i] + "").indexOf('.') + 1)).length();
            factor = ((afterpoint > factor) ? afterpoint : factor);
            sum += weights[i];
        }
        if (values.length != weights.length || sum != 1.0) {
            throw new IllegalArgumentException("Illegal Parameters");
        }
        factor = (int) Math.pow(10, factor);
        double[] rand = new double[factor];
        for (int i = 0; i < rand.length; ) {
            if (i == weights[pidx] * factor) {
                pidx++;
                continue;
            }
            rand[i] = values[pidx];
            i++;
        }
        return rand[new Random().nextInt(rand.length)];
    }

    public static int weightedRandom(double[] weights) {
        int factor = 0, pidx = 0;
        double sum = 0.0;
        for (int i = 0; i < weights.length; i++) {
            int afterpoint = (weights[i] + "".substring((weights[i] + "").indexOf('.') + 1)).length();
            factor = ((afterpoint > factor) ? afterpoint : factor);
            sum += weights[i];
        }
        if (sum != 1.0) {
            throw new IllegalArgumentException("Illegal Parameters");
        }
        factor = (int) Math.pow(10, factor);
        int[] rand = new int[factor];
        for (int i = 0; i < rand.length; ) {
            if (i == weights[pidx] * factor) {
                pidx++;
                continue;
            }
            rand[i] = pidx;
            i++;
        }
        return rand[new Random().nextInt(rand.length)];
    }
}
