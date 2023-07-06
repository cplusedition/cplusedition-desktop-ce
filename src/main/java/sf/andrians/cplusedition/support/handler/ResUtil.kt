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

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.IBotResult
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.IResources
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StorageException
import sf.andrians.cplusedition.support.Support

class ResUtil(private val rsrc: IResources) : IResUtil {

    override fun getResources(): IResources {
        return rsrc
    }

    override fun get(stringid: Int): String {
        return rsrc.getString(stringid)
    }

    override fun get(stringid: Int, vararg args: String): String {
        var ret = rsrc.getString(stringid)
        if (args.isNotEmpty()) ret += args.joinToString("")
        return ret
    }

    override fun format(stringid: Int, vararg args: Any?): String {
        return rsrc.getFormatted(stringid, *args)
    }

    override fun jsonObjectError(ret: JSONObject, stringid: Int, vararg args: String): JSONObject {
        try {
            JSONUtil.clear(ret)
            ret.put(Key.errors, get(stringid, *args))
            return ret
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    override fun jsonObjectInvalidPath(path: String): JSONObject {
        val msg = rsrc.getString(R.string.InvalidPath_) + path
        return jsonobjecterror1(msg)
    }

    override fun jsonObjectNotFound(path: String): JSONObject {
        val msg = rsrc.getString(R.string.NotFound_) + path
        return jsonobjecterror1(msg)
    }

    override fun jsonObjectParametersInvalid(vararg names: String): JSONObject {
        val msg = rsrc.getString(R.string.ParametersInvalid)
        return jsonobjecterror1(if (names.isNotEmpty()) "$msg: ${names.joinToString(", ")}" else msg)
    }

    override fun jsonObjectError(stringid: Int, vararg args: String): JSONObject {
        return jsonobjecterror1(get(stringid, *args))
    }

    override fun jsonObjectError(msg: String?): JSONObject {
        return jsonobjecterror1(msg)
    }

    override fun jsonObjectError(e: Throwable?, msg: String): JSONObject {
        
        return jsonobjecterror1(msg)
    }

    override fun jsonObjectError(e: Throwable?, stringid: Int, vararg args: String): JSONObject {
        return jsonObjectError(e, get(stringid, *args))
    }

    override fun jsonObjectError(errors: JSONArray): JSONObject {
        return if (errors.length() == 0) JSONObject() else JSONObject().put(Key.errors, errors)
    }

    override fun jsonObjectError(errors: Collection<String>): JSONObject {
        return if (errors.isEmpty()) JSONObject() else jsonobject1(Key.errors, errors)
    }

    override fun <R> botObjectError(msg: String): IBotResult<R, JSONObject> {
        return BotResult.fail(jsonObjectError(msg))
    }

    override fun <R> botObjectError(msg: Collection<String>): IBotResult<R, JSONObject> {
        return BotResult.fail(jsonObjectError(msg))
    }

    override fun <R> botObjectError(id: Int, vararg args: String): IBotResult<R, JSONObject> {
        return BotResult.fail(jsonObjectError(id, *args))
    }

    override fun <R> botObjectError(e: Throwable?, msg: String): IBotResult<R, JSONObject> {
        return BotResult.fail(jsonObjectError(e, msg))
    }

    override fun <R> botObjectError(e: Throwable?, id: Int, vararg args: String): IBotResult<R, JSONObject> {
        return BotResult.fail(jsonObjectError(e, id, *args))
    }

    override fun jsonError(stringid: Int, vararg args: String): String {
        return jsonError(get(stringid, *args))
    }

    override fun jsonError(e: Throwable?, stringid: Int, vararg args: String): String {
        return jsonError(e, get(stringid, *args))
    }

    override fun jsonError(e: Throwable?, msg: String): String {
        
        return jsonError(msg)
    }

    override fun jsonError(errors: Collection<String>): String {
        return jsonObjectError(errors).toString()
    }

    override fun jsonError(msg: String): String {
        return JSONObject().put(Key.errors, msg).toString()
    }

    override fun jsonErrorInvalidPath(path: String): String {
        return jsonError(rsrc.getString(R.string.InvalidPath_) + path)
    }

    override fun jsonErrorNotFound(path: String): String {
        return jsonError(rsrc.getString(R.string.NotFound_) + path)
    }

    override fun jsonObjectWarning(stringid: Int, vararg args: String): JSONObject {
        return JSONObject().put(Key.warns, get(stringid, *args))
    }

    override fun jsonObjectWarning(warnings: Collection<String>): JSONObject {
        return if (warnings.isEmpty()) JSONObject() else jsonobject1(Key.warns, warnings)
    }

    override fun jsonResult(value: Boolean): String {
        return "{ \"" + Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: Int): String {
        return "{ \"" + Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: Long): String {
        return "{ \"" + Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: String): String {
        return JSONObject().put(Key.result, value).toString()
    }

    override fun jsonObjectResult(stringid: Int, vararg args: String): JSONObject {
        return JSONObject().put(Key.result, get(stringid, *args))
    }

    override fun jsonObjectResult(value: Boolean): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(value: Int): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(value: Long): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(value: String): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(value: JSONObject): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(value: JSONArray): JSONObject {
        return JSONObject().put(Key.result, value)
    }

    override fun jsonObjectResult(key: String, value: JSONObject): JSONObject {
        return JSONObject().put(key, value)
    }

    override fun ajaxError(serial: Long, stringid: Int, vararg args: String): String {
        return JSONObject().put(Key.serial, serial).put(Key.errors, get(stringid, *args)).toString()
    }

    override fun secureException(stringid: Int, vararg args: String): SecureException {
        return SecureException(get(stringid, *args))
    }

    override fun secureException(e: Throwable?, stringid: Int, vararg args: String): SecureException {
        if (e != null && e is SecureException) return e
        return SecureException(get(stringid, *args), e)
    }

    override fun storageException(stringid: Int, vararg args: String): StorageException {
        return StorageException(get(stringid, *args))
    }

    override fun storageException(e: Throwable?, stringid: Int, vararg args: String): StorageException {
        if (e != null && e is StorageException) return e
        return StorageException(get(stringid, *args), e)
    }

    override fun secureException(e: Throwable?, msg: String): SecureException {
        if (e != null && e is SecureException) return e
        return SecureException(msg, e)
    }

    override fun storageException(e: Throwable?, msg: String): StorageException {
        if (e != null && e is StorageException) return e
        return StorageException(msg, e)
    }

    override fun actionOK(stringid: Int): String {
        return format(R.string.Action_OK, get(stringid))
    }

    override fun actionCancelled(stringid: Int): String {
        return format(R.string.Action_Cancelled, get(stringid))
    }

    override fun actionFailed(stringid: Int): String {
        return format(R.string.Action_Failed, get(stringid))
    }

    private fun jsonobjecterror1(value: Any?): JSONObject {
        return try {
            val ret = JSONObject()
            if (value != null) ret.put(Key.errors, value)
            ret
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    private fun jsonobject1(key: String, values: Collection<String>): JSONObject {
        return try {
            JSONObject().put(key, JSONArray(values))
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }
}
