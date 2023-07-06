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
package com.cplusedition.bot.dsl.html.api

import com.cplusedition.bot.core.TextUt
import java.util.*

enum class TAG

//////////////////////////////////////////////////////////////////////

 constructor(
        var startTag: REQ = REQ.YES,
        var endTag: REQ = REQ.YES,
        var isEmpty: Boolean = false,
        var isDeprecated: Boolean = false,
        var variant: DTD = DTD.HTML4
) {
    ARTICLE(DTD.HTML5),
    ASIDE(DTD.HTML5),
    AUDIO(DTD.HTML5),

    BDI(DTD.HTML5),

    CANVAS(DTD.HTML5),

    COMMAND(true, DTD.HTML5),

    DATALIST(DTD.HTML5),

    DETAILS(DTD.HTML5),

    EMBED(true, DTD.HTML5),

    FIGCAPTION(DTD.HTML5),
    FIGURE(DTD.HTML5),
    FOOTER(DTD.HTML5),

    HEADER(DTD.HTML5),
    HGROUP(DTD.HTML5),

    KEYGEN(DTD.HTML5),

    MARK(true, DTD.HTML5),

    METER(DTD.HTML5),
    NAV(DTD.HTML5),  /* group of navigational links NEW */ 

    OUTPUT(DTD.HTML5),

    PROGRESS(DTD.HTML5),

    RP(DTD.HTML5),
    RT(DTD.HTML5),
    RUBY(DTD.HTML5),

    SECTION(DTD.HTML5),

    SOURCE(true, DTD.HTML5),

    SUMMARY(DTD.HTML5),

    TIME(DTD.HTML5),

    TRACK(true, DTD.HTML5),

    VIDEO(DTD.HTML5),
    WBR(true, DTD.HTML5),

    UNKNOWN(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    DOCTYPE(true),
    A,
    ABBR,
    ACRONYM,
    ADDRESS,
    APPLET(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    AREA(true),
    B,
    BASE(true),
    BASEFONT(REQ.YES, REQ.NO, true, true, DTD.HTML4),
    BDO,
    BIG,
    BLOCKQUOTE,
    BODY(REQ.OPT, REQ.OPT, DTD.HTML4),
    BR(true),
    BUTTON,
    CAPTION,
    CENTER(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    CITE,
    CODE,
    COL(true),
    COLGROUP(REQ.YES, REQ.OPT, DTD.HTML4),
    DD(REQ.YES, REQ.OPT, DTD.HTML4),
    DEL,
    DFN,
    DIR(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    DIV,
    DL,
    DT(REQ.YES, REQ.OPT, DTD.HTML4),
    EM,
    FIELDSET,
    FONT(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    FORM,
    FRAME(true, DTD.HTML4_FRAMESET),
    FRAMESET(DTD.HTML4_FRAMESET),
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    HEAD(REQ.OPT, REQ.OPT, DTD.HTML4),
    HR(true),
    HTML(REQ.OPT, REQ.OPT, DTD.HTML4),
    I,
    IFRAME(REQ.YES, REQ.YES, false, false, DTD.HTML4),
    IMG(true),
    INPUT(true),
    INS,
    ISINDEX(REQ.YES, REQ.NO, true, true, DTD.HTML4),
    KBD,
    LABEL,
    LEGEND,
    LI(REQ.YES, REQ.OPT, DTD.HTML4),
    LINK(true),
    MAP,
    MENU(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    META(true),
    NOFRAMES(DTD.HTML4_FRAMESET),
    NOSCRIPT,
    OBJECT,
    OL,
    OPTGROUP,
    OPTION(REQ.YES, REQ.OPT, DTD.HTML4),
    P(REQ.YES, REQ.OPT, DTD.HTML4),
    PARAM(true),
    PRE,
    Q,
    S(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    SAMP,
    SCRIPT,
    SELECT,
    SMALL,
    SPAN,
    STRIKE(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    STRONG,
    STYLE,
    SUB,
    SUP,
    TABLE,
    TBODY(REQ.OPT, REQ.OPT, DTD.HTML4),
    TD(REQ.YES, REQ.OPT, DTD.HTML4),
    TEXTAREA,
    TFOOT(REQ.YES, REQ.OPT, DTD.HTML4),
    TH(REQ.YES, REQ.OPT, DTD.HTML4),
    THEAD(REQ.YES, REQ.OPT, DTD.HTML4),
    TITLE,
    TR(REQ.YES, REQ.OPT, DTD.HTML4),
    TT,
    U(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    UL,
    VAR,
    XMP(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    PLAINTEXT(REQ.YES, REQ.YES, false, true, DTD.HTML4),
    TERM(REQ.YES, REQ.YES, false, true, DTD.V2),
    SDFIELD(DTD.MS),

    NOBR(REQ.YES, REQ.YES, false, true, DTD.NETVIGATOR),
    SPACER(REQ.YES, REQ.NO, true, true, DTD.NETVIGATOR),
    MULTICOL(REQ.YES, REQ.YES, false, true, DTD.NETVIGATOR),
    LAYER(DTD.NETVIGATOR),
    ILAYER(DTD.NETVIGATOR),
    NOLAYER(DTD.NETVIGATOR);

    //////////////////////////////////////////////////////////////////////
    enum class REQ {
        YES,
        OPT,
        NO
    }

    enum class DTD {
        HTML5,
        HTML4,
        HTML4_FRAMESET,
        MS,
        NETVIGATOR,
        V2,
        X
    }

    companion object {
        const val NONE = 0x00

        const val C_BLOCK_FORMAT = 0x0008
        const val C_PRE_FORMAT = 0x0010
        val lookupTable: MutableMap<String, TAG> = TreeMap()

        val emptyArray = arrayOf<TAG>()

        /** @param name XHtml tag name in lower case or upper case, but not mixed.
         */
        operator fun get(name: String): TAG? {
            return lookupTable[name]
        }

        //////////////////////////////////////////////////////////////////////

        init {
            for (tag in arrayOf(
                    UNKNOWN,
                    HTML,
                    HEAD,
                    BODY,
                    META,
                    TITLE,
                    LINK,
                    BASE,
                    P,
                    DL,
                    DIV,
                    NOSCRIPT,
                    BLOCKQUOTE,
                    HR,
                    TABLE,
                    TR,
                    COLGROUP,
                    THEAD,
                    TBODY,
                    FORM,
                    INPUT,
                    SELECT,
                    BUTTON,
                    OPTGROUP,
                    OPTION,
                    TEXTAREA,
                    LABEL,
                    FIELDSET,
                    ADDRESS,
                    H1,
                    H2,
                    H3,
                    H4,
                    H5,
                    H6,
                    UL,
                    OL,
                    LI,
                    PRE,
                    SCRIPT,
                    STYLE,
                    XMP,
                    PLAINTEXT,
                    MULTICOL,
                    LAYER,
                    NOLAYER,
                    IFRAME,
                    OBJECT
            )) {
                tag.setModifier(C_BLOCK_FORMAT)
            }
            for (tag in arrayOf(PRE, SCRIPT, STYLE)) {
                tag.setModifier(C_PRE_FORMAT)
            }
            for (tag in values()) {
                val name: String = tag.name
                lookupTable[TextUt.toLowerCase(name)] = tag
                lookupTable[TextUt.toUpperCase(name)] = tag
            }
        }
    }

    private var modifier_ = 0

    constructor(empty: Boolean) : this(REQ.YES, REQ.NO, empty, false, DTD.HTML4) {}
    constructor(dtd: DTD) : this(REQ.YES, REQ.YES, false, false, dtd) {}
    constructor(empty: Boolean, dtd: DTD) : this(REQ.YES, REQ.NO, empty, false, dtd) {}
    constructor(start: REQ, end: REQ, dtd: DTD) : this(start, end, false, false, dtd) {}

    fun requireEndTag(): Boolean {
        return endTag == REQ.YES
    }

    fun optionalEndTag(): Boolean {
        return endTag == REQ.OPT
    }

    fun hasModifier(flag: Int): Boolean {
        return modifier_ and flag != 0
    }

    fun getModifier(): Int {
        return modifier_
    }

    fun setModifier(flag: Int) {
        modifier_ = modifier_ or flag
    }

    val isBlockFormat: Boolean
        get() = modifier_ and C_BLOCK_FORMAT != 0

    val isPreFormatted: Boolean
        get() = modifier_ and C_PRE_FORMAT != 0

}
