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

public class IntRange implements IIntSequence {

	int[] array;
	int start;
	int end;

	//////////////////////////////////////////////////////////////////////

	public IntRange(int[] a) {
		this.array = a;
		this.start = 0;
		this.end = a.length;
	}

	public IntRange(int[] a, int start, int end) {
		this.array = a;
		this.start = start;
		this.end = end;
	}

	public int size() {
		return end - start;
	}

	public boolean isEmpty() {
		return end - start == 0;
	}

	public int get(int index) {
		return array[start + index];
	}

	public int indexOf(int v) {
		for (int i = start; i < end; ++i)
			if (array[i] == v)
				return i;
		return -1;
	}

	public boolean contains(int v) {
		for (int i = start; i < end; ++i)
			if (array[i] == v)
				return true;
		return false;
	}

	public IIntIterator iterator() {
		return new Iterator();
	}

	public IIntSequence subRange(int start, int end) {
		return new IntRange(array, this.start + start, this.start + end);
	}

	public void copyTo(int[] dst, int dststart, int srcstart, int srcend) {
		System.arraycopy(array, start + srcstart, dst, dststart, srcend - srcstart);
	}

	public void copyTo(int[] dst, int dststart) {
		System.arraycopy(array, start, dst, dststart, end - start);
	}

	public int[] toArray() {
		int len = end - start;
		int[] ret = new int[len];
		System.arraycopy(array, start, ret, 0, len);
		return ret;
	}

	//////////////////////////////////////////////////////////////////////

	class Iterator implements IIntIterator {
		int index = start;
		public boolean hasNext() {
			return index < end;
		}
		public int next() {
			return array[index++];
		}
	}
	//////////////////////////////////////////////////////////////////////
}
