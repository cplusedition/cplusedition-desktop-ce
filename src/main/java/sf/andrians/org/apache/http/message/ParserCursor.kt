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
package sf.andrians.org.apache.http.message

/**
 * This class represents a context of a parsing operation:
 *
 *  * the current position the parsing operation is expected to start at
 *  * the bounds limiting the scope of the parsing operation
 *
 *
 * @since 4.0
 */
class ParserCursor(lowerBound: Int, upperBound: Int) {
    val lowerBound: Int
    val upperBound: Int
    var pos: Int
        private set

    fun updatePos(pos: Int) {
        if (pos < lowerBound) {
            throw IndexOutOfBoundsException("pos: " + pos + " < lowerBound: " + lowerBound)
        }
        if (pos > upperBound) {
            throw IndexOutOfBoundsException("pos: " + pos + " > upperBound: " + upperBound)
        }
        this.pos = pos
    }

    fun atEnd(): Boolean {
        return pos >= upperBound
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append('[')
        buffer.append(Integer.toString(lowerBound))
        buffer.append('>')
        buffer.append(Integer.toString(pos))
        buffer.append('>')
        buffer.append(Integer.toString(upperBound))
        buffer.append(']')
        return buffer.toString()
    }

    init {
        if (lowerBound < 0) {
            throw IndexOutOfBoundsException("Lower bound cannot be negative")
        }
        if (lowerBound > upperBound) {
            throw IndexOutOfBoundsException("Lower bound cannot be greater then upper bound")
        }
        this.lowerBound = lowerBound
        this.upperBound = upperBound
        pos = lowerBound
    }
}