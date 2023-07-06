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

public class LLKLexerInput extends AbstractLexerInput {

    ////////////////////////////////////////////////////////////

    private static final boolean CHECK = true;
    private static final int MARKER_SIZE = 1;

    ////////////////////////////////////////////////////////////

    private final char[] source;
    private char[] laSource;
    private final int length;

    ////////////////////////////////////////////////////////////

    public LLKLexerInput(final char[] a, final ILLKMain main) {
        super(main);
        source = a;
        laSource = a;
        length = source.length;
        LA1x();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (LLKLexerInput) super.clone();
    }

    @Override
    public void reset() {
        super.reset();
        offset = 0;
        locator.rewind(0);
        LA1x();
    }

    public int getSourceLength() {
        return length;
    }

    ////////////////////////////////////////////////////////////

    protected void LA1x() {
        if (offset < length) {
            la1 = laSource[offset];
        } else if (offset == length) {
            la1 = ILLKConstants.LEXER_EOF;
        } else {
            throw new LLKParseError("Reading passed EOF: length=" + length + ", offset=" + offset);
        }
    }

    @Override
    public final void consume(final int n) throws LLKParseError {
        offset += n;
        LA1x();
    }

    @Override
    public final int consume(final IConsumer consumer) throws LLKParseError {
        for (; offset < length; ++offset) {
            if (!consumer.consume(laSource[offset])) {
                LA1x();
                return la1;
            }
        }
        return la1 = ILLKConstants.LEXER_EOF;
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
        n += offset - 1;
        if (n < length) {
            return laSource[n];
        }
        return ILLKConstants.LEXER_EOF;
    }

    @Override
    public final boolean LA(final char[] expected) {
        int index = offset;
        final int len = expected.length;
        if (index + len > length) {
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
        index += offset - 1;
        final int len = expected.length;
        if (index + len > length) {
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
        return new CharRange(source);
    }

    @Override
    public final CharSequence getSource(final int start, final int end) {
        return new CharRange(source, start, end);
    }

    @Override
    public void setIgnoreCase(final boolean ignorecase) {
        if (ignorecase && laSource == source) {
            laSource = toLowerCase(source);
        } else {
            laSource = source;
        }
        LA1x();
    }

    @Override
    public boolean isIgnoreCase() {
        return laSource != source;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final void match(final char[] s) throws LLKParseException {
        final int len = s.length;
        int c;
        for (int i = 0, index = offset; i < len; ++i, ++index) {
            c = (index >= length ? ILLKConstants.LEXER_EOF : laSource[index]);
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
        for (int i = start, index = offset; i < len; ++i, ++index) {
            c = (index >= length ? ILLKConstants.LEXER_EOF : laSource[index]);
            if (c != s[i]) {
                throw llkMismatchException(
                    "match(char[], start): expected string=" + new String(s) + ", i=" + i + ", actual=" + _toString(c));
            }
        }
        consume(len - start);
    }

    ////////////////////////////////////////////////////////////
}
