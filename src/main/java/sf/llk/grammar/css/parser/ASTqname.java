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

public class ASTqname extends LLKNode {

	public ASTqname() {
		super(ILLKCSSParser.ASTqname);
	}

	public <T> T accept(ILLKCSSParserVisitor<T> visitor, T data) {
		return visitor.visit(this, data);
	}
}
