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
package sf.andrians.cplusedition.support.css

import com.cplusedition.bot.core.TextUt
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.handler.IResUtil
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

object CSSGenerator {
    private const val FIXED_FONTS = "monospace"
    private const val SANS_FONTS = "sans-serif"
    private const val SIDEBAR_TOP = 5
    private const val SIDEPANEL_BORDER = 5
    private const val LICENSE = "\n"
    private const val PROPRIETARY = "\n"
    private const val CC_BY_NC = "\n"

    /// Red color component. Value ranges from [0..255]
    /// Green color component. Value ranges from [0..255]
    /// Blue color component. Value ranges from [0..255]
    class ColorValue(private val _r: Int = 0, private val _g: Int = 0, private val _b: Int = 0, private val _a: Double = 1.0) {

        companion object {
            private val X_RGBA = Regex("rgba?\\(\\s*(.*?)\\s*\\)")
            private val X_COMMA = Regex("\\s*,\\s*")

            fun fromRGB_(r: Int, g: Int, b: Int): ColorValue {
                return ColorValue(r, g, b)
            }

            fun fromRGBA_(r: Int, g: Int, b: Int, a: Double): ColorValue {
                return ColorValue(r, g, b, a)
            }

            fun copy(other: ColorValue): ColorValue {
                return ColorValue(other._r, other._g, other._b, other._a);
            }

            /**
             * @return null instead of throwing exception on invalid input.
             * @param a If not specified, default is 1.0.
             */
            fun parseSafe_(value: String?, a: Double = 1.0): ColorValue? {
                if (value == null || value.isEmpty()) return null
                try {
                    val ret = ColorValue.from_(value, a)
                    if (ret != null) return ret
                } catch (e: Throwable) {
                }
                return null;
            }

            /**
             * Parses the color value with the following format:
             *    "#fff"
             *    "#abcdef"
             *    "#fff, 1.0"
             *    "#abcdef, 1.0"
             *    "fff",
             *    "abcdef",
             *    "255, 255, 255"
             *    "255, 255, 255, 1.0"
             *    "rgb(255, 255, 255)"
             *    "rgba(255, 255, 255, 1.0)"
             */
            private fun from_(value: String?, defa: Double = 1.0): ColorValue? {
                if (value == null || value.isEmpty()) return null;
                if (value.startsWith('#')) return parseHex_(value.substring(1), defa)
                if (!value.contains(",")) return parseHex_(value, defa)
                val m = ColorValue.X_RGBA.matchEntire(value) ?: return parseDec_(value, defa)
                return parseDec_(m.groupValues[1], defa)
            }

            private fun parseDec_(value: String, defa: Double): ColorValue? {
                val tokens = value.split(ColorValue.X_COMMA)
                val len = tokens.size
                if (len != 3 && len != 4) return null;
                val r = TextUt.parseInt(tokens[0], -1)
                val g = TextUt.parseInt(tokens[1], -1)
                val b = TextUt.parseInt(tokens[2], -1)
                val a = if (len == 4) TextUt.parseDouble(tokens[3], -1.0) else defa
                if (r < 0 || r > 255
                    || g < 0 || g > 255
                    || b < 0 || b > 255
                    || a < 0.0 || a > 1.0
                ) {
                    return null
                }
                return ColorValue(r, g, b, a)
            }

            /**
             * Parses the color value in the format FFFFFFFF, FFFFFF or FFF
             * ignorecase and optionally followed by , alpha in the later two cases.
             */
            private fun parseHex_(value: String, defa: Double): ColorValue? {
                var hex = value
                var a = defa
                if (hex.length != 3 && hex.length != 6 && hex.length != 8) return null
                if (hex.length == 8) {
                    hex = hex.substring(0, 6)
                    a = TextUt.parseHex("0x${hex.substring(6, 8)}", -1) / 255.0
                }
                if (a < 0 || a > 1.0) return null
                if (hex.length == 3) {
                    val r = hex.substring(0, 1)
                    val g = hex.substring(1, 2)
                    val b = hex.substring(2, 3)
                    hex = "${r}${r}${g}${g}${b}${b}"
                }
                val hexR = hex.substring(0, 2)
                val hexG = hex.substring(2, 4)
                val hexB = hex.substring(4, 6)
                val r = TextUt.parseHex(hexR, -1)
                val g = TextUt.parseHex(hexG, -1)
                val b = TextUt.parseHex(hexB, -1)
                if (r < 0 || g < 0 || b < 0) return null
                return ColorValue(r, g, b, a)
            }
        }

        val r get() = _r
        val g get() = _g
        val b get() = _b
        val a get() = _a
        val isOpaque get() = _a == 1.0

        fun equals_(other: ColorValue?): Boolean {
            return other != null && _r == other._r && _g == other._g && _b == other._b && _a == other._a
        }

        fun rgbEquals_(other: ColorValue?): Boolean {
            return other != null && _r == other._r && _g == other._g && _b == other._b
        }

        fun toRgbArray(): Array<Int> {
            return arrayOf(_r, _g, _b)
        }

        fun toRgbString(): String {
            return "${_r}, ${_g}, ${_b}"
        }

        fun toRgbaString(): String {
            return "${_r}, ${_g}, ${_b}, ${alpha2()}"
        }

        fun toHex3String(): String {
            return "${hex(_r)}${hex(_g)}${hex(_b)}"
        }

        fun toRgbOrRgbaString(): String {
            return if (isOpaque) toRgbString() else toRgbaString()
        }

        fun toCSSString(): String {
            return if (isOpaque) "rgb(${toRgbString()})" else "rgba(${toRgbaString()})"
        }

        fun toHexOrRgbaString(): String {
            return if (isOpaque) "#${toHex3String()}" else toRgbaString()
        }

        fun toHexOrCSSString(): String {
            return if (isOpaque) "#${toHex3String()}" else "rgba(${toRgbaString()})"
        }

        override fun toString(): String {
            return "rgba(${toRgbaString()})"
        }

        private fun alpha2(): String {
            return String.format("%.2f", (_a * 100).toInt() / 100.0)
        }

        private fun hex(v: Int): String {
            return v.toString(16).padStart(2, '0');
        }
    }

    interface IConf {
        /**
         * The scaled dpi, which is ~160 for Android.
         */
        fun dpi(): Int

        /**
         * The window width in scaled px.
         */
        fun winWidth(): Int

        /**
         * The window height in scaled px.
         */
        fun winHeight(): Int

        /**
         * Button size in scaled px.
         */
        fun buttonSize(): Int

        fun fontSize(): Double

        fun fontFamily(): String
        fun fixedFontFamily(): String

        fun dialogBGColor(): String
        fun headingColor(): String
        fun linkColor(): String
        fun annotationColor(): String
        fun highlightColor(): String
        fun isMobile(): Boolean
    }

    open class BuilderBase protected constructor(protected val conf: IConf) {

        protected val buttonSize: Int
        protected val buttonSizePx: String
        protected val fontSizePx: String
        protected val dialogBGOpacity: Double

        init {
            buttonSize = conf.buttonSize()
            buttonSizePx = conf.buttonSize().toString() + "px"
            fontSizePx = conf.fontSize().toString() + "px"
            dialogBGOpacity = ColorValue.parseSafe_(conf.dialogBGColor())?.a ?: 1.0
        }

        protected fun winwidth(faction: Double): Int {
            return (conf.winWidth() * faction).toInt()
        }

        protected fun winheight(faction: Double): Int {
            return (conf.winHeight() * faction).toInt()
        }

        protected fun buttonsize(): String {
            return buttonSize.toString()
        }

        protected fun buttonsizepx(): String {
            return buttonSizePx
        }

        protected fun buttonsizepx(times: Int): String {
            return (buttonSize * times).toString() + "px"
        }

        protected fun buttonsizepx(times: Int, delta: Int): String {
            return (buttonSize * times + delta).toString() + "px"
        }

        protected fun buttonsizepx(times: Double, delta: Int): String {
            return ((buttonSize * times).roundToInt() + delta).toString() + "px"
        }

        protected fun buttonsizem6px(): String {
            return buttonsizepx(1, -6)
        }

        protected fun buttonsizem5px(): String {
            return buttonsizepx(1, -5)
        }

        protected fun buttonsizemborderpx(): String {
            return (buttonSize - An.DEF.themeBorderWidth).toString() + "px"
        }

        protected fun buttonsizep1px(): String {
            return buttonsizepx(1, 1)
        }

        protected fun buttonsize25px(): String {
            return buttonsizepx(0.25, 0)
        }

        protected fun buttonsize50px(): String {
            return buttonsizepx(0.5, 0)
        }

        protected fun buttonsize60px(): String {
            return buttonsizepx(0.6, 0)
        }

        protected fun buttonsize80px(): String {
            return buttonsizepx(0.8, 0)
        }

        protected fun buttonsize75px(): String {
            return buttonsizepx(0.75, 0)
        }

        protected fun buttonsize150px(): String {
            return buttonsizepx(1.5, 0)
        }

        protected fun buttonsize200px(): String {
            return buttonsizepx(2)
        }

        protected fun buttonsize300px(): String {
            return buttonsizepx(3)
        }

        protected fun buttonsize400px(): String {
            return buttonsizepx(4)
        }

        protected fun buttonsize500px(): String {
            return buttonsizepx(5)
        }

        protected fun buttonsize600px(): String {
            return buttonsizepx(6)
        }

        protected fun buttonsize800px(): String {
            return buttonsizepx(8)
        }

        protected fun stylerowheightpx(): String {
            return Math.round(buttonSize * 4 / 5.toFloat()).toString() + "px"
        }

        protected fun dialogbgcolor(): String {
            return conf.dialogBGColor()
        }

        protected fun headingcolor(): String {
            return conf.headingColor()
        }

        protected fun linkcolor(): String {
            return conf.linkColor()
        }

        protected fun annotationcolor(): String {
            return conf.annotationColor()
        }

        protected fun highlightcolor(): String {
            return conf.highlightColor()
        }

        protected fun precolor(): String {
            return "#000080"
        }

        protected fun codecolor(): String {
            return "#7f0055"
        }

        protected fun errorcolor(): String {
            return "#e00"
        }

        protected fun warncolor(): String {
            return "#f50"
        }

        protected fun templatebuttoncolor(): String {
            return "rgba(74, 164, 255, 0.75)"
        }

        protected fun defaultfont(): String {
            return "\"" + conf.fontFamily() + "\", " + SANS_FONTS
        }

        protected fun defaultfontsize(): String {
            return conf.fontSize().toString()
        }

        protected fun defaultfontsizepx(): String {
            return fontSizePx
        }

        protected fun fixedfont(): String {
            return "\"" + conf.fixedFontFamily() + "\", " + FIXED_FONTS
        }

        protected fun symbolfontsizepx(): String {
            return (buttonSize * An.DEF.symbolFontSizeRatio).roundToInt().toString() + "px"
        }

        protected fun toolbarfontsizepx(): String {
            return (buttonSize * An.DEF.symbolFontSizeRatio).roundToInt().toString() + "px"
        }

        protected fun boxshadow(): String {
            return An.DEF.themeBoxShadow
        }

        protected fun dragborder(): String {
            return (buttonSize / 4).toString() + "px solid rgba(0, 0, 0, 0.0)"
        }

        protected fun promptminwidthpx(): String {
            return min(conf.dpi() * 3, winwidth(0.75)).toString() + "px"
        }

        protected fun promptmaxwidthpx(): String {
            return min(conf.dpi() * 4, winwidth(0.75)).toString() + "px"
        }

        protected fun sidebartoppx(): String {
            return SIDEBAR_TOP.toString() + "px"
        }

        protected fun sidebarheightpx(): String {
            return Math.round(buttonSize * 2 / 3.toFloat()).toString() + "px"
        }

        protected fun sidepanelborderpx(): String {
            return SIDEPANEL_BORDER.toString() + "px"
        }

        protected fun anbutton(): String {
            return if (conf.isMobile()) ""
            else TextUt.format("button.%s:active { %s: #fff; }%n", An.CSS.AnButton, "background-color")
        }

        protected fun anspin(): String {
            return ("@-webkit-keyframes "
                    + An.CSS.AnSpin
                    + " { 0% { -webkit-transform: rotate(0deg); transform: rotate(0deg); }\n"
                    + "100% { -webkit-transform: rotate(359deg); transform: rotate(359deg); }\n}\n"
                    + "@keyframes "
                    + An.CSS.AnSpin
                    + " { 0% { -webkit-transform: rotate(0deg); transform: rotate(0deg); }\n"
                    + "100% { -webkit-transform: rotate(359deg); transform: rotate(359deg); }\n}\n")
        }

        protected fun popupbg(color: String): String {
            return ColorValue.parseSafe_(color)?.let {
                ColorValue.fromRGBA_(it.r, it.g, it.b, dialogBGOpacity).toCSSString()
            } ?: color
        }

        protected fun popupbackdrop(filter: String): String {
            return if (dialogBGOpacity >= 0.99) "" else filter
        }

        protected fun hideInDesktop(): String {
            return if (conf.isMobile()) "" else "none !important"
        }

        protected fun hiddenInDesktop(): String {
            return if (conf.isMobile()) "visible" else "hidden !important"
        }
    }

    class StylesJSON(rsrc: IResUtil) {
        class Style(var group: String, var name: String, var label: String) {
            internal object Key {
                var group = "group"
                var name = "name"
                var label = "label"
                var style = "style"
                var tag = "tag"
            }

            var tag = "DIV"
            var styles: MutableMap<String, String> = TreeMap()
            fun tag(tag: String): Style {
                this.tag = tag
                return this
            }

            fun style(name: String, value: String): Style {
                styles[name] = value
                return this
            }

            fun style(namevalues: Array<Array<String>>): Style {
                for (nv in namevalues) {
                    styles[nv[0]] = nv[1]
                }
                return this
            }

            fun toJSON(ret: JSONObject): JSONObject {
                return try {
                    ret.put(Key.group, group)
                    ret.put(Key.name, name)
                    ret.put(Key.label, label)
                    ret.put(Key.tag, tag)
                    val a = JSONObject()
                    for ((key, value) in styles) {
                        a.put(key, value)
                    }
                    ret.put(Key.style, a)
                    ret
                } catch (e: Exception) {
                    throw AssertionError()
                }
            }
        }

        var highlightStyles: Array<Style> = createHighlightStyles(rsrc)

        private fun createHighlightStyles(rsrc: IResUtil): Array<Style> {
            return arrayOf(
                Style("bg", "x-highlight-gray", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("bg", "x-highlight-blue", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("bg", "x-highlight-green", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("bg", "x-highlight-yellow", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("bg", "x-highlight-orange", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("bg", "x-highlight-red", rsrc.get(R.string.Tooltips_TextHighlight)),
                Style("shadow", "x-shadow-gray", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-blue", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-green", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-yellow", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-orange", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-red", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se-black", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se-gray", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se-white", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se2-black", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se2-gray", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("shadow", "x-shadow-se2-white", rsrc.get(R.string.Tooltips_TextShadow)),
                Style("fg", "x-color-gray", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-blue", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-green", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-yellow", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-orange", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-red", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-plum", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-darkblue", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-dodgerblue", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-teal", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-white", rsrc.get(R.string.Tooltips_TextColor)),
                Style("fg", "x-color-black", rsrc.get(R.string.Tooltips_TextColor))
            )
        }

        fun build(): JSONObject {
            val ret = JSONObject()
            val paras = JSONArray()
            val highlights = JSONArray()
            val chars = JSONArray()
            val builtinChars = JSONArray()
            val builtinParas = JSONArray()
            try {
                ret.put(An.SettingsKey.charStyles, chars)
                ret.put(An.SettingsKey.highlightStyles, highlights)
                ret.put(An.SettingsKey.paraStyles, paras)
                ret.put(An.SettingsKey.builtinCharStyles, builtinChars)
                ret.put(An.SettingsKey.builtinParaStyles, builtinParas)
                ret.put(An.SettingsKey.bgImgSamples, JSONArray(bgImgSamples))
            } catch (e: Exception) {
                throw AssertionError()
            }
            for (style in charStyles) {
                chars.put(style.toJSON(JSONObject()))
            }
            for (style in highlightStyles) {
                highlights.put(style.toJSON(JSONObject()))
            }
            for (style in paraStyles) {
                paras.put(style.toJSON(JSONObject()))
            }
            for (style in builtinCharStyles) {
                builtinChars.put(style.toJSON(JSONObject()))
            }
            for (style in builtinParaStyles) {
                builtinParas.put(style.toJSON(JSONObject()))
            }
            return ret
        }

        companion object {
            private val builtinCharStyles = arrayOf(
                Style("", "x-bold", "Bold"),
                Style("", "x-italic", "Italic"),
                Style("", "x-underline", "Underline"),
                Style("", "x-strikethrough", "Strike through"),
                Style("xscript", "x-subscript", "Subscript"),
                Style("xscript", "x-superscript", "Superscript"),
                Style("float", "x-float-left", "Float left"),
                Style("float", "x-float-right", "Float right"),
                Style("", "x-nowrap", "Nowrap"),
            )
            private val builtinParaStyles = arrayOf(
                Style("align", "x-align-left", "Align left"),
                Style("align", "x-align-center", "Align center"),
                Style("align", "x-align-right", "Align right"),
                Style("align", "x-align-justify", "Align justify")
            )

            private val paraStyles = arrayOf(
                Style("body", "x-head1", "Head1"),
                Style("body", "x-head2", "Head2"),
                Style("body", "x-head3", "Head3"),
                Style("body", "x-header", "Header"),
                Style("body", "x-footer", "Footer"),
                Style("body", "x-body", "Body"),
                Style("body", "x-small", "Small"),
                Style("body", "x-large", "Large"),
                Style("body", "x-para", "Paragraph"),
                Style("body", "x-code", "Code"),
                Style("body", "x-pre", "Pre").tag("pre"),
                Style("body", "x-box-gray", "Gray box"),
                Style("body", "x-box", "Box shadow"),
                Style("", "x-none", "None"),
                Style("display", "x-hidden", "Hidden")
            )

            private val charStyles = arrayOf(
                Style("body", "x-head1", "Head1"),
                Style("body", "x-head2", "Head2"),
                Style("body", "x-head3", "Head3"),
                Style("body", "x-body", "Body"),
                Style("body", "x-small", "Small"),
                Style("body", "x-large", "Large"),
                Style("body", "x-code", "Code"),
                Style("body", "x-pre", "Pre"),
                Style("warn", "x-warn", "Warn"),
                Style("warn", "x-error", "Error"),
                Style("", "x-none", "None"),
                Style("display", "x-hidden", "Hidden")
            )
            private val b = 224
            val bgImgSamples = arrayOf(
                "radial-gradient(rgba(255, ${b + 16}, 0, 1.0),rgba(255, $b, 0, 1.0) 75%,rgba(255, ${b - 16}, 0, 1.0) 90%)",
                "radial-gradient(rgba(255, ${b - 8}, 34, 1.0),rgba(255, ${b - 24}, 34, 1.0) 75%, rgba(255, ${b - 40}, 34, 1.0) 90%)",
                "url(/assets/images/wallpapers/sand01.png)" +
                        ", radial-gradient(rgba(255, ${b + 12}, 0, 1.0), rgba(255, ${b + 4}, 0, 1.0) 80%, rgba(255, ${b - 4}, 0, 1.0) 90%)",
                "url(/assets/images/wallpapers/sand01.png)" +
                        ", radial-gradient(rgba(255, ${b - 8}, 0, 1.0), rgba(255, ${b - 16}, 0, 1.0) 80%, rgba(255, ${b - 24}, 0, 1.0) 90%)",
                "radial-gradient(circle, rgba(255, 255, 0, 0.50), rgba(255, 170, 68, 0.50)), " +
                        "url(/assets/images/wallpapers/cloth02.png)",
                "linear-gradient(rgba(204, 204, 204, 0.50), rgba(153, 153, 153, 0.50)), " +
                        "url(/assets/images/wallpapers/marble01.png)",
                "radial-gradient(circle, rgba(136, 204, 119, 0.5), rgba(136, 221, 238, 0.5)), " +
                        "url(/assets/images/wallpapers/glass02.png)",
                "url(/assets/images/wallpapers/cloud01.png), " +
                        "linear-gradient(rgba(238, 238, 0, 0.5), rgba(136, 221, 170, 0.5))",
                "url(/assets/images/wallpapers/cloth03.png), " +
                        "linear-gradient(rgba(106, 168, 79, 0.9), rgba(106, 168, 79, 0.9))",
                "radial-gradient(12rem, rgba(255, 255, 0, 0.50) 5%, rgba(30, 144, 255, 0.75) 25%, rgba(136, 221, 238, 0.75) 75%, rgba(17, 85, 204, 0.75)), " +
                        "url(/assets/images/samples/earth02.jpg)",
                "url(/assets/images/wallpapers/cloth02.png), " +
                        "linear-gradient(rgba(255, 160, 0, 0.9), rgba(255, 51, 0, 0.9))",
                "radial-gradient(rgba(255, 68, 34, 0.65), rgba(255, 68, 34, 0.65), rgba(255, 0, 0, 0.75) 85%)",
                "radial-gradient(farthest-side at left bottom, rgba(100, 200, 75, 0.75), rgba(136, 204, 120, 0.25) 99%, rgba(255, 255, 255, 0)), " +
                        "radial-gradient(farthest-side at right bottom, rgba(64, 165, 42, 0.75), rgba(200, 204, 128, 0.5) 99%, rgba(238, 238, 136, 0.5))",
                "radial-gradient(circle at left, rgba(255, 204, 51), rgb(255, 229, 153) 50%, rgba(255, 255, 255, 0) 50%), " +
                        "radial-gradient(circle at right, rgba(96, 196, 76, 0.75), rgba(136, 204, 120, 0.25) 99%)",
                "url(/assets/images/wallpapers/cloud01.png), " +
                        "linear-gradient(#777, #777)",
                "linear-gradient(rgba(255, 255, 255, 0.65), rgba(255, 255, 255, 0.65)), " +
                        "url(/assets/images/samples-blur/red.jpg)",
                "url(/assets/images/samples-blur/red.jpg)",
                "url(/assets/images/wallpapers/cloth02.png)",
                "linear-gradient(rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.75)), " +
                        "url(/assets/images/wallpapers/cloth01.png), " +
                        "linear-gradient(rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.75))",
                "",
                "none"
            )
        }
    }

	class Clientv1CSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return (CC_BY_NC + SharedCSS(conf).build()
            + "@media  (max-width : 639px) { .x-hide-in-narrow-screen { display: none !important; } } @media  (min-width : 640px) { .x-hide-in-wide-screen { display: none !important; } } @media  (max-width : 639px), (max-height : 639px) { .x-hide-in-small-screen { display: none !important; } } @media  (min-width : 640px) and (min-height : 640px) { .x-hide-in-large-screen { display: none !important; } } @media  (min-width : 640px) { .x-narrow { width: 512px; margin-left: auto; margin-right: auto; } } .x-disabled-in-desktop { visibility: "
            + hiddenInDesktop()
            + "; } .x-hide-in-desktop { display: "
            + hideInDesktop()
            + "; } body { width: 100vw; height: 100vh; background-color: #fff; overflow: hidden !important; -webkit-user-modify: read-only !important; -webkit-user-select: text !important; -webkit-touch-callout: none !important; -webkit-tap-highlight-color: rgba(164,164,164,0.75) !important; -webkit-overflow-scrolling: auto; overscroll-behavior: none; -webkit-text-size-adjust: none !important; } body[contenteditable] { -webkit-user-modify: read-write !important; } .x-anchor { cursor: pointer; color: "
            + linkcolor()
            + "; } .x-viewport { z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; } .x-flex { display: flex; align-items: flex-start; box-sizing: border-box; } .x-bullet, .x-bullet-fa { display: inline-block; box-sizing: border-box; overflow: visible; text-align: center; flex: 0 0 auto; align-self: baseline; justify-content: center; margin: 0 0.5em 0 0; padding: 0; font-family: FontAwesome; width: 1.5em; } .x-bullet-datetime, .x-bullet-text { display: inline-block; flex: 0 0 auto; align-self: baseline; margin: 0 5px 0 0; word-wrap: nowrap; word-break: normal; font-family: "
            + fixedfont()
            + "; color: "
            + headingcolor()
            + "; } .x-list-content { display: inline-block; overflow: auto; } div.x-smokescreen { display: none; position: absolute; box-sizing: border-box; left: 0; top: 0; height: 100vh; width: 100vw; pointer-events: auto; transform: translateZ(0); background-color: rgba(255,255,255,0); } div.x-annotation > div.x-smokescreen { display: none; position: absolute; z-index: 1299; } #x-leftsidepanel, #x-rightsidepanel { display: none; z-index: 1099; } div.x-leftsidepanel, div.x-rightsidepanel { z-index: 1100; position: absolute; box-sizing: border-box; top: 0; height: 100vh; width: 50vw; padding: 10px 10px 75vh 10px; -webkit-overflow-scrolling: touch; overflow: auto; background-color: rgba(255,255,255,1.0); } div.x-leftsidepanel { left: 0; border-right: 1px solid rgba(0, 0, 0, 0.75); } div.x-rightsidepanel { right: 0; border-left: 1px solid rgba(0, 0, 0, 0.75); } #x-rightsidebar { z-index: 1800; position: absolute; display: block; width: 100vh; box-sizing: border-box; left: 0; top: 0; margin: 0; padding: 0; overflow: hidden !important; transform-origin: 100% 100%; transform: translateY(-100%) translateX(calc(100vw - 100vh)) rotate(-90deg); background-color: rgba(255,255,255,0.0); } div.x-rsb-content { display: flex; flex-direction: row-reverse; align-items: flex-start; justify-content: flex-start; width: 100vh; box-sizing: border-box; float: right; -webkit-overflow-scrolling: touch; overflow: auto; } div.x-rsb-content > div.x-rsb-padding { flex: 1 1 auto !important; align-self: stretch; min-width: 75vh; } div.x-rsb-content > .x-rsb-tab { -webkit-user-select: none; user-select: none; flex: 0 0 auto; align-self: stretch; text-align: center; color: #444; font-weight: bold; padding: 0px 10px; margin-left: 1px; border-radius: 10px 0 0 0; box-sizing: border-box; line-height: "
            + buttonsize75px()
            + "; min-height: "
            + stylerowheightpx()
            + "; min-width: "
            + buttonsize150px()
            + "; } div.x-rsb-content a { cursor: pointer; color: #444; } #x-leftsplitpanel, #x-rightsplitpanel { position: absolute; left: 0; top: 0; height: 100%; padding: 10px 10px 75vh 10px; margin: 0; -webkit-overflow-scrolling: touch; overflow: auto; box-sizing: border-box; } .x-frsb { background-color: #fff !important; } div.x-annotation { display: inline; } .x-a-target, .x-a-link { cursor: pointer; color: "
            + annotationcolor()
            + "; } .x-a-content { all: initial; display: block; -webkit-user-modify: inherit; -webkit-user-select: inherit; -webkit-touch-callout: inherit; -webkit-tap-highlight-color: inherit; -webkit-text-size-adjust: inherit; line-height: 1.4; font-size: "
            + defaultfontsizepx()
            + "; font-family: "
            + defaultfont()
            + "; position: absolute; z-index: 1300; box-sizing: border-box; padding: 10px; max-width: 75vw; max-height: 50vh; -webkit-overflow-scrolling: touch; overflow: auto; } .x-a-style-text { background-color: #9cf; } .x-a-style-image { background-color: #fff; } .x-rounded-bottom { border-radius: 0 0 5px 5px; } .x-rounded-corners { border-radius: 5px; } .x-rounded-top { border-radius: 5px 5px 0 0; } li.x-list { display: flex; align-items: baseline; margin-left: 0; list-style: none; } div.x-flexbox { display: flex; align-items: flex-start; box-sizing: border-box; } div.x-pictureframe { display: inline-flex; flex-flow: column; align-items: flex-start; justify-content: flex-start; overflow: hidden; } div.x-slideshow { display: flex; flex-flow: column; align-items: center; justify-content: flex-start; } div.x-slideshow-content { position: relative; top: 0; left: 0; } div.x-slideshow-content img { display: none; position: absolute; left: 0; top: 0; z-index: 10; } div.x-slideshow-play { position: absolute; background-color: #fff; color: #000; left: 0; top: 0; z-index: 5; box-sizing: border-box; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-family: FontAwesome; font-size: "
            + buttonsizepx()
            + "; } div.x-slideshow-play:before { content: \"\\f01d\"; } div.x-t-button { display: none; min-width: 4rem; text-align: center; background-color: "
            + templatebuttoncolor()
            + "; } #x-rightsidebar.x-hidetemp { display: none; } .x-client-hide { visibility: hidden !important; } .x-client-show { z-index: 500 !important; visibility: visible !important; } .x-show-in-edit { display: none !important; } body[contenteditable] .x-show-in-edit { display: unset !important; } img.x-canvas { -webkit-user-select: none; user-select: none; max-width: 100%; box-sizing: content-box; } div.x-vlayout { display: flex; flex-flow: column nowrap; } div.x-vcaption { margin: 5px 0 0 0; } div.x-hlayout { display: inline-flex; box-sizing: border-box; width: 100%; margin: 5px 0; } div.x-hlayout { display: inline-flex; box-sizing: border-box; width: 100%; margin: 5px 0; } div.x-hcaption { flex: 1 1 auto; align-self: stretch; margin-left: 10px; } div.x-calendar { display: inline-block; overflow: hidden; color: #000; border-radius: 5px; border: 1px solid rgba(0, 0, 0, 0.5); } div.x-cal-head { display: flex; align-items: center; justify-content: center; overflow: hidden; font-weight: bold; width: 100%; box-sizing: border-box; border-bottom: 1px dotted rgba(0, 0, 0, 0.5); background-color: #8c7; min-height: 4ex; } div.x-cal-body { display: inline-flex; flex-flow: row wrap; align-items: flex-start; background-color: #ceb; box-sizing: border-box; } div.x-cal-body > div { display: flex; flex-flow: column wrap; flex: 1 1 14.285%; align-items: center; align-content: center; justify-content: center; line-height: 1.05; max-height: 28.57%; overflow: hidden; box-sizing: border-box; padding: 1px; word-break: break-all; align-self: stretch; } div.x-date-holiday { color: #f00; } div.x-cal-body > div.x-selected { background-color: rgba(255, 255, 255, 0.80); } div.x-cal-body div.x-annotation { display: inline-flex; flex: 1 1 auto; align-self: stretch; align-items: center; justify-content: center; } .x-calc-quantity .x-calc-quantity *, .x-calc-price, .x-calc-price *, .x-calc-subtotal .x-calc-subtotal *, .x-calc-total, .x-calc-total * { -webkit-user-select: none; user-select: none; } .x-calc-error { color: red; } div.x-subject { flex: 0 0 auto; border-bottom: 1px solid rgba(0, 0, 0, 0.5); padding: 0 0 0.75ex 0; font-weight: bold; } div.x-content { flex: 1 1 auto; padding: 1ex 0 0 0; } div.x-border-none { background-color: #fff; } div.x-border-onepx { border: 1px solid rgba(0, 0, 0, 0.5); background-color: #fff; } div.x-border-thin { padding: 5px; border: 1px solid rgba(0, 0, 0, 0.5); background-color: #fff; } div.x-border-thick { padding: 10px; border: 1px solid rgba(0, 0, 0, 0.5); background-color: #fff; } .x-button-cell { width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; } .x-button-height { height: "
            + buttonsizepx()
            + "; } .x-button-width { width: "
            + buttonsizepx()
            + "; } .x-toolbar-fontsize { font-size: "
            + toolbarfontsizepx()
            + "; } .x-rowheight { min-height: "
            + stylerowheightpx()
            + "; } .x-row { line-height: 1.5; } ul.x-rows>li { line-height: 1.5; } .x-accordion-show, .x-accordion-hide, .x-accordion-content { margin: 0.5ex 0; } .x-accordion-button:before { display: inline-block; font-family: FontAwesome; font-size: inherit; text-rendering: auto; -webkit-font-smoothing: antialiased; } .x-accordion-show .x-accordion-button:before { content: \"\\f139\"; } .x-accordion-hide .x-accordion-button:before { content: \"\\f13a\"; } .x-accordion-hide > .x-accordion-content { display: none !important; } .x-image-def { width: 240px; max-width: 50vw; } .x-photo-def { max-width: 50vmin; max-height: 50vmin; } .x-document-scan { display: flex; flex-flow: row wrap; } .x-document-scan img { margin: 5px; border: 1px solid rgba(0, 0, 0, 0.5); } .fa .x-emoji-photo:before, .fa.x-emoji-photo:before { content: \"\\f030\"; } .fa .x-emoji-blank:before, .fa.x-emoji-blank:before { content: \"\\f016\"; } .fa .x-emoji-calendar:before, .fa.x-emoji-calendar:before { content: \"\\f073\"; } .fa .x-emoji-canvas:before, .fa.x-emoji-canvas:before { content: \"\\f1fc\"; } .fa .x-emoji-blog:before, .fa.x-emoji-blog:before { content: \"\\f271\"; } .fa .x-emoji-cards:before, .fa.x-emoji-cards:before { content: \"\\f114\"; } .fa .x-emoji-home:before, .fa.x-emoji-home:before { content: \"\\f015\"; } .fa .x-emoji-audio:before, .fa.x-emoji-audio:before { content: \"\\f028\"; } .fa .x-emoji-memo:before, .fa.x-emoji-memo:before { content: \"\\f24a\"; } .fa .x-emoji-notes:before, .fa.x-emoji-notes:before { content: \"\\f044\"; } .fa .x-emoji-shopping:before, .fa.x-emoji-shopping:before { content: \"\\f07a\"; } .fa .x-emoji-todo:before, .fa.x-emoji-todo:before { content: \"\\f00c\"; } .x-audioinfo, .x-videoinfo { display: flex; padding: 10px; margin: 5px 0; } .x-audioinfo a.x-audio, .x-videoinfo a.x-video.xx-error { flex: 0 0 auto; align-self: center; margin: 0 10px 0 0; overflow: visible; box-sizing: border-box; text-align: center; min-width: 3rem; font-family: FontAwesome; } .x-audioinfo a.x-audio:before { font-size: 2rem; content: \"\\f028\"; } .x-videoinfo .x-video.xx-error:before { font-size: 1.5rem; content: \"\\f03d\"; } .x-videoinfo .x-video-poster { display: inline-block; flex: 0 0 auto; align-self: center; min-width: 3rem; margin: 0 15px 0 0; overflow: visible; } .x-videoinfo .x-video-poster > div { pointer-events: none; display: grid; position: relative; } .x-videoinfo .x-video-poster img { pointer-events: none; max-width: 10rem; max-height: 10rem; } .x-videoinfo .x-video-poster .x-play { display: flex; position: absolute; pointer-events: none; top: 0; left: 0; z-index: 5; width: 100%; height: 100%; box-sizing: border-box; align-items: center; justify-content: center; } .x-videoinfo .x-video-poster .x-play:before { font-family: FontAwesome; font-size: 2rem; text-shadow: 0 0 2px #000; color: #fff; content: \"\\f01d\"; } .x-audioinfo > div, .x-videoinfo > div { overflow: auto; flex: 1 1 100%; } .x-audioinfo > div > div:first-child, .x-videoinfo > div > div:first-child { font-family: Antonio-Regular; font-weight: normal; font-style: normal; margin-bottom: 5px; width: 100%; } .x-audioinfo .x-audio-datetime, .x-videoinfo .x-video-datetime { font-family: Antonio-Regular; font-weight: normal; font-style: normal; } .x-audioinfo .x-audio-filename, .x-videoinfo .x-video-filename, .x-audioinfo .x-media-filesize, .x-videoinfo .x-media-filesize { font-family: Antonio-Light; } .x-zindex-00, .x-z-bottom { z-index: 0  !important; } .x-zindex-10 { z-index: 10  !important; } .x-zindex-20 { z-index: 20  !important; } .x-z-lower { z-index: 25  !important; } .x-zindex-30 { z-index: 30  !important; } .x-zindex-40 { z-index: 40  !important; } .x-zindex-50, .x-z-middle { z-index: 50  !important; } .x-zindex-60 { z-index: 60  !important; } .x-zindex-70 { z-index: 70  !important; } .x-z-upper { z-index: 75  !important; } .x-zindex-80 { z-index: 80  !important; } .x-zindex-90 { z-index: 90  !important; } .x-zindex-100, .x-z-top { z-index: 100  !important; } div.x-w-shopping { width: 100%; } div.x-w-shopping table.x-w-shopping { margin: 2ex 0; border-top: 2px solid rgba(0, 0, 0, 0.5); border-bottom: 2px solid rgba(0, 0, 0, 0.5); border-collapse: collapse; box-sizing: border-box; width: 100%; } div.x-w-shopping table.x-w-shopping tr.x-w-shopping-header, tr.x-w-shopping-total { background-color: rgba(0, 0, 0, 0.1); } div.x-w-shopping table.x-w-shopping tr.x-w-shopping-header th { font-weight: bold; border-bottom: var(--x-theme-border); } div.x-w-shopping table.x-w-shopping td.x-todo-status { text-align: center; overflow: visible; width: 2.5em; min-width: 2.5em; } div.x-w-shopping table.x-w-shopping tr.x-w-shopping-total td { font-weight: bold; text-align: right; } div.x-w-shopping table.x-w-shopping td, th { padding-top: 1ex; padding-bottom: 0.75ex; vertical-align: middle; } div.x-w-shopping table.x-w-shopping tr.x-w-shopping > td { border-bottom: var(--x-theme-border); } div.x-w-shopping table.x-w-shopping th.x-w-shopping-desc, td.x-w-shopping-desc { text-align: left; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-desc { padding-left: 0.5em; padding-right: 0.5em; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-quantity, td.x-calc-quantity { text-align: right; padding-right: 0.5em; word-break: break-all; min-width: 2em; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-price, td.x-calc-price { text-align: right; padding-right: 0.5em; word-break: break-all; min-width: 3em; } div.x-w-shopping table.x-w-shopping td.x-calc-total { text-align: right; padding-right: 0.5em; word-break: break-all; min-width: 5em; } div.x-w-shopping.x-hide-done table.x-w-shopping tr.x-todo-done { display: none; } div.x-w-shopping table.x-w-shopping .x-date { margin-right: 0.25em; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-quantity, td.x-calc-quantity { width: 4em; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-price, td.x-calc-price { width: 5em; } @media  (max-width : 512px) { div.x-w-shopping table.x-w-shopping th.x-w-shopping-quantity, td.x-calc-quantity { width: 2em; } div.x-w-shopping table.x-w-shopping th.x-w-shopping-price, td.x-calc-price { width: 4em; } } div.x-w-shopping table.x-w-shopping td.x-todo-status:before { font-size: 1.25em; font-family: FontAwesome; content: \"\\f0c8\"; } div.x-w-shopping table.x-w-shopping tr.x-w-shopping.x-todo-done td.x-todo-status:before { content: \"\\f00c\"; } div.x-w-shopping.x-style-dark .x-date, div.x-w-shopping.x-style-light .x-date { color: inherit !important; } div.x-w-toc ul.x-w-toc { margin: 0; padding: 0; } div.x-w-toc ul.x-w-toc > li { list-style: none; padding: 1px 0; margin: 0; } div.x-w-toc ul.x-w-toc > li.x-w-toc1 { font-weight: bold; margin: 2px 0; } div.x-w-toc li.x-w-toc2, li.x-w-toc3, li.x-w-toc4 { display: flex; align-items: baseline; } div.x-w-toc li.x-w-toc2 > span.x-w-toc-bullet:before { content: \"\\f105\"; } div.x-w-toc li.x-w-toc3 > span.x-w-toc-bullet:before, li.x-w-toc4 > span.x-w-toc-bullet:before { content: \"\\f101\"; } div.x-w-toc span.x-w-toc-bullet { display: inline-block; flex: 0 0 auto; margin: 0 5px 0 0; padding: 0 0 0 2px; width: 1em; overflow: visible; font-family: FontAwesome; } div.x-w-toc li.x-w-toc4 > span.x-w-toc-bullet { margin-left: calc(1em + 5px); } div.x-w-todo li.x-w-todo { display: flex; align-items: baseline; margin: 0.5ex auto; box-sizing: border-box; padding: 0; } div.x-w-todo li.x-w-todo.x-todo-status { display: flex; align-items: center; } div.x-w-todo .x-todo-status { flex: 0 0 auto; display: inline-flex; justify-content: center; align-self: stretch; overflow: visible; width: 2.5em; } div.x-w-todo li.x-w-todo div.x-w-todo-subject { font-weight: bold; } div.x-w-todo li.x-w-todo .x-w-todo-content { border-left: 1px solid rgba(0, 0, 0, 0.75); padding-left: 0.5em; } div.x-w-todo li.x-w-todo .x-date { font-family: var(--x-font-fixed); margin-right: 0.25em; } div.x-w-todo.x-hide-done li.x-todo-done { display: none; } div.x-w-todo .x-todo-status:before { font-size: 1.25em; font-family: FontAwesome; content: \"\\f0c8\"; } div.x-w-todo li.x-w-todo.x-todo-done .x-todo-status:before { content: \"\\f00c\"; } div.x-w-todo.x-style-dark .x-date, div.x-w-todo.x-style-light .x-date { color: inherit !important; } div.x-w-audio  { display: inline-block; width: min-content; vertical-align: top; margin: 5px; } div.x-w-audio .x-media-photo img { display: block; max-width: min(240px, 50vmin); max-height: min(240px, 50vmin); } div.x-w-audio audio.x-audio { max-width: min(240px, 50vmin); margin-top: 10px; } div.x-w-audio .x-media-caption { padding: 5px 0; font-size: 90%; } div.x-w-video  { display: inline-block; width: min-content; vertical-align: top; margin: 5px; } div.x-w-video video.x-video { max-width: min(240px, 50vmin); max-height: min(240px, 50vmin); } div.x-w-video .x-media-caption { padding: 5px 0; font-size: 90%; } .x-style-light, .x-style-light * { border-color: inherit !important; color: #000; } .x-style-dark, .x-style-dark * { border-color: inherit !important; color: #fff; } a[href].x-style-light, .x-style-light a[href] { color: #00e; } a[href].x-style-dark, .x-style-dark a[href] { color: #ff0; } .x-border-line:before { border: 1px solid rgba(0, 0, 0, 0.5); } .x-m-a:before { background-color: "
            + highlightcolor()
            + " !important; } .x-m-t:before { background-color: "
            + highlightcolor()
            + " !important; } .x-sticker { all: initial; display: block; -webkit-user-modify: inherit; -webkit-user-select: inherit; -webkit-touch-callout: inherit; -webkit-tap-highlight-color: inherit; -webkit-text-size-adjust: inherit; line-height: 1.4; font-size: "
            + defaultfontsizepx()
            + "; font-family: "
            + defaultfont()
            + "; position: absolute; } @media  print { div.x-root { height: unset !important; } #x-rightsidebar { display: none !important; } } "
)
		}
	}
	class HostCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return (AndriansCSS(conf).build()
            + PROPRIETARY + SharedCSS(conf).build()
            + ".XxXqU:before { font-family: FontAwesome; display: inline-block; font-size: inherit; text-rendering: auto; -webkit-font-smoothing: antialiased; } .XxXvv { transform: scale(-1, 1); } .XxXpj:before { content: \"\\f037\"; } .XxXk2:before { content: \"\\f039\"; } .XxX99:before { content: \"\\f036\"; } .XxXc2:before { content: \"\\f038\"; } .XxXQV:before { content: \"\\f0f9\"; } .XxXIs:before { content: \"\\f103\"; } .XxXFa:before { content: \"\\f100\"; } .XxXc5:before { content: \"\\f102\"; } .XxXE2:before { content: \"\\f187\"; } .XxXdj:before { content: \"\\f0ab\"; } .XxX81:before { content: \"\\f0a8\"; } .XxXcI:before { content: \"\\f0a9\"; } .XxXBY:before { content: \"\\f0aa\"; } .XxXq0:before { content: \"\\f063\"; } .XxXXZ:before { content: \"\\f060\"; } .XxX4y:before { content: \"\\f061\"; } .XxXDz:before { content: \"\\f062\"; } .XxXvY:before { content: \"\\f047\"; } .XxXcz:before { content: \"\\f07e\"; } .XxXD5:before { content: \"\\f07d\"; } .XxXHv:before { content: \"\\f05e\"; } .XxXob:before { content: \"\\f02a\"; } .XxXwT:before { content: \"\\f0c9\"; } .XxX4C:before { content: \"\\f0f3\"; } .XxXmf:before { content: \"\\f0a2\"; } .XxXwy:before { content: \"\\f1f6\"; } .XxXL5:before { content: \"\\f1f7\"; } .XxXio:before { content: \"\\f032\"; } .XxXNV:before { content: \"\\f02e\"; } .XxXzL:before { content: \"\\f097\"; } .XxXRg:before { content: \"\\f1e2\"; } .XxXtg:before { content: \"\\f2a1\"; } .XxX7q:before { content: \"\\f1ec\"; } .XxXin:before { content: \"\\f073\"; } .XxX25:before { content: \"\\f133\"; } .XxXEY:before { content: \"\\f271\"; } .XxXO0:before { content: \"\\f030\"; } .XxXhW:before { content: \"\\f083\"; } .XxXFu:before { content: \"\\f0a3\"; } .XxXXK:before { content: \"\\f00c\"; } .XxXoG:before { content: \"\\f058\"; } .XxXYj:before { content: \"\\f05d\"; } .XxX0o:before { content: \"\\f14a\"; } .XxXh0:before { content: \"\\f046\"; } .XxXFI:before { content: \"\\f139\"; } .XxXUi:before { content: \"\\f053\"; } .XxXif:before { content: \"\\f054\"; } .XxXcup:before { content: \"\\f077\"; } .XxX7m:before { content: \"\\f1db\"; } .XxXF8:before { content: \"\\f10c\"; } .XxXgD:before { content: \"\\f017\"; } .XxXf8:before { content: \"\\f24d\"; } .XxXN2:before { content: \"\\f00d\"; } .XxXmj:before { content: \"\\f121\"; } .XxXRJ:before { content: \"\\f0db\"; } .XxXzR:before { content: \"\\f0e5\"; } .XxXDd:before { content: \"\\f0e6\"; } .XxXBk:before { content: \"\\f066\"; } .XxXJK:before { content: \"\\f0c5\"; } .XxXOv:before { content: \"\\f125\"; } .XxX0l:before { content: \"\\f1b2\"; } .XxXH2:before { content: \"\\f0c4\"; } .XxXAb:before { content: \"\\f108\"; } .XxX7y:before { content: \"\\f1bd\"; } .XxXKW:before { content: \"\\f155\"; } .XxX1c:before { content: \"\\f192\"; } .XxX6V:before { content: \"\\f019\"; } .XxXVj:before { content: \"\\f044\"; } .XxXUm:before { content: \"\\f141\"; } .XxXWA:before { content: \"\\f142\"; } .XxXns:before { content: \"\\f0ec\"; } .XxXDK:before { content: \"\\f065\"; } .XxX3X:before { content: \"\\f08e\"; } .XxX1p:before { content: \"\\f14c\"; } .XxXRH:before { content: \"\\f06e\"; } .XxXfj:before { content: \"\\f070\"; } .XxXnH:before { content: \"\\f1fb\"; } .XxX39:before { content: \"\\f1c6\"; } .XxX3R:before { content: \"\\f1c7\"; } .XxXKH:before { content: \"\\f1c5\"; } .XxXlM:before { content: \"\\f016\"; } .XxXdl:before { content: \"\\f15c\"; } .XxXWl:before { content: \"\\f0f6\"; } .XxXNl:before { content: \"\\f1c8\"; } .XxXEt:before { content: \"\\f024\"; } .XxXfil:before { content: \"\\f0b0\"; } .XxXDY:before { content: \"\\f07b\"; } .XxXhO:before { content: \"\\f114\"; } .XxXfS:before { content: \"\\f07c\"; } .XxX0R:before { content: \"\\f115\"; } .XxXKm:before { content: \"\\f031\"; } .XxXKR:before { content: \"\\f154\"; } .XxXUX:before { content: \"\\f013\"; } .XxXLP:before { content: \"\\f0ac\"; } .XxXxg:before { content: \"\\f1dc\"; } .XxXEG:before { content: \"\\f0a7\"; } .XxXqI:before { content: \"\\f0a4\"; } .XxXcm:before { content: \"\\f0a6\"; } .XxXIJ:before { content: \"\\f292\"; } .XxXOV:before { content: \"\\f08a\"; } .XxXvB:before { content: \"\\f1da\"; } .XxXf1:before { content: \"\\f015\"; } .XxX9d:before { content: \"\\f250\"; } .XxXo9:before { content: \"\\f2c3\"; } .XxXF0:before { content: \"\\f03e\"; } .XxXR0:before { content: \"\\f03c\"; } .XxX5P:before { content: \"\\f129\"; } .XxXPR:before { content: \"\\f05a\"; } .XxXkJ:before { content: \"\\f033\"; } .XxXHr:before { content: \"\\f084\"; } .XxXeX:before { content: \"\\f11c\"; } .XxXlup:before { content: \"\\f148\"; } .XxXwR:before { content: \"\\f0c1\"; } .XxX7n:before { content: \"\\f03a\"; } .XxXAx:before { content: \"\\f0cb\"; } .XxX15:before { content: \"\\f0ca\"; } .XxX9v:before { content: \"\\f023\"; } .XxXuu:before { content: \"\\f175\"; } .XxXlv:before { content: \"\\f178\"; } .XxX1h:before { content: \"\\f176\"; } .XxX7I:before { content: \"\\f0d0\"; } .XxXXg:before { content: \"\\f130\"; } .XxXX0:before { content: \"\\f131\"; } .XxXEc:before { content: \"\\f068\"; } .XxXnG:before { content: \"\\f146\"; } .XxX8h:before { content: \"\\f147\"; } .XxX7s:before { content: \"\\f1fc\"; } .XxX2v:before { content: \"\\f0c6\"; } .XxXWR:before { content: \"\\f1dd\"; } .XxX4a:before { content: \"\\f0ea\"; } .XxXcg:before { content: \"\\f04c\"; } .XxXyZ:before { content: \"\\f040\"; } .XxXvC:before { content: \"\\f14b\"; } .XxXUl:before { content: \"\\f04b\"; } .XxXpyc:before { content: \"\\f144\"; } .XxXdN:before { content: \"\\f01d\"; } .XxX1w:before { content: \"\\f067\"; } .XxXQe:before { content: \"\\f055\"; } .XxXc8:before { content: \"\\f0fe\"; } .XxXTM:before { content: \"\\f196\"; } .XxXKJ:before { content: \"\\f02f\"; } .XxXVV:before { content: \"\\f029\"; } .XxXEE:before { content: \"\\f059\"; } .XxXy2:before { content: \"\\f10d\"; } .XxX5G:before { content: \"\\f10e\"; } .XxXHS:before { content: \"\\f074\"; } .XxXDl:before { content: \"\\f1b8\"; } .XxXvd:before { content: \"\\f021\"; } .XxXIm:before { content: \"\\f112\"; } .XxX6K:before { content: \"\\f122\"; } .XxXqA:before { content: \"\\f079\"; } .XxX2y:before { content: \"\\f0e2\"; } .XxXkv:before { content: \"\\f01e\"; } .XxXas:before { content: \"\\f09e\"; } .XxXyl:before { content: \"\\f158\"; } .XxX1e:before { content: \"\\f0c7\"; } .XxXNP:before { content: \"\\f002\"; } .XxX7J:before { content: \"\\f010\"; } .XxX3H:before { content: \"\\f00e\"; } .XxXmQ:before { content: \"\\f064\"; } .XxX6l:before { content: \"\\f1e0\"; } .XxXMG:before { content: \"\\f14d\"; } .XxXHk:before { content: \"\\f045\"; } .XxXyo:before { content: \"\\f07a\"; } .XxXd2:before { content: \"\\f2cc\"; } .XxXB4:before { content: \"\\f090\"; } .XxXcf:before { content: \"\\f08b\"; } .XxXto:before { content: \"\\f198\"; } .XxXqz:before { content: \"\\f1e7\"; } .XxXbk:before { content: \"\\f118\"; } .XxXqD:before { content: \"\\f15d\"; } .XxXw2:before { content: \"\\f110\"; } .XxXaK:before { content: \"\\f0c8\"; } .XxXu3:before { content: \"\\f096\"; } .XxXy3:before { content: \"\\f18d\"; } .XxXW5:before { content: \"\\f006\"; } .XxXkjn:before { content: \"\\f249\"; } .XxXkj:before { content: \"\\f24a\"; } .XxXUt:before { content: \"\\f04d\"; } .XxXfc:before { content: \"\\f0cc\"; } .XxXZ4:before { content: \"\\f12c\"; } .XxXTP:before { content: \"\\f12b\"; } .XxX5O:before { content: \"\\f02b\"; } .XxXCZ:before { content: \"\\f02c\"; } .XxXBq:before { content: \"\\f120\"; } .XxXfth:before { content: \"\\f00a\"; } .XxXthl:before { content: \"\\f009\"; } .XxXD0:before { content: \"\\f00b\"; } .XxXGm:before { content: \"\\f087\"; } .XxXw1:before { content: \"\\f057\"; } .XxX5o:before { content: \"\\f05c\"; } .XxXfx:before { content: \"\\f150\"; } .XxXsE:before { content: \"\\f191\"; } .XxXKw:before { content: \"\\f204\"; } .XxXOM:before { content: \"\\f205\"; } .XxXVD:before { content: \"\\f152\"; } .XxXQ2:before { content: \"\\f151\"; } .XxXRj:before { content: \"\\f1f8\"; } .XxXZ0:before { content: \"\\f014\"; } .XxXPy:before { content: \"\\f0d1\"; } .XxXI3:before { content: \"\\f173\"; } .XxXBB:before { content: \"\\f0cd\"; } .XxX5K:before { content: \"\\f127\"; } .XxXPp:before { content: \"\\f09c\"; } .XxXQI:before { content: \"\\f13e\"; } .XxXG1:before { content: \"\\f093\"; } .XxXCR:before { content: \"\\f234\"; } .XxXCy:before { content: \"\\f21b\"; } .XxXHG:before { content: \"\\f03d\"; } .XxXAw:before { content: \"\\f027\"; } .XxXG2:before { content: \"\\f028\"; } .XxXx1:before { content: \"\\f071\"; } .XxXFK:before { content: \"\\f17a\"; } .XxX1f:before { content: \"\\f159\"; } .XxXWW:before { content: \"\\f0ad\"; } .XxXUk:before { font-family: NotoSansSymbols-Regular; display: inline-block; font-size: inherit; text-rendering: auto; -webkit-font-smoothing: antialiased; } .XxXA4:before { content: \"\\2982\"; } .XxX7u:before { content: \"\\2774\"; } .XxX57:before { content: \"\\f0c4\\00a0\\f14a\"; font-size: 60%; vertical-align: middle; } .XxXyF:before { content: \"\\f0c4\\00a0\\f096\"; font-size: 60%; vertical-align: middle; } .XxXtR { display: "
            + hideInDesktop()
            + "; } body { -webkit-touch-callout: none !important; -webkit-tap-highlight-color: rgba(164,164,164,0.75) !important; -webkit-text-size-adjust: none !important; overscroll-behavior: none; overflow: hidden; } textarea { margin: 0.5ex 0; } button, select, option { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; } select { padding: 4px; } input { -webkit-appearance: none; margin: 0; padding: 0; outline: none; vertical-align: middle; border-image-width: 0; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; } input[type=input] { background-color: #fff; } input[type=checkbox], input[type=radio] { -webkit-appearance: auto; cursor: pointer; white-space: nowrap; margin: 0; vertical-align: middle; background-color: #eee; min-width: 1.2em; width: 1.2em; height: 1.2em; } select > option { display: none; } .XxXBH { width: "
            + buttonsize600px()
            + " !important; } .XxXo3 { width: "
            + buttonsize400px()
            + " !important; } .XxXAd { width: "
            + buttonsize300px()
            + " !important; } .XxXqT { width: "
            + buttonsize200px()
            + " !important; } .XxX7O { width: "
            + buttonsizepx()
            + " !important; } .XxXLO { width: 100%; height: 100%; } button.XxXbO, div.XxXH4.XxXbO, table.XxXCq td.XxXTB.XxXbO { font-size: 50%; opacity: 0.75; } .XxXqu { display: inline-block; font-family: NotoSansSymbols-Regular; } .XxXCk:before { content: '/'; } .XxXFt:before { content: '\\2022'; } .XxXuI:before { font-family: NotoSansSymbols-Regular; content: '\\2af4'; } .XxXK8:before { font-family: NotoSansSymbols-Regular; content: '\\2af5'; } .XxXLn { opacity: 0.4 !important; cursor: not-allowed !important; } .XxXVc { font-size: 85%; } .XxX2F { font-size: 50%; } .XxXeZ { font-weight: bold; color: #444; } .XxXN6 { font-weight: bold; color: #170; } .XxXz3 { color: #009; } .XxXcw { color: #444; } .XxXNJ { color: #06c; } .XxX3J { color: #e00; } .XxXeU { color: #700; } [xxx-i5] { cursor: pointer; } div.XxXEm { flex: 1 1 auto; color: transparent; background-color: transparent; border: 0.5px solid transparent; margin: 5px 0; font-size: 110%; } button.XxXH4 { flex: 0 1 "
            + buttonsizepx()
            + "; align-self: stretch; color: #444; background-color: transparent; font-size: "
            + toolbarfontsizepx()
            + "; cursor: pointer; outline: none; border-radius: 3px; box-sizing: border-box; vertical-align: middle; text-align: center; overflow: hidden; -webkit-user-select: none; user-select: none; min-height: "
            + buttonsizepx()
            + "; width: "
            + buttonsizepx()
            + "; } "
            + anbutton()
            + " div.XxXH4 { display: inline-flex; box-sizing: border-box; flex: 0 1 "
            + buttonsizepx()
            + "; align-items: center; justify-content: center; color: #444; cursor: pointer; -webkit-user-select: none; user-select: none; min-height: "
            + buttonsizepx()
            + "; font-size: "
            + toolbarfontsizepx()
            + "; } button.XxXlZ, div.XxXlZ { border: 1px solid transparent; box-sizing: border-box; align-self: stretch; box-shadow: none; background-color: transparent; } button.XxXe0, div.XxXe0 { box-sizing: border-box; background-color: rgba(0, 0, 0, 0.1) !important; border: 1px inset #ccc !important; align-self: stretch; box-shadow: none; } .XxXUV { box-sizing: border-box; cursor: pointer; -webkit-overflow-scrolling: touch; overflow: auto; flex: 0 0 "
            + buttonsizepx()
            + "; font-size: "
            + toolbarfontsizepx()
            + "; background-color: transparent; align-self: stretch; white-space: nowrap; padding: 5px; border-radius: 3px; } div.XxXwn { box-sizing: border-box; display: inline-flex; cursor: pointer; overflow: hidden; align-self: stretch; align-items: center; justify-content: center; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; white-space: nowrap; } .XxXIW { width: "
            + buttonsizepx()
            + "; display: inline-flex; justify-content: center; align-items: baseline; } div.XxXCq { position: absolute; left: 0; top: 0; opacity: 1; background-color: transparent; padding: 10px; overflow: hidden; -webkit-user-select: none; user-select: none; transform: translateZ(0); z-index: 4000; } div.XxXCq * { -webkit-user-select: none; user-select: none; } table.XxXCq { color: black; background-color: "
            + popupbg(dialogbgcolor())
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; padding: 15px; border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; border: none; } td.XxXTB { display: inline-block; vertical-align: middle; text-align: center; box-sizing: border-box; cursor: pointer; text-rendering: auto; -webkit-font-smoothing: antialiased; background-size: cover; width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; font-size: "
            + symbolfontsizepx()
            + "; } tr.XxXHE > td { display: flex; box-sizing: border-box; overflow: hidden; align-items: center; justify-content: flex-start; padding: 0; min-height: "
            + stylerowheightpx()
            + "; } div.XxXmK { display: inline-flex; box-sizing: border-box; cursor: pointer; align-items: center; align-self: center; border: none; padding: 1ex 0.5em 0.75ex 0.5em; -webkit-user-select: none; user-select: none; width: "
            + buttonsize600px()
            + "; } div.XxXJF { display: flex; flex-flow: column; align-items: center; box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; width: 100%; } div.XxXJF > div { display: inline-flex; align-items: center; align-self: center; box-sizing: border-box; border: none; padding: 1ex 0.5em 0.75ex 0.5em; -webkit-user-select: none; user-select: none; cursor: pointer; width: 100%; min-height: "
            + stylerowheightpx()
            + "; } div.XxX42 { overflow-x: hidden; } div.XxX42 > div { justify-content: space-between; } div.XxXXE { margin: 1ex 0 0.75ex 0; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 5px; } .XxXC5 { min-height: "
            + stylerowheightpx()
            + "; } .XxXQQ { display: flex; box-sizing: border-box; cursor: pointer; overflow: hidden; align-items: center; justify-content: flex-start; padding: 1ex "
            + buttonsize50px()
            + " 0.75ex 0; font-size: "
            + defaultfontsizepx()
            + "; min-height: "
            + stylerowheightpx()
            + "; } .XxXQQ > *:first-child, .XxXBK > div > *:first-child { min-width: 2em; margin-right: 0.5em; text-align: center; } div.XxXK9 > div { display: flex; box-sizing: border-box; overflow: hidden; cursor: pointer; white-space: nowrap; align-items: center; justify-content: flex-start; margin: 0; padding: 1ex 0.5em 0.75ex 0.5em; min-width: "
            + buttonsizepx()
            + "; min-height: "
            + stylerowheightpx()
            + "; border-radius: 5px; } div.XxXK9 > div.XxXQv, div.XxXcA > div.XxXQv { background-color: rgba(0, 102, 204, 0.1) !important; } table.XxXCq td.XxXQv { border: 1px inset #ccc; background-color: rgba(0, 0, 0, 0.1); } tr.XxXKC tr td { height: "
            + buttonsizepx()
            + "; border-bottom: 1px solid rgba(0, 0, 0, 0.5); } div.XxXCq td.XxXg5 { height: 1px; padding: 0; background-color: rgba(0, 0, 0, 0.5); } li.XxXXO { width: 100%; height: 0; border-bottom: 1px dotted #000; } .XxXMS { box-sizing: border-box; cursor: pointer; border: none; -webkit-appearance: none; background-color: rgba(255, 255, 255, 0.5); } .XxX9R { box-sizing: border-box; display: flex; cursor: pointer; align-items: center; padding: 1ex 0.5em 0.75ex 0.5em; background-color: transparent; justify-content: flex-start; } div.XxXCq div.XxX16 { text-align: center; vertical-align: middle; box-sizing: border-box; padding: 3px; font-size: "
            + symbolfontsizepx()
            + "; width: "
            + buttonsizepx()
            + "; } div.XxXCq div.XxXjA { display: flex; box-sizing: border-box; cursor: pointer; align-items: center; justify-content: flex-start; padding: 1ex 0.5em 0.75ex 0.5em; align-self: center; border: none; width: "
            + buttonsize400px()
            + "; -webkit-user-select: none; user-select: none; } div.XxXCq div.XxX2a { display: inline-block; box-sizing: border-box; cursor: pointer; border: none; padding: 1ex 0.5em 0.75ex 0.5em; width: "
            + buttonsize200px()
            + "; -webkit-user-select: none; user-select: none; } div.XxXmk { display: inline-block; box-sizing: border-box; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; align-self: stretch; margin: 0 0 0 5px; vertical-align: middle; cursor: pointer; width: "
            + buttonsizem5px()
            + "; } div.XxXCq div.XxXmk { margin: 3px; width: "
            + buttonsizem6px()
            + "; } div.XxXmc { display: flex; align-items: baseline; box-sizing: border-box; cursor: pointer; overflow: hidden; background-color: rgba(240, 240, 240, 1.0); padding: 1ex 1em 0.75ex 1em; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; } ul.XxXoj { display: flex; justify-content: flex-start; flex-flow: row wrap; margin: 0; padding-bottom: 75vh; } ul.XxXoj >li { display: inline-block; padding: 0; margin: 0; list-style: none; width: 1.5em; height: 1.25em; vertical-align: middle; text-align: center; overflow: hidden; cursor: pointer; min-width: "
            + buttonsizepx()
            + "; min-height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } ul.XxXoj >li.XxXQv { background-color: rgba(0, 0, 0, 0.1); border-radius: 3px; } #XxX0V { position: absolute; top: 0; left: 0; width: 100vw; transform: translateZ(0); z-index: 1990; background-color: rgba(240, 240, 240, 1.0); } #XxX0V * { -webkit-user-select: none; user-select: none; } div.XxXoy div.XxXH4 { flex-shrink: 1; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } div.XxXoy .XxXqU:before { height: 100%; } div.XxXoy input.XxXTN, div.XxXoy textarea.XxXTN, div.XxXoy input.XxXJT, div.XxXoy textarea.XxXJT { flex: 1 1 50%; box-sizing: border-box; padding: 0.5ex 4px 0.5ex 4px; background-color: rgba(255, 255, 255, 0.5); border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; outline: none; margin: 0px 8px; max-height: "
            + buttonsizepx()
            + "; min-width: "
            + buttonsizepx()
            + "; max-width: "
            + buttonsize500px()
            + "; } div.XxXoy textarea.XxXTN, div.XxXoy textarea.XxXJT { -webkit-appearance: none; line-height: initial; align-self: center; overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch; white-space: nowrap; font-family: "
            + defaultfont()
            + "; } div.XxXoy textarea.XxXJT { flex: 1 1 auto; max-width: 6em; margin-left: 0; margin-right: 0; background-color: transparent; outline: none; border: none; } #XxX0V div.XxXoy, #XxXO1 div.XxXoy { height: "
            + buttonsizep1px()
            + "; border-bottom: 1px solid rgba(0, 0, 0, 0.5); padding-right: 8px; } #XxX0V input.XxXTN { background-color: #fff; } #XxXFP { position: absolute; top: "
            + buttonsizep1px()
            + "; left: 0; height: calc(100vh - "
            + buttonsizep1px()
            + "); width: 100vw; margin: 0; padding: 0; border: none; pointer-events: auto; overflow: hidden !important; } #XxXci { position: absolute; top: 0; left: 0; margin: 0; padding: 0; border: none; box-sizing: border-box; width: 100vw; height: calc(100vh - "
            + buttonsizep1px()
            + "); overflow: hidden !important; transform: translateZ(0); } #XxX16 { position: absolute; left: 0; top: 0; color: #fff; padding: 0; margin: 0; border: none; white-space: nowrap; overflow: visible; vertical-align: top; font-weight: normal; transform-origin: 0 0; display: flex; justify-content: flex-start; align-items: flex-start; z-index: 2000; -webkit-user-select: none; user-select: none; font-size: "
            + toolbarfontsizepx()
            + "; transform: translateZ(0) rotate(90deg) translateY(-"
            + sidebartoppx()
            + "); } div.XxXbz { flex: 1 1 100px; align-self: stretch; padding: 5px; margin: 0; border: none; border-radius: 0px 10px 0px 0px; vertical-align: top; text-align: center; cursor: pointer; -webkit-user-select: none; user-select: none; min-width: "
            + buttonsizepx()
            + "; line-height: "
            + sidebarheightpx()
            + "; } #XxXT1 { border-top: 5px solid rgba(0, 136, 204, 1.0); background-color: rgba(0, 136, 204, 1.0); } #XxXgA { margin-left: 1px; border-top: 5px solid rgba(119, 187, 0, 1.0); background-color: rgba(119, 187, 0, 1.0); } #XxXdW { margin-left: 1px; border-top: 5px solid rgba(238, 119, 0, 1.0); background-color: rgba(238, 119, 0, 1.0); } #XxXGz { flex: 0 1 100px; align-self: stretch; padding: 5px; margin: 0; border: none; border-radius: 0px 10px 0px 0px; vertical-align: top; text-align: center; border-top: 5px solid rgba(204, 204, 204, 1.0); background-color: rgba(204, 204, 204, 1.0); margin-left: 1px; -webkit-user-select: none; user-select: none; line-height: "
            + sidebarheightpx()
            + "; } #XxXGz>hr { display: inline-block; border: none; border-top: 1px solid rgba(0, 0, 0, 0.5); border-bottom: 0.5px solid #fff; height: 0px; vertical-align: middle; width: 50%; } #XxXO1 { position: absolute; left: 0; top: 0; z-index: 2100; transform-origin: 0% 0%; transform: translateZ(0); -webkit-user-select: none; user-select: none; } #XxXO1 > .XxXp2 { position: absolute; background-color: "
            + popupbg(dialogbgcolor())
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; top: 0; left: 0; width: 100%; box-sizing: border-box; } #XxXO1 div.XxXTV { box-sizing: border-box; -webkit-user-select: none; user-select: none; } #XxXO1 div.XxXMl { box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } #XxXO1 div.XxXSn { width: 100%; } #XxXO1 div.XxXx7 { display: flex; align-items: stretch; align-content: flex-start; flex-direction: column; padding: 3px; padding-left: 0.5em; } #XxXO1 div.XxXpe { box-sizing: border-box; margin: 0; padding: 0.5ex 0.5em 0.5ex 0.5em; border-bottom: 1px solid rgba(0, 0, 0, 0.5); overflow: hidden; } #XxXO1 div.XxXYT { display: flex; align-items: center; font-weight: bold; line-height: 2.5; box-sizing: border-box; width: 100%; } #XxXO1 div.XxXhg { display: flex; box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; width: 100%; } #XxXO1 div.XxXXr { align-self: stretch; flex: 0 0 auto; cursor: pointer; overflow: hidden; } #XxXO1 div.XxXXr > img { margin-left: 5px; max-height: 120px; } div.XxX92, div.XxXVe { display: flex; background-color: rgba(0, 0, 0, 0.1); flex-direction: row; justify-content: flex-start; text-align: center; vertical-align: middle; color: #444; width: 100%; white-space: nowrap; overflow: hidden; font-size: "
            + toolbarfontsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; height: "
            + buttonsizep1px()
            + "; } div.XxXie { cursor: pointer; min-width: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } div.XxXie > span { min-width: "
            + buttonsizepx()
            + "; } div.XxX92 > div.XxXie, div.XxXVe > div.XxXie { display: inline-block; border: 1px solid transparent; flex: auto; text-align: center; vertical-align: middle; } div.XxX92 > div.XxXie { border-bottom: 1px solid rgba(0, 0, 0, 0.5); } div.XxXVe > div.XxXie { border-top: 1px solid rgba(0, 0, 0, 0.5); } div.XxX92 > div.XxXQv { border: 1px solid rgba(0, 0, 0, 0.5); border-bottom-color: transparent; border-radius: 5px 5px 0 0; } div.XxXVe > div.XxXQv { border: 1px solid rgba(0, 0, 0, 0.5); border-top-color: transparent; border-radius: 0 0 5px 5px; } .XxXp2 > div.XxX92 > div.XxXQv:first-child, .XxXp2 > div.XxXVe > div.XxXQv:first-child { border-left-color: transparent; } .XxXp2 > div.XxX92 > div.XxXQv:last-child, .XxXp2 > div.XxXVe > div.XxXQv:last-child { border-right-color: transparent; } div.XxX92 > div.XxXLn > * { opacity: 0.25; } #XxXO1 div.XxXiu { border-top: 1px solid rgba(0, 0, 0, 0.5); } #XxXO1 div.XxX9t { box-sizing: border-box; } #XxXO1 option[selected] { background-color: #fff; } #XxXO1 option.XxXx0 { color: #888; } .XxXbn { display: flex; align-items: center; justify-content: center; align-self: center; margin: 0 auto; text-align: center; } div.XxX8Z { display: flex; align-items: center; box-sizing: border-box; width: 100%; color: #444; font-weight: bold; padding: 4px 0; } div.XxXwF { display: flex; align-items: center; box-sizing: border-box; width: 100%; } div.XxX8Z > div, div.XxXwF > div { display: flex; flex-wrap: wrap; flex: 1 1 14.285%; align-items: center; align-content: center; justify-content: center; max-height: 28.57%; text-align: center; vertical-align: middle; -webkit-overflow-scrolling: touch; overflow: auto; box-sizing: border-box; padding: 1px; word-break: break-all; border: 1px solid transparent; align-self: stretch; } div.XxXik { display: flex; flex: 1 1 25%; align-self: stretch; align-items: center; justify-content: center; box-sizing: border-box; border-bottom: 1px solid rgba(0, 0, 0, 0.5); overflow: hidden; -webkit-user-select: none; user-select: none; font-weight: bold; color: teal; } div.XxXFX { display: flex; flex: 1 1 25%; align-self: stretch; align-items: center; justify-content: center; box-sizing: border-box; border-bottom: 1px solid rgba(0, 0, 0, 0.5); overflow: hidden; -webkit-user-select: none; user-select: none; font-weight: bold; color: #444; } textarea.XxXXd { display: block; -webkit-appearance: none; background-color: "
            + popupbg("rgba(204, 238, 170, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; box-sizing: border-box; width: 100%; font-size: 120%; min-height: 7ex; height: 7ex; margin: 0; padding: 1ex 0.5em; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 5px; -webkit-overflow-scrolling: touch; overflow: auto; resize: none; align-self: stretch; outline: none; white-space: normal; word-break: break-all; } canvas.XxXyA { outline: 0.5px solid rgba(136,136,136,0.5); outline-offset: -0.5px; float: left; clear: both; -webkit-user-select: none; user-select: none; } canvas.XxXiG { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; -webkit-user-select: none; user-select: none; transform: translate(0,0); } textarea.XxX3N { position: absolute; top: 0; left: 0; overflow: visible; } .XxXei, .XxX7i { display: inline-flex !important; align-items: center; justify-content: center; box-sizing: border-box; overflow: visible; width: 2em; } .XxX7i { margin-right: 0.5em; } #XxXhK { z-index: 1200; display: none; flex-direction: column; flex-wrap: nowrap; position: absolute; top: "
            + buttonsizep1px()
            + "; left: calc(100vw - 6em); box-sizing: border-box; width: 6em; height: calc(100vh - "
            + buttonsizep1px()
            + "); padding: 1ex 1em 0.75ex 1em; color: #fff; background-color: rgba(116,27,71,0.75); -webkit-overflow-scrolling: touch; overflow: auto; white-space: nowrap; } #XxXhK > div { display: flex; align-items: center; min-height: "
            + stylerowheightpx()
            + "; } .XxXb2 > table.XxX2o { background-color: #444 !important; border: 1px solid rgba(0, 0, 0, 0.2); box-shadow: 0.25rem 0.25rem 1.25rem rgba(0, 0, 0, 0.75); width: 80vw; max-width: 400px; padding: 0; } div.XxXb2 table.XxX2o div.XxXoy { background-color: #00afff; } .XxXb2 div.XxXbG { display: flex; flex-direction: column-reverse; margin: 0 auto; outline: none; -webkit-user-select: none; user-select: none; min-height: 22ex; } div.XxXKb { background-color: #00afff; flex: 1 1 auto; box-sizing: border-box; font-size: 3rem; color: #8df; width: 100%; } div.XxXwG { display: flex; flex: 0 0 auto; box-sizing: border-box; border-radius: 0 0 5px 5px; width: 100%; padding: 0 5px; align-self: flex-end; } div.XxXec { vertical-align: middle; text-align: center; box-sizing: border-box; color: #fff; font-size: "
            + symbolfontsizepx()
            + "; width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } input.XxXSH { -webkit-appearance: slider-horizontal; } div.XxXyI { background-color: #00afff; display: flex; flex: 0 0 auto; align-self: flex-end; justify-content: flex-end; box-sizing: border-box; width: 100%; padding: 0 0.5em 0.25ex 0.5em; } div.XxX1j { background-color: #00afff; margin: 0 5px; font-size: 1rem; font-weight: bold; -webkit-overflow-scrolling: touch; overflow: auto; white-space: nowrap; color: #cff; } div.XxXRz { margin: 0 5px; font-family: UbuntuCondensed-Regular; font-size: 1.25rem; font-weight: bold; color: #fff; min-width: "
            + buttonsizepx()
            + "; } .XxXx2 { background-color: "
            + popupbg(dialogbgcolor())
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; border: "
            + dragborder()
            + "; border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; } .XxXAt { -webkit-overflow-scrolling: touch; overflow: auto; min-width: 12em; max-width: 16em; max-height: min(64ex, 75vh); } .XxXff { -webkit-overflow-scrolling: touch; overflow: auto; min-width: 16em; max-width: 24em; max-height: min(64ex, 75vh); } div.XxXy1 > div { display: flex; align-items: center; width: -webkit-fill-available; min-width: fit-content; padding: 1ex 1em 0.75ex 1em; box-sizing: border-box; min-height: "
            + stylerowheightpx()
            + "; white-space: nowrap; } div.XxXy1.XxXBK > div { padding-left: 0; } div.XxXkt div.XxXeJ { display: flex; flex-flow: column nowrap; align-items: flex-start; box-sizing: border-box; white-space: nowrap; width: 100%; line-height: 1.6; padding: 1ex 0.5em 0.75ex 0.5em; min-height: "
            + stylerowheightpx()
            + "; } div.XxXkt div.XxXeJ > div { display: flex; align-items: baseline; white-space: nowrap; } div.XxXkt .XxXlz { font-family: Ruda-Black; } div.XxXkt .XxXvF { font-size: 90%; } div.XxXkt .XxXMH { font-size: 90%; } div.XxXJ1 > div.XxXNn { background-color: rgba(0, 128, 0, 0.1); } .XxXHD:after { font-family: FontAwesome; font-size: 50%; line-height: 0; padding-bottom: 2ex; padding-left: 0.5em; } .XxXHDr:after { font-family: FontAwesome; font-size: 50%; } .XxXuk:after { content: \"\\f0a2\"; } .XxXukb { border-bottom: 1px solid !important; } .XxXLRd:after { content: \"\\f00c\"; } .XxXLR:after { content: \"\\f096\"; } .XxXLRe:after { content: \"\\00a0\"; } .XxXUH { color: #070; } .XxXUH .XxXlz { font-weight: bold; } .XxXcu { color: #070; } .XxXCJ { color: #444; } .XxXnz { color: #999; } .XxXt0 { background-color: #ddd; width: "
            + buttonsize150px()
            + "; height: "
            + buttonsize150px()
            + "; margin: 5px; box-sizing: border-box; flex: 1 1 auto; } .XxXld { background-color: #ddd; width: 100%; height: 100%; overflow: hidden; } .XxX3r { border-radius: 0.35rem; } .XxXic { box-shadow: 0 0 10px rgba(0,0,0,0.5); } .XxXDA { background-color: #9cf; } .XxXp6 { background-color: #fff; } #XxXqS { position: absolute; top: 0; left: 0; z-index: -1000; width: 1; height: 1; -webkit-appearance: none; background-color: transparent; color: transparent; } .XxXKS { width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; box-sizing: border-box; } .XxXBS { max-width: calc(100vw - 10px); max-height: calc(100vh - 10px); box-sizing: border-box; border: 10px solid rgba(255, 255, 255, 1.0); } #XxXhK > div.XxXQv { border-bottom: 0.5px solid #fff; font-weight: bold; } @media  print { #XxX0V, #XxX16, #XxXO1, #x-rightsidebar { display: none !important; } } "
)
		}
	}
	class CoreCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return (":root { --font-fixed: "
            + fixedfont()
            + "; --font-default: "
            + defaultfont()
            + "; --font-size: "
            + defaultfontsize()
            + "; --button-size: "
            + buttonsize()
            + "; --annotation-color: "
            + annotationcolor()
            + "; --bold-color: "
            + headingcolor()
            + "; --highlight-color: "
            + highlightcolor()
            + "; --link-color: "
            + linkcolor()
            + "; --dark-border: 1px solid rgba(0, 0, 0, 0.75); --light-border: 1px solid rgba(0, 0, 0, 0.4); --dotted-border: 1px dotted rgba(0, 0, 0, 0.5); --theme-border: 1px solid rgba(0, 0, 0, 0.5); --x-font-fixed: "
            + fixedfont()
            + "; --x-font-default: "
            + defaultfont()
            + "; --x-font-size: "
            + defaultfontsize()
            + "; --x-button-size: "
            + buttonsize()
            + "; --x-annotation-color: "
            + annotationcolor()
            + "; --x-bold-color: "
            + headingcolor()
            + "; --x-highlight-color: "
            + highlightcolor()
            + "; --x-link-color: "
            + linkcolor()
            + "; --x-dark-border: 1px solid rgba(0, 0, 0, 0.75); --x-light-border: 1px solid rgba(0, 0, 0, 0.4); --x-dotted-border: 1px dotted rgba(0, 0, 0, 0.5); --x-theme-border: 1px solid rgba(0, 0, 0, 0.5); --x-transparent-border: 1px solid transparent; --x-theme-border-radius: 0.35rem; --x-theme-box-shadow: 0 0 10px rgba(0,0,0,0.5); --x-theme-dialog-bgcolor: rgba(255, 255, 255, 0.80); --x-theme-toolbar-bgcolor: rgba(240, 240, 240, 1.0); --x-font-size-px: "
            + defaultfontsizepx()
            + "; --x-button-size-px: "
            + buttonsizepx()
            + "; --x-theme-lineheight: 1.4; } html, body, input, textarea, keygen, select, button { line-height: 1.4; font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; } select { -webkit-appearance: none; padding: 4px; } html, body, iframe, div, p, span, ul, ol, table, tbody, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } table, tbody { border-spacing: 0; } code, pre { white-space: pre; -webkit-overflow-scrolling: touch; overflow: auto; flex: 0 0 auto; font-family: "
            + fixedfont()
            + "; } input { -webkit-user-select: text !important; user-select: text !important; } textarea { -webkit-user-select: text !important; user-select: text !important; resize: none; font-family: "
            + fixedfont()
            + "; } li { margin: 0.5ex 0 0 1.5em; } blockquote { margin: 0 1.5em; padding: 0; } ul, ol { margin: 0.5ex 0; } audio { height: 4ex; border-radius: 2ex; border: 1px solid rgb(0, 0, 0, 0.25); width: -webkit-fill-available; } .x-root { position: absolute; top: 0; left: 0; height: 100vh; width: 100vw; z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; } "
)
		}
	}
	class ImageCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("body { -webkit-touch-callout: none !important; -webkit-tap-highlight-color: rgba(164,164,164,0.75) !important; overscroll-behavior: none; background-color: #fff; width: 100vw; height: 100vh; margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } .x-root { position: absolute; top: 0; left: 0; height: 100vh; width: 100vw; z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; } div, img, video, audio, table, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } #x-viewer { z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; text-align: center; } #x-content { display: none; position: absolute; margin: auto; -webkit-user-select: none; user-select: none; } "
)
		}
	}
	class SharedCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return (":root { --font-fixed: "
            + fixedfont()
            + "; --font-default: "
            + defaultfont()
            + "; --font-size: "
            + defaultfontsize()
            + "; --button-size: "
            + buttonsize()
            + "; --annotation-color: "
            + annotationcolor()
            + "; --bold-color: "
            + headingcolor()
            + "; --highlight-color: "
            + highlightcolor()
            + "; --link-color: "
            + linkcolor()
            + "; --dark-border: 1px solid rgba(0, 0, 0, 0.75); --light-border: 1px solid rgba(0, 0, 0, 0.4); --dotted-border: 1px dotted rgba(0, 0, 0, 0.5); --theme-border: 1px solid rgba(0, 0, 0, 0.5); --x-font-fixed: "
            + fixedfont()
            + "; --x-font-default: "
            + defaultfont()
            + "; --x-font-size: "
            + defaultfontsize()
            + "; --x-button-size: "
            + buttonsize()
            + "; --x-annotation-color: "
            + annotationcolor()
            + "; --x-bold-color: "
            + headingcolor()
            + "; --x-highlight-color: "
            + highlightcolor()
            + "; --x-link-color: "
            + linkcolor()
            + "; --x-dark-border: 1px solid rgba(0, 0, 0, 0.75); --x-light-border: 1px solid rgba(0, 0, 0, 0.4); --x-dotted-border: 1px dotted rgba(0, 0, 0, 0.5); --x-theme-border: 1px solid rgba(0, 0, 0, 0.5); --x-transparent-border: 1px solid transparent; --x-theme-border-radius: 0.35rem; --x-theme-box-shadow: 0 0 10px rgba(0,0,0,0.5); --x-theme-dialog-bgcolor: rgba(255, 255, 255, 0.80); --x-theme-toolbar-bgcolor: rgba(240, 240, 240, 1.0); --x-font-size-px: "
            + defaultfontsizepx()
            + "; --x-button-size-px: "
            + buttonsizepx()
            + "; --x-theme-lineheight: 1.4; } html, body, input, textarea, keygen, select, button { line-height: 1.4; font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; } select { -webkit-appearance: none; padding: 4px; } html, body, iframe, div, p, span, ul, ol, table, tbody, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } table, tbody { border-spacing: 0; } code, pre { white-space: pre; -webkit-overflow-scrolling: touch; overflow: auto; flex: 0 0 auto; font-family: "
            + fixedfont()
            + "; } input { -webkit-user-select: text !important; user-select: text !important; } textarea { -webkit-user-select: text !important; user-select: text !important; resize: none; font-family: "
            + fixedfont()
            + "; } li { margin: 0.5ex 0 0 1.5em; } blockquote { margin: 0 1.5em; padding: 0; } ul, ol { margin: 0.5ex 0; } audio { height: 4ex; border-radius: 2ex; border: 1px solid rgb(0, 0, 0, 0.25); width: -webkit-fill-available; } .x-root { position: absolute; top: 0; left: 0; height: 100vh; width: 100vw; z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; } .x-root, .x-a-content, .x-sticker { line-height: 1.4; } a, a:visited { text-decoration: none; color: auto; } a[href], a:visited[href] { color: "
            + linkcolor()
            + "; } .x-font-fixed { font-family: "
            + fixedfont()
            + "; } .x-pre { font-family: "
            + fixedfont()
            + "; color: "
            + precolor()
            + "; } .x-code  { font-family: "
            + fixedfont()
            + "; color: "
            + codecolor()
            + "; } .x-nowrap { white-space: nowrap; } .x-bold-color { color: "
            + headingcolor()
            + "; } .x-link-color { color: "
            + linkcolor()
            + "; } .x-annotation-color { color: "
            + annotationcolor()
            + "; } .x-highlight-color { color: "
            + highlightcolor()
            + "; } .x-error { color: "
            + errorcolor()
            + "; border-bottom: 1px dashed "
            + errorcolor()
            + "; } .x-warn { color: "
            + warncolor()
            + "; border-bottom: 1px dashed "
            + warncolor()
            + "; } .x-head3, .x-head2, .x-head1 { color: "
            + headingcolor()
            + "; } .x-compact { margin: 0  !important; padding: 0  !important; } .x-disabled { opacity: 0.4; cursor: not-allowed !important; } .x-placeholder { color: #888 !important; } .x-segments > span, .x-segment { display: inline-block; } .x-title { font-size: 2.5rem; font-weight: bold; } .x-bold { font-weight: bold; } .x-italic { font-style: italic; } .x-underline { text-decoration: underline; } .x-strikethrough { text-decoration: line-through; } .x-subscript { position: relative; font-size: 0.75em; top: 0.5ex; } .x-superscript { position: relative; font-size: 0.75em; bottom: 0.75ex; } .x-small { font-size: 75%; } .x-large { font-size: 150%; } .x-align-left { text-align: left; } .x-align-center { text-align: center; } .x-align-justify { text-align: justify; } .x-align-right { text-align: right; } .x-float-left { float: left; text-align: left; } .x-float-right { float: right; text-align: right; } .x-float-left-clear-none { float: left; text-align: left; clear: none; margin-right: 10px; } .x-float-right-clear-none { float: right; text-align: right; clear: none; margin-left: 1em; } .x-highlight-gray { background-color: rgba(160,160,160,0.5); } .x-highlight-blue { background-color: rgba(0,192,255,0.33); } .x-highlight-green { background-color: rgba(179,255,58,0.75); } .x-highlight-yellow { background-color: rgba(255,255,128,0.75); } .x-highlight-orange { background-color: rgba(255,128,0,0.5); } .x-highlight-red { background-color: rgba(255,0,0,0.33); } .x-shadow-gray { text-shadow: 0 0 2px rgba(0, 0, 0, 0.75); } .x-shadow-blue { text-shadow: 0 0 2px rgba(0, 0, 255, 0.75); } .x-shadow-green { text-shadow: 0 0 2px #0f0; } .x-shadow-yellow { text-shadow: 0 0 2px #ff0; } .x-shadow-orange { text-shadow: 0 0 2px #f80; } .x-shadow-red { text-shadow: 0 0 2px #f00; } .x-shadow-se-black { text-shadow: 2px 2px 1px rgba(0, 0, 0, 1.0); } .x-shadow-se-gray { text-shadow: 2px 2px 1px rgba(160, 160, 160, 1.0); } .x-shadow-se-white { text-shadow: 2px 2px 1px rgba(255, 255, 255, 1.0); } .x-shadow-se2-black { text-shadow: 0.4em 0.4ex 2px rgba(0, 0, 0, 0.75); } .x-shadow-se2-gray { text-shadow: 0.4em 0.4ex 2px rgba(0, 0, 0, 0.2); } .x-shadow-se2-white { text-shadow: 0.4em 0.4ex 2px rgba(255, 255, 255, 0.75); } .x-head3 { font-size: inherit; font-weight: bold; } .x-head2 { font-size: 150%; font-weight: bold; } .x-head1 { font-size: 200%; font-weight: bold; } .x-hidden { display: none !important; } .x-black, .x-color-black { color: #000; } .x-white, .x-color-white { color: #fff; } .x-gray, .x-color-gray { color: #777; } .x-orange, .x-color-orange { color: #f50; } .x-red, .x-color-red { color: #f00; } .x-green, .x-color-green { color: #080; } .x-plum, .x-color-plum { color: #7f0055; } .x-darkblue, .x-color-darkblue { color: #000080; } .x-blue, .x-color-blue { color: #00f; } .x-dodgerblue, .x-color-dodgerblue { color: #1e90ff; } .x-color-teal { color: #008080; } .x-color-yellow { color: #ff8; } table.x-compact { border-spacing: 0; border-collapse: collapse; } table.x-compact > tr > td { padding: 0; } ul.x-indent { margin-left: 1.5em; } ul.x-none { margin-left: 0; } ul.x-none > li, li.x-none { margin-left: 0; list-style: none; } ul.x-list-style-none > li, li.x-list-style-none { list-style: none; } ul.x-compact > li { margin-top: 0; margin-bottom: 0; padding: 0; } p.x-code, div.x-code, pre.x-code, td.x-code { box-sizing: border-box; max-width: 100%; margin: 0.5rem 1rem; padding: 10px; } p.x-code, div.x-code, pre.x-code, td.x-code { white-space: pre; -webkit-overflow-scrolling: touch; overflow: auto; padding-left: 10px; border-left: 0.3rem solid #7f0055; } td.x-code { overflow: hidden; } p.x-pre, div.x-pre, pre.x-pre { box-sizing: border-box; max-width: 100%; margin: 0.5rem 1rem; padding: 10px; } p.x-pre, div.x-pre, pre.x-pre, td.x-pre { white-space: pre; word-wrap: normal; word-break: normal; -webkit-overflow-scrolling: touch; overflow: auto; padding-left: 10px; border-left: 0.3rem solid #000080; } td.x-pre { overflow: hidden; } p.x-header, div.x-header, td.x-header { background-color: #e4e4e4; border-top: 1px solid rgba(0, 0, 0, 0.5); vertical-align: middle; border-bottom: 1px dotted rgba(0, 0, 0, 0.5); } p.x-header, div.x-header { margin: auto; padding: 10px; } p.x-body, div.x-body { margin: none; padding: 0; } .x-body { font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; font-weight: normal; font-style: normal; } p.x-para, div.x-para, pre.x-para { margin: 1ex auto; padding: 0; } p.x-box-gray, div.x-box-gray, td.x-box-gray { background-color: #e4e4e4; margin: 0.5rem 0; padding: 0.5rem; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; } p.x-box, div.x-box, td.x-box { border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; box-shadow: 1px 1px 8px rgba(102, 102, 102, 0.5); } p.x-box, div.x-box { margin: 5px auto; padding: 10px; } p.x-footer, div.x-footer, td.x-footer { background-color: #e4e4e4; border-top: 1px dotted rgba(0, 0, 0, 0.5); border-bottom: 1px solid rgba(0, 0, 0, 0.5); } p.x-footer, div.x-footer { margin: auto; padding: 10px; } p.x-head1, div.x-head1, td.x-head1 { font-size: 200%; font-weight: bold; } p.x-head2, div.x-head2, td.x-head2 { font-size: 150%; font-weight: bold; } p.x-head3, div.x-head3, td.x-head3 { font-size: 100%; font-weight: bold; } p.x-head3, div.x-head3 { margin: 5px  0; } p.x-head2, div.x-head2 { margin: 10px  0; } p.x-head1, div.x-head1 { margin: 20px  0; } .x-fa:before { font-family: FontAwesome; display: inline-block; font-size: inherit; text-rendering: auto; -webkit-font-smoothing: antialiased; } .x-fa90:before { transform: rotate(90deg); } .x-emoji-photo:before { content: \"\\1f4f7\"; } .x-emoji-audio:before { content: \"\\1f399\"; } .x-emoji-blog:before { content: \"\\1f4c5\"; } .x-emoji-calendar:before { content: \"\\1f5d3\"; } .x-emoji-canvas:before { content: \"\\1f3a8\"; } .x-emoji-blank:before { content: \"\\1f4c4\"; } .x-emoji-home:before { content: \"\\1f3e0\"; } .x-emoji-memo:before { content: \"\\1f4dd\"; } .x-emoji-notes:before { content: \"\\1f5d2\"; } .x-emoji-cards:before { content: \"\\1f5c2\"; } .x-emoji-shopping:before { content: \"\\1f6d2\"; } .x-emoji-sticker:before { content: \"\\1f4cc\"; } .x-emoji-todo:before { content: \"\\2714\"; } .x-fa-nbsp:before { content: '\\00a0'; } "
)
		}
	}
	class AndriansCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("div.XxXTT { position: absolute; left: 0; top: 0; width: 100vw; height: 100vh; margin: 0; padding: 0; border: none; outline: none; background-color: #000; } .XxXTi { position: fixed; left: 0; top: 0; outline: none; margin: 0; background-color: transparent; padding: 10px; transform: translate(-10px, -10px); border: 2px solid #000; border-radius: 50%; box-shadow: 0 0 2px 2px rgba(255,255,255,0.5); } .XxXrc { background-color: rgba(255, 255, 255, 0.75) !important; border-radius: 5px; box-shadow: 0 0 10px 5px rgba(255, 255, 255, 0.75) !important; } .XxXHa { border-radius: 5px; background-color: rgba(0, 0, 0, 0.1) !important; } .XxXBp { border-radius: 5px; background-color: rgba(255, 255, 255, 0.75) !important; } div.XxX2b { position: fixed; white-space: pre-wrap; box-sizing: border-box; max-width: 75vw; max-height: 50vh; line-height: 1.4; border: 1px solid rgba(0, 0, 0, 0.5); -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } div.XxX2b.XxXn8 { white-space: nowrap; } div.XxX1l { padding: 2ex 4ex 2ex 4ex; border-radius: 0.75ex; box-shadow: "
            + boxshadow()
            + "; } div.XxXhn { padding: 2ex 4ex 2ex 4ex; border-radius: 0.75ex; box-shadow: "
            + boxshadow()
            + "; } div.XxXCV { z-index: 5000; background-color: "
            + popupbg("rgba(136, 221, 170, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxXn8 { z-index: 5000; background-color: "
            + popupbg("rgba(136, 221, 170, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxXqN { z-index: 5000; background-color: "
            + popupbg("rgba(187, 221, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxXE7 { z-index: 5100; background-color: "
            + popupbg("rgba(255, 238, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxXUN { z-index: 5200; background-color: "
            + popupbg("rgba(255, 170, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } table.XxXFg { table-layout: fixed; } table.XxX2b td { padding: 0px 0.5ex; text-align: left; } .XxX17 { -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } table.XxXzC { border-bottom: 1px solid rgba(0, 0, 0, 0.5); box-sizing: border-box; width: 100%; } div.XxXat { display: flex; align-items: center; margin: 0; padding: 0; border: none; white-space: nowrap; width: 100%; -webkit-user-select: none; user-select: none; } div.XxXat.XxXhb, div.XxXat.XxXhb div.XxXat { background-color: rgba(0, 102, 204, 0.1) !important; } div.XxXV3 { display: flex; align-items: baseline; } span.XxXGr { display: flex; align-items: center; justify-content: center; width: "
            + buttonsizepx()
            + "; min-width: "
            + buttonsizepx()
            + "; min-height: "
            + stylerowheightpx()
            + "; cursor: pointer; margin: 0; padding: 0; } span.XxXV3 { display: flex; align-items: baseline; padding: 1ex 0.5em 0.75ex 0; margin: 0; } span.XxXej:before { font-family: FontAwesome; content: \"\\f101\"; } span.XxXnX:before { font-family: FontAwesome; content: \"\\f103\"; } span.XxXyN:before { font-family: FontAwesome; content: \"\\00a0\"; } div.XxXSn a, div.XxXSn a:visited, div.XxXpH a, div.XxXpH a:visited, div.XxXDk a, div.XxXDk a:visited, div.XxXDO a, div.XxXDO a:visited, div.XxXkt a, div.XxXkt a:visited, div.XxXJ1 a, div.XxXJ1 a:visited, div.XxXy1 a, div.XxXy1 a:visited { color: #06c; cursor: pointer; } div.XxXSn textarea.XxXR6 , div.XxXSn input.XxXR6 , div.XxXpH textarea.XxXR6 , div.XxXpH input.XxXR6  { display: block; -webkit-appearance: none; background-color: rgba(255, 255, 255, 0.5); color: #06c; width: 100%; box-sizing: border-box; padding: 1ex 1em 0.75ex 1em; border: none; border-radius: 0; outline: none; font-size: "
            + defaultfontsizepx()
            + "; } textarea.XxXR6  { margin: 0; overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch; white-space: nowrap; font-family: "
            + defaultfont()
            + "; } div.XxXDO { display: flex; align-items: baseline; flex-flow: nowrap; white-space: nowrap; flex: 1 1 100%; color: #444; padding: 1ex 1em 0.75ex 1em; box-sizing: border-box; overflow: hidden; } div.XxXSn div.XxXDO { background-color: rgba(0, 0, 0, 0.1); } div.XxXTE { -webkit-overflow-scrolling: touch; overflow: auto; } div.XxXSn div.XxXNV, div.XxXpH div.XxXNV { font-size: 120%; line-height: normal; } div.XxXDk { width: -webkit-fill-available; color: #444; padding: 0; -webkit-overflow-scrolling: touch; overflow: auto; white-space: nowrap; } div.XxXSn div.XxXDk { border: none; } div.XxXSn div.XxXDk div.XxXbG { background-color: rgba(255, 255, 255, 0.5); } div.XxXDk div.XxXbG > div { display: flex; align-items: center; min-height: "
            + stylerowheightpx()
            + "; padding: 1ex 1em 0.75ex 1em; box-sizing: border-box; white-space: nowrap; } div.XxXDk div.XxXbG > div > div { display: flex; width: 100%; align-items: baseline; white-space: nowrap; } div.XxXDk div.XxXbG a.XxXdP { flex: 1 1 auto; font-weight: bold; } div.XxXDk div.XxXbG a.XxXPN { flex: 1 1 auto; } div.XxXDk div.XxXbG a.XxX8A, div.XxXDk div.XxXbG a.XxXLd { font-family: "
            + fixedfont()
            + "; } table.XxXpH { background-color: "
            + popupbg(dialogbgcolor())
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; -webkit-overflow-scrolling: touch; overflow: auto; outline: none; margin: auto; padding: 0 10px 10px 10px; border-radius: 5px; border: "
            + dragborder()
            + "; box-shadow: "
            + boxshadow()
            + "; } table.XxXpH, table.XxXpH * { -webkit-user-select: none; user-select: none; } table.XxXpH textarea.XxXR6, table.XxXpH input.XxXR6 { background-color: rgba(255, 255, 255, 0.5); border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; } table.XxXpH textarea.XxXR6.XxXLn, table.XxXpH input.XxXR6.XxXLn { user-select: none; color: #777; background-color: rgba(0, 0, 0, 0.1) !important; } table.XxXpH div.XxXDk, div.XxXpH div.XxXDk { border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; margin: 0; width: -webkit-fill-available; } div.XxXpH div.XxXDk div.XxXbG { background-color: rgba(255, 255, 255, 0.5); } td.XxXdv { vertical-align: top; margin: 0; padding: 8px; border: none; } td.XxXSl { background-color: #fff; padding: 4px; text-align: center; vertical-align: middle; } td.XxXSl.XxXQv { background-color: rgba(0, 0, 0, 0.1); } td.XxXSl img { display: flex; margin: auto; box-shadow: "
            + boxshadow()
            + "; } div.XxX2o { display: none; position: absolute; box-sizing: border-box; left: 0; top: 0; line-height: 1.4; background-color: transparent; } table.XxX2o { table-layout: fixed; background-color: "
            + popupbg(dialogbgcolor())
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; box-sizing: border-box; padding: 0 10px 10px 10px; border: "
            + dragborder()
            + "; border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; overflow: hidden; } div.XxXUy, div.XxXbG { padding: 0; outline: none; -webkit-overflow-scrolling: touch; overflow: auto; border: none; border-image-width: 0; } div.XxXcG { padding: 0; -webkit-overflow-scrolling: touch; overflow: auto; outline: 1px solid rgba(0, 0, 0, 0.5); box-sizing: border-box; } div.XxXbG input, div.XxXcG input { overflow: hidden; } div.XxXA0 { margin: 1ex 0; font-weight: bold; color: #444; } div.XxX2o.XxXy9 table.XxX2o { background-color: "
            + popupbg("rgba(187, 221, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxX2o.XxXkG table.XxX2o { background-color: "
            + popupbg("rgba(255, 238, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } div.XxX2o.XxXK0 table.XxX2o { background-color: "
            + popupbg("rgba(255, 170, 0, 0.80)")
            + "; backdrop-filter: "
            + popupbackdrop("blur(25px)")
            + "; } table.XxX2o tr { width: 100%; } table.XxX2o * { -webkit-user-select: none; user-select: none; } div.XxXfN { background-color: transparent; display: none; color: #444; vertical-align: middle; margin: 5px 0; text-align: left; } div.XxX8I { display: none; padding: 1ex 0 0 0; vertical-align: middle; text-align: left; } div.XxXN9 { display: flex; align-items: center; color: #444; padding: 0.5ex; vertical-align: middle; text-align: left; } div.XxXmX { padding: 0.5ex 0.5em 0.5ex 0; margin: -0.5ex 0 -0.5ex 0; } div.XxXsz, div.XxX6J { color: #444; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; padding: 0.5ex 0.5em; -webkit-overflow-scrolling: touch; overflow: auto; vertical-align: middle; white-space: nowrap; text-align: left; } div.XxXsz, div.XxX6J { background-color: rgba(255, 255, 255, 0.5); } div.XxXsz > div { display: flex; align-items: center; padding: 0.5ex 0 0.5ex 0; } div.XxX9N { color: #444; font-weight: bold; padding: 0px 1em 0px 0px; -webkit-overflow-scrolling: touch; overflow: auto; vertical-align: middle; text-align: right; } textarea.XxXNQ, input.XxXNQ { -webkit-appearance: none; box-sizing: border-box; width: 100%; padding: 1ex 0.5em 0.75ex 0.5em; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; } textarea.XxXNQ { margin: 0; overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch; white-space: nowrap; font-family: "
            + defaultfont()
            + "; } div.XxXNQ, div.XxXbq { -webkit-appearance: none; box-sizing: border-box; width: 100%; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; display: flex; align-items: center; justify-content: flex-start; padding: 1ex 0.5em 0.75ex 0.5em; background-color: rgba(255, 255, 255, 0.5); } div.XxXMw { display: flex; align-items: center; background-color: rgba(255, 255, 255, 0.5); border: 1px solid rgba(0, 0, 0, 0.5); box-sizing: border-box; width: 100%; min-width: "
            + buttonsizepx()
            + "; padding: 0; border-radius: 3px; height: "
            + buttonsizepx()
            + "; overflow: hidden; } textarea.XxXMw, input.XxXMw { -webkit-appearance: none; padding: 1ex 0.5em 0.75ex 0.5em; box-sizing: border-box; width: 100%; border: none; } textarea.XxXMw { margin: 0; overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch; white-space: nowrap; font-family: "
            + defaultfont()
            + "; } select.XxXbq, div.XxXbq { box-sizing: border-box; width: 100%; padding: 1ex 0.5em 0.75ex 0.5em; overflow: auto; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; background-color: rgba(255, 255, 255, 0.5); } select.XxXRC, div.XxXRC { flex: 0 0 auto; width: auto; min-width: 4em; box-sizing: border-box; padding: 1ex 1em 0.75ex 1em; text-align: center; overflow: auto; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; background-color: rgba(255, 255, 255, 0.5); } select.XxXRC.XxXLn, div.XxXRC.XxXLn { user-select: none; color: #777; background-color: rgba(0, 0, 0, 0.1) !important; } select.XxXbq *, select.XxXRC *, div.XxXbq *, div.XxXRC * { display: none; } div.XxX2o input.XxXNQ, div.XxX2o textarea.XxXNQ { background-color: rgba(255, 255, 255, 0.5); display: block; } div.XxXGt { border: 1px solid rgba(0, 0, 0, 0.5); background-color: rgba(255, 255, 255, 0.5); } div.XxXVd { position: relative; border: 1px solid rgba(0, 0, 0, 0.5); box-sizing: border-box; background-color: rgba(0, 0, 0, 0.1); } div.XxX2o input.XxXLZ { -webkit-appearance: slider-horizontal; } div.XxX2o textarea.XxXnf { display: block; box-sizing: border-box; white-space: pre-wrap; background-color: rgba(255, 255, 255, 0.5); width: 100%; margin: 0; padding: 1ex 0.5em; border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; word-break: normal; resize: none; align-self: stretch; line-height: 1.4; font-family: "
            + defaultfont()
            + "; font-size: "
            + defaultfontsizepx()
            + "; -webkit-appearance: none; -webkit-overflow-scrolling: touch; overflow: auto; } div.XxX2o textarea.XxXnf.XxXOY { min-height: 20ex; height: 20ex; } div.XxX2o textarea.XxXnf.XxX4k { min-height: 10ex; height: 10ex; } div.XxX2o textarea.XxXtY { word-break: break-all; } div.XxXoy { display: flex; justify-content: flex-end; align-items: center; white-space: nowrap; box-sizing: border-box; margin: 0; padding: 0; border: none; outline: none; text-align: center; vertical-align: middle; overflow: hidden; background-color: rgba(0, 0, 0, 0.1); width: 100%; border-image-width: 0; font-size: "
            + toolbarfontsizepx()
            + "; color: #444; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; -webkit-user-select: none; user-select: none; } div.XxXoy div.XxXSM { display: inline-block; font-weight: bold; text-align: left; flex: 1 1 0%; -webkit-overflow-scrolling: touch; overflow: auto; font-size: 1.1rem; } div.XxXoy div[xxx-i5] { display: inline-block; text-align: center; flex: 0 1 "
            + buttonsizepx()
            + "; } div.XxX2o div.XxXoy { margin-bottom: 5px; background-color: transparent; } table.XxXLQ { position: absolute; left: 0; top: 0; background-color: #fff; -webkit-user-select: none; user-select: none; } table.XxXLQ div.XxXoy { background-color: rgba(240, 240, 240, 1.0); padding: 0px 10px; border-bottom: 1px solid rgba(0, 0, 0, 0.5); height: "
            + buttonsizep1px()
            + "; color: #444; } div.XxXtu { color: #f00; } div.XxXBn { background-color: rgba(0, 0, 0, 0.1); border-radius: 3px; } div.XxXp2 div.XxXBn { background-color: rgba(0, 0, 0, 0.1); } .XxX1G { position: relative; border: none; box-sizing: border-box; overflow: auto; top: 0; left: 0; margin: 0; padding: 0; background-color: #fff; width: 100vw; height: calc(100vh - "
            + buttonsizep1px()
            + "); } div.XxX9I { z-index: auto; -webkit-overflow-scrolling: touch; overflow: auto; } div.XxXb0 { width: -webkit-fill-available; padding-bottom: 75vh; min-width: fit-content; } div.XxXna { display: flex; flex-flow: row wrap; align-items: center; box-sizing: border-box; width: 100%; } div.XxXna > div { flex: 1 1 10%; align-self: stretch; min-height: "
            + buttonsize60px()
            + "; } div.XxXot, td.XxXot { height: 5px; } div.XxXXV, td.XxXXV { height: 10px; } .XxXTX { margin-top: 5px; } "
            + anspin()
            + " div.XxXV9 { color: #fff; font-size: 2rem; padding-bottom: 50vh; text-shadow: 0 0 2px #000; text-decoration: blink; flex: 0 0 auto; margin: auto; } .XxXMP { cursor: pointer; } .XxX1P { display: flex; align-items: center; box-sizing: border-box; width: 100%; } .XxXAl { display: flex; flex-flow: row nowrap; align-items: stretch; box-sizing: border-box; width: 100%; } .XxX6m { display: flex; flex-flow: column nowrap; align-items: center; box-sizing: border-box; width: 100%; } .XxXE3 { display: flex; align-items: stretch; box-sizing: border-box; width: 100%; } .XxXKi { display: flex; align-items: baseline; box-sizing: border-box; width: 100%; } .XxXjT { display: flex; flex-direction: column; align-items: flex-start; box-sizing: border-box; height: 100%; } .XxXBA { align-self: stretch; text-align: center; vertical-align: middle; font-size: "
            + symbolfontsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; flex: 0 0 "
            + buttonsizepx()
            + "; } .XxXfk { align-self: stretch; flex: 0 0 "
            + buttonsizepx()
            + "; } .XxXrg { flex: 1 1 auto; } .XxXZA { flex: 0 1 auto; text-align: center; } .XxXUL { color: #777; } .XxXcU { font-weight: bold; color: #1e90ff; } .XxX8U { border-bottom: 0.5px solid; } .XxXb9 { border: 1px solid rgba(0, 0, 0, 0.5); border-radius: 3px; } .XxX9V { margin: 5px 0px; } .XxXyq { margin: 0px 5px; } .XxXj6 { margin-left: 5px; } .XxXUC { margin-left: 10px; } .XxXb3 { margin-top: 5px; } .XxX22 { white-space: nowrap; } .XxXTR { -webkit-overflow-scrolling: touch; overflow: auto; } .XxXca { float: right; clear: both; font-size: 1rem; color: #888; } .XxX2b .XxXca { margin-top: 1ex; font-size: 75%; } .XxXUN .XxXca { color: rgba(255, 255, 255, 0.75); } ul.XxXrh { margin: 0; padding: 0; } ul.XxXrh > li, li.XxXrh { margin: 0; padding: 0; list-style: none; } div.XxXcA { background-color: rgba(255, 255, 255, 0.5); width: -webkit-fill-available; min-width: fit-content; border-bottom: 1px solid rgba(0, 0, 0, 0.5); box-sizing: border-box; } table.XxXcA>tr:nth-child(odd), table.XxXcA>tbody>tr:nth-child(odd), div.XxXcA>div:nth-child(odd), ul.XxXcA>li:nth-child(odd) { background-color: rgba(255, 255, 255, 0.0); } table.XxXcA>tr:nth-child(even), table.XxXcA>tbody>tr:nth-child(even), div.XxXcA>div:nth-child(even), ul.XxXcA>li:nth-child(even) { background-color: rgba(0, 0, 0, 0.04); } div.XxXlR > div.XxXfz, div.XxXcA > div.XxXfz { background-color: rgba(0, 102, 204, 0.1) !important; } table.XxXlR>tr, table.XxXlR>tbody>tr, div.XxXlR>div, ul.XxXlR>li { border-bottom: 1px solid rgba(0, 0, 0, 0.1); } "
)
		}
	}
	class CSSEditorCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("@font-face { font-family: FontAwesome; src: url('../fonts/symbols/fontawesome/FontAwesome.woff2') format('woff2'); } @font-face { font-family: Ruda-Regular; src: url('../fonts/google/ofl/ruda/Ruda-Regular.woff2') format('woff2'); } :root { --x-font-fixed: "
            + fixedfont()
            + "; --x-font-default: "
            + defaultfont()
            + "; --x-font-size-px: "
            + defaultfontsizepx()
            + "; --x-button-size-px: "
            + buttonsizepx()
            + "; } body { margin: 0; } .CodeMirror { font-family: var(--x-font-default); font-size: var(--x-font-size-px); line-height: 1.5; color: black; direction: ltr; } .CodeMirror-lines { padding: 4px 0; } .CodeMirror pre { padding: 0 4px; } .CodeMirror-scrollbar-filler, .CodeMirror-gutter-filler { background-color: white; } .CodeMirror-gutters { white-space: nowrap; border-right: 1px solid #ddd; background-color: #f7f7f7; } .CodeMirror-linenumber { text-align: right; white-space: nowrap; padding: 0 3px 0 5px; min-width: 20px; color: #999; } .CodeMirror-guttermarker { color: black; } .CodeMirror-guttermarker-subtle { color: #999; } .CodeMirror-cursor { border-left: 1px solid black; border-right: none; width: 0; } .CodeMirror div.CodeMirror-secondarycursor { border-left: 1px solid silver; } .cm-fat-cursor .CodeMirror-cursor { width: auto; border: 0 !important; background: #7e7; } .cm-fat-cursor div.CodeMirror-cursors { z-index: 1; } .cm-fat-cursor-mark { background-color: rgba(20, 255, 20, 0.5); animation: blink 1.06s steps(1) infinite; } .cm-animate-fat-cursor { width: auto; border: none; animation: blink 1.06s steps(1) infinite; background-color: #7e7; } @keyframes blink { 0% {} 50% { background-color: transparent; } 100% {} } .cm-tab { display: inline-block; text-decoration: inherit; } .CodeMirror-rulers { position: absolute; left: 0; right: 0; top: -50px; bottom: -20px; overflow: hidden; } .CodeMirror-ruler { position: absolute; border-left: 1px solid #ccc; top: 0; bottom: 0; } .cm-s-default .cm-header { color: blue; } .cm-s-default .cm-quote { color: #090; } .cm-negative { color: #d44; } .cm-positive { color: #292; } .cm-header { font-weight: bold; } .cm-strong { font-weight: bold; } .cm-em { font-style: italic; } .cm-link { text-decoration: underline; } .cm-strikethrough { text-decoration: line-through; } .cm-s-default .cm-keyword-2 { color: #a00; } .cm-s-default .cm-keyword { color: #00b; } .cm-s-default .cm-atom { color: #00b; } .cm-s-default .cm-number { color: #060; } .cm-s-default .cm-def { color: #00f; } .cm-s-default .cm-variable { color: #00a; } .cm-s-default .cm-variable-2 { color: #05f; } .cm-s-default .cm-variable-3, .cm-s-default .cm-type { color: #909; } .cm-s-default .cm-comment { color: #a50; } .cm-s-default .cm-string { color: #05f; } .cm-s-default .cm-string-2 { color: #f50; } .cm-s-default .cm-meta { color: #777; } .cm-s-default .cm-qualifier { color: #60c; } .cm-s-default .cm-builtin { color: #b06; } .cm-s-default .cm-bracket { color: #997; } .cm-s-default .cm-tag { color: #000; } .cm-s-default .cm-attribute { color: #00f; } .cm-s-default .cm-hr { color: #999; } .cm-s-default .cm-link { color: #00f; } .cm-s-default .cm-error { color: #f00; } .cm-invalidchar { color: #f00; } .CodeMirror-composing { border-bottom: 2px solid; } div.CodeMirror span.CodeMirror-matchingbracket { color: #0b0; } div.CodeMirror span.CodeMirror-nonmatchingbracket { color: #a22; } .CodeMirror-matchingtag { background: rgba(255, 150, 0, .3); } .CodeMirror-activeline-background { background: #e8f2ff; } .CodeMirror { position: relative; overflow: hidden; border: none; background: white; } .CodeMirror-scroll { -webkit-overflow-scrolling: touch; overflow: scroll !important; width: 100vw; height: 100vh; border: none; outline: none; position: relative; } .CodeMirror-sizer { position: relative; padding-bottom: 75vh !important; border-right: 0; } .CodeMirror-vscrollbar, .CodeMirror-hscrollbar, .CodeMirror-scrollbar-filler, .CodeMirror-gutter-filler { position: absolute; z-index: 6; display: none; } .CodeMirror-vscrollbar { right: 0; top: 0; overflow-x: hidden; overflow-y: scroll; } .CodeMirror-hscrollbar { bottom: 0; left: 0; overflow-y: hidden; overflow-x: scroll; } .CodeMirror-scrollbar-filler { right: 0; bottom: 0; } .CodeMirror-gutter-filler { left: 0; bottom: 0; } .CodeMirror-gutters { position: absolute; left: 0; top: 0; min-height: 100%; z-index: 3; } .CodeMirror-gutter { white-space: normal; height: 100%; display: inline-block; vertical-align: top; margin-bottom: -30px; } .CodeMirror-gutter-wrapper { position: absolute; z-index: 4; background: none !important; border: none !important; } .CodeMirror-gutter-background { position: absolute; top: 0; bottom: 0; z-index: 4; } .CodeMirror-gutter-elt { position: absolute; cursor: default; z-index: 4; } .CodeMirror-gutter-wrapper ::selection { background-color: transparent; } .CodeMirror-lines { cursor: text; min-height: 1px; } .CodeMirror pre { border-width: 0; background: transparent; font-family: inherit; font-size: inherit; margin: 0; white-space: pre; word-wrap: normal; line-height: inherit; color: inherit; z-index: 2; position: relative; overflow: visible; -webkit-tap-highlight-color: transparent; font-variant-ligatures: contextual; } .CodeMirror-wrap pre { white-space: pre-wrap; word-break: normal; word-wrap: break-word; } .CodeMirror-linebackground { position: absolute; left: 0; right: 0; bottom: 0; z-index: 0; } .CodeMirror-linewidget { position: relative; z-index: 2; padding: 0.1px; } .CodeMirror-widget { } .CodeMirror-rtl pre { direction: rtl; } .CodeMirror-code { -webkit-text-size-adjust: none; outline: none; } .CodeMirror-scroll, .CodeMirror-sizer, .CodeMirror-gutter, .CodeMirror-gutters, .CodeMirror-linenumber { box-sizing: content-box; } .CodeMirror-measure { position: absolute; overflow: hidden; visibility: hidden; width: 100%; height: 0; } .CodeMirror-cursor { position: absolute; pointer-events: none; } .CodeMirror-measure pre { position: static; } div.CodeMirror-cursors { visibility: hidden; position: relative; z-index: 3; } div.CodeMirror-dragcursors { visibility: visible; } .CodeMirror-focused div.CodeMirror-cursors { visibility: visible; } .CodeMirror-selected { background: #d9d9d9; } .CodeMirror-focused .CodeMirror-selected { background: #d7d4f0; } .CodeMirror-crosshair { cursor: crosshair; } .CodeMirror-line::selection, .CodeMirror-line > span::selection, .CodeMirror-line > span > span::selection { background: #d7d4f0; } .cm-searching { background-color: #ffa; background-color: rgba(255, 255, 0, .4); } .cm-force-border { padding-right: .1 px; } @media  print { .CodeMirror div.CodeMirror-cursors { visibility: hidden; } } .cm-tab-wrap-hack:after { content: ; } span.CodeMirror-selectedtext { background: none; } .CodeMirror-hints { position: absolute; overflow: hidden; margin: 0; list-style: none; z-index: 10; padding: 2px; box-shadow: 2px 3px 5px rgba(0,0,0,.2); border-radius: 3px; border: 1px solid silver; background: white; font-family: var(--x-font-default); font-size: var(--x-font-size-px); line-height: 2.0; max-width: 240px; max-height: 20em; -webkit-overflow-scrolling: touch; overflow-y: auto; } .CodeMirror-hint { margin: 0; white-space: pre; cursor: pointer; padding: 0 4px; border-radius: 2px; color: black; } .CodeMirror-hints > li:nth-child(odd) { border-bottom: 1px solild rgba(0, 0, 0, 0.1); background-color: rgba(255, 255, 255, 0.0); } .CodeMirror-hints > li:nth-child(even) { border-bottom: 1px solild rgba(0, 0, 0, 0.1); background-color: rgba(0, 0, 0, 0.04); } .CodeMirror-hints > li.CodeMirror-hint-active { background: #08f; color: white; } "
)
		}
	}
	class GameMinesCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("@font-face { font-family: FontAwesome; src: url('../fonts/symbols/fontawesome/FontAwesome.woff2') format('woff2'); } @font-face { font-family: Ruda-Regular; src: url('../fonts/google/ofl/ruda/Ruda-Regular.woff2') format('woff2'); } :root { --xx-xxxnt: "
            + buttonsize75px()
            + "; --xx-xxx8l: calc(var(--xx-xxxnt) / 2); --xx-xxxtg: calc(var(--xx-xxxnt) * 0.4); } html, input, textarea, keygen, select, button { font-family: Ruda-Regular; font-size: "
            + defaultfontsizepx()
            + "; } select { -webkit-appearance: none; padding: 4px; } html, body, iframe, div, p, span, ul, ol, table, tbody, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } table, tbody { border-spacing: 0; } body { margin: 0; font-family: Ruda-Regular; } div.XxXPO { display: flex; align-items: center; flex-flow: column nowrap; } div.XxXJL { display: flex; align-items: center; flex-flow: row nowrap; } div.XxXnw { display: none; position: absolute; background-color: #f8f8e8; box-sizing: border-box; left: 0; top: 0; } table.XxXnw { table-layout: fixed; box-sizing: border-box; padding: 10px; border: 1px solid rgba(0, 0, 0, 0.4); border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; overflow: hidden; } div.XxXgg { display: flex; justify-content: flex-end; white-space: nowrap; box-sizing: border-box; width: 100%; margin: 0; padding: 0; border: none; outline: none; text-align: center; vertical-align: middle; border-image-width: 0; font-size: "
            + toolbarfontsizepx()
            + "; color: #444; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } .XxXyV { display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; border: 1px solid transparent; cursor: pointer; background-size: cover; width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; font-size: "
            + symbolfontsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } .XxXyV.XxX5b { opacity: 0.4 !important; cursor: not-allowed !important; } .XxXGo { text-rendering: auto; -webkit-font-smoothing: antialiased; font-family: FontAwesome; } .XxXyV.XxX2a { background-color: #f0f0f0 !important; border-radius: "
            + buttonsize50px()
            + "; border: 1px solid #bbb !important; } .XxXcV { background-color: #ccc !important; } .XxXyV.XxXcV { border-radius: "
            + buttonsize50px()
            + "; } div.XxXYu { position: absolute; margin: 0; padding: 0; border: none; outline: none; left: 0; top: 0; width: 100vw; height: 100vh; background-color: #000; } "
            + anspin()
            + " div.XxX4o { flex: 0 0 auto; margin: 0; color: #fff; animation: XxXdn 10s infinite linear; -webkit-animation: XxXdn 10s infinite linear; } div.XxX4o:before { content: \"\\f110\"; font-family: FontAwesome; font-size: 80px; transform-origin: 50% 50%; transform: rotate(0deg); } .XxXvG { width: 100vw; height: 100vh; background-image: url(../images/wallpapers/cloth01.png); box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; } .XxXzF { width: min-content; margin: 0 auto; padding: var(--xx-xxx8l); } .XxXZ9 { display: flex; box-sizing: border-box; align-items: center; justify-content: center; text-align: center; padding-top: var(--xx-xxxtg); font-size: var(--xx-xxx8l); } .XxXd5 { display: flex; box-sizing: border-box; align-items: center; justify-content: center; text-align: center; padding-top: 0.5ex; font-size: var(--xx-xxxtg); color: #444; } .XxXwm { background-color: #fff; border: 1px solid #444; width: min-content; } .XxXam { display: flex; width: min-content; } .XxXMu { display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; border: 0.5px solid #ccc; font-size: var(--xx-xxxtg); width: var(--xx-xxxnt); height: var(--xx-xxxnt); } .XxXMu.XxXxN { background-color: #ddd; } .XxXMu.XxX2a { background-color: #fea; } .XxXMu.XxXxN.XxXcq { background-color: red; } .XxXJE.XxXiQ { background-color: orange; } .XxXJE:before { font-family: FontAwesome; content: \"\\f024\"; } .XxXcq:before { font-family: FontAwesome; content: \"\\f1e2\"; } "
)
		}
	}
	class GameSudokuCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("@font-face { font-family: FontAwesome; src: url('../fonts/symbols/fontawesome/FontAwesome.woff2') format('woff2'); } @font-face { font-family: Ruda-Regular; src: url('../fonts/google/ofl/ruda/Ruda-Regular.woff2') format('woff2'); } :root { --xx-xxxnt: calc("
            + buttonsizepx()
            + "); --xx-xxx8l: calc(var(--xx-xxxnt) / 2); --xx-xxxtg: calc(var(--xx-xxxnt) * 0.4); } html, input, textarea, keygen, select, button { font-family: Ruda-Regular; font-size: "
            + defaultfontsizepx()
            + "; } select { -webkit-appearance: none; padding: 4px; } html, body, iframe, div, p, span, ul, ol, table, tbody, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } table, tbody { border-spacing: 0; } body { margin: 0; font-family: Ruda-Regular; } div.XxXPO { display: flex; align-items: center; flex-flow: column nowrap; } div.XxXJL { display: flex; align-items: center; flex-flow: row nowrap; } div.XxXnw { display: none; position: absolute; background-color: #f8f8e8; box-sizing: border-box; left: 0; top: 0; } table.XxXnw { table-layout: fixed; box-sizing: border-box; padding: 10px; border: 1px solid rgba(0, 0, 0, 0.4); border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; overflow: hidden; } div.XxXgg { display: flex; justify-content: flex-end; white-space: nowrap; box-sizing: border-box; width: 100%; margin: 0; padding: 0; border: none; outline: none; text-align: center; vertical-align: middle; border-image-width: 0; font-size: "
            + toolbarfontsizepx()
            + "; color: #444; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } .XxXyV { display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; border: 1px solid transparent; cursor: pointer; background-size: cover; width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; font-size: "
            + symbolfontsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } .XxXyV.XxX5b { opacity: 0.4 !important; cursor: not-allowed !important; } .XxXGo { text-rendering: auto; -webkit-font-smoothing: antialiased; font-family: FontAwesome; } .XxXyV.XxX2a { background-color: #f0f0f0 !important; border-radius: "
            + buttonsize50px()
            + "; border: 1px solid #bbb !important; } .XxXcV { background-color: #ccc !important; } .XxXyV.XxXcV { border-radius: "
            + buttonsize50px()
            + "; } div.XxXYu { position: absolute; margin: 0; padding: 0; border: none; outline: none; left: 0; top: 0; width: 100vw; height: 100vh; background-color: #000; } "
            + anspin()
            + " div.XxX4o { flex: 0 0 auto; margin: 0; color: #fff; animation: XxXdn 10s infinite linear; -webkit-animation: XxXdn 10s infinite linear; } div.XxX4o:before { content: \"\\f110\"; font-family: FontAwesome; font-size: 80px; transform-origin: 50% 50%; transform: rotate(0deg); } .XxXvG { width: 100vw; height: 100vh; background-image: url(../images/wallpapers/cloth01.png); box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; } .XxXzF { display: flex; flex-flow: column nowrap; width: min-content; margin: 0 auto; padding: var(--xx-xxx8l) 0; } .XxXwm { display: flex; flex-flow: column nowrap; background-color: #fff; width: min-content; border: 2px solid #444; border-bottom-width: 1px; border-right-width: 1px; } .XxXwm>div:nth-child(3n) { border-bottom: 1px solid #444; } .XxXam { display: flex; flex: 1 1 auto; width: min-content; } .XxXam>div:nth-child(3n) { border-right: 1px solid #444; } .XxXMu { display: flex; flex-flow: row nowrap; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; overflow: hidden; font-weight: bold; flex: 1 1 auto; word-break: break-all; border: 0.5px solid #ddd; font-size: var(--xx-xxxtg); color: #000; padding: 2px; width: var(--xx-xxxnt); height: var(--xx-xxxnt); } .XxXMu.XxXxK { color: #000; background-color: #eee; } .XxXMu.XxXiQ { color: #c00; } .XxXMu.XxXJE { font-weight: normal; font-size: calc(var(--xx-xxxtg) * 0.8); color: #999; } .XxXMu.XxXJE.XxXqA { font-weight: bold; } .XxXMu.XxXpz { background-color: #ffc; color: #999; } .XxXZ9 { display: flex; flex-flow: column nowrap; box-sizing: border-box; align-items: center; justify-content: center; text-align: center; font-size: var(--xx-xxx8l); } .XxXd5 { display: flex; box-sizing: border-box; align-items: center; justify-content: center; text-align: center; font-size: var(--xx-xxxtg); color: #444; } .XxXsh { color: #03f; } .XxX0B { color: forestgreen; } "
)
		}
	}
	class GameMindsCSS(conf: IConf): BuilderBase(conf) {
		fun build(): String {
			return ("@font-face { font-family: FontAwesome; src: url('../fonts/symbols/fontawesome/FontAwesome.woff2') format('woff2'); } @font-face { font-family: Ruda-Regular; src: url('../fonts/google/ofl/ruda/Ruda-Regular.woff2') format('woff2'); } :root { --xx-xxxnt: calc("
            + buttonsize75px()
            + "); --xx-xxx8l: calc(var(--xx-xxxnt) / 2); --xx-xxxtg: calc(var(--xx-xxxnt) * 0.4); --xx-xxxmx: calc(var(--xx-xxxnt) / 4); } html, input, textarea, keygen, select, button { font-family: Ruda-Regular; font-size: "
            + defaultfontsizepx()
            + "; } select { -webkit-appearance: none; padding: 4px; } html, body, iframe, div, p, span, ul, ol, table, tbody, tr, td { margin: 0; padding: 0; border: none; border-image-width: 0; outline: none; } table, tbody { border-spacing: 0; } body { margin: 0; font-family: Ruda-Regular; } div.XxXPO { display: flex; align-items: center; flex-flow: column nowrap; } div.XxXJL { display: flex; align-items: center; flex-flow: row nowrap; } div.XxXnw { display: none; position: absolute; background-color: #f8f8e8; box-sizing: border-box; left: 0; top: 0; } table.XxXnw { table-layout: fixed; box-sizing: border-box; padding: 10px; border: 1px solid rgba(0, 0, 0, 0.4); border-radius: 5px; box-shadow: "
            + boxshadow()
            + "; overflow: hidden; } div.XxXgg { display: flex; justify-content: flex-end; white-space: nowrap; box-sizing: border-box; width: 100%; margin: 0; padding: 0; border: none; outline: none; text-align: center; vertical-align: middle; border-image-width: 0; font-size: "
            + toolbarfontsizepx()
            + "; color: #444; height: "
            + buttonsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; -webkit-overflow-scrolling: touch; overflow: auto; -webkit-user-select: none; user-select: none; } .XxXyV { display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; border: 1px solid transparent; cursor: pointer; background-size: cover; width: "
            + buttonsizepx()
            + "; height: "
            + buttonsizepx()
            + "; font-size: "
            + symbolfontsizepx()
            + "; line-height: "
            + buttonsizepx()
            + "; } .XxXyV.XxX5b { opacity: 0.4 !important; cursor: not-allowed !important; } .XxXGo { text-rendering: auto; -webkit-font-smoothing: antialiased; font-family: FontAwesome; } .XxXyV.XxX2a { background-color: #f0f0f0 !important; border-radius: "
            + buttonsize50px()
            + "; border: 1px solid #bbb !important; } .XxXcV { background-color: #ccc !important; } .XxXyV.XxXcV { border-radius: "
            + buttonsize50px()
            + "; } div.XxXYu { position: absolute; margin: 0; padding: 0; border: none; outline: none; left: 0; top: 0; width: 100vw; height: 100vh; background-color: #000; } "
            + anspin()
            + " div.XxX4o { flex: 0 0 auto; margin: 0; color: #fff; animation: XxXdn 10s infinite linear; -webkit-animation: XxXdn 10s infinite linear; } div.XxX4o:before { content: \"\\f110\"; font-family: FontAwesome; font-size: 80px; transform-origin: 50% 50%; transform: rotate(0deg); } .XxXvG { width: 100vw; height: 100vh; background-image: url(../images/wallpapers/cloth01.png); box-sizing: border-box; -webkit-overflow-scrolling: touch; overflow: auto; } .XxXzF { width: min-content; margin: 0 auto; padding: var(--xx-xxxnt) var(--xx-xxx8l) 75vh var(--xx-xxx8l); } .XxXZ9 { display: flex; box-sizing: border-box; align-items: center; justify-content: center; text-align: center; padding-top: var(--xx-xxxtg); font-size: var(--xx-xxx8l); } .XxXwm { background-color: #fff; padding: var(--xx-xxx8l); border: 1px solid #444; border-radius: var(--xx-xxxmx); width: min-content; } .XxXam { display: flex; width: min-content; } .XxXMu { display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; margin: 2px; border: 1px solid #ccc; border-radius: 5px; background-color: #fff; font-size: var(--xx-xxx8l); width: var(--xx-xxxnt); height: var(--xx-xxxnt); } .XxXMu.XxX2a { background-color: #fea; } .XxX5b .XxXMu { background-color: #cd0; } .XxX5b .XxXMu.XxXxK, .XxXMu.XxXxK { font-weight: bold; color: #fff; background-color: #8a0; } .XxXJE { display: flex; align-items: center; justify-content: flex-end; width: calc(var(--xx-xxxnt) * 2.75); } .XxXcq { display: flex; align-items: center; justify-content: center; font-weight: bold; width: var(--xx-xxxnt); height: var(--xx-xxxnt); margin: 2px; font-size: var(--xx-xxx8l); border-radius: var(--xx-xxx8l); color: #fff; background-color: #000; } .XxXxN { display: flex; align-items: center; justify-content: center; box-sizing: border-box; font-weight: bold; width: var(--xx-xxxnt); height: var(--xx-xxxnt); margin: 2px; font-size: var(--xx-xxx8l); border-radius: var(--xx-xxx8l); border: 1px solid #ccc; color: #000; background-color: #fff; } "
)
		}
	}
}
