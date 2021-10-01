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
package sf.andrians.ancoreutil

import sf.andrians.ancoreutil.util.text.IAttribute
import java.io.Closeable

/**
 * Basically same as IXmlWriter, but don't implicitly escape text and attribute values, except txt().
 * It don't add line breaks either, call lb() to break up lines.
 */
interface IXMLWriter : Closeable {

    fun xmlHeader(): IXMLWriter
    fun start(tag: String): IXMLWriter
    fun start(tag: String, vararg attrs: IAttribute): IXMLWriter

    /** @param attrs An array of key and value pairs (ie. key1, value1, key2, value2, ...).
     */
    fun start(tag: String, vararg attrs: String): IXMLWriter
    fun start(tag: String, attrs: Map<String, String?>): IXMLWriter

    /** Start n nested tags  */
    fun startAll(vararg tags: String): IXMLWriter
    fun empty(tag: String): IXMLWriter
    fun empty(tag: String, vararg attrs: IAttribute): IXMLWriter

    /** @param attrs An array of key and value pairs (ie. key1, value1, key2, value2, ...).
     */
    fun empty(tag: String, vararg attrs: String): IXMLWriter
    fun empty(tag: String, attrs: Map<String, String?>): IXMLWriter

    /**
     * Emit the given content, which may be plain text or contains tags, as is.
     * If there are more than one argument, each of the subsequent argument is on a new line,
     * with proper indents.
     */
    fun raw(content: String): IXMLWriter
    fun raw(vararg content: String): IXMLWriter
    fun format(format: String, vararg args: Any): IXMLWriter

    /** Same as raw(content).  */
    fun txt(content: String): IXMLWriter
    fun txt(vararg content: String): IXMLWriter

    /** Same as raw(escXml(content)).  */
    fun esc(content: String): IXMLWriter
    fun esc(vararg content: String): IXMLWriter

    /**
     * Emit comment.
     * If there are more than one argument, each of the subsequent argument is on a new line,
     * with proper indents.
     */
    fun comment(comment: String): IXMLWriter
    fun comment(vararg comment: String): IXMLWriter

    /**
     * Emit cdata.
     * If there are more than one argument, each of the subsequent argument is on a new line,
     * with proper indents.
     */
    fun cdata(cdata: String): IXMLWriter
    fun cdata(vararg cdata: String): IXMLWriter
    fun end(): IXMLWriter
    fun end(vararg expects: String): IXMLWriter
    fun end(level: Int): IXMLWriter
    fun endTill(level: Int): IXMLWriter
    fun endAll(): IXMLWriter
    fun element(tag: String, content: String?): IXMLWriter
    fun element(tag: String, content: String?, vararg attrs: IAttribute): IXMLWriter

    /** @param attrs An array of key and value pairs (ie. key1, value1, key2, value2, ...).
     */
    fun element(tag: String, content: String?, vararg attrs: String): IXMLWriter
    fun element(tag: String, content: String?, attrs: Map<String, String?>): IXMLWriter

    /** Write a line break. Use for manual formatting.  */
    fun lb(): IXMLWriter

    /** Write a line break only if there is not one already.  */
    fun lb1(): IXMLWriter
    fun formatted(vararg content: String): IXMLWriter
    fun flush(): IXMLWriter

    fun a(name: String, value: String?): IAttribute

    /**
     * @return Current nested level, 0 for top level outside root element.
     */
    fun level(): Int

    ////////////////////////////////////////////////////////////////////////

    companion object {
        const val INDENT = ""
        const val TAB = "    "
    }

    ////////////////////////////////////////////////////////////////////////

}