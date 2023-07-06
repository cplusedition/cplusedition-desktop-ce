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

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.DateUt
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.FilePathCollectors
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun00
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun21
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.IBasepath
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.Serial
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.bot
import com.cplusedition.bot.core.listOrEmpty
import com.cplusedition.bot.core.mkparentOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.support.IFileInfo.Key
import sf.andrians.cplusedition.support.IStorage.K
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface IFileStat {
    val isDir: Boolean
    val isFile: Boolean
    val length: Long
    val lastModified: Long
    val perm: String
    val readable: Boolean
    val writable: Boolean
    val checksumBytes: ByteArray?
}

interface IDeletedFileStat {
    val stat: IFileStat
    val id: Long
    val name: String
    val isDeleted: Boolean
    val lastDeleted: Long
    val dir: String
    fun cpath(): String

    @Throws(JSONException::class)
    fun toJSON(): JSONObject
}

class CleanupTrashResult constructor(
    val files: Long,
    val dirs: Long,
    val totalsize: Long,
)

interface IFileContent : ISeekableInputStreamProvider, ISeekableOutputStreamProvider {

    /// @return Actual content length in bytes.
    fun getContentLength(): Long

    @Throws(IOException::class)
    fun readBytes(): ByteArray

    @Throws(IOException::class)
    fun readText(charset: Charset = Charsets.UTF_8): String

    @Throws(IOException::class)
    /// @param timestamp If null, current time is used.
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size, timestamp: Long? = null)

    @Throws(IOException::class)
    /// @param timestamp If null, current time is used.
    fun write(data: InputStream, timestamp: Long? = null)

    @Throws(IOException::class)
    /// @param timestamp If null, current time is used.
    fun write(data: CharArray, offset: Int, length: Int, timestamp: Long? = null, charset: Charset = Charsets.UTF_8)

    @Throws(IOException::class)
    /// @param timestamp If null, current time is used.
    fun write(data: Reader, timestamp: Long? = null, charset: Charset = Charsets.UTF_8)

    /// This only works on files. This works across roots.
    @Throws(IOException::class)
    fun copyTo(dst: OutputStream)

    /// This only works on files. This works across roots.
    /// If destination exists, it would be overwritten if it is a file and deletable, otherwise copy fail,
    /// @param timestamp If null, current time is used.
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
    /// @param timestamp If null, current time is used.
    fun renameTo(dst: IFileInfo, timestamp: Long? = null): Boolean

    /// If data is not null, write data as a deleted file at rpathx, use current time if timestamp is not given.
    /// If data is null create a copy of file at rpathx preserving timestamp if timestamp is not given.
    /// @return true if recovery file is created.
    /// @param timestamp If null, current time is used.
    fun writeRecovery(data: InputStream? = null, timestamp: Long? = null): Boolean
}

interface IFileInfo : IBasepath {
    object Key {
        const val checksum = "cs"
        const val cpath = "cp"
        const val dir = "di"
        const val files = "fs"
        const val flag = "fl"
        const val id = "dd"
        const val isDeleted = "ds"
        const val isdir = "id"
        const val isfile = "if"
        const val isroot = "ir"
        const val lastDeleted = "dl"
        const val lastModified = "dt"
        const val lastUsed = "lu"
        const val length = "sz"
        const val name = "nm"
        const val notexists = "ne"
        const val notreadable = "nr"
        const val notwritable = "nw"
        const val offset = "of"
        const val perm = "pm"
        const val rpath = "rp"
        const val state = "st"
        const val supportHistory = "hh"
        const val dirId = "dI"
    }

    /// @return Presentation name may or may not be rpath.name
    override val name: String
    override val stem: String get() = Basepath.stem(name)
    override val suffix: String get() = Basepath.suffix(name)
    override val lcSuffix: String get() = Basepath.lcSuffix(name)
    override val dir: String? get() = Basepath.dir(apath)

    val root: IRootInfo

    /// @return The underlying file if this IFileInfo is backed by a plain File
    /// that can be accessed directly, otherwise null.
    val file: File? get

    /// @return Relative path relative to a root directory, eg. manual/index.html.
    val rpath: String

    /// @return The context relative path, ie. apath without leading /.
    val cpath: String

    /// @return "/" + rootdir.name() + "/" + rpath(rootdir), or null if not under the given rootdir.
    val apath: String

    val parent: IFileInfo?

    val exists: Boolean

    val supportHistory: Boolean

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
    fun <R : MutableCollection<String>> listDir(ret: R): R

    /**
     * @return The input collection with IFileInfo of files under the current directory.
     */
    fun <R : MutableCollection<IFileInfo>> readDir(ret: R): R

    /** @return true if directory exists or directory is created successfully. */
    fun mkparent(): Boolean

    /** @return true if directory exists or directory is created successfully. */
    fun mkdirs(): Boolean

    /**
     * @prune true to prevent saving to trash.
     * @return true if destination exists and deleted successfully.
     */
    fun delete(prune: Boolean = false): Boolean

    /**
     * Delete file without keeping history.
     * @return true if destination exists and deleted successfully.
     */
    fun shred(): Boolean

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    @Throws(JSONException::class)
    fun toJSON(): JSONObject

    val isDir: Boolean get() = stat()?.isDir == true
    val isFile: Boolean get() = stat()?.isFile == true

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

interface IRootInfo : IFileInfo {

    override fun stat(): IFileStat

    /// Find file or directory recursively under the given subdirectory.
    fun find(ret: MutableCollection<String>, subdir: String, searchtext: String)

    fun <T> transaction(code: Fun01<T>): T

    /**
     * Retrieve history, aka deleted files.
     * If name is empty and dir is a root and listdir is true, return all directories that contains deleted files.
     * If name is empty and dir is a root and listdir is false, return all versions of all deleted files at the root directory, non-recursive.
     * If name is empty and dir is not empty, return the all versions of all deleted files at the given directory, non-recursive.
     * If name is not empty, return all versions of the deleted file at dir/name.
     *
     * @param dir Dir part of cpath.
     * @param name File name, empty for directory.
     * @param listdir true to list directories, false to list files, only matter for root, ie. when dir and name are empty.
     * @param callback Called on each deleted item.
     */
    fun history(
        dir: String,
        name: String,
        listdir: Boolean = false,
        callback: Fun10<IDeletedFileStat>
    )

    /**
     * @param callback Call on each deleted file/directory order by delete time descending, ie. latest first.
     */
    fun scanTrash(callback: Fun10<IDeletedFileStat>)

    /**
     * @param predicate Return true to prune the given file. Deleted directories are always pruned
     * if it is no longer in use. If predicate is null, prune unused directories only.
     */
    fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>? = null): CleanupTrashResult

    /** @return (oks, fails) */
    fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int>

    /** @return File/directory entries with error. */
    fun fsck(rpath: String): Triple<Long, Long, List<String>> {
        var okfiles = 0L
        var okdirs = 0L
        val failed = ArrayList<String>()
        val b = ByteArray(K.BUFSIZE)

        fun fail(file: IFileInfo) {
            failed.add(file.cpath)
        }

        fun fsckdir() {
            ++okdirs
        }

        fun fsckfile(file: IFileInfo) {
            try {
                var length = 0L
                file.content().inputStream().use {
                    while (true) {
                        val n = it.read(b)
                        if (n < 0) break
                        length += n
                    }
                }
                if (length != file.content().getContentLength()) fail(file)
                else ++okfiles
            } catch (e: Exception) {
                fail(file)
            }
        }

        fileInfo(rpath).walk2 { file, stat ->
            if (stat.isDir) fsckdir()
            else if (stat.isFile) fsckfile(file)
        }
        return Triple(okfiles, okdirs, failed)
    }
}

interface ICloseableRootInfo : IRootInfo, Closeable

open class ReadOnlyFileStat(file: File) : IFileStat {
    override val isDir = file.isDirectory

    override val isFile = file.isFile

    override val readable = file.canRead()

    override val writable = false

    override val length = file.length()

    override val lastModified = file.lastModified()

    override val perm = "r-"

    override val checksumBytes = null
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

    override val root = this
    override val rpath = ""
    override val apath get() = File.separatorChar + name
    override val cpath get() = name
    override val parent: IFileInfo? = null
    override val supportHistory = false

    private val stat = ReadOnlyRootStat()

    override fun stat(): IFileStat {
        return stat
    }

    override fun content(): IFileContent {
        throw IOException()
    }

    override val exists = true

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

    override fun delete(prune: Boolean): Boolean {
        return false
    }

    override fun shred(): Boolean {
        return false
    }

    override fun scanTrash(callback: Fun10<IDeletedFileStat>) {
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): CleanupTrashResult {
        return CleanupTrashResult(0, 0, 0)
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toJSON(): JSONObject {
        return FileInfoUtil.toJSONFileInfo(this)
    }

    override fun find(ret: MutableCollection<String>, subdir: String, searchtext: String) {
        return
    }

    override fun history(
        dir: String,
        name: String,
        listdir: Boolean,
        callback: Fun10<IDeletedFileStat>
    ) {
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }
}

abstract class ReadOnlyInfoBase constructor(
    override val root: IRootInfo,
    override val rpath: String
) : IFileInfo {

    override val supportHistory get() = root.supportHistory

    override val cpath get() = root.name + File.separatorChar + rpath

    override val apath get() = File.separatorChar + cpath

    override val exists = true

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

    override fun delete(prune: Boolean): Boolean {
        return false
    }

    override fun shred(): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toJSON(): JSONObject {
        return FileInfoUtil.toJSONFileInfo(this)
    }
}

abstract class ReadOnlyContentBase : IFileContent {

    override fun readBytes(): ByteArray {
        return IOUt.readBytes(inputStream())
    }

    override fun readText(charset: Charset): String {
        return IOUt.readText(inputStream(), charset)
    }

    override fun copyTo(dst: OutputStream) {
        inputStream().use { input ->
            FileUt.copy(dst, input)
        }
    }

    override fun outputStream(): OutputStream {
        throw UnsupportedOperationException()
    }

    override fun seekableOutputStream(truncate: Boolean): AbstractSeekableOutputStream {
        throw UnsupportedOperationException()
    }

    override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?) {
        throw UnsupportedOperationException()
    }

    override fun write(data: InputStream, timestamp: Long?) {
        throw UnsupportedOperationException()
    }

    override fun write(data: CharArray, offset: Int, length: Int, timestamp: Long?, charset: Charset) {
        throw UnsupportedOperationException()
    }

    override fun write(data: Reader, timestamp: Long?, charset: Charset) {
        throw UnsupportedOperationException()
    }

    override fun copyTo(dst: IFileInfo, timestamp: Long?) {
        inputStream().use {
            dst.content().write(it, timestamp)
        }
    }

    override fun moveTo(dst: IFileInfo, timestamp: Long?) {
        throw UnsupportedOperationException()
    }

    override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
        return false
    }

    override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
        return false
    }
}

open class ReadOnlyContent constructor(
    private val lengthProvider: Fun01<Long>,
    private val inputStreamProvider: Fun01<InputStream>
) : ReadOnlyContentBase() {

    override fun getContentLength(): Long {
        return lengthProvider()
    }

    override fun inputStream(): InputStream {
        return this.inputStreamProvider()
    }

    override fun seekableInputStream(): MySeekableInputStream {
        return ReadOnlySeekableInputStream(lengthProvider, inputStreamProvider)
    }
}

open class ReadOnlyFileInfoWrapper(
    private val delegate: IFileInfo,
) : ReadOnlyInfoBase(delegate.root, delegate.rpath) {

    override val name get() = delegate.name
    override val file: File? get() = null
    override val parent: IFileInfo? get() = null

    private val content = ReadOnlyContent({
        delegate.stat()?.length ?: 0L
    }, {
        delegate.content().inputStream()
    })

    override fun content(): IFileContent {
        return content
    }

    override fun stat(): IFileStat? {
        return delegate.stat()
    }

    override fun fileInfo(rpath: String): IFileInfo {
        return delegate.fileInfo(rpath)
    }

    override fun <T : MutableCollection<String>> listDir(ret: T): T {
        return delegate.listDir(ret)
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        return delegate.readDir(ret)
    }
}

abstract class FileInfoBase(protected val f: File) : IFileInfo, IFileStat {

    override val exists: Boolean get() = (f.isFile || f.isDirectory)

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

    override fun <R : MutableCollection<String>> listDir(ret: R): R {
        if (!isDir) return ret
        for (name in f.listOrEmpty()) {
            ret.add(name)
        }
        return ret
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        if (!isDir) return ret
        for (name in f.listOrEmpty()) {
            ret.add(newfileinfo(name))
        }
        return ret
    }

    override fun mkparent(): Boolean {
        val parent = f.parentFile ?: return false
        parent.mkdirs()
        return parent.isDirectory
    }

    override fun mkdirs(): Boolean {
        f.mkdirs()
        return f.isDirectory
    }

    override fun delete(prune: Boolean): Boolean {
        if (f.isDirectory) {
            return f.listOrEmpty().isEmpty() && FileUt.delete(f)
        }
        return FileUt.delete(f)
    }

    /// Ovewrite file with random data, and delete it.
    override fun shred(): Boolean {
        FileUt.shred(f)
        return FileUt.delete(f)
    }

    override fun equals(other: Any?): Boolean {
        return other is FileInfo && compareValues(f, other.f) == 0
    }

    override fun hashCode(): Int {
        return f.hashCode()
    }

    @Throws(JSONException::class)
    override fun toJSON(): JSONObject {
        return FileInfoUtil.toJSONFileInfo(this)
    }

    protected abstract fun newfileinfo(rpath: String): IFileInfo
}

class NullRootInfo constructor(name: String) : ReadOnlyRootBase(name) {
    override val file: File?
        get() = null

    override fun fileInfo(rpath: String): IFileInfo {
        return FileInfoUtil.createNotexists(this, rpath)
    }

    override fun <R : MutableCollection<String>> listDir(ret: R): R {
        return ret
    }

    override fun <R : MutableCollection<IFileInfo>> readDir(ret: R): R {
        return ret
    }
}

/// FileInfo wrapper for a File.
open class FileInfo @JvmOverloads constructor(
    override val root: FileRootInfo,
    override val rpath: String,
    file: File = File(root.file, rpath).absoluteFile
) : FileInfoBase(file) {
    init {
        if (rpath.isEmpty() || rpath == ".." || rpath.startsWith("..$FS")) throw AssertionError()
    }

    private val fileContent = FileContent(this, f)

    //////////////////////////////////////////////////////////////////////

    override val name: String get() = f.name
    override val cpath get() = Basepath.joinRpath(root.cpath, rpath)
    override val apath get() = Basepath.joinPath(root.apath, rpath)

    override val parent: IFileInfo?
        get() = Basepath.dir(rpath).let { path ->
            if (path.isNullOrEmpty()) root else
                root.fileInfo(path)
        }
    override val file: File? get() = fileContent.file
    override val supportHistory get() = root.supportHistory

    override fun content(): IFileContent {
        return fileContent
    }

    override fun setLastModified(timestamp: Long): Boolean {
        return f.setLastModified(timestamp)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return f.setWritable(writable)
    }

    protected override fun newfileinfo(rpath: String): IFileInfo {
        return if (rpath.isEmpty()) this else FileInfo(root, Basepath.joinRpath(this.rpath, rpath))
    }
}

open class FileContent(
    protected val info: FileInfo,
    protected val f: File
) : IFileContent {

    val file: File get() = f

    override fun getContentLength(): Long {
        return f.length()
    }

    override fun inputStream(): InputStream {
        return f.inputStream()
    }

    override fun seekableInputStream(): MySeekableInputStream {
        return SeekableFileInputStream(f)
    }

    override fun outputStream(): OutputStream {
        return f.outputStream()
    }

    override fun seekableOutputStream(truncate: Boolean): AbstractSeekableOutputStream {
        return SeekableFileOutputStream(f, truncate)
    }

    override fun readBytes(): ByteArray {
        return IOUt.readBytes(inputStream())
    }

    override fun readText(charset: Charset): String {
        return IOUt.readText(inputStream(), charset)
    }

    override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?) {
        prepareToWrite()
        this.outputStream().use { it.write(data, offset, length) }
        f.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun write(data: InputStream, timestamp: Long?) {
        prepareToWrite()
        FileUt.copy(f, data)
        f.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun write(data: CharArray, offset: Int, length: Int, timestamp: Long?, charset: Charset) {
        prepareToWrite()
        outputStream().bufferedWriter(charset).use { it.write(data, offset, length) }
        f.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun write(data: Reader, timestamp: Long?, charset: Charset) {
        prepareToWrite()
        outputStream().bufferedWriter(charset).use { w ->
            val tmpbuf = CharArray(IOUt.BUFSIZE)
            IOUt.copyAll(tmpbuf, data) {
                w.write(tmpbuf, 0, it)
            }
        }
        f.setLastModified(timestamp ?: System.currentTimeMillis())
    }

    override fun copyTo(dst: OutputStream) {
        inputStream().use { input ->
            FileUt.copy(dst, input)
        }
    }

    override fun copyTo(dst: IFileInfo, timestamp: Long?) {
        inputStream().use {
            dst.content().write(it, timestamp)
        }
    }

    override fun moveTo(dst: IFileInfo, timestamp: Long?) {
        if (renameTo(dst, timestamp)) return
        copyTo(dst, timestamp)
        if (!f.delete()) throw IOException()
    }

    override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
        val dstcontent = dst.content()
        if (dstcontent is FileContent) {
            if (f.renameTo(dstcontent.f)) {
                dstcontent.f.setLastModified(timestamp ?: System.currentTimeMillis())
                return true
            }
        }
        return false
    }

    override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
        return false
    }

    private fun prepareToWrite() {
        if (f.exists()) {
            if (!f.isFile || !f.delete()) throw IOException()
            return
        }
        f.mkparentOrNull() ?: throw IOException()
    }
}

/// RootInfo back up by a file.
open class FileRootInfo @JvmOverloads constructor(
    file: File,
    final override val name: String = file.name
) : FileInfoBase(file), IRootInfo {

    override val supportHistory = false

    override val file: File get() = f

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

    override fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override fun stat(): IFileStat {
        return this
    }

    @Throws(IOException::class)
    override fun content(): IFileContent {
        throw IOException()
    }

    override fun find(ret: MutableCollection<String>, subdir: String, searchtext: String) {
        FileInfoUtil.find(ret, f, subdir, name, searchtext)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }

    override fun history(
        dir: String,
        name: String,
        listdir: Boolean,
        callback: Fun10<IDeletedFileStat>
    ) {
    }

    override fun scanTrash(callback: Fun10<IDeletedFileStat>) {
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): CleanupTrashResult {
        return CleanupTrashResult(0, 0, 0)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return if (rpath.isEmpty()) this else FileInfo(this, Basepath.joinRpath(this.rpath, rpath))
    }
}

interface IRecentsInfo : IFileInfo {
    val state: JSONObject?
    val timestamp: Long
}

class RecentsInfoWrapper constructor(
    private val info: IFileInfo,
    override val name: String,
    override val timestamp: Long,
    override val state: JSONObject?,
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

    override val supportHistory get() = info.supportHistory

    @Throws(IOException::class)
    override fun content(): IFileContent {
        return info.content()
    }

    override val file: File? get() = info.file

    override fun setLastModified(timestamp: Long): Boolean {
        return info.setLastModified(timestamp)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return info.setWritable(writable)
    }

    override fun fileInfo(rpath: String): IFileInfo {
        throw UnsupportedOperationException()
    }

    override fun <T : MutableCollection<String>> listDir(ret: T): T {
        throw UnsupportedOperationException()
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        throw UnsupportedOperationException()
    }

    override fun mkparent(): Boolean {
        return info.mkparent()
    }

    override fun mkdirs(): Boolean {
        return info.mkdirs()
    }

    override fun delete(prune: Boolean): Boolean {
        return info.delete(prune)
    }

    override fun shred(): Boolean {
        return info.shred()
    }

    override fun equals(other: Any?): Boolean {
        return info.equals(other)
    }

    override fun hashCode(): Int {
        return info.hashCode()
    }

    @Throws(JSONException::class)
    override fun toJSON(): JSONObject {
        val json = FileInfoUtil.toJSONFileInfo(this)
        json.put(Key.lastUsed, timestamp)
        if (state != null) {
            json.put(Key.state, state)
        }
        return json
    }
}

class RecentsRoot constructor(
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
    override fun <T : MutableList<IRecentsInfo>> listFiles(ret: T): T {
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

open class JSONObjectFileStat(
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

class JSONObjectDeletedFileStat(
    private val info: JSONObject
) : JSONObjectFileStat(info), IDeletedFileStat {
    override val stat: IFileStat get() = this
    override val id: Long get() = info.optLong(Key.id, 0L)
    override val name: String get() = info.stringOrDef(Key.name, "")
    override val isDeleted: Boolean get() = info.optBoolean(Key.isDeleted)
    override val lastDeleted: Long get() = info.optLong(Key.lastDeleted, 0L)
    override val dir: String get() = info.stringOrDef(Key.dir, "")

    override fun cpath(): String {
        return Basepath.joinPath(dir, name)
    }

    override fun toJSON(): JSONObject {
        return JSONObject(info.toString())
    }
}

open class ReadOnlyJSONObjectFIleInfo constructor(
    final override val root: IRootInfo,
    final override val rpath: String,
    private val jsoninfo: JSONObject,
    private val files: Map<String, ReadOnlyJSONObjectFIleInfo>,
    private val content: IFileContent
) : IFileInfo {
    private val stat = JSONObjectFileStat(jsoninfo)
    override val name = Basepath.name(rpath)
    override val cpath = Basepath.joinRpath(root.cpath, rpath)
    override val apath = Basepath.joinPath(root.apath, rpath)
    override val parent
        get() = Basepath.dir(rpath).let { path ->
            if (path.isNullOrEmpty()) root else
                root.fileInfo(path)
        }
    override val exists = stat.exists

    override val file: File? get() = null

    override fun stat(): IFileStat? {
        return if (stat.exists) stat else null
    }

    override val supportHistory = root.supportHistory

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
        val cleanrpath = Support.getcleanrpath(rpath) ?: return FileInfoUtil.createNotexists(root, rpath)
        for (name in cleanrpath.split(File.separator)) {
            if (name.isEmpty()) continue
            ret = ret.files[name] ?: return FileInfoUtil.createNotexists(root, cleanrpath)
        }
        return ret
    }

    override fun <T : MutableCollection<String>> listDir(ret: T): T {
        ret.addAll(files.values.map { it.name })
        return ret
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        ret.addAll(files.values)
        return ret
    }

    override fun mkparent(): Boolean {
        return false
    }

    override fun mkdirs(): Boolean {
        return stat.isDir
    }

    override fun delete(prune: Boolean): Boolean {
        return false
    }

    override fun shred(): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is ReadOnlyJSONObjectFIleInfo && rpath == other.rpath
    }

    override fun hashCode(): Int {
        return rpath.hashCode()
    }

    override fun toJSON(): JSONObject {
        return FileInfoUtil.toJSONFileInfo(this)
    }

    fun jsonInfo(rpath: String): JSONObject? {
        var ret = this
        val cleanrpath = Support.getcleanrpath(rpath) ?: return null
        for (name in cleanrpath.split(File.separator)) {
            if (name.isEmpty()) continue
            ret = ret.files[name] ?: return null
        }
        return ret.jsoninfo
    }
}

class TmpFileInfo constructor(
    private val fileinfo: IFileInfo,
) {
    fun <R> TmpFileInfo.use(code: Fun11<IFileInfo, R>): R {
        try {
            return code(this.fileinfo)
        } finally {
            this.fileinfo.delete()
        }
    }
}

object TmpUt {
    private val serial = Serial()
    private val lock = ReentrantLock()

    fun <R> withLock(code: Fun01<R>): R {
        return lock.withLock(code)
    }

    fun tmpdir(dir: IFileInfo, prefix: String = ".tmp"): IFileInfo {
        lock.withLock {
            while (true) {
                val file = dir.fileInfo("$prefix${serial.get()}")
                if (!file.exists && file.mkdirs()) return file
            }
        }
    }

    fun <R> tmpdir(dir: IFileInfo, prefix: String = ".tmp", code: Fun21<IFileInfo, Fun00, R>): R {
        tmpdir(dir, prefix).let {
            val ret = code(it) {
                lock.withLock {
                    it.deleteTree(null)
                }
            }
            return ret
        }
    }

    fun tmpfile(dir: IFileInfo, prefix: String = ".tmp", suffix: String = ".tmp"): IFileInfo {
        lock.withLock {
            while (true) {
                val ret = dir.fileInfo("$prefix${serial.get()}$suffix")
                if (!ret.exists) return ret
            }
        }
    }

    fun deleteTree(file: IFileInfo): Boolean {
        lock.withLock {
            return file.deleteTree(null)
        }
    }

}

object FileInfoUtil {
    fun toJSONFileInfo(ret: JSONArray, warns: JSONArray, fileinfo: IFileInfo) {
        try {
            ret.put(toJSONFileInfo(fileinfo))
        } catch (e: Exception) {
            warns.put(fileinfo.cpath)
        }
    }

    @Throws(JSONException::class)
    fun toJSONFileInfo(fileinfo: IFileInfo): JSONObject {
        return toJSONFileInfo(JSONObject(), fileinfo)
    }

    @Throws(JSONException::class)
    fun toJSONFileInfo(ret: JSONObject, fileinfo: IFileInfo): JSONObject {
        if (fileinfo.rpath == "" && fileinfo.supportHistory) ret.put(Key.supportHistory, true)
        ret.put(Key.name, fileinfo.name)
        ret.put(Key.rpath, fileinfo.rpath)
        ret.put(Key.cpath, fileinfo.cpath)
        toJSONFileStat(ret, fileinfo.stat())
        return ret
    }

    fun toJSONFileStat(ret: JSONObject, stat: IFileStat?): JSONObject {
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
        stat.checksumBytes?.let {
            ret.put(Key.checksum, Hex.encode(it))
        }
        ret.put(Key.perm, stat.perm)
        return ret
    }

    fun filesByName(dir: IFileInfo): Collection<IFileInfo> {
        return dir.readDir(TreeSet<IFileInfo>(IFileInfo.NameComparator.singleton))
    }

    fun fileAndstatsByName(dir: IFileInfo, callback: Fun20<IFileInfo, IFileStat>) {
        for (file in filesByName(dir)) {
            callback(file, file.stat()!!)
        }
    }

    fun isNotexists(fileinfo: JSONObject): Boolean {
        return fileinfo.optBoolean(Key.notexists, false)
    }

    fun createNotexists(name: String?): JSONObject {
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

    /// Find rpaths, of file or directory, which filename containing the given searchtext under srcdir/subdir.
    /// @return Paths in form dstdir/subdir/rpath.
    /// NOTE: This is used by the FilesPanel find files action.
    fun find(ret: MutableCollection<String>, srcdir: File, subdir: String, dstdir: String, searchtext: String) {
        if (!srcdir.isDirectory) return
        val lcpat = searchtext.lowercase(Locale.ROOT)
        val todir = Basepath.joinPath(dstdir, subdir)
        val rpaths = File(srcdir, subdir).bot.collects(FilePathCollectors::pathOfAny)
        for (rpath in rpaths) {
            if (Basepath.name(rpath).lowercase(Locale.ROOT).contains(lcpat)) {
                ret.add(Basepath.joinPath(todir, rpath))
            }
        }
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

    val NOTEXISTS_CONTENT =
        ReadOnlyContent({ 0L }, NotExistsSeekableInputStreamProvider::inputStream)

    fun createNotexists(root: IRootInfo, rpath: String): IFileInfo {
        return ReadOnlyJSONObjectFIleInfo(root, rpath, NOTEXISTS, emptyMap(), NOTEXISTS_CONTENT)
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

private object FileInfoExt {

    fun listOrEmpty(dir: IFileInfo): MutableList<String> {
        return dir.listDir(ArrayList<String>())
    }

    fun filesOrEmpty(dir: IFileInfo): MutableList<IFileInfo> {
        return dir.readDir(ArrayList<IFileInfo>())
    }

    /// @param postorder true for depth first, default is false.
    fun walk3(info: IFileInfo, rpath: String = "", postorder: Boolean = false, callback: Fun30<IFileInfo, String, IFileStat>) {
        info.stat()?.let {
            if (it.isDir) walk31(info, rpath, postorder, callback)
            else callback(info, rpath, it)
        }
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

    /// @param postorder True to walk in postorder depth first, default is preorder depth first.
    fun walk2(info: IFileInfo, postorder: Boolean = false, callback: Fun20<IFileInfo, IFileStat>) {
        info.stat()?.let {
            if (it.isDir) walk21(info, postorder, callback)
            else callback(info, it)
        }
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
        scan31(info, rpath, callback)
    }

    private fun scan31(dir: IFileInfo, rpath: String, callback: Fun31<IFileInfo, String, IFileStat, Boolean>) {
        for (file in dir.readDir(ArrayList())) {
            val filepath = if (rpath.isEmpty()) file.name else rpath + File.separatorChar + file.name
            val filestat = file.stat()!!
            if (callback(file, filepath, filestat) && filestat.isDir)
                scan31(file, filepath, callback)
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

    fun <T, R : MutableCollection<T>> collect(ret: R, dir: IFileInfo, collector: Fun31<IFileInfo, String, IFileStat, T?>): R {
        this.walk3(dir) { file, rpath, stat ->
            collector(file, rpath, stat)?.let {
                ret.add(it)
            }
        }
        return ret
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
    fun deleteSubtrees(dir: IFileInfo, callback: Fun10<IFileInfo>?): Boolean {
        if (dir.stat()?.isDir != true)
            return true
        var ret = true
        for (file in dir.readDir(ArrayList())) {
            if (!deleteTree(file, callback)) ret = false
        }
        return ret
    }

    fun deleteTree(dir: IFileInfo, callback: Fun10<IFileInfo>?): Boolean {
        val stat = dir.stat() ?: return true
        if (!stat.isDir) {
            callback?.invoke(dir)
            return dir.delete()
        }
        var isempty = true
        for (file in dir.readDir(ArrayList())) {
            if (!deleteTree(file, callback)) isempty = false
        }
        return if (isempty) dir.delete() else false
    }

    fun asBytes(fileinfo: IFileInfo): ByteArray {
        return fileinfo.content().inputStream().use { it.readBytes() }
    }

    fun asChars(fileinfo: IFileInfo): CharArray {
        return fileinfo.content().inputStream().reader().use { IOUt.readAll(it) }
    }
}

fun IFileInfo.listOrEmpty(): MutableList<String> {
    return FileInfoExt.listOrEmpty(this)
}

fun IFileInfo.filesOrEmpty(): MutableList<IFileInfo> {
    return FileInfoExt.filesOrEmpty(this)
}

fun IFileInfo.scan3(dirpath: String = "", callback: Fun31<IFileInfo, String, IFileStat, Boolean>) {
    FileInfoExt.scan3(this, dirpath, callback)
}

fun IFileInfo.walk2(postorder: Boolean = false, callback: Fun20<IFileInfo, IFileStat>) {
    FileInfoExt.walk2(this, postorder, callback)
}

fun IFileInfo.walk3(postorder: Boolean = false, rpath: String = "", callback: Fun30<IFileInfo, String, IFileStat>) {
    FileInfoExt.walk3(this, rpath, postorder, callback)
}

fun IFileInfo.deleteEmptySubtrees(): Int {
    return FileInfoExt.deleteEmptySubtrees(this)
}

fun IFileInfo.deleteEmptyTree(): Int {
    return FileInfoExt.deleteEmptyTree(this)
}

fun IFileInfo.deleteSubtrees(callback: Fun10<IFileInfo>?): Boolean {
    return FileInfoExt.deleteSubtrees(this, callback)
}

fun IFileInfo.deleteTree(callback: Fun10<IFileInfo>?): Boolean {
    return FileInfoExt.deleteTree(this, callback)
}

fun <T, R : MutableCollection<T>> IFileInfo.collect(ret: R, collector: Fun31<IFileInfo, String, IFileStat, T?>): R {
    return FileInfoExt.collect(ret, this, collector)
}

fun <R : MutableCollection<IFileInfo>> IFileInfo.files(ret: R): R {
    return collect(ret) { file, _, stat -> if (stat.isFile) file else null }
}

fun <R : MutableCollection<IFileInfo>> IFileInfo.dirs(ret: R): R {
    return collect(ret) { file, _, stat -> if (stat.isDir) file else null }
}

fun <R : MutableCollection<IFileInfo>> IFileInfo.filesAndDirs(ret: R): R {
    return collect(ret) { file, _, stat -> if (stat.isDir || stat.isFile) file else null }
}

fun IFileInfo.collect(collector: Fun31<IFileInfo, String, IFileStat, IFileInfo>): Collection<IFileInfo> {
    return collect(ArrayList<IFileInfo>(), collector)
}

fun IFileInfo.files(): Collection<IFileInfo> {
    return collect(ArrayList<IFileInfo>()) { file, _, stat -> if (stat.isFile) file else null }
}

fun IFileInfo.dirs(): Collection<IFileInfo> {
    return collect(ArrayList<IFileInfo>()) { file, _, stat -> if (stat.isDir) file else null }
}

fun IFileInfo.filesAndDirs(): Collection<IFileInfo> {
    return collect(ArrayList<IFileInfo>()) { file, _, stat -> if (stat.isDir || stat.isFile) file else null }
}

fun IFileInfo.asBytes(): ByteArray {
    return FileInfoExt.asBytes(this)
}

fun IFileInfo.asChars(): CharArray {
    return FileInfoExt.asChars(this)
}
