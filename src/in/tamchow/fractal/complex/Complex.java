package in.tamchow.fractal.complex;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Represents a Complex Number as 2 doubles or in cis arg form. Provides utility functions.
 */
public class Complex implements Serializable {
    public static final Complex i = new Complex(0, 1);
    private double a, ib;
    private int precision;

    public Complex(Complex old) {
        this.a = old.real();
        this.ib = old.imaginary();
        precision = old.getPrecision();
    }

    public Complex(double a, double ib) {
        this.a = a;
        this.ib = ib;
        precision = 25;
    }

    public Complex(double arg) {
        a = Math.cos(arg);
        ib = Math.sin(arg);
        precision = 25;
    }

    public Complex(String complex) {
        try {
            if (complex.indexOf('i') == -1) {
                a = Double.parseDouble(complex);
                ib = 0.0;
            } else if ((!complex.contains(",")) && complex.indexOf("i") > 0) {
                a = 0.0;
                ib = Double.parseDouble(complex.substring(0, complex.length()));
            } else {
                String a = complex.substring(0, complex.indexOf(","));
                String ib = complex.substring(complex.indexOf(",") + 1, complex.indexOf("i"));
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

    private int count_char(char c, String str) {
        int ctr = 0;
        for (int i = 0; i < str.length(); i++) {
            if (c == str.charAt(ctr)) {
                ctr++;
            }
        }
        return ctr;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String toString() {
        //round();
        if (ib < 0) {
            return a + ",-" + (-ib) + "i";
        } else return a + ",+" + ib + "i";
    }

    public boolean equals(Object complex) {
        if (complex instanceof Complex) {
            if (((Complex) complex).real() == a && ((Complex) complex).imaginary() == ib) {
                return true;
            }
        }
        return false;
    }

    public void round() {
        try {
            DecimalFormat df = new DecimalFormat("00.000000");
            df.setRoundingMode(RoundingMode.HALF_UP);
            System.out.println(df.format(ib));
            ib = df.parse(df.format(ib)).doubleValue();
            System.out.println(df.format(a));
            a = df.parse(df.format(a)).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public double modulus() {
        return Math.sqrt((a * a) + (ib * ib));
    }

    public double real() {
        return a;
    }

    public double imaginary() {
        return ib;
    }

    public Complex inverse() {
        double c = a * a + ib * ib;
        return new Complex((a / c), (-(ib / c)));
    }
}