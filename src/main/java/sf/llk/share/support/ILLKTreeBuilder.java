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

public interface ILLKTreeBuilder {

    public abstract Object clone() throws CloneNotSupportedException;

    public abstract void reset();

    public abstract ILLKNode root();

    public abstract void push(ILLKNode n);

    public abstract ILLKNode pop();

    public abstract ILLKNode peek();

    public abstract ILLKNode peek(int n);

    public abstract int size();

    public abstract ILLKNode get(int index);

    public abstract int peekMark();

    public abstract int peekMark(int n);

    public abstract int childCount();

    public abstract void clearScope();

    public abstract void open();

    public abstract void open(ILLKNode node);

    public abstract void close(ILLKNode n, int num);

    public abstract void close(ILLKNode n);

    public abstract void close(ILLKNode n, boolean condition);
}
