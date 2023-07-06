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

import com.cplusedition.bot.dsl.css.api.*
import com.cplusedition.bot.dsl.css.api.support.IRulesetChild
import java.util.*

class Ruleset : IRuleset {
    override val selectors: MutableCollection<ISelector> = LinkedList()
    override val declarations: MutableCollection<IDeclaration> = LinkedList()
    private val properties = TreeMap<String?, IDeclaration?>()

    ////////////////////////////////////////////////////////////////////////

    constructor(sel: Any, vararg children: IRulesetChild) {
        add(Selector(sel.toString()))
        for (c in children) {
            c.addTo(this)
        }
    }

    constructor(vararg children: IRulesetChild) {
        for (c in children) {
            c.addTo(this)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    override fun add(vararg sels: ISelector): IRuleset {
        Collections.addAll(selectors, *sels)
        return this
    }

    override fun add(vararg decls: IDeclaration): IRuleset {
        for (decl in decls) {
            a(decl)
        }
        return this
    }

    override fun add(vararg rules: IRuleset): IRuleset {
        for (rule in rules) {
            for (decl in rule.declarations) {
                a(decl)
            }
        }
        return this
    }

    override fun add(vararg decls: IDeclarations): IRuleset {
        for (decl in decls) {
            for (d in decl.declarations) {
                a(d)
            }
        }
        return this
    }

    override fun addTo(stylesheet: IStylesheet) {
        stylesheet.add(this)
    }

    override fun addTo(ruleset: IRuleset) {
        ruleset.add(this)
    }

    ////////////////////////////////////////////////////////////////////////

    override fun add(sel: ISelector): IRuleset {
        selectors.add(sel)
        return this
    }

    override fun add(decl: IDeclaration): IRuleset {
        a(decl)
        return this
    }

    override fun add(rule: IRuleset): IRuleset {
        for (decl in rule.declarations) {
            a(decl)
        }
        return this
    }

    override fun add(decls: IDeclarations): IRuleset {
        for (decl in decls) {
            a(decl)
        }
        return this
    }

    ////////////////////////////////////////////////////////////////////////

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    ////////////////////////////////////////////////////////////////////////

    private fun a(decl: IDeclaration): Boolean {
        val p = decl.property
        val o = properties[decl.property]
        if (o != null && o.expr.equals(decl.expr)) {
            return false
        }
        properties[p] = decl
        declarations.add(decl)
        return true
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
