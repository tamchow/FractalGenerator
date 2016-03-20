package in.tamchow.fractal.math;
import java.io.Serializable;
import java.util.EmptyStackException;
/**
 * A stack of ints, especially for BS' Stack-extended version.
 */
public class FixedStack<E> implements Serializable {
    E[] elements;
    int top;

    @SuppressWarnings("unchecked")
    public FixedStack(int capacity) {
        elements = (E[]) new Object[capacity];
        erase();
        resetTop();
    }
    public void resetTop() {top = elements.length;}

    public void erase() {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = null;
        }
    }

    public void pushN(E[] values) {
        for (E value : values) {
            push(value);
        }
    }

    public void push(E value) {
        if (isFull()) throw new IndexOutOfBoundsException("Overflow Exception"); elements[--top] = value;
    }
    public boolean isFull() {return (top == 0);}

    @SuppressWarnings("unchecked")
    public E[] popN(int n) {
        E[] values = (E[]) new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = pop();
        }
        return values;
    }

    public E pop() {
        if (isEmpty()) throw new EmptyStackException();
        E value = elements[top];
        elements[top] = null;
        ++top;
        return value;
    }
    public boolean isEmpty() {return (top == elements.length);}

    @SuppressWarnings("unchecked")
    public E[] peekN(int n) {
        E[] values = (E[]) new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = peek(i);
        }
        return values;
    }

    private E peek(int n) {
        if (isEmpty()) throw new EmptyStackException();
        return elements[top - n];
    }

    public void duplicateN(int n) {
        for (int i = 0; i < n; i++) {
            duplicate();
        }
    }

    public void duplicate() {
        push(peek());
    }

    public E peek() {
        if (isEmpty()) throw new EmptyStackException(); return elements[top];
    }

    @SuppressWarnings("unchecked")
    public void reverse() {
        E[] reversed = (E[]) new Object[elements.length];
        for (int i = 0, j = reversed.length - 1; i < elements.length && j >= 0; i++, j--) {
            reversed[j] = elements[i];
        }
        initStack(reversed);
    }

    @SuppressWarnings("unchecked")
    public void initStack(E[] elements) {
        this.elements = (E[]) new Object[elements.length];
        System.arraycopy(elements, 0, this.elements, 0, this.elements.length);
        resetTop();
    }

    public E[] dumpStack() {
        return elements;
    }
    public int size() {return elements.length - top;}

    public int sizeN() {
        int size = 0;
        for (E i : elements) {
            if (i != null) size++;
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        int capacity = elements.length;
        elements = (E[]) new Object[capacity];
        erase();
        resetTop();
    }
}