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

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.anjson.JSONUtil.jsonArrayOrNull
import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.anjson.JSONUtil.toJSONObjectOrNullList
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.WithoutUtil.Companion.Without
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.FileInfoUtil
import sf.andrians.cplusedition.support.IDeletedFileStat
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import java.io.File

class HistoryFilepickerHandler(
        private val storage: IStorage,
        private val ajax: IAjax?,
        private val thumbnailCallback: IThumbnailCallback
) : IFilepickerHandler {

    private val rsrc = storage.rsrc

    override fun handle(cmd: Int, params: JSONObject): String {
        val serial = params.optLong(An.Key.serial, -1L)
        if (ajax == null || serial < 0) {
            return handle1(cmd, params).toString()
        }
        storage.submit {
            try {
                val result = handle1(cmd, params)
                result.put(An.Key.serial, serial)
                ajax.result(result.toString())
            } catch (e: Exception) {
                ajax.error(serial, R.string.CommandFailed)
            }
        }
        return "{}"
    }

    @Throws(Exception::class)
    private fun handle1(cmd: Int, params: JSONObject): JSONObject {
        return when (cmd) {
            An.FilepickerCmd.FILEINFO -> actionFileInfo(params)
            An.FilepickerCmd.LISTDIR -> actionListDir(params)
            An.FilepickerCmd.MKDIRS -> actionMkdirs(params)
            An.FilepickerCmd.DELETE -> actionDelete(params)
            An.FilepickerCmd.RENAME -> actionRename(params)
            An.FilepickerCmd.COPY_INFO -> actionCopyInfo(params)
            An.FilepickerCmd.COPY -> actionCopy(params)
            An.FilepickerCmd.DELETE_DIRSUBTREE -> actionDeleteDirSubtree(params)
            An.FilepickerCmd.DELETE_ALL -> actionDeleteAll(params)
            An.FilepickerCmd.DELETE_INFO -> actionDeleteInfo(params)
            An.FilepickerCmd.DELETE_EMPTY_DIRS -> actionDeleteEmptyDirs(params)
            An.FilepickerCmd.LOCAL_IMAGE_INFOS -> actionLocalImageInfos(params)
            An.FilepickerCmd.LOCAL_IMAGE_THUMBNAILS -> actionLocalImageThumbnails(params, thumbnailCallback)
            An.FilepickerCmd.LIST_RECURSIVE -> actionListRecursive(params)
            else -> rsrc.jsonObjectError(R.string.InvalidFilepickerCommand_, cmd.toString())
        }
    }

    /// @param cpath Trailing / imply cpath is directory and filename is "".
    private fun actionFileInfo(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(An.Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        return fileinfo(cpath, false)
    }

    private fun actionListDir(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(An.Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        return fileinfo(cpath, true)
    }

    private fun fileinfo(cpath: String, listdir: Boolean): JSONObject {
        val cleanrpath = Support.getcleanrpathStrict(rsrc, cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        val ret = JSONObject()
        ret.put(An.Key.path, cpath)
        val dirtree = ret.putJSONArrayOrFail(An.Key.dirtree)
        if (cleanrpath.isEmpty()) {
            dirtree.put(toDeletedFileStat(storage.getHomeRoot()))
        } else {
            val fileinfo = storage.fileInfo(cleanrpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, cpath)
            ret.put(An.Key.dirpath, JSONArray().put(FileInfoUtil.tojson(fileinfo)))
            val history = fileinfo.root.history(if (cpath.endsWith(File.separator)) fileinfo.rpath + File.separator else fileinfo.rpath, listdir)
            for (info in history) {
                dirtree.put(info.toJSON())
            }
        }
        return ret
    }

    private fun actionMkdirs(params: JSONObject): JSONObject {
        return rsrc.jsonObjectError(R.string.UnsupportedOperation)
    }

    private fun actionDelete(params: JSONObject): JSONObject {
        fun run(): JSONObject? {
            val cpath = params.stringOrNull(An.Key.path) ?: return null
            val array = params.getJSONArray(An.Key.infos) ?: return null
            val infos = array.toJSONObjectOrNullList().mapNotNull { it }
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.first ?: return rsrc.jsonObjectError(it.second)
            }
            return deletedCounts(fileinfo.root.pruneHistory(infos, false))
        }
        return run() ?: return rsrc.jsonObjectError(R.string.InvalidArguments)
    }

    private fun actionRename(params: JSONObject): JSONObject {
        fun run(): JSONObject? {
            val s = params.stringOrNull(An.Key.path) ?: return null
            val srcjson = JSONUtil.jsonObjectOrNull(s) ?: return null
            val dstpath = params.stringOrNull(An.Key.filename) ?: return null
            val srcpath = srcjson.stringOrNull(An.Key.src) ?: return null
            val dst = storage.fileInfoAt(dstpath).let {
                it.first ?: return rsrc.jsonObjectError(it.second)
            }
            val src = storage.fileInfoAt(srcpath).let {
                it.first ?: return rsrc.jsonObjectError(it.second)
            }
            val srcinfos = srcjson.jsonArrayOrNull(An.Key.infos) ?: return null
            val infos = srcinfos.toJSONObjectOrNullList().mapNotNull { it }
            val result = src.root.recover(dst, infos)
            return recoverCounts(result)
        }
        return run() ?: rsrc.jsonObjectError(R.string.InvalidArguments)
    }

    private fun actionCopyInfo(params: JSONObject): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionCopy(params: JSONObject): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionDeleteDirSubtree(params: JSONObject): JSONObject {
        val cpath = params.stringOrNull(An.Key.path) ?: return rsrc.jsonObjectError(R.string.InvalidPath)
        val array = params.jsonArrayOrNull(An.Key.infos) ?: throw AssertionError()
        val infos = array.toJSONObjectOrNullList().mapNotNull { it }
        val fileinfo = storage.fileInfoAt(cpath).let {
            it.first ?: return rsrc.jsonObjectError(it.second)
        }
        return deletedCounts(fileinfo.root.pruneHistory(infos, true))
    }

    private fun actionDeleteAll(params: JSONObject): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionDeleteInfo(params: JSONObject): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionDeleteEmptyDirs(params: JSONObject): JSONObject {
        return rsrc.jsonObjectError(R.string.UnsupportedOperation)
    }

    private fun actionLocalImageInfos(params: JSONObject): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionLocalImageThumbnails(params: JSONObject, callback: Any): JSONObject {
        TODO("Not yet implemented")
    }

    private fun actionListRecursive(params: JSONObject): JSONObject {
        val json = params.stringOrNull(An.Key.path) ?: throw AssertionError()
        val searchparams = JSONUtil.jsonObjectOrNull(json) ?: throw AssertionError()
        val cpath = searchparams.stringOrNull(An.Key.path) ?: throw AssertionError()
        val searchtext = searchparams.stringOrNull(An.Key.text) ?: throw AssertionError()
        val regex = Without.exceptionOrNull { Regex(searchtext) } ?: return rsrc.jsonObjectError(R.string.InvalidRegex_, searchtext)
        val cleanrpath = Support.getcleanrpathStrict(rsrc, cpath).let {
            it.first ?: rsrc.jsonError(it.second)
        }
        val ret = JSONObject()
        ret.put(An.Key.path, cpath)
        val dirtree = ret.putJSONArrayOrFail(An.Key.dirtree)
        val warns = JSONArray()
        val predicate = { info: IDeletedFileStat ->
            regex.containsMatchIn(Basepath.joinRpath(info.dir, info.name))
        }
        try {
            if (cleanrpath.isEmpty()) {
                for (info in storage.getHomeRoot().searchHistory("", predicate)) {
                    dirtree.put(info.toJSON())
                }
            } else {
                val fileinfo = storage.fileInfo(cleanrpath) ?: return rsrc.jsonObjectError(R.string.InvalidPath_, cpath)
                ret.put(An.Key.dirpath, JSONArray().put(FileInfoUtil.tojson(fileinfo)))
                for (info in fileinfo.root.searchHistory(
                        (if (cpath.endsWith(File.separator)) fileinfo.rpath + File.separator else fileinfo.rpath),
                        predicate
                )) {
                    dirtree.put(info.toJSON())
                }
            }
            return ret
        } catch (e: Exception) {
            return rsrc.jsonObjectError(e, R.string.CommandFailed)
        }
    }

    private fun toDeletedFileStat(info: IFileInfo): JSONObject {
        val ret = FileInfoUtil.tojson(info)
        ret.put(IFileInfo.Key.id, 0)
        ret.put(IFileInfo.Key.dir, info.name)
        ret.put(IFileInfo.Key.name, "")
        ret.put(IFileInfo.Key.lastDeleted, info.stat()!!.lastModified)
        return ret
    }

    private fun deletedCounts(result: Pair<Int, Int>): JSONObject {
        val (files, dirs) = result
        return JSONObject()
                .put(An.Key.total, files + dirs)
                .put(An.Key.count, files)
                .put(An.Key.result, String.format("%s %d %s, %d %s",
                        rsrc.get(R.string.Removed),
                        files, rsrc.get(R.string.file),
                        dirs, rsrc.get(R.string.dir)))

    }

    private fun recoverCounts(result: Pair<Int, Int>): JSONObject {
        val (oks, fails) = result
        return JSONObject()
                .put(An.Key.total, oks + fails)
                .put(An.Key.fails, fails)
                .put(An.Key.result, String.format("%s %d %s, %d %s",
                        rsrc.get(R.string.Recover),
                        oks, rsrc.get(R.string.OK),
                        fails, rsrc.get(R.string.failed)))

    }
}
