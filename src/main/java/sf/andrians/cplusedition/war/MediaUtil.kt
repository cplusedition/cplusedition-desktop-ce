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

import com.cplusedition.anjson.JSONUtil.findsJSONObjectNotNull
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun21
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.IByteRange
import com.cplusedition.bot.core.IByteSlice
import com.cplusedition.bot.core.IInputStreamProvider
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.MyByteOutputStream
import com.cplusedition.bot.core.NullOutputStream
import com.cplusedition.bot.core.ProcessUt
import com.cplusedition.bot.core.ProcessUtBuilder
import com.cplusedition.bot.core.TextUt
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.file
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Effect
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.An.PATH
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IStorage
import sf.andrians.cplusedition.support.MyCloseableProvider
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.AudioInfo
import sf.andrians.cplusedition.support.media.IImgConverter
import sf.andrians.cplusedition.support.media.IMediaUtil
import sf.andrians.cplusedition.support.media.IPdfConverter
import sf.andrians.cplusedition.support.media.ImageCropInfo
import sf.andrians.cplusedition.support.media.ImageOutputInfo
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.ImageUtil.Dim
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Mime
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import sf.andrians.cplusedition.support.media.MimeUtil.suffixFromMime
import sf.andrians.cplusedition.support.media.PdfConverterBase
import sf.andrians.cplusedition.support.media.VideoInfo
import sf.andrians.cplusedition.war.MediaUtil.outputDim
import sf.andrians.cplusedition.war.MediaUtil.scaleImageAndApplyEffect
import sf.andrians.cplusedition.war.MediaUtil.toPngBlob
import sf.andrians.cplusedition.war.MediaUtil.writeImage
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.regex.Pattern
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageInputStream
import javax.imageio.stream.MemoryCacheImageInputStream
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object MediaUtil : MediaUtilImpl()

open class MediaUtilBase {

    val webpSupport: IImageSupport?
    val heicSupport: IImageSupport?
    val pdfSupport: IPdfSupport?
    val avSupport: IAVSupport?

    private val effectSupport = EffectSupport()

    init {
        if (Without.exceptionOrFalse {
                ProcessUt.backtick("gm", "-version").startsWith("GraphicsMagick ")
            }) {
            webpSupport = GmWebpSupport()
            heicSupport = GmHeicSupport()
            pdfSupport = GmPdfSupport()
        } else if (Without.exceptionOrFalse {
                ProcessUt.backtick("convert", "-version").startsWith("Version: ImagicMagick ")
                        && ProcessUt.backtick("identify", "-version").startsWith("Version: ImagicMagick ")
            }) {
            webpSupport = ImWebpSupport()
            heicSupport = ImHeicSupport()
            pdfSupport = ImPdfSupport()
        } else {
            webpSupport = null
            heicSupport = null
            pdfSupport = null
        }
        if (Without.exceptionOrFalse {
                ProcessUt.backtick("ffmpeg", "-version").startsWith("ffmpeg version ")
                        && ProcessUt.backtick("ffprobe", "-version").startsWith("ffprobe version ")
            }) {
            avSupport = FfmpegSupport()
        } else {
            avSupport = null
        }
    }

    //////////////////////////////////////////////////////////////////

    fun getImageReader(mime: String?): ImageReader? {
        val it = ImageIO.getImageReadersByMIMEType(mime)
        while (it.hasNext()) {
            return it.next()
        }
        return null
    }

    fun getImageWriter(mime: String?): ImageWriter? {
        val it = ImageIO.getImageWritersByMIMEType(mime)
        while (it.hasNext()) {
            return it.next()
        }
        return null
    }

    /**
     * Use to determine image output dimension from user input.
     * If any of width or height is specified, fit image to the given dimension, keeping aspect ratio.
     * Otherwise, use iwidth and iheight.
     * @iwidth Image intrinsic width.
     * @iheight Image intrinsic height.
     * @width If <=0, derive width from height. If height <=0, use iwidth.
     * @height if <=0, derive height from width. If width <=0, use iheight.
     */
    fun outputDim(iwidth: Int, iheight: Int, width: Int, height: Int): Dim {
        return if (iwidth == width && iheight == height || width <= 0 && height <= 0) {
            ImageUtil.limitImageArea(iwidth, iheight)
        } else if (height <= 0) {
            ImageUtil.limitImageArea(width, (width * iheight * 1.0 / iwidth).roundToInt())
        } else if (width <= 0) {
            ImageUtil.limitImageArea(((height * iwidth * 1.0) / iheight).roundToInt(), height)
        } else {
            ImageUtil.fit(iwidth, iheight, width, height)
        }
    }

    @Throws(IOException::class)
    fun getImageDim(res: IResUtil, file: File): Dim {
        return try {
            getImageDim1(file)
        } catch (e: Throwable) {
            throw IOException(res.get(R.string.ImageReadFailed_, file.name))
        }
    }

    @Throws(IOException::class)
    fun getImageDim1(file: File): Dim {
        val name = file.name
        val mime = MimeUtil.imageMimeFromPath(name) ?: throw IOException()
        val r = getImageReader(mime) ?: throw IOException()
        FileImageInputStream(file).use {
            r.input = it
            val width = r.getWidth(0)
            val height = r.getHeight(0)
            return Dim(width, height)
        }
    }

    /// Note outinfo.parentFile must already exists.
    @Throws(Exception::class)
    fun writeImage(
        storage: IStorage,
        outinfo: IFileInfo,
        omime: String,
        quality: Int,
        image: BufferedImage
    ) {
        val rsrc = storage.rsrc
        if (omime == Mime.WEBP) {
            if (webpSupport == null)
                throw IOException(rsrc.get(R.string.UnsupportedOutputFormat_, omime))
            if (!outinfo.mkparent())
                throw IOException(rsrc.get(R.string.CreateParentDirectoryFailed_, outinfo.cpath))
            webpSupport.writeImage(storage, outinfo, quality, image)
        } else if (omime == Mime.HEIC) {
            if (heicSupport == null)
                throw IOException(rsrc.get(R.string.UnsupportedOutputFormat_, omime))
            if (!outinfo.mkparent())
                throw IOException(rsrc.get(R.string.CreateParentDirectoryFailed_, outinfo.cpath))
            heicSupport.writeImage(storage, outinfo, quality, image)
        } else {
            val writer = getImageWriter(omime)
                ?: throw IOException(rsrc.get(R.string.UnsupportedOutputFormat_, omime))
            if (!outinfo.mkparent())
                throw IOException(rsrc.get(R.string.CreateParentDirectoryFailed_, outinfo.cpath))
            outinfo.content().outputStream().use {
                compressImage(it, writer, omime, quality, image)
            }
        }
    }

    @Throws
    fun toJpegDataUrl(quality: Int, image: BufferedImage): String {
        return ImageUtil.toJpegDataUrl(toJpegBlob(quality, image).byteBuffer())
    }

    @Throws
    fun toPngDataUrl(quality: Int, image: BufferedImage): String {
        return ImageUtil.toPngDataUrl(toPngBlob(quality, image).byteBuffer())
    }

    @Throws
    fun toJpegBlob(quality: Int, image: BufferedImage): MyByteOutputStream {
        return MyByteOutputStream().use {
            compressImage(it, getImageWriter(Mime.JPEG)!!, Mime.JPEG, quality, image)
            it
        }
    }

    @Throws
    fun toPngBlob(quality: Int, image: BufferedImage): MyByteOutputStream {
        return MyByteOutputStream().use {
            compressImage(it, getImageWriter(Mime.PNG)!!, Mime.PNG, quality, image)
            it
        }
    }

    private fun compressImage(
        output: OutputStream,
        writer: ImageWriter,
        omime: String,
        quality: Int,
        image: BufferedImage
    ) {
        assert(omime != Mime.WEBP)
        ImageIO.createImageOutputStream(output).use {
            writer.output = it
            val params = writer.defaultWriteParam
            if (omime == Mime.JPEG) {
                params.compressionMode = ImageWriteParam.MODE_EXPLICIT
                params.compressionQuality = quality / 100f
                params.compressionType = "JPEG"
            }
            writer.write(null, IIOImage(image, null, null), params)
        }
    }

    /// @param outwidth Output width before rotation
    /// @param outheight Output height before rotation
    fun scaleImageAndApplyEffect(
        outinfo: ImageOutputInfo,
        omime: String?,
        imime: String?,
        image: BufferedImage,
    ): BufferedImage {
        if (outinfo.width == image.width
            && outinfo.height == image.height
            && outinfo.rotation == 0
            && outinfo.effect == Effect.NONE
            && omime == imime
        ) return image
        val isjpeg = (Mime.JPEG == omime)
        val swap = (outinfo.rotation != 0) && (outinfo.rotation != 180)
        val iwidth = image.width
        val iheight = image.height
        val dim = outputDim(iwidth, iheight, outinfo.width, outinfo.height)
        val w = if (swap) dim.y else dim.x
        val h = if (swap) dim.x else dim.y
        val output = BufferedImage(w, h, if (isjpeg) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB)
        val g = output.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        if (isjpeg) {
            g.background = Color(0xff, 0xff, 0xff, 0xff)
            g.fillRect(0, 0, w, h)
        }
        rotate(g, outinfo.rotation, dim.x, dim.y)
        drawImage(g) { g.drawImage(image, 0, 0, dim.x, dim.y, it) }
        if (outinfo.rotation != 0) {
            
        }
        if (w != image.width || h != image.height) {
            
        }
        fun applyEffect(effect: Int, adjust: Double, bitmap: BufferedImage): BufferedImage {
            when (effect) {
                Effect.GRAY2, Effect.GRAY4, Effect.GRAY8, Effect.GRAY16, Effect.GRAY256 -> {
                    effectSupport.grayscaleEffect(bitmap, effect, adjust)
                }
            }
            return bitmap
        }
        return applyEffect(outinfo.effect, outinfo.adjust, output)
    }

    @Throws(Exception::class)
    fun cropImage(
        image: BufferedImage,
        cropinfo: ImageCropInfo?,
        opaque: Boolean = false
    ): BufferedImage {
        if (cropinfo == null
            || cropinfo.zoom == 1.0 && cropinfo.rotation == 0 && cropinfo.w == 0 && cropinfo.h == 0
        ) return image
        val output = BufferedImage(
            cropinfo.w, cropinfo.h, (if (opaque) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB)
        )
        val g = output.createGraphics()
        if (opaque) {
            g.background = Color.WHITE
            g.fillRect(0, 0, cropinfo.w, cropinfo.h)
        }
        g.translate(-cropinfo.x, -cropinfo.y)
        g.scale(cropinfo.zoom, cropinfo.zoom)
        rotate(g, cropinfo.rotation, image.width, image.height)
        drawImage(g) {
            if (opaque) g.drawImage(image, 0, 0, image.width, image.height, Color.WHITE, it)
            else g.drawImage(image, 0, 0, image.width, image.height, it)
        }
        return output
    }

    /// @param w Output width before rotation
    /// @param h Output height before rotation
    private fun rotate(g: Graphics2D, rotation: Int, w: Int, h: Int) {
        val radian = Math.toRadians(rotation.toDouble())
        val swap = (rotation != 0) && (rotation != 180)
        val ow = if (swap) h else w
        val oh = if (swap) w else h
        when (rotation) {
            0 -> {
            }

            180 -> {
                g.rotate(radian, ow * 0.5, oh * 0.5)
            }

            90 -> {
                g.rotate(radian, ow.toDouble(), 0.0)
                g.translate(ow, 0)
            }

            270 -> {
                g.rotate(radian, 0.0, oh.toDouble())
                g.translate(0, oh)
            }

            else -> throw IOException("$rotation")
        }
    }

    fun drawImage(g: Graphics2D, callback: Fun11<ImageObserver, Boolean>) {
        val complete = CountDownLatch(1)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        if (callback { _, infoflags, _, _, _, _ ->
                val done = infoflags and (ImageObserver.ALLBITS or ImageObserver.ERROR or ImageObserver.ABORT) != 0
                if (done) {
                    complete.countDown()
                }
                !done
            }) {
            complete.countDown()
        }
        if (!complete.await(Support.Def.scaleImageTimeout, TimeUnit.MILLISECONDS)) {
            throw TimeoutException()
        }
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun readSVGDimension(data: InputStream?): Dim? {
        val nspat = Pattern.compile("(?s)^https?://www\\.w3\\.org/.*/svg$")
        val sizeunitpat = Pattern.compile("(?s)^\\s*(\\d+)\\s*(\\S*)\\s*$")
        val viewboxpat = Pattern.compile("(?s)^\\s*[\\d.]+\\s+[\\d.]+\\s+([\\d.]+)\\s+([\\d.]+)\\s*$")
        if (data == null) return null
        val factory = SAXParserFactory.newInstance()
        factory.isValidating = false
        factory.isNamespaceAware = true
        val parser = factory.newSAXParser()
        val found = booleanArrayOf(false)
        val isroot = booleanArrayOf(true)
        val level = intArrayOf(0)
        var result: Dim? = null
        parser.parse(data, object : DefaultHandler() {
            @Throws(SAXException::class)
            override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                if ("svg" == localName && nspat.matcher(uri).matches()) {
                    level[0] += 1
                    if (isroot[0] && level[0] == 1) {
                        found[0] = true
                        val viewbox = attributes.getValue(uri, "viewBox")
                        if (viewbox != null) {
                            val m = viewboxpat.matcher(viewbox)
                            if (m.matches()) {
                                try {
                                    val ws = m.group(1)
                                    val hs = m.group(2)
                                    val w = ws.toInt()
                                    val h = hs.toInt()
                                    result = Dim(w, h)
                                } catch (e: Throwable) {
                                }
                            }
                        }
                        val width = attributes.getValue(uri, "width")
                        val height = attributes.getValue(uri, "height")
                        val wm = sizeunitpat.matcher(width)
                        val hm = sizeunitpat.matcher(height)
                        if (wm.matches() && hm.matches()) {
                            if (wm.group(2) == hm.group(2)) {
                                try {
                                    val ws = wm.group(1)
                                    val hs = hm.group(1)
                                    val w = ws.toInt()
                                    val h = hs.toInt()
                                    val ww = if (w > h) 512 else 512 * w / h
                                    val hh = if (w > h) 512 * h / w else 512
                                    
                                    result = Dim(ww, hh)
                                } catch (e: Throwable) {
                                }
                            }
                        }
                    }
                }
                isroot[0] = false
            }

            @Throws(SAXException::class)
            override fun endElement(uri: String, localName: String, qName: String) {
                if ("svg" == localName && nspat.matcher(uri).matches()) {
                    level[0] -= 1
                }
            }
        })
        return if (found[0] && level[0] == 0) result else null
    }

    @Throws(Exception::class)
    fun readSVGDimension(provider: IInputStreamProvider): Dim? {
        provider.inputStream().use { input ->
            val (width, height) = readSVGDimension(input) ?: return null
            return if (width > 0 && height > 0) Dim(width, height) else null
        }
    }

    @Throws(Exception::class)
    fun asBytes(provider: IInputStreamProvider): ByteArray {
        return provider.inputStream().use { it.readBytes() }
    }

    fun readDataurl(dataurl: String): BufferedImage? {
        return Without.throwableOrNull {
            if (!dataurl.startsWith("data:")) return@throwableOrNull null
            val mime = ImageUtil.mimeOfDataUrl(dataurl) ?: return@throwableOrNull null
            val index = dataurl.indexOf(";base64,")
            if (index < 0) return@throwableOrNull null
            val data = Base64.getDecoder().decode(dataurl.substring(index + 8))
            data.inputStream().use { readImage(it, mime) }
        }
    }

    fun readImage(file: IFileInfo, mime: String): BufferedImage? {
        return Without.throwableOrNull {
            file.content().inputStream().use { input ->
                readImage(input, mime)
            }
        }
    }

    fun readImage(input: InputStream, mime: String): BufferedImage? {
        return Without.throwableOrNull {
            if (mime == Mime.WEBP) webpSupport?.readImage(input)
            else if (mime == Mime.HEIC) heicSupport?.readImage(input)
            else ImageIO.read(input)
        }
    }

    fun readImageAsJpeg(file: IFileInfo, mime: String, quality: Int): IByteRange? {
        return Without.throwableOrNull {
            file.content().inputStream().use { input ->
                readImageAsJpeg(input, mime, quality)
            }
        }
    }

    fun readImageAsJpeg(input: InputStream, mime: String, quality: Int): IByteRange? {
        return Without.throwableOrNull {
            if (mime == Mime.WEBP) webpSupport?.readImageAsJpeg(input, quality)
            else if (mime == Mime.HEIC) heicSupport?.readImageAsJpeg(input, quality)
            else ImageIO.read(input)?.let { toJpegBlob(quality, it) }
        }
    }

    interface IMagicCmdSupport {
        fun identify(vararg args: String): ProcessUtBuilder
        fun convert(vararg args: String): ProcessUtBuilder
    }

    interface IffmpegCmdSupport {
        fun audioInfo(fileinfo: IFileInfo): AudioInfo?
        fun videoInfo(fileinfo: IFileInfo): VideoInfo?
        fun poster(vararg args: String): ProcessUtBuilder
    }

    interface IImageSupport {
        fun readImageDim(input: InputStream): Dim?
        fun readImage(input: InputStream): BufferedImage?
        fun readImageAsJpeg(input: InputStream, quality: Int): IByteRange?
        @Throws
        fun writeImage(storage: IStorage, outinfo: IFileInfo, quality: Int, image: BufferedImage)
        @Throws(Exception::class)
        fun cropImage(
            outinfo: IFileInfo,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            quality: Int,
            image: BufferedImage
        ): String
    }

    interface IPdfSupport {
        fun readPdfAsImage(
            fileinfo: IFileInfo,
            page: Int,
            width: Int,
            height: Int,
            scale: Double
        ): BufferedImage?

        /// @return ret.second is scaled dimension, ret.third is original image dimension.
        fun readPdfAsDataUrl(
            fileinfo: IFileInfo,
            page: Int,
            outmime: String,
            quality: Int,
            width: Int,
            height: Int,
            scale: Double,
        ): Triple<String, Dim, Dim>?

        /// @return ret.second is scaled dimension, ret.third is original image dimension.
        fun <V> readPdf(
            fileinfo: IFileInfo,
            page: Int,
            outmime: String,
            quality: Int,
            width: Int,
            height: Int,
            scale: Double,
            callback: Fun21<InputStream, String, V>,
        ): Triple<V, Dim, Dim>?

        fun readPdfPageDims(input: InputStream, scale: Double): List<Dim>?
    }

    interface IAVSupport {
        /// @return poster, scaled dimension, original dimension.
        fun audioInfo(fileinfo: IFileInfo): AudioInfo?
        fun videoInfo(fileinfo: IFileInfo): VideoInfo?
        fun posterAsJpeg(fileinfo: IFileInfo, time: Double, quality: Int, width: Int, height: Int): Triple<IByteRange, Dim, Dim>?
    }

    class GmCmdSupport : IMagicCmdSupport {
        override fun identify(vararg args: String): ProcessUtBuilder {
            return ProcessUtBuilder("gm", "identify", *args)
        }

        override fun convert(vararg args: String): ProcessUtBuilder {
            return ProcessUtBuilder("gm", "convert", *args)
        }
    }

    class ImCmdSupport : IMagicCmdSupport {
        override fun identify(vararg args: String): ProcessUtBuilder {
            return ProcessUtBuilder("identify", *args)
        }

        override fun convert(vararg args: String): ProcessUtBuilder {
            return ProcessUtBuilder("convert", *args)
        }
    }

    class FfmpegCmdSupport : IffmpegCmdSupport {
        companion object {
            val input = Regex("(?m)^\\s*Input\\s+.*:$")
            val duration = Regex("(?m)^\\s*Duration:\\s+([:.\\d]+),.*$")
            val resolution = Regex("(?m)^\\s*Stream\\s+.*:\\s+Video:\\s+.*,\\s+(\\d+x\\d+),.*$")
        }

        override fun poster(vararg args: String): ProcessUtBuilder {
            return ProcessUtBuilder("ffmpeg", *args)
        }

        override fun videoInfo(fileinfo: IFileInfo): VideoInfo? {
            return avinfo(fileinfo)?.findsJSONObjectNotNull { _, stream ->
                if (stream.stringOrNull("codec_type") == "video") {
                    val codec = stream.stringOrNull("codec_name")
                    val duration = stream.stringOrNull("duration")?.let { java.lang.Double.parseDouble(it) }
                    val w = stream.optInt("width", 0)
                    val h = stream.optInt("height", 0)
                    val bitrate = stream.stringOrNull("bit_rate")?.let { java.lang.Double.parseDouble(it).roundToInt() }
                    if (codec != null && duration != null && w > 0 && h > 0 && bitrate != null)
                        VideoInfo(codec, duration, w, h, bitrate)
                    else null
                } else null
            }
        }

        override fun audioInfo(fileinfo: IFileInfo): AudioInfo? {
            return avinfo(fileinfo)?.findsJSONObjectNotNull { _, stream ->
                if (stream.stringOrNull("codec_type") == "audio") {
                    val codec = stream.stringOrNull("codec_name")
                    val duration = stream.stringOrNull("duration")?.let { java.lang.Double.parseDouble(it) }
                    val channels = stream.optInt("channels", 0)
                    val samplerate = stream.stringOrNull("sample_rate")?.let { java.lang.Double.parseDouble(it).roundToInt() }
                    val bitrate = stream.stringOrNull("bit_rate")?.let { java.lang.Double.parseDouble(it).roundToInt() }
                    if (codec != null && duration != null && channels > 0 && samplerate != null && bitrate != null)
                        AudioInfo(codec, duration, channels, samplerate, bitrate)
                    else null
                } else null
            }
        }

        private fun avinfo(fileinfo: IFileInfo): JSONArray? {
            val root = fileinfo.root.file ?: return null
            return Without.throwableOrNull {
                val json = MyByteOutputStream().use { out ->
                    ProcessUtBuilder(
                        "ffprobe",
                        "-of", "json",
                        "-show_streams",
                        "-i", root.file(fileinfo.rpath).absolutePath,
                    ).asyncOrFail(out) {
                        out
                    }.get()
                }.toByteArray().inputStream().reader().readText()
                JSONObject(json).optJSONArray("streams")
            }
        }
    }

    class GmWebpSupport : AbstractWebpSupport(GmCmdSupport())
    class GmHeicSupport : AbstractHeicSupport(GmCmdSupport())
    class GmPdfSupport : AbstractPdfSupport(GmCmdSupport())
    class ImWebpSupport : AbstractWebpSupport(ImCmdSupport())
    class ImHeicSupport : AbstractHeicSupport(ImCmdSupport())
    class ImPdfSupport : AbstractPdfSupport(ImCmdSupport())
    class FfmpegSupport : AbstractAVSupport(FfmpegCmdSupport())

    abstract class AbstractWebpSupport(private val support: IMagicCmdSupport) : IImageSupport {
        override fun readImageDim(input: InputStream): Dim? {
            return Without.throwableOrNull {
                val output = MyByteOutputStream().use { out ->
                    support.identify("-format", "%w,%h", "-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) { out }
                        .get().toString()
                }
                val match = Regex("\\s*(\\d+),(\\d+)\\s*").matchEntire(output) ?: return@throwableOrNull null
                val w = TextUt.parseInt(match.groupValues[1], -1)
                val h = TextUt.parseInt(match.groupValues[2], -1)
                if (w >= 0 && h >= 0) Dim(w, h) else null
            }
        }

        override fun readImage(input: InputStream): BufferedImage? {
            return Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.convert("-quality", "100", "webp:-", "png:-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) {
                            out.inputStream().use {
                                ImageIO.read(it)
                            }
                        }.get()
                }
            }
        }

        override fun readImageAsJpeg(input: InputStream, quality: Int): IByteRange? {
            return Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.convert("-quality", "$quality", "webp:-", "jpeg:-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) {
                            out
                        }.get()
                }
            }
        }

        @Throws
        override fun writeImage(storage: IStorage, outinfo: IFileInfo, quality: Int, image: BufferedImage) {
            val extra = 128
            val width = image.width
            val height = image.height
            fun divof(value: Int): Int {
                var div = 1
                while (value / div > 16000) ++div
                return div
            }

            val wdiv = divof(width)
            val hdiv = divof(height)
            if (wdiv == 1 && hdiv == 1) {
                writeImage1(storage, outinfo, quality, image)?.let {
                    throw IOException()
                }
                return
            }
            val w = width / wdiv
            val h = height / hdiv
            for (iw in 0 until wdiv) {
                for (ih in 0 until hdiv) {
                    val x = width * iw / wdiv
                    val y = height * ih / hdiv
                    val ww = min(w + extra, width - x)
                    val hh = min(h + extra, height - y)
                    val s = if (wdiv > 1 && hdiv > 1) {
                        if (x == 0 && y == 0) "" else "@$x,$y"
                    } else if (hdiv > 1) {
                        if (y == 0) "" else "@$y"
                    } else {
                        if (x == 0) "" else "@$x"
                    }
                    val out = if (s.isEmpty()) outinfo
                    else outinfo.root.fileInfo(
                        Basepath.changeStem(
                            outinfo.rpath, Basepath.stem(outinfo.name) + s
                        )
                    )
                    cropImage(out, x, y, ww, hh, quality, image)
                }
            }
        }

        @Throws(Exception::class)
        private
        fun writeImage1(storage: IStorage, outinfo: IFileInfo, quality: Int, image: BufferedImage): String? {
            return toPngBlob(100, image).inputStream().use { input ->
                MyByteOutputStream().use { out ->
                    ByteArrayOutputStream().use { err ->
                        support.convert(
                            "-quality",
                            "$quality",
                            "-define", "webp:lossless=false",
                            "png:-",
                            "webp:-"
                        )
                            .input(input)
                            .async(out, err) { rc ->
                                outinfo.content().outputStream().use { output ->
                                    if (rc != 0) {
                                        output.write(byteArrayOf())
                                        return@async out.toString() + err.toString()
                                    }
                                    out.inputStream().use {
                                        FileUt.copy(output, it)
                                    }
                                }
                                null
                            }.get()
                    }
                }
            }
        }

        @Throws(Exception::class)
        override fun cropImage(
            outinfo: IFileInfo,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            quality: Int,
            image: BufferedImage
        ): String {
            return toPngBlob(100, image).inputStream().use { input ->
                MyByteOutputStream().use { out ->
                    ByteArrayOutputStream().use { err ->
                        support.convert(
                            "-quality",
                            "$quality",
                            "-define", "webp:lossless=false",
                            "-crop", "${width}x$height+$x+$y",
                            "png:-",
                            "webp:-"
                        )
                            .input(input)
                            .async(out, err) { rc ->
                                if (rc != 0) {
                                    outinfo.content().write(byteArrayOf())
                                    return@async out.toString() + err.toString()
                                }
                                out.inputStream().use {
                                    outinfo.content().write(it)
                                }
                                ""
                            }.get()
                    }
                }
            }
        }
    }

    abstract class AbstractHeicSupport(private val support: IMagicCmdSupport) : IImageSupport {
        override fun readImageDim(input: InputStream): Dim? {
            return Without.throwableOrNull {
                val output = MyByteOutputStream().use { out ->
                    support.identify("-format", "%w,%h", "-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) { out }
                        .get().toString()
                }
                val match = Regex("\\s*(\\d+),(\\d+)\\s*").matchEntire(output) ?: return@throwableOrNull null
                val w = TextUt.parseInt(match.groupValues[1], -1)
                val h = TextUt.parseInt(match.groupValues[2], -1)
                if (w >= 0 && h >= 0) Dim(w, h) else null
            }
        }

        override fun readImage(input: InputStream): BufferedImage? {
            return Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.convert("-quality", "100", "heic:-", "png:-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) {
                            out.inputStream().use {
                                ImageIO.read(it)
                            }
                        }.get()
                }
            }
        }

        override fun readImageAsJpeg(input: InputStream, quality: Int): IByteRange? {
            return Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.convert("-quality", "100", "heic:-", "jpeg:-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) {
                            out
                        }.get()
                }
            }
        }

        @Throws
        override fun writeImage(storage: IStorage, outinfo: IFileInfo, quality: Int, image: BufferedImage) {
            writeImage1(storage, outinfo, quality, image)?.let {
                throw IOException()
            }
        }

        @Throws(Exception::class)
        private
        fun writeImage1(storage: IStorage, outinfo: IFileInfo, quality: Int, image: BufferedImage): String? {
            return toPngBlob(100, image).inputStream().use { input ->
                MyByteOutputStream().use { out ->
                    ByteArrayOutputStream().use { err ->
                        support.convert(
                            "-quality",
                            "$quality",
                            "png:-",
                            "heic:-"
                        )
                            .input(input)
                            .async(out, err) { rc ->
                                outinfo.content().outputStream().use { output ->
                                    if (rc != 0) {
                                        output.write(byteArrayOf())
                                        return@async out.toString() + err.toString()
                                    }
                                    out.inputStream().use {
                                        FileUt.copy(output, it)
                                    }
                                }
                                null
                            }.get()
                    }
                }
            }
        }

        @Throws(Exception::class)
        override fun cropImage(
            outinfo: IFileInfo,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            quality: Int,
            image: BufferedImage
        ): String {
            return toPngBlob(100, image).inputStream().use { input ->
                MyByteOutputStream().use { out ->
                    ByteArrayOutputStream().use { err ->
                        support.convert(
                            "-quality",
                            "$quality",
                            "-crop", "${width}x$height+$x+$y",
                            "png:-",
                            "heic:-"
                        )
                            .input(input)
                            .async(out, err) { rc ->
                                if (rc != 0) {
                                    outinfo.content().write(byteArrayOf())
                                    return@async out.toString() + err.toString()
                                }
                                out.inputStream().use {
                                    outinfo.content().write(it)
                                }
                                ""
                            }.get()
                    }
                }
            }
        }
    }

    /// @param time Preferred time to extract the poster. For video with short duration, extract at duration / 2 instead.
    abstract class AbstractAVSupport(private val support: IffmpegCmdSupport) : IAVSupport {
        override fun audioInfo(fileinfo: IFileInfo): AudioInfo? {
            return support.audioInfo(fileinfo)
        }

        override fun videoInfo(fileinfo: IFileInfo): VideoInfo? {
            return support.videoInfo(fileinfo)
        }

        override fun posterAsJpeg(
            fileinfo: IFileInfo,
            time: Double,
            quality: Int,
            width: Int,
            height: Int
        ): Triple<IByteRange, Dim, Dim>? {
            val root = fileinfo.root.file ?: return null
            val info = support.videoInfo(fileinfo) ?: return null
            val duration = info.duration
            val at = if (time > duration - 1.0) {
                if (duration >= 1.0) duration - 1.0 else 0
            } else time
            val (w, h) = ImageUtil.fit(info.width, info.height, width, height)
            val ret = Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.poster(
                        "-ss", at.toString(),
                        "-i", root.file(fileinfo.rpath).absolutePath,
                        "-frames", "1",
                        "-s", "${w}x$h",
                        "-c:v", "mjpeg",
                        "-f", "image2",
                        "-"
                    ).asyncOrFail(out) {
                        out
                    }
                }.get()
            }
            return ret?.let {
                Triple(it, Dim(w, h), Dim(info.width, info.height))
            }
        }
    }

    abstract class AbstractPdfSupport(private val support: IMagicCmdSupport) : IPdfSupport {
        override fun readPdfAsImage(
            fileinfo: IFileInfo,
            page: Int,
            width: Int,
            height: Int,
            scale: Double,
        ): BufferedImage? {
            return readPdf(fileinfo, page, Mime.PNG, 100, width, height, scale) { input, mime ->
                MediaUtil.readImage(input, mime)
            }?.first
        }

        override fun readPdfAsDataUrl(
            fileinfo: IFileInfo,
            page: Int,
            outmime: String,
            quality: Int,
            width: Int,
            height: Int,
            scale: Double,
        ): Triple<String, Dim, Dim>? {
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            return Without.throwableOrNull X@{
                val ret = MyByteOutputStream().use { out ->
                    readPdf(fileinfo, page, outmime, quality, width, height, scale) { input, _ ->
                        IOUt.copyAll(buf, input) {
                            if (it > 0) {
                                out.write(buf, 0, it)
                            }
                        }
                        out
                    }
                } ?: return@X null
                val dataurl = ImageUtil.toDataUrl(outmime, ret.first.byteBuffer())
                return@X Triple(dataurl, ret.second, ret.third)
            }
        }

        override fun <V> readPdf(
            fileinfo: IFileInfo,
            page: Int,
            outmime: String,
            quality: Int,
            width: Int,
            height: Int,
            scale: Double,
            callback: Fun21<InputStream, String, V>,
        ): Triple<V, Dim, Dim>? {
            fun readPdfPageDim(page: Int, scale: Double): Dim? {
                val buf = fileinfo.content().inputStream().use { input ->
                    MyByteOutputStream().use { out ->
                        support.identify("-format", "%P", "pdf:-[$page]")
                            .input(input)
                            .asyncOrFail(out, NullOutputStream()) {
                                out
                            }.get()
                    }
                }
                return parseDim(IOUt.readText(buf.inputStream()), scale)
            }
            try {
                val pdfdim = readPdfPageDim(page, scale)
                    ?: return null
                val dim = outputDim(pdfdim.x, pdfdim.y, width, height)
                val ext = suffixFromMime(outmime)?.let {
                    if (it.isNotEmpty()) it.substring(1) else null
                } ?: return null
                val result = fileinfo.content().inputStream().use { input ->
                    support.convert(
                        "-quality", "$quality",
                        "-density", "${DEF.pdfDpi}x${DEF.pdfDpi}",
                        "-geometry", "${dim.x}x${dim.y}",
                        "pdf:-[$page]", "$ext:-"
                    )
                        .input(input)
                        .pipe {
                            callback(it, outmime)
                        }.get()
                }
                return if (result == null) null else Triple(result, dim, pdfdim)
            } catch (e: Throwable) {
                
                return null
            }
        }

        private val dimpat = Regex("(\\d+)x(\\d+)")

        fun parseDim(line: String, scale: Double): Dim? {
            val match = dimpat.matchEntire(line.trim()) ?: return null
            return Without.exceptionOrNull {
                Dim(
                    (Integer.parseInt(match.groupValues[1]) * scale).toInt(),
                    (Integer.parseInt(match.groupValues[2]) * scale).toInt()
                )
            }
        }

        override fun readPdfPageDims(input: InputStream, scale: Double): List<Dim>? {
            return Without.throwableOrNull {
                MyByteOutputStream().use { out ->
                    support.identify("-format", "%P\\n", "pdf:-")
                        .input(input)
                        .asyncOrFail(out, NullOutputStream()) {
                            IOUt.readLines(out.inputStream()).mapNotNull {
                                parseDim(it, scale)
                            }.toList()
                        }.get()
                }
            }
        }
    }

    class PdfConverter(
        val pdfsupport: IPdfSupport,
    ) : PdfConverterBase(), IPdfConverter {

        override fun getPageCount(storage: IStorage, srcinfo: IFileInfo): Int? {
            return srcinfo.content().inputStream().use {
                pdfsupport.readPdfPageDims(it, DEF.pdfScale)?.size
            }
        }

        override fun convert(
            storage: IStorage,
            dstinfo: IFileInfo,
            outinfo: ImageOutputInfo,
            srcinfo: IFileInfo,
            page: Int,
            done: Fun10<String>,
        ) {
            try {
                val outsuffix = dstinfo.lcSuffix
                val omime = MimeUtil.imageMimeFromLcSuffix(outsuffix)
                    ?: return done(storage.rsrc.get(R.string.UnsupportedOutputFormat_, outsuffix))
                return done(ImgConverter.convert(storage, dstinfo, outinfo, srcinfo, omime, Mime.PDF) { pdf ->
                    pdfsupport.readPdfAsImage(pdf, page, outinfo.width, outinfo.height, DEF.pdfScale)
                })
            } catch (e: Throwable) {
                return done(storage.rsrc.get(R.string.ImageWriteFailed_, srcinfo.name))
            }
        }

        /// @page Page number from 0.
        private fun getdstname(stem: String, page: Int, suffix: String): String {
            return TextUt.format("%s-%04d%s", stem, page + 1, suffix)
        }
    }

    private class EffectSupport {
        @Throws(Exception::class)
        fun grayscaleEffect(bitmap: BufferedImage, effect: Int, adjust: Double) {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getRGB(0, 0, width, height, pixels, 0, width)
            var i = 0
            val len = pixels.size
            while (i < len) {
                val c = pixels[i]
                val y = lum(c)
                pixels[i] = c and -0x1000000 or (y shl 16) or (y shl 8) or y
                ++i
            }
            fun blackandwhite(adjust: Double) {
                val q = ImageUtil.MyBlackOnWhitePosterizer(adjust)
                var offset = 0
                while (offset < len) {
                    pixels[offset] = q.quantize(pixels[offset])
                    ++offset
                }
            }
            when (effect) {
                Effect.GRAY2 -> blackandwhite(adjust)

                Effect.GRAY4,
                Effect.GRAY8,
                Effect.GRAY16
                -> {
                    val levels = if (effect == Effect.GRAY4) 4 else if (effect == Effect.GRAY8) 8 else 16
                    val q = ImageUtil.MyGrayPosterizer(pixels, levels, adjust, null)
                    if (q.levels().size <= 1) {
                        when (effect) {
                            Effect.GRAY16 -> blackandwhite(adjust * 8)
                            Effect.GRAY8 -> blackandwhite(adjust * 4)
                            else -> blackandwhite(adjust * 2)
                        }
                    } else {
                        for (offset in pixels.indices) {
                            pixels[offset] = q.quantize(pixels[offset])
                        }
                    }
                }

                Effect.GRAY256 -> {
                    val t = (adjust * 2.55).roundToInt()
                    val threshold = (min(max(t, 0), 255))
                    for (offset in pixels.indices) {
                        val value = pixels[offset]
                        if ((value and 0xff) > threshold) {
                            pixels[offset] = value or 0xffffff
                        }
                    }
                }

                else -> throw AssertionError("$effect")
            }
            bitmap.setRGB(0, 0, width, height, pixels, 0, width)
        }

        ///#BEGIN TODO
        ///#END

        fun lum(rgb: Int): Int {
            val r = (rgb shr 16 and 0xff).toDouble()
            val g = (rgb shr 8 and 0xff).toDouble()
            val b = (rgb and 0xff).toDouble()
            val y = r * 0.2126 + g * 0.7152 + b * 0.0722
            return y.roundToInt()
        }
    }
}

class ImgConverter : IImgConverter {

    override fun convert(
        storage: IStorage,
        dstinfo: IFileInfo,
        outinfo: ImageOutputInfo,
        srcinfo: IFileInfo,
        done: Fun10<String>
    ) {
        val msg = run {
            val omime = MimeUtil.imageMimeFromLcSuffix(dstinfo.lcSuffix)
                ?: return@run storage.rsrc.get(R.string.UnsupportedOutputFormat_, srcinfo.name)
            val imime = MimeUtil.imageMimeFromLcSuffix(srcinfo.lcSuffix)
                ?: return@run storage.rsrc.get(R.string.UnsupportedInputFormat_, srcinfo.name)
            return@run convert(storage, dstinfo, outinfo, srcinfo, omime, imime) {
                MediaUtil.readImage(it, imime)
            }
        }
        done(msg)
    }

    companion object {
        fun convert(
            storage: IStorage,
            dst: IFileInfo,
            outinfo: ImageOutputInfo,
            src: IFileInfo,
            omime: String?,
            imime: String,
            provider: Fun11<IFileInfo, BufferedImage?>
        ): String {
            val rsrc = storage.rsrc
            val timestamp = src.stat()!!.lastModified
            try {
                if (omime == null) return rsrc.get(R.string.UnsupportedOutputFormat_, dst.name)
                val writers = ImageIO.getImageWritersBySuffix(
                    if (omime == Mime.WEBP) Suffix.PNG.substring(1) else dst.lcSuffix.substring(1)
                )
                if (!writers.hasNext()) {
                    return rsrc.get(R.string.UnsupportedOutputFormat_, dst.name)
                }
                val image = storage.disk(src.cpath, MyCloseableProvider()).use {
                    provider(src)
                        ?: return storage.rsrc.get(R.string.ReadFailed_, src.name)
                }
                val outdim = MediaUtil.outputDim(image.width, image.height, outinfo.width, outinfo.height)
                val scaled = scaleImageAndApplyEffect(
                    outinfo.resize(outdim.x, outdim.y),
                    omime,
                    imime,
                    image,
                )
                storage.disk(dst.cpath, MyCloseableProvider()).use {
                    writeImage(storage, dst, omime, outinfo.quality, scaled)
                    dst.setLastModified(timestamp)
                }
                return "OK"
            } catch (e: Throwable) {
                
                return rsrc.get(R.string.ImageWriteFailed_, src.name)
            }
        }

    }
}

open class MediaUtilImpl : MediaUtilBase(), IMediaUtil {

    override fun supportPdfConversion(): Boolean {
        return pdfSupport != null
    }

    override fun supportExtraDesktopImageFormats(): Boolean {
        return webpSupport != null && heicSupport != null
    }

    override fun hasAvSupport(): Boolean {
        return avSupport != null
    }

    /// @return null on error or dimension not available.
    override fun isLandscape(lcsuffix: String, provider: IInputStreamProvider, index: Int): Boolean? {
        return try {
            if (Suffix.SVG == lcsuffix) {
                val dim = readSVGDimension(provider) ?: return null
                return dim.x > dim.y
            }
            if (!MimeUtil.isImageLcSuffix(lcsuffix)) return null
            if (webpSupport != null && lcsuffix == Suffix.WEBP) {
                provider.inputStream().use { webpSupport.readImageDim(it) }?.let { dim ->
                    return dim.x > dim.y
                }
            }
            if (heicSupport != null && lcsuffix == Suffix.HEIC) {
                provider.inputStream().use { heicSupport.readImageDim(it) }?.let { dim ->
                    return dim.x > dim.y
                }
            }
            val readers = ImageIO.getImageReadersBySuffix(lcsuffix.substring(1))
            if (!readers.hasNext()) return null
            val reader = readers.next()
            provider.inputStream().use { input ->
                reader.input = MemoryCacheImageInputStream(input)
                val width = reader.getWidth(index)
                val height = reader.getHeight(index)
                width > height
            }
        } catch (e: Throwable) {
            null
        }
    }

    override fun getImgConverter(): IImgConverter {
        return ImgConverter()
    }

    override fun getPdfConverter(): IPdfConverter? {
        return if (pdfSupport != null) PdfConverter(pdfSupport) else null
    }

    @Throws(IOException::class)
    override fun getImageDim(name: String, input: InputStream): Dim {
        val lcsuffix = Basepath.lcSuffix(name)
        if (lcsuffix == Suffix.SVG) return readSVGDimension(input)
            ?: throw IOException()
        if (lcsuffix == Suffix.WEBP) return webpSupport?.readImageDim(input)
            ?: throw IOException()
        if (lcsuffix == Suffix.HEIC) return heicSupport?.readImageDim(input)
            ?: throw IOException()
        val mime = MimeUtil.imageMimeFromPath(name) ?: throw IOException()
        val r = getImageReader(mime) ?: throw IOException()
        MemoryCacheImageInputStream(input).use {
            r.input = it
            val width = r.getWidth(0)
            val height = r.getHeight(0)
            return Dim(width, height)
        }
    }

    override fun pdfPoster(
        storage: IStorage,
        fileinfo: IFileInfo,
        page: Int,
        w: Int,
        h: Int,
        quality: Int
    ): Triple<String, Dim, Dim> {
        return pdfSupport?.readPdfAsDataUrl(fileinfo, page, Mime.JPEG, quality, w, h, DEF.pdfScale)
            ?: Triple(PATH._pdfPoster, Dim(768, 1024), Dim(768, 1024))
    }

    override fun audioInfo(ret: JSONObject, fileinfo: IFileInfo): Boolean {
        val info = avSupport?.audioInfo(fileinfo) ?: return false
        ret.put(MediaInfo.Duration, info.duration)
        ret.put(MediaInfo.Channels, info.channels)
        ret.put(MediaInfo.SampleRate, info.samplerate)
        ret.put(MediaInfo.Bitrate, info.bitrate)
        return true
    }

    override fun videoInfo(ret: JSONObject, fileinfo: IFileInfo): Boolean {
        val info = avSupport?.videoInfo(fileinfo) ?: return false
        ret.put(MediaInfo.Duration, info.duration)
        ret.put(MediaInfo.Width, info.width)
        ret.put(MediaInfo.Height, info.height)
        ret.put(MediaInfo.Bitrate, info.bitrate)
        return true
    }

    override fun videoPoster(
        fileinfo: IFileInfo,
        time: Double,
        w: Int,
        h: Int,
        quality: Int
    ): Triple<String, Dim, Dim> {
        return avSupport?.posterAsJpeg(fileinfo, time, quality, w, h)?.let {
            Triple(ImageUtil.toJpegDataUrl(it.first.byteBuffer()), it.second, it.third)
        } ?: Triple(PATH._videoPoster, Dim(1024, 768), Dim(1024, 768))
    }

    override fun readUrlAsJpegDataUrl(
        storage: IStorage,
        outinfo: ImageOutputInfo,
        url: String
    ): IBotResult<Triple<String, Dim, Dim>, JSONObject> {
        val rsrc = storage.rsrc
        val (image, mime) = if (url.startsWith("data:")) {
            val (blob, mime) = ImageUtil.blobFromDataUrl(url, null)
                ?: return rsrc.botObjectError(R.string.InvalidDataUrl)
            val img = ByteArrayInputStream(blob).use { MediaUtil.readImage(it, mime) }
                ?: return rsrc.botObjectError(R.string.ImageReadFailed)
            Pair(img, mime)
        } else {
            val cpath = Without.exceptionOrNull { URI.create(url).path }
                ?: return rsrc.botObjectError(R.string.InvalidURL)
            val fileinfo = storage.fileInfoAt(cpath).result() ?: return rsrc.botObjectError(R.string.InvalidPath)
            val name = fileinfo.name
            val lcsuffix = Basepath.lcSuffix(name)
            if (lcsuffix == Suffix.PDF) {
                pdfSupport ?: return rsrc.botObjectError(R.string.UnsupportedInputFormat_, Suffix.PDF)
                if (outinfo.rotation == 0 && outinfo.effect == Effect.NONE) {
                    pdfSupport.readPdfAsDataUrl(
                        fileinfo,
                        0,
                        Mime.JPEG,
                        outinfo.quality,
                        outinfo.width,
                        outinfo.height,
                        DEF.pdfScale
                    )?.let {
                        return BotResult.ok(it)
                    } ?: return rsrc.botObjectError(R.string.ImageReadFailed_, name)
                }
                val img = pdfSupport.readPdfAsImage(
                    fileinfo,
                    0,
                    outinfo.width,
                    outinfo.height,
                    DEF.pdfScale
                ) ?: return rsrc.botObjectError(R.string.ImageReadFailed_, name)
                Pair(img, Mime.JPEG)
            } else {
                val mime = MimeUtil.mimeFromLcSuffix(lcsuffix)
                    ?: return rsrc.botObjectError(R.string.UnsupportedInputFormat_, name)
                val img = fileinfo.content().inputStream().use { readImage(it, mime) }
                    ?: return rsrc.botObjectError(R.string.ImageReadFailed_, name)
                Pair(img, mime)
            }
        }
        val dim = outputDim(image.width, image.height, outinfo.width, outinfo.height)
        return try {
            val odataurl = toJpegDataUrl(
                outinfo.quality,
                scaleImageAndApplyEffect(
                    outinfo.resize(dim.x, dim.y),
                    Mime.JPEG,
                    mime,
                    image,
                )
            )
            BotResult.ok(Triple(odataurl, dim, Dim(image.width, image.height)))
        } catch (e: Throwable) {
            rsrc.botObjectError(e, R.string.CreateThumbnailFailed_)
        }
    }

    override fun readFileAsJpegDataUrl(
        storage: IStorage,
        outinfo: ImageOutputInfo,
        cropinfo: ImageCropInfo?,
        fileinfo: IFileInfo
    ): IBotResult<Triple<String, Dim, Dim>, JSONObject> {
        val rsrc = storage.rsrc
        return try {
            val lcsuffix = fileinfo.lcSuffix
            var (image, mime) = if (lcsuffix == Suffix.PDF) {
                if (pdfSupport == null)
                    return rsrc.botObjectError(R.string.UnsupportedOutputFormat_, lcsuffix)
                if (outinfo.rotation == 0 && outinfo.effect == Effect.NONE) {
                    pdfSupport.readPdfAsDataUrl(
                        fileinfo,
                        0,
                        Mime.JPEG,
                        outinfo.quality,
                        outinfo.width,
                        outinfo.height,
                        DEF.pdfScale
                    )?.let {
                        return BotResult.ok(it)
                    } ?: return rsrc.botObjectError(R.string.ImageReadFailed_, fileinfo.name)
                }
                val image = pdfSupport.readPdfAsImage(
                    fileinfo, 0, outinfo.width, outinfo.height, DEF.pdfScale
                ) ?: return rsrc.botObjectError(R.string.ImageReadFailed)
                Pair(image, Mime.JPEG)
            } else {
                Without.throwableOrNull {
                    MimeUtil.imageMimeFromLcSuffix(lcsuffix)?.let { mime ->
                        fileinfo.content().inputStream().use { MediaUtil.readImage(it, mime) }?.let { image ->
                            Pair(image, mime)
                        }
                    }
                } ?: return rsrc.botObjectError(R.string.ImageReadFailed_, fileinfo.name)
            }
            image = cropImage(image, cropinfo, true)
            val dim = ImageUtil.fit(image.width, image.height, outinfo.width, outinfo.height)
            val dataurl = MediaUtil.toJpegDataUrl(
                outinfo.quality,
                MediaUtil.scaleImageAndApplyEffect(
                    outinfo.resize(dim.x, dim.y),
                    Mime.JPEG,
                    mime,
                    image,
                )
            )
            BotResult.ok(Triple(dataurl, dim, Dim(image.width, image.height)))
        } catch (e: Throwable) {
            rsrc.botObjectError(e, R.string.CommandFailed)
        }
    }

    override fun writeImage(
        storage: IStorage,
        outinfo: ImageOutputInfo,
        cropinfo: ImageCropInfo?,
        srcpath: String
    ): JSONObject {
        val rsrc = storage.rsrc
        try {
            assert(outinfo.width >= 0)
            assert(outinfo.height >= 0)
            assert(outinfo.rotation == 0 || outinfo.rotation == 90 || outinfo.rotation == 180 || outinfo.rotation == 270)
            assert(outinfo.quality in 0..100)
            val srcresult = if (srcpath.startsWith("data:")) {
                val mime = ImageUtil.mimeOfDataUrl(srcpath);
                val image = readDataurl(srcpath)
                    ?: return rsrc.jsonObjectError(R.string.ImageReadFailed_, srcpath.substring(0, 24));
                Pair(image, mime)
            } else {
                val srcinfo = storage.fileInfoAt(srcpath).let {
                    it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
                }
                val mime = MimeUtil.imageMimeFromPath(srcinfo.name)
                    ?: return rsrc.jsonObjectError(R.string.UnsupportedInputFormat_, srcinfo.name)
                val image = readImage(srcinfo, mime)
                    ?: return rsrc.jsonObjectError(R.string.ImageReadFailed_, srcpath)
                Pair(image, mime)
            }
            var image = srcresult.first
            val imime = srcresult.second
            val (dstinfo, omime) = if (outinfo.path.startsWith("data:")) {
                Pair(null, Mime.JPEG)
            } else {
                val dstinfo = storage.fileInfoAt(outinfo.path).let {
                    it.result() ?: return rsrc.jsonObjectError(it.failure()!!)
                }
                val mime = MimeUtil.imageMimeFromPath(dstinfo.name)
                    ?: return rsrc.jsonObjectError(R.string.UnsupportedOutputFormat_, dstinfo.name)
                Pair(dstinfo, mime)
            }
            image = cropImage(image, cropinfo, omime == Mime.JPEG)
            val dim = outputDim(image.width, image.height, outinfo.width, outinfo.height)
            image = scaleImageAndApplyEffect(
                outinfo.resize(dim.x, dim.y),
                omime,
                imime,
                image
            )
            if (dstinfo == null) {
                val dataurl = toJpegDataUrl(outinfo.quality, image)
                return JSONObject().put(Key.result, dataurl)
            } else {
                try {
                    writeImage(storage, dstinfo, omime, outinfo.quality, image)
                } catch (e: UnsupportedOperationException) {
                    return rsrc.jsonObjectError(R.string.ActionRequireGmCommandFromTheGraphicsMagickPackage);
                }
                return JSONObject()
            }
        } catch (e: Throwable) {
            return rsrc.jsonObjectError(e, R.string.ImageWriteFailed_, outinfo.path)
        }
    }

    @Throws(IOException::class)
    override fun rewritePNG(rsrc: IResUtil, png: ByteArray): IByteSlice {
        val r = getImageReader(Mime.PNG)
        val w = getImageWriter(Mime.PNG)
        if (r == null || w == null) {
            throw IOException(rsrc.get(R.string.ImageReadFailed))
        }
        val image = ImageIO.read(ByteArrayInputStream(png))
        val out = MyByteOutputStream(png.size)
        ImageIO.write(image, "PNG", out)
        return out
    }

}
