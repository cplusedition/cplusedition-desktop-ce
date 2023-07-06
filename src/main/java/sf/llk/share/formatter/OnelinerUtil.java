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
package sf.llk.share.formatter;

import sf.llk.share.support.CharSequenceRange;
import sf.llk.share.support.IIntList;
import sf.llk.share.util.ISpaceUtil;

public class OnelinerUtil {

    private OnelinerUtil() {
    }

    public static char[] getSource(CharSequence s, int start, int end) {
        int len = end - start;
        char[] source = new char[len];
        if (s instanceof StringBuilder) {
            ((StringBuilder) s).getChars(start, end, source, 0);
        } else {
            for (int i = 0; i < len; ++i) {
                source[i] = s.charAt(start + i);
            }
        }
        return source;
    }

    public static CharSequence simpleWord(
        CharSequence s, int start, int end, boolean wasspace, IIntList softbreaks, ISpaceUtil spaceutil) {
        int left = spaceutil.skipWhitespaces(s, start, end);
        int right = spaceutil.rskipWhitespaces(s, left, end);
        boolean noleading = (left == start || wasspace || isSoftBreak(start, softbreaks));
        if (right == left) {
            return noleading ? "" : " ";
        }
        if (spaceutil.hasWhitespace(s, left, right)) {
            return null;
        }
        boolean notrailing = (right == end || isSoftBreak(right, softbreaks));
        if (noleading && notrailing) {
            return new CharSequenceRange(s, left, right);
        }
        StringBuilder ret = new StringBuilder();
        if (!noleading) {
            ret.append(' ');
        }
        ret.append(s, left, right);
        if (!notrailing) {
            ret.append(' ');
        }
        return ret;
    }

    public static boolean isSoftBreak(int i, IIntList breaks) {
        if (breaks == null) {
            return false;
        }
        return breaks.binarySearch(i) >= 0;
    }

    //////////////////////////////////////////////////////////////////////
}
