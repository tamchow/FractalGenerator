package in.tamchow.fractal.math.symbolics;

/**
 * Holds one term of a polynomial
 */
public class Term {
    public String coefficient;
    public String exponent;
    public String variable;
    public String constval;
    private boolean constant;

    public Term(String coefficient, String exponent, String variable) {
        setVariable(variable);
        setCoefficient(coefficient);
        setExponent(exponent);
    }

    public Term(String constval) {
        setConstval(constval);
        setConstant(true);
    }

    public static Term fromString(String term) {
        term = term.substring(1, term.length() - 1);//remove leading and trailing braces
        String[] parts = term.split(",");
        if (parts.length == 0 || parts.length == 1) {
            Term constant = new Term(term.trim());
        }
        return new Term(parts[0], parts[2], parts[1]);
    }

    public String getConstval() {
        return constval;
    }

    public void setConstval(String constval) {
        this.constval = constval;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
        if (this.coefficient.equals("") || this.coefficient == null) {
            setCoefficient("1");
        }
    }

    public String getExponent() {
        return exponent;
    }

    public void setExponent(String exponent) {
        this.exponent = exponent;
        if (this.exponent.equals("0") || this.exponent == null) {
            setConstant(true);
            constval = this.coefficient;
        }
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    public Term derivative() {
        Term deriv = new Term(this.coefficient + " * " + this.exponent, this.exponent + " - 1", this.variable);
        if (isConstant()) {
            deriv = new Term("0");
        }
        return deriv;
    }

    public String toString() {
        if (constant) {
            return constval;
        }
        return "( ( " + coefficient + " ) * " + "( " + variable + " ^ " + "( " + exponent + " ) ) )";
    }
}
