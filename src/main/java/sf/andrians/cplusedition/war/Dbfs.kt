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

import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun00
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.WatchDog
import org.json.JSONObject
import sf.andrians.ancoreutil.util.io.ByteIOUtil
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.ancoreutil.util.struct.IterableWrapper
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.IDeletedFileStat
import sf.andrians.cplusedition.support.IFileContent
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IFileStat
import sf.andrians.cplusedition.support.IRootInfo
import sf.andrians.cplusedition.support.ISeekableInputStream
import sf.andrians.cplusedition.war.DbBase.K
import sf.andrians.cplusedition.war.Dbfs.DeletedFileStat
import sf.andrians.cplusedition.war.Dbfs.Dir
import sf.andrians.cplusedition.war.Dbfs.Filepath
import sf.andrians.cplusedition.war.Dbfs.IFilepath
import sf.andrians.cplusedition.war.Dbfs.Stat
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.charset.Charset
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.math.min

interface IDbfs {
    val lock: ReentrantReadWriteLock

    /// Cleanup filesystem.
    fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int>

    fun isClosed(): Boolean

    fun <T> transaction(code: Fun01<T>): T

    fun closeDatabase()

    fun onPause()

    fun onResume()

    fun dirPathOf(dirid: Long): String?

    fun stat1(path: Basepath): ResultSet

    fun stat(path: Basepath): Stat?

    fun readDir(path: Basepath): ResultSet

    fun readDirOf(dirid: Long): ResultSet

    /// @return true if destination exists or created successfully.
    fun mkdirs(path: Basepath): Long?

    @Throws(IOException::class)
    fun getInputStream(path: Basepath): Pair<ISeekableInputStream, Stat>

    @Throws(IOException::class)
    fun getOutputStream(path: Basepath, append: Boolean = false): OutputStream

    @Throws(IOException::class)
    fun write(path: Basepath, data: ByteArray, offset: Int, length: Int, xrefs: JSONObject?, timestamp: Long? = null)

    @Throws(IOException::class)
    fun write(path: Basepath, data: InputStream, xrefs: JSONObject?, timestamp: Long? = null)

    fun writeRecovery(path: Basepath, data: InputStream?, timestamp: Long? = null): Boolean

    /// @return true if destination exists and deleted successfully.
    fun delete(path: Basepath): Boolean

    fun delete(stat: Stat): Boolean

    /// Copy src to dst.
    /// If destination exists, it would be overwritten if it is a file and deletable, otherwise copy fail,
    /// Both dst and src are expected to reside in this filesystem.
    @Throws(IOException::class)
    fun copy(dst: Basepath, src: Basepath, timestamp: Long? = null)

    /// This only works on files.
    fun copyTo(dst: OutputStream, src: Basepath)

    /// This works on both file and directory.
    /// This fail if destination already exists.
    /// Both dst and src are expected to reside in this filesystem.
    /// Note that this do not keep a copy of the old file in the trash.
    fun renameTo(dst: Basepath, src: Basepath, timestamp: Long? = null): Boolean

    fun setLastModified(path: Basepath, timestamp: Long): Boolean

    fun setWritable(path: Basepath, writable: Boolean): Boolean

    fun history(root: String, dir: String, name: String, listdir: Boolean, count: Int = -1): List<DeletedFileStat>

    fun searchHistory(root: String, dir: String, name: String, count: Int = -1, predicate: Fun11<IDeletedFileStat, Boolean>): List<DeletedFileStat>

    fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int>

    @Throws(IOException::class)
    fun recover(dst: Basepath, infos: List<IDeletedFileStat>): Pair<Int, Int>

    @Throws(IOException::class)
    fun recover(dst: IFileInfo, infos: List<IDeletedFileStat>): Pair<Int, Int>

    fun updateXrefs(from: IFileInfo, xrefs: JSONObject?)

    fun getInputStream(stat: Stat): InputStream

    /// Remove file entry regardless if it is a file or directory, deleted or not.
    /// If it is a file, its content is also removed.
    fun prune(stat: Stat): Boolean

}

class Dbfs {

    companion object {

        fun openDatabase(dbpath: String, key: String?, keephistory: Boolean, vararg rootdirs: String): IDbfs {
            return Impl(dbpath, keephistory,
                    key,
                    rootdirs).openDatabase(false)
        }

        fun initDatabase(dbpath: String, key: String?, keephistory: Boolean, vararg rootdirs: String): IDbfs {
            return Impl(dbpath, keephistory,
                    key,
                    rootdirs).openDatabase(true)
        }

        internal fun count(result: ResultSet): Long {
            var count = 0L
            while (result.next()) ++count
            return count
        }

        internal fun getLong(result: ResultSet): Long {
            return if (result.next()) result.getLong(1) else 0
        }

        internal fun seqOfStats(result: ResultSet): Sequence<Stat> {
            return sequence {
                while (result.next()) {
                    Stat.from(result)?.let {
                        yield(it)
                    }
                }
            }
        }

        internal fun seqOfDirs(result: ResultSet): Sequence<Dir> {
            return sequence {
                while (result.next()) {
                    yield(Dir.from(result))
                }
            }
        }

        internal fun seqOfContents(result: ResultSet): Sequence<Content> {
            return sequence {
                while (result.next()) {
                    yield(Content.from(result))
                }
            }
        }

        internal fun listOfDeletedFileStat(result: ResultSet, predicate: Fun11<IDeletedFileStat, Boolean>): List<DeletedFileStat> {
            val ret = ArrayList<DeletedFileStat>()
            while (result.next()) {
                DeletedFileStat.from(result)?.let {
                    if (predicate(it)) {
                        ret.add(it)
                    }
                }
            }
            return ret
        }

        fun listOfStats(result: ResultSet): List<Stat> {
            val ret = ArrayList<Stat>()
            while (result.next()) {
                Stat.from(result)?.let {
                    ret.add(it)
                }
            }
            return ret
        }

        internal
        fun listOfDirs(result: ResultSet): List<Dir> {
            val ret = ArrayList<Dir>()
            while (result.next()) {
                ret.add(Dir.from(result))
            }
            return ret
        }

        internal fun getdigester(): MessageDigest {
            return MessageDigest.getInstance("SHA-256")
        }
    }

    private object Table {
        const val files = "f"
        const val dirs = "d"
        const val contents = "c"
        const val xrefs = "x"
        const val xinfos = "i"
    }

    private object Index {
        const val nameIndex = "ni"
        const val dirIdIndex = "di"
        const val dirindex = "pi"
        const val contentIndex = "ci"
        const val fromIndex = "fi"
        const val toIndex = "ti"
    }

    interface IFilepath : IFileInfo, IFileContent {

        val basepath: Basepath // Relative path without leading / relative to root of Dbfs, NOT DbfsRootInfo.

        override fun stat(): Stat?

        override fun fileInfo(rpath: String): IFilepath

        /// @return true if filepath is a file and file size is 0 or filepath is an empty directory.
        fun isEmpty(): Boolean?

        @Throws(IOException::class)
        fun getInputStream2(): Pair<InputStream, Stat>

        @Throws(IOException::class)
        fun getOutputStream(append: Boolean = false): OutputStream

        fun scan3(rpath: String = "", callback: Fun31<IFilepath, String, Stat, Boolean>)

        fun walk3(rpath: String = "", bottomup: Boolean = false, callback: Fun30<IFilepath, String, Stat>)

        fun walk2(bottomup: Boolean = false, callback: Fun20<IFilepath, Stat>)

        fun readDir(): List<Stat>
    }

    class Filepath(
            override val root: DbfsRootInfo,
            override val rpath: String
    ) : IFilepath {

        override val cpath = FileUt.cleanPath(Basepath.joinRpath(root.cpath, rpath)).toString()
        override val basepath = Basepath.from(cpath)

        companion object {
            val SEP = File.separatorChar
        }

        //////////////////////////////////////////////////////////////////////

        override val name = basepath.nameWithSuffix
        override val apath: String get() = SEP + cpath
        override val parent get() = if (rpath.isEmpty()) null else Filepath(root, Basepath.dir(rpath) ?: "")

        override val exists get() = root.fs.stat1(basepath).next()

        override fun stat(): Stat? {
            return root.fs.stat((basepath))
        }

        override fun content(): IFileContent {
            return this
        }

        override fun fileInfo(rpath: String): IFilepath {
            return Filepath(root, Basepath.joinRpath(this.rpath, Basepath.trimLeadingSlash(rpath)))
        }

        override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
            val dir = rpath + SEP
            for (stat in readDir()) {
                ret.add(Filepath(root, dir + stat.name))
            }
            return ret
        }

        override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
            return root.fs.cleanupTrash(predicate)
        }

        override fun mkdirs(): Boolean {
            return root.fs.mkdirs(basepath) != null
        }

        override fun mkparent(): Boolean {
            return parent?.let { root.fs.mkdirs(it.basepath) != null } ?: false
        }

        override fun delete(): Boolean {
            return root.fs.delete(basepath)
        }

        //////////////////////////////////////////////////////////////////////

        override fun getInputStream(): InputStream {
            return root.fs.getInputStream(basepath).first
        }

        override fun getSeekableInputStream(): ISeekableInputStream {
            return root.fs.getInputStream(basepath).first
        }

        override fun getOutputStream(): OutputStream {
            return root.fs.getOutputStream(basepath)
        }

        override fun readBytes(): ByteArray {
            return getInputStream().use { it.readBytes() }
        }

        override fun readText(charset: Charset): String {
            return getInputStream().reader(charset).use { it.readText() }
        }

        override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?, xrefs: JSONObject?) {
            root.fs.write(basepath, data, offset, length, xrefs, timestamp)
        }

        override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
            return root.fs.writeRecovery(basepath, data, timestamp)
        }

        @Throws(IOException::class)
        override fun write(data: InputStream, timestamp: Long?, xrefs: JSONObject?) {
            root.fs.write(basepath, data, xrefs, timestamp)
        }

        //////////////////////////////////////////////////////////////////////

        override fun copyTo(dst: OutputStream) {
            root.fs.copyTo(dst, basepath)
        }

        @Throws(IOException::class)
        override fun copyTo(dst: IFileInfo, timestamp: Long?) {
            val srcstat = stat() ?: throw IOException()
            if (!srcstat.isFile) throw IOException()
            dst.stat()?.let { if (!(it.isFile && dst.delete())) throw IOException() }
            copy1(dst, timestamp)
        }

        @Throws(IOException::class)
        override fun moveTo(dst: IFileInfo, timestamp: Long?) {
            val srcstat = stat() ?: throw IOException()
            if (!srcstat.isFile) throw IOException()
            dst.stat()?.let { if (!(it.isFile && dst.delete())) throw IOException() }
            if (renameTo(dst, timestamp)) {
                return
            }
            copy1(dst, timestamp)
            if (!delete()) throw IOException()
        }

        override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
            dst.root.let {
                if (it is DbfsRootInfo && it.fs === this.root.fs) {
                    return root.fs.renameTo(Basepath.from(dst.cpath), basepath, timestamp)
                }
            }
            return false
        }

        private fun copy1(dst: IFileInfo, timestamp: Long?) {
            dst.root.let {
                if ((it is DbfsRootInfo) && it.fs === this.root.fs) {
                    return root.fs.copy(Basepath.from(dst.cpath), basepath, timestamp)
                }
            }
            getInputStream().use { input ->
                dst.content().write(input, timestamp)
            }
        }

        override fun equals(other: Any?): Boolean {
            return (other is Filepath) && other.root === root && other.basepath == basepath
        }

        override fun hashCode(): Int {
            return basepath.hashCode()
        }

        override fun toJSON(): JSONObject {
            return FileInfoUtil.tojson(this)
        }

        override fun isEmpty(): Boolean? {
            val stat = stat() ?: return null
            if (stat.isDir) {
                return readDir().firstOrNull() == null
            }
            return stat.length == 0L
        }

        override fun readDir(): List<Stat> {
            return listOfStats(root.fs.readDir(basepath))
        }

        override fun setLastModified(timestamp: Long): Boolean {
            return root.fs.setLastModified(basepath, timestamp)
        }

        override fun setWritable(writable: Boolean): Boolean {
            return root.fs.setWritable(basepath, writable)
        }

        @Throws(IOException::class)
        override fun getInputStream2(): Pair<InputStream, Stat> {
            return root.fs.getInputStream(basepath)
        }

        @Throws(IOException::class)
        override fun getOutputStream(append: Boolean): OutputStream {
            return root.fs.getOutputStream(basepath, append)
        }

        override fun scan3(rpath: String, callback: Fun31<IFilepath, String, Stat, Boolean>) {
            val stat = stat()
            if (stat == null || !stat.isDir) throw IOException()
            scan1(this, rpath, callback)
        }

        private fun scan1(dir: IFilepath, rpath: String, callback: Fun31<IFilepath, String, Stat, Boolean>) {
            for (stat in dir.readDir()) {
                val file = dir.fileInfo(stat.name)
                val filepath = if (rpath.isEmpty()) stat.name else "$rpath$SEP${stat.name}"
                if (callback(file, filepath, stat) && stat.isDir) {
                    scan1(file, filepath, callback)
                }
            }
        }

        override fun walk3(rpath: String, bottomup: Boolean, callback: Fun30<IFilepath, String, Stat>) {
            val stat = stat()
            if (stat == null || !stat.isDir) throw IOException()
            walk31(this, rpath, bottomup, callback)
        }

        private fun walk31(
                dir: IFilepath,
                rpath: String,
                bottomup: Boolean,
                callback: Fun30<IFilepath, String, Stat>
        ) {
            for (stat in dir.readDir()) {
                val file = dir.fileInfo(stat.name)
                val filepath = if (rpath.isEmpty()) stat.name else "$rpath$SEP${stat.name}"
                if (!bottomup) callback(file, filepath, stat)
                if (stat.isDir) walk31(file, filepath, bottomup, callback)
                if (bottomup) callback(file, filepath, stat)
            }
        }

        override fun walk2(bottomup: Boolean, callback: Fun20<IFilepath, Stat>) {
            if (stat()?.isDir != true) return
            walk21(this, bottomup, callback)
        }

        private fun walk21(dir: IFilepath, bottomup: Boolean, callback: Fun20<IFilepath, Stat>) {
            for (stat in dir.readDir()) {
                val file = dir.fileInfo(stat.name)
                if (!bottomup) callback(file, stat)
                if (stat.isDir) walk21(file, bottomup, callback)
                if (bottomup) callback(file, stat)
            }
        }
    }

    class Binding(private val stmt: PreparedStatement) {

        init {
            stmt.clearParameters()
        }

        fun bind(index: Int, value: Boolean?): Binding {
            if (value != null) stmt.setBoolean(index, value)
            return this
        }

        fun bind(index: Int, value: Int?): Binding {
            if (value != null) stmt.setInt(index, value)
            return this
        }

        fun bind(index: Int, value: Long?): Binding {
            if (value != null) stmt.setLong(index, value)
            return this
        }

        fun bind(index: Int, value: String?): Binding {
            if (value != null) stmt.setString(index, value)
            return this
        }

        fun bind(index: Int, value: ByteArray?): Binding {
            if (value != null) stmt.setBytes(index, value)
            return this
        }

        fun execute(): Boolean {
            return stmt.execute()
        }

        fun executeQuery(): ResultSet {
            return stmt.executeQuery()
        }

        fun executeUpdate(): Int {
            return stmt.executeUpdate()
        }

        fun insert(): Long? {
            val count = stmt.executeUpdate()
            return if (count == 0) null else {
                val ret = Dbfs.getLong(stmt.generatedKeys)
                if (ret > 0) ret else null
            }
        }
    }

    internal class DbfsLock(private val onclose: Fun00) : Closeable {
        private var closed = false
        override fun close() {
            if (!closed) {
                closed = true
                onclose()
            }
        }
    }

    internal class Impl(
            dbpath: String,
            private val keepHistory: Boolean,
            private val key: String?,
            private val roots: Array<out String>
    ) : DbBase(dbpath), IDbfs {
        internal lateinit var dirs: Dirs
        internal lateinit var files: Files
        internal lateinit var contents: Contents
        internal lateinit var xrefs: Xrefs
        internal lateinit var xinfos: Xinfos

        companion object {
            val FILE_IN_USE = MSG.get().getString(R.string.FileInUse)
        }

        internal fun openDatabase(forceinit: Boolean = false): IDbfs {
            lock.write {
                try {
                    if (connection != null) closeDatabase()
                    val conn = DriverManager.getConnection("jdbc:sqlite:${dbpath}").also { connection = it }
                    conn.autoCommit = false
                    conn.createStatement().use { stmt ->
                        transaction {
                            stmt.execute("PRAGMA key=\"x'${key}'\"")
                            stmt.execute("PRAGMA secure_delete=1")
                            stmt.execute("PRAGMA temp_store=3")
                            stmt.execute("PRAGMA sychronous=2")
                            stmt.execute("PRAGMA auto_vacuum=2")
                            if (forceinit || !isInitialized()) {
                                initialize(stmt)
                            }
                        }
                    }
                    dirs = Dirs(conn)
                    files = Files(conn)
                    contents = Contents(conn)
                    xrefs = Xrefs(conn)
                    xinfos = Xinfos(conn)
                    
                    return this
                } catch (e: Throwable) {
                    throw IOException("# Opendatabase failed: ${Basepath.nameWithoutSuffix(dbpath)}"
                    )
                }
            }
        }

        private fun initialize(stmt: Statement) {
            lock.write {
                
                transaction {
                    stmt.executeUpdate("drop table if exists ${Table.contents}")
                    stmt.executeUpdate("drop table if exists ${Table.dirs}")
                    stmt.executeUpdate("drop table if exists ${Table.files}")
                    stmt.executeUpdate("drop table if exists ${Table.xrefs}")
                    stmt.executeUpdate("drop table if exists ${Table.xinfos}")
                    Contents.createTable(stmt)
                    Dirs.createTable(stmt, roots)
                    Files.createTable(stmt, roots)
                    Xrefs.createTable(stmt)
                    Xinfos.createTable(stmt)
                }
            }
        }

        override fun onResume() {
            lock.write {
                paused = false
                if (isClosed()) openDatabase()
            }
        }

        /// @param predicate return true to delete a deleted file.
        /// If predicate is null, cleanup directories only and skip vacuum.
        /// @return (deletedFilesCount, deletedDirsCount)
        override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {

            var deletedfiles = 0
            var deleteddirs = 0
            lock.write {
                transaction {
                    val dirstats = ArrayList<Stat>()
                    connection!!.createStatement().use { stmt ->
                        val result = stmt.executeQuery("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path}" +
                                " from ${Table.files}, ${Table.dirs}" +
                                " where ${Table.files}.${Files.Col.dirId}=${Table.dirs}.${Dirs.Col.id}")
                        var filescount = 0L
                        while (result.next()) {
                            val deletedstat = DeletedFileStat.from(result) ?: continue
                            val stat = deletedstat.stat
                            ++filescount
                            if (!stat.isDeleted) continue
                            if (stat.isDir) {
                                dirstats.add(stat)
                                continue
                            }
                            if (stat.isFile) {
                                if (predicate == null || !predicate(deletedstat)) continue
                                files.prune(stat.id)
                                contents.deleteContent(stat.id)
                                ++deletedfiles
                            }
                        }
                    }
                    
                    connection!!.prepareStatement("select count(${Files.Col.id}) from ${Table.files}" +
                            " where ${Files.Col.dirId}=? or (${Files.Col.id}=? and ${Files.Col.isDeleted}=false)").use { usecount ->
                        val dirsInuse = TreeSet<Long>()
                        connection!!.createStatement().use { stmt ->
                            val list = stmt.executeQuery("select ${Dirs.Col.id} from ${Table.dirs}")
                            while (list.next()) {
                                val id = list.getLong(1)
                                if (Dbfs.getLong(Binding(usecount)
                                                .bind(1, id)
                                                .bind(2, id)
                                                .executeQuery()) > 0L) {
                                    dirsInuse.add(id)
                                    continue
                                }
                                dirs.prune(id)
                            }
                        }
                        while (true) {
                            var modified = false
                            val it = dirstats.iterator()
                            while (it.hasNext()) {
                                val stat = it.next()
                                if (dirsInuse.contains(stat.id)) continue
                                files.prune(stat.id)
                                it.remove()
                                deleteddirs += 1
                                if (Dbfs.getLong(Binding(usecount)
                                                .bind(1, stat.dirId)
                                                .bind(2, stat.dirId)
                                                .executeQuery()) == 0L) {
                                    dirs.prune(stat.dirId)
                                    dirsInuse.remove(stat.dirId)
                                    modified = true
                                }
                            }
                            if (!modified) break
                        }
                        
                    }
                    if (predicate != null) {
                        files.incrementalVacuum(connection!!)
                        files.optimize(connection!!)
                    }
                    connection!!.createStatement().use {
                        val filescount = Dbfs.getLong(it.executeQuery("select count(${Files.Col.id}) from ${Table.files}"))
                        val dirscount = Dbfs.getLong(it.executeQuery("select count(${Dirs.Col.id}) from ${Table.dirs}"))
                        val contentscount = Dbfs.getLong(it.executeQuery("select count(${Contents.Col.id}) from ${Table.contents}"))
                        
                    }
                }
            }
            return Pair(deletedfiles, deleteddirs)
        }

        override fun dirPathOf(dirid: Long): String? {
            return dirs.pathOf(dirid)
        }

        override fun stat1(path: Basepath): ResultSet {
            lock.read {
                return files.stat((path.dir ?: ""), path.nameWithSuffix)
            }
        }

        override fun stat(path: Basepath): Stat? {
            lock.read {
                return Stat.optional(stat1(path))
            }
        }

        override fun readDir(path: Basepath): ResultSet {
            lock.read {
                return files.readDir(path.toString())
            }
        }

        override fun readDirOf(dirid: Long): ResultSet {
            lock.read {
                return files.readDirOf(dirid)
            }
        }

        /// @return Files.id if success, null if fail.
        override fun mkdirs(path: Basepath): Long? {
            try {
                lock.write {
                    return transaction {
                        return@transaction mkdirs1(path)
                    }
                }
            } catch (_: Exception) {
                return null
            }
        }

        @Throws(IOException::class)
        private fun mkdirs1(path: Basepath): Long? {
            /// Note that this throws IOException if rollback is required.
            val dir = path.dir ?: ""
            val name = path.nameWithSuffix
            if (dir.isEmpty() && name.isEmpty()) return 1L
            val st = stat(path)
            if (st != null) {
                if (st.isDir) return st.id
                return null
            }
            val dirid: Long?
            if (dir.isNotEmpty()) {
                dirid = mkdirs1(Basepath.from(dir)) ?: return null
            } else {
                dirid = 1L
            }
            val time = System.currentTimeMillis()
            val fileid = files.mkfile(
                    dirid,
                    name,
                    time,
                    size = 0,
                    isdir = true
            ) ?: return null
            dirs.mkdir(fileid, path.toString()) ?: throw IOException()
            return fileid
        }

        @Throws(IOException::class)
        override fun getInputStream(path: Basepath): Pair<ISeekableInputStream, Stat> {
            lock.read {
                val stat = stat(path) ?: throw IOException()
                val stream = getInputStream1(stat)
                return Pair(stream, stat)
            }
        }

        @Throws(IOException::class)
        override fun getOutputStream(path: Basepath, append: Boolean): OutputStream {
            lock.write {
                return transaction {
                    val stat = stat(path)
                    if (stat != null) {
                        if (!stat.isFile) throw IOException()
                        if (append) {
                            return@transaction appending(stat)
                        }
                        if (!deleteEmptyDirOrFile(stat)) throw IOException(MSG.get().getString(R.string.DeleteFailed))
                    }
                    val dirid = stat(Basepath.from(path.dir ?: ""))?.id
                            ?: throw IOException()
                    val name = path.nameWithSuffix
                    val fileid = files.mkfile(
                            dirid,
                            name,
                            System.currentTimeMillis(),
                            size = 0,
                            isdir = false)
                            ?: throw IOException()
                    val filelock = lockFile(fileid, write = true) ?: throw IOException(FILE_IN_USE)
                    return@transaction DbfsOutputStream(this, fileid, name, filelock)
                }
            }
        }

        private fun appending(stat: Stat): OutputStream {
            val filelock = lockFile(stat.id, write = true) ?: throw IOException(FILE_IN_USE)
            val digester = getdigester()
            var last: Content? = null
            for (content in seqOfContents(contents.readContent(stat.id))) {
                digester.update(content.data)
                last = content
            }
            val info = AppendInfo(stat.length, digester, if (last != null && last.data.size < K.PART_SIZE) last else null)
            return DbfsOutputStream(this, stat.id, stat.name, filelock, info)
        }

        override fun write(path: Basepath, data: ByteArray, offset: Int, length: Int, xrefs: JSONObject?, timestamp: Long?) {
            lock.write {
                transaction {
                    val dirid = prepareToWrite(path)
                    val modified = timestamp ?: System.currentTimeMillis()
                    val fileid = mkfile(dirid, path, modified)
                    val (size, checksum) = writeContent(fileid, data, offset, length)
                    files.updateFile(fileid, size, modified, checksum)
                    updatexrefs(fileid, xrefs)
                }
            }
        }

        override fun write(path: Basepath, data: InputStream, xrefs: JSONObject?, timestamp: Long?) {
            lock.write {
                transaction {
                    val dirid = prepareToWrite(path)
                    val modified = timestamp ?: System.currentTimeMillis()
                    val fileid = mkfile(dirid, path, modified)
                    val (size, checksum) = writeContent(fileid, data)
                    files.updateFile(fileid, size, modified, checksum)
                    updatexrefs(fileid, xrefs)
                }
            }
        }

        private fun updatexrefs(from: Long, xrefs: JSONObject?) {
            if (xrefs == null) return
            this.xrefs.delete(from)
            for (cpath in IterableWrapper.wrap(xrefs.keys())) {
                val to = (xinfos.idOf(cpath) ?: xinfos.add(cpath)) ?: continue
                this.xrefs.insert(from, to)
            }
        }

        override fun updateXrefs(from: IFileInfo, xrefs: JSONObject?) {
            val stat = stat(Basepath.from(from.cpath)) ?: return
            updatexrefs(stat.id, xrefs)
        }

        override fun writeRecovery(path: Basepath, data: InputStream?, timestamp: Long?): Boolean {
            lock.write {
                return transaction {
                    val now = System.currentTimeMillis()
                    val modified = timestamp ?: now
                    try {
                        if (data == null) {
                            val srcstat = stat(path) ?: throw IOException()
                            val dirid = prepareToWriteRecovery(path)
                            lockFile(srcstat.id, false)?.use {
                                val dstid = mkdeleted(dirid, path, modified, srcstat.length, now, srcstat.checksumBytes)
                                copyContent(dstid, srcstat.id)
                            } ?: throw IOException(FILE_IN_USE)
                        } else {
                            val dirid = prepareToWriteRecovery(path)
                            val fileid = mkdeleted(dirid, path, modified, 0, now, null)
                            val (size, checksum) = writeContent(fileid, data)
                            files.updateFile(fileid, size, modified, checksum)
                        }
                        return@transaction true
                    } catch (e: Exception) {
                        return@transaction false
                    }
                }
            }
        }

        private fun mkdeleted(dirid: Long, path: Basepath, modified: Long, size: Long, lastDeleted: Long, checksum: ByteArray?): Long {
            return files.mkfile(
                    dirid,
                    path.nameWithSuffix,
                    modified,
                    size,
                    isdir = false,
                    writable = true,
                    isDeleted = true,
                    lastDeleted = lastDeleted,
                    checksum = checksum)
                    ?: throw IOException()
        }

        private fun mkfile(dirid: Long, path: Basepath, modified: Long, size: Long = 0, checksum: ByteArray? = null): Long {
            return files.mkfile(
                    dirid,
                    path.nameWithSuffix,
                    modified,
                    size,
                    isdir = false,
                    writable = true,
                    isDeleted = false,
                    lastDeleted = 0,
                    checksum = checksum)
                    ?: throw IOException()
        }

        private fun writeContent(fileid: Long, data: ByteArray, offset: Int, length: Int): Pair<Long, ByteArray> {
            val digester = getdigester()
            var remaining = length
            var ioffset = offset
            var ooffset = 0L
            while (remaining > 0) {
                val n = if (remaining > K.PART_SIZE) K.PART_SIZE else remaining
                val b = if (ioffset == 0 && n == data.size) data else data.copyOfRange(ioffset, n)
                contents.writeContent(fileid, ooffset, n, b)
                digester.update(data, offset, n)
                ooffset += n
                ioffset += n
                remaining -= n
            }
            return Pair(ooffset, digester.digest())
        }

        private fun writeContent(fileid: Long, data: InputStream): Pair<Long, ByteArray> {
            val digester = getdigester()
            var size = 0L
            var offset = 0L
            val buf = ByteArray(K.PART_SIZE)
            readdata(data, buf) { n ->
                digester.update(buf, 0, n)
                contents.writeContent(fileid, offset, n, if (n == buf.size) buf else buf.copyOf(n))
                size += n
                offset += n
            }
            return Pair(size, digester.digest())
        }

        private fun readdata(input: InputStream, buf: ByteArray, callback: Fun10<Int>) {
            while (true) {
                val n = ByteIOUtil.readWhilePossible(input, buf, 0, buf.size)
                if (n < 0) throw IOException()
                if (n > 0) callback(n)
                if (n < buf.size) break
            }
        }

        override fun delete(path: Basepath): Boolean {
            lock.write {
                return transaction {
                    val stat = stat(path) ?: return@transaction false
                    return@transaction deleteDirOrFile(stat)
                }
            }
        }

        override fun delete(stat: Stat): Boolean {
            lock.write {
                return transaction {
                    return@transaction deleteDirOrFile(stat)
                }
            }
        }

        private fun deleteDirOrFile(stat: Stat): Boolean {
            if (stat.isDir) {
                if (!files.isEmptyDir(stat.id)) return false
                return deleteEmptyDirOrFile(stat)
            }
            return deleteEmptyDirOrFile(stat)
        }

        private fun prepareToWrite(dst: Basepath): Long {
            val dststat = stat(dst)
            if (dststat != null) {
                if (!dststat.isFile) throw IOException()
                if (!deleteDirOrFile(dststat)) throw IOException()
            }
            return mkdirs(Basepath.from(dst.dir ?: "")) ?: throw IOException()
        }

        private fun prepareToWriteRecovery(dst: Basepath): Long {
            val dir = dst.dir ?: ""
            dirs.idOf(dir)?.let { return it }
            val basepath = Basepath.from(dir)
            val dirid = prepareToWriteRecovery(basepath)
            val now = System.currentTimeMillis()
            val fileid = files.mkfile(
                    dirid = dirid,
                    name = basepath.nameWithSuffix,
                    modified = now,
                    size = 0,
                    isdir = true,
                    writable = true,
                    isDeleted = true,
                    lastDeleted = now) ?: throw IOException()
            dirs.mkdir(fileid, dst.toString())
            return fileid
        }

        private fun deleteEmptyDirOrFile(stat: Stat): Boolean {
            lockFile(stat.id, write = true)?.use {
                return deleteLockedEmptyDirOrFile(stat)
            } ?: return false
        }

        private fun deleteLockedEmptyDirOrFile(stat: Stat): Boolean {
            val count: Int
            if (keepHistory) {
                count = files.delete(stat.id, System.currentTimeMillis())
            } else {
                count = files.prune(stat.id)
                if (stat.isFile) {
                    contents.deleteContent(stat.id)
                } else if (stat.isDir) {
                    dirs.prune(stat.id)
                }
            }
            return count > 0
        }

        /// Note that this only work on src file that is not deleted.
        override fun copy(dst: Basepath, src: Basepath, timestamp: Long?) {
            lock.write {
                val srcstat = stat(src) ?: throw IOException()
                transaction {
                    copy1(dst, srcstat.id, srcstat, timestamp)
                }
            }
        }

        /// Note that this work regardless if src file that is deleted or not.
        private fun copy1(dst: Basepath, srcid: Long, srcstat: IFileStat, timestamp: Long?) {
            lockFile(srcid, false)?.use {
                val dirid = prepareToWrite(dst)
                val modified = timestamp ?: System.currentTimeMillis()
                val dstid = mkfile(dirid, dst, modified, srcstat.length, srcstat.checksumBytes)
                copyContent(dstid, srcid)
            } ?: throw IOException(FILE_IN_USE)
        }

        private fun copyContent(dstid: Long, srcid: Long) {
            for (content in seqOfContents(contents.readContent(srcid))) {
                contents.writeContent(dstid, content.offset, content.length, content.data)
            }
        }

        /// Note that this only works if src exists, ie. not deleted.
        override fun copyTo(dst: OutputStream, src: Basepath) {
            lock.write {
                transaction {
                    val srcstat = stat(src) ?: throw IOException()
                    copyto1(dst, srcstat.id)
                }
            }
        }

        /// Note that this works regardless if src is deleted or not.
        /// If no content exists for the given id, nothing is written and without error.
        private fun copyto1(dst: OutputStream, id: Long) {
            lockFile(id, false)?.use {
                for (content in seqOfContents(contents.readContent(id))) {
                    dst.write(content.data, 0, content.length)
                }
            } ?: throw IOException(FILE_IN_USE)
        }

        override fun renameTo(dst: Basepath, src: Basepath, timestamp: Long?): Boolean {
            lock.write {
                return transaction {
                    try {
                        rename1(dst, src, timestamp)
                        return@transaction true
                    } catch (_: Exception) {
                        return@transaction false
                    }
                }
            }
        }

        private fun rename1(dst: Basepath, src: Basepath, timestamp: Long?) {
            val srcstat = stat(src) ?: throw IOException()
            if (stat(dst) != null) throw IOException()
            lockFile(srcstat.id, write = true)?.use {
                val dirid = mkdirs(Basepath.from(dst.dir ?: "")) ?: throw IOException()
                val modified = timestamp ?: System.currentTimeMillis()
                files.rename(srcstat.id, dirid, dst.nameWithSuffix, modified)
                if (srcstat.isDir) {
                    val dstpath = dst.toString()
                    dirs.rename(srcstat.id, dstpath)
                    rename2(srcstat, dstpath)
                }
            } ?: throw IOException(FILE_IN_USE)
        }

        private fun rename2(srcstat: Stat, dirpath: String) {
            for (child in Dbfs.listOfStats(readDirOf(srcstat.id))) {
                if (!child.isDir) continue
                val childpath = Basepath.joinRpath(dirpath, child.name)
                dirs.rename(child.id, childpath)
                rename2(child, childpath)
            }
        }

        override fun setLastModified(path: Basepath, timestamp: Long): Boolean {
            lock.write {
                return transaction {
                    val stat = stat(path) ?: return@transaction false
                    return@transaction files.updateFile(stat.id, stat.length, timestamp, stat.checksumBytes) > 0
                }
            }
        }

        override fun setWritable(path: Basepath, writable: Boolean): Boolean {
            lock.write {
                return transaction {
                    val stat = stat(path) ?: return@transaction false
                    return@transaction files.setWritable(stat.id, writable) > 0
                }
            }
        }

        override fun history(root: String, dir: String, name: String, listdir: Boolean, count: Int): List<DeletedFileStat> {
            val rootslash = root + File.separatorChar
            lock.read {
                if (name.isEmpty()) {
                    val seen = TreeSet<String>()
                    if (dir == root && listdir) {
                        return listOfDeletedFileStat(files.historyDirsOnly(count)) {
                            it.stat.isDir
                                    && (it.dir == "" && it.name == root || it.dir == root || it.dir.startsWith((rootslash)))
                                    && seen.add(Basepath.joinRpath(it.dir, it.name))
                        }
                    } else {
                        return listOfDeletedFileStat(files.historyOfDir(dir, count)) {
                            seen.add(Basepath.joinRpath(it.dir, it.name))
                        }
                    }
                } else {
                    return listOfDeletedFileStat(files.historyOfFile(dir, name, count)) {
                        true
                    }
                }
            }
        }

        override fun searchHistory(
                root: String,
                dir: String,
                name: String,
                count: Int,
                predicate: Fun11<IDeletedFileStat, Boolean>
        ): List<DeletedFileStat> {
            lock.read {
                if (name.isEmpty()) {
                    val seen = TreeSet<String>()
                    if (dir == root) {
                        val rootslash = root + File.separatorChar
                        return listOfDeletedFileStat(files.historyFilesOnly(count)) {
                            seen.add(Basepath.joinRpath(it.dir, it.name))
                                    && (it.dir == root || it.dir.startsWith((rootslash)))
                                    && predicate(it)
                        }
                    } else {
                        return listOfDeletedFileStat(files.historyOfDir(dir, count)) {
                            seen.add(Basepath.joinRpath(it.dir, it.name))
                                    && predicate(it)
                        }
                    }
                } else {
                    return listOfDeletedFileStat(files.historyOfFile(dir, name, count)) {
                        predicate(it)
                    }
                }
            }
        }

        override fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int> {
            lock.write {
                return transaction {
                    var count = 0
                    for (info in infos) {
                        val dstat = DeletedFileStat.from(info) ?: continue
                        val stat = dstat.stat
                        if (stat.isFile) {
                            if (all) {
                                val dir = dstat.dir
                                val name = dstat.name
                                val list = listOfDeletedFileStat(files.historyOfFile(dir, name, -1)) { it.stat.isFile }
                                for (deleted in list) {
                                    files.prune(deleted.stat.id)
                                    contents.deleteContent(deleted.stat.id)
                                    ++count
                                }
                            } else {
                                files.prune(dstat.id)
                                contents.deleteContent(dstat.id)
                                ++count
                            }
                        }
                    }
                    val (_, dirs) = this.cleanupTrash(null)
                    return@transaction Pair(count, dirs)
                }
            }
        }

        /// @param dst For single file recovery, dst must be a file.
        /// For multiple file recovery, dst mut be a directory.
        @Throws(IOException::class)
        override fun recover(dst: Basepath, infos: List<IDeletedFileStat>): Pair<Int, Int> {
            fun recover1(dst: Basepath, info: IDeletedFileStat): Boolean {
                try {
                    val stat = Stat.optional(files.historyOf(info.id)) ?: throw IOException()
                    copy1(dst, info.id, stat, stat.lastModified)
                    return true
                } catch (e: Exception) {
                    return false
                }
            }

            var oks = 0
            var fails = 0
            lock.write {
                transaction {
                    if (infos.size == 1) {
                        if (recover1(dst, infos[0])) ++oks else ++fails
                    } else {
                        for (info in infos) {
                            if (recover1(Basepath.fromClean(dst, info.name), info)) ++oks else ++fails
                        }
                    }
                }
            }
            return Pair(oks, fails)
        }

        override fun recover(dst: IFileInfo, infos: List<IDeletedFileStat>): Pair<Int, Int> {
            fun recover1(dst: IFileInfo, info: IDeletedFileStat): Boolean {
                try {
                    dst.content().getOutputStream().use {
                        copyto1(it, info.id)
                    }
                    dst.setLastModified(info.stat.lastModified)
                    return true
                } catch (e: Exception) {
                    return false
                }
            }

            var oks = 0
            var fails = 0
            lock.write {
                transaction {
                    if (infos.size == 1) {
                        if (recover1(dst, infos[0])) ++oks else ++fails
                    } else {
                        for (info in infos) {
                            if (recover1(dst.fileInfo(info.name), info)) ++oks else ++fails
                        }
                    }
                }
            }
            return Pair(oks, fails)
        }

        override fun getInputStream(stat: Stat): InputStream {
            lock.read {
                return getInputStream1(stat)
            }
        }

        @Throws(IOException::class)
        private fun getInputStream1(stat: Stat): ISeekableInputStream {
            if (!stat.isFile) throw IOException()
            val lock = lockFile(stat.id, false) ?: throw IOException(FILE_IN_USE)
            return DbfsInputStream(connection!!, stat, lock)
        }

        override fun prune(stat: Stat): Boolean {
            lock.write {
                try {
                    transaction {
                        if (stat.isDir) {
                            if (files.isEmptyDir(stat.id)) {
                                files.delete(stat.id, System.currentTimeMillis())
                            }
                        } else if (stat.isFile) {
                            files.prune(stat.id)
                            contents.deleteContent(stat.id)
                        }
                    }
                    return true
                } catch (e: Throwable) {
                    return false
                }
            }
        }

        private fun isInitialized(): Boolean {
            return connection!!.metaData.getTables(null, null, Table.files, null).next()
        }

    }

    internal class AppendInfo(
            val size: Long,
            val digester: MessageDigest,
            val content: Content?
    )

    /// @parma resultSet Non-empty rows of content table with same contentId order by offset asc.
    internal class DbfsInputStream(
            conn: Connection,
            stat: Stat,
            private val filelock: Closeable
    ) : ISeekableInputStream() {

        private val lock = ReentrantLock()
        private val provider = ContentProvider(conn, stat.id)
        private val size = stat.length
        private var closed = false
        private var watchdog = WatchDog(K.AUTO_CLOSE) {
            
        }

        companion object {
            private val EMPTY = ByteArray(0)
        }

        class ContentCache(conn: Connection, id: Long) {
            val stmt = conn.prepareStatement("select * from ${Table.contents} where ${Table.contents}.${Contents.Col.id}=?")
            var infos: List<ContentInfo>
            var cache = ArrayDeque<Content>()

            init {
                val result = Binding(conn.prepareStatement("select ${Contents.Col.id}," +
                        " ${Contents.Col.offset}, ${Contents.Col.length} from ${Table.contents}" +
                        " where ${Table.contents}.${Contents.Col.fileId}=?" +
                        " order by ${Table.contents}.${Contents.Col.offset} asc"))
                        .bind(1, id)
                        .executeQuery()
                infos = ContentInfo.from(result)
            }

            fun get(position: Long): Content? {
                for (index in 0 until infos.size) {
                    val info = infos[index]
                    if (info.offset <= position && position < info.end) return _get(info, position)
                }
                return null
            }

            fun close() {
                stmt.close()
            }

            private fun _get(info: ContentInfo, position: Long): Content {
                for (c in cache) {
                    if (c.offset == info.offset) {
                        cache.remove(c)
                        cache.addLast(c)
                        
                        return c
                    }
                }
                val ret = Content.from(Binding(stmt).bind(1, info.contentid).executeQuery())
                if (cache.size >= 3) cache.removeFirst()
                cache.addLast(ret)
                
                return ret
            }
        }

        class ContentProvider(conn: Connection, id: Long) {
            val cache = ContentCache(conn, id)
            var content: Content = cache.get(0) ?: Content(0, 0, 0, ByteArray(0))
            var position = 0L

            @Throws(IOException::class)
            fun read(): Int {
                if (position < content.offset || position >= content.end) {
                    content = cache.get(position) ?: return -1
                }
                val index = (position - content.offset).toInt()
                ++position
                return content.data[index].toInt() and 0xff
            }

            @Throws(IOException::class)
            fun read(b: ByteArray, off: Int, len: Int): Int {
                if (len == 0) return 0
                val dstend = off + len
                if (len < 0 || off < 0 || dstend > b.size)
                    throw IOException()
                var dstoff = off
                var count = len
                while (dstoff < dstend) {
                    var remaining = (content.end - position).toInt()
                    if (remaining <= 0) {
                        content = cache.get(position) ?: run {
                            val written = dstoff - off
                            return if (written > 0) written else -1
                        }
                        remaining = content.length
                    }
                    val srcoffset = (position - content.offset).toInt()
                    val n = if (count > remaining) remaining else count
                    System.arraycopy(content.data, srcoffset, b, dstoff, n)
                    position += n
                    dstoff += n
                    count -= n
                }
                val written = dstoff - off
                return if (written > 0) written else -1
            }

            /// @return 0 if end of file is reached, otherwise number of bytes skipped.
            @Throws(IOException::class)
            fun seek(pos: Long): Boolean {
                if (position == pos) return true
                if (pos >= content.offset && pos < content.end) {
                    position = pos
                    return true
                }
                content = cache.get(pos) ?: return false
                position = pos
                return true
            }

            fun close() {
                cache.close()
            }
        }

        override fun read(): Int {
            lock.withLock {
                watchdog.watch()
                return provider.read()
            }
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            lock.withLock {
                watchdog.watch()
                return provider.read(b, off, len)
            }
        }

        override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
            lock.withLock {
                watchdog.watch()
                if (!provider.seek(position)) return -1
                return provider.read(buffer, offset, size)
            }
        }

        override fun getSize(): Long {
            return size
        }

        override fun getPosition(): Long {
            return provider?.position ?: 0L
        }

        override fun setPosition(pos: Long) {
            readAt(pos, EMPTY, 0, 0)
        }

        override fun close() {
            lock.withLock {
                if (closed) return
                closed = true
                watchdog.cancel()
                super.close()
                provider.close()
                filelock.close()
            }
        }
    }

    internal class DbfsOutputStream(
            private val fs: Impl,
            private val fileId: Long,
            name: String,
            private val filelock: Closeable,
            info: AppendInfo? = null
    ) : OutputStream() {
        private var updating = false
        private var size = 0L
        private var offset = 0L
        private var length = 0
        private var start = 0
        private var digester: MessageDigest
        private val data = ByteArray(K.PART_SIZE)
        private var watchdog = WatchDog(K.AUTO_CLOSE) {
            
            this.close()
        }
        private val lock = ReentrantLock()
        private var closed = false
        private val timer = StepWatch()

        init {
            if (info?.content != null && info.content.length < K.PART_SIZE) {
                size = info.size
                digester = info.digester
                updating = true
                val it = info.content
                offset = it.offset
                length = it.length
                start = length
                System.arraycopy(it.data, 0, data, 0, length)
            } else {
                digester = getdigester()
            }
        }

        override fun write(b: Int) {
            lock.withLock {
                watchdog.watch()
                while (length >= data.size) flushnow()
                data[length] = (b and 0xff).toByte()
                ++length
            }
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            lock.withLock {
                watchdog.watch()
                var index = off
                var remaining = len
                while (remaining > 0) {
                    while (length >= data.size) flushnow()
                    val available = data.size - length
                    val count = min(available, remaining)
                    System.arraycopy(b, index, data, length, count)
                    length += count
                    index += count
                    remaining -= count
                }
            }
        }

        override fun flush() {
        }

        override fun close() {
            lock.withLock {
                if (closed) return
                closed = true
                watchdog.cancel()
                fs.lock.write {
                    fs.transaction {
                        flushnow()
                        fs.files.updateFile(fileId, size, System.currentTimeMillis(), digester.digest())
                        filelock.close()
                    }
                }
            }
        }

        private fun flushnow() {
            lock.withLock {
                watchdog.watch()
                fs.lock.write {
                    fs.transaction {
                        val content = if (length == data.size) data else data.copyOf(length)
                        if (updating) {
                            fs.contents.updateContent(fileId, offset, length, content)
                            updating = false
                        } else {
                            fs.contents.writeContent(fileId, offset, length, content)
                        }
                        val count = length - start
                        digester.update(data, start, count)
                        offset += length
                        size += count
                        start = 0
                        length = 0
                    }
                }
            }
        }
    }

    internal class Dirs(conn: Connection) {

        internal object Col {
            const val id = "f"
            const val path = "p"
        }

        companion object {
            fun createTable(stmt: Statement, roots: Array<out String>) {
                stmt.executeUpdate("create table ${Table.dirs} (" +
                        "${Col.id} integer primary key" +
                        ", ${Col.path} text" +
                        ")")
                stmt.executeUpdate("create index ${Index.dirindex} on ${Table.dirs}(${Col.path})")
                stmt.executeUpdate("insert into ${Table.dirs} (${Col.path}) values ('')")
                for (root in roots) {
                    stmt.executeUpdate("insert into ${Table.dirs} (${Col.path}) values ('${root}')")
                }
            }
        }

        private val pathOfStatement = conn.prepareStatement("select ${Col.path} from ${Table.dirs} where ${Col.id}=?")

        fun pathOf(id: Long): String? {
            val result = Binding(pathOfStatement)
                    .bind(1, id)
                    .executeQuery()
            return if (result.next()) result.getString(Col.path) else null
        }

        private val idOfStatement = conn.prepareStatement("select ${Col.id} from ${Table.dirs} where ${Col.path}=?")

        fun idOf(path: String): Long? {
            val result = Binding(idOfStatement)
                    .bind(1, path)
                    .executeQuery()
            return if (result.next()) result.getLong(Col.id) else null
        }

        private val mkdirStatement = conn.prepareStatement("insert into ${Table.dirs} (${Col.id}, ${Col.path}) values (?, ?)")

        fun mkdir(id: Long, path: String): Long? {
            return Binding(mkdirStatement)
                    .bind(1, id)
                    .bind(2, path)
                    .insert()
        }

        private val renameStatement = conn.prepareStatement("update ${Table.dirs}" +
                " set ${Col.path}=?" +
                " where ${Col.id}=?")

        fun rename(id: Long, path: String): Int {
            return Binding(renameStatement)
                    .bind(1, path)
                    .bind(2, id)
                    .executeUpdate()
        }

        private val pruneStatement = conn.prepareStatement("delete from ${Table.dirs} where ${Col.id}=?")

        fun prune(dirid: Long): Int {
            return Binding(pruneStatement)
                    .bind(1, dirid)
                    .executeUpdate()
        }

        private val everythingStatement = conn.prepareStatement("select * from ${Table.dirs}")

        fun everything(): ResultSet {
            return everythingStatement.executeQuery()
        }
    }

internal
    class Dir(
            val id: Long,
            val path: String
    ) {
        companion object {
            fun from(result: ResultSet): Dir {
                return Dir(
                        result.getLong(Dirs.Col.id),
                        result.getString(Dirs.Col.path)
                )
            }
        }

        override fun toString(): String {
            return "Dir($id, \"$path\")"
        }
    }

    internal class Files(conn: Connection) {
        internal object Col {
            const val id = "i"
            const val dirId = "d"
            const val name = "n"
            const val lastModified = "m"
            const val length = "s"
            const val isDir = "r"
            const val writable = "w"
            const val isDeleted = "x"
            const val lastDeleted = "l"
            const val checksum = "c"
        }

        companion object {
            fun createTable(stmt: Statement, roots: Array<out String>) {
                stmt.executeUpdate("create table ${Table.files} (" +
                        "${Col.id} integer primary key autoincrement" +
                        ", ${Col.dirId} integer" +
                        ", ${Col.name} text" +
                        ", ${Col.lastModified} integer" +
                        ", ${Col.length} integer" +
                        ", ${Col.isDir} boolean" +
                        ", ${Col.writable} boolean" +
                        ", ${Col.isDeleted} boolean" +
                        ", ${Col.lastDeleted} integer" +
                        ", ${Col.checksum} blob" +
                        ")")
                stmt.executeUpdate("create index ${Index.dirIdIndex} on ${Table.files}(${Col.dirId})")
                stmt.executeUpdate("create index ${Index.nameIndex} on ${Table.files}(${Col.name})")
                val date = System.currentTimeMillis()
                stmt.executeUpdate("insert into ${Table.files}" +
                        " (${Col.dirId}, ${Col.name}, ${Col.lastModified}, ${Col.length}," +
                        " ${Col.isDir}, ${Col.writable}, ${Col.isDeleted})" +
                        " values (1, '', ${date}, 0, true, true, false)")
                for (root in roots) {
                    stmt.executeUpdate("insert into ${Table.files}" +
                            " (${Col.dirId}, ${Col.name}, ${Col.lastModified}, ${Col.length}," +
                            " ${Col.isDir}, ${Col.writable}, ${Col.isDeleted})" +
                            " values (1, '${root}', ${date}, 0, true, true, false)")
                }
            }
        }

        private val statStatment = conn.prepareStatement("select ${Table.files}.* from ${Table.files}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " where ${Table.dirs}.${Dirs.Col.path}=?" +
                " and ${Table.files}.${Col.name}=?" +
                " and ${Table.files}.${Col.isDeleted}=false")

        fun stat(dir: String, name: String): ResultSet {
            return Binding(statStatment)
                    .bind(1, dir)
                    .bind(2, name)
                    .executeQuery()
        }

        private val readDirStatment = conn.prepareStatement("select ${Table.files}.* from ${Table.files}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " where ${Table.dirs}.${Dirs.Col.path}=?" +
                " and ${Table.files}.${Col.isDeleted}=false")

        fun readDir(dir: String): ResultSet {
            return Binding(readDirStatment)
                    .bind(1, dir)
                    .executeQuery()
        }

        private val readDirOfStatment = conn.prepareStatement("select * from ${Table.files}" +
                " where ${Table.files}.${Col.dirId}=?" +
                " and ${Table.files}.${Col.isDeleted}=false")

        fun readDirOf(dirid: Long): ResultSet {
            return Binding(readDirOfStatment)
                    .bind(1, dirid)
                    .executeQuery()
        }

        private val mkfileStatement = conn.prepareStatement("insert into ${Table.files}" +
                " (${Col.dirId}, ${Col.name}, ${Col.lastModified}, ${Col.length}," +
                " ${Col.isDir}, ${Col.writable}, ${Col.isDeleted}, ${Col.lastDeleted}, ${Col.checksum})" +
                " values (?, ?, ?, ?, ?, ?, ?, ?, ?)")

        fun mkfile(
                dirid: Long,
                name: String,
                modified: Long,
                size: Long,
                isdir: Boolean,
                writable: Boolean = true,
                isDeleted: Boolean = false,
                lastDeleted: Long = 0,
                checksum: ByteArray? = null
        ): Long? {
            return Binding(mkfileStatement)
                    .bind(1, dirid)
                    .bind(2, name)
                    .bind(3, modified)
                    .bind(4, size)
                    .bind(5, isdir)
                    .bind(6, writable)
                    .bind(7, isDeleted)
                    .bind(8, lastDeleted)
                    .bind(9, checksum)
                    .insert()
        }

        private val updateFileStatement = conn.prepareStatement("update ${Table.files}" +
                " set ${Col.length}=?" +
                ", ${Col.lastModified}=?" +
                ", ${Col.checksum}=?" +
                " where ${Col.id}=?")

        fun updateFile(fileid: Long, size: Long, modified: Long, checksum: ByteArray?): Int {
            return Binding(updateFileStatement)
                    .bind(1, size)
                    .bind(2, modified)
                    .bind(3, checksum)
                    .bind(4, fileid)
                    .executeUpdate()
        }

        private val setWritableStatement = conn.prepareStatement("update ${Table.files}" +
                " set ${Col.writable}=?" +
                " where ${Col.id}=?")

        fun setWritable(fileid: Long, writable: Boolean): Int {
            return Binding(setWritableStatement)
                    .bind(1, writable)
                    .bind(2, fileid)
                    .executeUpdate()
        }

        private val renameStatement = conn.prepareStatement("update ${Table.files}" +
                " set ${Col.dirId}=?" +
                ", ${Col.name}=?" +
                ", ${Col.lastModified}=?" +
                " where ${Col.id}=?")

        fun rename(fileid: Long, dirid: Long, name: String, modified: Long): Int {
            return Binding(renameStatement)
                    .bind(1, dirid)
                    .bind(2, name)
                    .bind(3, modified)
                    .bind(4, fileid)
                    .executeUpdate()
        }

        private val historyDirsOnlyStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path} from" +
                " (select distinct ${Col.dirId} as t from ${Table.files} where ${Col.isDeleted}=true and ${Col.isDir}=false) as t" +
                " left join ${Table.files} on t.t=${Table.files}.${Col.id}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Col.name} asc, ${Table.files}.${Col.lastDeleted} desc" +
                " limit ?")

        fun historyDirsOnly(count: Int): ResultSet {
            return Binding(historyDirsOnlyStatement)
                    .bind(1, count)
                    .executeQuery()
        }

        private val historyFilesOnlyStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path} from ${Table.files}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " where ${Table.files}.${Col.isDeleted}=true and ${Table.files}.${Col.isDir}=false" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Col.name} asc, ${Table.files}.${Col.lastDeleted} desc" +
                " limit ?")

        fun historyFilesOnly(count: Int): ResultSet {
            return Binding(historyFilesOnlyStatement)
                    .bind(1, count)
                    .executeQuery()
        }

        private val historyOfDirStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path} from ${Table.files}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " where ${Table.files}.${Col.isDeleted}=true and ${Table.files}.${Col.isDir}=false and ${Table.dirs}.${Dirs.Col.path}=?" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Col.name} asc, ${Table.files}.${Col.lastDeleted} desc" +
                " limit ?")

        fun historyOfDir(dir: String, count: Int): ResultSet {
            return Binding(historyOfDirStatement)
                    .bind(1, dir)
                    .bind(2, count)
                    .executeQuery()
        }

        private val historyOfFileStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path} from ${Table.files}" +
                " left join ${Table.dirs} on ${Table.files}.${Col.dirId}=${Table.dirs}.${Dirs.Col.id}" +
                " where ${Table.files}.${Col.isDeleted}=true" +
                " and ${Table.files}.${Col.isDir}=false " +
                " and ${Table.files}.${Col.name}=?" +
                " and ${Table.dirs}.${Dirs.Col.path}=?" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Col.name} asc, ${Table.files}.${Col.lastDeleted} desc" +
                " limit ?")

        /// @param count < 0 for all items.
        fun historyOfFile(dir: String, name: String, count: Int): ResultSet {
            return Binding(historyOfFileStatement)
                    .bind(1, name)
                    .bind(2, dir)
                    .bind(3, count)
                    .executeQuery()
        }

        private val historyOfStatement = conn.prepareStatement("select * from ${Table.files}" +
                " where ${Col.isDeleted}=true" +
                " and ${Col.id}=?")

        fun historyOf(id: Long): ResultSet {
            return Binding(historyOfStatement)
                    .bind(1, id)
                    .executeQuery()
        }

        private val isEmptyDirStatement = conn.prepareStatement("select count(${Col.id}) from ${Table.files}" +
                " where ${Col.isDeleted}=false and ${Col.dirId}=?")

        fun isEmptyDir(dirId: Long): Boolean {
            return Dbfs.getLong(Binding(isEmptyDirStatement)
                    .bind(1, dirId)
                    .executeQuery()) == 0L
        }

        private val deleteStatement = conn.prepareStatement("update ${Table.files} " +
                "set ${Col.isDeleted}=true" +
                ", ${Col.lastDeleted} = ?" +
                " where ${Col.id}=?")

        fun delete(fileid: Long, lastdeleted: Long): Int {
            return Binding(deleteStatement)
                    .bind(1, lastdeleted)
                    .bind(2, fileid)
                    .executeUpdate()
        }

        private val pruneStatement = conn.prepareStatement("delete from ${Table.files} where ${Col.id}=?")

        fun prune(fileid: Long): Int {
            return Binding(pruneStatement)
                    .bind(1, fileid)
                    .executeUpdate()
        }

        fun incrementalVacuum(conn: Connection) {
            conn.createStatement().use {
                //# NOTE When simply executed with long living statement, the incremental vacuum statement
                //# returns before completion and transaction failed to close. Apparently, this works around that OK.
                it.executeUpdate("pragma incremental_vacuum;")
            }
        }

        fun optimize(conn: Connection) {
            conn.createStatement().use {
                it.execute("pragma optimize")
            }
        }
    }

    open class Stat(
            val id: Long,
            val dirId: Long,
            val name: String,
            private val isdir: Boolean,
            override val lastModified: Long,
            override val length: Long,
            override val writable: Boolean,
            val isDeleted: Boolean,
            val lastDeleted: Long,
            override val checksumBytes: ByteArray?
    ) : IFileStat {
        companion object {
            fun optional(result: ResultSet): Stat? {
                if (!result.next()) return null
                return from(result)
            }

            fun from(result: ResultSet): Stat? {
                try {
                    return Stat(
                            result.getLong(Files.Col.id),
                            result.getLong(Files.Col.dirId),
                            result.getString(Files.Col.name),
                            result.getBoolean(Files.Col.isDir),
                            result.getLong(Files.Col.lastModified),
                            result.getLong(Files.Col.length),
                            result.getBoolean(Files.Col.writable),
                            result.getBoolean(Files.Col.isDeleted),
                            result.getLong(Files.Col.lastDeleted),
                            result.getBytes(Files.Col.checksum)
                    )
                } catch (e: Throwable) {
                    return null
                }
            }

            fun from(json: JSONObject): Stat? {
                val notexists = json.optBoolean(IFileInfo.Key.notexists, false)
                if (notexists) return null
                val id = json.optLong(IFileInfo.Key.id, -1)
                val name = json.stringOrNull(IFileInfo.Key.name)
                if (id <= 0 || name == null) return null
                val isdir = json.optBoolean(IFileInfo.Key.isdir, false)
                val notwritable = json.optBoolean(IFileInfo.Key.notwritable, false)
                val length = json.optLong(IFileInfo.Key.length, 0)
                val lastModified = json.optLong(IFileInfo.Key.lastModified, 0)
                val isDeleted = json.optBoolean(IFileInfo.Key.isDeleted, false)
                val lastDeleted = json.optLong(IFileInfo.Key.lastDeleted, 0)
                val checksum = json.stringOrNull(IFileInfo.Key.checksum)
                return Stat(
                        id,
                        0,
                        name,
                        isdir,
                        lastModified,
                        length,
                        !notwritable,
                        isDeleted,
                        lastDeleted,
                        if (checksum == null || checksum.isEmpty()) null else Hex.decode(checksum))
            }
        }

        val checksum get() = checksumBytes?.let { Hex.encode(it).toString() } ?: ""

        override val isDir get() = isdir

        override val isFile get() = !isdir

        override val perm get() = if (writable) "rw" else "r-"

        override val readable = true

        override fun toString(): String {
            return toString(StringPrintWriter()).toString()
        }

        internal fun toString(w: PrintWriter): PrintWriter {
            w.println("# Stat")
            w.println("id            = $id")
            w.println("dirId         = $dirId")
            w.println("name          = $name")
            w.println("last modified = ${String.format("%1\$tD %1\$tT", lastModified)} ($lastModified)")
            w.println("size          = $length")
            w.println("isDir         = $isdir")
            w.println("writable      = $writable")
            w.println("isDeleted     = $isDeleted")
            w.println("last deleted  = ${String.format("%1\$tD %1\$tT", lastDeleted)} ($lastDeleted)")
            w.println("checksum      = $checksum")
            return w
        }
    }

    class DeletedFileStat(
            override val stat: Stat,
            override val dir: String
    ) : IDeletedFileStat {

        override val id = stat.id
        override val name = stat.name
        override val isDeleted = stat.isDeleted
        override val lastDeleted = stat.lastDeleted

        companion object {
            fun from(result: ResultSet): DeletedFileStat? {
                try {
                    val stat = Stat.from(result) ?: return null
                    val dir = result.getString(Dirs.Col.path)
                    return DeletedFileStat(stat, dir)
                } catch (e: Throwable) {
                    return null
                }
            }

            fun from(json: JSONObject): DeletedFileStat? {
                val dir = json.stringOrNull(IFileInfo.Key.dir) ?: return null
                val stat = Stat.from(json) ?: return null
                return DeletedFileStat(stat, dir)
            }
        }

        override fun toJSON(): JSONObject {
            val ret = FileInfoUtil.tojson(JSONObject(), stat)
            ret.put(IFileInfo.Key.id, stat.id)
            ret.put(IFileInfo.Key.name, stat.name)
            ret.put(IFileInfo.Key.dir, dir)
            ret.put(IFileInfo.Key.checksum, stat.checksum)
            ret.put(IFileInfo.Key.isDeleted, isDeleted)
            ret.put(IFileInfo.Key.lastDeleted, lastDeleted)
            return ret
        }

        fun debugString(): String {
            val w = StringPrintWriter()
            w.println("# DeletedFileStat")
            w.println("dir           = $dir")
            stat.toString(w)
            return w.toString()
        }

        override fun toString(): String {
            val lastdeleted = String.format("%1\$tD %1\$tT (%d)", lastDeleted, lastDeleted)
            return "# DeletedFileStat($dir, $name, ${stat.isDir}, ${stat.length}, $isDeleted, $lastdeleted)"
        }
    }

    internal class Contents(conn: Connection) {
        internal object Col {
            const val id = "i"
            const val fileId = "f"
            const val offset = "o"
            const val length = "l"
            const val data = "d"
        }

        companion object {
            fun createTable(stmt: Statement) {
                stmt.executeUpdate("create table ${Table.contents} (" +
                        "${Col.id} integer primary key" +
                        ", ${Col.fileId} integer" +
                        ", ${Col.offset} integer" +
                        ", ${Col.length} integer" +
                        ", ${Col.data} blob" +
                        ")")
                stmt.executeUpdate("create index ${Index.contentIndex} on ${Table.contents}(${Col.fileId})")
            }
        }

        private val readContentStatement = conn.prepareStatement("select * from ${Table.contents}" +
                " where ${Table.contents}.${Col.fileId}=?" +
                " order by ${Table.contents}.${Col.offset} asc")

        fun readContent(fileid: Long): ResultSet {
            return Binding(readContentStatement).bind(1, fileid).executeQuery()
        }

        private val writeContentStatement = conn.prepareStatement("insert into ${Table.contents}" +
                " (${Col.fileId}, ${Col.offset}, ${Col.length}, ${Col.data})" +
                " values (?, ?, ?, ?)")

        fun writeContent(fileid: Long, offset: Long, length: Int, data: ByteArray): Int {
            return Binding(writeContentStatement)
                    .bind(1, fileid)
                    .bind(2, offset)
                    .bind(3, length)
                    .bind(4, data)
                    .executeUpdate()
        }

        private val updateContentStatement = conn.prepareStatement("update ${Table.contents}" +
                " set ${Col.offset}=?" +
                ", ${Col.length}=?" +
                ", ${Col.data}=?" +
                " where ${Table.contents}.${Col.fileId}=?")

        fun updateContent(fileid: Long, offset: Long, length: Int, data: ByteArray): Int {
            return Binding(updateContentStatement)
                    .bind(1, offset)
                    .bind(2, length)
                    .bind(3, data)
                    .bind(4, fileid)
                    .executeUpdate()
        }

        private val moveContentStatement = conn.prepareStatement("update ${Table.contents}" +
                " set ${Col.fileId}=?" +
                " where ${Table.contents}.${Col.fileId}=?")

        fun moveContent(to: Long, from: Long): Int {
            return Binding(moveContentStatement)
                    .bind(1, to)
                    .bind(2, from)
                    .executeUpdate()
        }

        private val deleteContentStatement = conn.prepareStatement("delete from ${Table.contents}" +
                " where ${Table.contents}.${Col.fileId}=?")

        fun deleteContent(fileid: Long): Int {
            return Binding(deleteContentStatement)
                    .bind(1, fileid)
                    .executeUpdate()
        }
    }

    internal class Content(
            val contentid: Long,
            val offset: Long,
            val length: Int,
            val data: ByteArray
    ) {
        val end = offset + length

        companion object {
            fun from(result: ResultSet): Content {
                return Content(
                        result.getLong(Contents.Col.id),
                        result.getLong(Contents.Col.offset),
                        result.getInt(Contents.Col.length),
                        result.getBytes(Contents.Col.data))
            }
        }
    }

    internal class ContentInfo(
            val contentid: Long,
            val offset: Long,
            val length: Int
    ) {
        val end = offset + length

        companion object {
            fun from(result: ResultSet): List<ContentInfo> {
                val ret = ArrayList<ContentInfo>()
                while (result.next()) {
                    ret.add(ContentInfo(
                            result.getLong(Contents.Col.id),
                            result.getLong(Contents.Col.offset),
                            result.getInt(Contents.Col.length)))
                }
                return ret
            }
        }
    }

    internal class Xrefs(conn: Connection) {
        object Col {
            const val id = "i"
            const val from = "f"
            const val to = "o"
        }

        companion object {
            fun createTable(stmt: Statement) {
                stmt.executeUpdate("create table ${Table.xrefs} (" +
                        "${Col.id} integer primary key" +
                        ", ${Col.from} integer" +
                        ", ${Col.to} integer" +
                        ")")
                stmt.executeUpdate("create index ${Index.fromIndex} on ${Table.xrefs}(${Col.from})")
                stmt.executeUpdate("create index ${Index.toIndex} on ${Table.xrefs}(${Col.to})")
            }
        }

        private val refsFromStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path}" +
                " from ${Table.xrefs}" +
                " left join ${Table.files} on ${Table.xrefs}.${Col.to}=${Table.files}.${Files.Col.id}" +
                " left join ${Table.dirs} on ${Table.dirs}.${Dirs.Col.id}=${Table.files}.${Files.Col.dirId}" +
                " where ${Table.xrefs}.${Col.from}=?" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Files.Col.name} asc")

        fun refsFrom(from: Long): ResultSet {
            return Binding(refsToStatement).bind(1, from).executeQuery()
        }

        private val refsToStatement = conn.prepareStatement("select ${Table.files}.*, ${Table.dirs}.${Dirs.Col.path}" +
                " from ${Table.xrefs}" +
                " left join ${Table.files} on ${Table.xrefs}.${Col.from}=${Table.files}.${Files.Col.id}" +
                " left join ${Table.dirs} on ${Table.dirs}.${Dirs.Col.id}=${Table.files}.${Files.Col.dirId}" +
                " where ${Table.xrefs}.${Col.to}=?" +
                " order by ${Table.dirs}.${Dirs.Col.path} asc, ${Table.files}.${Files.Col.name} asc")

        fun refsTo(to: Long): ResultSet {
            return Binding(refsToStatement).bind(1, to).executeQuery()
        }

        private val deleteStatement = conn.prepareStatement("delete from ${Table.xrefs} where ${Col.from}=?")

        fun delete(from: Long): Int {
            return Binding(deleteStatement).bind(1, from).executeUpdate()
        }

        private val insertStatement = conn.prepareStatement("insert into ${Table.xrefs}" +
                " (${Col.from}, ${Col.to}) values (?, ?)")

        fun insert(from: Long, to: Long): Long? {
            return Binding(insertStatement).bind(1, from).bind(2, to).insert()
        }
    }

    internal class Xinfos(conn: Connection) {
        object Col {
            const val id = "i"
            const val path = "p"
        }

        companion object {
            fun createTable(stmt: Statement) {
                stmt.executeUpdate("create table ${Table.xinfos} (" +
                        "${Col.id} integer primary key" +
                        ", ${Col.path} text unique" +
                        ")")
            }
        }

        private val idOfStatement = conn.prepareStatement("select ${Col.id} from ${Table.xinfos}" +
                " where ${Col.path}=?")

        fun idOf(path: String): Long? {
            val id = Dbfs.getLong(Binding(idOfStatement).bind(1, path).executeQuery())
            return if (id > 0) id else null
        }

        private val addStatement = conn.prepareStatement("insert or ignore into ${Table.xinfos}" +
                " (${Col.path}) values (?)")

        fun add(path: String): Long? {
            return Binding(addStatement).bind(1, path).insert()
        }

        private val purgeStatement = conn.prepareStatement("delete from ${Table.xinfos}" +
                " where ${Table.xinfos}.${Col.id}=?")

        fun purge(id: Long): Int {
            return Binding(purgeStatement).bind(1, id).executeUpdate()
        }
    }
}

class DbfsRootInfo(
        internal var fs: IDbfs,
        override val name: String
) : IRootInfo {

    override val root = this
    override val cpath = name
    override val apath = File.separatorChar + name
    override val rpath = ""
    private val basepath = Basepath.from(cpath)
    private val stat: Stat = fs.stat(basepath)!!
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

    fun onPause() {
        fs.onPause()
    }

    fun onResume() {
        fs.onResume()
    }

    override fun fileInfo(rpath: String): IFilepath {
        var rpath1 = rpath
        while (rpath1.startsWith(File.separatorChar)) rpath1 = rpath1.substring(1)
        return Filepath(this, Basepath.joinRpath(this.rpath, rpath1))
    }

    override fun <T : MutableCollection<IFileInfo>> readDir(ret: T): T {
        if (fs.isClosed()) return ret
        for (stat in Dbfs.listOfStats(fs.readDir(basepath))) {
            if (stat.id == 1L) continue
            val info = Filepath(this, Basepath.joinRpath(this.rpath, stat.name))
            ret.add(info)
        }
        return ret
    }

    override fun mkparent(): Boolean {
        return false
    }

    override fun mkdirs(): Boolean {
        return true
    }

    override fun delete(): Boolean {
        return false
    }

    override fun find(ret: MutableCollection<String>, rpathx: String, pattern: String) {
        fs.lock.read {
            val lcpat = TextUt.toLowerCase(pattern)
            Filepath(this, rpathx).walk2 { file, _ ->
                if (TextUt.toLowerCase(file.basepath.nameWithSuffix).contains(lcpat)) {
                    ret.add(file.apath)
                }
            }
        }
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): Pair<Int, Int> {
        return fs.cleanupTrash(predicate)
    }

    override fun history(rpath: String, listdir: Boolean): List<IDeletedFileStat> {
        val (dir, name) = Basepath.splitPath1(Basepath.clean(Basepath.joinRpath1(cpath, rpath)))
        return fs.history(cpath, dir ?: "", name, listdir, -1)
    }

    override fun searchHistory(rpath: String, predicate: Fun11<IDeletedFileStat, Boolean>): List<IDeletedFileStat> {
        val (dir, name) = Basepath.splitPath1(Basepath.clean(Basepath.joinRpath1(cpath, rpath)))
        return fs.searchHistory(cpath, dir ?: "", name, -1, predicate)
    }

    override fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int> {
        return fs.pruneHistory(infos, all)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        val src = infos.mapNotNull { DeletedFileStat.from(it) }
        if (src.size != infos.size) throw IOException()
        if (dst.root === this) {
            return fs.recover(Basepath.from(dst.cpath), src)
        } else {
            return fs.recover(dst, src)
        }
    }

    override fun updateXrefs(from: IFileInfo, infos: JSONObject?) {
        fs.updateXrefs(from, infos)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return fs.transaction(code)
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is DbfsRootInfo && other.fs === this.fs
    }

    override fun hashCode(): Int {
        return cpath.hashCode()
    }

    override fun toJSON(): JSONObject {
        val ret = JSONObject()
        ret.put(IFileInfo.Key.name, name)
        ret.put(IFileInfo.Key.isdir, true)
        ret.put(IFileInfo.Key.perm, stat.perm)
        return ret
    }

    fun close() {
        fs.lock.write {
            if (!fs.isClosed()) fs.closeDatabase()
        }
    }

    fun setDbfs(fs: IDbfs) {
        close()
        this.fs = fs
    }
}
