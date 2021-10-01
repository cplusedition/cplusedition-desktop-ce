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

object CoreUt : CoreUtil()

open class CoreUtil {
    public fun asyncLoop(count: Int, callback: Fun20<Int, Fun10<Boolean?>>) {
        if (count > 0) asyncLoop1(count, 0, callback)
    }

    private fun asyncLoop1(count: Int, index: Int, callback: Fun20<Int, Fun10<Boolean?>>) {
        callback(index) { terminate: Boolean? ->
            if (terminate != true && index + 1 < count) asyncLoop1(count, index + 1, callback)
        }
    }
}

/**
 * Typically, result is handled null safe as:
 *     result.onError { E -> ... }?.onOK { R -> ... } or
 *     result.onResult({ E -> ... }, { R -> ... })
 */
interface IBotResult<R, E> {
    fun isOK(): Boolean
    fun isFail(): Boolean
    fun result(): R?
    fun failure(): E?
    fun onFail(callback: Fun10<E>? = null): IBotResult<R, E>?
    fun <S> onOK(callback: Fun11<R, S>): S
    fun <S> onResult(onfail: Fun11<E, S>, onok: Fun11<R, S>): S
}

class BotResult<R, E> private constructor(
        private val result: R? = null,
        private val failure: E? = null
) : IBotResult<R, E> {
    companion object {
        fun <R, E> ok(result: R): IBotResult<R, E> {
            return BotResult(result, null)
        }

        fun <R, E> fail(error: E): IBotResult<R, E> {
            return BotResult(null, error)
        }
    }

    override fun isOK(): Boolean {
        return result != null
    }

    override fun isFail(): Boolean {
        return failure != null
    }

    override fun result(): R? {
        return this.result
    }

    override fun failure(): E? {
        return this.failure
    }

    override fun onFail(callback: Fun10<E>?): IBotResult<R, E>? {
        if (callback != null && failure != null) callback(failure)
        return if (result != null) this else null
    }

    override fun <S> onResult(onfail: Fun11<E, S>, onok: Fun11<R, S>): S {
        if (result != null) return onok(result)
        return onfail(failure!!)
    }

    override fun <S> onOK(callback: Fun11<R, S>): S {
        return callback(this.result!!)
    }
}

class ResultException(result: String) : Exception(result, null, false, false) {
}

/**
 * A simple monotonic serial counter that wraps on overflow.
 * By default, it count from 0 inclusive to Long.MAX_VALUE exclusive.
 */
open class Serial(
        protected val start: Long = 0L,
        protected val end: Long = Long.MAX_VALUE,
        protected var serial: Long = start
) {
    fun get(): Long {
        synchronized(this) {
            if (serial < start || serial == end) serial = start
            return serial++
        }
    }

    fun reset() {
        synchronized(this) {
            serial = start
        }
    }
}
