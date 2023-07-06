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
package com.cplusedition.bot.dsl.css.api

import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.dsl.css.impl.Declaration
import java.util.*

enum class PK(val property: String) {
    AlignContent("align-content"),
    AlignItems("align-items"),
    AlignSelf("align-self"),
    AlignmentBaseline("alignment-baseline"),
    All("all"),
    Animation("animation"),
    AnimationDelay("animation-delay"),
    AnimationDirection("animation-direction"),
    AnimationDuration("animation-duration"),
    AnimationFillMode("animation-fill-mode"),
    AnimationIterationCount("animation-iteration-count"),
    AnimationName("animation-name"),
    AnimationPlayState("animation-play-state"),
    AnimationTimingFunction("animation-timing-function"),
    BackdropFilter("backdrop-filter"),
    BackfaceVisibility("backface-visibility"),
    Background("background"),
    BackgroundAttachment("background-attachment"),
    BackgroundBlendMode("background-blend-mode"),
    BackgroundClip("background-clip"),
    BackgroundColor("background-color"),
    BackgroundImage("background-image"),
    BackgroundOrigin("background-origin"),
    BackgroundPosition("background-position"),
    BackgroundRepeat("background-repeat"),
    BackgroundSize("background-size"),
    BaselineShift("baseline-shift"),
    Border("border"),
    BorderBottom("border-bottom"),
    BorderBottomColor("border-bottom-color"),
    BorderBottomLeftRadius("border-bottom-left-radius"),
    BorderBottomRightRadius("border-bottom-right-radius"),
    BorderBottomStyle("border-bottom-style"),
    BorderBottomWidth("border-bottom-width"),
    BorderCollapse("border-collapse"),
    BorderColor("border-color"),
    BorderImage("border-image"),
    BorderImageOutset("border-image-outset"),
    BorderImageRepeat("border-image-repeat"),
    BorderImageSlice("border-image-slice"),
    BorderImageSource("border-image-source"),
    BorderImageWidth("border-image-width"),
    BorderLeft("border-left"),
    BorderLeftColor("border-left-color"),
    BorderLeftStyle("border-left-style"),
    BorderLeftWidth("border-left-width"),
    BorderRadius("border-radius"),
    BorderRight("border-right"),
    BorderRightColor("border-right-color"),
    BorderRightStyle("border-right-style"),
    BorderRightWidth("border-right-width"),
    BorderSpacing("border-spacing"),
    BorderStyle("border-style"),
    BorderTop("border-top"),
    BorderTopColor("border-top-color"),
    BorderTopLeftRadius("border-top-left-radius"),
    BorderTopRightRadius("border-top-right-radius"),
    BorderTopStyle("border-top-style"),
    BorderTopWidth("border-top-width"),
    BorderWidth("border-width"),
    Bottom("bottom"),
    BoxShadow("box-shadow"),
    BoxSizing("box-sizing"),
    BreakAfter("break-after"),
    BreakBefore("break-before"),
    BreakInside("break-inside"),
    BufferedRendering("buffered-rendering"),
    CaptionSide("caption-side"),
    Clear("clear"),
    Clip("clip"),
    ClipPath("clip-path"),
    ClipRule("clip-rule"),
    Color("color"),
    ColorInterpolation("color-interpolation"),
    ColorInterpolationFilters("color-interpolation-filters"),
    ColorRendering("color-rendering"),
    ColumnCount("column-count"),
    ColumnFill("column-fill"),
    ColumnGap("column-gap"),
    ColumnRule("column-rule"),
    ColumnRuleColor("column-rule-color"),
    ColumnRuleStyle("column-rule-style"),
    ColumnRuleWidth("column-rule-width"),
    ColumnSpan("column-span"),
    ColumnWidth("column-width"),
    Columns("columns"),
    Content("content"),
    CounterIncrement("counter-increment"),
    CounterReset("counter-reset"),
    Cursor("cursor"),
    Cx("cx"),
    Cy("cy"),
    Direction("direction"),
    Display("display"),
    DominantBaseline("dominant-baseline"),
    EmptyCells("empty-cells"),
    Fill("fill"),
    FillOpacity("fill-opacity"),
    FillRule("fill-rule"),
    Filter("filter"),
    Flex("flex"),
    FlexBasis("flex-basis"),
    FlexDirection("flex-direction"),
    FlexFlow("flex-flow"),
    FlexGrow("flex-grow"),
    FlexShrink("flex-shrink"),
    FlexWrap("flex-wrap"),
    Float("float"),
    FloodColor("flood-color"),
    FloodOpacity("flood-opacity"),
    Font("font"),
    FontFamily("font-family"),
    FontFeatureSettings("font-feature-settings"),
    FontSize("font-size"),
    FontSizeAdjust("font-size-adjust"),
    FontStretch("font-stretch"),
    FontStyle("font-style"),
    FontVariant("font-variant"),
    FontVariantCaps("font-variant-caps"),
    FontVariantEastAsian("font-variant-east-asian"),
    FontVariantLigatures("font-variant-ligatures"),
    FontVariantNumeric("font-variant-numeric"),
    FontWeight("font-weight"),
    Grid("grid"),
    GridArea("grid-area"),
    GridAutoColumns("grid-auto-columns"),
    GridAutoFlow("grid-auto-flow"),
    GridAutoRows("grid-auto-rows"),
    GridColumn("grid-column"),
    GridColumnEnd("grid-column-end"),
    GridColumnGap("grid-column-gap"),
    GridColumnStart("grid-column-start"),
    GridRow("grid-row"),
    GridRowEnd("grid-row-end"),
    GridRowGap("grid-row-gap"),
    GridRowStart("grid-row-start"),
    GridTemplate("grid-template"),
    GridTemplateAreas("grid-template-areas"),
    GridTemplateColumns("grid-template-columns"),
    GridTemplateRows("grid-template-rows"),
    Height("height"),
    Hyphens("hyphens"),
    ImageRendering("image-rendering"),
    Isolation("isolation"),
    JustifyContent("justify-content"),
    JustifyItems("justify-items"),
    JustifySelf("justify-self"),
    Left("left"),
    LetterSpacing("letter-spacing"),
    LightingColor("lighting-color"),
    LineHeight("line-height"),
    ListStyle("list-style"),
    ListStyleImage("list-style-image"),
    ListStylePosition("list-style-position"),
    ListStyleType("list-style-type"),
    Margin("margin"),
    MarginBottom("margin-bottom"),
    MarginLeft("margin-left"),
    MarginRight("margin-right"),
    MarginTop("margin-top"),
    MarkerEnd("marker-end"),
    MarkerMid("marker-mid"),
    MarkerStart("marker-start"),
    Mask("mask"),
    MaskType("mask-type"),
    MaxHeight("max-height"),
    MaxWidth("max-width"),
    MinHeight("min-height"),
    MinWidth("min-width"),
    MixBlendMode("mix-blend-mode"),
    ObjectFit("object-fit"),
    ObjectPosition("object-position"),
    Opacity("opacity"),
    Order("order"),
    Orphans("orphans"),
    Outline("outline"),
    OutlineColor("outline-color"),
    OutlineOffset("outline-offset"),
    OutlineStyle("outline-style"),
    OutlineWidth("outline-width"),
    Overflow("overflow"),
    OverflowWrap("overflow-wrap"),
    OverflowX("overflow-x"),
    OverflowY("overflow-y"),
    OverscrollBehavior("overscroll-behavior"),
    OverscrollBehaviorX("overscroll-behavior-x"),
    OverscrollBehaviorY("overscroll-behavior-y"),
    Padding("padding"),
    PaddingBottom("padding-bottom"),
    PaddingLeft("padding-left"),
    PaddingRight("padding-right"),
    PaddingTop("padding-top"),
    Page("page"),
    PageBreakAfter("page-break-after"),
    PageBreakBefore("page-break-before"),
    PageBreakInside("page-break-inside"),
    PaintOrder("paint-order"),
    Perspective("perspective"),
    PerspectiveOrigin("perspective-origin"),
    PointerEvents("pointer-events"),
    Position("position"),
    Quotes("quotes"),
    R("r"),
    Resize("resize"),
    Right("right"),
    RowGap("row-gap"),
    Rx("rx"),
    Ry("ry"),
    ShapeImageThreshold("shape-image-threshold"),
    ShapeMargin("shape-margin"),
    ShapeOutside("shape-outside"),
    ShapeRendering("shape-rendering"),
    Size("size"),
    Speak("speak"),
    Src("src"),
    StopColor("stop-color"),
    StopOpacity("stop-opacity"),
    Stroke("stroke"),
    StrokeDasharray("stroke-dasharray"),
    StrokeDashoffset("stroke-dashoffset"),
    StrokeLinecap("stroke-linecap"),
    StrokeLinejoin("stroke-linejoin"),
    StrokeMiterlimit("stroke-miterlimit"),
    StrokeOpacity("stroke-opacity"),
    StrokeWidth("stroke-width"),
    TabSize("tab-size"),
    TableLayout("table-layout"),
    TextAlign("text-align"),
    TextAlignLast("text-align-last"),
    TextAnchor("text-anchor"),
    TextDecoration("text-decoration"),
    TextDecorationColor("text-decoration-color"),
    TextDecorationLine("text-decoration-line"),
    TextDecorationStyle("text-decoration-style"),
    TextIndent("text-indent"),
    TextOverflow("text-overflow"),
    TextRendering("text-rendering"),
    TextShadow("text-shadow"),
    TextSizeAdjust("text-size-adjust"),
    TextTransform("text-transform"),
    TextUnderlinePosition("text-underline-position"),
    Top("top"),
    Transform("transform"),
    TransformOrigin("transform-origin"),
    TransformStyle("transform-style"),
    Transition("transition"),
    TransitionDelay("transition-delay"),
    TransitionDuration("transition-duration"),
    TransitionProperty("transition-property"),
    TransitionTimingFunction("transition-timing-function"),
    UnicodeBidi("unicode-bidi"),
    UserSelect("user-select"),
    VectorEffect("vector-effect"),
    VerticalAlign("vertical-align"),
    Visibility("visibility"),
    WhiteSpace("white-space"),
    Widows("widows"),
    Width("width"),
    WillChange("will-change"),
    WordBreak("word-break"),
    WordSpacing("word-spacing"),
    WordWrap("word-wrap"),
    WritingMode("writing-mode"),
    X("x"),
    Y("y"),
    ZIndex("z-index"),
    Zoom("zoom"),
    WebkitAlignContent("-webkit-align-content"),
    WebkitAlignItems("-webkit-align-items"),
    WebkitAlignSelf("-webkit-align-self"),
    WebkitAnimation("-webkit-animation"),
    WebkitAnimationDelay("-webkit-animation-delay"),
    WebkitAnimationDirection("-webkit-animation-direction"),
    WebkitAnimationDuration("-webkit-animation-duration"),
    WebkitAnimationFillMode("-webkit-animation-fill-mode"),
    WebkitAnimationIterationCount("-webkit-animation-iteration-count"),
    WebkitAnimationName("-webkit-animation-name"),
    WebkitAnimationPlayState("-webkit-animation-play-state"),
    WebkitAnimationTimingFunction("-webkit-animation-timing-function"),
    WebkitAppearance("-webkit-appearance"),
    WebkitAspectRatio("-webkit-aspect-ratio"),
    WebkitBackfaceVisibility("-webkit-backface-visibility"),
    WebkitBackgroundClip("-webkit-background-clip"),
    WebkitBackgroundOrigin("-webkit-background-origin"),
    WebkitBackgroundSize("-webkit-background-size"),
    WebkitBorderBottomLeftRadius("-webkit-border-bottom-left-radius"),
    WebkitBorderBottomRightRadius("-webkit-border-bottom-right-radius"),
    WebkitBorderHorizontalSpacing("-webkit-border-horizontal-spacing"),
    WebkitBorderImage("-webkit-border-image"),
    WebkitBorderRadius("-webkit-border-radius"),
    WebkitBorderTopLeftRadius("-webkit-border-top-left-radius"),
    WebkitBorderTopRightRadius("-webkit-border-top-right-radius"),
    WebkitBorderVerticalSpacing("-webkit-border-vertical-spacing"),
    WebkitBoxAlign("-webkit-box-align"),
    WebkitBoxDecorationBreak("-webkit-box-decoration-break"),
    WebkitBoxDirection("-webkit-box-direction"),
    WebkitBoxFlex("-webkit-box-flex"),
    WebkitBoxFlexGroup("-webkit-box-flex-group"),
    WebkitBoxLines("-webkit-box-lines"),
    WebkitBoxOrdinalGroup("-webkit-box-ordinal-group"),
    WebkitBoxOrient("-webkit-box-orient"),
    WebkitBoxPack("-webkit-box-pack"),
    WebkitBoxReflect("-webkit-box-reflect"),
    WebkitBoxShadow("-webkit-box-shadow"),
    WebkitBoxSizing("-webkit-box-sizing"),
    WebkitClipPath("-webkit-clip-path"),
    WebkitColumnBreakAfter("-webkit-column-break-after"),
    WebkitColumnBreakBefore("-webkit-column-break-before"),
    WebkitColumnBreakInside("-webkit-column-break-inside"),
    WebkitColumnCount("-webkit-column-count"),
    WebkitColumnGap("-webkit-column-gap"),
    WebkitColumnRule("-webkit-column-rule"),
    WebkitColumnRuleColor("-webkit-column-rule-color"),
    WebkitColumnRuleStyle("-webkit-column-rule-style"),
    WebkitColumnRuleWidth("-webkit-column-rule-width"),
    WebkitColumnSpan("-webkit-column-span"),
    WebkitColumnWidth("-webkit-column-width"),
    WebkitColumns("-webkit-columns"),
    WebkitFilter("-webkit-filter"),
    WebkitFlex("-webkit-flex"),
    WebkitFlexBasis("-webkit-flex-basis"),
    WebkitFlexDirection("-webkit-flex-direction"),
    WebkitFlexFlow("-webkit-flex-flow"),
    WebkitFlexGrow("-webkit-flex-grow"),
    WebkitFlexShrink("-webkit-flex-shrink"),
    WebkitFlexWrap("-webkit-flex-wrap"),
    WebkitFontFeatureSettings("-webkit-font-feature-settings"),
    WebkitFontSizeDelta("-webkit-font-size-delta"),
    WebkitFontSmoothing("-webkit-font-smoothing"),
    WebkitHyphenateCharacter("-webkit-hyphenate-character"),
    WebkitJustifyContent("-webkit-justify-content"),
    WebkitLineBreak("-webkit-line-break"),
    WebkitLineClamp("-webkit-line-clamp"),
    WebkitLocale("-webkit-locale"),
    WebkitMarginAfterCollapse("-webkit-margin-after-collapse"),
    WebkitMarginBeforeCollapse("-webkit-margin-before-collapse"),
    WebkitMask("-webkit-mask"),
    WebkitMaskBoxImage("-webkit-mask-box-image"),
    WebkitMaskBoxImageOutset("-webkit-mask-box-image-outset"),
    WebkitMaskBoxImageRepeat("-webkit-mask-box-image-repeat"),
    WebkitMaskBoxImageSlice("-webkit-mask-box-image-slice"),
    WebkitMaskBoxImageSource("-webkit-mask-box-image-source"),
    WebkitMaskBoxImageWidth("-webkit-mask-box-image-width"),
    WebkitMaskClip("-webkit-mask-clip"),
    WebkitMaskComposite("-webkit-mask-composite"),
    WebkitMaskImage("-webkit-mask-image"),
    WebkitMaskOrigin("-webkit-mask-origin"),
    WebkitMaskPosition("-webkit-mask-position"),
    WebkitMaskRepeat("-webkit-mask-repeat"),
    WebkitMaskSize("-webkit-mask-size"),
    WebkitOpacity("-webkit-opacity"),
    WebkitOrder("-webkit-order"),
    WebkitOverflowScrolling("-webkit-overflow-scrolling"),
    WebkitPerspective("-webkit-perspective"),
    WebkitPerspectiveOrigin("-webkit-perspective-origin"),
    WebkitPrintColorAdjust("-webkit-print-color-adjust"),
    WebkitRtlOrdering("-webkit-rtl-ordering"),
    WebkitRubyPosition("-webkit-ruby-position"),
    WebkitShapeImageThreshold("-webkit-shape-image-threshold"),
    WebkitShapeMargin("-webkit-shape-margin"),
    WebkitShapeOutside("-webkit-shape-outside"),
    WebkitTapHighlightColor("-webkit-tap-highlight-color"),
    WebkitTextCombine("-webkit-text-combine"),
    WebkitTextDecorationsInEffect("-webkit-text-decorations-in-effect"),
    WebkitTextEmphasis("-webkit-text-emphasis"),
    WebkitTextEmphasisColor("-webkit-text-emphasis-color"),
    WebkitTextEmphasisPosition("-webkit-text-emphasis-position"),
    WebkitTextEmphasisStyle("-webkit-text-emphasis-style"),
    WebkitTextFillColor("-webkit-text-fill-color"),
    WebkitTextOrientation("-webkit-text-orientation"),
    WebkitTextSecurity("-webkit-text-security"),
    WebkitTextSizeAdjust("-webkit-text-size-adjust"),
    WebkitTextStroke("-webkit-text-stroke"),
    WebkitTextStrokeColor("-webkit-text-stroke-color"),
    WebkitTextStrokeWidth("-webkit-text-stroke-width"),
    WebkitTouchCallout("-webkit-touch-callout"),
    WebkitTransform("-webkit-transform"),
    WebkitTransformOrigin("-webkit-transform-origin"),
    WebkitTransformStyle("-webkit-transform-style"),
    WebkitTransition("-webkit-transition"),
    WebkitTransitionDelay("-webkit-transition-delay"),
    WebkitTransitionDuration("-webkit-transition-duration"),
    WebkitTransitionProperty("-webkit-transition-property"),
    WebkitTransitionTimingFunction("-webkit-transition-timing-function"),
    WebkitUserDrag("-webkit-user-drag"),
    WebkitUserModify("-webkit-user-modify"),
    WebkitUserSelect("-webkit-user-select"),
    WebkitWritingMode("-webkit-writing-mode");

    fun set(expr: Any): IDeclaration {
        return s(expr)
    }

    fun set(expr: String): IDeclaration {
        return s(expr)
    }

    fun set(expr: Boolean): IDeclaration {
        return s(expr)
    }

    fun set(expr: Int): IDeclaration {
        return s(expr)
    }

    fun set(expr: Double): IDeclaration {
        return s(expr)
    }

    fun setf(format: String, vararg args: Any): IDeclaration {
        return Declaration(property, TextUt.format(format, *args))
    }

    /**
     * Set value, shorthand for set().
     */
    fun s(expr: Any): IDeclaration {
        return Declaration(property, expr)
    }

    /**
     * Set value, shorthand for set().
     */
    fun s(expr: String): IDeclaration {
        return Declaration(property, expr)
    }

    /**
     * Set value, shorthand for set().
     */
    fun s(expr: Boolean): IDeclaration {
        return Declaration(property, expr.toString())
    }

    /**
     * Set value, shorthand for set().
     */
    fun s(expr: Int): IDeclaration {
        return Declaration(property, expr.toString())
    }

    /**
     * Set value, shorthand for set().
     */
    fun s(expr: Double): IDeclaration {
        return Declaration(property, expr.toString())
    }

    /**
     * Set value, shorthand for setf().
     */
    fun f(format: String, vararg args: Any): IDeclaration {
        return Declaration(property, TextUt.format(format, *args))
    }

    /**
     * Set value as &lt;n>px
     */
    fun px(n: Int): IDeclaration {
        return set(n.toString() + "px")
    }

    /**
     * Set value as &lt;n>px
     */
    fun px(vararg a: Int): IDeclaration {
        val b = StringBuilder()
        for (n in a) {
            if (b.length > 0) {
                b.append(' ')
            }
            b.append(n.toString())
            b.append("px")
        }
        return set(b)
    }

    /**
     * Set value as &lt;n>em
     */
    fun em(n: Double): IDeclaration {
        return set(n.toString() + "em")
    }

    /**
     * Set value as &lt;n>em
     */
    fun em(vararg a: Double): IDeclaration {
        val b = StringBuilder()
        for (n in a) {
            if (b.length > 0) {
                b.append(' ')
            }
            b.append(n.toString())
            b.append("em")
        }
        return set(b)
    }

    override fun toString(): String {
        return property
    }

    companion object {
        private var lookup = lazy {
            val map = TreeMap<String, PK>()
            for (e in values()) {
                map[e.property] = e
            }
            map
        }

        @Synchronized
        operator fun get(name: String?): PK? {
            return lookup.value[name]
        }
    }

}
