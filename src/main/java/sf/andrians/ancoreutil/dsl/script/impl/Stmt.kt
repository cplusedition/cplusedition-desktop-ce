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
import sf.andrians.ancoreutil.dsl.script.api.IScriptChild
import sf.andrians.ancoreutil.dsl.script.api.IStmt

open class Stmt(vararg a: CharSequence) : IStmt {
    protected val stmts: Array<out CharSequence> = a
    override fun chain(vararg stmts: CharSequence): IStmt {
        return Chain(this, *stmts)
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

    override fun renderTo(ret: StringBuilder, indent: String, tab: String) {
        for (s in stmts) {
            ret.append(indent)
            ret.append(s)
            ret.append(IScriptChild.Companion.SEP)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}