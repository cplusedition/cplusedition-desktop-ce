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
"use strict";
/// Core utility classes.
Object.defineProperty(exports, "__esModule", { value: true });
exports.BotResult = exports.IBotOKResult = exports.IBotResult = exports.Serial = exports.Stack = exports.ArrayUt = exports.MapUt = exports.ArrayIterable = exports.Without = exports.With = exports.If = exports.NumberMap = exports.StringMap = exports.replaceAll_ = exports.jsonOf_ = exports.json_ = exports.ssmapOf_ = exports.ssmap_ = exports.smapOf_ = exports.smap_ = exports.mapOf_ = exports.map_ = exports.sarray_ = exports.Attrs = exports.TextUt = exports.RandUt = exports.JSONUt = exports.Ut = exports.HtmlWriter = exports.Atts = exports.Logger = void 0;
class Logger {
    constructor(debugging) {
        this.debugging = debugging;
        this.warns$ = 0;
        this.errors$ = 0;
    }
    d_(msg) {
        if (this.debugging)
            console.log(msg);
    }
    i_(msg) {
        console.log(msg);
    }
    w_(msg, err) {
        ++this.warns$;
        console.log(msg);
        if (this.debugging && err)
            console.log(`${err}`);
    }
    e_(msg, err) {
        ++this.errors$;
        console.log(msg);
        if (this.debugging && err)
            console.log(`${err}`);
    }
}
exports.Logger = Logger;
class Atts {
}
exports.Atts = Atts;
class HtmlWriter {
    constructor() {
        this.buf$ = "<!doctype html><html>";
        this.stack$ = [];
        this.closed$ = false;
    }
    start_(name, attrs, empty = false) {
        this.buf$ += `<${name}${this.attrs_(attrs)}${empty ? "/>" : ">"}`;
        if (!empty)
            this.stack$.push(name);
        return this;
    }
    end_(count = 1) {
        while (--count >= 0) {
            let name = this.stack$.pop();
            this.buf$ += `</${name}>`;
        }
        return this;
    }
    text_(text) {
        this.buf$ += HtmlWriter.escText_(text);
        return this;
    }
    close_() {
        if (!this.closed$) {
            this.buf$ += "</html>";
        }
        return this;
    }
    toString() {
        return this.buf$;
    }
    attrs_(attrs) {
        if (!attrs)
            return "";
        let ret = "";
        for (let key in attrs) {
            let value = attrs[key];
            if (!value) {
                ret += ` ${key}`;
            }
            else {
                ret += ` ${key}="${HtmlWriter.escAttr_(value)}"`;
            }
        }
        return ret;
    }
    static escAttr_(value) {
        let ret = null;
        let append = (index, c) => {
            if (ret == null)
                ret = value.substring(0, index) + c;
            else
                ret += c;
        };
        let len = value.length;
        for (let i = 0; i < len; ++i) {
            let c = value.charAt(i);
            switch (c) {
                case '"':
                    append(i, "&quot;");
                    break;
                case '&':
                    append(i, "&amp;");
                    break;
                default:
                    if (ret != null)
                        ret = ret + c;
                    break;
            }
        }
        return (ret != null ? ret : value);
    }
    static escText_(value) {
        let ret = null;
        let append = (index, c) => {
            if (ret == null)
                ret = value.substring(0, index) + c;
            else
                ret += c;
        };
        let len = value.length;
        for (let i = 0; i < len; ++i) {
            let c = value.charAt(i);
            switch (c) {
                case '\u00a0':
                    append(i, "&nbsp;");
                    break;
                case '>':
                    append(i, "&gt;");
                    break;
                case '<':
                    append(i, "&lt;");
                    break;
                case '&':
                    append(i, "&amp;");
                    break;
                default:
                    if (ret != null)
                        ret = ret + c;
                    break;
            }
        }
        return (ret != null ? ret : value);
    }
}
exports.HtmlWriter = HtmlWriter;
class Ut {
    static stopEvent_(e) {
        e.stopPropagation();
        e.preventDefault();
    }
    static let_(o, code) {
        return (o === null || o === undefined) ? null : code(o);
    }
    /// @return true If any of the args is NaN.
    static nan_(...args) {
        return args.some((value) => Number.isNaN(value));
    }
    /// Basically Number.parseInt() but optionally return def instead of NaN.
    /// Note that radix comes after def.
    static parseInt_(value, def = NaN, radix = 10) {
        if (value === null || value === undefined)
            return def;
        const ret = parseInt(value, radix);
        return Number.isNaN(ret) ? def : ret;
    }
    /// Call callback if result is an integer and not NaN.
    static parseInt2_(value, callback, radix = 10) {
        if (value === null || value === undefined)
            return;
        const ret = parseInt(value, radix);
        if (!Number.isNaN(ret))
            callback(ret);
    }
    /// Basically Number.parseFloat() but return def instead of NaN.
    static parseDouble_(value, def = NaN) {
        if (value === null || value === undefined)
            return def;
        try {
            const ret = parseFloat(value);
            return Number.isNaN(ret) ? def : ret;
        }
        catch (e) {
            return def;
        }
    }
    static parseDoublePx_(value, def = NaN) {
        if (value == null || value === undefined || value.lastIndexOf("px") != value.length - 2) {
            return def;
        }
        return Ut.parseDouble_(value.substring(0, value.length - 2), def);
    }
    static padStart_(s, len, padchar = " ") {
        if (padchar.length == 0)
            return s;
        while (s.length < len) {
            s = padchar + s;
        }
        return s;
    }
    static timeString_(ms, width = 6) {
        if (ms >= 100 * 1000) {
            return Ut.padStart_(`${(ms / 1000).toFixed(0)}s`, width);
        }
        else if (ms >= 1000) {
            return Ut.padStart_(`${(ms / 1000).toFixed(2)}s`, width);
        }
        return Ut.padStart_(`${ms.toFixed(0)}ms`, width);
    }
    static spliceString_(s, start, length) {
        if (length == 0)
            return s;
        if (start == 0 && length == s.length)
            return "";
        const prefix = (start == 0 ? "" : s.slice(0, start));
        const suffix = (start + length >= s.length ? "" : s.slice(start + length));
        return prefix.length == 0 ? suffix : suffix.length == 0 ? prefix : prefix + suffix;
    }
    static isEmpty_(s) {
        return s === null || s === undefined || s.length == 0;
    }
    static isNotEmpty_(s) {
        return !this.isEmpty_(s);
    }
    static repeatString_(s, count) {
        let ret = "";
        while (--count >= 0)
            ret += s;
        return ret;
    }
    /// @return The UTF-16 char codes of the given string, undefined for invalid charcode.
    static charcodes_(s) {
        const len = s.length;
        let ret = Array(len);
        for (let index = 0; index < len; ++index) {
            const c = s.charCodeAt(index);
            ret.push((isNaN(c) ? undefined : c));
        }
        return ret;
    }
    /// @return The UTF-16 char codes of the given string.
    /// @throws Error on invalid charcode.
    static charcodesOrFail_(s) {
        let ret = Array(s.length);
        this.charcodesOf_(s, (c, index) => {
            if (c === undefined)
                throw new Error(`${s}@${index}`);
            ret.push(c);
        });
        return ret;
    }
    /// @callback(charcode, index, s) The UTF-16 char codes for the given string, 
    /// undefined for invalid char code, the index and the given string itself.
    static charcodesOf_(s, callback) {
        let len = s.length;
        for (let index = 0; index < len; ++index) {
            const c = s.charCodeAt(index);
            callback((isNaN(c) ? undefined : c), index, s);
        }
    }
    static isHighSurrogate_(charcode) {
        return (0xD800 <= charcode && charcode <= 0xDBFF);
    }
    static isLowSurrogate_(charcode) {
        return (0xDC00 <= charcode && charcode <= 0xDFFF);
    }
    static isSurrogatePair_(s, index) {
        return index + 1 < s.length
            && this.isHighSurrogate_(s.charCodeAt(index))
            && this.isLowSurrogate_(s.charCodeAt(index + 1));
    }
    /// @return The Unicode 21 bits code points for the given string, undefined for invalid codepoint.
    static codepoints_(s) {
        let ret = Array();
        for (let index = 0, len = s.length; index < len; ++index) {
            ret.push(s.codePointAt(index));
            if (this.isSurrogatePair_(s, index))
                ++index;
        }
        return ret;
    }
    /// @return The Unicode 21 bits code points of the given string.
    /// @throws Error on invalid code point.
    static codepointsOrFail_(s) {
        let ret = Array(s.length);
        this.codepointsOf_(s, (c, index) => {
            if (c === undefined)
                throw new Error(`${s}@${index}`);
            ret.push(c);
        });
        return ret;
    }
    /// @callback(codepoint, index, s) The Unicode code points for the given string, 
    /// undefined for invalid codepoint, the index and the input string itself.
    static codepointsOf_(s, callback) {
        let len = s.length;
        for (let index = 0; index < len; ++index) {
            callback(s.codePointAt(index), index, s);
            if (this.isSurrogatePair_(s, index))
                ++index;
        }
    }
    /// Create a shadow copy of given Map as an object.
    static mapToObject(map) {
        let ret = {};
        for (let [k, v] of map) {
            ret[k] = v;
        }
        return ret;
    }
    /// Create a shadow copy of properties of given object to a Map.
    static objectToMap(obj) {
        var _a;
        let ret = new Map();
        for (let k of Object.keys(obj)) {
            ret.set(k, (_a = Object.getOwnPropertyDescriptor(obj, k)) === null || _a === void 0 ? void 0 : _a.value);
        }
        return ret;
    }
    static asyncIterate_(array, callback, done = null) {
        this._asyncIterate(array, 0, callback, done);
    }
    static _asyncIterate(array, index, callback, done = null) {
        callback(array[index], index, (terminate) => {
            ++index;
            if (terminate === true || index >= array.length) {
                done === null || done === void 0 ? void 0 : done(index);
                return;
            }
            ;
            this._asyncIterate(array, index, callback, done);
        });
    }
    static asyncLoop_(count, callback) {
        if (count > 0)
            this._asyncLoop(count, 0, callback);
    }
    static _asyncLoop(count, index, callback) {
        callback(index, (terminate) => {
            if (terminate === true || index + 1 >= count)
                return;
            this._asyncLoop(count, index + 1, callback);
        });
    }
    //#BEGIN REGION VSCODE
    /*---------------------------------------------------------------------------------------------
     *  Copyright (c) Microsoft Corporation. All rights reserved.
     *  Licensed under the MIT License. See License.txt in the project root for license information.
     *--------------------------------------------------------------------------------------------*/
    /// Deep copy properties of source into destination. The optional parameter "overwrite" allows to control
    /// if existing properties on the destination should be overwritten or not. Defaults to true (overwrite).
    static mixin_(dst, src, overwrite = true) {
        if (!Ut.isObject_(dst)) {
            return src;
        }
        if (Ut.isObject_(src)) {
            Object.keys(src).forEach(key => {
                if (key in dst) {
                    if (overwrite) {
                        if (Ut.isObject_(dst[key]) && Ut.isObject_(src[key])) {
                            Ut.mixin_(dst[key], src[key], overwrite);
                        }
                        else {
                            dst[key] = src[key];
                        }
                    }
                }
                else {
                    dst[key] = src[key];
                }
            });
        }
        return dst;
    }
    /// @returns whether the provided parameter is of type `object` but **not**
    /// `null`, an `array`, a `regexp`, nor a `date`.
    static isObject_(obj) {
        return typeof obj === 'object'
            && obj !== null
            && !Array.isArray(obj)
            && !(obj instanceof RegExp)
            && !(obj instanceof Date);
    }
}
exports.Ut = Ut;
class JSONUt {
    static jsonObjectOrNull_(json) {
        try {
            const ret = JSON.parse(json);
            if (typeof (ret) === "object")
                return ret;
        }
        catch (e) {
        }
        return null;
    }
    static jsonArrayOrNull_(json) {
        try {
            const ret = JSON.parse(json);
            if (Array.isArray(ret))
                return ret;
        }
        catch (e) {
        }
        return null;
    }
    static putJSONObject_(o, key) {
        const ret = json_();
        o[key] = ret;
        return ret;
    }
    static putJSONArray_(o, key) {
        const ret = new Array();
        o[key] = ret;
        return ret;
    }
    static numberAt_(o, key) {
        var _a;
        const value = (_a = o[key]) !== null && _a !== void 0 ? _a : null;
        return (value != null && typeof (value) === "number") ? value : null;
    }
    static stringAt_(o, key) {
        var _a;
        const value = (_a = o[key]) !== null && _a !== void 0 ? _a : null;
        return (value != null && typeof (value) === "string") ? value : null;
    }
    static jsonArrayAt_(o, key) {
        var _a;
        const value = (_a = o[key]) !== null && _a !== void 0 ? _a : null;
        return (value != null && Array.isArray(value)) ? value : null;
    }
    static jsonObjectAt_(o, key) {
        var _a;
        const value = (_a = o[key]) !== null && _a !== void 0 ? _a : null;
        return (value != null && typeof (value) === "object") ? value : null;
    }
}
exports.JSONUt = JSONUt;
class RandUt {
    /// @return A number from 0 inclusive to ceiling exclusive.
    static int1_(ceiling) {
        return Math.floor(Math.random() * ceiling);
    }
    /// @return A number from lower inclusive to upper exclusive.
    static int2_(lower, upper) {
        return lower + Math.floor(Math.random() * upper);
    }
    static bytes_(length) {
        let ret = new Array(length);
        for (let index = 0; index < length; ++index) {
            ret[index] = RandUt.int1_(256);
        }
        return ret;
    }
    static ints_(length, ceiling = Number.MAX_SAFE_INTEGER) {
        let ret = new Array(length);
        for (let index = 0; index < length; ++index) {
            ret[index] = RandUt.int1_(ceiling);
        }
        return ret;
    }
    static alpha_(length) {
        let bytes = new Array(length);
        for (let index = 0; index < length;) {
            let c = this.int1_(0x7b - 0x41) + 0x41;
            if (c >= 0x41 && c <= 0x5a || c >= 0x61 && c <= 0x7a) {
                bytes[index] = c;
                ++index;
            }
        }
        return String.fromCharCode(...bytes);
    }
    static alphaNumeric_(length) {
        let bytes = new Array(length);
        for (let index = 0; index < length;) {
            let c = this.int1_(0x7b - 0x30) + 0x30;
            if (c >= 0x41 && c <= 0x5a || c >= 0x61 && c <= 0x7a || c >= 0x30 && c <= 0x39) {
                bytes[index] = c;
                ++index;
            }
        }
        return String.fromCharCode(...bytes);
    }
}
exports.RandUt = RandUt;
class TextUt {
}
exports.TextUt = TextUt;
TextUt.lineSep$ = "\n";
class Attrs {
}
exports.Attrs = Attrs;
function sarray_() {
    return new Array();
}
exports.sarray_ = sarray_;
function map_(...args) {
    return new Map(args);
}
exports.map_ = map_;
function mapOf_(key, value) {
    return new Map().set(key, value);
}
exports.mapOf_ = mapOf_;
function smap_(...args) {
    return StringMap.from_(...args);
}
exports.smap_ = smap_;
function smapOf_(key, value) {
    return StringMap.of_(key, value);
}
exports.smapOf_ = smapOf_;
function ssmap_(...args) {
    return StringMap.from_(...args);
}
exports.ssmap_ = ssmap_;
function ssmapOf_(key, value) {
    return StringMap.of_(key, value);
}
exports.ssmapOf_ = ssmapOf_;
function json_(...args) {
    return StringMap.from_(...args);
}
exports.json_ = json_;
function jsonOf_(key, value) {
    return StringMap.of_(key, value);
}
exports.jsonOf_ = jsonOf_;
function replaceAll_(haystack, needle, replacement) {
    return haystack.split(needle).join(replacement);
}
exports.replaceAll_ = replaceAll_;
class StringMap {
    static of_(key, value) {
        let ret = new StringMap();
        ret[key] = value;
        return ret;
    }
    static from_(...args) {
        let ret = new StringMap();
        for (let arg of args) {
            ret[arg[0]] = arg[1];
        }
        return ret;
    }
    /// Shadow copy of given StringMap.
    static copy_(src) {
        return this.from_(...Object.entries(src));
    }
    static filter_(src, predicate) {
        return this.from_(...(Object.entries(src).filter(predicate)));
    }
}
exports.StringMap = StringMap;
class NumberMap extends Map {
    static of_(key, value) {
        return new NumberMap().set(key, value);
    }
    static from_(...args) {
        let ret = new NumberMap();
        for (let arg of args) {
            ret.set(arg[0], arg[1]);
        }
        return ret;
    }
}
exports.NumberMap = NumberMap;
class If {
    static null_(value, code) {
        return (value === null || value === undefined) ? code() : value;
    }
    static notnull_(value, code) {
        return (value !== null && value !== undefined) ? code(value) : null;
    }
}
exports.If = If;
class With {
    static closable_(closable, code) {
        try {
            return code(closable);
        }
        finally {
            closable.close();
        }
    }
    static exceptionOrNull_(code) {
        try {
            code();
            return null;
        }
        catch (e) {
            return e;
        }
    }
    static exceptionOrFail_(code) {
        try {
            code();
        }
        catch (e) {
            return;
        }
        throw new Error();
    }
    static range_(start, end, code) {
        for (let index = start; index < end; ++index) {
            code(index);
        }
    }
    static lines_(input, code) {
        let ret = [];
        input.split(TextUt.lineSep$).map((line) => {
            let output = code(line);
            if (output != null) {
                ret.push(output);
            }
        });
        return ret;
    }
    static let_(arg, code) {
        return code(arg);
    }
    static value_(value, code) {
        code(value);
        return value;
    }
    static optional_(value, code) {
        if (value === null || value === undefined)
            return;
        code(value);
        return value;
    }
    static optional0_(value, code) {
        if (value === null || value === undefined)
            return;
        setTimeout(() => code(value), 0);
        return value;
    }
}
exports.With = With;
class Without {
    static exceptionOrNull_(code) {
        try {
            return code();
        }
        catch (e) {
            return null;
        }
    }
    static exceptionOrFail_(code) {
        try {
            return code();
        }
        catch (e) {
            throw new Error(`${e}`);
        }
    }
}
exports.Without = Without;
/// A shadow readonly wrapper of an Array.
class ArrayIterable {
    constructor(items$) {
        this.items$ = items$;
    }
    [Symbol.iterator]() {
        return this.items$[Symbol.iterator]();
    }
    get length() {
        return this.items$.length;
    }
    get(index) {
        return this.items$[index];
    }
    forEach(callback) {
        this.items$.forEach((value, index) => callback(value, index));
    }
    filter(callback) {
        return this.items$.filter((value, index) => { return callback(value, index); });
    }
    map(callback) {
        return this.items$.map((value, index) => { return callback(value, index); });
    }
}
exports.ArrayIterable = ArrayIterable;
class MapUt {
    static addToList(ret, key, value) {
        var _a;
        let array = (_a = ret.get(key)) !== null && _a !== void 0 ? _a : null;
        if (array == null) {
            array = Array.of(value);
            ret.set(key, array);
        }
        else {
            array.push(value);
        }
        return ret;
    }
}
exports.MapUt = MapUt;
class ArrayUt {
    static first_(array) {
        if (array == null) {
            return undefined;
        }
        else if (Array.isArray(array)) {
            let len = array.length;
            return len == 0 ? undefined : array[0];
        }
        else {
            let ret = array.next();
            if (ret.done === true)
                return undefined;
            return ret.value;
        }
    }
    static last_(array) {
        if (array === null || array === undefined) {
            return undefined;
        }
        else if (Array.isArray(array)) {
            let len = array.length;
            return (len == 0) ? undefined : array[len - 1];
        }
        else {
            let ret = undefined;
            while (true) {
                let result = array.next();
                if (result.done !== true) {
                    ret = result.value;
                    continue;
                }
                return ret;
            }
        }
    }
    static delete_(array, value) {
        let index = array.indexOf(value);
        if (index >= 0) {
            array.splice(index, 1);
            return true;
        }
        return false;
    }
    static insert_(array, index, value) {
        array.splice(index, 0, value);
        return array;
    }
    static addAll_(to, ...from) {
        for (const e of from) {
            to.push(e);
        }
        return to;
    }
    static any_(array, predicate) {
        for (const elm of array) {
            if (predicate(elm))
                return true;
        }
        return false;
    }
    /// @return The index in array that the predicate returns true, otherwise -1.
    static findIndex_(array, predicate) {
        let index = 0;
        for (const value of array) {
            if (predicate(value))
                return index;
            ++index;
        }
        return -1;
    }
    /// @return The index in array that the predicate returns true, otherwise -1.
    static fill_(array, value) {
        for (let index = 0, len = array.length; index < len; ++index) {
            array[index] = value;
        }
        return array;
    }
    static strictEqual_(arr1, arr2) {
        const len = arr1.length;
        if (arr2.length !== len)
            return false;
        return arr1.every((value, index) => {
            return arr2[index] === value;
        });
    }
    static equal_(arr1, arr2) {
        const len = arr1.length;
        if (arr2.length !== len)
            return false;
        return arr1.every((value, index) => {
            return arr2[index] == value;
        });
    }
}
exports.ArrayUt = ArrayUt;
class Stack {
    constructor() {
        this.x_array = [];
    }
    length_() {
        return this.x_array.length;
    }
    clear_() {
        this.x_array.length = 0;
        return this;
    }
    push_(...a) {
        this.x_array.push(...a);
        return this;
    }
    pop_() {
        return this.x_array.pop();
    }
    peek_() {
        const len = this.x_array.length;
        return len > 0 ? this.x_array[len - 1] : undefined;
    }
}
exports.Stack = Stack;
/// A simple monotonic serial counter that wraps on overflow.
/// By default, it count from 0 inclusive to Long.MAX_VALUE exclusive.
class Serial {
    constructor(_start = 0, _end = Number.MAX_SAFE_INTEGER, _serial = _start) {
        this._start = _start;
        this._end = _end;
        this._serial = _serial;
    }
    get_() {
        if (this._serial < this._start || this._serial == this._end)
            this._serial = this._start;
        return this._serial++;
    }
    reset_() {
        this._serial = this._start;
    }
}
exports.Serial = Serial;
class IBotResult {
}
exports.IBotResult = IBotResult;
class IBotOKResult extends IBotResult {
}
exports.IBotOKResult = IBotOKResult;
class BotResult {
    constructor(_result = null, _failure = null) {
        this._result = _result;
        this._failure = _failure;
    }
    static ok_(result) {
        return new BotResult(result, null);
    }
    static fail_(error) {
        return new BotResult(null, error);
    }
    isOK_() {
        return this._result != null;
    }
    isFail_() {
        return this._failure != null;
    }
    onOK_(callback) {
        return callback(this._result);
    }
    onFail_(callback = null) {
        if (callback != null && this._failure != null)
            callback(this._failure);
        return (this._result != null) ? this : null;
    }
}
exports.BotResult = BotResult;
//# sourceMappingURL=botcore.js.map