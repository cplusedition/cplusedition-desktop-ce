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

import com.cplusedition.anjson.JSONUtil.putJSONArrayOrFail
import com.cplusedition.anjson.JSONUtil.putJSONObjectOrFail
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.ILog
import com.cplusedition.bot.core.TextUt
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.ancoreutil.util.struct.IntList
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.Support
import sf.andrians.cplusedition.support.handler.HandlerUtil
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.IThumbnailCallback
import sf.andrians.cplusedition.support.handler.IFilepickerHandler.ThumbnailResult
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.handler.ResUtil
import java.io.File
import java.io.IOException

object ImageUtil {
    /**
     * Fit rectangle srcwidth x srcheight into a dstwidth x dstheight rectangle, taking aspect ratios into account.
     */
    fun fit(srcwidth: Int, srcheight: Int, dstwidth: Int, dstheight: Int): Dim {
        val width: Int
        val height: Int
        val srcratio = srcwidth * 1.0 / srcheight
        val dstratio = dstwidth * 1.0 / dstheight
        if (srcratio >= dstratio) {
            width = dstwidth
            height = Math.floor(srcheight * 1.0 * dstwidth / srcwidth).toInt()
        } else {
            height = dstheight
            width = Math.floor(srcwidth * 1.0 * dstheight / srcheight).toInt()
        }
        return Dim(width, height)
    }

    /**
     * Unlike fit(), this only shrink to fit, never expanded.
     */
    fun shrink(srcwidth: Int, srcheight: Int, dstwidth: Int, dstheight: Int): Dim {
        if (srcwidth <= dstwidth && srcheight <= dstheight) {
            return Dim(srcwidth, srcheight)
        }
        val width: Int
        val height: Int
        val srcratio = srcwidth * 1.0 / srcheight
        val dstratio = dstwidth * 1.0 / dstheight
        if (srcratio >= dstratio) {
            width = dstwidth
            height = Math.floor(srcheight * 1.0 * dstwidth / srcwidth).toInt()
        } else {
            height = dstheight
            width = Math.floor(srcwidth * 1.0 * dstheight / srcheight).toInt()
        }
        return Dim(width, height)
    }

    fun toDataUrl(mime: String?, base64: String): String {
        return "data:$mime;base64,$base64"
    }

    fun toJpegDataUrl(base64: String): String {
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * @return { An.Key.result: dataurl, An.Key.errors: errors }
     */
    fun actionImageThumbnail(callback: IThumbnailCallback, resutil: ResUtil, info: JSONObject?, tsize: Int): String? {
        if (info == null) return resutil.jsonError(R.string.PleaseSpecifyAnImageId)
        val id = info.stringOrNull(MediaInfo.Id)
                ?: return resutil.jsonError(R.string.PleaseSpecifyAnImageId)
        val date = info.optLong(MediaInfo.FileDate, Long.MAX_VALUE)
        try {
            val tinfo = callback.getThumbnail(id, date, tsize)
            val dataurl = tinfo.dataUrl
            if (dataurl != null) {
                return HandlerUtil.jsonResult(toDataUrl(MimeUtil.JPEG, dataurl))
            }
        } catch (e: Throwable) {
            
        }
        return resutil.jsonError(R.string.ErrorCreatingThumbnail_, id)
    }

    fun actionImageThumbnails(callback: IThumbnailCallback, res: IResUtil, infos: JSONArray, tsize: Int): JSONObject {
        val ret = JSONObject()
        val result = ret.putJSONArrayOrFail(An.Key.result)
        for (i in 0 until infos.length()) {
            val info = infos.optJSONObject(i)
            if (info == null) {
                result.put(null)
                continue
            }
            val retinfo = JSONObject()
            result.put(retinfo)
            val id = info.stringOrNull(MediaInfo.Id)
            val date = info.optLong(MediaInfo.FileDate, Long.MAX_VALUE)
            if (id == null) {
                res.jsonObjectError(retinfo, R.string.PleaseSpecifyAnImageId)
                continue
            }
            try {
                val tinfo = callback.getThumbnail(id, date, tsize)
                thumbnailInfo(retinfo, tinfo)
            } catch (e: Throwable) {
                res.jsonObjectError(retinfo, R.string.UnexpectedException)
            }
        }
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

    @Throws(JSONException::class)
    fun localImageInfo(
            cpath: String?,
            name: String?,
            width: Int,
            height: Int,
            mime: String?,
            modified: Long,
            size: Long,
            dataurl: String?
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
        if (dataurl != null) {
            ret.putJSONObjectOrFail(MediaInfo.TnInfo).put(MediaInfo.DataUrl, dataurl)
        }
        return ret
    }

    @Throws(IOException::class)
    fun getTempImageFile(res: IResUtil, tmpdir: IFileInfo, info: JSONObject): IFileInfo {
        val id = info.optLong(MediaInfo.Id)
        val mime = info.stringOrNull(MediaInfo.Mime)
                ?: throw IOException(res.get(R.string.MissingImageMimeTypeParameter))
        val ext = MimeUtil.imageExtFromMime(mime) ?: throw IOException(res.get(R.string.UnsupportedImageFormat_) + mime)
        var outfile: File? = null
        for (serial in 0 until 100) {
            val name = TextUt.format("%08d%02d%s.%s", id, serial, "", ext)
            val file = tmpdir.fileInfo(name)
            if (!file.exists) {
                return file
            }
        }
        throw IOException(res.get(R.string.ErrorCreatingCacheFile))
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
        return bn.changeNameWithoutSuffix(bn.nameWithoutSuffix + "-tn" + dim[0] + 'x' + dim[1]).toString()
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

    class Dim(var x: Int, var y: Int)

    class MyBlackOnWhitePosterizer(pixels: IntArray, adjust: Double) : IPosterizer {

        private var level: Int

        init {
            val adjust1 = clamp(adjust, 0.0, 100.0)
            val populations = IntArray(256)
            for (pixel in pixels) {
                val y = pixel and 0xff
                ++populations[y]
            }
            var pop = 0
            level = 127
            val t = pixels.size.toLong()
            val target = (t * adjust1).toInt() / 100
            for (i in 0..255) {
                val p = populations[i]
                pop += p
                if (pop >= target) {
                    level = findthreshold(populations, i)
                    break
                }
            }
        }

        private fun findthreshold(populations: IntArray, t: Int): Int {
            var min = Int.MAX_VALUE
            var ret = t
            val tp = populations[t] / 2
            var i = Math.max(0, t - 3)
            val end = Math.min(populations.size, t + 4)
            while (i < end) {
                val pop = populations[i]
                if (pop < tp && pop < min) {
                    min = pop
                    ret = i
                }
                ++i
            }
            return ret
        }

        fun threshold(): Int {
            return level
        }

        override fun quantize(color: Int): Int {
            val a = color and -0x1000000
            val y = color and 0xff
            return if (y > level) a or 0x00ffffff else a
        }
    }

    class MyGrayPosterizer(pixels: IntArray, maxcolors: Int, adjust: Double, log: ILog?) : IPosterizer {
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
            val cuts = IntList()
            val subtotals = IntList()
            var total = 0
            var subtotal = 0
            run {
                var i = 0
                while (i < maxcolors - 1 && offset < len) {
                    val t = (totalpopulation * adjust * (i + 1) / 100).toInt()
                    while (offset < len) {
                        if (total >= t) {
                            cuts.add(offset)
                            subtotals.add(subtotal)
                            subtotal = 0
                            break
                        }
                        val p = populations[offset++]
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
            Support.assertion(cuts.size() <= maxcolors, "cuts=" + cuts.size())
            Support.assertion(subtotals.size() <= maxcolors, "subtotals=" + subtotals.size())
            val nlevels = cuts.size()
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
