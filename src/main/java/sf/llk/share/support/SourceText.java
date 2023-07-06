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

import java.util.Comparator;

public class SourceText implements ISourceText {

    private static Comparator<SourceText> ascendingComparator;

    protected CharSequence text;
    protected int start;
    protected int length;

    protected SourceText() {
    }

    public SourceText(CharSequence text, int start, int len) {
        this.text = text;
        this.start = start;
        this.length = len;
    }

    public SourceText(CharSequence text, int start) {
        this.text = text;
        this.start = start;
        this.length = text.length();
    }

    public SourceText(ISourceText sourcetext) {
        this.text = sourcetext.getText().toString();
        this.start = sourcetext.getOffset();
        this.length = sourcetext.getLength();
    }

    public static Comparator<SourceText> getAscendingComparator() {
        if (ascendingComparator == null) {
            ascendingComparator = (SourceText a, SourceText b) -> {
                int oa = a.getOffset();
                int ob = b.getOffset();
                return (oa > ob) ? 1 : (oa < ob) ? -1 : 0;
            };
        }
        return ascendingComparator;
    }

    @Override
    public CharSequence getText() {
        return text;
    }

    @Override
    public int getOffset() {
        return start;
    }

    @Override
    public int getEndOffset() {
        return start + length;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @Override
    public int hashCode() {
        return start * 31 + text.hashCode();
    }

    @Override
    public boolean equals(Object a) {
        if (!(a instanceof SourceText)) {
            return false;
        }
        SourceText aa = (SourceText) a;
        return start == aa.getOffset()
            && length == aa.getLength()
            && (text == null && aa.getText() == null || text != null && text.equals(aa.getText()));
    }
}
