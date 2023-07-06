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

/**
 * Heap implemented as an int[] with an independent Comparator.
 * <p>
 * index(root)=1;
 * index(leftchild(i))=2*i;
 * index(rightchild(i))=2*i+1;
 * index(parent(i)=i/2;
 * <p>
 * Note that index==0 is not occupied which save a few arithmetic
 * operations during access.
 */
public class IntHeap {

    protected final IIntComparator comparator;
    protected int size = 0;
    protected int[] heap;

    public IntHeap(int n) {
        this(IIntComparator.DescendingComparator.getSingleton(), n);
    }

    ////////////////////////////////////////////////////////////////////////

    public IntHeap(IIntComparator c) {
        this(c, 10);
    }
    public IntHeap(IIntComparator c, int n) {
        comparator = c;
        heap = new int[n];
    }

    /**
     * In place sorting of given array range in descending order.
     */
    public static void sortDescending(int[] array, int start, int end, IIntComparator comparator) {
        IntHeap heap = new IntHeap(comparator, end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(array[i]);
        }
        for (int i = start; i < end; ++i) {
            array[i] = heap.dequeue();
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * In place sorting of given array range in decending order.
     */
    public static void sortDescending(IIntVector array, int start, int end, IIntComparator comparator) {
        IntHeap heap = new IntHeap(comparator, end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(array.get(i));
        }
        for (int i = start; i < end; ++i) {
            array.set(i, heap.dequeue());
        }
    }

    /**
     * In place sorting of given array range in ascending order.
     */
    public static void sortAscending(int[] array, int start, int end, IIntComparator comparator) {
        IntHeap heap = new IntHeap(new IIntComparator.ReverseComparator(comparator), end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(array[i]);
        }
        for (int i = start; i < end; ++i) {
            array[i] = heap.dequeue();
        }
    }

    /**
     * In place sorting of given array range in ascending order.
     */
    public static void sortAscending(IIntVector array, int start, int end, IIntComparator comparator) {
        IntHeap heap = new IntHeap(new IIntComparator.ReverseComparator(comparator), end - start);
        for (int i = start; i < end; ++i) {
            heap.enqueue(array.get(i));
        }
        for (int i = start; i < end; ++i) {
            array.set(i, heap.dequeue());
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int get(int i) {
        return heap[i + 1];
    }

    public int peek() {
        return heap[1];
    }

    public void clear() {
        size = 0;
    }

    public void enqueue(int a) {
        if (++size >= heap.length) {
            growTo(heap.length * 2);
        }
        int i = 1;
        if (size > 1) {
            i = size;
            for (int p; i > 1 && comparator.compare(heap[p = (i / 2)], a) < 0; i = p) {
                heap[i] = heap[p];
            }
        }
        heap[i] = a;
    }

    /**
     * @return The highest priority (largest value) item removed.
     * @enter size() > 0
     * @exit item has been deleted.
     */
    public int dequeue() {
        if (size == 0) {
            throw new RuntimeException("Heap is empty");
        }
        int ret = heap[1];
        heap[1] = heap[size--];
        moveDown(1);
        return ret;
    }

    /**
     * Brute force linear search for the given value and remove it if found.
     */
    public boolean remove(int value) {
        for (int i = 1; i <= size; ++i) {
            if (comparator.compare(heap[i], value) == 0) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove element at the given index (0-based).
     */
    public void removeAt(int index) {
        ++index;
        for (int p; index > 1; index = p) {
            heap[index] = heap[p = (index / 2)];
        }
        dequeue();
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
                int a = heap[k];
                heap[k] = heap[left];
                heap[left] = a;
                moveDown(left);
            }
        }
    }

    private void growTo(int n) {
        System.arraycopy(heap, 0, heap = new int[n], 0, size);
    }

    ////////////////////////////////////////////////////////////////////////
}
