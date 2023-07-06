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

import java.io.PrintStream

interface ILog {
    fun d(msg: String, e: Throwable? = null)
    fun i(msg: String, e: Throwable? = null)
    fun w(msg: String, e: Throwable? = null)
    fun e(msg: String, e: Throwable? = null)
}

interface ITraceLogger : ILog {
    val debugging: Boolean
    fun d(e: Throwable? = null, message: Fun11<Throwable?, String>)
    fun i(e: Throwable? = null, message: Fun11<Throwable?, String>)
    fun w(e: Throwable? = null, message: Fun11<Throwable?, String>)
    fun e(e: Throwable? = null, message: Fun11<Throwable?, String>)

    /**
     * Enter a new scope.
     * It debug mode, it log a message with timestamp.
     */
    fun enter(msg: String = "")

    /**
     * Leave a scope.
     * In debug mode, it log a message with delta and timestamp.
     * The delta time is time elapsed since start of the scope.
     */
    fun leave(msg: String = "")

    fun <R> quiet(code: Fun01<R>): R

    /**
     * Enter a new scope, execute the code, leave the scope and return the result.
     * It debug mode, it log a message on enter and leave with timestamp.
     * The delta time in the leave message is the time elapsed since start of the scope.
     */
    fun <R> enter(msg: String = "", code: Fun01<R>): R {
        enter(msg)
        try {
            return code()
        } finally {
            leave(msg)
        }
    }
}

object SystemLogger : PrintStreamLogger(true, System.out, System.err), ITraceLogger

open class PrintStreamLogger(
    override val debugging: Boolean = true,
    protected val out: PrintStream,
    protected val err: PrintStream = out
) : ITraceLogger {

    private var quiet = false

    override fun d(msg: String, e: Throwable?) {
        if (debugging && !quiet) {
            out.println(msg)
            e?.printStackTrace(out)
        }
    }

    override fun i(msg: String, e: Throwable?) {
        if (!quiet) {
            out.println(msg)
            if (debugging) {
                e?.printStackTrace(out)
            }
        }
    }

    override fun w(msg: String, e: Throwable?) {
        if (!quiet) {
            out.println(msg)
            if (debugging) {
                e?.printStackTrace(out)
            }
        }
    }

    override fun e(msg: String, e: Throwable?) {
        if (!quiet) {
            err.println(msg)
            e?.printStackTrace(err)
        }
    }

    override fun d(e: Throwable?, message: Fun11<Throwable?, String>) {
        if (debugging && !quiet) {
            out.println(message(e))
            e?.printStackTrace(out)
        }
    }

    override fun i(e: Throwable?, message: Fun11<Throwable?, String>) {
        if (!quiet) {
            out.println(message(e))
            if (debugging) {
                e?.printStackTrace(out)
            }
        }
    }

    override fun w(e: Throwable?, message: Fun11<Throwable?, String>) {
        if (!quiet) {
            out.println(message(e))
            if (debugging) {
                e?.printStackTrace(out)
            }
        }
    }

    override fun e(e: Throwable?, message: Fun11<Throwable?, String>) {
        if (!quiet) {
            out.println(message(e))
            e?.printStackTrace(err)
        }
    }

    override fun enter(msg: String) {
        if (!quiet) {
            d("# +++ $msg")
        }
    }

    override fun leave(msg: String) {
        if (!quiet) {
            d("# --- $msg")
        }
    }

    override fun <R> quiet(code: Fun01<R>): R {
        quiet = true
        try {
            return code()
        } finally {
            quiet = false
        }
    }
}

open class StringLogger(
    debugging: Boolean = true,
) : PrintStreamLogger(debugging, StringPrintStream()) {

    override fun toString(): String {
        out.flush()
        err.flush()
        return out.toString()
    }
}
