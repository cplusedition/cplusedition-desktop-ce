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
package sf.llk.share.support;

import java.util.Arrays;

public abstract class AbstractIntQueue implements IIntQueue {

    ////////////////////////////////////////////////////////////////////////

    protected int[] elements;
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

    @Override
    public boolean contains(int o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(int o) {
        int index = head;
        int count = 0;
        while (index != tail) {
            if (elements[index] == o) {
                return count;
            }
            index = increment(index);
            ++count;
        }
        return -1;
    }

    @Override
    public int get(int index) {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[translateIndex(index)];
    }

    @Override
    public int shift() {
        if (tail == head) {
            throw new RuntimeException("Queue is empty.");
        }
        int ret = elements[head];
        head = increment(head);
        return ret;
    }

    @Override
    public final int unqueue() {
        return shift();
    }

    @Override
    public void shift(int n) {
        int size = size();
        if (size < n) {
            throw new OutOfBoundException(size, n);
        }
        while (--n >= 0) {
            head = increment(head);
        }
    }

    @Override
    public int pop() {
        if (tail == head) {
            throw new RuntimeException("Queue is empty.");
        }
        tail = decrement(tail);
        return elements[tail];
    }

    @Override
    public int pop(int n) {
        int size = size();
        if (size < n) {
            throw new OutOfBoundException(size, n);
        }
        while (--n >= 0) {
            tail = decrement(tail);
        }
        return elements[tail];
    }

    @Override
    public int peek() {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[decrement(tail)];
    }

    @Override
    public int peek(int nth) {
        if (head == tail) {
            throw new RuntimeException("Queue is empty.");
        }
        return elements[translateIndex(tail - nth)];
    }

    @Override
    public void set(int index, int v) {
        elements[translateIndex(index)] = v;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public IIntIterator iterator() {
        return new IntQueueIterator();
    }

    public void reset() {
        tail = head = elements.length / 2;
    }

    @Override
    public void clear() {
        if (false) {
            Arrays.fill(elements, 0);
        }
        tail = head = elements.length / 2;
    }

    @Override
    public IntRange subRange(int start, int end) {
        int size = end - start;
        int[] a = new int[size];
        copyTo(a, 0, 0, size);
        return new IntRange(a);
    }

    @Override
    public void copyTo(int[] dst, int dstart, int sstart, int send) {
        int len = send - sstart;
        if (len == 0) {
            return;
        }
        int size = size();
        if (len < 0 || len > size || send > size) {
            throw new IndexOutOfBoundsException("size=" + size + ", sstart=" + sstart + ", send=" + send);
        }
        if (tail >= head) {
            System.arraycopy(elements, head + sstart, dst, dstart, len);
            return;
        }
        int count = elements.length - head - sstart;
        if (count > 0) {
            System.arraycopy(elements, head + sstart, dst, dstart, count);
            dstart += count;
            len -= count;
            count = 0;
        } else if (count < 0) {
            count = -count;
        }
        if (tail > 0) {
            System.arraycopy(elements, count, dst, dstart, len);
        }
    }

    @Override
    public void copyTo(int[] dst, int dststart) {
        copyTo(dst, dststart, 0, size());
    }

    @Override
    public int[] toArray() {
        int size = size();
        int[] ret = new int[size()];
        copyTo(ret, 0, 0, size);
        return ret;
    }

    public int[] toArray(int[] ret) {
        copyTo(ret, 0, 0, size());
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

    protected final int increment(int index) {
        return (index == (elements.length - 1)) ? 0 : index + 1;
    }

    protected final int decrement(int index) {
        return (index == 0) ? (elements.length - 1) : index - 1;
    }

    ////////////////////////////////////////////////////////////////////////

    private class IntQueueIterator implements IIntIterator {
        private int index;

        IntQueueIterator() {
            this.index = head;
        }

        @Override
        public boolean hasNext() {
            return index != tail;
        }

        @Override
        public int next() {
            if (index == tail) {
                throw new IndexOutOfBoundsException();
            }
            int ret = elements[index];
            index = increment(index);
            return ret;
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
