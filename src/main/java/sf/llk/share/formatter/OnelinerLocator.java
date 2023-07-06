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
package sf.llk.share.formatter;

import sf.llk.share.support.ISourceLocation;
import sf.llk.share.support.ISourceLocator;
import sf.llk.share.support.LocatorFileInfo;
import sf.llk.share.support.SourceLocation;

public class OnelinerLocator implements ISourceLocator, Cloneable {

	////////////////////////////////////////////////////////////

	private static LocatorFileInfo fileInfo = new LocatorFileInfo(null, 1, 1, null);

	public OnelinerLocator() {
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	////////////////////////////////////////////////////////////

	public void reset(String filepath, Object data) {
	}

	public void toFile(String filepath, int linear, int line, Object data) {
	}

	public void toFile(String filepath, int linear, int line, Action action, Object data) {
	}

	public void newline(int offset) {
	}

	public void tab(int offset) {
	}

	public void setCharWidth(int offset, int width) {
	}

	public void mark() {
	}

	public void rewind() {
	}

	public void unmark() {
	}

	public void rewind(int offset) {
	}

	public void setTabWidth(int w) {
		throw new UnsupportedOperationException();
	}

	public int getTabWidth() {
		throw new UnsupportedOperationException();
	}

	public LocatorFileInfo getFileInfo() {
		return fileInfo;
	}

	public LocatorFileInfo getFileInfo(int linear) {
		return fileInfo;
	}

	public void setNestingEnabled(boolean b) {
	}

	public boolean getNestingEnabled() {
		throw new UnsupportedOperationException();
	}

	public int getNestLevel() {
		throw new UnsupportedOperationException();
	}

	public int getNestLevel(int offset) {
		throw new UnsupportedOperationException();
	}

	public int getLinear() {
		return 1;
	}

	public int getLinear(int offset) {
		return 1;
	}

	public int getOffset(int linear) {
		throw new UnsupportedOperationException();
	}

	public ISourceLocation getLocation(int offset) {
		ISourceLocation ret = new SourceLocation();
		getLocation(ret, offset);
		return ret;
	}

	public void getLocation(ISourceLocation loc, int offset) {
		loc.setLocation(null, offset, 1, 1);
	}

	////////////////////////////////////////////////////////////
}
