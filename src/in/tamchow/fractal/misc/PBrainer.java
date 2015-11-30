package in.tamchow.fractal.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * A PBrain interpreter
 */
public class PBrainer {
    private static final int SIZE = 65536;
    String code, codebackup;
    int[] procidx, operand;
    int ptr, proctr, itmp;

    public PBrainer() {
        code = "";
        codebackup = code;
        procidx = new int[SIZE];
        operand = new int[SIZE];
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
        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("File not found, interpreting input");
            executor.code = args[0];
        }
        try {
            Scanner sc = new Scanner(input);
            while (sc.hasNextLine()) {
                executor.code += sc.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        executor.execute();
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
                    if (operand[ptr] == 0) {
                        i = code.indexOf(']', i + 1);
                    }
                    continue outer;
                case ']':
                    i = code.lastIndexOf('[', i - 1);
                    continue outer;
                case ':':
                    itmp = i + 1;
                    codebackup = code;
                    code = codebackup.substring(procidx[operand[ptr]], codebackup.indexOf(')', procidx[operand[ptr]]));
                    execute();
                    i = itmp;
                    code = codebackup;
                    continue outer;
                case '<':
                    if (ptr - 1 < 0) {
                        ptr = 0;
                    } else {
                        --ptr;
                    }
                    break;
                case '>':
                    if (ptr + 1 > SIZE) {
                        ptr = SIZE;
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
                        e.printStackTrace();
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
