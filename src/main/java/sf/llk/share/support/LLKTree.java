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

import java.util.Stack;

public class LLKTree implements Cloneable, ILLKTreeBuilder {

    ////////////////////////////////////////////////////////////

    private Stack<ILLKNode> nodes;
    private IntList marks;
    private int nodeCount;
    private int mark;

    ////////////////////////////////////////////////////////////

    public LLKTree() {
        nodes = new java.util.Stack<>();
        marks = new IntList(32);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKTree ret = (LLKTree) super.clone();
        ret.nodes = new Stack<>();
        ret.nodes.addAll(nodes);
        ret.marks = (IntList) marks.clone();
        return ret;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void reset() {
        nodes.removeAllElements();
        marks.clear();
        nodeCount = 0;
        mark = 0;
    }

    @Override
    public ILLKNode root() {
        return nodes.elementAt(0);
    }

    @Override
    public void push(final ILLKNode n) {
        nodes.push(n);
        ++nodeCount;
    }

    @Override
    public ILLKNode pop() {
        --nodeCount;
        return nodes.pop();
    }

    @Override
    public ILLKNode peek() {
        return nodes.peek();
    }

    @Override
    public ILLKNode peek(final int n) {
        return nodes.get(nodeCount - n);
    }

    @Override
    public int size() {
        return nodeCount;
    }

    @Override
    public ILLKNode get(final int index) {
        return nodes.get(index);
    }

    @Override
    public int peekMark() {
        return mark;
    }

    @Override
    public int peekMark(final int n) {
        if (n == 1) {
            return mark;
        }
        return marks.peek(n - 1);
    }

    @Override
    public int childCount() {
        return nodeCount - mark;
    }

    @Override
    public void clearScope() {
        while (nodeCount > mark) {
            pop();
        }
        mark = marks.pop();
    }

    @Override
    public void open() {
        marks.push(mark);
        mark = nodeCount;
    }

    @Override
    public void open(final ILLKNode n) {
        marks.push(mark);
        mark = nodeCount;
    }

    @Override
    public void close(final ILLKNode n, int num) {
        mark = marks.pop();
        while (--num >= 0) {
            n.prepend(pop());
        }
        push(n);
    }

    @Override
    public void close(final ILLKNode n) {
        int a = childCount();
        while (--a >= 0) {
            n.prepend(pop());
        }
        push(n);
        mark = marks.pop();
    }

    @Override
    public void close(final ILLKNode n, final boolean condition) {
        if (condition) {
            int a = childCount();
            while (--a >= 0) {
                n.prepend(pop());
            }
            push(n);
        }
        mark = marks.pop();
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder("### LLKTree: (top is first)\n\t");
        for (int i = nodeCount; i > 0; ) {
            if (i == mark || marks.indexOf(i) >= 0) {
                ret.append("---\n\t");
            }
            --i;
            ret.append(i).append(": ").append(get(i)).append("\n\t");
        }
        return ret.toString();
    }

    ////////////////////////////////////////////////////////////
}
