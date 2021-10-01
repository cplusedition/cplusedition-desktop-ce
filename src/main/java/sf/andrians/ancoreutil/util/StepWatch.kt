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

class StepWatch : IStopWatch {
    ////////////////////////////////////////////////////////////////////////
    private var startTime: Long = -1
    private var elapsed: Long = 0
    private var total: Long = 0
    private var debug = false

    constructor() {}
    constructor(start: Boolean) {
        if (start) {
            start()
        }
    }

    override fun start(): StepWatch {
        startTime = System.currentTimeMillis()
        return this
    }

    override fun pause(): StepWatch {
        if (startTime < 0) {
            throw RuntimeException("StopWatch is not running.")
        }
        elapsed += System.currentTimeMillis() - startTime
        startTime = -1
        return this
    }

    override fun restart(): StepWatch {
        total = 0
        elapsed = 0
        startTime = System.currentTimeMillis()
        return this
    }

    override fun reset() {
        elapsed = 0
        startTime = -1
        total = 0
    }

    /**
     * @return Time elapsed since start() in sec.
     */
    override fun elapsed(): Float {
        return elapsedInMs() / 1000f
    }

    /**
     * @return Time elapsed since start() in ms.
     */
    override fun elapsedInMs(): Long {
        return if (startTime < 0) {
            elapsed
        } else elapsed + (System.currentTimeMillis() - startTime)
    }

    override fun toStringf(format: String, vararg args: Any): String {
        return toString(TextUtil.format(format, *args))
    }

    override fun toString(): String {
        val delta = delta()
        return TextUtil.format("%8.2f/%8.2f (sec)", delta, total / 1000f)
    }

    override fun toString(msg: String): String {
        val delta = delta()
        return TextUtil.format("%-48s: %8.2f/%8.2f (sec)", msg, delta, total / 1000f)
    }

    override fun toString(msg: String, count: Long, unit: String): String {
        val delta = delta()
        val rate: Float = StopWatch.Companion.rate(count.toFloat(), delta)
        return TextUtil.format(
                "%1$-48s: %3$8.2f/%4$8.2f (sec) %2$10d ($unit) %5$10.2f ($unit/sec)",
                msg,
                count,
                delta,
                total / 1000f,
                rate)
    }

    override fun toString(msg: String, count: Float, unit: String): String {
        val delta = delta()
        val rate: Float = StopWatch.Companion.rate(count, delta)
        return TextUtil.format(
                "%1$-48s: %3$8.2f/%4$8.2f (sec) %2$10.2f ($unit) %5$10.2f ($unit/sec)", msg, count, delta, total, rate)
    }

    fun debug(msg: String): String {
        return if (!debug) {
            msg
        } else toString(msg)
    }

    fun delta(): Float {
        val e = elapsedInMs()
        val delta = e - total
        total = e
        return delta / 1000f
    }

    fun total(): Long {
        return total
    }

    fun deltaInMs(): Long {
        val e = elapsedInMs()
        val time = e - total
        total = e
        return time
    }

    companion object {
        fun create(debug: Boolean): StepWatch {
            val ret = StepWatch(true)
            ret.debug = debug
            return ret
        }
    }
}
