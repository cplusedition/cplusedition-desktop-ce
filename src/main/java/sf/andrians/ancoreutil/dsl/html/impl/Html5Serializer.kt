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
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.ancoreutil.util.text.IXmlUtil
import sf.andrians.ancoreutil.util.text.TextUtil.join
import sf.andrians.ancoreutil.util.text.TextUtil.lineSeparator
import sf.andrians.ancoreutil.util.text.XmlUtil
import java.io.PrintWriter
import java.io.Writer
import java.util.*

/**
 * Serialize the DOM document as XHtml using explicit line breaks.
 */
open class Html5Serializer<T : Writer> : INodeVisitor<T> {

    protected var indent: String = ""
    protected var tab: String = ""

    private var xmlUtil: IXmlUtil = XmlUtil()
    private var noXmlEndTag = true
    private var line = StringBuilder()
    private var startLevel = 0
    private var newline_ = true
    var newline: Boolean
        get() = newline_
        set(value) {
            newline_ = value
        }

    private var preformatted = false
    var stack = Stack<String>()

    //////////////////////////////////////////////////////////////////////

    constructor() {}

    constructor(tab: String) {
        this.tab = tab
    }

    //////////////////////////////////////////////////////////////////////

    fun indent(indent: String): Html5Serializer<T> {
        this.indent = indent
        return this
    }

    fun tab(tab: String): Html5Serializer<T> {
        this.tab = tab
        return this
    }

    fun noXmlEndTag(noxmlendtag: Boolean): Html5Serializer<T> {
        noXmlEndTag = noxmlendtag
        return this
    }

    //////////////////////////////////////////////////////////////////////

    override fun visit(node: ITop, data: T) {
        for (c in node.children()) {
            c.accept(this, data)
        }
        if (!newline || line.isNotEmpty()) {
            lb(data)
        }
    }

    override fun visit(node: IWicketComponent, data: T) {
        visitElement(node, data)
    }

    override fun visit(node: IWicketElement, data: T) {
        visitElement(node, data)
    }

    override fun visit(node: IElement, data: T) {
        visitElement(node, data)
    }

    override fun visit(node: IText, data: T) {
        content(node.content)
    }

    override fun visit(node: ICData, data: T) {
        line.append(node.toString())
    }

    override fun visit(node: IComment, data: T) {
        line.append(node.toString())
    }

    override fun visit(node: ILine, data: T) {
        line.append(node.toString())
        lb(data)
    }

    override fun visit(node: IPI, data: T) {
        line.append(node.toString())
        lb(data)
    }

    override fun visit(node: IDeclaration, data: T) {
        line.append(node.toString())
        lb(data)
    }

    override fun visit(node: IChild?, data: T) {
        throw AssertionError("Should not reach here")
    }

    //////////////////////////////////////////////////////////////////////

    protected fun attributes(w: T, node: IElement) {
        val attrs = node.attributes() ?: return
        for (a in attrs) {
            if (Attribute.LB === a) {
                lb(w)
            } else {
                val value = a.avalue()
                line.append(" ")
                line.append(a.aname())
                if (value != null) {
                    line.append("=")
                    line.append('"')
                    line.append(xmlUtil.escAttr(value))
                    line.append('"')
                }
            }
        }
    }

    protected fun visitElement(node: IElement, data: T) {
        val name = node.tag()
        val tag: TAG? = TAG.get(name)
        if (tag != null && tag.isBlockFormat || node is IWicketElement) {
            if (!preformatted) {
                lb(data)
            }
            inlineElement(node, data, name, tag)
            if (!preformatted) {
                lb(data)
            }
        } else {
            inlineElement(node, data, name, tag)
        }
    }

    private fun inlineElement(node: IElement, data: T, name: String?, tag: TAG?) {
        line.append("<").append(name)
        attributes(data, node)
        if (node.childCount() > 0) {
            line.append(">")
            stack.push(name)
            val saved = preformatted
            val now = tag != null && tag.isPreFormatted
            if (!saved && now) {
                flush(data)
            }
            preformatted = saved or now
            for (c in node.children()) {
                c.accept(this, data)
            }
            stack.pop()
            preformatted = saved
            line.append("</")
            line.append(name)
            line.append(">")
        } else if (tag != null && tag.isEmpty) {
            line.append(">")
        } else if (noXmlEndTag) {
            line.append("></")
            line.append(name)
            line.append(">")
        } else {
            line.append(" />")
        }
    }

    protected fun content(content: Array<out String>) {
        val len = content.size
        if (len == 1) {
            line.append(content[0])
        } else if (len > 0) {
            line.append(join(getindent(), *content))
        }
    }

    protected fun indent() {
        if (tab.isNotEmpty()) {
            indent += tab
        }
    }

    protected fun unindent() {
        val tablen = tab.length
        if (tablen > 0) {
            val len = indent.length - tablen
            if (len >= 0) {
                indent = indent.substring(0, indent.length - tablen)
            }
        }
    }

    protected val lineBuffer: CharSequence get() = line

    protected fun level(): Int {
        return stack.size
    }

    protected fun lb(data: T) {
        if (newline && line.isEmpty()) {
            return
        }
        if (newline && !preformatted) {
            printIndent(data)
        }
        if (line.isNotEmpty()) {
            data.appendln(line)
        }
        newline = true
        line.setLength(0)
        startLevel = level()
    }

    protected fun newline(data: T) {
        data.appendln()
        newline = true
    }

    protected fun flush(data: T) {
        if (line.isEmpty()) {
            return
        }
        if (newline && !preformatted) {
            printIndent(data)
        }
        data.append(line)
        newline = false
        line.setLength(0)
        startLevel = level()
    }

    protected fun printIndent(data: T) {
        data.append(indent)
        if (tab.isNotEmpty()) {
            var level = level()
            if (level > startLevel) {
                level = startLevel
            }
            while (--level >= 0) {
                data.append(tab)
            }
        }
    }

    private fun getindent(): String {
        val b = StringBuilder(lineSeparator)
        b.append(indent)
        if (tab.isNotEmpty()) {
            var level = level()
            if (level > startLevel) {
                level = startLevel
            }
            while (--level >= 0) {
                b.append(tab)
            }
        }
        return b.toString()
    }

    //////////////////////////////////////////////////////////////////////

    companion object {

        fun serialize(indent: String = "", tab: String = "", e: IElement): String {
            return serialize(indent, tab, true, e)
        }

        fun serialize(indent: String = "", tab: String = "", noxmlendtag: Boolean, e: IElement): String {
            val w = StringPrintWriter()
            e.accept(Html5Serializer<StringPrintWriter>(tab).indent(indent).noXmlEndTag(noxmlendtag), w)
            return w.toString()
        }
    }
}