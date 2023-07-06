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

import java.util.ArrayList;
import java.util.List;

public abstract class LLKBinaryLexerBase implements ILLKBinaryLexer {

    protected abstract boolean llkGetBitInverted(int la1, int[] bitset);

    /*
	 * The software in this package is distributed under the GNU General Public
	 * License version 2, as published by the Free Software Foundation, but with
	 * the Classpath exception.  You should have received a copy of the GNU General
	 * Public License (GPL) and the Classpath exception along with this program.
	 */

    protected ILLKBinaryLexerInput llkInput;
    protected ILLKMain llkMain;
    protected ISourceLocator llkLocator;
    protected LLKTree llkTree = new LLKTree();
    protected boolean llkSeenEOF = false;
    protected final List<ILLKLifeCycleListener> llkLifeCycleListeners = new ArrayList<>(4);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    ///////////////////////////////////////////////////////////////////////

    @Override
    public ILLKBinaryLexerInput llkGetInput() {
        return llkInput;
    }

    @Override
    public ILLKMain llkGetMain() {
        return llkMain;
    }

    @Override
    public final int llkGetOffset() {
        return llkInput.getOffset();
    }

    @Override
    public void llkRewind(final int offset) {
        llkInput.rewind(offset);
    }

    public void llkAddLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.add(l);
    }

    public void llkRemoveLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.remove(l);
    }

    ///////////////////////////////////////////////////////////////////////

    public final int LA0() throws LLKParseError {
        return llkInput.LA0();
    }

    public final int LA1() throws LLKParseError {
        return llkInput.LA1();
    }

    public final int LA(final int n) throws LLKParseError {
        return llkInput.LA(n);
    }

    public byte[] llkGetSource() {
        return llkInput.getSource();
    }

    /**
     * @return Mapped location (filename, line, column) for the given linear offset.
     */
    public ISourceLocation llkGetLocation(final int offset) {
        return llkInput.getLocation(offset);
    }

    public ISourceLocation llkGetLocation() {
        return llkInput.getLocation(llkGetOffset());
    }

    public final void llkConsume() {
        llkInput.consume();
    }

    public final void llkConsume(final int n) {
        llkInput.consume(n);
    }

    ////////////////////////////////////////////////////////////

    /**
     * Consume chars until one matches the given char
     */
    public final void llkConsumeUntil(final int c) throws LLKParseError {
        int la1 = LA1();
        if (la1 != ILLKConstants.LEXER_EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != c && la1 != ILLKConstants.LEXER_EOF) {
                llkInput.consume();
            }
        }
    }

    /**
     * Consume chars until one matches the given token type.
     */
    public final void llkConsumeUntil(final int[] a) throws LLKParseError {
        int la1 = LA1();
        if (la1 != ILLKConstants.LEXER_EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != ILLKConstants.LEXER_EOF) {
                for (int i = 0; i < a.length; ++i) {
                    if (la1 == a[i]) {
                        return;
                    }
                }
                llkInput.consume();
            }
        }
    }

    /**
     * Consume chars until one matches the given token type.
     */
    public final void llkConsumeUntil(final int[] bitset, final boolean inverted) throws LLKParseError {
        int la1 = LA1();
        if (la1 != ILLKConstants.LEXER_EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != ILLKConstants.LEXER_EOF) {
                if (inverted ? llkGetBitInverted(la1, bitset) : llkGetBit(la1, bitset)) {
                    return;
                }
                llkInput.consume();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    protected LLKParseException llkParseException(final String msg) {
        return llkParseException(msg, null, llkGetOffset());
    }

    protected LLKParseException llkParseException(final String msg, final int offset) {
        return llkParseException(msg, null, offset);
    }

    protected LLKParseException llkParseException(final String msg, final Throwable e, final int offset) {
        final int c = LA1();
        return new LLKParseException(
            msg + ": (" + c + ") '" + toChar(c) + "'", e, llkInput.getLocator().getLocation(offset));
    }

    protected char toChar(final int c) {
        if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
            return (char) c;
        }
        return '?';
    }

    ///////////////////////////////////////////////////////////////////////

    protected final boolean llkSynMark() {
        llkInput.mark();
        return true;
    }

    protected final boolean llkSynRewind(final boolean b) {
        llkInput.rewind();
        return b;
    }

    protected final boolean llkSynMatchOrRewind(final boolean b) {
        if (!b) {
            llkInput.rewind();
        } else {
            llkInput.unmark();
        }
        return b;
    }

    protected final boolean llkSynMatch(final byte c) throws LLKParseError {
        if (LA1() != c) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final byte c) throws LLKParseError {
        if (LA1() == c) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final byte c1, final byte c2) throws LLKParseError {
        final int c = LA1();
        if (c != c1 && c != c2) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final byte c1, final byte c2) throws LLKParseError {
        final int c = LA1();
        if (c == c1 || c == c2) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final byte c1, final byte c2, final byte c3) throws LLKParseError {
        final int c = LA1();
        if (c != c1 && c != c2 && c != c3) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final byte c1, final byte c2, final byte c3) throws LLKParseError {
        final int c = LA1();
        if (c == c1 || c == c2 || c == c3) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchRange(final byte first, final byte last) throws LLKParseError {
        final int c = LA1();
        if (c < first || c > last) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNotRange(final byte first, final byte last) throws LLKParseError {
        final int c = LA1();
        if (c >= first || c <= last) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final int[] bset) throws LLKParseError {
        if (llkGetBit(LA1(), bset)) {
            llkInput.consume();
            return true;
        }
        return false;
    }

    protected final boolean llkSynMatchNot(final int[] bset) throws LLKParseError {
        if (llkGetBitInverted(LA1(), bset)) {
            llkInput.consume();
            return true;
        }
        return false;
    }

    protected final boolean llkSynMatch(final String s) throws LLKParseError {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (LA1() != s.charAt(i)) {
                return false;
            }
            llkInput.consume();
        }
        return true;
    }

    protected final boolean llkSynMatch(final String s, final int start) throws LLKParseError {
        llkInput.consume(start);
        final int len = s.length();
        for (int i = start; i < len; i++) {
            if (LA1() != s.charAt(i)) {
                return false;
            }
            llkInput.consume();
        }
        return true;
    }

    protected final boolean llkSynMatch(final byte[] s) throws LLKParseError {
        final int len = s.length;
        for (int i = 0; i < len; i++) {
            if (LA1() != s[i]) {
                return false;
            }
            llkInput.consume();
        }
        return true;
    }

    protected final boolean llkSynMatch(final byte[] s, final int start) throws LLKParseError {
        llkInput.consume(start);
        final int len = s.length;
        for (int i = start; i < len; i++) {
            if (LA1() != s[i]) {
                return false;
            }
            llkInput.consume();
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////

    protected static boolean llkBitTest(final int lak, final int c1, final int c2, final int c3) {
        return (lak == c1 || lak == c2 || lak == c3);
    }

    protected static boolean llkBitTest(
        final int lak, final int c1, final int c2, final int c3, final int c4) {
        return (lak == c1 || lak == c2 || lak == c3 || lak == c4);
    }

    protected static boolean llkInvertedBitTest(final int lak, final int size, final int c1) {
        return (lak >= 0 && lak < size && lak != c1);
    }

    protected static boolean llkInvertedBitTest(final int lak, final int size, final int c1, final int c2) {
        return (lak >= 0 && lak < size && lak != c1 && lak != c2);
    }

    protected static boolean llkInvertedBitTest(
        final int lak, final int size, final int c1, final int c2, final int c3) {
        return (lak >= 0 && lak < size && lak != c1 && lak != c2 && lak != c3);
    }

    protected static boolean llkInvertedBitTest(
        final int lak, final int size, final int c1, final int c2, final int c3, final int c4) {
        return (lak >= 0 && lak < size && lak != c1 && lak != c2 && lak != c3 && lak != c4);
    }

    protected static boolean llkGetBit(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
    }

    protected static boolean llkGetBitInverted(int n, final int[] bset, final int vocab_size) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0
            && n < vocab_size
            && ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
    }

}
