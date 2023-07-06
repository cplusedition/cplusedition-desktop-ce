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
package sf.llk.grammar.html.parser;

public interface ILLKHtmlDomParserVisitor<T> {
	T visit(ASTasp node, T data);
	T visit(ASTattribute node, T data);
	T visit(ASTattributes node, T data);
	T visit(ASTcdata node, T data);
	T visit(ASTcomment node, T data);
	T visit(ASTcond node, T data);
	T visit(ASTdeclaration node, T data);
	T visit(ASTdoctype node, T data);
	T visit(ASTdocument node, T data);
	T visit(ASTdtdId node, T data);
	T visit(ASTendTag node, T data);
	T visit(ASTextraEndTag node, T data);
	T visit(ASTjste node, T data);
	T visit(ASTpi node, T data);
	T visit(ASTscript node, T data);
	T visit(ASTstartTag node, T data);
	T visit(ASTstyle node, T data);
	T visit(ASTtext node, T data);
	T visit(LLKNode node, T data);
}
