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
import java.nio.charset.Charset

class LEByteArrayBinaryReader(a: ByteArray, charset: Charset = TextUtil.UTF8()) : AbstractByteArrayBinaryReader(a, charset) {

    @Throws(IOException::class)
    override fun readI8(): Byte {
        if (position == buffer.size) {
            throw IOException(ERROR_EOF)
        }
        return buffer[position++]
    }

    @Throws(IOException::class)
    override fun readI16(): Short {
        if (position + 2 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++]
        val b1 = buffer[position++]
        return ByteIOUtil.i16LE(b0, b1)
    }

    @Throws(IOException::class)
    override fun readI32(): Int {
        if (position + 4 > buffer.size) {
            throw IOException(ERROR_EOF)
        }
        val b0 = buffer[position++]
        val b1 = buffer[position++]
        val b2 = buffer[position++]
        val b3 = buffer[position++]
        return ByteIOUtil.i32LE(b0, b1, b2, b3)
    }

    @Throws(IOException::class)
    override fun readI64(): Long {
        val low = (readI32().toLong() and 0xffff_ffffL)
        val high = readI32().toLong()
        return (high shl 32) or low
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    override fun readU8(): Int {
        return buffer[position++].toInt() and 0xff
    }

    @Throws(IOException::class)
    override fun readU16(): Int {
        val b0 = buffer[position++]
        val b1 = buffer[position++]
        return ByteIOUtil.u16LE(b0, b1)
    }

    ////////////////////////////////////////////////////////////////////
}