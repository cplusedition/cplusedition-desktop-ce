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
exports.Servlet = void 0;
const botcore_1 = require("./bot/botcore");
const botnode_1 = require("./bot/botnode");
const net = require("net");
class ServerKey {
}
ServerKey.statusCode = "statusCode";
ServerKey.headers = "headers";
ServerKey.data = "data";
ServerKey.method = "method";
ServerKey.referrer = "referrer";
ServerKey.url = "url";
class KK {
}
KK.SERVER = ".server";
class Reader {
    constructor(socket$, callback$) {
        this.socket$ = socket$;
        this.callback$ = callback$;
        this._buffers = new Array();
        this._state = Reader.IDLE;
        this._available = 0;
        this._length = 0;
        this.socket$.on("data", (data) => {
            this._buffers.push(data);
            this._available += data.length;
            this.socket$.pause();
            this._ondata();
        });
    }
    resume_() {
        this.socket$.resume();
    }
    unconsume_() {
        if (this._available == 0)
            return;
        try {
            const b = Buffer.concat(this._buffers);
            this.socket$.unshift(b);
            this._available = 0;
            this._buffers.length = 0;
            this._length = 0;
            this._state = Reader.IDLE;
        }
        catch (e) {
            this._end();
        }
    }
    _ondata() {
        switch (this._state) {
            case Reader.IDLE: {
                const buf = this._consume(4);
                if (buf == null) {
                    this.resume_();
                    return;
                }
                this._length = buf.readInt32BE(0);
                if (this._length < 0) {
                    this._end();
                    return;
                }
                this._state = Reader.DATA;
                this._ondata();
                break;
            }
            case Reader.DATA: {
                const buf = this._consume(this._length);
                if (buf == null) {
                    this.resume_();
                    return;
                }
                this._length = 0;
                this._state = Reader.IDLE;
                this.socket$.pause();
                this.callback$(buf);
                break;
            }
        }
    }
    _end() {
        this._state = Reader.END;
        this.callback$(null);
    }
    _consume(size) {
        if (size > this._available)
            return null;
        const ret = new Array();
        let remaining = size;
        while (remaining > 0) {
            const b = this._buffers.shift();
            if (b === undefined)
                throw new Error(); // Should not happen.
            if (b.length <= remaining) {
                ret.push(b);
                remaining -= b.length;
            }
            else {
                ret.push(b.slice(0, remaining));
                this._buffers.unshift(b.slice(remaining));
                remaining = 0;
            }
        }
        this._available -= size;
        return Buffer.concat(ret);
    }
}
Reader.IDLE = 0;
Reader.DATA = 1;
Reader.END = 2;
class Servlet {
    constructor(sockdir$) {
        this.sockdir$ = sockdir$;
        this.sockdir$.mkdirsOrFail_();
    }
    _writelen(c, len) {
        const b = Buffer.alloc(4);
        b.writeInt32BE(len, 0);
        c.write(b, (err) => this._onerror("Write error", err));
    }
    _onerror(_msg, err) {
        if (!err)
            return;
    }
    _readheader(client, callback) {
        const r = new Reader(client, (buf) => {
            r.unconsume_();
            if (buf != null) {
                const json = botcore_1.JSONUt.jsonObjectOrNull_(buf.toString(botnode_1.Encoding.utf8$));
                if (json != null) {
                    const status = json[ServerKey.statusCode];
                    const headers = json[ServerKey.headers];
                    callback(status, headers);
                    return;
                }
            }
            callback(500, botcore_1.json_());
            return;
        });
        r.resume_();
    }
    handle(request, response) {
        var _a, _b, _c;
        const req = botcore_1.json_([ServerKey.url, request.url], [ServerKey.headers, request.headers], [ServerKey.referrer, request.referrer], [ServerKey.method, request.method], [ServerKey.data, ((_c = (_b = (_a = request.uploadData) === null || _a === void 0 ? void 0 : _a[0]) === null || _b === void 0 ? void 0 : _b.bytes) !== null && _c !== void 0 ? _c : Buffer.alloc(0)).toString(botnode_1.Encoding.base64$)]);
        const client = net.createConnection({
            path: this.sockdir$.file_(KK.SERVER).path$,
            allowHalfOpen: true,
        }).on("connect", () => {
            this._readheader(client, (status, headers) => {
                client.removeAllListeners("data");
                response(botcore_1.json_([ServerKey.statusCode, status], [ServerKey.headers, headers], [ServerKey.data, client]));
            });
            setTimeout(() => {
                const b = Buffer.from(JSON.stringify(req), "utf8");
                this._writelen(client, b.length);
                client.write(b);
                client.end();
            }, 10);
        }).on("error", (_err) => {
        }).on("close", (_haserr) => {
        });
        return client;
    }
}
exports.Servlet = Servlet;
//# sourceMappingURL=servlet.js.map