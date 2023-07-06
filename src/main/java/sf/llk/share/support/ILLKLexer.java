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

public interface ILLKLexer {

    /**
     * @return Next normal (not special or ignore) token
     */
    ILLKToken llkNextToken();

    int llkNextType();

    /**
     * @return Next token including special and ignore tokens.
     */
    boolean llkNextToken1();

    /**
     * @return The previous normal token.
     */
    ILLKToken llkGetToken0();

    ILLKToken llkReset();

    ILLKMain llkGetMain();

    ILLKLexerInput llkGetInput();

    CharSequence llkGetSource();

    CharSequence llkGetSource(int start, int end);

    String llkGetTokenName(int type);

    int llkGetTokenStart();

    void llkSetTokenStart(int offset);

    int llkGetOffset();

    ISourceLocation llkGetLocation();

    ISourceLocation llkGetLocation(int offset);

    int llkGetKeywordContexts();

    void llkSetKeywordContexts(int context, ILLKToken lt0);

    void llkSetContext(int context, int state, ILLKToken lt0);

    /**
     * Rewind lexer input to start of the non-special token that follows the given token.
     * If the given token do not has a next field, do nothing.  Since lexer has not look ahead
     * of the given token yet.
     */
    void llkRewind(ILLKToken lt0);

    void llkSetDirectiveHandler(IDirectiveHandler handler);

    void llkSetTokenHandler(ILLKTokenHandler handler);

    IDirectiveHandler llkGetDirectiveHandler();

    ILLKTokenHandler llkGetTokenHandler();

    Object clone() throws CloneNotSupportedException;
}
