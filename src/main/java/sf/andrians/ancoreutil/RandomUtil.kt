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
package sf.andrians.ancoreutil

import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.util.*

class RandomUtil(private val random: Random) {
    ////////////////////////////////////////////////////////////////////////
    private object Initializer {
        var singleton = RandomUtil()
    }

    class RandomStream(private val data: ByteArray, private val length: Long) : InputStream() {
        private var count: Long = 0

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (count >= length) {
                return -1
            }
            if (len >= length - count) {
                val n = (length - count).toInt()
                get(b, off, n, count)
                count = length
                return n
            }
            get(b, off, len, count)
            count += len.toLong()
            return len
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            if (count >= length) {
                return -1
            }
            if (b.size >= length - count) {
                val n = (length - count).toInt()
                get(b, 0, n, count)
                count = length
                return n
            }
            get(b, 0, b.size, count)
            count += b.size.toLong()
            return b.size
        }

        @Throws(IOException::class)
        override fun read(): Int {
            if (count >= length) {
                return -1
            }
            val index = (count % data.size).toInt()
            ++count
            return data[index].toInt() and 0xff
        }

        private operator fun get(b: ByteArray, off: Int, len: Int, count: Long) {
            var off = off
            var len = len
            var start = (count % data.size).toInt()
            var n = data.size - start
            while (n < len) {
                System.arraycopy(data, start, b, off, n)
                len -= n
                off += n
                start = 0
                n = data.size
            }
            if (len > 0) {
                System.arraycopy(data, start, b, off, len)
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////
    internal constructor() : this(SecureRandom()) {}

    ////////////////////////////////////////////////////////////////////////
    @get:Synchronized
    val bool: Boolean
        get() = random.nextBoolean()

    @get:Synchronized
    val byte: Byte
        get() {
            val ret = ByteArray(1)
            random.nextBytes(ret)
            return ret[0]
        }

    @get:Synchronized
    val int: Int
        get() = random.nextInt()

    @Synchronized
    fun getInt(max: Int): Int {
        return if (max == 0) {
            0
        } else random.nextInt(max)
    }

    @Synchronized
    fun getInt(min: Int, max: Int): Int {
        return getInt(max - min) + min
    }

    @get:Synchronized
    val long: Long
        get() = random.nextLong()

    @Synchronized
    fun getLong(max: Long): Long {
        if (max == 0L) {
            return 0
        }
        var ret = random.nextLong()
        if (ret == Long.MIN_VALUE) {
            ++ret
        }
        return Math.abs(ret) % max
    }

    @Synchronized
    fun getLong(min: Long, max: Long): Long {
        return getLong(max - min) + min
    }

    @get:Synchronized
    val float: Float
        get() = random.nextFloat()

    @Synchronized
    fun getFloat(max: Float): Float {
        return if (max == 0f) {
            0f
        } else Math.abs(random.nextFloat()) % max
    }

    @Synchronized
    fun getFloat(min: Float, max: Float): Float {
        return getFloat(max - min) + min
    }

    @get:Synchronized
    val double: Double
        get() = random.nextDouble()

    @Synchronized
    fun getDouble(max: Double): Double {
        return if (max == 0.0) {
            0.0
        } else Math.abs(random.nextDouble()) % max
    }

    @Synchronized
    fun getDouble(min: Double, max: Double): Double {
        return getDouble(max - min) + min
    }

    @Synchronized
    fun getUInt(max: Int): Int {
        if (max == 0) {
            return 0
        }
        val ret = random.nextInt(max)
        return if (ret >= 0) ret else -ret
    }

    @Synchronized
    fun getULong(max: Int): Long {
        if (max == 0) {
            return 0
        }
        val ret = random.nextInt(max)
        return if (ret >= 0) ret.toLong() else (-ret).toLong()
    }

    ////////////////////////////////////////////////////////////////////////
    @Synchronized
    operator fun get(ret: DoubleArray): DoubleArray {
        for (i in ret.indices) {
            ret[i] = random.nextDouble()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: FloatArray): FloatArray {
        for (i in ret.indices) {
            ret[i] = random.nextFloat()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: LongArray): LongArray {
        for (i in ret.indices) {
            ret[i] = random.nextLong()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: IntArray): IntArray {
        for (i in ret.indices) {
            ret[i] = random.nextInt()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: CharArray): CharArray {
        for (i in ret.indices) {
            ret[i] = getInt(0x10000).toChar()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: ShortArray): ShortArray {
        for (i in ret.indices) {
            ret[i] = random.nextInt().toShort()
        }
        return ret
    }

    @Synchronized
    operator fun get(ret: ByteArray): ByteArray {
        random.nextBytes(ret)
        return ret
    }

    @Synchronized
    operator fun get(ret: ByteArray?, off: Int, len: Int) {
        val b = ByteArray(len)
        random.nextBytes(b)
        System.arraycopy(b, 0, ret, off, len)
    }

    @Synchronized
    operator fun get(ret: BooleanArray): BooleanArray {
        val retlen = ret.size
        var len = retlen
        if (len % 8 != 0) {
            ++len
        }
        val a = ByteArray(len)
        random.nextBytes(a)
        var i = 0
        var bit = 0
        while (i < len) {
            var n = 7
            while (n >= 0 && bit < retlen) {
                ret[bit++] = a[i].toInt() and (1 shl n) != 0
                --n
            }
            ++i
        }
        return ret
    }

    @Synchronized
    fun getASCII(ret: CharArray): CharArray {
        for (i in ret.indices) {
            ret[i] = aSCII
        }
        return ret
    }

    @Synchronized
    fun getUnsigned(ret: IntArray, max: Int): IntArray {
        for (i in ret.indices) {
            ret[i] = getUInt(max)
        }
        return ret
    }

    @Synchronized
    fun getUnsigned(ret: LongArray, max: Int): LongArray {
        for (i in ret.indices) {
            ret[i] = getULong(max)
        }
        return ret
    }

    ////////////////////////////////////////////////////////////////////////
    @get:Synchronized
    val aSCII: Char
        get() = getInt(' '.toInt(), 'z'.toInt() + 1).toChar()

    @get:Synchronized
    val letter: Char
        get() {
            var c = 0.toChar()
            while (!Character.isLetter(c)) {
                c = aSCII
            }
            return c
        }

    @get:Synchronized
    val digit: Char
        get() {
            var c = 0.toChar()
            while (!Character.isDigit(c)) {
                c = uTFChar
            }
            return c
        }

    @get:Synchronized
    val letterOrDigit: Char
        get() {
            var c = 0.toChar()
            while (!Character.isLetterOrDigit(c)) {
                c = aSCII
            }
            return c
        }

    @get:Synchronized
    val uTFChar: Char
        get() = getInt(0xffff).toChar()

    @get:Synchronized
    val uTFLetter: Char
        get() {
            var c = 0.toChar()
            while (!Character.isLetter(c)) {
                c = uTFChar
            }
            return c
        }

    @get:Synchronized
    val uTFLetterOrDigit: Char
        get() {
            var c = 0.toChar()
            while (!Character.isLetterOrDigit(c)) {
                c = uTFChar
            }
            return c
        }

    ////////////////////////////////////////////////////////////////////////
    @Synchronized
    fun getString(maxlength: Int): String {
        return getString(0, maxlength)
    }

    @Synchronized
    fun getString(minlength: Int, maxlength: Int): String {
        val len = getInt(minlength, maxlength)
        val ret = CharArray(len)
        for (i in 0 until len) {
            ret[i] = aSCII
        }
        return String(ret)
    }

    @Synchronized
    fun getWord(maxlength: Int): String {
        return getWord(0, maxlength)
    }

    @Synchronized
    fun getWord(minlength: Int, maxlength: Int): String {
        val len = getInt(minlength, maxlength)
        val ret = CharArray(len)
        for (i in 0 until len) {
            ret[i] = if (i == 0) letter else letterOrDigit
        }
        return String(ret)
    }

    @Synchronized
    fun getWords(count: Int, maxlength: Int): Array<String?> {
        return getWords(count, 0, maxlength)
    }

    @Synchronized
    fun getWords(count: Int, minlength: Int, maxlength: Int): Array<String?> {
        val ret = arrayOfNulls<String>(count)
        for (i in 0 until count) {
            ret[i] = getWord(minlength, maxlength)
        }
        return ret
    }

    @Synchronized
    fun getUniqueWord(minlength: Int, maxlength: Int, set: Set<String?>): String? {
        var s: String? = null
        do {
            val len = getInt(minlength, maxlength)
            val ret = CharArray(len)
            for (i in 0 until len) {
                ret[i] = if (i == 0) letter else letterOrDigit
            }
            s = String(ret)
        } while (set.contains(s))
        return s
    }

    @Synchronized
    fun getUniqueWords(count: Int, maxlength: Int): Array<String?> {
        return getUniqueWords(count, 0, maxlength)
    }

    @Synchronized
    fun getUniqueWords(count: Int, minlength: Int, maxlength: Int): Array<String?> {
        var count = count
        val ret = arrayOfNulls<String>(count)
        val set: MutableSet<String> = HashSet()
        while (count > 0) {
            val s = getWord(minlength, maxlength)
            if (!set.add(s)) {
                continue
            }
            --count
            ret[count] = s
        }
        return ret
    }

    @Synchronized
    fun getUTFString(maxlength: Int): String {
        return getUTFString(0, maxlength)
    }

    @Synchronized
    fun getUTFString(minlength: Int, maxlength: Int): String {
        val len = getInt(minlength, maxlength)
        val ret = CharArray(len)
        for (i in 0 until len) {
            ret[i] = uTFChar
        }
        return String(ret)
    }

    @Synchronized
    fun getUTFWord(maxlength: Int): String {
        return getUTFWord(0, maxlength)
    }

    @Synchronized
    fun getUTFWord(minlength: Int, maxlength: Int): String {
        val len = getInt(minlength, maxlength)
        val ret = CharArray(len)
        for (i in 0 until len) {
            ret[i] = if (i == 0) uTFLetter else uTFLetterOrDigit
        }
        return String(ret)
    }

    @Synchronized
    fun getUTFWords(count: Int, maxlength: Int): Array<String?> {
        return getUTFWords(count, 0, maxlength)
    }

    @Synchronized
    fun getUTFWords(count: Int, minlength: Int, maxlength: Int): Array<String?> {
        val ret = arrayOfNulls<String>(count)
        for (i in 0 until count) {
            ret[i] = getUTFWord(minlength, maxlength)
        }
        return ret
    }

    @Synchronized
    fun getUniqueUTFWords(count: Int, maxlength: Int): Array<String?> {
        return getUniqueUTFWords(count, 0, maxlength)
    }

    @Synchronized
    fun getUniqueUTFWords(count: Int, minlength: Int, maxlength: Int): Array<String?> {
        var count = count
        val ret = arrayOfNulls<String>(count)
        val set: MutableSet<String> = HashSet()
        while (count > 0) {
            val s = getUTFWord(minlength, maxlength)
            if (!set.add(s)) {
                continue
            }
            --count
            ret[count] = s
        }
        return ret
    }

    /**
     * Generate a random sequence from 0 to size.
     */
    fun randomSequence(size: Int): IntArray {
        val ret = IntArray(size)
        for (i in 0 until size) {
            ret[i] = i
        }
        permutate(ret)
        return ret
    }

    /**
     * Generate a random permutation of given array in place.
     */
    fun permutate(ret: IntArray) {
        var index: Int
        var v: Int
        var i = ret.size
        while (i > 0) {
            index = getInt(i)
            --i
            v = ret[i]
            ret[i] = ret[index]
            ret[index] = v
        }
    }

    /**
     * Generate a random permutation of given list in place.
     */
    fun <T> permutate(ret: MutableList<T>) {
        var v: T
        var index: Int
        var i = ret.size
        while (i > 0) {
            index = getInt(i)
            --i
            v = ret[i]
            ret[i] = ret[index]
            ret[index] = v
        }
    }

    /**
     * Generate a random permutation of given array in place.
     */
    fun permutate(ret: CharArray) {
        var index: Int
        var v: Char
        var i = ret.size
        while (i > 0) {
            index = getInt(i)
            --i
            v = ret[i]
            ret[i] = ret[index]
            ret[index] = v
        }
    }

    /**
     * Generate a random permutation of given array in place.
     */
    fun permutate(ret: Array<Any?>) {
        var index: Int
        var v: Any?
        var i = ret.size
        while (i > 0) {
            index = getInt(i)
            --i
            v = ret[i]
            ret[i] = ret[index]
            ret[index] = v
        }
    }

    fun sleep(min: Long, max: Long) {
        try {
            Thread.sleep(getLong(min, max))
        } catch (e: InterruptedException) {
        }
    } ////////////////////////////////////////////////////////////////////////

    companion object {
        val singleton: RandomUtil
            get() = Initializer.singleton
    }

}
