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
package sf.andrians.cplusedition.support.media

import com.cplusedition.bot.core.Basepath
import sf.andrians.cplusedition.support.An
import java.util.*
import kotlin.collections.MutableMap

object MimeUtil {
    //#BEGIN SINCE 2.9.0 From andrians.dart An.MimeUtil.
    const val ALL = "*/*"
    const val HTML = "text/html"
    const val CSS = "text/css"
    const val PDF = "application/pdf"
    const val JS = "text/javascript"
    const val JPEG = "image/jpeg"
    const val PNG = "image/png"
    const val IMAGE = "image/*"
    const val AUDIO = "audio/*"
    const val VIDEO = "video/*"
    const val M4A = "audio/mp4"
    const val TEXT_PLAIN = "text/plain"
    private val imageMimeByExt: MutableMap<String, String> = TreeMap()
    private val imageExtByMime: MutableMap<String, String> = TreeMap()

    private val audioPlaybackMimeByExt: MutableMap<String, String> = TreeMap()
    private val audioPlaybackExtByMime: MutableMap<String, String> = TreeMap()
    private val audioRecordMimeByExt: MutableMap<String, String> = TreeMap()
    private val videoPlaybackMimeByExt: MutableMap<String, String> = TreeMap()
    private val videoPlaybackExtByMime: MutableMap<String, String> = TreeMap()
    private val videoRecordingMimeByExt: MutableMap<String, String> = TreeMap()
    private val exportableMimeByExt: MutableMap<String, String> = TreeMap()
    private val importableMimeByExt: MutableMap<String, String> = TreeMap()
    private val FONT_MIMES: MutableMap<String, String> = TreeMap()
    fun mimeFromPath(path: String?): String? {
        val ext = Basepath.lcExt(path) ?: return null
        return when (ext) {
            "html" -> HTML
            "css" -> CSS
            "js" -> JS
            else -> mediaMimeFromLcExt(ext)
        }
    }

    fun mediaMimeFromPath(path: String?): String? {
        return mediaMimeFromLcExt(Basepath.lcExt(path))
    }

    fun mediaMimeFromLcExt(lcext: String?): String? {
        if (lcext == null) {
            return null
        }
        var ret = imageMimeByExt[lcext]
        if (ret != null) {
            return ret
        }
        if (audioPlaybackMimeByExt[lcext].also { ret = it } != null) {
            return ret
        }
        if ("pdf" == lcext) {
            return PDF
        }
        return if (videoPlaybackMimeByExt[lcext].also { ret = it } != null) {
            ret
        } else null
    }

    fun audioPlaybackMimeFromPath(path: String?): String? {
        return audioPlaybackMimeFromLcExt(Basepath.lcExt(path))
    }

    fun audioPlaybackMimeFromLcExt(lcext: String?): String? {
        return if (lcext == null) null else audioPlaybackMimeByExt[lcext]
    }

    fun audioRecordMimeFromPath(path: String?): String? {
        return audioRecordMimeFromLcExt(Basepath.lcExt(path))
    }

    fun audioRecordMimeFromLcExt(lcext: String?): String? {
        return if (lcext == null) null else audioRecordMimeByExt[lcext]
    }

    fun isAudioPlaybackLcExt(lcext: String?): Boolean {
        return lcext != null && audioPlaybackMimeByExt.containsKey(lcext)
    }

    fun audioRecordingExt(): String {
        return "m4a"
    }

    fun imageMimeFromPath(path: String?): String? {
        return imageMimeFromLcExt(Basepath.lcExt(path))
    }

    fun imageMimeFromLcExt(lcext: String?): String? {
        return if (lcext == null) null else imageMimeByExt[lcext]
    }

    fun imageExtFromMime(mime: String?): String? {
        return if (mime == null) null else imageExtByMime[mime]
    }

    fun isImagePath(path: String?): Boolean {
        return isImageLcExt(Basepath.lcExt(path))
    }

    fun isImageLcExt(lcext: String?): Boolean {
        return lcext != null && imageMimeByExt.containsKey(lcext)
    }

    fun isImageMime(mime: String?): Boolean {
        return mime != null && imageExtByMime.containsKey(mime)
    }

    fun isImageExportableToPhotoLibrary(lcext: String?): Boolean {
        return lcext != null && imageMimeByExt.containsKey(lcext) && "ico" != lcext && "svg" != lcext
    }

    fun isVideoPlaybackLcExt(lcext: String?): Boolean {
        return lcext != null && videoPlaybackMimeByExt.containsKey(lcext)
    }

    fun videoRecordingExt(): String {
        return "mp4"
    }

    fun videoPlaybackExtFromMime(mime: String?): String? {
        return if (mime == null) null else videoPlaybackExtByMime[mime.toLowerCase()]
    }

    fun videoPlaybackMimeFromPath(path: String?): String? {
        return videoPlaybackMimeFromLcExt(Basepath.lcExt(path))
    }

    fun videoPlaybackMimeFromLcExt(lcext: String?): String? {
        return if (lcext == null) null else videoPlaybackMimeByExt[lcext]
    }

    fun isHtmlPath(path: String?): Boolean {
        return "html" == Basepath.lcExt(path)
    }

    fun isMediaLcExt(lcext: String?): Boolean {
        return ("pdf" == lcext || isImageLcExt(lcext)
                || isVideoPlaybackLcExt(lcext)
                || isAudioPlaybackLcExt(lcext))
    }

    fun isImportableLcExt(lcext: String?): Boolean {
        return (lcext != null
                //#IF PRO
                //#ELSE PRO
                && importableMimeByExt.containsKey(lcext)
                //#ENDIF PRO
                )
    }

    fun isImportableMime(mime: String?): Boolean {
        return (mime != null
                //#IF PRO
                //#ELSE PRO
                && importableMimeByExt.containsValue(mime)
                //#ENDIF PRO
                )
    }

    fun isExportableLcExt(lcext: String?): Boolean {
        return (lcext != null
                //#IF PRO
                //#ELSE PRO
                && exportableMimeByExt.containsKey(lcext)
                //#ENDIF PRO
                )
    }

    fun isExportableMime(mime: String?): Boolean {
        return (mime != null
                //#IF PRO
                //#ELSE PRO
                && exportableMimeByExt.containsValue(mime)
                //#ENDIF PRO
                )
    }

    fun isSharableLcExt(lcext: String?): Boolean {
        return isMediaLcExt(lcext)
    }

    //#BEGIN NOTE Java side only
    fun isVideoPlaybackMime(mime: String?): Boolean {
        return videoPlaybackExtByMime.containsKey(mime)
    }

    fun pdfMimeFromPath(path: String?): String? {
        return pdfMimeFromLcExt(Basepath.lcExt(path))
    }

    fun pdfMimeFromLcExt(lcext: String?): String? {
        return if ("pdf" == lcext) PDF else null
    }

    fun fontMimeFromPath(path: String?): String? {
        return fontMimeFromLcExt(Basepath.lcExt(path))
    }

    fun fontMimeFromLcExt(lcext: String?): String? {
        return if (lcext == null) {
            null
        } else FONT_MIMES[lcext]
    }

    fun fontFaceFormat(ext: String?): String? {
        if (ext != null) {
            val lc = ext.toLowerCase(Locale.ENGLISH)
            if ("woff" == lc) {
                return "woff"
            } else if ("woff2" == lc) {
                return "woff2"
            } else if ("ttf" == lc || "otf" == lc) {
                return "opentype"
            } else if ("eot" == lc) {
                return "embeded-opentype"
            } else if ("svg" == lc || "svgz" == lc) {
                return "svg"
            }
        }
        return null
    }
    //#END NOTE Java side only.

    //#END SINCE 2.9.0
    init {
        imageMimeByExt["png"] = PNG
        imageMimeByExt["jpg"] = JPEG
        imageMimeByExt["gif"] = "image/gif"
        imageMimeByExt["bmp"] = "image/bmp"
        imageMimeByExt["ico"] = "image/vnd.microsoft.icon"
        imageMimeByExt["svg"] = "image/svg+xml"
        for ((key, value) in imageMimeByExt) {
            imageExtByMime[value] = key
        }
        imageMimeByExt["jpeg"] = JPEG
        audioPlaybackMimeByExt["mp3"] = "audio/mpeg"
        audioPlaybackMimeByExt["m4a"] = "audio/mp4"
        audioPlaybackMimeByExt["wav"] = "audio/wav"
        audioPlaybackMimeByExt["audio/mpeg"] = "mp3"
        audioPlaybackMimeByExt["audio/mp4"] = "m4a"
        audioPlaybackMimeByExt["audio/wav"] = "wav"
        for ((key, value) in audioPlaybackMimeByExt) {
            audioPlaybackExtByMime[value] = key
        }
        audioRecordMimeByExt["m4a"] = "audio/mp4"
        videoPlaybackMimeByExt["mov"] = "video/quicktime"
        videoPlaybackMimeByExt["mp4"] = "video/mp4"
        for ((key, value) in videoPlaybackMimeByExt) {
            videoPlaybackExtByMime[value] = key
        }
        videoRecordingMimeByExt["mp4"] = "video/mp4"
        importableMimeByExt.putAll(imageMimeByExt)
        importableMimeByExt.putAll(audioPlaybackMimeByExt)
        importableMimeByExt.putAll(videoPlaybackMimeByExt)
        importableMimeByExt[An.DEF.pdf] = PDF
        exportableMimeByExt.putAll(importableMimeByExt)
        exportableMimeByExt[An.DEF.css] = PDF
        exportableMimeByExt[An.DEF.html] = HTML
        FONT_MIMES["woff2"] = "application/font-woff2"
        FONT_MIMES["woff"] = "application/font-woff"
        FONT_MIMES["ttf"] = "application/x-font-ttf"
        FONT_MIMES["otf"] = "font/opentype"
        FONT_MIMES["eot"] = "application/vnd.ms-fontobject"
    }
}
