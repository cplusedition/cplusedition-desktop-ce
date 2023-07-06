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

import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.With
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.bot
import com.cplusedition.bot.core.changeSuffix
import com.cplusedition.bot.core.deleteSubtreesOrFail
import com.cplusedition.bot.core.file
import com.cplusedition.bot.core.mkdirsOrFail
import com.cplusedition.bot.core.mkparentOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.LinkInfoStatus
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.Backend
import sf.andrians.cplusedition.support.BackupRestoreResult
import sf.andrians.cplusedition.support.BackupUtil


import sf.andrians.cplusedition.support.EncryptedRootInfo
import sf.andrians.cplusedition.support.FileRootInfo
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IRootInfo
import sf.andrians.cplusedition.support.ISecUtilAccessor
import sf.andrians.cplusedition.support.ISettingsStore
import sf.andrians.cplusedition.support.IStorageAccessor
import sf.andrians.cplusedition.support.ReadOnlyBackupFileRoot
import sf.andrians.cplusedition.support.StorageBase
import sf.andrians.cplusedition.support.StorageException
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.DefaultSettings
import sf.andrians.cplusedition.support.css.CSSGenerator
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.handler.ResUtil
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Mime
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.andrians.cplusedition.war.Conf.Defs
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.SecretKey
import kotlin.concurrent.withLock

class Storage constructor(
    private val dataDir: File,
    private val backend: Backend,
    pass: CharArray,
    configroot: IRootInfo,
    assetsroot: IRootInfo? = null,
) : StorageBase(Conf.logger, ResUtil(MSG.get())) {

    private val accessor: IStorageAccessor = StorageAccessor()
    private val assetsRoot: IRootInfo
    private val settingsStore: ISettingsStore
    private val externalBackupRoot: IRootInfo
    private val internalBackupRoot: IRootInfo
    private val homeRoot: IRootInfo
    private val rootList: List<IRootInfo>
    private val customResourcesRoot: CustomResourceRoot
    private val secUtil = SecUtil(dataDir, pass)
    private val backupUtil = BackupUtil(rsrc, secUtil)
    private val pool = Executors.newSingleThreadExecutor()
    protected val customResources: CustomResources
    private val cacheLock = ReentrantLock()

    init {
        settingsStore = SettingsStore(
            SettingStoreAccessor(rsrc, configroot, secUtil, SettingsStorageAccessorDelegate {
                DefaultSettings.defaultSettings(Defs.dpi)
            })
        )
        this.assetsRoot = assetsroot ?: AssetsRoot(
            AssetsRoot.readAssetsJson(
                rsrc, configroot.fileInfo(ConfigPaths.assetsJson).content().inputStream()
            )
        ) { rpath ->
            dataDir.file("assets/$rpath").inputStream()
        }
        internalBackupRoot =
                    FileRootInfo(dataDir.file(PATH.Internal).mkdirsOrFail(), PATH.Internal)
        externalBackupRoot =
                    FileRootInfo(dataDir.file(PATH.External).mkdirsOrFail(), PATH.External)
        homeRoot =
                    when (backend) {
                        Backend.PLAIN -> FileRootInfo(dataDir.file(PATH.Home).mkdirsOrFail(), PATH.Home)
                        else -> throw AssertionError()
                    }
        rootList = arrayListOf(this.assetsRoot, homeRoot, /* privateRoot, */ internalBackupRoot, externalBackupRoot)
        customResources = CustomResources(
            rsrc,
            dataDir.file(Paths.assetsResourcesJs).readBytes(),
            settingsStore.invoke { FontsCSSGenerator.getFontsCSS(it.getSettings()) },
            FontsCSSGenerator.getSymbolsCSS(configroot)
        )
        settingsStore.invoke {
            customResources.regenerate(it.getSettings(), Conf.getCSSConf(it.getCurrentSettings()))
        }
        customResourcesRoot = CustomResourceRoot { rpath ->
            if (!rpath.startsWith(PATH.assetsClient_)) throw IOException()
            customResources.asBytes(rpath) ?: throw IOException()
        }
        setupHomeRoot()
    }

    override fun <R> submit(task: Fun11<IStorageAccessor, R>): Future<R> {
        return pool.submit<R> {
            task(accessor)
        }
    }

    private fun getDbKey(): String {
        return getDbKey(secUtil.invoke { it.getDbKey() })
    }

    private fun getDbKey(key: SecretKey): String {
        return Hex.encode(key.encoded).toString()
    }

    private fun setupHomeRoot() {
        homeRoot.fileInfo(Paths.drafts).mkdirs()
        homeRoot.fileInfo(Paths.blog).mkdirs()
        homeRoot.fileInfo(Paths.indexhtml).let {
            if (!it.exists) {
                val from = fileInfo(PATH.assetsTemplatesIndexHtml)!!
                from.content().copyTo(it)
            }
        }
    }

    override fun onPause() {
        
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        
        this.onPause()
        pool.shutdown()
        pool.awaitTermination(2, TimeUnit.SECONDS)
        super.onDestroy()
    }

    override fun getAssetsRoot(): IRootInfo {
        return assetsRoot
    }

    override fun getHomeRoot(): IRootInfo {
        return homeRoot
    }

    override fun getInternalBackupRoot(): IRootInfo {
        return internalBackupRoot
    }

    override fun getExternalBackupRoot(): IRootInfo {
        return externalBackupRoot
    }

    override fun getRoots(): List<IRootInfo> {
        return rootList
    }

    override fun regenerateCustomResources() {
        getSettingsStore().invoke {
            val conf = Conf.getCSSConf(it.getCurrentSettings())
            customResources.regenerate(it.getSettings(), conf)
        }
    }

    override fun getCustomResourcesTimestamp(): Long {
        return customResources.getTimestamp()
    }

    override fun getButtonSize(): Int {
        return customResources.buttonSize
    }

    override fun getHostCss(): String {
        return customResources.getHostCss()
    }

    private fun isValidScheme(scheme: String?): Boolean {
        return scheme == null || scheme == Conf.SCHEME
    }

    private fun isValidHost(host: String?): Boolean {
        return host == null || host == Conf.HOST
    }

    override fun linkInfo(uri: URI): String {
        if (!isValidScheme(uri.scheme) || !isValidHost(uri.host) || uri.userInfo != null) {
            return LinkInfoStatus.INVALID
        }
        val cpath = uri.path
        val fileinfo = fileInfoAt(cpath).result() ?: return LinkInfoStatus.INVALID
        return if (fileinfo.exists) LinkInfoStatus.EXISTS else LinkInfoStatus.NOTEXISTS
    }

    override fun getCached(type: String, since: Long, cpath: String): InputStream? {
        return cacheLock.withLock {
            val cached = Conf.getCacheFile(dataDir, type, cachefilename(cpath))
            if (cached == null || !cached.exists() || cached.lastModified() <= since) {
                null
            } else try {
                FileInputStream(cached)
            } catch (e: IOException) {
                null
            }
        }
    }

    override fun putCached(type: String, cpath: String, data: InputStream): Boolean {
        return cacheLock.withLock {
            val cached = Conf.getCacheFile(dataDir, type, cachefilename(cpath))
                ?: return@withLock false
            cached.mkparentOrNull()
                ?: return@withLock false
            return@withLock try {
                FileUt.copy(cached, data)
                true
            } catch (e: IOException) {
                false
            }
        }
    }

    override fun clearCached(includediskfiles: Boolean) {
        cacheLock.withLock {
            if (includediskfiles) {
                Conf.clearCacheDir(dataDir)
            }
        }
    }

    override fun getTemp(filename: String?): File? {
        return gettemp(Conf.getTempDir(dataDir), filename)
    }

    override fun deleteTemp(tmpfile: File?) {
        if (tmpfile == null) return
        val dir = tmpfile.parentFile ?: return
        if (dir != Conf.getTempDir(dataDir)) {
            tmpfile.delete()
            return
        }
        return deletetemp(tmpfile)
    }

    override fun getShared(filename: String): File? {
        return null
    }

    override fun deleteShared(name: String) {}

    override fun clearShared(excludes: Collection<String>?) {}

    override fun getStaging(cpath: String): Triple<String, File, File?>? {
        return null
    }

    override fun closeStaging(tmpfile: File) {}

    override fun saveStaging(tmpfile: File) {}

    private fun cachefilename(rpath: String): String {
        return "$rpath.cAcHe"
    }

    override fun getSettingsStore(): ISettingsStore {
        return settingsStore
    }

    private fun getfile(rpathx: String): File {
        return File(dataDir, rpathx)
    }

    /// @param rpathx Clean context relative path without leading /.
    /// @return null if path is not valid or not under a valid root directory.
    override fun fileInfo(cleanrpath: String?): IFileInfo? {
        if (cleanrpath == null) return null
        documentFileInfo(cleanrpath)?.let { return it }
        if (cleanrpath == PATH.assets || cleanrpath.startsWith(PATH.assets_)) {
            if (customResources.asBytes(cleanrpath) != null) return customResourcesRoot.fileInfo(cleanrpath)
            return if (cleanrpath.length <= PATH.assets_.length) assetsRoot
            else assetsRoot.fileInfo(cleanrpath.substring(PATH.assets_.length))
        }
        if (cleanrpath == PATH.Internal || cleanrpath.startsWith(PATH.Internal_)) {
            return if (cleanrpath.length <= PATH.Internal_.length) internalBackupRoot
            else internalBackupRoot.fileInfo(cleanrpath.substring(PATH.Internal_.length))
        }
        if (cleanrpath == PATH.External || cleanrpath.startsWith(PATH.External_)) {
            return if (cleanrpath.length <= PATH.External_.length) externalBackupRoot
            else externalBackupRoot.fileInfo(cleanrpath.substring(PATH.External_.length))
        }
        return null
    }

    /// @param rpathx Clean context relative path without leading /.
    /// @return null if path is not valid or not under a document directory.
    override fun documentFileInfo(cleanrpath: String?): IFileInfo? {
        if (cleanrpath == null) {
            return null
        } else if (cleanrpath == PATH.Home || cleanrpath.startsWith(PATH.Home_)) {
            return if (cleanrpath.length <= PATH.Home_.length) homeRoot
            else homeRoot.fileInfo(cleanrpath.substring(PATH.Home_.length))
        } else {
            return null
        }
    }

    private inner class StorageAccessor : StorageAccessorBase(this), IStorageAccessor {

        override fun cleanHome() {
        }

        override fun factoryReset() {
            settingsStore.invoke { it.reset() }
            regenerateCustomResources()
            dataDir.file(PATH.Home).deleteSubtreesOrFail()
            secUtil.invoke { it.reset() }
            setupHomeRoot()
        }

        override fun <R> secAction(task: Fun11<ISecUtilAccessor, R>): R {
            return secUtil.invoke(task)
        }

        @Throws(StorageException::class)
        override fun resourceResponse(response: ICpluseditionResponse, info: IFileInfo) {
            val rpathx = info.cpath
            try {
                customResources.asBytes(rpathx)?.let {
                    response.setContentLength(it.size.toLong())
                    response.setData(it.inputStream())
                    return
                }
                if (rpathx.startsWith(PATH.assetsJs_) || rpathx.startsWith(PATH.assetsFonts_)) {
                    val file = dataDir.file(rpathx)
                    if (file.exists()) {
                        response.setContentLength(file.length())
                        response.setData(file.inputStream())
                        return
                    }
                }
                val content = info.content().inputStream()
                response.setContentLength(info.content().getContentLength())
                response.setData(content)
            } catch (e: Exception) {
                throw rsrc.storageException(e, R.string.FailedToCopy_, rpathx)
            }
        }

        @Throws(StorageException::class)
        override fun heicResponse(response: ICpluseditionResponse, info: IFileInfo) {
            MediaUtil.readImageAsJpeg(info, Mime.HEIC, DEF.jpegQualityHigh)?.let { blob ->
                response.setContentType(Mime.JPEG)
                response.setContentLength(blob.length.toLong())
                response.setData(blob.inputStream())
            } ?:
            throw rsrc.storageException(R.string.ReadFailed_, info.cpath)
        }

        override fun readBackupFiletree(backupfile: IFileInfo): JSONObject? {
            return Without.exceptionOrNull {
                backupUtil.readBackupFiletree(backupfile)
            }
        }

        override fun readBackupFileInfo(backupfile: IFileInfo): JSONObject {
            return backupUtil.readBackupFileInfo(backupfile)
        }

        override fun backupData(backupfile: IFileInfo, aliases: List<String>, src: IFileInfo): BackupRestoreResult {
            val timer = StepWatch()
            try {
                if (backupfile.lcSuffix == Suffix.ZIP) {
                    if (src.isDir) return backupUtil.exportData(backupfile, src)
                    else throw rsrc.secureException(R.string.InvalidPath_, src.name)
                } else {
                    return backupUtil.backupData(backupfile, aliases, src)
                }
            } finally {
                
            }
        }

        override fun backupKey(keyfile: IFileInfo) {
            backupUtil.backupKey(keyfile)
        }

        override fun restoreData(
            destdir: IFileInfo,
            backupfile: IFileInfo,
            srcpath: String,
            sync: Boolean
        ): BackupRestoreResult {
            val timer = StepWatch()
            try {
                return backupUtil.restoreData(destdir, backupfile, srcpath, sync)
            } finally {
                
            }
        }

        override fun verifyBackup(backupfile: IFileInfo): BackupRestoreResult {
            val timer = StepWatch()
            try {
                return backupUtil.verifyBackup(backupfile)
            } finally {
                
            }
        }

        override fun forwardBackup(backupfile: IFileInfo, aliases: List<String>): JSONObject {
            return backupUtil.forwardBackup(backupfile, aliases)
        }

        override fun backupFileRoot(backupfile: IFileInfo): ReadOnlyBackupFileRoot? {
            try {
                backupfile.content().seekableInputStream() ?: return null
                return secAction { sec ->
                    ReadOnlyBackupFileRoot.of(sec, backupUtil, backupfile)
                }
            } catch (e: Throwable) {
                return null
            }
        }
    }

    class SettingsStorageAccessorDelegate constructor(
        override val createDefaultSettings: Fun01<JSONObject>
    ) : ISettingsStorageAccessorDelegate {
        private var systemFonts: JSONObject? = null

        /**
         * @return fontname->JSONObject.
         */
        override fun findSystemFonts(): JSONObject {
            systemFonts?.let { return it }
            val ret = JSONObject()
            File(Paths.fontsDir).bot.walk { file, rpath ->
                if (!(file.isFile && file.canRead())) return@walk
                val basename = Basepath.from(file.name)
                if (MimeUtil.fontMimeFromLcSuffix(basename.lcSuffix) == null) return@walk
                val fontfaceformat = MimeUtil.fontFaceFormat(basename.lcSuffix) ?: return@walk
                val fontname = "${An.DEF.System} ${basename.stem}"
                val fontface = Support.FontInfo.fontface(fontname, rpath, fontfaceformat)
                val namestyle = Support.FontInfo.splitFamily(fontname)
                var info = ret.optJSONObject(namestyle.first)
                try {
                    if (info == null) {
                        info = JSONObject()
                        info.put(Support.FontInfo.Key.fontname, namestyle.first)
                        info.put(Support.FontInfo.Key.category, An.DEF.System)
                        info.put(Support.FontInfo.Key.styles, JSONArray())
                        info.put(Support.FontInfo.Key.fontfaces, JSONArray())
                        ret.put(namestyle.first, info)
                    }
                    info.getJSONArray(Support.FontInfo.Key.styles).put(namestyle.second)
                    info.getJSONArray(Support.FontInfo.Key.fontfaces).put(fontface)
                } catch (e: JSONException) {
                    
                    ret.remove(namestyle.first)
                }
            }
            systemFonts = ret
            return ret
        }
    }

    protected class CustomResources constructor(
        val rsrc: IResUtil,
        private val resourcesJs: ByteArray,
        private val fontsCss: ByteArray,
        private val symbolsCss: ByteArray,
    ) : StorageBase.AbstractCustomResources() {
        var buttonSize = Support.SettingsDefs.buttonSize

        fun regenerate(settings: JSONObject, conf: CSSGenerator.IConf) {
            buttonSize = conf.buttonSize()
            generateCss(conf, fontsCss, symbolsCss)
            generateResourcesJs()
            StorageBase.StylesGenerator.updateStylesSettings(settings, rsrc)
        }

        public override fun getTimestamp(): Long {
            return super.getTimestamp()
        }

        public override fun getHostCss(): String {
            return super.getHostCss()
        }

        fun generateResourcesJs() {
            val timer = StepWatch()
            resourcesLock.writeLock().lock()
            try {
                resources[An.PATH.assetsResourcesJs] = resourcesJs
            } finally {
                resourcesLock.writeLock().unlock()
            }
            
        }

    }
}

private object Paths {
    const val assetsResourcesJs = "assets/js/r.js"
    const val blog = "blog"
    const val drafts = "drafts"
    const val indexhtml = "index.html"
    const val fontsDir = "/usr/share/fonts"
}

//////////////////////////////////////////////////////////////////////
