package in.tamchow.fractal.math.complex;
import in.tamchow.fractal.helpers.math.FixedStack;
/**
 * Supports dyadic operations on complex numbers as expressions in RPN format
 */
public class RPNHelper {
    // Supported operators
    private static final Operator[] OPERATORS = {
            new Operator("+", 0, Associativity.LEFT),
            new Operator("-", 0, Associativity.LEFT),
            new Operator("*", 5, Associativity.LEFT),
            new Operator("/", 5, Associativity.LEFT),
            new Operator("^", 10, Associativity.RIGHT)
    };
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
        return evaluateRPN(infixToRPN(infix));
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
    private static boolean contains(String token) {
        return getByToken(token) != null;
    }
    /**
     * Test if a certain is an operator .
     *
     * @param token The token to be tested .
     * @return True if token is an operator . Otherwise False .
     */
    private static boolean isOperator(String token) {
        return contains(token);
    }
    private static Operator getByToken(String token) {
        for (Operator operator : OPERATORS) {
            if (operator.symbol.equals(token)) {
                return operator;
            }
        }
        return null;
    }
    /**
     * Test the associativity of a certain operator token .
     *
     * @param token The token to be tested (needs to operator).
     * @param type  Associativity.LEFT or Associativity.RIGHT
     * @return True if the tokenType equals the input parameter type .
     */
    private static boolean isAssociative(String token, Associativity type) {
        if (!isOperator(token)) {
            throw new IllegalArgumentException("Invalid token: " + token);
        }
        return getByToken(token).associativity == type;
    }
    /**
     * Compare precendece of two operators.
     *
     * @param token1 The first operator .
     * @param token2 The second operator .
     * @return A negative number if token1 has a smaller precedence than token2,
     * 0 if the precendences of the two tokens are equal, a positive number
     * otherwise.
     */
    private static int comparePrecedence(String token1, String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new IllegalArgumentException("Invalid tokens: " + token1
                    + " " + token2);
        }
        return getByToken(token1).precedence - getByToken(token2).precedence;
    }
    private static int countOccurrencesOfParentheses(String[] inputTokens) {
        int ctr = 0;
        for (String token : inputTokens) {
            if (token.equals("(") || token.equals(")")) {
                ++ctr;
            }
        }
        return ctr;
    }
    public static String[] infixToRPN(String[] inputTokens) {
        String[] out = new String[inputTokens.length - countOccurrencesOfParentheses(inputTokens)];
        int outCtr = 0;
        FixedStack<String> stack = new FixedStack<>(inputTokens.length);
        // For all the input tokens [S1] read the next token [S2]
        for (String token : inputTokens) {
            if (isOperator(token)) {
                // If token is an operator (x) [S3]
                while (!stack.isEmpty() && isOperator(stack.peek())) {
                    // [S4]
                    if ((isAssociative(token, Associativity.LEFT) && comparePrecedence(
                            token, stack.peek()) <= 0)
                            || (isAssociative(token, Associativity.RIGHT) && comparePrecedence(
                            token, stack.peek()) < 0)) {
                        out[outCtr++] = stack.pop();    // [S5] [S6]
                        continue;
                    }
                    break;
                }
                // Push the new operator on the stack [S7]
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);    // [S8]
            } else if (token.equals(")")) {
                // [S9]
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    out[outCtr++] = stack.pop(); // [S10]
                }
                stack.pop(); // [S11]
            } else {
                out[outCtr++] = token; // [S12]
            }
        }
        while (!stack.isEmpty()) {
            out[outCtr++] = stack.pop(); // [S13]
        }
        return out;
    }
    private enum Associativity {
        LEFT, RIGHT
    }
    private static class Operator {
        private String symbol;
        private int precedence;
        private Associativity associativity;
        public Operator(String symbol, int precedence, Associativity associativity) {
            this.symbol = symbol;
            this.precedence = precedence;
            this.associativity = associativity;
        }
    }
}