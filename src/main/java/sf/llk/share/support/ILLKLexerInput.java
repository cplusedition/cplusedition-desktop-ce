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

public interface ILLKLexerInput extends ILLKInput, ICharSequence {

    public interface IConsumer {
        boolean consume(int la1);
    }

    ILLKMain getMain();

    int getOffset();

    ISourceLocator getLocator();

    ISourceLocation getLocation();

    ISourceLocation getLocation(int offset);

    CharSequence getSource();

    CharSequence getSource(int start);

    CharSequence getSource(int start, int end);

    void setIgnoreCase(boolean ignorecase);

    boolean isIgnoreCase();

    /**
     * Replace the given range in the input buffer by the given text.
     */
    void replace(int start, int end, char[] text);

    void replace(int start, int end, char[] text, int tstart, int tend);

    void replace(int start, int end, ICharSequence text);

    void newline();

    void tab();

    /**
     * Rewind input stream to the given offset.
     */
    void rewind(int offset);

    /**
     * Set the cutoff offset of current token.  This is replaced each time setCutoff() is called.
     */
    void setCutoff();

    /**
     * Add current offset as additional cutoff, this is must be removed by matching removeCutoff(int offset).
     */
    int addCutoff();

    void addCutoff(int offset);

    boolean removeCutoff(int offset);

    @Override
    Object clone() throws CloneNotSupportedException;

    boolean LA(char[] expected);

    boolean LA(char[] expected, int index);

    boolean matchOpt(char c);

    int LA1Consume();

    @Override
    int consumeLA1();

    /**
     * Consume input until scanner.consume return false.
     *
     * @return The first character not consumed (LA1()).
     */
    int consume(IConsumer consumer);

    void match(char c) throws LLKParseException;

    void match(char c1, char c2) throws LLKParseException;

    void match(char c1, char c2, char c3) throws LLKParseException;

    void match(int[] bset) throws LLKParseException;

    void matchRange(char c1, char c2) throws LLKParseException;

    void matchNot(char c) throws LLKParseException;

    void matchNot(char c1, char c2) throws LLKParseException;

    void matchNot(char c1, char c2, char c3) throws LLKParseException;

    void matchNot(int[] bset) throws LLKParseException;

    void matchNotRange(char c1, char c2) throws LLKParseException;

    void match(char[] a) throws LLKParseException;

    void match(char[] a, int start) throws LLKParseException;

}
