"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createWindow = void 0;
const stream_1 = require("stream");
const etut_1 = require("../bot-electron/etut");
const botcore_1 = require("../bot/botcore");
const consts_1 = require("../common/consts");
const servlet_1 = require("../servlet");
const Electron = require('electron');
class KK {
}
KK.PRELOAD_JS = "../preload.js";
KK.WIDTH = 720;
KK.HEIGHT = 960;
function createWindow(w, h, debuggable) {
    const win = new Electron.BrowserWindow({
        width: w,
        height: h,
        resizable: true,
        enableLargerThanScreen: true,
        autoHideMenuBar: true,
        webPreferences: etut_1.EtUt.setSecureWebPreferences_({}, true, debuggable),
        backgroundColor: "#fff",
    });
    Electron.Menu.setApplicationMenu(null);
    if (main.debugging_) {
        main.showDeveloperTools(win.webContents);
        main.addDevToolsDidFinishLoadListener(() => {
            win.loadURL("http://localhost:8081/index.html");
        });
    }
    else {
        win.loadURL("http://localhost:8081/index.html");
    }
}
exports.createWindow = createWindow;
function setupEventHandlers(main) {
    Electron.app.on('will-quit', (_event) => {
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
        contents.addListener("console-message", (_event, level, message) => {
            if (main.debugging_) {
                main.consoleLog$.push(`${level}: ${message}`);
            }
        });
        contents.on('did-finish-load', () => {
            main.didFinishLoad(contents);
        });
    });
}
class Main {
    constructor() {
        this.width = KK.WIDTH;
        this.height = KK.HEIGHT;
        this.devToolsDidFinishLoadListeners = new Array();
        this.pageDidFinishLoadListeners = new Array();
        this.debuggable$ = false;
        this.debugging$ = false;
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
    }
    addDevToolsDidFinishLoadListener(listener) {
        this.devToolsDidFinishLoadListeners.push(listener);
    }
    addPageDidFInishLoadListener(listener) {
        this.pageDidFinishLoadListeners.push(listener);
    }
    confirmWillQuit(callback) {
        callback(true);
    }
    boot() {
        try {
            this.debugging$ = Electron.app.commandLine.hasSwitch("d");
            this.debuggable$ = this.debugging$ || Electron.app.commandLine.hasSwitch("D");
            this.width = botcore_1.Ut.parseInt_(Electron.app.commandLine.getSwitchValue("w"), KK.WIDTH);
            this.height = botcore_1.Ut.parseInt_(Electron.app.commandLine.getSwitchValue("h"), KK.HEIGHT);
            const url = Electron.app.commandLine.getSwitchValue("u");
            if (url.length == 0)
                throw "-u url parameter is required";
            setupEventHandlers(this);
            Electron.session.defaultSession.protocol.interceptStreamProtocol("http", (request, response) => {
                if (new URL(request.url).pathname.endsWith(".html")) {
                    servlet_1.Servlet.redirectPdf(response, url);
                    return;
                }
                const buffers = new Array();
                process.stdin
                    .on("data", (b) => { buffers.push(Buffer.from(b)); })
                    .on("end", () => {
                    const data = Buffer.concat(buffers);
                    servlet_1.Servlet.pdfResponse(response, stream_1.Readable.from(data));
                });
            });
            createWindow(this.width, this.height, this.debuggable$);
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