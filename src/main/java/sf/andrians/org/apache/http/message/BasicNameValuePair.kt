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

import sf.andrians.org.apache.http.NameValuePair
import sf.andrians.org.apache.http.util.Args
import sf.andrians.org.apache.http.util.LangUtils
import java.io.Serializable

/**
 * Basic implementation of [NameValuePair].
 *
 * @since 4.0
 */
class BasicNameValuePair(name: String?, value: String?) : NameValuePair, Cloneable, Serializable {
    override val name: String
    override val value: String?

    override fun toString(): String {
        if (value == null) {
            return name
        }
        val len = name.length + 1 + value.length
        val buffer = StringBuilder(len)
        buffer.append(name)
        buffer.append("=")
        buffer.append(value)
        return buffer.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is NameValuePair) {
            val that = other as BasicNameValuePair
            return name == that.name && LangUtils.equals(value, that.value)
        }
        return false
    }

    override fun hashCode(): Int {
        var hash = LangUtils.HASH_SEED
        hash = LangUtils.hashCode(hash, name)
        hash = LangUtils.hashCode(hash, value)
        return hash
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    companion object {
        private const val serialVersionUID = -6437800749411518984L
    }

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    init {
        this.name = Args.notNull(name, "Name")
        this.value = value
    }
}
