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

import com.cplusedition.bot.core.Without
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.EncodeHintType.CHARACTER_SET
import com.google.zxing.EncodeHintType.ERROR_CORRECTION
import com.google.zxing.Result
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import sf.andrians.cplusedition.support.An.Key
import java.io.InputStream
import java.util.*

abstract class BarcodeBase : IBarcodeUtil {

    abstract protected fun detectqrcode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): Result?
    abstract protected fun detectbarcode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): Result?

    override fun detectQRCode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): JSONObject? {
        return Without.throwableOrNull {
            val result = detectqrcode(input, mime, cropinfo) ?: return@throwableOrNull null
            JSONObject().put(Key.result, result.text)
                .put(Key.type, result.barcodeFormat)
                .put(Key.timestamp, result.timestamp)
        }
    }

    override fun detectBarcode(input: InputStream, mime: String, cropinfo: ImageCropInfo?): JSONObject? {
        return Without.throwableOrNull {
            val result = detectbarcode(input, mime, cropinfo) ?: return@throwableOrNull null
            JSONObject().put(Key.result, result.text)
                .put(Key.type, result.barcodeFormat)
                .put(Key.timestamp, result.timestamp)
        }
    }

    protected fun encodeqrcode(text: String, correctionlevel: String): BitMatrix {
        val hints: MutableMap<EncodeHintType, Any?> = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[ERROR_CORRECTION] = correctionlevel
        hints[CHARACTER_SET] = Charsets.UTF_8.name()
        return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
    }
}