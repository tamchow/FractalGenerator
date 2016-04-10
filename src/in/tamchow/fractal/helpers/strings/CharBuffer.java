package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.math.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
/**
 * No-nonsense Character Buffer which is faster than StringBuilder.
 * Does not cache String representation for performance reasons.
 */
public class CharBuffer implements CharSequence, Comparable<CharBuffer>, Serializable {
    @NotNull
    private final char[] buffer;
    private int size;
    public CharBuffer(int length) {
        buffer = new char[length];
        size = 0;
    }
    public CharBuffer(@NotNull String str) {
        buffer = new char[str.length()];
        str.getChars(0, buffer.length, buffer, 0);
        size = buffer.length;
    }
    @Override
    public int length() {
        return size;
    }
    @Override
    public char charAt(int index) {
        return buffer[MathUtils.boundsProtected(index, buffer.length)];
    }
    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        int length = end - start;
        @NotNull CharBuffer sub = new CharBuffer(length);
        System.arraycopy(buffer, start, sub.buffer, 0, length);
        return sub;
    }
    @NotNull
    @Override
    public String toString() {
        return new String(buffer);
    }
    @NotNull
    public CharBuffer append(@NotNull CharSequence csq) {
        int csq_length = csq.length(), end = size + csq_length;
        if (csq instanceof String) {
            ((String) csq).getChars(0, csq_length, buffer, size);
        } else {
            for (int i = size; i < end; ++i) {
                buffer[i] = csq.charAt(i);
            }
        }
        size = end;
        return this;
    }
    @NotNull
    public CharBuffer append(@NotNull CharSequence csq, int start, int end) {
        return append(csq.subSequence(start, end));
    }
    @NotNull
    public CharBuffer append(char c) {
        return append(String.valueOf(c));
    }
    @Override
    public boolean equals(@Nullable Object o) {
        return o != null && o instanceof CharBuffer && toString().equals(o.toString());
    }
    public boolean equalsIgnoreCase(@NotNull CharBuffer o) {
        return toString().equalsIgnoreCase(o.toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public int compareTo(@NotNull CharBuffer o) {
        return toString().compareTo(o.toString());
    }
}