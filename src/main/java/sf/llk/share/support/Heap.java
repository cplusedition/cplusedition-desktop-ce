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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Heap implemented as an array of objects with an independent Comparator.
 * root is the greatest object.
 * <p>
 * index(root)=1;
 * index(leftchild(i))=2*i;
 * index(rightchild(i))=2*i+1;
 * index(parent(i)=i/2;
 * <p>
 * Note that index==0 is not occupied which save a few arithmetic
 * operations during access.
 */
public class Heap<T> implements IHeap<T> {

    protected final Comparator<T> comparator;
    protected int size = 0;
    protected T[] heap;

    public Heap(Comparator<T> c) {
        this(c, 10);
    }

    ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public Heap(Comparator<T> c, int n) {
        comparator = c;
        heap = (T[]) new Object[n + 1];
    }

    /**
     * In place sorting of given list range in descending order.
     */
    public static <T> void sortDescending(List<T> list, int start, int end, Comparator<T> comparator) {
        Heap<T> heap = new Heap<>(comparator, end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(list.get(i));
        }
        for (int i = start; i < end; ++i) {
            list.set(i, heap.dequeue());
        }
    }

    /**
     * In place sorting of given list range in decending order.
     */
    public static <T> void sortAscending(List<T> list, int start, int end, Comparator<T> comparator) {
        Heap<T> heap = new Heap<>(new ReversedComparator<>(comparator), end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(list.get(i));
        }
        for (int i = start; i < end; ++i) {
            list.set(i, heap.dequeue());
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * In place sorting of given list range in descending order.
     */
    public static <T> void sortDescending(Collection<T> array, Comparator<T> comparator) {
        int size = array.size();
        Heap<T> heap = new Heap<>(comparator, size);
        array.stream().forEach((t) -> {
            {
                heap.enqueue(t);
            }
        });
        array.clear();
        for (int i = 0; i < size; ++i) {
            array.add(heap.dequeue());
        }
    }

    /**
     * In place sorting of given list range in decending order.
     */
    public static <T> void sortAscending(Collection<T> array, Comparator<T> comparator) {
        int size = array.size();
        Heap<T> heap = new Heap<>(new ReversedComparator<>(comparator), size);
        array.stream().forEach((t) -> {
            {
                heap.enqueue(t);
            }
        });
        array.clear();
        for (int i = 0; i < size; ++i) {
            array.add(heap.dequeue());
        }
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get(int i) {
        return heap[i + 1];
    }

    @Override
    public T peek() {
        return heap[1];
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public void enqueue(T a) {
        if (++size >= heap.length) {
            growTo(heap.length * 2);
        }
        int i = 1;
        if (size > 1) {
            for (i = size; i > 1 && comparator.compare(heap[i / 2], a) < 0; i /= 2) {
                heap[i] = heap[i / 2];
            }
        }
        heap[i] = a;
    }

    /**
     * @return the highest priority (lowest value) item.
     * @enter (existing item count >= 1) && (item != NULL)
     * @exit item has been deleted.
     */
    @Override
    public T dequeue() {
        if (size == 0) {
            throw new RuntimeException("Heap is empty");
        }
        T ret = heap[1];
        heap[1] = heap[size--];
        moveDown(1);
        return ret;
    }

    ////////////////////////////////////////////////////////////////////////

    private void moveDown(int k) {
        int left = 2 * k;
        if (left <= size) {
            int right = 2 * k + 1;
            if (right <= size && comparator.compare(heap[right], heap[left]) > 0) {
                left = right;
            }
            if (comparator.compare(heap[k], heap[left]) < 0) {
                T a = heap[k];
                heap[k] = heap[left];
                heap[left] = a;
                moveDown(left);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void growTo(int n) {
        System.arraycopy(heap, 0, heap = (T[]) new Object[n], 0, size);
    }

    ////////////////////////////////////////////////////////////////////////
}
