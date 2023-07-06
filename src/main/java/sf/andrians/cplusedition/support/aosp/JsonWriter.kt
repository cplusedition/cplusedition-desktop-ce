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
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sf.andrians.cplusedition.support.aosp

import com.cplusedition.bot.core.TextUt
import java.io.Closeable
import java.io.IOException
import java.io.Writer
import java.util.*

/**
 * Writes a JSON ([RFC 4627](http://www.ietf.org/rfc/rfc4627.txt))
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new `JsonWriter`. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 *
 *  * To write **arrays**, first call [.beginArray].
 * Write each of the array's elements with the appropriate [.value]
 * methods or by nesting other arrays and objects. Finally close the array
 * using [.endArray].
 *  * To write **objects**, first call [.beginObject].
 * Write each of the object's properties by alternating calls to
 * [.name] with the property's value. Write property values with the
 * appropriate [.value] method or by nesting other objects or arrays.
 * Finally close the object using [.endObject].
 *
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> `[
 * {
 * "id": 912345678901,
 * "text": "How do I write JSON on Android?",
 * "geo": null,
 * "user": {
 * "name": "android_newb",
 * "followers_count": 41
 * }
 * },
 * {
 * "id": 912345678902,
 * "text": "@android_newb just use android.util.JsonWriter!",
 * "geo": [50.454722, -104.606667],
 * "user": {
 * "name": "jesse",
 * "followers_count": 2
 * }
 * }
 * ]`</pre>
 * This code encodes the above structure: <pre>   `public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 * JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 * writer.setIndent("  ");
 * writeMessagesArray(writer, messages);
 * writer.close();
 * }
 *
 * public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 * writer.beginArray();
 * for (Message message : messages) {
 * writeMessage(writer, message);
 * }
 * writer.endArray();
 * }
 *
 * public void writeMessage(JsonWriter writer, Message message) throws IOException {
 * writer.beginObject();
 * writer.name("id").value(message.getId());
 * writer.name("text").value(message.getText());
 * if (message.getGeo() != null) {
 * writer.name("geo");
 * writeDoublesArray(writer, message.getGeo());
 * } else {
 * writer.name("geo").nullValue();
 * }
 * writer.name("user");
 * writeUser(writer, message.getUser());
 * writer.endObject();
 * }
 *
 * public void writeUser(JsonWriter writer, User user) throws IOException {
 * writer.beginObject();
 * writer.name("name").value(user.getName());
 * writer.name("followers_count").value(user.getFollowersCount());
 * writer.endObject();
 * }
 *
 * public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 * writer.beginArray();
 * for (Double value : doubles) {
 * writer.value(value);
 * }
 * writer.endArray();
 * }`</pre>
 *
 *
 * Each `JsonWriter` may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an [IllegalStateException].
 */
class JsonWriter(out: Writer?) : Closeable {
    /** The output data, containing at most one top-level array or object.  */
    private val out: Writer
    private val stack: MutableList<JsonScope> = ArrayList()

    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private var indent: String? = null

    /**
     * The name/value separator; either ":" or ": ".
     */
    private var separator = ":"
    /**
     * Returns true if this writer has relaxed syntax rules.
     */
    /**
     * Configure this writer to relax its syntax rules. By default, this writer
     * only emits well-formed JSON as specified by [RFC 4627](http://www.ietf.org/rfc/rfc4627.txt). Setting the writer
     * to lenient permits the following:
     *
     *  * Top-level values of any type. With strict writing, the top-level
     * value must be an object or an array.
     *  * Numbers may be [NaNs][Double.isNaN] or [       ][Double.isInfinite].
     *
     */
    var isLenient = false

    /**
     * Sets the indentation string to be repeated for each level of indentation
     * in the encoded document. If `indent.isEmpty()` the encoded document
     * will be compact. Otherwise the encoded document will be more
     * human-readable.
     *
     * @param indent a string containing only whitespace.
     */
    fun setIndent(indent: String) {
        if (indent.isEmpty()) {
            this.indent = null
            separator = ":"
        } else {
            this.indent = indent
            separator = ": "
        }
    }

    /**
     * Begins encoding a new array. Each call to this method must be paired with
     * a call to [.endArray].
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun beginArray(): JsonWriter {
        return open(JsonScope.EMPTY_ARRAY, "[")
    }

    /**
     * Ends encoding the current array.
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun endArray(): JsonWriter {
        return close(JsonScope.EMPTY_ARRAY, JsonScope.NONEMPTY_ARRAY, "]")
    }

    /**
     * Begins encoding a new object. Each call to this method must be paired
     * with a call to [.endObject].
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun beginObject(): JsonWriter {
        return open(JsonScope.EMPTY_OBJECT, "{")
    }

    /**
     * Ends encoding the current object.
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun endObject(): JsonWriter {
        return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}")
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    @Throws(IOException::class)
    private fun open(empty: JsonScope, openBracket: String): JsonWriter {
        beforeValue(true)
        stack.add(empty)
        out.write(openBracket)
        return this
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    @Throws(IOException::class)
    private fun close(empty: JsonScope, nonempty: JsonScope, closeBracket: String): JsonWriter {
        val context = peek()
        check(!(context != nonempty && context != empty)) { "Nesting problem: $stack" }
        stack.removeAt(stack.size - 1)
        if (context == nonempty) {
            newline()
        }
        out.write(closeBracket)
        return this
    }

    /**
     * Returns the value on the top of the stack.
     */
    private fun peek(): JsonScope {
        return stack[stack.size - 1]
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private fun replaceTop(topOfStack: JsonScope) {
        stack[stack.size - 1] = topOfStack
    }

    /**
     * Encodes the property name.
     *
     * @param name the name of the forthcoming value. May not be null.
     * @return this writer.
     */
    @Throws(IOException::class)
    fun name(name: String?): JsonWriter {
        if (name == null) {
            throw NullPointerException("name == null")
        }
        beforeName()
        string(name)
        return this
    }

    /**
     * Encodes `value`.
     *
     * @param value the literal string value, or null to encode a null literal.
     * @return this writer.
     */
    @Throws(IOException::class)
    fun value(value: String?): JsonWriter {
        if (value == null) {
            return nullValue()
        }
        beforeValue(false)
        string(value)
        return this
    }

    /**
     * Encodes `null`.
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun nullValue(): JsonWriter {
        beforeValue(false)
        out.write("null")
        return this
    }

    /**
     * Encodes `value`.
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun value(value: Boolean): JsonWriter {
        beforeValue(false)
        out.write(if (value) "true" else "false")
        return this
    }

    /**
     * Encodes `value`.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite] unless this writer is lenient.
     * @return this writer.
     */
    @Throws(IOException::class)
    fun value(value: Double): JsonWriter {
        require(!(!isLenient && (java.lang.Double.isNaN(value) || java.lang.Double.isInfinite(value)))) { "Numeric values must be finite, but was $value" }
        beforeValue(false)
        out.append(java.lang.Double.toString(value))
        return this
    }

    /**
     * Encodes `value`.
     *
     * @return this writer.
     */
    @Throws(IOException::class)
    fun value(value: Long): JsonWriter {
        beforeValue(false)
        out.write(java.lang.Long.toString(value))
        return this
    }

    /**
     * Encodes `value`.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite] unless this writer is lenient.
     * @return this writer.
     */
    @Throws(IOException::class)
    fun value(value: Number?): JsonWriter {
        if (value == null) {
            return nullValue()
        }
        val string = value.toString()
        require(!(!isLenient &&
                (string == "-Infinity" || string == "Infinity" || string == "NaN"))) { "Numeric values must be finite, but was $value" }
        beforeValue(false)
        out.append(string)
        return this
    }

    /**
     * Ensures all buffered data is written to the underlying [Writer]
     * and flushes that writer.
     */
    @Throws(IOException::class)
    fun flush() {
        out.flush()
    }

    /**
     * Flushes and closes this writer and the underlying [Writer].
     *
     * @throws IOException if the JSON document is incomplete.
     */
    @Throws(IOException::class)
    override fun close() {
        out.close()
        if (peek() != JsonScope.NONEMPTY_DOCUMENT) {
            throw IOException("Incomplete document")
        }
    }

    @Throws(IOException::class)
    private fun string(value: String) {
        out.write("\"")
        var i = 0
        val length = value.length
        while (i < length) {
            val c = value[i]
            when (c) {
                '"', '\\' -> {
                    out.write('\\'.code)
                    out.write(c.code)
                }
                '\t' -> out.write("\\t")
                '\b' -> out.write("\\b")
                '\n' -> out.write("\\n")
                '\r' -> out.write("\\r")
                '\u000c' -> out.write("\\f")
                '\u2028', '\u2029' -> out.write(TextUt.format("\\u%04x", c.code))
                else -> if (c.code <= 0x1F) {
                    out.write(TextUt.format("\\u%04x", c.code))
                } else {
                    out.write(c.code)
                }
            }
            i++
        }
        out.write("\"")
    }

    @Throws(IOException::class)
    private fun newline() {
        if (indent == null) {
            return
        }
        out.write("\n")
        for (i in 1 until stack.size) {
            out.write(indent)
        }
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the name's value.
     */
    @Throws(IOException::class)
    private fun beforeName() {
        val context = peek()
        if (context == JsonScope.NONEMPTY_OBJECT) {
            out.write(','.code)
        } else check(context == JsonScope.EMPTY_OBJECT) {
            "Nesting problem: $stack"
        }
        newline()
        replaceTop(JsonScope.DANGLING_NAME)
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     *
     * @param root true if the value is a new array or object, the two values
     * permitted as top-level elements.
     */
    @Throws(IOException::class)
    private fun beforeValue(root: Boolean) {
        when (peek()) {
            JsonScope.EMPTY_DOCUMENT -> {
                check(!(!isLenient && !root)) { "JSON must start with an array or an object." }
                replaceTop(JsonScope.NONEMPTY_DOCUMENT)
            }
            JsonScope.EMPTY_ARRAY -> {
                replaceTop(JsonScope.NONEMPTY_ARRAY)
                newline()
            }
            JsonScope.NONEMPTY_ARRAY -> {
                out.append(',')
                newline()
            }
            JsonScope.DANGLING_NAME -> {
                out.append(separator)
                replaceTop(JsonScope.NONEMPTY_OBJECT)
            }
            JsonScope.NONEMPTY_DOCUMENT -> throw IllegalStateException(
                    "JSON must have only one top-level value.")
            else -> throw IllegalStateException("Nesting problem: $stack")
        }
    }

    init {
        stack.add(JsonScope.EMPTY_DOCUMENT)
    }

    /**
     * Creates a new instance that writes a JSON-encoded stream to `out`.
     * For best performance, ensure [Writer] is buffered; wrapping in
     * [BufferedWriter][java.io.BufferedWriter] if necessary.
     */
    init {
        if (out == null) {
            throw NullPointerException("out == null")
        }
        this.out = out
    }
}
