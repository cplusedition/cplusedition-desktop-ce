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
package sf.andrians.org.apache.http

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * An entity that can be sent or received with an HTTP message.
 * Entities can be found in some
 * [requests][HttpEntityEnclosingRequest] and in
 * [responses][HttpResponse], where they are optional.
 *
 *
 * There are three distinct types of entities in HttpCore,
 * depending on where their [content][.getContent] originates:
 *
 *  * **streamed**: The content is received from a stream, or
 * generated on the fly. In particular, this category includes
 * entities being received from a [connection][HttpConnection].
 * [Streamed][.isStreaming] entities are generally not
 * [repeatable][.isRepeatable].
 *
 *  * **self-contained**: The content is in memory or obtained by
 * means that are independent from a connection or other entity.
 * Self-contained entities are generally [repeatable][.isRepeatable].
 *
 *  * **wrapping**: The content is obtained from another entity.
 *
 *
 * This distinction is important for connection management with incoming
 * entities. For entities that are created by an application and only sent
 * using the HTTP components framework, the difference between streamed
 * and self-contained is of little importance. In that case, it is suggested
 * to consider non-repeatable entities as streamed, and those that are
 * repeatable (without a huge effort) as self-contained.
 *
 * @since 4.0
 */
interface HttpEntity {
    /**
     * Tells if the entity is capable of producing its data more than once.
     * A repeatable entity's getContent() and writeTo(OutputStream) methods
     * can be called more than once whereas a non-repeatable entity's can not.
     * @return true if the entity is repeatable, false otherwise.
     */
    val isRepeatable: Boolean

    /**
     * Tells about chunked encoding for this entity.
     * The primary purpose of this method is to indicate whether
     * chunked encoding should be used when the entity is sent.
     * For entities that are received, it can also indicate whether
     * the entity was received with chunked encoding.
     *
     *
     * The behavior of wrapping entities is implementation dependent,
     * but should respect the primary purpose.
     *
     *
     * @return  `true` if chunked encoding is preferred for this
     * entity, or `false` if it is not
     */
    val isChunked: Boolean

    /**
     * Tells the length of the content, if known.
     *
     * @return  the number of bytes of the content, or
     * a negative number if unknown. If the content length is known
     * but exceeds [Long.MAX_VALUE][java.lang.Long.MAX_VALUE],
     * a negative number is returned.
     */
    val contentLength: Long

    /**
     * Obtains the Content-Type header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity. It can include a
     * charset attribute.
     *
     * @return  the Content-Type header for this entity, or
     * `null` if the content type is unknown
     */
    val contentType: Header?

    /**
     * Obtains the Content-Encoding header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity.
     * Wrapping entities that modify the content encoding should
     * adjust this header accordingly.
     *
     * @return  the Content-Encoding header for this entity, or
     * `null` if the content encoding is unknown
     */
    val contentEncoding: Header?

    /**
     * Returns a content stream of the entity.
     * [Repeatable][.isRepeatable] entities are expected
     * to create a new instance of [InputStream] for each invocation
     * of this method and therefore can be consumed multiple times.
     * Entities that are not [repeatable][.isRepeatable] are expected
     * to return the same [InputStream] instance and therefore
     * may not be consumed more than once.
     *
     *
     * IMPORTANT: Please note all entity implementations must ensure that
     * all allocated resources are properly deallocated after
     * the [InputStream.close] method is invoked.
     *
     * @return content stream of the entity.
     *
     * @throws IOException if the stream could not be created
     * @throws UnsupportedOperationException
     * if entity content cannot be represented as [java.io.InputStream].
     *
     * @see .isRepeatable
     */
    @get:Throws(IOException::class, UnsupportedOperationException::class)
    val content: InputStream?

    /**
     * Writes the entity content out to the output stream.
     *
     *
     * IMPORTANT: Please note all entity implementations must ensure that
     * all allocated resources are properly deallocated when this method
     * returns.
     *
     * @param outStream the output stream to write entity content to
     *
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun writeTo(outStream: OutputStream?)

    /**
     * Tells whether this entity depends on an underlying stream.
     * Streamed entities that read data directly from the socket should
     * return `true`. Self-contained entities should return
     * `false`. Wrapping entities should delegate this call
     * to the wrapped entity.
     *
     * @return  `true` if the entity content is streamed,
     * `false` otherwise
     */ 
    val isStreaming: Boolean

    /**
     * This method is deprecated since version 4.1. Please use standard
     * java convention to ensure resource deallocation by calling
     * [InputStream.close] on the input stream returned by
     * [.getContent]
     *
     *
     * This method is called to indicate that the content of this entity
     * is no longer required. All entity implementations are expected to
     * release all allocated resources as a result of this method
     * invocation. Content streaming entities are also expected to
     * dispose of the remaining content, if any. Wrapping entities should
     * delegate this call to the wrapped entity.
     *
     *
     * This method is of particular importance for entities being
     * received from a [connection][HttpConnection]. The entity
     * needs to be consumed completely in order to re-use the connection
     * with keep-alive.
     *
     * @throws IOException if an I/O error occurs.
     *
     * @see .getContent
     */
    @Deprecated("""(4.1) Use {@link org.apache.http.util.EntityUtils#consume(HttpEntity)}
     
      """)
    @Throws(IOException::class)
    fun consumeContent()
}
