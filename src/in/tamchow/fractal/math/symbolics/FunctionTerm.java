package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Support for transcendental {@link FunctionTerm#FUNCTION_DATA}s for derivative-requiring fractal modes
 */
class FunctionTerm extends Derivable {
    private static final FunctionTermData[] FUNCTION_DATA = {
            new FunctionTermData("sin", "$v*(cos$)", "((-(sin$))*$v)+($vv*(cos$))"),
            new FunctionTermData("cos", "(-(sin$))*$v", "((-(cos$))*$v)+($vv*(-(sin$)))"),
            new FunctionTermData("log", "$v/$", "((-($v*$v))/($*$))+(($vv*$v)/$)"),
            new FunctionTermData("exp", "$v*(exp$)", "exp$*($v+$vv)"),
            new FunctionTermData("sinh", "$v*(cosh$)", "($v*(sinh$))+($vv*(cosh$))"),
            new FunctionTermData("cosh", "$v*(sinh$)", "($v*(cosh$))+($vv*(sinh$))"),
            new FunctionTermData("tan", "$v*((sec$)^" + _2 + ")", "((sec$)^" + _2 + ")*((" + _2 + "*($v^" + _2 + ")*(tan$))+$vv)"),
            new FunctionTermData("tanh", "$v*((sech$)^" + _2 + ")", "((sech$)^" + _2 + ")*($vv-(" + _2 + "*($v^" + _2 + ")*(tanh$)))"),
            new FunctionTermData("cot", "-($v*((cosec$)^" + _2 + "))", "((cosec$)^" + _2 + ")*((" + _2 + "*($v^" + _2 + ")*(cot$))-$vv)"),
            new FunctionTermData("coth", "-($v*((cosech$)^" + _2 + "))", "((cosech$)^" + _2 + ")*((" + _2 + "*($v^" + _2 + ")*coth$)-$vv)"),
            new FunctionTermData("sec", "$v*((sec$)*(tan$))", "$vv*(tan$)*(sec$)+($v^" + _2 + ")*((sec$)^" + _3 + ")+($v^" + _2 + ")*((tan$)^" + _2 + ")*(sec$)-(($v^" + _2 + ")*((sec$)^" + _3 + "))"),
            new FunctionTermData("sech", "$v*(-(tanh$)*(sech$))", "($v^" + _2 + ")*((tan$)^" + _2 + ")*(sec$)-($vv*(tan$)*(sec$))"),
            new FunctionTermData("cosec", "$v*(-(cosec$)*(cot$))", "($v^" + _2 + ")*((cosec$)^" + _3 + ")+($v^" + _2 + ")*((cot$)^" + _2 + ")*(cosec$)-($vv*(cot$)*(cosec$))"),
            new FunctionTermData("cosech", "$v*(-(cosech$)*(coth$))", "($v^" + _2 + ")*((cosech$)^" + _3 + ")+($v^" + _2 + ")*((coth$)^" + _2 + ")*(cosech$)-($vv*(coth$)*(cosech$))")};
    private static final String FUNCTION_DERIVATIVE_1 = "(#*e*(f^ev)*fv)+(#v*(f^e))",
            FUNCTION_DERIVATIVE_2 = "(" + _2 + "*e*#v*fv*(f^ev))+(#vv*(f^e))+(e*#*((ev*(f^evv)*(fv^" + _2 + "))+(fvv*(f^ev))))";
    private String function, constant, exponent, variableCode, oldvariablecode, z_value;
    private Polynomial coefficient, argument;
    private String[][] consts;
    private boolean polynomial;
    public FunctionTerm() {
    }
    public FunctionTerm(String variable, String variableCode, String oldvariablecode, @NotNull String[][] varconst) {
        setZ_value(variable);
        setConsts(varconst);
        setVariableCode(variableCode);
        setOldvariablecode(oldvariablecode);
    }
    @NotNull
    public static FunctionTerm fromString(@NotNull String function, String variableCode, @NotNull String[][] consts, String oldvariablecode) {
        @NotNull FunctionTerm f = new FunctionTerm(null, variableCode, oldvariablecode, consts);
        @NotNull String[] parts = StringManipulator.split(function, ";");
        f.coefficient = Polynomial.fromString(parts[0], variableCode, oldvariablecode, consts);
        if (parts.length > 1) {
            f.function = parts[1];
            f.argument = Polynomial.fromString(parts[2], variableCode, oldvariablecode, consts);
            f.exponent = parts[3];
            if (parts.length >= 5) {
                f.constant = parts[4];
            } else {
                f.constant = _0;
            }
            f.polynomial = false;
        } else {
            f.polynomial = true;
        }
        return f;
    }
    static boolean isSpecialFunctionTerm(@NotNull String function) {
        return getUsedFunctionTermIndex(function) != -1;
    }
    private static int getUsedFunctionTermIndex(@NotNull String function) {
        for (int i = 0; i < FUNCTION_DATA.length; i++) {
            if (function.contains(FUNCTION_DATA[i].function)) {
                return i;
            }
        }
        return -1;
    }
    public String[][] getConsts() {
        return consts;
    }
    public void setConsts(@NotNull String[][] constdec) {
        this.consts = new String[constdec.length][constdec[0].length];
        for (int i = 0; i < this.consts.length; i++) {
            System.arraycopy(constdec[i], 0, this.consts[i], 0, this.consts[i].length);
        }
    }
    private boolean isPolynomial() {
        return polynomial || ((function == null || function.isEmpty() || argument == null) && (coefficient != null));
    }
    @NotNull
    @Override
    public String toString() {
        if (isPolynomial()) {
            return coefficient.toString();
        }
        return new ResizableCharBuffer().append(coefficient.toString()).append("*((").append(function.trim()).append("(").append(argument.toString()).append("))^(").append(exponent.trim()).append("))+(").append(constant.trim()).append(")").toString();
    }
    @NotNull
    public String derivativeBase(int order) {
        if (isPolynomial()) {
            return coefficient.derivative(order);
        }
        coefficient.setConsts(consts);
        argument.setConsts(consts);
        @NotNull String deriv;
        switch (order) {
            case 0:
                return toString();
            case 1:
                deriv = FUNCTION_DERIVATIVE_1;
                break;
            case 2:
                deriv = FUNCTION_DERIVATIVE_2;
                break;
            default:
                throw new IllegalArgumentException(UNSUPPORTED_DERIVATIVE_ORDER_MESSAGE);
        }
        @NotNull final String[][] REPLACEMENTS = {
                {"evv", "(e- " + _2 + ")"},
                {"ev", "(e- " + _1 + ")"},
                {"e", "(" + exponent + ")"},
                {"fvv", "(" + FUNCTION_DATA[getUsedFunctionTermIndex(function)].derivative2.trim() + ")"},
                {"fv", "(" + FUNCTION_DATA[getUsedFunctionTermIndex(function)].derivative1.trim() + ")"},
                {"f", "(" + function.trim() + "$)"},
                {"#vv", "(" + coefficient.secondDerivative() + ")"},
                {"#v", "(" + coefficient.firstDerivative() + ")"},
                {"#", "(" + coefficient.toString().trim() + ")"},
                {"$vv", "(" + argument.secondDerivative() + ")"},
                {"$v", "(" + argument.firstDerivative() + ")"},
                {"$", "(" + argument.toString().trim() + ")"}};
        return StringManipulator.format(deriv, REPLACEMENTS);
    }
    @NotNull
    public Complex getDegree() {
        return coefficient.getDegree();
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
    public String getZ_value() {
        return z_value;
    }
    public void setZ_value(String z_value) {
        this.z_value = z_value;
    }
    private static class FunctionTermData {
        String function, derivative1, derivative2;
        public FunctionTermData(String function, String derivative1, String derivative2) {
            this.function = function;
            this.derivative1 = derivative1;
            this.derivative2 = derivative2;
        }
        public FunctionTermData(@NotNull FunctionTermData old) {
            function = old.function;
            derivative1 = old.derivative1;
            derivative2 = old.derivative2;
        }
    }
}