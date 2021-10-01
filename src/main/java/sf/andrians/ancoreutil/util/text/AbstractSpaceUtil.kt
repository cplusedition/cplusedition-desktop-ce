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

abstract class AbstractSpaceUtil : ISpaceUtil {
    ////////////////////////////////////////////////////////////////////////
    override fun isSpaces(s: CharSequence): Boolean {
        for (i in s.length - 1 downTo 0) {
            if (!isSpace(s[i])) {
                return false
            }
        }
        return true
    }

    override fun isSpaces(s: CharSequence, start: Int, end: Int): Boolean {
        for (i in end - 1 downTo start) {
            if (!isSpace(s[i])) {
                return false
            }
        }
        return true
    }

    override fun isWhitespaces(s: CharSequence): Boolean {
        for (i in s.length - 1 downTo 0) {
            if (!isWhitespace(s[i])) {
                return false
            }
        }
        return true
    }

    override fun isWhitespaces(s: CharSequence, start: Int, end: Int): Boolean {
        for (i in end - 1 downTo start) {
            if (!isWhitespace(s[i])) {
                return false
            }
        }
        return true
    }

    override fun skipSpaces(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (!isSpace(s[start])) {
                break
            }
            ++start
        }
        return start
    }

    override fun skipWhitespaces(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (!isWhitespace(s[start])) {
                break
            }
            ++start
        }
        return start
    }

    override fun rskipSpaces(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        while (end > start) {
            if (!isSpace(s[end - 1])) {
                break
            }
            --end
        }
        return end
    }

    override fun rskipWhitespaces(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        while (end > start) {
            if (!isWhitespace(s[end - 1])) {
                break
            }
            --end
        }
        return end
    }

    override fun skipNonSpaces(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (isSpace(s[start])) {
                break
            }
            ++start
        }
        return start
    }

    override fun skipNonWhitespaces(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (isWhitespace(s[start])) {
                break
            }
            ++start
        }
        return start
    }

    override fun rskipNonSpaces(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        while (end > start) {
            if (isSpace(s[end - 1])) {
                break
            }
            --end
        }
        return end
    }

    override fun rskipNonWhitespaces(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        while (end > start) {
            if (isWhitespace(s[end - 1])) {
                break
            }
            --end
        }
        return end
    }

    override fun endsWithLineBreak(s: CharSequence): Boolean {
        val len = s.length
        return rskipLineBreak(s, 0, len) != len
    }

    override fun endsWithSpaces(s: CharSequence): Boolean {
        val len = s.length
        return rskipSpaces(s, 0, len) != len
    }

    override fun endsWithWhitespaces(s: CharSequence): Boolean {
        val len = s.length
        return rskipWhitespaces(s, 0, len) != len
    }

    override fun countLines(s: CharSequence): Int {
        return countLines(s, 0, s.length)
    }

    /** @return Number of lines, including the last line which need not be terminated by a line delimiter.
     */
    override fun countLines(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        var ret = 0
        while (start < end) {
            ++ret
            start = skipLineSafe(s, start, end)
            start = skipLineBreak(s, start, end)
        }
        return ret
    }

    override fun lcountLineBreaks(s: CharSequence): Int {
        return lcountLineBreaks(s, 0, s.length)
    }

    override fun lcountLineBreaks(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        var ret = 0
        var offset: Int
        while (skipLineBreak(s, start, end).also { offset = it } != start) {
            ++ret
            start = offset
        }
        return ret
    }

    override fun lcountBlankLines(s: CharSequence): Int {
        return lcountBlankLines(s, 0, s.length)
    }

    override fun lcountBlankLines(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        var ret = -1
        var offset: Int
        start = skipSpaces(s, start, end)
        while (skipLineBreak(s, start, end).also { offset = it } != start) {
            ++ret
            start = skipSpaces(s, offset, end)
        }
        return ret
    }

    override fun rcountLineBreaks(s: CharSequence): Int {
        return rcountLineBreaks(s, 0, s.length)
    }

    override fun rcountLineBreaks(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        var ret = 0
        var e: Int
        while (rskipLineBreak(s, start, end).also { e = it } != end) {
            ++ret
            end = e
        }
        return ret
    }

    override fun rcountBlankLines(s: CharSequence): Int {
        return rcountBlankLines(s, 0, s.length)
    }

    override fun rcountBlankLines(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        var ret = -1
        var e: Int
        end = rskipSpaces(s, start, end)
        while (rskipLineBreak(s, start, end).also { e = it } != end) {
            ++ret
            end = rskipSpaces(s, start, e)
        }
        return ret
    }

    override fun skipLine(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (skipLineBreak(s, start, end) != start) {
                return start
            }
            ++start
        }
        return -1
    }

    override fun skipLineSafe(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        while (start < end) {
            if (skipLineBreak(s, start, end) != start) {
                return start
            }
            ++start
        }
        return start
    }

    override fun skipToNextLine(s: CharSequence, start: Int, end: Int): Int {
        var start = start
        var index: Int
        while (start < end) {
            if (skipLineBreak(s, start, end).also { index = it } != start) {
                return index
            }
            ++start
        }
        return start
    }

    override fun rskipLine(s: CharSequence, start: Int, end: Int): Int {
        var end = end
        while (end > start) {
            if (rskipLineBreak(s, start, end) != end) {
                return end
            }
            --end
        }
        return start
    }

    override fun hasLineBreak(s: CharSequence): Boolean {
        return skipLine(s, 0, s.length) >= 0
    }

    override fun hasLineBreak(s: CharSequence, start: Int, end: Int): Boolean {
        return skipLine(s, start, end) >= 0
    }

    override fun hasWhitespace(s: CharSequence, start: Int, end: Int): Boolean {
        var start = start
        while (start < end) {
            if (isWhitespace(s[start])) {
                return true
            }
            ++start
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////////
    override fun ltrimSpaces(b: CharSequence): CharSequence {
        return ltrimSpaces(b, 0, b.length)
    }

    override fun ltrimSpaces(b: CharSequence, start: Int, end: Int): CharSequence {
        return b.subSequence(skipSpaces(b, start, end), end)
    }

    override fun ltrimWhitespaces(b: CharSequence): CharSequence {
        return ltrimWhitespaces(b, 0, b.length)
    }

    override fun ltrimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence {
        return b.subSequence(skipWhitespaces(b, start, end), end)
    }

    override fun ltrimLineBreaks(buf: CharSequence): CharSequence {
        var start = 0
        val end = buf.length
        var e: Int
        while (skipLineBreak(buf, start, end).also { e = it } != start) {
            start = e
        }
        return buf.subSequence(start, end)
    }

    override fun ltrimBlankLines(buf: CharSequence): CharSequence {
        val end = buf.length
        var start = skipSpaces(buf, 0, end)
        var prev = 0
        var s: Int
        while (skipLineBreak(buf, start, end).also { s = it } != start) {
            prev = s
            start = skipSpaces(buf, s, end)
        }
        return buf.subSequence(prev, end)
    }

    override fun rtrimSpaces(b: CharSequence): CharSequence {
        return rtrimSpaces(b, 0, b.length)
    }

    override fun rtrimSpaces(b: CharSequence, start: Int, end: Int): CharSequence {
        return b.subSequence(start, rskipSpaces(b, start, end))
    }

    override fun rtrimWhitespaces(b: CharSequence): CharSequence {
        return rtrimWhitespaces(b, 0, b.length)
    }

    override fun rtrimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence {
        return b.subSequence(start, rskipWhitespaces(b, start, end))
    }

    override fun rtrimLineBreaks(buf: CharSequence): CharSequence {
        var end = buf.length
        var e: Int
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            end = e
        }
        return buf.subSequence(0, end)
    }

    override fun rtrimBlankLines(buf: CharSequence): CharSequence {
        var end = rskipSpaces(buf, 0, buf.length)
        var prev = end
        var e: Int
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            prev = end
            end = rskipSpaces(buf, 0, e)
        }
        return buf.subSequence(0, prev)
    }

    override fun trimSpaces(b: CharSequence): CharSequence {
        return trimSpaces(b, 0, b.length)
    }

    override fun trimSpaces(b: CharSequence, start: Int, end: Int): CharSequence {
        val s = skipSpaces(b, start, end)
        val e = rskipSpaces(b, s, end)
        return b.subSequence(s, e)
    }

    override fun trimWhitespaces(b: CharSequence): CharSequence {
        return trimWhitespaces(b, 0, b.length)
    }

    override fun trimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence {
        val s = skipWhitespaces(b, start, end)
        val e = rskipWhitespaces(b, s, end)
        return b.subSequence(s, e)
    }

    ////////////////////////////////////////////////////////////////////////
    override fun ltrimSpaces(b: StringBuilder): Boolean {
        return ltrimSpaces(b, 0, b.length)
    }

    override fun ltrimSpaces(b: StringBuilder, start: Int, end: Int): Boolean {
        val s = skipSpaces(b, start, end)
        if (s == start) {
            return false
        }
        b.delete(start, s)
        return true
    }

    override fun ltrimWhitespaces(b: StringBuilder): Boolean {
        return ltrimWhitespaces(b, 0, b.length)
    }

    override fun ltrimWhitespaces(b: StringBuilder, start: Int, end: Int): Boolean {
        val s = skipWhitespaces(b, start, end)
        if (s == start) {
            return false
        }
        b.delete(start, s)
        return true
    }

    override fun ltrimLineBreaks(buf: StringBuilder): Int {
        var ret = 0
        var end = buf.length
        var e: Int
        while (skipLineBreak(buf, 0, end).also { e = it } != end) {
            end = e
            ++ret
        }
        if (ret > 0) {
            buf.delete(0, end)
        }
        return ret
    }

    override fun ltrimBlankLines(buf: StringBuilder): Int {
        var ret = 0
        val end = buf.length
        var start = skipSpaces(buf, 0, end)
        var prev = 0
        var s: Int
        while (skipLineBreak(buf, start, end).also { s = it } != start) {
            prev = s
            start = skipSpaces(buf, s, end)
            ++ret
        }
        if (ret > 0) {
            buf.delete(0, prev)
        }
        return ret
    }

    override fun rtrimSpaces(b: StringBuilder): Boolean {
        return rtrimSpaces(b, 0, b.length)
    }

    override fun rtrimSpaces(b: StringBuilder, start: Int, end: Int): Boolean {
        val e = rskipSpaces(b, start, end)
        if (e == end) {
            return false
        }
        b.delete(e, end)
        return true
    }

    override fun rtrimWhitespaces(b: StringBuilder): Boolean {
        return rtrimWhitespaces(b, 0, b.length)
    }

    override fun rtrimWhitespaces(b: StringBuilder, start: Int, end: Int): Boolean {
        val e = rskipWhitespaces(b, start, end)
        if (e == end) {
            return false
        }
        b.delete(e, end)
        return true
    }

    override fun rtrimLineBreaks(buf: StringBuilder): Int {
        var ret = 0
        var e: Int
        var end = buf.length
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            end = e
            ++ret
        }
        if (ret > 0) {
            buf.setLength(end)
        }
        return ret
    }

    override fun rtrimBlankLines(buf: StringBuilder): Int {
        var ret = -1
        var e: Int
        var end = rskipSpaces(buf, 0, buf.length)
        var prev = end
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            prev = end
            end = rskipSpaces(buf, 0, e)
            ++ret
        }
        if (ret > 0) {
            buf.setLength(prev)
        }
        return ret
    }

    override fun rspace(buf: StringBuilder): Boolean {
        val len = buf.length
        if (len > 0 && !isWhitespace(buf[len - 1])) {
            buf.append(' ')
            return true
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////////

    override fun ltrimSpaces(b: StringBuffer): Boolean {
        return ltrimSpaces(b, 0, b.length)
    }

    override fun ltrimSpaces(b: StringBuffer, start: Int, end: Int): Boolean {
        val s = skipSpaces(b, start, end)
        if (s == start) {
            return false
        }
        b.delete(start, s)
        return true
    }

    override fun ltrimWhitespaces(b: StringBuffer): Boolean {
        return ltrimWhitespaces(b, 0, b.length)
    }

    override fun ltrimWhitespaces(b: StringBuffer, start: Int, end: Int): Boolean {
        val s = skipWhitespaces(b, start, end)
        if (s == start) {
            return false
        }
        b.delete(start, s)
        return true
    }

    override fun ltrimLineBreaks(buf: StringBuffer): Int {
        var ret = 0
        var end = buf.length
        var e: Int
        while (skipLineBreak(buf, 0, end).also { e = it } != end) {
            end = e
            ++ret
        }
        if (ret > 0) {
            buf.delete(0, end)
        }
        return ret
    }

    override fun ltrimBlankLines(buf: StringBuffer): Int {
        var ret = 0
        val end = buf.length
        var start = skipSpaces(buf, 0, end)
        var prev = 0
        var s: Int
        while (skipLineBreak(buf, start, end).also { s = it } != start) {
            prev = s
            start = skipSpaces(buf, s, end)
            ++ret
        }
        if (ret > 0) {
            buf.delete(0, prev)
        }
        return ret
    }

    override fun rtrimSpaces(b: StringBuffer): Boolean {
        return rtrimSpaces(b, 0, b.length)
    }

    override fun rtrimSpaces(b: StringBuffer, start: Int, end: Int): Boolean {
        val e = rskipSpaces(b, start, end)
        if (e == end) {
            return false
        }
        b.delete(e, end)
        return true
    }

    override fun rtrimWhitespaces(b: StringBuffer): Boolean {
        return rtrimWhitespaces(b, 0, b.length)
    }

    override fun rtrimWhitespaces(b: StringBuffer, start: Int, end: Int): Boolean {
        val e = rskipWhitespaces(b, start, end)
        if (e == end) {
            return false
        }
        b.delete(e, end)
        return true
    }

    override fun rtrimLineBreaks(buf: StringBuffer): Int {
        var ret = 0
        var e: Int
        var end = buf.length
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            end = e
            ++ret
        }
        if (ret > 0) {
            buf.setLength(end)
        }
        return ret
    }

    override fun rtrimBlankLines(buf: StringBuffer): Int {
        var ret = -1
        var e: Int
        var end = rskipSpaces(buf, 0, buf.length)
        var prev = end
        while (rskipLineBreak(buf, 0, end).also { e = it } != end) {
            prev = end
            end = rskipSpaces(buf, 0, e)
            ++ret
        }
        if (ret > 0) {
            buf.setLength(prev)
        }
        return ret
    }

    override fun rspace(buf: StringBuffer): Boolean {
        val len = buf.length
        if (len > 0 && !isWhitespace(buf[len - 1])) {
            buf.append(' ')
            return true
        }
        return false
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * Determine column width of given string.  Input string must not contains line breaks.
     * @return 0-based column number.
     */
    override fun columnOf(s: CharSequence, start: Int, end: Int, lmargin: Int, tabwidth: Int): Int {
        var start = start
        var column = lmargin
        var c: Char
        while (start < end) {
            c = s[start]
            if (c == '\t') {
                column = column - column % tabwidth + tabwidth
                ++start
                continue
            }
            if (isLineBreak(c)) {
                throw AssertionError("Unexpected line break character: buf=$s, index=$start")
            }
            ++column
            ++start
        }
        return column
    }

    override fun splitLines(str: CharSequence): List<String> {
        return splitLines(str, false)
    }

    fun splitLines(str: CharSequence, withlinebreak: Boolean): List<String> {
        val ret: MutableList<String> = ArrayList()
        var start = 0
        val end = str.length
        var e: Int
        var index = 0
        while (index < end) {
            if (skipLineBreak(str, index, end).also { e = it } != index) {
                ret.add(str.subSequence(start, if (withlinebreak) e else index).toString())
                index = e - 1
                start = e
            }
            ++index
        }
        if (start < str.length) {
            ret.add(str.subSequence(start, end).toString())
        }
        return ret
    }

    override fun splitWords(ret: MutableCollection<String>, str: CharSequence, start: Int, end: Int): Int {
        var start = start
        var count = 0
        val b = StringBuilder()
        var c: Char
        do {
            start = skipWhitespaces(str, start, end)
            while (start < end) {
                c = str[start++]
                if (isWhitespace(c)) break
                b.append(c.toString())
            }
            if (b.isNotEmpty()) {
                ret.add(b.toString())
                ++count
                b.setLength(0)
            }
        } while (start < end)
        return count
    }

    ////////////////////////////////////////////////////////////////////////
}