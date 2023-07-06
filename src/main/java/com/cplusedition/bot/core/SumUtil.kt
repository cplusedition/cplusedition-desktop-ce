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
import java.io.IOException
import java.security.MessageDigest
import java.util.*

object SumUt : SumUtil()

open class SumUtil {

    fun digesterPool(kind: SumKind): ObjectPool<MessageDigest> {
        return ObjectPool { MessageDigest.getInstance(kind.algorithm) }
    }

    fun digest(
        digester: MessageDigest,
        a: ByteArray,
        offset: Int,
        len: Int
    ): ByteArray {
        digester.update(a, offset, len)
        return digester.digest()
    }

    @Throws
    fun digest(
        digester: MessageDigest,
        file: File,
    ): ByteArray {
        digester.reset()
        With.bytes(file) { buf, len ->
            digester.update(buf, 0, len)
        }
        return digester.digest()
    }

    @Throws
    fun digestToHex(
        digester: MessageDigest,
        a: ByteArray,
        offset: Int,
        len: Int
    ): String {
        return Hex.encode(digest(digester, a, offset, len)).toString()
    }

    fun digestToHex(
        digester: MessageDigest,
        file: File,
    ): String {
        val actual = digest(digester, file)
        return Hex.encode(actual).toString()
    }

    @Throws
    fun verify(
        digester: MessageDigest,
        file: File,
        sum: String
    ): Boolean {
        return verify(digester, file, Hex.decode(sum))
    }

    @Throws
    fun verify(
        digester: MessageDigest,
        file: File,
        expected: ByteArray
    ): Boolean {
        val actual = digest(digester, file)
        return expected.contentEquals(actual)
    }

    @Throws(IOException::class)
    fun readlines(sumfile: File, pat: Regex, callback: (MatchResult, String) -> Unit) {
        sumfile.useLines { lines ->
            for (line in lines) {
                if (line.isEmpty()) continue
                val match = pat.matchEntire(line) ?: continue
                callback(match, line)
            }
        }
    }

    @Throws(IOException::class)
    fun readline(line: String, kind: SumKind? = null): SumAndPath? {
        return SumKind.FormatE.match(line)
            ?: SumKind.FormatA.match(line, kind)
            ?: SumKind.FormatB.match(line)
            ?: SumKind.FormatC.match(line)
            ?: SumKind.FormatD.match(line)
    }
}

data class SumAndPath constructor(
    val sum: String,
    val path: String
) {
    fun match(actual: String): Boolean {
        return actual.contentEquals(sum, true)
    }

    fun match(actual: ByteArray): Boolean {
        return actual.contentEquals(Hex.decode(sum))
    }
}

data class ExpectedAndActual constructor(
    val expected: String,
    val actual: String
) {
    fun match(): Boolean {
        return actual.contentEquals(expected, true)
    }

    fun match(actual: ByteArray): Boolean {
        return actual.contentEquals(Hex.decode(expected))
    }
}

enum class SumKind constructor(
    val algorithm: String,
    val bits: Int
) {
    MD5("MD5", 128),
    SHA1("SHA1", 160),
    SHA256("SHA-256", 256),
    SHA512("SHA-512", 512);

    private val lock = Any()

    override fun toString(): String {
        return algorithm
    }

    fun getDigester(): MessageDigest {
        synchronized(lock) {
            var local = digesters.get()
            if (local == null) {
                local = TreeMap()
                digesters.set(local)
            }
            var digester = local[this]
            if (digester == null) {
                digester = MessageDigest.getInstance(algorithm)
                local[this] = digester
                return digester
            }
            digester.reset()
            return digester
        }
    }

    fun isValidHexString(hex: String): Boolean {
        return Hex.decode(hex).size * 8 == bits
    }

    fun digest(a: ByteArray, offset: Int = 0, len: Int = a.size): ByteArray {
        return SumUt.digest(getDigester(), a, offset, len)
    }

    @Throws(IOException::class)
    fun digest(file: File): ByteArray {
        return SumUt.digest(this.getDigester(), file)
    }

    @Throws(IOException::class)
    fun digestToHex(file: File): String {
        return SumUt.digestToHex(this.getDigester(), file)
    }

    fun digestToHex(a: ByteArray, offset: Int = 0, len: Int = a.size): String {
        return SumUt.digestToHex(getDigester(), a, offset, len)
    }

    /**
     * Read a single checksum from the given file.
     */
    @Throws(IOException::class)
    fun readsum(sumfile: File): SumAndPath? {
        return SumUt.readline(sumfile.readText(), this)
    }

    /**
     * Read a single checksum from the given file
     * and calculate the digest of the datafile.
     * @return The expected and actual checksums.
     */
    @Throws(IOException::class)
    fun readsum(sumfile: File, datafile: File): ExpectedAndActual {
        val expected = readsum(sumfile) ?: throw IOException()
        val actual = digestToHex(datafile)
        return ExpectedAndActual(expected.sum.lowercase(), actual)
    }

    /**
     * Read multiple checksums, one per line, from the given file.
     */
    @Throws
    fun readsums(sumfile: File, callback: (SumAndPath?, String) -> Unit) {
        sumfile.useLines { lines ->
            readsums(lines, callback)
        }
    }

    @Throws
    fun readsums(lines: Sequence<String>, callback: (SumAndPath?, String) -> Unit) {
        for (line in lines) {
            if (line.isEmpty()) continue
            callback(SumUt.readline(line, this), line)
        }
    }

    @Throws(IOException::class)
    fun verify(expected: String, datafile: File): Boolean {
        return expected.contentEquals(digestToHex(datafile), true)
    }

    /**
     * @param sumfile Contain a single checksum for the datafile.
     */
    @Throws(IOException::class)
    fun verify(sumfile: File, datafile: File): ExpectedAndActual {
        val ext = Basepath.ext(sumfile.name)
            ?: throw IOException("$sumfile")
        val kindstring = TextUt.toUpperCase(ext)
        Without.throwableOrNull {
            valueOf(kindstring)
        } ?: throw IOException("$sumfile")
        val expected = readsum(sumfile)
            ?: throw IOException("$sumfile")
        val actual = digestToHex(datafile)
        return ExpectedAndActual(expected.sum, actual)
    }

    companion object {
        private val digesters = ThreadLocal<MutableMap<SumKind, MessageDigest>>()

        fun get(name: String): SumKind? {
            return try {
                valueOf(name)
            } catch (e: Throwable) {
                null
            }
        }

        /** @param hexlen Length of hex encoded checksum. */
        fun get(hexlen: Int): SumKind? {
            return when (hexlen) {
                32 -> MD5
                40 -> SHA1
                64 -> SHA256
                128 -> SHA512
                else -> null
            }
        }
    }

    object FormatA {
        private val format = Regex("(?s)^\\s*(\\w+)\\s*\\((.*)\\)\\s*= (\\S.*?)\\s*$")
        fun match(line: String, kind: SumKind?): SumAndPath? {
            val m = format.matchEntire(line) ?: return null
            val sumkind = TextUt.toUpperCase(m.groupValues[1])
            if (kind != null && kind != get(sumkind)) {
                throw IOException("Checksum kind mismatch: expected=kind, actual=$sumkind")
            }
            return SumAndPath(m.groupValues[3].lowercase(), m.groupValues[2])
        }
    }

    object FormatB {
        private val format = Regex("(?s)^\\s*\\(?(.*?)\\)?\\s*=\\s+(\\S.*?)\\s*$")
        fun match(line: String): SumAndPath? {
            val m = format.matchEntire(line) ?: return null
            return SumAndPath(m.groupValues[2].lowercase(), m.groupValues[1])
        }
    }

    object FormatC {
        private val format = Regex("(?s)^\\s*([0-9A-Fa-f]+)(?:\\s\\*|\\*\\s|\\s\\s)(.*?)\\s*$")
        fun match(line: String): SumAndPath? {
            val m = format.matchEntire(line) ?: return null
            return SumAndPath(m.groupValues[1].lowercase(), m.groupValues[2])
        }
    }

    object FormatD {
        private val format = Regex("(?s)^\\s*([0-9A-Fa-f]+)(?:\\s+(\\S.*?))?\\s*$")
        fun match(line: String): SumAndPath? {
            val m = format.matchEntire(line) ?: return null
            return SumAndPath(m.groupValues[1].lowercase(), m.groupValues[2])
        }
    }

    object FormatE {
        private val format = Regex("(?s)^\\s*([0-9A-Fa-f]+)\\s*$")
        fun match(line: String): SumAndPath? {
            val m = format.matchEntire(line) ?: return null
            return SumAndPath(m.groupValues[1].lowercase(), "")
        }
    }
}
