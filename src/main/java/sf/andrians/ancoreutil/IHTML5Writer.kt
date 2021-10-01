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

/**
 * Similar to XMLWriter but use Tag class to treat end tags in HTML instead XML style.
 */
interface IHTML5Writer : IXMLWriter {

    ////////////////////////////////////////////////////////////////////////

    fun id(value: String): IAttribute?
    fun css(value: String): IAttribute?
    fun type(value: String): IAttribute?
    fun name(value: String): IAttribute?
    fun href(value: String): IAttribute?
    fun src(value: String): IAttribute?
    fun id(value: Any): IAttribute?
    fun css(value: Any): IAttribute?
    fun type(value: Any): IAttribute?

    ////////////////////////////////////////////////////////////////////////

    /** Write doctype declaration for HTML5  */
    fun doctype(): IHTML5Writer

    /** Write title, escaping text.  */
    fun title(text: String): IHTML5Writer

    /** Write link element for the given stylesheet.  */
    fun stylesheet(href: String): IHTML5Writer

    /** Write script element for the given href.  */
    fun javascript(href: String): IHTML5Writer

    /** Write inline javascripts.  */
    fun script(vararg content: String): IHTML5Writer

    /** Write inline styles.  */
    fun style(vararg content: String): IHTML5Writer

    /** Writer meta element for the given content-type.  */
    fun contentType(charset: String): IHTML5Writer

    ////////////////////////////////////////////////////////////////////////

    override fun xmlHeader(): IHTML5Writer
    override fun start(tag: String): IHTML5Writer
    override fun start(tag: String, vararg attrs: IAttribute): IHTML5Writer
    override fun start(tag: String, vararg attrs: String): IHTML5Writer
    override fun start(tag: String, attrs: Map<String, String?>): IHTML5Writer
    override fun startAll(vararg tags: String): IHTML5Writer
    override fun empty(tag: String): IHTML5Writer
    override fun empty(tag: String, vararg attrs: IAttribute): IHTML5Writer
    override fun empty(tag: String, vararg attrs: String): IHTML5Writer
    override fun empty(tag: String, attrs: Map<String, String?>): IHTML5Writer
    override fun raw(content: String): IHTML5Writer
    override fun raw(vararg content: String): IHTML5Writer
    override fun txt(content: String): IHTML5Writer
    override fun txt(vararg content: String): IHTML5Writer
    override fun format(format: String, vararg args: Any): IHTML5Writer
    override fun comment(comment: String): IHTML5Writer
    override fun comment(vararg comment: String): IHTML5Writer
    override fun cdata(cdata: String): IHTML5Writer
    override fun cdata(vararg cdata: String): IHTML5Writer
    override fun end(): IHTML5Writer
    override fun end(vararg expects: String): IHTML5Writer
    override fun end(level: Int): IHTML5Writer
    override fun endTill(level: Int): IHTML5Writer
    override fun endAll(): IHTML5Writer
    override fun element(tag: String, content: String?): IHTML5Writer
    override fun element(tag: String, content: String?, vararg attrs: IAttribute): IHTML5Writer
    override fun element(tag: String, content: String?, vararg attrs: String): IHTML5Writer
    override fun element(tag: String, content: String?, attrs: Map<String, String?>): IHTML5Writer
    override fun lb(): IHTML5Writer
    override fun formatted(vararg content: String): IHTML5Writer
    override fun flush(): IHTML5Writer

    ////////////////////////////////////////////////////////////////////////

    companion object {
        const val DOCTYPE_HTML5 = "<!DOCTYPE html>"
        const val ISO_8859_1 = "ISO-8859-1"
        const val UTF_8 = "UTF-8"
    }

    ////////////////////////////////////////////////////////////////////////
}