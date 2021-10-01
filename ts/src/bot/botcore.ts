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
/// Core utility classes.

export type Byte = number;
export type ByteX = number | null;
export type ByteXX = number | null | undefined;
export type Int = number;
export type IntX = number | null;
export type IntXX = number | null | undefined;
export type Long = number;
export type LongX = number | null;
export type LongXX = number | null | undefined;
export type Float = number;
export type FloatX = number | null;
export type FloatXX = number | null | undefined;
export type Double = number;
export type DoubleX = number | null;
export type DoubleXX = number | null | undefined;

export type booleanX = boolean | null;
export type booleanXX = boolean | null | undefined;
export type numberX = number | null;
export type numberXX = number | null | undefined;
export type stringX = string | null;
export type stringXX = string | null | undefined;
export type ArrayX<T> = Array<T> | null;
export type ArrayXX<T> = Array<T> | null | undefined;
export type StringMapX<T> = StringMap<T> | null;
export type StringMapXX<T> = StringMap<T> | null | undefined;
export type JSONObject = StringMap<any>;
export type JSONObjectX = StringMap<any> | null;
export type JSONObjectXX = StringMap<any> | null | undefined;
export type JSONArray = Array<any>;
export type JSONArrayX = Array<any> | null;
export type JSONArrayXX = Array<any> | null | undefined;
export type ErrorX = Error | null;
export type ErrorXX = Error | null | undefined;

export type Fun00 = () => void;
export type Fun01<R> = () => R;
export type Fun10<T> = (arg1: T) => void;
export type Fun11<T, R> = (arg1: T) => R;
export type Fun20<T1, T2> = (arg1: T1, arg2: T2) => void;
export type Fun21<T1, T2, R> = (arg1: T1, arg2: T2) => R;
export type Fun30<T1, T2, T3> = (arg1: T1, arg2: T2, arg3: T3) => void;
export type Fun31<T1, T2, T3, R> = (arg1: T1, arg2: T2, arg3: T3) => R;
export type Fun40<T1, T2, T3, T4> = (arg1: T1, arg2: T2, arg3: T3, arg4: T4) => void;
export type Fun41<T1, T2, T3, T4, R> = (arg1: T1, arg2: T2, arg3: T3, arg4: T4) => R;

export type Fun00X = Fun00 | null;
export type Fun01X<R> = Fun01<R> | null;
export type Fun10X<T> = Fun10<T> | null;
export type Fun11X<T, R> = Fun11<T, R> | null;
export type Fun20X<T1, T2> = Fun20<T1, T2> | null;
export type Fun21X<T1, T2, R> = Fun21<T1, T2, R> | null;
export type Fun30X<T1, T2, T3> = Fun30<T1, T2, T3> | null;
export type Fun31X<T1, T2, T3, R> = Fun31<T1, T2, T3, R> | null;
export type Fun40X<T1, T2, T3, T4> = Fun40<T1, T2, T3, T4> | null;
export type Fun41X<T1, T2, T3, T4, R> = Fun41<T1, T2, T3, T4, R> | null;

export type Opt10<T> = (arg?: T) => void;

export interface ILogger {
    d_(msg: string): void;
    i_(msg: string): void;
    w_(msg: string, err?: any): void;
    e_(msg: string, err?: any): void;
}

export class Logger implements ILogger {

    protected warns$ = 0;
    protected errors$ = 0;

    constructor(protected debugging: boolean) { }

    d_(msg: string): void {
        if (this.debugging) console.log(msg);
    }

    i_(msg: string): void {
        console.log(msg);
    }

    w_(msg: string, err?: any): void {
        ++this.warns$;
        console.log(msg);
        if (this.debugging && err) console.log(`${err}`);
    }

    e_(msg: string, err?: any): void {
        ++this.errors$;
        console.log(msg);
        if (this.debugging && err) console.log(`${err}`);
    }
}

export class Atts {
    [name: string]: string;
}

export class HtmlWriter {
    private buf$ = "<!doctype html><html>";
    private stack$: string[] = [];
    private closed$ = false;
    constructor() { }
    start_(name: string, attrs?: Atts, empty: boolean = false): this {
        this.buf$ += `<${name}${this.attrs_(attrs)}${empty ? "/>" : ">"}`;
        if (!empty) this.stack$.push(name);
        return this;
    }
    end_(count: number = 1): this {
        while (--count >= 0) {
            let name = this.stack$.pop();
            this.buf$ += `</${name}>`;
        }
        return this;
    }
    text_(text: string): this {
        this.buf$ += HtmlWriter.escText_(text);
        return this;
    }
    close_(): this {
        if (!this.closed$) {
            this.buf$ += "</html>";
        }
        return this;
    }
    toString(): string {
        return this.buf$;
    }
    private attrs_(attrs?: Atts): string {
        if (!attrs) return "";
        let ret = "";
        for (let key in attrs) {
            let value = attrs[key];
            if (!value) {
                ret += ` ${key}`;
            } else {
                ret += ` ${key}="${HtmlWriter.escAttr_(value)}"`;
            }
        }
        return ret;
    }

    static escAttr_(value: string): string {
        let ret: string | null = null;
        let append = (index: number, c: string) => {
            if (ret == null) ret = value.substring(0, index) + c;
            else ret += c;
        };
        let len = value.length;
        for (let i = 0; i < len; ++i) {
            let c = value.charAt(i);
            switch (c) {
                case '"': append(i, "&quot;"); break;
                case '&': append(i, "&amp;"); break;
                default: if (ret != null) ret = ret + c; break;
            }
        }
        return (ret != null ? ret : value);
    }

    static escText_(value: string): string {
        let ret: string | null = null;
        let append = (index: number, c: string) => {
            if (ret == null) ret = value.substring(0, index) + c;
            else ret += c;
        };
        let len = value.length;
        for (let i = 0; i < len; ++i) {
            let c = value.charAt(i);
            switch (c) {
                case '\u00a0': append(i, "&nbsp;"); break;
                case '>': append(i, "&gt;"); break;
                case '<': append(i, "&lt;"); break;
                case '&': append(i, "&amp;"); break;
                default: if (ret != null) ret = ret + c; break;
            }
        }
        return (ret != null ? ret : value);
    }
}

export class Ut {

    static stopEvent_(e: Event): void {
        e.stopPropagation();
        e.preventDefault();
    }

    static let_<T, R>(o: T | null | undefined, code: Fun11<T, R>): R | null {
        return (o === null || o === undefined) ? null : code(o);
    }

    /// @return true If any of the args is NaN.
    static nan_(...args: number[]): boolean {
        return args.some((value) => Number.isNaN(value));
    }

    /// Basically Number.parseInt() but optionally return def instead of NaN.
    /// Note that radix comes after def.
    static parseInt_(value: string | null | undefined, def: number = NaN, radix: number = 10): number {
        if (value === null || value === undefined) return def;
        const ret = parseInt(value, radix);
        return Number.isNaN(ret) ? def : ret;
    }

    /// Call callback if result is an integer and not NaN.
    static parseInt2_(value: string | null | undefined, callback: Fun10<number>, radix: number = 10) {
        if (value === null || value === undefined) return;
        const ret = parseInt(value, radix);
        if (!Number.isNaN(ret)) callback(ret);
    }

    /// Basically Number.parseFloat() but return def instead of NaN.
    static parseDouble_(value: string | null | undefined, def: number = NaN): number {
        if (value === null || value === undefined) return def;
        try {
            const ret = parseFloat(value);
            return Number.isNaN(ret) ? def : ret;
        } catch (e) {
            return def;
        }
    }

    static parseDoublePx_(value: string | null | undefined, def: number = NaN): number {
        if (value == null || value === undefined || value.lastIndexOf("px") != value.length - 2) {
            return def;
        }
        return Ut.parseDouble_(value.substring(0, value.length - 2), def);
    }

    static padStart_(s: string, len: number, padchar = " "): string {
        if (padchar.length == 0) return s;
        while (s.length < len) {
            s = padchar + s;
        }
        return s;
    }

    static timeString_(ms: number, width: number = 6): string {
        if (ms >= 100 * 1000) {
            return Ut.padStart_(`${(ms / 1000).toFixed(0)}s`, width);
        } else if (ms >= 1000) {
            return Ut.padStart_(`${(ms / 1000).toFixed(2)}s`, width);
        }
        return Ut.padStart_(`${ms.toFixed(0)}ms`, width);
    }

    static spliceString_(s: string, start: number, length: number): string {
        if (length == 0) return s;
        if (start == 0 && length == s.length) return "";
        const prefix = (start == 0 ? "" : s.slice(0, start));
        const suffix = (start + length >= s.length ? "" : s.slice(start + length));
        return prefix.length == 0 ? suffix : suffix.length == 0 ? prefix : prefix + suffix;
    }

    static isEmpty_(s: string | null | undefined): boolean {
        return s === null || s === undefined || s.length == 0;
    }

    static isNotEmpty_(s: string | null | undefined): boolean {
        return !this.isEmpty_(s);
    }

    static repeatString_(s: string, count: number): string {
        let ret = "";
        while (--count >= 0) ret += s;
        return ret;
    }

    /// @return The UTF-16 char codes of the given string, undefined for invalid charcode.
    static charcodes_(s: string): Array<number | undefined> {
        const len = s.length;
        let ret = Array<number | undefined>(len);
        for (let index = 0; index < len; ++index) {
            const c = s.charCodeAt(index);
            ret.push((isNaN(c) ? undefined : c));
        }
        return ret;
    }

    /// @return The UTF-16 char codes of the given string.
    /// @throws Error on invalid charcode.
    static charcodesOrFail_(s: string): Array<number> {
        let ret = Array<number>(s.length);
        this.charcodesOf_(s, (c, index) => {
            if (c === undefined) throw new Error(`${s}@${index}`);
            ret.push(c);
        });
        return ret;
    }

    /// @callback(charcode, index, s) The UTF-16 char codes for the given string, 
    /// undefined for invalid char code, the index and the given string itself.
    static charcodesOf_(s: string, callback: Fun30<number | undefined, number, string>) {
        let len = s.length;
        for (let index = 0; index < len; ++index) {
            const c = s.charCodeAt(index);
            callback((isNaN(c) ? undefined : c), index, s);
        }
    }

    static isHighSurrogate_(charcode: number): boolean {
        return (0xD800 <= charcode && charcode <= 0xDBFF);
    }

    static isLowSurrogate_(charcode: number): boolean {
        return (0xDC00 <= charcode && charcode <= 0xDFFF);
    }

    static isSurrogatePair_(s: string, index: Int): Boolean {
        return index + 1 < s.length
            && this.isHighSurrogate_(s.charCodeAt(index))
            && this.isLowSurrogate_(s.charCodeAt(index + 1));
    }

    /// @return The Unicode 21 bits code points for the given string, undefined for invalid codepoint.
    static codepoints_(s: string): Array<number | undefined> {
        let ret = Array<number | undefined>();
        for (let index = 0, len = s.length; index < len; ++index) {
            ret.push(s.codePointAt(index));
            if (this.isSurrogatePair_(s, index)) ++index;
        }
        return ret;
    }

    /// @return The Unicode 21 bits code points of the given string.
    /// @throws Error on invalid code point.
    static codepointsOrFail_(s: string): Array<number> {
        let ret = Array<number>(s.length);
        this.codepointsOf_(s, (c, index) => {
            if (c === undefined) throw new Error(`${s}@${index}`);
            ret.push(c);
        });
        return ret;
    }

    /// @callback(codepoint, index, s) The Unicode code points for the given string, 
    /// undefined for invalid codepoint, the index and the input string itself.
    static codepointsOf_(s: string, callback: Fun30<number | undefined, number, string>) {
        let len = s.length;
        for (let index = 0; index < len; ++index) {
            callback(s.codePointAt(index), index, s);
            if (this.isSurrogatePair_(s, index)) ++index;
        }
    }

    /// Create a shadow copy of given Map as an object.
    static mapToObject<V>(map: Map<string, V>): object {
        let ret: { [key: string]: V; } = {};
        for (let [k, v] of map) {
            ret[k] = v;
        }
        return ret;
    }

    /// Create a shadow copy of properties of given object to a Map.
    static objectToMap<V>(obj: object): Map<string, V> {
        let ret = new Map<string, V>();
        for (let k of Object.keys(obj)) {
            ret.set(k, Object.getOwnPropertyDescriptor(obj, k)?.value);
        }
        return ret;
    }

    static asyncIterate_<T>(array: T[], callback: Fun30<T, Int, Opt10<boolean>>, done: Fun10X<Int> = null): void {
        this._asyncIterate(array, 0, callback, done);
    }

    static _asyncIterate<T>(array: T[], index: Int, callback: Fun30<T, Int, Opt10<boolean>>, done: Fun10X<Int> = null): void {
        callback(array[index], index, (terminate?: boolean) => {
            ++index;
            if (terminate === true || index >= array.length) {
                done?.(index);
                return;
            };
            this._asyncIterate(array, index, callback, done);
        });
    }

    static asyncLoop_(count: Int, callback: Fun20<Int, Opt10<boolean>>): void {
        if (count > 0) this._asyncLoop(count, 0, callback);
    }

    static _asyncLoop(count: Int, index: Int, callback: Fun20<Int, Opt10<boolean>>): void {
        callback(index, (terminate?: boolean) => {
            if (terminate === true || index + 1 >= count) return;
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
    static mixin_(dst: any, src: any, overwrite: boolean = true): any {
        if (!Ut.isObject_(dst)) {
            return src;
        }
        if (Ut.isObject_(src)) {
            Object.keys(src).forEach(key => {
                if (key in dst) {
                    if (overwrite) {
                        if (Ut.isObject_(dst[key]) && Ut.isObject_(src[key])) {
                            Ut.mixin_(dst[key], src[key], overwrite);
                        } else {
                            dst[key] = src[key];
                        }
                    }
                } else {
                    dst[key] = src[key];
                }
            });
        }
        return dst;
    }

    /// @returns whether the provided parameter is of type `object` but **not**
    /// `null`, an `array`, a `regexp`, nor a `date`.
    static isObject_(obj: any): obj is Object {
        return typeof obj === 'object'
            && obj !== null
            && !Array.isArray(obj)
            && !(obj instanceof RegExp)
            && !(obj instanceof Date);
    }
    //#END REGION VSCODE
}

export class JSONUt {
    static jsonObjectOrNull_(json: string): JSONObjectX {
        try {
            const ret = JSON.parse(json);
            if (typeof (ret) === "object") return ret;
        } catch (e: any) {
        }
        return null;
    }

    static jsonArrayOrNull_(json: string): JSONArrayX {
        try {
            const ret = JSON.parse(json);
            if (Array.isArray(ret)) return ret;
        } catch (e: any) {
        }
        return null;
    }

    static putJSONObject_(o: JSONObject, key: string): JSONObject {
        const ret = json_();
        o[key] = ret;
        return ret;
    }

    static putJSONArray_(o: JSONObject, key: string): JSONArray {
        const ret = new Array<any>();
        o[key] = ret;
        return ret;
    }

    static numberAt_(o: JSONObject, key: string): numberX {
        const value = o[key] ?? null;
        return (value != null && typeof (value) === "number") ? value : null;
    }

    static stringAt_(o: JSONObject, key: string): stringX {
        const value = o[key] ?? null;
        return (value != null && typeof (value) === "string") ? value : null;
    }

    static jsonArrayAt_(o: JSONObject, key: string): JSONArrayX {
        const value = o[key] ?? null;
        return (value != null && Array.isArray(value)) ? value : null;
    }

    static jsonObjectAt_(o: JSONObject, key: string): JSONObjectX {
        const value = o[key] ?? null;
        return (value != null && typeof (value) === "object") ? value : null;
    }
}

export class RandUt {
    /// @return A number from 0 inclusive to ceiling exclusive.
    static int1_(ceiling: Int): Int {
        return Math.floor(Math.random() * ceiling);
    }
    /// @return A number from lower inclusive to upper exclusive.
    static int2_(lower: Int, upper: Int): Int {
        return lower + Math.floor(Math.random() * upper);
    }
    static bytes_(length: Int): Byte[] {
        let ret = new Array<Byte>(length);
        for (let index = 0; index < length; ++index) {
            ret[index] = RandUt.int1_(256);
        }
        return ret;
    }
    static ints_(length: Int, ceiling: Int = Number.MAX_SAFE_INTEGER): Int[] {
        let ret = new Array<Int>(length);
        for (let index = 0; index < length; ++index) {
            ret[index] = RandUt.int1_(ceiling);
        }
        return ret;
    }
    static alpha_(length: Int): string {
        let bytes = new Array<Byte>(length);
        for (let index = 0; index < length;) {
            let c = this.int1_(0x7b - 0x41) + 0x41;
            if (c >= 0x41 && c <= 0x5a || c >= 0x61 && c <= 0x7a) {
                bytes[index] = c;
                ++index;
            }
        }
        return String.fromCharCode(...bytes);
    }
    static alphaNumeric_(length: Int): string {
        let bytes = new Array<Byte>(length);
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

export class TextUt {
    static readonly lineSep$ = "\n";
}

export class Attrs {
    [name: string]: string;
}

export function sarray_(): string[] {
    return new Array<string>();
}

export function map_<K, V>(...args: [K, V][]): Map<K, V> {
    return new Map<K, V>(args);
}

export function mapOf_<K, V>(key: K, value: V): Map<K, V> {
    return new Map<K, V>().set(key, value);
}

export function smap_<V>(...args: [string, V][]): StringMap<V> {
    return StringMap.from_(...args);
}

export function smapOf_<V>(key: string, value: V): StringMap<V> {
    return StringMap.of_(key, value);
}

export function ssmap_(...args: [string, string][]): StringMap<string> {
    return StringMap.from_(...args);
}

export function ssmapOf_(key: string, value: string): StringMap<string> {
    return StringMap.of_(key, value);
}

export function json_(...args: [string, any][]): JSONObject {
    return StringMap.from_(...args);
}

export function jsonOf_(key: string, value: any): JSONObject {
    return StringMap.of_(key, value);
}

export function replaceAll_(haystack: string, needle: string, replacement: string): string {
    return haystack.split(needle).join(replacement);
}

export class StringMap<V> {
    static of_<V>(key: string, value: V): StringMap<V> {
        let ret = new StringMap<V>();
        ret[key] = value;
        return ret;
    }
    static from_<V>(...args: [string, V][]): StringMap<V> {
        let ret = new StringMap<V>();
        for (let arg of args) {
            ret[arg[0]] = arg[1];
        }
        return ret;
    }
    /// Shadow copy of given StringMap.
    static copy_<V>(src: StringMap<V>): StringMap<V> {
        return this.from_(...Object.entries(src));
    }
    static filter_<V>(src: StringMap<V>, predicate: Fun11<[string, V], boolean>): StringMap<V> {
        return this.from_(...(Object.entries(src).filter(predicate)));
    }
    [key: string]: V;
}

export class NumberMap<V> extends Map<number, V> {
    static of_<V>(key: number, value: V) {
        return new NumberMap().set(key, value);
    }
    static from_<V>(...args: [number, V][]): NumberMap<V> {
        let ret = new NumberMap<V>();
        for (let arg of args) {
            ret.set(arg[0], arg[1]);
        }
        return ret;
    }
    [key: number]: V;
}

export class If {
    static null_<T>(value: T | null | undefined, code: Fun01<T>): T {
        return (value === null || value === undefined) ? code() : value;
    }

    static notnull_<T>(value: T | null | undefined, code: Fun11<T, T>): T | null {
        return (value !== null && value !== undefined) ? code(value) : null;
    }
}

export interface Closable {
    close(): void;
}

export class With {
    static closable_<T extends Closable, R>(closable: T, code: Fun11<T, R>): R {
        try {
            return code(closable);
        } finally {
            closable.close();
        }
    }

    static exceptionOrNull_(code: Fun00): any | null {
        try {
            code();
            return null;
        } catch (e: any) {
            return e;
        }
    }

    static exceptionOrFail_(code: Fun00) {
        try {
            code();
        } catch (e: any) {
            return;
        }
        throw new Error();
    }

    static range_(start: number, end: number, code: Fun10<number>) {
        for (let index = start; index < end!; ++index) {
            code(index);
        }
    }

    static lines_(input: string, code: Fun11<string, string | null>): string[] {
        let ret: string[] = [];
        input.split(TextUt.lineSep$).map((line) => {
            let output = code(line);
            if (output != null) {
                ret.push(output);
            }
        });
        return ret;
    }

    static let_<T, R>(arg: T, code: Fun11<T, R>): R {
        return code(arg);
    }

    static value_<T>(value: T, code: Fun10<T>): T {
        code(value);
        return value;
    }

    static optional_<T>(value: T | null | undefined, code: Fun10<T>): T | null | undefined {
        if (value === null || value === undefined) return;
        code(value);
        return value;
    }

    static optional0_<T>(value: T | null | undefined, code: Fun10<T>): T | null | undefined {
        if (value === null || value === undefined) return;
        setTimeout(() => code(value), 0);
        return value;
    }
}

export class Without {
    static exceptionOrNull_<T>(code: Fun01<T>): T | null {
        try {
            return code();
        } catch (e: any) {
            return null;
        }
    }

    static exceptionOrFail_<T>(code: Fun01<T>): T {
        try {
            return code();
        } catch (e: any) {
            throw new Error(`${e}`);
        }
    }
}

/// A shadow readonly wrapper of an Array.
export class ArrayIterable<V> implements Iterable<V> {
    constructor(private readonly items$: V[]) {
    }
    [Symbol.iterator](): IterableIterator<V> {
        return this.items$[Symbol.iterator]();
    }
    get length(): number {
        return this.items$.length;
    }
    get(index: number): V {
        return this.items$[index];
    }
    forEach(callback: Fun20<V, number>): void {
        this.items$.forEach((value, index) => callback(value, index));
    }
    filter(callback: Fun21<V, number, unknown>): V[] {
        return this.items$.filter((value, index) => { return callback(value, index); });
    }
    map<U>(callback: Fun21<V, number, U>): U[] {
        return this.items$.map((value, index) => { return callback(value, index); });
    }
}

export abstract class MapUt {
    static addToList<K, V>(ret: Map<K, V[]>, key: K, value: V): Map<K, V[]> {
        let array = ret.get(key) ?? null;
        if (array == null) {
            array = Array.of(value);
            ret.set(key, array);
        } else {
            array.push(value);
        }
        return ret;
    }
}

export class ArrayUt {
    static first_<T>(array: T[] | Iterator<T> | null | undefined): T | undefined {
        if (array == null) {
            return undefined;
        } else if (Array.isArray(array)) {
            let len = array.length;
            return len == 0 ? undefined : array[0];
        } else {
            let ret = array.next();
            if (ret.done === true) return undefined;
            return ret.value;
        }
    }

    static last_<T>(array: T[] | Iterator<T> | null | undefined): T | undefined {
        if (array === null || array === undefined) {
            return undefined;
        } else if (Array.isArray(array)) {
            let len = array.length;
            return (len == 0) ? undefined : array[len - 1];
        } else {
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

    static delete_<T>(array: T[], value: T): boolean {
        let index = array.indexOf(value);
        if (index >= 0) {
            array.splice(index, 1);
            return true;
        }
        return false;
    }

    static insert_<T>(array: T[], index: number, value: T): T[] {
        array.splice(index, 0, value);
        return array;
    }

    static addAll_<T>(to: T[], ...from: T[]): T[] {
        for (const e of from) {
            to.push(e);
        }
        return to;
    }

    static any_<T>(array: T[], predicate: Fun11<T, boolean>): boolean {
        for (const elm of array) {
            if (predicate(elm)) return true;
        }
        return false;
    }

    /// @return The index in array that the predicate returns true, otherwise -1.
    static findIndex_<T>(array: T[], predicate: Fun11<T, boolean>): number {
        let index = 0;
        for (const value of array) {
            if (predicate(value)) return index;
            ++index;
        }
        return -1;
    }

    /// @return The index in array that the predicate returns true, otherwise -1.
    static fill_<T>(array: T[], value: T): T[] {
        for (let index = 0, len = array.length; index < len; ++index) {
            array[index] = value;
        }
        return array;
    }

    static strictEqual_<T>(arr1: T[], arr2: T[]): boolean {
        const len = arr1.length;
        if (arr2.length !== len) return false;
        return arr1.every((value, index) => {
            return arr2[index] === value;
        });
    }

    static equal_<T>(arr1: T[], arr2: T[]): boolean {
        const len = arr1.length;
        if (arr2.length !== len) return false;
        return arr1.every((value, index) => {
            return arr2[index] == value;
        });
    }
}

export class Stack<T> {
    private x_array: T[] = [];
    length_(): Int {
        return this.x_array.length;
    }
    clear_(): this {
        this.x_array.length = 0;
        return this;
    }
    push_(...a: T[]): this {
        this.x_array.push(...a);
        return this;
    }
    pop_(): T | undefined {
        return this.x_array.pop();
    }
    peek_(): T | undefined {
        const len = this.x_array.length;
        return len > 0 ? this.x_array[len - 1] : undefined;
    }
}

/// A simple monotonic serial counter that wraps on overflow.
/// By default, it count from 0 inclusive to Long.MAX_VALUE exclusive.
export class Serial {
    constructor(
        protected readonly _start: Long = 0,
        protected readonly _end: Long = Number.MAX_SAFE_INTEGER,
        protected _serial: Long = _start
    ) { }

    get_(): Long {
        if (this._serial < this._start || this._serial == this._end) this._serial = this._start;
        return this._serial++;
    }

    reset_() {
        this._serial = this._start;
    }
}

export abstract class IBotResult<R, E> {
    abstract isOK_(): boolean;
    abstract isFail_(): boolean;
    abstract onFail_(callback: Fun10X<E>): IBotOKResult<R, E> | null;
}

export abstract class IBotOKResult<R, E> extends IBotResult<R, E> {
    abstract onOK_<S>(callback: Fun11<R, S>): S;
}

export class BotResult<R, E> implements IBotOKResult<R, E>{
    private constructor(
        private _result: R | null = null,
        private _failure: E | null = null
    ) { }

    static ok_<R, E>(result: R): IBotResult<R, E> {
        return new BotResult<R, E>(result, null);
    }

    static fail_<R, E>(error: E): IBotResult<R, E> {
        return new BotResult<R, E>(null, error);
    }

    isOK_(): boolean {
        return this._result != null;
    }

    isFail_(): boolean {
        return this._failure != null;
    }

    onOK_<S>(callback: Fun11<R, S>): S {
        return callback(this._result!!);
    }

    onFail_(callback: Fun10X<E> = null): IBotOKResult<R, E> | null {
        if (callback != null && this._failure != null) callback(this._failure);
        return (this._result != null) ? this : null;
    }
}
