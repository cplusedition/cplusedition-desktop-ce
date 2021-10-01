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

interface IByteWriter {

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

    @Throws(IOException::class)
    fun write(value: ByteArray)

    @Throws(IOException::class)
    fun write(value: ByteArray, offset: Int, length: Int)

    @Throws(IOException::class)
    fun close()

    @Throws(IOException::class)
    fun length(): Long

    @Throws(IOException::class)
    fun position(): Long

    @Throws(IOException::class)
    fun seek(pos: Long)

    @Throws(IOException::class)
    fun pad(start: Long, alignment: Int, value: Byte)
}