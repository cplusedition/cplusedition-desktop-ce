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

/** IIntQueue is a static/dynamic random read/writable sequence of integers.  */
interface IIntQueue : IIntVector {

    /** Same as push().  */
    fun queue(e: Int)

    /** Same as shift().  */
    fun unqueue(): Int

    /** Remove object from the head.  */
    fun shift(): Int

    /** Remove object from the head.  */
    fun shift(n: Int)

    /** Add element to tail.  */
    fun push(value: Int)

    /** Remove last element from the tail.  */
    fun pop(): Int

    /**
     * Remove given number of elements from the tail.
     * @return The removed element with smallest index.
     */
    fun pop(n: Int): Int

    /** Same as peek(1).  */
    fun peek(): Int

    /** @return The nth object (1 based) from the tail. eg. to the last object, use peek(1).
     */
    fun peek(nth: Int): Int

    /** Truncate size to given length.  */
    fun setLength(n: Int)

    fun clear()

    ////////////////////////////////////////////////////////////////////////
}
