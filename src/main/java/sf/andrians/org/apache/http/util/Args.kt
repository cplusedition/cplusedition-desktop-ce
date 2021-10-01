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

object Args {
    fun check(expression: Boolean, message: String) {
        require(expression) { message }
    }

    fun check(expression: Boolean, message: String, vararg args: Any) {
        require(expression) { String.format(message, *args) }
    }

    fun check(expression: Boolean, message: String, arg: Any?) {
        require(expression) { String.format(message, arg) }
    }

    fun <T : Any> notNull(argument: T?, name: String): T {
        requireNotNull(argument) { "$name may not be null" }
        return argument
    }

    fun <T : CharSequence> notEmpty(argument: T?, name: String): T {
        requireNotNull(argument) { "$name may not be null" }
        require(!TextUtils.isEmpty(argument)) { "$name may not be empty" }
        return argument
    }

    fun <T : CharSequence> notBlank(argument: T?, name: String): T {
        requireNotNull(argument) { "$name may not be null" }
        require(!TextUtils.isBlank(argument)) { "$name may not be blank" }
        return argument
    }

    fun <T : CharSequence> containsNoBlanks(argument: T?, name: String): T {
        requireNotNull(argument) { "$name may not be null" }
        require(argument.length != 0) { "$name may not be empty" }
        require(!TextUtils.containsBlanks(argument)) { "$name may not contain blanks" }
        return argument
    }

    fun <E, T : Collection<E>> notEmpty(argument: T?, name: String): T {
        requireNotNull(argument) { "$name may not be null" }
        require(!argument.isEmpty()) { "$name may not be empty" }
        return argument
    }

    fun positive(n: Int, name: String): Int {
        require(n > 0) { "$name may not be negative or zero" }
        return n
    }

    fun positive(n: Long, name: String): Long {
        require(n > 0) { "$name may not be negative or zero" }
        return n
    }

    fun notNegative(n: Int, name: String): Int {
        require(n >= 0) { "$name may not be negative" }
        return n
    }

    fun notNegative(n: Long, name: String): Long {
        require(n >= 0) { "$name may not be negative" }
        return n
    }
}