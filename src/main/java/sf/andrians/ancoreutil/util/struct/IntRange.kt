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

class IntRange : IIntSequence {
    var array: IntArray
    var start: Int
    var end: Int

    //////////////////////////////////////////////////////////////////////
    constructor(a: IntArray) {
        array = a
        start = 0
        end = a.size
    }

    constructor(a: IntArray, start: Int, end: Int) {
        array = a
        this.start = start
        this.end = end
    }

    override fun size(): Int {
        return end - start
    }

    override val isEmpty: Boolean
        get() = end - start == 0

    override fun get(index: Int): Int {
        return array[start + index]
    }

    override fun indexOf(o: Int): Int {
        for (i in start until end) if (array[i] == o) return i
        return -1
    }

    override fun contains(value: Int): Boolean {
        for (i in start until end) if (array[i] == value) return true
        return false
    }

    override fun iterator(): IIntIterator {
        return Iterator()
    }

    override fun subRange(start: Int, end: Int): IIntSequence {
        return IntRange(array, this.start + start, this.start + end)
    }

    override fun copyTo(dst: IntArray, dststart: Int, srcstart: Int, srcend: Int) {
        System.arraycopy(array, start + srcstart, dst, dststart, srcend - srcstart)
    }

    override fun copyTo(dst: IntArray, dststart: Int) {
        System.arraycopy(array, start, dst, dststart, end - start)
    }

    override fun toArray(): IntArray {
        val len = end - start
        val ret = IntArray(len)
        System.arraycopy(array, start, ret, 0, len)
        return ret
    }

    //////////////////////////////////////////////////////////////////////

    internal inner class Iterator : IIntIterator {
        var index = start
        override fun hasNext(): Boolean {
            return index < end
        }

        override fun next(): Int {
            return array[index++]
        }
    }

    //////////////////////////////////////////////////////////////////////
}
