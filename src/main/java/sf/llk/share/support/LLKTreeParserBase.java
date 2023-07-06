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
import java.util.Stack;

public abstract class LLKTreeParserBase {

    public abstract String llkGetTokenName(int type);

    protected abstract boolean llkGetBitInverted(int n, int[] bset);

    /*
     * The software in this package is distributed under the GNU General Public
     * License version 2, as published by the Free Software Foundation, but with
     * the Classpath exception.  You should have received a copy of the GNU General
     * Public License (GPL) and the Classpath exception along with this program.
     */

    ///////////////////////////////////////////////////////////////////////

    protected ILLKTreeParserInput llkInput;
    protected ILLKMain llkMain;
    protected ILLKTreeBuilder llkTree;
    protected ILLKNode LT1;
    protected ILLKNode llkParent;
    protected final Stack<ILLKNode> llkStack = new Stack<>();
    protected final List<ILLKLifeCycleListener> llkLifeCycleListeners = new ArrayList<>(4);

    ///////////////////////////////////////////////////////////////////////

    public ILLKTreeParserInput llkGetInput() {
        return llkInput;
    }

    public void llkAddLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.add(l);
    }

    public void llkRemoveLifeCycleListener(final ILLKLifeCycleListener l) {
        llkLifeCycleListeners.remove(l);
    }

    ///////////////////////////////////////////////////////////////////////

    /**
     * @return The next token ahead, null if there is none.
     */
    public final ILLKNode LT1() {
        return LT1;
    }

    /**
     * @return The nth tokens ahead where 1th is the next token ahead.
     */
    public final ILLKNode LT(int n) {
        if (n <= 0) {
            llkError("ASSERT(n>0): n=" + n);
            return null;
        }
        ILLKNode t = LT1;
        for (; n > 1 && t != null; --n) {
            t = t.getNext();
        }
        return t;
    }

    /**
     * @return Token type of the next token ahead, EOF if next token is null.
     */
    public final int LA1() {
        return LT1 == null ? ILLKTreeParserInput.EOF : LT1.getType();
    }

    /**
     * @return Token type of the next token ahead, EOF if next token is null.
     */
    public final int LA(int n) {
        if (n <= 0) {
            llkError("ASSERT(n>0): n=" + n);
            return ILLKTreeParserInput.EOF;
        }
        ILLKNode t = LT1;
        for (; n > 1 && t != null; --n) {
            t = t.getNext();
        }
        return (t == null) ? ILLKTreeParserInput.EOF : t.getType();
    }

    ////////////////////////////////////////////////////////////

    /**
     * @return The parent node for LT1, null if none.
     */
    public final ILLKNode llkGetParent() {
        return llkParent;
    }

    ////////////////////////////////////////////////////////////

    /**
     * Consume chars until one matches the given char
     */
    public final ILLKNode llkConsumeUntil(final int type) throws LLKParseError {
        if (LT1 != null) {
            LT1 = LT1.getNext();
        }
        while (LT1 != null && LT1.getType() != type) {
            LT1 = LT1.getNext();
        }
        return LT1;
    }

    /**
     * Consume chars until one matches the given token type.
     */
    public final ILLKNode llkConsumeUntil(final int[] a) throws LLKParseError {
        if (LT1 != null) {
            LT1 = LT1.getNext();
        }
        int la1;
        while (LT1 != null) {
            la1 = LT1.getType();
            for (int i = 0; i < a.length; ++i) {
                if (la1 == a[i]) {
                    return LT1;
                }
            }
            LT1 = LT1.getNext();
        }
        return LT1;
    }

    /**
     * Consume chars until one matches the given token type.
     */
    public final ILLKNode llkConsumeUntil(final int[] bitset, final boolean inverted) throws LLKParseError {
        if (LT1 != null) {
            LT1 = LT1.getNext();
        }
        int la1;
        while (LT1 != null) {
            la1 = LT1.getType();
            if (inverted ? llkGetBitInverted(la1, bitset) : llkGetBit(la1, bitset)) {
                return LT1;
            }
            LT1 = LT1.getNext();
        }
        return LT1;
    }

    ////////////////////////////////////////////////////////////

    protected ISourceLocation llkMapLocation(final ILLKNode n) {
        return (n == null ? null : llkInput.getLocator().getLocation(n.getOffset()));
    }

    protected void llkError(final String msg) {
        ILLKNode lt1 = LT1;
        if (lt1 == null && !llkStack.empty()) {
            lt1 = llkStack.peek();
        }
        llkMain.error(msg, lt1 == null ? -1 : lt1.getOffset());
    }

    protected LLKParseException llkParseException(final String msg, final ILLKNode lt1) {
        final ILLKNode p = (llkStack.empty() ? null : llkStack.peek());
        final ILLKNode loc = (lt1 != null ? lt1 : (p != null) ? p : null);
        return new LLKParseException(msg + ":\n\t" + lt1 + "\n\tparent=" + p, llkMapLocation(loc));
    }

    protected LLKParseException llkExtraChildrenException(final ILLKNode child, final ILLKNode parent) {
        return new LLKParseException(
            "Extra children not matched:\n\textra child=" + child + "\n\tparent=" + parent,
            llkMapLocation(child));
    }

    protected LLKParseException llkMismatchException(final String msg, final int actual, final ILLKNode lt1) {
        return llkParseException(msg + ", actual='" + llkGetTokenName(actual) + '\'', lt1);
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int[] bset, final int actual, final ILLKNode lt1) {
        return llkParseException(msg + llkToString(bset) + ", actual=" + llkToString(actual), lt1);
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int type, final int actual, final ILLKNode lt1) {
        return llkParseException(
            msg + '\'' + llkToString(type) + "', actual='" + llkToString(actual) + '\'', lt1);
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int actual, final ILLKNode lt1) {
        return llkParseException(
            msg + "(" + llkToString(c1) + ", " + llkToString(c2) + "), actual=" + llkToString(actual), lt1);
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int c1, final int c2, final int c3, final int actual, final ILLKNode lt1) {
        return llkParseException(
            msg
                + "("
                + llkToString(c1)
                + ", "
                + llkToString(c2)
                + ", "
                + llkToString(c3)
                + "), actual="
                + llkToString(actual),
            lt1
        );
    }

    ////////////////////////////////////////////////////////////

    public final void llkMatch(final ILLKNode lt1, final int type) throws LLKParseException {
        final int la1 = lt1.getType();
        if (la1 != type) {
            throw llkMismatchException("match(int): expected=" + llkGetTokenName(type), la1, lt1);
        }
    }

    public final void llkMatch(final int type) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 != type) {
            throw llkMismatchException("match(int): expected=", type, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatchNot(final int type) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 == type) {
            throw llkMismatchException("matchNot(int): not expected=", type, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatch(final int type1, final int type2) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 != type1 && la1 != type2) {
            throw llkMismatchException("match(int, int): expected=", type1, type2, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatchNot(final int type1, final int type2) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 == type1 || la1 == type2) {
            throw llkMismatchException("matchNot(int, int): not expected=", type1, type2, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatch(final int type1, final int type2, final int type3) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 != type1 && la1 != type2 && la1 != type3) {
            throw llkMismatchException("match(int, int, int): expected=", type1, type2, type3, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatchNot(final int type1, final int type2, final int type3) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 == type1 || la1 == type2 || la1 == type3) {
            throw llkMismatchException(
                "matchNot(int, int, int): not expected=", type1, type2, type3, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatchRange(final int first, final int last) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 < first || la1 > last) {
            throw llkMismatchException("matchRange(int, int): range=", first, last, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatchNotRange(final int first, final int last) throws LLKParseException {
        final int la1 = LT1.getType();
        if (la1 >= first && la1 <= last) {
            throw llkMismatchException("matchNotRange(int, int): range=", first, last, la1, LT1);
        }
        LT1 = LT1.getNext();
    }

    public final void llkMatch(final int[] bset) throws LLKParseException {
        final int la1 = LT1.getType();
        if (llkGetBit(la1, bset)) {
            LT1 = LT1.getNext();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset, la1, LT1);
        }
    }

    public final void llkMatchNot(final int[] bset) throws LLKParseException {
        final int la1 = LT1.getType();
        if (llkGetBitInverted(la1, bset)) {
            LT1 = LT1.getNext();
        } else {
            throw llkMismatchException("match(BitSet): expected=", bset, la1, LT1);
        }
    }

    ////////////////////////////////////////////////////////////

    public final boolean llkSynMatch(final int type) {
        if (LT1.getType() != type) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatchNot(final int type) {
        if (LT1.getType() == type) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatch(final int type1, final int type2) {
        final int la1 = LT1.getType();
        if (la1 != type1 && la1 != type2) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatchNot(final int type1, final int type2) {
        final int la1 = LT1.getType();
        if (la1 == type1 || la1 == type2) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatch(final int type1, final int type2, final int type3) {
        final int la1 = LT1.getType();
        if (la1 != type1 && la1 != type2 && la1 != type3) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatchNot(final int type1, final int type2, final int type3) {
        final int la1 = LT1.getType();
        if (la1 == type1 || la1 == type2 || la1 == type3) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatchRange(final int first, final int last) {
        final int la1 = LT1.getType();
        if (la1 < first || la1 > last) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatchNotRange(final int first, final int last) {
        final int la1 = LT1.getType();
        if (la1 >= first && la1 <= last) {
            return false;
        }
        LT1 = LT1.getNext();
        return true;
    }

    public final boolean llkSynMatch(final int[] bset) {
        final int la1 = LT1.getType();
        if (llkGetBit(la1, bset)) {
            LT1 = LT1.getNext();
            return true;
        }
        return false;
    }

    public final boolean llkSynMatchNot(final int[] bset) {
        final int la1 = LT1.getType();
        if (llkGetBitInverted(la1, bset)) {
            LT1 = LT1.getNext();
            return true;
        }
        return false;
    }

    protected final boolean llkSynMark() {
        llkInput.mark(LT1);
        return true;
    }

    protected final boolean llkSynRewind(final boolean b) {
        LT1 = llkInput.rewind();
        return b;
    }

    protected final boolean llkSynMatchOrRewind(final boolean b) {
        if (!b) {
            LT1 = llkInput.rewind();
        }
        return b;
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

}
