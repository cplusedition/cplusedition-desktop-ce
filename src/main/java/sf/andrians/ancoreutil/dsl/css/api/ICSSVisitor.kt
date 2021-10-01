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

interface ICSSVisitor<T> {
    fun visit(node: IStylesheet, data: T)
    fun visit(node: INamespace, data: T)
    fun visit(node: IImport, data: T)
    fun visit(node: IMedia, data: T)
    fun visit(node: IFontface, data: T)
    fun visit(node: IPage, data: T)
    fun visit(node: IRuleset, data: T)
    fun visit(node: ISelector?, data: T)
    fun visit(node: IDeclaration, data: T)
    fun visit(node: IRaw, data: T)
}