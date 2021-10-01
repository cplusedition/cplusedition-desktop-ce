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
package sf.andrians.ancoreutil

/**
 * A simple monotonic serial counter that wraps on overflow.
 * By default, it count from 0..Long.MAX_VALUE inclusive at both end.
 */
open class Serial {
    protected val start: Long
    protected val end: Long
    protected var serial: Long

    constructor(): this(0L)

    constructor(start: Long, end: Long = Long.MAX_VALUE) {
        this.start = start
        this.end = end
        serial = start - 1
    }

    constructor(start: Long, end: Long, serial: Long) {
        this.start = start
        this.end = end
        this.serial = if (serial < start - 1 || serial >= end) start - 1 else serial
    }

    @Synchronized
    fun get(): Long {
        if (serial == end) {
            serial = start - 1
        }
        return ++serial
    }
}