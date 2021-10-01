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

import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.DateUt
import com.cplusedition.bot.core.FilePathCollectors
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun00
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun21
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.Serial
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.listOrEmpty
import com.cplusedition.bot.core.mkparentOrNull
import com.cplusedition.bot.core.ut
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.support.IFileInfo.Key
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

interface IFileStat {
    val isDir: Boolean
    val isFile: Boolean
    val length: Long
    val lastModified: Long
    val perm: String // The file permission string, eg. rw.
    val readable: Boolean
    val writable: Boolean
    val checksumBytes: ByteArray?
}

interface IDeletedFileStat {
    val stat: IFileStat
    val id: Long
    val dir: String
    val name: String
    val isDeleted: Boolean
    val lastDeleted: Long

    @Throws(JSONException::class)
    fun toJSON(): JSONObject
}

interface IFileContent : ISeekableInputStreamProvider {

    @Throws(IOException::class)
    fun getOutputStream(): OutputStream

    @Throws(IOException::class)
    fun readBytes(): ByteArray

    @Throws(IOException::class)
    fun readText(charset: Charset = Charsets.UTF_8): String

    @Throws(IOException::class)
    /// @timestamp If null, current time is used.
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size, timestamp: Long? = null, xrefs: JSONObject? = null)

    @Throws(IOException::class)
    /// @timestamp If null, current time is used.
    fun write(data: InputStream, timestamp: Long? = null, xrefs: JSONObject? = null)

    /// This only works on files. This works across roots.
    @Throws(IOException::class)
    fun copyTo(dst: OutputStream)

    /// This only works on files. This works across roots.
    /// If destination exists, it would be overwritten if it is a file and deletable, otherwise copy fail,
    /// @timestamp If null, current time is used.
    @Throws(IOException::class)
    fun copyTo(dst: IFileInfo, timestamp: Long? = null)

    /// This only works on files. This works across roots.
    /// If destination exists, it would be overwritten if it is a file and deletable, otherwise move fail,
    /// It would try rename src to dst first. If that failed, it would copy and delete the src.
    /// @timestamp If null, current time is used.
    @Throws(IOException::class)
    fun moveTo(dst: IFileInfo, timestamp: Long? = null)

    /// This works on both file and directory but only if src and dst are under the same root.
    /// If src and dst are File, it works exactly as File.renameTo().
    /// In other cases, it may or may not fail if destination already exists.
    /// @timestamp If null, current time is used.
    fun renameTo(dst: IFileInfo, timestamp: Long? = null): Boolean

    /// If data is not null, write data as a deleted file at rpathx, use current time if timestamp is not given.
    /// If data is null create a copy of file at rpathx preserving timestamp if timestamp is not given.
    /// @return true if recovery file is created.
    /// @timestamp If null, current time is used.
    fun writeRecovery(data: InputStream? = null, timestamp: Long? = null): Boolean
}

interface IFileInfo {
    object Key {
        //#BEGIN IFileInfo.Key
        val checksum = "cs"
        val cpath = "cp"
        val dir = "di"
        val files = "fs"
        val flag = "fl"
        val id = "dd"
        val isDeleted = "ds"
        val isdir = "id"
        val isfile = "if"
        val isroot = "ir"
        val lastDeleted = "dl"
        val lastModified = "dt"
        val length = "sz"
        val name = "nm"
        val notexists = "ne"
        val notreadable = "nr"
        val notwritable = "nw"
        val perm = "pm"
        val rpath = "rp"
        val state = "st"
        //#END IFileInfo.Key
    }

    val root: IRootInfo

    /// @return Presentation name may or may not be rpath.nameWithSuffix
    val name: String

    /// @return Relative path relative to root directory, eg. manual/index.html.
    val rpath: String

    /// @return The context relative path, ie. apath without leading /.
    val cpath: String

    /// @return "/" + rootdir.name() + "/" + rpath(rootdir), or null if not under the given rootdir.
    val apath: String

    val parent: IFileInfo?

    val exists: Boolean

    val isDir: Boolean
        get() {
            return stat()?.isDir == true
        }

    fun stat(): IFileStat?

    @Throws(IOException::class)
    fun content(): IFileContent

    fun setLastModified(timestamp: Long): Boolean

    fun setWritable(writable: Boolean): Boolean

    /**
     * @param @notnull rpath Clean relative path relative to this resource.
     * @return @notnull An IFileInfo object of file with the given rpath. Note that rpath should have been checked for validity.
     */
    fun fileInfo(rpath: String): IFileInfo

    /**
     * @return The input collection with IFileInfo of files under the current directory.
     */
    fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T

    /**
     * @param predicate Return true to prune the given file. Deleted directories are always pruned if it is no longer in use.
     * If predicate is null, prune unused directories only and skip vaccum for quick cleanup.
     */
    fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>? = null): Pair<Int, Int>

    /** @return true if directory exists or directory is created successfully. */
    fun mkparent(): Boolean

    /** @return true if directory exists or directory is created successfully. */
    fun mkdirs(): Boolean

    /** @return true if destination exists and deleted successfully. */
    fun delete(): Boolean

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    @Throws(JSONException::class)
    fun toJSON(): JSONObject

    class NameComparator private constructor() : Comparator<IFileInfo?> {
        override fun compare(lhs: IFileInfo?, rhs: IFileInfo?): Int {
            if (lhs == null) return if (rhs == null) 0 else -1
            return if (rhs == null) 1 else lhs.name.compareTo(rhs.name)
        }

        companion object {
            val singleton = NameComparator()
        }
    }
}

interface IFileFileInfo : IFileInfo {
    val file: File get
}

interface IRootInfo : IFileInfo {

    override fun stat(): IFileStat

    fun find(ret: MutableCollection<String>, rpathx: String, pattern: String)
    fun <T> transaction(code: Fun01<T>): T

    /**
     * @param rpath Relative path, with trailing / for directory.
     * @param listdir true to list directories, false to list files. This only matter if rpath is a root.
     */
    fun history(rpath: String, listdir: Boolean = false): List<IDeletedFileStat>

    fun searchHistory(rpath: String, predicate: Fun11<IDeletedFileStat, Boolean>): List<IDeletedFileStat>

    /** @param all true to delete all history items with same path as the given id. */
    fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int>

    /** @return (oks, fails) */
    fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int>

    fun updateXrefs(from: IFileInfo, infos: JSONObject?)
}

open class ReadOnlyRootStat : IFileStat {
    override val isDir = true

    override val isFile = false

    override val readable = true

    override val writable = false

    override val length = 0L

    override val lastModified = 0L

    override val perm = "r-"

    override val checksumBytes = null
}

abstract class ReadOnlyRootBase(
        override val name: String
) : IRootInfo {

    private val stat = ReadOnlyRootStat()

    override val root = this

    override val rpath = ""

    override val apath = File.separatorChar + name

    override val cpath = name

    override val parent: IFileInfo? = null

    override val exists = true

    override fun stat(): IFileStat {
        return stat
    }

    override fun content(): IFileContent {
        throw IOException()
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return false
    }

    override fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override fun mkdirs(): Boolean {
        return true
    }

    override fun mkparent(): Boolean {
        return false
    }

    override fun delete(): Boolean {
        return false
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        return ret
    }

    override fun find(ret: MutableCollection<String>, rpathx: String, pattern: String) {
        return
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun history(rpath: String, listdir: Boolean): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun searchHistory(rpath: String, predicate: Fun11<IDeletedFileStat, Boolean>): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun updateXrefs(from: IFileInfo, infos: JSONObject?) {
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toJSON(): JSONObject {
        return FileInfoUtil.tojson(this)
    }
}

open class ReadOnlyContent(
        private val length: Long,
        private val provider: Fun01<InputStream>
) : IFileContent {
    override fun getInputStream(): InputStream {
        return this.provider()
    }

    override fun getSeekableInputStream(): ISeekableInputStream {
        return ReadOnlySeekableInputStream(length, provider)
    }

    override fun readBytes(): ByteArray {
        return getInputStream().use { it.readBytes() }
    }

    override fun readText(charset: Charset): String {
        return getInputStream().reader(charset).use { it.readText() }
    }

    override fun copyTo(dst: OutputStream) {
        getInputStream().use { input ->
            FileUt.copy(dst, input)
        }
    }

    override fun getOutputStream(): OutputStream {
        throw IOException()
    }

    override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?, xrefs: JSONObject?) {
        throw IOException()
    }

    override fun write(data: InputStream, timestamp: Long?, xrefs: JSONObject?) {
        throw IOException()
    }

    override fun copyTo(dst: IFileInfo, timestamp: Long?) {
        getInputStream().use {
            dst.content().write(it, timestamp)
        }
    }

    override fun moveTo(dst: IFileInfo, timestamp: Long?) {
        throw IOException()
    }

    override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
        return false
    }

    override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
        return false
    }

}

abstract class FileInfoBase(protected val f: File) : IFileFileInfo, IFileStat {

    override val file: File get() = f

    override val exists: Boolean get() = f.exists()

    override fun stat(): IFileStat? {
        return if (exists) this else null
    }

    override val isDir: Boolean get() = f.isDirectory

    override val isFile: Boolean get() = f.isFile

    override val readable: Boolean get() = f.canRead()

    override val writable: Boolean get() = f.canWrite()

    override val length: Long get() = f.length()

    override val lastModified: Long get() = f.lastModified()

    override val perm: String get() = FileInfoUtil.perm(f)

    override val checksumBytes = null

    override fun fileInfo(rpath: String): IFileInfo {
        return if (rpath.isEmpty()) this else newfileinfo(rpath)
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        if (!isDir) return ret
        for (name in f.listOrEmpty()) {
            ret.add(newfileinfo(name))
        }
        return ret
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun mkparent(): Boolean {
        val parent = f.parentFile
        return parent != null && (parent.isDirectory || !parent.exists() && parent.mkdirs())
    }

    override fun mkdirs(): Boolean {
        return f.isDirectory || f.mkdirs()
    }

    override fun delete(): Boolean {
        if (f.isDirectory) {
            return f.listOrEmpty().isEmpty() && f.delete()
        }
        return f.delete()
    }

    override fun equals(other: Any?): Boolean {
        return other is FileInfo && compareValues(f, other.f) == 0
    }

    override fun hashCode(): Int {
        return f.hashCode()
    }

    @Throws(JSONException::class)
    override fun toJSON(): JSONObject {
        return FileInfoUtil.tojson(this)
    }

    protected abstract fun newfileinfo(rpath: String): IFileInfo
}

/// FileInfo wrapper for a File.
open class FileInfo @JvmOverloads constructor(
        override val root: FileRootInfo,
        override val rpath: String,
        file: File = File(root.file, rpath).absoluteFile
) : FileInfoBase(file) {

    private val fileContent = FileContent(this, f)

    //////////////////////////////////////////////////////////////////////

    override val name: String get() = f.name
    override val cpath = Basepath.joinRpath(root.cpath, rpath)
    override val apath = Basepath.joinPath(root.apath, rpath)

    override val parent: IFileInfo?
        get() = if (rpath.isEmpty()) null else FileInfo(root, Basepath.dir(rpath) ?: "")

    override fun content(): IFileContent {
        return fileContent
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return f.setLastModified(timestamp)
    }

    override fun setWritable(yes: Boolean): Boolean {
        return f.setWritable(yes)
    }

    protected override fun newfileinfo(rpath: String): IFileInfo {
        return FileInfo(root, Basepath.joinRpath(this.rpath, rpath))
    }
}

class FileContent(
        private val info: FileInfo,
        private
        val file: File
) : IFileContent {

    override fun getInputStream(): InputStream {
        return file.inputStream()
    }

    override fun getSeekableInputStream(): ISeekableInputStream {
        return SeekableFileInputStream(file)
    }

    override fun getOutputStream(): OutputStream {
        return file.outputStream()
    }

    override fun readBytes(): ByteArray {
        return getInputStream().use { it.readBytes() }
    }

    override fun readText(charset: Charset): String {
        return getInputStream().reader(charset).use { it.readText() }
    }

    override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?, xrefs: JSONObject?) {
        prepareToWrite()
        getOutputStream().use { it.write(data, offset, length) }
        file.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun write(data: InputStream, timestamp: Long?, xrefs: JSONObject?) {
        prepareToWrite()
        FileUt.copy(file, data)
        file.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun copyTo(dst: OutputStream) {
        getInputStream().use { input ->
            FileUt.copy(dst, input)
        }
    }

    override fun copyTo(dst: IFileInfo, timestamp: Long?) {
        getInputStream().use {
            dst.content().write(it, timestamp)
        }
    }

    override fun moveTo(dst: IFileInfo, timestamp: Long?) {
        dst.root.let { if (it === info.root && renameTo(dst, timestamp)) return }
        copyTo(dst, timestamp)
        if (!file.delete()) throw IOException()
    }

    override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
        val dstcontent = dst.content()
        if (dstcontent is FileContent) {
            if (file.renameTo(dstcontent.file)) {
                dstcontent.file.setLastModified(timestamp ?: System.currentTimeMillis())
                return true
            }
        }
        return false
    }

    override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
        return false
    }

    private fun prepareToWrite() {
        if (file.exists()) {
            if (!file.isFile || !file.delete()) throw IOException()
            return
        }
        file.mkparentOrNull() ?: throw IOException()
    }
}

/// RootInfo back up by a file.
open class FileRootInfo @JvmOverloads constructor(
        file: File,
        final override val name: String = file.name
) : FileInfoBase(file), IRootInfo {

    override val root get() = this

    override val rpath = ""

    override val cpath = name

    override val apath = File.separator + name

    override val parent: IFileInfo? = null

    override fun setLastModified(timestamp: Long): Boolean {
        return false
    }

    override fun mkdirs(): Boolean {
        return true
    }

    override fun setWritable(yes: Boolean): Boolean {
        return false
    }

    override fun stat(): IFileStat {
        return this
    }

    @Throws(IOException::class)
    override fun content(): IFileContent {
        throw IOException()
    }

    override fun find(ret: MutableCollection<String>, rpathx: String, pattern: String) {
        FileInfoUtil.find1(ret, rpathx, pattern, f, name)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }

    override fun history(rpath: String, listdir: Boolean): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun searchHistory(rpath: String, predicate: Fun11<IDeletedFileStat, Boolean>): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun updateXrefs(from: IFileInfo, infos: JSONObject?) {
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return FileInfo(this, Basepath.joinRpath(this.rpath, rpath))
    }
}

interface IRecentsInfo : IFileInfo {
    val state: JSONObject?
}

class RecentsInfoWrapper(
        private val info: IFileInfo,
        override val name: String,
        override val state: JSONObject?
) : IRecentsInfo {

    override val root get() = info.root

    override val rpath get() = info.rpath

    override val cpath get() = info.cpath

    override val apath get() = info.apath

    override val parent get() = info.parent

    override val exists get() = info.exists

    override fun stat(): IFileStat? {
        return info.stat()
    }

    @Throws(IOException::class)
    override fun content(): IFileContent {
        return info.content()
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return info.setLastModified(timestamp)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return info.setWritable(writable)
    }

    override fun fileInfo(rpath: String): IFileInfo {
        throw UnsupportedOperationException()
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        throw UnsupportedOperationException()
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
        return info.cleanupTrash(predicate)
    }

    override fun mkparent(): Boolean {
        return info.mkparent()
    }

    override fun mkdirs(): Boolean {
        return info.mkdirs()
    }

    override fun delete(): Boolean {
        return info.delete()
    }

    override fun equals(other: Any?): Boolean {
        return info.equals(other)
    }

    override fun hashCode(): Int {
        return info.hashCode()
    }

    @Throws(JSONException::class)
    override fun toJSON(): JSONObject {
        val json = FileInfoUtil.tojson(this)
        if (state != null) {
            json.put(Key.state, state)
        }
        return json
    }

}

class RecentsRoot(
        private val _name: String,
        private val capacity: Int
) : IRecentsRoot {
    companion object {
        private val serial = Serial()
    }

    class Item internal constructor(var key: String, var info: IRecentsInfo) {
        var timestamp: Long

        init {
            timestamp = serial.get()
        }
    }

    private val undos: MutableList<Item> = LinkedList()
    private val redos: MutableList<Item> = LinkedList()

    private val lock: Lock = ReentrantLock()
    override val count: Int
        get() = undos.size

    fun dir(): File? {
        return null
    }

    fun trashDir(): File? {
        return null
    }

    fun name(): String {
        return _name
    }

    override fun clear() {
        lock.lock()
        try {
            undos.clear()
            redos.clear()
            serial.reset()
        } finally {
            lock.unlock()
        }
    }

    override fun clean() {
        lock.lock()
        try {
            for (i in undos.indices.reversed()) {
                val item = undos[i]
                if (!item.info.exists) {
                    undos.removeAt(i)
                }
            }
            for (i in redos.indices.reversed()) {
                val item = redos[i]
                if (!item.info.exists) {
                    redos.removeAt(i)
                }
            }
        } finally {
            lock.unlock()
        }
    }

    override fun status(): JSONObject? {
        return try {
            JSONObject().put(An.Key.backward, undos.size > 0).put(An.Key.forward, redos.size > 0)
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    private fun _eq(a: JSONObject?, b: JSONObject?): Boolean {
        if (a == null) return b == null
        if (b == null) return false
        val aa = a.toString()
        val bb = b.toString()
        return aa == bb
    }

    private fun _isnotdup(first: Item?, item: Item?, path: String): Boolean {
        var ret = false
        if (first != null && item != null) {
            ret = first.key == item.key && _eq(first.info.state, item.info.state)
        }
        return !ret
    }

    private fun _prepend(list: MutableList<Item>, item: Item) {
        if (list.size >= capacity) {
            list.removeAt(list.size - 1)
        }
        list.add(0, item)
    }

    private fun _last(list: List<Item>): Item? {
        val size = list.size
        return if (size > 0) list[size - 1] else null
    }

    private fun _first(list: List<Item>): Item? {
        val size = list.size
        return if (size > 0) list[0] else null
    }

    override fun put(navigation: Int, path: String, info: IRecentsInfo): JSONObject? {
        lock.lock()
        return try {
            val item = Item(path, info)
            when (navigation) {
                An.RecentsCmd.INVALID -> {
                }
                An.RecentsCmd.BACK -> {
                    
                    if (_isnotdup(_last(redos), item, path)) {
                        if (redos.size >= capacity) {
                            redos.removeAt(0)
                        }
                        redos.add(item)
                    }
                }
                An.RecentsCmd.FORWARD -> {
                    
                    if (_isnotdup(_last(undos), item, path)) {
                        _prepend(undos, item)
                    }
                }
                else -> {
                    
                    if (_isnotdup(_first(undos), item, path)) {
                        _prepend(undos, item)
                    }
                }
            }
            status()
        } finally {
            lock.unlock()
        }
    }

    override fun peek(): IRecentsInfo? {
        lock.lock()
        return try {
            val item = _first(undos)
            item?.info
        } finally {
            lock.unlock()
        }
    }

    override fun back(): IRecentsInfo? {
        lock.lock()
        return try {
            val ret = _first(undos)
            if (ret != null) {
                undos.removeAt(0)
                return ret.info
            }
            null
        } finally {
            lock.unlock()
        }
    }

    override fun forward(): IRecentsInfo? {
        lock.lock()
        return try {
            val ret = _last(redos)
            if (ret != null) {
                redos.removeAt(redos.size - 1)
                return ret.info
            }
            null
        } finally {
            lock.unlock()
        }
    }
    /**
     * @return IFileInfo with the most recent first.
     */
    override fun <T : MutableList<IFileInfo>> listFiles(ret: T): T {
        lock.lock()
        return try {
            for (item in undos) {
                ret.add(item.info)
            }
            ret
        } finally {
            lock.unlock()
        }
    }

    override fun listAll(ret: JSONArray) {
        lock.lock()
        try {
            for (item in redos) {
                try {
                    val json = item.info.toJSON()
                    json.put(Key.flag, true)
                    ret.put(json)
                } catch (e: JSONException) {
                }
            }
            for (item in undos) {
                try {
                    ret.put(item.info.toJSON())
                } catch (e: JSONException) {
                }
            }
        } finally {
            lock.unlock()
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is RecentsRoot
    }

}

class JSONObjectFileStat(
        private val info: JSONObject
) : IFileStat {
    val exists get() = !info.optBoolean(Key.notexists, false)

    override val isDir get() = exists && info.optBoolean(Key.isdir, false)

    override val isFile get() = exists && info.optBoolean(Key.isfile, false)

    override val length get() = info.optLong(Key.length, 0)

    override val lastModified get() = info.optLong(Key.lastModified, 0)

    override val readable get() = exists && !info.optBoolean(Key.notreadable, false)

    override val writable get() = exists && !info.optBoolean(Key.notwritable, false)

    override val perm get() = info.stringOrNull(Key.perm) ?: FileInfoUtil.perm(readable, writable)

    override val checksumBytes get() = info.stringOrNull(Key.checksum)?.let { Hex.decode(it) }
}

open class ReadOnlyJSONObjectFIleInfo(
        final override val root: IRootInfo,
        final override val rpath: String,
        info: JSONObject,
        private val files: Map<String, ReadOnlyJSONObjectFIleInfo>,
        private val content: IFileContent
) : IFileInfo {
    private val stat = JSONObjectFileStat(info)
    override val name = Basepath.nameWithSuffix(rpath)
    override val cpath = Basepath.joinRpath(root.cpath, rpath)
    override val apath = Basepath.joinPath(root.apath, rpath)
    override val parent get() = if (rpath.isEmpty()) null else fileInfo(Basepath.dir(rpath) ?: "")
    override val exists = stat.exists

    override fun stat(): IFileStat? {
        return if (stat.exists) stat else null
    }

    override fun content(): IFileContent {
        return content
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return false
    }

    override fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override fun fileInfo(rpath: String): IFileInfo {
        var ret = this
        val cleanrpath = Support.getcleanrpath(rpath) ?: return FileInfoUtil.notexists(root, rpath)
        for (name in cleanrpath.split(File.separator)) {
            if (name.isEmpty()) continue
            ret = ret.files[name] ?: return FileInfoUtil.notexists(root, cleanrpath)
        }
        return ret
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        ret.addAll(files.values)
        return ret
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun mkparent(): Boolean {
        return false
    }

    override fun mkdirs(): Boolean {
        return stat.isDir
    }

    override fun delete(): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is ReadOnlyJSONObjectFIleInfo && rpath == other.rpath
    }

    override fun hashCode(): Int {
        return rpath.hashCode()
    }

    override fun toJSON(): JSONObject {
        return FileInfoUtil.tojson(this)
    }
}

object FileInfoUtil {
    fun tojson(ret: JSONArray, warns: JSONArray, fileinfo: IFileInfo) {
        try {
            ret.put(tojson(fileinfo))
        } catch (e: Exception) {
            warns.put(fileinfo.cpath)
        }
    }

    @Throws(JSONException::class)
    fun tojson(fileinfo: IFileInfo): JSONObject {
        val ret = JSONObject()
        ret.put(Key.name, fileinfo.name)
        ret.put(Key.rpath, fileinfo.rpath)
        ret.put(Key.cpath, fileinfo.cpath)
        tojson(ret, fileinfo.stat())
        return ret
    }

    fun tojson(ret: JSONObject, stat: IFileStat?): JSONObject {
        if (stat == null) {
            ret.put(Key.notexists, true)
            return ret
        }
        if (stat.isDir) {
            ret.put(Key.isdir, true)
        }
        if (stat.isFile) {
            ret.put(Key.isfile, true)
        }
        if (!stat.readable) {
            ret.put(Key.notreadable, true)
        }
        if (!stat.writable) {
            ret.put(Key.notwritable, true)
        }
        if (stat.length != 0L) {
            ret.put(Key.length, stat.length)
        }
        if (stat.lastModified != 0L) {
            ret.put(Key.lastModified, stat.lastModified)
        }
        ret.put(Key.perm, stat.perm)
        return ret
    }

    fun listFiles(dir: IFileInfo): List<IFileInfo> {
        val ret: MutableList<IFileInfo> = ArrayList()
        dir.readDir(ret)
        return ret
    }

    fun filesByName(dir: IFileInfo): Collection<IFileInfo> {
        return dir.readDir(TreeSet<IFileInfo>(IFileInfo.NameComparator.singleton))
    }

    fun notexists(fileinfo: JSONObject): Boolean {
        return fileinfo.optBoolean(Key.notexists, false)
    }

    fun notexists(name: String?): JSONObject {
        val ret = JSONObject()
        try {
            ret.put(Key.notexists, true)
            ret.put(Key.name, name)
            ret.put(Key.perm, "--")
        } catch (e: JSONException) {
            throw AssertionError()
        }
        return ret
    }

    fun perm(file: File?): String {
        return if (file == null || !file.exists()) "--" else perm(file.canRead(), file.canWrite())
    }

    fun perm(readable: Boolean, writable: Boolean): String {
        return TextUt.format("%c%c", if (readable) 'r' else '-', if (writable) 'w' else '-')
    }

    fun find1(ret: MutableCollection<String>, basepath: String, pattern: String, dir: File, name: String) {
        if (!dir.isDirectory) return
        val lcpat = pattern.toLowerCase()
        val rpaths = File(dir, basepath).ut.collects(FilePathCollectors::pathOfAny)
        val prefix = Basepath.joinPath(name, basepath)
        for (rpath in rpaths) {
            if (Basepath.nameWithSuffix(rpath).toLowerCase().contains(lcpat)) {
                ret.add(Basepath.joinPath(prefix, rpath))
            }
        }
    }

    fun walk3(info: IFileInfo, rpath: String = "", postorder: Boolean = false, callback: Fun30<IFileInfo, String, IFileStat>) {
        if (info.stat()?.isDir != true) return
        walk31(info, rpath, postorder, callback)
    }

    private fun walk31(dir: IFileInfo, rpath: String, postorder: Boolean, callback: Fun30<IFileInfo, String, IFileStat>) {
        for (file in dir.readDir(ArrayList())) {
            val filepath = if (rpath.isEmpty()) file.name else rpath + File.separatorChar + file.name
            val filestat = file.stat()!!
            if (!postorder) callback(file, filepath, filestat)
            if (filestat.isDir) walk31(file, filepath, postorder, callback)
            if (postorder) callback(file, filepath, filestat)
        }
    }

    fun walk2(info: IFileInfo, postorder: Boolean = false, callback: Fun20<IFileInfo, IFileStat>) {
        if (info.stat()?.isDir != true) return
        walk21(info, postorder, callback)
    }

    private fun walk21(dir: IFileInfo, postorder: Boolean, callback: Fun20<IFileInfo, IFileStat>) {
        for (file in dir.readDir(ArrayList())) {
            val filestat = file.stat()!!
            if (!postorder) callback(file, filestat)
            if (filestat.isDir) walk21(file, postorder, callback)
            if (postorder) callback(file, filestat)
        }
    }

    fun scan3(info: IFileInfo, rpath: String = "", callback: Fun31<IFileInfo, String, IFileStat, Boolean>) {
        if (info.stat()?.isDir != true) return
        scan1(info, rpath, callback)
    }

    private fun scan1(dir: IFileInfo, rpath: String, callback: Fun31<IFileInfo, String, IFileStat, Boolean>) {
        for (file in dir.readDir(ArrayList())) {
            val filepath = if (rpath.isEmpty()) file.name else rpath + File.separatorChar + file.name
            val filestat = file.stat()!!
            if (callback(file, filepath, filestat) && filestat.isDir) scan1(file, filepath, callback)
        }
    }

    fun scan2(info: IFileInfo, callback: Fun21<IFileInfo, IFileStat, Boolean>) {
        if (info.stat()?.isDir != true) return
        scan21(info, callback)
    }

    private fun scan21(dir: IFileInfo, callback: Fun21<IFileInfo, IFileStat, Boolean>) {
        for (file in dir.readDir(ArrayList())) {
            val filestat = file.stat()!!
            if (callback(file, filestat) && filestat.isDir) scan21(file, callback)
        }
    }

    fun deleteEmptyTree(dir: IFileInfo): Int {
        if (dir.stat()?.isDir != true) return 0
        var deleted = 0
        deleteEmptyDirs1(dir, true) { ++deleted }
        return deleted
    }

    fun deleteEmptySubtrees(dir: IFileInfo): Int {
        if (dir.stat()?.isDir != true) return 0
        var deleted = 0
        for (file in dir.readDir(ArrayList())) {
            deleteEmptyDirs1(file, file.stat()?.isDir == true) { ++deleted }
        }
        return deleted
    }

    private fun deleteEmptyDirs1(dir: IFileInfo, isdir: Boolean, ondelete: Fun00): Int {
        if (!isdir) return 1
        var count = 0
        for (file in dir.readDir(ArrayList())) {
            count += deleteEmptyDirs1(file, (file.stat()?.isDir == true), ondelete)
        }
        if (count == 0) {
            if (dir.delete()) ondelete()
        }
        return count
    }

    /** @return true if delete success, false if some file failed to delete. */
    fun deleteSubtrees(dir: IFileInfo): Boolean {
        if (dir.stat()?.isDir != true) return true
        var ret = true
        for (file in dir.readDir(ArrayList())) {
            if (!deleteTree(file)) ret = false
        }
        return ret
    }

    fun deleteTree(dir: IFileInfo): Boolean {
        val stat = dir.stat() ?: return true
        if (!stat.isDir) return dir.delete()
        var isempty = true
        for (file in dir.readDir(ArrayList())) {
            if (!deleteTree(file)) isempty = false
        }
        return if (isempty) dir.delete() else false
    }

    private val EMPTY_FILE_ARRAY = arrayOf<File>()
    fun listfiles(file: File?): Array<File> {
        if (file != null) {
            try {
                val ret = file.listFiles()
                if (ret != null) {
                    return ret
                }
            } catch (e: Throwable) {
            }
            
            
        }
        return EMPTY_FILE_ARRAY
    }

    fun readonly(): JSONObject {
        return JSONObject()
                .put(Key.notwritable, true)
                .put(Key.isdir, false)
                .put(Key.isfile, true)
    }

    private val NOTEXISTS: JSONObject = JSONObject()
            .put(Key.notexists, true)
            .put(Key.notreadable, true)
            .put(Key.notwritable, true)
            .put(Key.isdir, false)
            .put(Key.isfile, false)

    fun notexists(root: IRootInfo, rpath: String): IFileInfo {
        return ReadOnlyJSONObjectFIleInfo(root, rpath, NOTEXISTS, emptyMap(),
                ReadOnlyContent(0L, NotExistsSeekableInputStreamProvider::getInputStream))
    }

    fun defaultCleanupTrashPredicate(stat: IDeletedFileStat): Boolean {
        var longer = DateUt.DAY * An.DEF.keepLongerDays
        var shorter = DateUt.DAY * An.DEF.keepShorterDays
        val now = System.currentTimeMillis()
        val longer1 = now - longer
        val shorter1 = now - shorter
        if (stat.stat.length <= An.DEF.keepLongerSizeLimit) {
            return stat.lastDeleted < longer1
        } else {
            return stat.lastDeleted < shorter1
        }
    }

    fun isRootOrUnderReadonlyRoot(fileinfo: IFileInfo): Boolean {
        return fileinfo.rpath.isEmpty() || !fileinfo.root.stat().writable
    }
}
