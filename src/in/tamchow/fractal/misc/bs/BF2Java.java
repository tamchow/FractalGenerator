package in.tamchow.fractal.misc.bs;
import in.tamchow.fractal.helpers.strings.CharBuffer;
import in.tamchow.fractal.helpers.strings.ResizableCharBuffer;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static in.tamchow.fractal.helpers.strings.StringManipulator.createRepeat;
import static in.tamchow.fractal.helpers.strings.StringManipulator.getRepeats;
/**
 * Compiles pure Brainfuck to Java
 * <br>
 * Available CLI switches:
 * <ol>
 *  <li>-s : Followed by integer specifying memory size.</li>
 *  <li>-f : Followed by absolute or relative path to input file. Extension does not matter.</li>
 *  <li>-i : Enables the optimizer.</li>
 *  <li>-o : Changes the output path to whatever follows it.</li>
 *  <li>-w : Enables the optimizer and allows for memory wrapping.</li>
 *  <li>-c : Adds comments representing the source.</li>
 *  <li>-t : Specifies the data type of the memory array (any Java primitive type works)</li>
 * </ol>
 * <br>
 * Except when using '-f', the program code must be the first argument in the list.
 *
 * @author Tamoghna Chowdhury
 * @version 1.2
 */
public class BF2Java {
    private String startBlock = "{",
            classHeader = "public class %s " + startBlock + "\n", endBlock = "}\n",
            mainHeader = "public static void main(String[] args) " + startBlock + "\n",
            initHeader = "private static %s[] mem = new %s[%d];\n",
            pointerHeader = "private static int ptr = 0;\n",
            getter = "private static %s get() {\nreturn mem[bounds()];\n}\n",
            setter = "private static void set(%s value) {\nmem[bounds()] = value;\n}\n",
            boundsProtect = "\tprivate static int bounds() {\n" +
                    "return (ptr < 0) ? Math.abs(mem.length + ptr) % mem.length : ((ptr >= mem.length) ? (ptr % mem.length) : ptr);\n}\n",
            addVal = "++mem[ptr];", subVal = "--mem[ptr];", addPtr = "++ptr;", subPtr = "--ptr;",
            addValOp = "mem[ptr] += %d;", subValOp = "mem[ptr] -= %d;", addPtrOp = "ptr +=%d;", subPtrOp = "ptr -= %d;",
            addValW = "set((%s) (get() + %d));", subValW = "set((%s) (get() - %d));",
            loop = "while(mem[ptr] != 0)" + startBlock,
            loopW = "while(get() != 0)" + startBlock,
            input = "mem[ptr] = (char) System.in.read();\n",
            output = "System.out.print((char) mem[ptr]);\n";
    public BF2Java(String programName, int memorySize, String memoryType) {
        classHeader = String.format(classHeader, programName);
        initHeader = String.format(initHeader, memoryType, memoryType, memorySize);
        getter = String.format(getter, memoryType);
        setter = String.format(setter, memoryType);
    }
    public static String compile(String programName, int memorySize, String memoryType, String BFProgram, boolean addComments) {
        BF2Java bf2j = new BF2Java(programName, memorySize, memoryType);
        CharBuffer builder = new ResizableCharBuffer();
        builder.append(bf2j.classHeader).append(bf2j.initHeader).append(bf2j.pointerHeader).append(bf2j.mainHeader);
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
            if (addComments) {
                builder.append("//").append(current).append("\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append(bf2j.endBlock).append(bf2j.endBlock);
        return builder.toString().trim();
    }
    public static String compileOptimize(String programName, int memorySize, String memoryType, String BFProgram, boolean addComments) {
        if (BFProgram.indexOf('<') >= 0 && BFProgram.indexOf('>') >= 0 && BFProgram.indexOf('<') < BFProgram.indexOf('>')) {
            return compileWrapOptimize(programName, memorySize, memoryType, BFProgram, addComments);
        }
        BF2Java bf2j = new BF2Java(programName, memorySize, memoryType);
        CharBuffer builder = new ResizableCharBuffer();
        builder.append(bf2j.classHeader).append(bf2j.initHeader).append(bf2j.pointerHeader).append(bf2j.mainHeader);
        outer:
        for (int i = 0; i < BFProgram.length(); ) {
            char current = BFProgram.charAt(i);
            int count = 0;
            switch (current) {
                case '+':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.addVal);
                    } else {
                        builder.append(String.format(bf2j.addValOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '-':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.subVal);
                    } else {
                        builder.append(String.format(bf2j.subValOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '>':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.addPtr);
                    } else {
                        builder.append(String.format(bf2j.addPtrOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '<':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.subPtr);
                    } else {
                        builder.append(String.format(bf2j.subPtrOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '[':
                    builder.append(bf2j.loop);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case ']':
                    builder.append(bf2j.endBlock);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case '.':
                    builder.append(bf2j.output);
                    if (addComments) {
                        builder.subBuffer(0, builder.length() - 1).append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case ',':
                    builder.append(bf2j.input);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                default:
                    break;//spec says anything else is a comment, so ignore it
            }
            ++i;
        }
        builder.append(bf2j.endBlock).append(bf2j.endBlock);
        return builder.toString().trim();
    }
    public static String compileWrapOptimize(String programName, int memorySize, String memoryType, String BFProgram, boolean addComments) {
        BF2Java bf2j = new BF2Java(programName, memorySize, memoryType);
        CharBuffer builder = new ResizableCharBuffer();
        builder.append(bf2j.classHeader).append(bf2j.initHeader).append(bf2j.pointerHeader).append(bf2j.getter).append(bf2j.setter).append(bf2j.boundsProtect).append(bf2j.mainHeader);
        outer:
        for (int i = 0; i < BFProgram.length(); ) {
            char current = BFProgram.charAt(i);
            int count = 0;
            switch (current) {
                case '+':
                    count = getRepeats(BFProgram, i);
                    builder.append(String.format(bf2j.addValW, memoryType, count));
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '-':
                    count = getRepeats(BFProgram, i);
                    builder.append(String.format(bf2j.subValW, memoryType, count));
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '>':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.addPtr);
                    } else {
                        builder.append(String.format(bf2j.addPtrOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '<':
                    count = getRepeats(BFProgram, i);
                    if (count == 1) {
                        builder.append(bf2j.subPtr);
                    } else {
                        builder.append(String.format(bf2j.subPtrOp, count));
                    }
                    if (addComments) {
                        builder.append("//").append(createRepeat(current, count)).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    i += count;
                    continue outer;
                case '[':
                    builder.append(bf2j.loopW);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case ']':
                    builder.append(bf2j.endBlock);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case '.':
                    builder.append(bf2j.output);
                    if (addComments) {
                        builder.subBuffer(0, builder.length() - 1).append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                case ',':
                    builder.append(bf2j.input);
                    if (addComments) {
                        builder.append("//").append(current).append("\n");
                    } else {
                        builder.append("\n");
                    }
                    break;
                default:
                    break;//spec says anything else is a comment, so ignore it
            }
            ++i;
        }
        builder.append(bf2j.endBlock).append(bf2j.endBlock);
        return builder.toString().trim();
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Nothing to do!");
        }
        int size = 30000;
        String name = "BFProgram", compiled = "", output = name + ".java", code = args[0], type = "byte";
        boolean addComments = false;
        List<String> argList = Arrays.asList(args);
        int oIndex = argList.indexOf("-o"),
                fIndex = argList.indexOf("-f"),
                sIndex = argList.indexOf("-s"),
                tIndex = argList.indexOf("-t");
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
            size = Integer.parseInt(argList.get(sIndex + 1));
        }
        if (oIndex >= 0) {
            output = argList.get(oIndex + 1);
        }
        if (tIndex >= 0) {
            type = argList.get(tIndex + 1);
        }
        if (argList.contains("-c")) {
            addComments = true;
        }
        if (argList.contains("-i")) {
            compiled = compileOptimize(name, size, type, code, addComments);
        } else if (argList.contains("-w")) {
            compiled = compileWrapOptimize(name, size, type, code, addComments);
        } else {
            compiled = compile(name, size, type, code, addComments);
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