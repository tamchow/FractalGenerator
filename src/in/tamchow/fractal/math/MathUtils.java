package in.tamchow.fractal.math;
import in.tamchow.fractal.math.complex.Complex;

import java.util.Random;
/**
 * Weighted Random Number generator and approximations,prime number calculator
 */
public class MathUtils {
    public static boolean approxEquals(Complex a, Complex b, double tolerance) {
        return Math.abs(a.real() - b.real()) <= tolerance && Math.abs(a.imaginary() - b.imaginary()) <= tolerance;
    }
    public static int weightedRandom(double[] weights) {
        return (int) weightedRandom(null, weights);
    }
    public static double weightedRandom(double[] values, double[] weights) {
        int factor = 0, pidx = 0; double sum = 0.0; boolean custom = values != null;
        for (double weight : weights) {
            int afterpoint = (weight + "".substring((weight + "").indexOf('.') + 1)).length();
            factor = ((afterpoint > factor) ? afterpoint : factor);
            sum += weight;
        } if ((values.length != weights.length && !custom) || sum != 1.0) {
            throw new IllegalArgumentException("Illegal Parameters");
        }
        factor = (int) Math.pow(10, factor);
        double[] rand = new double[factor];
        for (int i = 0; i < rand.length; ) {
            if (i == weights[pidx] * factor) {
                pidx++; continue;
            } rand[i] = (custom) ? values[pidx] : pidx; i++;
        }
        return rand[new Random().nextInt(rand.length)];
    }
    public static int[] translateCoordinates(int x, int y, int ix, int iy, int fx, int fy) {
        return new int[]{(int) (((double) x / ix) * fx), (int) (((double) y / iy) * fy)};
    }
    public static int firstPrimeFrom(int from) {
        for (int i = from; i > 0; i++) {
            int factors = 0; for (int j = 2; j < i; j++) {if (i % j == 0) {++factors;}} if (factors == 0) {return i;}
        } return -1;
    }
}
