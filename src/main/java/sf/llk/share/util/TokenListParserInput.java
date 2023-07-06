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
/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.util;

import java.util.LinkedList;
import java.util.List;

import sf.llk.share.support.IDirectiveHandler;
import sf.llk.share.support.ILLKLexer;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.IReadOnlyLocator;
import sf.llk.share.support.ISourceLocation;
import sf.llk.share.support.LLKParseError;

public class TokenListParserInput implements ILLKParserInput {

    //////////////////////////////////////////////////////////////////////

    final ILLKMain main;
    final ILLKToken head;
    ILLKToken current;
    final List<ILLKToken> marks = new LinkedList<>();

    //////////////////////////////////////////////////////////////////////

    /**
     * @param head
     * @param main
     */
    public TokenListParserInput(final ILLKToken head, final ILLKMain main) {
        this.main = main;
        this.head = head;
        current = head;
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public ILLKToken LT0() {
        return current;
    }

    @Override
    public ILLKToken LT1() {
        return current.getNext();
    }

    @Override
    public ILLKToken LT(int n) {
        ILLKToken ret = current;
        while (n > 0) {
            ret = ret.getNext();
            --n;
            if (ret == null && n > 0) {
                throw new LLKParseError("Lookahead passed EOF: n=" + n);
            }
        }
        return ret;
    }

    @Override
    public ILLKMain getMain() {
        return main;
    }

    @Override
    public IReadOnlyLocator getLocator() {
        return main.getLocator();
    }

    @Override
    public ISourceLocation getLocation(final int offset) {
        return main.getLocator().getLocation(offset);
    }

    @Override
    public ILLKLexer getLexer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence getSource() {
        throw new UnsupportedOperationException();
    }
    @Override
    public CharSequence getSource(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTokenName(final int type) {
        throw new UnsupportedOperationException();
    }

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
    public void setDirectiveHandler(final IDirectiveHandler handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IDirectiveHandler getDirectiveHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rewind(final ILLKToken lt0) {
        current = lt0;
    }

    @Override
    public int LA0() {
        return current.getType();
    }

    @Override
    public int LA1() {
        return LT1().getType();
    }

    @Override
    public int LA(final int n) {
        return LT(n).getType();
    }

    @Override
    public final int consumeLA0() {
        current = LT1();
        return current.getType();
    }

    @Override
    public final int consumeLA1() {
        current = LT1();
        return LT1().getType();
    }

    @Override
    public final boolean matchOpt(final int type) {
        final ILLKToken lt1 = LT1();
        if (lt1.getType() != type) {
            return false;
        }
        current = lt1;
        return true;
    }

    @Override
    public void consume() {
        current = LT1();
    }

    @Override
    public void consume(final int n) {
        current = LT(n);
    }

    @Override
    public void reset() {
        current = head;
        marks.clear();
    }

    @Override
    public void mark() {
        marks.add(0, current);
    }

    @Override
    public void unmark() {
        marks.remove(0);
    }

    @Override
    public final void remark() {
        marks.set(0, current);
    }

    @Override
    public void rewind() {
        current = marks.remove(0);
    }

    @Override
    public boolean isMarked() {
        return marks.size() > 0;
    }

    //////////////////////////////////////////////////////////////////////
}
