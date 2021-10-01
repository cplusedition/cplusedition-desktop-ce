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

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Write binary data in little endian order to a random access file.
 */
abstract class AbstractFileBinaryWriter : AbstractBinaryWriter {

    var outStream: RandomAccessFile

    protected constructor(fname: String, mode: String, charset: Charset? = null) : super(charset) {
        outStream = RandomAccessFile(fname, mode)
    }

    protected constructor(file: RandomAccessFile, charset: Charset?) : super(charset) {
        outStream = file
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
        outStream.write(value, offset, length)
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun write16BE(value: Int) {
        outStream.write(value shr 8 and 0xFF)
        outStream.write(value and 0xFF)
    }

    @Throws(IOException::class)
    override fun write32BE(value: Int) {
        outStream.write(value shr 24 and 0xFF)
        outStream.write(value shr 16 and 0xFF)
        outStream.write(value shr 8 and 0xFF)
        outStream.write(value and 0xFF)
    }

    @Throws(IOException::class)
    override fun write64BE(value: Long) {
        outStream.write((value shr 56 and 0xff).toInt())
        outStream.write((value shr 48 and 0xff).toInt())
        outStream.write((value shr 40 and 0xff).toInt())
        outStream.write((value shr 32 and 0xff).toInt())
        outStream.write((value shr 24 and 0xff).toInt())
        outStream.write((value shr 16 and 0xff).toInt())
        outStream.write((value shr 8 and 0xff).toInt())
        outStream.write((value and 0xff).toInt())
    }

    @Throws(IOException::class)
    override fun write16LE(value: Int) {
        outStream.write(value and 0xff)
        outStream.write(value shr 8 and 0xff)
    }

    @Throws(IOException::class)
    override fun write32LE(value: Int) {
        outStream.write(value and 0xff)
        outStream.write(value shr 8 and 0xff)
        outStream.write(value shr 16 and 0xff)
        outStream.write(value shr 24 and 0xff)
    }

    @Throws(IOException::class)
    override fun write64LE(value: Long) {
        outStream.write((value and 0xff).toInt())
        outStream.write((value shr 8 and 0xff).toInt())
        outStream.write((value shr 16 and 0xff).toInt())
        outStream.write((value shr 24 and 0xff).toInt())
        outStream.write((value shr 32 and 0xff).toInt())
        outStream.write((value shr 40 and 0xff).toInt())
        outStream.write((value shr 48 and 0xff).toInt())
        outStream.write((value shr 56 and 0xff).toInt())
    }

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    override fun length(): Long {
        return outStream.length()
    }

    @Throws(IOException::class)
    override fun position(): Long {
        return outStream.filePointer
    }

    @Throws(IOException::class)
    override fun seek(pos: Long) {
        outStream.seek(pos)
    }

    @Throws(IOException::class)
    override fun close() {
        outStream.close()
    }

    companion object {

        @Throws(FileNotFoundException::class)
        fun create(
                fname: String,
                mode: String,
                charset: Charset? = null,
                order: ByteOrder = ByteOrder.LITTLE_ENDIAN
        ): AbstractFileBinaryWriter {
            return if (order == ByteOrder.LITTLE_ENDIAN) {
                LEFileBinaryWriter(
                        fname,
                        mode,
                        charset
                )
            } else BEFileBinaryWriter(fname, mode, charset)
        }
    }
}
