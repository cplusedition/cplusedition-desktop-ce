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
package sf.andrians.ancoreutil.dsl.css.impl

import sf.andrians.ancoreutil.dsl.css.api.IAOp
import sf.andrians.ancoreutil.dsl.css.api.ICSSVisitor
import sf.andrians.ancoreutil.dsl.css.api.IRuleset
import sf.andrians.ancoreutil.dsl.css.api.ISelector

class Selector : ISelector {
    private val expr = StringBuilder()

    constructor(sel: Any) {
        sel(sel)
    }

    constructor(vararg sels: Any) {
        sel(*sels)
    }

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    override fun sel(sel: Any): ISelector {
        val s = sel.toString()
        if (s.length > 0) {
            if (expr.length > 0) {
                expr.append(", ")
            }
            expr.append(s)
        }
        return this
    }

    override fun sel(vararg sels: Any): ISelector {
        for (sel in sels) {
            sel(sel)
        }
        return this
    }

    override fun id(id: Any): ISelector {
        expr.append('#')
        expr.append(id.toString())
        return this
    }

    override fun css(cls: Any): ISelector {
        expr.append('.')
        expr.append(cls.toString())
        return this
    }

    override fun psc(pseudo: Any): ISelector {
        expr.append(':')
        expr.append(pseudo.toString())
        return this
    }

    override fun pse(pseudo: Any): ISelector {
        expr.append("::")
        expr.append(pseudo.toString())
        return this
    }

    override fun attr(expr: Any): ISelector {
        this.expr.append('[')
        this.expr.append(expr.toString())
        this.expr.append(']')
        return this
    }

    override fun attr(name: Any, op: IAOp, value: Any): ISelector {
        expr.append('[')
        expr.append(name.toString())
        expr.append(op.toString())
        expr.append('"')
        expr.append(value.toString())
        expr.append("\"]")
        return this
    }

    override fun desc(sel: Any): ISelector {
        return a(sel)
    }

    override fun desc(vararg sels: Any): ISelector {
        return a(*sels)
    }

    override fun child(sel: Any): ISelector {
        return c(sel)
    }

    override fun child(vararg sels: Any): ISelector {
        return c(*sels)
    }

    override fun silbing(sel: Any): ISelector {
        expr.append('+')
        expr.append(sel.toString())
        return this
    }

    override fun c(sel: Any): ISelector {
        expr.append('>')
        expr.append(sel.toString())
        return this
    }

    override fun c(vararg sels: Any): ISelector {
        for (sel in sels) {
            expr.append('>')
            expr.append(sel.toString())
        }
        return this
    }

    override fun a(sel: Any): ISelector {
        expr.append(' ')
        expr.append(sel.toString())
        return this
    }

    override fun a(vararg sels: Any): ISelector {
        for (sel in sels) {
            expr.append(' ')
            expr.append(sel.toString())
        }
        return this
    }

    override fun addTo(ruleset: IRuleset) {
        ruleset.add(this)
    }

    override fun toString(): String {
        return expr.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}