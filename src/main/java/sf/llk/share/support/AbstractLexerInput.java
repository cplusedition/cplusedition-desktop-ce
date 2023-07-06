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

import java.util.Arrays;
public abstract class AbstractLexerInput implements ILLKLexerInput, Cloneable {

    ////////////////////////////////////////////////////////////

    protected static final int MARK_CAP = 32;

    ////////////////////////////////////////////////////////////

    protected ILLKMain main;
    protected ISourceLocator locator;
    protected int offset;
    protected int markOffset;
    protected int[] markLocations;
    protected int la1;

    ////////////////////////////////////////////////////////////

    public AbstractLexerInput(final ILLKMain main) {
        this.main = main;
        locator = main.getLocator();
        markLocations = new int[MARK_CAP];
    }

    protected abstract void LA1x();

    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractLexerInput ret = (AbstractLexerInput) super.clone();
        ret.main = (ILLKMain) main.clone();
        ret.locator = ret.main.getLocator();
        ret.markLocations = markLocations.clone();
        return ret;
    }
    @Override
    public void reset() {
        markOffset = 0;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final ILLKMain getMain() {
        return main;
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final ISourceLocator getLocator() {
        return locator;
    }

    @Override
    public final ISourceLocation getLocation() {
        return locator.getLocation(offset);
    }

    @Override
    public final ISourceLocation getLocation(final int offset) {
        return locator.getLocation(offset);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final void mark() {
        if (markOffset >= markLocations.length) {
            expandMarkLocations();
        }
        markLocations[markOffset++] = offset;
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
        markLocations[markOffset - 1] = offset;
        locator.mark();
    }

    @Override
    public final boolean isMarked() {
        return markOffset > 0;
    }

    @Override
    public void rewind() {
        offset = markLocations[--markOffset];
        locator.rewind();
        LA1x();
    }

    /**
     * Rewind character input stream to the given offset.
     */
    @Override
    public void rewind(final int offset) {
        this.offset = offset;
        locator.rewind(offset);
        LA1x();
    }

    ////////////////////////////////////////////////////////////

    public void setCutoff() {
    }

    @Override
    public int addCutoff() {
        return offset;
    }

    @Override
    public void addCutoff(final int offset) {
    }

    @Override
    public boolean removeCutoff(final int offset) {
        return false;
    }

    ////////////////////////////////////////////////////////////

    /**
     * CharSequence.length()
     */
    @Override
    public final int length() {
        return offset;
    }

    @Override
    public final CharSequence subSequence(final int start, final int end) {
        return getSource(start, end);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final CharSequence getSource(final int start) {
        return getSource(start, offset);
    }

    @Override
    public final void newline() {
        locator.newline(offset);
    }

    /**
     * NOTE: When tab() is called by user action, \t should have been consumed,
     * so here offset is decremented.
     */
    @Override
    public final void tab() {
        locator.tab(offset - 1);
    }

    @Override
    public void consume() throws LLKParseError {
        ++offset;
        LA1x();
    }

    @Override
    public final int LA1Consume() {
        final int ret = la1;
        consume();
        return ret;
    }

    @Override
    public final int consumeLA0() {
        int la0 = la1;
        consume();
        return la0;
    }

    @Override
    public final int consumeLA1() {
        consume();
        return la1;
    }

    @Override
    public final boolean matchOpt(final char c) {
        if (la1 != c) {
            return false;
        }
        consume();
        return true;
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
        if (la1 != c1 && la1 != c2 && la1 != c3) {
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
    public void match(final char[] s) throws LLKParseException {
        final int len = s.length;
        for (int i = 0; i < len; i++) {
            if (la1 != s[i]) {
                throw llkMismatchException(
                    "match(char[]): expected string=" + new String(s) + ", char=" + s[i] + ", actual=" + _toString(la1));
            }
            consume();
        }
    }

    @Override
    public void match(final char[] s, final int start) throws LLKParseException {
        consume(start);
        final int len = s.length;
        for (int i = start; i < len; i++) {
            if (la1 != s[i]) {
                throw llkMismatchException(
                    "match(char[], int): expected string="
                        + new String(s)
                        + ", char="
                        + s[i]
                        + ", actual="
                        + _toString(la1));
            }
            consume();
        }
    }

    @Override
    public void replace(final int start, final int end, final char[] text) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final char[] text, final int tstart, final int tend) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final ICharSequence text) {
        throw new UnsupportedOperationException("Lexer input buffer read only");
    }

    ////////////////////////////////////////////////////////////

    protected LLKParseException llkMismatchException(final String msg) {
        return new LLKParseException(msg, locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(final String msg, final int[] bset) {
        return new LLKParseException(msg + _toString(bset) + ", actual=" + LA1(), locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(final String msg, final int c) {
        return new LLKParseException(
            msg + '\'' + _toString(c) + "', actual='" + _toString(LA1()) + '\'',
            locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int first, final int last, final int c) {
        return new LLKParseException(
            msg + "('" + _toString(first) + "', '" + _toString(last) + "'), actual='" + _toString(c) + '\'',
            locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int c3, final int c) {
        return new LLKParseException(
            msg
                + "('"
                + _toString(c1)
                + "', '"
                + _toString(c2)
                + "', '"
                + _toString(c3)
                + "'), actual='"
                + _toString(c)
                + '\'',
            locator.getLocation(offset)
        );
    }

    ////////////////////////////////////////////////////////////

    protected static char[] toLowerCase(final char[] a) {
        return toLowerCase(a, 0, a.length);
    }

    protected static char[] toLowerCase(final char[] a, final int start, final int end) {
        final char[] ret = new char[end - start];
        for (int i = end - 1; i >= start; --i) {
            ret[i] = Character.toLowerCase(a[i]);
        }
        return ret;
    }

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

    public static String _toString(final int[] bset) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bset.length; ++i) {
            if (i != 0) {
                buf.append(", 0x");
            } else {
                buf.append("0x");
            }
            buf.append(Integer.toHexString(bset[i]));
        }
        return buf.toString();
    }

    public static String _toString(final int c) {
        if (c == -1) {
            return "EOF";
        }
        if (c >= ' ' && c < 0x7f) {
            return String.valueOf((char) c);
        }
        return String.format("\\u%04x", c);
    }

    ////////////////////////////////////////////////////////////

    private void expandMarkLocations() {
        markLocations = Arrays.copyOf(markLocations, markLocations.length * 2);
    }

    ////////////////////////////////////////////////////////////

}
