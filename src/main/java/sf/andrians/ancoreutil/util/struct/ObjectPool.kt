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

class ObjectPool<T>  constructor(protected var type: Class<T>, protected var quota: Int = 0) {
    protected var size = 0
    protected var pool: MutableList<T>
    fun get(): T {
        if (size > 0) {
            return pool.removeAt(--size)
        }
        try {
            return type.newInstance()
        } catch (e: InstantiationException) {
        } catch (e: IllegalAccessException) {
        }
        throw RuntimeException("Fail to create new instance for: type=" + type.name)
    }

    fun unget(a: T) {
        if (quota <= 0 || size < quota) {
            pool.add(a)
            ++size
        }
    }

    fun clear() {
        pool.clear()
        size = 0
    }

    /**
     * @param type      Object class.
     * @param quota Pool size limit, 0 for no limit.
     */
    init {
        pool = LinkedList()
    }
}