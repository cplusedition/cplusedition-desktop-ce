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

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.Lock
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun Int.isOdd(): Boolean {
    return (this and 0x01) != 0
}

val <T> Iterator<T>.bot: IteratorExt<T>
    get() = IteratorExt(this)

val <T> Iterable<T>.bot: IterableExt<T>
    get() = IterableExt(this)

val <T> Sequence<T>.bot: SequenceExt<T>
    get() = SequenceExt(this)

val <T> Array<T>.bot: ArrayExt<T>
    get() = ArrayExt(this)

val <C, K, V> C.bot: MapExt<C, K, V> where C : Map<K, V>
    get() = MapExt(this)

val <C, T> C.bot: MutableCollectionExt<C, T> where C : MutableCollection<T>
    get() = MutableCollectionExt(this)

val <C, T> C.bot: MutableListExt<C, T> where C : MutableList<T>
    get() = MutableListExt(this)

val <C, K, V> C.bot: MutableMapExt<C, K, V> where C : MutableMap<K, V>
    get() = MutableMapExt(this)

val <C, K, KK, VV> C.bot: MutableMapOfMapExt<C, K, KK, VV> where C : MutableMap<K, MutableMap<KK, VV>>
    get() = MutableMapOfMapExt(this)

val <C, K, VV> C.bot: MutableMapOfSetExt<C, K, VV> where C : MutableMap<K, MutableSet<VV>>
    get() = MutableMapOfSetExt(this)

val <C, K, VV> C.bot: MutableMapOfListExt<C, K, VV> where C : MutableMap<K, MutableList<VV>>
    get() = MutableMapOfListExt(this)

val NodeList.bot: NodeListExt
    get() = NodeListExt(this)

val <F, S>Pair<F, S>.bot: PairExt<F, S>
    get() = PairExt(this)

val <V> V?.bot: NullableExt<V>
    get() = NullableExt<V>(this)

val String.bot: StringExt
    get() = StringExt(this)

object With : WithUtil()
open class WithUtil {

    fun await(count: Int = 1, code: Fun10<CountDownLatch>) {
        val done = CountDownLatch(count)
        code(done)
        done.await()
    }

    fun exceptionOrFalse(code: Fun00): Boolean {
        return try {
            code()
            false
        } catch (e: Exception) {
            true
        }
    }

    /**
     * @return The exception or null.
     */
    fun exceptionOrNull(code: Fun00): Exception? {
        return try {
            code()
            null
        } catch (e: Exception) {
            e
        }
    }

    /**
     * If code throws an exception, ignores it.
     * If code does not throw an exception, throw an IllegalStateException.
     */
    fun exceptionOrFail(code: Fun00): Exception {
        try {
            code()
        } catch (e: Exception) {
            return e
        }
        throw IllegalStateException()
    }

    fun <R> exceptionResult(code: Fun01<R>): IBotResult<R, Exception> {
        return try {
            BotResult.ok(code())
        } catch (e: Exception) {
            BotResult.fail(e)
        }
    }

    /**
     * @return the throwable thrown by the code or null.
     */
    fun throwableOrNull(code: Fun00): Throwable? {
        return try {
            code()
            null
        } catch (e: Throwable) {
            e
        }
    }

    /**
     * If code throws a Throwable, ignores it.
     * If code does not throw a Throwable, throw an IllegalStateException.
     */
    fun throwableOrFail(code: Fun00): Throwable {
        try {
            code()
        } catch (e: Throwable) {
            return e
        }
        throw IllegalStateException()
    }

    /**
     * If code throws a Throwable, ignores it.
     * If code does not throw a Throwable, throw an IllegalStateException.
     */
    fun <R> throwableResult(code: Fun01<R>): IBotResult<R, Throwable> {
        return try {
            BotResult.ok(code())
        } catch (e: Throwable) {
            BotResult.fail(e)
        }
    }

    fun <T : Closeable, R> closeable(target: T, code: Fun11<T, R>): R {
        return target.use(code)
    }

    @Throws(IOException::class)
    fun <R> inputStream(file: File, code: Fun11<InputStream, R>): R {
        return FileInputStream(file).use(code)
    }

    @Throws(IOException::class)
    fun <R> outputStream(file: File, code: Fun11<OutputStream, R>): R {
        return FileOutputStream(file).use(code)
    }

    @Throws(IOException::class)
    fun <R> printWriter(file: File, code: Fun11<PrintWriter, R>): R {
        return PrintWriter(file).use(code)
    }

    @Throws(IOException::class)
    fun zipInputStream(zipfile: File, code: Fun20<ZipInputStream, ZipEntry>) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipfile))).use { zipinput ->
            while (true) {
                val entry = zipinput.nextEntry ?: break
                code(zipinput, entry)
            }
        }
    }

    @Throws(IOException::class)
    fun zipOutputStream(zipfile: File, code: Fun10<ZipOutputStream>) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipfile))).use(code)
    }

    @Throws(IOException::class)
    fun bufferedWriter(file: File, charset: Charset = Charsets.UTF_8, code: Fun10<BufferedWriter>) {
        FileOutputStream(file).bufferedWriter(charset).use(code)
    }

    @Throws(IOException::class)
    fun bufferedReader(file: File, charset: Charset = Charsets.UTF_8, code: Fun10<BufferedReader>) {
        FileInputStream(file).bufferedReader(charset).use(code)
    }

    @Throws(IOException::class)
    fun bytes(file: File, bufsize: Int = K.BUFSIZE, code: Fun20<ByteArray, Int>) {
        FileInputStream(file).use { input ->
            val buf = ByteArray(bufsize)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                if (n > 0) code(buf, n)
            }
        }
    }

    fun bytes(input: InputStream, bufsize: Int = K.BUFSIZE, code: Fun20<ByteArray, Int>) {
        val buf = ByteArray(bufsize)
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            if (n > 0) code(buf, n)
        }
    }

    @Throws(IOException::class)
    fun lines(file: File, charset: Charset = Charsets.UTF_8, code: Fun10<String>) {
        BufferedReader(InputStreamReader(FileInputStream(file), charset)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                code(line)
            }
        }
    }

    /**
     * Rewrite a file with a text content transform.
     *
     * @param code(String): String?.
     */
    @Throws(IOException::class)
    fun rewriteText(file: File, charset: Charset = Charsets.UTF_8, code: Fun11<String, String>): Boolean {
        val input = file.readText(charset)
        val output = code(input)
        val modified = (output != input)
        if (modified) {
            file.writeText(output, charset)
        }
        return modified
    }

    /**
     * Rewrite a file with a line by line transform.
     *
     * @param code(String): String?.
     */
    @Throws(IOException::class)
    fun rewriteLines(file: File, charset: Charset = Charsets.UTF_8, code: Fun11<String, String?>) {
        backup(file) { dst, src ->
            bufferedReader(src, charset) { reader ->
                bufferedWriter(dst, charset) { writer ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        val output = code(line)
                        if (output != null) writer.appendLine(output)
                    }
                }
            }
        }
    }

    /**
     * Rewrite a file with a line by line transform.
     *
     * @param code(String): String?.
     */
    @Throws(IOException::class)
    fun rewriteLineList(file: File, charset: Charset = Charsets.UTF_8, code: Fun11<List<String>, List<String>?>) {
        val lines = file.readLines(charset)
        val output = code(lines)
        if (output != null && !StructUt.equals(output, lines)) {
            file.bufferedWriter(charset).use {
                for (line in output) it.appendLine(line)
            }
        }
    }

    /**
     *  If code() does not return null then fail.
     */
    @Throws(AssertionError::class)
    fun nullOrFail(code: Fun01<String?>) {
        val error = code() ?: return
        throw IllegalStateException(error)
    }

    /**
     * @param code(tmpdir): T
     * @throws Exception If operation fail.
     */
    @Throws(Exception::class)
    fun <T> tmpdir(dir: File, code: Fun11<File, T>): T {
        dir.mkdirsOrFail()
        val tmpdir = Files.createTempDirectory(dir.toPath(), "tmp").toFile()
        try {
            return code(tmpdir)
        } finally {
            FileUt.deleteRecursively(tmpdir)
        }
    }

    /**
     * @param code(tmpfile): T
     * @throws Exception If operation fail.
     */
    @Throws(Exception::class)
    fun <T> tmpfile(dir: File?, suffix: String = ".tmp", code: Fun11<File, T>): T {
        val tmpfile = File.createTempFile("tmp", suffix, dir)
        try {
            return code(tmpfile)
        } finally {
            FileUt.delete(tmpfile)
        }
    }

    @Throws(Exception::class)
    fun <T> atmpdir(dir: File, code: Fun21<File, Fun00, T>): T {
        dir.mkdirsOrFail()
        val tmpdir = Files.createTempDirectory(dir.toPath(), "tmp").toFile()
        return code(tmpdir) {
            FileUt.deleteRecursively(tmpdir)
        }
    }

    @Throws(Exception::class)
    fun <T> atmpfile(dir: File?, suffix: String = ".tmp", code: Fun21<File, Fun00, T>): T {
        val tmpfile = File.createTempFile("tmp", suffix, dir)
        return code(tmpfile) {
            FileUt.delete(tmpfile)
        }
    }

    /**
     * Create a tmpfile, call code(tmpfile, file) with tmpfile as dst and input file as src.
     * If code() return true and without throwing exception, copy tmpfile to input file.
     * Delete the tmpfile in either case,
     *
     * @param code(dstfile, srcfile) True to apply changes to outfile, false or exception to leave outfile intact.
     * @throws Exception If operation fail.
     */
    @Throws(Exception::class)
    fun backup(outfile: File, code: Fun20<File, File>) {
        tmpfile(outfile.parentFile) {
            code(it, outfile)
            _renameOrCopy(outfile, it)
        }
    }

    private fun _renameOrCopy(dst: File, src: File): Boolean {
        if (dst.exists() && !dst.delete()) return false
        if (!src.renameTo(dst)) {
            FileUt.copyas(dst, src)
            src.delete()
        }
        return true
    }

    /**
     * Create a tmpfile, call code(tmpfile, file) with tmpfile as dst and input file as src.
     * If code() return true and without throwing exception, copy tmpfile to input file.
     * Delete the tmpfile in either case,
     *
     * @param code(dstfile, srcfile) True to apply changes to outfile, false or exception to leave outfile intact.
     * @throws Exception If operation fail.
     */
    @Throws(Exception::class)
    fun backup21(outfile: File, code: Fun21<File, File, Boolean>) {
        tmpfile(outfile.parentFile) {
            if (code(it, outfile)) {
                _renameOrCopy(outfile, it)
            }
        }
    }

    /**
     * Delete the backupfile if it exists and copy the outfile to the backupfile.
     * Perform the code() operation and write to the outfile.
     * If operation fail, restore outfile from the backup and the backup get deleted.
     * If operation suceed, the result stays at the outfile and the backupfile contains content
     * of the original outfile.
     *
     * @param code(dstfile, srcfile)
     * @throws Exception If operation fail.
     */
    @Throws(Exception::class)
    fun backup(outfile: File, backupfile: File, code: Fun20<File, File>) {
        if (!_renameOrCopy(backupfile, outfile)) throw IOException()
        try {
            code(outfile, backupfile)
        } catch (e: Throwable) {
            _renameOrCopy(outfile, backupfile)
            throw IOException(e)
        }
    }

    /**
     * Execute the code that generate a list of values, shuffle it and add the shuffled list to ret.
     */
    fun <V> shuffle(ret: MutableList<V>, code: Fun10<MutableList<V>>) {
        val buf = ArrayList<V>()
        code(buf)
        buf.shuffle()
        ret.addAll(buf)
    }

    fun <V> lock(lock: Lock, code: Fun01<V>): V {
        lock.lock()
        try {
            return code()
        } finally {
            lock.unlock()
        }
    }

    @Throws(TimeoutException::class, InterruptedException::class)
    fun sync(
        count: Int = 1,
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.MILLISECONDS,
        code: Fun10<Fun00>
    ) {
        val done = CountDownLatch(count)
        code { done.countDown() }
        if (!done.await(timeout, timeunit)) throw TimeoutException()
    }

    @Throws(TimeoutException::class, InterruptedException::class)
    fun <V> sync(
        count: Int = 1,
        timeout: Long = DateUt.DAY,
        timeunit: TimeUnit = TimeUnit.MILLISECONDS,
        code: Fun10<Fun10<V>>
    ): V {
        val done = CountDownLatch(count)
        var ret: V? = null
        code { result ->
            ret = result
            done.countDown()
        }
        if (!done.await(timeout, timeunit)) throw TimeoutException()
        return ret!!
    }
}

object Without : WithoutUtil()

open class WithoutUtil {

    /**
     * @return False if code() throws an exception, otherwise return value of code().
     * Example: let ok = Without.exceptionOrFalse { code }
     */
    fun exceptionOrFalse(code: Fun01<Boolean>): Boolean {
        return try {
            code()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * @return Result of the given block or null if there is an exception.
     * Example: let value = Without.exception(Int.parse(s)) ?: -1
     */
    fun <T> exceptionOrNull(code: () -> T): T? {
        return try {
            code()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @return Result of the given block if it does not throw an Exception,
     * otherwise throw an IllegalStateException.
     * Example: val value = Without.exception(Int.parse(s))
     */
    fun <T> exceptionOrFail(code: () -> T): T {
        try {
            return code()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    /**
     * @return Result of the given block or null if there is an exception.
     * Example: let value = Without.throwable(Int.parse(s)) ?: -1
     */
    fun <T> throwableOrNull(code: () -> T): T? {
        return try {
            code()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * @return Result of the given block if it does not throw a Throwable,
     * otherwise throw an IllegalStateException.
     * Example: val value = Without.throwableOrFail(Int.parse(s))
     */
    fun <T> throwableOrFail(code: () -> T): T {
        try {
            return code()
        } catch (e: Throwable) {
            throw IllegalStateException(e)
        }
    }

    fun comments(file: File, prefix: String = "#", code: (String) -> Unit) {
        With.lines(file) {
            val s = it.trim()
            if (s.isNotEmpty() && !s.startsWith(prefix)) {
                code(it)
            }
        }
    }
}

open class IteratorExt<T> constructor(
    private val c: Iterator<T>
) {
    fun join(sep: CharSequence): String {
        if (!c.hasNext()) return ""
        val first = c.next().toString()
        if (!c.hasNext()) return first
        val b = StringBuilder(first)
        if (sep.isEmpty()) {
            for (t in c) b.append(t.toString())
        } else {
            for (t in c) {
                b.append(sep)
                b.append(t.toString())
            }
        }
        return b.toString()
    }

    fun joinln(): String {
        return join(LS)
    }
}

open class IterableExt<T> constructor(
    private val c: Iterable<T>,
) {
    fun joinln(): String {
        return c.joinToString(LS)
    }

    fun joinlns(): String {
        val ret = c.joinToString(LS)
        return if (ret.endsWith(LS)) ret else ret + LS
    }

    /**
     * @return A filepath joined by File.separator.
     */
    fun joinPath(): String {
        return c.joinToString(FS)
    }
}

open class SequenceExt<T> constructor(
    private val c: Sequence<T>,
) {
    fun joinln(): String {
        return c.joinToString(LS)
    }

    fun joinlns(): String {
        val ret = c.joinToString(LS)
        return if (ret.endsWith(LS)) ret else ret + LS
    }

    /**
     * @return A filepath joined by File.separator.
     */
    fun joinPath(): String {
        return c.joinToString(FS)
    }
}

open class ArrayExt<T> constructor(
    private val c: Array<T>,
) {
    fun joinln(): String {
        return c.joinToString(LS)
    }

    fun joinPath(): String {
        return c.joinToString(FS)
    }
}

open class MutableCollectionExt<C, T> constructor(
    private val c: C,
) : IterableExt<T>(c) where C : MutableCollection<T> {
    fun adding(vararg a: T): C {
        c.addAll(a)
        return c
    }
}

open class MutableListExt<C, T> constructor(
    private val c: C,
) : IterableExt<T>(c) where C : MutableList<T> {
    fun addAll(vararg elements: T): Boolean {
        return c.addAll(elements)
    }

    fun adding(vararg elements: T): C {
        c.addAll(elements)
        return c
    }

    fun addAt(index: Int, value: T): C {
        c.add(index, value)
        return c
    }
}

open class MapExt<C, K, V> constructor(
    private val c: C,
) where C : Map<K, V> {
    fun map(mapper: (K, V) -> V?): MutableMap<K, V> {
        val ret = mutableMapOf<K, V>()
        for ((k, v) in c.entries) {
            val value = mapper(k, v) ?: continue
            ret[k] = value
        }
        return ret
    }
}

open class MutableMapExt<C, K, V> constructor(
    private val c: C,
) : MapExt<C, K, V>(c) where C : MutableMap<K, V> {
    fun getOrCreate(k: K, ctor: Fun11<K, V>): V {
        return c.get(k) ?: ctor(k).also { c.put(k, it) }
    }

    fun adding(map: Map<K, V>): C {
        for ((k, v) in map.entries) {
            if (v != null) {
                c[k] = v
            }
        }
        return c
    }

    fun adding(key: K, value: V): C {
        if (value != null) {
            c[key] = value
        }
        return c
    }
}

open class MutableMapOfMapExt<C, K, KK, VV> constructor(
    private val c: C,
) : MutableMapExt<C, K, MutableMap<KK, VV>>(c) where C : MutableMap<K, MutableMap<KK, VV>> {
    fun getOrCreate(k: K): MutableMap<KK, VV> {
        return c.get(k) ?: TreeMap<KK, VV>().also { c.put(k, it) }
    }

    fun merging(other: C): C {
        for ((k, v) in other) {
            val map = getOrCreate(k)
            map.putAll(v)
        }
        return c
    }

    fun adding2(k: K, kk: KK, value: VV): C {
        val map = getOrCreate(k)
        map[kk] = value
        return c
    }
}

open class MutableMapOfSetExt<C, K, VV> constructor(
    private val c: C,
) : MutableMapExt<C, K, MutableSet<VV>>(c) where C : MutableMap<K, MutableSet<VV>> {
    fun getOrCreate(k: K): MutableSet<VV> {
        return c.get(k) ?: TreeSet<VV>().also { c.put(k, it) }
    }

    fun merging(other: C): C {
        for ((k, v) in other) {
            val set = getOrCreate(k)
            set.addAll(v)
        }
        return c
    }

    fun adding2(key: K, value: VV): C {
        val set = getOrCreate(key)
        set.add(value)
        return c
    }
}

open class MutableMapOfListExt<C, K, VV> constructor(
    private val c: C,
) : MutableMapExt<C, K, MutableList<VV>>(c) where C : MutableMap<K, MutableList<VV>> {
    fun getOrCreate(k: K): MutableList<VV> {
        return c.get(k) ?: ArrayList<VV>().also { c.put(k, it) }
    }

    fun merging(other: C): C {
        for ((k, v) in other) {
            val list = getOrCreate(k)
            list.addAll(v)
        }
        return c
    }

    fun adding2(key: K, value: VV): C {
        val list = getOrCreate(key)
        list.add(value)
        return c
    }
}

open class PairExt<F, S> constructor(
    private val c: Pair<F, S>
) {
    fun join(sep: CharSequence): String {
        return "${c.first}$sep${c.second}"
    }
}

open class NullableExt<V> constructor(
    private val c: V?
) {
    fun <R> notnull(code: Fun11<V, R>): R? {
        return if (c == null) null else code(c)
    }

    fun emptyOr(def: String): String {
        return if (c == null) "" else def
    }
}

open class StringExt constructor(
    private val c: String?
) {
    fun notNullOrEmpty(code: Fun11<String, String>): String? {
        return if (c.isNullOrEmpty()) c else code(c)
    }

    fun emptyOr(def: String): String {
        return if (c.isNullOrEmpty()) "" else def
    }
}

open class NodeListExt constructor(
    private val c: NodeList
) {

    fun elements(): Iterable<Element> {
        return NodeListElementIterable(c)
    }

    fun nodes(): Iterable<Node> {
        return NodeListIterable(c)
    }
}

class ElementListIterable(
    private val list: NodeList
) : Iterable<Element>, Iterator<Element> {
    var length = list.length
    private var index = 0
    override fun hasNext(): Boolean {
        return index < length
    }

    override fun next(): Element {
        return list.item(index++) as Element
    }

    override fun iterator(): Iterator<Element> {
        return this
    }
}

class NodeListElementIterable(
    private val list: NodeList
) : Iterable<Element>, Iterator<Element> {
    var length = list.length
    private var index = 0
    private var current: Element? = null

    init {
        next1()
    }

    override fun hasNext(): Boolean {
        return current != null
    }

    override fun next(): Element {
        val ret = current!!
        next1()
        return ret
    }

    override fun iterator(): Iterator<Element> {
        return this
    }

    private fun next1() {
        for (i in index until length) {
            val e = list.item(i)
            if (e is Element) {
                index = i + 1
                current = e
                return
            }
        }
        index = length
        current = null
    }
}

class NodeListIterable(
    private val list: NodeList
) : Iterable<Node>, Iterator<Node> {
    var length = list.length
    private var index = 0
    override fun hasNext(): Boolean {
        return index < length
    }

    override fun next(): Node {
        return list.item(index++)
    }

    override fun iterator(): Iterator<Node> {
        return this
    }
}

class EnumerationIterable<T>(
    private val list: Enumeration<T>
) : Iterable<T>, Iterator<T> {
    override fun hasNext(): Boolean {
        return list.hasMoreElements()
    }

    override fun next(): T {
        return list.nextElement()
    }

    override fun iterator(): Iterator<T> {
        return this
    }
}

private object K {
    const val BUFSIZE = 8192
}
