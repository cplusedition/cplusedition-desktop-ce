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
package sf.andrians.ancoreutil.util.struct

import java.io.IOException
import java.io.OutputStream

/**
 * IByteSequence interface provide access to part of a binary blob.
 */
interface IByteSequence {
    fun size(): Int
    fun byteAt(index: Int): Byte
    fun u8(index: Int): Int
    fun u8(index: Int, shift: Int): Int
    fun subSequence(start: Int, end: Int): IByteSequence
    fun getBytes(srcstart: Int, srcend: Int, dst: ByteArray?, dststart: Int)

    @Throws(IOException::class)
    fun write(os: OutputStream, srcstart: Int, srcend: Int)
    fun toArray(): ByteArray
}
