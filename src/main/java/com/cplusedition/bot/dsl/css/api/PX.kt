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

import com.cplusedition.bot.dsl.css.impl.Declaration

enum class PX(val property: String) {
    MozBinding("-moz-binding"),
    MozBorderBottomColors("-moz-border-bottom-colors"),
    MozBorderEnd("-moz-border-end"),
    MozBorderEndColor("-moz-border-end-color"),
    MozBorderEndStyle("-moz-border-end-style"),
    MozBorderEndWidth("-moz-border-end-width"),
    MozBorderLeftColors("-moz-border-left-colors"),
    MozBorderRightColors("-moz-border-right-colors"),
    MozBorderStart("-moz-border-start"),
    MozBorderStartColor("-moz-border-start-color"),
    MozBorderStartStyle("-moz-border-start-style"),
    MozBorderStartWidth("-moz-border-start-width"),
    MozBorderTopColors("-moz-border-top-colors"),
    MozBoxAlign("-moz-box-align"),
    MozBoxDirection("-moz-box-direction"),
    MozBoxFlex("-moz-box-flex"),
    MozBoxOrdinalGroup("-moz-box-ordinal-group"),
    MozBoxOrient("-moz-box-orient"),
    MozBoxPack("-moz-box-pack"),
    MozFloatEdge("-moz-float-edge"),
    MozForceBrokenImageIcon("-moz-force-broken-image-icon"),
    MozImageRegion("-moz-image-region"),
    MozMarginEnd("-moz-margin-end"),
    MozMarginStart("-moz-margin-start"),
    MozOrient("-moz-orient"),
    MozOutlineRadius("-moz-outline-radius"),
    MozOutlineRadiusBottomleft("-moz-outline-radius-bottomleft"),
    MozOutlineRadiusBottomright("-moz-outline-radius-bottomright"),
    MozOutlineRadiusTopleft("-moz-outline-radius-topleft"),
    MozOutlineRadiusTopright("-moz-outline-radius-topright"),
    MozPaddingEnd("-moz-padding-end"),
    MozPaddingStart("-moz-padding-start"),
    MozScriptLevel("-moz-script-level"),
    MozScriptMinSize("-moz-script-min-size"),
    MozScriptSizeMultiplier("-moz-script-size-multiplier"),
    MozStackSizing("-moz-stack-sizing"),
    MozTextBlink("-moz-text-blink"),
    MozUserFocus("-moz-user-focus"),
    MozUserInput("-moz-user-input"),
    MozUserModify("-moz-user-modify"),
    MozUserSelect("-moz-user-select"),
    MozWindowShadow("-moz-window-shadow"),
    WebkitAspectRatio("-webkit-aspect-ratio"),
    WebkitBackgroundComposite("-webkit-background-composite"),
    WebkitBorderAfter("-webkit-border-after"),
    WebkitBorderAfterColor("-webkit-border-after-color"),
    WebkitBorderAfterStyle("-webkit-border-after-style"),
    WebkitBorderAfterWidth("-webkit-border-after-width"),
    WebkitBorderBefore("-webkit-border-before"),
    WebkitBorderBeforeColor("-webkit-border-before-color"),
    WebkitBorderBeforeStyle("-webkit-border-before-style"),
    WebkitBorderBeforeWidth("-webkit-border-before-width"),
    WebkitBorderEnd("-webkit-border-end"),
    WebkitBorderEndColor("-webkit-border-end-color"),
    WebkitBorderEndStyle("-webkit-border-end-style"),
    WebkitBorderEndWidth("-webkit-border-end-width"),
    WebkitBorderFit("-webkit-border-fit"),
    WebkitBorderHorizontalSpacing("-webkit-border-horizontal-spacing"),
    WebkitBorderStart("-webkit-border-start"),
    WebkitBorderStartColor("-webkit-border-start-color"),
    WebkitBorderStartStyle("-webkit-border-start-style"),
    WebkitBorderStartWidth("-webkit-border-start-width "),
    WebkitBorderVerticalSpacing("-webkit-border-vertical-spacing"),
    WebkitBoxAlign("-webkit-box-align"),
    WebkitBoxDirection("-webkit-box-direction"),
    WebkitBoxFlex("-webkit-box-flex"),
    WebkitBoxFlexGroup("-webkit-box-flex-group"),
    WebkitBoxLines("-webkit-box-lines"),
    WebkitBoxOrdinalGroups("-webkit-box-ordinal-groups"),
    WebkitBoxOrient("-webkit-box-orient"),
    WebkitBoxPack("-webkit-box-pack"),
    WebkitBoxReflect("-webkit-box-reflect"),
    WebkitBoxShadow("-webkit-box-shadow"),
    WebkitColumnAxis("-webkit-column-axis"),
    WebkitColumnBreakAfter("-webkit-column-break-after"),
    WebkitColumnBreakBefore("-webkit-column-break-before"),
    WebkitColumnBreakInside("-webkit-column-break-inside"),
    WebkitDashboardRegion("-webkit-dashboard-region"),
    WebkitFilter("-webkit-filter"),
    WebkitFontSmoothing("-webkit-font-smoothing"),
    WebkitHighlight("-webkit-highlight"),
    WebkitHyphenateCharset("-webkit-hyphenate-charset"),
    WebkitHyphenateLimitAfter("-webkit-hyphenate-limit-after"),
    WebkitHyphenateLimitBefore("-webkit-hyphenate-limit-before"),
    WebkitHyphenateLimitLines("-webkit-hyphenate-limit-lines"),
    WebkitLineAlign("-webkit-line-align"),
    WebkitLineBoxContain("-webkit-line-box-contain"),
    WebkitLineBreak("-webkit-line-break"),
    WebkitLineClamp("-webkit-line-clamp"),
    WebkitLineGrid("-webkit-line-grid"),
    WebkitLineSnap("-webkit-line-snap"),
    WebkitLocale("-webkit-locale"),
    WebkitLogicalHeight("-webkit-logical-height"),
    WebkitLogicalWidth("-webkit-logical-width"),
    WebkitMarginAfter("-webkit-margin-after"),
    WebkitMarginAfterCollapse("-webkit-margin-after-collapse"),
    WebkitMarginBefore("-webkit-margin-before"),
    WebkitMarginBeforeCollapse("-webkit-margin-before-collapse"),
    WebkitMarginBottomCollapse("-webkit-margin-bottom-collapse"),
    WebkitMarginCollapse("-webkit-margin-collapse"),
    WebkitMarginEnd("-webkit-margin-end"),
    WebkitMarginStart("-webkit-margin-start"),
    WebkitMarginTopCollapse("-webkit-margin-top-collapse"),
    WebkitMarquee("-webkit-marquee"),
    WebkitMarqueeDirection("-webkit-marquee-direction"),
    WebkitMarqueeIncrement("-webkit-marquee-increment"),
    WebkitMarqueeRepetition("-webkit-marquee-repetition"),
    WebkitMarqueeSpeed("-webkit-marquee-speed"),
    WebkitMarqueeStyle("-webkit-marquee-style"),
    WebkitMask("-webkit-mask"),
    WebkitMaskAttachment("-webkit-mask-attachment"),
    WebkitMaskBoxImage("-webkit-mask-box-image"),
    WebkitMaskBoxImageOutset("-webkit-mask-box-image-outset"),
    WebkitMaskBoxImageRepeat("-webkit-mask-box-image-repeat"),
    WebkitMaskBoxImageSlice("-webkit-mask-box-image-slice"),
    WebkitMaskBoxImageWidth("-webkit-mask-box-image-width"),
    WebkitMaskBoximageSource("-webkit-mask-boximage-source"),
    WebkitMaskClip("-webkit-mask-clip"),
    WebkitMaskComposite("-webkit-mask-composite"),
    WebkitMaskImage("-webkit-mask-image"),
    WebkitMaskOrigin("-webkit-mask-origin"),
    WebkitMaskPosition("-webkit-mask-position"),
    WebkitMaskPositionX("-webkit-mask-position-x"),
    WebkitMaskPositionY("-webkit-mask-position-y"),
    WebkitMaskRepeat("-webkit-mask-repeat"),
    WebkitMaskRepeatX("-webkit-mask-repeat-x"),
    WebkitMaskRepeatY("-webkit-mask-repeat-y"),
    WebkitMaskSize("-webkit-mask-size"),
    WebkitMatchNearestMailBlockquoteColor("-webkit-match-nearest-mail-blockquote-color"),
    WebkitMaxLogicalHeight("-webkit-max-logical-height"),
    WebkitMaxLogicalWidth("-webkit-max-logical-width"),
    WebkitMinLogicalHeight("-webkit-min-logical-height"),
    WebkitMinLogicalWidth("-webkit-min-logical-width"),
    WebkitNbspMode("-webkit-nbsp-mode"),
    WebkitOverflowScrolling("-webkit-overflow-scrolling"),
    WebkitPaddingAfter("-webkit-padding-after"),
    WebkitPaddingBefore("-webkit-padding-before"),
    WebkitPaddingEnd("-webkit-padding-end"),
    WebkitPaddingStart("-webkit-padding-start"),
    WebkitPerspectiveOriginX("-webkit-perspective-origin-x"),
    WebkitPerspectiveOriginY("-webkit-perspective-origin-y"),
    WebkitPrintColorAdjust("-webkit-print-color-adjust"),
    WebkitRegionBreakAfter("-webkit-region-break-after"),
    WebkitRegionBreakBefore("-webkit-region-break-before"),
    WebkitRegionBreakInside("-webkit-region-break-inside"),
    WebkitRtlOrdering("-webkit-rtl-ordering"),
    WebkitShapeInsided("-webkit-shape-insided"),
    WebkitShapeOutside("-webkit-shape-outside"),
    WebkitSvgShadow("-webkit-svg-shadow"),
    WebkitTapHighlightColor("-webkit-tap-highlight-color"),
    WebkitTextDecorationsInEffect("-webkit-text-decorations-in-effect"),
    WebkitTextEmphasisPosition("-webkit-text-emphasis-position"),
    WebkitTextFillColor("-webkit-text-fill-color"),
    WebkitTextSecurity("-webkit-text-security"),
    WebkitTextSizeAdjust("-webkit-text-size-adjust"),
    WebkitTextStroke("-webkit-text-stroke"),
    WebkitTextStrokeColor("-webkit-text-stroke-color"),
    WebkitTextStrokeWidth("-webkit-text-stroke-width"),
    WebkitTouchCallout("-webkit-touch-callout"),
    backgroundOriginX("-origin-x"),
    backgroundOriginY("-origin-y");

    fun value(expr: Any): IDeclaration {
        return Declaration(property, expr)
    }

    fun value(expr: Boolean): IDeclaration {
        return Declaration(property, expr.toString())
    }

    fun value(expr: Int): IDeclaration {
        return Declaration(property, expr.toString())
    }

    fun value(expr: Double): IDeclaration {
        return Declaration(property, expr.toString())
    }

    /** Set value, shorthand for value().  */
    fun s(expr: Any): IDeclaration {
        return value(expr)
    }

    /** Set value, shorthand for value().  */
    fun s(expr: Boolean): IDeclaration {
        return value(expr)
    }

    /** Set value, shorthand for value().  */
    fun s(expr: Int): IDeclaration {
        return value(expr)
    }

    /** Set value, shorthand for value().  */
    fun s(expr: Double): IDeclaration {
        return value(expr)
    }

    override fun toString(): String {
        return property
    }

}
