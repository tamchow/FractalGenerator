package in.tamchow.fractal.helpers;
/**
 * Converts a number in digit representation from one base to another
 * Not guaranteed to work with bases above 65,451 (more characters than which the lookup can use for substituting digits)
 *
 * @author Tamoghna Chowdhury
 * @version 1.2
 */
public class BaseConverter {
    protected static final String negativeBaseErrorMessage = "Negative or zero base values are illegal - supplied bases were %d & %d.", invalidInputNumberErrorMessage = "The supplied number (%s) for base conversion is invalid.", tooLargeBaseForLookupErrorMessage = "Not enough available characters for substitution. Number of available characters is %d , minimum required number is %d";
    private static String lookup = "0123456789ABCDEFGHIJKLMNOPQRSTWXYZabcdefghijklmnopqrstwxyz+/=,?!;:\"'^`~|\\@#$%&*_<>(){}";
    private static void updateLookup(int base) {
        int charsToAdd = base - lookup.length(); if (charsToAdd > 0) {
            for (int i = 0; i < Character.MAX_VALUE && charsToAdd > 0; ++i) {
                if ((!lookup.contains("" + (char) i)) &&
                            (!Character.isISOControl((char) i)) &&
                            (!Character.isWhitespace((char) i)) &&
                            ((char) i != '.') && (char) i != '-') {
                    lookup += (char) i; --charsToAdd;
                }
            }
        } if (charsToAdd > 0) {
            throw new IllegalArgumentException(String.format(tooLargeBaseForLookupErrorMessage, lookup.length(), base));
        }
    }
    public static long convertToNumber(String inputNumber, int from_base) {
        if (from_base >= lookup.length()) {
            updateLookup(from_base);
        } long number = 0; int length = inputNumber.length(); for (int i = 0; i < length; ++i) {
            long digitValue = lookup.indexOf(inputNumber.charAt(i));
            if (digitValue < 0) {//this digit does not exist in the lookup, so the input is incorrect
                throw new IllegalArgumentException(String.format(invalidInputNumberErrorMessage, inputNumber));
            } number += digitValue * Math.round(Math.pow(from_base, length - 1 - i));
        } return number;
    }
    private static int countDigits(long number, int to_base) {
        int num_digits = 0; while (number > 0) {
            number /= to_base; ++num_digits;
        } return num_digits;
    }
    private static int[] createDigits(long number, int to_base) {
        int[] digits = new int[countDigits(number, to_base)]; int num_digits = 0; while (number > 0) {
            digits[num_digits++] = (int) (number % to_base); number /= to_base;
        } return digits;
    }
    public static String changeBase(String inputNumber, int from_base, int to_base) {
        return changeBase(inputNumber, from_base, to_base, true);
    }
    private static boolean isInvalidInputNumber(String inputNumber, int from_base) {
        //there should be no more than 1 - sign, which should have been removed already if it exists
        return inputNumber.contains("-") ||
                       //there should be no more than 1 fixed point, which should have been removed already if it exists
                       inputNumber.contains(".") ||
                       //there should be no characters in the string which indicate values for a digit greater than the current base
                       (!inputNumber.matches(String.format("^[%s]*$", lookup.substring(0, from_base))));
    }
    public static String changeBase(String inputNumber, int from_base, int to_base, boolean substituteNumerics) {
        boolean isNegative = false; if (inputNumber.startsWith("-")) {
            //the provided number is (supposedly) negative
            inputNumber = inputNumber.substring(1, inputNumber.length()); isNegative = true;
        } if (from_base <= 0 || to_base <= 0) {
            //negative or zero bases can't be handled using simple integer arithmetic
            throw new IllegalArgumentException(String.format(negativeBaseErrorMessage, from_base, to_base));
        } updateLookup(Math.max(from_base, to_base)); if (inputNumber.contains(".")) {
            return changeBase(inputNumber.substring(0, inputNumber.indexOf(".")), from_base, to_base, substituteNumerics)/*Integer part*/ + "." +
                           changeBase(inputNumber.substring(inputNumber.indexOf(".") + 1, inputNumber.length()), from_base, to_base, substituteNumerics)/*Fractional part*/;
        }
        //checking for invalid numbers is easier now when negatives and fixed points should have been taken care of
        if (isInvalidInputNumber(inputNumber, from_base)) {
            throw new IllegalArgumentException(String.format(invalidInputNumberErrorMessage, inputNumber));
        }
        //special handling for 0 to avoid undefined behaviour and other bugs
        if (inputNumber.matches("^[0]+$")) {//check for any number of only 0s in the input
            return (isNegative) ? "-" + lookup.charAt(0) : lookup.charAt(0) + "";//I could simply return "0", but this looks less hardcoded
        } String new_number = ""; int[] digits = createDigits(convertToNumber(inputNumber, from_base), to_base);
        if (substituteNumerics) {
            //use character representation for digits
            for (int i = digits.length - 1; i >= 0; --i) {
                new_number += lookup.charAt(digits[i]);
            }
        } else {
            //use numeric representations for digits, separate using space
            for (int i = digits.length - 1; i >= 0; --i) {
                new_number += digits[i] + " ";
            }
        } return (isNegative) ? "-" + new_number.trim() : new_number.trim();
    }
}