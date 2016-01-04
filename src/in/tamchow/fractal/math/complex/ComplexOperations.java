package in.tamchow.fractal.math.complex;
/**
 * Provides Utility methods for handling complex numbers
 */
public class ComplexOperations {
    public static Complex subtract(Complex z1, Complex z2) {
        return new Complex(z1.real() - z2.real(), z1.imaginary() - z2.imaginary());
    }
    public static Complex multiply(Complex z1, Complex z2) {
        double a = (z1.real() * z2.real() - z1.imaginary() * z2.imaginary());
        double ib = (z1.real() * z2.imaginary() + z1.imaginary() * z2.real()); return new Complex(a, ib);
    }
    public static Complex exponent(Complex z) {return power(Complex.E, z);}
    public static Complex power(Complex z1, Complex z2) {
        if (z1.equals(Complex.ZERO)) {return Complex.ZERO;}
        double b1 = z2.real() * z1.arg() + 0.5 * z2.imaginary() * Math.log(z1.modulus() * z1.modulus());
        double b2 = Math.pow(z1.modulus() * z1.modulus(), z2.real() / 2) * Math.exp(-z2.imaginary() * z1.arg());
        double a = b2 * Math.cos(b1); double b = b2 * Math.sin(b1); return new Complex(a, b);
    }
    public static Complex add(Complex z1, Complex z2) {
        return new Complex(z1.real() + z2.real(), z1.imaginary() + z2.imaginary());
    }
    public static Complex log(Complex z, Complex base) {return divide(principallog(z), principallog(base));}
    public static Complex divide(Complex z1, Complex z2) {
        double c = z2.real() * z2.real() + z2.imaginary() * z2.imaginary();
        double a = (z1.real() * z2.real() + z1.imaginary() * z2.imaginary()) / c;
        double ib = (z2.real() * z1.imaginary() - z2.imaginary() * z1.real()) / c; return new Complex(a, ib);
    }
    public static Complex principallog(Complex z) {double r = z.modulus(); return new Complex(Math.log(r), z.arg());}
    public static double distance_squared(Complex z1, Complex z2) {
        return Math.sqrt((Math.pow(z1.real() - z2.real(), 2) + Math.pow(z1.imaginary() - z2.imaginary(), 2)));
    }
    public static Complex sin(Complex z) {return new Complex(Math.sin(z.real()) * Math.cosh(z.imaginary()), Math.cos(z.real()) * Math.sinh(z.imaginary()));}
    public static Complex cos(Complex z) {return new Complex(Math.cos(z.real()) * Math.cosh(z.imaginary()), Math.sin(z.real()) * Math.sinh(z.imaginary()));}
    public static Complex sinh(Complex z) {return new Complex(Math.sinh(z.real()) * Math.cos(z.imaginary()), Math.cosh(z.real()) * Math.sin(z.imaginary()));}
    public static Complex cosh(Complex z) {return new Complex(Math.cosh(z.real()) * Math.cos(z.imaginary()), Math.sinh(z.real()) * Math.sin(z.imaginary()));}
}