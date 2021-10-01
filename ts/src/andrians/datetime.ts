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
import { Int, Long } from "../bot/botcore";

export class DateTime {
    static readonly FOREVER = 1 << 30;
    static readonly MIN = 1000 * 60;
    static readonly HOUR = DateTime.MIN * 60;
    static readonly DAY = DateTime.HOUR * 24;
    static readonly SUNDAY = 0;
    static readonly MONDAY = 1;
    static readonly TUEDAY = 2;
    static readonly WEDNESDAY = 3;
    static readonly THURSDAY = 4;
    static readonly FRIDAY = 5;
    static readonly SATURDAY = 6;
    private _date: Date;
    /// @date Either ms in UTC or a Date object.
    /// @options.isUtc Specify if date parameter is utc or local time. Default is utc.
    constructor(date: Long | Date, isUtc?: boolean) {
        this._date = (date instanceof Date ? date : new Date(date));
        if (isUtc === false) {
            this._date = new Date(this._date.valueOf() + this._date.getTimezoneOffset() * DateTime.MIN);
        }
    }
    static local_(
        year: number,
        month: number, // 1..12
        day?: number, // 1..31
        hour?: number, // 0..23
        minute?: number, // 0..59
        second?: number, // 0..59
        ms?: number,  // 0..999
    ) {
        return new DateTime(Date.UTC(year, month - 1, day ?? 1, hour ?? 0, minute ?? 0, second ?? 0, ms ?? 0), false);
    }
    static utc_(
        year: number,
        month: number, // 1..12
        day?: number, // 1..31
        hour?: number, // 0..23
        minute?: number, // 0..59
        second?: number, // 0..59
        ms?: number,  // 0..999
    ) {
        return new DateTime(Date.UTC(year, month - 1, day ?? 1, hour ?? 0, minute ?? 0, second ?? 0, ms ?? 0), true);
    }
    /// @param ms UTC time in ms since 01/01/1970.
    /// @param isUtc default is true.
    static fromMillisecondsSinceEpoch_(ms: number, isUtc?: boolean): DateTime {
        return new DateTime(ms, isUtc);
    }
    static now_(): DateTime {
        return new DateTime(new Date());
    }
    static ms_(): Long {
        return Date.now();
    }
    /// @return Today string in form yyyymmdd
    static today_(): string {
        return DateTime.yyyymmdd_(this.now_());
    }
    static yyyymmdd_(date: DateTime, sep: string = ""): string {
        const year = date.year$.toFixed(0);
        const month = date.month$.toFixed(0).padStart(2, "0");
        const day = date.day$.toFixed(0).padStart(2, "0");
        return year + sep + month + sep + day;
    }
    static hhmmss_(date: DateTime, sep: string = ""): string {
        const hour = date.hour$.toFixed(0);
        const minute = date.minute$.toFixed(0).padStart(2, "0");
        const second = date.second$.toFixed(0).padStart(2, "0");
        return hour + sep + minute + sep + second;
    }
    add_(ms: number): DateTime {
        return new DateTime(this._date.valueOf() + ms);
    }
    subtract_(ms: number): DateTime {
        return new DateTime(this._date.valueOf() - ms);
    }
    isAfter_(date: DateTime): boolean {
        return this.millisecondsSinceEpoch$ > date.millisecondsSinceEpoch$;
    }
    /// @return Milliseconds since 01/01/1970 in UTC time.
    get millisecondsSinceEpoch$(): Long {
        return this._date.getTime();
    }
    /// Day of the month starting from 1.
    get day$(): Int {
        return this._date.getDate();
    }
    get weekday$(): Int {
        return this._date.getDay();
    }
    /// Month of the year starting from 1.
    get month$(): Int {
        return this._date.getMonth() + 1;
    }
    get year$(): Int {
        return this._date.getFullYear();
    }
    get hour$(): Int {
        return this._date.getHours();
    }
    get minute$(): Int {
        return this._date.getMinutes();
    }
    get second$(): Int {
        return this._date.getSeconds();
    }
}
