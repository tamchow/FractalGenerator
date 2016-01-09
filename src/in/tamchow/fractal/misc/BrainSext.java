package in.tamchow.fractal.misc;
import in.tamchow.fractal.math.MathUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
/**
 * Interpreter for custom BFk-family language christened "BrainSext"(raw pun and acronym for "Brains Extended",
 * capable of interpreting PBrain, BrainFlow, pure BFk (directly),
 * command sets from Extended BFk,and  a set of extended behaviour from PointerLang
 * <a href="http://codegolf.stackexchange.com/questions/52277/write-an-interpreter-for-my-new-esoteric-language-pointerlang?rq=1">here</a>>
 * (with replacement by equivalent commands)
 *
 * Example "Hello World" program:
 *
 * <pre>[G2.]\Hello World</pre>
 *
 * Explanation:
 *
 * 1. Anything after '\', or halt, is preloaded as data
 * 2. '[' indicates start of loop
 * 3. 'G' will execute next operation if value at pointer is greater than value in storage, which is by default -1;
 * 4. 2 is the index in the code string relative to the currently executing operand to jump to,
 * if the condition is not met.
 * 5. '.' outputs the character representation of the value at the pointer
 * 6. ']' indicates end of loop
 */
public class BrainSext {
    public static final int OUTPUT = 0, ERROR = 1;
    String code, codebackup, output, errors;
    int[] procidx, operand;
    int ptr, proctr, itmp, size, errorCount;
    boolean silent;
    private int storage;
    public BrainSext(boolean silent) {
        code = ""; codebackup = code; size = 65536; procidx = new int[size]; initMemory(); ptr = 0; proctr = 0;
        itmp = 0; storage = -1; this.silent = silent;
    }
    void initMemory() {operand = new int[size]; for (int i = 0; i < operand.length; i++) {operand[i] = -1;}}
    public static void main(String[] args) {
        BrainSext executor = new BrainSext(false);
        if (args.length == 0) {throw new IllegalArgumentException("Nothing to interpret");}
        if (args[0].equalsIgnoreCase("-s")) {
            executor.size = Integer.parseInt(args[1]);
            if (args[2].equalsIgnoreCase("-f")) {executor.readFile(args[3]);} else {
                executor.code = args[2]; executor.codebackup = executor.code;
            }
        } else {
            if (args[0].equalsIgnoreCase("-f")) {
                executor.readFile(args[1]);
            } else {executor.code = args[0]; executor.codebackup = executor.code;}
        } executor.execute();
    }
    public boolean isSilent() {return silent;}
    public void setSilent(boolean silent) {this.silent = silent;}
    public void setMemory(int[] data) {size = data.length; operand = new int[size]; System.arraycopy(data, 0, operand, 0, size);}
    void readFile(String path) {
        File input = new File(path); try {
            Scanner sc = new Scanner(input); while (sc.hasNextLine()) {code += sc.nextLine();} codebackup = code;
        } catch (FileNotFoundException e) {System.err.println("Input File location " + path + " is invalid.");}
    }
    public void execute() {
        int[] tmp = new int[operand.length];
        //filter comments first
        while (code.contains("{") && code.contains("}")) {
            int i1 = code.indexOf('{'), i2 = code.indexOf('}'); code = code.replace(code.substring(i1, i2 + 1), "");
        } //load appended data,if any
        if (code.contains("h") && code.length() > code.indexOf('h') + 1) {
            String data = code.substring(code.indexOf('h') + 1, code.length());
            for (int i = 0; i < data.length() && i < size; i++) {operand[i] = data.charAt(i);}
        } outer:
        for (int i = 0; i < code.length(); ) {
            switch (code.charAt(i)) {
                case 'm': if (numAfter(i)) {
                    size = StringManipulator.getNumFromIndex(code, i + 1);
                } else {size = codebackup.length();} initMemory(); for (int j = 0; j < codebackup.length(); j++) {
                    operand[j] = codebackup.charAt(j);
                } break; case 'u': for (int j = 0; j < code.length() && j < operand.length; j++) {
                    code += "" + (char) operand[j];
                } initMemory(); continue outer;
                case '(': procidx[proctr] = i + 1; i = StringManipulator.findMatchingCloser('(', codebackup, procidx[operand[ptr]]) + 1; if (i == 0) {
                    setOutput("Unmatched ( for procedure declaration at " + i + "\n", ERROR);
                } i = MathUtils.boundsProtected(i, code.length()); proctr++; break;
                case ')': break;//skip this over, index will be updated anyway
                case '[': if (StringManipulator.findMatchingCloser('[', code, i - 1) == -1) {
                    setOutput("Unmatched [ at " + i + "\n", ERROR);
                } if (operand[ptr] == 0) {
                    i = StringManipulator.findMatchingCloser('[', code, i + 1) + 1;
                } continue outer; case ']': if (StringManipulator.findMatchingOpener(']', code, i - 1) == -1) {
                    setOutput("Unmatched ] at " + i + "\n", ERROR);
                } i = StringManipulator.findMatchingOpener(']', code, i - 1) + 1; continue outer;
                case ':': codebackup = code; itmp = StringManipulator.findMatchingCloser('(', codebackup, procidx[operand[ptr]]); if (itmp == -1) {
                    setOutput("Unmatched ) for procedure call at " + i + "\n", ERROR);
                } operand[ptr] = MathUtils.boundsProtected(operand[ptr], procidx.length); code = codebackup.substring(procidx[operand[ptr]], itmp - 1); execute(); i = itmp; code = codebackup; continue outer;
                case 'C': operand[ptr] = (operand[ptr] + "").charAt(0);
                case 'I': operand[ptr] = Integer.valueOf("" + (char) operand[ptr]);
                case '@': i = jumpIndex(i, false); continue outer;
                case 's': int t = operand[ptr]; operand[ptr] = storage; storage = t; break;
                case 'f': operand[ptr] = storage; break; case 't': storage = operand[ptr]; break;
                case 'l': operand[ptr] = operand[ptr] << storage; break;
                case 'r': operand[ptr] = operand[ptr] >> storage; break;
                case 'a': operand[ptr] = operand[ptr] & storage; break;
                case 'n': operand[ptr] = toInt(!fromInt(operand[ptr])); break;
                case 'o': operand[ptr] = operand[ptr] | storage; break;
                case 'x': operand[ptr] = operand[ptr] ^ storage; break;
                case 'd': System.arraycopy(operand, 0, tmp, 0, operand.length); System.arraycopy(tmp, 0, operand, 0, ptr - 1); System.arraycopy(tmp, ptr + 1, operand, ptr, tmp.length); break;
                case 'i': System.arraycopy(operand, 0, tmp, 0, operand.length); System.arraycopy(tmp, 0, operand, 0, ptr); System.arraycopy(tmp, ptr, operand, ptr + 1, tmp.length); operand[ptr] = -1; break;
                case 'E': if (operand[ptr] == storage) {i++; break;} else {i = jumpIndex(i, true); continue outer;}
                case 'N': if (!(operand[ptr] == storage)) {i++; break;} else {i = jumpIndex(i, true); continue outer;}
                case 'G': if (operand[ptr] > storage) {i++; break;} else {i = jumpIndex(i, true); continue outer;}
                case 'L': if (operand[ptr] < storage) {i++; break;} else {i = jumpIndex(i, true); continue outer;}
                case '<': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    ptr -= StringManipulator.getNumFromIndex(code, i + 1); ptr = MathUtils.boundsProtected(ptr, size);
                } else {if (ptr - 1 < 0) {ptr = size - 1;} else {--ptr;}} break; case '>': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    ptr += StringManipulator.getNumFromIndex(code, i + 1); ptr = MathUtils.boundsProtected(ptr, size);
                } else {if (ptr + 1 >= size) {ptr = size - 1;} else {--ptr;}} break; case '+': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] += StringManipulator.getNumFromIndex(code, i + 1);
                } else {++operand[ptr];} break; case '-': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] -= StringManipulator.getNumFromIndex(code, i + 1);
                } else {--operand[ptr];} break; case '=': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] = StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] = ptr;} break; case '*': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] *= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] *= 1;} break; case '/': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] /= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] /= 1;} break; case '%': if (numAfter(i)) {
                    i = indexAfterSkipLiteral(i, StringManipulator.getNumFromIndex(code, i + 1));
                    operand[ptr] %= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] %= 1;} break;
                case '^': ptr = operand[ptr]; ptr = MathUtils.boundsProtected(ptr, size); break;
                case '&': operand[ptr] = MathUtils.boundsProtected(operand[ptr], size); operand[ptr] = operand[operand[ptr]]; break;
                case ';': int c = 0; if (numAfter(i)) {
                    c = StringManipulator.getNumFromIndex(code, i);
                } if (c > 0) {i = StringManipulator.nthIndex(code, ']', i, c) + 1;} else if (c < 0) {
                    i = StringManipulator.nthIndexBackwards(code, '[', i, c);
                } i = MathUtils.boundsProtected(i, code.length()); continue outer;
                case ',': try {operand[ptr] = System.in.read();} catch (IOException e) {
                    setOutput("Input error: " + e.getMessage(), ERROR);
                } break; case '.': setOutput((char) operand[ptr] + "", OUTPUT); break;
                case '"': setOutput(operand[ptr] + "", OUTPUT); break; case '!': System.out.print(operand[ptr]); break;
                case '\\': setOutput("Program signalled HALT at " + i + "\n", ERROR); return;
                default: if (!numAfter(i)) {
                    setOutput("Unrecognized character at " + i + "\n", ERROR);
                }
            } i++;
        }
    }
    void setOutput(String output, int mode) {
        switch (mode) {
            case ERROR: this.errors += output; if (!silent) System.err.print(output); errorCount++; break;
            case OUTPUT: this.output += output; if (!silent) System.out.print(output); break;
        }
    }
    public int getErrorCount() {return errorCount;}
    public String getOutput() {return output;}
    public String getErrors() {return errors;}
    boolean fromInt(int val) {return val > 0;}
    int toInt(boolean val) {if (val) return 1; return 0;}
    int jumpIndex(int i, boolean relative) {
        if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
            if (relative) {i += StringManipulator.getNumFromIndex(code, i + 1);} else {
                i = StringManipulator.getNumFromIndex(code, i + 1);
            } i = indexAfterSkipLiteral(i, i); i = MathUtils.boundsProtected(i, code.length());
        } else {i = operand[ptr]; i = MathUtils.boundsProtected(i, code.length());} return i;
    }
    boolean numAfter(int i) {
        return ((i + 1 < code.length() - 1) && (Character.isDigit(code.charAt(i + 1)) || code.charAt(i + 1) == '_'));
    }
    int indexAfterSkipLiteral(int index, int literal) {
        return index += (literal + "").length();
    }
}