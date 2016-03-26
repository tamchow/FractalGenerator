package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Support for transcendental functions for derivative-requiring fractal modes
 */
public class FunctionTerm {
    public final FunctionTermData[] functions = {new FunctionTermData("sin", "$v * ( cos $ )", "( ( - ( sin $ ) ) * $v ) + ( $vv * cos $ )"),
            new FunctionTermData("cos", "( - ( sin $ ) ) * $v", "( ( - ( cos $ ) ) * $v ) + ( $vv * ( - ( sin $ ) ) )"),
            new FunctionTermData("log", " $v / $", "( ( - ( $v * $v ) ) / ( $ * $ ) ) + ( ( $vv * $v ) / $ )"),
            new FunctionTermData("exp", "$v * ( exp $ )", "exp $ * ( $v + $vv )"),
            new FunctionTermData("sinh", "$v * ( cosh $ )", "( $v * ( sinh $ ) ) + ( $vv * ( cosh $ ) )"),
            new FunctionTermData("cosh", "$v * ( sinh $ )", "( $v * ( cosh $ ) ) + ( $vv * ( sinh $ ) )"),
            new FunctionTermData("tan", "$v * ( ( sec $ ) ^ 2 )", "( ( sec $ ) ^ 2 ) * ( ( 2 * ( $v ^ 2 ) * tan $ ) + $vv )"),
            new FunctionTermData("tanh", "$v * ( ( sech $ ) ^ 2 )", "( ( sech $ ) ^ 2 ) * ( $vv - ( 2 * ( $v ^ 2 ) * tanh $ ) )"),
            new FunctionTermData("cot", "- ( $v * ( ( cosec $ ) ^ 2 ) )", "( ( cosec $ ) ^ 2 ) * ( ( 2 * ( $v ^ 2 ) * cot $ ) - $vv )"),
            new FunctionTermData("coth", "- ( $v * ( ( cosech $ ) ^ 2 ) )", "( ( cosech $ ) ^ 2 ) * ( ( 2 * ( $v ^ 2 ) * coth $ ) - $vv )"),
            new FunctionTermData("sec", "$v * ( ( sec $ ) * ( tan $ ) )", "$vv * tan $ * sec $ + ( $v  ^ 2 ) * ( ( sec $ ) ^ 3 ) + ( $v  ^ 2 ) * ( ( tan $ ) ^ 2 ) * ( sec $ ) - ( ( $v  ^ 2 ) * ( ( sec $ ) ^ 3 ) )"),
            new FunctionTermData("sech", "$v * ( - ( tanh $ ) * ( sech $ ) )", "( $v  ^ 2 ) * ( ( tan $ ) ^ 2 ) * ( sec $ ) - ( $vv * tan $ * sec $ )"),
            new FunctionTermData("cosec", "$v * ( - ( cosec $ ) * ( cot $ ) )", "( $v ^ 2 ) * ( ( cosec $ ) ^ 3 ) + ( $v ^ 2 ) * ( ( cot $ ) ^ 2 ) * ( cosec $ ) - ( $vv * cot $ * cosec $ )"),
            new FunctionTermData("cosech", "$v * ( - ( cosech $ ) * ( coth $ ) )", "( $v ^ 2 ) * ( ( cosech $ ) ^ 3 ) + ( $v ^ 2 ) * ( ( coth $ ) ^ 2 ) * ( cosech $ ) - ( $vv * coth $ * cosech $ )")};
    String function, constant, variableCode, oldvariablecode;
    Polynomial coefficient, argument;
    String[][] consts;
    public static FunctionTerm fromString(String function, String variableCode, String oldvariablecode) {
        FunctionTerm f = new FunctionTerm();
        String[] parts = StringManipulator.split(function, ";");
        f.variableCode = variableCode;
        f.oldvariablecode = oldvariablecode;
        f.coefficient = Polynomial.fromString(parts[0]);
        if (parts.length == 4) {
            f.constant = parts[3];
        } else {
            f.constant = "0";
        }
        f.function = parts[1];
        f.argument = Polynomial.fromString(parts[2]);
        return f;
    }
    public static boolean isSpecialFunctionTerm(String function) {
        return getUsedFunctionTermIndex(function) != -1;
    }
    public static int getUsedFunctionTermIndex(String function) {
        for (int i = 0; i < new FunctionTerm().functions.length; i++) {
            if (function.contains(new FunctionTerm().functions[i].function)) {
                return i;
            }
        }
        return -1;
    }
    public String[][] getConsts() {
        return consts;
    }
    public void setConsts(String[][] constdec) {
        this.consts = new String[constdec.length][constdec[0].length];
        for (int i = 0; i < this.consts.length; i++) {
            System.arraycopy(constdec[i], 0, this.consts[i], 0, this.consts[i].length);
        }
    }
    @Override
    public String toString() {
        return coefficient + " * ( " + function.trim() + " ( " + argument + " ) ) + ( " + constant.trim() + " )";
    }
    public String derivative(int order) {
        coefficient.setConstdec(consts);
        argument.setConstdec(consts);
        String deriv = "";
        switch (order) {
            case 1:
                deriv += "( # * fv ) + ( #v * f)";
                break;
            case 2:
                deriv += "( # * fvv ) + ( 2 * ( #v * fv ) ) + ( #vv * f )";
                break;
            default:
                throw new IllegalArgumentException("Only 1st and 2nd order derivatives are supported");
        }
        final String[][] REPLACEMENTS = {{"fvv", "( " + functions[getUsedFunctionTermIndex(function)].derivative2.trim() + " )"}, {"fv", "( " + functions[getUsedFunctionTermIndex(function)].derivative1.trim() + " )"}, {"f", "( " + function.trim() + " $ )"}, {"#vv", "( " + coefficient.derivative().derivative().toString().trim() + " )"}, {"#v", "( " + coefficient.derivative().toString().trim() + " )"}, {"#", "( " + coefficient.toString().trim() + " )"}, {"$vv", "( " + argument.derivative().derivative().toString().trim() + " )"}, {"$v", "( " + argument.derivative().toString().trim() + " )"}, {"$", "( " + argument.toString().trim() + " )"}};
        return StringManipulator.format(deriv, REPLACEMENTS);
    }
    public Complex getDegree() {
        return coefficient.getDegree();
    }
    private class FunctionTermData {
        String function, derivative1, derivative2;
        public FunctionTermData(String function, String derivative1, String derivative2) {
            this.function = function;
            this.derivative1 = derivative1;
            this.derivative2 = derivative2;
        }
        public FunctionTermData(FunctionTermData old) {
            function = old.function;
            derivative1 = old.derivative1;
            derivative2 = old.derivative2;
        }
    }
}