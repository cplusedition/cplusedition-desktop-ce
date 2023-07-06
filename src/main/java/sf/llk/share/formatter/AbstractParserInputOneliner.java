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
package sf.llk.share.formatter;

import sf.llk.share.support.IIntList;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseError;
import sf.llk.share.util.ISpaceUtil;

public abstract class AbstractParserInputOneliner implements IOnelinerFormatter {

    //////////////////////////////////////////////////////////////////////

    protected ISpaceUtil spaceutil;
    protected int spaces;
    protected int newline;
    protected int comment1;
    protected int comment;

    public AbstractParserInputOneliner(ISpaceUtil spaceutil, int spaces, int newline, int comment1, int comment) {
        this.spaceutil = spaceutil;
        this.spaces = spaces;
        this.newline = newline;
        this.comment1 = comment1;
        this.comment = comment;
    }

    protected abstract ILLKParserInput createParserInput(char[] source);

    public CharSequence toOneliner(
        CharSequence s, int start, int end, boolean wasspace, int columns, IIntList softbreaks) {
        StringBuilder ret = new StringBuilder();
        int len = end - start;
        char[] source = new char[len];
        for (int i = 0; i < len; ++i)
            source[i] = s.charAt(start + i);
        try {
            ILLKParserInput pin = createParserInput(source);
            while (true) {
                ILLKToken t = pin.LT1();
                int type = t.getType();
                if (type == spaces) {
                    if (!wasspace) {
                        int offset = t.getOffset();
                        if (softbreaks != null && isSoftBreak(offset, softbreaks)) {
                            skipSoftBreak(t);
                        } else {
                            ret.append(' ');
                            wasspace = true;
                        }
                    }
                } else if (type == newline) {
                    if (!wasspace) {
                        int offset = t.getOffset();
                        if (softbreaks != null && isSoftBreak(offset, softbreaks)) {
                            skipSoftBreak(t);
                        } else {
                            ret.append(' ');
                            wasspace = true;
                        }
                    }
                } else if (type == comment) {
                    if (spaceutil.isSpaces(ret) && (followedByLineBreak(t)))
                        return null;
                    if (spaceutil.hasLineBreak(t.getText()))
                        return null;
                    if (!wasspace)
                        ret.append(' ');
                    CharSequence text = t.getText();
                    if (spaceutil.skipLine(text, 0, text.length()) >= 0)
                        return null;
                    ret.append(text);
                    ret.append(' ');
                    wasspace = true;
                } else if (type == comment1) {
                    ILLKToken tt = t.getNext();
                    if (tt != null) {
                        if (tt.getType() != newline)
                            throw new LLKParseError(
                                "Single line comment must be followed by a NEWLINE token: token="
                                    + tt
                            );
                        if (tt.getNext() != null || pin.LA1() != ILLKParserInput.EOF)
                            return null;
                    }
                    if (!wasspace)
                        ret.append(' ');
                    ret.append(t.getText());
                    wasspace = false;
                } else if (type == ILLKParserInput.EOF) {
                    break;
                } else {
                    ret.append(t.getText());
                    wasspace = false;
                }
                if (ret.length() > columns)
                    return null;
                pin.consume();
            }
        } catch (Exception e) {
            throw new LLKParseError("Source = " + s, e);
        }
        return ret;
    }

    //////////////////////////////////////////////////////////////////////

    protected boolean isSoftBreak(int i, IIntList breaks) {
        return breaks.binarySearch(i) >= 0;
    }

    protected ILLKToken skipSoftBreak(ILLKToken st) {
        for (ILLKToken n = st.getNext(); n != null; st = n, n = n.getNext()) {
            int next = n.getType();
            if (next != spaces)
                break;
        }
        return st;
    }

    protected boolean followedByLineBreak(ILLKToken s) {
        for (s = s.getNext(); s != null; s = s.getNext()) {
            int type = s.getType();
            if (type == spaces)
                continue;
            if (type == newline)
                return true;
            return false;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////
}
