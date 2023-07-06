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

/** IIntQueue is a static/dynamic random read/writable sequence of integers. */
public interface IIntQueue extends IIntVector {

	////////////////////////////////////////////////////////////////////////

	/** Same as push(). */
	void queue(int e);

	/** Same as shift(). */
	int unqueue();

	/** Remove object from the head. */
	int shift();

	/** Remove object from the head. */
	void shift(int n);

	/** Add element to tail. */
	void push(int value);

	/** Remove last element from the tail. */
	int pop();

	/**
	 * Remove given number of elements from the tail.
	 * @return The removed element with smallest index.
	 */
	int pop(int n);

	/** Same as peek(1). */
	int peek();

	/** @return The nth object (1 based) from the tail. eg. to the last object, use peek(1). */
	int peek(int nth);

	/** Truncate size to given length. */
	void setLength(int n);

	void clear();

	////////////////////////////////////////////////////////////////////////
}
