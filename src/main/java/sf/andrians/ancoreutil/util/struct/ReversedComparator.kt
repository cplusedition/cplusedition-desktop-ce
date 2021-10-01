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

import java.io.Serializable
import java.util.*

class ReversedComparator<T>(private val comparator: Comparator<T>) : Comparator<T>, Serializable {
    override fun compare(a: T, b: T): Int {
        return comparator.compare(b, a)
    }

    companion object {
        private const val serialVersionUID = 3290237494982860181L
    }

}