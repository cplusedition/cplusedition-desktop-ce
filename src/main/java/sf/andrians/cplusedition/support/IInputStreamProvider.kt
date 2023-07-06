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

import com.cplusedition.bot.core.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import kotlin.math.min

interface IInputStream : AutoCloseable {
    @Throws(IOException::class)
    fun read(): Int
    @Throws(IOException::class)
    fun read(a: ByteArray): Int
    @Throws(IOException::class)
    fun read(a: ByteArray, off: Int, len: Int): Int
}

interface IOutputStream : AutoCloseable {
    @Throws(IOException::class)
    fun write(b: Int)
    @Throws(IOException::class)
    fun write(a: ByteArray)
    @Throws(IOException::class)
    fun write(a: ByteArray, off: Int, len: Int)
}

interface ISeekableInputStream {
    /**
     * Note that position for sequential input using read() are not affect by readAt().
     * @return  The number of bytes read, possibly zero, or {@code -1} if the
     * given position is greater than or equal to the file's current size.
     */
    @Throws(IOException::class)
    fun readAt(pos: Long, b: ByteArray, off: Int = 0, size: Int = b.size): Int
    fun skip(n: Long): Long
    fun getPosition(): Long
    fun setPosition(pos: Long)
    fun getSize(): Long
}

abstract class MySeekableInputStream : InputStream(), IInputStream, ISeekableInputStream {
    fun readFullyAt(pos: Long, b: ByteArray, off: Int, size: Int) {
        return readFullyAt(this, pos, b, off, size)
    }

    fun readWhilePossibleAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        return readWhilePossibleAt(this, pos, b, off, size)
    }

    fun copy(pos: Long, size: Int, callback: Fun30<ByteArray, Int, Int>) {
        copy(this, pos, size, callback)
    }

    companion object {
        fun readFullyAt(input: ISeekableInputStream, pos: Long, b: ByteArray, off: Int, size: Int) {
            var count = 0
            var remaining = size
            while (remaining > 0) {
                val n = input.readAt(pos + count, b, off + count, remaining)
                if (n < 0) throw IOException()
                count += n
                remaining -= n
            }
        }

        fun readWhilePossibleAt(input: ISeekableInputStream, pos: Long, b: ByteArray, off: Int, size: Int): Int {
            if (size == 0) return 0
            var count = 0
            var remaining = size
            while (remaining > 0) {
                val n = input.readAt(pos + count, b, off + count, remaining)
                if (n < 0) break
                count += n
                remaining -= n
            }
            return if (count > 0) count else -1
        }

        fun copy(input: ISeekableInputStream, pos: Long, size: Int, callback: Fun30<ByteArray, Int, Int>) {
            val b = ByteArray(IStorage.K.BUFSIZE)
            var offset = 0
            var remaining = size
            while (remaining > 0) {
                val len = min(remaining, b.size)
                val n = input.readAt(pos + offset, b, 0, len)
                if (n < 0) throw IOException()
                callback(b, 0, n)
                offset += n
                remaining -= n
            }
        }
    }
}

interface ISeekableOutputStream : AutoCloseable {
    /// Note: This does NOT update the file position for sequential writes.
    @Throws(IOException::class)
    fun writeAt(pos: Long, b: ByteArray, off: Int = 0, len: Int = b.size)
    @Throws(IOException::class)
    fun writeAt(pos: Long, b: ByteBuffer)
}

abstract class AbstractSeekableOutputStream : OutputStream(), ISeekableOutputStream

interface ISeekableOutputStreamProvider : IOutputStreamProvider {
    /// @param truncate True to truncate existing file to 0 length if file already exists,
    /// otherwise keep existing file intact on open.
    @Throws(IOException::class)
    fun seekableOutputStream(truncate: Boolean): ISeekableOutputStream?
}

class SeekableFileOutputStream(file: File, truncate: Boolean) : AbstractSeekableOutputStream() {
    private val ch = FileChannel.open(
        file.toPath(),
        StandardOpenOption.WRITE,
        if (truncate) StandardOpenOption.TRUNCATE_EXISTING else StandardOpenOption.CREATE
    )
    private val bbuf = ByteArray(1)

    override fun writeAt(pos: Long, b: ByteArray, off: Int, len: Int) {
        ch.write(ByteBuffer.wrap(b, off, len), pos)
    }

    override fun writeAt(pos: Long, b: ByteBuffer) {
        ch.write(b, pos)
    }

    override fun write(b: Int) {
        val pos = ch.position()
        bbuf[0] = b.toByte()
        ch.write(ByteBuffer.wrap(bbuf))
        ch.position(pos + 1)
    }

    override fun write(b: ByteArray) {
        this.write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        ch.write(ByteBuffer.wrap(b, off, len))
    }

    override fun close() {
        ch.close()
    }
}

interface ISeekableInputStreamProvider : IInputStreamProvider {
    fun seekableInputStream(): MySeekableInputStream?
}

class ExistingSeekableInputStreamProvider(private val seekable: MySeekableInputStream) : ISeekableInputStreamProvider {
    override fun seekableInputStream(): MySeekableInputStream {
        return seekable
    }

    override fun inputStream(): InputStream {
        return seekable
    }
}

class FileSeekableInputStreamProvider(private val file: File) : ISeekableInputStreamProvider {
    override fun seekableInputStream(): MySeekableInputStream {
        return SeekableFileInputStream(file)
    }

    override fun inputStream(): InputStream {
        return FileInputStream(file)
    }
}

class SeekableFileInputStream(file: File) : MySeekableInputStream() {
    private val length = file.length()
    private val seekable = FileChannel.open(file.toPath(), StandardOpenOption.READ)
    private val buf = ByteArray(1)

    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        return seekable.read(ByteBuffer.wrap(b, off, size), pos)
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

    override fun skip(n: Long): Long {
        val pos = getPosition()
        val len = min(n, getSize() - pos)
        setPosition(pos + len)
        return len
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

class ShadowSeekableInputStream(
    private val seekable: ISeekableInputStream,
    private val lock: Any,
) : MySeekableInputStream() {
    private val length = seekable.getSize()
    private val buf = ByteArray(1)
    private var _position = 0L

    override fun getSize(): Long {
        return length
    }

    override fun getPosition(): Long {
        return _position
    }

    override fun setPosition(pos: Long) {
        this._position = pos
    }

    override fun skip(n: Long): Long {
        val len = min(n, length - _position)
        if (len > 0) _position += len
        return len
    }

    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        synchronized(lock) {
            return seekable.readAt(pos, b, off, size)
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        synchronized(lock) {
            val ret = seekable.readAt(_position, b, off, len)
            if (ret > 0) _position += ret
            return ret
        }
    }

    override fun read(b: ByteArray): Int {
        return this.read(b, 0, b.size)
    }

    override fun read(): Int {
        while (true) {
            val count = this.read(buf, 0, 1)
            if (count == 0) continue
            if (count < 0) return -1
            return buf[0].toInt() and 0xff
        }
    }

    override fun close() {
    }
}

class ReadOnlySeekableInputStream constructor(
    private val lengthProvider: Fun01<Long>,
    private val inputStreamProvider: Fun01<InputStream>
) : MySeekableInputStream() {

    private val input = inputStreamProvider()
    private var seekable = inputStreamProvider()
    private var position = 0L

    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        if (pos != this.position) {
            seekable.close()
            seekable = inputStreamProvider()
            seekable.skip(pos)
        }
        val count = seekable.read(b, off, size)
        if (count >= 0) {
            this.position += count
        } else {
            this.position = lengthProvider()
        }
        return count
    }

    override fun getSize(): Long {
        return lengthProvider()
    }

    override fun getPosition(): Long {
        return position
    }

    override fun setPosition(pos: Long) {
        this.position = pos
    }

    override fun skip(n: Long): Long {
        val pos = getPosition()
        val len = min(n, getSize() - pos)
        setPosition(pos + len)
        return len
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
    override fun seekableInputStream(): MySeekableInputStream {
        throw IOException()
    }

    override fun inputStream(): InputStream {
        throw IOException()
    }
}

class SeekableByteArrayInputStream constructor(
    private val input: ByteArray,
    private val start: Int = 0,
    private val len: Int = input.size
) : MySeekableInputStream() {

    private val end = start + len
    private var index = start

    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        if (pos >= len) return -1
        val count = if (pos + size > len) (len - pos).toInt() else size
        val startoffset = (start + pos).toInt()
        System.arraycopy(input, startoffset, b, off, count)
        return count
    }

    override fun getSize(): Long {
        return len.toLong()
    }

    override fun getPosition(): Long {
        return (index - start).toLong()
    }

    override fun setPosition(pos: Long) {
        if (pos > len) throw IOException()
        this.index = start + pos.toInt()
    }

    override fun skip(n: Long): Long {
        val pos = getPosition()
        val len = min(n, getSize() - pos)
        setPosition(pos + len)
        return len
    }

    @Throws(IOException::class)
    override fun close() {
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (index >= end) {
            if (index == end) {
                ++index
                return -1
            }
            throw IOException("Reading pass end of file")
        }
        return input[index++].toInt() and 0xff
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        var len1 = len
        if (index >= end) {
            if (index == end) {
                ++index
                return -1
            }
            throw IOException("Reading pass end of file")
        }
        val e = index + len1
        if (e > end) {
            len1 = end - index
        }
        System.arraycopy(input, index, b, off, len1)
        index += len1
        return len1
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    @Throws(IOException::class)
    fun seek(pos: Long) {
        val index1 = start.toLong() + pos
        if (index1 > input.size) {
            throw IOException("Position out of bound: ")
        }
        index = index1.toInt()
    }
}

class SeekableByteArrayOutputStream constructor(
    private val input: ByteArray,
    private val start: Int = 0,
    private val len: Int = input.size
) : ISeekableOutputStream {

    private val end = (start + len).toLong()

    override fun writeAt(pos: Long, b: ByteArray, off: Int, len: Int) {
        writeAt(pos, ByteBuffer.wrap(b, off, len))
    }

    override fun writeAt(pos: Long, b: ByteBuffer) {
        if (pos >= Int.MAX_VALUE.toLong() || pos + b.remaining() > end)
            throw IOException()
        b.get(input, pos.toInt(), b.remaining())
    }

    @Throws(IOException::class)
    override fun close() {
    }
}

class StayOpenSeekableInputStream(private val seekable: MySeekableInputStream) : MySeekableInputStream() {
    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        return seekable.readAt(pos, b, off, size)
    }

    override fun getSize(): Long {
        return seekable.getSize()
    }

    override fun getPosition(): Long {
        return seekable.getPosition()
    }

    override fun setPosition(pos: Long) {
        seekable.setPosition(pos)
    }

    override fun skip(n: Long): Long {
        return seekable.skip(n)
    }

    override fun read(): Int {
        return seekable.read()
    }

    override fun close() {
    }
}

class PositionTrackingInputStreamAdapter(
    private val input: InputStream
) : IInputStream, IPositionTracking {
    private var closed = false
    private var position = 0L
    override fun close() {
        if (!closed) {
            closed = true
            input.close()
        }
    }

    override fun read(): Int {
        val pos = position
        val ret = input.read()
        position = pos + 1
        return ret
    }

    override fun read(a: ByteArray): Int {
        return this.read(a, 0, a.size)
    }

    override fun read(a: ByteArray, off: Int, len: Int): Int {
        val pos = position
        val ret = input.read(a, off, len)
        if (ret > 0) {
            position = pos + ret
        }
        return ret
    }

    override fun getPosition(): Long {
        return position
    }
}

class PositionTrackingRandomFileAdapter(
    private val file: RandomAccessFile,
) : IInputStream, IPositionTracking {
    private var closed = false
    private var position = 0L
    override fun close() {
        if (!closed) {
            closed = true
            file.close()
        }
    }

    override fun read(): Int {
        val pos = position
        val ret = file.readUnsignedByte()
        position = pos + 1
        return ret
    }

    override fun read(a: ByteArray): Int {
        return this.read(a, 0, a.size)
    }

    override fun read(a: ByteArray, off: Int, len: Int): Int {
        val pos = position
        val ret = file.read(a, off, len)
        if (ret > 0) {
            position = pos + ret
        }
        return ret
    }

    override fun getPosition(): Long {
        return position
    }
}

object InputUt {
    @Throws(EOFException::class)
    fun readU8(input: IInputStream): Int {
        val ret = input.read()
        if (ret < 0) throw EOFException()
        return ret
    }

    @Throws(IOException::class)
    fun readFully(input: IInputStream, a: ByteArray, offset: Int = 0, length: Int = a.size): ByteArray {
        var off = offset
        var len = length
        while (len > 0) {
            val n = input.read(a, off, len)
            if (n < 0) {
                throw EOFException("Expected=$length, actual=${off - offset}")
            }
            len -= n
            off += n
        }
        return a
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached.
     * @return number of bytes read, possibly 0 if EOF is reached, but never -1.
     */
    @Throws(IOException::class)
    fun readWhilePossible(input: IInputStream, a: ByteArray, offset: Int = 0, length: Int = a.size): Int {
        var off = offset
        var remaining = length
        while (remaining > 0) {
            val n = input.read(a, off, remaining)
            if (n < 0)
                break
            remaining -= n
            off += n
        }
        return length - remaining
    }
}
