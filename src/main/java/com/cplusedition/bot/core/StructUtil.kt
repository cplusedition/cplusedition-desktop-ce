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

import java.util.*

object StructUt : StructUtil()

open class StructUtil {

    fun <T> concat(vararg a: Collection<T>): MutableList<T> {
        val ret = ArrayList<T>()
        for (c in a) {
            ret.addAll(c)
        }
        return ret
    }

    fun concat(vararg aa: ByteArray): ByteArray {
        var len = 0
        for (a in aa) {
            len += a.size
        }
        val ret = ByteArray(len)
        var offset = 0
        for (a in aa) {
            System.arraycopy(a, 0, ret, offset, a.size)
            offset += a.size
        }
        return ret
    }

    fun equals(b1: ByteArray, b2: ByteArray): Boolean {
        if (b1.size != b2.size) return false
        for (i in 0..b1.lastIndex) {
            if (b1[i] != b2[i]) return false
        }
        return true
    }

    fun equals(a: ByteArray, b: ByteArray, boff: Int, blen: Int): Boolean {
        if (a.size != blen) {
            return false
        }
        for (i in 0 until blen) {
            if (a[i] != b[boff + i]) {
                return false
            }
        }
        return true
    }

    fun equals(a: ByteArray, aoff: Int, alen: Int, b: ByteArray, boff: Int, blen: Int): Boolean {
        if (alen != blen) {
            return false
        }
        for (i in 0 until blen) {
            if (a[aoff + i] != b[boff + i]) {
                return false
            }
        }
        return true
    }

    fun <T> equals(b1: Array<T>, b2: Array<T>): Boolean {
        if (b1.size != b2.size) return false
        for (i in 0..b1.lastIndex) {
            if (b1[i] != b2[i]) return false
        }
        return true
    }

    fun <T> equals(a: Array<T>, b: Array<T>, boff: Int, blen: Int): Boolean {
        if (a.size != blen) {
            return false
        }
        for (i in 0 until blen) {
            if (a[i] != b[boff + i]) {
                return false
            }
        }
        return true
    }

    fun <T> equals(a: Array<T>, aoff: Int, alen: Int, b: Array<T>, boff: Int, blen: Int): Boolean {
        if (alen != blen) {
            return false
        }
        for (i in 0 until blen) {
            if (a[aoff + i] != b[boff + i]) {
                return false
            }
        }
        return true
    }

    fun <T> equals(a: Collection<T>, b: Collection<T>): Boolean {
        if (a.size != b.size) return false
        val ita = a.iterator()
        val itb = b.iterator()
        while (ita.hasNext()) {
            if (ita.next() != itb.next()) return false
        }
        return true
    }

    fun <T : Comparable<T>> diff(a: Set<T>, b: Set<T>): DiffStat<T> {
        val ret = DiffStat<T>()
        ret.bonly.addAll(b)
        for (e in a) {
            if (ret.bonly.remove(e)) {
                ret.sames.add(e)
            } else {
                ret.aonly.add(e)
            }
        }
        return ret
    }

    fun <T> diff(a: Map<String, T>, b: Map<String, T>): DiffStat<String> {
        val ret = DiffStat<String>()
        ret.bonly.addAll(b.keys)
        for ((ka, va) in a) {
            if (!ret.bonly.remove(ka)) {
                ret.aonly.add(ka)
                continue
            }
            val vb = b[ka]
            if (va == vb) {
                ret.sames.add(ka)
            } else {
                ret.diffs.add(ka)
            }
        }
        return ret
    }

    /**
     * @return -1 if a==null && b!=null,
     * 1 if a!=null && b==null,
     * 0, if a==null && b== null,
     * else return callback(a!!, b!!).
     */
    fun <T> nullableCompare(a: T?, b: T?, callback: (T, T) -> Int): Int {
        return if (a == null) {
            if (b == null) 0 else -1
        } else {
            if (b == null) 1 else callback(a, b)
        }
    }

    fun <T> nullableEquals(a: T?, b: T?, callback: (T, T) -> Boolean): Boolean {
        if (a == null) return b == null
        if (b == null) return false
        return callback(a, b)
    }

    fun <K, V> putList(ret: MutableMap<K, MutableList<V>>, key: K, vararg values: V) {
        val c = ret[key] ?: ArrayList<V>().also { ret[key] = it }
        c.bot.adding(*values)
    }

    fun <K, V> putList(ret: MutableMap<K, MutableList<V>>, key: K, values: Iterable<V>) {
        val c = ret[key] ?: ArrayList<V>().also { ret[key] = it }
        c.addAll(values)
    }

    fun <K, KK, VV> putTreeMap(ret: MutableMap<K, MutableMap<KK, VV>>, key1: K, key: KK, value: VV) {
        val c = ret[key1] ?: TreeMap<KK, VV>().also { ret[key1] = it }
        c[key] = value
    }

    fun byteArray(vararg a: Int): ByteArray {
        return ByteArray(a.size) { a[it].toByte() }
    }
}

open class DiffStat<T : Comparable<T>> {
    val aonly = TreeSet<T>()
    val bonly = TreeSet<T>()
    val diffs = TreeSet<T>()
    val sames = TreeSet<T>()

    fun hasDiff(): Boolean {
        return aonly.size > 0 || bonly.size > 0 || diffs.size > 0
    }

    override fun toString(): String {
        return toString("A", "B")
    }

    fun toString(
        msg1: String,
        msg2: String,
        printsames: Boolean = false,
        printaonly: Boolean = true,
        printbonly: Boolean = true,
        printdiffs: Boolean = true
    ): String {
        val w = StringPrintWriter()
        if (printsames) {
            w.println("### Same: ${sames.size}")
            sames.forEach { w.println(it.toString()) }
        }
        if (printaonly) {
            w.println("### $msg1 only: ${aonly.size}")
            aonly.forEach { w.println(it.toString()) }
        }
        if (printbonly) {
            w.println("### $msg2 only: ${bonly.size}")
            bonly.forEach { w.println(it.toString()) }
        }
        if (printdiffs) {
            w.println("### Diff: ${diffs.size}")
            diffs.forEach { w.println(it.toString()) }
        }
        return w.toString()
    }
}

interface IEqualityMap<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V): V?
    fun remove(key: K): V?
    fun containsKey(key: K): Boolean
    fun containsValue(value: V): Boolean
    fun isEmpty(): Boolean
    fun iterator(): Iterator<K>
    fun clear()
}

/// A simple compact map that only require key with equality operator.
class ArrayMap<K, V>(
    cap: Int = 4
) : IEqualityMap<K, V> {
    private val _keys = ArrayList<K>(cap)
    private val _values = ArrayList<V>(cap)
    override fun get(key: K): V? {
        val index = _keys.indexOf(key)
        return if (index >= 0) _values.get(index) else null
    }

    override fun put(key: K, value: V): V? {
        val index = _keys.indexOf(key)
        if (index >= 0) {
            val ret = _values.get(index)
            _values.set(index, value)
            return ret
        } else {
            _keys.add(key)
            _values.add(value)
            return null
        }
    }

    override fun remove(key: K): V? {
        val index = _keys.indexOf(key)
        if (index >= 0) {
            val ret = _values.get(index)
            _keys.removeAt(index)
            _values.removeAt(index)
            return ret
        }
        return null
    }

    override fun containsKey(key: K): Boolean {
        return _keys.indexOf(key) >= 0
    }

    override fun containsValue(value: V): Boolean {
        return _values.indexOf(value) >= 0
    }

    override fun isEmpty(): Boolean {
        return _keys.isEmpty()
    }

    override fun iterator(): Iterator<K> {
        return _keys.iterator()
    }

    override fun clear() {
        _keys.clear()
        _values.clear()
    }

    data class Quad<T1, T2, T3, T4> constructor(
        val first: T1,
        val second: T2,
        val third: T3,
        val fourth: T4,
    )
}
