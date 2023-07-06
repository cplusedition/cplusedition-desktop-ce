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

import com.cplusedition.bot.dsl.html.api.IDeclaration
import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.api.INodeVisitor
import java.util.*

class Declaration(val name: String, vararg content: String) : IDeclaration {
    val content: Array<String> = Arrays.copyOf(content, content.size)

    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun <T> accept(visitor: INodeVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    override fun toString(): String {
        val b = StringBuilder(64)
        b.append("<!")
        b.append(name)
        for (s in content) {
            b.append(' ')
            b.append(s)
        }
        b.append(">")
        return b.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}