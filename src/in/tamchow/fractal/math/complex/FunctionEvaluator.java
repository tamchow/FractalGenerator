package in.tamchow.fractal.math.complex;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.math.Comparator;
import in.tamchow.fractal.math.symbolics.Polynomial;

import static in.tamchow.fractal.helpers.strings.StringManipulator.*;
/**
 * Implements an iterative evaluator for FUNCTION_DATA described in ComplexOperations,
 * making heavy use of string replacement;
 */
public class FunctionEvaluator {
    private static final String[] FUNCTIONS =
            {"exp", "log", "log2", "sin", "sinh", "cosec", "cosech", "cos", "cosh", "sec", "sech", "tan", "tanh", "cot", "coth"};
    private String[][] constdec;
    private String z_value, oldvalue, variableCode, oldvariablecode;
    private boolean hasBeenSubstituted;
    private boolean advancedDegree;
    public FunctionEvaluator(String variable, String variableCode, String[][] varconst) {
        this(variable, variableCode, varconst, true);
    }
    public FunctionEvaluator(String variable, String variableCode, String[][] varconst, boolean advancedDegree) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(variableCode + "_p");
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    public FunctionEvaluator(String variableCode, String[][] varconst) {
        this(variableCode, varconst, true);
    }
    public FunctionEvaluator(String variableCode, String[][] varconst, boolean advancedDegree) {
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(variableCode + "_p");
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    public FunctionEvaluator(String variableCode, String[][] varconst, String oldvariablecode, boolean advancedDegree) {
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    public FunctionEvaluator(String variable, String variableCode, String[][] varconst, String oldvariablecode) {
        this(variable, variableCode, varconst, oldvariablecode, true);
    }
    public FunctionEvaluator(String variable, String variableCode, String[][] varconst, String oldvariablecode, boolean advancedDegree) {
        setZ_value(variable);
        setConstdec(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
        hasBeenSubstituted = false;
        setAdvancedDegree(advancedDegree);
    }
    @NotNull
    public static FunctionEvaluator prepareIFS(String variableCode, String r_code, String t_code, String p_code, double x, double y) {
        @NotNull String[][] varconst = {{"0", "0"}};
        @NotNull FunctionEvaluator fe = new FunctionEvaluator(variableCode, String.valueOf(x), varconst);
        fe.addConstant(new String[]{r_code, String.valueOf(Math.sqrt(x * x + y * y))}/*rho*/);
        fe.addConstant(new String[]{t_code, String.valueOf(Math.atan2(y, x))}/*theta*/);
        fe.addConstant(new String[]{p_code, String.valueOf(Math.atan2(x, y))}/*phi*/);
        return fe;
    }
    private void addConstant(@NotNull String[] constant) {
        @NotNull String[][] tmpconsts = new String[constdec.length][2];
        for (int i = 0; i < constdec.length; i++) {
            System.arraycopy(constdec[i], 0, tmpconsts[i], 0, tmpconsts.length);
        }
        constdec = new String[tmpconsts.length + 1][2];
        for (int i = 0; i < tmpconsts.length; i++) {
            System.arraycopy(tmpconsts[i], 0, constdec[i], 0, constdec.length);
        }
        System.arraycopy(constant, 0, constdec[constdec.length - 1], 0, constant.length);
    }
    public String getOldvariablecode() {
        return oldvariablecode;
    }
    public void setOldvariablecode(String oldvariablecode) {
        this.oldvariablecode = oldvariablecode;
    }
    public String getOldvalue() {
        return oldvalue;
    }
    public void setOldvalue(String oldvalue) {
        this.oldvalue = oldvalue;
    }
    public boolean isAdvancedDegree() {
        return advancedDegree;
    }
    private void setAdvancedDegree(boolean advancedDegree) {
        this.advancedDegree = advancedDegree;
    }
    public String getVariableCode() {
        return variableCode;
    }
    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }
    @NotNull
    public Complex getDegree(String function) {
        function = replace(function, oldvariablecode, variableCode);
        @NotNull Complex degree = Complex.ZERO;
        if ((function.contains(variableCode) && (!function.contains("^")))) {
            degree = Complex.ONE;
            return degree;
        }
        if (!hasBeenSubstituted) {
            hasBeenSubstituted = true;
            return getDegree(substitute(function, true));
        }
        if (function.contains("exp")) {
            int startidx = function.indexOf("exp");
            int endidx = findMatchingCloser('(', function, function.indexOf('(', startidx + 1));
            @NotNull String function2 = replace(function, function.substring(startidx, endidx + 1), "");
            return getDegree(function2);
        }
        if (function.contains("log")) {
            int startidx = function.indexOf("log");
            int endidx = findMatchingCloser('(', function, function.indexOf('(', startidx + 1));
            @NotNull String function2 = replace(function, function.substring(startidx, endidx + 1), "");
            return getDegree(function2);
        }
        if ((function.contains("*") || function.contains("/")) && advancedDegree) {
            for (int i = 0; i < function.length(); i++) {
                if (function.charAt(i) == '*' || function.charAt(i) == '/') {
                    int closeLeftIndex = indexOfBackwards(function, i, ')');
                    int openLeftIndex = findMatchingOpener(')', function, closeLeftIndex);
                    @NotNull Complex dl = getDegree(function.substring(openLeftIndex, closeLeftIndex + 1));
                    int openRightIndex = function.indexOf('(', i);
                    int closeRightIndex = findMatchingCloser('(', function, openRightIndex);
                    @NotNull Complex dr = getDegree(function.substring(openRightIndex, closeRightIndex + 1));
                    @NotNull Complex tmpdegree = Complex.ZERO;
                    if (function.charAt(i) == '*') {
                        tmpdegree = ComplexOperations.add(dl, dr);
                    } else if (function.charAt(i) == '/') {
                        tmpdegree = ComplexOperations.subtract(dl, dr);
                    }
                    String function2 = function.replace(function.substring(openLeftIndex, closeRightIndex + 1),
                            variableCode + " ^ " + tmpdegree);
                    return getDegree(function2);
                }
            }
        }
        int idx = 0, varidx = 0;
        while (function.indexOf('^', idx) != -1) {
            varidx = function.indexOf(variableCode, varidx) + 1;
            idx = function.indexOf('^', varidx) + 1;
            @NotNull Complex nextDegree = new Complex(function.substring(idx + 1, function.indexOf(' ', idx + 1)));
            degree = (nextDegree.modulus() > degree.modulus()) ? nextDegree : degree;
        }
        return degree;
    }
    @NotNull
    public Complex getDegree(@NotNull Polynomial polynomial) {
        return getDegree(limitedEvaluate(polynomial.toString(), polynomial.countVariableTerms() * 2 + polynomial.countConstantTerms()));
    }
    private boolean hasConditional(String function) {
        return function.contains("?") || function.contains(":");//conditional syntax similar to Java's ternary operator
    }
    private String processConditional(String function) {
        if (hasConditional(function)) {
            while (hasConditional(function)) {
                String conditionalExpression = function.substring(function.lastIndexOf('['), function.indexOf(']') + 1),
                        conditional = conditionalExpression.substring(1, conditionalExpression.length() - 1).trim(),//trim square brackets
                        replaceWith = "";
                String[] parts = split(conditional, "?"),
                        comparison = split(parts[0], " "),
                        replace = split(parts[1], ":");
                boolean result = ComplexComparator.compare(
                        new Complex(comparison[0]),
                        new Complex(comparison[2]),
                        Comparator.fromAlias(parts[1]));
                replaceWith = result ? replace[0] : replace[1];
                function = replace(function, conditionalExpression, replaceWith);
            }
        }
        return function;
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
    public double evaluateForIFS(@NotNull String expr) {
        return evaluate(expr, false).modulus();
    }
    private boolean hasNoFunctions(@NotNull String expr) {
        @NotNull String[] parts = split(expr, " ");
        for (@NotNull String part : parts) {
            for (String function : FUNCTIONS) {
                if (part.equalsIgnoreCase(function)) {
                    return false;
                }
            }
        }
        return true;
    }
    public Complex evaluate(@NotNull String expr, Complex z_value) {
        String z_value_backup = this.z_value;
        setZ_value(z_value.toString());
        Complex result = evaluate(expr, false);
        setZ_value(z_value_backup);
        return result;
    }
    public Complex evaluate(@NotNull String expr, boolean isSymbolic) {
        @NotNull String subexpr = substitute(expr, isSymbolic);
        subexpr = processConditional(subexpr);
        Complex ztmp;
        int flag = 0;
        /**Disabled for performance reasons:
         if ((!isSymbolic) && hasNoFunctions(subexpr)) {
         ztmp = RPNHelper.evaluateInfix(split(subexpr, " "));
         } else {
         do {
         ztmp = eval(process(subexpr));
         if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
         subexpr = replace(subexpr, subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), ztmp.toString());
         } else {
         ++flag;
         }
         } while (flag <= 1);
         }*/
        do {
            ztmp = eval(process(subexpr));
            if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                subexpr = replace(subexpr, subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), ztmp.toString());
            } else {
                ++flag;
            }
        } while (flag <= 1);
        return ztmp;
    }
    @NotNull
    private Complex eval(@NotNull String[] processed) {
        @NotNull Complex ztmp = new Complex(Complex.ZERO);
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
                    case "log2":
                        ztmp = ComplexOperations.log(ztmp, new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "sin":
                        ztmp = ComplexOperations.sin(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "sinh":
                        ztmp = ComplexOperations.sinh(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "cos":
                        ztmp = ComplexOperations.cos(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "cosh":
                        ztmp = ComplexOperations.cosh(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "tan":
                        ztmp = ComplexOperations.tan(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "tanh":
                        ztmp = ComplexOperations.tanh(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "sec":
                        ztmp = ComplexOperations.sec(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "sech":
                        ztmp = ComplexOperations.sech(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "cosec":
                        ztmp = ComplexOperations.cosec(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "cosech":
                        ztmp = ComplexOperations.cosech(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "cot":
                        ztmp = ComplexOperations.cot(new Complex(processed[i + 1]));
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "coth":
                        ztmp = ComplexOperations.coth(new Complex(processed[i + 1]));
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
                    case "re":
                        ztmp = new Complex(ztmp.real(), 0);
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "im":
                        ztmp = new Complex(0, ztmp.imaginary());
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    case "flip":
                        ztmp = ComplexOperations.flip(ztmp);
                        if (i < (processed.length - 1)) {
                            ++i;
                        }
                        break;
                    default:
                        ztmp = new Complex(processed[i]);
                }
            } catch (ArrayIndexOutOfBoundsException ae) {
                throw new IllegalArgumentException("Function Input Error", ae);
            }
        }
        return ztmp;
    }
    private String[] process(@NotNull String subexpr) {
        String expr;
        if (subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1) {
            expr = subexpr;
        } else {
            expr = subexpr.substring(subexpr.lastIndexOf('(') + 1, subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1));
        }
        expr = expr.trim();
        return split(expr, " ");
    }
    private String substitute(@NotNull String expr, boolean isSymbolic) {
        @NotNull String[] mod = split(expr, " ");
        @NotNull String sub = "";
        for (int i = 0; i < mod.length; i++) {
            if (mod[i].equalsIgnoreCase(variableCode) && (!isSymbolic)) {
                mod[i] = z_value;
            } else if ((mod[i].equalsIgnoreCase(oldvariablecode)) && (!isSymbolic)) {
                mod[i] = z_value;
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
        String val;
        for (String[] aConstdec : constdec) {
            if (aConstdec[0].equals(totry)) {
                val = aConstdec[1];
                return val;
            }
        }
        return null;
    }
    @NotNull
    private String limitedEvaluate(@NotNull String expr, int depth) {
        @NotNull String subexpr = substitute(expr, true);
        Complex ztmp;
        int flag = 0, ctr = 0;
        do {
            ztmp = eval(process(subexpr));
            if (!(subexpr.lastIndexOf('(') == -1 || subexpr.indexOf(')') == -1)) {
                subexpr = replace(subexpr, subexpr.substring((subexpr.lastIndexOf('(')), subexpr.indexOf(')', subexpr.lastIndexOf('(') + 1) + 1), "" + ztmp);
                ctr++;
            } else {
                ++flag;
            }
        } while (flag <= 1 && ctr <= depth);
        return subexpr;
    }
}