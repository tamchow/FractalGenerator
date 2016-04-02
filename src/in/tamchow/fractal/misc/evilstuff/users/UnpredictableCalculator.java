package in.tamchow.fractal.misc.evilstuff.users;
import in.tamchow.fractal.helpers.math.FixedStack;
import in.tamchow.fractal.misc.evilstuff.EvilStuff;
/**
 * A class which implements a calculator which provides unpredictable functions.
 * <p/>
 * Uses {@link EvilStuff}
 *
 * @author Tamoghna Chowdhury
 * @version 19.03.2016
 */
public class UnpredictableCalculator {
    private static void repeatedlyDoEvilStuffToInteger(int waitPeriod) {
        EvilStuff.repeatedlyDoEvilStuff(Integer.class, "cache", "Evil_Thread_", waitPeriod);
    }
    public static void main(String[] args) {
        java.util.Scanner in = new java.util.Scanner(System.in);
        repeatedlyDoEvilStuffToInteger(64);
        do {
            System.out.println("\nEnter an expression to evaluate.\nPlease use integers between -64 (-128/2) and 63 (127/2):");
            String expression = in.nextLine();
            try {
                final String SPLIT_REGEX = "[\\s]+";
                int result = RPNHelper.evaluateRPN(RPNHelper.postfix(expression.split(SPLIT_REGEX)).split(SPLIT_REGEX));
                System.out.format("%s = %s , no, really it's %d",
                        expression, result, new Integer(result));
            } catch (IllegalArgumentException | java.util.EmptyStackException incorrectException) {
                System.out.println("I won't be more unpredictable if you don't let me :( -> " +
                        "\nI didn't expect you to enter: " +
                        ((incorrectException instanceof java.util.EmptyStackException) ?
                                expression : incorrectException.getLocalizedMessage()));
                try {
                    EvilStuff.stopDoingEvilStuff();
                } catch (InterruptedException interrupted) {
                    interrupted.printStackTrace();
                } finally {
                    break;
                }
            }
        } while (true);
    }
}
class RPNHelper {
    // in stack precedence
    private static final int[] isp = {0, 19, 12, 12, 13, 13, 13, 0};
    // incoming character precedence
    private static final int[] icp = {20, 19, 12, 12, 13, 13, 13, 0};
    // operators
    private static final char[] operators = {'(', ')', '+', '-', '/', '*', '%', ' '};
    // precedence stack
    private static Precedence[] stack;
    // stack top pointer
    private static int top;
    // pop element from stack
    private static Precedence pop() {
        return stack[top--];
    }
    // push element onto stack
    private static void push(Precedence ele) {
        stack[++top] = ele;
    }
    // get precedence token for symbol
    public static Precedence getToken(String symbol) {
        switch (symbol.charAt(0)) {
            case '(':
                return Precedence.lparen;
            case ')':
                return Precedence.rparen;
            case '+':
                return Precedence.plus;
            case '-':
                return Precedence.minus;
            case '/':
                return Precedence.divide;
            case '*':
                return Precedence.times;
            case '%':
                return Precedence.mod;
            case ' ':
                return Precedence.eos;
            default:
                return Precedence.operand;
        }
    }
    // Function to convert infix to postfix
    public static String postfix(String[] infix) {
        String postfix = "";
        top = 0;
        stack = new Precedence[infix.length];
        stack[0] = Precedence.eos;
        Precedence token;
        for (int i = 0; i < infix.length; i++) {
            token = getToken(infix[i]);
            // if token is operand append to postfix
            if (token == Precedence.operand)
                postfix += infix[i] + " ";
                // if token is right parenthesis pop till matching left parenthesis
            else if (token == Precedence.rparen) {
                while (stack[top] != Precedence.lparen)
                    postfix += operators[pop().getIndex()] + " ";
                // discard left parenthesis
                pop();
            }
            // else pop stack elements whose precedence is greater than that of token
            else {
                while (isp[stack[top].getIndex()] >= icp[token.getIndex()])
                    postfix += operators[pop().getIndex()] + " ";
                push(token);
            }
        }
        // pop any remaining elements in stack
        while ((token = pop()) != Precedence.eos)
            postfix += operators[token.getIndex()] + " ";
        return postfix.trim();
    }
    public static int evaluateRPN(String[] tokens) {
        if (tokens.length == 0) {
            throw new UnsupportedOperationException("Blank");
        }
        FixedStack<String> tks = new FixedStack<>(tokens.length);
        tks.initStack(tokens);
        return evaluateRPN(tks);
    }
    public static int evaluateRPN(FixedStack<String> tkstack) {
        String tk = tkstack.pop();
        int x, y;
        try {
            x = Integer.parseInt(tk);
        } catch (NumberFormatException nfe) {
            y = evaluateRPN(tkstack);
            x = evaluateRPN(tkstack);
            switch (tk.charAt(0)) {
                case '+':
                    x += y;
                    break;
                case '-':
                    x -= y;
                    break;
                case '*':
                    x *= y;
                    break;
                case '/':
                    x /= y;
                    break;
                case '%':
                    x %= y;
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
        mod(6),
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