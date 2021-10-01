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

object ReadOnlyCollection {

    fun <T> wrap(c: MutableCollection<T>): Collection<T> {
        return CollectionWrapper(c)
    }

    fun <T> wrap(a: Array<T>): Collection<T> {
        return ArrayWrapper(a, 0, a.size)
    }

    fun <T> wrap(a: Array<T>, start: Int, end: Int): Collection<T> {
        return ArrayWrapper(a, start, end)
    }

    ////////////////////////////////////////////////////////////////////////

    private class CollectionWrapper<T>(private val collection: MutableCollection<T>) : MutableCollection<T> {

        override val size: Int
            get() = collection.size

        override fun isEmpty(): Boolean {
            return collection.isEmpty()
        }

        override operator fun contains(element: T): Boolean {
            return collection.contains(element)
        }

        override fun iterator(): MutableIterator<T> {
            return collection.iterator()
        }

        override fun containsAll(c: Collection<T>): Boolean {
            return collection.containsAll(c)
        }

        override fun equals(o: Any?): Boolean {
            return collection == o
        }

        override fun hashCode(): Int {
            return collection.hashCode()
        }

        ////////////////////////////////////////////////////////////////////////

        override fun add(e: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun remove(o: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun addAll(c: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun removeAll(c: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun retainAll(c: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            throw UnsupportedOperationException()
        }

    }

    ////////////////////////////////////////////////////////////////////////

    private class ArrayWrapper<T>(private val array: Array<T>, private val start: Int, end: Int) : MutableCollection<T> {

        private val length = end - start

        override val size: Int
            get() = length

        override fun isEmpty(): Boolean {
            return length == 0
        }

        override operator fun contains(element: T): Boolean {
            return ArrayUtil.contains1(array, start, start + length, element)
        }

        override fun iterator(): MutableIterator<T> {
            return IterableWrapper.ArrayWrapper(array, 0, array.size)
        }

        override fun containsAll(elements: Collection<T>): Boolean {
            val end = start + length
            for (o in elements) {
                if (!ArrayUtil.contains1(array, start, end, o)) return false
            }
            return true
        }

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is ArrayWrapper<*>) return false
            val oo = other as ArrayWrapper<T>
            if (oo.array.size != array.size || oo.start != start || oo.length != length) return false
            var i = start
            val end = start + length
            while (i < end) {
                if (oo.array[i] !== array[i]) return false
                ++i
            }
            return true
        }

        override fun hashCode(): Int {
            var hashcode = 0
            var i = start
            val end = start + length
            while (i < end) {
                if (array[i] != null) hashcode = hashcode * 31 + array[i].hashCode()
                ++i
            }
            return hashcode
        }

        override fun add(element: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun remove(element: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun addAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun removeAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun retainAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            throw UnsupportedOperationException()
        }
    }
}
