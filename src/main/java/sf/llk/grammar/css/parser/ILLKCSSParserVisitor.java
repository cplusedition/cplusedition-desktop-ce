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
package sf.llk.grammar.css.parser;

public interface ILLKCSSParserVisitor<T> {
	T visit(ASTClass node, T data);
	T visit(ASTImport node, T data);
	T visit(ASTatRule node, T data);
	T visit(ASTattr node, T data);
	T visit(ASTattrib node, T data);
	T visit(ASTcalc node, T data);
	T visit(ASTcharset node, T data);
	T visit(ASTcolor node, T data);
	T visit(ASTcolor_stop node, T data);
	T visit(ASTcombinator node, T data);
	T visit(ASTdeclaration node, T data);
	T visit(ASTexpr node, T data);
	T visit(ASTfont_face node, T data);
	T visit(ASTfunction node, T data);
	T visit(ASThexcolor node, T data);
	T visit(ASTkeyframe_selector node, T data);
	T visit(ASTkeyframes node, T data);
	T visit(ASTkeyframes_block node, T data);
	T visit(ASTlinear_gradient node, T data);
	T visit(ASTmedia node, T data);
	T visit(ASTmedia_expr node, T data);
	T visit(ASTmedia_feature node, T data);
	T visit(ASTmedia_query node, T data);
	T visit(ASTmedia_query_list node, T data);
	T visit(ASTmedia_type node, T data);
	T visit(ASTmedium node, T data);
	T visit(ASToperator node, T data);
	T visit(ASTpage node, T data);
	T visit(ASTposition node, T data);
	T visit(ASTprio node, T data);
	T visit(ASTproduct node, T data);
	T visit(ASTproperty node, T data);
	T visit(ASTpseudo node, T data);
	T visit(ASTpseudo_page node, T data);
	T visit(ASTqname node, T data);
	T visit(ASTradial_gradient node, T data);
	T visit(ASTruleset node, T data);
	T visit(ASTselector node, T data);
	T visit(ASTside_or_corner node, T data);
	T visit(ASTsimple_selector node, T data);
	T visit(ASTstyleSheet node, T data);
	T visit(ASTsum node, T data);
	T visit(ASTterm node, T data);
	T visit(ASTunary_operator node, T data);
	T visit(ASTunit node, T data);
	T visit(LLKNode node, T data);
}
