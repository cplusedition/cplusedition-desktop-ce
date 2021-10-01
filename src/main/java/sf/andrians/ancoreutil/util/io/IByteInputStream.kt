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

import java.io.Closeable
import java.io.Flushable
import java.io.IOException

interface IByteInputStream : Closeable, Flushable {
    @Throws(IOException::class)
    fun read(): Int

    @Throws(IOException::class)
    fun read(b: ByteArray?, offset: Int, len: Int): Int

    @Throws(IOException::class)
    fun readBool(): Boolean

    @Throws(IOException::class)
    fun read8(): Byte

    @Throws(IOException::class)
    fun readU8(): Int

    @Throws(IOException::class)
    fun read16BE(): Short

    @Throws(IOException::class)
    fun readU16BE(): Int

    @Throws(IOException::class)
    fun read32BE(): Int

    @Throws(IOException::class)
    fun readU32BE(): Long

    @Throws(IOException::class)
    fun read64BE(): Long

    @Throws(IOException::class)
    fun read16LE(): Short

    @Throws(IOException::class)
    fun readU16LE(): Int

    @Throws(IOException::class)
    fun read32LE(): Int

    @Throws(IOException::class)
    fun readU32LE(): Long

    @Throws(IOException::class)
    fun read64LE(): Long

    /**
     * Read 16 bit in native byte order.
     */
    @Throws(IOException::class)
    fun read16(): Short

    /**
     * Read unsigned 16 bit in native byte order.
     */
    @Throws(IOException::class)
    fun readU16(): Int

    /**
     * Read 32 bit in native byte order.
     */
    @Throws(IOException::class)
    fun read32(): Int

    /**
     * Read unsigned 32 bit in native byte order.
     */
    @Throws(IOException::class)
    fun readU32(): Long

    /**
     * Read 64 bit in native byte order.
     */
    @Throws(IOException::class)
    fun read64(): Long

    @Throws(IOException::class)
    fun readFully(b: ByteArray): ByteArray

    @Throws(IOException::class)
    fun readFully(b: ByteArray, offset: Int, len: Int): ByteArray

    @Throws(IOException::class)
    fun available(): Int

    @Throws(IOException::class)
    fun seek(pos: Int)

    @Throws(IOException::class)
    fun align(start: Int, alignment: Int)
    val buffer: ByteArray?
    fun position(): Int
    fun length(): Int
}