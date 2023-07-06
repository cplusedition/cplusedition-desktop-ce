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

interface ISpaceUtil : ISpaceDetector {

    /** @return true if character is a line break character.
     */
    fun isLineBreak(c: Char): Boolean

    fun isSpaces(s: CharSequence): Boolean
    fun isSpaces(s: CharSequence, start: Int, end: Int): Boolean
    fun isWhitespaces(s: CharSequence): Boolean
    fun isWhitespaces(s: CharSequence, start: Int, end: Int): Boolean

    fun skipNonSpaces(s: CharSequence, start: Int, end: Int): Int
    fun skipNonWhitespaces(s: CharSequence, start: Int, end: Int): Int
    fun rskipNonSpaces(s: CharSequence, start: Int, end: Int): Int
    fun rskipNonWhitespaces(s: CharSequence, start: Int, end: Int): Int

    fun endsWithLineBreak(s: CharSequence): Boolean
    fun endsWithSpaces(s: CharSequence): Boolean
    fun endsWithWhitespaces(s: CharSequence): Boolean

    fun countLines(s: CharSequence): Int
    fun countLines(s: CharSequence, start: Int, end: Int): Int

    fun lcountLineBreaks(s: CharSequence): Int
    fun lcountLineBreaks(s: CharSequence, start: Int, end: Int): Int
    fun lcountBlankLines(s: CharSequence): Int
    fun lcountBlankLines(s: CharSequence, start: Int, end: Int): Int

    /** @return Number of trailing line breaks.
     */
    fun rcountLineBreaks(s: CharSequence): Int

    fun rcountLineBreaks(s: CharSequence, start: Int, end: Int): Int
    /** @return Number of trailing blank lines.
     */
    fun rcountBlankLines(s: CharSequence): Int

    fun rcountBlankLines(s: CharSequence, start: Int, end: Int): Int

    fun hasLineBreak(s: CharSequence): Boolean
    fun hasLineBreak(s: CharSequence, start: Int, end: Int): Boolean
    fun hasWhitespace(s: CharSequence, start: Int, end: Int): Boolean

    fun ltrimSpaces(b: CharSequence): CharSequence
    fun ltrimSpaces(b: CharSequence, start: Int, end: Int): CharSequence
    fun ltrimWhitespaces(b: CharSequence): CharSequence
    fun ltrimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence
    fun ltrimLineBreaks(buf: CharSequence): CharSequence
    fun ltrimBlankLines(buf: CharSequence): CharSequence

    fun rtrimSpaces(b: CharSequence): CharSequence
    fun rtrimSpaces(b: CharSequence, start: Int, end: Int): CharSequence
    fun rtrimWhitespaces(b: CharSequence): CharSequence
    fun rtrimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence
    fun rtrimLineBreaks(buf: CharSequence): CharSequence
    fun rtrimBlankLines(buf: CharSequence): CharSequence

    fun trimSpaces(b: CharSequence): CharSequence
    fun trimSpaces(b: CharSequence, start: Int, end: Int): CharSequence
    fun trimWhitespaces(b: CharSequence): CharSequence
    fun trimWhitespaces(b: CharSequence, start: Int, end: Int): CharSequence

    fun ltrimSpaces(b: StringBuilder): Boolean
    fun ltrimSpaces(b: StringBuilder, start: Int, end: Int): Boolean
    fun ltrimWhitespaces(b: StringBuilder): Boolean
    fun ltrimWhitespaces(b: StringBuilder, start: Int, end: Int): Boolean
    /** @return Number of trailing line breaks deleted.
     */
    fun ltrimLineBreaks(buf: StringBuilder): Int

    /** @return Number of blank lines deleted.
     */
    fun ltrimBlankLines(buf: StringBuilder): Int

    fun rtrimSpaces(b: StringBuilder): Boolean
    fun rtrimSpaces(b: StringBuilder, start: Int, end: Int): Boolean
    fun rtrimWhitespaces(b: StringBuilder): Boolean
    fun rtrimWhitespaces(b: StringBuilder, start: Int, end: Int): Boolean
    /** @return Number of trailing line breaks deleted.
     */
    fun rtrimLineBreaks(buf: StringBuilder): Int

    /** @return Number of blank lines deleted.
     */
    fun rtrimBlankLines(buf: StringBuilder): Int

    fun rspace(buf: StringBuilder): Boolean

    fun ltrimSpaces(b: StringBuffer): Boolean
    fun ltrimSpaces(b: StringBuffer, start: Int, end: Int): Boolean
    fun ltrimWhitespaces(b: StringBuffer): Boolean
    fun ltrimWhitespaces(b: StringBuffer, start: Int, end: Int): Boolean
    /** @return Number of trailing line breaks deleted.
     */
    fun ltrimLineBreaks(buf: StringBuffer): Int

    /** @return Number of blank lines deleted.
     */
    fun ltrimBlankLines(buf: StringBuffer): Int

    fun rtrimSpaces(b: StringBuffer): Boolean
    fun rtrimSpaces(b: StringBuffer, start: Int, end: Int): Boolean
    fun rtrimWhitespaces(b: StringBuffer): Boolean
    fun rtrimWhitespaces(b: StringBuffer, start: Int, end: Int): Boolean
    /** @return Number of trailing line breaks deleted.
     */
    fun rtrimLineBreaks(buf: StringBuffer): Int

    /** @return Number of blank lines deleted.
     */
    fun rtrimBlankLines(buf: StringBuffer): Int

    fun rspace(buf: StringBuffer): Boolean

    fun columnOf(s: CharSequence, start: Int, end: Int, lmargin: Int, tabwidth: Int): Int
    fun splitLines(str: CharSequence): List<String>
    /** Split words using whitespaces as delimiters.  */
    fun splitWords(ret: MutableCollection<String>, str: CharSequence, start: Int, end: Int): Int
}
