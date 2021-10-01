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
package sf.andrians.ancoreutil.dsl.html.api

import java.io.File

interface IXHtmlBuilder<E : IElement?> {
    fun xml(): IPI
    fun xml(encoding: String?): IPI
    fun xml(version: String?, encoding: String?): IPI
    fun declaration(name: String, vararg contents: String): IDeclaration

    fun doctype(): IDeclaration
    fun doctype(doctype: String): IDeclaration

    fun empty(name: String, vararg attrs: IAttr): E
    fun area(vararg attrs: IAttr): E?
    fun base(vararg attrs: IAttr): E?
    fun basefont(vararg attrs: IAttr): E?
    fun br(vararg attrs: IAttr): E?
    fun col(vararg attrs: IAttr): E?
    fun doctype(vararg attrs: IAttr): E?
    fun embed(vararg attrs: IAttr): E?
    fun frame(vararg attrs: IAttr): E?

    fun img(vararg attrs: IAttr): E?
    fun input(vararg attrs: IAttr): E?
    fun isindex(vararg attrs: IAttr): E?
    fun link(vararg attrs: IAttr): E?
    fun meta(vararg attrs: IAttr): E?
    fun param(vararg attrs: IAttr): E?
    fun spacer(vararg attrs: IAttr): E?

    /////
    fun title(vararg contents: String): E?
    fun colgroup(width1: String, vararg widths: String): E?

    ///// Element with INode...children
    ///// Element with Object...chidlren
    fun elm(name: String, vararg children: Any): E
    fun top(vararg children: Any): ITop
    fun a(vararg children: Any): E?
    fun abbr(vararg children: Any): E?
    fun acronym(vararg children: Any): E?
    fun address(vararg children: Any): E?
    fun applet(vararg children: Any): E?
    fun b(vararg children: Any): E?
    fun bdo(vararg children: Any): E?
    fun big(vararg children: Any): E?
    fun blockquote(vararg children: Any): E?
    fun body(vararg children: Any): E?
    fun button(vararg children: Any): E?
    fun caption(vararg children: Any): E?
    fun center(vararg children: Any): E?
    fun cite(vararg children: Any): E?
    fun code(vararg children: Any): E?
    fun colgroup(vararg children: Any): E?
    fun dd(vararg children: Any): E?
    fun del(vararg children: Any): E?
    fun dfn(vararg children: Any): E?
    fun dir(vararg children: Any): E?
    fun div(vararg children: Any): E?
    fun dl(vararg children: Any): E?
    fun dt(vararg children: Any): E?
    fun em(vararg children: Any): E?
    fun fieldset(vararg children: Any): E?
    fun font(vararg children: Any): E?
    fun form(vararg children: Any): E?
    fun frameset(vararg children: Any): E?
    fun h1(vararg children: Any): E?
    fun h2(vararg children: Any): E?
    fun h3(vararg children: Any): E?
    fun h4(vararg children: Any): E?
    fun h5(vararg children: Any): E?
    fun h6(vararg children: Any): E?
    fun head(vararg children: Any): E?
    fun hr(vararg children: Any): E?
    fun html(vararg children: Any): E?
    fun i(vararg children: Any): E?
    fun iframe(vararg children: Any): E?
    fun ilayer(vararg children: Any): E?
    fun ins(vararg children: Any): E?
    fun kbd(vararg children: Any): E?
    fun label(vararg children: Any): E?
    fun layer(vararg children: Any): E?
    fun legend(vararg children: Any): E?
    fun li(vararg children: Any): E?
    fun link(vararg children: Any): E?
    fun map(vararg children: Any): E?
    fun menu(vararg children: Any): E?
    fun meta(vararg children: Any): E?
    fun multicol(vararg children: Any): E?
    fun nobr(vararg children: Any): E?
    fun noframes(vararg children: Any): E?
    fun nolayer(vararg children: Any): E?
    fun noscript(vararg children: Any): E?
    fun `object`(vararg children: Any): E?
    fun ol(vararg children: Any): E?
    fun optgroup(vararg children: Any): E?
    fun option(vararg children: Any): E?
    fun p(vararg children: Any): E?
    fun plaintext(vararg children: Any): E?
    fun pre(vararg children: Any): E?
    fun q(vararg children: Any): E?
    fun s(vararg children: Any): E?
    fun samp(vararg children: Any): E?
    fun script(vararg children: Any): E?
    fun sdfield(vararg children: Any): E?
    fun select(vararg children: Any): E?
    fun small(vararg children: Any): E?
    fun span(vararg children: Any): E?
    fun strike(vararg children: Any): E?
    fun strong(vararg children: Any): E?
    fun style(vararg children: Any): E?
    fun sub(vararg children: Any): E?
    fun sup(vararg children: Any): E?
    fun table(vararg children: Any): E?
    fun tbody(vararg children: Any): E?
    fun td(vararg children: Any): E?
    fun term(vararg children: Any): E?
    fun textarea(vararg children: Any): E?
    fun tfoot(vararg children: Any): E?
    fun th(vararg children: Any): E?
    fun thead(vararg children: Any): E?
    fun tr(vararg children: Any): E?
    fun tt(vararg children: Any): E?
    fun u(vararg children: Any): E?
    fun ul(vararg children: Any): E?
    fun `var`(vararg children: Any): E?
    fun xmp(vararg children: Any): E?
    /**
     * Emit content as is, ie. even if it contains tags.
     */
    fun raw(vararg content: String): IText
    fun rawfile(file: File): IText

    /**
     * Emit content as is, same as raw() but with intention that content don't contain tags.
     */
    fun txt(vararg content: String): IText

    /**
     * Shorthand for txt().
     */
    fun t(vararg content: String): IText

    /**
     * Escape content and emit as literal text.
     */
    fun esc(vararg content: String): IText
    fun cdata(vararg content: String): ICData
    fun comment(vararg content: String): IComment
    fun pi(target: String, vararg content: String): IPI

    fun attibutes(): IAttributes
    fun atts(name: Any, value: Boolean): IAttributes
    fun atts(name: Any, value: Int): IAttributes
    fun atts(name: Any, value: Long): IAttributes
    fun atts(name: Any, value: Float): IAttributes
    fun atts(name: Any, value: Double): IAttributes
    fun atts(name: Any, value: Any): IAttributes
    fun attributes(attr: IAttribute): IAttributes
    fun attributes(vararg attrs: IAttribute): IAttributes
    fun atts(vararg namevalues: Any): IAttributes
    fun atts(linebreaks: Boolean): IAttributes
    fun lb(vararg namevalues: Any): IAttributes
    fun lb(vararg attrs: IAttribute): IAttributes
    fun attr(name: Any, value: Boolean): IAttribute
    fun attr(name: Any, value: Int): IAttribute
    fun attr(name: Any, value: Long): IAttribute
    fun attr(name: Any, value: Float): IAttribute
    fun attr(name: Any, value: Double): IAttribute
    fun attr(name: Any, value: Any): IAttribute
    fun attr(name: Any, vararg values: Any): IAttribute

    fun id(value: Any): IAttribute
    fun type(value: Any): IAttribute
    fun name(value: Any): IAttribute
    fun value(value: Any): IAttribute
    fun content(value: Any): IAttribute
    fun href(value: Any): IAttribute
    fun rel(value: Any): IAttribute
    fun src(value: Any): IAttribute
    fun colspan(value: Any): IAttribute
    fun css(value: Any): IAttribute
    fun css(vararg values: Any): IAttribute

    /**
     * Inline style specification.
     */
    fun istyle(style: Any): IAttribute
    fun istyle(vararg styles: Any): IAttribute

    fun onload(script: String): IAttribute
    fun onunload(script: String): IAttribute
    fun onclick(script: String): IAttribute
    fun ondblclick(script: String): IAttribute
    fun onmousedown(script: String): IAttribute
    fun onmouseup(script: String): IAttribute
    fun onmouseover(script: String): IAttribute
    fun onmousemove(script: String): IAttribute
    fun onmouseout(script: String): IAttribute
    fun onfocus(script: String): IAttribute
    fun onblur(script: String): IAttribute
    fun onkeypress(script: String): IAttribute
    fun onkeydown(script: String): IAttribute
    fun onkeyup(script: String): IAttribute
    fun onsubmit(script: String): IAttribute
    fun onreset(script: String): IAttribute
    fun onselect(script: String): IAttribute

    fun contenttype(type: Any): E?
    fun stylesheet(href: Any): E?
    fun javascript(src: Any): E?
    fun stylesheet(): E?
    fun javascript(): E?
    fun vbox(vararg children: IElement): E?
    fun hbox(vararg children: IElement): E?

    /**
     * Force a line break at the output.
     */
    fun lb(): IText
}
