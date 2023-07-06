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
package com.cplusedition.bot.dsl.html.impl

import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.api.INodeVisitor
import com.cplusedition.bot.dsl.html.api.IPI

class PI(val target: String, vararg content: String) : IPI {
    val text: String

    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun <T> accept(visitor: INodeVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    override fun toString(): String {
        return "<?$target $text ?>"
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        text = content.joinToString(" ")
    }
}