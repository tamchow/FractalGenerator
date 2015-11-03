package in.tamchow.fractal.misc;

/**
 * Miscellaneous: Implementation of a cyclic shifting algorithm for strings.
 */
public class CyclicShift {
    public static String doCyclicShift(String input) {
        char[] processor = input.toCharArray();
        for (int i = 1; i < processor.length; i++) {
            char tmp = processor[i];
            processor[i] = processor[0];
            processor[0] = tmp;
        }
        return new String(processor);
    }
}
