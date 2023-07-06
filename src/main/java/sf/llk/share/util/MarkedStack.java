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
package sf.llk.share.util;

import java.util.ArrayList;

import sf.llk.share.support.IntList;

/**
 * Stack with a parallel mark stack that can be used to mark and rewind scopes in the value stack.
 */
public class MarkedStack<T> extends ArrayList<T> {

    ////////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = 3850796216099044568L;
    private IntList marks = new IntList();
    private int mark;

    ////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        MarkedStack<T> ret = (MarkedStack<T>) super.clone();
        try {
            ret.marks = (IntList) marks.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    @Override
    public void clear() {
        super.clear();
        marks.clear();
        mark = 0;
    }

    public void clearMarks() {
        marks.clear();
        mark = 0;
    }

    /**
     * Save the current value stack size.
     */
    public int mark() {
        marks.add(mark);
        mark = size();
        return mark;
    }

    public int markCount() {
        return marks.size();
    }

    public int peekMark() {
        return mark;
    }

    /**
     * Pop the topmost mark (not the value entry).
     */
    public void popMark() {
        mark = marks.pop();
    }

    /**
     * Get number of entries above the last mark.
     *
     * @throws IndexOutOfBound if mark is above top of value stack.
     * @return Number of entries above last mark. If there is no mark,
     * return value is same as current node stack size..
     */
    public int childCount() {
        int ret = size() - mark;
        if (ret < 0) {
            throw new RuntimeException("Expected >=0: ret=" + ret);
        }
        return ret;
    }

    /**
     * Remove all entries on the stack above the topmost mark (inclusively).
     */
    public void rewind() {
        int size = size();
        if (mark > size) {
            throw new RuntimeException("Mark is beyond top of value stack: size=" + size + "mark=" + mark);
        }
        removeRange(mark, size);
        popMark();
    }

    public void push(T a) {
        add(a);
    }

    public T peek() {
        return get(size() - 1);
    }

    /**
     * Peek at value of the nth entry below top of value stack.
     * peek(1) is same as peek().
     */
    public T peek(int n) {
        return get(size() - n);
    }

    public T pop() {
        return remove(size() - 1);
    }

    /**
     * Pop n entries from top of stack.
     */
    public void pop(int n) {
        int size = size();
        if (n < 0 || n > size) {
            throw new RuntimeException("Expected n >=0 && n<=" + size + ": n=" + n);
        }
        removeRange(size - n, size);
    }

    @Override
    public void removeRange(int start, int end) {
        super.removeRange(start, end);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("### MarkStack: (top is first)\n\t");
        for (int i = size(); i > 0; ) {
            if (i == mark || marks.indexOf(i) >= 0) {
                ret.append("---\n\t");
            }
            --i;
            ret.append(i).append(": ").append(get(i)).append("\n\t");
        }
        return ret.toString();
    }

    ////////////////////////////////////////
}
