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

import java.io.PrintWriter
import kotlin.experimental.or
import kotlin.math.min

object ArrayUt : ArrayUtil()

open class ArrayUtil {

    fun sprint(
        array: BooleanArray,
        format: String = "%s",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: ByteArray,
        format: String = "%02x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: ShortArray,
        format: String = "%04x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: IntArray,
        format: String = "%08x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: LongArray,
        format: String = "%016x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: FloatArray,
        format: String = "%f",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: DoubleArray,
        format: String = "%f",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun sprint(
        array: CharArray,
        format: String = "%c",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun <T> sprint(
        array: Array<T>,
        format: String = "%s",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): String {
        return print(StringPrintWriter(), array, format, sep, perline, offsetformat).toString()
    }

    fun <W : PrintWriter> print(
        out: W,
        array: BooleanArray,
        format: String = "%s",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: ByteArray,
        format: String = "%02x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: ShortArray,
        format: String = "%04x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: IntArray,
        format: String = "%08x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: LongArray,
        format: String = "%016x",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: FloatArray,
        format: String = "%f",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: DoubleArray,
        format: String = "%f",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter> print(
        out: W,
        array: CharArray,
        format: String = "%c",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun <W : PrintWriter, T> print(
        out: W,
        array: Array<T>,
        format: String = "%s",
        sep: String = ", ",
        perline: Int = 0,
        offsetformat: String = "%08x: "
    ): W {
        array.forEachIndexed { index, b ->
            print1(out, offsetformat, format, sep, perline, index, b)
        }
        return out
    }

    fun toByteArray(a: BooleanArray): ByteArray {
        val bytes = a.size / 8 + (if ((a.size % 8) == 0) 0 else 1)
        var index = 0
        val mask = byteArrayOf(1, 2, 4, 8, 10, 20, 40, 80)
        val ret = ByteArray(bytes) {
            var value: Byte = 0
            for (n in 0 until 8) {
                if (index >= a.size) break
                if (a[index++]) {
                    value = value or mask[n]
                }
            }
            value
        }
        return ret
    }

    private fun <W : PrintWriter, T> print1(
        out: W,
        offsetformat: String,
        format: String,
        sep: String,
        perline: Int,
        index: Int,
        b: T
    ) {
        if (perline > 0 && (index % perline) == 0) {
            if (index > 0) out.println()
            if (offsetformat.isNotEmpty()) out.printf(offsetformat, index)
        } else if (index > 0) out.print(sep)
        out.printf(format, b)
    }

    fun contentEquals(a: Array<CharSequence?>?, b: Array<CharSequence?>?): Boolean {
        if (a == null) {
            if (b != null) return false
        } else if (b == null) {
            return false
        } else {
            if (a.size != b.size) return false
            a.forEachIndexed { index, aa ->
                val bb = b[index]
                if (aa == null) {
                    if (bb != null) return false
                } else if (bb == null) {
                    return false
                } else if (!aa.contentEquals(bb)) return false
            }
        }
        return true
    }

    fun compare(a: ByteArray, b: ByteArray): Int {
        val len = min(a.size, b.size)
        for (index in 0 until len) {
            val aa = a[index]
            val bb = b[index]
            if (aa > bb) return 1
            else if (bb > aa) return -1
        }
        return a.size.compareTo(b.size)
    }
}

/// For efficiency, this comparator consider longer array as larger.
object NotSortingByteArrayComparator : Comparator<ByteArray> {
    override fun compare(a: ByteArray?, b: ByteArray?): Int {
        if (a == null) return if (b == null) 0 else -1
        if (b == null) return 1
        val d = a.size - b.size
        if (d != 0) return d
        for (i in a.indices) {
            val dd = a[i] - b[i]
            if (dd != 0) return dd
        }
        return 0
    }
}
