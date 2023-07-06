"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
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
const electron_1 = require("electron");
console.log("# Loading preload.js ...");
electron_1.contextBridge.exposeInMainWorld("XxXZl", {
    XxXior: (channel, callback) => {
        electron_1.ipcRenderer.on(channel, callback);
    },
    XxXisd: (channel, ...args) => {
        electron_1.ipcRenderer.send(channel, ...args);
    },
    XxXiiv: (channel, ...args) => {
        return electron_1.ipcRenderer.invoke(channel, ...args);
    },
    XxXira: (channel) => {
        electron_1.ipcRenderer.removeAllListeners(channel);
    }
});
//# sourceMappingURL=preload.js.map