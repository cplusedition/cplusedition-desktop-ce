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
/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.support;

import java.util.Arrays;

/**
 * A simple implementation of IIntSet using an int[] and binary search and insertion.
 */
public final class IntArraySet implements IIntSet {

    ////////////////////////////////////////////////////////////////////////

    private static final int CAPACITY = 10;

    ////////////////////////////////////////////////////////////////////////

    protected int[] array;
    protected int size;

    ////////////////////////////////////////////////////////////////////////

    public IntArraySet() {
        this(CAPACITY);
    }

    public IntArraySet(int cap) {
        array = new int[cap];
        size = 0;
    }

    public IntArraySet(IIntIterable a) {
        this(a.size());
        for (IIntIterator it = a.iterator(); it.hasNext(); ) {
            add(it.next());
        }
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean contains(int v) {
        return Arrays.binarySearch(array, 0, size, v) >= 0;
    }

    @Override
    public boolean remove(int v) {
        int index = Arrays.binarySearch(array, 0, size, v);
        if (index < 0) {
            return false;
        }
        --size;
        if (index != size) {
            System.arraycopy(array, index + 1, array, index, size - index);
        }
        return true;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public IIntIterator iterator() {
        return new IntSetIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(int v) {
        int index = Arrays.binarySearch(array, 0, size, v);
        if (index >= 0) {
            return false;
        }
        index = -index - 1;
        if (index >= array.length) {
            array = Arrays.copyOf(array, Math.max(array.length * 2, index + 1));
        }
        if (size >= array.length) {
            array = Arrays.copyOf(array, array.length * 2);
        }
        if (index < size) {
            System.arraycopy(array, index, array, index + 1, size - index);
        }
        array[index] = v;
        ++size;
        return true;
    }

    @Override
    public boolean addAll(int... a) {
        boolean added = false;
        for (int v : a) {
            added |= add(v);
        }
        return added;
    }

    @Override
    public boolean addAll(IIntIterable a) {
        boolean added = false;
        for (IIntIterator it = a.iterator(); it.hasNext(); ) {
            added |= add(it.next());
        }
        return added;
    }

    @Override
    public void copyTo(int[] dst, int dststart) {
        System.arraycopy(array, 0, dst, dststart, size);
    }

    @Override
    public int[] toArray() {
        return Arrays.copyOf(array, size);
    }

    ////////////////////////////////////////////////////////////////////////

    protected class IntSetIterator implements IIntIterator {
        private int index;

        IntSetIterator() {
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public int next() {
            return array[index++];
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
