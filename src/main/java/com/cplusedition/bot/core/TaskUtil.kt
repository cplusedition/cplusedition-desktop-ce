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
import com.cplusedition.bot.core.TaskUt.defaultThreadCount
import com.cplusedition.bot.core.TaskUt.newForkJoinPool
import java.util.*
import java.util.concurrent.*

object TaskUt : TaskUtil()

open class TaskUtil {
    fun newForkJoinPool(threads: Int? = null): IJoinablePool {
        return JoinableExecutorService(threads)
    }

    fun newFixedThreadPool(threads: Int? = null): IThreadPool {
        return FixedThreadPoolSubmiter(threads ?: defaultThreadCount())
    }

    /// This should be used only if all tasks are submitted before callback() returns.
    /// The method wait for the pool to become quiescence, and thus ensure all tasks are executed
    /// then shut it down before return. However, it does not call awaitShutdown().
    /// Use forkJoinPool() if task are submitted in async tasks that may execute after callback returns.
    /// @callback(pool) The pool will shutdown after callback() returns and the pool is quiesence.
    fun <R> forkJoinTasks(
        pool: IJoinablePool = newForkJoinPool(),
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.SECONDS,
        callback: Fun11<IJoinablePool, R>
    ): R {
        try {
            return callback(pool)
        } finally {
            pool.awaitDone(timeout, timeunit)
            pool.shutdown()
        }
    }

    /// Like forkJoinTasks() but has more control in when to shutdown the pool.
    /// The method return when callback() returns. It wait for the pool to become quienscence
    /// and shut it down when done() is called. However, it does not call awaitShutdown().
    /// This should be used if jobs are submitted in async tasks that may execute after callback returns.
    /// Or client would like to keep the pool around for reuse.
    /// @param callback(pool, done) Client call done() to allow the pool to shutdown on quiesence.
    fun forkJoinAsync(
        pool: IJoinablePool = newForkJoinPool(),
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.SECONDS,
        callback: Fun20<ISubmitter, Fun00>
    ): IJoinablePool {
        try {
            callback(pool) {
                pool.awaitDone(timeout, timeunit)
                pool.shutdown()
            }
        } catch (e: Throwable) {
            pool.awaitDone(timeout, timeunit)
            pool.shutdown()
            throw e
        }
        return pool
    }

    fun <R> taskGroupPool(
        group: ITaskGroup,
        pool: IJoinablePool = newForkJoinPool(),
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.SECONDS,
        callback: Fun11<ISubmitter, R>
    ): R {
        try {
            return callback(object : ISubmitter {
                override fun submit(task: Runnable): Future<*> {
                    return pool.submit {
                        val id = group.enter()
                        try {
                            task.run()
                        } finally {
                            group.leave(id)
                        }
                    }
                }

                override fun <V> submit(task: Callable<V>): Future<V> {
                    return pool.submit(Callable {
                        val id = group.enter()
                        try {
                            task.call()
                        } finally {
                            group.leave(id)
                        }
                    })
                }
            })
        } finally {
            group.awaitDone(timeout, timeunit)
            pool.shutdown()
        }
    }

    /// Execute and wait for completion of a number of tasks using the given executor.
    /// Note that client is responsible to shutdown the input executor when done.
    fun <R> countedTaskGroupPool(
        count: Int,
        delegate: IJoinablePool = newForkJoinPool(),
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.SECONDS,
        callback: Fun11<ISubmitter, R>
    ): R {
        val pool = CountedTaskGroupExecutor(count, delegate)
        try {
            return callback(pool)
        } finally {
            pool.awaitDone(timeout, timeunit)
        }
    }

    /// Guard sync exection of the given task with the given semaphore.
    fun <R> sync(sem: Semaphore, callback: Fun01<R>): R {
        sem.acquire()
        try {
            return callback()
        } finally {
            sem.release()
        }
    }

    /// Guard async exection of the given task with the given semaphore.
    /// @param callback(done) Caller must call done() in the callback to exit
    /// the exclusive region.
    fun <R> async(sem: Semaphore, callback: Fun11<Fun00, R>): R {
        sem.acquire()
        return callback {
            sem.release()
        }
    }

    fun defaultThreadCount(): Int {
        val cpus = Runtime.getRuntime().availableProcessors()
        return if (cpus > 3) cpus / 2 else 1
    }
}

class CountedTaskGroupExecutor(
    count: Int,
    private val delegate: IJoinablePool = TaskUt.newForkJoinPool(),
) : CountedTaskGroup(count), IJoinablePool {
    override fun submit(task: Runnable): Future<*> {
        return delegate.submit {
            val id = enter()
            try {
                task.run()
            } finally {
                leave(id)
            }
        }
    }

    override fun <V> submit(task: Callable<V>): Future<V> {
        return delegate.submit(Callable {
            val id = enter()
            try {
                task.call()
            } finally {
                leave(id)
            }
        })
    }

    override fun shutdown() {
        delegate.shutdown()
    }

    override fun awaitTermination(timeout: Long, timeunit: TimeUnit): Boolean {
        return delegate.awaitTermination(timeout, timeunit)
    }
}

open class CountedTaskGroup(count: Int) : ITaskGroup {
    companion object {
        private val serial = Serial()
    }

    private val taskid = Serial()
    private val counter = CountDownLatch(count)
    val gid = serial.get()
    val pending get() = counter.count

    override fun enter(): Long {
        synchronized(taskid) {
            val tid = taskid.get()
            return tid
        }
    }

    override fun leave(tid: Long) {
        synchronized(taskid) {
            counter.countDown()
        }
    }

    override fun enterLeave() {
        synchronized(taskid) {
            counter.countDown()
        }
    }

    override fun awaitDone(timeout: Long, unit: TimeUnit): Boolean {
        return counter.await(timeout, unit)
    }
}

open class PassthroughSubmitter constructor(
    private val delegate: ISubmitter,
) : ISubmitter {
    override fun submit(task: Runnable): Future<*> {
        return delegate.submit(task)
    }

    override fun <V> submit(task: Callable<V>): Future<V> {
        return delegate.submit(task)
    }
}

open class WatchedSubmitter constructor(
    private val delegate: ISubmitter,
    private val log: ITraceLogger,
    private val name: String = "",
    private val verbose: Boolean = false,
    val timer: PerformanceWatch = PerformanceWatch()
) : ISubmitter {
    private val serial = Serial()
    private val start = System.currentTimeMillis()
    private val semaphore = Semaphore(1)
    private var submited = 0L
    private var pending = 0
    private var active = 0
    private var lastprint = 0L

    override fun submit(task: Runnable): Future<*> {
        val id = TaskUt.sync(semaphore) {
            ++submited
            ++pending
            serial.get()
        }
        return delegate.submit {
            watch(id) { task.run() }
        }
    }

    override fun <V> submit(task: Callable<V>): Future<V> {
        val id = TaskUt.sync(semaphore) {
            ++submited
            ++pending
            serial.get()
        }
        return delegate.submit<V> {
            watch(id) { task.call() }
        }
    }

    fun printStat(now: Long = System.currentTimeMillis()) {
        val (submitcount, pendingcount, activecount) = TaskUt.sync(semaphore) {
            Triple(submited, pending, active)
        }
        printstat(log, name, start, now, submitcount, pendingcount, activecount)
    }

    fun printDurations() {
        log.d(timer.durations.stats())
    }

    private fun <R> watch(id: Long, callback: Fun01<R>): R {
        val taskstart = TaskUt.sync(semaphore) {
            --pending
            ++active
            System.currentTimeMillis()
        }
        try {
            return timer.duration(name) {
                callback()
            }
        } catch (e: Throwable) {
            log.e("# $e")
            throw e
        } finally {
            val now = System.currentTimeMillis()
            val (submitcount, pendingcount, activecount, delta) = TaskUt.sync(semaphore) {
                --active
                val delta = (now - lastprint) > DELTA
                if (delta) lastprint = now
                ArrayMap.Quad(submited, pending, active, delta)
            }
            if (verbose || delta) {
                printstat(log, verbose, name, start, submitcount, pendingcount, activecount, id, taskstart)
            }
        }
    }

    companion object {
        private val DELTA = 5000
        private fun printstat(
            log: ITraceLogger,
            verbose: Boolean,
            name: String,
            start: Long,
            submitcount: Long,
            pendingcount: Int,
            activecount: Int,
            id: Long,
            taskstart: Long,
        ) {
            val now = System.currentTimeMillis()
            if (verbose) {
                log.d {
                    TextUt.format(
                        "# %s: %s: Task#%d end, %5d total, %d pending, %d active: %s elapsed",
                        name,
                        fmt((now - start) / 1000.0),
                        id,
                        submitcount,
                        pendingcount,
                        activecount,
                        fmt((now - taskstart) / 1000.0)
                    )
                }
            } else {
                printstat(log, name, start, now, submitcount, pendingcount, activecount)
            }
        }

        private fun printstat(
            log: ITraceLogger,
            name: String,
            start: Long,
            now: Long,
            submitcount: Long,
            pendingcount: Int,
            activecount: Int
        ) {
            log.d {
                TextUt.format(
                    "# %s: %s: %5d total, %d pending, %d active",
                    name,
                    fmt((now - start) / 1000.0),
                    submitcount,
                    pendingcount,
                    activecount
                )
            }
        }
    }
}

interface ITaskGroup : IJoinable {
    fun enter(): Long
    fun leave(id: Long)

    /// Shorthand for leave(enter())
    fun enterLeave()
}

interface ISubmitter {
    fun submit(task: Runnable): Future<*>
    fun <R> submit(task: Callable<R>): Future<R>
}

interface IGroupSubmitter : ISubmitter {
    fun <R> submit(group: ITaskGroup, task: Callable<R>): Future<R>
}

interface IJoinable {
    fun awaitDone(timeout: Long, timeunit: TimeUnit): Boolean
}

interface IShutdownable {
    fun shutdown()
    fun awaitTermination(timeout: Long, timeunit: TimeUnit): Boolean
}

interface IJoinableSubmitter : ISubmitter, IJoinable

interface ITaskGroupPool : IGroupSubmitter, IShutdownable

interface IThreadPool : ISubmitter, IShutdownable

interface IJoinablePool : IThreadPool, IJoinable

open class JoinableExecutorService(threads: Int? = null) : ForkJoinPool(
    (threads ?: defaultThreadCount()),
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null, true
), IJoinablePool {
    override fun awaitDone(timeout: Long, timeunit: TimeUnit): Boolean {
        return awaitQuiescence(timeout, timeunit)
    }
}

open class FixedThreadPoolSubmiter(threads: Int) : ThreadPoolExecutor(
    threads, threads,
    0L, TimeUnit.MILLISECONDS,
    LinkedBlockingQueue(),
), IThreadPool

open class WorkerThreadPool constructor(
    private val pool: IThreadPool = newForkJoinPool(defaultThreadCount())
) : ITaskGroupPool {

    override fun <R> submit(group: ITaskGroup, task: Callable<R>): Future<R> {
        return pool.submit(Callable {
            val id = group.enter()
            try {
                task.call()
            } finally {
                group.leave(id)
            }
        })
    }

    override fun submit(task: Runnable): Future<*> {
        return pool.submit(task)
    }

    override fun <R> submit(task: Callable<R>): Future<R> {
        return pool.submit(task)
    }

    override fun shutdown() {
        pool.shutdown()
    }

    override fun awaitTermination(timeout: Long, timeunit: TimeUnit): Boolean {
        return pool.awaitTermination(timeout, timeunit)
    }
}

interface IResourceLocker<K> {
    fun <R> sync(key: K, callback: Fun01<R>): R
    fun <R> async(key: K, callback: Fun11<Fun00, R>): R

    /// Wait for all tasks completed.
    /// @return false if timeout.
    fun join(timeout: Long, timeunit: TimeUnit): Boolean
}

open class ResourceLocker<K> : IResourceLocker<K> {
    private val mylock = Semaphore(1)
    private val waiters = TreeMap<K, Waiter<K>>()
    private val joiners = ArrayList<Semaphore>()

    override fun <R> sync(key: K, callback: Fun01<R>): R {
        return this.async(key) { done ->
            try {
                callback()
            } finally {
                done()
            }
        }
    }

    override fun <R> async(key: K, callback: Fun11<Fun00, R>): R {
        mylock.acquire()
        val waiter = try {
            val ret = waiters.bot.getOrCreate(key) { Waiter(Semaphore(1, true), key, 0) }
            ++ret.pending
            ret
        } finally {
            mylock.release()
        }
        waiter.sem.acquire()
        return callback {
            unlock(waiter.key)
        }
    }

    /// Wait for all tasks completed.
    /// @return false if timeout.
    override fun join(timeout: Long, timeunit: TimeUnit): Boolean {
        mylock.acquire()
        val sem = try {
            if (waiters.isNotEmpty()) {
                val sem = Semaphore(0)
                joiners.add(sem)
                sem
            } else null
        } finally {
            mylock.release()
        }
        return sem?.tryAcquire(timeout, timeunit) ?: true
    }

    private fun unlock(key: K) {
        mylock.acquire()
        try {
            waiters.get(key)?.let {
                it.sem.release()
                --it.pending
                if (it.pending == 0) {
                    waiters.remove(key)
                }
            }
            if (waiters.isEmpty()) {
                joiners.forEach { it.release() }
                joiners.clear()
            }
        } finally {
            mylock.release()
        }
    }

    data class Waiter<K> constructor(
        val sem: Semaphore,
        val key: K,
        var pending: Int
    )
}
