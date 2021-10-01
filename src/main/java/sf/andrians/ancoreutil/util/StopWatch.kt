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

/**
 * Stop watch for timing elapse time.
 */
class StopWatch : IStopWatch {
    private var startTime: Long = -1
    private var elapsed: Long = 0

    constructor() {}

    constructor(start: Boolean) {
        if (start) {
            startTime = System.currentTimeMillis()
        }
    }

    override fun start(): StopWatch {
        startTime = System.currentTimeMillis()
        return this
    }

    override fun pause(): StopWatch {
        if (startTime < 0) {
            throw RuntimeException("StopWatch is not running.")
        }
        elapsed += System.currentTimeMillis() - startTime
        startTime = -1
        return this
    }

    override fun restart(): StopWatch {
        elapsed = 0
        startTime = System.currentTimeMillis()
        return this
    }

    override fun reset() {
        elapsed = 0
        startTime = -1
    }

    /**
     * @return Time elapsed since start() in sec.
     */
    override fun elapsed(): Float {
        return if (startTime < 0) {
            elapsed / 1000f
        } else (elapsed + (System.currentTimeMillis() - startTime)) / 1000f
    }

    override fun elapsedInMs(): Long {
        return if (startTime < 0) {
            elapsed
        } else elapsed + (System.currentTimeMillis() - startTime)
    }

    override fun toString(): String {
        return TextUtil.format("%1$8.2f (sec)", elapsed())
    }

    ////////////////////////////////////////////////////////////////////////

    override fun toStringf(format: String, vararg args: Any): String {
        return format(TextUtil.format(format, *args), elapsed())
    }

    override fun toString(msg: String): String {
        return format(msg, elapsed())
    }

    ////////////////////////////////////////////////////////////////////////

    override fun toString(msg: String, count: Long, unit: String): String {
        val time = elapsed()
        return format(msg, count, unit, time, rate(count.toFloat(), time))
    }

    override fun toString(msg: String, count: Float, unit: String): String {
        val time = elapsed()
        return format(msg, count, unit, time, rate(count, time))
    }

    fun toString(count: Long, unit: String): String {
        val time = elapsed()
        return format(count, unit, time, rate(count.toFloat(), time))
    }

    fun toString(count: Float, unit: String): String {
        val time = elapsed()
        return format(count, unit, time, rate(count, time))
    }

    fun toString(start: FloatArray, msg: String): String {
        val e = elapsed()
        val time = e - start[0]
        start[0] = e
        return TextUtil.format("%-32s: %8.2f/%8.2f (sec)", msg, time, e)
    }

    ////////////////////////////////////////////////////////////////////////

    companion object {

        const val TIMER_RESOLUTION = 1e-6f

        fun rate(count: Float, time: Float): Float {
            var time = time
            if (time < TIMER_RESOLUTION) {
                time = TIMER_RESOLUTION
            }
            return count / time
        }

        fun format(msg: String, time: Float): String {
            return TextUtil.format("%1$-32s: %2$8.2f (sec)", msg, time)
        }

        fun format(msg: String, count: Long, unit: String, time: Float, rate: Float): String {
            return TextUtil.format("%1$-32s: %3$8.2f (sec) %2$10d ($unit) %4$10.2f ($unit/sec)", msg, count, time, rate)
        }

        fun format(msg: String, count: Float, unit: String, time: Float, rate: Float): String {
            return TextUtil.format("%1$-32s: %3$8.2f (sec) %2$10.2f ($unit) %4$10.2f ($unit/sec)", msg, count, time, rate)
        }

        fun format(count: Long, unit: String, time: Float, rate: Float): String {
            return TextUtil.format("%2$8.2f (sec) %1$10d ($unit) %3$10.2f ($unit/sec)", count, time, rate)
        }

        fun format(count: Float, unit: String, time: Float, rate: Float): String {
            return TextUtil.format("%2$8.2f (sec) %1$10.2f ($unit) %3$10.2f ($unit/sec)", count, time, rate)
        }
    }
}
