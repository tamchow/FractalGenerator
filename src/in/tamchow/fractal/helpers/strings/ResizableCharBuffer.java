package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * A resizeable version of {@link CharBuffer}
 */
public class ResizableCharBuffer extends CharBuffer {
    public static final int RESIZE_FACTOR = 2;
    public ResizableCharBuffer() {
        super();
    }
    public ResizableCharBuffer(int capacity) {
        super(capacity);
    }
    public ResizableCharBuffer(@NotNull String str) {
        super(str);
    }
    public void resize(int newSize) {
        char[] tmp = new char[buffer.length];
        System.arraycopy(buffer, 0, tmp, 0, tmp.length);
        buffer = new char[newSize];
        System.arraycopy(tmp, 0, buffer, 0, tmp.length);
    }
    @Override
    public CharBuffer append(CharSequence csq) {
        if (size + csq.length() >= buffer.length) {
            resize(buffer.length + (2 * csq.length()));
        }
        return super.append(csq);
    }
}