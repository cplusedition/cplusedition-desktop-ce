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
package sf.andrians.ancoreutil.util.text

object JavascriptUtil {
    /**
     * Escape and quote the given string in Javascript style.
     */
    fun quote(s: CharSequence): String {
        val ret = StringBuilder("\"")
        esc(ret, s)
        ret.append("\"")
        return ret.toString()
    }

    fun esc(ret: StringBuilder, s: CharSequence) {
        var cc: String? = ""
        var c: Char
        var n: Int
        var i = 0
        val max = s.length
        LOOP@ while (i < max) {
            c = s[i++]
            when (c) {
                '\n' -> cc = "\\n"
                '\r' -> cc = "\\r"
                '\t' -> cc = "\\t"
                '\'' -> cc = "\\\'"
                '\"' -> cc = "\\\""
                '\\' -> cc = "\\\\"
                '\b' -> cc = "\\b"
                '\u000c' -> cc = "\\f"
                else -> {
                    n = c.toInt()
                    cc = if (n < 0x20 || n == 0x7f) {
                        TextUtil.format("\\u%04x", n)
                    } else {
                        ret.append(c)
                        continue@LOOP
                    }
                }
            }
            ret.append(cc)
        }
    }
}