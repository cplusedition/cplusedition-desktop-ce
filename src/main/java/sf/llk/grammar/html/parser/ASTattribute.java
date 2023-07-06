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

import sf.llk.grammar.html.Attributes;
import sf.llk.share.support.ILLKNamedNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ISourceText;

public class ASTattribute extends LLKNode implements ILLKNamedNode {

	public ASTattribute() {
		super(ILLKHtmlDomParser.ASTattribute);
	}

	public ASTattribute(int type) {
		super(type);
	}

	@Override
    public <T> T accept(ILLKHtmlDomParserVisitor<T> visitor, T data) {
		return visitor.visit(this, data);
	}

	private Attributes info;

	public ILLKToken getNameToken() {
		return firstToken;
	}

	public ILLKToken getValueToken() {
		if (lastToken == firstToken) {
            return null;
        }
		if (lastToken.getType() == ILLKHtmlDomParser.ASSIGN) {
            return null;
        }
		return lastToken;
	}

	@Override
    public CharSequence getName() {
		return firstToken.getText();
	}

	@Override
    public int getNameOffset() {
		return firstToken.getOffset();
	}

	@Override
    public ISourceText getNamePosition() {
		return firstToken;
	}

	public CharSequence getValue() {
		ILLKToken t = getValueToken();
		return (t == null) ? null : t.getText();
	}

	public Attributes getInfo() {
		if (info == null) {
			try {
				this.info = Attributes.valueOf(getName().toString());
			} catch (Exception e) {
				this.info = Attributes.UNKNOWN;
		}}
		return info;
	}
}
