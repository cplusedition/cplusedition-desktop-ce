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
package sf.llk.grammar.html;

public enum HtmlTag {

	UNKNOWN(REQ.YES, REQ.YES, false, true, DTD.STD),
	A,
	ABBR,
	ACRONYM,
	ADDRESS,
	APPLET(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	AREA(REQ.YES, REQ.NO, true),
	B,
	BASE(REQ.YES, REQ.NO, true),
	BASEFONT(REQ.YES, REQ.NO, true, true, DTD.LOOSE),
	BDO,
	BIG,
	BLOCKQUOTE,
	BODY(REQ.OPT, REQ.OPT, false),
	BR(REQ.YES, REQ.NO, true),
	BUTTON,
	CAPTION,
	CENTER(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	CITE,
	CODE,
	COL(REQ.YES, REQ.NO, true),
	COLGROUP(REQ.YES, REQ.OPT, false),
	DD(REQ.YES, REQ.OPT, false),
	DEL,
	DFN,
	DIR(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	DIV,
	DL,
	DT(REQ.YES, REQ.OPT, false),
	EM,
	FIELDSET,
	FONT(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	FORM,
	FRAME(REQ.YES, REQ.NO, true, false, DTD.FRAMESET),
	FRAMESET(REQ.YES, REQ.YES, false, false, DTD.FRAMESET),
	H1,
	H2,
	H3,
	H4,
	H5,
	H6,
	HEAD(REQ.OPT, REQ.OPT, false),
	HR(REQ.YES, REQ.NO, true),
	HTML(REQ.OPT, REQ.OPT, false),
	I,
	IFRAME(REQ.YES, REQ.YES, false, false, DTD.LOOSE),
	IMG(REQ.YES, REQ.NO, true),
	INPUT(REQ.YES, REQ.NO, true),
	INS,
	ISINDEX(REQ.YES, REQ.NO, true, true, DTD.LOOSE),
	KBD,
	LABEL,
	LEGEND,
	LI(REQ.YES, REQ.OPT, false),
	LINK(REQ.YES, REQ.NO, true),
	MAP,
	MENU(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	META(REQ.YES, REQ.NO, true),
	NOFRAMES(REQ.YES, REQ.YES, false, false, DTD.FRAMESET),
	NOSCRIPT(REQ.YES, REQ.YES, false),
	OBJECT,
	OL,
	OPTGROUP,
	OPTION(REQ.YES, REQ.OPT, false),
	P(REQ.YES, REQ.OPT, false),
	PARAM(REQ.YES, REQ.NO, true),
	PRE,
	Q,
	S(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	SAMP,
	SCRIPT,
	SELECT,
	SMALL,
	SPAN,
	STRIKE(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	STRONG,
	STYLE,
	SUB,
	SUP,
	TABLE,
	TBODY(REQ.OPT, REQ.OPT, false),
	TD(REQ.YES, REQ.OPT, false),
	TEXTAREA,
	TFOOT(REQ.YES, REQ.OPT, false),
	TH(REQ.YES, REQ.OPT, false),
	THEAD(REQ.YES, REQ.OPT, false),
	TITLE,
	TR(REQ.YES, REQ.OPT, false),
	TT(),
	U(REQ.YES, REQ.YES, false, true, DTD.LOOSE),
	UL,
	VAR,
	ARTICLE,
	ASIDE,
	AUDIO,
	BDI,
	CANVAS,
	DATA,
	DATALIST(REQ.YES, REQ.OPT, false),
	DETAILS,
	DIALOG,
	FIGCAPTION,
	FIGURE,
	FOOTER,
	HEADER,
	KEYGEN(REQ.YES, REQ.NO, true),
	MAIN,
	MARK,
	MENUITEM(REQ.YES, REQ.NO, true),
	METER,
	NAV,
	OUTPUT,
	PROGRESS,
	RB,
	RP,
	RT,
	RTC,
	RUBY,
	SECTION,
	SOURCE,
	SUMMARY,
	TEMPLATE,
	TIME,
	TRACK(REQ.YES, REQ.NO, true),
	VIDEO,
	XMP(REQ.YES, REQ.YES, false, true, DTD.STD),
	PLAINTEXT(REQ.YES, REQ.YES, false, true, DTD.STD),
	TERM(REQ.YES, REQ.YES, false, true, DTD.V2),
	SDFIELD(REQ.YES, REQ.YES, false, false, DTD.MS),
	EMBED(REQ.YES, REQ.NO, true, false, DTD.MS),
	NOBR(REQ.YES, REQ.YES, false, true, DTD.NETVIGATOR),
	WBR(REQ.YES, REQ.NO, true, true, DTD.NETVIGATOR),
	SPACER(REQ.YES, REQ.NO, true, true, DTD.NETVIGATOR),
	MULTICOL(REQ.YES, REQ.YES, false, true, DTD.NETVIGATOR),
	LAYER(REQ.YES, REQ.YES, false, false, DTD.NETVIGATOR),
	ILAYER(REQ.YES, REQ.YES, false, false, DTD.NETVIGATOR),
	NOLAYER(REQ.YES, REQ.YES, false, false, DTD.NETVIGATOR),
	;

	//////////////////////////////////////////////////////////////////////

	public enum REQ {
		YES, OPT, NO
	}

	public static final int NONE = 0x00;
	public static final int C_INLINE = 0x0001;
	public static final int C_BLOCK = 0x0002;
	public static final int C_ELEMENT = 0x0004;

	//////////////////////////////////////////////////////////////////////

	static {
		for (final HtmlTag tag: new HtmlTag[] {
				TT, I, B, BIG, SMALL,
				EM, STRONG, DFN, CODE, SAMP, KBD, VAR, CITE,
				ABBR,
				ACRONYM,
				A, IMG, OBJECT,
				BR, SCRIPT,
				MAP, Q, SUB, SUP, SPAN,
				BDO,
				INPUT, SELECT, TEXTAREA, LABEL,
				BUTTON,
				U, FONT, S, STRIKE,
				TERM,
				SDFIELD, EMBED,
				NOBR, WBR, SPACER, ILAYER,
			}) {
            tag.setModifier(C_INLINE);
        }
		for (final HtmlTag tag: new HtmlTag[] {
				UNKNOWN,
				P, DL, DIV, NOSCRIPT, BLOCKQUOTE, FORM, HR, TABLE,
				FIELDSET, ADDRESS,
				H1, H2, H3, H4, H5, H6,
				UL, OL,
				PRE, SCRIPT, STYLE,
				XMP, PLAINTEXT,
				MULTICOL, LAYER, NOLAYER,
			}) {
            tag.setModifier(C_BLOCK);
        }
		for (final HtmlTag tag: new HtmlTag[] {
				UL, OL, DL,
				SELECT, OPTGROUP,
				TABLE, THEAD, TFOOT, TBODY, COLGROUP, TR,
			}) {
            tag.setModifier(C_ELEMENT);
        }
	}

	//////////////////////////////////////////////////////////////////////

	int modifier;
	REQ startTag;
	REQ endTag;
	boolean isEmpty;
	boolean isDeprecated;
	DTD variant;

	//////////////////////////////////////////////////////////////////////

	HtmlTag() {
		this(REQ.YES, REQ.YES, false, false, DTD.STD);
	}

	HtmlTag(final REQ stag, final REQ etag, final boolean empty) {
		this(stag, etag, empty, false, DTD.STD);
	}

	HtmlTag(final REQ stag, final REQ etag, final boolean empty, final boolean depreated, final DTD dtd) {
		this.startTag = stag;
		this.endTag = etag;
		this.isEmpty = empty;
		this.isDeprecated = depreated;
		this.variant = dtd;
	}

	//////////////////////////////////////////////////////////////////////

	public static HtmlTag get(final String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (final Exception e) {
			return UNKNOWN;
		}
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public boolean optionalEndTag() {
		return startTag == REQ.YES && endTag == REQ.OPT;
	}

	public void setModifier(final int flag) {
		modifier |= flag;
	}

	public boolean hasModifier(final int flag) {
		return (modifier & flag) != 0;
	}

	public boolean isBlock() {
		return (modifier & C_BLOCK) != 0;
	}

	public boolean isInline() {
		return (modifier & C_INLINE) != 0;
	}

	/** @return true if this element with optional end tag ends by the given element. */
	public boolean endOptional(final HtmlTag by, final boolean isendtag) {
		if (isendtag && by == this) {
            return false;
        }
		switch (this) {
		case COLGROUP:
			if (by == COLGROUP
				|| by == TD
				|| by == TR
				|| by == TFOOT
				|| by == THEAD
				|| by == TABLE
				|| by == TBODY) {
                return true;
            }
			break;
		case DD:
		case DT:
			if (by == DD || by == DT || (isendtag && by == DL)) {
                return true;
            }
			break;
		case LI:
			if (by == LI || (isendtag && (by == UL || by == OL))) {
                return true;
            }
			break;
		case OPTION:
			if (by == OPTION || by == OPTGROUP || by == SELECT) {
                return true;
            }
			break;
		case P:
			if (!by.hasModifier(C_INLINE)) {
                return true;
            }
			break;
		case TD:
		case TH:
			if (by == TD || by == TH || by == TFOOT || by == THEAD || by == TR) {
                return true;
            }
			if (by == TABLE) {
                return isendtag;
            }
			break;
		case TFOOT:
		case THEAD:
		case TR:
			if (by == TFOOT || by == THEAD || by == TR) {
                return true;
            }
			if (by == TABLE) {
                return isendtag;
            }
			break;
		case TBODY:
			if (by == TABLE) {
                return isendtag;
            }
			break;
		default :
		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////
}
