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

import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.mapsJSONObjectNotNull
import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.Without
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.*
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import java.util.*

class HistoryFilepickerHandler(
    private val storage: IStorage,
    private val ajax: IAjax?,
    private val thumbnailCallback: IThumbnailCallback
) : IFilepickerHandler {

    private val rsrc = storage.rsrc

    override fun handle(cmd: Int, params: JSONObject): JSONObject {
        val serial = params.optLong(An.Key.serial, -1L)
        if (ajax == null || serial < 0) {
            return handle1(cmd, params)
        }
        storage.submit {
            try {
                val result = handle1(cmd, params)
                result.put(An.Key.serial, serial)
                ajax.response(result.toString())
            } catch (e: Exception) {
                ajax.error(serial, R.string.CommandFailed)
            }
        }
        return JSONObject()
    }

    @Throws(Exception::class)
    private fun handle1(cmd: Int, params: JSONObject): JSONObject {
        return when (cmd) {
            An.FilepickerCmd.FILEINFO -> actionFileInfo(params)
            An.FilepickerCmd.LISTDIR -> actionListDir(params)
            An.FilepickerCmd.DELETE -> actionDelete(params)
            An.FilepickerCmd.RENAME -> actionRename(params)
            An.FilepickerCmd.DELETE_DIRSUBTREE -> actionDeleteDirSubtree(params)
            An.FilepickerCmd.LIST_RECURSIVE -> actionListRecursive(params)
            An.FilepickerCmd.DIRINFO -> actionDirInfo(params)
            An.FilepickerCmd.MKDIRS,
            An.FilepickerCmd.COPY_INFO,
            An.FilepickerCmd.COPY,
            An.FilepickerCmd.DELETE_ALL,
            An.FilepickerCmd.DELETE_INFO,
            An.FilepickerCmd.DELETE_EMPTY_DIRS,
            An.FilepickerCmd.LOCAL_IMAGE_INFOS,
            An.FilepickerCmd.LOCAL_IMAGE_THUMBNAILS,
            An.FilepickerCmd.SHRED -> unsupported()

            else -> rsrc.jsonObjectError(R.string.InvalidFilepickerCommand_, cmd.toString())
        }
    }

    private fun unsupported(): JSONObject {
        return rsrc.jsonObjectError(R.string.UnsupportedOperation)
    }

    override fun listDir(ret: JSONObject, cpath: String): JSONObject {
        return fileinfo(Basepath.ensureTrailingSlash(cpath))
    }

    /// @param cpath Trailing / imply cpath is directory and filename is "".
    private fun actionFileInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        return fileinfo(cpath)
    }

    private fun actionListDir(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        return fileinfo(Basepath.ensureTrailingSlash(cpath))
    }

    private fun fileinfo(cpath: String): JSONObject {
        return this.fileinfo(JSONObject(), cpath)
    }

    private fun fileinfo(ret: JSONObject, cpath: String): JSONObject {
        var cleancpath = Support.getcleanrpathStrict(rsrc, cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val dirtree = ret.putJSONArrayOrFail(An.Key.dirtree)
        if (cleancpath.isEmpty()) {
            ret.put(Key.path, cleancpath)
            for (root in arrayOf(storage.getHomeRoot())) {
                val json = toDeletedFileStat(root, "", root.name)
                var size = 0L
                root.scanTrash { file ->
                    if (file.stat.isFile) {
                        size += file.stat.length
                    }
                }
                json.put(IFileInfo.Key.length, size)
                dirtree.put(json)
            }
            return ret
        }
        val fileinfo = storage.fileInfo(cleancpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, cpath)
        val root = fileinfo.root
        val ccpath = if (cpath.endsWith(FS) && !cleancpath.endsWith(FS)) cleancpath + FS else cleancpath
        val (dir, name) = Basepath.splitPath(ccpath)
        ret.put(Key.path, ccpath)
        if (dir == root.cpath && name.isEmpty()) {
            val sizes = TreeMap<String, Long>()
            val infos = TreeMap<String, JSONObject>()
            for (d in dirsList(root)) {
                val dcpath = d.cpath()
                if (!infos.containsKey(dcpath)) {
                    var size = 0L
                    filesAtDir(root, dcpath) {
                        size += it.stat.length
                    }
                    sizes[dcpath] = size + (sizes[dcpath] ?: 0L)
                    infos[dcpath] = d.toJSON()
                }
            }
            for ((dcpath, json) in infos.entries) {
                val size = sizes[dcpath] ?: 0L
                json.put(IFileInfo.Key.length, size)
                dirtree.put(json)
            }
        } else if (name.isEmpty() || dir.isNullOrEmpty() && name == root.cpath) {
            val seen = TreeSet<String>()
            root.history(if (dir.isNullOrEmpty()) name else dir, "", false) { dstat ->
                if (seen.add(dstat.cpath())) {
                    dirtree.put(dstat.toJSON())
                }
            }
        } else {
            root.history(dir ?: "", name, false) { dstat ->
                dirtree.put(dstat.toJSON())
            }
        }
        return ret
    }

    private fun actionDelete(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidArguments)
        val infos = params.getJSONArray(An.Key.infos)
            ?.mapsJSONObjectNotNull { JSONObjectDeletedFileStat(it) }
            ?.toList()
            ?: return rsrc.jsonObjectError(R.string.InvalidArguments)
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        return cleanupTrashResult(rsrc, R.string.Removed, pruneHistory(fileinfo.root, infos, false))
    }

    private fun actionRename(params: JSONObject): JSONObject {
        fun run(): JSONObject? {
            val dstpath = params.stringOrNull(An.Key.dst) ?: return null
            val srcpath = params.stringOrNull(An.Key.src) ?: return null
            val srcinfos = params.jsonArrayOrNull(An.Key.infos) ?: return null
            val dst = storage.fileInfoAt(dstpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            val src = storage.fileInfoAt(srcpath).let {
                it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
            }
            val infos = srcinfos.mapsJSONObjectNotNull { it }.toList()
            val result = src.root.recover(dst, infos)
            return recoverCounts(result)
        }
        return run() ?: rsrc.jsonObjectError(R.string.InvalidArguments)
    }

    private fun actionDeleteDirSubtree(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path)
            ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        val infos = params.jsonArrayOrNull(An.Key.infos)
            ?.mapsJSONObjectNotNull { JSONObjectDeletedFileStat(it) }
            ?.toList()
            ?: return rsrc.jsonObjectError(R.string.InvalidArguments)
        val fileinfo = if (cpath.isEmpty()) storage.getHomeRoot() else storage.fileInfoAt(cpath).let {
            it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
        }
        return cleanupTrashResult(rsrc, R.string.Removed, pruneHistory(fileinfo.root, infos, true))
    }

    private fun actionListRecursive(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path)
            ?: throw AssertionError()
        val searchtext = params.stringOrNull(Key.text)
            ?: throw AssertionError()
        val regex = Without.exceptionOrNull { Regex(searchtext) }
            ?: return rsrc.jsonObjectError(R.string.InvalidRegex_, searchtext)
        val cleancpath = Support.getcleanrpathStrict(rsrc, cpath).first
        val root = if (cleancpath.isNullOrEmpty() || cleancpath == FS) {
            storage.getHomeRoot()
        } else {
            val fileinfo = storage.fileInfo(cleancpath)
                ?: return rsrc.jsonObjectError(R.string.InvalidPath_, cpath)
            fileinfo.root
        }
        val ret = JSONObject()
        ret.put(Key.path, root.cpath + FS)
        val dirtree = ret.putJSONArrayOrFail(An.Key.dirtree)
        val seen = TreeSet<String>()
        val result = TreeMap<String, JSONObject>()
        try {
            root.scanTrash { dstat ->
                if (!dstat.stat.isFile) return@scanTrash
                val dcpath = dstat.cpath()
                if (seen.add(dcpath) && regex.containsMatchIn(dcpath)) {
                    result[dcpath] = dstat.toJSON()
                }
            }
            for (json in result.values) {
                dirtree.put(json)
            }
            return ret
        } catch (e: Exception) {
            return rsrc.jsonObjectError(e, R.string.CommandFailed)
        }
    }

    @Throws(JSONException::class)
    fun actionDirInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(Key.path) ?: return rsrc.jsonObjectError(R.string.ParameterMissingPath)
        var files = 0L
        var size = 0L
        val errors = TreeSet<String>()
        val cleancpath = Support.getcleanrpathStrict(errors, rsrc, cpath)
        if (errors.size > 0) return rsrc.jsonObjectError(errors)
        val fileinfo: IFileInfo? = if (cleancpath == null) null
        else if (cleancpath.isEmpty() || cleancpath == FS) storage.getHomeRoot()
        else storage.fileInfo(cleancpath)
        if (fileinfo == null || !fileinfo.root.supportHistory)
            return rsrc.jsonObjectError(R.string.InvalidPath)
        val root = fileinfo.root
        if (cleancpath?.isEmpty() == true || cleancpath != null && cleancpath.indexOf(FS) < 0 && cpath.endsWith(FS)) {
            val dirs = dirsUnder(root, fileinfo.cpath)
            val seen = TreeSet<String>()
            for (dir in dirs) {
                val dircpath = dir.cpath()
                if (seen.add(dircpath)) {
                    filesAtDir(root, dircpath) { file ->
                        ++files
                        size += file.stat.length
                    }
                }
            }
            return JSONObject().put(
                Key.result, JSONArray()
                    .put(files).put(seen.size).put(size)
            )
        }
        root.history(cleancpath ?: "", "", false) {
            ++files
            size += it.stat.length
        }
        return JSONObject().put(Key.result, JSONArray()
            .put(files).put(1).put(size)
        )
    }

    private fun toDeletedFileStat(info: IFileInfo, dir: String, name: String): JSONObject {
        val ret = FileInfoUtil.toJSONFileInfo(info)
        ret.put(IFileInfo.Key.id, 0)
        ret.put(IFileInfo.Key.dir, dir)
        ret.put(IFileInfo.Key.name, name)
        ret.put(IFileInfo.Key.lastDeleted, info.stat()!!.lastModified)
        return ret
    }

    private fun recoverCounts(result: Pair<Int, Int>): JSONObject {
        val (oks, fails) = result
        return JSONObject()
            .put(An.Key.total, oks + fails)
            .put(An.Key.fails, fails)
            .put(
                An.Key.result, String.format(
                    "%s %d %s, %d %s",
                    rsrc.get(R.string.Recover),
                    oks, rsrc.get(R.string.OK),
                    fails, rsrc.get(R.string.failed)
                )
            )
    }

    companion object {
        fun cleanupTrashResult(rsrc: IResUtil, msgid: StringId, result: CleanupTrashResult): JSONObject {
            return JSONObject()
                .put(An.Key.total, result.files + result.dirs)
                .put(An.Key.count, result.files)
                .put(
                    An.Key.result, String.format(
                        "%s %d %s, %d %s, %s",
                        rsrc.get(msgid),
                        result.files, rsrc.get(R.string.file),
                        result.dirs, rsrc.get(R.string.dir),
                        TextUt.fileHexUnit4String(result.totalsize, "B")
                    )
                )
        }

        fun pruneHistory(root: IRootInfo, infos: List<IDeletedFileStat>, all: Boolean): CleanupTrashResult {
            return root.transaction {
                root.cleanupTrash { dstat ->
                    for (info in infos) {
                        if (info.stat.isDir && (info.dir == "" || info.cpath() == dstat.dir)) return@cleanupTrash true
                        else if (info.stat.isFile) {
                            if (all && (info.dir == dstat.dir && info.name == dstat.name)) return@cleanupTrash true
                            else if (info.id == dstat.id) return@cleanupTrash true
                        }
                    }
                    return@cleanupTrash false
                }
            }
        }

        fun dirsList(
            root: IRootInfo,
        ): ArrayList<IDeletedFileStat> {
            val dirs = ArrayList<IDeletedFileStat>()
            root.history(root.cpath, "", true) { dstat ->
                dirs.add(dstat)
            }
            return dirs
        }

        fun dirsUnder(
            root: IRootInfo,
            cpath: String,
        ): ArrayList<IDeletedFileStat> {
            val dirs = ArrayList<IDeletedFileStat>()
            val cpathslash = cpath + FS
            root.history(root.cpath, "", true) { dstat ->
                val dircpath = dstat.cpath()
                if (dircpath == cpath || dircpath.startsWith(cpathslash)) {
                    dirs.add(dstat)
                }
            }
            return dirs
        }

        fun filesAtDir(root: IRootInfo, cpath: String, callback: Fun10<IDeletedFileStat>) {
            root.history(cpath, "", false) {
                callback(it)
            }
        }
    }
}
