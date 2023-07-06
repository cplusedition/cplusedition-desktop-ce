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
package sf.llk.share.formatter;

import java.util.List;

import sf.llk.share.support.IIntList;
import sf.llk.share.support.IntList;
import sf.llk.share.util.AbstractSpaceUtil;
import sf.llk.share.util.DefaultSpaceUtil;
import sf.llk.share.util.ISpaceUtil;
import sf.llk.share.util.LLKShareUtil;

/**
 * Text buffer for text formatted.
 *
 * <li>FormatBuilder worked as a line buffer which holds text to be formatted, and backed by a
 * formatted buffer which holds formatted text.</li>
 * <li>Client append text to the buffer then call flushLine() when a hard line break is required.</li>
 * <li>Breaks and soft breaks in FormatBuilder works differently from that in FormatBuffer.</li>
 * <li>FormatBuilder breaks are points that line break that can be inserted when line is (flush and) wrapped.
 * Whitespaces are automatic break points.</li>
 * <li>FormatBuilder soft breaks are start of whitespace segment in the formatted buffer that can be completely
 * removed when the formatted buffer is merged into single line.</li>
 *
 * @author chrisl
 */
public class FormatBuilder {

    ////////////////////////////////////////////////////////////////////////

    public static final int DEF_LINEBUF_SIZE = 128;
    public static final int DEF_FORMATTED_SIZE = 256;
    public static final int DEF_LINEWIDTH = 80;
    public static final int DEF_TABWIDTH = 4;

    public static class DefaultOnelinerFormatter implements IOnelinerFormatter {
        private static DefaultOnelinerFormatter singleton;
        public static DefaultOnelinerFormatter getSingleton() {
            if (singleton == null) {
                singleton = new DefaultOnelinerFormatter();
            }
            return singleton;
        }
        @Override
        public String toOneliner(CharSequence s, int start, int end, boolean wasspace, int columns, IIntList breaks) {
            ISpaceUtil spaceutil = DefaultSpaceUtil.getSingleton();
            StringBuilder ret = new StringBuilder();
            char c;
            for (; start < end; ++start) {
                c = s.charAt(start);
                if (spaceutil.isWhitespace(c)) {
                    if (breaks != null && OnelinerUtil.isSoftBreak(start, breaks)) {
                        start = spaceutil.skipSpaces(s, start + 1, end) - 1;
                        continue;
                    }
                    if (wasspace) {
                        continue;
                    }
                    wasspace = true;
                    ret.append(' ');
                    continue;
                }
                wasspace = false;
                ret.append(c);
                if (ret.length() > columns) {
                    return null;
                }
            }
            return ret.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////

    protected int lineWidth;
    protected int tabWidth;
    protected int indentWidth;
    protected String lineBreak;
    protected String tabString;
    protected StringBuilder indentString;
    protected StringBuilder lineBuffer;
    protected StringBuilder formattedBuffer;
    protected IIntList breakList;
    protected IIntList noBreakList;
    protected IIntList softBreakList;
    protected int indentsOnBreak;
    protected boolean trimTrailingSpaces;
    protected boolean onelinerOK;
    protected IOnelinerFormatter onelinerFormatter;
    protected ISpaceUtil spaceUtil;
    protected boolean destroyed = false;

    ////////////////////////////////////////////////////////////////////////

    public FormatBuilder() {
        this(DEF_LINEWIDTH, DEF_TABWIDTH, "", "\t");
    }

    public FormatBuilder(int linewidth, int tabwidth) {
        this(linewidth, tabwidth, "", "\t");
    }

    /**
     * @deprecated Use FormatBuilder(int tabwidth, String indent, String tab) instead.
     */
    @Deprecated
    public FormatBuilder(String indent, String tab, int tabwidth) {
        this(DEF_LINEWIDTH, tabwidth, indent, tab);
    }

    public FormatBuilder(int tabwidth, String indent, String tab) {
        this(DEF_LINEWIDTH, tabwidth, indent, tab);
    }

    public FormatBuilder(int width, int tabwidth, String indent, String tab) {
        this(width, tabwidth, indent, tab, LLKShareUtil.LB);
    }

    public FormatBuilder(int width, int tabwidth, String indent, String tab, String linebreak) {
        this(
            width,
            tabwidth,
            indent,
            tab,
            linebreak,
            DefaultOnelinerFormatter.getSingleton(),
            DefaultSpaceUtil.getSingleton());
    }

    public FormatBuilder(
        int width,
        int tabwidth,
        String indent,
        String tab,
        String linebreak,
        IOnelinerFormatter oneliner,
        ISpaceUtil spaceutil) {
        createBuffers();
        indentString.append(indent);
        lineWidth = width;
        tabWidth = tabwidth;
        tabString = tab;
        lineBreak = linebreak;
        trimTrailingSpaces = true;
        onelinerOK = true;
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
        onelinerFormatter = oneliner;
        spaceUtil = spaceutil;
    }

    private void createBuffers() {
        indentString = new StringBuilder();
        lineBuffer = new StringBuilder(DEF_LINEBUF_SIZE);
        formattedBuffer = new StringBuilder(DEF_FORMATTED_SIZE);
        breakList = new IntList();
        noBreakList = new IntList();
        softBreakList = new IntList();
    }

    /**
     * Create a new instance with parameters, but not content, of the given buffer.
     */
    public FormatBuilder(FormatBuilder a) {
        createBuffers();
        init(a);
    }

    /**
     * Init this to the given buffer's state.
     * This is usually used to init. state of lookahead buffers.
     */
    public void init(FormatBuilder b) {
        clear();
        indentString.append(b.indentString);
        lineWidth = b.lineWidth;
        tabWidth = b.tabWidth;
        indentWidth = b.indentWidth;
        lineBreak = b.lineBreak;
        tabString = b.tabString;
        indentsOnBreak = b.indentsOnBreak;
        trimTrailingSpaces = b.trimTrailingSpaces;
        onelinerOK = b.onelinerOK;
        onelinerFormatter = b.onelinerFormatter;
        spaceUtil = b.spaceUtil;
    }

    public void destroy() {
        destroyed = true;
    }

    public FormatBuilder(FormatBuffer a) {
        createBuffers();
        init(a);
    }

    /**
     * Init this to the given buffer's state.
     * This is usually used to init. state of lookahead buffers.
     */
    public void init(FormatBuffer b) {
        clear();
        indentString.append(b.getIndent());
        lineWidth = b.getLineWidth();
        tabWidth = b.getTabWidth();
        indentWidth = b.getIndentWidth();
        lineBreak = b.getLineBreak();
        tabString = b.getTab();
        indentsOnBreak = b.getIndentAfterBreak();
        trimTrailingSpaces = true;
        onelinerOK = true;
        onelinerFormatter = DefaultOnelinerFormatter.getSingleton();
        spaceUtil = DefaultSpaceUtil.getSingleton();
    }

    ////////////////////////////////////////////////////////////////////////

    private void clear() {
        indentString.setLength(0);
        lineBuffer.setLength(0);
        formattedBuffer.setLength(0);
        breakList.clear();
        noBreakList.clear();
        softBreakList.clear();
        indentsOnBreak = 0;
        destroyed = false;
    }

    public String getLineBreak() {
        return lineBreak;
    }

    public String getTabString() {
        return tabString;
    }

    public IIntList getBreaks() {
        return breakList;
    }

    public IIntList getNoBreaks() {
        return noBreakList;
    }

    public IIntList getSoftBreaks() {
        return softBreakList;
    }

    public void setTrimTrailingSpaces(boolean b) {
        trimTrailingSpaces = b;
    }

    public boolean isTrimTrailingSpaces() {
        return trimTrailingSpaces;
    }

    public void setOnelinerOK(boolean b) {
        onelinerOK = b;
    }

    public boolean isOnelinerOK() {
        return onelinerOK;
    }

    public void setOnelinerFormatter(IOnelinerFormatter formatter) {
        this.onelinerFormatter = formatter;
    }

    public IOnelinerFormatter getOnelinerFormatter() {
        return onelinerFormatter;
    }

    public void setSpaceUtil(AbstractSpaceUtil util) {
        this.spaceUtil = util;
    }

    public ISpaceUtil getSpaceUtil() {
        return spaceUtil;
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
        formattedBuffer.append(lineBuffer, linestart, lineend);
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
        StringBuilder ret = new StringBuilder();
        ret.append(formattedBuffer);
        if (spaceUtil.endsWithLineBreak(ret)) {
            ret.append(indentString);
            int linestart = spaceUtil.skipSpaces(lineBuffer, 0, lineend);
            ret.append(lineBuffer, linestart, lineend);
        } else {
            ret.append(lineBuffer);
        }
        return ret.toString();
    }

    public boolean ltrimSpaces() {
        boolean ret = spaceUtil.ltrimSpaces(formattedBuffer);
        if (formattedBuffer.length() == 0) {
            ret |= spaceUtil.ltrimSpaces(lineBuffer);
        }
        return ret;
    }

    public boolean ltrimWhitespaces() {
        boolean ret = spaceUtil.ltrimWhitespaces(formattedBuffer);
        if (formattedBuffer.length() == 0) {
            ret |= spaceUtil.ltrimWhitespaces(lineBuffer);
        }
        return ret;
    }

    /**
     * Trim trailing space characters in line buffer.
     */
    public boolean rtrimSpaces() {
        return spaceUtil.rtrimSpaces(lineBuffer);
    }

    /**
     * Trim trailing whitespace characters in line/text buffer (including line breaks).
     */
    public boolean rtrimWhitespaces() {
        boolean ret = spaceUtil.rtrimWhitespaces(lineBuffer);
        if (lineBuffer.length() == 0) {
            ret |= spaceUtil.rtrimWhitespaces(formattedBuffer);
            trimBreaks();
            unshiftNonTerminatedLine();
        }
        return ret;
    }

    public int rtrimLineBreaks() {
        int ret = 0;
        int e;
        int end = lineBuffer.length();
        while (end > 0 && (e = spaceUtil.rskipLineBreak(lineBuffer, 0, end)) != end) {
            end = e;
            ++ret;
        }
        lineBuffer.setLength(end);
        if (end == 0) {
            ret += spaceUtil.rtrimLineBreaks(formattedBuffer);
            trimBreaks();
        }
        return ret;
    }

    public int rtrimBlankLines() {
        if (!isBlank()) {
            return -1;
        }
        flush();
        int ret = spaceUtil.rtrimBlankLines(formattedBuffer);
        trimBreaks();
        return ret;
    }

    /**
     * @return true if line/formatted buffer ends with a line break
     * (with optionally some trailing whitespaces).
     */
    public boolean endsWithLineBreak() {
        int end = spaceUtil.rskipSpaces(lineBuffer, 0, lineBuffer.length());
        if (end != 0) {
            return spaceUtil.rskipLineBreak(lineBuffer, 0, end) != end;
        }
        return spaceUtil.endsWithLineBreak(formattedBuffer);
    }

    /**
     * @return The number of blank lines (ie. numer of line breaks-1) at end of text.
     */
    public int countTrailingBlankLines() {
        int ret = -1;
        int len = lineBuffer.length();
        int end = spaceUtil.rskipSpaces(lineBuffer, 0, len);
        int e;
        while (end > 0 && (e = spaceUtil.rskipLineBreak(lineBuffer, 0, end)) != end) {
            end = spaceUtil.rskipSpaces(lineBuffer, 0, e);
            ++ret;
        }
        if (end == 0) {
            len = formattedBuffer.length();
            end = spaceUtil.rskipSpaces(formattedBuffer, 0, len);
            while (end > 0 && (e = spaceUtil.rskipLineBreak(formattedBuffer, 0, end)) != end) {
                end = spaceUtil.rskipSpaces(formattedBuffer, 0, e);
                ++ret;
            }
        }
        return ret;
    }

    public boolean endsWithSpaces() {
        if (isBlank()) {
            return indentWidth != 0;
        }
        return spaceUtil.isSpace(lineBuffer.charAt(lineBuffer.length() - 1));
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

    public StringBuilder getLineBuffer() {
        return lineBuffer;
    }

    /**
     * @return The formatted text buffer.
     */
    public StringBuilder getFormatted() {
        if (destroyed) {
            throw new AssertionError("Instance is already destroyed");
        }
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

    public int getTabWidth() {
        return tabWidth;
    }

    public String getTab() {
        return tabString;
    }

    public void setLineWidth(int linewidth) {
        lineWidth = linewidth;
    }

    /**
     * @column 0-based column number.
     */
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
        end = spaceUtil.rskipWhitespaces(s, start, end);
        if (start >= end) {
            return true;
        }
        return spaceUtil.columnOf(s, start, end, getColumn(), tabWidth) < lineWidth - margin;
    }

    /**
     * Convert given string to a oneliner.
     */
    public CharSequence getOneLiner(CharSequence s, boolean trimbreaks, boolean wasspace) {
        return getOneLiner(s, 0, s.length(), trimbreaks, wasspace);
    }

    public CharSequence getOneLiner(CharSequence s, int start, int end, boolean trimbreaks, boolean wasspace) {
        end = spaceUtil.rskipWhitespaces(s, start, end);
        if (end == start) {
            return "";
        }
        return onelinerFormatter.toOneliner(
            s, start, end, wasspace, getLineWidth() - getIndentWidth(), (trimbreaks ? softBreakList : null));
    }

    ////////////////////////////////////////////////////////////////////////

    public void unshiftNonTerminatedLine() {
        if (lineBuffer.length() != 0) {
            throw new RuntimeException("Line buffer not empty: line=" + lineBuffer.toString());
        }
        int len = formattedBuffer.length();
        if (spaceUtil.rskipLineBreak(formattedBuffer, 0, len) == len) {
            int start = spaceUtil.rskipLine(formattedBuffer, 0, len);
            append(formattedBuffer, start, len);
            formattedBuffer.setLength(start);
            if (lineBuffer.length() > 0) {
                setTempIndentWidth(lineBuffer);
            }
            spaceUtil.ltrimSpaces(lineBuffer);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public void append(char c) {
        lineBuffer.append(c);
    }

    public void append(CharSequence s) {
        lineBuffer.append(s);
    }

    public void append(char[] a) {
        lineBuffer.append(a, 0, a.length);
    }

    public void append(char[] a, int start, int len) {
        lineBuffer.append(a, start, len);
    }

    public void append(CharSequence b, int start, int end) {
        lineBuffer.append(b, start, end);
    }

    public boolean space() {
        int len = lineBuffer.length();
        if (len == 0 || !spaceUtil.isSpace(lineBuffer.charAt(len - 1))) {
            lineBuffer.append(' ');
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

    public void appendFormatted(CharSequence str) {
        appendFormatted(str, 0, str.length());
    }

    public void appendFormatted(char[] a, int start, int end) {
        if (lineBuffer.length() != 0) {
            flush();
        }
        formattedBuffer.append(a, start, end - start);
    }

    public void appendFormatted(CharSequence str, int start, int end) {
        if (lineBuffer.length() != 0) {
            flush();
        }
        formattedBuffer.append(str, start, end);
    }

    public boolean appendFormatted(FormatBuilder b, boolean oneliner, boolean wasspace) {
        return appendFormatted(b, oneliner, false, true, wasspace, 0);
    }

    /**
     * Append formatted content of given FormatBuilder.  Adjust indent to match indent
     * of the last line of the given buffer.
     *
     * @param oneliner   True to enable oneliner formatting.
     * @param twoliner   True to enable twoliner formatting.
     * @param trimbreaks True to eliminate line breaks that match positions in the break list
     *                   when converting to oneliner.
     * @param wasspace
     * @param margin     Extra margin to add to oneliner when checking if it fit on current line.
     * @return true if oneliner is appended.
     */
    public boolean appendFormatted(
        FormatBuilder b, boolean oneliner, boolean twoliner, boolean trimbreaks, boolean wasspace, int margin) {
        b.flush();
        StringBuilder formatted = b.getFormatted();
        if (twoliner) {
            oneliner = true;
        }
        if (oneliner) {
            CharSequence line = b.getOneLiner(b.getFormatted(), trimbreaks, wasspace);
            if (line != null) {
                if (canFit(line, margin)) {
                    appendFormattedOneLiner(formatted, line);
                    return true;
                }
                flushLine();
                if (twoliner) {
                    if (canFit(line, 0)) {
                        setTempIndentWidth(formatted);
                        appendFormattedOneLiner(formatted, line);
                        return false;
                    }
                }
            }
        }
        appendFormatted(formatted);
        unshiftNonTerminatedLine();
        return false;
    }

    public void appendFormattedOneLiner(CharSequence formatted, boolean trimbreaks, boolean wasspace) {
        flush();
        CharSequence oneliner = getOneLiner(formatted, trimbreaks, wasspace);
        appendFormattedOneLiner(formatted, oneliner);
    }

    public void appendFormattedOneLiner(CharSequence formatted, CharSequence oneliner) {
        if (oneliner.length() == 0) {
            return;
        }
        int breaks = spaceUtil.rcountLineBreaks(formatted);
        if (!isBlank()) {
            append(oneliner);
            if (breaks > 0) {
                flushLine();
            }
            while (--breaks > 0) {
                lineBreak();
            }
            return;
        }
        if (length() == 0) {
            formattedBuffer.append(formatted, 0, spaceUtil.skipSpaces(formatted, 0, formatted.length()));
            formattedBuffer.append(oneliner);
            if (breaks > 0) {
                while (--breaks >= 0) {
                    lineBreak();
                }
            } else {
                unshiftNonTerminatedLine();
            }
        } else {
            flushLine(oneliner);
        }
    }

    public int skipSpaces(int start) {
        return spaceUtil.skipSpaces(lineBuffer, start, lineBuffer.length());
    }

    public int skipSpaces(int start, int end) {
        return spaceUtil.skipSpaces(lineBuffer, start, end);
    }

    ////////////////////////////////////////////////////////////////////////

    public void setTempIndentWidth(CharSequence buf) {
        int indents = (indentWidthOf(buf, 0) / tabWidth) * tabWidth;
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

    /**
     * Determine indent width of the given string.
     *
     * @return 0-based column number.
     */
    public int indentWidthOf(CharSequence buf, int start) {
        int column = 0;
        char c;
        for (int len = buf.length(); start < len; ++start) {
            c = buf.charAt(start);
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
     * Determine column width of given string.
     *
     * @return 0-based column number.
     */
    public int columnOf(CharSequence buf, int start, int end) {
        return spaceUtil.columnOf(buf, start, end, getColumn(), tabWidth);
    }

    /**
     * Determine column width of given string.
     *
     * @return 0-based column number.
     */
    public int columnOf(CharSequence buf, int start, int end, int column) {
        return spaceUtil.columnOf(buf, start, end, column, tabWidth);
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Append given string, indent each lines properly.
     */
    public void indentLines(CharSequence s) {
        indentLines(s, 0, s.length());
    }

    /**
     * Append given string, indent each lines properly.
     */
    public void indentLines(CharSequence str, int start, int end) {
        int index = 0;
        do {
            index = spaceUtil.skipLine(str, start, end);
            if (index < 0) {
                append(str, spaceUtil.skipSpaces(str, start, end), end);
                break;
            }
            append(str, start, index);
            newLine();
            start = spaceUtil.skipSpaces(str, spaceUtil.skipLineBreak(str, index, end), end);
        } while (start < end);
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Append a block of indented text.
     * That is simply prefix lines in the block with current indent without trimming leading spaces.
     */
    public void appendBlock(CharSequence s) {
        appendBlock(s, 0, s.length());
    }

    public void appendBlock(CharSequence s, int start, int end) {
        int linestart = spaceUtil.skipSpaces(lineBuffer, 0, lineBuffer.length());
        int index;
        do {
            index = spaceUtil.skipLine(s, start, end);
            if (index < 0) {
                append(s, start, end);
                break;
            }
            append(s, start, index);
            flush(linestart);
            linestart = 0;
            newLine();
            start = spaceUtil.skipLineBreak(s, index, end);
        } while (start < end);
    }

    public void unIndentBlock(StringBuilder s) {
        List<String> lines = spaceUtil.splitLines(s);
        int min = Integer.MAX_VALUE;
        IIntList indents = new IntList();
        for (String line : lines) {
            int len = line.length();
            if (len == 0 || spaceUtil.skipSpaces(line, 0, len) == len) {
                indents.add(0);
                continue;
            }
            int indent = indentWidthOf(line, 0);
            indents.add(indent);
            if (indent < min) {
                min = indent;
            }
        }
        s.setLength(0);
        for (int n = 0; n < lines.size(); ++n) {
            String line = lines.get(n);
            int indent = (indents.get(n) - min) / tabWidth;
            for (; indent > 0; --indent) {
                s.append(tabString);
            }
            int len = line.length();
            s.append(line, spaceUtil.skipSpaces(line, 0, len), len);
            s.append(lineBreak);
        }
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
            space();
            return;
        }
        while (col < column) {
            int c = col - (col % tabWidth) + tabWidth;
            if (c <= column) {
                append(tabString);
                col = c;
            } else {
                space();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public void indent() {
        indentString.append(tabString);
        indentWidth += tabWidth;
    }

    public void indent(int n) {
        while (--n > 0) {
            indent();
        }
    }

    public void unIndent() {
        if (indentWidth < tabWidth || indentString.length() < tabString.length()) {
            String s = toString();
            System.err.println(
                "Cannot unindent before start of line: current indent="
                    + indentWidth
                    + ", fTabWidth="
                    + tabWidth
                    + ", buffer=\n"
                    + s);
            indentString.setLength(0);
            indentWidth = 0;
        } else {
            indentString.setLength(indentString.length() - tabString.length());
            indentWidth -= tabWidth;
        }
    }

    public void unIndent(int n) {
        while (--n > 0) {
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

    ////////////////////////////////////////////////////////////////////////

    public void setSoftBreak() {
        softBreakList.add(formattedBuffer.length());
    }

    public void setNoBreak() {
        int len = lineBuffer.length();
        if (len == 0 || (!noBreakList.isEmpty() && noBreakList.peek() >= len)) {
            return;
        }
        noBreakList.add(len);
    }

    public boolean isNoBreak(int offset) {
        return noBreakList.binarySearch(offset) >= 0;
    }

    /**
     * @param offset Line buffer offset.
     */
    public void insertNoBreak(int offset) {
        int last = -1;
        if (noBreakList.isEmpty() || offset > (last = noBreakList.peek())) {
            noBreakList.add(offset);
            return;
        }
        if (offset == last) {
            return;
        }
        int index = noBreakList.binarySearch(offset);
        if (index < 0) {
            noBreakList.insert(-index - 1, offset);
        }
    }

    public void clearNoBreaks() {
        noBreakList.clear();
    }

    /**
     * Set current length of line buffer as a break point.
     * Breaks are points that line break should be inserted when wrapping the line buffer.
     * Unlike, wrapping normal text, code formatting do not try to fit most text into a single line.
     * If content of line buffer exceeded line with, it would be desirable to break up the line
     * according to logical segments (eg. method parameters are broken up to separate lines).
     */
    public void setBreak() {
        int len = lineBuffer.length();
        if (len == 0 || (!breakList.isEmpty() && breakList.peek() >= len)) {
            return;
        }
        breakList.push(len);
    }

    public boolean isBreak(int offset) {
        return breakList.binarySearch(offset) >= 0;
    }

    /**
     * @param offset Line buffer offset.
     */
    public void insertBreak(int offset) {
        int last = -1;
        if (breakList.isEmpty() || offset > (last = breakList.peek())) {
            breakList.add(offset);
            return;
        }
        if (offset == last) {
            return;
        }
        int index = breakList.binarySearch(offset);
        if (index < 0) {
            breakList.insert(-index - 1, offset);
        }
    }

    public void clearBreaks() {
        breakList.clear();
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

    public void adjustBlankLinesTo(int n) {
        if (n < 0) {
            return;
        }
        flushLine();
        rtrimBlankLines();
        for (int i = 0; i < n; ++i) {
            lineBreak();
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Flush and line break.
     */
    public void newLine() {
        newLine(false);
    }

    public void newLine(boolean softbreak) {
        flush(softbreak);
        if (trimTrailingSpaces) {
            spaceUtil.rtrimSpaces(formattedBuffer);
        }
        lineBreak(softbreak);
    }

    /**
     * Flush and line break if line is not ended with line break.
     */
    public void newLineCond() {
        flush();
        int len = formattedBuffer.length();
        if (len > 0 && spaceUtil.rskipLineBreak(formattedBuffer, 0, len) == len) {
            lineBreak();
        }
    }

    /**
     * Flush and line break if line buffer is not a blank line.
     */
    public boolean flushLine() {
        return flushLine(false);
    }

    public boolean flushLine(boolean softbreak) {
        int len = lineBuffer.length();
        if (len == 0) {
            return false;
        }
        spaceUtil.ltrimSpaces(lineBuffer, 0, len);
        if (lineBuffer.length() > 0) {
            newLine(softbreak);
            return true;
        }
        adjustIndentsOnBreak();
        return false;
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
    public boolean flushLineCond(int width) {
        int len = lineBuffer.length();
        if (len == 0) {
            return false;
        }
        int start = spaceUtil.skipSpaces(lineBuffer, 0, len);
        if (start < len && (indentWidth + len - start > width)) {
            newLine();
            return true;
        }
        return false;
    }

    public void wrap() {
        wrap(null, null, false);
    }

    public void wrap(char[] before, char[] after) {
        wrap(before, after, false);
    }

    public void wrap(char[] before, char[] after, boolean softbreak) {
        int end = lineBuffer.length();
        if (end == 0) {
            return;
        }
        int start = spaceUtil.skipSpaces(lineBuffer, 0, end);
        if (start >= end) {
            clearLineBuffer();
            return;
        }
        if (before != null || after != null) {
            insertBreaks(start, end, before, after);
        }
        flush(start, false);
        lineBreak(softbreak);
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
        flush(false);
    }

    public void flush(boolean softbreak) {
        if (destroyed) {
            throw new AssertionError("Instance is already destroyed");
        }
        int end = lineBuffer.length();
        if (end == 0) {
            return;
        }
        int start = spaceUtil.skipSpaces(lineBuffer, 0, end);
        if (start >= end) {
            clearLineBuffer();
            return;
        }
        flush(start, softbreak);
    }

    public void flush(int start) {
        flush(start, false);
    }

    public void flush(int start, boolean softbreak) {
        int end = lineBuffer.length();
        if (breakList.size() == 0 || canFit(0)) {
            formattedBuffer.append(indentString);
            formattedBuffer.append(lineBuffer, start, end);
            clearLineBuffer();
            return;
        }
        insertBreak(end);
        int column = indentWidth;
        int prev = start;
        int x = start;
        for (int i = 0, size = breakList.size(); i < size; ++i) {
            x = breakList.get(i);
            if (x <= start) {
                continue;
            }
            column = columnOf(lineBuffer, prev, x, column);
            if (column >= lineWidth) {
                boolean notempty = prev > start;
                if (notempty) {
                    formattedBuffer.append(indentString);
                    formattedBuffer.append(lineBuffer, start, prev);
                }
                start = spaceUtil.skipSpaces(lineBuffer, prev, x);
                if (start >= end) {
                    break;
                }
                if (notempty) {
                    lineBreak(softbreak);
                }
                column = columnOf(lineBuffer, start, x, indentWidth);
            }
            prev = x;
            if (x >= end) {
                break;
            }
        }
        if (start < end) {
            formattedBuffer.append(indentString);
            formattedBuffer.append(lineBuffer, start, end);
        }
        clearLineBuffer();
    }

    public void insertBreaks(int start, int end, char[] before, char[] after) {
        int prev = -1;
        int c = -1;
        for (; start < end; prev = c, ++start) {
            if (after != null && prev >= 0) {
                for (char d : after) {
                    if (prev == d) {
                        insertBreak(start);
                        break;
                    }
                }
            }
            c = lineBuffer.charAt(start);
            if (before != null) {
                for (char d : before) {
                    if (c == d) {
                        insertBreak(start);
                        break;
                    }
                }
            }
        }
    }

    public void breakWhitespaces(int start, int end) {
        boolean wasspace = false;
        for (char c; start < end; ++start) {
            c = lineBuffer.charAt(start);
            if (Character.isWhitespace(c)) {
                if (wasspace) {
                    continue;
                }
                int index = breakList.binarySearch(start);
                if (index < 0) {
                    breakList.insert(-index - 1, start);
                }
                wasspace = true;
                continue;
            }
            wasspace = false;
        }
    }

    ////////////////////////////////////////////////////////////////////////

    private void lineBreak(boolean softbreak) {
        if (softbreak) {
            setSoftBreak();
        }
        lineBreak();
    }

    private void lineBreak() {
        formattedBuffer.append(lineBreak);
        adjustIndentsOnBreak();
    }

    private void adjustIndentsOnBreak() {
        while (indentsOnBreak > 0) {
            indent();
            --indentsOnBreak;
        }
        while (indentsOnBreak < 0) {
            unIndent();
            ++indentsOnBreak;
        }
    }

    private void trimBreaks() {
        int len = formattedBuffer.length();
        while (!softBreakList.isEmpty() && softBreakList.peek() > len) {
            softBreakList.pop();
        }
    }

    private void clearLineBuffer() {
        lineBuffer.setLength(0);
        breakList.clear();
        noBreakList.clear();
    }

    ////////////////////////////////////////////////////////////////////////
}
