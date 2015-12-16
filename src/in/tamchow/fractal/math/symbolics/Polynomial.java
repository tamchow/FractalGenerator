package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;

import java.util.ArrayList;
/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial {
    ArrayList<Term> terms;
    ArrayList<String> signs;
    String[][] constdec;
    String z_value;
    String variableCode;
    public Polynomial(String variable, String variableCode, String[][] varconst) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
    }
    public Polynomial() {
        terms = new ArrayList<>();
        signs = new ArrayList<>();
    }
    public static Polynomial fromString(String polynomial) {
        Polynomial poly = new Polynomial(); String[] tokens = polynomial.split(";");
        for (String token : tokens) {
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
    public String getZ_value() {
        return z_value;
    }
    public void setZ_value(String z_value) {
        this.z_value = z_value;
    }
    public String[][] getConstdec() {
        return constdec;
    }
    public void setConstdec(String[][] constdec) {
        this.constdec = new String[constdec.length][constdec[0].length];
        for (int i = 0; i < this.constdec.length; i++) {
            System.arraycopy(constdec[i], 0, this.constdec[i], 0, this.constdec[i].length);
        }
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
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
        } deriv.setConstdec(constdec); deriv.setVariableCode(variableCode); deriv.setZ_value(z_value);
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
            Complex vardeg;
            try {
                vardeg = new Complex(term.exponent);
            } catch (IllegalArgumentException iae) {
                if (!term.isConstant()) {
                    FunctionEvaluator fe = new FunctionEvaluator(variableCode, constdec, false);
                    vardeg = fe.evaluate(term.exponent, true);
                } else {
                    vardeg = new Complex(Complex.ZERO);
                }
            }
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