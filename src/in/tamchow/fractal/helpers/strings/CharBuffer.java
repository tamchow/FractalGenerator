package in.tamchow.fractal.helpers.strings;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
/**
 * Little-nonsense Character Buffer which is faster than StringBuilder.
 * Does not cache String representation for performance reasons.
 */
public class CharBuffer implements CharSequence, Comparable<CharBuffer>, Serializable, Cloneable {
    public static final int DEFAULT_CAPACITY = 250;
    @NotNull
    protected char[] buffer;
    protected int size;
    public CharBuffer() {
        this(DEFAULT_CAPACITY);
    }
    public CharBuffer(int capacity) {
        buffer = new char[capacity];
        size = 0;
    }
    public CharBuffer(@NotNull String str) {
        buffer = new char[str.length()];
        str.getChars(0, buffer.length, buffer, 0);
        size = buffer.length;
    }
    public CharBuffer(@NotNull CharBuffer other) {
        buffer = new char[other.length()];
        System.arraycopy(other.buffer, 0, buffer, 0, other.length());
        size = buffer.length;
    }
    public CharBuffer(char value[], int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }
        this.buffer = copyOfRange(value, offset, offset + count);
    }
    public CharBuffer(CharSequence csq) {
        buffer = new char[csq.length()];
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = csq.charAt(i);
        }
        size = buffer.length;
    }
    @NotNull
    public static Character[] box(@NotNull char[] chars) {
        @NotNull Character[] boxed = new Character[chars.length];
        for (int i = 0; i < boxed.length; ++i) {
            boxed[i] = chars[i];
        }
        return boxed;
    }
    @NotNull
    public static char[] unBox(@NotNull Character[] chars) {
        @NotNull char[] unBoxed = new char[chars.length];
        for (int i = 0; i < unBoxed.length; ++i) {
            unBoxed[i] = chars[i];
        }
        return unBoxed;
    }
    @NotNull
    private static char[] copyOfRange(@NotNull char[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        char[] copy = new char[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }
    @Override
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return new CharBuffer(this);
    }
    @Override
    public int length() {
        return size;
    }
    @Override
    public char charAt(int index) {
        //Note: Wraps indices so that no *IndexOutOfBoundsException s are thrown.
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
        return new String(buffer, 0, size);
    }
    @NotNull
    public CharBuffer append(@NotNull Object obj) {
        return append(obj.toString());
    }
    @NotNull
    public CharBuffer append(@NotNull CharSequence csq) {
        //size = calculateSize();
        int csq_length = csq.length(), end = size + csq_length;
        if (end > buffer.length) {
            throw new StringIndexOutOfBoundsException(end - buffer.length);
        }
        if (csq instanceof String) {
            ((String) csq).getChars(0, csq_length, buffer, size);
        } else if (csq instanceof CharBuffer) {
            System.arraycopy(((CharBuffer) csq).buffer, 0, buffer, size, csq_length);
        } else {
            for (int i = size; i < end; ++i) {
                buffer[i] = csq.charAt(i - size);
            }
        }
        size = end;
        return this;
    }
    @NotNull
    public char[] toCharArray() {
        char[] tmp = new char[size];
        System.arraycopy(buffer, 0, tmp, 0, size);
        return tmp;
    }
    private int calculateSize() {
        int ctr = 0;
        while (ctr < buffer.length && buffer[ctr] != '\0') {
            ++ctr;
        }
        return ctr;
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
        return o == this || (o != null && o instanceof CharBuffer && toString().equals(o.toString()));
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
        return (equals(o)) ? 0 : toString().compareTo(o.toString());
    }
    @NotNull
    public CharBuffer trim() {
        int len = buffer.length;
        int st = 0;
        char[] val = buffer;
        while ((st < len) && (Character.isWhitespace(val[st]))) {
            st++;
        }
        while ((st < len) && (Character.isWhitespace(val[len - 1]))) {
            len--;
        }
        return ((st > 0) || (len < buffer.length)) ? subBuffer(st, len) : this;
    }
    @NotNull
    public CharBuffer subBuffer(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > buffer.length) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return ((beginIndex == 0) && (endIndex == buffer.length)) ? this
                : new CharBuffer(buffer, beginIndex, subLen);
    }
    @NotNull
    public CharBuffer subBuffer(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = buffer.length - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return (beginIndex == 0) ? this : new CharBuffer(buffer, beginIndex, subLen);
    }
}