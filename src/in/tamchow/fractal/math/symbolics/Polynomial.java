package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;

import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial extends Operable<Polynomial, Polynomial.Term> {
    public Polynomial(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        super(variable, variableCode, oldvariablecode, varconst);
    }
    public Polynomial() {
        super();
    }
    @NotNull
    public static Polynomial fromString(@NotNull String polynomial) {
        @NotNull Polynomial poly = new Polynomial();
        @NotNull String[] tokens = split(polynomial, ",");
        poly.terms.ensureCapacity(tokens.length);
        poly.signs.ensureCapacity(tokens.length + 1);
        for (@NotNull String token : tokens) {
            if (token.equals("+") || token.equals("-")) {
                poly.signs.add(token.trim());
            } else {
                Term term = new Term();
                term.fromString(token.trim());
                poly.terms.add(term);
            }
        }
        if (poly.signs.size() == poly.terms.size() - 1) {
            poly.signs.add(0, "+");
        }
        return poly;
    }
    public static Polynomial fromString(String poly, String variableCode, String oldVariableCode, String[][] consts) {
        Polynomial polynomial = fromString(poly);
        polynomial.setVariableCode(variableCode);
        polynomial.setVariableCode(oldVariableCode);
        polynomial.setConsts(consts);
        return polynomial;
    }
    @Override
    public String derivativeBase(int order) {
        if (order < 0) {
            throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        }
        Polynomial derived = this;
        for (int i = 0; i < order; ++i) {
            derived = derived.derivative();
        }
        return derived.toString();
    }
    @NotNull
    public Polynomial derivative() {
        @NotNull Polynomial deriv = new Polynomial(z_value, variableCode, oldvariablecode, consts);
        deriv.setSigns(this.signs);
        for (int i = 0; i < terms.size(); i++) {
            deriv.terms.add(terms.get(i).derivative());
        }
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
    @Override
    public Complex getDegree() {
        @NotNull Complex degree = new Complex(Complex.ZERO);
        for (@NotNull Term term : terms) {
            Complex vardeg;
            try {
                vardeg = new Complex(term.exponent);
            } catch (IllegalArgumentException iae) {
                if (!term.isConstant()) {
                    @NotNull FunctionEvaluator fe = new FunctionEvaluator(variableCode, oldvariablecode, consts, false);
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
    protected static class Term extends Derivable {
        private static final String ZERO = String.valueOf(0), ONE = String.valueOf(1);
        private String exponent;
        private String variable;
        private String coefficient;
        private String constval;
        private boolean constant;
        public Term(String coefficient, String exponent, String variable) {
            initTerm(coefficient, exponent, variable);
        }
        public Term(String constval) {
            initTerm(constval);
        }
        public Term() {
            initTerm(ZERO);
        }
        private void initTerm(String constval) {
            setCoefficient(constval);
            makeConstant();
        }
        private void initTerm(String coefficient, String exponent, String variable) {
            setVariable(variable);
            setCoefficient(coefficient);
            setExponent(exponent);
        }
        @Override
        public String derivativeBase(int order) {
            if (order < 0) {
                throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
            }
            Term derived = this;
            for (int i = 0; i < order; ++i) {
                derived = derived.derivative();
            }
            return derived.toString();
        }
        @NotNull
        public void fromString(String term) {
            //term = term.substring(1, term.length() - 1).trim();//remove leading and trailing braces
            /*replace(term,":^:",":");
            replace(term,":*:",":");*/
            @NotNull String[] parts = split(term, ":");
            switch (parts.length) {
                case 0:
                    initTerm(ZERO);
                    break;
                case 1:
                    initTerm(term.trim());
                    break;
                case 2:
                    throw new IllegalArgumentException("Malformed Polynomial String");
                default:
                    initTerm(parts[0].trim(), parts[2].trim(), parts[1].trim());
            }
        }
        private String getConstval() {
            return constval;
        }
        private void setConstval(String constval) {
            this.constval = constval;
        }
        private String getVariable() {
            return variable;
        }
        private void setVariable(String variable) {
            this.variable = variable;
        }
        private String getCoefficient() {
            return coefficient;
        }
        private void setCoefficient(String coefficient) {
            this.coefficient = coefficient;
            if (this.coefficient == null || this.coefficient.isEmpty()) {
                setCoefficient(String.valueOf(1));
            }
        }
        private String getExponent() {
            return exponent;
        }
        private void setExponent(String exponent) {
            this.exponent = exponent;
            if (this.exponent == null) {
                makeConstant();
            } else {
                setConstant(false);
                try {
                    if (Double.valueOf(this.exponent).equals(0.0)) {
                        makeConstant();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        private void makeConstant() {
            setConstant(true);
            setConstval(this.coefficient);
            setVariable(ONE);
            this.exponent = ZERO;
        }
        @NotNull
        private Term derivative() {
            @NotNull Term deriv = new Term(ZERO);
            if (!isConstant()) {
                deriv = new Term(this.coefficient + " * ( " + this.exponent + " )", "( " + this.exponent + " - " + ONE + " )", this.variable);
            }
            return deriv;
        }
        public boolean isConstant() {
            return constant;
        }
        public void setConstant(boolean constant) {
            this.constant = constant;
        }
        @Override
        public String toString() {
            if (constant) {
                return constval;
            }
            return "( ( " + coefficient + " ) * " + "( ( " + variable + " ) ^ ( " + exponent + " ) ) )";
        }
    }
}