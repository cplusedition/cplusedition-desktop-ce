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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UnixSocket {

    public enum SockType {
        DGRAM, STREAM;
    }

    protected UnixSocketInputStream in;
    protected UnixSocketOutputStream out;
    protected int nativeSocketHandle;
    protected String socketFile;
    protected SockType socketType;
    protected boolean closed = false;
    protected List<Runnable> onCloseListeners = new ArrayList<Runnable>();

    ////////////////////////////////////////////////////////////////////////

    protected UnixSocket() {
    }

    protected UnixSocket(int sockhandle, SockType socktype) throws IOException {
        nativeSocketHandle = sockhandle;
        socketType = socktype;
        socketFile = null;
        in = new UnixSocketInputStream(nativeSocketHandle);
        if (socketType == UnixSocket.SockType.STREAM)
            out = new UnixSocketOutputStream(nativeSocketHandle);
    }

    ////////////////////////////////////////////////////////////////////////

    public InputStream getInputStream() {
        return in;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public void close() {
        this.closed = true;
        Native.close(nativeSocketHandle);
        for (Runnable listener : this.onCloseListeners) {
            listener.run();
        }
    }

    public UnixSocket onClose(Runnable listener) {
        this.onCloseListeners.add(listener);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////

    protected class UnixSocketInputStream extends InputStream {
        private int nativeHandle;
        protected UnixSocketInputStream(int sockhandle) {
            this.nativeHandle = sockhandle;
        }
        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int count = Native.read(nativeHandle, b, 0, 1);
            if (count == -1)
                throw new IOException();
            return count > 0 ? (b[0] & 0xff) : -1;
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            int count = Native.read(nativeHandle, b, off, len);
            if (count == -1)
                throw new IOException();
            if (count == 0)
                return -1;
            return count;
        }
        public void close() throws IOException {
            Native.closeInput(nativeHandle);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    protected static class UnixSocketOutputStream extends OutputStream {
        private int nativeHandle;
        private boolean closed = false;
        private Lock lock = new ReentrantLock();
        protected UnixSocketOutputStream(int sockhandle) {
            this.nativeHandle = sockhandle;
        }
        @Override
        public void write(int b) throws IOException {
            lock.lock();
            try {
                if (closed) throw new IOException();
                byte[] data = new byte[1];
                data[0] = (byte) b;
                if (Native.write(nativeHandle, data, 0, 1) != 1)
                    throw new IOException();
            } finally {
                lock.unlock();
            }
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            lock.lock();
            try {
                if (closed) throw new IOException();
                if (b == null) {
                    throw new NullPointerException();
                } else if ((off < 0)
                        || (off > b.length)
                        || (len < 0)
                        || ((off + len) > b.length)
                        || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                if (Native.write(nativeHandle, b, off, len) != len) throw new IOException();
            } finally {
                lock.unlock();
            }
        }
        public void close() {
            lock.lock();
            try {
                if (!closed) {
                    closed = true;
                    Native.closeOutput(nativeHandle);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    protected static class Native {
        native static int listen(String sockfile, int socktype, int backlog);
        native static int accept(int nativehandle, int socktype);
        native static int open(String sockfile, int socktype);
        native static int read(int nativehandle, byte[] b, int off, int len);
        native static int write(int nativehandle, byte[] b, int off, int len);
        native static int close(int nativehandle);
        native static int closeInput(int nativehandle);
        native static int closeOutput(int nativehandle);
    }

    ////////////////////////////////////////////////////////////////////////
}
