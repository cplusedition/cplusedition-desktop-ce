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

public abstract class LLKHtmlDomParserVisitorAdapter<T> implements ILLKHtmlDomParserVisitor<T> {

	public T visit(ASTasp node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTattribute node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTattributes node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTcdata node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTcomment node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTcond node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTdeclaration node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTdoctype node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTdocument node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTdtdId node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTendTag node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTextraEndTag node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTjste node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTpi node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTscript node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTstartTag node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTstyle node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(ASTtext node, T data) {
		return node.childrenAccept(this, data);
	}

	public T visit(LLKNode node, T data) {
		return node.childrenAccept(this, data);
	}

}
