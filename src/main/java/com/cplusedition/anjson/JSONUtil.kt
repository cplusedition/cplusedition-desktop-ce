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
package com.cplusedition.anjson

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object JSONUtil {

    /// JSONObject extensions

    fun <R> JSONObject.map(callback: (key: String) -> R): Sequence<R> {
        return this.keys().asSequence().map(callback)
    }

    fun JSONObject.foreach(callback: (key: String) -> Unit) {
        for (key in keys().asSequence()) {
            callback(key)
        }
    }

    fun JSONObject.foreachJSONObject(callback: (key: String, value: JSONObject?) -> Unit) {
        foreach { key ->
            callback(key, this.jsonObjectOrNull(key))
        }
    }

    fun JSONObject.foreachJSONArray(callback: (key: String, value: JSONArray?) -> Unit) {
        foreach { key ->
            callback(key, this.jsonArrayOrNull(key))
        }
    }

    fun JSONObject.foreachString(callback: (key: String, value: String?) -> Unit) {
        foreach { key ->
            callback(key, this.stringOrNull(key))
        }
    }

    fun <T> JSONObject.find(callback: (key: String) -> T?): T? {
        for (key in keys().asSequence()) {
            return callback(key) ?: continue
        }
        return null
    }

    fun <T> JSONObject.findJSONObject(callback: (key: String, value: JSONObject?) -> T?): T? {
        for (key in keys().asSequence()) {
            return callback(key, this.jsonObjectOrNull(key)) ?: continue
        }
        return null
    }

    fun <T> JSONObject.findJSONArray(callback: (key: String, value: JSONArray?) -> T?): T? {
        for (key in keys().asSequence()) {
            return callback(key, this.jsonArrayOrNull(key)) ?: continue
        }
        return null
    }

    fun <T> JSONObject.findString(callback: (key: String, value: String?) -> T?): T? {
        for (key in keys().asSequence()) {
            return callback(key, this.stringOrNull(key)) ?: continue
        }
        return null
    }

    fun JSONObject.keyList(): List<String> {
        return this.keys().asSequence().toList()
    }

    /**LIke put(key, value) but fail on JSONException. */
    fun JSONObject.putOrFail(key: String, value: Any?): JSONObject {
        try {
            return put(key, value)
        } catch (e: JSONException) {
            throw AssertionError(e)
        }
    }

    fun JSONObject.putJSONObjectOrFail(key: String): JSONObject {
        val a = JSONObject()
        try {
            this.put(key, a)
        } catch (e: JSONException) {
            throw AssertionError(e)
        }
        return a
    }

    fun JSONObject.putJSONArrayOrFail(key: String): JSONArray {
        val a = JSONArray()
        try {
            this.put(key, a)
        } catch (e: JSONException) {
            throw AssertionError(e)
        }
        return a
    }

    /** Llike optString() but returns def when result is null or not exists. */
    fun JSONObject.stringOrDef(key: String, def: String): String {
        return jsonString(this.opt(key)) ?: def
    }

    /** Llike optString() but returns null instead of "" as def. */
    fun JSONObject.stringOrNull(key: String): String? {
        return jsonString(this.opt(key))
    }

    /** @return JSONArray at given key as List<String> or empty list if getJSONArray() failed. */
    fun JSONObject.stringListOrEmpty(key: String): List<String> {
        val array = this.jsonArrayOrNull(key) ?: return listOf()
        return array.toStringList("")
    }

    /** @return JSONObject at given key as Map<String, String> or null. */
    fun JSONObject.stringMapOrNull(key: String): Map<String, String>? {
        val o = this.jsonObjectOrNull(key) ?: return null
        val ret = TreeMap<String, String>()
        o.foreach { k ->
            val value = o.stringOrNull(k)
            if (value != null) ret[k] = value
        }
        return ret
    }

    /** Like getJSONObject() but return null instead of throwing JSONException. */
    fun JSONObject.jsonObjectOrNull(key: String): JSONObject? {
        try {
            return this.getJSONObject(key)
        } catch (e: JSONException) {
            return null
        }
    }

    fun JSONObject.jsonObjectOrFail(key: String): JSONObject {
        return this.jsonObjectOrNull(key) ?: throw AssertionError();
    }

    /** Like getJSONArray() but return null instead of throwing JSONException. */
    fun JSONObject.jsonArrayOrNull(key: String): JSONArray? {
        try {
            return this.getJSONArray(key);
        } catch (e: JSONException) {
            return null
        }
    }

    fun JSONObject.jsonArrayOrFail(key: String): JSONArray {
        return this.jsonArrayOrNull(key) ?: throw AssertionError()
    }

    /// JSONArray extensions

    fun <R> JSONArray.map(callback: (index: Int) -> R): List<R> {
        return (0 until this.length()).map(callback)
    }

    fun JSONArray.foreach(callback: (index: Int) -> Unit) {
        for (index in 0 until length()) {
            callback(index)
        }
    }

    fun JSONArray.foreachJSONObject(callback: (index: Int, value: JSONObject?) -> Unit) {
        this.foreach { index ->
            callback(index, this.jsonObjectOrNull(index))
        }
    }

    fun JSONArray.foreachJSONArray(callback: (index: Int, value: JSONArray?) -> Unit) {
        this.foreach { index ->
            callback(index, this.jsonArrayOrNull(index))
        }
    }

    fun JSONArray.foreachString(callback: (index: Int, value: String?) -> Unit) {
        this.foreach { index ->
            callback(index, this.stringOrNull(index))
        }
    }

    fun <T> JSONArray.find(callback: (index: Int) -> T?): T? {
        for (index in 0 until length()) {
            return callback(index) ?: continue
        }
        return null
    }

    fun <T> JSONArray.findJSONObject(callback: (index: Int, value: JSONObject?) -> T?): T? {
        for (index in 0 until length()) {
            return callback(index, this.jsonObjectOrNull(index)) ?: continue
        }
        return null
    }

    fun <T> JSONArray.findJSONArray(callback: (index: Int, value: JSONArray?) -> T?): T? {
        for (index in 0 until length()) {
            return callback(index, this.jsonArrayOrNull(index)) ?: continue
        }
        return null
    }

    fun <T> JSONArray.findString(callback: (index: Int, value: String?) -> T?): T? {
        for (index in 0 until length()) {
            return callback(index, this.stringOrNull(index)) ?: continue
        }
        return null
    }

    /** Llike getJSONObject() but return null instead of throwing JSONException. */
    fun JSONArray.jsonObjectOrNull(index: Int): JSONObject? {
        try {
            return this.getJSONObject(index)
        } catch (e: Exception) {
            return null
        }
    }

    /** Llike getJSONArray() but return null instead of throwing JSONException. */
    fun JSONArray.jsonArrayOrNull(index: Int): JSONArray? {
        try {
            return this.getJSONArray(index)
        } catch (e: Exception) {
            return null
        }
    }

    /** Llike optString() but returns def when result is null or not exists and does not throw JSONException. */
    fun JSONArray.stringOrDef(index: Int, def: String): String {
        return jsonString(this.opt(index)) ?: def
    }

    /** Llike optString() but returns null instead of "" as def and does not throw JSONException. */
    fun JSONArray.stringOrNull(index: Int): String? {
        return jsonString(this.opt(index))
    }

    fun JSONArray.toJSONObjectOrNullList(): List<JSONObject?> {
        val len = this.length()
        val ret = ArrayList<JSONObject?>(len)
        var i = 0
        while (i < len) {
            ret.add(this.jsonObjectOrNull(i++))
        }
        return ret
    }

    fun JSONArray.toStringOrNullList(): List<String?> {
        val len = this.length()
        val ret = ArrayList<String?>(len)
        var i = 0
        while (i < len) {
            ret.add(this.stringOrNull(i++))
        }
        return ret
    }

    fun <T : MutableList<String?>> JSONArray.toStringOrNullList(ret: T): T {
        var i = 0
        val len = this.length()
        while (i < len) {
            ret.add(this.stringOrNull(i))
            ++i
        }
        return ret
    }

    fun JSONArray.toStringList(def: String): List<String> {
        val len = this.length()
        val ret = ArrayList<String>(len)
        var i = 0
        while (i < len) {
            ret.add(this.stringOrDef(i++, def))
        }
        return ret
    }

    /** LIke toStringList() but fail and return null instead of using def on invalid value. */
    fun JSONArray.toStringListOrNull(): List<String>? {
        val len = this.length()
        val ret = ArrayList<String>(len)
        var i = 0
        while (i < len) {
            val value = this.stringOrNull(i++) ?: return null
            ret.add(value)
        }
        return ret
    }

    /** LIke toStringList() but fail instead of using def on invalid value. */
    fun JSONArray.toStringListOrFail(): List<String> {
        return toStringListOrNull() ?: throw AssertionError()
    }

    fun <T : MutableList<String>> JSONArray.toStringList(ret: T, def: String): T {
        var i = 0
        val len = this.length()
        while (i < len) {
            ret.add(this.stringOrDef(i, def))
            ++i
        }
        return ret
    }

    fun JSONArray.toIntList(def: Int): List<Int> {
        val len = this.length()
        val ret = ArrayList<Int>(len)
        var i = 0
        while (i < len) {
            ret.add(this.optInt(i++, def))
        }
        return ret
    }

    /// Util methods

    fun clear(ret: JSONObject) {
        for (key in ret.keys().asSequence().toList()) {
            ret.remove(key)
        }
    }

    /** Shadow copoy of given JSONObject. */
    fun copy(a: JSONObject): JSONObject {
        return JSONObject(a, a.keys().asSequence().toList().toTypedArray())
    }

    fun jsonObjectOrNull(text: String): JSONObject? {
        try {
            return JSONObject(text)
        } catch (e: Exception) {
            return null
        }
    }

    fun jsonObjectOrFail(text: String): JSONObject {
        return this.jsonObjectOrNull(text) ?: throw AssertionError()
    }

    fun jsonArrayOrNull(text: String): JSONArray? {
        try {
            return JSONArray(text)
        } catch (e: Exception) {
            return null
        }
    }

    fun jsonArrayOrFail(text: String): JSONArray {
        return this.jsonArrayOrNull(text) ?: throw AssertionError()
    }

    fun jsonString(value: Any?): String? {
        return if (value === JSONObject.NULL) {
            null
        } else if (value is String) {
            value
        } else {
            value?.toString()
        }
    }

}
