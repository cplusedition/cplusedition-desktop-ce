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
package sf.llk.share.util;

import java.util.List;

import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseError;

public class NodeUtil {

    private NodeUtil() {
    }

    public static boolean hasAncestor(final ILLKNode node, final int type) {
        ILLKNode p = node.getParent();
        while (p != null) {
            if (p.getType() == type) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * @return true if node has descendent of the given type.
     */
    public static boolean hasDescendent(final ILLKNode node, final int type) {
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            if (child.getType() == type || hasDescendent(child, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if node has descendent of the given types.
     */
    public static boolean hasDescendent(final ILLKNode node, final int... types) {
        int type;
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            type = child.getType();
            for (int k = 0; k < types.length; ++k) {
                if (type == types[k]) {
                    return true;
                }
            }
            if (hasDescendent(child, types)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if node has descendents of the given type only.
     */
    public static boolean hasOnlyDescendent(final ILLKNode node, final int type) {
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            if (child.getType() != type || !hasOnlyDescendent(child, type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if node has descendents of the given types only.
     */
    public static boolean hasOnlyDescendent(final ILLKNode node, final int... types) {
        int type;
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            type = child.getType();
            FOUND:
            {
                for (int k = 0; k < types.length; ++k) {
                    if (type == types[k]) {
                        break FOUND;
                    }
                }
                return false;
            }
            if (!hasOnlyDescendent(child, types)) {
                return false;
            }
        }
        return true;
    }

    public static int countDescendent(final ILLKNode node, final int... types) {
        int count = 0;
        int type;
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            type = child.getType();
            for (int k = 0; k < types.length; ++k) {
                if (type == types[k]) {
                    ++count;
                }
            }
            count += countDescendent(child, types);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ILLKNode> List<T> findDescendent(
        final ILLKNode node, final int type, final List<T> ret) {
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            if (child.getType() == type) {
                ret.add((T) child);
            }
            findDescendent(child, type, ret);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ILLKNode> List<T> findDescendent(
        final ILLKNode node, final Class<?> c, final List<T> ret) {
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            if (c.isInstance(child)) {
                ret.add((T) child);
            }
            findDescendent(child, c, ret);
        }
        return ret;
    }

    public static int countNodes(final ILLKNode node) {
        int count = 0;
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            count += countNodes(child) + 1;
        }
        return count;
    }

    public static boolean checkDistinctChildren(final ILLKNode a, final ILLKNode b) {
        for (ILLKNode aa = a.getFirst(), bb = b.getFirst();
             aa != null && bb != null;
             aa = aa.getNext(),
                 bb = bb.getNext()) {
            if (aa == bb) {
                throw new LLKParseError("ERROR: children are not distinct:\n\t" + aa + "\n\t" + bb);
            }
            if (!checkDistinctChildren(aa, bb)) {
                return false;
            }
        }
        return true;
    }

    public static ILLKNode[] structuralEquals(final ILLKNode a, final ILLKNode b) {
        if (a == null) {
            return b == null ? null : new ILLKNode[]{null, b};
        }
        if (b == null || a.getType() != b.getType() || a.childCount() != b.childCount()) {
            return new ILLKNode[]{a, b};
        }
        final String na = a.getText().toString();
        final String nb = b.getText().toString();
        if (na == null && nb != null || na != null && !na.equals(nb)) {
            return new ILLKNode[]{a, b};
        }
        for (ILLKNode t1 = a.getFirst(), t2 = b.getFirst(); t1 != null; t1 = t1.getNext(), t2 = t2.getNext()) {
            final ILLKNode[] ret = structuralEquals(t1, t2);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public static StringBuilder getTokenText(ILLKToken start, final int end) {
        final StringBuilder b = new StringBuilder();
        for (; start.getOffset() < end; start = start.getNext()) {
            b.append(start.getText());
        }
        return b;
    }

    public static String toString(final String[] names, final ILLKToken t) {
        final int type = t.getType();
        final int start = t.getOffset();
        return t.getClass().getName()
            + "(type="
            + type
            + ", name="
            + names[type]
            + ", start="
            + start
            + ", text="
            + t.getText();
    }

    public static String toString(final String[] names, final char[] source, final ILLKToken t) {
        if (source == null) {
            return toString(names, t);
        }
        final int type = t.getType();
        final int start = t.getOffset();
        return t.getClass().getName()
            + "(type="
            + type
            + ", name="
            + names[type]
            + ", start="
            + start
            + ", text="
            + t.getText()
            + ", source="
            + new String(source, start, t.getEndOffset() - start)
            + ")";
    }
}
