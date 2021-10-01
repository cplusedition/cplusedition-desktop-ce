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
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder

object ByteInputStream {

    ////////////////////////////////////////////////////////////////////

    internal const val ERROR_EOF = "Reading pass end of input"
    internal val BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN

    ////////////////////////////////////////////////////////////////////

    fun wrap(buf: ByteArray): IByteInputStream {
        return ByteArrayInputStream(buf)
    }

    fun wrap(s: IByteSequence): IByteInputStream {
        return ByteSequenceInputStream(s)
    }

    internal abstract class Base : InputStream(), IByteInputStream {
        @Throws(IOException::class)
        override fun readBool(): Boolean {
            return read8().toInt() != 0
        }

        @Throws(IOException::class)
        override fun readU32BE(): Long {
            return (read32BE().toLong() and 0xffff_ffffL)
        }

        @Throws(IOException::class)
        override fun read64BE(): Long {
            val high = readU32BE()
            val low = readU32BE()
            return high shl 32 or low
        }

        @Throws(IOException::class)
        override fun readU32LE(): Long {
            return (read32LE().toLong() and 0xffff_ffffL)
        }

        @Throws(IOException::class)
        override fun read64LE(): Long {
            val low = readU32LE()
            val high = readU32LE()
            return high shl 32 or low
        }

        ////////////////////////////////////////////////////////////////////

        @Throws(IOException::class)
        override fun read16(): Short {
            return if (BIG_ENDIAN) read16BE() else read16LE()
        }

        @Throws(IOException::class)
        override fun readU16(): Int {
            return if (BIG_ENDIAN) readU16BE() else readU16LE()
        }

        @Throws(IOException::class)
        override fun read32(): Int {
            return if (BIG_ENDIAN) read32BE() else read32LE()
        }

        @Throws(IOException::class)
        override fun readU32(): Long {
            return if (BIG_ENDIAN) readU32BE() else readU32LE()
        }

        @Throws(IOException::class)
        override fun read64(): Long {
            return if (BIG_ENDIAN) read64BE() else read64LE()
        }

        ////////////////////////////////////////////////////////////////////
        @Throws(IOException::class)
        override fun readFully(b: ByteArray): ByteArray {
            return readFully(b, 0, b.size)
        }

        @Throws(IOException::class)
        override fun readFully(b: ByteArray, offset: Int, len: Int): ByteArray {
            var count = 0
            do {
                val n = read(b, offset, len - count)
                if (n < 0) {
                    throw IOException(ERROR_EOF)
                }
                count += n
            } while (count < len)
            return b
        }

        @Throws(IOException::class)
        override fun align(start: Int, alignment: Int) {
            var pos = position() - start.toLong()
            if (pos % alignment != 0L) {
                pos = (pos + alignment - 1 and (alignment - 1).inv().toLong()) + start
                if (pos > Int.MAX_VALUE) {
                    throw IOException("Seeking pass end of file: pos=$pos")
                }
                seek(pos.toInt())
            }
        }

        override fun flush() {}
    }

    ////////////////////////////////////////////////////////////////////

    private class ByteArrayInputStream internal constructor(
            override var buffer: ByteArray
    ) : Base() {

        private var position = 0

        override fun read(): Int {
            return if (position >= buffer.size) {
                -1
            } else buffer[position++].toInt() and 0xff
        }

        override fun read(b: ByteArray, offset: Int, len: Int): Int {
            val left = buffer.size - position
            if (left <= 0) {
                return -1
            }
            val count = if (len > left) left else len
            System.arraycopy(buffer, position, b, offset, count)
            position += count
            return count
        }

        ////////////////////////////////////////////////////////////////////

        /**
         * This is similar to read() but throws an IOException instead of return -1 on EOF.
         */
        @Throws(IOException::class)
        override fun read8(): Byte {
            if (position >= buffer.size) {
                throw IOException(ERROR_EOF)
            }
            return buffer[position++]
        }

        @Throws(IOException::class)
        override fun readU8(): Int {
            if (position >= buffer.size) {
                throw IOException(ERROR_EOF)
            }
            return buffer[position++].toInt() and 0xff
        }

        @Throws(IOException::class)
        override fun read16BE(): Short {
            if (position + 2 > buffer.size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            return ByteIOUtil.i16BE(b0, b1)
        }

        @Throws(IOException::class)
        override fun readU16BE(): Int {
            if (position + 2 > buffer.size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            return ByteIOUtil.u16BE(b0, b1)
        }

        @Throws(IOException::class)
        override fun read32BE(): Int {
            if (position + 4 > buffer.size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            val b2 = buffer[position++]
            val b3 = buffer[position++]
            return ByteIOUtil.i32BE(b0, b1, b2, b3)
        }

        ////////////////////////////////////////////////////////////////////

        @Throws(IOException::class)
        override fun read16LE(): Short {
            if (position + 2 > buffer.size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            return ByteIOUtil.i16LE(b0, b1)
        }

        @Throws(IOException::class)
        override fun readU16LE(): Int {
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            return ByteIOUtil.u16LE(b0, b1)
        }

        @Throws(IOException::class)
        override fun read32LE(): Int {
            if (position + 4 > buffer.size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = buffer[position++]
            val b1 = buffer[position++]
            val b2 = buffer[position++]
            val b3 = buffer[position++]
            return ByteIOUtil.i32LE(b0, b1, b2, b3)
        }

        override fun position(): Int {
            return position
        }

        override fun length(): Int {
            return buffer.size
        }

        @Throws(IOException::class)
        override fun available(): Int {
            return buffer.size - position
        }

        @Throws(IOException::class)
        override fun seek(pos: Int) {
            position = pos
        }

        override fun close() {
            buffer = ByteArray(0)
            position = 0
        }

    }

    ////////////////////////////////////////////////////////////////////

    private class ByteSequenceInputStream internal constructor(private val input: IByteSequence) : Base() {

        private val size = input.size()
        private var position = 0

        ////////////////////////////////////////////////////////////////////

        override fun read(): Int {
            return if (position >= size) {
                -1
            } else input.byteAt(position++).toInt() and 0xff
        }

        override fun read(b: ByteArray, offset: Int, len: Int): Int {
            val left = size - position
            if (left <= 0) {
                return -1
            }
            val count = if (len > left) left else len
            val end = position + count
            input.getBytes(position, end, b, offset)
            position = end
            return count
        }

        ////////////////////////////////////////////////////////////////////

        /**
         * This is similar to read() but throws an IOException instead of return -1 on EOF.
         */
        @Throws(IOException::class)
        override fun read8(): Byte {
            if (position >= size) {
                throw IOException(ERROR_EOF)
            }
            return input.byteAt(position++)
        }

        @Throws(IOException::class)
        override fun readU8(): Int {
            if (position >= size) {
                throw IOException(ERROR_EOF)
            }
            return input.byteAt(position++).toInt() and 0xff
        }

        @Throws(IOException::class)
        override fun read16BE(): Short {
            if (position + 2 > size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            return ByteIOUtil.i16BE(b0, b1)
        }

        @Throws(IOException::class)
        override fun readU16BE(): Int {
            if (position + 2 > size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            return ByteIOUtil.u16BE(b0, b1)
        }

        @Throws(IOException::class)
        override fun read32BE(): Int {
            if (position + 4 > size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            val b2 = input.byteAt(position++)
            val b3 = input.byteAt(position++)
            return ByteIOUtil.i32BE(b0, b1, b2, b3)
        }

        ////////////////////////////////////////////////////////////////////

        @Throws(IOException::class)
        override fun read16LE(): Short {
            if (position + 2 > size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            return ByteIOUtil.i16LE(b0, b1)
        }

        @Throws(IOException::class)
        override fun readU16LE(): Int {
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            return ByteIOUtil.u16LE(b0, b1)
        }

        @Throws(IOException::class)
        override fun read32LE(): Int {
            if (position + 4 > size) {
                throw IOException(ERROR_EOF)
            }
            val b0 = input.byteAt(position++)
            val b1 = input.byteAt(position++)
            val b2 = input.byteAt(position++)
            val b3 = input.byteAt(position++)
            return ByteIOUtil.i32LE(b0, b1, b2, b3)
        }

        ////////////////////////////////////////////////////////////////////

        override val buffer: ByteArray
            get() = input.toArray()

        override fun position(): Int {
            return position
        }

        override fun length(): Int {
            return size
        }

        @Throws(IOException::class)
        override fun available(): Int {
            return size - position
        }

        @Throws(IOException::class)
        override fun seek(pos: Int) {
            if (pos < 0) {
                throw IOException("Invalid pos=$pos")
            }
            position = pos
        }

        override fun close() {
            position = size
        }
    }
}
