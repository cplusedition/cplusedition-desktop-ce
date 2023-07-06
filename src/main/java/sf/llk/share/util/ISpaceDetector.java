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

public interface ISpaceDetector {

    /**
     * @return true if given character is non-line-break whitespaces.
     */
    boolean isSpace(char c);

    /**
     * @return true if given character is whitespaces (ie. spaces or line breaks).
     */
    boolean isWhitespace(char c);

    /**
     * @return offset after line break sequence,
     * return start if no line break sequence is found at offset==start.
     */
    int skipLineBreak(CharSequence s, int start, int end);

    /**
     * @return offset of start of line break sequence that ends at offset==end,
     * return end if no line break sequence ends at offset==end.
     */
    int rskipLineBreak(CharSequence s, int start, int end);

    int skipSpaces(CharSequence s, int start, int end);

    int rskipSpaces(CharSequence s, int start, int end);

    int skipWhitespaces(CharSequence s, int start, int end);

    int rskipWhitespaces(CharSequence s, int start, int end);

    /**
     * @retrun offset of the next line break, -1 if no line break is found.
     */
    int skipLine(CharSequence s, int start, int end);

    /**
     * @retrun offset of the next line break, end if no line break is found.
     */
    int skipLineSafe(CharSequence s, int start, int end);

    /**
     * @retrun offset of first character on the next line, end if no line break is found.
     */
    int skipToNextLine(CharSequence s, int start, int end);

    /**
     * @retrun offset of the start of line (ie. after line break), start if no line break is found.
     */
    int rskipLine(CharSequence s, int start, int end);
}
