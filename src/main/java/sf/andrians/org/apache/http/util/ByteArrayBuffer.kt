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

import java.io.Serializable

/**
 * A resizable byte array.
 *
 * @since 4.0
 */
class ByteArrayBuffer(capacity: Int) : Serializable {
    private var buffer: ByteArray
    private var len = 0
    private fun expand(newlen: Int) {
        val newbuffer = ByteArray(Math.max(buffer.size shl 1, newlen))
        System.arraycopy(buffer, 0, newbuffer, 0, len)
        buffer = newbuffer
    }

    /**
     * Appends `len` bytes to this buffer from the given source
     * array starting at index `off`. The capacity of the buffer
     * is increased, if necessary, to accommodate all `len` bytes.
     *
     * @param   b        the bytes to be appended.
     * @param   off      the index of the first byte to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if `off` if out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: ByteArray?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        if (off < 0 || off > b.size || len < 0 ||
                off + len < 0 || off + len > b.size) {
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
     * Appends `b` byte to this buffer. The capacity of the buffer
     * is increased, if necessary, to accommodate the additional byte.
     *
     * @param   b        the byte to be appended.
     */
    fun append(b: Int) {
        val newlen = len + 1
        if (newlen > buffer.size) {
            expand(newlen)
        }
        buffer[len] = b.toByte()
        len = newlen
    }

    /**
     * Appends `len` chars to this buffer from the given source
     * array starting at index `off`. The capacity of the buffer
     * is increased if necessary to accommodate all `len` chars.
     *
     *
     * The chars are converted to bytes using simple cast.
     *
     * @param   b        the chars to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if `off` if out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: CharArray?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        if (off < 0 || off > b.size || len < 0 ||
                off + len < 0 || off + len > b.size) {
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
            val c = b[i1].code
            if (c >= 0x20 && c <= 0x7E ||
                c >= 0xA0 && c <= 0xFF ||
                c == 0x09) {
                buffer[i2] = c.toByte()
            } else {
                buffer[i2] = '?'.code.toByte()
            }
            i1++
            i2++
        }
        this.len = newlen
    }

    /**
     * Appends `len` chars to this buffer from the given source
     * char array buffer starting at index `off`. The capacity
     * of the buffer is increased if necessary to accommodate all
     * `len` chars.
     *
     *
     * The chars are converted to bytes using simple cast.
     *
     * @param   b        the chars to be appended.
     * @param   off      the index of the first char to append.
     * @param   len      the number of bytes to append.
     * @throws IndexOutOfBoundsException if `off` if out of
     * range, `len` is negative, or
     * `off` + `len` is out of range.
     */
    fun append(b: CharArrayBuffer?, off: Int, len: Int) {
        if (b == null) {
            return
        }
        append(b.buffer(), off, len)
    }

    /**
     * Clears content of the buffer. The underlying byte array is not resized.
     */
    fun clear() {
        len = 0
    }

    /**
     * Converts the content of this buffer to an array of bytes.
     *
     * @return byte array
     */
    fun toByteArray(): ByteArray {
        val b = ByteArray(len)
        if (len > 0) {
            System.arraycopy(buffer, 0, b, 0, len)
        }
        return b
    }

    /**
     * Returns the `byte` value in this buffer at the specified
     * index. The index argument must be greater than or equal to
     * `0`, and less than the length of this buffer.
     *
     * @param      i   the index of the desired byte value.
     * @return     the byte value at the specified index.
     * @throws     IndexOutOfBoundsException  if `index` is
     * negative or greater than or equal to [.length].
     */
    fun byteAt(i: Int): Int {
        return buffer[i].toInt()
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage
     * available for newly appended bytes, beyond which an allocation
     * will occur.
     *
     * @return  the current capacity
     */
    fun capacity(): Int {
        return buffer.size
    }

    /**
     * Returns the length of the buffer (byte count).
     *
     * @return  the length of the buffer
     */
    fun length(): Int {
        return len
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     * If the current capacity is less than the argument, then a new internal
     * array is allocated with greater capacity. If the `required`
     * argument is non-positive, this method takes no action.
     *
     * @param   required   the minimum required capacity.
     *
     * @since 4.1
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
     * Returns reference to the underlying byte array.
     *
     * @return the byte array.
     */
    fun buffer(): ByteArray {
        return buffer
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
     * Returns `true` if this buffer is empty, that is, its
     * [.length] is equal to `0`.
     * @return `true` if this buffer is empty, `false`
     * otherwise.
     */
    val isEmpty: Boolean
        get() = len == 0

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
     * specified byte, starting the search at the specified
     * `beginIndex` and finishing at `endIndex`.
     * If no such byte occurs in this buffer within the specified bounds,
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
     * @param   b            the byte to search for.
     * @param   from         the index to start the search from.
     * @param   to           the index to finish the search at.
     * @return  the index of the first occurrence of the byte in the buffer
     * within the given bounds, or `-1` if the byte does
     * not occur.
     *
     * @since 4.1
     */
    /**
     * Returns the index within this buffer of the first occurrence of the
     * specified byte, starting the search at `0` and finishing
     * at [.length]. If no such byte occurs in this buffer within
     * those bounds, `-1` is returned.
     *
     * @param   b   the byte to search for.
     * @return  the index of the first occurrence of the byte in the
     * buffer, or `-1` if the byte does not occur.
     *
     * @since 4.1
     */

    fun indexOf(b: Byte, from: Int = 0, to: Int = len): Int {
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
            if (buffer[i] == b) {
                return i
            }
        }
        return -1
    }

    companion object {
        private const val serialVersionUID = 4359112959524048036L
    }

    /**
     * Creates an instance of [ByteArrayBuffer] with the given initial
     * capacity.
     *
     * @param capacity the capacity
     */
    init {
        Args.notNegative(capacity, "Buffer capacity")
        buffer = ByteArray(capacity)
    }
}
