package in.tamchow.fractal.math.complex;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
import java.util.regex.Pattern;
/**
 * Represents a Complex Number as 2 doubles or in cis arg form. Provides utility FUNCTION_DATA.
 */
public final class Complex extends Number implements Serializable, Comparable<Complex>, Cloneable {
    public static final Complex i = new Complex(0, 1), ZERO = new Complex(0, 0), ONE = new Complex(1, 0), E = new Complex(Math.E, 0), PI = new Complex(Math.PI, 0);
    /**
     * The uber-regex for all your complex-number needs! Comes with extensive error-checking!
     */
    private static final Pattern REGEX = Pattern.compile(
            // Real mantissa
            "^(?<real>(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+))" +
                    // Exponent symbol
                    "(?:(?:(?:(?<=\\d)[Ee])?" +
                    // Real exponent
                    "(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+)))(?<=\\d))?)" +
                    // Grouping separator ","
                    "(?:(?<=\\d),(?=\\d|[+-]|\\.)" +
                    // Imaginary mantissa
                    "(?<imaginary>(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+))" +
                    // Exponent symbol
                    "(?:(?:(?:(?<=\\d)[Ee])?" +
                    // Imaginary Exponent
                    "(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+)))" +
                    // Iota (imaginary unit) symbol
                    "(?<=\\d))?)i)?$|^" +
                    // Pure imaginary number identifier (pure reals are handled by the above)
                    // Imaginary mantissa
                    "(?<pureImaginary>(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+))" +
                    // Exponent symbol
                    "(?:(?:(?:(?<=\\d)[Ee])?" +
                    // Imaginary Exponent
                    "(?:[+-]?(?:\\d*(?:\\.(?=\\d))?\\d+)))" +
                    // Iota (imaginary unit) symbol
                    "(?<=\\d))?)i$"
    );
    private double a, b;
    public Complex(@NotNull Complex old) {
        this(old.real(), old.imaginary(), false);
    }
    @Deprecated
    public Complex() {
        a = 0;
        b = 0;
    }
    public Complex(double a, double b) {
        this(a, b, false);
    }
    public Complex(double a, double b, boolean cis) {
        if (cis) {
            @NotNull Complex value = ComplexOperations.multiply(new Complex(a),
                    ComplexOperations.exponent(ComplexOperations.multiply(Complex.i, new Complex(b))));
            this.a = value.real();
            this.b = value.imaginary();
        } else {
            this.a = a;
            this.b = b;
        }
    }
    public Complex(double a) {
        this(a, 0, false);
    }
    public Complex(@NotNull String complex) {
        /* Not using explicit input validation for performance reasons.
         if(!isInCorrectFormat(complex)){
         throw new NumberFormatException("Illegal Format for Input "+complex);
         }*/
        try {
            if (complex.lastIndexOf('i') == -1) {
                a = Double.parseDouble(complex);
                b = 0.0;
            } else if ((!complex.contains(",")) && complex.lastIndexOf("i") > 0) {
                a = 0.0;
                b = Double.parseDouble(complex.substring(0, complex.length() - 1));
            } else {
                @NotNull String a = complex.substring(0, complex.indexOf(","));
                @NotNull String ib = complex.substring(complex.indexOf(",") + 1, complex.lastIndexOf("i"));
                if (a.startsWith("+")) {
                    a = a.substring(1, a.length());
                }
                if (ib.startsWith("+")) {
                    ib = ib.substring(1, ib.length());
                }
                this.a = Double.parseDouble(a);
                this.b = Double.parseDouble(ib);
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Illegal Format for Input " + e.getLocalizedMessage());
        }
    }
    @NotNull
    public static Complex random() {
        return random(Complex.ZERO, Complex.ONE);
    }
    @NotNull
    public static Complex random(@NotNull Complex lowerBound, @NotNull Complex upperBound) {
        double modulusRange = upperBound.modulus() - lowerBound.modulus(), argRange = upperBound.arg() - lowerBound.arg();
        double randomModulus = Math.random() * modulusRange, randomArg = Math.random() * argRange;
        return new Complex(randomModulus, randomArg, true);
    }
    @Override
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return new Complex(this);
    }
    @NotNull
    public Complex negated() {
        return new Complex(-a, -b);
    }
    public double real() {
        return a;
    }
    public double imaginary() {
        return b;
    }
    public double cabs() {
        return (a * a) + (b * b);
    }
    private boolean isInCorrectFormat(@NotNull String complex) {
        return REGEX.matcher(complex).matches();
    }
    @Override
    public int compareTo(@NotNull Complex complex) {
        // Lexicographical comparison imposing partial order on the field of complex numbers
        double realComparisonResult = real() - complex.real();
        if (Math.abs(realComparisonResult) < MathUtils.ULP) {
            double imaginaryComparisonResult = imaginary() - complex.imaginary();
            return (Math.abs(imaginaryComparisonResult) < MathUtils.ULP) ? 0 : (imaginaryComparisonResult > 0 ? 1 : -1);
        } else {
            return (realComparisonResult > 0) ? 1 : -1;
        }
    }
    public double modulus() {
        return Math.sqrt(cabs());
    }
    @Override
    public boolean equals(Object complex) {
        if (complex == this) {
            return true;
        }
        if (complex instanceof Complex) {
            Complex other = (Complex) complex;
            // Fuzzy compare for floating point representation
            if (Math.abs(other.real() - a) < MathUtils.ULP && Math.abs(other.imaginary() - b) < MathUtils.ULP) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        //Easy way out
        return toString().hashCode();
    }
    @NotNull
    @Override
    public String toString() {
        if (b < 0) {
            return a + ",-" + (-b) + "i";
        } else return a + ",+" + b + "i";
    }
    /*
    public String toString() {
        if (b < 0) {
            return String.format("%f,-%fi", a, -b);
        } else return String.format("%f,+%fi", a, b);
    }
    */
    public double arg() {
        return Math.atan2(b, a); //return (arg < 0) ? arg + 2 * Math.PI : arg;
        /*if(b!=0){return 2*Math.atan((modulus()-a)/b);}
        else if(a>0&&b==0){return 0;}
        else if(a<0&&b==0){return Math.PI;}
        else{return Double.NaN;}*/
    }
    @NotNull
    public Complex conjugate() {
        return new Complex(this.a, -this.b);
    }
    @NotNull
    public Complex inverse() {
        double c = a * a + b * b;
        return new Complex((a / c), (-(b / c)));
    }
    @Override
    public int intValue() {
        return Math.round((float) modulus());
    }
    @Override
    public long longValue() {
        return Math.round(modulus());
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