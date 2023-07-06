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
package sf.andrians.cplusedition.war

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.ILog
import com.cplusedition.bot.core.deleteSubtreesOrNull
import com.cplusedition.bot.core.file
import com.cplusedition.bot.core.mkdirsOrFail
import org.json.JSONObject
import sf.andrians.cplusedition.support.An.SettingsKey
import sf.andrians.cplusedition.support.ConsoleLoggerAdapter
import sf.andrians.cplusedition.support.Support.SettingsDefs
import sf.andrians.cplusedition.support.css.CSSGenerator
import java.io.File
import java.util.*
import kotlin.math.roundToInt

object Conf {
    const val SCHEME = "http"
    const val HOST = "localhost"
    const val APP = "Cplusedition"
    const val COPY_BUFSIZE = 16 * 1024
    const val QRCODE_SCALE = 2

    @JvmStatic
    val logger: ILog = ConsoleLoggerAdapter()

    fun df(format: String, vararg args: Any?) {
        
    }

    fun d(msg: String) {
        
    }

    @JvmStatic
    fun d(msg: String, e: Throwable?) {
        
    }

    fun w(msg: String) {
        logger.w(msg)
    }

    fun w(msg: String, e: Throwable?) {
        logger.w(msg, e)
    }

    fun e(msg: String) {
        logger.e(msg)
    }

    @JvmStatic
    fun e(msg: String, e: Throwable?) {
        logger.e(msg, e)
    }

    fun packageKey(key: String): String {
        return "$APP.$key"
    }

    fun getEtcDir(datadir: File): File {
        return File(datadir, "etc").mkdirsOrFail()
    }

    @JvmStatic
    fun getLoginCf(datadir: File): File {
        return File(getEtcDir(datadir), Paths.loginCf)
    }

    fun getKeystore(datadir: File): File {
        return File(getEtcDir(datadir), Paths.keystore)
    }

    fun getCacheDir(datadir: File, type: String?): File {
        var ret = File(datadir, Paths._cache)
        if (type != null && type.isNotEmpty()) {
            ret = File(ret, type)
        }
        ret.mkdirs()
        return ret
    }

    @JvmStatic
    fun getCacheFile(datadir: File, type: String?, rpath: String): File? {
        val cachedir = getCacheDir(datadir, type)
        val file = File(cachedir, rpath)
        val rpathx = FileUt.rpathOrNull(file, cachedir) ?: return null
        return File(cachedir, rpathx)
    }

    fun getTempDir(datadir: File): File {
        return datadir.file(Paths.TEMP_DIR)
    }

    @JvmStatic
    fun clearCacheDir(datadir: File) {
        val cachedir = getCacheDir(datadir, null)
        cachedir.deleteSubtreesOrNull()
    }

    fun getDatabaseDir(datadir: File): File {
        return File(datadir, ".db").also { it.mkdirs() }
    }

    @JvmStatic
    fun getCSSConf(ret: JSONObject): CSSGenerator.IConf {
        return object : CSSGenerator.IConf {
            val buttonsSize = ret.optInt(SettingsKey.buttonSize, SettingsDefs.buttonSize * SettingsDefs.dpi / Defs.dpi)
            val fontName = ret.stringOrDef(SettingsKey.uiFontName, SettingsDefs.uiFontName)
            val fontStyle = ret.stringOrDef(SettingsKey.uiFontStyle, SettingsDefs.uiFontStyle)
            val fontSize = precision(
                ret.optDouble(SettingsKey.uiFontSize, SettingsDefs.fontSize * SettingsDefs.dpi / Defs.dpi),
                100.0)
            val fixedFontName = ret.stringOrDef(SettingsKey.fixedFontName, SettingsDefs.fixedFontName)
            val fixedFontStyle = ret.stringOrDef(SettingsKey.fixedFontStyle, SettingsDefs.fixedFontStyle)
            val dialogBGColor = ret.stringOrDef(SettingsKey.dialogBGColor, SettingsDefs.dialogBGColor)
            val headingColor = ret.stringOrDef(SettingsKey.headingColor, SettingsDefs.headingColor)
            val linkColor = ret.stringOrDef(SettingsKey.linkColor, SettingsDefs.linkColor)
            val annotationColor = ret.stringOrDef(SettingsKey.annotationColor, SettingsDefs.annotationColor)
            val highlightColor = ret.stringOrDef(SettingsKey.highlightColor, SettingsDefs.highlightColor)
            val fontFamily = fontName + if (fontStyle.isNotEmpty()) "-$fontStyle" else ""
            val fixedFontFamily = fixedFontName + if (fixedFontStyle.isNotEmpty()) "-$fixedFontStyle" else ""

            private fun precision(value: Double, precision: Double): Double {
                return (value * precision).roundToInt() / precision
            }

            override fun dpi(): Int {
                return Defs.dpi
            }

            override fun winWidth(): Int {
                return 1280
            }

            override fun winHeight(): Int {
                return 800
            }

            override fun isMobile(): Boolean {
                return false
            }

            override fun buttonSize(): Int {
                return buttonsSize
            }

            override fun fontFamily(): String {
                return fontFamily
            }

            override fun fontSize(): Double {
                return fontSize
            }

            override fun fixedFontFamily(): String {
                return fixedFontFamily
            }

            override fun dialogBGColor(): String {
                return dialogBGColor
            }

            override fun headingColor(): String {
                return headingColor
            }

            override fun linkColor(): String {
                return linkColor
            }

            override fun annotationColor(): String {
                return annotationColor
            }

            override fun highlightColor(): String {
                return highlightColor
            }
        }
    }

    private object Paths {
        const val loginCf = "login.cf"
        const val _cache = ".cache"
        const val TEMP_DIR = ".tmp"
        const val keystore = ".keystore"
    }

    internal object Defs {
        const val dpi = 160
    }

    object CacheType {
        const val thumbnails = "tn"
        const val images = "im"
        const val indexes = "ix"
    }

    internal object RequestCode {
        const val EVENT = 0x10000000
    }
}
