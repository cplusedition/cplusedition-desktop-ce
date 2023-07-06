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

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.anjson.JSONUtil.findsJSONArrayNotNull
import com.cplusedition.anjson.JSONUtil.keyList
import com.cplusedition.anjson.JSONUtil.putJSONArray
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.IInputStreamProvider
import com.cplusedition.bot.core.MyByteOutputStream
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.XMLUt
import com.cplusedition.bot.core.bot
import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.impl.Html5Serializer
import com.cplusedition.bot.text.HtmlSpaceUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.FilepickerCmd
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.An.Param
import sf.andrians.cplusedition.support.An.XrefKey
import sf.andrians.cplusedition.support.AnUri.Companion.UriBuilder
import sf.andrians.cplusedition.support.BackupUtil
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.GalleryGenerator
import sf.andrians.cplusedition.support.GalleryParams
import sf.andrians.cplusedition.support.Http
import sf.andrians.cplusedition.support.Http.HttpHeader
import sf.andrians.cplusedition.support.Http.HttpStatus
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IFileStat
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.IStorageAccessor
import sf.andrians.cplusedition.support.MySeekableInputStream
import sf.andrians.cplusedition.support.StorageBase
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.FilepickerCmdUtil
import sf.andrians.cplusedition.support.XrefUt
import sf.andrians.cplusedition.support.asChars
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.media.IBarcodeUtil
import sf.andrians.cplusedition.support.media.IMediaUtil
import sf.andrians.cplusedition.support.media.ImageCropInfo
import sf.andrians.cplusedition.support.media.ImageOrPdfConverter
import sf.andrians.cplusedition.support.media.ImageOutputInfo
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.ImageUtil.Dim
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Mime
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.andrians.cplusedition.support.media.MimeUtil.isVideoLcSuffix
import sf.andrians.cplusedition.support.templates.Templates
import sf.andrians.cplusedition.support.walk2
import sf.andrians.cplusedition.support.walk3
import sf.llk.share.support.ILLKToken
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.io.Writer
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

abstract class CpluseditionRequestHandler constructor(
    context: ICpluseditionContext,
    ajax: IAjax?,
    protected val thumbnailcallback: IThumbnailCallback,
    protected val mediaUtil: IMediaUtil,
    protected val barcodeUtil: IBarcodeUtil,
) : CpluseditionRequestHandlerBase(context), ICpluseditionRequestHandler {

    protected val storage = context.getStorage()
    protected val rsrc = storage.rsrc
    protected val historyFilepicker: IFilepickerHandler
    private val linkVerifier: LinkVerifier
    private val recentsHandler: IRecentsHandler
    private val filepicker: IFilepickerHandler

    init {
        filepicker = FilepickerHandler(storage, ajax, thumbnailcallback)
        recentsHandler = RecentsHandler(context)
        historyFilepicker = HistoryFilepickerHandler(storage, ajax, thumbnailcallback)
        linkVerifier = LinkVerifier(context)
    }

    @Throws(Exception::class)
    override fun handleRequest(request: ICpluseditionRequest, response: ICpluseditionResponse, cleanrpath: String): Boolean {
        val info = storage.fileInfo(cleanrpath)
        if (info == null) {
            if (cleanrpath == "favicon.ico") {
                resourceResponse(
                    response,
                    Mime.ICO,
                    storage.fileInfo(PATH.assetsTemplatesFaviconIco)!!,
                    PATH._assetsTemplatesFaviconIco
                )
                return true
            }
            return false
        }
        val path = "/$cleanrpath"
        if (!info.name.lowercase(Locale.ROOT).endsWith(".html") && request.getParam(Param.view) != null) {
            if (!actionView(response, request.getParam(Param.mime), info, path)) {
                notfound(response, path)
            }
            return true
        }
        if (isResourceRequest(request, response, info, path)) {
            return true
        }
        if (cleanrpath.startsWith(PATH.assetsTemplatesRes_)
            && (csseditorHtml(response, path)
                    )
        ) {
            return true
        }
        if (path.startsWith(PATH._assets_)
            && (errorPage404(request, response, path)
                    || errorPage500(request, response, path))
        ) {
            return true
        }
        return htmlResponse(response, info)
    }

    @Throws(Exception::class)
    protected fun actionView(response: ICpluseditionResponse, mime: String?, info: IFileInfo, path: String): Boolean {
        if (!info.exists) return false
        val mime1 = mime
            ?: MimeUtil.mimeFromPath(info.name)
            ?: return false
        if (mime1 == Mime.TXT || mime1 == Mime.JSON || mime1 == Mime.XML) {
            val csspath = "/" + PATH.assetsClientCss + "?t=" + storage.getCustomResourcesTimestamp()
            htmlResponse(response, Templates.TextTemplate().build(csspath, info.content().readText()))
            return true
        }
        if (mime1.startsWith("image/")) {
            val csspath = PATH._assetsImageCss + "?t=" + storage.getCustomResourcesTimestamp()
            htmlResponse(response, Templates.ImageTemplate().build(csspath, info.name))
            return true
        }
        return false
    }

    override fun <R> recentsAction(callback: Fun11<IRecentsHandler, R>): R {
        return callback(recentsHandler)
    }

    override fun <R> filepickerAction(callback: Fun11<IFilepickerHandler, R>): R {
        return callback(filepicker)
    }

    private fun errorPage500(request: ICpluseditionRequest, response: ICpluseditionResponse, path: String): Boolean {
        if (PATH._assetsTemplates500Html == path) {
            var opath = request.getParam(Param.path) ?: ""
            var msg = request.getParam(Param.msg) ?: "ERROR"
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
        if (PATH._assetsTemplates404Html == path) {
            var opath = request.getParam(Param.path) ?: ""
            if (opath.isNotEmpty()) {
                opath = try {
                    URLDecoder.decode(opath, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    context.e("# Should not happen", e)
                    ""
                }
            }
            var writable = false
            if (opath.startsWith("/") /* && filepicker != null *//* filepickerHandler may be null in testing only */) {
                var dir = opath
                while (true) {
                    dir = Basepath.dir(dir) ?: break
                    val stat = storage.fileInfoAt(dir).result()?.stat()
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

    private fun csseditorHtml(response: ICpluseditionResponse, path: String): Boolean {
        if (PATH._assetsCSSEditorHtml == path) {
            htmlResponse(response, Templates.CSSEditorTemplate().build(storage))
            return true
        }
        return false
    }

    protected fun actionHome(response: ICpluseditionResponse, docpath: String) {
        val ret = actionGetSettings(docpath)
        val settings = """
if(self!==top){self.location.href='${PATH._assetsTemplates404Html}';}
window.AnSettings=${ret}
"""
        try {
            response.setupHtmlResponse()
            val content = MyByteOutputStream()
            content.writer().use {
                Templates.HomeTemplate().serialize(it, storage, storage.rsrc.get(R.string.PleaseWait), settings)
            }
            response.setData(content.inputStream())
        } catch (e: Throwable) {
            Support.e("ERROR: homeResponse()", e)
        }
    }

    ////////////////////////////////////////////////////////////
    ///// Setting actions

    @Throws(JSONException::class)
    protected fun actionGetSettings(docpath: String?): JSONObject {
        return storage.getSettingsStore().invoke {
            val ret = JSONObject()
            ret.put(Key.result, it.getSettings())
            ret.put(Key.status, it.getPreferences())
            ret.put(Key.supportwebp, mediaUtil.supportExtraDesktopImageFormats())
            if (docpath != null) {
                val docinfo = this.actionFilepicker(FilepickerCmd.FILEINFO, JSONObject().put(Key.path, docpath))
                ret.put(Key.fileinfo, docinfo.optJSONObject(Key.fileinfo))
            }
            ret
        }
    }

    protected fun actionGetSessionPreferences(): JSONObject {
        return storage.getSettingsStore().invoke {
            JSONObject().put(Key.result, it.getPreferences())
        }
    }

    protected fun actionUpdateSessionPreferences(json: String): JSONObject {
        return Without.throwableOrNull {
            val updates = JSONUtil.jsonObjectOrNull(json)
                ?: return@throwableOrNull rsrc.jsonObjectError(R.string.ParametersInvalid)
            rsrc.jsonObjectError(storage.getSettingsStore().invoke {
                it.updatePreferences(rsrc, updates)
            })
        } ?: rsrc.jsonObjectError(R.string.CommandFailed)
    }

    protected fun actionGetTemplatesInfo(): JSONObject {
        return storage.getSettingsStore().invoke {
            try {
                return@invoke it.getTemplatesJson(JSONObject(), Key.result)
            } catch (e: Exception) {
                return@invoke rsrc.jsonObjectError(e, R.string.CommandFailed)
            }
        }
    }

    protected fun actionGetXrefs(path: String): JSONObject {
        return storage.getSettingsStore().invoke { accessor ->
            try {
                val apath = Basepath.cleanPath(if (!path.startsWith(FS)) "$FS$path" else path)
                val refs = accessor.getXrefsFrom(apath)?.let { JSONArray(it) }
                val refsby = accessor.getXrefsTo(apath)?.let { JSONArray(it) }
                JSONObject()
                    .put(Key.result, JSONArray().put(refs).put(refsby))
                    .put(Key.status, accessor.hasXrefs())
            } catch (e: Throwable) {
                rsrc.jsonObjectError(e, R.string.CommandFailed)
            }
        }
    }

    ////////////////////////////////////////////////////////////
    ///// System actions

    ////////////////////////////////////////////////////////////
    ///// Browser actions

    protected fun actionQuitFromClient(): JSONObject {
        storage.onPause()
        return JSONObject()
    }

    /**
     * @param json {
     * Key.tag: String,
     * Key.attrs: String,
     * } where Key.url is an encoded absolute URI or an baseurl relative URI.
     * @return ret {
     * Key.errors: Object,
     * Key.tag: String,
     * Key.attrs: {String: String},
     * } where Key.url is an absolute encoded URI.
     * @throws Exception On error.
     */
    @Throws(Exception::class)
    protected fun actionSanitize(json: String): JSONObject {
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
        return ret
    }

    /**
     * Esc. unicode characters to quoted "\xxxx" format.
     */
    private fun escCSSValue(key: String, value: String): String {
        var start = 0
        val end = value.length
        var isquoted = false
        if (end > 2) {
            val c = value[0].code
            isquoted = (c == 0x22 || c == 0x27) && c == value[end - 1].code
        }
        if ("content" != key && !isquoted) {
            return value
        }
        var ret: StringBuilder? = null
        for (i in 0 until end) {
            val c = value[i].code
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

    ////////////////////////////////////////////////////////////
    ///// Backup actions

    protected fun actionBackupData(
        st: IStorageAccessor,
        backuppath: String,
        aliases: List<String>,
        srcdirpath: String
    ): JSONObject {
        val backupfile = storage.fileInfoAt(backuppath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val srcdir = if (srcdirpath.isEmpty()) storage.getHomeRoot() else storage.fileInfoAt(srcdirpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (!srcdir.exists) return rsrc.jsonObjectError(R.string.SourceNotFound)
        if (!srcdir.isDir) return rsrc.jsonObjectError(R.string.SourceExpectingADir)
        return actionBackupData(st, backupfile, aliases, srcdir)
    }

    protected fun actionBackupData(
        st: IStorageAccessor,
        backupfile: IFileInfo,
        aliases: List<String>,
        srcdir: IFileInfo
    ): JSONObject {
        if (backupfile.cpath.startsWith(srcdir.cpath + "/")) {
            return rsrc.jsonObjectError(R.string.BackupFileMustNotUndirSourceDirectory)
        }
        try {
            val result = st.backupData(backupfile, aliases, srcdir)
            val stringid = (if (backupfile.name.endsWith(Suffix.ZIP)) R.string.Export else R.string.Backup)
            return BackupUtil.backupRestoreResult(rsrc, stringid, result)
        } catch (e: Throwable) {
            backupfile.delete()
            return rsrc.jsonObjectError(e, rsrc.actionFailed(R.string.Backup))
        }
    }

    protected fun actionBackupKey(st: IStorageAccessor, keypath: String): JSONObject {
        val keyfile = storage.fileInfoAt(keypath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        try {
            st.backupKey(keyfile)
            return rsrc.jsonObjectResult(rsrc.actionOK(R.string.BackupKey))
        } catch (e: Throwable) {
            keyfile.delete()
            return rsrc.jsonObjectError(e, rsrc.actionFailed(R.string.BackupKey))
        }
    }

    protected fun actionRestoreData(
        st: IStorageAccessor,
        backuppath: String,
        sync: Boolean,
        from: String,
        destdir: String
    ): JSONObject {
        val backupfile = storage.fileInfoAt(backuppath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val lcsuffix = Basepath.lcSuffix(backupfile.name)
        if (lcsuffix == Suffix.ZIP) {
            return actionUnzip(backuppath, destdir, from)
        }
        if (lcsuffix != Suffix.IBACKUP && lcsuffix != Suffix.BACKUP) throw IOException()
        val destdirinfo = storage.fileInfoAt(destdir).let {
            it.result() ?: throw IOException()
        }
        if (!destdirinfo.root.stat().writable || !destdirinfo.mkdirs() || destdirinfo.stat()?.writable != true)
            return rsrc.jsonObjectError(R.string.DestinationNotWritable_, destdir)
        try {
            val result = st.restoreData(destdirinfo, backupfile, from, sync)
            val ret = BackupUtil.backupRestoreResult(rsrc, R.string.Restore, result)
            this.filepicker.listDir(ret, destdir)
            return ret
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, rsrc.actionFailed(R.string.Restore))
        } finally {
            val xrefs = XrefUt.buildXrefs(destdirinfo)
            storage.getSettingsStore().invoke {
                it.deleteXrefsFrom(destdirinfo)
                it.updateXrefs(xrefs)
            }
        }
    }

    protected fun actionVerifyBackup(st: IStorageAccessor, backuppath: String): JSONObject {
        val backupfile = storage.fileInfoAt(backuppath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        backupfile.stat()
            ?: return rsrc.jsonObjectNotFound(backuppath)
        val result = BackupUtil.actionVerifyBackup1(st, backupfile)
        return BackupUtil.backupRestoreResult(rsrc, R.string.BackupVerify, result)
    }

    protected fun actionBackupConversion(
        st: IStorageAccessor,
        dstpath: String,
        srcpath: String,
        aliases: List<String>,
        cut: Boolean
    ): JSONObject {
        return BackupUtil.actionBackupConversion0(
            storage, st, dstpath, srcpath, aliases.toTypedArray(), cut
        ).onResult({ it }, { result ->
            val msg = BackupUtil.backupRestoreResult(rsrc, R.string.Backup, result)
            val ret = rsrc.jsonObjectResult(msg)
            this.filepicker.listDir(ret, Basepath.dir(dstpath) ?: "")
            ret
        })
    }

    protected fun actionForwardBackup(st: IStorageAccessor, backuppath: String, aliases: List<String>): JSONObject {
        return storage.fileInfoAt(backuppath).onResult({
            rsrc.jsonObjectError(it)
        }, {
            val lcsuffix = Basepath.lcSuffix(it.name)
            if (lcsuffix != Suffix.IBACKUP && lcsuffix != Suffix.BACKUP)
                return@onResult rsrc.jsonObjectError(rsrc.actionFailed(R.string.BackupForward))
            st.forwardBackup(it, aliases)
        })
    }

    protected fun actionBackupFileInfo(st: IStorageAccessor, cpath: String): JSONObject {
        return storage.fileInfoAt(cpath).onResult({
            rsrc.jsonObjectError(it)
        }, {
            st.readBackupFileInfo(it)
        })
    }

    protected fun actionReadBackupFiletree(st: IStorageAccessor, backuppath: String): JSONObject {
        try {
            val backupfile = StorageBase.existingFileInfoAt(storage, backuppath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            val dirtree = st.readBackupFiletree(backupfile) ?: JSONObject()
            return JSONObject().put(Key.result, dirtree)
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, R.string.ErrorReadingBackup)
        }
    }

    protected fun actionGetBackupKeyAliases(st: IStorageAccessor): JSONObject {
        return Without.exceptionOrNull {
            val aliases = st.secAction { sec ->
                sec.getBackupPublicKeys().mapValues { sec.descOf(it.key, it.value) }
            }
            JSONObject().put(Key.result, JSONObject(aliases))
        } ?: rsrc.jsonObjectError(R.string.CommandFailed)
    }

    protected fun actionImportBackupKey(st: IStorageAccessor, alias: String, cpath: String): JSONObject {
        return BackupUtil.importBackupKey(storage, st, barcodeUtil, alias, cpath)
    }

    protected fun actionExportBackupKey(st: IStorageAccessor, alias: String, cpath: String): JSONObject {
        return BackupUtil.exportBackupKey(storage, st, barcodeUtil, alias, cpath)
    }

    protected fun actionDeleteBackupKey(st: IStorageAccessor, alias: String): JSONObject {
        return Without.exceptionOrNull {
            val aliases = st.secAction { sec ->
                sec.deleteAlias(alias)
                sec.getBackupPublicKeys().mapValues {
                    sec.descOf(it.key, it.value)
                }
            }
            JSONObject().put(Key.result, JSONObject(aliases))
        } ?: storage.rsrc.jsonObjectError(R.string.CommandFailed)
    }

    ////////////////////////////////////////////////////////////
    ///// File actions

    protected fun actionFilepicker(cmd: Int, params: JSONObject): JSONObject {
        return try {
            filepicker.handle(cmd, params)
        } catch (e: Throwable) {
            rsrc.jsonObjectError(e, R.string.CommandFailed, ": filepicker: " + FilepickerCmdUtil.toString(cmd))
        }
    }

    protected fun actionLinkVerifier(cmd: Int, data: JSONObject): JSONObject {
        return try {
            linkVerifier.handle(cmd, data)
        } catch (e: Throwable) {
            rsrc.jsonObjectError(e, R.string.CommandFailed, ": linkVerifier: ${ILinkVerifier.Cmd.toString(cmd)}")
        }
    }

    protected inner class BlogFinder {
        val dayRegex = Regex("^(\\d\\d)(\\.html)?$")
        fun actionListBlogs(year: Int, month: Int): JSONObject {
            val bloginfo = storage.documentFileInfo(TextUt.format("Home/blog/%04d/%02d", year, month))
                ?: return rsrc.jsonObjectError(R.string.NotFound)
            val ret = JSONObject()
            bloginfo.readDir(ArrayList()).forEach {
                val name = it.name
                val match = dayRegex.matchEntire(name) ?: return@forEach
                val stat = it.stat()
                if (match.groupValues[2].isEmpty() && stat?.isDir == true) {
                    val info = it.fileInfo("$name.html")
                    if (info.exists) ret.put("$name/$name.html", info)
                } else if (match.groupValues[2].isNotEmpty() && stat?.isFile == true) {
                    if (!bloginfo.fileInfo("$name/$name.html").exists) ret.put(it.name, it)
                }
            }
            return JSONObject().put(Key.result, ret)
        }

        fun actionFindBlog(next: Boolean, year: Int, month: Int, day: Int): JSONObject {
            try {
                var mm: Int? = month
                var dd: Int? = day
                for (yinfo in sortedyear(year, next)) {
                    for (minfo in sortedmonth(yinfo, mm, next)) {
                        for (dinfo in sortedday(minfo, dd, next)) {
                            return JSONObject().put(Key.result, dinfo.cpath)
                                .put(Key.fileinfo, dinfo)
                        }
                        dd = null
                    }
                    mm = null
                }
            } catch (e: Exception) {
            }
            return rsrc.jsonObjectError(if (next) R.string.BlogNextNotFound else R.string.BlogPrevNotFound)
        }

        private fun sortedyear(year: Int, next: Boolean): List<IFileInfo> {
            val regex = Regex("^\\d\\d\\d\\d$")
            val bloginfo = storage.documentFileInfo("Home/blog") ?: return listOf()
            val sorted = bloginfo.readDir(ArrayList()).filter {
                val name = it.name
                if (!name.matches(regex) || it.stat()?.isDir != true) return@filter false
                val n = TextUt.parseInt(name)
                n != null && (if (next) n >= year else n <= year)
            }.sortedBy { it.name }
            return if (next) sorted else sorted.reversed()
        }

        private fun sortedmonth(yinfo: IFileInfo, month: Int?, next: Boolean): List<IFileInfo> {
            val regex = Regex("^\\d\\d$")
            val sorted = yinfo.readDir(ArrayList()).filter {
                if (!regex.matches(it.name) || it.stat()?.isDir != true) return@filter false
                if (month == null) return@filter true
                val n = TextUt.parseInt(it.name)
                n != null && (if (next) n >= month else n <= month)
            }.sortedBy { it.name }
            return if (next) sorted else sorted.reversed()
        }

        private fun sortedday(minfo: IFileInfo, day: Int?, next: Boolean): List<IFileInfo> {
            val sorted = minfo.readDir(ArrayList()).map {
                val name = it.name
                val match = dayRegex.matchEntire(name) ?: return@map null
                if (day != null) {
                    val n = TextUt.parseInt(match.groupValues[1])
                    val keep = n != null && (if (next) n > day else n < day)
                    if (!keep) return@map null
                }
                val stat = it.stat()
                if (match.groupValues[2].isEmpty() && stat?.isDir == true) {
                    val info = it.fileInfo("$name.html")
                    if (info.exists) return@map info
                } else if (match.groupValues[2].isNotEmpty() && stat?.isFile == true) {
                    val n = match.groupValues[1]
                    return@map if (minfo.fileInfo("$n/$n.html").exists) null else it
                }
                null
            }.filterNotNull().sortedBy { it.name }
            return if (next) sorted else sorted.reversed()
        }
    }

    protected fun actionReadCSS(cpath: String): JSONObject {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (Basepath.lcSuffix(fileinfo.name) != Suffix.CSS)
            return rsrc.jsonObjectError(R.string.InvalidPath)
        if (!fileinfo.exists)
            return rsrc.jsonObjectError(R.string.FileNotFound)
        val position = storage.getSettingsStore().invoke { it.getFilePosition(cpath) }
        return try {
            val content = fileinfo.content().readText()
            return JSONObject().put(Key.result, content).put(Key.status, position)
        } catch (e: Throwable) {
            rsrc.jsonObjectError(R.string.ReadFailed)
        }
    }

    protected fun actionSaveCSS(cpath: String, content: String, infos: JSONObject?): JSONObject {
        val lcsuffix = Basepath.from(cpath).lcSuffix
        if (Suffix.CSS != lcsuffix) {
            return rsrc.jsonObjectError(R.string.InvalidPath)
        }
        infos?.optJSONArray(XrefKey.POSITION)?.let { position ->
            storage.getSettingsStore().invoke { it.updateFilePosition(cpath, position) }
        }
        return savefile(cpath, content, infos?.optJSONObject(XrefKey.LINKS))
    }

    protected fun actionSaveHtml(cpath: String, content: String, infos: JSONObject?): JSONObject {
        val lcsuffix = Basepath.from(cpath).lcSuffix
        if (Suffix.HTML != lcsuffix) {
            return rsrc.jsonObjectError(R.string.InvalidPath)
        }
        return savefile(cpath, content, infos?.optJSONObject(XrefKey.LINKS))
    }

    private fun savefile(cpath: String, content: String, info: JSONObject?): JSONObject {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (!fileinfo.root.stat().writable) return rsrc.jsonObjectError(R.string.DestinationNotWritable_, cpath)
        return try {
            fileinfo.content().write(content.byteInputStream(), null)
            info?.optJSONObject(XrefKey.LINKS)?.let { xrefs ->
                storage.getSettingsStore().invoke {
                    it.updateXrefs(fileinfo.apath, xrefs.keyList())
                }
            }
            JSONObject()
        } catch (e: Exception) {
            rsrc.jsonObjectError(R.string.WriteFailed)
        }
    }

    protected fun actionSaveRecovery(cpath: String, content: String): JSONObject {
        try {
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.result() ?: return rsrc.jsonObjectError(R.string.InvalidPath)
            }
            content.byteInputStream().use {
                fileinfo.content().writeRecovery(it)
            }
            return JSONObject()
        } catch (e: Throwable) {
            
            return rsrc.jsonObjectWarning(R.string.RecoveryFileSaveFailed)
        }
    }

    ////////////////////////////////////////////////////////////
    ///// Files panel actions

    protected fun actionHistoryFilepicker(cmd: Int, params: JSONObject): JSONObject {
        return try {
            historyFilepicker.handle(cmd, params)
        } catch (e: Throwable) {
            rsrc.jsonObjectError(e, R.string.CommandFailed, ": historyFilepicker: " + FilepickerCmdUtil.toString(cmd))
        }
    }

    protected fun actionGenerateGallery(
        params: GalleryParams,
        islandscape: Fun31<String, IInputStreamProvider, Int, Boolean?>,
        done: Fun10<JSONObject>,
    ) {
        val tncallback = { fileinfo: IFileInfo, width: Int, height: Int, quality: Int ->
            Without.throwableOrNull {
                if (isVideoLcSuffix(fileinfo.lcSuffix)) {
                    mediaUtil.videoPoster(fileinfo, 10.0, width, height, quality).first
                } else {
                    mediaUtil.readFileAsJpegDataUrl(
                        storage,
                        ImageOutputInfo.tn(width, height, quality),
                        null,
                        fileinfo
                    ).result()?.first
                }
            }
        }
        GalleryGenerator(storage, params, islandscape, tncallback).run(done)
    }

    protected fun actionImageConversion(
        srcdirpath: String,
        rpaths: List<String>?,
        outinfo: ImageOutputInfo?,
        cut: Boolean
    ): JSONObject {
        if (outinfo == null || rpaths == null)
            return rsrc.jsonObjectError(R.string.ParametersInvalid)
        return storage.fileInfoAt(srcdirpath).onResult({
            rsrc.jsonObjectError(it.bot.joinln())
        }, { srcdir ->
            val ret = ImageOrPdfConverter(storage, mediaUtil, outinfo, srcdir, rpaths, cut).run()
            filepickerAction {
                it.listDir(ret, Basepath.dir(outinfo.path) ?: "")
            }
            ret
        })
    }

    protected fun actionFsck(cpath: String): JSONObject {
        return storage.fileInfoAt(cpath).onResult({
            rsrc.jsonObjectInvalidPath(cpath)
        }, {
            val result = it.root.fsck(it.rpath)
            JSONObject().put(Key.result, JSONArray().put(result.first).put(result.second))
                .put(Key.errors, JSONArray(result.third))
        })
    }

    protected fun actionUnzip(zippath: String, dstdirpath: String, srcpath: String): JSONObject {
        val dir = storage.fileInfoAt(dstdirpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val zipfile = storage.fileInfoAt(zippath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (!dir.mkdirs() || dir.stat()?.writable != true)
            return rsrc.jsonObjectError(R.string.DestinationNotWritable_, dstdirpath)
        if (!zipfile.exists) return rsrc.jsonObjectError(R.string.FileNotFound)
        val result = BackupUtil.unzip(zipfile, srcpath, dir)
        val ret = BackupUtil.backupRestoreResult(rsrc, R.string.Unzip, result)
        this.filepicker.listDir(ret, dstdirpath)
        return ret
    }

    protected fun actionVerifyZip(zippath: String): JSONObject {
        val zipfile = storage.fileInfoAt(zippath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        return actionVerifyZip1(zipfile)
    }

    protected fun actionVerifyZip1(zipfile: IFileInfo): JSONObject {
        if (!zipfile.exists) return rsrc.jsonObjectError(R.string.FileNotFound)
        val result = BackupUtil.verifyZip(zipfile)
        if (result.fails.size > 0) {
            val fails = result.fails.toMutableList().add(0, rsrc.actionFailed(R.string.BackupVerify))
            return JSONObject().put(Key.errors, JSONArray(fails))
        }
        return BackupUtil.backupRestoreResult(rsrc, R.string.BackupVerify, result)
    }

    protected fun actionZip(zippath: String, srcdirpath: String): JSONObject {
        val zipfile = storage.fileInfoAt(zippath).let { it.result() ?: return rsrc.jsonObjectError(it.failure()!!) }
        val srcdir = storage.fileInfoAt(srcdirpath).let { it.result() ?: return rsrc.jsonObjectError(it.failure()!!) }
        if (zipfile.cpath.startsWith(srcdir.cpath + "/")) return rsrc.jsonObjectError(R.string.BackupFileMustNotUndirSourceDirectory)
        return srcdir.root.transaction {
            try {
                val result = BackupUtil.writeZip(zipfile, srcdir, false)
                val ret = BackupUtil.backupRestoreResult(rsrc, R.string.Zip, result)
                filepicker.listDir(ret, Basepath.dir(zippath) ?: "")
                ret
            } catch (e: java.lang.Exception) {
                return@transaction rsrc.jsonObjectError(R.string.CommandFailed)
            }
        }
    }

    @Throws(IOException::class, JSONException::class)
    protected fun actionToggleNoBackup(cpath: String): JSONObject {
        val dirinfo = StorageBase.getWritableDirectory(storage, cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val nobackuppath = dirinfo.apath + File.separatorChar + DEF.nobackup
        val nobackup = dirinfo.fileInfo(DEF.nobackup)
        val exists = nobackup.exists
        if (exists) {
            if (!nobackup.delete(true)) {
                return rsrc.jsonObjectError(R.string.ErrorDeleting_, nobackuppath)
            }
        } else {
            try {
                nobackup.content().write(byteArrayOf().inputStream())
            } catch (e: Exception) {
                return rsrc.jsonObjectError(R.string.ErrorCreatingFile_, nobackuppath)
            }
        }
        return filepicker.listDir(JSONObject(), cpath)
            .put(Key.status, !exists)
    }

    /**
     * @param templatecontent The template file content.
     * @param outpath         The path to the destination file, must starts with /Documents/.
     * @return {Key.errors: errors}
     * @throws Exception On error.
     */
    @Throws(Exception::class)
    protected fun actionTemplate(templatecontent: String, outpath: String): JSONObject {
        val basepath = Basepath.from(outpath)
        if (".html" != basepath.suffix) return rsrc.jsonObjectError(R.string.ExpectingHtml)
        val outinfo = storage.fileInfoAt(outpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (!outinfo.mkparent()) {
            return rsrc.jsonObjectError(R.string.CreateParentDirectoryFailed_, outpath)
        }
        outinfo.content().write(templatecontent.byteInputStream())
        return JSONObject()
    }

    protected fun actionScanBarcode(cpath: String, cropinfo: ImageCropInfo?): JSONObject {
        return Without.exceptionOrNull {
            if (cpath.startsWith("data:")) {
                val (blob, mime) = ImageUtil.blobFromDataUrl(cpath)
                    ?: return@exceptionOrNull rsrc.jsonObjectError(R.string.InvalidDataUrl)
                blob.inputStream().use {
                    barcodeUtil.detectBarcode(it, mime, cropinfo)
                }
            } else {
                val fileinfo = storage.fileInfoAt(cpath).let {
                    it.result() ?: return@exceptionOrNull rsrc.jsonObjectError(it.failure()!!)
                }
                val mime = MimeUtil.imageMimeFromPath(fileinfo.name)
                    ?: return@exceptionOrNull rsrc.jsonObjectError(R.string.UnsupportedInputFormat_, fileinfo.name)
                fileinfo.content().inputStream().use {
                    barcodeUtil.detectBarcode(it, mime, cropinfo)
                }
            }
        } ?: return rsrc.jsonObjectError(R.string.BarcodeNotFound)
    }

    protected fun actionGenerateBarcode(type: String, scale: Int, text: String): JSONObject {
        return Without.exceptionOrNull {
            val lcsuffix = ".${type.lowercase()}"
            if (lcsuffix != Suffix.PNG)
                return@exceptionOrNull rsrc.jsonObjectError(R.string.UnsupportedOutputFormat_, type)
            barcodeUtil.generateQRCode(text, Mime.PNG, scale)?.let {
                JSONObject().put(Key.result, ImageUtil.toDataUrl(Mime.PNG, it))
            }
        } ?: rsrc.jsonObjectError(R.string.BarcodeGenerateFailed)
    }

    protected fun actionFindFiles(fromdir: String?, pattern: String?): JSONObject {
        if (fromdir == null || pattern == null) return rsrc.jsonObjectError(R.string.InvalidArguments)
        try {
            val paths = storage.submit {
                it.find(TreeSet(), fromdir, pattern)
            }.get()
            return JSONObject().put(Key.result, JSONArray(paths))
        } catch (e: Exception) {
            return rsrc.jsonObjectError(e, R.string.CommandFailed, fromdir)
        }
    }

    protected fun actionCleanupTrash(cpath: String): JSONObject {
        val result = if (cpath.isEmpty() || cpath == FS) {
            storage.getHomeRoot().cleanupTrash(FileInfoUtil::defaultCleanupTrashPredicate)
        } else {
            val fileinfo = StorageBase.getExistingDocumentDirectory(storage, cpath).let {
                it.result() ?: return storage.rsrc.jsonObjectError(it.failure()!!)
            }
            fileinfo.root.cleanupTrash(FileInfoUtil::defaultCleanupTrashPredicate)
        }
        return HistoryFilepickerHandler.cleanupTrashResult(rsrc, R.string.CleanupTrash, result)
    }

    protected fun actionCreateFromTemplate(
        tpath: String,
        outpath: String,
    ): JSONObject {
        val cleanrpath = if (tpath.contains(FS)) {
            Support.getcleanrpath(tpath)
        } else {
            storage.getSettingsStore().invoke {
                it.getTemplateInfo(tpath)?.let { info ->
                    arrayOf(PATH.assets, "templates", info.cat.lowercase(), info.filename).bot.joinPath()
                }
            } ?: return rsrc.jsonObjectError(R.string.TemplateNotFound_, tpath)
        }
        val tinfo = storage.fileInfo(cleanrpath)
            ?: return rsrc.jsonObjectError(R.string.TemplateNotFound_, tpath)
        val templatecontent = tinfo.content().readText()
        return this.actionTemplate(templatecontent, outpath)
    }

    ////////////////////////////////////////////////////////////
    ///// Recents panel actions

    ////////////////////////////////////////////////////////////
    ///// Search panel actions

    protected fun actionGlobalSearch(
        filterignorecase: Boolean,
        filefilter: String,
        searchignorecase: Boolean,
        searchtext: String /* , final boolean isregex */
    ): JSONObject {
        val id = storage.postSearch(filterignorecase, filefilter, searchignorecase, searchtext /* , isregex */)
        return JSONObject().putOrFail(Key.result, id)
    }

    ////////////////////////////////////////////////////////////
    ///// Image actions

    @Throws(JSONException::class)
    protected fun actionPhotoLibraryThumbnails(storage: IStorage, json: String): JSONObject {
        val a = JSONObject(json)
        val infos = a.optJSONArray(Key.infos) ?: return rsrc.jsonObjectError(R.string.NoImageInfoAvailable)
        val tsize = _getthumbnailsize(a)
        return ImageUtil.actionImageThumbnails(storage, infos, tsize, thumbnailcallback)
    }

    private fun _getthumbnailsize(params: JSONObject): Int {
        val size = params.optInt(Key.size)
        if (size > 0) return size
        return if (params.optInt(Key.type, MediaInfo.Thumbnail.Kind.MICRO_KIND) == MediaInfo.Thumbnail.Kind.MINI_KIND) {
            MediaInfo.Thumbnail.Mini.WIDTH
        } else MediaInfo.Thumbnail.Micro.WIDTH
    }

    @Throws(Exception::class)
    protected fun actionViewPhotoLibraryThumbnail(storage: IStorage, size: Int, imageinfo: JSONObject): JSONObject {
        val cpath = imageinfo.stringOrNull(MediaInfo.Uri) ?: return rsrc.jsonObjectError(R.string.PleaseSpecifyAnImageId)
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        return Without.exceptionOrNull {
            mediaUtil.readFileAsJpegDataUrl(
                storage, ImageOutputInfo.tn(size, size, DEF.jpegQualityThumbnail), null, fileinfo
            ).result()?.let {
                rsrc.jsonObjectResult(it.first)
            }
        } ?: rsrc.jsonObjectError(R.string.ImageReadFailed_, cpath)
    }

    protected fun actionSaveBase64Image(cpath: String, data: String): JSONObject {
        try {
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            val mime = MimeUtil.imageMimeFromPath(fileinfo.name)
            val (blob) = ImageUtil.blobFromDataUrl(data, mime)
                ?: return rsrc.jsonObjectError(R.string.InvalidDataUrl)
            val input = if (Mime.PNG == mime) {
                mediaUtil.rewritePNG(rsrc, blob).inputStream()
            } else blob.inputStream()
            input.use { fileinfo.content().write(it) }
            return JSONObject()
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, R.string.ImageWriteFailed)
        }
    }

    protected fun actionWriteImage(
        frompath: String,
        outinfo: ImageOutputInfo,
        cropinfo: ImageCropInfo?
    ): JSONObject {
        return mediaUtil.writeImage(storage, outinfo, cropinfo, frompath)
    }

    protected fun actionPreviewImage(
        outinfo: ImageOutputInfo,
    ): JSONObject {
        return mediaUtil.readUrlAsJpegDataUrl(storage, outinfo, outinfo.path).onResult({ it }, {
            rsrc.jsonObjectResult(it.first)
        })
    }

    private fun localImageThumbnail(
        fileinfo: IFileInfo,
        tnsize: Int,
        quality: Int,
        crop: ImageCropInfo?
    ): IBotResult<Triple<String, Dim, Dim>, JSONObject> {
        return mediaUtil.readFileAsJpegDataUrl(storage, ImageOutputInfo.tn(tnsize, tnsize, quality), crop, fileinfo)
    }

    /**
     * @return { Key.result: dataurl, Key.errors: errors }
     */
    protected fun actionLocalImageThumbnail(cpath: String, tnsize: Int, quality: Int, crop: ImageCropInfo?): JSONObject {
        try {
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            return localImageThumbnail(fileinfo, tnsize, quality, crop).onResult({ it }, {
                val imageinfo = ImageUtil.localImageInfo(
                    fileinfo,
                    it.second.x,
                    it.second.y,
                    ImageUtil.mimeOfDataUrl(it.first),
                    null
                )
                JSONObject()
                    .put(Key.result, it.first)
                    .put(Key.imageinfo, imageinfo)
                    .put(Key.width, it.third.x)
                    .put(Key.height, it.third.y)
            })
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, R.string.CreateThumbnailFailed_, cpath)
        }
    }

    protected fun actionPdfPoster(cpath: String, page: Int): JSONObject {
        return storage.fileInfoAt(cpath).onResult({
            rsrc.jsonObjectError(it)
        }, { fileinfo ->
            val url = mediaUtil.pdfPoster(
                storage, fileinfo, page,
                DEF.previewPhotoSize, DEF.previewPhotoSize, DEF.jpegQuality
            )
            JSONObject().put(Key.result, url)
        })
    }

    /**
     * @param cpath Context relative file path.
     * @return {Key.result : {Image info with image dimension information}}.
     */
    protected fun actionLocalImageInfo(cpath: String, withtn: Boolean): JSONObject {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val filestat = fileinfo.stat() ?: return rsrc.jsonObjectError(R.string.ImageNotFound_, cpath)
        val lcsuffix = Basepath.lcSuffix(fileinfo.name)
        if (lcsuffix == Suffix.PDF) return localPdfInfo(cpath, fileinfo, withtn, filestat)
        return localImageInfo(lcsuffix, cpath, fileinfo, withtn, filestat)
    }

    private fun localPdfInfo(
        cpath: String,
        fileinfo: IFileInfo,
        withtn: Boolean,
        filestat: IFileStat
    ): JSONObject {
        return try {
            val (dataurl, dim) = mediaUtil.pdfPoster(
                storage, fileinfo, 0,
                DEF.previewPhotoSize, DEF.previewPhotoSize, DEF.jpegQuality
            )
            return localImageInfo1(fileinfo, dim, Mime.PDF, filestat, if (withtn) dataurl else null)
        } catch (e: Exception) {
            rsrc.jsonObjectError(e, R.string.ErrorReading_, cpath)
        }
    }

    private fun localImageInfo(
        lcsuffix: String,
        cpath: String,
        fileinfo: IFileInfo,
        withtn: Boolean,
        filestat: IFileStat
    ): JSONObject {
        val mime = MimeUtil.imageMimeFromLcSuffix(lcsuffix) ?: return rsrc.jsonObjectError(R.string.InvalidImagePath_, cpath)
        return try {
            val dim = fileinfo.content().inputStream().use { input ->
                mediaUtil.getImageDim(fileinfo.name, input)
            }
            val tndataurl = if (withtn) {
                val result = localImageThumbnail(fileinfo, DEF.previewPhotoSize, DEF.jpegQualityThumbnail, null)
                result.failure()?.let { return it }
                result.result()?.first
            } else null
            return localImageInfo1(fileinfo, dim, mime, filestat, tndataurl)
        } catch (e: Exception) {
            rsrc.jsonObjectError(e, R.string.ImageReadFailed_, cpath)
        }
    }

    private fun localImageInfo1(
        fileinfo: IFileInfo,
        dim: Dim,
        mime: String,
        filestat: IFileStat,
        tndataurl: String?
    ): JSONObject {
        return JSONObject().put(
            Key.result, ImageUtil.localImageInfo(
                fileinfo.apath,
                fileinfo.name,
                dim.x,
                dim.y,
                mime,
                filestat.lastModified,
                filestat.length,
                tndataurl
            )
        )
    }

    @Throws(JSONException::class)
    protected fun actionExternalImageInfo(): JSONObject {
        return actionImageInfos(storage.fileInfo(PATH.assetsImages_)!!)
    }

    @Throws(JSONException::class)
    protected fun actionImageInfos(imagesdir: IFileInfo): JSONObject {
        val ret = JSONObject()
        val result = JSONArray()
        ret.put(Key.result, result)
        imagesdir.walk2 { file, stat ->
            if (!stat.isFile) return@walk2
            val mime = MimeUtil.imageMimeFromPath(file.name) ?: return@walk2
            try {
                val dim = file.content().inputStream().use {
                    mediaUtil.getImageDim(file.name, it)
                }
                result.put(
                    ImageUtil.localImageInfo(
                        file.apath,
                        file.name,
                        dim.x,
                        dim.y,
                        mime,
                        stat.lastModified,
                        stat.length,
                        null
                    )
                )
            } catch (e: Exception) {
                
            }
        }
        return ret
    }

    ////////////////////////////////////////////////////////////
    ///// Video actions

    protected fun actionVideoInfo(params: JSONArray): JSONObject {
        try {
            val result = JSONObject()
            for (index in 0 until params.length()) {
                val request = params.getJSONObject(index)
                val cpath = request.stringOrNull(Key.path)
                if (cpath.isNullOrEmpty()) {
                    continue
                }
                val info = result.putJSONObjectOrFail(cpath)
                val fileinfo = storage.fileInfoAt(cpath).result()
                if (fileinfo == null) {
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.InvalidPath))
                    continue
                }
                val stat = fileinfo.stat()
                if (stat == null) {
                    info.put(MediaInfo.FileExists, false)
                    continue
                }
                if (!stat.readable || !stat.isFile) {
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.InvalidPath))
                    continue
                }
                val timestamp = request.optLong(Key.time, 0)
                val lastmodified = stat.lastModified
                if (lastmodified == timestamp) {
                    continue
                }
                try {
                    info.put(MediaInfo.Playable, true)
                    info.put(MediaInfo.FileExists, true)
                    info.put(MediaInfo.FileDate, lastmodified)
                    info.put(MediaInfo.FileSize, stat.length)
                    mediaUtil.videoInfo(info, fileinfo)
                } catch (e: Throwable) {
                    
                    info.put(MediaInfo.Playable, false)
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.Error))
                }
            }
            return JSONObject().put(Key.result, result)
        } catch (e: Throwable) {
            
            return storage.rsrc.jsonObjectResult(R.string.CommandFailed)
        }
    }

    protected fun actionVideoPoster(cpath: String, time: Double, w: Int, h: Int, quality: Int): JSONObject {
        try {
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            val ret = mediaUtil.videoPoster(fileinfo, time, w, h, quality)
            return rsrc.jsonObjectResult(ret.first)
        } catch (e: Throwable) {
            
            return storage.rsrc.jsonObjectResult(R.string.CommandFailed)
        }
    }

////////////////////////////////////////////////////////////
///// Audio actions

    protected fun actionAudioInfo(params: JSONArray): JSONObject {
        try {
            val result = JSONObject()
            for (index in 0 until params.length()) {
                val request = params.getJSONObject(index)
                val cpath = request.stringOrNull(Key.path) ?: continue
                val info = result.putJSONObjectOrFail(cpath)
                val fileinfo = storage.fileInfoAt(cpath).result()
                if (fileinfo == null) {
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.InvalidPath))
                    continue
                }
                val stat = fileinfo.stat()
                if (stat == null) {
                    info.put(MediaInfo.FileExists, false)
                    continue
                }
                if (!stat.readable || !stat.isFile) {
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.InvalidPath))
                    continue
                }
                val timestamp = request.optLong(Key.time, 0)
                val lastmodified = stat.lastModified
                if (lastmodified == timestamp) {
                    continue
                }
                try {
                    info.put(MediaInfo.Playable, true)
                    info.put(MediaInfo.FileExists, true)
                    info.put(MediaInfo.FileDate, lastmodified)
                    info.put(MediaInfo.FileSize, stat.length)
                    mediaUtil.audioInfo(info, fileinfo)
                } catch (e: Throwable) {
                    
                    info.put(MediaInfo.Playable, false)
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.Error))
                }
            }
            return JSONObject().put(Key.result, result)
        } catch (e: Throwable) {
            
            return storage.rsrc.jsonObjectResult(R.string.CommandFailed)
        }
    }

////////////////////////////////////////////////////////////

    protected fun servererror(response: ICpluseditionResponse, path: String?, e: Throwable?) {
        context.e("CpluseditionRequestHandler: servererror" + if (path == null) "" else ": $path", e)
        response.setStatus(HttpStatus.InternalServerError)
    }

    protected fun badrequest(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: backrequest: $path")
        response.setStatus(HttpStatus.BadRequest)
    }

    protected fun notfound(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: notfound: $path")
        response.setStatus(HttpStatus.NotFound)
    }

    protected fun unsatifiableRangeError(response: ICpluseditionResponse, path: String) {
        context.w("# CpluseditionRequestHandler: unsatifiableRangeError: $path")
        response.setStatus(HttpStatus.RequestedRangeNotSatisfiable)
    }

    protected fun illegalArgumentResponse(response: ICpluseditionResponse) {
        jsonResponse(response, rsrc.jsonObjectParametersInvalid())
    }

////////////////////////////////////////////////////////////

    @Throws(JSONException::class)
    private fun parseSpaceSeparateAttributes(errors: MutableList<String>, namevalues: JSONObject, source: String) {
        val sutil = HtmlSpaceUtil.singleton
        val lines = source.lines()
        for (line in lines) {
            val end = line.length
            val start = sutil.skipWhitespaces(line, 0, end)
            if (start == end) {
                continue
            }
            val s = line.substring(start, end)
            val index = s.indexOf(' ')
            var name: String
            var value: String
            if (index < 0) {
                name = XMLUt.unesc(s).replace('\u00a0', ' ').trim { it <= ' ' }.lowercase(Support.LOCALE)
                value = ""
            } else {
                name = XMLUt.unesc(s.substring(0, index)).replace('\u00a0', ' ').trim { it <= ' ' }
                    .lowercase(Support.LOCALE)
                value = XMLUt.unesc(s.substring(index + 1)).replace('\u00a0', ' ')
            }
            if (("style" == name || "class" == name)
                && (value.isEmpty() || value.trim { it <= ' ' }.isEmpty())
            ) {
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

    private fun isResourceRequest(
        request: ICpluseditionRequest,
        response: ICpluseditionResponse,
        info: IFileInfo,
        path: String
    ): Boolean {
        val lcsuffix = Basepath.lcSuffix(info.name)
        if (isJavascriptRequest(response, info, path, lcsuffix)) {
            return true
        }
        if (isCSSRequest(response, info, path, lcsuffix)) {
            return true
        }
        if (fontRequest(response, info, path, lcsuffix)) {
            return true
        }
        if (imageRequest(response, info, path, lcsuffix)) {
            return true
        }
        if (audioRequest(response, request, info, path, lcsuffix)) {
            return true
        }
        if (isPlainTextRequest(path)) {
            resourceResponse(response, Mime.TXT, info, path)
            return true
        }
        if (pdfRequest(response, info, path)) {
            return true
        }
        if (videoRequest(response, request, info, path, lcsuffix)) {
            return true
        }
        return false
    }

    private fun isPlainTextRequest(path: String): Boolean {
        return false
    }

    private fun isJavascriptRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcsuffix: String): Boolean {
        if (Suffix.JS == lcsuffix) {
            resourceResponse(response, Mime.JS, info, path)
            return true
        }
        return false
    }

    private fun fontRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcsuffix: String): Boolean {
        val mime = MimeUtil.fontMimeFromLcSuffix(lcsuffix) ?: return false
        resourceResponse(response, mime, info, path)
        return true
    }

    private fun isCSSRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcsuffix: String): Boolean {
        if (Suffix.CSS == lcsuffix) {
            resourceResponse(response, Mime.CSS, info, path)
            return true
        }
        return false
    }

    private fun imageRequest(response: ICpluseditionResponse, info: IFileInfo, path: String, lcsuffix: String): Boolean {
        val mime = if (lcsuffix == Suffix.SVG) Mime.SVG else MimeUtil.imageMimeFromLcSuffix(lcsuffix) ?: return false
        if (path == "/favicon.ico") {
            resourceResponse(
                response,
                mime,
                storage.fileInfo(PATH._assetsTemplatesFaviconIco)!!,
                PATH._assetsTemplatesFaviconIco
            )
            return true
        }
        if (!info.exists) return false
        resourceResponse(response, mime, info, path)
        return true
    }

    private fun audioRequest(
        response: ICpluseditionResponse,
        request: ICpluseditionRequest,
        info: IFileInfo,
        path: String,
        lcsuffix: String
    ): Boolean {
        if (!info.exists) return false
        val mime = MimeUtil.audioMimeFromLcSuffix(lcsuffix) ?: return false
        mediaResponse(request, response, mime, info, path)
        return true
    }

    private fun videoRequest(
        response: ICpluseditionResponse,
        request: ICpluseditionRequest,
        info: IFileInfo,
        path: String,
        lcsuffix: String
    ): Boolean {
        val mime = MimeUtil.videoMimeFromLcSuffix(lcsuffix) ?: return false
        if (!info.exists) return false
        mediaResponse(request, response, mime, info, path)
        return true
    }

    private fun pdfRequest(response: ICpluseditionResponse, info: IFileInfo, path: String): Boolean {
        val mime = MimeUtil.mimeFromPath(info.name) ?: return false
        if (mime != Mime.PDF) return false
        resourceResponse(response, mime, info, path)
        if (!info.exists) return false
        return true
    }

    private fun htmlResponse(response: ICpluseditionResponse, element: IElement) {
        try {
            response.setupHtmlResponse()
            val data = MyByteOutputStream()
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
    private fun htmlResponse(response: ICpluseditionResponse, fileinfo: IFileInfo): Boolean {
        if (Basepath.lcSuffix(fileinfo.name) != Suffix.HTML) return false
        if (!fileinfo.exists) throw FileNotFoundException(fileinfo.apath)
        htmlResponse(response, fileinfo.content().readBytes())
        return true
    }

    private fun resourceResponse(response: ICpluseditionResponse, mime: String, info: IFileInfo, path: String) {
        try {
            if (mime == Mime.HEIC) {
                storage.submit { it.heicResponse(response, info) }.get()
                return
            }
            response.setContentType(mime)
            storage.submit { it.resourceResponse(response, info) }.get()
        } catch (e: FileNotFoundException) {
            notfound(response, path)
        } catch (e: Throwable) {
            servererror(response, path, e)
        }
    }

    private fun mediaResponse(
        request: ICpluseditionRequest,
        response: ICpluseditionResponse,
        mime: String,
        info: IFileInfo,
        path: String
    ) {
        try {
            response.setContentType(mime)
            response.setHeader(HttpHeader.Connection, "keep-alive")
            response.setHeader(HttpHeader.NoCache, "true")
            response.setHeader(HttpHeader.CacheControl, "no-cache")
            response.setHeader(HttpHeader.KeepAlive, "timeout=20")
            var range: Http.HttpRange? = null
            val value = request.getHeader(HttpHeader.Range)
            if (value != null) {
                range = Http.HttpRange.parse(value, info.content().getContentLength(), Long.MAX_VALUE)
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
            val content = info.content().inputStream()
            storage.submit {
                response.setData(PartialInputStream(content, range.first, size))
            }.get()
        } catch (e: FileNotFoundException) {
            notfound(response, path)
        } catch (e: Throwable) {
            servererror(response, path, e)
        }
    }

    protected fun jsonResponse(response: ICpluseditionResponse, json: JSONObject) {
        try {
            response.setHeader("no-cache", "true")
            response.setContentType("application/json;charset=UTF-8")
            response.setData(json.toString().byteInputStream())
        } catch (e: IOException) {
            Support.e("ERROR: Sending JSON response", e);
            response.setStatus(HttpStatus.InternalServerError)
        }
    }

    companion object {
        private val CSSURLQQ = Pattern.compile("url\\(\\s*\"([^\"]+?)(\")\\s*\\)")
        private val CSSURLQ = Pattern.compile("url\\(\\s*'([^']+?)(')\\s*\\)")
        private val CSSURL = Pattern.compile("url\\(\\s*((?![\"']).*?)\\s*\\)")

        protected
        fun partialResponse(output: OutputStream, input: MySeekableInputStream, start: Long, size: Long) {
            if (size >= Integer.MAX_VALUE.toLong()) throw IOException()
            input.copy(start, size.toInt()) { b, off, n ->
                output.write(b, off, n)
            }
        }

        fun actionRequestFixBrokenLinks(storage: IStorage, topath: String, fromdir: String?): JSONObject {
            fun nameStemLcsuffix(file: IFileInfo): Triple<String, String, String> {
                val name = file.name
                val (stem, suffix) = Basepath.splitName(name)
                return Triple(name, stem, suffix.lowercase())
            }

            fun found(value: JSONArray, aapath: String, stat: IFileStat) {
                value.put(
                    JSONArray()
                        .put(aapath)
                        .put(stat.length)
                        .put(stat.lastModified)
                )
            }

            fun found(ret: JSONObject, apath: String, aapath: String, stat: IFileStat) {
                ret.optJSONArray(apath)?.let { value ->
                    if (value.findsJSONArrayNotNull { _, a ->
                            if (a.stringOrNull(0) == aapath) a else null
                        } == null) {
                        found(value, aapath, stat)
                    }
                    return
                }
                val create = ret.putJSONArray(apath)
                found(create, aapath, stat)
            }

            fun trySameName(ret: JSONObject, apath: String, name: String, aapath: String, file: IFileInfo, stat: IFileStat) {
                if (stat.isFile && file.name == name) {
                    found(ret, apath, aapath, stat)
                }
            }

            fun trySameStem(
                ret: JSONObject,
                apath: String,
                stem: String,
                lcsuffix: String,
                file: IFileInfo,
                stat: IFileStat,
            ) {
                val (fstem, fsuffix) = Basepath.splitName(file.name)
                if (fstem == stem && MimeUtil.isSameType(fsuffix.lowercase(), lcsuffix)) {
                    found(ret, apath, file.apath, stat)
                }
            }

            fun trySameStemSameDir(ret: JSONObject, apath: String, stem: String, lcsuffix: String, dir: IFileInfo?) {
                dir?.let { parent ->
                    FileInfoUtil.fileAndstatsByName(parent) { file, stat ->
                        if (stat.isFile) {
                            trySameStem(ret, apath, stem, lcsuffix, file, stat)
                        }
                    }
                }
            }

            fun trySameNameAncestors(ret: JSONObject, apath: String, name: String, dir: IFileInfo?) {
                var parent = dir?.parent
                var rpath = ".."
                while (parent != null) {
                    val p = parent
                    FileInfoUtil.fileAndstatsByName(p) { file, stat ->
                        trySameName(ret, apath, name, rpath, file, stat)
                    }
                    parent = p.parent
                    rpath += "$FS.."
                }
            }

            fun trySameNameDescendants(ret: JSONObject, apath: String, name: String, fileinfo: IFileInfo) {
                fileinfo.walk3 { file, rpath, stat ->
                    trySameName(ret, apath, name, rpath, file, stat)
                }
            }

            fun trySameStemAncestors(
                ret: JSONObject,
                apath: String,
                name: String,
                stem: String,
                lcsuffix: String,
                dir: IFileInfo?
            ) {
                var rpath = ".."
                var parent = dir?.parent
                while (true) {
                    val p = parent ?: break
                    FileInfoUtil.fileAndstatsByName(p) { file, stat ->
                        if (stat.isFile && file.name != name) {
                            trySameStem(ret, apath, stem, lcsuffix, file, stat)
                        }
                    }
                    parent = p.parent
                    rpath += "$FS.."
                }
            }

            fun trySameStemDescendants(
                ret: JSONObject,
                apath: String,
                name: String,
                stem: String,
                lcsuffix: String,
                fileinfo: IFileInfo
            ) {
                fileinfo.walk3 { file, _, stat ->
                    if (stat.isFile && file.name != name) {
                        trySameStem(ret, apath, stem, lcsuffix, file, stat)
                    }
                }
            }

            fun tryHierarchy(
                ret: JSONObject,
                apath: String,
                name: String,
                stem: String,
                lcsuffix: String,
                target: IFileInfo,
                dir: IFileInfo?
            ) {
                trySameNameAncestors(ret, apath, name, dir)
                trySameNameDescendants(ret, apath, name, target)
                trySameStemAncestors(ret, apath, name, stem, lcsuffix, dir)
                trySameStemDescendants(ret, apath, name, stem, lcsuffix, target)
            }

            val rsrc = storage.rsrc
            val fileinfo = storage.fileInfoAt(topath).let {
                it.result()
                    ?: return rsrc.jsonObjectError(it.failure()!!.bot.joinln())
            }
            if (fileinfo.lcSuffix != Suffix.HTML)
                return rsrc.jsonObjectError(R.string.ExpectingHtml)
            val ret = JSONObject()
            val tobaseuri = UriBuilder().path(fileinfo.apath).buildOrNull()
                ?: return rsrc.jsonObjectError(R.string.InvalidInputPath_, topath)
            val frombaseuri = if (fromdir == null) null else
                storage.fileInfoAt(Basepath.joinPath(fromdir, fileinfo.name)).result()?.let {
                    UriBuilder().path(it.apath).buildOrNull()?.toAbsolute()
                }
            XrefUt.parseHtmlXrefs(fileinfo.asChars(), fileinfo.apath) { _, _, url ->
                if (url.startsWith("data:") || url.startsWith("blob:"))
                    return@parseHtmlXrefs
                val touri = tobaseuri.resolveUri(url)
                val apath = Basepath.cleanPath(touri.toAbsolute().path)
                val target = storage.fileInfoAt(apath).result()
                    ?: return@parseHtmlXrefs
                if (target.exists)
                    return@parseHtmlXrefs
                val (name, stem, lcsuffix) = nameStemLcsuffix(target)
                val dir = target.parent
                if (frombaseuri != null) {
                    val olduri = frombaseuri.resolveUri(url)
                    val oldpath = Basepath.cleanPath(olduri.toAbsolute().path)
                    val oldtarget = storage.fileInfoAt(oldpath).result()
                    if (oldtarget != null) {
                        val oldstat = oldtarget.stat()
                        if (oldstat != null) {
                            found(ret, apath, oldpath, oldstat)
                            return@parseHtmlXrefs
                        }
                        val olddir = oldtarget.parent
                        trySameStemSameDir(ret, apath, stem, lcsuffix, dir)
                        trySameStemSameDir(ret, apath, stem, lcsuffix, olddir)
                        tryHierarchy(ret, apath, name, stem, lcsuffix, target, dir)
                        tryHierarchy(ret, apath, name, stem, lcsuffix, oldtarget, olddir)
                    } else {
                        trySameStemSameDir(ret, apath, stem, lcsuffix, dir)
                        tryHierarchy(ret, apath, name, stem, lcsuffix, target, dir)
                    }
                    return@parseHtmlXrefs
                }
                trySameStemSameDir(ret, apath, stem, lcsuffix, dir)
                tryHierarchy(ret, apath, name, stem, lcsuffix, target, dir)
            }
            return JSONObject().put(Key.result, ret)
        }

        fun actionConfirmFixBrokenLinks(storage: IStorage, topath: String, tofixes: JSONObject): JSONObject {
            val rsrc = storage.rsrc
            val fileinfo = storage.fileInfoAt(topath).let {
                it.result()
                    ?: return rsrc.jsonObjectError(it.failure()!!.bot.joinln())
            }
            if (fileinfo.lcSuffix != Suffix.HTML)
                return rsrc.jsonObjectError(R.string.ExpectingHtml)
            val tobaseuri = UriBuilder().path(fileinfo.apath).buildOrNull()
                ?: return rsrc.jsonObjectError(R.string.InvalidInputPath_, topath)
            val fixed = ArrayList<String>()
            val notfixed = ArrayList<String>()
            val ignored = ArrayList<String>()
            fun fix(tok: ILLKToken, url: String): Boolean? {
                if (url.startsWith("data:") || url.startsWith("blob:"))
                    return null
                val touri = tobaseuri.resolveUri(url)
                val apath = Basepath.cleanPath(touri.toAbsolute().path)
                val target = storage.fileInfoAt(apath).result()
                    ?: return null
                if (target.exists) return null
                val aapath = tofixes.stringOrNull(apath)
                    ?: return false
                val newtarget = storage.fileInfoAt(aapath).result()
                val stat = newtarget?.stat()
                if (newtarget == null || stat == null || !stat.isFile)
                    return false
                val newuri = UriBuilder(tobaseuri).from(touri).path(newtarget.apath.split(FS)).buildOrNull()
                    ?: return false
                val href = if (touri.isRelative) newuri.toRelative().toString() else newuri.toString()
                tok.text = XMLUt.quoteAttr(href)
                return true
            }

            return XrefUt.parseHtmlXrefs(fileinfo.asChars(), fileinfo.apath) { _, tok, url ->
                when (fix(tok, url)) {
                    true -> fixed.add(url)
                    false -> notfixed.add(url)
                    else -> ignored.add(url)
                }
            }?.firstToken?.let { tok ->
                Without.throwableOrNull {
                    XrefUt.render(tok).reader().use {
                        fileinfo.content().write(it)
                    }
                    JSONObject()
                        .put(Key.result, JSONArray().put(fixed.size).put(notfixed.size))
                        .put(Key.warns, JSONArray(notfixed))
                } ?: rsrc.jsonObjectError(R.string.ActionFixBrokenLinksFailed)
            } ?: JSONObject().put(Key.result, JSONArray().put(0).put(0))
        }
    }

}

internal class PartialInputStream(
    private val input: InputStream,
    start: Long,
    size: Long
) : InputStream() {
    var position = start
    var end = start + size

    init {
        if (start > 0 && input.skip(start) != start) throw IOException()
    }

    override fun read(): Int {
        val oposition = position
        val ret = input.read()
        position += oposition + 1
        return ret
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (position >= end) return -1
        val oposition = position
        val length = min(end - position, len.toLong()).toInt()
        val n = input.read(b, off, length)
        if (n > 0) position = oposition + n
        return n
    }

    override fun close() {
        input.close()
    }
}

internal class PartialSeekableInputStream(
    private val input: MySeekableInputStream,
    start: Long,
    size: Long
) : InputStream() {
    val buf = ByteArray(1)
    var position = start
    var end = start + size

    override fun read(): Int {
        if (position >= end) return -1
        input.readFullyAt(position, buf, 0, 1)
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
    private val input: MySeekableInputStream,
    private val start: Long,
    size: Long
) : InputStream() {
    val crlf = "\r\n".toByteArray()
    val bufsize = IStorage.K.BUFSIZE
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
