package in.tamchow.fractal.misc.RC4Utility;
/**
 * Implements simple RC4 encryption/decryption
 */
public class RC4 {
    private int[] key, config;
    public RC4(byte[] key) {
        initRC4(key);
    }
    private void initRC4(byte[] key) {
        if (key.length == 0 || key.length > 256) {
            throw new IllegalArgumentException("Key length out of range");
        } this.key = new int[256]; config = new int[key.length]; populateKey();
    }
    private void populateKey() {
        for (int i = 0; i < key.length; i++) {key[i] = i;} int j = 0; for (int i = 0; i < key.length; i++) {
            j = ((j + key[i] + config[i % config.length]) % key.length); int tmp = key[i]; key[i] = key[j];
            key[j] = tmp;
        }
    }
    public RC4() {initRC4(new byte[]{2, 4, 6, 8, 16, 32, 64});}
    public String process(String input) {
        byte[] toprocess = new byte[input.length()];
        for (int i = 0; i < toprocess.length; i++) {toprocess[i] = (byte) ((int) input.charAt(i));}
        byte[] processed = process(toprocess); String output = "";
        for (byte aProcessed : processed) {output += (char) ((int) aProcessed);} return output;
    }
    public byte[] process(byte[] input) {
        byte[] output = new byte[input.length]; int i = 0, j = 0; for (int k = 0; k < input.length; k++) {
            i = (i + 1) % key.length; j = (j + key[i]) % key.length; int tmp = key[i]; key[i] = key[j]; key[j] = tmp;
            int m = key[(key[i] + key[j]) % key.length]; output[k] = (byte) (input[k] ^ m);
        } return output;
    }
}