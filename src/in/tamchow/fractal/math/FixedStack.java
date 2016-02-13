package in.tamchow.fractal.math;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
import java.util.EmptyStackException;
/**
 * Implements a fixed-size stack of <code>Complex</code> objects
 */
public class FixedStack implements Serializable {
    Complex[] elements;
    int top;
    public FixedStack(int capacity) {elements = new Complex[capacity]; resetTop();}
    public void resetTop() {top = elements.length;}
    public void push(Complex value) {
        if (isFull()) throw new IndexOutOfBoundsException("Overflow Exception"); elements[--top] = value;
    }
    public boolean isFull() {return (top == 0);}
    public Complex pop() {
        if (isEmpty()) throw new EmptyStackException(); return elements[top++];
    }
    public boolean isEmpty() {return (top == elements.length);}
    public Complex peek() {
        if (isEmpty()) throw new EmptyStackException(); return elements[top];
    }
    public int size() {return elements.length - top;}
    public void clear() {int capacity = elements.length; elements = new Complex[capacity]; resetTop();}
}