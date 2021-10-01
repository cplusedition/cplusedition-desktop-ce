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
package sf.unixsocket;

import java.io.IOException;
import java.io.InputStream;

public class UnixClientSocket extends UnixSocket {

    ////////////////////////////////////////////////////////////////////////

    public UnixClientSocket(String sockfile, SockType socktype) {
        super.socketFile = sockfile;
        super.socketType = socktype;
    }

    public UnixClientSocket connect() throws IOException {
        if ((nativeSocketHandle = Native.open(socketFile, socketType.ordinal())) == -1)
            throw new IOException("Failed to open unix socket");
        if (socketType == SockType.STREAM)
            in = new UnixSocketInputStream(nativeSocketHandle);
        out = new UnixSocketOutputStream(nativeSocketHandle);
        return this;
    }

    public UnixClientSocket onClose(Runnable listener) {
        super.onClose(listener);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public InputStream getInputStream() {
        if (socketType == SockType.STREAM)
            return in;
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////////////////
}
