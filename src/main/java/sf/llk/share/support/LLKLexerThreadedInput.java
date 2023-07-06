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
import java.io.Reader;

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
public class LLKLexerThreadedInput extends AbstractLexerInput {

    ////////////////////////////////////////////////////////////

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_FILL = false;
    private static final int BUF_SIZE = 1024 * 1024;
    private static final int CHUNK_SIZE = BUF_SIZE / 4;

    ////////////////////////////////////////////////////////////

    private final Reader reader;
    private char[] buffer;
    private int tailOffset;
    private volatile int headOffset;
    private boolean isEOF;
    private int cutoff = -1;
    private int extraCutoff = -1;
    private final IntHeap extraCutoffs;
    private FillingThread fillingThread;

    ////////////////////////////////////////////////////////////

    public LLKLexerThreadedInput(final Reader r, final ILLKMain main) {
        this(r, BUF_SIZE, main);
    }

    public LLKLexerThreadedInput(final Reader r, final int bufsize, final ILLKMain main) {
        super(main);
        reader = r;
        buffer = new char[bufsize];
        extraCutoffs = new IntHeap(IIntComparator.AscendingComparator.getSingleton());
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
            }
        }
        super.reset();
        fillingThread = null;
        tailOffset = 0;
        offset = 0;
        headOffset = 0;
        isEOF = false;
        cutoff = -1;
        extraCutoff = -1;
        extraCutoffs.clear();
        try {
            reader.reset();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        locator.rewind(0);
        startFilling();
        LA1x();
    }

    ////////////////////////////////////////////////////////////

    public int getSourceLength() {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final synchronized void setCutoff() {
        cutoff = offset;
    }

    @Override
    public final int addCutoff() {
        addCutoff(offset);
        return offset;
    }

    @Override
    public final void addCutoff(final int offset) {
        if (extraCutoff < 0) {
            extraCutoff = offset;
            return;
        }
        if (offset >= extraCutoff) {
            extraCutoffs.enqueue(offset);
            return;
        }
        extraCutoffs.enqueue(extraCutoff);
        extraCutoff = offset;
    }

    @Override
    public final boolean removeCutoff(final int offset) {
        if (extraCutoff < 0 || offset < extraCutoff) {
            return false;
        }
        if (offset == extraCutoff) {
            extraCutoff = extraCutoffs.isEmpty() ? -1 : extraCutoffs.dequeue();
            return true;
        }
        return extraCutoffs.remove(offset);
    }

    ////////////////////////////////////////////////////////////

    protected void LA1x() {
        if (offset >= headOffset && !fill(1)) {
            if (la1 == ILLKConstants.LEXER_EOF) {
                throw new LLKParseError("Reading passed EOF: offset=" + offset);
            }
            la1 = ILLKConstants.LEXER_EOF;
            return;
        }
        la1 = buffer[offset - tailOffset];
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
    public final int consume(final IConsumer consumer) throws LLKParseError {
        int len = headOffset - tailOffset;
        int start = offset - tailOffset;
        int index = start;
        while (true) {
            if (index == len) {
                offset += index - start;
                if (!fill(1)) {
                    break;
                }
                len = headOffset - tailOffset;
                start = offset - tailOffset;
                index = start;
            }
            if (!consumer.consume(buffer[index])) {
                offset += index - start;
                return la1 = buffer[index];
            }
            ++index;
        }
        offset += index - start;
        return ILLKConstants.LEXER_EOF;
    }

    @Override
    public final int LA0() {
        if (offset == 0) {
            return ILLKConstants.LEXER_EOF;
        }
        return buffer[offset - 1 - tailOffset];
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
        return buffer[index - tailOffset];
    }

    @Override
    public final boolean LA(final char[] expected) {
        final int len = expected.length;
        int index = offset;
        if (index + len > headOffset && !fill(len)) {
            return false;
        }
        index -= tailOffset;
        for (int i = 0; i < len; ++i, ++index) {
            if (buffer[index] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean LA(final char[] expected, final int start) {
        final int len = expected.length;
        int index = offset + start - 1;
        if (index + len > headOffset && !fill(len)) {
            return false;
        }
        index -= tailOffset;
        for (int i = 0; i < len; ++i, ++index) {
            if (buffer[index] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public final char charAt(final int index) {
        if (DEBUG) {
            if (index < tailOffset || index > offset) {
                throw new AssertionError(
                    "Index out of bound: baseOffset=" + tailOffset + ", offset=" + offset + ", index=" + index);
            }
        }
        return buffer[index - tailOffset];
    }

    @Override
    public void getChars(final int sstart, final int send, final char[] dst, final int dstart) {
        if (DEBUG) {
            if (sstart < tailOffset || send > offset) {
                throw new AssertionError(
                    "Index out of bound: bound start="
                        + tailOffset
                        + ", end="
                        + offset
                        + ", request start="
                        + sstart
                        + ", end="
                        + send);
            }
        }
        System.arraycopy(buffer, sstart - tailOffset, dst, dstart, send - sstart);
    }

    @Override
    public final CharSequence getSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final CharSequence getSource(final int start, final int end) {
        if (DEBUG) {
            if (end < start || start < tailOffset || end > offset) {
                throw new AssertionError(
                    "Index out of bound: baseOffset="
                        + tailOffset
                        + ", offset="
                        + offset
                        + ", start="
                        + start
                        + ", end="
                        + end);
            }
        }
        return new CharRange(buffer, start - tailOffset, end - tailOffset);
    }

    @Override
    public void setIgnoreCase(final boolean ignorecase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIgnoreCase() {
        return false;
    }

    ////////////////////////////////////////////////////////////

    private void startFilling() {
        fillingThread = new FillingThread();
        fillingThread.start();
    }

    private void trace(final String msg) {
        System.out.println(msg);
    }

    private synchronized boolean fill(final int require) {
        if (isEOF) {
            return false;
        }
        int keep = offset;
        if (markOffset > 0) {
            keep = markLocations[0];
        }
        if (cutoff >= 0 && keep > cutoff) {
            keep = cutoff;
        }
        if (extraCutoff >= 0 && keep > extraCutoff) {
            keep = extraCutoff;
        }
        keep = headOffset - keep + (keep > tailOffset ? 1 : 0);
        final int filled = headOffset - tailOffset;
        int notkeep = 0;
        final int min = Math.min(require, CHUNK_SIZE);
        if (keep == filled || keep + require >= buffer.length) {
            final char[] old = buffer;
            buffer = new char[buffer.length + min];
            System.arraycopy(old, 0, buffer, 0, filled);
            if (DEBUG_FILL) {
                System.out.println("# fill(): expand: " + buffer.length);
            }
        } else {
            notkeep = filled - keep;
            if (notkeep > 0) {
                System.arraycopy(buffer, notkeep, buffer, 0, keep);
                tailOffset += notkeep;
                if (DEBUG_FILL) {
                    System.out.println("# fill(): shift: " + notkeep);
                }
            }
        }
        while (headOffset - offset < min) {
            if (headOffset - tailOffset < buffer.length && fillingThread.isDone()) {
                isEOF = true;
                fillingThread.abort();
                break;
            }
            synchronized (fillingThread) {
                fillingThread.notify();
            }
            try {
                wait(1);
            } catch (final InterruptedException e) {
                isEOF = true;
                break;
            }
        }
        if (DEBUG_FILL) {
            trace("fill(): " + (headOffset - tailOffset));
        }
        return (headOffset >= offset + require);
    }

    private synchronized boolean fillBuffer() throws IOException {
        int filled = headOffset - tailOffset;
        int toread = buffer.length - filled;
        if (toread > CHUNK_SIZE) {
            toread = CHUNK_SIZE;
        }
        while (toread > 0) {
            final int n = reader.read(buffer, filled, toread);
            if (n < 0) {
                if (DEBUG) {
                    trace("fillBuffer(): EOF");
                }
                notify();
                Thread.yield();
                return true;
            }
            filled += n;
            headOffset += n;
            toread -= n;
        }
        if (DEBUG_FILL) {
            trace("fillBuffer(): " + filled);
        }
        return false;
    }

    private synchronized boolean isFilled() {
        return (headOffset - tailOffset) == buffer.length;
    }

    private synchronized void pause() {
        try {
            wait(1);
        } catch (final InterruptedException ignored) {
        }
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
            while (!isDone) {
                if (isAborting) {
                    break;
                }
                if (isFilled()) {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (final InterruptedException ignored) {
                        }
                    }
                } else {
                    try {
                        isDone = fillBuffer();
                        pause();
                    } catch (final IOException e) {
                        isDone = true;
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////
}
