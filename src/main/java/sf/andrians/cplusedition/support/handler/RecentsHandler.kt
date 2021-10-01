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

import com.cplusedition.anjson.JSONUtil.stringOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IRecentsRoot
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.RecentsInfoWrapper
import sf.andrians.cplusedition.support.Support

class RecentsHandler(context: ICpluseditionContext, private val filepickerHandler: IFilepickerHandler) : IRecentsHandler {

    private val storage: IStorage = context.getStorage()
    private val recentsRoot: IRecentsRoot = context.getRecentsRoot()

    @Throws(Exception::class)
    override fun handleRecents(cmd: Int): String {
        val rsrc = storage.rsrc
        return when (cmd) {
            An.RecentsCmd.CLEAR -> actionRecentsClear()
            An.RecentsCmd.CLEAN -> actionRecentsClean()
            An.RecentsCmd.INFO -> actionRecentsInfo()
            An.RecentsCmd.BACK, An.RecentsCmd.PEEK, An.RecentsCmd.FORWARD -> {
                var info: IFileInfo? = null
                when (cmd) {
                    An.RecentsCmd.BACK -> do {
                        info = recentsRoot.back()
                    } while (info != null && !info.exists)
                    An.RecentsCmd.PEEK -> info = recentsRoot.peek()
                    An.RecentsCmd.FORWARD -> do {
                        info = recentsRoot.forward()
                    } while (info != null && !info.exists)
                }
                if (info == null) {
                    rsrc.jsonError(R.string.RecentsNoMoreHistoryAvailable)
                } else if (!info.exists) {
                    rsrc.jsonError(R.string.RecentsHistoryEntryNoLongerExists)
                } else {
                    info.toJSON().toString()
                }
            }
            else -> rsrc.jsonError(R.string.RecentsInvalidCommand)
        }
    }

    @Throws(JSONException::class)
    fun actionRecentsClear(): String {
        recentsRoot.clear()
        return recentsinfo(recentsRoot)
    }

    @Throws(JSONException::class)
    fun actionRecentsClean(): String {
        recentsRoot.clean()
        return recentsinfo(recentsRoot)
    }

    @Throws(JSONException::class)
    fun actionRecentsInfo(): String {
        return recentsinfo(recentsRoot)
    }

    /// @param cpath A context relative path with leading /.
    override fun recentsPut(navigation: Int, cpath: String, state: JSONObject?) {
        val fileinfo = storage.fileInfoAt(cpath).first ?: return
        recentsRoot.put(navigation, cpath, RecentsInfoWrapper(fileinfo, cpath, state))
    }

    @Throws(Exception::class)
    override fun recentsSave(state: JSONObject) {
        val recents = JSONArray()
        for (info in recentsRoot.listFiles(ArrayList())) {
            recents.put(info.toJSON())
        }
        state.put(An.SessionKey.recents, recents)
        state.put(An.SessionKey.loggedin, storage.isLoggedIn())
    }

    override fun recentsRestore(recents: JSONArray?) {
        recentsRoot.clear()
        try {
            if (recents != null) {
                var i = recents.length()
                while (--i >= 0) {
                    val info = recents.getJSONObject(i)
                    val name = info.stringOrNull(IFileInfo.Key.name) ?: continue
                    val st = info.getJSONObject(IFileInfo.Key.state)
                    recentsPut(An.RecentsCmd.INFO, name, st)
                }
            }
        } catch (e: JSONException) {
            recentsRoot.clear()
            
        }
    }

    @Throws(JSONException::class)
    private fun recentsinfo(dir: IRecentsRoot): String {
        val ret = JSONObject()
        val dirtree = JSONArray()
        ret.put(An.Key.dirtree, dirtree)
        dir.listAll(dirtree)
        return ret.toString()
    }

    companion object {
        @Throws(JSONException::class)
        fun removeLocked(storage: IStorage, recents: JSONArray?): JSONArray {
            val ret = JSONArray()
            if (recents != null) {
                var i = recents.length()
                while (--i >= 0) {
                    val info = recents.getJSONObject(i)
                    val name = info.stringOrNull(IFileInfo.Key.name)
                    if (name == null /* || storage.isLocked(name) */) {
                        continue
                    }
                    ret.put(info)
                }
            }
            return ret
        }
    }
}
