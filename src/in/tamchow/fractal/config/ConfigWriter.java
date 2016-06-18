package in.tamchow.fractal.config;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.io.*;
/**
 * Writes configuration files from configuration objects in human-readable or serialized format
 */
public class ConfigWriter {
    public static void writeConfig(BatchContainer<Config> config, @NotNull File output, boolean humanReadable) {
        try {
            if (!output.exists()) {
                if (!output.createNewFile()) {
                    throw new IOException("Could not create file: " + output);
                }
            }
            if (humanReadable) {
                @NotNull BufferedWriter writer = new BufferedWriter(new FileWriter(output));
                writer.write(config.toString());
                writer.flush();
                writer.close();
            } else {
                @NotNull ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
                writer.writeObject(config);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}