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

/**
 * CharRange range inside a char[], indexing is relative to start of the input char[].
 * It maintain the start, end index of the range in the input char[].
 */
class CharRange  constructor(
        private val text: CharArray,
        val start: Int = 0,
        val end: Int = text.size
) : ICharSequence {

    override fun getChars(srcBegin: Int, srcEnd: Int, dst: CharArray, dstBegin: Int) {
        System.arraycopy(text, srcBegin, dst, dstBegin, srcEnd - srcBegin)
    }

    override val length: Int get() = end - start

    override fun get(index: Int): Char {
        var index = index
        index += start
        if (index < start || index > end) throw IndexOutOfBoundsException("Start=$start, end=$end, index=$index")
        return text[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return CharRange(text, startIndex + this.start, endIndex + this.start)
    }

    override fun toString(): String {
        return String(text, start, end - start)
    }

}
