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

public abstract class AbstractTokenHandler implements ILLKTokenHandler {

    protected INamePool namePool;
    protected int[] specialTokens;
    protected ILLKToken token0;
    protected ILLKToken specialHead;
    protected ILLKToken specialTail;

    public AbstractTokenHandler(final INamePool pool, final int[] specialtokens) {
        init(pool, specialtokens);
    }

    protected AbstractTokenHandler(final int[] specialtokens) {
        init(new NamePool(32, 2.0f), specialtokens);
    }

    protected void init(final INamePool namepool, final int[] specialtokens) {
        namePool = namepool;
        specialTokens = specialtokens;
        token0 = new CustomTextToken(2, 0, 0, "");
    }

    @Override
    public void setLexerInput(final ILLKLexerInput input) {
    }

    @Override
    public INamePool namePool() {
        return namePool;
    }

    @Override
    public boolean yieldToken(final ILLKToken t) {
        if (t.getType() == ILLKConstants.TOKEN_TYPE_IGNORE) {
            return false;
        }
        return yell(t.isSpecial(), t);
    }

    @Override
    public boolean yieldToken(final int type, final int start, final int end) {
        if (type == ILLKConstants.TOKEN_TYPE_IGNORE) {
            return false;
        }
        final boolean isspecial = specialTokens != null && llkGetBit(type, specialTokens);
        return yell(
            isspecial, isspecial ? createSpecialToken(type, start, end) : createToken(type, start, end));
    }

    @Override
    public boolean yieldToken(final int type, final int start, final int end, final Object data) {
        if (type == ILLKConstants.TOKEN_TYPE_IGNORE) {
            return false;
        }
        final boolean isspecial = specialTokens != null && llkGetBit(type, specialTokens);
        return yell(
            isspecial,
            isspecial ? createSpecialToken(type, start, end, data) : createToken(type, start, end, data));
    }

    @Override
    public boolean yieldToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        if (type == ILLKConstants.TOKEN_TYPE_IGNORE) {
            return false;
        }
        final boolean isspecial = specialTokens != null && llkGetBit(type, specialTokens);
        return yell(
            isspecial,
            isspecial
                ? createSpecialToken(type, start, end, text, data)
                : createToken(type, start, end, text, data)
        );
    }

    @Override
    public boolean yieldTextToken(final int type, final int start, final int end, final CharSequence text) {
        if (type == ILLKConstants.TOKEN_TYPE_IGNORE) {
            return false;
        }
        final boolean isspecial = specialTokens != null && llkGetBit(type, specialTokens);
        return yell(
            isspecial,
            isspecial
                ? createSpecialTextToken(type, start, end, text)
                : createTextToken(type, start, end, text)
        );
    }

    @Override
    public void rewind(final ILLKToken lt0) {
        final ILLKToken lt1 = lt0.getNext();
        token0 = lt0;
        specialHead = null;
        if (lt1 != null) {
            specialHead = lt1.getSpecial();
        }
        specialTail = specialHead;
        if (specialTail != null) {
            for (ILLKToken t = specialTail.getNext(); t != null; t = t.getNext()) {
                specialTail = t;
            }
        }
    }

    @Override
    public void rewind(final int offset) {
        throw new UnsupportedOperationException("Use rewind(ILLKToken)");
    }

    @Override
    public ILLKToken getToken0() {
        return token0;
    }

    @Override
    public int getType0() {
        return token0.getType();
    }

    @Override
    public int getOffset0() {
        return token0.getOffset();
    }

    @Override
    public int getEndOffset0() {
        return token0.getEndOffset();
    }

    @Override
    public CharSequence getText0() {
        return token0.getText();
    }

    @Override
    public Object getData0() {
        return token0.getData();
    }

    @Override
    public void reset() {
        token0 = createToken(2, 0, 0);
        specialHead = null;
        specialTail = null;
    }

    @Override
    public Object clone() {
        AbstractTokenHandler ret;
        try {
            ret = (AbstractTokenHandler) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new LLKParseError("Error cloning: " + this, e);
        }
        ret.token0 = (ILLKToken) token0.clone();
        ret.specialHead = null;
        ret.specialTail = null;
        if (specialHead != null) {
            ret.specialHead = (ILLKToken) specialHead.clone();
            ret.specialTail = ret.specialHead;
            for (ILLKToken t = ret.specialHead.getNext(); t != null; t = t.getNext()) {
                ret.specialTail = t;
            }
        }
        return ret;
    }

    ////////////////////////////////////////////////////////////////////////

    protected final boolean yell(final boolean isspecial, final ILLKToken t) {
        if (isspecial) {
            if (specialHead == null) {
                specialHead = specialTail = t;
            } else {
                specialTail.setNext(t);
                specialTail = t;
            }
            return false;
        }
        if (specialHead != null) {
            t.setSpecial(specialHead);
            specialHead = specialTail = null;
        }
        token0.setNext(t);
        token0 = t;
        return true;
    }

    ////////////////////////////////////////////////////////////////////////

    protected static boolean llkGetBit(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
    }

    ////////////////////////////////////////////////////////////////////////

    public abstract static class AbstractToken implements ILLKToken, Cloneable {

        public int type;
        public ILLKToken next;
        protected int start, end;

        ////////////////////////////////////////////////////////////////////////

        public AbstractToken(final int type, final int start, final int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }

        ////////////////////////////////////////////////////////////////////////

        @Override
        public Object clone() {
            try {
                final AbstractToken ret = (AbstractToken) super.clone();
                ret.next = null;
                return ret;
            } catch (final CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Iterable<ILLKToken> specials() {
            return new EmptyIterable<>();
        }

        ////////////////////////////////////////////////////////////////////////

        @Override
        public int getType() {
            return type;
        }

        @Override
        public void setType(final int type) {
            this.type = type;
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
        public void setNext(final ILLKToken t) {
            next = t;
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(final Object data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getData() {
            return null;
        }

        @Override
        public int getOffset() {
            return start;
        }

        @Override
        public int getEndOffset() {
            return end;
        }

        @Override
        public int getLength() {
            return end - start;
        }

        @Override
        public void setOffset(final int offset) {
            start = offset;
        }

        @Override
        public void setEndOffset(final int offset) {
            end = offset;
        }

        @Override
        public CharSequence getText() {
            return null;
        }

        @Override
        public void setText(final CharSequence text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocationString() {
            return "(" + start + "-" + end + ")";
        }

        ////////////////////////////////////////////////////////////////////////

        @Override
        public String toString() {
            return getClass().getName()
                + "(type="
                + type
                + ", start="
                + start
                + ", end="
                + end
                + ", text="
                + getText()
                + ")";
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomTextToken extends AbstractToken {

        protected ILLKToken special;
        protected CharSequence text;

        public CustomTextToken(final int type, final int start, final int end, final CharSequence text) {
            super(type, start, end);
            this.text = text;
        }

        @Override
        public void setText(final CharSequence s) {
            text = s;
        }

        @Override
        public CharSequence getText() {
            return text;
        }

        @Override
        public boolean isSpecial() {
            return false;
        }

        @Override
        public ILLKToken getSpecial() {
            return special;
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            special = t;
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomTextDataToken extends AbstractToken {

        protected ILLKToken special;
        protected CharSequence text;
        protected Object data;

        public CustomTextDataToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            super(type, start, end);
            this.text = text;
            this.data = data;
        }

        @Override
        public void setText(final CharSequence s) {
            text = s;
        }

        @Override
        public CharSequence getText() {
            return text;
        }

        @Override
        public boolean isSpecial() {
            return false;
        }

        @Override
        public ILLKToken getSpecial() {
            return special;
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            special = t;
        }

        @Override
        public String toString() {
            return text.toString();
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public void setData(final Object data) {
            this.data = data;
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomSpecialTextToken extends AbstractToken {

        private CharSequence text;

        public CustomSpecialTextToken(final int type, final int start, final int end, final CharSequence text) {
            super(type, start, end);
            this.text = text;
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public ILLKToken getSpecial() {
            return null;
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return text;
        }

        @Override
        public void setText(final CharSequence text) {
            this.text = text;
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomSpecialTextDataToken extends AbstractToken {

        CharSequence text;
        Object data;

        public CustomSpecialTextDataToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            super(type, start, end);
            this.text = text;
            this.data = data;
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public ILLKToken getSpecial() {
            return null;
        }

        @Override
        public void setSpecial(final ILLKToken t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return text;
        }

        @Override
        public void setText(final CharSequence text) {
            this.text = text;
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public void setData(final Object data) {
            this.data = data;
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
