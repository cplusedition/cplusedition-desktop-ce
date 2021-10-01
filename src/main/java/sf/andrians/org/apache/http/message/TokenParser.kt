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

import sf.andrians.org.apache.http.util.CharArrayBuffer
import java.util.*

/**
 * Low level parser for header field elements. The parsing routines of this class are designed
 * to produce near zero intermediate garbage and make no intermediate copies of input data.
 *
 *
 * This class is immutable and thread safe.
 *
 * @since 4.4
 */
class TokenParser {
    /**
     * Extracts from the sequence of chars a token terminated with any of the given delimiters
     * discarding semantically insignificant whitespace characters.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be `null` if the token
     * is not delimited by any character.
     */
    fun parseToken(buf: CharArrayBuffer, cursor: ParserCursor, delimiters: BitSet?): String {
        val dst = StringBuilder()
        var whitespace = false
        while (!cursor.atEnd()) {
            val current = buf[cursor.pos]
            whitespace = if (delimiters != null && delimiters[current.toInt()]) {
                break
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor)
                true
            } else {
                if (whitespace && dst.length > 0) {
                    dst.append(' ')
                }
                copyContent(buf, cursor, delimiters, dst)
                false
            }
        }
        return dst.toString()
    }

    /**
     * Extracts from the sequence of chars a value which can be enclosed in quote marks and
     * terminated with any of the given delimiters discarding semantically insignificant
     * whitespace characters.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be `null` if the value
     * is not delimited by any character.
     */
    fun parseValue(buf: CharArrayBuffer, cursor: ParserCursor, delimiters: BitSet?): String {
        val dst = StringBuilder()
        var whitespace = false
        while (!cursor.atEnd()) {
            val current = buf[cursor.pos]
            whitespace = if (delimiters != null && delimiters[current.toInt()]) {
                break
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor)
                true
            } else if (current == DQUOTE) {
                if (whitespace && dst.length > 0) {
                    dst.append(' ')
                }
                copyQuotedContent(buf, cursor, dst)
                false
            } else {
                if (whitespace && dst.length > 0) {
                    dst.append(' ')
                }
                copyUnquotedContent(buf, cursor, delimiters, dst)
                false
            }
        }
        return dst.toString()
    }

    /**
     * Skips semantically insignificant whitespace characters and moves the cursor to the closest
     * non-whitespace character.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     */
    fun skipWhiteSpace(buf: CharArrayBuffer, cursor: ParserCursor) {
        var pos = cursor.pos
        val indexFrom = cursor.pos
        val indexTo = cursor.upperBound
        for (i in indexFrom until indexTo) {
            val current = buf[i]
            if (!isWhitespace(current)) {
                break
            }
            pos++
        }
        cursor.updatePos(pos)
    }

    /**
     * Transfers content into the destination buffer until a whitespace character or any of
     * the given delimiters is encountered.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be `null` if the value
     * is delimited by a whitespace only.
     * @param dst destination buffer
     */
    fun copyContent(buf: CharArrayBuffer, cursor: ParserCursor, delimiters: BitSet?,
                    dst: StringBuilder) {
        var pos = cursor.pos
        val indexFrom = cursor.pos
        val indexTo = cursor.upperBound
        for (i in indexFrom until indexTo) {
            val current = buf[i]
            if (delimiters != null && delimiters[current.toInt()] || isWhitespace(current)) {
                break
            }
            pos++
            dst.append(current)
        }
        cursor.updatePos(pos)
    }

    /**
     * Transfers content into the destination buffer until a whitespace character,  a quote,
     * or any of the given delimiters is encountered.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param delimiters set of delimiting characters. Can be `null` if the value
     * is delimited by a whitespace or a quote only.
     * @param dst destination buffer
     */
    fun copyUnquotedContent(buf: CharArrayBuffer, cursor: ParserCursor,
                            delimiters: BitSet?, dst: StringBuilder) {
        var pos = cursor.pos
        val indexFrom = cursor.pos
        val indexTo = cursor.upperBound
        for (i in indexFrom until indexTo) {
            val current = buf[i]
            if (delimiters != null && delimiters[current.toInt()]
                    || isWhitespace(current) || current == DQUOTE) {
                break
            }
            pos++
            dst.append(current)
        }
        cursor.updatePos(pos)
    }

    /**
     * Transfers content enclosed with quote marks into the destination buffer.
     *
     * @param buf buffer with the sequence of chars to be parsed
     * @param cursor defines the bounds and current position of the buffer
     * @param dst destination buffer
     */
    fun copyQuotedContent(buf: CharArrayBuffer, cursor: ParserCursor,
                          dst: StringBuilder) {
        if (cursor.atEnd()) {
            return
        }
        var pos = cursor.pos
        var indexFrom = cursor.pos
        val indexTo = cursor.upperBound
        var current = buf[pos]
        if (current != DQUOTE) {
            return
        }
        pos++
        indexFrom++
        var escaped = false
        var i = indexFrom
        while (i < indexTo) {
            current = buf[i]
            if (escaped) {
                if (current != DQUOTE && current != ESCAPE) {
                    dst.append(ESCAPE)
                }
                dst.append(current)
                escaped = false
            } else {
                if (current == DQUOTE) {
                    pos++
                    break
                }
                if (current == ESCAPE) {
                    escaped = true
                } else if (current != CR && current != LF) {
                    dst.append(current)
                }
            }
            i++
            pos++
        }
        cursor.updatePos(pos)
    }

    companion object {
        fun INIT_BITSET(vararg b: Int): BitSet {
            val bitset = BitSet()
            for (aB in b) {
                bitset.set(aB)
            }
            return bitset
        }

        /** US-ASCII CR, carriage return (13)  */
        const val CR = '\r'

        /** US-ASCII LF, line feed (10)  */
        const val LF = '\n'

        /** US-ASCII SP, space (32)  */
        const val SP = ' '

        /** US-ASCII HT, horizontal-tab (9)  */
        const val HT = '\t'

        /** Double quote  */
        const val DQUOTE = '\"'

        /** Backward slash / escape character  */
        const val ESCAPE = '\\'
        fun isWhitespace(ch: Char): Boolean {
            return ch == SP || ch == HT || ch == CR || ch == LF
        }

        val INSTANCE = TokenParser()
    }
}
