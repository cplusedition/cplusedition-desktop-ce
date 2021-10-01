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

class ListIterable<T>(private val list: List<T>, private var start: Int, private val end: Int) : Iterable<T>, MutableIterator<T> {
    override fun iterator(): MutableIterator<T> {
        return this
    }

    override fun hasNext(): Boolean {
        return start < end
    }

    override fun next(): T {
        return list[start++]
    }

    override fun remove() {
        throw UnsupportedOperationException()
    }

}