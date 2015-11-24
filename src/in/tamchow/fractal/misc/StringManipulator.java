package in.tamchow.fractal.misc;

/**
 * Miscellaneous: String Manipulation.
 */
public class StringManipulator {
    public static String doCyclicShift(String input) {
        char[] processor = input.toCharArray();
        for (int i = 1; i < processor.length; i++) {
            char tmp = processor[i];
            processor[i] = processor[0];
            processor[0] = tmp;
        }
        return new String(processor);
    }

    private int count_char(char c, String str) {
        int ctr = 0;
        for (int i = 0; i < str.length(); i++) {
            if (c == str.charAt(ctr)) {
                ctr++;
            }
        }
        return ctr;
    }
}