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

/**
 * Read binary data in little endian order from random access file.
 */
class LEFileBinaryReader(filepath: String, charset: Charset = TextUtil.UTF8()) : AbstractFileBinaryReader(filepath, charset) {

    @Throws(IOException::class)
    override fun readI16(): Short {
        val buf = ByteArray(2)
        readFully(buf)
        return ByteIOUtil.i16LE(buf[0], buf[1])
    }

    @Throws(IOException::class)
    override fun readI32(): Int {
        val buf = ByteArray(4)
        readFully(buf)
        return ByteIOUtil.i32LE(buf[0], buf[1], buf[2], buf[3])
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
        return readI8().toInt() and 0xff
    }

    @Throws(IOException::class)
    override fun readU16(): Int {
        val buf = ByteArray(2)
        readFully(buf)
        return ByteIOUtil.u16LE(buf[0], buf[1])
    }

    ////////////////////////////////////////////////////////////////////
}