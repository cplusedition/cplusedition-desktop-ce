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

public interface ILLKBinaryLexerInput extends ILLKInput, IByteSequence {

    ILLKMain getMain();

    int getOffset();

    /**
     * Rewind input stream to the given offset.
     */
    void rewind(int offset);

    void setCutoff();

    void setCutoff(int offset);

    @Override
    Object clone() throws CloneNotSupportedException;

    ISourceLocator getLocator();

    ISourceLocation getLocation();

    ISourceLocation getLocation(int offset);

    /**
     * Replace the given range in the input buffer by the given text.
     */
    void replace(int start, int end, byte[] text);

    void replace(int start, int end, byte[] text, int tstart, int tend);

    void replace(int start, int end, IByteSequence text);

    @Override
    byte byteAt(int index);

    @Override
    void getBytes(int start, int end, byte[] dst, int dststart);

    byte[] getSource();

    byte[] getSource(int start);

    byte[] getSource(int start, int end);

    IByteSequence getSourceSequence();

    IByteSequence getSourceSequence(int start);

    IByteSequence getSourceSequence(int start, int end);

    boolean LA(byte[] expected);

    boolean LA(byte[] expected, int index);

    boolean matchOpt(byte c);

    int LA1Consume();

    @Override
    int consumeLA1();

    short consume2BE();

    short consume2LE();

    int consumeU2BE();

    int consumeU2LE();

    int consume4BE();

    int consume4LE();

    long consume8BE();

    long consume8LE();

    void match(int c) throws LLKParseException;

    void match(int c1, int c2) throws LLKParseException;

    void match(int c1, int c2, int c3) throws LLKParseException;

    void match(int[] bset) throws LLKParseException;

    void matchRange(int c1, int c2) throws LLKParseException;

    void matchNot(int c) throws LLKParseException;

    void matchNot(int c1, int c2) throws LLKParseException;

    void matchNot(int c1, int c2, int c3) throws LLKParseException;

    void matchNot(int[] bset) throws LLKParseException;

    void matchNotRange(int c1, int c2) throws LLKParseException;

    void match(byte[] a) throws LLKParseException;

    void match(byte[] a, int start) throws LLKParseException;

}
