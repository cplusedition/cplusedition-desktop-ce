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

import com.cplusedition.bot.core.IBotResult
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.support.IResources
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StorageException

interface IResUtil {
    fun getResources(): IResources
    fun get(stringid: Int): String
    fun get(stringid: Int, vararg args: String): String
    fun format(stringid: Int, vararg args: Any?): String
    fun jsonObjectError(ret: JSONObject, stringid: Int, vararg args: String): JSONObject
    fun jsonObjectError(stringid: Int, vararg args: String): JSONObject
    fun jsonObjectError(msg: String?): JSONObject
    fun jsonObjectError(e: Throwable?, stringid: Int, vararg args: String): JSONObject
    fun jsonObjectError(e: Throwable?, msg: String): JSONObject
    fun jsonObjectError(errors: Collection<String>): JSONObject
    fun jsonObjectError(errors: JSONArray): JSONObject
    fun jsonObjectInvalidPath(path: String): JSONObject
    fun jsonObjectNotFound(path: String): JSONObject
    fun jsonObjectParametersInvalid(vararg names: String): JSONObject
    fun jsonObjectResult(stringid: Int, vararg args: String): JSONObject
    fun jsonObjectResult(value: Boolean): JSONObject
    fun jsonObjectResult(value: Int): JSONObject
    fun jsonObjectResult(value: Long): JSONObject
    fun jsonObjectResult(value: String): JSONObject
    fun jsonObjectResult(value: JSONObject): JSONObject
    fun jsonObjectResult(value: JSONArray): JSONObject
    fun jsonObjectResult(key: String, value: JSONObject): JSONObject
    fun jsonObjectWarning(stringid: Int, vararg args: String): JSONObject
    fun jsonObjectWarning(warnings: Collection<String>): JSONObject
    fun jsonError(msg: String): String
    fun jsonError(stringid: Int, vararg args: String): String
    fun jsonError(e: Throwable?, stringid: Int, vararg args: String): String
    fun jsonError(e: Throwable?, msg: String): String
    fun jsonErrorInvalidPath(path: String): String
    fun jsonErrorNotFound(path: String): String
    fun jsonError(errors: Collection<String>): String
    fun jsonResult(value: Boolean): String
    fun jsonResult(value: Int): String
    fun jsonResult(value: Long): String
    fun jsonResult(value: String): String
    fun secureException(stringid: Int, vararg args: String): SecureException
    fun secureException(e: Throwable?, stringid: Int, vararg args: String): SecureException
    fun secureException(e: Throwable?, msg: String): SecureException
    fun storageException(stringid: Int, vararg args: String): StorageException
    fun storageException(e: Throwable?, stringid: Int, vararg args: String): StorageException
    fun storageException(e: Throwable?, msg: String): StorageException
    fun ajaxError(serial: Long, stringid: Int, vararg args: String): String
    fun actionOK(stringid: Int): String
    fun actionCancelled(stringid: Int): String
    fun actionFailed(stringid: Int): String
    fun <R> botObjectError(msg: String): IBotResult<R, JSONObject>
    fun <R> botObjectError(msg: Collection<String>): IBotResult<R, JSONObject>
    fun <R> botObjectError(id: Int, vararg args: String): IBotResult<R, JSONObject>
    fun <R> botObjectError(e: Throwable?, msg: String): IBotResult<R, JSONObject>
    fun <R> botObjectError(e: Throwable?, id: Int, vararg args: String): IBotResult<R, JSONObject>
}