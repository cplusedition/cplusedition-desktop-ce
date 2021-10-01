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
package com.cplusedition.bot.core

import com.cplusedition.bot.core.IStepWatch.Companion.fmt
import java.io.Closeable
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule
import kotlin.concurrent.withLock

interface IStepWatch {

    fun restart(): IStepWatch
    fun pause(): IStepWatch
    fun resume(): IStepWatch

    /** Total time elapsed in sec. */
    fun elapsedSec(): Float

    /** Total time elapsed in ms. */
    fun elapsedMs(): Long

    /** Delta time since last delta elapsed in sec. */
    fun deltaSec(): Float

    /** Delta time since last delta elapsed in ms. */
    fun deltaMs(): Long

    fun toString(msg: String): String
    fun toString(msg: String, count: Int, unit: String): String
    fun toString(msg: String, count: Long, unit: String): String
    fun toString(msg: String, count: Float, unit: String): String
    fun toStringf(format: String, vararg args: Any): String

    companion object {
        fun fmt(value: Float): String {
            return if (value >= 1000) TextUt.format("%6d", value.toInt()) else TextUt.format("%6.2f", value)
        }
    }
}

open class StepWatch : IStepWatch {

    ////////////////////////////////////////////////////////////////////////

    private var startTime: Long = System.currentTimeMillis()
    private var elapsed: Long = 0
    private var stepStartTime: Long = 0

    override fun restart(): StepWatch {
        startTime = System.currentTimeMillis()
        elapsed = 0
        stepStartTime = 0
        return this
    }

    override fun pause(): StepWatch {
        if (startTime < 0) {
            throw IllegalStateException()
        }
        elapsed += System.currentTimeMillis() - startTime
        startTime = -1
        return this
    }

    override fun resume(): StepWatch {
        startTime = System.currentTimeMillis()
        return this
    }

    /**
     * @return Time elapsed since start() in sec.
     */
    override fun elapsedSec(): Float {
        return elapsedMs() / 1000f
    }

    /**
     * @return Time elapsed since start() in ms.
     */
    override fun elapsedMs(): Long {
        return if (startTime < 0) elapsed else elapsed + (System.currentTimeMillis() - startTime)
    }

    override fun deltaSec(): Float {
        val e = elapsedMs()
        val delta = e - stepStartTime
        stepStartTime = e
        return delta / 1000f
    }

    override fun deltaMs(): Long {
        val e = elapsedMs()
        val time = e - stepStartTime
        stepStartTime = e
        return time
    }

    override fun toStringf(format: String, vararg args: Any): String {
        return toString(TextUt.format(format, *args))
    }

    override fun toString(): String {
        val delta = deltaSec()
        return "${fmt(delta)}/${fmt(stepStartTime / 1000f)} s"
    }

    override fun toString(msg: String): String {
        val delta = deltaSec()
        return "${fmt(delta)}/${fmt(stepStartTime / 1000f)} s: $msg"
    }

    override fun toString(msg: String, count: Int, unit: String): String {
        return toString(msg, count.toLong(), unit)
    }

    override fun toString(msg: String, count: Long, unit: String): String {
        val delta = deltaSec()
        val rate = rate(count.toFloat(), delta)
        return TextUt.format(
                "%s/%s s %10d $unit %10.2f $unit/s: %s",
                fmt(delta),
                fmt(stepStartTime / 1000f),
                count,
                rate,
                msg
        )
    }

    override fun toString(msg: String, count: Float, unit: String): String {
        val delta = deltaSec()
        val rate = rate(count, delta)
        return TextUt.format(
                "%s/%s s %10.2f $unit %10.2f $unit/s: %s", fmt(delta), fmt(stepStartTime / 1000f), count, rate, msg
        )
    }

    companion object {

        const val TIMER_RESOLUTION = 1e-6f

        fun rate(count: Float, time: Float): Float {
            val elapsed = if (time < TIMER_RESOLUTION) TIMER_RESOLUTION else time
            return count / elapsed
        }
    }
}

open class PerformanceWatch : StepWatch() {
    private val matrix = TreeMap<String, MutableList<Long>>()

    fun duration(cat: String, elapsed: Long): Long {
        (matrix[cat] ?: ArrayList<Long>().also { matrix.put(cat, it) }).add(elapsed)
        return elapsed
    }

    fun <T> duration(cat: String, callback: Fun01<T>): Pair<T, Long> {
        deltaMs()
        val ret = callback()
        val ms = duration(cat, deltaMs())
        return Pair(ret, ms)
    }

    fun max(cat: String): Long? {
        return matrix[cat]?.maxOrNull()
    }

    fun min(cat: String): Long? {
        return matrix[cat]?.minOrNull()
    }

    fun average(cat: String): Double? {
        return matrix[cat]?.average()
    }

    /** Note spaces around cat is allowed for proper alignment at text output. */
    fun durationStat(cat: String): String {
        val c = cat.trim()
        val max = "${max(c) ?: 0}".padStart(6)
        val min = "${min(c) ?: 0}".padStart(6)
        val average = fmt((average(c) ?: 0.0).toFloat())
        return "$cat: max: $max, min: $min, average: $average"
    }
}

class WatchDog(
        private val timeout: Long,
        private val callback: Fun00
) : Timer(), Closeable {
    private val start = DateUt.ms
    private var cancelled = true
    private val lock = ReentrantLock()

    init {
        schedule(timeout) { close() }
    }

    fun watch(threshold: Long = timeout / 10) {
        lock.withLock {
            val ms = DateUt.ms
            if (ms - start < threshold) return
            if (!cancelled) this.cancel()
            this.schedule(timeout) { close() }
        }
    }

    override fun cancel() {
        lock.withLock {
            super.cancel()
            cancelled = true
        }
    }

    override fun close() {
        lock.withLock {
            callback()
        }
    }

}
