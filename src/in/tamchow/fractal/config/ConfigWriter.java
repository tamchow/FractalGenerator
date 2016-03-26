package in.tamchow.fractal.config;

import java.io.*;

/**
 * Writes configuration files from configuration objects in human-readable or serialized format
 */
public class ConfigWriter {
    public static void writeHumanReadable(Config config, File output) {
        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(config + "");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSerialized(Config config, File output) {
        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
            writer.writeObject(config);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}