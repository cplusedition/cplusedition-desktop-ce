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

import com.cplusedition.bot.core.LS
import com.cplusedition.bot.core.StringPrintWriter
import com.cplusedition.bot.core.XMLUt
import com.cplusedition.bot.dsl.html.api.*
import java.io.PrintWriter
import java.util.*

/** Serialize the DOM document as Html/Html5 using explicit line breaks.  */
open class HtmlSerializer<T : PrintWriter> : INodeVisitor<T> {

    protected var indent: String = ""
    protected var tab: String = ""
    private var noXmlEndTag = true
    private var line = StringBuilder()
    private var startLevel = 0
    private var preformatted = false
    private var stack = Stack<String>()
    private var newline_ = true
    var newline: Boolean
        get() = newline_
        set(value) {
            newline_ = value
        }

    //////////////////////////////////////////////////////////////////////

    constructor() {}

    constructor(tab: String) {
        this.tab = tab
    }

    //////////////////////////////////////////////////////////////////////

    fun indent(indent: String): HtmlSerializer<T> {
        this.indent = indent
        return this
    }

    fun tab(tab: String): HtmlSerializer<T> {
        this.tab = tab
        return this
    }

    fun noXmlEndTag(noxmlendtag: Boolean): HtmlSerializer<T> {
        noXmlEndTag = noxmlendtag
        return this
    }

    //////////////////////////////////////////////////////////////////////
    override fun visit(node: IFragment, data: T) {
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
        val attrs = node.attributes()
        for (a in attrs) {
            if (Attribute.LB === a) {
                lb(w)
            } else {
                line.append(" ")
                line.append(a.aname())
                val value = a.avalue()
                if (value != null) {
                    line.append("=")
                    line.append('"')
                    line.append(XMLUt.esc(value))
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
            line.append(content.joinToString(getindent()))
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
            data.println(line)
        }
        newline = true
        line.setLength(0)
        startLevel = level()
    }

    protected fun newline(data: T) {
        data.println()
        newline = true
    }

    protected fun flush(data: T) {
        if (line.isEmpty()) {
            return
        }
        if (newline && !preformatted) {
            printIndent(data)
        }
        data.print(line)
        newline = false
        line.setLength(0)
        startLevel = level()
    }

    protected fun printIndent(data: T) {
        data.print(indent)
        if (tab.isNotEmpty()) {
            var level = level()
            if (level > startLevel) {
                level = startLevel
            }
            while (--level >= 0) {
                data.print(tab)
            }
        }
    }

    private fun getindent(): String {
        val b = StringBuilder(LS)
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
            serialize(w, indent, tab, noxmlendtag, e)
            return w.toString()
        }

        fun serialize(w: PrintWriter, indent: String = "", tab: String = "", noxmlendtag: Boolean, e: IElement) {
            e.accept(HtmlSerializer<PrintWriter>(tab).indent(indent).noXmlEndTag(noxmlendtag), w)
        }
    }
}
