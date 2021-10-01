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
import sf.andrians.org.apache.http.util.CharArrayBuffer

/**
 * Interface for parsing header values into elements.
 * Instances of this interface are expected to be stateless and thread-safe.
 *
 * @since 4.0
 */
interface HeaderValueParser {
    /**
     * Parses a header value into elements.
     * Parse errors are indicated as `RuntimeException`.
     *
     *
     * Some HTTP headers (such as the set-cookie header) have values that
     * can be decomposed into multiple elements. In order to be processed
     * by this parser, such headers must be in the following form:
     *
     * <pre>
     * header  = [ element ] *( "," [ element ] )
     * element = name [ "=" [ value ] ] *( ";" [ param ] )
     * param   = name [ "=" [ value ] ]
     *
     * name    = token
     * value   = ( token | quoted-string )
     *
     * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
     * white space&gt;
     * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
     * text          = any char except &lt;"&gt;
     * quoted-char   = "\" char
    </pre> *
     *
     *
     * Any amount of white space is allowed between any part of the
     * header, element or param and is ignored. A missing value in any
     * element or param will be stored as the empty [String];
     * if the "=" is also missing <var>null</var> will be stored instead.
     *
     *
     *
     * Note that this parser does not apply to list-typed HTTP header fields in
     * general; it is only suitable for fields that use the syntax described
     * above. Counter-examples are "Link" (RFC 8288), "If-None-Match" (RFC 7232)
     * or "Dav" (RFC 4918).
     *
     *
     * @param buffer    buffer holding the header value to parse
     * @param cursor    the parser cursor containing the current position and
     * the bounds within the buffer for the parsing operation
     *
     * @return  an array holding all elements of the header value
     *
     * @throws ParseException        in case of a parsing error
     */
    @Throws(ParseException::class)
    fun parseElements(
            buffer: CharArrayBuffer,
            cursor: ParserCursor): Array<HeaderElement>

    /**
     * Parses a single header element.
     * A header element consist of a semicolon-separate list
     * of name=value definitions.
     *
     * @param buffer    buffer holding the element to parse
     * @param cursor    the parser cursor containing the current position and
     * the bounds within the buffer for the parsing operation
     *
     * @return  the parsed element
     *
     * @throws ParseException        in case of a parse error
     */
    @Throws(ParseException::class)
    fun parseHeaderElement(
            buffer: CharArrayBuffer,
            cursor: ParserCursor): HeaderElement

    /**
     * Parses a list of name-value pairs.
     * These lists are used to specify parameters to a header element.
     * Parse errors are indicated as `ParseException`.
     *
     * @param buffer    buffer holding the name-value list to parse
     * @param cursor    the parser cursor containing the current position and
     * the bounds within the buffer for the parsing operation
     *
     * @return  an array holding all items of the name-value list
     *
     * @throws ParseException        in case of a parse error
     */
    @Throws(ParseException::class)
    fun parseParameters(
            buffer: CharArrayBuffer,
            cursor: ParserCursor): Array<NameValuePair>

    /**
     * Parses a name=value specification, where the = and value are optional.
     *
     * @param buffer    the buffer holding the name-value pair to parse
     * @param cursor    the parser cursor containing the current position and
     * the bounds within the buffer for the parsing operation
     *
     * @return  the name-value pair, where the value is `null`
     * if no value is specified
     */
    @Throws(ParseException::class)
    fun parseNameValuePair(
            buffer: CharArrayBuffer,
            cursor: ParserCursor): NameValuePair
}