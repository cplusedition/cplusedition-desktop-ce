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

/**
 * Signals a parse error.
 * Parse errors when receiving a message will typically trigger
 * [ProtocolException]. Parse errors that do not occur during
 * protocol execution may be handled differently.
 * This is an unchecked exception, since there are cases where
 * the data to be parsed has been generated and is therefore
 * known to be parseable.
 *
 * @since 4.0
 */
class ParseException : RuntimeException {
    /**
     * Creates a [ParseException] without details.
     */
    constructor() : super() {}

    /**
     * Creates a [ParseException] with a detail message.
     *
     * @param message the exception detail message, or `null`
     */
    constructor(message: String?) : super(message) {}

    companion object {
        private const val serialVersionUID = -7288819855864183578L
    }
}