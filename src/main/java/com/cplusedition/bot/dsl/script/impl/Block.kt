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
package com.cplusedition.bot.dsl.script.impl

import com.cplusedition.bot.core.LS
import com.cplusedition.bot.dsl.script.api.IBlock
import com.cplusedition.bot.dsl.script.api.IScriptChild
import com.cplusedition.bot.dsl.script.api.IStmt

class Block(private val header: CharSequence, private val children: Array<out IScriptChild>) : IBlock {

    constructor(vararg children: IScriptChild) : this(HEADER, children)

    companion object {
        val HEADER = "{"
    }

    override fun chain(vararg stmts: CharSequence): IStmt {
        return Chain(this, *stmts)
    }

    override fun renderTo(ret: StringBuilder, indent: String, tab: String) {
        ret.append(indent)
        ret.append(header)
        if (children.isNotEmpty()) {
            ret.append(LS)
            val indent1 = indent + tab
            for (child in children) {
                child.renderTo(ret, indent1, tab)
            }
            ret.append(indent)
        }
        when (header[header.length - 1]) {
            '{' -> ret.append('}')
            '(' -> ret.append(')')
        }
    }

}
