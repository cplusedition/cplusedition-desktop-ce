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
package sf.andrians.ancoreutil.dsl.css.api

open class PD protected constructor(private val property_: String, override val expr: String) : IDeclaration {

    override val property get() = property_

    override fun addTo(ruleset: IRuleset) {
        ruleset.add(this)
    }

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        visitor.visit(this, data)
    }

    override fun toString(): String {
        return expr
    }

    //////////////////////////////////////////////////////////////////////

    abstract class FlexAlignBase(val NAME: String) {
        val Center = PD(NAME, "center")
        val End = PD(NAME, "end")
        val FlexEnd = PD(NAME, "flex-end")
        val FlexStart = PD(NAME, "flex-start")
        val Normal = PD(NAME, "normal")
        val Start = PD(NAME, "start")
        val Stretch = PD(NAME, "stretch")
    }

    object AlignItems : FlexAlignBase("align-items") {
        val Baseline = PD(NAME, "baseline")
        val SelfEnd = PD(NAME, "self-end")
        val SelfStart = PD(NAME, "self-start")
    }

    object AlignSelf : FlexAlignBase("align-self") {
        val Auto = PD(NAME, "auto")
        val Baseline = PD(NAME, "baseline")
        val SelfEnd = PD(NAME, "self-end")
        val SelfStart = PD(NAME, "self-start")
    }

    object Azimuth {
        const val NAME = "azimuth"
        val LeftSide = PD(NAME, "left-side")
        val FarLeft = PD(NAME, "far-left")
        val Left = PD(NAME, "left")
        val CenterLeft = PD(NAME, "center-left")
        val Center = PD(NAME, "center")
        val CenterRight = PD(NAME, "center-right")
        val Right = PD(NAME, "right")
        val FarRight = PD(NAME, "far-right")
        val RightSide = PD(NAME, "right-side")
        val Behind = PD(NAME, "behind")
        val Leftwards = PD(NAME, "leftwards")
        val Rightwards = PD(NAME, "rightwards")
        val Inherit = PD(NAME, "inherit")
    }

    object BackgroundAttachment {
        const val NAME = "background-attachment"
        val Scroll = PD(NAME, "scroll")
        val Fixed = PD(NAME, "fixed")
        val Inherit = PD(NAME, "inherit")

    }

    object BackgroundColor {
        const val NAME = "background-color"
        val Transparent = PD(NAME, "transparent")
        val Inherit = PD(NAME, "inherit")
    }

    object BackgroundImage {
        const val NAME = "background-image"
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object BackgroundPosition {
        const val NAME = "background-position"
        val Top = PD(NAME, "top")
        val Bottom = PD(NAME, "bottom")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Center = PD(NAME, "center")
        val Inherit = PD(NAME, "inherit")
    }

    object BackgroundRepeat {
        const val NAME = "background-repeat"
        val Repeat = PD(NAME, "repeat")
        val RepeatX = PD(NAME, "repeat-x")
        val RepeatY = PD(NAME, "repeat-y")
        val NoRepeat = PD(NAME, "no-repeat")
        val Inherit = PD(NAME, "inherit")
    }

    object Border {
        const val NAME = "border"
        val None = PD(NAME, "none")
    }

    object BorderCollapse {
        const val NAME = "border-collapse"
        val Collapse = PD(NAME, "collapse")
        val Separate = PD(NAME, "separate")
        val Inherit = PD(NAME, "inherit")
    }

    object BorderColor {
        const val NAME = "border-color"
        val Transparent = PD(NAME, "transparent")
        val Inherit = PD(NAME, "inherit")
    }

    object BorderStyle {
        const val NAME = "border-style"
        val None = PD(NAME, "none")
        val Hidden = PD(NAME, "hidden")
        val Dotted = PD(NAME, "dotted")
        val Dashed = PD(NAME, "dashed")
        val Solid = PD(NAME, "solid")
        val Double = PD(NAME, "double")
        val Groove = PD(NAME, "groove")
        val Ridge = PD(NAME, "ridge")
        val Inset = PD(NAME, "inset")
        val Outset = PD(NAME, "outset")
    }

    object BorderWidth {
        const val NAME = "border-width"
        val Thin = PD(NAME, "thin")
        val Medium = PD(NAME, "medium")
        val Thick = PD(NAME, "thick")
    }

    object BoxSizing {
        const val NAME = "box-sizing"
        val BorderBox = PD(NAME, "border-box")
        val ContentBox = PD(NAME, "content-box")
    }

    object CaptionSide {
        const val NAME = "caption-side"
        val Top = PD(NAME, "top")
        val Bottom = PD(NAME, "bottom")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Inherit = PD(NAME, "inherit")
    }

    object Clear {
        const val NAME = "clear"
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Both = PD(NAME, "both")
        val None = PD(NAME, "none")
    }

    object Content {
        const val NAME = "content"
        val OpenQuote = PD(NAME, "open-quote")
        val CloseQuote = PD(NAME, "close-quote")
        val NoOpenQuote = PD(NAME, "no-open-quote")
        val NoCloseQuote = PD(NAME, "no-close-quote")
        val Inherit = PD(NAME, "inherit")
    }

    object Cue {
        const val NAME = "cue"
        val CueBefore = PD(NAME, "cue-before")
        val CueAfter = PD(NAME, "cue-after")
        val Inherit = PD(NAME, "inherit")
    }

    object Cursor {
        const val NAME = "cursor"
        val Auto = PD(NAME, "auto")
        val Crosshair = PD(NAME, "crosshair")
        val Default = PD(NAME, "default")
        val EResize = PD(NAME, "e-resize")
        val Help = PD(NAME, "help")
        val Inherit = PD(NAME, "inherit")
        val Move = PD(NAME, "move")
        val NEResize = PD(NAME, "ne-resize")
        val NResize = PD(NAME, "n-resize")
        val NWResize = PD(NAME, "nw-resize")
        val NotAllowed = PD(NAME, "not-allowed")
        val Pointer = PD(NAME, "pointer")
        val SEResize = PD(NAME, "se-resize")
        val SResize = PD(NAME, "s-resize")
        val SWResize = PD(NAME, "sw-resize")
        val Text = PD(NAME, "text")
        val WResize = PD(NAME, "w-resize")
        val Wait = PD(NAME, "wait")
    }

    object Display {
        const val NAME = "display"
        val None = PD(NAME, "none")
        val Inline = PD(NAME, "inline")
        val Block = PD(NAME, "block")
        val InlineBlock = PD(NAME, "inline-block")
        val Flex = PD(NAME, "flex")
        val InlineFlex = PD(NAME, "inline-flex")
        val Grid = PD(NAME, "grid")
        val ListItem = PD(NAME, "list-item")
        val RunIn = PD(NAME, "run-in")
        val Compact = PD(NAME, "compact")
        val Table = PD(NAME, "table")
        val InlineTable = PD(NAME, "inline-table")
        val TableRowGroup = PD(NAME, "table-row-group")
        val TableHeaderGroup = PD(NAME, "table-header-group")
        val TableFooterGroup = PD(NAME, "table-footer-group")
        val TableRow = PD(NAME, "table-row")
        val TableColumnGroup = PD(NAME, "table-column-group")
        val TableColumn = PD(NAME, "table-column")
        val TableCell = PD(NAME, "table-cell")
        val TableCaption = PD(NAME, "caption")
        val Ruby = PD(NAME, "ruby")
        val RubyBase = PD(NAME, "ruby-base")
        val RubyText = PD(NAME, "ruby-text")
        val RubyBaseGroup = PD(NAME, "ruby-base-group")
        val RubyTextGroup = PD(NAME, "ruby-text-group")
    }

    object Elevation {
        const val NAME = "elevation"
        val Below = PD(NAME, "below")
        val Level = PD(NAME, "level")
        val Above = PD(NAME, "above")
        val Higher = PD(NAME, "higher")
        val Lower = PD(NAME, "lower")
        val Inherit = PD(NAME, "inherit")
    }

    object FlexDirection {
        const val NAME = "flex-direction"
        val Column = PD(NAME, "column")
        val ColumnReverse = PD(NAME, "column-reverse")
        val Row = PD(NAME, "row")
        val RowReverse = PD(NAME, "row-reverse")
    }

    object FlexFlow {
        const val NAME = "flex-flow"
        val Column = PD(NAME, "column")
        val ColumnReverse = PD(NAME, "column-reverse")
        val Row = PD(NAME, "row")
        val RowReverse = PD(NAME, "row-reverse")
        val NoWrap = PD(NAME, "nowrap")
        val Wrap = PD(NAME, "wrap")
        val WrapReverse = PD(NAME, "wrap-reverse")
        val ColumnWrap = PD(NAME, "column wrap")
        val ColumnNowrap = PD(NAME, "column nowrap")
        val ColumnReverseWrap = PD(NAME, "column-reverse wrap")
        val ColumnReverseNowrap = PD(NAME, "column-reverse nowrap")
        val RowWrap = PD(NAME, "row wrap")
        val RowNowrap = PD(NAME, "row nowrap")
        val RowReverseWrap = PD(NAME, "row-reverse wrap")
        val RowReverseNowrap = PD(NAME, "row-reverse nowrap")
    }

    object FlexWrap {
        const val NAME = "flex-wrap"
        val NoWrap = PD(NAME, "nowrap")
        val Wrap = PD(NAME, "wrap")
        val WrapReverse = PD(NAME, "wrap-reverse")
    }

    object Float {
        const val NAME = "float"
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object Font {
        const val NAME = "font"
        val Caption = PD(NAME, "caption")
        val Icon = PD(NAME, "icon")
        val Menu = PD(NAME, "menu")
        val MessageBox = PD(NAME, "message-box")
        val SmallCaption = PD(NAME, "small-caption")
        val StatusBar = PD(NAME, "status-bar")
        val Inherit = PD(NAME, "inherit")
    }

    /** Generic font familes.  */
    object FontFamily {
        const val NAME = "font-family"
        val Serif = PD(NAME, "serif")
        val SansSerif = PD(NAME, "sans-serif")
        val Cursive = PD(NAME, "cursive")
        val Fantasy = PD(NAME, "fantasy")
        val Monospace = PD(NAME, "monospace")
    }

    object FontSize {
        const val NAME = "font-size"
        val XXSmall = PD(NAME, "xx-small")
        val XSmall = PD(NAME, "x-small")
        val Small = PD(NAME, "small")
        val Medium = PD(NAME, "medium")
        val Large = PD(NAME, "large")
        val XLarge = PD(NAME, "x-large")
        val XXLarge = PD(NAME, "xx-large")
        val Larger = PD(NAME, "larger")
        val Smaller = PD(NAME, "smaller")
    }

    object FontStyle {
        const val NAME = "font-style"
        val Normal = PD(NAME, "normal")
        val Italic = PD(NAME, "italic")
        val Oblique = PD(NAME, "oblique")
        val Inherit = PD(NAME, "inherit")
    }

    object FontStretch {
        const val NAME = "font-stretch"
        val Normal = PD(NAME, "normal")
        val Wider = PD(NAME, "wider")
        val Narrower = PD(NAME, "narrower")
        val UltraCondensed = PD(NAME, "ultra-condensed")
        val ExtraCondensed = PD(NAME, "extra-condensed")
        val Condensed = PD(NAME, "condensed")
        val SemiCondensed = PD(NAME, "semi-condensed")
        val SemiExpanded = PD(NAME, "semi-expanded")
        val Expanded = PD(NAME, "expanded")
        val ExtraExpanded = PD(NAME, "extra-expanded")
        val UltraExpanded = PD(NAME, "ultra-expanded")
        val Inherit = PD(NAME, "inherit")
    }

    object FontVariant {
        const val NAME = "font-variant"
        val Normal = PD(NAME, "normal")
        val SmallCaps = PD(NAME, "small-caps")
        val Inherit = PD(NAME, "inherit")
    }

    object FontWeight {
        const val NAME = "font-weight"
        val Normal = PD(NAME, "normal")
        val Bold = PD(NAME, "bold")
        val Bolder = PD(NAME, "bolder")
        val Lighter = PD(NAME, "lighter")
        val X100 = PD(NAME, "100")
        val X200 = PD(NAME, "200")
        val X300 = PD(NAME, "300")
        val X400 = PD(NAME, "400")
        val X500 = PD(NAME, "500")
        val X600 = PD(NAME, "600")
        val X700 = PD(NAME, "700")
        val X800 = PD(NAME, "800")
        val X900 = PD(NAME, "900")
        val Inherit = PD(NAME, "inherit")
    }

    object Height {
        const val NAME = "height"
        val FillAvailable = PD(NAME, "-webkit-fill-available")
        val FitContent = PD(NAME, "fit-content")
        val MinContent = PD(NAME, "min-content")
        val MaxContent = PD(NAME, "max-content")
    }

    object LineBreak {
        const val NAME = "line-break"
        val AnyWhere = PD(NAME, "any-where")
        val Auto = PD(NAME, "auto")
        val Loose = PD(NAME, "loose")
        val Normal = PD(NAME, "normal")
        val Strict = PD(NAME, "strict")
    }

    object ListStylePosition {
        const val NAME = "list-style-position"
        val Outside = PD(NAME, "outside")
        val Inside = PD(NAME, "insdie")
        val Inherit = PD(NAME, "inherit")
    }

    object ListStyleType {
        const val NAME = "list-style-type"
        val None = PD(NAME, "none")
        val Asterisks = PD(NAME, "asterisks")
        val Box = PD(NAME, "box")
        val Check = PD(NAME, "check")
        val Circle = PD(NAME, "circle")
        val Diamond = PD(NAME, "diamond")
        val Disc = PD(NAME, "disc")
        val Hyphen = PD(NAME, "hyphen")
        val Square = PD(NAME, "square")
        val Decimal = PD(NAME, "decimal")
        val DecimalLeadingZero = PD(NAME, "decimal-leading-zero")
        val LowerRoman = PD(NAME, "lower-roman")
        val UpperRoman = PD(NAME, "upper-roman")
        val LowerAlpha = PD(NAME, "lower-alpha")
        val UpPerAlpha = PD(NAME, "up-per-alpha")
        val LowerGreek = PD(NAME, "lower-greek")
        val LowerLatin = PD(NAME, "lower-latin")
        val UpperLatin = PD(NAME, "upper-latin")
        val Hebrew = PD(NAME, "hebrew")
        val Armenian = PD(NAME, "armenian")
        val GeorGian = PD(NAME, "geor-gian")
        val CjkIdeographic = PD(NAME, "cjk-ideographic")
        val Hiragana = PD(NAME, "hiragana")
        val Katakana = PD(NAME, "katakana")
        val HiraGanaIroha = PD(NAME, "hira-gana-iroha")
        val KatakanaIroha = PD(NAME, "katakana-iroha")
        val Footnotes = PD(NAME, "footnotes")
    }

    object JustifyContent : FlexAlignBase("justify-content") {
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val SpaceAround = PD(NAME, "space-around")
        val SpaceBetween = PD(NAME, "space-between")
        val SpaceEvenly = PD(NAME, "space-evenly")
    }

    object JustifyItems : FlexAlignBase("justify-items") {
        val Baseline = PD(NAME, "baseline")
        val Left = PD(NAME, "left")
        val Legacy = PD(NAME, "legacy")
        val Right = PD(NAME, "right")
        val SelfEnd = PD(NAME, "self-end")
        val SelfStart = PD(NAME, "self-start")
    }

    object JustifySelf : FlexAlignBase("justify-self") {
        val Auto = PD(NAME, "auto")
        val Baseline = PD(NAME, "baseline")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val SelfEnd = PD(NAME, "self-end")
        val SelfStart = PD(NAME, "self-start")
    }

    object Margin {
        const val NAME = "margin"
        val Zero = PD(NAME, "0")
    }

    object Marks {
        const val NAME = "marks"
        val Crop = PD(NAME, "crop")
        val Cross = PD(NAME, "cross")
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object Outline {
        const val NAME = "outline"
        val None = PD(NAME, "none")
    }

    object OutlineColor {
        const val NAME = "outline-color"
        val Invert = PD(NAME, "invert")
        val Inherit = PD(NAME, "inherit")
    }

    object OutlineStyle {
        const val NAME = "outline-style"
        val None = PD(NAME, "None")
        val Dotted = PD(NAME, "dotted")
        val Dashed = PD(NAME, "dashed")
        val Solid = PD(NAME, "solid")
        val Double = PD(NAME, "double")
        val Groove = PD(NAME, "groove")
        val Ridge = PD(NAME, "ridge")
        val Inset = PD(NAME, "inset")
        val Outset = PD(NAME, "outset")
    }

    object Overflow {
        const val NAME = "overflow"
        val Visible = PD(NAME, "visible")
        val Hidden = PD(NAME, "hidden")
        val Scroll = PD(NAME, "scroll")
        val Auto = PD(NAME, "auto")
        val NoDisplay = PD(NAME, "no-display")
        val NoContent = PD(NAME, "no-content")
    }

    object PageBreakAfter {
        const val NAME = "page-break-after"
        val Auto = PD(NAME, "auto")
        val Always = PD(NAME, "always")
        val Avoid = PD(NAME, "avoid")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Inherit = PD(NAME, "inherit")
    }

    object PageBreakBefore {
        const val NAME = "page-break-before"
        val Auto = PD(NAME, "auto")
        val Always = PD(NAME, "always")
        val Avoid = PD(NAME, "avoid")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Inherit = PD(NAME, "inherit")
    }

    object PageBreakInside {
        const val NAME = "page-break-inside"
        val Avoid = PD(NAME, "avoid")
        val Auto = PD(NAME, "auto")
        val Inherit = PD(NAME, "inherit")
    }

    object Padding {
        const val NAME = "padding"
        val Zero = PD(NAME, "0")
    }

    object Position {
        const val NAME = "position"
        val Static = PD(NAME, "static")
        val Relative = PD(NAME, "relative")
        val Absolute = PD(NAME, "absolute")
        val Fixed = PD(NAME, "fixed")
        val Inherit = PD(NAME, "inherit")
    }

    object Pitch {
        const val NAME = "pitch"
        val XLow = PD(NAME, "x-low")
        val Low = PD(NAME, "low")
        val Medium = PD(NAME, "medium")
        val High = PD(NAME, "high")
        val XHigh = PD(NAME, "x-high")
        val Inherit = PD(NAME, "inherit")
    }

    object PlayDuring {
        const val NAME = "play-during"
        val Mix = PD(NAME, "mix")
        val Repeat = PD(NAME, "repeat")
        val Auto = PD(NAME, "auto")
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object PointerEvents {
        const val NAME = "pointer-events"
        val All = PD(NAME, "all")
        val Auto = PD(NAME, "auto")
        val BoundingBox = PD(NAME, "bounding-box")
        val Fill = PD(NAME, "fill")
        val None = PD(NAME, "none")
        val Painted = PD(NAME, "painted")
        val Stroke = PD(NAME, "stroke")
        val Visible = PD(NAME, "visible")
        val VisibleFill = PD(NAME, "visiblefill")
        val VisiblePainted = PD(NAME, "visiblepainted")
        val VisibleStroke = PD(NAME, "visiblestroke")
    }

    object Size {
        const val NAME = "size"
        val Auto = PD(NAME, "auto")
        val Portrait = PD(NAME, "portrait")
        val Landscape = PD(NAME, "landscape")
        val Inherit = PD(NAME, "inherit")
    }

    object Speak {
        const val NAME = "speak"
        val Normal = PD(NAME, "normal")
        val None = PD(NAME, "none")
        val SpellOut = PD(NAME, "spell-out")
        val Inherit = PD(NAME, "inherit")
    }

    object SpeakHeader {
        const val NAME = "speak-header"
        val Once = PD(NAME, "once")
        val Always = PD(NAME, "always")
        val Inherit = PD(NAME, "inherit")
    }

    object SpeakNumeric {
        const val NAME = "speak-numeric"
        val Digits = PD(NAME, "digits")
        val Continuous = PD(NAME, "continuous")
        val Inherit = PD(NAME, "inherit")
    }

    object SpeakPunctuation {
        const val NAME = "speak-punctuation"
        val Code = PD(NAME, "code")
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object SpeakRate {
        const val NAME = "speak-rate"
        val XSlow = PD(NAME, "x-slow")
        val Slow = PD(NAME, "slow")
        val Medium = PD(NAME, "medium")
        val Fast = PD(NAME, "fast")
        val XFast = PD(NAME, "x-fast")
        val Faster = PD(NAME, "faster")
        val Slower = PD(NAME, "slower")
        val Inherit = PD(NAME, "inherit")
    }

    object TableLayout {
        const val NAME = "table-layout"
        val Auto = PD(NAME, "auto")
        val Fixed = PD(NAME, "fixed")
        val Inherit = PD(NAME, "inherit")
    }

    object TextAlign {
        const val NAME = "text-align"
        val Start = PD(NAME, "start")
        val End = PD(NAME, "end")
        val Left = PD(NAME, "left")
        val Right = PD(NAME, "right")
        val Center = PD(NAME, "center")
        val Justify = PD(NAME, "justify")
    }

    object TextDecoration {
        const val NAME = "text-decoration"
        val None = PD(NAME, "none")
        val Underline = PD(NAME, "underline")
        val Overline = PD(NAME, "overline")
        val LineThrough = PD(NAME, "line-through")
        val Blink = PD(NAME, "blink")
        val Inherit = PD(NAME, "inherit")
    }

    object TextOverflow {
        const val NAME = "text-overflow"
        val Clip = PD(NAME, "clip")
        val Ellipsis = PD(NAME, "ellipsis")
        val Inherit = PD(NAME, "inherit")
    }

    object TextTransform {
        const val NAME = "text-transform"
        val Capitalize = PD(NAME, "capitalize")
        val Uppercase = PD(NAME, "uppercase")
        val Lowercase = PD(NAME, "lowercase")
        val None = PD(NAME, "none")
        val Inherit = PD(NAME, "inherit")
    }

    object UnicodeBidi {
        const val NAME = "unicode-bidi"
        val Normal = PD(NAME, "normal")
        val Embed = PD(NAME, "embed")
        val BidiOverride = PD(NAME, "bidi-override")
        val Inherit = PD(NAME, "inherit")
    }

    object UserSelect {
        const val NAME = "user-select"
        val None = PD(NAME, "none")
        val Text = PD(NAME, "text")
        val All = PD(NAME, "all")
        val Element = PD(NAME, "element")
    }

    object VerticalAlign {
        const val NAME = "vertical-align"
        val Baseline = PD(NAME, "baseline")
        val Sub = PD(NAME, "sub")
        val Super = PD(NAME, "super")
        val Top = PD(NAME, "top")
        val TextTop = PD(NAME, "text-top")
        val Middle = PD(NAME, "middle")
        val Bottom = PD(NAME, "bottom")
        val TextBottom = PD(NAME, "text-bottom")
    }

    object Visibility {
        const val NAME = "visibility"
        val Visible = PD(NAME, "visible")
        val Hidden = PD(NAME, "hidden")
        val Collapse = PD(NAME, "collapse")
    }

    object Volume {
        const val NAME = "volume"
        val Silent = PD(NAME, "silent")
        val XSoft = PD(NAME, "x-soft")
        val Soft = PD(NAME, "soft")
        val Medium = PD(NAME, "medium")
        val Loud = PD(NAME, "loud")
        val XLoud = PD(NAME, "x-loud")
        val Inherit = PD(NAME, "inherit")
    }

    object WhiteSpace {
        const val NAME = "white-space"
        val BreakSpaces = PD(NAME, "break-spaces")
        val Normal = PD(NAME, "normal")
        val Pre = PD(NAME, "pre")
        val PreLine = PD(NAME, "pre-line")
        val PreWrap = PD(NAME, "pre-wrap")
        val Nowrap = PD(NAME, "nowrap")
        val Inherit = PD(NAME, "inherit")
    }

    abstract class WidthBase(val NAME: String) {
        val Auto = PD(NAME, "auto")
        val FillAvailable = PD(NAME, "-webkit-fill-available")
        val FitContent = PD(NAME, "fit-content")
        val MinContent = PD(NAME, "min-content")
        val MaxContent = PD(NAME, "max-content")
        val Percent100 = PD(NAME, "100%")
        val Zero = PD(NAME, "0")
    }

    object Width : WidthBase("width")
    object MaxWidth : WidthBase("max-width")
    object MinWidth : WidthBase("min-width")

    object WordBreak {
        const val NAME = "word-break"
        val BreakAll = PD(NAME, "break-all")
        val BreakWord = PD(NAME, "break-word")
        val KeepAll = PD(NAME, "keep-all")
        val Normal = PD(NAME, "normal")
    }

    //////////////////////////////////////////////////////////////////////
}

