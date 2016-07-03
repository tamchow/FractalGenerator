package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;
import in.tamchow.fractal.helpers.strings.StringManipulator;
/**
 * Holds a transcendental function chain
 */
public class Function extends Operable<Function, FunctionTerm> {
    public Function(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        super(variable, variableCode, oldvariablecode, varconst);
    }
    public Function() {
        super();
    }
    public static boolean isSpecialFunction(@NotNull String function) {
        return FunctionTerm.isSpecialFunctionTerm(function);
    }
    @NotNull
    public static Function fromString(@NotNull String function, String variableCode, String oldvariablecode, String[][] consts) {
        return fromString(function, variableCode, oldvariablecode, consts, true);
    }
    @NotNull
    public static Function fromString(@NotNull String function, String variableCode, String oldvariablecode, String[][] consts, boolean deepEvaluate) {
        @NotNull Function poly = new Function(null, variableCode, oldvariablecode, consts);
        if (deepEvaluate) {
            String[] divisors = StringManipulator.split(function, "|/|");
            Function[] dividing = new Function[divisors.length];
            for (int i = 0; i < divisors.length; ++i) {
                String[] multipliers = StringManipulator.split(divisors[i], "|*|");
                Function[] multiplying = new Function[multipliers.length];
                for (int j = 0; j < multipliers.length; ++j) {
                    multiplying[j] = fromString(multipliers[j], variableCode, oldvariablecode, consts, false);
                }
                Function multiplied = multiplying[0];
                for (int j = 1; j < multiplying.length; ++j) {
                    multiplied.multiply(multiplying[j]);
                }
                dividing[i] = multiplied;
            }
            Function divided = dividing[0];
            for (int i = 1; i < dividing.length; ++i) {
                divided.divide(dividing[i]);
            }
            poly = divided;
            return poly;
        }
        @NotNull String[] tokens = StringManipulator.split(function, "|");
        poly.terms.ensureCapacity(tokens.length);
        poly.signs.ensureCapacity(tokens.length + 1);
        for (@NotNull String token : tokens) {
            if (token.equals("+") || token.equals("-")) {
                poly.signs.add(token.trim());
            } else {
                poly.terms.add(FunctionTerm.fromString(token.trim(), variableCode, consts, oldvariablecode));
            }
        }
        if (poly.signs.size() == poly.terms.size() - 1) {
            poly.signs.add(0, "+");
        }
        return poly;
    }
    @Override
    @NotNull
    public String derivativeBase(int order) {
        @NotNull ResizableCharBuffer deriv = new ResizableCharBuffer(STRING_PREFIX_SIZE * terms.size() * terms.get(0).toString().length());
        if (order < 0) {
            throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        } else if (order == 0) {
            return toString();
        }
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            deriv.append(" ").append(signs.get(j)).append(" ").append(terms.get(i).derivativeBase(order));
        }
        return process(deriv.toString());
    }
}