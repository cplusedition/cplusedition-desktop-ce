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
package sf.andrians.org.json

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

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

interface IStringPool {
    val size: Int
    fun intern(s: String): String
}

class MyStringPool : IStringPool {
    private val strings = TreeMap<String, String>()
    override fun intern(s: String): String {
        return strings.get(s) ?: run {
            strings.put(s, s)
            s
        }
    }

    override val size: Int get() = strings.size
}

interface IJSONTokenerDelegate {
    fun put(result: JSONObject, name: String, value: Any?)
    fun put(result: JSONArray, value: Any?)
}

class MyJSONTokenerDelegate constructor(
    private val pool: IStringPool
) : IJSONTokenerDelegate {
    override fun put(result: JSONObject, name: String, value: Any?) {
        result.put(pool.intern(name), if (value is String) pool.intern(value) else value)
    }

    override fun put(result: JSONArray, value: Any?) {
        result.put(if (value is String) pool.intern(value) else value)
    }
}

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2014-05-03
 */
class MyJSONTokener constructor(
    reader: Reader,
    private val pool: IStringPool = MyStringPool()
) {
    /** current read character position on the current line.  */
    private var character: Long
    /** flag to indicate if the end of the input has been found.  */
    private var eof: Boolean
    /** current read index of the input.  */
    private var index: Long
    /** current line of the input.  */
    private var line: Long
    /** previous character read from the input.  */
    private var previous: Char
    /** Reader for the input.  */
    private val reader: Reader
    /** flag to indicate that a previous character was requested.  */
    private var usePrevious: Boolean
    /** the number of characters read in the previous line.  */
    private var characterPreviousLine: Long
    private val delegate = MyJSONTokenerDelegate(pool)
    /**
     * Construct a JSONTokener from a Reader. The caller must close the Reader.
     *
     * @param reader     A reader.
     */
    init {
        this.reader = if (reader.markSupported()) reader else BufferedReader(reader)
        eof = false
        usePrevious = false
        previous = 0.toChar()
        index = 0
        character = 1
        characterPreviousLine = 0
        line = 1
    }
    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     * @throws JSONException Thrown if trying to step back more than 1 step
     * or if already at the start of the string
     */
    @Throws(JSONException::class)
    fun back() {
        if (usePrevious || index <= 0) {
            throw JSONException("Stepping back two steps is not supported")
        }
        decrementIndexes()
        usePrevious = true
        eof = false
    }
    /**
     * Decrements the indexes for the [.back] method based on the previous character read.
     */
    private fun decrementIndexes() {
        index--
        if (previous == '\r' || previous == '\n') {
            line--
            character = characterPreviousLine
        } else if (character > 0) {
            character--
        }
    }
    /**
     * Checks if the end of the input has been reached.
     *
     * @return true if at the end of the file and we didn't step back
     */
    fun end(): Boolean {
        return eof && !usePrevious
    }
    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     * @throws JSONException thrown if there is an error stepping forward
     * or backward while checking for more data.
     */
    @Throws(JSONException::class)
    fun more(): Boolean {
        if (usePrevious) {
            return true
        }
        try {
            reader.mark(1)
        } catch (e: IOException) {
            throw JSONException("Unable to preserve stream position", e)
        }
        try {
            if (reader.read() <= 0) {
                eof = true
                return false
            }
            reader.reset()
        } catch (e: IOException) {
            throw JSONException("Unable to read the next character from the stream", e)
        }
        return true
    }
    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws JSONException Thrown if there is an error reading the source string.
     */
    @Throws(JSONException::class)
    operator fun next(): Char {
        val c: Int
        if (usePrevious) {
            usePrevious = false
            c = previous.code
        } else {
            c = try {
                reader.read()
            } catch (exception: IOException) {
                throw JSONException(exception)
            }
        }
        if (c <= 0) {
            eof = true
            return '\u0000'
        }
        incrementIndexes(c)
        previous = c.toChar()
        return previous
    }
    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     * @param c the current character read.
     */
    private fun incrementIndexes(c: Int) {
        if (c > 0) {
            index++
            if (c == '\r'.code) {
                line++
                characterPreviousLine = character
                character = 0
            } else if (c == '\n'.code) {
                if (previous != '\r') {
                    line++
                    characterPreviousLine = character
                }
                character = 0
            } else {
                character++
            }
        }
    }
    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws JSONException if the character does not match.
     */
    @Throws(JSONException::class)
    fun next(c: Char): Char {
        val n = this.next()
        if (n != c) {
            if (n.code > 0) {
                throw this.syntaxError(
                    "Expected '" + c + "' and instead saw '" +
                            n + "'"
                )
            }
            throw this.syntaxError("Expected '$c' and instead saw ''")
        }
        return n
    }
    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws JSONException
     * Substring bounds error if there are not
     * n characters remaining in the source string.
     */
    @Throws(JSONException::class)
    fun next(n: Int): String {
        if (n == 0) {
            return ""
        }
        val chars = CharArray(n)
        var pos = 0
        while (pos < n) {
            chars[pos] = this.next()
            if (end()) {
                throw this.syntaxError("Substring bounds error")
            }
            pos += 1
        }
        return String(chars)
    }
    /**
     * Get the next char in the string, skipping whitespace.
     * @throws JSONException Thrown if there is an error reading the source string.
     * @return  A character, or 0 if there are no more characters.
     */
    @Throws(JSONException::class)
    fun nextClean(): Char {
        while (true) {
            val c = this.next()
            if (c.code == 0 || c > ' ') {
                return c
            }
        }
    }
    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     * `"`&nbsp;<small>(double quote)</small> or
     * `'`&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws JSONException Unterminated string.
     */
    @Throws(JSONException::class)
    fun nextString(quote: Char): String {
        var c: Char
        val sb = StringBuilder()
        while (true) {
            c = this.next()
            when (c) {
                '\u0000', '\n', '\r' -> throw this.syntaxError("Unterminated string")
                '\\' -> {
                    c = this.next()
                    when (c) {
                        'b' -> sb.append('\b')
                        't' -> sb.append('\t')
                        'n' -> sb.append('\n')
                        'f' -> sb.append('\u000c')
                        'r' -> sb.append('\r')
                        'u' -> try {
                            sb.append(this.next(4).toInt(16).toChar())
                        } catch (e: NumberFormatException) {
                            throw this.syntaxError("Illegal escape.", e)
                        }
                        '"', '\'', '\\', '/' -> sb.append(c)
                        else -> throw this.syntaxError("Illegal escape.")
                    }
                }
                else -> {
                    if (c == quote) {
                        return sb.toString()
                    }
                    sb.append(c)
                }
            }
        }
    }
    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  delimiter A delimiter character.
     * @return   A string.
     * @throws JSONException Thrown if there is an error while searching
     * for the delimiter
     */
    @Throws(JSONException::class)
    fun nextTo(delimiter: Char): String {
        val sb = StringBuilder()
        while (true) {
            val c = this.next()
            if (c == delimiter || c.code == 0 || c == '\n' || c == '\r') {
                if (c.code != 0) {
                    back()
                }
                return sb.toString().trim { it <= ' ' }
            }
            sb.append(c)
        }
    }
    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     * @throws JSONException Thrown if there is an error while searching
     * for the delimiter
     */
    @Throws(JSONException::class)
    fun nextTo(delimiters: String): String {
        var c: Char
        val sb = StringBuilder()
        while (true) {
            c = this.next()
            if (delimiters.indexOf(c) >= 0 || c.code == 0 || c == '\n' || c == '\r') {
                if (c.code != 0) {
                    back()
                }
                return sb.toString().trim { it <= ' ' }
            }
            sb.append(c)
        }
    }
    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    @Throws(JSONException::class)
    fun nextValue(): Any {
        var c = nextClean()
        val string: String
        when (c) {
            '"', '\'' -> return nextString(c)
            '{' -> {
                back()
                return readObject()
            }
            '[' -> {
                back()
                return readArray()
            }
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */
        val sb = StringBuilder()
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c)
            c = this.next()
        }
        if (!eof) {
            back()
        }
        string = sb.toString().trim { it <= ' ' }
        if ("" == string) {
            throw this.syntaxError("Missing value")
        }
        return stringToValue(string)
    }
    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     * is not found.
     * @throws JSONException Thrown if there is an error while searching
     * for the to character
     */
    @Throws(JSONException::class)
    fun skipTo(to: Char): Char {
        var c: Char
        try {
            val startIndex = index
            val startCharacter = character
            val startLine = line
            reader.mark(1000000)
            do {
                c = this.next()
                if (c.code == 0) {
                    reader.reset()
                    index = startIndex
                    character = startCharacter
                    line = startLine
                    return '\u0000'
                }
            } while (c != to)
            reader.mark(1)
        } catch (exception: IOException) {
            throw JSONException(exception)
        }
        back()
        return c
    }
    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A JSONException object, suitable for throwing
     */
    fun syntaxError(message: String): JSONException {
        return JSONException(message + this.toString())
    }
    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @param causedBy The throwable that caused the error.
     * @return  A JSONException object, suitable for throwing
     */
    fun syntaxError(message: String, causedBy: Throwable?): JSONException {
        return JSONException(message + this.toString(), causedBy)
    }
    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    override fun toString(): String {
        return " at " + index + " [character " + character + " line " +
                line + "]"
    }

    @Throws(org.json.JSONException::class)
    fun readObject(): JSONObject {
        val ret = JSONObject()
        var c: Char
        var key: String
        if (this.nextClean() != '{') {
            throw this.syntaxError("A JSONObject text must begin with '{'")
        }
        while (true) {
            c = this.nextClean()
            key = when (c) {
                '\u0000' -> throw this.syntaxError("A JSONObject text must end with '}'")
                '}' -> return ret
                else -> {
                    this.back()
                    this.nextValue().toString()
                }
            }

            c = this.nextClean()
            if (c != ':') {
                throw this.syntaxError("Expected a ':' after a key")
            }

            if (ret.opt(key) != null) {
                throw this.syntaxError("Duplicate key \"$key\"")
            }
            val value = this.nextValue()
            delegate.put(ret, key, value)
            when (this.nextClean()) {
                ';', ',' -> {
                    if (this.nextClean() == '}') {
                        return ret
                    }
                    this.back()
                }
                '}' -> return ret
                else -> throw this.syntaxError("Expected a ',' or '}'")
            }
        }
    }

    fun readArray(): JSONArray {
        val ret = JSONArray()
        if (this.nextClean() != '[') {
            throw this.syntaxError("A JSONArray text must start with '['")
        }

        var nextChar: Char = this.nextClean()
        if (nextChar.code == 0) {
            throw this.syntaxError("Expected a ',' or ']'")
        }
        if (nextChar != ']') {
            this.back()
            while (true) {
                if (this.nextClean() == ',') {
                    this.back()
                    delegate.put(ret, JSONObject.NULL)
                } else {
                    this.back()
                    delegate.put(ret, this.nextValue())
                }
                when (this.nextClean()) {
                    '\u0000' -> throw this.syntaxError("Expected a ',' or ']'")
                    ',' -> {
                        nextChar = this.nextClean()
                        if (nextChar.code == 0) {
                            throw this.syntaxError("Expected a ',' or ']'")
                        }
                        if (nextChar == ']') {
                            return ret
                        }
                        this.back()
                    }
                    ']' -> return ret
                    else -> throw this.syntaxError("Expected a ',' or ']'")
                }
            }
        }
        return ret
    }

    companion object {
        /**
         * Get the hex value of a character (base16).
         * @param c A character between '0' and '9' or between 'A' and 'F' or
         * between 'a' and 'f'.
         * @return  An int between 0 and 15, or -1 if c was not a hex digit.
         */
        fun dehexchar(c: Char): Int {
            if (c >= '0' && c <= '9') {
                return c.code - '0'.code
            }
            if (c >= 'A' && c <= 'F') {
                return c.code - ('A'.code - 10)
            }
            return if (c >= 'a' && c <= 'f') {
                c.code - ('a'.code - 10)
            } else -1
        }
    }

    /**
     * Tests if the value should be tried as a decimal. It makes no test if there are actual digits.
     *
     * @param val value to test
     * @return true if the string is "-0" or if it contains '.', 'e', or 'E', false otherwise.
     */
    protected fun isDecimalNotation(`val`: String): Boolean {
        return `val`.indexOf('.') > -1 || `val`.indexOf('e') > -1 || `val`.indexOf('E') > -1 || "-0" == `val`
    }

    fun stringToValue(string: String): Any {
        if ("" == string) {
            return string
        }

        if ("true".equals(string, ignoreCase = true)) {
            return java.lang.Boolean.TRUE
        }
        if ("false".equals(string, ignoreCase = true)) {
            return java.lang.Boolean.FALSE
        }
        if ("null".equals(string, ignoreCase = true)) {
            return JSONObject.NULL
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */
        val initial = string[0]
        if (initial >= '0' && initial <= '9' || initial == '-') {
            try {
                if (this.isDecimalNotation(string)) {
                    val d = java.lang.Double.valueOf(string)
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d
                    }
                } else {
                    val myLong = java.lang.Long.valueOf(string)
                    if (string == myLong.toString()) {
                        return if (myLong.toLong() == myLong.toInt().toLong()) {
                            Integer.valueOf(myLong.toInt())
                        } else myLong
                    }
                }
            } catch (ignore: Exception) {
            }
        }
        return string
    }
}
