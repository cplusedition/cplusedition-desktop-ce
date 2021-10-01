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

import sf.andrians.ancoreutil.util.text.TextUtil.format
import java.io.OutputStream
import java.io.PrintWriter

class Term {
    ////////////////////////////////////////////////////////////////////////
    private val stream: PrintWriter

    ////////////////////////////////////////////////////////////////////////
    constructor(s: OutputStream) {
        stream = ConsolePrintWriter(s)
    }

    constructor(s: PrintWriter) {
        stream = s
    }

    ////////////////////////////////////////////////////////////////////////
    fun print(msg: String?): Term {
        stream.print(msg)
        return this
    }

    fun println(msg: String?): Term {
        stream.println(msg)
        return this
    }

    fun println(): Term {
        stream.println()
        return this
    }

    fun printf(format: String, vararg args: Any): Term {
        stream.print(format(format, *args))
        return this
    }

    fun printlnf(format: String, vararg args: Any): Term {
        stream.println(format(format, *args))
        return this
    }

    ////////////////////////////////////////////////////////////////////////
    fun home(): Term {
        stream.print(CURSOR_HOME)
        return this
    }

    fun forwardY(y: Int): Term {
        stream.printf(CURSOR_FORWARD_Y, y)
        return this
    }

    fun backwardY(y: Int): Term {
        stream.printf(CURSOR_BACKWARD_Y, y)
        return this
    }

    fun gotoYX(y: Int, x: Int): Term {
        stream.printf(CURSOR_POSITION_YX, y, x)
        return this
    }

    fun gotoX(x: Int): Term {
        stream.printf(CURSOR_POSITION_X, x)
        return this
    }

    fun eraseX(x: Int): Term {
        stream.printf(ERASE_X, x)
        return this
    }

    fun eraseEOL(): Term {
        stream.print(ERASE_LINE_FORWARD)
        return this
    }

    fun eraseSOL(): Term {
        stream.print(ERASE_LINE_BACKWARD)
        return this
    }

    /**
     * Note: eraseLine() do not move cursor position.
     */
    fun eraseLine(): Term {
        stream.print(ERASE_LINE)
        return this
    }

    fun eraseEOP(): Term {
        stream.print(ERASE_PAGE_FORWARD)
        return this
    }

    fun eraseSOP(): Term {
        stream.print(ERASE_PAGE_BACKWARD)
        return this
    }

    /**
     * Note: erasePage() do not move cursor position.
     */
    fun erasePage(): Term {
        stream.print(ERASE_PAGE)
        return this
    }

    fun flush() {
        stream.flush()
    } ////////////////////////////////////////////////////////////////////////

    companion object {
        const val CURSOR_HOME = "\r"
        const val CURSOR_FORWARD_Y = "\u001b[%dE"
        const val CURSOR_BACKWARD_Y = "\u001b[%dF"
        const val CURSOR_POSITION_YX = "\u001b[%d;%dH" // Count from 1
        const val CURSOR_POSITION_X = "\u001b[%dG" // Count from 1
        const val ERASE_X = "\u001b[%dX" // 1 for cursor position only
        const val ERASE_LINE_FORWARD = "\u001b[0K"
        const val ERASE_LINE_BACKWARD = "\u001b[1K"
        const val ERASE_LINE = "\u001b[2K"
        const val ERASE_PAGE_FORWARD = "\u001b[0J"
        const val ERASE_PAGE_BACKWARD = "\u001b[1J"
        const val ERASE_PAGE = "\u001b[2J"
    }
}