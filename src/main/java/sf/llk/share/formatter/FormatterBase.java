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

import sf.llk.share.support.CharRange;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.IReadOnlyLocator;
import sf.llk.share.util.ISpaceUtil;
import sf.llk.share.util.LLKShareUtil;
import sf.llk.share.util.ObjectPool;

public abstract class FormatterBase {

    //////////////////////////////////////////////////////////////////////

    protected static final CharSequence EMPTY_CHARSEQ = new CharRange(new char[0]);
    protected static final int IS_SPACES = 0x01;
    protected static final int IS_WHITESPACES = 0x03;

    //////////////////////////////////////////////////////////////////////

    protected int startOffset;
    protected int endOffset;
    protected int macroEndOffset;
    protected ObjectPool<FormatBuilder> bufferPool = new ObjectPool<FormatBuilder>(FormatBuilder.class, 5);
    private int typeSPACES;
    private int typeNEWLINE;

    //////////////////////////////////////////////////////////////////////

    /**
     * Emit non-pure whitespace special tokens.
     */
    protected abstract void emitSpecial(FormatBuilder buf, ILLKToken t);
    /**
     * @return The end of line token emitted (eg, CPPCOMMENT or NEWLINE),
     * null if no line terminating special token emitted or line break is synthetic.
     */
    protected abstract ILLKToken emitRestOfLine(FormatBuilder buf, ILLKToken t, boolean softbreak);

    //////////////////////////////////////////////////////////////////////

    public FormatterBase() {
    }

    protected void init(int spaces, int newline) {
        init(0, Integer.MAX_VALUE, spaces, newline);
    }

    protected void init(int start, int end, int spaces, int newline) {
        this.startOffset = start;
        this.endOffset = end;
        this.typeSPACES = spaces;
        this.typeNEWLINE = newline;
    }

    //////////////////////////////////////////////////////////////////////

    public int getOffset() {
        return startOffset;
    }

    public void setOffset(int start) {
        startOffset = start;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int end) {
        endOffset = end;
    }

    /**
     * Skip to end of the given token if it is beyond current offset.
     */
    public void skipToken(ILLKToken lt1) {
        skip(lt1.getEndOffset());
    }

    public void skip(int offset) {
        if (offset > startOffset) {
            startOffset = offset;
        }
    }

    //////////////////////////////////////////////////////////////////////

    protected FormatBuilder cloneBuf(FormatBuilder buf) {
        FormatBuilder ret = new FormatBuilder(buf);
        StringBuilder line = buf.getLineBuffer();
        ret.append(line);
        line.setLength(0);
        return ret;
    }

    protected FormatBuilder getBuf(FormatBuilder buf) {
        return new FormatBuilder(buf);
    }

    ////////////////////////////////////////////////////////////////////////

    protected CharSequence getSpecial(ILLKToken t) {
        StringBuilder b = null;
        for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext()) {
            int offset = s.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset >= startOffset) {
                if (b == null) {
                    b = new StringBuilder();
                }
                b.append(s.getText());
            }
        }
        if (b == null) {
            return EMPTY_CHARSEQ;
        }
        return b;
    }

    protected void getSpecial(StringBuilder buf, ILLKToken t) {
        if (t == null) {
            return;
        }
        getSpecial(buf, t.getSpecial(), endOffset);
    }

    protected void getSpecial(StringBuilder buf, ILLKToken s, int end) {
        if (s == null) {
            return;
        }
        for (; s != null; s = s.getNext()) {
            int offset = s.getOffset();
            if (offset >= end) {
                break;
            }
            if (offset >= startOffset) {
                buf.append(s.getText());
            }
        }
    }

    protected StringBuilder getTokenText(ILLKToken start, ILLKToken end) {
        StringBuilder b = new StringBuilder();
        for (ILLKToken t = start; t != null && t != end; t = t.getNext()) {
            if (t != start) {
                getSpecial(b, t);
            }
            b.append(t.getText());
        }
        return b;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Emit special tokens before the given token with special handling for pure whitespaces.
     *
     * @param spaces If specials are pure whitespace, remove any existing trailing spaces and
     *               force to the given number of spaces.  If specials are not pure whitespaces,
     *               works as emitSpecial(buf, t, spaces, 0).
     */
    protected void emitSpecial(FormatBuilder buf, ILLKToken t, int spaces) {
        if (t.getOffset() < startOffset) {
            return;
        }
        if (isWhitespaces(t)) {
            buf.rtrimSpaces();
            for (int i = 0; i < spaces; ++i) {
                buf.append(' ');
            }
            skip(t.getOffset());
            return;
        }
        if (spaces > 0) {
            buf.space();
        }
        emitSpecial(buf, t);
    }

    /**
     * Emit special tokens before the given token, with special handling for pure whitespaces.
     *
     * @param spaces If special tokens are pure space, or whitespace and breaks==0,
     *               <0	as is;
     *               0	none;
     *               >0	ensure there is one space.
     * @param breaks If special tokens are pure whitespaces and has line breaks,
     *               <0	as is;
     *               >=0	keep only the specified number of line breaks.
     */
    protected void emitSpecial(FormatBuilder buf, ILLKToken t, int spaces, int breaks) {
        if (t.getOffset() < startOffset) {
            return;
        }
        int type = isSpacesOrWhitespaces(t);
        if (type == IS_SPACES || (type == IS_WHITESPACES) && breaks == 0) {
            if (spaces < 0) {
                for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext()) {
                    int offset = s.getOffset();
                    if (offset >= endOffset) {
                        break;
                    }
                    if (offset >= startOffset) {
                        buf.append(s.getText());
                    }
                }
            } else if (spaces > 0) {
                buf.space();
            }
            skip(t.getOffset());
            return;
        }
        if (type == IS_WHITESPACES) {
            emitLineBreaks(buf, t, breaks);
            skip(t.getOffset());
            return;
        }
        if (spaces > 0) {
            buf.space();
        }
        emitSpecial(buf, t);
    }

    /**
     * @param spaces <0	emit content of given buffer as is;
     *               0	emit nothing;
     *               >0	ensure there is at least one space.
     */
    protected void emitSpaces(FormatBuilder buf, CharSequence b, int spaces) {
        if (spaces == 0) {
            return;
        }
        if (spaces < 0) {
            buf.append(b);
            return;
        }
        buf.space();
    }

    /**
     * @param breaks <0	emit any NEWLINE token in the specials of 't';
     *               0	emit no line breaks;
     *               >0	emit at most the given number of line breaks in the specials of 't'.
     */
    protected void emitLineBreaks(FormatBuilder buf, ILLKToken t, int breaks) {
        if (breaks == 0) {
            return;
        }
        for (ILLKToken s = t.getSpecial(); s != null && breaks != 0; s = s.getNext()) {
            int offset = s.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset < startOffset) {
                continue;
            }
            if (s.getType() == typeNEWLINE) {
                buf.newLine();
                if (breaks > 0) {
                    --breaks;
                    if (breaks == 0) {
                        break;
                    }
                }
            }
        }
    }

    protected ILLKToken emitRestOfLine(FormatBuilder buf, ILLKToken t) {
        return emitRestOfLine(buf, t, false);
    }

    ////////////////////////////////////////////////////////////////////////

    public ILLKToken emit(FormatBuilder buf, ILLKNode node, int spaces, int breaks) {
        return emit(buf, node.getFirstToken(), node.getEndOffset(), spaces, breaks);
    }

    public ILLKToken emit(FormatBuilder buf, ILLKToken start, int end, int spaces, int breaks) {
        for (; start != null && start.getOffset() < end; start = start.getNext()) {
            emit(buf, start, spaces, breaks);
        }
        return start;
    }

    public ILLKToken emitSpaceAfter(FormatBuilder buf, ILLKToken t, int end, int spaces, int breaks) {
        t = emit(buf, t, end, spaces, breaks);
        space(buf, t);
        return t;
    }

    public void emit(FormatBuilder buf, ILLKToken t, int spaces, int breaks) {
        emitSpecial(buf, t, spaces, breaks);
        emitText(buf, t);
    }

    public void emit(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 0, 0);
        emitText(buf, t);
    }

    ////////////////////////////////////////////////////////////////////////

    protected ILLKToken emit1(FormatBuilder buf, ILLKToken start, int spaces, int breaks) {
        emit(buf, start, spaces, breaks);
        return start.getNext();
    }

    protected ILLKToken emitn(FormatBuilder buf, int count, ILLKToken start, int spaces, int breaks) {
        for (int i = 0; i < count; ++i) {
            emit(buf, start, spaces, breaks);
            start = start.getNext();
        }
        return start;
    }

    /**
     * Trim any preceeding spaces and emit the given token.
     */
    protected void emitNoSpace(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 0);
        emitText(buf, t);
    }

    /**
     * Trim any preceeding spaces and emit the next n tokens.
     *
     * @return The last (non-special) token that is emitted.
     */
    protected ILLKToken emitNoSpace(FormatBuilder buf, int n, ILLKToken t) {
        for (int i = 0; i < n; ++i, t = t.getNext()) {
            emitSpecial(buf, t, 0);
            emitText(buf, t);
        }
        return t;
    }

    protected void emitSpace(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 1);
        emitText(buf, t);
    }

    /**
     * @return The last (non-special) token that is emitted.
     */
    protected ILLKToken emitSpace(FormatBuilder buf, int n, ILLKToken t) {
        for (int i = 0; i < n; ++i, t = t.getNext()) {
            emitSpecial(buf, t, 1);
            emitText(buf, t);
        }
        return t;
    }

    protected void emitSpaceAfter(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 0, 0);
        emitText(buf, t);
        space(buf, t.getNext());
    }

    protected void emitSpaceAfter(FormatBuilder buf, ILLKToken t, int spaces, int breaks) {
        emitSpecial(buf, t, spaces, breaks);
        emitText(buf, t);
        space(buf, t.getNext());
    }

    protected void emitSpaceSpace(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 1, 0);
        emitText(buf, t);
        space(buf, t.getNext());
    }

    protected void emitNewline(FormatBuilder buf, ILLKToken t, int breaks) {
        emitSpecial(buf, t, 1, breaks);
        flushLine(buf, t);
        emitText(buf, t);
    }

    protected void flushLine(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            buf.flushLine();
        }
    }

    protected void emitText(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            buf.append(t.getText());
        }
        skipToken(t);
    }

    protected void indent(FormatBuilder buf, ILLKToken t) {
        int offset = t.getOffset();
        if (offset >= startOffset) {
            buf.indent();
        } else if (offset < macroEndOffset) {
            buf.indentAfterBreak();
        }
    }

    protected void unIndent(FormatBuilder buf, ILLKToken t) {
        int offset = t.getOffset();
        if (offset >= startOffset) {
            buf.unIndent();
        } else if (offset < macroEndOffset) {
            buf.unIndentAfterBreak();
        }
    }

    protected void indentAfterBreak(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            buf.indentAfterBreak();
        }
    }

    protected void space(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            buf.space();
        }
    }

    public void emitBlock(FormatBuilder buf, ILLKNode node) {
        emitRestOfLine(buf, node.getFirstToken());
        emit(buf, node, 1, 0);
        emitRestOfLine(buf, node.getLastToken().getNext());
    }

    public void emitBlock(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            emitRestOfLine(buf, t);
            emit(buf, t, 0, 0);
            emitRestOfLine(buf, t.getNext());
        }
    }

    public void emitSimpleStatement(FormatBuilder buf, ILLKNode node) {
        ILLKToken end = node.getLastToken();
        emit(buf, node.getFirstToken(), end.getOffset(), 1, 0);
        emit(buf, end, 0, 0);
        emitRestOfLine(buf, end.getNext());
    }

    protected void emitUnIndentToken(FormatBuilder buf, ILLKToken t, int spaces, int breaks) {
        emitSpecial(buf, t, spaces, breaks);
        if (t.getOffset() >= startOffset) {
            if (buf.endsWithLineBreak()) {
                buf.unIndent();
            } else {
                buf.unIndentAfterBreak();
            }
        }
        emitText(buf, t);
    }

    protected void emitUnformatted(FormatBuilder buf, CharSequence s) {
        int len = s.length();
        int end = LLKShareUtil.trailingChars(s, len, new char[]{' ', '\t', '\u000b', '\u000c'});
        if (end < len) {
            s = s.subSequence(0, end);
        }
        buf.flushLine();
        buf.appendFormatted(s);
        buf.flushLine();
    }

    protected ILLKToken emitIfMatch(FormatBuilder buf, ILLKToken t, int type, int spaces, int breaks) {
        if (t != null && t.getType() == type) {
            emit(buf, t, spaces, breaks);
            t = t.getNext();
        }
        return t;
    }

    protected ILLKToken emitBlockIfMatch(FormatBuilder buf, ILLKToken t, int end, int type) {
        boolean first = true;
        for (; t != null && t.getOffset() < end && t.getType() == type; ) {
            if (first) {
                emitRestOfLine(buf, t);
            }
            first = false;
            emit(buf, t, 0, 1);
            t = t.getNext();
            emitRestOfLine(buf, t);
        }
        return t;
    }

    public void emitSpecialElement(FormatBuilder buf, ILLKToken t, int perline) {
        int end = t.getOffset();
        if (end < startOffset) {
            return;
        }
        if (perline > 0 && isWhitespaces(t)) {
            skip(end);
            return;
        }
        FormatBuilder bb = new FormatBuilder(buf);
        ElementRange e = new ElementRange(bb, t, end);
        emitSpecial(bb, t, 0, -1);
        bb.flush();
        if (buf.getSpaceUtil().isSpaces(bb.getFormatted())) {
            return;
        }
        e.end = end;
        e.isSpecial = true;
        emitElement(buf, e, 0, perline);
    }

    public int emitElement(FormatBuilder buf, ElementRange e, int eindex, int perline) {
        int linewidth = buf.getLineWidth();
        FormatBuilder b = e.buffer;
        b.flush();
        StringBuilder formatted = b.getFormatted();
        boolean onelinerok = b.isOnelinerOK();
        if (!e.isSpecial) {
            ISpaceUtil util = buf.getSpaceUtil();
            CharSequence oneliner = (onelinerok ? b.getOneLiner(formatted, true, true) : null);
            if (!onelinerok) {
                buf.setOnelinerOK(false);
            }
            if (oneliner != null) {
                int len = oneliner.length();
                int start = util.skipSpaces(oneliner, 0, len);
                int right = b.columnOf(oneliner, start, len, b.getIndentWidth()) + 1;
                if (right < linewidth) {
                    if (buf.columnOf(oneliner, start, len) >= linewidth) {
                        buf.flushLine();
                    } else {
                        buf.space();
                    }
                    buf.append(oneliner);
                    buf.rtrimWhitespaces();
                    ++eindex;
                    if (e.hardBreak || perline > 0 && (eindex % perline) == 0) {
                        buf.flushLine();
                        eindex = 0;
                    }
                    return eindex;
                }
            }
            int end = util.rskipWhitespaces(formatted, 0, formatted.length());
            int start = util.skipWhitespaces(formatted, 0, end);
            int index = util.skipLine(formatted, start, end);
            if (index < 0) {
                index = end;
            }
            if (buf.columnOf(formatted, start, index) + 1 < linewidth) {
                buf.space();
                buf.append(formatted, start, index);
                index = util.skipLineBreak(formatted, index, end);
                formatted.delete(0, index);
            }
        }
        buf.flushLine();
        buf.appendFormatted(b, onelinerok && !e.isSpecial, true);
        buf.flushLine();
        return 0;
    }

    public boolean emitDotSuffix(FormatBuilder buf, ILLKToken start, int end, boolean indented) {
        FormatBuilder b = new FormatBuilder(buf);
        if (!indented) {
            indent(b, start);
        }
        start = emit(b, start, end, 0, 0);
        b.flush();
        CharSequence oneliner = b.getOneLiner(b.getFormatted(), false, true);
        if (oneliner != null && buf.canFit(oneliner, 0)) {
            buf.append(oneliner);
        } else {
            emitRestOfLine(buf, start, true);
            if (!indented) {
                indent(buf, start);
                indented = true;
            }
            if (oneliner != null && buf.canFit(oneliner, 0)) {
                buf.append(oneliner);
            } else {
                b.getSpaceUtil().ltrimBlankLines(b.getFormatted());
                buf.appendFormatted(b, false, false, false, true, 0);
            }
        }
        return indented;
    }

    //////////////////////////////////////////////////////////////////////

    public void compactCloseBrace(FormatBuilder buf) {
        ISpaceUtil util = buf.getSpaceUtil();
        StringBuilder line = buf.getLineBuffer();
        int end = line.length();
        int start = util.skipSpaces(line, 0, end);
        if (start >= end || line.charAt(start) != '}' || util.skipSpaces(line, start + 1, end) != end) {
            return;
        }
        StringBuilder formatted = buf.getFormatted();
        end = formatted.length();
        int e = util.rskipWhitespaces(formatted, 0, end);
        if (e == end) {
            return;
        }
        end = e;
        boolean trimmed = false;
        while (true) {
            int n = 0;
            for (; end > 0 && formatted.charAt(end - 1) == '}'; --end) {
                ++n;
            }
            if (n == 0) {
                return;
            }
            if (end > 0) {
                end = util.rskipSpaces(formatted, 0, end);
            }
            if (end < 0) {
                return;
            }
            e = util.rskipLineBreak(formatted, 0, end);
            if (e == end) {
                return;
            }
            if (!trimmed) {
                line.setLength(start + 1);
                trimmed = true;
            }
            formatted.setLength(end);
            for (int i = 0; i < n; ++i) {
                line.append('}');
            }
            end = util.rskipWhitespaces(formatted, 0, e);
        }
    }

    //////////////////////////////////////////////////////////////////////

    protected ILLKToken startBlock(FormatBuilder buf, ILLKToken t, boolean newline) {
        if (newline) {
            emitRestOfLine(buf, t);
            emitSpecial(buf, t, 0, -1);
        } else {
            emitSpecial(buf, t, 1, 0);
        }
        emitText(buf, t);
        emitRestOfLine(buf, t = t.getNext());
        indent(buf, t);
        return t;
    }

    public ILLKToken startBlock(FormatBuilder buf, ILLKToken t) {
        if (t.getOffset() >= startOffset) {
            emit(buf, t, 1, 0);
        }
        emitRestOfLine(buf, t = t.getNext());
        indent(buf, t);
        return t;
    }

    protected void endBlock(FormatBuilder buf, ILLKToken t) {
        boolean b = t.getOffset() >= startOffset;
        if (b) {
            emitRestOfLine(buf, t);
            emitSpecial(buf, t, 0, 0);
            buf.rtrimBlankLines();
        }
        unIndent(buf, t);
        if (b) {
            emitText(buf, t);
        }
    }

    public void startDangle(FormatBuilder buf, ILLKToken t) {
        emitRestOfLine(buf, t);
        indent(buf, t);
    }

    public void endDangle(FormatBuilder buf, ILLKToken t) {
        emitRestOfLine(buf, t);
        unIndent(buf, t);
    }

    public ILLKToken startParen(FormatBuilder buf, ILLKToken t) {
        emit(buf, t);
        emitRestOfLine(buf, t = t.getNext(), true);
        indent(buf, t);
        return t;
    }

    public void endParen(FormatBuilder buf, ILLKToken t) {
        emitRestOfLine(buf, t, true);
        emitSpecial(buf, t, 0, 0);
        unIndent(buf, t);
        emitText(buf, t);
    }

    public ILLKToken startParen(FormatBuilder buf, ILLKToken t, boolean softbreak) {
        emit(buf, t);
        emitRestOfLine(buf, t = t.getNext(), softbreak);
        indent(buf, t);
        return t;
    }

    public void endParen(FormatBuilder buf, ILLKToken t, boolean softbreak) {
        emitRestOfLine(buf, t, softbreak);
        emitSpecial(buf, t, 0, 0);
        unIndent(buf, t);
        emitText(buf, t);
    }

    public FormatBuilder startParenSession(FormatBuilder buf, ILLKToken t) {
        return startParenSession(buf, t, true, true);
    }

    public FormatBuilder startParenSession(FormatBuilder buf, ILLKToken t, boolean grabline, boolean softbreak) {
        emitSpecial(buf, t, 0, 1);
        FormatBuilder b = startSession(buf, grabline);
        emitText(b, t);
        emitRestOfLine(b, t = t.getNext(), softbreak);
        indent(b, t);
        return b;
    }

    public void endParenSession(FormatBuilder buf, FormatBuilder b, ILLKToken t) {
        endParen(b, t);
        endSession(buf, b);
    }

    //////////////////////////////////////////////////////////////////////

    public FormatBuilder startSession(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, 0, 0);
        return startSession(buf, true);
    }

    public FormatBuilder startSession(FormatBuilder buf, ILLKToken t, int spaces, int breaks) {
        emitSpecial(buf, t, spaces, breaks);
        return startSession(buf, true);
    }

    public FormatBuilder startSession(FormatBuilder buf) {
        return startSession(buf, true);
    }

    public FormatBuilder startSession(FormatBuilder buf, boolean grabline) {
        FormatBuilder ret = bufferPool.get();
        ret.init(buf);
        ret.setOnelinerOK(true);
        if (grabline) {
            StringBuilder line = buf.getLineBuffer();
            ret.append(line);
            line.setLength(0);
        }
        return ret;
    }

    public void endSession(FormatBuilder buf, FormatBuilder b) {
        endSession(buf, b, true, false);
    }

    public void endSession(FormatBuilder buf, FormatBuilder b, boolean wasspace, boolean join) {
        endSession(buf, b, wasspace, join, false);
    }

    public void endSession(FormatBuilder buf, FormatBuilder b, boolean wasspace, boolean join, boolean unindent) {
        try {
            endSession1(buf, b, wasspace, join, unindent);
        } finally {
            b.destroy();
            bufferPool.unget(b);
        }
    }

    public void endSession1(FormatBuilder buf, FormatBuilder b, boolean wasspace, boolean join, boolean unindent) {
        b.flush();
        StringBuilder formatted = b.getFormatted();
        CharSequence oneliner = null;
        if (b.isOnelinerOK()) {
            oneliner = buf.getOnelinerFormatter().toOneliner(formatted, 0, formatted.length(), wasspace,
                buf.getLineWidth() - buf.getIndentWidth(), b.getSoftBreaks());
        } else {
            buf.setOnelinerOK(false);
        }
        if (oneliner != null && buf.canFit(oneliner, 0, oneliner.length(), 0)) {
            buf.appendFormattedOneLiner(formatted, oneliner);
            return;
        }
        if (join) {
            ISpaceUtil spaceutil = b.getSpaceUtil();
            int end = formatted.length();
            int s = spaceutil.skipWhitespaces(formatted, 0, end);
            int e = spaceutil.skipLineSafe(formatted, s, end);
            String line = formatted.substring(s, e);
            if (buf.canFit(line, 0)) {
                buf.flushLine(line);
                e = spaceutil.skipLineBreak(formatted, e, end);
                if (e < end) {
                    String str = formatted.substring(e, end);
                    if (unindent) {
                        str = unIndent(str, b.getTab(), b.getSpaceUtil());
                    }
                    buf.appendFormatted(str);
                    buf.unshiftNonTerminatedLine();
                }
                return;
            }
        }
        buf.flushLine();
        buf.appendFormatted(b, false, false, true, wasspace, 0);
    }

    //////////////////////////////////////////////////////////////////////

    protected ILLKToken skipLineBreaks(ILLKToken token, int n) {
        if (token == null) {
            return null;
        }
        ILLKToken s = token.getSpecial();
        for (; s != null && n > 0; s = s.getNext()) {
            int offset = s.getOffset();
            if (offset < startOffset) {
                continue;
            }
            int type = s.getType();
            if (type == typeSPACES) {
                continue;
            }
            if (type == typeNEWLINE) {
                skipToken(s);
                --n;
                continue;
            }
            break;
        }
        return s;
    }

    protected void skipBlankLines(ILLKToken token) {
        if (token == null) {
            return;
        }
        boolean wasbreak = false;
        for (ILLKToken s = token.getSpecial(); s != null; s = s.getNext()) {
            if (s.getType() == typeSPACES) {
                continue;
            }
            if (s.getType() != typeNEWLINE) {
                return;
            }
            if (wasbreak) {
                skip(s.getEndOffset());
            } else {
                wasbreak = true;
            }
        }
    }

    protected boolean hasLineBreak(ILLKToken t) {
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset < startOffset) {
                continue;
            }
            int type = t.getType();
            if (type == typeNEWLINE) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if special before given token starts with a NEWLINE or CPP_COMMENT.
     */
    protected boolean startsWithLineBreak(ILLKToken t) {
        int type;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset < startOffset) {
                continue;
            }
            type = t.getType();
            if (type == typeNEWLINE) {
                return true;
            }
            if (type != typeSPACES) {
                return false;
            }
        }
        return false;
    }

    /**
     * @return true if last non-space special before the given token is a NEWLINE or CPP_COMMENT.
     */
    protected boolean endsWithLineBreak(ILLKToken t) {
        int type;
        boolean isbreak = false;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset < startOffset) {
                continue;
            }
            type = t.getType();
            if (type == typeSPACES) {
                continue;
            }
            isbreak = (type == typeNEWLINE);
        }
        return isbreak;
    }

    /**
     * @return true if special before given token starts with a NEWLINE or CPP_COMMENT.
     */
    protected boolean startsWithBlankLine(ILLKToken t) {
        int type;
        boolean wasbreak = false;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset >= endOffset) {
                break;
            }
            if (offset < startOffset) {
                continue;
            }
            type = t.getType();
            if (type == typeNEWLINE) {
                if (wasbreak) {
                    return true;
                }
                wasbreak = true;
                continue;
            }
            if (type != typeSPACES) {
                return false;
            }
        }
        return false;
    }

    protected boolean isWhitespaces(ILLKToken t) {
        int type;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset < startOffset) {
                continue;
            }
            if (offset >= endOffset) {
                break;
            }
            type = t.getType();
            if (type != typeSPACES && type != typeNEWLINE) {
                return false;
            }
        }
        return true;
    }

    protected boolean isSpaces(ILLKToken t) {
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset < startOffset) {
                continue;
            }
            if (offset >= endOffset) {
                break;
            }
            if (t.getType() != typeSPACES) {
                return false;
            }
        }
        return true;
    }

    protected int isSpacesOrWhitespaces(ILLKToken t) {
        return isSpacesOrWhitespaces(t, endOffset);
    }

    protected int isSpacesOrWhitespaces(ILLKToken t, int end) {
        int ret = IS_SPACES;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset < startOffset) {
                continue;
            }
            if (offset >= end) {
                break;
            }
            int type = t.getType();
            if (type == typeNEWLINE) {
                ret |= IS_WHITESPACES;
            } else if (type != typeSPACES) {
                return 0;
            }
        }
        return ret;
    }

    protected boolean isWhitespaces(ILLKToken t, int end) {
        int type;
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset < startOffset) {
                continue;
            }
            if (offset >= end) {
                break;
            }
            type = t.getType();
            if (type != typeSPACES && type != typeNEWLINE) {
                return false;
            }
        }
        return true;
    }

    protected boolean isSpaces(ILLKToken t, int end) {
        for (t = t.getSpecial(); t != null; t = t.getNext()) {
            int offset = t.getOffset();
            if (offset < startOffset) {
                continue;
            }
            if (offset >= end) {
                break;
            }
            if (t.getType() != typeSPACES) {
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////

    protected static ILLKToken follow(ILLKNode node) {
        return node.getLastToken().getNext();
    }

    public static String indentComment(ILLKToken s, String linebreak, IReadOnlyLocator locator, ISpaceUtil spacer) {
        return indentComment(s.getText(), s.getOffset(), linebreak, locator, spacer);
    }

    public static String indentComment(
        CharSequence comment, int offset, String linebreak, IReadOnlyLocator locator, ISpaceUtil spacer) {
        int start = 0;
        int end = comment.length();
        int index = LLKShareUtil.indexOf('\n', comment, start, end);
        if (index < 0) {
            return spacer.trimSpaces(comment) + linebreak;
        }
        StringBuilder formatted = new StringBuilder();
        int column = locator.getLocation(offset).getColumn();
        int spaces = 0;
        do {
            if (start != 0) {
                formatted.append(comment.charAt(start) == '*' ? " " : LLKShareUtil.stringOf(' ', spaces));
            }
            int adjust = (index > start && comment.charAt(index - 1) == '\r') ? -1 : 0;
            formatted.append(comment, start, index + adjust);
            formatted.append(linebreak);
            start = spacer.skipSpaces(comment, index + 1, end);
            int col = locator.getLocation(offset + start).getColumn();
            if (col > column) {
                spaces = col - column;
            } else {
                spaces = 0;
            }
            index = LLKShareUtil.indexOf('\n', comment, start, end);
        } while (start < end && index >= 0);
        if (start < end) {
            if (start != 0) {
                formatted.append(comment.charAt(start) == '*' ? " " : LLKShareUtil.stringOf(' ', spaces));
            }
            formatted.append(comment, start, end);
        }
        formatted.append(linebreak);
        return formatted.toString();
    }

    private String unIndent(String s, String tab, ISpaceUtil spaceutil) {
        StringBuilder b = new StringBuilder(s);
        int end = b.length();
        int tablen = tab.length();
        int index = 0;
        if (tab.equals(b.substring(0, tablen))) {
            b.replace(0, tablen, "");
            end -= tablen;
        }
        while ((index = spaceutil.skipLineSafe(b, index, end)) < end) {
            index = spaceutil.skipLineBreak(b, index, end);
            if (index < end) {
                if (tab.equals(b.substring(index, index + tablen))) {
                    b.replace(index, index + tablen, "");
                    end -= tablen;
                }
            }
        }
        return b.toString();
    }

    //////////////////////////////////////////////////////////////////////

    protected static class ElementRange {
        public FormatBuilder buffer;
        public ILLKToken startToken;
        public int start;
        public int end;
        public boolean isSpecial;
        public boolean hardBreak;
        public ElementRange(FormatBuilder b, ILLKToken t, int startoffset) {
            this.buffer = b;
            this.startToken = t;
            this.start = startoffset;
        }
    }

    //////////////////////////////////////////////////////////////////////
}
