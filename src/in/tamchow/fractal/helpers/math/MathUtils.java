package in.tamchow.fractal.helpers.math;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.matrix.Matrix;

import static in.tamchow.fractal.math.matrix.MatrixOperations.*;
/**
 * Weighted Random Number generator and approximations,prime number calculator
 */
public final class MathUtils {
    public static final double ULP = 10E-15;
    private MathUtils() {
    }
    public static String MDAtoString(Object[][] items) {
        String representation = "[";
        for (Object[] subitems : items) {
            for (Object item : subitems) {
                representation += String.valueOf(item) + ",";
            }
            representation = representation.substring(0, representation.length() - 1);//trims trailing ','
            representation += "],\n[";
        }
        return representation.trim().substring(0, representation.length() - 3);//trims trailing stuff
    }
    public static String intMDAtoString(int[][] items) {
        String representation = "[";
        for (int[] subitems : items) {
            for (int item : subitems) {
                representation += String.valueOf(item) + ",";
            }
            representation = representation.substring(0, representation.length() - 1);//trims trailing ','
            representation += "],\n[";
        }
        return representation.trim().substring(0, representation.length() - 3);//trims trailing stuff
    }
    public static int boundsProtected(int ptr, int size) {
        return (ptr < 0) ? Math.abs(size + ptr) % size : ((ptr >= size) ? (ptr % size) : ptr);
    }
    /**
     * @param array the array to splice
     * @param from the index to splice from (inclusive)
     * @param to the index to splice to (inclusive)
     * @param <T> the type parameter of array
     * @return the spliced array in the given range
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] splice(@NotNull T[] array, int from, int to) {
        from = boundsProtected(from, array.length);
        to = boundsProtected(to, array.length);
        T[] spliced;
        if (from > to) {
            spliced = (T[]) new Object[array.length - from + to];
            int j = 0;
            for (int i = from; i < array.length && j < spliced.length; ++i, ++j) {
                spliced[j] = array[i];
            }
            for (int i = 0; i <= to; ++i) {
                spliced[j] = array[i];
            }
        } else {
            spliced = (T[]) new Object[to - from];
            for (int i = from, j = 0; i <= to && j < spliced.length; ++i, ++j) {
                spliced[j] = array[i];
            }
        }
        return spliced;
    }
    /**
     * @param string the {@link String} to splice
     * @param from the index to splice from (inclusive)
     * @param to the index to splice to (inclusive)
     * @return the spliced {@link String} in the given range
     */
    @NotNull
    public static String splice(@NotNull String string, int from, int to) {
        from = boundsProtected(from, string.length());
        to = boundsProtected(to, string.length());
        if (from > to) {
            return string.substring(from, string.length()) + string.substring(0, to);
        } else {
            return string.substring(from, to);
        }
    }
    public static int clamp(int ptr, int size) {
        return clamp(ptr, 0, size - 1);//for array indices
    }
    public static int clamp(int ptr, int min, int max) {
        if (max < min) {
            return (ptr < min) ? min : ptr;
        }
        if (max == min) {
            return min;///*or,*/ return max;
        }
        return (ptr < min) ? min : ((ptr > max) ? max : ptr);
    }
    @NotNull
    public static int[] imageBounds(int y, int x, int width, int height) {
        y += boundsProtected(x / width, height);
        x = boundsProtected(x, width);
        y = boundsProtected(y, height);
        return new int[]{y, x};
    }
    public static int normalized(int y, int x, int width, int height) {
        @NotNull int[] yx = imageBounds(y, x, width, height);
        return yx[0] * width + yx[1];
    }
    @NotNull
    public static void intDDAAdd(@NotNull int[][] from, int[][] to) {
        if ((from.length != to.length) || (from[0].length != to[0].length)) {
            throw new IllegalArgumentException("Dimensions of both arguments must be the same.");
        }
        for (int i = 0; i < to.length; ++i) {
            for (int j = 0; j < to[i].length; ++j) {
                to[i][j] += from[i][j];
            }
        }
    }
    public static boolean approxEquals(@NotNull Complex a, @NotNull Complex b, double tolerance) {
        return Math.abs(a.real() - b.real()) <= tolerance && Math.abs(a.imaginary() - b.imaginary()) <= tolerance;
    }
    public static int weightedRandom(@NotNull double[] weights) {
        return (int) weightedRandom(null, weights);
    }
    public static double weightedRandom(@Nullable double[] values, @NotNull double[] weights) {
        int factor = 0, pidx = 0;
        double sum = 0.0;
        boolean custom = values != null;
        for (double weight : weights) {
            int afterpoint = (String.valueOf(weight).substring((String.valueOf(weight)).indexOf('.') + 1)).length();
            factor = ((afterpoint > factor) ? afterpoint : factor);
            sum += weight;
        }
        if (custom && (values.length != weights.length || (sum > 1.0 - ULP && sum < 1.0 + ULP))) {
            throw new IllegalArgumentException("Illegal Parameters");
        }
        factor = Math.round((float) Math.pow(10, factor));
        @NotNull double[] rand = new double[factor];
        for (int i = 0; i < rand.length; ) {
            if (i == weights[pidx] * factor) {
                ++pidx;
                continue;
            }
            rand[i] = (custom) ? values[pidx] : pidx;
            ++i;
        }
        return rand[new MersenneTwister().nextInt(rand.length)];
    }
    @NotNull
    public static int[] translateCoordinates(int x, int y, int ix, int iy, int fx, int fy) {
        return new int[]{(int) (((double) x / ix) * fx), (int) (((double) y / iy) * fy)};
    }
    public static int firstPrimeFrom(int from) {
        for (int i = from; i > 0; i++) {
            int factors = 0;
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    ++factors;
                }
            }
            if (factors == 0) {
                return i;
            }
        }
        return -1;
    }
    private static void quickSort(@Nullable int[][] arr, int low, int high) {
        if (arr == null || arr.length == 0) return;
        if (low >= high) return;
        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle][1];
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i][1] < pivot) {
                i++;
            }
            while (arr[j][1] > pivot) {
                j--;
            }
            if (i <= j) {
                @NotNull int[] temp = new int[2];
                System.arraycopy(arr[i], 0, temp, 0, temp.length);//temp=arr[i]
                System.arraycopy(arr[j], 0, arr[i], 0, arr[i].length);//arr[i]=arr[j]
                System.arraycopy(temp, 0, arr[j], 0, arr[j].length);//arr[j]=temp
                i++;
                j--;
            }
        }// recursively sort the 2 subparts
        if (low < j) quickSort(arr, low, j);
        if (high > i) quickSort(arr, i, high);
    }
    public static int indexOf(@NotNull int[] data, int element) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == element) {
                return i;
            }
        }
        return -1;
    }
    @NotNull
    public static int[] rankListFromHistogram(@NotNull int[] histogram) {
        @NotNull int[][] map = new int[histogram.length][2];
        for (int i = 0; i < histogram.length; i++) {
            map[i][0] = i;
            map[i][1] = histogram[i];
        }
        quickSort(map, 0, map.length - 1);
        @NotNull int[] rankList = new int[histogram.length];
        for (int i = 0; i < rankList.length; i++) {
            rankList[i] = map[i][0];
        }
        return rankList;
    }
    @NotNull
    public static Matrix complexToMatrix(@NotNull Complex data) {
        return new Matrix(new double[][]{{data.real()}, {data.imaginary()}});
    }
    @NotNull
    public static Complex matrixToComplex(@NotNull Matrix data) {
        return new Complex(data.get(0, 0), data.get(1, 0));
    }
    /**
     * Uses the 3-shears rotation technique over conventional rotation techniques for
     * improved image quality.
     *
     * @param angle the angle by which to rotate {@code point} about the global origin (0,0)
     * @param point the {@link Matrix} indicating the point to be rotated
     * @return the rotated point
     */
    @NotNull
    public static Matrix doRotate(@NotNull Matrix point, double angle) {
        @NotNull Matrix tanMatrix = new Matrix(new double[][]{{1, -Math.tan(angle / 2)}, {0, 1}});
        @NotNull Matrix sinMatrix = new Matrix(new double[][]{{1, 0}, {Math.sin(angle), 1}});
        return multiply(tanMatrix, multiply(sinMatrix, multiply(tanMatrix, point)));
    }
    /**
     * Same as {@link MathUtils#doRotate(Matrix, double)} above,
     * but does the rotation about the specified {@code origin} instead of the global one.
     *
     * @param angle the angle by which to rotate {@code point} about the given {@code origin}
     * @param point the {@link Matrix} indicating the point to be rotated
     * @param origin the {@link Matrix} indicating the origin about which {@code point} is to be rotated.
     * @return the rotated point
     */
    @NotNull
    public static Matrix doRotate(@NotNull Matrix point, @NotNull Matrix origin, double angle) {
        return add(doRotate(subtract(point, origin), angle), origin);
    }
    public static int[] linearize(int[][] data) {
        int[] linearized = new int[data.length * data[0].length];
        for (int i = 0; i < data.length; ++i) {
            System.arraycopy(data[i], 0, linearized, i * data[i].length, data[i].length);
        }
        return linearized;
    }
    public static int percentileValue(int[] data, double percentile) {
        int[] tmp = new int[data.length];
        System.arraycopy(data, 0, tmp, 0, data.length);
        quickSort(tmp);
        int idx = clamp(Math.round((float) (data.length * percentile)), data.length);
        return tmp[idx];
    }
    public static double percentileOf(int value, int[] data) {
        int less = 0, equal = 0;
        for (int item : data) {
            if (item == value) ++equal;
            else if (item < value) ++less;
        }
        return (less + 0.5 * equal) / data.length;
    }
    public static int median(int[] data) {
        return percentileValue(data, 0.5);
    }
    public static void quickSort(int[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }
    private static void quickSort(int[] arr, int low, int high) {
        if (arr == null || arr.length == 0) return;
        if (low >= high) return;
        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }
            while (arr[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }// recursively sort the 2 subparts
        if (low < j) quickSort(arr, low, j);
        if (high > i) quickSort(arr, i, high);
    }
    @NotNull
    public static int[] mostEfficientfactor(int a) {
        int num_factors = 0;
        for (int i = 1; i <= a; i++) {
            if (a % i == 0) num_factors++;
        }
        @NotNull int[] factors = new int[num_factors];
        num_factors = 0;
        for (int i = 1; i <= a && num_factors < factors.length; i++) {
            if (a % i == 0) {
                factors[num_factors] = i;
                num_factors++;
            }
        }
        @NotNull FactorData[] data = new FactorData[num_factors];
        for (int i = 0; i < num_factors; i++) {
            for (int j = i + 1; j < num_factors; j++) {
                data[i] = new FactorData(factors[i], factors[j]);
            }
        }
        quickSort(data, 0, data.length - 1);
        return new int[]{data[0].a, data[0].b};
    }
    private static void quickSort(@Nullable FactorData[] arr, int low, int high) {
        if (arr == null || arr.length == 0) return;
        if (low >= high) return;
        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle].sum;
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i].sum < pivot) {
                i++;
            }
            while (arr[j].sum > pivot) {
                j--;
            }
            if (i <= j) {
                @Nullable FactorData temp = new FactorData(arr[i]);
                arr[i] = new FactorData(arr[j]);
                arr[j] = new FactorData(temp);
                i++;
                j--;
            }
        }// recursively sort the 2 subparts
        if (low < j) quickSort(arr, low, j);
        if (high > i) quickSort(arr, i, high);
    }
    private static class FactorData {
        int a, b, sum;
        public FactorData(int a, int b) {
            this.a = a;
            this.b = b;
            this.sum = a + b;
        }
        public FactorData(@NotNull FactorData old) {
            a = old.a;
            b = old.b;
            sum = old.sum;
        }
    }
}