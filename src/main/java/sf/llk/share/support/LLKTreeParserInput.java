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
public class LLKTreeParserInput implements ILLKTreeParserInput, Cloneable {

    ////////////////////////////////////////////////////////////

    private static final String NAME = "LLKTreeParserInput";
    private static final boolean CHECK = true;
    private static final int MARKER_SIZE = 1;

    ////////////////////////////////////////////////////////////

    private final ILLKMain main;
    private int markOffset;
    private ILLKNode[] markLocations;

    ////////////////////////////////////////////////////////////

    public LLKTreeParserInput(final ILLKMain main) {
        this.main = main;
        markLocations = new ILLKNode[MARKER_SIZE * 32];
        reset();
    }

    /**
     * NOTE: ILLKMain main is not cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final LLKTreeParserInput ret = (LLKTreeParserInput) super.clone();
        ret.markLocations = markLocations.clone();
        return ret;
    }

    @Override
    public void reset() {
        markOffset = 0;
    }

    @Override
    public final void mark(final ILLKNode current) {
        if (markOffset >= markLocations.length) {
            expandMarkLocations();
        }
        markLocations[markOffset++] = current;
    }

    @Override
    public final ILLKNode rewind() {
        return markLocations[--markOffset];
    }

    @Override
    public final ILLKMain getMain() {
        return main;
    }

    @Override
    public final ISourceLocator getLocator() {
        return main.getLocator();
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

    ////////////////////////////////////////////////////////////

    private void expandMarkLocations() {
        final ILLKNode[] ret = new ILLKNode[markLocations.length * 2];
        System.arraycopy(markLocations, 0, ret, 0, markOffset);
        markLocations = ret;
        if (CHECK && MARKER_SIZE > 1 && (ret.length % MARKER_SIZE) != 0) {
            throw new LLKParseError(NAME + ".expandMarkLocations(): assert (ret.length%MARKER_SIZE)==0");
        }
    }

    ////////////////////////////////////////////////////////////
}
