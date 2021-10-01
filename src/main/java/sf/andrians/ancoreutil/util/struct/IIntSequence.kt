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

/**
 * IIntSequence is a read only random accessible sequence of integers.
 */
interface IIntSequence : IIntIterable {
    /** Get object from the head at given 0-based index.  */
    operator fun get(index: Int): Int

    /** @return Index of object from head that equals() to the given object, -1 if not found.
     */
    fun indexOf(x: Int): Int
    fun subRange(start: Int, end: Int): IIntSequence
    fun copyTo(dst: IntArray, dststart: Int, srcstart: Int, srcend: Int)
}
