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
import { contextBridge, ipcRenderer, IpcRendererEvent } from "electron";

type IpcRendererCallback = (event: IpcRendererEvent, ...args: any[]) => void;

console.log("# Loading preload.js ...");
contextBridge.exposeInMainWorld(
"XxXZl"
    , {
XxXior
            : (channel: string, callback: IpcRendererCallback) => {
                ipcRenderer.on(channel, callback);
            },
XxXisd
            : (channel: string, ...args: any[]) => {
                ipcRenderer.send(channel, ...args);
            },
XxXiiv
            : (channel: string, ...args: any[]) => {
                return ipcRenderer.invoke(channel, ...args);
            },
XxXira
            : (channel: string) => {
                ipcRenderer.removeAllListeners(channel);
            }
    });
