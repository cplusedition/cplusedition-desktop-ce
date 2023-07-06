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

import com.cplusedition.bot.core.ProcessUtil.Companion.okOrFail
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object ProcessUt : ProcessUtil()

open class ProcessUtil {

    fun sleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    fun await(done: CountDownLatch) {
        try {
            done.await()
        } catch (e: InterruptedException) {
        }
    }

    fun await(done: CountDownLatch, ms: Long) {
        try {
            done.await(ms, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
        }
    }

    fun exec(vararg cmdline: String): Process? {
        return exec(FileUt.pwd(), null, *cmdline)
    }

    fun exec(workdir: File, vararg cmdline: String): Process? {
        return exec(workdir, null, *cmdline)
    }

    fun exec(workdir: File, env: Array<out String>?, vararg cmdline: String): Process? {
        return try {
            Runtime.getRuntime().exec(cmdline, env, workdir)
        } catch (e: Throwable) {
            null
        }
    }

    @Throws(Exception::class)
    fun backtick(cmd: String, args: List<String>): String {
        return backtick(FileUt.pwd(), cmd, *args.toTypedArray())
    }

    @Throws(Exception::class)
    fun backtick(workdir: File, cmd: String, args: List<String>): String {
        return backtick(workdir, cmd, *args.toTypedArray())
    }

    @Throws(Exception::class)
    fun backtick(cmd: String, vararg args: String): String {
        return backtick(FileUt.pwd(), cmd, *args)
    }

    @Throws(Exception::class)
    fun backtick(workdir: File, cmd: String, vararg args: String): String {
        return ProcessUtBuilder(workdir, cmd, *args)
            .backtick { rc, out, err ->
                okOrFail(rc, out, err)
                IOUt.readText(out)
            }.get()
    }

    @Throws(Exception::class)
    fun backtick(out: OutputStream, workdir: File, cmd: String, args: List<String>) {
        backtick(out, workdir, cmd, *args.toTypedArray())
    }

    @Throws(Exception::class)
    fun backtick(out: OutputStream, workdir: File, cmd: String, vararg args: String) {
        val err = MyByteOutputStream()
        ProcessUtBuilder(workdir, cmd, *args)
            .async(out, err) { rc ->
                if (rc != 0) {
                    val errs = IOUt.readText(err.inputStream())
                    throw RuntimeException("# ERROR: rc=$rc\n$errs")
                }
            }.get()
    }

    companion object {
        fun okOrFail(rc: Int, out: InputStream, err: InputStream) {
            if (rc == 0) return
            throw RuntimeException(errmsg(rc, out, err))
        }

        fun errmsg(rc: Int, out: InputStream, err: InputStream): String {
            return StringPrintWriter().use { w ->
                w.println("# rc=$rc")
                print(w, out)
                print(w, err)
                w
            }.toString()
        }

        fun print(w: PrintWriter, out: InputStream) {
            IOUt.readText(out).let {
                if (it.isNotEmpty()) w.println(it)
            }
        }
    }
}

class ProcessUtBuilder(
    private var workdir: File,
    private val cmd: String,
    private vararg val args: String
) {
    private var env: Array<out String>? = null
    private var timeout = DateUt.DAY
    private var timeunit = TimeUnit.MILLISECONDS
    private var input: InputStream? = null
    private var onInputErrorCallback: Fun10<Exception> = { e -> throw e }

    constructor(cmd: String, vararg args: String) : this(FileUt.pwd(), cmd, *args)
    constructor(cmd: String, args: Collection<String>) : this(FileUt.pwd(), cmd, *(args.toTypedArray()))
    constructor(workdir: File, cmd: String, args: Collection<String>) : this(workdir, cmd, *(args.toTypedArray()))

    companion object {
        private val pool = Executors.newCachedThreadPool()

        fun submit(task: Runnable): Future<*> {
            return pool.submit(task)
        }

        fun <V> submit(task: Callable<V>): Future<V> {
            return pool.submit(task)
        }
    }

    fun env(vararg env: String): ProcessUtBuilder {
        this.env = env
        return this
    }

    fun timeout(value: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): ProcessUtBuilder {
        this.timeout = value
        this.timeunit = unit
        return this
    }

    fun input(input: InputStream): ProcessUtBuilder {
        this.input = input
        return this
    }

    fun onInputError(callback: Fun10<Exception>): ProcessUtBuilder {
        this.onInputErrorCallback = callback
        return this
    }

    fun asyncOrFailStdio(): Future<Unit> {
        return asyncOrFail(StayOpenOutputStream(System.out), StayOpenOutputStream(System.err)) {}
    }

    fun asyncOrFail(out: OutputStream, err: OutputStream = NullOutputStream()): Future<Unit> {
        return asyncOrFail(out, err) {}
    }

    fun <V> asyncOrFail(out: OutputStream, callback: Fun01<V>): Future<V> {
        return asyncOrFail(out, NullOutputStream(), callback)
    }

    fun <V> asyncOrFail(out: OutputStream, err: OutputStream, callback: Fun01<V>): Future<V> {
        return async(out, err) { rc ->
            if (rc != 0) throw RuntimeException("ERROR: rc=$rc")
            callback()
        }
    }

    fun asyncOrFail(): Future<Unit> {
        return async(NullOutputStream(), NullOutputStream()) { rc ->
            if (rc != 0) throw RuntimeException("ERROR: rc=$rc")
        }
    }

    fun async(out: OutputStream, err: OutputStream): Future<Int> {
        return async(out, err) { rc -> rc }
    }

    fun <V> async(callback: Fun11<Int, V>): Future<V> {
        return async(NullOutputStream(), NullOutputStream(), callback)
    }

    fun async(): Future<Int> {
        return async { rc -> rc }
    }

    /// NOTE that the input, out and err streams are closed before calling the callback.
    /// Wrap System.out and System.err in a StayOpenOutputStream if neccessary.
    fun <V> async(
        out: OutputStream,
        err: OutputStream = NullOutputStream(),
        callback: Fun11<Int, V>
    ): Future<V> {
        return async({
            FileUt.copyByteWise(out, it)
        }, {
            FileUt.copyByteWise(err, it)
        }, {
            out.close()
            err.close()
            callback(it)
        })
    }

    /// Pipe the process output to out(InputStream)
    /// and return a Future for the result of out(InputStream).
    /// The out() callback is executed in a separate thread.
    /// The future return by this method complete after
    /// the process ended and the out() method returns a result.
    /// @param out(InputStream) The input stream comes from
    /// the output stream of the process.
    fun <V> pipe(
        err: OutputStream = NullOutputStream(),
        out: Fun11<InputStream, V>
    ): Future<V?> {
        var result: V? = null
        return async({
            result = out(it)
        }, {
            FileUt.copyByteWise(err, it)
        }, {
            err.close()
            if (it != 0) throw IOException()
            result
        })
    }

    private fun <V> async(
        out: Fun10<InputStream>,
        err: Fun10<InputStream>,
        callback: Fun11<Int, V>
    ): Future<V> {
        return pool.submit(Callable {
            val cmdline = arrayOf(cmd, *args)
            val process = Runtime.getRuntime().exec(cmdline, env, workdir)
            try {
                val outmon = pool.submit {
                    process.inputStream.use { input ->
                        out(input)
                    }
                }
                val errmon = pool.submit {
                    process.errorStream.use { input ->
                        err(input)
                    }
                }
                try {
                    this.input?.use { input ->
                        process.outputStream.use {
                            FileUt.copy(DEFAULT_BUFFER_SIZE, it, input)
                        }
                    }
                } catch (e: Exception) {
                    this.onInputErrorCallback(e)
                }
                if (!process.waitFor(timeout, timeunit)) {
                    throw TimeoutException()
                }
                outmon.get(timeout, timeunit)
                errmon.get(timeout, timeunit)
                return@Callable callback(process.exitValue())
            } catch (e: Throwable) {
                try {
                    process.destroyForcibly().waitFor()
                } catch (_: Throwable) {
                }
                throw e
            }
        })
    }

    fun backtickOrFail(): Future<String> {
        return backtick { rc, out, err ->
            if (rc != 0) {
                throw RuntimeException(ProcessUtil.errmsg(rc, out, err))
            }
            IOUt.readText(out)
        }
    }

    fun <V> backtickOrFail(callback: Fun21<InputStream, InputStream, V>): Future<V> {
        return backtick { rc, out, err ->
            okOrFail(rc, out, err)
            callback(out, err)
        }
    }

    /// backtick() is like async() but buffer the out and err streams implicitly.
    fun <V> backtick(callback: Fun31<Int, InputStream, InputStream, V>): Future<V> {
        val out = MyByteOutputStream()
        val err = MyByteOutputStream()
        return async(out, err) { rc ->
            out.inputStream().use { o ->
                err.inputStream().use { e ->
                    callback(rc, o, e)
                }
            }
        }
    }
}
