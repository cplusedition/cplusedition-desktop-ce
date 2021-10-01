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

import java.io.Serializable
import java.util.*

open class IntList : IIntList, Serializable {

    protected var capacity = CAPACITY
    protected var size = 0
    protected var list: IntArray

    constructor() {
        list = IntArray(capacity)
    }

    constructor(cap: Int) {
        capacity = cap
        list = IntArray(capacity)
    }

    constructor(a: IntArray) {
        size = a.size
        capacity = size + CAPACITY
        list = IntArray(capacity)
        System.arraycopy(a, 0, list, 0, size)
    }

    constructor(a: IIntVector) {
        size = a.size()
        capacity = a.size() + CAPACITY
        list = IntArray(capacity)
        a.copyTo(list, 0, 0, size)
    }

    ////////////////////////////////////////////////////////////////////////
    override fun add(v: Int) {
        if (size == capacity) {
            expand()
        }
        list[size] = v
        ++size
    }

    override fun add(a: IntArray) {
        add(a, 0, a.size)
    }

    fun add(a: IntArray, start: Int, end: Int) {
        val len = end - start
        ensureCapacity(size + len)
        System.arraycopy(a, start, list, size, len)
        size += len
    }

    override fun insert(index: Int, v: Int) {
        if (index > size) {
            throw indexSizeException("Expected index <= ", index)
        }
        if (size == capacity) {
            expand()
        }
        if (index != size) {
            System.arraycopy(list, index, list, index + 1, size - index)
        }
        list[index] = v
        ++size
    }

    override fun insert(index: Int, a: IntArray) {
        insert(index, a, 0, a.size)
    }

    override fun insert(index: Int, a: IntArray, start: Int, end: Int) {
        if (index > size) {
            throw indexSizeException("Expected index <= ", index)
        }
        val len = end - start
        if (size + len > capacity) {
            val newsize = Math.max(size + len, capacity) + capacity / 2
            val ret = IntArray(newsize)
            System.arraycopy(list, 0, ret, 0, index)
            System.arraycopy(a, start, ret, index, len)
            if (index != size) {
                System.arraycopy(list, index, ret, index + len, size - index)
            }
            list = ret
        } else {
            if (index != size) {
                System.arraycopy(list, index, list, index + len, size - index)
            }
            System.arraycopy(a, start, list, index, len)
        }
        size += len
    }

    override fun remove(index: Int): Int {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        val ret = list[index]
        if (index != size - 1) {
            System.arraycopy(list, index + 1, list, index, size - index - 1)
        }
        --size
        return ret
    }

    override fun remove(start: Int, end: Int) {
        if (end > size) {
            throw indexSizeException("Expected end < ", end)
        }
        val len = end - start
        System.arraycopy(list, end, list, start, len)
        size -= len
    }

    override fun set(index: Int, value: Int) {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        list[index] = value
    }

    fun or(index: Int, value: Int) {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        list[index] = list[index] or value
    }

    fun and(index: Int, value: Int) {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        list[index] = list[index] and value
    }

    fun andNot(index: Int, value: Int) {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        list[index] = list[index] and value.inv()
    }

    fun xor(index: Int, value: Int) {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        list[index] = list[index] xor value
    }

    override fun get(index: Int): Int {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        return list[index]
    }

    override fun size(): Int {
        return size
    }

    override fun toArray(): IntArray {
        val ret = IntArray(size)
        System.arraycopy(list, 0, ret, 0, size)
        return ret
    }

    /**
     * Convert content to byte[] by casting each int element to byte.
     */
    fun toByteArray(): ByteArray {
        val ret = ByteArray(size)
        for (i in 0 until size) {
            ret[i] = list[i].toByte()
        }
        return ret
    }

    override fun copyTo(dst: IntArray, dststart: Int, srcstart: Int, srcend: Int) {
        System.arraycopy(list, srcstart, dst, dststart, srcend - srcstart)
    }

    override fun copyTo(dst: IntArray, dststart: Int) {
        System.arraycopy(list, 0, dst, dststart, size)
    }

    fun pack() {
        val ret = IntArray(size)
        System.arraycopy(list, 0, ret, 0, size)
        list = ret
        capacity = size
    }

    override fun clear() {
        size = 0
    }

    override fun toString(): String {
        return toString(8)
    }

    fun toString(count: Int): String {
        val buf = StringBuilder()
        val count1 = count - 1
        for (i in 0 until size) {
            buf.append(list[i])
            buf.append(' ')
            if (i % count == count1) {
                buf.append("\n")
            }
        }
        if (size % count != 0) {
            buf.append("\n")
        }
        return buf.toString()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        val ret = super.clone() as IntList
        ret.list = list.clone()
        return ret
    }

    override val isEmpty: Boolean
        get() = size == 0

    override fun push(value: Int) {
        if (size == capacity) {
            expand()
        }
        list[size] = value
        ++size
    }

    override fun peek(): Int {
        return list[size - 1]
    }

    override fun pop(): Int {
        --size
        return list[size]
    }

    override fun peek(nth: Int): Int {
        if (nth <= 0 || nth > size) {
            throw IndexOutOfBoundsException("Expected 0 < n <= $size: n=$nth")
        }
        return list[size - nth]
    }

    override fun pop(n: Int): Int {
        if (n <= 0 || n > size) {
            throw IndexOutOfBoundsException("Expected: 0 < n <= $size: n=$n")
        }
        size -= n
        return list[size]
    }

    override fun swap(value: Int): Int {
        val ret = list[size - 1]
        list[size - 1] = value
        return ret
    }

    ////////////////////////////////////////////////////////////////////////
    override fun queue(e: Int) {
        push(e)
    }

    override fun unqueue(): Int {
        return shift()
    }

    override fun shift(): Int {
        return remove(0)
    }

    override fun shift(n: Int) {
        remove(0, n)
    }

    ////////////////////////////////////////////////////////////////////////
    override fun iterator(): IIntIterator {
        return IntListIterator()
    }

    override fun setLength(n: Int) {
        if (n < 0 || n > size) {
            throw IndexOutOfBoundsException("Expected: 0 <= n <= $size: n=$n")
        }
        size = n
    }

    fun ensureCapacity(newcap: Int) {
        var newcap = newcap
        if (newcap > capacity) {
            newcap = Math.max(newcap, capacity) + capacity / 2
            expand(newcap)
        }
    }

    /** Truncate size by the given delta.  */
    fun shrinkBy(n: Int) {
        if (n < 0 || n > size) {
            throw IndexOutOfBoundsException("Expected: 0 <= n <= $size: n=$n")
        }
        size -= n
    }

    /** Increase size by the given delta and filled with given default value.  */
    fun growBy(n: Int, def: Int) {
        val newsize = size + n
        if (newsize > capacity) {
            expand(newsize + newsize shr 1)
        }
        for (i in size until newsize) {
            list[i] = def
        }
        size = newsize
    }

    /** @return Value of the last element after increment.
     */
    fun increment(): Int {
        if (size == 0) {
            throw IndexOutOfBoundsException("size==0")
        }
        return ++list[size - 1]
    }

    /** @return Value of the last element after decrement.
     */
    fun decrement(): Int {
        if (size == 0) {
            throw IndexOutOfBoundsException("size==0")
        }
        return --list[size - 1]
    }

    /** @return Value at given index after increment.
     */
    fun increment(index: Int): Int {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        return ++list[index]
    }

    /** @return Value at given index after decrement.
     */
    fun decrement(index: Int): Int {
        if (index >= size) {
            throw indexSizeException("Expected index < ", index)
        }
        return --list[index]
    }

    fun sort() {
        Arrays.sort(list, 0, size)
    }

    /**
     * Keep only unique values.
     * @entry Must have been sorted.
     */
    fun unique() {
        if (size <= 1) {
            return
        }
        var k = 0
        var p = list[0]
        for (i in 1 until size) {
            val v = list[i]
            if (v != p) {
                p = v
                list[++k] = p
            }
        }
        size = k + 1
    }

    override fun binarySearch(x: Int): Int {
        var start = 0
        var end = size - 1
        while (start <= end) {
            val mid = start + end ushr 1
            if (list[mid] < x) {
                start = mid + 1
            } else if (list[mid] > x) {
                end = mid - 1
            } else {
                return mid
            }
        }
        return -(start + 1)
    }

    override fun insertionIndex(x: Int): Int {
        var start = 0
        var end = size - 1
        while (start <= end) {
            val mid = start + end ushr 1
            if (list[mid] < x) {
                start = mid + 1
            } else if (list[mid] > x) {
                end = mid - 1
            } else {
                return mid + 1
            }
        }
        return start
    }

    override fun indexOf(x: Int): Int {
        for (i in 0 until size) {
            if (list[i] == x) {
                return i
            }
        }
        return -1
    }

    override fun contains(value: Int): Boolean {
        return indexOf(value) >= 0
    }

    override fun subRange(start: Int, end: Int): IntRange {
        return IntRange(list, start, end)
    }

    fun trim() {
        if (list.size != size) {
            list = Arrays.copyOf(list, size)
        }
    }

    ////////////////////////////////////////////////////////////////////////
    protected fun expand(cap: Int = capacity + (capacity shr 1) + 1) {
        capacity = cap
        val ret = IntArray(cap)
        System.arraycopy(list, 0, ret, 0, size)
        list = ret
    }

    private fun indexSizeException(msg: String, index: Int): IndexOutOfBoundsException {
        return IndexOutOfBoundsException("$msg$size, index=$index")
    }

    ////////////////////////////////////////////////////////////////////////
    protected inner class IntListIterator : IIntIterator {
        private var index = 0
        override fun hasNext(): Boolean {
            return index < size
        }

        override fun next(): Int {
            return list[index++]
        }
    } ////////////////////////////////////////////////////////////////////////

    companion object {
        ////////////////////////////////////////////////////////////////////////
        private const val serialVersionUID = 5681418481728541841L
        private const val CAPACITY = 8
    }
}
