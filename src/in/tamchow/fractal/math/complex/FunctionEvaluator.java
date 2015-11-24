package in.tamchow.fractal.math.complex;

import in.tamchow.fractal.math.symbolics.Polynomial;

import java.util.StringTokenizer;

/**
 * Implements an iterative parser for functions described in ComplexOperations, making heavy use of string replacement;
 */
public class FunctionEvaluator {
    private String[][] constdec;
    private String z_value;
    private String variableCode;

    public FunctionEvaluator(String variable, String variableCode, String[][] varconst) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
    }

    public FunctionEvaluator(String variableCode, String[][] varconst) {
        setConstdec(varconst);
        setVariableCode(variableCode);
    }
    public String getVariableCode() {
        return variableCode;
    }

    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }

    public double getDegree(String function) {
        double degree = 0;
        int idx = 0, varidx = 0;
        if (function.contains(variableCode) && (!function.contains("^"))) {
            degree = 1;
        }
        while (function.indexOf('^', idx) != -1) {
            varidx = function.indexOf(variableCode, varidx) + 1;
            idx = function.indexOf('^', varidx) + 1;
            double nextDegree = new Complex(function.substring(idx + 1, function.indexOf(' ', idx + 1))).modulus();
            degree = (nextDegree > degree) ? nextDegree : degree;
        }
        return degree;
    }

    public double getDegree(Polynomial polynomial) {
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

    public Complex evaluate(String expr) {
        String subexpr = substitute(expr, false);
        Complex ztmp;
        new Complex(z_value);
        int flag = 0;
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

    protected String limitedEvaluate(String expr, int depth) {
        String subexpr = substitute(expr, true);
        Complex ztmp;
        new Complex(z_value);
        int flag = 0, ctr = 0;
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
        String expr = "";
        if (subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1) {
            expr = subexpr;
        } else {
            expr = subexpr.substring(subexpr.lastIndexOf('(') + 1, subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1));
        }
        StringTokenizer tokens = new StringTokenizer(expr, " ", false);
        String[] mod = new String[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            mod[i] = tokens.nextToken();
            i++;
        }
        return mod;
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

    private String substitute(String expr, boolean isSymbolic) {
        StringTokenizer tokens = new StringTokenizer(expr, " ", false);
        String[] mod = new String[tokens.countTokens()];
        String sub = "";
        int i = 0;
        while (tokens.hasMoreTokens()) {
            mod[i] = tokens.nextToken();
            if (mod[i].equals(variableCode) && (!isSymbolic)) {
                mod[i] = "" + z_value;
            } else if (getConstant(mod[i]) != null) {
                mod[i] = getConstant(mod[i]);
            }
            i++;
        }
        for (String aMod : mod) {
            sub += aMod + " ";
        }
        return sub.trim();
    }
}