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
package sf.andrians.cplusedition.support.handler

import java.util.*
import kotlin.collections.set

abstract class CpluseditionRequestHandlerBase protected constructor(
        protected val context: ICpluseditionContext
) {

    protected object Flags {
        const val None = 0x00
        const val User = 0x01
    }

    enum class HtmlTag(private val flags: Int) {
        //#BEGIN HtmlTag
        //#BEGIN SHUFFLE
        A(Flags.User),
        ABBR(Flags.User),
        ADDRESS(Flags.User),
        ARTICLE(Flags.User),
        ASIDE(Flags.User),
        AUDIO(Flags.User),
        B(Flags.User),
        BDI(Flags.User),
        BDO(Flags.User),
        BLOCKQUOTE(Flags.User),
        BR(Flags.User),
        CANVAS(Flags.User),
        CAPTION(Flags.User),
        CITE(Flags.User),
        CODE(Flags.User),
        COL(Flags.User),
        COLGROUP(Flags.User),
        DATA(Flags.User),
        DD(Flags.User),
        DEL(Flags.User),
        DETAILS(Flags.User),
        DFN(Flags.User),
        DIV(Flags.User),
        DL(Flags.User),
        DT(Flags.User),
        EM(Flags.User),
        FIGCAPTION(Flags.User),
        FIGURE(Flags.User),
        FOOTER(Flags.User),
        H1(Flags.User),
        H2(Flags.User),
        H3(Flags.User),
        H4(Flags.User),
        H5(Flags.User),
        H6(Flags.User),
        HEADER(Flags.User),
        HR(Flags.User),
        I(Flags.User),
        IMG(Flags.User),
        INS(Flags.User),
        KBD(Flags.User),
        LI(Flags.User),
        MAIN(Flags.User),
        MARK(Flags.User),
        METER(Flags.User),
        NAV(Flags.User),
        OL(Flags.User),
        OUTPUT(Flags.User),
        P(Flags.User),
        PRE(Flags.User),
        PROGRESS(Flags.User),
        Q(Flags.User),
        RB(Flags.User),
        RP(Flags.User),
        RT(Flags.User),
        RTC(Flags.User),
        RUBY(Flags.User),
        S(Flags.User),
        SAMP(Flags.User),
        SECTION(Flags.User),
        SMALL(Flags.User),
        SPAN(Flags.User),
        STRONG(Flags.User),
        SUB(Flags.User),
        SUMMARY(Flags.User),
        SUP(Flags.User),
        TABLE(Flags.User),
        TBODY(Flags.User),
        TD(Flags.User),
        TFOOT(Flags.User),
        TH(Flags.User),
        THEAD(Flags.User),
        TIME(Flags.User),
        TITLE(Flags.User),
        TR(Flags.User),
        U(Flags.User),
        UL(Flags.User),
        VAR(Flags.User),
        WBR(Flags.User),
        FIELDSET(Flags.User),
        INPUT(Flags.User),
        LABEL(Flags.User),
        LEGEND(Flags.User),
        OPTGROUP(Flags.User),
        OPTION(Flags.User),
        SELECT(Flags.User),
        TEXTAREA(Flags.User),
        FONT(Flags.User),
        BUTTON(Flags.User),
        DATALIST(Flags.User),
        MENUITEM(Flags.User),
        MENU(Flags.User),
        PICTURE(Flags.User),
        SOURCE(Flags.User),
        TRACK(Flags.User),
        VIDEO(Flags.User),
        AREA(Flags.User),
        BASE(Flags.User),
        BODY(Flags.User),
        DIALOG(Flags.User),
        DOCTYPE(Flags.User),
        FORM(Flags.User),
        HEAD(Flags.User),
        HTML(Flags.User),
        KEYGEN(Flags.User),
        LINK(Flags.User),
        MAP(Flags.User),
        META(Flags.User),
        NOSCRIPT(Flags.User),
        PARAM(Flags.User),
        STYLE(Flags.User),
        TEMPLATE(Flags.User),
        IFRAME(Flags.None),
        EMBED(Flags.None),
        OBJECT(Flags.None),
        SCRIPT(Flags.None),
        //#END SHUFFLE
        //#END HtmlTag
        ;

        companion object {
            private val tags: MutableMap<String, HtmlTag> = TreeMap()

            fun get(name: String): HtmlTag? {
                return tags[name.toUpperCase(Locale.ENGLISH)]
            }

            init {
                for (tag in values()) {
                    tags[tag.name] = tag
                }
            }
        }

        fun flags(): Int {
            return flags
        }

    }

    enum class HtmlAttr(private val property: String, private val flags: Int) {
        //#BEGIN HtmlAttr
        //#BEGIN SHUFFLE
        Abbr("abbr", Flags.User),
        Alt("alt", Flags.User),
        Autoplay("autoplay", Flags.User),
        Border("border", Flags.User),
        Cite("cite", Flags.User),
        Class("class", Flags.User),
        Cols("cols", Flags.User),
        Colspan("colspan", Flags.User),
        Controls("controls", Flags.User),
        Datetime("datetime", Flags.User),
        Default("default", Flags.User),
        Dir("dir", Flags.User),
        Height("height", Flags.User),
        Href("href", Flags.User),
        Id("id", Flags.User),
        Kind("kind", Flags.User),
        Lang("lang", Flags.User),
        Loop("loop", Flags.User),
        Mediagroup("mediagroup", Flags.User),
        Muted("muted", Flags.User),
        Name("name", Flags.User),
        Poster("poster", Flags.User),
        Preload("preload", Flags.User),
        Reversed("reversed", Flags.User),
        Rowspan("rowspan", Flags.User),
        Scope("scope", Flags.User),
        Scoped("scoped", Flags.User),
        Sortable("sortable", Flags.User),
        Sorted("sorted", Flags.User),
        Span("span", Flags.User),
        Src("src", Flags.User),
        Srclang("srclang", Flags.User),
        Start("start", Flags.User),
        Style("style", Flags.User),
        Tabindex("tabindex", Flags.User),
        Title("title", Flags.User),
        Translate("translate", Flags.User),
        Type("type", Flags.User),
        Width("width", Flags.User),
        Codecs("codecs", Flags.User),
        Face("face", Flags.User),
        Autofocus("autofocus", Flags.User),
        Autocapitalize("autocapitalize", Flags.User),
        Autocomplete("autocomplete", Flags.User),
        Autocorrect("autocorrect", Flags.User),
        Checked("checked", Flags.User),
        Command("command", Flags.User),
        Dirname("dirname", Flags.User),
        Disabled("disabled", Flags.User),
        High("high", Flags.User),
        Icon("icon", Flags.User),
        Inputmode("inputmode", Flags.User),
        Label("label", Flags.User),
        List("list", Flags.User),
        Low("low", Flags.User),
        Max("max", Flags.User),
        Maxlength("maxlength", Flags.User),
        Min("min", Flags.User),
        Minlength("minlength", Flags.User),
        Multiple("multiple", Flags.User),
        Placeholder("placeholder", Flags.User),
        Radiogroup("radiogroup", Flags.User),
        Readonly("readonly", Flags.User),
        Required("required", Flags.User),
        Selected("selected", Flags.User),
        Spellcheck("spellcheck", Flags.User),
        Optimum("optimum", Flags.User),
        Rows("rows", Flags.User),
        Size("size", Flags.User),
        Step("step", Flags.User),
        Value("value", Flags.User),
        Wrap("wrap", Flags.User),
        Accesskey("accesskey", Flags.User),
        Contenteditable("contenteditable", Flags.User),
        Headers("headers", Flags.User),
        Pattern("pattern", Flags.User),
        Accept("accept", Flags.User),
        AcceptCharset("accept-charset", Flags.User),
        Action("action", Flags.User),
        Contextmenu("contextmenu", Flags.User),
        Enctype("enctype", Flags.User),
        Form("form", Flags.User),
        Formaction("formaction", Flags.User),
        Formenctype("formenctype", Flags.User),
        Formmethod("formmethod", Flags.User),
        Formnovalidate("formnovalidate", Flags.User),
        Formtarget("formtarget", Flags.User),
        Menu("menu", Flags.User),
        Method("method", Flags.User),
        Novalidate("novalidate", Flags.User),
        Allowfullscreen("allowfullscreen", Flags.User),
        Async("async", Flags.User),
        Content("content", Flags.User),
        Crossorigin("crossorigin", Flags.User),
        Data("data", Flags.User),
        Defer("defer", Flags.User),
        Challenge("challenge", Flags.User),
        Charset("charset", Flags.User),
        Coords("coords", Flags.User),
        Download("download", Flags.User),
        Draggable("draggable", Flags.User),
        Dropzone("dropzone", Flags.User),
        For("for", Flags.User),
        Hidden("hidden", Flags.User),
        Hreflang("hreflang", Flags.User),
        HttpEquiv("httpequiv", Flags.User),
        Ismap("ismap", Flags.User),
        Itemid("itemid", Flags.User),
        Itemprop("itemprop", Flags.User),
        Itemref("itemref", Flags.User),
        Itemscope("itemscope", Flags.User),
        Itemtype("itemtype", Flags.User),
        Keytype("keytype", Flags.User),
        Manifest("manifest", Flags.User),
        Media("media", Flags.User),
        Open("open", Flags.User),
        Rel("rel", Flags.User),
        Role("role", Flags.User),
        Sandbox("sandbox", Flags.User),
        Seamless("seamless", Flags.User),
        Shape("shape", Flags.User),
        Sizes("sizes", Flags.User),
        Srcdoc("srcdoc", Flags.User),
        Target("target", Flags.User),
        Typemustmatch("typemustmatch", Flags.User),
        Usemap("usemap", Flags.User),
        //#END SHUFFLE
        //#END HtmlAttr
        ;

        companion object {
            private val attrs: MutableMap<String, HtmlAttr> = TreeMap()

            operator fun get(property: String): HtmlAttr? {
                return attrs[property]
            }

            init {
                for (e in values()) {
                    attrs[e.property] = e
                }
            }
        }

        fun flags(): Int {
            return flags
        }
    }

    enum class HtmlEvent {
        //#BEGIN SHUFFLE
        onabort,
        onautocomplete,
        onautocompleteerror,
        onafterprint,
        onbeforeprint,
        onbeforeunload,
        onblur,
        oncancel,
        oncanplay,
        oncanplaythrough,
        onchange,
        onclick,
        onclose,
        oncontextmenu,
        oncuechange,
        ondblclick,
        ondrag,
        ondragend,
        ondragenter,
        ondragexit,
        ondragleave,
        ondragover,
        ondragstart,
        ondrop,
        ondurationchange,
        onemptied,
        onended,
        onerror,
        onfocus,
        onhashchange,
        oninput,
        oninvalid,
        onkeydown,
        onkeypress,
        onkeyup,
        onlanguagechange,
        onload,
        onloadeddata,
        onloadedmetadata,
        onloadstart,
        onmessage,
        onmousedown,
        onmouseenter,
        onmouseleave,
        onmousemove,
        onmouseout,
        onmouseover,
        onmouseup,
        onmousewheel,
        onoffline,
        ononline,
        onpagehide,
        onpageshow,
        onpause,
        onplay,
        onplaying,
        onpopstate,
        onprogress,
        onratechange,
        onreset,
        onresize,
        onscroll,
        onseeked,
        onseeking,
        onselect,
        onshow,
        onsort,
        onstalled,
        onstorage,
        onsubmit,
        onsuspend,
        ontimeupdate,
        ontoggle,
        onunload,
        onvolumechange,
        onwaiting;

        companion object {
            //#END SHUFFLE
            private val events: MutableMap<String, HtmlEvent> = TreeMap()

            operator fun get(name: String?): HtmlEvent? {
                return events[name]
            }

            init {
                for (e in values()) {
                    events[e.name] = e
                }
            }
        }
    }

    companion object {
        //#NOTE Java new URI() do not escape []. However dart Uri.parse() throws exception on[].
        internal const val PATH_ILLEGAL = "[]\u007f\\" // ?\"<>{}[]|\\^`%&*()';~!$#;
        protected const val DEF_LINEWIDTH = 120
        protected const val DEF_INDENTWIDTH = 2
    }

}
