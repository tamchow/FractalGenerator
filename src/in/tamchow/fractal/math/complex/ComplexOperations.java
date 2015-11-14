package in.tamchow.fractal.math.complex;

/**
 * Provides Utility methods for handling complex numbers
 * TODO:
 * add sin,cos,tan,asin,acos,atan and hyperbolic ones if possible;
 * EDIT:
 * sin,cos,tan and derivatives can be added
 */
public class ComplexOperations {
    private static long factorial(int tofac) {
        long fac = 1;
        if (tofac == 0) return 1;//0!=1
        else {
            while (tofac > 0) {
                fac *= tofac;
                tofac--;
            }
        }
        return fac;
    }

    public static Complex add(Complex z1, Complex z2) {
        return new Complex(z1.real() + z2.real(), z1.imaginary() + z2.imaginary());//z1+z2=(a+c)+i(c+d)
    }

    public static Complex subtract(Complex z1, Complex z2) {
        return new Complex(z1.real() - z2.real(), z1.imaginary() - z2.imaginary());//z1-z2=(a-c)+i(c-d)
    }

    public static Complex multiply(Complex z1, Complex z2) {
        double a = (z1.real() * z2.real() - z1.imaginary() * z2.imaginary());
        double ib = (z1.real() * z2.imaginary() + z1.imaginary() * z2.real());
        return new Complex(a, ib);
    }

    public static Complex divide(Complex z1, Complex z2) {
        double c = z2.real() * z2.real() + z2.imaginary() * z2.imaginary();
        double a = (z1.real() * z2.real() + z1.imaginary() * z2.imaginary()) / c;
        double ib = (z2.real() * z1.imaginary() - z2.imaginary() * z1.real()) / c;
        return new Complex(a, ib);
    }

    public static Complex power(Complex z1, Complex z2) {
        if (z1.real() == 0 && z1.imaginary() == 0) {
            return new Complex(z1);
        }
        double b1 = z2.real() * z1.arg() + 0.5 * z2.imaginary() * Math.log(z1.modulus() * z1.modulus());
        double b2 = Math.pow(z1.modulus() * z1.modulus(), z2.real() / 2) * Math.exp(-z2.imaginary() * z1.arg());
        double a = b2 * Math.cos(b1);
        double b = b2 * Math.sin(b1);
        return new Complex(a, b);
    }

    public static Complex ipower(int n) {
        if ((n % 2) == 0) {
            if (((n / 2) % 2) == 0) {
                return new Complex(1, 0);
            } else return new Complex(-1, 0);
        } else {
            if ((((n - 1) / 2) % 2) == 0) {
                return Complex.i;
            } else return new Complex(0, -1);
        }
    }

    public static Complex exponent(Complex z) {
        Complex ez = new Complex(0, 0);
        for (int i = 0; i <= z.getPrecision(); i++) {
            Complex exp = power(z, new Complex(i, 0));
            double a = exp.real() / factorial(i);
            double b = exp.imaginary() / factorial(i);
            ez = add(ez, new Complex(a, b));
        }
        return ez;
    }

    public static Complex principallog(Complex z) {
        double r = z.modulus();
        return new Complex(Math.log(r), z.arg());
    }
}