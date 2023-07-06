"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Result = exports.Testcase = exports.CommonK = exports.Key = exports.DebugKey = exports.ServerKey = exports.IPC = exports.SCHEME_HOSTPORT = void 0;
exports.SCHEME_HOSTPORT = "http://localhost:8080";
class IPC {
}
IPC.HOST = "XxXZl";
IPC.FROM_RENDERER = "XxXgb";
IPC.FROM_RENDERER_INVOKE = "XxXGt";
IPC.FROM_MAIN = "XxX2L";
IPC.CMD = "XxXle";
IPC.SERIAL = "XxX5k";
IPC.ARGS = "XxXSr";
IPC.ON_RECEIVE = "XxXior";
IPC.SEND = "XxXisd";
IPC.INVOKE = "XxXiiv";
IPC.REMOVE_ALL = "XxXira";
IPC.hello = "XxXBM";
IPC.result = "XxX2j";
IPC.progress = "XxXI2";
IPC.hi = "XxXY5";
IPC.test = "XxXr3";
IPC.quit = "XxXp6";
IPC.showDeveloperTools = "XxXUh";
IPC.showPdf = "XxXN2";
IPC.jof = "XxXMy";
IPC.screenshot = "XxXhm";
exports.IPC = IPC;
class ServerKey {
}
ServerKey.statusCode = "XxXgb";
ServerKey.headers = "XxXwd";
ServerKey.data = "XxXm0";
ServerKey.method = "XxXef";
ServerKey.referrer = "XxXYI";
ServerKey.url = "XxXGV";
ServerKey.ipc = "XxXr7";
ServerKey.serial = "XxXds";
exports.ServerKey = ServerKey;
class DebugKey {
}
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
exports.DebugKey = DebugKey;
class Key {
}
Key.path = "XxXQm";
Key.result = "XxXJL";
Key.errors = "XxXox";
Key.stacktrace = "XxXBq";
Key.expectedfailure = "XxXNT";
Key.expectedresult = "XxXIS";
exports.Key = Key;
class CommonK {
}
CommonK.TEST_TIMEOUT = 180;
CommonK.PAUSE = 2000;
CommonK.TO02 = 2000;
CommonK.TO05 = 5000;
CommonK.TO15 = 15000;
CommonK.TO30 = 30000;
CommonK.STEP = 200;
CommonK.MSG_TIMEOUT = "timeout";
exports.CommonK = CommonK;
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
        return (_a = this.json[Key.result]) !== null && _a !== void 0 ? _a : null;
    }
    get errors() {
        var _a;
        return (_a = this.json[Key.errors]) !== null && _a !== void 0 ? _a : null;
    }
    get stacktrace() {
        var _a;
        return (_a = this.json[Key.stacktrace]) !== null && _a !== void 0 ? _a : "";
    }
    get isExpectedFailure() {
        return this.json[Key.expectedfailure] === true;
    }
    get isExpectedResult() {
        return this.json[Key.expectedresult] === true;
    }
}
exports.Result = Result;
//# sourceMappingURL=consts.js.map