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
package sf.andrians.ancoreutil.util.struct

import java.util.*

/**
 * Stack with a parallel mark stack that can be used to mark and rewind scopes in the value stack.
 */
class MarkedStack<T> : ArrayList<T>() {
    private var marks = IntList()
    private var mark = 0

    ////////////////////////////////////////////////////////////////////////
    override fun clone(): Any {
        val ret = super.clone() as MarkedStack<T>
        try {
            ret.marks = marks.clone() as IntList
        } catch (e: CloneNotSupportedException) {
            throw RuntimeException(e)
        }
        return ret
    }

    override fun clear() {
        super.clear()
        marks.clear()
        mark = 0
    }

    fun clearMarks() {
        marks.clear()
        mark = 0
    }

    /**
     * Save the current value stack size.
     */
    fun mark(): Int {
        marks.add(mark)
        mark = size
        return mark
    }

    fun markCount(): Int {
        return marks.size()
    }

    fun peekMark(): Int {
        return mark
    }

    /**
     * Pop the topmost mark (not the value entry).
     */
    fun popMark() {
        mark = marks.pop()
    }

    /**
     * Get number of entries above the last mark.
     * @return    Number of entries above last mark. If there is no mark,
     * return value is same as current node stack size..
     * @throws IndexOutOfBound if mark is above top of value stack.
     */
    fun childCount(): Int {
        val ret = size - mark
        if (ret < 0) throw RuntimeException("Expected >=0: ret=$ret")
        return ret
    }

    /**
     * Remove all entries on the stack above the topmost mark (inclusively).
     */
    fun rewind() {
        val size = size
        if (mark > size) throw RuntimeException("Mark is beyond top of value stack: size=" + size + "mark=" + mark)
        removeRange(mark, size)
        popMark()
    }

    fun push(a: T) {
        add(a)
    }

    fun peek(): T? {
        return get(size - 1)
    }

    /**
     * Peek at value of the nth entry below top of value stack.
     * peek(1) is same as peek().
     */
    fun peek(n: Int): T? {
        return get(size - n)
    }

    fun pop(): T? {
        return removeAt(size - 1)
    }

    /**
     * Pop n entries from top of stack.
     */
    fun pop(n: Int) {
        val size = size
        if (n < 0 || n > size) throw RuntimeException("Expected n >=0 && n<=$size: n=$n")
        removeRange(size - n, size)
    }

    public override fun removeRange(start: Int, end: Int) {
        super.removeRange(start, end)
    }

    override fun toString(): String {
        val ret = StringBuilder("### MarkStack: (top is first)\n\t")
        var i = size
        while (i > 0) {
            if (i == mark || marks.indexOf(i) >= 0) ret.append("---\n\t")
            --i
            ret.append(i).append(": ").append(get(i)).append("\n\t")
        }
        return ret.toString()
    } ////////////////////////////////////////

    companion object {
        ////////////////////////////////////////////////////////////////////////
        private const val serialVersionUID = 3850796216099044568L
    }
}