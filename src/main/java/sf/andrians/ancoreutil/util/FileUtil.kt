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
package sf.andrians.ancoreutil.util

import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun20
import com.cplusedition.bot.core.Fun21
import sf.andrians.ancoreutil.util.io.ByteIOUtil
import sf.andrians.ancoreutil.util.struct.ArrayUtil
import sf.andrians.ancoreutil.util.struct.Empty
import sf.andrians.ancoreutil.util.struct.IterableWrapper
import sf.andrians.ancoreutil.util.struct.ReversedComparator
import sf.andrians.ancoreutil.util.struct.StructUtil
import sf.andrians.ancoreutil.util.text.ChunkedCharBuffer
import sf.andrians.ancoreutil.util.text.ICharSequence
import sf.andrians.ancoreutil.util.text.MatchUtil
import sf.andrians.ancoreutil.util.text.TextUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.Flushable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Reader
import java.io.Writer
import java.net.URL
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Static file utilities.
 */
object FileUtil {

    private const val BUFSIZE = 16 * 1024
    private val ROOT = File("", "")

    @get:Synchronized
    var fileFilter: FileFilter? = null
        get() {
            if (field == null) {
                field = FileFilter { file -> file.isFile }
            }
            return field
        }
        private set

    @get:Synchronized
    var dirFilter: FileFilter? = null
        get() {
            if (field == null) {
                field = FileFilter { file -> file.isDirectory }
            }
            return field
        }
        private set

    ////////////////////////////////////////////////////////////////////

    fun today(format: String): File {
        return File(TextUtil.today(format))
    }

    fun isEmpty(dir: File): Boolean {
        require(dir.isDirectory) { "Expecting a directory: $dir" }
        val list = dir.list()
        return list == null || list.size == 0
    }

    /**
     * @return true if infile is newer than outfile.
     */
    fun isNewer(infile: File, outfile: File): Boolean {
        return !outfile.exists() || infile.lastModified() > outfile.lastModified()
    }

    /**
     * @return true if any of the srcfiles is newer than outfile.
     */
    fun isNewer(outfile: File, srcfiles: Collection<File>): Boolean {
        if (!outfile.exists()) {
            return true
        }
        val lastmodified = outfile.lastModified()
        for (src in srcfiles) {
            if (src.lastModified() >= lastmodified) {
                return true
            }
        }
        return false
    }

    /**
     * @return true if infile is older than outfile.
     */
    fun isOlder(infile: File, outfile: File): Boolean {
        return outfile.exists() && infile.lastModified() <= outfile.lastModified()
    }

    /**
     * @return true if any of the input files is newer than outfile.
     */
    fun hasModified(ofile: File, ifiles: Collection<File>): Boolean {
        for (ifile in ifiles) {
            if (isNewer(ifile, ofile)) {
                return true
            }
        }
        return false
    }

    /**
     * @return true if any of the input files is newer than outfile.
     */
    fun hasModified(ofile: File, ifiles: Array<File>): Boolean {
        for (ifile in ifiles) {
            if (isNewer(ifile, ofile)) {
                return true
            }
        }
        return false
    }

    fun hasModified(outdir: File, regex: Pattern, replace: String, indir: File, vararg rpath: String): Boolean {
        return hasModified(outdir, regex, replace, indir, IterableWrapper.wrap(*rpath))
    }

    fun hasModified(outdir: File, regex: Pattern, replace: String, indir: File, rpaths: Iterable<String>): Boolean {
        for (rpath in rpaths) {
            val opath = MatchUtil.replaceAll(regex, replace, rpath)
            if (isNewer(File(indir, rpath), File(outdir, opath))) {
                return true
            }
        }
        return false
    }

    /**
     * @return The set of input files that are newer than the output file.
     */
    fun modifiedSet(ofile: File, ifiles: Collection<File>): Set<File> {
        val ret = TreeSet<File>()
        for (ifile in ifiles) {
            if (isNewer(ifile, ofile)) {
                ret.add(ifile)
            }
        }
        return ret
    }

    fun modifiedSet(outdir: File, regex: Pattern, replace: String, indir: File, rpaths: Iterable<String>): Set<String> {
        val ret = TreeSet<String>()
        for (rpath in rpaths) {
            val opath = MatchUtil.replaceAll(regex, replace, rpath)
            if (isNewer(File(indir, rpath), File(outdir, opath))) {
                ret.add(rpath)
            }
        }
        return ret
    }

    /**
     * Remove files that match the java regex includes and do not match excludes from the input collection.
     *
     * @return The removed items.
     */
    fun filter(files: MutableCollection<File>, includes: String?, excludes: String?): List<File> {
        return filter(files, MatchUtil.compile1(includes), MatchUtil.compile1(excludes))
    }

    /**
     * Remove files that match the java regex includes and do not match excludes from the input collection.
     *
     * @return The removed items.
     */
    fun filter(files: MutableCollection<File>, includes: Pattern?, excludes: Pattern?): List<File> {
        val ret: MutableList<File> = ArrayList()
        val it = files.iterator()
        while (it.hasNext()) {
            val file = it.next()
            if (MatchUtil.match(file.absolutePath, includes, excludes)) {
                it.remove()
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * Keep files that match the java regex includes and do not match excludes in the input collection.
     *
     * @return The removed items.
     */
    fun keep(files: MutableCollection<File>, includes: String?, excludes: String?): List<File> {
        return keep(files, MatchUtil.compile1(includes), MatchUtil.compile1(excludes))
    }

    /**
     * Keep only files that match the java regex includes and do not match excludes in the input collection.
     *
     * @return The removed items.
     */
    fun keep(files: MutableCollection<File>, includes: Pattern?, excludes: Pattern?): List<File> {
        val ret: MutableList<File> = ArrayList()
        val it = files.iterator()
        while (it.hasNext()) {
            val file = it.next()
            if (!MatchUtil.match(file.absolutePath, includes, excludes)) {
                it.remove()
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * Grep files that match the given java regexs without modifying the input collection.
     *
     * @return Files that match the java regex includes and not match excludes.
     */
    fun grep(files: Collection<File>, includes: String?, excludes: String?): List<File> {
        return grep(files, MatchUtil.compile1(includes), MatchUtil.compile1(excludes))
    }

    /**
     * Grep files using given java regexs, without modifying the input collection.
     *
     * @return Files that match the java regex includes and not match excludes.
     */
    fun grep(files: Collection<File>, includes: Pattern?, excludes: Pattern?): List<File> {
        val ret: MutableList<File> = ArrayList()
        val it = files.iterator()
        while (it.hasNext()) {
            val file = it.next()
            if (MatchUtil.match(file.absolutePath, includes, excludes)) {
                ret.add(file)
            }
        }
        return ret
    }

    @Throws(IOException::class)
    fun <T : MutableCollection<String>> fgrep(ret: T, file: File, pat: String): T {
        return fgrep(ret, file, Pattern.compile(pat))
    }

    /**
     * Grep file lines that match the given pattern and return all the group(1) of the the matches.
     *
     * @return The lines that matches.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun <T : MutableCollection<String>> fgrep(ret: T, file: File, pat: Pattern, charset: Charset? = null): T {
        for (line in asStringList(file, charset)) {
            val m = pat.matcher(line)
            if (m.matches()) {
                ret.add(line)
            }
        }
        return ret
    }

    @Throws(IOException::class)
    fun <T : MutableCollection<String>> fgrep1(ret: T, file: File, pat: String, charset: Charset? = null): T {
        return fgrep1(ret, file, Pattern.compile(pat), charset)
    }

    /**
     * Grep file lines that match the given pattern.
     *
     * @return group(1) of all matches.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun <T : MutableCollection<String>> fgrep1(ret: T, file: File, pat: Pattern, charset: Charset? = null): T {
        for (line in asStringList(file, charset)) {
            val m = pat.matcher(line)
            if (m.matches()) {
                ret.add(m.group(1))
            }
        }
        return ret
    }

    /**
     * Grep file lines that match the given pattern.
     *
     * @return The matcher of all the matches.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun <T : MutableCollection<Matcher>> fgrepMatcher(ret: T, file: File, pat: Pattern, charset: Charset? = null): T {
        for (line in asStringList(file, charset)) {
            val m = pat.matcher(line)
            if (m.matches()) {
                ret.add(m)
            }
        }
        return ret
    }

    fun map(regex: Pattern, replace: String, rpaths: Iterable<String>): Set<String> {
        val ret = TreeSet<String>()
        for (rpath in rpaths) {
            ret.add(MatchUtil.replaceAll(regex, replace, rpath))
        }
        return ret
    }

    fun mapExists(outdir: File, regex: Pattern, replace: String, rpaths: Iterable<String>): Set<String> {
        val ret = TreeSet<String>()
        for (rpath in rpaths) {
            val ofile = File(outdir, MatchUtil.replaceAll(regex, replace, rpath))
            if (ofile.exists()) {
                ret.add(rpath)
            }
        }
        return ret
    }

    fun mapExistFiles(outdir: File, regex: Pattern, replace: String, rpaths: Iterable<String>): Set<File> {
        val ret = TreeSet<File>()
        for (rpath in rpaths) {
            val ofile = File(outdir, MatchUtil.replaceAll(regex, replace, rpath))
            if (ofile.exists()) {
                ret.add(ofile)
            }
        }
        return ret
    }

    /**
     * @return The set of input files that are newer than the output file.
     */
    fun modifiedSet(ofile: File, ifiles: Array<File>): Set<File> {
        val ret = TreeSet<File>()
        for (ifile in ifiles) {
            if (isNewer(ifile, ofile)) {
                ret.add(ifile)
            }
        }
        return ret
    }

    fun home(): File {
        return File(System.getProperty("user.home"))
    }

    fun home(vararg segments: String): File {
        return file(File(System.getProperty("user.home")), *segments)
    }

    fun pwd(): File {
        return File(System.getProperty("user.dir"))
    }

    fun pwd(vararg segments: String): File {
        return file(File(System.getProperty("user.dir")), *segments)
    }

    fun root(): File {
        return ROOT
    }

    @kotlin.jvm.JvmStatic
    fun root(vararg segments: String?): File {
        return File(ROOT, TextUtil.filepath(*segments))
    }

    /**
     * Walk up to the ancestor and look for the first file at ancestor/rpath that exists.
     *
     * @return The file at ancestor/rpath if found, otherwise null.
     */
    fun ancestorSubtree(file: File, rpath: String): File? {
        var parent = file.absoluteFile.parentFile
        while (parent != null) {
            val f = File(parent, rpath)
            if (f.exists()) return f
            parent = parent.parentFile
        }
        return null
    }

    fun ancestor(file: File, generations: Int): File? {
        var ret = file.absoluteFile
        var count = generations
        while (--count >= 0) {
            ret = ret.parentFile ?: return null
        }
        return ret
    }

    fun mkdirs(dir: String): File {
        return mkdirs(File(dir))
    }

    fun mkdirs(dir: File): File {
        if (!dir.exists() && !dir.mkdirs()) {
            throw AssertionError("FATAL: mkdirs failed: $dir")
        }
        return dir
    }

    fun mkdirs(dir: File, vararg segments: String): File {
        return mkdirs(file(dir, *segments))
    }

    fun mkparent(file: File): File {
        mkdirs(file.absoluteFile.parentFile)
        return file
    }

    fun mkparent(file: File, vararg segments: String): File {
        val f = file(file, *segments)
        mkdirs(f.absoluteFile.parentFile)
        return f
    }

    fun mkdirs(vararg dirs: File) {
        for (dir in dirs) {
            mkdirs(dir)
        }
    }

    fun mkparent(vararg files: File) {
        for (file in files) {
            mkparent(file)
        }
    }

    fun cleanFile(dir: File, vararg segments: String?): File {
        return File(TextUtil.cleanupFilePath(file(dir, *segments).absolutePath).toString())
    }

    fun file(dir: File, vararg segments: String?): File {
        return if (segments.isEmpty()) {
            dir
        } else File(dir, TextUtil.filepath(*segments))
    }

    fun filef(format: String, vararg args: Any): File {
        return File(TextUtil.format(format, *args))
    }

    fun file(vararg segments: String?): File {
        return File(TextUtil.filepath(*segments))
    }

    fun file(dir: File, up: Int, vararg segments: String): File {
        var dir = dir
        var up = up
        dir = dir.absoluteFile
        while (--up >= 0) {
            dir = dir.parentFile
        }
        return file(dir, *segments)
    }

    fun format(format: String, vararg args: Any): File {
        return File(TextUtil.format(format, *args))
    }

    fun aformat(format: String, vararg args: Any): File {
        return format(format, *args).absoluteFile
    }

    fun apath(dir: File, vararg segments: String): String {
        return file(dir, *segments).absolutePath
    }

    fun apath(vararg segments: String?): String {
        return File(TextUtil.filepath(*segments)).absolutePath
    }

    fun aparent(file: File): File? {
        return file.absoluteFile.parentFile
    }

    fun sibling(file: File, vararg segments: String): File {
        return file(file.absoluteFile.parentFile, *segments)
    }

    fun afile(path: String): File {
        var ret = File(path)
        if (!ret.isAbsolute) {
            ret = ret.absoluteFile
        }
        return ret
    }

    fun afile(basedir: File?, path: String): File {
        var ret = File(path)
        if (!ret.isAbsolute) {
            ret = File(basedir, path).absoluteFile
        }
        return ret
    }

    fun apaths(vararg files: File): Array<String> {
        return Array(files.size) {
            files[it].absolutePath
        }
    }

    fun apaths(files: Collection<File>): Array<String> {
        val iter = files.iterator()
        return Array(files.size) {
            iter.next().absolutePath
        }
    }

    /**
     * This is same as the rpath() counterpart except that it returns null
     * instead of throwing RuntimeException if result path is not valid.
     *
     * @return Relative paths without leading /.
     */
    fun rpathX(files: Collection<File>, basedir: File): List<String?> {
        val ret: MutableList<String?> = ArrayList()
        for (file in files) {
            ret.add(rpathX(file, basedir))
        }
        return ret
    }
    /**
     * Specialize cleanPath() for rpathX.
     * Result may not be same as cleanPath(), but good for rpathX.
     */
    private fun cleanPathSegments(b: CharSequence): List<String> {
        var c: Char
        val blen = b.length
        var i = 0
        val sep = File.separatorChar
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
                    if (i + 1 >= blen || b[i + 1] == sep) {
                        ++i
                        ++i
                        continue
                    }
                    if (i + 1 < blen && b[i + 1] == '.' && (i + 2 >= blen || b[i + 2] == sep)) {
                        val retsize = ret.size
                        if (retsize > 0 && ".." != ret[retsize - 1]) {
                            ret.removeAt(retsize - 1)
                            ++i
                            ++i
                            continue
                        }
                    }
                }
            }
            if (c == sep) {
                if (buf.isNotEmpty()) ret.add(buf.toString())
                buf.setLength(0)
            } else {
                buf.append(c)
            }
            ++i
        }
        if (buf.isNotEmpty()) ret.add(buf.toString())
        return ret
    }

    /// @return Relative path without leading / or null if file is not under base.
    @kotlin.jvm.JvmStatic
    fun rpathX(file: File, base: File): String? {
        return rpathX(file.absolutePath, base.absolutePath)
    }

    fun rpathX(path: String, basepath: String): String? {
        val f = cleanPathSegments(path)
        val b = cleanPathSegments(basepath)
        if (f.contains("..") || b.contains("..")) return null
        val blen = b.size
        val flen = f.size
        var i = 0
        while (i < blen && i < flen) {
            if (f[i] != b[i]) {
                break
            }
            ++i
        }
        return if (i < blen) null else TextUtil.join(File.separator, f.subList(i, flen))
    }

    /// Like rpathX() but allow path not under basedir.
    /// @return Relative path without leading /, but possibly with leading ../ or null on error.
    fun rpathOrNull(path: String, basedirpath: String): String? {
        val f = cleanPathSegments(path)
        val b = cleanPathSegments(basedirpath)
        if (f.contains("..") || b.contains("..")) return null
        val blen = b.size
        val flen = f.size
        var i = 0
        while (i < blen && i < flen) {
            if (f[i] != b[i]) {
                break
            }
            ++i
        }
        val rpath = TextUtil.join(File.separator, f.subList(i, flen))
        if (i < blen) {
            val uppath = TextUtil.upPath(TextUtil.join(File.separator, b.subList(i, blen)))
            return TextUtil.join(File.separator, uppath, rpath)
        }
        return rpath
    }

    /**
     * @return Relative paths of given files relative to the basedir.
     * @throws RuntimeException if unable to derive a valid relative path.
     */
    fun rpath(files: Collection<File>, basedir: File): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (file in files) {
            ret.add(rpath(file, basedir))
        }
        return ret
    }

    /**
     * @return Relative path of given file relative to the basedir.
     * @throws RuntimeException if unable to derive a valid relative path.
     */
    fun rpath(file: File, basedir: File): String {
        return rpathX(file, basedir) ?: throw RuntimeException("File is not under basedir: file=$file, basedir=$basedir")
    }

    fun addSuffix(file: File, suffix: String): File {
        return File(file.absoluteFile.parentFile, file.name + suffix)
    }

    fun removeSuffix(file: File, suffix: String): File {
        var name = file.name
        if (name.endsWith(suffix)) {
            name = name.substring(0, name.length - suffix.length)
            return File(file.absoluteFile.parentFile, name)
        }
        throw AssertionError("ASSERT: File not end with suffix: file=$file, suffix=$suffix")
    }

    fun prefix(basedir: File?, rpaths: Array<String>): Array<File> {
        return Array(rpaths.size) { File(basedir, rpaths[it]) }
    }

    /**
     * @return basename (without dir and extension) of given file.
     */
    fun basename(file: File): String {
        var ret = file.absoluteFile.name
        val index = ret.lastIndexOf('.')
        if (index >= 0) {
            ret = ret.substring(0, index)
        }
        return ret
    }

    fun basename(path: String): String {
        var path1 = path
        var index = path1.lastIndexOf(File.separatorChar)
        if (index >= 0) {
            path1 = path1.substring(index + 1)
        }
        index = path1.lastIndexOf('.')
        if (index >= 0) {
            path1 = path1.substring(0, index)
        }
        return path1
    }

    fun withExt(file: File, vararg exts: String): Boolean {
        val name = file.name
        val index = name.lastIndexOf('.')
        if (index < 0) {
            return false
        }
        val ext = name.substring(index + 1)
        for (s in exts) {
            if (ext == s) {
                return true
            }
        }
        return false
    }

    fun withExtIgnorecase(file: File, vararg exts: String): Boolean {
        val name = file.name
        val index = name.lastIndexOf('.')
        if (index < 0) {
            return false
        }
        val ext = name.substring(index + 1)
        for (s in exts) {
            if (ext.equals(s, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Replace any extension of the given file with the given ext,
     * eg. replaceExt(new File("test.java"), ".class") returns test.class.
     */
    fun replaceExt(file: File, ext: String): File {
        return sibling(file, basename(file) + ext)
    }

    fun canonicalPwd(): File {
        return canonicalFile(pwd())
    }

    fun canonicalFile(path: String): File {
        return canonicalFile(File(path))
    }

    fun canonicalFile(file: File): File {
        return try {
            file.canonicalFile
        } catch (e: IOException) {
            file
        }
    }

    fun canonicalFile(file: File, path: String): File {
        var file = file
        file = File(file, path)
        return try {
            file.canonicalFile
        } catch (e: IOException) {
            file
        }
    }

    fun canonicalFiles(files: Collection<File>): MutableCollection<File> {
        val ret: MutableCollection<File> = TreeSet()
        for (file in files) {
            try {
                ret.add(file.canonicalFile)
            } catch (e: IOException) {
                ret.add(file)
            }
        }
        return ret
    }

    fun canonicalPath(file: File): String {
        return try {
            file.canonicalPath
        } catch (e: IOException) {
            file.absolutePath
        }
    }

    fun canonicalPaths(files: Collection<File>): Collection<String> {
        val ret: MutableCollection<String> = TreeSet()
        for (file in files) {
            try {
                ret.add(file.canonicalPath)
            } catch (e: IOException) {
                ret.add(file.absolutePath)
            }
        }
        return ret
    }

    fun isAbsolute(path: String): Boolean {
        return File(path).isAbsolute
    }

    fun isBaseDir(base: File, file: File): Boolean {
        val base1 = base.absoluteFile
        var file1: File? = file.absoluteFile
        while (file1 != null) {
            if (file1.equals(base1)) {
                return true
            }
            file1 = file1.parentFile
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////

    fun reverseSorted(files: Collection<File>): SortedSet<File> {
        val ret = TreeSet<File>(reversedFileComparator)
        ret.addAll(files)
        return ret
    }

    fun toFiles(paths: Collection<String>?): List<File>? {
        if (paths == null) {
            return null
        }
        val ret: MutableList<File> = ArrayList()
        for (path in paths) {
            ret.add(File(path))
        }
        return ret
    }

    fun toFiles(basedir: File?, vararg rpaths: String): Array<File> {
        return Array(rpaths.size) {
            File(basedir, rpaths[it])
        }
    }

    fun toFiles(paths: Array<String>, start: Int, end: Int): Array<File> {
        return Array(end - start) {
            File(paths[start + it])
        }
    }

    fun toArray(files: Collection<File>?): Array<File>? {
        return files?.toTypedArray()
    }

    fun removeTree(dir: File?): Boolean {
        if (dir != null && dir.exists()) {
            val files = collect(TreeSet<File>(ReverseFileComparator.singleton), dir)
            for (file in files) {
                if (file.exists() && !file.delete()) {
                    return false
                }
            }
            if (!dir.delete()) {
                return false
            }
        }
        return true
    }

    /**
     * Like removeTree() but don't remove input directory.
     */
    fun removeSubTrees(dir: File?): Boolean {
        if (dir != null && dir.exists()) {
            val files = collect(TreeSet<File>(ReverseFileComparator.singleton), dir)
            for (file in files) {
                if (file.exists() && !file.delete()) {
                    return false
                }
            }
        }
        return true
    }

    fun deleteEmptySubtrees(dir: File) {
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory) {
                deleteEmptyTree(file)
            }
        }
    }

    fun deleteEmptyTree(dir: File) {
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory) {
                deleteEmptyTree(file)
            }
        }
        if (list(dir).size == 0) {
            delete(dir)
        }
    }
    ////////////////////////////////////////////////////////////////////

    /**
     * @return simple name of directories directly under the given directory.
     */
    fun listDirs(dir: File): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (name in list(dir)) {
            if (File(dir, name).isDirectory) {
                ret.add(name)
            }
        }
        return ret
    }

    /**
     * @return simple name of files (not directory) directly under the given directory.
     */
    fun listFiles(dir: File): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (name in list(dir)) {
            if (File(dir, name).isFile) {
                ret.add(name)
            }
        }
        return ret
    }

    /**
     * @return simple name of directories directly under the given directory that match the given java regex.
     */
    fun listDirs(dir: File, includes: String?, excludes: String?): MutableList<String> {
        val include = if (includes == null) null else Pattern.compile(includes)
        val exclude = if (excludes == null) null else Pattern.compile(excludes)
        val ret: MutableList<String> = ArrayList()
        for (name in list(dir)) {
            if (File(dir, name).isDirectory && MatchUtil.match(name, include, exclude)) {
                ret.add(name)
            }
        }
        return ret
    }

    /**
     * @return simple name of files directly under the given directory that match the given java regex.
     */
    fun listFiles(dir: File, includes: String?, excludes: String?): MutableList<String> {
        val include = if (includes == null) null else Pattern.compile(includes)
        val exclude = if (excludes == null) null else Pattern.compile(excludes)
        val ret: MutableList<String> = ArrayList()
        for (name in list(dir)) {
            if (File(dir, name).isFile && MatchUtil.match(name, include, exclude)) {
                ret.add(name)
            }
        }
        return ret
    }

    /**
     * @return simple name of files or directories directly under the given directory
     * that is accepted by the given filter.
     */
    fun <T : MutableCollection<String>> list(ret: T, dir: File, filter: FilenameFilter?): T {
        for (name in list(dir)) {
            if (filter == null || filter.accept(dir, name)) {
                ret.add(name)
            }
        }
        return ret
    }

    /**
     * @return Directories directly under the given directory.
     */
    fun dirs(dir: File): MutableList<File> {
        val ret: MutableList<File> = ArrayList()
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory) {
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * @return Files directly under the given directory.
     */
    fun dirs(dir: File, include: String?): MutableList<File> {
        return dirs(dir, MatchUtil.compile1(include), null)
    }

    /**
     * @return Directories directly under the given directory whose simple name
     * matches the given java regexs.
     */
    fun dirs(dir: File, include: Pattern?, exclude: Pattern?): MutableList<File> {
        val ret: MutableList<File> = ArrayList()
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory && MatchUtil.match(file.name, include, exclude)) {
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * @return Files (not directory) directly under the given directory.
     */
    fun files(dir: File): MutableList<File> {
        val ret: MutableList<File> = ArrayList()
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isFile) {
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * @return simple name of files (not directory) directly under the given directory
     * that matches the given java regex.
     */
    fun files(dir: File, include: String?): MutableList<File> {
        return files(dir, MatchUtil.compile1(include), null)
    }

    /**
     * @return simple name of files (not directory) directly under the given directory
     * that matches the given java regex.
     */
    fun files(dir: File, include: Pattern?, exclude: Pattern?): MutableList<File> {
        val ret: MutableList<File> = ArrayList()
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isFile && MatchUtil.match(file.name, include, exclude)) {
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * @return Files or directory directly under the given directory
     * whose simple name matches the given java regexs.
     */
    fun <T : MutableCollection<File>> list(ret: T, dir: File, include: Pattern?, exclude: Pattern?): T {
        for (name in list(dir)) {
            if (MatchUtil.match(name, include, exclude)) {
                ret.add(File(dir, name))
            }
        }
        return ret
    }

    fun <T : MutableCollection<File>> list(ret: T, dir: File, filter: FileFilter?): T {
        for (name in list(dir)) {
            val file = File(dir, name)
            if (filter == null || filter.accept(file)) {
                ret.add(file)
            }
        }
        return ret
    }

    /**
     * @return Absolute/relative path of files/directory under dir that are accepted by the filter.
     */
    fun listRecursive(dir: File, absolute: Boolean, filter: FileFilter?): MutableList<String> {
        val ret: MutableList<String> = ArrayList()
        listRecursive(ret, dir, if (absolute) null else dir, filter)
        return ret
    }

    /**
     * @return Absolute (if basedir==null)/relative path of files/directory under dir that are accepted by the filter.
     */
    fun listRecursive(dir: File, basedir: File?, filter: FileFilter?): MutableList<String> {
        val ret: MutableList<String> = ArrayList()
        listRecursive(ret, dir, basedir, filter)
        return ret
    }

    /**
     * @return Absolute (if basedir==null)/relative path of files/directory under dir that are accepted by the filter.
     */
    fun <T : MutableCollection<String>> listRecursive(ret: T, dir: File, basedir: File?, filter: FileFilter?): T {
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory) {
                listRecursive(ret, file, basedir, filter)
            }
            if (filter == null || filter.accept(file)) {
                ret?.add(basedir?.let { rpath(file, it) } ?: file.absolutePath)
            }
        }
        return ret
    }

    /**
     * @param basedir null for absolute path.
     * @param filter  null to accept all file/directory.
     * Print path of files/directory under dir relative to basedir that are accepted by the filter.
     */
    fun listRecursive(out: PrintStream, dir: File, basedir: File?, filter: FileFilter?) {
        for (name in list(dir)) {
            val file = File(dir, name)
            if (file.isDirectory) {
                listRecursive(out, file, basedir, filter)
            }
            if (filter == null || filter.accept(file)) {
                out.println(basedir?.let { rpath(file, it) } ?: file.absolutePath)
            }
        }
    }

    /**
     * @return Files/directories under the given dir that has filename
     * match the given java.util.regex expressions.
     */
    fun findFilename(dir: File, include: String?): MutableCollection<File> {
        return findFilename(ArrayList(), dir, include)
    }

    /**
     * @return Files/directories under the given dir that has filename
     * match the given java.util.regex expressions.
     */
    fun <T : MutableCollection<File>> findFilename(ret: T, dir: File, include: String? = null): T {
        val pat = MatchUtil.compile1(include)
        collect(ret, dir, FileFilter { file ->
            pat == null || pat.matcher(file.name).matches()
        })
        return ret
    }

    /**
     * @return Files, not directories, under the given dir.
     */
    fun collectFiles(dir: File): MutableCollection<File> {
        return collect(dir, fileFilter)
    }

    /**
     * @return Directories, not files, under the given dir.
     */
    fun collectDirs(dir: File): MutableCollection<File> {
        return collect(dir, dirFilter)
    }

    /**
     * @return Files, not directories, under the given dir.
     */
    fun <T : MutableCollection<File>> collectFiles(ret: T, vararg dirs: File): T {
        for (dir in dirs) {
            collect(ret, dir, fileFilter)
        }
        return ret
    }

    /**
     * @return Directories, not files, under the given dir.
     */
    fun <T : MutableCollection<File>> collectDirs(ret: T, vararg dirs: File): T {
        for (dir in dirs) {
            collect(ret, dir, dirFilter)
        }
        return ret
    }

    /**
     * @return Files/directories under the given dir that has absolute path
     * match the given java regex expressions.
     */
    fun collect(dir: File): MutableCollection<File> {
        return collect(ArrayList(), dir)
    }

    /**
     * @return Files/directories under the given dir.
     */
    fun <T : MutableCollection<File>> collect(ret: T, dir: File): T {
        return collect(ret, dir, FileFilter { true })
    }

    /**
     * @return Files/directories under the given dir that has absolute path
     * match the given java regex expressions.
     */
    fun collect(dir: File, include: String?): MutableCollection<File> {
        return collect(ArrayList(), dir, MatchUtil.compile1(include), null)
    }

    /**
     * @return Files/directories under the given dir that has absolute path
     * match the given java regex expressions.
     */
    fun collect(dir: File, include: String?, exclude: String?): MutableCollection<File> {
        return collect(ArrayList(), dir, MatchUtil.compile1(include), MatchUtil.compile1(exclude))
    }

    fun <T : MutableCollection<File>> collect(ret: T, dir: File, include: Pattern?, exclude: Pattern?): T {
        collect(ret, dir, FileFilter { file ->
            MatchUtil.match(file.absolutePath, include, exclude)
        })
        return ret
    }

    /**
     * @return Files/directories under the given dir that match the given java regex expressions.
     */
    fun collect(dir: File, includes: Array<String>, excludes: Array<String>): MutableCollection<File> {
        return collect(dir, MatchUtil.compile(*includes), MatchUtil.compile(*excludes))
    }

    /**
     * @return Files/directories under the given dir that match the given java regex expressions.
     */
    fun collect(dir: File, includes: Iterable<Pattern>?, excludes: Iterable<Pattern>?): MutableCollection<File> {
        return collect(ArrayList(), dir, includes, excludes)
    }

    /**
     * @return Files/directories under the given dir that match the given java regex expressions.
     */
    fun <T : MutableCollection<File>> collect(ret: T, dir: File, includes: Iterable<Pattern>?, excludes: Iterable<Pattern>?): T {
        collect(ret, dir, FileFilter { file ->
            MatchUtil.match(file.absolutePath, includes, excludes)
        })
        return ret
    }

    /**
     * @return Files/directories under the given dir that are accepted by the filter.
     * Recurse regardless directory is accepted by the filter.
     */
    fun collect(dir: File, filter: FileFilter?): MutableCollection<File> {
        return collect(ArrayList(), dir, filter)
    }

    fun <T : MutableCollection<File>> collect(ret: T, dir: File, filter: FileFilter?): T {
        val callback: Fun11<File, Boolean>? = if (filter == null) {
            null
        } else {
            { filter.accept(it) }
        }
        return collect(ret, dir, callback)
    }

    /**
     * @return Files/directories under the given dir that are accepted by the filter.
     * Recurse regardless directory is accepted by the filter.
     */
    fun <T : MutableCollection<File>> collect(ret: T, dir: File, accept: Fun11<File, Boolean>? = null): T {
        scan(dir) {
            if (accept == null || accept(it)) {
                ret.add(it)
            }
            it.isDirectory
        }
        return ret
    }

    fun scan(base: File, callback: FileFilter) {
        scan(base) { file -> callback.accept(file) }
    }

    /**
     * Similar to findRecursive() but let file filter decide which file/directory to be returned.
     * Also if filter.accept() return true for a directory, it recurse into the directory.
     * Otherwise, the directory and everything under it are ignored.
     */
    fun scan(dir: File, callback: Fun11<File, Boolean>) {
        require(dir.isDirectory) { "Expecting directory: $dir" }
        for (name in list(dir)) {
            val file = File(dir, name)
            if (callback(file) && file.isDirectory) {
                scan(file, callback)
            }
        }
    }

    @Throws(Exception::class)
    fun scan(dir: File, dirpath: String, callback: FileRpathFilter) {
        require(dir.isDirectory) { "Expecting directory: $dir" }
        return scan(dir, dirpath) { file, rpath ->
            callback.accept(file, rpath)
        }
    }

    @Throws(Exception::class)
    fun scan(dir: File, dirpath: String, callback: Fun21<File, String, Boolean>) {
        for (name in list(dir)) {
            val file = File(dir, name)
            val rpath = if (dirpath.isEmpty()) name else dirpath + File.separatorChar + name
            if (callback(file, rpath) && file.isDirectory) {
                scan(file, rpath, callback)
            }
        }
    }

    fun walkPostOrder(dir: File, dirpath: String, callback: ICallback2<File, String>) {
        walkPostOrder(dir, dirpath) { file: File, rpath: String -> callback.callback(file, rpath) }
    }

    fun walkPostOrder(dir: File, dirpath: String, callback: Fun20<File, String>) {
        for (name in list(dir)) {
            val file = File(dir, name)
            val rpath = if (dirpath.isEmpty()) name else dirpath + File.separatorChar + name
            if (file.isDirectory) {
                walkPostOrder(file, rpath, callback)
            }
            callback(file, rpath)
        }
    }

    @Throws(Exception::class)
    fun walkPostOrderX(dir: File, dirpath: String, callback: ICallback2X<File, String>) {
        walkPostOrderX(dir, dirpath) { file, rpath -> callback.callback(file, rpath) }
    }

    @Throws(Exception::class)
    fun walkPostOrderX(dir: File, dirpath: String, callback: Fun20<File, String>) {
        for (name in list(dir)) {
            val file = File(dir, name)
            val rpath = if (dirpath.isEmpty()) name else dirpath + File.separatorChar + name
            if (file.isDirectory) {
                walkPostOrderX(file, rpath, callback)
            }
            callback(file, rpath)
        }
    }

    fun rmdirRecursive(dir: File, filter: FileFilter?, keeproot: Boolean): Int {
        var count = 0
        for (name in list(dir)) {
            val path = File(dir, name)
            if (filter != null && !filter.accept(path)) {
                continue
            }
            if (path.isDirectory) {
                count += rmdirRecursive(path, filter, false)
            }
            if (path.delete()) {
                ++count
            }
        }
        if (!keeproot && dir.delete()) {
            ++count
        }
        return count
    }

    fun latest(vararg files: File): File? {
        var ret: File? = null
        var time: Long = 0
        for (file in files) {
            val t = file.lastModified()
            if (t > time) {
                time = t
                ret = file
            }
        }
        return ret
    }

    fun latest(files: Collection<File>): File? {
        var ret: File? = null
        var time: Long = 0
        for (file in files) {
            val t = file.lastModified()
            if (t > time) {
                time = t
                ret = file
            }
        }
        return ret
    }

    /**
     * Sort the files in place, in lexical order, and return the last file.
     */
    fun last(vararg files: File): File? {
        val len = files.size
        if (len > 0) {
            Arrays.sort(files)
            return files[len - 1]
        }
        return null
    }

    /**
     * Sort the files in place, in lexical order, and return the last file.
     */
    fun last(files: MutableList<File>): File? {
        val size = files.size
        if (size > 0) {
            files.sort()
            return files[size - 1]
        }
        return null
    }

    /**
     * Sort the files in place, in lexical order, and return the last file.
     */
    fun first(vararg files: File): File? {
        if (files.isNotEmpty()) {
            Arrays.sort(files)
            return files[0]
        }
        return null
    }

    /**
     * Sort the files in place, in lexical order, and return the last file.
     */
    fun first(files: MutableList<File>): File? {
        if (files.size > 0) {
            files.sort()
            return files[0]
        }
        return null
    }

    /**
     * @param regex java.util.regex pattern.
     * @return The last file, sorted in alphnumeric order, in the given directory
     * with simple filename that match the given java regex.
     */
    fun last(dir: File, regex: String?): File? {
        val pat = MatchUtil.compile1(regex)
        val ret = ArrayList<File>()
        list(ret, dir, pat, null)
        return last(ret)
    }

    fun lastRecursive(dir: File, regex: String?): File? {
        val ret: MutableList<File> = ArrayList()
        collect(ret, dir, MatchUtil.compile1(regex), null)
        return last(ret)
    }

    /**
     * @param regex java.util.regex pattern.
     * @return True if the last file with simple name that match the given java regex,
     * sorted in alphnumeric order exists and added to the ret collection, false if no file added.
     */
    fun last(ret: MutableCollection<File?>, dir: File, regex: String?): Boolean {
        val file = last(dir, regex)
        if (file != null) {
            ret.add(file)
            return true
        }
        return false
    }

    /**
     * @param regex java.util.regex pattern.
     * @return The number of files in the given directory with simple filename that match the given java regex,
     * with group(1) sorted in ascending order
     */
    fun lastGroup(ret: MutableCollection<File>, dir: File, regex: String): Int {
        val pat = Pattern.compile(regex)
        val map = TreeMap<String, MutableCollection<File>>()
        for (name in list(dir)) {
            val m = pat.matcher(name)
            if (m.matches()) {
                StructUtil.addToCollection(map, m.group(1), File(dir, name))
            }
        }
        if (map.isEmpty()) {
            return 0
        }
        val a = map.lastEntry().value
        ret.addAll(a)
        return a.size
    }

    /**
     * @param regex java.util.regex pattern.
     * @return The last file in the given directory with simple filename that match the given java regex,
     * with group(1) sorted in ascending order
     */
    fun lastGroup1(dir: File, regex: String): File? {
        val pat = Pattern.compile(regex)
        val map = TreeMap<String, File>()
        for (name in list(dir)) {
            val m = pat.matcher(name)
            if (m.matches()) {
                map[m.group(1)] = File(dir, name)
            }
        }
        return if (map.isEmpty()) null else map.lastEntry().value
    }

    /**
     * @param regex java.util.regex pattern.
     * @return The last file in the given files with absolute path
     * that match the given java regex and have group(1) sorted in alphanumeric order.
     * null if not match found.
     */
    fun lastGroup1(files: Collection<File>, regex: String): File? {
        val pat = Pattern.compile(regex)
        val map = TreeMap<String, File>()
        for (file in files) {
            val m = pat.matcher(file.absolutePath)
            if (m.matches()) {
                map[m.group(1)] = file
            }
        }
        return if (map.isEmpty()) null else map.lastEntry().value
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(FileNotFoundException::class)
    fun bufferedInputStream(file: File): InputStream {
        return BufferedInputStream(FileInputStream(file))
    }

    @Throws(FileNotFoundException::class)
    fun bufferedOutputStream(file: File): OutputStream {
        return BufferedOutputStream(FileOutputStream(file))
    }

    /**
     * Open given path as InputStream or GZIPInputStream depends on file extension.
     * Return System.in if file is null.
     *
     * @throws IOException if open failed. Any opened stream is closed before return.
     */
    @Throws(IOException::class)
    fun openInputOrGzipStream(file: File?): InputStream {
        if (file == null) {
            return System.`in`
        }
        var input = bufferedInputStream(file)
        val name = file.name
        val len = name.length
        if (len > 3 && name.substring(len - 3, len).equals(".gz", ignoreCase = true)) {
            input = try {
                GZIPInputStream(input)
            } catch (e: IOException) {
                close(input)
                throw e
            }
        }
        return input
    }

    /**
     * Open given path as InputStream or GZIPInputStream depends on file extension.
     *
     * @throws IOException if open failed. Any opened stream is closed before return.
     */
    @Throws(IOException::class)
    fun openInputOrGzipReader(file: File?): Reader {
        return InputStreamReader(openInputOrGzipStream(file))
    }
    /**
     * Open given path as OutputStream or GZIPOutputStream depends on file extension.
     *
     * @throws IOException if open failed. Any opened stream is closed before return.
     */
    @Throws(IOException::class)
    fun openOutputOrGzipStream(file: File?): OutputStream {
        if (file == null) {
            return System.out
        }
        var os = bufferedOutputStream(file)
        val name = file.name
        val len = name.length
        if (len > 3 && name.substring(len - 3, len).equals(".gz", ignoreCase = true)) {
            os = try {
                GZIPOutputStream(os)
            } catch (e: IOException) {
                close(os)
                throw e
            }
        }
        return os
    }
    /**
     * Open given path as PrintWriter depends on file extension.
     *
     * @throws IOException if open failed. Any opened stream is closed before return.
     */
    @Throws(IOException::class)
    fun openOutputOrGzipWriter(file: File?): PrintWriter {
        return PrintWriter(openOutputOrGzipStream(file))
    }

    @Throws(IOException::class)
    fun openOutputOrGzipPrintStream(file: File?): PrintStream {
        return PrintStream(openOutputOrGzipStream(file))
    }
    /**
     * Wrap exception to added zipfile name to error message.
     */
    @Throws(IOException::class)
    fun openZip(zip: File): ZipFile {
        return try {
            ZipFile(zip)
        } catch (e: IOException) {
            throw IOException("ERROR: Opening zip file: $zip", e)
        }
    }

    /**
     * Flush Flushable, ignore IOException.
     */
    fun flush(s: Flushable?) {
        if (s == null) {
            return
        }
        try {
            s.flush()
        } catch (e: IOException) {
        }
    }

    /**
     * Close I/O stream, ignore IOException.
     */
    fun close(s: Closeable?) {
        if (s == null) {
            return
        }
        try {
            s.close()
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    /**
     * Close input, ignore IOException.
     */
    fun close(s: AutoCloseable?) {
        if (s == null) {
            return
        }
        try {
            s.close()
        } catch (e: Exception) {
        }
    }

    /**
     * If path is not null, close output stream. Otherwise just flush.
     * Ignore IOException in both clase.
     */
    fun close(s: OutputStream?, path: String?) {
        if (s == null) {
            return
        }
        try {
            if (path == null) {
                s.flush()
                return
            }
            s.close()
        } catch (e: IOException) {
        }
    }

    /**
     * If path is not null, close writer. Otherwise just flush.
     * Ignore IOException in both clase.
     */
    fun close(s: Writer?, path: String?) {
        if (s == null) {
            return
        }
        try {
            if (path == null) {
                s.flush()
                return
            }
            s.close()
        } catch (e: IOException) {
        }
    }

    fun close(s: Closeable?, log: PrintStream?) {
        if (s == null) {
            return
        }
        try {
            s.close()
        } catch (e: IOException) {
        }
    }

    fun close(f: ZipFile?) {
        if (f == null) {
            return
        }
        try {
            f.close()
        } catch (e: IOException) {
        }
    }

    fun close(f: ZipFile?, log: PrintStream?) {
        if (f == null) {
            return
        }
        try {
            f.close()
        } catch (e: IOException) {
        }
    }

    /**
     * Do File.delete(), ignoring failure.
     */
    fun delete(file: File?) {
        if (file != null && !file.delete()) {
        }
    }

    /**
     * Do File.delete(), ignoring failure.
     */
    fun delete(vararg files: File) {
        for (file in files) {
            if (file != null && !file.delete()) {
            }
        }
    }

    /**
     * Rename file, ignore failure.
     */
    fun renameTo(to: File, from: File?) {
        if (from != null && !from.renameTo(to)) {
        }
    }

    /**
     * Update timestamp, ignore failure.
     */
    fun setLastModified(file: File?, timestamp: Long) {
        if (file != null && !file.setLastModified(timestamp)) {
        }
    }

    fun setReadable(file: File?, b: Boolean, owneronly: Boolean) {
        if (file != null && !file.setReadable(b, owneronly)) {
        }
    }

    fun setWritable(file: File?, b: Boolean, owneronly: Boolean) {
        if (file != null && !file.setWritable(b, owneronly)) {
        }
    }

    @kotlin.jvm.JvmStatic
    fun setOwnerReadWriteOnly(file: File): Boolean {
        return (file.setReadable(false, false) && file.setReadable(true, true)
                && file.setWritable(false, false) && file.setWritable(true, true))
    }

    fun delete(file: File?, err: PrintStream) {
        if (file != null && file.exists() && !file.delete()) {
            err.println("ERROR: failed to delete file: $file")
        }
    }

    @Throws(IOException::class)
    fun readFully(ret: CharArray, r: Reader): Int {
        return readFully(ret, 0, ret.size, r)
    }

    /**
     * @return Number of byte read. Note that this return 0..len-1 instead of -1 on end of file.
     */
    @Throws(IOException::class)
    fun readFully(ret: CharArray?, offset: Int, len: Int, r: Reader): Int {
        var n = 0
        while (n < len) {
            val c = r.read(ret, offset + n, len - n)
            if (c < 0) {
                break
            }
            n += c
        }
        return n
    }

    /**
     * Read input file as raw bytes.
     */
    @Throws(IOException::class)
    fun asBytes(file: File): ByteArray {
        return bufferedInputStream(file).use {
            asBytes(it)
        }
    }

    @Throws(IOException::class)
    fun asBytes(input: InputStream): ByteArray {
        val list: MutableList<ByteArray> = ArrayList()
        var size = asBytes(list, input)
        val ret = ByteArray(size)
        var offset = 0
        for (a in list) {
            System.arraycopy(a, 0, ret, offset, if (size > BUFSIZE) BUFSIZE else size)
            offset += BUFSIZE
            size -= BUFSIZE
        }
        return ret
    }

    /**
     * Read file content as raw bytes regardless of file format,
     * ie. for .gz file, read compressed data.
     */
    @Throws(IOException::class)
    fun asBytes(ret: ByteArray, file: File): Int {
        var input: InputStream? = null
        return try {
            input = bufferedInputStream(file)
            asBytes(ret, input)
        } finally {
            close(input)
        }
    }

    @Throws(IOException::class)
    fun asBytes(ret: ByteArray, input: InputStream): Int {
        return ByteIOUtil.readWhilePossible(input, ret)
    }

    /**
     * Read file content as characters regardless of file format.
     * To read .gz file, use ungzChars() instead.
     */
    @Throws(IOException::class)
    fun asChars(file: File): CharArray {
        return bufferedReader(file).use {
            asChars(it)
        }
    }

    /**
     * Read file content as characters in the given charset regardless of file format.
     * To read .gz file, use ungzChars() instead.
     */
    @Throws(IOException::class)
    fun asChars(file: File, charset: Charset): CharArray {
        return bufferedReader(file, charset).use {
            asChars(it)
        }
    }

    @Throws(IOException::class)
    fun asChars(input: Reader): CharArray {
        val list = ArrayList<CharArray>()
        var size = asChars(list, input)
        val ret = CharArray(size)
        var offset = 0
        for (a in list) {
            System.arraycopy(a, 0, ret, offset, if (size > BUFSIZE) BUFSIZE else size)
            offset += BUFSIZE
            size -= BUFSIZE
        }
        return ret
    }

    @Throws(IOException::class)
    fun asBytes(r: FileChannel): ByteArray {
        val size = r.size()
        if (size > Int.MAX_VALUE) {
            throw IOException("ERROR: Unable to allocate buffer of size: $size")
        }
        val ret = ByteArray(size.toInt())
        val b = ByteBuffer.wrap(ret)
        while (r.read(b) != -1) {
            if (b.position() >= size) {
                break
            }
        }
        return ret
    }

    @Throws(IOException::class)
    fun asByteBuffer(r: FileChannel): ByteBuffer {
        val size = r.size()
        if (size > Int.MAX_VALUE) {
            throw IOException("ERROR: Unable to allocate buffer of size: $size")
        }
        val ret = ByteBuffer.allocate(size.toInt())
        while (r.read(ret) != -1) {
            if (ret.position() >= size) {
                break
            }
        }
        return ret
    }

    @Throws(IOException::class)
    fun asCharBuffer(r: FileChannel): CharBuffer {
        return asCharBuffer(r, Charset.forName("UTF-8"))
    }

    @Throws(IOException::class)
    fun asCharBuffer(r: FileChannel, charset: Charset): CharBuffer {
        val size = r.size()
        if (size > Int.MAX_VALUE) {
            throw IOException("ERROR: Unable to allocate buffer of size: $size")
        }
        val ret = ByteBuffer.allocate(size.toInt())
        while (r.read(ret) != -1) {
            if (ret.position() >= size) {
                break
            }
        }
        ret.rewind()
        return charset.decode(ret)
    }

    @Throws(IOException::class)
    fun asString(u: URL): String {
        return asString(u, Charset.defaultCharset())
    }

    @Throws(IOException::class)
    fun asString(u: URL, charset: Charset = TextUtil.UTF8()): String {
        return InputStreamReader(BufferedInputStream(u.openStream()), charset).use {
            String(asChars(it))
        }
    }

    @Throws(IOException::class)
    fun asString(file: File): String {
        return String(asChars(file))
    }

    @Throws(IOException::class)
    fun asString(file: File, charset: Charset): String {
        return String(asChars(file, charset))
    }

    @kotlin.jvm.JvmStatic
    @Throws(IOException::class)
    fun asString(input: Reader): String {
        return String(asChars(input))
    }

    @Throws(IOException::class)
    fun asStrings(ret: MutableCollection<String>, charset: Charset, file: File): Collection<String> {
        return bufferedReader(file, charset).use {
            asStrings(ret, it)
        }
    }

    /**
     * Read each input line as a String, without line separators.
     */
    @Throws(IOException::class)
    fun <T : MutableCollection<String>> asStrings(ret: T, r: Reader): T {
        val br = BufferedReader(r)
        while (true) {
            val line = br.readLine() ?: break
            ret.add(line)
        }
        return ret
    }

    @Throws(IOException::class)
    fun asTextRange(file: File, charset: Charset? = null): ICharSequence {
        return bufferedReader(file, charset).use {
            asTextRange(it)
        }
    }

    @Throws(IOException::class)
    fun asTextRange(input: Reader): ICharSequence {
        val ret = ChunkedCharBuffer(BUFSIZE)
        var b = CharArray(BUFSIZE)
        var start = 0
        var n: Int
        while (input.read(b, start, BUFSIZE - start).also { n = it } != -1) {
            start += n
            if (start == BUFSIZE) {
                ret.add(b)
                b = CharArray(BUFSIZE)
                start = 0
            }
        }
        if (start > 0) {
            val a = CharArray(start)
            System.arraycopy(b, 0, a, 0, start)
            ret.add(a)
        }
        return ret
    }

    @Throws(IOException::class)
    fun asStringList(file: File, charset: Charset? = null): List<String> {
        val ret = ArrayList<String>(128)
        return bufferedReader(file, charset).use {
            asStrings(ret, it)
        }
    }

    @Throws(IOException::class)
    fun asStringList(r: Reader): List<String> {
        val ret = ArrayList<String>(128)
        return asStrings(ret, r)
    }

    @Throws(IOException::class)
    fun getResourceAsString(resource: String): String {
        var `is` = FileUtil::class.java.getResourceAsStream(resource) ?: throw IOException("Resource not exists: $resource")
        var r: Reader? = null
        return try {
            `is` = BufferedInputStream(`is`)
            r = InputStreamReader(`is`)
            asString(r)
        } finally {
            close(r)
            close(`is`)
        }
    }

    @Throws(IOException::class)
    fun transform(file: File, transformer: IStringTransformer) {
        writeFile(file, false, transformer.transform(asString(file)) ?: "")
    }

    /**
     * Similar to asBytes() but if file has .gz extension, read uncompressed content instead.
     */
    @Throws(IOException::class)
    fun ungzBytes(file: File?): ByteArray {
        val `is` = openInputOrGzipStream(file)
        return try {
            asBytes(`is`)
        } finally {
            close(`is`)
        }
    }

    /**
     * Similar to asChars() but if file has .gz extension, read uncompressed content instead.
     */
    @Throws(IOException::class)
    fun ungzChars(file: File?): CharArray {
        val r = openInputOrGzipReader(file)
        return try {
            asChars(r)
        } finally {
            close(r)
        }
    }

    @Throws(IOException::class)
    fun unzip(outdir: File?, zipfile: File?, preserveTimestamp: Boolean = false) {
        var z: ZipFile? = null
        try {
            z = ZipFile(zipfile)
            for (e in IterableWrapper.wrap<ZipEntry>(z.entries())) {
                val rpath = e.name
                val dst = File(outdir, rpath)
                if (e.isDirectory) {
                    mkdirs(dst)
                    continue
                }
                mkparent(dst)
                var `is`: InputStream? = null
                try {
                    `is` = z.getInputStream(e)
                    copy(dst, `is`)
                } finally {
                    close(`is`)
                    if (preserveTimestamp) {
                        setLastModified(dst, e.time)
                    }
                }
            }
        } finally {
            close(z)
        }
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, content: ByteArray) {
        mkparent(file)
        bufferedOutputStream(file, append).use {
            it.write(content)
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, content: ByteArray, start: Int, end: Int) {
        mkparent(file)
        bufferedOutputStream(file, append).use {
            it.write(content, start, end - start)
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, content: CharArray) {
        writeFile(file, append, TextUtil.UTF8(), content)
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, charset: Charset, content: CharArray) {
        mkparent(file)
        bufferedWriter(file, append, charset).use {
            it.write(content)
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, vararg contents: CharArray) {
        writeFile(file, append, TextUtil.UTF8(), *contents)
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, charset: Charset, vararg contents: CharArray) {
        mkparent(file)
        bufferedWriter(file, append, charset).use {
            for (content in contents) {
                it.write(content)
            }
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, content: CharSequence) {
        writeFile(file, append, TextUtil.UTF8(), content)
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, charset: Charset, content: CharSequence) {
        mkparent(file)
        bufferedWriter(file, append, charset).use {
            it.append(content)
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, vararg contents: CharSequence) {
        writeFile(file, append, TextUtil.UTF8(), *contents)
    }

    @Throws(IOException::class)
    fun writeFile(file: File, append: Boolean, charset: Charset, vararg contents: CharSequence) {
        mkparent(file)
        bufferedWriter(file, append, charset).use {
            for (content in contents) {
                it.append(content)
            }
        }
    }

    @Throws(IOException::class)
    fun writeZip(zipfile: ZipOutputStream, rpath: String, charset: Charset = TextUtil.UTF8(), content: String) {
        zipfile.putNextEntry(ZipEntry(rpath))
        val data = charset.encode(content)
        zipfile.write(data.array(), data.position(), data.limit() - data.position())
        zipfile.closeEntry()
    }

    @Throws(IOException::class)
    fun writeZip(zipfile: ZipOutputStream, rpath: String, charset: Charset, content: CharArray) {
        zipfile.putNextEntry(ZipEntry(rpath))
        val data = charset.encode(CharBuffer.wrap(content))
        zipfile.write(data.array(), data.position(), data.limit() - data.position())
        zipfile.closeEntry()
    }

    @Throws(IOException::class)
    fun gzip(file: File) {
        gzip(File(file.absoluteFile.parentFile, file.name + ".gz"), file)
    }

    @Throws(IOException::class)
    fun gzip(outfile: File, infile: File) {
        if (outfile.exists()) {
            throw IOException("ERROR: outfile already exists, not overwriting: $outfile")
        }
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = bufferedInputStream(infile)
            os = bufferedOutputStream(outfile)
            os = GZIPOutputStream(os)
            copy(os, `is`)
        } catch (e: IOException) {
            close(os)
            delete(outfile)
            throw e
        } finally {
            close(os)
            close(`is`)
        }
    }

    ////////////////////////////////////////////////////////////////////

    /**
     * Copy all files under srcdir to under dstdir.
     */
    @Throws(IOException::class)
    fun copyto(dstdir: File, srcdir: File) {
        for (rpath in listRecursive(
                srcdir,
                false,
                FileFilter { file -> file.isFile })) {
            val dst = mkparent(dstdir, rpath)
            copy(dst, File(srcdir, rpath))
        }
    }

    @Throws(IOException::class)
    fun copyto(dstdir: File, dir: File, filter: FileFilter?) {
        for (rpath in listRecursive(dir, false, filter)) {
            val dst = mkparent(dstdir, rpath)
            copy(dst, File(dir, rpath))
        }
    }

    @Throws(IOException::class)
    fun copync(dstdir: File?, dir: File, filter: FileFilter?) {
        for (rpath in listRecursive(dir, false, filter)) {
            val dst = File(dstdir, rpath)
            if (!dst.exists()) {
                mkparent(dst)
                copy(dst, File(dir, rpath))
            }
        }
    }

    @Throws(IOException::class)
    fun copyto(dstdir: File, basedir: File?, dir: File, filter: FileFilter?) {
        for (rpath in listRecursive(dir, basedir, filter)) {
            val dst = mkparent(dstdir, rpath)
            copy(dst, File(basedir, rpath))
        }
    }

    @Throws(IOException::class)
    fun copync(dstdir: File?, basedir: File?, dir: File, filter: FileFilter?) {
        for (rpath in listRecursive(dir, basedir, filter)) {
            val dst = File(dstdir, rpath)
            if (!dst.exists()) {
                mkparent(dst)
                copy(dst, File(basedir, rpath))
            }
        }
    }

    @Throws(IOException::class)
    fun copyflat(dstdir: File?, dir: File, filter: FileFilter?) {
        for (src in collect(dir, filter)) {
            copy(File(dstdir, src.name), src)
        }
    }

    @Throws(IOException::class)
    fun copy(dst: File, src: File) {
        bufferedInputStream(src).use { input ->
            bufferedOutputStream(dst).use { output ->
                copy(output, input)
            }
        }
    }

    @Throws(IOException::class)
    fun copy(dst: File, src: InputStream) {
        copy(dst, false, src)
    }

    @Throws(IOException::class)
    fun copy(dst: File, append: Boolean, src: InputStream) {
        bufferedOutputStream(dst, append).use {
            copy(it, src)
        }
    }

    @Throws(IOException::class)
    fun copy(dst: OutputStream, src: InputStream) {
        copy(BUFSIZE, dst, src)
    }

    @Throws(IOException::class)
    fun copy(bufsize: Int, output: OutputStream, input: InputStream) {
        val b = ByteArray(bufsize)
        while (true) {
            val n = ByteIOUtil.readWhilePossible(input, b)
            if (n > 0) {
                output.write(b, 0, n)
            }
            if (n < b.size) {
                break
            }
        }
    }

    @Throws(IOException::class)
    fun copy(output: OutputStream, input: InputStream, length: Long) {
        copy(BUFSIZE, output, input, length)
    }

    @Throws(IOException::class)
    fun copy(bufsize: Int, output: OutputStream, input: InputStream, length: Long) {
        var remaining = length
        val b = ByteArray(bufsize)
        while (remaining > 0) {
            val len = min(bufsize, remaining)
            val n = ByteIOUtil.readWhilePossible(input, b, 0, len)
            if (n > 0) {
                output.write(b, 0, n)
                remaining -= n.toLong()
            }
            if (n < len) {
                break
            }
        }
    }

    private fun min(bufsize: Int, length: Long): Int {
        return if (length >= bufsize) bufsize else length.toInt()
    }

    @Throws(IOException::class)
    fun copy(output: File, input: Reader) {
        copy(output, false, input)
    }

    @Throws(IOException::class)
    fun copy(output: File, append: Boolean, input: Reader) {
        bufferedWriter(output, append).use {
            copy(it, input)
        }
    }

    @Throws(IOException::class)
    fun copy(output: Writer, input: Reader) {
        copy(BUFSIZE, output, input)
    }

    @Throws(IOException::class)
    fun copy(bufsize: Int, output: Writer, input: Reader) {
        val b = CharArray(bufsize)
        while (true) {
            val n = readFully(b, input)
            if (n > 0) {
                output.write(b, 0, n)
            }
            if (n < bufsize) {
                break
            }
        }
    }

    @Throws(IOException::class)
    fun compare(a: File, b: File): Boolean {
        if (a.length() != b.length()) return false
        bufferedInputStream(a).use { sa ->
            bufferedInputStream(b).use { sb ->
                return compare(sa, sb)
            }
        }
    }

    @Throws(IOException::class)
    fun compare(a: InputStream, b: InputStream): Boolean {
        val inputa = BufferedInputStream(a)
        val inputb = BufferedInputStream(b)
        val len = BUFSIZE
        val bufa = ByteArray(len)
        val bufb = ByteArray(len)
        do {
            val counta = ByteIOUtil.readWhilePossible(inputa, bufa)
            val countb = ByteIOUtil.readWhilePossible(inputb, bufb)
            if (counta < len || countb < len) {
                return counta == countb && ArrayUtil.compare(bufa, 0, bufb, 0, counta)
            }
            if (!ArrayUtil.compare(bufa, 0, bufb, 0, len)) {
                return false
            }
        } while (true)
    }

    fun rename(from: File, to: File) {
        if (!from.renameTo(to)) {
            throw RuntimeException("ERROR: Failed to rename $from to $to")
        }
    }

    //#BEGIN NOTE
//# Remove dependency on IProgressListener
//#END NOTE
////////////////////////////////////////////////////////////////////
    @Throws(IOException::class)
    fun backupFile(file: File, bakname: String): File? {
        if (file.exists()) {
            val bak = File(file.absoluteFile.parentFile, bakname)
            if (bak.exists() && !bak.delete()) {
                throw IOException("ERROR: Failed to delete file: $bak")
            }
            if (!file.renameTo(bak)) {
                throw IOException("ERROR: Failed to rename file to: $bak")
            }
            return bak
        }
        return null
    }

    @Throws(IOException::class)
    fun backupAndWriteFile(file: File, bakname: String, charset: Charset = TextUtil.UTF8(), content: String) {
        val bak = backupFile(file, bakname)
        try {
            writeFile(file, false, charset, content)
        } catch (e: IOException) {
            if (bak != null) {
                delete(file)
                renameTo(file, bak)
            }
            throw e
        }
    }

    /**
     * @return true if filea differ from fileb.
     */
    fun diff(filea: File?, fileb: File?): Boolean {
        var diff = true
        openInputOrGzipStream(filea).use { ia ->
            openInputOrGzipStream(fileb).use { ib ->
                try {
                    while (true) {
                        val a = ia.read()
                        if (a != ib.read()) break
                        if (a == -1) {
                            diff = false
                            break
                        }
                    }
                } catch (e: IOException) {
                    diff = true
                }
            }
        }
        return diff
    }

    /**
     * @return true if any file differ or exists in only one of the directories.
     */
    fun diffRecursive(dira: File, dirb: File): Boolean {
        return diffRecursive(null, null, null, null, dira, dirb)
    }

    /**
     * @return true if any file differ or exists in only one of the directories.
     */
    fun diffRecursive(
            diffs: MutableSet<String>?,
            aonly: MutableSet<String>?,
            bonly: MutableSet<String>?,
            same: MutableSet<String>?,
            dira: File,
            dirb: File
    ): Boolean {
        val pathsa: MutableSet<String> = TreeSet()
        val pathsb: MutableSet<String> = TreeSet()
        listRecursive(pathsa, dira, dira, fileFilter)
        listRecursive(pathsb, dirb, dirb, fileFilter)
        var diff = false
        for (rpath in pathsa) {
            if (!pathsb.remove(rpath)) {
                diff = true
                aonly?.add(rpath)
                continue
            }
            val filea = File(dira, rpath)
            val fileb = File(dirb, rpath)
            if (diff(filea, fileb)) {
                diff = true
                diffs?.add(rpath)
                continue
            }
            same?.add(rpath)
        }
        bonly?.addAll(pathsb)
        return diff || pathsb.size > 0
    }

    ////////////////////////////////////////////////////////////////////

    val fileComparator: Comparator<File?>
        get() = FileComparator.singleton

    val fileIgnorecaseComparator: Comparator<File?>
        get() = FileIgnorecaseComparator.singleton

    val reversedFileComparator: Comparator<File?>
        get() = ReversedComparator(FileComparator.singleton)

    val reversedFileIgnorecaseComparator: Comparator<File?>
        get() = ReversedComparator(FileIgnorecaseComparator.singleton)

    ////////////////////////////////////////////////////////////////////

    /**
     * Write given data to the given file using the given SafeWriter.
     * In any case, if write failed, the original file if exists is kept intact.
     */
    fun <T> safeWrite(writer: SafeWriter<T>, file: File, data: T): Boolean {
        val tmpfile = File.createTempFile(file.name, ".tmp", file.parentFile)
        try {
            tmpfile.outputStream().buffered().use {
                writer.write(it, data)
            }
        } catch (e: Exception) {
            tmpfile.delete()
            return false
        }
        return (!file.exists() || file.delete()) && tmpfile.renameTo(file)
    }

    ////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    private fun asBytes(list: MutableList<ByteArray>, input: InputStream): Int {
        var b = ByteArray(BUFSIZE)
        var start = 0
        var n: Int
        while (input.read(b, start, BUFSIZE - start).also { n = it } != -1) {
            start += n
            if (start == BUFSIZE) {
                list.add(b)
                b = ByteArray(BUFSIZE)
                start = 0
            }
        }
        val size = list.size * BUFSIZE + start
        if (start > 0) {
            list.add(b)
        }
        return size
    }

    /**
     * @return The total size in characters.
     */
    @Throws(IOException::class)
    private fun asChars(ret: MutableList<CharArray>, input: Reader): Int {
        var b = CharArray(BUFSIZE)
        var start = 0
        var n: Int
        while (input.read(b, start, BUFSIZE - start).also { n = it } != -1) {
            start += n
            if (start == BUFSIZE) {
                ret.add(b)
                b = CharArray(BUFSIZE)
                start = 0
            }
        }
        val size = ret.size * BUFSIZE + start
        if (start > 0) {
            ret.add(b)
        }
        return size
    }

    fun list(dir: File): Array<String> {
        val ret = dir.list()
        return ret ?: Empty.STRING_ARRAY
    }

    @Throws(FileNotFoundException::class)
    private fun bufferedOutputStream(file: File, append: Boolean): OutputStream {
        return BufferedOutputStream(FileOutputStream(file, append))
    }

    @Throws(FileNotFoundException::class)
    private fun bufferedReader(file: File, charset: Charset? = null): Reader {
        return InputStreamReader(bufferedInputStream(file), charset ?: TextUtil.UTF8())
    }

    @Throws(FileNotFoundException::class)
    private fun bufferedWriter(file: File, append: Boolean, charset: Charset? = null): Writer {
        return OutputStreamWriter(bufferedOutputStream(file, append), charset ?: TextUtil.UTF8())
    }

    ////////////////////////////////////////////////////////////////////

    interface SafeWriter<T> {
        @Throws(Exception::class)
        fun write(os: OutputStream?, data: T)
    }

    interface IStringTransformer {
        @Throws(IOException::class)
        fun transform(content: String?): String?
    }

    ////////////////////////////////////////////////////////////////////

    object FileComparator {
        val singleton = nullsFirst<File>(naturalOrder())
    }

    object ReverseFileComparator {
        val singleton = nullsLast<File>(reverseOrder())
    }

    object FileIgnorecaseComparator {
        val singleton = nullsFirst<File>(kotlin.Comparator { a, b ->
            a.absolutePath.compareTo(b.absolutePath, ignoreCase = true)
        })
    }

    object FileTimestampComparator {
        val singleton = nullsFirst<File>(compareBy { it.lastModified() })
    }

    object FilenameComparator {
        val singleton = nullsFirst<File>(compareBy { it.name })
    }

    class FileOnlyFilter : FileFilter {
        override fun accept(file: File): Boolean {
            return file.isFile
        }
    }

    class DirOnlyFilter : FileFilter {
        override fun accept(file: File): Boolean {
            return file.isDirectory
        }
    }

    class NotDirFilter : FileFilter {
        override fun accept(file: File): Boolean {
            return !file.isDirectory
        }
    }

    interface FileRpathFilter {
        @Throws(Exception::class)
        fun accept(file: File, rpath: String): Boolean
    }
}
