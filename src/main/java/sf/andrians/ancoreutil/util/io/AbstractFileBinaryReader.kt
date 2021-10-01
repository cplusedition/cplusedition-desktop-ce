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
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Read binary data in little endian order from random access file.
 */
abstract class AbstractFileBinaryReader protected constructor(
        filepath: String,
        charset: Charset = TextUtil.UTF8()
) : AbstractBinaryReader(charset) {

    var inStream: RandomAccessFile = RandomAccessFile(filepath, "r")

    @Throws(IOException::class)
    override fun readI16BE(): Short {
        val buf = ByteArray(2)
        readFully(buf)
        return (buf[0].toInt() and 0xff shl 8 or (buf[1].toInt() and 0xff)).toShort()
    }

    @Throws(IOException::class)
    override fun readI32BE(): Int {
        val buf = ByteArray(4)
        readFully(buf)
        return buf[0].toInt() and 0xff shl 8 or (buf[1].toInt() and 0xff) shl 8 or (buf[2].toInt() and 0xff) shl 8 or (buf[3].toInt() and 0xff)
    }

    @Throws(IOException::class)
    override fun readI64BE(): Long {
        val high = readI32().toLong()
        val low = (readI32().toLong() and 0xffffL)
        return high shl 32 or low
    }

    @Throws(IOException::class)
    override fun readU16BE(): Int {
        val buf = ByteArray(2)
        readFully(buf)
        return buf[0].toInt() and 0xff shl 8 or (buf[1].toInt() and 0xff) and 0xffff
    }

    @Throws(IOException::class)
    override fun readI16LE(): Short {
        val buf = ByteArray(2)
        readFully(buf)
        return (buf[1].toInt() and 0xff shl 8 or (buf[0].toInt() and 0xff)).toShort()
    }

    @Throws(IOException::class)
    override fun readI32LE(): Int {
        val buf = ByteArray(4)
        readFully(buf)
        return buf[3].toInt() and 0xff shl 8 or (buf[2].toInt() and 0xff) shl 8 or (buf[1].toInt() and 0xff) shl 8 or (buf[0].toInt() and 0xff)
    }

    @Throws(IOException::class)
    override fun readI64LE(): Long {
        val low = (readI32().toLong() and 0xffffL)
        val high = readI32().toLong()
        return high shl 32 or low
    }

    @Throws(IOException::class)
    override fun readU16LE(): Int {
        val buf = ByteArray(2)
        readFully(buf)
        return buf[1].toInt() and 0xff shl 8 or (buf[0].toInt() and 0xff) and 0xffff
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun readI8(): Byte {
        val b = inStream.read()
        if (b == -1) {
            throw IOException(ERROR_EOF)
        }
        return b.toByte()
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        inStream.readFully(b)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray, offset: Int, len: Int) {
        inStream.readFully(b, offset, len)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return inStream.read(b, 0, b.size)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, offset: Int, len: Int): Int {
        return inStream.read(b, offset, len)
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun close() {
        inStream.close()
    }

    @Throws(IOException::class)
    override fun position(): Long {
        return inStream.filePointer
    }

    @Throws(IOException::class)
    override fun seek(pos: Long) {
        inStream.seek(pos)
    }

    @Throws(IOException::class)
    override fun length(): Long {
        return inStream.length()
    }

    @Throws(IOException::class)
    override fun remaining(): Long {
        return inStream.length() - inStream.filePointer
    }

    @Throws(IOException::class)
    override fun hasRemaining(): Boolean {
        return remaining() > 0
    }

    @Throws(IOException::class)
    override fun align(start: Long, alignment: Int) {
        var pos = inStream.filePointer - start
        if (pos % alignment != 0L) {
            pos = (pos + alignment - 1 and (alignment - 1).inv().toLong()) + start
            seek(pos)
        }
    } ////////////////////////////////////////////////////////////////////

    companion object {

        @Throws(IOException::class)
        fun create(
                filepath: String,
                charset: Charset = TextUtil.UTF8(),
                order: ByteOrder = ByteOrder.LITTLE_ENDIAN
        ): AbstractFileBinaryReader {
            return if (order == ByteOrder.LITTLE_ENDIAN) {
                LEFileBinaryReader(filepath, charset)
            } else BEFileBinaryReader(filepath, charset)
        }
    }

    ////////////////////////////////////////////////////////////////////
}
