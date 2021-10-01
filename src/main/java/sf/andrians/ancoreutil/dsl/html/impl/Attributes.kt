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
package sf.andrians.ancoreutil.dsl.html.impl

import sf.andrians.ancoreutil.dsl.html.api.*
import sf.andrians.ancoreutil.util.text.TextUtil.join
import java.util.*

class Attributes : IAttributes {
    private var linebreaks = false
    private var broken = true
    private val attributes: MutableList<IAttribute> = ArrayList(4)

    constructor() {}
    constructor(linebreaks: Boolean) {
        this.linebreaks = linebreaks
    }

    constructor(attr: IAttribute) {
        a1(attr)
    }

    constructor(vararg attrs: IAttribute) {
        aa(*attrs)
    }

    constructor(linebreaks: Boolean, attr: IAttribute) {
        this.linebreaks = linebreaks
        a1(attr)
    }

    constructor(linebreaks: Boolean, vararg attrs: IAttribute) {
        this.linebreaks = linebreaks
        aa(*attrs)
    }

    //////////////////////////////////////////////////////////////////////
    override fun lb(): IAttributes {
        if (!broken) {
            attributes.add(Attribute.Companion.LB)
            broken = true
        }
        return this
    }

    override fun a(attr: IAttribute): IAttributes {
        return a1(attr)
    }

    override fun a(vararg attrs: IAttribute): IAttributes {
        for (attr in attrs) {
            a1(attr)
        }
        return this
    }

    override fun a(attrs: IAttributes): IAttributes {
        for (attr in attrs) {
            a1(attr)
        }
        return this
    }

    override fun a(vararg attrs: IAttributes): IAttributes {
        for (a in attrs) {
            for (attr in a) {
                a1(attr)
            }
        }
        return this
    }

    override fun a(vararg attrs: IAttr): IAttributes {
        for (n in attrs) {
            n.addTo(this)
        }
        return this
    }

    override fun a(name: Any, value: Boolean): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: Any, value: Int): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: Any, value: Long): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: Any, value: Float): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: Any, value: Double): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: Any, value: Any?): IAttributes {
        return a1(Attribute(name, value))
    }

    override fun a(name: Any, vararg value: Any): IAttributes {
        return a1(Attribute(name, *value))
    }

    override fun a(name: String, value: Boolean): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: String, value: Int): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: String, value: Long): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: String, value: Float): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: String, value: Double): IAttributes {
        return a1(Attribute(name, value.toString()))
    }

    override fun a(name: String, value: Any?): IAttributes {
        return a1(Attribute(name, value))
    }

    override fun a(name: String, vararg value: Any): IAttributes {
        return a1(Attribute(name, *value))
    }

    override fun xmlns(url: String): IAttributes {
        return a1(Xmlns("xmlns", url))
    }

    override fun xmlns(name: String, url: String): IAttributes {
        return a1(Xmlns("xmlns:$name", url))
    }

    override fun id(id: Any): IAttributes {
        return a1(Attribute("id", id))
    }

    override fun css(css: Any): IAttributes {
        return a1(Attribute("class", css))
    }

    override fun css(vararg classes: Any): IAttributes {
        return a1(Attribute("class", join<Any>(" ", *classes)))
    }

    override fun type(type: Any): IAttributes {
        return a1(Attribute("type", type))
    }

    override fun name(name: Any): IAttributes {
        return a1(Attribute("name", name))
    }

    override fun value(value: Any): IAttributes {
        return a1(Attribute("value", value))
    }

    override fun content(value: Any): IAttributes {
        return a1(Attribute("content", value))
    }

    override fun label(label: Any): IAttributes {
        return a1(Attribute("label", label))
    }

    override fun checked(): IAttributes {
        return a1(Attribute("checked", "true"))
    }

    override fun selected(): IAttributes {
        return a1(Attribute("selected", "true"))
    }

    override fun width(width: Any): IAttributes {
        return a1(Attribute("width", width))
    }

    override fun href(url: Any): IAttributes {
        return a1(Attribute("href", url))
    }

    override fun rel(rel: Any): IAttributes {
        return a1(Attribute("rel", rel))
    }

    override fun src(url: Any): IAttributes {
        return a1(Attribute("src", url))
    }

    override fun style(style: Any): IAttributes {
        return a1(Attribute("style", style))
    }

    override fun colspan(n: Int): IAttributes {
        return a1(Attribute("colspan", n.toString()))
    }

    //////////////////////////////////////////////////////////////////////
    override fun onload(script: String): IAttributes {
        return a1(Attribute("onload", script))
    }

    override fun onunload(script: String): IAttributes {
        return a1(Attribute("onunload", script))
    }

    override fun onclick(script: String): IAttributes {
        return a1(Attribute("onclick", script))
    }

    override fun ondblclick(script: String): IAttributes {
        return a1(Attribute("ondblclick", script))
    }

    override fun onmousedown(script: String): IAttributes {
        return a1(Attribute("onmousedown", script))
    }

    override fun onmouseup(script: String): IAttributes {
        return a1(Attribute("onmouseup", script))
    }

    override fun onmouseover(script: String): IAttributes {
        return a1(Attribute("onmouseover", script))
    }

    override fun onmousemove(script: String): IAttributes {
        return a1(Attribute("onmousemove", script))
    }

    override fun onmouseout(script: String): IAttributes {
        return a1(Attribute("onmouseout", script))
    }

    override fun onfocus(script: String): IAttributes {
        return a1(Attribute("onfocus", script))
    }

    override fun onblur(script: String): IAttributes {
        return a1(Attribute("onblur", script))
    }

    override fun onkeypress(script: String): IAttributes {
        return a1(Attribute("onkeypress", script))
    }

    override fun onkeydown(script: String): IAttributes {
        return a1(Attribute("onkeydown", script))
    }

    override fun onkeyup(script: String): IAttributes {
        return a1(Attribute("onkeyup", script))
    }

    override fun onsubmit(script: String): IAttributes {
        return a1(Attribute("onsubmit", script))
    }

    override fun onreset(script: String): IAttributes {
        return a1(Attribute("onreset", script))
    }

    override fun onselect(script: String): IAttributes {
        return a1(Attribute("onselect", script))
    }

    //////////////////////////////////////////////////////////////////////

    override fun onload(vararg scripts: String): IAttributes {
        return a1(Attribute("onload", join(" ", *scripts)))
    }

    override fun onunload(vararg scripts: String): IAttributes {
        return a1(Attribute("onunload", join(" ", *scripts)))
    }

    override fun onclick(vararg scripts: String): IAttributes {
        return a1(Attribute("onclick", join(" ", *scripts)))
    }

    override fun ondblclick(vararg scripts: String): IAttributes {
        return a1(Attribute("ondblclick", join(" ", *scripts)))
    }

    override fun onmousedown(vararg scripts: String): IAttributes {
        return a1(Attribute("onmousedown", join(" ", *scripts)))
    }

    override fun onmouseup(vararg scripts: String): IAttributes {
        return a1(Attribute("onmouseup", join(" ", *scripts)))
    }

    override fun onmouseover(vararg scripts: String): IAttributes {
        return a1(Attribute("onmouseover", join(" ", *scripts)))
    }

    override fun onmousemove(vararg scripts: String): IAttributes {
        return a1(Attribute("onmousemove", join(" ", *scripts)))
    }

    override fun onmouseout(vararg scripts: String): IAttributes {
        return a1(Attribute("onmouseout", join(" ", *scripts)))
    }

    override fun onfocus(vararg scripts: String): IAttributes {
        return a1(Attribute("onfocus", join(" ", *scripts)))
    }

    override fun onblur(vararg scripts: String): IAttributes {
        return a1(Attribute("onblur", join(" ", *scripts)))
    }

    override fun onkeypress(vararg scripts: String): IAttributes {
        return a1(Attribute("onkeypress", join(" ", *scripts)))
    }

    override fun onkeydown(vararg scripts: String): IAttributes {
        return a1(Attribute("onkeydown", join(" ", *scripts)))
    }

    override fun onkeyup(vararg scripts: String): IAttributes {
        return a1(Attribute("onkeyup", join(" ", *scripts)))
    }

    override fun onsubmit(vararg scripts: String): IAttributes {
        return a1(Attribute("onsubmit", join(" ", *scripts)))
    }

    override fun onreset(vararg scripts: String): IAttributes {
        return a1(Attribute("onreset", join(" ", *scripts)))
    }

    override fun onselect(vararg scripts: String): IAttributes {
        return a1(Attribute("onselect", join(" ", *scripts)))
    }

    //////////////////////////////////////////////////////////////////////

    override fun addTo(attrs: IAttributes) {
        for (a in attributes) {
            attrs.a(a)
        }
    }

    override fun addTo(e: IElement) {
        for (a in attributes) {
            e.a(a)
        }
    }

    //////////////////////////////////////////////////////////////////////
    override fun iterator(): MutableIterator<IAttribute> {
        return attributes.iterator()
    }

    //////////////////////////////////////////////////////////////////////
    private fun aa(vararg attrs: IAttribute) {
        for (attr in attrs) {
            a1(attr)
        }
    }

    private fun a1(attr: IAttribute): IAttributes {
        if (linebreaks && !broken) {
            attributes.add(Attribute.Companion.LB)
        }
        attributes.add(attr)
        broken = false
        return this
    } //////////////////////////////////////////////////////////////////////

    companion object {
        //////////////////////////////////////////////////////////////////////
        private const val serialVersionUID = 1L
    }
}