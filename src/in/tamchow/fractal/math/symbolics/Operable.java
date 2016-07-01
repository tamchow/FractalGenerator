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
public abstract class Operable<T extends Operable, E extends Derivable> extends Derivable {
    private static final int STRING_PREFIX_SIZE = 3;
    private static int timesDivided = 0;
    protected ArrayList<Operable<T, E>> multipliers, denominators;
    protected ArrayList<E> terms;
    protected ArrayList<String> signs;
    protected Operable() {
        multipliers = new ArrayList<>();
        denominators = new ArrayList<>();
        terms = new ArrayList<>();
        signs = new ArrayList<>();
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
    public Operable<T, E> add(Operable<T, E> other) {
        terms.addAll(other.terms);
        signs.addAll(other.signs);
        return this;
    }
    public Operable<T, E> multiply(Operable<T, E> other) {
        multipliers.add(other);
        return this;
    }
    public Operable<T, E> divide(Operable<T, E> other) {
        ++timesDivided;
        if (timesDivided % 2 == 0) {
            multipliers.add(other);
            multipliers.addAll(other.multipliers);
            denominators.addAll(other.denominators);
        } else {
            denominators.add(other);
            multipliers.addAll(other.denominators);
            denominators.addAll(other.multipliers);
        }
        return this;
    }
    public Operable<T, E> subtract(Operable<T, E> other) {
        other.negate();
        add(other);
        return this;
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
    public String toStringBase() {
        @NotNull ResizableCharBuffer function = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            function.append(" " + signs.get(j) + " " + terms.get(i));
        }
        return process(function.toString()).trim();
    }
    public abstract Complex getDegree();
    public boolean useEx() {
        return !(multipliers.isEmpty() || denominators.isEmpty());
    }
    public String derivative(int order) {
        switch (order) {
            case 0:
                return toString();
            case 1:
                //TODO:Implement
                return derivativeBase(1);
            case 2:
                //TODO:Implement
                return derivativeBase(2);
            default:
                throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        }
    }
    public String firstDerivative() {
        return useEx() ? derivative(1).trim() : firstDerivativeBase();
    }
    public String secondDerivative() {
        return useEx() ? derivative(2).trim() : secondDerivativeBase();
    }
    @NotNull
    @Override
    public String toString() {
        return useEx() ? "( " + toStringBase() + multiplyTerms(multipliers, true) + " ) / ( " + multiplyTerms(denominators, false) + " )" : toStringBase();
    }
    private String multiplyTerms(List<Operable<T, E>> terms, boolean hasPreceding) {
        ResizableCharBuffer repr = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (Operable<T, E> term : terms) {
            repr.append(" * " + term);
        }
        if (!hasPreceding) {
            return repr.toString().substring(3, repr.length());//trim leading multiply
        }
        return repr.toString();
    }
}