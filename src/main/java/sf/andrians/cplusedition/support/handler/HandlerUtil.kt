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
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.Support
import java.util.*

object HandlerUtil {
    fun stringVar(format: String, vararg args: String): String {
        var format1 = format
        var i = 0
        val len = args.size
        while (i < len) {
            format1 = format1.replace("\${$i}", args[i])
            ++i
        }
        return format1
    }

    fun jsonObjectError(msg: String?): JSONObject {
        return jsonObject(An.Key.errors, msg)
    }

    fun jsonObjectErrors(msgs: List<String?>): JSONObject {
        return when (msgs.size) {
            0 -> JSONObject()
            1 -> jsonObject(An.Key.errors, msgs[0])
            else -> jsonObject(An.Key.errors, msgs)
        }
    }

    fun jsonObjectResult(result: Boolean): JSONObject {
        try {
            return jsonObject(An.Key.result, result)
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    fun jsonObjectResult(result: String?): JSONObject {
        try {
            return jsonObject(An.Key.result, result)
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    fun jsonObject(key: String, result: Any?): JSONObject {
        return try {
            JSONObject().put(key, result)
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    fun jsonResult(value: Boolean): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    fun jsonResult(value: Int): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    fun jsonResult(value: Long): String {
        return "{ \"" + An.Key.result + "\" : " + value.toString() + " }"
    }

    fun jsonResult(key: String, result: Any?): String {
        return try {
            JSONObject().put(key, result).toString()
        } catch (e: JSONException) {
            throw AssertionError()
        }
    }

    fun jsonResult(value: String?): String {
        return jsonValue(An.Key.result, value)
    }

    fun jsonError(msg: String?): String {
        return jsonValue(An.Key.errors, msg)
    }

    fun jsonError(msg: String, e: Throwable?): String {
        
        return jsonValue(An.Key.errors, msg)
    }

    fun jsonErrors(vararg msgs: String?): String {
        return jsonErrors(Arrays.asList(*msgs))
    }

    fun jsonErrors(msgs: Collection<String?>?): String {
        return JSONObject().put(An.Key.errors, JSONArray(msgs)).toString()
    }

    @Throws(JSONException::class)
    fun jsonError(ret: JSONObject, msg: String?) {
        JSONUtil.clear(ret)
        ret.put(An.Key.errors, msg)
    }

    @Throws(JSONException::class)
    fun jsonErrors(ret: JSONObject, msgs: Collection<String?>?) {
        JSONUtil.clear(ret)
        ret.put(An.Key.errors, JSONArray(msgs))
    }

    fun jsonValue(key: String, value: Long): String {
        return JSONObject().put(key, value).toString()
    }

    fun jsonValue(key: String, value: String?): String {
        val ret = JSONObject();
        if (value == null) ret.put(key, JSONObject.NULL) else ret.put(key, value)
        return ret.toString()
    }

    fun jsonValues(key: String, msgs: Collection<String?>): String {
        return JSONObject().put(key, JSONArray(msgs)).toString()
    }

    fun jsonValues(key: String, values: Array<String?>): String {
        return JSONObject().put(key, JSONArray(values)).toString()
    }

    fun ajaxError(serial: Long, msg: String?): String {
        return ajaxValue(serial, An.Key.errors, msg)
    }

    fun ajaxResult(serial: Long, result: String?): String {
        return ajaxValue(serial, An.Key.result, result)
    }

    private fun ajaxValue(serial: Long, key: String, value: String?): String {
        val ret = JSONObject().put(An.Key.serial, serial);
        if (value == null) ret.put(key, JSONObject.NULL) else ret.put(key, value)
        return ret.toString()
    }

}
