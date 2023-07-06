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
public class SourceTokens implements ISourceTokens {

    protected final ILLKToken startToken;
    protected final int endOffset;
    protected final String sep;
    protected CharSequence text;

    public SourceTokens(final ILLKToken start, final int endoffset) {
        this(start, endoffset, "");
    }

    public SourceTokens(final ILLKToken start, final int endoffset, final String sep) {
        startToken = start;
        endOffset = endoffset;
        this.sep = sep;
    }

    @Override
    public ILLKToken getFirstToken() {
        return startToken;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public int getLength() {
        return getText().length();
    }

    @Override
    public int getOffset() {
        return startToken.getOffset();
    }

    @Override
    public CharSequence getText() {
        if (text == null) {
            text = getText(startToken, endOffset, sep);
        }
        return text;
    }

    public static CharSequence getText(final ILLKToken start, final int endoffset, final String sep) {
        final StringBuilder ret = new StringBuilder();
        for (ILLKToken t = start; t.getEndOffset() <= endoffset; t = t.getNext()) {
            if (ret.length() > 0) {
                ret.append(sep);
            }
            ret.append(t.getText());
        }
        return ret;
    }
}
