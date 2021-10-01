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
import java.io.RandomAccessFile
import java.nio.charset.Charset

/**
 * Write binary data in little endian order to a random access file.
 */
class LEFileBinaryWriter : AbstractFileBinaryWriter {

    constructor(fname: String, mode: String, charset: Charset? = null) : super(fname, mode, charset) {}
    constructor(file: RandomAccessFile, charset: Charset? = null) : super(file, charset) {}

    @Throws(IOException::class)
    override fun write8(value: Int) {
        outStream.write(value)
    }

    @Throws(IOException::class)
    override fun write16(value: Int) {
        outStream.write(value and 0xff)
        outStream.write(value shr 8 and 0xff)
    }

    @Throws(IOException::class)
    override fun write32(value: Int) {
        outStream.write(value and 0xff)
        outStream.write(value shr 8 and 0xff)
        outStream.write(value shr 16 and 0xff)
        outStream.write(value shr 24 and 0xff)
    }

    @Throws(IOException::class)
    override fun write64(value: Long) {
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
}