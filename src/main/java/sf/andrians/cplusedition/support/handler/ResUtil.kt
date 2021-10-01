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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IResources
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StorageException
import sf.andrians.cplusedition.support.Support

class ResUtil(private val rsrc: IResources) : IResUtil {

    override fun getResources(): IResources {
        return rsrc
    }

    override fun get(id: Int, vararg args: String): String {
        var ret = rsrc.getString(id)
        if (args.isNotEmpty()) ret += args.joinToString("")
        return ret
    }

    override fun format(id: Int, vararg args: Any?): String {
        return rsrc.getFormatted(id, *args)
    }

    override fun jsonObjectError(ret: JSONObject, id: Int, vararg args: String): JSONObject {
        try {
            JSONUtil.clear(ret)
            ret.put(An.Key.errors, get(id, *args))
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

    override fun jsonObjectError(key: Int, vararg args: String): JSONObject {
        return jsonobjecterror1(get(key, *args))
    }

    override fun jsonObjectError(e: Throwable?, key: Int, vararg args: String): JSONObject {
        val msg = get(key, *args)
        
        return jsonobjecterror1(msg)
    }

    override fun jsonObjectError(errors: Collection<String>): JSONObject {
        return when (errors.size) {
            0 -> JSONObject().put(An.Key.errors, rsrc.getString(R.string.Error))
            else -> jsonobject1(An.Key.errors, errors)
        }
    }

    override fun jsonError(key: Int, vararg args: String): String {
        return jsonError(get(key, *args))
    }

    override fun jsonError(e: Throwable?, key: Int, vararg args: String): String {
        val msg = get(key, *args)
        
        return jsonError(msg)
    }

    override fun jsonError(errors: Collection<String>): String {
        return jsonObjectError(errors).toString()
    }

    override fun jsonError(msg: String): String {
        return JSONObject().put(An.Key.errors, msg).toString()
    }

    override fun jsonErrorInvalidPath(path: String): String {
        return jsonError(rsrc.getString(R.string.InvalidPath_) + path)
    }

    override fun jsonErrorNotFound(path: String): String {
        return jsonError(rsrc.getString(R.string.NotFound_) + path)
    }

    override fun jsonObjectWarning(warnings: Collection<String>): JSONObject {
        return jsonobject1(An.Key.warns, warnings)
    }

    override fun jsonResult(value: Boolean): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: Int): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: Long): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    override fun jsonResult(value: String): String {
        return JSONObject().put(An.Key.result, value).toString()
    }

    override fun jsonObjectResult(value: Boolean): JSONObject {
        return JSONObject().put(An.Key.result, value)
    }

    override fun jsonObjectResult(value: Int): JSONObject {
        return JSONObject().put(An.Key.result, value)
    }

    override fun jsonObjectResult(value: Long): JSONObject {
        return JSONObject().put(An.Key.result, value)
    }

    override fun jsonObjectResult(value: String): JSONObject {
        return JSONObject().put(An.Key.result, value)
    }

    override fun jsonObjectResult(key: String, value: JSONObject): JSONObject {
        return JSONObject().put(key, value)
    }

    override fun ajaxError(serial: Long, id: Int, vararg args: String): String {
        return JSONObject().put(An.Key.serial, serial).put(An.Key.errors, get(id, *args)).toString()
    }

    override fun secureException(id: Int, vararg args: String): SecureException {
        return SecureException(get(id, *args))
    }

    override fun secureException(e: Throwable?, id: Int, vararg args: String): SecureException {
        if (e != null && e is SecureException) return e
        return SecureException(get(id, *args), e)
    }

    override fun storageException(id: Int, vararg args: String): StorageException {
        return StorageException(get(id, *args))
    }

    override fun storageException(e: Throwable?, id: Int, vararg args: String): StorageException {
        if (e != null && e is StorageException) return e
        return StorageException(get(id, *args), e)
    }

    private fun jsonobjecterror1(value: Any?): JSONObject {
        return try {
            JSONObject().put(An.Key.errors, value)
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
