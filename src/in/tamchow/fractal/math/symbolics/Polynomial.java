package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.complex.FunctionEvaluator;

import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial extends Operable<Polynomial, Polynomial.Term> {
    private String[][] constdec;
    private String z_value;
    private String variableCode, oldvariablecode;
    public Polynomial(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        super();
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
    }
    public Polynomial() {
        super();
    }
    @NotNull
    public static Polynomial fromString(@NotNull String polynomial) {
        @NotNull Polynomial poly = new Polynomial();
        @NotNull String[] tokens = split(polynomial, ",");
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
    @Override
    public String derivative(int order) {
        switch (order) {
            case 1:
                return derivative().toString();
            case 2:
                return derivative().derivative().toString();
            default:
                throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        }
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
    @Override
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
        public String derivative(int order) {
            switch (order) {
                case 1:
                    return derivative().toString();
                case 2:
                    return derivative().derivative().toString();
                default:
                    throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
            }
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
                deriv = new Term(this.coefficient + " * " + this.exponent, this.exponent + " - " + ONE, this.variable);
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
            return "( ( " + coefficient + " ) * " + "( " + variable + " ^ " + "( " + exponent + " ) ) )";
        }
    }
}