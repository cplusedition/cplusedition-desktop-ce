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
package com.cplusedition.bot.dsl.css.api

import com.cplusedition.bot.dsl.css.api.support.IStylesheetChild

interface IStylesheet : ICSSNode, Iterable<IStylesheetChild> {
    val namespaces: Collection<INamespace>
    val imports: Collection<IImport>
    val medias: Collection<IMedia>
    val fontFaces: Collection<IFontface>
    val pages: Collection<IPage>
    val rulesets: Collection<IRuleset>

    fun add(vararg imports: IImport): IStylesheet
    fun add(vararg namespaces: INamespace): IStylesheet
    fun add(vararg medias: IMedia): IStylesheet
    fun add(vararg fontfaces: IFontface): IStylesheet?
    fun add(vararg pages: IPage): IStylesheet
    fun add(vararg rulesets: IRuleset): IStylesheet
    fun add(vararg rulesets: IRulesets): IStylesheet
    fun add(vararg children: IStylesheetChild): IStylesheet

    fun add(iport: IImport): IStylesheet
    fun add(namespace: INamespace): IStylesheet
    fun add(media: IMedia): IStylesheet
    fun add(fontface: IFontface): IStylesheet
    fun add(page: IPage): IStylesheet
    fun add(ruleset: IRuleset): IStylesheet
    fun add(rulesets: IRulesets): IStylesheet
    fun add(child: IRaw): IStylesheet
    fun add(child: IStylesheetChild): IStylesheet
}
