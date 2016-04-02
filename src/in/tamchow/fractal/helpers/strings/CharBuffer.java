package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
/**
 *
 */
public class CharBuffer implements CharSequence, Comparable<CharBuffer>, Serializable {
    private final char[] buffer;
    private int size;
    private String representationCache;
    public CharBuffer(int length) {
        buffer = new char[length];
        size = 0;
        //representationCache=toString();
    }
    public CharBuffer(String str) {
        buffer = new char[str.length()];
        str.getChars(0, buffer.length, buffer, 0);
        size = buffer.length;
        representationCache = toString();
    }
    @Override
    public int length() {
        return size;
    }
    @Override
    public char charAt(int index) {
        return buffer[MathUtils.boundsProtected(index, buffer.length)];
    }
    @Override
    public CharSequence subSequence(int start, int end) {
        int length = end - start;
        CharBuffer sub = new CharBuffer(length);
        System.arraycopy(buffer, start, sub.buffer, 0, length);
        return sub;
    }
    @Override
    public String toString() {
        if (representationCache != null) {
            return representationCache;
        }
        return new String(buffer);
    }
    public CharBuffer append(CharSequence csq) {
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
    public CharBuffer append(CharSequence csq, int start, int end) {
        return append(csq.subSequence(start, end));
    }
    public CharBuffer append(char c) {
        return append(String.valueOf(c));
    }
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof CharBuffer && toString().equals(o.toString());
    }
    public boolean equalsIgnoreCase(CharBuffer o) {
        return toString().equalsIgnoreCase(o.toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public int compareTo(CharBuffer o) {
        return toString().compareTo(o.toString());
    }
}