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

object MimeUtil {
    object Mime {
        const val HTML = "text/html"
        const val CSS = "text/css"
        const val JS = "text/javascript"
        const val TXT = "text/plain"
        const val PDF = "application/pdf"
        const val JSON = "application/json"
        const val XML = "application/xml"
        const val JPEG = "image/jpeg"
        const val PNG = "image/png"
        const val GIF = "image/gif"
        const val BMP = "image/bmp"
        const val ICO = "image/vnd.microsoft.icon"
        const val WEBP = "image/webp"
        const val HEIC = "image/heic"
        const val SVG = "image/svg+xml"
        const val AUDIO = "audio/*"
        const val M4A = "audio/mp4"
        const val MP3 = "audio/mpeg"
        const val WAV = "audio/wav"
        const val OGG = "audio/ogg"
        const val FLAC = "audio/flac"
        const val AWB = "audio/amr-wb"
        const val MP4 = "video/mp4"
        const val MOV = "video/quicktime"
        const val WEBM = "video/webm"
        const val WOFF2 = "application/font-woff2"
        const val WOFF = "application/font-woff"
        const val TTF = "application/x-font-ttf"
        const val OTF = "font/opentype"
        const val EOT = "application/vnd.ms-fontobject"
        const val DER = "application/pkcs7-mime"
        const val PEM = "application/pem"
        const val ZIP = "application/zip"
        const val BACKUP = "application/vnd.cplusedition.backup"
        const val IBACKUP = "application/vnd.cplusedition.ibackup"
    }

    object Suffix {
        const val HTML = ".html"
        const val CSS = ".css"
        const val JS = ".js"
        const val TXT = ".txt"
        const val PDF = ".pdf"
        const val JSON = ".json"
        const val XML = ".xml"
        const val JPG = ".jpg"
        const val JPEG = ".jpeg"
        const val PNG = ".png"
        const val GIF = ".gif"
        const val BMP = ".bmp"
        const val ICO = ".ico"
        const val WEBP = ".webp"
        const val HEIC = ".heic"
        const val SVG = ".svg"
        const val SVGZ = ".svgz"
        const val M4A = ".m4a"
        const val MP3 = ".mp3"
        const val WAV = ".wav"
        const val OGG = ".ogg"
        const val FLAC = ".flac"
        const val AWB = ".awb"
        const val MP4 = ".mp4"
        const val MOV = ".mov"
        const val WEBM = ".webm"
        const val WOFF2 = ".woff2"
        const val WOFF = ".woff"
        const val TTF = ".ttf"
        const val OTF = ".otf"
        const val EOT = ".eot"
        const val DER = ".der"
        const val PEM = ".pem"
        const val ZIP = ".zip"
        const val BACKUP = ".backup"
        const val IBACKUP = ".ibackup"
    }

    object Fontface {
        const val WOFF2 = "woff2"
        const val WOFF = "woff"
        const val OPENTYPE = "opentype"
        const val EOT = "embeded-opentype"
        const val SVG = "svg"
    }

    private val fontSuffices = setOf(Suffix.WOFF2, Suffix.WOFF, Suffix.TTF, Suffix.OTF, Suffix.EOT)
    private val textViewerSuffices = setOf(Suffix.TXT, Suffix.JSON, Suffix.XML)
    private val mimeBySuffix = mapOf(
        Suffix.HTML to Mime.HTML,
        Suffix.CSS to Mime.CSS,
        Suffix.JS to Mime.JS,
        Suffix.TXT to Mime.TXT,
        Suffix.PDF to Mime.PDF,
        Suffix.JSON to Mime.JSON,
        Suffix.XML to Mime.XML,
        Suffix.JPG to Mime.JPEG,
        Suffix.JPEG to Mime.JPEG,
        Suffix.PNG to Mime.PNG,
        Suffix.GIF to Mime.GIF,
        Suffix.BMP to Mime.BMP,
        Suffix.ICO to Mime.ICO,
        Suffix.WEBP to Mime.WEBP,
        Suffix.HEIC to Mime.HEIC,
        Suffix.M4A to Mime.M4A,
        Suffix.MP3 to Mime.MP3,
        Suffix.WAV to Mime.WAV,
        Suffix.OGG to Mime.OGG,
        Suffix.FLAC to Mime.FLAC,
        Suffix.AWB to Mime.AWB,
        Suffix.MP4 to Mime.MP4,
        Suffix.MOV to Mime.MOV,
        Suffix.WEBM to Mime.WEBM,
        Suffix.WOFF2 to Mime.WOFF2,
        Suffix.WOFF to Mime.WOFF,
        Suffix.TTF to Mime.TTF,
        Suffix.OTF to Mime.OTF,
        Suffix.EOT to Mime.EOT,
        Suffix.DER to Mime.DER,
        Suffix.PEM to Mime.PEM,
        Suffix.ZIP to Mime.ZIP,
        Suffix.BACKUP to Mime.BACKUP,
        Suffix.IBACKUP to Mime.IBACKUP,
    )
    private val suffixByMime = mutableMapOf(*(mimeBySuffix.map { it.value to it.key }.toTypedArray())).also {
        it.put(Mime.JPEG, Suffix.JPG)
    }

    val mimeTextArray = arrayOf(Mime.TXT)

    fun mimeFromPath(path: String?): String? {
        if (path == null) return null
        return mimeBySuffix[Basepath.lcSuffix(path)]
    }

    fun mimeFromLcSuffix(lcsuffix: String): String? {
        return mimeBySuffix[lcsuffix]
    }

    fun suffixFromMime(mime: String?): String? {
        if (mime == null) return null
        return suffixByMime[mime]
    }

    fun isBackupLcSuffix(lcsuffix: String): Boolean {
        return lcsuffix == Suffix.ZIP || lcsuffix == Suffix.BACKUP || lcsuffix == Suffix.IBACKUP
    }

    fun isImageLcSuffix(lcsuffix: String): Boolean {
        return mimeBySuffix[lcsuffix]?.startsWith("image/") == true
    }

    fun isAudioLcSuffix(lcsuffix: String): Boolean {
        return mimeBySuffix[lcsuffix]?.startsWith("audio/") == true
    }

    fun isVideoLcSuffix(lcsuffix: String): Boolean {
        return mimeBySuffix[lcsuffix]?.startsWith("video/") == true
    }

    fun isMediaLcSuffix(lcsuffix: String): Boolean {
        if (lcsuffix == Suffix.PDF) return true
        val mime = mimeBySuffix[lcsuffix] ?: return false
        return mime.startsWith("image/") || mime.startsWith("audio/") || mime.startsWith("video/")
    }

    /// Text files other than html that can be viewed in the main editor area, eg, .xml, .txt, .json, ... etc.
    fun isTextViewerLcSuffix(lcsuffix: String): Boolean {
        return textViewerSuffices.contains(lcsuffix)
    }

    /// Files that can be viewed through a viewer, includes text, pdf, image, audio, video, ... etc.
    fun isViewerLcSuffix(lcsuffix: String): Boolean {
        return isTextViewerLcSuffix(lcsuffix) || isMediaLcSuffix(lcsuffix)
    }

    fun isFontLcSuffix(lcsuffix: String): Boolean {
        return fontSuffices.contains(lcsuffix)
    }

    fun isSeekableLcSuffix(lcsuffix: String): Boolean {
        return isAudioLcSuffix(lcsuffix) || isVideoLcSuffix(lcsuffix) || lcsuffix == Suffix.BACKUP || lcsuffix == Suffix.IBACKUP
    }

    fun isImportableLcSuffix(lcsuffix: String): Boolean {
        return true
    }

    fun imageMimeFromLcSuffix(lcsuffix: String): String? {
        val mime = mimeBySuffix[lcsuffix] ?: return null
        return if (mime.startsWith("image/")) mime else null
    }

    fun audioMimeFromLcSuffix(lcsuffix: String): String? {
        val mime = mimeBySuffix[lcsuffix] ?: return null
        return if (mime.startsWith("audio/")) mime else null
    }

    fun videoMimeFromLcSuffix(lcsuffix: String): String? {
        val mime = mimeBySuffix[lcsuffix] ?: return null
        return if (mime.startsWith("video/")) mime else null
    }

    fun fontMimeFromLcSuffix(lcsuffix: String): String? {
        return if (fontSuffices.contains(lcsuffix)) mimeBySuffix[lcsuffix] else null
    }

    fun imageMimeFromPath(path: String): String? {
        return imageMimeFromLcSuffix(Basepath.lcSuffix(path))
    }

    fun imageSuffixFromMime(mime: String): String? {
        if (!mime.startsWith("image/")) return null
        return suffixByMime[mime]
    }

    fun audioRecordingSuffix(): String {
        return Suffix.M4A
    }

    fun videoRecordingSuffix(): String {
        return Suffix.MP4
    }

    fun fontFaceFormat(lcsuffix: String): String? {
        if (Suffix.WOFF == lcsuffix) {
            return Fontface.WOFF
        } else if (Suffix.WOFF2 == lcsuffix) {
            return Fontface.WOFF2
        } else if (Suffix.TTF == lcsuffix || Suffix.OTF == lcsuffix) {
            return Fontface.OPENTYPE
        } else if (Suffix.EOT == lcsuffix) {
            return Fontface.EOT
        } else if (Suffix.SVG == lcsuffix || Suffix.SVGZ == lcsuffix) {
            return Fontface.SVG
        }
        return null
    }

    fun isSameType(lcsuffixa: String, lcsuffixb: String): Boolean {
        if (isFontLcSuffix(lcsuffixa) && isFontLcSuffix(lcsuffixb)) return true
        if (isImageLcSuffix(lcsuffixa) && isImageLcSuffix(lcsuffixb)) return true
        if (isVideoLcSuffix(lcsuffixa) && isVideoLcSuffix(lcsuffixb)) return true
        if (isAudioLcSuffix(lcsuffixa) && isAudioLcSuffix(lcsuffixb)) return true
        return lcsuffixa == lcsuffixb
    }
}
