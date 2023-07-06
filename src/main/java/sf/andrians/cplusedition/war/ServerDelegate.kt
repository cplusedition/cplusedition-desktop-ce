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
import com.cplusedition.anjson.JSONUtil.mapsStringNotNull
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.FSC
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.RandomUt
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.bot
import com.cplusedition.bot.core.file
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Effect
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.An.Param
import sf.andrians.cplusedition.support.An.RESULT
import sf.andrians.cplusedition.support.An.RecentsCmd
import sf.andrians.cplusedition.support.Backend
import sf.andrians.cplusedition.support.BackupUtil
import sf.andrians.cplusedition.support.ConfigRoot
import sf.andrians.cplusedition.support.FileRootInfo
import sf.andrians.cplusedition.support.GalleryParams
import sf.andrians.cplusedition.support.Http.HttpStatus
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IRecentsRoot
import sf.andrians.cplusedition.support.ISecUtilAccessor
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.JavaLoggerAdapter
import sf.andrians.cplusedition.support.RecentsRoot
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.Def.recentsSize
import sf.andrians.cplusedition.support.Support.PathUtil
import sf.andrians.cplusedition.support.handler.CpluseditionRequestHandler
import sf.andrians.cplusedition.support.handler.IAjaxResponder
import sf.andrians.cplusedition.support.handler.ICpluseditionContext
import sf.andrians.cplusedition.support.handler.ICpluseditionRequest
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.andrians.cplusedition.support.handler.IFilepickerHandler
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.ImageCropInfo
import sf.andrians.cplusedition.support.media.ImageOutputInfo
import sf.andrians.cplusedition.support.media.ImageOutputInfoWithTnAndInput
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Mime
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO

open class ServerDelegate constructor(
    private val dataDir: File,
    storage: IStorage
) : CpluseditionRequestHandler(
    CpluseditionContextAdapter(storage, RecentsRoot(storage.rsrc.get(R.string.Recents), recentsSize)),
    null,
    MyThumbnailCallback(storage),
    MediaUtil,
    BarcodeUtil,
) {

    constructor(datadir: File, backend: Backend, pass: CharArray) : this(
        datadir, Storage(datadir, backend, pass, ConfigRoot(FileRootInfo(datadir.file(PATH.assetsConfig))))
    )

    private val lock: Lock = ReentrantLock()
    private val pool = Executors.newCachedThreadPool()
    private val eventUtil = EventUtil(storage)

    init {
        dataDir.file("etc").mkdirs()
        storage.getSettingsStore().invoke { st ->
            recentsAction {
                it.recentsRestore(st.getSession())
            }
        }
    }

    fun destroy() {
        storage.getSettingsStore().invoke { st ->
            val session = st.getSession()
            recentsAction {
                it.recentsSave(session)
            }
            st.saveSession(session)
        }
        pool.shutdownNow()
        storage.onDestroy()
    }

    fun handle1(
        response: ICpluseditionResponse,
        request: ICpluseditionRequest,
    ) {
        lock.lock()
        try {
            handle2(response, request)
        } finally {
            lock.unlock()
        }
    }

    fun handlea(response: ICpluseditionResponse, request: ICpluseditionRequest) {
        val path = request.getPathInfo()
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, storage.rsrc, path)
            ?: return this.badrequest(response, arrayOf(path, *errors.toTypedArray()).bot.joinln())
        val cleanpath = FSC + cleanrpath
        lock.lock()
        try {
            handlea(response, request, cleanpath)
        } catch (e: FileNotFoundException) {
            this.notfound(response, path)
        } catch (e: Throwable) {
            this.servererror(response, path, e)
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
            ?: return this.badrequest(response, arrayOf(path, *errors.toTypedArray()).bot.joinln())
        val cleanpath = FSC + cleanrpath
        try {
            if (request.getParam(Param.save) != null) {
                val fileinfo = storage.fileInfo(cleanrpath)
                if (fileinfo == null
                    || !fileinfo.root.stat().writable
                    || Basepath.lcSuffix(fileinfo.name) != Suffix.HTML
                )
                    return this.notfound(response, path)
                try {
                    getPostAsBytes(request).inputStream().use { fileinfo.content().write(it) }
                    val ret = JSONObject()
                    ret.put(Key.result, fileinfo.apath)
                    ret.put(Key.status, HttpStatus.NotModified)
                    jsonResponse(response, ret)
                } catch (e: Throwable) {
                    this.servererror(response, path, e)
                }
            }
            if ("/" == cleanpath || "/index.html".equals(cleanpath, ignoreCase = true)) {
                val docpath = request.getParam(Param.path) ?: PathUtil.getHomeHtml()
                return this.actionHome(response, docpath)
            }
            if (!this.handleRequest(request, response, cleanrpath)) {
                this.notfound(response, path)
            }
        } catch (e: FileNotFoundException) {
            this.notfound(response, path)
        } catch (e: Throwable) {
            this.servererror(response, path, e)
        }
    }

    @Throws(Exception::class)
    private fun handlea(
        response: ICpluseditionResponse,
        request: ICpluseditionRequest,
        path: String
    ) {
        val jof = _JOF[path]
        if (jof == null) {
            this.notfound(response, path)
            return
        }
        when (jof) {

            ///// Setting actions

            _JOF.getSettings -> {
                val docpath = getPostAsString(request)
                jsonResponse(response, actionGetSettings(docpath))
                return
            }

            _JOF.getTemplatesInfo -> {
                jsonResponse(response, storage.getSettingsStore().invoke {
                    it.getTemplatesJson(JSONObject(), Key.result)
                })
            }

            _JOF.updateUISettings -> {
                val updates = JSONUtil.jsonObjectOrNull(getPostAsString(request))
                val ret = if (updates == null) rsrc.jsonObjectError(R.string.ParametersInvalid)
                else actionUpdateSettings(updates)
                jsonResponse(response, ret)
                return
            }

            _JOF.updateSessionPreferences -> {
                val json = getPostAsString(request)
                jsonResponse(response, actionUpdateSessionPreferences(json))
                return
            }

            _JOF.getSessionPreferences -> {
                jsonResponse(response, actionGetSessionPreferences())
                return
            }

            _JOF.getXrefs -> {
                val params = getPostAsJSONArray(request)
                val apath = params.getString(0)
                jsonResponse(response, actionGetXrefs(apath))
            }

            _JOF.rebuildXrefs -> {
                val params = getPostAsJSONArray(request)
                val cpath = params.getString(0)
                val ret = storage.submit {
                    val file = if (cpath.isEmpty() || cpath == FS) null else {
                        storage.fileInfoAt(cpath).result()
                            ?: return@submit rsrc.jsonObjectError(R.string.InvalidPath)
                    }
                    it.rebuildXrefs(file)
                }.get()
                jsonResponse(response, ret)
            }

            _JOF.requestFixBrokenLinks -> {
                val params = getPostAsJSONArray(request)
                val topath = params.getString(0)
                val fromdir = params.stringOrNull(1)
                jsonResponse(response, actionRequestFixBrokenLinks(storage, topath, fromdir))
            }

            _JOF.confirmFixBrokenLinks -> {
                val params = getPostAsJSONArray(request)
                val topath = params.getString(0)
                val tofix = params.getJSONObject(1)
                jsonResponse(response, actionConfirmFixBrokenLinks(storage, topath, tofix))
            }

            ///// System actions

            _JOF.actionQuit -> {
                jsonResponse(response, actionQuitFromClient())
            }

            _JOF.actionPrint -> {
                jsonResponse(response, rsrc.jsonObjectResult(R.string.PrintServiceNotAvailable))
                return
            }

            _JOF.actionCleanHome -> {
                recentsAction {
                    it.handle(RecentsCmd.CLEAR)
                }
                storage.submit { it.cleanHome() }.get()
                jsonResponse(response, JSONObject())
                return
            }

            _JOF.actionResetUserSettings -> {
                storage.getSettingsStore().invoke { it.reset() }
                storage.regenerateCustomResources()
                jsonResponse(response, JSONObject())
                return
            }

            _JOF.actionFactoryReset -> {
                recentsAction {
                    it.handle(RecentsCmd.CLEAR)
                }
                Conf.getLoginCf(dataDir).delete()
                storage.submit { it.factoryReset() }.get()
                jsonResponse(response, JSONObject())
                return
            }

            ///// Browser actions

            _JOF.onDocumentLoaded -> {
                jsonResponse(response, onDocumentLoaded())
                return
            }

            _JOF.sanitize -> {
                val json = getPostAsString(request)
                jsonResponse(response, this.actionSanitize(json))
                return
            }

            ///// Backup actions

            _JOF.actionBackupData -> {
                val params = getPostAsJSONArray(request)
                val backuppath = params.getString(0)
                val aliases = params.getJSONArray(1).mapsStringNotNull { it }.toList()
                val srcdir = params.getString(2)
                val ret = storage.submit {
                    actionBackupData(it, backuppath, aliases, srcdir)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.actionBackupKey -> {
                val params = getPostAsJSONArray(request)
                val keypath = params.getString(0)
                val ret = storage.submit {
                    actionBackupKey(it, keypath)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.actionRestoreData -> {
                val params = getPostAsJSONArray(request)
                val backuppath = params.getString(0)
                val sync = params.getBoolean(1)
                val from = params.getString(2)
                val destdir = params.getString(3)
                val ret = storage.submit {
                    actionRestoreData(it, backuppath, sync, Basepath.trimLeadingSlash(from).toString(), destdir)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.actionBackupVerify -> {
                val params = getPostAsJSONArray(request)
                val backuppath = params.getString(0)
                val ret = storage.submit {
                    actionVerifyBackup(it, backuppath)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.actionBackupConversion -> {
                val params = getPostAsJSONArray(request)
                val dstpath = params.getString(0)
                val srcpath = params.getString(1)
                val aliases = params.getJSONArray(2).mapsStringNotNull { it }.toList()
                val cut = params.getBoolean(3)
                val ret = storage.submit {
                    actionBackupConversion(it, dstpath, srcpath, aliases, cut)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.actionBackupForward -> {
                val params = getPostAsJSONArray(request)
                val backuppath = params.getString(0)
                val aliases = params.getJSONArray(1).mapsStringNotNull { it }.toList()
                val ret = storage.submit {
                    actionForwardBackup(it, backuppath, aliases)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.getBackupFileInfo -> {
                val cpath = getPostAsJSONArray(request).getString(0)
                val ret = storage.submit {
                    actionBackupFileInfo(it, cpath)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.readBackupFiletree -> {
                val params = getPostAsJSONArray(request)
                val backupfile = params.getString(0)
                val ret = storage.submit {
                    actionReadBackupFiletree(it, backupfile)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.getBackupKeyAliases -> {
                val ret = storage.submit {
                    actionGetBackupKeyAliases(it)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.deleteBackupKey -> {
                val alias = getPostAsString(request)
                val ret = storage.submit {
                    actionDeleteBackupKey(it, alias)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.importBackupKey -> {
                val param = getPostAsJSONArray(request)
                val alias = param.getString(0)
                val cpath = param.getString(1)
                val ret = storage.submit {
                    actionImportBackupKey(it, alias, cpath)
                }.get()
                jsonResponse(response, ret)
                return
            }

            _JOF.exportBackupKey -> {
                val param = getPostAsJSONArray(request)
                val alias = param.getString(0)
                val cpath = param.getString(1)
                val ret = storage.submit {
                    actionExportBackupKey(it, alias, cpath)
                }.get()
                jsonResponse(response, ret)
                return
            }

            ///// File actions

            _JOF.filepicker -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                val data = getPostAsString(request)
                jsonResponse(response, this.actionFilepicker(cmd, JSONObject(data)))
                return
            }

            _JOF.historyFilepicker -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                val data = getPostAsString(request)
                jsonResponse(response, this.actionHistoryFilepicker(cmd, JSONObject(data)))
                return
            }

            _JOF.linkVerifier -> {
                val data = getPostAsString(request)
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                jsonResponse(response, this.actionLinkVerifier(cmd, JSONObject(data)))
                return
            }

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
                jsonResponse(response, actionSaveCSS(cpath, content, infos))
                return
            }

            _JOF.saveHtml -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                val content = args.stringOrNull(1)
                if (cpath == null || content == null) return illegalArgumentResponse(response)
                val infos = args.optJSONObject(2)
                jsonResponse(response, actionSaveHtml(cpath, content, infos))
                return
            }

            _JOF.saveRecovery -> {
                val params = getPostAsJSONArray(request)
                val cpath = params.getString(0)
                val content = params.getString(1)
                jsonResponse(response, actionSaveRecovery(cpath, content))
                return
            }

            _JOF.findBlog -> {
                val params = getPostAsJSONArray(request)
                val year = params.getInt(0)
                val month = params.getInt(1)
                val day = params.getInt(2)
                val next = params.getBoolean(3)
                jsonResponse(response, BlogFinder().actionFindBlog(next, year, month, day))
                return
            }

            _JOF.listBlogs -> {
                val params = getPostAsJSONArray(request)
                val year = params.getInt(0)
                val month = params.getInt(1)
                jsonResponse(response, BlogFinder().actionListBlogs(year, month))
                return
            }

            ///// Files panel actions

            _JOF.toggleNobackup -> {
                val cpath = getPostAsString(request)
                jsonResponse(response, this.actionToggleNoBackup(cpath))
                return
            }

            _JOF.listZip -> {
                val params = getPostAsJSONArray(request)
                val zipfilepath = params.getString(0)
                jsonResponse(response, BackupUtil.listZip(storage, zipfilepath))
                return
            }

            _JOF.unzip -> {
                val params = getPostAsJSONArray(request)
                val zipfilepath = params.getString(0)
                val dstdir = params.getString(1)
                val src = params.getString(2)
                jsonResponse(response, this.actionUnzip(zipfilepath, dstdir, src))
                return
            }

            _JOF.zip -> {
                val params = getPostAsJSONArray(request)
                val zipfilepath = params.getString(0)
                val srcdirpath = params.getString(1)
                jsonResponse(response, this.actionZip(zipfilepath, srcdirpath))
                return
            }

            _JOF.findFiles -> {
                val params = getPostAsJSONObject(request)
                jsonResponse(response, actionFindFiles(params.stringOrNull(Key.path), params.stringOrNull(Key.text)))
                return
            }

            _JOF.createFromTemplate -> {
                val params = getPostAsJSONObject(request)
                val tpath = params.stringOrNull(Key.template)
                val outpath = params.stringOrNull(Key.path)
                if (tpath == null || outpath == null) return illegalArgumentResponse(response)
                return jsonResponse(response, actionCreateFromTemplate(tpath, outpath))
            }

            _JOF.generateGallery -> {
                val args = getPostAsJSONArray(request)
                val params = GalleryParams.from(args, 0)
                val done = CountDownLatch(1)
                actionGenerateGallery(params, mediaUtil::isLandscape) {
                    jsonResponse(response, it)
                    done.countDown()
                }
                done.await()
                return
            }

            _JOF.imageConversion -> {
                val args = getPostAsJSONArray(request)
                val srcdirpath = args.getString(0)
                val rpaths = args.getJSONArray(1).mapsStringNotNull { it }.toList()
                val outinfo = ImageOutputInfo.from(args, 2).result()
                val cut = args.getBoolean(9)
                jsonResponse(response, actionImageConversion(srcdirpath, rpaths, outinfo, cut))
                return
            }

            _JOF.scanBarcode -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                    ?: return illegalArgumentResponse(response)
                val crop = ImageCropInfo.from(args.optJSONArray(2))
                jsonResponse(response, actionScanBarcode(cpath, crop))
                return
            }

            _JOF.generateBarcode -> {
                val args = getPostAsJSONArray(request)
                val type = args.stringOrNull(0) ?: return illegalArgumentResponse(response)
                val scale = args.optInt(1, Conf.QRCODE_SCALE)
                val text = args.stringOrNull(2) ?: return illegalArgumentResponse(response)
                jsonResponse(response, actionGenerateBarcode(type, scale, text))
                return
            }

            _JOF.cleanupTrash -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                    ?: return illegalArgumentResponse(response)
                jsonResponse(response, actionCleanupTrash(cpath))
                return
            }

            _JOF.actionFsck -> {
                val args = getPostAsJSONArray(request)
                val cpath = args.stringOrNull(0)
                    ?: return illegalArgumentResponse(response)
                jsonResponse(response, actionFsck(cpath))
                return
            }

            ///// Events panel actions

            _JOF.postEvent -> {
                storage.getSettingsStore().invoke {
                    val json = getPostAsString(request)
                    val error = eventUtil.postEvent(json)
                    jsonResponse(response, rsrc.jsonObjectError(error))
                }
                return
            }

            _JOF.removeEvents -> {
                val json = getPostAsString(request)
                jsonResponse(response, eventUtil.removeEvents(JSONArray(json)))
                return
            }

            _JOF.clearEvents -> {
                eventUtil.clearEvents()
                jsonResponse(response, JSONObject())
                return
            }

            _JOF.getEvents -> {
                val params = getPostAsJSONObject(request)
                val refresh = params.optBoolean(Key.filterIgnorecase)
                val excludeExpired = params.optBoolean(Key.filter)
                val storage = storage
                jsonResponse(response, eventUtil.getEvents(refresh, excludeExpired))
                return
            }

            _JOF.exportEvents -> {
                jsonResponse(response, rsrc.jsonObjectError(R.string.UnsupportedOperation))
                return
            }

            _JOF.getPendingAlarmCount -> {
                jsonResponse(response, rsrc.jsonObjectResult(eventUtil.getPendingEventCount().toLong()))
                return
            }

            ///// Recents panel actions

            _JOF.recents -> {
                val cmd = TextUt.parseInt(request.getParam("kind"), -1)
                jsonResponse(response, recentsAction {
                    it.handle(cmd)
                })
                return
            }

            _JOF.recentsPut -> {
                val json = getPostAsString(request)
                val args = JSONArray(json)
                val navigation = args.getInt(0)
                val cpath = args.stringOrNull(1)
                if (cpath != null) {
                    val state = args.optJSONObject(2)
                    recentsAction {
                        it.recentsPut(navigation, cpath, state, System.currentTimeMillis())
                    }
                }
                jsonResponse(response, JSONObject())
                return
            }

            ///// Search panel actions

            _JOF.globalSearch -> {
                val params = getPostAsJSONObject(request)
                jsonResponse(
                    response,
                    actionGlobalSearch(
                        params.optBoolean(Key.filterIgnorecase),
                        params.stringOrDef(Key.filter, ""),
                        params.optBoolean(Key.searchIgnorecase),
                        params.stringOrDef(Key.text, "") /* , params.optBoolean(Key.isregex) */
                    )
                )
                return
            }

            _JOF.globalSearchResult -> {
                val params = getPostAsJSONObject(request)
                return jsonResponse(
                    response,
                    storage.getSearchResult(params.getLong(Key.id) /* , params.getBoolean(Key.isregex) */)
                )
            }

            ///// Image actions

            _JOF.photoLibraryInfos -> {
                jsonResponse(response, actionExternalImageInfo())
                return
            }

            _JOF.photoLibraryThumbnails -> {
                jsonResponse(response, actionPhotoLibraryThumbnails(storage, getPostAsString(request)))
                return
            }

            _JOF.viewPhotoLibraryThumbnail -> {
                val size = TextUt.parseInt(request.getParam("kind") ?: "", DEF.previewPhotoSize)
                val imageinfo = getPostAsJSONObject(request)
                jsonResponse(response, actionViewPhotoLibraryThumbnail(storage, size, imageinfo))
                return
            }

            _JOF.importImageFromPhotoLibrary -> {
                val json = getPostAsString(request)
                jsonResponse(response, run {
                    val otiinfo = ImageOutputInfoWithTnAndInput.from(JSONArray(json), 0).let {
                        it.result() ?: return@run rsrc.jsonObjectError(it.failure()!!)
                    }
                    actionImportImage(storage, otiinfo)
                })
                return
            }

            _JOF.localImageInfo -> {
                val params = getPostAsJSONArray(request)
                val cpath = params.getString(0)
                val withtn = params.getBoolean(1)
                jsonResponse(response, actionLocalImageInfo(cpath, withtn))
                return
            }

            _JOF.takePhoto -> {
                jsonResponse(response, actionTakePhoto())
                return
            }

            _JOF.takePhotoCancelled -> {
                jsonResponse(response, actionTakePhotoCancelled(JSONObject()))
                return
            }

            _JOF.saveBase64Image -> {
                val params = getPostAsJSONObject(request)
                val cpath = params.getString(Key.path)
                val data = params.getString(Key.data)
                jsonResponse(response, actionSaveBase64Image(cpath, data))
                return
            }

            _JOF.writeImage -> {
                val params = getPostAsJSONArray(request)
                val frompath = params.getString(0)
                jsonResponse(
                    response,
                    ImageOutputInfo.from(params, 1).onResult({
                        rsrc.jsonObjectError(it)
                    }, {
                        val cropinfo = ImageCropInfo.from(params.optJSONArray(8))
                        actionWriteImage(frompath, it, cropinfo)
                    })
                )
                return
            }

            _JOF.previewImage -> {
                val params = getPostAsJSONArray(request)
                jsonResponse(
                    response,
                    ImageOutputInfo.from(params, 0).onResult({
                        rsrc.jsonObjectError(it)
                    }, {
                        actionPreviewImage(it)
                    })
                )
                return
            }

            _JOF.pdfPoster -> {
                val params = getPostAsJSONArray(request)
                val cpath = params.getString(0)
                val page = params.getInt(1)
                jsonResponse(response, actionPdfPoster(cpath, page))
                return
            }

            _JOF.localImageThumbnail -> {
                val params = getPostAsJSONArray(request)
                val cpath = params.getString(0)
                val tnsize = params.getInt(1)
                val quality = params.getInt(2)
                val crop = ImageCropInfo.from(params.optJSONArray(3))
                jsonResponse(response, actionLocalImageThumbnail(cpath, tnsize, quality, crop))
                return
            }

            ///// Video actions

            _JOF.videoInfos -> {
                val params = getPostAsJSONArray(request)
                jsonResponse(response, actionVideoInfo(params))
                return
            }

            _JOF.videoPoster -> {
                try {
                    val params = getPostAsJSONArray(request)
                    val cpath = params.getString(0)
                    val time = params.getDouble(1)
                    val w = params.getInt(2)
                    val h = params.getInt(3)
                    val quality = params.getInt(4)
                    jsonResponse(response, actionVideoPoster(cpath, time, w, h, quality))
                } catch (e: Throwable) {
                    jsonResponse(response, rsrc.jsonObjectParametersInvalid())
                }
                return
            }

            ///// Audio actions

            _JOF.audioInfos -> {
                val params = getPostAsJSONArray(request)
                jsonResponse(response, actionAudioInfo(params))
                return
            }

            ///// Unused actions

            _JOF.actionView, _JOF.exportToPhotoLibraryInfos,
            _JOF.undead, _JOF.heartbeat, _JOF.focus, _JOF.onWindowSizeChanged,
            _JOF.formatCSS,
            ->
                return

            else -> {
            jsonResponse(response, rsrc.jsonObjectError(R.string.UnsupportedOperation))
            return
            }
        }
    }

    private fun onDocumentLoaded(): JSONObject {
        return JSONObject()
    }

    private fun actionUpdateSettings(update: JSONObject): JSONObject {
        val errors = storage.getSettingsStore().invoke {
            it.updateSettings(rsrc, update)
        }
        if (errors.isNotEmpty()) {
            return rsrc.jsonObjectError(errors)
        }
        storage.regenerateCustomResources()
        return JSONObject()
    }

    private fun actionTakePhoto(): JSONObject {
        val path = if (RandomUt.getBool()) PATH._assetsImagesPortraitSample else PATH._assetsImagesLandscapeSample
        return try {
            val lcsuffix = Basepath.lcSuffix(path)
            val imime = MimeUtil.imageMimeFromLcSuffix(lcsuffix)
                ?: return rsrc.jsonObjectError(R.string.UnsupportedInputFormat_, lcsuffix)
            val ret = JSONObject()
            val file = File(dataDir, path)
            ret.put(Key.result, RESULT.OK)
            val bitmap = ImageIO.read(file)
            val width = bitmap.width
            val height = bitmap.height
            val tnsize = ImageUtil.fit(width, height, Support.Def.thumbnailMiniWidth, Support.Def.thumbnailMiniHeight)
            val dataurl = MediaUtil.toJpegDataUrl(
                DEF.jpegQuality,
                MediaUtil.scaleImageAndApplyEffect(
                    ImageOutputInfo("", tnsize.x, tnsize.y, 0, Effect.NONE, 0.0, DEF.jpegQuality),
                    Mime.JPEG,
                    imime,
                    bitmap
                )
            )
            val info = ImageUtil.localImageInfo(path, file.name, width, height, imime, file.lastModified(), file.length(), dataurl)
            ret.put(Key.imageinfo, info)
            ret
        } catch (e: Throwable) {
            rsrc.jsonObjectError(e, rsrc.actionFailed(R.string.TakePhotoTitle))
        }
    }

    private fun actionTakePhotoCancelled(imageinfo: JSONObject): JSONObject {
        return JSONObject()
    }

    /**
     * @return {An.Key.imageinfo: {}, An.Key.tnpath: String }
     */
    private fun actionImportImage(storage: IStorage, otiinfo: ImageOutputInfoWithTnAndInput): JSONObject {
        try {
            val otinfo = otiinfo.outputTnInfo
            val info = otiinfo.inputInfo ?: return rsrc.jsonObjectParametersInvalid()
            val srcpath = info.stringOrNull(MediaInfo.Id) ?: return rsrc.jsonObjectParametersInvalid()
            val width = info.optInt(MediaInfo.Width, -1)
            val height = info.optInt(MediaInfo.Height, -1)
            if (width < 0 || height < 0) {
                return rsrc.jsonObjectError(R.string.InvalidInputDimension, srcpath)
            }
            val outinfo = otinfo.outputInfo
            val imime = MimeUtil.imageMimeFromPath(srcpath)
                ?: return rsrc.jsonObjectError(R.string.UnsupportedInputFormat_, srcpath)
            val src = storage.fileInfoAt(srcpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            var dst = storage.fileInfoAt(outinfo.path).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            if (dst.stat()?.isDir == true) {
                dst = ImageUtil.getDstImageFile(rsrc, dst, src.name)
            }
            val omime = MimeUtil.imageMimeFromPath(dst.name)
                ?: return rsrc.jsonObjectError(R.string.UnsupportedOutputFormat_, outinfo.path)
            val image = MediaUtil.readImage(src, imime)
                ?: return rsrc.jsonObjectError(R.string.ErrorReading_, srcpath)
            val dim = MediaUtil.outputDim(image.width, image.height, outinfo.width, outinfo.height)
            try {
                MediaUtil.writeImage(
                    storage, dst, omime, outinfo.quality,
                    MediaUtil.scaleImageAndApplyEffect(
                        outinfo.resize(dim.x, dim.y),
                        omime,
                        imime,
                        image
                    )
                )
            } catch (e: UnsupportedOperationException) {
                return rsrc.jsonObjectError(R.string.ActionRequireGmCommandFromTheGraphicsMagickPackage)
            } catch (e: Throwable) {
                return rsrc.jsonObjectError(R.string.ImageWriteFailed_, dst.name)
            }
            val ret = JSONObject()
            val hasdim = otinfo.tnwidth > 0 || otinfo.tnheight > 0
            val tnwidth = if (hasdim) otinfo.tnwidth else DEF.thumbnailSize
            val tnheight = if (hasdim) otinfo.tnheight else DEF.thumbnailSize
            val tndim = MediaUtil.outputDim(image.width, image.height, tnwidth, tnheight)
            if (tndim.x == dim.x && tndim.y == dim.y) {
                ret.put(Key.tnpath, dst.apath)
            } else {
                val dataurl = MediaUtil.toJpegDataUrl(
                    DEF.jpegQualityThumbnail,
                    MediaUtil.scaleImageAndApplyEffect(
                        outinfo.resize(tndim.x, tndim.y),
                        Mime.JPEG,
                        imime,
                        image
                    )
                )
                ret.put(Key.tnpath, dataurl)
            }
            val noswap = outinfo.rotation == 0 || outinfo.rotation == 180
            val dststat = dst.stat()!!
            ret.put(
                Key.imageinfo, ImageUtil.localImageInfo(
                    dst.apath,
                    src.name,
                    if (noswap) dim.x else dim.y,
                    if (noswap) dim.y else dim.x,
                    omime,
                    dststat.lastModified,
                    dststat.length,
                    null
                )
            )
            return ret
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, R.string.CommandFailed)
        }
    }

    protected class CpluseditionContextAdapter(
        private val storage: IStorage,
        private val recentsRoot: IRecentsRoot
    ) : JavaLoggerAdapter(), ICpluseditionContext {

        private val ajaxResponder: IAjaxResponder? = null

        override fun getStorage(): IStorage {
            return storage
        }

        override fun getRecentsRoot(): IRecentsRoot {
            return recentsRoot
        }

        override fun getAjaxResponder(): IAjaxResponder? {
            return ajaxResponder
        }
    }

    internal enum class _JOF(var value: String) {
        actionBackupConversion("/a/XxXJQ"),
        actionBackupData("/a/XxX7B"),
        actionBackupForward("/a/XxX7H"),
        actionBackupKey("/a/XxXfD"),
        actionBackupVerify("/a/XxXfU"),
        actionCleanHome("/a/XxX0k"),
        actionFactoryReset("/a/XxXSa"),
        actionFsck("/a/XxXwv"),
        actionView("/a/XxXkW"),
        actionPrint("/a/XxXCW"),
        actionQuit("/a/XxXx1"),
        actionResetUserSettings("/a/XxXWf"),
        actionRestoreData("/a/XxXTL"),
        actionShare("/a/XxXdU"),
        audioInfos("/a/XxXFQ"),
        audioPause("/a/XxXOA"),
        audioPlay("/a/XxX6s"),
        audioSeek("/a/XxX30"),
        cleanupTrash("/a/XxXcz"),
        clearEvents("/a/XxXY7"),
        clearFocus("/a/XxX2A"),
        copyToClipboard("/a/XxXwW"),
        createAudioPlayer("/a/XxXvv"),
        createFromTemplate("/a/XxXmh"),
        deleteBackupKey("/a/XxXgA"),
        destroyAudioPlayer("/a/XxXkJ"),
        exportBackupKey("/a/XxXTa"),
        exportEvents("/a/XxXgy"),
        exportToPhotoLibrary("/a/XxXxI"),
        exportToPhotoLibraryInfos("/a/XxXuF"),
        filepicker("/a/XxX7s"),
        findBlog("/a/XxXs9"),
        findFiles("/a/XxXah"),
        focus("/a/XxXIx"),
        formatCSS("/a/XxXFS"),
        generateBarcode("/a/XxXPh"),
        generateGallery("/a/XxXAY"),
        gestureEnable("/a/XxXmT"),
        getAudioStatus("/a/XxXR6"),
        getBackupFileInfo("/a/XxXOD"),
        getBackupKeyAliases("/a/XxXSI"),
        getDeviceSize("/a/XxXpk"),
        getEvents("/a/XxX61"),
        getPendingAlarmCount("/a/XxXqX"),
        getSessionPreferences("/a/XxXfA"),
        getSettings("/a/XxXo2"),
        getTemplatesInfo("/a/XxXiF"),
        getXrefs("/a/XxXgxr"),
        globalSearch("/a/XxXa4"),
        globalSearchResult("/a/XxXII"),
        heartbeat("/a/XxXyI"),
        hideCaret("/a/XxXyN"),
        hideKeyboard("/a/XxXwH"),
        historyFilepicker("/a/XxXvt"),
        imageConversion("/a/XxXUM"),
        importBackupKey("/a/XxXNz"),
        importImageFromPhotoLibrary("/a/XxXaN"),
        importVideoFromPhotoLibrary("/a/XxX56"),
        isKeyboardShown("/a/XxX8J"),
        linkVerifier("/a/XxX6J"),
        listBlogs("/a/XxXo5"),
        listZip("/a/XxXCs"),
        localImageInfo("/a/XxXLW"),
        localImageThumbnail("/a/XxXmw"),
        onBoot("/a/XxXVq"),
        onDocumentLoaded("/a/XxXtN"),
        onDocumentUnload("/a/XxXGW"),
        onIFrameLoaded("/a/XxXDW"),
        onIFrameUnload("/a/XxXnw"),
        onWindowSizeChanged("/a/XxXcF"),
        openDatabaseForTesting("/a/XxX9x"),
        pasteFromClipboard("/a/XxXcx"),
        pdfPoster("/a/XxXy7"),
        photoLibraryInfos("/a/XxXUe"),
        photoLibraryThumbnails("/a/XxXVm"),
        playVideo("/a/XxXQC"),
        pollAudioPlayer("/a/XxXgi"),
        postEvent("/a/XxX43"),
        previewImage("/a/XxXGL"),
        readBackupFiletree("/a/XxX2e"),
        readCSS("/a/XxXgE"),
        rebuildXrefs("/a/XxXrxr"),
        requestFixBrokenLinks("/a/XxXrfb"),
        confirmFixBrokenLinks("/a/XxXcfb"),
        recents("/a/XxXbz"),
        recentsPut("/a/XxXrz"),
        recordVideo("/a/XxXVZ"),
        releaseAudioPlayer("/a/XxXuT"),
        removeEvents("/a/XxX4z"),
        requestAudioRecordingPermission("/a/XxXJe"),
        requestVideoRecordingPermission("/a/XxXYU"),
        sanitize("/a/XxXsS"),
        saveBase64Image("/a/XxX9P"),
        saveCSS("/a/XxXNS"),
        saveHtml("/a/XxXVa"),
        saveRecovery("/a/XxX2S"),
        scanBarcode("/a/XxX9v"),
        showKeyboard("/a/XxXrI"),
        sidepanelChanged("/a/XxXkP"),
        startAudioRecording("/a/XxXSC"),
        stopAudioRecording("/a/XxXON"),
        takePhoto("/a/XxXts"),
        takePhotoCancelled("/a/XxXEI"),
        takeScreenshot("/a/XxXA2"),
        toggleNobackup("/a/XxXWu"),
        undead("/a/XxXbf"),
        unzip("/a/XxXIj"),
        updateSessionPreferences("/a/XxX03"),
        updateUISettings("/a/XxXsu"),
        videoInfos("/a/XxXFi"),
        videoPoster("/a/XxXvpo"),
        viewPhotoLibraryThumbnail("/a/XxX4K"),
        writeImage("/a/XxXOE"),
        zip("/a/XxX8F"),
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
        @Throws(IOException::class)
        private fun getPostAsBytes(request: ICpluseditionRequest): ByteArray {
            return IOUt.readBytes(request.getInputStream())
        }

        @Throws(IOException::class)
        private fun getPostAsString(request: ICpluseditionRequest): String {
            return IOUt.readText(request.getInputStream())
        }

        @Throws(IOException::class, JSONException::class)
        private fun getPostAsJSONArray(request: ICpluseditionRequest): JSONArray {
            return JSONArray(getPostAsString(request))
        }

        @Throws(IOException::class, JSONException::class)
        private fun getPostAsJSONObject(request: ICpluseditionRequest): JSONObject {
            return JSONObject(getPostAsString(request))
        }

    }
}

private
class MyThumbnailCallback(private val storage: IStorage) : IFilepickerHandler.IThumbnailCallback {
    /**
     * @param lastmodified ms since epoch.
     */
    @Throws(Exception::class)
    override fun getThumbnail(cpath: String, lastmodified: Long, tnsize: Int): IFilepickerHandler.ThumbnailResult {
        val fileinfo = storage.fileInfoAt(cpath).result()
        if (fileinfo?.exists != true)
            return IFilepickerHandler.ThumbnailResult(storage.rsrc.get(R.string.ImageNotFound_, cpath))
        if (tnsize <= MediaInfo.Thumbnail.Micro.WIDTH) {
            storage.getCached(Conf.CacheType.thumbnails, lastmodified, cpath)?.use { input ->
                val json = InputStreamReader(input).use { it.readText() }
                val a = JSONObject(json)
                val tnwidth = a.optInt(MediaInfo.Width, -1)
                val tnheight = a.optInt(MediaInfo.Height, -1)
                if (tnwidth > 0 && tnheight > 0) {
                    val dim = ImageUtil.fit(tnwidth, tnheight, tnsize, tnsize)
                    if (tnwidth == dim.x && tnheight == dim.y) {
                        val dataurl = a.getString(MediaInfo.DataUrl)
                        
                        return IFilepickerHandler.ThumbnailResult(tnwidth, tnheight, dataurl)
                    }
                }
            }
        }
        try {
            val imime = MimeUtil.imageMimeFromPath(fileinfo.name)
                ?: return IFilepickerHandler.ThumbnailResult(storage.rsrc.get(R.string.UnsupportedInputFormat_, fileinfo.name))
            val image = MediaUtil.readImage(fileinfo, imime)
                ?: return IFilepickerHandler.ThumbnailResult(storage.rsrc.get(R.string.ImageReadFailed_, cpath))
            val imagewidth = image.width
            val imageheight = image.height
            val r = ImageUtil.fit(imagewidth, imageheight, tnsize, tnsize)
            val dataurl = MediaUtil.toJpegDataUrl(
                An.DEF.jpegQuality,
                MediaUtil.scaleImageAndApplyEffect(
                    ImageOutputInfo("", r.x, r.y, 0, An.Effect.NONE, 0.0, An.DEF.jpegQuality),
                    MimeUtil.Mime.JPEG,
                    imime,
                    image
                )
            )
            if (tnsize <= MediaInfo.Thumbnail.Micro.WIDTH) {
                val json = JSONObject()
                json.put(MediaInfo.Width, r.x)
                json.put(MediaInfo.Height, r.y)
                json.put(MediaInfo.DataUrl, dataurl)
                json.toString().byteInputStream().use {
                    storage.putCached(Conf.CacheType.thumbnails, cpath, it)
                }
            }
            
            return IFilepickerHandler.ThumbnailResult(r.x, r.y, dataurl)
        } catch (e: Throwable) {
            
            return IFilepickerHandler.ThumbnailResult(storage.rsrc.get(R.string.CreateThumbnailFailed_, cpath))
        }
    }
}
