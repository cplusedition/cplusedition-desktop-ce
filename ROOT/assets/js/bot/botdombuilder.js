"use strict";
var __classPrivateFieldSet = (this && this.__classPrivateFieldSet) || function (receiver, state, value, kind, f) {
    if (kind === "m") throw new TypeError("Private method is not writable");
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a setter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot write private member to an object whose class did not declare it");
    return (kind === "a" ? f.call(receiver, value) : f ? f.value = value : state.set(receiver, value)), value;
};
var __classPrivateFieldGet = (this && this.__classPrivateFieldGet) || function (receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
};
var _DomBuilder_stack, _DomBuilder_cursor;
Object.defineProperty(exports, "__esModule", { value: true });
exports.DomBuilder = void 0;
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
const botcore_1 = require("./botcore");
class DomBuilder {
    static attr1_(elm, key, value) {
        if (value == null) {
            elm.removeAttribute(key);
        }
        else {
            elm.setAttribute(key, value);
        }
        return elm;
    }
    static attrs1_(elm, attrs = null) {
        if (attrs != null) {
            for (const [k, v] of Object.entries(attrs)) {
                this.attr1_(elm, k, v);
            }
        }
        return elm;
    }
    /// Create a text node. 
    static createText_(content) {
        return new Text(content);
    }
    static offline1_(doc, tag, ...classes) {
        return new DomBuilder(doc.createElement(tag)).addClasses_(classes);
    }
    /// Create a DOM root that is not attached to a parent. 
    static offline_(doc, tag, attrs = null) {
        return new DomBuilder(doc.createElement(tag), attrs);
    }
    /// Use the given element as root of the tree. 
    constructor(cursor, attrs = null) {
        _DomBuilder_stack.set(this, new botcore_1.Stack());
        _DomBuilder_cursor.set(this, void 0);
        __classPrivateFieldSet(this, _DomBuilder_cursor, DomBuilder.attrs1_(cursor, attrs), "f");
    }
    /// This is basically a noop. However, it is useful for grouping statements in
    /// the same level and visualize nesting levels in typescript code. Typically,
    /// a group contains a matching push() and pop().
    /// Example: b.ul_().indent_(
    ///     b.push_().li_()...,
    ///     b.peek_().li_()...,
    ///     b.pop_().li_()...,
    /// )
    indent_(..._dontcare) {
        return this;
    }
    /// Add attributes to the cursor element.
    attr_(key, value) {
        DomBuilder.attr1_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"), key, value);
        return this;
    }
    attrs_(keyvalues) {
        DomBuilder.attrs1_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"), keyvalues);
        return this;
    }
    attrx_(attrs) {
        for (const attr of attrs) {
            if (attr instanceof Array) {
                this.attr_(attr[0], attr[1]);
            }
            else {
                this.attrs_(attr);
            }
        }
        return this;
    }
    addClass_(c) {
        if (c.length > 0)
            __classPrivateFieldGet(this, _DomBuilder_cursor, "f").classList.add(c);
        return this;
    }
    addClasses_(c) {
        for (const cc of c) {
            if (cc.length > 0)
                __classPrivateFieldGet(this, _DomBuilder_cursor, "f").classList.add(cc);
        }
        return this;
    }
    addStyle_(key, value) {
        const style = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").style;
        style.setProperty(key, value);
        return this;
    }
    addStyles_(styles) {
        const style = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").style;
        for (const [key, value] of Object.entries(styles)) {
            style.setProperty(key, value);
        }
        return this;
    }
    append_(child, attrs = null) {
        __classPrivateFieldGet(this, _DomBuilder_cursor, "f").appendChild(typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs));
        return this;
    }
    appendNodes_(...child) {
        for (const n of child) {
            this, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").appendChild(n);
        }
        return this;
    }
    append1_(child, ...classes) {
        return this.append_(child, (0, botcore_1.smapOf_)("class", classes.join(" ")));
    }
    append2_(child, name, value) {
        return this.append_(child, (0, botcore_1.smapOf_)(name, value));
    }
    /// Insert node/nodes before the next node as children to the cursor element.
    /// Overloads:
    ///     (Node node, String tag, [attrs])
    ///     (Node node, HTMLElement  newchild, [attrs])
    ///     (Node node, Array<Node> newchildren)
    ///     (Node node, Node newchild)
    insertBefore_(next, child, attrs = null) {
        if (child instanceof Array) {
            for (const n of child) {
                __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore(n, next);
            }
        }
        else {
            __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore(typeof (child) === "string"
                ? this.createElement_(child, attrs)
                : (child.nodeType == Node.ELEMENT_NODE)
                    ? DomBuilder.attrs1_(child, attrs)
                    : child, next);
        }
        return this;
    }
    prepend_(child, attrs = null) {
        return this.insertBefore_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild, child, attrs);
    }
    prependNodes_(...child) {
        const next = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild;
        for (const n of child) {
            this.insertBefore_(next, n);
        }
        return this;
    }
    prepend1_(child, ...classes) {
        return this.prepend_(child, (0, botcore_1.smapOf_)("class", classes.join(" ")));
    }
    prepend2_(child, name, value) {
        return this.prepend_(child, (0, botcore_1.smapOf_)(name, value));
    }
    /// Append a child and use it as cursor.
    /// Overloads:
    /// (String, [StringMap<String> attrs])
    /// (HTMLElement)
    child_(child, attrs = null) {
        __classPrivateFieldSet(this, _DomBuilder_cursor, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").appendChild((typeof (child) === "string")
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)), "f");
        return this;
    }
    /// Append a child and use it as cursor.
    child1_(child, ...classes) {
        return this.child_(child).addClasses_(classes);
    }
    /// Append a child and use it as cursor.
    child2_(child, name, value) {
        return this.child_(child).attr_(name, value);
    }
    /// Create and insert node as child of the cursor before the given next node.
    /// Overloads:
    /// (Node next, String tag, [StringMap<String> attrs])
    /// (Node next, HTMLElement child)
    childBefore_(next, child, attrs = null) {
        __classPrivateFieldSet(this, _DomBuilder_cursor, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)), next), "f");
        return this;
    }
    childBefore1_(next, tag, ...classes) {
        return this.childBefore_(next, tag).addClasses_(classes);
    }
    childBefore2_(next, tag, name, value) {
        return this.childBefore_(next, tag).attr_(name, value);
    }
    childBeforeFirst_(child, attrs = null) {
        return this.childBefore_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild, child, attrs);
    }
    childBeforeFirst1_(tag, ...classes) {
        return this.childBefore_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild, tag).addClasses_(classes);
    }
    childBeforeFirst2_(tag, name, value) {
        return this.childBefore_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild, tag).attr_(name, value);
    }
    /// Insert a sibling before the cursor and use it as new cursor.
    /// Overloads:
    /// (String tag, [StringMap<String> attrs])
    /// (HTMLElement elm)
    siblingBefore_(child, attrs = null) {
        __classPrivateFieldSet(this, _DomBuilder_cursor, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").parentNode.insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)), __classPrivateFieldGet(this, _DomBuilder_cursor, "f")), "f");
        return this;
    }
    /// Insert a sibling after the cursor and use it as new cursor.
    /// Overloads:
    /// (String tag, [StringMap<String> attrs])
    /// (HTMLElement elm)
    siblingAfter_(child, attrs = null) {
        __classPrivateFieldSet(this, _DomBuilder_cursor, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").parentNode.insertBefore((typeof (child) === "string"
            ? this.createElement_(child, attrs)
            : DomBuilder.attrs1_(child, attrs)), __classPrivateFieldGet(this, _DomBuilder_cursor, "f").nextSibling), "f");
        return this;
    }
    /// Append text as child to cursor. 
    text_(text) {
        __classPrivateFieldGet(this, _DomBuilder_cursor, "f").appendChild(DomBuilder.createText_(text));
        return this;
    }
    /// Append text as child to cursor. 
    textBefore_(next, text) {
        __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore(DomBuilder.createText_(text), next);
        return this;
    }
    /// Create a text node and insert as a sibling after the cursor. 
    textSiblingAfter_(text) {
        const textnode = DomBuilder.createText_(text);
        __classPrivateFieldGet(this, _DomBuilder_cursor, "f").parentNode.insertBefore(textnode, __classPrivateFieldGet(this, _DomBuilder_cursor, "f").nextSibling);
        return textnode;
    }
    /// Remove the given child from the current cursor element. 
    removeChild_(child) {
        var _a;
        (_a = child.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(child);
        return this;
    }
    removeChildren_(children) {
        var _a;
        for (const c of children) {
            (_a = c.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(c);
        }
        return this;
    }
    /// Replace the given child with the given bynode from the current cursor element. 
    replaceChild_(child, bynode) {
        var _a;
        __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore(bynode, child);
        (_a = child.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(child);
        return this;
    }
    /// Remove children start up to but not including end of the given element
    /// and append as children of cursor.
    moveChildren_(start, end) {
        var _a;
        let c = start;
        while (c != null && c != end) {
            const n = c.nextSibling;
            (_a = c.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(c);
            __classPrivateFieldGet(this, _DomBuilder_cursor, "f").append(c);
            c = n;
        }
        return this;
    }
    /// Append children of the given element and insert as children of
    /// cursor before the given next node.
    moveChildrenBefore_(next, start, end) {
        var _a;
        let c = start;
        while (c != null && c != end) {
            const n = c.nextSibling;
            (_a = c.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(c);
            __classPrivateFieldGet(this, _DomBuilder_cursor, "f").insertBefore(c, next);
            c = n;
        }
        return this;
    }
    /// Wrap the cursor with the given tag and use the wrapper as cursor.
    wrap_(tag, attrs) {
        const target = this.cursor_();
        this.up_().childBefore_(target, tag, attrs).push_().up_().removeChild_(target).pop_().appendNodes_(target);
        return this;
    }
    /// Move children of given child to as sibling before child and remove child. 
    unwrap_(child) {
        var _a;
        this.moveChildrenBefore_(child, child.firstChild, null);
        (_a = child.parentNode) === null || _a === void 0 ? void 0 : _a.removeChild(child);
        return this;
    }
    /// Remove all children of the cursor.
    empty_() {
        for (let c = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild; c != null;) {
            const n = c.nextSibling;
            c.remove();
            c = n;
        }
        return this;
    }
    /// Push cursor onto stack.
    push_() {
        __classPrivateFieldGet(this, _DomBuilder_stack, "f").push_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"));
        return this;
    }
    /// Pop the element from the cursor stack and use it as the cursor element.
    pop_(n = 1) {
        while (--n >= 0) {
            const e = __classPrivateFieldGet(this, _DomBuilder_stack, "f").pop_();
            if (e === undefined)
                throw new Error();
            __classPrivateFieldSet(this, _DomBuilder_cursor, e, "f");
        }
        return this;
    }
    /// Restore but do not remove the cursor element from top of the cursor stack.
    peek_() {
        __classPrivateFieldSet(this, _DomBuilder_cursor, __classPrivateFieldGet(this, _DomBuilder_stack, "f").peek_(), "f");
        return this;
    }
    /// Swap cursor with top of stack. 
    swap_() {
        const cursor = __classPrivateFieldGet(this, _DomBuilder_stack, "f").pop_();
        __classPrivateFieldGet(this, _DomBuilder_stack, "f").push_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"));
        __classPrivateFieldSet(this, _DomBuilder_cursor, cursor, "f");
        return this;
    }
    /// Use parent element of the cursor element.
    /// If it is null, throw an Error.
    up_() {
        const parent = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").parentElement;
        if (parent == null) {
            throw new Error();
        }
        __classPrivateFieldSet(this, _DomBuilder_cursor, parent, "f");
        return this;
    }
    /// Use the next sibling element of the cursor element.
    /// If it is null, throw an Error.
    right_() {
        const sibling = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").nextElementSibling;
        if (sibling == null) {
            throw new Error();
        }
        __classPrivateFieldSet(this, _DomBuilder_cursor, sibling, "f");
        return this;
    }
    /// Use the previous sibling element of the cursor element.
    /// If it is null, throw an Error.
    left_() {
        const sibling = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").previousElementSibling;
        if (sibling == null) {
            throw new Error();
        }
        __classPrivateFieldSet(this, _DomBuilder_cursor, sibling, "f");
        return this;
    }
    /// Use the first child element of the cursor element.
    /// If it is null, throw an Error.
    first_() {
        const child = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstElementChild;
        if (child == null) {
            throw new Error();
        }
        __classPrivateFieldSet(this, _DomBuilder_cursor, child, "f");
        return this;
    }
    setCursor_(elm) {
        __classPrivateFieldSet(this, _DomBuilder_cursor, elm, "f");
        return this;
    }
    cursor_() {
        return __classPrivateFieldGet(this, _DomBuilder_cursor, "f");
    }
    classList_() {
        return __classPrivateFieldGet(this, _DomBuilder_cursor, "f").classList;
    }
    /// @return The document of the cursor. 
    doc_() {
        return __classPrivateFieldGet(this, _DomBuilder_cursor, "f").ownerDocument;
    }
    /// Create a selection in the document.defaultView.
    /// Overloads:
    ///     () Select the cursor element.
    ///     (Node child) Select the given child node of the cursor.
    ///     (Node start, Node end) Select child start inclusive to child end exclusive of the cursor.
    /// If end is null, select from start to last child of the cursor.
    select_(start = null, end = null) {
        var _a;
        const selection = (_a = this.doc_().defaultView) === null || _a === void 0 ? void 0 : _a.getSelection();
        if (selection != null) {
            selection.removeAllRanges();
            selection.addRange(this.rangeOf_(start, end));
        }
        return this;
    }
    selectTextRange_(text, start, end) {
        var _a;
        const selection = (_a = this.doc_().defaultView) === null || _a === void 0 ? void 0 : _a.getSelection();
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
    scrollTo_(x, y) {
        var _a;
        (_a = this.doc_().defaultView) === null || _a === void 0 ? void 0 : _a.scrollTo(x, y);
        return this;
    }
    /// Execute the given function as fn(cursor()) and return this builder.
    exec_(fn) {
        fn(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"));
        return this;
    }
    /// Execute the given function on each child of the cursor, break if fn() return false.
    children_(fn) {
        for (let c = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild; c != null; c = c.nextSibling) {
            if (!fn(c, __classPrivateFieldGet(this, _DomBuilder_cursor, "f")))
                break;
        }
        return this;
    }
    /// Execute fn(child, cursor) for the given children of the cursor, break if fn() return false.
    each_(start, end, fn) {
        for (let c = start; c != null && c != end;) {
            const n = c.nextSibling;
            if (!fn(c, __classPrivateFieldGet(this, _DomBuilder_cursor, "f")))
                break;
            c = n;
        }
        return this;
    }
    /// Create an element with given tag and attributes.
    createElement_(tag, attrs = null) {
        return DomBuilder.attrs1_(__classPrivateFieldGet(this, _DomBuilder_cursor, "f").ownerDocument.createElement(tag), attrs);
    }
    /// @return Index of the given child node of the cursor.
    indexOf_(child) {
        let i = 0;
        for (let c = __classPrivateFieldGet(this, _DomBuilder_cursor, "f").firstChild; c != null; c = c.nextSibling, ++i) {
            if (c === child) {
                return i;
            }
        }
        return -1;
    }
    /// @param start A child of cursor. If null select the cursor.
    /// @param end A child of cursor, If null use lastChild.
    /// @return A range that start before start and end after end.
    rangeOf_(start, end = null) {
        var _a;
        const ret = this.createRange_();
        if (start == null) {
            ret.selectNode(__classPrivateFieldGet(this, _DomBuilder_cursor, "f"));
        }
        else {
            ret.setStartBefore(start);
            ret.setEndAfter((_a = end !== null && end !== void 0 ? end : __classPrivateFieldGet(this, _DomBuilder_cursor, "f").lastChild) !== null && _a !== void 0 ? _a : start);
        }
        return ret;
    }
    /// @return An empty range.
    createRange_() {
        return __classPrivateFieldGet(this, _DomBuilder_cursor, "f").ownerDocument.createRange();
    }
    /// Return the current cursor, but pop n element after it.
    cursorPop_(n = 1) {
        const ret = this.cursor_();
        this.pop_(n);
        return ret;
    }
    iframe_(...attrs) {
        return this.child_("iframe").attrx_(attrs);
    }
    iframe1_(...classes) {
        return this.child1_("iframe", ...classes);
    }
    div_(...attrs) {
        return this.child_("div").attrx_(attrs);
    }
    div1_(...classes) {
        return this.child1_("div", ...classes);
    }
    div2_(name, value) {
        return this.child2_("div", name, value);
    }
    span_(...attrs) {
        return this.child_("span").attrx_(attrs);
    }
    span1_(...classes) {
        return this.child1_("span", ...classes);
    }
    span2_(name, value) {
        return this.child2_("span", name, value);
    }
    br_(...attrs) {
        return this.child_("br").attrx_(attrs);
    }
    br1_(...classes) {
        return this.child1_("br", ...classes);
    }
    br2_(name, value) {
        return this.child2_("br", name, value);
    }
    hr_(...attrs) {
        return this.child_("hr").attrx_(attrs);
    }
    hr1_(...classes) {
        return this.child1_("hr", ...classes);
    }
    hr2_(name, value) {
        return this.child2_("hr", name, value);
    }
    ul_(...attrs) {
        return this.child_("ul").attrx_(attrs);
    }
    ul1_(...classes) {
        return this.child1_("ul", ...classes);
    }
    ul2_(name, value) {
        return this.child2_("ul", name, value);
    }
    li_(...attrs) {
        return this.child_("li").attrx_(attrs);
    }
    li1_(...classes) {
        return this.child1_("li", ...classes);
    }
    li2_(name, value) {
        return this.child2_("li", name, value);
    }
    input_(...attrs) {
        return this.child_("input").attrx_(attrs);
    }
    input1_(...classes) {
        return this.child1_("input", ...classes);
    }
    input2_(name, value) {
        return this.child2_("input", name, value);
    }
    textarea_(...attrs) {
        return this.child_("textarea").attrx_(attrs);
    }
    textarea1_(...classes) {
        return this.child1_("textarea", ...classes);
    }
    textarea2_(name, value) {
        return this.child2_("textarea", name, value);
    }
    table_(...attrs) {
        return this.child_("table").attrx_(attrs);
    }
    table1_(...classes) {
        return this.child1_("table", ...classes);
    }
    table2_(name, value) {
        return this.child2_("table", name, value);
    }
    tbody_(...attrs) {
        return this.child_("tbody").attrx_(attrs);
    }
    tbody1_(...classes) {
        return this.child1_("tbody", ...classes);
    }
    tbody2_(name, value) {
        return this.child2_("tbody", name, value);
    }
    tr_(...attrs) {
        return this.child_("tr").attrx_(attrs);
    }
    tr1_(...classes) {
        return this.child1_("tr", ...classes);
    }
    tr2_(name, value) {
        return this.child2_("tr", name, value);
    }
    td_(...attrs) {
        return this.child_("td").attrx_(attrs);
    }
    td1_(...classes) {
        return this.child1_("td", ...classes);
    }
    td2_(name, value) {
        return this.child2_("td", name, value);
    }
    th_(...attrs) {
        return this.child_("th").attrx_(attrs);
    }
    th1_(...classes) {
        return this.child1_("th", ...classes);
    }
    th2_(name, value) {
        return this.child2_("th", name, value);
    }
    button_(...attrs) {
        return this.child_("button").attrx_(attrs);
    }
    button1_(...classes) {
        return this.child1_("button", ...classes);
    }
    button2_(name, value) {
        return this.child2_("button", name, value);
    }
    b_(...attrs) {
        return this.child_("b").attrx_(attrs);
    }
    b1_(...classes) {
        return this.child1_("b", ...classes);
    }
    b2_(name, value) {
        return this.child2_("b", name, value);
    }
    code_(...attrs) {
        return this.child_("code").attrx_(attrs);
    }
    code1_(...classes) {
        return this.child1_("code", ...classes);
    }
    code2_(name, value) {
        return this.child2_("code", name, value);
    }
    a_(href = null, ...attrs) {
        if (href != null) {
            if (typeof (href) === "string") {
                if (href.length > 0)
                    attrs.unshift(["href", href]);
            }
            else {
                attrs.unshift(href);
            }
        }
        return this.child_("a").attrx_(attrs);
    }
    a1_(href, ...classes) {
        return this.a_(href).addClasses_(classes);
    }
    a2_(name, value) {
        return this.child2_("a", name, value);
    }
    img_(src = null, ...attrs) {
        if (src != null) {
            if (typeof (src) === "string") {
                if (src.length > 0)
                    attrs.unshift(["src", src]);
            }
            else {
                attrs.unshift(src);
            }
        }
        return this.child_("img").attrx_(attrs);
    }
    img1_(src, ...classes) {
        return this.img_(src).addClasses_(classes);
    }
    img2_(name, value) {
        return this.child2_("img", name, value);
    }
}
exports.DomBuilder = DomBuilder;
_DomBuilder_stack = new WeakMap(), _DomBuilder_cursor = new WeakMap();
//# sourceMappingURL=botdombuilder.js.map