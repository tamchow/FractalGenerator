package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Represents a mathematical entity that can be differentiated
 */
public abstract class Derivable implements Serializable, Comparable<Derivable> {
    protected static final String UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE = "Only 1st and 2nd order derivatives are supported";
    protected static final String _0 = String.valueOf(0), _1 = String.valueOf(1), _2 = String.valueOf(2), _3 = String.valueOf(3);
    protected static final String DERIVATIVE_SYMBOL = "$";
    protected abstract String derivativeBase(int order);
    public abstract Complex getDegree();
    //protected String firstDerivativeBase() {return derivativeBase(1).trim();}
    //protected String secondDerivativeBase() {return derivativeBase(2).trim();}
    //public abstract void fromString(String input);
    //public abstract Complex getDegree();
    @Override
    public abstract String toString();
    @Override
    public boolean equals(Object other) {
        return other.getClass() == getClass() && other.toString().equals(toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public int compareTo(Derivable other) {
        return toString().compareTo(other.toString());
    }
}