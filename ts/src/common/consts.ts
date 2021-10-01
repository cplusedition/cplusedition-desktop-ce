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
    //#BEGIN IPC
    static readonly HOST = "XrM";
    static readonly FROM_RENDERER = "X7W";
    static readonly FROM_RENDERER_INVOKE = "XVr";
    static readonly FROM_MAIN = "Xyt";
    static readonly CMD = "Xde";
    static readonly SERIAL = "XmQ";
    static readonly ARGS = "XwH";

    static readonly hello = "XOy";
    static readonly result = "XEJ";
    static readonly progress = "XOr";
    static readonly hi = "XL6";
    static readonly test = "X0t";
    static readonly quit = "XqN";
    static readonly showDeveloperTools = "XXO";
    //#END IPC
}

export declare class IpcRenderer {
    ipcOnReceive(channel: string, callback: (e: Electron.IpcRendererEvent, ...args: any[]) => void): void;
    ipcSend(channel: string, ...args: any[]): void;
    ipcInvoke(channel: string, ...args: any[]): Promise<any>;
    ipcRemoveAllListeners(channel: string): void;
}

export abstract class DebugKey {
    //#BEGIN DebugKey
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
    //#END DebugKey
    //#BEGIN An.Key
    static readonly path = "Xv7";
    static readonly result = "XXc";
    static readonly errors = "XZ7";
    static readonly stacktrace = "Xom";
    static readonly expectedfailure = "XgS";
    static readonly expectedresult = "Xvl";
    //#END An.Key
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
    static readonly TEST_TIMEOUT = 60; // sec.
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
        return this.json[DebugKey.result] ?? null;
    }
    get errors(): TestResult {
        return this.json[DebugKey.errors] ?? null;
    }
    get stacktrace(): string {
        return this.json[DebugKey.stacktrace] ?? "";
    }
    get isExpectedFailure(): boolean {
        return this.json[DebugKey.expectedfailure] === true;
    }
    get isExpectedResult(): boolean {
        return this.json[DebugKey.expectedresult] === true;
    }
}
