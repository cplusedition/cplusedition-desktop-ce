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

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.anjson.JSONUtil.findsJSONArrayNotNull
import com.cplusedition.anjson.JSONUtil.foreach
import com.cplusedition.anjson.JSONUtil.foreachJSONArrayNotNull
import com.cplusedition.anjson.JSONUtil.foreachJSONObjectNotNull
import com.cplusedition.anjson.JSONUtil.foreachStringNotNull
import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.jsonObjectOrFail
import com.cplusedition.anjson.JSONUtil.jsonObjectOrNull
import com.cplusedition.anjson.JSONUtil.keyList
import com.cplusedition.anjson.JSONUtil.maps
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun00
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.ILog
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.RandomUt
import com.cplusedition.bot.core.ResourceLocker
import com.cplusedition.bot.core.Serial
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.StructUt
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.With
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.listOrEmpty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.SessionPreferencesKey
import sf.andrians.cplusedition.support.An.SettingsKey
import sf.andrians.cplusedition.support.ISecUtil.EtcPaths
import sf.andrians.cplusedition.support.StorageBase.ReadOnlyJSONRoot
import sf.andrians.cplusedition.support.Support.FontInfo
import sf.andrians.cplusedition.support.css.CSSGenerator
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.andrians.org.json.MyJSONTokener
import sf.andrians.org.json.MyStringPool
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipFile

private class RootAttr(
    val locked: Boolean,
    val encrypted: Boolean
)

abstract class StorageBase constructor(
    private val logger: ILog,
    override val rsrc: IResUtil,
) : IStorage {

    private val searchResultLock: Lock = ReentrantLock()
    private val searchResults: MutableMap<Long, Array<JSONObject>?> = TreeMap()

    protected val diskMgr = DiskManager(this)

    override fun onPause() {
        clearCached(true)
        getSettingsStore().invoke { it.saveXrefs() }
    }

    override fun onResume() {
    }

    override fun onDestroy() {
        getSettingsStore().invoke { it.saveXrefs() }
        workerThreadPool.shutdown()
        singleThreadPool.shutdown()
        workerThreadPool.awaitTermination(2, SECONDS)
        singleThreadPool.awaitTermination(1, SECONDS)
        diskMgr.join(2, SECONDS)
    }

    override fun fileInfoAt(cpath: String?): IBotResult<IFileInfo, Collection<String>> {
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (cleanrpath.isNullOrEmpty()) {
            errors.add(rsrc.get(R.string.InvalidPath_, "$cpath"))
            return BotResult.fail(errors)
        }
        return fileInfo(cleanrpath)?.let { BotResult.ok(it) }
            ?: BotResult.fail(listOf(rsrc.get(R.string.InvalidPath, "$cpath")))
    }

    override fun documentFileInfoAt(cpath: String?): IBotResult<IFileInfo, Collection<String>> {
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (cleanrpath == null) {
            errors.add(rsrc.get(R.string.InvalidPath, "$cpath"))
            return BotResult.fail(errors)
        }
        return documentFileInfo(cleanrpath)?.let { BotResult.ok(it) }
            ?: BotResult.fail(listOf(rsrc.get(R.string.InvalidPath, "$cpath")))
    }

    override fun <R> disk(cpath: String, provider: ICloseableProvider<R>): R {
        return diskMgr.lock(cpath, provider)
    }

    override fun postSearch(
        filterignorecase: Boolean,
        filefilter: String,
        searchignorecase: Boolean,
        searchtext: String /*, final boolean isregex*/
    ): Long {
        val id = serial.get()
        postRegexSearch(id, filterignorecase, filefilter, searchignorecase, searchtext)
        return id
    }

    override fun getSearchResult(id: Long /*, final boolean isregex*/): JSONObject {
        return getRegexSearchResult(id)
    }

    private fun postRegexSearch(id: Long, filterignorecase: Boolean, filefilter: String, searchignorecase: Boolean, text: String) {
        val roots = mutableListOf(getAssetsRoot(), getHomeRoot() /* , getPrivateRoot() */)
        var filepat: Pattern? = null
        if (filefilter.isNotEmpty()) {
            try {
                filepat = Pattern.compile(filefilter, if (filterignorecase) Pattern.CASE_INSENSITIVE else 0)
            } catch (e: PatternSyntaxException) {
                searchErrors(id, rsrc.get(R.string.InvalidRegex_, filefilter), e.message ?: "")
                return
            }
        }
        if (text.isEmpty()) {
            searchErrors(id, rsrc.get(R.string.InvalidRegex_, text))
            return
        }
        val pat: Pattern
        try {
            pat = Pattern.compile(text, if (searchignorecase) Pattern.CASE_INSENSITIVE else 0)
        } catch (e: PatternSyntaxException) {
            searchErrors(id, rsrc.get(R.string.InvalidRegex_, text), "${e.message}")
            return
        }
        val storage: IStorage = this
        searchResultLock.lock()
        try {
            searchResults[id] = null
            Thread(
                Runnable {
                    val results = JSONObject()
                    for (root in roots) {
                        regexSearch(results, storage, filepat, pat, root)
                    }
                    val ret = JSONObject()
                    ret.putOrFail(An.Key.result, results)
                    searchResultLock.lock()
                    if (searchResults.containsKey(id)) {
                        searchResults[id] = arrayOf(ret)
                    }
                    searchResultLock.unlock()
                }).start()
        } finally {
            searchResultLock.unlock()
        }
    }

    protected fun regexSearch(
        results: JSONObject, storage: IStorage, filepat: Pattern?, pat: Pattern, dir: IFileInfo
    ) {
        for (info in dir.readDir(ArrayList())) {
            val stat = info.stat()
            if (stat == null || !stat.readable) continue
            if (stat.isDir) {
                regexSearch(results, storage, filepat, pat, info)
                continue
            }
            if (Basepath.lcSuffix(info.name) != Suffix.HTML) continue
            val apath = info.apath
            if (filepat == null || filepat.matcher(apath).find()) {
                try {
                    regexSearch(results, pat, apath, IOUt.readText(info.content().inputStream()))
                } catch (e: Exception) {
                    logger.w(rsrc.get(R.string.ErrorReading_, apath), e)
                }
            }
        }
    }

    fun regexSearch(results: JSONObject, pat: Pattern, apath: String, content: String) {
        val m = pat.matcher(content)
        val stat = JSONArray()
        while (m.find()) {
            stat.put(m.start())
        }
        if (stat.length() > 0) {
            results.putOrFail(apath, stat)
        }
    }

    private fun getRegexSearchResult(id: Long): JSONObject {
        searchResultLock.lock()
        return try {
            if (!searchResults.containsKey(id)) {
                return rsrc.jsonObjectResult(rsrc.get(R.string.SearchAborted, ": $id"))
            }
            val ret = searchResults[id] ?: return BUSY
            searchResults.remove(id)
            ret[0]
        } finally {
            searchResultLock.unlock()
        }
    }

    private fun searchErrors(id: Long, vararg msgs: String) {
        searchResultLock.lock()
        try {
            if (searchResults.containsKey(id)) {
                val ret = JSONObject()
                if (msgs.size == 1) {
                    ret.put(An.Key.errors, msgs[0])
                } else {
                    ret.put(An.Key.errors, JSONArray(msgs))
                }
                searchResults[id] = arrayOf(ret)
            }
        } catch (e: JSONException) {
            logger.w("# ASSERT: should not happen", e)
        } finally {
            searchResultLock.unlock()
        }
    }

    /**
     * @return A temp file with the given filename in a random named directory in the given tmpdir.
     */
    protected fun gettemp(tmpdir: File, filename: String?): File? {
        return try {
            singleThreadPool.submit<File> {
                try {
                    val dir = File.createTempFile("tmp", "", tmpdir)
                    if (!dir.delete()) return@submit null
                    if (!dir.mkdirs()) return@submit null
                    val file = if (filename == null) dir else File(dir, filename)
                    
                    return@submit file
                } catch (e: Throwable) {
                    return@submit null
                }
            }.get()
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    /**
     * Remove all file/directory recursively directly under the tmpdir with name that is not excluded by excludes.
     */
    protected fun cleartemp(tmpdir: File, excludes: Collection<String?>?) {
        singleThreadPool.execute {
            if (tmpdir.exists()) {
                for (name in tmpdir.listOrEmpty()) {
                    if (excludes != null && excludes.contains(name)) {
                        continue
                    }
                    
                    delete_(File(tmpdir, name))
                }
            }
        }
    }

    /**
     * Delete the file/directory recursively directly under the tmpdir with the given name.
     * Do nothing if not exists.
     */
    protected fun deletetemp(tmpdir: File?, name: String?) {
        if (name != null) {
            
            val file = File(tmpdir, name)
            delete_(file)
        }
    }

    protected fun deletetemp(tmpfile: File) {
        val dir = tmpfile.parentFile ?: return
        
        delete_(dir)
    }

    private fun delete_(file: File) {
        singleThreadPool.execute {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    abstract class AbstractCustomResources {
        protected val resources: MutableMap<String, ByteArray> = TreeMap()
        protected val resourcesLock: ReadWriteLock = ReentrantReadWriteLock()
        private val hostcss: String = An.PATH.assetsCss_ + "host" + RandomUt.getLong() + ".css"
        private var timestamp: Long = 0

        protected open fun getHostCss(): String {
            return hostcss
        }

        protected open fun getTimestamp(): Long {
            return timestamp
        }

        /**
         * @param rpath Context relative path without leading /.
         */
        fun asBytes(rpath: String): ByteArray? {
            resourcesLock.readLock().lock()
            try {
                return resources[rpath]
            } finally {
                resourcesLock.readLock().unlock()
            }
        }

        protected fun generateCss(conf: CSSGenerator.IConf, fontscss: ByteArray, symbolscss: ByteArray) {
            resourcesLock.writeLock().lock()
            timestamp = System.currentTimeMillis()
            try {
                resources.clear()
                resources[An.PATH.assetsClientCss] =
                    StructUt.concat(serialize(CSSGenerator.Clientv1CSS(conf).build()), fontscss, symbolscss)
                resources[getHostCss()] = StructUt.concat(serialize(CSSGenerator.HostCSS(conf).build()), fontscss)
                resources[An.PATH.assetsImageCss] = serialize(CSSGenerator.ImageCSS(conf).build())
                resources[An.PATH.assetsCSSEditorCss] = serialize(CSSGenerator.CSSEditorCSS(conf).build())
                resources[An.PATH.assetsGameMindsCss] = serialize(CSSGenerator.GameMindsCSS(conf).build())
                resources[An.PATH.assetsGameSudokuCss] = serialize(CSSGenerator.GameSudokuCSS(conf).build())
                resources[An.PATH.assetsGameMinesCss] = serialize(CSSGenerator.GameMinesCSS(conf).build())
            } finally {
                resourcesLock.writeLock().unlock()
                
            }
        }

        protected fun serialize(stylesheet: String): ByteArray {
            return stylesheet.byteInputStream().use { it.readBytes() }
        }
    }

    abstract class AbstractFontsCSSGenerator

    protected object FontsCSSGenerator : AbstractFontsCSSGenerator() {

        private var fonts: JSONObject? = null
        private var fontsCSS: ByteArray? = null
        private var symbolsCSS: ByteArray? = null

        fun readFontSettings(settings: JSONObject, configroot: IRootInfo, systemfonts: JSONObject?) {
            val timer = StepWatch()
            
            try {
                val fonts = readFontsJSON(configroot)
                val fonts2 = fonts.getJSONObject(FontInfo.Key.fonts2)
                val fonts0 = fonts.getJSONObject(FontInfo.Key.fonts0)
                val cats0 = fonts.getJSONArray(FontInfo.Key.cats0)
                if (systemfonts != null && systemfonts.length() > 0) {
                    systemfonts.foreachJSONObjectNotNull { key, info ->
                        fonts0.put(key, info)
                    }
                    cats0.put("System")
                }
                
                
                
                settings.putOpt(SettingsKey.symbolFamilies, fonts2)
                settings.putOpt(SettingsKey.fontFamilies, fonts0)
                settings.putOpt(SettingsKey.fontCategories, cats0)
            } catch (e: Throwable) {
                
            } finally {
                
            }
        }

        fun getFontsCSS(settingsObject: JSONObject): ByteArray {
            return fontsCSS ?: generateFontsCss(settingsObject).also { fontsCSS = it }
        }

        fun getSymbolsCSS(confroot: IRootInfo): ByteArray {
            return symbolsCSS ?: generateSymbolsCss(confroot).also { symbolsCSS = it }
        }

        private fun generateFontsCss(settingsObject: JSONObject): ByteArray {
            try {
                return ByteArrayOutputStream().use { output ->
                    PrintStream(output, false, "UTF-8").use { out ->
                        settingsObject.optJSONObject(SettingsKey.fontFamilies)?.foreachJSONObjectNotNull { _, info ->
                            info.optJSONArray(FontInfo.Key.fontfaces)?.foreachStringNotNull { _, fontface ->
                                out.println(fontface)
                            }
                        }
                    }
                    output
                }.toByteArray()
            } catch (e: Throwable) {
                
                return ByteArray(0)
            }
        }

        private fun generateSymbolsCss(confroot: IRootInfo): ByteArray {
            try {
                return confroot.fileInfo(ConfigPaths.fontawesomeCss).content().readBytes()
            } catch (e: Throwable) {
                
                return ByteArray(0)
            }
        }

        @Throws(IOException::class)
        private fun readFontsJSON(confroot: IRootInfo): JSONObject {
            fonts?.let { return it }
            return try {
                confroot.fileInfo(ConfigPaths.fontsJson).content().inputStream().bufferedReader().use {
                    MyJSONTokener(it, stringPool).readObject()
                }
            } catch (e: Throwable) {
                JSONObject()
            }.also { fonts = it }
        }

    }

    companion object {
        private val BUSY = JSONObject().put(An.Key.busy, true)
        private val serial = Serial()
        private val stringPool = MyStringPool()

        fun getExistingDocumentDirectory(storage: IStorage, cpath: String): IBotResult<IFileInfo, String> {
            val ret = storage.documentFileInfoAt(cpath).result()
                ?: return BotResult.fail(storage.rsrc.get(R.string.SourceMustBeUnderDocumentDirectories))
            if (!ret.exists) return BotResult.fail(storage.rsrc.get(R.string.DestinationNotExists))
            return BotResult.ok(ret)
        }

        fun getWritableDirectory(storage: IStorage, cpath: String): IBotResult<IFileInfo, String> {
            return storage.fileInfoAt(cpath).ifOK { fileinfo ->
                val stat = fileinfo.stat() ?: return@ifOK BotResult.fail(storage.rsrc.get(R.string.DestinationNotExists))
                if (!stat.isDir) BotResult.fail(storage.rsrc.get(R.string.DestinationExpectingADir))
                else if (!stat.writable) BotResult.fail(storage.rsrc.get(R.string.DestinationNotWritable_, cpath))
                else BotResult.ok(fileinfo)
            } ?: BotResult.fail(storage.rsrc.get(R.string.invalidFilepath_, cpath))
        }

        fun existingFileInfoAt(storage: IStorage, cpath: String): IBotResult<IFileInfo, Collection<String>> {
            return storage.fileInfoAt(cpath).mapOK { fileinfo ->
                if (!fileinfo.exists) BotResult.fail(listOf(storage.rsrc.get(R.string.FileNotFound_, cpath)))
                else BotResult.ok(fileinfo)
            }
        }

        fun updateSessionPreferences(
            res: IResUtil,
            accessor: ISettingsStoreAccessor,
            preferences: JSONObject,
            update: JSONObject
        ): List<String> {
            val errors = ArrayList<String>()
            for (key in update.keys()) {
                try {
                    when (key) {
                        SessionPreferencesKey.filePositions -> {
                            val value = update.jsonArrayOrNull(key)
                            if (value != null) {
                                val cpath = value.stringOrNull(0)
                                val position = value.jsonArrayOrNull(1)
                                if (cpath != null && position != null) accessor.updateFilePosition(cpath, position)
                            }
                        }

                        SessionPreferencesKey.gameMinesSave,
                        SessionPreferencesKey.gameSudokuSave,
                        SessionPreferencesKey.gameMindsSave,
                        SessionPreferencesKey.symbolFamily,
                        SessionPreferencesKey.imageDefaultOutputFormat,
                        SessionPreferencesKey.photoDefaultOutputFormat,
                        -> {
                            val value = update.stringOrNull(key)
                            if (value != null) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }

                        SessionPreferencesKey.symbolRecents -> {
                            val value = update.optJSONArray(key)
                            if (value != null) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }

                        SessionPreferencesKey.gameSudokuAutofill,
                        SessionPreferencesKey.showDoneEvents -> {
                            val value = update.optBoolean(key, false)
                            if (value) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }

                        SessionPreferencesKey.trashAutoCleanupTimestamp -> {
                            val value = update.optLong(key, 0)
                            if (value > 0L) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }

                        SessionPreferencesKey.gameMinesMapSize,
                        SessionPreferencesKey.gameMinesDifficulty,
                        SessionPreferencesKey.gameMindsCells,
                        SessionPreferencesKey.gameMindsDigits,
                        SessionPreferencesKey.gameSudokuDifficulty,
                        SessionPreferencesKey.gameSudokuAssists,
                        SessionPreferencesKey.imageDefaultOutputSize,
                        SessionPreferencesKey.imageDefaultOutputQuality,
                        SessionPreferencesKey.photoDefaultOutputSize,
                        SessionPreferencesKey.photoDefaultOutputQuality,
                        SessionPreferencesKey.sidepanelWidth
                        -> {
                            val value = update.optInt(key, -1)
                            if (value >= 0) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }

                        else -> {
                            val msg = res.format(R.string.invalidNameValue, key, update.opt(key))
                            
                            errors.add(msg)
                        }
                    }
                } catch (e: Exception) {
                    val msg = res.get(R.string.ErrorUpdatingSettings, key, "${update.opt(key)}")
                    
                    errors.add(msg)
                }
            }
            return errors
        }

    }

    class AssetsRoot constructor(
        assetjson: JSONObject,
        private val provider: Fun11<String, InputStream>
    ) : ReadOnlyJSONRoot(assetjson, An.PATH.assets, AssetsRoot::createAssetFileInfos) {
        override fun inputStream(rpath: String): InputStream {
            return provider(rpath)
        }

        companion object {
            fun readAssetsJson(rsrc: IResUtil, assetjson: InputStream): JSONObject {
                try {
                    assetjson.bufferedReader().use {
                        return MyJSONTokener(it, stringPool).readObject()
                    }
                } catch (e: Throwable) {
                    
                    return JSONObject()
                }
            }

            private fun createAssetFileInfos(root: ReadOnlyJSONRoot, json: JSONObject, dir: String): Map<String, AssetFileInfo> {
                val a = json.optJSONArray(IFileInfo.Key.files) ?: return emptyMap()
                json.remove(IFileInfo.Key.files)
                val ret = TreeMap<String, AssetFileInfo>()
                for (index in 0 until a.length()) {
                    val info = a.optJSONObject(index)
                    val name = info.stringOrNull(IFileInfo.Key.name) ?: continue
                    val rpath = Basepath.joinRpath(dir, name)
                    val files = createAssetFileInfos(root, info, rpath)
                    ret[name] = AssetFileInfo(root, rpath, info, files)
                }
                return ret
            }
        }
    }

    abstract class ReadOnlyJSONRoot constructor(
        jsontree: JSONObject,
        name: String,
        infotreecreator: Fun31<ReadOnlyJSONRoot, JSONObject, String, Map<String, AssetFileInfo>>,
    ) : ReadOnlyRootBase(name) {

        private val tree = AssetFileInfo(this, "", jsontree, infotreecreator(this, jsontree, ""))

        override val file: File? get() = null

        override fun stat(): IFileStat {
            return tree.stat()!!
        }

        internal abstract fun inputStream(rpath: String): InputStream

        fun jsonInfo(rpath: String): JSONObject? {
            return tree.jsonInfo(rpath)
        }

        override fun fileInfo(rpath: String): IFileInfo {
            return tree.fileInfo(rpath)
        }

        override fun <T : MutableCollection<String>> listDir(ret: T): T {
            return tree.listDir(ret)
        }

        override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
            return tree.readDir(ret)
        }

        override fun find(ret: MutableCollection<String>, subdir: String, searchtext: String) {
            val lcpat = searchtext.lowercase(Locale.ROOT)
            val dir = fileInfo(subdir)
            dir.walk2 { file, _ ->
                if (file.name.lowercase(Locale.ROOT).contains(lcpat)) {
                    ret.add(file.apath)
                }
            }
        }

        companion object {
            private val EMPTYROOT = JSONObject()
                .put(IFileInfo.Key.name, "")
                .put(IFileInfo.Key.isdir, true)
                .put(IFileInfo.Key.isfile, false)
                .put(IFileInfo.Key.notwritable, true)

            @Throws(JSONException::class)
            fun emptyrootinfo(provider: Fun11<String, InputStream>): AssetsRoot {
                return AssetsRoot(EMPTYROOT, provider)
            }

            fun createFileInfoTree(root: ReadOnlyJSONRoot, json: JSONObject, dir: String): Map<String, AssetFileInfo> {
                val a = json.optJSONObject(IFileInfo.Key.files)
                    ?: return emptyMap()
                json.remove(IFileInfo.Key.files)
                val ret = TreeMap<String, AssetFileInfo>()
                for (key in a.keys()) {
                    val info = a.optJSONObject(key)
                        ?: continue
                    val name = info.stringOrNull(IFileInfo.Key.name)
                        ?: continue
                    if (info.has(IFileInfo.Key.offset)
                        && info.optBoolean(IFileInfo.Key.isfile)
                        && info.optLong(IFileInfo.Key.offset, 0L) <= 0L
                    ) continue
                    val rpath = Basepath.joinRpath(dir, name)
                    val files = createFileInfoTree(root, info, rpath)
                    ret[name] = AssetFileInfo(root, rpath, info, files)
                }
                return ret
            }
        }
    }

    /**
     * FileInfo for a readonly asset.
     */
    class AssetFileInfo(
        root: ReadOnlyJSONRoot,
        rpath: String,
        info: JSONObject,
        files: Map<String, AssetFileInfo>
    ) : ReadOnlyJSONObjectFIleInfo(
        root, rpath, info, files,
        ReadOnlyContent({
            info.optLong(IFileInfo.Key.length, 0L)
        }, {
            root.inputStream(rpath)
        })
    )

    //////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////

    object ConfigPaths {
        const val assetsJson = "assets.json"
        const val fontsJson = "fonts.json"
        const val templatesJson = "templates.json"
        const val fontawesomeCss = "fontawesome.css"
        fun isValid(rpath: String): Boolean {
            return rpath == assetsJson || rpath == fontawesomeCss || rpath == fontsJson || rpath == templatesJson
        }
    }

    protected abstract class StorageAccessorBase(
        private val storage: IStorage
    ) : IStorageAccessor {
        override fun readBytes(cpath: String): ByteArray {
            return storage.fileInfoAt(cpath).result()?.content()?.readBytes() ?: throw IOException()
        }

        override fun readText(cpath: String, charset: Charset): String {
            return storage.fileInfoAt(cpath).result()?.content()?.readText() ?: throw IOException()
        }

        override fun <T : MutableCollection<String>> find(ret: T, fromdir: String, searchtext: String): T {
            if (fromdir.isEmpty() || fromdir == "/") {
                for (root in storage.getRoots()) {
                    root.find(ret, "", searchtext)
                }
            } else {
                storage.fileInfoAt(fromdir).result()?.let { fileinfo ->
                    fileinfo.root.find(ret, fileinfo.rpath, searchtext)
                }
            }
            return ret
        }

        override fun getXrefs(to: IFileInfo): List<IFileInfo> {
            val ret = ArrayList<IFileInfo>()
            val cpath = to.cpath
            storage.getSettingsStore().invoke {
                it.getXrefs { key, value ->
                    if (value?.has(cpath) == true) {
                        storage.fileInfoAt(key).ifOK {
                            ret.add(it)
                        }
                    }
                }
            }
            return ret
        }

        override fun rebuildXrefs(at: IFileInfo?): JSONObject {

            return try {
                val xrefs = TreeMap<String, Collection<String>>()
                val roots = if (at == null)
                    arrayOf(storage.getHomeRoot(), storage.getInternalBackupRoot(), storage.getAssetsRoot())
                else arrayOf(at)
                Without.throwableOrNull {
                    for (root in roots) {
                        XrefUt.buildXrefs(xrefs, root)
                    }
                }
                storage.getSettingsStore().invoke {
                    for (root in roots) {
                        it.deleteXrefsFrom(root)
                    }
                    it.updateXrefs(xrefs)
                }
                storage.rsrc.jsonObjectResult(xrefs.size)
            } catch (e: Throwable) {
                storage.rsrc.jsonObjectError(R.string.CommandFailed)
            }
        }
    }

    protected class SettingsStore constructor(
        private val accessor: ISettingsStoreAccessor
    ) : ISettingsStore {
        private val lock = ReentrantLock()
        override fun <T> invoke(code: Fun11<ISettingsStoreAccessor, T>): T {
            return With.lock(lock) { code(accessor) }
        }
    }

    protected interface ISettingsStorageAccessorDelegate {
        val createDefaultSettings: Fun01<JSONObject>
        fun findSystemFonts(): JSONObject
    }

    protected class SettingStoreAccessor constructor(
        private val rsrc: IResUtil,
        private val configRoot: IRootInfo,
        private val secUtil: ISecUtil,
        private val delegate: ISettingsStorageAccessorDelegate,
    ) : ISettingsStoreAccessor {
        private val templatesInfos = TemplateInfos(configRoot)
        private var settings = initSettings()
        private var preferences = initPreferences()
        private var filePositions = initFilePositions()
        private var xrefs: JSONObject
        private var hasXrefs: Boolean

        init {
            val json = readXrefs()
            hasXrefs = json != null
            xrefs = json ?: JSONObject()
        }

        override fun reset() {
            secUtil.invoke {
                it.deleteCf(EtcPaths.settingsCf)
                it.deleteCf(EtcPaths.preferencesCf)
                it.deleteCf(EtcPaths.xrefsCf)
            }
            settings = initDefaultSettings()
            preferences = initDefaultPreferences()
            xrefs = JSONObject()
            hasXrefs = false
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

        override fun getSession(): JSONObject {
            return secUtil.invoke {
                try {
                    it.getCf(EtcPaths.sessionCf)?.bufferedReader()?.let {
                        MyJSONTokener(it, stringPool).readObject()
                    } ?: JSONObject()
                } catch (e: Throwable) {
                    
                    it.deleteCf(EtcPaths.sessionCf)
                    JSONObject()
                }
            }
        }

        override fun saveSession(session: JSONObject) {
            secUtil.invoke {
                try {
                    it.putCf(EtcPaths.sessionCf)?.bufferedWriter()?.use {
                        JSONUtil.write(it, session)
                    }
                } catch (e: Throwable) {
                    
                    it.deleteCf(EtcPaths.sessionCf)
                }
            }
        }

        /// Note that changes to preferences must goes through updatePreferences().
        override fun getPreferences(): JSONObject {
            return preferences
        }

        override fun getEvents(): JSONObject? {
            return secUtil.invoke {
                try {
                    it.getCf(EtcPaths.eventsCf)?.bufferedReader()?.let {
                        MyJSONTokener(it, stringPool).readObject()
                    }
                } catch (e: Throwable) {
                    
                    it.deleteCf(EtcPaths.eventsCf)
                    null
                }
            }
        }

        override fun deleteEvents() {
            secUtil.invoke {
                it.deleteCf(EtcPaths.eventsCf)
            }
        }

        override fun saveXrefs() {
            try {
                writeXrefs(xrefs)
            } catch (e: Throwable) {
                
            }
        }

        override fun updateSettings(res: IResUtil, update: JSONObject): List<String> {
            val errors = Support.updateUISettings(res, getCurrentSettings(), update)
            saveSettings(getCurrentSettings())
            return errors
        }

        @Throws(Exception::class)
        override fun updatePreferences(res: IResUtil, update: JSONObject): List<String> {
            val errors = updateSessionPreferences(res, this, preferences, update)
            savePreferences(preferences)
            return errors
        }

        override fun getTemplatesJson(ret: JSONObject, key: String): JSONObject {
            return templatesInfos.getTemplatesJson(ret, key)
        }

        override fun getTemplateInfo(name: String?): TemplateInfo? {
            return templatesInfos.getInfo(name)
        }

        override fun updateFilePosition(cpath: String, position: JSONArray) {
            filePositions.findsJSONArrayNotNull { index, value -> if (value.stringOrNull(0) == cpath) index else null }
                ?.let { filePositions.remove(it) }
            if (filePositions.length() > An.DEF.recentFilePositionCount) filePositions.remove(0)
            filePositions.put(JSONArray().put(cpath).put(position))
            savePreferences(preferences)
        }

        override fun getFilePosition(cpath: String): JSONArray? {
            return filePositions.findsJSONArrayNotNull { _, value ->
                if (value.stringOrNull(0) == cpath) value.jsonArrayOrNull(1) else null
            }
        }

        private fun initFilePositions(): JSONArray {
            return getPreferences().jsonArrayOrNull(SessionPreferencesKey.filePositions) ?: JSONArray().also {
                getPreferences().put(SessionPreferencesKey.filePositions, it)
            }
        }

        private fun initPreferences(): JSONObject {
            return Without.exceptionOrNull { readPreferences() } ?: initDefaultPreferences()
        }

        private fun initDefaultPreferences(): JSONObject {
            return Without.exceptionOrNull { readPreferences() } ?: JSONObject()
                .put(SessionPreferencesKey.symbolFamily, An.DEF.FontAwesome)
                .put(SessionPreferencesKey.showDoneEvents, true)
        }

        override fun hasXrefs(): Boolean {
            return hasXrefs
        }

        override fun getXrefs(callback: Fun20<String, JSONObject?>) {
            xrefs.foreach { callback(it, xrefs.jsonObjectOrNull(it)) }
        }

        override fun getXrefsFrom(apath: String): Collection<String>? {
            return xrefs.optJSONObject(apath)?.keyList()
        }

        override fun getXrefsTo(apath: String): Collection<String>? {
            val ret = TreeSet<String>()
            xrefs.foreach {
                if (xrefs.jsonObjectOrNull(it)?.has(apath) == true) {
                    ret.add(it)
                }
            }
            return if (ret.size > 0) ret else null
        }

        override fun deleteXrefsFrom(from: IFileInfo) {
            val apath = from.apath
            if (from.isDir) {
                val prefix = apath + FS
                for (key in xrefs.keys().asSequence().toList()) {
                    if (key.startsWith(prefix))
                        xrefs.remove(key)
                }
            } else {
                xrefs.remove(apath)
            }
        }

        override fun renameXrefsFrom(to: String, from: String) {
            xrefs.remove(from)?.let {
                xrefs.put(to, it)
            }
        }

        override fun updateXrefs(xrefs: Map<String, Collection<String>>?) {
            xrefs?.entries?.forEach {
                updatexrefs_(it.key, it.value)
            }
        }

        override fun updateXrefs(from: String, xrefs: Collection<String>?) {
            updatexrefs_(from, xrefs)
        }

        //////////////////////////////////////////////////////////////////////

        private fun initSettings(): JSONObject {
            val ret = initDefaultSettings()
            Without.exceptionOrNull {
                readSettings()?.let {
                    val current = ret.getJSONObject(SettingsKey.current)
                    for (key in it.keys()) {
                        if (current.has(key)) current.putOpt(key, it.get(key))
                    }
                }
            }
            return ret
        }

        private fun initDefaultSettings(): JSONObject {
            try {
                val settings = JSONObject()
                settings.put(SettingsKey.defaults, delegate.createDefaultSettings())
                settings.put(SettingsKey.current, delegate.createDefaultSettings())
                getFontSettings(settings, configRoot)
                StylesGenerator.updateStylesSettings(settings, rsrc)
                templatesInfos.getTemplatesJson(settings, SettingsKey.htmlTemplates)
                return settings
            } catch (e: Throwable) {
                Support.e("ERROR: initDefaultSettings(): " + e.message);
                return JSONObject()
            }
        }

        private fun getFontSettings(settings: JSONObject, configroot: IRootInfo) {
            return FontsCSSGenerator.readFontSettings(settings, configroot, delegate.findSystemFonts())
        }

        private fun updatexrefs_(from: String, tos: Collection<String>?) {
            try {
                this.xrefs.remove(from)
                if (tos == null) return
                val value = JSONObject()
                tos.forEach { to ->
                    val v = stringPool.intern(to)
                    value.put(v, v)
                }
                this.xrefs.put(stringPool.intern(from), value)
            } catch (e: Throwable) {
                
                deleteXrefs()
            }
        }

        private fun readXrefs(): JSONObject? {
            return secUtil.invoke {
                try {
                    it.getCf(EtcPaths.xrefsCf)?.use { input ->
                        GZIPInputStream(input).bufferedReader().use {
                            MyJSONTokener(it, stringPool).readObject()
                        }
                    }
                } catch (e: Throwable) {
                    
                    deletexrefs(it)
                    null
                }
            }
        }

        private fun writeXrefs(data: JSONObject) {
            secUtil.invoke {
                try {
                    it.putCf(EtcPaths.xrefsCf).use { out ->
                        GZIPOutputStream(out).bufferedWriter().use { w ->
                            JSONUtil.write(w, data)
                        }
                    }
                } catch (e: Throwable) {
                    
                    deletexrefs(it)
                }
            }
        }

        private fun deleteXrefs() {
            secUtil.invoke {
                deletexrefs(it)
            }
        }

        private fun deletexrefs(it: ISecUtilAccessor) {
            Without.exceptionOrNull {
                it.deleteCf(EtcPaths.xrefsCf)
            }
        }

        private fun readPreferences(): JSONObject? {
            return secUtil.invoke {
                try {
                    it.getCf(EtcPaths.preferencesCf)?.bufferedReader()?.let {
                        MyJSONTokener(it, stringPool).readObject()
                    }
                } catch (e: Throwable) {
                    
                    deleteprefs(it)
                    null
                }
            }
        }

        private fun savePreferences(preferences: JSONObject) {
            secUtil.invoke {
                try {
                    it.putCf(EtcPaths.preferencesCf)?.bufferedWriter()?.use {
                        JSONUtil.write(it, preferences)
                    }
                } catch (e: Throwable) {
                    
                    deleteprefs(it)
                }
            }
        }

        private fun deleteprefs(it: ISecUtilAccessor) {
            Without.exceptionOrNull {
                it.deleteCf(EtcPaths.preferencesCf)
            }
        }

        private fun readSettings(): JSONObject? {
            return secUtil.invoke {
                try {
                    it.getCf(EtcPaths.settingsCf)?.bufferedReader()?.use {
                        MyJSONTokener(it, stringPool).readObject()
                    }
                } catch (e: Throwable) {
                    
                    deletesettings(it)
                    null
                }
            }
        }

        private fun saveSettings(current: JSONObject) {
            secUtil.invoke {
                try {
                    it.putCf(EtcPaths.settingsCf)?.bufferedWriter()?.use {
                        JSONUtil.write(it, current)
                    }
                } catch (e: Throwable) {
                    
                    deletesettings(it)
                }
            }
        }

        private fun deletesettings(it: ISecUtilAccessor) {
            Without.exceptionOrNull {
                it.deleteCf(EtcPaths.settingsCf)
            }
        }
    }

    protected class TemplateInfos(
        configroot: IRootInfo
    ) {
        private val json = getTemplatesJson(configroot)
        private val lcCats: List<String> = getLcCats(json)
        private val byLcCat = TreeMap<String, Array<TemplateInfo>>()
        private val byName = TreeMap<String, TemplateInfo>()

        init {
            json.optJSONObject(An._TemplatesJSONKey.templates)?.foreachJSONArrayNotNull { cat, infos ->
                val tinfos = ArrayList<TemplateInfo>()
                infos.foreachJSONObjectNotNull { _, info ->
                    info.stringOrNull(An._TemplatesJSONKey.name)?.let { name ->
                        val tinfo = TemplateInfo(info)
                        byName[name] = tinfo
                        tinfos.add(tinfo)
                    }
                }
                if (tinfos.size > 0) {
                    val lccat = TextUt.toLowerCase(cat)
                    byLcCat[lccat] = tinfos.toTypedArray()
                    byName[lccat] = tinfos[0]
                }
            }
            json.optJSONObject(An._TemplatesJSONKey.aliases)?.foreachStringNotNull { key, value ->
                byName[value]?.let {
                    byName[key] = it
                }
            }
        }

        private fun getTemplatesJson(configroot: IRootInfo): JSONObject {
            return try {
                configroot.fileInfo(StorageBase.ConfigPaths.templatesJson).content().inputStream().bufferedReader().use {
                    MyJSONTokener(it, stringPool).readObject()
                }
            } catch (e: Throwable) {
                Support.e("ERROR: Reading templates.json: " + e.message);
                JSONObject()
            }
        }

        private fun getLcCats(json: JSONObject): List<String> {
            return json.jsonObjectOrFail(An._TemplatesJSONKey.aliases).maps { it.lowercase(Locale.ROOT) }.toList()
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

    object StylesGenerator {
        fun updateStylesSettings(settings: JSONObject, rsrc: IResUtil) {
            try {
                val styles = readStyles(rsrc)
                settings.put(SettingsKey.charStyles, styles.getJSONArray(SettingsKey.charStyles))
                settings.put(SettingsKey.highlightStyles, styles.getJSONArray(SettingsKey.highlightStyles))
                settings.put(SettingsKey.paraStyles, styles.getJSONArray(SettingsKey.paraStyles))
                settings.put(SettingsKey.builtinCharStyles, styles.getJSONArray(SettingsKey.builtinCharStyles))
                settings.put(SettingsKey.builtinParaStyles, styles.getJSONArray(SettingsKey.builtinParaStyles))
                settings.put(SettingsKey.bgImgSamples, styles.getJSONArray(SettingsKey.bgImgSamples))
            } catch (e: Throwable) {
                Support.e("ERROR: Getting font families: " + e.message);
            }
        }

        @Synchronized
        private fun readStyles(rsrc: IResUtil): JSONObject {
            try {
                return CSSGenerator.StylesJSON(rsrc).build()
            } catch (e: Throwable) {
                Support.w("ERROR: Reading styles: " + e.message);
                return JSONObject()
            }
        }
    }

//////////////////////////////////////////////////////////////////////

    /// Allow only use accessible custom resource, currently only client-v1.css.
    class CustomResourceRoot(
        internal val getter: Fun11<String, ByteArray>
    ) : ReadOnlyRootBase("") {

        override val name = ""
        override val apath = ""

        override val file: File? get() = null

        override fun fileInfo(rpath: String): IFileInfo {
            return if (rpath.startsWith(An.PATH.assetsClient_)) CustomResourceInfo(this, rpath)
            else FileInfoUtil.createNotexists(this, rpath)
        }

        override fun <T : MutableCollection<String>> listDir(ret: T): T {
            throw UnsupportedOperationException()
        }

        override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
            throw UnsupportedOperationException()
        }
    }

    class CustomResourceInfo(
        root: CustomResourceRoot, rpath: String
    ) : ReadOnlyJSONObjectFIleInfo(
        root,
        rpath,
        FileInfoUtil.readonly(),
        emptyMap(),
        ReadOnlyContent({
            root.getter(rpath).size.toLong()
        }, {
            root.getter(rpath).inputStream()
        })
    )

    protected object AssetsPaths {
        const val config = "config"
    }
}

interface ICloseableProvider<R> {
    fun run(closer: Fun00): R
}

open class CloseableOutputStreamProvider constructor(
    private val output: OutputStream,
) : ICloseableProvider<OutputStream> {
    override fun run(closer: Fun00): OutputStream {
        return object : OutputStream() {
            private var closed = false
            override fun write(b: Int) {
                output.write(b)
            }

            override fun write(b: ByteArray) {
                output.write(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                output.write(b, off, len)
            }

            override fun close() {
                if (closed) return
                closed = true
                try {
                    super.close()
                } finally {
                    closer()
                }
            }
        }
    }
}

open class CloseableInputStreamProvider constructor(
    private val input: InputStream,
) : ICloseableProvider<InputStream> {
    override fun run(closer: Fun00): InputStream {
        return object : InputStream() {
            private var closed = false
            override fun read(): Int {
                return input.read()
            }

            override fun read(b: ByteArray): Int {
                return input.read(b)
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                return input.read(b, off, len)
            }

            override fun close() {
                if (closed) return
                closed = true
                try {
                    super.close()
                } finally {
                    closer()
                }
            }
        }
    }
}

class MyCloseable(private val callback: Fun00) : AutoCloseable {
    private var closed = false
    override fun close() {
        if (closed)
            return
        closed = true
        callback()
    }
}

class MyCloseableProvider() : ICloseableProvider<MyCloseable> {
    override fun run(closer: Fun00): MyCloseable {
        return MyCloseable(closer)
    }
}

class DiskManager constructor(
    private val storage: IStorage
) {
    private val locker = ResourceLocker<String>()
    fun <R> lock(key: String, provider: ICloseableProvider<R>): R {
        return locker.async(key) { done ->
            provider.run(done)
        }
    }

    fun join(timeout: Long, timeunit: TimeUnit) {
        locker.join(timeout, timeunit)
    }
}

class ZipFileRoot constructor(
    override val file: File,
    filetree: JSONObject,
    name: String,
    private val onclose: Fun00? = null
) : ReadOnlyJSONRoot(filetree, name, ReadOnlyJSONRoot::createFileInfoTree), ICloseableRootInfo {

    private val zipfile = ZipFile(file)

    override fun inputStream(rpath: String): InputStream {
        val entry = zipfile.getEntry(rpath) ?: throw IOException()
        return zipfile.getInputStream(entry)
    }

    override fun close() {
        FileUt.closeAndIgnoreError(zipfile)
        onclose?.invoke()
    }
}

/// Allow access to config resources under assets/config.
class ConfigRoot constructor(
    private val configdir: IFileInfo
) : ReadOnlyRootBase("") {

    override val name = ""

    override val apath = ""

    override val file: File? get() = null

    override fun fileInfo(rpath: String): IFileInfo {
        if (StorageBase.ConfigPaths.isValid(rpath)) {
            val cleanrpath = Support.getcleanrpath(rpath)
            if (cleanrpath != null) {
                return ReadOnlyFileInfoWrapper(configdir.fileInfo(cleanrpath))
            }
        }
        return FileInfoUtil.createNotexists(this, rpath)
    }

    override fun <T : MutableCollection<String>> listDir(ret: T): T {
        throw UnsupportedOperationException()
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        throw UnsupportedOperationException()
    }
}
