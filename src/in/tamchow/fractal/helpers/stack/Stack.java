package in.tamchow.fractal.helpers.stack;
import in.tamchow.fractal.helpers.stack.impls.FixedStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
/**
 * Abstract Stack Base class. Implements {@link java.util.Collection}.
 * <p/>
 * Known implementors:
 * <p/>
 * {@link FixedStack} - A fixed-length array-based stack.
 *
 * @see java.util.Collection
 * @see java.lang.Iterable
 * @see FixedStack
 */
public abstract class Stack<E> implements Collection<E>, Serializable {
    /**
     * @param item the item to push on to this stack
     * @throws StackOverflowException if the stack is full.
     */
    public abstract void push(E item) throws StackOverflowException;
    /**
     * @return an array containing the items of this stack
     */
    public abstract E[] dumpStack();
    /**
     * @return whether the stack is full or not
     */
    public abstract boolean isFull();
    @Override
    public boolean add(E item) {
        try {
            push(item);
            return true;
        } catch (StackOverflowException soe) {
            return false;
        }
    }
    /**
     * @return the size of this stack
     * @see Collection#size()
     */
    @Override
    public abstract int size();
    /**
     * @return whether this stack is empty or not
     * @see Collection#isEmpty()
     */
    @Override
    public abstract boolean isEmpty();
    /**
     * @param o the {@link Object} to check
     * @return true if o is present in this stack
     * @see Collection#contains(Object)
     */
    @Override
    public boolean contains(Object o) {
        return false;
    }
    /**
     * @return {@link StackIterator}
     * @see Collection#iterator()
     */
    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new StackIterator(dumpStack());
    }
    /**
     * @return the elements of this stack
     * @see Collection#toArray()
     */
    @NotNull
    @Override
    public Object[] toArray() {
        return dumpStack();
    }
    /**
     * @return the elements of this stack
     * @see Collection#toArray(T[])
     */
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        a = (T[]) new Object[size()];
        //System.arraycopy(dumpStack(),0,a,0,size());
        E[] data = dumpStack();
        for (int i = 0; i < a.length; ++i) {
            a[i] = (T) data[i];
        }
        return a;
    }
    /**
     * @return the topmost element without removing it
     * @throws EmptyStackException if the stack is empty
     */
    public abstract E peek() throws EmptyStackException;
    /**
     * @return the topmost element after removing it
     * @throws EmptyStackException if the stack is empty
     */
    public abstract E pop() throws EmptyStackException;
    /**
     * @param item the {@link Object} to remove
     * @return does not return
     * @throws UnsupportedOperationException
     * @see Collection#removeAll(Collection)
     */
    @Override
    public boolean remove(Object item) {
        throw new UnsupportedOperationException(this.getClass() + " does not support remove operation");
    }
    /**
     * @param c the {@link Collection} to check
     * @return true if all the elements of c are present in this stack
     * @see Collection#containsAll(Collection)
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }
    /**
     * @param c the {@link Collection} to add
     * @return true
     * @see Collection#addAll(Collection)
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        for (E item : c) {
            add(item);
        }
        return true;
    }
    /**
     * @param c the {@link Collection} to remove
     * @return does not return
     * @throws UnsupportedOperationException
     * @see Collection#removeAll(Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(this.getClass() + " does not support remove operation");
    }
    /**
     * @param c the {@link Collection} to retain
     * @return does not return
     * @throws UnsupportedOperationException
     * @see Collection#retainAll(Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(this.getClass() + " does not support retain operation");
    }
    /**
     * @see Collection#clear()
     */
    @Override
    public abstract void clear();
    /**
     * @return whether the argument is equal to this
     * @see Collection#equals(Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof Stack) {
            @NotNull Stack<E> that = (Stack<E>) other;
            if (that.size() != size()) {
                return false;
            }
            E[] myItems = dumpStack(), othersItems = that.dumpStack();
            for (int i = 0; i < myItems.length && i < othersItems.length; ++i) {
                if (!myItems[i].equals(othersItems[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    /**
     * @return the hashCode of the current Stack
     * @see Collection#hashCode()
     */
    @Override
    public int hashCode() {
        E[] myItems = dumpStack();
        int hashCode = peek().hashCode();
        for (int i = 1; i < myItems.length; ++i) {
            hashCode ^= myItems[i].hashCode();
        }
        return hashCode;
    }
    /**
     * Simple Iterator for a Stack
     *
     * @see java.util.Iterator
     */
    private class StackIterator implements Iterator<E> {
        E[] data;
        int pointer;
        @SuppressWarnings("unchecked")
        private StackIterator(@NotNull E[] data) {
            this.data = (E[]) new Object[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
        }
        /**
         * @return whether the StackIterator has more elements
         * @see Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return pointer < data.length;
        }
        /**
         * @return the next element
         * @see Iterator#next()
         */
        @Override
        public E next() {
            return data[pointer++];
        }
        /**
         * @throws UnsupportedOperationException as the StackIterator doesn't support removing elements
         * @see Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException(this.getClass() + " does not support remove operation");
        }
    }
}