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
import { smap_, stringX } from "../bot/botcore";

Error.stackTraceLimit = 50;

export class ExpectedFailure extends Error {
}

export class ExpectedResult extends Error {
}

export abstract class MimeUt {
    protected static readonly bySuffix = smap_<string>(
        [".jpg", "image/jpeg"],
        [".png", "image/png"],
        [".gif", "image/gif"],
        [".ico", "image/ico"],
        [".svg", "image/svg+xml"],
        [".html", "text/html;charset=utf-8"],
        [".css", "text/css;charset=utf-8"],
        [".js", "text/javascript;charset=utf-8"],
        [".json", "application/json;charset=utf-8"],
        [".pdf", "application/pdf"],
    );
    protected static readonly byMime = smap_<string>(...Object.entries(MimeUt.bySuffix)
        .map<[string, string]>((entry) => {
            return [entry[1], entry[0]];
        }));
    static mimeOf(suffix: stringX): stringX {
        return suffix == null ? null : MimeUt.bySuffix[suffix] ?? null;
    }
    static suffixOf(mime: stringX): stringX {
        return mime == null ? null : MimeUt.byMime[mime] ?? null;
    }
}

