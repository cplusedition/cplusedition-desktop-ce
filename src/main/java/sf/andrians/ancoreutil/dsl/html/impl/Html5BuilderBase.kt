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
import sf.andrians.ancoreutil.util.text.TextUtil.lineSeparator
import sf.andrians.ancoreutil.util.text.TextUtil.quote
import sf.andrians.ancoreutil.util.text.XmlUtil
import java.io.File

abstract class Html5BuilderBase<E : IElement> : IHtml5Builder<E> {

    protected var xmlUtil = XmlUtil()

    //////////////////////////////////////////////////////////////////////

    override fun xml(): IPI {
        return xml("1.0", "UTF-8")
    }

    override fun xml(encoding: String?): IPI {
        return PI("xml", "encoding=" + xmlUtil.quoteAttr(encoding))
    }

    override fun xml(version: String?, encoding: String?): IPI {
        return PI("xml", "version=" + xmlUtil.quoteAttr(version), "encoding=" + xmlUtil.quoteAttr(encoding))
    }

    override fun declaration(name: String, vararg contents: String): IDeclaration {
        return Declaration(name, *contents)
    }

    override fun doctype(): IDeclaration {
        return Declaration("DOCTYPE", "html")
    }

    override fun doctype(doctype: String): IDeclaration {
        return Declaration("DOCTYPE", doctype)
    }

    //////////////////////////////////////////////////////////////////////

    override fun title(vararg contents: String): E {
        return elm("title", txt(*contents))
    }

    override fun meta(vararg attrs: IAttr): E {
        return empty("meta", *attrs)
    }

    override fun link(vararg attrs: IAttr): E {
        return empty("link", *attrs)
    }

    override fun base(vararg attrs: IAttr): E {
        return empty("base", *attrs)
    }

    override fun img(vararg attrs: IAttr): E {
        return empty("img", *attrs)
    }

    override fun br(vararg attrs: IAttr): E {
        return empty("br", *attrs)
    }

    override fun colgroup(width1: String, vararg widths: String): E {
        val ret = elm("colgroup")
        ret.a(col(atts("width", width1)))
        for (width in widths) {
            ret.a(col(atts("width", width)))
        }
        return ret
    }

    override fun col(vararg attrs: IAttr): E {
        return empty("col", *attrs)
    }

    override fun input(vararg attrs: IAttr): E {
        return empty("input", *attrs)
    }

    override fun embed(vararg attrs: IAttr): E {
        return empty("embed", *attrs)
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    override fun raw(vararg content: String): IRaw {
        return Raw(*content)
    }

    override fun rawfile(file: File): IRaw {
        return RawFile(file)
    }

    override fun txt(vararg content: String): IText {
        return Text(*content)
    }

    override fun t(vararg content: String): IText {
        return Text(*content)
    }

    override fun esc(vararg content: String): IText {
        val c = Array(content.size) {
            xmlUtil.escText(content[it], true)
        }
        return Text(*c)
    }

    override fun lb(): ILine {
        return Linebreak()
    }

    override fun cdata(vararg content: String): ICData {
        return CData(*content)
    }

    override fun comment(vararg content: String): IComment {
        return Comment(*content)
    }

    override fun pi(target: String, vararg content: String): IPI {
        return PI(target, join(lineSeparator, *content))
    }

    //////////////////////////////////////////////////////////////////////
    override fun attibutes(): IAttributes {
        return Attributes()
    }

    override fun attributes(attr: IAttribute): IAttributes {
        return Attributes(attr)
    }

    override fun attributes(vararg attrs: IAttribute): IAttributes {
        return Attributes(*attrs)
    }

    override fun atts(name: Any, value: Boolean): IAttributes {
        return Attributes(Attribute(name, value.toString()))
    }

    override fun atts(name: Any, value: Int): IAttributes {
        return Attributes(Attribute(name, value.toString()))
    }

    override fun atts(name: Any, value: Long): IAttributes {
        return Attributes(Attribute(name, value.toString()))
    }

    override fun atts(name: Any, value: Float): IAttributes {
        return Attributes(Attribute(name, value.toString()))
    }

    override fun atts(name: Any, value: Double): IAttributes {
        return Attributes(Attribute(name, value.toString()))
    }

    override fun atts(name: Any, value: Any): IAttributes {
        return Attributes(Attribute(name, value))
    }

    override fun atts(linebreaks: Boolean): IAttributes {
        return Attributes(linebreaks)
    }

    override fun atts(vararg namevalues: Any): IAttributes {
        return lb(false, *namevalues)
    }

    override fun lb(vararg attrs: IAttribute): IAttributes {
        val ret: IAttributes = Attributes()
        var first = true
        for (attr in attrs) {
            if (first) {
                first = false
            } else {
                ret.a(Attribute.LB)
            }
            ret.a(attr)
        }
        return ret
    }

    override fun lb(vararg namevalues: Any): IAttributes {
        return lb(true, *namevalues)
    }

    private fun lb(linebreak: Boolean, vararg namevalues: Any): IAttributes {
        val len = namevalues.size
        require(len % 2 == 0) { "ERROR: Expected even number of arguments, actual=$len" }
        val ret = Attributes()
        var i = 0
        while (i < len) {
            if (i > 0 && linebreak) {
                ret.a(Attribute.LB)
            }
            ret.a(namevalues[i], namevalues[i + 1])
            i += 2
        }
        return ret
    }

    override fun attr(name: Any, value: Boolean): IAttribute {
        return Attribute(name, value.toString())
    }

    override fun attr(name: Any, value: Int): IAttribute {
        return Attribute(name, value.toString())
    }

    override fun attr(name: Any, value: Long): IAttribute {
        return Attribute(name, value.toString())
    }

    override fun attr(name: Any, value: Float): IAttribute {
        return Attribute(name, value.toString())
    }

    override fun attr(name: Any, value: Double): IAttribute {
        return Attribute(name, value.toString())
    }

    override fun attr(name: Any, value: Any): IAttribute {
        return Attribute(name, value)
    }

    override fun attr(name: Any, vararg values: Any): IAttribute {
        return Attribute(name, *values)
    }

    override fun colspan(value: Any): IAttribute {
        return Attribute("colspan", value)
    }

    override fun css(value: Any): IAttribute {
        return Attribute("class", value)
    }

    override fun css(vararg values: Any): IAttribute {
        return Attribute("class", *values)
    }

    override fun istyle(style: Any): IAttribute {
        return attr("style", style.toString())
    }

    override fun istyle(vararg styles: Any): IAttribute {
        return attr("style", *styles)
    }

    override fun href(value: Any): IAttribute {
        return Attribute("href", value)
    }

    override fun rel(value: Any): IAttribute {
        return Attribute("rel", value)
    }

    override fun src(value: Any): IAttribute {
        return Attribute("src", value)
    }

    override fun id(value: Any): IAttribute {
        return Attribute("id", value)
    }

    override fun type(value: Any): IAttribute {
        return Attribute("type", value)
    }

    override fun name(value: Any): IAttribute {
        return Attribute("name", value)
    }

    override fun value(value: Any): IAttribute {
        return Attribute("value", value)
    }

    override fun content(value: Any): IAttribute {
        return Attribute("content", value)
    }

    //////////////////////////////////////////////////////////////////////
    override fun onload(script: String): IAttribute {
        return Attribute("onload", script)
    }

    override fun onunload(script: String): IAttribute {
        return Attribute("onunload", script)
    }

    override fun onclick(script: String): IAttribute {
        return Attribute("onclick", script)
    }

    override fun ondblclick(script: String): IAttribute {
        return Attribute("ondblclick", script)
    }

    override fun onmousedown(script: String): IAttribute {
        return Attribute("onmousedown", script)
    }

    override fun onmouseup(script: String): IAttribute {
        return Attribute("onmouseup", script)
    }

    override fun onmouseover(script: String): IAttribute {
        return Attribute("onmouseover", script)
    }

    override fun onmousemove(script: String): IAttribute {
        return Attribute("onmousemove", script)
    }

    override fun onmouseout(script: String): IAttribute {
        return Attribute("onmouseout", script)
    }

    override fun onfocus(script: String): IAttribute {
        return Attribute("onfocus", script)
    }

    override fun onblur(script: String): IAttribute {
        return Attribute("onblur", script)
    }

    override fun onkeypress(script: String): IAttribute {
        return Attribute("onkeypress", script)
    }

    override fun onkeydown(script: String): IAttribute {
        return Attribute("onkeydown", script)
    }

    override fun onkeyup(script: String): IAttribute {
        return Attribute("onkeyup", script)
    }

    override fun onsubmit(script: String): IAttribute {
        return Attribute("onsubmit", script)
    }

    override fun onreset(script: String): IAttribute {
        return Attribute("onreset", script)
    }

    override fun onselect(script: String): IAttribute {
        return Attribute("onselect", script)
    }

    //////////////////////////////////////////////////////////////////////
    override fun contenttype(type: Any): E {
        return meta(atts("http-equiv", "Content-Type", "content", type))
    }

    override fun stylesheet(): E {
        return link(atts("rel", "stylesheet").type("text/css"))
    }

    override fun stylesheet(href: Any): E {
        return link(atts("rel", "stylesheet").type("text/css").href(href))
    }

    override fun javascript(): E {
        return script(type("text/javascript"))
    }

    override fun javascript(src: Any): E {
        return script(attibutes().type("text/javascript").src(src))
    }

    override fun vbox(vararg children: IElement): E {
        val len = children.size
        val rows = Array<IElement>(len) {
            tr(td(children[it]))
        }
        return table(*rows)
    }

    override fun hbox(vararg children: IElement): E {
        val len = children.size
        val cols = Array<IElement>(len) {
            td(children[it])
        }
        return table(tr(*cols))
    }

    //////////////////////////////////////////////////////////////////////

    override fun top(vararg children: Any): ITop {
        val parent: ITop = Top()
        Element.addAll(parent, *children)
        return parent
    }

    override fun html(vararg children: Any): E {
        return elm("html", *children)
    }

    override fun head(vararg children: Any): E {
        return elm("head", *children)
    }

    override fun style(vararg children: Any): E {
        return elm("style", *children)
    }

    override fun script(vararg children: Any): E {
        return elm("script", *children)
    }

    override fun body(vararg children: Any): E {
        return elm("body", *children)
    }

    override fun div(vararg children: Any): E {
        return elm("div", *children)
    }

    override fun span(vararg children: Any): E {
        return elm("span", *children)
    }

    override fun h1(vararg children: Any): E {
        return elm("h1", *children)
    }

    override fun h2(vararg children: Any): E {
        return elm("h2", *children)
    }

    override fun h3(vararg children: Any): E {
        return elm("h3", *children)
    }

    override fun h4(vararg children: Any): E {
        return elm("h4", *children)
    }

    override fun h5(vararg children: Any): E {
        return elm("h5", *children)
    }

    override fun h6(vararg children: Any): E {
        return elm("h6", *children)
    }

    override fun b(vararg children: Any): E {
        return elm("b", *children)
    }

    override fun em(vararg children: Any): E {
        return elm("em", *children)
    }

    override fun code(vararg children: Any): E {
        return elm("code", *children)
    }

    override fun pre(vararg children: Any): E {
        return elm("pre", *children)
    }

    override fun a(vararg children: Any): E {
        return elm("a", *children)
    }

    override fun p(vararg children: Any): E {
        return elm("p", *children)
    }

    override fun blockquote(vararg children: Any): E {
        return elm("blockquote", *children)
    }

    override fun ul(vararg children: Any): E {
        return elm("ul", *children)
    }

    override fun li(vararg children: Any): E {
        return elm("li", *children)
    }

    override fun ol(vararg children: Any): E {
        return elm("ol", *children)
    }

    override fun table(vararg children: Any): E {
        return elm("table", *children)
    }

    override fun thead(vararg children: Any): E {
        return elm("thead", *children)
    }

    override fun tbody(vararg children: Any): E {
        return elm("tbody", *children)
    }

    override fun tfoot(vararg children: Any): E {
        return elm("tfoot", *children)
    }

    override fun colgroup(vararg children: Any): E {
        return elm("colgroup", *children)
    }

    override fun tr(vararg children: Any): E {
        return elm("tr", *children)
    }

    override fun th(vararg children: Any): E {
        return elm("th", *children)
    }

    override fun td(vararg children: Any): E {
        return elm("td", *children)
    }

    override fun form(vararg children: Any): E {
        return elm("form", *children)
    }

    override fun button(vararg children: Any): E {
        return elm("button", *children)
    }

    override fun optgroup(vararg children: Any): E {
        return elm("optgroup", *children)
    }

    override fun option(vararg children: Any): E {
        return elm("option", *children)
    }

    override fun select(vararg children: Any): E {
        return elm("select", *children)
    }

    override fun textarea(vararg children: Any): E {
        return elm("textarea", *children)
    }

    override fun noscript(vararg children: Any): E {
        return elm("noscript", *children)
    }

    override fun iframe(vararg children: Any): E {
        return elm("iframe", *children)
    }

    override fun applet(vararg children: Any): E {
        return elm("applet", *children)
    }

    override fun `object`(vararg children: Any): E {
        return elm("object", *children)
    }

    //////////////////////////////////////////////////////////////////////
    override fun area(vararg attrs: IAttr): E {
        return empty("area", *attrs)
    }

    override fun basefont(vararg attrs: IAttr): E {
        return empty("basefont", *attrs)
    }

    override fun doctype(vararg attrs: IAttr): E {
        return empty("doctype", *attrs)
    }

    override fun frame(vararg attrs: IAttr): E {
        return empty("frame", *attrs)
    }

    override fun isindex(vararg attrs: IAttr): E {
        return empty("isindex", *attrs)
    }

    override fun param(vararg attrs: IAttr): E {
        return empty("param", *attrs)
    }

    override fun spacer(vararg attrs: IAttr): E {
        return empty("spacer", *attrs)
    }

    //////////////////////////////////////////////////////////////////////

    override fun abbr(vararg children: Any): E {
        return elm("abbr", *children)
    }

    override fun acronym(vararg children: Any): E {
        return elm("acronym", *children)
    }

    override fun address(vararg children: Any): E {
        return elm("address", *children)
    }

    override fun bdo(vararg children: Any): E {
        return elm("bdo", *children)
    }

    override fun big(vararg children: Any): E {
        return elm("big", *children)
    }

    override fun caption(vararg children: Any): E {
        return elm("caption", *children)
    }

    override fun center(vararg children: Any): E {
        return elm("center", *children)
    }

    override fun cite(vararg children: Any): E {
        return elm("cite", *children)
    }

    override fun dd(vararg children: Any): E {
        return elm("dd", *children)
    }

    override fun del(vararg children: Any): E {
        return elm("del", *children)
    }

    override fun dfn(vararg children: Any): E {
        return elm("dfn", *children)
    }

    override fun dir(vararg children: Any): E {
        return elm("dir", *children)
    }

    override fun dl(vararg children: Any): E {
        return elm("dl", *children)
    }

    override fun dt(vararg children: Any): E {
        return elm("dt", *children)
    }

    override fun fieldset(vararg children: Any): E {
        return elm("fieldset", *children)
    }

    override fun font(vararg children: Any): E {
        return elm("font", *children)
    }

    override fun frameset(vararg children: Any): E {
        return elm("frameset", *children)
    }

    override fun hr(vararg children: Any): E {
        return elm("hr", *children)
    }

    override fun i(vararg children: Any): E {
        return elm("i", *children)
    }

    override fun ilayer(vararg children: Any): E {
        return elm("ilayer", *children)
    }

    override fun ins(vararg children: Any): E {
        return elm("ins", *children)
    }

    override fun kbd(vararg children: Any): E {
        return elm("kbd", *children)
    }

    override fun label(vararg children: Any): E {
        return elm("label", *children)
    }

    override fun layer(vararg children: Any): E {
        return elm("layer", *children)
    }

    override fun legend(vararg children: Any): E {
        return elm("legend", *children)
    }

    override fun link(vararg children: Any): E {
        return elm("link", *children)
    }

    override fun map(vararg children: Any): E {
        return elm("map", *children)
    }

    override fun menu(vararg children: Any): E {
        return elm("menu", *children)
    }

    override fun meta(vararg children: Any): E {
        return elm("meta", *children)
    }

    override fun multicol(vararg children: Any): E {
        return elm("multicol", *children)
    }

    override fun nobr(vararg children: Any): E {
        return elm("nobr", *children)
    }

    override fun noframes(vararg children: Any): E {
        return elm("noframes", *children)
    }

    override fun nolayer(vararg children: Any): E {
        return elm("nolayer", *children)
    }

    override fun plaintext(vararg children: Any): E {
        return elm("plaintext", *children)
    }

    override fun q(vararg children: Any): E {
        return elm("q", *children)
    }

    override fun s(vararg children: Any): E {
        return elm("s", *children)
    }

    override fun samp(vararg children: Any): E {
        return elm("samp", *children)
    }

    override fun sdfield(vararg children: Any): E {
        return elm("sdfield", *children)
    }

    override fun small(vararg children: Any): E {
        return elm("small", *children)
    }

    override fun strike(vararg children: Any): E {
        return elm("strike", *children)
    }

    override fun strong(vararg children: Any): E {
        return elm("strong", *children)
    }

    override fun sub(vararg children: Any): E {
        return elm("sub", *children)
    }

    override fun sup(vararg children: Any): E {
        return elm("sup", *children)
    }

    override fun term(vararg children: Any): E {
        return elm("term", *children)
    }

    override fun tt(vararg children: Any): E {
        return elm("tt", *children)
    }

    override fun u(vararg children: Any): E {
        return elm("u", *children)
    }

    override fun `var`(vararg children: Any): E {
        return elm("var", *children)
    }

    override fun xmp(vararg children: Any): E {
        return elm("xmp", *children)
    }

    ////////////////////////////////////////////////////////////////////
    override fun source(vararg attrs: IAttr): E {
        return empty("source", *attrs)
    }

    override fun command(vararg attrs: IAttr): E {
        return empty("command", *attrs)
    }

    override fun mark(vararg attrs: IAttr): E {
        return empty("mark", *attrs)
    }

    override fun track(vararg attrs: IAttr): E {
        return empty("track", *attrs)
    }

    override fun wbr(vararg attrs: IAttr): E {
        return empty("wbr", *attrs)
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    override fun article(vararg children: Any): E {
        return elm("article", *children)
    }

    override fun aside(vararg children: Any): E {
        return elm("aside", *children)
    }

    override fun audio(vararg children: Any): E {
        return elm("audio", *children)
    }

    override fun bdi(vararg children: Any): E {
        return elm("bdi", *children)
    }

    override fun canvas(vararg children: Any): E {
        return elm("canvas", *children)
    }

    override fun datalist(vararg children: Any): E {
        return elm("datalist", *children)
    }

    override fun details(vararg children: Any): E {
        return elm("details", *children)
    }

    override fun figcaption(vararg children: Any): E {
        return elm("figcaption", *children)
    }

    override fun figure(vararg children: Any): E {
        return elm("figure", *children)
    }

    override fun footer(vararg children: Any): E {
        return elm("footer", *children)
    }

    override fun header(vararg children: Any): E {
        return elm("header", *children)
    }

    override fun hgroup(vararg children: Any): E {
        return elm("hgroup", *children)
    }

    override fun keygen(vararg children: Any): E {
        return elm("keygen", *children)
    }

    override fun meter(vararg children: Any): E {
        return elm("meter", *children)
    }

    override fun nav(vararg children: Any): E {
        return elm("nav", *children)
    }

    override fun output(vararg children: Any): E {
        return elm("output", *children)
    }

    override fun progress(vararg children: Any): E {
        return elm("progress", *children)
    }

    override fun rp(vararg children: Any): E {
        return elm("rp", *children)
    }

    override fun rt(vararg children: Any): E {
        return elm("rt", *children)
    }

    override fun ruby(vararg children: Any): E {
        return elm("ruby", *children)
    }

    override fun section(vararg children: Any): E {
        return elm("section", *children)
    }

    override fun summary(vararg children: Any): E {
        return elm("summary", *children)
    }

    override fun time(vararg children: Any): E {
        return elm("time", *children)
    }

    override fun video(vararg children: Any): E {
        return elm("video", *children)
    } //////////////////////////////////////////////////////////////////////

    companion object {

        fun readyfuncall(name: String?, vararg args: Any): StringBuilder {
            return readyfuncall(StringBuilder(), name, *args)
        }

        fun readyfuncall(ret: StringBuilder, name: String?, vararg args: Any): StringBuilder {
            ret.append("$(function() { ")
            funcall(ret, name, *args)
            ret.append(" });")
            return ret
        }

        fun funcall(name: String?, vararg args: Any): StringBuilder {
            return funcall(StringBuilder(), name, *args)
        }

        fun funcall(ret: StringBuilder, name: String?, vararg args: Any): StringBuilder {
            ret.append(name).append("(")
            for (i in 0 until args.size) {
                if (i > 0) {
                    ret.append(", ")
                }
                ret.append(tostring(args[i]))
            }
            ret.append(");")
            return ret
        }

        fun istyle1(key: Any, value: Any): String {
            return "$key:$value;"
        }

        fun tostring(a: Any): String? {
            return if (a is CharSequence) {
                quote(a.toString())
            } else if (a is Number || a is Boolean) {
                a.toString()
            } else {
                throw AssertionError("Expected only CharSequence, Number or Boolean types: " + a.javaClass)
            }
        }
    }
}
