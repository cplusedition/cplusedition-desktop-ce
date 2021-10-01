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

import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback

class FilepickerHandler(
        storage: IStorage,
        private val ajax: IAjax?,
        private val thumbnailCallback: IThumbnailCallback
) : FilepickerHandlerBase(storage) {

    @Throws(Exception::class)
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

    fun handle1(cmd: Int, params: JSONObject): JSONObject {
        try {
            return (when (cmd) {
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
                An.FilepickerCmd.FILEINFOS -> actionFileInfos(params)
                An.FilepickerCmd.DIRINFO -> actionDirInfo(params)
                else -> rsrc.jsonObjectError(R.string.InvalidFilepickerCommand_, cmd.toString())
            })
        } catch (e: Exception) {
            return rsrc.jsonObjectError(e, R.string.CommandFailed, "$cmd")
        }
    }

}
