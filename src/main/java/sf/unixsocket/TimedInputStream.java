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
import java.io.InterruptedIOException;

class TimedInputStream extends InputStream {

	private InputStream input;
	private int timeout;

	/**
	 * @param timeout		If the read call blocks for the specified amount of time it will be cancelled,
	 * and a java.io.InterruptedIOException will be thrown. A <code>timeout</code> of zero
	 * is interpreted as an infinite timeout.
	 */
	protected TimedInputStream(InputStream input, int timeout) {
		this.input = input;
		this.timeout = timeout;
	}

	public int read() throws IOException {
		byte[] b = new byte[1];
		int count = read1(timeout, b, 0, 1);
		if (count == -1)
			throw new IOException();
		return count > 0 ? (b[0] & 0xff) : -1;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		int count = read1(timeout, b, off, len);
		if (count == -1)
			throw new IOException();
		return count;
	}

	private int read1(int timeout, final byte[] b, final int off, final int len)
		throws InterruptedIOException {
		final int[] ret = { -1 };
		Thread thread = new Thread() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				try {
					ret[0] = input.read(b, off, len);
				} catch (IOException e) {
			}}
		};
		thread.setDaemon(false);
		thread.start();
		try {
			thread.join(timeout);
		} catch (InterruptedException e) {
		}
		if (thread.isAlive()) {
			throw new InterruptedIOException("Read timed out");
		}
		return ret[0];
	}
}