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
package sf.andrians.ancoreutil.util

import sf.andrians.ancoreutil.util.text.TextUtil
import java.util.*

object DateUtil {
    const val SECOND = 1000L
    const val MINUTE = 60 * SECOND
    const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    fun ms(): Long {
        return System.currentTimeMillis()
    }

    /**
     * @return The simple datetime string in form YYYYMMDD-hhmmss.
     */
    fun now(): String {
        return datetimeString(System.currentTimeMillis())
    }

    /**
     * @return The simple date string in form YYYYMMDD.
     */
    fun today(): String {
        return dateString(System.currentTimeMillis())
    }

    fun today(format: String): String {
        return TextUtil.format(format, today())
    }

    /**
     * @return Simple date string in form YYYYMMDD.
     */
    fun dateString(ms: Long): String {
        return TextUtil.format("%1\$tY%1\$tm%1\$td", ms)
    }

    /**
     * @return Simple date string in form YYYYMMDD.
     */
    fun dateString(date: Date): String {
        return TextUtil.format("%1\$tY%1\$tm%1\$td", date)
    }

    /**
     * @return Simple time string in form hhmmss
     */
    fun timeString(date: Date): String {
        return TextUtil.format("%1\$tH%1\$tM%1\$tS", date)
    }

    /**
     * @return Simple time string in form hhmmss
     */
    fun timeString(ms: Long): String {
        return TextUtil.format("%1\$tH%1\$tM%1\$tS", ms)
    }

    /**
     * @return Simple date time string in form YYYYMMDD-hhmmss
     */
    fun datetimeString(date: Date): String {
        return TextUtil.format("%1\$tY%1\$tm%1\$td-%1\$tH%1\$tM%1\$tS", date)
    }

    /**
     * @return Simple date time string in form YYYYMMDD-hhmmss
     */
    fun datetimeString(ms: Long): String {
        return TextUtil.format("%1\$tY%1\$tm%1\$td-%1\$tH%1\$tM%1\$tS", ms)
    }
}