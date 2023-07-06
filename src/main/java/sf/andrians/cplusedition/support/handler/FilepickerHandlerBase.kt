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

import com.cplusedition.anjson.JSONUtil.foreachStringNotNull
import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.maps
import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.anjson.JSONUtil.stringSequenceOrEmpty
import com.cplusedition.bot.core.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.*
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.Key.dirpath
import sf.andrians.cplusedition.support.An.Key.fileinfo
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
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
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val info = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        try {
            val json = FileInfoUtil.toJSONFileInfo(info)
            return rsrc.jsonObjectResult(Key.fileinfo, json)
        } catch (e: Exception) {
            return rsrc.jsonObjectError(R.string.CommandFailed)
        }
    }

    @Throws(JSONException::class)
    fun actionDirInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
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

        val errors = TreeSet<String>()
        val cleanrpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (errors.size > 0)
            return rsrc.jsonObjectError(errors)
        if (cleanrpath == "") {
            for (root in storage.getRoots()) {
                count(root)
            }
        } else {
            val info = storage.fileInfo(cleanrpath)
                ?: return rsrc.jsonObjectError(R.string.InvalidPath)
            if (!info.isDir)
                return rsrc.jsonObjectError(R.string.DestinationExpectingADir)
            for (file in info.readDir(ArrayList())) {
                if (file.isDir) {
                    ++dirs1
                    count(file)
                } else {
                    ++files1
                    size1 += file.stat()!!.length
                }
            }
        }
        return JSONObject().put(
            Key.result, JSONArray()
                .put(files + files1).put(dirs + dirs1).put(size + size1).put(files1).put(dirs1).put(size1)
        )
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
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        return listdir(JSONObject(), cpath)
    }

    @Throws(JSONException::class)
    protected fun listdir(ret: JSONObject, cpath: String?): JSONObject {
        val cleanpath = Support.getcleanrpath(cpath)
            ?: return rsrc.jsonObjectError(R.string.InvalidPath_, "$cpath")
        if (cleanpath.isEmpty()) {
            ret.put(Key.filename, "")
            return listroots1(ret)
        }
        val dir = storage.fileInfo(cleanpath)
            ?: return rsrc.jsonObjectError(R.string.InvalidPath_, "$cpath")
        val warns = JSONArray()
        val warnings = ArrayList<String>()
        val illegals = TreeSet<Int>()
        val dirpath = ret.putJSONArrayOrFail(Key.dirpath)
        FileInfoUtil.toJSONFileInfo(dirpath, warns, dir.root)
        var name = ""
        val segments = dir.rpath.split(FSC)
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
                    FileInfoUtil.toJSONFileInfo(dirpath, warns, d)
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
        if (warns.length() > 0) {
            val a = ret.jsonArrayOrNull(Key.warns) ?: JSONArray().also { ret.put(Key.warns, it) }
            for (w in warns.maps { warns.stringOrNull(it) }) {
                if (w != null) a.put(w)
            }
        }
        return ret
    }

    @Throws(JSONException::class)
    fun actionMkdirs(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val ret = JSONObject()
        val warns = ret.putJSONArrayOrFail(Key.warns)
        fileinfo.root.transaction {
            if (listdirpath(ret, warns, fileinfo) {
                    val stat = it.stat()
                    if (stat != null && !stat.isDir || stat == null && !it.mkdirs()) {
                        rsrc.get(R.string.CreateDirectoryFailed_, it.apath)
                    } else null
                }
            ) {
                listdirtree(ret, warns, fileinfo, null)
            }
        }
        return ret
    }

    @Throws(JSONException::class)
    fun actionDelete(params: JSONObject): JSONObject {
        return relaxFileinfo(params.stringOrNull(Key.path)).onResult({ it }, { (cpath, fileinfo, stat) ->
            if (FileInfoUtil.isRootOrUnderReadonlyRoot(fileinfo))
                return@onResult rsrc.jsonObjectError(R.string.NotDeletable)
            if (stat.isDir && fileinfo.readDir(ArrayList()).size > 0)
                return@onResult rsrc.jsonObjectError(R.string.CannotDeleteNonEmptyDirectory_, cpath)
            val isfile = stat.isFile
            if (!fileinfo.delete())
                return@onResult rsrc.jsonObjectError(R.string.DeleteFailed_, cpath)
            if (isfile) {
                storage.getSettingsStore().invoke {
                    it.deleteXrefsFrom(fileinfo)
                }
            }
            val parent = fileinfo.parent ?: fileinfo.root
            listdir(JSONObject(), parent, "")
        })
    }

    private fun relaxFileinfo(path: String?): IBotResult<Triple<String, IFileInfo, IFileStat>, JSONObject> {
        val cpath = path
            ?: return BotResult.fail(rsrc.jsonObjectError(R.string.ParameterMissingPath))
        val fileinfo = storage.fileInfo(Support.getcleanrpath(cpath))
            ?: return BotResult.fail(rsrc.jsonObjectError(R.string.InvalidPath_, cpath))
        val stat = fileinfo.stat()
            ?: return BotResult.fail(rsrc.jsonObjectError(R.string.NotFound_, cpath))
        return BotResult.ok(Triple(cpath, fileinfo, stat))
    }

    @Throws(JSONException::class)
    fun actionShred(params: JSONObject): JSONObject {
        return relaxFileinfo(params.stringOrNull(Key.path)).onResult({ it }, { (dirpath, dir, _) ->
            if (FileInfoUtil.isRootOrUnderReadonlyRoot(dir))
                return@onResult rsrc.jsonObjectError(R.string.NotDeletable)
            val errors = TreeSet<String>()
            val rpaths = params.jsonArrayOrNull(Key.rpaths)
                ?: return@onResult rsrc.jsonObjectError(R.string.InvalidArguments)
            storage.getSettingsStore().invoke { st ->
                shred(errors, st, dir, rpaths)
            }
            if (errors.isNotEmpty()) return@onResult rsrc.jsonObjectError(errors)
            listdir(JSONObject(), dir, "")
        })
    }

    private fun shred(
        errors: MutableCollection<String>,
        st: ISettingsStoreAccessor,
        dir: IFileInfo,
        rpaths: JSONArray,
    ) {
        val shredfile = { file: IFileInfo, rpath: String ->
            if (file.shred()) {
                st.deleteXrefsFrom(file)
            } else {
                errors.add(rpath)
            }
        }
        rpaths.foreachStringNotNull { index, rpath ->
            val file = dir.fileInfo(rpath)
            if (file.isFile) {
                shredfile(file, rpath)
            } else if (file.isDir) {
                file.walk3(true, dirpath) { f, p, stat ->
                    if (stat.isDir) {
                        if (!f.delete(true)) {
                            errors.add(p)
                        }
                    } else if (stat.isFile) {
                        shredfile(f, p)
                    }
                }
                if (!file.delete(true)) {
                    errors.add(rpath)
                }
            }
        }
    }

    @Throws(JSONException::class)
    fun actionRename(params: JSONObject): JSONObject {
        val newname = params.stringOrNull(Key.filename)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissing_, Key.filename)
        val path = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val fixxrefs = params.optBoolean(Key.xrefs)
        val cleanpath = Support.getcleanrpath(path)
            ?: return rsrc.jsonObjectError(R.string.InvalidPath_, path)
        val srcinfo = storage.fileInfo(cleanpath)
            ?: return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", path)
        val srcstat = srcinfo.stat()
            ?: return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", path)
        //// Cannot rename root, top directories and in readonly tree.
        if (cleanpath.isEmpty() || FileInfoUtil.isRootOrUnderReadonlyRoot(srcinfo)) {
            return rsrc.jsonObjectError(R.string.SourceNotDeletable, ": ", path)
        }
        val err = ArrayList<String>()
        if (Support.sanitizeFilenameStrict(err, rsrc, newname).size > 0)
            return rsrc.jsonObjectError(err)
        val isdir = srcstat.isDir
        val parent = srcinfo.parent
            ?: return rsrc.jsonObjectError(R.string.RenameFailed_, path)
        val dstinfo = parent.fileInfo(newname)
        if (dstinfo.exists)
            return rsrc.jsonObjectError(R.string.DestinationAlreadyExists, ": ", newname)
        try {
            if (srcinfo.content().renameTo(dstinfo, srcstat.lastModified)) {
                storage.getSettingsStore().invoke { st ->
                    st.renameXrefsFrom(dstinfo.apath, srcinfo.apath)
                    if (fixxrefs) {
                        XrefUt.onMove(storage, st, dstinfo, srcinfo) { it == srcinfo.apath }
                    }
                }
            }
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
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingDestinationPath)
        val src = params.stringOrNull(Key.src)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingSourcePath)
        return CopyInfo.of(storage, cut, dst, src)
    }

    @Throws(JSONException::class)
    fun actionLocalImageInfos(params: JSONObject): JSONObject {
        val dir = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val dirinfo = storage.fileInfoAt(dir).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (dirinfo.stat().let { it == null || !it.isDir }) {
            return rsrc.jsonObjectError(R.string.InvalidPath_, dir)
        }
        val ret = JSONObject()
        val result = ret.putJSONArrayOrFail(Key.result)
        for (fileinfo in FileInfoUtil.filesByName(dirinfo)) {
            val name = fileinfo.name
            val mime = MimeUtil.imageMimeFromLcSuffix(Basepath.lcSuffix(name)) ?: continue
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
        val infos = params.optJSONArray(Key.infos)
            ?: return rsrc.jsonObjectError(R.string.NoImageInfoAvailable)
        val tsize = _getthumbnailsize(params)
        return ImageUtil.actionImageThumbnails(storage, infos, tsize, callback)
    }

    /// @param dirpath Relative path for dir in root.
    /// @return { dirtree: [rpath, fileinfo][] }
    fun actionListRecursive(params: JSONObject): JSONObject {
        try {
            val path = params.stringOrNull(Key.path)
            val level = TextUt.parseInt(params.stringOrNull(Key.level), 0)
            val dirinfo = storage.fileInfoAt(path).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            if (dirinfo.stat().let { it == null || !it.isDir }) {
                return rsrc.jsonObjectError(R.string.InvalidPath)
            }
            val dirtree = JSONArray()
            /// NOTE: Currently only level value of 0 and 1 are supported.
            if (level == 1) listOneLevelDown(dirtree, dirinfo)
            else listRecursive(dirtree, "", dirinfo)
            return JSONObject().put(Key.dirtree, dirtree)
        } catch (e: Exception) {
            return rsrc.jsonObjectError(R.string.CommandFailed)
        }
    }

    private fun listOneLevelDown(ret: JSONArray, dirinfo: IFileInfo) {
        for (info in FileInfoUtil.filesByName(dirinfo)) {
            if (info.stat()?.isDir == true) {
                listOneLevelDown1(ret, info.name, info)
            }
        }
    }

    private fun listOneLevelDown1(ret: JSONArray, dirpath: String, dirinfo: IFileInfo) {
        for (info in FileInfoUtil.filesByName(dirinfo)) {
            if (info.stat()?.isFile == true) {
                val rpath = Basepath.joinRpath(dirpath, info.name)
                ret.put(JSONArray().put(rpath).put(info.toJSON()))
            }
        }
    }

    private fun listRecursive(ret: JSONArray, dirpath: String, dirinfo: IFileInfo) {
        for (info in FileInfoUtil.filesByName(dirinfo)) {
            val rpath = Basepath.joinRpath(dirpath, info.name)
            if (info.stat().let { it != null && it.isDir }) {
                listRecursive(ret, rpath, info)
                continue
            }
            ret.put(JSONArray().put(rpath).put(info.toJSON()))
        }
    }

    /// @param { src: "A context relative path to basedir", rpaths: [string] }
    /// @return  {
    ///   An.Key.result: { rpath : fileinfo},
    ///   An.Key.errors: Object,
    /// }
    @Throws(JSONException::class)
    fun actionFileInfos(params: JSONObject): JSONObject {
        val dir = params.stringOrNull(Key.src)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingSourcePath)
        val rpaths = params.stringSequenceOrEmpty(Key.rpaths)
        val dirinfo = storage.fileInfoAt(dir).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
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
        private val overwriting = JSONObject()
        private val copying = JSONObject()
        private val notcopying = TreeMap<String, MutableList<String>>()
        fun info(): JSONObject {
            if (!dstinfo.root.stat().writable)
                return res.jsonObjectError(R.string.DestinationNotWritable_, dstinfo.cpath)
            val srcstat = srcinfo.stat()
                ?: return res.jsonObjectError(R.string.SourceNotFound, ": ", srcinfo.cpath)
            if (cut && !srcstat.writable)
                return res.jsonObjectError(R.string.SourceNotDeletable, ": ", srcinfo.cpath)
            if (!srcstat.isDir) {
                val name = srcinfo.name
                val dinfo = dstinfo.fileInfo(name)
                info1(dstinfo, dinfo.stat(), srcinfo, srcstat, name)
            } else {
                val files = FileInfoUtil.filesByName(srcinfo)
                val dststat = dstinfo.stat()
                if (dststat != null && dststat.isDir && !dststat.writable) {
                    return res.jsonObjectError(R.string.DestinationNotWritable_, dstinfo.cpath)
                }
                copydir(dstinfo, files)
            }
            return result()
        }

        private fun copydir(dparent: IFileInfo, sinfos: Collection<IFileInfo>) {
            for (sinfo in sinfos) {
                val sstat = sinfo.stat()
                val name = sinfo.name
                if (sstat == null) {
                    StructUt.putList(notcopying, res.get(R.string.SourceNotFound), name)
                    continue
                }
                if (Support.sanitizeFilepath(ArrayList(), res, name).size > 0) {
                    StructUt.putList(notcopying, res.get(R.string.SourceNotValid), name)
                    continue
                }
                val dinfo = dstinfo.fileInfo(name)
                val dstat = dinfo.stat()
                if (sstat.isDir) {
                    if (dstat == null) {
                        copying.put(name, infoOf(sstat, sstat))
                    } else if (dstat.isDir) {
                        if (!dstat.writable) {
                            StructUt.putList(notcopying, res.get(R.string.DestinationNotWritable), name)
                        } else overwriting.put(name, infoOf(sstat, dstat))
                    } else {
                        overwriting.put(name, infoOf(sstat, dstat))
                    }
                } else {
                    info1(dparent, dstat, sinfo, sstat, name)
                }
            }
        }

        private fun info1(
            dparent: IFileInfo,
            dstat: IFileStat?,
            sinfo: IFileInfo,
            sstat: IFileStat,
            rpath: String,
        ) {
            if (Support.sanitizeFilepath(ArrayList(), res, sinfo.name).size > 0) {
                StructUt.putList(notcopying, res.get(R.string.SourceNotValid), rpath)
                return
            }
            if (dstat != null) {
                if (!dstat.writable) {
                    StructUt.putList(notcopying, res.get(R.string.DestinationNotWritable_), rpath)
                } else {
                    overwriting.put(rpath, infoOf(sstat, dstat))
                }
                return
            }
            val dparentstat = dparent.stat()
            if (dparentstat != null && !dparentstat.writable) {
                StructUt.putList(notcopying, res.get(R.string.DestinationNotWritable_), rpath)
            } else {
                copying.put(rpath, infoOf(sstat, sstat))
            }
        }

        private fun infoOf(sstat: IFileStat, dstat: IFileStat): JSONArray {
            return JSONArray().put(sstat.isDir).put(dstat.isDir).put(sstat.length).put(sstat.lastModified)
        }

        /// Create result JSONObject string with file paths sorted.
        private fun result(): JSONObject {
            val ret = JSONObject()
            ret.put(Key.copying, copying)
            ret.put(Key.overwriting, overwriting)
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
                val srcinfo = storage.fileInfoAt(srcpath).result()
                    ?: return res.jsonObjectError(R.string.SourceNotValid, ": ", srcpath)
                val dstinfo = storage.fileInfoAt(dstpath).result()
                    ?: return res.jsonObjectError(R.string.DestinationNotValid_, dstpath)
                return CopyInfo(res, cut, dstinfo, srcinfo).info()
            }
        }
    }

    @Throws(JSONException::class)
    fun actionCopy(params: JSONObject): JSONObject {
        val cut = params.optBoolean(Key.cut)
        val preservetimestamp = params.optBoolean(Key.timestamp)
        val fixxrefs = params.optBoolean(Key.xrefs)
        val dstdirpath = params.stringOrNull(Key.dst)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingDestinationPath)
        val srcpath = params.stringOrNull(Key.src)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingSourcePath)
        val src = storage.fileInfoAt(srcpath).result()
            ?: return rsrc.jsonObjectError(R.string.SourceNotValid, ": ", srcpath)
        val dstdir = storage.fileInfoAt(dstdirpath).result()
            ?: return rsrc.jsonObjectError(R.string.DestinationNotValid_, dstdirpath)
        val rpaths = params.stringSequenceOrEmpty(Key.rpaths).toList()
        return CopyAction(this, storage, cut, preservetimestamp, fixxrefs, dstdir, src, rpaths).copy()
    }

    internal class CopyAction constructor(
        private val handler: FilepickerHandlerBase,
        private val storage: IStorage,
        private val cut: Boolean,
        private val preserveTimestamp: Boolean,
        private val fixXrefs: Boolean,
        private val dst: IFileInfo,
        private val src: IFileInfo,
        private val rpaths: List<String>
    ) {
        private val rsrc = storage.rsrc
        private val warns = JSONArray()
        private val oks = JSONArray()

        @Throws(JSONException::class)
        fun copy(): JSONObject {
            if (rpaths.isEmpty()) {
                storage.getSettingsStore().invoke { st ->
                    copy1(st, dst, src, "")
                }?.let {
                    return it
                }
            } else {
                copymulti(dst, src)?.let {
                    return it
                }
            }
            val ret = JSONObject()
            if (warns.length() > 0) {
                ret.put(Key.warns, warns)
            }
            ret.put(Key.result, oks)
            ret.put(Key.count, src.readDir(ArrayList<IFileInfo>()).size)
            return handler.listdir(ret, if (dst.isDir) dst.cpath else dst.parent?.cpath)
        }

        /// @return A json error on error or nil if OK.
        private fun copy1(st: ISettingsStoreAccessor, dst: IFileInfo, src: IFileInfo, rpath: String): JSONObject? {
            val srcstat = src.stat()
            if (srcstat == null || !srcstat.readable)
                return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", src.cpath)
            val dststat = dst.stat()
            if (srcstat.isFile) {
                return copyfile1(st, dst, dststat, src, srcstat) { it == src.apath }
            }
            if (srcstat.isDir) {
                if (dststat == null) {
                    if (!dst.mkdirs())
                        return rsrc.jsonObjectError(R.string.CreateDirectoryFailed_, this.dst.cpath)
                } else if (!dststat.isDir) {
                    return rsrc.jsonObjectError(R.string.DestinationExpectingADir, ": ", this.dst.cpath)
                } else if (!dststat.writable) {
                    return rsrc.jsonObjectError(R.string.DestinationNotWritable_, this.dst.cpath)
                }
                return dst.root.transaction {
                    val base = src.apath + FS
                    copydir1(st, dst, src, rpath) {
                        it.startsWith(base)
                    }
                }
            }
            return null
        }

        private fun copydir1(
            st: ISettingsStoreAccessor,
            dst: IFileInfo,
            src: IFileInfo,
            rpath: String,
            incopyset: Fun11<String, Boolean>,
        ): JSONObject? {
            if (dst.exists && !dst.isDir)
                dst.delete()
            dst.mkdirs()
            val dstsrcs = ArrayList<Triple<IFileInfo, IFileInfo, String>>()
            for (name in src.listOrEmpty()) {
                val s = src.fileInfo(name)
                val d = dst.fileInfo(name)
                val r = Basepath.joinRpath(rpath, name)
                if (s.isDir) {
                    copydir1(st, d, s, r, incopyset)
                } else {
                    dstsrcs.add(Triple(d, s, r))
                }
            }
            val ret = copyfiles(st, dstsrcs, incopyset)
            if (cut) src.deleteEmptyTree()
            return ret
        }

        private fun copymulti(dst: IFileInfo, src: IFileInfo): JSONObject? {
            if (!src.isDir)
                return rsrc.jsonObjectError(R.string.SourceExpectingADir)
            dst.stat().let {
                if (it == null) {
                    if (!dst.mkdirs())
                        return rsrc.jsonObjectError(R.string.CreateDirectoryFailed_, this.dst.cpath)
                } else if (!it.isDir)
                    return rsrc.jsonObjectError(R.string.DestinationExpectingADir)
            }
            val fileset = TreeSet<String>()
            val dirset = ArrayList<String>()
            val srcs = ArrayList<IFileInfo>()
            val fileinfos = ArrayList<Triple<IFileInfo, IFileInfo, String>>()
            val dirinfos = ArrayList<Triple<IFileInfo, IFileInfo, String>>()
            for (rpath in rpaths.toSortedSet()) {
                val s = src.fileInfo(rpath)
                val d = dst.fileInfo(rpath)
                srcs.add(s)
                if (s.isDir) {
                    dirset.add(s.apath + FS)
                    dirinfos.add(Triple(d, s, rpath))
                } else {
                    fileset.add(s.apath)
                    fileinfos.add(Triple(d, s, rpath))
                }
            }
            val incopyset = { apath: String ->
                if (fileset.contains(apath)) true
                else dirset.find { apath.startsWith(it) } != null
            }
            storage.getSettingsStore().invoke { st ->
                for (info in dirinfos) {
                    copydir1(st, info.first, info.second, info.third, incopyset)
                }
                copyfiles(st, fileinfos, incopyset)
            }
            if (cut) {
                for (s in srcs) {
                    s.deleteEmptyTree()
                }
            }
            return null
        }

        private fun copyfiles(
            st: ISettingsStoreAccessor,
            dstsrcs: ArrayList<Triple<IFileInfo, IFileInfo, String>>,
            incopyset: Fun11<String, Boolean>
        ): JSONObject? {
            val errors = TreeSet<String>()
            for ((d, s, rpath) in dstsrcs) {
                val sstat = s.stat()
                    ?: continue
                val dstat = d.stat()
                if (dstat != null) {
                    if (sstat.isDir) {
                        if (!d.isDir) {
                            if (d.delete()) {
                                st.deleteXrefsFrom(d)
                            } else {
                                errors.add(rsrc.jsonError(R.string.DeleteFailed_, rpath))
                                continue
                            }
                        }
                        continue
                    }
                    if (!d.deleteTree { st.deleteXrefsFrom(it) }) {
                        errors.add(rsrc.jsonError(R.string.DeleteFailed_, rpath))
                        continue
                    }
                    copy1(st, d, s, sstat, rpath, incopyset)?.let {
                        errors.add(it)
                    }
                    continue
                }
                if (sstat.isDir) {
                    if (!d.mkdirs()) {
                        errors.add(rsrc.jsonError(R.string.CreateDirectoryFailed_, rpath))
                    }
                    continue
                }
                copy1(st, d, s, sstat, rpath, incopyset)?.let {
                    errors.add(it)
                }
            }
            return if (errors.isNotEmpty()) rsrc.jsonObjectError(errors) else null
        }

        private fun copyfile1(
            st: ISettingsStoreAccessor,
            dst: IFileInfo,
            dststat: IFileStat?,
            src: IFileInfo,
            srcstat: IFileStat,
            incopyset: Fun11<String, Boolean>
        ): JSONObject? {
            val ret = if (dststat != null && dststat.isDir) {
                copy1(st, dst.fileInfo(src.name), src, srcstat, src.name, incopyset)
            } else {
                copy1(st, dst, src, srcstat, src.name, incopyset)
            }
            return ret?.let { rsrc.jsonObjectError(it) }
        }

        /// @return An error message or nil if copy OK.
        private fun copy1(
            st: ISettingsStoreAccessor,
            dst: IFileInfo,
            src: IFileInfo,
            srcstat: IFileStat?,
            rpath: String,
            incopyset: Fun11<String, Boolean>,
        ): String? {
            if (srcstat == null || !srcstat.readable) {
                return rsrc.get(R.string.SourceNotFound, ": ", rpath)
            }
            if (!srcstat.isFile) {
                return rsrc.get(R.string.SourceExpectingAFile, ": ", rpath)
            }
            val dstat = dst.stat()
            if (dstat != null && !dstat.isFile) {
                return rsrc.get(R.string.DestinationExpectingAFile, ": ", rpath)
            }
            if (dst.apath == src.apath) {
                return rsrc.get(R.string.CopyToSelfIsNotAllowed, ": ", rpath)
            }
            val dstext = Basepath.ext(dst.name)
            val srcext = Basepath.ext(src.name)
            if (dstext != srcext) {
                return rsrc.get(R.string.ChangeFileExtIsNotAllowed, ": ", rpath)
            }
            try {
                if (fixXrefs) {
                    val lcsuffix = src.lcSuffix
                    XrefUt.onCopy(st, dst, src, lcsuffix, cut, preserveTimestamp, incopyset)
                    if (cut) {
                        XrefUt.onMove(storage, st, dst, src, incopyset)
                    }
                } else {
                    val timestamp = if (preserveTimestamp) srcstat.lastModified else System.currentTimeMillis()
                    if (cut) src.content().moveTo(dst, timestamp)
                    else src.content().copyTo(dst, timestamp)
                    XrefUt.buildXrefs(dst) { apath, xrefs ->
                        st.updateXrefs(apath, xrefs)
                    }
                    if (cut) st.deleteXrefsFrom(src)
                }
                oks.put(rpath)
                return null
            } catch (e: Throwable) {
                
                return rsrc.get(R.string.CopyFailed_, rpath)
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
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val dir = storage.fileInfoAt(cpath).let {
            it.result()
                ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val dirstat = dir.stat()
            ?: return rsrc.jsonObjectError(R.string.NotFound_, cpath)
        if (!dirstat.isDir)
            return rsrc.jsonObjectError(R.string.DestinationExpectingADir, ": ", cpath)
        val warns = JSONArray()
        dir.root.transaction {
            deletedirsubtree(warns, dir)
        }
        val ret = JSONObject()
        if (warns.length() > 0) {
            ret.put(Key.warns, warns)
        }
        listdir(ret, dir, "")
        return ret
    }

    private fun deletedirsubtree(warns: JSONArray, dir: IFileInfo): Boolean {
        var ok = true
        storage.getSettingsStore().invoke {
            dir.walk2(true) { info, stat ->
                val isfile = stat.isFile
                if (!info.delete()) {
                    warns.put(rsrc.get(R.string.DeleteFailed_, info.apath))
                    ok = false
                    return@walk2
                }
                if (isfile) {
                    it.deleteXrefsFrom(info)
                }
            }
        }
        return ok
    }

    @Throws(JSONException::class)
    fun actionDeleteAll(params: JSONObject): JSONObject {
        val src = params.stringOrNull(Key.src)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingSourcePath)
        val rpaths = params.stringSequenceOrEmpty(Key.rpaths).toList()
        return deleteall0(src, rpaths)
    }

    @Throws(JSONException::class)
    private fun deleteall0(srcpath: String, rpaths: List<String>): JSONObject {
        val ret = JSONObject()
        val warns = JSONArray()
        val oks = ret.putJSONArrayOrFail(Key.result)
        deleteall1(warns, oks, srcpath, rpaths)?.let { return it }
        if (warns.length() > 0) ret.put(Key.warns, warns)
        return listdir(ret, srcpath)
    }

    /// @return nil if OK, otherwise the json error.
    private fun deleteall1(warns: JSONArray, oks: JSONArray, srcpath: String, rpaths: List<String>): JSONObject? {
        val srcinfo = storage.fileInfoAt(srcpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        val srcstat = srcinfo.stat()
        if (srcstat == null || !srcstat.readable) {
            return rsrc.jsonObjectError(R.string.SourceNotFound, ": ", srcpath)
        }
        return storage.getSettingsStore().invoke { st ->
            fun callback(file: IFileInfo) {
                st.deleteXrefsFrom(file)
            }
            if (srcstat.isDir) {
                srcinfo.root.transaction {
                    for (rpath in rpaths) {
                        val sinfo = srcinfo.fileInfo(rpath)
                        deleteall2(oks, sinfo, rpath, ::callback)?.let {
                            warns.put(it)
                        }
                    }
                }
                null
            } else {
                val rpath = srcinfo.name
                deleteall2(oks, srcinfo, rpath, ::callback)?.let {
                    rsrc.jsonObjectError(it)
                }
            }
        }
    }

    /// @return nil if OK, otherwise the error message
    private fun deleteall2(oks: JSONArray, sinfo: IFileInfo, rpath: String, callback: Fun10<IFileInfo>): String? {
        val sstat = sinfo.stat()
        if (sstat == null || !sstat.readable)
            return rsrc.get(R.string.NotFound_, rpath)
        if (!sinfo.deleteTree(callback))
            return rsrc.get(R.string.DeleteFailed_, rpath)
        oks.put(rpath)
        return null
    }

    @Throws(JSONException::class)
    fun actionDeleteInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        return DeleteInfo.of(storage, cpath)
    }

    internal class DeleteInfo(
        val storage: IStorage,
        val src: String,
        val srcinfo: IFileInfo,
        val srcstat: IFileStat
    ) {
        val deleting = JSONObject()
        val inuse = JSONObject()
        val notdeleting = JSONObject()

        @Throws(JSONException::class)
        private fun info(): JSONObject {
            if (!srcstat.isDir) {
                val parent = srcinfo.parent
                if (parent == null || parent.stat()?.writable != true) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                info1(srcinfo, srcstat)
                return result()
            }

            val srcinfos = FileInfoUtil.filesByName(srcinfo)
            if (srcinfos.isEmpty()) {
                if (FileInfoUtil.isRootOrUnderReadonlyRoot(srcinfo)) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                try {
                    val json = FileInfoUtil.toJSONFileInfo(srcinfo)
                    return JSONObject().put(Key.dirinfo, json)
                } catch (e: Exception) {
                    return storage.rsrc.jsonObjectError(R.string.CommandFailed, ": ", src)
                }
            } else {
                if (!srcstat.writable) {
                    return storage.rsrc.jsonObjectError(R.string.NotDeletable_, src)
                }
                for (info in srcinfos) {
                    val stat = info.stat()
                    if (stat == null) {
                        notdeleting.put(info.name, storage.rsrc.get(R.string.NotExists))
                        continue
                    }
                    info1(info, stat)
                }
                return result()
            }
        }

        private fun info1(info: IFileInfo, stat: IFileStat) {
            val xrefs = if (info.isFile) storage.getSettingsStore().invoke { it.getXrefsTo(info.apath) }
            else null
            if (!xrefs.isNullOrEmpty()) {
                stat1(inuse, info.name, stat)
            } else {
                stat1(deleting, info.name, stat)
            }
        }

        private fun stat1(ret: JSONObject, key: String, stat: IFileStat) {
            ret.put(key, JSONArray().put(stat.isDir).put(stat.length).put(stat.lastModified))
        }

        private fun result(): JSONObject {
            val ret = JSONObject()
                .put(Key.deleting, deleting)
                .put(Key.overwriting, inuse)
                .put(Key.notdeleting, notdeleting)
            return ret
        }

        companion object {
            /// @return The json result string {
            ///     An.Key.result: {
            ///         An.Key.deleting : []
            ///         An.Key.inuse : { filestat: [], xrefs: []}
            ///         An.Key.notdeleting: [reason: []]
            ///     }
            ///     An.Key.errors:  errors
            /// }
            @Throws(JSONException::class)
            fun of(storage: IStorage, src: String): JSONObject {
                val srcinfo = storage.fileInfoAt(src).let {
                    it.result() ?: return storage.rsrc.jsonObjectError(it.failure()!!)
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
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        val dirinfo = storage.fileInfoAt(cpath).let {
            it.result()
                ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        if (!dirinfo.isDir)
            return rsrc.jsonObjectError(R.string.DestinationExpectingADir)
        val count = dirinfo.deleteEmptySubtrees()
        val ret = JSONObject().put(
            Key.result, String.format(
                "%s %d %s",
                rsrc.get(R.string.Removed),
                count,
                rsrc.get(R.string.emptyDirectories)
            )
        )
        listdir(ret, dirinfo, null)
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
            FileInfoUtil.toJSONFileInfo(dirtree, warns, info)
        }
        ret.put(Key.path, dir.apath)
        ret.put(Key.filename, filename)
    }

    @Throws(JSONException::class)
    private fun <T> listdirpath(
        ret: JSONObject,
        warns: JSONArray,
        dir: IFileInfo,
        callback: Fun11<IFileInfo, T?>? = null
    ): Boolean {
        val dirpath = ret.putJSONArrayOrFail(Key.dirpath)
        FileInfoUtil.toJSONFileInfo(dirpath, warns, dir.root)
        var d: IFileInfo = dir.root
        for (segment in dir.rpath.split(FSC)) {
            if (segment.isEmpty()) continue
            val dd = d.fileInfo(segment)
            if (callback != null) {
                callback(dd)?.let {
                    ret.put(Key.errors, it)
                    return false
                }
            }
            d = dd
            FileInfoUtil.toJSONFileInfo(dirpath, warns, d)
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
            FileInfoUtil.toJSONFileInfo(dirtree, warns, root)
        }
        if (warns.length() > 0) ret.put(Key.warns, warns)
        return ret
    }
}
