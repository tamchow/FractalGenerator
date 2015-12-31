package in.tamchow.fractal.misc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
/**
 * A (extended) BFk family interpreter, capable of interpreting PBrain, BrainFlow, pure BFk, and  a set of extended behaviour from PointerLang <a href="http://codegolf.stackexchange.com/questions/52277/write-an-interpreter-for-my-new-esoteric-language-pointerlang?rq=1">here</a>>:
 */
public class BFker {
    String code, codebackup;
    int[] procidx, operand;
    int ptr, proctr, itmp, size;
    public BFker() {
        code = ""; codebackup = code; size = 65536; procidx = new int[size]; operand = new int[size];
        for (int i = 0; i < operand.length; i++) {operand[i] = -1;} ptr = 0; proctr = 0; itmp = 0;
    }
    public static void main(String[] args) {
        BFker executor = new BFker();
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
    public void execute() {//filter comments first
        while (code.contains("{") && code.contains("}")) {
            int i1 = code.indexOf('{'), i2 = code.indexOf('}'); code = code.replace(code.substring(i1, i2 + 1), "");
        } outer:
        for (int i = 0; i < code.length(); ) {
            switch (code.charAt(i)) {
                case '(': procidx[proctr] = i + 1; proctr++; break;
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
                case '<': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    ptr -= StringManipulator.getNumFromIndex(code, i + 1); if (ptr < 0) {ptr = size + ptr;}
                    if (ptr >= size) {
                        while (ptr >= size) {ptr = ptr - size;}
                    }
                } else {if (ptr - 1 < 0) {ptr = size - 1;} else {--ptr;}} break;
                case '>': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    ptr += StringManipulator.getNumFromIndex(code, i + 1); if (ptr < 0) {ptr = size + ptr;}
                    if (ptr >= size) {
                        while (ptr >= size) {ptr = ptr - size;}
                    }
                } else {if (ptr + 1 >= size) {ptr = size - 1;} else {--ptr;}} break;
                case '+': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] += StringManipulator.getNumFromIndex(code, i + 1);
                } else {++operand[ptr];} break;
                case '-': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] -= StringManipulator.getNumFromIndex(code, i + 1);
                } else {--operand[ptr];} break;
                case '=': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] = StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] = ptr;} break;
                case '*': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] *= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] *= 1;} break;
                case '/': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] /= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] /= 1;} break;
                case '%': if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    operand[ptr] %= StringManipulator.getNumFromIndex(code, i + 1);
                } else {operand[ptr] %= 1;} break;
                case '^': ptr = operand[ptr]; if (ptr < 0) {ptr = size + ptr;} if (ptr >= size) {
                    while (ptr >= size) {ptr = ptr - size;}
                } break;
                case '&': if (operand[ptr] < 0) {operand[ptr] = size + operand[ptr];} if (operand[ptr] >= size) {
                    while (operand[ptr] >= size) {operand[ptr] = operand[ptr] - size;}
                } operand[ptr] = operand[operand[ptr]]; break;
                case ';': int c = 0; if ((i + 1 < code.length() - 1) && Character.isDigit(code.charAt(i + 1))) {
                    c = StringManipulator.getNumFromIndex(code, i);
                } if (c > 0) {ptr = StringManipulator.nthIndex(code, ']', i, c) + 1;} else if (c < 0) {
                    ptr = StringManipulator.nthIndexBackwards(code, '[', i, c);
                } if (ptr < 0) {ptr = size + ptr;} if (ptr >= size) {
                    while (ptr >= size) {ptr = ptr - size;}
                } break; case ',': try {operand[ptr] = System.in.read();} catch (IOException e) {
                    System.err.print("Input error: " + e.getMessage());
                } break; case '.': System.out.print((char) operand[ptr]); break;
                case '!': System.out.print(operand[ptr]); break;
            } i++;
        }
    }
}