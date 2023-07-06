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
 * A token handler that implement the classic LLK token list generation.
 */
public class DefaultTokenHandler {

    public static ILLKTokenHandler getInstance(
        final ILLKLexerInput input, final int[] specialtokens, final boolean usetext, final boolean useintern) {
        if (usetext) {
            if (useintern) {
                return new WithTextInternTokenHandler(input, specialtokens);
            }
            return new WithTextTokenHandler(input, specialtokens);
        }
        return new WithoutTextTokenHandler(specialtokens);
    }

    public static ILLKTokenHandler getInstance(
        final ILLKLexerInput input, final int[] specialtokens, final boolean usetext) {
        return usetext
            ? new WithTextTokenHandler(input, specialtokens)
            : new WithoutTextTokenHandler(specialtokens);
    }

    public static ILLKTokenHandler getInstance() {
        return new WithoutTextTokenHandler(null);
    }

    private static class WithTextTokenHandler extends AbstractTokenHandler {

        protected final ILLKLexerInput lexerInput;

        public WithTextTokenHandler(final ILLKLexerInput input, final int[] specialtokens) {
            super(specialtokens);
            lexerInput = input;
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end) {
            final String text = lexerInput.getSource(start, end).toString();
            return new LLKToken(type, start, end, text);
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end, final Object data) {
            final String text = lexerInput.getSource(start, end).toString();
            return new LLKToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new LLKToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createTextToken(
            final int type, final int start, final int end, final CharSequence text) {
            return new LLKToken(type, start, end, text, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end) {
            final String text = lexerInput.getSource(start, end).toString();
            return new CustomSpecialTextDataToken(type, start, end, text, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end, final Object data) {
            final String text = lexerInput.getSource(start, end).toString();
            return new CustomSpecialTextDataToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createSpecialToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new CustomSpecialTextDataToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createSpecialTextToken(
            final int type, final int start, final int end, final CharSequence text) {
            return new CustomSpecialTextDataToken(type, start, end, text, null);
        }
    }

    private static class WithTextInternTokenHandler extends AbstractTokenHandler {

        static final int LLK_INTERN_SIZE = 8;
        protected final ILLKLexerInput lexerInput;

        public WithTextInternTokenHandler(final ILLKLexerInput input, final int[] specialtokens) {
            super(new NamePool(512, 2.0f), specialtokens);
            lexerInput = input;
            specialTokens = specialtokens;
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end) {
            CharSequence text;
            if (end - start <= LLK_INTERN_SIZE) {
                text = namePool.intern(lexerInput, start, end);
            } else {
                text = lexerInput.getSource(start, end);
            }
            return new LLKToken(type, start, end, text);
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end, final Object data) {
            CharSequence text;
            if (end - start <= LLK_INTERN_SIZE) {
                text = namePool.intern(lexerInput, start, end);
            } else {
                text = lexerInput.getSource(start, end);
            }
            return new LLKToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new LLKToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createTextToken(
            final int type, final int start, final int end, final CharSequence text) {
            return new LLKToken(type, start, end, text, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end) {
            CharSequence text;
            if (end - start <= LLK_INTERN_SIZE) {
                text = namePool.intern(lexerInput, start, end);
            } else {
                text = lexerInput.getSource(start, end);
            }
            return new CustomSpecialTextDataToken(type, start, end, text, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end, final Object data) {
            CharSequence text;
            if (end - start <= LLK_INTERN_SIZE) {
                text = namePool.intern(lexerInput, start, end);
            } else {
                text = lexerInput.getSource(start, end);
            }
            return new CustomSpecialTextDataToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createSpecialToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new CustomSpecialTextDataToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createSpecialTextToken(
            final int type, final int start, final int end, CharSequence text) {
            if (end - start <= LLK_INTERN_SIZE) {
                text = namePool.intern(text.toString());
            }
            return new CustomSpecialTextDataToken(type, start, end, text, null);
        }
    }

    private static class WithoutTextTokenHandler extends AbstractTokenHandler {

        public WithoutTextTokenHandler(final int[] specialtokens) {
            super(specialtokens);
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end) {
            return new LLKToken(type, start, end);
        }

        @Override
        public ILLKToken createToken(final int type, final int start, final int end, final Object data) {
            return new LLKToken(type, start, end, null, data);
        }

        @Override
        public ILLKToken createToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new LLKToken(type, start, end, text, data);
        }

        @Override
        public ILLKToken createTextToken(
            final int type, final int start, final int end, final CharSequence text) {
            return new LLKToken(type, start, end, text, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end) {
            return new CustomSpecialTextDataToken(type, start, end, null, null);
        }

        @Override
        public ILLKToken createSpecialToken(final int type, final int start, final int end, final Object data) {
            return new CustomSpecialTextDataToken(type, start, end, null, data);
        }

        @Override
        public ILLKToken createSpecialToken(
            final int type, final int start, final int end, final CharSequence text, final Object data) {
            return new CustomSpecialTextDataToken(type, start, end, null, data);
        }

        @Override
        public ILLKToken createSpecialTextToken(
            final int type, final int start, final int end, final CharSequence text) {
            return new CustomSpecialTextDataToken(type, start, end, text, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
