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
package sf.andrians.ancoreutil.util.io

import sf.andrians.ancoreutil.util.struct.IByteSequence
import sf.andrians.ancoreutil.util.struct.IntValue
import sf.andrians.ancoreutil.util.text.TextUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteOrder
import java.nio.charset.Charset

object ByteIOUtil {

    private fun ByteArray.u8(offset: Int): Int {
        return (this[offset].toInt() and 0xff)
    }

    private fun ByteArray.u8(offset: Int, shift: Int): Int {
        return this.u8(offset) shl shift
    }

    val isNativeBigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN

    fun getNativeOrderBinaryReader(buf: ByteArray): IBinaryReader {
        return if (isNativeBigEndian) BEByteArrayBinaryReader(buf) else LEByteArrayBinaryReader(buf)
    }

    fun getNativeOrderBinaryReader(buf: ByteArray, charset: Charset = TextUtil.UTF8()): IBinaryReader {
        return if (isNativeBigEndian) BEByteArrayBinaryReader(buf, charset) else LEByteArrayBinaryReader(buf, charset)
    }

    /**
     * Read from the given byte array.
     * @return A new byte array with a copy of the given range.
     */
    fun readFully(a: ByteArray, offset: Int, len: Int): ByteArray {
        val ret = ByteArray(len)
        System.arraycopy(a, offset, ret, 9, len)
        return ret
    }

    @Throws(IOException::class)
    fun readFully(input: InputStream, a: ByteArray): ByteArray {
        readFully(input, a, 0, a.size)
        return a
    }

    @Throws(IOException::class)
    fun readFully(input: InputStream, a: ByteArray, offset: Int, len: Int) {
        var offset1 = offset
        var len1 = len
        val length = len1
        while (len1 > 0) {
            val n = input.read(a, offset1, len1)
            if (n < 0) {
                throw IOException("Reading pass end of file: expected=" + length + ", actual=" + (length - len1))
            }
            len1 -= n
            offset1 += n
        }
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached, and returns number of bytes read.
     */
    @Throws(IOException::class)
    fun readWhilePossible(input: InputStream, a: ByteArray, offset: Int = 0, len: Int = a.size): Int {
        var offset1 = offset
        var len1 = len
        val length = len1
        while (len1 > 0) {
            val n = input.read(a, offset1, len1)
            if (n < 0) {
                break
            }
            len1 -= n
            offset1 += n
        }
        return length - len1
    }

    /**
     * Skip using InputStream.read() instead of InputStream.skip().
     */
    @Throws(IOException::class)
    fun skipFully(input: InputStream, length: Long) {
        val n = skipWhilePossible(input, length)
        if (n != length) {
            throw IOException("Skip failed: expected=" + length + "actual=" + n)
        }
    }

    /**
     * @return Number of bytes skipped.
     */
    @Throws(IOException::class)
    fun skipWhilePossible(input: InputStream, length: Long): Long {
        val buf = ByteArray(16 * 1024)
        var remaining = length
        while (remaining > 0) {
            val len = Math.min(buf.size.toLong(), remaining).toInt()
            val n = input.read(buf, 0, len)
            if (n < 0) {
                return length - remaining
            }
            remaining -= n.toLong()
        }
        return length
    }

    ////////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun read8(input: InputStream): Byte {
        val b = input.read()
        if (b < 0) {
            throw IOException("Reading pass end of file")
        }
        return b.toByte()
    }

    @Throws(IOException::class)
    fun readU8(input: InputStream): Int {
        val b = input.read()
        if (b < 0) {
            throw IOException("Reading pass end of file")
        }
        return b and 0xff
    }

    @Throws(IOException::class)
    fun read16BE(input: InputStream): Short {
        val b = ByteArray(2)
        readFully(input, b)
        return read16BE(b, 0)
    }

    @Throws(IOException::class)
    fun read16LE(input: InputStream): Short {
        val b = ByteArray(2)
        readFully(input, b)
        return read16LE(b, 0)
    }

    @Throws(IOException::class)
    fun readU16BE(input: InputStream): Int {
        val b = ByteArray(2)
        readFully(input, b)
        return readU16BE(b, 0)
    }

    @Throws(IOException::class)
    fun readU16LE(input: InputStream): Int {
        val b = ByteArray(2)
        readFully(input, b)
        return readU16LE(b, 0)
    }

    @Throws(IOException::class)
    fun read32BE(input: InputStream): Int {
        val b = ByteArray(4)
        readFully(input, b)
        return read32BE(b, 0)
    }

    @Throws(IOException::class)
    fun read32LE(input: InputStream): Int {
        val b = ByteArray(4)
        readFully(input, b)
        return read32LE(b, 0)
    }

    @Throws(IOException::class)
    fun readU32BE(input: InputStream): Long {
        val b = ByteArray(4)
        readFully(input, b)
        return readU32BE(b, 0)
    }

    @Throws(IOException::class)
    fun readU32LE(input: InputStream): Long {
        val b = ByteArray(4)
        readFully(input, b)
        return readU32LE(b, 0)
    }

    @Throws(IOException::class)
    fun read64BE(input: InputStream): Long {
        val b = ByteArray(8)
        readFully(input, b)
        return read64BE(b, 0)
    }

    @Throws(IOException::class)
    fun read64LE(input: InputStream): Long {
        val b = ByteArray(8)
        readFully(input, b)
        return read64LE(b, 0)
    }

    @Throws(IOException::class)
    fun readChars(input: InputStream, len: Int): CharArray {
        val a = CharArray(len)
        val b = ByteArray(len)
        readFully(input, b)
        for (i in b.indices) {
            a[i] = (b[i].toInt() and 0xff).toChar()
        }
        return a
    }

    /**
     * Read null terminated string out of the given number of bytes from the input stream.
     */
    @Throws(IOException::class)
    fun readCharZ(input: InputStream, len: Int): String {
        val a = CharArray(len)
        val b = ByteArray(len)
        readFully(input, b)
        for (i in b.indices) {
            val v = b[i].toInt()
            if (v == 0) {
                return String(a, 0, i)
            }
            a[i] = (v and 0xff).toChar()
        }
        return String(a)
    }

    /**
     * Read null terminated string from the input stream.
     */
    @Throws(IOException::class)
    fun readCharZ(input: InputStream): CharSequence {
        val ret = StringBuilder()
        var b: Int
        while (true) {
            b = input.read()
            if (b < 0) {
                throw IOException("Reading pass end of file")
            }
            if (b == 0) {
                break
            }
            ret.append(b.toChar())
        }
        return ret
    }

    @Throws(IOException::class)
    fun read16(input: InputStream): Short {
        return if (isNativeBigEndian) read16BE(input) else read16LE(input)
    }

    @Throws(IOException::class)
    fun readU16(input: InputStream): Int {
        return if (isNativeBigEndian) readU16BE(input) else readU16LE(input)
    }

    @Throws(IOException::class)
    fun read32(input: InputStream): Int {
        return if (isNativeBigEndian) read32BE(input) else read32LE(input)
    }

    @Throws(IOException::class)
    fun readU32(input: InputStream): Long {
        return if (isNativeBigEndian) readU32BE(input) else readU32LE(input)
    }

    @Throws(IOException::class)
    fun read64(input: InputStream): Long {
        return if (isNativeBigEndian) read64BE(input) else read64LE(input)
    }

    ////////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    fun write16BE(output: OutputStream, value: Int) {
        val b = ByteArray(2)
        b[0] = (value shr 8 and 0xff).toByte()
        b[1] = (value and 0xff).toByte()
        output.write(b)
    }

    @Throws(IOException::class)
    fun write16LE(output: OutputStream, value: Int) {
        val b = ByteArray(2)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        output.write(b)
    }

    @Throws(IOException::class)
    fun write32BE(output: OutputStream, value: Int) {
        val b = ByteArray(4)
        b[0] = (value shr 24 and 0xff).toByte()
        b[1] = (value shr 16 and 0xff).toByte()
        b[2] = (value shr 8 and 0xff).toByte()
        b[3] = (value and 0xff).toByte()
        output.write(b)
    }

    @Throws(IOException::class)
    fun write32LE(output: OutputStream, value: Int) {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        output.write(b)
    }

    @Throws(IOException::class)
    fun write64BE(output: OutputStream, value: Long) {
        val b = ByteArray(8)
        write64BE(b, 0, value)
        output.write(b)
    }

    @Throws(IOException::class)
    fun write64LE(output: OutputStream, value: Long) {
        val b = ByteArray(8)
        write64LE(b, 0, value)
        output.write(b)
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    fun write(out: OutputStream, b: Int) {
        out.write(b)
    }

    @Throws(IOException::class)
    fun write(out: OutputStream, a: ByteArray) {
        out.write(a, 0, a.size)
    }

    @Throws(IOException::class)
    fun write(out: OutputStream, a: ByteArray, start: Int, end: Int) {
        out.write(a, start, end - start)
    }

    @Throws(IOException::class)
    fun write16(output: OutputStream, value: Int) {
        if (isNativeBigEndian) {
            write16BE(output, value)
        } else {
            write16LE(output, value)
        }
    }

    @Throws(IOException::class)
    fun write32(output: OutputStream, value: Int) {
        if (isNativeBigEndian) {
            write32BE(output, value)
        } else {
            write32LE(output, value)
        }
    }

    @Throws(IOException::class)
    fun write64(output: OutputStream, value: Long) {
        if (isNativeBigEndian) {
            write64BE(output, value)
        } else {
            write64LE(output, value)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun read8(b: IByteSequence, offset: Int): Byte {
        return b.byteAt(offset)
    }

    fun readU8(b: IByteSequence, offset: Int): Int {
        return b.u8(offset)
    }

    fun read16BE(b: IByteSequence, offset: Int): Short {
        return (b.u8(offset, 8) or b.u8(offset + 1)).toShort()
    }

    fun read16LE(b: IByteSequence, offset: Int): Short {
        return (b.u8(offset) or (b.u8(offset + 1, 8))).toShort()
    }

    fun readU16BE(b: IByteSequence, offset: Int): Int {
        return (b.u8(offset, 8) or b.u8(offset + 1)) and 0xffff
    }

    fun readU16LE(b: IByteSequence, offset: Int): Int {
        return (b.u8(offset) or b.u8(offset + 1, 8)) and 0xffff
    }

    fun read32BE(b: IByteSequence, offset: Int): Int {
        return b.u8(offset, 24) or
                b.u8(offset + 1, 16) or
                b.u8(offset + 2, 8) or
                b.u8(offset + 3)
    }

    fun read32LE(b: IByteSequence, offset: Int): Int {
        return b.u8(offset) or
                b.u8(offset + 1, 8) or
                b.u8(offset + 2, 16) or
                b.u8(offset + 3, 24)
    }

    fun readU32BE(b: IByteSequence, offset: Int): Long {
        return read32BE(b, offset).toLong() and 0xffff_ffffL
    }

    fun readU32LE(b: IByteSequence, offset: Int): Long {
        return read32LE(b, offset).toLong() and 0xffff_ffffL
    }

    fun read64BE(b: IByteSequence, offset: Int): Long {
        return (readU32BE(b, offset) shl 32) or readU32BE(b, offset + 4)
    }

    fun read64LE(b: IByteSequence, offset: Int): Long {
        return readU32LE(b, offset) or (readU32LE(b, offset + 4) shl 32)
    }

    fun read16(b: IByteSequence, offset: Int): Short {
        return if (isNativeBigEndian) read16BE(b, offset) else read16LE(b, offset)
    }

    fun readU16(b: IByteSequence, offset: Int): Int {
        return if (isNativeBigEndian) readU16BE(b, offset) else readU16LE(b, offset)
    }

    fun read32(b: IByteSequence, offset: Int): Int {
        return if (isNativeBigEndian) read32BE(b, offset) else read32LE(b, offset)
    }

    fun readU32(b: IByteSequence, offset: Int): Long {
        return if (isNativeBigEndian) readU32BE(b, offset) else readU32LE(b, offset)
    }

    fun read64(b: IByteSequence, offset: Int): Long {
        return if (isNativeBigEndian) read64BE(b, offset) else read64LE(b, offset)
    }

    ////////////////////////////////////////////////////////////////////////

    fun read8(b: ByteArray, offset: Int): Byte {
        return b[offset]
    }

    fun readU8(b: ByteArray, offset: Int): Int {
        return b.u8(offset)
    }

    fun read16BE(b: ByteArray, offset: Int): Short {
        return i16BE(b[offset], b[offset + 1])
    }

    fun read16LE(b: ByteArray, offset: Int): Short {
        return i16LE(b[offset], b[offset + 1])
    }

    fun readU16BE(b: ByteArray, offset: Int): Int {
        return u16BE(b[offset], b[offset + 1])
    }

    fun readU16LE(b: ByteArray, offset: Int): Int {
        return u16LE(b[offset], b[offset + 1])
    }

    fun read32BE(b: ByteArray, offset: Int): Int {
        return i32BE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun read32LE(b: ByteArray, offset: Int): Int {
        return i32LE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun readU32BE(b: ByteArray, offset: Int): Long {
        return u32BE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun readU32LE(b: ByteArray, offset: Int): Long {
        return u32LE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun read64BE(b: ByteArray, offset: Int): Long {
        return readU32BE(b, offset) shl 32 or readU32BE(b, offset + 4)
    }

    fun read64LE(b: ByteArray, offset: Int): Long {
        return readU32LE(b, offset) or (readU32LE(b, offset + 4) shl 32)
    }

    fun read16(b: ByteArray, offset: Int): Short {
        return if (isNativeBigEndian) read16BE(b, offset) else read16LE(b, offset)
    }

    fun readU16(b: ByteArray, offset: Int): Int {
        return if (isNativeBigEndian) readU16BE(b, offset) else readU16LE(b, offset)
    }

    fun read32(b: ByteArray, offset: Int): Int {
        return if (isNativeBigEndian) read32BE(b, offset) else read32LE(b, offset)
    }

    fun readU32(b: ByteArray, offset: Int): Long {
        return if (isNativeBigEndian) readU32BE(b, offset) else readU32LE(b, offset)
    }

    fun read64(b: ByteArray, offset: Int): Long {
        return if (isNativeBigEndian) read64BE(b, offset) else read64LE(b, offset)
    }

    ////////////////////////////////////////////////////////////////////////

    fun read8(b: ByteArray, offset: IntValue): Byte {
        return b[offset.value++]
    }

    fun readU8(b: ByteArray, offset: IntValue): Int {
        return b.u8(offset.value++)
    }

    fun read16BE(b: ByteArray, offset: IntValue): Short {
        return i16BE(b[offset.value++], b[offset.value++])
    }

    fun read16LE(b: ByteArray, offset: IntValue): Short {
        return i16LE(b[offset.value++], b[offset.value++])
    }

    fun readU16BE(b: ByteArray, offset: IntValue): Int {
        return u16BE(b[offset.value++], b[offset.value++])
    }

    fun readU16LE(b: ByteArray, offset: IntValue): Int {
        return u16LE(b[offset.value++], b[offset.value++])
    }

    fun read32BE(b: ByteArray, offset: IntValue): Int {
        return i32BE(b[offset.value++],
                b[offset.value++],
                b[offset.value++],
                b[offset.value++])
    }

    fun read32LE(b: ByteArray, offset: IntValue): Int {
        return i32LE(b[offset.value++],
                b[offset.value++],
                b[offset.value++],
                b[offset.value++])
    }

    fun readU32BE(b: ByteArray, offset: IntValue): Long {
        return u32LE(b[offset.value++],
                b[offset.value++],
                b[offset.value++],
                b[offset.value++])
    }

    fun readU32LE(b: ByteArray, offset: IntValue): Long {
        return u32LE(b[offset.value++],
                b[offset.value++],
                b[offset.value++],
                b[offset.value++])
    }

    fun read64BE(b: ByteArray, offset: IntValue): Long {
        return readU32BE(b, offset) shl 32 or readU32BE(b, offset)
    }

    fun read64LE(b: ByteArray, offset: IntValue): Long {
        return readU32LE(b, offset) or (readU32LE(b, offset) shl 32)
    }

    fun read16(b: ByteArray, offset: IntValue): Short {
        return if (isNativeBigEndian) read16BE(b, offset) else read16LE(b, offset)
    }

    fun readU16(b: ByteArray, offset: IntValue): Int {
        return if (isNativeBigEndian) readU16BE(b, offset) else readU16LE(b, offset)
    }

    fun read32(b: ByteArray, offset: IntValue): Int {
        return if (isNativeBigEndian) read32BE(b, offset) else read32LE(b, offset)
    }

    fun readU32(b: ByteArray, offset: IntValue): Long {
        return if (isNativeBigEndian) readU32BE(b, offset) else readU32LE(b, offset)
    }

    fun read64(b: ByteArray, offset: IntValue): Long {
        return if (isNativeBigEndian) read64BE(b, offset) else read64LE(b, offset)
    }

    ////////////////////////////////////////////////////////////////////////

    fun write(b: ByteArray, offset: Int, value: Byte): Int {
        var offset = offset
        b[offset++] = value
        return offset
    }

    fun write16BE(b: ByteArray, offset: Int, value: Int): Int {
        var offset = offset
        b[offset++] = (value shr 8).toByte()
        b[offset++] = value.toByte()
        return offset
    }

    fun write16LE(b: ByteArray, offset: Int, value: Int): Int {
        var offset = offset
        b[offset++] = value.toByte()
        b[offset++] = (value shr 8).toByte()
        return offset
    }

    fun write32BE(b: ByteArray, offset: Int, value: Int): Int {
        var offset = offset
        b[offset++] = (value shr 24).toByte()
        b[offset++] = (value shr 16).toByte()
        b[offset++] = (value shr 8).toByte()
        b[offset++] = value.toByte()
        return offset
    }

    fun write32LE(b: ByteArray, offset: Int, value: Int): Int {
        var offset = offset
        b[offset++] = value.toByte()
        b[offset++] = (value shr 8).toByte()
        b[offset++] = (value shr 16).toByte()
        b[offset++] = (value shr 24).toByte()
        return offset
    }

    fun write64BE(b: ByteArray, offset: Int, value: Long): Int {
        var offset = offset
        offset = write32BE(b, offset, (value ushr 32).toInt())
        return write32BE(b, offset, (value and 0xffffffffL).toInt())
    }

    fun write64LE(b: ByteArray, offset: Int, value: Long): Int {
        var offset = offset
        offset = write32LE(b, offset, (value and 0xffffffffL).toInt())
        return write32LE(b, offset, (value ushr 32).toInt())
    }

    fun write(b: ByteArray, offset: Int, value: ByteArray): Int {
        System.arraycopy(value, 0, b, offset, value.size)
        return offset + value.size
    }

    fun write(b: ByteArray, offset: Int, value: ByteArray, start: Int, end: Int): Int {
        val len = end - start
        System.arraycopy(value, start, b, offset, len)
        return offset + len
    }

    fun write16(b: ByteArray, offset: Int, value: Int) {
        if (isNativeBigEndian) {
            write16BE(b, offset, value)
        } else {
            write16LE(b, offset, value)
        }
    }

    fun write32(b: ByteArray, offset: Int, value: Int) {
        if (isNativeBigEndian) {
            write32BE(b, offset, value)
        } else {
            write32LE(b, offset, value)
        }
    }

    fun write64(b: ByteArray, offset: Int, value: Long) {
        if (isNativeBigEndian) {
            write64BE(b, offset, value)
        } else {
            write64LE(b, offset, value)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun u8(b: Byte): Int {
        return b.toInt() and 0xff
    }

    fun u8(b: Byte, shift: Int): Int {
        return (b.toInt() and 0xff) shl shift
    }

    fun u8(b: Int): Int {
        return b and 0xff
    }

    fun u8(b: Int, shift: Int): Int {
        return (b and 0xff) shl shift
    }

    fun i16BE(b0: Byte, b1: Byte): Short {
        return (u8(b0, 8) or u8(b1)).toShort()
    }

    fun u16BE(b0: Byte, b1: Byte): Int {
        return u8(b0, 8) or u8(b1)
    }

    fun i16LE(b0: Byte, b1: Byte): Short {
        return (u8(b0) or u8(b1, 8)).toShort()
    }

    fun u16LE(b0: Byte, b1: Byte): Int {
        return u8(b0) or u8(b1, 8)
    }

    fun i32BE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
        return u8(b0, 24) or
                u8(b1, 16) or
                u8(b2, 8) or
                u8(b3)
    }

    fun u32BE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Long {
        return (u8(b0, 24) or
                u8(b1, 16) or
                u8(b2, 8) or
                u8(b3)).toLong() and 0xffff_ffffL
    }

    fun i32LE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
        return u8(b0) or
                u8(b1, 8) or
                u8(b2, 16) or
                u8(b3, 24)
    }

    fun u32LE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Long {
        return (u8(b0) or
                u8(b1, 8) or
                u8(b2, 16) or
                u8(b3, 24)).toLong() and 0xffff_ffffL
    }

    ////////////////////////////////////////////////////////////////////////

    fun htons(v: Short): Short {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun htonl(v: Int): Int {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun htonl(v: Long): Long {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun ntohs(v: Short): Short {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun ntohl(v: Int): Int {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun ntohl(v: Long): Long {
        return if (isNativeBigEndian) {
            v
        } else swap(v)
    }

    fun swap(x: Short): Short {
        return (x.toInt() shl 8 or (x.toInt() ushr 8)).toShort()
    }

    fun swap(x: Char): Char {
        return (x.toInt() shl 8 or (x.toInt() ushr 8)).toChar()
    }

    fun swap(x: Int): Int {
        return x shl 24 or (x and 0x0000ff00 shl 8) or (x and 0x00ff0000 ushr 8) or (x ushr 24)
    }

    fun swap(x: Long): Long {
        return (swap(x.toInt()).toLong() shl 32) or (swap((x ushr 32).toInt()).toLong() and 0xffff_ffffL)
    }

    ////////////////////////////////////////////////////////////////////////

    fun equals(a: ByteArray?, b: ByteArray?): Boolean {
        if (a == null) {
            return b == null
        }
        if (b == null) {
            return false
        }
        val len = a.size
        if (b.size != len) {
            return false
        }
        for (i in 0 until len) {
            if (a[i] != b[i]) {
                return false
            }
        }
        return true
    }

    fun equals(a: ByteArray?, b: ByteArray?, boffset: Int, blen: Int): Boolean {
        if (a == null) {
            return b == null
        }
        if (b == null) {
            return false
        }
        if (a.size != blen) {
            return false
        }
        for (i in 0 until blen) {
            if (a[i] != b[boffset + i]) {
                return false
            }
        }
        return true
    }

    ////////////////////////////////////////////////////////////////////////
}