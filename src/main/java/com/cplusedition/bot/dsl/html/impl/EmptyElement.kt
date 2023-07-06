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

import com.cplusedition.bot.dsl.html.api.Attribute
import com.cplusedition.bot.dsl.html.api.IAttr
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
import com.cplusedition.bot.dsl.html.api.TAG

open class EmptyElement : IElement {
    private var tag: String
    private var id: String? = null
    protected val attributes: MutableList<IAttribute> = ArrayList(4)

    constructor(tagname: String) {
        tag = tagname
    }

    constructor(tagname: String, vararg attrs: IAttr) {
        tag = tagname
        for (a in attrs) {
            a.addTo(this)
        }
    }

    constructor(tag: TAG) {
        this.tag = tag.name
    }

    override fun tag(): String {
        return tag
    }

    override fun id(): String? {
        return id
    }

    override fun childCount(): Int {
        return 0
    }

    override fun attrCount(): Int {
        return attributes.size
    }

    override fun children(): Iterable<IChild> {
        return emptyList()
    }

    override fun attributes(): Iterable<IAttribute> {
        return attributes
    }

    override fun id(id: Any): IElement {
        this.id = id.toString()
        return this
    }

    override fun css(css: Any): IElement {
        return a("css", css)
    }

    override fun css(vararg csss: Any): IElement {
        return a("css", *csss)
    }

    override fun type(type: Any): IElement {
        return a("type", type)
    }

    override fun name(name: Any): IElement {
        return a("name", name)
    }

    override fun value(value: Any): IElement {
        return a("value", value)
    }

    override fun content(value: Any): IElement {
        return a("content", value)
    }

    override fun label(label: Any): IElement {
        return a("label", label)
    }

    override fun width(width: Any): IElement {
        return a("width", width)
    }

    override fun href(url: Any): IElement {
        return a("href", url)
    }

    override fun rel(rel: Any): IElement {
        return a("rel", rel)
    }

    override fun src(url: Any): IElement {
        return a("src", url)
    }

    override fun style(style: Any): IElement {
        return a("style", style)
    }

    override fun raw(vararg content: String): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: raw")
    }

    override fun txt(vararg content: String): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: txt")
    }

    override fun esc(vararg content: String): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: esc")
    }

    override fun a(child: IFragment): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: IElement): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: IText): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: ICData): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: IComment): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: IPI): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: IDeclaration): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(child: INode): IElement {
        throw UnsupportedOperationException("Empty element cannot has children: " + child.javaClass)
    }

    override fun a(a: IAttributes): IElement {
        for (aa in a) {
            attributes.add(aa)
        }
        return this
    }

    override fun a(a: IAttribute): IElement {
        attributes.add(a)
        return this
    }

    override fun a(name: String, value: Any): IElement {
        attributes.add(Attribute(name, value))
        return this
    }

    override fun a(name: String, vararg values: Any): IElement {
        attributes.add(Attribute(name, *values))
        return this
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

    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun <T> accept(visitor: INodeVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    protected fun setTag(tagname: String) {
        tag = tagname
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}