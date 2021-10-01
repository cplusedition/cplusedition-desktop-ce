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

interface INodeVisitor<T> {
    fun visit(node: ITop, data: T)
    fun visit(node: IWicketComponent, data: T)
    fun visit(node: IWicketElement, data: T)
    fun visit(node: IElement, data: T)
    fun visit(node: IText, data: T)
    fun visit(node: ILine, data: T)
    fun visit(node: ICData, data: T)
    fun visit(node: IComment, data: T)
    fun visit(node: IPI, data: T)
    fun visit(node: IDeclaration, data: T)
    fun visit(node: IChild?, data: T)
}