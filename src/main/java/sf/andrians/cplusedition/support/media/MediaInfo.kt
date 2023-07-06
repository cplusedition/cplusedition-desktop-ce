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

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.anjson.JSONUtil.stringOrNull
import org.json.JSONObject

object MediaInfo {
    final const val Bitrate = "#Xvx"
    final const val Channels = "#XVO"
    final const val CreationDate = "#XZy"
    final const val DataUrl = "#Xy5"
    final const val Description = "#XU5"
    final const val Duration = "#X6s"
    final const val Error = "#Xpv"
    final const val FileDate = "#X0w"
    final const val FileExists = "#Xob"
    final const val FileSize = "#Xzt"
    final const val FrameRate = "#Xs8"
    final const val Height = "#XNV"
    final const val Id = "#Xcu"
    final const val Mime = "#XBo"
    final const val Playable = "#X1P"
    final const val Private = "#XAs"
    final const val Rotation = "#XFw"
    final const val SampleRate = "#XE7"
    final const val StagingId = "#XHs"
    final const val Title = "#XFE"
    final const val TnInfo = "#Xu3"
    final const val Uri = "#XIE"
    final const val Width = "#XvV"
    fun id(info: JSONObject): Long {
        return info.optLong(Id, -1)
    }

    fun cpath(info: JSONObject): String? {
        return info.stringOrNull(Id)
    }

    fun mime(info: JSONObject): String? {
        return info.stringOrNull(Mime)
    }

    fun uri(info: JSONObject): String? {
        return info.stringOrNull(Uri)
    }

    fun title(info: JSONObject): String {
        return info.stringOrDef(Title, "")
    }

    fun description(info: JSONObject): String {
        return info.stringOrDef(Description, "")
    }

    fun creationDate(info: JSONObject, def: Long = 0): Long {
        return info.optLong(CreationDate, def)
    }

    fun lastModified(info: JSONObject, def: Long = 0): Long {
        return info.optLong(FileDate, def)
    }

    fun size(info: JSONObject, def: Long = 0): Long {
        return info.optLong(FileSize, def)
    }

    fun width(info: JSONObject, def: Int = -1): Int {
        return info.optInt(Width, def)
    }

    fun height(info: JSONObject, def: Int = -1): Int {
        return info.optInt(Height, def)
    }

    fun rotation(info: JSONObject, def: Int = -1): Int {
        return info.optInt(Rotation, def)
    }

    fun duration(info: JSONObject, def: Double = -1.0): Double {
        return info.optDouble(Duration, def)
    }

    fun bitrate(info: JSONObject, def: Int = -1): Int {
        return info.optInt(Bitrate, def)
    }

    fun samplerate(info: JSONObject, def: Int = -1): Int {
        return info.optInt(SampleRate, def)
    }

    fun framerate(info: JSONObject, def: Double = -1.0): Double {
        return info.optDouble(FrameRate, def)
    }

    fun channels(info: JSONObject, def: Int = -1): Int {
        return info.optInt(Channels, def)
    }

    fun isPlayable(info: JSONObject): Boolean {
        return info.optBoolean(Playable, false)
    }

    fun isPrivate(info: JSONObject): Boolean {
        return info.optBoolean(Private, false)
    }

    fun stagingId(info: JSONObject): String? {
        return info.stringOrNull(StagingId)
    }

    fun tnInfo(info: JSONObject): JSONObject? {
        return info.optJSONObject(TnInfo)
    }

    fun dataUrl(info: JSONObject?): String? {
        return info?.stringOrNull(DataUrl)
    }

    fun error(info: JSONObject?): String? {
        return info?.stringOrNull(Error)
    }

    interface Thumbnail {
        interface Kind {
            companion object {
                const val MINI_KIND = 1
                const val FULL_SCREEN_KIND = 2
                const val MICRO_KIND = 3
            }
        }

        interface Micro {
            companion object {
                const val WIDTH = 96
                const val HEIGHT = 96
            }
        }

        interface Mini {
            companion object {
                const val WIDTH = 512
                const val HEIGHT = 384
            }
        }
    }
}
