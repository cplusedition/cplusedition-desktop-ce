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

import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.ancoreutil.util.text.IAttribute
import sf.andrians.ancoreutil.util.text.TextUtil
import sf.andrians.ancoreutil.util.text.XmlUtil
import java.io.PrintWriter
import java.util.*

/**
 * Note: Be sure to call close() to flush the line buffer.
 *
 * @param indent Initial indent.
 * @param tab
 */
open class XMLWriter  constructor(
        protected var writer: PrintWriter = StringPrintWriter(),
        indent: String = IXMLWriter.INDENT,
        tab: String? = null) : IXMLWriter {

    constructor(): this(StringPrintWriter(), IXMLWriter.INDENT, null)

    ////////////////////////////////////////////////////////////////////////

    open class Builder internal constructor() {
        protected var writer: PrintWriter? = null
        protected var indent = ""
        protected var tab: String? = null

        fun indent(indent: String): Builder {
            this.indent = indent
            return this
        }

        fun tab(tab: String?): Builder {
            this.tab = tab
            return this
        }

        open fun build(): XMLWriter {
            return XMLWriter((writer ?: StringPrintWriter()), indent, tab)
        }
    }

    protected var initialIndent: String = ""
    protected var tab: String? = null

    ////////////////////////////////////////////////////////////////////////

    protected val lineBuffer = StringBuilder()
    protected var startLevel = 0
    protected val stack = Stack<String>()
    override fun toString(): String {
        return writer.toString()
    }

    ////////////////////////////////////////////////////////////////////////

    override fun xmlHeader(): IXMLWriter {
        raw("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").lb()
        return this
    }

    override fun start(tag: String): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        stack.push(tag)
        return this
    }

    override fun startAll(vararg tags: String): IXMLWriter {
        for (tag in tags) {
            lineBuffer.append("<")
            lineBuffer.append(tag)
            lineBuffer.append(">")
            stack.push(tag)
        }
        return this
    }

    override fun start(tag: String, vararg attrs: IAttribute): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append(">")
        stack.push(tag)
        return this
    }

    override fun start(tag: String, vararg attrs: String): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append(">")
        stack.push(tag)
        return this
    }

    override fun start(tag: String, attrs: Map<String, String?>): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(attrs)
        lineBuffer.append(">")
        stack.push(tag)
        return this
    }

    override fun empty(tag: String): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        lineBuffer.append("/>")
        return this
    }

    override fun empty(tag: String, vararg attrs: IAttribute): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append("/>")
        return this
    }

    override fun empty(tag: String, vararg attrs: String): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append("/>")
        return this
    }

    override fun empty(tag: String, attrs: Map<String, String?>): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(attrs)
        lineBuffer.append("/>")
        return this
    }

    override fun raw(content: String): IXMLWriter {
        lineBuffer.append(content)
        return this
    }

    override fun raw(vararg content: String): IXMLWriter {
        var i = 0
        val len = content.size
        while (i < len) {
            if (i > 0) {
                lb()
            }
            lineBuffer.append(content[i])
            ++i
        }
        return this
    }

    override fun txt(content: String): IXMLWriter {
        return raw(content)
    }

    override fun txt(vararg content: String): IXMLWriter {
        return raw(*content)
    }

    override fun esc(content: String): IXMLWriter {
        return raw(*escXml(content))
    }

    override fun esc(vararg content: String): IXMLWriter {
        return raw(*escXml(*content))
    }

    override fun format(format: String, vararg args: Any): IXMLWriter {
        lineBuffer.append(TextUtil.format(format, *args))
        return this
    }

    override fun comment(comment: String): IXMLWriter {
        lineBuffer.append("<!-- ")
        lineBuffer.append(comment)
        lineBuffer.append(" -->")
        return this
    }

    override fun comment(vararg comment: String): IXMLWriter {
        lineBuffer.append("<!-- ")
        var i = 0
        val len = comment.size
        while (i < len) {
            if (i > 0) {
                lb()
            }
            lineBuffer.append(comment[i])
            ++i
        }
        lineBuffer.append(" -->")
        return this
    }

    override fun cdata(cdata: String): IXMLWriter {
        lineBuffer.append("<![CDATA[")
        lineBuffer.append(cdata)
        lineBuffer.append("]]>")
        return this
    }

    override fun cdata(vararg cdata: String): IXMLWriter {
        lineBuffer.append("<![CDATA[")
        var i = 0
        val len = cdata.size
        while (i < len) {
            if (i > 0) {
                lb()
            }
            lineBuffer.append(cdata[i])
            ++i
        }
        lineBuffer.append("]]>")
        return this
    }

    override fun end(): IXMLWriter {
        lineBuffer.append("</")
        lineBuffer.append(stack.pop())
        lineBuffer.append(">")
        return this
    }

    override fun end(vararg expects: String): IXMLWriter {
        for (tag in expects) {
            end1(tag)
        }
        return this
    }

    override fun end(level: Int): IXMLWriter {
        var levels = level
        while (--levels >= 0) {
            end()
        }
        return this
    }

    override fun endTill(level: Int): IXMLWriter {
        while (stack.size > level) {
            end()
        }
        return this
    }

    override fun endAll(): IXMLWriter {
        while (stack.size > 0) {
            end()
        }
        return this
    }

    override fun element(tag: String, content: String?): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        if (content != null) {
            lineBuffer.append(content)
        }
        lineBuffer.append("</")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        return this
    }

    override fun element(tag: String, content: String?, vararg attrs: IAttribute): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append(">")
        if (content != null) {
            lineBuffer.append(content)
        }
        lineBuffer.append("</")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        return this
    }

    override fun element(tag: String, content: String?, vararg attrs: String): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(*attrs)
        lineBuffer.append(">")
        if (content != null) {
            lineBuffer.append(content)
        }
        lineBuffer.append("</")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        return this
    }

    override fun element(tag: String, content: String?, attrs: Map<String, String?>): IXMLWriter {
        lineBuffer.append("<")
        lineBuffer.append(tag)
        attributes(attrs)
        lineBuffer.append(">")
        if (content != null) {
            lineBuffer.append(content)
        }
        lineBuffer.append("</")
        lineBuffer.append(tag)
        lineBuffer.append(">")
        return this
    }

    ////////////////////////////////////////////////////////////////////////

    override fun a(name: String, value: String?): IAttribute {
        return XmlUtil.Attribute(name, value)
    }

    ////////////////////////////////////////////////////////////////////////
    override fun lb(): IXMLWriter {
        printIndent()
        writer.println(lineBuffer)
        lineBuffer.setLength(0)
        startLevel = level()
        return this
    }

    override fun lb1(): IXMLWriter {
        if (lineBuffer.isNotEmpty()) {
            lb()
        }
        return this
    }

    override fun formatted(vararg content: String): IXMLWriter {
        if (lineBuffer.isNotEmpty()) {
            lb()
        }
        var i = 0
        val len = content.size
        while (i < len) {
            writer.print(content[i])
            ++i
        }
        return this
    }

    override fun flush(): IXMLWriter {
        writer.flush()
        return this
    }

    ////////////////////////////////////////////////////////////////////////
    override fun level(): Int {
        return stack.size
    }

    override fun close() {
        if (lineBuffer.length > 0) {
            printIndent()
            writer.print(lineBuffer)
        }
        writer.close()
    }

    protected fun printIndent() {
        writer.print(initialIndent)
        if (tab != null) {
            var level = level()
            if (level > startLevel) {
                level = startLevel
            }
            while (--level >= 0) {
                writer.print(tab)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    protected fun end1(expected: String) {
        val tag = stack.pop()
        if (tag != expected) {
            throw RuntimeException("ERROR: Mismatched end tag: expected=$expected, actual=$tag")
        }
        lineBuffer.append("</")
        lineBuffer.append(tag)
        lineBuffer.append(">")
    }

    protected fun attributes(vararg attrs: IAttribute) {
        for (attr in attrs) {
            attribute(attr.name(), attr.value())
        }
    }

    protected fun attributes(vararg attrs: String) {
        var i = 0
        while (i < attrs.size) {
            attribute(attrs[i], attrs[i + 1])
            i += 2
        }
    }

    protected fun attributes(attrs: Map<String, String?>) {
        for ((key, value) in attrs) {
            attribute(key, value)
        }
    }

    protected fun attribute(name: String?, value: String?) {
        if (name != null) {
            lineBuffer.append(" ")
            lineBuffer.append(name)
            lineBuffer.append("=\"")
            lineBuffer.append(value ?: "")
            lineBuffer.append("\"")
        }
    }

    ////////////////////////////////////////////////////////////////////////

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        ////////////////////////////////////////////////////////////////////////

        fun escXml(vararg a: CharSequence): Array<String> {
            return Array(a.size) {
                XmlUtil.escXml1(a[it])
            }
        }
    }

    init {
        initialIndent = indent
        this.tab = tab
    }
}
