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
package sf.andrians.cplusedition.war.support

import org.json.JSONObject
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import sf.andrians.ancoreutil.util.FileUtil
import sf.andrians.ancoreutil.util.StepWatch
import sf.andrians.ancoreutil.util.struct.IntPair
import sf.andrians.ancoreutil.util.text.TextUtil.equals
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.IInputStreamProvider
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MediaInfo
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.war.Base64
import sf.andrians.cplusedition.war.Conf
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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

object MediaUtil {
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

    @JvmStatic
    @Throws(IOException::class)
    fun rewritePNG(res: IResUtil, png: ByteArray): ByteArray {
        val r = getImageReader(MimeUtil.PNG)
        val w = getImageWriter(MimeUtil.PNG)
        if (r == null || w == null) {
            throw IOException(res.get(R.string.ErrorReadingImage_))
        }
        val image = ImageIO.read(ByteArrayInputStream(png))
        val out = ByteArrayOutputStream(png.size)
        ImageIO.write(image, "PNG", out)
        return out.toByteArray()
    }

    @JvmStatic
    fun convertImage(dst: IFileInfo, src: IFileInfo): Boolean {
        try {
            dst.content().getOutputStream().use {
                return convertImage(it, dst.name, src)
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun convertImage(out: OutputStream, outname: String, srcinfo: IFileInfo): Boolean {
        try {
            val r = getImageReader(MimeUtil.mimeFromPath(srcinfo.name))
            val w = getImageWriter(MimeUtil.mimeFromPath(outname))
            if (r == null || w == null) return false
            srcinfo.content().getInputStream().use { it ->
                MemoryCacheImageInputStream(it).use { imageinput ->
                    r.input = imageinput
                    w.output = out
                    w.write(r.read(0))
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    @Throws(IOException::class)
    fun getImageDimension(res: IResUtil, file: File): IntPair {
        return try {
            getImageDimension1(file)
        } catch (e: Throwable) {
            throw IOException(res.get(R.string.ErrorReadingImage_) + file.name)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getImageDimension1(file: File): IntPair {
        val name = file.name
        val mime = MimeUtil.imageMimeFromPath(name) ?: throw IOException()
        val r = getImageReader(mime) ?: throw IOException()
        FileImageInputStream(file).use {
            r.input = it
            val width = r.getWidth(0)
            val height = r.getHeight(0)
            return IntPair(width, height)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getImageDimension(name: String, input: InputStream): IntPair {
        val mime = MimeUtil.imageMimeFromPath(name) ?: throw IOException()
        val r = getImageReader(mime) ?: throw IOException()
        MemoryCacheImageInputStream(input).use {
            r.input = it
            val width = r.getWidth(0)
            val height = r.getHeight(0)
            return IntPair(width, height)
        }
    }

    /**
     * @param image       The original image.
     * @param compression 0..100
     * @return A Base64 encoded image that fit into a sizexsize square.
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @JvmStatic
    @Throws(Exception::class)
    fun scaleImageBase64(
            res: IResUtil,
            timeout: Long,
            image: BufferedImage,
            mime: String,
            twidth: Int,
            theight: Int,
            rotation: Int,
            effect: Int,
            adjust: Double,
            compression: Int
    ): String {
        val scaled = scaleImage(res, timeout, image, mime, twidth, theight, rotation, effect, adjust, compression)
        return Base64.encodeToString(scaled, Base64.NO_WRAP)
    }

    @Throws(Exception::class)
    fun scaleImage(
            res: IResUtil,
            timeout: Long,
            image: BufferedImage,
            mime: String,
            twidth: Int,
            theight: Int,
            rotation: Int,
            effect: Int,
            adjust: Double,
            compression: Int
    ): ByteArray {
        val writer = getImageWriter(mime)
                ?: throw IOException(res.get(R.string.UnsupportedImageFormat_) + mime)
        val os = ByteArrayOutputStream()
        val ios = ImageIO.createImageOutputStream(os)
        try {
            writer.output = ios
            scaleImage(writer, res, timeout, mime, twidth, theight, rotation, effect, adjust, compression, image)
        } finally {
            ios.close()
        }
        return os.toByteArray()
    }

    @Throws(Exception::class)
    fun scaleImage(
            writer: ImageWriter,
            res: IResUtil,
            timeout: Long,
            mime: String,
            twidth: Int,
            theight: Int,
            rotation: Int,
            effect: Int,
            adjust: Double,
            compression: Int,
            image: BufferedImage
    ) {
        val isjpeg = MimeUtil.JPEG == mime
        val swap = rotation != 0 && rotation != 180
        val w = if (swap) theight else twidth
        val h = if (swap) twidth else theight
        var bimage = BufferedImage(w, h, if (isjpeg) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB)
        val complete = CountDownLatch(1)
        val g = bimage.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        if (isjpeg) {
            g.background = Color(0xff, 0xff, 0xff, 0xff)
            g.fillRect(0, 0, w, h)
        }
        val radian = Math.toRadians(rotation.toDouble())
        when (rotation) {
            0 -> {
            }
            180 -> g.rotate(radian, w * 0.5, h * 0.5)
            90 -> {
                g.rotate(radian, w.toDouble(), 0.0)
                g.translate(w, 0)
            }
            270 -> {
                g.rotate(radian, 0.0, h.toDouble())
                g.translate(0, h)
            }
            else -> throw IOException(res.get(R.string.InvalidRotation_) + rotation)
        }
        if (g.drawImage(
                        image,
                        0,
                        0,
                        twidth,
                        theight
                ) { img, infoflags, x, y, width, height ->
                    val done = infoflags and (ImageObserver.ALLBITS or ImageObserver.ERROR or ImageObserver.ABORT) != 0
                    if (done) {
                        complete.countDown()
                    }
                    !done
                }) {
            complete.countDown()
        }
        if (!complete.await(timeout, TimeUnit.MILLISECONDS)) {
            throw TimeoutException()
        }
        bimage = applyEffect(effect, adjust, bimage)
        val params = writer.defaultWriteParam
        if (isjpeg) {
            params.compressionMode = ImageWriteParam.MODE_EXPLICIT
            params.compressionType = "JPEG"
            params.compressionQuality = compression / 100f
        }
        writer.write(null, IIOImage(bimage, null, null), params)
    }

    @Throws(Exception::class)
    fun applyEffect(effect: Int, adjust: Double, bitmap: BufferedImage): BufferedImage {
        when (effect) {
            An.Effect.GRAY2, An.Effect.GRAY4, An.Effect.GRAY8, An.Effect.GRAY16, An.Effect.GRAY256 -> {
                grayscaleEffect(bitmap, effect, adjust)
            }
        }
        return bitmap
    }

    @Throws(Exception::class)
    private fun grayscaleEffect(bitmap: BufferedImage, effect: Int, adjust: Double) {
        val timer = StepWatch(true)
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
        
        when (effect) {
            An.Effect.GRAY2 -> {
                val q = ImageUtil.MyBlackOnWhitePosterizer(pixels, adjust)
                
                var offset = 0
                while (offset < len) {
                    pixels[offset] = q.quantize(pixels[offset])
                    ++offset
                }
            }
            An.Effect.GRAY4,
            An.Effect.GRAY8,
            An.Effect.GRAY16
            -> {
                val levels = if (effect == An.Effect.GRAY4) 4 else if (effect == An.Effect.GRAY8) 8 else 16
                val q = ImageUtil.MyGrayPosterizer(pixels, levels, adjust, null)
                
                for (offset in pixels.indices) {
                    pixels[offset] = q.quantize(pixels[offset])
                }
            }
            An.Effect.GRAY256 -> {
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

    /**
     * @param size The max thumbnail output size, eg. 512.
     * @return The base64 string for the scaled image.
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun getThumbnailBase64(res: IResUtil, size: Int, cpath: String, input: InputStream): String {
        val image = ImageIO.read(input) ?: throw IOException(res.get(R.string.ErrorReadingImage_) + cpath)
        val width = image.width
        val height = image.height
        val dim = ImageUtil.shrink(width, height, size, size)
        val mime = MimeUtil.imageMimeFromPath(cpath) ?: throw IOException(res.get(R.string.UnsupportedFileType_) + cpath)
        return try {
            scaleImageBase64(
                    res,
                    Support.Def.thumbnailTimeout,
                    image,
                    mime,
                    dim.x,
                    dim.y,
                    0,
                    An.Effect.NONE,
                    0.0,
                    An.DEF.jpegQuality)
        } catch (e: Throwable) {
            throw IOException(res.get(R.string.ErrorCreatingThumbnail_) + cpath)
        }
    }

    fun toDataUrl(mime: String, imagedata: ByteArray): String {
        return "data:$mime;base64," + Base64.encodeToString(imagedata, Base64.NO_WRAP)
    }

    @JvmStatic
    fun fromDataUrl(expectedmime: String?, data: String): ByteArray? {
        var data = data
        if (!data.startsWith("data:")) {
            return null
        }
        var index = data.indexOf(';')
        if (index <= 5) {
            return null
        }
        if (expectedmime != null && data.substring(5, index) != expectedmime) {
            return null
        }
        index = data.indexOf(',')
        if (index >= 0) {
            data = data.substring(index + 1)
        }
        return Base64.decode(data, Base64.DEFAULT)
    }

    fun lum(rgb: Int): Int {
        val r = (rgb shr 16 and 0xff).toDouble()
        val g = (rgb shr 8 and 0xff).toDouble()
        val b = (rgb and 0xff).toDouble()
        val y = r * 0.2126 + g * 0.7152 + b * 0.0722
        return Math.round(y).toInt()
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun readSVGDimension(data: InputStream?): JSONObject? {
        val nspat = Pattern.compile("(?s)^https?://www\\.w3\\.org/.*/svg$")
        val sizeunitpat = Pattern.compile("(?s)^\\s*(\\d+)\\s*(\\S*)\\s*$")
        val viewboxpat = Pattern.compile("(?s)^\\s*[\\d.]+\\s+[\\d.]+\\s+([\\d.]+)\\s+([\\d.]+)\\s*$")
        if (data == null) return null
        val factory = SAXParserFactory.newInstance()
        factory.isValidating = false
        factory.isNamespaceAware = true
        val parser = factory.newSAXParser()
        val info = JSONObject()
        val found = booleanArrayOf(false)
        val isroot = booleanArrayOf(true)
        val level = intArrayOf(0)
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
                                    info.put(MediaInfo.Width, w)
                                    info.put(MediaInfo.Height, h)
                                    return
                                } catch (e: Throwable) {
                                }
                            }
                        }
                        val width = attributes.getValue(uri, "width")
                        val height = attributes.getValue(uri, "height")
                        val wm = sizeunitpat.matcher(width)
                        val hm = sizeunitpat.matcher(height)
                        if (wm.matches() && hm.matches()) {
                            if (equals(wm.group(2), hm.group(2))) {
                                try {
                                    val ws = wm.group(1)
                                    val hs = hm.group(1)
                                    val w = ws.toInt()
                                    val h = hs.toInt()
                                    val ww = if (w > h) 512 else 512 * w / h
                                    val hh = if (w > h) 512 * h / w else 512
                                    
                                    info.put(MediaInfo.Width, ww)
                                    info.put(MediaInfo.Height, hh)
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
        return if (found[0] && level[0] == 0) info else null
    }

    @Throws(Exception::class)
    fun readSVGDimension(provider: IInputStreamProvider): ImageUtil.Dim? {
        provider.getInputStream().use { input ->
            val info = readSVGDimension(input) ?: return null
            val width = MediaInfo.width(info)
            val height = MediaInfo.height(info)
            return if (width > 0 && height > 0) ImageUtil.Dim(width, height) else null
        }
    }

    /// @return null on error or dimension not available.
    @JvmStatic
    fun isLandscape(suffix: String, provider: IInputStreamProvider, index: Int): Boolean? {
        return try {
            if (".svg" == suffix) {
                val dim = readSVGDimension(provider) ?: return null
                return dim.x > dim.y
            }
            if (suffix.length < 1) return null
            val lcext = suffix.substring(1)
            val reader = ImageIO.getImageReadersBySuffix(lcext).next()
            val width = reader.getWidth(index)
            val height = reader.getHeight(index)
            width > height
        } catch (e: Throwable) {
            null
        }
    }

    @Throws(Exception::class)
    fun asBytes(provider: IInputStreamProvider): ByteArray {
        provider.getInputStream().use { input -> return FileUtil.asBytes(input) }
    }
}
