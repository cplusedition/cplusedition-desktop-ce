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

import java.util.TreeSet;

public enum Attributes {

	UNKNOWN(),
	ABBR(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	ACCEP_CHARSET(new HtmlTag[] { HtmlTag.FORM }),
	ACCEPT(new HtmlTag[] { HtmlTag.INPUT }),
	ACCESSKEY(new HtmlTag[] { HtmlTag.A, HtmlTag.AREA, HtmlTag.BUTTON, HtmlTag.INPUT, HtmlTag.LABEL, HtmlTag.LEGEND, HtmlTag.TEXTAREA }),
	ACTION(new HtmlTag[] { HtmlTag.FORM }),
	ALIGN(
		new HtmlTag[] {
			HtmlTag.CAPTION,
			HtmlTag.APPLET, HtmlTag.IFRAME, HtmlTag.IMG, HtmlTag.INPUT,
			HtmlTag.OBJECT,
			HtmlTag.LEGEND,
			HtmlTag.TABLE,
			HtmlTag.HR,
			HtmlTag.DIV, HtmlTag.H1, HtmlTag.H2, HtmlTag.H3, HtmlTag.H4, HtmlTag.H5,
			HtmlTag.H6,
			HtmlTag.COL, HtmlTag.COLGROUP, HtmlTag.TBODY, HtmlTag.TD, HtmlTag.TFOOT, HtmlTag.TH,
			HtmlTag.THEAD, HtmlTag.TR
		}),
	ALINK(new HtmlTag[] { HtmlTag.BODY }),
	ALT(
		new HtmlTag[] {
			HtmlTag.APPLET,
			HtmlTag.AREA, HtmlTag.IMG,
			HtmlTag.INPUT
		}),
	ARCHIVE(
		new HtmlTag[] {
			HtmlTag.OBJECT,
			HtmlTag.APPLET
		}),
	AXIS(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	BACKGROUND(new HtmlTag[] { HtmlTag.BODY }),
	BGCOLOR(
		new HtmlTag[] {
			HtmlTag.TABLE,
			HtmlTag.TR,
			HtmlTag.TD, HtmlTag.TH,
			HtmlTag.BODY
		}),
	BORDER(
		new HtmlTag[] {
			HtmlTag.IMG, HtmlTag.OBJECT,
			HtmlTag.TABLE
		}),
	CELLPADDING(new HtmlTag[] { HtmlTag.TABLE }),
	CELLSPACING(new HtmlTag[] { HtmlTag.TABLE }),
	CHAR(new HtmlTag[] { HtmlTag.COL, HtmlTag.COLGROUP, HtmlTag.TBODY, HtmlTag.TD, HtmlTag.TFOOT, HtmlTag.TH, HtmlTag.THEAD, HtmlTag.TR }),
	CHAROFF(new HtmlTag[] { HtmlTag.COL, HtmlTag.COLGROUP, HtmlTag.TBODY, HtmlTag.TD, HtmlTag.TFOOT, HtmlTag.TH, HtmlTag.THEAD, HtmlTag.TR }),
	CHARSET(new HtmlTag[] { HtmlTag.A, HtmlTag.LINK, HtmlTag.SCRIPT }),
	CHECKED(new HtmlTag[] { HtmlTag.INPUT }),
	CITE(
		new HtmlTag[] {
			HtmlTag.BLOCKQUOTE, HtmlTag.Q,
			HtmlTag.DEL, HtmlTag.INS
		}),
	CLASS(
		new HtmlTag[] {
			HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.HEAD, HtmlTag.HTML, HtmlTag.META, HtmlTag.PARAM,
			HtmlTag.SCRIPT, HtmlTag.STYLE, HtmlTag.TITLE
		},
		false),
	CLASSID(new HtmlTag[] { HtmlTag.OBJECT }),
	CLEAR(new HtmlTag[] { HtmlTag.BR }),
	CODE(new HtmlTag[] { HtmlTag.APPLET }),
	CODEBASE(
		new HtmlTag[] {
			HtmlTag.OBJECT,
			HtmlTag.APPLET
		}),
	CODETYPE(new HtmlTag[] { HtmlTag.OBJECT }),
	COLOR(new HtmlTag[] { HtmlTag.BASEFONT, HtmlTag.FONT }),
	COLS(
		new HtmlTag[] {
			HtmlTag.FRAMESET,
			HtmlTag.TEXTAREA
		}),
	COLSPAN(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	COMPACT(new HtmlTag[] { HtmlTag.DIR, HtmlTag.MENU, HtmlTag.DL, HtmlTag.OL, HtmlTag.UL }),
	CONTENT(new HtmlTag[] { HtmlTag.META }),
	COORDS(
		new HtmlTag[] {
			HtmlTag.AREA,
			HtmlTag.A
		}),
	DATA(new HtmlTag[] { HtmlTag.OBJECT }),
	DATETIME(new HtmlTag[] { HtmlTag.DEL, HtmlTag.INS }),
	DECLARE(new HtmlTag[] { HtmlTag.OBJECT }),
	DEFER(new HtmlTag[] { HtmlTag.SCRIPT }),
	DIR(
		new HtmlTag[] {
			HtmlTag.APPLET, HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.BDO, HtmlTag.BR, HtmlTag.FRAME,
			HtmlTag.FRAMESET, HtmlTag.HR, HtmlTag.IFRAME, HtmlTag.PARAM, HtmlTag.SCRIPT
		},
		false),
	DISABLED(new HtmlTag[] { HtmlTag.BUTTON, HtmlTag.INPUT, HtmlTag.OPTGROUP, HtmlTag.OPTION, HtmlTag.SELECT, HtmlTag.TEXTAREA }),
	ENCTYPE(new HtmlTag[] { HtmlTag.FORM }),
	FACE(new HtmlTag[] { HtmlTag.BASEFONT, HtmlTag.FONT }),
	FOR(new HtmlTag[] { HtmlTag.LABEL }),
	FRAME(new HtmlTag[] { HtmlTag.TABLE }),
	FRAMEBORDER(new HtmlTag[] { HtmlTag.FRAME, HtmlTag.IFRAME }),
	HEADERS(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	HEIGHT(
		new HtmlTag[] {
			HtmlTag.IFRAME,
			HtmlTag.IMG, HtmlTag.OBJECT,
			HtmlTag.APPLET,
			HtmlTag.TD, HtmlTag.TH
		}),
	HREF(
		new HtmlTag[] {
			HtmlTag.A, HtmlTag.AREA, HtmlTag.LINK,
			HtmlTag.BASE
		}),
	HREFLANG(new HtmlTag[] { HtmlTag.A, HtmlTag.LINK }),
	HSPACE(new HtmlTag[] { HtmlTag.APPLET, HtmlTag.IMG, HtmlTag.OBJECT }),
	HTTP_EQUIV(new HtmlTag[] { HtmlTag.META }),
	ID(new HtmlTag[] { HtmlTag.BASE, HtmlTag.HEAD, HtmlTag.HTML, HtmlTag.META, HtmlTag.SCRIPT, HtmlTag.STYLE, HtmlTag.TITLE }, false),
	ISMAP(new HtmlTag[] { HtmlTag.IMG }),
	LABEL(
		new HtmlTag[] {
			HtmlTag.OPTION,
			HtmlTag.OPTGROUP
		}),
	LANG(
		new HtmlTag[] {
			HtmlTag.APPLET, HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.BR, HtmlTag.FRAME,
			HtmlTag.FRAMESET, HtmlTag.HR, HtmlTag.IFRAME, HtmlTag.PARAM, HtmlTag.SCRIPT
		},
		false),
	LANGUAGE(new HtmlTag[] { HtmlTag.SCRIPT }),
	LINK(new HtmlTag[] { HtmlTag.BODY }),
	LONGDESC(
		new HtmlTag[] {
			HtmlTag.IMG,
			HtmlTag.FRAME, HtmlTag.IFRAME
		}),
	MARGINHEIGHT(new HtmlTag[] { HtmlTag.FRAME, HtmlTag.IFRAME }),
	MARGINWIDTH(new HtmlTag[] { HtmlTag.FRAME, HtmlTag.IFRAME }),
	MAXLENGTH(new HtmlTag[] { HtmlTag.INPUT }),
	MEDIA(
		new HtmlTag[] {
			HtmlTag.STYLE,
			HtmlTag.LINK
		}),
	METHOD(new HtmlTag[] { HtmlTag.FORM }),
	MULTIPLE(new HtmlTag[] { HtmlTag.SELECT }),
	NAME(
		new HtmlTag[] {
			HtmlTag.BUTTON, HtmlTag.TEXTAREA,
			HtmlTag.APPLET,
			HtmlTag.SELECT,
			HtmlTag.FRAME, HtmlTag.IFRAME,
			HtmlTag.A,
			HtmlTag.INPUT, HtmlTag.OBJECT,
			HtmlTag.MAP,
			HtmlTag.PARAM,
			HtmlTag.META
		}),
	NOHREF(new HtmlTag[] { HtmlTag.AREA }),
	NORESIZE(new HtmlTag[] { HtmlTag.FRAME }),
	NOSHADE(new HtmlTag[] { HtmlTag.HR }),
	NOWRAP(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	OBJECT(new HtmlTag[] { HtmlTag.APPLET }),
	ONBLUR(new HtmlTag[] { HtmlTag.A, HtmlTag.AREA, HtmlTag.BUTTON, HtmlTag.INPUT, HtmlTag.LABEL, HtmlTag.SELECT, HtmlTag.TEXTAREA }),
	ONCHANGE(new HtmlTag[] { HtmlTag.INPUT, HtmlTag.SELECT, HtmlTag.TEXTAREA }),
	ONCLICK(noEventTags(), false),
	ONDBLCLICK(noEventTags(), false),
	ONFOCUS(new HtmlTag[] { HtmlTag.A, HtmlTag.AREA, HtmlTag.BUTTON, HtmlTag.INPUT, HtmlTag.LABEL, HtmlTag.SELECT, HtmlTag.TEXTAREA }),
	ONKEYDOWN(noEventTags(), false),
	ONKEYPRESS(noEventTags(), false),
	ONKEYUP(noEventTags(), false),
	ONLOAD(
		new HtmlTag[] {
			HtmlTag.FRAMESET,
			HtmlTag.BODY
		}),
	ONMOUSEDOWN(noEventTags(), false),
	ONMOUSEMOVE(noEventTags(), false),
	ONMOUSEOUT(noEventTags(), false),
	ONMOUSEOVER(noEventTags(), false),
	ONMOUSEUP(noEventTags(), false),
	ONRESET(new HtmlTag[] { HtmlTag.FORM }),
	ONSELECT(new HtmlTag[] { HtmlTag.INPUT, HtmlTag.TEXTAREA }),
	ONSUBMIT(new HtmlTag[] { HtmlTag.FORM }),
	ONUNLOAD(
		new HtmlTag[] {
			HtmlTag.FRAMESET,
			HtmlTag.BODY
		}),
	PROFILE(new HtmlTag[] { HtmlTag.HEAD }),
	PROMPT(new HtmlTag[] { HtmlTag.ISINDEX }),
	READONLY(
		new HtmlTag[] {
			HtmlTag.TEXTAREA,
			HtmlTag.INPUT
		}),
	REL(new HtmlTag[] { HtmlTag.A, HtmlTag.LINK }),
	REV(new HtmlTag[] { HtmlTag.A, HtmlTag.LINK }),
	ROWS(
		new HtmlTag[] {
			HtmlTag.FRAMESET,
			HtmlTag.TEXTAREA
		}),
	ROWSPAN(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	RULES(new HtmlTag[] { HtmlTag.TABLE }),
	SCHEME(new HtmlTag[] { HtmlTag.META }),
	SCOPE(new HtmlTag[] { HtmlTag.TD, HtmlTag.TH }),
	SCROLLING(new HtmlTag[] { HtmlTag.FRAME, HtmlTag.IFRAME }),
	SELECTED(new HtmlTag[] { HtmlTag.OPTION }),
	SHAPE(
		new HtmlTag[] {
			HtmlTag.AREA,
			HtmlTag.A
		}),
	SIZE(
		new HtmlTag[] {
			HtmlTag.HR,
			HtmlTag.FONT,
			HtmlTag.INPUT,
			HtmlTag.BASEFONT,
			HtmlTag.SELECT
		}),
	SPAN(
		new HtmlTag[] {
			HtmlTag.COL,
			HtmlTag.COLGROUP
		}),
	SRC(
		new HtmlTag[] {
			HtmlTag.SCRIPT,
			HtmlTag.INPUT,
			HtmlTag.FRAME, HtmlTag.IFRAME,
			HtmlTag.IMG
		}),
	STANDBY(new HtmlTag[] { HtmlTag.OBJECT }),
	START(new HtmlTag[] { HtmlTag.OL }),
	STYLE(
		new HtmlTag[] {
			HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.HEAD, HtmlTag.HTML, HtmlTag.META, HtmlTag.PARAM,
			HtmlTag.SCRIPT, HtmlTag.STYLE, HtmlTag.TITLE
		},
		false),
	SUMMARY(new HtmlTag[] { HtmlTag.TABLE }),
	TABINDEX(new HtmlTag[] { HtmlTag.A, HtmlTag.AREA, HtmlTag.BUTTON, HtmlTag.INPUT, HtmlTag.OBJECT, HtmlTag.SELECT, HtmlTag.TEXTAREA }),
	TARGET(new HtmlTag[] { HtmlTag.A, HtmlTag.AREA, HtmlTag.BASE, HtmlTag.FORM, HtmlTag.LINK }),
	TEXT(new HtmlTag[] { HtmlTag.BODY }),
	TITLE(
		new HtmlTag[] {
			HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.HEAD, HtmlTag.HTML, HtmlTag.META, HtmlTag.PARAM,
			HtmlTag.SCRIPT, HtmlTag.STYLE, HtmlTag.TITLE
		},
		false),
	TYPE(
		new HtmlTag[] {
			HtmlTag.A, HtmlTag.LINK,
			HtmlTag.OBJECT,
			HtmlTag.PARAM,
			HtmlTag.SCRIPT,
			HtmlTag.STYLE,
			HtmlTag.INPUT,
			HtmlTag.LI,
			HtmlTag.OL,
			HtmlTag.UL,
			HtmlTag.BUTTON
		}),
	USEMAP(new HtmlTag[] { HtmlTag.IMG, HtmlTag.INPUT, HtmlTag.OBJECT }),
	VALIGN(new HtmlTag[] { HtmlTag.COL, HtmlTag.COLGROUP, HtmlTag.TBODY, HtmlTag.TD, HtmlTag.TFOOT, HtmlTag.TH, HtmlTag.THEAD, HtmlTag.TR }),
	VALUE(
		new HtmlTag[] {
			HtmlTag.OPTION,
			HtmlTag.PARAM,
			HtmlTag.INPUT,
			HtmlTag.BUTTON,
			HtmlTag.LI
		}),
	VALUETYPE(new HtmlTag[] { HtmlTag.PARAM }),
	VERSION(new HtmlTag[] { HtmlTag.HTML }),
	VLINK(new HtmlTag[] { HtmlTag.BODY }),
	VSPACE(new HtmlTag[] { HtmlTag.APPLET, HtmlTag.IMG, HtmlTag.OBJECT }),
	WIDTH(
		new HtmlTag[] {
			HtmlTag.HR,
			HtmlTag.IFRAME,
			HtmlTag.IMG, HtmlTag.OBJECT,
			HtmlTag.TABLE,
			HtmlTag.APPLET,
			HtmlTag.COL,
			HtmlTag.COLGROUP,
			HtmlTag.TD, HtmlTag.TH,
			HtmlTag.PRE
		}), ;

	private static HtmlTag[] NO_EVENT_TAGS = new HtmlTag[] {
		HtmlTag.APPLET, HtmlTag.BASE, HtmlTag.BASEFONT, HtmlTag.BDO, HtmlTag.BR, HtmlTag.FONT,
		HtmlTag.FRAME, HtmlTag.FRAMESET, HtmlTag.HEAD, HtmlTag.HTML, HtmlTag.IFRAME,
		HtmlTag.ISINDEX, HtmlTag.META, HtmlTag.PARAM, HtmlTag.SCRIPT, HtmlTag.STYLE,
		HtmlTag.TITLE
	};
	private static HtmlTag[] noEventTags() {
		return NO_EVENT_TAGS;
	}
	static {
		for (Attributes a: new Attributes[] {
				ACTION, ALT, COLS, CONTENT, DIR, HEIGHT, LABEL, NAME,
				ROWS, SIZE, SRC, TYPE, TYPE, WIDTH
			})
			a.isRequired = true;
	}

	//////////////////////////////////////////////////////////////////////

	TreeSet<HtmlTag> validTags;
	boolean isRequired;

	//////////////////////////////////////////////////////////////////////

	Attributes() {
	}

	Attributes(HtmlTag[] tags) {
		this(tags, true);
	}

	Attributes(HtmlTag[] tags, boolean valid) {
		if (tags != null) {
			validTags = new TreeSet<HtmlTag>();
			for (HtmlTag tag: tags)
				validTags.add(tag);
			if (!valid) {
				for (HtmlTag tag: HtmlTag.values()) {
					if (validTags.contains(tag)) {
						validTags.remove(tag);
					} else {
						validTags.add(tag);
					}
				}
			}
		}
	}

	public boolean isValid(HtmlTag tag) {
		return validTags == null || validTags.contains(tag);
	}

	public boolean isRequired() {
		return isRequired;
	}

	//////////////////////////////////////////////////////////////////////
}
