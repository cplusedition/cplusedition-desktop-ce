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
import java.io.PrintWriter
import java.nio.ByteBuffer
import java.util.*

object TextUt : TextUtil()

val LS = System.getProperty("line.separator")!!

open class TextUtil {

    val DEC_SIZE_UNIT = arrayOf("", "k", "m", "g", "t")
    val SIZE_UNIT = arrayOf("", "K", "M", "G", "T")

    fun isNullOrEmpty(s: CharSequence?): Boolean {
        return s.isNullOrEmpty()
    }

    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        return StructUt.nullableEquals(a, b) X@{ aa, bb ->
            if (aa.length != bb.length) return@X false
            for (index in 0 until aa.length) {
                if (aa[index] != bb[index]) return@X false
            }
            true
        }
    }

    fun indexOf(c: Char, s: CharSequence, start: Int, end: Int): Int {
        return s.subSequence(start, end).indexOf(c)
    }

    fun <V : Comparable<V>> compareComparable(a: V?, b: V?): Int {
        return if (a == null) {
            if (b == null) 0 else -1
        } else {
            if (b == null) 1 else a.compareTo(b)
        }
    }

    fun <V : Comparable<V>> compareIterable(a: Iterable<V?>?, b: Iterable<V?>?): Int {
        return if (a == null) {
            if (b == null) 0
            else -1
        } else if (b == null) {
            1
        } else {
            val ai = a.iterator()
            val bi = b.iterator()
            while (ai.hasNext() && bi.hasNext()) {
                val aa = ai.next()
                val bb = bi.next()
                val ret: Int = this.compareComparable(aa, bb)
                if (ret != 0)
                    return ret
            }
            if (ai.hasNext() == bi.hasNext()) 0
            else if (ai.hasNext()) 1
            else -1
        }
    }

    /// Check if haystack contains all characters in needle in order but not neccessarily contingeous.
    /// eg. fuzzyMatch("abc", "a b d c" returns true.
    /// eg. fuzzyMatch("abc", "abd" returns false.
    fun fuzzyMatch(needle: String, haystack: String): Boolean {
        var start = 0
        val length = haystack.length
        for (c in needle) {
            if (start > length)
                return false
            val index = haystack.indexOf(c, start)
            if (index < 0)
                return false
            start = index + 1
        }
        return true
    }

    /**
     * @return Values with 4 or less digits, 1000 as divider, rounding up.
     */
    fun decUnit4(size: Int): Pair<Int, String> {
        return valueUnit(DEC_SIZE_UNIT, 10000, 1000, 500, size)
    }

    /** Like decUnit4(size) but return a String like 100M instead of a Pair. */
    fun decUnit4String(size: Int, suffix: String = "", sep: String = " "): String {
        val sizeunit = decUnit4(size)
        val s = sizeunit.second + suffix
        return if (s.isEmpty()) "${sizeunit.first}" else "${sizeunit.first}$sep$s"
    }

    /**
     * @return Values with 4 or less digits, 1024 as divider, rounding up.
     */
    fun hexUnit4(size: Int): Pair<Int, String> {
        return valueUnit(SIZE_UNIT, 10000, 1024, 512, size)
    }

    /** Like decUnit4(size) but return a String instead of a Pair. */
    fun hexUnit4String(size: Int, suffix: String = "", sep: String = " "): String {
        val sizeunit = hexUnit4(size)
        val s = sizeunit.second + suffix
        return if (s.isEmpty()) "${sizeunit.first}" else "${sizeunit.first}$sep$s"
    }

    /**
     * @return Values with 4 or less digits, 1000 as divider, rounding up.
     */
    fun decUnit4(size: Long): Pair<Long, String> {
        return valueUnit(DEC_SIZE_UNIT, 10000, 1000, 500, size)
    }

    /** Like decUnit4(size) but return a String like 100M instead of a Pair. */
    fun decUnit4String(size: Long, suffix: String = "", sep: String = " "): String {
        val sizeunit = decUnit4(size)
        val s = sizeunit.second + suffix
        return if (s.isEmpty()) "${sizeunit.first}" else "${sizeunit.first}$sep$s"
    }

    /**
     * @return Values with 4 or less digits, 1024 as divider, rounding up.
     */
    fun hexUnit4(size: Long): Pair<Long, String> {
        return valueUnit(SIZE_UNIT, 10000, 1024, 512, size)
    }

    /** Like decUnit4(size) but return a String instead of a Pair. */
    fun hexUnit4String(size: Long, suffix: String = "", sep: String = " "): String {
        val sizeunit = hexUnit4(size)
        val s = sizeunit.second + suffix
        return if (s.isEmpty()) "${sizeunit.first}" else "${sizeunit.first}$sep$s"
    }

    /**
     * @return Values with 4 or less digits, 1024 as divider, unix style rounding up.
     */
    fun fileHexUnit4(size: Long): Pair<Long, String> {
        return valueUnit(SIZE_UNIT, 10000, 1024, 1023, size)
    }

    /** Like fileHexUnit4(size) but return a String instead of a Pair. */
    fun fileHexUnit4String(size: Long, suffix: String = "", sep: String = " "): String {
        val sizeunit = fileHexUnit4(size)
        val s = sizeunit.second + suffix
        return if (s.isEmpty()) "${sizeunit.first}" else "${sizeunit.first}$sep$s"
    }

    fun fileHexUnit4String(file: File): String {
        return fileHexUnit4String(file.length(), "B")
    }

    /**
     * @return Value, unit Pair where value is less than max, eg. return at most 4 digits if max is 10000.
     */
    fun valueUnit(units: Array<String>, max: Double, divider: Double, size: Double): Pair<Double, String> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value /= divider
            ++unit
        }
        value = if (positive) value else -value
        return Pair(value, units[unit])
    }

    /**
     * @return Value, unit Pair where value is less than max, eg. return at most 4 digits if max is 10000.
     */
    fun valueUnit(units: Array<String>, max: Float, divider: Float, size: Float): Pair<Float, String> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value /= divider
            ++unit
        }
        value = if (positive) value else -value
        return Pair(value, units[unit])
    }

    /**
     * @return Value, unit Pair where value is less than max, eg. return at most 4 digits if max is 10000.
     */
    fun valueUnit(units: Array<String>, max: Long, divider: Long, rounding: Long, size: Long): Pair<Long, String> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value = (value + rounding) / divider
            ++unit
        }
        value = if (positive) value else -value
        return Pair(value, units[unit])
    }

    /**
     * @return Value, unit Pair where value is less than max, eg. return at most 4 digits if max is 10000.
     */
    fun valueUnit(units: Array<String>, max: Int, divider: Int, rounding: Int, size: Int): Pair<Int, String> {
        val positive = size >= 0
        var value = if (positive) size else -size
        var unit = 0
        val len = units.size
        while (unit < len - 1) {
            if (value < max) {
                break
            }
            value = (value + rounding) / divider
            ++unit
        }
        value = if (positive) value else -value
        return Pair(value, units[unit])
    }

    /**
     * Split at the first occurence of the sep.
     *
     * @return (s, null) if sep not found.
     */
    fun split2(sep: String, s: String): Pair<String, String?> {
        val index = s.indexOf(sep)
        if (index < 0) return Pair(s, null)
        return Pair(s.substring(0, index), s.substring(index + sep.length))
    }

    /**
     * Split using StringTokenizer, ignore empty segments. Example: split("/", "a//b/")
     * returns ("a", "b")
     */
    fun split(delimiters: String, s: String, transform: IStringTransformer): List<String> {
        val t = StringTokenizer(s, delimiters)
        val ret = ArrayList<String>()
        while (t.hasMoreTokens()) {
            val value = transform(t.nextToken())
            if (value != null) ret.add(value)
        }
        return ret
    }

    fun splitAt(s: String, index: Int): Pair<String, String?> {
        return if (index >= 0) Pair(s.substring(0, index), s.substring(index)) else Pair(s, null)
    }

    /** Format using ROOT locale, ie. locale independent. */
    fun format(format: String, vararg args: Any?): String {
        return String.format(Locale.ROOT, format, *args)
    }

    /** String.toLowerCase() using ROOT locale, ie. locale independent. */
    fun toLowerCase(value: String, locale: Locale = Locale.ROOT): String {
        return value.lowercase(locale)
    }

    /** String.toUpperCase() using ROOT locale, ie. locale independent. */
    fun toUpperCase(value: String, locale: Locale = Locale.ROOT): String {
        return value.uppercase(locale)
    }

    /** String.toLowerCase() using ROOT locale, ie. locale independent. */
    fun toLowerCaseOrNull(value: String?, locale: Locale = Locale.ROOT): String? {
        return value?.lowercase(locale)
    }

    /** String.toUpperCase() using ROOT locale, ie. locale independent. */
    fun toUpperCaseOrNull(value: String?, locale: Locale = Locale.ROOT): String? {
        return value?.uppercase(locale)
    }

    fun capitalCase(value: String, locale: Locale = Locale.ROOT): String {
        return if (value.length > 0) value.substring(0, 1).uppercase(locale) + value.substring(1) else value
    }

    /** String.toLowerCase() using default locale. */
    fun localeLowerCase(value: String): String {
        return value.lowercase(Locale.getDefault())
    }

    /** String.toUpperCase() using default locale. */
    fun localeUpperCase(value: String): String {
        return value.uppercase(Locale.getDefault())
    }

    /** String.toLowerCase() using default locale. */
    fun localeLowerCaseOrNull(value: String?): String? {
        return value?.lowercase(Locale.getDefault())
    }

    /** String.toUpperCase() using default locale. */
    fun localeUpperCaseOrNull(value: String?): String? {
        return value?.uppercase(Locale.getDefault())
    }

    fun parseInt(value: String?, def: Int): Int {
        try {
            return value?.toInt() ?: def
        } catch (e: Throwable) {
            return def
        }
    }

    fun parseHex(value: String?, def: Int): Int {
        try {
            return value?.toInt(16) ?: def
        } catch (e: Throwable) {
            return def
        }
    }

    fun parseLong(value: String?, def: Long): Long {
        try {
            return value?.toLong() ?: def
        } catch (e: Throwable) {
            return def
        }
    }

    fun parseFloat(value: String?, def: Float): Float {
        try {
            return value?.toFloat() ?: def
        } catch (e: Throwable) {
            return def
        }
    }

    fun parseDouble(value: String?, def: Double): Double {
        try {
            return value?.toDouble() ?: def
        } catch (e: Throwable) {
            return def
        }
    }

    fun parseInt(value: String?): Int? {
        try {
            return value?.toInt()
        } catch (e: Throwable) {
            return null
        }
    }

    fun parseLong(value: String?): Long? {
        try {
            return value?.toLong()
        } catch (e: Throwable) {
            return null
        }
    }

    fun parseFloat(value: String?): Float? {
        try {
            return value?.toFloat()
        } catch (e: Throwable) {
            return null
        }
    }

    fun parseDouble(value: String?): Double? {
        try {
            return value?.toDouble()
        } catch (e: Throwable) {
            return null
        }
    }

    fun quote(s: CharSequence): String {
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

    fun q(s: CharSequence): String {
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

    /// Escape the non-ascii chars in the given string with \x or \unnnn.
    fun escString(s: CharSequence): String {
        val ret = StringBuilder()
        for (c in s) {
            val cc = when (c) {
                '\n' -> "\\n"
                '\r' -> "\\r"
                '\t' -> "\\t"
                '\'' -> "\\\'"
                '\"' -> "\\\""
                '\\' -> "\\\\"
                '\b' -> "\\b"
                '\u000c' -> "\\f"
                else -> {
                    val n = c.code
                    if (n < 0x20 || n >= 0x7f) {
                        "\\u" + Integer.toHexString(n).padStart(4, '0')
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
     * Escape the occurences of given chars in the given string with \.
     */
    fun escString(s: CharSequence, chars: String): String {
        val ret = StringBuilder()
        for (c in s) {
            if (chars.indexOf(c) >= 0) {
                ret.append('\\')
            }
            ret.append(c)
        }
        return ret.toString()
    }

    /**
     * @param start Start offset for column 1.
     * @param end   End offfset whose column number is to be determined.
     * @return 1-based column number of 'end'.
     */
    fun columnOf(str: CharSequence, start: Int, end: Int, tabwidth: Int): Int {
        var start1 = start
        var ret = 0
        var c: Char
        while (start1 < end) {
            c = str[start1]
            if (c == '\r' || c == '\n') {
                ret = 0
            } else if (c == '\t') {
                ret = (ret / tabwidth + 1) * tabwidth
            } else {
                ++ret
            }
            ++start1
        }
        return ret + 1
    }

    fun stringOf(c: Char, len: Int): String {
        return when (len) {
            0 -> ""
            1 -> c.toString()
            else -> c.toString().padEnd(len, c)
        }
    }
}

object Hex {
    const val LOWER = "0123456789abcdef"
    const val UPPER = "0123456789ABCDEF"

    @Throws(NumberFormatException::class)
    fun decodeOrNull(hex: CharSequence): ByteArray? {
        val len = hex.length
        if (len.isOdd()) return null
        var i = 0
        var o = 0
        val ret = ByteArray(len / 2)
        while (i < len) {
            ret[o++] = ((decode(hex[i++]) shl 4) + decode(hex[i++])).toByte()
        }
        return ret
    }

    fun decode(hex: CharSequence): ByteArray {
        return decodeOrNull(hex) ?: throw NumberFormatException(hex.toString())
    }

    fun encode(bytes: ByteArray, uppercase: Boolean = false): CharSequence {
        val ret = StringBuilder()
        val hex = if (uppercase) UPPER else LOWER
        for (b in bytes) {
            val n = b.toInt() and 0xff
            ret.append(hex[n ushr 4])
            ret.append(hex[n and 0x0f])
        }
        return ret
    }

    fun toString(array: ByteArray): String {
        val ret = StringBuilder()
        for (b in array) {
            if (ret.isNotEmpty()) ret.append(", ")
            val n = b.toInt() and 0xff
            ret.append("0x" + (n.toString(16).padStart(2, '0')))
        }
        return ret.toString()
    }

    @Throws(NumberFormatException::class)
    fun decode(c: Char): Int {
        val n = c.code
        return if (n in 0x61..0x66) 10 + n - 0x61
        else if (n in 0x41..0x46) 10 + n - 0x41
        else if (n in 0x30..0x39) n - 0x30
        else throw NumberFormatException(n.toString())
    }

    fun dump(valueformat: String, a: ByteArray, start: Int = 0, end: Int = a.size): String {
        return HexDumper(a, start, end - start).offsetFormat("").valueFormat(valueformat).dump()
    }

    fun dump(a: ByteArray, start: Int = 0, end: Int = a.size): String {
        return HexDumper(a, start, end - start).dump()
    }

    fun dump(
        out: PrintWriter,
        a: ByteArray,
        start: Int = 0,
        end: Int = a.size,
        offsetformat: String = "%08x: ",
        valueformat: String = "%02x ",
        perline: Int = 16
    ): PrintWriter {
        return HexDumper(a, start, end - start)
            .offsetFormat(offsetformat)
            .valueFormat(valueformat)
            .perline(perline)
            .dump(out)
    }
}

class HexDumper constructor(
    private val bytes: ByteBuffer,
) {
    private var offsetformat: String = "%08x: "
    private var valueformat: String = "%02x "
    private var perline: Int = 16
    private var startOffset = 0

    constructor(b: ByteArray, off: Int = 0, len: Int = b.size) : this(ByteBuffer.wrap(b, off, len))

    fun offsetFormat(format: String): HexDumper {
        this.offsetformat = format
        return this
    }

    fun valueFormat(format: String): HexDumper {
        this.valueformat = format
        return this
    }

    fun perline(n: Int): HexDumper {
        this.perline = n
        return this
    }

    fun startOffset(off: Int): HexDumper {
        this.startOffset = off
        return this
    }

    fun startOffset(off: Long): HexDumper {
        if (off >= Integer.MAX_VALUE)
            throw AssertionError()
        this.startOffset = off.toInt()
        return this
    }

    fun dump(): String {
        return dump(StringPrintWriter()).toString()
    }

    fun dump(out: PrintWriter): PrintWriter {
        val start = bytes.position()
        val len = bytes.remaining()
        val end = bytes.limit()
        if (len == 0) {
            out.println()
            out.flush()
            return out
        }
        var xstart = (startOffset + start) / perline * perline
        val xend = ((startOffset + end - 1) / perline + 1) * perline
        while (xstart < xend) {
            if (offsetformat.isNotEmpty())
                out.print(TextUt.format(offsetformat, xstart))
            for (i in 0 until perline) {
                if (i == perline / 2) out.print(' ')
                val index = xstart + i
                if (index in start until end) {
                    out.print(TextUt.format(valueformat, bytes[index]))
                } else {
                    out.print("   ")
                }
            }
            out.print(" |")
            for (i in 0 until perline) {
                if (i == perline / 2) out.print(" ")
                val index = xstart + i
                if (index in start until end) {
                    val c: Int = bytes[index].toInt() and 0xff
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
            xstart += perline
        }
        out.flush()
        return out
    }
}

/// For efficiency, this comparator consider longer CharSequence as larger.
object NotSortingCharSequenceComparator : Comparator<CharSequence> {
    override fun compare(a: CharSequence?, b: CharSequence?): Int {
        if (a == null) return if (b == null) 0 else -1
        if (b == null) return 1
        val d = a.length - b.length
        if (d != 0) return d
        for (i in a.indices) {
            val dd = a[i] - b[i]
            if (dd != 0) return dd
        }
        return 0
    }
}
