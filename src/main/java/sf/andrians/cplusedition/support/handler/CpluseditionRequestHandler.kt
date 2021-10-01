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
package sf.andrians.cplusedition.support.handler

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.MyByteArrayOutputStream
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.dsl.html.api.IElement
import sf.andrians.ancoreutil.dsl.html.impl.Html5Serializer
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.struct.IterableWrapper
import sf.andrians.ancoreutil.util.struct.ReversedComparator
import sf.andrians.ancoreutil.util.text.HtmlSpaceUtil
import sf.andrians.ancoreutil.util.text.TextUtil
import sf.andrians.ancoreutil.util.text.XmlUtil
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.Http
import sf.andrians.cplusedition.support.Http.HttpHeader
import sf.andrians.cplusedition.support.Http.HttpStatus
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IFileStat
import sf.andrians.cplusedition.support.IInputStreamProvider
import sf.andrians.cplusedition.support.ISeekableInputStream
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.StorageBase
import sf.andrians.cplusedition.support.StorageException
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.PathUtil
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.templates.Templates
import sf.andrians.org.apache.http.client.utils.URLEncodedUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.io.Writer
import java.net.URI
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

class CpluseditionRequestHandler(
        context: ICpluseditionContext,
        ajax: IAjax?,
        val thumbnailcallback: IThumbnailCallback
) : CpluseditionRequestHandlerBase(context) {

    val storage = context.getStorage()
    val rsrc = storage.rsrc
    private val recentsHandler: IRecentsHandler
    private val linkVerifier: LinkVerifier
    val filepicker: IFilepickerHandler
    val historyFilepicker: IFilepickerHandler

    init {
        filepicker = FilepickerHandler(storage, ajax, thumbnailcallback)
        historyFilepicker = HistoryFilepickerHandler(storage, ajax, thumbnailcallback)
        recentsHandler = RecentsHandler(context, filepicker)
        linkVerifier = LinkVerifier(context)
    }

    fun isLoggedIn(): Boolean {
        return storage.isLoggedIn()
    }

    @Throws(Exception::class)
    fun handleRequest(request: ICpluseditionRequest, response: ICpluseditionResponse, cleanrpath: String): Boolean {
        val info = storage.fileInfo(cleanrpath) ?: return false
        val path = "/$cleanrpath"
        if (!info.name.toLowerCase().endsWith(".html") && request.getParam(An.Param.view) != null) {
            if (!actionView(response, request.getParam(An.Param.mime), info, path)) {
                notfound(response, path)
            }
            return true
        }
        if (isResourceRequest(request, response, info, path)) {
            return true
        }
        if (cleanrpath.startsWith(An.PATH.assetsTemplatesRes_)
                && (csseditorHtml(request, response, path)
                        )) {
            return true
        }
        if (path.startsWith(An.PATH._assets_)
                && (errorPage404(request, response, path)
                        || errorPage500(request, response, path))) {
            return true
        }
        return htmlResponse(response, request, info)
    }

    private fun errorPage500(request: ICpluseditionRequest, response: ICpluseditionResponse, path: String): Boolean {
        if (An.PATH._assetsTemplates500Html == path) {
            var opath = request.getParam("path") ?: ""
            var msg = request.getParam("msg") ?: "ERROR"
            try {
                opath = URLDecoder.decode(opath, "UTF-8")
                msg = URLDecoder.decode(msg, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                context.e("# Should not happen", e)
            }
            htmlResponse(response, Templates.Error500Template().build(storage, opath, msg))
            return true
        }
        return false
    }

    private fun errorPage404(request: ICpluseditionRequest, response: ICpluseditionResponse, path: String): Boolean {
        if (An.PATH._assetsTemplates404Html == path) {
            var opath = request.getParam("path") ?: ""
            if (opath.isNotEmpty()) {
                opath = try {
                    URLDecoder.decode(opath, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    context.e("# Should not happen", e)
                    ""
                }
            }
            var writable = false
            if (opath.startsWith("/") && filepicker != null /* filepickerHandler may be null in testing only */) {
                var dir = opath
                while (true) {
                    dir = Basepath.dir(dir) ?: break
                    val stat = storage.fileInfoAt(dir).first?.stat()
                    if (stat != null) {
                        writable = stat.writable
                        break
                    }
                }
            }
            htmlResponse(response, Templates.Error404Template().build(storage, writable, opath))
            return true
        }
        return false
    }

    private fun csseditorHtml(request: ICpluseditionRequest, response: ICpluseditionResponse, path: String): Boolean {
        if (An.PATH._assetsCSSEditorHtml == path) {
            htmlResponse(response, Templates.CSSEditorTemplate().build(storage))
            return true
        }
        return false
    }

    fun actionRecents(cmd: Int): String {
        return try {
            recentsHandler.handleRecents(cmd)
        } catch (e: Throwable) {
            rsrc.jsonError(e, R.string.CommandFailed, ": recents: " + Support.RecentsCmdUtil.toString(cmd))
        }
    }

    fun actionRecentsPut(navigation: Int, cpath: String, state: JSONObject?): String {
        recentsHandler.recentsPut(navigation, cpath, state)
        return "{}"
    }

    fun actionFilepicker(cmd: Int, params: JSONObject): String {
        return try {
            filepicker.handle(cmd, params)
        } catch (e: Throwable) {
            rsrc.jsonError(e, R.string.CommandFailed, ": filepicker: " + Support.FilepickerCmdUtil.toString(cmd))
        }
    }

    fun actionHistoryFilepicker(cmd: Int, params: JSONObject): String {
        return try {
            historyFilepicker.handle(cmd, params)
        } catch (e: Throwable) {
            rsrc.jsonError(e, R.string.CommandFailed, ": historyFilepicker: " + Support.FilepickerCmdUtil.toString(cmd))
        }
    }

    /**
     * @return @nullable null if cpath is not valid.
     */
    fun actionFileInfo(cpath: String?): IFileInfo? {
        return storage.fileInfoAt(cpath).first
    }

    fun actionLinkVerifier(cmd: Int, data: String): String {
        return try {
            linkVerifier.handle(cmd, data)
        } catch (e: Throwable) {
            rsrc.jsonError(e, R.string.CommandFailed, ": linkVerifier: ${ILinkVerifier.Cmd.toString(cmd)}")
        }
    }

    @Throws(IOException::class, JSONException::class)
    fun actionToggleNoBackup(cpath: String): String {
        val dirinfo = StorageBase.getWritableDocumentDirectory(storage, cpath).let {
            it.first ?: return rsrc.jsonError(it.second!!)
        }
        val nobackuppath = dirinfo.apath + File.separatorChar + An.DEF.nobackup
        val nobackup = dirinfo.fileInfo(An.DEF.nobackup)
        val exists = nobackup.exists
        if (exists) {
            if (!nobackup.delete()) {
                return rsrc.jsonError(R.string.ErrorDeleting_, nobackuppath)
            }
        } else {
            try {
                nobackup.content().write(byteArrayOf().inputStream())
            } catch (e: Exception) {
                return rsrc.jsonError(R.string.ErrorCreatingFile_, nobackuppath)
            }
        }
        val json = actionFilepicker(An.FilepickerCmd.LISTDIR, JSONObject().put(Key.path, cpath))
        val ret = JSONObject(json)
        ret.put(Key.status, !exists)
        return ret.toString()
    }

    @Throws(Exception::class)
    fun recentsSave(state: JSONObject) {
        recentsHandler.recentsSave(state)
    }

    fun recentsRestore(recents: JSONArray?) {
        recentsHandler.recentsRestore(recents)
    }

    /**
     * @param templatecontent The template file content.
     * @param outpath         The path to the destination file, must starts with /Documents/.
     * @return {An.Key.errors: errors}
     * @throws Exception On error.
     */
    @Throws(Exception::class)
    fun actionTemplate(templatecontent: String, outpath: String): String {
        val basepath = Basepath.from(outpath)
        if (".html" != basepath.suffix) return rsrc.jsonError(R.string.ExpectingHtml)
        val outinfo = storage.fileInfoAt(outpath).let {
            it.first ?: return rsrc.jsonError(it.second)
        }
        if (!outinfo.mkparent()) {
            //#TODO R.string.ErrorCreatingParentDirectory_
            return rsrc.jsonError(R.string.ErrorCreatingDirectory_, outpath)
        }
        outinfo.content().write(templatecontent.byteInputStream())
        return "{}"
    }

    /**
     * @param json {
     * An.Key.tag: String,
     * An.Key.attrs: String,
     * } where An.Key.url is an encoded absolute URI or an baseurl relative URI.
     * @return ret {
     * An.Key.errors: Object, // errors
     * An.Key.tag: String, // Sanitized tag
     * An.Key.attrs: {String: String}, // Sanitized attributes
     * } where An.Key.url is an absolute encoded URI.
     * @throws Exception On error.
     */
    @Throws(Exception::class)
    fun actionSanitize(json: String?): String {
        val params = JSONObject(json)
        val errors = ArrayList<String>()
        val warns = JSONArray()
        val ret = JSONObject()
        if (params.has(Key.attrs)) {
            //// NOTE that the attribute string is in space separated pairs editing format instead of format html
            //// format.
            val attrs = params.stringOrNull(Key.attrs)
            val namevalues = JSONObject()
            if (attrs != null) {
                parseSpaceSeparateAttributes(errors, namevalues, attrs)
            }
            if (errors.size == 0) {
                ret.put(Key.attrs, namevalues)
            }
        }
        if (params.has(Key.tag)) {
            val tagname = params.stringOrDef(Key.tag, "")
            val tag = HtmlTag.get(tagname)
            if (tag == null) {
                warns.put(rsrc.get(R.string.HtmlTagInvalid_, tagname))
            } else if (tag.flags() and Flags.User == 0) {
                warns.put(rsrc.get(R.string.UnsupportedTag, ": ", tagname))
            } else {
                ret.put(Key.tag, tag.name)
            }
        }
        if (errors.size > 0) {
            ret.put(Key.errors, JSONArray(errors))
        }
        if (warns.length() > 0) {
            ret.put(Key.warns, warns)
        }
        return ret.toString()
    }

    /**
     * Esc. unicode characters to quoted "\xxxx" format.
     */
    private fun escCSSValue(key: String, value: String): String {
        var start = 0
        val end = value.length
        var isquoted = false
        if (end > 2) {
            val c = value[0].toInt()
            isquoted = (c == 0x22 || c == 0x27) && c == value[end - 1].toInt()
        }
        if ("content" != key && !isquoted) {
            return value
        }
        var ret: StringBuilder? = null
        for (i in 0 until end) {
            val c = value[i].toInt()
            if (c < 0x20 || c >= 0x7f || !isquoted && c == 0x22) {
                if (ret == null) {
                    ret = StringBuilder()
                    if (!isquoted) {
                        ret.append("\"")
                    }
                }
                if (start < i) {
                    ret.append(value.substring(start, i))
                }
                ret.append(String.format("\\%06x", c))
                start = i + 1
            }
        }
        if (ret == null) {
            return value
        }
        if (start < end) {
            ret.append(value.substring(start, end))
        }
        if (!isquoted) {
            ret.append("\"")
        }
        return ret.toString()
    }

    fun illegalCharacter(index: Int, c: Char): String {
        return rsrc.get(R.string.IllegalFilenameCharacter_, " @$index: $c")
    }

    fun actionSanitizeHtml(content: String?): String {
        return HandlerUtil.jsonResult(content)
    }

    private fun hasCSSUrl(cssvalue: String?): Boolean {
        return cssvalue != null && !cssvalue.isEmpty() && cssvalue.contains("url(")
    }

    fun cleanupCSSUrl(errors: MutableList<String>, cssvalue: String?): String? {
        return if (!hasCSSUrl(cssvalue)) cssvalue else cleanupCSSUrl1(errors,
                cleanupCSSUrl1(errors,
                        cleanupCSSUrl1(errors, cssvalue, CSSURLQQ),
                        CSSURLQ),
                CSSURL)
    }

    fun cleanupCSSUrl1(errors: MutableList<String>, cssvalue: String?, regex: Pattern): String? {
        if (cssvalue == null || !hasCSSUrl(cssvalue)) return cssvalue
        val m = regex.matcher(cssvalue)
        if (m.find()) {
            val start = m.start(0)
            val end = m.end(0)
            val url = m.group(1)
            if (url != null) {
                //#FIXME Unfortunately, Swift URL class always works in strict form
                //# but we allow human readable form of url, eg. with Chinese characters, in editing.
                return try {
                    val u = URI(url)
                    val quote = if (m.groupCount() >= 2) m.group(2) else ""
                    val path = TextUtil.cleanupFilePath(u.path).toString()
                    val uu = URI(null, null, path, u.query, u.fragment)
                    val suffix = cleanupCSSUrl1(errors, cssvalue.substring(end), regex)
                    val prefix = cssvalue.substring(0, start)
                    val ret = prefix + "url(" + quote + uu.toASCIIString() + quote + ")" + suffix
                    
                    ret
                } catch (e: Throwable) {
                    errors.add(rsrc.get(R.string.InvalidURL_, url))
                    null
                }
            }
        }
        return cssvalue
    }

    @Throws(JSONException::class)
    fun actionSanitizeAttributes(content: String): String {
        val errors = ArrayList<String>()
        val namevalues = JSONObject()
        parseSpaceSeparateAttributes(errors, namevalues, content)
        val ret = JSONObject()
        if (errors.size > 0) {
            HandlerUtil.jsonErrors(ret, errors)
        } else {
            ret.put(Key.attrs, namevalues)
        }
        return ret.toString()
    }

    @Throws(JSONException::class)
    private fun parseSpaceSeparateAttributes(errors: MutableList<String>, namevalues: JSONObject, source: String) {
        val sutil = HtmlSpaceUtil.singleton
        val lines = TextUtil.splitLines(source, false)
        for (line in lines) {
            val end = line.length
            val start = sutil.skipWhitespaces(line, 0, end)
            if (start == end) {
                continue
            }
            val index = TextUtil.indexOf(' ', line, start, end)
            var name: String
            var value: String
            if (index < 0) {
                name = XmlUtil.unescXml1(line).replace('\u00a0', ' ').trim { it <= ' ' }.toLowerCase(Support.LOCALE)
                value = ""
            } else {
                name = XmlUtil.unescXml1(line.substring(0, index)).replace('\u00a0', ' ').trim { it <= ' ' }.toLowerCase(Support.LOCALE)
                value = XmlUtil.unescXml1(line.substring(index + 1)).replace('\u00a0', ' ')
            }
            if (("style" == name || "class" == name)
                    && (value.isEmpty() || value.trim { it <= ' ' }.isEmpty())) {
                continue
            }
            if (name.startsWith("x-") || name.startsWith("aria-")) {
                namevalues.put(name, value)
                continue
            }
            val attr = HtmlAttr.get(name)
            if (attr != null) {
                if (attr.flags() and Flags.User == 0) {
                    errors.add(rsrc.get(R.string.HtmlAttributeUnsupported_, name))
                } else {
                    namevalues.put(name, value)
                }
                continue
            }
            if (Support.AttrUtil.canEdit(name)) {
                namevalues.put(name, value)
                continue
            }
            val event = HtmlEvent.get(name)
            if (event != null) {
                errors.add(rsrc.get(R.string.HtmlAttributeUnsupported_, name))
            } else {
                errors.add(rsrc.get(R.string.HtmlAttributeInvalid_, name))
            }
        }
    }

    @Throws(Exception::class)
    fun actionView(response: ICpluseditionResponse, mime: String?, info: IFileInfo, path: String): Boolean {
        var mime1 = mime
        if (mime1 == null) {
            val lcext = Basepath.lcExt(info.name)
            mime1 = MimeUtil.mediaMimeFromLcExt(lcext)
            if (mime1 == null) {
                return false
            }
        }
        if (!info.exists) return false
        if (mime1.startsWith("image/")) {
            val csspath = An.PATH._assetsImageCss + "?t=" + storage.getCustomResourcesTimestamp()
            htmlResponse(response, Templates.ImageTemplate().build(csspath, info.name))
            return true
        } else if (mime1.startsWith("audio/")) {
            val csspath = An.PATH._assetsAudioCss + "?t=" + storage.getCustomResourcesTimestamp()
            htmlResponse(response, Templates.AudioTemplate().build(csspath, path))
            return true
        } else if (mime1.startsWith("video/")) {
            val csspath = An.PATH._assetsVideoCss + "?t=" + storage.getCustomResourcesTimestamp()
            htmlResponse(response, Templates.VideoTemplate().build(csspath, path))
            return true
        } else if (mime1 == MimeUtil.PDF) {
            if (pdfRequest(response, info, path)) {
                return true
            }
        }
        return false
    }

    //#BEGIN FIXME
    //#END FIXME

    private fun isResourceRequest(request: ICpluseditionRequest, response: ICpluseditionResponse, info: IFileInfo, path: String): Boolean {
        val lcext = Basepath.lcExt(info.name)
        if (isJavascriptRequest(response, info, path, lcext)) {
            return true
        }
        if (isCSSRequest(response, info, path, lcext)) {
            return true
        }
        if (fontRequest(response, info, path, lcext)) {
            return true
        }
        if (imageRequest(response, info, path, lcext)) {
            return true
        }
        if (audioRequest(response, request, info, path, lcext)) {
            return true
        }
        if (isPlainTextRequest(path)) {
            resourceResponse(response, "text/plain", info, path)
            return true
        }
        if (pdfRequest(response, info, path)) {
            return true
        }
        if (videoRequest(response, request, info, path, lcext)) {
            return true
        }
        return false
    }

    private fun isPlainTextRequest(path: String): Boolean {
        return false
    }

    private fun isJavascriptRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcext: String?): Boolean {
        if ("js" == lcext) {
            resourceResponse(response, "text/javascript", info, path)
            return true
        }
        return false
    }

    fun fontRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcext: String?): Boolean {
        val mime = MimeUtil.fontMimeFromLcExt(lcext) ?: return false
        resourceResponse(response, mime, info, path)
        return true
    }

    private fun isCSSRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcext: String?): Boolean {
        if ("css" == lcext) {
            resourceResponse(response, "text/css", info, path)
            return true
        }
        return false
    }

    private fun imageRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcext: String?): Boolean {
        val mime = MimeUtil.imageMimeFromLcExt(lcext) ?: return false
        if (path == "/favicon.ico") {
            resourceResponse(response, mime, storage.fileInfo(An.PATH._assetsTemplatesFaviconIco)!!, An.PATH._assetsTemplatesFaviconIco)
            return true
        }
        if (!info.exists) return false
        resourceResponse(response, mime, info, path)
        return true
    }

    private fun pdfRequest(response: ICpluseditionResponse, info: IFileInfo, path: String): Boolean {
        val mime = MimeUtil.pdfMimeFromPath(info.name) ?: return false
        resourceResponse(response, mime, info, path)
        if (!info.exists) return false
        return true
    }

    private fun audioRequest(response: ICpluseditionResponse, request: ICpluseditionRequest, info: IFileInfo, path: String, lcext: String?): Boolean {
        val mime = MimeUtil.audioPlaybackMimeFromLcExt(lcext) ?: return false
        if (!info.exists) return false
        mediaResponse(request, response, mime, info, path)
        return true
    }

    private fun videoRequest(response: ICpluseditionResponse, request: ICpluseditionRequest, info: IFileInfo, path: String, lcext: String?): Boolean {
        val mime = MimeUtil.videoPlaybackMimeFromLcExt(lcext) ?: return false
        if (!info.exists) return false
        mediaResponse(request, response, mime, info, path)
        return true
    }

    private fun htmlResponse(response: ICpluseditionResponse, element: IElement) {
        try {
            response.setupHtmlResponse()
            val data = MyByteArrayOutputStream()
            data.writer().use {
                element.accept(Html5Serializer<Writer>("    ").indent("").noXmlEndTag(true), it)
            }
            response.setContentLength(data.size().toLong())
            response.setData(data.inputStream())
        } catch (e: Throwable) {
            context.e("# ERROR: htmlResponse(): Send response failed", e)
        }
    }

    private fun htmlResponse(response: ICpluseditionResponse, content: String) {
        htmlResponse(response, content.toByteArray(Charsets.UTF_8))
    }

    private fun htmlResponse(response: ICpluseditionResponse, bytes: ByteArray) {
        try {
            response.setupHtmlResponse()
            response.setContentLength(bytes.size.toLong())
            response.setData(bytes.inputStream())
        } catch (e: Throwable) {
            context.e("# ERROR: htmlResponse(): Send response failed", e)
        }
    }

    @Throws(Exception::class)
    private fun htmlResponse(response: ICpluseditionResponse, request: ICpluseditionRequest, fileinfo: IFileInfo): Boolean {
        if (Basepath.lcSuffix(fileinfo.name) != An.DEF.htmlSuffix) return false
        if (!fileinfo.exists) throw FileNotFoundException(fileinfo.apath)
        //#IF USE_SEARCH_HIGHLIGHT
        //#ENDIF USE_SEARCH_HIGHLIGHT
        htmlResponse(response, fileinfo.content().readBytes())
        return true
    }

    //#IF USE_SEARCH_HIGHLIGHT
    //#ENDIF USE_SEARCH_HIGHLIGHT

    private fun resourceResponse(response: ICpluseditionResponse, mime: String, info: IFileInfo, path: String) {
        try {
            response.setContentType(mime)
            storage.submit { it.resourceResponse(response, info) }.get()
        } catch (e: FileNotFoundException) {
            notfound(response, path)
        } catch (e: Throwable) {
            servererror(response, path, e)
        }
    }

    private fun mediaResponse(request: ICpluseditionRequest, response: ICpluseditionResponse, mime: String, info: IFileInfo, path: String) {
        val max = 10 * 1024 * 1024L
        try {
            response.setContentType(mime)
            response.setHeader(HttpHeader.Connection, "keep-alive")
            response.setHeader(HttpHeader.NoCache, "true")
            response.setHeader(HttpHeader.CacheControl, "no-cache")
            response.setHeader(HttpHeader.KeepAlive, "timeout=20")
            var range: Http.HttpRange? = null
            val value = request.getHeader(HttpHeader.Range)
            if (value != null) {
                val totallength = info.stat()?.length ?: throw IOException()
                range = Http.HttpRange.parse(value, totallength, Long.MAX_VALUE)
            }
            if (range == null) {
                storage.submit { it.resourceResponse(response, info) }.get()
                return
            }
            val size = range.size()
            response.setStatus(HttpStatus.PartialContent)
            response.setHeader(HttpHeader.AcceptRanges, "bytes")
            response.setHeader(HttpHeader.ContentRange, range.contentRange())
            response.setContentLength(size)
            storage.submit {
                response.setData(PartialInputStream(info.content().getSeekableInputStream(), range.first, size))
            }.get()
        } catch (e: FileNotFoundException) {
            notfound(response, path)
        } catch (e: Throwable) {
            servererror(response, path, e)
        }
    }

    fun servererror(response: ICpluseditionResponse, path: String?, e: Throwable?) {
        context.e("CpluseditionRequestHandler: servererror" + if (path == null) "" else ": $path", e)
        response.setStatus(HttpStatus.InternalServerError)
    }

    fun badrequest(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: backrequest: $path")
        response.setStatus(HttpStatus.BadRequest)
    }

    fun notfound(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: notfound: $path")
        response.setStatus(HttpStatus.NotFound)
    }

    fun unsatifiableRangeError(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: unsatifiableRangeError: $path")
        response.setStatus(HttpStatus.RequestedRangeNotSatisfiable)
    }

    companion object {
        fun partialResponse(output: OutputStream, input: ISeekableInputStream, start: Long, size: Long) {
            if (size >= Integer.MAX_VALUE.toLong()) throw IOException()
            val bufsize = 64 * 1024
            val b = ByteArray(bufsize)
            var offset = 0
            var remaining = size.toInt()
            while (remaining > 0) {
                val len = min(remaining, b.size)
                val n = input.readAt(start + offset, b, 0, len)
                if (n < 0) throw IOException()
                output.write(b, 0, n)
                offset += n
                remaining -= n
            }
        }

        private val CSSURLQQ = Pattern.compile("url\\(\\s*\"([^\"]+?)(\")\\s*\\)")
        private val CSSURLQ = Pattern.compile("url\\(\\s*'([^']+?)(')\\s*\\)")
        private val CSSURL = Pattern.compile("url\\(\\s*((?![\"']).*?)\\s*\\)")
    }

}

internal class PartialInputStream(
        private val input: ISeekableInputStream,
        private val start: Long,
        private val size: Long
) : InputStream() {
    val buf = ByteArray(1)
    var position = start
    var end = start + size

    override fun read(): Int {
        if (position >= end) return -1
        if (input.readAt(position, buf, 0, 1) <= 0) throw IOException()
        position += 1
        return (buf[0].toInt() and 0xff)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (position >= end) return -1
        val length = min(end - position, len.toLong()).toInt()
        val n = input.readAt(position, b, off, length)
        if (n > 0) position += n
        return n
    }

    override fun close() {
        input.close()
    }
}

internal class ChunkedInputStream(
        private val input: ISeekableInputStream,
        private val start: Long,
        private val size: Long
) : InputStream() {
    val crlf = "\r\n".toByteArray()
    val bufsize = 64 * 1024
    val buf = ByteArray(bufsize)
    var inputstream = byteArrayOf().inputStream()
    var offset = 0L
    var remaining = size

    init {
        ensureAvailable()
    }

    override fun read(): Int {
        ensureAvailable()
        return inputstream.read()
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureAvailable()
        return inputstream.read(b, off, len)
    }

    override fun close() {
        input.close()
    }

    private fun ensureAvailable() {
        if (inputstream.available() == 0) {
            if (remaining == 0L) {
                inputstream = "0\r\n\r\n".toByteArray().inputStream()
                return
            }
            val len = min(remaining, (bufsize - 1024).toLong()).toInt()
            val header = (len.toString(16) + "\r\n").toByteArray()
            header.copyInto(buf, 0)
            input.readFullyAt(start + offset, buf, header.size, len)
            crlf.copyInto(buf, header.size + len)
            offset += len
            remaining -= len
            inputstream = ByteArrayInputStream(buf, 0, header.size + len + 2)
        }
    }
}

public class GalleryGenerator(
        private val storage: IStorage,
        private val isLandscape: Fun31<String, IInputStreamProvider, Int, Boolean?>,
        private val ajax: Fun10<JSONObject>
) {
    private val res = storage.rsrc

    private class ImageInfos(
            var size: Long,
            var count: Int,
            var infos: Map<String, List<ImageInfo>>
    )

    private class ImageInfo(
            var cpath: String,
            var rpath: String,
            var basepath: Basepath
    )

    private fun error(msgid: Int, vararg args: String) {
        ajax(res.jsonObjectError(msgid, *args))
    }

    private fun error(msg: Collection<String>) {
        ajax(res.jsonObjectError(msg))
    }

    private fun result(result: JSONObject) {
        ajax(result)
    }

    fun run(
            outpath: String,
            templatepath: String,
            descending: Boolean,
            singlesection: Boolean,
            dirpath: String,
            rpaths: JSONArray
    ) {
        val outinfo = storage.fileInfoAt(outpath).first
        val outdirinfo = outinfo?.parent
        if (outinfo == null || outdirinfo == null) return error(R.string.InvalidOutputPath_, outpath)
        if (outdirinfo.stat()?.writable != true) return error(R.string.DestinationNotWritable, ": ", outpath)
        val templateinfo = storage.fileInfoAt(templatepath).let {
            it.first ?: return error(it.second)
        }
        if (!templateinfo.exists) return error(R.string.NotFound_, templatepath)
        val dirinfo = storage.fileInfoAt(dirpath).let {
            it.first ?: return error(it.second)
        }
        if (!dirinfo.exists) return error(R.string.NotFound_, dirpath)
        try {
            val content = templateinfo.content().readText()
            val bydir = sortbydir(rpaths)
            if (bydir.length() == 0) {
                return result(JSONObject())
            }
            val dirprefix = if (PathUtil.isAssetsTree(dirpath)) dirinfo.apath
            else (FileUtil.rpathOrNull(dirinfo.apath, outdirinfo.apath) ?: dirinfo.apath)
            
            val basepath = Basepath.from(templatepath)
            return when (basepath.nameWithoutSuffix) {
                An.TemplateName.audioV2 -> generateAudioV2Gallery(outinfo, content, dirprefix, dirinfo, descending, singlesection, bydir)
                An.TemplateName.homeSimpler -> generateHomeSimplerGallery(outinfo, content, dirprefix, dirinfo, descending, singlesection, bydir)
                An.TemplateName.mediaSticker -> generateMediaStickerGallery(outinfo, content, dirprefix, dirinfo, descending, singlesection, bydir)
                An.TemplateName.mediaWall -> generateMediaWallGallery(outinfo, content, dirprefix, dirinfo, descending, singlesection, bydir)
                else -> error(R.string.InvalidTemplate)
            }
        } catch (e: Exception) {
            return error(R.string.CommandFailed)
        }
    }

    private fun getStringList(a: JSONArray): List<String> {
        val ret: MutableList<String> = ArrayList()
        var index = 0
        val len = a.length()
        while (index < len) {
            val s = a.stringOrNull(index)
            if (s != null) {
                ret.add(s)
            }
            ++index
        }
        return ret
    }

    private fun _sorted0(ret: List<String>, descending: Boolean): List<String> {
        val c = java.util.Comparator { a: String, b: String -> a.compareTo(b) }
        ret.sortedWith(if (descending) ReversedComparator(c) else c)
        return ret
    }

    private fun _sorted(a: Collection<String>, descending: Boolean): List<String> {
        return _sorted0(ArrayList(a), descending)
    }

    private fun _sorted(a: Iterator<String>, descending: Boolean): List<String> {
        return _sorted0(a.asSequence().toList(), descending)
    }

    private fun _sorted(a: JSONArray, descending: Boolean): List<String> {
        return _sorted0(getStringList(a), descending)
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateAudioV2Gallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String? ->
            if (!singlesection) result.put(name, section)
            null
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addaudio = { path: String, cpath: String, filestat: IFileStat ->
            val url = href(path)
            section.put(cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), filestat)))
        }
        val addvideo = { path: String, cpath: String, filestat: IFileStat ->
            val url = href(path)
            section.put(cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), filestat)))
        }
        for (sec in _sorted(bydir.keys(), descending)) {
            beginsection(sec)
            for (rpath in _sorted(bydir.getJSONArray(sec), descending)) {
                val basepath = Basepath.from(rpath)
                val lcext = basepath.lcExt
                if (MimeUtil.isAudioPlaybackLcExt(lcext)) {
                    val fileinfo = dirinfo.fileInfo(rpath)
                    val filestat = fileinfo.stat()
                    if (filestat == null || !filestat.isFile) {
                        continue
                    }
                    addaudio(Basepath.joinRpath(dirprefix, rpath), fileinfo.cpath, filestat)
                } else if (MimeUtil.isVideoPlaybackLcExt(lcext)) {
                    val fileinfo = dirinfo.fileInfo(rpath)
                    val filestat = fileinfo.stat()
                    if (filestat == null || !filestat.isFile) {
                        continue
                    }
                    addvideo(Basepath.joinRpath(dirprefix, rpath), fileinfo.cpath, filestat)
                }
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.audioV2)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateHomeSimplerGallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String? ->
            if (!singlesection) result.put(name, section)
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addtoc2 = { rpathAndLabel: Pair<String, String>, cpath: String ->
            val url = href(Basepath.joinRpath(dirprefix, rpathAndLabel.first))
            section.put(cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, rpathAndLabel.second))
        }
        val addtoc3 = { rpathAndLabel: Pair<String, String>, cpath: String ->
            val url = href(Basepath.joinRpath(dirprefix, rpathAndLabel.first)) + "?view"
            section.put(cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, rpathAndLabel.second))
        }
        for (sec in _sorted(bydir.keys(), descending)) {
            beginsection(sec)
            for (rpath in _sorted(bydir.getJSONArray(sec), descending)) {
                val basepath = Basepath.from(rpath)
                val lcext = basepath.lcExt
                val fileinfo = dirinfo.fileInfo(rpath)
                val filestat = fileinfo.stat()
                if (filestat == null || !filestat.isFile) {
                    continue
                }
                if (An.DEF.html == lcext) {
                    addtoc2(Pair(rpath, label(basepath.nameWithoutSuffix)), fileinfo.cpath)
                } else if (MimeUtil.isMediaLcExt(lcext)) {
                    addtoc3(Pair(rpath, label(basepath.nameWithoutSuffix)), fileinfo.cpath)
                }
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.homeSimpler)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generatePhotoSticker1Gallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        val infos = scanMedias(dirinfo, descending, bydir) { MimeUtil.isImageLcExt(it) }
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String ->
            if (!singlesection) result.put(name, section)
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addimage = { rpath: String, label: String, suffix: String, fileinfo: IFileInfo ->
            
            val path = Basepath.joinRpath(dirprefix, rpath)
            val url = href(path)
            val islandscape = isLandscape(suffix, fileinfo.content(), 0)
            val orientation = if (islandscape == null) "" else if (islandscape === java.lang.Boolean.TRUE) "x-landscape" else "x-portrait"
            section.put(fileinfo.cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, label)
                    .put(Key.dimension, orientation)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), fileinfo.stat()!!)))
            Unit
        }
        for (sec in _sorted(infos.infos.keys, descending)) {
            beginsection(sec)
            for (info in infos.infos[sec]!!) {
                val fileinfo = storage.fileInfoAt(info.cpath).first
                if (fileinfo?.exists != true) continue
                addimage(info.rpath, label(info.basepath.nameWithoutSuffix), info.basepath.suffix, fileinfo)
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.photoSticker1)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generatePhotoWallGallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        val infos = scanMedias(dirinfo, descending, bydir) { MimeUtil.isImageLcExt(it) }
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String ->
            if (!singlesection) result.put(name, section)
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addimage = { rpath: String, label: String, suffix: String, fileinfo: IFileInfo ->
            val path = Basepath.joinRpath(dirprefix, rpath)
            val url = href(path)
            val islandscape = isLandscape(suffix, fileinfo.content(), 0)
            val orientation = if (islandscape === java.lang.Boolean.TRUE) "x-landscape" else "x-portrait"
            section.put(fileinfo.cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, label)
                    .put(Key.dimension, orientation)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), fileinfo.stat()!!)))
        }
        for (sec in _sorted(infos.infos.keys, descending)) {
            beginsection(sec)
            for (info in infos.infos[sec]!!) {
                val fileinfo = storage.fileInfoAt(info.cpath).first
                if (fileinfo?.exists != true) continue
                addimage(info.rpath, label(info.basepath.nameWithoutSuffix), info.basepath.suffix, fileinfo)
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.photoWall)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateMediaStickerGallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        fun ismedia(lcext: String?): Boolean {
            return MimeUtil.isImageLcExt(lcext)
                    || MimeUtil.isVideoPlaybackLcExt(lcext)
                    || MimeUtil.isAudioPlaybackLcExt(lcext)
        }

        val infos = scanMedias(dirinfo, descending, bydir, ::ismedia)
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String ->
            if (!singlesection) result.put(name, section)
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addimage = { rpath: String, label: String, basepath: Basepath, fileinfo: IFileInfo ->
            
            val path = Basepath.joinRpath(dirprefix, rpath)
            val url = href(path)
            val item = JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, label)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), fileinfo.stat()!!))
            if (ismedia(basepath.lcExt)) {
                val islandscape = isLandscape(basepath.suffix, fileinfo.content(), 0)
                val orientation = if (islandscape == null) "" else if (islandscape === java.lang.Boolean.TRUE) "x-landscape" else "x-portrait"
                item.put(Key.dimension, orientation)
            }
            section.put(fileinfo.cpath, item)
        }
        for (sec in _sorted(infos.infos.keys, descending)) {
            beginsection(sec)
            for (info in infos.infos[sec]!!) {
                val fileinfo = storage.fileInfoAt(info.cpath).first
                if (fileinfo?.exists != true) continue
                addimage(info.rpath, label(info.basepath.nameWithoutSuffix), info.basepath, fileinfo)
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.mediaSticker)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateMediaWallGallery(
            outinfo: IFileInfo,
            template: String,
            dirprefix: String?,
            dirinfo: IFileInfo,
            descending: Boolean,
            singlesection: Boolean,
            bydir: JSONObject
    ) {
        fun ismedia(lcext: String?): Boolean {
            return MimeUtil.isImageLcExt(lcext)
                    || MimeUtil.isVideoPlaybackLcExt(lcext)
                    || MimeUtil.isAudioPlaybackLcExt(lcext)
        }

        val infos = scanMedias(dirinfo, descending, bydir, ::ismedia)
        val result = JSONObject()
        var section = JSONObject()
        if (singlesection) result.put("", section)
        val beginsection = { name: String ->
            if (!singlesection) result.put(name, section)
        }
        val endsection = {
            if (!singlesection) section = JSONObject()
        }
        val addmedia = { rpath: String, label: String, basepath: Basepath, fileinfo: IFileInfo ->
            val path = Basepath.joinRpath(dirprefix, rpath)
            val url = href(path)
            val item = JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, label)
                    .put(Key.filestat, FileInfoUtil.tojson(JSONObject(), fileinfo.stat()!!))
            if (ismedia(basepath.lcExt)) {
                val islandscape = isLandscape(basepath.suffix, fileinfo.content(), 0)
                val orientation = if (islandscape == null) "" else if (islandscape === java.lang.Boolean.TRUE) "x-landscape" else "x-portrait"
                item.put(Key.dimension, orientation)
            }
            section.put(fileinfo.cpath, item)
        }
        for (sec in _sorted(infos.infos.keys, descending)) {
            beginsection(sec)
            for (info in infos.infos[sec]!!) {
                val fileinfo = storage.fileInfoAt(info.cpath).first
                if (fileinfo?.exists != true) continue
                addmedia(info.rpath, label(info.basepath.nameWithoutSuffix), info.basepath, fileinfo)
            }
            endsection()
        }
        result(JSONObject()
                .put(Key.type, An.TemplateName.mediaWall)
                .put(Key.backward, descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result))
    }

    private fun href(path: String): String {
        return URLEncodedUtils.encPath(path, TextUtil.UTF8())!!.replace("\"", "&quot;")
    }

    private fun label(text: String): String {
        if (text.isEmpty()) return text
        val c = text[0]
        return if (Character.isUpperCase(c)) text else Character.toUpperCase(c).toString() + text.substring(1)
    }

    @Throws(JSONException::class)
    private fun sortbydir(rpaths: JSONArray): JSONObject {
        val ret = JSONObject()
        var index = 0
        val len = rpaths.length()
        while (index < len) {
            val rpath = rpaths.stringOrNull(index)
            if (rpath == null) {
                ++index
                continue
            }
            val basepath = Basepath.from(rpath)
            var section = basepath.dir
            if (section == null) section = ""
            val sections = ret.optJSONArray(section)
            if (sections != null) {
                sections.put(rpath)
            } else {
                ret.put(section, JSONArray().put(rpath))
            }
            ++index
        }
        return ret
    }

    private fun sectionname(prefix: String, rdir: String): String {
        return Basepath.joinRpath(prefix, rdir)
    }

    @Throws(JSONException::class)
    private fun scanMedias(dirinfo: IFileInfo, descending: Boolean, bydir: JSONObject, ismedia: Fun11<String?, Boolean>): ImageInfos {
        var size = 0L
        var count = 0
        val map: MutableMap<String, List<ImageInfo>> = TreeMap()
        for (section in IterableWrapper.wrap(bydir.keys())) {
            val sorted: MutableList<ImageInfo> = ArrayList()
            for (rpath in _sorted(getStringList(bydir.getJSONArray(section)), descending)) {
                val basepath = Basepath.from(rpath)
                if (ismedia(basepath.lcExt)) {
                    val fileinfo = dirinfo.fileInfo(rpath)
                    val filestat = fileinfo.stat()
                    if (filestat == null || !filestat.isFile) {
                        continue
                    }
                    val cpath = fileinfo.apath
                    size += filestat.length
                    count += 1
                    sorted.add(ImageInfo(cpath, rpath, basepath))
                }
            }
            if (sorted.isNotEmpty()) {
                map[section] = sorted
            }
        }
        return ImageInfos(size, count, map)
    }

    companion object {
        private val audioV2Pat = Pattern.compile("(?s)^(.*?)<div\\s+class=\"xx-header\\s+.*?</ul>(.*?)(id=\"x-rightsidebar\".*)$")
        private val homePat = Pattern.compile("(?s)^" +
                "(.*<div\\s+class=\"xx-right\".*?)<div\\s+class=\"xx-section\".*?" +
                "(<div\\s+class=\"xx-footer\".*?)" +
                "(id=\"x-rightsidebar\".*)$")
        private val sticker1Pat = Pattern.compile("(?s)^(.*<div\\s+class=\"xx-top\">).*?(<div\\s+id=\"x-rightsidebar\".*)$")
        private val wallPat = Pattern.compile("(?s)^(.*)<div\\s+class=\"xx-section\">.*?(<div\\s+id=\"x-rightsidebar\".*)$")
    }
}
