package in.tamchow.fractal.math.complex;
import java.io.Serializable;
/**
 * Represents a Complex Number as 2 doubles or in cis arg form. Provides utility functions.
 */
public class Complex extends Number implements Serializable, Comparable<Complex> {
    public static final Complex i = new Complex(0, 1), ZERO = new Complex(0, 0), ONE = new Complex(1, 0);
    private double a, ib;
    private int precision;
    public Complex(Complex old) {
        this.a = old.real();
        this.ib = old.imaginary();
        precision = old.getPrecision();
    }
    public int getPrecision() {
        return precision;
    }
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    public double real() {
        return a;
    }
    public double imaginary() {
        return ib;
    }
    public Complex(double a, double ib) {
        this.a = a;
        this.ib = ib;
        precision = 25;
    }
    public Complex(double arg) {
        precision = 25;
        Complex value = ComplexOperations.exponent(ComplexOperations.multiply(Complex.i, new Complex(arg + "")));
        this.a = value.real(); this.ib = value.imaginary();
    }
    public Complex(String complex) {
        try {
            if (complex.lastIndexOf('i') == -1) {
                a = Double.parseDouble(complex);
                ib = 0.0;
            } else if ((!complex.contains(",")) && complex.lastIndexOf("i") > 0) {
                a = 0.0;
                ib = Double.parseDouble(complex.substring(0, complex.length()));
            } else {
                String a = complex.substring(0, complex.indexOf(","));
                String ib = complex.substring(complex.indexOf(",") + 1, complex.lastIndexOf("i"));
                if (a.startsWith("+")) {
                    a = a.substring(1, a.length());
                }
                if (ib.startsWith("+")) {
                    ib = ib.substring(1, ib.length());
                }
                this.a = Double.parseDouble(a);
                this.ib = Double.parseDouble(ib);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Input Format Error", e);
        }
        precision = 25;
    }
    public int compareTo(Complex complex) {
        return (int) (this.modulus() - complex.modulus());
    }
    public double modulus() {
        return Math.sqrt((a * a) + (ib * ib));
    }
    public boolean equals(Object complex) {
        if (complex instanceof Complex) {
            if (((Complex) complex).real() == a && ((Complex) complex).imaginary() == ib) {
                return true;
            }
        }
        return false;
    }
    public String toString() {
        //round();
        if (ib < 0) {
            return a + ",-" + (-ib) + "i";
        } else return a + ",+" + ib + "i";
    }
    public double arg() {
        if (a > 0) return Math.atan((ib / a));
        else if (a < 0 && ib >= 0) return Math.atan((ib / a)) + Math.PI;
        else if (a < 0 && ib < 0) return Math.atan((ib / a)) - Math.PI;
        else if (a == 0 && ib > 0) return Math.PI / 2;
        else if (a == 0 && ib < 0) return -Math.PI / 2;
        else return Double.NaN;
    }
    public Complex conjugate() {
        return new Complex(this.a, -this.ib);
    }
    public Complex inverse() {
        double c = a * a + ib * ib;
        return new Complex((a / c), (-(ib / c)));
    }
    @Override
    public int intValue() {
        return (int) modulus();
    }
    @Override
    public long longValue() {
        return (long) modulus();
    }
    @Override
    public float floatValue() {
        return (float) modulus();
    }
    @Override
    public double doubleValue() {
        return modulus();
    }
}