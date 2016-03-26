package in.tamchow.fractal.math;

import java.io.Serializable;
import java.util.EmptyStackException;

/**
 * A generic fixed-length stack
 */
public class FixedStack<E> implements Serializable {
    /**
     * Array of elements
     */
    private E[] elements;
    /**
     * Stack top pointer
     */
    private int top;

    /**
     * Parameterized constructor. No default constructor.
     *
     * @param capacity The maximum size  (capacity) of the stack
     * @see FixedStack#setSize(int)
     * @see FixedStack#resetTop(boolean)
     */
    public FixedStack(int capacity) {
        setSize(capacity);
        resetTop(false);
    }

    /**
     * Resets the stack top pointer depending on whether the stack is empty or full
     *
     * @param notEmpty Whether or not the stack is empty
     */
    private void resetTop(boolean notEmpty) {
        top = notEmpty ? 0 : elements.length;
    }

    /**
     * Note: Setting the size <b>WILL CLEAR THE STACK</b>.
     *
     * @param size The size of the newly-initialized stack
     * @see FixedStack#resetTop(boolean)
     */
    @SuppressWarnings("unchecked")
    private void setSize(int size) {
        this.elements = (E[]) new Object[Math.abs(size)];
        resetTop(false);
    }

    /**
     * Has the same effect as {@link FixedStack#setSize(int)},
     * but does not reinitialize the elements array
     *
     * @see FixedStack#setSize(int)
     * @see FixedStack#resetTop(boolean)
     */
    private void erase() {
        for (int i = 0; i < elements.length; ++i) {
            elements[i] = null;
        }
        resetTop(false);
    }

    /**
     * Pushes a set of values onto the stack
     *
     * @param values The values to push
     */
    public void pushN(E[] values) {
        for (E value : values) {
            push(value);
        }
    }

    /**
     * Pushes a value onto the stack
     *
     * @param value The value to push
     */
    public void push(E value) {
        if (isFull()) throw new StackOverflowException();
        elements[--top] = value;
    }

    /**
     * Checks whether the stack is full
     *
     * @return Whether the stack is full or not
     */
    public boolean isFull() {
        return (top == 0);
    }

    /**
     * Pops a set of values from the stack
     *
     * @return The popped values
     */
    public E[] popN(int n) {
        @SuppressWarnings("unchecked")
        E[] values = (E[]) new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = pop();
        }
        return values;
    }

    /**
     * Pops a value from the stack
     *
     * @return The popped value
     */
    public E pop() {
        if (isEmpty()) throw new EmptyStackException();
        E value = elements[top];
        elements[top++] = null;
        return value;
    }

    /**
     * Checks whether the stack is empty
     *
     * @return Whether the stack is empty or not
     */
    public boolean isEmpty() {
        return (top == elements.length);
    }

    /**
     * Peeks at a set of values on the stack
     *
     * @param n The number of values to peek at
     * @return The peeked-at values
     */
    public E[] peekN(int n) {
        @SuppressWarnings("unchecked")
        E[] values = (E[]) new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = peek(i);
        }
        return values;
    }

    /**
     * Peeks at a value on the stack at a particular index
     *
     * @param n The relative index of the value to peek at
     * @return The peeked-at value
     */
    private E peek(int n) {
        if (isEmpty()) throw new EmptyStackException();
        return elements[top - n];
    }

    /**
     * Duplicates the n topmost elements of the stack, top-down.
     *
     * @param n The number of elements to duplicate
     */
    public void duplicateN(int n) {
        for (int i = 0; i < n; i++) {
            duplicate();
        }
    }

    /**
     * Duplicates the topmost element of the stack
     */
    public void duplicate() {
        push(peek());
    }

    /**
     * Peeks at a value on the stack
     *
     * @return The peeked-at value
     */
    public E peek() {
        if (isEmpty()) throw new EmptyStackException();
        return elements[top];
    }

    /**
     * Reverses the stack
     *
     * @see FixedStack#initStack(Object[])
     */
    public void reverse() {
        @SuppressWarnings("unchecked")
        E[] reversed = (E[]) new Object[elements.length];
        for (int i = 0, j = reversed.length - 1; i < elements.length && j >= 0; i++, j--) {
            reversed[j] = elements[i];
        }
        initStack(reversed);
    }

    /**
     * Initializes the stack with the supplied set of values
     *
     * @param elements The set of initial values
     * @see FixedStack#pushN(Object[])
     * @see FixedStack#setSize(int)
     */
    public void initStack(E[] elements) {
        setSize(elements.length);
        pushN(elements);
    }

    /**
     * Dumps the stack elements to the caller
     *
     * @return The set of elements currently on the stack
     */
    public E[] dumpStack() {
        return elements;
    }

    /**
     * Provides the current number of elements on the stack
     *
     * @return The size of the stack
     */
    public int size() {
        return elements.length - top;
    }

    /**
     * More conventional stack size calculation.
     * Use not recommended.
     *
     * @return The size of the stack
     * @see FixedStack#size()
     */
    public int sizeN() {
        int size = 0;
        for (E i : elements) {
            if (i != null) size++;
        }
        return size;
    }

    /**
     * Alias for {@link FixedStack#erase()}
     *
     * @see FixedStack#erase()
     */
    public void clear() {
        erase();
    }
}

/**
 * Custom Stack Overflow Exception class
 */
class StackOverflowException extends IndexOutOfBoundsException {
    /**
     * Constructs the exception with a default message
     */
    public StackOverflowException() {
        this("Stack Overflow");
    }

    /**
     * Constructs the exception with a custom message
     *
     * @param message The custom message
     */
    public StackOverflowException(String message) {
        super(message);
    }
}