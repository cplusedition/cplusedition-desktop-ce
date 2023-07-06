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

import java.util.Vector;

public class SourceLocator implements ISourceLocator, Cloneable {

    ////////////////////////////////////////////////////////////

    private int tabWidth;
    private IntList lineOffsets;
    private IntList tabOffsets;
    private IntList columnOffsets;
    private IntList columnWidths;
    private IntList fileStarts;
    private IntList levelLines;
    private IntList levels;
    private Vector<LocatorFileInfo> fileInfos;
    private IntList markStack;
    private boolean enableNesting;

    ////////////////////////////////////////////////////////////

    public SourceLocator(int tabwidth) {
        this.tabWidth = tabwidth;
        this.lineOffsets = new IntList(1024);
        this.tabOffsets = new IntList(1024);
        this.columnOffsets = new IntList(32);
        this.columnWidths = new IntList(32);
        this.fileStarts = new IntList(32);
        this.levelLines = new IntList(32);
        this.levels = new IntList(32);
        this.fileInfos = new Vector<>(32);
        this.markStack = new IntList(32);
        enableNesting = true;
        lineOffsets.add(0);
        levelLines.add(1);
        levels.add(1);
        toFile(null, 1, 1, null);
    }

    public Object clone() throws CloneNotSupportedException {
        SourceLocator ret = (SourceLocator) super.clone();
        ret.lineOffsets = (IntList) lineOffsets.clone();
        ret.tabOffsets = (IntList) tabOffsets.clone();
        ret.columnOffsets = (IntList) columnOffsets.clone();
        ret.columnWidths = (IntList) columnWidths.clone();
        ret.fileStarts = (IntList) fileStarts.clone();
        ret.levelLines = (IntList) levelLines.clone();
        ret.levels = (IntList) levels.clone();
        ret.fileInfos = new Vector<>(fileInfos);
        ret.markStack = (IntList) markStack.clone();
        return ret;
    }

    ////////////////////////////////////////////////////////////

    public void reset(String filepath, Object data) {
        lineOffsets.clear();
        tabOffsets.clear();
        columnOffsets.clear();
        columnWidths.clear();
        fileStarts.clear();
        fileInfos.clear();
        markStack.clear();
        lineOffsets.add(0);
        toFile(filepath, 1, 1, data);
        enableNesting = true;
    }

    /**
     * @param line The 1-based line number in the given file that corresponding to the given linear line.
     */
    public void toFile(String filepath, int linear, int line, Object data) {
        toFile(filepath, linear, line, null, data);
    }

    public void toFile(String filepath, int linear, int line, Action action, Object data) {
        if (filepath == null && fileInfos.size() > 0)
            filepath = getFileInfo().path;
        fileStarts.add(linear);
        fileInfos.add(new LocatorFileInfo(filepath, linear, line, data));
        if (action == null || action == ISourceLocator.Action.SAME)
            return;
        levelLines.add(linear);
        int level = levels.peek();
        level = (action == ISourceLocator.Action.ENTER) ? ++level : --level;
        levels.add(level);
    }

    public void newline(int offset) {
        lineOffsets.add(offset);
    }

    public void tab(int offset) {
        tabOffsets.add(offset);
    }

    public void setCharWidth(int offset, int width) {
        columnOffsets.add(offset);
        columnWidths.add(width);
    }

    public void mark() {
        markStack.push(columnWidths.size());
        markStack.push(columnOffsets.size());
        markStack.push(lineOffsets.size());
        markStack.push(tabOffsets.size());
        markStack.push(fileStarts.size());
    }

    public void rewind() {
        int size = markStack.pop();
        fileInfos.setSize(size);
        fileStarts.setLength(size);
        tabOffsets.setLength(markStack.pop());
        lineOffsets.setLength(markStack.pop());
        columnOffsets.setLength(markStack.pop());
        columnWidths.setLength(markStack.pop());
    }

    public void unmark() {
        markStack.pop(5);
    }

    public void rewind(int offset) {
        if (offset < 0)
            throw new LLKParseError("ASSERT: offset >= 0: offset=" + offset);
        int line = lineOffsets.insertionIndex(offset);
        lineOffsets.setLength(line);
        tabOffsets.setLength(tabOffsets.insertionIndex(offset));
        int index = fileStarts.insertionIndex(line);
        fileStarts.setLength(index);
        fileInfos.setSize(index);
        index = columnOffsets.insertionIndex(offset);
        columnOffsets.setLength(index);
        columnWidths.setLength(index);
    }

    public void setTabWidth(int w) {
        this.tabWidth = w;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public LocatorFileInfo getFileInfo() {
        return fileInfos.lastElement();
    }

    public LocatorFileInfo getFileInfo(int linear) {
        if (linear < 0) return null;
        int index = fileStarts.insertionIndex(linear);
        return fileInfos.get(index - 1);
    }

    public void setNestingEnabled(boolean b) {
        enableNesting = b;
    }

    public boolean getNestingEnabled() {
        return enableNesting;
    }

    public int getNestLevel() {
        if (!enableNesting)
            return 1;
        return levels.peek();
    }

    public int getNestLevel(int offset) {
        if (!enableNesting)
            return 1;
        int index = levelLines.insertionIndex(getLinear(offset));
        return levels.get(index - 1);
    }

    public int getLinear() {
        return lineOffsets.size();
    }

    public int getLinear(int offset) {
        if (offset < 0)
            throw new LLKParseError("ASSERT: offset >= 0: offset=" + offset);
        return lineOffsets.insertionIndex(offset);
    }

    public int getOffset(int linear) {
        return lineOffsets.get(linear - 1);
    }

    public ISourceLocation getLocation(int offset) {
        ISourceLocation ret = new SourceLocation();
        getLocation(ret, offset);
        return ret;
    }

    private void getLocation(ISourceLocation loc, int offset) {
        int linear = offset < 0 ? -1 : getLinear(offset);
        LocatorFileInfo info = getFileInfo(linear);
        if (info == null) {
            loc.setLocation("", -1, -1, -1);
        } else {
            loc.setLocation(
                info.getFilepath(),
                offset,
                linear - info.getLinear() + info.getLine(),
                getColumn(offset, linear));
        }
    }

    ////////////////////////////////////////////////////////////

    /**
     * @return 1-based column number for the given offset.
     */
    private int getColumn(int offset, int linear) {
        if (offset < 0)
            throw new LLKParseError("ASSERT: offset >= 0: offset=" + offset);
        int p = getOffset(linear);
        int col = 1;
        int tabsize = tabOffsets.size();
        int tabindex = tabOffsets.binarySearch(p);
        if (tabindex < 0)
            tabindex = -tabindex - 1;
        int taboffset = (tabindex < tabsize ? tabOffsets.get(tabindex) : -1);
        int columnsize = columnOffsets.size();
        int columnindex = columnOffsets.binarySearch(p);
        if (columnindex < 0)
            columnindex = -columnindex - 1;
        int columnoffset = (columnindex < columnsize ? columnOffsets.get(columnindex) : -1);
        for (; p < offset; ++p) {
            if (p == columnoffset) {
                col += columnWidths.get(columnindex);
                ++columnindex;
                columnoffset = (columnindex < columnsize ? columnOffsets.get(columnindex) : -1);
                continue;
            } else if (p == taboffset) {
                col = col - ((col - 1) % tabWidth) + tabWidth;
                ++tabindex;
                taboffset = (tabindex < tabsize ? tabOffsets.get(tabindex) : -1);
                continue;
            }
            ++col;
        }
        return col;
    }

    ////////////////////////////////////////////////////////////
}
