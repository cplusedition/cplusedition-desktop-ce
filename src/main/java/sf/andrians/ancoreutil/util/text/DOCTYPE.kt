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

import java.util.*

enum class DOCTYPE(private vararg val content: String) {
    HTML40(
            "HTML",
            "PUBLIC",
            "\"-//W3C//DTD HTML 4.0 Transitional//EN\"",
            "\"http://www.w3.org/TR/REC-html40/loose.dtd\""),
    HTML401("HTML", "PUBLIC", "\"-//W3C//DTD HTML 4.01//EN\"", "\"http://www.w3.org/TR/html4/strict.dtd\""),
    HTML401Transitional(
            "HTML",
            "PUBLIC",
            "\"-//W3C//DTD HTML 4.01 Transitional//EN\"",
            "\"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\""),
    HTML401Frameset(
            "HTML",
            "PUBLIC",
            "\"-//W3C//DTD HTML 4.01 Frameset//EN\"",
            "\"http://www.w3.org/TR/1999/REC-html401-19991224/frameset.dtd\""),
    XHTML10(
            "raw",
            "PUBLIC",
            "\"-//W3C//DTD XHTML 1.0 Strict//EN\"",
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\""),
    XHTML10Transitional(
            "raw",
            "PUBLIC",
            "\"-//W3C//DTD XHTML 1.0 Transitional//EN\"",
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\""),
    XHTML10Frameset(
            "raw",
            "PUBLIC",
            "\"-//W3C//DTD XHTML 1.0 Frameset//EN\"",
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\""),
    XHTML11(
            "raw", "PUBLIC", "\"-//W3C//DTD XHTML 1.1 Strict//EN\"", "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""),
    HTML5("raw");

    fun content(): Array<String> {
        return Arrays.copyOf(content, content.size)
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append("<!DOCTYPE")
        for (s in content) {
            ret.append(' ')
            ret.append(s)
        }
        ret.append(">")
        return ret.toString()
    }

}