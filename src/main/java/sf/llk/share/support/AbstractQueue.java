/*!            
    C+edition for Desktop, Community Edition.
    Copyright (C) 2021 Cplusedition Limited.  All rights reserved.
    
    The author licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractQueue<T> implements IQueue<T> {

    ////////////////////////////////////////////////////////////////////////

    protected T[] elements;
    protected int head;
    protected int tail;

    ////////////////////////////////////////////////////////////////////////

    protected abstract int translateIndex(int nth);

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isEmpty() {
        return tail == head;
    }

    @Override
    public int size() {
        return tail >= head ? (tail - head) : ((elements.length - head) + tail);
    }

    /**
     * @return true if there are object in the queue equals() to the given object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return indexOf((T) o) >= 0;
    }

    /**
     * @return Index of object from head that equals() to the given object, -1 if not found.
     */
    @Override
    public int indexOf(T o) {
        int index = head;
        int count = 0;
        while (index != tail) {
            if (elements[index].equals(o)) {
                return count;
            }
            index = increment(index);
            ++count;
        }
        return -1;
    }

    @Override
    public T get(int index) {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[translateIndex(index)];
    }

    @Override
    public final T dequeue() {
        if (tail == head) {
            throw new RuntimeException("Queue is empty.");
        }
        T ret = elements[head];
        head = increment(head);
        return ret;
    }

    /**
     * Remove object from the head.
     */
    @Override
    public T shift() {
        return dequeue();
    }

    /**
     * Remove object from the head.
     */
    @Override
    public void shift(int n) {
        int size = size();
        if (size < n) {
            throw new RuntimeException("Queue size=" + size + ", n=" + n);
        }
        while (--n >= 0) {
            head = increment(head);
        }
    }

    /**
     * Remove object from the tail.
     */
    @Override
    public T pop() {
        if (tail == head) {
            throw new RuntimeException("Queue is empty.");
        }
        tail = decrement(tail);
        return elements[tail];
    }

    /**
     * Remove object from the tail.
     */
    @Override
    public void pop(int n) {
        int size = size();
        if (size < n) {
            throw new RuntimeException("Queue size=" + size + ", n=" + n);
        }
        while (--n >= 0) {
            tail = decrement(tail);
        }
    }

    /**
     * Same as peek(1).
     */
    @Override
    public T peek() {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[decrement(tail)];
    }

    /**
     * Get the nth object (1 based) from the tail. eg. to the last object, use peek(1).
     */
    @Override
    public T peek(int nth) {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[translateIndex(size() - nth)];
    }

    @Override
    public void set(int index, T a) {
        int size = size();
        if (index >= size) {
            throw new OutOfBoundException(size, index);
        }
        elements[translateIndex(index)] = a;
    }

    @Override
    public void setLength(int n) {
        int size = size();
        if (n == size) {
            return;
        }
        if (n > size) {
            throw new RuntimeException("Grow size is not allowed: size=" + size + ", new length=" + n);
        }
        tail = translateIndex(n);
    }

    @Override
    public void clear() {
        if (false) {
            Arrays.fill(elements, null);
        }
        tail = head = elements.length / 2;
    }

    /**
     * The iterator iterate the queue elements from head to tail.
     * Result is undetermined if elements are added/removed during iteration.
     *
     * @return An iterator over all elements in the queue.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    public <E> E[] toArray(E[] ret) {
        int size = size();
        if (isEmpty()) {
            return ret;
        }
        if (tail >= head) {
            System.arraycopy(elements, head, ret, 0, size);
        } else {
            int end = (elements.length - head);
            System.arraycopy(elements, head, ret, 0, end);
            System.arraycopy(elements, 0, ret, end, tail);
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = head; i != tail; i = increment(i)) {
            if (i != head) {
                sb.append(", ");
            }
            sb.append(elements[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////

    @Deprecated
    @Override
    public boolean add(T e) {
        queue(e);
        return true;
    }

    @Deprecated
    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.stream().forEach((e) -> {
            {
                queue(e);
            }
        });
        return true;
    }

    ////////////////////////////////////////////////////////////////////////

    @Deprecated
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////////////////

    protected int increment(int index) {
        return (index == (elements.length - 1)) ? 0 : index + 1;
    }

    protected int decrement(int index) {
        return (index == 0) ? (elements.length - 1) : index - 1;
    }

    @SuppressWarnings("unchecked")
    protected T[] newarray(int rank) {
        return (T[]) new Object[rank];
    }

    ////////////////////////////////////////////////////////////////////////

    protected class IteratorImpl implements Iterator<T> {
        private int fOffset;

        IteratorImpl() {
            fOffset = head;
        }

        @Override
        public boolean hasNext() {
            return (fOffset != tail);
        }

        @Override
        public T next() {
            T ret = elements[fOffset];
            fOffset = increment(fOffset);
            return ret;
        }

        /**
         * Not implemented, do nothing.
         */
        @Override
        public void remove() {
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
