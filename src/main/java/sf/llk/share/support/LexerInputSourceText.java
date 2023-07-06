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
package sf.llk.share.support;

public class LexerInputSourceText implements ISourceText, CharSequence {

    private final ILLKLexerInput input;
    private int start;
    private int end;

    public LexerInputSourceText(final ILLKLexerInput input, final int start, final int end) {
        this.input = input;
        this.start = start;
        this.end = end;
    }

    public void set(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public CharSequence getText() {
        return this;
    }

    @Override
    public int getEndOffset() {
        return end;
    }

    @Override
    public int getLength() {
        return end - start;
    }

    @Override
    public int getOffset() {
        return start;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(final int index) {
        return input.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new LexerInputSourceText(input, this.start + start, this.start + end);
    }

    @Override
    public String toString() {
        return input.getSource(start, end).toString();
    }
}
