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
import { Request, StreamProtocolResponse } from "electron";
import { Fun10, Fun20, Int, JSONObject, JSONUt, json_ } from "./bot/botcore";
import { Encoding, Filepath } from "./bot/botnode";
import net = require("net");

//////////////////////////////////////////////////////////////////////

type ElectronResponse = (stream?: (NodeJS.ReadableStream) | (StreamProtocolResponse)) => void;

abstract class ServerKey {
    static readonly statusCode = "statusCode";
    static readonly headers = "headers";
    static readonly data = "data";
    static readonly method = "method";
    static readonly referrer = "referrer";
    static readonly url = "url";
}

class KK {
    static readonly SERVER = ".server";
}

class Reader {
    private static readonly IDLE = 0;
    private static readonly DATA = 1;
    private static readonly END = 2;
    private _buffers = new Array<Buffer>();
    private _state = Reader.IDLE;
    private _available = 0;
    private _length = 0;

    constructor(
        private readonly socket$: net.Socket,
        private readonly callback$: Fun10<Buffer | null>,
    ) {
        this.socket$.on("data", (data: Buffer) => {
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
        if (this._available == 0) return;
        try {
            const b = Buffer.concat(this._buffers);
            this.socket$.unshift(b);
            this._available = 0;
            this._buffers.length = 0;
            this._length = 0;
            this._state = Reader.IDLE;
        } catch (e) {
            
            this._end();
        }
    }
    private _ondata() {
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
    private _end() {
        this._state = Reader.END;
        this.callback$(null);
    }
    private _consume(size: Int): Buffer | null {
        if (size > this._available) return null;
        const ret = new Array<Buffer>();
        let remaining = size;
        while (remaining > 0) {
            const b = this._buffers.shift();
            if (b === undefined) throw new Error(); // Should not happen.
            if (b.length <= remaining) {
                ret.push(b);
                remaining -= b.length;
            } else {
                ret.push(b.slice(0, remaining));
                this._buffers.unshift(b.slice(remaining));
                remaining = 0;
            }
        }
        this._available -= size;
        return Buffer.concat(ret);
    }
}

export class Servlet {
    constructor(
        private sockdir$: Filepath,
    ) {
        this.sockdir$.mkdirsOrFail_();
    }

    private _writelen(c: net.Socket, len: Int) {
        const b = Buffer.alloc(4);
        b.writeInt32BE(len, 0);
        c.write(b, (err) => this._onerror("Write error", err));
    }

    private _onerror(_msg: string, err?: Error) {
        if (!err) return;
        
    }

    private _readheader(client: net.Socket, callback: Fun20<number, JSONObject>): void {
        const r = new Reader(client,
            (buf) => {
                r.unconsume_();
                if (buf != null) {
                    const json = JSONUt.jsonObjectOrNull_(buf.toString(Encoding.utf8$));
                    if (json != null) {
                        const status = json[ServerKey.statusCode] as Int;
                        const headers = json[ServerKey.headers] as JSONObject;
                        callback(status, headers);
                        return;
                    }
                }
                callback(500, json_());
                return;
            });
        r.resume_();
    }

    handle(request: Request, response: ElectronResponse): net.Socket {
        const req = json_(
            [ServerKey.url, request.url],
            [ServerKey.headers, request.headers],
            [ServerKey.referrer, request.referrer],
            [ServerKey.method, request.method],
            [ServerKey.data, (request.uploadData?.[0]?.bytes ?? Buffer.alloc(0)).toString(Encoding.base64$)],
        );
        const client = net.createConnection({
            path: this.sockdir$.file_(KK.SERVER).path$,
            allowHalfOpen: true,
        }).on("connect", () => {
            this._readheader(client, (status, headers) => {
                client.removeAllListeners("data");
                response(json_(
                    [ServerKey.statusCode, status],
                    [ServerKey.headers, headers],
                    [ServerKey.data, client],
                ) as StreamProtocolResponse);
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

