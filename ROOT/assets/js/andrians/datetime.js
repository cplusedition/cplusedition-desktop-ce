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
Object.defineProperty(exports, "__esModule", { value: true });
exports.DateTime = void 0;
class DateTime {
    /// @date Either ms in UTC or a Date object.
    /// @options.isUtc Specify if date parameter is utc or local time. Default is utc.
    constructor(date, isUtc) {
        this._date = (date instanceof Date ? date : new Date(date));
        if (isUtc === false) {
            this._date = new Date(this._date.valueOf() + this._date.getTimezoneOffset() * DateTime.MIN);
        }
    }
    static local_(year, month, // 1..12
    day, // 1..31
    hour, // 0..23
    minute, // 0..59
    second, // 0..59
    ms) {
        return new DateTime(Date.UTC(year, month - 1, day !== null && day !== void 0 ? day : 1, hour !== null && hour !== void 0 ? hour : 0, minute !== null && minute !== void 0 ? minute : 0, second !== null && second !== void 0 ? second : 0, ms !== null && ms !== void 0 ? ms : 0), false);
    }
    static utc_(year, month, // 1..12
    day, // 1..31
    hour, // 0..23
    minute, // 0..59
    second, // 0..59
    ms) {
        return new DateTime(Date.UTC(year, month - 1, day !== null && day !== void 0 ? day : 1, hour !== null && hour !== void 0 ? hour : 0, minute !== null && minute !== void 0 ? minute : 0, second !== null && second !== void 0 ? second : 0, ms !== null && ms !== void 0 ? ms : 0), true);
    }
    /// @param ms UTC time in ms since 01/01/1970.
    /// @param isUtc default is true.
    static fromMillisecondsSinceEpoch_(ms, isUtc) {
        return new DateTime(ms, isUtc);
    }
    static now_() {
        return new DateTime(new Date());
    }
    static ms_() {
        return Date.now();
    }
    /// @return Today string in form yyyymmdd
    static today_() {
        return DateTime.yyyymmdd_(this.now_());
    }
    static yyyymmdd_(date, sep = "") {
        const year = date.year$.toFixed(0);
        const month = date.month$.toFixed(0).padStart(2, "0");
        const day = date.day$.toFixed(0).padStart(2, "0");
        return year + sep + month + sep + day;
    }
    static hhmmss_(date, sep = "") {
        const hour = date.hour$.toFixed(0);
        const minute = date.minute$.toFixed(0).padStart(2, "0");
        const second = date.second$.toFixed(0).padStart(2, "0");
        return hour + sep + minute + sep + second;
    }
    add_(ms) {
        return new DateTime(this._date.valueOf() + ms);
    }
    subtract_(ms) {
        return new DateTime(this._date.valueOf() - ms);
    }
    isAfter_(date) {
        return this.millisecondsSinceEpoch$ > date.millisecondsSinceEpoch$;
    }
    /// @return Milliseconds since 01/01/1970 in UTC time.
    get millisecondsSinceEpoch$() {
        return this._date.getTime();
    }
    /// Day of the month starting from 1.
    get day$() {
        return this._date.getDate();
    }
    get weekday$() {
        return this._date.getDay();
    }
    /// Month of the year starting from 1.
    get month$() {
        return this._date.getMonth() + 1;
    }
    get year$() {
        return this._date.getFullYear();
    }
    get hour$() {
        return this._date.getHours();
    }
    get minute$() {
        return this._date.getMinutes();
    }
    get second$() {
        return this._date.getSeconds();
    }
}
exports.DateTime = DateTime;
DateTime.FOREVER = 1 << 30;
DateTime.MIN = 1000 * 60;
DateTime.HOUR = DateTime.MIN * 60;
DateTime.DAY = DateTime.HOUR * 24;
DateTime.SUNDAY = 0;
DateTime.MONDAY = 1;
DateTime.TUEDAY = 2;
DateTime.WEDNESDAY = 3;
DateTime.THURSDAY = 4;
DateTime.FRIDAY = 5;
DateTime.SATURDAY = 6;
//# sourceMappingURL=datetime.js.map