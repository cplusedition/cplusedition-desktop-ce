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
package sf.andrians.ancoreutil.dsl.script.api

/** A very simple DSL for writing scripts.  */
interface IScriptBuilder {
    fun script(vararg children: IScriptChild): String
    fun stm(vararg spans: CharSequence): IStmt
    fun fmt(format: String, vararg args: Any): IStmt

    /** JQuery selector $("selector", ...)  */
    fun sel(vararg selectors: String): IStmt
    fun blk(vararg children: IScriptChild): IBlock
    fun blk(header: CharSequence, vararg children: IScriptChild): IBlock

    /** Just like blk(), but with default header: function().  */
    fun func(vararg children: IScriptChild): IBlock

    /** Just like blk(), but with default header: function(args).  */
    fun func(args: CharSequence, vararg children: IScriptChild): IBlock
}