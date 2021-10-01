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

class DefaultSpaceUtil : AbstractSpaceUtil() {
    override fun isWhitespace(c: Char): Boolean {
        for (i in WHITESPACES.indices.reversed()) {
            if (c == WHITESPACES[i]) return true
        }
        return false
    }

    override fun isSpace(c: Char): Boolean {
        for (i in SPACES.indices.reversed()) {
            if (c == SPACES[i]) return true
        }
        return false
    }

    override fun isLineBreak(c: Char): Boolean {
        return c == '\n' || c == '\r'
    }

    override fun skipLineBreak(s: CharSequence, start: Int, end: Int): Int {
        if (start < end) {
            val c = s[start]
            if (c == '\n') return start + 1
            if (c == '\r') {
                return if (start + 1 < end && s[start + 1] == '\n') start + 2 else start + 1
            }
        }
        return start
    }

    override fun rskipLineBreak(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        if (end > start) {
            val c = s[end - 1]
            if (c == '\n') {
                --end
                if (end - 1 >= start && s[end - 1] == '\r') --end
            } else if (c == '\r') {
                --end
            }
        }
        return end
    }

    override fun hasLineBreak(s: CharSequence): Boolean {
        var c: Char
        for (i in s.length - 1 downTo 0) {
            c = s[i]
            if (c == '\n' || c == '\r') return true
        }
        return false
    }

    override fun hasLineBreak(s: CharSequence, start: Int, end: Int): Boolean {
        var c: Char
        for (i in end - 1 downTo start) {
            c = s[i]
            if (c == '\n' || c == '\r') return true
        }
        return false
    }

    companion object {
        private val SPACES = charArrayOf(' ', '\t', '\u000c')
        private val WHITESPACES = charArrayOf(' ', '\t', '\u000c', '\n', '\r')
        private val singleton_ = lazy { DefaultSpaceUtil() }
        val  singleton get() = singleton_.value
    }
}