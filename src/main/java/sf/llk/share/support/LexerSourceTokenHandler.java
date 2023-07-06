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
 * Custom token handler create custom tokens with minimal size and use lexer input source for source text.
 */
public class LexerSourceTokenHandler extends AbstractTokenHandler {

    ////////////////////////////////////////////////////////////////////////

    final char[] source;

    ////////////////////////////////////////////////////////////////////////

    public LexerSourceTokenHandler(final char[] source, final int[] specialtokens) {
        super(specialtokens);
        this.source = source;
    }

    public LexerSourceTokenHandler(final char[] source, final INamePool namepool, final int[] specialtokens) {
        super(namepool, specialtokens);
        this.source = source;
    }

    @Override
    public ILLKToken createToken(final int type, final int start, final int end) {
        return new CustomToken(type, start, end, source);
    }

    @Override
    public ILLKToken createToken(final int type, final int start, final int end, final Object data) {
        return new CustomDataToken(type, start, end, source, data);
    }

    @Override
    public ILLKToken createTextToken(final int type, final int start, final int end, final CharSequence text) {
        return new CustomTextToken(type, start, end, text);
    }

    @Override
    public ILLKToken createToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        return new CustomTextDataToken(type, start, end, text, data);
    }

    @Override
    public ILLKToken createSpecialToken(final int type, final int start, final int end) {
        return new CustomSpecialToken(type, start, end, source);
    }

    @Override
    public ILLKToken createSpecialToken(final int type, final int start, final int end, final Object data) {
        return new CustomSpecialDataToken(type, start, end, source, data);
    }

    @Override
    public ILLKToken createSpecialTextToken(
        final int type, final int start, final int end, final CharSequence text) {
        return new CustomSpecialTextToken(type, start, end, text);
    }

    @Override
    public ILLKToken createSpecialToken(
        final int type, final int start, final int end, final CharSequence text, final Object data) {
        return new CustomSpecialTextDataToken(type, start, end, text, data);
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomSpecialToken extends AbstractToken implements CharSequence {

        protected final char[] source;

        public CustomSpecialToken(final int type, final int start, final int end, final char[] source) {
            super(type, start, end);
            this.source = source;
        }

        @Override
        public void setText(final CharSequence s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return this;
        }

        @Override
        public char charAt(final int index) {
            return source[start + index];
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new CharRange(source, this.start + start, this.start + end);
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
        public String toString() {
            return new String(source, start, end - start);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomSpecialDataToken extends CustomSpecialToken {

        private Object data;

        public CustomSpecialDataToken(
            final int type, final int start, final int end, final char[] source, final Object data) {
            super(type, start, end, source);
            this.data = data;
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

    public static class CustomToken extends AbstractToken implements CharSequence {

        protected final char[] source;
        protected ILLKToken special;

        public CustomToken(final int type, final int start, final int end, final char[] source) {
            super(type, start, end);
            this.source = source;
        }

        @Override
        public void setText(final CharSequence s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return this;
        }

        @Override
        public char charAt(final int index) {
            return source[start + index];
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new CharRange(source, this.start + start, this.start + end);
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
            return new String(source, start, end - start);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public static class CustomDataToken extends AbstractToken implements CharSequence {

        protected final char[] source;
        protected ILLKToken special;
        protected Object data;

        public CustomDataToken(
            final int type, final int start, final int end, final char[] source, final Object data) {
            super(type, start, end);
            this.source = source;
            this.data = data;
        }

        @Override
        public void setText(final CharSequence s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return this;
        }

        @Override
        public char charAt(final int index) {
            return source[start + index];
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new CharRange(source, this.start + start, this.start + end);
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
            return new String(source, start, end - start);
        }

        @Override
        public void setData(final Object data) {
            this.data = data;
        }

        @Override
        public Object getData() {
            return data;
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
