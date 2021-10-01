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

import java.io.PrintWriter
import java.io.StringWriter

class StringPrintWriter : PrintWriter, CharSequence {

    private val stringWriter: StringWriter

    constructor() : super(StringWriter()) {
        stringWriter = out as StringWriter
    }

    constructor(initsize: Int) : super(StringWriter(initsize)) {
        stringWriter = out as StringWriter
    }

    constructor(w: StringWriter) : super(w) {
        stringWriter = w
    }

    ////////////////////////////////////////////////////////////////////

    val buffer: StringBuffer
        get() = stringWriter.buffer

    override fun toString(): String {
        return stringWriter.toString()
    }

    override fun get(index: Int): Char {
        return stringWriter.buffer[index]
    }

    override val length: Int
        get() {
            return stringWriter.buffer.length
        }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return stringWriter.buffer.subSequence(startIndex, endIndex)
    }

    ////////////////////////////////////////////////////////////////////
}