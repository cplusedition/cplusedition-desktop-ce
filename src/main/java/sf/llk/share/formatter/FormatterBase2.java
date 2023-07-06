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

import java.util.Arrays;

import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKToken;

public abstract class FormatterBase2 extends FormatterBase {

    public abstract ILLKMain getMain();
    public abstract ILLKToken LT0();
    public abstract ILLKToken LT1();
    public abstract ILLKToken LT(int n);

    public void emitSpaces(FormatBuilder buf, StringBuffer b, int spaces) {
        if (spaces < 0) {
            buf.append(b);
        } else if (spaces == 1) {
            buf.space();
        } else if (spaces > 1) {
            buf.space();
            for (int i = 1; i < b.length() && i < spaces; ++i)
                buf.append(' ');
        }
    }

    public void emitLineBreaks(FormatBuilder buf, StringBuffer b, int breaks) {
        buf.getSpaceUtil().ltrimSpaces(b);
        if (breaks < 0) {
            buf.indentLines(b.toString());
        } else if (breaks == 1) {
            buf.newLine();
        } else if (breaks > 1) {
            buf.newLine();
            int count = buf.getSpaceUtil().lcountLineBreaks(b, 0, b.length());
            for (int i = 1; i < count && i < breaks; ++i)
                buf.newLine();
        }
    }

    public void emitText(FormatBuilder buf) {
        buf.append(LT0().getText());
    }

    public void emitToken(FormatBuilder buf, ILLKToken t) {
        emitSpecial(buf, t, -1, -1);
        buf.append(t.getText());
    }

    public void emitTokens(FormatBuilder buf, ILLKToken first, ILLKToken last) {
        int end = (last == null ? Integer.MAX_VALUE : last.getEndOffset());
        for (; first != null && first.getOffset() < end; first = first.getNext()) {
            emitSpecial(buf, first, -1, -1);
            buf.append(first.getText());
        }
    }

    public void emitSpace(FormatBuilder buf) {
        emitSpace(buf, LT0());
    }

    public void emitNoSpace(FormatBuilder buf) {
        emitNoSpace(buf, LT0());
    }

    public void emitSpaces(FormatBuilder buf, int spaces) {
        ILLKToken t = LT0();
        emitSpecial(buf, t, spaces);
        buf.append(t.getText());
    }

    public void emit(FormatBuilder buf) {
        emitNoSpace(buf, LT0());
    }

    public void emit(FormatBuilder buf, int spaces) {
        emitSpaces(buf, spaces);
    }

    protected boolean lt0Type(int... types) {
        int type = LT0().getType();
        return Arrays.stream(types).anyMatch(t -> type == t);
    }
}
