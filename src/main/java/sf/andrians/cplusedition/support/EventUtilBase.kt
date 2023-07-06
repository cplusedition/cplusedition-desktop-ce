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
package sf.andrians.cplusedition.support

import com.cplusedition.anjson.JSONUtil.foreachStringNotNull
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.With
import com.cplusedition.bot.core.Without
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.EventKey
import sf.andrians.cplusedition.support.An.EventRepeat
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.ObsoletedEventKey
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.locks.ReentrantLock

abstract class EventUtilBase constructor(
    protected var storage: IStorage,
    delegate: IEventDelegate,
) {
    companion object {
        const val VERSION = 3
        const val EventsJson = "Home/etc/events.json"
        const val START: Long = 1000
        const val END = Long.MAX_VALUE

        fun createsettings(events: JSONObject): Pair<JSONObject, JSONObject> {
            val settings = JSONObject().putOrFail(EventKey.Version, VERSION)
                .putOrFail(EventKey.Events, events)
            return Pair(settings, events)
        }

        fun wrap(event: JSONObject): IEventInfo {
            return JSONWrapper(event)
        }

        fun fixupEvents(events: JSONObject): JSONObject {
            for (id in events.keys()) {
                val event = events.getJSONObject(id)
                val filepath = event.stringOrNull(ObsoletedEventKey.Filepath)
                if (filepath != null) {
                    val fragment = event.stringOrNull(ObsoletedEventKey.Fragment)
                    try {
                        val href = URI(null, null, filepath, fragment).toString()
                        event.put(EventKey.Url, href)
                        event.put(ObsoletedEventKey.Filepath, JSONObject.NULL)
                        event.put(ObsoletedEventKey.Fragment, JSONObject.NULL)
                    } catch (e: Exception) {
                    }
                }
                if (event.optBoolean(ObsoletedEventKey.AlarmOff)) {
                    event.put(ObsoletedEventKey.AlarmOff, JSONObject.NULL)
                    event.put(EventKey.Repeat, EventRepeat.Off);
                }
            }
            return events
        }
    }

    private val lock = ReentrantLock()
    private var settings: Pair<JSONObject, JSONObject>? = null
    private var delegate: IEventDelegate

    init {
        this.delegate = delegate
    }

    ////////////////////////////////////////////////////////////////////////

    fun reset() {
        With.lock(lock) {
            settings = null
        }
    }

    /**
     * @return A serialize JSON string of the events, not the settings, ie. without the metadata.
     */
    fun getEvents(refresh: Boolean, excludeDone: Boolean): JSONObject {
        //// Require a write lock to update repeating events.
        lock.lock()
        return try {
            val events = ensureSettingsLoaded(refresh).second
            val result = JSONObject()
            val now = delegate.now()
            for (id in events.keys()) {
                val event = events.getJSONObject(id)
                event.put(EventKey.Pending, isPending(now, event))
                if (excludeDone) {
                    if (event.optBoolean(EventKey.Done))
                        continue
                }
                val clone = JSONObject(event.toString())
                val href = event.stringOrDef(EventKey.Url, "")
                if (href.isNotEmpty()) {
                    val cpath = Without.exceptionOrNull { URI(href).path }
                    if (!cpath.isNullOrEmpty()) {
                        val exists = (storage.fileInfoAt(cpath).result()?.exists == true)
                        clone.put(EventKey.Exists, exists)
                        
                    }
                }
                result.put(id, clone)
            }
            JSONObject().put(Key.result, result)
        } catch (e: Throwable) {
            storage.rsrc.jsonObjectError(storage.rsrc.get(R.string.EventsGetFailed))
        } finally {
            lock.unlock()
        }
    }

    fun getPendingEventCount(): Int {
        return With.lock(lock) {
            val events = ensureSettingsLoaded().second
            val now = delegate.now()
            var count = 0
            for (id in events.keys()) {
                val event = events.optJSONObject(id)
                if (isPending(now, event)) {
                    ++count
                }
            }
            count
        }
    }

    private fun isPending(now: Long, event: JSONObject): Boolean {
        if (event.optBoolean(EventKey.Done)
            || event.stringOrNull(EventKey.Repeat) == EventRepeat.Off
        ) {
            return false
        }
        if (EventRepeat.Once == event.stringOrNull(EventKey.Repeat)) {
            if (event.optLong(EventKey.Ms) <= now) {
                return false
            }
        }
        return true
    }

    @Throws(Exception::class)
    fun importEvents(json: String?) {
        
        if (json == null) {
            return
        }
        lock.lock()
        try {
            val pair = ensureSettingsLoaded()
            val events = pair.second
            val input = JSONObject(json)
            val a = input.optJSONObject(EventKey.Events)
            if (a == null || input.optInt(EventKey.Version, -1) < 0) {
                throw IOException("Invalid events file")
            }
            val it = a.keys()
            while (it.hasNext()) {
                val id = it.next()
                
                val event = a.optJSONObject(id)
                if (event.optLong(EventKey.Ms, -1) < 0 || event.stringOrNull(EventKey.Id) == null
                    || event.stringOrNull(EventKey.Repeat) == null
                ) {
                    
                    continue
                }
                events.put(id, event)
            }
            updatePending(events)
            delegate.saveSettings(pair.first)
        } finally {
            lock.unlock()
        }
    }

    @Throws(Exception::class)
    fun clearEvents() {
        
        lock.lock()
        try {
            val events = JSONObject()
            delegate.saveSettings(createsettings(events).first)
            updatePending(events)
        } finally {
            lock.unlock()
        }
    }

    @Throws(Exception::class)
    fun postEvent(json: String?): String? {
        
        if (json == null) {
            return storage.rsrc.get(R.string.InvalidEvent)
        }
        lock.lock()
        return try {
            val pair = ensureSettingsLoaded()
            val events = pair.second
            val event = JSONObject(json)
            //// Make sure the event is sane.
            val id = event.stringOrNull(EventKey.Id)
            val ms = event.optLong(EventKey.Ms, -1L)
            val repeat = event.stringOrNull(EventKey.Repeat)
            if (id == null || ms < 0 || repeat == null) {
                return storage.rsrc.get(R.string.InvalidEvent)
            }
            //// Make sure repeating events  trigger in the future.
            val now = delegate.now()
            event.put(EventKey.Lastms, now)
            updateRepeatingEvent(now, event)
            
            events.put(id, event)
            updatePending(events)
            delegate.saveSettings(pair.first)
            null
        } finally {
            lock.unlock()
        }
    }

    /**
     * @param json
     * A JSONArray of event id.
     */
    fun removeEvents(ids: JSONArray): JSONObject {
        
        return With.lock(lock) {
            val pair = ensureSettingsLoaded()
            val events = pair.second
            try {
                var count = 0
                ids.foreachStringNotNull { _, id ->
                    if (events.remove(id) != null) {
                        ++count
                    }
                }
                JSONObject().put(Key.result, count)
            } catch (e: Throwable) {
                val msg = storage.rsrc.get(R.string.EventsRemoveFailed)
                
                storage.rsrc.jsonObjectError(msg)
            } finally {
                updatePending(events)
                delegate.saveSettings(pair.first)
            }
        }
    }

    @Throws(Exception::class)
    fun restoreEvents() {
        
        lock.lock()
        try {
            val (settings, events) = ensureSettingsLoaded()
            if (updateRepeatingEvents()) {
                delegate.saveSettings(events)
            }
            updatePending(settings)
        } finally {
            lock.unlock()
        }
    }

    fun onAlarm(id: String?, ms: Long) {
        if (id == null || ms < 0) {
            return
        }
        
        lock.lock()
        try {
            val (settings, events) = ensureSettingsLoaded()
            try {
                val event = events.getJSONObject(id) ?: return
                delegate.sendNotification(wrap(event))
                val ams = event.getLong(EventKey.Ms)
                val alastms = event.optLong(EventKey.Lastms, -1L)
                if (ms == ams && ms > alastms) {
                    event.put(EventKey.Lastms, ms)
                    updateRepeatingEvent(ms, event)
                    val newms = event.getLong(EventKey.Ms)
                    
                }
            } catch (e: Throwable) {
                
            }
            updateRepeatingEvents()
            delegate.saveSettings(settings)
            updatePending(events)
        } catch (e: Exception) {
            
        } finally {
            lock.unlock()
        }
    }

    internal fun updateRepeatingEvents(): Boolean {
        
        lock.lock()
        var modified = false
        try {
            val events = ensureSettingsLoaded().second
            val now = delegate.now()
            for (key in events.keys().asSequence()) {
                var event: JSONObject
                try {
                    event = events.getJSONObject(key)
                    if (updateRepeatingEvent(now, event)) {
                        modified = true
                    }
                } catch (e: JSONException) {
                    
                }
            }
        } finally {
            lock.unlock()
        }
        return modified
    }

    ////////////////////////////////////////////////////////////////////////

    private fun updatePending(events: JSONObject): JSONObject? {
        var pendingms = Long.MAX_VALUE
        var pending: JSONObject? = null
        for (id in events.keys()) {
            try {
                val event = events.getJSONObject(id)
                val ams = event.getLong(EventKey.Ms)
                val lastms = event.optLong(EventKey.Lastms, -1L)
                val disabled = event.optBoolean(EventKey.Done)
                if (ams <= lastms || disabled) {
                    continue
                }
                if (pending == null || ams < pendingms) {
                    pendingms = ams
                    pending = event
                }
            } catch (e: JSONException) {
                
            }
        }
        if (pending != null) delegate.setAlarm(pending)
        return pending
    }

    /**
     * If the event is a repeating event, advance the event to a time in the future if necessary. If it should go off since lastms,
     * send ONE notification for the most recent trigger.
     *
     * @param event
     * @return The true if the event time, ms, is changed.
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun updateRepeatingEvent(now: Long, event: JSONObject): Boolean {
        
        val repeat = event.stringOrNull(EventKey.Repeat)
        val done = event.optBoolean(EventKey.Done)
        if (done || repeat == null || EventRepeat.Off == repeat || EventRepeat.Once == repeat) {
            return false
        }
        var ms = event.getLong(EventKey.Ms)
        val lastms = event.optLong(EventKey.Lastms, -1L)
        while (ms <= lastms) {
            ms = advanceRepeatingEvent(event, repeat, ms)
        }
        if (ms < now) {
            //// We have a missed trigger, skip till we get the last trigger just before now.
            var notifyms = ms
            while (advanceRepeatingEvent(event, repeat, ms).also { ms = it } < now) {
                notifyms = ms
            }
            event.put(EventKey.Lastms, notifyms)
            event.put(EventKey.Ms, notifyms)
            //// Send one notification, for the latest expired trigger.
            delegate.sendNotification(wrap(event))
        }
        event.put(EventKey.Ms, ms)
        return true
    }

    @Throws(JSONException::class)
    private fun advanceRepeatingEvent(event: JSONObject, repeat: String, ams: Long): Long {
        
        val cal = Calendar.getInstance()
        cal.timeInMillis = ams
        when (repeat) {
            EventRepeat.Off, EventRepeat.Once -> {
            }

            EventRepeat.Daily -> cal.add(Calendar.HOUR, 24)
            EventRepeat.Workdays -> {
                cal.add(Calendar.HOUR, 24)
                when (cal[Calendar.DAY_OF_WEEK]) {
                    Calendar.SATURDAY -> cal.add(Calendar.HOUR, 2 * 24)
                    Calendar.SUNDAY -> cal.add(Calendar.HOUR, 24)
                    else -> {
                    }
                }
            }

            EventRepeat.Weekly -> cal.add(Calendar.HOUR, 24 * 7)
            EventRepeat.Monthly -> cal.add(Calendar.MONTH, 1)
            EventRepeat.Yearly -> cal.add(Calendar.YEAR, 1)
            else -> throw AssertionError()
        }
        val ms = cal.timeInMillis
        event.put(EventKey.Ms, ms)
        return ms
    }

    fun wrap(event: JSONObject): IEventInfo {
        return JSONWrapper(event)
    }

    private fun ensureSettingsLoaded(force: Boolean = false): Pair<JSONObject, JSONObject> {
        this.settings.let {
            return if (it == null || force) delegate.readSettings() else it
        }
    }

    interface IEventDelegate {
        fun now(): Long
        fun readSettings(): Pair<JSONObject, JSONObject>
        @Throws(Exception::class)
        fun saveSettings(settings: JSONObject)
        fun sendNotification(event: IEventInfo)
        fun setAlarm(event: JSONObject)
    }

    interface IEventInfo {
        fun optString(key: String): String?
        fun optString(key: String, def: String): String
        fun optLong(key: String, def: Long): Long
    }

    internal class JSONWrapper(private val event: JSONObject) : IEventInfo {
        override fun optString(key: String): String? {
            return event.stringOrNull(key)
        }

        override fun optString(key: String, def: String): String {
            return event.stringOrNull(key) ?: def
        }

        override fun optLong(key: String, def: Long): Long {
            return event.optLong(key, def)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun setDelegate(d: IEventDelegate) {
    }

    ////////////////////////////////////////////////////////////////////////
}
