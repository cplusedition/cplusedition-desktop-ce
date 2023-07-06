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

import java.io.Serializable;

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public class SourceLocation implements ISourceLocation, Cloneable, Serializable {

    private static final long serialVersionUID = 8647570819206912050L;

    public String filename;
    public int offset, line, column;

    public SourceLocation() {
    }

    public SourceLocation(final ISourceLocation loc) {
        filename = loc.getFilename();
        offset = loc.getOffset();
        line = loc.getLine();
        column = loc.getColumn();
    }

    public SourceLocation(final String filename, final int offset, final int line, final int column) {
        this.filename = filename;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    public SourceLocation(final int offset, final int line, final int column) {
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLocation(final String filename, final int offset, final int line, final int column) {
        this.filename = filename;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    @Override
    public void setLocation(final ISourceLocation loc) {
        filename = loc.getFilename();
        offset = loc.getOffset();
        line = loc.getLine();
        column = loc.getColumn();
    }

    @Override
    public final String getFilename() {
        return filename;
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final int getLine() {
        return line;
    }

    @Override
    public final int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return filename
            + (line < 0 ? "(???" : "(" + line)
            + (column < 0 ? ",???" : "," + column)
            + (offset < 0 ? "@???" : "@" + offset)
            + ")";
    }
}
