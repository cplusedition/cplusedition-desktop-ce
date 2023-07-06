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

import java.util.Iterator;

import sf.llk.share.support.ChildrenIterator;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.TokenIterator;

public class LLKNode implements ILLKNode, Cloneable {

	////////////////////////////////////////////////////////////

	protected int type;
	protected int modifiers;
	protected CharSequence text;
	protected ILLKNode parent;
	protected ILLKNode first, next;
	protected int offset, end;
	protected ILLKToken firstToken, lastToken;
	protected Object data;

	////////////////////////////////////////////////////////////

	public LLKNode(int type) {
		this.type = type;
	}

	public Object clone() {
		try {
			LLKNode ret = (LLKNode)super.clone();
			ret.first = null;
			ret.parent = null;
			ret.next = null;
			ILLKNode tt, prev = null;
			for (ILLKNode t = first; t != null; t = t.getNext()) {
				tt = (ILLKNode)t.clone();
				ret.add(tt, prev);
				prev = tt;
			}
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
	}}

	public Iterable<ILLKNode> children() {
		return new ChildrenIterator(getFirst());
	}

	public Iterable<ILLKToken> tokens() {
		return new TokenIterator(getFirstToken(), getLastToken());
	}

	////////////////////////////////////////////////////////////

	public int getType() {
		return type;
	}

	public CharSequence getText() {
		return text;
	}

	public ILLKNode getParent() {
		return parent;
	}

	public ILLKNode getFirst() {
		return first;
	}

	public ILLKNode getNext() {
		return next;
	}

	public ILLKNode getLast() {
		if (first == null)
			return null;
		ILLKNode ret = first;
		ILLKNode n;
		while ((n = ret.getNext()) != null)
			ret = n;
		return ret;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Get the nth child. First child at n==0.
	 * @param n
	 * @return null if nth child not exists.
	 */
	public ILLKNode get(int n) {
		ILLKNode t = first;
		while (t != null && n > 0) {
			t = t.getNext();
			--n;
		}
		return t;
	}

	public int childCount() {
		int ret = 0;
		for (ILLKNode t = first; t != null; t = t.getNext())
			++ret;
		return ret;
	}

	public boolean hasChildren() {
		return first != null;
	}

	public boolean hasSingleChild() {
		return first != null && first.getNext() == null;
	}

	public boolean hasMultipleChildren() {
		return first != null && first.getNext() != null;
	}

	////////////////////////////////////////////////////////////

	public void setType(int type) {
		this.type = type;
	}

	public void setText(CharSequence text) {
		this.text = text;
	}

	public void setParent(ILLKNode n) {
		parent = n;
	}

	public void setFirst(ILLKNode n) {
		first = n;
	}

	public void setNext(ILLKNode n) {
		next = n;
	}

	public int getModifiers() {
		return modifiers;
	}

	public void setModifiers(int mod) {
		modifiers = mod;
	}

	////////////////////////////////////////////////////////////

	public int indexOf(ILLKNode c) {
		int ret = 0;
		for (ILLKNode t = first; t != null; t = t.getNext()) {
			if (t.equals(c))
				return ret;
			++ret;
		}
		return -1;
	}

	/**
	 * Decouple given node from its parent and insert it as first child in the children list.
	 */
	public void prepend(ILLKNode c) {
		c.setParent(this);
		c.setNext(first);
		first = c;
	}

	/**
	 * Decouple given node from its parent and siblings and append it to end of children list.
	 */
	public boolean add(ILLKNode c) {
		c.setParent(this);
		c.setNext(null);
		if (first == null)
			first = c;
		else {
			ILLKNode n = first;
			ILLKNode t;
			while ((t = n.getNext()) != null)
				n = t;
			n.setNext(c);
		}
		return true;
	}

	/**
	 * Decouple given node 'child' from its parent and siblings and insert it after the given 'prev' child.
	 * If given 'prev'==null, insert 'child' as first child.
	 * @return child.
	 */
	public ILLKNode add(ILLKNode child, ILLKNode prev) {
		child.setParent(this);
		if (first == null || prev == null) {
			child.setNext(first);
			first = child;
		} else {
			child.setNext(prev.getNext());
			prev.setNext(child);
		}
		return child;
	}

	/**
	 * Insert the given node (list) and its siblings to the children list after the given child prev.
	 * If given prev==null, insert node as first child.
	 */
	public void addAll(ILLKNode list, ILLKNode prev) {
		if (list == null)
			return;
		ILLKNode last = list;
		for (ILLKNode t = list; t != null; t = t.getNext()) {
			last = t;
			t.setParent(this);
		}
		if (first == null || prev == null) {
			last.setNext(first);
			first = list;
		} else {
			last.setNext(prev.getNext());
			prev.setNext(list);
	}}

	/**
	 * Append the given node (list) and its siblings to the children list.
	 */
	public void addAll(ILLKNode list) {
		for (ILLKNode t = list; t != null; t = t.getNext())
			t.setParent(this);
		ILLKNode last = getLast();
		if (last == null)
			first = list;
		else
			last.setNext(list);
	}

	/**
	 * Remove given child from children list.
	 * Removed child is keep intact, ie. all its fields are not modified.
	 * In particular, the next field of the child is intact so that iteration can use
	 * child.getNext().
	 */
	public boolean remove(ILLKNode child) {
		if (child.equals(first)) {
			first = child.getNext();
			return true;
		}
		ILLKNode n = first;
		ILLKNode t;
		while (!child.equals(t = n.getNext())) {
			if (t == null)
				return false;
			n = t;
		}
		n.setNext(child.getNext());
		return true;
	}

	/**
	 * Remove children from (and include) start to (and exclude) end.
	 * If end==null, remove all children from (and include) start.
	 * Removed children fields (in particular parent and next fields) are intact.
	 * @return false if start not found.
	 */
	public boolean remove(ILLKNode start, ILLKNode end) {
		if (first == null || start == null)
			return false;
		if (first.equals(start)) {
			setFirst(end);
			return true;
		}
		for (ILLKNode p = first, n; (n = p.getNext()) != null; p = n) {
			if (n.equals(start)) {
				p.setNext(end);
				return true;
		}}
		return false;
	}

	/**
	 * Remove all children.
	 * @return The first node of the list of children remove.
	 */
	public ILLKNode removeAll() {
		ILLKNode ret = first;
		first = null;
		return ret;
	}

	/**
	 * Remove this node from its parent's children list.
	 * The parent and next field is cleared.
	 * @return The next sibling of this node, null if none.
	 */
	public ILLKNode detach() {
		if (parent == null)
			return null;
		ILLKNode c = parent.getFirst();
		if (equals(c)) {
			parent.setFirst(next);
		} else {
			ILLKNode t;
			while (!equals(t = c.getNext()))
				c = t;
			c.setNext(next);
		}
		c = next;
		parent = null;
		next = null;
		return c;
	}

	public ILLKToken getFirstToken() {
		return firstToken;
	}

	public void setFirstToken(ILLKToken t) {
		firstToken = t;
		if (t != null)
			setOffset(t.getOffset());
	}

	public ILLKToken getLastToken() {
		return lastToken;
	}

	public void setLastToken(ILLKToken t) {
		lastToken = t;
		if (t != null)
			setEndOffset(t.getEndOffset());
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public int getEndOffset() {
		return end;
	}

	public int getLength() {
		return end - offset;
	}

	public void setEndOffset(int end) {
		this.end = end;
	}

	public String getLocationString() {
		return ("(" + getOffset() + "-" + getEndOffset() + ")");
	}

	public ILLKNode[] structuralEquals(ILLKNode b) {
		if (b == null || getType() != b.getType() || childCount() != b.childCount())
			return new ILLKNode[] { this, b };
		CharSequence na = getText();
		CharSequence nb = b.getText();
		if (na == null && nb != null || na != null && !na.equals(nb))
			return new ILLKNode[] { this, b };
		for (ILLKNode t1 = getFirst(), t2 = b.getFirst(); t1 != null; t1 = t1.getNext(), t2 = t2.getNext()) {
			ILLKNode[] ret = t1.structuralEquals(t2);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public String toString() {
		return super.toString()
			+ getLocationString()
			+ String.format(": %s, modifiers=0x%08x", text, modifiers);
	}

	//////////////////////////////////////////////////////////////////////

	public boolean isEmpty() {
		return first == null;
	}

	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	public int indexOf(Object o) {
		if (!(o instanceof ILLKNode))
			return -1;
		return indexOf((ILLKNode)o);
	}

	public Iterator<ILLKNode> iterator() {
		return children().iterator();
	}

	public boolean remove(Object o) {
		if (!(o instanceof ILLKNode))
			return false;
		return remove((ILLKNode)o);
	}

	public void clear() {
		first = null;
	}

	public Object[] toArray() {
		ILLKNode[] ret = new ILLKNode[childCount()];
		int i = 0;
		for (ILLKNode n = getFirst(); n != null; n = n.getNext())
			ret[i++] = n;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int i = 0;
		for (ILLKNode n = getFirst(); n != null; n = n.getNext())
			a[i++] = (T)n;
		return a;
	}

	//////////////////////////////////////////////////////////////////////

	public void init(int mod, String text) {
		this.modifiers = mod;
		this.text = text;
	}

	public boolean hasModifier(int mod) {
		return (modifiers & mod) != 0;
	}

	public void addModifier(int mod) {
		modifiers |= mod;
	}

	public void removeModifier(int mod) {
		modifiers &= ~mod;
	}

	public <T> T accept(ILLKCSSParserVisitor<T> visitor, T data) {
		return visitor.visit(this, data);
	}

	public <T> T childrenAccept(ILLKCSSParserVisitor<T> visitor, T data) {
		for (ILLKNode n = first; n != null; n = n.getNext())
			((LLKNode)n).accept(visitor, data);
		return data;
	}

}
