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
package sf.llk.share.formatter;

import sf.llk.share.support.IIntStack;
import sf.llk.share.support.IntList;
import sf.llk.share.util.AbstractSpaceUtil;
import sf.llk.share.util.DefaultSpaceUtil;

/**
 * Text buffer for text formatted.
 * <p>
 * <li>FormatBuffer worked as a line buffer which holds text to be formatted, and backed by a
 * formatted buffer which holds formatted text.</li>
 * <li>Client append text to the buffer then call flush() when a hard line break is required.</li>
 * <li>Client also set the soft breaks while appending text.  If the line width is exceeded
 * when flush() is called, output would be wrapped by breaking at the soft breaks.</li>
 */
public class FormatBuffer {

    ////////////////////////////////////////////////////////////////////////

    public static final int EXPANDED = 0x00;
    public static final int COMPACT = 0x01;
    public static final int ONELINER = 0x02;
    public static final int DEF_LINEWIDTH = 80;
    public static final int DEF_TABWIDTH = 4;
    static final boolean DEBUG = false;
    private static final char[] CRLF = new char[]{'\r', '\n'};

    ////////////////////////////////////////////////////////////////////////
    protected final StringBuffer indentString;
    protected final StringBuffer lineBuffer;
    protected final StringBuffer formattedBuffer;
    protected final IntList formattedBreaks;
    protected final IntList breakList;
    protected final IntList markStack;
    protected AbstractSpaceUtil spaceUtil = DefaultSpaceUtil.getSingleton();
    protected int lineWidth;
    protected int tabWidth;
    protected int indentWidth;
    protected String tabString;
    protected String lineBreak;
    protected int formatMode;
    protected IIntStack indentStack;
    protected int indentsOnBreak;

    ////////////////////////////////////////////////////////////////////////

    public FormatBuffer() {
        this(DEF_LINEWIDTH, DEF_TABWIDTH, "", "\t", EXPANDED);
    }

    public FormatBuffer(int linewidth, int tabwidth) {
        this(linewidth, tabwidth, "", "\t", EXPANDED);
    }

    /**
     * @deprecated Use FormatBuffer(int tabwidth, String indent, String tab) instead.
     */
    @Deprecated
    public FormatBuffer(String indent, String tab, int tabwidth) {
        this(DEF_LINEWIDTH, tabwidth, indent, tab, EXPANDED);
    }

    public FormatBuffer(int tabwidth, String indent, String tab) {
        this(DEF_LINEWIDTH, tabwidth, indent, tab, EXPANDED);
    }

    public FormatBuffer(int width, int tabwidth, String indent, String tab, int mode) {
        this(width, tabwidth, indent, tab, "\n", mode);
    }

    public FormatBuffer(int width, int tabwidth, String indent, String tab, String linebreak, int mode) {
        lineWidth = width;
        tabWidth = tabwidth;
        indentString = new StringBuffer(indent);
        tabString = tab;
        lineBreak = linebreak;
        formatMode = mode;
        char c;
        indentWidth = 0;
        for (int i = 0; i < indentString.length(); ++i) {
            c = indentString.charAt(i);
            if (c == '\t') {
                indentWidth += tabWidth;
            } else {
                ++indentWidth;
            }
        }
        lineBuffer = new StringBuffer(128);
        formattedBuffer = new StringBuffer(256);
        formattedBreaks = new IntList();
        breakList = new IntList();
        markStack = new IntList();
        indentStack = new IntList();
    }

    public FormatBuffer(FormatBuffer a) {
        this();
        init(a);
    }

    public FormatBuffer(FormatBuffer a, IIntStack indentstack) {
        this();
        init(a);
        indentStack = indentstack;
    }

    /**
     * Skip LF/CRLF given that s.charAt(start) is either '\r' or '\n'.
     */
    public final static int skipCRLF(CharSequence s, int start, int end) {
        int ret = start + 1;
        if (s.charAt(start) == '\r' && ret < end && s.charAt(ret) == '\n') {
            ++ret;
        }
        return ret;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Init this to the given buffer's state.
     * This is usually used to init. state of lookahead buffers.
     */
    public void init(FormatBuffer b) {
        clear();
        lineWidth = b.lineWidth;
        indentWidth = b.indentWidth;
        tabWidth = b.tabWidth;
        lineBreak = b.lineBreak;
        indentString.setLength(0);
        indentString.append(b.indentString);
        tabString = b.tabString;
        formatMode = b.formatMode;
        indentsOnBreak = b.indentsOnBreak;
        spaceUtil = b.spaceUtil;
    }

    public void clear() {
        lineBuffer.setLength(0);
        formattedBuffer.setLength(0);
        formattedBreaks.clear();
        breakList.clear();
        markStack.clear();
        indentStack.clear();
        indentsOnBreak = 0;
        indentString.setLength(0);
    }

    public IIntStack getIndentStack() {
        return indentStack;
    }

    public AbstractSpaceUtil getSpaceUtil() {
        return spaceUtil;
    }

    public void setSpaceUtil(AbstractSpaceUtil util) {
        spaceUtil = util;
    }

    public int getTotalLength() {
        return formattedBuffer.length() + lineBuffer.length();
    }

    public int indexOf(String str) {
        int lineend = lineBuffer.length();
        if (lineend == 0) {
            return formattedBuffer.indexOf(str);
        }
        int len = formattedBuffer.length();
        formattedBuffer.append(indentString);
        int linestart = spaceUtil.skipSpaces(lineBuffer, 0, lineend);
        formattedBuffer.append(lineBuffer.toString().substring(linestart, lineend));
        int ret = formattedBuffer.indexOf(str);
        formattedBuffer.setLength(len);
        return ret;
    }

    @Override
    public String toString() {
        int lineend = lineBuffer.length();
        if (lineend == 0) {
            return formattedBuffer.toString();
        }
        StringBuffer ret = new StringBuffer();
        ret.append(formattedBuffer);
        if (spaceUtil.endsWithLineBreak(ret)) {
            ret.append(indentString);
            int linestart = spaceUtil.skipSpaces(lineBuffer, 0, lineend);
            ret.append(lineBuffer.toString().substring(linestart, lineend));
        } else {
            ret.append(lineBuffer);
        }
        return ret.toString();
    }

    public boolean trimLeadingSpaces() {
        boolean ret = spaceUtil.ltrimSpaces(formattedBuffer);
        if (formattedBuffer.length() == 0) {
            ret |= spaceUtil.ltrimSpaces(lineBuffer);
        }
        return ret;
    }

    public boolean trimLeadingWhitespaces() {
        boolean ret = spaceUtil.ltrimWhitespaces(formattedBuffer);
        if (formattedBuffer.length() == 0) {
            ret |= spaceUtil.ltrimWhitespaces(lineBuffer);
        }
        return ret;
    }

    /**
     * Trim trailing space characters in line/text buffer (preserve line breaks).
     */
    public boolean trimTrailingSpaces() {
        boolean ret = spaceUtil.rtrimSpaces(lineBuffer);
        if (lineBuffer.length() == 0) {
            ret |= spaceUtil.rtrimSpaces(formattedBuffer);
        }
        return ret;
    }

    /**
     * Trim trailing whitespace characters in line/text buffer (including line breaks).
     */
    public boolean trimTrailingWhitespaces() {
        boolean ret = spaceUtil.rtrimWhitespaces(lineBuffer);
        if (lineBuffer.length() == 0) {
            ret |= spaceUtil.rtrimWhitespaces(formattedBuffer);
            int index = formattedBuffer.lastIndexOf("\n");
            if (index >= 0) {
                append(formattedBuffer, index + 1, formattedBuffer.length());
                formattedBuffer.setLength(index + 1);
            }
        }
        return ret;
    }

    public int trimTrailingLineBreaks() {
        char c;
        int ret = 0;
        DONE:
        {
            for (int i = lineBuffer.length() - 1; i >= 0; --i) {
                c = lineBuffer.charAt(i);
                if (c == '\n') {
                    ++ret;
                    if (i > 0 && lineBuffer.charAt(i - 1) == '\r') {
                        --i;
                    }
                    continue;
                } else if (c == '\r') {
                    ++ret;
                    continue;
                }
                break DONE;
            }
            ret += spaceUtil.rtrimLineBreaks(formattedBuffer);
        }
        return ret;
    }

    public int trimTrailingBlankLines() {
        flush();
        return spaceUtil.rtrimBlankLines(formattedBuffer);
    }

    /**
     * @return true if line/formatted buffer ends with a line break
     * (with optionally some trailing whitespaces).
     */
    public boolean endsWithLineBreak() {
        char c;
        for (int i = lineBuffer.length() - 1; i >= 0; --i) {
            c = lineBuffer.charAt(i);
            if (c == ' ' || c == '\t' || c == 0xa0) {
                continue;
            }
            return (c == '\n');
        }
        return spaceUtil.endsWithLineBreak(formattedBuffer);
    }

    /**
     * @return true if merged buffer is not empty and ends with whitespace.
     */
    public boolean endsWithWhitespace() {
        int len = lineBuffer.length();
        if (len > 0) {
            return (spaceUtil.isWhitespace(lineBuffer.charAt(len - 1)));
        }
        len = formattedBuffer.length();
        return (len > 0 && spaceUtil.isWhitespace(formattedBuffer.charAt(len - 1)));
    }

    /**
     * @return The number of blank lines (ie. numer of line breaks-1) at end of text.
     */
    public int countTrailingBlankLines() {
        int len = lineBuffer.length();
        int count = -1;
        char c = '\0';
        char last;
        for (int i = len - 1; i >= 0; --i) {
            last = c;
            c = lineBuffer.charAt(i);
            if (c == '\n') {
                ++count;
            } else if (c == '\r' && last != '\n') {
                ++count;
            } else if (!spaceUtil.isWhitespace(c)) {
                return count;
            }
        }
        len = formattedBuffer.length();
        c = '\0';
        for (int i = len - 1; i >= 0; --i) {
            last = c;
            c = formattedBuffer.charAt(i);
            if (c == '\n') {
                ++count;
            } else if (c == '\r' && last != '\n') {
                ++count;
            } else if (!spaceUtil.isWhitespace(c)) {
                return count;
            }
        }
        return count;
    }

    public int countNonBlankLines() {
        int count = 0;
        if (!spaceUtil.isWhitespaces(lineBuffer)) {
            ++count;
        }
        char c;
        for (int i = 0, len = formattedBuffer.length(); i < len; ++i) {
            c = formattedBuffer.charAt(i);
            if (!spaceUtil.isWhitespace(c)) {
                ++count;
                i = skipLine(formattedBuffer, i + 1) - 1;
            }
        }
        return count;
    }

    /**
     * @return Column (0-based, ie. first column is column 0) of next insert position.
     */
    public int getColumn() {
        int column = indentWidth;
        char c;
        int len = lineBuffer.length();
        for (int i = spaceUtil.skipWhitespaces(lineBuffer, 0, len); i < len; ++i) {
            c = lineBuffer.charAt(i);
            if (c == '\t') {
                column = column - (column % tabWidth) + tabWidth;
            } else {
                ++column;
            }
        }
        return column;
    }

    /**
     * Determine column width of given string.
     *
     * @return 0-based column number.
     */
    public int columnWidthOf(CharSequence buf, int start, int end, int column) {
        for (char c; start < end; ++start) {
            c = buf.charAt(start);
            if (c == '\t') {
                column = column - (column % tabWidth) + tabWidth;
                continue;
            }
            if (c == '\r' || c == '\n') {
                break;
            }
            ++column;
        }
        return column;
    }

    public StringBuffer getLineBuffer() {
        return lineBuffer;
    }

    /**
     * @return The formatted text buffer.
     */
    public StringBuffer getFormatted() {
        return formattedBuffer;
    }

    public int length() {
        return lineBuffer.length();
    }

    public void setLength(int len) {
        lineBuffer.setLength(len);
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int linewidth) {
        lineWidth = linewidth;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public String getTab() {
        return tabString;
    }

    public String getLineBreak() {
        return lineBreak;
    }

    public boolean isOneLiner() {
        return formatMode == ONELINER;
    }

    public boolean isCompact() {
        return formatMode == COMPACT;
    }

    public int skipLine(StringBuffer buf, int start) {
        char c;
        int len = buf.length();
        for (int i = start; i < len; ++i) {
            c = buf.charAt(i);
            if (c == '\n') {
                return i + 1;
            } else if (c == '\r') {
                if (i + 1 < len && buf.charAt(i + 1) == '\n') {
                    return i + 2;
                }
                return i + 1;
            }
        }
        return len;
    }

    public int getFormatMode() {
        return formatMode;
    }

    public void setFormatMode(int mode) {
        formatMode = mode;
    }

    public boolean isBeyond(int column) {
        return getColumn() >= column;
    }

    /**
     * @return true if line buffer contains only whitespaces.
     */
    public boolean isBlank() {
        int len = lineBuffer.length();
        return len == 0 || spaceUtil.skipWhitespaces(lineBuffer, 0, len) == len;
    }

    public boolean canFit(int len) {
        return getColumn() + len < lineWidth;
    }

    public boolean canFit(CharSequence s, int margin) {
        return canFit(s, 0, s.length(), margin);
    }

    public boolean canFit(CharSequence s, int start, int end, int margin) {
        return columnWidthOf(s, start, end, getColumn()) + margin < lineWidth;
    }

    /**
     * Flush line buffer to text buffer and determine length of formatted buffer content
     * if it is formatted as an oneliner.
     * <p>
     * Usually, oneliner would collapse whitespaces into a single space.  However, in some cases,
     * it would be required to eliminate all the added spaces.  The break list can be used for such
     * purpose.  Whenever there is an added line break (and indents that come with it), setBreak()
     * is called to mark the position after the line break.  When converting to oneliner, such positions
     * and spaces after it would be eliminated.
     *
     * @param trimbreaks Trim line break and following spaces at locations in fBreaks.
     * @return Length of oneliner.
     */
    public int oneLinerLength(boolean trimbreaks, boolean trim_leading_space) {
        flush();
        int len = formattedBuffer.length() - 1;
        char c;
        for (; len >= 0; --len) {
            c = formattedBuffer.charAt(len);
            if (!spaceUtil.isWhitespace(c)) {
                break;
            }
        }
        ++len;
        if (len == 0) {
            return 0;
        }
        int ret = 0;
        boolean wasspace = trim_leading_space;
        for (int i = 0; i < len; ++i) {
            c = formattedBuffer.charAt(i);
            if (spaceUtil.isWhitespace(c)) {
                if (trimbreaks && c == '\n' && isFormattedBreak(i)) {
                    i = skipFormattedBreak(i);
                    wasspace = false;
                    continue;
                }
                if (wasspace) {
                    continue;
                }
                wasspace = true;
                ++ret;
                continue;
            }
            wasspace = false;
            ++ret;
        }
        return ret;
    }

    private boolean isFormattedBreak(int i) {
        return formattedBreaks.binarySearch(i + 1) >= 0;
    }

    /* @return Offset of the last space in fFormatted. */
    private int skipFormattedBreak(int i) {
        return spaceUtil.skipSpaces(formattedBuffer, i + 1, formattedBuffer.length()) - 1;
    }

    /**
     * Flush line buffer to text buffer and construct an oneliner from its content.
     *
     * @return The oneliner derived from the text buffer content.
     */
    public String getOneLiner(boolean trimbreaks, boolean trim_leading_space) {
        flush();
        int len = formattedBuffer.length() - 1;
        char c;
        for (; len >= 0; --len) {
            c = formattedBuffer.charAt(len);
            if (!spaceUtil.isWhitespace(c)) {
                break;
            }
        }
        ++len;
        if (len == 0) {
            return "";
        }
        boolean wasspace = trim_leading_space;
        for (int i = 0; i < len; ++i) {
            c = formattedBuffer.charAt(i);
            if (spaceUtil.isWhitespace(c)) {
                if (trimbreaks && c == '\n' && isFormattedBreak(i)) {
                    i = skipFormattedBreak(i);
                    wasspace = false;
                    continue;
                }
                if (wasspace) {
                    continue;
                }
                wasspace = true;
                append(' ');
                continue;
            }
            wasspace = false;
            append(c);
        }
        String ret = lineBuffer.toString();
        lineBuffer.setLength(0);
        return ret;
    }

    /**
     * Flush line buffer and converted formatted buffer to a oneliner.
     *
     * @return The formatted buffer.
     */
    public StringBuffer toOneLiner(boolean trimbreaks, boolean trim_leading_space) {
        flush();
        int len = formattedBuffer.length() - 1;
        char c;
        for (; len >= 0; --len) {
            c = formattedBuffer.charAt(len);
            if (!spaceUtil.isWhitespace(c)) {
                break;
            }
        }
        formattedBuffer.setLength(++len);
        if (len == 0) {
            return formattedBuffer;
        }
        boolean wasspace = trim_leading_space;
        int out = 0;
        for (int i = 0; i < len; ++i) {
            c = formattedBuffer.charAt(i);
            if (spaceUtil.isWhitespace(c)) {
                if (trimbreaks && c == '\n' && isFormattedBreak(i)) {
                    i = skipFormattedBreak(i);
                    wasspace = false;
                    continue;
                }
                if (wasspace) {
                    continue;
                }
                wasspace = true;
                formattedBuffer.setCharAt(out++, ' ');
                continue;
            }
            wasspace = false;
            formattedBuffer.setCharAt(out++, c);
        }
        formattedBuffer.setLength(out);
        return formattedBuffer;
    }

    ////////////////////////////////////////////////////////////////////////

    public String toOneLiner(CharSequence str, boolean trim_leading_space) {
        int len = str.length();
        StringBuilder ret = new StringBuilder();
        boolean wasspace = trim_leading_space;
        char c;
        for (int i = 0; i < len; ++i) {
            c = str.charAt(i);
            if (spaceUtil.isWhitespace(c)) {
                if (wasspace) {
                    continue;
                }
                wasspace = true;
                ret.append(' ');
                continue;
            }
            wasspace = false;
            ret.append(c);
        }
        return ret.toString();
    }

    ////////////////////////////////////////////////////////////////////////

    public void unshiftNonTerminatedLine() {
        if (lineBuffer.length() != 0) {
            throw new RuntimeException("Line buffer not empty: line=" + lineBuffer.toString());
        }
        if (!endsWithLineBreak()) {
            int len = formattedBuffer.length();
            int start = formattedBuffer.lastIndexOf("\n") + 1;
            append(formattedBuffer, start, len);
            formattedBuffer.setLength(start);
            if (lineBuffer.length() > 0) {
                setTempIndentWidth(lineBuffer);
            }
            spaceUtil.ltrimSpaces(lineBuffer);
        }
    }

    public void append(char c) {
        lineBuffer.append(c);
    }

    public void append(CharSequence s) {
        lineBuffer.append(s);
    }

    public void append(char[] a, int start, int len) {
        lineBuffer.append(a, start, len);
    }

    public void append(CharSequence b, int start, int end) {
        lineBuffer.append(b, start, end);
    }

    /**
     * Append space conditionally, append only if current content not ended with whitespace.
     */
    public boolean appendSpaceCond() {
        if (space(lineBuffer)) {
            return true;
        }
        return space(formattedBuffer);
    }

    public boolean space() {
        return space(lineBuffer);
    }

    public boolean space(StringBuffer buf) {
        int len = buf.length();
        if (len > 0 && !spaceUtil.isWhitespace(buf.charAt(len - 1))) {
            buf.append(' ');
            return true;
        }
        return false;
    }

    public void appendFormatted(char c) {
        if (lineBuffer.length() != 0) {
            flush();
        }
        formattedBuffer.append(c);
    }

    public void appendFormatted(char[] a, int start, int end) {
        if (lineBuffer.length() != 0) {
            flush();
        }
        formattedBuffer.append(a, start, end - start);
    }

    public void appendFormatted(CharSequence str) {
        appendFormatted(str, 0, str.length());
    }

    public void appendFormatted(CharSequence str, int start, int end) {
        if (lineBuffer.length() != 0) {
            flush();
        }
        formattedBuffer.append(str, start, end);
    }

    public boolean appendFormatted(FormatBuffer b, boolean oneliner) {
        return appendFormatted(b, oneliner, false, false, 0, 0);
    }

    /**
     * Append formatted content of given FormatBuffer.  Adjust indent to match indent
     * of the last line of the given buffer.
     *
     * @param oneliner   True to enable oneliner formatting.
     * @param twoliner   True to enable twoliner formatting.
     * @param trimbreaks True to eliminate line breaks that match positions in the break list
     *                   when converting to oneliner.
     * @param spacing    0=no leading space, 1=1 leading space, -1=0 if b has no leading space, 1 otherwise.
     * @param margin     Extra margin to add to oneliner when checking if it fit on current line.
     * @return true if oneliner is appended.
     */
    public boolean appendFormatted(
        FormatBuffer b, boolean oneliner, boolean twoliner, boolean trimbreaks, int spacing, int margin) {
        b.flush();
        StringBuffer buf = b.getFormatted();
        if (!oneliner) {
            flushLine();
            appendFormatted(buf);
            unshiftNonTerminatedLine();
            return false;
        }
        int len = b.oneLinerLength(trimbreaks, false) + margin;
        if (canFit(len)) {
            appendOneLiner(b, trimbreaks, spacing);
            return true;
        }
        flushLine();
        if (twoliner) {
            if (canFit(len)) {
                setTempIndentWidth(buf);
                appendOneLiner(b, trimbreaks, 0);
                return false;
            }
        }
        appendFormatted(buf);
        unshiftNonTerminatedLine();
        return false;
    }

    public int skipSpaces(int start) {
        return skipSpaces(start, lineBuffer.length());
    }

    ////////////////////////////////////////////////////////////////////////

    public int skipSpaces(int start, int end) {
        return spaceUtil.skipSpaces(lineBuffer, start, end);
    }

    private void appendOneLiner(FormatBuffer b, boolean trimbreaks, int spacing) {
        StringBuffer buf = b.toOneLiner(trimbreaks, spacing >= 0);
        if (spacing == 1) {
            appendSpaceCond();
        }
        append(buf, 0, buf.length());
    }

    public void setTempIndentWidth(CharSequence buf) {
        int indents = (determineIndentWidth(buf) / tabWidth) * tabWidth;
        setTempIndentWidth(indents);
    }

    /**
     * Set indent width for current line temporarily to given value and adjust indentAfterBreak
     * indent width to the current value.
     *
     * @param w Indent width rounded to multiple of fTabWidth.
     */
    public void setTempIndentWidth(int w) {
        int current = indentWidth;
        if (w > current) {
            while (w > current) {
                indent();
                unIndentAfterBreak();
                w -= tabWidth;
            }
        } else if (w < current) {
            while (w < current) {
                unIndent();
                indentAfterBreak();
                w += tabWidth;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Determine indent width of current line buffer.
     */
    private int determineIndentWidth(CharSequence buf) {
        int column = 0;
        char c;
        for (int i = 0, len = buf.length(); i < len; ++i) {
            c = buf.charAt(i);
            if (c == '\t') {
                column = column - (column % tabWidth) + tabWidth;
            } else if (c == ' ' || c == 0xa0) {
                ++column;
            } else {
                break;
            }
        }
        return column;
    }

    /**
     * Append given string, indent each lines properly.
     */
    public void indentLines(CharSequence s) {
        indentLines(s, 0, s.length());
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Append given string, indent each lines properly.
     */
    public void indentLines(CharSequence str, int start, int end) {
        int index = 0;
        do {
            index = indexOfAny(CRLF, str, start, end);
            if (index < 0) {
                append(str, start, end);
                break;
            }
            append(str, start, index);
            newLine();
            start = spaceUtil.skipSpaces(str, skipCRLF(str, index, end), end);
        } while (start < end);
    }

    /**
     * Append a block of indented text.
     * That is simply prefix lines in the block with current indent without trimming leading spaces.
     */
    public void appendBlock(CharSequence s) {
        appendBlock(s, 0, s.length());
    }

    ////////////////////////////////////////////////////////////////////////

    public void appendBlock(CharSequence s, int start, int end) {
        int linestart = spaceUtil.skipSpaces(lineBuffer, 0, lineBuffer.length());
        int index;
        do {
            index = indexOfAny(CRLF, s, start, end);
            if (index < 0) {
                append(s, start, end);
                break;
            }
            append(s, start, index);
            flush(linestart);
            linestart = 0;
            newLine();
            start = skipCRLF(s, index, end);
        } while (start < end);
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Append tab till insert position is beyond the given column.
     * If text of current line already exceed the given column,
     * make sure there is at least one trailing space.
     *
     * @param column 0-based column number.
     */
    public void tabToColumn(int column) {
        int col = getColumn();
        if (col >= column) {
            appendSpaceCond();
            return;
        }
        while (col < column) {
            int c = col - (col % tabWidth) + tabWidth;
            if (c <= column) {
                append(tabString);
                col = c;
            } else {
                appendSpaceCond();
            }
        }
    }

    public void indent() {
        indentString.append(tabString);
        indentWidth += tabWidth;
    }

    public void indent(int n) {
        while (--n >= 0) {
            indent();
        }
    }

    public void unIndent() {
        if (indentWidth < tabWidth) {
            String s = toString();
            throw new RuntimeException(
                "Cannot unindent before start of line: current indent="
                    + indentWidth
                    + ", fTabWidth="
                    + tabWidth
                    + ", buffer=\n"
                    + s
            );
        }
        indentString.setLength(indentString.length() - tabString.length());
        indentWidth -= tabWidth;
    }

    public void unIndent(int n) {
        while (--n >= 0) {
            unIndent();
        }
    }

    public String getIndent() {
        return indentString.toString();
    }

    public int getIndentWidth() {
        return indentWidth;
    }

    public void setIndentWidth(int w) {
        setIndentWidth(w, true);
    }

    public void setIndentWidth(int w, boolean reset_indent_onbreak) {
        if (reset_indent_onbreak) {
            indentsOnBreak = 0;
        }
        while (indentWidth < w) {
            indent();
        }
        while (indentWidth > w) {
            unIndent();
        }
    }

    public int getIndentLevel() {
        return indentWidth / tabWidth;
    }

    public void setIndentLevel(int n) {
        indentWidth = tabWidth * n;
        indentString.setLength(0);
        for (int i = 0; i < n; ++i) {
            indentString.append(tabString);
        }
    }

    public void indentAfterBreak() {
        ++indentsOnBreak;
    }

    public void unIndentAfterBreak() {
        --indentsOnBreak;
    }

    public int getIndentAfterBreak() {
        return indentsOnBreak;
    }

    public void setIndentAfterBreak(int n) {
        indentsOnBreak = n;
    }

    public void setIndentWidthAfterBreak(int w) {
        int indent = indentWidth;
        indentsOnBreak = 0;
        while (indent < w) {
            indentAfterBreak();
            indent += tabWidth;
        }
        while (indent > w) {
            unIndentAfterBreak();
            indent -= tabWidth;
        }
    }

    public void pushIndent() {
        indentStack.push(indentWidth);
    }

    public void pushIndents() {
        indentStack.push(indentsOnBreak);
        indentStack.push(indentWidth);
    }

    public void popIndents() {
        setIndentWidth(indentStack.pop());
        indentsOnBreak = indentStack.pop();
    }

    /**
     * Save the indent width expected after break (ie. current indent width + indents after break).
     */
    public void pushAfterBreakIndent() {
        indentStack.push(indentWidth + indentsOnBreak * tabWidth);
    }

    public void pushIndent(int w) {
        indentStack.push(w);
    }

    public int popIndent() {
        return indentStack.pop();
    }

    ////////////////////////////////////////////////////////////////////////

    public int peekIndent() {
        return indentStack.peek();
    }

    public void setFormattedBreak() {
        formattedBreaks.push(formattedBuffer.length());
    }

    public void resetFormattedBreaks(int n) {
        formattedBreaks.setLength(n);
    }

    /**
     * Set current length of line buffer as a break point.
     * Breaks are points that line break should be inserted when wrapping the line buffer.
     * Unlike, wrapping normal text, code formatting do not try to fit most text into a single line.
     * If content of line buffer exceeded line with, it would be desirable to break up the line
     * according to logical segments (eg. method parameters are broken up to separate lines).
     */
    public void setBreak() {
        int size = breakList.size();
        int len = lineBuffer.length();
        if (len == 0 || (size > 0 && breakList.get(size - 1) >= len)) {
            return;
        }
        breakList.push(len);
    }

    /**
     * Reset break list to given length and set break.
     * <li>
     * Typically, when formatting a line, the line is consists of a number of segments.
     * When building a segment, breaks are added as if the segment would exceed line width
     * and need to be broken up.  However, when a segment turns out to be short, it would be
     * better not to break up the segment.  In that case, all breaks for the segment should be
     * removed and set a break at end of the segment.
     * </li>
     * <li>
     * The mark stack is used for such purpose.  mark() save the length of the break list
     * when the segement starts.  mark=unmark() at retrieve the mark at when the segment ends.
     * If segment is short, resetBreak(mark) reset break list to state at start of segment
     * and add the end of segment as a new break.
     * </li>
     */
    public void resetBreak(int len) {
        breakList.setLength(len);
        setBreak();
    }

    /**
     * Reset breaks if line buffer content can be fit into one line else set break.
     *
     * @param len
     */
    public void resetBreaksIfFit(int len) {
        int linelen = lineBuffer.length();
        if (indentWidth + linelen - spaceUtil.skipSpaces(lineBuffer, 0, linelen) < lineWidth) {
            setBreak();
            return;
        }
        resetBreak(len);
    }

    /**
     * Mark current size in the break list.
     */
    public void mark() {
        setBreak();
        markStack.push(breakList.size());
    }

    public void unmark() {
        unmark(lineWidth);
    }

    public void unmark(int len) {
        int mark = markStack.pop();
        int start = breakList.get(mark - 1);
        if (lineBuffer.indexOf("\n", start) >= 0) {
            return;
        }
        if (lineBuffer.length() - start + indentWidth + tabWidth >= len) {
            return;
        }
        resetBreak(mark);
    }

    public int peekMark() {
        return markStack.peek();
    }

    /**
     * Flush and trim leading and trailing blank lines.
     */
    public void trimBlankLines() {
        trimTrailingBlankLines();
        trimLeadingBlankLines();
    }

    public void trimLeadingBlankLines() {
        flush();
        int start = 0;
        int len = formattedBuffer.length();
        char c;
        for (int i = 0; i < len; ++i) {
            c = formattedBuffer.charAt(i);
            if (!spaceUtil.isWhitespace(c)) {
                break;
            }
            if (c == '\n') {
                start = i + 1;
            }
        }
        if (start > 0) {
            formattedBuffer.delete(0, start);
        }
    }

    /**
     * Ensure there are given number of blank lines at end of current text
     * (unless current text is empty).
     */
    public void ensureBlankLines(int n) {
        if (n < 0) {
            return;
        }
        flushLine();
        if (formattedBuffer.length() == 0) {
            return;
        }
        for (int i = countTrailingBlankLines(); i < n; ++i) {
            newLine();
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public void adjustBlankLinesTo(int n) {
        if (n < 0) {
            return;
        }
        flushLine();
        trimTrailingBlankLines();
        for (int i = 0; i < n; ++i) {
            lineBreak();
        }
    }

    /**
     * Flush and line break.
     */
    public void newLine() {
        flush();
        lineBreak();
    }

    /**
     * Flush and line break if line is not ended with line break.
     */
    public void newLineCond() {
        flush();
        int len = formattedBuffer.length();
        if (len > 0 && formattedBuffer.charAt(len - 1) != '\n') {
            lineBreak();
        }
    }

    /**
     * Flush and line break if line buffer is not a blank line.
     */
    public void flushLine() {
        int start = 0;
        int end = lineBuffer.length();
        if (end > 0) {
            start = spaceUtil.skipSpaces(lineBuffer, start, end);
        }
        if (end > start) {
            flush();
            lineBreak();
        } else {
            breakList.clear();
            lineBuffer.setLength(0);
            adjustIndentsOnBreak();
        }
    }

    /**
     * Append String and flush line.
     */
    public void flushLine(char c) {
        append(c);
        flushLine();
    }

    /**
     * Append characters and flush line.
     */
    public void flushLine(char[] a, int start, int end) {
        lineBuffer.append(a, start, end - start);
        flushLine();
    }

    public void flushLine(CharSequence s) {
        append(s);
        flushLine();
    }

    /**
     * Flush line conditionally, if line (excluding leading spaces) is longer than given width.
     */
    public boolean flushLineCond(int width, boolean reset) {
        int len = lineBuffer.length();
        int start = 0;
        if (len > 0) {
            start = spaceUtil.skipSpaces(lineBuffer, 0, len);
        }
        if (start < len && (indentWidth + len - start > width)) {
            newLine();
            return true;
        }
        if (reset) {
            resetBreak(0);
        }
        return false;
    }

    /**
     * Wrap line and flush line buffer content to text buffer without adding trailing line break.
     * <li>
     * If line is shorter than line with, the line is just append to the text buffer.
     * If line is longer than line with, the line is broken up according to the break list.
     * A new line is added at each break point.
     * </li>
     */
    public void flush() {
        int linelen = lineBuffer.length();
        if (linelen == 0) {
            breakList.clear();
            return;
        }
        int start = spaceUtil.skipSpaces(lineBuffer, 0, linelen);
        if (start >= lineBuffer.length()) {
            lineBuffer.setLength(0);
            breakList.clear();
            return;
        }
        flush(start);
    }

    ////////////////////////////////////////////////////////////////////////

    public void flush(int start) {
        int linelen = lineBuffer.length();
        if (indentWidth + linelen - start <= lineWidth) {
            formattedBuffer.append(indentString);
            formattedBuffer.append(lineBuffer.substring(start, linelen));
            lineBuffer.setLength(0);
            breakList.clear();
            return;
        }
        int i = 0;
        boolean isfirst = true;
        String line = lineBuffer.toString();
        for (; i < breakList.size(); ++i) {
            int x = breakList.get(i);
            if (start < x) {
                formattedBuffer.append(indentString);
                if (!isfirst) {
                    formattedBuffer.append(tabString);
                }
                isfirst = false;
                formattedBuffer.append(line.substring(start, x));
                start = spaceUtil.skipSpaces(line, x, linelen);
                if (start < linelen) {
                    lineBreak();
                } else {
                    break;
                }
            }
        }
        if (start < linelen) {
            formattedBuffer.append(indentString);
            if (!isfirst) {
                formattedBuffer.append(tabString);
            }
            formattedBuffer.append(line.substring(start, linelen));
        }
        lineBuffer.setLength(0);
        breakList.clear();
    }

    private final void lineBreak() {
        formattedBuffer.append(lineBreak);
        adjustIndentsOnBreak();
    }

    private final void adjustIndentsOnBreak() {
        while (indentsOnBreak > 0) {
            indent();
            --indentsOnBreak;
        }
        while (indentsOnBreak < 0) {
            unIndent();
            ++indentsOnBreak;
        }
    }

    public static int indexOfAny(final char[] delims, final CharSequence str) {
        char c;
        for (int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);
            for (int k = 0; k < delims.length; ++k) {
                if (c == delims[k]) {
                    return i;
                }}}
        return -1;
    }

    public static int indexOfAny(final char[] delims, final CharSequence str, final int start, final int end) {
        char c;
        for (int i = start; i < end; ++i) {
            c = str.charAt(i);
            for (int k = 0; k < delims.length; ++k) {
                if (c == delims[k]) {
                    return i;
                }}}
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////
}
