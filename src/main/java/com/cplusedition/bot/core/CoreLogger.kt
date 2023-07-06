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

import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.math.floor
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

//////////////////////////////////////////////////////////////////////

interface ICoreLogger : ILoggerShortcuts

interface ILoggerCore : ITraceLogger {

    val prefix: String

    /**
     * @return The system time in ms when the logger is created.
     */
    val startTime: Long

    /**
     * @return The error Count.
     * Note that this method is synchronous.
     */
    val errorCount: Int

    fun awaitShutdown(timeout: Long = 1000)

    /**
     * Reset the error count.
     * @return The error count before reset.
     */
    fun resetErrorCount(): Int

    /**
     * Wait for all previous log task to complete and flush output.
     * Note that this method is synchronous.
     */
    fun flush()

    /**
     * Save log to a file.
     */
    fun saveLog(file: File)

    /**
     * Wait for all previous log task to complete and get a copy of the log.
     * Note that this method is synchronous.
     */
    fun getLog(): List<String>

    /** Log an error, ie. increment errorCount, without any log message. */
    fun e()

    fun deltaMs(): Long

    fun deltaSec(): Double

    /** Debug message with timestamp. */
    fun dd(msg: String = "")

    /** Info message with timestamp. */
    fun ii(msg: String = "")

    /**
     * Like dd() but generate message with given code block.
     * @param message(e, delta) where delta is time elapsed since start of the last enter() in sec.
     */
    fun dd(e: Throwable? = null, message: Fun21<Throwable?, Double, String>)

    /**
     * Like dd() but generate message with given code block.
     * @param message(e, delta) where delta is time elapsed since start of the last enter() in sec.
     */
    fun ii(e: Throwable? = null, message: Fun21<Throwable?, Double, String>)

    /**
     * Like leave(String) but generate message with the given code block.
     * @param message(start, delta) Return msg for leave() call.
     */
    fun leave(message: Fun21<Double, Double, String>)

    /**
     * Like leave(msg), but throw an exception if there are errors logged.
     * Note that this method is synchronous.
     */
    @Throws(IllegalStateException::class)
    fun leaveX(msg: String = "")
}

interface ILoggerShortcuts : ILoggerCore {

    fun d(any: Any?) {
        d("$any")
    }

    fun dfmt(format: String, vararg args: Any) {
        if (debugging)
            d(String.format(Locale.ROOT, format, *args))
    }

    fun ifmt(format: String, vararg args: Any) {
        i(String.format(Locale.ROOT, format, *args))
    }

    fun wfmt(format: String, vararg args: Any) {
        w(String.format(Locale.ROOT, format, *args))
    }

    fun efmt(format: String, vararg args: Any) {
        e(String.format(Locale.ROOT, format, *args))
    }

    fun d(msgs: Iterable<String>) {
        if (debugging)
            d(msgs.iterator())
    }

    fun i(msgs: Iterable<String>) {
        i(msgs.iterator())
    }

    fun w(msgs: Iterable<String>) {
        w(msgs.iterator())
    }

    fun e(msgs: Iterable<String>) {
        e(msgs.iterator())
    }

    /**
     * Print multi-line debug messages without timestamp
     */
    fun d(vararg msgs: String) {
        if (debugging)
            d(msgs.iterator())
    }

    /**
     * Print multi-line info messages without timestamp
     */
    fun i(vararg msgs: String) {
        i(msgs.iterator())
    }

    /**
     * Print multi-line warn messages without timestamp
     */
    fun w(vararg msgs: String) {
        w(msgs.iterator())
    }

    /**
     * Print multi-line error messages without timestamp
     */
    fun e(vararg msgs: String) {
        e(msgs.iterator())
    }

    fun d(msgs: Iterator<String>) {
        if (debugging && msgs.hasNext()) {
            this.d(msgs.bot.joinln())
        }
    }

    fun i(msgs: Iterator<String>) {
        if (msgs.hasNext()) {
            this.i(msgs.bot.joinln())
        }
    }

    fun w(msgs: Iterator<String>) {
        if (msgs.hasNext()) {
            this.w(msgs.bot.joinln())
        }
    }

    fun e(msgs: Iterator<String>) {
        if (msgs.hasNext()) {
            this.e(msgs.bot.joinln())
        }
    }

    fun d(msgs: Sequence<String>) {
        if (debugging)
            this.d(msgs.bot.joinln())
    }

    fun i(msgs: Sequence<String>) {
        this.i(msgs.bot.joinln())
    }

    fun w(msgs: Sequence<String>) {
        this.w(msgs.bot.joinln())
    }

    fun e(msgs: Sequence<String>) {
        this.e(msgs.bot.joinln())
    }

    fun dd(msg: Any?) {
        dd("$msg")
    }

    fun ii(msg: Any?) {
        dd("$msg")
    }

    fun enter(name: KCallable<*>, msg: String = "") {
        enter(join(name.name, msg))
    }

    fun enter(name: KClass<*>, msg: String = "") {
        enter(join(name.simpleName ?: name.toString(), msg))
    }

    fun <T> enter(name: KCallable<*>, msg: String = "", code: Fun01<T>): T {
        return enter(join(name.name, msg), code)
    }

    fun <T> enter(name: KClass<*>, msg: String = "", code: Fun01<T>): T {
        return enter(join(name.simpleName ?: name.toString(), msg), code)
    }

    fun <T> enterX(name: KCallable<*>, msg: String = "", code: Fun01<T>): T? {
        return enterX(join(name.name, msg), code)
    }

    fun <T> enterX(name: KClass<*>, msg: String = "", code: Fun01<T>): T? {
        return enterX(join(name.simpleName ?: name.toString(), msg), code)
    }

    @Throws(Exception::class)
    fun <T> enterX(msg: String = "", code: Fun01<T>): T? {
        this.enter(msg)
        return try {
            code()
        } catch (e: Throwable) {
            this.e(msg, e)
            null
        } finally {
            this.leaveX(msg)
        }
    }

    fun enterAwait(msg: String = "", code: Fun10<Fun00>) {
        enter(msg)
        val done = CountDownLatch(1)
        code {
            leave(msg)
            done.countDown()
        }
        done.await()
    }

    fun enterAwaitX(msg: String = "", code: Fun10<Fun00>) {
        enter(msg)
        val done = CountDownLatch(1)
        code {
            leaveX(msg)
            done.countDown()
        }
        done.await()
    }

    /**
     * Print message with rate.
     */
    fun drate(msg: String = "", unit: String, count: Number) {
        if (debugging)
            irate(msg, unit, count)
    }

    /**
     * Print message with rate.
     */
    fun irate(msg: String = "", unit: String, count: Number) {
        ii { _, delta ->
            CoreLogger.fmtRate(msg, unit, count.toDouble(), delta)
        }
    }

    /**
     * Like enter() but show rate upon leave in debug mode.
     * Note that rate would not be printed if an exception occurs in code() and no count is available.
     * @param code() Result is the count for the rate calculation.
     * @return count returned by code().
     */
    fun <T : Number> enterRate(msg: String = "", unit: String, code: Fun01<T>): T {
        var count: T? = null
        val ret: T
        enter(msg)
        try {
            ret = code()
            count = ret
        } finally {
            leave { _, delta ->
                if (count != null)
                    CoreLogger.fmtRate(msg, unit, count.toDouble(), delta)
                else ""
            }
        }
        return ret
    }

    /**
     * Check that at least one error occurs in code().
     * If so, clear the error status, otherwise log an error.
     */
    fun expectError(msg: String = "# ERROR: Expecting error, but not occurred", code: Fun00) {
        enter {
            try {
                code()
            } finally {
                if (resetErrorCount() == 0) {
                    e(msg)
                }
            }
        }
    }

    private fun join(name: String, msg: String): String {
        return if (msg.isEmpty()) name else "$name: $msg"
    }
}

//////////////////////////////////////////////////////////////////////

abstract class AbstractCoreLogger(
    final override val debugging: Boolean,
    final override val startTime: Long = System.currentTimeMillis(),
    final override val prefix: String = "####",
) : ICoreLogger {
    protected abstract fun log(
        msg: String,
        e: Throwable? = null,
        timestamp: Boolean = false,
        error: Boolean = false
    )

    protected abstract fun log(
        e: Throwable? = null,
        timestamp: Boolean = false,
        error: Boolean = false,
        message: Fun11<Throwable?, String>
    )

    protected abstract fun log2(
        e: Throwable? = null,
        message: Fun21<Throwable?, Double, String>
    )

    override fun d(msg: String, e: Throwable?) {
        if (debugging) {
            i(msg, e)
        }
    }

    override fun i(msg: String, e: Throwable?) {
        val ex = if (debugging) e else null
        this.log(msg, ex, false)
    }

    override fun w(msg: String, e: Throwable?) {
        this.log(msg, e, false)
    }

    override fun e(msg: String, e: Throwable?) {
        this.log(msg, e, false, error = true)
    }

    override fun d(e: Throwable?, message: Fun11<Throwable?, String>) {
        if (debugging) {
            i(e, message)
        }
    }

    override fun i(e: Throwable?, message: Fun11<Throwable?, String>) {
        val ex = if (debugging) e else null
        this.log(ex, timestamp = false, error = false, message = message)
    }

    override fun w(e: Throwable?, message: Fun11<Throwable?, String>) {
        this.log(e, timestamp = false, error = false, message = message)
    }

    override fun e(e: Throwable?, message: Fun11<Throwable?, String>) {
        this.log(e, timestamp = false, error = true, message = message)
    }

    override fun dd(e: Throwable?, message: Fun21<Throwable?, Double, String>) {
        if (debugging) {
            ii(e, message)
        }
    }

    override fun ii(e: Throwable?, message: Fun21<Throwable?, Double, String>) {
        this.log2(e, message)
    }

    override fun dd(msg: String) {
        if (debugging) {
            ii(msg)
        }
    }

    override fun ii(msg: String) {
        this.log(msg, null, timestamp = true, error = false)
    }

    override fun deltaSec(): Double {
        return deltaMs() / 1e3
    }
}

//////////////////////////////////////////////////////////////////////

open class NullCoreLogger : AbstractCoreLogger(false, 0L, "") {
    override fun log(msg: String, e: Throwable?, timestamp: Boolean, error: Boolean) {
    }

    override fun log(e: Throwable?, timestamp: Boolean, error: Boolean, message: Fun11<Throwable?, String>) {
    }

    override fun log2(e: Throwable?, message: Fun21<Throwable?, Double, String>) {
    }

    override fun e() {
    }

    override val errorCount: Int = 0

    override fun awaitShutdown(timeout: Long) {
    }

    override fun resetErrorCount(): Int {
        return 0
    }

    override fun flush() {
    }

    override fun <R> quiet(code: Fun01<R>): R {
        return code()
    }

    override fun saveLog(file: File) {
    }

    override fun getLog(): List<String> {
        return emptyList()
    }

    override fun deltaMs(): Long {
        return 0L
    }

    override fun enter(msg: String) {
    }

    override fun leave(msg: String) {
    }

    override fun <T> enterX(msg: String, code: Fun01<T>): T? {
        return code()
    }

    override fun leaveX(msg: String) {
    }

    override fun leave(message: Fun21<Double, Double, String>) {
    }

    override fun drate(msg: String, unit: String, count: Number) {
    }

    override fun irate(msg: String, unit: String, count: Number) {
    }

    override fun <T : Number> enterRate(msg: String, unit: String, code: Fun01<T>): T {
        return code()
    }
}

//////////////////////////////////////////////////////////////////////

/**
 * A thread safe logger.
 * Note that unless otherwise specified, all calls are asynchronous.
 * In general, methods that retreive status, eg. errorCount, are synchronous.
 */
open class CoreLogger(
    debugging: Boolean,
    startTime: Long = System.currentTimeMillis(),
    prefix: String = "####",
    out: PrintStream = System.out,
    err: PrintStream = System.err
) : AbstractCoreLogger(debugging, startTime, prefix) {

    ////////////////////////////////////////////////////////////////////////

    private val delegate = Delegate(debugging, startTime, prefix, out, err)

    override fun log(msg: String, e: Throwable?, timestamp: Boolean, error: Boolean) {
        delegate.log(msg, e, timestamp, error)
    }

    override fun log(e: Throwable?, timestamp: Boolean, error: Boolean, message: Fun11<Throwable?, String>) {
        delegate.log(e, timestamp, error, message)
    }

    override fun log2(e: Throwable?, message: Fun21<Throwable?, Double, String>) {
        delegate.log2(e, message)
    }

    override fun awaitShutdown(timeout: Long) {
        delegate.awaitShutdown(timeout)
    }

    override fun e() {
        delegate.e()
    }

    /** @return The errorCount. Note that this method is synchronous. */
    override val errorCount: Int
        get() = delegate.getErrorCount()

    override fun resetErrorCount(): Int {
        return delegate.resetErrorCount()
    }

    override fun flush() {
        delegate.flush()
    }

    override fun <R> quiet(code: Fun01<R>): R {
        return delegate.quiet(code)
    }

    @Throws(IOException::class)
    override fun saveLog(file: File) {
        delegate.saveLog(file)
    }

    /**
     * @ return The log entries.
     * Note that this method is synchronous.
     */

    override fun getLog(): List<String> {
        return delegate.getLog()
    }

    fun addLifecycleListener(listener: ILifecycleListener) {
        delegate.addLifecycleListener(listener)
    }

    fun removeLifecycleListener(listener: ILifecycleListener) {
        delegate.removeLifecycleListener(listener)
    }

    ////////////////////////////////////////////////////////////////////////

    override fun deltaMs(): Long {
        return delegate.elapsed()
    }

    override fun enter(msg: String) {
        delegate.enter(msg)
    }

    override fun leave(msg: String) {
        delegate.leave(msg)
    }

    @Throws(IllegalStateException::class)
    override fun leaveX(msg: String) {
        delegate.leaveX(msg)
    }

    override fun leave(message: Fun21<Double, Double, String>) {
        delegate.leave(message)
    }

    ////////////////////////////////////////////////////////////////////////

    interface ILifecycleListener {
        fun onStart(msg: String, starttime: Long, logger: Fun10<String>)
        fun onDone(msg: String, endtime: Long, errors: Int, logger: Fun10<String>)
    }

    ////////////////////////////////////////////////////////////////////////

    private class Delegate(
        private val debugging: Boolean,
        private var startTime: Long,
        private val prefix: String,
        private val out: PrintStream,
        private val err: PrintStream
    ) {

        private class Info(
            val msg: String,
            val errorCount: Int,
            val startTime: Long
        )

        private val callStack = Stack<Info>()
        private val quietStack = Stack<Boolean>()
        private val logs = ArrayList<String>()
        private val listeners = ArrayList<ILifecycleListener>()
        private val executor = Executors.newSingleThreadExecutor()
        private val prefixEnter = "$prefix +++++++"
        private val prefixTimestamp = "$prefix        "

        private var quiet = false
        private var errorCount = 0

        fun getErrorCount(): Int {
            return executor.submit(Callable {
                errorCount
            }).get()
        }

        fun resetErrorCount(): Int {
            return executor.submit(Callable {
                val ret = errorCount
                errorCount = 0
                return@Callable ret
            }).get()
        }

        fun awaitShutdown(timeout: Long) {
            executor.awaitTermination(timeout, MILLISECONDS)
        }

        fun flush() {
            executor.submit {
                flushall()
            }.get()
        }

        fun log(msg: String, e: Throwable? = null, timestamp: Boolean, error: Boolean = false) {
            val time = if (timestamp) System.currentTimeMillis() else null
            executor.submit {
                log(msg, e, time, error)
            }
        }

        fun log(e: Throwable? = null, timestamp: Boolean, error: Boolean = false, message: Fun11<Throwable?, String>) {
            val time = if (timestamp) System.currentTimeMillis() else null
            executor.submit {
                val msg = message(e)
                log(msg, e, time, error)
            }
        }

        fun log2(e: Throwable? = null, message: Fun21<Throwable?, Double, String>) {
            val time = System.currentTimeMillis()
            executor.submit {
                val start = callStack.peek().startTime
                val msg = message(e, (time - start) / 1000.0)
                log(msg, e, time, false)
            }
        }

        private fun log(msg: String, e: Throwable?, time: Long?, error: Boolean) {
            if (error) {
                ++errorCount
                flushall()
                log1(err, msg, e, time, null)
                err.flush()
            } else {
                flushall()
                log1(out, msg, e, time, null)
                out.flush()
            }
        }

        fun e() {
            executor.submit {
                ++errorCount
            }
        }

        fun elapsed(): Long {
            val time = System.currentTimeMillis()
            return executor.submit<Long> {
                elapsed1(time)
            }.get()
        }

        fun enter(msg: String) {
            val time = System.currentTimeMillis()
            executor.submit {
                enter1(msg, time)
            }
        }

        fun leave(msg: String) {
            val time = System.currentTimeMillis()
            executor.submit {
                leave1(msg, time)
            }
        }

        fun leave(message: Fun21<Double, Double, String>) {
            val time = System.currentTimeMillis()
            executor.submit {
                leave1(message(time / 1000.0, (time - callStack.peek().startTime) / 1000.0), time)
            }
        }

        @Throws(IllegalStateException::class)
        fun leaveX(msg: String) {
            val time = System.currentTimeMillis()
            try {
                executor.submit {
                    if (errorCount > 0) {
                        val info = callStack.peek()
                        leave1(msg, time)
                        flushall()
                        throw IllegalStateException(info.msg)
                    } else {
                        leave1(msg, time)
                    }
                }.get()
            } catch (e: ExecutionException) {
                throw e.cause ?: e
            }
        }

        fun <R> quiet(code: Fun01<R>): R {
            executor.submit {
                quietStack.push(quiet)
                quiet = true
            }
            try {
                return code()
            } finally {
                executor.submit {
                    quiet = quietStack.pop()
                }
            }
        }

        @Throws(IOException::class)
        fun saveLog(file: File): Future<*> {
            return executor.submit {
                file.mkparentOrFail()
                if (logs.isNotEmpty() && !logs.last().endsWith(LS)) {
                    logs.add(LS)
                }
                file.writeText(logs.joinToString(""))
            }
        }

        fun getLog(): List<String> {
            return executor.submit(Callable {
                ArrayList(logs)
            }).get()
        }

        @Throws
        fun addLifecycleListener(listener: ILifecycleListener) {
            executor.submit {
                listeners.add(listener)
            }
        }

        @Throws
        fun removeLifecycleListener(listener: ILifecycleListener) {
            executor.submit {
                listeners.remove(listener)
            }
        }

        ////////////////////////////////////////////////////////////////////////

        private fun flushall() {
            out.flush()
            err.flush()
        }

        private fun elapsed1(ms: Long): Long {
            val info = callStack.peek()
            return ms - info.startTime
        }

        private fun log1(out: PrintStream, msg: String, e: Throwable? = null, start: Long?, end: Long?) {
            if (quiet) {
                return
            }
            val s = when {
                start != null && end != null ->
                    "$prefix ${IStepWatch.fmt((end - start) / 1000f)}/${IStepWatch.fmt((end - startTime) / 1000f)} s: $msg"

                start != null ->
                    "$prefixTimestamp${IStepWatch.fmt((start - startTime) / 1000f)} s: $msg"

                end != null ->
                    "$prefixEnter${IStepWatch.fmt((end - startTime) / 1000f)} s: $msg"

                else -> msg
            }
            if (e == null) {
                smartlog(out, s)
            } else {
                val w = StringPrintWriter()
                if (s.endsWith(LS)) {
                    w.print(s)
                } else if (s.isNotEmpty()) {
                    w.println(s)
                }
                e.printStackTrace(w)
                val str = w.toString()
                out.print(str)
                logs.add(str)
            }
        }

        private fun smartlog(out: PrintStream, msg: String) {
            if (msg.endsWith(LS)) {
                out.print(msg)
                logs.add(msg)
            } else if (msg.isNotEmpty()) {
                out.println(msg)
                logs.add(msg + LS)
            }
        }

        private fun enter1(msg: String, time: Long) {
            callStack.push(Info(msg, errorCount, time))
            if (debugging && callStack.size == 1) {
                listeners.forEach {
                    it.onStart(msg, startTime) { s ->
                        log1(out, s, null, null, time)
                    }
                }
            }
            errorCount = 0
            if (debugging && msg.isNotEmpty()) {
                val b = StringBuilder()
                for (i in 0 until callStack.size) {
                    b.append('+')
                }
                if (b.isNotEmpty()) b.append(' ')
                b.append(msg)
                log1(out, b.toString(), null, null, time)
            }
        }

        private fun leave1(msg: String, time: Long) {
            val info = callStack.pop()
            errorCount += info.errorCount
            if (debugging && (info.msg.isNotEmpty() || msg.isNotEmpty())) {
                val b = StringBuilder()
                for (i in 0..callStack.size) {
                    b.append('-')
                }
                if (b.isNotEmpty()) b.append(' ')
                b.append(msg.ifEmpty { info.msg })
                log1(out, b.toString(), null, info.startTime, time)
            }
            if (callStack.isEmpty()) {
                listeners.forEach {
                    it.onDone(info.msg, time, errorCount) { s ->
                        log1(out, s, null, null, time)
                    }
                }
            }
        }
    }

    companion object {
        const val EPSILON = 1e-6

        fun rate(count: Double, time: Double): Double {
            val elapsed = if (time < EPSILON) EPSILON else time
            return count / elapsed
        }

        fun fmtRate(
            msg: String,
            unit: String,
            count: Double,
            delta: Double,
        ): String {
            val b = StringBuilder(msg)
            val rate = rate(count, delta)
            val format = if (floor(count) == count) "%.0f" else "%.2f"
            b.append(TextUt.format("$format/%.2f: %.2f %s/s", count, delta, rate, unit))
            return b.toString()
        }
    }
}
