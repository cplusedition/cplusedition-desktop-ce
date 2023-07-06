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
package com.cplusedition.bot.text

interface ISpaceDetector {

    /** @return true if given character is non-line-break whitespaces.
     */
    fun isSpace(c: Char): Boolean

    /** @return true if given character is whitespaces (ie. spaces or line breaks).
     */
    fun isWhitespace(c: Char): Boolean

    /**
     * @return offset after line break sequence,
     * return start if no line break sequence is found at offset==start.
     */
    fun skipLineBreak(s: CharSequence, start: Int, end: Int): Int

    /**
     * @return offset of start of line break sequence that ends at offset==end,
     * return end if no line break sequence ends at offset==end.
     */
    fun rskipLineBreak(s: CharSequence, start: Int, end: Int): Int

    fun skipSpaces(s: CharSequence, start: Int, end: Int): Int
    fun rskipSpaces(s: CharSequence, start: Int, end: Int): Int
    fun skipWhitespaces(s: CharSequence, start: Int, end: Int): Int
    fun rskipWhitespaces(s: CharSequence, start: Int, end: Int): Int

    /** @retrun offset of the next line break, -1 if no line break is found.
     */
    fun skipLine(s: CharSequence, start: Int, end: Int): Int

    /** @retrun offset of the next line break, end if no line break is found.
     */
    fun skipLineSafe(s: CharSequence, start: Int, end: Int): Int

    /** @retrun offset of first character on the next line, end if no line break is found.
     */
    fun skipToNextLine(s: CharSequence, start: Int, end: Int): Int

    /** @retrun offset of the start of line (ie. after line break), start if no line break is found.
     */
    fun rskipLine(s: CharSequence, start: Int, end: Int): Int
}
