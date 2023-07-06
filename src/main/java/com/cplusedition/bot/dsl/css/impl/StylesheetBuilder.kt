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
package com.cplusedition.bot.dsl.css.impl

import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.dsl.css.api.IDeclaration
import com.cplusedition.bot.dsl.css.api.IDeclarations
import com.cplusedition.bot.dsl.css.api.IFontface
import com.cplusedition.bot.dsl.css.api.IImport
import com.cplusedition.bot.dsl.css.api.IMedia
import com.cplusedition.bot.dsl.css.api.IMedium
import com.cplusedition.bot.dsl.css.api.IPage
import com.cplusedition.bot.dsl.css.api.IRuleset
import com.cplusedition.bot.dsl.css.api.IRulesets
import com.cplusedition.bot.dsl.css.api.ISelector
import com.cplusedition.bot.dsl.css.api.IStylesheet
import com.cplusedition.bot.dsl.css.api.IStylesheetBuilder
import com.cplusedition.bot.dsl.css.api.PD
import com.cplusedition.bot.dsl.css.api.PK
import com.cplusedition.bot.dsl.css.api.PX
import com.cplusedition.bot.dsl.css.api.support.IRulesetChild
import com.cplusedition.bot.dsl.css.api.support.IStylesheetChild

open class StylesheetBuilder : IStylesheetBuilder {

    inner class Shortcut {
        fun borderless(): IDeclarations {
            return declarations(PK.Border.s("none"), PK.Margin.s(0), PK.Padding.s(0))
        }

        fun alignCenter(): IRuleset {
            return ruleset(
                    PD.TextAlign.Center,
                    PD.VerticalAlign.Middle)
        }

        fun alignLeftTop(): IRuleset {
            return ruleset(
                    PD.TextAlign.Left,
                    PD.VerticalAlign.Top)
        }

        fun alignLeftMiddle(): IRuleset {
            return ruleset(
                    PD.TextAlign.Left,
                    PD.VerticalAlign.Middle)
        }

        fun alignRightMiddle(): IRuleset {
            return ruleset(
                    PD.TextAlign.Left,
                    PD.VerticalAlign.Middle)
        }

        fun borderRadiusTop(expr: Any): IRuleset {
            return ruleset(
                    PK.BorderTopRightRadius.s(expr),
                    PK.BorderTopLeftRadius.s(expr),
                    PK.WebkitBorderTopRightRadius.s(expr),
                    PK.WebkitBorderTopLeftRadius.s(expr))
        }

        fun borderRadiusBottom(expr: Any): IRuleset {
            return ruleset(
                    PK.BorderBottomRightRadius.s(expr),
                    PK.BorderBottomLeftRadius.s(expr),
                    PK.WebkitBorderBottomRightRadius.s(expr),
                    PK.WebkitBorderBottomLeftRadius.s(expr))
        }

        fun borderRadius(expr: Any): IRuleset {
            return ruleset(
                    PK.BorderRadius.s(expr),
                    PK.WebkitBorderRadius.s(expr),
                    PX.MozOutlineRadius.s(expr))
        }
    }

    ////////////////////////////////////////////////////////////////////////

    var f = Shortcut()
    fun fmt(format: String, vararg args: Any): String {
        return TextUt.format(format, *args)
    }

    ////////////////////////////////////////////////////////////////////////

    override fun stylesheet(vararg nodes: IStylesheetChild): IStylesheet {
        val ret: IStylesheet = Stylesheet()
        for (n in nodes) {
            ret.add(n)
        }
        return ret
    }

    override fun imports(uri: String, vararg mediums: IMedium): IImport {
        return Import(uri, *mediums)
    }

    override fun media(vararg mediums: IMedium): IMedia {
        return Media(*mediums)
    }

    override fun media(medium: IMedium, vararg rulesets: IRuleset): IMedia {
        val ret: IMedia = Media(medium)
        ret.add(*rulesets)
        return ret
    }

    override fun media(vararg rulesets: IRuleset): IMedia {
        val ret: IMedia = Media()
        ret.add(*rulesets)
        return ret
    }

    fun medium(m: String): IMedium {
        return Medium(m)
    }

    override fun fontface(vararg decls: IDeclaration): IFontface {
        return Fontface(*decls)
    }

    override fun page(name: String?, vararg decls: IDeclaration): IPage {
        return Page(name, *decls)
    }

    override fun rulesets(vararg children: IStylesheetChild): IRulesets {
        return Rulesets(*children)
    }

    override fun declarations(vararg children: IDeclaration): IDeclarations {
        return Declarations(*children)
    }

    override fun ruleset(vararg children: IRulesetChild): IRuleset {
        return Ruleset(*children)
    }

    override fun ruleset(sel: Any, vararg children: IRulesetChild): IRuleset {
        return Ruleset(sel, *children)
    }

    override fun rule(property: Any, value: Any): IDeclaration {
        return Declaration(property, value)
    }

    override fun self(format: String, vararg args: Any): ISelector {
        return Selector(TextUt.format(format, *args))
    }

    override fun sel(): ISelector {
        return Selector()
    }

    override fun sel(sel: Any): ISelector {
        return Selector(sel)
    }

    override fun sel(vararg sels: Any): ISelector {
        return Selector(*sels)
    }

    override fun id(id: Any): ISelector? {
        return Selector().id(id)
    }

    override fun css(css: Any): ISelector? {
        return Selector().css(css)
    }

    ////////////////////////////////////////////////////////////////////////

    companion object {
        const val ABSOLUTE = "absolute"
        const val AUTO = "auto"
        const val BASELINE = "baseline"
        const val BLACK = "black"
        const val BLOCK = "block"
        const val BLUE = "blue"
        const val BOLD = "bold"
        const val BORDER_BOX = "border-box"
        const val BOTTOM = "bottom"
        const val CENTER = "center"
        const val CLIP = "clip"
        const val COLUMN = "column"
        const val COLUMN_REVERSE = "column-reverse"
        const val CONTENT_BOX = "content-box"
        const val ELLIPSIS = "ellipsis"
        const val FIXED = "fixed"
        const val FLEX = "flex"
        const val FLEX_END = "flex-end"
        const val FLEX_START = "flex-start"
        const val GREEN = "green"
        const val GRID = "grid"
        const val HIDDEN = "hidden"
        const val IMPORTANT = "!important"
        const val INHERIT = "inherit"
        const val INITIAL = "initial"
        const val INLINE = "inline"
        const val INLINE_BLOCK = "inline-block"
        const val INLINE_FLEX = "inline-flex"
        const val ITALIC = "italic"
        const val JUSTIFY = "justify"
        const val LEFT = "left"
        const val MIDDLE = "middle"
        const val NONE = "none"
        const val NORMAL = "normal"
        const val NOWRAP = "nowrap"
        const val NOT_ALLOWED = "not-allowed"
        const val POINTER = "pointer"
        const val PRE = "pre"
        const val PRE_LINE = "pre-line"
        const val PRE_WRAP = "pre-wrap"
        const val RED = "red"
        const val RELATIVE = "relative"
        const val ROW = "row"
        const val ROW_REVERSE = "row-reverse"
        const val RIGHT = "right"
        const val STATIC = "static"
        const val STRETCH = "stretch"
        const val TOP = "top"
        const val TRANSPARENT = "transparent"
        const val UNSET = "unset"
        const val VISIBLE = "visible"
        const val WHITE = "white"
        const val WRAP = "wrap"
    }
}
