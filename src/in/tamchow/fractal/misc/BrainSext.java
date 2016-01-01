package in.tamchow.fractal.misc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
/**
 * Interpreter for custom BFk-family language christened "BrainSext"(raw pun and acronym for "Brains Extended",
 * capable of interpreting PBrain, BrainFlow, pure BFk (directly),
 * command sets from Extended BFk,and  a set of extended behaviour from PointerLang
 * <a href="http://codegolf.stackexchange.com/questions/52277/write-an-interpreter-for-my-new-esoteric-language-pointerlang?rq=1">here</a>>
 * (with replacement by equivalent commands:
 */
public class BrainSext {
    String code, codebackup;
    int[] procidx, operand;
    int ptr, proctr, itmp, size;
    private int storage;
    public BrainSext() {
        code = ""; codebackup = code; size = 65536; procidx = new int[size]; initMemory(); ptr = 0; proctr = 0;
        itmp = 0; storage = -1;
    }
    void initMemory() {operand = new int[size]; for (int i = 0; i < operand.length; i++) {operand[i] = -1;}}
    public static void main(String[] args) {
        BrainSext executor = new BrainSext();
        if (args.length == 0) {throw new IllegalArgumentException("Nothing to interpret");}
        if (args[0].equalsIgnoreCase("-s")) {
            executor.size = Integer.parseInt(args[1]);
            if (args[2].equalsIgnoreCase("-f")) {executor.readFile(args[3]);} else {executor.code = args[2];}
        } else {
            if (args[0].equalsIgnoreCase("-f")) {
                executor.readFile(args[1]);
            } else {executor.code = args[0];}
        } executor.execute();
    }
    void readFile(String path) {
        File input = new File(path); try {
            Scanner sc = new Scanner(input); while (sc.hasNextLine()) {code += sc.nextLine();}
        } catch (FileNotFoundException e) {System.err.println("Input File location " + path + " is invalid.");}
    }
    public void execute() {
        int[] tmp = new int[operand.length];
        //filter comments first
        while (code.contains("{") && code.contains("}")) {
            int i1 = code.indexOf('{'), i2 = code.indexOf('}'); code = code.replace(code.substring(i1, i2 + 1), "");
        } outer:
        for (int i = 0; i < code.length(); ) {
            switch (code.charAt(i)) {
                case 'm': if (numAfter(i)) {
                    size = StringManipulator.getNumFromIndex(code, i + 1);
                } else {size = code.length();} initMemory(); for (int j = 0; j < code.length(); j++) {
                    operand[j] = code.charAt(j);
                } break; case 'u': for (int j = 0; j < code.length() && j < operand.length; j++) {
                    code += "" + (char) operand[j];
                } initMemory(); continue outer; case '(': procidx[proctr] = i + 1; proctr++; break;
                case '[': if (StringManipulator.findMatchingCloser('[', code, i - 1) == -1) {
                    System.err.println("Unmatched [ at " + i);
                } if (operand[ptr] == 0) {
                    i = StringManipulator.findMatchingCloser('[', code, i + 1) + 1;
                } continue outer; case ']': if (StringManipulator.findMatchingOpener(']', code, i - 1) == -1) {
                    System.err.println("Unmatched ] at " + i);
                } i = StringManipulator.findMatchingOpener(']', code, i - 1) + 1; continue outer;
                case ':': codebackup = code; itmp = StringManipulator.findMatchingCloser('(', codebackup, procidx[operand[ptr]]); if (itmp == -1) {
                    System.err.println("Unmatched ) for procedure call at " + i);
                } code = codebackup.substring(procidx[operand[ptr]], itmp - 1); execute(); i = itmp; code = codebackup; continue outer;
                case 'C': operand[ptr] = operand[ptr] + "".charAt(0);
                case 'I': operand[ptr] = Integer.valueOf("" + (char) operand[ptr]);
                case '@': i = jumpIndex(i); continue outer;
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
                case 'E': if (operand[ptr] == storage) {break;} else {i = jumpIndex(i); continue outer;}
                case 'N': if (!(operand[ptr] == storage)) {break;} else {i = jumpIndex(i); continue outer;}
                case 'G': if (operand[ptr] > storage) {break;} else {i = jumpIndex(i); continue outer;}
                case 'L': if (operand[ptr] < storage) {break;} else {i = jumpIndex(i); continue outer;}
                case '<': if (numAfter(i)) {
                    ptr -= StringManipulator.getNumFromIndex(code, i + 1); ptr = boundsProtected(ptr, size);
                } else {if (ptr - 1 < 0) {ptr = size - 1;} else {--ptr;}} break; case '>': if (numAfter(i)) {
                    ptr += StringManipulator.getNumFromIndex(code, i + 1); ptr = boundsProtected(ptr, size);
                } else {if (ptr + 1 >= size) {ptr = size - 1;} else {--ptr;}} break; case '+': if (numAfter(i)) {
                    operand[ptr] += StringManipulator.getNumFromIndex(code, i + 1);
                } else {++operand[ptr];} break; case '-': if (numAfter(i)) {
                    operand[ptr] -= StringManipulator.getNumFromIndex(code, i + 1);
                } else {--operand[ptr];} break; case '=': if (numAfter(i)) {
                    operand[ptr] = StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] = ptr;} break; case '*': if (numAfter(i)) {
                    operand[ptr] *= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] *= 1;} break; case '/': if (numAfter(i)) {
                    operand[ptr] /= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] /= 1;} break; case '%': if (numAfter(i)) {
                    operand[ptr] %= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] %= 1;} break;
                case '^': ptr = operand[ptr]; ptr = boundsProtected(ptr, size); break;
                case '&': operand[ptr] = boundsProtected(operand[ptr], size); operand[ptr] = operand[operand[ptr]]; break;
                case ';': int c = 0; if (numAfter(i)) {
                    c = StringManipulator.getNumFromIndex(code, i);
                } if (c > 0) {i = StringManipulator.nthIndex(code, ']', i, c) + 1;} else if (c < 0) {
                    i = StringManipulator.nthIndexBackwards(code, '[', i, c);
                } i = boundsProtected(i, code.length()); continue outer;
                case ',': try {operand[ptr] = System.in.read();} catch (IOException e) {
                    System.err.print("Input error: " + e.getMessage());
                } break; case '.': System.out.print((char) operand[ptr]); break;
                case '"': System.out.print(operand[ptr]); break; case '!': System.out.print(operand[ptr]); break;
                case 'h': System.err.println("Program signalled HALT at " + i); return; default: if (!numAfter(i)) {
                    System.err.println("Unrecognized character at " + i);
                }
            } i++;
        }
    }
    int boundsProtected(int ptr, int size) {
        if (ptr < 0) {ptr = size + ptr;} if (ptr >= size) {
            while (ptr >= size) {ptr = ptr - size;}
        } return ptr;
    }
    boolean fromInt(int val) {return val > 0;}
    int toInt(boolean val) {if (val) return 1; return 0;}
    int jumpIndex(int i) {
        if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
            i = StringManipulator.getNumFromIndex(code, i + 1); i = boundsProtected(i, code.length());
        } else {i = operand[ptr]; i = boundsProtected(i, code.length());} return i;
    }
    boolean numAfter(int i) {
        return ((i + 1 < code.length() - 1) && (Character.isDigit(code.charAt(i + 1)) || code.charAt(i + 1) == '_'));
    }
}