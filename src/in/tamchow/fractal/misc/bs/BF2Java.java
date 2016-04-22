package in.tamchow.fractal.misc.bs;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
/**
 * Compiles pure Brainfuck to Java
 *
 * Available CLI switches:
 * 1. -s : Followed by integer specifying memory size.
 * 2. -f : Followed by absolute or relative path to input file. Extension does not matter.
 * 3. -i : Enables the optimizer.
 * 4. -o : Changes the output path to whatever follows it.
 *
 * Except when using '-f', the program code must be the first argument in the list.
 *
 * @author Tamoghna Chowdhury
 * @version 1.1
 */
public class BF2Java {
    private String startBlock = "{\n",
            classHeader = "public class %s " + startBlock, endBlock = "}\n",
            mainHeader = "public static void main(String[] args) " + startBlock,
            initHeader = "byte[] mem = new byte[%d];\nint ptr = 0;\n",
            addVal = "++mem[ptr];\n", subVal = "--mem[ptr];\n", addPtr = "++ptr;\n", subPtr = "--ptr;\n",
            addValOp = "mem[ptr]+=%d;\n", subValOp = "mem[ptr]-=%d;\n", addPtrOp = "ptr+=%d;\n", subPtrOp = "ptr-=%d;\n",
            loop = "while(mem[ptr] != 0)" + startBlock, input = "mem[ptr] = (char) System.in.read();\n",
            output = "System.out.print((char) mem[ptr]);\n";
    public BF2Java(String programName, int memorySize) {
        classHeader = String.format(classHeader, programName);
        initHeader = String.format(initHeader, memorySize);
    }
    public static String compileBFtoJava(String programName, int memorySize, String BFProgram) {
        BF2Java bf2j = new BF2Java(programName, memorySize);
        StringBuilder builder = new StringBuilder();
        builder.append(bf2j.classHeader).append(bf2j.mainHeader).append(bf2j.initHeader);
        for (int i = 0; i < BFProgram.length(); ++i) {
            char current = BFProgram.charAt(i);
            switch (current) {
                case '+':
                    builder.append(bf2j.addVal);
                    break;
                case '-':
                    builder.append(bf2j.subVal);
                    break;
                case '>':
                    builder.append(bf2j.addPtr);
                    break;
                case '<':
                    builder.append(bf2j.subPtr);
                    break;
                case '[':
                    builder.append(bf2j.loop);
                    break;
                case ']':
                    builder.append(bf2j.endBlock);
                    break;
                case '.':
                    builder.append(bf2j.output);
                    break;
                case ',':
                    builder.append(bf2j.input);
                    break;
                default:
                    break;//spec says anything else is a comment, so ignore it
            }
        }
        builder.append(bf2j.endBlock).append(bf2j.endBlock);
        return builder.toString().trim();
    }
    public static String optimizeBFtoJava(String programName, int memorySize, String BFProgram) {
        BF2Java bf2j = new BF2Java(programName, memorySize);
        StringBuilder builder = new StringBuilder();
        builder.append(bf2j.classHeader).append(bf2j.mainHeader).append(bf2j.initHeader);
        outer:
        for (int i = 0; i < BFProgram.length(); ) {
            char current = BFProgram.charAt(i);
            int count = 0;
            switch (current) {
                case '+':
                    count = getCharRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.addVal);
                    } else {
                        builder.append(String.format(bf2j.addValOp, count));
                    }
                    i += count;
                    continue outer;
                case '-':
                    count = getCharRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.subVal);
                    } else {
                        builder.append(String.format(bf2j.subValOp, count));
                    }
                    i += count;
                    continue outer;
                case '>':
                    count = getCharRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.addPtr);
                    } else {
                        builder.append(String.format(bf2j.addPtrOp, count));
                    }
                    i += count;
                    continue outer;
                case '<':
                    count = getCharRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.subPtr);
                    } else {
                        builder.append(String.format(bf2j.subPtrOp, count));
                    }
                    i += count;
                    continue outer;
                case '[':
                    builder.append(bf2j.loop);
                    break;
                case ']':
                    builder.append(bf2j.endBlock);
                    break;
                case '.':
                    builder.append(bf2j.output);
                    break;
                case ',':
                    builder.append(bf2j.input);
                    break;
                default:
                    break;//spec says anything else is a comment, so ignore it
            }
            ++i;
        }
        builder.append(bf2j.endBlock).append(bf2j.endBlock);
        return builder.toString().trim();
    }
    private static int getCharRepeats(String str, int idx) {
        int ctr = 1;
        char current = str.charAt(idx++);
        while (idx < str.length() && current == str.charAt(idx)) {
            ++ctr;
            ++idx;
        }
        return ctr;
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Nothing to do!");
        }
        int size = 30000;
        String name = "BFProgram", compiled = "", output = name + ".java", code = args[0];
        List<String> argList = Arrays.asList(args);
        int oIndex = argList.indexOf("-o"),
                fIndex = argList.indexOf("-f"),
                sIndex = argList.indexOf("-s");
        if (fIndex >= 0) {
            File inputFile = new File(argList.get(fIndex + 1));
            String inputPath = inputFile.getAbsolutePath(), inputFileName = inputFile.getName();
            name = inputFileName.substring(0, inputFileName.lastIndexOf("."));
            try {
                StringBuilder raw = new StringBuilder();
                Scanner sc = new Scanner(inputFile);
                while (sc.hasNextLine()) {
                    raw.append(sc.nextLine());
                }
                code = raw.toString();
            } catch (FileNotFoundException fnfe) {
                System.err.println("The file path: " + inputPath + " is incorrect, or does not exist");
            }
            output = inputPath.substring(0, inputPath.lastIndexOf(".")) + ".java";
        }
        if (sIndex >= 0) {
            size = Integer.valueOf(argList.get(sIndex + 1));
        }
        if (oIndex >= 0) {
            output = argList.get(oIndex + 1);
        }
        if (argList.contains("-i")) {
            compiled = optimizeBFtoJava(name, size, code);
        } else {
            compiled = compileBFtoJava(name, size, code);
        }
        System.out.println(compiled);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(compiled);
            writer.flush();
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}