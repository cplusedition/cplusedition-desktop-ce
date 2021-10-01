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
package sf.andrians.ancoreutil.dsl.script.impl

import sf.andrians.ancoreutil.dsl.script.api.IBlock
import sf.andrians.ancoreutil.dsl.script.api.IScriptBuilder
import sf.andrians.ancoreutil.dsl.script.api.IScriptChild
import sf.andrians.ancoreutil.dsl.script.api.IStmt
import sf.andrians.ancoreutil.util.text.TextUtil.format
import sf.andrians.ancoreutil.util.text.TextUtil.quote

class ScriptBuilder : IScriptBuilder {
    private val b = StringBuilder()
    private var indent = ""
    private var tab = "  "

    constructor() {}
    constructor(tab: String) {
        this.tab = tab
    }

    constructor(indent: String, tab: String) {
        this.indent = indent
        this.tab = tab
    }

    override fun script(vararg children: IScriptChild): String {
        for (child in children) {
            child.renderTo(b, indent, tab)
        }
        return b.toString()
    }

    override fun stm(vararg clauses: CharSequence): IStmt {
        return Stmt(*clauses)
    }

    override fun blk(vararg children: IScriptChild): IBlock {
        return Block(*children)
    }

    override fun blk(header: CharSequence, vararg children: IScriptChild): IBlock {
        return Block(header, children)
    }

    override fun func(vararg children: IScriptChild): IBlock {
        return Block("function() {", children)
    }

    override fun func(args: CharSequence, vararg children: IScriptChild): IBlock {
        return Block("function($args) {", children)
    }

    override fun fmt(format: String, vararg args: Any): IStmt {
        return Stmt(format(format, *args))
    }

    override fun sel(vararg selectors: String): IStmt {
        val b = StringBuilder("$(")
        var i = 0
        val len = selectors.size
        while (i < len) {
            if (i > 0) {
                b.append(", ")
            }
            b.append(quote(selectors[i]))
            ++i
        }
        b.append(")")
        return Stmt(b.toString())
    }
}