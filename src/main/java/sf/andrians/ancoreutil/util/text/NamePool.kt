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
package sf.andrians.ancoreutil.util.text

import java.util.*

/**
 * NamePool is a singleton set of Name.  NamePool entry 0 is always null.
 * Once added, Name cannot be removed from the pool.
 */
class NamePool  constructor(capacity: Int = DEF_SIZE, loadfactor: Float = DEF_LOAD_FACTOR) : INamePool {
    protected var ordered: Array<Name?> = arrayOfNulls(0)
    protected var entries: Array<Name?> = arrayOfNulls(0)
    protected var entryCount = 0
    protected var threshold = 0
    protected var initCapacity = 0
    protected var indexMask = 0
    protected var loadFactor = 0f

    init {
        init(capacity, loadfactor)
    }

    ////////////////////////////////////////////////////////////////////////

    class Name(var id: Int, var hashcode: Int, var s: String, var link: Name?) : IName {

        var length_ = s.length

        override fun id(): Int {
            return id
        }

        override val length: Int get() = length_

        override fun get(index: Int): Char {
            return s[index]
        }

        override fun getChars(srcBegin: Int, srcEnd: Int, dst: CharArray, dstBegin: Int) {
            s.toCharArray(dst, dstBegin, srcBegin, srcEnd)
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return s.substring(startIndex, endIndex)
        }

        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return hashcode
        }

        override fun toString(): String {
            return s
        }

        companion object {
            fun hash(cs: CharSequence, start: Int, end: Int): Int {
                var end = end
                var h = 0
                while (end > start) {
                    h = h * 31 + cs[--end].toInt()
                }
                return h
            }

            fun hash(cs: CharArray, start: Int, end: Int): Int {
                var end = end
                var h = 0
                while (end > start) {
                    h = h * 31 + cs[--end].toInt()
                }
                return h
            }
        }
    }

    private inner class NameIterator : MutableIterator<IName> {
        private val limit = entryCount
        private var index = 0
        override fun hasNext(): Boolean {
            return index < limit
        }

        override fun next(): IName {
            if (limit != entryCount) {
                throw ConcurrentModificationException()
            }
            return ordered[index++]!!
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }

    constructor(loadfactor: Float) : this(DEF_SIZE, loadfactor) {}
    constructor(vararg a: CharSequence) : this(a.size + DEF_SIZE) {
        for (s in a) {
            intern(s)
        }
    }

    private fun init(capacity: Int, loadfactor: Float) {
        var capacity = capacity
        if (capacity > MAX_SIZE) {
            capacity = MAX_SIZE
        }
        var cap = 1
        while (cap < capacity) {
            cap = cap shl 1
        }
        ordered = arrayOfNulls(cap)
        entries = arrayOfNulls(cap)
        loadFactor = loadfactor
        initCapacity = cap
        indexMask = cap - 1
        entryCount = 1
        threshold = (cap * loadfactor).toInt()
    }

    ////////////////////////////////////////////////////////////////////////
    override fun size(): Int {
        return entryCount
    }

    override fun intern(a: CharArray?): IName? {
        if (a == null) {
            return null
        }
        val len = a.size
        val hash = Name.hash(a, 0, len)
        val index = hash and indexMask
        var e = entries[index]
        while (e != null) {
            if (e.length == len && equals(e, a, 0, len)) {
                return e
            }
            e = e.link
        }
        return add(String(a), hash, index)
    }

    override fun intern(a: CharArray, start: Int, end: Int): IName {
        val hash = Name.hash(a, start, end)
        val index = hash and indexMask
        val len = end - start
        var e = entries[index]
        while (e != null) {
            if (e.length == len && equals(e, a, start, len)) {
                return e
            }
            e = e.link
        }
        return add(String(a, start, len), hash, index)
    }

    override fun intern(a: CharSequence?): IName? {
        if (a == null) {
            return null
        }
        val len = a.length
        val hash = Name.hash(a, 0, len)
        val index = hash and indexMask
        var e = entries[index]
        while (e != null) {
            if (e.s === a || e.length == len && equals(e, a, 0, len)) {
                return e
            }
            e = e.link
        }
        return add(a.toString(), hash, index)
    }

    override fun intern(a: CharSequence, start: Int, end: Int): IName {
        val hash = Name.hash(a, start, end)
        val index = hash and indexMask
        val len = end - start
        var e = entries[index]
        while (e != null) {
            if (e.length == len && equals(e, a, start, len)) {
                return e
            }
            e = e.link
        }
        return add(a.subSequence(start, end).toString(), hash, index)
    }

    override fun get(a: CharArray?): IName? {
        return if (a == null) {
            null
        } else get(a, 0, a.size)
    }

    override fun get(a: CharArray, start: Int, end: Int): IName? {
        val hash = Name.hash(a, start, end)
        val index = hash and indexMask
        val len = end - start
        var e = entries[index]
        while (e != null) {
            if (e.length == len && equals(e, a, start, len)) {
                return e
            }
            e = e.link
        }
        return null
    }

    override fun get(a: CharSequence?): IName? {
        return if (a == null) {
            null
        } else get(a, 0, a.length)
    }

    override fun get(a: CharSequence, start: Int, end: Int): IName? {
        val hash = Name.hash(a, start, end)
        val index = hash and indexMask
        val len = end - start
        var e = entries[index]
        while (e != null) {
            if (e.length == len && equals(e, a, start, len)) {
                return e
            }
            e = e.link
        }
        return null
    }

    override fun get(index: Int): IName? {
        return if (index < 0 || index >= ordered.size) {
            null
        } else ordered[index]
    }

    override fun iterator(): MutableIterator<IName> {
        return NameIterator()
    }

    fun contains(key: CharSequence, start: Int, end: Int): Boolean {
        return get(key, start, end) != null
    }

    operator fun contains(key: CharSequence?): Boolean {
        return key != null && get(key, 0, key.length) != null
    }

    override fun clear() {
        for (i in entries.indices.reversed()) {
            entries[i] = null
        }
        init(initCapacity, loadFactor)
    }

    private fun add(a: String, hash: Int, index: Int): Name {
        val e = Name(entryCount, hash, a, entries[index])
        entries[index] = e
        if (entryCount >= ordered.size) {
            ordered = Arrays.copyOf(ordered, ordered.size * 2)
        }
        ordered[entryCount] = e
        ++entryCount
        if (entryCount >= threshold && entries.size < MAX_SIZE) {
            rehash(entries.size shl 1)
        }
        return e
    }

    /* @capacity Must be power of 2. */
    private fun rehash(capacity: Int) {
        var capacity = capacity
        if (capacity > MAX_SIZE) {
            capacity = MAX_SIZE
        }
        val a = arrayOfNulls<Name>(capacity)
        val m = capacity - 1
        var e: Name?
        var next: Name?
        for (i in entries.indices.reversed()) {
            e = entries[i]
            entries[i] = null
            while (e != null) {
                next = e.link
                val index = e.hashcode and m
                e.link = a[index]
                a[index] = e
                e = next
            }
        }
        threshold = if (capacity >= MAX_SIZE) {
            Int.MAX_VALUE
        } else {
            (capacity * loadFactor).toInt()
        }
        indexMask = m
        entries = a
    } ////////////////////////////////////////////////////////////////////////

    companion object {
        ////////////////////////////////////////////////////////////////////////
        protected const val DEBUG = false
        protected const val MAX_SIZE = 1 shl 30
        protected const val DEF_SIZE = 256
        protected const val DEF_LOAD_FACTOR = 0.75f

        ////////////////////////////////////////////////////////////////////////
        private fun equals(k: Name, a: CharSequence, start: Int, len: Int): Boolean {
            var len = len
            val s = k.s
            var end = start + len
            while (--len >= 0) {
                if (s[len] != a[--end]) {
                    return false
                }
            }
            return true
        }

        private fun equals(k: Name, a: CharArray, start: Int, len: Int): Boolean {
            var len = len
            val s = k.s
            var end = start + len
            while (--len >= 0) {
                if (s[len] != a[--end]) {
                    return false
                }
            }
            return true
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
