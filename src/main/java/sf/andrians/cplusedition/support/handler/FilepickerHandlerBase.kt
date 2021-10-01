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
package sf.andrians.cplusedition.support.handler

import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.stringListOrEmpty
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun11
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.util.struct.StructUtil.addToList
import sf.andrians.ancoreutil.util.text.TextUtil
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.*
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import java.io.File
import java.util.*

////////////////////////////////////////////////////////////////////////

abstract class FilepickerHandlerBase(
        protected val storage: IStorage
) : IFilepickerHandler {
    protected val rsrc = storage.rsrc

    ////////////////////////////////////////////////////////////////////////

    /// @param path A context relative path.
    /// @return  {
    ///   An.Key.fileinfo: {fileinfo},
    ///   An.Key.errors: Object,
    /// }
    @Throws(JSONException::class)
    fun actionFileInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val info = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        try {
            val json = FileInfoUtil.tojson(info)
            return rsrc.jsonObjectResult(Key.fileinfo, json)
        } catch (e: Exception) {
            return rsrc.jsonObjectError(R.string.CommandFailed)
        }

    }

    @Throws(JSONException::class)
    fun actionDirInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val info = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        if (!info.isDir) return rsrc.jsonObjectError(R.string.DestinationIsNotADirectory)
        var files1 = 0L
        var dirs1 = 0L
        var size1 = 0L
        var files = 0L
        var dirs = 0L
        var size = 0L
        fun count(dir: IFileInfo) {
            for (file in dir.readDir(ArrayList())) {
                if (file.isDir) {
                    ++dirs
                    count(file)
                } else {
                    ++files
                    size += file.stat()!!.length
                }
            }
        }
        for (file in info.readDir(ArrayList())) {
            if (file.isDir) {
                ++dirs1
                count(file)
            } else {
                ++files1
                size1 += file.stat()!!.length
            }
        }
        return JSONObject().put(Key.result, JSONArray()
                .put(files + files1).put(dirs + dirs1).put(size + size1).put(files1).put(dirs1).put(size1))
    }

    /***
     * @param cpath A context relative path, with or without leading /.
     * @return A json string {
     * An.Key.dirtree: [fileinfo]
     * An.Key.dirpath: [fileinfo]
     * An.Key.path: String
     * An.Key.filename: String
     * }
     * Where filename is "" if last segment of cpath is a directory, otherwise, it is filename of the file.
     * @since 1.7 Trailing / is now valid, it is invalid before 1.7.
     */
    @Throws(JSONException::class)
    fun actionListDir(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        return listdir(JSONObject(), cpath)
    }

    @Throws(JSONException::class)
    private fun listdir(ret: JSONObject, cpath: String?): JSONObject {
        val cleanpath = Support.getcleanrpath(cpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, "$cpath")
        if (cleanpath.isEmpty()) {
            ret.put(Key.filename, "")
            return listroots1(ret)
        }
        val dir = storage.fileInfo(cleanpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, "$cpath")
        val warns = JSONArray()
        val warnings = ArrayList<String>()
        val illegals = TreeSet<Int>()
        val dirpath = ret.putJSONArrayOrFail(Key.dirpath)
        FileInfoUtil.tojson(dirpath, warns, dir.root)
        var name = ""
        val segments = TextUtil.splitAll(dir.rpath, File.separatorChar)
        val len = segments.size
        var d: IFileInfo = dir.root
        for (i in 0 until len) {
            val segment = segments[i]
            if (segment.isNotEmpty()) {
                Support.sanitizeFilenameStrict1(warnings, rsrc, illegals, segment)
                if (illegals.size > 0) {
                    warnings.add(rsrc.get(R.string.InvalidFilenameCharacter_, Support.escIllegals(illegals)))
                }
                if (warnings.size > 0) {
                    name = segment
                    break
                }
                val dd = d.fileInfo(segment)
                val stat = dd.stat()
                if (stat != null && stat.isDir) {
                    d = dd
                    FileInfoUtil.tojson(dirpath, warns, d)
                    continue
                }
            }
            //// Only the last segment may be not a directory.
            if (i != len - 1) {
                warnings.add(rsrc.get(R.string.InvalidPath_, "$cpath"))
                break
            }
            name = segment
            break
        }
        listdirtree(ret, warns, d, name)
        for (s in warnings) warns.put(s)
        if (warns.length() > 0) ret.put(Key.warns, warns)
        return ret
    }

    @Throws(JSONException::class)
    fun actionMkdirs(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val ret = JSONObject()
        val warns = JSONArray()
        if (listdirpath(ret, warns, fileinfo) {
                    val stat = it.stat()
                    if (stat != null && !stat.isDir || stat == null && !it.mkdirs()) {
                        rsrc.get(R.string.CreateDirectoryFailed_, it.apath)
                    } else null
                }
        ) {
            listdirtree(ret, warns, fileinfo, null)
        }
        return ret
    }

    @Throws(JSONException::class)
    fun actionDelete(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val cleanrpath = Support.getcleanrpath(cpath)
        val fileinfo = storage.fileInfo(cleanrpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, cpath)
        val stat = fileinfo.stat() ?: return rsrc.jsonObjectError(R.string.NotFound_, cpath)
        if (FileInfoUtil.isRootOrUnderReadonlyRoot(fileinfo)) return rsrc.jsonObjectError(R.string.NotDeletable)
        if (stat.isDir && fileinfo.readDir(ArrayList()).size > 0) return rsrc.jsonObjectError(R.string.CannotDeleteNonEmptyDirectory_, cpath)
        if (!fileinfo.delete()) return rsrc.jsonObjectError(R.string.DeleteFailed_, cpath)
        val parent = fileinfo.parent ?: fileinfo.root
        return listdir(JSONObject(), parent, "")
    }

    @Throws(JSONException::class)
    fun actionRename(params: JSONObject): JSONObject {
        val path = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val newname = params.stringOrNull(Key.filename)
                ?: return rsrc.jsonObjectError(R.string.MissingParameter_, Key.filename)
        //#BEGIN NOTE Relax source path checking to allow rename invalid source path to a valid one.
        val cleanpath = Support.getcleanrpath(path) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, path)
        //#END NOTE
        val srcinfo = storage.fileInfo(cleanpath) ?: return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", path)
        val srcstat = srcinfo.stat() ?: return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", path)
        //// Cannot rename root, top directories and in readonly tree.
        if (cleanpath.isEmpty() || FileInfoUtil.isRootOrUnderReadonlyRoot(srcinfo)) {
            return rsrc.jsonObjectError(R.string.SourceNotDeletable, ": ", path)
        }
        val err = ArrayList<String>()
        if (Support.sanitizeFilenameStrict(err, rsrc, newname).size > 0) {
            return rsrc.jsonObjectError(err)
        }
        val isdir = srcstat.isDir
        val parent = srcinfo.parent ?: return rsrc.jsonObjectError(R.string.RenameFailed_, path)
        val dstinfo = parent.fileInfo(newname)
        if (dstinfo.exists) {
            return rsrc.jsonObjectError(R.string.DestinationAlreadyExists, ": ", newname)
        }
        try {
            srcinfo.content().renameTo(dstinfo, srcstat.lastModified)
        } catch (e: Exception) {
            return rsrc.jsonObjectError(e, R.string.RenameFailed_, "$path -> $newname")
        }
        val ret = JSONObject()
        if (isdir) {
            listdir(ret, dstinfo, "")
        } else {
            listdir(ret, parent, newname)
        }
        return ret
    }

    @Throws(JSONException::class)
    fun actionCopyInfo(params: JSONObject): JSONObject {
        val cut = params.optBoolean(Key.cut)
        val dst = params.stringOrNull(Key.dst)
                ?: return rsrc.jsonObjectError(R.string.MissingParameterDestinationPath)
        val src = params.stringOrNull(Key.src) ?: return rsrc.jsonObjectError(R.string.MissingParameterSourcePath)
        return CopyInfo.of(storage, cut, dst, src)
    }

    @Throws(JSONException::class)
    fun actionLocalImageInfos(params: JSONObject): JSONObject {
        val dir = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val dirinfo = storage.fileInfoAt(dir).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        if (dirinfo.stat().let { it == null || !it.isDir }) {
            return rsrc.jsonObjectError(R.string.InvalidPath_, dir)
        }
        val ret = JSONObject()
        val result = ret.putJSONArrayOrFail(Key.result)
        for (fileinfo in FileInfoUtil.filesByName(dirinfo)) {
            val name = fileinfo.name
            val mime = MimeUtil.imageMimeFromPath(name) ?: continue
            val stat = fileinfo.stat() ?: continue
            val modified = stat.lastModified
            val size = stat.length
            result.put(ImageUtil.localImageInfo(dirinfo.apath + "/" + fileinfo.name, name, -1, -1, mime, modified, size, null))
        }
        return ret
    }

    private fun _getthumbnailsize(params: JSONObject): Int {
        val size = params.optInt(Key.size)
        if (size > 0) return size
        return if (params.optInt(Key.type, MediaInfo.Thumbnail.Kind.MICRO_KIND) == MediaInfo.Thumbnail.Kind.MINI_KIND) {
            MediaInfo.Thumbnail.Mini.WIDTH
        } else MediaInfo.Thumbnail.Micro.WIDTH
    }

    @Throws(JSONException::class)
    fun actionLocalImageThumbnails(params: JSONObject, callback: IThumbnailCallback): JSONObject {
        val infos = params.optJSONArray(Key.infos) ?: return rsrc.jsonObjectError(R.string.NoImageInfoAvailable)
        val tsize = _getthumbnailsize(params)
        return ImageUtil.actionImageThumbnails(callback, rsrc, infos, tsize)
    }

    /// @param dirpath Relative path for dir in root.
    fun actionListRecursive(params: JSONObject): JSONObject {
        try {
            val path = params.getString(Key.path)
            val dirinfo = storage.fileInfoAt(path).let {
                it.first ?: return rsrc.jsonObjectError(it.second)
            }
            if (dirinfo.stat().let { it == null || !it.isDir }) {
                return rsrc.jsonObjectError(R.string.InvalidPath)
            }
            val ret = JSONObject()
            val dirtree = JSONArray()
            try {
                ret.put(Key.dirtree, dirtree)
            } catch (e: JSONException) {
            }
            listRecursive1(dirtree, "", dirinfo)
            return ret
        } catch (e: Exception) {
            return rsrc.jsonObjectError(R.string.CommandFailed)
        }
    }

    private fun listRecursive1(ret: JSONArray, dirpath: String, dirinfo: IFileInfo) {
        for (info in FileInfoUtil.filesByName(dirinfo)) {
            val rpath = TextUtil.joinRpath(dirpath, info.name)
            if (info.stat().let { it != null && it.isDir }) {
                listRecursive1(ret, rpath, info)
                continue
            }
            ret.put(rpath)
        }
    }

    /// @param { src: "A context relative path to basedir", rpaths: [string] }
    /// @return  {
    ///   An.Key.result: { rpath : fileinfo}, // Only for rpath that exists.
    ///   An.Key.errors: Object,
    /// }
    @Throws(JSONException::class)
    fun actionFileInfos(params: JSONObject): JSONObject {
        val dir = params.stringOrNull(Key.src) ?: return rsrc.jsonObjectError(R.string.MissingParameterSourcePath)
        val rpaths = params.stringListOrEmpty(Key.rpaths)
        val dirinfo = storage.fileInfoAt(dir).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val ret = JSONObject()
        for (rpath in rpaths) {
            val info = dirinfo.fileInfo(rpath)
            if (info.exists) {
                ret.put(rpath, info.toJSON())
            }
        }
        return rsrc.jsonObjectResult(Key.result, ret)
    }

    private class CopyInfo(
            private val res: IResUtil,
            private val cut: Boolean,
            private val dstinfo: IFileInfo,
            private val srcinfo: IFileInfo
    ) {
        private val overwriting = TreeMap<String, List<Boolean>>()
        private val copying = TreeMap<String, List<Boolean>>()
        private val notcopying = TreeMap<String, MutableList<String>>()
        fun info(): JSONObject {
            if (cut && FileInfoUtil.isRootOrUnderReadonlyRoot(srcinfo)) {
                return res.jsonObjectError(R.string.SourceNotDeletable, ": ", srcinfo.cpath)
            }
            if (!dstinfo.root.stat().writable) {
                return res.jsonObjectError(R.string.DestinationNotWritable, ": ", dstinfo.cpath)
            }
            val srcstat = srcinfo.stat()
                    ?: return res.jsonObjectError(R.string.SourceNotFound, ": ", srcinfo.cpath)
            if (!srcstat.isDir) {
                val name = srcinfo.name
                val dinfo = dstinfo.fileInfo(name)
                info1(dstinfo, dinfo.stat(), srcinfo, srcstat, name)
            } else {
                val files = FileInfoUtil.listFiles(srcinfo)
                if (cut && !srcstat.writable) {
                    return res.jsonObjectError(R.string.SourceNotDeletable)
                } else {
                    val dststat = dstinfo.stat()
                    if (dststat != null && dststat.isDir && !dststat.writable) {
                        return res.jsonObjectError(R.string.DestinationNotWritable)
                    }
                    copydir(dstinfo, "", files)
                }
            }
            return result()
        }

        private fun copydir(dparent: IFileInfo, dirrpath: String, sinfos: List<IFileInfo>) {
            for (sinfo in sinfos) {
                val sstat = sinfo.stat()
                val sname = sinfo.name
                val rpath = TextUtil.joinRpath(dirrpath, sname)
                if (sstat == null) {
                    addToList(notcopying, res.get(R.string.SourceNotFound), rpath)
                    continue
                }
                if (Support.sanitizeFilepath(ArrayList(), res, sname).size > 0) {
                    addToList(notcopying, res.get(R.string.SourceNotValid), rpath)
                    continue
                }
                val dinfo = dstinfo.fileInfo(rpath)
                val dstat = dinfo.stat()
                if (sstat.isDir) {
                    if (dstat == null) {
                        copying.put(rpath, listOf(sstat.isDir, sstat.isDir))
                    } else if (dstat.isDir) {
                        if (!dstat.writable) {
                            addToList(notcopying, res.get(R.string.DestinationNotWritable), rpath)
                        } else overwriting.put(rpath, listOf(sstat.isDir, dstat.isDir))
                    } else {
                        overwriting.put(rpath, listOf(sstat.isDir, dstat.isDir))
                    }
                } else {
                    info1(dparent, dstat, sinfo, sstat, rpath)
                }
            }
        }

        private fun info1(dparent: IFileInfo, dstat: IFileStat?, sinfo: IFileInfo, sstat: IFileStat, rpath: String) {
            if (Support.sanitizeFilepath(ArrayList(), res, sinfo.name).size > 0) {
                addToList(notcopying, res.get(R.string.SourceNotValid), rpath)
                return
            }
            if (dstat != null) {
                if (!dstat.writable) {
                    addToList(notcopying, res.get(R.string.DestinationNotWritable), rpath)
                } else {
                    overwriting.put(rpath, listOf(sstat.isDir, dstat.isDir))
                }
                return
            }
            val dparentstat = dparent.stat()
            if (dparentstat != null && !dparentstat.writable) {
                addToList(notcopying, res.get(R.string.DestinationNotWritable), rpath)
            } else {
                copying.put(rpath, listOf(sstat.isDir, sstat.isDir))
            }
        }

        /// Create result JSONObject string with file paths sorted.
        private fun result(): JSONObject {
            val ret = JSONObject()
            ret.put(Key.copying, JSONObject(copying as Map<*, *>))
            ret.put(Key.overwriting, JSONObject(overwriting as Map<*, *>))
            val n = ret.putJSONObjectOrFail(Key.notcopying)
            for ((key, value) in notcopying) {
                val a = n.putJSONArrayOrFail(key)
                for (s in TreeSet(value)) {
                    a.put(s)
                }
            }
            return ret
        }

        companion object {
            /// @return JSONObject result string for
            /// {
            ///     An.Key.copying: [],
            ///     An.Key.overwriting: [],
            ///     An.Key.notcopying: { reason: [] },
            ///     An.Key.errors: error
            /// }
            fun of(storage: IStorage, cut: Boolean, dstpath: String, srcpath: String): JSONObject {
                val res = storage.rsrc
                val srcinfo = storage.fileInfoAt(srcpath).first
                        ?: return res.jsonObjectError(R.string.SourceNotValid, ": ", srcpath)
                val dstinfo = storage.fileInfoAt(dstpath).first
                        ?: return res.jsonObjectError(R.string.DestinationNotValid, ": ", dstpath)
                return CopyInfo(res, cut, dstinfo, srcinfo).info()
            }
        }
    }

    @Throws(JSONException::class)
    fun actionCopy(params: JSONObject): JSONObject {
        val cut = params.optBoolean(Key.cut)
        val preservetimestamp = params.optBoolean(Key.timestamp)
        val dstdir = params.stringOrNull(Key.dst)
                ?: return rsrc.jsonObjectError(R.string.MissingParameterDestinationPath)
        val srcdir = params.stringOrNull(Key.src)
                ?: return rsrc.jsonObjectError(R.string.MissingParameterSourcePath)
        val rpaths = params.stringListOrEmpty(Key.rpaths)
        return CopyAction(this, cut, preservetimestamp, dstdir, srcdir, rpaths).copy()
    }

    internal inner class CopyAction(
            private val handler: FilepickerHandlerBase,
            private val cut: Boolean,
            private val preserveTimestamp: Boolean,
            private val dstDir: String,
            private val srcPath: String,
            private val rpaths: List<String>
    ) {
        private val warns = JSONArray()
        private val oks = JSONArray()

        @Throws(JSONException::class)
        fun copy(): JSONObject {
            val error = copy1()
            if (error != null) {
                return error
            }
            val ret = JSONObject()
            if (warns.length() > 0) {
                ret.put(Key.warns, warns)
            }
            ret.put(Key.result, oks)
            return handler.listdir(ret, dstDir)
        }

        /// @return A json error on error or nil if OK.
        private fun copy1(): JSONObject? {
            val dst = storage.fileInfoAt(dstDir).first ?: return rsrc.jsonObjectError(R.string.DestinationNotValid, ": ", dstDir)
            val src = storage.fileInfoAt(srcPath).first ?: return rsrc.jsonObjectError(R.string.SourceNotValid, ": ", srcPath)
            return copy2(dst, src)
        }

        private fun copy2(dst: IFileInfo, src: IFileInfo): JSONObject? {
            val srcstat = src.stat()
            if (srcstat == null || !srcstat.readable) return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", srcPath)
            val dststat = dst.stat()
            val errors = ArrayList<String>()
            if (srcstat.isDir) {
                if (dststat == null) {
                    if (!dst.mkdirs()) return rsrc.jsonObjectError(R.string.failedToCreateDirectory_, dstDir)
                } else if (!dststat.isDir) {
                    return rsrc.jsonObjectError(R.string.DestinationNotADirectory, ": ", dstDir)
                } else if (!dststat.writable) {
                    return rsrc.jsonObjectError(R.string.DestinationNotWritable, ":", dstDir)
                }
                dst.root.transaction {
                    val paths = if (rpaths.isEmpty()) src.readDir(ArrayList()).map { it.name } else rpaths
                    copydir1(errors, dst, src, paths)
                }
            } else {
                copyfile1(errors, dst, dststat, src, srcstat)
            }
            return if (errors.size > 0) rsrc.jsonObjectError(errors) else null
        }

        private fun copydir1(errors: MutableList<String>, dst: IFileInfo, src: IFileInfo, rpaths: List<String>) {
            for (rpath in rpaths) {
                val s = src.fileInfo(rpath)
                val d = dst.fileInfo(rpath)
                val sstat = s.stat() ?: continue // Should not happen
                val dstat = d.stat()
                if (dstat != null) {
                    if (sstat.isDir) {
                        if (!d.isDir && !d.delete()) {
                            errors.add(rsrc.jsonError(R.string.DeleteFailed_, rpath))
                            continue
                        }
                        if (!d.mkdirs()) {
                            errors.add(rsrc.jsonError(R.string.ErrorCreatingDirectory_, rpath))
                            continue
                        }
                        copydir1(errors, d, s, s.readDir(ArrayList()).map { it.name })
                        continue
                    }
                    if (!FileInfoUtil.deleteTree(d)) {
                        errors.add(rsrc.jsonError(R.string.DeleteFailed_, rpath))
                        continue
                    }
                    copy1(errors, d, s, sstat, src.name)
                    continue
                }
                if (sstat.isDir) {
                    if (!d.mkdirs()) {
                        errors.add(rsrc.jsonError(R.string.ErrorDeletingDirectory_, rpath))
                        continue
                    }
                    copydir1(errors, d, s, s.readDir(ArrayList()).map { it.name })
                    continue
                }
                copy1(errors, d, s, sstat, rpath)
            }
            if (cut) FileInfoUtil.deleteEmptySubtrees(src)
        }

        private fun copyfile1(errors: MutableList<String>, dst: IFileInfo, dststat: IFileStat?, src: IFileInfo, srcstat: IFileStat) {
            if (dststat != null && dststat.isDir) {
                copy1(errors, dst.fileInfo(src.name), src, srcstat, src.name)
            } else {
                copy1(errors, dst, src, srcstat, src.name)
            }
        }

        /// @return An error message or nil if copy OK.
        private fun copy1(errors: MutableList<String>, dst: IFileInfo, src: IFileInfo, srcstat: IFileStat?, srcrpath: String) {
            if (srcstat == null || !srcstat.readable) {
                errors.add(rsrc.get(R.string.SourceNotFound, ": ", srcrpath))
                return
            }
            if (!srcstat.isFile) {
                errors.add(rsrc.get(R.string.SourceNotAFile, ": ", srcrpath))
                return
            }
            val dstat = dst.stat()
            if (dstat != null && dstat.isDir) {
                errors.add(rsrc.get(R.string.DestinationMustNotBeADirectory, ": ", srcrpath))
                return
            }
            if (dst.apath == src.apath) {
                errors.add(rsrc.get(R.string.CopyToSelfIsNotAllowed, ": ", srcrpath))
                return
            }
            val dstext = Basepath.ext(dst.name)
            val srcext = Basepath.ext(src.name)
            if (!TextUtil.equals(dstext, srcext)) {
                errors.add(rsrc.get(R.string.ChangeFileExtIsNotAllowed, ": ", srcrpath))
                return
            }
            try {
                val timestamp = if (preserveTimestamp) srcstat.lastModified else System.currentTimeMillis()
                if (cut) src.content().moveTo(dst, timestamp)
                else src.content().copyTo(dst, timestamp)
                oks.put(srcrpath)
            } catch (e: Throwable) {
                
                errors.add(rsrc.get(R.string.CopyFailed_, srcrpath))
            }
        }
    }

    /**
     * Delete everything under the given directory but not the directory itthis.
     *
     * @since v1.7 It is now an error if path does not exists or not a directory.
     */
    @Throws(JSONException::class)
    fun actionDeleteDirSubtree(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val dir = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val dirstat = dir.stat() ?: return rsrc.jsonObjectError(R.string.NotFound_, cpath)
        if (!dirstat.isDir) return rsrc.jsonObjectError(R.string.DestinationNotADirectory, ": ", cpath)
        val warns = JSONArray()
        deletedirsubtree(warns, dir)
        val ret = JSONObject()
        if (warns.length() > 0) {
            ret.put(Key.warns, warns)
        }
        listdir(ret, dir, "")
        return ret
    }

    private fun deletedirsubtree(warns: JSONArray, dir: IFileInfo): Boolean {
        var ok = true
        dir.root.transaction {
            for (fileinfo in FileInfoUtil.listFiles(dir)) {
                val filestat = fileinfo.stat()!!
                if (filestat.isDir) {
                    if (!deletedirsubtree(warns, fileinfo)) {
                        ok = false
                        continue
                    }
                }
                if (!fileinfo.delete()) {
                    warns.put(rsrc.get(R.string.DeleteFailed_, fileinfo.apath))
                    ok = false
                    continue
                }
            }
        }
        return ok
    }

    @Throws(JSONException::class)
    fun actionDeleteAll(params: JSONObject): JSONObject {
        val src = params.stringOrNull(Key.src) ?: return rsrc.jsonObjectError(R.string.MissingParameterSourcePath)
        val rpaths = params.stringListOrEmpty(Key.rpaths)
        return deleteall0(src, rpaths)
    }

    @Throws(JSONException::class)
    private fun deleteall0(srcpath: String, rpaths: List<String>): JSONObject {
        val ret = JSONObject()
        val warns = JSONArray()
        val oks = ret.putJSONArrayOrFail(Key.result)
        deleteall1(warns, oks, srcpath, rpaths)?.let { errors -> return errors }
        if (warns.length() > 0) {
            ret.put(Key.warns, warns)
        }
        return listdir(ret, srcpath)
    }

    /// @return nil if OK, otherwise the json error.
    private fun deleteall1(warns: JSONArray, oks: JSONArray, srcpath: String, rpaths: List<String>): JSONObject? {
        val srcinfo = storage.fileInfoAt(srcpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val srcstat = srcinfo.stat()
        if (srcstat == null || !srcstat.readable) {
            return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", srcpath)
        }
        if (srcstat.isDir) {
            srcinfo.root.transaction {
                for (rpath in rpaths) {
                    val sinfo = srcinfo.fileInfo(rpath)
                    val msg = delete1(oks, sinfo, rpath)
                    if (msg != null) {
                        warns.put(msg)
                    }
                }
            }
        } else {
            val rpath = srcinfo.name
            val msg = delete1(oks, srcinfo, rpath)
            if (msg != null) {
                return HandlerUtil.jsonObjectError(msg)
            }
        }
        return null
    }

    /// @return nil if OK, otherwise the error message
    private fun delete1(oks: JSONArray, sinfo: IFileInfo, rpath: String): String? {
        val sstat = sinfo.stat()
        if (sstat == null || !sstat.readable) {
            return rsrc.get(R.string.NotFound_, rpath)
        }
        if (sstat.isDir) {
            val dontcare: MutableList<IFileInfo> = ArrayList()
            sinfo.readDir(dontcare)
            if (dontcare.size > 0) {
                return rsrc.get(R.string.CannotDeleteNonEmptyDirectory_, rpath)
            }
        }
        if (!sinfo.delete()) {
            return rsrc.get(R.string.DeleteFailed_, rpath)
        }
        oks.put(rpath)
        return null
    }

    @Throws(JSONException::class)
    fun actionDeleteInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        return DeleteInfo.of(storage, cpath)
    }

    internal class DeleteInfo(
            val storage: IStorage,
            val src: String,
            val srcinfo: IFileInfo,
            val srcstat: IFileStat
    ) {
        val deleting: MutableList<String> = ArrayList()
        val notdeleting: MutableMap<String, MutableList<String>> = TreeMap()

        @Throws(JSONException::class)
        private fun info(): JSONObject {
            if (!srcstat.isDir) {
                val parent = srcinfo.parent
                if (parent == null || parent.stat()?.writable != true) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                info1(srcstat, srcinfo.name)
                return result()
            }
            val srcinfos = FileInfoUtil.listFiles(srcinfo)
            var hasfile = false
            for (info in srcinfos) {
                if (!info.stat()!!.isDir) {
                    hasfile = true
                    break
                }
            }
            return if (hasfile) {
                if (!srcstat.writable) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                info1(srcinfos)
            } else if (srcinfos.isNotEmpty()) {
                if (!srcstat.writable) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                JSONObject().put(Key.dirpath, srcinfo.apath)
            } else {
                if (FileInfoUtil.isRootOrUnderReadonlyRoot(srcinfo)) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                try {
                    val json = FileInfoUtil.tojson(srcinfo)
                    JSONObject().put(Key.dirinfo, json)
                } catch (e: Exception) {
                    return storage.rsrc.jsonObjectError(R.string.CommandFailed, ": ", src)
                }
            }
        }

        /// Delete only files.
        private fun info1(infos: List<IFileInfo>): JSONObject {
            for (info in infos) {
                val stat = info.stat()!!
                if (stat.isDir) {
                    continue
                }
                info1(stat, info.name)
            }
            return result()
        }

        private fun info1(stat: IFileStat, rpath: String) {
            deleting.add(rpath)
        }

        private fun result(): JSONObject {
            val ret = JSONObject()
            val d = ret.putJSONArrayOrFail(Key.deleting)
            for (rpath in TreeSet(deleting)) {
                d.put(rpath)
            }
            val nd = ret.putJSONObjectOrFail(Key.notdeleting)
            for ((key, value) in notdeleting) {
                val a = nd.putJSONArrayOrFail(key)
                for (rpath in TreeSet(value)) {
                    a.put(rpath)
                }
            }
            return ret
        }

        companion object {
            /// @return The json result string {
            ///     An.Key.result: {
            ///         An.Key.deleting : []
            ///         An.Key.notdeleting: [reason: []]
            ///     }
            ///     An.Key.errors:  errors
            /// }
            @Throws(JSONException::class)
            fun of(storage: IStorage, src: String): JSONObject {
                val srcinfo = storage.fileInfoAt(src).let {
                    it.first ?: return storage.rsrc.jsonObjectError(it.second)
                }
                val srcstat = srcinfo.stat()
                return if (srcstat == null || !srcstat.readable) {
                    storage.rsrc.jsonObjectError(R.string.NotFound_, src)
                } else DeleteInfo(storage, src, srcinfo, srcstat).info()
            }
        }

    }

    @Throws(JSONException::class)
    fun actionDeleteEmptyDirs(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.MissingPathParameter)
        val dir = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        if (dir.stat().let { it == null || !it.isDir }) return rsrc.jsonObjectError(R.string.DestinationIsNotADirectory)
        val count = FileInfoUtil.deleteEmptySubtrees(dir)
        val ret = JSONObject().put(Key.result, String.format(
                "%s %d %s",
                rsrc.get(R.string.Removed),
                count,
                rsrc.get(R.string.emptyDirectories)))
        listdir(ret, dir, null)
        return ret
    }

    /**
     * @param ret     {
     * An.Key.path: String,
     * An.Key.filename: String,
     * An.Key.dirpath: [dirinfo],
     * An.Key.dirtree: [fileinfo],
     * }
     * @param dirpath The rpath of dir relative to root.
     */
    @Throws(JSONException::class)
    fun listdir(ret: JSONObject, dir: IFileInfo, filename: String?): JSONObject {
        val warns = JSONArray()
        listdirpath<Unit>(ret, warns, dir)
        listdirtree(ret, warns, dir, filename)
        if (warns.length() > 0) ret.put(Key.warns, warns)
        return ret
    }

    @Throws(JSONException::class)
    private fun listdirtree(ret: JSONObject, warns: JSONArray, dir: IFileInfo, filename: String?) {
        val dirtree = ret.putJSONArrayOrFail(Key.dirtree)
        for (info in FileInfoUtil.filesByName(dir)) {
            FileInfoUtil.tojson(dirtree, warns, info)
        }
        ret.put(Key.path, dir.apath)
        ret.put(Key.filename, filename)
    }

    @Throws(JSONException::class)
    private fun <T> listdirpath(ret: JSONObject, warns: JSONArray, dir: IFileInfo, callback: Fun11<IFileInfo, T?>? = null): Boolean {
        val dirpath = ret.putJSONArrayOrFail(Key.dirpath)
        FileInfoUtil.tojson(dirpath, warns, dir.root)
        var d: IFileInfo = dir.root
        for (segment in TextUtil.splitAll(dir.rpath, File.separatorChar)) {
            if (segment.isEmpty()) continue
            val dd = d.fileInfo(segment)
            if (callback != null) {
                callback(dd)?.let {
                    ret.put(Key.errors, it)
                    return false
                }
            }
            d = dd
            FileInfoUtil.tojson(dirpath, warns, d)
        }
        return true
    }

    @Throws(JSONException::class)
    fun listroots1(ret: JSONObject): JSONObject {
        ret.put(Key.path, "")
        ret.put(Key.dirpath, JSONArray())
        val dirtree = ret.putJSONArrayOrFail(Key.dirtree)
        val warns = JSONArray()
        for (root in storage.getRoots()) {
            FileInfoUtil.tojson(dirtree, warns, root)
        }
        if (warns.length() > 0) ret.put(Key.warns, warns)
        return ret
    }

}
