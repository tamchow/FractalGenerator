package in.tamchow.fractal.math.symbolics;

import in.tamchow.fractal.math.complex.Complex;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial {
    ArrayList<Term> terms;
    ArrayList<String> signs;

    public Polynomial() {
        terms = new ArrayList<>();
        signs = new ArrayList<>();
    }

    public static Polynomial fromString(String polynomial) {
        Polynomial poly = new Polynomial();
        StringTokenizer tokenizer = new StringTokenizer(polynomial.trim(), ";", false);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("+") || token.equals("-")) {
                poly.signs.add(token.trim());
            } else {
                poly.terms.add(Term.fromString(token.trim()));
            }
        }
        if (poly.signs.size() == poly.terms.size() - 1 || (!poly.signs.get(0).equals("-"))) {
            poly.signs.add(0, "+");
        }
        return poly;
    }

    public ArrayList<String> getSigns() {
        return signs;
    }

    public void setSigns(ArrayList<String> signs) {
        this.signs.clear();
        this.signs.addAll(signs);
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public void setTerms(ArrayList<Term> terms) {
        this.terms.clear();
        this.terms.addAll(terms);
    }

    public Polynomial derivative() {
        Polynomial deriv = new Polynomial();
        deriv.setSigns(this.signs);
        for (int i = 0; i < terms.size(); i++) {
            deriv.terms.add(terms.get(i).derivative());
        }
        return deriv;
    }

    public int countVariableTerms() {
        int ctr = 0;
        for (Term term : terms) {
            if (!term.isConstant()) {
                ctr++;
            }
        }
        return ctr;
    }

    public double getDegree() {
        Complex degree = new Complex(Complex.ZERO);
        for (Term term : terms) {
            Complex vardeg = new Complex(term.exponent);
            if (vardeg.compareTo(degree) > 0) {
                degree = new Complex(vardeg);
            }
        }
        return degree.modulus();
    }

    public int countConstantTerms() {
        int ctr = 0;
        for (Term term : terms) {
            if (term.isConstant()) {
                ctr++;
            }
        }
        return ctr;
    }
    public String toString() {
        String polynomial = "";
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            polynomial += " " + signs.get(j) + " " + terms.get(i);
        }
        if (polynomial.trim().charAt(0) == '+') {
            return polynomial.trim().substring(1, polynomial.trim().length());
        }
        return polynomial.trim();
    }
}