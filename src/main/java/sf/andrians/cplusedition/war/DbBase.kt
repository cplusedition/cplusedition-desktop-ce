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
package sf.andrians.cplusedition.war

import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.Fun01
import java.io.Closeable
import java.sql.Connection
import java.sql.Savepoint
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

class FileLocker {
    private val lock = ReentrantLock()
    private val locks = TreeMap<Long, Int>()
    private var paused = false
    fun lock(fileid: Long, write: Boolean): Boolean {
        lock.withLock {
            if (paused) return false
            val value = locks[fileid]
            if (write) {
                if (value != null) return false
                locks[fileid] = -1
                return true
            }
            if (value != null && value <= 0) return false
            locks[fileid] = if (value == null) 1 else value + 1
            return true
        }
    }

    fun unlock(fileid: Long) {
        lock.withLock {
            val value = locks[fileid] ?: return
            if (value <= 1) {
                locks.remove(fileid)
                return
            }
            locks.put(fileid, value - 1)
        }
    }

    fun size(): Int {
        lock.withLock { return locks.size }
    }

    fun isEmpty(): Boolean {
        lock.withLock { return locks.isEmpty() }
    }

    fun clear() {
        lock.withLock { locks.clear() }
    }

    fun pause(): Boolean {
        lock.withLock {
            paused = true
            return locks.isEmpty()
        }
    }
}

abstract class DbBase(
        protected val dbpath: String
) {

    object K {
        const val PART_SIZE = 1024 * 1024  // B
        const val AUTO_CLOSE = 5 * 1000L // 5 sec idle.
        const val AUTO_CLOSE_TICK = 1 * 1000L
        const val CLOSE_DB_ITERS = 40 // 8 sec.
        const val CLOSE_DB_TICK = 200L // ms
    }

    val lock = ReentrantReadWriteLock()
    protected val fileLocks = FileLocker()
    protected var connection: Connection? = null
    protected var transactionStack = Stack<Savepoint>()
    protected var paused = false

    fun isClosed(): Boolean {
        lock.read {
            return connection?.isClosed ?: true
        }
    }

    fun <T> transaction(code: Fun01<T>): T {
        transactionStack.push(connection!!.setSavepoint())
        val ret: T
        try {
            ret = code()
            connection!!.releaseSavepoint(transactionStack.pop())
            //#NOTE As of sqlite-jdbc-3.34.0 releaseSavepoint don't seems to commit the transaction.
            //# Doing it manually here.
            if (transactionStack.isEmpty()) connection!!.commit()
            return ret
        } catch (e: Throwable) {
            val savepoint = transactionStack.pop()
            connection!!.rollback(savepoint)
            throw e
        }
    }

    fun closeDatabase() {
        lock.write {
            connection?.let { conn ->
                val name = Basepath.nameWithoutSuffix(dbpath)
                for (i in 0 until K.CLOSE_DB_ITERS) {
                    
                    if (transactionStack.isEmpty() && fileLocks.pause()) break
                    Thread.sleep(K.CLOSE_DB_TICK)
                }
                transactionStack.clear()
                fileLocks.clear()
                try {
                    conn.close()
                } catch (e: Throwable) {
                    Conf.e("Close connection failed: $name");
                } finally {
                    connection = null
                }
            }
        }
    }

    fun onPause() {
        lock.write {
            paused = true
            closeDatabase()
        }
    }

    protected fun lockFile(fileid: Long, write: Boolean): Closeable? {
        return Dbfs.DbfsLock {}
    }
}
