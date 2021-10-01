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
package sf.andrians.ancoreutil.util.io

import java.io.IOException

interface IBinaryReader : IByteReader {

    @Throws(IOException::class)
    fun readI16(): Short

    @Throws(IOException::class)
    fun readU16(): Int

    @Throws(IOException::class)
    fun readI32(): Int

    @Throws(IOException::class)
    fun readI64(): Long

    @Throws(IOException::class)
    fun readFloat(): Float

    @Throws(IOException::class)
    fun readDouble(): Double

    //#BEGIN FIXME
    //#END FIXME

}
