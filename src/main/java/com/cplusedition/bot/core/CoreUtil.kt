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
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

interface ISetter<T> {
    fun setter(callback: Fun10<T>)
}

/// A property setter protected by a ReentrantLock.
class MutexSetter<T>(
    private val property: T
) : ISetter<T> {
    private val lock = Any()
    override fun setter(callback: Fun10<T>) {
        synchronized(lock) {
            callback(property)
        }
    }
}

/// A property setter protected by a Semaphore.
class SemaphoreSetter<T>(
    private val property: T
) : ISetter<T> {
    private val sema = Semaphore(1)
    override fun setter(callback: Fun10<T>) {
        TaskUt.sync(sema) {
            callback(property)
        }
    }
}

interface IObjectPoolDelegate<T> {
    fun ctor(): T

    fun onget(a: T, size: Int): T

    fun onunget(a: T, size: Int): T?
}

abstract class ObjectPoolDelegate<T> : IObjectPoolDelegate<T> {
    override fun onunget(a: T, size: Int): T? {
        return a
    }

    override fun onget(a: T, size: Int): T {
        return a
    }
}

interface IPool<T> {
    fun get(): T?
    fun unget(item: T)
    fun size(): Int
}

class Pool<T> constructor(private val _limit: Int) : IPool<T> {
    private val _queue: Queue<T> = ConcurrentLinkedDeque()

    override fun size(): Int {
        return this._limit
    }

    override fun unget(item: T) {
        if (_queue.size >= _limit) return
        _queue.add(item)
    }

    override fun get(): T? {
        return _queue.poll()
    }
}

interface IObjectPool<T> : IPool<T> {
    fun <R> use(callback: Fun11<T, R>): R
    fun <R> lock(callback: Fun01<R>): R
    fun clear()
}

open class ObjectPool<T> constructor(
    private val delegate: IObjectPoolDelegate<T>,
) : IObjectPool<T> {
    protected val pool = LinkedList<T>()
    protected val lock = ReentrantLock()

    constructor(creator: Fun01<T>) : this(object : ObjectPoolDelegate<T>() {
        override fun ctor(): T {
            return creator()
        }
    })

    override fun get(): T {
        lock.withLock {
            return pool.pollFirst()?.let {
                delegate.onget(it, pool.size)
            } ?: delegate.ctor()
        }
    }

    override fun unget(a: T) {
        lock.withLock {
            delegate.onunget(a, pool.size)?.let {
                pool.addFirst(a)
            }
        }
    }

    override fun size(): Int {
        lock.withLock { return pool.size }
    }

    override fun <R> use(callback: Fun11<T, R>): R {
        val a = get()
        try {
            return callback(a)
        } finally {
            unget(a)
        }
    }

    override fun <R> lock(callback: Fun01<R>): R {
        return lock.withLock(callback)
    }

    override fun clear() {
        lock.withLock {
            pool.clear()
        }
    }
}

interface ITaggedObjectPool<K, V> {
    fun get(tag: K): V
    fun unget(tag: K, a: V)
    fun <R> use(tag: K, callback: Fun11<V, R>): R
    fun <R> lock(callback: Fun01<R>): R
    fun size(tag: K): Int
    fun clear()
}

interface ITaggedObjectPoolDelegate<K, V> {
    fun ctor(tag: K): V

    fun onget(tag: K, a: V, size: Int): V

    fun onunget(tag: K, a: V, size: Int): V?
}

abstract class TaggedObjectPoolDelegate<K, V> : ITaggedObjectPoolDelegate<K, V> {
    override fun onget(tag: K, a: V, size: Int): V {
        return a
    }

    override fun onunget(tag: K, a: V, size: Int): V? {
        return a
    }
}

/// Note that all pool must contains objects of same type, typically an interface type.
open class TaggedObjectPool<K, V> constructor(
    private val delegate: ITaggedObjectPoolDelegate<K, V>,
) : ITaggedObjectPool<K, V> {
    protected val pools = ArrayMap<K, ObjectPool<V>>()
    protected val lock = ReentrantLock()
    protected var size = 0

    constructor(creator: Fun11<K, V>) : this(object : TaggedObjectPoolDelegate<K, V>() {
        override fun ctor(tag: K): V {
            return creator(tag)
        }
    })

    override fun get(tag: K): V {
        lock.withLock {
            return getpool(tag).get()
        }
    }

    override fun unget(tag: K, a: V) {
        lock.withLock {
            getpool(tag).unget(a)
        }
    }

    override fun size(tag: K): Int {
        lock.withLock {
            return getpool(tag).size()
        }
    }

    override fun <R> use(tag: K, callback: Fun11<V, R>): R {
        val a = get(tag)
        try {
            return callback(a)
        } finally {
            unget(tag, a)
        }
    }

    override fun <R> lock(callback: Fun01<R>): R {
        return lock.withLock(callback)
    }

    override fun clear() {
        lock.withLock {
            pools.clear()
        }
    }

    private fun getpool(tag: K): ObjectPool<V> {
        return pools.get(tag) ?: ObjectPool(object : IObjectPoolDelegate<V> {
            override fun ctor(): V {
                return delegate.ctor(tag)
            }

            override fun onunget(a: V, size: Int): V? {
                return delegate.onunget(tag, a, size)
            }

            override fun onget(a: V, size: Int): V {
                return delegate.onget(tag, a, size)
            }
        }
        ).also { pools.put(tag, it) }
    }
}

interface Disposable {
    fun dispose()

    fun <R> Disposable.use(code: Fun11<Disposable, R>): R {
        try {
            return code(this)
        } finally {
            this.dispose()
        }
    }
}

class TmpFile constructor(
    val file: File
) : AutoCloseable {
    override fun close() {
        file.delete()
    }

    fun <R> TmpFile.use(code: Fun11<File, R>): R {
        try {
            return code(this.file)
        } finally {
            this.file.delete()
        }
    }
}

/**
 * Typically, result is handled null safe as:
 *     result.onFail { E -> ... }?.onOK { R -> ... } or
 *     result.onResult({ E -> ... }, { R -> ... })
 */
interface IBotResult<R, E> {

    fun isOK(): Boolean
    fun isFail(): Boolean
    fun result(): R?
    fun failure(): E?

    /// If fail, call callback and return null, otherwise return self.
    fun onFail(onfail: Fun10<E>): IBotOKResult<R, E>?

    /// If fail, return result of onfail(), otherwise return result of onok.
    fun <S> onResult(onfail: Fun11<E, S>, onok: Fun11<R, S>): S

    /// If fail, call callback and return null, otherwise return OK result.
    fun onFailOrResult(onfail: Fun10<E>): R?

    /// If fail return result of callback, otherwise return null.
    fun <S> ifFail(onfail: Fun11<E, S>): S?

    /// If OK return result of callback, else return null.
    fun <S> ifOK(onok: Fun11<R, S>): S?

    /// If OK, return result of callback, else the failure.
    fun <S> mapOK(onok: Fun11<R, IBotResult<S, E>>): IBotResult<S, E>

    /// If fail, return result of callback, else the OK result.
    fun <S> mapFail(onfail: Fun11<E, IBotResult<R, S>>): IBotResult<R, S>
}

interface IBotOKResult<R, S> : IBotResult<R, S> {
    /// Assume OK and call callback. Typical use as onFail()?.onOK().
    fun <S> onOK(onok: Fun11<R, S>): S
}

class BotResult<R, E> private constructor(
    private val result: R? = null,
    private val failure: E? = null
) : IBotOKResult<R, E> {
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

    override fun onFail(onfail: Fun10<E>): IBotOKResult<R, E>? {
        if (failure != null) {
            onfail(failure)
            return null
        }
        return this
    }

    override fun <S> onOK(onok: Fun11<R, S>): S {
        return onok(this.result!!)
    }

    override fun <S> onResult(onfail: Fun11<E, S>, onok: Fun11<R, S>): S {
        if (result != null) return onok(result)
        return onfail(failure!!)
    }

    override fun onFailOrResult(onfail: Fun10<E>): R? {
        if (failure != null) {
            onfail(failure)
            return null
        }
        return result
    }

    override fun <S> ifFail(onfail: Fun11<E, S>): S? {
        return if (failure != null) onfail(failure) else null
    }

    override fun <S> ifOK(onok: Fun11<R, S>): S? {
        return if (result != null) onok(result) else null
    }

    override fun <S> mapOK(onok: Fun11<R, IBotResult<S, E>>): IBotResult<S, E> {
        return if (result != null) onok(result) else fail(failure!!)
    }

    override fun <S> mapFail(onfail: Fun11<E, IBotResult<R, S>>): IBotResult<R, S> {
        return if (failure != null) onfail(failure) else ok(result!!)
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

/**
 * A simple monotonic serial counter that wraps on overflow.
 * By default, it count from 0 inclusive to Int.MAX_VALUE exclusive.
 */
open class Serial32(
    protected val start: Int = 0,
    protected val end: Int = Int.MAX_VALUE,
    protected var serial: Int = start
) {
    fun get(): Int {
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

object Empty {
    val booleanArray = BooleanArray(0)
    val byteArray = ByteArray(0)
    val charArray = CharArray(0)
    val intArray = IntArray(0)
    val longArray = LongArray(0)
    val stringArray = arrayOf<String>()
    val anyArray = arrayOf<Any>()
}

/** A simple value holder. */
open class Value<T>(var value: T) {
}

open class TodoException : RuntimeException("TODO") {
    private val serialVersionUID = 1L
}
