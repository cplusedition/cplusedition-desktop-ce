"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.createWindow = void 0;
const etut_1 = require("./bot-electron/etut");
const botcore_1 = require("./bot/botcore");
const botnode_1 = require("./bot/botnode");
const consts_1 = require("./common/consts");
const servlet_1 = require("./servlet");
const Electron = require('electron');
class KK {
}
KK.PRELOAD_JS = "preload.js";
KK.WIDTH = 720;
KK.HEIGHT = 960;
function createWindow(w, h, plugins, debuggable) {
    const win = new Electron.BrowserWindow({
        width: w,
        height: h,
        resizable: true,
        enableLargerThanScreen: true,
        autoHideMenuBar: true,
        webPreferences: etut_1.EtUt.setSecureWebPreferences_({
            "preload": Electron.app.getAppPath() + `/${KK.PRELOAD_JS}`,
        }, plugins, debuggable),
        backgroundColor: "#fff",
    });
    Electron.Menu.setApplicationMenu(null);
    if (main.debugging_) {
        main.showDeveloperTools(win.webContents);
        main.addDevToolsDidFinishLoadListener(() => {
            win.loadURL("http://localhost:8080/");
        });
    }
    else {
        win.loadURL("http://localhost:8080/");
    }
    return win;
}
exports.createWindow = createWindow;
function setupEventHandlers(main) {
    Electron.app.on('will-quit', (_event) => {
        main.confirmWillQuit((_ok) => {
        });
    });
    Electron.app.on('before-quit', (_event) => {
        main.confirmWillQuit((_ok) => {
        });
    });
    Electron.app.on('activate', () => {
    });
    Electron.app.on("session-created", (session) => {
        session.on("will-download", (_, _item) => {
        });
    });
    Electron.app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            Electron.app.quit();
        }
    });
    Electron.app.on('web-contents-created', (_event, contents) => {
        if (contents.session) {
            etut_1.EtUt.configSession_(contents.session);
        }
        contents.on('will-attach-webview', (event, webPreferences, _params) => {
            event.preventDefault();
            delete webPreferences.preload;
            etut_1.EtUt.setSecureWebPreferences_(webPreferences);
        });
        contents.on('will-navigate', (event, url) => {
            try {
                const u = new URL(url);
                const docurl = new URL(contents.getURL());
                if (etut_1.EtUt.offsite_(u, docurl)) {
                    event.preventDefault();
                }
            }
            catch (e) {
                event.preventDefault();
            }
        });
        contents.on('new-window', (event, _url, _frameName, _disposition, _options, _additionalFeatures, _referrer) => {
            event.preventDefault();
        });
        contents.addListener("console-message", (_event, _level, _message) => {
        });
        contents.on('did-finish-load', () => {
            main.didFinishLoad(contents);
        });
    });
}
function setupSchemeHandlers(protocol, servlet) {
    protocol.interceptStreamProtocol("http", (request, callback) => {
        servlet.handle(request, callback);
    });
}
class Main {
    constructor() {
        this.width = KK.WIDTH;
        this.height = KK.HEIGHT;
        this.contents$ = null;
        this.window$ = null;
        this.quitCallback$ = null;
        this.servlet$ = new servlet_1.Servlet(botnode_1.Filepath.pwd_().file_("../../run"));
        this.devToolsDidFinishLoadListeners = new Array();
        this.pageDidFinishLoadListeners = new Array();
        this.debuggable$ = false;
        this.debugging$ = false;
        this.plugins$ = false;
        this.consoleLog$ = new Array();
    }
    get debugging_() { return this.debugging$; }
    didFinishLoad(contents) {
        const url = contents.getURL();
        if (url.startsWith("devtools://")) {
            if (this.debugging$) {
                while (this.devToolsDidFinishLoadListeners.length > 0) {
                    this.devToolsDidFinishLoadListeners.pop()();
                }
            }
            return;
        }
        if (url != consts_1.SCHEME_HOSTPORT + "/")
            return;
        this.contents$ = contents;
        contents.enableDeviceEmulation({
            screenPosition: "mobile",
            screenSize: { width: this.width, height: this.height },
            deviceScaleFactor: 0,
            scale: 1.0,
            viewPosition: { x: 0, y: 0 },
            viewSize: { width: 0, height: 0 },
        });
        while (this.pageDidFinishLoadListeners.length > 0) {
            this.pageDidFinishLoadListeners.pop()();
        }
        Electron.ipcMain.on(consts_1.IPC.FROM_RENDERER, (event, ...args) => this.handleIpcEvent(event, ...args));
    }
    addDevToolsDidFinishLoadListener(listener) {
        this.devToolsDidFinishLoadListeners.push(listener);
    }
    addPageDidFInishLoadListener(listener) {
        this.pageDidFinishLoadListeners.push(listener);
    }
    confirmWillQuit(callback) {
        var _a;
        (_a = this.contents$) === null || _a === void 0 ? void 0 : _a.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.quit);
        this.quitCallback$ = callback;
    }
    boot() {
        try {
            this.debugging$ = Electron.app.commandLine.hasSwitch("d");
            this.debuggable$ = this.debugging$ || Electron.app.commandLine.hasSwitch("D");
            this.plugins$ = Electron.app.commandLine.hasSwitch("P");
            this.width = botcore_1.Ut.parseInt_(Electron.app.commandLine.getSwitchValue("w"), KK.WIDTH);
            this.height = botcore_1.Ut.parseInt_(Electron.app.commandLine.getSwitchValue("h"), KK.HEIGHT);
            setupEventHandlers(this);
            setupSchemeHandlers(Electron.session.defaultSession.protocol, this.servlet$);
            this.window$ = createWindow(this.width, this.height, this.plugins$, this.debuggable$);
        }
        catch (e) {
            Electron.app.quit();
        }
    }
    showDeveloperTools(contents) {
        if (this.debuggable$ && contents != null) {
            contents.openDevTools({
                mode: "detach",
            });
        }
    }
    handleIpcEvent(_event, ...args) {
        var _a, _b, _c, _d;
        return __awaiter(this, void 0, void 0, function* () {
            if (args.length == 0) {
                return;
            }
            switch (args[0]) {
                case consts_1.IPC.quit: {
                    if (this.quitCallback$ != null) {
                        this.quitCallback$(true);
                        this.contents$ = null;
                    }
                    else {
                        (_a = this.window$) === null || _a === void 0 ? void 0 : _a.close();
                    }
                    break;
                }
                case consts_1.IPC.showDeveloperTools: {
                    main.showDeveloperTools(main.contents$);
                    break;
                }
                case consts_1.IPC.jof: {
                    if (args.length > 3) {
                        const url = args[1];
                        const bytes = Buffer.from(((_b = args[2]) !== null && _b !== void 0 ? _b : ""), "utf8");
                        const serial = args[3];
                        this.servlet$.handle(servlet_1.Servlet.makeRequest(url, [{ bytes: bytes }]), (data) => {
                            var _a, _b, _c;
                            try {
                                if (data != null) {
                                    const res = data;
                                    if (res.statusCode != 200) {
                                        (_a = this.contents$) === null || _a === void 0 ? void 0 : _a.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.jof, serial, (0, botcore_1.jsonOf_)(consts_1.Key.errors, `${res.statusCode}`));
                                        return;
                                    }
                                    if (res.data == null) {
                                        (_b = this.contents$) === null || _b === void 0 ? void 0 : _b.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.jof, serial, (0, botcore_1.json_)());
                                        return;
                                    }
                                    let result = "";
                                    const s = res.data;
                                    s.setEncoding("utf8");
                                    s.on("readable", () => {
                                        var _a;
                                        result += (_a = s.read()) !== null && _a !== void 0 ? _a : "";
                                    });
                                    s.on("end", () => {
                                        var _a;
                                        let json;
                                        try {
                                            json = JSON.parse(result);
                                        }
                                        catch (e) {
                                            json = (0, botcore_1.jsonOf_)(consts_1.Key.errors, `${e}: ${result}`);
                                        }
                                        (_a = this.contents$) === null || _a === void 0 ? void 0 : _a.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.jof, serial, json);
                                    });
                                    return;
                                }
                            }
                            catch (e) {
                            }
                            ;
                            (_c = this.contents$) === null || _c === void 0 ? void 0 : _c.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.jof, serial, (0, botcore_1.jsonOf_)(consts_1.Key.errors, "500"));
                        }, true);
                    }
                    break;
                }
                case consts_1.IPC.showPdf: {
                    if (args.length > 3) {
                        const url = args[1];
                        const width = args[2];
                        const height = args[3];
                        this.servlet$.handle(servlet_1.Servlet.makeRequest(url), (data) => {
                            try {
                                const s = data === null || data === void 0 ? void 0 : data.data;
                                if (s != null) {
                                    this.servlet$.pdfViewer(url, width, height, s, main.debugging_);
                                }
                            }
                            catch (e) {
                            }
                            ;
                        });
                    }
                    break;
                }
                case consts_1.IPC.screenshot: {
                    if (args.length < 5) {
                        (_c = this.contents$) === null || _c === void 0 ? void 0 : _c.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.screenshot);
                        break;
                    }
                    const cpath = args[1];
                    const top = botcore_1.Ut.parseInt_(args[2], 0);
                    const hasrsb = args[4];
                    (_d = this.contents$) === null || _d === void 0 ? void 0 : _d.capturePage().then((image) => {
                        var _a;
                        const size = image.getSize();
                        const dataurl = image.crop({
                            x: 0,
                            y: top,
                            width: size.width,
                            height: size.height - top,
                        }).toDataURL();
                        (_a = this.contents$) === null || _a === void 0 ? void 0 : _a.send(consts_1.IPC.FROM_MAIN, consts_1.IPC.screenshot, cpath, dataurl, size.width, size.height - top, hasrsb);
                    });
                    break;
                }
            }
        });
    }
    quit() {
        Electron.app.quit();
    }
}
const main = new Main();
Electron.app.enableSandbox();
Electron.app.whenReady().then(() => {
    main.boot();
});
//# sourceMappingURL=index.js.map