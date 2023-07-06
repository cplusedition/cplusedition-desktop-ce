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

public class DefaultSpaceUtil extends AbstractSpaceUtil {

    private static final char[] SPACES = new char[]{' ', '\t', '\f'};
    private static final char[] WHITESPACES = new char[]{' ', '\t', '\f', '\n', '\r'};

    private static DefaultSpaceUtil singleton;

    public static DefaultSpaceUtil getSingleton() {
        if (singleton == null) {
            singleton = new DefaultSpaceUtil();
        }
        return singleton;
    }

    @Override
    public boolean isWhitespace(char c) {
        for (int i = WHITESPACES.length - 1; i >= 0; --i) {
            if (c == WHITESPACES[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSpace(char c) {
        for (int i = SPACES.length - 1; i >= 0; --i) {
            if (c == SPACES[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLineBreak(char c) {
        return c == '\n' || c == '\r';
    }

    @Override
    public int skipLineBreak(CharSequence s, int start, int end) {
        if (start < end) {
            char c = s.charAt(start);
            if (c == '\n') {
                return start + 1;
            }
            if (c == '\r') {
                if (start + 1 < end && s.charAt(start + 1) == '\n') {
                    return start + 2;
                }
                return start + 1;
            }
        }
        return start;
    }

    @Override
    public int rskipLineBreak(CharSequence s, int start, int end) {
        if (end > start) {
            char c = s.charAt(end - 1);
            if (c == '\n') {
                --end;
                if (end - 1 >= start && s.charAt(end - 1) == '\r') {
                    --end;
                }
            } else if (c == '\r') {
                --end;
            }
        }
        return end;
    }

    @Override
    public boolean hasLineBreak(CharSequence s) {
        char c;
        for (int i = s.length() - 1; i >= 0; --i) {
            c = s.charAt(i);
            if (c == '\n' || c == '\r') {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasLineBreak(CharSequence s, int start, int end) {
        char c;
        for (int i = end - 1; i >= start; --i) {
            c = s.charAt(i);
            if (c == '\n' || c == '\r') {
                return true;
            }
        }
        return false;
    }
}
