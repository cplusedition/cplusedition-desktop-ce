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

import java.util.*

/** TODO: Check the valid element list, in particular HTML5 elements, for the HTML4 attributes.  */
enum class A {
    Async("async", TAG.SCRIPT),
    Autocomplete("autocomplete", TAG.FORM, TAG.INPUT),
    Autofocus("autofocus", TAG.BUTTON, TAG.INPUT, TAG.KEYGEN, TAG.SELECT, TAG.TEXTAREA),
    Autoplay("autoplay", TAG.AUDIO, TAG.VIDEO),  //Border("border", TABLE),
    Challenge("challenge", TAG.KEYGEN),  //Charset("charset", META),

    Command("command", TAG.COMMAND),  //Content("content", META),
    Contenteditable("contenteditable"),
    Contextmenu("contextmenu"),
    Controls("controls", TAG.AUDIO, TAG.VIDEO),  //Coords("coords", AREA),
    Crossorigin("crossorigin", TAG.AUDIO, TAG.IMG, TAG.VIDEO),  //Data("data", OBJECT),

    Default("default", TAG.TRACK),  //Defer("defer", SCRIPT),

    Dirname("dirname", TAG.INPUT, TAG.TEXTAREA),  //Disabled("disabled", BUTTON, COMMAND, FIELDSET, INPUT, KEYGEN, OPTGROUP, OPTION, SELECT, TEXTAREA),
    Draggable("draggable"),
    Dropzone("dropzone"),  //Enctype("enctype", FORM),

    Form("form", TAG.BUTTON, TAG.FIELDSET, TAG.INPUT, TAG.KEYGEN, TAG.LABEL, TAG.OBJECT, TAG.OUTPUT, TAG.SELECT, TAG.TEXTAREA),
    Formaction("formaction", TAG.BUTTON, TAG.INPUT),
    Formenctype("formenctype", TAG.BUTTON, TAG.INPUT),
    Formmethod("formmethod", TAG.BUTTON, TAG.INPUT),
    Formnovalidate("formnovalidate", TAG.BUTTON, TAG.INPUT),
    Formtarget("formtarget", TAG.BUTTON, TAG.INPUT),  //Headers("headers", TD, TH),

    Hidden("hidden"),
    High("high", TAG.METER),  //Href("href", A, AREA),

    Icon("icon", TAG.COMMAND),  //Id("id"),

    Keytype("keytype", TAG.KEYGEN),
    Kind("kind", TAG.TRACK),  //Label("label", COMMAND, MENU, OPTGROUP, OPTION, TRACK),

    List("list", TAG.INPUT),
    Loop("loop", TAG.AUDIO, TAG.VIDEO),
    Low("low", TAG.METER),
    Manifest("manifest"),
    Max("max", TAG.INPUT),  //Max("max", METER, PROGRESS),

    Mediagroup("mediagroup", TAG.AUDIO, TAG.VIDEO),  //Method("method", FORM),
    Min("min", TAG.INPUT),  //Min("min", METER),

    Muted("muted", TAG.AUDIO, TAG.VIDEO),  //Name("name", BUTTON, FIELDSET, INPUT, KEYGEN, OUTPUT, SELECT, TEXTAREA),

    Novalidate("novalidate", TAG.FORM),
    Open("open", TAG.DETAILS),  //Open("open", DIALOG),
    Optimum("optimum", TAG.METER),
    Pattern("pattern", TAG.INPUT),
    Placeholder("placeholder", TAG.INPUT, TAG.TEXTAREA),
    Poster("poster", TAG.VIDEO),
    Preload("preload", TAG.AUDIO, TAG.VIDEO),
    Radiogroup("radiogroup", TAG.COMMAND),  //Readonly("readonly", INPUT, TEXTAREA),

    Required("required", TAG.INPUT, TAG.SELECT, TAG.TEXTAREA),
    Reversed("reversed", TAG.OL),  //Rows("rows", TEXTAREA),

    Sandbox("sandbox", TAG.IFRAME),
    Spellcheck("spellcheck"),  //Scope("scope", TH),
    Scoped("scoped", TAG.STYLE),
    Seamless("seamless", TAG.IFRAME),  //Selected("selected", OPTION),

    Sizes("sizes", TAG.LINK),  //Span("span", COL, COLGROUP),

    Srcdoc("srcdoc", TAG.IFRAME),
    Srclang("srclang", TAG.TRACK),  //Start("start", OL),
    Step("step", TAG.INPUT),  //Style("style"),

    Translate("translate"),

    Typemustmatch("typemustmatch", TAG.OBJECT),  //Usemap("usemap", IMG, OBJECT),

    Wrap("wrap", TAG.TEXTAREA),
    Onabort("onabort"),
    Onafterprint("onafterprint", TAG.BODY),
    Onbeforeprint("onbeforeprint", TAG.BODY),
    Onbeforeunload("onbeforeunload", TAG.BODY),  //Onblur("onblur", BODY),

    Oncancel("oncancel"),
    Oncanplay("oncanplay"),
    Oncanplaythrough("oncanplaythrough"),  //Onchange("onchange"),

    Onclose("onclose"),
    Oncontextmenu("oncontextmenu"),
    Oncuechange("oncuechange"),  //Ondblclick("ondblclick"),
    Ondrag("ondrag"),
    Ondragend("ondragend"),
    Ondragenter("ondragenter"),
    Ondragleave("ondragleave"),
    Ondragover("ondragover"),
    Ondragstart("ondragstart"),
    Ondrop("ondrop"),
    Ondurationchange("ondurationchange"),
    Onemptied("onemptied"),
    Onended("onended"),
    Onerror("onerror", TAG.BODY),  //Onerror("onerror"),

    Onhashchange("onhashchange", TAG.BODY),
    Oninput("oninput"),
    Oninvalid("oninvalid"),  //Onkeydown("onkeydown"),

    Onloadeddata("onloadeddata"),
    Onloadedmetadata("onloadedmetadata"),
    Onloadstart("onloadstart"),
    Onmessage("onmessage", TAG.BODY),  //Onmousedown("onmousedown"),

    Onmousewheel("onmousewheel"),
    Onoffline("onoffline", TAG.BODY),
    Ononline("ononline", TAG.BODY),
    Onpagehide("onpagehide", TAG.BODY),
    Onpageshow("onpageshow", TAG.BODY),
    Onpause("onpause"),
    Onplay("onplay"),
    Onplaying("onplaying"),
    Onpopstate("onpopstate", TAG.BODY),
    Onprogress("onprogress"),
    Onratechange("onratechange"),  //Onreset("onreset"),
    Onresize("onresize", TAG.BODY),
    Onscroll("onscroll", TAG.BODY),  //Onscroll("onscroll"),
    Onseeked("onseeked"),
    Onseeking("onseeking"),  //Onselect("onselect"),
    Onshow("onshow"),
    Onstalled("onstalled"),
    Onstorage("onstorage", TAG.BODY),  //Onsubmit("onsubmit"),
    Onsuspend("onsuspend"),
    Ontimeupdate("ontimeupdate"),  //Onunload("onunload", BODY),
    Onvolumechange("onvolumechange"),
    Onwaiting("onwaiting"),

    Abbr("abbr", TAG.TD, TAG.TH),
    AcceptCharset("accept-charset", TAG.FORM),
    Accept("accept", TAG.INPUT),
    Accesskey("accesskey", TAG.A, TAG.AREA, TAG.BUTTON, TAG.INPUT, TAG.LABEL, TAG.LEGEND, TAG.TEXTAREA),
    Action("action", TAG.FORM),
    Align("align",
            TAG.CAPTION,
            TAG.APPLET,
            TAG.IFRAME,
            TAG.IMG,
            TAG.INPUT,
            TAG.OBJECT,
            TAG.LEGEND,
            TAG.TABLE,
            TAG.HR,
            TAG.DIV,
            TAG.H1,
            TAG.H2,
            TAG.H3,
            TAG.H4,
            TAG.H5,
            TAG.H6,
            TAG.P,
            TAG.COL,
            TAG.COLGROUP,
            TAG.TBODY,
            TAG.TD,
            TAG.TFOOT,
            TAG.TH,
            TAG.THEAD,
            TAG.TR),
    Alink("alink", TAG.BODY),
    Alt("alt", TAG.APPLET, TAG.AREA, TAG.IMG, TAG.INPUT),
    Archive("archive", TAG.OBJECT, TAG.APPLET),
    Axis("axis", TAG.TD, TAG.TH),
    Background("background", TAG.BODY),
    Bgcolor("bgcolor", TAG.TABLE, TAG.TR, TAG.TD, TAG.TH, TAG.BODY),
    Border("border", TAG.IMG, TAG.OBJECT, TAG.TABLE),
    Cellpadding("cellpadding", TAG.TABLE),
    Cellspacing("cellspacing", TAG.TABLE),
    Char("char", TAG.COL, TAG.COLGROUP, TAG.TBODY, TAG.TD, TAG.TFOOT, TAG.TH, TAG.THEAD, TAG.TR),
    Charoff("charoff", TAG.COL, TAG.COLGROUP, TAG.TBODY, TAG.TD, TAG.TFOOT, TAG.TH, TAG.THEAD, TAG.TR),
    Charset(
            "charset", TAG.A, TAG.LINK, TAG.SCRIPT, TAG.META),
    Checked("checked", TAG.INPUT),
    Cite("cite", TAG.BLOCKQUOTE, TAG.Q, TAG.DEL, TAG.INS),
    Class("class",
            TAG.emptyArray,
            TAG.BASE,
            TAG.BASEFONT,
            TAG.HEAD,
            TAG.HTML,
            TAG.META,
            TAG.PARAM,
            TAG.SCRIPT,
            TAG.STYLE,
            TAG.TITLE),
    Classid("classid", TAG.OBJECT),
    Clear("clear", TAG.BR),
    Code("code", TAG.APPLET),
    Codebase("codebase", TAG.OBJECT, TAG.APPLET),
    Codetype("codetype", TAG.OBJECT),
    Color("color", TAG.BASEFONT, TAG.FONT),
    Cols("cols", TAG.FRAMESET, TAG.TEXTAREA),
    Colspan("colspan", TAG.TD, TAG.TH),
    Compact("compact", TAG.DIR, TAG.MENU, TAG.DL, TAG.OL, TAG.UL),
    Content("content", TAG.META),
    Coords("coords", TAG.AREA, TAG.A),
    Data("data", TAG.OBJECT),
    Datetime("datetime", TAG.DEL, TAG.INS),
    Declare("declare", TAG.OBJECT),
    Defer("defer", TAG.SCRIPT),
    Dir("dir",
            TAG.emptyArray,
            TAG.APPLET,
            TAG.BASE,
            TAG.BASEFONT,
            TAG.BR,
            TAG.FRAME,
            TAG.FRAMESET,
            TAG.HR,
            TAG.IFRAME,
            TAG.PARAM,
            TAG.SCRIPT),
    Disabled("disabled", TAG.BUTTON, TAG.INPUT, TAG.OPTGROUP, TAG.OPTION, TAG.SELECT, TAG.TEXTAREA),
    Enctype("enctype", TAG.FORM),
    Face("face", TAG.BASEFONT, TAG.FONT),
    For(
            "for", TAG.LABEL, TAG.OUTPUT),
    Frame("frame", TAG.TABLE),
    Frameborder("frameborder", TAG.FRAME, TAG.IFRAME),
    Headers("headers", TAG.TD, TAG.TH),
    Height(
            "height", TAG.IFRAME, TAG.IMG, TAG.OBJECT, TAG.APPLET, TAG.TD, TAG.TH, TAG.EMBED, TAG.INPUT, TAG.VIDEO),
    Href("href", TAG.A, TAG.AREA, TAG.LINK, TAG.BASE),
    Hreflang("hreflang", TAG.A, TAG.LINK),
    Hspace("hspace", TAG.APPLET, TAG.IMG, TAG.OBJECT),
    HttpEquiv("http-equiv", TAG.META),
    Id("id", TAG.emptyArray, TAG.BASE, TAG.HEAD, TAG.HTML, TAG.META, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Ismap("ismap", TAG.IMG),
    Label(
            "label", TAG.OPTION, TAG.OPTGROUP, TAG.COMMAND, TAG.MENU, TAG.TRACK),
    Lang("lang", TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BR, TAG.FRAME, TAG.FRAMESET, TAG.HR, TAG.IFRAME, TAG.PARAM, TAG.SCRIPT),
    Language("language", TAG.SCRIPT),
    Link("link", TAG.BODY),
    Longdesc("longdesc", TAG.IMG, TAG.FRAME, TAG.IFRAME),
    Marginheight("marginheight", TAG.FRAME, TAG.IFRAME),
    Marginwidth("marginwidth", TAG.FRAME, TAG.IFRAME),
    Maxlength("maxlength", TAG.INPUT),
    Media("media", TAG.STYLE, TAG.LINK, TAG.A, TAG.AREA, TAG.SOURCE),
    Method("method", TAG.FORM),
    Multiple("multiple", TAG.SELECT),
    Name(
            "name", TAG.BUTTON, TAG.TEXTAREA, TAG.APPLET, TAG.SELECT, TAG.FRAME, TAG.IFRAME, TAG.A, TAG.INPUT, TAG.OBJECT, TAG.MAP, TAG.PARAM, TAG.META, TAG.FIELDSET, TAG.KEYGEN, TAG.OUTPUT, TAG.FORM),
    Nohref("nohref", TAG.AREA),
    Noresize("noresize", TAG.FRAME),
    Noshade("noshade", TAG.HR),
    Nowrap("nowrap", TAG.TD, TAG.TH),
    Object("object", TAG.APPLET),
    Onblur("onblur", TAG.A, TAG.AREA, TAG.BUTTON, TAG.INPUT, TAG.LABEL, TAG.SELECT, TAG.TEXTAREA),
    Onchange("onchange", TAG.INPUT, TAG.SELECT, TAG.TEXTAREA),
    Onclick(
            "onclick",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Ondblclick(
            "ondblclick",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onfocus("onfocus", TAG.A, TAG.AREA, TAG.BUTTON, TAG.INPUT, TAG.LABEL, TAG.SELECT, TAG.TEXTAREA),
    Onkeydown(
            "onkeydown",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onkeypress(
            "onkeypress",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onkeyup(
            "onkeyup",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onload("onload", TAG.FRAMESET, TAG.BODY),
    Onmousedown(
            "onmousedown",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onmousemove(
            "onmousemove",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onmouseout(
            "onmouseout",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onmouseover(
            "onmouseover",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onmouseup(
            "onmouseup",
            TAG.emptyArray, TAG.APPLET, TAG.BASE, TAG.BASEFONT, TAG.BDO, TAG.BR, TAG.FONT, TAG.FRAME, TAG.FRAMESET, TAG.HEAD, TAG.HTML, TAG.IFRAME, TAG.ISINDEX, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Onreset("onreset", TAG.FORM),
    Onselect("onselect", TAG.INPUT, TAG.TEXTAREA),
    Onsubmit("onsubmit", TAG.FORM),
    Onunload("onunload", TAG.FRAMESET, TAG.BODY),
    Profile("profile", TAG.HEAD),
    Prompt("prompt", TAG.ISINDEX),
    Readonly("readonly", TAG.TEXTAREA, TAG.INPUT),
    Rel("rel", TAG.A, TAG.LINK),
    Rev("rev", TAG.A, TAG.LINK),
    Rows("rows", TAG.FRAMESET, TAG.TEXTAREA),
    Rowspan("rowspan", TAG.TD, TAG.TH),
    Rules("rules", TAG.TABLE),
    Scheme("scheme", TAG.META),
    Scope("scope", TAG.TD, TAG.TH),
    Scrolling("scrolling", TAG.FRAME, TAG.IFRAME),
    Selected("selected", TAG.OPTION),
    Shape("shape", TAG.AREA, TAG.A),
    Size("size", TAG.HR, TAG.FONT, TAG.INPUT, TAG.BASEFONT, TAG.SELECT),
    Span("span", TAG.COL, TAG.COLGROUP),
    Src("src", TAG.SCRIPT, TAG.INPUT, TAG.FRAME, TAG.IFRAME, TAG.IMG),
    Standby("standby", TAG.OBJECT),
    Start("start", TAG.OL),
    Style("style", TAG.emptyArray, TAG.BASE, TAG.BASEFONT, TAG.HEAD, TAG.HTML, TAG.META, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.TITLE),
    Summary("summary", TAG.TABLE),
    Tabindex("tabindex", TAG.A, TAG.AREA, TAG.BUTTON, TAG.INPUT, TAG.OBJECT, TAG.SELECT, TAG.TEXTAREA),
    Target("target", TAG.A, TAG.AREA, TAG.BASE, TAG.FORM, TAG.LINK),
    Text("text", TAG.BODY),
    Title(
            "title",
            TAG.emptyArray,
            TAG.BASE,
            TAG.BASEFONT,
            TAG.HEAD,
            TAG.HTML,
            TAG.META,
            TAG.PARAM,
            TAG.SCRIPT,
            TAG.TITLE,
            TAG.ABBR,
            TAG.DFN,
            TAG.COMMAND,
            TAG.LINK,
            TAG.STYLE),
    Type(
            "type", TAG.A, TAG.LINK, TAG.OBJECT, TAG.PARAM, TAG.SCRIPT, TAG.STYLE, TAG.INPUT, TAG.LI, TAG.OL, TAG.UL, TAG.BUTTON, TAG.AREA, TAG.COMMAND, TAG.EMBED, TAG.SOURCE, TAG.MENU),
    Usemap("usemap", TAG.IMG, TAG.INPUT, TAG.OBJECT),
    Valign("valign", TAG.COL, TAG.COLGROUP, TAG.TBODY, TAG.TD, TAG.TFOOT, TAG.TH, TAG.THEAD, TAG.TR),
    Value(
            "value", TAG.OPTION, TAG.PARAM, TAG.INPUT, TAG.BUTTON, TAG.LI, TAG.METER, TAG.PROGRESS),
    Valuetype("valuetype", TAG.PARAM),
    Version("version", TAG.HTML),
    Vlink("vlink", TAG.BODY),
    Vspace("vspace", TAG.APPLET, TAG.IMG, TAG.OBJECT),
    Width(
            "width", TAG.HR, TAG.IFRAME, TAG.IMG, TAG.OBJECT, TAG.TABLE, TAG.APPLET, TAG.COL, TAG.COLGROUP, TAG.TD, TAG.TH, TAG.PRE, TAG.CANVAS, TAG.EMBED, TAG.VIDEO);

    private var text: String
    private val valids: MutableSet<TAG> = TreeSet()
    private val invalids: MutableSet<TAG> = TreeSet()

    constructor(text: String, vararg valids: TAG) {
        this.text = text
        Collections.addAll(this.valids, *valids)
    }

    constructor(text: String, valids: Array<TAG>, vararg invalids: TAG) {
        this.text = text
        Collections.addAll(this.valids, *valids)
        Collections.addAll(this.invalids, *invalids)
    }

    fun isValid(tag: TAG?): Boolean {
        return valids.contains(tag) || !invalids.contains(tag)
    }

    fun value(value: Any): IAttribute {
        return Attribute(toString(), value.toString())
    }

    fun value(value: String): IAttribute {
        return Attribute(toString(), value)
    }

    fun value(value: Boolean): IAttribute {
        return Attribute(toString(), value.toString())
    }

    fun value(value: Int): IAttribute {
        return Attribute(toString(), value.toString())
    }

    fun value(value: Double): IAttribute {
        return Attribute(toString(), value.toString())
    }

    /** Set value, shorthand for value().  */
    fun s(value: Any): IAttribute {
        return Attribute(toString(), value.toString())
    }

    /** Set value, shorthand for value().  */
    fun s(value: String): IAttribute {
        return Attribute(toString(), value)
    }

    /** Set value, shorthand for value().  */
    fun s(value: Boolean): IAttribute {
        return Attribute(toString(), value.toString())
    }

    /** Set value, shorthand for value().  */
    fun s(value: Int): IAttribute {
        return Attribute(toString(), value.toString())
    }

    /** Set value, shorthand for value().  */
    fun s(value: Double): IAttribute {
        return Attribute(toString(), value.toString())
    }

    override fun toString(): String {
        return text
    }
}
