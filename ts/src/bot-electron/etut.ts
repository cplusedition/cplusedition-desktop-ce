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
const { } = require('electron');
import { ILogger } from "../bot/botcore";

export class EtUt {
    static setSecureWebPreferences_(prefs: Electron.WebPreferences, plugins: boolean = false, debugging: boolean = false): Electron.WebPreferences {
        prefs.devTools = debugging;
        prefs.plugins = plugins;
        prefs.sandbox = true;
        prefs.javascript = true;
        prefs.webSecurity = true;
        prefs.allowRunningInsecureContent = false;
        prefs.contextIsolation = true;
        prefs.enableRemoteModule = false;
        prefs.nodeIntegration = false;
        prefs.disableDialogs = true;
        prefs.textAreasAreResizable = false;
        prefs.defaultEncoding = 'UTF-8';
        return prefs;
    }

    static configSession_(session: Electron.Session, log: ILogger | null = null) {
        session
            .setPermissionRequestHandler((webContents, permission, callback) => {
                const url = webContents.getURL();
                log?.d_(`# Session: Deny permssion: ${permission} for ${url}`);
                callback(false);
            });
        session.on('will-download', (event, item, _webContents) => {
            const url = item.getURL();
            const mime = item.getMimeType();
            log?.d_(`# Session: Deny download: ${url}, ${mime}`);
            event.preventDefault();
            item.cancel();
        });
        session.on('spellcheck-dictionary-download-begin', (event, langcode) => {
            log?.d_(`# Session: Deny download dictionary for: ${langcode}`);
            event.preventDefault();
        });
        session.enableNetworkEmulation({ offline: true });
        Promise.all([
            session.clearHostResolverCache(),
            session.clearAuthCache({ type: "password" }),
            session.clearCache(),
            session.clearStorageData(),
        ]);
    }

    static copyProperties_(to: Object, from: Object) {
        Object.getOwnPropertyNames(from)
            .forEach(function (propKey) {
                const desc = Object.getOwnPropertyDescriptor(from, propKey);
                if (desc) {
                    Object.defineProperty(to, propKey, desc);
                }
            });
    }

    static offsite_(url: URL, docurl: URL): boolean {
        return url.protocol != docurl.protocol || url.hostname != docurl.hostname || url.port != docurl.port;
    }
}
