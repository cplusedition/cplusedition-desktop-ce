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
package sf.andrians.org.apache.http.message

import sf.andrians.org.apache.http.HeaderElement
import sf.andrians.org.apache.http.NameValuePair
import sf.andrians.org.apache.http.ParseException
import sf.andrians.org.apache.http.util.Args
import sf.andrians.org.apache.http.util.CharArrayBuffer
import java.util.*

/**
 * Basic implementation for parsing header values into elements.
 * Instances of this class are stateless and thread-safe.
 * Derived classes are expected to maintain these properties.
 *
 * @since 4.0
 */
class BasicHeaderValueParser : HeaderValueParser {
    private val tokenParser: TokenParser

    override fun parseElements(buffer: CharArrayBuffer,
                               cursor: ParserCursor): Array<HeaderElement> {
        Args.notNull(buffer, "Char array buffer")
        Args.notNull(cursor, "Parser cursor")
        val elements: MutableList<HeaderElement> = ArrayList()
        while (!cursor.atEnd()) {
            val element = parseHeaderElement(buffer, cursor)
            if (!(element.name.isEmpty() && element.value == null)) {
                elements.add(element)
            }
        }
        return elements.toTypedArray()
    }

    override fun parseHeaderElement(buffer: CharArrayBuffer,
                                    cursor: ParserCursor): HeaderElement {
        Args.notNull(buffer, "Char array buffer")
        Args.notNull(cursor, "Parser cursor")
        val nvp = parseNameValuePair(buffer, cursor)
        var params: Array<NameValuePair>? = null
        if (!cursor.atEnd()) {
            val ch = buffer[cursor.pos - 1]
            if (ch != ELEM_DELIMITER) {
                params = parseParameters(buffer, cursor)
            }
        }
        return createHeaderElement(nvp.name, nvp.value, params)
    }

    /**
     * Creates a header element.
     * Called from [.parseHeaderElement].
     *
     * @return  a header element representing the argument
     */
    protected fun createHeaderElement(
            name: String,
            value: String?,
            params: Array<NameValuePair>?): HeaderElement {
        return BasicHeaderElement(name, value, params)
    }

    override fun parseParameters(buffer: CharArrayBuffer,
                                 cursor: ParserCursor): Array<NameValuePair> {
        Args.notNull(buffer, "Char array buffer")
        Args.notNull(cursor, "Parser cursor")
        tokenParser.skipWhiteSpace(buffer, cursor)
        val params: MutableList<NameValuePair> = ArrayList()
        while (!cursor.atEnd()) {
            val param = parseNameValuePair(buffer, cursor)
            params.add(param)
            val ch = buffer[cursor.pos - 1]
            if (ch == ELEM_DELIMITER) {
                break
            }
        }
        return params.toTypedArray()
    }

    override fun parseNameValuePair(buffer: CharArrayBuffer,
                                    cursor: ParserCursor): NameValuePair {
        Args.notNull(buffer, "Char array buffer")
        Args.notNull(cursor, "Parser cursor")
        val name = tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS)
        if (cursor.atEnd()) {
            return BasicNameValuePair(name, null)
        }
        val delim = buffer[cursor.pos].toInt()
        cursor.updatePos(cursor.pos + 1)
        if (delim != '='.toInt()) {
            return createNameValuePair(name, null)
        }
        val value = tokenParser.parseValue(buffer, cursor, VALUE_DELIMS)
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.pos + 1)
        }
        return createNameValuePair(name, value)
    }

    @Deprecated("(4.4) use {@link TokenParser}")
    fun parseNameValuePair(buffer: CharArrayBuffer,
                           cursor: ParserCursor,
                           delimiters: CharArray?): NameValuePair {
        Args.notNull(buffer, "Char array buffer")
        Args.notNull(cursor, "Parser cursor")
        val delimSet = BitSet()
        if (delimiters != null) {
            for (delimiter in delimiters) {
                delimSet.set(delimiter.toInt())
            }
        }
        delimSet.set('='.toInt())
        val name = tokenParser.parseToken(buffer, cursor, delimSet)
        if (cursor.atEnd()) {
            return BasicNameValuePair(name, null)
        }
        val delim = buffer[cursor.pos].toInt()
        cursor.updatePos(cursor.pos + 1)
        if (delim != '='.toInt()) {
            return createNameValuePair(name, null)
        }
        delimSet.clear('='.toInt())
        val value = tokenParser.parseValue(buffer, cursor, delimSet)
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.pos + 1)
        }
        return createNameValuePair(name, value)
    }

    /**
     * Creates a name-value pair.
     * Called from [.parseNameValuePair].
     *
     * @param name      the name
     * @param value     the value, or `null`
     *
     * @return  a name-value pair representing the arguments
     */
    protected fun createNameValuePair(name: String?, value: String?): NameValuePair {
        return BasicNameValuePair(name, value)
    }

    companion object {
        /**
         * A default instance of this class, for use as default or fallback.
         * Note that [BasicHeaderValueParser] is not a singleton, there
         * can be many instances of the class itself and of derived classes.
         * The instance here provides non-customized, default behavior.
         *
         */
        @Deprecated("(4.3) use {@link #INSTANCE}")
        val DEFAULT = BasicHeaderValueParser()
        val INSTANCE = BasicHeaderValueParser()
        private const val PARAM_DELIMITER = ';'
        private const val ELEM_DELIMITER = ','

        private val TOKEN_DELIMS: BitSet = TokenParser.Companion.INIT_BITSET('='.toInt(), PARAM_DELIMITER.toInt(), ELEM_DELIMITER.toInt())
        private val VALUE_DELIMS: BitSet = TokenParser.Companion.INIT_BITSET(PARAM_DELIMITER.toInt(), ELEM_DELIMITER.toInt())

        /**
         * Parses elements with the given parser.
         *
         * @param value     the header value to parse
         * @param parser    the parser to use, or `null` for default
         *
         * @return  array holding the header elements, never `null`
         * @throws ParseException in case of a parsing error
         */
        @Throws(ParseException::class)
        fun parseElements(value: String,
                          parser: HeaderValueParser?): Array<HeaderElement> {
            Args.notNull(value, "Value")
            val buffer = CharArrayBuffer(value.length)
            buffer.append(value)
            val cursor = ParserCursor(0, value.length)
            return (parser ?: INSTANCE).parseElements(buffer, cursor)
        }

        /**
         * Parses an element with the given parser.
         *
         * @param value     the header element to parse
         * @param parser    the parser to use, or `null` for default
         *
         * @return  the parsed header element
         */
        @Throws(ParseException::class)
        fun parseHeaderElement(value: String,
                               parser: HeaderValueParser?): HeaderElement? {
            Args.notNull(value, "Value")
            val buffer = CharArrayBuffer(value.length)
            buffer.append(value)
            val cursor = ParserCursor(0, value.length)
            return (parser ?: INSTANCE)
                    .parseHeaderElement(buffer, cursor)
        }

        /**
         * Parses parameters with the given parser.
         *
         * @param value     the parameter list to parse
         * @param parser    the parser to use, or `null` for default
         *
         * @return  array holding the parameters, never `null`
         */
        @Throws(ParseException::class)
        fun parseParameters(value: String,
                            parser: HeaderValueParser?): Array<NameValuePair> {
            Args.notNull(value, "Value")
            val buffer = CharArrayBuffer(value.length)
            buffer.append(value)
            val cursor = ParserCursor(0, value.length)
            return (parser ?: INSTANCE).parseParameters(buffer, cursor)
        }

        /**
         * Parses a name-value-pair with the given parser.
         *
         * @param value     the NVP to parse
         * @param parser    the parser to use, or `null` for default
         *
         * @return  the parsed name-value pair
         */
        @Throws(ParseException::class)
        fun parseNameValuePair(value: String,
                               parser: HeaderValueParser?): NameValuePair? {
            Args.notNull(value, "Value")
            val buffer = CharArrayBuffer(value.length)
            buffer.append(value)
            val cursor = ParserCursor(0, value.length)
            return (parser ?: INSTANCE)
                    .parseNameValuePair(buffer, cursor)
        }
    }

    init {
        tokenParser = TokenParser.Companion.INSTANCE
    }
}
