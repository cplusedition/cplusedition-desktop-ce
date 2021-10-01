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
import java.io.OutputStream
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.*

abstract class AbstractBinaryWriter(charset: Charset? = null) : OutputStream(), IBinaryWriter {

    private val charSet = charset ?: TextUtil.UTF8()

    @Throws(IOException::class)
    override fun write(value: ByteArray) {
        write(value, 0, value.size)
    }

    @Throws(IOException::class)
    override fun writeChar(value: Char) {
        val cb = CharBuffer.wrap(charArrayOf(value))
        val bb = charSet.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(
                CodingErrorAction.REPORT).encode(cb)
        write(bb.array(), 0, bb.limit())
    }

    @Throws(IOException::class)
    override fun writeChars(value: CharArray, offset: Int, length: Int) {
        val cb = CharBuffer.wrap(value, offset, length)
        val bb = charSet.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(
                CodingErrorAction.REPORT).encode(cb)
        write(bb.array(), 0, bb.limit())
    }

    @Throws(IOException::class)
    override fun writeString(value: String) {
        val a = value.toCharArray()
        writeChars(a, 0, a.size)
    }

    @Throws(IOException::class)
    override fun writeFloat(value: Float) {
        val value = java.lang.Float.floatToIntBits(value)
        write32(value)
    }

    @Throws(IOException::class)
    override fun writeDouble(value: Double) {
        val value = java.lang.Double.doubleToLongBits(value)
        write64(value)
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    override fun pad(start: Long, alignment: Int, value: Byte) {
        val pos = position()
        var p = pos - start
        p += (alignment - 1).toLong()
        p = p and (alignment - 1).inv().toLong()
        p += start
        if (p > pos) {
            val padding = ByteArray((p - pos).toInt())
            Arrays.fill(padding, 0, padding.size, value)
            write(padding)
        }
    }

    @Throws(IOException::class)
    fun align(start: Long, alignment: Int) {
        var pos = position() - start
        if (pos % alignment != 0L) {
            pos = (pos + alignment - 1 and (alignment - 1).inv().toLong()) + start
            if (pos > Int.MAX_VALUE) {
                throw IOException("Seeking pass end of file: pos=$pos")
            }
            seek(pos)
        }
    }

    ////////////////////////////////////////////////////////////////////

}