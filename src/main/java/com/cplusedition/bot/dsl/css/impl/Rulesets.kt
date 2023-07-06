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
import com.cplusedition.bot.dsl.css.api.IRulesets
import com.cplusedition.bot.dsl.css.api.IStylesheet
import com.cplusedition.bot.dsl.css.api.support.IStylesheetChild
import java.util.*

class Rulesets(vararg children: IStylesheetChild) : IRulesets {
    override val children: MutableCollection<IStylesheetChild> = LinkedList()

    override fun add(vararg children: IStylesheetChild): Boolean {
        var added = false
        for (child in children) {
            added = added or add(child)
        }
        return added
    }

    override fun add(children: Collection<IStylesheetChild>): Boolean {
        var added = false
        for (child in children) {
            added = added or add(child)
        }
        return added
    }

    override fun add(child: IStylesheetChild): Boolean {
        return children.add(child)
    }

    override fun addTo(stylesheet: IStylesheet) {
        for (child in children) {
            child.addTo(stylesheet)
        }
    }

    override fun iterator(): MutableIterator<IStylesheetChild> {
        return children.iterator()
    }

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        throw AssertionError("ASSERT: Should not reach here")
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        add(*children)
    }
}
