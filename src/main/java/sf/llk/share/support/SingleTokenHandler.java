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

/**
 * A token handler that keep only a single token (the most recent yielded non-special token).
 */
public class SingleTokenHandler implements ILLKTokenHandler {

    ////////////////////////////////////////////////////////////

    private final SingleToken token1;
    private final INamePool namePool;

    ////////////////////////////////////////////////////////////

    public SingleTokenHandler(final ILLKLexerInput input) {
        token1 = new SingleToken(input);
        namePool = new NamePool(1024, 2.0f);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new LLKParseError(e);
        }
    }

    @Override
    public void setLexerInput(final ILLKLexerInput input) {
        token1.lexerInput = input;
    }

    @Override
    public void reset() {
    }

    @Override
    public void rewind(final ILLKToken token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rewind(final int offset) {
    }

    @Override
    public INamePool namePool() {
        return namePool;
    }

    ////////////////////////////////////////////////////////////

    /**
     * NOTE: This is the only yieldXXX() method that takes special tokens,
     * the other yieldXXX() methods only create normal tokens.
     * So lexer using this token handler must create special tokens with createSpecialXXX() explicitly.
     */
    @Override
    public boolean yieldToken(final ILLKToken token) {
        return token != ILLKConstants.IGNORE_TOKEN && !token1.isSpecial;
    }

    @Override
    public boolean yieldToken(final int type, final int start, final int end) {
        token1.set(type, start, end, null, null, false);
        return true;
    }

    @Override
    public boolean yieldToken(final int type, final int start, final int end, final Object data) {
        token1.set(type, start, end, null, data, false);
        return true;
    }

    @Override
    public boolean yieldToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        token1.set(type, start, end, text, data, false);
        return true;
    }

    @Override
    public boolean yieldTextToken(final int type, final int start, final int end, final CharSequence text) {
        token1.set(type, start, end, text, null, false);
        return true;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public ILLKToken createToken(final int type, final int start, final int end) {
        token1.set(type, start, end, null, null, false);
        return token1;
    }

    @Override
    public ILLKToken createToken(final int type, final int start, final int end, final Object data) {
        token1.set(type, start, end, null, data, false);
        return token1;
    }

    @Override
    public ILLKToken createToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        token1.set(type, start, end, text, data, false);
        return token1;
    }

    public ILLKToken createToken(
        final int type, final int start, final int end, final CharSequence text, final long data) {
        token1.set(type, start, end, text, data, false);
        token1.longValue = data;
        return token1;
    }

    public ILLKToken createToken(
        final int type, final int start, final int end, final CharSequence text, final double data) {
        token1.set(type, start, end, text, data, false);
        token1.doubleValue = data;
        return token1;
    }

    @Override
    public ILLKToken createTextToken(final int type, final int start, final int end, final CharSequence text) {
        token1.set(type, start, end, text, null, false);
        return token1;
    }

    @Override
    public ILLKToken createSpecialToken(final int type, final int start, final int end) {
        token1.set(type, start, end, null, null, true);
        return token1;
    }

    @Override
    public ILLKToken createSpecialToken(final int type, final int start, final int end, final Object data) {
        token1.set(type, start, end, null, data, true);
        return token1;
    }

    @Override
    public ILLKToken createSpecialToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        token1.set(type, start, end, text, data, true);
        return token1;
    }

    @Override
    public ILLKToken createSpecialTextToken(
        final int type, final int start, final int end, final CharSequence text) {
        token1.set(type, start, end, text, null, true);
        return token1;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public int getType0() {
        return token1.type;
    }

    @Override
    public int getOffset0() {
        return token1.offset;
    }

    @Override
    public int getEndOffset0() {
        return token1.endOffset;
    }

    @Override
    public CharSequence getText0() {
        return token1.getText();
    }

    @Override
    public Object getData0() {
        return token1.data;
    }

    @Override
    public ILLKToken getToken0() {
        return token1;
    }

    ////////////////////////////////////////////////////////////

    protected static boolean llkGetBit(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
    }

    public static class SingleToken implements ILLKToken, CharSequence, Cloneable {

        protected ILLKLexerInput lexerInput;
        protected int type;
        protected int offset;
        protected int endOffset;
        protected boolean isSpecial;
        protected CharSequence text;
        protected Object data;
        protected ILLKToken next;
        protected long longValue;
        protected double doubleValue;

        public SingleToken(final ILLKLexerInput input) {
            type = ILLKConstants.TOKEN_TYPE_INVALID;
            lexerInput = input;
        }

        public void set(
            final int type,
            final int start,
            final int end,
            final CharSequence text,
            final Object data,
            final boolean isspecial) {
            this.type = type;
            offset = start;
            endOffset = end;
            this.text = text;
            this.data = data;
            isSpecial = isspecial;
        }

        public void set(
            final int type,
            final int start,
            final int end,
            final CharSequence text,
            final long data,
            final boolean isspecial) {
            this.type = type;
            offset = start;
            endOffset = end;
            this.text = text;
            longValue = data;
            isSpecial = isspecial;
        }

        public void set(
            final int type,
            final int start,
            final int end,
            final CharSequence text,
            final double data,
            final boolean isspecial) {
            this.type = type;
            offset = start;
            endOffset = end;
            this.text = text;
            doubleValue = data;
            isSpecial = isspecial;
        }

        ////////////////////////////////////////////////////////////

        @Override
        public int getType() {
            return type;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public int getLength() {
            return endOffset - offset;
        }

        @Override
        public CharSequence getText() {
            if (text == null) {
                return this;
            }
            return text;
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public void setNext(final ILLKToken t) {
            next = t;
        }

        @Override
        public ILLKToken getNext() {
            return next;
        }

        @Override
        public ILLKToken getSpecial() {
            return null;
        }

        @Override
        public boolean isSpecial() {
            return isSpecial;
        }

        @Override
        public String getLocationString() {
            return "@" + offset;
        }

        ////////////////////////////////////////////////////////////

        @Override
        public void setType(final int type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOffset(final int offset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEndOffset(final int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setText(final CharSequence text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(final Object v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<ILLKToken> specials() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new LLKParseError(e);
            }
        }

        ////////////////////////////////////////////////////////////

        @Override
        public char charAt(final int index) {
            return lexerInput.charAt(offset + index);
        }

        @Override
        public int length() {
            return endOffset - offset;
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            if (text == null) {
                text = lexerInput.getSource(offset, endOffset);
            }
            return text.toString();
        }

        ////////////////////////////////////////////////////////////

        public int intValue() {
            return (int) longValue;
        }

        public long longValue() {
            return longValue;
        }

        public double doubleValue() {
            return doubleValue;
        }

        ////////////////////////////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////
}
