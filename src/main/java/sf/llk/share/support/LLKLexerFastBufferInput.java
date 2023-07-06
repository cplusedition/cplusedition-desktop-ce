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
package sf.llk.share.support;

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

/*
 * LLKLexerCaseBufferInput is similar to LLKLexerBufferInput but without ignore case support and
 * minimized moving characters if possible.
 *
 * [*****------------------*********************]
 *           ^offset             ^laStart    ^laOffset
 *
 */
public class LLKLexerFastBufferInput implements ILLKLexerInput, Cloneable {

    ////////////////////////////////////////////////////////////

    private static final boolean CHECK = true;
    private static final int MARKER_SIZE = 1;

    ////////////////////////////////////////////////////////////

    private final char[] original;
    private final int originalStart;
    private ILLKMain main;
    private final ISourceLocator locator;
    private char[] source;
    private int length;
    private int gapStart;
    private int gapEnd;
    private int laOffset;
    private int laLength;
    private int markOffset;
    private int[] markLocations;
    private int la1;

    ////////////////////////////////////////////////////////////

    public LLKLexerFastBufferInput(final char[] a, final ILLKMain main) {
        this(a, 0, main);
    }

    public LLKLexerFastBufferInput(final char[] a, final int start, final ILLKMain main) {
        original = a;
        originalStart = start;
        source = a;
        this.main = main;
        gapStart = 0;
        length = source.length - start;
        gapEnd = start;
        laOffset = start;
        laLength = source.length;
        markLocations = new int[32];
        locator = main.getLocator();
        LA1x();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKLexerFastBufferInput ret = (LLKLexerFastBufferInput) super.clone();
        ret.source = source.clone();
        ret.main = (ILLKMain) main.clone();
        return ret;
    }

    @Override
    public void reset() {
        source = original.clone();
        gapStart = 0;
        length = source.length - originalStart;
        gapEnd = originalStart;
        laOffset = originalStart;
        laLength = source.length;
        markOffset = 0;
        locator.rewind(0);
        LA1x();
    }

    @Override
    public ILLKMain getMain() {
        return main;
    }

    @Override
    public final int getOffset() {
        return gapStart - gapEnd + laOffset;
    }

    public int getSourceLength() {
        return length;
    }

    @Override
    public final void mark() {
        if (markOffset >= markLocations.length) {
            expandMarkLocations();
        }
        markLocations[markOffset++] = getOffset();
        locator.mark();
    }

    @Override
    public final void unmark() {
        --markOffset;
        locator.unmark();
    }

    @Override
    public final void remark() {
        locator.unmark();
        markLocations[markOffset - 1] = getOffset();
        locator.mark();
    }

    @Override
    public final void rewind() {
        final int newoffset = markLocations[--markOffset];
        if (gapStart == gapEnd) {
            gapStart = newoffset;
            gapEnd = newoffset;
            laOffset = newoffset;
        } else {
            while (gapStart > newoffset) {
                source[--gapEnd] = source[--gapStart];
            }
            laOffset = newoffset + gapEnd - gapStart;
        }
        locator.rewind();
        LA1x();
    }

    @Override
    public final boolean isMarked() {
        return markOffset > 0;
    }

    /**
     * Rewind character input stream to the given offset.
     */
    @Override
    public final void rewind(final int newoffset) {
        final int offset = getOffset();
        if (newoffset > offset) {
            throw new AssertionError(
                "Cannot rewind to a forward location: offset=" + offset + ", newoffset=" + newoffset);
        }
        if (gapStart == gapEnd) {
            gapStart = newoffset;
            gapEnd = newoffset;
            laOffset = newoffset;
        } else {
            if (gapStart > newoffset) {
                final int len = gapStart - newoffset;
                System.arraycopy(source, newoffset, source, gapEnd - len, len);
                gapStart = newoffset;
                gapEnd -= len;
            }
            laOffset = newoffset + gapEnd - gapStart;
        }
        locator.rewind(newoffset);
        LA1x();
    }

    @Override
    public void setCutoff() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addCutoff() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCutoff(final int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeCutoff(final int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void consume() throws LLKParseError {
        ++laOffset;
        if (laOffset < laLength) {
            la1 = source[laOffset];
        } else if (laOffset == laLength) {
            la1 = ILLKConstants.LEXER_EOF;
        } else {
            throw new LLKParseError("consume(int) passed EOF: laEnd=" + laLength + ", laOffset=" + laOffset);
        }
    }

    @Override
    public final void consume(final int n) throws LLKParseError {
        laOffset += n;
        LA1x();
    }

    @Override
    public final int consume(final IConsumer consumer) throws LLKParseError {
        for (; laOffset < laLength; ++laOffset) {
            if (!consumer.consume(source[laOffset])) {
                return la1 = source[laOffset];
            }
        }
        return la1 = ILLKConstants.LEXER_EOF;
    }

    @Override
    public final int LA0() {
        if (laOffset > gapEnd) {
            return source[laOffset - 1];
        }
        if (gapStart == 0) {
            return ILLKConstants.LEXER_EOF;
        }
        return source[gapStart - 1];
    }

    @Override
    public final int LA1() {
        return la1;
    }

    @Override
    public final int LA(int n) {
        if (n == 0) {
            return LA0();
        }
        n += laOffset - 1;
        if (n >= laLength) {
            return ILLKConstants.LEXER_EOF;
        }
        return source[n];
    }

    @Override
    public final boolean LA(final char[] expected) {
        int index = laOffset;
        final int len = expected.length;
        if (index + len > laLength) {
            return false;
        }
        for (int i = 0; i < len; ++i, ++index) {
            if (source[index] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param index Start from LA(index)
     */
    @Override
    public final boolean LA(final char[] expected, int index) {
        index += laOffset - 1;
        final int len = expected.length;
        if (index + len > laLength) {
            return false;
        }
        for (int i = 0; i < len; ++i, ++index) {
            if (source[index] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private void LA1x() {
        if (laOffset < laLength) {
            la1 = source[laOffset];
        } else if (laOffset == laLength) {
            la1 = ILLKConstants.LEXER_EOF;
        } else {
            throw new LLKParseError("consume(int) passed EOF: laEnd=" + laLength + ", laOffset=" + laOffset);
        }
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final char charAt(int index) {
        if (index >= gapStart) {
            index += gapEnd - gapStart;
        }
        return source[index];
    }

    @Override
    public int length() {
        return gapStart;
    }

    @Override
    public void getChars(int sstart, int send, final char[] dst, final int dstart) {
        if (sstart <= gapStart && send > gapStart) {
            throw new AssertionError(
                "Cannot access across offset: " + gapStart + ", request start=" + sstart + ", end=" + send);
        }
        if (sstart > gapStart) {
            final int gap = gapEnd - gapStart;
            sstart += gap;
            send += gap;
        }
        System.arraycopy(source, sstart, dst, dstart, send - sstart);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return getSource(start, end);
    }

    @Override
    public final CharSequence getSource() {
        if (laOffset == laLength) {
            if (source.length != length) {
                resize(length);
            }
            return new CharRange(source, 0, length);
        }
        final StringBuilder b = new StringBuilder(length + 16);
        b.append(source, 0, gapStart);
        b.append(source, gapEnd, source.length - gapEnd);
        return b;
    }

    @Override
    public final CharSequence getSource(final int start) {
        final int offset = getOffset();
        int gap = 0;
        if (start < gapStart) {
            splitAt(offset);
        } else {
            gap = gapEnd - gapStart;
        }
        return new CharRange(source, start + gap, offset + gap);
    }

    @Override
    public final CharSequence getSource(final int start, final int end) {
        final int offset = getOffset();
        if (end > offset) {
            throw new AssertionError(
                "ASSERT: Cannot get source ahead of current offset: end=" + end + ", offset=" + offset);
        }
        int gap = 0;
        if (start < gapStart) {
            if (end > gapStart) {
                splitAt(offset);
            }
        } else {
            gap = gapEnd - gapStart;
        }
        return new CharRange(source, start + gap, end + gap);
    }

    @Override
    public void setIgnoreCase(final boolean ignorecase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIgnoreCase() {
        return false;
    }

    @Override
    public void replace(final int start, final int end, final char[] text) {
        replace(start, end, text, 0, text.length);
    }

    @Override
    public void replace(final int start, final int end, final char[] text, final int tstart, final int tend) {
        final int len = tend - tstart;
        adjustForReplace(start, end, len);
        System.arraycopy(text, tstart, source, gapEnd, len);
        LA1x();
    }

    @Override
    public void replace(final int start, final int end, final ICharSequence range) {
        final int len = range.length();
        adjustForReplace(start, end, len);
        range.getChars(0, len, source, gapEnd);
        LA1x();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public ISourceLocator getLocator() {
        return main.getLocator();
    }

    @Override
    public ISourceLocation getLocation() {
        return locator.getLocation(getOffset());
    }

    @Override
    public ISourceLocation getLocation(final int offset) {
        return locator.getLocation(offset);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final void newline() {
        locator.newline(getOffset());
    }

    /**
     * NOTE: When tab() is called by user action, \t should have been consumed,
     * so here offset is decremented.
     */
    @Override
    public final void tab() {
        locator.tab(getOffset() - 1);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final boolean matchOpt(final char c) {
        if (laOffset >= laLength || source[laOffset] != c) {
            return false;
        }
        consume();
        return true;
    }

    @Override
    public int LA1Consume() {
        final int ret = la1;
        consume();
        return ret;
    }

    @Override
    public int consumeLA0() {
        int la0 = la1;
        consume();
        return la0;
    }

    @Override
    public int consumeLA1() {
        consume();
        return la1;
    }

    @Override
    public final void match(final char c) throws LLKParseException {
        if (la1 != c) {
            throw llkMismatchException("match(char): expected=", c);
        }
        consume();
    }

    @Override
    public final void matchNot(final char c) throws LLKParseException {
        if (la1 == c) {
            throw llkMismatchException("matchNot(char): not expected=", c);
        }
        consume();
    }

    @Override
    public final void match(final char c1, final char c2) throws LLKParseException {
        if (la1 != c1 && la1 != c2) {
            throw llkMismatchException("match(char, char): expected=", c1, c2, la1);
        }
        consume();
    }

    @Override
    public final void matchNot(final char c1, final char c2) throws LLKParseException {
        if (la1 == c1 || la1 == c2) {
            throw llkMismatchException("matchNot(char, char): not expected=", c1, c2, la1);
        }
        consume();
    }

    @Override
    public final void match(final char c1, final char c2, final char c3) throws LLKParseException {
        int c = ILLKConstants.LEXER_EOF;
        if (laOffset >= laLength || ((c = source[laOffset]) != c1 && c != c2 && c != c3)) {
            throw llkMismatchException("match(char, char, char): expected=", c1, c2, c3, la1);
        }
        consume();
    }

    @Override
    public final void matchNot(final char c1, final char c2, final char c3) throws LLKParseException {
        if (la1 == c1 || la1 == c2 || la1 == c3) {
            throw llkMismatchException("matchNot(char, char, char): not expected=", c1, c2, c3, la1);
        }
        consume();
    }

    @Override
    public final void matchRange(final char first, final char last) throws LLKParseException {
        if (la1 < first || la1 > last) {
            throw llkMismatchException("matchRange(char, char): range=", first, last, la1);
        }
        consume();
    }

    @Override
    public final void matchNotRange(final char first, final char last) throws LLKParseException {
        if (la1 >= first && la1 <= last) {
            throw llkMismatchException("matchNotRange(char, char): range=", first, last, la1);
        }
        consume();
    }

    @Override
    public final void match(final int[] bset) throws LLKParseException {
        if (llkGetBit(la1, bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
        }
    }

    @Override
    public final void matchNot(final int[] bset) throws LLKParseException {
        if (llkGetBitInverted(la1, bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
        }
    }

    @Override
    public final void match(final char[] s) throws LLKParseException {
        final int len = s.length;
        int c;
        for (int i = 0, index = laOffset; i < len; ++i, ++index) {
            c = (index >= laLength ? ILLKConstants.LEXER_EOF : source[index]);
            if (c != s[i]) {
                throw llkMismatchException(
                    "match(char[]): expected string=" + new String(s) + ", i=" + i + ", actual=" + _toString(c));
            }
        }
        consume(len);
    }

    @Override
    public final void match(final char[] s, final int start) throws LLKParseException {
        consume(start);
        final int len = s.length;
        int c;
        for (int i = start, index = laOffset; i < len; ++i, ++index) {
            c = (index >= laLength ? ILLKConstants.LEXER_EOF : source[index]);
            if (c != s[i]) {
                throw llkMismatchException(
                    "match(char[], start): expected string=" + new String(s) + ", i=" + i + ", actual=" + _toString(c));
            }
        }
        consume(len - start);
    }

    ////////////////////////////////////////////////////////////

    protected LLKParseException llkMismatchException(final String msg) {
        return new LLKParseException(msg, locator.getLocation(getOffset()));
    }

    protected LLKParseException llkMismatchException(final String msg, final int[] bset) {
        return new LLKParseException(
            msg + _toString(bset) + ", actual=" + _toString(LA1()), locator.getLocation(getOffset()));
    }

    protected LLKParseException llkMismatchException(final String msg, final int c) {
        return new LLKParseException(
            msg + '\'' + _toString(c) + "', actual='" + _toString(LA1()) + '\'', locator.getLocation(getOffset()));
    }

    protected LLKParseException llkMismatchException(final String msg, final int first, final int last, final int c) {
        return new LLKParseException(
            msg + "('" + _toString(first) + "', '" + _toString(last) + "'), actual='" + _toString(c) + '\'',
            locator.getLocation(getOffset()));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int c3, final int c) {
        return new LLKParseException(
            msg
                + ("('" + _toString(c1) + "', '" + _toString(c2) + "', '" + _toString(c3))
                + "'), actual='"
                + _toString(c)
                + '\'',
            locator.getLocation(getOffset()));
    }

    protected final String _toString(final int[] bset) {
        return AbstractLexerInput._toString(bset);
    }

    protected final String _toString(final int c) {
        return AbstractLexerInput._toString(c);
    }

    ////////////////////////////////////////////////////////////

    protected static boolean llkGetBit(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
    }

    /**
     * @return true if n is a set bit in the bitset that is invert of the given bitset.
     */
    protected static boolean llkGetBitInverted(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
    }

    ////////////////////////////////////////////////////////////

    private void expandMarkLocations() {
        final int[] ret = new int[markLocations.length * 2];
        System.arraycopy(markLocations, 0, ret, 0, markOffset);
        markLocations = ret;
        if (CHECK && MARKER_SIZE > 1 && (ret.length % MARKER_SIZE) != 0) {
            throw new LLKParseError("ASSERT: (ret.length % MARKER_SIZE) == 0");
        }
    }

    private void resize(final int newsize) {
        final char[] a = new char[newsize];
        final int delta = newsize - laLength;
        final int newend = gapEnd + delta;
        System.arraycopy(source, 0, a, 0, gapStart);
        System.arraycopy(source, gapEnd, a, newend, laLength - gapEnd);
        source = a;
        gapEnd = newend;
        laOffset += delta;
        laLength = newsize;
    }

    private void splitAt(final int index) {
        if (gapStart == index) {
            return;
        }
        if (gapStart == gapEnd) {
            gapStart = index;
            gapEnd = index;
            return;
        }
        if (gapStart > index) {
            final int len = gapStart - index;
            gapEnd -= len;
            System.arraycopy(source, index, source, gapEnd, len);
            gapStart = index;
        } else if (gapStart < index) {
            final int len = index - gapStart;
            System.arraycopy(source, gapEnd, source, gapStart, len);
            gapStart = index;
            gapEnd += len;
        }
    }

    private void adjustForReplace(final int start, final int end, final int len) {
        final int delta = len - end + start;
        splitAt(end);
        laOffset = gapEnd;
        gapStart = start;
        if (source == original || len > gapEnd - gapStart) {
            resize(source.length + Math.max(delta << 2, source.length));
        }
        gapEnd -= len;
        laOffset = gapEnd;
        length += delta;
    }

    ////////////////////////////////////////////////////////////
}
