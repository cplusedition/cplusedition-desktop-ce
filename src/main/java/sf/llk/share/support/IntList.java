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

import java.io.Serializable;
import java.util.Arrays;

public class IntList implements IIntList, IIntStack, Cloneable, Serializable {

	////////////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 5681418481728541841L;
	private static final int CAPACITY = 8;

	////////////////////////////////////////////////////////////////////////

	protected int capacity = CAPACITY;
	protected int size = 0;
	protected int[] list;

	////////////////////////////////////////////////////////////////////////

	public IntList() {
		list = new int[capacity];
	}

	public IntList(int cap) {
		capacity = cap;
		list = new int[capacity];
	}

	public IntList(int[] a) {
		size = a.length;
		capacity = size + CAPACITY;
		list = new int[capacity];
		System.arraycopy(a, 0, list, 0, size);
	}

	public IntList(IIntVector a) {
		size = a.size();
		capacity = a.size() + CAPACITY;
		list = new int[capacity];
		a.copyTo(list, 0, 0, size);
	}

	////////////////////////////////////////////////////////////////////////

	public void add(int v) {
		if (size == capacity)
			expand();
		list[size] = v;
		++size;
	}

	public void add(int[] a) {
		add(a, 0, a.length);
	}

	public void add(int[] a, int start, int end) {
		int len = end - start;
		ensureCapacity(size + len);
		System.arraycopy(a, start, list, size, len);
		size += len;
	}

	public void insert(int index, int v) {
		if (index > size)
			throw indexSizeException("Expected index <= ", index);
		if (size == capacity)
			expand();
		if (index != size)
			System.arraycopy(list, index, list, index + 1, size - index);
		list[index] = v;
		++size;
	}

	public void insert(int index, int[] a) {
		insert(index, a, 0, a.length);
	}

	public void insert(int index, int[] a, int start, int end) {
		if (index > size)
			throw indexSizeException("Expected index <= ", index);
		int len = end - start;
		if (size + len > capacity) {
			int newsize = Math.max(size + len, capacity) + capacity / 2;
			int[] ret = new int[newsize];
			System.arraycopy(list, 0, ret, 0, index);
			System.arraycopy(a, start, ret, index, len);
			if (index != size)
				System.arraycopy(list, index, ret, index + len, size - index);
			list = ret;
		} else {
			if (index != size)
				System.arraycopy(list, index, list, index + len, size - index);
			System.arraycopy(a, start, list, index, len);
		}
		size += len;
	}

	public int remove(int index) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		int ret = list[index];
		if (index != size - 1)
			System.arraycopy(list, index + 1, list, index, size - index - 1);
		--size;
		return ret;
	}

	public void remove(int start, int end) {
		if (end > size)
			throw indexSizeException("Expected end < ", end);
		int len = end - start;
		System.arraycopy(list, end, list, start, len);
		size -= len;
	}

	public void set(int index, int value) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		list[index] = value;
	}

	public void or(int index, int value) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		list[index] |= value;
	}

	public void and(int index, int value) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		list[index] &= value;
	}

	public void andNot(int index, int value) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		list[index] &= ~value;
	}

	public void xor(int index, int value) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		list[index] ^= value;
	}

	public int get(int index) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		return list[index];
	}

	public int size() {
		return size;
	}

	public int[] toArray() {
		int[] ret = new int[size];
		System.arraycopy(list, 0, ret, 0, size);
		return ret;
	}

	/**
	 * Convert content to byte[] by casting each int element to byte.
	 */
	public byte[] toByteArray() {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; ++i)
			ret[i] = (byte)list[i];
		return ret;
	}

	public void copyTo(int[] dst, int dststart, int srcstart, int srcend) {
		System.arraycopy(list, srcstart, dst, dststart, srcend - srcstart);
	}

	public void copyTo(int[] dst, int dststart) {
		System.arraycopy(list, 0, dst, dststart, size);
	}

	public void pack() {
		int[] ret = new int[size];
		System.arraycopy(list, 0, ret, 0, size);
		list = ret;
		capacity = size;
	}

	public void clear() {
		size = 0;
	}

	public String toString() {
		return toString(8);
	}

	public String toString(int count) {
		StringBuffer buf = new StringBuffer();
		int count1 = count - 1;
		for (int i = 0; i < size; ++i) {
			buf.append(list[i]);
			buf.append(' ');
			if ((i % count) == count1)
				buf.append("\n");
		}
		if ((size % count) != 0)
			buf.append("\n");
		return buf.toString();
	}

	public Object clone() throws CloneNotSupportedException {
		IntList ret = (IntList)super.clone();
		ret.list = list.clone();
		return ret;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void push(int value) {
		if (size == capacity)
			expand();
		list[size] = value;
		++size;
	}

	public int peek() {
		return list[size - 1];
	}

	public int pop() {
		--size;
		return list[size];
	}

	public int peek(int n) {
		if (n <= 0 || n > size)
			throw new IndexOutOfBoundsException("Expected 0 < n <= " + size + ": n=" + n);
		return list[size - n];
	}

	public int pop(int n) {
		if (n <= 0 || n > size)
			throw new IndexOutOfBoundsException("Expected: 0 < n <= " + size + ": n=" + n);
		size -= n;
		return list[size];
	}

	public int swap(int value) {
		int ret = list[size - 1];
		list[size - 1] = value;
		return ret;
	}

	////////////////////////////////////////////////////////////////////////
	
	public final void queue(int e) {
		push(e);
	}

	public final int unqueue() {
		return shift();
	}

	public final int shift() {
		return remove(0);
	}

	public final void shift(int n) {
		remove(0, n);
	}

	////////////////////////////////////////////////////////////////////////

	public IIntIterator iterator() {
		return new IntListIterator();
	}

	public void setLength(int n) {
		if (n < 0 || n > size)
			throw new IndexOutOfBoundsException("Expected: 0 <= n <= " + size + ": n=" + n);
		size = n;
	}

	public void ensureCapacity(int newcap) {
		if (newcap > capacity) {
			newcap = Math.max(newcap, capacity) + capacity / 2;
			expand(newcap);
	}}

	/** Truncate size by the given delta. */
	public void shrinkBy(int n) {
		if (n < 0 || n > size)
			throw new IndexOutOfBoundsException("Expected: 0 <= n <= " + size + ": n=" + n);
		size -= n;
	}

	/** Increase size by the given delta and filled with given default value. */
	public void growBy(int n, int def) {
		int newsize = size + n;
		if (newsize > capacity)
			expand(newsize + newsize >> 1);
		for (int i = size; i < newsize; ++i)
			list[i] = def;
		size = newsize;
	}

	/** @return Value of the last element after increment. */
	public int increment() {
		if (size == 0)
			throw new IndexOutOfBoundsException("size==0");
		return ++list[size - 1];
	}

	/** @return Value of the last element after decrement. */
	public int decrement() {
		if (size == 0)
			throw new IndexOutOfBoundsException("size==0");
		return --list[size - 1];
	}

	/** @return Value at given index after increment. */
	public int increment(int index) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		return ++list[index];
	}

	/** @return Value at given index after decrement. */
	public int decrement(int index) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		return --list[index];
	}

	public void sort() {
		Arrays.sort(list, 0, size);
	}

	/**
	 * Keep only unique values.
	 * @entry Must have been sorted.
	 */
	public void unique() {
		if (size <= 1)
			return;
		int k = 0;
		int p = list[0];
		for (int i = 1; i < size; ++i) {
			int v = list[i];
			if (v != p) {
				list[++k] = p = v;
		}}
		size = k + 1;
	}

	public int binarySearch(int x) {
		int start = 0;
		int end = size - 1;
		while (start <= end) {
			int mid = (start + end) >>> 1;
			if (list[mid] < x)
				start = mid + 1;
			else if (list[mid] > x)
				end = mid - 1;
			else
				return mid;
		}
		return -(start + 1);
	}

	public int insertionIndex(int x) {
		int start = 0;
		int end = size - 1;
		while (start <= end) {
			int mid = (start + end) >>> 1;
			if (list[mid] < x)
				start = mid + 1;
			else if (list[mid] > x)
				end = mid - 1;
			else
				return mid + 1;
		}
		return start;
	}

	public int indexOf(int x) {
		for (int i = 0; i < size; ++i) {
			if (list[i] == x)
				return i;
		}
		return -1;
	}

	public boolean contains(int value) {
		return indexOf(value) >= 0;
	}

	public IntRange subRange(int start, int end) {
		return new IntRange(list, start, end);
	}

	////////////////////////////////////////////////////////////////////////

	protected void expand() {
		expand(capacity + (capacity >> 1) + 1);
	}

	protected void expand(int cap) {
		this.capacity = cap;
		int[] ret = new int[cap];
		System.arraycopy(list, 0, ret, 0, size);
		list = ret;
	}

	private IndexOutOfBoundsException indexSizeException(String msg, int index) {
		return new IndexOutOfBoundsException(msg + size + ", index=" + index);
	}

	////////////////////////////////////////////////////////////////////////

	protected class IntListIterator implements IIntIterator {
		private int index = 0;
		public boolean hasNext() {
			return index < size;
		}
		public int next() {
			return list[index++];
		}
	}

	////////////////////////////////////////////////////////////////////////
}
