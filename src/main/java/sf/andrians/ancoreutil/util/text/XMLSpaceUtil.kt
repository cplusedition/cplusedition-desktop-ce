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

class XMLSpaceUtil : AbstractSpaceUtil() {
    //////////////////////////////////////////////////////////////////////
    override fun isSpace(c: Char): Boolean {
        return c == ' ' || c == '\t'
    }

    override fun isWhitespace(c: Char): Boolean {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028'
    }

    override fun isLineBreak(c: Char): Boolean {
        return c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028'
    }

    override fun skipLineBreak(s: CharSequence, start: Int, end: Int): Int {
        if (start < end) {
            var c = s[start]
            if (c == '\n') return start + 1
            if (c == '\r') {
                return if (start + 1 < end && (s[start + 1].also { c = it } == '\n' || c == '\u0085')) start + 2 else start + 1
            }
            if (c == '\u0085' || c == '\u2028') return start + 1
        }
        return start
    }

    override fun rskipLineBreak(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        if (end > start) {
            val c = s[end - 1]
            if (c == '\n' || c == '\u0085') {
                --end
                if (end - 1 >= start && s[end - 1] == '\r') --end
            } else if (c == '\r' || c == '\u0085' || c == '\u2028') {
                --end
            }
        }
        return end
    } //////////////////////////////////////////////////////////////////////

    companion object {
        //////////////////////////////////////////////////////////////////////
        var singleton: XMLSpaceUtil? = null
            get() {
                if (field == null) field = XMLSpaceUtil()
                return field
            }
            private set
    }
}