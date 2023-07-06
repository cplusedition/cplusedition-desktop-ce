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

public interface IReadOnlyLocator {

    ////////////////////////////////////////////////////////////

    int getTabWidth();

    /**
     * @return The path of the current file.
     */
    LocatorFileInfo getFileInfo();

    /**
     * @return Path of file that contains the given linear line (1-based).
     */
    LocatorFileInfo getFileInfo(int linear);

    boolean getNestingEnabled();

    void setNestingEnabled(boolean b);

    /**
     * @return The current nested file level, 1 for top level file.
     */
    int getNestLevel();

    /**
     * @return The nested file level at the given offset, 1 for top level file.
     */
    int getNestLevel(int offset);

    /**
     * @return The current linear line number (1-based).
     */
    int getLinear();

    /**
     * @return The linear line number (1-based) for the given offset (0-based).
     */
    int getLinear(int offset);

    /**
     * @return Linear offset (0-based) of the given linear line number (1-based).
     */
    int getOffset(int linear);

    /**
     * @return The mapped location (filepath, offset, line and column) for the given linear offset.
     */
    ISourceLocation getLocation(int offset);

    Object clone() throws CloneNotSupportedException;

    ////////////////////////////////////////////////////////////
}
