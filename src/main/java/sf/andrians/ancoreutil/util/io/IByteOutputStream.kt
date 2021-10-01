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
import java.io.Closeable
import java.io.Flushable
import java.io.IOException

interface IByteOutputStream : Closeable, Flushable {
    @Throws(IOException::class)
    fun write(value: Int)

    @Throws(IOException::class)
    fun write(value: ByteArray?)

    @Throws(IOException::class)
    fun write(value: ByteArray, offset: Int, len: Int)

    @Throws(IOException::class)
    fun write(value: IByteSequence?)

    @Throws(IOException::class)
    fun write(value: Boolean)

    /**
     * Same as write(int).
     */
    @Throws(IOException::class)
    fun write8(value: Int)

    @Throws(IOException::class)
    fun write16BE(value: Int)

    @Throws(IOException::class)
    fun write32BE(value: Int)

    @Throws(IOException::class)
    fun write64BE(value: Long)

    @Throws(IOException::class)
    fun write16LE(value: Int)

    @Throws(IOException::class)
    fun write32LE(value: Int)

    @Throws(IOException::class)
    fun write64LE(value: Long)

    /**
     * Write 16 bit native byte order.
     */
    @Throws(IOException::class)
    fun write16(value: Int)

    /**
     * Write 32 bit native byte order.
     */
    @Throws(IOException::class)
    fun write32(value: Int)

    /**
     * Write 64 bit native byte order.
     */
    @Throws(IOException::class)
    fun write64(value: Long)

    @Throws(IOException::class)
    fun seek(pos: Int)
    fun position(): Int
    fun length(): Int
    val buffer: ByteArray?
    fun toByteArray(): ByteArray
    fun toByteArray(start: Int, end: Int): ByteArray
}