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
package sf.andrians.ancoreutil

import sf.andrians.ancoreutil.dsl.html.api.TAG
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.ancoreutil.util.text.IAttribute
import java.io.PrintWriter

/**
 * Note: Be sure to call close() to flush the line buffer.
 */
class HTML5Writer(w: PrintWriter, indent: String, tab: String?) : XMLWriter(w, indent, tab), IHTML5Writer {

    constructor() : this(StringPrintWriter(), IXMLWriter.INDENT, null)

    ////////////////////////////////////////////////////////////////////////

    class Builder internal constructor() : XMLWriter.Builder() {
        override fun build(): HTML5Writer {
            return HTML5Writer((writer ?: StringPrintWriter()), indent, tab)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    override fun id(value: String): IAttribute? {
        return a("id", value)
    }

    override fun css(value: String): IAttribute? {
        return a("class", value)
    }

    override fun type(value: String): IAttribute? {
        return a("type", value)
    }

    override fun name(value: String): IAttribute? {
        return a("name", value)
    }

    override fun href(value: String): IAttribute? {
        return a("href", value)
    }

    override fun src(value: String): IAttribute? {
        return a("src", value)
    }

    override fun id(value: Any): IAttribute? {
        return a("id", value.toString())
    }

    override fun css(value: Any): IAttribute? {
        return a("class", value.toString())
    }

    override fun type(value: Any): IAttribute? {
        return a("type", value.toString())
    }

    ////////////////////////////////////////////////////////////////////////

    override fun doctype(): IHTML5Writer {
        raw(IHTML5Writer.DOCTYPE_HTML5).lb()
        return this
    }

    override fun contentType(charset: String): IHTML5Writer {
        empty("meta", "http-equiv", "Content-Type", "content", "text/html", "charset", charset).lb()
        return this
    }

    override fun title(text: String): IHTML5Writer {
        element("title", text).lb()
        return this
    }

    override fun stylesheet(href: String): IHTML5Writer {
        empty("link", "rel", "stylesheet", "type", "text/css", "href", href).lb()
        return this
    }

    override fun javascript(href: String): IHTML5Writer {
        empty("script", "type", "text/javascript", "src", href).lb()
        return this
    }

    override fun script(vararg content: String): IHTML5Writer {
        start("script", "type", "text/javascript").lb()
        raw(*content).lb()
        end().lb()
        return this
    }

    override fun style(vararg content: String): IHTML5Writer {
        start("style", "type", "text/css").lb()
        raw(*content).lb()
        end().lb()
        return this
    }

    ////////////////////////////////////////////////////////////////////////

    override fun xmlHeader(): IHTML5Writer {
        super.xmlHeader()
        return this
    }

    override fun start(tag: String): IHTML5Writer {
        super.start(tag)
        return this
    }

    override fun startAll(vararg tags: String): IHTML5Writer {
        super.startAll(*tags)
        return this
    }

    override fun start(tag: String, vararg attrs: IAttribute): IHTML5Writer {
        super.start(tag, *attrs)
        return this
    }

    override fun start(tag: String, vararg attrs: String): IHTML5Writer {
        super.start(tag, *attrs)
        return this
    }

    override fun start(tag: String, attrs: Map<String, String?>): IHTML5Writer {
        super.start(tag, attrs)
        return this
    }

    override fun empty(tag: String): IHTML5Writer {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        endemptytag(tag)
        return this
    }

    override fun empty(tag: String, vararg attrs: IAttribute): IHTML5Writer {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        endemptytag(tag)
        return this
    }

    override fun empty(tag: String, vararg attrs: String): IHTML5Writer {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        endemptytag(tag)
        return this
    }

    override fun empty(tag: String, attrs: Map<String, String?>): IHTML5Writer {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(attrs)
        endemptytag(tag)
        return this
    }

    override fun raw(content: String): IHTML5Writer {
        super.raw(content)
        return this
    }

    override fun raw(vararg content: String): IHTML5Writer {
        super.raw(*content)
        return this
    }

    override fun txt(content: String): IHTML5Writer {
        super.txt(content)
        return this
    }

    override fun txt(vararg content: String): IHTML5Writer {
        super.txt(*content)
        return this
    }

    override fun esc(content: String): IHTML5Writer {
        super.esc(content)
        return this
    }

    override fun esc(vararg content: String): IHTML5Writer {
        super.esc(*content)
        return this
    }

    override fun format(format: String, vararg args: Any): IHTML5Writer {
        super.format(format, *args)
        return this
    }

    override fun comment(comment: String): IHTML5Writer {
        super.comment(comment)
        return this
    }

    override fun comment(vararg comment: String): IHTML5Writer {
        super.comment(*comment)
        return this
    }

    override fun cdata(cdata: String): IHTML5Writer {
        super.cdata(cdata)
        return this
    }

    override fun cdata(vararg cdata: String): IHTML5Writer {
        super.cdata(*cdata)
        return this
    }

    override fun end(): IHTML5Writer {
        super.end()
        return this
    }

    override fun end(vararg expects: String): IHTML5Writer {
        super.end(*expects)
        return this
    }

    override fun end(level: Int): IHTML5Writer {
        super.end(level)
        return this
    }

    override fun endTill(level: Int): IHTML5Writer {
        super.endTill(level)
        return this
    }

    override fun endAll(): IHTML5Writer {
        super.endAll()
        return this
    }

    override fun element(tag: String, content: String?): IHTML5Writer {
        super.element(tag, content)
        return this
    }

    override fun element(tag: String, content: String?, vararg attrs: IAttribute): IHTML5Writer {
        super.element(tag, content, *attrs)
        return this
    }

    override fun element(tag: String, content: String?, vararg attrs: String): IHTML5Writer {
        super.element(tag, content, *attrs)
        return this
    }

    override fun element(tag: String, content: String?, attrs: Map<String, String?>): IHTML5Writer {
        super.element(tag, content, attrs)
        return this
    }

    ////////////////////////////////////////////////////////////////////////

    override fun formatted(vararg content: String): IHTML5Writer {
        super.formatted(*content)
        return this
    }

    override fun lb(): IHTML5Writer {
        super.lb()
        return this
    }

    override fun flush(): IHTML5Writer {
        super.flush()
        return this
    }

    ////////////////////////////////////////////////////////////////////////

    private fun endemptytag(tagname: String) {
        val tag = TAG.get(tagname)
        if (tag != null && tag.requireEndTag()) {
            lineBuffer.append("></").append(tag).append(">")
        } else {
            lineBuffer.append(">")
        }
    }

    ////////////////////////////////////////////////////////////////////////

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    ////////////////////////////////////////////////////////////////////////
}