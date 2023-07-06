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

/**
 * A dynamic double end queue.
 */
public class Queue<T> extends AbstractQueue<T> {

    ////////////////////////////////////////////////////////////////////////

    public Queue() {
        this(10, 0);
    }

    public Queue(int size) {
        this(size, 0);
    }

    public Queue(int size, int head) {
        if (size <= 0) {
            size = 1;
        }
        if (head >= size) {
            head = size - 1;
        }
        elements = newarray(size);
        this.head = this.tail = head;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Add object to head.
     */
    @Override
    public void unshift(T e) {
        int nhead = decrement(head);
        if (nhead == tail) {
            grow();
            nhead = decrement(head);
        }
        head = nhead;
        elements[head] = e;
    }

    /**
     * Add object to tail.
     */
    @Override
    public void push(T e) {
        queue(e);
    }

    @Override
    public final void queue(T e) {
        int ntail = increment(tail);
        if (ntail == head) {
            grow();
            ntail = increment(tail);
        }
        elements[tail] = e;
        tail = ntail;
    }

    ////////////////////////////////////////////////////////////////////////

    protected void grow() {
        int size = size();
        int ocap = elements.length;
        int ncap = (ocap * 2 + 1);
        int nhead = (ncap - size) / 2;
        T[] ret = newarray(ncap);
        if (tail >= head) {
            System.arraycopy(elements, head, ret, nhead, size);
        } else {
            System.arraycopy(elements, head, ret, nhead, (ocap - head));
            System.arraycopy(elements, 0, ret, nhead + ocap - head, tail);
        }
        head = nhead;
        tail = nhead + size;
        elements = ret;
    }

    /**
     * Translate index relative to head to index relative to start of elements array.
     */
    @Override
    protected final int translateIndex(int nth) {
        if (nth >= size()) {
            throw new IndexOutOfBoundsException("Bound=" + size() + ", index=" + nth);
        }
        return (head + nth) % elements.length;
    }

    ////////////////////////////////////////////////////////////////////////
}
