package in.tamchow.fractal.math.complex;

import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.math.FixedStack;

/**
 * Supports dyadic operations on complex numbers as expressions in RPN format
 */
public class RPNHelper {
    /**
     * in stack precedence
     **/
    private static final int[] isp = {0, 19, 12, 12, 13, 13, 13, 0};
    /**
     * incoming character precedence
     **/
    private static final int[] icp = {20, 19, 12, 12, 13, 13, 13, 0};
    /**
     * operators
     **/
    private static final String[] operators = {"(", ")", "+", "-", "/", "*", "^", " "};
    /**
     * precedence stack
     **/
    private static Precedence[] stack;
    /**
     * stack top pointer
     **/
    private static int top;

    /**
     * pop element from stack
     **/
    private static Precedence pop() {
        return stack[top--];
    }

    /**
     * push element onto stack
     **/
    private static void push(Precedence ele) {
        stack[++top] = ele;
    }

    /**
     * get precedence token for symbol
     **/
    public static Precedence getToken(String symbol) {
        switch (symbol) {
            case "(":
                return Precedence.lparen;
            case ")":
                return Precedence.rparen;
            case "+":
                return Precedence.plus;
            case "-":
                return Precedence.minus;
            case "/":
                return Precedence.divide;
            case "*":
                return Precedence.times;
            case "^":
                return Precedence.pow;
            case " ":
                return Precedence.eos;
            default:
                return Precedence.operand;
        }
    }

    /**
     * Function to convert infix to postfix
     **/
    public static String postfix(String[] infix) {
        String postfix = "";
        top = 0;
        stack = new Precedence[infix.length];
        stack[0] = Precedence.eos;
        Precedence token;
        for (String anInfix : infix) {
            token = getToken(anInfix);
            /** if token is operand append to postfix **/
            if (token == Precedence.operand)
                postfix += anInfix + " ";
            /** if token is right parenthesis pop till matching left parenthesis **/
            else if (token == Precedence.rparen) {
                while (stack[top] != Precedence.lparen)
                    postfix += operators[pop().getIndex()] + " ";
                /** discard left parenthesis **/
                pop();
            }
            /** else pop stack elements whose precedence is greater than that of token **/
            else {
                while (isp[stack[top].getIndex()] >= icp[token.getIndex()])
                    postfix += operators[pop().getIndex()] + " ";
                push(token);
            }
        }
        /** pop any remaining elements in stack **/
        while ((token = pop()) != Precedence.eos)
            postfix += operators[token.getIndex()] + " ";
        return postfix.trim();
    }

    public static Complex evaluateRPN(String[] tokens) {
        if (tokens.length == 0) {
            throw new UnsupportedOperationException("Blank");
        }
        FixedStack<String> tks = new FixedStack<>(tokens.length);
        //tks.initStack(tokens);
        tks.pushN(tokens);
        /*for(String token:tokens){
            tks.push(token);
        }*/
        return evaluateRPN(tks);
    }

    public static Complex evaluateInfix(String[] infix) {
        return evaluateRPN(StringManipulator.split(postfix(infix), " "));
    }

    public static Complex evaluateRPN(FixedStack<String> tkstack) {
        String tk = tkstack.pop();
        Complex x, y;
        try {
            x = new Complex(tk);
        } catch (NumberFormatException nfe) {
            y = evaluateRPN(tkstack);
            x = evaluateRPN(tkstack);
            switch (tk) {
                case "+":
                    x = ComplexOperations.add(x, y);
                    break;
                case "-":
                    x = ComplexOperations.subtract(x, y);
                    break;
                case "*":
                    x = ComplexOperations.multiply(x, y);
                    break;
                case "/":
                    x = ComplexOperations.divide(x, y);
                    break;
                case "^":
                    x = ComplexOperations.power(x, y);
                    break;
                default:
                    throw new UnsupportedOperationException("Illegal Character Entered");
            }
        }
        return x;
    }

    private enum Precedence {
        lparen(0),

        rparen(1),

        plus(2),

        minus(3),

        divide(4),

        times(5),

        pow(6),

        eos(7),

        operand(8);

        private int index;

        Precedence(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}