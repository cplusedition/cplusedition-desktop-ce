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
package sf.andrians.cplusedition.support

import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.WithUtil.Companion.With
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

interface IInputStreamProvider {
    @Throws(Exception::class)
    fun getInputStream(): InputStream
}

class FileInputStreamProvider(
        private val file: File
) : IInputStreamProvider {
    override fun getInputStream(): InputStream {
        return file.inputStream()
    }
}

abstract class ISeekableInputStream : InputStream() {
    /**
     * @return  The number of bytes read, possibly zero, or {@code -1} if the
     * given position is greater than or equal to the file's current size.
     */
    @Throws(IOException::class)
    abstract fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int
    abstract fun getSize(): Long
    abstract fun getPosition(): Long
    abstract fun setPosition(pos: Long)

    fun readFullyAt(position: Long, buffer: ByteArray, off: Int, size: Int) {
        var offset = 0
        var remaining = size
        while (remaining > 0) {
            val n = readAt(position + offset, buffer, off + offset, remaining)
            if (n < 0) throw IOException()
            offset += n
            remaining -= n
        }
    }

}

interface ISeekableInputStreamProvider : IInputStreamProvider {
    fun getSeekableInputStream(): ISeekableInputStream
}

class SeekableFileInputStream(
        file: File
) : ISeekableInputStream() {
    private val length = file.length()
    private val seekable = FileChannel.open(file.toPath(), StandardOpenOption.READ)
    private val buf = ByteArray(1)

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return seekable.read(ByteBuffer.wrap(buffer, offset, size), position)
    }

    override fun getSize(): Long {
        return length
    }

    override fun getPosition(): Long {
        return seekable.position()
    }

    override fun setPosition(pos: Long) {
        seekable.position(pos)
    }

    override fun read(b: ByteArray): Int {
        return seekable.read(ByteBuffer.wrap(b))
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return seekable.read(ByteBuffer.wrap(b, off, len))
    }

    override fun read(): Int {
        while (true) {
            val count = seekable.read(ByteBuffer.wrap(buf))
            if (count == 0) continue
            if (count < 0) return -1
            return buf[0].toInt() and 0xff
        }
    }

    override fun close() {
        seekable.close()
        super.close()
    }
}

class ReadOnlySeekableInputStream(
        private val length: Long,
        private val provider: Fun01<InputStream>
) : ISeekableInputStream() {

    private val input = provider()
    private var seekable = provider()
    private var position = 0L

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position != this.position) {
            seekable.close()
            seekable = provider()
            seekable.skip(position)
        }
        val count = seekable.read(buffer, offset, size)
        if (count >= 0) {
            this.position += count
        } else {
            this.position = length
        }
        return count
    }

    override fun getSize(): Long {
        return length
    }

    override fun getPosition(): Long {
        return position
    }

    override fun setPosition(pos: Long) {
        this.position = pos
    }

    override fun read(b: ByteArray): Int {
        val ret = input.read(b)
        if (ret > 0) this.position += ret
        return ret
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val ret = input.read(b, off, len)
        if (ret > 0) this.position += ret
        return ret
    }

    override fun read(): Int {
        val ret = input.read()
        if (ret >= 0) this.position += 1
        return ret
    }

    override fun close() {
        With.exceptionOrNull { input.close() }
        With.exceptionOrNull { seekable.close() }
        super.close()
    }
}

object NotExistsSeekableInputStreamProvider : ISeekableInputStreamProvider {
    override fun getSeekableInputStream(): ISeekableInputStream {
        throw IOException()
    }

    override fun getInputStream(): InputStream {
        throw IOException()
    }
}

class SeekableByteArrayInputStream constructor(
        private val input: ByteArray,
        private val start: Int = 0,
        private val len: Int = input.size
) : ISeekableInputStream() {

    val end = start + len
    var offset = start

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= len) return -1
        val count = if (position + size > len) (len - position).toInt() else size
        val startoffset = (start + position).toInt()
        System.arraycopy(input, startoffset, buffer, offset, count)
        return count
    }

    override fun getSize(): Long {
        return len.toLong()
    }

    override fun getPosition(): Long {
        return (offset - start).toLong()

    }

    override fun setPosition(pos: Long) {
        if (pos > len) throw IOException()
        this.offset = start + pos.toInt()
    }

    @Throws(IOException::class)
    override fun close() {
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (offset >= end) {
            if (offset == end) {
                ++offset
                return -1
            }
            throw IOException("Reading pass end of file")
        }
        return input[offset++].toInt() and 0xff
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        var len1 = len
        if (offset >= end) {
            if (offset == end) {
                ++offset
                return -1
            }
            throw IOException("Reading pass end of file")
        }
        val e = offset + len1
        if (e > end) {
            len1 = end - offset
        }
        System.arraycopy(input, offset, b, off, len1)
        offset += len1
        return len1
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    @Throws(IOException::class)
    fun seek(pos: Long) {
        var pos1 = pos
        pos1 += start.toLong()
        if (pos1 > input.size) {
            throw IOException("Position out of bound: ")
        }
        offset = start + pos1.toInt()
    }
}

