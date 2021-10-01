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

import com.cplusedition.anjson.JSONUtil.findJSONArray
import com.cplusedition.anjson.JSONUtil.foreachString
import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.jsonObjectOrFail
import com.cplusedition.anjson.JSONUtil.map
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.WithUtil.Companion.With
import com.cplusedition.bot.core.WithoutUtil.Companion.Without
import com.cplusedition.bot.core.file
import com.cplusedition.bot.core.joinln
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.io.StringPrintStream
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.LinkInfoStatus
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.An.SessionPreferencesKey
import sf.andrians.cplusedition.support.An.SettingsKey
import sf.andrians.cplusedition.support.An._TemplatesJSONKey
import sf.andrians.cplusedition.support.BackupRestoreResult
import sf.andrians.cplusedition.support.BackupUtil
import sf.andrians.cplusedition.support.FileInfoBase
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.FileRootInfo
import sf.andrians.cplusedition.support.IFileContent
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IRootInfo
import sf.andrians.cplusedition.support.ISecUtilAccessor
import sf.andrians.cplusedition.support.ISettingsStore
import sf.andrians.cplusedition.support.ISettingsStoreAccessor
import sf.andrians.cplusedition.support.ReadOnlyContent
import sf.andrians.cplusedition.support.ReadOnlyJSONObjectFIleInfo
import sf.andrians.cplusedition.support.ReadOnlyRootBase
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StorageBase
import sf.andrians.cplusedition.support.StorageException
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.Support.SettingsDefs
import sf.andrians.cplusedition.support.TemplateInfo
import sf.andrians.cplusedition.support.css.CSSGenerator
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.handler.ResUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.*
import java.util.concurrent.locks.ReentrantLock

//#IF USE_DBFS
//#ELSE USE_DBFS
import com.cplusedition.bot.core.deleteSubtreesOrFail
//#ENDIF USE_DBFS

class Storage constructor(
        private val dataDir: File,
        pass: CharArray,
        configroot: IRootInfo,
        assetsroot: IRootInfo? = null
) : StorageBase(Conf.logger, ResUtil(MSG.get())) {

    private val assetsRoot: IRootInfo
    private val settingsStore: ISettingsStore
    private val externalBackupRoot: IRootInfo
    private val internalBackupRoot: IRootInfo

    //#IF USE_DBFS
 ///     private val homeRoot: DbfsRootInfo
    //#ELSE USE_DBFS
    private val homeRoot: IRootInfo
    //#ENDIF USE_DBFS

    private val rootList: List<IRootInfo>
    private val customResourcesRoot: CustomResourceRoot
    private val customResources: CustomResources
    private val secUtil = SecUtil(dataDir, pass)
    private val backupUtil = BackupUtil(rsrc, secUtil)

    init {
        this.assetsRoot = assetsroot
                ?: AssetsRoot(AssetsRoot.readAssetsJson(rsrc, configroot.fileInfo(Paths.assetsJson).content().getInputStream())) { cpath ->
                    dataDir.file(cpath).inputStream()
                }
        settingsStore = SettingsStore(configroot, FileRootInfo(Conf.getEtcDir(dataDir)))
        //#IF USE_DBFS
 ///         homeRoot = DbfsRootInfo(Dbfs.openDatabase(
 ///                 dataDir.file(PATH.Home + ".db").absolutePath,
 ///                 getDbKey(),
 ///                 true,
 ///                 PATH.Home
 ///         ), PATH.Home)
        //#ELSE USE_DBFS
        //#IF USE_AESFS
        //#ELSE USE_AESFS
        homeRoot = FileRootInfo(FileUtil.mkdirs(dataDir, PATH.Home), PATH.Home)
        //#ENDIF USE_AESFS
        //#ENDIF USE_DBFS
        internalBackupRoot = FileRootInfo(FileUtil.mkdirs(dataDir, PATH.Internal), PATH.Internal)
        externalBackupRoot = FileRootInfo(FileUtil.mkdirs(dataDir, PATH.External), PATH.External)
        rootList = arrayListOf(this.assetsRoot, homeRoot, /* privateRoot, */ internalBackupRoot, externalBackupRoot)
        customResources = CustomResources(
                FontsCSSGenerator.getFontsCSS(configroot),
                FontsCSSGenerator.getSymbolsCSS(configroot),
                File(dataDir, Paths.assetsResourcesJs))
        settingsStore.call {
            customResources.regenerate(it.getSettings())
        }
        customResourcesRoot = CustomResourceRoot { rpath ->
            if (!rpath.startsWith(PATH.assetsClient_)) throw IOException()
            customResources.asBytes(rpath) ?: throw IOException()
        }
        setupHomeRoot()
    }

    private fun getDbKey(): String {
        return Hex.encode(secUtil.invoke { it.getDbKey().encoded }).toString()
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
        //#IF USE_DBFS
 ///         homeRoot.onPause()
        //#ENDIF USE_DBFS
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        //#IF USE_DBFS
 ///         homeRoot.onResume()
        //#ENDIF USE_DBFS
    }

    override fun onDestroy() {
        //#IF USE_DBFS
 ///         homeRoot.close()
        //#ENDIF USE_DBFS
        super.onDestroy()
    }

    override fun factoryReset() {
        settingsStore.call { it.reset() }
        regenerateCustomResources()
        //#IF USE_DBFS
 ///         homeRoot.close()
 ///         val dbfile = dataDir.file(PATH.Home + ".db")
 ///         dbfile.delete()
 ///         secUtil.invoke { it.reset() }
 ///         homeRoot.setDbfs(Dbfs.openDatabase(
 ///                 dbfile.absolutePath,
 ///                 getDbKey(),
 ///                 true,
 ///                 PATH.Home
 ///         ))
        //#ELSE USE_DBFS
        dataDir.file(PATH.Home).deleteSubtreesOrFail()
        secUtil.invoke { it.reset() }
        //#ENDIF USE_DBFS
        setupHomeRoot()
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
        getSettingsStore().call {
            customResources.regenerate(it.getSettings())
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
        if (!isValidScheme(uri.scheme)
                || !isValidHost(uri.host)
                || uri.userInfo != null) {
            return LinkInfoStatus.INVALID
        }
        val cpath = uri.path
        val fileinfo = fileInfoAt(cpath).first ?: return LinkInfoStatus.INVALID
        return if (fileinfo.exists) LinkInfoStatus.EXISTS else LinkInfoStatus.NOTEXISTS
    }

    override fun getCached(type: String, since: Long, cpath: String): InputStream? {
        val cached = Conf.getCacheFile(dataDir, type, cachefilename(cpath))
        return if (cached == null || !cached.exists() || cached.lastModified() <= since) {
            null
        } else try {
            FileInputStream(cached)
        } catch (e: IOException) {
            null
        }
    }

    override fun putCached(type: String, cpath: String, data: InputStream): Boolean {
        val cached = Conf.getCacheFile(dataDir, type, cachefilename(cpath)) ?: return false
        FileUtil.mkparent(cached)
        return try {
            FileUtil.copy(cached, false, data)
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun readBackupFiletree(backupfile: IFileInfo): JSONObject? {
        return backupUtil.readBackupFiletree(backupfile)
    }

    override fun <R> secAction(task: Fun11<ISecUtilAccessor, R>): R {
        return secUtil.invoke(task)
    }

    override fun backupData(backupfile: IFileInfo, aliases: List<String>, srcdirpath: String): BackupRestoreResult {
        val timer = StepWatch()
        try {
            val srcdir = if (srcdirpath.isEmpty()) getHomeRoot() else fileInfoAt(srcdirpath).let {
                it.first ?: throw SecureException(it.second.joinln())
            }
            if (srcdir.root.name != PATH.Home) {
                throw rsrc.secureException(R.string.InvalidPath_, srcdirpath)
            }
            if (backupfile.name.endsWith(DEF.zipSuffix)) {
                return backupUtil.exportData(backupfile, srcdir)
            } else {
                return backupUtil.backupData(backupfile, aliases, srcdir)
            }
        } finally {
            
        }
    }

    override fun backupKey(keyfile: IFileInfo) {
        backupUtil.backupKey(keyfile)
    }

    override fun restoreData(destdir: IFileInfo, backupfile: IFileInfo, srcpath: String, sync: Boolean): BackupRestoreResult {
        val timer = StepWatch()
        try {
            return backupUtil.restoreData(destdir, backupfile, srcpath, sync)
        } finally {
            
        }
    }

    override fun clearCached(includediskfiles: Boolean) {
        if (includediskfiles) {
            Conf.clearCacheDir(dataDir)
        }
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
            val length = info.stat()?.length ?: throw IOException()
            response.setContentLength(length)
            response.setData(info.content().getInputStream())
        } catch (e: Exception) {
            throw rsrc.storageException(e, R.string.FailedToCopy_, rpathx)
        }
    }

    override fun getSettingsStore(): ISettingsStore {
        return settingsStore
    }

    private fun getfile(rpathx: String): File {
        return File(dataDir, rpathx)
    }

    private class CustomResources(
            val fontsCss: ByteArray,
            val symbolsCss: ByteArray,
            val resourcesJs: File
    ) : AbstractCustomResources() {
        var buttonSize = SettingsDefs.buttonSize

        fun regenerate(settings: JSONObject) {
            val conf = Conf.getCSSConf(settings.getJSONObject(SettingsKey.current))
            buttonSize = conf.buttonSize()
            generateCss(conf, fontsCss, symbolsCss)
            StylesGenerator.updateStylesSettings(settings)
            generateResourcesJs()
        }

        public override fun getTimestamp(): Long {
            return super.getTimestamp()
        }

        public override fun getHostCss(): String {
            return super.getHostCss()
        }

        fun generateResourcesJs() {
            val timer = StepWatch()
            val out = ByteArrayOutputStream()
            var input: InputStream? = null
            try {
                input = FileInputStream(resourcesJs)
                FileUtil.copy(out, input)
            } catch (e: IOException) {
                Conf.w("ERROR: Reading " + resourcesJs.name);
            } finally {
                FileUtil.close(input)
            }
            val a = out.toByteArray()
            resourcesLock.writeLock().lock()
            try {
                resources[PATH.assetsResourcesJs] = a
            } finally {
                resourcesLock.writeLock().unlock()
            }
            
        }

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

    override fun <T : MutableCollection<String>> find(ret: T, fromdir: String, searchtext: String): T {
        if (fromdir.isEmpty()) {
            for (root in rootList) {
                root.find(ret, "", searchtext)
            }
        } else {
            fileInfoAt(fromdir).first?.let { fileinfo ->
                fileinfo.root.find(ret, fileinfo.rpath, searchtext)
            }
        }
        return ret
    }
}

private object FontsCSSGenerator : StorageBase.AbstractFontsCSSGenerator() {
    private var fonts: JSONObject? = null
    private var fontsCSS: ByteArray? = null
    private var symbolsCSS: ByteArray? = null

    fun getFontSettings(settings: JSONObject, configroot: IRootInfo) {
        try {
            val fonts = readFonts(configroot)
            settings.put(SettingsKey.fontCategories, JSONArray(fonts.getJSONArray(Support.FontInfo.Key.cats0).toString()))
            settings.put(SettingsKey.fontFamilies, JSONObject(fonts.getJSONObject(Support.FontInfo.Key.fonts0).toString()))
            settings.put(SettingsKey.symbolFamilies, JSONObject(fonts.getJSONObject(Support.FontInfo.Key.fonts2).toString()))
        } catch (e: Throwable) {
            Conf.e("ERROR: getting font families: " + e.message)
        }
    }

    @Synchronized
    fun getFontsCSS(configroot: IRootInfo): ByteArray {
        return fontsCSS ?: try {
            val fonts = readFonts(configroot)
            val fonts0 = fonts.optJSONObject(Support.FontInfo.Key.fonts0)
            StringPrintStream("UTF-8").use { out ->
                fonts0.keys().forEach { key ->
                    val info = fonts0.optJSONObject(key) ?: return@forEach
                    val fontfaces = info.optJSONArray(Support.FontInfo.Key.fontfaces) ?: return@forEach
                    for (i in 0 until fontfaces.length()) {
                        ///#NOTE The fontface string has been sanitized when generating fonts.json.
                        out.println(fontfaces.getString(i))
                    }
                }
                out
            }.toByteArray()
        } catch (e: Throwable) {
            Conf.e("ERROR: Generating fonts.css", e)
            ByteArray(0)
        }.also { fontsCSS = it }
    }

    @Synchronized
    fun getSymbolsCSS(configroot: IRootInfo): ByteArray {
        return symbolsCSS ?: try {
            configroot.fileInfo(Paths.fontawesomeCss).content().readBytes()
        } catch (e: Throwable) {
            Conf.e("ERROR: Reading fontawesome.css")
            ByteArray(0)
        }.also { symbolsCSS = it }
    }

    @Synchronized
    private fun readFonts(configroot: IRootInfo): JSONObject {
        return fonts ?: try {
            val jsonfile = configroot.fileInfo(Paths.fontsJson)
            JSONObject(jsonfile.content().readText())
        } catch (e: Throwable) {
            Conf.w("ERROR: Reading fonts.json: " + e.message)
            JSONObject()
        }.also { fonts = it }
    }
}

private class SettingsStore(
        configroot: IRootInfo,
        etcroot: IRootInfo
) : ISettingsStore {
    private val lock = ReentrantLock()
    private val accessor = ISettingsStoreImpl(configroot, etcroot)
    override fun <T> call(code: Fun11<ISettingsStoreAccessor, T>): T {
        return With.lock(lock) { code(accessor) }
    }
}

private class ISettingsStoreImpl(
        private val configRoot: IRootInfo,
        private val etcRoot: IRootInfo
) : ISettingsStoreAccessor {
    private val templatesInfos = TemplateInfos(configRoot)
    private var settings = reloadSettings()
    private var preferences = initPreferences()
    private var filePositions = initFilePositions()

    private object K {
        val settings = "settings.json"
        val preferences = "preferences.json"
    }

    override fun reset() {
        etcRoot.fileInfo(K.settings).delete()
        etcRoot.fileInfo(K.preferences).delete()
        settings = initDefaultSettings(configRoot)
        preferences = initDefaultPreferences()
        filePositions = JSONArray()
    }

    /// Note that changes to settings must goes through updateSettings().
    override fun getSettings(): JSONObject {
        return settings
    }

    override fun getCurrentSettings(): JSONObject {
        return settings.optJSONObject(SettingsKey.current)!!
    }

    override fun getDefaultSettings(): JSONObject {
        return settings.optJSONObject(SettingsKey.defaults)!!
    }

    /// Note that changes to preferences must goes through updatePreferences().
    override fun getPreferences(): JSONObject {
        return preferences
    }

    private fun reloadSettings(): JSONObject {
        return initSettings(configRoot)
    }

    override fun updateSettings(res: IResUtil, update: JSONObject): List<String> {
        val errors = Support.updateSettings(res, getCurrentSettings(), update)
        saveSettings()
        return errors
    }

    @Throws(Exception::class)
    override fun updatePreferences(res: IResUtil, update: JSONObject): List<String> {
        val errors = StorageBase.updateSessionPreferences(res, this, preferences, update)
        savePreferences()
        return errors
    }

    override fun getTemplatesJson(ret: JSONObject, key: String): JSONObject {
        return templatesInfos.getTemplatesJson(ret, key)
    }

    override fun getTemplateInfo(name: String?): TemplateInfo? {
        return templatesInfos.getInfo(name)
    }

    override fun updateFilePosition(cpath: String, position: JSONArray) {
        filePositions.findJSONArray { index, value -> if (value?.stringOrNull(0) == cpath) index else null }
                ?.let { filePositions.remove(it) }
        if (filePositions.length() > DEF.recentFilePositionCount) filePositions.remove(0)
        filePositions.put(JSONArray().put(cpath).put(position))
        savePreferences()
    }

    override fun getFilePosition(cpath: String): JSONArray? {
        return filePositions.findJSONArray { _, value ->
            if (value?.stringOrNull(0) == cpath) value.jsonArrayOrNull(1) else null
        }
    }

    private fun initFilePositions(): JSONArray {
        return getPreferences().jsonArrayOrNull(SessionPreferencesKey.filePositions) ?: JSONArray().also {
            getPreferences().put(SessionPreferencesKey.filePositions, it)
        }
    }

    private fun initPreferences(): JSONObject {
        try {
            readPreferences()?.let { return it }
        } catch (e: Exception) {
            Conf.e("ERROR: initPreferences(): " + e.message);
        }
        return initDefaultPreferences()
    }

    private fun initDefaultPreferences(): JSONObject {
        return JSONObject()
                .put(SessionPreferencesKey.symbolFamily, DEF.FontAwesome)
                .put(SessionPreferencesKey.showDoneEvents, true)
    }

    private fun initSettings(configroot: IRootInfo): JSONObject {
        val ret = initDefaultSettings(configroot)
        try {
            readSettings()?.let {
                val current = ret.getJSONObject(SettingsKey.current)
                for (key in it.keys()) {
                    if (current.has(key)) current.putOpt(key, it.get(key))
                }
            }
        } catch (e: Throwable) {
            Conf.e("ERROR: initSettings(): " + e.message);
        }
        return ret
    }

    private fun initDefaultSettings(configroot: IRootInfo): JSONObject {
        try {
            val settings = JSONObject()
            settings.put(SettingsKey.defaults, Support.DefaultSettings.defaultSettings(Conf.Defs.dpi))
            settings.put(SettingsKey.current, Support.DefaultSettings.defaultSettings(Conf.Defs.dpi))
            FontsCSSGenerator.getFontSettings(settings, configroot)
            StylesGenerator.updateStylesSettings(settings)
            templatesInfos.getTemplatesJson(settings, SettingsKey.htmlTemplates)
            return settings
        } catch (e: Throwable) {
            Conf.e("ERROR: initDefaultSettings(): " + e.message)
            return JSONObject()
        }
    }

    private fun readPreferences(): JSONObject? {
        return Without.exceptionOrNull { JSONObject(etcRoot.fileInfo(K.preferences).content().readText()) }
    }

    private fun readSettings(): JSONObject? {
        return Without.exceptionOrNull { JSONObject(etcRoot.fileInfo(K.settings).content().readText()) }
    }

    private fun saveSettings() {
        getCurrentSettings().toString().byteInputStream().use {
            etcRoot.fileInfo(K.settings).content().write(it)
        }
    }

    private fun savePreferences() {
        preferences.toString().byteInputStream().use {
            etcRoot.fileInfo(K.preferences).content().write(it)
        }
    }

}

private object Paths {
    const val assetsJson = "assets.json"
    const val fontsJson = "fonts.json"
    const val templatesJson = "templates.json"
    const val fontawesomeCss = "fontawesome.css"
    const val assetsResourcesJs = "assets/js/r.js"
    const val blog = "blog"
    const val drafts = "drafts"
    const val indexhtml = "index.html"
}

private object StylesGenerator {
    fun updateStylesSettings(settings: JSONObject) {
        try {
            val styles = readStyles(settings)
            settings.put(SettingsKey.charStyles, styles.getJSONArray(SettingsKey.charStyles))
            settings.put(SettingsKey.highlightStyles, styles.getJSONArray(SettingsKey.highlightStyles))
            settings.put(SettingsKey.paraStyles, styles.getJSONArray(SettingsKey.paraStyles))
            settings.put(SettingsKey.builtinCharStyles, styles.getJSONArray(SettingsKey.builtinCharStyles))
            settings.put(SettingsKey.builtinParaStyles, styles.getJSONArray(SettingsKey.builtinParaStyles))
            settings.put(SettingsKey.bgImgSamples, styles.getJSONArray(SettingsKey.bgImgSamples))
        } catch (e: Throwable) {
            Conf.e("ERROR: Getting font families: " + e.message)
        }
    }

    @Synchronized
    private fun readStyles(settings: JSONObject): JSONObject {
        try {
            return CSSGenerator.StylesJSON(MSG.get()).build()
        } catch (e: Throwable) {
            Conf.w("ERROR: Reading styles: " + e.message)
            return JSONObject()
        }
    }
}

//////////////////////////////////////////////////////////////////////

class TemplateInfos(
        configroot: IRootInfo
) {
    private val json = getTemplatesJson(configroot)
    private val lcCats: List<String> = getLcCats(json)
    private val byLcCat = TreeMap<String, Array<TemplateInfo>>()
    private val byName = TreeMap<String, TemplateInfo>()

    init {
        json.optJSONObject(_TemplatesJSONKey.templates)?.let {
            for (key in it.keySet()) {
                val infos = it.optJSONArray(key) ?: continue
                val tinfos = ArrayList<TemplateInfo>()
                var index = 0
                val len = infos.length()
                while (index < len) {
                    val info = infos.optJSONObject(index)
                    val name = info?.stringOrNull(_TemplatesJSONKey.name)
                    if (name == null) {
                        ++index
                        continue
                    }
                    val tinfo = TemplateInfo(info)
                    byName[name] = tinfo
                    tinfos.add(tinfo)
                    ++index
                }
                val lccat = TextUt.toLowerCase(key)
                byLcCat[lccat] = tinfos.toTypedArray()
            }
        }
        json.optJSONObject(_TemplatesJSONKey.aliases)?.let {
            it.foreachString { key, value ->
                if (value != null) {
                    val info = byName[value]
                    if (info != null) {
                        byName[key] = info
                    }
                }
            }
        }
    }

    private fun getTemplatesJson(configroot: IRootInfo): JSONObject {
        return try {
            val jsonfile = configroot.fileInfo(Paths.templatesJson)
            JSONObject(jsonfile.content().readText())
        } catch (e: Throwable) {
            Conf.e("ERROR: Reading templates.json: " + e.message)
            JSONObject()
        }
    }

    private fun getLcCats(json: JSONObject): List<String> {
        return json.jsonObjectOrFail(_TemplatesJSONKey.aliases).map { it.toLowerCase() }.toList()
    }

    fun getTemplatesJson(ret: JSONObject, key: String): JSONObject {
        return ret.put(key, json)
    }

    fun getInfo(name: String?): TemplateInfo? {
        return if (name == null) null else byName[name]
    }

    fun isLcCat(lccat: String?): Boolean {
        return lcCats.contains(lccat)
    }
}

//////////////////////////////////////////////////////////////////////

/// Allow access to config resources under assets/config.
class ConfigRoot(
        internal val configDir: File
) : ReadOnlyRootBase("") {

    override val name = ""
    override val apath = ""

    override fun fileInfo(rpath: String): IFileInfo {
        val cleanrpath = Support.getcleanrpath(rpath)
        if (cleanrpath != null) {
            val file = configDir.file(cleanrpath)
            if (file.exists()) return ConfigInfo(this, cleanrpath, file)
        }
        return FileInfoUtil.notexists(this, rpath)
    }
}

//////////////////////////////////////////////////////////////////////

class ConfigInfo(
        override val root: ConfigRoot,
        override val rpath: String,
        file: File
) : FileInfoBase(file) {

    private val content = ReadOnlyContent(f.length()) { f.inputStream() }
    override val name = Basepath.nameWithSuffix(rpath)
    override val cpath = rpath
    override val apath get() = File.separatorChar + rpath
    override val parent: IFileInfo? get() = null

    override fun content(): IFileContent {
        return content
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return false
    }

    override fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return root.fileInfo(Basepath.joinRpath(this.rpath, rpath))
    }
}

//////////////////////////////////////////////////////////////////////

/// Allow only use accessible custom resource, currently only client-v1.css.
class CustomResourceRoot(
        internal val getter: Fun11<String, ByteArray>
) : ReadOnlyRootBase("") {

    override val name = ""
    override val apath = ""

    override fun fileInfo(rpath: String): IFileInfo {
        return if (rpath.startsWith(PATH.assetsClient_)) CustomResourceInfo(this, rpath)
        else FileInfoUtil.notexists(this, rpath)
    }
}

//////////////////////////////////////////////////////////////////////

class CustomResourceInfo(
        root: CustomResourceRoot,
        rpath: String
) : ReadOnlyJSONObjectFIleInfo(
        root, rpath, FileInfoUtil.readonly(), emptyMap(),
        ReadOnlyContent(root.getter(rpath).size.toLong()) { root.getter(rpath).inputStream() })

//////////////////////////////////////////////////////////////////////
