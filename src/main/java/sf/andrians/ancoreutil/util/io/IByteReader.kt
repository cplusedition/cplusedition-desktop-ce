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

interface IByteReader {
    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    fun readI8(): Byte

    @Throws(IOException::class)
    fun readU8(): Int

    @Throws(IOException::class)
    fun readI16BE(): Short

    @Throws(IOException::class)
    fun readU16BE(): Int

    @Throws(IOException::class)
    fun readI32BE(): Int

    @Throws(IOException::class)
    fun readI64BE(): Long

    @Throws(IOException::class)
    fun readI16LE(): Short

    @Throws(IOException::class)
    fun readU16LE(): Int

    @Throws(IOException::class)
    fun readI32LE(): Int

    @Throws(IOException::class)
    fun readI64LE(): Long

    /**
     * Read as much as possible.
     */
    @Throws(IOException::class)
    fun read(b: ByteArray?): Int

    /**
     * Read as much as possible.
     */
    @Throws(IOException::class)
    fun read(b: ByteArray?, offset: Int, len: Int): Int

    /**
     * Read the specified number of bytes, throw exception if not enough data.
     */
    @Throws(IOException::class)
    fun readFully(b: ByteArray)

    /**
     * Read the specified number of bytes, throw exception if not enough data.
     */
    @Throws(IOException::class)
    fun readFully(b: ByteArray, offset: Int, len: Int)

    ////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    fun length(): Long

    @Throws(IOException::class)
    fun position(): Long

    @Throws(IOException::class)
    fun remaining(): Long

    @Throws(IOException::class)
    fun hasRemaining(): Boolean

    @Throws(IOException::class)
    fun seek(pos: Long)

    @Throws(IOException::class)
    fun close()

    @Throws(IOException::class)
    fun align(start: Long, alignment: Int) ////////////////////////////////////////////////////////////////////
}