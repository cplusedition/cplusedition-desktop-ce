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

public class LLKParserInput implements ILLKParserInput, Cloneable {

    ////////////////////////////////////////////////////////////

    private static final String NAME = "LLKParserInput";
    private static final boolean CHECK = true;
    private static final int MARKER_SIZE = 1;

    ////////////////////////////////////////////////////////////

    protected ILLKToken[] markLocations;
    protected int markOffset;
    protected ILLKLexer lexer;
    protected ILLKToken current;
    protected int la1 = -1;

    ////////////////////////////////////////////////////////////

    public LLKParserInput(final ILLKLexer lexer) {
        this.lexer = lexer;
        markLocations = new ILLKToken[MARKER_SIZE * 32];
        current = lexer.llkGetToken0();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKParserInput ret = (LLKParserInput) super.clone();
        ret.lexer = (ILLKLexer) lexer.clone();
        ret.markLocations = markLocations.clone();
        ret.current = ret.lexer.llkGetToken0();
        ret.la1 = -1;
        return ret;
    }

    @Override
    public void reset() {
        current = lexer.llkReset();
        la1 = -1;
        markOffset = 0;
    }

    @Override
    public final void mark() {
        if (markOffset >= markLocations.length) {
            expandMarkLocations();
        }
        markLocations[markOffset++] = current;
    }

    @Override
    public final void unmark() {
        --markOffset;
    }

    @Override
    public final void remark() {
        markLocations[markOffset - 1] = current;
    }

    @Override
    public final void rewind() {
        current = markLocations[--markOffset];
        la1 = -1;
    }

    @Override
    public final boolean isMarked() {
        return markOffset > 0;
    }

    @Override
    public final ILLKMain getMain() {
        return lexer.llkGetMain();
    }

    @Override
    public final ILLKLexer getLexer() {
        return lexer;
    }

    @Override
    public final CharSequence getSource() {
        return lexer.llkGetInput().getSource();
    }

    @Override
    public final CharSequence getSource(final int start, final int end) {
        return lexer.llkGetInput().getSource(start, end);
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
        return current.getType();
    }

    @Override
    public final int LA1() {
        if (la1 < 0) {
            ILLKToken t = current.getNext();
            if (t == null) {
                t = lexer.llkNextToken();
            }
            la1 = t.getType();
        }
        return la1;
    }

    @Override
    public final int LA(int n) {
        ILLKToken t = current;
        for (; n > 0; --n) {
            t = t.getNext();
            if (t == null) {
                t = lexer.llkNextToken();
            }
        }
        return t.getType();
    }

    @Override
    public final int consumeLA0() {
        int la0 = la1;
        consume();
        return la0;
    }

    @Override
    public final int consumeLA1() {
        current = current.getNext();
        if (current == null) {
            current = lexer.llkNextToken();
        }
        ILLKToken t = current.getNext();
        if (t == null) {
            t = lexer.llkNextToken();
        }
        la1 = t.getType();
        return la1;
    }

    @Override
    public final ILLKToken LT0() {
        return current;
    }

    @Override
    public final ILLKToken LT1() {
        final ILLKToken t = current.getNext();
        if (t == null) {
            return lexer.llkNextToken();
        }
        return t;
    }

    @Override
    public final ILLKToken LT(int n) {
        ILLKToken t = current;
        for (; n > 0; --n) {
            t = t.getNext();
            if (t == null) {
                t = lexer.llkNextToken();
            }
        }
        return t;
    }

    @Override
    public final boolean matchOpt(final int type) {
        if (la1 >= 0 && type != la1) {
            return false;
        }
        if (LA1() != type) {
            return false;
        }
        current = current.getNext();
        if (current == null) {
            current = lexer.llkNextToken();
        }
        la1 = -1;
        return true;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void consume() {
        current = current.getNext();
        if (current == null) {
            current = lexer.llkNextToken();
        }
        la1 = -1;
    }

    @Override
    public void consume(int n) {
        for (; n > 0; --n) {
            current = current.getNext();
            if (current == null) {
                current = lexer.llkNextToken();
            }
        }
        la1 = -1;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final int getKeywordContexts() {
        return lexer.llkGetKeywordContexts();
    }

    @Override
    public final void setKeywordContexts(final int context) {
        lexer.llkSetKeywordContexts(context, current);
    }

    @Override
    public final void setContext(final int context, final int state) {
        lexer.llkSetContext(context, state, current);
        la1 = -1;
    }

    @Override
    public final void rewind(final ILLKToken lt0) {
        lexer.llkRewind(lt0);
        current = lt0;
        la1 = -1;
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

    private void expandMarkLocations() {
        final ILLKToken[] ret = new ILLKToken[markLocations.length * 2];
        System.arraycopy(markLocations, 0, ret, 0, markOffset);
        markLocations = ret;
        if (CHECK && MARKER_SIZE > 1 && (ret.length % MARKER_SIZE) != 0) {
            System.err.println(NAME + ".expandMarkLocations(): assert (ret.length%MARKER_SIZE)==0");
        }
    }

    ////////////////////////////////////////////////////////////
}
