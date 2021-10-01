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
package sf.andrians.cplusedition.support

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.util.text.TextUtil
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.handler.IResUtil
import java.io.File
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

object Support {
    private val datetimeFormatter = DateFormat.getDateTimeInstance()
    private val SEPA = File.separator.toCharArray()
    val LOCALE = Locale.US
    fun log(msg: String?) {
        //#IF ENABLE_LOG
        println(msg)
        //#ENDIF ENABLE_LOG
    }

    fun d(msg: String?) {
        //#IF ENABLE_LOG
        println(msg)
        //#ENDIF ENABLE_LOG
    }

    fun d(msg: String?, e: Throwable?) {
        //#IF ENABLE_LOG
        println(msg)
        e?.printStackTrace(System.out)
        //#ENDIF ENABLE_LOG
    }

    fun assertion(ok: Boolean) {
        if (!ok) {
            throw AssertionError()
        }
    }

    fun assertion(ok: Boolean, msg: String?) {
        if (!ok) {
            throw AssertionError(msg)
        }
    }

    fun joinStyles(styles: JSONArray): String {
        val b = StringBuilder()
        var i = 0
        val len = styles.length()
        while (i < len) {
            val namevalue = styles.optJSONArray(i)
            val key = namevalue.stringOrNull(0)
            if (key != null) {
                val value = namevalue.stringOrDef(1, key)
                b.append(key).append(':').append(value).append(';')
            }
            ++i
        }
        return b.toString()
    }

    fun updateSettings(res: IResUtil, current: JSONObject, update: JSONObject): List<String> {
        val errors = ArrayList<String>()
        val it = update.keys()
        while (it.hasNext()) {
            val key = it.next()
            try {
                when (key) {
                    An.SettingsKey.uiFontName,
                    An.SettingsKey.uiFontStyle,
                    An.SettingsKey.fixedFontName,
                    An.SettingsKey.fixedFontStyle,
                    An.SettingsKey.dateFormat,
                    An.SettingsKey.timeFormat,
                    An.SettingsKey.dialogBGColor,
                    An.SettingsKey.headingColor,
                    An.SettingsKey.linkColor,
                    An.SettingsKey.annotationColor,
                    An.SettingsKey.highlightColor
                    -> {
                        val value = update.stringOrNull(key)
                        if (value != null) {
                            current.put(key, value)
                        }
                    }
                    An.SettingsKey.buttonSize,
                    An.SettingsKey.uiFontSize,
                    An.SettingsKey.imageDimension
                    -> {
                        val value = update.optInt(key, -1)
                        if (value >= 0) {
                            current.put(key, value)
                        }
                    }
                    else -> {
                        val msg = res.format(R.string.invalidNameValue, key, update.opt(key))
                        
                        errors.add(msg)
                    }
                }
            } catch (e: Exception) {
                val msg = res.get(R.string.ErrorUpdatingSettings, key, "${update.opt(key)}")
                
                errors.add(msg)
            }
        }
        return errors
    }

    fun safeUrlPath(path: String): String {
        return path
    }

    /**
     * Esc. [] in query which is illegal in Android Uri.parse().
     */
    fun fixUriEsc(part: String?): String? {
        if (part == null) {
            return null
        }
        val len = part.length
        if (len == 0) {
            return part
        }
        val b = StringBuilder()
        var start = -1
        for (i in 0 until len) {
            val c = part[i]
            if (c == '[' || c == ']') {
                if (start >= 0) {
                    b.append(part, start, i)
                    start = -1
                }
                b.append('%')
                b.append(Integer.toHexString(c.toInt()))
            } else if (start < 0) {
                start = i
            }
        }
        if (start == 0) {
            return part
        }
        if (start > 0) {
            b.append(part, start, len)
        }
        return b.toString()
    }

    /**
     * @param path A context relative path, that may or may not starts with '/'.
     * @return The relative path to get from path to the context root, eg. for /Documents/index.html returns "../" index.html
     * returns "./" /Documents/manual/test.html?mime=text/html return "../../"
     */
    fun toContext(path: String?): String? {
        var path = path ?: return null
        val index = path.indexOf('?')
        if (index >= 0) {
            path = path.substring(0, index)
        }
        if (path.length == 0) {
            return "./"
        }
        val segments = TextUtil.split(path, "/")
        val n = segments.size - 1
        if (n <= 0) {
            return "./"
        }
        val ret = StringBuilder()
        for (i in 0 until n) {
            ret.append("../")
        }
        return ret.toString()
    }

    /**
     * Remove redundant ../, ./ and duplicated / WITHOUT sanitizing the filepath
     *
     * @param path A context path with or without leading /.
     * @return A context path without leading and trailing /, returns null if path is not valid, eg. starts with ../.
     */
    fun getcleanrpath(path: String?): String? {
        if (path == null) return null
        val cleanpath = TextUtil.trim(SEPA, TextUtil.cleanupFilePath(path).toString())
        return if (cleanpath.startsWith("..${File.separatorChar}") || cleanpath == "..") null else cleanpath
    }

    /**
     * Remove redundant ../, ./ and duplicated / and sanitize the filepath
     *
     * @param path A context path with or without leading /.
     * @return A context path without leading / or null on error
     */
    fun getcleanrpathStrict(res: IResUtil, path: String?): Pair<String?, Collection<String>> {
        val errors = TreeSet<String>()
        return Pair(getcleanrpathStrict(errors, res, path), errors)
    }

    fun getcleanrpathStrict(errors: MutableCollection<String>, res: IResUtil, path: String?): String? {
        val cleanpath = getcleanrpath(path)
        if (cleanpath == null) {
            errors.add(res.get(R.string.InvalidPath_, "$path"))
            return null
        }
        if (cleanpath.isNotEmpty()) {
            sanitizeFilepathStrict0(errors, res, cleanpath)
            if (errors.isNotEmpty()) {
                return null
            }
        }
        return cleanpath
    }

    ///# NOTE \\ is made invalid to avoid the escape hell.
    /// ?#%;[] are made invalid because Tomcat do not decode them in request.getPathInfo().
    private const val ValidFilenameChars = " !\"$&'()*+,-./:<=>@^_`{|}~" // Invalid \\?#%;[]
    private val InvalidFilenameChars = "/\\?#%;[]".codePoints().toArray()

    private fun sanitizeFilename1(illegals: MutableCollection<Int>, filename: String, len: Int) {
        filename.codePoints().forEach {
            if (InvalidFilenameChars.contains(it) || !Character.isDefined(it)) {
                illegals.add(it)
            } else {
                when (Character.getType(it).toByte()) {
                    Character.UPPERCASE_LETTER,
                    Character.LOWERCASE_LETTER,
                    Character.TITLECASE_LETTER,
                    Character.MODIFIER_LETTER,
                    Character.OTHER_LETTER,
                    Character.NON_SPACING_MARK,
                    Character.ENCLOSING_MARK,
                    Character.COMBINING_SPACING_MARK,
                    Character.DECIMAL_DIGIT_NUMBER,
                    Character.LETTER_NUMBER,
                    Character.OTHER_NUMBER,
                    Character.SPACE_SEPARATOR,
                    Character.DASH_PUNCTUATION,
                    Character.START_PUNCTUATION,
                    Character.END_PUNCTUATION,
                    Character.CONNECTOR_PUNCTUATION,
                    Character.OTHER_PUNCTUATION,
                    Character.MATH_SYMBOL,
                    Character.CURRENCY_SYMBOL,
                    Character.MODIFIER_SYMBOL,
                    Character.OTHER_SYMBOL,
                    Character.INITIAL_QUOTE_PUNCTUATION,
                    Character.FINAL_QUOTE_PUNCTUATION
                    -> Unit
                    else -> {
                        illegals.add(it)
                    }
                }
            }
        }
    }

    /**
     * Escape the given illegal filename characters as printable unicode escape for presentation.
     */
    fun escIllegals(illegals: Collection<Int>): String {
        val b = StringBuilder()
        for (c in illegals) {
            if (c >= 0x20 && c < 0x7f && c != '\\'.toInt()) {
                b.append(c.toChar())
            } else {
                b.append(TextUtil.format("{0x%04x}", c))
            }
        }
        return b.toString()
    }

    fun <T : MutableCollection<String>> sanitizeFilepath(errors: T, res: IResUtil, apath: String?): T {
        return sanitizeFilepath(errors, res, TextUtil.split(StringTokenizer(apath, "/")))
    }

    fun <T : MutableCollection<String>> sanitizeFilepath(errors: T, res: IResUtil, segments: Collection<String?>): T {
        val illegals: MutableSet<Int> = TreeSet()
        for (segment in segments) {
            sanitizeFilenameStrict0(errors, res, illegals, segment)
        }
        if (illegals.size > 0) {
            errors.add(res.get(R.string.InvalidFilenameCharacter_) + escIllegals(illegals))
        }
        return errors
    }

    /**
     * Similar to sanitizeFilepathStrict1, but allow empty filename.
     */
    fun <T : MutableCollection<String>> sanitizeFilepathStrict0(errors: T, res: IResUtil, apath: String) {
        sanitizeFilepathStrict0(errors, res, TextUtil.split(StringTokenizer(apath, "/")))
    }

    /**
     * Similar to sanitizeFilepathStrict1, but allow empty filename.
     */
    fun <T : MutableCollection<String>> sanitizeFilepathStrict0(errors: T, res: IResUtil, segments: Collection<String>): T {
        val illegals = TreeSet<Int>()
        for (segment in segments) {
            sanitizeFilenameStrict0(errors, res, illegals, segment)
        }
        if (illegals.size > 0) {
            errors.add(res.get(R.string.InvalidFilenameCharacter_) + escIllegals(illegals))
        }
        return errors
    }

    /**
     * Similar to sanitizeFilenameStrict1 but don't check on empty filename. Use by getcleanrpathStrict that should has eliminated
     * //, and thus the empty filename only occurs at the end which is OK.
     */
    fun <T : MutableCollection<String>> sanitizeFilenameStrict0(
            errors: T,
            res: IResUtil,
            illegals: MutableCollection<Int>,
            filename: String?
    ) {
        if (filename == null) {
            errors.add(res.get(R.string.FilenameMustNotBeNull))
            return
        }
        val len = filename.length
        if (len == 0) {
            return
        }
        var c = filename[0]
        if (c == '.' && len != 1 && filename != "..") {
            errors.add(res.get(R.string.FilenameMustNotStartsWithDot))
        }
        if (Character.isSpaceChar(c)) {
            errors.add(res.get(R.string.FilenameMustNotStartsWithSpaces))
        }
        c = filename[len - 1]
        if (Character.isSpaceChar(c)) {
            errors.add(res.get(R.string.FilenameMustNotEndsWithSpaces))
        }
        sanitizeFilename1(illegals, filename, len)
    }

    /**
     * Similar to sanitizeFilepath, but also check that filename do not start with . and spaces and do not end with spaces.
     */
    fun sanitizeFilepathStrict(res: IResUtil, apath: String): Boolean {
        return sanitizeFilepathStrict(ArrayList(), res, apath).isEmpty()
    }

    /**
     * Similar to sanitizeFilepath, but also check that filename do not start with . and spaces and do not end with spaces.
     */
    fun <T : MutableCollection<String>> sanitizeFilepathStrict(errors: T, res: IResUtil, apath: String): T {
        return sanitizeFilepathStrict(errors, res, TextUtil.split(StringTokenizer(apath, "/")))
    }

    fun <T : MutableCollection<String>> sanitizeFilepathStrict(errors: T, res: IResUtil, segments: Collection<String>): T {
        val illegals: MutableSet<Int> = TreeSet()
        for (segment in segments) {
            sanitizeFilenameStrict1(errors, res, illegals, segment)
        }
        if (illegals.size > 0) {
            errors.add(res.get(R.string.InvalidFilenameCharacter_) + escIllegals(illegals))
        }
        return errors
    }

    fun <T : MutableCollection<String>> sanitizeFilenameStrict(errors: T, res: IResUtil, filename: String): T {
        val illegals: MutableSet<Int> = TreeSet()
        sanitizeFilenameStrict1(errors, res, illegals, filename)
        if (illegals.size > 0) {
            errors.add(res.get(R.string.InvalidFilenameCharacter_) + escIllegals(illegals))
        }
        return errors
    }

    /**
     * Also check that filename is not empty.
     */
    fun <T : MutableCollection<String>> sanitizeFilenameStrict1(
            errors: T, res: IResUtil, illegals: MutableCollection<Int>, filename: String?
    ) {
        sanitizeFilenameStrict0(errors, res, illegals, filename)
        if (filename != null && filename.isEmpty()) {
            errors.add(res.get(R.string.FilenameMustNotBeEmpty))
        }
    }

    fun datetime(ms: Long): String {
        return datetimeFormatter.format(Date(ms))
    }

    object Def {
        const val recentsSize = 24
        const val UnexpectedException = "Unexpected exception"
        const val maxInputImageArea = 32 * 1024 * 1024
        const val thumbnailMiniHeight = 384
        const val thumbnailMiniWidth = 512
        const val loginFailureTimeout: Long = 5000
        const val memoryInfoInterval: Long = 3000
        const val scaleImageTimeout: Long = 10000
        const val thumbnailTimeout: Long = 5000
    }

    object SettingsDefs {
        //#BEGIN SHUFFLE
        val symbolFamily = "FontAwesome"
        val buttonSize = 40
        val annotationColor = "#9400d3"
        val fixedFontStyle = "Regular"
        val dateFormat = "mm/dd/yyyy"
        val highlightColor = "rgba(255, 255, 128, 0.75)"
        val winHeight = 640
        val winWidth = 360
        val dialogBGColor = "rgba(255, 255, 255, 0.80)"
        val uiFontName = "Ruda"
        val timeFormat = "hh:mm"
        val uiFontStyle = "Regular"
        val linkColor = "#0000cc"
        val imageDimension = 1024
        val fixedFontName = "UbuntuMono"
        val dpi = 160
        val headingColor = "#1e90ff"
        val fontSize = 16.0
        //#END SHUFFLE
    }

    class JSONArrayStringValueIterables(private val array: JSONArray) : Iterable<String>, MutableIterator<String> {
        private var index = 0
        private val length = array.length()
        private var next: String? = null

        override fun iterator(): Iterator<String> {
            return this
        }

        override fun hasNext(): Boolean {
            do {
                next = array.stringOrNull(index++)
            } while (next == null && index < length)
            return next != null
        }

        override fun next(): String {
            return next!!
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

        companion object {
            fun toList(array: JSONArray): List<String> {
                val ret = ArrayList<String>()
                for (value in JSONArrayStringValueIterables(array)) {
                    ret.add(value)
                }
                return ret
            }
        }
    }

    object EffectUtil {
        val Name = arrayOf(
                "",
                "Gray (4 levels)", "Black and white")

        fun toString(effect: Int): String {
            return if (effect >= 0 && effect < Name.size) Name[effect] else Name[An.Effect.NONE]
        }
    }

    object AttrUtil {
        var canedit: MutableSet<String> = TreeSet()
        fun canEdit(attr: String?): Boolean {
            return canedit.contains(attr)
        }

        init {
            canedit.add(An.ATTR.xAnnotation)
            canedit.add(An.ATTR.xButton)
            canedit.add(An.ATTR.xPageTemplate)
            canedit.add(An.ATTR.xPlaceholder)
            canedit.add(An.ATTR.xTemplate)
            canedit.add(An.ATTR.xTemplatePlaceholder)
            canedit.add(An.ATTR.xFormat)
            canedit.add(An.ATTR.xInfo)
            canedit.add(An.ATTR.xTooltips)
        }
    }

    object PathUtil {
        private val assets_ = arrayOf(
                An.PATH._assets_,
                An.PATH.assets_
        )
        private val assets = arrayOf(
                An.PATH._assets,
                An.PATH.assets
        )
        private val homePrivate_ = arrayOf(
                An.PATH._Home_,
                An.PATH.Home_
        )
        private val homePrivate = arrayOf(
                An.PATH._Home,
                An.PATH.Home
        )

        fun isAssetsSubtree(cpath: String?): Boolean {
            return cpath != null && _isUnder(cpath, assets)
        }

        fun isAssetsTree(cpath: String?): Boolean {
            return cpath != null && (_isUnder(cpath, assets) || _isOneOf(cpath, assets) || _isOneOf(cpath, assets_))
        }

        fun isDocumentsSubtree(cpath: String?): Boolean {
            return cpath != null && _isUnder(cpath, homePrivate_)
        }

        fun isDocumentsTree(cpath: String?): Boolean {
            return (cpath != null
                    && (_isUnder(cpath, homePrivate_)
                    || _isOneOf(cpath, homePrivate_)
                    || _isOneOf(cpath, homePrivate)))
        }

        fun isDocumentsRoot(name: String): Boolean {
            return _isOneOf(name, homePrivate)
        }

        fun isHomeHtml(cpath: String?): Boolean {
            return cpath != null && (An.PATH._HomeIndexHtml == cpath
                    )
        }

        fun getHomeHtml(loggedin: Boolean): String {
            return /* if (loggedin) An.PATH._PrivateIndexHtml else */ An.PATH._HomeIndexHtml
        }

        fun getHome(loggedin: Boolean): String {
            return /* if (loggedin) An.PATH.Private else */ An.PATH.Home
        }

        fun ensureLeadingSlash(path: String?): String? {
            return path?.let { _ensureLeadingSlash1(it) }
        }

        fun ensureTrailingSlash(path: String?): String? {
            if (path == null) {
                return null
            }
            return if (path.endsWith("/")) path else "$path/"
        }

        private fun _ensureLeadingSlash1(path: String): String {
            return if (path.startsWith("/")) {
                path
            } else "/$path"
        }

        fun interpretPath(isloggedin: Boolean, path: String): String {
            if (!path.contains("{")) {
                return path
            }
            val time = System.currentTimeMillis()
            val year = TextUtil.format("%tY", time)
            val month = TextUtil.format("%tm", time)
            val day = TextUtil.format("%td", time)
            val date = TextUtil.format("%1\$tY%1\$tm%1\$td", time)
            val home = getHome(isloggedin)
            return path.replace("{home}", home).replace("{date}", date).replace("{year}", year).replace("{month}", month).replace("{day}", day)
        }

        /**
         * @return true If cpath is under the given roots, but not the root itself.
         */
        private fun _isUnder(cpath: String, roots: Array<String>): Boolean {
            for (root in roots) {
                if (cpath.startsWith(root) && cpath.length > root.length) {
                    return true
                }
            }
            return false
        }

        /**
         * @return true If cpath is one of the given roots.
         */
        private fun _isOneOf(cpath: String, roots: Array<String>): Boolean {
            return roots.contains(cpath)
        }
    }

    object DefaultSettings {
        @Throws(JSONException::class)
        fun defaultSettings(dpi: Int): JSONObject {
            val buttonsize = (dpi / 4f).roundToInt()
            val fontsize = (dpi * 15f / 160f).roundToInt()
            val symbolsize = (dpi / 8f).roundToInt()
            return initdefaults(buttonsize, fontsize, fontsize, symbolsize)
        }

        @Throws(JSONException::class)
        private fun initdefaults(buttonsize: Int, fontsize: Int, fixedfontsize: Int, symbolsize: Int): JSONObject {
            val ret = JSONObject()
            val locale = Locale.getDefault()
            val cal = Calendar.getInstance(locale)
            cal[1970, Calendar.NOVEMBER, 23, 14, 34] = 45
            val d = cal.time
            val date = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(d)
            val time = DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(d)
            var dateformat: String = SettingsDefs.dateFormat
            if (date.startsWith("11/23/")) {
                dateformat = "mm/dd/yyyy"
            } else if (date.startsWith("23/11/")) {
                dateformat = "dd/mm/yyyy"
            } else if (date.endsWith("/11/23")) {
                dateformat = "yyyy/mm/dd"
            } else if (date.endsWith("/23/11")) {
                dateformat = "yyyy/dd/mm"
            } else if (date.startsWith("11-23-")) {
                dateformat = "mm-dd-yyyy"
            } else if (date.startsWith("23-11-")) {
                dateformat = "dd-mm-yyyy"
            } else if (date.endsWith("-11-23")) {
                dateformat = "yyyy-mm-dd"
            } else if (date.endsWith("-23-11")) {
                dateformat = "yyyy-dd-mm"
            } else if (date.startsWith("11.23.")) {
                dateformat = "mm.dd.yyyy"
            } else if (date.startsWith("23.11.")) {
                dateformat = "dd.mm.yyyy"
            } else if (date.endsWith(".11.23")) {
                dateformat = "yyyy.mm.dd"
            } else if (date.endsWith(".23.11")) {
                dateformat = "yyyy.dd.mm"
            }
            var timeformat = "hh:mm"
            if (!time.startsWith("14")) {
                timeformat = "hh:mm+M"
            }
            //#IF ENABLE_LOG
            log("# locale=$locale, date=$date, time=$time, dateformat=$dateformat, timeformat=$timeformat")
            //#ENDIF ENABLE_LOG
            ret.put(An.SettingsKey.buttonSize, buttonsize)
            ret.put(An.SettingsKey.uiFontName, SettingsDefs.uiFontName)
            ret.put(An.SettingsKey.uiFontStyle, SettingsDefs.uiFontStyle)
            ret.put(An.SettingsKey.uiFontSize, fontsize)
            ret.put(An.SettingsKey.fixedFontName, SettingsDefs.fixedFontName)
            ret.put(An.SettingsKey.fixedFontStyle, SettingsDefs.fixedFontStyle)
            ret.put(An.SettingsKey.dialogBGColor, SettingsDefs.dialogBGColor)
            ret.put(An.SettingsKey.headingColor, SettingsDefs.headingColor)
            ret.put(An.SettingsKey.linkColor, SettingsDefs.linkColor)
            ret.put(An.SettingsKey.annotationColor, SettingsDefs.annotationColor)
            ret.put(An.SettingsKey.highlightColor, SettingsDefs.highlightColor)
            ret.put(An.SettingsKey.dateFormat, dateformat)
            ret.put(An.SettingsKey.timeFormat, timeformat)
            ret.put(An.SettingsKey.imageDimension, SettingsDefs.imageDimension)
            return ret
        }
    }

    object FontInfo {
        @Throws(JSONException::class)
        fun getCategory(info: JSONObject): String? {
            var cat: String?
            if (info.isNull(Key.category)) {
                cat = FontCats.Others
            } else {
                cat = info.getString(Key.category)
                if (!FontCats.contains(cat)) {
                    cat = FontCats.Others
                }
            }
            return cat
        }

        fun splitFamily(family: String): Pair<String, String> {
            val index = family.indexOf('-')
            return if (index <= 0) {
                Pair(family, "")
            } else Pair(family.substring(0, index), family.substring(index + 1))
        }

        fun fontFamily(name: String, style: String?): String {
            return if (style == null || style.isEmpty()) {
                name
            } else "$name-$style"
        }

        fun fontface(name: String, path: String, fontfaceformat: String): String {
            return "@font-face { font-family: '$name'; src: url('../fonts/$path') format('$fontfaceformat');}"
        }

        object FontCats {
            const val Decorative = "Decorative"
            const val Monospace = "Monospace"
            const val SansSerif = "Sans Serif"
            const val Serif = "Serif"
            const val System = "System"
            const val Others = "Others"
            val set: MutableSet<String> = TreeSet()
            operator fun contains(cat: String?): Boolean {
                return set.contains(cat)
            }

            init {
                set.add(Decorative)
                set.add(Monospace)
                set.add(SansSerif)
                set.add(Serif)
                set.add(System)
                set.add(Others)
            }
        }

        object Key {
            const val fontname = "fontname"
            const val url = "url"
            const val fontfaceformat = "fontfaceformat"
            const val size = "size"
            const val license = "license"
            const val category = "category"
            const val subsets = "subsets"
            const val glyphcount = "glyphcount"
            const val glyphs = "glyphs"

            const val codepages = "codepages"
            const val glyphnames = "glyphnames"
            const val styles = "styles"
            const val fontfaces = "fontfaces"
            const val urls = "urls"
            const val fontfaceformats = "fontfaceformats"
            const val fonts0 = "fonts0"
            const val cats0 = "cats0"
            const val fonts1 = "fonts1"
            const val cats1 = "cats1"
            const val fonts2 = "fonts2"

            const val fonts = "fonts"
        }
    }

    object StylesInfo {
        object Key {
            const val charstyle = "charstyle"
            const val parastyle = "parastyle"
            const val classes = "class"
            const val text = "text"
            const val tooltips = "tooltips"
        }
    }

    object RecentsCmdUtil {
        private val names = arrayOf(
                "INVALID",
                "CLEAR", "CLEAN", "INFO", "BACK", "PEEK", "FORWARD")

        fun toString(cmd: Int): String {
            return if (cmd < 0 || cmd >= names.size) {
                names[An.RecentsCmd.INVALID]
            } else names[cmd]
        }
    }

    object FilepickerCmdUtil {
        private val names = arrayOf(
                "INVALID", "FILEINFO", "GOTO", "VALIDATE", "LISTDIR", "LISTDIR1", "LIST_DIRTREE", "MKDIR",
                "RENAME", "COPY",
                "COPY_INFO", "DELETE", "DELETE_DIRTREE", "DELETE_DIRSUBTREE", "DELETE_ALL", "DELETE_INFO", "IMAGE_INFO", "IMAGE_INFOS",
                "IMAGE_THUMBNAILS")

        fun toString(cmd: Int): String {
            return if (cmd < 0 || cmd >= names.size) {
                names[An.FilepickerCmd.INVALID]
            } else names[cmd]
        }
    }
}
