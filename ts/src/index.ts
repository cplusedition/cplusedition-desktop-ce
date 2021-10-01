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
import { WebContents } from "electron";
import { EtUt } from "./bot-electron/etut";
import { Fun00, Fun10, Int, Ut } from "./bot/botcore";
import { Filepath } from "./bot/botnode";
import { IPC, SCHEME_HOSTPORT } from "./common/consts";
import { Servlet } from "./servlet";

const Electron = require('electron');

class KK {
    static readonly PRELOAD_JS = "preload.js";
}

export function createWindow(w: Int, h: Int, plugins: boolean, debuggable: boolean) {
    const win = new Electron.BrowserWindow({
        width: w,
        height: h,
        resizable: true,
        enableLargerThanScreen: true,
        autoHideMenuBar: true,
        webPreferences: EtUt.setSecureWebPreferences_({
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
    } else {
        
        win.loadURL("http://localhost:8080/");
    }
}

function setupEventHandlers(main: Main) {
    Electron.app.on('will-quit', (_event) => {
        
        main.confirmWillQuit((_ok) => {
        });
    });
    Electron.app.on('activate', () => {
        
    });
    Electron.app.on('window-all-closed', () => {
        
        if (process.platform !== 'darwin') {
            Electron.app.quit();
        }
    });
    Electron.app.on('web-contents-created', (_event, contents) => {
        
        if (contents.session) {
            EtUt.configSession_(contents.session);
        }
        contents.on('will-attach-webview', (event, webPreferences, _params) => {
            
            event.preventDefault();
            delete webPreferences.preload;
            EtUt.setSecureWebPreferences_(webPreferences);
        });
        contents.on('will-navigate', (event, url) => {
            
            try {
                const u = new URL(url);
                const docurl = new URL(contents.getURL());
                if (EtUt.offsite_(u, docurl)) {
                    
                    event.preventDefault();
                }
            } catch (e) {
                
                event.preventDefault();
            }
        });
        contents.on('new-window', (event, _url, _frameName, _disposition, _options, _additionalFeatures, _referrer) => {
            
            event.preventDefault();
        });
        contents.addListener("console-message", (_event, _level, message) => {
            main.consoleLog$.push(message);
        });
        contents.on('did-finish-load', () => {
            
            main.didFinishLoad(contents);
        });
    });
}

function setupSchemeHandlers(protocol: Electron.Protocol, servlet: Servlet) {
    protocol.interceptStreamProtocol("http", (request, callback) => {
        servlet.handle(request, callback);
    });
}

class Main {
    private width: Int = 600;
    private height: Int = 960;
    private contents$: Electron.WebContents | null = null;
    private servlet$ = new Servlet(Filepath.pwd_().file_("../../run")
    );
    private devToolsDidFinishLoadListeners = new Array<Fun00>();
    private pageDidFinishLoadListeners = new Array<Fun00>();
    private debuggable$ = false;
    private debugging$ = false;
    private plugins$ = false;
    consoleLog$ = new Array<string>();
    constructor() {
    }
    get debugging_(): boolean { return this.debugging$; }
    didFinishLoad(contents: Electron.WebContents) {
        const url = contents.getURL();
        if (url.startsWith("devtools://")) {
            if (this.debugging$) {
                while (this.devToolsDidFinishLoadListeners.length > 0) {
                    this.devToolsDidFinishLoadListeners.pop()!();
                }
            }
            return;
        }
        if (url != SCHEME_HOSTPORT + "/") return;
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
            this.pageDidFinishLoadListeners.pop()!();
        }
        Electron.ipcMain.on(IPC.FROM_RENDERER, (event, ...args) => this.handleIpcEvent(event, ...args));
    }
    addDevToolsDidFinishLoadListener(listener: Fun00) {
        this.devToolsDidFinishLoadListeners.push(listener);
    }
    addPageDidFInishLoadListener(listener: Fun00) {
        this.pageDidFinishLoadListeners.push(listener);
    }
    confirmWillQuit(callback: Fun10<boolean>) {
        callback(true);
    }
    boot() {
        try {
            this.debugging$ = Electron.app.commandLine.hasSwitch("d");
            this.debuggable$ = this.debugging$ || Electron.app.commandLine.hasSwitch("D");
            this.plugins$ = Electron.app.commandLine.hasSwitch("P");
            this.width = Ut.parseInt_(Electron.app.commandLine.getSwitchValue("w"), 600);
            this.height = Ut.parseInt_(Electron.app.commandLine.getSwitchValue("h"), 960);
            setupEventHandlers(this);
            setupSchemeHandlers(Electron.session.defaultSession.protocol, this.servlet$);
            
            createWindow(this.width, this.height, this.plugins$, this.debuggable$);
        } catch (e: any) {
            
            Electron.app.quit();
        }
    }

    showDeveloperTools(contents: WebContents | null) {
        if (this.debuggable$ && contents != null) {
            contents.openDevTools({
                mode: "detach",
            });
        }
    }

    handleIpcEvent(_event: Electron.IpcMainEvent, ...args: any[]) {
        if (args.length == 0) { return; }
        switch (args[0]) {
            case IPC.showDeveloperTools: {
                main.showDeveloperTools(main.contents$);
                break;
            }
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
