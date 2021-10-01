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
 * Write binary data a byte array.
 */
abstract class AbstractByteArrayBinaryWriter : AbstractBinaryWriter, IByteArrayBinaryWriter {

    private var buffer_: ByteArray
    override val buffer: ByteArray get() = buffer_
    protected var size: Int
    protected var position: Int

    protected constructor(charset: Charset? = null) : super(charset) {
        buffer_ = ByteArray(DEF_SIZE)
        size = 0
        position = 0
    }

    protected constructor(a: ByteArray, charset: Charset = TextUtil.UTF8()) : super(charset) {
        buffer_ = a
        size = 0
        position = 0
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun writeBoolean(v: Boolean) {
        write8(if (v) 1 else 0)
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        write8(b)
    }

    @Throws(IOException::class)
    override fun write(value: ByteArray, offset: Int, length: Int) {
        ensureCapacity(length)
        for (i in 0 until length) {
            buffer_[position++] = value[offset + i]
        }
    }

    @Throws(IOException::class)
    override fun write16BE(value: Int) {
        ensureCapacity(2)
        buffer_[position++] = (value shr 8 and 0xff).toByte()
        buffer_[position++] = (value and 0xff).toByte()
    }

    @Throws(IOException::class)
    override fun write32BE(value: Int) {
        ensureCapacity(4)
        buffer_[position++] = (value shr 24 and 0xff).toByte()
        buffer_[position++] = (value shr 16 and 0xff).toByte()
        buffer_[position++] = (value shr 8 and 0xff).toByte()
        buffer_[position++] = (value and 0xff).toByte()
    }

    @Throws(IOException::class)
    override fun write64BE(value: Long) {
        ensureCapacity(8)
        buffer_[position++] = (value shr 56 and 0xff).toByte()
        buffer_[position++] = (value shr 48 and 0xff).toByte()
        buffer_[position++] = (value shr 40 and 0xff).toByte()
        buffer_[position++] = (value shr 32 and 0xff).toByte()
        buffer_[position++] = (value shr 24 and 0xff).toByte()
        buffer_[position++] = (value shr 16 and 0xff).toByte()
        buffer_[position++] = (value shr 8 and 0xff).toByte()
        buffer_[position++] = (value and 0xff).toByte()
    }

    @Throws(IOException::class)
    override fun write16LE(value: Int) {
        ensureCapacity(2)
        buffer_[position++] = (value and 0xff).toByte()
        buffer_[position++] = (value shr 8 and 0xff).toByte()
    }

    @Throws(IOException::class)
    override fun write32LE(value: Int) {
        ensureCapacity(4)
        buffer_[position++] = (value and 0xff).toByte()
        buffer_[position++] = (value shr 8 and 0xff).toByte()
        buffer_[position++] = (value shr 16 and 0xff).toByte()
        buffer_[position++] = (value shr 24 and 0xff).toByte()
    }

    @Throws(IOException::class)
    override fun write64LE(value: Long) {
        ensureCapacity(8)
        buffer_[position++] = (value and 0xff).toByte()
        buffer_[position++] = (value shr 8 and 0xff).toByte()
        buffer_[position++] = (value shr 16 and 0xff).toByte()
        buffer_[position++] = (value shr 24 and 0xff).toByte()
        buffer_[position++] = (value shr 32 and 0xff).toByte()
        buffer_[position++] = (value shr 40 and 0xff).toByte()
        buffer_[position++] = (value shr 48 and 0xff).toByte()
        buffer_[position++] = (value shr 56 and 0xff).toByte()
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    override fun position(): Long {
        return position.toLong()
    }

    override fun length(): Long {
        return size.toLong()
    }

    @Throws(IOException::class)
    override fun seek(pos: Long) {
        if (pos > Int.MAX_VALUE) {
            throw IOException("Offset too large, limit=" + Int.MAX_VALUE + ", pos=" + pos)
        }
        position = pos.toInt()
    }

    @Throws(IOException::class)
    override fun close() {
        buffer_ = ByteArray(0)
        position = 0
    }

    override fun toByteArray(): ByteArray {
        val ret = ByteArray(size)
        System.arraycopy(buffer_, 0, ret, 0, size)
        return ret
    }

    protected fun ensureCapacity(count: Int) {
        val end = position + count
        if (end > buffer_.size) {
            val a = ByteArray(buffer_.size + Math.max(count, buffer_.size))
            System.arraycopy(buffer_, 0, a, 0, buffer_.size)
            buffer_ = a
        }
        if (end > size) {
            size = end
        }
    }

    ////////////////////////////////////////////////////////////////////

    companion object {

        private const val DEF_SIZE = 256
        fun wrap(a: ByteArray): IByteWriter {
            return BEByteArrayBinaryWriter(a)
        }

        fun createUTF8LE(): AbstractByteArrayBinaryWriter {
            return create(TextUtil.UTF8(), ByteOrder.LITTLE_ENDIAN)
        }

        fun createUTF8BE(): AbstractByteArrayBinaryWriter {
            return create(TextUtil.UTF8(), ByteOrder.BIG_ENDIAN)
        }

        fun create(charset: Charset, order: ByteOrder): AbstractByteArrayBinaryWriter {
            return if (order == ByteOrder.LITTLE_ENDIAN) {
                LEByteArrayBinaryWriter(charset)
            } else BEByteArrayBinaryWriter(charset)
        }
    }
}
