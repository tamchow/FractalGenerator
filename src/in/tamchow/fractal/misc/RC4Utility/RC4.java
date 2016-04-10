package in.tamchow.fractal.misc.RC4Utility;
import org.jetbrains.annotations.NotNull;
/**
 * Implements simple RC4 encryption/decryption
 */
public class RC4 {
    private int[] key, config;
    public RC4(@NotNull byte[] key) {
        initRC4(key);
    }
    public RC4() {
        initRC4(new byte[]{2, 4, 6, 8, 16, 32, 64});
    }
    private void initRC4(@NotNull byte[] key) {
        if (key.length == 0 || key.length > 256) {
            throw new IllegalArgumentException("Key length out of range");
        }
        this.key = new int[256];
        config = new int[key.length];
        populateKey();
    }
    private void populateKey() {
        for (int i = 0; i < key.length; i++) {
            key[i] = i;
        }
        int j = 0;
        for (int i = 0; i < key.length; i++) {
            j = ((j + key[i] + config[i % config.length]) % key.length);
            int tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
        }
    }
    @NotNull
    public String process(@NotNull String input) {
        @NotNull byte[] toprocess = new byte[input.length()];
        for (int i = 0; i < toprocess.length; i++) {
            toprocess[i] = (byte) ((int) input.charAt(i));
        }
        @NotNull byte[] processed = process(toprocess);
        @NotNull String output = "";
        for (byte aProcessed : processed) {
            output += (char) ((int) aProcessed);
        }
        return output;
    }
    @NotNull
    public byte[] process(@NotNull byte[] input) {
        @NotNull byte[] output = new byte[input.length];
        int i = 0, j = 0;
        for (int k = 0; k < input.length; k++) {
            i = (i + 1) % key.length;
            j = (j + key[i]) % key.length;
            int tmp = key[i];
            key[i] = key[j];
            key[j] = tmp;
            int m = key[(key[i] + key[j]) % key.length];
            output[k] = (byte) (input[k] ^ m);
        }
        return output;
    }
}