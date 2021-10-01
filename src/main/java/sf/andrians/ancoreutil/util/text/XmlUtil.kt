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
package sf.andrians.ancoreutil.util.text

import org.w3c.dom.Element
import org.w3c.dom.Node
import sf.andrians.ancoreutil.XMLWriter
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import java.io.IOException
import java.util.*

class XmlUtil : IXmlUtil {
    companion object {
        private val ENTITIES: MutableMap<String, String> = TreeMap()
        fun escXml(vararg content: CharSequence): Array<String> {
            val len = content.size
            val ret = Array(len) {
                escXml1(content[it])
            }
            return ret
        }

        /**
         * Escape XML reserved characters in text without checking for xmlEntityRef.
         */
        fun escXml1(value: CharSequence?): String {
            if (value == null) {
                return ""
            }
            val ret = StringBuilder()
            escXml1(ret, value)
            return ret.toString()
        }

        private fun escXml1(ret: StringBuilder, value: CharSequence?): StringBuilder {
            if (value != null) {
                var i = 0
                val len = value.length
                while (i < len) {
                    val c = value[i]
                    when (c) {
                        '&' -> ret.append("&amp;")
                        '<' -> ret.append("&lt;")
                        '>' -> ret.append("&gt;")
                        '"' -> ret.append("&quot;")
                        '\'' -> ret.append("&#39;")
                        else -> ret.append(c)
                    }
                    i++
                }
            }
            return ret
        }

        private fun escXml1(ret: StringPrintWriter, value: CharSequence?): StringPrintWriter {
            if (value != null) {
                var i = 0
                val len = value.length
                while (i < len) {
                    val c = value[i]
                    when (c) {
                        '&' -> ret.append("&amp;")
                        '<' -> ret.append("&lt;")
                        '>' -> ret.append("&gt;")
                        '"' -> ret.append("&quot;")
                        '\'' -> ret.append("&#39;")
                        else -> ret.append(c)
                    }
                    i++
                }
            }
            return ret
        }

        /**
         * Escape XML reserved characters in text without checking for xmlEntityRef.
         */
        fun unescXml(value: CharSequence?): String? {
            return if (value == null) {
                null
            } else unescXml(StringBuilder(), value).toString()
        }

        fun unescXml1(value: CharSequence): String {
            return unescXml(StringBuilder(), value).toString()
        }

        private fun unescXml(ret: StringBuilder, value: CharSequence): StringBuilder {
            val start = intArrayOf(0)
            val end = value.length
            while (start[0] < end) {
                try {
                    if (unescentity(ret, value, start, end)) {
                        continue
                    }
                } catch (e: IOException) {
                    throw AssertionError("ASSERT: Should not happen")
                }
                ret.append(value[start[0]++])
            }
            return ret
        }

        fun unescXml(ret: StringPrintWriter, value: CharSequence): StringPrintWriter {
            val start = intArrayOf(0)
            val end = value.length
            while (start[0] < end) {
                try {
                    if (unescentity(ret, value, start, end)) {
                        continue
                    }
                } catch (e: IOException) {
                    throw AssertionError("ASSERT: Should not happen")
                }
                ret.append(value[start[0]++])
            }
            return ret
        }

        fun sprint(node: Node, indent: String, tab: String): String {
            val w = XMLWriter(StringPrintWriter(), indent, tab)
            sprint(w, node, indent, tab)
            return w.toString()
        }

        fun sprint(w: XMLWriter, node: Node, indent: String, tab: String) {
            when (node.nodeType) {
                Node.ELEMENT_NODE -> {
                    val elm = node as Element
                    val attrs = elm.attributes
                    val map: MutableMap<String, String?> = TreeMap()
                    for (i in 0 until attrs.length) {
                        val n = attrs.item(i)
                        val name = n.nodeName
                        map[name] = elm.getAttribute(name)
                    }
                    if (node.hasChildNodes()) {
                        w.start(node.getNodeName(), map)
                        w.lb1()
                        val children = node.getChildNodes()
                        var i = 0
                        while (i < children.length) {
                            sprint(w, children.item(i), indent + tab, tab)
                            ++i
                        }
                        w.lb1()
                        w.end()
                        w.lb1()
                    } else {
                        w.empty(node.getNodeName(), map)
                    }
                }
                Node.TEXT_NODE -> w.esc(node.nodeValue)
                Node.CDATA_SECTION_NODE -> w.cdata(escXml1(node.nodeValue))
                Node.COMMENT_NODE -> w.comment(escXml1(node.nodeValue))
                else -> w.comment("Unknown node type: " + node.nodeType)
            }
        }

        @Throws(IOException::class)
        private fun unescentity(
                ret: Appendable, value: CharSequence, start: IntArray, end: Int): Boolean {
            val s = start[0]
            if (s + 1 < end && value[s] == '&') {
                if (value[s + 1] == '#') {
                    if (s + 2 < end) {
                        val c = value[s + 2]
                        if (c == 'x') {
                            return uneschex(ret, value, start, end)
                        }
                        if (c >= '0' && c <= '9') {
                            return unescdec(ret, value, start, end)
                        }
                    }
                } else {
                    for ((key, value1) in ENTITIES) {
                        if (lookahead(value, s + 1, end, key)) {
                            ret.append(value1)
                            start[0] += key.length + 1
                            return true
                        }
                    }
                }
            }
            return false
        }

        @Throws(IOException::class)
        private fun unescdec(ret: Appendable, value: CharSequence, start: IntArray, end: Int): Boolean {
            var s = start[0] + 2
            var n = value[s] - '0'
            while (++s < end) {
                val c = value[s]
                if (c == ';') {
                    ++s
                    break
                }
                if (c >= '0' && c <= '9') {
                    n = n * 10 + (c - '0')
                    continue
                }
                return false
            }
            if (n < 0 || n > Character.MAX_CODE_POINT || !Character.isValidCodePoint(n)) {
                return false
            }
            start[0] = s
            ret.append(n.toChar())
            return true
        }

        @Throws(IOException::class)
        private fun uneschex(ret: Appendable, value: CharSequence, start: IntArray, end: Int): Boolean {
            var s = start[0] + 3
            if (s >= end) {
                return false
            }
            var n = 0
            while (s < end) {
                val c = value[s]
                if (c >= '0' && c <= '9') {
                    n = (n shl 4) + (c - '0')
                    ++s
                    continue
                } else if (c >= 'a' && c <= 'f') {
                    n = (n shl 4) + (10 + c.toInt() - 'a'.toInt())
                    ++s
                    continue
                } else if (c >= 'A' && c <= 'F') {
                    n = (n shl 4) + (10 + c.toInt() - 'A'.toInt())
                    ++s
                    continue
                } else if (c == ';') {
                    ++s
                    break
                }
                return false
                ++s
            }
            if (n < 0 || n > Character.MAX_CODE_POINT || !Character.isValidCodePoint(n)) {
                return false
            }
            start[0] = s
            ret.append(n.toChar())
            return true
        }

        private fun lookahead(value: CharSequence, start: Int, end: Int, key: String): Boolean {
            val len = key.length
            for (i in 0 until len) {
                if (start + i >= end || value[start + i] != key[i]) {
                    return false
                }
            }
            return true
        }

        init {
            ENTITIES["amp;"] = "&"
            ENTITIES["quot;"] = "\""
            ENTITIES["apos;"] = "'"
            ENTITIES["lt;"] = "<"
            ENTITIES["gt;"] = ">"
        }
    }

    class Attribute(private val name: String, private val value: String?) : IAttribute {
        override fun name(): String {
            return name
        }

        override fun value(): String? {
            return value
        }

    }

    override fun escAttr(source: CharSequence?): String {
        return escXml1(source)
    }

    override fun quoteAttr(value: CharSequence?): String {
        val buf = StringPrintWriter((value?.length ?: 1) * 2)
        buf.append('"')
        escXml1(buf, value)
        buf.append('"')
        return buf.toString()
    }

    override fun escText(source: CharSequence?, preservespace: Boolean): String {
        return escXml1(source)
    }
}
