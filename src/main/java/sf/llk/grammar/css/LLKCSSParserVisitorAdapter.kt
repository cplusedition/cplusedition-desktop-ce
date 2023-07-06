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
package sf.llk.grammar.css

import sf.llk.grammar.css.parser.*

open class LLKCSSParserVisitorAdapter<T> : ILLKCSSParserVisitor<T> {
    override fun visit(node: ASTClass, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTImport, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTatRule, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTattrib, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTcharset, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTcombinator, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTdeclaration, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTexpr, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTfont_face, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTfunction, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASThexcolor, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTmedia, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTmedium, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASToperator, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTpage, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTprio, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTproperty, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTpseudo, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTpseudo_page, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTruleset, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTselector, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTsimple_selector, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTstyleSheet, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTterm, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTunary_operator, data: T): T {
        return data
    }

    override fun visit(node: ASTmedia_query_list, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTmedia_query, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTmedia_expr, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTmedia_feature, data: T): T {
        return data
    }

    override fun visit(node: ASTmedia_type, data: T): T {
        return data
    }

    override fun visit(node: LLKNode, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTkeyframe_selector, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTkeyframes, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTkeyframes_block, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTattr, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTcalc, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTproduct, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTqname, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTsum, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTunit, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTcolor, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTcolor_stop, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTlinear_gradient, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTposition, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTradial_gradient, data: T): T {
        return node.childrenAccept(this, data)
    }

    override fun visit(node: ASTside_or_corner, data: T): T {
        return node.childrenAccept(this, data)
    }

}

