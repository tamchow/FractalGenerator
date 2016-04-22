package in.tamchow.fractal.misc.bs;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
/**
 * Compiles pure Brainfuck to Java
 *
 * @author Tamoghna Chowdhury
 * @version 1.0
 */
public class BF2Java {
    private String startBlock = "{\n",
            classHeader = "public class %s " + startBlock, endBlock = "}\n",
            mainHeader = "public static void main(String[] args) " + startBlock,
            initHeader = "byte[] mem = new byte[%d];\nint ptr = 0;\n",
            addVal = "++mem[ptr];\n", subVal = "--mem[ptr];\n", addPtr = "++ptr;\n", subPtr = "--ptr;\n",
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
            }
        }
        builder.append(bf2j.endBlock).append("\n").append(bf2j.endBlock);
        return builder.toString();
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
            String input = new File(argList.get(fIndex + 1)).getAbsolutePath();
            name = input.substring(input.lastIndexOf("/") + 1, input.lastIndexOf("."));
            try {
                code = new Scanner(new File(input)).nextLine();
            } catch (FileNotFoundException fnfException) {
                System.err.println("The file path: " + input + " is incorrect, or does not exist");
            }
            output = input.substring(0, input.lastIndexOf(".")) + ".java";
        }
        if (sIndex >= 0) {
            size = Integer.valueOf(argList.get(sIndex + 1));
        }
        if (oIndex >= 0) {
            output = argList.get(oIndex + 1);
        }
        compiled = compileBFtoJava(name, size, code);
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