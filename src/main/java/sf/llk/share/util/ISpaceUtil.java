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

import java.util.Collection;
import java.util.List;

public interface ISpaceUtil extends ISpaceDetector {

    /**
     * @return true if character is a space character.
     */
    @Override
    public abstract boolean isSpace(char c);

    /**
     * @return true if character is a line break character.
     */
    public abstract boolean isLineBreak(char c);

    /**
     * @return true if character is a space or line break character.
     */
    @Override
    public abstract boolean isWhitespace(char c);

    public abstract boolean isSpaces(CharSequence s);

    public abstract boolean isSpaces(CharSequence s, int start, int end);

    public abstract boolean isWhitespaces(CharSequence s);

    public abstract boolean isWhitespaces(CharSequence s, int start, int end);

    @Override
    public abstract int skipSpaces(CharSequence s, int start, int end);

    @Override
    public abstract int skipWhitespaces(CharSequence s, int start, int end);

    @Override
    public abstract int rskipSpaces(CharSequence s, int start, int end);

    @Override
    public abstract int rskipWhitespaces(CharSequence s, int start, int end);

    public abstract int skipNonSpaces(CharSequence s, int start, int end);

    public abstract int skipNonWhitespaces(CharSequence s, int start, int end);

    public abstract int rskipNonSpaces(CharSequence s, int start, int end);

    public abstract int rskipNonWhitespaces(CharSequence s, int start, int end);

    public abstract boolean endsWithLineBreak(CharSequence s);

    public abstract boolean endsWithSpaces(CharSequence s);

    public abstract boolean endsWithWhitespaces(CharSequence s);

    public abstract int countLines(CharSequence s);

    public abstract int countLines(CharSequence s, int start, int end);

    public abstract int lcountLineBreaks(CharSequence s);

    public abstract int lcountLineBreaks(CharSequence s, int start, int end);

    public abstract int lcountBlankLines(CharSequence s);

    public abstract int lcountBlankLines(CharSequence s, int start, int end);

    /**
     * @return Number of trailing line breaks.
     */
    public abstract int rcountLineBreaks(CharSequence s);

    public abstract int rcountLineBreaks(CharSequence s, int start, int end);

    /**
     * @return Number of trailing blank lines.
     */
    public abstract int rcountBlankLines(CharSequence s);

    public abstract int rcountBlankLines(CharSequence s, int start, int end);

    public abstract boolean hasLineBreak(CharSequence s);

    public abstract boolean hasLineBreak(CharSequence s, int start, int end);

    public abstract boolean hasWhitespace(CharSequence s, int start, int end);

    public abstract CharSequence ltrimSpaces(CharSequence b);

    public abstract CharSequence ltrimSpaces(CharSequence b, int start, int end);

    public abstract CharSequence ltrimWhitespaces(CharSequence b);

    public abstract CharSequence ltrimWhitespaces(CharSequence b, int start, int end);

    public abstract CharSequence ltrimLineBreaks(CharSequence buf);

    public abstract CharSequence ltrimBlankLines(CharSequence buf);

    public abstract CharSequence rtrimSpaces(CharSequence b);

    public abstract CharSequence rtrimSpaces(CharSequence b, int start, int end);

    public abstract CharSequence rtrimWhitespaces(CharSequence b);

    public abstract CharSequence rtrimWhitespaces(CharSequence b, int start, int end);

    public abstract CharSequence rtrimLineBreaks(CharSequence buf);

    public abstract CharSequence rtrimBlankLines(CharSequence buf);

    public abstract CharSequence trimSpaces(CharSequence b);

    public abstract CharSequence trimSpaces(CharSequence b, int start, int end);

    public abstract CharSequence trimWhitespaces(CharSequence b);

    public abstract CharSequence trimWhitespaces(CharSequence b, int start, int end);

    public abstract boolean ltrimSpaces(StringBuilder b);

    public abstract boolean ltrimSpaces(StringBuilder b, int start, int end);

    public abstract boolean ltrimWhitespaces(StringBuilder b);

    public abstract boolean ltrimWhitespaces(StringBuilder b, int start, int end);

    /**
     * @return Number of trailing line breaks deleted.
     */
    public abstract int ltrimLineBreaks(StringBuilder buf);

    /**
     * @return Number of blank lines deleted.
     */
    public abstract int ltrimBlankLines(StringBuilder buf);

    public abstract boolean rtrimSpaces(StringBuilder b);

    public abstract boolean rtrimSpaces(StringBuilder b, int start, int end);

    public abstract boolean rtrimWhitespaces(StringBuilder b);

    public abstract boolean rtrimWhitespaces(StringBuilder b, int start, int end);

    /**
     * @return Number of trailing line breaks deleted.
     */
    public abstract int rtrimLineBreaks(StringBuilder buf);

    /**
     * @return Number of blank lines deleted.
     */
    public abstract int rtrimBlankLines(StringBuilder buf);

    public abstract boolean rspace(StringBuilder buf);

    public abstract boolean ltrimSpaces(StringBuffer b);

    public abstract boolean ltrimSpaces(StringBuffer b, int start, int end);

    public abstract boolean ltrimWhitespaces(StringBuffer b);

    public abstract boolean ltrimWhitespaces(StringBuffer b, int start, int end);

    /**
     * @return Number of trailing line breaks deleted.
     */
    public abstract int ltrimLineBreaks(StringBuffer buf);

    /**
     * @return Number of blank lines deleted.
     */
    public abstract int ltrimBlankLines(StringBuffer buf);

    public abstract boolean rtrimSpaces(StringBuffer b);

    public abstract boolean rtrimSpaces(StringBuffer b, int start, int end);

    public abstract boolean rtrimWhitespaces(StringBuffer b);

    public abstract boolean rtrimWhitespaces(StringBuffer b, int start, int end);

    /**
     * @return Number of trailing line breaks deleted.
     */
    public abstract int rtrimLineBreaks(StringBuffer buf);

    /**
     * @return Number of blank lines deleted.
     */
    public abstract int rtrimBlankLines(StringBuffer buf);

    public abstract boolean rspace(StringBuffer buf);

    public abstract int columnOf(CharSequence s, int start, int end, int lmargin, int tabwidth);

    public abstract List<String> splitLines(CharSequence str);

    /**
     * Split words using whitespaces as delimiters.
     */
    public abstract int splitWords(Collection<String> ret, CharSequence str, int start, int end);
}
