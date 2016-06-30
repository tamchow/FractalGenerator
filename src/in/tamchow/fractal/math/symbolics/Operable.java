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
 * It is the user's responsibility to check {@link Operable#useEx()}
 * and call the {@code *Ex()} instead of the normal variant if it returns {@code true}
 * </p>
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
    public void add(Operable<T, E> other) {
        terms.addAll(other.terms);
        signs.addAll(other.signs);
    }
    public void multiply(Operable<T, E> other) {
        multipliers.add(other);
    }
    public void divide(Operable<T, E> other) {
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
    }
    public void subtract(Operable<T, E> other) {
        other.negate();
        add(other);
    }
    public void negate() {
        for (int i = 0; i < signs.size(); ++i) {
            if (signs.get(i).equals("+")) {
                signs.set(i, "-");
            } else if (signs.get(i).equals("-")) {
                signs.set(i, "+");
            }
        }
    }
    protected String process(String repr) {
        repr = repr.trim();
        if (repr.charAt(0) == '+') {
            return repr.substring(1, repr.length());
        }
        return "( " + repr + " )";
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull ResizableCharBuffer function = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            function.append(" " + signs.get(j) + " " + terms.get(i));
        }
        return process(function.toString());
    }
    public abstract Complex getDegree();
    public boolean useEx() {
        return !(multipliers.isEmpty() || denominators.isEmpty());
    }
    public String derivativeEx(int order) {
        switch (order) {
            case 1:
                //TODO:Implement
                return derivative(1);
            case 2:
                //TODO:Implement
                return derivative(2);
            default:
                throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        }
    }
    public String firstDerivativeEx() {
        if (useEx()) {
            return derivativeEx(1);
        }
        return firstDerivative();
    }
    public String secondDerivativeEx() {
        if (useEx()) {
            return derivativeEx(2);
        }
        return secondDerivative();
    }
    public String toStringEx() {
        if (useEx()) {
            String repr = "( " + toString();
            repr += multiplyTerms(multipliers);
            repr += " / ";
            repr += multiplyTerms(denominators);
            return repr + " )";
        }
        return toString();
    }
    private String multiplyTerms(List<Operable<T, E>> terms) {
        ResizableCharBuffer repr = new ResizableCharBuffer(terms.size() * STRING_PREFIX_SIZE * terms.get(0).toString().length());
        for (Operable<T, E> term : terms) {
            repr.append(" * " + term);
        }
        return repr.toString();
    }
}