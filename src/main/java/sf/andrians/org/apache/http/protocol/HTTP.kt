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
package sf.andrians.org.apache.http.protocol

import sf.andrians.org.apache.http.Consts

/**
 * Constants and static helpers related to the HTTP protocol.
 *
 * @since 4.0
 */
object HTTP {
    const val CR = 13 // <US-ASCII CR, carriage return (13)>
    const val LF = 10 // <US-ASCII LF, linefeed (10)>
    const val SP = 32 // <US-ASCII SP, space (32)>
    const val HT = 9 // <US-ASCII HT, horizontal-tab (9)>

    /** HTTP header definitions  */
    const val TRANSFER_ENCODING = "Transfer-Encoding"
    const val CONTENT_LEN = "Content-Length"
    const val CONTENT_TYPE = "Content-Type"
    const val CONTENT_ENCODING = "Content-Encoding"
    const val EXPECT_DIRECTIVE = "Expect"
    const val CONN_DIRECTIVE = "Connection"
    const val TARGET_HOST = "Host"
    const val USER_AGENT = "User-Agent"
    const val DATE_HEADER = "Date"
    const val SERVER_HEADER = "Server"

    /** HTTP expectations  */
    const val EXPECT_CONTINUE = "100-continue"

    /** HTTP connection control  */
    const val CONN_CLOSE = "Close"
    const val CONN_KEEP_ALIVE = "Keep-Alive"

    /** Transfer encoding definitions  */
    const val CHUNK_CODING = "chunked"
    const val IDENTITY_CODING = "identity"
    val DEF_CONTENT_CHARSET = Consts.ISO_8859_1
    val DEF_PROTOCOL_CHARSET = Consts.ASCII

    @Deprecated("(4.2)")
    val UTF_8 = "UTF-8"

    @Deprecated("(4.2)")
    val UTF_16 = "UTF-16"

    @Deprecated("(4.2)")
    val US_ASCII = "US-ASCII"

    @Deprecated("(4.2)")
    val ASCII = "ASCII"

    @Deprecated("(4.2)")
    val ISO_8859_1 = "ISO-8859-1"

    @Deprecated("(4.2)")
    val DEFAULT_CONTENT_CHARSET = ISO_8859_1

    @Deprecated("(4.2)")
    val DEFAULT_PROTOCOL_CHARSET = US_ASCII

    @Deprecated("(4.2)")
    val OCTET_STREAM_TYPE = "application/octet-stream"

    @Deprecated("(4.2)")
    val PLAIN_TEXT_TYPE = "text/plain"

    @Deprecated("(4.2)")
    val CHARSET_PARAM = "; charset="

    @Deprecated("(4.2)")
    val DEFAULT_CONTENT_TYPE = OCTET_STREAM_TYPE
    fun isWhitespace(ch: Char): Boolean {
        return ch.toInt() == SP || ch.toInt() == HT || ch.toInt() == CR || ch.toInt() == LF
    }
}