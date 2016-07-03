package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;
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
    public static Polynomial fromString(String polynomial, String variableCode, String oldVariableCode, String[][] consts) {
        return fromString(polynomial, variableCode, oldVariableCode, consts, true);
    }
    public static Polynomial fromString(String polynomial, String variableCode, String oldVariableCode, String[][] consts, boolean deepEvaluate) {
        @NotNull Polynomial poly = new Polynomial();
        if (deepEvaluate) {
            String[] divisors = StringManipulator.split(polynomial, ",/,");
            Polynomial[] dividing = new Polynomial[divisors.length];
            for (int i = 0; i < divisors.length; ++i) {
                String[] multipliers = StringManipulator.split(divisors[i], ",*,");
                Polynomial[] multiplying = new Polynomial[multipliers.length];
                for (int j = 0; j < multipliers.length; ++j) {
                    multiplying[j] = fromString(multipliers[j], variableCode, oldVariableCode, consts, false);
                }
                Polynomial multiplied = multiplying[0];
                for (int j = 1; j < multiplying.length; ++j) {
                    multiplied.multiply(multiplying[j]);
                }
                dividing[i] = multiplied;
            }
            Polynomial divided = dividing[0];
            for (int i = 1; i < dividing.length; ++i) {
                divided.divide(dividing[i]);
            }
            poly = divided;
            return poly;
        }
        @NotNull String[] tokens = split(polynomial, ",");
        poly.terms.ensureCapacity(tokens.length);
        poly.signs.ensureCapacity(tokens.length + 1);
        for (@NotNull String token : tokens) {
            if (token.equals("+") || token.equals("-")) {
                poly.signs.add(token.trim());
            } else {
                Term term = new Term();
                term.fromString(token.trim(), variableCode, oldVariableCode, consts);
                poly.terms.add(term);
            }
        }
        if (poly.signs.size() == poly.terms.size() - 1) {
            poly.signs.add(0, "+");
        }
        poly.setVariableCode(variableCode);
        poly.setVariableCode(oldVariableCode);
        poly.setConsts(consts);
        return poly;
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
        private String exponent, variable, coefficient, constval, variableCode, oldvariablecode;
        private String[][] consts;
        private boolean constant;
        public Term(String coefficient, String exponent, String variable) {
            initTerm(coefficient, exponent, variable);
        }
        public Term(String constval) {
            initTerm(constval);
        }
        public Term() {
            initTerm(_0);
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
        public String[][] getConsts() {
            return consts;
        }
        public void setConsts(@NotNull String[][] constdec) {
            consts = new String[constdec.length][constdec[0].length];
            for (int i = 0; i < this.consts.length; i++) {
                System.arraycopy(constdec[i], 0, consts[i], 0, consts[i].length);
            }
        }
        public String getVariableCode() {
            return variableCode;
        }
        public void setVariableCode(String variableCode) {
            this.variableCode = variableCode;
        }
        public String getOldvariablecode() {
            return oldvariablecode;
        }
        public void setOldvariablecode(String oldvariablecode) {
            this.oldvariablecode = oldvariablecode;
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
        public Complex getDegree() {
            Complex vardeg;
            try {
                vardeg = new Complex(exponent);
            } catch (IllegalArgumentException iae) {
                if (!isConstant()) {
                    @NotNull FunctionEvaluator fe = new FunctionEvaluator(variableCode, oldvariablecode, consts, false);
                    vardeg = fe.evaluate(exponent, true);
                } else {
                    vardeg = new Complex(Complex.ZERO);
                }
            }
            return vardeg;
        }
        @NotNull
        public void fromString(String term, String variableCode, String oldVariableCode, String[][] consts) {
            //term = term.substring(1, term.length() - 1).trim();//remove leading and trailing braces
            /*replace(term,":^:",":");
            replace(term,":*:",":");*/
            @NotNull String[] parts = split(term, ":");
            switch (parts.length) {
                case 0:
                    initTerm(_0);
                    break;
                case 1:
                    initTerm(term.trim());
                    break;
                case 2:
                    throw new IllegalArgumentException("Malformed Polynomial String");
                default:
                    initTerm(parts[0].trim(), parts[2].trim(), parts[1].trim());
            }
            setVariableCode(variableCode);
            setVariableCode(oldVariableCode);
            setConsts(consts);
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
            setVariable(_1);
            this.exponent = _0;
        }
        @NotNull
        private Term derivative() {
            @NotNull Term deriv = new Term(_0);
            if (!isConstant()) {
                deriv = new Term("(" + this.coefficient + ")*(" + this.exponent + ")", "((" + this.exponent + ")-" + _1 + ")", this.variable);
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
            return "((" + coefficient + ")*((" + variable + ")^(" + exponent + ")))";
        }
    }
}