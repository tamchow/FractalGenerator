package in.tamchow.fractal.misc.mathstuff;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.math.BaseConverter;
/**
 * Maths Stuff, mostly from code-golfing
 */
public class MathStuff {
    @NotNull
    public static String numberLineRepresentation(float number, int precision) {
        int g = (int) number, d = Math.round((number - g) * precision), a = ("" + g + 1).length(), b = ("" + g).length(), i = 0;
        @NotNull String h = "", q = "" + g;
        int c = q.length();
        for (; i < b; i++) h += " ";
        for (++i; i <= b + precision; i++) h += "-";
        for (i = c; i < c + d; i++) q += "|";
        for (; i < precision + b; i++) q += " ";
        return q + (g + 1) + "\n" + h;
    }
    public static int[] diamondPuzzleSolver(int sum, int product, int low, int high) {
        for (int a = low; a <= high; a++) {
            for (int b = low; b <= high; b++) if (a + b == sum && a * b == product) return new int[]{a, b};
        }
        return null;
    }
    @NotNull
    public static int[] diamondPuzzleSolverQuadratic(int sum, int product) {
        int x = sum + (int) Math.sqrt(sum * sum - 4 * product);
        return new int[]{x / 2, sum - x / 2};
    }
    @NotNull
    private static String negated(String binaryNumber) {
        char[] negated = new char[binaryNumber.length()];
        for (int i = 0; i < negated.length; ++i) {
            if (binaryNumber.charAt(i) == '0') {
                negated[i] = '1';
            } else if (binaryNumber.charAt(i) == '1') {
                negated[i] = '0';
            }
        }
        return new String(negated);
    }
    public static long solveJosephus(long number) {
        long negated = Long.parseLong(negated(BaseConverter.changeBase(number + "", 10, 2, true)), 2);
        return number - negated;
    }
}
