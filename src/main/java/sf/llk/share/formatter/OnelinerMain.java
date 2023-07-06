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
package sf.llk.share.formatter;

import java.io.IOException;
import java.util.Map;

import sf.llk.share.support.ISourceLocation;
import sf.llk.share.support.SimpleLLKMain;

public class OnelinerMain extends SimpleLLKMain {

    boolean debug;

    public OnelinerMain(String source) {
        super(source, null, new OnelinerLocator());
        this.debug = (source != null);
    }

    public OnelinerMain(String source, Map<String, Object> options) {
        super(source, options, new OnelinerLocator());
        this.debug = (source != null);
    }

    @Override
    public char[] getFileContent() throws IOException {
        return getFilepath().toCharArray();
    }

    @Override
    public void error(String msg, Throwable e, ISourceLocation loc) {
        if (debug)
            super.error(msg, e, loc);
        else
            error();
    }

    public void warn(String msg, Throwable e, ISourceLocation loc) {
        if (debug)
            super.warn(msg, e, loc);
        else
            warn();
    }
}