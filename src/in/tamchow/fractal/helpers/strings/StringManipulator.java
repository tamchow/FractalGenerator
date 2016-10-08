package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
/**
 * Miscellaneous: String manipulating utility methods.
 */
public final class StringManipulator {
    public static final char BRACE_OPEN = '{', BRACE_CLOSE = '}', PARENTHESIS_OPEN = '(', PARENTHESIS_CLOSE = ')', SQUARE_OPEN = '[', SQUARE_CLOSE = ']';
    private StringManipulator() {
    }
    public static <T> String join(final T[] items, String prefix, String suffix, String joiner) {
        return join(Arrays.asList(items), prefix, suffix, joiner);
    }
    public static <T> String join(final List<T> items, String prefix, String suffix, String joiner) {
        final int length = items.size();
        ResizableCharBuffer buffer = new ResizableCharBuffer(prefix.length() + suffix.length() + length *
                (joiner.length() + items.get(0).toString().length()));
        buffer.append(prefix);
        for (int i = 0; i < length; ++i) {
            T item = items.get(i);
            if (i == length - 1) {
                buffer.append(item);
            } else {
                buffer.append(item).append(joiner);
            }
        }
        return buffer.append(suffix).toString();
    }
    public static String createRepeat(char item, int repeats) {
        return createRepeat("" + item, repeats);
    }
    public static String createRepeat(String item, int repeats) {
        CharBuffer buffer = new CharBuffer(repeats * item.length());
        for (int i = 0; i < repeats; ++i) {
            buffer.append(item);
        }
        return buffer.toString();
    }
    public static int getRepeats(String str, int idx) {
        int ctr = 1;
        char current = str.charAt(idx++);
        while (idx < str.length() && current == str.charAt(idx)) {
            ++ctr;
            ++idx;
        }
        return ctr;
    }
    @NotNull
    public static String doCyclicShift(@NotNull String input, int positions) {
        @NotNull char[] processor = input.toCharArray();
        while (positions-- > 0) {
            for (int i = 1; i++ < processor.length; ) {
                char tmp = processor[i];
                processor[i] = processor[0];
                processor[0] = tmp;
            }
        }
        return new String(processor);
    }
    public static long doCyclicShift(long num) {
        int digits = Long.valueOf(num).toString().length();
        long one = num % 10;
        long other = num / 10;
        return (long) (one * Math.pow(10, digits)) + other;
    }
    public static long doCyclicShift(long num, int positions) {
        while (positions-- > 0) {
            num = doCyclicShift(num);
        }
        return num;
    }
    public static int indexOfBackwards(@NotNull String search, int startindex, char tosearch) {
        for (int i = startindex; i > 0; i--) {
            if (search.charAt(i) == tosearch) {
                return i;
            }
        }
        return -1;
    }
    public static int indexOfBetweenBackwards(@NotNull String search, int startindex, int endindex, char tosearch) {
        for (int i = startindex; i > endindex; i--) {
            if (search.charAt(i) == tosearch) {
                return i;
            }
        }
        return -1;
    }
    public static int indexOfBetween(@NotNull String search, int startindex, int endindex, char tosearch) {
        for (int i = startindex; i < endindex; i++) {
            if (search.charAt(i) == tosearch) {
                return i;
            }
        }
        return -1;
    }
    public static int findMatchingOpener(char toMatch, @NotNull String in, int closeIndex) {
        int openIndex = closeIndex;
        int counter = 0;
        while (counter >= 0 && openIndex >= 0) {
            char c = in.charAt(openIndex);
            switch (toMatch) {
                case PARENTHESIS_CLOSE:
                    if (c == PARENTHESIS_OPEN) {
                        counter--;
                    } else if (c == PARENTHESIS_CLOSE) {
                        counter++;
                    }
                    break;
                case BRACE_CLOSE:
                    if (c == BRACE_OPEN) {
                        counter--;
                    } else if (c == BRACE_CLOSE) {
                        counter++;
                    }
                    break;
                case SQUARE_CLOSE:
                    if (c == SQUARE_OPEN) {
                        counter--;
                    } else if (c == SQUARE_CLOSE) {
                        counter++;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported match character");
            }
            if (counter == 0) {
                return openIndex;
            }
            openIndex--;
        }
        return -1;
    }
    public static int findMatchingCloser(char toMatch, @NotNull String in, int openIndex) {
        int closeIndex = openIndex;
        int counter = 0;
        while (counter >= 0 && closeIndex < in.length()) {
            char c = in.charAt(closeIndex);
            switch (toMatch) {
                case PARENTHESIS_OPEN:
                    if (c == PARENTHESIS_OPEN) {
                        counter++;
                    } else if (c == PARENTHESIS_CLOSE) {
                        counter--;
                    }
                    break;
                case BRACE_OPEN:
                    if (c == BRACE_OPEN) {
                        counter++;
                    } else if (c == BRACE_CLOSE) {
                        counter--;
                    }
                    break;
                case SQUARE_OPEN:
                    if (c == SQUARE_OPEN) {
                        counter++;
                    } else if (c == SQUARE_CLOSE) {
                        counter--;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported match character");
            }
            if (counter == 0) {
                return closeIndex;
            }
            closeIndex++;
        }
        return -1;
    }
    public static int getNumFromIndex(@NotNull String str, int idx) {
        String num = "";
        boolean negative = false;
        if (str.charAt(idx) == '_') {
            negative = true;
            idx++;
        }
        for (int i = idx; i < str.length(); i++) {
            char current = str.charAt(i);
            if (Character.isDigit(current)) {
                num += current;
            } else {
                break;
            }
        }
        if (negative) {
            return -Integer.valueOf(num);
        }
        return Integer.valueOf(num);
    }
    public static int nthIndexBackwards(@NotNull String s, char c, int idx, int n) {
        int ctr = 0;
        for (int i = idx; i >= 0; i--) {
            if (ctr == n) {
                return i;
            }
            if (s.charAt(i) == c) {
                ctr++;
            }
        }
        return -1;
    }
    public static int nthIndex(@NotNull String s, char c, int idx, int n) {
        int ctr = 0;
        for (int i = idx; i < s.length(); i++) {
            if (ctr == n) {
                return i;
            }
            if (s.charAt(i) == c) {
                ctr++;
            }
        }
        return -1;
    }
    @NotNull
    public static int[] indexesOf(@NotNull String in, char what) {
        return indexesOf(in, "" + what);
    }
    public static int countOccurrencesOf(@NotNull String in, char what) {
        return countOccurrencesOf(in, "" + what);
    }
    @NotNull
    public static String format(@NotNull String toFormat, @NotNull String[][] details) {
        @NotNull String formatted = toFormat;
        for (String[] detail : details) {
            formatted = replace(formatted, detail[0], detail[1]);
        }
        return formatted;
    }
    @NotNull
    public static String correctPadding(String in, String[] operations) {
        for (String operation : operations) {
            in = replace(in, operation, " " + operation + " ");
        }
        in = replace(in, "  ", " ");
        return in.trim();
    }
    @NotNull
    public static int[] indexesOf(@NotNull String in, @NotNull String what) {
        @NotNull int[] backup = new int[in.length()], indexes;
        int idx = 0, count = 0;
        while ((idx = in.indexOf(what, idx)) != -1) {
            backup[count++] = idx++;
        }
        indexes = new int[count];
        System.arraycopy(backup, 0, indexes, 0, count);
        return indexes;
    }
    public static int countOccurrencesOf(@NotNull String in, @NotNull String what) {
        //return indexesOf(in, what).length;
        int index = in.indexOf(what);
        int occurrences = 0;
        while (index != -1) {
            ++occurrences;
            in = in.substring(index + 1);
            index = in.indexOf(what);
        }
        return occurrences;
    }
    @NotNull
    public static String delete(@NotNull String value, @NotNull String what) {
        return replace(value, what, "");
    }
    @NotNull
    public static String replace(@NotNull String value, @NotNull String from, @Nullable String to) {
        if (value.length() == 0 || from.length() == 0) {
            //the length checks will throw the necessary NullPointerExceptions
            return value;
        }
        if (to == null) {
            to = "";
        }
        @NotNull String result = value;
        int lastIndex = 0, index = value.indexOf(from), fl = from.length(), replaceCount = countOccurrencesOf(value, from);
        if (index != -1) {
            @NotNull CharBuffer buffer = new ResizableCharBuffer(result.length() - replaceCount * (fl - to.length()));
            while (index != -1) {
                buffer.append(value.substring(lastIndex, index)).append(to);
                lastIndex = index + fl;
                index = value.indexOf(from, lastIndex);
            }
            buffer.append(value.substring(lastIndex));
            result = buffer.toString();
        }
        return result;
    }
    @NotNull
    public static String[] split(@NotNull String what, @NotNull String at) {
        if (what.isEmpty() || at.isEmpty()) {
            //the length checks will throw the necessary NullPointerExceptions
            throw new IllegalArgumentException("Empty String");
        }
        if (what.isEmpty()) {
            return new String[]{""};
        }
        if (at.isEmpty()) {
            return new String[]{what};
        }
        @NotNull String[] result = new String[countOccurrencesOf(what, at) + 1];
        if (result.length == 1) {
            result[0] = what;
            return result;
        }
        int lastIndex = 0, index = what.indexOf(at), fl = at.length(), sidx = 0;
        while (index != -1) {
            result[sidx++] = what.substring(lastIndex, index);
            lastIndex = index + fl;
            index = what.indexOf(at, lastIndex);
        }
        result[sidx] = what.substring(lastIndex);
        return result;
    }
}