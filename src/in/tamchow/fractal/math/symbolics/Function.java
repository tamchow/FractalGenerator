package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;
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
        @NotNull Function poly = new Function(null, variableCode, oldvariablecode, consts);
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
        @NotNull String deriv = "";
        if (order < 0) {
            throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        } else if (order == 0) {
            return toString();
        }
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            deriv += " " + signs.get(j) + " " + terms.get(i).derivativeBase(order);
        }
        return process(deriv);
    }
    @NotNull
    @Override
    public Complex getDegree() {
        @NotNull Complex degree = new Complex(Complex.ZERO);
        for (@NotNull FunctionTerm term : terms) {
            @NotNull Complex vardeg = term.getDegree();
            if (vardeg.modulus() > degree.modulus()) {
                degree = new Complex(vardeg);
            }
        }
        return degree;
    }
}