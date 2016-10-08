package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.CharBuffer;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;
import in.tamchow.fractal.math.complex.Complex;

import java.util.ArrayList;
import java.util.List;

import static in.tamchow.fractal.helpers.strings.StringManipulator.*;
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
    protected static final int STRING_PREFIX_SIZE = 3;
    private static final int OPTIONAL_PREFIX_SIZE = 2;
    private static final String DIVISION_DERIVATIVE_1 = "((v*$u)-(u*$v))/(v^" + _2 + ")",
            DIVISION_DERIVATIVE_2 = "(((v^" + _2 + ")*$$u)-(v*((" + _2 + "*$u*$v)+(u*$$v)))+(" + _2 + "*u*($v^" + _2 + ")))/(v^" + _3 + ")";
    private static int timesDivided = 0;
    protected ArrayList<T> multipliers, denominators;
    protected ArrayList<E> terms;
    protected ArrayList<String> signs;
    protected String[][] consts;
    protected String z_value;
    protected String variableCode;
    protected String oldvariablecode;
    protected Operable() {
        init(null, null, null, null);
    }
    protected Operable(@Nullable String variable, @Nullable String variableCode, @Nullable String oldvariablecode, @Nullable String[][] varconst) {
        init(variable, variableCode, oldvariablecode, varconst);
    }
    protected Operable(T other) {
        init(other);
    }
    protected void init(@Nullable String variable, @Nullable String variableCode, @Nullable String oldvariablecode, @Nullable String[][] varconst) {
        multipliers = new ArrayList<>();
        denominators = new ArrayList<>();
        terms = new ArrayList<>();
        signs = new ArrayList<>();
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
    protected void init(T other) {
        init(other.z_value, other.variableCode, other.oldvariablecode, other.consts);
        setTerms(other.terms);
        setSigns(other.signs);
        setMultipliers(other.multipliers);
        setDenominators(other.denominators);
    }
    protected void setMultipliers(@NotNull List<T> multipliers) {
        this.multipliers.clear();
        this.multipliers.addAll(multipliers);
    }
    protected void setDenominators(@NotNull List<T> denominators) {
        this.denominators.clear();
        this.denominators.addAll(denominators);
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
    @SuppressWarnings("unchecked")
    public T negate() {
        for (int i = 0; i < signs.size(); ++i) {
            if (signs.get(i).equals("+")) {
                signs.set(i, "-");
            } else if (signs.get(i).equals("-")) {
                signs.set(i, "+");
            }
        }
        return (T) this;
    }
    protected String process(String repr) {
        repr = repr.trim();
        if (repr.charAt(0) == '+') {
            return repr.substring(1, repr.length());
        }
        return "(" + repr + ")";
    }
    @NotNull
    protected String toStringBase() {
        @NotNull ResizableCharBuffer function = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            function.append(" " + signs.get(j) + " " + terms.get(i));
        }
        return process(function.toString()).trim();
    }
    public Complex getDegree() {
        @NotNull Complex degree = new Complex(Complex.ZERO);
        for (@NotNull E term : terms) {
            Complex vardeg = term.getDegree();
            if (vardeg.modulus() > degree.modulus()) {
                degree = new Complex(vardeg);
            }
        }
        return degree;
    }
    protected boolean useEx() {
        return !(multipliers.isEmpty() || denominators.isEmpty());
    }
    public String derivative(int order) {
        if (useEx()) {
            switch (order) {
                case 0:
                    return toString();
                case 1: {
                    List<List<String>> mDerivative = multiplyListDerivative(createTerms(multipliers.size()));
                    CharBuffer repr = new ResizableCharBuffer();
                    repr.append("(");
                    for (int i = 0; i < mDerivative.size(); ++i) {
                        repr.append("(").append(joinTerms(mDerivative.get(i), "*", true)).append(")");
                        if (i < mDerivative.size() - 1) {
                            repr.append("+");
                        }
                    }
                    repr.append(")");
                    String mDerived = replaceDerivatives(repr.toString(), multipliers, order);
                    if (denominators.isEmpty()) {
                        return mDerived;
                    } else {
                        List<List<String>> dDerivative = multiplyListDerivative(createTerms(multipliers.size()));
                        CharBuffer dRepr = new ResizableCharBuffer();
                        dRepr.append("(");
                        for (int i = 0; i < dDerivative.size(); ++i) {
                            dRepr.append("(").append(joinTerms(dDerivative.get(i), "*", true)).append(")");
                            if (i < dDerivative.size() - 1) {
                                dRepr.append("+");
                            }
                        }
                        dRepr.append(")");
                        String dDerived = replaceDerivatives(dRepr.toString(), multipliers, order);
                        final String[][] items = {
                                {"$u", mDerived},
                                {"u", itemString(multipliers)},
                                {"$v", dDerived},
                                {"v", itemString(denominators)}
                        };
                        return format(DIVISION_DERIVATIVE_1, items);
                    }
                }
                case 2: {
                    List<List<String>> mDerivative = multiplyListDerivative(createTerms(multipliers.size()));
                    CharBuffer repr = new ResizableCharBuffer();
                    repr.append("(");
                    for (int i = 0; i < mDerivative.size(); ++i) {
                        repr.append("(").append(joinTerms(mDerivative.get(i), "*", true)).append(")");
                        if (i < mDerivative.size() - 1) {
                            repr.append("+");
                        }
                    }
                    repr.append(")");
                    String mDerived = replaceDerivatives(repr.toString(), multipliers, 1);
                    List<List<List<String>>> mDerivative2 = new ArrayList<>();
                    for (List<String> unit : mDerivative) {
                        mDerivative2.add(multiplyListDerivative(unit));
                    }
                    CharBuffer repr2 = new ResizableCharBuffer();
                    organizeDerivative(mDerivative2, repr2);
                    String mDerived2 = replaceDerivatives(repr2.toString(), multipliers, 2);
                    if (denominators.isEmpty()) {
                        return mDerived2;
                    } else {
                        List<List<String>> dDerivative = multiplyListDerivative(createTerms(multipliers.size()));
                        CharBuffer dRepr = new ResizableCharBuffer();
                        dRepr.append("(");
                        for (int i = 0; i < dDerivative.size(); ++i) {
                            dRepr.append("(").append(joinTerms(dDerivative.get(i), "*", true)).append(")");
                            if (i < dDerivative.size() - 1) {
                                dRepr.append("+");
                            }
                        }
                        dRepr.append(")");
                        String dDerived = replaceDerivatives(dRepr.toString(), multipliers, 1);
                        List<List<List<String>>> dDerivative2 = new ArrayList<>();
                        for (List<String> unit : dDerivative) {
                            dDerivative2.add(multiplyListDerivative(unit));
                        }
                        CharBuffer dRepr2 = new ResizableCharBuffer();
                        organizeDerivative(dDerivative2, dRepr2);
                        String dDerived2 = replaceDerivatives(dRepr2.toString(), multipliers, 2);
                        final String[][] items = {
                                {"$$u", mDerived2},
                                {"$u", mDerived},
                                {"u", itemString(multipliers)},
                                {"$$v", dDerived2},
                                {"$v", dDerived},
                                {"v", itemString(denominators)}
                        };
                        return format(DIVISION_DERIVATIVE_2, items);
                    }
                }
                default:
                    throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
            }
        }
        return derivativeBase(order);
    }
    private void organizeDerivative(List<List<List<String>>> mDerivative2, CharBuffer repr2) {
        repr2.append("(");
        for (int i = 0; i < mDerivative2.size(); ++i) {
            repr2.append("(");
            for (int j = 0; j < mDerivative2.get(i).size(); ++j) {
                repr2.append("(").append(joinTerms(mDerivative2.get(i).get(j), "*", true)).append(")");
                if (j < mDerivative2.get(i).size() - 1) {
                    repr2.append("+");
                }
            }
            repr2.append(")");
            if (i < mDerivative2.size() - 1) {
                repr2.append("+");
            }
        }
        repr2.append(")");
    }
    private String replaceDerivatives(String toReplace, List<T> items, int order) {
        for (int i = 0; i < items.size(); ++i) {
            for (int j = order; j >= 0; --j) {
                toReplace = replace(toReplace, createDerivativeSymbol(j) + i, items.get(i).derivative(j));
            }
        }
        return toReplace;
    }
    private String createDerivativeSymbol(int order) {
        return createRepeat(DERIVATIVE_SYMBOL, order);
    }
    private List<String> createTerms(int size) {
        List<String> terms = new ArrayList<>(size);
        for (int i = 0; i < terms.size(); ++i) {
            terms.set(i, "" + i);
        }
        return terms;
    }
    private List<List<String>> multiplyListDerivative(List<String> terms) {
        List<List<String>> derivative = new ArrayList<>(terms.size());
        for (int i = 0; i < terms.size(); ++i) {
            List<String> termsBak = new ArrayList<>(terms.size());
            termsBak.addAll(terms);
            termsBak.set(i, DERIVATIVE_SYMBOL + termsBak.get(i));
            derivative.set(i, termsBak);
        }
        return derivative;
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
        return useEx() ? new ResizableCharBuffer().append("((").append(toStringBase()).append(joinTerms(multipliers, "*", false)).append(")/").append(itemString(denominators)).toString() : toStringBase();
    }
    private String itemString(List<T> items) {
        return "(" + joinTerms(items, "*", true) + ")";
    }
    private <V> String joinTerms(List<V> terms, String joiner, boolean hasNoPreceding) {
        ResizableCharBuffer repr = new ResizableCharBuffer(terms.size() * joiner.length() * (STRING_PREFIX_SIZE - 1) * terms.get(0).toString().length());
        for (V term : terms) {
            repr.append(")" + joiner + "(" + term + ")");
        }
        if (hasNoPreceding) {
            return repr.toString().substring(joiner.length() + (OPTIONAL_PREFIX_SIZE - 1), repr.length());//trim leading joiner
        }
        return repr.toString();
    }
}