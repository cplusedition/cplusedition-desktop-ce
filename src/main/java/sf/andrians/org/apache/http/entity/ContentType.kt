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
package sf.andrians.org.apache.http.entity

import sf.andrians.org.apache.http.*
import sf.andrians.org.apache.http.message.BasicHeaderValueFormatter
import sf.andrians.org.apache.http.message.BasicHeaderValueParser
import sf.andrians.org.apache.http.message.BasicNameValuePair
import sf.andrians.org.apache.http.message.ParserCursor
import sf.andrians.org.apache.http.util.Args
import sf.andrians.org.apache.http.util.CharArrayBuffer
import sf.andrians.org.apache.http.util.TextUtils
import java.io.Serializable
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*

/**
 * Content type information consisting of a MIME type and an optional charset.
 *
 *
 * This class makes no attempts to verify validity of the MIME type.
 * The input parameters of the [.create] method, however, may not
 * contain characters `<">, <;>, <,>` reserved by the HTTP specification.
 *
 * @since 4.2
 */
class ContentType : Serializable {
    companion object {
        private const val serialVersionUID = -7768694718232371896L

        val APPLICATION_ATOM_XML = create(
                "application/atom+xml", Consts.ISO_8859_1)
        val APPLICATION_FORM_URLENCODED = create(
                "application/x-www-form-urlencoded", Consts.ISO_8859_1)
        val APPLICATION_JSON = create(
                "application/json", Consts.UTF_8)
        val APPLICATION_OCTET_STREAM = create(
                "application/octet-stream", null as Charset?)
        val APPLICATION_SOAP_XML = create(
                "application/soap+xml", Consts.UTF_8)
        val APPLICATION_SVG_XML = create(
                "application/svg+xml", Consts.ISO_8859_1)
        val APPLICATION_XHTML_XML = create(
                "application/xhtml+xml", Consts.ISO_8859_1)
        val APPLICATION_XML = create(
                "application/xml", Consts.ISO_8859_1)
        val IMAGE_BMP = create(
                "image/bmp")
        val IMAGE_GIF = create(
                "image/gif")
        val IMAGE_JPEG = create(
                "image/jpeg")
        val IMAGE_PNG = create(
                "image/png")
        val IMAGE_SVG = create(
                "image/svg+xml")
        val IMAGE_TIFF = create(
                "image/tiff")
        val IMAGE_WEBP = create(
                "image/webp")
        val MULTIPART_FORM_DATA = create(
                "multipart/form-data", Consts.ISO_8859_1)
        val TEXT_HTML = create(
                "text/html", Consts.ISO_8859_1)
        val TEXT_PLAIN = create(
                "text/plain", Consts.ISO_8859_1)
        val TEXT_XML = create(
                "text/xml", Consts.ISO_8859_1)
        val WILDCARD = create(
                "*/*", null as Charset?)
        private var CONTENT_TYPE_MAP: Map<String, ContentType> = {
            val contentTypes = arrayOf(
                    APPLICATION_ATOM_XML,
                    APPLICATION_FORM_URLENCODED,
                    APPLICATION_JSON,
                    APPLICATION_SVG_XML,
                    APPLICATION_XHTML_XML,
                    APPLICATION_XML,
                    IMAGE_BMP,
                    IMAGE_GIF,
                    IMAGE_JPEG,
                    IMAGE_PNG,
                    IMAGE_SVG,
                    IMAGE_TIFF,
                    IMAGE_WEBP,
                    MULTIPART_FORM_DATA,
                    TEXT_HTML,
                    TEXT_PLAIN,
                    TEXT_XML)
            val map = HashMap<String, ContentType>()
            for (contentType in contentTypes) {
                map.put(contentType.mimeType, contentType)
            }
            Collections.unmodifiableMap(map)
        }()

        val DEFAULT_TEXT = TEXT_PLAIN
        val DEFAULT_BINARY = APPLICATION_OCTET_STREAM

        private fun valid(s: String): Boolean {
            for (i in 0 until s.length) {
                val ch = s[i]
                if (ch == '"' || ch == ',' || ch == ';') {
                    return false
                }
            }
            return true
        }

        /**
         * Creates a new instance of [ContentType].
         *
         * @param mimeType MIME type. It may not be `null` or empty. It may not contain
         * characters `<">, <;>, <,>` reserved by the HTTP specification.
         * @param charset charset.
         * @return content type
         */
        /**
         * Creates a new instance of [ContentType] without a charset.
         *
         * @param mimeType MIME type. It may not be `null` or empty. It may not contain
         * characters `<">, <;>, <,>` reserved by the HTTP specification.
         * @return content type
         */

        fun create(mimeType: String, charset: Charset? = null): ContentType {
            val normalizedMimeType = Args.notBlank(mimeType, "MIME type").lowercase(Locale.ROOT)
            Args.check(valid(normalizedMimeType), "MIME type may not contain reserved characters")
            return ContentType(normalizedMimeType, charset)
        }

        /**
         * Creates a new instance of [ContentType].
         *
         * @param mimeType MIME type. It may not be `null` or empty. It may not contain
         * characters `<">, <;>, <,>` reserved by the HTTP specification.
         * @param charset charset. It may not contain characters `<">, <;>, <,>` reserved by the HTTP
         * specification. This parameter is optional.
         * @return content type
         * @throws UnsupportedCharsetException Thrown when the named charset is not available in
         * this instance of the Java virtual machine
         */
        @Throws(UnsupportedCharsetException::class)
        fun create(mimeType: String, charset: String?): ContentType {
            return create(mimeType, if (!TextUtils.isBlank(charset)) Charset.forName(charset) else null)
        }

        private fun create(helem: HeaderElement, strict: Boolean): ContentType {
            return create(helem.name, helem.getParameters(), strict)
        }

        private fun create(mimeType: String, params: Array<NameValuePair>?, strict: Boolean): ContentType {
            var charset: Charset? = null
            if (params != null) {
                for (param in params) {
                    if (param.name.equals("charset", ignoreCase = true)) {
                        val s = param.value
                        if (!TextUtils.isBlank(s)) {
                            try {
                                charset = Charset.forName(s)
                            } catch (ex: UnsupportedCharsetException) {
                                if (strict) {
                                    throw ex
                                }
                            }
                        }
                        break
                    }
                }
            }
            return ContentType(mimeType, charset, if (params != null && params.size > 0) params else null)
        }

        /**
         * Creates a new instance of [ContentType] with the given parameters.
         *
         * @param mimeType MIME type. It may not be `null` or empty. It may not contain
         * characters `<">, <;>, <,>` reserved by the HTTP specification.
         * @param params parameters.
         * @return content type
         *
         * @since 4.4
         */
        @Throws(UnsupportedCharsetException::class)
        fun create(
            mimeType: String, vararg params: NameValuePair
        ): ContentType {
            val type = Args.notBlank(mimeType, "MIME type").lowercase(Locale.ROOT)
            Args.check(valid(type), "MIME type may not contain reserved characters")
            return create(mimeType, Arrays.copyOf(params, params.size), true)
        }

        /**
         * Parses textual representation of `Content-Type` value.
         *
         * @param s text
         * @return content type
         * @throws ParseException if the given text does not represent a valid
         * `Content-Type` value.
         * @throws UnsupportedCharsetException Thrown when the named charset is not available in
         * this instance of the Java virtual machine
         */
        @Throws(ParseException::class, UnsupportedCharsetException::class)
        fun parse(
                s: String): ContentType {
            Args.notNull(s, "Content type")
            val buf = CharArrayBuffer(s.length)
            buf.append(s)
            val cursor = ParserCursor(0, s.length)
            val elements: Array<HeaderElement> = BasicHeaderValueParser.Companion.INSTANCE.parseElements(buf, cursor)
            if (elements.size > 0) {
                return create(elements[0], true)
            }
            throw ParseException("Invalid content type: $s")
        }

        /**
         * Extracts `Content-Type` value from [HttpEntity] exactly as
         * specified by the `Content-Type` header of the entity. Returns `null`
         * if not specified.
         *
         * @param entity HTTP entity
         * @return content type
         * @throws ParseException if the given text does not represent a valid
         * `Content-Type` value.
         * @throws UnsupportedCharsetException Thrown when the named charset is not available in
         * this instance of the Java virtual machine
         */
        @Throws(ParseException::class, UnsupportedCharsetException::class)
        operator fun get(
                entity: HttpEntity?): ContentType? {
            if (entity == null) {
                return null
            }
            val header = entity.contentType
            if (header != null) {
                val elements = header.elements
                if (elements.isNotEmpty()) {
                    return create(elements[0], true)
                }
            }
            return null
        }

        /**
         * Extracts `Content-Type` value from [HttpEntity]. Returns `null`
         * if not specified or incorrect (could not be parsed)..
         *
         * @param entity HTTP entity
         * @return content type
         *
         * @since 4.4
         */
        fun getLenient(entity: HttpEntity?): ContentType? {
            if (entity == null) {
                return null
            }
            val header = entity.contentType
            if (header != null) {
                try {
                    val elements = header.elements
                    if (elements.isNotEmpty()) {
                        return create(elements[0], false)
                    }
                } catch (ex: ParseException) {
                    return null
                }
            }
            return null
        }

        /**
         * Extracts `Content-Type` value from [HttpEntity] or returns the default value
         * [.DEFAULT_TEXT] if not explicitly specified.
         *
         * @param entity HTTP entity
         * @return content type
         * @throws ParseException if the given text does not represent a valid
         * `Content-Type` value.
         * @throws UnsupportedCharsetException Thrown when the named charset is not available in
         * this instance of the Java virtual machine
         */
        @Throws(ParseException::class, UnsupportedCharsetException::class)
        fun getOrDefault(
                entity: HttpEntity?): ContentType {
            val contentType = get(entity)
            return contentType ?: DEFAULT_TEXT
        }

        /**
         * Extracts `Content-Type` value from [HttpEntity] or returns the default value
         * [.DEFAULT_TEXT] if not explicitly specified or incorrect (could not be parsed).
         *
         * @param entity HTTP entity
         * @return content type
         *
         * @since 4.4
         */
        @Throws(ParseException::class, UnsupportedCharsetException::class)
        fun getLenientOrDefault(
                entity: HttpEntity?): ContentType {
            val contentType = get(entity)
            return contentType ?: DEFAULT_TEXT
        }

        /**
         * Returns `Content-Type` for the given MIME type.
         *
         * @param mimeType MIME type
         * @return content type or `null` if not known.
         *
         * @since 4.5
         */
        fun getByMimeType(mimeType: String?): ContentType? {
            return mimeType?.let { CONTENT_TYPE_MAP[mimeType] }
        }
    }

    val mimeType: String
    val charset: Charset?
    private val params: Array<NameValuePair>?

    internal constructor(
            mimeType: String,
            charset: Charset?) {
        this.mimeType = mimeType
        this.charset = charset
        params = null
    }

    internal constructor(
            mimeType: String,
            charset: Charset?,
            params: Array<NameValuePair>?) {
        this.mimeType = mimeType
        this.charset = charset
        this.params = params
    }

    /**
     * @since 4.3
     */
    fun getParameter(name: String): String? {
        Args.notEmpty(name, "Parameter name")
        if (params == null) {
            return null
        }
        for (param in params) {
            if (param.name.equals(name, ignoreCase = true)) {
                return param.value
            }
        }
        return null
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a `Content-Type` header.
     */
    override fun toString(): String {
        val buf = CharArrayBuffer(64)
        buf.append(mimeType)
        if (params != null) {
            buf.append("; ")
            BasicHeaderValueFormatter.Companion.INSTANCE.formatParameters(buf, params, false)
        } else if (charset != null) {
            buf.append("; charset=")
            buf.append(charset.name())
        }
        return buf.toString()
    }

    /**
     * Creates a new instance with this MIME type and the given Charset.
     *
     * @param charset charset
     * @return a new instance with this MIME type and the given Charset.
     * @since 4.3
     */
    fun withCharset(charset: Charset?): ContentType {
        return create(mimeType, charset)
    }

    /**
     * Creates a new instance with this MIME type and the given Charset name.
     *
     * @param charset name
     * @return a new instance with this MIME type and the given Charset name.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     * @since 4.3
     */
    fun withCharset(charset: String?): ContentType {
        return create(mimeType, charset)
    }

    /**
     * Creates a new instance with this MIME type and the given parameters.
     *
     * @param params
     * @return a new instance with this MIME type and the given parameters.
     * @since 4.4
     */
    @Throws(UnsupportedCharsetException::class)
    fun withParameters(
            vararg params: NameValuePair): ContentType {
        if (params.size == 0) {
            return this
        }
        val paramMap: MutableMap<String?, String?> = LinkedHashMap()
        if (this.params != null) {
            for (param in this.params) {
                paramMap[param.name] = param.value
            }
        }
        for (param in params) {
            paramMap[param.name] = param.value
        }
        val newParams: MutableList<NameValuePair> = ArrayList(paramMap.size + 1)
        if (charset != null && !paramMap.containsKey("charset")) {
            newParams.add(BasicNameValuePair("charset", charset.name()))
        }
        for ((key, value) in paramMap) {
            newParams.add(BasicNameValuePair(key, value))
        }
        return create(mimeType, newParams.toTypedArray(), true)
    }
}
