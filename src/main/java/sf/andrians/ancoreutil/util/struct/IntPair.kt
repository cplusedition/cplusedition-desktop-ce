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
 * Wrapper class to hold two int values.
 */
class IntPair(var first: Int, var second: Int) {
    fun first(): Int {
        return first
    }

    fun second(): Int {
        return second
    }

    fun toString(sep: String): String {
        return first.toString() + sep + second
    }

    override fun hashCode(): Int {
        return first * 31 xor second
    }

    override fun equals(o: Any?): Boolean {
        if (o !is IntPair) return false
        val p = o
        return p.first == first && p.second == second
    }

    override fun toString(): String {
        return toString(", ")
    }

}