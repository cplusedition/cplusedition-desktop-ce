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
import java.io.OutputStream
import java.nio.ByteOrder

class ByteOutputStream constructor(capacity: Int = DEF_SIZE) : OutputStream(), IByteOutputStream {

    override var buffer = ByteArray(capacity)
        private set
    private var size = 0
    private var position = 0
    private var closed = false

    override fun write(value: Int) {
        ensureCapacity(1)
        buffer[position++] = value.toByte()
    }

    override fun write(b: ByteArray, offset: Int, len: Int) {
        ensureCapacity(len)
        for (i in 0 until len) {
            buffer[position++] = b[offset + i]
        }
    }

    override fun close() {
        closed = true
    }

    ////////////////////////////////////////////////////////////////////
    override fun write(s: IByteSequence?) {
        if (s == null) {
            return
        }
        val size = s.size()
        ensureCapacity(size)
        s.getBytes(0, size, buffer, position)
        position += size
    }

    override fun write(v: Boolean) {
        write8(if (v) 1 else 0)
    }

    override fun write8(value: Int) {
        ensureCapacity(1)
        buffer[position++] = value.toByte()
    }

    override fun write16BE(value: Int) {
        ensureCapacity(2)
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    override fun write32BE(value: Int) {
        ensureCapacity(4)
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    override fun write64BE(value: Long) {
        ensureCapacity(8)
        buffer[position++] = (value shr 56).toByte()
        buffer[position++] = (value shr 48).toByte()
        buffer[position++] = (value shr 40).toByte()
        buffer[position++] = (value shr 32).toByte()
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = value.toByte()
    }

    override fun write16LE(value: Int) {
        ensureCapacity(2)
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 8).toByte()
    }

    override fun write32LE(value: Int) {
        ensureCapacity(4)
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 24).toByte()
    }

    override fun write64LE(value: Long) {
        ensureCapacity(8)
        buffer[position++] = value.toByte()
        buffer[position++] = (value shr 8).toByte()
        buffer[position++] = (value shr 16).toByte()
        buffer[position++] = (value shr 24).toByte()
        buffer[position++] = (value shr 32).toByte()
        buffer[position++] = (value shr 40).toByte()
        buffer[position++] = (value shr 48).toByte()
        buffer[position++] = (value shr 56).toByte()
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun write16(value: Int) {
        if (BIG_ENDIAN) {
            write16BE(value)
        } else {
            write16LE(value)
        }
    }

    @Throws(IOException::class)
    override fun write32(value: Int) {
        if (BIG_ENDIAN) {
            write32BE(value)
        } else {
            write32LE(value)
        }
    }

    @Throws(IOException::class)
    override fun write64(value: Long) {
        if (BIG_ENDIAN) {
            write64BE(value)
        } else {
            write64LE(value)
        }
    }

    ////////////////////////////////////////////////////////////////////
    override fun position(): Int {
        return position
    }

    override fun length(): Int {
        return size
    }

    override fun toByteArray(): ByteArray {
        val ret = ByteArray(size)
        System.arraycopy(buffer, 0, ret, 0, size)
        return ret
    }

    override fun toByteArray(start: Int, end: Int): ByteArray {
        val len = end - start
        val ret = ByteArray(len)
        System.arraycopy(buffer, start, ret, 0, len)
        return ret
    }

    @Throws(IOException::class)
    override fun seek(pos: Int) {
        position = pos
    }

    ////////////////////////////////////////////////////////////////////
    protected fun ensureCapacity(count: Int) {
        if (count > 0 && closed) throw IOException()
        val end = position + count
        if (end > buffer.size) {
            val a = ByteArray(buffer.size + Math.max(count, buffer.size))
            System.arraycopy(buffer, 0, a, 0, buffer.size)
            buffer = a
        }
        if (end > size) {
            size = end
        }
    }

    companion object {
        private const val DEF_SIZE = 256
        private val BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
    }
}
