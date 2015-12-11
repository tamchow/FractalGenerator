package in.tamchow.fractal.math;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Implements a fixed-size stack of <code>Complex</code> objects
 */
public class FixedStack {
    Complex[] elements;
    int top;
    public FixedStack(int capacity) {elements = new Complex[capacity]; resetTop();}
    public void resetTop() {top = elements.length;}
    public void push(Complex value) {elements[--top] = value;}
    public Complex pop() {return elements[top++];}
    public Complex peek() {return elements[top];}
    public boolean isEmpty() {return (top == elements.length);}
    public boolean isFull() {return (top == 0);}
    public int size() {return elements.length - top;}
    public void clear() {int capacity = elements.length; elements = new Complex[capacity]; resetTop();}
}