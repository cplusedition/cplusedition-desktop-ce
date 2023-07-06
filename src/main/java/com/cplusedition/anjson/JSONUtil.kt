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
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package com.cplusedition.anjson

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.Writer
import java.util.*
import java.util.regex.Pattern

object JSONUtil {

    /// JSONObject extensions

    fun <R> JSONObject.maps(callback: (key: String) -> R): Sequence<R> {
        return this.keys().asSequence().map(callback)
    }

    fun <R> JSONObject.mapsNotNull(callback: (key: String) -> R?): Sequence<R> {
        return this.keys().asSequence().mapNotNull(callback)
    }

    fun JSONObject.foreach(callback: (key: String) -> Unit) {
        for (key in keys()) {
            callback(key)
        }
    }

    fun <T> JSONObject.finds(callback: (key: String) -> T): T? {
        for (key in keys()) {
            val ret = callback(key)
            if (ret != null) return ret
        }
        return null
    }

    fun <T> JSONObject.accum(intial: T, callback: (ret: T, key: String) -> T): T {
        var ret = intial
        for (key in keys()) {
            ret = callback(ret, key)
        }
        return ret
    }

    fun <T> JSONObject.mapsJSONObjectOrNull(callback: (value: JSONObject?) -> T): Sequence<T> {
        return this.maps { key -> callback(this.jsonObjectOrNull(key)) }
    }

    fun <T> JSONObject.mapsJSONArrayOrNull(callback: (value: JSONArray?) -> T): Sequence<T> {
        return this.maps { key -> callback(this.jsonArrayOrNull(key)) }
    }

    fun <T> JSONObject.mapsStringOrNull(callback: (value: String?) -> T): Sequence<T> {
        return this.maps { key -> callback(this.stringOrNull(key)) }
    }

    fun <T> JSONObject.mapsJSONObjectNotNull(callback: (value: JSONObject) -> T): Sequence<T> {
        return this.mapsNotNull { key -> this.jsonObjectOrNull(key)?.let { callback(it) } }
    }

    fun <T> JSONObject.mapsJSONArrayNotNull(callback: (value: JSONArray) -> T): Sequence<T> {
        return this.mapsNotNull { key -> this.jsonArrayOrNull(key)?.let { callback(it) } }
    }

    fun <T> JSONObject.mapsStringNotNull(callback: (value: String) -> T): Sequence<T> {
        return this.mapsNotNull { key -> this.stringOrNull(key)?.let { callback(it) } }
    }

    fun JSONObject.foreachJSONObjectOrNull(callback: (key: String, value: JSONObject?) -> Unit) {
        this.foreach { key ->
            callback(key, this.jsonObjectOrNull(key))
        }
    }

    fun JSONObject.foreachJSONArrayOrNull(callback: (key: String, value: JSONArray?) -> Unit) {
        this.foreach { key ->
            callback(key, this.jsonArrayOrNull(key))
        }
    }

    fun JSONObject.foreachStringOrNull(callback: (key: String, value: String?) -> Unit) {
        this.foreach { key ->
            callback(key, this.stringOrNull(key))
        }
    }

    fun JSONObject.foreachJSONObjectNotNull(callback: (key: String, value: JSONObject) -> Unit) {
        this.foreach { key ->
            this.jsonObjectOrNull(key)?.let { callback(key, it) }
        }
    }

    fun JSONObject.foreachJSONArrayNotNull(callback: (key: String, value: JSONArray) -> Unit) {
        this.foreach { key ->
            this.jsonArrayOrNull(key)?.let { callback(key, it) }
        }
    }

    fun JSONObject.foreachStringNotNull(callback: (key: String, value: String) -> Unit) {
        this.foreach { key ->
            this.stringOrNull(key)?.let { callback(key, it) }
        }
    }

    fun <T> JSONObject.findsJSONObjectOrNull(callback: (key: String, value: JSONObject?) -> T): T? {
        return this.finds { key ->
            callback(key, this.jsonObjectOrNull(key))
        }
    }

    fun <T> JSONObject.findsJSONArrayOrNull(callback: (key: String, value: JSONArray?) -> T): T? {
        return this.finds { key ->
            callback(key, this.jsonArrayOrNull(key))
        }
    }

    fun <T> JSONObject.findsStringOrNull(callback: (key: String, value: String?) -> T): T? {
        return this.finds { key ->
            callback(key, this.stringOrNull(key))
        }
    }

    fun <T> JSONObject.findsJSONObjectNotNull(callback: (key: String, value: JSONObject) -> T): T? {
        return finds { key ->
            this.jsonObjectOrNull(key)?.let { callback(key, it) }
        }
    }

    fun <T> JSONObject.findsJSONArrayNotNull(callback: (key: String, value: JSONArray) -> T): T? {
        return finds { key ->
            this.jsonArrayOrNull(key)?.let { callback(key, it) }
        }
    }

    fun <T> JSONObject.findsStringNotNull(callback: (key: String, value: String) -> T): T? {
        return finds { key ->
            this.stringOrNull(key)?.let { callback(key, it) }
        }
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

    fun JSONObject.putJSONArray(key: String): JSONArray {
        return JSONArray().also { this.put(key, it) }
    }

    fun JSONObject.putJSONObject(key: String): JSONObject {
        return JSONObject().also { this.put(key, it) }
    }

    /**
     * Put v to a JSONArray at key. If JSONArray at key not exists, create one.
     */
    fun <V> JSONObject.putToJSONArrayAt(key: String, value: V): JSONObject {
        (optJSONArray(key) ?: putJSONArray(key)).put(value)
        return this
    }

    /**
     * Put k, v to a JSONObject at key. If JSONObject at key not exists, create one.
     */
    fun <V> JSONObject.putToJSONObjectAt(key: String, k: String, v: V): JSONObject {
        (optJSONObject(key) ?: putJSONObject(key)).put(k, v)
        return this
    }

    /** Llike optString() but returns def when result is null or not exists. */
    fun JSONObject.stringOrDef(key: String, def: String): String {
        return jsonString(this.opt(key) ?: JSONObject.NULL) ?: def
    }

    /** Llike optString() but returns null instead of "" as def. */
    fun JSONObject.stringOrNull(key: String): String? {
        return jsonString(this.opt(key) ?: JSONObject.NULL)
    }

    /** @return JSONArray at given key as Sequence<String> or empty list if getJSONArray() failed. */
    fun JSONObject.stringSequenceOrEmpty(key: String, def: String = ""): Sequence<String> {
        val array = this.jsonArrayOrNull(key) ?: return sequenceOf()
        return array.maps { array.stringOrDef(it, def) }
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

    fun <R> JSONArray.maps(callback: (index: Int) -> R): Sequence<R> {
        return (0 until this.length()).asSequence().map(callback)
    }

    fun <R> JSONArray.mapsNotNull(callback: (index: Int) -> R?): Sequence<R> {
        return (0 until this.length()).asSequence().mapNotNull(callback)
    }

    fun JSONArray.foreach(callback: (index: Int) -> Unit) {
        for (index in 0 until length()) {
            callback(index)
        }
    }

    fun <T> JSONArray.finds(callback: (index: Int) -> T): T? {
        for (index in 0 until length()) {
            val ret = callback(index)
            if (ret != null) return ret
        }
        return null
    }

    fun <T> JSONArray.accum(initial: T, callback: (ret: T, index: Int) -> T): T {
        var ret = initial
        for (index in 0 until length()) {
            ret = callback(ret, index)
        }
        return ret
    }

    fun <T> JSONArray.mapsJSONObjectOrNull(callback: (value: JSONObject?) -> T): Sequence<T> {
        return this.maps { callback(this.jsonObjectOrNull(it)) }
    }

    fun <T> JSONArray.mapsJSONArrayOrNull(callback: (value: JSONArray?) -> T): Sequence<T> {
        return this.maps { callback(this.jsonArrayOrNull(it)) }
    }

    fun <T> JSONArray.mapsStringOrNull(callback: (value: String?) -> T): Sequence<T> {
        return this.maps { callback(this.stringOrNull(it)) }
    }

    fun <T> JSONArray.mapsJSONObjectNotNull(callback: (value: JSONObject) -> T): Sequence<T> {
        return this.mapsNotNull { index -> this.jsonObjectOrNull(index)?.let { callback(it) } }
    }

    fun <T> JSONArray.mapsJSONArrayNotNull(callback: (value: JSONArray) -> T): Sequence<T> {
        return this.mapsNotNull { index -> this.jsonArrayOrNull(index)?.let { callback(it) } }
    }

    fun <T> JSONArray.mapsStringNotNull(callback: (value: String) -> T): Sequence<T> {
        return this.mapsNotNull { index -> this.stringOrNull(index)?.let { callback(it) } }
    }

    fun JSONArray.foreachJSONObjectOrNull(callback: (index: Int, value: JSONObject?) -> Unit) {
        this.foreach { index ->
            callback(index, this.jsonObjectOrNull(index))
        }
    }

    fun JSONArray.foreachJSONArrayOrNull(callback: (index: Int, value: JSONArray?) -> Unit) {
        this.foreach { index ->
            callback(index, this.jsonArrayOrNull(index))
        }
    }

    fun JSONArray.foreachStringOrNull(callback: (index: Int, value: String?) -> Unit) {
        this.foreach { index ->
            callback(index, this.stringOrNull(index))
        }
    }

    fun JSONArray.foreachJSONObjectNotNull(callback: (index: Int, value: JSONObject) -> Unit) {
        this.foreach { index ->
            this.jsonObjectOrNull(index)?.let { callback(index, it) }
        }
    }

    fun JSONArray.foreachJSONArrayNotNull(callback: (index: Int, value: JSONArray) -> Unit) {
        this.foreach { index ->
            this.jsonArrayOrNull(index)?.let { callback(index, it) }
        }
    }

    fun JSONArray.foreachStringNotNull(callback: (index: Int, value: String) -> Unit) {
        this.foreach { index ->
            this.stringOrNull(index)?.let { callback(index, it) }
        }
    }

    fun <T> JSONArray.findsJSONObjectOrNull(callback: (index: Int, value: JSONObject?) -> T): T? {
        return this.finds { index ->
            callback(index, this.jsonObjectOrNull(index))
        }
    }

    fun <T> JSONArray.findsJSONArrayOrNull(callback: (index: Int, value: JSONArray?) -> T): T? {
        return this.finds { index ->
            callback(index, this.jsonArrayOrNull(index))
        }
    }

    fun <T> JSONArray.findsStringOrNull(callback: (index: Int, value: String?) -> T): T? {
        return this.finds { index ->
            callback(index, this.stringOrNull(index))
        }
    }

    fun <T> JSONArray.findsJSONObjectNotNull(callback: (index: Int, value: JSONObject) -> T): T? {
        return this.finds { index ->
            this.jsonObjectOrNull(index)?.let { callback(index, it) }
        }
    }

    fun <T> JSONArray.findsJSONArrayNotNull(callback: (index: Int, value: JSONArray) -> T): T? {
        return this.finds { index ->
            this.jsonArrayOrNull(index)?.let { callback(index, it) }
        }
    }

    fun <T> JSONArray.findsStringNotNull(callback: (index: Int, value: String) -> T): T? {
        return this.finds { index ->
            this.stringOrNull(index)?.let { callback(index, it) }
        }
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
        return jsonString(this.opt(index) ?: JSONObject.NULL) ?: def
    }

    /** Llike optString() but returns null instead of "" as def and does not throw JSONException. */
    fun JSONArray.stringOrNull(index: Int): String? {
        return jsonString(this.opt(index) ?: JSONObject.NULL)
    }

    fun JSONArray.putJSONArray(): JSONArray {
        return JSONArray().also { this.put(it) }
    }

    fun JSONArray.putJSONObject(): JSONObject {
        return JSONObject().also { this.put(it) }
    }

    /// Util methods

    fun clear(ret: JSONObject) {
        for (key in ret.keys()) {
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

    fun write(writer: Writer, a: JSONObject, tab: Int = 0, indent: Int = 0): Writer {
        Serializer(writer).write(a, tab, indent)
        return writer
    }

    fun write(writer: Writer, a: JSONArray, tab: Int = 0, indent: Int = 0): Writer {
        Serializer(writer).write(a, tab, indent)
        return writer
    }

    private class Serializer(
        private val writer: Writer,
    ) {
        companion object {
            private val NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
        }

        /**
         * Write the contents of the JSONObject as JSON text to a writer.
         *
         *
         * If `indentFactor > 0` and the [JSONObject]
         * has only one key, then the object will be output on a single line:
         * <pre>`{"key": 1}`</pre>
         *
         *
         * If an object has 2 or more keys, then it will be output across
         * multiple lines: ``<pre>{
         * "key1": 1,
         * "key2": "value 2",
         * "key3": 3
         * }</pre>
         *
         * **
         * Warning: This method assumes that the data structure is acyclical.
         ** *
         *
         * @param tab The number of spaces to add to each level of indentation.
         * @param indent       The indentation of the top level.
         * @return The writer.
         * @throws JSONException
         */
        @Throws(JSONException::class)
        fun write(a: JSONObject, tab: Int, indent: Int) {
            return try {
                var commanate = false
                val length: Int = a.length()
                writer.write('{'.code)
                if (length == 1) {
                    a.foreach { key ->
                        writer.write(JSONObject.quote(key))
                        writer.write(':'.code)
                        if (tab > 0) {
                            writer.write(' '.code)
                        }
                        try {
                            writeValue(a.get(key), tab, indent)
                        } catch (e: java.lang.Exception) {
                            throw JSONException("Unable to write JSONObject value for key: $key", e)
                        }
                    }
                } else if (length != 0) {
                    val newindent = indent + tab
                    a.foreach { key ->
                        if (commanate) {
                            writer.write(','.code)
                        }
                        if (tab > 0) {
                            writer.write('\n'.code)
                        }
                        indent(newindent)
                        writer.write(JSONObject.quote(key))
                        writer.write(':'.code)
                        if (tab > 0) {
                            writer.write(' '.code)
                        }
                        try {
                            writeValue(a.get(key), tab, newindent)
                        } catch (e: java.lang.Exception) {
                            throw JSONException("Unable to write JSONObject value for key: $key", e)
                        }
                        commanate = true
                    }
                    if (tab > 0) {
                        writer.write('\n'.code)
                    }
                    indent(indent)
                }
                writer.write('}'.code)
            } catch (exception: IOException) {
                throw JSONException(exception)
            }
        }

        /**
         * Write the contents of the JSONArray as JSON text to a writer.
         *
         *
         * If `indentFactor > 0` and the [JSONArray] has only
         * one element, then the array will be output on a single line:
         * <pre>`[1]`</pre>
         *
         *
         * If an array has 2 or more elements, then it will be output across
         * multiple lines: <pre>`[
         * 1,
         * "value 2",
         * 3
         * ]
        `</pre> *
         *
         * **
         * Warning: This method assumes that the data structure is acyclical.
         ** *
         *
         * @param tab The number of spaces to add to each level of indentation.
         * @param indent       The indentation of the top level.
         * @return The writer.
         * @throws JSONException
         */
        @Throws(JSONException::class)
        fun write(a: JSONArray, tab: Int, indent: Int) {
            return try {
                var commanate = false
                val length: Int = a.length()
                writer.write('['.code)
                if (length == 1) {
                    try {
                        writeValue(a.get(0), tab, indent)
                    } catch (e: java.lang.Exception) {
                        throw JSONException("Unable to write JSONArray value at index: 0", e)
                    }
                } else if (length != 0) {
                    val newindent = indent + tab
                    var i = 0
                    while (i < length) {
                        if (commanate) {
                            writer.write(','.code)
                        }
                        if (tab > 0) {
                            writer.write('\n'.code)
                        }
                        indent(newindent)
                        try {
                            writeValue(a.get(i), tab, newindent)
                        } catch (e: java.lang.Exception) {
                            throw JSONException("Unable to write JSONArray value at index: $i", e)
                        }
                        commanate = true
                        i += 1
                    }
                    if (tab > 0) {
                        writer.write('\n'.code)
                    }
                    indent(indent)
                }
                writer.write(']'.code)
            } catch (e: IOException) {
                throw JSONException(e)
            }
        }

        @Throws(JSONException::class, IOException::class)
        private fun writeValue(value: Any?, tab: Int, indent: Int) {
            if (value == null || value == JSONObject.NULL) {
                writer.write("null")
            } else if (value is Number) {
                val numberAsString = JSONObject.numberToString(value)
                if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
                    writer.write(numberAsString)
                } else {
                    writer.write(JSONObject.quote(numberAsString))
                }
            } else if (value is Boolean) {
                writer.write(value.toString())
            } else if (value is Enum<*>) {
                writer.write(JSONObject.quote(value.name))
            } else if (value is JSONObject) {
                write(value, tab, indent)
            } else if (value is JSONArray) {
                write(value, tab, indent)
            } else if (value is Map<*, *>) {
                write(JSONObject(value), tab, indent)
            } else if (value is Collection<*>) {
                write(JSONArray(value), tab, indent)
            } else if (value.javaClass.isArray) {
                write(JSONArray(value), tab, indent)
            } else {
                writer.write(JSONObject.quote(value.toString()))
            }
        }

        @Throws(IOException::class)
        private fun indent(indent: Int) {
            var i = 0
            while (i < indent) {
                writer.write(' '.code)
                i += 1
            }
        }
    }
}
