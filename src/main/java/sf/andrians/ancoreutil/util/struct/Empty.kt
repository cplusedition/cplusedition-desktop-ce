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
package sf.andrians.ancoreutil.util.struct

import java.sql.Timestamp

object Empty {
    val BYTE_ARRAY = ByteArray(0)
    val CHAR_ARRAY = CharArray(0)
    val SHORT_ARRAY = ShortArray(0)
    val INT_ARRAY = IntArray(0)
    val LONG_ARRAY = LongArray(0)
    val FLOAT_ARRAY = FloatArray(0)
    val DOUBLE_ARRAY = DoubleArray(0)
    val OBJECT_ARRAY = Array<Any>(0) { "" }
    val STRING_ARRAY = Array<String>(0) { "" }
    const val STRING = ""
    val TIMESTAMP = Timestamp(0)
}