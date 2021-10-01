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

import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.ILog
import com.cplusedition.bot.core.RandomUt
import com.cplusedition.bot.core.Serial
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.StructUt
import com.cplusedition.bot.core.listOrEmpty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.util.struct.IterableWrapper
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.SessionPreferencesKey
import sf.andrians.cplusedition.support.css.CSSGenerator
import sf.andrians.cplusedition.support.handler.IResUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

private class RootAttr(
        val locked: Boolean,
        val encrypted: Boolean
)

abstract class StorageBase(
        private val logger: ILog,
        override val rsrc: IResUtil
) : IStorage, IStorageAccessor {

    private val searchResultLock: Lock = ReentrantLock()
    private val searchResults: MutableMap<Long, Array<JSONObject>?> = TreeMap()

    protected var pool = Executors.newSingleThreadExecutor()
    protected var tmpdirThread = Executors.newSingleThreadExecutor()

    override fun onPause() {
        clearCached(true)
    }

    override fun onResume() {
    }

    override fun onDestroy() {
        pool.shutdownNow()
        tmpdirThread.shutdownNow()
        tmpdirThread.awaitTermination(1, SECONDS)
        pool.awaitTermination(1, SECONDS)
    }

    override fun <R> submit(task: Fun11<IStorageAccessor, R>): Future<R> {
        return pool.submit<R> {
            task(this)
        }
    }

    override fun fileInfoAt(cpath: String?): Pair<IFileInfo?, Collection<String>> {
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (cleanrpath == null || cleanrpath.isEmpty()) {
            errors.add(rsrc.get(R.string.InvalidPath_, "$cpath"))
            return Pair(null, errors)
        }
        return Pair(fileInfo(cleanrpath), listOf(rsrc.get(R.string.InvalidPath, "$cpath")))
    }

    override fun documentFileInfoAt(cpath: String?): Pair<IFileInfo?, Collection<String>> {
        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (cleanrpath == null) {
            errors.add(rsrc.get(R.string.InvalidPath, "$cpath"))
            return Pair(null, errors)
        }
        return Pair(documentFileInfo(cleanrpath), listOf(rsrc.get(R.string.InvalidPath, "$cpath")))
    }

    override fun readBytes(cpath: String): ByteArray {
        return fileInfoAt(cpath).first?.content()?.getInputStream()?.use {
            it.readBytes()
        } ?: throw IOException()
    }

    override fun readText(cpath: String, charset: Charset): String {
        return fileInfoAt(cpath).first?.content()?.getInputStream()?.reader(charset)?.use {
            it.readText()
        } ?: throw IOException()
    }

    @Synchronized
    override fun isLoggedIn(): Boolean {
        return true
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

    override fun getSearchResult(id: Long /*, final boolean isregex*/): String {
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
            searchErrors(id, rsrc.get(R.string.InvalidRegex_, "$text"))
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
            if (Basepath.lcSuffix(info.name) != An.DEF.htmlSuffix) continue
            val apath = info.apath
            if (filepat == null || filepat.matcher(apath).find()) {
                try {
                    info.content().getInputStream().use {
                        val content = it.reader().readText()
                        regexSearch(results, pat, apath, content)
                    }
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

    private fun getRegexSearchResult(id: Long): String {
        searchResultLock.lock()
        return try {
            if (!searchResults.containsKey(id)) {
                return rsrc.jsonError(R.string.SearchAborted, ": $id")
            }
            val ret = searchResults[id] ?: return BUSY
            searchResults.remove(id)
            ret[0].toString()
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
            tmpdirThread.submit<File> {
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
        tmpdirThread.execute {
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
        tmpdirThread.execute {
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
            //#IF ENABLE_LOG
            
            val timer = StepWatch()
            //#ENDIF ENABLE_LOG
            resourcesLock.writeLock().lock()
            timestamp = System.currentTimeMillis()
            try {
                resources.clear()
                resources[An.PATH.assetsClientCss] = StructUt.concat(serialize(CSSGenerator.Clientv1CSS(conf).build()), fontscss, symbolscss)
                resources[getHostCss()] = StructUt.concat(serialize(CSSGenerator.HostCSS(conf).build()), fontscss)
                resources[An.PATH.assetsImageCss] = serialize(CSSGenerator.ImageCSS(conf).build())
                resources[An.PATH.assetsAudioCss] = serialize(CSSGenerator.AudioCSS(conf).build())
                resources[An.PATH.assetsPDFCss] = serialize(CSSGenerator.PDFCSS(conf).build())
                resources[An.PATH.assetsVideoCss] = serialize(CSSGenerator.VideoCSS(conf).build())
                resources[An.PATH.assetsCSSEditorCss] = serialize(CSSGenerator.CSSEditorCSS(conf).build())
                resources[An.PATH.assetsGameMindsCss] = serialize(CSSGenerator.GameMindsCSS(conf).build())
                resources[An.PATH.assetsGameSudokuCss] = serialize(CSSGenerator.GameSudokuCSS(conf).build())
                resources[An.PATH.assetsGameMinesCss] = serialize(CSSGenerator.GameMinesCSS(conf).build())
            } finally {
                resourcesLock.writeLock().unlock()
                
            }
        }

        protected fun serialize(stylesheet: String): ByteArray {
            return stylesheet.byteInputStream().readBytes()
        }
    }

    abstract class AbstractFontsCSSGenerator

    companion object {
        private val BUSY = "{ \"" + An.Key.busy + "\": true }"
        private val serial = Serial()

        fun getExistingDocumentDirectory(storage: IStorage, cpath: String): Pair<IFileInfo?, String?> {
            val ret = storage.documentFileInfoAt(cpath).first
                    ?: return Pair(null, storage.rsrc.get(R.string.SourceMustBeUnderDocumentDirectories))
            if (!ret.exists) return Pair(null, storage.rsrc.get(R.string.DestinationNotExists))
            return Pair(ret, null)
        }

        fun getWritableDocumentDirectory(storage: IStorage, cpath: String): Pair<IFileInfo?, String?> {
            val ret = storage.documentFileInfoAt(cpath).first
                    ?: return Pair(null, storage.rsrc.get(R.string.DestinationMustBeUnderDocumentDirectories))
            val stat = ret.stat() ?: return Pair(null, storage.rsrc.get(R.string.DestinationNotExists))
            if (!stat.writable) return Pair(null, storage.rsrc.get(R.string.DestinationNotWritable))
            return Pair(ret, null)
        }

        fun existingFileInfoAt(storage: IStorage, cpath: String): Pair<IFileInfo?, Collection<String>?> {
            val ret = storage.fileInfoAt(cpath).let {
                it.first ?: return Pair(null, it.second)
            }
            if (!ret.exists) return Pair(null, listOf(storage.rsrc.get(R.string.FileNotFound_, cpath)))
            return Pair(ret, null)
        }

        fun updateSessionPreferences(res: IResUtil, accessor: ISettingsStoreAccessor, preferences: JSONObject, update: JSONObject): List<String> {
            val errors = ArrayList<String>()
            for (key in IterableWrapper.wrap(update.keys())) {
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
                        SessionPreferencesKey.symbolFamily
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
                            if (value == null) {
                                preferences.put(key, value)
                            } else {
                                preferences.remove(key)
                            }
                        }
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

    class AssetsRoot(
            assetjson: JSONObject,
            private val provider: Fun11<String, InputStream>
    ) : ReadOnlyRootBase(An.PATH.assets) {

        private val info = AssetFileInfo(this, "", assetjson, createAssetFileInfos(this, assetjson, ""))

        override fun fileInfo(rpath: String): IFileInfo {
            return info.fileInfo(rpath)
        }

        override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
            return info.readDir(ret)
        }

        override fun find(ret: MutableCollection<String>, rpathx: String, pattern: String) {
            val dir = fileInfo(rpathx)
            if (dir.stat()?.isDir != true) return
            val lcpat = pattern.toLowerCase(Locale.ROOT)
            FileInfoUtil.walk2(dir) { file, _ ->
                if (file.name.toLowerCase(Locale.ROOT).contains(lcpat)) {
                    ret.add(file.apath)
                }
            }
        }

        internal fun getInputStream(rpath: String): InputStream {
            return provider(Basepath.joinRpath(name, rpath))
        }

        companion object {
            private val EMPTYROOT = JSONObject()
                    .put(IFileInfo.Key.name, "")
                    .put(IFileInfo.Key.isdir, true)
                    .put(IFileInfo.Key.isfile, false)
                    .put(IFileInfo.Key.notwritable, true)

            fun readAssetsJson(rsrc: IResUtil, assetjson: InputStream): JSONObject {
                try {
                    assetjson.use {
                        return JSONObject(it.reader().readText())
                    }
                } catch (e: Throwable) {
                    
                    return JSONObject()
                }
            }

            private fun createAssetFileInfos(root: AssetsRoot, json: JSONObject, dir: String): Map<String, AssetFileInfo> {
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

            @Throws(JSONException::class)
            fun emptyrootinfo(provider: Fun11<String, InputStream>): AssetsRoot {
                return AssetsRoot(EMPTYROOT, provider)
            }
        }
    }

    /**
     * FileInfo for a readonly asset.
     */
    class AssetFileInfo(
            root: AssetsRoot,
            rpath: String,
            info: JSONObject,
            files: Map<String, AssetFileInfo>
    ) : ReadOnlyJSONObjectFIleInfo(root, rpath, info, files,
            ReadOnlyContent(info.optLong(IFileInfo.Key.length, 0L)) {
                root.getInputStream(rpath)
            })

    //////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
}

