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

import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

abstract class SeekableInputStream : InputStream() {

    @Throws(IOException::class)
    abstract fun length(): Long

    @Throws(IOException::class)
    abstract fun position(): Long

    @Throws(IOException::class)
    abstract fun seek(pos: Long)

    class RandomAccessFileWrapper(val file: RandomAccessFile) : SeekableInputStream() {
        @Throws(IOException::class)
        override fun close() {
            file.close()
        }

        @Throws(IOException::class)
        override fun position(): Long {
            return file.filePointer
        }

        @Throws(IOException::class)
        override fun length(): Long {
            return file.length()
        }

        @Throws(IOException::class)
        override fun read(): Int {
            return file.read()
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            return file.read(b, off, len)
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            return file.read(b)
        }

        @Throws(IOException::class)
        fun readUTF(): String {
            return file.readUTF()
        }

        @Throws(IOException::class)
        override fun seek(pos: Long) {
            file.seek(pos)
        }

    }

    class ByteArrayWrapper constructor(
            private val input: ByteArray,
            private val start: Int = 0,
            len: Int = input.size
    ) : SeekableInputStream() {

        val end = start + len
        var offset = start

        @Throws(IOException::class)
        override fun close() {
        }

        @Throws(IOException::class)
        override fun position(): Long {
            return (offset - start).toLong()
        }

        @Throws(IOException::class)
        override fun length(): Long {
            return (end - start).toLong()
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
            return input[offset++].toInt()
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
        override fun seek(pos: Long) {
            var pos1 = pos
            pos1 += start.toLong()
            if (pos1 > input.size) {
                throw IOException("Position out of bound: ")
            }
            offset = start + pos1.toInt()
        }
    }

    companion object {
        fun wrap(file: RandomAccessFile): SeekableInputStream {
            return RandomAccessFileWrapper(file)
        }

        fun wrap(a: ByteArray): SeekableInputStream {
            return ByteArrayWrapper(a)
        }
    }
}