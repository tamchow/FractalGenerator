package in.tamchow.fractal.math.complex;
import java.io.Serializable;
/**
 * Represents a Complex Number as 2 doubles or in cis arg form. Provides utility functions.
 */
public final class Complex extends Number implements Serializable, Comparable<Complex> {
    public static final Complex i = new Complex(0, 1), ZERO = new Complex(0, 0), ONE = new Complex(1, 0), E = new Complex(Math.E, 0), PI = new Complex(Math.PI, 0);
    private static final String DECIMAL_REGEX = "^[+-]?([0-9]*\\.?[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?$";
    private double a, ib;
    public Complex(Complex old) {initComplex(old.real(), old.imaginary(), false);}

    public Complex() {
    }

    public Complex(double a, double ib) {
        initComplex(a, ib, false);
    }

    public Complex(double a) {
        initComplex(a, 0, false);
    }

    public Complex(String complex) {
        /** Not using explicit input validation for performance reasons.
         if(!isInCorrectFormat(complex)){
         throw new NumberFormatException("Illegal Format for Input "+complex);
         }*/
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
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Illegal Format for Input " + e.getLocalizedMessage());
        }
    }

    public void initComplex(double a, double ib, boolean cis) {
        if (cis) {
            Complex value = ComplexOperations.multiply(new Complex(a), ComplexOperations.exponent(ComplexOperations.multiply(Complex.i, new Complex(ib))));
            this.a = value.real(); this.ib = value.imaginary();
        } else {this.a = a; this.ib = ib;}
    }

    public double real() {
        return a;
    }

    public double imaginary() {
        return ib;
    }

    private boolean isInCorrectFormat(String complex) {
        if (complex.length() <= 0) return false;
        if (complex.contains(",")) {
            String[] parts = complex.split(",");
            return parts[0].matches(DECIMAL_REGEX) && parts[1].substring(0, parts[1].length() - 1)/*trim the 'i'*/.matches(DECIMAL_REGEX);
        } else return complex.matches(DECIMAL_REGEX);
    }

    @Override
    public int compareTo(Complex complex) {
        return (int) (this.modulus() - complex.modulus());
    }
    public double modulus() {
        return Math.sqrt((a * a) + (ib * ib));
    }
    @Override
    public boolean equals(Object complex) {
        if (complex instanceof Complex) {
            if (((Complex) complex).real() == a && ((Complex) complex).imaginary() == ib) {return true;}
        } return false;
    }
    @Override
    public String toString() {if (ib < 0) {return a + ",-" + (-ib) + "i";} else return a + ",+" + ib + "i";}
    public double arg() {
        return Math.atan2(ib, a); //return (arg < 0) ? arg + 2 * Math.PI : arg;
        /*if(ib!=0){return 2*Math.atan((modulus()-a)/ib);}
        else if(a>0&&ib==0){return 0;}
        else if(a<0&&ib==0){return Math.PI;}
        else{return Double.NaN;}*/
    }
    public Complex conjugate() {return new Complex(this.a, -this.ib);}
    public Complex inverse() {double c = a * a + ib * ib; return new Complex((a / c), (-(ib / c)));}
    @Override
    public int intValue() {return (int) modulus();}
    @Override
    public long longValue() {return (long) modulus();}
    @Override
    public float floatValue() {return (float) modulus();}
    @Override
    public double doubleValue() {return modulus();}
}