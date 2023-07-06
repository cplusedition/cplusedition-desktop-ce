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
package sf.llk.share.formatter;

import java.io.PrintWriter;

import sf.llk.share.support.CharRange;
import sf.llk.share.support.IIntList;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.util.DefaultNameDetector;
import sf.llk.share.util.DefaultSpaceUtil;
import sf.llk.share.util.INameDetector;
import sf.llk.share.util.ISpaceDetector;

public class FormatterTestUtil {

    protected FormatterTestUtil() {
    }

    public static boolean verifyFormatted(
        char[] source,
        CharSequence formatted,
        INameDetector namedetector,
        ISpaceDetector spaceutil,
        ILLKMain main) {
        return verifyFormatted(source, formatted, namedetector, spaceutil, main, true);
    }

    public static boolean verifyFormatted(
        char[] source,
        CharSequence formatted,
        INameDetector namedetector,
        ISpaceDetector spaceutil,
        ILLKMain main,
        boolean checkindent) {
        boolean ok = true;
        if (checkindent)
            ok &= checkIndent(formatted, spaceutil, main);
        CharSequence input = new CharRange(source);
        int[] diff = differIgnoringWhitespaces(input, formatted, null, false, namedetector, spaceutil);
        if (diff != null) {
            main.error(
                "Formatted output differ from input source: file="
                    + main.getFilepath()
                    + ", first difference at original offset="
                    + diff[0]
                    + ", formatted offset="
                    + diff[1]
                    + "\n# original=\n"
                    + threeLines(diff[0], input, 0, input.length(), spaceutil)
                    + "\n# formatted=\n"
                    + threeLines(diff[1], formatted, 0, formatted.length(), spaceutil));
            return false;
        }
        return ok;
    }

    public static CharSequence threeLines(int offset, CharSequence s, int start, int end, ISpaceDetector spaceutil) {
        int ss = Math.max(start, offset - 100);
        int ee = Math.min(end, offset + 100);
        return s.subSequence(ss, offset) + "|***|" + s.subSequence(offset, ee);
    }

    public static CharSequence threeLines(int offset, char[] s, int start, int end, ISpaceDetector spaceutil) {
        int ss = Math.max(start, offset - 100);
        int ee = Math.min(end, offset + 100);
        return String.valueOf(s, ss, offset - ss) + "|***|" + String.valueOf(s, offset, ee - offset);
    }

    public static boolean checkIndent(CharSequence formatted, ISpaceDetector spaceutil, ILLKMain main) {
        int len = formatted.length();
        int index = len;
        while (len > 0) {
            index = spaceutil.rskipWhitespaces(formatted, 0, len);
            index = spaceutil.skipSpaces(formatted, findLineStart(formatted, index), index);
            if (index == len)
                break;
            if (formatted.charAt(index) != '*')
                break;
            len = index;
        }
        if (index >= 0) {
            int indent = indentOf(index, 8, formatted, spaceutil);
            if (indent != 1) {
                main.error(
                    main.getFilepath()
                        + ": "
                        + "Expected indent of last non-empty line at column 1: actual="
                        + indent
                        + ".  This may not actually an error, since this check is rather dumb.");
                return false;
            }
        }
        return true;
    }

    public static int indentOf(int offset, int tabwidth, CharSequence text, ISpaceDetector spaceutil) {
        return indentOf(text, offset, text.length(), tabwidth, spaceutil);
    }

    public static int indentOf(CharSequence text, int start, int end, int tabwidth, ISpaceDetector spaceutil) {
        int ret = 0;
        int index;
        for (; start < end; ++start) {
            if ((index = spaceutil.skipLineBreak(text, start, end)) != start) {
                start = index - 1;
                ret = 0;
                continue;
            }
            char c = text.charAt(start);
            if (c == '\t')
                ret = (ret / tabwidth + 1) * tabwidth;
            else if (spaceutil.isSpace(c))
                ++ret;
            else
                break;
        }
        return ret + 1;
    }

    public static int findLineStart(CharSequence text, int offset) {
        for (; offset > 0; --offset) {
            if (text.charAt(offset - 1) == '\n')
                return offset;
        }
        return offset;
    }

    public static int countNodes(ILLKNode node) {
        int ret = 1;
        for (ILLKNode n : node.children())
            ret += countNodes(n);
        return ret;
    }

    public static int countNodes(ILLKNode node, int type) {
        int ret = 0;
        for (ILLKNode n : node.children()) {
            if (n.getType() == type)
                ++ret;
            ret += countNodes(n, type);
        }
        return ret;
    }

    public static int countNodes(ILLKNode node, int[] types) {
        int ret = 0;
        for (ILLKNode n : node.children()) {
            int type = n.getType();
            for (int t : types) {
                if (type == t) {
                    ++ret;
                    break;
                }
            }
            ret += countNodes(n, types);
        }
        return ret;
    }

    public static String simpleFilename(String path) {
        if (path == null)
            return "";
        int index = path.lastIndexOf('/');
        if (index >= 0)
            return path.substring(index + 1);
        return path;
    }

    //////////////////////////////////////////////////////////////////////

    public static void dumpTokens(PrintWriter out, ILLKNode node) {
        int end = node.getEndOffset();
        for (ILLKToken t = node.getFirstToken(); t.getOffset() < end; t = t.getNext()) {
            for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext())
                out.println(s.toString());
            out.println(t.toString());
        }
        out.flush();
    }

    public static void dumpTokens(PrintWriter out, ILLKNode node, String[] tokennames) {
        int end = node.getEndOffset();
        for (ILLKToken t = node.getFirstToken(); t.getOffset() < end; t = t.getNext()) {
            for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext())
                out.println(toString(tokennames, s));
            out.println(toString(tokennames, t));
        }
        out.flush();
    }

    public static String sdumpTokens(ILLKToken t, String[] names) {
        StringBuilder ret = new StringBuilder();
        for (; t != null; t = t.getNext()) {
            for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext()) {
                ret.append('\t');
                ret.append(toString(names, s));
                ret.append('\n');
            }
            ret.append(toString(names, t));
            ret.append('\n');
        }
        return ret.toString();
    }

    public static String sdumpXml(ILLKNode unit, String msg, String[] names) {
        FormatBuilder buf = new FormatBuilder(2, "", "  ");
        buf.flushLine(msg);
        dumpXml(unit, buf, true, names);
        buf.flushLine();
        return buf.toString();
    }

    public static String sdumpXml(ILLKNode node, boolean withtext, String[] names) {
        if (node == null)
            return null;
        FormatBuilder buf = new FormatBuilder(2, "", "  ");
        dumpXml(node, buf, withtext, true, names);
        buf.flushLine();
        return buf.toString();
    }

    public static void dumpXml(ILLKNode node, FormatBuilder buf, boolean withtext, String[] names) {
        dumpXml(node, buf, withtext, true, names);
    }

    public static void dumpXml(
        ILLKNode node, FormatBuilder buf, boolean withtext, boolean preserve, String[] names) {
        buf.append('<');
        String name = (names != null ? names[node.getType()] : String.valueOf(node.getType()));
        buf.append(name);
        if (preserve)
            buf.append(" xml:space=\"preserve\"");
        if (node.getModifiers() != 0)
            buf.append(" modifier=\"0x" + Integer.toHexString(node.getModifiers()) + "\"");
        buf.append(" range=\"" + node.getLocationString() + "\"");
        boolean hastext = withtext && node.getText() != null;
        if (hastext) {
            buf.append(" text=\"" + escAttrValue(node.getText()) + "\"");
        }
        if (node.getData() != null) {
            buf.append(" data=\"" + escAttrValue(node.getData().toString()) + "\"");
        }
        if (node.getFirst() == null) {
            buf.append("/>");
            buf.flushLine();
            return;
        }
        buf.append(">");
        buf.flushLine();
        buf.indent();
        for (ILLKNode child = node.getFirst(); child != null; child = child.getNext()) {
            dumpXml(child, buf, withtext, false, names);
        }
        buf.unIndent();
        buf.append("</");
        buf.append(name);
        buf.append(">");
        buf.flushLine();
    }

    private static String toString(String[] names, ILLKToken t) {
        int type = t.getType();
        if (names == null || type >= names.length)
            return "TOKEN_" + t.getType();
        return names[type];
    }

    //////////////////////////////////////////////////////////////////////

    public static int[] differIgnoringWhitespaces(
        final CharSequence str1, final CharSequence str2, final IIntList offsets, final boolean ignorecase) {
        return differIgnoringWhitespaces(
            str1, str2, offsets, ignorecase, DefaultNameDetector.getSingleton(), DefaultSpaceUtil.getSingleton());
    }

    /**
     * Find first offset of the difference between the two string ignoring all
     * whitespaces.
     *
     * @param offsets If not null, return list of all whitespace boundaries pairs
     *                that differ { start1, end1, start2, end2 }.
     * @return Offsets of the first difference in str1 and str2 respectively,
     * null if strings are same.
     */
    public static int[] differIgnoringWhitespaces(
        final CharSequence str1,
        final CharSequence str2,
        final IIntList offsets,
        final boolean ignorecase,
        final INameDetector namer,
        final ISpaceDetector spacer) {
        char c1 = 0;
        char c2 = 0;
        int end1 = 0;
        int end2 = 0;
        int wend1 = 0;
        int wend2 = 0;
        final int len1 = str1.length();
        final int len2 = str2.length();
        int start1, start2;
        boolean a1 = false;
        boolean a2 = false;
        FAIL:
        {
            for (; end1 < len1 && end2 < len2; ) {
                start1 = end1;
                start2 = end2;
                while (end1 < len1 && spacer.isWhitespace(c1 = str1.charAt(end1))) {
                    ++end1;
                }
                while (end2 < len2 && spacer.isWhitespace(c2 = str2.charAt(end2))) {
                    ++end2;
                }
                if (offsets != null && (end1 != start1 || end2 != start2)) {
                    if (!match(str1, start1, end1, str2, start2, end2)) {
                        offsets.add(start1);
                        offsets.add(end1);
                        offsets.add(start2);
                        offsets.add(end2);
                    }
                }
                if (end1 >= len1 || end2 >= len2) {
                    break;
                }
                wend1 = end1 + 1;
                wend2 = end2 + 1;
                while (wend1 < len1 && !spacer.isWhitespace(str1.charAt(wend1))) {
                    ++wend1;
                }
                final int wlen1 = wend1 - end1;
                int len = wlen1;
                while ((--len > 0) && wend2 < len2 && !spacer.isWhitespace(str2.charAt(wend2))) {
                    ++wend2;
                }
                final int wlen2 = wend2 - end2;
                if (wlen2 < wlen1) {
                    wend1 = end1 + wlen2;
                }
                if (!match(str1, end1, wend1, str2, end2, wend2)) {
                    if (!ignorecase) {
                        break FAIL;
                    }
                    if (!matchIgnoreCase(str1, end1, wend1, str2, end2, wend2)) {
                        break FAIL;
                    }
                    if (offsets != null) {
                        offsets.add(end1);
                        offsets.add(wend1);
                        offsets.add(end2);
                        offsets.add(wend2);
                    }
                }
                if (namer != null) {
                    if (end1 == start1 && end2 != start2) {
                        if (a1 && namer.isNamePart(c1)) {
                            break FAIL;
                        }
                    }
                    if (end2 == start2 && end1 != start1) {
                        if (a2 && namer.isNamePart(c2)) {
                            break FAIL;
                        }
                    }
                    a1 = namer.isNamePart(str1.charAt(wend1 - 1));
                    a2 = namer.isNamePart(str2.charAt(wend2 - 1));
                }
                end1 = wend1;
                end2 = wend2;
                if (wend1 >= len1 || wend2 >= len2) {
                    break;
                }
            }
            start1 = end1;
            start2 = end2;
            end1 = spacer.skipWhitespaces(str1, end1, len1);
            end2 = spacer.skipWhitespaces(str2, end2, len2);
            if (offsets != null && (end1 != start1 || end2 != start2)) {
                if (!match(str1, start1, end1, str2, start2, end2)) {
                    offsets.add(start1);
                    offsets.add(end1);
                    offsets.add(start2);
                    offsets.add(end2);
                }
            }
            if (end1 == len1 && end2 == len2) {
                return null;
            }
        }
        return new int[]{end1, end2};
    }

    public static boolean match(
        final CharSequence str1,
        final int start1,
        final int end1,
        final CharSequence str2,
        final int start2,
        final int end2) {
        final int len = end1 - start1;
        if (end2 - start2 != len) {
            return false;
        }
        for (int i = len - 1; i >= 0; --i) {
            if (str1.charAt(start1 + i) != str2.charAt(start2 + i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean match(
        final CharSequence str1, final int start1, final int end1, final CharSequence str2, final int start2) {
        final int len = end1 - start1;
        if (start2 + len > str2.length()) {
            return false;
        }
        for (int i = len - 1; i >= 0; --i) {
            if (str1.charAt(start1 + i) != str2.charAt(start2 + i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchIgnoreCase(
        final CharSequence str1,
        final int start1,
        final int end1,
        final CharSequence str2,
        final int start2,
        final int end2) {
        final int len = end1 - start1;
        if (end2 - start2 != len) {
            return false;
        }
        char c1, c2;
        for (int i = len - 1; i >= 0; --i) {
            c1 = str1.charAt(start1 + i);
            c2 = str2.charAt(start2 + i);
            if (c1 != c2 && c1 != Character.toLowerCase(c2) && c1 != Character.toUpperCase(c2)) {
                return false;
            }
        }
        return true;
    }

    public static CharSequence escAttrValue(final CharSequence value) {
        return escAttrValue(value, '"');
    }

    /**
     * Escape XML reserved characters in attribute values.
     */
    public static CharSequence escAttrValue(final CharSequence value, final char delim) {
        if (value == null) {
            return "";
        }
        final StringBuilder ret = new StringBuilder();
        escAttrValue(ret, value, delim);
        return ret;
    }

    public static void escAttrValue(final StringBuilder ret, final CharSequence value, final char delim) {
        for (int i = 0, len = value.length(); i < len; i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '&':
                    if (isXmlEntityRef(value, i)) {
                        ret.append(c);
                    } else {
                        ret.append("&amp;");
                    }
                    break;
                case '<':
                    ret.append("&lt;");
                    break;
                case '>':
                    ret.append("&gt;");
                    break;
                case '\'':
                    if (c == delim) {
                        ret.append("&apos;");
                    } else {
                        ret.append(c);
                    }
                    break;
                case '"':
                    if (c == delim) {
                        ret.append("&quot;");
                    } else {
                        ret.append(c);
                    }
                    break;
                case '%':
                    ret.append("&#37;");
                    break;
                default:
                    ret.append(c);
            }
        }
    }

    /**
     * @return true if substring started at 'start' is a XML entity reference.
     */
    public static boolean isXmlEntityRef(final CharSequence str, int start) {
        final int len = str.length();
        if (start >= len || str.charAt(start) != '&') {
            return false;
        }
        if (++start >= len) {
            return false;
        }
        if (str.charAt(start) == '#') {
            return isXmlCharRefPart(str, start + 1);
        }
        int i = start;
        if (!isNameStart(str.charAt(i))) {
            return false;
        }
        for (++i; i < len; ++i) {
            if (!isName(str.charAt(i))) {
                break;
            }
        }
        return (i > start && i < len && str.charAt(i) == ';');
    }

    /**
     * NOTE: No well-formness check for the referenced char.
     *
     * @param start Index to char after "&#".
     */
    public static boolean isXmlCharRefPart(final CharSequence str, int start) {
        final int len = str.length();
        if (start >= len) {
            return false;
        }
        char c;
        if (str.charAt(start) == 'x') {
            ++start;
            int i = start;
            for (; i < len; ++i) {
                c = str.charAt(i);
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                    continue;
                }
                break;
            }
            return (i > start && i < len && str.charAt(i) == ';');
        }
        int i = start;
        for (; i < len; ++i) {
            c = str.charAt(i);
            if (c < '0' || c > '9') {
                break;
            }
        }
        return (i > start && i < len && str.charAt(i) == ';');
    }

    public static boolean isXml10Char(final char c) {
        return (c == '\t'
            || c == '\r'
            || c == '\n'
            || c >= 0x20 && c <= 0xD7FF
            || c >= 0xe000 && c < 0xFFFD
            || c >= 0x10000 && c <= 0x10FFFF);
    }

    public static boolean isNameStart(final int c) {
        return c == ':'
            || c >= 'A' && c <= 'Z'
            || c == '_'
            || c >= 'a' && c <= 'z'
            || c >= '\u00C0' && c <= '\u00D6'
            || c >= '\u00D8' && c <= '\u00F6'
            || c >= '\u00F8' && c <= '\u02FF'
            || c >= '\u0370' && c <= '\u037D'
            || c >= '\u037F' && c <= '\u1FFF'
            || c >= '\u200C' && c <= '\u200D'
            || c >= '\u2070' && c <= '\u218F'
            || c >= '\u2C00' && c <= '\u2FEF'
            || c >= '\u3001' && c <= '\uD7FF'
            || c >= '\uF900' && c <= '\uFDCF'
            || c >= '\uFDF0' && c <= '\uFFFD'
            ;
    }

    public static boolean isName(final int c) {
        return isNameStart(c)
            || c == '-'
            || c == '.'
            || c >= '0' && c <= '9'
            || c == '\u00B7'
            || c >= '\u0300' && c <= '\u036F'
            || c >= '\u203F' && c <= '\u2040';
    }

}
