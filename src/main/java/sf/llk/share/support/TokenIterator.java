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

import java.util.Iterator;

public class TokenIterator implements Iterable<ILLKToken>, Iterator<ILLKToken> {
    private ILLKToken current;
    private int offset, lastOffset, end;

    public TokenIterator(final ILLKToken start, final ILLKToken last) {
        current = start;
        if (last != null) {
            lastOffset = last.getOffset();
            end = last.getEndOffset();
        } else {
            lastOffset = Integer.MAX_VALUE;
            end = Integer.MAX_VALUE;
        }
        if (start != null) {
            offset = current.getOffset();
        }
    }

    @Override
    public boolean hasNext() {
        return current != null && offset <= lastOffset && offset != end;
    }

    @Override
    public ILLKToken next() {
        if (current == null) {
            throw new RuntimeException("Iterating pass end of list.");
        }
        ILLKToken prev = current;
        current = current.getNext();
        if (current != null) {
            offset = current.getOffset();
        }
        return prev;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ILLKToken> iterator() {
        return this;
    }
}
