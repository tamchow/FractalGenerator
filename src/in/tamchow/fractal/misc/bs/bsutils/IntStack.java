package in.tamchow.fractal.misc.bs.bsutils;
import java.util.EmptyStackException;
/**
 * A stack of ints, especially for BS' Stack-extended version.
 */
public class IntStack {
    int[] elements;
    int top;
    public IntStack(int capacity) {elements = new int[capacity]; erase(); resetTop();}
    public void resetTop() {top = elements.length;}
    public void erase() {
        for (int i = 0; i < elements.length; i++) {elements[i] = -1;}
    }
    public void pushN(int[] values) {for (int value : values) {push(value);}}
    public void push(int value) {
        if (isFull()) throw new IndexOutOfBoundsException("Overflow Exception"); elements[--top] = value;
    }
    public boolean isFull() {return (top == 0);}
    public int[] popN(int n) {
        int[] values = new int[n]; for (int i = 0; i < n; i++) {
            values[i] = pop();
        } return values;
    }
    public int pop() {
        if (isEmpty()) throw new EmptyStackException(); int value = elements[top]; elements[top] = -1; ++top;
        return value;
    }
    public boolean isEmpty() {return (top == elements.length);}
    public int[] peekN(int n) {
        int[] values = new int[n]; for (int i = 0; i < n; i++) {
            values[i] = peek(i);
        } return values;
    }
    private int peek(int n) {
        if (isEmpty()) throw new EmptyStackException(); return elements[top - n];
    }
    public void duplicateN(int n) {for (int i = 0; i < n; i++) {duplicate();}}
    public void duplicate() {push(peek());}
    public int peek() {
        if (isEmpty()) throw new EmptyStackException(); return elements[top];
    }
    public void reverse() {
        int[] reversed = new int[elements.length];
        for (int i = 0, j = reversed.length - 1; i < elements.length && j >= 0; i++, j--) {
            reversed[j] = elements[i];
        } initStack(reversed);
    }
    public void initStack(int[] elements) {
        this.elements = new int[elements.length]; System.arraycopy(elements, 0, this.elements, 0, this.elements.length);
        resetTop();
    }
    public int[] dumpStack() {return elements;}
    public int size() {return elements.length - top;}
    public int sizeN() {
        int size = 0; for (int i : elements) {
            if (i > 0) size++;
        } return size;
    }
    public void clear() {int capacity = elements.length; elements = new int[capacity]; erase(); resetTop();}
}