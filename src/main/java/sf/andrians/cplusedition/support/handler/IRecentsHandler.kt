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

import org.json.JSONArray
import org.json.JSONObject

interface IRecentsHandler {
    @Throws(Exception::class)
    fun handleRecents(cmd: Int): String

    /// @param cpath Context relative path with or without leading /.
    fun recentsPut(navigation: Int, cpath: String, state: JSONObject?)

    @Throws(Exception::class)
    fun recentsSave(state: JSONObject)
    fun recentsRestore(recents: JSONArray?)
}
