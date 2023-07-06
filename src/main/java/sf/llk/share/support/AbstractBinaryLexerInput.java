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

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public abstract class AbstractBinaryLexerInput implements ILLKBinaryLexerInput, Cloneable {

    ////////////////////////////////////////////////////////////

    protected ILLKMain main;
    protected ISourceLocator locator;
    protected int offset;

    ////////////////////////////////////////////////////////////

    public AbstractBinaryLexerInput(final ILLKMain main) {
        this.main = main;
        locator = main.getLocator();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractBinaryLexerInput ret = (AbstractBinaryLexerInput) super.clone();
        ret.main = (ILLKMain) main.clone();
        ret.locator = ret.main.getLocator();
        return ret;
    }

    ////////////////////////////////////////////////////////////

    @Override
    public ILLKMain getMain() {
        return main;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public ISourceLocator getLocator() {
        return locator;
    }

    @Override
    public ISourceLocation getLocation() {
        return locator.getLocation(offset);
    }

    @Override
    public ISourceLocation getLocation(final int offset) {
        return locator.getLocation(offset);
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void setCutoff() {
    }

    @Override
    public void setCutoff(final int offset) {
    }

    ////////////////////////////////////////////////////////////

    protected LLKParseException llkMismatchException(final String msg) {
        return new LLKParseException(msg, locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(final String msg, final int[] bset) {
        return new LLKParseException(msg + _toString(bset) + ", actual=" + LA1(), locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(final String msg, final int expected) {
        return new LLKParseException(
            msg + _toString(expected) + ", actual=" + _toString(LA1()), locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int first, final int last, final int c) {
        return new LLKParseException(
            msg + "(" + _toString(first) + ", " + _toString(last) + "), actual=" + _toString(c),
            locator.getLocation(offset));
    }

    protected LLKParseException llkMismatchException(
        final String msg, final int expected1, final int expected2, final int expected3, final int c) {
        return new LLKParseException(
            msg
                + "("
                + _toString(expected1)
                + ", "
                + _toString(expected2)
                + ", "
                + _toString(expected3)
                + "), actual="
                + _toString(c),
            locator.getLocation(offset)
        );
    }

    protected final String _toString(final int[] bset) {
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

    protected static boolean llkGetBit(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
    }

    /**
     * @return true if n is a set bit in the bitset that is invert of the given bitset.
     */
    protected static boolean llkGetBitInverted(int n, final int[] bset) {
        final int mask = (n & ILLKConstants.MODMASK);
        return n >= 0 && ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
    }

    protected static String _toString(final byte[] a) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; ++i) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(String.format("0x%02x", a[i] & 0xff));
        }
        return b.toString();
    }

    protected static String _toString(final int a) {
        if (a == -1) {
            return ("EOF");
        }
        return String.format("0x%02x", a);
    }

    ////////////////////////////////////////////////////////////
}
