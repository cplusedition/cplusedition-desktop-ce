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

public interface ILLKTokenHandler extends Cloneable {

    void setLexerInput(ILLKLexerInput input);

    void reset();

    Object clone();

    /**
     * Create a normal token.
     */
    ILLKToken createToken(int type, int start, int end);

    ILLKToken createToken(int type, int start, int end, Object data);

    ILLKToken createToken(int type, int start, int end, CharSequence text, Object data);

    ILLKToken createTextToken(int type, int start, int end, CharSequence text);

    /**
     * Create a special token.
     */
    ILLKToken createSpecialToken(int type, int start, int end);

    ILLKToken createSpecialToken(int type, int start, int end, Object data);

    ILLKToken createSpecialToken(int type, int start, int end, CharSequence text, Object data);

    ILLKToken createSpecialTextToken(int type, int start, int end, CharSequence text);

    /**
     * Add given token to token chain unless token is ILLKConstants.TOKEN_TYPE_IGNORE (ignore).
     */
    boolean yieldToken(ILLKToken token);

    /**
     * Create and add token to token chain unless token type is an ILLKConstants.TOKEN_TYPE_IGNORE (ignore).
     *
     * @return true if token is a normal token.
     */
    boolean yieldToken(int type, int start, int end);

    boolean yieldToken(int type, int start, int end, Object data);

    boolean yieldToken(int type, int start, int end, CharSequence text, Object data);

    boolean yieldTextToken(int type, int start, int end, CharSequence text);

    INamePool namePool();

    void rewind(ILLKToken token);

    void rewind(int offset);

    /**
     * @return The last created normal token.
     */
    ILLKToken getToken0();

    int getType0();

    int getOffset0();

    int getEndOffset0();

    CharSequence getText0();

    Object getData0();
}
