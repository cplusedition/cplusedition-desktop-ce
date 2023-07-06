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
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ISourceText;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.util.LLKShareUtil;

public class ASTstartTag extends NamedNode {

	public ASTstartTag() {
		super(ILLKHtmlDomParser.ASTstartTag);
	}

	public ASTstartTag(int type) {
		super(type);
	}

	public <T> T accept(ILLKHtmlDomParserVisitor<T> visitor, T data) {
		return visitor.visit(this, data);
	}

	////////////////////////////////////////////////////////////////////////

	private ASTattributes attributes;
	private HtmlTag tag;
	private boolean isEmpty;
	private Object data;

	////////////////////////////////////////////////////////////////////////

	public void init(ASTattributes attrs, boolean empty) throws LLKParseException {
		this.attributes = attrs;
		this.tag = HtmlTag.get(getName().toString());
		this.isEmpty = empty || tag.isEmpty();
	}

	public ISourceText getNamePosition() {
		return firstToken.getNext();
	}

	public CharSequence getName() {
		return getNamePosition().getText();
	}

	public int getNameOffset() {
		return getNamePosition().getOffset();
	}

	@Override
	public void setName(CharSequence name) {
		firstToken.getNext().setText(name);
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	public ASTattributes getAttributes() {
		return attributes;
	}

	public HtmlTag getTag() {
		return tag;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public boolean hasAttribute(String name) {
		return getAttribute(name) != null;
	}

	public ASTattribute getAttribute(String name) {
		if (attributes == null)
			return null;
		for (ILLKNode n: attributes.children()) {
			if (LLKShareUtil.equalsIgnoreCase(name, ((ASTattribute)n).getName())) {
				return (ASTattribute)n;
		}}
		return null;
	}

	public CharSequence getAttributeText(String name) {
		ASTattribute a = getAttribute(name);
		if (a == null)
			return null;
		return a.getValue();
	}

	public ASTattribute removeAttribute(String name) {
		if (attributes == null)
			return null;
		for (ILLKNode p = null, c = attributes.getFirst(); c != null; p = c, c = c.getNext()) {
			if (LLKShareUtil.equalsIgnoreCase(name, ((ASTattribute)c).getName())) {
				if (p == null) {
					attributes.setFirst(c.getNext());
				} else {
					p.setNext(c.getNext());
				}
				return (ASTattribute)c;
		}}
		return null;
	}

	public void addAttribute(ASTattribute a) {
		if (attributes == null) {
			attributes = new ASTattributes();
			a.setParent(attributes);
			attributes.setFirst(a);
			attributes.setFirstToken(a.getFirstToken());
			attributes.setLastToken(a.getLastToken());
		} else {
			a.setParent(attributes);
			ILLKNode p = null;
			for (ILLKNode n = attributes.getFirst(); n != null; n = n.getNext())
				p = n;
			if (p == null) {
				attributes.setFirst(a);
				attributes.setFirstToken(a.getFirstToken());
				attributes.setLastToken(a.getLastToken());
			} else {
				p.setNext(a);
				attributes.setLastToken(a.getLastToken());
	}}}

	////////////////////////////////////////////////////////////////////////
}
