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

import sf.andrians.ancoreutil.util.struct.IIntList

object OnelinerUtil {
    fun getSource(s: CharSequence, start: Int, end: Int): CharArray {
        val len = end - start
        val source = CharArray(len)
        if (s is StringBuilder) {
            s.getChars(start, end, source, 0)
        } else {
            for (i in 0 until len) {
                source[i] = s[start + i]
            }
        }
        return source
    }

    fun simpleWord(
            s: CharSequence, start: Int, end: Int, wasspace: Boolean, softbreaks: IIntList?, spaceutil: ISpaceUtil): CharSequence? {
        val left = spaceutil.skipWhitespaces(s, start, end)
        val right = spaceutil.rskipWhitespaces(s, left, end)
        val noleading = left == start || wasspace || isSoftBreak(start, softbreaks)
        if (right == left) {
            return if (noleading) "" else " "
        }
        if (spaceutil.hasWhitespace(s, left, right)) {
            return null
        }
        val notrailing = right == end || isSoftBreak(right, softbreaks)
        if (noleading && notrailing) {
            return CharSequenceRange(s, left, right)
        }
        val ret = StringBuilder()
        if (!noleading) {
            ret.append(' ')
        }
        ret.append(s, left, right)
        if (!notrailing) {
            ret.append(' ')
        }
        return ret
    }

    fun isSoftBreak(i: Int, breaks: IIntList?): Boolean {
        return breaks != null && breaks.binarySearch(i) >= 0
    } //////////////////////////////////////////////////////////////////////
}
