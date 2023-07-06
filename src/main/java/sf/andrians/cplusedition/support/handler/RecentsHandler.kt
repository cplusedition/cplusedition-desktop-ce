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
import com.cplusedition.anjson.JSONUtil.jsonObjectOrNull
import com.cplusedition.anjson.JSONUtil.stringOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.*
import sf.andrians.cplusedition.support.An.SessionParam
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class RecentsHandler(context: ICpluseditionContext) : IRecentsHandler {

    private val storage: IStorage = context.getStorage()
    private val recentsRoot: IRecentsRoot = context.getRecentsRoot()
    private val recentsByTime = TreeMap<String, IRecentsInfo>()
    private val lock = ReentrantLock()

    @Throws(Exception::class)
    override fun handle(cmd: Int): JSONObject {
        return lock.withLock {
            try {
                when (cmd) {
                    An.RecentsCmd.CLEAR -> actionRecentsClear()
                    An.RecentsCmd.CLEAN -> actionRecentsClean()
                    An.RecentsCmd.INFO -> actionRecentsInfo()
                    An.RecentsCmd.SORTED -> actionRecentsSorted()
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
                            storage.rsrc.jsonObjectError(R.string.RecentsNoMoreHistoryAvailable)
                        } else if (!info.exists) {
                            storage.rsrc.jsonObjectError(R.string.RecentsHistoryEntryNoLongerExists)
                        } else {
                            info.toJSON()
                        }
                    }
                    else -> storage.rsrc.jsonObjectError(R.string.RecentsInvalidCommand)
                }
            } catch (e: Throwable) {
                storage.rsrc.jsonObjectError(R.string.CommandFailed)
            }
        }
    }

    /// @param cpath A context relative path with leading /.
    override fun recentsPut(navigation: Int, cpath: String, state: JSONObject?, timestamp: Long): JSONObject {
        return lock.withLock {
            try {
                val fileinfo = storage.fileInfoAt(cpath).result()
                    ?: return storage.rsrc.jsonObjectInvalidPath(cpath)
                val info = RecentsInfoWrapper(fileinfo, cpath, timestamp, state)
                recentsRoot.put(navigation, cpath, info)
                recentsByTime.put(labelOf(info), info)
                if (recentsByTime.size > Support.Def.recentsSize) {
                    recentsByTime.entries.toList().minByOrNull { it.value.timestamp }?.let {
                        recentsByTime.remove(it.key)
                    }
                }
                JSONObject()
            } catch (e: Throwable) {
                
                storage.rsrc.jsonObjectError(R.string.CommandFailed)
            }
        }
    }

    @Throws(Exception::class)
    override fun recentsSave(session: JSONObject) {
        lock.withLock {
            val recents = JSONArray()
            for (info in recentsRoot.listFiles(ArrayList())) {
                recents.put(info.toJSON())
            }
            val bytime = JSONObject()
            for ((label, info) in recentsByTime.entries) {
                bytime.put(label, info.toJSON())
            }
            session.put(An.SessionKey.recents, recents)
            session.put(An.SessionKey.recentsByTime, bytime)
        }
    }

    override fun recentsRestore(session: JSONObject) {
        lock.withLock {
            fun fromjson(json: JSONObject?): IRecentsInfo? {
                if (json == null) return null
                val cpath = json.stringOrNull(IFileInfo.Key.name) ?: return null
                val state = json.optJSONObject(IFileInfo.Key.state)
                val lastused = json.optLong(IFileInfo.Key.lastUsed)
                val fileinfo = storage.fileInfoAt(cpath).result() ?: return null
                return RecentsInfoWrapper(fileinfo, cpath, lastused, state)
            }
            try {
                recentsRoot.clear()
                session.jsonArrayOrNull(An.SessionKey.recents)?.let {
                    var i = it.length()
                    while (--i >= 0) {
                        val info = fromjson(it.jsonObjectOrNull(i)) ?: continue
                        recentsRoot.put(An.RecentsCmd.INFO, info.cpath, info)
                    }
                }
            } catch (e: Throwable) {
                recentsRoot.clear()
                
            }
            try {
                recentsByTime.clear()
                session.jsonObjectOrNull(An.SessionKey.recentsByTime)?.let {
                    for (key in it.keys()) {
                        val info = fromjson(it.jsonObjectOrNull(key)) ?: continue
                        recentsByTime.put(key, info)
                    }
                }
            } catch (e: Throwable) {
                recentsByTime.clear()
                
            }
        }
    }

    @Throws(JSONException::class)
    private fun actionRecentsClear(): JSONObject {
        recentsRoot.clear()
        recentsByTime.clear()
        return recentsinfo(recentsRoot)
    }

    @Throws(JSONException::class)
    private fun actionRecentsClean(): JSONObject {
        recentsRoot.clean()
        val it = recentsByTime.values.iterator()
        while (it.hasNext()) {
            val info = it.next()
            if (!info.exists) it.remove()
        }
        return recentsinfo(recentsRoot)
    }

    @Throws(JSONException::class)
    private fun actionRecentsInfo(): JSONObject {
        return recentsinfo(recentsRoot)
    }

    @Throws(JSONException::class)
    private fun actionRecentsSorted(): JSONObject {
        val ret = JSONObject()
        val dirtree = JSONArray()
        ret.put(An.Key.dirtree, dirtree)
        val visited = TreeSet<String>()
        for ((label, info) in recentsByTime.entries) {
            if (visited.add(label)) dirtree.put(info.toJSON())
        }
        return ret
    }

    private fun labelOf(info: IRecentsInfo): String {
        val offset = info.state?.optInt(SessionParam.y, 0) ?: 0
        return info.cpath + (if (offset > 0) "@$offset" else "")
    }

    @Throws(JSONException::class)
    private fun recentsinfo(dir: IRecentsRoot): JSONObject {
        val ret = JSONObject()
        val dirtree = JSONArray()
        ret.put(An.Key.dirtree, dirtree)
        dir.listAll(dirtree)
        return ret
    }

}
