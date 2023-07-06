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

public interface ILLKNode extends ISourceText {

    int getType();

    int getModifiers();

    ILLKNode getParent();

    /**
     * @return The first child of this node; null if no children.
     */
    ILLKNode getFirst();

    /**
     * @return The next sibling, null if this is the last child..
     */
    ILLKNode getNext();

    /**
     * @return The last child, null if node has no children.
     */
    ILLKNode getLast();

    ILLKToken getFirstToken();

    ILLKToken getLastToken();

    Object getData();

    void setType(int type);

    void setModifiers(int m);

    void setText(CharSequence name);

    void setOffset(int offset);

    void setEndOffset(int offset);

    void setParent(ILLKNode n);

    void setFirst(ILLKNode n);

    void setNext(ILLKNode n);

    void setFirstToken(ILLKToken t);

    void setLastToken(ILLKToken t);

    void setData(Object v);

    boolean hasChildren();

    boolean hasSingleChild();

    boolean hasMultipleChildren();

    int indexOf(ILLKNode n);

    int childCount();

    ILLKNode get(int n);

    void prepend(ILLKNode n);

    boolean add(ILLKNode n);

    /**
     * @return n that is just added.
     */
    ILLKNode add(ILLKNode n, ILLKNode prev);

    void addAll(ILLKNode list, ILLKNode prev);

    void addAll(ILLKNode list);

    /**
     * Remove children from (and include) start to (and exclude) end.
     * If end==null, remove all children from (and include) start.
     *
     * @return false if start not found.
     */
    boolean remove(ILLKNode start, ILLKNode end);

    boolean remove(ILLKNode n);

    ILLKNode removeAll();

    ILLKNode detach();

    Iterable<ILLKNode> children();

    Iterable<ILLKToken> tokens();

    String getLocationString();

    ILLKNode[] structuralEquals(ILLKNode n);

    Object clone() throws CloneNotSupportedException;
}
