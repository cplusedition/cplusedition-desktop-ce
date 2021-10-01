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
package sf.andrians.ancoreutil.util.struct

/**
 * Heap implemented as an int[] with an independent Comparator.
 *
 * index(root)=1;
 * index(leftchild(i))=2*i;
 * index(rightchild(i))=2*i+1;
 * index(parent(i)=i/2;
 *
 * Note that index==0 is not occupied which save a few arithmetic
 * operations during access.
 */
class IntHeap  constructor(protected var comparator: IIntComparator, n: Int = 10) {

    protected var size = 0
    protected var heap = IntArray(n)

    constructor(n: Int) : this(IIntComparator.DescendingComparator.singleton, n) {}

    val isEmpty: Boolean
        get() = size == 0

    fun size(): Int {
        return size
    }

    operator fun get(i: Int): Int {
        return heap[i + 1]
    }

    fun peek(): Int {
        return heap[1]
    }

    fun clear() {
        size = 0
    }

    fun enqueue(a: Int) {
        if (++size >= heap.size) {
            growTo(heap.size * 2)
        }
        var i = 1
        if (size > 1) {
            i = size
            while (i > 1) {
                val p = i / 2
                if (comparator.compare(heap[p], a) >= 0) break
                heap[i] = heap[p]
                i = p
            }
        }
        heap[i] = a
    }

    /**
     * @return The highest priority (largest value) item removed.
     * @enter    size() > 0
     * @exit    item has been deleted.
     */
    fun dequeue(): Int {
        if (size == 0) {
            throw RuntimeException("Heap is empty")
        }
        val ret = heap[1] // Save the root.
        heap[1] = heap[size--] // Put the last item in the root.
        moveDown(1) // Move the new root down if necessary.
        return ret
    }

    /**
     * Brute force linear search for the given value and remove it if found.
     */
    fun remove(value: Int): Boolean {
        for (i in 1..size) {
            if (comparator.compare(heap[i], value) == 0) {
                removeAt(i)
                return true
            }
        }
        return false
    }

    /**
     * Remove element at the given index (0-based).
     */
    fun removeAt(index: Int) {
        var index = index
        ++index
        var p: Int
        while (index > 1) {
            heap[index] = heap[(index / 2).also { p = it }]
            index = p
        }
        dequeue()
    }

    ////////////////////////////////////////////////////////////////////////

    private fun moveDown(k: Int) {
        var left = 2 * k
        if (left <= size) {
            val right = 2 * k + 1
            if (right <= size && comparator.compare(heap[right], heap[left]) > 0) {
                left = right
            }
            if (comparator.compare(heap[k], heap[left]) < 0) {
                val a = heap[k]
                heap[k] = heap[left]
                heap[left] = a
                moveDown(left)
            }
        }
    }

    private fun growTo(n: Int) {
        System.arraycopy(heap, 0, IntArray(n).also { heap = it }, 0, size)
    }

    ////////////////////////////////////////////////////////////////////////

    companion object {
        /** In place sorting of given array range in descending order.  */
        fun sortDescending(array: IntArray, start: Int, end: Int, comparator: IIntComparator) {
            val heap = IntHeap(comparator, end - start)
            for (i in start until end) {
                heap.enqueue(array[i])
            }
            for (i in start until end) {
                array[i] = heap.dequeue()
            }
        }

        /** In place sorting of given array range in decending order.  */
        fun sortDescending(array: IIntVector, start: Int, end: Int, comparator: IIntComparator) {
            val heap = IntHeap(comparator, end - start)
            for (i in start until end) {
                heap.enqueue(array[i])
            }
            for (i in start until end) {
                array[i] = heap.dequeue()
            }
        }

        /** In place sorting of given array range in ascending order.  */
        fun sortAscending(array: IntArray, start: Int, end: Int, comparator: IIntComparator) {
            val heap = IntHeap(IIntComparator.ReverseComparator(comparator), end - start)
            for (i in start until end) {
                heap.enqueue(array[i])
            }
            for (i in start until end) {
                array[i] = heap.dequeue()
            }
        }

        /** In place sorting of given array range in ascending order.  */
        fun sortAscending(array: IIntVector, start: Int, end: Int, comparator: IIntComparator) {
            val heap = IntHeap(IIntComparator.ReverseComparator(comparator), end - start)
            for (i in start until end) {
                heap.enqueue(array[i])
            }
            for (i in start until end) {
                array[i] = heap.dequeue()
            }
        }
    }
}
