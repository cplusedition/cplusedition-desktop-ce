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
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package sf.andrians.org.apache.http.util

/**
 * A set of utility methods to help produce consistent
 * [equals][Object.equals] and [hashCode][Object.hashCode] methods.
 *
 *
 * @since 4.0
 */
object LangUtils {
    const val HASH_SEED = 17
    const val HASH_OFFSET = 37
    fun hashCode(seed: Int, hashcode: Int): Int {
        return seed * HASH_OFFSET + hashcode
    }

    fun hashCode(seed: Int, b: Boolean): Int {
        return hashCode(seed, if (b) 1 else 0)
    }

    fun hashCode(seed: Int, obj: Any?): Int {
        return hashCode(seed, obj?.hashCode() ?: 0)
    }

    /**
     * Check if two objects are equal.
     *
     * @param obj1 first object to compare, may be `null`
     * @param obj2 second object to compare, may be `null`
     * @return `true` if the objects are equal or both null
     */
    fun equals(obj1: Any?, obj2: Any?): Boolean {
        return if (obj1 == null) obj2 == null else obj1 == obj2
    }

    /**
     * Check if two object arrays are equal.
     *
     *  * If both parameters are null, return `true`
     *  * If one parameter is null, return `false`
     *  * If the array lengths are different, return `false`
     *  * Compare array elements using .equals(); return `false` if any comparisons fail.
     *  * Return `true`
     *
     *
     * @param a1 first array to compare, may be `null`
     * @param a2 second array to compare, may be `null`
     * @return `true` if the arrays are equal or both null
     */
    fun equals(a1: Array<Any?>?, a2: Array<Any?>?): Boolean {
        if (a1 == null) {
            return a2 == null
        }
        if (a2 != null && a1.size == a2.size) {
            for (i in a1.indices) {
                if (!equals(a1[i], a2[i])) {
                    return false
                }
            }
            return true
        }
        return false
    }
}