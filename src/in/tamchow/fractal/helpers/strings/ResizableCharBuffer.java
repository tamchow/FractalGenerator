package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * A resizeable version of {@link CharBuffer}
 */
@SuppressWarnings("WeakerAccess")
public final class ResizableCharBuffer extends CharBuffer {
    private static final float RESIZE_FACTOR = 2.0f;
    private float resizeFactor;
    public ResizableCharBuffer() {
        super();
    }
    public ResizableCharBuffer(int capacity) {
        this(RESIZE_FACTOR, capacity);
    }
    public ResizableCharBuffer(float resizeFactor, int capacity) {
        super(capacity);
        setResizeFactor(resizeFactor);
    }
    public ResizableCharBuffer(@NotNull String str) {
        super(str);
    }
    public ResizableCharBuffer(@NotNull CharBuffer buffer) {
        super(buffer);
    }
    public ResizableCharBuffer(@NotNull CharSequence csq) {
        super(csq);
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
            resize(buffer.length + Math.round(resizeFactor * csq.length()));
        }
        return super.append(csq);
    }
    public float getResizeFactor() {
        return resizeFactor;
    }
    public void setResizeFactor(final float resizeFactor) {
        this.resizeFactor = resizeFactor;
    }
}