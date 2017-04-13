package in.tamchow.fractal.helpers.math;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
/**
 * Converts a number in digit representation from one base to another
 * Not guaranteed to work with bases above 65,451 (more characters than which the lookup can use for substituting digits)
 *
 * @author Tamoghna Chowdhury
 * @version 1.2
 */
public final class BaseConverter {
    private static final Pattern ZEROS = Pattern.compile("^[0]+$"), SPACES = Pattern.compile("\\s+");
    private static final int RESTRICTED_CHARS_COUNT = 84, MAX_LOOKUP_LENGTH = Character.MAX_VALUE - RESTRICTED_CHARS_COUNT;
    private static final String negativeBaseErrorMessage = "Negative or zero base values are illegal - supplied bases were %d & %d.", invalidInputNumberErrorMessage = "The supplied number (%s) for base conversion is invalid.", tooLargeBaseForLookupErrorMessage = "Not enough available characters for substitution. Number of available characters is %d , minimum required number is %d";
    @NotNull
    private static String lookup = "0123456789ABCDEFGHIJKLMNOPQRSTWXYZabcdefghijklmnopqrstwxyz+/=,?!;:\"'^`~|\\@#$%&*_<>(){}";
    private static int lastUsedCharIndex = 0;
    private BaseConverter() {
    }
    private static void updateLookup(int base) {
        if (base < lookup.length()) {
            return;
        } else if (base > MAX_LOOKUP_LENGTH) {//Substitution and digit lookup may be impossible, so we we throw an exception here
            throw new IllegalArgumentException(String.format(tooLargeBaseForLookupErrorMessage, lookup.length(), base));
        }
        int charsToAdd = base - lookup.length();
        @NotNull char[] extras = new char[charsToAdd];
        if (charsToAdd > 0) {
            for (int i = lastUsedCharIndex; i < Character.MAX_VALUE && charsToAdd > 0; ++i) {
                if ((!lookup.contains("" + (char) i)) &&
                        (!Character.isISOControl((char) i)) &&
                        (!Character.isWhitespace((char) i)) &&
                        ((char) i != '.') && (char) i != '-') {
                    extras[extras.length - charsToAdd] = (char) i;
                    lastUsedCharIndex = i;
                    --charsToAdd;
                }
            }
        }
        lookup += new String(extras);
        if (charsToAdd > 0) {//Just in case the top one didn't work.
            throw new IllegalArgumentException(String.format(tooLargeBaseForLookupErrorMessage, lookup.length(), base));
        }
    }
    public static long convertToNumber(@NotNull String inputNumber, int from_base) {
        return convertToNumber(inputNumber, from_base, true);
    }
    public static boolean isNegative(@NotNull String number) {
        return number.startsWith("-");
    }
    public static long convertToNumber(@NotNull String inputNumber, int from_base, boolean checkIfNegative) {
        @NotNull String copyOfInput = inputNumber;
        boolean isNegative = checkIfNegative && isNegative(inputNumber);
        if (isNegative) {
            //the provided number is (supposedly) negative
            inputNumber = inputNumber.substring(1, inputNumber.length());
        }
        if (from_base >= lookup.length()) {
            updateLookup(from_base);//in case this was called directly
        }
        long number = 0;
        int length = inputNumber.length();
        for (int i = 0; i < length; ++i) {
            long digitValue = lookup.indexOf(inputNumber.charAt(i));
            if (digitValue < 0) {//this digit does not exist in the lookup, so the input is incorrect
                throw new IllegalArgumentException(String.format(invalidInputNumberErrorMessage, copyOfInput));
            }
            number += digitValue * Math.round(Math.pow(from_base, length - 1 - i));
        }
        return isNegative ? -number : number;
    }
    public static long convertToNumber(@NotNull int[] digits, int from_base) {
        long number = 0;
        int length = digits.length;
        for (int i = 0; i < length; ++i) {
            number += digits[i] * Math.round(Math.pow(from_base, length - 1 - i));
        }
        return number;
    }
    private static int countDigits(long number, int to_base) {
        int num_digits = 0;
        while (number > 0) {
            number /= to_base;
            ++num_digits;
        }
        return num_digits;
    }
    @NotNull
    private static int[] createDigits(long number, int to_base) {
        @NotNull int[] digits = new int[countDigits(number, to_base)];
        int num_digits = 0;
        while (number > 0) {
            digits[num_digits++] = (int) (number % to_base);
            number /= to_base;
        }
        return digits;
    }
    @NotNull
    public static String changeBase(@NotNull String inputNumber, int from_base, int to_base) {
        return changeBase(inputNumber, from_base, to_base, true);
    }
    private static boolean isInvalidInputNumber(@NotNull String inputNumber, int from_base) {
        //there should be no more than 1 - sign, which should have been removed already if it exists
        return inputNumber.contains("-") ||
                //there should be no more than 1 fixed point, which should have been removed already if it exists
                inputNumber.contains(".") ||
                //there should be no characters in the string which indicate values for a digit greater than the current base
                (!inputNumber.matches(String.format("^[%s]*$", lookup.substring(0, from_base))));
    }
    /**
     * Utility method for parsing an array of Strings to an array of ints
     *
     * @param data the {@link String} array to parse into a an {@code int} array
     * @return {@code int} array containing the parsed values
     */
    @NotNull
    private static int[] parseStringsToIntegers(@NotNull String[] data) {
        @NotNull int[] ints = new int[data.length];
        for (int i = 0; i < data.length; ++i) {
            ints[i] = Integer.valueOf(data[i]);
        }
        return ints;
    }
    /**
     * Utility method for parsing an array of Strings to an array of doubles
     *
     * @param data the {@link String} array to parse into a an {@code double} array
     * @return {@code double} array containing the parsed values
     */
    @NotNull
    private static double[] parseStringsToDoubles(@NotNull String[] data) {
        @NotNull double[] ints = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            ints[i] = Double.valueOf(data[i]);
        }
        return ints;
    }
    public static double convertToNumber(@NotNull double[] digits, double from_base) {
        double number = 0;
        int length = digits.length;
        for (int i = 0; i < length; ++i) {
            number += digits[i] * Math.pow(from_base, length - 1 - i);
        }
        return number;
    }
    @NotNull
    private static double[] createDigits(double number, double to_base) {
        @NotNull double[] digits = new double[countDigits(number, to_base)];
        int num_digits = 0;
        while (number > 0) {
            digits[num_digits++] = (number % to_base);
            number /= to_base;
        }
        return digits;
    }
    private static int countDigits(double number, double to_base) {
        int num_digits = 0;
        while (number > 0) {
            number /= to_base;
            ++num_digits;
        }
        return num_digits;
    }
    @NotNull
    public static double[] changeBase(@NotNull double[] digits, double from_base, double to_base) {
        return createDigits(convertToNumber(digits, from_base), to_base);
    }
    @NotNull
    public static String changeBase(@NotNull String inputNumber, double from_base, double to_base) {

        boolean isNegative = false;
        if (inputNumber.startsWith("-")) {
            isNegative = true;
            inputNumber = inputNumber.substring(1, inputNumber.length());
        }
        DoubleStream digits = Arrays.stream(changeBase(parseStringsToDoubles(inputNumber.split("\\s+")), from_base, to_base));
        String new_number = digits
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(" "))
                .trim();
        return isNegative ? "-" + new_number : new_number;
    }
    @NotNull
    public static String changeBase(@NotNull String inputNumber, int from_base, int to_base, boolean substituteNumerics) {
        @NotNull String copyOfInput = inputNumber;
        boolean isNegative = isNegative(inputNumber);
        if (isNegative) {
            //the provided number is (supposedly) negative
            inputNumber = inputNumber.substring(1, inputNumber.length());
        }
        if (from_base <= 0 || to_base <= 0) {
            //negative or zero bases can't be handled using simple integer arithmetic
            throw new IllegalArgumentException(String.format(negativeBaseErrorMessage, from_base, to_base));
        }
        updateLookup(Math.max(from_base, to_base));
        //Note - This is in fact incorrect behavior, but this is a stopgap to not crash on decimal input
        if (inputNumber.contains(".")) {
            return changeBase(inputNumber.substring(0, inputNumber.indexOf(".")), from_base, to_base, substituteNumerics)/*Integer part*/ + "." +
                    changeBase(inputNumber.substring(inputNumber.indexOf(".") + 1, inputNumber.length()), from_base, to_base, substituteNumerics)/*Fractional part*/;
        }
        //checking for invalid numbers is easier now when negatives and fixed points should have been taken care of
        if (isInvalidInputNumber(inputNumber, from_base)) {
            throw new IllegalArgumentException(String.format(invalidInputNumberErrorMessage, copyOfInput));
        }
        //special handling for 0 to avoid undefined behaviour and other bugs
        if (ZEROS.matcher(inputNumber).find()) {//check for any number of only 0s in the input
            return (isNegative) ? "-" + lookup.charAt(0) : lookup.charAt(0) + "";//I could simply return "0", but this looks less hardcoded
        }
        IntStream digits = Arrays.stream(SPACES.matcher(inputNumber).find() ?
                //presence of whitespaces imply the number is in numeric digit format
                createDigits(convertToNumber(parseStringsToIntegers(SPACES.split(inputNumber)), from_base), to_base) :
                createDigits(convertToNumber(inputNumber, from_base, false), to_base));
        String new_number = (substituteNumerics ? digits.map(digit -> lookup.charAt(digit)) : digits)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(" "))
                .trim();
        return (isNegative) ? "-" + new_number : new_number;//indicate negative if necessary (+ sign indicates a digit!), no trailing whitespace
    }
}