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
package sf.andrians.cplusedition.support.handler

import org.json.JSONObject

interface IFilepickerHandler {

    @Throws(Exception::class)
    fun handle(cmd: Int, params: JSONObject): JSONObject
    fun listDir(ret: JSONObject, cpath: String): JSONObject

    class ThumbnailResult {
        var error: String? = null
            private set
        var width = 0
            private set
        var height = 0
            private set
        var dataUrl: String? = null
            private set

        constructor(error: String?) {
            this.error = error
        }

        constructor(width: Int, height: Int, base64: String?) {
            this.width = width
            this.height = height
            dataUrl = base64
        }

    }

    interface IThumbnailCallback {
        /**
         * @param cpath Context relative path to locate the full size image.
         */
        @Throws(Exception::class)
        fun getThumbnail(cpath: String, lastmodified: Long, tnsize: Int): ThumbnailResult
    }
}