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

/* Placeholder, doing nothing. */
public class NullLocator implements ISourceLocator, Cloneable {

    ////////////////////////////////////////////////////////////

    public NullLocator() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void reset(final String filepath, final Object data) {
    }

    /**
     * @param line The 1-based line number in the given file that corresponding to the given linear line.
     */
    @Override
    public void toFile(final String filepath, final int linear, final int line, final Object data) {
    }

    @Override
    public void toFile(
        final String filepath, final int linear, final int line, final Action action, final Object data) {
    }

    @Override
    public void newline(final int offset) {
    }

    @Override
    public void tab(final int offset) {
    }

    @Override
    public void setCharWidth(final int offset, final int width) {
    }

    @Override
    public void mark() {
    }

    @Override
    public void rewind() {
    }

    @Override
    public void unmark() {
    }

    @Override
    public void rewind(final int offset) {
    }

    @Override
    public void setTabWidth(final int w) {
    }

    @Override
    public ISourceLocation getLocation(final int offset) {
        return new SourceLocation(offset, -1, -1);
    }

    @Override
    public int getTabWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocatorFileInfo getFileInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocatorFileInfo getFileInfo(final int linear) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNestingEnabled(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getNestingEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNestLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNestLevel(final int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLinear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLinear(final int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOffset(final int linear) {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////
}
