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
 * LLKLexerBufferInput is similar to LLKLexerInput but it keep a buffer that is larger than the
 * input source and allow efficient macro expansion.
 *
 * [*****----------------------*****]
 *           ^offset                 ^laOffset
 *
 */
public class LLKLexerBufferInput extends AbstractLexerInput {

    ////////////////////////////////////////////////////////////

    private static final boolean CHECK = true;
    private static final int MIN_SIZE = 0;

    ////////////////////////////////////////////////////////////

    private final char[] original;
    private final int originalStart;
    private char[] source;
    private char[] laSource;
    private int length;
    private int laOffset;
    private int laLength;

    ////////////////////////////////////////////////////////////

    public LLKLexerBufferInput(final char[] a, final ILLKMain main) {
        this(a, 0, main);
    }

    public LLKLexerBufferInput(final char[] a, final int start, final ILLKMain main) {
        super(main);
        original = a;
        originalStart = start;
        source = a.clone();
        laSource = source;
        laOffset = start;
        length = source.length - start;
        laLength = length;
        LA1x();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKLexerBufferInput ret = (LLKLexerBufferInput) super.clone();
        ret.source = source.clone();
        if (laSource != source) {
            ret.laSource = laSource.clone();
        } else {
            ret.laSource = ret.source;
        }
        ret.markLocations = markLocations.clone();
        return ret;
    }

    @Override
    public void reset() {
        super.reset();
        if (laSource != source) {
            source = original.clone();
            laSource = toLowerCase(original, 0, original.length);
        } else {
            source = original.clone();
            laSource = source;
        }
        offset = 0;
        laOffset = originalStart;
        length = source.length - originalStart;
        laLength = length;
        markOffset = 0;
        locator.rewind(0);
        LA1x();
    }

    public int getSourceLength() {
        return length;
    }

    @Override
    public final void rewind() {
        final int newoffset = markLocations[--markOffset];
        if (offset == laOffset) {
            offset = laOffset = newoffset;
        } else if (laSource == source) {
            while (offset > newoffset) {
                source[--laOffset] = source[--offset];
            }
        } else {
            while (offset > newoffset) {
                source[--laOffset] = source[--offset];
                laSource[laOffset] = laSource[offset];
            }
        }
        locator.rewind();
        LA1x();
    }

    /**
     * Rewind character input stream to the given offset.
     */
    @Override
    public final void rewind(final int newoffset) {
        if (newoffset > offset) {
            throw new AssertionError(
                "Cannot rewind to a forward location: offset=" + offset + ", newoffset=" + newoffset);
        }
        if (offset == laOffset) {
            offset = laOffset = newoffset;
        } else if (laSource == source) {
            while (offset > newoffset) {
                source[--laOffset] = source[--offset];
            }
        } else {
            while (offset > newoffset) {
                source[--laOffset] = source[--offset];
                laSource[laOffset] = laSource[offset];
            }
        }
        locator.rewind(newoffset);
        LA1x();
    }

    @Override
    public final void consume() throws LLKParseError {
        if (offset == laOffset && offset < length) {
            ++offset;
            ++laOffset;
        } else if (offset < length) {
            if (laSource != source) {
                laSource[offset] = laSource[laOffset];
            }
            source[offset++] = source[laOffset++];
        } else {
            throw new LLKParseError("consume() passed EOF");
        }
        LA1x();
    }

    @Override
    public final void consume(int n) throws LLKParseError {
        if (offset + n > length) {
            offset = length;
            throw new LLKParseError("consume(int) passed EOF: length=" + length + ", offset=" + offset + ", n=" + n);
        }
        if (offset == laOffset) {
            offset += n;
            laOffset += n;
        } else {
            while (--n >= 0) {
                if (laSource != source) {
                    laSource[offset] = laSource[laOffset];
                }
                source[offset++] = source[laOffset++];
            }
        }
        LA1x();
    }

    @Override
    public final int consume(final IConsumer consumer) throws LLKParseError {
        int index = laOffset;
        for (; index < laLength; ++index) {
            if (!consumer.consume(laSource[index])) {
                consume(index - laOffset);
                return la1;
            }
        }
        consume(index - laOffset);
        return ILLKConstants.LEXER_EOF;
    }

    @Override
    public final int LA0() {
        if (offset == 0) {
            return ILLKConstants.LEXER_EOF;
        }
        return laSource[offset - 1];
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
        return laSource[n];
    }

    @Override
    public final boolean LA(final char[] expected) {
        int index = laOffset;
        final int len = expected.length;
        if (index + len > laLength) {
            return false;
        }
        for (int i = 0; i < len; ++i, ++index) {
            if (laSource[index] != expected[i]) {
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
            if (laSource[index] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final char charAt(final int index) {
        return source[index];
    }

    @Override
    public void getChars(final int sstart, final int send, final char[] dst, final int dstart) {
        System.arraycopy(source, sstart, dst, dstart, send - sstart);
    }

    @Override
    public final CharSequence getSource() {
        return new CharRange(source, 0, length);
    }

    @Override
    public final CharSequence getSource(final int start, final int end) {
        if (end > offset) {
            throw new AssertionError(
                "ASSERT: Cannot get source ahead of current offset: end=" + end + ", offset=" + offset);
        }
        return new CharRange(source, start, end);
    }

    @Override
    public void setIgnoreCase(final boolean ignorecase) {
        if (offset > 0) {
            throw new AssertionError("Ignore case can only be modified before lexing started");
        }
        if (ignorecase && laSource == source) {
            laSource = toLowerCase(source, laOffset, source.length);
        } else {
            laSource = source;
        }
        LA1x();
    }

    @Override
    public boolean isIgnoreCase() {
        return laSource != source;
    }

    @Override
    public void replace(final int start, final int end, final char[] text) {
        replace(start, end, text, 0, text.length);
    }

    @Override
    public void replace(final int start, final int end, char[] text, final int tstart, final int tend) {
        final int len = tend - tstart;
        final int delta = len - end + start;
        if (delta > laOffset - offset) {
            resize(source.length + Math.max(MIN_SIZE, Math.max(delta << 2, source.length)));
        }
        splitAt(end);
        offset = start;
        laOffset -= len;
        System.arraycopy(text, tstart, source, laOffset, len);
        if (laSource != source) {
            text = toLowerCase(text, tstart, tend);
            System.arraycopy(text, 0, laSource, laOffset, len);
        }
        length += delta;
        LA1x();
    }

    @Override
    public void replace(final int start, final int end, final ICharSequence text) {
        final int len = text.length();
        final int delta = len - end + start;
        if (delta > laOffset - offset) {
            resize(source.length + Math.max(MIN_SIZE, Math.max(delta << 2, source.length)));
        }
        splitAt(end);
        offset = start;
        laOffset -= len;
        text.getChars(0, len, source, laOffset);
        if (laSource != source) {
            text.getChars(0, len, laSource, laOffset);
            toLowerCase(laSource, laOffset, laOffset + len);
        }
        length += delta;
        LA1x();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final void match(final char[] s) throws LLKParseException {
        final int len = s.length;
        int c;
        for (int i = 0, index = laOffset; i < len; ++i, ++index) {
            c = (index >= laLength ? ILLKConstants.LEXER_EOF : laSource[index]);
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
            c = (index >= laLength ? ILLKConstants.LEXER_EOF : laSource[index]);
            if (c != s[i]) {
                throw llkMismatchException(
                    "match(char[], start): expected string=" + new String(s) + ", i=" + i + ", actual=" + _toString(c));
            }
        }
        consume(len - start);
    }

    ////////////////////////////////////////////////////////////

    protected void LA1x() {
        if (offset >= length) {
            la1 = ILLKConstants.LEXER_EOF;
        } else {
            la1 = laSource[laOffset];
        }
    }

    private void resize(final int newsize) {
        char[] a = new char[newsize];
        final int endoffset = laOffset + newsize - source.length;
        System.arraycopy(source, 0, a, 0, offset);
        System.arraycopy(source, laOffset, a, endoffset, source.length - laOffset);
        if (laSource != source) {
            source = a;
            a = new char[newsize];
            System.arraycopy(laSource, 0, a, 0, offset);
            System.arraycopy(laSource, laOffset, a, endoffset, laSource.length - laOffset);
            laSource = a;
        } else {
            source = a;
            laSource = a;
        }
        laLength = newsize;
        laOffset = endoffset;
    }

    private void splitAt(final int index) {
        if (offset == index) {
            return;
        }
        if (offset > index) {
            final int len = offset - index;
            laOffset -= len;
            System.arraycopy(source, index, source, laOffset, len);
            if (laSource != source) {
                System.arraycopy(laSource, index, laSource, laOffset, len);
            }
            offset = index;
        } else if (offset < index) {
            final int len = index - offset;
            System.arraycopy(source, laOffset, source, offset, len);
            if (laSource != source) {
                System.arraycopy(laSource, laOffset, laSource, offset, len);
            }
            offset += len;
            laOffset += len;
        }
    }

    ////////////////////////////////////////////////////////////
}
