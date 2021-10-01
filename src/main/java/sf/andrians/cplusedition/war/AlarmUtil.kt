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

import com.cplusedition.anjson.JSONUtil.jsonObjectOrFail
import com.cplusedition.anjson.JSONUtil.putOrFail
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.WithoutUtil.Companion.Without
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.Serial
import sf.andrians.ancoreutil.util.FileUtil.asString
import sf.andrians.ancoreutil.util.FileUtil.writeFile
import sf.andrians.ancoreutil.util.struct.IterableWrapper.wrap
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.EventKey
import sf.andrians.cplusedition.support.An.EventRepeat
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.ObsoletedEventKey
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.handler.HandlerUtil
import sf.andrians.cplusedition.support.handler.IResUtil
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

internal object AlarmUtil {
    const val VERSION = 3
    private const val START = Conf.RequestCode.ALARM + 10.toLong()
    private const val END = Conf.RequestCode.ALARM + 0x100000.toLong()

    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private var settings: JSONObject? = null
    private var serial = Serial(START, END)
    private var delegate: IAlarmDelegate = object : IAlarmDelegate {
        override fun readSettings(context: Context): JSONObject {
            val file = getAlarmsJson(context)
            if (!file.exists()) {
                return createsettings(JSONObject())
            }
            try {
                val ret = JSONObject(asString(file))
                if (ret.optJSONObject(EventKey.Events) == null) {
                    return createsettings(ret)
                }
                val s = ret.optLong(EventKey.Serial, -1)
                if (s >= 0) {
                    serial = Serial(START, END, s)
                }
                return ret
            } catch (e: Throwable) {
                
                return createsettings(JSONObject())
            }
        }

        @Throws(IOException::class)
        override fun saveSettings(context: Context, settings: JSONObject) {
            val file = getAlarmsJson(context)
            try {
                settings.put(EventKey.Serial, serial.get())
            } catch (e: JSONException) {
                
            }
            writeFile(file, false, settings.toString())
        }

        override fun sendNotification(context: Context?, alarm: IAlarmInfo?) {}
        override fun setAlarm(context: Context?, alarm: JSONObject?) {
        }

        private fun getAlarmsJson(context: Context): File {
            return Conf.getAlarmJson(context.getDatadir())
        }

        override fun now(): Long {
            return System.currentTimeMillis()
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun reset() {
        settings = null
    }

    /** @return null if not logged in. */
    @JvmStatic
    fun getSettings(context: Context, storage: IStorage): String? {
        if (!storage.isLoggedIn()) {
            return null
        }
        lock.writeLock().lock()
        return try {
            return (this.settings ?: loadSettings(context)).toString()
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * @return A serialize JSON string of the alarms, not the settings, ie. without the metadata.
     */
    @JvmStatic
    fun getEvents(context: Context, storage: IStorage, refresh: Boolean, excludeExpired: Boolean): String {
        //// Require a write lock to update repeating alarms.
        lock.writeLock().lock()
        return try {
            val alarms = if (refresh) loadSettings(context).second else ensureSettingsLoaded(context).second
            val result = JSONObject()
            val now = delegate.now()
            for (id in wrap(alarms.keys())) {
                val alarm = alarms.getJSONObject(id)
                alarm.put(EventKey.Pending, isPending(now, alarm))
                if (excludeExpired) {
                    val ms = alarm.getLong(EventKey.Ms)
                    val lastms = alarm.optLong(EventKey.Lastms, -1L)
                    if (ms <= lastms) {
                        continue
                    }
                }
                val clone = JSONObject(alarm.toString())
                val href = alarm.stringOrDef(EventKey.Url, "")
                if (href.isNotEmpty()) {
                    val cpath = Without.exceptionOrNull { URI(href).path }
                    if (cpath != null && cpath.isNotEmpty()) {
                        val exists = (storage.fileInfoAt(cpath).first?.exists == true)
                        clone.put(EventKey.Exists, exists)
                        
                    }
                }
                result.put(id, clone)
            }
            val ret = JSONObject()
            ret.put(Key.result, result)
            ret.toString()
        } catch (e: Throwable) {
            HandlerUtil.jsonError(context.getString(R.string.EventsGetFailed))
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun exportEvents(context: Context, storage: IStorage, cpath: String): String {
        //// Require a write lock to update repeating alarms.
        lock.writeLock().lock()
        return try {
            val alarms = ensureSettingsLoaded(context).second
            val fileinfo = storage.fileInfoAt(cpath).let {
                it.first ?: return storage.rsrc.jsonError(it.second)
            }
            val stat = fileinfo.stat()
            if (stat != null && !stat.writable) return storage.rsrc.jsonError(R.string.destinationNotWritable_, cpath)
            val template = storage.fileInfoAt("assets/templates/todo/todo-v2.html").first!!.content().readText();
            val content = if (stat != null) fileinfo.content().readText() else null
            return JSONObject()
                    .put(Key.template, template)
                    .put(Key.text, content)
                    .put(Key.result, alarms).toString()
        } catch (e: Throwable) {
            HandlerUtil.jsonError(context.getString(R.string.ExportFailed))
        } finally {
            lock.writeLock().unlock()
        }
    }

    @JvmStatic
    fun getPendingAlarmCount(context: Context): Int {
        lock.writeLock().lock()
        return try {
            val alarms = ensureSettingsLoaded(context).second
            val now = delegate.now()
            var count = 0
            for (id in wrap(alarms.keys())) {
                val alarm = alarms.optJSONObject(id)
                if (isPending(now, alarm)) {
                    ++count
                }
            }
            count
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun isPending(now: Long, alarm: JSONObject): Boolean {
        if (alarm.optBoolean(EventKey.Done)
                || alarm.stringOrNull(EventKey.Repeat) == EventRepeat.Off
        ) {
            return false
        }
        if (EventRepeat.Once == alarm.stringOrNull(EventKey.Repeat)) {
            if (alarm.optLong(EventKey.Ms) <= now) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    @Throws(Exception::class)
    fun importAlarms(context: Context, json: String?) {
        
        if (json == null) {
            return
        }
        lock.writeLock().lock()
        try {
            val alarms = ensureSettingsLoaded(context).second
            val input = JSONObject(json)
            val a = input.optJSONObject(EventKey.Events)
            if (a == null || input.optInt(EventKey.Version, -1) < 0) {
                throw IOException("Invalid alarms file")
            }
            val it = a.keys()
            while (it.hasNext()) {
                val id = it.next()
                
                val alarm = a.optJSONObject(id)
                if (alarm.optLong(EventKey.Ms, -1) < 0 || alarm.stringOrNull(EventKey.Id) == null
                        || alarm.stringOrNull(EventKey.Repeat) == null) {
                    
                    continue
                }
                alarms.put(id, alarm)
            }
            delegate.saveSettings(context, this.settings!!)
            updatePending(context, alarms)
        } finally {
            lock.writeLock().unlock()
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun clearAlarms(context: Context) {
        
        lock.writeLock().lock()
        try {
            val alarms = JSONObject()
            val settings = createsettings(alarms).also { this.settings = it }
            delegate.saveSettings(context, settings)
            updatePending(context, alarms)
        } finally {
            lock.writeLock().unlock()
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun postEvent(context: Context, json: String?): String? {
        
        if (json == null) {
            return context.getString(R.string.InvalidEvent)
        }
        lock.writeLock().lock()
        return try {
            val events = ensureSettingsLoaded(context).second
            val event = JSONObject(json)
            //// Make sure the alarm is sane.
            val id = event.stringOrNull(EventKey.Id)
            val ms = event.optLong(EventKey.Ms, -1L)
            val repeat = event.stringOrNull(EventKey.Repeat)
            if (id == null || ms < 0 || repeat == null) {
                return context.getString(R.string.InvalidEvent)
            }
            //// Make sure repeating alarms trigger in the future.
            val now = delegate.now()
            event.put(EventKey.Lastms, now)
            updateRepeatingAlarm(context, now, event)
            
            events.put(id, event)
            delegate.saveSettings(context, this.settings!!)
            updatePending(context, events)
            null
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * @param json
     * A JSONArray of alarm id.
     */
    @JvmStatic
    fun removeAlarms(context: Context, json: String): String {
        
        lock.writeLock().lock()
        var ret: String
        return try {
            val alarms = ensureSettingsLoaded(context).second
            try {
                var count = 0
                val ids = JSONArray(json)
                var i = 0
                val len = ids.length()
                while (i < len) {
                    val id = ids.stringOrNull(i)
                    val old = alarms.remove(id)
                    if (old != null) {
                        ++count
                    }
                    ++i
                }
                val result = JSONObject()
                result.put(Key.result, count)
                ret = result.toString()
            } catch (e: Throwable) {
                val msg = context.getString(R.string.EventsRemoveFailed)
                ret = HandlerUtil.jsonError(msg)
                
            }
            delegate.saveSettings(context, this.settings!!)
            updatePending(context, alarms)
            ret
        } catch (e: Exception) {
            val msg = context.getString(R.string.EventsRemoveFailed)
            
            HandlerUtil.jsonError(msg)
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Throws(Exception::class)
    fun restoreAlarms(context: Context) {
        
        lock.writeLock().lock()
        try {
            val (settings, alarms) = ensureSettingsLoaded(context)
            if (updateRepeatingAlarms(context)) {
                delegate.saveSettings(context, alarms)
            }
            updatePending(context, settings)
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun onAlarm(context: Context, id: String?, ms: Long) {
        if (id == null || ms < 0) {
            return
        }
        
        lock.writeLock().lock()
        try {
            val (settings, alarms) = ensureSettingsLoaded(context)
            try {
                val alarm = alarms.getJSONObject(id) ?: return
                delegate.sendNotification(context, wrap(alarm))
                val ams = alarm.getLong(EventKey.Ms)
                val alastms = alarm.optLong(EventKey.Lastms, -1L)
                if (ms == ams && ms > alastms) {
                    alarm.put(EventKey.Lastms, ms)
                    updateRepeatingAlarm(context, ms, alarm)
                    val newms = alarm.getLong(EventKey.Ms)
                    
                }
            } catch (e: Throwable) {
                
            }
            updateRepeatingAlarms(context)
            delegate.saveSettings(context, settings)
            updatePending(context, alarms)
        } catch (e: Exception) {
            
        } finally {
            lock.writeLock().unlock()
        }
    }

    internal fun updateRepeatingAlarms(context: Context): Boolean {
        
        lock.writeLock().lock()
        var modified = false
        try {
            val alarms = ensureSettingsLoaded(context).second
            val now = delegate.now()
            for (key in alarms.keys().asSequence()) {
                var alarm: JSONObject
                try {
                    alarm = alarms.getJSONObject(key)
                    if (updateRepeatingAlarm(context, now, alarm)) {
                        modified = true
                    }
                } catch (e: JSONException) {
                    
                }
            }
        } finally {
            lock.writeLock().unlock()
        }
        return modified
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Select the next alarm to go off.
     */
    private fun updatePending(context: Context, alarms: JSONObject): JSONObject? {
        var pendingms = Long.MAX_VALUE
        var pending: JSONObject? = null
        for (id in wrap(alarms.keys())) {
            try {
                val alarm = alarms.getJSONObject(id)
                val ams = alarm.getLong(EventKey.Ms)
                val lastms = alarm.optLong(EventKey.Lastms, -1L)
                val disabled = alarm.optBoolean(EventKey.Done)
                if (ams <= lastms || disabled) {
                    continue
                }
                if (pending == null || ams < pendingms) {
                    pendingms = ams
                    pending = alarm
                }
            } catch (e: JSONException) {
                
            }
        }
        delegate.setAlarm(context, pending)
        return pending
    }

    /**
     * If the alarm is a repeating alarm, advance the alarm to a time in the future if necessary. If it should go off since lastms,
     * send ONE notification for the most recent trigger.
     *
     * @param alarm
     * @return The true if the alarm time, ms, is changed.
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun updateRepeatingAlarm(context: Context, now: Long, alarm: JSONObject): Boolean {
        
        val repeat = alarm.stringOrNull(EventKey.Repeat)
        val done = alarm.optBoolean(EventKey.Done)
        if (done || repeat == null || EventRepeat.Off == repeat || EventRepeat.Once == repeat) {
            return false
        }
        var ms = alarm.getLong(EventKey.Ms)
        val lastms = alarm.optLong(EventKey.Lastms, -1L)
        while (ms <= lastms) {
            ms = advanceRepeatingAlarm(alarm, repeat, ms)
        }
        if (ms < now) {
            //// We have a missed trigger, skip till we get the last trigger just before now.
            var notifyms = ms
            while (advanceRepeatingAlarm(alarm, repeat, ms).also { ms = it } < now) {
                notifyms = ms
            }
            alarm.put(EventKey.Lastms, notifyms)
            alarm.put(EventKey.Ms, notifyms)
            //// Send one notification, for the latest expired trigger.
            delegate.sendNotification(context, wrap(alarm))
        }
        alarm.put(EventKey.Ms, ms)
        return true
    }

    @Throws(JSONException::class)
    private fun advanceRepeatingAlarm(alarm: JSONObject, repeat: String, ams: Long): Long {
        
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
        alarm.put(EventKey.Ms, ms)
        return ms
    }

    fun wrap(alarm: JSONObject): IAlarmInfo {
        return JSONWrapper(alarm)
    }

    private fun ensureSettingsLoaded(context: Context): Pair<JSONObject, JSONObject> {
        val settings = this.settings ?: return loadSettings(context)
        val alarms = settings.jsonObjectOrFail(EventKey.Events)
        return Pair(settings, alarms)
    }

    private fun loadSettings(context: Context): Pair<JSONObject, JSONObject> {
        val settings = delegate.readSettings(context).also { this.settings = it }
        var alarms = settings.optJSONObject(EventKey.Events)
        if (alarms == null) {
            alarms = JSONObject()
            settings.putOrFail(EventKey.Events, alarms)
        } else {
            fixupAlarms(alarms)
        }
        return Pair(settings, alarms)
    }

    private fun fixupAlarms(alarms: JSONObject) {
        for (id in alarms.keys()) {
            val alarm = alarms.getJSONObject(id)
            val filepath = alarm.stringOrNull(ObsoletedEventKey.Filepath)
            if (filepath != null) {
                val fragment = alarm.stringOrNull(ObsoletedEventKey.Fragment)
                try {
                    val href = URI(null, null, filepath, fragment).toString()
                    alarm.put(EventKey.Url, href)
                    alarm.put(ObsoletedEventKey.Filepath, null)
                    alarm.put(ObsoletedEventKey.Fragment, null)
                } catch (e: Exception) {
                }
            }
            if (alarm.optBoolean(ObsoletedEventKey.AlarmOff)) {
                alarm.put(ObsoletedEventKey.AlarmOff, null)
                alarm.put(EventKey.Repeat, EventRepeat.Off);
            }
        }
    }

    internal class Intent
    internal class Context(
            private val datadir: File,
            val res: IResUtil
    ) {

        fun getDatadir(): File {
            return datadir
        }

        fun getString(id: Int): String {
            return res.get(id)
        }
    }

    internal interface IAlarmDelegate {
        fun now(): Long
        fun readSettings(context: Context): JSONObject

        @Throws(Exception::class)
        fun saveSettings(context: Context, settings: JSONObject)
        fun sendNotification(context: Context?, alarm: IAlarmInfo?)
        fun setAlarm(context: Context?, alarm: JSONObject?)
    }

    internal interface IAlarmInfo {
        fun optString(key: String?): String?
        fun optLong(key: String?, def: Long): Long
    }

    internal class JSONWrapper(private val alarm: JSONObject) : IAlarmInfo {
        override fun optString(key: String?): String? {
            return if (key == null) null else alarm.stringOrNull(key)
        }

        override fun optLong(key: String?, def: Long): Long {
            return alarm.optLong(key, def)
        }

    }

    private fun createsettings(alarms: JSONObject?): JSONObject {
        return JSONObject().putOrFail(EventKey.Version, VERSION)
                .putOrFail(EventKey.Events, alarms)
    }

    ////////////////////////////////////////////////////////////////////////

    fun setDelegate(d: IAlarmDelegate) {
    }

    ////////////////////////////////////////////////////////////////////////
}
