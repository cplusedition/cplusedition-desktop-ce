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

public class LLKParseException extends LLKException implements ILLKParseException {

    ////////////////////////////////////////////////////////////

    private static final long serialVersionUID = 9143525513702388664L;

    protected String id;
    protected ISourceLocation location;

    ////////////////////////////////////////////////////////////

    public LLKParseException() {
    }

    public LLKParseException(final String msg) {
        super(msg);
    }

    public LLKParseException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public LLKParseException(final String msg, final ISourceLocation loc) {
        super(msg);
        setLocation(loc);
    }

    public LLKParseException(final String msg, final Throwable cause, final ISourceLocation loc) {
        super(msg, cause);
        setLocation(loc);
    }

    public LLKParseException(final String id, final String msg, final Throwable cause, final ISourceLocation loc) {
        super(id + ": " + msg, cause);
        this.id = id;
        setLocation(loc);
    }

    private void setLocation(final ISourceLocation loc) {
        if (loc != null) {
            location = new SourceLocation(loc);
        }
    }

    ////////////////////////////////////////////////////////////

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return ((location != null) ? location.toString() + ": " : "(?,?): ") + getMessage();
    }

    ////////////////////////////////////////////////////////////
}
