package in.tamchow.fractal.misc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
/**
 * A PBrain interpreter
 */
public class PBrainer {
    String code, codebackup;
    int[] procidx, operand;
    int ptr, proctr, itmp, size;
    public PBrainer() {
        code = "";
        codebackup = code;
        size = 65536;
        procidx = new int[size];
        operand = new int[size];
        for (int i = 0; i < operand.length; i++) {
            operand[i] = -1;
        }
        ptr = 0;
        proctr = 0;
        itmp = 0;
    }
    public static void main(String[] args) {
        PBrainer executor = new PBrainer();
        if (args.length == 0) {
            throw new IllegalArgumentException("Nothing to interpret");
        }
        if (args[0].equalsIgnoreCase("-s")) {
            executor.size = Integer.parseInt(args[1]);
            if (args[2].equalsIgnoreCase("-f")) {
                executor.readFile(args[3]);
            } else {
                executor.code = args[2];
            }
        } else {
            if (args[0].equalsIgnoreCase("-f")) {
                executor.readFile(args[1]);
            } else {
                executor.code = args[0];
            }
        }
        executor.execute();
    }
    void readFile(String path) {
        File input = new File(path);
        try {
            Scanner sc = new Scanner(input);
            while (sc.hasNextLine()) {
                code += sc.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void execute() {
        outer:
        for (int i = 0; i < code.length(); ) {
            switch (code.charAt(i)) {
                case '(':
                    procidx[proctr] = i + 1;
                    proctr++;
                    break;
                case '[':
                    if (code.indexOf(']') == -1) {
                        System.err.println("Unmatched [ at " + i);
                    }
                    if (operand[ptr] == 0) {
                        i = code.indexOf(']', i + 1);
                    }
                    continue outer;
                case ']':
                    if (code.indexOf('[') == -1) {
                        System.err.println("Unmatched ] at " + i);
                    }
                    i = code.lastIndexOf('[', i - 1);
                    continue outer;
                case ':':
                    codebackup = code;
                    itmp = codebackup.indexOf(')', procidx[operand[ptr]]) + 1;
                    if (itmp - 1 == -1) {
                        System.err.println("Unmatched ) at " + i);
                    }
                    code = codebackup.substring(procidx[operand[ptr]], codebackup.indexOf(')', itmp - 1));
                    execute();
                    i = itmp;
                    code = codebackup;
                    continue outer;
                case '<':
                    if (ptr - 1 < 0) {
                        ptr = size - 1;
                    } else {
                        --ptr;
                    }
                    break;
                case '>':
                    if (ptr + 1 > size) {
                        ptr = 0;
                    } else {
                        ++ptr;
                    }
                    break;
                case '+':
                    ++operand[ptr];
                    break;
                case '-':
                    --operand[ptr];
                    break;
                case ',':
                    try {
                        operand[ptr] = System.in.read();
                    } catch (IOException e) {
                        System.err.print("Input error: " + e.getMessage());
                    }
                    break;
                case '.':
                    System.out.print((char) operand[ptr]);
                    break;
            }
            i++;
        }
    }
}
