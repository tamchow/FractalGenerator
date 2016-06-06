package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial implements Serializable, Comparable<Polynomial> {
    private ArrayList<Term> terms;
    private ArrayList<String> signs;
    private String[][] constdec;
    private String z_value;
    private String variableCode, oldvariablecode;
    public Polynomial(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
    }
    public Polynomial() {
        terms = new ArrayList<>();
        signs = new ArrayList<>();
    }
    @NotNull
    public static Polynomial fromString(@NotNull String polynomial) {
        @NotNull Polynomial poly = new Polynomial();
        @NotNull String[] tokens = StringManipulator.split(polynomial, ";");
        for (@NotNull String token : tokens) {
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
    public String getOldvariablecode() {
        return oldvariablecode;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
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
    public void setConstdec(@NotNull String[][] constdec) {
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
    private void setSigns(@NotNull ArrayList<String> signs) {
        this.signs.clear();
        this.signs.addAll(signs);
    }
    public ArrayList<Term> getTerms() {
        return terms;
    }
    public void setTerms(@NotNull ArrayList<Term> terms) {
        this.terms.clear();
        this.terms.addAll(terms);
    }
    @NotNull
    public Polynomial derivative() {
        @NotNull Polynomial deriv = new Polynomial();
        deriv.setSigns(this.signs);
        for (int i = 0; i < terms.size(); i++) {
            deriv.terms.add(terms.get(i).derivative());
        }
        deriv.setConstdec(constdec);
        deriv.setVariableCode(variableCode);
        deriv.setZ_value(z_value);
        return deriv;
    }
    public int countVariableTerms() {
        int ctr = 0;
        for (@NotNull Term term : terms) {
            if (!term.isConstant()) {
                ctr++;
            }
        }
        return ctr;
    }
    @NotNull
    public Complex getDegree() {
        @NotNull Complex degree = new Complex(Complex.ZERO);
        for (@NotNull Term term : terms) {
            Complex vardeg;
            try {
                vardeg = new Complex(term.exponent);
            } catch (IllegalArgumentException iae) {
                if (!term.isConstant()) {
                    @NotNull FunctionEvaluator fe = new FunctionEvaluator(variableCode, oldvariablecode, constdec, false);
                    vardeg = fe.evaluate(term.exponent, true);
                } else {
                    vardeg = new Complex(Complex.ZERO);
                }
            }
            if (vardeg.modulus() > degree.modulus()) {
                degree = new Complex(vardeg);
            }
        }
        return degree;
    }
    public int countConstantTerms() {
        int ctr = 0;
        for (@NotNull Term term : terms) {
            if (term.isConstant()) {
                ctr++;
            }
        }
        return ctr;
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String polynomial = "";
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            polynomial += " " + signs.get(j) + " " + terms.get(i);
        }
        polynomial = polynomial.trim();
        if (polynomial.trim().charAt(0) == '+') {
            return polynomial.substring(1, polynomial.length());
        }
        return polynomial;
    }
    @Override
    public int compareTo(@NotNull Polynomial o) {
        return toString().compareTo(o.toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Polynomial && toString().equals(o.toString());
    }
}