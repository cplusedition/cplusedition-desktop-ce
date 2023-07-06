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
package sf.llk.share.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import kotlin.text.Charsets;
import sf.llk.share.support.IByteSequence;
import sf.llk.share.support.IIntList;
public class LLKShareUtil {

    public static final String LB = System.getProperty("line.separator");
    public static final String SEP = System.getProperty("file.separator");

    public static boolean optEquals(final CharSequence a, final CharSequence b) {
        return equals(a, b);
    }

    public static boolean optEquals(final Integer a, final Integer b) {
        if (a == null) return b == null;
        if (b == null) return false;
        return a.equals(b);
    }

    public static boolean equals(final CharSequence a, final CharSequence b) {
        if (a == null) return b == null;
        if (b == null) return false;
        final int len = a.length();
        if (b.length() != len) return false;
        for (int index = 0; index < len; ++index) {
            if (a.charAt(index) != b.charAt(index)) return false;
        }
        return true;
    }

    public static boolean equalsIgnoreCase(final CharSequence a, final CharSequence b) {
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }
        final int alen = a.length();
        final int blen = b.length();
        if (alen != blen) {
            return false;
        }
        for (int i = 0; i < alen; ++i) {
            final char ca = a.charAt(i);
            final char cb = b.charAt(i);
            if (ca != cb && ca != Character.toLowerCase(cb) && ca != Character.toUpperCase(cb)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Index of last char that is not one of the given characters + 1;
     */
    public static int trailingChars(final CharSequence buf, int end, final char... chars) {
        char c;
        NEXT:
        while (--end >= 0) {
            c = buf.charAt(end);
            for (final char cc : chars) {
                if (c == cc) {
                    continue NEXT;
                }
            }
            break;
        }
        return end + 1;
    }

    public static int trailingChars(final CharSequence buf, final char... chars) {
        return trailingChars(buf, buf.length(), chars);
    }

    public static int indexOf(final char delim, final CharSequence buf) {
        return indexOf(delim, buf, 0, buf.length());
    }

    /**
     * @return Index of first occurence of delim or -1.
     */
    public static int indexOf(final char delim, final CharSequence buf, final int start, final int end) {
        for (int i = start; i < end; ++i) {
            if (buf.charAt(i) == delim) {
                return i;
            }
        }
        return -1;
    }

    public static String stringOf(final char c, int n) {
        final StringBuilder b = new StringBuilder();
        while (--n >= 0) {
            b.append(c);
        }
        return b.toString();
    }

    public static String spacesOf(int spaces) {
        return stringOf(' ', spaces);
    }

    /**
     * Count number of occurance of a char in a string.
     */
    public static int count(final char c, final CharSequence str) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                ret++;
            }
        }
        return ret;
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

    public static String join(final String sep, final String[] a) {
        return join(sep, a, 0, a.length);
    }

    public static String join(final String sep, final String[] a, final int start, final int end) {
        final int len = end - start;
        if (len == 0) {
            return "";
        }
        if (len == 1) {
            return a[0];
        }
        final StringBuilder b = new StringBuilder();
        for (int i = start; i < end; ++i) {
            if (i != 0) {
                b.append(sep);
            }
            b.append(a[i]);
        }
        return b.toString();
    }

    @SafeVarargs
    public static <T> List<T> toList(final T... a) {
        final List<T> ret = new ArrayList<>(a.length);
        for (final T s : a) {
            ret.add(s);
        }
        return ret;
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

    public static String optToString(Object a) {
        return a == null ? null : a.toString();
    }

    public static int replaceAll(final StringBuilder buf, final char from, final char to) {
        return replaceAll(buf, 0, buf.length(), from, to);
    }

    public static int replaceAll(final StringBuilder buf, int start, final int end, final char from, final char to) {
        int count = 0;
        for (; start < end; ++start) {
            if (buf.charAt(start) == from) {
                buf.setCharAt(start, to);
                ++count;
            }
        }
        return count;
    }

    public static List<String> split(final String str, final String delims) {
        return split(new StringTokenizer(str, delims));
    }

    public static List<String> split(final StringTokenizer tok) {
        final List<String> ret = new ArrayList<>();
        while (tok.hasMoreTokens()) {
            ret.add(tok.nextToken());
        }
        return ret;
    }

    /**
     * Read input file as raw chars using UTF_8 encoding.
     */
    public static char[] asChars(final File file) throws IOException {
        return Charsets.UTF_8.decode(ByteBuffer.wrap(asBytes(file))).array();
    }

    /**
     * Read input file as raw bytes.
     */
    public static byte[] asBytes(final File file) throws IOException {
        FileInputStream in = null;
        try {
            return asBytes(in = new FileInputStream(file));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static int BUFSIZE = 16 * 1024;

    public static byte[] asBytes(final InputStream r) throws IOException {
        final List<byte[]> list = new ArrayList<>();
        int size = asBytes(list, r);
        final byte[] ret = new byte[size];
        int offset = 0;
        for (final byte[] a : list) {
            System.arraycopy(a, 0, ret, offset, size > BUFSIZE ? BUFSIZE : size);
            offset += BUFSIZE;
            size -= BUFSIZE;
        }
        return ret;
    }

    private static int asBytes(final List<byte[]> list, final InputStream r) throws IOException {
        byte[] b = new byte[BUFSIZE];
        int start = 0;
        int n;
        while ((n = r.read(b, start, BUFSIZE - start)) != -1) {
            start += n;
            if (start == BUFSIZE) {
                list.add(b);
                b = new byte[BUFSIZE];
                start = 0;
            }
        }
        final int size = list.size() * BUFSIZE + start;
        if (start > 0) {
            list.add(b);
        }
        return size;
    }

    /**
     * Return name part of a file path. eg. dir1/dir2/file.java return file.java
     */
    public static String fileName(String path) {
        final int index = path.lastIndexOf(SEP);
        if (index >= 0) {
            path = path.substring(index + 1);
            if (path.length() == 0) {
                return null;
            }
        }
        return path;
    }

    public static void printHexC(
        final PrintStream out,
        final int perline,
        final String indent,
        final String offsetformat,
        final String valueformat,
        final String startcomment,
        final String endcomment,
        final byte[] a,
        final int start,
        final int end) {
        printHexC(
            new PrintWriter(out), perline, indent, offsetformat, valueformat, startcomment, endcomment, a, start, end);
    }

    /**
     * Similar to printHexDump, but allow custom PERLINE, indent, comment
     * separator and no linefeed on empty input.
     */
    public static void printHexC(
        final PrintWriter out,
        final int perline,
        final String indent,
        final String offsetformat,
        final String valueformat,
        final String startcomment,
        final String endcomment,
        final byte[] a,
        final int start,
        final int end) {
        final int len = a.length;
        if (len == 0 || start == end) {
            return;
        }
        final String padding = spacesOf(String.format(valueformat, 0).length());
        int xstart = (start / perline) * perline;
        final int xend = (((end - 1) / perline) + 1) * perline;
        for (; xstart < xend; xstart += perline) {
            if (indent != null) {
                out.print(indent);
            }
            if (offsetformat != null) {
                out.print(String.format(offsetformat, xstart));
            }
            for (int i = 0; i < perline; ++i) {
                if (i == perline / 2) {
                    out.print(' ');
                }
                final int index = xstart + i;
                if (index >= start && index < end) {
                    out.print(String.format(valueformat, a[index]));
                } else {
                    out.print(padding);
                }
            }
            out.print(startcomment);
            for (int i = 0; i < perline; ++i) {
                final int index = xstart + i;
                if (index >= start && index < end) {
                    final int c = a[index] & 0xff;
                    if (c < 0x20 || c >= 0x7f) {
                        out.print('.');
                    } else {
                        out.print((char) c);
                    }
                } else {
                    out.print(' ');
                }
            }
            out.println(endcomment);
        }
        out.flush();
    }

    /**
     * Print bytes in java source code format.
     *
     * @param out
     * @param perline
     * @param indent
     * @param valueformat   Value format for each individual value including spacing but
     *                      and trailing comma, %1 is value.
     * @param commentformat including trailing line break, %1 is offset, %2 is printable
     *                      characters of the values.
     * @param data
     */
    public static void printHexJava(
        final PrintWriter out,
        final int perline,
        final String indent,
        final String valueformat,
        final String commentformat,
        final IByteSequence data) {
        final int size = data.size();
        if (size == 0) {
            return;
        }
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < size; ) {
            out.print(indent);
            final int offset = i;
            s.setLength(0);
            for (int l = 0; l < perline && i < size; ++l, ++i) {
                final byte v = data.byteAt(i);
                out.printf(valueformat, v);
                s.append((v >= 0x20 && v <= 0x7e) ? (char) v : '.');
            }
            if (commentformat != null) {
                out.printf(commentformat, offset, s);
            } else {
                out.println();
            }
        }
        out.flush();
    }

    public static String sprintArray(String indent, int columns, int[] a) {
        return sprintArray(indent, columns, "%1$7d", a);
    }

    public static String sprintArray(String indent, int columns, String fmt, int[] a) {
        if (a == null) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        int offset = 0;
        for (int i = 0; i < a.length; ++i) {
            offset = i % columns;
            if (offset == 0) {
                buf.append(indent);
            }
            buf.append(String.format(fmt, a[i]));
            if (offset == columns - 1) {
                buf.append("\n");
            }
        }
        if (offset != 0) {
            buf.append("\n");
        }
        return buf.toString();
    }

}
