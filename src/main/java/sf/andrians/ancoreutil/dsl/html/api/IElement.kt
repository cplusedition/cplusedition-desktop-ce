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

interface IElement : IChild {
    fun tag(): String
    fun id(): String?
    fun raw(vararg content: String): IElement
    fun txt(vararg content: String): IElement
    fun esc(vararg content: String): IElement

    fun attrCount(): Int
    fun attributes(): Iterable<IAttribute>
    fun id(id: Any): IElement
    fun css(css: Any): IElement
    fun css(vararg csss: Any): IElement
    fun type(type: Any): IElement
    fun name(name: Any): IElement
    fun value(value: Any): IElement
    fun content(value: Any): IElement
    fun label(label: Any): IElement
    fun width(width: Any): IElement
    fun href(url: Any): IElement
    fun rel(rel: Any): IElement
    fun src(url: Any): IElement
    fun style(style: Any): IElement
    fun a(a: IAttributes): IElement
    fun a(a: IAttribute): IElement
    fun a(name: String, value: Any): IElement
    fun a(name: String, vararg values: Any): IElement

    fun childCount(): Int
    fun children(): Iterable<IChild>
    fun n(vararg children: INode): IElement
    fun c(vararg children: IChild): IElement
    fun a(child: ITop): IElement
    fun a(child: IElement): IElement
    fun a(child: IText): IElement
    fun a(child: ICData): IElement
    fun a(child: IComment): IElement
    fun a(child: IPI): IElement
    fun a(child: IDeclaration): IElement
    fun a(child: INode): IElement
}
