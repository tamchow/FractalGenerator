package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;
import in.tamchow.fractal.math.complex.Complex;

import java.util.ArrayList;
import java.util.List;
/**
 * Denotes a symbolic mathematical entity which supports certain operations.
 *
 * <h3>Note:</h3>
 * <p>
 * Also, the {@link Operable#add(Operable)}, {@link Operable#subtract(Operable)}, {@link Operable#multiply(Operable)} and {@link Operable#divide(Operable)}
 * only work with {@link Operable}s of the same type.
 *
 * That is, only a {@link Function} can be operated with a {@link Function},
 * and only a {@link Polynomial} can be operated with a {@link Polynomial}.
 * </p>
 */
public abstract class Operable<T extends Operable<T, E>, E extends Derivable> extends Derivable {
    private static final int STRING_PREFIX_SIZE = 3;
    private static int timesDivided = 0;
    protected ArrayList<T> multipliers, denominators;
    protected ArrayList<E> terms;
    protected ArrayList<String> signs;
    protected String[][] consts;
    protected String z_value;
    protected String variableCode;
    protected String oldvariablecode;
    protected Operable() {
        multipliers = new ArrayList<>();
        denominators = new ArrayList<>();
        terms = new ArrayList<>();
        signs = new ArrayList<>();
    }
    protected Operable(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        this();
        setZ_value(variable);
        setConsts(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
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
    public String getZ_value() {
        return z_value;
    }
    public void setZ_value(String z_value) {
        this.z_value = z_value;
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public String getOldvariablecode() {
        return oldvariablecode;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
    }
    public List<E> getTerms() {
        return terms;
    }
    protected void setTerms(@NotNull List<E> terms) {
        this.terms.clear();
        this.terms.addAll(terms);
    }
    public ArrayList<String> getSigns() {
        return signs;
    }
    protected void setSigns(@NotNull List<String> signs) {
        this.signs.clear();
        this.signs.addAll(signs);
    }
    @SuppressWarnings("unchecked")
    public T add(T other) {
        terms.addAll(other.terms);
        signs.addAll(other.signs);
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T multiply(T other) {
        multipliers.add(other);
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T divide(T other) {
        ++timesDivided;
        if (timesDivided % 2 == 0) {
            multipliers.add(other);
            normalizeDenominatorAll(other, false);
        } else {
            denominators.add(other);
            normalizeDenominatorAll(other, true);
        }
        return (T) this;
    }
    protected void normalizeDenominatorAll(T other, boolean dividing) {
        if (!other.denominators.isEmpty()) {
            int timesDivided = 0;
            for (T subOther : other.denominators) {
                ++timesDivided;
                other.normalizeDenominatorAll(subOther, timesDivided % 2 != 0);
            }
        }
        if (dividing) {
            multipliers.addAll(other.denominators);
            denominators.addAll(other.multipliers);
        } else {
            multipliers.addAll(other.multipliers);
            denominators.addAll(other.denominators);
        }
    }
    @SuppressWarnings("unchecked")
    public T subtract(T other) {
        other.negate();
        add(other);
        return (T) this;
    }
    public Operable<T, E> negate() {
        for (int i = 0; i < signs.size(); ++i) {
            if (signs.get(i).equals("+")) {
                signs.set(i, "-");
            } else if (signs.get(i).equals("-")) {
                signs.set(i, "+");
            }
        }
        return this;
    }
    protected String process(String repr) {
        repr = repr.trim();
        if (repr.charAt(0) == '+') {
            return repr.substring(1, repr.length());
        }
        return "( " + repr + " )";
    }
    @NotNull
    protected String toStringBase() {
        @NotNull ResizableCharBuffer function = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            function.append(" " + signs.get(j) + " " + terms.get(i));
        }
        return process(function.toString()).trim();
    }
    public abstract Complex getDegree();
    protected boolean useEx() {
        return !(multipliers.isEmpty() || denominators.isEmpty());
    }
    public String derivative(int order) {
        if (useEx()) {
            switch (order) {
                case 0:
                    return toString();
                case 1:
                    //TODO:Implement
                    break;
                case 2:
                    //TODO:Implement
                    break;
                default:
                    throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
            }
        }
        return derivativeBase(order);
    }
    public String firstDerivative() {
        return derivative(1).trim();
    }
    public String secondDerivative() {
        return derivative(2).trim();
    }
    @NotNull
    @Override
    public String toString() {
        return useEx() ? "( ( " + toStringBase() + multiplyTerms(multipliers, false) + " ) / ( " + multiplyTerms(denominators, true) + " )" : toStringBase();
    }
    private String multiplyTerms(List<T> terms, boolean hasNoPreceding) {
        ResizableCharBuffer repr = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (T term : terms) {
            repr.append(" ) * ( " + term + " )");
        }
        if (hasNoPreceding) {
            return repr.toString().substring(5, repr.length());//trim leading multiply
        }
        return repr.toString();
    }
}