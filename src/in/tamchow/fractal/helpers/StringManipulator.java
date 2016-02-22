package in.tamchow.fractal.helpers;
/**
 * Miscellaneous: String Manipulation.
 */
public class StringManipulator {
    public static final char BRACE_OPEN = '{', BRACE_CLOSE = '}', PARENTHESIS_OPEN = '(', PARENTHESIS_CLOSE = ')', SQUARE_OPEN = '[', SQUARE_CLOSE=']';
    public static String doCyclicShift(String input) {
        char[] processor = input.toCharArray(); for (int i = 1; i < processor.length; i++) {
            char tmp = processor[i]; processor[i] = processor[0]; processor[0] = tmp;
        } return new String(processor);}
    public static long doCyclicShift(long num) {
        int digits = Long.valueOf(num).toString().length(); long one = num % 10; long other = num / 10;
        return (long) (one * Math.pow(10, digits)) + other;}
    public static int indexOfBackwards(String search, int startindex, char tosearch) {
        for (int i = startindex; i > 0; i--) {if (search.charAt(i) == tosearch) {return i;}} return -1;}
    public static int indexOfBetweenBackwards(String search, int startindex, int endindex, char tosearch) {
        for (int i = startindex; i > endindex; i--) {if (search.charAt(i) == tosearch) {return i;}} return -1;}
    public static int indexOfBetween(String search, int startindex, int endindex, char tosearch) {
        for (int i = startindex; i < endindex; i++) {if (search.charAt(i) == tosearch) {return i;}} return -1;
    }
    public static int findMatchingOpener(char toMatch, String in, int closeIndex) {
        int openIndex = closeIndex; int counter = 0; while (counter >= 0 && openIndex >= 0) {
            char c = in.charAt(openIndex); switch (toMatch) {
                case PARENTHESIS_CLOSE: if (c == PARENTHESIS_OPEN) {counter--;} else if (c == PARENTHESIS_CLOSE) {
                    counter++;
                } break;
                case BRACE_CLOSE: if (c == BRACE_OPEN) {counter--;} else if (c == BRACE_CLOSE) {counter++;} break;
                case SQUARE_CLOSE: if (c == SQUARE_OPEN) {counter--;} else if (c == SQUARE_CLOSE) {counter++;} break;
                default: throw new IllegalArgumentException("Unsupported match character");
            } if (counter == 0) {return openIndex;} openIndex--;
        } return -1;
    }
    public static int findMatchingCloser(char toMatch, String in, int openIndex) {
        int closeIndex = openIndex; int counter = 0; while (counter >= 0 && closeIndex < in.length()) {
            char c = in.charAt(closeIndex); switch (toMatch) {
                case PARENTHESIS_OPEN: if (c == PARENTHESIS_OPEN) {counter++;} else if (c == PARENTHESIS_CLOSE) {
                    counter--;
                } break;
                case BRACE_OPEN: if (c == BRACE_OPEN) {counter++;} else if (c == BRACE_CLOSE) {counter--;} break;
                case SQUARE_OPEN: if (c == SQUARE_OPEN) {counter++;} else if (c == SQUARE_CLOSE) {counter--;} break;
                default: throw new IllegalArgumentException("Unsupported match character");
            } if (counter == 0) {return closeIndex;} closeIndex++;
        } return -1;}
    public static int getNumFromIndex(String str, int idx) {
        String num = ""; boolean negative = false; if (str.charAt(idx) == '_') {negative = true; idx++;}
        for (int i = idx; i < str.length(); i++) {
            char current = str.charAt(i); if (Character.isDigit(current)) {num += current;} else {break;}
        } if (negative) {
            return -Integer.valueOf(num);
        } return Integer.valueOf(num);
    }
    public static int nthIndexBackwards(String s, char c, int idx, int n) {
        int ctr = 0; for (int i = idx; i >= 0; i--) {if (ctr == n) {return i;} if (s.charAt(i) == c) {ctr++;}}
        return -1;
    }
    public static int nthIndex(String s, char c, int idx, int n) {
        int ctr = 0; for (int i = idx; i < s.length(); i++) {if (ctr == n) {return i;} if (s.charAt(i) == c) {ctr++;}}
        return -1;
    }
    private int count_char(char c, String str) {
        int ctr = 0; for (int i = 0; i < str.length(); i++) {if (c == str.charAt(ctr)) {ctr++;}} return ctr;}}