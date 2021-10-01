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
import sf.andrians.org.apache.http.util.Args
import sf.andrians.org.apache.http.util.CharArrayBuffer

/**
 * Basic implementation for formatting header value elements.
 * Instances of this class are stateless and thread-safe.
 * Derived classes are expected to maintain these properties.
 *
 * @since 4.0
 */
class BasicHeaderValueFormatter : HeaderValueFormatter {
    override fun formatElements(charBuffer: CharArrayBuffer?,
                                elems: Array<HeaderElement>,
                                quote: Boolean): CharArrayBuffer {
        Args.notNull(elems, "Header element array")
        val len = estimateElementsLen(elems)
        var buffer = charBuffer
        if (buffer == null) {
            buffer = CharArrayBuffer(len)
        } else {
            buffer.ensureCapacity(len)
        }
        for (i in elems.indices) {
            if (i > 0) {
                buffer.append(", ")
            }
            formatHeaderElement(buffer, elems[i], quote)
        }
        return buffer
    }

    /**
     * Estimates the length of formatted header elements.
     *
     * @param elems     the header elements to format, or `null`
     *
     * @return  a length estimate, in number of characters
     */
    protected fun estimateElementsLen(elems: Array<HeaderElement>?): Int {
        if (elems == null || elems.size < 1) {
            return 0
        }
        var result = (elems.size - 1) * 2 // elements separated by ", "
        for (elem in elems) {
            result += estimateHeaderElementLen(elem)
        }
        return result
    }

    override fun formatHeaderElement(charBuffer: CharArrayBuffer?,
                                     elem: HeaderElement,
                                     quote: Boolean): CharArrayBuffer {
        Args.notNull(elem, "Header element")
        val len = estimateHeaderElementLen(elem)
        var buffer = charBuffer
        if (buffer == null) {
            buffer = CharArrayBuffer(len)
        } else {
            buffer.ensureCapacity(len)
        }
        buffer.append(elem.name)
        val value = elem.value
        if (value != null) {
            buffer.append('=')
            doFormatValue(buffer, value, quote)
        }
        val parcnt = elem.parameterCount
        if (parcnt > 0) {
            for (i in 0 until parcnt) {
                buffer.append("; ")
                formatNameValuePair(buffer, elem.getParameter(i), quote)
            }
        }
        return buffer
    }

    /**
     * Estimates the length of a formatted header element.
     *
     * @param elem      the header element to format, or `null`
     *
     * @return  a length estimate, in number of characters
     */
    protected fun estimateHeaderElementLen(elem: HeaderElement?): Int {
        if (elem == null) {
            return 0
        }
        var result = elem.name.length // name
        val value = elem.value
        if (value != null) {
            result += 3 + value.length // ="value"
        }
        val parcnt = elem.parameterCount
        if (parcnt > 0) {
            for (i in 0 until parcnt) {
                result += 2 +  // ; <param>
                        estimateNameValuePairLen(elem.getParameter(i))
            }
        }
        return result
    }

    override fun formatParameters(charBuffer: CharArrayBuffer?,
                                  nvps: Array<NameValuePair>,
                                  quote: Boolean): CharArrayBuffer {
        Args.notNull(nvps, "Header parameter array")
        val len = estimateParametersLen(nvps)
        var buffer = charBuffer
        if (buffer == null) {
            buffer = CharArrayBuffer(len)
        } else {
            buffer.ensureCapacity(len)
        }
        for (i in nvps.indices) {
            if (i > 0) {
                buffer.append("; ")
            }
            formatNameValuePair(buffer, nvps[i], quote)
        }
        return buffer
    }

    /**
     * Estimates the length of formatted parameters.
     *
     * @param nvps      the parameters to format, or `null`
     *
     * @return  a length estimate, in number of characters
     */
    protected fun estimateParametersLen(nvps: Array<NameValuePair>?): Int {
        if (nvps == null || nvps.size < 1) {
            return 0
        }
        var result = (nvps.size - 1) * 2 // "; " between the parameters
        for (nvp in nvps) {
            result += estimateNameValuePairLen(nvp)
        }
        return result
    }

    override fun formatNameValuePair(charBuffer: CharArrayBuffer?,
                                     nvp: NameValuePair,
                                     quote: Boolean): CharArrayBuffer {
        Args.notNull(nvp, "Name / value pair")
        val len = estimateNameValuePairLen(nvp)
        var buffer = charBuffer
        if (buffer == null) {
            buffer = CharArrayBuffer(len)
        } else {
            buffer.ensureCapacity(len)
        }
        buffer.append(nvp.name)
        val value = nvp.value
        if (value != null) {
            buffer.append('=')
            doFormatValue(buffer, value, quote)
        }
        return buffer
    }

    /**
     * Estimates the length of a formatted name-value pair.
     *
     * @param nvp       the name-value pair to format, or `null`
     *
     * @return  a length estimate, in number of characters
     */
    protected fun estimateNameValuePairLen(nvp: NameValuePair?): Int {
        if (nvp == null) {
            return 0
        }
        var result = nvp.name.length // name
        val value = nvp.value
        if (value != null) {
            result += 3 + value.length // ="value"
        }
        return result
    }

    /**
     * Actually formats the value of a name-value pair.
     * This does not include a leading = character.
     * Called from [formatNameValuePair][.formatNameValuePair].
     *
     * @param buffer    the buffer to append to, never `null`
     * @param value     the value to append, never `null`
     * @param quote     `true` to always format with quotes,
     * `false` to use quotes only when necessary
     */
    protected fun doFormatValue(buffer: CharArrayBuffer,
                                value: String,
                                quote: Boolean) {
        var quoteFlag = quote
        if (!quoteFlag) {
            var i = 0
            while (i < value.length && !quoteFlag) {
                quoteFlag = isSeparator(value[i])
                i++
            }
        }
        if (quoteFlag) {
            buffer.append('"')
        }
        for (i in 0 until value.length) {
            val ch = value[i]
            if (isUnsafe(ch)) {
                buffer.append('\\')
            }
            buffer.append(ch)
        }
        if (quoteFlag) {
            buffer.append('"')
        }
    }

    /**
     * Checks whether a character is a [separator][.SEPARATORS].
     *
     * @param ch        the character to check
     *
     * @return  `true` if the character is a separator,
     * `false` otherwise
     */
    protected fun isSeparator(ch: Char): Boolean {
        return SEPARATORS.indexOf(ch) >= 0
    }

    /**
     * Checks whether a character is [unsafe][.UNSAFE_CHARS].
     *
     * @param ch        the character to check
     *
     * @return  `true` if the character is unsafe,
     * `false` otherwise
     */
    protected fun isUnsafe(ch: Char): Boolean {
        return UNSAFE_CHARS.indexOf(ch) >= 0
    }

    companion object {
        /**
         * A default instance of this class, for use as default or fallback.
         * Note that [BasicHeaderValueFormatter] is not a singleton, there
         * can be many instances of the class itself and of derived classes.
         * The instance here provides non-customized, default behavior.
         *
         */
        @Deprecated("(4.3) use {@link #INSTANCE}")
        val DEFAULT = BasicHeaderValueFormatter()
        val INSTANCE = BasicHeaderValueFormatter()

        /**
         * Special characters that can be used as separators in HTTP parameters.
         * These special characters MUST be in a quoted string to be used within
         * a parameter value .
         */
        const val SEPARATORS = " ;,:@()<>\\\"/[]?={}\t"

        /**
         * Unsafe special characters that must be escaped using the backslash
         * character
         */
        const val UNSAFE_CHARS = "\"\\"

        /**
         * Formats an array of header elements.
         *
         * @param elems     the header elements to format
         * @param quote     `true` to always format with quoted values,
         * `false` to use quotes only when necessary
         * @param formatter         the formatter to use, or `null`
         * for the [default][.INSTANCE]
         *
         * @return  the formatted header elements
         */
        fun formatElements(elems: Array<HeaderElement>,
                           quote: Boolean,
                           formatter: HeaderValueFormatter?): String {
            return (formatter ?: INSTANCE)
                    .formatElements(null, elems, quote).toString()
        }

        /**
         * Formats a header element.
         *
         * @param elem      the header element to format
         * @param quote     `true` to always format with quoted values,
         * `false` to use quotes only when necessary
         * @param formatter         the formatter to use, or `null`
         * for the [default][.INSTANCE]
         *
         * @return  the formatted header element
         */
        fun formatHeaderElement(elem: HeaderElement,
                                quote: Boolean,
                                formatter: HeaderValueFormatter?): String {
            return (formatter ?: INSTANCE)
                    .formatHeaderElement(null, elem, quote).toString()
        }

        /**
         * Formats a set of parameters.
         *
         * @param nvps      the parameters to format
         * @param quote     `true` to always format with quoted values,
         * `false` to use quotes only when necessary
         * @param formatter         the formatter to use, or `null`
         * for the [default][.INSTANCE]
         *
         * @return  the formatted parameters
         */
        fun formatParameters(nvps: Array<NameValuePair>,
                             quote: Boolean,
                             formatter: HeaderValueFormatter?): String {
            return (formatter ?: INSTANCE)
                    .formatParameters(null, nvps, quote).toString()
        }

        /**
         * Formats a name-value pair.
         *
         * @param nvp       the name-value pair to format
         * @param quote     `true` to always format with a quoted value,
         * `false` to use quotes only when necessary
         * @param formatter         the formatter to use, or `null`
         * for the [default][.INSTANCE]
         *
         * @return  the formatted name-value pair
         */
        fun formatNameValuePair(nvp: NameValuePair,
                                quote: Boolean,
                                formatter: HeaderValueFormatter?): String {
            return (formatter ?: INSTANCE)
                    .formatNameValuePair(null, nvp, quote).toString()
        }
    }
} // class BasicHeaderValueFormatter
