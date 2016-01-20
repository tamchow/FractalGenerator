package in.tamchow.fractal.helpers;
import in.tamchow.fractal.math.complex.Complex;

import java.util.Random;
/**
 * Weighted Random Number generator and approximations,prime number calculator
 */
public class MathUtils {
    public static int boundsProtected(int ptr, int size) {
        return (ptr < 0) ? Math.abs(size + ptr) % size : ((ptr >= size) ? (ptr % size) : ptr);
    }
    public static String numberLineRepresentation(float number, int precision) {
        float f = number; int p = precision;
        int g = (int) f, d = Math.round((f - g) * p), a = ("" + g + 1).length(), b = ("" + g).length(), i = 0;
        String h = "", q = "" + g; int c = q.length(); for (; i < b; i++) h += " "; for (++i; i <= b + p; i++) h += "-";
        for (i = c; i < c + d; i++) q += "|"; for (; i < p + b; i++) q += " "; return q + (g + 1) + "\n" + h;
    }
    public static int[] diamondPuzzleSolver(int sum, int product, int low, int high) {
        for (int a = low; a <= high; a++) {
            for (int b = low; b <= high; b++) if (a + b == sum && a * b == product) return new int[]{a, b};
        } return null;
    }
    public static int[] diamondPuzzleSolverQuadratic(int sum, int product) {
        int x = sum + (int) Math.sqrt(sum * sum - 4 * product); return new int[]{x / 2, sum - x / 2};
    }
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
    public int[] mostEfficientfactor(int a) {
        int num_factors = 0; for (int i = 1; i <= a; i++) {if (a % i == 0) num_factors++;}
        int[] factors = new int[num_factors]; num_factors = 0;
        for (int i = 1; i <= a && num_factors < factors.length; i++) {
            if (a % i == 0) {factors[num_factors] = i; num_factors++;}
        } FactorData[] data = new FactorData[num_factors]; for (int i = 0; i < num_factors; i++) {
            for (int j = i + 1; j < num_factors; j++) {data[i] = new FactorData(factors[i], factors[j]);}
        } quickSort(data, 0, data.length - 1); return new int[]{data[0].a, data[0].b};
    }
    void quickSort(FactorData[] arr, int low, int high) {
        if (arr == null || arr.length == 0) return; if (low >= high) return;
        // pick the pivot
        int middle = low + (high - low) / 2; int pivot = arr[middle].sum;
        // make left < pivot and right > pivot
        int i = low, j = high; while (i <= j) {
            while (arr[i].sum < pivot) {i++;} while (arr[j].sum > pivot) {j--;} if (i <= j) {
                FactorData temp = new FactorData(arr[i]); arr[i] = new FactorData(arr[j]);
                arr[j] = new FactorData(temp); i++; j--;
            }
        }
        // recursively sort two sub parts
        if (low < j) quickSort(arr, low, j); if (high > i) quickSort(arr, i, high);
    }
    private class FactorData {
        int a, b, sum;
        public FactorData(int a, int b) {this.a = a; this.b = b; this.sum = a + b;}
        public FactorData(FactorData old) {a = old.a; b = old.b; sum = old.sum;}
    }
}
