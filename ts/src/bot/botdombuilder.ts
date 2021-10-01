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
import { Fun10, Fun21, smapOf_, Stack, StringMap, StringMapX, stringX } from "./botcore";

type _DomAttrs = [key: string, value: string] | StringMap<string>;

export class DomBuilder {
    private static attr1_(elm: HTMLElement, key: string, value: stringX): HTMLElement {
        if (value == null) {
            elm.removeAttribute(key);
        } else {
            elm.setAttribute(key, value);
        }
        return elm;
    }
    private static attrs1_(elm: HTMLElement, attrs: StringMap<stringX> | null = null): HTMLElement {
        if (attrs != null) {
            for (const [k, v] of Object.entries(attrs)) {
                this.attr1_(elm, k, v);
            }
        }
        return elm;
    }

    /// Create a text node. 
    static createText_(content: string): Text {
        return new Text(content);
    }

    static offline1_(doc: Document, tag: string, ...classes: string[]) {
        return new DomBuilder(doc.createElement(tag)).addClasses_(classes);
    }

    /// Create a DOM root that is not attached to a parent. 
    static offline_(doc: Document, tag: string, attrs: StringMap<stringX> | null = null): DomBuilder {
        return new DomBuilder(doc.createElement(tag), attrs);
    }

    #stack = new Stack<HTMLElement>();
    #cursor: HTMLElement;

    /// Use the given element as root of the tree. 
    constructor(cursor: HTMLElement, attrs: StringMap<stringX> | null = null) {
        this.#cursor = DomBuilder.attrs1_(cursor, attrs);
    }

    /// This is basically a noop. However, it is useful for grouping statements in
    /// the same level and visualize nesting levels in typescript code. Typically,
    /// a group contains a matching push() and pop().
    /// Example: b.ul_().indent_(
    ///     b.push_().li_()...,
    ///     b.peek_().li_()...,
    ///     b.pop_().li_()...,
    /// )
    indent_(..._dontcare: any[]): this {
        return this;
    }

    /// Add attributes to the cursor element.
    attr_(key: string, value: string): this {
        DomBuilder.attr1_(this.#cursor, key, value);
        return this;
    }

    attrs_(keyvalues: StringMap<stringX>): this {
        DomBuilder.attrs1_(this.#cursor, keyvalues);
        return this;
    }

    attrx_(attrs: _DomAttrs[]): this {
        for (const attr of attrs) {
            if (attr instanceof Array) {
                this.attr_(attr[0], attr[1]);
            } else {
                this.attrs_(attr);
            }
        }
        return this;
    }

    addClass_(c: string): this {
        if (c.length > 0) this.#cursor.classList.add(c);
        return this;
    }

    addClasses_(c: string[]): this {
        for (const cc of c) { if (cc.length > 0) this.#cursor.classList.add(cc); }
        return this;
    }

    addStyle_(key: string, value: string): this {
        const style = (this.#cursor as HTMLElement).style;
        style.setProperty(key, value);
        return this;
    }

    addStyles_(styles: StringMap<stringX>): this {
        const style = (this.#cursor as HTMLElement).style;
        for (const [key, value] of Object.entries(styles)) {
            style.setProperty(key, value);
        }
        return this;
    }

    append_(child: string | HTMLElement, attrs: StringMap<stringX> | null = null): this {
        this.#cursor.appendChild(typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs));
        return this;
    }

    appendNodes_(...child: Node[]): this {
        for (const n of child) {
            this, this.#cursor.appendChild(n);
        }
        return this;
    }

    append1_(child: string | HTMLElement, ...classes: string[]): this {
        return this.append_(child, smapOf_("class", classes.join(" ")));
    }

    append2_(child: string | HTMLElement, name: string, value: string): this {
        return this.append_(child, smapOf_(name, value));
    }

    /// Insert node/nodes before the next node as children to the cursor element.
    /// Overloads:
    ///     (Node node, String tag, [attrs])
    ///     (Node node, HTMLElement  newchild, [attrs])
    ///     (Node node, Array<Node> newchildren)
    ///     (Node node, Node newchild)
    insertBefore_(next: Node | null, child: string | HTMLElement | Array<Node> | Node, attrs: StringMapX<stringX> = null): this {
        if (child instanceof Array) {
            for (const n of child) {
                this.#cursor.insertBefore(n, next);
            }
        } else {
            this.#cursor.insertBefore(
                typeof (child) === "string"
                    ? this.createElement_(child, attrs)
                    : (child.nodeType == Node.ELEMENT_NODE)
                        ? DomBuilder.attrs1_(child as HTMLElement, attrs)
                        : child,
                next);
        }
        return this;
    }

    prepend_(child: string | HTMLElement, attrs: StringMap<string> | null = null): this {
        return this.insertBefore_(this.#cursor.firstChild, child, attrs);
    }

    prependNodes_(...child: Node[]): this {
        const next = this.#cursor.firstChild;
        for (const n of child) {
            this.insertBefore_(next, n);
        }
        return this;
    }

    prepend1_(child: string | HTMLElement, ...classes: string[]): this {
        return this.prepend_(child, smapOf_("class", classes.join(" ")));
    }

    prepend2_(child: string | HTMLElement, name: string, value: string): this {
        return this.prepend_(child, smapOf_(name, value));
    }

    /// Append a child and use it as cursor.
    /// Overloads:
    /// (String, [StringMap<String> attrs])
    /// (HTMLElement)
    child_(child: string | HTMLElement, attrs: StringMap<stringX> | null = null): this {
        this.#cursor = this.#cursor.appendChild((typeof (child) === "string")
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs));
        return this;
    }

    /// Append a child and use it as cursor.
    child1_(child: string | HTMLElement, ...classes: string[]): this {
        return this.child_(child).addClasses_(classes);
    }

    /// Append a child and use it as cursor.
    child2_(child: string | HTMLElement, name: string, value: string): this {
        return this.child_(child).attr_(name, value);
    }

    /// Create and insert node as child of the cursor before the given next node.
    /// Overloads:
    /// (Node next, String tag, [StringMap<String> attrs])
    /// (Node next, HTMLElement child)
    childBefore_(next: Node | null, child: string | HTMLElement, attrs: StringMap<stringX> | null = null): this {
        this.#cursor = this.#cursor.insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)),
            next);
        return this;
    }

    childBefore1_(next: Node | null, tag: string | HTMLElement, ...classes: string[]): this {
        return this.childBefore_(next, tag).addClasses_(classes);
    }

    childBefore2_(next: Node | null, tag: string | HTMLElement, name: string, value: string): this {
        return this.childBefore_(next, tag).attr_(name, value);
    }

    childBeforeFirst_(child: string | HTMLElement, attrs: StringMap<string> | null = null): this {
        return this.childBefore_(this.#cursor.firstChild, child, attrs);
    }

    childBeforeFirst1_(tag: string | HTMLElement, ...classes: string[]): this {
        return this.childBefore_(this.#cursor.firstChild, tag).addClasses_(classes);
    }

    childBeforeFirst2_(tag: string | HTMLElement, name: string, value: string): this {
        return this.childBefore_(this.#cursor.firstChild, tag).attr_(name, value);
    }

    /// Insert a sibling before the cursor and use it as new cursor.
    /// Overloads:
    /// (String tag, [StringMap<String> attrs])
    /// (HTMLElement elm)
    siblingBefore_(child: string | HTMLElement, attrs: StringMap<stringX> | null = null): this {
        this.#cursor = this.#cursor.parentNode!.insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)),
            this.#cursor);
        return this;
    }

    /// Insert a sibling after the cursor and use it as new cursor.
    /// Overloads:
    /// (String tag, [StringMap<String> attrs])
    /// (HTMLElement elm)
    siblingAfter_(child: string | HTMLElement, attrs: StringMap<stringX> | null = null): this {
        this.#cursor = this.#cursor.parentNode!.insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)),
            this.#cursor.nextSibling);
        return this;
    }

    /// Append text as child to cursor. 
    text_(text: string): this {
        this.#cursor.appendChild(DomBuilder.createText_(text));
        return this;
    }

    /// Append text as child to cursor. 
    textBefore_(next: Node | null, text: string): this {
        this.#cursor.insertBefore(DomBuilder.createText_(text), next);
        return this;
    }

    /// Create a text node and insert as a sibling after the cursor. 
    textSiblingAfter_(text: string): Text {
        const textnode = DomBuilder.createText_(text);
        this.#cursor.parentNode!.insertBefore(textnode, this.#cursor.nextSibling);
        return textnode;
    }

    /// Remove the given child from the current cursor element. 
    removeChild_(child: Node): this {
        child.parentNode?.removeChild(child);
        return this;
    }

    removeChildren_(children: Node[]): this {
        for (const c of children) {
            c.parentNode?.removeChild(c);
        }
        return this;
    }

    /// Replace the given child with the given bynode from the current cursor element. 
    replaceChild_(child: Node, bynode: Node): this {
        this.#cursor.insertBefore(bynode, child);
        child.parentNode?.removeChild(child);
        return this;
    }

    /// Remove children start up to but not including end of the given element
    /// and append as children of cursor.
    moveChildren_(start: Node | null, end: Node | null): this {
        let c: Node | null = start;
        while (c != null && c != end) {
            const n: Node | null = c.nextSibling;
            c.parentNode?.removeChild(c);
            this.#cursor.append(c);
            c = n;
        }
        return this;
    }

    /// Append children of the given element and insert as children of
    /// cursor before the given next node.
    moveChildrenBefore_(next: Node, start: Node | null, end: Node | null): this {
        let c: Node | null = start;
        while (c != null && c != end) {
            const n: Node | null = c.nextSibling;
            c.parentNode?.removeChild(c);
            this.#cursor.insertBefore(c, next);
            c = n;
        }
        return this;
    }

    /// Wrap the cursor with the given tag and use the wrapper as cursor.
    wrap_(tag: string | HTMLElement, attrs: StringMap<stringX>): this {
        const target = this.cursor_();
        this.up_().childBefore_(target, tag, attrs).push_().up_().removeChild_(target).pop_().appendNodes_(target);
        return this;
    }

    /// Move children of given child to as sibling before child and remove child. 
    unwrap_(child: Node): this {
        this.moveChildrenBefore_(child, child.firstChild, null);
        child.parentNode?.removeChild(child);
        return this;
    }

    /// Remove all children of the cursor.
    empty_(): this {
        for (let c = this.#cursor.firstChild; c != null;) {
            const n = c.nextSibling;
            c.remove();
            c = n;
        }
        return this;
    }

    /// Push cursor onto stack.
    push_(): this {
        this.#stack.push_(this.#cursor);
        return this;
    }

    /// Pop the element from the cursor stack and use it as the cursor element.
    pop_(n: number = 1): this {
        while (--n >= 0) {
            const e = this.#stack.pop_();
            if (e === undefined) throw new Error();
            this.#cursor = e;
        }
        return this;
    }

    /// Restore but do not remove the cursor element from top of the cursor stack.
    peek_(): this {
        this.#cursor = this.#stack.peek_()!;
        return this;
    }

    /// Swap cursor with top of stack. 
    swap_(): this {
        const cursor = this.#stack.pop_()!;
        this.#stack.push_(this.#cursor);
        this.#cursor = cursor;
        return this;
    }

    /// Use parent element of the cursor element.
    /// If it is null, throw an Error.
    up_(): this {
        const parent = this.#cursor.parentElement;
        if (parent == null) { throw new Error(); }
        this.#cursor = parent;
        return this;
    }

    /// Use the next sibling element of the cursor element.
    /// If it is null, throw an Error.
    right_(): this {
        const sibling = this.#cursor.nextElementSibling;
        if (sibling == null) { throw new Error(); }
        this.#cursor = sibling as HTMLElement;
        return this;
    }

    /// Use the previous sibling element of the cursor element.
    /// If it is null, throw an Error.
    left_(): this {
        const sibling = this.#cursor.previousElementSibling;
        if (sibling == null) { throw new Error(); }
        this.#cursor = sibling as HTMLElement;
        return this;
    }

    /// Use the first child element of the cursor element.
    /// If it is null, throw an Error.
    first_(): this {
        const child = this.#cursor.firstElementChild;
        if (child == null) { throw new Error(); }
        this.#cursor = child as HTMLElement;
        return this;
    }

    setCursor_(elm: HTMLElement): this {
        this.#cursor = elm;
        return this;
    }

    cursor_(): HTMLElement {
        return this.#cursor;
    }

    classList_(): DOMTokenList {
        return this.#cursor.classList;
    }

    /// @return The document of the cursor. 
    doc_(): Document {
        return this.#cursor.ownerDocument;
    }

    /// Create a selection in the document.defaultView.
    /// Overloads:
    ///     () Select the cursor element.
    ///     (Node child) Select the given child node of the cursor.
    ///     (Node start, Node end) Select child start inclusive to child end exclusive of the cursor.
    /// If end is null, select from start to last child of the cursor.
    select_(start: Node | null = null, end: Node | null = null): this {
        const selection = this.doc_().defaultView?.getSelection();
        if (selection != null) {
            selection.removeAllRanges();
            selection.addRange(this.rangeOf_(start, end));
        }
        return this;
    }

    selectTextRange_(text: Node, start: number, end: number): this {
        const selection = this.doc_().defaultView?.getSelection();
        if (selection != null) {
            const range = this.createRange_();
            range.setStart(text, start);
            range.setEnd(text, end);
            selection.removeAllRanges();
            selection.addRange(range);
        }
        return this;
    }

    /// ScrollTo the given x, y document coordinate in the document.defaultView
    scrollTo_(x: number, y: number): this {
        this.doc_().defaultView?.scrollTo(x, y);
        return this;
    }

    /// Execute the given function as fn(cursor()) and return this builder.
    exec_(fn: Fun10<HTMLElement>): this {
        fn(this.#cursor);
        return this;
    }

    /// Execute the given function on each child of the cursor, break if fn() return false.
    children_(fn: Fun21<Node, HTMLElement, boolean>): this {
        for (let c = this.#cursor.firstChild; c != null; c = c.nextSibling) {
            if (!fn(c, this.#cursor)) break;
        }
        return this;
    }

    /// Execute fn(child, cursor) for the given children of the cursor, break if fn() return false.
    each_(start: Node | null, end: Node | null, fn: Fun21<Node, HTMLElement, boolean>): this {
        for (let c = start; c != null && c != end;) {
            const n = c.nextSibling;
            if (!fn(c, this.#cursor)) break;
            c = n;
        }
        return this;
    }

    /// Create an element with given tag and attributes.
    createElement_(tag: string, attrs: StringMap<stringX> | null = null): HTMLElement {
        return DomBuilder.attrs1_(this.#cursor.ownerDocument.createElement(tag), attrs);
    }

    /// @return Index of the given child node of the cursor.
    indexOf_(child: Node): number {
        let i: number = 0;
        for (let c = this.#cursor.firstChild; c != null; c = c.nextSibling, ++i) {
            if (c === child) { return i; }
        }
        return -1;
    }

    /// @param start A child of cursor. If null select the cursor.
    /// @param end A child of cursor, If null use lastChild.
    /// @return A range that start before start and end after end.
    rangeOf_(start: Node | null, end: Node | null = null): Range {
        const ret: Range = this.createRange_();
        if (start == null) {
            ret.selectNode(this.#cursor);
        } else {
            ret.setStartBefore(start);
            ret.setEndAfter(end ?? this.#cursor.lastChild ?? start);
        }
        return ret;
    }

    /// @return An empty range.
    createRange_(): Range {
        return this.#cursor.ownerDocument.createRange();
    }

    /// Return the current cursor, but pop n element after it.
    cursorPop_(n: number = 1): HTMLElement {
        const ret = this.cursor_();
        this.pop_(n);
        return ret;
    }

    iframe_(...attrs: _DomAttrs[]): this {
        return this.child_("iframe").attrx_(attrs);
    }

    iframe1_(...classes: string[]): this {
        return this.child1_("iframe", ...classes);
    }

    div_(...attrs: _DomAttrs[]): this {
        return this.child_("div").attrx_(attrs);
    }

    div1_(...classes: string[]): this {
        return this.child1_("div", ...classes);
    }

    div2_(name: string, value: string): this {
        return this.child2_("div", name, value);
    }

    span_(...attrs: _DomAttrs[]): this {
        return this.child_("span").attrx_(attrs);
    }

    span1_(...classes: string[]): this {
        return this.child1_("span", ...classes);
    }

    span2_(name: string, value: string): this {
        return this.child2_("span", name, value);
    }

    br_(...attrs: _DomAttrs[]): this {
        return this.child_("br").attrx_(attrs);
    }

    br1_(...classes: string[]): this {
        return this.child1_("br", ...classes);
    }

    br2_(name: string, value: string): this {
        return this.child2_("br", name, value);
    }

    hr_(...attrs: _DomAttrs[]): this {
        return this.child_("hr").attrx_(attrs);
    }

    hr1_(...classes: string[]): this {
        return this.child1_("hr", ...classes);
    }

    hr2_(name: string, value: string): this {
        return this.child2_("hr", name, value);
    }

    ul_(...attrs: _DomAttrs[]): this {
        return this.child_("ul").attrx_(attrs);
    }

    ul1_(...classes: string[]): this {
        return this.child1_("ul", ...classes);
    }

    ul2_(name: string, value: string): this {
        return this.child2_("ul", name, value);
    }

    li_(...attrs: _DomAttrs[]): this {
        return this.child_("li").attrx_(attrs);
    }

    li1_(...classes: string[]): this {
        return this.child1_("li", ...classes);
    }

    li2_(name: string, value: string): this {
        return this.child2_("li", name, value);
    }

    input_(...attrs: _DomAttrs[]): this {
        return this.child_("input").attrx_(attrs);
    }

    input1_(...classes: string[]): this {
        return this.child1_("input", ...classes);
    }

    input2_(name: string, value: string): this {
        return this.child2_("input", name, value);
    }

    textarea_(...attrs: _DomAttrs[]): this {
        return this.child_("textarea").attrx_(attrs);
    }

    textarea1_(...classes: string[]): this {
        return this.child1_("textarea", ...classes);
    }

    textarea2_(name: string, value: string): this {
        return this.child2_("textarea", name, value);
    }

    table_(...attrs: _DomAttrs[]): this {
        return this.child_("table").attrx_(attrs);
    }

    table1_(...classes: string[]): this {
        return this.child1_("table", ...classes);
    }

    table2_(name: string, value: string): this {
        return this.child2_("table", name, value);
    }

    tbody_(...attrs: _DomAttrs[]): this {
        return this.child_("tbody").attrx_(attrs);
    }

    tbody1_(...classes: string[]): this {
        return this.child1_("tbody", ...classes);
    }

    tbody2_(name: string, value: string): this {
        return this.child2_("tbody", name, value);
    }

    tr_(...attrs: _DomAttrs[]): this {
        return this.child_("tr").attrx_(attrs);
    }

    tr1_(...classes: string[]): this {
        return this.child1_("tr", ...classes);
    }

    tr2_(name: string, value: string): this {
        return this.child2_("tr", name, value);
    }

    td_(...attrs: _DomAttrs[]): this {
        return this.child_("td").attrx_(attrs);
    }

    td1_(...classes: string[]): this {
        return this.child1_("td", ...classes);
    }

    td2_(name: string, value: string): this {
        return this.child2_("td", name, value);
    }

    th_(...attrs: _DomAttrs[]): this {
        return this.child_("th").attrx_(attrs);
    }

    th1_(...classes: string[]): this {
        return this.child1_("th", ...classes);
    }

    th2_(name: string, value: string): this {
        return this.child2_("th", name, value);
    }

    button_(...attrs: _DomAttrs[]): this {
        return this.child_("button").attrx_(attrs);
    }

    button1_(...classes: string[]): this {
        return this.child1_("button", ...classes);
    }

    button2_(name: string, value: string): this {
        return this.child2_("button", name, value);
    }

    b_(...attrs: _DomAttrs[]): this {
        return this.child_("b").attrx_(attrs);
    }

    b1_(...classes: string[]): this {
        return this.child1_("b", ...classes);
    }

    b2_(name: string, value: string): this {
        return this.child2_("b", name, value);
    }

    code_(...attrs: _DomAttrs[]): this {
        return this.child_("code").attrx_(attrs);
    }

    code1_(...classes: string[]): this {
        return this.child1_("code", ...classes);
    }

    code2_(name: string, value: string): this {
        return this.child2_("code", name, value);
    }

    a_(href: string | _DomAttrs | null = null, ...attrs: _DomAttrs[]): this {
        if (href != null) {
            if (typeof (href) === "string") {
                if (href.length > 0) attrs.unshift(["href", href]);
            } else {
                attrs.unshift(href);
            }
        }
        return this.child_("a").attrx_(attrs);
    }

    a1_(href: stringX, ...classes: string[]): this {
        return this.a_(href).addClasses_(classes);
    }

    a2_(name: string, value: string): this {
        return this.child2_("a", name, value);
    }

    img_(src: string | _DomAttrs | null = null, ...attrs: _DomAttrs[]): this {
        if (src != null) {
            if (typeof (src) === "string") {
                if (src.length > 0) attrs.unshift(["src", src]);
            } else {
                attrs.unshift(src);
            }
        }
        return this.child_("img").attrx_(attrs);
    }

    img1_(src: stringX, ...classes: string[]): this {
        return this.img_(src).addClasses_(classes);
    }

    img2_(name: string, value: string): this {
        return this.child2_("img", name, value);
    }
}
