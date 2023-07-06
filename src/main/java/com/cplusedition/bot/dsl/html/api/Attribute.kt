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
package com.cplusedition.bot.dsl.html.api

open class Attribute : IAttribute {
    private val name: String
    private val value: String?

    constructor(name: Any, value: Any?) {
        validate(value)
        this.name = name.toString()
        this.value = value?.toString()
    }

    constructor(name: Any, vararg values: Any) {
        for (value in values) {
            validate(value)
        }
        this.name = name.toString()
        value = if (values.isEmpty()) null else values.joinToString(" ")
    }

    constructor(name: String, value: String) {
        this.name = name
        this.value = value
    }

    constructor(name: String, vararg values: String) {
        this.name = name
        value = if (values.isEmpty()) null else values.joinToString(" ")
    }

    override fun aname(): String {
        return name
    }

    override fun avalue(): String? {
        return value
    }

    //////////////////////////////////////////////////////////////////////
    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun addTo(attributes: IAttributes) {
        attributes.a(this)
    }

    private fun validate(value: Any?) {
        if (value == null || value is String || value is Number || value is Enum<*>) {
            return
        }
        throw IllegalArgumentException(
                "Expecting a String, Number, or Enum object, actual: " + value.javaClass.name)
    }

    companion object {
        private const val serialVersionUID = 1L
        var LB = Attribute("", null as String?)
    }
}