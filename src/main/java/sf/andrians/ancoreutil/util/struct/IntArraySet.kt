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

import java.util.*

/**
 * A simple implementation of IIntSet using an int[] and binary search and insertion.
 */
class IntArraySet  constructor(cap: Int = CAPACITY) : IIntSet {

    private var array = IntArray(cap)
    private var size = 0

    constructor(a: IIntIterable) : this(a.size()) {
        val it = a.iterator()
        while (it.hasNext()) {
            add(it.next())
        }
    }

    ////////////////////////////////////////////////////////////////////////

    override fun contains(value: Int): Boolean {
        return Arrays.binarySearch(array, 0, size, value) >= 0
    }

    override fun remove(v: Int): Boolean {
        val index = Arrays.binarySearch(array, 0, size, v)
        if (index < 0) {
            return false
        }
        --size
        if (index != size) {
            System.arraycopy(array, index + 1, array, index, size - index)
        }
        return true
    }

    override fun clear() {
        size = 0
    }

    override val isEmpty: Boolean
        get() = size == 0

    override fun iterator(): IIntIterator {
        return IntSetIterator()
    }

    override fun size(): Int {
        return size
    }

    override fun add(v: Int): Boolean {
        var index = Arrays.binarySearch(array, 0, size, v)
        if (index >= 0) {
            return false
        }
        index = -index - 1
        if (index >= array.size) {
            array = Arrays.copyOf(array, Math.max(array.size * 2, index + 1))
        }
        if (size >= array.size) {
            array = Arrays.copyOf(array, array.size * 2)
        }
        if (index < size) {
            System.arraycopy(array, index, array, index + 1, size - index)
        }
        array[index] = v
        ++size
        return true
    }

    override fun addRange(start: Int, end: Int): Boolean {
        var added = false
        for (i in start until end) {
            added = added or add(i)
        }
        return added
    }

    override fun addAll(vararg a: Int): Boolean {
        var added = false
        for (v in a) {
            added = added or add(v)
        }
        return added
    }

    override fun addAll(a: IIntIterable): Boolean {
        var added = false
        val it = a.iterator()
        while (it.hasNext()) {
            added = added or add(it.next())
        }
        return added
    }

    override fun copyTo(dst: IntArray, dststart: Int) {
        System.arraycopy(array, 0, dst, dststart, size)
    }

    override fun toArray(): IntArray {
        return Arrays.copyOf(array, size)
    }

    ////////////////////////////////////////////////////////////////////////

    protected inner class IntSetIterator internal constructor() : IIntIterator {
        private var index = 0
        override fun hasNext(): Boolean {
            return index < size
        }

        override fun next(): Int {
            return array[index++]
        }

    }

    ////////////////////////////////////////////////////////////////////////

    companion object {
        private const val CAPACITY = 10
    }

    ////////////////////////////////////////////////////////////////////////
}