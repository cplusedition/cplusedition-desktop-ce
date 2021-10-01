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
package sf.andrians.cplusedition.war

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.anjson.JSONUtil.toStringListOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.MyByteArrayOutputStream
import com.cplusedition.bot.core.RandomUt
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.WithoutUtil.Companion.Without
import com.cplusedition.bot.core.file
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.TodoException
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.text.TextUtil
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.An.XrefKey
import sf.andrians.cplusedition.support.BackupRestoreResult
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.Http.HttpStatus
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IRecentsRoot
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.JavaLoggerAdapter
import sf.andrians.cplusedition.support.RecentsRoot
import sf.andrians.cplusedition.support.StorageBase
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.PathUtil
import sf.andrians.cplusedition.support.handler.CpluseditionRequestHandler
import sf.andrians.cplusedition.support.handler.FilepickerHandler
import sf.andrians.cplusedition.support.handler.GalleryGenerator
import sf.andrians.cplusedition.support.handler.HandlerUtil
import sf.andrians.cplusedition.support.handler.IAjaxResponder
import sf.andrians.cplusedition.support.handler.ICpluseditionContext
import sf.andrians.cplusedition.support.handler.ICpluseditionRequest
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.andrians.cplusedition.support.handler.IFilepickerHandler
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.ThumbnailResult
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.handler.ResUtil
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.templates.Templates
import sf.andrians.cplusedition.war.support.MediaUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URI
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream

open class ServerDelegate(datadir: File, pass: CharArray) {
    private val res = ResUtil(MSG.get())

    private val dataDir: File = datadir
    private val storage: IStorage
    private val configRoot: ConfigRoot
    private val recents: IRecentsRoot
    private val alarmContext: AlarmUtil.Context
    private val thumbnailCallback: IThumbnailCallback
    private val annocloudContext: ICpluseditionContext
    private val requestHandler: CpluseditionRequestHandler
    private val filepickerHandler: IFilepickerHandler
    private val lock: Lock = ReentrantLock()
    private val pool = Executors.newCachedThreadPool()
    private val passwordTokens = TreeMap<String, String>()

    init {
        FileUtil.mkdirs(datadir, "etc")
        alarmContext = AlarmUtil.Context(datadir, res)
        configRoot = ConfigRoot(datadir.file(An.PATH.assetsConfig))
        storage = Storage(this.dataDir, pass, configRoot)
        recents = RecentsRoot(res.get(R.string.Recents), Support.Def.recentsSize)
        thumbnailCallback = MyThumbnailCallback(storage)
        annocloudContext = CpluseditionContextAdapter(storage, recents)
        requestHandler = CpluseditionRequestHandler(annocloudContext, null, thumbnailCallback)
        filepickerHandler = FilepickerHandler(storage, null, thumbnailCallback)
    }

    fun destroy() {
        pool.shutdownNow()
        storage.onDestroy()
    }

    fun handle1(
            response: ICpluseditionResponse,
            request: ICpluseditionRequest
    ) {
        lock.lock()
        try {
            handle2(response, request)
        } finally {
            lock.unlock()
        }
    }

    private fun handle2(
            response: ICpluseditionResponse,
            request: ICpluseditionRequest
    ) {
        val path = request.getPathInfo()
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, storage.rsrc, path)
                ?: return requestHandler.badrequest(response, path + "\n" + TextUtil.join("\n", errors))
        val cleanpath = File.separatorChar + cleanrpath
        try {
            if (request.getParam(An.Param.save) != null) {
                val fileinfo = storage.fileInfo(cleanrpath)
                if (fileinfo == null
                        || !fileinfo.root.stat().writable
                        || Basepath.lcSuffix(fileinfo.name) != An.DEF.htmlSuffix)
                    return requestHandler.notfound(response, path)
                try {
                    getPostAsBytes(request).inputStream().use { fileinfo.content().write(it) }
                    val ret = JSONObject()
                    ret.put(Key.result, fileinfo.apath)
                    ret.put(Key.status, HttpStatus.NotModified)
                    jsonResponse(response, ret.toString())
                } catch (e: Throwable) {
                    requestHandler.servererror(response, path, e)
                }
            }
            if (cleanpath.startsWith("/a/")) {
                return handlea(requestHandler, response, request, cleanpath)
            }
            if ("/" == cleanpath || "/index.html".equals(cleanpath, ignoreCase = true)) {
                return actionHome(response, request)
            }
            if (!requestHandler.handleRequest(request, response, cleanrpath)) {
                requestHandler.notfound(response, path)
            }
        } catch (e: FileNotFoundException) {
            requestHandler.notfound(response, path)
        } catch (e: Throwable) {
            requestHandler.servererror(response, path, e)
        }
    }

    fun actionHome(response: ICpluseditionResponse, request: ICpluseditionRequest) {
        val w: PrintWriter? = null
        val isloggedin = storage.isLoggedIn()
        val docpath = request.getParam(An.Param.path) ?: PathUtil.getHomeHtml(isloggedin)
        val ret = actionGetSettings(isloggedin, docpath, requestHandler)
        val settings = """
if(self!==top){self.location.href='${An.PATH._assetsTemplates404Html}';}
window.AnSettings=${ret}
""".trimIndent()
        try {
            response.setupHtmlResponse()
            val content = MyByteArrayOutputStream()
            content.writer().use {
                Templates.HomeTemplate().serialize(it, storage, settings)
            }
            response.setData(content.inputStream())
        } catch (e: Throwable) {
            Conf.e("ERROR: homeResponse()", e);
        } finally {
            FileUtil.close(w)
        }
    }

    @Throws(Exception::class)
    private fun handlea(
            requesthandler: CpluseditionRequestHandler,
            response: ICpluseditionResponse,
            request: ICpluseditionRequest,
            path: String
    ) {
        val jof = _JOF[path]
        if (jof == null) {
            requesthandler.notfound(response, path)
            return
        }
        when (jof) {
            _JOF.recents -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                jsonResponse(response, requesthandler.actionRecents(cmd))
                return
            }
            _JOF.filepicker -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                val data = getPostAsString(request)
                jsonResponse(response, requesthandler.actionFilepicker(cmd, JSONObject(data)))
                return
            }
            _JOF.historyFilepicker -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                val data = getPostAsString(request)
                jsonResponse(response, requesthandler.actionHistoryFilepicker(cmd, JSONObject(data)))
                return
            }
            //#BEGIN FIXME Should remove backupFilepicker action
            _JOF.backupFilepicker -> {
                val data = getPostAsString(request)
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                jsonResponse(response, requesthandler.actionFilepicker(cmd, JSONObject(data)))
                return
            }
            //#END FIXME
            _JOF.linkVerifier -> {
                val data = getPostAsString(request)
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                jsonResponse(response, requesthandler.actionLinkVerifier(cmd, data))
                return
            }
            _JOF.isLoggedIn -> {
                jsonResponse(response, HandlerUtil.jsonResult(requesthandler.isLoggedIn()))
                return
            }
            _JOF.hasLogin -> {
                jsonResponse(response, HandlerUtil.jsonResult(Conf.getLoginCf(dataDir).exists()))
                return
            }
            _JOF.getSettings -> {
                val docpath = getPostAsString(request)
                val isloggedin = storage.isLoggedIn()
                val ret = actionGetSettings(isloggedin, docpath, requesthandler)
                jsonResponse(response, ret)
                return
            }
            _JOF.updateUISettings -> {
                val update = JSONObject(getPostAsString(request))
                val errors = storage.getSettingsStore().call {
                    it.updateSettings(res, update)
                }
                if (errors.isNotEmpty()) {
                    jsonResponse(response, HandlerUtil.jsonErrors(errors))
                    return
                }
                storage.regenerateCustomResources()
                jsonResponse(response, "{}")
                return
            }
            _JOF.updateSessionPreferences -> {
                val update = JSONObject(getPostAsString(request))
                val errors = storage.getSettingsStore().call {
                    it.updatePreferences(res, update)
                }
                val json = if (errors.isEmpty()) "{}" else HandlerUtil.jsonErrors(errors)
                jsonResponse(response, json)
                return
            }
            _JOF.getSessionPreferences -> {
                jsonResponse(response, storage.getSettingsStore().call {
                    JSONObject().put(Key.result, it.getPreferences()).toString()
                })
                return
            }
            _JOF.getTemplatesInfo -> {
                jsonResponse(response, storage.getSettingsStore().call {
                    it.getTemplatesJson(JSONObject(), Key.result).toString()
                })
            }
            _JOF.sanitize -> {
                val json = getPostAsString(request)
                jsonResponse(response, requesthandler.actionSanitize(json))
                return
            }
            _JOF.photoLibraryInfos -> {
                jsonResponse(response, actionExternalImageInfo())
                return
            }
            _JOF.photoLibraryThumbnails -> {
                jsonResponse(response, actionPhotoLibraryThumbnails(storage, getPostAsString(request)))
                return
            }
            _JOF.viewPhotoLibraryThumbnail -> {
                val size = TextUt.parseInt(request.getParam("kind") ?: "", An.DEF.previewPhotoSize)
                val json = getPostAsString(request)
                val imageinfo = JSONObject(json)
                jsonResponse(response, actionViewPhotoLibraryThumbnail(storage, size, imageinfo))
                return
            }
            _JOF.importImageFromPhotoLibrary -> {
                val json = getPostAsString(request)
                jsonResponse(response, actionImportImageFromPhotoLibrary(storage, json))
                return
            }
            _JOF.localImageInfo -> {
                val cpath = getPostAsString(request)
                jsonResponse(response, actionLocalImageInfo(cpath))
                return
            }
            _JOF.takePhoto -> {
                jsonResponse(response, actionTakePhoto())
                return
            }
            _JOF.takePhotoCancelled -> {
                val json = getPostAsString(request)
                val imageinfo = JSONObject(json)
                jsonResponse(response, actionTakePhotoCancelled(imageinfo))
                return
            }
            _JOF.saveBase64Image -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                jsonResponse(response, actionSaveBase64Image(params))
                return
            }
            _JOF.writeImage -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val frompath = params.getString(0)
                val topath = params.getString(1)
                val width = params.getInt(2)
                val height = params.getInt(3)
                val rotation = params.getInt(4)
                val effect = params.getInt(5)
                val adjust = params.getDouble(6)
                val compression = params.getInt(7)
                jsonResponse(response, actionExportImage(frompath, topath, width, height, rotation, effect, adjust, compression))
                return
            }
            _JOF.previewImage -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val url = params.getString(0)
                val rotation = params.getInt(1)
                val effect = params.getInt(2)
                val adjust = params.getDouble(3)
                jsonResponse(response, actionPreviewImage(url, rotation, effect, adjust))
                return
            }
            _JOF.localImageThumbnail -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val cpath = params.getString(0)
                val tnsize = params.getInt(1)
                jsonResponse(response, actionLocalImageThumbnail(cpath, tnsize))
                return
            }
            _JOF.findBlog -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val year = params.getInt(0)
                val month = params.getInt(1)
                if (params.length() > 2) {
                    val day = params.getInt(2)
                    val next = params.getBoolean(3)
                    jsonResponse(response, BlogFinder().actionFindBlog(next, year, month, day))
                } else {
                    jsonResponse(response, BlogFinder().actionListBlogs(year, month))
                }
                return
            }
            _JOF.findFiles -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                jsonResponse(response, actionFindFiles(params.stringOrNull(Key.path), params.stringOrNull(Key.text)))
                return
            }
            _JOF.globalSearch -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                jsonResponse(
                        response,
                        postGlobalSearch(
                                params.optBoolean(Key.filterIgnorecase),
                                params.stringOrDef(Key.filter, ""),
                                params.optBoolean(Key.searchIgnorecase),
                                params.stringOrDef(Key.text, "") /* , params.optBoolean(An.Key.isregex) */
                        ))
                return
            }
            _JOF.globalSearchResult -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                jsonResponse(response, storage.getSearchResult(params.getLong(Key.id) /* , params.getBoolean(An.Key.isregex) */))
                return
            }
            _JOF.createFromTemplate -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                val tpath = params.stringOrNull(Key.template)
                val outpath = params.stringOrNull(Key.path)
                if (tpath == null || outpath == null) return illegalArgumentResponse(response)
                var cleanrpath: String? = null
                if (tpath.contains(File.separator)) {
                    cleanrpath = Support.getcleanrpath(tpath)
                } else {
                    storage.getSettingsStore().call {
                        val info = it.getTemplateInfo(tpath)
                        if (info == null) {
                            res.jsonError(R.string.TemplateNotFound_, tpath)
                        } else {
                            cleanrpath = TextUtil.join(File.separator, An.PATH.assets, "templates", info.cat.toLowerCase(), info.filename)
                            null
                        }
                    }?.let { return jsonResponse(response, it) }
                }
                val tinfo = storage.fileInfo(cleanrpath)
                        ?: return jsonResponse(response, res.jsonError(R.string.TemplateNotFound_, tpath))
                val templatecontent = tinfo.content().readText()
                return jsonResponse(response, requesthandler.actionTemplate(templatecontent, outpath))
            }
            _JOF.postEvent -> {
                val json = getPostAsString(request)
                val error = AlarmUtil.postEvent(alarmContext, json)
                jsonResponse(response, if (error == null) "{}" else HandlerUtil.jsonError(error))
                return
            }
            _JOF.removeEvents -> {
                val json = getPostAsString(request)
                jsonResponse(response, AlarmUtil.removeAlarms(alarmContext, json))
                return
            }
            _JOF.clearEvents -> {
                AlarmUtil.clearAlarms(alarmContext)
                jsonResponse(response, "{}")
                return
            }
            _JOF.getEvents -> {
                val json = getPostAsString(request)
                val params = JSONObject(json)
                val refresh = params.optBoolean(Key.filterIgnorecase)
                val excludeExpired = params.optBoolean(Key.filter)
                val storage = storage
                jsonResponse(response, AlarmUtil.getEvents(alarmContext, storage, refresh, excludeExpired))
                return
            }
            _JOF.exportEvents -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val cpath = params.getString(0)
                jsonResponse(response, AlarmUtil.exportEvents(alarmContext, storage, cpath))
                return
            }
            _JOF.toggleNobackup -> {
                val cpath = getPostAsString(request)
                jsonResponse(response, requesthandler.actionToggleNoBackup(cpath))
                return
            }
            _JOF.actionBackupData -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val backuppath = params.getString(0)
                val aliases = params.getJSONArray(1).toStringListOrNull()
                        ?: return jsonResponse(response, storage.rsrc.jsonError(R.string.InvalidArguments))
                val srcdir = params.getString(2)
                jsonResponse(response, actionBackupData(backuppath, aliases, srcdir))
                return
            }
            _JOF.actionBackupKey -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val keypath = params.getString(0)
                jsonResponse(response, actionBackupKey(keypath))
                return
            }
            _JOF.actionRestoreData -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val backuppath = params.getString(0)
                //#BEGIN SINCE 2.16.0
                val sync = params.getBoolean(1)
                val from = params.getString(2)
                val destdir = params.getString(3)
                jsonResponse(response, actionRestoreData(backuppath, sync, TextUtil.stripLeadingSlash(from), destdir))
                //#END SINCE 2.16.0
                return
            }
            _JOF.readBackupFiletree -> {
                val json = getPostAsString(request)
                val params = JSONArray(json)
                val backupfile = params.getString(0)
                jsonResponse(response, readBackupFiletree(backupfile))
                return
            }
            _JOF.getBackupKeyAliases -> {
                val result = Without.exceptionOrNull {
                    val aliases = storage.submit {
                        it.secAction { sec ->
                            sec.getBackupKeyAliases().mapValues { sec.descOf(it.key, it.value as X509Certificate) }
                        }
                    }.get()
                    JSONObject().put(Key.result, JSONObject(aliases)).toString()
                } ?: storage.rsrc.jsonError(R.string.CommandFailed)
                jsonResponse(response, result)
                return
            }
            _JOF.deleteBackupKey -> {
                val alias = getPostAsString(request)
                val result = Without.exceptionOrNull {
                    val aliases = storage.submit {
                        it.secAction { sec ->
                            sec.deleteAlias(alias)
                            sec.getBackupKeyAliases().mapValues {
                                sec.descOf(it.key, it.value as X509Certificate)
                            }
                        }
                    }.get()
                    JSONObject().put(Key.result, JSONObject(aliases)).toString()
                } ?: storage.rsrc.jsonError(R.string.CommandFailed)
                jsonResponse(response, result)
                return
            }
            _JOF.importBackupKey -> {
                val param = getPostAsJSONArray(request)
                val alias = param.getString(0)
                val cpath = param.getString(1)
                jsonResponse(response, actionImportBackupKey(alias, cpath))
                return
            }
            _JOF.actionResetUserSettings -> {
                storage.getSettingsStore().call { it.reset() }
                storage.regenerateCustomResources()
                jsonResponse(response, "{}")
                return
            }
            _JOF.actionFactoryReset -> {
                requesthandler.actionRecents(An.RecentsCmd.CLEAR)
                Conf.getLoginCf(dataDir).delete()
                storage.factoryReset()
                jsonResponse(response, "{}")
                return
            }
            _JOF.recentsPut -> {
                val json = getPostAsString(request)
                val args = JSONArray(json)
                val navigation = args.getInt(0)
                val cpath = args.stringOrNull(1)
                if (cpath != null) {
                    val state = args.optJSONObject(2)
                    requesthandler.actionRecentsPut(navigation, cpath, state)
                }
                jsonResponse(response, "{}")
                return
            }
            _JOF.getPendingAlarmCount -> {
                jsonResponse(response, HandlerUtil.jsonResult(AlarmUtil.getPendingAlarmCount(alarmContext).toLong()))
                return
            }
            _JOF.actionPrint -> {
                jsonResponse(response, res.jsonError(R.string.PrintServiceNotAvailable))
                return
            }
            _JOF.saveRecovery -> {
                saveRecovery(request, response)
                return
            }
            _JOF.exportToPhotoLibrary -> {

                //#TODO
                jsonResponse(response, "{}")
                return
            }
            _JOF.actionShare -> throw TodoException()
            _JOF.readCSS -> {
                val json = getPostAsString(request)
                val args = JSONArray(json)
                val cpath = args.stringOrNull(0)
                        ?: return illegalArgumentResponse(response)
                jsonResponse(response, actionReadCSS(cpath))
                return
            }
            _JOF.saveCSS -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                val content = args.stringOrNull(1)
                if (cpath == null || content == null) return illegalArgumentResponse(response)
                val infos = args.optJSONObject(2)
                val lcsuffix = Basepath.from(cpath).lcSuffix
                if (An.DEF.cssSuffix != lcsuffix) {
                    return jsonResponse(response, res.jsonError(R.string.InvalidPath))
                }
                //#BEGIN TODO For dbfs, this may save with the xrefs instead.
                val position = infos?.getJSONArray(XrefKey.POSITION)
                if (position != null) {
                    storage.getSettingsStore().call { it.updateFilePosition(cpath, position) }
                }
                //#END TODO
                jsonResponse(response, actionSaveFile(cpath, content, infos))
                return
            }
            _JOF.saveHtml -> {
                //#NOTE: For testing only.
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                val content = args.stringOrNull(1)
                if (cpath == null || content == null) return illegalArgumentResponse(response)
                val infos = args.optJSONObject(2)
                val lcsuffix = Basepath.from(cpath).lcSuffix
                if (An.DEF.htmlSuffix != lcsuffix) {
                    return jsonResponse(response, res.jsonError(R.string.InvalidPath))
                }
                jsonResponse(response, actionSaveFile(cpath, content, infos))
                return
            }
            _JOF.generateGallery -> {
                val args = getPostAsJSONArray(request)
                val outpath = args.getString(0)
                val templatepath = args.getString(1)
                val descending = args.getBoolean(2)
                val singlesection = args.getBoolean(3)
                val dirpath = args.getString(4)
                val rpaths = args.getJSONArray(5)
                val done = CountDownLatch(1)
                GalleryGenerator(storage, MediaUtil::isLandscape) { ret ->
                    jsonResponse(response, ret.toString())
                    done.countDown()
                }.run(outpath, templatepath, descending, singlesection, dirpath, rpaths)
                done.await()
                return
            }
            _JOF.imageConversion -> {
                val args = getPostAsJSONArray(request)
                val dirpath = args.getString(0)
                val rpaths = args.getString(1)
                val lcext = args.getString(2)
                val dim = args.getInt(3)
                val rotation = args.getInt(4)
                val effect = args.getInt(5)
                val adjust = args.getDouble(6)
                val quality = args.getInt(7)
                jsonResponse(response, actionImageConversion(dirpath, rpaths, lcext, dim, rotation, effect, adjust, quality))
                return
            }
            _JOF.cleanupTrash -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                        ?: return illegalArgumentResponse(response)
                jsonResponse(response, actionCleanupTrash(cpath))
                return
            }
            _JOF.onDocumentLoaded -> {
                val args = getPostAsJSONArray(request)
                jsonResponse(response, onDocumentLoaded(args))
                return
            }
            _JOF.videoInfos -> {
                val params = getPostAsJSONArray(request)
                jsonResponse(response, actionVideoInfo(params))
                return
            }
            _JOF.audioInfos -> {
                val params = getPostAsJSONArray(request)
                jsonResponse(response, actionAudioInfo(params))
                return
            }
            _JOF.actionPreview, _JOF.formatCSS, _JOF.generateBarcode, _JOF.heartbeat, _JOF.onWindowSizeChanged, _JOF.recentsStatus,
            _JOF.scanBarcode, _JOF.undead
            ->                 // TODO
                return
            _JOF.exportToPhotoLibraryInfos -> TODO()
            _JOF.focus -> TODO()
            else -> {
                //#END SINCE 2.9
                jsonResponse(response, res.jsonError(R.string.UnsupportedOperation))
                return
            }
        }
    }

    private fun illegalArgumentResponse(response: ICpluseditionResponse) {
        jsonResponse(response, res.jsonError(R.string.IllegalArguments))
    }

    private fun actionReadCSS(cpath: String): String {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return res.jsonError(it.second)
        }
        if (Basepath.lcSuffix(fileinfo.name) != An.DEF.cssSuffix) return res.jsonError(R.string.InvalidPath)
        if (!fileinfo.exists) return res.jsonError(R.string.FileNotFound)
        val position = storage.getSettingsStore().call { it.getFilePosition(cpath) }
        return try {
            //#TODO For dbfs, this may save comes from file metadata instead.
            val content = fileinfo.content().readText()
            return JSONObject().put(Key.result, content).put(Key.status, position).toString()
        } catch (e: Throwable) {
            res.jsonError(R.string.ReadFailed)
        }
    }

    private fun actionSaveFile(cpath: String, content: String, xrefs: JSONObject?): String {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return res.jsonError(it.second)
        }
        if (!fileinfo.root.stat().writable) return res.jsonError(R.string.destinationNotWritable_, cpath)
        return try {
            fileinfo.content().write(content.byteInputStream(), null, xrefs)
            "{}"
        } catch (e: Exception) {
            res.jsonError(R.string.WriteFailed)
        }
    }

    private fun saveRecovery(req: ICpluseditionRequest, response: ICpluseditionResponse) {
        try {
            val json = getPostAsString(req)
            val params = JSONArray(json)
            val cpath = params.getString(0)
            val content = params.getString(1)
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.first ?: throw IOException()
            }
            fileinfo.content().writeRecovery(content.byteInputStream())
        } catch (e: Throwable) {
            
        }
        jsonResponse(response, "{}")
    }

    //#BEGIN TODO Remove me
    private fun cleanupRecoveryDirectories(response: ICpluseditionResponse, param: String) {
        jsonResponse(response, HandlerUtil.jsonResult(String.format("%s %d %s, %d %s",
                res.get(R.string.Removed),
                0, res.get(R.string.file),
                0, res.get(R.string.dir))))
    }
    //#END TODO

    //////////////////////////////////////////////////////////////////////

    private fun actionTakePhoto(): String {
        val path = if (RandomUt.getBool()) An.PATH._assetsImagesPortraitSample else An.PATH._assetsImagesLandscapeSample
        return try {
            val mime = MimeUtil.imageMimeFromPath(path)
                    ?: return res.jsonError(R.string.InvalidImageFileExt_, Basepath.ext(path) ?: "")
            val ret = JSONObject()
            val file = File(dataDir, path)
            ret.put(Key.result, An.RESULT.OK)
            val bitmap = ImageIO.read(file)
            val width = bitmap.width
            val height = bitmap.height
            val tnsize = ImageUtil.fit(width, height, Support.Def.thumbnailMiniWidth, Support.Def.thumbnailMiniHeight)
            val dataurl = ImageUtil.toJpegDataUrl(MediaUtil.scaleImageBase64(
                    res,
                    5000,
                    bitmap,
                    "image/jpeg",
                    tnsize.x,
                    tnsize.y,
                    0,
                    An.Effect.NONE,
                    0.0,
                    An.DEF.jpegQuality))
            val info = ImageUtil.localImageInfo(path, file.name, width, height, mime, file.lastModified(), file.length(), dataurl)
            ret.put(Key.imageinfo, info)
            ret.toString()
        } catch (e: Throwable) {
            res.jsonError(e, R.string.TakePhotoFailed)
        }
    }

    private fun actionTakePhotoCancelled(imageinfo: JSONObject): String {
        return "{}"
    }

    @Throws(JSONException::class)
    protected fun actionGetSettings(isloggedin: Boolean, docpath: String?, requesthandler: CpluseditionRequestHandler): String {
        return storage.getSettingsStore().call {
            val a = JSONObject()
            a.put(Key.result, it.getSettings())
            a.put(Key.status, it.getPreferences())
            a.put(Key.isloggedin, isloggedin)
            if (docpath != null) {
                val json = requesthandler.actionFilepicker(An.FilepickerCmd.FILEINFO, JSONObject().put(Key.path, docpath))
                val docinfo = JSONObject(json)
                a.put(Key.fileinfo, docinfo.optJSONObject(Key.fileinfo))
            }
            a.toString()
        }
    }

    private fun actionBackupData(backuppath: String, aliases: List<String>, srcdir: String): String {
        val backupfile = storage.fileInfoAt(backuppath).let {
            it.first ?: return res.jsonError(it.second)
        }
        backupfile.root.name.let {
            if (it != PATH.Internal && it != PATH.External) {
                return res.jsonError(R.string.InvalidPath)
            }
        }
        try {
            val result = storage.submit { it.backupData(backupfile, aliases, srcdir) }.get()
            return JSONObject()
                    .put(Key.result, backupRestoreResult(res,
                            (if (backupfile.name.endsWith(DEF.zipSuffix)) R.string.ExportResult else R.string.BackupResult),
                            result))
                    .put(Key.warns, JSONArray(result.fails)).toString()
        } catch (e: Throwable) {
            backupfile.delete()
            return res.jsonError(e, /*if (backupevents) R.string.BackupEventsFailed else */ R.string.BackupDataFailed)
        }
    }

    private fun backupRestoreResult(res: IResUtil, stringid: Int, result: BackupRestoreResult): String {
        return res.format(stringid, result.oks, result.skipped, result.fails.size)
    }

    private fun actionBackupKey(keypath: String): String {
        val keyfile = storage.fileInfoAt(keypath).let {
            it.first ?: return res.jsonError(it.second)
        }
        try {
            storage.submit { it.backupKey(keyfile) }.get()
            return res.jsonResult(res.get(R.string.BackupKeyOK))
        } catch (e: Throwable) {
            keyfile.delete()
            return res.jsonError(e, /*if (backupevents) R.string.BackupEventsFailed else */ R.string.BackupDataFailed)
        }
    }

    //#BEGIN SINCE 2.16.0
    private fun actionRestoreData(
            backuppath: String,
            sync: Boolean,
            from: String,
            destdir: String
    ): String {
        val backupfile = storage.fileInfoAt(backuppath).let {
            it.first ?: return res.jsonError(it.second)
        }
        val lcext = Basepath.lcExt(backupfile.name)
        try {
            if (lcext != An.DEF.ibackup && lcext != An.DEF.backup) throw IOException()
            val destdirinfo = storage.fileInfoAt(destdir).let {
                it.first ?: throw IOException()
            }
            if (!destdirinfo.root.stat().writable) throw IOException()
            val result = storage.submit { it.restoreData(destdirinfo, backupfile, from, sync) }.get()
            return JSONObject()
                    .put(Key.result, backupRestoreResult(res, R.string.RestoreResult, result))
                    .put(Key.warns, JSONArray(result.fails)).toString()
        } catch (e: Throwable) {
            return res.jsonError(e, if (An.DEF.events == lcext) R.string.RestoreEventsFailed else R.string.RestoreDataFailed)
        }
    }
    //#END SINCE 2.16.0

    private fun actionImportBackupKey(alias: String, cpath: String): String {
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return storage.rsrc.jsonError(it.second)
        }
        return Without.exceptionOrNull {
            storage.submit {
                it.secAction { sec ->
                    sec.importPublicKey(alias, fileinfo).onResult({ msgid ->
                        storage.rsrc.jsonError(msgid)
                    }, { cert ->
                        val desc = sec.descOf(alias, cert.trustedCertificate as X509Certificate)
                        JSONObject().put(Key.result, JSONArray(desc)).toString()
                    })
                }
            }.get()
        } ?: storage.rsrc.jsonError(R.string.ImportFailed)
    }

    private fun readBackupFiletree(backuppath: String): String {
        try {
            val backupfile = StorageBase.existingFileInfoAt(storage, backuppath).let {
                it.first ?: return res.jsonError(it.second!!)
            }
            val dirtree = storage.submit { it.readBackupFiletree(backupfile) ?: JSONObject() }.get()
            return JSONObject().put(Key.result, dirtree).toString()
        } catch (e: Throwable) {
            return res.jsonError(e, R.string.ErrorReadingBackup)
        }
    }

    @Throws(JSONException::class)
    private fun importExportResult(rpaths: JSONArray, okid: Int, oks: JSONArray, errid: Int, fails: JSONObject): JSONObject {
        val ret = JSONObject()
        if (fails.length() > 0) {
            if (rpaths.length() == 1) {
                val rpath = rpaths.getString(0)
                ret.put(Key.errors, fails.getString(rpath))
            } else {
                ret.put(Key.errors, res.format(errid, oks.length(), fails.length()))
            }
        } else {
            ret.put(Key.result, res.format(okid, oks.length()))
        }
        return ret
    }

    /**
     * @param path Context relative file path.
     * @return {An.Key.result : {Image info with image dimension information}}.
     */
    fun actionLocalImageInfo(path: String): String {
        val fileinfo = storage.fileInfoAt(path).let {
            it.first ?: return res.jsonError(it.second)
        }
        val filestat = fileinfo.stat() ?: return res.jsonError(R.string.ImageNotFound_, path)
        val mime = MimeUtil.imageMimeFromPath(fileinfo.name) ?: return res.jsonError(R.string.InvalidImagePath_, path)
        return try {
            val dim = fileinfo.content().getInputStream().use { input ->
                MediaUtil.getImageDimension(fileinfo.name, input)
            }
            val ret = JSONObject()
            ret.put(Key.result, ImageUtil.localImageInfo(
                    fileinfo.apath,
                    fileinfo.name,
                    dim.first,
                    dim.second,
                    mime,
                    filestat.lastModified,
                    filestat.length,
                    null))
            ret.toString()
        } catch (e: Exception) {
            res.jsonError(e, R.string.ErrorReadingImage_, path)
        }
    }

    @Throws(JSONException::class)
    private fun actionExternalImageInfo(): String {
        return actionImageInfos(storage.fileInfo(An.PATH.assetsImages_)!!)
    }

    @Throws(JSONException::class)
    private fun actionImageInfos(imagesdir: IFileInfo): String {
        val ret = JSONObject()
        val result = JSONArray()
        ret.put(Key.result, result)
        FileInfoUtil.walk2(imagesdir) { file, stat ->
            if (!stat.isFile) return@walk2
            val mime = MimeUtil.imageMimeFromPath(file.name) ?: return@walk2
            try {
                val dim = file.content().getInputStream().use {
                    MediaUtil.getImageDimension(file.name, it)
                }
                result.put(ImageUtil.localImageInfo(
                        file.apath,
                        file.name,
                        dim.first,
                        dim.second,
                        mime,
                        stat.lastModified,
                        stat.length,
                        null))
            } catch (e: Exception) {
                
            }
        }
        return ret.toString()
    }

    @Throws(JSONException::class)
    private fun actionPhotoLibraryThumbnails(storage: IStorage, json: String): String {
        val a = JSONObject(json)
        val infos = a.optJSONArray(Key.infos) ?: return res.jsonError(R.string.NoImageInfoAvailable)
        val tsize = _getthumbnailsize(a)
        return ImageUtil.actionImageThumbnails(MyThumbnailCallback(storage), res, infos, tsize).toString()
    }

    private fun _getthumbnailsize(params: JSONObject): Int {
        val size = params.optInt(Key.size)
        if (size > 0) return size
        return if (params.optInt(Key.type, MediaInfo.Thumbnail.Kind.MICRO_KIND) == MediaInfo.Thumbnail.Kind.MINI_KIND) {
            MediaInfo.Thumbnail.Mini.WIDTH
        } else MediaInfo.Thumbnail.Micro.WIDTH
    }

    @Throws(Exception::class)
    private fun actionViewPhotoLibraryThumbnail(storage: IStorage, size: Int, imageinfo: JSONObject): String {
        val cpath = imageinfo.stringOrNull(MediaInfo.Uri) ?: return res.jsonError(R.string.PleaseSpecifyAnImageId)
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return res.jsonError(it.second)
        }
        return Without.exceptionOrNull {
            fileinfo.content().getInputStream().use {
                val dataurl = ImageUtil.toJpegDataUrl(MediaUtil.getThumbnailBase64(res, size, cpath, it))
                HandlerUtil.jsonResult(dataurl)
            }
        } ?: res.jsonError(R.string.ErrorReadingImage_, cpath)
    }

    /**
     * @return {An.Key.tnpath: /tnrpathx, An.Key.result: /rpathx}
     */
    private fun actionImportImageFromPhotoLibrary(storage: IStorage, json: String): String {
        try {
            /* A context relative path. */
            val ret = JSONObject()
            val params = JSONArray(json)
            val outpath = params.stringOrNull(0) ?: return res.jsonError(R.string.DestinationPathIsRequired)
            val owidth = params.getInt(1)
            val oheight = params.getInt(2)
            val orotation = params.getInt(3)
            val effect = params.getInt(4)
            val adjust = params.getDouble(5)
            val compression = params.optInt(6, An.DEF.jpegQuality)
            val tncompression = An.DEF.jpegQualityThumbnail
            val tnwidth = params.optInt(7, -1)
            val tnheight = params.optInt(8, -1)
            val info = params.getJSONObject(9)
            val srcpath = info.getString(MediaInfo.Id)
            val width = info.optInt(MediaInfo.Width, -1)
            val height = info.optInt(MediaInfo.Height, -1)
            if (width < 0 || height < 0) {
                return res.jsonError(R.string.UnknownInputDimension, srcpath)
            }
            var dst = storage.fileInfoAt(outpath).let {
                it.first ?: return res.jsonError(it.second)
            }
            if (dst.stat()?.isDir == true) {
                dst = ImageUtil.getTempImageFile(res, dst, info)
            }
            val mime = MimeUtil.imageMimeFromPath(srcpath) ?: return res.jsonError(R.string.UnsupportedImageFormat_, srcpath)
            val omime = MimeUtil.imageMimeFromPath(dst.name) ?: return res.jsonError(R.string.UnsupportedImageFormat_, outpath)
            val src = storage.fileInfoAt(srcpath).let {
                it.first ?: return res.jsonError(it.second)
            }
            var image: BufferedImage? = null
            if (width == owidth && height == oheight && mime == omime && orotation == 0 && effect == An.Effect.NONE) {
                src.content().copyTo(dst, src.stat()?.lastModified ?: System.currentTimeMillis())
            } else {
                src.content().getInputStream().use {
                    val img = ImageIO.read(it)?.also { image = it } ?: return res.jsonError(R.string.ErrorReading_, srcpath)
                    MediaUtil.scaleImage(
                            res,
                            Support.Def.scaleImageTimeout,
                            img,
                            omime,
                            owidth,
                            oheight,
                            orotation,
                            effect,
                            adjust,
                            compression).inputStream().use {
                        dst.content().write(it)
                    }
                }
            }
            if (tnwidth == owidth && tnheight == oheight) {
                ret.put(Key.tnpath, dst.apath)
            } else if (tnwidth > 0 && tnheight > 0) {
                val img = image ?: src.content().getInputStream().use {
                    ImageIO.read(it) ?: return res.jsonError(R.string.ErrorReading_, srcpath)
                }
                val tnpath = ImageUtil.getThumbnailPath(dst.apath, tnwidth, tnheight, orotation)
                val tninfo = storage.fileInfoAt(tnpath).let {
                    it.first ?: return res.jsonError(it.second)
                }
                MediaUtil.scaleImage(
                        res,
                        Support.Def.thumbnailTimeout,
                        img,
                        omime,
                        tnwidth,
                        tnheight,
                        orotation,
                        effect,
                        adjust,
                        tncompression).inputStream().use {
                    tninfo.content().write(it)
                }
                ret.put(Key.tnpath, tninfo.apath)

            }
            val noswap = orotation == 0 || orotation == 180
            val dststat = dst.stat()!!
            ret.put(Key.imageinfo, ImageUtil.localImageInfo(
                    dst.apath,
                    src.name,
                    if (noswap) owidth else oheight,
                    if (noswap) oheight else owidth,
                    omime,
                    dststat.lastModified,
                    dststat.length,
                    null))
            return ret.toString()
        } catch (e: Throwable) {
            return res.jsonError(e, R.string.CommandFailed)
        }
    }

    private fun actionSaveBase64Image(params: JSONObject): String {
        try {
            val path = params.getString(Key.path)
            val fileinfo = storage.fileInfoAt(path).let {
                it.first ?: return res.jsonError(it.second)
            }
            val mime = MimeUtil.imageMimeFromPath(fileinfo.name)
            val data = params.getString(Key.data)
            var blob = MediaUtil.fromDataUrl(mime, data) ?: return res.jsonError(R.string.InvalidDataURL)
            if (MimeUtil.PNG == mime) {
                blob = MediaUtil.rewritePNG(res, blob)
            }
            blob.inputStream().use { fileinfo.content().write(it) }
            return "{}"
        } catch (e: Throwable) {
            return res.jsonError(e, R.string.ErrorSavingImage)
        }
    }

    private fun actionExportImage(
            frompath: String,
            topath: String,
            towidth: Int,
            toheight: Int,
            rotation: Int,
            effect: Int,
            adjust: Double,
            compression: Int
    ): String {
        try {
            assert(towidth >= 0)
            assert(toheight >= 0)
            assert(rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270)
            assert(compression >= 0 && compression <= 100)
            val dstinfo = storage.fileInfoAt(topath).let {
                it.first ?: return res.jsonError(it.second)
            }
            val omime = MimeUtil.imageMimeFromPath(dstinfo.name) ?: return res.jsonError(R.string.InvalidOutputMimeType)
            val srcinfo = storage.fileInfoAt(frompath).let {
                it.first ?: return res.jsonError(it.second)
            }
            MimeUtil.imageMimeFromPath(srcinfo.name) ?: return res.get(R.string.InvalidInputMimeType)
            val image = srcinfo.content().getInputStream().use {
                ImageIO.read(it) ?: return res.jsonError(R.string.ErrorReadingImage_, frompath)
            }
            MediaUtil.scaleImage(
                    res,
                    Support.Def.scaleImageTimeout,
                    image,
                    omime,
                    towidth,
                    toheight,
                    rotation,
                    effect,
                    adjust,
                    compression).inputStream().use {
                dstinfo.content().write(it)
            }
            return "{}"
        } catch (e: Throwable) {
            return res.jsonError(e, R.string.ErrorWritingImage_, topath)
        }
    }

    private fun actionPreviewImage(
            url: String,
            rotation: Int,
            effect: Int,
            adjust: Double
    ): String {
        val input: InputStream
        if (url.startsWith("data:")) {
            val blob = MediaUtil.fromDataUrl(null, url) ?: return res.jsonError(R.string.InvalidDataURL)
            input = ByteArrayInputStream(blob)
        } else {
            val cpath = Without.exceptionOrNull { URI.create(url).path } ?: return res.jsonError(R.string.InvalidURL)
            val fileinfo = storage.fileInfoAt(cpath).first ?: return res.jsonError(R.string.InvalidPath)
            input = fileinfo.content().getInputStream()
        }
        return try {
            val image = ImageIO.read(input)
            val output = MediaUtil.scaleImage(
                    res,
                    Support.Def.scaleImageTimeout,
                    image,
                    MimeUtil.JPEG,
                    image.width,
                    image.height,
                    rotation,
                    effect,
                    adjust,
                    An.DEF.jpegQualityThumbnail)
            val odataurl = ImageUtil.toDataUrl(MimeUtil.JPEG, Base64.encodeToString(output, Base64.NO_WRAP))
            HandlerUtil.jsonResult(odataurl)
        } catch (e: Throwable) {
            res.jsonError(e, R.string.ErrorCreatingThumbnail_)
        } finally {
            FileUtil.close(input)
        }
    }

    /**
     * @return { An.Key.result: dataurl, An.Key.errors: errors }
     */
    private fun actionLocalImageThumbnail(cpath: String, tnsize: Int): String {
        try {
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.first ?: return res.jsonError(it.second)
            }
            val image = Without.exceptionOrNull {
                fileinfo.content().getInputStream().use {
                    ImageIO.read(it)
                }
            } ?: return res.jsonError(R.string.ErrorReadingImage_, cpath)
            val dim = ImageUtil.fit(image.width, image.height, tnsize, tnsize)
            val base64 = MediaUtil.scaleImageBase64(
                    res,
                    Support.Def.scaleImageTimeout,
                    image,
                    MimeUtil.JPEG,
                    dim.x,
                    dim.y,
                    0,
                    0,
                    0.0,
                    An.DEF.jpegQualityThumbnail)
            return HandlerUtil.jsonResult(ImageUtil.toDataUrl(MimeUtil.JPEG, base64))
        } catch (e: Throwable) {
            return res.jsonError(e, R.string.ErrorCreatingThumbnail_, cpath)
        }
    }

    fun actionImageConversion(dirpath: String, rpaths: String, outpath: String, dim: Int, rotation: Int, effect: Int, adjust: Double, quality: Int): String {
        val names = JSONUtil.jsonArrayOrFail(rpaths).toStringListOrNull() ?: return res.jsonError(R.string.InvalidArguments)
        val outbasepath = Basepath.from(outpath)
        val lcext = outbasepath.lcExt
        val mime = MimeUtil.imageMimeFromLcExt(lcext) ?: return res.jsonError(R.string.InvalidOutputMimeType)
        val fails = ArrayList<String>()
        val oks = ArrayList<String>()
        for (name in names) {
            val cpath = Basepath.joinPath(dirpath, name)
            val fileinfo = storage.fileInfoAt(cpath).first
            if (fileinfo == null || !fileinfo.exists) {
                fails.add(name)
                continue
            }
            val lc = Basepath.lcExt(fileinfo.name)
            if (!MimeUtil.isImageLcExt(lc)) {
                fails.add(name)
                continue
            }
            var outinfo = fileinfo
            if (lc != lcext) {
                outinfo = storage.fileInfoAt(Basepath.changeSuffix(cpath, ".${lcext}").toString()).first
                if (outinfo == null) {
                    fails.add(name)
                    continue
                }
            }
            val writers = ImageIO.getImageWritersBySuffix(lcext)
            if (!writers.hasNext()) {
                fails.add(name)
                continue
            }
            val writer = writers.next()
            val output = ByteArrayOutputStream()
            try {
                val image = fileinfo.content().getInputStream().use {
                    ImageIO.read(it)
                }
                MemoryCacheImageOutputStream(output).use {
                    writer.output = it
                    val wh = ImageUtil.fit(image.width, image.height, dim, dim)
                    MediaUtil.scaleImage(writer, res, 30 * 1000, mime, wh.x, wh.y, rotation, effect, adjust, quality, image)
                }
                output.toByteArray().inputStream().use {
                    outinfo.content().write(it)
                }
            } catch (e: Exception) {
                fails.add(name)
                continue
            }
            oks.add(name)
        }
        return JSONObject().put(Key.result, oks.size).put(Key.fails, fails.size).toString()
    }

    private inner class BlogFinder {
        val dayRegex = Regex("^(\\d\\d)(\\.html)?$")
        fun actionListBlogs(year: Int, month: Int): String {
            val bloginfo = storage.documentFileInfo(TextUt.format("Home/blog/%04d/%02d", year, month))
                    ?: return res.jsonError(R.string.NotFound)
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
            return JSONObject().put(Key.result, ret).toString()
        }

        fun actionFindBlog(next: Boolean, year: Int, month: Int, day: Int): String {
            try {
                var mm: Int? = month
                var dd: Int? = day
                for (yinfo in sortedyear(year, next)) {
                    for (minfo in sortedmonth(yinfo, mm, next)) {
                        for (dinfo in sortedday(minfo, dd, next)) {
                            return JSONObject().put(Key.result, dinfo.cpath)
                                    .put(Key.fileinfo, dinfo).toString()
                        }
                        dd = null
                    }
                    mm = null
                }
            } catch (e: Exception) {
            }
            return res.jsonError(if (next) R.string.BlogNextNotFound else R.string.BlogPrevNotFound)
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

    private fun actionFindFiles(fromdir: String?, pattern: String?): String {
        /* Context relative paths. */
        if (fromdir == null || pattern == null) return res.jsonError(R.string.InvalidArguments)
        try {
            val paths = storage.submit { it.find(TreeSet(), fromdir, pattern) }.get()
            return JSONObject().put(Key.result, JSONArray(paths)).toString()
        } catch (e: Exception) {
            return res.jsonError(e, R.string.CommandFailed, fromdir)
        }
    }

    private fun postGlobalSearch(
            filterignorecase: Boolean,
            filefilter: String,
            searchignorecase: Boolean,
            searchtext: String /* , final boolean isregex */
    ): String {
        val id = storage.postSearch(filterignorecase, filefilter, searchignorecase, searchtext /* , isregex */)
        return JSONObject().putOrFail(Key.result, id).toString()
    }

    //#BEGIN FIXME No longer valid for Dbfs
    private fun actionCleanupTrash(cpath: String): String {
        val (files, dirs) = if (cpath.isEmpty() || cpath == File.separator) {
            storage.getHomeRoot().cleanupTrash(FileInfoUtil::defaultCleanupTrashPredicate)
        } else {
            val fileinfo = StorageBase.getExistingDocumentDirectory(storage, cpath).let {
                it.first ?: return storage.rsrc.jsonError(it.second!!)
            }
            fileinfo.cleanupTrash(FileInfoUtil::defaultCleanupTrashPredicate)
        }
        return JSONObject()
                .put(Key.total, files + dirs)
                .put(Key.result, String.format("%s: %s %d %s, %d %s",
                        res.get(R.string.CleanupTrash),
                        res.get(R.string.Removed),
                        files,
                        res.get(R.string.file),
                        dirs,
                        res.get(R.string.dir))).toString()
    }
    //#END FIXME

    private fun actionVideoInfo(params: JSONArray): String {
        try {
            val result = JSONObject()
            for (index in 0 until params.length()) {
                val request = params.getJSONObject(index)
                val cpath = request.stringOrNull(Key.path)
                if (TextUtil.isEmpty(cpath) || cpath == null) {
                    continue
                }
                val info = result.putJSONObjectOrFail(cpath)
                val fileinfo = storage.fileInfoAt(cpath).first
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
                } catch (e: Throwable) {
                    
                    info.put(MediaInfo.Playable, false)
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.Error))
                }
            }
            return JSONObject().put(Key.result, result).toString()
        } catch (e: Throwable) {
            
            return storage.rsrc.jsonError(R.string.CommandFailed)
        }
    }

    private fun actionAudioInfo(params: JSONArray): String {
        try {
            val result = JSONObject()
            for (index in 0 until params.length()) {
                val request = params.getJSONObject(index)
                val cpath = request.stringOrNull(Key.path) ?: continue
                val info = result.putJSONObjectOrFail(cpath)
                val fileinfo = storage.fileInfoAt(cpath).first
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
                } catch (e: Throwable) {
                    
                    info.put(MediaInfo.Playable, false)
                    info.put(MediaInfo.Error, storage.rsrc.get(R.string.Error))
                }
            }
            return JSONObject().put(Key.result, result).toString()
        } catch (e: Throwable) {
            
            return storage.rsrc.jsonError(R.string.CommandFailed)
        }
    }

    private fun onDocumentLoaded(args: JSONArray): String {
        return "{}"
    }

    private class MyThumbnailCallback(private val storage: IStorage) : IThumbnailCallback {
        /**
         * @param lastmodified ms since epoch.
         */
        @Throws(Exception::class)
        override fun getThumbnail(rpath: String, lastmodified: Long, size: Int): ThumbnailResult {
            val fileinfo = storage.fileInfoAt(rpath).first
            if (fileinfo?.exists != true) return ThumbnailResult(storage.rsrc.get(R.string.ImageNotFound_, rpath))
            if (size <= MediaInfo.Thumbnail.Micro.WIDTH) {
                var input: InputStream? = null
                try {
                    input = storage.getCached(Conf.CacheType.thumbnails, lastmodified, rpath)
                    if (input != null) {
                        val json = FileUtil.asString(InputStreamReader(input, TextUtil.UTF8()))
                        val a = JSONObject(json)
                        val tnwidth = a.optInt(MediaInfo.Width, -1)
                        val tnheight = a.optInt(MediaInfo.Height, -1)
                        if (tnwidth > 0 && tnheight > 0) {
                            val dim = ImageUtil.fit(tnwidth, tnheight, size, size)
                            if (tnwidth == dim.x && tnheight == dim.y) {
                                val dataurl = a.getString(MediaInfo.DataUrl)
                                
                                return ThumbnailResult(tnwidth, tnheight, dataurl)
                            }
                        }
                    }
                } finally {
                    FileUtil.close(input)
                }
            }
            try {
                val image = Without.exceptionOrNull {
                    fileinfo.content().getInputStream().use {
                        ImageIO.read(it)
                    }
                } ?: return ThumbnailResult(storage.rsrc.get(R.string.UnsupportedImageFormat_, rpath))
                val imagewidth = image.width
                val imageheight = image.height
                val r = ImageUtil.fit(imagewidth, imageheight, size, size)
                val dataurl = ImageUtil.toJpegDataUrl(MediaUtil.scaleImageBase64(
                        storage.rsrc,
                        Support.Def.thumbnailTimeout,
                        image,
                        "image/jpeg",
                        r.x,
                        r.y,
                        0,
                        An.Effect.NONE,
                        0.0,
                        An.DEF.jpegQuality))
                if (size <= MediaInfo.Thumbnail.Micro.WIDTH) {
                    val json = JSONObject()
                    json.put(MediaInfo.Width, r.x)
                    json.put(MediaInfo.Height, r.y)
                    json.put(MediaInfo.DataUrl, dataurl)
                    json.toString().byteInputStream().use {
                        storage.putCached(Conf.CacheType.thumbnails, rpath, it)
                    }
                }
                
                return ThumbnailResult(r.x, r.y, dataurl)
            } catch (e: Throwable) {
                
                return ThumbnailResult(storage.rsrc.get(R.string.ErrorCreatingThumbnail_, rpath))
            }
        }

    }

    protected class CpluseditionContextAdapter(
            private val storage: IStorage,
            private val recentsRoot: IRecentsRoot
    ) : JavaLoggerAdapter(), ICpluseditionContext {

        val isDebug get() = Conf.DEBUG

        private val ajaxResponder: IAjaxResponder? = null

        override fun getStorage(): IStorage {
            return storage
        }

        override fun getRecentsRoot(): IRecentsRoot {
            return recentsRoot
        }

        override fun getAjaxResponder(): IAjaxResponder? {
            return ajaxResponder
        } // @Override

    }

    internal enum class _JOF(var value: String) {
        //# NOTE: This should be in sync with host.dart _JOF.
        //#BEGIN _JOF
        actionBackupData("/a/actionBackupData"),
        actionBackupKey("/a/actionBackupKey"),
        actionFactoryReset("/a/actionFactoryReset"),
        actionPreview("/a/actionPreview"),
        actionPrint("/a/actionPrint"),
        actionResetUserSettings("/a/actionResetUserSettings"),
        actionRestoreData("/a/actionRestoreData"),
        actionShare("/a/actionShare"),
        audioInfos("/a/audioInfos"),
        audioPause("/a/audioPause"),
        audioPlay("/a/audioPlay"),
        audioSeek("/a/audioSeek"),
        backupFilepicker("/a/backupFilepicker"),
        cleanupTrash("/a/cleanupTrash"),
        clearEvents("/a/clearEvents"),
        clearFocus("/a/clearFocus"),
        copyToClipboard("/a/copyToClipboard"),
        createAudioPlayer("/a/createAudioPlayer"),
        createFromTemplate("/a/createFromTemplate"),
        deleteBackupKey("/a/deleteBackupKey"),
        destroyAudioPlayer("/a/destroyAudioPlayer"),
        exportEvents("/a/exportEvents"),
        exportToPhotoLibrary("/a/exportToPhotoLibrary"),
        exportToPhotoLibraryInfos("/a/exportToPhotoLibraryInfos"),
        filepicker("/a/filepicker"),
        findBlog("/a/findBlog"),
        findFiles("/a/findFiles"),
        focus("/a/focus"),
        formatCSS("/a/formatCSS"),
        generateBarcode("/a/generateBarcode"),
        generateGallery("/a/generateGallery"),
        gestureEnable("/a/gestureEnable"),
        getAudioStatus("/a/getAudioStatus"),
        getBackupKeyAliases("/a/getBackupKeyAliases"),
        getDeviceSize("/a/getDeviceSize"),
        getEvents("/a/getEvents"),
        getPendingAlarmCount("/a/getPendingAlarmCount"),
        getSessionPreferences("/a/getSessionPreferences"),
        getSettings("/a/getSettings"),
        getTemplatesInfo("/a/getTemplatesInfo"),
        getUISettings("/a/getUISettings"),
        globalSearch("/a/globalSearch"),
        globalSearchResult("/a/globalSearchResult"),
        hasLogin("/a/hasLogin"),
        heartbeat("/a/heartbeat"),
        hideCaret("/a/hideCaret"),
        hideKeyboard("/a/hideKeyboard"),
        historyFilepicker("/a/historyFilepicker"),
        imageConversion("/a/imageConversion"),
        importBackupKey("/a/importBackupKey"),
        importImageFromPhotoLibrary("/a/importImageFromPhotoLibrary"),
        importVideoFromPhotoLibrary("/a/importVideoFromPhotoLibrary"),
        isKeyboardShown("/a/isKeyboardShown"),
        isLoggedIn("/a/isLoggedIn"),
        linkVerifier("/a/linkVerifier"),
        localImageInfo("/a/localImageInfo"),
        localImageThumbnail("/a/localImageThumbnail"),
        onBoot("/a/onBoot"),
        onDocumentLoaded("/a/onDocumentLoaded"),
        onDocumentUnload("/a/onDocumentUnload"),
        onIFrameLoaded("/a/onIFrameLoaded"),
        onIFrameUnload("/a/onIFrameUnload"),
        onWindowSizeChanged("/a/onWindowSizeChanged"),
        pasteFromClipboard("/a/pasteFromClipboard"),
        photoLibraryInfos("/a/photoLibraryInfos"),
        photoLibraryThumbnails("/a/photoLibraryThumbnails"),
        playVideo("/a/playVideo"),
        pollAudioPlayer("/a/pollAudioPlayer"),
        postEvent("/a/postEvent"),
        previewImage("/a/previewImage"),
        readBackupFiletree("/a/readBackupFiletree"),
        readCSS("/a/readCSS"),
        recents("/a/recents"),
        recentsPut("/a/recentsPut"),
        recentsStatus("/a/recentsStatus"),
        recordVideo("/a/recordVideo"),
        releaseAudioPlayer("/a/releaseAudioPlayer"),
        removeEvents("/a/removeEvents"),
        requestAudioRecordingPermission("/a/requestAudioRecordingPermission"),
        requestVideoRecordingPermission("/a/requestVideoRecordingPermission"),
        sanitize("/a/sanitize"),
        saveBase64Image("/a/saveBase64Image"),
        saveCSS("/a/saveCSS"),
        saveHtml("/a/saveHtml"),
        saveRecovery("/a/saveRecovery"),
        saveStylesheet("/a/saveStylesheet"),
        scanBarcode("/a/scanBarcode"),
        showKeyboard("/a/showKeyboard"),
        sidepanelChanged("/a/sidepanelChanged"),
        startAudioRecording("/a/startAudioRecording"),
        stopAudioRecording("/a/stopAudioRecording"),
        takePhoto("/a/takePhoto"),
        takePhotoCancelled("/a/takePhotoCancelled"),
        toggleKeyboard("/a/toggleKeyboard"),
        toggleNobackup("/a/toggleNobackup"),
        undead("/a/undead"),
        updateSessionPreferences("/a/updateSessionPreferences"),
        updateUISettings("/a/updateUISettings"),
        videoInfos("/a/videoInfos"),
        viewPhotoLibraryThumbnail("/a/viewPhotoLibraryThumbnail"),
        writeImage("/a/writeImage"),
        //#END _JOF
        ;

        companion object {
            private var table = lazy {
                val t = TreeMap<String, _JOF>()
                for (e in values()) {
                    t[e.value] = e
                }
                t
            }

            operator fun get(value: String?): _JOF? {
                return table.value[value]
            }
        }

    }

    companion object {
        private const val serialVersionUID = 1L
        val logger = Conf.logger
        const val DAY = 1000 * 60 * 60 * 24.toLong()
        const val symbolcss = "assets/fonts/"

        private fun jsonResponse(response: ICpluseditionResponse, json: String) {
            try {
                jsonResponse1(response, json)
            } catch (e: IOException) {
                logger.e("ERROR: Sending response", e)
                response.setStatus(HttpStatus.InternalServerError)
            }
        }

        @Throws(IOException::class)
        private fun jsonResponse1(response: ICpluseditionResponse, json: String) {
            val bytes = json.toByteArray(TextUtil.UTF8())
            response.setHeader("no-cache", "true")
            response.setContentType("application/json;charset=UTF-8")
            response.setData(bytes.inputStream())
        }

        @Throws(IOException::class)
        private fun getPostAsBytes(request: ICpluseditionRequest): ByteArray {
            val `is` = request.getInputStream()
            return try {
                FileUtil.asBytes(`is`)
            } finally {
                FileUtil.close(`is`)
            }
        }

        @Throws(IOException::class)
        private fun getPostAsString(request: ICpluseditionRequest): String {
            val `is` = request.getInputStream()
            return try {
                FileUtil.asString(InputStreamReader(`is`, TextUtil.UTF8()))
            } finally {
                FileUtil.close(`is`)
            }
        }

        @Throws(IOException::class, JSONException::class)
        private fun getPostAsJSONArray(request: ICpluseditionRequest): JSONArray {
            return JSONArray(getPostAsString(request))
        }
    }
}
