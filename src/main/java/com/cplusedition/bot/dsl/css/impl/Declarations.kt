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
package com.cplusedition.bot.dsl.css.impl

import com.cplusedition.bot.dsl.css.api.ICSSVisitor
import com.cplusedition.bot.dsl.css.api.IDeclaration
import com.cplusedition.bot.dsl.css.api.IDeclarations
import com.cplusedition.bot.dsl.css.api.IRuleset
import java.util.*

class Declarations(vararg decls: IDeclaration) : IDeclarations {
    override val declarations: MutableCollection<IDeclaration> = LinkedList()
    private val properties = TreeMap<String?, IDeclaration>()

    override fun add(decls: Collection<IDeclaration>): Boolean {
        var added = false
        for (decl in decls) {
            added = added or add(decl)
        }
        return added
    }

    override fun add(decl: IDeclaration): Boolean {
        val p = decl.property
        val o = properties[decl.property]
        if (o != null && o.expr.equals(decl.expr)) {
            return false
        }
        properties[p] = decl
        declarations.add(decl)
        return true
    }

    override fun addTo(ruleset: IRuleset) {
        for (decl in declarations) {
            ruleset.add(decl)
        }
    }

    override fun iterator(): MutableIterator<IDeclaration> {
        return declarations.iterator()
    }

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        throw AssertionError("ASSERT: Should not reach here.")
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        for (decl in decls) {
            add(decl)
        }
    }
}
