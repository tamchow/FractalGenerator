package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;

import java.util.ArrayList;
/**
 * Holds a transcendental function chain
 */
public class Function extends Operable<Function, FunctionTerm> {
    private String[][] consts;
    private String z_value;
    private String variableCode, oldvariablecode;
    public Function(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        super();
        setZ_value(variable);
        setConsts(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
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
    public String getOldvariablecode() {
        return oldvariablecode;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
    }
    public String getZ_value() {
        return z_value;
    }
    public void setZ_value(String z_value) {
        this.z_value = z_value;
    }
    public String[][] getConsts() {
        return consts;
    }
    public void setConsts(@NotNull String[][] constdec) {
        consts = new String[constdec.length][constdec[0].length];
        for (int i = 0; i < this.consts.length; i++) {
            System.arraycopy(constdec[i], 0, consts[i], 0, consts[i].length);
        }
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public ArrayList<String> getSigns() {
        return signs;
    }
    public void setSigns(@NotNull ArrayList<String> signs) {
        this.signs.clear();
        this.signs.addAll(signs);
    }
    public ArrayList<FunctionTerm> getTerms() {
        return terms;
    }
    public void setTerms(@NotNull ArrayList<FunctionTerm> terms) {
        this.terms.clear();
        this.terms.addAll(terms);
    }
    @Override
    @NotNull
    public String derivative(int order) {
        @NotNull String deriv = "";
        if (order < 0) {
            throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        } else if (order == 0) {
            return toString();
        }
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            deriv += " " + signs.get(j) + " " + terms.get(i).derivative(order);
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