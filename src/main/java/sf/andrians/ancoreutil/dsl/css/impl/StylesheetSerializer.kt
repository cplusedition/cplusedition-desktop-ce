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
package sf.andrians.ancoreutil.dsl.css.impl

import sf.andrians.ancoreutil.dsl.css.api.*
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.io.StringPrintStream
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintStream
import java.io.UnsupportedEncodingException

object StylesheetSerializer {
    fun serialize(s: IStylesheet?): String {
        val ret = StringPrintStream()
        serialize(ret, s)
        return ret.toString()
    }

    @Throws(UnsupportedEncodingException::class)
    fun serialize(s: IStylesheet?, charset: String?): ByteArray? {
        val ret = StringPrintStream(charset)
        serialize(ret, s)
        return ret.toByteArray()
    }

    @Throws(FileNotFoundException::class)
    fun serialize(outfile: File?, stylesheet: IStylesheet?) {
        serialize(outfile, "\t", stylesheet)
    }

    fun serialize(out: PrintStream, s: IStylesheet?) {
        serialize(out, "\t", s)
    }

    @Throws(FileNotFoundException::class)
    fun serialize(outfile: File?, tab: String, stylesheet: IStylesheet?) {
        var out: PrintStream? = null
        try {
            out = PrintStream(outfile)
            serialize(out, tab, stylesheet)
        } finally {
            FileUtil.close(out)
        }
    }

    fun serialize(out: PrintStream, tab: String, s: IStylesheet?) {
        s?.accept(SerialzeVisitor(tab), out)
    }

    internal class SerialzeVisitor(val tab: String) : ICSSVisitor<PrintStream> {
        private val indent = StringBuilder()
        override fun visit(node: IStylesheet, data: PrintStream) {
            for (child in node) {
                child.accept(this, data)
            }
        }

        override fun visit(node: INamespace, data: PrintStream) {
            data.print(indent)
            data.print("@namespace ")
            data.print(node.prefix)
            data.print(' ')
            data.print(quote(node.uri))
            data.println(';')
        }

        override fun visit(node: IImport, data: PrintStream) {
            data.print(indent)
            data.print("@import ")
            val uri = node.uri
            if (uri.startsWith("url(")) {
                data.print(uri)
            } else {
                data.print(quote(uri))
            }
            print(data, " ", node.mediums)
            data.println(';')
        }

        override fun visit(node: IMedia, data: PrintStream) {
            data.print(indent)
            data.print("@media ")
            print(data, " ", node.mediums)
            val rulesets = node.rulesets
            if (rulesets.isNotEmpty()) {
                data.println(" {")
                indent()
                for (ruleset in rulesets) {
                    ruleset.accept(this, data)
                }
                outdent()
                data.print(indent)
                data.println("}")
            }
        }

        override fun visit(node: IFontface, data: PrintStream) {
            data.print(indent)
            data.println("@font-face {")
            indent()
            for (decl in node.declarations) {
                decl.accept(this, data)
            }
            outdent()
            data.print(indent)
            data.println("}")
        }

        override fun visit(node: IPage, data: PrintStream) {
            data.print(indent)
            data.print("@page ")
            data.print(node.name)
            data.println(" {")
            indent()
            for (decl in node.declarations) {
                decl.accept(this, data)
            }
            outdent()
            data.print(indent)
            data.println("}")
        }

        override fun visit(node: IRuleset, data: PrintStream) {
            val sels = node.selectors
            val decls = node.declarations
            if (!sels.isEmpty() || !decls.isEmpty()) {
                data.print(indent)
                print(data, "", sels)
                data.println(" {")
                indent()
                for (decl in decls) {
                    decl.accept(this, data)
                }
                outdent()
                data.print(indent)
                data.println("}")
            }
        }

        override fun visit(node: ISelector?, data: PrintStream) {
            throw AssertionError("Should not reach here")
        }

        override fun visit(node: IDeclaration, data: PrintStream) {
            data.print(indent)
            data.print(node.property)
            data.print(": ")
            data.print(node.expr)
            data.println(";")
        }

        override fun visit(node: IRaw, data: PrintStream) {
            data.print(indent)
            data.println(node.toString())
        }

        private fun quote(s: String): String {
            val ret = StringBuilder()
            ret.append('"')
            var i = 0
            val len = s.length
            while (i < len) {
                val c = s[i]
                if (c == '"') {
                    ret.append("\\\"")
                    ++i
                    continue
                }
                ret.append(c)
                ++i
            }
            ret.append('"')
            return ret.toString()
        }

        private fun print(data: PrintStream, initial: String, a: Collection<*>) {
            var first = true
            for (m in a) {
                data.print(if (first) initial else ", ")
                data.print(m.toString())
                first = false
            }
        }

        fun indent() {
            indent.append(tab)
        }

        fun outdent() {
            indent.setLength(indent.length - tab.length)
        }
    }
}