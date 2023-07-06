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
package sf.llk.grammar.html.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import sf.llk.grammar.html.HtmlTag;
import sf.llk.share.util.LLKShareUtil;

public class HtmlFormatOptions {

    ////////////////////////////////////////////////////////////////////////

    public static final String DEF_NOINDENTS = "html,head,body,table,thead,tbody,tfoot";

    public enum Case {
        PRESERVE, UPPER, LOWER
    }

    public enum Style {
        INLINE(false, false, false),
        BREAKAFTER(false, false, true),
        PARAGRAPH(true, false, true),
        BLOCK(true, true, true),
        BLOCK1(true, true, true),
        INDENTED(true, true, true),
        PREFORMATTED(true, false, true);

        private boolean breakBefore;
        private boolean breakContent;
        private boolean breakAfter;

        Style(final boolean before, final boolean content, final boolean after) {
            this.breakBefore = before;
            this.breakContent = content;
            this.breakAfter = after;
        }

        /** @return true if should ensure line break before start tag. */
        public boolean breakBefore() {
            return breakBefore;
        }

        /** @return true if should ensure line break before and after content. */
        public boolean breakContent() {
            return breakContent;
        }

        /** @return true if should ensure line break after end tag. */
        public boolean breakAfter() {
            return breakAfter;
        }
    }

    //////////////////////////////////////////////////////////////////////

    public boolean isFixing = false;
    public boolean isCompact = false;
    public boolean warnImplict = false;

    public int lineWidth = 120;
    public int tabWidth = 4;
    public String lineBreak = "\n";
    public String tabString = "\t";

    public boolean indentAttributeValue = true;
    public Case convertTagCase = Case.LOWER;
    public Case convertAttrCase = Case.LOWER;
    public Set<String> noIndents = new HashSet<String>();

    private final Map<HtmlTag, Style> formatStyles = new HashMap<HtmlTag, Style>();
    {
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.TT, HtmlTag.I, HtmlTag.B, HtmlTag.BIG, HtmlTag.SMALL,
                HtmlTag.EM, HtmlTag.STRONG, HtmlTag.DFN, HtmlTag.CODE, HtmlTag.SAMP,
                HtmlTag.KBD, HtmlTag.VAR, HtmlTag.CITE,
                HtmlTag.ABBR, HtmlTag.ACRONYM,
                HtmlTag.A, HtmlTag.IMG,
                HtmlTag.Q, HtmlTag.SUB, HtmlTag.SUP, HtmlTag.SPAN,
                HtmlTag.U, HtmlTag.FONT, HtmlTag.S, HtmlTag.STRIKE,
                HtmlTag.BDO,
                HtmlTag.TERM,
                HtmlTag.SDFIELD, HtmlTag.EMBED,
                HtmlTag.NOBR, HtmlTag.SPACER,
                HtmlTag.DATALIST, HtmlTag.KEYGEN, HtmlTag.MENUITEM, HtmlTag.TRACK,
            }) {
            checkUnique(tag, Style.INLINE);
        }
        for (final HtmlTag tag: new HtmlTag[] { HtmlTag.BR, HtmlTag.WBR, }) {
            checkUnique(tag, Style.BREAKAFTER);
        }
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.PARAM, HtmlTag.OPTION, HtmlTag.INPUT, HtmlTag.SELECT,
                HtmlTag.TEXTAREA, HtmlTag.LABEL, HtmlTag.BUTTON,
                HtmlTag.ADDRESS, HtmlTag.CAPTION, HtmlTag.LEGEND,
                HtmlTag.CENTER,
                HtmlTag.DEL, HtmlTag.INS,
                HtmlTag.DD, HtmlTag.DT, HtmlTag.LI,
            }) {
            checkUnique(tag, Style.PARAGRAPH);
        }
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.TD, HtmlTag.TH,
                HtmlTag.COL,
                HtmlTag.H1, HtmlTag.H2, HtmlTag.H3, HtmlTag.H4,
                HtmlTag.H5, HtmlTag.H6,
                HtmlTag.P,
                HtmlTag.BASE, HtmlTag.TITLE, HtmlTag.BASEFONT, HtmlTag.DIR,
                HtmlTag.NOSCRIPT, HtmlTag.NOFRAMES, HtmlTag.APPLET, HtmlTag.AREA,
                HtmlTag.ISINDEX, HtmlTag.LINK, HtmlTag.MENU, HtmlTag.META,
                HtmlTag.NOLAYER,
                HtmlTag.ARTICLE, HtmlTag.ASIDE, HtmlTag.AUDIO, HtmlTag.BDI, HtmlTag.CANVAS, HtmlTag.DATA,
                HtmlTag.DETAILS, HtmlTag.DIALOG,
                HtmlTag.FIGCAPTION, HtmlTag.FIGURE, HtmlTag.FOOTER, HtmlTag.HEADER, HtmlTag.MAIN, HtmlTag.MARK,
                HtmlTag.METER, HtmlTag.NAV,
                HtmlTag.OUTPUT, HtmlTag.PROGRESS, HtmlTag.RB, HtmlTag.RP, HtmlTag.RT, HtmlTag.RTC, HtmlTag.RUBY,
                HtmlTag.SECTION,
                HtmlTag.SOURCE, HtmlTag.SUMMARY, HtmlTag.TEMPLATE, HtmlTag.TIME, HtmlTag.VIDEO,
            }) {
            checkUnique(tag, Style.BLOCK);
        }
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.FRAMESET, HtmlTag.FIELDSET, HtmlTag.OPTGROUP, HtmlTag.MAP,
                HtmlTag.COLGROUP, HtmlTag.TR, HtmlTag.THEAD, HtmlTag.TBODY, HtmlTag.TFOOT,
                HtmlTag.MULTICOL,
            }) {
            checkUnique(tag, Style.BLOCK1);
        }
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.HTML, HtmlTag.HEAD, HtmlTag.BODY,
                HtmlTag.FRAME, HtmlTag.IFRAME, HtmlTag.FORM, HtmlTag.TABLE,
                HtmlTag.BLOCKQUOTE, HtmlTag.DIV,
                HtmlTag.DL, HtmlTag.UL, HtmlTag.OL,
                HtmlTag.HR,
                HtmlTag.SCRIPT, HtmlTag.STYLE,
                HtmlTag.OBJECT,
                HtmlTag.LAYER, HtmlTag.ILAYER,
            }) {
            checkUnique(tag, Style.INDENTED);
        }
        for (final HtmlTag tag:
            new HtmlTag[] {
                HtmlTag.UNKNOWN,
                HtmlTag.PRE,
                HtmlTag.XMP, HtmlTag.PLAINTEXT
            }) {
            checkUnique(tag, Style.PREFORMATTED);
        }

        //////////////////////////////////////////////////////////////////////

        final StringBuilder none = new StringBuilder();
        for (final HtmlTag tag: HtmlTag.values()) {
            if (formatStyles.get(tag) == null) {
                none.append(" " + tag);
            }
        }
        if (none.length() != 0) {
            throw new AssertionError("Tag must has a formatting modifier:\n\t" + none.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public HtmlFormatOptions() {
        this(DEF_NOINDENTS);
    }

    public HtmlFormatOptions(final String noindents) {
        setNoIndents(noindents);
    }

    public HtmlFormatOptions(final int indentwidth) {
        setIndentWidth(indentwidth);
    }

    ////////////////////////////////////////////////////////////////////////

    public void setIndentWidth(final int width) {
        tabWidth = width;
        tabString = LLKShareUtil.spacesOf(width);
    }

    public void setNoIndents(String noindents) {
        noIndents.clear();
        if (noindents == null || noindents.length() == 0) {
            return;
        }
        if (noindents.equals("defaults")) {
            noindents = DEF_NOINDENTS;
        }
        final StringTokenizer tok = new StringTokenizer(noindents, " ,\t\n\r");
        while (tok.hasMoreTokens()) {
            noIndents.add(tok.nextToken().toUpperCase());
    }}

    public void setParagraphStyleOveride(final String tags) {
        if (tags == null || tags.length() == 0) {
            return;
        }
        final StringTokenizer tok = new StringTokenizer(tags, " ,\t\n\r");
        while (tok.hasMoreTokens()) {
            final HtmlTag tag = HtmlTag.get(tok.nextToken().toUpperCase());
            if (tags != null && tag != HtmlTag.UNKNOWN) {
                formatStyles.put(tag, Style.PARAGRAPH);
    }}}

    public void setBlockStyleOveride(final String tags) {
        if (tags == null || tags.length() == 0) {
            return;
        }
        final StringTokenizer tok = new StringTokenizer(tags, " ,\t\n\r");
        while (tok.hasMoreTokens()) {
            final HtmlTag tag = HtmlTag.get(tok.nextToken().toUpperCase());
            if (tags != null && tag != HtmlTag.UNKNOWN) {
                formatStyles.put(tag, Style.BLOCK);
    }}}

    public void setIndentedStyleOveride(final String tags) {
        if (tags == null || tags.length() == 0) {
            return;
        }
        final StringTokenizer tok = new StringTokenizer(tags, " ,\t\n\r");
        while (tok.hasMoreTokens()) {
            final HtmlTag tag = HtmlTag.get(tok.nextToken().toUpperCase());
            if (tags != null && tag != HtmlTag.UNKNOWN) {
                formatStyles.put(tag, Style.INDENTED);
    }}}

    public boolean isNoIndentTag(final String tag) {
        return noIndents.contains(tag.toUpperCase());
    }

    public Style getStyle(final HtmlTag tag) {
        return formatStyles.get(tag);
    }

    ////////////////////////////////////////////////////////////////////////

    private void checkUnique(final HtmlTag tag, final Style style) {
        final Style old = formatStyles.put(tag, style);
        if (old != null) {
            throw new AssertionError(
                "Format style for tag is already defined: tag=" + tag + ", old=" + old + ", new=" + style);
    }}

    ////////////////////////////////////////////////////////////////////////
}
