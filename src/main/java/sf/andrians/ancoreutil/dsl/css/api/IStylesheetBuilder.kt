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
package sf.andrians.ancoreutil.dsl.css.api

import sf.andrians.ancoreutil.dsl.css.api.support.IRulesetChild
import sf.andrians.ancoreutil.dsl.css.api.support.IStylesheetChild

interface IStylesheetBuilder {
    fun stylesheet(vararg nodes: IStylesheetChild): IStylesheet
    fun imports(uri: String, vararg mediums: IMedium): IImport
    fun media(vararg mediums: IMedium): IMedia
    fun media(medium: IMedium, vararg rulesets: IRuleset): IMedia
    fun media(vararg rulesets: IRuleset): IMedia
    fun fontface(vararg decls: IDeclaration): IFontface
    fun page(name: String?, vararg decls: IDeclaration): IPage
    fun rulesets(vararg children: IStylesheetChild): IRulesets
    fun declarations(vararg children: IDeclaration): IDeclarations
    fun ruleset(vararg children: IRulesetChild): IRuleset
    fun ruleset(sel: Any, vararg children: IRulesetChild): IRuleset
    fun rule(property: Any, value: Any): IDeclaration
    fun self(format: String, vararg args: Any): ISelector
    fun sel(): ISelector
    fun sel(sel: Any): ISelector
    fun sel(vararg sels: Any): ISelector
    fun id(id: Any): ISelector?
    fun css(css: Any): ISelector?
}
