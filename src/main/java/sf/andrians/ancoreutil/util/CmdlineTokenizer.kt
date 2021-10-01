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

import sf.andrians.ancoreutil.util.text.DefaultSpaceUtil
import sf.andrians.ancoreutil.util.text.ISpaceUtil
import java.util.*

class CmdlineTokenizer {
    private var cmdline: CharSequence
    private val spaceUtil: ISpaceUtil = DefaultSpaceUtil.singleton
    private var length: Int
    private var offset = 0

    constructor(cmdline: CharSequence) {
        this.cmdline = cmdline
        length = cmdline.length
    }

    constructor(cmdline: CharSequence, start: Int) {
        this.cmdline = cmdline
        length = cmdline.length
        offset = start
    }

    fun tokens(): List<String> {
        val ret: MutableList<String> = ArrayList()
        var s = next()
        while (s != null) {
            ret.add(s.toString())
            s = next()
        }
        return ret
    }

    operator fun next(): CharSequence? {
        skipWhitespaces()
        if (offset >= length) return null
        val ret = StringBuilder()
        while (offset < length) {
            val c = cmdline[offset]
            if (spaceUtil.isWhitespace(c)) {
                break
            }
            if (c == '"') {
                dqword(ret)
                ++offset
                continue
            } else if (c == '\'') {
                qword(ret)
                ++offset
                continue
            } else if (c == '\\') {
                if (offset + 1 < length) {
                    ++offset
                }
            }
            ret.append(cmdline[offset])
            ++offset
        }
        return ret
    }

    private fun dqword(ret: StringBuilder) {
        ++offset
        if (offset >= length) {
            ret.append(cmdline[offset - 1])
            return
        }
        while (offset < length) {
            val c = cmdline[offset]
            if (c == '"') {
                break
            } else if (c == '\\') {
                if (offset + 1 < length) {
                    ++offset
                }
            }
            ret.append(c)
            ++offset
        }
    }

    private fun qword(ret: StringBuilder) {
        ++offset
        if (offset >= length) {
            ret.append(cmdline[offset - 1])
            return
        }
        while (offset < length) {
            val c = cmdline[offset]
            if (c == '\'') {
                break
            }
            ret.append(c)
            ++offset
        }
    }

    private fun skipWhitespaces() {
        while (offset < length
                && spaceUtil.isWhitespace(cmdline[offset])) {
            ++offset
        }
    }
}
