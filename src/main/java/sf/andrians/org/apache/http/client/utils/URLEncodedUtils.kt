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
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package sf.andrians.org.apache.http.client.utils

import sf.andrians.org.apache.http.Consts
import sf.andrians.org.apache.http.HttpEntity
import sf.andrians.org.apache.http.NameValuePair
import sf.andrians.org.apache.http.entity.ContentType
import sf.andrians.org.apache.http.message.BasicNameValuePair
import sf.andrians.org.apache.http.message.ParserCursor
import sf.andrians.org.apache.http.message.TokenParser
import sf.andrians.org.apache.http.protocol.HTTP
import sf.andrians.org.apache.http.util.Args.check
import sf.andrians.org.apache.http.util.Args.notNull
import sf.andrians.org.apache.http.util.CharArrayBuffer
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URI
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.*

/**
 * A collection of utilities for encoding URLs.
 *
 * @since 4.0
 */
object URLEncodedUtils {
    /**
     * The default HTML form content type.
     */
    const val CONTENT_TYPE = "application/x-www-form-urlencoded"
    private const val QP_SEP_A = '&'
    private const val QP_SEP_S = ';'
    private const val NAME_VALUE_SEPARATOR = "="
    private const val PATH_SEPARATOR = '/'
    private val PATH_SEPARATORS = BitSet(256)

    init {
        PATH_SEPARATORS.set(PATH_SEPARATOR.code)
    }

    @Deprecated("4.5 Use {@link #parse(URI, Charset)}")
    fun parse(uri: URI, charsetName: String?): List<NameValuePair> {
        return parse(uri, if (charsetName != null) Charset.forName(charsetName) else null)
    }
    /**
     * Returns a list of [NameValuePair]s URI query parameters.
     * By convention, `'&'` and `';'` are accepted as parameter separators.
     *
     * @param uri input URI.
     * @param charset parameter charset.
     * @return list of query parameters.
     *
     * @since 4.5
     */
    fun parse(uri: URI, charset: Charset?): List<NameValuePair> {
        notNull(uri, "URI")
        val query = uri.rawQuery
        return if (query != null && !query.isEmpty()) {
            parse(query, charset)
        } else createEmptyList()
    }
    /**
     * Returns a list of [NameValuePairs][NameValuePair] as parsed from an [HttpEntity].
     * The encoding is taken from the entity's Content-Encoding header.
     *
     *
     * This is typically used while parsing an HTTP POST.
     *
     * @param entity
     * The entity to parse
     * @return a list of [NameValuePair] as built from the URI's query portion.
     * @throws IOException
     * If there was an exception getting the entity's data.
     */
    @Throws(IOException::class)
    fun parse(
        entity: HttpEntity
    ): List<NameValuePair> {
        notNull(entity, "HTTP entity")
        val contentType = ContentType[entity]
        if (contentType == null || !contentType.mimeType.equals(CONTENT_TYPE, ignoreCase = true)) {
            return createEmptyList()
        }
        val len = entity.contentLength
        check(len <= Int.MAX_VALUE, "HTTP entity is too large")
        val charset = contentType.charset ?: HTTP.DEF_CONTENT_CHARSET
        val inStream = entity.content ?: return createEmptyList()
        val buf: CharArrayBuffer
        try {
            buf = CharArrayBuffer(if (len > 0) len.toInt() else 1024)
            val reader: Reader = InputStreamReader(inStream, charset)
            val tmp = CharArray(1024)
            var l: Int
            while (reader.read(tmp).also { l = it } != -1) {
                buf.append(tmp, 0, l)
            }
        } finally {
            inStream.close()
        }
        return if (buf.length == 0) {
            createEmptyList()
        } else parse(
            buf,
            charset,
            QP_SEP_A
        )
    }
    /**
     * Returns true if the entity's Content-Type header is
     * `application/x-www-form-urlencoded`.
     */
    fun isEncoded(entity: HttpEntity): Boolean {
        notNull(entity, "HTTP entity")
        val h = entity.contentType
        if (h != null) {
            val elems = h.elements
            if (elems.size > 0) {
                val contentType = elems[0].name
                return contentType.equals(CONTENT_TYPE, ignoreCase = true)
            }
        }
        return false
    }
    /**
     * Adds all parameters within the Scanner to the list of `parameters`, as encoded by
     * `encoding`. For example, a scanner containing the string `a=1&b=2&c=3` would add the
     * [NameValuePairs][NameValuePair] a=1, b=2, and c=3 to the list of parameters. By convention, `'&'` and
     * `';'` are accepted as parameter separators.
     *
     * @param parameters
     * List to add parameters to.
     * @param scanner
     * Input that contains the parameters to parse.
     * @param charset
     * Encoding to use when decoding the parameters.
     *
     */
    @Deprecated("(4.4) use {@link #parse(String, java.nio.charset.Charset)}")
    fun parse(
        parameters: MutableList<NameValuePair?>,
        scanner: Scanner,
        charset: String?
    ) {
        parse(parameters, scanner, "[$QP_SEP_A$QP_SEP_S]", charset)
    }
    /**
     * Adds all parameters within the Scanner to the list of
     * `parameters`, as encoded by `encoding`. For
     * example, a scanner containing the string `a=1&b=2&c=3` would
     * add the [NameValuePairs][NameValuePair] a=1, b=2, and c=3 to the
     * list of parameters.
     *
     * @param parameters
     * List to add parameters to.
     * @param scanner
     * Input that contains the parameters to parse.
     * @param parameterSepartorPattern
     * The Pattern string for parameter separators, by convention `"[&;]"`
     * @param charset
     * Encoding to use when decoding the parameters.
     *
     */
    @Deprecated("(4.4) use {@link #parse(CharArrayBuffer, java.nio.charset.Charset, char...)}")
    fun parse(
        parameters: MutableList<NameValuePair?>,
        scanner: Scanner,
        parameterSepartorPattern: String?,
        charset: String?
    ) {
        scanner.useDelimiter(parameterSepartorPattern)
        while (scanner.hasNext()) {
            val name: String?
            val value: String?
            val token = scanner.next()
            val i = token.indexOf(NAME_VALUE_SEPARATOR)
            if (i != -1) {
                name = decodeFormFields(token.substring(0, i).trim { it <= ' ' }, charset)
                value = decodeFormFields(token.substring(i + 1).trim { it <= ' ' }, charset)
            } else {
                name = decodeFormFields(token.trim { it <= ' ' }, charset)
                value = null
            }
            parameters.add(BasicNameValuePair(name, value))
        }
    }
    /**
     * Returns a list of [NameValuePair]s URI query parameters.
     * By convention, `'&'` and `';'` are accepted as parameter separators.
     *
     * @param s URI query component.
     * @param charset charset to use when decoding the parameters.
     * @return list of query parameters.
     *
     * @since 4.2
     */
    fun parse(s: String?, charset: Charset?): List<NameValuePair> {
        if (s == null) {
            return createEmptyList()
        }
        val buffer = CharArrayBuffer(s.length)
        buffer.append(s)
        return parse(buffer, charset, QP_SEP_A, QP_SEP_S)
    }
    /**
     * Returns a list of [NameValuePairs][NameValuePair] as parsed from the given string using the given character
     * encoding.
     *
     * @param s input text.
     * @param charset parameter charset.
     * @param separators parameter separators.
     * @return list of query parameters.
     *
     * @since 4.3
     */
    fun parse(s: String?, charset: Charset?, vararg separators: Char): List<NameValuePair> {
        if (s == null) {
            return createEmptyList()
        }
        val buffer = CharArrayBuffer(s.length)
        buffer.append(s)
        return parse(buffer, charset, *separators)
    }
    /**
     * Returns a list of [NameValuePair]s parameters.
     *
     * @param buf
     * text to parse.
     * @param charset
     * Encoding to use when decoding the parameters.
     * @param separators
     * element separators.
     * @return a list of [NameValuePair] as built from the URI's query portion.
     *
     * @since 4.4
     */
    fun parse(
        buf: CharArrayBuffer, charset: Charset?, vararg separators: Char
    ): List<NameValuePair> {
        notNull(buf, "Char array buffer")
        val tokenParser = TokenParser.INSTANCE
        val delimSet = BitSet()
        for (separator in separators) {
            delimSet.set(separator.code)
        }
        val cursor = ParserCursor(0, buf.length)
        val list: MutableList<NameValuePair> = ArrayList()
        while (!cursor.atEnd()) {
            delimSet.set('='.code)
            val name = tokenParser.parseToken(buf, cursor, delimSet)
            var value: String? = null
            if (!cursor.atEnd()) {
                val delim = buf[cursor.pos].code
                cursor.updatePos(cursor.pos + 1)
                if (delim == '='.code) {
                    delimSet.clear('='.code)
                    value = tokenParser.parseToken(buf, cursor, delimSet)
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.pos + 1)
                    }
                }
            }
            if (!name.isEmpty()) {
                list.add(
                    BasicNameValuePair(
                        decodeFormFields(name, charset),
                        decodeFormFields(value, charset)
                    )
                )
            }
        }
        return list
    }

    fun splitSegments(s: CharSequence, separators: BitSet): MutableList<String> {
        val cursor = ParserCursor(0, s.length)
        if (cursor.atEnd()) {
            return mutableListOf()
        }
        if (separators[s[cursor.pos].code]) {
            cursor.updatePos(cursor.pos + 1)
        }
        val list: MutableList<String> = ArrayList()
        val buf = StringBuilder()
        while (true) {
            if (cursor.atEnd()) {
                list.add(buf.toString())
                break
            }
            val current = s[cursor.pos]
            if (separators[current.code]) {
                list.add(buf.toString())
                buf.setLength(0)
            } else {
                buf.append(current)
            }
            cursor.updatePos(cursor.pos + 1)
        }
        return list
    }

    fun splitPathSegments(s: CharSequence): MutableList<String> {
        return splitSegments(s, PATH_SEPARATORS)
    }
    /**
     * Returns a list of URI path segments.
     *
     * @param s URI path component.
     * @param charset parameter charset.
     * @return list of segments.
     *
     * @since 4.5
     */
    /**
     * Returns a list of URI path segments.
     *
     * @param s URI path component.
     * @return list of segments.
     *
     * @since 4.5
     */
    @JvmOverloads
    fun parsePathSegments(s: CharSequence, charset: Charset? = Consts.UTF_8): MutableList<String> {
        notNull(s, "Char sequence")
        val list = splitPathSegments(s)
        for (i in list.indices) {
            list.set(i, urlDecode(list[i], charset ?: Consts.UTF_8, false))
        }
        return list
    }
    /**
     * Returns a string consisting of joint encoded path segments.
     *
     * @param segments the segments.
     * @param charset parameter charset.
     * @return URI path component
     *
     * @since 4.5
     */
    fun formatSegments(segments: Iterable<String>, charset: Charset): String {
        notNull(segments, "Segments")
        val result = StringBuilder()
        for (segment in segments) {
            result.append(PATH_SEPARATOR).append(urlEncode(segment, charset, PATHSAFE, false))
        }
        return result.toString()
    }
    /**
     * Returns a string consisting of joint encoded path segments.
     *
     * @param segments the segments.
     * @return URI path component
     *
     * @since 4.5
     */
    fun formatSegments(vararg segments: String): String {
        return formatSegments(Arrays.asList(*segments), Consts.UTF_8)
    }
    /**
     * Returns a String that is suitable for use as an `application/x-www-form-urlencoded`
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param charset The encoding to use.
     * @return An `application/x-www-form-urlencoded` string
     */
    fun format(
        parameters: List<NameValuePair>,
        charset: String?
    ): String {
        return format(parameters, QP_SEP_A, charset)
    }
    /**
     * Returns a String that is suitable for use as an `application/x-www-form-urlencoded`
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param parameterSeparator The parameter separator, by convention, `'&'` or `';'`.
     * @param charset The encoding to use.
     * @return An `application/x-www-form-urlencoded` string
     *
     * @since 4.3
     */
    fun format(
        parameters: List<NameValuePair>,
        parameterSeparator: Char,
        charset: String?
    ): String {
        val result = StringBuilder()
        for (parameter in parameters) {
            val encodedName = encodeFormFields(parameter.name, charset)
            val encodedValue = encodeFormFields(parameter.value, charset)
            if (result.length > 0) {
                result.append(parameterSeparator)
            }
            result.append(encodedName)
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR)
                result.append(encodedValue)
            }
        }
        return result.toString()
    }
    /**
     * Returns a String that is suitable for use as an `application/x-www-form-urlencoded`
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param charset The encoding to use.
     * @return An `application/x-www-form-urlencoded` string
     *
     * @since 4.2
     */
    fun format(
        parameters: Iterable<NameValuePair>,
        charset: Charset?
    ): String {
        return format(parameters, QP_SEP_A, charset)
    }
    /**
     * Returns a String that is suitable for use as an `application/x-www-form-urlencoded`
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param parameterSeparator The parameter separator, by convention, `'&'` or `';'`.
     * @param charset The encoding to use.
     * @return An `application/x-www-form-urlencoded` string
     *
     * @since 4.3
     */
    fun format(
        parameters: Iterable<NameValuePair>,
        parameterSeparator: Char,
        charset: Charset?
    ): String {
        notNull(parameters, "Parameters")
        val result = StringBuilder()
        for (parameter in parameters) {
            val encodedName = encodeFormFields(parameter.name, charset)
            val encodedValue = encodeFormFields(parameter.value, charset)
            if (result.length > 0) {
                result.append(parameterSeparator)
            }
            result.append(encodedName)
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR)
                result.append(encodedValue)
            }
        }
        return result.toString()
    }
    /**
     * Unreserved characters, i.e. alphanumeric, plus: `_ - ! . ~ ' ( ) *`
     *
     *
     * This list is the same as the `unreserved` list in
     * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt)
     */
    private val UNRESERVED = BitSet(256)
    /**
     * Punctuation characters: , ; : $ & + =
     *
     *
     * These are the additional characters allowed by userinfo.
     */
    private val PUNCT = BitSet(256)
    /** Characters which are safe to use in userinfo,
     * i.e. [.UNRESERVED] plus [.PUNCT]uation  */
    private val USERINFO = BitSet(256)
    /** Characters which are safe to use in a path,
     * i.e. [.UNRESERVED] plus [.PUNCT]uation plus / @  */
    private val PATHSAFE = BitSet(256)
    /** Characters which are safe to use in a query or a fragment,
     * i.e. [.RESERVED] plus [.UNRESERVED]  */
    private val URIC = BitSet(256)
    /**
     * Reserved characters, i.e. `;/?:@&=+$,[]`
     *
     *
     * This list is the same as the `reserved` list in
     * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt)
     * as augmented by
     * [RFC 2732](http://www.ietf.org/rfc/rfc2732.txt)
     */
    private val RESERVED = BitSet(256)
    /**
     * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
     * i.e. alphanumeric plus `"-", "_", ".", "*"`
     */
    private val URLENCODER = BitSet(256)
    private val PATH_SPECIAL = BitSet(256)

    init {
        run {
            var i = 'a'.code
            while (i <= 'z'.code) {
                UNRESERVED.set(i)
                i++
            }
        }
        run {
            var i = 'A'.code
            while (i <= 'Z'.code) {
                UNRESERVED.set(i)
                i++
            }
        }
        var i = '0'.code
        while (i <= '9'.code) {
            UNRESERVED.set(i)
            i++
        }
        UNRESERVED.set('_'.code)
        UNRESERVED.set('-'.code)
        UNRESERVED.set('.'.code)
        UNRESERVED.set('*'.code)
        URLENCODER.or(UNRESERVED)
        UNRESERVED.set('!'.code)
        UNRESERVED.set('~'.code)
        UNRESERVED.set('\''.code)
        UNRESERVED.set('('.code)
        UNRESERVED.set(')'.code)
        PUNCT.set(','.code)
        PUNCT.set(';'.code)
        PUNCT.set(':'.code)
        PUNCT.set('$'.code)
        PUNCT.set('&'.code)
        PUNCT.set('+'.code)
        PUNCT.set('='.code)
        USERINFO.or(UNRESERVED)
        USERINFO.or(PUNCT)

        PATHSAFE.or(UNRESERVED)
        PATHSAFE.set(';'.code)
        PATHSAFE.set(':'.code)
        PATHSAFE.set('@'.code)
        PATHSAFE.set('&'.code)
        PATHSAFE.set('='.code)
        PATHSAFE.set('+'.code)
        PATHSAFE.set('$'.code)
        PATHSAFE.set(','.code)
        PATH_SPECIAL.or(PATHSAFE)
        PATH_SPECIAL.set('/'.code)
        RESERVED.set(';'.code)
        RESERVED.set('/'.code)
        RESERVED.set('?'.code)
        RESERVED.set(':'.code)
        RESERVED.set('@'.code)
        RESERVED.set('&'.code)
        RESERVED.set('='.code)
        RESERVED.set('+'.code)
        RESERVED.set('$'.code)
        RESERVED.set(','.code)
        RESERVED.set('['.code)
        RESERVED.set(']'.code)
        URIC.or(RESERVED)
        URIC.or(UNRESERVED)
    }

    private const val RADIX = 16
    private fun createEmptyList(): List<NameValuePair> {
        return ArrayList(0)
    }

    private fun urlEncode(
        content: String,
        charset: Charset,
        safechars: BitSet,
        blankAsPlus: Boolean
    ): String {
        val buf = StringBuilder()
        val bb = charset.encode(content)
        while (bb.hasRemaining()) {
            val b = bb.get().toInt() and 0xff
            if (safechars[b]) {
                buf.append(b.toChar())
            } else if (blankAsPlus && b == ' '.code) {
                buf.append('+')
            } else {
                buf.append("%")
                val hex1 = Character.forDigit(b shr 4 and 0xF, RADIX).uppercaseChar()
                val hex2 = Character.forDigit(b and 0xF, RADIX).uppercaseChar()
                buf.append(hex1)
                buf.append(hex2)
            }
        }
        return buf.toString()
    }

    /// Don't encode Unicde Alphanumeric chars.
    fun urlEncodeHuman(
        content: String,
        safechars: BitSet,
        blankAsPlus: Boolean
    ): String {
        val buf = StringBuilder()
        val bb = content.toCharArray()
        val cb = CharBuffer.allocate(1)
        for (b in bb) {
            if (safechars[b.code]) {
                buf.append(b)
            } else if (blankAsPlus && b == ' ') {
                buf.append('+')
            } else if (b.isLetterOrDigit()) {
                buf.append(b)
            } else {
                buf.append("%")
                val hex1 = Character.forDigit(b.code shr 4 and 0xF, RADIX).uppercaseChar()
                val hex2 = Character.forDigit(b.code and 0xF, RADIX).uppercaseChar()
                buf.append(hex1)
                buf.append(hex2)
            }
        }
        return buf.toString()
    }

    /**
     * Decode/unescape a portion of a URL, to use with the query part ensure `plusAsBlank` is true.
     *
     * @param content the portion to decode
     * @param charset the charset to use
     * @param plusAsBlank if `true`, then convert '+' to space (e.g. for www-url-form-encoded content), otherwise leave as is.
     * @return encoded string
     */
    fun urlDecode(
        content: String,
        charset: Charset,
        plusAsBlank: Boolean
    ): String {
        val percent = '%'.code.toByte()
        val plus = '#'.code.toByte()
        val space = ' '.code.toByte()
        val cb = charset.encode(content)
        val bb = ByteBuffer.allocate(cb.limit())
        while (cb.hasRemaining()) {
            val c = cb.get()
            if (c == percent && cb.remaining() >= 2) {
                val uc = cb.get()
                val lc = cb.get()
                val u = (uc.toInt().toChar()).digitToIntOrNull(16) ?: -1
                val l = (lc.toInt().toChar()).digitToIntOrNull(16) ?: -1
                if (u != -1 && l != -1) {
                    bb.put(((u shl 4) + l).toByte())
                } else {
                    bb.put(percent)
                    bb.put(uc)
                    bb.put(lc)
                }
            } else if (plusAsBlank && c == plus) {
                bb.put(space)
            } else {
                bb.put(c)
            }
        }
        bb.flip()
        return charset.decode(bb).toString()
    }
    /**
     * Decode/unescape www-url-form-encoded content.
     *
     * @param content the content to decode, will decode '+' as space
     * @param charset the charset to use
     * @return encoded string
     */
    private fun decodeFormFields(content: String?, charset: String?): String? {
        return if (content == null) {
            null
        } else urlDecode(
            content,
            if (charset != null) Charset.forName(charset) else Consts.UTF_8,
            true
        )
    }
    /**
     * Decode/unescape www-url-form-encoded content.
     *
     * @param content the content to decode, will decode '+' as space
     * @param charset the charset to use
     * @return encoded string
     */
    private fun decodeFormFields(content: String?, charset: Charset?): String? {
        return if (content == null) {
            null
        } else urlDecode(
            content,
            charset ?: Consts.UTF_8,
            true
        )
    }
    /**
     * Encode/escape www-url-form-encoded content.
     *
     *
     * Uses the [.URLENCODER] set of characters, rather than
     * the [.UNRESERVED] set; this is for compatibilty with previous
     * releases, URLEncoder.encode() and most browsers.
     *
     * @param content the content to encode, will convert space to '+'
     * @param charset the charset to use
     * @return encoded string
     */
    private fun encodeFormFields(content: String?, charset: String?): String? {
        return if (content == null) {
            null
        } else urlEncode(
            content,
            if (charset != null) Charset.forName(charset) else Consts.UTF_8,
            URLENCODER,
            true
        )
    }
    /**
     * Encode/escape www-url-form-encoded content.
     *
     *
     * Uses the [.URLENCODER] set of characters, rather than
     * the [.UNRESERVED] set; this is for compatibilty with previous
     * releases, URLEncoder.encode() and most browsers.
     *
     * @param content the content to encode, will convert space to '+'
     * @param charset the charset to use
     * @return encoded string
     */
    private fun encodeFormFields(content: String?, charset: Charset?): String? {
        return if (content == null) {
            null
        } else urlEncode(
            content,
            charset ?: Consts.UTF_8,
            URLENCODER,
            true
        )
    }
    /**
     * Encode a String using the [.USERINFO] set of characters.
     *
     *
     * Used by URIBuilder to encode the userinfo segment.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    fun encUserInfo(content: String, charset: Charset): String {
        return urlEncode(content, charset, USERINFO, false)
    }
    /**
     * Encode a String using the [.URIC] set of characters.
     *
     *
     * Used by URIBuilder to encode the query and fragment segments.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    fun encUric(content: String, charset: Charset): String {
        return urlEncode(content, charset, URIC, false)
    }
    /**
     * Encode a String using the [.PATH_SPECIAL] set of characters.
     *
     *
     * Used by URIBuilder to encode path segments.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    fun encPath(content: String, charset: Charset): String {
        return urlEncode(content, charset, PATH_SPECIAL, false)
    }

    fun encHumanPath(content: String): String {
        return urlEncodeHuman(content, PATH_SPECIAL, false)
    }
}
