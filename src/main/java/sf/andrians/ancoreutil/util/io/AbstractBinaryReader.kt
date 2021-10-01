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
import java.io.InputStream
import java.nio.charset.Charset

abstract class AbstractBinaryReader(val charSet: Charset = TextUtil.UTF8()) : InputStream(), IBinaryReader {

    private var decoder = lazy {
        charSet.newDecoder()
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    override fun read(): Int {
        return readU8()
    }

    @Throws(IOException::class)
    fun readFully(len: Int): ByteArray {
        val ret = ByteArray(len)
        readFully(ret)
        return ret
    }

    //#BEGIN FIXME
    //#END FIXME

    @Throws(IOException::class)
    override fun readFloat(): Float {
        val value = readI32()
        return java.lang.Float.intBitsToFloat(value)
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        val value = readI64()
        return java.lang.Double.longBitsToDouble(value)
    }

    ////////////////////////////////////////////////////////////////////

    companion object {
        const val ERROR_EOF = "Reading pass end of input"
        const val ERROR_CLOSED = "Reading of closed input"
    }

}
