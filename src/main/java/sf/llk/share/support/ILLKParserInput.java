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

public interface ILLKParserInput extends ILLKInput {

    int EOF = 0;

    ILLKToken LT0();

    ILLKToken LT1();

    ILLKToken LT(int n);

    boolean matchOpt(int type);

    ILLKMain getMain();

    IReadOnlyLocator getLocator();

    ILLKLexer getLexer();

    CharSequence getSource();

    CharSequence getSource(int start, int end);

    ISourceLocation getLocation(int offset);

    String getTokenName(int type);

    int getKeywordContexts();

    void setKeywordContexts(int context);

    void setContext(int context, int state);

    /**
     * Rewind lexer input to start of the non-special token that follows the given token.
     * If the given token do not has a next field, do nothing.  Since lexer has not look ahead
     * of the given token yet.
     */
    void rewind(ILLKToken lt0);

    void setDirectiveHandler(IDirectiveHandler handler);

    IDirectiveHandler getDirectiveHandler();

    @Override
    Object clone() throws CloneNotSupportedException;
}
