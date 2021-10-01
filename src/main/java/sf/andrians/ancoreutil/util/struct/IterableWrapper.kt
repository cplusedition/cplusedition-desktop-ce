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

object IterableWrapper {
    fun <T> wrap(e: Enumeration<*>): Iterable<T> {
        return EnumerationIterable(e)
    }

    fun <T> wrap(list: List<T>, start: Int, end: Int): Iterable<T> {
        return ListIterable(list, start, end)
    }

    fun <T> wrap(it: MutableIterator<T>): Iterable<T> {
        return IteratorWrapper(it)
    }

    fun <T> wrap(vararg a: T): Iterable<T> {
        return ArrayWrapper(a, 0, a.size)
    }

    fun <T> wrap(a: Array<T>, start: Int, end: Int): Iterable<T> {
        return ArrayWrapper(a, start, end)
    }

    fun <T> wrapCountable(vararg a: T): ICountableIterable<T> {
        return CountableArrayWrapper(a, 0, a.size)
    }

    fun <T> wrapCountable(a: Array<T>, start: Int, end: Int): ICountableIterable<T> {
        return CountableArrayWrapper(a, start, end)
    }

    fun <T> readonly(it: Iterator<T>): Iterable<T> {
        return ReadOnlyIterator(it)
    }

    fun <T> empty(): Iterable<T> {
        return EmptyIterable()
    }

    private class EnumerationIterable<T>(private val e: Enumeration<*>) : Iterable<T>, MutableIterator<T> {
        override fun iterator(): MutableIterator<T> {
            return this
        }

        override fun hasNext(): Boolean {
            return e.hasMoreElements()
        }

        override fun next(): T {
            return e.nextElement() as T
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

    }

    private class IteratorWrapper<T>(private val it: MutableIterator<T>) : Iterable<T>, MutableIterator<T> {
        override fun iterator(): MutableIterator<T> {
            return this
        }

        override fun hasNext(): Boolean {
            return it.hasNext()
        }

        override fun next(): T {
            return it.next()
        }

        override fun remove() {
            it.remove()
        }

    }

    open class ArrayWrapper<T>(
            protected var a: Array<out T>,
            protected var start: Int,
            protected var end: Int
    ) : Iterable<T>, MutableIterator<T> {
        override fun iterator(): MutableIterator<T> {
            return this
        }

        override fun hasNext(): Boolean {
            return start < end
        }

        override fun next(): T {
            if (start >= end) throw NoSuchElementException()
            return a[start++]
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

    }

    private class CountableArrayWrapper<T>(a: Array<out T>, start: Int, end: Int) : ArrayWrapper<T>(a, start, end), ICountableIterable<T> {
        private val size: Int = end - start
        override val isEmpty: Boolean
            get() = end == start

        override fun size(): Int {
            return size
        }

    }
}
