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
package com.cplusedition.bot.core

import com.cplusedition.bot.core.Basepath.Companion.cleanPath
import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.*
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min

////////////////////////////////////////////////////////////////////////

val File.suffix: String
    get() {
        return Basepath.suffix(this.name)
    }

val File.lcSuffix: String
    get() {
        return Basepath.lcSuffix(this.name)
    }

fun File.changeSuffix(newsuffix: String): File {
    return File(parentFile, "${Basepath.stem(name)}$newsuffix")
}

fun File.changeStem(newbase: String): File {
    return File(parentFile, "$newbase${Basepath.suffix(name)}")
}

fun File.changeName(newname: String): File {
    return File(parentFile, newname)
}

/**
 * @return Name of file entries under this directory, or an empty array.
 */
fun File.listOrEmpty(): Array<String> {
    return Without.throwableOrNull { list() } ?: EMPTY.stringArray
}

/**
 * @return File/directory entries under this directory, or an empty array.
 */
fun File.filesOrEmpty(): List<File> {
    return listOrEmpty().map { this.file(it) }
}

/**
 * @return File with cleaned up path.
 */
fun File.clean(): File {
    return File(Basepath.cleanPath(absolutePath))
}

/**
 * @return File with cleaned up path.
 */
fun File.clean(vararg segments: String): File {
    return File(Basepath.cleanPath(file(*segments).absolutePath))
}

fun File.file(vararg segments: String): File {
    return if (segments.isEmpty()) this else File(this, segments.joinToString(FS))
}

fun File.fileFmt(format: String, vararg args: Any?): File {
    return File(TextUt.format(format, args))
}

fun File.mkparentOrNull(): File? {
    val parent = absoluteFile.parentFile ?: return null
    return if (parent.mkdirsOrNull() != null) this else null
}

fun File.mkparentOrFail(): File {
    return mkparentOrNull() ?: error(absolutePath)
}

/**
 * @return The specified directory if it exists or created, otherwise null.
 */
fun File.mkdirsOrNull(): File? {
    return if (exists() && isDirectory || !exists() && mkdirs()) this else null
}

/**
 * @return The specified directory if exists or created, othewise throw IllegalStateException
 */
fun File.mkdirsOrFail(): File {
    return mkdirsOrNull() ?: error(absolutePath)
}

/**
 * @return File if exists, otherwise null
 */
fun File.existsOrNull(): File? {
    return if (exists()) this else null
}

/**
 * @return File if exists, otherwise throw IllegalStateException
 */
fun File.existsOrFail(): File {
    return if (exists()) this else error(absolutePath)
}

fun File.canWriteOrFail(): File {
    return if (exists() && canWrite()) this else error(absolutePath)
}

fun File.canReadOrFail(): File {
    return if (exists() && canRead()) this else error(absolutePath)
}

/**
 * Delete all files and directories under this directory recursively, but not this directory.
 * @return this file if delete successfully, otherwise null
 */
fun File.deleteSubtreesOrNull(): File? {
    for (name in listOrEmpty()) {
        if (!File(this, name).deleteRecursively()) return null
    }
    return this
}

fun File.deleteTreeOrNull(): File? {
    if (!deleteRecursively()) return null
    return this
}

/**
 * Delete all files and directories under this directory recursively, but not this directory.
 * @return this file.
 * @throws If delete failed.
 */
fun File.deleteSubtreesOrFail(): File {
    for (name in listOrEmpty()) {
        if (!File(this, name).deleteRecursively()) error(absolutePath)
    }
    return this
}

fun File.deleteTreeOrFail(): File {
    if (!deleteRecursively()) error(absolutePath)
    return this
}

fun File.deleteOrFail(): File {
    if (exists() && !delete()) error(absolutePath)
    return this
}

val File.bot: FileUtExt
    get() = FileUtExt(this)

////////////////////////////////////////////////////////////////////////

val FS = File.separator
val FSC = File.separatorChar
val PS = File.pathSeparator
val PSC = File.pathSeparatorChar
val FS_PAT = Regex("[/\\\\]")

interface IBasepath {
    val dir: String?
    val name: String
    val stem: String
    val suffix: String
    val lcSuffix: String
}

open class Basepath(
    override val dir: String?,
    override val name: String,
    override val stem: String,
    override val suffix: String
) : IBasepath {
    companion object {
        fun from(dir: String?, name: String): Basepath {
            val namesuffix = splitName((name))
            return Basepath(dir, name, namesuffix.first, namesuffix.second)
        }

        fun from(file: File): Basepath {
            return from(file.absolutePath)
        }

        /// @param path A clean path.
        fun from(path: CharSequence): Basepath {
            var end = path.length
            while (end > 0 && path[end - 1] == FSC) end -= 1
            val (dir, name) = splitCleanpath(path, end)
            val stemsuffix = splitName(name)
            return Basepath(dir, name, stemsuffix.first, stemsuffix.second)
        }

        fun fromClean(vararg segments: String): Basepath {
            return from(cleanSequence(segments.bot.joinPath()))
        }

        fun cleanPath(path: CharSequence): String {
            return cleanBuilder(StringBuilder(path)).toString()
        }

        fun cleanSequence(path: CharSequence): StringBuilder {
            return cleanBuilder(StringBuilder(path))
        }

        /**
         * Remove duplicated /, /./ and /../
         */
        fun cleanBuilder(b: StringBuilder): StringBuilder {
            var c: Char
            var last = -1
            var len = 0
            val max = b.length
            var i = 0
            val sep = FSC
            while (i < max) {
                c = b[i]
                if ((last == sep.code || len == 0) && c == '.' && (i + 1 < max && b[i + 1] == sep || i + 1 >= max)) {
                    ++i
                    ++i
                    continue
                }
                if (last == sep.code && c == sep) {
                    ++i
                    continue
                }
                if (last == sep.code && c == '.' && i + 1 < max && b[i + 1] == '.' && len >= 2 && (i + 2 >= max || b[i + 2] == sep)) {
                    val index = b.lastIndexOf(sep, len - 2)
                    if ("..$sep" != b.substring(index + 1, len)) {
                        len = index + 1
                        ++i
                        ++i
                        continue
                    }
                }
                b.setCharAt(len++, c)
                last = c.code
                ++i
            }
            b.setLength(len)
            return b
        }

        fun dir(path: CharSequence): String? {
            var end = path.length
            while (end > 0 && path[end - 1] == FSC) end -= 1
            val index = path.lastIndexOf(FS, startIndex = end - 1)
            return if (index < 0) null
            else path.substring(0, index)
        }

        fun name(path: CharSequence): String {
            var end = path.length
            while (end > 0 && path[end - 1] == FSC) end -= 1
            val index = path.lastIndexOf(FSC, startIndex = end - 1)
            return if (index < 0) {
                if (end == path.length) path.toString()
                else path.substring(0, end)
            } else path.substring(index + 1, end)
        }

        fun stem(path: CharSequence): String {
            val name = name(path)
            val index = name.lastIndexOf('.')
            return if (index <= 0 || name == "..") name
            else name.substring(0, index)
        }

        fun suffix(path: CharSequence): String {
            val name = name(path)
            val index = name.lastIndexOf('.')
            return if (index <= 0 || name == "..") ""
            else name.substring(index)
        }

        fun lcSuffix(path: CharSequence): String {
            return TextUt.toLowerCase(suffix(path))
        }

        fun ext(path: CharSequence?): String? {
            if (path == null) return null
            val name = name(path)
            val index = name.lastIndexOf('.')
            return if (index <= 0 || name == "..") null
            else name.substring(index + 1)
        }

        fun lcExt(path: CharSequence?): String? {
            return TextUt.toLowerCaseOrNull(ext(path))
        }

        fun changeName(path: CharSequence, newname: CharSequence): String {
            return from(path).changeName(newname).toString()
        }

        fun changeStem(path: CharSequence, newbase: CharSequence): String {
            return from(path).changeStem(newbase).toString()
        }

        fun changeSuffix(path: CharSequence, newsuffix: CharSequence): String {
            return from(path).dirAndStem + newsuffix
        }

        /// If dir is null return name. If dir empty return /name. If dir == "." return ./name
        fun joinPath(dir: CharSequence?, name: CharSequence): String {
            return if (dir == null) name.toString()
            else if (name.isEmpty()) dir.toString()
            else joinpath0(dir, name)
        }

        /// Like joinPath() but return a relative path if directory is null, empty or ".".
        fun joinRpath(dir: CharSequence?, name: CharSequence): String {
            return if (dir.isNullOrEmpty() || dir == ".") name.toString()
            else if (name.isEmpty()) dir.toString()
            else joinpath0(dir, name)
        }

        /// Like joinPath() but return trailing file separator, eg. dir/, if name is empty.
        fun joinPathSlash(dir: CharSequence?, name: CharSequence): String {
            return joinpath0(dir ?: "", name)
        }

        /// Like joinRpath() but with trailing file separator, eg. dir/, if name is empty.
        fun joinRpathSlash(dir: CharSequence?, name: CharSequence): String {
            return if (dir.isNullOrEmpty() || dir == ".") name.toString()
            else joinpath0(dir, name)
        }

        private fun joinpath0(dir: CharSequence, name: CharSequence): String {
            return "${trimTrailingSlash(dir)}$FS${trimLeadingSlash(name)}"
        }

        /**
         * Split path to (dir, name). If input has trailing /, name is "".
         */
        fun splitPath(path: CharSequence): Pair<String?, String> {
            return splitCleanpath(cleanPath(path))
        }

        private fun splitCleanpath(path: CharSequence, end: Int = path.length): Pair<String?, String> {
            val index = path.lastIndexOf(FS, startIndex = end - 1)
            return if (index < 0) Pair(null, path.substring(0, end))
            else Pair(path.substring(0, index), path.substring(index + 1, end))
        }

        /** Split name to (stem, suffix) */
        fun splitName(name: CharSequence): Pair<String, String> {
            val index = name.lastIndexOf('.')
            return if (index <= 0 || name == "..") Pair(name.toString(), "")
            else Pair(name.substring(0, index), name.substring(index))
        }

        fun trimLeadingSlash(path: CharSequence): CharSequence {
            val len = path.length
            var index = 0
            while (index < len && path[index] == FSC) ++index
            return if (index == 0) path else path.subSequence(index, path.length)
        }

        fun trimTrailingSlash(path: CharSequence): CharSequence {
            val len = path.length
            var index = len
            while (index > 0 && path[index - 1] == FSC) --index
            return if (index == len) path else path.subSequence(0, index)
        }

        fun ensureLeadingSlash(path: String): String {
            return if (path.startsWith(FS)) path else FS + path
        }

        fun ensureTrailingSlash(path: String): String {
            return if (path.endsWith(FS)) path else path + FS
        }

        /// @return Clean rpath or null if rpath starts with "../".
        fun cleanRpath(rpath: CharSequence): String? {
            val ret = trimLeadingSlash(cleanPath(rpath))
            if (ret.startsWith("..$FSC")) return null
            return ret.toString()
        }
    }

    override val lcSuffix: String get() = TextUt.toLowerCase(suffix)
    val ext: String? get() = if (suffix.isEmpty()) null else suffix.substring(1)
    val lcExt: String? get() = TextUt.toLowerCaseOrNull(ext)
    val dirAndStem: String get() = joinPath(dir, stem)

    fun toFile(): File {
        return File(this.toString())
    }

    fun file(rpath: CharSequence): Basepath {
        return from(joinPath(this.toString(), rpath))
    }

    fun changeName(newname: CharSequence): Basepath {
        return from(joinPath(dir, newname))
    }

    fun changeStem(newbase: CharSequence): Basepath {
        return from(joinPath(dir, "$newbase$suffix"))
    }

    fun changeSuffix(newsuffix: CharSequence): Basepath {
        return from(joinPath(dir, "$stem$newsuffix"))
    }

    override fun toString(): String {
        return dirAndStem + suffix
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Basepath) return false
        return dir == other.dir && name == other.name
    }

    override fun hashCode(): Int {
        var result = dir?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + stem.hashCode()
        result = 31 * result + suffix.hashCode()
        return result
    }
}

////////////////////////////////////////////////////////////////////////

private object EMPTY {
    val stringArray = emptyArray<String>()
    val fileArray = emptyArray<File>()
}

object FileUt : FileUtil()

open class FileUtil {

    private val BUFSIZE = 16 * 1024
    val ROOT = File("", "")
    val HOME = File(System.getProperty("user.home")!!)
    val PWD = File(System.getProperty("user.dir")!!)
    val everythingFilter: FileFilter = FileFilter { true }
    val fileFilter: FileFilter = FileFilter { it.isFile }
    val notFileFilter: FileFilter = FileFilter { !it.isFile }
    val dirFilter: FileFilter = FileFilter { it.isDirectory }
    val notDirFilter: FileFilter = FileFilter { !it.isDirectory }
    val everythingPredicate: IFilePathPredicate = { _, _ -> true }
    val filePredicate: IFilePathPredicate = { file, _ -> file.isFile }
    val notFilePredicate: IFilePathPredicate = { file, _ -> !file.isFile }
    val dirPredicate: IFilePathPredicate = { file, _ -> file.isDirectory }
    val notDirPredicate: IFilePathPredicate = { file, _ -> !file.isDirectory }

    var lastModifiedComparator = Comparator<File> { o1, o2 ->
        if (o1 == null) {
            if (o2 == null) 0 else -1
        } else if (o2 == null) {
            1
        } else {
            o1.lastModified().compareTo(o2.lastModified())
        }
    }

    val filesystem = FileSystems.getDefault()

    /** Permission for world read only directory: rwXr-Xr-X */
    val permissionsWorldReadonlyDir = setOf(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_EXECUTE,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.OTHERS_READ,
        PosixFilePermission.OTHERS_EXECUTE
    )

    /** Permission for world read only file: rw-r--r-- */
    val permissionsWorldReadonlyFile = setOf(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.OTHERS_READ
    )

    /** Permission for world read only directory: rwX----- */
    val permissionsOwnerOnlyDir = setOf(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_EXECUTE
    )

    /** Permission for world read only file: rw------- */
    val permissionsOwnerOnlyFile = setOf(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE
    )

    fun getPermission(file: File): Set<PosixFilePermission> {
        return Files.getPosixFilePermissions(filesystem.getPath(file.absolutePath))
    }

    @Throws(IOException::class)
    fun setPermission(permissions: Set<PosixFilePermission>, file: File) {
        Files.setPosixFilePermissions(filesystem.getPath(file.absolutePath), permissions)
    }

    fun setPermission(permissions: Set<PosixFilePermission>, files: Iterator<File>) {
        files.forEach { setPermission(permissions, it) }
    }

    /**
     * Set file permission to rwx------ for directory, rw------- for file.
     *
     * @throws IOException If any operation failed.
     */
    @Throws(IOException::class)
    fun setOwnerOnly(vararg files: File) {
        for (file in files) {
            setOwnerOnly1(file)
        }
    }

    fun setOwnerOnly1(file: File): File {
        if (file.isDirectory) {
            setPermission(permissionsOwnerOnlyDir, file)
        } else {
            setPermission(permissionsOwnerOnlyFile, file)
        }
        return file
    }

    /**
     * Set file attributes to rwXr-Xr-X.
     * @return The number of file entries updated.
     * @throws AssertionError If any operation failed.
     */
    fun setWorldReadonly(vararg files: File): Int {
        var count = 0
        for (file in files) {
            try {
                if (file.isDirectory) {
                    Files.setPosixFilePermissions(filesystem.getPath(file.absolutePath), permissionsWorldReadonlyDir)
                } else if (file.isFile) {
                    Files.setPosixFilePermissions(filesystem.getPath(file.absolutePath), permissionsWorldReadonlyFile)
                }
            } catch (e: Throwable) {
                continue
            }
            ++count
        }
        return count
    }

    /// @return true if src is newer than dst.
    fun isNewer(src: File, dst: File): Boolean {
        return src.lastModified() > dst.lastModified()
    }

    fun isRecursiveSymlink(file: Path, parent: Path): Boolean {
        return (Without.throwableOrNull {
            Files.isSymbolicLink(file) && Files.isSameFile(parent, file)
        } == true)
    }

    fun home(vararg segments: String): File {
        return if (segments.isEmpty()) HOME else HOME.clean(*segments)
    }

    fun pwd(vararg segments: String): File {
        return if (segments.isEmpty()) PWD else PWD.clean(*segments)
    }

    fun root(vararg segments: String): File {
        return if (segments.isEmpty()) ROOT else ROOT.clean(*segments)
    }

    fun closeOrFail(file: Closeable?) {
        try {
            file?.close()
        } catch (e: Throwable) {
            throw AssertionError(e)
        }
    }

    fun closeAndIgnoreError(file: Closeable?) {
        try {
            file?.close()
        } catch (e: Throwable) {
        }
    }

    fun file(vararg segments: String): File {
        return File(segments.joinToString(FS))
    }

    fun fileFmt(format: String, vararg args: Any?): File {
        return File(TextUt.format(format, args))
    }

    fun mkdirsOrNull(vararg segments: String): File? {
        return mkdirsOrNull(file(*segments))
    }

    fun mkdirsOrNull(file: File): File? {
        return if (file.exists() || file.mkdirs()) file else null
    }

    fun mkdirsOrFail(file: File): File {
        return if (file.exists() || file.mkdirs()) file else throw AssertionError()
    }

    /**
     * @return The specified file if its parent exists or created, otherwise null.
     */
    fun mkparent(vararg segments: String): File? {
        return file(*segments).mkparentOrNull()
    }

    fun cleanFile(vararg segments: String): File {
        return File(cleanPath(file(*segments).absolutePath))
    }

    /**
     * Specialize cleanPath() for rpathOrNull.
     * Result may not be same as cleanPath(), but good for rpathOrNull.
     */
    protected /* for testing */ fun cleanPathSegments(b: CharSequence): List<String> {
        var c: Char
        val blen = b.length
        var i = 0
        val sep = FSC
        val ret = ArrayList<String>()
        val buf = StringBuilder()
        while (i < blen) {
            c = b[i]
            if (buf.isEmpty()) {
                if (c == sep) {
                    ++i
                    continue
                }
                if (c == '.') {
                    if (i + 1 < blen && b[i + 1] == sep || i + 1 >= blen) {
                        ++i
                        ++i
                        continue
                    }
                    if (i + 1 < blen && b[i + 1] == '.' && (i + 2 >= blen || b[i + 2] == sep)) {
                        val retsize = ret.size
                        if (retsize > 0 && ".." != ret.last()) {
                            ret.removeLast()
                            ++i
                            ++i
                            continue
                        }
                    }
                }
            }
            if (c == sep) {
                ret.add(buf.toString())
                buf.setLength(0)
            } else {
                buf.append(c)
            }
            ++i
        }
        if (buf.isNotEmpty()) {
            ret.add(buf.toString())
        }
        return ret
    }

    /**
     * @return Relative path without leading / or null if file is not under basedir.
     */
    fun rpathOrNull(file: File, basedir: File): String? {
        return rpathOrNull(file.absolutePath, basedir.absolutePath)
    }

    fun rpathOrNull(filepath: String, basepath: String): String? {
        val f = cleanPathSegments(filepath)
        val b = cleanPathSegments(basepath)
        if (f.contains("..") || b.contains("..")) return null
        val blen = b.size
        val flen = f.size
        var i = 0
        while (i < blen && i < flen) {
            if (b[i] != f[i]) {
                break
            }
            ++i
        }
        if (i < blen) return null
        return f.subList(i, flen).joinToString(FS)
    }

    /**
     * @return Relative path without leading /, allowing .. if file is not under basedir.
     */
    fun rpath(file: File, basedir: File): String? {
        return rpath(file.absolutePath, basedir.absolutePath)
    }

    fun rpath(file: String, basedir: String): String? {
        val f = cleanPathSegments(file)
        val b = cleanPathSegments(basedir)
        if (f.contains("..") || b.contains("..")) return null
        val blen = b.size
        val flen = f.size
        var i = 0
        while (i < blen && i < flen) {
            if (b[i] != f[i]) {
                break
            }
            ++i
        }
        val ret = ArrayList(f.subList(i, flen))
        if (i == blen) {
            return ret.joinToString(FS)
        }
        while (++i <= blen) {
            ret.add(0, "..")
        }
        return ret.joinToString(FS)
    }

    fun copy(dst: File, src: InputStream) {
        dst.mkparentOrNull() ?: throw IOException()
        With.outputStream(dst) {
            copy(it, src)
        }
    }

    fun copy(dst: OutputStream, src: File) {
        With.inputStream(src) {
            copy(dst, it)
        }
    }

    @Throws(IOException::class)
    fun copy(output: OutputStream, input: InputStream, length: Long): Long {
        val b = ByteArray(BUFSIZE)
        var remaining = length
        var total = 0L
        while (remaining > 0) {
            val len = if (remaining > BUFSIZE) BUFSIZE else remaining.toInt()
            val n = input.read(b, 0, len)
            if (n < 0) break
            if (n > 0) {
                output.write(b, 0, n)
                remaining -= n
                total += n
            }
        }
        return total
    }

    @Throws(IOException::class)
    fun copy(output: OutputStream, input: InputStream): Long {
        return copy(BUFSIZE, output, input)
    }

    @Throws(IOException::class)
    fun copy(bufsize: Int, output: OutputStream, input: InputStream): Long {
        return copy(ByteArray(bufsize), output, input)
    }

    @Throws(IOException::class)
    fun copy(buf: ByteArray, output: OutputStream, input: InputStream): Long {
        var written = 0L
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            if (n > 0) {
                written += n
                output.write(buf, 0, n)
            }
        }
        return written
    }

    @Throws(IOException::class)
    fun copyByteWise(output: OutputStream, input: InputStream) {
        while (true) {
            val b = input.read()
            if (b < 0) break
            output.write(b)
        }
    }

    @Throws(IOException::class)
    fun copyas(dst: File, src: File, preservetimestamp: Boolean = false) {
        With.inputStream(src) { input ->
            dst.mkparentOrNull() ?: throw IOException()
            With.outputStream(dst) { output ->
                copy(output, input)
            }
        }
        if (preservetimestamp) dst.setLastModified(src.lastModified())
        FileUt.setPermission(FileUt.getPermission(src), dst)
    }

    @Throws(IOException::class)
    fun copydiff(dst: File, src: File, preservetimestamp: Boolean = false): Boolean {
        if (!src.isFile) throw IOException()
        if (dst.isDirectory) throw IOException()
        if (dst.isFile && !diff(dst, src)) return false
        With.inputStream(src) { input ->
            dst.mkparentOrNull() ?: throw IOException()
            With.outputStream(dst) { output ->
                copy(output, input)
            }
        }
        if (preservetimestamp) dst.setLastModified(src.lastModified())
        FileUt.setPermission(FileUt.getPermission(src), dst)
        return true
    }

    fun copyto(dstdir: File, srcfile: File, preservetimestamp: Boolean = false) {
        copyas(File(dstdir, srcfile.name), srcfile, preservetimestamp)
    }

    fun copyto(dstdir: File, srcfiles: Collection<File>, preservetimestamp: Boolean = false): Int {
        return copyto(dstdir, srcfiles.iterator(), preservetimestamp)
    }

    fun copyto(dstdir: File, srcfiles: Sequence<File>, preservetimestamp: Boolean = false): Int {
        return copyto(dstdir, srcfiles.iterator(), preservetimestamp)
    }

    fun copyto(dstdir: File, srcfiles: Iterator<File>, preservetimestamp: Boolean = false): Int {
        if (!dstdir.isDirectory) error("# Expecting a directory: $dstdir")
        var count = 0
        for (srcfile in srcfiles) {
            copyas(File(dstdir, srcfile.name), srcfile, preservetimestamp)
            ++count
        }
        return count
    }

    fun copyto(dstdir: File, vararg srcfiles: File): Int {
        return copyto(dstdir, srcfiles.iterator(), false)
    }

    fun copydir(
        dstdir: File,
        srcdir: File,
        preservetimestamp: Boolean = false,
        predicate: Fun21<File, File, Boolean>? = null
    ) {
        srcdir.bot.walk { file, rpath ->
            val dst = dstdir.file(rpath)
            if (predicate?.invoke(dst, file) != false) {
                if (file.isDirectory) {
                    dst.mkdirsOrNull() ?: throw IOException()
                } else copyas(dst, file, preservetimestamp)
            }
        }
    }

    @Throws(IOException::class)
    fun asString(input: InputStream, charset: Charset = Charsets.UTF_8): String {
        return InputStreamReader(input, charset).readText()
    }

    @Throws(IOException::class)
    fun asStringList(input: InputStream, charset: Charset = Charsets.UTF_8): List<String> {
        return InputStreamReader(input, charset).readLines()
    }

    fun delete(file: File): Boolean {
        try {
            return file.exists() && file.delete()
        } catch (_: Throwable) {
            return false
        }
    }

    fun delete(files: Iterable<File>): Int {
        var count = 0
        files.forEach {
            if (FileUt.delete(it)) ++count
        }
        return count
    }

    fun delete(files: Sequence<File>): Int {
        var count = 0
        files.forEach {
            if (FileUt.delete(it)) ++count
        }
        return count
    }

    fun deleteRecursively(file: File): Boolean {
        try {
            return file.exists() && file.deleteRecursively()
        } catch (_: Throwable) {
            return false
        }
    }

    @Throws(IOException::class)
    fun openGzOrInputStream(file: File): InputStream {
        val input = file.inputStream()
        return if (file.name.endsWith(".gz")) GZIPInputStream(input) else input
    }

    @Throws(IOException::class)
    fun openGzOrOutputStream(file: File): OutputStream {
        val output = file.outputStream()
        return if (file.name.endsWith(".gz")) GZIPOutputStream(output) else output
    }

    /**
     * Zip only file in the given basedir with rpath matching the given
     * regular expressions. No directory entries are created.
     */
    @Throws(IOException::class)
    fun zip(
        zipfile: File,
        basedir: File,
        include: String,
        exclude: String? = null,
        preservetimestamp: Boolean = true
    ): Int {
        return zip(
            zipfile,
            basedir,
            Regex(include),
            if (exclude != null) Regex(exclude) else null,
            preservetimestamp
        )
    }

    /**
     * Zip only file in the given basedir with rpath matching the given
     * regular expressions. No directory entries are created.
     */
    @Throws(IOException::class)
    fun zip(
        zipfile: File,
        basedir: File,
        include: Regex,
        exclude: Regex? = null,
        preservetimestamp: Boolean = true
    ): Int {
        var count = 0
        zip(zipfile, basedir, preservetimestamp) { file, rpath ->
            if (file.isFile) {
                val accept = MatchUt.matches(rpath, include, exclude)
                if (accept) ++count
                accept
            } else true
        }
        return count
    }

    /**
     * Zip everything in the given basedir, including directory entries.
     */
    @Throws(IOException::class)
    fun zip(zipfile: File, basedir: File, preservetimestamp: Boolean = true): Int {
        return zip(zipfile, basedir, preservetimestamp) { _, _ -> true }
    }

    /**
     * Zip file/directory in the given basedir with FileUt.scan(basedir, accept).  If file is a directory
     * and accept returns false, then the directory and all its decendents are ignored.
     *
     * @param accept(file, rpath) Return true to include file/directory in the zip output.
     */
    @Throws(IOException::class)
    fun zip(zipfile: File, basedir: File, preservetimestamp: Boolean = true, accept: IFilePathPredicate): Int {
        var count = 0
        With.zipOutputStream(zipfile) { out ->
            U.scan1(basedir, "") { file, rpath ->
                val yes = accept(file, rpath)
                if (yes) {
                    zipentry(out, preservetimestamp, file, rpath)
                    ++count
                }
                yes
            }
        }
        return count
    }

    /**
     * Zip file/directory with the given rpaths in the given basedir if it exists.
     * Note that if File(basedir, rpath) is a directory, the directory is included
     * as a directory entry in the zip file, but not its descendents.
     */
    @Throws(IOException::class)
    fun zip(zipfile: File, basedir: File, rpaths: Sequence<String>, preservetimestamp: Boolean = true): Int {
        var count = 0
        With.zipOutputStream(zipfile) { out ->
            for (rpath in rpaths) {
                val file = File(basedir, rpath)
                if (file.exists()) {
                    zipentry(out, preservetimestamp, file, rpath)
                    ++count
                }
            }
        }
        return count
    }

    fun zipentry(out: ZipOutputStream, preservetimestamp: Boolean, file: File, rpath: String) {
        val isdir = file.isDirectory
        val entry = zipentry(rpath, isdir)
        if (preservetimestamp) {
            val time = FileTime.fromMillis(file.lastModified())
            entry.creationTime = time
            entry.lastModifiedTime = time
        }
        out.putNextEntry(entry)
        if (!isdir) {
            copy(out, file)
        }
    }

    fun zipentry(rpath: String, isdir: Boolean = false): ZipEntry {
        return (if (FSC != '/') rpath.replace(FSC, '/') else rpath).let {
            ZipEntry(if (isdir && !it.endsWith('/')) "$it/" else it)
        }
    }

    @Throws(IOException::class)
    fun unzip(
        outdir: File,
        zipfile: File,
        include: String,
        exclude: String? = null,
        preservetimestamp: Boolean = true
    ): Int {
        return unzip(
            outdir,
            zipfile,
            Regex(include),
            if (exclude != null) Regex(exclude) else null,
            preservetimestamp
        )
    }

    @Throws(IOException::class)
    fun unzip(
        outdir: File,
        zipfile: File,
        include: Regex,
        exclude: Regex? = null,
        preservetimestamp: Boolean = true
    ): Int {
        var count = 0
        unzip(outdir, zipfile, preservetimestamp) {
            val yes = MatchUt.matches(it.name, include, exclude)
            if (yes) ++count
            yes
        }
        return count
    }

    @Throws(IOException::class)
    fun unzip(
        outdir: File,
        zipfile: File,
        preservetimestamp: Boolean = true,
        accept: ((ZipEntry) -> Boolean)? = null
    ): Int {
        var count = 0
        With.zipInputStream(zipfile) { input, entry ->
            val yes = (accept == null || accept(entry))
            if (yes) {
                val file = File(outdir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirsOrNull() ?: throw IOException(entry.name)
                } else {
                    try {
                        copy(file, input)
                    } finally {
                        input.closeEntry()
                    }
                }
                if (preservetimestamp) {
                    val timestamp = entry.lastModifiedTime ?: entry.creationTime
                    if (timestamp != null) {
                        file.setLastModified(timestamp.toMillis())
                    }
                }
                ++count
            }
        }
        return count
    }

    fun count(dir: File, predicate: (File) -> Boolean): Int {
        var count = 0
        dir.bot.walk { file, _ ->
            if (predicate(file)) ++count
        }
        return count
    }

    /**
     * Diff files, ignoring empty directories, in the given directories.
     */
    @Throws(IOException::class)
    fun diffDir(dir1: File, dir2: File): DiffStat<String> {
        val stat = DiffStat<String>()
        dir1.bot.walk { file1, rpath ->
            if (!file1.isFile) return@walk
            val file2 = File(dir2, rpath)
            when {
                !file2.isFile -> stat.aonly.add(rpath)
                diff(file1, file2) -> stat.diffs.add(rpath)
                else -> stat.sames.add(rpath)
            }
        }
        dir2.bot.walk { file, rpath ->
            if (!file.isFile) return@walk
            if (!File(dir1, rpath).isFile) {
                stat.bonly.add(rpath)
            }
        }
        return stat
    }

    /**
     * Diff files exists in dir1, ignoring extras in dir2.
     */
    @Throws(IOException::class)
    fun diffDir1(dir1: File, dir2: File): DiffStat<String> {
        val stat = DiffStat<String>()
        dir1.bot.walk { file1, rpath ->
            if (!file1.isFile) return@walk
            val file2 = File(dir2, rpath)
            when {
                !file2.isFile -> stat.aonly.add(rpath)
                diff(file1, file2) -> stat.diffs.add(rpath)
                else -> stat.sames.add(rpath)
            }
        }
        return stat
    }

    @Throws(IOException::class)
    fun diff(file1: File, file2: File): Boolean {
        if (!file1.exists() || !file2.exists()) return true
        var diff = false
        return Without.throwableOrNull {
            With.inputStream(file1) { input1 ->
                With.inputStream(file2) { input2 ->
                    diff = diff(input1, input2)
                }
            }
            diff
        } ?: true
    }

    @Throws(IOException::class)
    fun diff(input1: InputStream, input2: InputStream): Boolean {
        val b1 = ByteArray(BUFSIZE)
        val b2 = ByteArray(BUFSIZE)
        while (true) {
            val n1 = IOUt.readWhilePossible(input1, b1, 0, b1.size)
            val n2 = IOUt.readWhilePossible(input2, b2, 0, b2.size)
            if (n1 != n2) {
                return true
            }
            for (i in 0 until n1) {
                if (b1[i] != b2[i]) {
                    return true
                }
            }
            if (n1 < b1.size) {
                return false
            }
        }
    }

    @Throws(IOException::class)
    fun findDupBySHA1(files: Sequence<File>): MutableMap<String, NavigableSet<File>> {
        val ret = TreeMap<String, NavigableSet<File>>()
        for (file in files) {
            val digest = SumKind.SHA1.digestToHex(file)
            var a: NavigableSet<File>? = ret[digest]
            if (a == null) {
                a = TreeSet(lastModifiedComparator)
                ret[digest] = a
            }
            a.add(file)
        }
        return ret
    }

    fun findDup(dir: File): Map<File, NavigableSet<File>> {
        val files = dir.bot.collects(FilePathCollectors::fileOfAny)
        val bymd5 = findDupBySHA1(files)
        val ret = TreeMap<File, NavigableSet<File>>()
        for ((_, list) in bymd5) {
            while (true) {
                val file = list.pollFirst() ?: break
                val dups = TreeSet(lastModifiedComparator)
                for (other in list) {
                    if (!FileUt.diff(file, other)) {
                        dups.add(other)
                    }
                }
                for (dup in dups) list.remove(dup)
                ret[file] = dups
            }
        }
        return ret
    }

    fun shred(file: File): Boolean {
        if (!file.isFile) return false
        val bufsize = 4096
        val ch = Files.newByteChannel(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        val size = ch.size()
        val buf = ByteArray(bufsize)
        var pos = 0L
        while (pos < size) {
            ch.position(pos)
            val len = min(size - pos, bufsize.toLong()).toInt()
            RandomUt.get(buf)
            val n = ch.write(ByteBuffer.wrap(buf, 0, len))
            if (n < 0) return false
            pos += n
        }
        return true
    }
}

////////////////////////////////////////////////////////////////////

object FilePathCollectors {
    fun pairOfAny(file: File, rpath: String): Pair<File, String> {
        return Pair(file, rpath)
    }

    fun pairOfFiles(file: File, rpath: String): Pair<File, String>? {
        return if (file.isFile) Pair(file, rpath) else null
    }

    fun pairOfDirs(file: File, rpath: String): Pair<File, String>? {
        return if (file.isDirectory) Pair(file, rpath) else null
    }

    fun fileOfAny(file: File, @Suppress("UNUSED_PARAMETER") rpath: String): File {
        return file
    }

    fun fileOfFiles(file: File, @Suppress("UNUSED_PARAMETER") rpath: String): File? {
        return if (file.isFile) file else null
    }

    fun fileOfDirs(file: File, @Suppress("UNUSED_PARAMETER") rpath: String): File? {
        return if (file.isDirectory) file else null
    }

    fun pathOfAny(@Suppress("UNUSED_PARAMETER") file: File, rpath: String): String {
        return rpath
    }

    fun pathOfFiles(file: File, rpath: String): String? {
        return if (file.isFile) rpath else null
    }

    fun pathOfDirs(file: File, rpath: String): String? {
        return if (file.isDirectory) rpath else null
    }
}

//////////////////////////////////////////////////////////////////////

open class FileUtExt(protected val receiver: File) {

    val deprecated: FileUtDeprecated get() = FileUtDeprecated(receiver)

    open fun walk(
        basepath: String = "",
        bottomup: Boolean = false,
        callback: IFilePathCallback
    ) {
        U.walk1(receiver, basepath, bottomup, null, callback)
    }

    open fun <T> collects(
        basepath: String = "",
        bottomup: Boolean = false,
        collector: IFilePathCollector<T>
    ): Sequence<T> {
        return U.collect1(receiver, basepath, bottomup, null, collector)
    }

    /**
     * Like walk1() but it stop searching and return the first file
     * with which the predicate returns true.
     */
    open fun find(
        basepath: String = "",
        bottomup: Boolean = false,
        accept: IFilePathPredicate
    ): File? {
        return U.find1(receiver, basepath, bottomup, null, accept)
    }

    fun scan(basepath: String = "", predicate: IFilePathPredicate) {
        U.scan1(receiver, basepath, predicate)
    }

    ////////////////////////////////////////////////////////////

    fun files(
        basepath: String = "",
        bottomup: Boolean = false,
        callback: IFilePathCallback
    ) {
        this.walk(basepath, bottomup) { file, rpath ->
            if (file.isFile) callback(file, rpath)
        }
    }

    fun dirs(
        basepath: String = "",
        bottomup: Boolean = false,
        callback: IFilePathCallback
    ) {
        this.walk(basepath, bottomup) { file, rpath ->
            if (file.isDirectory) callback(file, rpath)
        }
    }

    fun findOrFail(
        basepath: String = "",
        bottomup: Boolean = false,
        accept: IFilePathPredicate
    ): File {
        return this.find(basepath, bottomup, accept)
            ?: error(receiver.absolutePath)
    }

    ////////////////////////////////////////////////////////////

    fun collects(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<Pair<File, String>> {
        return this.collects(basepath, bottomup, FilePathCollectors::pairOfAny)
    }

    fun <T> collects(
        collector: IFilePathCollector<T>
    ): Sequence<T> {
        return this.collects("", false, collector)
    }

    fun fileOfFiles(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<File> {
        return collects(basepath, bottomup, FilePathCollectors::fileOfFiles)
    }

    fun fileOfDirs(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<File> {
        return collects(basepath, bottomup, FilePathCollectors::fileOfDirs)
    }

    fun fileOfAny(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<File> {
        return collects(basepath, bottomup, FilePathCollectors::fileOfAny)
    }

    fun pathOfFiles(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<String> {
        return collects(basepath, bottomup, FilePathCollectors::pathOfFiles)
    }

    fun pathOfDirs(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<String> {
        return collects(basepath, bottomup, FilePathCollectors::pathOfDirs)
    }

    fun pathOfAny(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<String> {
        return collects(basepath, bottomup, FilePathCollectors::pathOfAny)
    }

    fun pairOfFiles(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<Pair<File, String>> {
        return collects(basepath, bottomup, FilePathCollectors::pairOfFiles)
    }

    fun pairOfDirs(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<Pair<File, String>> {
        return collects(basepath, bottomup, FilePathCollectors::pairOfDirs)
    }

    fun pairOfAny(
        basepath: String = "",
        bottomup: Boolean = false,
    ): Sequence<Pair<File, String>> {
        return collects(basepath, bottomup, FilePathCollectors::pairOfAny)
    }

    ////////////////////////////////////////////////////////////

    fun findDup(): Map<File, NavigableSet<File>> {
        return FileUt.findDup(receiver)
    }

    fun findDupBySHA1(): MutableMap<String, NavigableSet<File>> {
        return FileUt.findDupBySHA1(receiver.bot.fileOfAny())
    }

    fun copyas(dstfile: File, preservetimestamp: Boolean = false) {
        FileUt.copyas(dstfile, receiver, preservetimestamp)
    }

    fun copyto(dstdir: File, preservetimestamp: Boolean = false) {
        FileUt.copyto(dstdir, receiver, preservetimestamp)
    }

    fun copydiff(dstfile: File, preservetimestamp: Boolean = false) {
        FileUt.copydiff(dstfile, receiver, preservetimestamp)
    }

    fun diff(other: File): Boolean {
        return FileUt.diff(receiver, other)
    }

    fun diffDir(otherdir: File): DiffStat<String> {
        return FileUt.diffDir(receiver, otherdir)
    }

    fun rpath(basedir: File): String? {
        return FileUt.rpath(receiver, basedir)
    }
}

class FileUtDeprecated(
    receiver: File,
) : FileUtExt(receiver) {
    private var ignoresdir: IFilePathPredicate? = null

    fun ignoresdir(predicate: IFilePathPredicate): FileUtDeprecated {
        this.ignoresdir = predicate
        return this
    }

    override fun walk(
        basepath: String,
        bottomup: Boolean,
        callback: IFilePathCallback
    ) {
        U.walk1(receiver, basepath, bottomup, ignoresdir, callback)
    }

    override fun <T> collects(
        basepath: String,
        bottomup: Boolean,
        collector: IFilePathCollector<T>
    ): Sequence<T> {
        return U.collect1(receiver, basepath, bottomup, ignoresdir, collector)
    }

    /**
     * Like walk1() but it stop searching and return the first file
     * with which the predicate returns true.
     */
    override fun find(
        basepath: String,
        bottomup: Boolean,
        accept: IFilePathPredicate
    ): File? {
        return U.find1(receiver, basepath, bottomup, ignoresdir, accept)
    }

    fun <T> walk3(
        ret: T,
        basepath: String = "",
        callback: Fun31<T, File, String, T>
    ) {
        walk3(ret, receiver, basepath, ignoresdir, callback)
    }

    private fun <T> walk3(
        ret: T,
        dir: File,
        rpath: String = "",
        ignoresdir: IFilePathPredicate? = null,
        callback: Fun31<T, File, String, T>
    ) {
        val parentpath = dir.toPath()
        for (name in dir.listOrEmpty()) {
            val file = File(dir, name)
            val filepath = if (rpath.isEmpty()) name else "$rpath$FS$name"
            val ret1 = callback(ret, file, filepath)
            if (file.isDirectory
                && !FileUt.isRecursiveSymlink(file.toPath(), parentpath)
                && (ignoresdir == null || !ignoresdir(file, filepath))
            ) {
                walk3(ret1, file, filepath, ignoresdir, callback)
            }
        }
    }
}

private object U {
    fun scan1(
        dir: File,
        rpath: String,
        predicate: IFilePathPredicate
    ) {
        val parentpath = dir.toPath()
        for (name in dir.listOrEmpty()) {
            val file = File(dir, name)
            val filepath = if (rpath.isEmpty()) name else "$rpath$FSC$name"
            if (predicate(file, filepath)
                && file.isDirectory
                && !FileUt.isRecursiveSymlink(file.toPath(), parentpath)
            ) {
                scan1(file, filepath, predicate)
            }
        }
    }

    fun walk1(
        dir: File,
        rpath: String,
        bottomup: Boolean,
        ignoresdir: IFilePathPredicate?,
        callback: IFilePathCallback
    ) {
        val parentpath = dir.toPath()
        for (name in dir.listOrEmpty()) {
            val file = File(dir, name)
            val filepath = if (rpath.isEmpty()) name else "$rpath$FS$name"
            if (!bottomup) callback(file, filepath)
            if (file.isDirectory
                && !FileUt.isRecursiveSymlink(file.toPath(), parentpath)
                && (ignoresdir == null || !ignoresdir(file, filepath))
            ) {
                walk1(file, filepath, bottomup, ignoresdir, callback)
            }
            if (bottomup) callback(file, filepath)
        }
    }

    /**
     * Walk the directory recursively.
     *
     * @return Sequence<Pair<File, String>> File/directory where includes() return true.
     */
    fun <T> collect1(
        dir: File,
        dirpath: String,
        bottomup: Boolean,
        ignoresdir: IFilePathPredicate?,
        collector: IFilePathCollector<T>
    ): Sequence<T> {
        return sequence {
            collect2(dir, dirpath, bottomup, ignoresdir, collector)
        }
    }

    private suspend fun <T> SequenceScope<T>.collect2(
        dir: File,
        dirpath: String,
        bottomup: Boolean,
        ignoresdir: IFilePathPredicate?,
        collector: IFilePathCollector<T>
    ) {
        val parentpath = dir.toPath()
        for (name in dir.listOrEmpty()) {
            val file = File(dir, name)
            val filepath = if (dirpath.isEmpty()) name else "$dirpath$FS$name"
            if (!bottomup) {
                collector(file, filepath)?.let {
                    yield(it)
                }
            }
            if (file.isDirectory
                && !FileUt.isRecursiveSymlink(file.toPath(), parentpath)
                && (ignoresdir == null || !ignoresdir(file, filepath))
            ) {
                collect2(file, filepath, bottomup, ignoresdir, collector)
            }
            if (bottomup) {
                collector(file, filepath)?.let {
                    yield(it)
                }
            }
        }
    }

    fun find1(
        dir: File,
        rpath: String,
        bottomup: Boolean,
        ignoresdir: IFilePathPredicate?,
        accept: IFilePathPredicate
    ): File? {
        val parentpath = dir.toPath()
        for (name in dir.listOrEmpty()) {
            val file = File(dir, name)
            val filepath = if (rpath.isEmpty()) name else "$rpath$FS$name"
            if (!bottomup && accept(file, filepath)) return file
            if (file.isDirectory
                && !FileUt.isRecursiveSymlink(file.toPath(), parentpath)
                && (ignoresdir == null || !ignoresdir(file, filepath))
            ) {
                val ret = find1(file, filepath, bottomup, ignoresdir, accept)
                if (ret != null) return ret
            }
            if (bottomup && accept(file, filepath)) return file
        }
        return null
    }
}

////////////////////////////////////////////////////////////////////
