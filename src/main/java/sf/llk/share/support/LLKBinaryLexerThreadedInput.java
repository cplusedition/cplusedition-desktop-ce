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
package sf.llk.share.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

/*
 * Same as LLKLexerStreamInput but filling of the input buffer is preformed in a separate thread
 * which may be performed in parallel for multi-cored or multi-threaded systems.
 *
 * |xxxxxxxxxxxxxxxxYYYYYYYYYYYYYYYYzzzzzzzzzzzzzzzzz................|
 *  ^tailOffset     ^offset         ^headOffset      ^filledOffset
 */
public class LLKBinaryLexerThreadedInput extends AbstractBinaryLexerInput {

    ////////////////////////////////////////////////////////////

    private static final boolean DEBUG = false;
    private static final int BUF_SIZE = 32 * 1024;
    private static final int CHUNK_SIZE = 4 * 1024;

    ////////////////////////////////////////////////////////////

    private final InputStream reader;
    private final byte[] buffer;
    private int tailOffset;
    private int headOffset;
    private volatile int filledOffset;
    private boolean isEOF;
    private int cutOff;
    private int markOffset;
    private int[] markLocations;
    private FillingThread fillingThread;
    private long startTime;
    private int la1;

    ////////////////////////////////////////////////////////////

    public LLKBinaryLexerThreadedInput(final InputStream r, final ILLKMain main) {
        this(r, BUF_SIZE, main);
    }

    public LLKBinaryLexerThreadedInput(final InputStream r, final int bufsize, final ILLKMain main) {
        super(main);
        reader = r;
        buffer = new byte[bufsize];
        markLocations = new int[32];
        startFilling();
        LA1x();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public synchronized void reset() {
        fillingThread.abort();
        while (!fillingThread.isDone()) {
            try {
                wait();
            } catch (final InterruptedException ignored) {
        }}
        fillingThread = null;
        tailOffset = 0;
        offset = 0;
        headOffset = 0;
        filledOffset = 0;
        markOffset = 0;
        isEOF = false;
        try {
            reader.reset();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        locator.rewind(0);
        startFilling();
        LA1x();
    }

    public void setIgnoreCase(final boolean ignorecase) {
        throw new UnsupportedOperationException();
    }

    public boolean isIgnoreCase() {
        return false;
    }

    public int getSourceLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void mark() {
        if (markOffset >= markLocations.length) {
            expandMarkLocations();
        }
        markLocations[markOffset++] = offset;
        locator.mark();
    }

    @Override
    public final void unmark() {
        --markOffset;
        locator.unmark();
    }

    @Override
    public final void remark() {
        locator.unmark();
        markLocations[markOffset - 1] = offset;
        locator.mark();
    }

    @Override
    public final void rewind() {
        offset = markLocations[--markOffset];
        if (offset < tailOffset) {
            throw new LLKParseError("baseOffset=" + tailOffset + ", offset=" + offset);
        }
        locator.rewind();
        LA1x();
    }

    @Override
    public final boolean isMarked() {
        return markOffset > 0;
    }

    /**
     * Rewind character input stream to the given offset.
     */
    @Override
    public final void rewind(final int offset) {
        this.offset = offset;
        locator.rewind(offset);
        LA1x();
    }

    @Override
    public synchronized void setCutoff() {
        cutOff = offset;
    }

    @Override
    public synchronized void setCutoff(final int offset) {
        cutOff = offset;
    }

    @Override
    public final void consume() throws LLKParseError {
        if (offset >= headOffset && !fill(1)) {
            throw new LLKParseError("consume() passed EOF");
        }
        ++offset;
        LA1x();
    }

    @Override
    public final void consume(final int n) throws LLKParseError {
        final int end = offset + n;
        if (end > headOffset && !fill(n)) {
            throw new LLKParseError("consume(int) passed EOF: length=" + headOffset + ", offset=" + end);
        }
        offset = end;
        LA1x();
    }

    @Override
    public final int LA0() {
        if (offset == 0) {
            return ILLKConstants.LEXER_EOF;
        }
        return get(offset - 1);
    }

    @Override
    public final int LA1() {
        return la1;
    }

    @Override
    public final int LA(final int n) {
        final int index = n + offset - 1;
        if (index >= headOffset && !fill(n)) {
            return ILLKConstants.LEXER_EOF;
        }
        return get(index);
    }

    @Override
    public final boolean LA(final byte[] expected) {
        final int len = expected.length;
        int index = offset;
        if (index + len > headOffset && !fill(len)) {
            return false;
        }
        index -= tailOffset;
        for (int i = 0; i < len; ++i, ++index) {
            if (buffer[index] != expected[i]) {
                return false;
        }}
        return true;
    }

    @Override
    public final boolean LA(final byte[] expected, final int start) {
        final int len = expected.length;
        int index = offset + start - 1;
        if (index + len > headOffset && !fill(len)) {
            return false;
        }
        index -= tailOffset;
        for (int i = 0; i < len; ++i, ++index) {
            if (buffer[index] != expected[i]) {
                return false;
        }}
        return true;
    }

    private void LA1x() {
        if (offset >= headOffset && !fill(1)) {
            la1 = ILLKConstants.LEXER_EOF;
            return;
        }
        la1 = get(offset);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public int size() {
        return offset;
    }

    @Override
    public byte byteAt(final int index) {
        if (index < tailOffset || index > headOffset) {
            throw new AssertionError(
                "Index out of bound: tail=" + tailOffset + ", head=" + headOffset + ", index=" + index);
        }
        return buffer[index - tailOffset];
    }

    @Override
    public void getBytes(final int start, final int end, final byte[] dst, final int dststart) {
        if (start < tailOffset || end > headOffset) {
            throw new AssertionError(
                "Index out of bound: tail="
                    + tailOffset
                    + ", head="
                    + headOffset
                    + ", request start="
                    + start
                    + ", end="
                    + end);
        }
        System.arraycopy(buffer, start - tailOffset, dst, dststart, end - start);
    }

    @Override
    public void write(final OutputStream os, final int start, final int end) throws IOException {
        if (start < tailOffset || end > headOffset) {
            throw new AssertionError(
                "Index out of bound: tail="
                    + tailOffset
                    + ", head="
                    + headOffset
                    + ", request start="
                    + start
                    + ", end="
                    + end);
        }
        os.write(buffer, start - tailOffset, end - start);
    }

    @Override
    public IByteSequence subSequence(final int start, final int end) {
        if (start < tailOffset || end > headOffset) {
            throw new AssertionError(
                "Index out of bound: tail="
                    + tailOffset
                    + ", head="
                    + headOffset
                    + ", request start="
                    + start
                    + ", end="
                    + end);
        }
        return new ByteRange(buffer, start - tailOffset, end - tailOffset);
    }

    @Override
    public byte[] toArray() {
        throw new UnsupportedOperationException();
    }

    public final int getByte(final int index) {
        if (DEBUG) {
            if (index < tailOffset || index > offset) {
                throw new AssertionError(
                    "Index out of bound: baseOffset=" + tailOffset + ", offset=" + offset + ", index=" + index);
        }}
        return buffer[index - tailOffset] & 0xff;
    }

    @Override
    public final byte[] getSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final byte[] getSource(final int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final byte[] getSource(final int start, final int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IByteSequence getSourceSequence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IByteSequence getSourceSequence(final int start) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IByteSequence getSourceSequence(final int start, final int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replace(final int start, final int end, final byte[] text) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final byte[] text, final int tstart, final int tend) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    @Override
    public void replace(final int start, final int end, final IByteSequence text) {
        throw new UnsupportedOperationException("Lexer input buffer is read only");
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final boolean matchOpt(final byte c) {
        if (LA1() != c) {
            return false;
        }
        consume();
        return true;
    }

    @Override
    public int LA1Consume() {
        final int ret = get(offset);
        consume();
        return ret;
    }

    @Override
    public int consumeLA0() {
        int la0 = la1;
        consume();
        return la0;
    }

    @Override
    public int consumeLA1() {
        consume();
        return la1;
    }

    @Override
    public final short consume2BE() {
        final int end = offset + 2;
        if (end > headOffset && !fill(2)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        final int index = offset - tailOffset + 1;
        offset = end;
        final short ret = (short)((la1 << 8) | buffer[index] & 0xff);
        LA1x();
        return ret;
    }

    @Override
    public final short consume2LE() {
        final int end = offset + 2;
        if (end > headOffset && !fill(2)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        final int index = offset - tailOffset + 1;
        final short ret = (short)(la1 & 0xff | buffer[index] << 8);
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final int consumeU2BE() {
        final int end = offset + 2;
        if (end > headOffset && !fill(2)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        final int index = offset - tailOffset + 1;
        final int ret = ((la1 << 8) | buffer[index] & 0xff) & 0xffff;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final int consumeU2LE() {
        final int end = offset + 2;
        if (end > headOffset && !fill(2)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        final int index = offset - tailOffset + 1;
        final int ret = (la1 & 0xff | buffer[index] << 8) & 0xffff;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final int consume4BE() {
        final int end = offset + 4;
        if (end > headOffset && !fill(4)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        int index = offset - tailOffset + 1;
        final int ret = (la1 << 8 | buffer[index++] & 0xff) << 16
            | (buffer[index++] << 8 | buffer[index] & 0xff) & 0xffff;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final int consume4LE() {
        final int end = offset + 4;
        if (end > headOffset && !fill(4)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        int index = offset - tailOffset + 1;
        final int ret = (la1 & 0xff | buffer[index++] << 8) & 0xffff
            | (buffer[index++] & 0xff | buffer[index] << 8) << 16;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final long consume8BE() {
        final int end = offset + 8;
        if (end > headOffset && !fill(8)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        int index = offset - tailOffset + 1;
        final long ret = ((la1 << 8 | buffer[index++] & 0xffL) << 16
            | (buffer[index++] << 8 | buffer[index++] & 0xff) & 0xffffL) << 32
            | ((buffer[index++] << 8 | buffer[index++] & 0xffL) << 16
                | (buffer[index++] << 8 | buffer[index] & 0xff) & 0xffffL) & 0xffffffffL;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final long consume8LE() {
        final int end = offset + 8;
        if (end > headOffset && !fill(8)) {
            throw new LLKParseError("Reading passed EOF: headOffset=" + headOffset + ", offset=" + end);
        }
        int index = offset - tailOffset + 1;
        final long ret = ((la1 & 0xffL | buffer[index++] << 8) & 0xffffL
            | (buffer[index++] & 0xffL | buffer[index++] << 8) << 16) & 0xffffffffL
            | ((buffer[index++] & 0xffL | buffer[index++] << 8) & 0xffffL
                | (buffer[index++] & 0xffL | buffer[index] << 8) << 16) << 32;
        offset = end;
        LA1x();
        return ret;
    }

    @Override
    public final void match(final int c) throws LLKParseException {
        if (LA1() != c) {
            throw llkMismatchException("match(byte): expected=", c);
        }
        consume();
    }

    @Override
    public final void matchNot(final int c) throws LLKParseException {
        if (LA1() == c) {
            throw llkMismatchException("matchNot(byte): not expected=", c);
        }
        consume();
    }

    @Override
    public final void match(final int c1, final int c2) throws LLKParseException {
        final int c = LA1();
        if (c != c1 && c != c2) {
            throw llkMismatchException("match(byte, byte): expected=", c1, c2, (byte)c);
        }
        consume();
    }

    @Override
    public final void matchNot(final int c1, final int c2) throws LLKParseException {
        final int c = LA1();
        if (c == c1 || c == c2) {
            throw llkMismatchException("matchNot(byte, byte): not expected=", c1, c2, (byte)c);
        }
        consume();
    }

    @Override
    public final void match(final int c1, final int c2, final int c3) throws LLKParseException {
        final int c = LA1();
        if (c != c1 && c != c2 && c != c3) {
            throw llkMismatchException("match(byte, byte, byte): expected=", c1, c2, c3, (byte)c);
        }
        consume();
    }

    @Override
    public final void matchNot(final int c1, final int c2, final int c3) throws LLKParseException {
        final int c = LA1();
        if (c == c1 || c == c2 || c == c3) {
            throw llkMismatchException("matchNot(byte, byte, byte): not expected=", c1, c2, c3, (byte)c);
        }
        consume();
    }

    @Override
    public final void matchRange(final int first, final int last) throws LLKParseException {
        final int c = LA1();
        if (c < first || c > last) {
            throw llkMismatchException("matchRange(byte, byte): range=", first, last, (byte)c);
        }
        consume();
    }

    @Override
    public final void matchNotRange(final int first, final int last) throws LLKParseException {
        final int c = LA1();
        if (c >= first && c <= last) {
            throw llkMismatchException("matchNotRange(byte, byte): range=", first, last, (byte)c);
        }
        consume();
    }

    @Override
    public final void match(final int[] bset) throws LLKParseException {
        if (llkGetBit(LA1(), bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
    }}

    @Override
    public final void matchNot(final int[] bset) throws LLKParseException {
        if (llkGetBitInverted(LA1(), bset)) {
            consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset);
    }}

    @Override
    public final void match(final byte[] s) throws LLKParseException {
        final int len = s.length;
        for (int i = 0; i < len; i++) {
            if (LA1() != s[i]) {
                throw llkMismatchException(
                    "match(byte[]): expected string=" + _toString(s) + ", byte=" + s[i] + ", actual=" + LA1());
            }
            consume();
    }}

    @Override
    public final void match(final byte[] s, final int start) throws LLKParseException {
        consume(start);
        final int len = s.length;
        for (int i = start; i < len; i++) {
            if (LA1() != s[i]) {
                throw llkMismatchException(
                    "match(byte[], int): expected string=" + _toString(s) + ", byte=" + s[i] + ", actual=" + LA1());
            }
            consume();
    }}

    ////////////////////////////////////////////////////////////

    private void startFilling() {
        fillingThread = new FillingThread();
        if (DEBUG) {
            startTime = System.currentTimeMillis();
        }
        fillingThread.start();
    }

    private int get(final int offset) {
        final int index = offset - tailOffset;
        if (index < 0 || index >= headOffset - tailOffset) {
            throw new LLKParseError(
                "baseOffset=" + tailOffset + ", offset=" + offset + " head=" + headOffset + ", index=" + index);
        }
        return buffer[index] & 0xff;
    }

    private void expandMarkLocations() {
        final int[] ret = new int[markLocations.length * 2];
        System.arraycopy(markLocations, 0, ret, 0, markOffset);
        markLocations = ret;
    }

    private void trace(final String msg) {
        System.err.println(
            String.format(
                "# %s: filledOffset=%d, %.3f (sec)",
                msg,
                filledOffset,
                (System.currentTimeMillis() - startTime) / 1000f));
    }

    private synchronized boolean fill(final int require) {
        if (isEOF) {
            return false;
        }
        int keep = offset;
        if (markOffset > 0) {
            keep = markLocations[0];
        }
        if (cutOff >= 0 && cutOff < keep) {
            keep = cutOff;
        }
        keep = headOffset - keep + (keep > tailOffset ? 1 : 0);
        int mark = headOffset - tailOffset;
        int notkeep = 0;
        if (keep < mark) {
            notkeep = mark - keep;
            System.arraycopy(buffer, notkeep, buffer, 0, keep);
            tailOffset += notkeep;
            if (filledOffset == mark) {
                mark = keep;
                filledOffset = keep;
        }}
        while (filledOffset - mark < require) {
            synchronized (fillingThread) {
                if (filledOffset == mark && fillingThread.isDone()) {
                    isEOF = true;
                    fillingThread.abort();
                    break;
                }
                fillingThread.notify();
            }
            if (!isEOF) {
                try {
                    wait(100);
                } catch (final InterruptedException e) {
                    isEOF = true;
        }}}
        final int len = filledOffset - mark;
        if (notkeep > 0) {
            System.arraycopy(buffer, mark, buffer, headOffset - tailOffset, len);
        }
        headOffset += len;
        filledOffset = headOffset - tailOffset;
        if (DEBUG) {
            trace("fill()");
        }
        synchronized (fillingThread) {
            fillingThread.notify();
        }
        return (headOffset >= offset + require);
    }

    synchronized boolean fillBuffer() throws IOException {
        final int filled = filledOffset;
        int toread = buffer.length - filled;
        if (toread > CHUNK_SIZE) {
            toread = CHUNK_SIZE;
        }
        final int n = reader.read(buffer, filled, toread);
        if (n < 0) {
            if (DEBUG) {
                trace("fillBuffer(): EOF");
            }
            notify();
            Thread.yield();
            return true;
        }
        filledOffset += n;
        if (DEBUG) {
            trace("fillBuffer()");
        }
        notify();
        return false;
    }

    boolean isFull() {
        return filledOffset == buffer.length;
    }

    ////////////////////////////////////////////////////////////

    private class FillingThread extends Thread {
        volatile boolean isAborting;
        volatile boolean isDone;

        FillingThread() {
        }

        public synchronized void abort() {
            isAborting = true;
        }

        public synchronized boolean isDone() {
            return isDone;
        }

        @Override
        public void run() {
            boolean done = false;
            while (true) {
                synchronized (this) {
                    if (done) {
                        isDone = true;
                    }
                    if (isAborting) {
                        break;
                    }
                    while (!isDone && isFull()) {
                        try {
                            wait();
                        } catch (final InterruptedException ignored) {
                }}}
                try {
                    done = fillBuffer();
                } catch (final IOException e) {
                    done = true;
        }}}
    }
}
