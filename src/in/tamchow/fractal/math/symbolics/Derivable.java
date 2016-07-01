package in.tamchow.fractal.math.symbolics;
import java.io.Serializable;
/**
 * Represents a mathematical entity that can be differentiated
 */
public abstract class Derivable implements Serializable, Comparable<Derivable> {
    protected static final String UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE = "Only 1st and 2nd order derivatives are supported";
    public abstract String derivativeBase(int order);
    public String firstDerivativeBase() {
        return derivativeBase(1).trim();
    }
    public String secondDerivativeBase() {
        return derivativeBase(2).trim();
    }
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