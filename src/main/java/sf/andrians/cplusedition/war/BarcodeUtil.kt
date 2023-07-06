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

import com.cplusedition.bot.core.Without
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q
import sf.andrians.cplusedition.support.media.BarcodeBase
import sf.andrians.cplusedition.support.media.ImageCropInfo
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream

object BarcodeUtil : BarcodeBase() {
    override val defaultScale = Conf.QRCODE_SCALE

    override fun generateQRCode(text: String, mime: String, scale: Int): ByteArray? {
        return Without.throwableOrNull {
            MediaUtil.toPngBlob(
                100,
                renderqrcode(encodeqrcode(text, Q.name), scale)
            ).toByteArray()
        }
    }

    @Throws(Exception::class)
    override fun detectqrcode(
        input: InputStream,
        mime: String,
        cropinfo: ImageCropInfo?,
    ): com.google.zxing.Result? {
        return Without.exceptionOrNull {
            val img = crop(MediaUtil.readImage(input, mime), cropinfo) ?: return@exceptionOrNull null
            val source = BufferedImageLuminanceSource(img)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            QRCodeReader().decode(bitmap)
        }
    }

    @Throws(Exception::class)
    override fun detectbarcode(
        input: InputStream,
        mime: String,
        cropinfo: ImageCropInfo?,
    ): com.google.zxing.Result? {
        return Without.exceptionOrNull {
            val img = crop(MediaUtil.readImage(input, mime), cropinfo) ?: return@exceptionOrNull null
            val source = BufferedImageLuminanceSource(img)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            MultiFormatReader().decode(bitmap)
        }
    }

    private fun crop(image: BufferedImage?, cropinfo: ImageCropInfo?): BufferedImage? {
        if (image == null) return null
        if (cropinfo == null) return image
        return MediaUtil.cropImage(image, cropinfo, true)
    }

    private fun renderqrcode(matrix: BitMatrix, scale: Int = 1): BufferedImage {
        val width = matrix.width
        val height = matrix.height
        val image = BufferedImage(width * scale, height * scale, BufferedImage.TYPE_BYTE_GRAY)
        val gfx = image.createGraphics()
        gfx.background = Color(255, 255, 255, 0)
        gfx.clearRect(0, 0, width * scale, height * scale)
        val row = BitArray(width)
        for (y in 0 until height) {
            matrix.getRow(y, row)
            for (x in 0 until width) {
                if (!row.get(x)) continue
                for (yy in 0 until scale) {
                    for (xx in 0 until scale) {
                        image.setRGB(x * scale + xx, y * scale + yy, 0)
                    }
                }
            }
        }
        return image
    }

}
