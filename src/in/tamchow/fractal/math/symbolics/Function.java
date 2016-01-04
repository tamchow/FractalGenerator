package in.tamchow.fractal.math.symbolics;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Support for transcendental functions for derivative-requiring fractal modes
 */
public class Function {
    public final FunctionData[] functions = {new FunctionData("sin", "$v * ( cos $ )", "( ( - ( sin $ ) ) * $v ) + ( $vv * cos $ )"), new FunctionData("cos", "( - ( sin $ ) ) * $v", "( ( - ( cos $ ) ) * $v ) + ( $vv * ( - ( sin $ ) ) )"), new FunctionData("log", " $v / $", "( ( - ( $v * $v ) ) / ( $ * $ ) ) + ( ( $vv * $v ) / $ )"), new FunctionData("exp", "$v * ( exp $ )", "exp $ * ( $v + $vv )"), new FunctionData("sinh", "$v * ( cosh $ )", "( $v * ( sinh $ ) ) + ( $vv * ( cosh $ ) )"), new FunctionData("cosh", "$v * ( sinh $ )", "( $v * ( cosh $ ) ) + ( $vv * ( sinh $ ) )")};
    String function, constant, variableCode;
    Polynomial coefficient, argument;
    String[][] consts;
    public static Function fromString(String function, String variableCode) {
        Function f = new Function(); f.variableCode = variableCode;
        f.coefficient = Polynomial.fromString(function.split(";")[0]);
        if (function.split(";").length == 4) {f.constant = function.split(";")[3];} else {f.constant = "0";}
        f.function = function.split(";")[1]; f.argument = Polynomial.fromString(function.split(";")[2]); return f;
    }
    public static boolean isSpecialFunction(String function) {return getUsedFunctionIndex(function) != -1;}
    public static int getUsedFunctionIndex(String function) {
        for (int i = 0; i < new Function().functions.length; i++) {
            if (function.contains(new Function().functions[i].function)) {return i;}
        } return -1;
    }
    public String[][] getConsts() {
        return consts;
    }
    public void setConsts(String[][] constdec) {
        this.consts = new String[constdec.length][constdec[0].length]; for (int i = 0; i < this.consts.length; i++) {
            System.arraycopy(constdec[i], 0, this.consts[i], 0, this.consts[i].length);
        }
    }
    public String toString() {return coefficient + " * ( " + function.trim() + " ( " + argument + " ) ) + ( " + constant + " )";}
    public String derivative(int degree) {
        coefficient.setConstdec(consts); argument.setConstdec(consts); String deriv = ""; switch (degree) {
            case 1: deriv += "( # * fv ) + ( #v * f)"; break;
            case 2: deriv += "( # * fvv ) + ( 2 * ( #v * fv ) ) + ( #vv * f )"; break;
            default: throw new IllegalArgumentException("Only 1st and 2nd order derivatives are supported");
        } deriv = deriv.replace("fvv", "( " + functions[getUsedFunctionIndex(function)].derivative2.trim() + " )");
        deriv = deriv.replace("fv", "( " + functions[getUsedFunctionIndex(function)].derivative1.trim() + " )");
        deriv = deriv.replace("f", "( " + function.trim() + " $ )");
        deriv = deriv.replace("#vv", "( " + coefficient.derivative().derivative().toString().trim() + " )");
        deriv = deriv.replace("#v", "( " + coefficient.derivative().toString().trim() + " )");
        deriv = deriv.replace("#", "( " + coefficient.toString().trim() + " )");
        deriv = deriv.replace("$vv", "( " + argument.derivative().derivative().toString().trim() + " )");
        deriv = deriv.replace("$v", "( " + argument.derivative().toString().trim() + " )");
        deriv = deriv.replace("$", "( " + argument.toString().trim() + " )"); return deriv;
    }
    public Complex getDegree() {return coefficient.getDegree();}
    private class FunctionData {
        String function, derivative1, derivative2;
        public FunctionData(String function, String derivative1, String derivative2) {
            this.function = function; this.derivative1 = derivative1; this.derivative2 = derivative2;
        }
        public FunctionData(FunctionData old) {function = old.function; derivative1 = old.derivative1; derivative2 = old.derivative2;}
    }
}