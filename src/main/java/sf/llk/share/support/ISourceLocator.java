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

public interface ISourceLocator extends IReadOnlyLocator {

    public static enum Action {
        SAME, ENTER, RETURN, SYS, CSYS, LEVEL5, LEVEL6, LEVEL7, LEVEL8, LLKRETURN
    }

    ////////////////////////////////////////////////////////////

    void reset(String filepath, Object data);

    /**
     * Update locator file information.
     *
     * @param line The 1-based line number in the given file that corresponding to the given linear line.
     */
    void toFile(String filepath, int linear, int line, Object data);

    void toFile(String filepath, int linear, int line, Action action, Object data);

    /**
     * Update locator line information.
     *
     * @param offset 0-based offset of first character of a new line (ie. after line break).
     */
    void newline(int offset);

    /**
     * Update locator tab information.
     *
     * @param offset 0-based offset of the tab character.
     */
    void tab(int offset);

    void setTabWidth(int width);

    void setCharWidth(int offset, int width);

    void mark();

    void unmark();

    void rewind();

    void rewind(int offset);

    ////////////////////////////////////////////////////////////
}
