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

import sf.andrians.ancoreutil.util.text.TextUtil
import java.io.IOException
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Read binary data from a byte array.
 */
abstract class AbstractByteArrayBinaryReader protected constructor(
        var buffer: ByteArray,
        charset: Charset = TextUtil.UTF8()
) : AbstractBinaryReader(charset) {

    protected var position = 0

    operator fun get(index: Int): Byte {
        return buffer[index]
    }

    @Throws(IOException::class)
    override fun readI16BE(): Short {
        if (position + 2 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        return (b0 and 0xff shl 8 or (b1 and 0xff)).toShort()
    }

    @Throws(IOException::class)
    override fun readI32BE(): Int {
        if (position + 4 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        val b2 = buffer[position++].toInt()
        val b3 = buffer[position++].toInt()
        return b0 and 0xff shl 24 or (b1 and 0xff shl 16) or (b2 and 0xff shl 8) or (b3 and 0xff)
    }

    @Throws(IOException::class)
    override fun readI64BE(): Long {
        val high = readI32BE().toLong()
        val low = (readI32BE().toLong() and 0xffffL)
        return high shl 32 or low
    }

    @Throws(IOException::class)
    override fun readU16BE(): Int {
        if (position + 2 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        return b0 and 0xff shl 8 or (b1 and 0xff) and 0xffff
    }

    @Throws(IOException::class)
    override fun readI16LE(): Short {
        if (position + 2 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        return (b1 and 0xff shl 8 or (b0 and 0xff)).toShort()
    }

    @Throws(IOException::class)
    override fun readI32LE(): Int {
        if (position + 4 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        val b2 = buffer[position++].toInt()
        val b3 = buffer[position++].toInt()
        return b3 and 0xff shl 24 or (b2 and 0xff shl 16) or (b1 and 0xff shl 8) or (b0 and 0xff)
    }

    @Throws(IOException::class)
    override fun readI64LE(): Long {
        val low = (readI32LE().toLong() and 0xffffL)
        val high = readI32LE().toLong()
        return high shl 32 or low
    }

    @Throws(IOException::class)
    override fun readU16LE(): Int {
        val b0 = buffer[position++].toInt()
        val b1 = buffer[position++].toInt()
        return b1 and 0xff shl 8 or (b0 and 0xff) and 0xffff
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun readBoolean(): Boolean {
        return readI8().toInt() != 0
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        val len = b.size
        var count = 0
        do {
            val n = read(b, 0, len - count)
            if (n < 0) {
                throw IOException(ERROR_EOF)
            }
            count += n
        } while (count < len)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray, offset: Int, len: Int) {
        var count = 0
        do {
            val n = read(b, offset, len - count)
            if (n < 0) {
                throw IOException(ERROR_EOF)
            }
            count += n
        } while (count < len)
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    @Throws(IOException::class)
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

    @Throws(IOException::class)
    override fun length(): Long {
        return buffer.size.toLong()
    }

    @Throws(IOException::class)
    override fun remaining(): Long {
        return (buffer.size - position).toLong()
    }

    @Throws(IOException::class)
    override fun hasRemaining(): Boolean {
        return buffer.size > position
    }

    @Throws(IOException::class)
    override fun position(): Long {
        return position.toLong()
    }

    @Throws(IOException::class)
    override fun seek(pos: Long) {
        if (pos > Int.MAX_VALUE) {
            throw IOException("Offset too large, limit=" + Int.MAX_VALUE + ", pos=" + pos)
        }
        position = pos.toInt()
    }

    @Throws(IOException::class)
    override fun align(start: Long, alignment: Int) {
        var pos = position - start
        if (pos % alignment != 0L) {
            pos = (pos + alignment - 1 and (alignment - 1).inv().toLong()) + start
            if (pos > Int.MAX_VALUE) {
                throw IOException("Seeking pass end of file: pos=$pos")
            }
            position = pos.toInt()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        buffer = ByteArray(0)
        position = 0
    }

    ////////////////////////////////////////////////////////////////////

    companion object {
        fun wrap(a: ByteArray): IByteReader {
            return BEByteArrayBinaryReader(a)
        }

        fun wrapUTF8LE(a: ByteArray): AbstractByteArrayBinaryReader {
            return wrap(a, TextUtil.UTF8(), ByteOrder.LITTLE_ENDIAN)
        }

        fun wrapUTF8BE(a: ByteArray): AbstractByteArrayBinaryReader {
            return wrap(a, TextUtil.UTF8(), ByteOrder.BIG_ENDIAN)
        }

        fun wrap(a: ByteArray, charset: Charset, order: ByteOrder): AbstractByteArrayBinaryReader {
            return if (order == ByteOrder.LITTLE_ENDIAN) {
                LEByteArrayBinaryReader(a, charset)
            } else BEByteArrayBinaryReader(a, charset)
        }
    }

    ////////////////////////////////////////////////////////////////////
}