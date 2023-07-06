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
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSpaceUtil implements ISpaceUtil {

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isSpaces(final CharSequence s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            if (!isSpace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSpaces(
        final CharSequence s, final int start, final int end) {
        for (int i = end - 1; i >= start; --i) {
            if (!isSpace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWhitespaces(final CharSequence s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            if (!isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWhitespaces(
        final CharSequence s, final int start, final int end) {
        for (int i = end - 1; i >= start; --i) {
            if (!isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int skipSpaces(final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (!isSpace(s.charAt(start))) {
                break;
            }
        }
        return start;
    }

    @Override
    public int skipWhitespaces(final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (!isWhitespace(s.charAt(start))) {
                break;
            }
        }
        return start;
    }

    @Override
    public int rskipSpaces(final CharSequence s, final int start, int end) {
        for (; end > start; --end) {
            if (!isSpace(s.charAt(end - 1))) {
                break;
            }
        }
        return end;
    }

    @Override
    public int rskipWhitespaces(final CharSequence s, final int start, int end) {
        for (; end > start; --end) {
            if (!isWhitespace(s.charAt(end - 1))) {
                break;
            }
        }
        return end;
    }

    @Override
    public int skipNonSpaces(final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (isSpace(s.charAt(start))) {
                break;
            }
        }
        return start;
    }

    @Override
    public int skipNonWhitespaces(
        final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (isWhitespace(s.charAt(start))) {
                break;
            }
        }
        return start;
    }

    @Override
    public int rskipNonSpaces(final CharSequence s, final int start, int end) {
        for (; end > start; --end) {
            if (isSpace(s.charAt(end - 1))) {
                break;
            }
        }
        return end;
    }

    @Override
    public int rskipNonWhitespaces(
        final CharSequence s, final int start, int end) {
        for (; end > start; --end) {
            if (isWhitespace(s.charAt(end - 1))) {
                break;
            }
        }
        return end;
    }

    @Override
    public boolean endsWithLineBreak(final CharSequence s) {
        final int len = s.length();
        return rskipLineBreak(s, 0, len) != len;
    }

    @Override
    public boolean endsWithSpaces(final CharSequence s) {
        final int len = s.length();
        return rskipSpaces(s, 0, len) != len;
    }

    @Override
    public boolean endsWithWhitespaces(final CharSequence s) {
        final int len = s.length();
        return rskipWhitespaces(s, 0, len) != len;
    }

    @Override
    public int countLines(final CharSequence s) {
        return countLines(s, 0, s.length());
    }

    /**
     * @return Number of lines, including the last line which need not be terminated by a line delimiter.
     */
    @Override
    public int countLines(final CharSequence s, int start, final int end) {
        int ret = 0;
        while (start < end) {
            ++ret;
            start = skipLineSafe(s, start, end);
            start = skipLineBreak(s, start, end);
        }
        return ret;
    }

    @Override
    public int lcountLineBreaks(final CharSequence s) {
        return lcountLineBreaks(s, 0, s.length());
    }

    @Override
    public int lcountLineBreaks(final CharSequence s, int start, final int end) {
        int ret = 0;
        int offset;
        while ((offset = skipLineBreak(s, start, end)) != start) {
            ++ret;
            start = offset;
        }
        return ret;
    }

    @Override
    public int lcountBlankLines(final CharSequence s) {
        return lcountBlankLines(s, 0, s.length());
    }

    @Override
    public int lcountBlankLines(final CharSequence s, int start, final int end) {
        int ret = -1;
        int offset;
        start = skipSpaces(s, start, end);
        while ((offset = skipLineBreak(s, start, end)) != start) {
            ++ret;
            start = skipSpaces(s, offset, end);
        }
        return ret;
    }

    @Override
    public int rcountLineBreaks(final CharSequence s) {
        return rcountLineBreaks(s, 0, s.length());
    }

    @Override
    public int rcountLineBreaks(final CharSequence s, final int start, int end) {
        int ret = 0;
        int e;
        while ((e = rskipLineBreak(s, start, end)) != end) {
            ++ret;
            end = e;
        }
        return ret;
    }

    @Override
    public int rcountBlankLines(final CharSequence s) {
        return rcountBlankLines(s, 0, s.length());
    }

    @Override
    public int rcountBlankLines(final CharSequence s, final int start, int end) {
        int ret = -1;
        int e;
        end = rskipSpaces(s, start, end);
        while ((e = rskipLineBreak(s, start, end)) != end) {
            ++ret;
            end = rskipSpaces(s, start, e);
        }
        return ret;
    }

    @Override
    public int skipLine(final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (skipLineBreak(s, start, end) != start) {
                return start;
            }
        }
        return -1;
    }

    @Override
    public int skipLineSafe(final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (skipLineBreak(s, start, end) != start) {
                return start;
            }
        }
        return start;
    }

    @Override
    public int skipToNextLine(final CharSequence s, int start, final int end) {
        int index;
        for (; start < end; ++start) {
            if ((index = skipLineBreak(s, start, end)) != start) {
                return index;
            }
        }
        return start;
    }

    @Override
    public int rskipLine(final CharSequence s, final int start, int end) {
        while (end > start) {
            if (rskipLineBreak(s, start, end) != end) {
                return end;
            }
            --end;
        }
        return start;
    }

    @Override
    public boolean hasLineBreak(final CharSequence s) {
        return skipLine(s, 0, s.length()) >= 0;
    }

    @Override
    public boolean hasLineBreak(
        final CharSequence s, final int start, final int end) {
        return skipLine(s, start, end) >= 0;
    }

    @Override
    public boolean hasWhitespace(
        final CharSequence s, int start, final int end) {
        for (; start < end; ++start) {
            if (isWhitespace(s.charAt(start))) {
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public CharSequence ltrimSpaces(final CharSequence b) {
        return ltrimSpaces(b, 0, b.length());
    }

    @Override
    public CharSequence ltrimSpaces(
        final CharSequence b, final int start, final int end) {
        return b.subSequence(skipSpaces(b, start, end), end);
    }

    @Override
    public CharSequence ltrimWhitespaces(final CharSequence b) {
        return ltrimWhitespaces(b, 0, b.length());
    }

    @Override
    public CharSequence ltrimWhitespaces(
        final CharSequence b, final int start, final int end) {
        return b.subSequence(skipWhitespaces(b, start, end), end);
    }

    @Override
    public CharSequence ltrimLineBreaks(final CharSequence buf) {
        int start = 0;
        final int end = buf.length();
        for (int e; ((e = skipLineBreak(buf, start, end)) != start); ) {
            start = e;
        }
        return buf.subSequence(start, end);
    }

    @Override
    public CharSequence ltrimBlankLines(final CharSequence buf) {
        final int end = buf.length();
        int start = skipSpaces(buf, 0, end);
        int prev = 0;
        for (int s; (s = skipLineBreak(buf, start, end)) != start; ) {
            prev = s;
            start = skipSpaces(buf, s, end);
        }
        return buf.subSequence(prev, end);
    }

    @Override
    public CharSequence rtrimSpaces(final CharSequence b) {
        return rtrimSpaces(b, 0, b.length());
    }

    @Override
    public CharSequence rtrimSpaces(
        final CharSequence b, final int start, final int end) {
        return b.subSequence(start, rskipSpaces(b, start, end));
    }

    @Override
    public CharSequence rtrimWhitespaces(final CharSequence b) {
        return rtrimWhitespaces(b, 0, b.length());
    }

    @Override
    public CharSequence rtrimWhitespaces(
        final CharSequence b, final int start, final int end) {
        return b.subSequence(start, rskipWhitespaces(b, start, end));
    }

    @Override
    public CharSequence rtrimLineBreaks(final CharSequence buf) {
        int end = buf.length();
        for (int e; (e = rskipLineBreak(buf, 0, end)) != end; ) {
            end = e;
        }
        return buf.subSequence(0, end);
    }

    @Override
    public CharSequence rtrimBlankLines(final CharSequence buf) {
        int end = rskipSpaces(buf, 0, buf.length());
        int prev = end;
        for (int e; (e = rskipLineBreak(buf, 0, end)) != end; ) {
            prev = end;
            end = rskipSpaces(buf, 0, e);
        }
        return buf.subSequence(0, prev);
    }

    @Override
    public CharSequence trimSpaces(final CharSequence b) {
        return trimSpaces(b, 0, b.length());
    }

    @Override
    public CharSequence trimSpaces(
        final CharSequence b, final int start, final int end) {
        final int s = skipSpaces(b, start, end);
        final int e = rskipSpaces(b, s, end);
        return b.subSequence(s, e);
    }

    @Override
    public CharSequence trimWhitespaces(final CharSequence b) {
        return trimWhitespaces(b, 0, b.length());
    }

    @Override
    public CharSequence trimWhitespaces(
        final CharSequence b, final int start, final int end) {
        final int s = skipWhitespaces(b, start, end);
        final int e = rskipWhitespaces(b, s, end);
        return b.subSequence(s, e);
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean ltrimSpaces(final StringBuilder b) {
        return ltrimSpaces(b, 0, b.length());
    }

    @Override
    public boolean ltrimSpaces(
        final StringBuilder b, final int start, final int end) {
        final int s = skipSpaces(b, start, end);
        if (s == start) {
            return false;
        }
        b.delete(start, s);
        return true;
    }

    @Override
    public boolean ltrimWhitespaces(final StringBuilder b) {
        return ltrimWhitespaces(b, 0, b.length());
    }

    @Override
    public boolean ltrimWhitespaces(
        final StringBuilder b, final int start, final int end) {
        final int s = skipWhitespaces(b, start, end);
        if (s == start) {
            return false;
        }
        b.delete(start, s);
        return true;
    }

    @Override
    public int ltrimLineBreaks(final StringBuilder buf) {
        int ret = 0;
        int end = buf.length();
        for (int e; ((e = skipLineBreak(buf, 0, end)) != end); ) {
            end = e;
            ++ret;
        }
        if (ret > 0) {
            buf.delete(0, end);
        }
        return ret;
    }

    @Override
    public int ltrimBlankLines(final StringBuilder buf) {
        int ret = 0;
        final int end = buf.length();
        int start = skipSpaces(buf, 0, end);
        int prev = 0;
        for (int s; (s = skipLineBreak(buf, start, end)) != start; ) {
            prev = s;
            start = skipSpaces(buf, s, end);
            ++ret;
        }
        if (ret > 0) {
            buf.delete(0, prev);
        }
        return ret;
    }

    @Override
    public boolean rtrimSpaces(final StringBuilder b) {
        return rtrimSpaces(b, 0, b.length());
    }

    @Override
    public boolean rtrimSpaces(
        final StringBuilder b, final int start, final int end) {
        final int e = rskipSpaces(b, start, end);
        if (e == end) {
            return false;
        }
        b.delete(e, end);
        return true;
    }

    @Override
    public boolean rtrimWhitespaces(final StringBuilder b) {
        return rtrimWhitespaces(b, 0, b.length());
    }

    @Override
    public boolean rtrimWhitespaces(
        final StringBuilder b, final int start, final int end) {
        final int e = rskipWhitespaces(b, start, end);
        if (e == end) {
            return false;
        }
        b.delete(e, end);
        return true;
    }

    @Override
    public int rtrimLineBreaks(final StringBuilder buf) {
        int ret = 0;
        int e;
        int end = buf.length();
        while ((e = rskipLineBreak(buf, 0, end)) != end) {
            end = e;
            ++ret;
        }
        if (ret > 0) {
            buf.setLength(end);
        }
        return ret;
    }

    @Override
    public int rtrimBlankLines(final StringBuilder buf) {
        int ret = -1;
        int e;
        int end = rskipSpaces(buf, 0, buf.length());
        int prev = end;
        while ((e = rskipLineBreak(buf, 0, end)) != end) {
            prev = end;
            end = rskipSpaces(buf, 0, e);
            ++ret;
        }
        if (ret > 0) {
            buf.setLength(prev);
        }
        return ret;
    }

    @Override
    public boolean rspace(final StringBuilder buf) {
        final int len = buf.length();
        if (len > 0 && !isWhitespace(buf.charAt(len - 1))) {
            buf.append(' ');
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean ltrimSpaces(final StringBuffer b) {
        return ltrimSpaces(b, 0, b.length());
    }

    @Override
    public boolean ltrimSpaces(
        final StringBuffer b, final int start, final int end) {
        final int s = skipSpaces(b, start, end);
        if (s == start) {
            return false;
        }
        b.delete(start, s);
        return true;
    }

    @Override
    public boolean ltrimWhitespaces(final StringBuffer b) {
        return ltrimWhitespaces(b, 0, b.length());
    }

    @Override
    public boolean ltrimWhitespaces(
        final StringBuffer b, final int start, final int end) {
        final int s = skipWhitespaces(b, start, end);
        if (s == start) {
            return false;
        }
        b.delete(start, s);
        return true;
    }

    @Override
    public int ltrimLineBreaks(final StringBuffer buf) {
        int ret = 0;
        int end = buf.length();
        for (int e; ((e = skipLineBreak(buf, 0, end)) != end); ) {
            end = e;
            ++ret;
        }
        if (ret > 0) {
            buf.delete(0, end);
        }
        return ret;
    }

    @Override
    public int ltrimBlankLines(final StringBuffer buf) {
        int ret = 0;
        final int end = buf.length();
        int start = skipSpaces(buf, 0, end);
        int prev = 0;
        for (int s; (s = skipLineBreak(buf, start, end)) != start; ) {
            prev = s;
            start = skipSpaces(buf, s, end);
            ++ret;
        }
        if (ret > 0) {
            buf.delete(0, prev);
        }
        return ret;
    }

    @Override
    public boolean rtrimSpaces(final StringBuffer b) {
        return rtrimSpaces(b, 0, b.length());
    }

    @Override
    public boolean rtrimSpaces(
        final StringBuffer b, final int start, final int end) {
        final int e = rskipSpaces(b, start, end);
        if (e == end) {
            return false;
        }
        b.delete(e, end);
        return true;
    }

    @Override
    public boolean rtrimWhitespaces(final StringBuffer b) {
        return rtrimWhitespaces(b, 0, b.length());
    }

    @Override
    public boolean rtrimWhitespaces(
        final StringBuffer b, final int start, final int end) {
        final int e = rskipWhitespaces(b, start, end);
        if (e == end) {
            return false;
        }
        b.delete(e, end);
        return true;
    }

    @Override
    public int rtrimLineBreaks(final StringBuffer buf) {
        int ret = 0;
        int e;
        int end = buf.length();
        while ((e = rskipLineBreak(buf, 0, end)) != end) {
            end = e;
            ++ret;
        }
        if (ret > 0) {
            buf.setLength(end);
        }
        return ret;
    }

    @Override
    public int rtrimBlankLines(final StringBuffer buf) {
        int ret = -1;
        int e;
        int end = rskipSpaces(buf, 0, buf.length());
        int prev = end;
        while ((e = rskipLineBreak(buf, 0, end)) != end) {
            prev = end;
            end = rskipSpaces(buf, 0, e);
            ++ret;
        }
        if (ret > 0) {
            buf.setLength(prev);
        }
        return ret;
    }

    @Override
    public boolean rspace(final StringBuffer buf) {
        final int len = buf.length();
        if (len > 0 && !isWhitespace(buf.charAt(len - 1))) {
            buf.append(' ');
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Determine column width of given string.  Input string must not contains line breaks.
     *
     * @return 0-based column number.
     */
    @Override
    public int columnOf(
        final CharSequence buf,
        int start,
        final int end,
        int column,
        final int tabwidth) {
        for (char c; start < end; ++start) {
            c = buf.charAt(start);
            if (c == '\t') {
                column = column - (column % tabwidth) + tabwidth;
                continue;
            }
            if (isLineBreak(c)) {
                throw new AssertionError(
                    "Unexpected line break character: buf="
                        + buf
                        + ", index="
                        + start
                );
            }
            ++column;
        }
        return column;
    }

    @Override
    public List<String> splitLines(final CharSequence str) {
        return splitLines(str, false);
    }

    public List<String> splitLines(
        final CharSequence str, final boolean withlinebreak) {
        if (str == null) {
            return null;
        }
        final List<String> ret = new ArrayList<>();
        int start = 0;
        final int end = str.length();
        int e;
        for (int index = 0; index < end; ++index) {
            if ((e = skipLineBreak(str, index, end)) != index) {
                ret.add(
                    str.subSequence(start, withlinebreak ? e : index).toString());
                index = e - 1;
                start = e;
            }
        }
        if (start < str.length()) {
            ret.add(str.subSequence(start, end).toString());
        }
        return ret;
    }

    @Override
    public int splitWords(
        final Collection<String> ret,
        final CharSequence str,
        int start,
        final int end) {
        int count = 0;
        final StringBuilder b = new StringBuilder();
        char c;
        do {
            start = skipWhitespaces(str, start, end);
            while (start < end && !isWhitespace(c = str.charAt(start++))) {
                b.append(c);
            }
            if (b.length() > 0) {
                ret.add(b.toString());
                ++count;
                b.setLength(0);
            }
        } while (start < end);
        return count;
    }

    ////////////////////////////////////////////////////////////////////////
}
