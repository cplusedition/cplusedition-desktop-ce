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

import com.cplusedition.bot.core.XMLUt
import com.cplusedition.bot.dsl.html.api.*

class Element : EmptyElement {
    protected val children: MutableList<IChild> = ArrayList(4)

    constructor(name: Any, vararg children: INode) : super(name.toString()) {
        addAll(this, *children)
    }

    constructor(name: Any, vararg children: Any) : super(name.toString()) {
        addAll(this, *children)
    }

    override fun childCount(): Int {
        return children.size
    }

    override fun children(): Iterable<IChild> {
        return children
    }

    override fun raw(vararg content: String): IElement {
        children.add(Raw(*content))
        return this
    }

    override fun txt(vararg content: String): IElement {
        children.add(Text(*content))
        return this
    }

    override fun esc(vararg content: String): IElement {
        children.add(Text(*content.map { XMLUt.esc(it).toString() }.toTypedArray()))
        return this
    }

    override fun a(child: IFragment): IElement {
        for (cc in child.children()) {
            children.add(cc)
        }
        return this
    }

    override fun a(child: IElement): IElement {
        children.add(child)
        return this
    }

    override fun a(child: IText): IElement {
        children.add(child)
        return this
    }

    override fun a(child: ICData): IElement {
        children.add(child)
        return this
    }

    override fun a(child: IComment): IElement {
        children.add(child)
        return this
    }

    override fun a(child: IPI): IElement {
        children.add(child)
        return this
    }

    override fun a(child: INode): IElement {
        throw AssertionError("ERROR: Should not reach here: " + child.javaClass)
    }

    companion object {
        private const val serialVersionUID = 1L
        fun addAll(e: IElement, vararg children: INode) {
            for (c in children) {
                c.addTo(e)
            }
        }

        fun addAll(e: IElement, vararg children: Any) {
            var i = 0
            val len = children.size
            while (i < len) {
                val c = children[i]
                if (c is CharSequence) {
                    var a: MutableList<String>? = null
                    while (i + 1 < len && children[i + 1] is CharSequence) {
                        if (a == null) {
                            a = ArrayList()
                            a.add(c.toString())
                        }
                        a.add(children[i + 1].toString())
                        ++i
                    }
                    if (a == null) {
                        e.a(Text(c.toString()))
                    } else {
                        e.a(Text(*(a.toTypedArray())))
                    }
                } else if (c is INode) {
                    c.addTo(e)
                } else {
                    throw AssertionError("Unexpected child type: " + c.javaClass)
                }
                ++i
            }
        }
    }
}
