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
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class UnixServerSocket extends UnixSocket {

    private Lock lock = new ReentrantLock();
    private List<Function<Exception, Void>> onErrorListeners = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////

    public UnixServerSocket(String sockfile, SockType socktype) {
        super.socketFile = sockfile;
        super.socketType = socktype;
    }

    ////////////////////////////////////////////////////////////////////////

    public UnixServerSocket listen(int backlog) throws IOException {
        lock.lock();
        try {
            if ((nativeSocketHandle = Native.listen(socketFile, socketType.ordinal(), backlog)) == -1)
                throw new IOException("Failed to open and listen on unix socket");
            return this;
        } finally {
            lock.unlock();
        }
    }

    public UnixSocket accept() throws IOException {
        if (this.closed()) throw new InterruptedIOException();
        int sockhandle = -1;
        if ((sockhandle = Native.accept(nativeSocketHandle, socketType.ordinal())) == -1) {
            lock.lock();
            try {
                IOException e = new IOException("Failed to accept on unix socket");
                for (Function<Exception, Void> listener : onErrorListeners) {
                    listener.apply(e);
                }
                throw e;
            } finally {
                lock.unlock();
            }
        }
        return new UnixSocket(sockhandle, socketType);
    }

    public UnixServerSocket onClose(Runnable listener) {
        lock.lock();
        try {
            super.onClose(listener);
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            super.close();
        } finally {
            lock.unlock();
        }
    }

    public boolean closed() {
        return this.closed;
    }

    public UnixServerSocket onError(Function<Exception, Void> callback) {
        this.onErrorListeners.add(callback);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////
}
