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
import com.cplusedition.bot.core.IStepWatch.Companion.rate
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
        const val TIMER_RESOLUTION = 1e-6f

        fun fmt(value: Float?): String {
            return if (value == null) "   ---"
            else if (value >= 1000) TextUt.format("%6d", value.toInt()) else TextUt.format("%6.2f", value)
        }

        fun fmt(value: Double?): String {
            return if (value == null) "   ---"
            else if (value >= 1000) TextUt.format("%6d", value.toInt()) else TextUt.format("%6.2f", value)
        }

        fun rate(count: Float, time: Float): Float {
            val elapsed = if (time < TIMER_RESOLUTION) TIMER_RESOLUTION else time
            return count / elapsed
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
            "%s/%s s %10.2f $unit %10.2f $unit/s: %s", fmt(delta),
            fmt(stepStartTime / 1000f), count, rate, msg
        )
    }
}

open class PerformanceWatch : StepWatch() {

    val durations = Durations()
    var rates = Rates()

    ////////////////////////////////////////////////////////////////////////

    fun <R> duration(cat: String, callback: Fun01<R>): R {
        return durations.duration(cat, callback)
    }

    fun <R> rate(cat: String, count: Number, callback: Fun01<R>): R {
        return rates.rate(cat, count.toDouble(), callback)
    }

    /** Remove the max n values. */
    fun <T : Comparable<T>> trimMax(n: Int, list: List<T>): List<T> {
        if (list.size < n) return listOf()
        return trim(list, list.sortedDescending().subList(0, n))
    }

    /** Remove the min n values. */
    fun <T : Comparable<T>> trimMin(n: Int, list: List<T>): List<T> {
        if (list.size < n) return listOf()
        return trim(list, list.sorted().subList(0, n))
    }

    /** Remove first occurence of each value in the toremove list. */
    fun <T : Comparable<T>> trim(list: List<T>, toremove: List<T>): List<T> {
        val a = toremove.toMutableList()
        return list.filter { !(a.remove(it)) }
    }
    ////////////////////////////////////////////////////////////////////////

    class Durations {
        private val lock = ReentrantLock()
        private val durations = TreeMap<String, MutableList<Long>>()

        fun cats(): Set<String> {
            lock.withLock {
                return durations.keys.toSet()
            }
        }

        fun average(cat: String): Double {
            lock.withLock {
                return durations[cat]?.average() ?: throw AssertionError()
            }
        }

        fun <R> duration(cat: String, callback: Fun01<R>): R {
            val start = System.currentTimeMillis()
            val ret = callback()
            add(cat, System.currentTimeMillis() - start)
            return ret
        }

        fun <R> enter(cat: String, callback: Fun11<Fun00, R>): R {
            val start = System.currentTimeMillis()
            return callback {
                add(cat, System.currentTimeMillis() - start)
            }
        }

        fun of(cat: String): Sequence<Long> {
            lock.withLock {
                return sequence {
                    val list = durations.get(cat) ?: return@sequence
                    list.forEach { yield(it) }
                }
            }
        }

        /**
         * Note spaces around cat is allowed for proper alignment at text output.
         */
        fun stat(cat: String, label: String = cat, scale: Double = 1.0, unit: String = "ms"): String {
            lock.withLock {
                return durationstat(cat, label, scale, unit)
            }
        }

        fun stats(regex: String, scale: Double = 1.0, unit: String = "ms"): String {
            lock.withLock {
                val re = Regex(regex)
                return durationstats(durations.keys.filter { re.matches(it) }, scale, unit)
            }
        }

        fun stats(scale: Double = 1.0, unit: String = "ms"): String {
            lock.withLock {
                return durationstats(durations.keys, scale, unit)
            }
        }

        /// @param callback(values): newvalues
        fun filter(cat: String, callback: Fun11<List<Long>, List<Long>>): Durations {
            lock.withLock {
                durations.put(cat, callback(durations.get(cat)!!).toMutableList())
            }
            return this
        }

        /// @param callback(cat, values): newvalues
        fun filter(callback: Fun21<String, List<Long>, List<Long>>): Durations {
            lock.withLock {
                for (cat in cats()) {
                    durations.put(cat, callback(cat, durations.get(cat)!!).toMutableList())
                }
            }
            return this
        }

        private fun durationstat(cat: String, label: String = cat, scale: Double = 1.0, unit: String = "ms"): String {
            val c = cat.trim()
            val max = durations[c]?.maxOrNull()?.let { it / scale }
            val min = durations[c]?.minOrNull()?.let { it / scale }
            val average = durations[c]?.average()?.let { it / scale }
            return TextUt.format(
                "%s: max: %s, min: %s, average: %s %s",
                label, fmt(max), fmt(min), fmt(average), unit
            )
        }

        private fun durationstats(cats: Collection<String>, scale: Double = 1.0, unit: String = "ms"): String {
            val w = StringPrintWriter()
            val width = durations.keys.maxOfOrNull { it.length } ?: 8
            for (cat in cats) {
                w.println(durationstat(cat, cat.padEnd(width), scale, unit))
            }
            return w.toString()
        }

        private fun add(cat: String, elapsed: Long) {
            lock.withLock {
                (durations[cat] ?: ArrayList<Long>().also {
                    durations.put(cat, it)
                }).add(elapsed)
            }
        }
    }

    class Rates {
        private val lock = ReentrantLock()
        private val rates = TreeMap<String, MutableList<Double>>()

        ////////////////////////////////////////////////////////////////////////

        fun <R> enter(cat: String, callback: Fun11<Fun10<Double>, R>): R {
            val start = System.currentTimeMillis()
            return callback { count ->
                add(cat, count, System.currentTimeMillis() - start)
            }
        }

        fun <R> rate(cat: String, count: Double, callback: Fun01<R>): R {
            val start = System.currentTimeMillis()
            val ret = callback()
            add(cat, count, System.currentTimeMillis() - start)
            return ret
        }

        fun of(cat: String): Sequence<Double> {
            lock.withLock {
                return sequence {
                    val list = rates.get(cat) ?: return@sequence
                    list.forEach { yield(it) }
                }
            }
        }

        fun cats(): Set<String> {
            lock.withLock {
                return rates.keys.toSet()
            }
        }

        fun average(cat: String): Double {
            lock.withLock {
                return rates[cat]?.average() ?: throw AssertionError()
            }
        }

        fun stat(cat: String, label: String = cat, scale: Double = 1.0, unit: String = "count/s"): String {
            lock.withLock {
                return ratestat(cat, label, scale, unit)
            }
        }

        fun stats(regex: String, scale: Double = 1.0, unit: String = "count/s"): String {
            lock.withLock {
                val re = Regex(regex)
                return ratestats(rates.keys.filter { re.matches(it) }, scale, unit)
            }
        }

        fun stats(scale: Double = 1.0, unit: String = "count/s"): String {
            lock.withLock {
                return ratestats(rates.keys, scale, unit)
            }
        }

        /// @param callback(cat, values): newvalues
        fun filter(callback: Fun21<String, List<Double>, List<Double>>): Rates {
            lock.withLock {
                for (cat in cats()) {
                    rates.put(cat, callback(cat, rates.get(cat)!!).toMutableList())
                }
            }
            return this
        }

        /// @param callback(values): newvalues
        fun filter(cat: String, callback: Fun11<List<Double>, List<Double>>): Rates {
            lock.withLock {
                rates.put(cat, callback(rates.get(cat)!!).toMutableList())
            }
            return this
        }

        private fun ratestat(cat: String, label: String = cat, scale: Double = 1.0, unit: String = "count/s"): String {
            val c = cat.trim()
            val max = rates[c]?.maxOrNull()?.let { it / scale }
            val min = rates[c]?.minOrNull()?.let { it / scale }
            val average = rates[c]?.average()?.let { it / scale }
            return TextUt.format(
                "%s: max: %s, min: %s, average: %s %s",
                label, fmt(max), fmt(min), fmt(average), unit
            )
        }

        private fun ratestats(cats: Collection<String>, scale: Double = 1.0, unit: String = "ms"): String {
            val w = StringPrintWriter()
            val width = rates.keys.maxOfOrNull { it.length } ?: 8
            for (cat in cats) {
                w.println(ratestat(cat, cat.padEnd(width), scale, unit))
            }
            return w.toString()
        }

        private fun add(cat: String, count: Number, elapsed: Long) {
            lock.withLock {
                if (elapsed == 0L) return
                (rates[cat] ?: ArrayList<Double>().also {
                    rates.put(cat, it)
                }).add(count.toDouble() * 1000.0 / elapsed.toDouble())
            }
        }
    }
}

class WatchDog(
    private val timeout: Long,
    private val callback: Fun00
) : Closeable {
    private var timer: Timer? = null
    private val start = DateUt.ms
    private var closed = false
    private val lock = ReentrantLock()

    init {
        Timer().also { timer = it }.schedule(timeout) {
            timeout()
        }
    }

    fun watch(threshold: Long = timeout / 5) {
        lock.withLock {
            if (closed) return
            val ms = DateUt.ms
            if (ms - start < threshold) return
            timer?.cancel()
            Timer().also { timer = it }.schedule(timeout) {
                timeout()
            }
        }
    }

    override fun close() {
        lock.withLock {
            closing()
        }
    }

    private fun timeout() {
        lock.lock()
        if (!closing()) {
            lock.unlock()
            return
        }
        lock.unlock()
        callback()
    }

    private fun closing(): Boolean {
        if (closed) return false
        closed = true
        timer?.cancel()
        timer = null
        return true
    }
}
