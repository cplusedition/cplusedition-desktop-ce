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
exports.Result = exports.Testcase = exports.CommonK = exports.DebugKey = exports.IPC = exports.SCHEME_HOSTPORT = void 0;
exports.SCHEME_HOSTPORT = "http://localhost:8080";
class IPC {
}
exports.IPC = IPC;
//#BEGIN IPC
IPC.HOST = "XrM";
IPC.FROM_RENDERER = "X7W";
IPC.FROM_RENDERER_INVOKE = "XVr";
IPC.FROM_MAIN = "Xyt";
IPC.CMD = "Xde";
IPC.SERIAL = "XmQ";
IPC.ARGS = "XwH";
IPC.hello = "XOy";
IPC.result = "XEJ";
IPC.progress = "XOr";
IPC.hi = "XL6";
IPC.test = "X0t";
IPC.quit = "XqN";
IPC.showDeveloperTools = "XXO";
class DebugKey {
}
exports.DebugKey = DebugKey;
//#BEGIN DebugKey
DebugKey.id = "id";
DebugKey.testpath = "testpath";
DebugKey.testclass = "testclass";
DebugKey.testname = "testname";
DebugKey.testparams = "testparams";
DebugKey.debugging = "debugging";
DebugKey.timelapse = "timelapse";
DebugKey.screenshot = "screenshot";
DebugKey.timeout = "timeout";
DebugKey.cmd = "cmd";
DebugKey.busyWaitStep = "busywaitstep";
DebugKey.busyWaitTimeout = "busywaittimeout";
DebugKey.type = "type";
DebugKey.outdir = "outdir";
DebugKey.zipfile = "zipfile";
DebugKey.data = "data";
//#END DebugKey
//#BEGIN An.Key
DebugKey.path = "Xv7";
DebugKey.result = "XXc";
DebugKey.errors = "XZ7";
DebugKey.stacktrace = "Xom";
DebugKey.expectedfailure = "XgS";
DebugKey.expectedresult = "Xvl";
class CommonK {
}
exports.CommonK = CommonK;
CommonK.TEST_TIMEOUT = 60; // sec.
CommonK.PAUSE = 2000;
CommonK.TO02 = 2000;
CommonK.TO05 = 5000;
CommonK.TO15 = 15000;
CommonK.TO30 = 30000;
CommonK.STEP = 200;
CommonK.MSG_TIMEOUT = "timeout";
class Testcase {
    constructor(json) {
        this.json = json;
        this.testpath = this.json[DebugKey.testpath];
        this.testclass = this.json[DebugKey.testclass];
        this.testmethod = this.json[DebugKey.testname];
    }
    get qname() {
        return this.testclass + "/" + this.testmethod;
    }
    get testparams() {
        return this.json[DebugKey.testparams];
    }
    testparam(key) {
        var _a;
        const params = (_a = this.testparams) !== null && _a !== void 0 ? _a : null;
        return params == null ? null : params[key];
    }
    get debugging() {
        var _a;
        return (_a = this.json[DebugKey.debugging]) !== null && _a !== void 0 ? _a : false;
    }
    get timelapse() {
        var _a;
        return (_a = this.json[DebugKey.timelapse]) !== null && _a !== void 0 ? _a : false;
    }
    get timeout() {
        var _a;
        return (this.debugging ? 24 * 60 * 60 : ((_a = this.json[DebugKey.timeout]) !== null && _a !== void 0 ? _a : CommonK.TEST_TIMEOUT)) * 1000;
    }
}
exports.Testcase = Testcase;
class Result {
    constructor(json) {
        this.json = json;
    }
    get result() {
        var _a;
        return (_a = this.json[DebugKey.result]) !== null && _a !== void 0 ? _a : null;
    }
    get errors() {
        var _a;
        return (_a = this.json[DebugKey.errors]) !== null && _a !== void 0 ? _a : null;
    }
    get stacktrace() {
        var _a;
        return (_a = this.json[DebugKey.stacktrace]) !== null && _a !== void 0 ? _a : "";
    }
    get isExpectedFailure() {
        return this.json[DebugKey.expectedfailure] === true;
    }
    get isExpectedResult() {
        return this.json[DebugKey.expectedresult] === true;
    }
}
exports.Result = Result;
//# sourceMappingURL=consts.js.map