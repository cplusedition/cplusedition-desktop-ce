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

import java.io.IOException;
import java.io.OutputStream;

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public class LLKBinaryLexerInput extends AbstractBinaryLexerInput {

    ////////////////////////////////////////////////////////////

    private static final int MARKER_SIZE = 1;

    ////////////////////////////////////////////////////////////

    private final byte[] source;
    private final int length;
    private int markOffset;
    private int[] markLocations;
    private int la1;

    ////////////////////////////////////////////////////////////

    public LLKBinaryLexerInput(final byte[] a, final ILLKMain main) {
        super(main);
        source = a;
        length = source.length;
        markLocations = new int[MARKER_SIZE * 32];
        LA1x();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKBinaryLexerInput ret = (LLKBinaryLexerInput)super.clone();
        ret.markLocations = markLocations.clone();
        return ret;
    }

    @Override
    public void reset() {
        offset = 0;
        markOffset = 0;
        locator.rewind(0);
        LA1x();
    }

    public void setIgnoreCase(final boolean ignorecase) {
        throw new UnsupportedOperationException();
    }

    public boolean isIgnoreCase() {
        return false;
    }

    public int getSourceLength() {
        return length;
    }

    @Override
    public final void mark() {
        if (markOffset >= markLocations.length) {
            markLocations = expand(markLocations);
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
    public final void rewind() {
        offset = markLocations[--markOffset];
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
    public final void rewind(final int offset) {
        this.offset = offset;
        locator.rewind(offset);
        LA1x();
    }

    @Override
    public final void consume() throws LLKParseError {
        ++offset;
        LA1x();
    }

    @Override
    public final void consume(final int n) throws LLKParseError {
        offset += n;
        LA1x();
    }

    @Override
    public final int LA0() {
        if (offset == 0) {
            return ILLKConstants.LEXER_EOF;
        }
        return source[offset - 1] & 0xff;
    }

    @Override
    public final int LA1() {
        return la1;
    }

    @Override
    public final int LA(int n) {
        n += offset - 1;
        if (n < length) {
            return source[n] & 0xff;
        }
        return ILLKConstants.LEXER_EOF;
    }

    @Override
    public final boolean LA(final byte[] expected) {
        int index = offset;
        final int len = expected.length;
        if (index + len > length) {
            return false;
        }
        for (int i = 0; i < len; ++i, ++index) {
            if (source[index] != expected[i]) {
                return false;
        }}
        return true;
    }

    /**
     * @param index Start from LA(index)
     */
    @Override
    public final boolean LA(final byte[] expected, int index) {
        index += offset - 1;
        final int len = expected.length;
        if (index + len > length) {
            return false;
        }
        for (int i = 0; i < len; ++i, ++index) {
            if (source[index] != expected[i]) {
                return false;
        }}
        return true;
    }

    private void LA1x() {
        if (offset < length) {
            la1 = source[offset] & 0xff;
        } else if (offset == length) {
            la1 = ILLKConstants.LEXER_EOF;
        } else {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + offset);
    }}

    ////////////////////////////////////////////////////////////

    @Override
    public int size() {
        return offset;
    }

    @Override
    public byte byteAt(final int index) {
        return source[index];
    }

    @Override
    public void getBytes(final int start, final int end, final byte[] dst, final int dststart) {
        System.arraycopy(source, start, dst, dststart, end - start);
    }

    @Override
    public void write(final OutputStream os, final int start, final int end) throws IOException {
        os.write(source, start, end - start);
    }

    @Override
    public IByteSequence subSequence(final int start, final int end) {
        return new ByteRange(source, start, end);
    }

    @Override
    public byte[] toArray() {
        return source.clone();
    }

    /**
     * CAUTION: Don't modifiy the char[] returned.
     */
    @Override
    public final byte[] getSource() {
        return source;
    }

    @Override
    public final byte[] getSource(final int start) {
        return getSource(start, offset);
    }

    @Override
    public final byte[] getSource(final int start, final int end) {
        final int len = end - start;
        final byte[] ret = new byte[len];
        System.arraycopy(source, start, ret, 0, len);
        return ret;
    }

    @Override
    public IByteSequence getSourceSequence() {
        return new ByteRange(source);
    }

    @Override
    public IByteSequence getSourceSequence(final int start, final int end) {
        return new ByteRange(source, start, end);
    }

    @Override
    public IByteSequence getSourceSequence(final int start) {
        return new ByteRange(source, start, offset);
    }

    @Override
    public void replace(final int start, final int end, final byte[] text) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final byte[] text, final int tstart, final int tend) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final IByteSequence text) {
        throw new UnsupportedOperationException("Lexer input buffer read only");
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final boolean matchOpt(final byte c) {
        if (offset >= length || la1 != c) {
            return false;
        }
        ++offset;
        LA1x();
        return true;
    }

    @Override
    public final int LA1Consume() {
        final int ret = la1;
        ++offset;
        LA1x();
        return ret;
    }

    @Override
    public final int consumeLA0() {
        int la0 = la1;
        ++offset;
        LA1x();
        return la0;
    }

    @Override
    public final int consumeLA1() {
        ++offset;
        LA1x();
        return la1;
    }

    @Override
    public final short consume2BE() {
        if (offset + 2 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 2));
        }
        ++offset;
        final short ret = (short)((la1 << 8) | source[offset++] & 0xff);
        LA1x();
        return ret;
    }

    @Override
    public final short consume2LE() {
        if (offset + 2 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 2));
        }
        ++offset;
        final short ret = (short)(la1 & 0xff | source[offset++] << 8);
        LA1x();
        return ret;
    }

    @Override
    public final int consumeU2BE() {
        if (offset + 2 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 2));
        }
        ++offset;
        final int ret = ((la1 << 8) | source[offset++] & 0xff) & 0xffff;
        LA1x();
        return ret;
    }

    @Override
    public final int consumeU2LE() {
        if (offset + 2 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 2));
        }
        ++offset;
        final int ret = (la1 & 0xff | source[offset++] << 8) & 0xffff;
        LA1x();
        return ret;
    }

    @Override
    public final int consume4BE() {
        if (offset + 4 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 4));
        }
        ++offset;
        final int ret = (la1 << 8 | source[offset++] & 0xff) << 16
            | (source[offset++] << 8 | source[offset++] & 0xff) & 0xffff;
        LA1x();
        return ret;
    }

    @Override
    public final int consume4LE() {
        if (offset + 4 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 4));
        }
        ++offset;
        final int ret = (la1 & 0xff | source[offset++] << 8) & 0xffff
            | (source[offset++] & 0xff | source[offset++] << 8) << 16;
        LA1x();
        return ret;
    }

    @Override
    public final long consume8BE() {
        if (offset + 8 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 8));
        }
        ++offset;
        final long ret = ((la1 << 8 | source[offset++] & 0xffL) << 16
            | (source[offset++] << 8 | source[offset++] & 0xff) & 0xffffL) << 32
            | ((source[offset++] << 8 | source[offset++] & 0xffL) << 16
                | (source[offset++] << 8 | source[offset++] & 0xff) & 0xffffL) & 0xffffffffL;
        LA1x();
        return ret;
    }

    @Override
    public final long consume8LE() {
        if (offset + 8 > length) {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + (offset + 8));
        }
        ++offset;
        final long ret = ((la1 & 0xffL | source[offset++] << 8) & 0xffffL
            | (source[offset++] & 0xffL | source[offset++] << 8) << 16) & 0xffffffffL
            | ((source[offset++] & 0xffL | source[offset++] << 8) & 0xffffL
                | (source[offset++] & 0xffL | source[offset++] << 8) << 16) << 32;
        LA1x();
        return ret;
    }

    @Override
    public final void match(final int c) throws LLKParseException {
        if (offset >= length || la1 != c) {
            throw llkMismatchException("match(byte): expected=", c);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void matchNot(final int c) throws LLKParseException {
        if (la1 == c) {
            throw llkMismatchException("matchNot(byte): not expected=", c);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void match(final int c1, final int c2) throws LLKParseException {
        if (la1 != c1 && la1 != c2) {
            throw llkMismatchException("match(byte, byte): expected=", c1, c2, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void matchNot(final int c1, final int c2) throws LLKParseException {
        if (la1 == c1 || la1 == c2) {
            throw llkMismatchException("matchNot(byte, byte): not expected=", c1, c2, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void match(final int c1, final int c2, final int c3) throws LLKParseException {
        if (la1 != c1 && la1 != c2 && la1 != c3) {
            throw llkMismatchException("match(byte, byte, byte): expected=", c1, c2, c3, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void matchNot(final int c1, final int c2, final int c3) throws LLKParseException {
        if (la1 == c1 || la1 == c2 || la1 == c3) {
            throw llkMismatchException("matchNot(byte, byte, byte): not expected=", c1, c2, c3, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void matchRange(final int first, final int last) throws LLKParseException {
        if (la1 < first || la1 > last) {
            throw llkMismatchException("matchRange(byte, byte): range=", first, last, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void matchNotRange(final int first, final int last) throws LLKParseException {
        if (la1 >= first && la1 <= last) {
            throw llkMismatchException("matchNotRange(byte, byte): range=", first, last, la1);
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void match(final int[] bset) throws LLKParseException {
        if (llkGetBit(LA1(), bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
    }}

    @Override
    public final void matchNot(final int[] bset) throws LLKParseException {
        if (llkGetBitInverted(LA1(), bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
    }}

    @Override
    public final void match(final byte[] a) throws LLKParseException {
        final int len = a.length;
        byte c;
        for (int i = 0, index = offset; i < len; ++i, ++index) {
            c = (index >= length ? ILLKConstants.LEXER_EOF : source[index]);
            if (c != a[i]) {
                throw llkMismatchException(
                    String.format(
                        "match(byte[]): a=%s, i=%d, expected=0x%02x, actual=0x%02x", _toString(a), i, a[i], c));
        }}
        consume(len);
    }

    @Override
    public final void match(final byte[] a, final int start) throws LLKParseException {
        consume(start);
        final int len = a.length;
        byte c;
        for (int i = start, index = offset; i < len; ++i, ++index) {
            c = (index >= length ? ILLKConstants.LEXER_EOF : source[index]);
            if (c != a[i]) {
                throw llkMismatchException(
                    String.format(
                        "match(byte[]): a=%s, i=%d, expected=0x%02x, actual=0x%02x", _toString(a), i, a[i], c));
        }}
        consume(len - start);
    }

    ////////////////////////////////////////////////////////////

    private int[] expand(final int[] a) {
        final int[] ret = new int[a.length * 2];
        System.arraycopy(a, 0, ret, 0, a.length);
        return ret;
    }

    ////////////////////////////////////////////////////////////
}
