package in.tamchow.fractal.math.complex;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Provides Utility methods for handling complex numbers
 */
public final class ComplexOperations {
    @NotNull
    public static Complex subtract(@NotNull final Complex z1, @NotNull final Complex z2) {
        return new Complex(z1.real() - z2.real(), z1.imaginary() - z2.imaginary());
    }
    @NotNull
    public static Complex multiply(@NotNull final Complex z1, @NotNull final Complex z2) {
        double a = (z1.real() * z2.real() - z1.imaginary() * z2.imaginary());
        double ib = (z1.real() * z2.imaginary() + z1.imaginary() * z2.real());
        return new Complex(a, ib);
    }
    @NotNull
    public static Complex exponent(@NotNull final Complex z) {
        double ePowA = Math.exp(z.real());
        return new Complex(ePowA * Math.cos(z.imaginary()), ePowA * Math.sin(z.imaginary()));
    }
    @NotNull
    public static Complex powerOpt(@NotNull final Complex z1, @NotNull final Complex z2) {
        if (z2.equals(Complex.ZERO)) {
            return Complex.ONE;
        } else if (z1.equals(Complex.ZERO)) {
            return Complex.ZERO;
        }
        return exponent(multiply(z2, principallog(z1)));
    }
    @NotNull
    public static Complex power(@NotNull final Complex z1, @NotNull final Complex z2) {
        if (z1.equals(Complex.ZERO)) {
            return Complex.ZERO;
        }
        double b1 = z2.real() * z1.arg() + 0.5 * z2.imaginary() * Math.log(z1.modulus() * z1.modulus());
        double b2 = Math.pow(z1.modulus() * z1.modulus(), z2.real() / 2) * Math.exp(-z2.imaginary() * z1.arg());
        double a = b2 * Math.cos(b1);
        double b = b2 * Math.sin(b1);
        return new Complex(a, b);
    }
    @NotNull
    public static Complex add(@NotNull final Complex z1, @NotNull final Complex z2) {
        return new Complex(z1.real() + z2.real(), z1.imaginary() + z2.imaginary());
    }
    @NotNull
    public static Complex log(@NotNull final Complex z, @NotNull final Complex base) {
        return divide(principallog(z), principallog(base));
    }
    @NotNull
    public static Complex divide(@NotNull final Complex z1, @NotNull final Complex z2) {
        double c = z2.real() * z2.real() + z2.imaginary() * z2.imaginary();
        double a = (z1.real() * z2.real() + z1.imaginary() * z2.imaginary()) / c;
        double ib = (z2.real() * z1.imaginary() - z2.imaginary() * z1.real()) / c;
        return new Complex(a, ib);
    }
    @NotNull
    public static Complex principallog(@NotNull final Complex z) {
        return new Complex(Math.log(z.modulus()), z.arg());
    }
    public static double distance_squared(@NotNull final Complex z1, @NotNull final Complex z2) {
        return (Math.pow(z1.real() - z2.real(), 2) + Math.pow(z1.imaginary() - z2.imaginary(), 2));
    }
    public static double distance(@NotNull final Complex z1, @NotNull final Complex z2) {
        return Math.sqrt(distance_squared(z1, z2));
    }
    @NotNull
    public static Complex cot(@NotNull final Complex z) {
        return divide(Complex.ONE, tan(z));
    }
    @NotNull
    public static Complex tan(@NotNull final Complex z) {
        return divide(sin(z), cos(z));
    }
    @NotNull
    public static Complex sin(@NotNull final Complex z) {
        return new Complex(Math.sin(z.real()) * Math.cosh(z.imaginary()), Math.cos(z.real()) * Math.sinh(z.imaginary()));
    }
    @NotNull
    public static Complex cos(@NotNull final Complex z) {
        return new Complex(Math.cos(z.real()) * Math.cosh(z.imaginary()), -Math.sin(z.real()) * Math.sinh(z.imaginary()));
    }
    @NotNull
    public static Complex coth(@NotNull final Complex z) {
        return divide(Complex.ONE, tanh(z));
    }
    @NotNull
    public static Complex tanh(@NotNull final Complex z) {
        return divide(sinh(z), cosh(z));
    }
    @NotNull
    public static Complex sinh(@NotNull final Complex z) {
        return new Complex(Math.sinh(z.real()) * Math.cos(z.imaginary()), Math.cosh(z.real()) * Math.sin(z.imaginary()));
    }
    @NotNull
    public static Complex cosh(@NotNull final Complex z) {
        return new Complex(Math.cosh(z.real()) * Math.cos(z.imaginary()), Math.sinh(z.real()) * Math.sin(z.imaginary()));
    }
    @NotNull
    public static Complex sec(@NotNull final Complex z) {
        return divide(Complex.ONE, cos(z));
    }
    @NotNull
    public static Complex sech(@NotNull final Complex z) {
        return divide(Complex.ONE, cosh(z));
    }
    @NotNull
    public static Complex cosec(@NotNull final Complex z) {
        return divide(Complex.ONE, sin(z));
    }
    @NotNull
    public static Complex cosech(@NotNull final Complex z) {
        return divide(Complex.ONE, sinh(z));
    }
    @NotNull
    public static Complex flip(@NotNull final Complex z) {
        return new Complex(z.imaginary(), z.real());
    }
}