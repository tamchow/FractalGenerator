package in.tamchow.fractal.math.complex;
import in.tamchow.fractal.math.symbolics.Polynomial;
import in.tamchow.fractal.misc.StringManipulator;
/**
 * Implements an iterative parser for functions described in ComplexOperations, making heavy use of string replacement;
 */
public class FunctionEvaluator {
    private String[][] constdec;
    private String z_value;
    private String variableCode;
    private boolean hasBeenSubstituted;
    private boolean advancedDegree;
    public FunctionEvaluator(String variable, String variableCode, String[][] varconst, boolean advancedDegree) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    public FunctionEvaluator(String variableCode, String[][] varconst, boolean advancedDegree) {
        setConstdec(varconst);
        setVariableCode(variableCode);
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    public boolean isAdvancedDegree() {
        return advancedDegree;
    }
    public void setAdvancedDegree(boolean advancedDegree) {
        this.advancedDegree = advancedDegree;
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    public Complex getDegree(String function) {
        Complex degree = new Complex(Complex.ZERO); if (function.contains("exp")) {
            String function2 = function.replace(function.substring(function.indexOf("exp"), function.indexOf(')', function.indexOf("exp")) + 1), "");
            return getDegree(function2);
        } if (function.contains("log")) {
            String function2 = function.replace(function.substring(function.indexOf("log"), function.indexOf(')', function.indexOf("log")) + 1), "");
            return getDegree(function2);
        }
        if ((function.contains("*") || function.contains("/")) && advancedDegree) {
            for (int i = 0; i < function.length(); i++) {
                if (function.charAt(i) == '*' || function.charAt(i) == '/') {
                    Complex dl = getDegree(function.substring(StringManipulator.indexOfBackwards(function, i, '('), StringManipulator.indexOfBackwards(function, i, ')') + 1));
                    Complex dr = getDegree(function.substring(function.indexOf('(', i), function.indexOf(')', i) + 1));
                    Complex tmpdegree = new Complex(Complex.ZERO);
                    if (function.charAt(i) == '*') {
                        tmpdegree = ComplexOperations.add(dl, dr);
                    } else if (function.charAt(i) == '/') {
                        tmpdegree = ComplexOperations.subtract(dl, dr);
                    }
                    String function2 = function.replace(function.substring(StringManipulator.indexOfBackwards(function, i, '('), function.indexOf(')', i) + 1), "z ^ " + tmpdegree);
                    return getDegree(function2);
                }
            }
        }
        if (!hasBeenSubstituted) {
            hasBeenSubstituted = true;
            return getDegree(substitute(function, true));
        }
        int idx = 0, varidx = 0;
        if ((function.contains(variableCode) && (!function.contains("^")))) {
            degree = new Complex(Complex.ONE);
        }
        while (function.indexOf('^', idx) != -1) {
            varidx = function.indexOf(variableCode, varidx) + 1;
            idx = function.indexOf('^', varidx) + 1;
            Complex nextDegree = new Complex(function.substring(idx + 1, function.indexOf(' ', idx + 1)));
            degree = (nextDegree.modulus() > degree.modulus()) ? nextDegree : degree;
        }
        return degree;
    }
    public Complex getDegree(Polynomial polynomial) {
        return getDegree(limitedEvaluate(polynomial + "", polynomial.countVariableTerms() * 2 + polynomial.countConstantTerms()));
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
        this.constdec = constdec;
    }
    public Complex evaluate(String expr, boolean isSymbolic) {
        String subexpr = substitute(expr, isSymbolic);
        Complex ztmp; int flag = 0;
        do {
            ztmp = eval(process(subexpr));
            if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                subexpr = subexpr.replace(subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), "" + ztmp);
            } else {
                ++flag;
            }
        } while (flag <= 1);
        return ztmp;
    }
    private Complex eval(String[] processed) {
        Complex ztmp = new Complex(0, 0);
        if (processed.length == 1) {
            return new Complex(processed[0]);
        }
        for (int i = 0; i < processed.length - 1; i++) {
            try {
                switch (processed[i]) {
                    case "+":
                        ztmp = ComplexOperations.add(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "-":
                        ztmp = ComplexOperations.subtract(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "*":
                        ztmp = ComplexOperations.multiply(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "/":
                        ztmp = ComplexOperations.divide(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "^":
                        ztmp = ComplexOperations.power(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "exp":
                        ztmp = ComplexOperations.exponent(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "log":
                        ztmp = ComplexOperations.principallog(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "inv":
                        ztmp = ztmp.inverse();
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "conj":
                        ztmp = ztmp.conjugate();
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    default:
                        ztmp = new Complex(processed[i]);
                }
                //ztmp.round(factor);
            } catch (ArrayIndexOutOfBoundsException ae) {
                throw new IllegalArgumentException("Function Input Error", ae);
            }
        }
        return ztmp;
    }
    private String[] process(String subexpr) {
        String expr;
        if (subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1) {
            expr = subexpr;
        } else {
            expr = subexpr.substring(subexpr.lastIndexOf('(') + 1, subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1));
        }
        expr = expr.trim();
        return expr.split(" ");
    }
    private String substitute(String expr, boolean isSymbolic) {
        String[] mod = expr.split(" "); String sub = "";
        for (int i = 0; i < mod.length; i++) {
            if (mod[i].equals(variableCode) && (!isSymbolic)) {
                mod[i] = "" + z_value;
            } else if (getConstant(mod[i]) != null) {
                mod[i] = getConstant(mod[i]);
            }
        }
        for (String aMod : mod) {
            sub += aMod + " ";
        }
        return sub.trim();
    }
    private String getConstant(String totry) {
        String val = null;
        for (String[] aConstdec : constdec) {
            if (aConstdec[0].equals(totry)) {
                val = aConstdec[1];
                return val;
            }
        }
        return val;
    }
    protected String limitedEvaluate(String expr, int depth) {
        String subexpr = substitute(expr, true);
        Complex ztmp; int flag = 0, ctr = 0;
        do {
            ztmp = eval(process(subexpr));
            if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                subexpr = subexpr.replace(subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), "" + ztmp);
                ctr++;
            } else {
                ++flag;
            }
        } while (flag <= 1 && ctr <= depth);
        return subexpr;
    }
}