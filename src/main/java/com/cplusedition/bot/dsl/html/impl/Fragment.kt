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

import com.cplusedition.bot.dsl.html.api.IAttribute
import com.cplusedition.bot.dsl.html.api.IAttributes
import com.cplusedition.bot.dsl.html.api.ICData
import com.cplusedition.bot.dsl.html.api.IChild
import com.cplusedition.bot.dsl.html.api.IComment
import com.cplusedition.bot.dsl.html.api.IDeclaration
import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.api.IFragment
import com.cplusedition.bot.dsl.html.api.INode
import com.cplusedition.bot.dsl.html.api.INodeVisitor
import com.cplusedition.bot.dsl.html.api.IPI
import com.cplusedition.bot.dsl.html.api.IText

/**
 * An invisible holder element with no attributes that simply group nodes together.
 * When this is added to another element, it adds the children to the element instead.
 */
class Fragment : IFragment {
    private val children: MutableList<IChild> = ArrayList(4)
    override fun <T> accept(visitor: INodeVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    override fun tag(): String {
        throw UnsupportedOperationException()
    }

    override fun id(): String? {
        throw UnsupportedOperationException()
    }

    override fun raw(vararg content: String): IElement {
        throw UnsupportedOperationException()
    }

    override fun txt(vararg content: String): IElement {
        throw UnsupportedOperationException()
    }

    override fun esc(vararg content: String): IElement {
        throw UnsupportedOperationException()
    }

    override fun childCount(): Int {
        return 0
    }

    override fun attrCount(): Int {
        throw UnsupportedOperationException()
    }

    override fun children(): Iterable<IChild> {
        return children
    }

    override fun attributes(): Iterable<IAttribute> {
        throw UnsupportedOperationException()
    }

    override fun id(id: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun css(css: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun css(vararg csss: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun type(type: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun name(name: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun value(value: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun content(value: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun label(label: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun width(width: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun href(url: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun rel(rel: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun src(url: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun style(style: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun a(a: IAttributes): IElement {
        throw UnsupportedOperationException()
    }

    override fun a(a: IAttribute): IElement {
        throw UnsupportedOperationException()
    }

    override fun a(name: String, value: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun a(name: String, vararg values: Any): IElement {
        throw UnsupportedOperationException()
    }

    override fun c(vararg children: IChild): IElement {
        for (child in children) {
            child.addTo(this)
        }
        return this
    }

    override fun n(vararg children: INode): IElement {
        for (child in children) {
            child.addTo(this)
        }
        return this
    }

    override fun a(child: IFragment): IElement {
        for (c in child.children()) {
            children.add(c)
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

    override fun a(child: IDeclaration): IElement {
        children.add(child)
        return this
    }

    override fun a(child: INode): IElement {
        throw AssertionError("ERROR: Should not reach here")
    }

    override fun addTo(e: IElement) {
        for (c in children) {
            c.addTo(e)
        }
    }
}