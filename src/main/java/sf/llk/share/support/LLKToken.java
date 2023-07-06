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

public class LLKToken implements ILLKToken, Cloneable {

    ////////////////////////////////////////////////////////////////////////

    public int type;
    public ILLKToken next;
    public ILLKToken special;
    protected int start, end;
    protected CharSequence text;
    protected Object data;

    ////////////////////////////////////////////////////////////////////////

    public LLKToken(final int type, final int start, final int end) {
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public LLKToken(final int type, final int start, final int end, final CharSequence text) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public LLKToken(final int type, final int start, final int end, final CharSequence text, final Object value) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.text = text;
        data = value;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public Object clone() {
        try {
            final LLKToken ret = (LLKToken) super.clone();
            ret.next = null;
            if (special != null) {
                ILLKToken s = (ILLKToken) special.clone();
                ret.setSpecial(s);
                ILLKToken os = special;
                ILLKToken ss;
                while ((os = os.getNext()) != null) {
                    ss = (ILLKToken) os.clone();
                    s.setNext(ss);
                    s = ss;
                }
            }
            return ret;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<ILLKToken> specials() {
        return new TokenIterator(special, null);
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(final int type) {
        this.type = type;
    }

    @Override
    public ILLKToken getNext() {
        return next;
    }

    @Override
    public ILLKToken getSpecial() {
        return special;
    }

    @Override
    public void setNext(final ILLKToken t) {
        next = t;
    }

    @Override
    public void setSpecial(final ILLKToken t) {
        special = t;
    }

    @Override
    public void setData(final Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setText(final CharSequence s) {
        text = s;
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
        return end;
    }

    @Override
    public int getLength() {
        return end - start;
    }

    @Override
    public void setOffset(final int offset) {
        start = offset;
    }

    @Override
    public void setEndOffset(final int offset) {
        end = offset;
    }

    @Override
    public String getLocationString() {
        return "(" + start + "-" + end + ")";
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "LLKToken(type="
            + type
            + ", start="
            + start
            + ", end="
            + end
            + ", text="
            + text
            + ", data="
            + data
            + ")";
    }

    ////////////////////////////////////////////////////////////////////////
}
