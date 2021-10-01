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

interface IIntComparator {
    fun compare(a: Int, b: Int): Int

    /** Comparator that sort in descending order, ie. dequeue highest value first.  */
    class DescendingComparator : IIntComparator {
        override fun compare(a: Int, b: Int): Int {
            return a - b
        }

        companion object {
            var singleton: DescendingComparator = DescendingComparator()
        }
    }

    /** Comparator that sort in ascending order, ie. dequeue lowest value first.  */
    class AscendingComparator : IIntComparator {
        override fun compare(a: Int, b: Int): Int {
            return b - a
        }

        companion object {
            var singleton: AscendingComparator = AscendingComparator()
        }
    }

    class ReverseComparator(private val comparator: IIntComparator) : IIntComparator {
        override fun compare(a: Int, b: Int): Int {
            return -comparator.compare(a, b)
        }

    }
}