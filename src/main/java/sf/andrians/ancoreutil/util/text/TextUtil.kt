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
package sf.andrians.ancoreutil.util.text

import com.cplusedition.bot.core.Fun21
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.ancoreutil.util.struct.ByteSequence
import sf.andrians.ancoreutil.util.struct.IByteSequence
import sf.andrians.ancoreutil.util.struct.IIntList
import sf.andrians.ancoreutil.util.struct.IterableWrapper
import sf.andrians.ancoreutil.util.struct.ReversedComparator
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Static text utility methods.
 *
 *
 * TODO: . Remove synchronized for functions that are classified as synchronized just because of using Perl5Util
 * re.
 */
object TextUtil {
    val pathSeparatorChar = File.separatorChar
    private val SEP_ARRAY = charArrayOf(pathSeparatorChar)
    val pathSeparator = pathSeparatorChar.toString()
    private val LINE_SEP = lazy { System.getProperty("line.separator") ?: "\n" }
    private val filepathEscapeChars: MutableMap<String, String> = Hashtable()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()
    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()

    //////////////////// String functions

    //#BEGIN
    //#FIXME Unsafe
    //#END
    fun escString(s: CharSequence): String {
        val ret = StringBuilder()
        var cc: String
        var c: Char
        var n: Int
        var i = 0
        val max = s.length
        while (i < max) {
            c = s[i++]
            when (c) {
                '\n' -> cc = "\\n"
                '\r' -> cc = "\\r"
                '\t' -> cc = "\\t"
                '\'' -> cc = "\\\'"
                '\"' -> cc = "\\\""
                '\\' -> cc = "\\\\"
                '\b' -> cc = "\\b"
                '\u000c' -> cc = "\\f"
                else -> {
                    n = c.toInt()
                    cc = if (n < 0x20 || n == 0x7f) {
                        "\\0" + Integer.toOctalString(n)
                    } else {
                        "" + c
                    }
                }
            }
            ret.append(cc)
        }
        return ret.toString()
    }

    /**
     * Escape the occurences of given chars in the given string.
     */
    fun escString(str: CharSequence, chars: String): String {
        val ret = StringBuilder()
        var c: Char
        val len = str.length
        for (i in 0 until len) {
            c = str[i]
            if (chars.indexOf(c) >= 0) {
                ret.append('\\')
            }
            ret.append(c)
        }
        return ret.toString()
    }

    fun unquote(s: CharSequence, quotes: String?): CharSequence {
        val len = s.length
        if (len <= 1 || quotes == null) {
            return s
        }
        val c = s[0]
        return if (quotes.indexOf(c) >= 0 && s[len - 1] == c) {
            s.subSequence(1, len - 1)
        } else s
    }

    /**
     * Make first character of all words uppercase.
     */
    fun capitalizeFirstAll(s: CharSequence): String {
        val buf = StringBuilder()
        var isfirst = true
        for (i in 0 until s.length) {
            val c = s[i]
            if (isfirst) {
                buf.append(Character.toUpperCase(c))
            } else {
                buf.append(c)
            }
            isfirst = Character.isWhitespace(c)
        }
        return buf.toString()
    }

    /**
     * Make first word of the given text capital.
     */
    fun capitalizeFirst1(s: CharSequence): CharSequence {
        val len = s.length
        val buf = StringBuilder(len)
        for (i in 0 until len) {
            val c = s[i]
            if (buf.length == 0) {
                if (Character.isWhitespace(c)) {
                    continue
                }
                buf.append(Character.toUpperCase(c))
            } else {
                buf.append(c)
            }
        }
        return buf
    }

    @kotlin.jvm.JvmStatic
    fun quote(s: CharSequence?): String? {
        if (s == null) {
            return null
        }
        val buf = StringBuilder()
        buf.append('"')
        var c: Char
        var i = 0
        val max = s.length
        while (i < max) {
            c = s[i]
            if (c == '"') {
                buf.append('\\')
            }
            buf.append(c)
            ++i
        }
        buf.append('"')
        return buf.toString()
    }

    fun q(s: CharSequence?): String? {
        if (s == null) {
            return null
        }
        val ret = StringBuilder()
        ret.append('\'')
        var c: Char
        var i = 0
        val len = s.length
        while (i < len) {
            c = s[i]
            if (c == '\'') {
                ret.append("'\\''")
            } else {
                ret.append(c)
            }
            ++i
        }
        ret.append('\'')
        return ret.toString()
    }

    @Throws(Exception::class)
    fun quoteString(s: String): String {
        var i = 0
        val len = s.length
        while (i < len) {
            val c = s[i]
            if (c.toInt() < 0x20 || c == '"' || c == '\\') {
                return quoteString1(s)
            }
            ++i
        }
        return '"'.toString() + s + '"'
    }

    @Throws(Exception::class)
    fun quoteString1(s: String): String {
        val b = StringBuilder("\"")
        var i = 0
        val len = s.length
        while (i < len) {
            val c = s[i]
            if (c.toInt() == 0x09) {
                b.append("\\t")
            } else if (c.toInt() == 0x0a) {
                b.append("\\n")
            } else if (c.toInt() == 0xd) {
                b.append("\\r")
            } else if (c == '\\') {
                b.append("\\\\")
            } else if (c == '"') {
                b.append("\\\"")
            } else if (c.toInt() < 0x20) {
                throw Exception("Illegal character: " + c.toInt())
            } else {
                b.append(c)
            }
            ++i
        }
        b.append('"')
        return b.toString()
    }

    @kotlin.jvm.JvmStatic
    val lineSeparator: String
        get() = LINE_SEP.value

    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        if (a == null) {
            return b == null
        }
        if (b == null) {
            return false
        }
        val alen = a.length
        if (alen != b.length) {
            return false
        }
        for (i in 0 until alen) {
            if (a[i] != b[i]) {
                return false
            }
        }
        return true
    }

    @kotlin.jvm.JvmStatic
    fun equals(a: String?, b: String?): Boolean {
        return if (a == null) {
            b == null
        } else a == b
    }

    fun arrayEquals(a: Array<Any>?, b: Array<Any>?): Boolean {
        if (a === b) {
            return true
        }
        if (a == null || b == null) {
            return false
        }
        if (a.size != b.size) {
            return false
        }
        for (i in a.indices) {
            if (a[i] != b[i]) {
                return false
            }
        }
        return true
    }

    fun equalsIgnoreCase(a: CharSequence?, b: CharSequence?): Boolean {
        return nullFirst(a, b) { aa, bb ->
            if (equalsIgnoreCase1(aa, bb)) 0 else 1
        } == 0
    }

    fun equalsIgnoreCase1(a: CharSequence, b: CharSequence): Boolean {
        val alen = a.length
        val blen = b.length
        if (alen != blen) {
            return false
        }
        for (i in 0 until alen) {
            val ca = a[i]
            val cb = b[i]
            if (ca != cb && ca != Character.toLowerCase(cb) && ca != Character.toUpperCase(cb)) {
                return false
            }
        }
        return true
    }

    fun compare(a: CharSequence?, b: CharSequence?): Int {
        return nullFirst(a, b, TextUtil::compare1)
    }

    fun compare1(a: CharSequence, b: CharSequence): Int {
        return compare1(a, b)
    }

    fun compare(a: CharSequence?, astart: Int, aend: Int, b: CharSequence?, bstart: Int, bend: Int): Int {
        return nullFirst(a, b) { aa, bb ->
            compare1(aa, 0, aa.length, bb, 0, bb.length)
        }
    }

    fun compare1(a: CharSequence, astart: Int, aend: Int, b: CharSequence, bstart: Int, bend: Int): Int {
        var aindex = astart
        var bindex = bstart
        val alen = aend - astart
        val blen = bend - bstart
        val end = astart + min(alen, blen)
        while (aindex < end) {
            val d = a[aindex++] - b[bindex++]
            if (d != 0) return if (d > 0) 1 else -1
        }
        return alen.compareTo(blen)
    }

    @Deprecated("", ReplaceWith("compareValues(a, b)"))
    fun <V : Comparable<*>> compareComparable(a: V?, b: V?): Int {
        return compareValues(a, b)
    }

    fun <V : Comparable<*>> compareComparable(a: Iterable<V>?, b: Iterable<V>?): Int {
        return nullFirst(a, b) { a1, b1 ->
            val ai = a1.iterator()
            val bi = b1.iterator()
            while (ai.hasNext() && bi.hasNext()) {
                val aa = ai.next()
                val bb = bi.next()
                val ret = compareValues(aa, bb)
                if (ret != 0) {
                    return@nullFirst ret
                }
            }
            return@nullFirst if (ai.hasNext() == bi.hasNext()) 0 else if (ai.hasNext()) 1 else -1
        }
    }

    fun <V : Comparable<*>> compareArray(a: Array<V>?, b: Array<V>?): Int {
        return compareComparable(
                if (a == null) null else IterableWrapper.wrap(a, 0, a.size),
                if (b == null) null else IterableWrapper.wrap(b, 0, b.size))
    }

    fun hashcode(a: Any?): Int {
        return a?.hashCode() ?: 0
    }

    fun classnameOf(a: Any?): String {
        return if (a == null) {
            "null"
        } else a.javaClass.name
    }

    fun concat(a: CharArray, vararg b: Char): CharArray {
        val ret = CharArray(a.size + b.size)
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    fun concat(a: String?, vararg b: String): Array<String?> {
        val ret = arrayOfNulls<String>(1 + b.size)
        ret[0] = a
        System.arraycopy(b, 0, ret, 1, b.size)
        return ret
    }

    fun concat(head: Array<String?>, tail: String?): Array<String?> {
        val ret = arrayOfNulls<String>(head.size + 1)
        System.arraycopy(head, 0, ret, 0, head.size)
        ret[head.size] = tail
        return ret
    }

    fun concat(head: Array<String?>, tail: Collection<String?>): Array<String?> {
        var len = head.size
        val ret = arrayOfNulls<String>(len + tail.size)
        System.arraycopy(head, 0, ret, 0, len)
        for (s in tail) {
            ret[len++] = s
        }
        return ret
    }

    fun concat(a: Array<String?>, vararg b: String): Array<String?> {
        val ret = arrayOfNulls<String>(a.size + b.size)
        System.arraycopy(a, 0, ret, 0, a.size)
        System.arraycopy(b, 0, ret, a.size, b.size)
        return ret
    }

    /**
     * A convenient shortcut for frequently used pattern, that replace:
     * if (first)
     * first = false;
     * else
     * b.append(sep);
     * with:
     * first = TextUtil.append(b, first, sep);
     */
    fun append(b: StringBuilder, first: Boolean, sep: String?): Boolean {
        if (!first) {
            b.append(sep)
        }
        return false
    }

    /**
     * Convenient method that append sep only if not the first item.
     * Typically usage: first=append(ret, first, ", ", text);
     */
    fun append(b: StringBuilder, first: Boolean, sep: String?, text: String?): Boolean {
        if (!first) {
            b.append(sep)
        }
        b.append(text)
        return false
    }

    /**
     * Similar to append() but append only if text is not null.
     */
    fun appendIfNotNull(b: StringBuilder, first: Boolean, sep: String?, text: String?): Boolean {
        var first = first
        if (text != null) {
            if (!first) {
                b.append(sep)
            } else {
                first = false
            }
            b.append(text)
        }
        return first
    }

    fun prefixes(prefix: String?, s: String?): String {
        return prefix(prefix, " ", StringTokenizer(s))
    }

    fun prefix(prefix: String?, vararg a: String): Array<String?> {
        val ret = arrayOfNulls<String>(a.size)
        for (i in 0 until a.size) {
            ret[i] = if (prefix == null) a[i] else prefix + a[i]
        }
        return ret
    }

    fun prefix(prefix: String?, sep: String?, tokener: StringTokenizer): String {
        val ret = StringBuilder()
        while (tokener.hasMoreTokens()) {
            if (ret.length > 0) {
                ret.append(sep)
            }
            ret.append(prefix)
            ret.append(tokener.nextToken())
        }
        return ret.toString()
    }

    fun prefix(prefix: String, strs: Collection<String>): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (s in strs) {
            ret.add(prefix + s)
        }
        return ret
    }

    fun prefix(ret: MutableCollection<String?>, prefix: String, strs: Collection<String>): Collection<String?> {
        for (s in strs) {
            ret.add(prefix + s)
        }
        return ret
    }

    /**
     * Prefix each token in the given str separate by ' ' or ',' by the given prefix.
     */
    fun prefixCSV(prefix: String?, str: String?): String {
        val tok = StringTokenizer(str, " ,", true)
        val ret = StringBuilder()
        var s: String
        while (tok.hasMoreTokens()) {
            s = tok.nextToken()
            if (isDelimiter(" ,", s)) {
                ret.append(s)
            } else {
                ret.append(prefix).append(s)
            }
        }
        return ret.toString()
    }

    fun join(ret: StringBuilder, sep: CharSequence?, a: Iterable<*>): StringBuilder {
        var first = true
        for (s in a) {
            if (first) {
                first = false
            } else {
                ret.append(sep)
            }
            ret.append(s.toString())
        }
        return ret
    }

    fun join(ret: StringBuilder, sep: CharSequence?, vararg a: String): StringBuilder {
        var first = true
        for (s in a) {
            if (first) {
                first = false
            } else {
                ret.append(sep)
            }
            ret.append(s)
        }
        return ret
    }

    fun join(ret: StringBuilder, sep: CharSequence?, a: Array<String?>, start: Int, end: Int): StringBuilder {
        for (i in start until end) {
            if (i != start) {
                ret.append(sep)
            }
            ret.append(a[i])
        }
        return ret
    }

    fun join(sep: CharSequence, a: Iterable<*>?): String {
        if (a == null) {
            return ""
        }
        val buf = StringBuilder()
        join(buf, sep, a)
        return buf.toString()
    }

    fun join(sep: CharSequence, vararg a: String): String {
        return join(sep, a as Array<String>, 0, a.size)
    }

    /**
     * Use joins() instead of join() to avoid unintentional conversion to Objects.
     */
    @SafeVarargs
    fun <T> joins(sep: CharSequence, vararg a: T): String {
        return joins(sep, a, 0, a.size)
    }

    fun <T> joins(sep: CharSequence, a: Array<T>, start: Int, end: Int): String {
        val b = StringBuilder()
        for (i in start until end) {
            if (i != 0) {
                b.append(sep)
            }
            b.append(a[i].toString())
        }
        return b.toString()
    }

    fun <T> joinln(a: Iterable<T>): String {
        return join(lineSeparator, a)
    }

    @SafeVarargs
    fun <T> joinln(vararg a: T): String {
        return join(lineSeparator, *a)
    }

    @kotlin.jvm.JvmStatic
    fun joinln(vararg a: String): String {
        return join(lineSeparator, *a)
    }

    /**
     * Like joinln() but add a line separator at end.
     */
    fun joinlns(vararg a: String): String {
        return join(lineSeparator, *a) + lineSeparator
    }

    /**
     * Like joinln() but add a line separator at end.
     */
    fun <T> joinlns(a: Iterable<T>): String {
        return join(lineSeparator, a) + lineSeparator
    }

    fun splice(start: Int, end: Int, vararg a: String): Array<String?> {
        val ret = arrayOfNulls<String>(end - start)
        System.arraycopy(a, start, ret, 0, end - start)
        return ret
    }

    fun splice(start: Int, vararg a: String): Array<String?> {
        return splice(start, a.size, *a)
    }

    fun split(str: String?): List<String> {
        return split(StringTokenizer(str))
    }

    fun split(str: String?, delims: String?): List<String> {
        return split(StringTokenizer(str, delims))
    }

    fun split(tok: StringTokenizer): MutableList<String> {
        val ret: MutableList<String> = ArrayList()
        while (tok.hasMoreTokens()) {
            ret.add(tok.nextToken())
        }
        return ret
    }

    fun split(vararg toks: StringTokenizer): MutableList<String> {
        val ret = ArrayList<String>()
        split(ret, *toks)
        return ret
    }

    fun <T : MutableCollection<String>> split(ret: T, str: String?): T {
        return split(ret, StringTokenizer(str))
    }

    fun <T : MutableCollection<String>> split(ret: T, str: String?, delims: String?): T {
        return split(ret, StringTokenizer(str, delims))
    }

    fun <T : MutableCollection<String>> split(ret: T, tok: StringTokenizer): T {
        while (tok.hasMoreTokens()) {
            ret.add(tok.nextToken())
        }
        return ret
    }

    fun <T : MutableCollection<String>> split(ret: T, vararg tokeners: StringTokenizer): T {
        for (tok in tokeners) {
            while (tok.hasMoreTokens()) {
                ret.add(tok.nextToken())
            }
        }
        return ret
    }

    fun <T : MutableList<String>> splitFilePath(ret: T, path: String): T {
        split(ret, StringTokenizer(path, File.separator))
        return ret
    }

    /**
     * Split given string into segments using the given separator.
     * This differ from the split() methods in that this do not use StringTokenizer
     * and return empty strings anywhere in s.
     */
    fun splitAll(s: String, sep: Char): MutableList<String> {
        val a: MutableList<String> = ArrayList()
        var start = 0
        var end: Int
        while (s.indexOf(sep, start).also { end = it } >= 0) {
            a.add(s.substring(start, end))
            start = end + 1
        }
        if (start <= s.length) {
            a.add(s.substring(start))
        }
        return a
    }

    fun splitFilePath(path: String?): MutableList<String> {
        return split(StringTokenizer(path, File.separator))
    }

    fun splitLines(str: CharSequence, esc: Boolean = false): MutableList<String> {
        return splitLines(ArrayList(), str, esc)
    }

    fun <T : MutableList<String>> splitLines(ret: T, str: CharSequence?, esc: Boolean = false): T {
        if (str == null) {
            return ret
        }
        var start = 0
        val len = str.length
        var c: Char
        var i = 0
        while (i < len) {
            c = str[i]
            if (esc && c == '\\') {
                ++i
                if (i < len && str[i] == '\r' && i + 1 < len && str[i + 1] == '\n') {
                    ++i
                }
                ++i
                continue
            }
            if (c == '\r' || c == '\n') {
                ret.add(str.subSequence(start, i).toString())
                if (c == '\r' && i + 1 < len && str[i + 1] == '\n') {
                    ++i
                }
                start = i + 1
                if (i + 1 >= len) {
                    //// Preserve terminating line break.
                    ret.add("")
                }
            }
            ++i
        }
        if (start < len) {
            ret.add(str.subSequence(start, len).toString())
        }
        return ret
    }

    fun removeFirstLine(s: StringBuilder, start: Int): String {
        val len = s.length
        var c: Char
        var i = start
        while (i < len) {
            c = s[i]
            if (c == '\\') {
                ++i
                if (i < len && s[i] == '\r' && i + 1 < len && s[i + 1] == '\n') {
                    ++i
                }
                ++i
                continue
            }
            if (c == '\n') {
                val ret = s.substring(0, i)
                s.delete(0, i + 1)
                return ret
            }
            if (c == '\r') {
                val ret = s.substring(0, i)
                if (i + 1 < len && s[i + 1] == '\n') {
                    ++i
                }
                s.delete(0, i + 1)
                return ret
            }
            ++i
        }
        val ret = s.substring(0, len)
        s.delete(0, len)
        return ret
    }

    fun removeQuote(s: String): String {
        var s = s
        val last = s.length - 1
        if (s[0] == '"' && s[last] == '"' || s[0] == '\'' && s[last] == '\'') {
            s = s.substring(1, last)
        }
        return s
    }

    fun removeHead(s: String?, head: String): String? {
        return if (s != null && s.startsWith(head)) {
            s.substring(head.length)
        } else s
    }

    fun removeTail(s: String?, tail: String): String? {
        return if (s != null && s.endsWith(tail)) {
            s.substring(0, s.length - tail.length)
        } else s
    }

    fun replaceAll(buf: StringBuffer, find: String, replace: String) {
        var index = buf.indexOf(find)
        while (index >= 0) {
            buf.replace(index, index + find.length, replace)
            index = buf.indexOf(find, index + replace.length)
        }
    }

    fun replaceAll(buf: StringBuilder, find: String, replace: String) {
        var index = buf.indexOf(find)
        while (index >= 0) {
            buf.replace(index, index + find.length, replace)
            index = buf.indexOf(find, index + replace.length)
        }
    }

    fun replaceAll(buf: StringBuilder, from: Char, to: Char): Int {
        return replaceAll(buf, 0, buf.length, from, to)
    }

    fun replaceAll(buf: StringBuilder, start: Int, end: Int, from: Char, to: Char): Int {
        var start = start
        var count = 0
        while (start < end) {
            if (buf[start] == from) {
                buf.setCharAt(start, to)
                ++count
            }
            ++start
        }
        return count
    }

    /**
     * URL encode each segment of given path.
     *
     * @throws RuntimeException is UTF-8 encoding is not supported.
     */

    fun encodeUrl(s: String?, encoding: String? = "UTF-8"): String {
        val segments = split(s, File.separator)
        val ret = StringBuilder()
        for (segment in segments) {
            try {
                if (ret.length > 0) {
                    ret.append(File.separator)
                }
                ret.append(URLEncoder.encode(segment, encoding))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }
        }
        return ret.toString()
    }

    /**
     * URL encode each segment of given path.
     *
     * @throws RuntimeException is UTF-8 encoding is not supported.
     */

    fun encodeUrl(segments: Collection<String?>, encoding: String? = "UTF-8"): List<String> {
        val ret: MutableList<String> = ArrayList(segments.size)
        for (segment in segments) {
            try {
                ret.add(URLEncoder.encode(segment, encoding))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }
        }
        return ret
    }

    /**
     * Unescape all %xx triplets.
     */
    fun unescapePercents(s: String): String {
        val ret = StringBuilder()
        var n = 0
        while (n < s.length) {
            val c = s[n]
            if (c != '%') {
                ret.append(c)
            } else {
                val i = s.substring(n + 1, n + 3)
                val o = hexToInt(i, -1)
                if (o == -1) {
                    ret.append("%")
                } else {
                    ret.append(o.toChar())
                    n += 2
                }
            }
            n++
        }
        return ret.toString()
    }

    /**
     * @return -1 on error.
     */
    fun hexToByte(c: Char): Int {
        if (c < '0') {
            return -1
        }
        if (c <= '9') {
            return c - '0'
        }
        if (c < 'A') {
            return -1
        }
        if (c <= 'F') {
            return c - 'A' + 10
        }
        if (c < 'a') {
            return -1
        }
        return if (c <= 'f') {
            c - 'a' + 10
        } else -1
    }

    /**
     * Convert hex string to int.
     */
    fun hexToInt(s: CharSequence, def: Int): Int {
        var ret = 0
        var n = 0
        val len = s.length
        val c: Char
        if (len >= 2 && s[0] == '0') {
            c = s[1]
            if (c == 'x' || c == 'X') {
                n = 2
            }
        }
        if (len - n > 8) {
            return def
        }
        var v: Int
        while (n < len) {
            if (hexToByte(s[n]).also { v = it } < 0) {
                return def
            }
            ret = ret shl 4 or v
            ++n
        }
        return ret
    }

    /**
     * @param @notnull s
     * @return null on error.
     */
    fun hexToByteArray(s: CharSequence): ByteArray? {
        var len = s.length
        if (len and 0x1 != 0) {
            return null
        }
        var olen = len / 2
        val ret = ByteArray(olen)
        while (len > 1) {
            val l = hexToByte(s[--len])
            if (l < 0) {
                return null
            }
            val h = hexToByte(s[--len])
            if (h < 0) {
                return null
            }
            ret[--olen] = (h shl 4 or l).toByte()
        }
        return ret
    }

    fun toLowerHex2(b: Int): String {
        return "" + HEX_LOWER[b shr 4 and 0xf] + HEX_LOWER[b and 0xf]
    }

    fun toUpperHex2(b: Int): String {
        return "" + HEX_UPPER[b shr 4 and 0xf] + HEX_UPPER[b and 0xf]
    }

    fun toLowerHex4(n: Int): String {
        return "" + HEX_LOWER[n shr 12 and 0xf] + HEX_LOWER[n shr 8 and 0xf] + HEX_LOWER[n shr 4 and 0xf] + HEX_LOWER[n and 0xf]
    }

    fun toUpperHex4(n: Int): String {
        return "" + HEX_UPPER[n shr 12 and 0xf] + HEX_UPPER[n shr 8 and 0xf] + HEX_UPPER[n shr 4 and 0xf] + HEX_UPPER[n and 0xf]
    }

    fun toLowerHex8(n: Int): String {
        return toLowerHex4((n shr 16) and 0xffff) + toLowerHex4(n and 0xffff)
    }

    fun toUpperHex8(n: Int): String {
        return toUpperHex4((n shr 16) and 0xffff) + toUpperHex4(n and 0xffff)
    }
    /**
     * Esacpe characters that are sensitive as a unix filename. NOTE: % is not escaped so that existing %xx
     * triplet are preserved.
     */
    fun escapeFilepath(s: String): String {
        val ret = StringBuilder()
        for (n in 0 until s.length) {
            val i = s.substring(n, n + 1)
            val o = filepathEscapeChars[i]
            ret.append(o ?: i)
        }
        return ret.toString()
    }

    /**
     * Unesacpe sensitive unix filename characters. NOTE that we do not unescape all ^xx triplet. Use
     * unescapePercents() for that purpose.
     */
    fun unescapeFilepath(s: String): String {
        val ret = StringBuilder()
        var n = 0
        while (n < s.length) {
            val c = s[n]
            if (c != '^') {
                ret.append(c)
            } else {
                val i = s.substring(n, n + 3)
                val o = filepathEscapeChars[i]
                if (o == null) {
                    ret.append("^")
                } else {
                    ret.append(o)
                    n += 2
                }
            }
            n++
        }
        return ret.toString()
    }
    /**
     * Fixup file path to full path if pwd != null and remove redundant chars.
     */
    /**
     * Fixup file path to full path and remove redundant chars.
     */

    fun normalizeFilePath(path: String, pwd: String? = System.getProperty("user.dir")): String {
        val len = path.length
        val b = StringBuilder()
        if (pwd != null && (len == 0 || path[0] != pathSeparatorChar)) {
            b.append(pwd)
            if (lastChar(pwd) != pathSeparatorChar.toInt()) {
                b.append(pathSeparatorChar)
            }
        }
        b.append(path)
        cleanupFilePath(b)
        return b.toString()
    }

    fun cleanupFilePath(path: String): CharSequence {
        val b = StringBuilder(path)
        cleanupFilePath(b)
        return b
    }

    /**
     * Remove duplicated /, /./ and /../
     */
    fun cleanupFilePath(b: StringBuilder) {
        var c: Char
        var last = -1
        var len = 0
        val max = b.length
        var i = 0
        val sepc = pathSeparatorChar
        val sepi = pathSeparatorChar.toInt()
        while (i < max) {
            c = b[i]
            if ((last == sepi || len == 0) && c == '.' && (i + 1 < max && b[i + 1] == sepc || i + 1 >= max)) {
                ++i
                ++i
                continue
            }
            if (last == sepi && c == sepc) {
                ++i
                continue
            }
            if (last == sepi && c == '.' && i + 1 < max && b[i + 1] == '.' && len >= 2 && (i + 2 >= max || b[i + 2] == sepc)) {
                val index = b.lastIndexOf(File.separator, len - 2)
                if ("../" != b.substring(index + 1, len)) {
                    len = index + 1
                    ++i
                    ++i
                    continue
                }
            }
            b.setCharAt(len++, c)
            last = c.toInt()
            ++i
        }
        b.setLength(len)
    }

    /**
     * @param base Base directory.
     * @return The normalized path, null if result path reference to location not under base directory.
     */
    fun toRelative(filepath: String, base: String): String? {
        var filepath = filepath
        var base = base
        if (lastChar(base) != pathSeparatorChar.toInt()) {
            base += pathSeparatorChar
        }
        filepath = toAbsolute(filepath, base) ?: return null
        if (filepath.isEmpty()) {
            return ""
        }
        if (filepath[0] == pathSeparatorChar) {
            if (!filepath.startsWith(base)) {
                return null
            }
            filepath = filepath.substring(base.length)
        }
        if (filepath.isEmpty()) {
            return ""
        }
        return if (filepath[0] == pathSeparatorChar
                || ".." == filepath
                || filepath.startsWith("..$pathSeparatorChar")
                || filepath.contains("$pathSeparatorChar..$pathSeparatorChar")) {
            null
        } else filepath
    }

    fun toRelative(fname: String, base: String, allowdotdot: Boolean): String? {
        var base = base
        if (!allowdotdot) {
            return toRelative(fname, base)
        }
        base = File(base).absolutePath
        val file = File(fname)
        val path = StringBuilder(file.absolutePath)
        if (path.indexOf(base) != 0) {
            return null
        }
        var start = base.length
        while (start < path.length) {
            if (path[start] != File.separatorChar) {
                break
            }
            ++start
        }
        return path.substring(start)
    }

    /**
     * Remove ../ and ./ in the given path convert path to relative.
     * Unfortunately, java.io.File.getCanonicalPath() class resolve symbolic links and
     * thus cannot be used here.
     *
     * @param base The base dir (must ends with /).
     */
    fun toAbsolute(filepath: String, base: String?): String? {
        var filepath = filepath
        if (filepath.isEmpty()) {
            return base
        }
        filepath = normalizeFilePath(filepath, base)
        return if (filepath.isEmpty() || filepath[0] != pathSeparatorChar) {
            null
        } else filepath
    }

    /**
     * Convert the given file path to an absolute path and make sure there is a trailing /.
     *
     * @param base The base dir (must ends with /).
     */
    fun toAbsoluteDir(filepath: String, base: String?): String? {
        var filepath = filepath
        filepath = toAbsolute(filepath, base) ?: return null
        return if (lastChar(filepath) != pathSeparatorChar.toInt()) {
            filepath + pathSeparatorChar
        } else filepath
    }

    fun stripLeadingSlash(path: String): String {
        return if (path.length == 0 || path[0] != '/') {
            path
        } else path.substring(1)
    }

    fun filepath(vararg segments: String?): String {
        return filepath(StringBuilder(), *segments).toString()
    }

    fun filepath(dir: File, vararg segments: String?): String {
        return filepath(StringBuilder(dir.path), *segments).toString()
    }

    /**
     * Construct a file path from given segments, removing duplicated file separators.
     */
    fun filepath(ret: StringBuilder, vararg segments: String?): StringBuilder {
        for (s in segments) {
            if (s == null) {
                continue
            }
            var len = ret.length
            var start = 0
            val end = s.length
            if (len > 0) {
                while (len > 0 && ret[len - 1] == File.separatorChar) {
                    --len
                }
                if (ret.length > len) {
                    ret.setLength(len)
                }
                while (start < end && s[start] == File.separatorChar) {
                    ++start
                }
                ret.append(File.separatorChar)
            }
            ret.append(s, start, end)
        }
        return ret
    }

    fun filepath(segments: Iterable<String?>): String {
        return filepath(StringBuilder(), segments).toString()
    }

    fun filepath(dir: File, segments: Iterable<String?>): String {
        return filepath(StringBuilder(dir.path), segments).toString()
    }

    fun filepath(ret: StringBuilder, segments: Iterable<String?>): StringBuilder {
        for (s in segments) {
            if (s == null) {
                continue
            }
            val len = ret.length
            if (len > 0 && ret[len - 1] != File.separatorChar) {
                ret.append(File.separatorChar)
            }
            ret.append(s)
        }
        return ret
    }

    fun filepaths(basedir: File, rpaths: Iterable<String>): Collection<String> {
        val ret: MutableCollection<String> = ArrayList()
        val base = basedir.path
        for (rpath in rpaths) {
            ret.add(base + File.separatorChar + rpath)
        }
        return ret
    }

    /**
     * @return The relative path in form ../.. ... such that new File(rpath, upPath(rpath), rpath) is the same file.
     * Example: / return "", a return .., a/b return ../..
     */
    fun upPath(rpath: String): String {
        val s = normalizeFilePath(rpath, null)
        if ("/" == s) {
            return ""
        }
        if (".." == s || "../" == s || "/.." == s || "/../" == s) {
            throw RuntimeException("ERROR: upPath() only works for subtree: $rpath")
        }
        val count = count(File.separatorChar, s)
        val b = StringBuilder()
        for (i in 0..count) {
            if (i > 0) {
                b.append(File.separatorChar)
            }
            b.append("..")
        }
        return b.toString()
    }

    /**
     * This is similar to splitSegments(), but return the best match, instead of null, if not enough segments is available.
     *
     * @param segments If segments is negative, number is counted backwards.
     */
    fun split(path: String, segments: Int, vararg seps: Char): Pair<String?, String?> {
        var ret = splitSegments(path, segments, *seps)
        if (ret == null) {
            ret = Pair(if (segments > 0) path else null, if (segments <= 0) path else null)
        }
        return ret
    }

    /**
     * Split path into two parts, where first part contains the given numer of segments.
     *
     * @param segments If segments is negative, number is counted backwards.
     * @return null if unable to split at the given number of segments.
     */
    fun splitSegments(path: String, segments: Int, vararg seps: Char): Pair<String?, String?>? {
        var seps = seps
        if (segments == 0) {
            return Pair(null, path)
        }
        if (seps.size == 0) {
            seps = SEP_ARRAY
        }
        var index: Int
        if (segments < 0) {
            index = path.length
            for (i in 0 until -segments) {
                index = lastIndexOfAny(path, 0, index, *seps)
                if (index < 0) {
                    return null
                }
            }
        } else {
            index = -1
            val len = path.length
            for (i in 0 until segments) {
                index = indexOfAny(seps, path, index + 1, len)
                if (index < 0) {
                    return null
                }
            }
        }
        return Pair(path.substring(0, index), path.substring(index + 1))
    }

    /**
     * Split path into two parts, where first part contains the given numer of segments.
     *
     * @param segments If segments is negative, number is counted backwards.
     * @return null if unable to split at the given number of segments.
     */
    fun splitSegments(path: String, sep: String, segments: Int): Pair<String?, String>? {
        if (segments == 0) {
            return Pair(null, path)
        }
        val slen = sep.length
        var index: Int
        if (segments < 0) {
            index = path.length + 1
            for (i in 0 until -segments) {
                index = path.lastIndexOf(sep, index - 1)
                if (index < 0) {
                    return null
                }
            }
        } else {
            index = -slen
            for (i in 0 until segments) {
                index = path.indexOf(sep, index + slen)
                if (index < 0) {
                    return null
                }
            }
        }
        return Pair(path.substring(0, index), path.substring(index + slen))
    }

    /**
     * Get the first n segments in the path. If there are no more than the given number of segments
     * return path itself.
     */
    fun getSegments(path: String, segments: Int, vararg seps: Char): String? {
        val p = splitSegments(path, segments, *seps) ?: return path
        return p.first
    }

    /**
     * A simplified version of getSegments() with default path separator char.
     */
    fun getPathSegments(path: String?, n: Int): String? {
        if (path == null) {
            return null
        }
        if (n == 0) {
            return ""
        }
        val sep = pathSeparatorChar
        var index = -1
        for (i in 0 until n) {
            index = path.indexOf(sep, index + 1)
            if (index < 0) {
                return path
            }
        }
        return path.substring(0, index)
    }

    /**
     * @param n The nth segment, 0 for first segment.
     * @return The nth segment in the path, null if not exists.
     */
    fun getSegment(path: String, n: Int, vararg seps: Char): String? {
        var seps = seps
        if (seps.size == 0) {
            seps = SEP_ARRAY
        }
        var index = -1
        var start = 0
        val len = path.length
        for (i in 0..n) {
            index = indexOfAny(seps, path, start, len)
            if (index < 0) {
                return if (i == n) {
                    path.substring(start)
                } else null
            }
            if (i == n) {
                return path.substring(start, index)
            }
            start = index + 1
        }
        return null
    }

    /**
     * @return Substring of the given path with the last segment removed.
     */
    fun popSegment(path: String, vararg delims: Char): String {
        val index = lastIndexOfAny(path, *delims)
        return if (index >= 0) {
            path.substring(0, index)
        } else ""
    }

    /**
     * @return Last segment of the given path, return the path itself if it is the only segment.
     */
    fun lastSegment(path: String, vararg delims: Char): String {
        val index = lastIndexOfAny(path, *delims)
        return if (index >= 0) {
            path.substring(index + 1)
        } else path
    }

    /**
     * Get the last n segments in the path. If there are no more than the given number of segments
     * return path itself.
     */
    fun lastSegments(path: String, segments: Int, vararg seps: Char): String {
        var segments = segments
        var end = path.length
        while (--segments >= 0) {
            val index = lastIndexOfAny(path, 0, end, *seps)
            if (index < 0) {
                return path
            }
            end = index
        }
        return if (end < path.length) {
            path.substring(end + 1)
        } else ""
    }

    fun extract(s: String, start: Int, open: Char, close: Char, def: String): String {
        var index = s.indexOf(open, start)
        if (index < 0) {
            return def
        }
        val end = s.indexOf(close, ++index)
        return if (end < 0) {
            s.substring(index)
        } else s.substring(index, end)
    }

    ////////////////////////////////////////////////////////////////////////

    fun toLowerCase(a: CharArray, start: Int, end: Int): CharArray {
        val ret = CharArray(end - start)
        for (i in end - 1 downTo start) {
            ret[i] = Character.toLowerCase(a[i])
        }
        return ret
    }

    fun toLowerCase(a: CharSequence, start: Int, end: Int): CharSequence {
        val ret = StringBuilder(end - start)
        for (i in start until end) {
            ret.append(Character.toLowerCase(a[i]))
        }
        return ret
    }

    fun toCharArray(buf: StringBuffer): CharArray {
        val len = buf.length
        val ret = CharArray(len)
        buf.getChars(0, len, ret, 0)
        return ret
    }

    @Deprecated("Use toStringList() or StructUtil.toList() instead.")
    fun toList(vararg a: String): List<String> {
        val ret = ArrayList<String>()
        Collections.addAll<String>(ret, *a)
        return ret
    }

    fun toStringList(vararg a: Any): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (s in a) {
            ret.add(toString(s))
        }
        return ret
    }

    fun toArray(a: Collection<String>): Array<String> {
        return a.toTypedArray()
    }

    fun toArray(a: List<String>, start: Int, end: Int): Array<String> {
        var start = start
        val len = end - start
        val ret = a.toTypedArray()
        var i = 0
        while (i < len) {
            ret[i] = a[start]
            ++i
            ++start
        }
        return ret
    }

    ////////////////////////////////////////////////////////////////////////

    fun lastChar(s: CharSequence): Int {
        val len = s.length
        return if (len == 0) {
            -1
        } else s[len - 1].toInt()
    }

    fun lastChar(s: CharSequence, n: Int): Int {
        require(n > 0) { "Expected n>0: n=$n" }
        val len = s.length
        return if (len < n) {
            -1
        } else s[len - n].toInt()
    }

    fun isChars(s: CharSequence, vararg chars: Char): Boolean {
        return isChars(s, 0, s.length, *chars)
    }

    fun isChars(s: CharSequence, chars: CharSequence): Boolean {
        return isChars(s, 0, s.length, chars)
    }

    fun isChars(s: CharSequence, start: Int, end: Int, vararg chars: Char): Boolean {
        val clen = chars.size
        var i = start - 1
        while (++i < end) {
            if (indexOf(s[i], chars, 0, clen) < 0) {
                return false
            }
        }
        return true
    }

    fun isChars(s: CharSequence, start: Int, end: Int, chars: CharSequence): Boolean {
        val clen = chars.length
        var i = start - 1
        while (++i < end) {
            if (indexOf(s[i], chars, 0, clen) < 0) {
                return false
            }
        }
        return true
    }

    fun endsWithChars(s: CharSequence, vararg chars: Char): Boolean {
        val len = s.length
        if (len == 0) {
            return false
        }
        val c = s[len - 1]
        for (cc in chars) {
            if (c == cc) {
                return true
            }
        }
        return false
    }

    fun endsWith(s: String, vararg exts: String): Boolean {
        for (ext in exts) {
            if (s.endsWith(ext)) {
                return true
            }
        }
        return false
    }

    fun endsWithIgnoreCase(s: CharSequence, ext: CharSequence): Boolean {
        val slen = s.length
        val elen = ext.length
        if (slen < elen) {
            return false
        }
        var si = slen - 1
        var ei = elen - 1
        while (ei >= 0) {
            val sc = s[si]
            val ec = ext[ei]
            if (sc != ec && Character.toLowerCase(sc) != Character.toLowerCase(ec)) {
                return false
            }
            --si
            --ei
        }
        return true
    }

    fun skipChars(str: CharSequence, start: Int, end: Int, vararg chars: Char): Int {
        var start = start
        var c: Char
        NEXT@ while (start < end) {
            c = str[start]
            for (cc in chars) {
                if (c == cc) {
                    ++start
                    continue@NEXT
                }
            }
            return start
            ++start
        }
        return start
    }

    /**
     * @return Index of last char that is not one of the given characters + 1;
     */
    fun trailingChars(buf: CharSequence, end: Int, vararg chars: Char): Int {
        var end = end
        var c: Char
        NEXT@ while (--end >= 0) {
            c = buf[end]
            for (cc in chars) {
                if (c == cc) {
                    continue@NEXT
                }
            }
            break
        }
        return end + 1
    }

    fun trailingChars(buf: CharSequence, vararg chars: Char): Int {
        return trailingChars(buf, buf.length, *chars)
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * Skip string started at given index 'start' and end with the given delimiter.
     *
     * @param str
     * @param start Index after the opening delimiter.
     * @param end
     * @param delim Close delimiter for the string.
     * @param esc   Escape prefix.
     * @return Index of the close delimiter.
     */
    fun skipString(str: CharSequence, start: Int, end: Int, delim: Char, esc: Int): Int {
        var start = start
        var c: Char
        while (start < end) {
            c = str[start]
            if (c == delim) {
                return start
            }
            if (c.toInt() == esc && start + 1 < end) {
                ++start
            }
            ++start
        }
        return start
    }

    fun skipString(str: CharSequence, start: Int, delim: Char): Int {
        return skipString(str, start, str.length, delim, '\\'.toInt())
    }

    /**
     * Skip a string end with the given delimiter string.
     *
     * @return Index at last char of the delimiter string.
     */
    fun skipString(str: CharSequence, start: Int, end: Int, delim: String): Int {
        var start = start
        outer@ while (start < end) {
            var i = 0
            while (i < delim.length) {
                if (start + i >= end) {
                    return end
                }
                if (str[start + i] != delim[i]) {
                    ++start
                    continue@outer
                }
                ++i
            }
            return start + i - 1
            ++start
        }
        return start
    }

    fun skipString(str: CharSequence, start: Int, delim: String): Int {
        return skipString(str, start, str.length, delim)
    }

    /**
     * Skip characters belong to the specified delimiters and return the remaining string.
     *
     * @return Remaining string, "" if nothing remains.
     */
    fun skipDelimiters(str: String, delimiters: String): String {
        for (i in 0 until str.length) {
            if (delimiters.indexOf(str[i]) < 0) {
                return str.substring(i)
            }
        }
        return ""
    }
    /**
     * @return Index of first occurence of delim or -1.
     */
    ////////////////////////////////////////////////////////////////////////
    /**
     * @return Index of first occurence of delim or -1.
     */

    fun indexOf(delim: Char, buf: CharSequence, start: Int = 0, end: Int = buf.length): Int {
        for (i in start until end) {
            if (buf[i] == delim) {
                return i
            }
        }
        return -1
    }

    /**
     * @return Index of first occurence of delim or -1.
     */
    fun indexOf(delim: Char, buf: CharArray, start: Int, end: Int): Int {
        for (i in start until end) {
            if (buf[i] == delim) {
                return i
            }
        }
        return -1
    }
    /**
     * @return Index of first occurence of delim or end.
     */
    /**
     * @return Index of first occurence of delim or end.
     */

    fun find(delim: Char, s: CharSequence, start: Int = 0, end: Int = s.length): Int {
        for (i in start until end) {
            if (s[i] == delim) {
                return i
            }
        }
        return end
    }

    /**
     * @return Index of last occurence of delim or -1.
     */
    fun lastIndexOf(delim: Char, buf: CharSequence, start: Int, end: Int): Int {
        for (i in end - 1 downTo start) {
            if (buf[i] == delim) {
                return i
            }
        }
        return -1
    }

    /**
     * @return Index of last occurence of delim or -1.
     */
    fun lastIndexOf(delim: Char, buf: CharArray, start: Int, end: Int): Int {
        for (i in end - 1 downTo start) {
            if (buf[i] == delim) {
                return i
            }
        }
        return -1
    }

    fun indexOfAny(delims: CharArray, str: CharSequence): Int {
        var c: Char
        for (i in 0 until str.length) {
            c = str[i]
            for (k in delims.indices) {
                if (c == delims[k]) {
                    return i
                }
            }
        }
        return -1
    }

    fun indexOfAny(delims: CharArray, str: CharSequence, start: Int, end: Int): Int {
        var c: Char
        for (i in start until end) {
            c = str[i]
            for (k in delims.indices) {
                if (c == delims[k]) {
                    return i
                }
            }
        }
        return -1
    }

    fun indexOfWhitespace(str: CharSequence, start: Int = 0, end: Int = str.length): Int {
        var c: Char
        for (i in start until end) {
            c = str[i]
            if (Character.isWhitespace(c)) {
                return i
            }
        }
        return -1
    }

    fun startsWith(needle: CharSequence, haystack: CharSequence): Boolean {
        val len = needle.length
        if (haystack.length < len) {
            return false
        }
        for (i in 0 until len) {
            if (haystack[i] != needle[i]) {
                return false
            }
        }
        return true
    }

    fun startsWith(needle: CharSequence, haystack: CharSequence, start: Int, end: Int): Boolean {
        val len = needle.length
        if (end - start < len) {
            return false
        }
        for (i in 0 until len) {
            if (haystack[start + i] != needle[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Count number of occurance of a char in a string.
     */
    fun count(c: Char, str: CharSequence): Int {
        var ret = 0
        for (i in 0 until str.length) {
            if (str[i] == c) {
                ret++
            }
        }
        return ret
    }

    fun count(c: Char, buf: CharSequence, start: Int, end: Int): Int {
        var ret = 0
        for (i in start until end) {
            if (buf[i] == c) {
                ret++
            }
        }
        return ret
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * Trim leading space characters (preserve line breaks).
     *
     * @return false is input buffer is unchanged.
     */
    fun trimLeadingChars(buf: StringBuilder, start: Int, end: Int, vararg chars: Char): Boolean {
        val s = skipChars(buf, start, end, *chars)
        if (s != start) {
            buf.delete(start, s)
            return true
        }
        return false
    }

    /**
     * Trim trailing characters.
     *
     * @return false if buffer is unchanged.
     */
    fun trimTrailingChars(buf: StringBuilder, vararg chars: Char): Boolean {
        val len = buf.length
        val end = trailingChars(buf, len, *chars)
        if (end != len) {
            buf.setLength(end)
            return true
        }
        return false
    }

    /**
     * Trim trailing File.separatorChar.
     */
    fun trimTrailingSep(s: String?): String? {
        if (s == null) {
            return null
        }
        val len = s.length
        var end = len
        while (--end > 0) {
            if (s[end] != File.separatorChar) {
                break
            }
        }
        return if (end < len - 1) s.substring(0, end + 1) else s
    }

    /**
     * Trim trailing seps from given string.
     */
    fun trimTrailingChars(s: String?, vararg seps: Char): String? {
        if (s == null) {
            return null
        }
        val len = s.length
        var end = len
        NEXT@ while (--end > 0) {
            val ss = s[end]
            for (c in seps) {
                if (ss == c) {
                    continue@NEXT
                }
            }
            break
        }
        return if (end < len - 1) s.substring(0, end + 1) else s
    }

    /**
     * Trim leading and trailing occurences of given delims.
     */
    fun trim(delims: CharArray, str: String): String {
        val len = str.length
        var c: Char
        var start = 0
        start_loop@ while (start < len) {
            c = str[start]
            for (i in delims.indices) {
                if (c == delims[i]) {
                    ++start
                    continue@start_loop
                }
            }
            break
            ++start
        }
        var end = len - 1
        end_loop@ while (end > start) {
            c = str[end]
            for (i in delims.indices) {
                if (c == delims[i]) {
                    --end
                    continue@end_loop
                }
            }
            break
            --end
        }
        return str.substring(start, end + 1)
    }

    fun trim(s: CharSequence): CharSequence {
        return trim(DefaultSpaceUtil.Companion.singleton, s, 0, s.length)
    }

    fun trim(s: CharSequence, start: Int, end: Int): CharSequence {
        return trim(DefaultSpaceUtil.Companion.singleton, s, start, end)
    }

    fun trim(sutil: ISpaceUtil, s: CharSequence, start: Int = 0, end: Int = s.length): CharSequence {
        var start = start
        var end = end
        end = sutil.rskipWhitespaces(s, start, end)
        start = sutil.skipWhitespaces(s, start, end)
        return CharSequenceRange(s, start, end)
    }

    /**
     * Wrap given string to lines less than the given width.
     */

    fun wrapLine(text: String, width: Int, tabwidth: Int, linebreak: String = lineSeparator): String {
        val LINEBREAKS = charArrayOf('\r', '\n')
        if (text.isEmpty()) {
            return ""
        }
        val tokenizer = StringTokenizer(text, " \t\r\n$linebreak", true)
        val ret = StringBuilder()
        var tok: String
        var column = 0
        while (tokenizer.hasMoreTokens()) {
            tok = tokenizer.nextToken()
            if (tok == "\t") {
                tok = ""
                for (i in 0 until tabwidth - column % 8) {
                    tok += ' '
                }
            }
            if (column != 0 && column + tok.length >= width) {
                ret.append(linebreak)
                column = 0
                tok = skipSpaceTokens(tokenizer, tok)
            }
            ret.append(tok)
            if (tok == linebreak || indexOfAny(LINEBREAKS, tok) >= 0) {
                column = 0
            } else {
                column += tok.length
            }
        }
        return ret.toString()
    }

    fun reverse(s: String): String {
        val ret = StringBuilder(s)
        return ret.reverse().toString()
    }

    /**
     * Find first offset of the difference between the two string ignoring all whitespaces.
     *
     * @param offsets If not null, return list of all whitespace boundaries pairs that differ { start1, end1, start2, end2 }.
     * @return Offsets of the first difference in str1 and str2 respectively, null if strings are same.
     */
    ////////////////////////////////////////////////////////////////////////

    fun differIgnoringWhitespaces(
            str1: CharSequence,
            str2: CharSequence,
            offsets: IIntList?,
            ignorecase: Boolean,
            namer: INameDetector? = DefaultNameDetector.singleton,
            spacer: ISpaceDetector = DefaultSpaceUtil.singleton
    ): IntArray? {
        var c1 = 0.toChar()
        var c2 = 0.toChar()
        var end1 = 0
        var end2 = 0
        var wend1 = 0
        var wend2 = 0
        val len1 = str1.length
        val len2 = str2.length
        var start1: Int
        var start2: Int
        var a1 = false
        var a2 = false
        while (end1 < len1 && end2 < len2) {
            start1 = end1
            start2 = end2
            while (end1 < len1 && spacer.isWhitespace(str1[end1].also { c1 = it })) {
                ++end1
            }
            while (end2 < len2 && spacer.isWhitespace(str2[end2].also { c2 = it })) {
                ++end2
            }
            if (offsets != null && (end1 != start1 || end2 != start2)) {
                if (!match(str1, start1, end1, str2, start2, end2)) {
                    offsets.add(start1)
                    offsets.add(end1)
                    offsets.add(start2)
                    offsets.add(end2)
                }
            }
            if (end1 >= len1 || end2 >= len2) {
                break
            }
            wend1 = end1 + 1
            wend2 = end2 + 1
            while (wend1 < len1 && !spacer.isWhitespace(str1[wend1])) {
                ++wend1
            }
            val wlen1 = wend1 - end1
            var len = wlen1
            while (--len > 0 && wend2 < len2 && !spacer.isWhitespace(str2[wend2])) {
                ++wend2
            }
            val wlen2 = wend2 - end2
            if (wlen2 < wlen1) {
                wend1 = end1 + wlen2
            }
            if (!match(str1, end1, wend1, str2, end2, wend2)) {
                if (!ignorecase) {
                    return intArrayOf(end1, end2)
                }
                if (!matchIgnoreCase(str1, end1, wend1, str2, end2, wend2)) {
                    return intArrayOf(end1, end2)
                }
                if (offsets != null) {
                    offsets.add(end1)
                    offsets.add(wend1)
                    offsets.add(end2)
                    offsets.add(wend2)
                }
            }
            if (namer != null) {
                if (end1 == start1 && end2 != start2) {
                    if (a1 && namer.isNamePart(c1)) {
                        return intArrayOf(end1, end2)
                    }
                }
                if (end2 == start2 && end1 != start1) {
                    if (a2 && namer.isNamePart(c2)) {
                        return intArrayOf(end1, end2)
                    }
                }
                a1 = namer.isNamePart(str1[wend1 - 1])
                a2 = namer.isNamePart(str2[wend2 - 1])
            }
            end1 = wend1
            end2 = wend2
            if (wend1 >= len1 || wend2 >= len2) {
                break
            }
        }
        start1 = end1
        start2 = end2
        end1 = spacer.skipWhitespaces(str1, end1, len1)
        end2 = spacer.skipWhitespaces(str2, end2, len2)
        if (offsets != null && (end1 != start1 || end2 != start2)) {
            if (!match(str1, start1, end1, str2, start2, end2)) {
                offsets.add(start1)
                offsets.add(end1)
                offsets.add(start2)
                offsets.add(end2)
            }
        }
        if (end1 == len1 && end2 == len2) {
            return null
        }
        return intArrayOf(end1, end2)
    }

    fun threeLines(offset: Int, s: CharSequence, start: Int, end: Int): CharSequence {
        val util: DefaultSpaceUtil = DefaultSpaceUtil.singleton
        val ss = util.rskipLine(s, start, util.rskipLineBreak(s, start, util.rskipLine(s, start, offset)))
        val ee = util.skipLineBreak(s, util.skipLineSafe(s, util.skipLineBreak(s, util.skipLineSafe(s, offset, end), end), end), end)
        return s.subSequence(ss, offset).toString() + "|***|" + s.subSequence(offset, ee)
    }

    fun match(str1: CharSequence, start1: Int, end1: Int, str2: CharSequence, start2: Int, end2: Int): Boolean {
        val len = end1 - start1
        if (end2 - start2 != len) {
            return false
        }
        for (i in len - 1 downTo 0) {
            if (str1[start1 + i] != str2[start2 + i]) {
                return false
            }
        }
        return true
    }

    fun match(str1: CharSequence, start1: Int, end1: Int, str2: CharSequence, start2: Int): Boolean {
        val len = end1 - start1
        if (start2 + len > str2.length) {
            return false
        }
        for (i in len - 1 downTo 0) {
            if (str1[start1 + i] != str2[start2 + i]) {
                return false
            }
        }
        return true
    }

    fun matchIgnoreCase(str1: CharSequence, start1: Int, end1: Int, str2: CharSequence, start2: Int, end2: Int): Boolean {
        val len = end1 - start1
        if (end2 - start2 != len) {
            return false
        }
        var c1: Char
        var c2: Char
        for (i in len - 1 downTo 0) {
            c1 = str1[start1 + i]
            c2 = str2[start2 + i]
            if (c1 != c2 && c1 != Character.toLowerCase(c2) && c1 != Character.toUpperCase(c2)) {
                return false
            }
        }
        return true
    }

    /**
     * Skip space, tab and first line delimiter.
     *
     * @return The next token not space/tab or second line delimiter.
     */
    private fun skipSpaceTokens(tokenizer: StringTokenizer, tok: String): String {
        var tok = tok
        while (skipDelimiters(tok, " \t").also { tok = it }.length == 0) {
            if (!tokenizer.hasMoreTokens()) {
                return ""
            }
            tok = tokenizer.nextToken()
        }
        if (tok == "\r") {
            tok = tokenizer.nextToken()
            if (tok == "\n") {
                return tokenizer.nextToken()
            }
        }
        return tok
    }

    ////////////////////////////////////////////////////////////////////////
    fun indentWidthOf(str: CharSequence, start: Int, tabwidth: Int): Int {
        var start = start
        var ret = 0
        var c: Char
        val len = str.length
        while (start < len) {
            c = str[start]
            if (c == '\t') {
                ret = (ret / tabwidth + 1) * tabwidth
            } else if (c == ' ' || c.toInt() == 0xa0) {
                ++ret
            } else if (c == '\n' || c == '\r') {
                ret = 0
            } else {
                break
            }
            ++start
        }
        return ret
    }

    fun indentWidthOf(str: CharSequence, tabwidth: Int): Int {
        return indentWidthOf(str, 0, tabwidth)
    }

    /**
     * @param start Start offset for column 1.
     * @param end   End offfset whose column number is to be determined.
     * @return 1-based column number of 'end'.
     */
    fun columnOf(str: CharSequence, start: Int, end: Int, tabwidth: Int): Int {
        var start = start
        var ret = 0
        var c: Char
        while (start < end) {
            c = str[start]
            if (c == '\r' || c == '\n') {
                ret = 0
            } else if (c == '\t') {
                ret = (ret / tabwidth + 1) * tabwidth
            } else {
                ++ret
            }
            ++start
        }
        return ret + 1
    }

    fun spacesToTabs(text: CharSequence, tabwidth: Int): String {
        val buf = StringBuilder()
        var c: Char
        var count = 0
        for (i in 0 until text.length) {
            c = text[i]
            if (c == ' ' || c.toInt() == 0xa0) {
                ++count
                if (count == tabwidth) {
                    buf.setLength(buf.length - count + 1)
                    buf.append('\t')
                    count = 0
                }
            } else {
                buf.append(c)
                count = 0
            }
        }
        return buf.toString()
    }

    fun tabToSpaces(text: CharSequence, tab: String?): String {
        val buf = StringBuilder()
        var c: Char
        for (i in 0 until text.length) {
            c = text[i]
            if (c == '\t') {
                buf.append(tab)
            } else {
                buf.append(c)
            }
        }
        return buf.toString()
    }

    /**
     * Convert between tab and space according to given tab string.
     */
    fun convertTabs(text: CharSequence, tab: String, tabwidth: Int): String {
        return if (tab == "\t") {
            spacesToTabs(text, tabwidth)
        } else tabToSpaces(text, tab)
    }

    fun stringOf(c: Char, n: Int): String {
        var n = n
        val b = StringBuilder()
        while (--n >= 0) {
            b.append(c)
        }
        return b.toString()
    }

    fun spacesOf(n: Int): String {
        return when (n) {
            0 -> ""
            1 -> " "
            2 -> "  "
            3 -> "   "
            4 -> "    "
            5 -> "     "
            6 -> "      "
            7 -> "       "
            else -> stringOf(' ', n)
        }
    }

    fun withSign(value: Int): String {
        return if (value > 0) {
            "+$value"
        } else value.toString()
    }

    fun parseInt(s: String?, def: Int): Int {
        s ?: return def
        return try {
            s.toInt()
        } catch (e: Throwable) {
            def
        }
    }

    fun parseLong(s: String?, def: Long): Long {
        s ?: return def
        return try {
            s.toLong()
        } catch (e: Throwable) {
            def
        }
    }

    /**
     * @param str Space and/or comma separated decimal strings.
     * @return int[] that the numeric values.
     */
    @Throws(NumberFormatException::class)
    fun parseLongs(str: String?): LongArray? {
        if (str == null) {
            return null
        }
        val tok = StringTokenizer(str, " \t\n\u000c,")
        val ret = LongArray(tok.countTokens())
        val count = 0
        while (tok.hasMoreTokens()) {
            ret[count] = tok.nextToken().toLong()
        }
        return ret
    }

    fun parseOct(s: String?, def: Int): Int {
        s ?: return def
        return try {
            s.toInt(8)
        } catch (e: Throwable) {
            def
        }
    }

    fun parseHex(s: String?, def: Int): Int {
        var s = s ?: return def
        if (s.length > 2 && s[0] == '0' && (s[1] == 'x' || s[1] == 'X')) {
            s = s.substring(2)
        }
        return try {
            s.toInt(16)
        } catch (e: Throwable) {
            def
        }
    }

    fun parseFloat(s: String?, def: Float): Float {
        s ?: return def
        return try {
            s.toFloat()
        } catch (e: Throwable) {
            def
        }
    }

    /**
     * Parse 's' for two comma separated float numbers (eg. 1.2f, 2.4).
     */
    @Throws(NumberFormatException::class)
    fun parseFloat2(s: String): FloatArray {
        val ret = FloatArray(2)
        var index = s.indexOf(',')
        if (index < 0) {
            throw NumberFormatException("TextUtil.parseFloat2(): invalid input string: $s")
        }
        ret[0] = s.substring(0, index).toFloat()
        index = DefaultSpaceUtil.singleton.skipWhitespaces(s, ++index, s.length)
        ret[1] = s.substring(index).toFloat()
        return ret
    }

    fun parseDouble(s: String?, def: Double): Double {
        s ?: return def
        try {
            return s.toDouble()
        } catch (e: Throwable) {
            return def
        }
    }

    /**
     * Parse 's' for two comma separated float numbers (eg. 1.2, 2.4).
     */
    @Throws(NumberFormatException::class)
    fun parseDouble2(s: String): DoubleArray {
        val ret = DoubleArray(2)
        var index = s.indexOf(',')
        if (index < 0) {
            throw NumberFormatException("TextUtil.parseDouble2(): invalid input string: $s")
        }
        ret[0] = s.substring(0, index).toDouble()
        index = DefaultSpaceUtil.singleton.skipWhitespaces(s, ++index, s.length)
        ret[1] = s.substring(index).toDouble()
        return ret
    }

    fun parseDate(date: String?, def: Long): Long {
        date ?: return def
        try {
            val ret = DateFormat.getDateTimeInstance().parse(date) ?: return def
            return ret.time
        } catch (e: ParseException) {
            return def
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /* @deprecated Use sprintCollection() instead. */
    fun <T> sprintList(a: Collection<T>, indent: CharSequence?): String {
        return sprintCollection(a, indent)
    }

    fun <T> sprintCollection(a: Collection<T>, indent: CharSequence?): String {
        val buf = StringBuilder()
        for (t in a) {
            buf.append(indent)
            buf.append(t.toString())
        }
        return buf.toString()
    }

    fun <K, V> sprintMap(a: Map<K, V>, indent: CharSequence?): String {
        val buf = StringBuilder()
        for ((key, value) in a) {
            buf.append(indent)
            buf.append(key.toString())
            buf.append(" => ")
            buf.append(value.toString())
        }
        return buf.toString()
    }

    fun sprintf(format: String, vararg args: Any): String {
        return format(format, *args)
    }

    fun sprintlnf(format: String, vararg args: Any): String {
        return format(format + lineSeparator, *args)
    }

    fun sprintHexDump(a: ByteArray): CharSequence {
        return sprintHexDump("", ByteSequence.wrap(a), 0, a.size)
    }

    fun sprintHexDump(a: ByteArray, start: Int, end: Int): CharSequence {
        return sprintHexDump("", ByteSequence.wrap(a), start, end)
    }

    fun sprintHexDump(offsetformat: String, a: ByteArray, start: Int, end: Int): CharSequence {
        return sprintHexDump(offsetformat, ByteSequence.wrap(a), start, end)
    }

    /**
     * Dump byte array in hexdump -C format.
     *
     * @param offsetformat Offset output format string, eg. "%08x: "
     */
    fun sprintHexDump(offsetformat: String?, a: IByteSequence, start: Int, end: Int): CharSequence {
        val ret = StringPrintWriter()
        printHexDump(ret, offsetformat, "%02x ", a, start, end)
        return ret.toString()
    }

    fun printHexDump(out: PrintStream, offsetformat: String?, a: ByteArray) {
        printHexDump(PrintWriter(out), offsetformat, a, 0, a.size)
    }

    fun printHexDump(out: PrintStream, offsetformat: String?, a: ByteArray, start: Int, end: Int) {
        printHexDump(PrintWriter(out), offsetformat, a, start, end)
    }

    fun printHexDump(out: PrintStream, offsetformat: String?, valueformat: String, a: ByteArray, start: Int, end: Int) {
        printHexDump(PrintWriter(out), offsetformat, valueformat, ByteSequence.wrap(a), start, end)
    }

    /**
     * Dump byte array in hexdump -C format.
     *
     * @param offsetformat Offset output format string, eg. "%08x: "
     */

    fun printHexDump(out: PrintWriter, offsetformat: String?, a: ByteArray, start: Int = 0, end: Int = a.size) {
        printHexDump(out, offsetformat, "%02x ", ByteSequence.wrap(a), start, end)
    }

    fun printHexDump(out: PrintWriter, offsetformat: String?, valueformat: String, a: ByteArray, start: Int, end: Int) {
        printHexDump(out, offsetformat, valueformat, ByteSequence.wrap(a), start, end)
    }

    fun printHexDump(
            out: PrintWriter,
            offsetformat: String?,
            valueformat: String,
            a: IByteSequence,
            start: Int = 0,
            end: Int = a.size()
    ) {
        val PERLINE = 16
        val len = a.size()
        if (len == 0) {
            out.println()
            out.flush()
            return
        }
        val padding = spacesOf(format(valueformat, 0).length)
        var xstart = start / PERLINE * PERLINE
        val xend = ((end - 1) / PERLINE + 1) * PERLINE
        while (xstart < xend) {
            if (offsetformat != null) {
                out.print(format(offsetformat, xstart))
            }
            for (i in 0 until PERLINE) {
                if (i == PERLINE / 2) {
                    out.print(' ')
                }
                val index = xstart + i
                if (index >= start && index < end) {
                    out.print(format(valueformat, a.byteAt(index)))
                } else {
                    out.print(padding)
                }
            }
            out.print(" |")
            for (i in 0 until PERLINE) {
                val index = xstart + i
                if (index >= start && index < end) {
                    val c: Int = a.byteAt(index).toInt() and 0xff
                    if (c < 0x20 || c >= 0x7f) {
                        out.print('.')
                    } else {
                        out.print(c.toChar())
                    }
                } else {
                    out.print(' ')
                }
            }
            out.println("|")
            xstart += PERLINE
        }
        out.flush()
    }

    fun printHexC(
            out: PrintStream,
            perline: Int,
            indent: String?,
            offsetformat: String?,
            valueformat: String,
            startcomment: String?,
            endcomment: String?,
            a: ByteArray,
            start: Int,
            end: Int
    ) {
        printHexC(PrintWriter(out), perline, indent, offsetformat, valueformat, startcomment, endcomment, a, start, end)
    }

    /**
     * Similar to printHexDump, but allow custom PERLINE, indent, comment separator and no linefeed on empty input.
     */
    fun printHexC(
            out: PrintWriter,
            perline: Int,
            indent: String?,
            offsetformat: String?,
            valueformat: String,
            startcomment: String?,
            endcomment: String?,
            a: ByteArray,
            start: Int,
            end: Int
    ) {
        val len = a.size
        if (len == 0 || start == end) {
            return
        }
        val padding = spacesOf(format(valueformat, 0).length)
        var xstart = start / perline * perline
        val xend = ((end - 1) / perline + 1) * perline
        while (xstart < xend) {
            if (indent != null) {
                out.print(indent)
            }
            if (offsetformat != null) {
                out.print(format(offsetformat, xstart))
            }
            for (i in 0 until perline) {
                if (i == perline / 2) {
                    out.print(' ')
                }
                val index = xstart + i
                if (index >= start && index < end) {
                    out.print(format(valueformat, a[index]))
                } else {
                    out.print(padding)
                }
            }
            out.print(startcomment)
            for (i in 0 until perline) {
                val index = xstart + i
                if (index >= start && index < end) {
                    val c: Int = a[index].toInt() and 0xff
                    if (c < 0x20 || c >= 0x7f) {
                        out.print('.')
                    } else {
                        out.print(c.toChar())
                    }
                } else {
                    out.print(' ')
                }
            }
            out.println(endcomment)
            xstart += perline
        }
        out.flush()
    }

    /**
     * Print bytes in java source code format.
     *
     * @param out
     * @param perline
     * @param indent
     * @param valueformat   Value format for each individual value including spacing but and trailing comma, %1 is value.
     * @param commentformat including trailing line break, %1 is offset, %2 is printable characters of the values.
     * @param data
     */
    fun printHexJava(
            out: PrintWriter, perline: Int, indent: String?, valueformat: String, commentformat: String?, data: IByteSequence
    ) {
        val size = data.size()
        if (size == 0) {
            return
        }
        val s = StringBuilder()
        var i = 0
        while (i < size) {
            out.print(indent)
            val offset = i
            s.setLength(0)
            var l = 0
            while (l < perline && i < size) {
                val v = data.byteAt(i)
                out.printf(valueformat, v)
                s.append(if (v >= 0x20 && v <= 0x7e) v.toChar() else '.')
                ++l
                ++i
            }
            if (commentformat != null) {
                out.printf(commentformat, offset, s)
            } else {
                out.println()
            }
        }
        out.flush()
    }

    fun printHex(
            out: PrintStream,
            perline: Int,
            indent: String?,
            offsetformat: String?,
            valueformat: String,
            a: ByteArray,
            start: Int,
            end: Int
    ) {
        printHex(PrintWriter(out), perline, indent, offsetformat, valueformat, a, start, end)
    }

    /**
     * Print byte array in hex, like printHexDump(), but without character block.
     */
    fun printHex(
            out: PrintWriter,
            perline: Int,
            indent: String?,
            offsetformat: String?,
            valueformat: String,
            a: ByteArray,
            start: Int,
            end: Int
    ) {
        val len = a.size
        if (len == 0 || start == end) {
            return
        }
        var xstart = start / perline * perline
        val xend = ((end - 1) / perline + 1) * perline
        while (xstart < xend) {
            if (indent != null) {
                out.print(indent)
            }
            if (offsetformat != null) {
                out.print(format(offsetformat, xstart))
            }
            for (i in 0 until perline) {
                if (i == perline / 2) {
                    out.print(' ')
                }
                val index = xstart + i
                if (index >= start && index < end) {
                    out.print(format(valueformat, a[index]))
                } else {
                    out.print("   ")
                }
            }
            out.println()
            xstart += perline
        }
        out.flush()
    }
    /**
     * Print byte array in hex.
     */
    /**
     * Print byte array in hex.
     */

    fun sprintHex(perline: Int, format: String, a: ByteArray, start: Int = 0, end: Int = a.size): CharSequence {
        var start = start
        val ret = StringBuilder()
        val len = a.size
        if (len == 0 || start == end) {
            ret.append("\n")
            return ret
        }
        while (start < end) {
            var i = 0
            while (i < perline && start + i < end) {
                if (i == perline / 2) {
                    ret.append(' ')
                }
                ret.append(format(format, a[start + i]))
                ++i
            }
            ret.append(lineSeparator)
            start += perline
        }
        return ret
    }

    fun toLowerHex(a: ByteArray): CharSequence {
        return toLowerHex(a, 0, a.size)
    }

    fun toLowerHex(a: ByteArray, start: Int, end: Int = a.size): CharSequence {
        var start = start
        val b = StringBuilder()
        while (start < end) {
            val v = a[start].toInt()
            b.append(HEX_LOWER[v ushr 4 and 0xf])
            b.append(HEX_LOWER[v and 0xf])
            ++start
        }
        return b
    }

    fun toUpperHex(a: ByteArray): CharSequence {
        return toUpperHex(a, 0, a.size)
    }

    fun toUpperHex(a: ByteArray, start: Int, end: Int = a.size): CharSequence {
        var start = start
        val b = StringBuilder()
        while (start < end) {
            val v = a[start].toInt()
            b.append(HEX_UPPER[v ushr 4 and 0xf])
            b.append(HEX_UPPER[v and 0xf])
            ++start
        }
        return b
    }

    fun toString(a: Any?): String {
        return a?.toString() ?: "null"
    }

    fun toStringArray(vararg a: Any): Array<String?> {
        val len = a.size
        val ret = arrayOfNulls<String>(len)
        for (i in 0 until len) {
            ret[i] = if (a[i] == null) null else a[i].toString()
        }
        return ret
    }

    /**
     * Similar to toString() but return null instead of "null" if object is null.
     */
    fun stringValue(a: Any?): String? {
        return a?.toString()
    }

    /**
     * @return a.toString() if a is not null, otherwise return def.
     */
    fun stringValue(a: Any?, def: String): String {
        return a?.toString() ?: def
    }

    /**
     * Similar to toStringArray() but return null instead of "null" if object is null.
     */
    fun stringArrayValue(vararg a: Any): Array<String?> {
        val len = a.size
        val ret = arrayOfNulls<String>(len)
        for (i in 0 until len) {
            ret[i] = if (a[i] == null) null else a[i].toString()
        }
        return ret
    }

    fun sprintArray(format: String, a: ByteArray, start: Int = 0, end: Int = a.size): String {
        var start = start
        val ret = StringBuilder()
        while (start < end) {
            ret.append(format(format, a[start]))
            ++start
        }
        return ret.toString()
    }

    fun sprintArray(format: String, a: IntArray): String {
        val ret = StringBuilder()
        for (n in a) {
            ret.append(format(format, n))
        }
        return ret.toString()
    }

    fun sprintArray(format: String, a: IntArray, start: Int, end: Int): String {
        var start = start
        val ret = StringBuilder()
        while (start < end) {
            ret.append(format(format, a[start++]))
        }
        return ret.toString()
    }

    @SafeVarargs
    fun <T> sprintArray(vararg a: T): String {
        val ret = StringBuilder()
        var first = true
        for (e in a) {
            if (first) {
                first = false
            } else {
                ret.append(", ")
            }
            ret.append(toString(e))
        }
        return ret.toString()
    }

    @SafeVarargs
    fun <T> sprintArray(format: String, vararg a: T): String {
        return sprintArray(format, 0, a.size, *a)
    }

    @SafeVarargs
    fun <T> sprintArray(format: String, start: Int, end: Int, vararg a: T): String {
        val ret = StringBuilder()
        var first = true
        for (i in start until end) {
            if (first) {
                first = false
            } else {
                ret.append(", ")
            }
            ret.append(format(format, a[i]))
        }
        return ret.toString()
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * Split lines using "\n" or "\r\n" as delimiters.
     */
    fun splitLines(str: CharArray?): List<String>? {
        if (str == null) {
            return null
        }
        val ret: MutableList<String> = ArrayList()
        var start = 0
        val len = str.size
        var c: Char
        var index = 0
        while (index < len) {
            c = str[index]
            if (c == '\r' || c == '\n') {
                ret.add(String(str, start, index - start))
                if (c == '\r' && index + 1 < len && str[index + 1] == '\n') {
                    ++index
                }
                start = index + 1
            }
            ++index
        }
        if (start < str.size) {
            ret.add(String(str, start, len - start))
        }
        return ret
    }

    /**
     * @return The dir/name if dir and name are not null or empty,
     * return dir if name is null or empty.
     * return name if dir is null or empty,
     * return "" if both dir and name are null or empty,
     */
    fun joinRpath(dir: String?, name: String?): String {
        return if (isEmpty(dir)) (name ?: "") else if (isEmpty(name)) (dir ?: "") else dir + File.separator + name
    }

    fun <T> join(sep: String, a: Iterable<T>): String {
        val ret = StringBuilder()
        var first = true
        for (s in a) {
            if (first) {
                first = false
            } else {
                ret.append(sep)
            }
            ret.append(s)
        }
        return ret.toString()
    }

    @SafeVarargs
    fun <T> join(sep: String, vararg a: T): String {
        val ret = StringBuilder()
        var first = true
        for (s in a) {
            if (first) {
                first = false
            } else {
                ret.append(sep)
            }
            ret.append(s)
        }
        return ret.toString()
    }

    fun join(sep: CharSequence, a: Array<out String>, start: Int, end: Int = a.size): String {
        val len = end - start
        if (len == 0) {
            return ""
        }
        if (len == 1) {
            return a[0]
        }
        val b = StringBuilder()
        for (i in start until end) {
            if (i != 0) {
                b.append(sep)
            }
            b.append(a[i])
        }
        return b.toString()
    }

    fun join(sep: String, vararg a: String): String {
        return join(sep, a, 0, a.size)
    }

    fun join(sep: CharSequence, a: List<String>, start: Int, end: Int = a.size): String {
        val b = StringBuilder()
        for (i in start until end) {
            if (i != start) {
                b.append(sep)
            }
            b.append(a[i])
        }
        return b.toString()
    }

    /**
     * Like join() but with leading sep if a is not empty.
     *
     * @return /a/b/c for join1("/", ["a", "b", "c"]), return "" for join1("/", []).
     */
    fun join1(sep: String, a: List<String?>, start: Int, end: Int): String {
        return join1(StringBuilder(), sep, a, start, end)
    }

    fun join1(b: StringBuilder, sep: String, a: List<String?>, start: Int, end: Int): String {
        for (i in start until end) {
            b.append(sep)
            b.append(a[i])
        }
        return b.toString()
    }

    fun isDelimiter(delimiters: String, str: String): Boolean {
        for (i in str.length - 1 downTo 0) {
            if (delimiters.indexOf(str[i]) < 0) {
                return false
            }
        }
        return true
    }

    fun isEmpty(s: CharSequence?): Boolean {
        return s == null || s.length == 0
    }

    fun indexOfAny(delimiters: String, str: String): Int {
        var i = 0
        val len = str.length
        while (i < len) {
            if (delimiters.indexOf(str[i]) >= 0) {
                return i
            }
            ++i
        }
        return -1
    }

    fun lastIndexOfAny(delimiters: String, str: String): Int {
        for (i in str.length - 1 downTo 0) {
            if (delimiters.indexOf(str[i]) >= 0) {
                return i
            }
        }
        return -1
    }

    fun lastIndexOfAny(str: String, vararg delims: Char): Int {
        return lastIndexOfAny(str, 0, str.length, *delims)
    }

    fun lastIndexOfAny(str: String, start: Int, end: Int, vararg delims: Char): Int {
        for (i in end - 1 downTo start) {
            for (c in delims) {
                if (c == str[i]) {
                    return i
                }
            }
        }
        return -1
    }

    fun isOneOf(needle: String?, vararg stack: String): Boolean {
        for (s in stack) {
            if (equals(needle, s)) {
                return true
            }
        }
        return false
    }

    ////////////////////////////////////////////////////////////////////////
    fun splitManifestAttrValue(value: String): List<String> {
        val ret: MutableList<String> = ArrayList()
        for (s in ManifestValueScanner(value)) {
            ret.add(s.trim { it <= ' ' })
        }
        return ret
    }

    fun getSimpleClassName(c: Class<*>): String {
        return getSimpleClassName(c.name)
    }

    fun getSimpleClassName(name: String): String {
        var name = name
        var index = name.lastIndexOf('.')
        if (index >= 0) {
            name = name.substring(index + 1)
        }
        index = name.lastIndexOf('$')
        if (index >= 0) {
            name = name.substring(index + 1)
        }
        return name
    }

    fun getOuterName(c: Class<*>): String {
        return getOuterName(c.name)
    }

    fun getOuterName(name: String): String {
        var name = name
        val index = name.lastIndexOf('.')
        if (index >= 0) {
            name = name.substring(index + 1)
        }
        return name
    }

    /**
     * @return Simple date string of format 20090101
     */
    fun dateString(): String {
        return SimpleDateFormat("yyyyMMdd").format(Date())
    }

    /**
     * @return Simple date string of format 20090101
     */
    fun dateString(date: Date?): String {
        return SimpleDateFormat("yyyyMMdd").format(date)
    }

    /**
     * @return Simple date string of format 20090101
     */
    fun dateString(ms: Long): String {
        return SimpleDateFormat("yyyyMMdd").format(Date(ms))
    }

    /**
     * @return Simple datetime string of format 20090101 01:01:01
     */
    fun dateTime(): String {
        return SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Date())
    }

    /**
     * @return Simple datetime string of format 20090101 01:01:01
     */
    fun dateTime(date: Date?): String {
        return SimpleDateFormat("yyyyMMdd HH:mm:ss").format(date)
    }

    /**
     * @return Simple datetime string of format 20090101 01:01:01
     */
    fun dateTime(ms: Long): String {
        return SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Date(ms))
    }

    /**
     * String.format((Locale)null, format, args).
     */
    @kotlin.jvm.JvmStatic
    fun format(format: String, vararg args: Any?): String {
        return java.lang.String.format(Locale.ROOT, format, *args)
    }

    /**
     * Generate an array of string from the given format with the given integer range.
     */
    fun formatRange(format: String, first: Int, last: Int): Array<String?> {
        val len = last - first + 1
        val ret = arrayOfNulls<String>(len)
        for (i in 0 until len) {
            ret[i] = format(format, first + i)
        }
        return ret
    }

    fun UTF8(): Charset {
        return CharsetInitializer.UTF8
    }

    fun ASCII(): Charset {
        return CharsetInitializer.ASCII
    }

    fun fromUtf8(data: ByteArray): String {
        return UTF8().decode(ByteBuffer.wrap(data)).toString()
    }

    fun fromUtf8(data: ByteArray, start: Int, length: Int): String {
        return UTF8().decode(ByteBuffer.wrap(data, start, length)).toString()
    }

    @Throws(IOException::class)
    fun fromUtf8(input: InputStream): String {
        return FileUtil.asString(InputStreamReader(input, UTF8()))
    }

    fun toUtf8(data: String): ByteArray {
        return data.toByteArray(UTF8())
    }

    fun ensureEndsWith(c: Char, s: String?): String? {
        if (s == null) {
            return null
        }
        val len = s.length
        if (len == 0) {
            return s
        }
        var end = len
        while (end > 0 && s[end - 1] == c) {
            --end
        }
        if (end == len) {
            return s + c
        }
        return if (end == len - 1) {
            s
        } else s.substring(0, end + 1)
    }

    ////////////////////////////////////////////////////////////////////////

    val charSequenceComparator: Comparator<CharSequence?>
        get() = CharSequenceComparator.singleton

    val stringComparator: Comparator<String?>
        get() = StringComparator.singleton

    val stringIgnorecaseComparator: Comparator<String?>
        get() = StringIgnorecaseComparator.singleton

    val reversedStringComparator: Comparator<String?>
        get() = ReversedComparator(StringComparator.singleton)

    val reversedStringIgnorecaseComparator: Comparator<String?>
        get() = ReversedComparator(StringIgnorecaseComparator.singleton)

    ////////////////////////////////////////////////////////////////////////

    val BINARY_VALUE_UNIT = arrayOf("", "K", "M", "G", "T")
    val DECIMAL_VALUE_UNIT = arrayOf("", "k", "m", "g", "t")

    ////////////////////////////////////////////////////////////////////////

    fun trimLeading(s: String, c: Char): String {
        val len = s.length
        if (len == 0) {
            return s
        }
        for (i in 0 until len) {
            if (s[i] != c) {
                return if (i == 0) {
                    s
                } else s.substring(i)
            }
        }
        return ""
    }

    fun today(): String {
        return format("%1\$tY%1\$tm%1\$td", System.currentTimeMillis())
    }

    /**
     * @param format eg. "%s.log where %s would be the today string of form 20141231.
     */
    fun today(format: String): String {
        return format(format, today())
    }

    fun now(): String {
        return format("%1\$tY%1\$tm%1\$td-%1\$tH%1\$tM%1\$tS", System.currentTimeMillis())
    }

    fun now(format: String): String {
        return format(format, now())
    }

    fun dateformat(format: String): String {
        return SimpleDateFormat(format).format(Date())
    }

    fun decUnit4(size: Long): Pair<Long, String> {
        return valueUnit(DECIMAL_VALUE_UNIT, 10000, 1000, 555, size)
    }

    fun decUnit4(units: Array<String>, size: Long): Pair<Long, String> {
        return valueUnit(units, 10000, 1000, 555, size)
    }

    fun valueUnit(units: Array<String?>, max: Float, divider: Float, size: Float): Pair<Float, String?> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value = value / divider
            ++unit
        }
        return Pair(if (positive) value else -value, units[unit])
    }

    fun valueUnit(units: Array<String>, max: Long, divider: Long, rouding: Long, size: Long): Pair<Long, String> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value = (value + rouding) / divider
            ++unit
        }
        return Pair(if (positive) value else -value, units[unit])
    }

    /**
     * Convert JSONString in "..\\..." to normal unescaped Java string.
     */
    fun unescJSONString(s: String): String {
        val ret = StringBuilder()
        var i = 1
        val len = s.length - 1
        while (i < len) {
            val c = s[i]
            if (c == '\\' && i + 1 < len) {
                val c1 = s[i + 1]
                if (i + 5 < len && c1 == 'u') {
                    val count = unescJSONString(ret, s, i + 2)
                    if (count > 0) {
                        i += count + 2
                        continue
                    }
                } else if (c1 == '"') {
                    ret.append('"')
                    i += 2
                    continue
                } else if (c1 == 't') {
                    ret.append("\t")
                    i += 2
                    continue
                } else if (c1 == 'n') {
                    ret.append("\n")
                    i += 2
                    continue
                } else if (c1 == 'r') {
                    ret.append("\r")
                    i += 2
                    continue
                }
            }
            ret.append(c)
            ++i
        }
        return ret.toString()
    }

    fun unescJSONString(ret: StringBuilder, s: String, start: Int): Int {
        var v = 0
        var i = 0
        while (i < 4) {
            val c = s[start + i]
            if (c >= '0' && c <= '9') {
                v = (v shl 4) + (c - '0')
                ++i
                continue
            }
            if (c >= 'a' && c <= 'f') {
                v = (v shl 4) + 10 + (c - 'a')
                ++i
                continue
            }
            if (c >= 'A' && c <= 'F') {
                v = (v shl 4) + 10 + (c - 'A')
                ++i
                continue
            }
            if (i == 2) {
                break
            }
            return -1
            ++i
        }
        if (v >= 0x20 && v < 0x7f || v == '\t'.toInt() || v == '\n'.toInt() || v == '\r'.toInt()) {
            ret.append(v.toChar())
            return i
        }
        return -1
    } ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    private object CharsetInitializer {
        var UTF8 = Charset.forName("UTF-8")
        var ASCII = Charset.forName("US-ASCII")
    }

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////

    class ManifestValueScanner(var value: CharSequence) : MutableIterator<String>, Iterable<String?> {
        var length: Int
        var start: Int
        var end: Int
        override fun iterator(): MutableIterator<String> {
            return this
        }

        override fun hasNext(): Boolean {
            return start < length
        }

        override fun next(): String {
            while (end < length) {
                val c = value[end]
                if (c == '"') {
                    skipString('"')
                    continue
                } else if (c == ',') {
                    val ret = value.subSequence(start, end).toString()
                    ++end
                    start = end
                    return ret
                }
                ++end
            }
            val ret = value.subSequence(start, end).toString()
            start = end
            return ret
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

        private fun skipString(delim: Char) {
            ++end
            while (end < length) {
                if (value[end] == delim) {
                    ++end
                    return
                }
                ++end
            }
        }

        init {
            length = value.length
            start = 0
            end = 0
        }
    }

    fun <T> nullFirst(a: T?, b: T?, compare: Fun21<T, T, Int>): Int {
        if (a == null) {
            return if (b == null) 0 else -1
        }
        if (b == null) {
            return 1
        }
        return compare(a, b)
    }

    fun <T> nullLast(a: T?, b: T?, compare: Fun21<T, T, Int>): Int {
        if (a == null) {
            return if (b == null) 0 else 1
        }
        if (b == null) {
            return -1
        }
        return compare(a, b)
    }

    ////////////////////////////////////////////////////////////////////////

    private class CharSequenceComparator : Comparator<CharSequence?>, Serializable {
        override fun compare(a: CharSequence?, b: CharSequence?): Int {
            return nullFirst(a, b) { aa, bb -> TextUtil.compare(aa, bb) }
        }

        companion object {
            val singleton_ = lazy { CharSequenceComparator() }
            val singleton: CharSequenceComparator get() = singleton_.value
        }
    }

    private class StringComparator : Comparator<String?>, Serializable {
        override fun compare(a: String?, b: String?): Int {
            return compareValues(a, b)
        }

        companion object {
            private val singleton_ = lazy { StringComparator() }
            val singleton: StringComparator get() = singleton_.value
        }
    }

    private class StringIgnorecaseComparator : Comparator<String?>, Serializable {
        override fun compare(a: String?, b: String?): Int {
            return nullFirst(a, b) { aa, bb -> aa.compareTo(bb, ignoreCase = true) }
        }

        companion object {
            private val singleton_ = lazy { StringIgnorecaseComparator() }
            val singleton: StringIgnorecaseComparator get() = singleton_.value
        }
    }

    init {
        filepathEscapeChars["~"] = "^7e"
        filepathEscapeChars["!"] = "^21"
        filepathEscapeChars["$"] = "^24"
        filepathEscapeChars["&"] = "^26"
        filepathEscapeChars["*"] = "^2a"
        filepathEscapeChars["("] = "^28"
        filepathEscapeChars[")"] = "^29"
        filepathEscapeChars["["] = "^5b"
        filepathEscapeChars["]"] = "^5d"
        filepathEscapeChars["|"] = "^7c"
        filepathEscapeChars["?"] = "^3f"
        filepathEscapeChars[";"] = "^3b"
        filepathEscapeChars["<"] = "^3c"
        filepathEscapeChars[">"] = "^3e"
        filepathEscapeChars["\""] = "^22"
        filepathEscapeChars["'"] = "^27"
        filepathEscapeChars["^7e"] = "~"
        filepathEscapeChars["^21"] = "!"
        filepathEscapeChars["^24"] = "$"
        filepathEscapeChars["^26"] = "&"
        filepathEscapeChars["^2a"] = "*"
        filepathEscapeChars["^28"] = "("
        filepathEscapeChars["^29"] = ")"
        filepathEscapeChars["^5b"] = "["
        filepathEscapeChars["^5d"] = "]"
        filepathEscapeChars["^7c"] = "|"
        filepathEscapeChars["^3f"] = "?"
        filepathEscapeChars["^3b"] = ";"
        filepathEscapeChars["^3c"] = "<"
        filepathEscapeChars["^3e"] = ">"
        filepathEscapeChars["^22"] = "\""
        filepathEscapeChars["^27"] = "'"
    }
}
