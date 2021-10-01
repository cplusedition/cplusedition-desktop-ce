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

class ByteRange : IByteSequence {

    var array: ByteArray
    var start: Int
    var end: Int

    constructor(a: ByteArray) {
        array = a
        start = 0
        end = a.size
    }

    constructor(a: ByteArray, start: Int, end: Int) {
        array = a
        this.start = start
        this.end = end
    }

    //////////////////////////////////////////////////////////////////////

    override fun u8(index: Int): Int {
        return array[index].toInt() and 0xff
    }

    override fun u8(index: Int, shift: Int): Int {
        return u8(index) shl shift
    }

    override fun size(): Int {
        return end - start
    }

    override fun byteAt(index: Int): Byte {
        return array[start + index]
    }

    override fun subSequence(start: Int, end: Int): IByteSequence {
        return ByteRange(array, this.start + start, this.start + end)
    }

    override fun getBytes(srcstart: Int, srcend: Int, dst: ByteArray?, dststart: Int) {
        System.arraycopy(array, start + srcstart, dst, dststart, srcend - srcstart)
    }

    @Throws(IOException::class)
    override fun write(os: OutputStream, start: Int, end: Int) {
        os.write(array, start, end - start)
    }

    override fun toArray(): ByteArray {
        val len = end - start
        val ret = ByteArray(len)
        System.arraycopy(array, start, ret, 0, len)
        return ret
    }

    //////////////////////////////////////////////////////////////////////
}
