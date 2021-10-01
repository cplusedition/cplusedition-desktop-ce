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

interface ISelector : IRulesetChild {
    /** Append sel if current selector is empty otherwise &lt;code>, sel */
    fun sel(sel: Any): ISelector

    /** Append &lt;code>sel, sel, ...  */
    fun sel(vararg sels: Any): ISelector

    /** Append &lt;code>#id  */
    fun id(id: Any): ISelector

    /** Append &lt;code>.css  */
    fun css(css: Any): ISelector

    /** Append &lt;code>:psc  */
    fun psc(pseudo: Any): ISelector

    /** Append &lt;code>::pse  */
    fun pse(pseudo: Any): ISelector

    /** Append &lt;code>[expr]  */
    fun attr(expr: Any): ISelector

    /** Append &lt;code>[name op "value" ]  */
    fun attr(name: Any, op: IAOp, value: Any): ISelector

    /** Append &lt;code>sel  */
    fun desc(sel: Any): ISelector

    /** desc(sel) for each sel  */
    fun desc(vararg sels: Any): ISelector

    /** Append &lt;code>>sel  */
    fun child(sel: Any): ISelector

    /** child(sel) for each sel  */
    fun child(vararg sels: Any): ISelector

    /** Append &lt;code>+sel  */
    fun silbing(sel: Any): ISelector
    /** Same as child(sel)  */
    fun c(sel: Any): ISelector

    /** Same as child(sels)  */
    fun c(vararg sels: Any): ISelector

    /** @return Same as desc(sel)
     */
    fun a(sel: Any): ISelector

    /** Shortcut for desc(sel) for each sel.  */
    fun a(vararg sels: Any): ISelector
}
