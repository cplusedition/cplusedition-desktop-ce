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

import com.cplusedition.anjson.JSONUtil.mapsStringNotNull
import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.Fun41
import com.cplusedition.bot.core.IInputStreamProvider
import com.cplusedition.bot.core.ITraceLogger
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.XMLUt
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.andrians.org.apache.http.client.utils.URLEncodedUtils
import sf.llk.grammar.html.parser.ASTstartTag
import sf.llk.grammar.html.parser.ASTtext
import sf.llk.grammar.html.parser.HtmlLexer
import sf.llk.grammar.html.parser.HtmlSaxAdapter
import sf.llk.share.support.DefaultTokenHandler
import sf.llk.share.support.LLKLexerInput
import java.io.File
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

object Support : ITraceLogger {
    private val datetimeFormatter = DateFormat.getDateTimeInstance()
    private val SEPA = File.separator.toCharArray()
    private var level = 0
    private var quiet = false
    private val quietStack = Stack<Boolean>()
    val LOCALE = Locale.US

    override fun d(msg: String, e: Throwable?) {
    }

    override fun i(msg: String, e: Throwable?) {
        log(
            msg
            , null
        )
    }

    override fun w(msg: String, e: Throwable?) {
        log(msg, e)
    }

    override fun e(msg: String, e: Throwable?) {
        log(msg, e)
    }

    override val debugging: Boolean
        get() =
    false

    override fun d(e: Throwable?, message: Fun11<Throwable?, String>) {
    }

    override fun i(e: Throwable?, message: Fun11<Throwable?, String>) {
        log(
            null,
            message
        )
    }

    override fun w(e: Throwable?, message: Fun11<Throwable?, String>) {
        log(e, message)
    }

    override fun e(e: Throwable?, message: Fun11<Throwable?, String>) {
        log(e, message)
    }

    override fun enter(msg: String) {
        this.d("# ${TextUt.stringOf('+', ++level)} $msg")
    }

    override fun <T> enter(msg: String, code: Fun01<T>): T {
        enter(msg)
        try {
            return code()
        } finally {
            leave(msg)
        }
    }

    override fun leave(msg: String) {
        this.d("# ${TextUt.stringOf('-', if (level > 0) --level else level)} $msg")
    }

    override fun <R> quiet(code: Fun01<R>): R {
        singleThreadPool.submit {
            quietStack.push(quiet)
            quiet = true
        }
        try {
            return code()
        } finally {
            singleThreadPool.submit {
                quiet = quietStack.pop()
            }
        }
    }

    private fun log(msg: String, e: Throwable?) {
        singleThreadPool.submit {
            if (!quiet) {
                println(msg)
                e?.printStackTrace(System.out)
            }
        }
    }

    private fun log(e: Throwable?, callback: Fun11<Throwable?, String>) {
        singleThreadPool.submit {
            if (!quiet) {
                println(callback(e))
                e?.printStackTrace(System.out)
            }
        }
    }

    fun <R> ignoreThrowable(msg: Any, code: Fun01<R>): R? {
        try {
            return code()
        } catch (e: Throwable) {
            d("# Ignored throwable: $msg: $e", e);
            return null
        }
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

    fun updateUISettings(res: IResUtil, current: JSONObject, update: JSONObject): List<String> {
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
                    An.SettingsKey.timeZone,
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
                b.append(Integer.toHexString(c.code))
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
        var p = path ?: return null
        val index = p.indexOf('?')
        if (index >= 0) {
            p = p.substring(0, index)
        }
        if (p.startsWith("/")) p = p.substring(1)
        if (p.isEmpty()) {
            return "./"
        }
        val segments = p.split("/")
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
        val cleanpath = Basepath.cleanPath(path).trim(File.separatorChar)
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
    private const val ValidFilenameChars = " !\"$&'()*+,-./:<=>@^_`{|}[]~"
    private val InvalidFilenameChars = "/\\?#%;".codePoints().toArray()

    private fun sanitizeFilename1(illegals: MutableCollection<Int>, filename: String) {
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
            if (c >= 0x20 && c < 0x7f && c != '\\'.code) {
                b.append(c.toChar())
            } else {
                b.append(TextUt.format("{0x%04x}", c))
            }
        }
        return b.toString()
    }

    fun <T : MutableCollection<String>> sanitizeFilepath(errors: T, res: IResUtil, apath: String): T {
        return sanitizeFilepath(errors, res, apath.split("/"))
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
        sanitizeFilepathStrict0(errors, res, apath.split("/"))
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
        sanitizeFilename1(illegals, filename)
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
        return sanitizeFilepathStrict(errors, res, apath.split("/"))
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

    fun validateFilepath(rsrc: IResUtil, path: String): MutableCollection<String> {
        val errors = ArrayList<String>()
        sanitizeFilepathStrict(errors, rsrc, path)
        return errors
    }

    fun validateFilename(rsrc: IResUtil, filename: String?): MutableCollection<String> {
        val errors = ArrayList<String>()
        val illegals = TreeSet<Int>()
        sanitizeFilenameStrict1(errors, rsrc, illegals, filename)
        return errors
    }

    fun datetime(ms: Long): String {
        return datetimeFormatter.format(Date(ms))
    }

    object Def {
        const val recentsSize = 24
        const val UnexpectedException = "Unexpected exception"
        const val maxInputImageArea = DEF.maxOutputImageArea
        const val thumbnailMiniHeight = 384
        const val thumbnailMiniWidth = 512
        const val loginFailureTimeout = 5000L
        const val memoryInfoInterval = 3000L
        const val scaleImageTimeout = 30 * 1000L
        const val thumbnailTimeout = 10 * 1000L
    }

    object SettingsDefs {
        const val symbolFamily = "FontAwesome"
        const val buttonSize = 40
        const val annotationColor = "#9400d3"
        const val fixedFontStyle = "Regular"
        const val dateFormat = "mm/dd/yyyy"
        const val highlightColor = "rgba(255, 255, 128, 0.75)"
        const val winHeight = 640
        const val winWidth = 360
        const val dialogBGColor = "rgba(255, 255, 240, 0.95)"
        const val uiFontName = "Ruda"
        const val timeFormat = "hh:mm"
        const val uiFontStyle = "Regular"
        const val linkColor = "#0000cc"
        const val imageDimension = 1024
        const val fixedFontName = "UbuntuMono"
        const val dpi = 160
        const val headingColor = "#1e90ff"
        const val fontSize = 16.0
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
            "Gray (4 levels)", "Black and white"
        )

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
            canedit.add(An.ATTR.xDateFormat)
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

        fun getHomeHtml(): String {
            return /* if (loggedin) An.PATH._PrivateIndexHtml else */ An.PATH._HomeIndexHtml
        }

        fun getHome(): String {
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

        fun interpretPath(path: String): String {
            if (!path.contains("{")) {
                return path
            }
            val time = System.currentTimeMillis()
            val year = TextUt.format("%tY", time)
            val month = TextUt.format("%tm", time)
            val day = TextUt.format("%td", time)
            val date = TextUt.format("%1\$tY%1\$tm%1\$td", time)
            val home = getHome()
            return path.replace("{home}", home).replace("{date}", date).replace("{year}", year).replace("{month}", month)
                .replace("{day}", day)
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
            return initdefaults(buttonsize, fontsize)
        }

        @Throws(JSONException::class)
        private fun initdefaults(buttonsize: Int, fontsize: Int): JSONObject {
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
            "CLEAR", "CLEAN", "INFO", "BACK", "PEEK", "FORWARD"
        )

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
            "IMAGE_THUMBNAILS"
        )

        fun toString(cmd: Int): String {
            return if (cmd < 0 || cmd >= names.size) {
                names[An.FilepickerCmd.INVALID]
            } else names[cmd]
        }
    }
}

class GalleryParams constructor(
    val outpath: String,
    val templatepath: String,
    val options: GalleryOptions,
    val dirpath: String,
    val rpaths: Collection<String>?,
) {
    companion object {
        fun from(args: JSONArray, index: Int): GalleryParams {
            val outpath = args.getString(index)
            val templatepath = args.getString(index + 1)
            val options = GalleryOptions.from(args.getJSONArray(index + 2))
            val dirpath = args.getString(index + 3)
            val rpaths = args.getJSONArray(index + 4).mapsStringNotNull { it }.toList()
            return GalleryParams(outpath, templatepath, options, dirpath, rpaths)
        }
    }
}

class GalleryOptions constructor(
    val descending: Boolean,
    val singleSection: Boolean,
    val largeTn: Boolean,
    val scrollableTn: Boolean,
    val preserving: Boolean,
) {
    companion object {
        fun from(args: JSONArray, start: Int = 0): GalleryOptions {
            return GalleryOptions(
                args.getBoolean(start),
                args.getBoolean(start + 1),
                args.getBoolean(start + 2),
                args.getBoolean(start + 3),
                args.getBoolean(start + 4),
            )
        }
    }
}

class GalleryGenerator constructor(
    private val storage: IStorage,
    private val params: GalleryParams,
    private val isLandscape: Fun31<String, IInputStreamProvider, Int, Boolean?>,
    private val thumbnailGenerator: Fun41<IFileInfo, Int, Int, Int, String?>,
) {
    private val rsrc = storage.rsrc
    private val options = params.options
    private val major = if (options.scrollableTn) 0 else
        if (options.largeTn) DEF.previewPhotoSize else
            DEF.thumbnailSize
    private val minor = if (options.scrollableTn) DEF.scrollableThumbnailSize else
        if (options.largeTn) DEF.previewPhotoSize else
            DEF.thumbnailSize

    private class GalleryInfos constructor(
        val size: Long,
        val count: Int,
        val infos: Map<String, List<GalleryInfo>>
    )

    private class GalleryInfo constructor(
        val fileinfo: IFileInfo,
        val filestat: IFileStat,
        val cpath: String,
        val rpath: String,
        val basepath: Basepath,
        val poster: String?,
        var thumbnail: GalleryInfo?
    )

    fun run(done: Fun10<JSONObject>) {
        fun error(msgid: Int, vararg args: String) {
            done(rsrc.jsonObjectError(msgid, *args))
        }

        fun error(msg: Collection<String>) {
            done(rsrc.jsonObjectError(msg))
        }

        fun result(result: JSONObject) {
            done(result)
        }

        val rpaths = params.rpaths
            ?: return error(R.string.ParametersInvalid)
        val outinfo = storage.fileInfoAt(params.outpath).result()
        val outdirinfo = outinfo?.parent
        if (outinfo == null || outdirinfo == null)
            return error(R.string.InvalidOutputPath_, params.outpath)
        if (outdirinfo.stat()?.writable != true)
            return error(R.string.DestinationNotWritable_, params.outpath)
        val templateinfo = storage.fileInfoAt(params.templatepath).let {
            it.result() ?: return error(it.failure()!!)
        }
        if (!templateinfo.exists)
            return error(R.string.NotFound_, params.templatepath)
        val dirinfo = storage.fileInfoAt(params.dirpath).let {
            it.result() ?: return error(it.failure()!!)
        }
        if (!dirinfo.exists)
            return error(R.string.NotFound_, params.dirpath)
        try {
            val template = uplink(templateinfo.content().readText(), makelinklabel(outdirinfo.name))
            val bydir = sortbydir(rpaths)
            if (bydir.length() == 0) {
                return result(JSONObject())
            }
            val dirprefix = if (Support.PathUtil.isAssetsTree(params.dirpath)) dirinfo.apath
            else (FileUt.rpathOrNull(dirinfo.apath, outdirinfo.apath) ?: dirinfo.apath)
            
            val basepath = Basepath.from(params.templatepath)
            when (basepath.stem) {
                An.TemplateName.audioV2 -> generateAudioV2Gallery(
                    outinfo,
                    template,
                    dirprefix,
                    dirinfo,
                    bydir,
                    done
                )

                An.TemplateName.homeSimpler -> generateHomeSimplerGallery(
                    outinfo,
                    template,
                    dirprefix,
                    dirinfo,
                    bydir,
                    done
                )

                An.TemplateName.mediaSticker -> generateMediaStickerGallery(
                    outinfo,
                    template,
                    dirprefix,
                    dirinfo,
                    bydir,
                    done
                )

                An.TemplateName.mediaWall -> generateMediaWallGallery(
                    outinfo,
                    template,
                    dirprefix,
                    dirinfo,
                    bydir,
                    done
                )

                else -> error(R.string.InvalidTemplate)
            }
        } catch (e: Exception) {
            return error(R.string.CommandFailed)
        }
    }

    private fun makelinklabel(name: String): String {
        return TextUt.capitalCase(name).replace(labelPat, " ")
    }

    private fun uplink(template: String, label: String): String {
        val main = NullLLKMain("")
        val input = LLKLexerInput(template.toCharArray(), main)
        val doc = object : HtmlSaxAdapter(
            HtmlLexer(
                input,
                DefaultTokenHandler.getInstance(input, HtmlLexer.specialTokens(), true, true)
            )
        ) {
            private var state = 0
            override fun startTag(node: ASTstartTag) {
                Support.ignoreThrowable(node) {
                    when (state) {
                        0 -> if (node.getAttributeText(An.ATTR.id) == An.ID.xRightSidebar) state = 1
                        1 -> {
                            node.getAttributeText(An.ATTR.classes)?.let { classes ->
                                classes.contains(An.CSS.xRightSidebarTab) && classes.contains(An.CSS.xPlaceholder)
                                node.getAttribute(An.ATTR.href)?.let { a ->
                                    a.valueToken.text = XMLUt.quoteAttr("../index.html")
                                    state = 2
                                }
                            }
                        }

                        else -> {
                            if (state >= 2) state = 0
                        }
                    }
                    super.startTag(node)
                }
            }

            override fun text(node: ASTtext?) {
                when (state) {
                    2 -> {
                        node?.text = XMLUt.escText(label)
                        state = 0
                    }
                }
            }
        }.parse()
        return XrefUt.render(doc.firstToken).toString()
    }

    private fun getStringList(a: JSONArray): List<String> {
        val ret: MutableList<String> = ArrayList()
        var index = 0
        val len = a.length()
        while (index < len) {
            val s = a.stringOrNull(index)
            if (s != null) {
                ret.add(s)
            }
            ++index
        }
        return ret
    }

    private fun _sorted0(ret: List<String>, descending: Boolean): List<String> {
        val c = java.util.Comparator { a: String, b: String -> a.compareTo(b) }
        ret.sortedWith(if (descending) c.reversed() else c)
        return ret
    }

    private fun _sorted(a: Collection<String>, descending: Boolean): List<String> {
        return _sorted0(ArrayList(a), descending)
    }

    private fun _sorted(a: Iterator<String>, descending: Boolean): List<String> {
        return _sorted0(a.asSequence().toList(), descending)
    }

    private fun _sorted(a: JSONArray, descending: Boolean): List<String> {
        return _sorted0(getStringList(a), descending)
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateAudioV2Gallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>,
    ) {
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }

        val addaudio = { path: String, cpath: String, filestat: IFileStat ->
            val url = href(path)
            section.put(
                cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.filestat, FileInfoUtil.toJSONFileStat(JSONObject(), filestat))
            )
        }
        val addvideo = { path: String, cpath: String, filestat: IFileStat ->
            val url = href(path)
            section.put(
                cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.filestat, FileInfoUtil.toJSONFileStat(JSONObject(), filestat))
            )
        }
        for (sec in bydir.keys()) {
            for (rpath in bydir.getJSONArray(sec).mapsStringNotNull { it }) {
                val basepath = Basepath.from(rpath)
                val lcsuffix = basepath.lcSuffix
                if (MimeUtil.isAudioLcSuffix(lcsuffix)) {
                    val fileinfo = dirinfo.fileInfo(rpath)
                    val filestat = fileinfo.stat()
                    if (filestat == null || !filestat.isFile) {
                        continue
                    }
                    addaudio(Basepath.joinRpath(dirprefix, rpath), fileinfo.cpath, filestat)
                } else if (MimeUtil.isVideoLcSuffix(lcsuffix)) {
                    val fileinfo = dirinfo.fileInfo(rpath)
                    val filestat = fileinfo.stat()
                    if (filestat == null || !filestat.isFile) {
                        continue
                    }
                    addvideo(Basepath.joinRpath(dirprefix, rpath), fileinfo.cpath, filestat)
                }
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.audioV2)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateHomeSimplerGallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>
    ) {
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }
        val addtoc2 = { rpathAndLabel: Pair<String, String>, cpath: String ->
            val url = href(Basepath.joinRpath(dirprefix, rpathAndLabel.first))
            section.put(
                cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, rpathAndLabel.second)
            )
        }
        val addtoc3 = { rpathAndLabel: Pair<String, String>, cpath: String ->
            val url = href(Basepath.joinRpath(dirprefix, rpathAndLabel.first)) + "?view"
            section.put(
                cpath, JSONObject()
                    .put(Key.url, url)
                    .put(Key.text, rpathAndLabel.second)
            )
        }
        for (sec in bydir.keys()) {
            for (rpath in bydir.getJSONArray(sec).mapsStringNotNull { it }) {
                val basepath = Basepath.from(rpath)
                val lcsuffix = basepath.lcSuffix
                val fileinfo = dirinfo.fileInfo(rpath)
                val filestat = fileinfo.stat()
                if (filestat == null || !filestat.isFile) {
                    continue
                }
                if (lcsuffix == Suffix.HTML) {
                    addtoc2(Pair(rpath, label(basepath)), fileinfo.cpath)
                } else if (MimeUtil.isViewerLcSuffix(lcsuffix)) {
                    addtoc3(Pair(rpath, label(basepath)), fileinfo.cpath)
                }
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.homeSimpler)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generatePhotoSticker1Gallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>
    ) {
        val infos = scanMedias(dirinfo, bydir, MimeUtil::isImageLcSuffix)
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }
        for (sec in infos.infos.keys) {
            for (info in infos.infos[sec]!!) {
                if (!info.fileinfo.exists) continue
                addmedia(section, dirprefix, info)
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.photoSticker1)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generatePhotoWallGallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>
    ) {
        val infos = scanMedias(dirinfo, bydir, MimeUtil::isImageLcSuffix)
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }
        for (sec in infos.infos.keys) {
            for (info in infos.infos[sec]!!) {
                if (!info.fileinfo.exists) continue
                addmedia(section, dirprefix, info)
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.photoWall)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateMediaStickerGallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>
    ) {
        val infos = scanMedias(dirinfo, bydir, MimeUtil::isMediaLcSuffix)
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }
        for (sec in infos.infos.keys) {
            for (info in infos.infos[sec]!!) {
                if (!info.fileinfo.exists) continue
                addmedia(section, dirprefix, info)
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.mediaSticker)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    @Throws(JSONException::class, StorageException::class)
    private fun generateMediaWallGallery(
        outinfo: IFileInfo,
        template: String,
        dirprefix: String?,
        dirinfo: IFileInfo,
        bydir: JSONObject,
        done: Fun10<JSONObject>
    ) {
        val infos = scanMedias(dirinfo, bydir, MimeUtil::isMediaLcSuffix)
        val result = JSONObject()
        var section = JSONObject()
        if (options.singleSection) result.put("", section)
        val endsection = { name: String ->
            if (!options.singleSection) {
                result.put(name, section)
                section = JSONObject()
            }
        }
        for (sec in infos.infos.keys) {
            for (info in infos.infos[sec]!!) {
                if (!info.fileinfo.exists) continue
                addmedia(section, dirprefix, info)
            }
            endsection(sec)
        }
        done(
            JSONObject()
                .put(Key.type, An.TemplateName.mediaWall)
                .put(Key.backward, options.descending)
                .put(Key.path, outinfo.cpath)
                .put(Key.template, template)
                .put(Key.text, if (options.preserving && outinfo.exists) outinfo.content().readText() else "")
                .put(Key.result, result)
        )
    }

    private fun addmedia(section: JSONObject, dirprefix: String?, galleryinfo: GalleryInfo) {
        
        val path = Basepath.joinRpath(dirprefix, galleryinfo.rpath)
        val url = href(path)
        val item = JSONObject()
            .put(Key.url, url)
            .put(Key.text, label(galleryinfo.basepath))
            .put(Key.filestat, FileInfoUtil.toJSONFileStat(JSONObject(), galleryinfo.filestat))
        Without.throwableOrNull {
            val islandscape = isLandscape(galleryinfo.basepath.suffix, galleryinfo.fileinfo.content(), 0)
            val orientation = if (islandscape == null) ""
            else if (islandscape == true) An.CSS.xLandscape
            else An.CSS.xPortrait
            item.put(Key.orientation, orientation)
            if (galleryinfo.poster != null) item.put(Key.poster, galleryinfo.poster)
            val tn = galleryinfo.thumbnail
            if (tn != null) {
                item.put(Key.src, href(Basepath.joinRpath(dirprefix, tn.rpath)))
            } else if (MimeUtil.isImageLcSuffix(galleryinfo.basepath.lcSuffix)) {
                if (galleryinfo.filestat.length > DEF.thumbnailThreshold) {
                    thumbnailGenerator(
                        galleryinfo.fileinfo,
                        if (islandscape == true) major else minor,
                        if (islandscape == true) minor else major,
                        DEF.jpegQualityVeryLow
                    )?.let {
                        item.put(Key.src, it)
                        if (options.scrollableTn) {
                            item.put(Key.css, if (islandscape == true) An.CSS.xxScrollH else An.CSS.xxScrollV)
                        }
                        item
                    } ?: item.put(Key.src, url)
                }
            }
            item
        } ?: return
        section.put(galleryinfo.cpath, item)
    }

    private fun href(path: String): String {
        return URLEncodedUtils.encPath(path, Charsets.UTF_8).replace("\"", "&quot;")
    }

    private fun label(basepath: Basepath): String {
        val text = if (basepath.name == "index.html" && basepath.dir != null)
            Basepath.from(basepath.dir!!).stem else
            basepath.stem
        if (text.isEmpty())
            return text
        val label = text.replace(labelPat, " ").trim().ifEmpty { text }
        val c = label[0]
        return if (Character.isUpperCase(c)) label else
            Character.toUpperCase(c).toString() + label.substring(1)
    }

    @Throws(JSONException::class)
    private fun sortbydir(rpaths: Collection<String>): JSONObject {
        val ret = JSONObject()
        for (rpath in rpaths) {
            val basepath = Basepath.from(rpath)
            var section = basepath.dir
            if (section == null) section = ""
            val sections = ret.optJSONArray(section)
            if (sections != null) {
                sections.put(rpath)
            } else {
                ret.put(section, JSONArray().put(rpath))
            }
        }
        return ret
    }

    private fun sectionname(prefix: String, rdir: String): String {
        return Basepath.joinRpath(prefix, rdir)
    }

    @Throws(JSONException::class)
    private fun scanMedias(
        dirinfo: IFileInfo,
        bydir: JSONObject,
        ismedia: Fun11<String, Boolean>
    ): GalleryInfos {
        var size = 0L
        var count = 0
        val map: MutableMap<String, List<GalleryInfo>> = TreeMap()
        dirinfo.root.transaction {
            for (section in bydir.keys()) {
                val sorted: MutableList<GalleryInfo> = ArrayList()
                for (rpath in bydir.getJSONArray(section).mapsStringNotNull { it }) {
                    val basepath = Basepath.from(rpath)
                    val lcsuffix = basepath.lcSuffix
                    if (ismedia(lcsuffix)) {
                        val fileinfo = dirinfo.fileInfo(rpath)
                        val filestat = fileinfo.stat()
                        if (filestat == null || !filestat.isFile) {
                            continue
                        }
                        size += filestat.length
                        count += 1
                        val poster = if (lcsuffix == MimeUtil.Suffix.PDF) {
                            thumbnailGenerator(fileinfo, DEF.pdfPosterSize, DEF.pdfPosterSize, DEF.jpegQualityThumbnail)
                        } else if (MimeUtil.isVideoLcSuffix(lcsuffix)) {
                            thumbnailGenerator(fileinfo, DEF.pdfPosterSize, DEF.pdfPosterSize, DEF.jpegQualityThumbnail)
                        } else null
                        sorted.add(GalleryInfo(fileinfo, filestat, fileinfo.cpath, rpath, basepath, poster, null))
                    }
                }
                val filtered = filterThumbnails(sorted)
                if (filtered.isNotEmpty()) {
                    map[section] = filtered
                }
            }
        }
        return GalleryInfos(size, count, map)
    }

    ///// @page 0 for first page.

    private fun filterThumbnails(a: MutableList<GalleryInfo>): List<GalleryInfo> {
        return a
    }

    companion object {
        private val labelPat = Regex("[-_]")
    }
}
