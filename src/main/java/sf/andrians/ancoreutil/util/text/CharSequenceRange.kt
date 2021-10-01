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

class CharSequenceRange  constructor(private var text: CharSequence, private var start: Int, private var end: Int = text.length) : CharSequence {
    operator fun set(s: CharSequence, start: Int, end: Int) {
        text = s
        this.start = start
        this.end = end
    }

    override val length: Int get() = end - start

    override fun get(index: Int): Char {
        var index = index
        index = start + index
        if (index < start || index >= end) throw IndexOutOfBoundsException("start=$start, end=$end, index=$index")
        return text[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return CharSequenceRange(text, this.start + startIndex, this.start + endIndex)
    }

    override fun toString(): String {
        val b = StringBuilder()
        for (i in start until end) b.append(text[i])
        return b.toString()
    }

}