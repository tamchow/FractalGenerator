package in.tamchow.fractal.complex;

import java.util.StringTokenizer;

/**
 * Implements an iterative parser for functions described in ComplexOperations, makes heavy use of string replacement;
 */
public class FunctionEvaluator {
    private String[][] constdec;
    private String z_value;

    public FunctionEvaluator(String z_value, String[][] varconst) {
        this.z_value = z_value;
        this.constdec = varconst;
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
        String subexpr = substitute(expr);
        Complex ztmp;
        new Complex(z_value);
        int flag = 0;
        do {
            ztmp = eval(process(subexpr));
            if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                subexpr = subexpr.replace(subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), "" + ztmp);
            }
            if ((subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                ++flag;
            }
        } while (flag <= 1);
        return ztmp;
    }

    private Complex eval(String[] processed) {
        Complex ztmp = new Complex(0, 0);
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

    private String substitute(String expr) {
        StringTokenizer tokens = new StringTokenizer(expr, " ", false);
        String[] mod = new String[tokens.countTokens()];
        String sub = "";
        int i = 0;
        while (tokens.hasMoreTokens()) {
            mod[i] = tokens.nextToken();
            if (mod[i].equals("z")) {
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