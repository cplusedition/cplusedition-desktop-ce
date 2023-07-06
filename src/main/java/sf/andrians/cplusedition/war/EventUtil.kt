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

import com.cplusedition.bot.core.Serial
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.support.An.EventKey
import sf.andrians.cplusedition.support.EventUtilBase
import sf.andrians.cplusedition.support.EventUtilBase.Companion.END
import sf.andrians.cplusedition.support.EventUtilBase.Companion.START
import sf.andrians.cplusedition.support.EventUtilBase.Companion.createsettings
import sf.andrians.cplusedition.support.EventUtilBase.IEventDelegate
import sf.andrians.cplusedition.support.EventUtilBase.IEventInfo
import sf.andrians.cplusedition.support.IStorage
import java.io.IOException

internal class EventUtil constructor(
    storage: IStorage,
) : EventUtilBase(storage, Delegate(storage))

private class Delegate constructor(private val storage: IStorage) : IEventDelegate {

    private var serial = Serial(START, END)
    override fun readSettings(): Pair<JSONObject, JSONObject> {
        val file = storage.fileInfoAt(EventUtilBase.EventsJson).result()
        try {
            if (file == null || !file.exists) {
                val settings = storage.getSettingsStore().invoke { ssa ->
                    ssa.getEvents()?.also {
                        ssa.deleteEvents()
                    }
                } ?: return createsettings(JSONObject())
                return readsettings1(settings)
            }
            val settings = JSONObject(file.content().readText())
            return readsettings1(settings)
        } catch (e: Throwable) {
            
            return createsettings(JSONObject())
        }
    }

    private fun readsettings1(settings: JSONObject): Pair<JSONObject, JSONObject> {
        val events = settings.optJSONObject(EventKey.Events)
            ?:
            return createsettings(EventUtilBase.fixupEvents(settings))
        val s = settings.optLong(EventKey.Serial, -1)
        if (s >= 0) {
            serial = Serial(START, END, s)
        }
        return Pair(settings, events)
    }

    @Throws(IOException::class)
    override fun saveSettings(settings: JSONObject) {
        val file = storage.fileInfoAt(EventUtilBase.EventsJson).result()
            ?: return
        try {
            val json = settings.toString(2)
            if (file.exists && file.content().readText() == json)
                return
            settings.put(EventKey.Serial, serial.get())
            json.byteInputStream().use { file.content().write(it) }
        } catch (e: JSONException) {
            
        }
    }

    override fun sendNotification(event: IEventInfo) {}

    override fun setAlarm(event: JSONObject) {
    }

    override fun now(): Long {
        return System.currentTimeMillis()
    }
}
