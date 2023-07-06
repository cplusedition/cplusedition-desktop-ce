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
package sf.andrians.cplusedition.support

import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.ICharRange
import com.cplusedition.bot.core.ICharSlice
import com.cplusedition.bot.core.MyCharArrayWriter
import com.cplusedition.bot.core.StringPrintStream
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.XMLUt
import sf.andrians.cplusedition.support.AnUri.Companion.UriBuilder
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.llk.grammar.css.CSSUtil
import sf.llk.grammar.css.LLKCSSParserVisitorAdapter
import sf.llk.grammar.css.parser.ASTstyleSheet
import sf.llk.grammar.css.parser.ASTterm
import sf.llk.grammar.css.parser.CSSLexer
import sf.llk.grammar.css.parser.CSSParser
import sf.llk.grammar.css.parser.ILLKCSSLexer
import sf.llk.grammar.html.parser.ASTattribute
import sf.llk.grammar.html.parser.ASTdocument
import sf.llk.grammar.html.parser.ASTstartTag
import sf.llk.grammar.html.parser.HtmlLexer
import sf.llk.grammar.html.parser.HtmlSaxAdapter
import sf.llk.share.support.DefaultTokenHandler
import sf.llk.share.support.ILLKToken
import sf.llk.share.support.ISourceLocator
import sf.llk.share.support.LLKLexerInput
import sf.llk.share.support.SimpleLLKMain
import java.nio.charset.Charset
import java.util.*

val XrefUt = XrefUtil()

open class XrefUtil {

    private val urlPat = Regex("^\\s*url\\((.*?)\\)\\s*$")

    fun canXref(lcsuffix: String): Boolean {
        return lcsuffix == Suffix.HTML || lcsuffix == Suffix.CSS
    }

    fun filepathOfUrl(url: String, baseuri: BaseUri): String? {
        if (url.startsWith("data:") || url.startsWith("blob:"))
            return null
        var path = url.indexOf("#").let {
            if (it >= 0) url.substring(0, it) else url
        }
        path.indexOf("?").let {
            if (it >= 0) path = path.substring(0, it)
        }
        return baseuri.resolveUri(path).toAbsolute().path
    }

    fun changeUri(file: IFileInfo, callback: Fun30<ASTattribute, ILLKToken, String>): Boolean {
        val tok = XrefUt.parseHtmlXrefs(file.asChars(), file.apath, callback)?.firstToken
            ?: return false
        file.content().write(render(tok).reader())
        return true
    }

    /// Source file that hold the uri has moved from obaseuri to to new baseuri,
    /// resolve any relative uris against the new baseuri.
    fun rebaseUri(tobase: BaseUri, frombase: BaseUri, url: String, incopyset: Fun11<String, Boolean>): Pair<String, AnUri?> {
        if (url.startsWith("data:") || url.startsWith("blob:"))
            return Pair(url, null)
        val fromuri = frombase.resolveUri(url)
        val au = fromuri.toAbsolute()
        val frompath = au.path
        return if (incopyset(frompath)) {
            if (fromuri.isRelative) {
                val tou = tobase.resolveUri(url).toAbsolute()
                val topath = tou.path
                Pair(topath, null)
            } else {
                val tou = tobase.resolveUri(fromuri.toRelative()).toAbsolute()
                val topath = tou.path
                Pair(topath, tou)
            }
        } else {
            if (!fromuri.isRelative)
                return Pair(fromuri.path, null)
            val tou = tobase.relativize(au)
            val topath = tou.toAbsolute().path
            if (frompath != topath) {
                
                return Pair(frompath, null)
            }
            Pair(frompath, tou)
        }
    }

    /// File at srcpath has moved to dstpath.
    /// Fixup any url that reference srcpath to reference dstpath.
    /// @dstpath A canonical clean absolute path to dst file.
    /// @srcpath A canonical clean absolute path to src file.
    fun moveUri(
        dstpath: String,
        srcpath: String,
        url: String,
        tobase: BaseUri,
    ): Pair<String, AnUri?> {
        if (url.startsWith("data:") || url.startsWith("blob:"))
            return Pair(url, null)
        val touri = tobase.resolveUri(url)
        val apath = Basepath.cleanPath(touri.toAbsolute().path)
        if (apath != srcpath)
            return Pair(apath, null)
        val newuri = UriBuilder(tobase).from(touri).path(dstpath).buildOrNull()
            ?: return Pair(apath, null)
        return if (touri.isRelative)
            Pair(apath, newuri.toRelative())
        else
            Pair(apath, newuri)
    }

    fun buildXrefs(dir: IFileInfo): MutableMap<String, Collection<String>> {
        val ret = TreeMap<String, Collection<String>>()
        buildXrefs(ret, dir)
        return ret
    }

    fun buildXrefs(ret: MutableMap<String, Collection<String>>, dir: IFileInfo) {
        dir.walk2 { file, stat ->
            if (stat.isFile) {
                buildXrefs(file) { apath, refs ->
                    ret.put(apath, refs)
                }
            }
        }
    }

    fun buildXrefs(file: IFileInfo, callback: Fun20<String, Collection<String>>) {
        when (file.lcSuffix) {
            Suffix.HTML -> buildHtmlXrefs(file, callback)
            Suffix.CSS -> buildCSSXrefs(file, callback)
        }
    }

    fun moveXrefs(dst: IFileInfo, src: IFileInfo, file: IFileInfo): Pair<ICharSlice?, Collection<String>>? {
        return when (file.lcSuffix) {
            Suffix.HTML -> moveHtmlXrefs(dst.apath, src.apath, file)
            Suffix.CSS -> moveCSSXrefs(dst.apath, src.apath, file)
            else -> null
        }
    }

    /// Update references in all files that reference src file.
    /// @return { apath -> [apath] } The updated xrefs for files that reference the src file.
    fun onMove(
        dst: IFileInfo,
        src: IFileInfo,
        froms: Collection<IFileInfo>?,
    ): Map<String, Collection<String>> {
        val ret = TreeMap<String, Collection<String>>()
        froms?.forEach { file ->
            moveXrefs(dst, src, file)?.let { (output, xrefs) ->
                if (output != null) {
                    file.content().write(output.reader(), System.currentTimeMillis(), Charsets.UTF_8)
                }
                ret.put(file.apath, xrefs)
            }
        }
        return ret
    }

    /**
     * Update xrefs in dst file on copy.
     * @return (output, xrefs) Where <code>output</code> is content of dst with xrefs fixed,
     * may be null if dst content is same as src or if there are errors.
     * <code>xrefs</code> are all the xrefs in output, may be empty if there are errors.
     */
    fun onCopy(
        dst: IFileInfo,
        src: IFileInfo,
        lcsuffix: String,
        incopyset: Fun11<String, Boolean>
    ): Pair<ICharRange?, Collection<String>?> {
        val ret = if (lcsuffix == Suffix.HTML) {
            UriBuilder().path(dst.apath).buildOrNull()?.let { tobase ->
                rebaseHtmlXrefs(src, tobase, incopyset)
            }
        } else if (lcsuffix == Suffix.CSS) {
            UriBuilder().path(dst.apath).buildOrNull()?.let { tobase ->
                rebaseCSSXrefs(src, tobase, incopyset)
            }
        } else {
            null
        }
        return ret ?: Pair(null, null)
    }

    fun onCopy(
        st: ISettingsStoreAccessor,
        dst: IFileInfo,
        src: IFileInfo,
        lcsuffix: String,
        cut: Boolean,
        preservetimestamp: Boolean,
        incopyset: Fun11<String, Boolean>
    ): Collection<String>? {
        val (output, xrefs) = XrefUt.onCopy(dst, src, lcsuffix, incopyset)
        if (output == null) {
            val timestamp = (if (preservetimestamp) src.stat()?.lastModified else null) ?: System.currentTimeMillis()
            if (cut) src.content().moveTo(dst, timestamp)
            else src.content().copyTo(dst, timestamp)
        } else {
            dst.content().write(output.array, output.offset, output.length, System.currentTimeMillis(), Charsets.UTF_8)
        }
        if (cut && src.delete()) {
            st.deleteXrefsFrom(src)
        }
        st.updateXrefs(dst.apath, xrefs)
        return null
    }

    fun onMove(storage: IStorage, st: ISettingsStoreAccessor, dst: IFileInfo, src: IFileInfo, incopyset: Fun11<String, Boolean>) {
        val xrefs = st.getXrefsTo(src.apath)?.mapNotNull { value ->
            storage.fileInfoAt(value).result()?.let {
                if (it.exists && !incopyset(it.apath)) it else null
            }
        }
        val map = XrefUt.onMove(dst, src, xrefs)
        st.updateXrefs(map)
    }

    fun buildCSSXrefs(file: IFileInfo, callback: Fun20<String, Collection<String>>) {
        file.apath.let { apath ->
            UriBuilder().path(apath).buildOrNull()?.let { baseuri ->
                XrefUt.buildCSSXrefs(file, apath, baseuri).let {
                    callback(apath, it)
                }
            }
        }
    }

    fun buildCSSXrefs(file: IFileInfo, msg: String, baseuri: BaseUri): Collection<String> {
        val ret = TreeSet<String>()
        parseCSSXrefs(file.asChars(), msg) { _, href ->
            filepathOfUrl(href, baseuri)?.let {
                ret.add(it)
            }
        }
        return ret
    }

    fun rebaseCSSXrefs(
        file: IFileInfo,
        tobase: BaseUri,
        incopyset: Fun11<String, Boolean>
    ): Pair<ICharRange?, Collection<String>> {
        val xrefs = TreeSet<String>()
        val apath = file.apath
        val output = UriBuilder().path(apath).buildOrNull()?.let { frombase ->
            parseCSSXrefs(file.asChars(), apath) { tok, href ->
                rebaseUri(tobase, frombase, href, incopyset).let { (frompath, touri) ->
                    if (touri == null) {
                        xrefs.add(frompath)
                    } else {
                        val to = touri.toString()
                        
                        tok.text = "url(${CSSUtil.quoteUrl(to)})"
                        xrefs.add(touri.toAbsolute().path)
                    }
                }
            }?.firstToken?.let { tok ->
                render(tok)
            }
        }
        return Pair(output, xrefs)
    }

    fun moveCSSXrefs(dstpath: String, srcpath: String, file: IFileInfo): Pair<ICharRange?, Collection<String>>? {
        val apath = file.apath
        return UriBuilder().path(apath).buildOrNull()?.let { baseuri ->
            val xrefs = TreeSet<String>()
            parseCSSXrefs(file.asChars(), apath) { tok, href ->
                moveUri(dstpath, srcpath, href, baseuri).let { (frompath, touri) ->
                    if (touri == null) {
                        xrefs.add(frompath)
                    } else {
                        val to = touri.toString()
                        
                        tok.text = "url(${CSSUtil.quoteUrl(to)})"
                        xrefs.add(touri.toAbsolute().path)
                    }
                }
            }?.firstToken?.let { tok ->
                val output = render(tok)
                Pair(output, xrefs)
            }
        }
    }

    fun parseCSSXrefs(source: CharArray, msg: String, callback: Fun20<ILLKToken, String>): ASTstyleSheet? {
        val main = NullLLKMain(msg)
        
        return Support.ignoreThrowable(msg) {
            val input = LLKLexerInput(source, main)
            val stylesheet = CSSParser(
                CSSLexer(
                    input,
                    DefaultTokenHandler.getInstance(input, CSSLexer.specialTokens(), true, true)
                )
            ).styleSheet()
            stylesheet
                .accept(object : LLKCSSParserVisitorAdapter<String>() {
                    override fun visit(node: ASTterm, data: String): String {
                        Support.ignoreThrowable(node) {
                            node.firstToken?.let { tok ->
                                if (tok.type == ILLKCSSLexer.URI) {
                                    tok.text?.let {
                                        val url = CSSUtil.unesc(it.toString())
                                        var href = urlPat.matchEntire(url)?.let { it.groupValues[1] } ?: url
                                        href = TextUt.unquote(href, "\"'").toString()
                                        callback(tok, href)
                                    }
                                }
                            }
                        }
                        return super.visit(node, data)
                    }
                }, "")
            stylesheet
        }
    }

    fun buildHtmlXrefs(file: IFileInfo, callback: Fun20<String, Collection<String>>) {
        file.apath.let { apath ->
            UriBuilder().path(apath).buildOrNull()?.let { baseuri ->
                XrefUt.buildHtmlXrefs(file, apath, baseuri).let {
                    callback(apath, it)
                }
            }
        }
    }

    fun buildHtmlXrefs(file: IFileInfo, filepath: String, baseuri: BaseUri): Collection<String> {
        val ret = TreeSet<String>()
        parseHtmlXrefs(file.asChars(), filepath) { _, _, href ->
            filepathOfUrl(href, baseuri)?.let { path ->
                ret.add(path)
            }
        }
        return ret
    }

    fun rebaseHtmlXrefs(
        file: IFileInfo,
        tobase: BaseUri,
        incopyset: Fun11<String, Boolean>,
    ): Pair<ICharRange?, Collection<String>> {
        val xrefs = TreeSet<String>()
        val apath = file.apath
        val output = UriBuilder().path(apath).buildOrNull()?.let { frombase ->
            parseHtmlXrefs(file.asChars(), apath) { _, tok, href ->
                rebaseUri(tobase, frombase, href, incopyset).let { (frompath, touri) ->
                    if (touri == null) {
                        xrefs.add(frompath)
                    } else {
                        val to = touri.toString()
                        
                        tok.text = XMLUt.quoteAttr(to)
                        xrefs.add(touri.toAbsolute().path)
                    }
                }
            }?.firstToken?.let {
                render(it)
            }
        }
        return Pair(output, xrefs)
    }

    fun moveHtmlXrefs(dstpath: String, srcpath: String, file: IFileInfo): Pair<ICharRange?, Collection<String>>? {
        val apath = file.apath
        return UriBuilder().path(apath).buildOrNull()?.let { baseuri ->
            val xrefs = TreeSet<String>()
            parseHtmlXrefs(file.asChars(), apath) { _, tok, href ->
                moveUri(dstpath, srcpath, href, baseuri).let { (frompath, touri) ->
                    if (touri == null) {
                        xrefs.add(frompath)
                    } else {
                        val to = touri.toString()
                        
                        tok.text = XMLUt.quoteAttr(to)
                        xrefs.add(touri.toAbsolute().path)
                    }
                }
            }?.firstToken?.let {
                val output = render(it)
                Pair(output, xrefs)
            }
        }
    }

    fun parseHtmlXrefs(source: CharArray, msg: String, callback: Fun30<ASTattribute, ILLKToken, String>): ASTdocument? {
        val main = NullLLKMain(msg)
        
        fun add(attr: ASTattribute) {
            attr.valueToken?.let { tok ->
                tok.text?.let {
                    val value = XMLUt.unesc(it.toString())
                    callback(attr, tok, TextUt.unquote(value, "\"'").toString())
                }
            }
        }

        return Support.ignoreThrowable(msg) {
            val input = LLKLexerInput(source, main)
            object : HtmlSaxAdapter(
                HtmlLexer(
                    input,
                    DefaultTokenHandler.getInstance(input, HtmlLexer.specialTokens(), true, true)
                )
            ) {
                override fun startTag(node: ASTstartTag) {
                    Support.ignoreThrowable(node) {
                        node.getAttribute("src")?.let { add(it) }
                        node.getAttribute("href")?.let { add(it) }
                        super.startTag(node)
                    }
                }
            }.parse()
        }
    }

    fun render(tok: ILLKToken): ICharRange {
        fun render1(ret: Appendable, t: ILLKToken) {
            for (s in t.specials()) {
                ret.append(s.text)
            }
            ret.append(t.text)
        }
        return MyCharArrayWriter().use { w ->
            var t: ILLKToken? = tok
            while (t != null) {
                render1(w, t)
                t = t.next
            }
            w
        }
    }
}

class NullLLKMain constructor(
    filepath: String,
    locator: ISourceLocator? = null,
    private val out: StringPrintStream = StringPrintStream(),
    private val err: StringPrintStream = StringPrintStream(),
) : SimpleLLKMain(filepath, TreeMap(), locator, out, err) {

    override fun getFileContent(): CharArray {
        throw UnsupportedOperationException()
    }

    override fun getFileContent(charset: Charset?): CharArray {
        throw UnsupportedOperationException()
    }

    fun out(): String {
        out.flush()
        return out.toString()
    }

    fun err(): String {
        err.flush()
        return err.toString()
    }
}
