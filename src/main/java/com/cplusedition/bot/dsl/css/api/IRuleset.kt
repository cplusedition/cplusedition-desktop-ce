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

import com.cplusedition.bot.dsl.css.api.support.IRulesetChild
import com.cplusedition.bot.dsl.css.api.support.IStylesheetChild

interface IRuleset : IStylesheetChild, IRulesetChild {
    val selectors: Collection<ISelector>
    val declarations: Collection<IDeclaration>

    fun add(vararg sels: ISelector): IRuleset
    fun add(vararg decls: IDeclaration): IRuleset

    /** Add declarations, but not the selectors, of the given rules to this rule.  */
    fun add(vararg rules: IRuleset): IRuleset
    fun add(vararg decls: IDeclarations): IRuleset

    fun add(sel: ISelector): IRuleset
    fun add(decl: IDeclaration): IRuleset

    /** Add declarations, but not the selectors, of the given rules to this rule.  */
    fun add(rule: IRuleset): IRuleset
    fun add(decls: IDeclarations): IRuleset
}
