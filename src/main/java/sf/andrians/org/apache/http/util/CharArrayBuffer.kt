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
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package sf.andrians.org.apache.http.util

import sf.andrians.org.apache.http.protocol.HTTP
import java.io.Serializable
import java.nio.CharBuffer

/**
 * A resizable char array.
 *
 * @since 4.0
 */
class CharArrayBuffer(capacity: Int) : CharSequence, Serializable {
    private var buffer: CharArray
    private var len = 0
    private fun expand(newlen: Int) {
        val newbuffer = CharArray(Math.max(buffer.size shl 1, newlen))
        System.arraycopy(buffer, 0, newbuffer, 0, len)
        buffer = newbuffer
    }

    /**
     * Appends `len` chars to this buffer from the given source
     * array starting at index `off`. The capacity of the buffer
     * is increased, if necessary, to accommodate all `len` chars.
     *
     * @param   b        the chars to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of chars to append.
     * @throws IndexOutOfBoundsException if `off` is out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: CharArray?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        if (off < 0 || off > b.size || len < 0 ||
            off + len < 0 || off + len > b.size
        ) {
            throw IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.size)
        }
        if (len == 0) {
            return
        }
        val newlen = this.len + len
        if (newlen > buffer.size) {
            expand(newlen)
        }
        System.arraycopy(b, off, buffer, this.len, len)
        this.len = newlen
    }

    /**
     * Appends chars of the given string to this buffer. The capacity of the
     * buffer is increased, if necessary, to accommodate all chars.
     *
     * @param str    the string.
     */
    fun append(str: String?) {
        val s = str ?: "null"
        val strlen = s.length
        val newlen = len + strlen
        if (newlen > buffer.size) {
            expand(newlen)
        }
        s.toCharArray(buffer, len, 0, strlen)
        len = newlen
    }

    /**
     * Appends `len` chars to this buffer from the given source
     * buffer starting at index `off`. The capacity of the
     * destination buffer is increased, if necessary, to accommodate all
     * `len` chars.
     *
     * @param   b        the source buffer to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of chars to append.
     * @throws IndexOutOfBoundsException if `off` is out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: CharArrayBuffer?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        append(b.buffer, off, len)
    }

    /**
     * Appends all chars to this buffer from the given source buffer starting
     * at index `0`. The capacity of the destination buffer is
     * increased, if necessary, to accommodate all [.length] chars.
     *
     * @param   b        the source buffer to be appended.
     */
    fun append(b: CharArrayBuffer?) {
        if (b == null) {
            return
        }
        append(b.buffer, 0, b.len)
    }

    /**
     * Appends `ch` char to this buffer. The capacity of the buffer
     * is increased, if necessary, to accommodate the additional char.
     *
     * @param   ch        the char to be appended.
     */
    fun append(ch: Char) {
        val newlen = len + 1
        if (newlen > buffer.size) {
            expand(newlen)
        }
        buffer[len] = ch
        len = newlen
    }

    /**
     * Appends `len` bytes to this buffer from the given source
     * array starting at index `off`. The capacity of the buffer
     * is increased, if necessary, to accommodate all `len` bytes.
     *
     *
     * The bytes are converted to chars using simple cast.
     *
     * @param   b        the bytes to be appended.
     * @param   off      the index of the first byte to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if `off` is out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: ByteArray?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        if (off < 0 || off > b.size || len < 0 ||
            off + len < 0 || off + len > b.size
        ) {
            throw IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.size)
        }
        if (len == 0) {
            return
        }
        val oldlen = this.len
        val newlen = oldlen + len
        if (newlen > buffer.size) {
            expand(newlen)
        }
        var i1 = off
        var i2 = oldlen
        while (i2 < newlen) {
            buffer[i2] = (b[i1].toInt() and 0xff).toChar()
            i1++
            i2++
        }
        this.len = newlen
    }

    /**
     * Appends `len` bytes to this buffer from the given source
     * array starting at index `off`. The capacity of the buffer
     * is increased, if necessary, to accommodate all `len` bytes.
     *
     *
     * The bytes are converted to chars using simple cast.
     *
     * @param   b        the bytes to be appended.
     * @param   off      the index of the first byte to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if `off` is out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: ByteArrayBuffer?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        append(b.buffer(), off, len)
    }

    /**
     * Clears content of the buffer. The underlying char array is not resized.
     */
    fun clear() {
        len = 0
    }

    /**
     * Converts the content of this buffer to an array of chars.
     *
     * @return char array
     */
    fun toCharArray(): CharArray {
        val b = CharArray(len)
        if (len > 0) {
            System.arraycopy(buffer, 0, b, 0, len)
        }
        return b
    }

    /**
     * Returns the `char` value in this buffer at the specified
     * index. The index argument must be greater than or equal to
     * `0`, and less than the length of this buffer.
     *
     * @param      index   the index of the desired char value.
     * @return     the char value at the specified index.
     * @throws     IndexOutOfBoundsException  if `index` is
     * negative or greater than or equal to [.length].
     */
    override fun get(index: Int): Char {
        return buffer[index]
    }

    /**
     * Returns reference to the underlying char array.
     *
     * @return the char array.
     */
    fun buffer(): CharArray {
        return buffer
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage
     * available for newly appended chars, beyond which an allocation will
     * occur.
     *
     * @return  the current capacity
     */
    fun capacity(): Int {
        return buffer.size
    }

    /**
     * Returns the length of the buffer (char count).
     *
     * @return  the length of the buffer
     */
    override val length: Int get() = len

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     * If the current capacity is less than the argument, then a new internal
     * array is allocated with greater capacity. If the `required`
     * argument is non-positive, this method takes no action.
     *
     * @param   required   the minimum required capacity.
     */
    fun ensureCapacity(required: Int) {
        if (required <= 0) {
            return
        }
        val available = buffer.size - len
        if (required > available) {
            expand(len + required)
        }
    }

    /**
     * Sets the length of the buffer. The new length value is expected to be
     * less than the current capacity and greater than or equal to
     * `0`.
     *
     * @param      len   the new length
     * @throws     IndexOutOfBoundsException  if the
     * `len` argument is greater than the current
     * capacity of the buffer or less than `0`.
     */
    fun setLength(len: Int) {
        if (len < 0 || len > buffer.size) {
            throw IndexOutOfBoundsException("len: " + len + " < 0 or > buffer len: " + buffer.size)
        }
        this.len = len
    }

    /**
     * Returns `true` if this buffer is full, that is, its
     * [.length] is equal to its [.capacity].
     * @return `true` if this buffer is full, `false`
     * otherwise.
     */
    val isFull: Boolean
        get() = len == buffer.size

    /**
     * Returns the index within this buffer of the first occurrence of the
     * specified character, starting the search at the specified
     * `beginIndex` and finishing at `endIndex`.
     * If no such character occurs in this buffer within the specified bounds,
     * `-1` is returned.
     *
     *
     * There is no restriction on the value of `beginIndex` and
     * `endIndex`. If `beginIndex` is negative,
     * it has the same effect as if it were zero. If `endIndex` is
     * greater than [.length], it has the same effect as if it were
     * [.length]. If the `beginIndex` is greater than
     * the `endIndex`, `-1` is returned.
     *
     * @param   ch     the char to search for.
     * @param   from   the index to start the search from.
     * @param   to     the index to finish the search at.
     * @return  the index of the first occurrence of the character in the buffer
     * within the given bounds, or `-1` if the character does
     * not occur.
     */
    /**
     * Returns the index within this buffer of the first occurrence of the
     * specified character, starting the search at `0` and finishing
     * at [.length]. If no such character occurs in this buffer within
     * those bounds, `-1` is returned.
     *
     * @param   ch          the char to search for.
     * @return  the index of the first occurrence of the character in the
     * buffer, or `-1` if the character does not occur.
     */

    fun indexOf(ch: Char, from: Int = 0, to: Int = len): Int {
        var beginIndex = from
        if (beginIndex < 0) {
            beginIndex = 0
        }
        var endIndex = to
        if (endIndex > len) {
            endIndex = len
        }
        if (beginIndex > endIndex) {
            return -1
        }
        for (i in beginIndex until endIndex) {
            if (buffer[i] == ch) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns a substring of this buffer. The substring begins at the specified
     * `beginIndex` and extends to the character at index
     * `endIndex - 1`.
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified substring.
     * @throws  StringIndexOutOfBoundsException  if the
     * `beginIndex` is negative, or
     * `endIndex` is larger than the length of this
     * buffer, or `beginIndex` is larger than
     * `endIndex`.
     */
    fun substring(beginIndex: Int, endIndex: Int): String {
        if (beginIndex < 0) {
            throw IndexOutOfBoundsException("Negative beginIndex: $beginIndex")
        }
        if (endIndex > len) {
            throw IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + len)
        }
        if (beginIndex > endIndex) {
            throw IndexOutOfBoundsException("beginIndex: $beginIndex > endIndex: $endIndex")
        }
        return String(buffer, beginIndex, endIndex - beginIndex)
    }

    /**
     * Returns a substring of this buffer with leading and trailing whitespace
     * omitted. The substring begins with the first non-whitespace character
     * from `beginIndex` and extends to the last
     * non-whitespace character with the index lesser than
     * `endIndex`.
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified substring.
     * @throws  IndexOutOfBoundsException  if the
     * `beginIndex` is negative, or
     * `endIndex` is larger than the length of this
     * buffer, or `beginIndex` is larger than
     * `endIndex`.
     */
    fun substringTrimmed(beginIndex: Int, endIndex: Int): String {
        if (beginIndex < 0) {
            throw IndexOutOfBoundsException("Negative beginIndex: $beginIndex")
        }
        if (endIndex > len) {
            throw IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + len)
        }
        if (beginIndex > endIndex) {
            throw IndexOutOfBoundsException("beginIndex: $beginIndex > endIndex: $endIndex")
        }
        var beginIndex0 = beginIndex
        var endIndex0 = endIndex
        while (beginIndex0 < endIndex && HTTP.isWhitespace(buffer[beginIndex0])) {
            beginIndex0++
        }
        while (endIndex0 > beginIndex0 && HTTP.isWhitespace(buffer[endIndex0 - 1])) {
            endIndex0--
        }
        return String(buffer, beginIndex0, endIndex0 - beginIndex0)
    }

    /**
     * {@inheritDoc}
     * @since 4.4
     */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        if (startIndex < 0) {
            throw IndexOutOfBoundsException("Negative beginIndex: $startIndex")
        }
        if (endIndex > len) {
            throw IndexOutOfBoundsException("endIndex: $endIndex > length: $len")
        }
        if (startIndex > endIndex) {
            throw IndexOutOfBoundsException("beginIndex: $startIndex > endIndex: $endIndex")
        }
        return CharBuffer.wrap(buffer, startIndex, endIndex)
    }

    override fun toString(): String {
        return String(buffer, 0, len)
    }

    companion object {
        private const val serialVersionUID = -6208952725094867135L
    }

    /**
     * Creates an instance of [CharArrayBuffer] with the given initial
     * capacity.
     *
     * @param capacity the capacity
     */
    init {
        Args.notNegative(capacity, "Buffer capacity")
        buffer = CharArray(capacity)
    }
}
