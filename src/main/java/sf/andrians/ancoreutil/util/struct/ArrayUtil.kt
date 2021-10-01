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

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.util.*

object ArrayUtil {
    fun add(array: IntArray, e: Int): IntArray {
        var array = array
        val len = array.size
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun add(array: CharArray, e: Char): CharArray {
        var array = array
        val len = array.size
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun add(array: ByteArray, e: Byte): ByteArray {
        var array = array
        val len = array.size
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun addUniq(array: IntArray, e: Int): IntArray {
        var array = array
        val len = array.size
        for (i in 0 until len) {
            if (array[i] == e) {
                return array
            }
        }
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun addUniq(array: CharArray, e: Char): CharArray {
        var array = array
        val len = array.size
        for (i in 0 until len) {
            if (array[i] == e) {
                return array
            }
        }
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun addUniq(array: ByteArray, e: Byte): ByteArray {
        var array = array
        val len = array.size
        for (i in 0 until len) {
            if (array[i] == e) {
                return array
            }
        }
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun <E> add(array: Array<E>, e: E): Array<E> {
        var array = array
        val len = array.size
        array = Arrays.copyOf(array, len + 1)
        array[len] = e
        return array
    }

    fun <E> add(array: Array<E>, index: Int, e: E): Array<E> {
        val len = array.size
        val b = Arrays.copyOf(array, len + 1)
        System.arraycopy(array, 0, b, 0, index)
        b[index] = e
        System.arraycopy(array, index, b, index + 1, len - index)
        return b
    }

    fun <E> addAll(array: Array<E>, vararg a: E): Array<E> {
        var array = array
        val alen = array.size
        val blen = a.size
        array = Arrays.copyOf(array, alen + blen)
        System.arraycopy(a, 0, array, alen, blen)
        return array
    }

    fun <E> addAll(array: Array<E>, c: Collection<E>): Array<E> {
        var array = array
        val len = array.size
        array = Arrays.copyOf(array, len + c.size)
        var count = 0
        for (e in c) {
            array[len + count] = e
            ++count
        }
        return array
    }

    fun <E> remove(array: Array<E>?, e: E): Array<E>? {
        if (array != null) {
            for (i in array.indices) {
                if (e == array[i]) {
                    return remove(array, i)
                }
            }
        }
        return array
    }

    fun <E> remove(array: Array<E>?, index: Int): Array<E>? {
        if (array == null) {
            return null
        }
        val len = array.size - 1
        val b = arrayOfNulls<Any>(len) as Array<E>
        if (index > 0) {
            System.arraycopy(b, 0, array, 0, index)
        }
        if (index < len) {
            System.arraycopy(b, index, array, index + 1, len - index)
        }
        return b
    }

    fun contains(array: IntArray?, value: Int): Boolean {
        return array != null && indexOf(array, 0, array.size, value) >= 0
    }

    /** @return Index of first occurence of value in the given array, -1 if not found.
     */
    fun indexOf(array: IntArray?, value: Int): Int {
        return if (array == null) {
            -1
        } else indexOf(array, 0, array.size, value)
    }

    fun contains(array: IntArray?, start: Int, end: Int, value: Int): Boolean {
        return indexOf(array, start, end, value) >= 0
    }

    /** @return Index of first occurence of value in the given array, -1 if not found.
     */
    fun indexOf(array: IntArray?, start: Int, end: Int, value: Int): Int {
        if (array == null) {
            return -1
        }
        for (i in start until end) {
            if (array[i] == value) {
                return i
            }
        }
        return -1
    }

    fun <T> contains(array: Array<T?>, value: T): Boolean {
        return indexOf(array, 0, array.size, value) >= 0
    }

    fun <T> contains(array: Array<T?>?, start: Int, end: Int, value: T): Boolean {
        return indexOf(array, start, end, value) >= 0
    }

    fun <T> contains1(array: Array<T>?, start: Int, end: Int, value: T): Boolean {
        return indexOf1(array, start, end, value) >= 0
    }

    /** @return Index of first occurence of value in the given array, -1 if not found.
     */
    fun <T> indexOf1(array: Array<T>?, start: Int, end: Int, value: T): Int {
        if (array == null) {
            return -1
        }
        val isnull = value == null
        for (i in start until end) {
            if (isnull && array[i] == null || !isnull && value == array[i]) {
                return i
            }
        }
        return -1
    }

    /** @return Index of first occurence of value in the given array, -1 if not found.
     */
    fun <T> indexOf(array: Array<T?>?, value: T): Int {
        return if (array == null) {
            -1
        } else indexOf(array, 0, array.size, value)
    }

    /** @return Index of first occurence of value in the given array, -1 if not found.
     */
    fun <T> indexOf(array: Array<T?>?, start: Int, end: Int, value: T?): Int {
        if (array == null) {
            return -1
        }
        val isnull = value == null
        for (i in start until end) {
            if (isnull && array[i] == null || !isnull && value == array[i]) {
                return i
            }
        }
        return -1
    }

    /**
     * In place compact of given sorted array by removing duplicated elements.
     * @return end index after the last valid element.
     */
    fun <T> uniq(array: Array<T>, start: Int, end: Int, comparator: Comparator<T>): Int {
        var start = start
        var ret = end
        if (end - start <= 1) {
            return ret
        }
        var keep = array[start++]
        ret = start
        while (start < end) {
            val a = array[start]
            if (comparator.compare(keep, a) == 0) {
                ++start
                continue
            }
            keep = a // new unique element
            if (start != ret) {
                array[ret] = a
            }
            ++ret
            ++start
        }
        return ret
    }

    fun toByteArray(array: BooleanArray?): ByteArray? {
        return if (array == null) {
            null
        } else toByteArray(array, 0, array.size)
    }

    fun toByteArray(array: BooleanArray?, start: Int, end: Int): ByteArray? {
        if (array == null) {
            return null
        }
        val olen = end - start
        var len = olen / 8
        if (olen % 8 > 0) {
            ++len
        }
        val ret = ByteArray(len)
        var i = 0
        var index = start
        while (i < len) {
            var v = 0
            var k = 7
            while (k >= 0 && index < olen) {
                if (array[index++]) {
                    v = v or (1 shl k)
                }
                --k
            }
            ret[i] = v.toByte()
            ++i
        }
        return ret
    }

    fun toByteArray(array: ShortArray?): ByteArray? {
        return if (array == null) {
            null
        } else toByteArray(array, 0, array.size)
    }

    fun toByteArray(array: ShortArray?, start: Int, end: Int): ByteArray? {
        if (array == null) {
            return null
        }
        val len = end - start
        if (len == 0) {
            return Empty.BYTE_ARRAY
        }
        val a = ByteArray(len * 2)
        val b = ByteBuffer.wrap(a)
        for (i in start until end) {
            b.putShort(array[i])
        }
        return a
    }

    fun toByteArray(array: IntArray?): ByteArray? {
        return if (array == null) {
            null
        } else toByteArray(array, 0, array.size)
    }

    fun toByteArray(array: IntArray?, start: Int, end: Int): ByteArray? {
        if (array == null) {
            return null
        }
        val len = end - start
        if (len == 0) {
            return Empty.BYTE_ARRAY
        }
        val a = ByteArray(len * 4)
        val b = ByteBuffer.wrap(a)
        for (i in start until end) {
            b.putInt(array[i])
        }
        return a
    }

    fun toByteArray(array: CharArray?): ByteArray? {
        return if (array == null) {
            null
        } else toByteArray(array, 0, array.size)
    }

    fun toByteArray(array: CharArray?, start: Int, end: Int): ByteArray? {
        if (array == null) {
            return null
        }
        val len = end - start
        if (len == 0) {
            return Empty.BYTE_ARRAY
        }
        val a = ByteArray(len * 2)
        val b = ByteBuffer.wrap(a)
        for (i in start until end) {
            b.putChar(array[i])
        }
        return a
    }

    fun toIntArray(array: ByteArray?): IntArray? {
        return if (array == null) {
            null
        } else toIntArray(array, 0, array.size)
    }

    fun toCharArray(array: ByteArray?): CharArray? {
        return if (array == null) {
            null
        } else toCharArray(array, 0, array.size)
    }

    fun toIntArray(array: ByteArray?, start: Int, end: Int): IntArray? {
        if (array == null) {
            return null
        }
        val len = end - start
        if (len == 0) {
            return Empty.INT_ARRAY
        }
        if (len and 0x03 != 0) {
            throw AssertionError("ASSERT: (len & 0x03) == 0: len=$len")
        }
        val a = IntArray(len / 4)
        val b = ByteBuffer.wrap(array, start, len)
        for (i in a.indices) {
            a[i] = b.int
        }
        return a
    }

    fun toCharArray(array: ByteArray?, start: Int, end: Int): CharArray? {
        if (array == null) {
            return null
        }
        val len = end - start
        if (len == 0) {
            return Empty.CHAR_ARRAY
        }
        if (len and 0x01 != 0) {
            throw AssertionError("ASSERT: (len & 0x01) == 0: len=$len")
        }
        val a = CharArray(len / 2)
        val b = ByteBuffer.wrap(array, start, len)
        for (i in a.indices) {
            a[i] = b.char
        }
        return a
    }

    fun compare(a: ByteArray, offseta: Int, b: ByteArray, offsetb: Int, len: Int): Boolean {
        for (i in 0 until len) {
            if (a[offseta + i] != b[offsetb + i]) {
                return false
            }
        }
        return true
    }

    fun compare(a: CharArray, offseta: Int, b: CharArray, offsetb: Int, len: Int): Boolean {
        for (i in 0 until len) {
            if (a[offseta + i] != b[offsetb + i]) {
                return false
            }
        }
        return true
    }

    fun compare(a: IntArray, offseta: Int, b: IntArray, offsetb: Int, len: Int): Boolean {
        for (i in 0 until len) {
            if (a[offseta + i] != b[offsetb + i]) {
                return false
            }
        }
        return true
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: BooleanArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: ByteArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: ShortArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: IntArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: LongArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: FloatArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: DoubleArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b.toString())
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: CharArray) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b)
            if (quote != null) {
                out.append(c)
            }
        }
    }

    fun toString(out: PrintWriter, sep: String?, quote: Char?, a: Array<String?>) {
        var first = true
        val c = quote ?: ' '
        for (b in a) {
            if (first) {
                first = false
            } else {
                out.append(sep)
            }
            if (quote != null) {
                out.append(c)
            }
            out.append(b)
            if (quote != null) {
                out.append(c)
            }
        }
    } // FIXME: This do not works in sun jdk, a cast is still required.
}
