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
/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.support;

import java.io.IOException;
import java.io.OutputStream;

public class ByteRange implements IByteSequence {

    final byte[] array;
    final int start;
    final int end;

    //////////////////////////////////////////////////////////////////////

    public ByteRange(byte[] a) {
        this.array = a;
        this.start = 0;
        this.end = a.length;
    }

    public ByteRange(byte[] a, int start, int end) {
        this.array = a;
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public byte byteAt(int index) {
        return array[start + index];
    }

    @Override
    public IByteSequence subSequence(int start, int end) {
        return new ByteRange(array, this.start + start, this.start + end);
    }

    @Override
    public void getBytes(int srcstart, int srcend, byte[] dst, int dststart) {
        System.arraycopy(array, start + srcstart, dst, dststart, srcend - srcstart);
    }

    @Override
    public void write(OutputStream os, int start, int end) throws IOException {
        os.write(array, start, end - start);
    }

    @Override
    public byte[] toArray() {
        int len = end - start;
        byte[] ret = new byte[len];
        System.arraycopy(array, start, ret, 0, len);
        return ret;
    }

    //////////////////////////////////////////////////////////////////////
}
