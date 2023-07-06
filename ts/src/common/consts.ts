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
import { Fun00, Int, StringMap, StringMapX } from "../bot/botcore";

export const SCHEME_HOSTPORT = "http://localhost:8080";

export type TestResult = string | string[] | StringMap<any> | null;

export abstract class IPC {
    static readonly HOST = "XxXZl";
    static readonly FROM_RENDERER = "XxXgb";
    static readonly FROM_RENDERER_INVOKE = "XxXGt";
    static readonly FROM_MAIN = "XxX2L";
    static readonly CMD = "XxXle";
    static readonly SERIAL = "XxX5k";
    static readonly ARGS = "XxXSr";

    static readonly ON_RECEIVE = "XxXior";
    static readonly SEND = "XxXisd";
    static readonly INVOKE = "XxXiiv";
    static readonly REMOVE_ALL = "XxXira";

    static readonly hello = "XxXBM";
    static readonly result = "XxX2j";
    static readonly progress = "XxXI2";
    static readonly hi = "XxXY5";
    static readonly test = "XxXr3";
    static readonly quit = "XxXp6";
    static readonly showDeveloperTools = "XxXUh";
    static readonly showPdf = "XxXN2";
    static readonly jof = "XxXMy";
    static readonly screenshot = "XxXhm";
}

export declare class IpcRenderer {
XxXior
        (channel: string, callback: (e: Electron.IpcRendererEvent, ...args: any[]) => void): void;
XxXisd
        (channel: string, ...args: any[]): void;
XxXiiv
        (channel: string, ...args: any[]): Promise<any>;
XxXira
        (channel: string): void;
}

export abstract class ServerKey {
    static readonly statusCode = "XxXgb";
    static readonly headers = "XxXwd";
    static readonly data = "XxXm0";
    static readonly method = "XxXef";
    static readonly referrer = "XxXYI";
    static readonly url = "XxXGV";
    static readonly ipc = "XxXr7";
    static readonly serial = "XxXds";
}

export abstract class DebugKey {
    static readonly id = "id";
    static readonly testpath = "testpath";
    static readonly testclass = "testclass";
    static readonly testname = "testname";
    static readonly testparams = "testparams";
    static readonly debugging = "debugging";
    static readonly timelapse = "timelapse";
    static readonly screenshot = "screenshot";
    static readonly timeout = "timeout";
    static readonly cmd = "cmd";
    static readonly busyWaitStep = "busywaitstep";
    static readonly busyWaitTimeout = "busywaittimeout";
    static readonly type = "type";
    static readonly outdir = "outdir";
    static readonly zipfile = "zipfile";
    static readonly data = "data";
}

export abstract class Key {
    static readonly path = "XxXQm";
    static readonly result = "XxXJL";
    static readonly errors = "XxXox";
    static readonly stacktrace = "XxXBq";
    static readonly expectedfailure = "XxXNT";
    static readonly expectedresult = "XxXIS";
}

export interface ITest {
    teardown(callback: () => void): void;
}

export interface ITester {
    error(result: TestResult, test: ITest): void;
    progress(msg: TestResult): void;
}

export interface ITestcase {

    readonly testpath: string;
    readonly testclass: string;
    readonly testmethod: string;
    readonly debugging: boolean;
    readonly timelapse: boolean;
    readonly qname: string;
    readonly testparams: StringMapX<any>;
    testparam<T>(key: string): T | null;
}

export interface IResult {
    readonly result: TestResult;
    readonly errors: TestResult;
    readonly stacktrace: string;
    readonly isExpectedFailure: boolean;
    readonly isExpectedResult: boolean;
}

export type Test = (done: Fun00) => void;

export class CommonK {
    static readonly TEST_TIMEOUT = 180;
    static readonly PAUSE = 2000;
    static readonly TO02 = 2000;
    static readonly TO05 = 5000;
    static readonly TO15 = 15000;
    static readonly TO30 = 30000;
    static readonly STEP = 200;
    static readonly MSG_TIMEOUT = "timeout";
}

export class Testcase implements ITestcase {
    readonly testpath: string;
    readonly testclass: string;
    readonly testmethod: string;

    constructor(private json: StringMap<any>) {
        this.testpath = this.json[DebugKey.testpath]!;
        this.testclass = this.json[DebugKey.testclass]!;
        this.testmethod = this.json[DebugKey.testname]!;
    }

    get qname(): string {
        return this.testclass + "/" + this.testmethod;
    }
    get testparams(): StringMapX<any> {
        return this.json[DebugKey.testparams];
    }
    testparam<T>(key: string): T | null {
        const params = this.testparams ?? null;
        return params == null ? null : params[key] as T;
    }
    get debugging(): boolean {
        return this.json[DebugKey.debugging] ?? false;
    }
    get timelapse(): boolean {
        return this.json[DebugKey.timelapse] ?? false;
    }
    get timeout(): Int {
        return (this.debugging ? 24 * 60 * 60 : (this.json[DebugKey.timeout] ?? CommonK.TEST_TIMEOUT)) * 1000;
    }
}

export class Result implements IResult {
    constructor(private json: StringMap<any>) {
    }
    get result(): TestResult {
        return this.json[Key.result] ?? null;
    }
    get errors(): TestResult {
        return this.json[Key.errors] ?? null;
    }
    get stacktrace(): string {
        return this.json[Key.stacktrace] ?? "";
    }
    get isExpectedFailure(): boolean {
        return this.json[Key.expectedfailure] === true;
    }
    get isExpectedResult(): boolean {
        return this.json[Key.expectedresult] === true;
    }
}
