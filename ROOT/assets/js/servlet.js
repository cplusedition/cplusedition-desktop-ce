"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Servlet = void 0;
const stream_1 = require("stream");
const botcore_1 = require("./bot/botcore");
const botnode_1 = require("./bot/botnode");
const shared_1 = require("./shared");
const consts_1 = require("./common/consts");
const net = require("net");
const child_process = require("child_process");
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
                throw new Error();
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
                    const status = json[consts_1.ServerKey.statusCode];
                    const headers = json[consts_1.ServerKey.headers];
                    callback(status, headers);
                    return;
                }
            }
            callback(500, (0, botcore_1.json_)());
            return;
        });
        r.resume_();
    }
    handle(request, response, fromipc = false) {
        var _a, _b, _c;
        const req = (0, botcore_1.json_)([consts_1.ServerKey.url, request.url], [consts_1.ServerKey.headers, request.headers], [consts_1.ServerKey.referrer, request.referrer], [consts_1.ServerKey.method, request.method], [consts_1.ServerKey.data, ((_c = (_b = (_a = request.uploadData) === null || _a === void 0 ? void 0 : _a[0]) === null || _b === void 0 ? void 0 : _b.bytes) !== null && _c !== void 0 ? _c : Buffer.alloc(0)).toString(botnode_1.Encoding.base64$)], [consts_1.ServerKey.ipc, fromipc]);
        const client = net.createConnection({
            path: this.sockdir$.file_(KK.SERVER).path$,
            allowHalfOpen: true,
            family: 4,
        }).on("connect", () => {
            this._readheader(client, (status, headers) => {
                client.removeAllListeners("data");
                if (status >= 200 && status < 300 && headers[shared_1.HttpHeader.ContentType] == "application/pdf") {
                    response((0, botcore_1.json_)(["statusCode", status], ["headers", headers], ["data", client]));
                    return;
                }
                response((0, botcore_1.json_)(["statusCode", status], ["headers", headers], ["data", client]));
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
    pdfViewer(url, width, height, data, debuggable) {
        try {
            const index = botnode_1.Filepath.pwd_().file_("pdfviewer").path$;
            const electron = process.execPath;
            const args = ["--host-rules='MAP * 127.0.0.1'"];
            if (debuggable)
                args.push("-d");
            args.push(`-u=${url}`, index);
            if (width != null)
                args.push(`--w=${width}`);
            if (height != null)
                args.push(`--h=${height}`);
            data.pipe(child_process.spawn(electron, args, {}).stdin);
        }
        catch (e) {
        }
    }
    static makeRequest(url, data = []) {
        return {
            "url": url,
            "headers": (0, botcore_1.json_)(),
            "referrer": "http://localhost:8080",
            "method": "GET",
            "uploadData": data,
        };
    }
    static redirectPdf(response, url) {
        response((0, botcore_1.json_)(["statusCode", 302], ["headers", (0, botcore_1.json_)([shared_1.HttpHeader.Location, url])], ["data", stream_1.Readable.from("")]));
    }
    static pdfResponse(response, res) {
        try {
            response((0, botcore_1.json_)(["statusCode", 200], ["headers", (0, botcore_1.json_)([shared_1.HttpHeader.ContentType, "application/pdf"])], ["data", res]));
        }
        catch (e) {
        }
    }
}
exports.Servlet = Servlet;
//# sourceMappingURL=servlet.js.map