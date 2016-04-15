package in.tamchow.fractal.helpers.stack.impls;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.stack.Stack;
import in.tamchow.fractal.helpers.stack.StackOverflowException;

import java.util.EmptyStackException;
/**
 * A generic fixed-length stack implementing {@link Stack}
 *
 * @see Stack
 */
public class FixedStack<E> extends Stack<E> {
    /**
     * Array of elements
     */
    private E[] elements;
    /**
     * Stack top pointer
     */
    private int top;
    /**
     * Parametrized constructor. No default constructor.
     *
     * @param capacity The maximum size  (capacity) of the stack
     * @see FixedStack#setSize(int)
     * @see FixedStack#resetTop(boolean)
     */
    public FixedStack(int capacity) {
        setSize(capacity);
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
    public void pushN(@NotNull E[] values) {
        for (E value : values) {
            push(value);
        }
    }
    /**
     * Pushes a value onto the stack
     *
     * @param value The value to push
     */
    @Override
    public void push(E value) {
        if (isFull()) throw new FixedStackOverflowException();
        elements[--top] = value;
    }
    /**
     * Checks whether the stack is full
     *
     * @return Whether the stack is full or not
     */
    @Override
    public boolean isFull() {
        return (top == 0);
    }
    /**
     * Pops a set of values from the stack
     *
     * @return The popped values
     */
    @NotNull
    public E[] popN(int n) {
        @NotNull @SuppressWarnings("unchecked")
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
    @Override
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
    @Override
    public boolean isEmpty() {
        return (top == elements.length);
    }
    /**
     * Peeks at a set of values on the stack
     *
     * @param n The number of values to peek at
     * @return The peeked-at values
     */
    @NotNull
    public E[] peekN(int n) {
        @NotNull @SuppressWarnings("unchecked")
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
    @Override
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
        @NotNull @SuppressWarnings("unchecked")
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
    public void initStack(@NotNull E[] elements) {
        setSize(elements.length);
        pushN(elements);
    }
    /**
     * Dumps the stack elements to the caller
     *
     * @return The set of elements currently on the stack
     */
    @Override
    public E[] dumpStack() {
        return elements;
    }
    /**
     * Provides the current number of elements on the stack
     *
     * @return The size of the stack
     */
    @Override
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
        for (@Nullable E i : elements) {
            if (i != null) ++size;
        }
        return size;
    }
    /**
     * Alias for {@link FixedStack#erase()}
     *
     * @see FixedStack#erase()
     */
    @Override
    public void clear() {
        erase();
    }
}
/**
 * Custom Stack Overflow Exception class
 */
class FixedStackOverflowException extends StackOverflowException {
    /**
     * Constructs the exception with a default message
     */
    public FixedStackOverflowException() {
        this(" Fixed Stack Overflow");
    }
    /**
     * Constructs the exception with a custom message
     *
     * @param message the custom message
     */
    public FixedStackOverflowException(String message) {
        super(message);
    }
}