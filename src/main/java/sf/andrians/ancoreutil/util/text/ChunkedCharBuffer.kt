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
package sf.andrians.ancoreutil.util.text

import java.util.*

class ChunkedCharBuffer : ICharSequence {
    private val buffers: MutableList<CharArray> = ArrayList()
    private var chunkSize = 8 * 1024
    private var length_ = 0

    constructor() {}
    constructor(chunksize: Int) {
        chunkSize = chunksize
    }

    fun newChunk(): CharArray {
        return CharArray(chunkSize)
    }

    fun add(chunk: CharArray) {
        buffers.add(chunk)
        length_ += chunk.size
    }

    override val length: Int get() = length_

    override fun get(index: Int): Char {
        val n = index / chunkSize
        val offset = index - n * chunkSize
        return buffers[n][offset]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return ChunkedCharSequence(startIndex, endIndex)
    }

    override fun getChars(srcBegin: Int, srcEnd: Int, dst: CharArray, dstBegin: Int) {
        var dstbegin = dstBegin
        var n = srcBegin / chunkSize
        var offset = srcBegin - n * chunkSize
        var len = srcEnd - srcBegin
        val dstend = dstbegin + len
        if (len > chunkSize - offset) len = chunkSize - offset
        var b = buffers[n]
        while (true) {
            System.arraycopy(b, offset, dst, dstbegin, len)
            dstbegin += len
            if (dstbegin >= dstend) break
            b = buffers[++n]
            offset = 0
            len = dstend - dstbegin
            if (len > chunkSize) len = chunkSize
        }
    }

    override fun toString(): String {
        return asString(0, length)
    }

    fun asString(start: Int, end: Int): String {
        val a = CharArray(end - start)
        getChars(start, end, a, 0)
        return String(a)
    }

    private inner class ChunkedCharSequence(private val start: Int, private val end: Int) : CharSequence {

        override val length: Int get() = end - start

        override fun get(index: Int): Char {
            return this@ChunkedCharBuffer[start + index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return ChunkedCharSequence(this.start + startIndex, this.start + endIndex)
        }

        override fun toString(): String {
            return asString(start, end)
        }
    }
}