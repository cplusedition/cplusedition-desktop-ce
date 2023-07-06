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
public class SingleTokenParserInput implements ILLKParserInput, Cloneable {

    ////////////////////////////////////////////////////////////

    protected ILLKLexer lexer;
    protected IntList markLocations;
    protected ILLKToken token1;
    protected IntList laList;

    ////////////////////////////////////////////////////////////

    public SingleTokenParserInput(final ILLKLexer lexer) {
        this.lexer = lexer;
        markLocations = new IntList();
        token1 = null;
        laList = new IntList();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public Object clone() throws CloneNotSupportedException {
        final SingleTokenParserInput ret = (SingleTokenParserInput) super.clone();
        ret.lexer = (ILLKLexer) lexer.clone();
        ret.markLocations = (IntList) markLocations.clone();
        if (token1 != null) {
            ret.token1 = (ILLKToken) token1.clone();
        }
        ret.laList = new IntList();
        return ret;
    }

    @Override
    public void reset() {
        lexer.llkReset();
        markLocations.clear();
        laList.clear();
        token1 = null;
    }

    @Override
    public final void mark() {
        markLocations.add(lexer.llkGetOffset());
    }

    @Override
    public final void unmark() {
        markLocations.pop();
    }

    @Override
    public final void remark() {
        markLocations.swap(lexer.llkGetOffset());
    }

    @Override
    public final void rewind() {
        lexer.llkGetInput().rewind(markLocations.pop());
        laList.clear();
        token1 = null;
    }

    @Override
    public final boolean isMarked() {
        return markLocations.size() > 0;
    }

    @Override
    public final ILLKMain getMain() {
        return lexer.llkGetMain();
    }

    @Override
    public final ILLKLexer getLexer() {
        return lexer;
    }

    public final CharSequence getSource(final int start, final int end) {
        return lexer.llkGetInput().getSource(start, end);
    }

    @Override
    public final CharSequence getSource() {
        return lexer.llkGetInput().getSource();
    }

    @Override
    public final ISourceLocation getLocation(final int offset) {
        return lexer.llkGetInput().getLocation(offset);
    }

    @Override
    public final ISourceLocator getLocator() {
        return lexer.llkGetInput().getLocator();
    }

    @Override
    public final String getTokenName(final int type) {
        return lexer.llkGetTokenName(type);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final int LA0() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ILLKToken LT0() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int LA1() {
        if (token1 == null) {
            token1 = lexer.llkNextToken();
        }
        return token1.getType();
    }

    @Override
    public final ILLKToken LT1() {
        if (token1 == null) {
            token1 = lexer.llkNextToken();
        }
        return token1;
    }

    @Override
    public final int LA(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        if (n == 1) {
            return LA1();
        }
        --n;
        if (n <= laList.size()) {
            return laList.get(n - 1);
        }
        final int offset = token1.getOffset();
        laList.clear();
        for (int i = 0; i < n; ++i) {
            laList.add(lexer.llkNextToken().getType());
        }
        final int ret = laList.get(n - 1);
        lexer.llkGetInput().rewind(offset);
        token1 = lexer.llkNextToken();
        return ret;
    }

    @Override
    public final int consumeLA0() {
        int la0 = LA1();
        consume();
        return la0;
    }

    @Override
    public final int consumeLA1() {
        consume();
        return LA1();
    }

    @Override
    public final ILLKToken LT(final int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean matchOpt(final int type) {
        if (LA1() != type) {
            return false;
        }
        consume();
        return true;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void consume() {
        laList.clear();
        token1 = null;
    }

    @Override
    public void consume(int n) {
        laList.clear();
        while (--n > 0) {
            token1 = lexer.llkNextToken();
        }
        token1 = null;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final int getKeywordContexts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setKeywordContexts(final int context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setContext(final int context, final int state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void rewind(final ILLKToken lt0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDirectiveHandler(final IDirectiveHandler handler) {
        lexer.llkSetDirectiveHandler(handler);
    }

    @Override
    public final IDirectiveHandler getDirectiveHandler() {
        return lexer.llkGetDirectiveHandler();
    }

    ////////////////////////////////////////////////////////////
}
