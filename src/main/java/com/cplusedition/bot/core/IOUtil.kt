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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.CharArrayReader
import java.io.CharArrayWriter
import java.io.EOFException
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.io.Reader
import java.io.StringWriter
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.channels.ByteChannel
import java.nio.charset.Charset

interface IPositionTracking {
    fun getPosition(): Long
}

abstract class PositionTrackingOutputStream : OutputStream(), IPositionTracking

open class PositionTrackingOutputStreamAdapter(private val output: OutputStream) : PositionTrackingOutputStream() {

    private var position = 0L

    override fun getPosition(): Long {
        return position
    }

    override fun write(b: Int) {
        val oposition = position
        output.write(b)
        position = oposition + 1
    }

    override fun write(b: ByteArray) {
        val oposition = position
        output.write(b, 0, b.size)
        position = oposition + b.size
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val oposition = position
        output.write(b, off, len)
        position = oposition + len
    }
}

open class StringPrintStream : PrintStream {
    private val myout: ByteArrayOutputStream

    constructor() : super(ByteArrayOutputStream()) {
        myout = out as ByteArrayOutputStream
    }

    constructor(charset: String) : super(ByteArrayOutputStream(), false, charset) {
        myout = out as ByteArrayOutputStream
    }

    fun toByteArray(): ByteArray {
        return myout.toByteArray()
    }

    @Throws(UnsupportedEncodingException::class)
    fun toString(charset: String): String {
        return myout.toString(charset)
    }

    override fun toString(): String {
        return myout.toString()
    }
}

open class StringPrintWriter : PrintWriter, CharSequence {

    private val stringWriter: StringWriter

    val buffer: StringBuffer
        get() = stringWriter.buffer
    override val length: Int get() = stringWriter.buffer.length

    constructor() : this(StringWriter())

    constructor(initsize: Int) : this(StringWriter(initsize))

    constructor(w: StringWriter) : super(w) {
        stringWriter = w
    }

    override operator fun get(index: Int): Char {
        return stringWriter.buffer[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return stringWriter.buffer.subSequence(startIndex, endIndex)
    }

    fun print(msgs: Iterator<String>) {
        if (msgs.hasNext()) print(msgs.bot.join(""))
    }

    fun print(msgs: Iterable<String>) {
        print(msgs.iterator())
    }

    fun print(msgs: Sequence<String>) {
        print(msgs.iterator())
    }

    fun println(msgs: Iterator<String>) {
        if (msgs.hasNext()) println(msgs.bot.joinln())
    }

    fun println(msgs: Iterable<String>) {
        println(msgs.iterator())
    }

    fun println(msgs: Sequence<String>) {
        println(msgs.iterator())
    }

    override fun toString(): String {
        return stringWriter.toString()
    }
}

////////////////////////////////////////////////////////////////////////

open class NumberReader(
    var b: ByteArray
) {

    constructor() : this(ByteArray(8))

    private fun ishl(offset: Int, shift: Int): Int {
        return b[offset].toInt() shl shift
    }

    private fun u8shl(offset: Int, shift: Int): Int {
        return readU8(offset) shl shift
    }

    private fun lu8shl(offset: Int, shift: Int): Long {
        return readLong8(offset) shl shift
    }

    private fun readU8(offset: Int): Int {
        return (b[offset].toInt() and 0xff)
    }

    private fun readLong8(offset: Int): Long {
        return (b[offset].toLong() and 0xffL)
    }

    private fun readU32BE(offset: Int): Long {
        return (lu8shl(offset, 24)
                or lu8shl(offset + 1, 16)
                or lu8shl(offset + 2, 8)
                or readLong8(offset + 3))
    }

    private fun readU32LE(offset: Int): Long {
        return (readLong8(offset)
                or lu8shl(offset + 1, 8)
                or lu8shl(offset + 2, 16)
                or lu8shl(offset + 3, 24))
    }

    fun readByte(): Byte {
        return b[0]
    }

    fun read8(): Int {
        return b[0].toInt()
    }

    fun readU8(): Int {
        return readU8(0)
    }

    fun read16BE(): Int {
        return (ishl(0, 8) or readU8(1))
    }

    fun read16LE(): Int {
        return (readU8(0) or ishl(1, 8))
    }

    fun readU16BE(): Int {
        return (read16BE() and 0xffff)
    }

    fun readU16LE(): Int {
        return (read16LE() and 0xffff)
    }

    fun read32BE(): Int {
        return (ishl(0, 24)
                or u8shl(1, 16)
                or u8shl(2, 8)
                or readU8(3))
    }

    fun read32LE(): Int {
        return (readU8(0)
                or u8shl(1, 8)
                or u8shl(2, 16)
                or ishl(3, 24))
    }

    fun readU32BE(): Long {
        return readU32BE(0)
    }

    fun readU32LE(): Long {
        return readU32LE(0)
    }

    fun read64BE(): Long {
        return ((readU32BE(0) shl 32) or readU32BE(4))
    }

    fun read64LE(): Long {
        return (readU32LE(0) or (readU32LE(4) shl 32))
    }
}

////////////////////////////////////////////////////////////////////

open class ByteReader(
    val input: InputStream
) {

    constructor(array: ByteArray) : this(ByteArrayInputStream(array))

    var buf = ByteArray(8)
    var b = NumberReader(buf)

    private fun fully(len: Int): NumberReader {
        IOUt.readFully(input, buf, 0, len)
        return b
    }

    @Throws(IOException::class)
    fun read(): Int {
        val b = input.read()
        if (b < 0) {
            throw EOFException()
        }
        return b
    }

    @Throws(IOException::class)
    fun read(ret: ByteArray): ByteArray {
        IOUt.readFully(input, ret)
        return ret
    }

    @Throws(IOException::class)
    fun read(ret: ByteArray, off: Int, len: Int): ByteArray {
        IOUt.readFully(input, ret, off, len)
        return ret
    }

    fun readByte(): Byte {
        return read().toByte()
    }

    @Throws(IOException::class)
    fun read8(): Int {
        return read().toByte().toInt()
    }

    @Throws(IOException::class)
    fun readU8(): Int {
        return read()
    }

    @Throws(IOException::class)
    fun read16BE(): Int {
        return fully(2).read16BE()
    }

    @Throws(IOException::class)
    fun read16LE(): Int {
        return fully(2).read16LE()
    }

    @Throws(IOException::class)
    fun readU16BE(): Int {
        return fully(2).readU16BE()
    }

    @Throws(IOException::class)
    fun readU16LE(): Int {
        return fully(2).readU16LE()
    }

    @Throws(IOException::class)
    fun read32BE(): Int {
        return fully(4).read32BE()
    }

    @Throws(IOException::class)
    fun read32LE(): Int {
        return fully(4).read32LE()
    }

    @Throws(IOException::class)
    fun readU32BE(): Long {
        return fully(4).readU32BE()
    }

    @Throws(IOException::class)
    fun readU32LE(): Long {
        return fully(4).readU32LE()
    }

    @Throws(IOException::class)
    fun read64BE(): Long {
        return fully(8).read64BE()
    }

    @Throws(IOException::class)
    fun read64LE(): Long {
        return fully(8).read64LE()
    }

    @Deprecated("Use read63UV() instead", ReplaceWith("read63UV()"))
    fun read64UV(): Long {
        return read63UV()
    }

    fun read63UV(): Long {
        var ret = 0L
        var shift = 0
        while (true) {
            val v = read()
            if (v < 0)
                throw IOException()
            ret += ((v and 0x7f).toLong() shl shift)
            if ((v and 0x80) == 0x80) break
            shift += 7
        }
        return ret
    }

    @Throws(IOException::class)
    fun readUtf8(len: Int): CharSequence {
        val b = ByteArray(len)
        IOUt.readFully(input, b)
        return IOUt.UTF_8.decode(ByteBuffer.wrap(b))
    }

    @Throws(IOException::class)
    fun read32BEUtf8(limit: Int = Int.MAX_VALUE): CharSequence {
        val len = read32BE()
        if (len > limit) throw IOException()
        return readUtf8(len)
    }

    @Throws(IOException::class)
    fun readU8Bytes(limit: Int = 255): ByteArray {
        return IOUt.readU8Bytes(input, limit)
    }

    @Throws(IOException::class)
    fun read32BEBytes(limit: Int = Int.MAX_VALUE): ByteArray {
        val len = read32BE()
        if (len > limit) throw IOException()
        return read(ByteArray(len))
    }

    ////////////////////////////////////////////////////////////////////////

}

open class NumberWriter(
    val b: ByteArray
) {
    constructor() : this(ByteArray(8))

    private fun shr(value: Int, shift: Int): Byte {
        return (value shr shift).toByte()
    }

    private fun shr(value: Long, shift: Int): Byte {
        return (value shr shift).toByte()
    }

    fun write16BE(value: Int) {
        b[0] = shr(value, 8)
        b[1] = value.toByte()
    }

    fun write16LE(value: Int) {
        b[0] = value.toByte()
        b[1] = shr(value, 8)
    }

    fun write32BE(value: Int) {
        b[0] = shr(value, 24)
        b[1] = shr(value, 16)
        b[2] = shr(value, 8)
        b[3] = value.toByte()
    }

    fun write32LE(value: Int) {
        b[0] = value.toByte()
        b[1] = shr(value, 8)
        b[2] = shr(value, 16)
        b[3] = shr(value, 24)
    }

    fun write64BE(value: Long) {
        b[0] = shr(value, 56)
        b[1] = shr(value, 48)
        b[2] = shr(value, 40)
        b[3] = shr(value, 32)
        b[4] = shr(value, 24)
        b[5] = shr(value, 16)
        b[6] = shr(value, 8)
        b[7] = value.toByte()
    }

    fun write64LE(value: Long) {
        b[0] = value.toByte()
        b[1] = shr(value, 8)
        b[2] = shr(value, 16)
        b[3] = shr(value, 24)
        b[4] = shr(value, 32)
        b[5] = shr(value, 40)
        b[6] = shr(value, 48)
        b[7] = shr(value, 56)
    }
}

////////////////////////////////////////////////////////////////////////

open class ByteWriter(
    val out: OutputStream
) {
    val b = ByteArray(8)

    @Throws(IOException::class)
    fun write(b: Byte): ByteWriter {
        out.write(b.toInt())
        return this
    }

    @Throws(IOException::class)
    fun write(b: Int): ByteWriter {
        out.write(b)
        return this
    }

    @Throws(IOException::class)
    fun writeU8(b: Int): ByteWriter {
        if (b < 0 || b > 0xff) throw IOException()
        return write(b)
    }

    @Throws(IOException::class)
    fun write(a: ByteArray): ByteWriter {
        out.write(a, 0, a.size)
        return this
    }

    @Throws(IOException::class)
    fun write(a: ByteArray, start: Int, len: Int): ByteWriter {
        out.write(a, start, len)
        return this
    }

    @Throws(IOException::class)
    fun write16BE(value: Int): ByteWriter {
        IOUt.write16BE(b, 0, value)
        out.write(b, 0, 2)
        return this
    }

    @Throws(IOException::class)
    fun write16LE(value: Int): ByteWriter {
        IOUt.write16LE(b, 0, value)
        out.write(b, 0, 2)
        return this
    }

    @Throws(IOException::class)
    fun write32BE(value: Int): ByteWriter {
        IOUt.write32BE(b, 0, value)
        out.write(b, 0, 4)
        return this
    }

    @Throws(IOException::class)
    fun write32LE(value: Int): ByteWriter {
        IOUt.write32LE(b, 0, value)
        out.write(b, 0, 4)
        return this
    }

    @Throws(IOException::class)
    fun write64BE(value: Long): ByteWriter {
        IOUt.write64BE(b, 0, value)
        out.write(b, 0, 8)
        return this
    }

    @Throws(IOException::class)
    fun write64LE(value: Long): ByteWriter {
        IOUt.write64LE(b, 0, value)
        out.write(b, 0, 8)
        return this
    }

    @Throws(IOException::class)
    @Deprecated("Use write63UV(value) instead", ReplaceWith("write63UV((value))"))
    fun write64UV(value: Long) {
        return write63UV((value))
    }

    @Throws(IOException::class)
    fun write63UV(value: Long) {
        if (value < 0)
            throw IOException()
        var v = value
        while (true) {
            val lsb = (v and 0x7f).toInt()
            v = (v ushr 7)
            if (v == 0L) {
                write(lsb or 0x80)
                return
            }
            write(lsb)
        }
    }

    @Throws(IOException::class)
    fun writeU8Bytes(a: ByteArray, off: Int = 0, len: Int = a.size): ByteWriter {
        IOUt.writeU8Bytes(out, a, off, len)
        return this
    }

    @Throws(IOException::class)
    fun write32BEBytes(a: ByteArray, off: Int = 0, len: Int = a.size): ByteWriter {
        if (len < 0) throw IOException()
        write32BE(len)
        return write(a, off, len)
    }
}

////////////////////////////////////////////////////////////////////////

object IOUt : IOUtil()

open class IOUtil {

    val BUFSIZE = DEFAULT_BUFFER_SIZE
    val isNativeBigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
    val UTF_8 = Charset.forName("UTF-8")

    ////////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun readFully(input: InputStream, a: ByteArray, offset: Int = 0, length: Int = a.size): ByteArray {
        readFully(offset, length) { off, remaining ->
            input.read(a, off, remaining)
        }
        return a
    }

    @Throws(IOException::class)
    fun readFully(input: ByteChannel, a: ByteArray, offset: Int = 0, length: Int = a.size): ByteArray {
        readFully(offset, length) { off, remaining ->
            input.read(ByteBuffer.wrap(a, off, remaining))
        }
        return a
    }

    @Throws(IOException::class)
    fun readFully(reader: Reader, a: CharArray, offset: Int = 0, length: Int = a.size) {
        readFully(offset, length) { off, remaining ->
            reader.read(a, off, remaining)
        }
    }

    @Throws(IOException::class)
    fun skipFully(input: InputStream, length: Long) {
        var remaining = length
        //// HACK: Default implementation should has try to skip as much as possible before return.
        //// But just in case some implementation do not loop.
        var n: Long
        while (remaining > 0) {
            n = input.skip(remaining)
            if (n <= 0) {
                throw EOFException("Expected=$length, actual=${length - remaining}")
            }
            remaining -= n
        }
    }

    fun readFully(offset: Int, length: Int, reader: Fun21<Int, Int, Int>) {
        var off = offset
        var remaining = length
        while (remaining > 0) {
            val n = reader(off, remaining)
            if (n < 0) {
                throw EOFException("Expected=$length, actual=${off - offset}")
            }
            remaining -= n
            off += n
        }
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached.
     * @return number of bytes read, possibly 0 if EOF is reached, but never -1.
     */
    @Throws(IOException::class)
    fun readWhilePossible(input: InputStream, a: ByteArray, offset: Int = 0, length: Int = a.size): Int {
        return readWhile(offset, a.size, length) { off, len ->
            input.read(a, off, len)
        }
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached.
     * @return number of bytes read, possibly 0 if EOF is reached, but never -1.
     */
    @Throws(IOException::class)
    fun readWhilePossible(input: ByteChannel, a: ByteArray, offset: Int = 0, length: Int = a.size): Int {
        return readWhile(offset, a.size, length) { off, len ->
            input.read(ByteBuffer.wrap(a, off, len))
        }
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached.
     * @return number of bytes read, possibly 0 if EOF is reached, but never -1.
     */
    @Throws(IOException::class)
    fun readWhilePossible(input: RandomAccessFile, a: ByteArray, offset: Int = 0, length: Int = a.size): Int {
        return readWhile(offset, a.size, length) { off, len ->
            input.read(a, off, len)
        }
    }

    /**
     * Like readFully(), except that it allow partial reads if EOF is reached.
     * @return number of bytes read, possibly 0 if EOF is reached, but never -1.
     */
    @Throws(IOException::class)
    fun readWhilePossible(reader: Reader, a: CharArray, offset: Int = 0, length: Int = a.size): Int {
        return readWhile(offset, a.size, length) { off, len ->
            reader.read(a, off, len)
        }
    }

    fun readWhile(start: Int, end: Int, length: Int, reader: Fun21<Int, Int, Int>): Int {
        var off = start
        var remaining = length
        while (off < end && remaining > 0) {
            val n = reader(off, Math.min(remaining, end - off))
            if (n < 0)
                break
            remaining -= n
            off += n
        }
        return length - remaining
    }

    fun readWhile(length: Long, reader: Fun11<Long, Int>): Long {
        var remaining = length
        while (remaining > 0) {
            val n = reader(remaining)
            if (n < 0)
                break
            remaining -= n
        }
        return length - remaining
    }

    fun readAll(input: InputStream): ByteArray {
        return input.readBytes()
    }

    fun readAll(reader: Reader): CharArray {
        return CharArrayWriter().use {
            reader.copyTo(it)
            it
        }.toCharArray()
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Copy length bytes from input to collector through the given tmpbuf.
     *  @collector(tmpbuf, n): Data to copy is provied at the tmpbuf,
     *   n specify number of bytes to copy, may be 0. Return false to abort copying.
     *   @return True if completed successfully,
     *   false if copy is aborted by collector or end of input before length bytes are read.
     */
    @Throws(IOException::class)
    fun copyFor(tmpbuf: ByteArray, input: InputStream, length: Long, collector: Fun11<Int, Boolean>): Boolean {
        val size = tmpbuf.size
        return copyFor(length, collector) { remaining ->
            val len = if (remaining < size) remaining.toInt() else size
            input.read(tmpbuf, 0, len)
        }
    }

    /// Copy length bytes from input to collector through the given tmpbuf.
    /// @collector(tmpbuf, n): Data to copy is provied at the tmpbuf,
    /// n specify number of bytes to copy, may be 0. Return false to abort copying.
    /// @return True if completed successfully,
    /// false if copy is aborted by collector or end of input before length bytes are read.
    @Throws(IOException::class)
    fun copyFor(tmpbuf: CharArray, reader: Reader, length: Long, collector: Fun11<Int, Boolean>): Boolean {
        val size = tmpbuf.size
        return copyFor(length, collector) { remaining ->
            val len = if (remaining < size) remaining.toInt() else size
            reader.read(tmpbuf, 0, len)
        }
    }

    fun copyFor(length: Long, collector: Fun11<Int, Boolean>, producer: Fun11<Long, Int>): Boolean {
        var remaining = length
        while (remaining > 0) {
            val n = producer(remaining)
            if (n < 0 || !collector(n))
                return false
            remaining -= n
        }
        return true
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Copy input to collector through the given tmpbuf.
     * @collector(n): Data to copy is provied at the tmpbuf,
     * n specify number of bytes to copy, may be 0. Return false to abort copying.
     * @return True if completed successfully, false if copy is aborted by collector.
     * Throws(IOException::class)
     */
    fun copyWhile(tmpbuf: ByteArray, input: InputStream, collector: Fun11<Int, Boolean>): Boolean {
        val size = tmpbuf.size
        return copyWhile(collector) {
            input.read(tmpbuf, 0, size)
        }
    }

    /**
     * Copy input to collector through the given tmpbuf.
     * @collector(n): Data to copy is provied at the tmpbuf,
     * n specify number of bytes to copy, may be 0. Return false to abort copying.
     * @return True if completed successfully, false if copy is aborted by collector.
     * Throws(IOException::class)
     */
    fun copyWhile(tmpbuf: CharArray, reader: Reader, collector: Fun11<Int, Boolean>): Boolean {
        val size = tmpbuf.size
        return copyWhile(collector) {
            reader.read(tmpbuf, 0, size)
        }
    }

    fun copyWhile(collector: Fun11<Int, Boolean>, producer: Fun01<Int>): Boolean {
        while (true) {
            val n = producer()
            if (n < 0)
                return true
            if (!collector(n))
                return false
        }
    }

    ////////////////////////////////////////////////////////////////////////

    fun copyAll(tmpbuf: ByteArray, input: InputStream, length: Long, collector: Fun10<Int>) {
        copyFor(tmpbuf, input, length) { n: Int ->
            collector(n)
            true
        }
    }

    /**
     * Copy input to collector through the given tmpbuf.
     * @collector(n): Data to copy is provied at the tmpbuf,
     * n specify number of bytes to copy, may be 0.
     * @Throws IOException
     */
    fun copyAll(tmpbuf: ByteArray, input: InputStream, collector: Fun10<Int>) {
        copyWhile(tmpbuf, input) {
            collector(it)
            true
        }
    }

    fun copyAll(tmpbuf: CharArray, reader: Reader, length: Long, collector: Fun10<Int>) {
        copyFor(tmpbuf, reader, length) { n: Int ->
            collector(n)
            true
        }
    }

    /**
     * Copy input to collector through the given tmpbuf.
     * @collector(n): Data to copy is provied at the tmpbuf,
     * n specify number of bytes to copy, may be 0.
     * @Throws IOException
     */
    fun copyAll(tmpbuf: CharArray, reader: Reader, collector: Fun10<Int>) {
        copyWhile(tmpbuf, reader) {
            collector(it)
            true
        }
    }

    ////////////////////////////////////////////////////////////////////////

    @Throws(EOFException::class)
    fun readByte(input: InputStream): Byte {
        return readU8(input).toByte()
    }

    @Throws(EOFException::class)
    fun readU8(input: InputStream): Int {
        val ret = input.read()
        if (ret < 0) throw EOFException()
        return ret
    }

    @Throws(EOFException::class)
    fun writeByte(output: OutputStream, value: Byte) {
        output.write(value.toInt())
    }

    @Throws(EOFException::class)
    fun writeU8(output: OutputStream, value: Int) {
        if (value < 0 || value > 0xff) throw IOException()
        output.write(value)
    }

    @Throws(IOException::class)
    fun readU8Bytes(input: InputStream, limit: Int = Int.MAX_VALUE): ByteArray {
        val len = IOUt.readU8(input)
        if (len > limit) throw IOException()
        val ret = ByteArray(len)
        IOUt.readFully(input, ret)
        return ret
    }

    @Throws(IOException::class)
    fun writeU8Bytes(output: OutputStream, a: ByteArray, off: Int = 0, len: Int = a.size) {
        if (len > 255) throw IOException()
        output.write(len)
        output.write(a, off, len)
    }

    ////////////////////////////////////////////////////////////////////////

    @Throws(IOException::class)
    fun readText(input: InputStream, charset: Charset = Charsets.UTF_8): String {
        return input.bufferedReader(charset).use { it.readText() }
    }

    @Throws(IOException::class)
    fun readLines(input: InputStream, charset: Charset = Charsets.UTF_8): Sequence<String> {
        return sequence {
            input.bufferedReader(charset).use {
                while (true) {
                    val line = it.readLine() ?: break
                    yield(line)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun readBytes(input: InputStream): ByteArray {
        return input.buffered().use { it.readBytes() }
    }

    @Throws(IOException::class)
    fun writeText(output: OutputStream, text: String, charset: Charset = Charsets.UTF_8) {
        output.bufferedWriter(charset).use { it.write(text) }
    }

    @Throws(IOException::class)
    fun writeBytes(output: OutputStream, bytes: ByteArray, off: Int = 0, len: Int = bytes.size) {
        output.buffered().use { it.write(bytes, off, len) }
    }

    ////////////////////////////////////////////////////////////////////////

    fun htons(v: Short): Short {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun htonl(v: Int): Int {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun htonl(v: Long): Long {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun ntohs(v: Short): Short {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun ntohl(v: Int): Int {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun ntohl(v: Long): Long {
        return if (isNativeBigEndian) v else swap(v)
    }

    fun swap(x: Short): Short {
        val ix = x.toInt()
        return (ix shl 8 or ix.ushr(8)).toShort()
    }

    fun swap(x: Char): Char {
        return (x.code shl 8 or x.code.ushr(8)).toChar()
    }

    fun swap(x: Int): Int {
        return x shl 24 or (x and 0x0000ff00 shl 8) or (x and 0x00ff0000).ushr(8) or x.ushr(24)
    }

    fun swap(x: Long): Long {
        return swap(x.toInt()).toLong() shl 32 or (swap(x.ushr(32).toInt()).toLong() and 0xffffffffL)
    }

    private fun ByteArray.u8(offset: Int = 0): Int {
        return (this[offset].toInt() and 0xff)
    }

    private fun ByteArray.u8(offset: Int, shift: Int): Int {
        return this.u8(offset) shl shift
    }

    ////////////////////////////////////////////////////////////////////////

    fun readByte(b: ByteArray, offset: Int = 0): Byte {
        return b[offset]
    }

    fun readU8(b: ByteArray, offset: Int = 0): Int {
        return b.u8(offset)
    }

    fun read16BE(b: ByteArray, offset: Int = 0): Short {
        return i16BE(b[offset], b[offset + 1])
    }

    fun read16LE(b: ByteArray, offset: Int = 0): Short {
        return i16LE(b[offset], b[offset + 1])
    }

    fun readU16BE(b: ByteArray, offset: Int = 0): Int {
        return u16BE(b[offset], b[offset + 1])
    }

    fun readU16LE(b: ByteArray, offset: Int = 0): Int {
        return u16LE(b[offset], b[offset + 1])
    }

    fun read32BE(b: ByteArray, offset: Int = 0): Int {
        return i32BE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun read32LE(b: ByteArray, offset: Int = 0): Int {
        return i32LE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun readU32BE(b: ByteArray, offset: Int = 0): Long {
        return u32BE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun readU32LE(b: ByteArray, offset: Int = 0): Long {
        return u32LE(b[offset], b[offset + 1], b[offset + 2], b[offset + 3])
    }

    fun read64BE(b: ByteArray, offset: Int = 0): Long {
        return readU32BE(b, offset) shl 32 or readU32BE(b, offset + 4)
    }

    fun read64LE(b: ByteArray, offset: Int = 0): Long {
        return readU32LE(b, offset) or (readU32LE(b, offset + 4) shl 32)
    }

    fun read16(b: ByteArray, offset: Int = 0): Short {
        return if (isNativeBigEndian) read16BE(b, offset) else read16LE(b, offset)
    }

    fun readU16(b: ByteArray, offset: Int = 0): Int {
        return if (isNativeBigEndian) readU16BE(b, offset) else readU16LE(b, offset)
    }

    fun read32(b: ByteArray, offset: Int = 0): Int {
        return if (isNativeBigEndian) read32BE(b, offset) else read32LE(b, offset)
    }

    fun readU32(b: ByteArray, offset: Int = 0): Long {
        return if (isNativeBigEndian) readU32BE(b, offset) else readU32LE(b, offset)
    }

    fun read64(b: ByteArray, offset: Int = 0): Long {
        return if (isNativeBigEndian) read64BE(b, offset) else read64LE(b, offset)
    }

    ////////////////////////////////////////////////////////////////////////

    fun write(b: ByteArray, offset: Int, value: Byte): Int {
        var off = offset
        b[off++] = value
        return off
    }

    fun shr(value: Int, shift: Int): Byte {
        return (value shr shift).toByte()
    }

    fun shr(value: Long, shift: Int): Byte {
        return (value shr shift).toByte()
    }

    fun write16BE(b: ByteArray, offset: Int, value: Int): Int {
        var off = offset
        b[off++] = shr(value, 8)
        b[off++] = value.toByte()
        return off
    }

    fun write16LE(b: ByteArray, offset: Int, value: Int): Int {
        var off = offset
        b[off++] = value.toByte()
        b[off++] = shr(value, 8)
        return off
    }

    fun write32BE(b: ByteArray, offset: Int, value: Int): Int {
        var off = offset
        b[off++] = shr(value, 24)
        b[off++] = shr(value, 16)
        b[off++] = shr(value, 8)
        b[off++] = value.toByte()
        return off
    }

    fun write32LE(b: ByteArray, offset: Int, value: Int): Int {
        var off = offset
        b[off++] = value.toByte()
        b[off++] = shr(value, 8)
        b[off++] = shr(value, 16)
        b[off++] = shr(value, 24)
        return off
    }

    fun write64BE(b: ByteArray, offset: Int, value: Long): Int {
        var off = offset
        b[off++] = shr(value, 56)
        b[off++] = shr(value, 48)
        b[off++] = shr(value, 40)
        b[off++] = shr(value, 32)
        b[off++] = shr(value, 24)
        b[off++] = shr(value, 16)
        b[off++] = shr(value, 8)
        b[off++] = value.toByte()
        return off
    }

    fun write64LE(b: ByteArray, offset: Int, value: Long): Int {
        var off = offset
        b[off++] = value.toByte()
        b[off++] = shr(value, 8)
        b[off++] = shr(value, 16)
        b[off++] = shr(value, 24)
        b[off++] = shr(value, 32)
        b[off++] = shr(value, 40)
        b[off++] = shr(value, 48)
        b[off++] = shr(value, 56)
        return off
    }

    fun write(b: ByteArray, offset: Int, value: ByteArray): Int {
        System.arraycopy(value, 0, b, offset, value.size)
        return offset + value.size
    }

    fun write(b: ByteArray, offset: Int, value: ByteArray, start: Int, end: Int): Int {
        val len = end - start
        System.arraycopy(value, start, b, offset, len)
        return offset + len
    }

    ////////////////////////////////////////////////////////////////////////

    fun u8(b: Byte): Int {
        return b.toInt() and 0xff
    }

    fun u8(b: Byte, shift: Int): Int {
        return (b.toInt() and 0xff) shl shift
    }

    fun u8(b: Int): Int {
        return b and 0xff
    }

    fun u8(b: Int, shift: Int): Int {
        return (b and 0xff) shl shift
    }

    fun i16BE(b0: Byte, b1: Byte): Short {
        return (u8(b0, 8) or u8(b1)).toShort()
    }

    fun u16BE(b0: Byte, b1: Byte): Int {
        return u8(b0, 8) or u8(b1)
    }

    fun i16LE(b0: Byte, b1: Byte): Short {
        return (u8(b0) or u8(b1, 8)).toShort()
    }

    fun u16LE(b0: Byte, b1: Byte): Int {
        return u8(b0) or u8(b1, 8)
    }

    fun i32BE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
        return u8(b0, 24) or
                u8(b1, 16) or
                u8(b2, 8) or
                u8(b3)
    }

    fun u32BE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Long {
        return (u8(b0, 24) or
                u8(b1, 16) or
                u8(b2, 8) or
                u8(b3)).toLong() and 0xffff_ffffL
    }

    fun i32LE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
        return u8(b0) or
                u8(b1, 8) or
                u8(b2, 16) or
                u8(b3, 24)
    }

    fun u32LE(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Long {
        return (u8(b0) or
                u8(b1, 8) or
                u8(b2, 16) or
                u8(b3, 24)).toLong() and 0xffff_ffffL
    }
}

////////////////////////////////////////////////////////////////////////

interface IOutputStreamProvider {
    @Throws(IOException::class)
    fun outputStream(): OutputStream
}

interface IInputStreamProvider {
    @Throws(Exception::class)
    fun inputStream(): InputStream
}

interface IByteBufferProvider {
    @Throws(Exception::class)
    fun byteBuffer(): ByteBuffer
}

interface IByteSeuence : IInputStreamProvider
interface IByteSlice : IInputStreamProvider, IByteBufferProvider
interface IByteRange : IByteSlice {
    val array: ByteArray
    val offset: Int
    val length: Int
}

open class ByteRange constructor(
    override val array: ByteArray,
    override val offset: Int,
    override val length: Int
) : IByteRange {
    override fun inputStream(): InputStream {
        return ByteArrayInputStream(array, offset, length)
    }

    override fun byteBuffer(): ByteBuffer {
        return ByteBuffer.wrap(array, offset, length)
    }
}

interface IReaderProvider {
    @Throws(Exception::class)
    fun reader(): Reader
}

interface ICharBufferProvider {
    @Throws(Exception::class)
    fun charBuffer(): CharBuffer
}

interface ICharSeuence : IReaderProvider
interface ICharSlice : IReaderProvider, ICharBufferProvider
interface ICharRange : ICharSlice {
    val array: CharArray
    val offset: Int
    val length: Int
    /** @return Implementation should return a string created from array, offset and length. */
    fun string(): String
}

open class CharRange constructor(
    override val array: CharArray,
    override val offset: Int,
    override val length: Int
) : ICharRange {
    override fun reader(): Reader {
        return CharArrayReader(array, offset, length)
    }

    override fun charBuffer(): CharBuffer {
        return CharBuffer.wrap(array, offset, length)
    }

    override fun string(): String {
        return String(array, offset, length)
    }
}

class FileInputStreamProvider(
    private val file: File
) : IInputStreamProvider {
    override fun inputStream(): InputStream {
        return file.inputStream()
    }
}

class ByteArrayInputStreamProvider(
    private val blob: ByteArray
) : IByteSlice {
    override fun inputStream(): InputStream {
        return ByteArrayInputStream(blob)
    }

    override fun byteBuffer(): ByteBuffer {
        return ByteBuffer.wrap(blob)
    }
}

open class MyByteOutputStream(
    cap: Int = IOUt.BUFSIZE
) : ByteArrayOutputStream(cap), IByteRange {
    private var closed = false
    private val lock = Any()
    fun buffer(): ByteArray {
        return buf
    }

    override fun close() {
        synchronized(lock) {
            if (!closed) {
                closed = true
                super.close()
            }
        }
    }

    override fun inputStream(): InputStream {
        close()
        return ByteArrayInputStream(buf, 0, count)
    }

    override fun byteBuffer(): ByteBuffer {
        close()
        return ByteBuffer.wrap(buf, 0, count)
    }

    override val array: ByteArray get() = buf
    override val offset: Int get() = 0
    override val length: Int get() = count
}

open class MyCharArrayWriter(
    cap: Int = IOUt.BUFSIZE
) : CharArrayWriter(cap), ICharRange {
    private var closed = false
    private val lock = Any()
    fun buffer(): CharArray {
        return buf
    }

    override fun close() {
        synchronized(lock) {
            if (!closed) {
                closed = true
                super.close()
            }
        }
    }

    override fun reader(): Reader {
        close()
        return CharArrayReader(buf, 0, count)
    }

    override fun charBuffer(): CharBuffer {
        close()
        return CharBuffer.wrap(buf, 0, count)
    }

    override fun string(): String {
        close()
        return String(buf, 0, count)
    }

    override val array: CharArray get() = buf
    override val offset: Int get() = 0
    override val length: Int get() = count
}

open class SafeInputStream(private val input: InputStream) : InputStream() {
    override fun read(): Int {
        return input.read()
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return input.read(b, off, len)
    }

    override fun close() {
        try {
            super.close()
        } catch (e: Exception) {
        }
    }
}

open class LimitedInputStream(
    private val input: InputStream,
    private val limit: Long
) : InputStream() {
    private var remaining = limit
    private var position = 0L
    private var closed = false

    fun getPosition(): Long {
        return position
    }

    override fun read(): Int {
        if (closed || remaining <= 0) return -1
        val ret = input.read()
        if (ret < 0) {
            remaining = 0
            position = limit
        } else {
            --remaining
            ++position
        }
        return ret
    }

    override fun read(b: ByteArray): Int {
        return this.read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed) return -1
        if (len == 0) return 0
        if (remaining == 0L) return -1
        val oremaining = remaining
        val oposititon = position
        val ret = input.read(b, off, (if (len > remaining) remaining.toInt() else len))
        if (ret < 0) {
            remaining = 0
            position = limit
            return -1
        }
        remaining = oremaining - ret
        position = oposititon + ret
        return ret
    }

    override fun close() {
        try {
            this.closed = true
            super.close()
        } catch (e: Exception) {
        }
    }
}

open class StayOpenOutputStream(output: OutputStream) : PositionTrackingOutputStreamAdapter(output) {
    override fun close() {
    }
}

open class StayOpenInputStream(input: InputStream) : FilterInputStream(input) {
    override fun close() {
    }
}

/// Read a chunk of the given size, without closing the original input stream.
open class ChunkInputStream(
    private val input: InputStream,
    private val size: Int
) : InputStream() {
    private var position = 0
    private var closed = false
    override fun read(): Int {
        if (closed || position >= size) return -1
        val ret = input.read()
        if (ret >= 0) ++position
        return ret
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed || position >= size) return -1
        val available = size - position
        val count = if (len > available) available else len
        val ret = input.read(b, off, count)
        if (ret > 0) position += ret
        return ret
    }

    override fun close() {
        closed = true
    }
}

class NullOutputStream : OutputStream() {
    override fun write(b: Int) {
    }

    override fun write(b: ByteArray) {
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
    }
}

////////////////////////////////////////////////////////////////////
