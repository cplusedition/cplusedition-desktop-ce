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

import sf.llk.grammar.html.HtmlTag;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.INamePosition;
import sf.llk.share.support.ISourceText;
import sf.llk.share.support.NamePosition;

public class ASTendTag extends NamedNode {

	public ASTendTag() {
		super(ILLKHtmlDomParser.ASTendTag);
	}

	public ASTendTag(int type) {
		super(type);
	}

	public <T> T accept(ILLKHtmlDomParserVisitor<T> visitor, T data) {
		return visitor.visit(this, data);
	}

	////////////////////////////////////////////////////////////////////////

	private INamePosition namePosition;
	private boolean isExtra;
	private boolean isImplicit;
	private HtmlTag tag;

	////////////////////////////////////////////////////////////////////////

	public ASTendTag(boolean implicit) {
		this();
		isImplicit = implicit;
	}

	public void init(ILLKToken name, boolean extra) {
		namePosition = firstToken.getNext();
		this.isExtra = extra;
		tag = HtmlTag.get(getName().toString());
	}

	public void init(String name) {
		this.namePosition = new NamePosition(name, -1, name.length());
		tag = HtmlTag.get(name);
	}

	public ISourceText getNamePosition() {
		return namePosition;
	}

	public CharSequence getName() {
		return namePosition.getText();
	}

	public int getNameOffset() {
		return namePosition.getOffset();
	}

	@Override
	public void setName(CharSequence name) {
		namePosition.setText(name);
	}

	public boolean isExtra() {
		return isExtra;
	}

	public boolean isImplicit() {
		return isImplicit;
	}

	public HtmlTag getTag() {
		return tag;
	}

	////////////////////////////////////////////////////////////////////////
}
