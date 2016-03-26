package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.StringManipulator;
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
        /*term.replace(":^:",":");
        term.replace(":*:",":");*/
        String[] parts = StringManipulator.split(term, ":");
        if (parts.length == 0) {
            return new Term("0", "0", "0");
        }
        if (parts.length == 1) {
            return new Term(term.trim());
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
        if (this.coefficient == null || this.coefficient.equals("")) {
            setCoefficient("1");
        }
    }
    public String getExponent() {
        return exponent;
    }
    public void setExponent(String exponent) {
        this.exponent = exponent;
        if (this.exponent == null || this.exponent.equals("0")) {
            setConstant(true);
            constval = this.coefficient;
        }
    }
    public Term derivative() {
        Term deriv = new Term("0");
        if (!isConstant()) {
            deriv = new Term(this.coefficient + " * " + this.exponent, this.exponent + " - 1", this.variable);
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