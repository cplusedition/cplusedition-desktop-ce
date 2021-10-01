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
package sf.andrians.ancoreutil.util.struct

import java.lang.reflect.Array.newInstance
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Some static utilities for data structure manipulation.
 */
object StructUtil {
    ////////////////////////////////////////////////////////////////////////

    /** Sort given array with the primary and secondary keys.  */
    fun <T> doubleSort(a: Array<T>, c1: Comparator<T>, c2: Comparator<T>) {
        Arrays.sort(a, c1)
        var start = 0
        var end = 1
        val a1 = a[start]
        var a2: T
        while (end < a.size) {
            a2 = a[end]
            if (c1.compare(a1, a2) == 0) {
                ++end
                continue
            }
            Arrays.sort(a, start, end, c2)
            start = end
            ++end
        }
        if (end > start) {
            Arrays.sort(a, start, end, c2)
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun <T> toHashSet(vararg a: T): MutableSet<T> {
        val ret = HashSet<T>()
        Collections.addAll(ret, *a)
        return ret
    }

    fun <T> toHashSet(start: Int, end: Int, vararg args: T): MutableSet<T> {
        val ret = HashSet<T>()
        ret.addAll(Arrays.asList(*args).subList(start, end))
        return ret
    }

    fun <T> toTreeSet(vararg a: T): MutableSet<T> {
        val ret = TreeSet<T>()
        Collections.addAll(ret, *a)
        return ret
    }

    fun <T> toTreeSet(c: Comparator<T>, vararg a: T): MutableSet<T> {
        val ret = TreeSet<T>(c)
        Collections.addAll(ret, *a)
        return ret
    }

    fun <T> toTreeSet(start: Int, end: Int, vararg args: T): MutableSet<T> {
        val ret = TreeSet<T>()
        ret.addAll(Arrays.asList(*args).subList(start, end))
        return ret
    }

    fun <T> toTreeSet(c: Comparator<T>, start: Int, end: Int, vararg args: T): MutableSet<T> {
        val ret = TreeSet<T>(c)
        ret.addAll(Arrays.asList(*args).subList(start, end))
        return ret
    }

    fun <T> toList(vararg a: T): MutableList<T> {
        val ret = ArrayList<T>(a.size)
        Collections.addAll(ret, *a)
        return ret
    }

    fun <T> toList(start: Int, end: Int, vararg args: T): MutableList<T> {
        val ret = ArrayList<T>(args.size)
        ret.addAll(Arrays.asList(*args).subList(start, end))
        return ret
    }

    fun <T> toListNotNull(vararg a: T?): MutableList<T> {
        val ret = ArrayList<T>(a.size)
        for (s in a) {
            if (s != null) {
                ret.add(s)
            }
        }
        return ret
    }

    fun <T> toListNotNull(start: Int, end: Int, vararg args: T?): MutableList<T> {
        val ret = ArrayList<T>(args.size)
        for (i in start until end) {
            val arg = args[i]
            if (arg != null) {
                ret.add(arg)
            }
        }
        return ret
    }

    fun <E, T : MutableList<E>> toList(ret: T, it: Iterator<E>): T {
        while (it.hasNext()) {
            ret.add(it.next())
        }
        return ret
    }

    fun <T> toArray(ret: Array<T>, offset: Int, it: Iterator<T>): Array<T> {
        var offset = offset
        while (it.hasNext()) {
            ret[offset++] = it.next()
        }
        return ret
    }

    fun <K, V> toTreeMap1(key: K, value: V): TreeMap<K, V> {
        val ret = TreeMap<K, V>()
        ret[key] = value
        return ret
    }

    fun <K, V> toTreeMap(vararg keyvalues: Any): TreeMap<K, V> {
        val ret = TreeMap<K, V>()
        var i = 0
        while (i < keyvalues.size) {
            ret[keyvalues[i] as K] = keyvalues[i + 1] as V
            i += 2
        }
        return ret
    }

    fun <K, V> toHashMap1(key: K, value: V): HashMap<K, V> {
        val ret = HashMap<K, V>()
        ret[key] = value
        return ret
    }

    fun <K, V> toHashMap(vararg keyvalues: Any): HashMap<K, V> {
        val ret = HashMap<K, V>()
        var i = 0
        while (i < keyvalues.size) {
            ret[keyvalues[i] as K] = keyvalues[i + 1] as V
            i += 2
        }
        return ret
    }

    ////////////////////////////////////////////////////////////////////////

    fun <K, V> getList(map: MutableMap<K, List<V>>, key: K): List<V> {
        return map[key] ?: ArrayList<V>().also { map[key] = it }
    }

    fun <K, V> getTreeSet(map: MutableMap<K, TreeSet<V>>, key: K, value: V): TreeSet<V> {
        return map[key] ?: TreeSet<V>().also { map[key] = it }
    }

    fun <K, V> getHashSet(map: MutableMap<K, HashSet<V>>, key: K, value: V): HashSet<V> {
        return map[key] ?: HashSet<V>().also { map[key] = it }
    }

    fun <K, KK, VV> getTreeMap(map: MutableMap<K, TreeMap<KK, VV>>, key: K): TreeMap<KK, VV> {
        return map[key] ?: TreeMap<KK, VV>().also { map[key] = it }
    }

    fun <K, KK, VV> getHashMap(map: MutableMap<K, HashMap<KK, VV>>, key: K): HashMap<KK, VV> {
        return map[key] ?: HashMap<KK, VV>().also { map[key] = it }
    }

    ////////////////////////////////////////////////////////////////////////

    fun <K, V> addToCollection(map: MutableMap<K, MutableCollection<V>>, key: K, value: V): Boolean {
        return (map[key] ?: ArrayList<V>().also {
            map[key] = it
        }).add(value)
    }

    fun <K, V> addToList(map: MutableMap<K, MutableList<V>>, key: K, value: V): Boolean {
        return (map[key] ?: ArrayList<V>(4).also {
            map[key] = it
        }).add(value)
    }

    fun <K, V> addToTreeSet(map: MutableMap<K, MutableSet<V>>, key: K, value: V): Boolean {
        return (map[key] ?: TreeSet<V>().also {
            map[key] = it
        }).add(value)
    }

    fun <K, V> addToHashSet(map: MutableMap<K, MutableSet<V>>, key: K, value: V): Boolean {
        return (map[key] ?: HashSet<V>().also {
            map[key] = it
        }).add(value)
    }

    fun <T, K, V> addHashMapToMap(map: MutableMap<T, MutableMap<K, V>>, key: T, ekey: K, evalue: V): V? {
        return (map[key] ?: HashMap<K, V>().also {
            map[key] = it
        }).put(ekey, evalue)
    }

    fun <T, K, V> addTreeMapToMap(map: MutableMap<T, MutableMap<K, V>>, key: T, ekey: K, evalue: V): V? {
        return (map[key] ?: TreeMap<K, V>().also {
            map[key] = it
        }).put(ekey, evalue)
    }

    ////////////////////////////////////////////////////////////////////////

    fun <K, V> addToCollection(map: MutableMap<K, MutableCollection<V>>, key: K, values: Collection<V>): Boolean {
        return (map[key] ?: ArrayList<V>().also {
            map[key] = it
        }).addAll(values)
    }

    fun <K, V> addToList(map: MutableMap<K, MutableList<V>>, key: K, values: Collection<V>): Boolean {
        return (map[key] ?: ArrayList<V>().also {
            map[key] = it
        }).addAll(values)
    }

    fun <K, V> addToTreeSet(map: MutableMap<K, MutableSet<V>>, key: K, values: Collection<V>): Boolean {
        return (map[key] ?: TreeSet<V>().also {
            map[key] = it
        }).addAll(values)
    }

    fun <K, V> addToHashSet(map: MutableMap<K, MutableSet<V>>, key: K, values: Collection<V>): Boolean {
        return (map[key] ?: HashSet<V>().also {
            map[key] = it
        }).addAll(values)
    }

    fun <K, VK, VV> addToTreeMap(map: MutableMap<K, MutableMap<VK, VV>>, key: K, vkey: VK, value: VV): VV? {
        return (map[key] ?: TreeMap<VK, VV>().also {
            map[key] = it
        }).put(vkey, value)
    }

    fun <K, VK, VV> addToHashMap(map: MutableMap<K, MutableMap<VK, VV>>, key: K, vkey: VK, value: VV): VV? {
        return (map[key] ?: HashMap<VK, VV>().also {
            map[key] = it
        }).put(vkey, value)
    }

    ////////////////////////////////////////////////////////////////////////

    fun eq(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else a == b
    }

    fun <K, V> valueCount(map: Map<K, Collection<V>?>): Int {
        var count = 0
        for ((_, v) in map) {
            if (v != null) {
                count += v.size
            }
        }
        return count
    }

    fun <T> addAll(a: MutableCollection<T>, vararg args: T): Boolean {
        var ret = false
        for (arg in args) {
            ret = ret or a.add(arg)
        }
        return ret
    }

    fun <T> splice(start: Int, vararg a: T): Array<T> {
        return splice(start, a.size, *a)
    }

    fun <T> splice(start: Int, end: Int, vararg a: T): Array<T> {
        return Arrays.copyOfRange(a, start, end)
    }

    fun <T> concat(a: Array<T>, vararg b: T): Array<T> {
        if (b.isEmpty()) return a
        if (a.isEmpty()) return Arrays.copyOf(b, b.size)
        val ret = Arrays.copyOf(a, a.size + b.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    fun <T> concat(a: T, vararg b: T): Array<T> {
        val ret = newInstance(b.javaClass.componentType, 1 + b.size) as Array<T>
        ret[0] = a
        if (b.isEmpty()) return ret
        System.arraycopy(b, 0, ret, 1, b.size)
        return ret
    }

    fun concat(a: Int, vararg b: Int): IntArray {
        val ret = IntArray(1 + b.size)
        ret[0] = a
        if (b.isEmpty()) return ret
        System.arraycopy(b, 0, ret, 1, b.size)
        return ret
    }

    fun concat(a: IntArray, vararg b: Int): IntArray {
        if (b.isEmpty()) return a
        if (a.isEmpty()) return b.clone()
        val ret = IntArray(a.size + b.size)
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    fun concat(a: ByteArray, vararg b: Byte): ByteArray {
        if (b.isEmpty()) return a
        if (a.isEmpty()) return b.clone()
        val ret = ByteArray(a.size + b.size)
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    fun concatArrays(aa: Collection<ByteArray>): ByteArray {
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

    fun concatArrays(vararg aa: ByteArray): ByteArray {
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

    fun concatNoClone(a: IntArray, vararg b: Int): IntArray {
        if (b.isEmpty()) return a
        if (a.isEmpty()) return b
        val ret = IntArray(a.size + b.size)
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    fun <T> concatClone(a: Array<T>, vararg b: T): Array<T> {
        if (b.isEmpty()) return a.clone()
        if (a.isEmpty()) return Arrays.copyOf(b, b.size)
        val ret = newInstance(a.javaClass.componentType, a.size + b.size) as Array<T>
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * @return    index >= 0 if found, otherwise -ret-1 would be the insertion index.
     * ie a[-ret-2] < key < a[-ret-1]
     */
    fun binarySearch(a: IntArray, start: Int, end: Int, key: Int): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            if (a[mid] < key) {
                start = mid + 1
            } else if (a[mid] > key) {
                end = mid - 1
            } else {
                return mid
            }
        }
        return -(start + 1)
    }

    /**
     * @return    index >= 0 if found, otherwise -ret-1 would be the insertion index.
     * ie a[-ret-2] < key < a[-ret-1]
     */
    fun binarySearch(a: FloatArray, start: Int, end: Int, key: Int): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            if (a[mid] < key) {
                start = mid + 1
            } else if (a[mid] > key) {
                end = mid - 1
            } else {
                return mid
            }
        }
        return -(start + 1)
    }

    /**
     * @return    index>=0 if found, otherwise -ret-1 would be the insertion index
     * ie. a[-ret-2]<key></key><a></a>[-ret-1]
     */
    fun <T : Comparable<T>?> binarySearch(a: Array<T>, start: Int, end: Int, key: T): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            val c = a[mid]!!.compareTo(key)
            if (c < 0) {
                start = mid + 1
            } else if (c > 0) {
                end = mid - 1
            } else {
                return mid
            }
        }
        return -(start + 1)
    }

    fun insertionIndex(a: IntArray, x: Int): Int {
        return insertionIndex(a, 0, a.size, x)
    }

    /**
     * Binary search for insertion point of 'x' in the given range of 'a' .
     * Returned index is the insertion point for 'x' regardless a match is found or not.
     *
     * @return Index in 'a' such that a[index-1] <=x <a></a>[index]
     */
    fun insertionIndex(a: IntArray, start: Int, end: Int, x: Int): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            if (a[mid] < x) {
                start = mid + 1
            } else if (a[mid] > x) {
                end = mid - 1
            } else {
                return mid + 1
            }
        }
        return start
    }

    fun insertionIndex(a: FloatArray, x: Int): Int {
        return insertionIndex(a, 0, a.size, x)
    }

    /**
     * Binary search for insertion point of 'x' in the given range of 'a'.
     * Returned index is the insertion point for 'x' regardless a match is found or not.
     *
     * @return Index in 'a' such that a[index-1] <=x <a></a>[index]
     */
    fun insertionIndex(a: FloatArray, start: Int, end: Int, x: Int): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            if (a[mid] < x) {
                start = mid + 1
            } else if (a[mid] > x) {
                end = mid - 1
            } else {
                return mid + 1
            }
        }
        return start
    }

    /**
     * In place compact of given sorted list by removing duplicated elements.
     * @return end index after the last valid element.
     */
    fun <T> uniq(list: MutableList<T>, start: Int, end: Int): Int {
        var start = start
        var ret = end - start
        if (ret <= 1) {
            return ret
        }
        var keep = list[start++]
        ret = start
        while (start < end) {
            val a = list[start]
            if (keep == a) {
                ++start
                continue
            }
            keep = a // new unique element
            if (start != ret) {
                list[ret] = a
            }
            ++ret
            ++start
        }
        for (i in end - 1 downTo ret) {
            list.removeAt(i)
        }
        return ret
    }

    /**
     * Binary search for insertion point of 'x' in the given range of 'a'.
     * Returned index is the insertion point for 'x' regardless a match is found or not.
     *
     * @return Index in 'a' such that a[index-1] <=x <a></a>[index]
     */
    fun <T : Comparable<T>?> insertionIndex(a: Array<T>, start: Int, end: Int, key: T): Int {
        var start = start
        var end = end
        --end
        while (start <= end) {
            val mid = start + end ushr 1
            val c = a[mid]!!.compareTo(key)
            if (c < 0) {
                start = mid + 1
            } else if (c > 0) {
                end = mid - 1
            } else {
                return mid + 1
            }
        }
        return start
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * Insert objects before the given at object, if the at object is found.
     * @return index of the first inserted object, -1 if at object not found.
     */
    fun <T> insertBefore(list: MutableList<T?>, at: T, vararg objects: T): Int {
        val index = list.indexOf(at)
        if (index < 0) {
            return -1
        }
        list.addAll(index, ReadOnlyCollection.wrap(objects))
        return index
    }

    /**
     * Insert objects after the given at object, if the at object is found.
     * @return index of the first inserted object, -1 if at object not found.
     */
    fun <T> insertAfter(list: MutableList<T?>, at: T, vararg objects: T): Int {
        var index = list.indexOf(at)
        if (index < 0) {
            return -1
        }
        list.addAll(++index, ReadOnlyCollection.wrap(objects))
        return index
    }

    ////////////////////////////////////////////////////////////////////////
    fun hash(h: Int): Int {
        /* Better hash from java.util.HashMap, CLASSPATH license. */
        return h xor (h ushr 20) xor (h ushr 12) xor (h ushr 7) xor (h ushr 4)
    }

    ////////////////////////////////////////////////////////////////////////
    abstract class CollectionGenerator<T> protected constructor(protected var ret: MutableCollection<T>) : Runnable, Iterable<T> {
        private val initialized = false
        override fun iterator(): MutableIterator<T> {
            if (!initialized) {
                run()
            }
            return ret.iterator()
        }

    } ////////////////////////////////////////////////////////////////////////
}
