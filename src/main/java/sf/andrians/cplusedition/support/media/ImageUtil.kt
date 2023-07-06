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

import com.cplusedition.anjson.JSONUtil.foreach
import com.cplusedition.anjson.JSONUtil.jsonObjectOrNull
import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.putJSONObject
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.*
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.ThumbnailResult
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Mime
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface IBarcodeUtil {
    val defaultScale: Int

    /// @return PNG image.
    fun generateQRCode(text: String, mime: String = Mime.PNG, scale: Int = this.defaultScale): ByteArray?

    /// @return { Key.result: String, Key.type: String, Key.timestamp: Long }.
    fun detectQRCode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): JSONObject?

    /// @return { Key.result: String, Key.type: String, Key.timestamp: Long }.
    fun detectBarcode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): JSONObject?
}

object ImageUtil {

    data class Dim(var x: Int, var y: Int)

    fun limitImageArea(width: Int, height: Int): Dim {
        val maxarea = DEF.maxOutputImageArea
        val area = width * height
        if (area <= maxarea)
            return Dim(width, height)
        val scale = sqrt(maxarea.toDouble() / area)
        return Dim((width * scale).roundToInt(), (height * scale).roundToInt())
    }

    /**
     * Fit rectangle srcwidth x srcheight into a dstwidth x dstheight rectangle, taking aspect ratios into account.
     * If both dstwidth and dstheight <= 0, return the source dimension.
     * If any of dstwidth or dstheight <= 0, it would be derived from the source aspect ratio.
     */
    fun fit(srcwidth: Int, srcheight: Int, dstwidth: Int, dstheight: Int): Dim {
        if (dstwidth <= 0 && dstheight <= 0)
            return limitImageArea(srcwidth, srcheight)
        val srcratio = srcwidth.toDouble() / srcheight
        val dw = if (dstwidth <= 0) (dstheight.toDouble() * srcratio) else dstwidth.toDouble()
        val dh = if (dstheight <= 0) (dstwidth.toDouble() / srcratio) else dstheight.toDouble()
        val dstratio = dw / dh
        val w = if (srcratio >= dstratio) dw else (srcwidth.toDouble() * dh / srcheight)
        val h = if (srcratio >= dstratio) (srcheight.toDouble() * dw / srcwidth) else dh
        return limitImageArea(w.toInt(), h.toInt())
    }

    /**
     * Unlike fit(), this only shrink to fit, never expanded.
     */
    fun shrink(srcwidth: Int, srcheight: Int, dstwidth: Int, dstheight: Int): Dim {
        if (srcwidth <= dstwidth && srcheight <= dstheight)
            return limitImageArea(srcwidth, srcheight)
        val srcratio = srcwidth.toDouble() / srcheight
        val dstratio = dstwidth.toDouble() / dstheight
        val w = if (srcratio >= dstratio) dstwidth else (srcwidth.toDouble() * dstheight / srcheight).roundToInt()
        val h = if (srcratio >= dstratio) (srcheight.toDouble() * dstwidth / srcwidth).roundToInt() else dstheight
        return limitImageArea(w, h)
    }

    fun toDataUrl(mime: String, base64: String): String {
        return "data:$mime;base64,$base64"
    }

    fun toDataUrl(mime: String, data: ByteArray): String {
        return toDataUrl(mime, Base64.getEncoder().encodeToString(data).toString())
    }

    fun toDataUrl(mime: String, data: ByteBuffer): String {
        return toDataUrl(mime, Charsets.ISO_8859_1.decode(Base64.getEncoder().encode(data)).toString())
    }

    fun toJpegDataUrl(data: ByteArray): String {
        return toDataUrl(Mime.JPEG, data)
    }

    fun toJpegDataUrl(data: ByteBuffer): String {
        return toDataUrl(Mime.JPEG, data)
    }

    fun toJpegDataUrl(base64: String): String {
        return toDataUrl(Mime.JPEG, base64)
    }

    fun toPngDataUrl(data: ByteArray): String {
        return toDataUrl(Mime.PNG, data)
    }

    fun toPngDataUrl(data: ByteBuffer): String {
        return toDataUrl(Mime.PNG, data)
    }

    fun toPngDataUrl(base64: String): String {
        return toDataUrl(Mime.PNG, base64)
    }

    @JvmStatic
    fun mimeOfDataUrl(data: String): String? {
        if (!data.startsWith("data:")) return null
        val index = data.indexOf(';')
        if (index <= 5) return null
        return data.substring(5, index).lowercase(Locale.ROOT)
    }

    fun blobFromDataUrl(data: String, expectedmime: String? = null): Pair<ByteArray, String>? {
        var base64 = data
        if (!base64.startsWith("data:")) {
            return null
        }
        var index = base64.indexOf(';')
        if (index <= 5) {
            return null
        }
        val mime = base64.substring(5, index)
        if (expectedmime != null && mime != expectedmime) {
            return null
        }
        index = base64.indexOf(',')
        if (index >= 0) {
            base64 = base64.substring(index + 1)
        }
        return Pair(Base64.getDecoder().decode(base64), mime)
    }

    /**
     * @return { An.Key.result: dataurl, An.Key.errors: errors }
     */
    fun actionImageThumbnail(rsrc: IResUtil, info: JSONObject?, tsize: Int, callback: IThumbnailCallback): JSONObject {
        if (info == null) return rsrc.jsonObjectError(R.string.PleaseSpecifyAnImageId)
        val id = info.stringOrNull(MediaInfo.Id)
            ?: return rsrc.jsonObjectError(R.string.PleaseSpecifyAnImageId)
        val date = info.optLong(MediaInfo.FileDate, Long.MAX_VALUE)
        try {
            val tinfo = callback.getThumbnail(id, date, tsize)
            val dataurl = tinfo.dataUrl
            if (dataurl != null) {
                return rsrc.jsonObjectResult(toDataUrl(Mime.JPEG, dataurl))
            }
        } catch (e: Throwable) {
            
        }
        return rsrc.jsonObjectError(R.string.CreateThumbnailFailed_, id)
    }

    fun actionImageThumbnails(storage: IStorage, infos: JSONArray, tsize: Int, callback: IThumbnailCallback): JSONObject {
        val rsrc = storage.rsrc
        val usepool = infos.length() > DEF.thumbnailUsePool
        
        val ret = JSONObject()
        val result = ret.putJSONArrayOrFail(Key.result)
        val group = CountedTaskGroup(infos.length())

        fun unpooled(mediainfo: JSONObject, cpath: String, date: Long) {
            val tid = group.enter()
            try {
                val tinfo = callback.getThumbnail(cpath, date, tsize)
                thumbnailInfo(mediainfo, tinfo)
            } catch (e: Throwable) {
                rsrc.jsonObjectError(mediainfo, R.string.UnexpectedException)
            } finally {
                group.leave(tid)
            }
        }

        fun pooled(mediainfo: JSONObject, id: String, date: Long) {
            workerThreadPool.submit {
                unpooled(mediainfo, id, date)
            }
        }

        infos.foreach X@{
            val info = infos.jsonObjectOrNull(it)
            if (info == null) {
                result.put(JSONObject.NULL)
                group.leave(group.enter())
                return@X
            }
            val mediainfo = result.putJSONObject()
            val cpath = MediaInfo.cpath(info)
            val date = info.optLong(MediaInfo.FileDate, Long.MAX_VALUE)
            if (cpath == null) {
                rsrc.jsonObjectError(mediainfo, R.string.PleaseSpecifyAnImageId)
                group.leave(group.enter())
                return@X
            }
            if (usepool) pooled(mediainfo, cpath, date)
            else unpooled(mediainfo, cpath, date)
        }
        group.awaitDone(1, TimeUnit.HOURS)
        return ret
    }

    @Throws(JSONException::class)
    private fun thumbnailInfo(ret: JSONObject, tinfo: ThumbnailResult) {
        val error = tinfo.error
        if (error != null) {
            ret.put(MediaInfo.Error, error)
            return
        }
        ret.put(MediaInfo.Width, tinfo.width)
        ret.put(MediaInfo.Height, tinfo.height)
        ret.put(MediaInfo.DataUrl, tinfo.dataUrl)
    }

    fun localImageInfo(
        file: IFileInfo,
        width: Int,
        height: Int,
        mime: String?,
        tndataurl: String?
    ): JSONObject {
        val stat = file.stat()
        return localImageInfo(
            file.cpath,
            file.name,
            width,
            height,
            mime,
            (stat?.lastModified ?: 0L),
            (stat?.length ?: 0L),
            tndataurl
        )
    }

    @Throws(JSONException::class)
    fun localImageInfo(
        cpath: String,
        name: String,
        width: Int,
        height: Int,
        mime: String?,
        modified: Long,
        size: Long,
        tndataurl: String?
    ): JSONObject {
        val ret = JSONObject()
        ret.put(MediaInfo.Id, cpath)
        ret.put(MediaInfo.Title, name)
        ret.put(MediaInfo.Description, "")
        ret.put(MediaInfo.Width, width)
        ret.put(MediaInfo.Height, height)
        ret.put(MediaInfo.Mime, mime)
        ret.put(MediaInfo.FileSize, size)
        ret.put(MediaInfo.FileDate, modified)
        ret.put(MediaInfo.Uri, cpath)
        if (tndataurl != null) {
            ret.putJSONObjectOrFail(MediaInfo.TnInfo).put(MediaInfo.DataUrl, tndataurl)
        }
        return ret
    }

    @Throws(IOException::class)
    fun getDstImageFile(res: IResUtil, dstdir: IFileInfo, filename: String): IFileInfo {
        dstdir.fileInfo(filename).let { if (!it.exists) return it }
        val basepath = Basepath.from(filename)
        for (serial in 0 until 100) {
            val name = TextUt.format("%s%02d%s%s", basepath.stem, serial, "", basepath.suffix)
            val file = dstdir.fileInfo(name)
            if (!file.exists) {
                return file
            }
        }
        throw IOException(res.get(R.string.DestinationExistsNotOverwriting))
    }

    fun getRotatedDimension(width: Int, height: Int, rotation: Int): IntArray {
        val rotated = rotation != 0 && rotation != 180
        val w = if (rotated) height else width
        val h = if (rotated) width else height
        return intArrayOf(w, h)
    }

    fun getThumbnailPath(imagepath: String, width: Int, height: Int, rotation: Int): String {
        val bn = Basepath.from(imagepath)
        val dim = getRotatedDimension(width, height, rotation)
        return bn.changeStem(bn.stem + "-tn" + dim[0] + 'x' + dim[1]).toString()
    }

    fun <T : Comparable<T>> clamp(adjust: T, min: T, max: T): T {
        var adjust1 = adjust
        if (adjust1 < min) {
            adjust1 = min
        } else if (adjust1 > max) {
            adjust1 = max
        }
        return adjust1
    }

    interface IPosterizer {
        fun quantize(color: Int): Int
    }

    class MyBlackOnWhitePosterizer(adjust: Double) : IPosterizer {

        private var level: Int = clamp(adjust * 255.0 / 100.0, 0.0, 255.0).toInt()

        fun threshold(): Int {
            return level
        }

        override fun quantize(color: Int): Int {
            val a = color and -0x1000000
            val y = color and 0xff
            return if (y > level) a or 0x00ffffff else a
        }
    }

    class MyGrayPosterizer constructor(pixels: IntArray, maxcolors: Int, adjust: Double, log: ILog?) : IPosterizer {
        private val levels: IntArray
        private val thresholds: IntArray
        private val tCount: Int

        init {
            val adjust1 = clamp(adjust, 0.0, 100.0)
            val populations = IntArray(256)
            var count = 0
            for (pixel in pixels) {
                val y = pixel and 0xff
                val c = ++populations[y]
                if (c == 1) {
                    ++count
                }
            }
            if (count <= maxcolors) {
                levels = IntArray(count)
                var i = 0
                var offset = 0
                while (i < populations.size) {
                    if (populations[i] == 0) {
                        ++i
                        continue
                    }
                    levels[offset++] = i
                    ++i
                }
            } else {
                levels = partitionByPopulation(populations, pixels.size.toLong(), maxcolors, adjust1)
            }
            tCount = levels.size - 1
            thresholds = IntArray(tCount)
            for (i in 0 until tCount) {
                thresholds[i] = levels[i] + levels[i + 1] shr 1
            }
            stretchContrast(levels)
        }

        fun levels(): IntArray {
            return levels
        }

        fun thresholds(): IntArray {
            return thresholds
        }

        private fun partitionByPopulation(
            populations: IntArray, totalpopulation: Long, maxcolors: Int, adjust: Double
        ): IntArray {
            var offset = 0
            val len = populations.size
            val cuts = mutableListOf<Int>()
            val subtotals = mutableListOf<Int>()
            var total = 0
            var subtotal = 0
            run {
                var i = 0f
                while (i < maxcolors - 1 && offset < len) {
                    val t = (totalpopulation * adjust * (i + 1) / 100).toInt()
                    while (offset < len) {
                        val p = populations[offset++]
                        if (total >= t || subtotal > 0 && p >= t) {
                            cuts.add(offset)
                            subtotals.add(subtotal)
                            subtotal = 0
                            break
                        }
                        total += p
                        subtotal += p
                    }
                    ++i
                }
            }
            while (offset < len) {
                subtotal += populations[offset++]
            }
            cuts.add(len)
            subtotals.add(subtotal)
            Support.assertion(cuts.size <= maxcolors, "cuts=" + cuts.size)
            Support.assertion(subtotals.size <= maxcolors, "subtotals=" + subtotals.size)
            val nlevels = cuts.size
            val levels = IntArray(nlevels)
            var start = 0
            for (i in 0 until nlevels) {
                val end = cuts[i]
                levels[i] = average(subtotals[i] / 2, populations, start, end)
                start = end
            }
            return levels
        }

        private fun average(average: Int, populations: IntArray, start: Int, end: Int): Int {
            var pop = 0
            for (i in start until end) {
                pop += populations[i]
                if (pop >= average) {
                    return i
                }
            }
            return end
        }

        private fun stretchContrast(levels: IntArray) {
            val len = levels.size
            val range = levels[len - 1] - levels[0]
            for (i in 1 until len - 1) {
                val c = (levels[i] - levels[0]) * 0xff / range
                levels[i] = c or (c shl 8) or (c shl 16)
            }
            levels[0] = 0
            levels[len - 1] = 0xffffff
        }

        override fun quantize(color: Int): Int {
            val a = color and -0x1000000
            val y = color and 0xff
            for (i in 0 until tCount) {
                if (y < thresholds[i]) {
                    return a or levels[i]
                }
            }
            return a or levels[tCount]
        }
    }
}

interface IPdfPageConverter {
    fun genOutputPath(page: Int): String
    fun convertPage(
        storage: IStorage,
        dst: IFileInfo,
        src: IFileInfo,
        page: Int,
    ): IBotResult<Unit, String>
}

class ImageOrPdfConverter constructor(
    val storage: IStorage,
    val mediautil: IMediaUtil,
    val outinfo: ImageOutputInfo,
    val srcdir: IFileInfo,
    val rpaths: Collection<String>,
    val cut: Boolean,
) {
    val rsrc = storage.rsrc
    fun run(): JSONObject {
        val imgconverter = mediautil.getImgConverter()
        val pdfconverter = mediautil.getPdfConverter()

        val fails = ConcurrentSkipListMap<String, String>()
        val warns = ConcurrentLinkedDeque<String>()
        val oks = ConcurrentLinkedDeque<String>()

        TaskUt.forkJoinTasks {
            val pool = it

            fun prepareWrite(dstdir: IFileInfo, dstsuffix: String, rpath: String): IBotResult<IFileInfo, Collection<String>> {
                val dstinfo = dstdir.fileInfo(Basepath.changeSuffix(rpath, dstsuffix))
                return if (dstinfo.mkparent()) BotResult.ok(dstinfo)
                else BotResult.fail(listOf(storage.rsrc.get(R.string.CreateParentDirectoryFailed_, dstinfo.cpath)))
            }

            fun convertpdf(dst: IFileInfo, src: IFileInfo, rpath: String) {
                pdfconverter?.convert(storage, Support, dst, outinfo, src) { result ->
                    result.onResult({
                        
                        fails[rpath] = it
                    }, { results ->
                        var ok = true
                        for ((label, msg) in results.entries) {
                            if (msg != "OK") {
                                fails[label] = msg
                                ok = false
                            }
                        }
                        if (ok) {
                            oks.add(rpath)
                            if (cut)
                                src.delete()
                        } else fails[rpath] = rsrc.get(R.string.ActionConvertFailed_, rpath)
                    })
                }
            }

            fun convertimg(dstinfo: IFileInfo, src: IFileInfo, rpath: String) {
                
                pool.submit {
                    imgconverter.convert(storage, dstinfo, outinfo, src) {
                        if (it == "OK") {
                            oks.add(rpath)
                            if (cut)
                                src.delete()
                        } else fails[rpath] = it
                    }
                }
            }

            fun convert(dstdir: IFileInfo, dstsuffix: String, src: IFileInfo, rpath: String) {
                try {
                    prepareWrite(dstdir, dstsuffix, rpath).onResult({
                        val msg = it.bot.joinln()
                        
                        fails.put(rpath, msg)
                    }, { dstinfo ->
                        val lcsuffix = Basepath.lcSuffix(src.name)
                        if (lcsuffix == MimeUtil.Suffix.PDF) {
                            convertpdf(dstinfo, src, rpath)
                        } else if (MimeUtil.isImageLcSuffix(lcsuffix)) {
                            convertimg(dstinfo, src, rpath)
                        } else {
                            fails[rpath] = rsrc.get(R.string.UnsupportedInputFormat_, lcsuffix)
                        }
                    })
                } catch (e: Throwable) {
                    
                    fails[rpath] = e.message ?: "ERROR"
                }
            }

            val dstinfo = storage.fileInfoAt(outinfo.path).result()
            val dstdir = dstinfo?.parent
            if (dstinfo == null || dstdir == null) {
                val msg = rsrc.get(R.string.DestinationNotValid_, outinfo.path)
                
                return@forkJoinTasks msg
            }
            val dstsuffix = dstinfo.suffix
            for (rpath in rpaths) {
                val srcinfo = srcdir.fileInfo(rpath)
                val stat = srcinfo.stat()
                if (stat == null) {
                    
                    fails[rpath] = rsrc.get(R.string.InvalidInputPath_, rpath)
                    continue
                }
                if (stat.isFile) {
                    convert(dstdir, dstsuffix, srcinfo, rpath)
                } else if (stat.isDir) {
                    srcinfo.walk3 { src, path, st ->
                        if (!st.isFile) return@walk3
                        if (!MimeUtil.isImageLcSuffix(Basepath.lcSuffix(src.name))) return@walk3
                        convert(dstdir, dstsuffix, src, Basepath.joinRpath(rpath, path))
                    }
                }
            }
            null
        }?.let {
            return rsrc.jsonObjectError(it)
        }
        return JSONObject()
            .put(Key.result, JSONArray(oks))
            .put(Key.fails, JSONObject(fails as Map<*, *>))
            .put(Key.warns, JSONArray(warns))
    }
}

abstract class PdfConverterBase : IPdfConverter {

    override fun convert(
        storage: IStorage,
        log: ITraceLogger,
        dstinfo: IFileInfo,
        outinfo: ImageOutputInfo,
        srcinfo: IFileInfo,
        done: Fun10<IBotResult<Map<String, String>, String>>
    ) {
        val results = ConcurrentSkipListMap<String, String>()
        val msg = run {
            val rsrc = storage.rsrc
            val srcname = srcinfo.name
            try {
                val dstdir = dstinfo.parent
                    ?: return@run rsrc.get(R.string.DestinationNotValid_, dstinfo.cpath)
                val pagecount = getPageCount(storage, srcinfo)
                    ?: return@run rsrc.get(R.string.ReadFailed_, srcname)
                val stem = Basepath.stem(srcname)
                TaskUt.forkJoinTasks { it ->
                    val pool = it
                    log.enter("# Converting $pagecount pages ...");
                    for (page in 0 until pagecount) {
                        val pagestr = (page + 1).toString().padStart(4, '0')
                        val label = "$srcname#$pagestr"
                        val dstname = getoutname(stem, pagestr, dstinfo.lcSuffix)
                        val dst = dstdir.fileInfo(dstname)
                        if (!dst.mkparent()) {
                            val msg = rsrc.get(R.string.CreateParentDirectoryFailed_, dst.cpath)
                            log.d("# $msg")
                            results[label] = msg
                            continue
                        }
                        log.enter("# $label start");
                        pool.submit {
                            convert(storage, dst, outinfo, srcinfo, page) {
                                results[pagestr] = it
                                log.leave("# $label end: $it");
                            }
                        }
                    }
                    log.leave("# Done");
                }
                return@run "OK"
            } catch (e: Throwable) {
                
                return@run rsrc.get(R.string.ReadFailed_, srcname)
            }
        }
        done(if (msg == "OK") BotResult.ok(results) else BotResult.fail(msg))
    }

    /// @return Destination in form: srcrpath/{srcpath.stem}-{page+1}{output.lcsuffix}
    private fun getoutname(stem: String, pagestr: String, outsuffix: String): String {
        return TextUt.format("%s-%s%s", stem, pagestr, outsuffix)
    }
}
