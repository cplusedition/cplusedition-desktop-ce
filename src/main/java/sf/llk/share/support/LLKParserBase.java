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

import java.util.ArrayList;
import java.util.List;

public abstract class LLKParserBase {

    public abstract String llkGetTokenName(int type);

    protected abstract boolean llkGetBitInverted(int n, int[] bset);

    protected ILLKTreeBuilder llkTree;

    public ILLKTreeBuilder llkGetTreeBuilder() {
        return llkTree;
    }

    public void llkSetTreeBuilder(final ILLKTreeBuilder builder) {
        llkTree = builder;
    }

    ///////////////////////////////////////////////////////////////////////

    protected ILLKMain llkMain;
    protected ILLKParserInput llkInput;
    protected final List<ILLKLifeCycleListener> llkLifeCycleListeners = new ArrayList<>(4);

    ///////////////////////////////////////////////////////////////////////

    public ILLKMain llkGetMain() {
        return llkMain;
    }

    public ILLKParserInput llkGetInput() {
        return llkInput;
    }

    public void llkAddLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.add(l);
    }

    public void llkRemoveLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.remove(l);
    }

    ///////////////////////////////////////////////////////////////////////

    public final int LA0() {
        return llkInput.LA0();
    }

    public final int LA1() {
        return llkInput.LA1();
    }

    public final int LA(final int n) {
        return llkInput.LA(n);
    }

    public final ILLKToken LT0() {
        return llkInput.LT0();
    }

    public final ILLKToken LT1() {
        return llkInput.LT1();
    }

    public final ILLKToken LT(final int n) {
        return llkInput.LT(n);
    }

    public final void llkConsume() {
        llkInput.consume();
    }

    public final void llkConsume(final int n) {
        llkInput.consume(n);
    }

    /**
     * Consume chars until one matches the given char
     */
    public final void llkConsumeUntil(final int c) throws LLKParseError {
        int la1 = LA1();
        if (la1 != ILLKParserInput.EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != c && la1 != ILLKParserInput.EOF) {
                llkInput.consume();
            }
        }
    }

    /**
     * Consume chars until one matches the given token type.
     */
    public final void llkConsumeUntil(final int[] a) throws LLKParseError {
        int la1 = LA1();
        if (la1 != ILLKParserInput.EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != ILLKParserInput.EOF) {
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
        if (la1 != ILLKParserInput.EOF) {
            llkInput.consume();
            while ((la1 = LA1()) != ILLKParserInput.EOF) {
                if (inverted ? llkGetBitInverted(la1, bitset) : llkGetBit(la1, bitset)) {
                    return;
                }
                llkInput.consume();
            }
        }
    }

    public ISourceLocation llkGetLocation(final int offset) {
        return llkInput.getLocator().getLocation(offset);
    }

    ////////////////////////////////////////////////////////////

    public final void getKeywordContexts() {
        llkInput.getKeywordContexts();
    }

    public final void setKeywordContexts(final int context) {
        llkInput.setKeywordContexts(context);
    }

    public final void llkSetContext(final int context, final int state) {
        llkInput.setContext(context, state);
    }

    public CharSequence llkGetSource() {
        return llkInput.getSource();
    }

    public CharSequence llkGetSource(final ILLKToken t) {
        return llkInput.getSource(t.getOffset(), t.getEndOffset());
    }

    public CharSequence llkGetSource(final int start, final int end) {
        return llkInput.getSource(start, end);
    }

    ////////////////////////////////////////////////////////////

    protected LLKParseException llkParseException(final String msg) {
        return llkParseException(msg, null, LT1());
    }

    protected LLKParseException llkParseException(final String msg, final Throwable e, final ILLKToken t) {
        final int type = t.getType();
        final int offset = t.getOffset();
        return new LLKParseException(
            msg
                + ": "
                + t.getClass().getName()
                + "(type="
                + type
                + ", name="
                + llkGetTokenName(type)
                + ", start="
                + offset
                + ", text="
                + t.getText(),
            e,
            llkGetLocation(offset)
        );
    }

    protected LLKParseException llkMismatchException(final String msg, final int[] bset, final int actual) {
        return new LLKParseException(
            msg + llkToString(bset) + ", actual=" + llkToString(actual), llkGetLocation(LT1().getOffset()));
    }

    protected LLKParseException llkMismatchException(final String msg, final int expected, final int actual) {
        return new LLKParseException(
            msg + llkToString(expected) + ", actual=" + llkToString(actual),
            llkGetLocation(LT1().getOffset()));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int actual) {
        return new LLKParseException(
            msg + "(" + llkToString(c1) + ", " + llkToString(c2) + "), actual=" + llkToString(actual),
            llkGetLocation(LT1().getOffset()));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int c3, final int actual) {
        return new LLKParseException(
            msg
                + "("
                + llkToString(c1)
                + ", "
                + llkToString(c2)
                + ", "
                + llkToString(c3)
                + "), actual="
                + llkToString(actual),
            llkGetLocation(LT1().getOffset())
        );
    }

    ////////////////////////////////////////////////////////////

    protected final String llkToString(final int type) {
        return llkGetTokenName(type);
    }

    protected final String llkToString(final int[] bset) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bset.length; ++i) {
            if (i != 0) {
                buf.append(", 0x");
            } else {
                buf.append("0x");
            }
            buf.append(Integer.toHexString(bset[i]));
        }
        return buf.toString();
    }

    protected final void llkMatch(final int type) throws LLKParseException, LLKParseError {
        if (LA1() != type) {
            throw llkMismatchException("match(int): expected=", type, LA1());
        }
        llkInput.consume();
    }

    protected final void llkMatchNot(final int type) throws LLKParseException, LLKParseError {
        if (LA1() == type) {
            throw llkMismatchException("matchNot(int): not expected=", type, LA1());
        }
        llkInput.consume();
    }

    protected final void llkMatch(final int type1, final int type2) throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 != type1 && la1 != type2) {
            throw llkMismatchException("match(int, int): expected=", type1, type2, la1);
        }
        llkInput.consume();
    }

    protected final void llkMatchNot(final int type1, final int type2) throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 == type1 || la1 == type2) {
            throw llkMismatchException("matchNot(int, int): not expected=", type1, type2, LA1());
        }
        llkInput.consume();
    }

    protected final void llkMatch(final int type1, final int type2, final int type3)
        throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 != type1 && la1 != type2 && la1 != type3) {
            throw llkMismatchException("match(int, int, int): expected=", type1, type2, type3, la1);
        }
        llkInput.consume();
    }

    protected final void llkMatchNot(final int type1, final int type2, final int type3)
        throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 == type1 || la1 == type2 || la1 == type3) {
            throw llkMismatchException(
                "matchNot(int, int, int): not expected=", type1, type2, type3, LA1());
        }
        llkInput.consume();
    }

    protected final void llkMatchRange(final int first, final int last) throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 < first || la1 > last) {
            throw llkMismatchException("matchRange(int, int): range=", first, last, la1);
        }
        llkInput.consume();
    }

    protected final void llkMatchNotRange(final int first, final int last) throws LLKParseException, LLKParseError {
        final int la1 = LA1();
        if (la1 >= first && la1 <= last) {
            throw llkMismatchException("matchNotRange(int, int): range=", first, last, la1);
        }
        llkInput.consume();
    }

    protected final void llkMatch(final int[] bset) throws LLKParseException, LLKParseError {
        if (llkGetBit(LA1(), bset)) {
            llkInput.consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset, LA1());
        }
    }

    protected final void llkMatchNot(final int[] bset) throws LLKParseException, LLKParseError {
        if (llkGetBitInverted(LA1(), bset)) {
            llkInput.consume();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset, LA1());
        }
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

    protected final boolean llkSynRewind(final boolean mark, final boolean b) {
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

    protected final boolean llkSynMatch(final int type) {
        if (LA1() != type) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final int type) {
        if (LA1() == type) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final int type1, final int type2) {
        final int la1 = LA1();
        if (la1 != type1 && la1 != type2) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final int type1, final int type2) {
        final int la1 = LA1();
        if (la1 == type1 || la1 == type2) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final int type1, final int type2, final int type3) {
        final int la1 = LA1();
        if (la1 != type1 && la1 != type2 && la1 != type3) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNot(final int type1, final int type2, final int type3) {
        final int la1 = LA1();
        if (la1 == type1 || la1 == type2 || la1 == type3) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchRange(final int first, final int last) {
        final int c = LA1();
        if (c < first || c > last) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatchNotRange(final int first, final int last) {
        final int c = LA1();
        if (c >= first || c <= last) {
            return false;
        }
        llkInput.consume();
        return true;
    }

    protected final boolean llkSynMatch(final int[] bset) {
        if (llkGetBit(LA1(), bset)) {
            llkInput.consume();
            return true;
        }
        return false;
    }

    protected final boolean llkSynMatchNot(final int[] bset) {
        if (llkGetBitInverted(LA1(), bset)) {
            llkInput.consume();
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////

}
