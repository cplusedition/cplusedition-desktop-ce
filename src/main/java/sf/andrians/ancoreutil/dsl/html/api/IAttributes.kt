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
package sf.andrians.ancoreutil.dsl.html.api

interface IAttributes : IAttr, Iterable<IAttribute> {
    fun lb(): IAttributes
    fun a(attr: IAttribute): IAttributes
    fun a(vararg attrs: IAttribute): IAttributes
    fun a(attrs: IAttributes): IAttributes
    fun a(vararg attrs: IAttributes): IAttributes
    fun a(vararg attrs: IAttr): IAttributes
    fun a(name: String, value: Boolean): IAttributes
    fun a(name: String, value: Int): IAttributes
    fun a(name: String, value: Long): IAttributes
    fun a(name: String, value: Float): IAttributes
    fun a(name: String, value: Double): IAttributes
    fun a(name: String, value: Any?): IAttributes
    fun a(name: String, vararg values: Any): IAttributes
    fun a(name: Any, value: Boolean): IAttributes
    fun a(name: Any, value: Int): IAttributes
    fun a(name: Any, value: Long): IAttributes
    fun a(name: Any, value: Float): IAttributes
    fun a(name: Any, value: Double): IAttributes
    fun a(name: Any, value: Any?): IAttributes
    fun a(name: Any, vararg values: Any): IAttributes
    fun xmlns(url: String): IAttributes
    fun xmlns(name: String, url: String): IAttributes
    fun id(id: Any): IAttributes
    fun css(css: Any): IAttributes
    fun css(vararg csss: Any): IAttributes
    fun type(type: Any): IAttributes
    fun name(name: Any): IAttributes
    fun value(value: Any): IAttributes
    fun content(value: Any): IAttributes
    fun label(label: Any): IAttributes
    fun width(width: Any): IAttributes
    fun href(url: Any): IAttributes
    fun rel(rel: Any): IAttributes
    fun src(url: Any): IAttributes
    fun style(style: Any): IAttributes
    fun checked(): IAttributes
    fun selected(): IAttributes
    fun colspan(n: Int): IAttributes

    fun onload(script: String): IAttributes
    fun onunload(script: String): IAttributes
    fun onclick(script: String): IAttributes
    fun ondblclick(script: String): IAttributes
    fun onmousedown(script: String): IAttributes
    fun onmouseup(script: String): IAttributes
    fun onmouseover(script: String): IAttributes
    fun onmousemove(script: String): IAttributes
    fun onmouseout(script: String): IAttributes
    fun onfocus(script: String): IAttributes
    fun onblur(script: String): IAttributes
    fun onkeypress(script: String): IAttributes
    fun onkeydown(script: String): IAttributes
    fun onkeyup(script: String): IAttributes
    fun onsubmit(script: String): IAttributes
    fun onreset(script: String): IAttributes
    fun onselect(script: String): IAttributes

        fun onload(vararg scripts: String): IAttributes
    fun onunload(vararg scripts: String): IAttributes
    fun onclick(vararg scripts: String): IAttributes
    fun ondblclick(vararg scripts: String): IAttributes
    fun onmousedown(vararg scripts: String): IAttributes
    fun onmouseup(vararg scripts: String): IAttributes
    fun onmouseover(vararg scripts: String): IAttributes
    fun onmousemove(vararg scripts: String): IAttributes
    fun onmouseout(vararg scripts: String): IAttributes
    fun onfocus(vararg scripts: String): IAttributes
    fun onblur(vararg scripts: String): IAttributes
    fun onkeypress(vararg scripts: String): IAttributes
    fun onkeydown(vararg scripts: String): IAttributes
    fun onkeyup(vararg scripts: String): IAttributes
    fun onsubmit(vararg scripts: String): IAttributes
    fun onreset(vararg scripts: String): IAttributes
    fun onselect(vararg scripts: String): IAttributes
}
