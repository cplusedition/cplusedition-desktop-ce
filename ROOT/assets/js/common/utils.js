"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.MimeUt = exports.ExpectedResult = exports.ExpectedFailure = void 0;
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
const botcore_1 = require("../bot/botcore");
Error.stackTraceLimit = 50;
class ExpectedFailure extends Error {
}
exports.ExpectedFailure = ExpectedFailure;
class ExpectedResult extends Error {
}
exports.ExpectedResult = ExpectedResult;
class MimeUt {
    static mimeOf(suffix) {
        var _a;
        return suffix == null ? null : (_a = MimeUt.bySuffix[suffix]) !== null && _a !== void 0 ? _a : null;
    }
    static suffixOf(mime) {
        var _a;
        return mime == null ? null : (_a = MimeUt.byMime[mime]) !== null && _a !== void 0 ? _a : null;
    }
}
MimeUt.bySuffix = (0, botcore_1.smap_)([".jpg", "image/jpeg"], [".png", "image/png"], [".gif", "image/gif"], [".ico", "image/ico"], [".svg", "image/svg+xml"], [".html", "text/html;charset=utf-8"], [".css", "text/css;charset=utf-8"], [".js", "text/javascript;charset=utf-8"], [".json", "application/json;charset=utf-8"], [".pdf", "application/pdf"]);
MimeUt.byMime = (0, botcore_1.smap_)(...Object.entries(MimeUt.bySuffix)
    .map((entry) => {
    return [entry[1], entry[0]];
}));
exports.MimeUt = MimeUt;
//# sourceMappingURL=utils.js.map