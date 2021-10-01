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
package sf.andrians.ancoreutil.util

import sf.andrians.ancoreutil.util.text.TextUtil
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TestWatch {

    ////////////////////////////////////////////////////////////////////////

    private val timer = StepWatch(true)

    ////////////////////////////////////////////////////////////////////////

    var isDebug = false
        private set
    var isPassed = false
        private set
    private var logs = ArrayList<String>()

    ////////////////////////////////////////////////////////////////////////

    constructor(debug: Boolean) {
        init(debug, null)
    }

    constructor(c: Class<*>) {
        var debug = false
        debug = try {
            val f = c.getField("DEBUG")
            f.getBoolean(null)
        } catch (e: Throwable) {
            false
        }
        init(debug, c)
    }

    private fun init(debug: Boolean, c: Class<*>?) {
        isDebug = debug
        logs.add(
                "### "
                        + TextUtil.format("%1\$tY%1\$tm%1\$td %1\$tH:%1\$tM:%1\$tS", System.currentTimeMillis())
                        + (" JDK" + System.getProperty("java.version"))
                        + (" " + System.getProperty("os.arch")))
        if (c != null) {
            logs.add("### " + c.name)
        }
    }

    fun log1(msg: String) {
        val delta = timer.delta()
        val total = timer.total()
        val s = TextUtil.format("%6.2f/%6.2f (sec): %s", delta, total / 1000f, msg)
        println(s)
        logs.add(s)
    }

    fun debugf(format: String, vararg args: Any) {
        debug(TextUtil.format(format, *args))
    }

    fun debug(msg: String?) {
        if (isDebug) {
            log1(msg ?: "null")
        }
    }

    fun enter(name: String) {
        if (isDebug) {
            log1("## Enter  $name")
        }
    }

    fun leave(name: String) {
        if (isDebug) {
            log1("## Leave $name")
        }
    }

    fun debugPrintln(msg: String) {
        if (isDebug) {
            println(msg)
            logs.add(msg)
        }
    }

    fun errorf(format: String, vararg args: Any) {
        error(TextUtil.format(format, *args))
    }

    fun error(msg: String) {
        val delta = timer.delta()
        val total = timer.total()
        val s = TextUtil.format("%6.2f/%6.2f (sec): %s", delta, total / 1000f, msg)
        println(s)
        logs.add(s)
    }

    @Throws(IOException::class)
    fun done(logfile: File, msg: String = "test done") {
        log1(msg)
        savelog(logfile)
    }

    fun pass() {
        isPassed = true
    }

    fun elapsed(): Float {
        return timer.elapsed()
    }

    fun delta(): Float {
        return timer.delta()
    }

    ////////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    private fun savelog(logfile: File) {
        val sep = TextUtil.lineSeparator
        FileUtil.mkparent(logfile)
        FileUtil.writeFile(logfile, true, TextUtil.joinln(logs) + sep + sep)
    }

    ////////////////////////////////////////////////////////////////////////
}
