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
    //#BEGIN SINCE 2.9
    //#BEGIN MediaInfo
        final val Bitrate = "#XS7"
        final val Channels = "#X7h"
        final val CreationDate = "#XqK"
        final val DataUrl = "#XUn"
        final val Description = "#XNL"
        final val Duration = "#Xzr"
        final val Error = "#Xnv"
        final val FileDate = "#XP0"
        final val FileExists = "#XHR"
        final val FileSize = "#X86"
        final val FrameRate = "#X1w"
        final val Height = "#XWL"
        final val Id = "#Xt8"
        final val Mime = "#Xhy"
        final val Playable = "#XEP"
        final val Private = "#XsM"
        final val Rotation = "#XHA"
        final val SampleRate = "#Xm8"
        final val StagingId = "#Xoi"
        final val Title = "#XYA"
        final val TnInfo = "#XUq"
        final val Uri = "#XOu"
        final val Width = "#Xwt"
    //#END MediaInfo
    fun id(info: JSONObject): Long {
        return info.optLong(Id, -1)
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

    fun creationDate(info: JSONObject): Long {
        return info.optLong(CreationDate, 0)
    }

    fun lastModified(info: JSONObject): Long {
        return info.optLong(FileDate, 0)
    }

    fun size(info: JSONObject): Long {
        return info.optLong(FileSize, 0)
    }

    fun width(info: JSONObject): Int {
        return info.optInt(Width, -1)
    }

    fun height(info: JSONObject): Int {
        return info.optInt(Height, -1)
    }

    fun rotation(info: JSONObject): Int {
        return info.optInt(Rotation, -1)
    }

    fun duration(info: JSONObject): Double {
        return info.optDouble(Duration, -1.0)
    }

    fun bitrate(info: JSONObject): Int {
        return info.optInt(Bitrate, -1)
    }

    fun samplerate(info: JSONObject): Int {
        return info.optInt(SampleRate, -1)
    }

    fun framerate(info: JSONObject): Double {
        return info.optDouble(FrameRate, -1.0)
    }

    fun channels(info: JSONObject): Int {
        return info.optInt(Channels, -1)
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
    //#END SINCE 2.9
}