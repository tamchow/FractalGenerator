package in.tamchow.fractal.misc.RC4Utility;

import java.io.*;
import java.util.ArrayList;

/**
 * Does file encryption/decryption using the RC4 class
 */
public class EncryptDecryptFile {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No File Specified");
        }
        byte[] key = null;
        if (args.length == 2) {
            String[] keys = args[1].split(",");
            key = new byte[keys.length];
            for (int i = 0; i < key.length; i++) {
                key[i] = Byte.valueOf(keys[i]);
            }
        }
        File input = new File(args[0]);
        if (!input.exists()) {
            throw new FileNotFoundException("Specified file does not exist");
        }
        File output;
        if (input.getName().contains("_encrypted")) {
            output = new File(input.getCanonicalPath().substring(0, input.getCanonicalPath().lastIndexOf('_')));
            if (output.exists()) {
                output.delete();
            }
        } else {
            output = new File(input.getCanonicalPath() + "_encrypted");
        }
        DataInputStream dis = new DataInputStream(new FileInputStream(input));
        ArrayList<Byte> intermediate = new ArrayList<>();
        while (dis.available() > 0) {
            intermediate.add(dis.readByte());
        }
        dis.close();
        byte[] toprocess = new byte[intermediate.size()];
        for (int i = 0; i < toprocess.length; i++) {
            toprocess[i] = intermediate.get(i);
        }
        RC4 manip = new RC4();
        if (key != null) {
            manip = new RC4(key);
        }
        byte[] processed = manip.process(toprocess);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(output));
        dos.write(processed, 0, processed.length);
        dos.flush();
        dos.close();
    }
}