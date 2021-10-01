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
/*
 * Created on Mar 8, 2004
 */
package sf.andrians.ancoreutil.util.io

import java.io.IOException

interface IBinaryWriter : IByteWriter {

    @Throws(IOException::class)
    fun write16(value: Int)

    @Throws(IOException::class)
    fun write32(value: Int)

    @Throws(IOException::class)
    fun write64(value: Long)

    @Throws(IOException::class)
    fun writeFloat(value: Float)

    @Throws(IOException::class)
    fun writeDouble(value: Double)

    /**
     * Write character in configured character encoding.
     */
    @Throws(IOException::class)
    fun writeChar(value: Char)

    /**
     * Write characters in configured character encoding.
     */
    @Throws(IOException::class)
    fun writeChars(value: CharArray, offset: Int, length: Int)

    /**
     * Write string in configured character encoding.
     */
    @Throws(IOException::class)
    fun writeString(value: String)

}
