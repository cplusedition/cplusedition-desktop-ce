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

import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.IByteSlice
import com.cplusedition.bot.core.IInputStreamProvider
import com.cplusedition.bot.core.ITraceLogger
import com.cplusedition.bot.core.Without
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Effect
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.ImageUtil.Dim
import java.io.InputStream

interface IMediaUtil {
    fun supportExtraDesktopImageFormats(): Boolean

    fun supportPdfConversion(): Boolean

    fun hasAvSupport(): Boolean

    fun isLandscape(lcsuffix: String, provider: IInputStreamProvider, index: Int): Boolean?

    fun getPdfConverter(): IPdfConverter?

    fun getImgConverter(): IImgConverter

    fun getImageDim(name: String, input: InputStream): Dim

    /// Render the specified page of the pdf file as JPEG data url.
    /// Return the url to the default pdf place holder image on error.
    /// ret.second is scaled dimension, ret.third is the original image dimension.
    fun pdfPoster(
        storage: IStorage,
        fileinfo: IFileInfo,
        page: Int,
        w: Int,
        h: Int,
        quality: Int,
    ): Triple<String, Dim, Dim>

    fun audioInfo(ret: JSONObject, fileinfo: IFileInfo): Boolean

    fun videoInfo(ret: JSONObject, fileinfo: IFileInfo): Boolean

    fun videoPoster(
        fileinfo: IFileInfo,
        time: Double,
        w: Int,
        h: Int,
        quality: Int,
    ): Triple<String, Dim, Dim>

    /// Read data url, pdf or image file specified by the url.
    /// Apply scale and effect as specified by the image output info.
    /// ret.second is scaled dimension, ret.third is the original image dimension.
    fun readUrlAsJpegDataUrl(
        storage: IStorage,
        outinfo: ImageOutputInfo,
        url: String
    ): IBotResult<Triple<String, Dim, Dim>, JSONObject>

    /// Read image or pdf file specified by the mime and the input stream.
    /// Apply scale and effect as specified by the image output info.
    /// ret.second is scaled dimension, ret.third is the original image dimension.
    fun readFileAsJpegDataUrl(
        storage: IStorage,
        outinfo: ImageOutputInfo,
        cropinfo: ImageCropInfo?,
        fileinfo: IFileInfo,
    ): IBotResult<Triple<String, Dim, Dim>, JSONObject>

    fun writeImage(storage: IStorage, outinfo: ImageOutputInfo, cropinfo: ImageCropInfo?, srcpath: String): JSONObject

    fun rewritePNG(rsrc: IResUtil, png: ByteArray): IByteSlice
}

interface IImgConverter {
    /// @param done(msg) Returns error message, "OK" string for OK.
    fun convert(
        storage: IStorage,
        dstinfo: IFileInfo,
        outinfo: ImageOutputInfo,
        srcinfo: IFileInfo,
        done: Fun10<String>
    )
}

interface IPdfConverter {
    /// @param done() Result is map of { page -> msg },
    /// where msg is the error message or "OK" if OK.
    fun convert(
        storage: IStorage,
        log: ITraceLogger,
        dstinfo: IFileInfo,
        outinfo: ImageOutputInfo,
        srcinfo: IFileInfo,
        done: Fun10<IBotResult<Map<String, String>, String>>
    )

    fun getPageCount(storage: IStorage, srcinfo: IFileInfo): Int?

    /// @param done(msg) Returns error message, "OK" string for OK.
    fun convert(
        storage: IStorage,
        dstinfo: IFileInfo,
        outinfo: ImageOutputInfo,
        srcinfo: IFileInfo,
        page: Int,
        done: Fun10<String>,
    )
}

open class ImageOutputInfo(
    val path: String,
    val width: Int,
    val height: Int,
    val rotation: Int,
    val effect: Int,
    val adjust: Double,
    val quality: Int,
) {
    fun resize(w: Int, h: Int): ImageOutputInfo {
        return ImageOutputInfo(path, w, h, rotation, effect, adjust, quality)
    }

    companion object {
        fun from(params: JSONArray, start: Int): IBotResult<ImageOutputInfo, Int> {
            var index = start
            return Without.throwableOrNull {
                val outpath =
                    params.stringOrNull(index++) ?: return@throwableOrNull BotResult.fail(R.string.DestinationPathIsRequired)
                val width = params.getInt(index++)
                val height = params.getInt(index++)
                val rotation = params.getInt(index++)
                val effect = params.getInt(index++)
                val adjust = params.getDouble(index++)
                val quality = params.optInt(index++, DEF.jpegQuality)
                BotResult.ok(ImageOutputInfo(outpath, width, height, rotation, effect, adjust, quality))
            } ?: BotResult.fail(R.string.InvalidArguments)
        }

        fun tn(width: Int, height: Int, quality: Int): ImageOutputInfo {
            return ImageOutputInfo("", width, height, 0, Effect.NONE, 0.0, quality)
        }

        fun ident(quality: Int = DEF.jpegQuality): ImageOutputInfo {
            return ImageOutputInfo("", 0, 0, 0, Effect.NONE, 0.0, quality)
        }
    }
}

open class ImageOutputInfoWithTn constructor(
    val outputInfo: ImageOutputInfo,
    val tnwidth: Int,
    val tnheight: Int
) {
    companion object {
        fun from(params: JSONArray, start: Int): IBotResult<ImageOutputInfoWithTn, Int> {
            return Without.throwableOrNull {
                val outinfo = ImageOutputInfo.from(params, start).let {
                    it.result() ?: return@throwableOrNull BotResult.fail(it.failure()!!)
                }
                val tnwidth = params.optInt(start + 7, -1)
                val tnheight = params.optInt(start + 8, -1)
                BotResult.ok(ImageOutputInfoWithTn(outinfo, tnwidth, tnheight))
            } ?: BotResult.fail(R.string.InvalidArguments)
        }
    }
}

open class ImageOutputInfoWithTnAndInput constructor(
    val outputTnInfo: ImageOutputInfoWithTn,
    val inputInfo: JSONObject?
) {
    companion object {
        fun from(params: JSONArray, start: Int): IBotResult<ImageOutputInfoWithTnAndInput, Int> {
            return Without.throwableOrNull {
                val outputinfo = ImageOutputInfoWithTn.from(params, start).let {
                    it.result() ?: return@throwableOrNull BotResult.fail(it.failure()!!)
                }
                val inputinfo = params.optJSONObject(start + 9)
                BotResult.ok(ImageOutputInfoWithTnAndInput(outputinfo, inputinfo))
            } ?: BotResult.fail(R.string.InvalidArguments)
        }
    }
}

open class ImageCropInfo constructor(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val rotation: Int,
    val zoom: Double,
) {
    companion object {
        /// w=0, h=0, rotation=0 and zoom=1.0 means no cropping.
        fun none() = ImageCropInfo(0, 0, 0, 0, 0, 1.0)

        fun from(args: JSONArray?, index: Int = 0): ImageCropInfo? {
            if (args == null || index >= args.length()) return null
            val x = args.optInt(index)
            val y = args.optInt(index + 1)
            val w = args.optInt(index + 2)
            val h = args.optInt(index + 3)
            val zoom = args.optDouble(index + 4, 1.0)
            val rotation = args.optInt(index + 5, 0)
            return ImageCropInfo(x, y, w, h, rotation, zoom)
        }
    }
}

class VideoInfo constructor(
    val codec: String,
    val duration: Double,
    val width: Int,
    val height: Int,
    val bitrate: Int,
) {
    override fun toString(): String {
        return "$codec, $duration, $width, $height, $bitrate"
    }
}

class AudioInfo constructor(
    val codec: String,
    val duration: Double,
    val channels: Int,
    val samplerate: Int,
    val bitrate: Int,
) {
    override fun toString(): String {
        return "$codec, $duration, $channels, $samplerate, $bitrate"
    }
}
