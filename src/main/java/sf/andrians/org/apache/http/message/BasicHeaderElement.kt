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

import sf.andrians.org.apache.http.HeaderElement
import sf.andrians.org.apache.http.NameValuePair
import sf.andrians.org.apache.http.util.Args
import sf.andrians.org.apache.http.util.LangUtils

/**
 * Basic implementation of [HeaderElement]
 *
 * @since 4.0
 */
class BasicHeaderElement  constructor(
        name: String,
        value: String?,
        parameters: Array<NameValuePair>? = null) : HeaderElement, Cloneable {
    override val name: String
    override val value: String?
    private val parameters: Array<NameValuePair>

    /**
     * Constructor with name, value and parameters.
     *
     * @param name header element name
     * @param value header element value. May be `null`
     * @param parameters header element parameters. May be `null`.
     * Parameters are copied by reference, not by value
     */
    /**
     * Constructor with name and value.
     *
     * @param name header element name
     * @param value header element value. May be `null`
     */
    init {
        this.name = Args.notNull(name, "Name")
        this.value = value
        if (parameters != null) {
            this.parameters = parameters
        } else {
            this.parameters = arrayOf()
        }
    }

    override fun getParameters(): Array<NameValuePair>? {
        return parameters.clone()
    }

    override val parameterCount: Int
        get() = parameters.size

    override fun getParameter(index: Int): NameValuePair {
        return parameters[index]
    }

    override fun getParameterByName(name: String): NameValuePair? {
        Args.notNull(name, "Name")
        var found: NameValuePair? = null
        for (current in parameters) {
            if (current.name.equals(name, ignoreCase = true)) {
                found = current
                break
            }
        }
        return found
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is HeaderElement) {
            val that = other as BasicHeaderElement
            return (name == that.name && LangUtils.equals(value, that.value)
                    && LangUtils.equals(parameters, that.parameters))
        }
        return false
    }

    override fun hashCode(): Int {
        var hash = LangUtils.HASH_SEED
        hash = LangUtils.hashCode(hash, name)
        hash = LangUtils.hashCode(hash, value)
        for (parameter in parameters) {
            hash = LangUtils.hashCode(hash, parameter)
        }
        return hash
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(name)
        if (value != null) {
            buffer.append("=")
            buffer.append(value)
        }
        for (parameter in parameters) {
            buffer.append("; ")
            buffer.append(parameter)
        }
        return buffer.toString()
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}
