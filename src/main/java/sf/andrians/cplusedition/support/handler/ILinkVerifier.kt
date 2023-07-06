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

interface ILinkVerifier {
    object Cmd {
        const val INVALID = 0
        const val LINKINFOS = 1
        const val LINKINFO = 2
        private val names = arrayOf(
            "INVALID",
            "LINKINFOS",
            "LINKINFO"
        )

        fun toString(cmd: Int): String {
            return if (cmd < 0 || cmd >= names.size) {
                names[INVALID]
            } else names[cmd]
        }
    }

    fun handle(cmd: Int, data: JSONObject): JSONObject
}
